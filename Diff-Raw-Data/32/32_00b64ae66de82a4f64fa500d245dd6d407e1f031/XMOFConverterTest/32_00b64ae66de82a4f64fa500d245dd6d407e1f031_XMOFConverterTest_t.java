 package org.modelexecution.fuml.convert.xmof;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import java.io.File;
 
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EEnum;
 import org.eclipse.emf.ecore.EEnumLiteral;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.EcoreFactory;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.junit.Test;
 import org.modelexecution.fuml.convert.IConversionResult;
 import org.modelexecution.xmof.Syntax.Activities.IntermediateActivities.Activity;
 import org.modelexecution.xmof.Syntax.Activities.IntermediateActivities.IntermediateActivitiesFactory;
 import org.modelexecution.xmof.Syntax.Classes.Kernel.BehavioredEClass;
 import org.modelexecution.xmof.Syntax.Classes.Kernel.BehavioredEOperation;
 import org.modelexecution.xmof.Syntax.Classes.Kernel.KernelFactory;
 
 import fUML.Syntax.Classes.Kernel.AggregationKind;
 import fUML.Syntax.Classes.Kernel.Association;
 import fUML.Syntax.Classes.Kernel.Class_;
 import fUML.Syntax.Classes.Kernel.Enumeration;
 import fUML.Syntax.Classes.Kernel.Operation;
 import fUML.Syntax.Classes.Kernel.Package;
 import fUML.Syntax.Classes.Kernel.Property;
 import fUML.Syntax.Classes.Kernel.PropertyList;
 
 public class XMOFConverterTest {
 
 	private static final String DEFAULT_PATH = "test.xmof";
 
 	@Test
 	public void testCanConvert() {
 		Resource resource = createResource();
 		EPackage ePackage = createDefaultTestPackage();
 		BehavioredEClass class1 = createBehavioredEClass("Test1");
 		ePackage.getEClassifiers().add(class1);
 		resource.getContents().add(ePackage);
 
 		// because it contains no activity
 		assertTrue(!new XMOFConverter().canConvert(resource));
 
 		// add activity
 		addEmptyActivity(class1);
 		assertTrue(new XMOFConverter().canConvert(resource));
 	}
 
 	@Test
 	public void testEPackageConversion() {
 		Resource resource = createResource();
 		EPackage ePackage = createDefaultTestPackage();
 		EPackage eSubpackage = EcoreFactory.eINSTANCE.createEPackage();
 		eSubpackage.setName("SubTest");
 		eSubpackage.setNsPrefix("subtest");
 		eSubpackage.setNsURI("http://test/sub");
 		ePackage.getESubpackages().add(eSubpackage);
 
 		BehavioredEClass class1 = createBehavioredEClass("Test1");
 		addEmptyActivity(class1);
 		eSubpackage.getEClassifiers().add(class1);
 		resource.getContents().add(ePackage);
 		
 		XMOFConverter converter = new XMOFConverter();
 		IConversionResult result = converter.convert(resource);
 
 		assertNotNull(result.getFUMLElement(class1));
 		assertTrue(result.getFUMLElement(class1) instanceof Class_);
 
 		Class_ fumlClass1 = (Class_) result.getFUMLElement(class1);
 		Package subPackage = fumlClass1.package_;
 		assertEquals(eSubpackage.getName(), subPackage.name);
 		assertTrue(subPackage.nestingPackage != null);
 		Package parentPackage = subPackage.nestingPackage;
 		assertEquals(ePackage.getName(), parentPackage.name);
 		assertTrue(parentPackage.nestedPackage.contains(subPackage));
 	}
 
 	@Test
 	public void testEClassConversion() {
 		Resource resource = createResource();
 		EPackage ePackage = createDefaultTestPackage();
 		BehavioredEClass class1 = createBehavioredEClass("Test1");
 		addEmptyActivity(class1);
 		class1.setAbstract(true);
 		ePackage.getEClassifiers().add(class1);
 		resource.getContents().add(ePackage);
 
 		XMOFConverter converter = new XMOFConverter();
 		IConversionResult result = converter.convert(resource);
 
 		assertNotNull(result.getFUMLElement(class1));
 		assertTrue(result.getFUMLElement(class1) instanceof Class_);
 
 		Class_ fumlClass1 = (Class_) result.getFUMLElement(class1);
 
 		assertEquals(class1.getName(), fumlClass1.name);
 		assertEquals(class1.isAbstract(), fumlClass1.isAbstract);
 	}
 
 	@Test
 	public void testEClassWithOneReference() {
 		Resource resource = createResource();
 		EPackage ePackage = createDefaultTestPackage();
 		BehavioredEClass class1 = createBehavioredEClass("Test1");
 		BehavioredEClass class2 = createBehavioredEClass("Test2");
 		addEmptyActivity(class1);
 		ePackage.getEClassifiers().add(class1);
 		ePackage.getEClassifiers().add(class2);
 		EReference reference = createReference("test", class2);
 		reference.setLowerBound(2);
 		reference.setUpperBound(12);
 		reference.setContainment(true);
 		class1.getEStructuralFeatures().add(reference);
 		resource.getContents().add(ePackage);
 
 		XMOFConverter converter = new XMOFConverter();
 		IConversionResult result = converter.convert(resource);
 
 		Class_ fumlClass1 = (Class_) result.getFUMLElement(class1);
 		Class_ fumlClass2 = (Class_) result.getFUMLElement(class2);
 		Association fumlAssociation = (Association) result
 				.getFUMLElement(reference);
 
 		// check property in source class
 		assertTrue(containsPropertyWithName(fumlClass1.ownedAttribute,
 				reference.getName()));
 		Property propertyInSourceClass = getPropertyByName(
 				fumlClass1.ownedAttribute, reference.getName());
 		assertEquals(reference.getLowerBound(),
 				propertyInSourceClass.multiplicityElement.lower);
 		assertEquals(reference.getUpperBound(),
 				propertyInSourceClass.multiplicityElement.upper.naturalValue);
 		assertEquals(reference.getEType().getName(),
 				propertyInSourceClass.typedElement.type.name);
 		assertEquals(fumlClass2, propertyInSourceClass.typedElement.type);
 		assertEquals(AggregationKind.composite,
 				propertyInSourceClass.aggregation);
 		assertEquals(fumlAssociation, propertyInSourceClass.association);
 
 		// check property in association
 		assertTrue(containsPropertyWithName(fumlAssociation.ownedEnd, reference
 				.getEContainingClass().getName().toLowerCase()));
 		Property propertyInAssociation = getPropertyByName(
 				fumlAssociation.ownedEnd, reference.getEContainingClass()
 						.getName().toLowerCase());
 		assertEquals(1, propertyInAssociation.multiplicityElement.lower);
 		assertEquals(1,
 				propertyInAssociation.multiplicityElement.upper.naturalValue);
 		assertEquals(reference.getEContainingClass().getName(),
 				propertyInAssociation.typedElement.type.name);
 		assertEquals(fumlClass1, propertyInAssociation.typedElement.type);
 		assertEquals(fumlAssociation, propertyInAssociation.association);
 
 		// check association
 		assertEquals(reference.getName(), fumlAssociation.name);
 		assertTrue(fumlAssociation.memberEnd.contains(propertyInAssociation));
 		assertTrue(fumlAssociation.memberEnd.contains(propertyInSourceClass));
		assertEquals(2, fumlAssociation.memberEnd.size());
		assertEquals(1, fumlAssociation.ownedEnd.size());
 	}
 
 	@Test
 	public void testEClassWithTwoOppositeReferences() {
 		Resource resource = createResource();
 		EPackage ePackage = createDefaultTestPackage();
 		BehavioredEClass class1 = createBehavioredEClass("Test1");
 		BehavioredEClass class2 = createBehavioredEClass("Test2");
 		addEmptyActivity(class1);
 		ePackage.getEClassifiers().add(class1);
 		ePackage.getEClassifiers().add(class2);
 
 		EReference reference = createReference("test1ToTest2", class2);
 		reference.setLowerBound(2);
 		reference.setUpperBound(12);
 		reference.setContainment(true);
 		class1.getEStructuralFeatures().add(reference);
 
 		EReference oppositeReference = createReference("test2ToTest1", class1);
 		oppositeReference.setLowerBound(1);
 		oppositeReference.setUpperBound(1);
 		oppositeReference.setContainment(false);
 		oppositeReference.setEOpposite(reference);
 		reference.setEOpposite(oppositeReference);
 		class2.getEStructuralFeatures().add(oppositeReference);
 
 		resource.getContents().add(ePackage);
 
 		XMOFConverter converter = new XMOFConverter();
 		IConversionResult result = converter.convert(resource);
 
 		// test both references are mapped to the same association
 		Association fumlAssociation = (Association) result
 				.getFUMLElement(reference);
 		assertTrue(result.getFUMLElement(reference) != null);
 		assertTrue(result.getFUMLElement(oppositeReference) != null);
 		assertTrue(result.getFUMLElement(oppositeReference) == result
 				.getFUMLElement(reference));
 
 		Class_ fumlClass1 = (Class_) result.getFUMLElement(class1);
 		Class_ fumlClass2 = (Class_) result.getFUMLElement(class2);
 
 		// check property in source class
 		assertTrue(containsPropertyWithName(fumlClass1.ownedAttribute,
 				reference.getName()));
 		Property property = getPropertyByName(fumlClass1.ownedAttribute,
 				reference.getName());
 		assertEquals(reference.getLowerBound(),
 				property.multiplicityElement.lower);
 		assertEquals(reference.getUpperBound(),
 				property.multiplicityElement.upper.naturalValue);
 		assertEquals(reference.getEType().getName(),
 				property.typedElement.type.name);
 		assertEquals(fumlClass2, property.typedElement.type);
 		assertEquals(AggregationKind.composite, property.aggregation);
 		assertEquals(fumlAssociation, property.association);
 
 		// check property in target class
 		assertTrue(containsPropertyWithName(fumlClass2.ownedAttribute,
 				oppositeReference.getName()));
 		Property oppositeProperty = getPropertyByName(
 				fumlClass2.ownedAttribute, oppositeReference.getName());
 		assertEquals(oppositeReference.getLowerBound(),
 				oppositeProperty.multiplicityElement.lower);
 		assertEquals(oppositeReference.getUpperBound(),
 				oppositeProperty.multiplicityElement.upper.naturalValue);
 		assertEquals(oppositeReference.getEType().getName(),
 				oppositeProperty.typedElement.type.name);
 		assertEquals(fumlClass1, oppositeProperty.typedElement.type);
 		assertEquals(fumlAssociation, oppositeProperty.association);
		
		// check opposite
		assertEquals(property.opposite, oppositeProperty);
		assertEquals(oppositeProperty.opposite, property);
 
 		// check association
 		assertTrue(fumlAssociation.memberEnd.contains(property));
 		assertTrue(fumlAssociation.memberEnd.contains(oppositeProperty));
		assertEquals(2, fumlAssociation.memberEnd.size());
		assertEquals(0, fumlAssociation.ownedEnd.size());
 	}
 
 	@Test
 	public void testEClassWithSuperType() {
 		Resource resource = createResource();
 		EPackage ePackage = createDefaultTestPackage();
 		BehavioredEClass class1 = createBehavioredEClass("Test1");
 		BehavioredEClass class2 = createBehavioredEClass("Test2");
 		addEmptyActivity(class1);
 		class2.getESuperTypes().add(class1);
 		ePackage.getEClassifiers().add(class1);
 		ePackage.getEClassifiers().add(class2);
 		resource.getContents().add(ePackage);
 
 		XMOFConverter converter = new XMOFConverter();
 		IConversionResult result = converter.convert(resource);
 
 		Class_ fumlClass1 = (Class_) result.getFUMLElement(class1);
 		Class_ fumlClass2 = (Class_) result.getFUMLElement(class2);
 
 		assertEquals(1, fumlClass2.generalization.size());
 		assertEquals(1, fumlClass2.general.size());
 
 		assertEquals(fumlClass1, fumlClass2.general.get(0));
 		assertEquals(fumlClass1, fumlClass2.generalization.get(0).general);
 		assertEquals(fumlClass2, fumlClass2.generalization.get(0).specific);
 	}
 
 	@Test
 	public void testEClassWithEOperation() {
 		Resource resource = createResource();
 		EPackage ePackage = createDefaultTestPackage();
 		BehavioredEClass class1 = createBehavioredEClass("Test1");
 		addEmptyActivity(class1);
 		ePackage.getEClassifiers().add(class1);
 		resource.getContents().add(ePackage);
 
 		XMOFConverter converter = new XMOFConverter();
 		IConversionResult result = converter.convert(resource);
 
 		Class_ fumlClass1 = (Class_) result.getFUMLElement(class1);
 		assertEquals(1, fumlClass1.ownedOperation.size());
 
 		Operation fumlOperation = fumlClass1.ownedOperation.get(0);
 		assertEquals(1, fumlOperation.method.size());
 		assertEquals("TestActivity", fumlOperation.method.get(0).name);
 	}
 
 	@Test
 	public void testEClassWithEAttribute() {
 		Resource resource = createResource();
 		EPackage ePackage = createDefaultTestPackage();
 		BehavioredEClass class1 = createBehavioredEClass("Test1");
 		addEmptyActivity(class1);
 		EAttribute eAttribute = EcoreFactory.eINSTANCE.createEAttribute();
 		eAttribute.setLowerBound(2);
 		eAttribute.setUpperBound(4);
 		eAttribute.setChangeable(false);
 		eAttribute.setUnique(true);
 		eAttribute.setOrdered(false);
 		class1.getEStructuralFeatures().add(eAttribute);
 		ePackage.getEClassifiers().add(class1);
 		resource.getContents().add(ePackage);
 
 		XMOFConverter converter = new XMOFConverter();
 		IConversionResult result = converter.convert(resource);
 
 		Class_ fumlClass1 = (Class_) result.getFUMLElement(class1);
 		assertEquals(1, fumlClass1.ownedAttribute.size());
 
 		Property property = (Property) result.getFUMLElement(eAttribute);
 		assertEquals(property, fumlClass1.ownedAttribute.get(0));
 
 		assertEquals(2, property.multiplicityElement.lower);
 		assertEquals(4, property.multiplicityElement.upper.naturalValue);
 
 		assertEquals(!eAttribute.isChangeable(), property.isReadOnly);
 		assertEquals(eAttribute.isUnique(),
 				property.multiplicityElement.isUnique);
 		assertEquals(eAttribute.isOrdered(),
 				property.multiplicityElement.isOrdered);
 	}
 
 	@Test
 	public void testEClassWithAttributeOfTypeEEnum() {
 		Resource resource = createResource();
 		EPackage ePackage = createDefaultTestPackage();
 		BehavioredEClass class1 = createBehavioredEClass("Test1");
 		addEmptyActivity(class1);
 		EEnum eEnum = EcoreFactory.eINSTANCE.createEEnum();
 		eEnum.setName("Visibility");
 		EEnumLiteral eEnumLiteral1 = EcoreFactory.eINSTANCE
 				.createEEnumLiteral();
 		eEnumLiteral1.setName("public");
 		eEnum.getELiterals().add(eEnumLiteral1);
 		EAttribute eAttribute = EcoreFactory.eINSTANCE.createEAttribute();
 		eAttribute.setName("visibility");
 		eAttribute.setEType(eEnum);
 		class1.getEStructuralFeatures().add(eAttribute);
 		ePackage.getEClassifiers().add(class1);
 		ePackage.getEClassifiers().add(eEnum);
 		resource.getContents().add(ePackage);
 
 		XMOFConverter converter = new XMOFConverter();
 		IConversionResult result = converter.convert(resource);
 
 		Class_ fumlClass1 = (Class_) result.getFUMLElement(class1);
 		Enumeration fumlEnumeration = (Enumeration) result
 				.getFUMLElement(eEnum);
 
 		Property attribute = fumlClass1.ownedAttribute.get(0);
 		assertEquals(fumlEnumeration, attribute.typedElement.type);
 
 		assertEquals(1, fumlEnumeration.ownedLiteral.size());
 		assertEquals(eEnumLiteral1.getName(),
 				fumlEnumeration.ownedLiteral.get(0).name);
 		assertEquals(fumlEnumeration,
 				fumlEnumeration.ownedLiteral.get(0).enumeration);
 	}
 
 	private boolean containsPropertyWithName(PropertyList properties,
 			String name) {
 		return getPropertyByName(properties, name) != null;
 	}
 
 	private Property getPropertyByName(PropertyList properties, String name) {
 		for (Property property : properties) {
 			if (name.equals(property.name)) {
 				return property;
 			}
 		}
 		return null;
 	}
 
 	private EReference createReference(String name, EClass eType) {
 		EReference reference = EcoreFactory.eINSTANCE.createEReference();
 		reference.setName(name);
 		reference.setEType(eType);
 		return reference;
 	}
 
 	private BehavioredEClass createBehavioredEClass(String name) {
 		BehavioredEClass class1 = KernelFactory.eINSTANCE
 				.createBehavioredEClass();
 		class1.setName(name);
 		return class1;
 	}
 
 	private EPackage createDefaultTestPackage() {
 		EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
 		ePackage.setName("Test");
 		ePackage.setNsPrefix("test");
 		ePackage.setNsURI("http://test");
 		return ePackage;
 	}
 
 	private void addEmptyActivity(BehavioredEClass eClass) {
 		Activity activity = IntermediateActivitiesFactory.eINSTANCE
 				.createActivity();
 		activity.setName("TestActivity");
 		BehavioredEOperation operation = KernelFactory.eINSTANCE
 				.createBehavioredEOperation();
 		operation.setName("testOperation");
 		operation.getMethod().add(activity);
 		eClass.getEOperations().add(operation);
 		eClass.getOwnedBehavior().add(activity);
 	}
 
 	private Resource createResource() {
 		return new ResourceSetImpl().createResource(URI.createFileURI(new File(
 				DEFAULT_PATH).getAbsolutePath()));
 	}
 
 }
