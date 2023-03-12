package org.springframework.samples.petclinic;

import com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.CompositeArchRule;
import com.tngtech.archunit.library.Architectures;
import jakarta.persistence.Entity;
import org.jmolecules.archunit.JMoleculesArchitectureRules;
import org.springframework.data.repository.Repository;
import org.springframework.stereotype.Controller;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.assignableTo;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.properties.CanBeAnnotated.Predicates.annotatedWith;
import static com.tngtech.archunit.lang.conditions.ArchConditions.dependOnClassesThat;
import static com.tngtech.archunit.lang.conditions.ArchConditions.not;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

@AnalyzeClasses(packagesOf = PetClinicApplication.class, importOptions = DoNotIncludeTests.class)
class ArchitectureTests {

	@ArchTest
	public static final ArchRule CONTROLLER_NAMING = classes()
		.that().areAnnotatedWith(Controller.class)
		.or().haveSimpleNameEndingWith("Controller")
		.should().beAnnotatedWith(Controller.class)
		.andShould().haveSimpleNameEndingWith("Controller")
		.because("controller should be easy to find");

	@ArchTest
	public static final ArchRule DEPENDENCIES_BETWEEN_MODULES = CompositeArchRule
		.of(
			classes()
				.that().resideInAPackage("..owner..")
				.should(not(dependOnClassesThat(resideInAPackage("..vet..")))))
		.and(
			classes()
				.that().resideInAPackage("..vet..")
				.should(not(dependOnClassesThat(resideInAPackage("..owner..")))));

	@ArchTest
	public static final ArchRule LAYERS = Architectures.layeredArchitecture()
		.consideringOnlyDependenciesInLayers()
		.layer("Controller").definedBy(annotatedWith(Controller.class))
		.layer("Domain").definedBy(assignableTo(Repository.class))
		.layer("Persistence").definedBy(annotatedWith(Entity.class))
		.whereLayer("Controller").mayNotBeAccessedByAnyLayer()
		.whereLayer("Domain").mayOnlyBeAccessedByLayers("Controller")
		.whereLayer("Persistence").mayOnlyBeAccessedByLayers("Controller", "Domain");

	@ArchTest
	public static final ArchRule JMOLECULES_LAYERS = JMoleculesArchitectureRules.ensureLayering();

}
