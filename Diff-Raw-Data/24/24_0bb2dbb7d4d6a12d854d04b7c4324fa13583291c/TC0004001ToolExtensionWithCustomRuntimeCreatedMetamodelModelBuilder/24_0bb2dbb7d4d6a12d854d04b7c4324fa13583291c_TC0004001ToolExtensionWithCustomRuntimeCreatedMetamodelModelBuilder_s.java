 package org.eclipse.rmf.reqif10.tests.uc000.tc4001;
 
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EFactory;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.ETypedElement;
 import org.eclipse.emf.ecore.EcoreFactory;
 import org.eclipse.rmf.reqif10.ReqIF;
 import org.eclipse.rmf.reqif10.common.util.ReqIFToolExtensionUtil;
 import org.eclipse.rmf.reqif10.tests.util.MinimalModelBuilder;
 
 public class TC0004001ToolExtensionWithCustomRuntimeCreatedMetamodelModelBuilder extends MinimalModelBuilder {
 
 	EPackage metamodel;
 
 	EClass mmNodeWithReferencesUnsetable;
 	EReference mmSingleUnsetDefaultValueUnsetable;
 	EReference mmSingleSetNotDefaultValueUnsetable;
 	EReference mmSingleSetDefaultValueUnsetable;
 	EReference mmManyUnssetDefaultValueUnsetable;
 	EReference mmManySetNotDefaultValueUnsetable;
 	EReference mmManySetDefaultValueUnsetable;
 
 	EClass mmNodeWithReferences;
 	EReference mmSingleUnsetDefaultValue;
 	EReference mmSingleSetNotDefaultValue;
 	EReference mmManyUnssetDefaultValue;
 	EReference mmManySetNotDefaultValue;
 
 	public TC0004001ToolExtensionWithCustomRuntimeCreatedMetamodelModelBuilder() throws Exception {
 		super();
 		createMetamodel();
 	}
 
 	@Override
 	public void createToolExtensions() throws Exception {
 		ReqIF reqIF = getReqIF();
 
 		EFactory factory = metamodel.getEFactoryInstance();
 
 		// ***************************
 		// nodeWithReferencesUnsetable
 		//
 		EObject nodeWithReferencesUnsetable_root = factory.create(mmNodeWithReferencesUnsetable);
 
 		// do nothing with mmSingleUnsetDefaultValueUnsetable
 
 		// mmSingleSetNotDefaultValueUnsetable
 		EObject nodeWithReferencesUnsetable_1 = factory.create(mmNodeWithReferencesUnsetable);
 		nodeWithReferencesUnsetable_root.eSet(mmSingleSetNotDefaultValueUnsetable, nodeWithReferencesUnsetable_1);
 
 		// mmSingleSetDefaultValueUnsetable
 		nodeWithReferencesUnsetable_root.eSet(mmSingleSetDefaultValueUnsetable, null);
 
 		// do nothing for mmManyUnssetDefaultValueUnsetable
 
 		// mmManySetNotDefaultValueUnsetable
 		EObject nodeWithReferencesUnsetable_2 = factory.create(mmNodeWithReferencesUnsetable);
 		((EList<EObject>) nodeWithReferencesUnsetable_root.eGet(mmManySetNotDefaultValueUnsetable)).add(nodeWithReferencesUnsetable_2);
 
 		// mmManySetDefaultValueUnsetable
 		((EList<EObject>) nodeWithReferencesUnsetable_root.eGet(mmManySetDefaultValueUnsetable)).clear();
 
 		// hook the tool extension model into the tool extension class
 		ReqIFToolExtensionUtil.addToolExtension(reqIF, nodeWithReferencesUnsetable_root);
 
 		// ******************
 		// nodeWithReferences
 		//
 		EObject nodeWithReferences_root = factory.create(mmNodeWithReferences);
 
 		// do nothing with mmSingleUnsetDefaultValue
 
 		// mmSingleSetNotDefaultValue
		EObject nodeWithReferences_1 = factory.create(mmNodeWithReferences);
 		nodeWithReferences_root.eSet(mmSingleSetNotDefaultValue, null);
 
 		// do nothing for mmManyUnssetDefaultValue
 
 		// mmManySetNotDefaultValue
 		EObject nodeWithReferences_2 = factory.create(mmNodeWithReferences);
 		((EList<EObject>) nodeWithReferences_root.eGet(mmManySetNotDefaultValue)).add(nodeWithReferences_2);
 
 		ReqIFToolExtensionUtil.addToolExtension(reqIF, nodeWithReferences_root);
 
 	}
 
 	private void createMetamodel() {
 		metamodel = EcoreFactory.eINSTANCE.createEPackage();
 		metamodel.setName("MyToolExtension");
 		metamodel.setNsPrefix("mm");
 		metamodel.setNsURI("http://www.eclipse.org/rmf/test/mm");
 
 		// unsetable references
 		{
 			mmNodeWithReferencesUnsetable = EcoreFactory.eINSTANCE.createEClass();
 			mmNodeWithReferencesUnsetable.setName("NodeWithReferencesUnsetable");
 
 			mmSingleUnsetDefaultValueUnsetable = EcoreFactory.eINSTANCE.createEReference();
 			mmSingleUnsetDefaultValueUnsetable.setName("singleUnsetDefaultValueUnsetable");
 			mmSingleUnsetDefaultValueUnsetable.setLowerBound(0);
 			mmSingleUnsetDefaultValueUnsetable.setUpperBound(1);
 			mmSingleUnsetDefaultValueUnsetable.setUnsettable(true);
 			mmSingleUnsetDefaultValueUnsetable.setContainment(true);
 			mmSingleUnsetDefaultValueUnsetable.setEType(mmNodeWithReferencesUnsetable);
 
 			mmSingleSetNotDefaultValueUnsetable = EcoreFactory.eINSTANCE.createEReference();
 			mmSingleSetNotDefaultValueUnsetable.setName("singleSetNotDefaultValueUnsetable");
 			mmSingleSetNotDefaultValueUnsetable.setLowerBound(0);
 			mmSingleSetNotDefaultValueUnsetable.setUpperBound(1);
 			mmSingleSetNotDefaultValueUnsetable.setUnsettable(true);
 			mmSingleSetNotDefaultValueUnsetable.setContainment(true);
 			mmSingleSetNotDefaultValueUnsetable.setEType(mmNodeWithReferencesUnsetable);
 
 			mmSingleSetDefaultValueUnsetable = EcoreFactory.eINSTANCE.createEReference();
 			mmSingleSetDefaultValueUnsetable.setName("singleSetDefaultValueUnsetable");
 			mmSingleSetDefaultValueUnsetable.setLowerBound(0);
 			mmSingleSetDefaultValueUnsetable.setUpperBound(1);
 			mmSingleSetDefaultValueUnsetable.setUnsettable(true);
 			mmSingleSetDefaultValueUnsetable.setContainment(true);
 			mmSingleSetDefaultValueUnsetable.setEType(mmNodeWithReferencesUnsetable);
 
 			mmManyUnssetDefaultValueUnsetable = EcoreFactory.eINSTANCE.createEReference();
 			mmManyUnssetDefaultValueUnsetable.setName("manyUnssetDefaultValueUnsetable");
 			mmManyUnssetDefaultValueUnsetable.setLowerBound(0);
 			mmManyUnssetDefaultValueUnsetable.setUpperBound(ETypedElement.UNBOUNDED_MULTIPLICITY);
 			mmManyUnssetDefaultValueUnsetable.setUnsettable(true);
 			mmManyUnssetDefaultValueUnsetable.setContainment(true);
 			mmManyUnssetDefaultValueUnsetable.setEType(mmNodeWithReferencesUnsetable);
 
 			mmManySetNotDefaultValueUnsetable = EcoreFactory.eINSTANCE.createEReference();
 			mmManySetNotDefaultValueUnsetable.setName("manySetNotDefaultValueUnsetable");
 			mmManySetNotDefaultValueUnsetable.setLowerBound(0);
 			mmManySetNotDefaultValueUnsetable.setUpperBound(ETypedElement.UNBOUNDED_MULTIPLICITY);
 			mmManySetNotDefaultValueUnsetable.setUnsettable(true);
 			mmManySetNotDefaultValueUnsetable.setContainment(true);
 			mmManySetNotDefaultValueUnsetable.setEType(mmNodeWithReferencesUnsetable);
 
 			mmManySetDefaultValueUnsetable = EcoreFactory.eINSTANCE.createEReference();
 			mmManySetDefaultValueUnsetable.setName("manySetDefaultValueUnsetable");
 			mmManySetDefaultValueUnsetable.setLowerBound(0);
 			mmManySetDefaultValueUnsetable.setUpperBound(ETypedElement.UNBOUNDED_MULTIPLICITY);
 			mmManySetDefaultValueUnsetable.setUnsettable(true);
 			mmManySetDefaultValueUnsetable.setContainment(true);
 			mmManySetDefaultValueUnsetable.setEType(mmNodeWithReferencesUnsetable);
 
 			mmNodeWithReferencesUnsetable.getEStructuralFeatures().add(mmSingleUnsetDefaultValueUnsetable);
 			mmNodeWithReferencesUnsetable.getEStructuralFeatures().add(mmSingleSetNotDefaultValueUnsetable);
 			mmNodeWithReferencesUnsetable.getEStructuralFeatures().add(mmSingleSetDefaultValueUnsetable);
 			mmNodeWithReferencesUnsetable.getEStructuralFeatures().add(mmManyUnssetDefaultValueUnsetable);
 			mmNodeWithReferencesUnsetable.getEStructuralFeatures().add(mmManySetNotDefaultValueUnsetable);
 			mmNodeWithReferencesUnsetable.getEStructuralFeatures().add(mmManySetDefaultValueUnsetable);
 
 			metamodel.getEClassifiers().add(mmNodeWithReferencesUnsetable);
 		}
 
 		// unsetable attributes
 		{
 			EClass nodeWithAttributesUnsetable = EcoreFactory.eINSTANCE.createEClass();
 
 			nodeWithAttributesUnsetable.setName("NodeWithAttributesUnsetable");
 
 			metamodel.getEClassifiers().add(nodeWithAttributesUnsetable);
 		}
 
 		// not unsetable references
 		{
 			mmNodeWithReferences = EcoreFactory.eINSTANCE.createEClass();
 			mmNodeWithReferences.setName("NodeWithReferences");
 
 			mmSingleUnsetDefaultValue = EcoreFactory.eINSTANCE.createEReference();
 			mmSingleUnsetDefaultValue.setName("singleUnsetDefaultValue");
 			mmSingleUnsetDefaultValue.setLowerBound(0);
 			mmSingleUnsetDefaultValue.setUpperBound(1);
 			mmSingleUnsetDefaultValue.setUnsettable(false);
 			mmSingleUnsetDefaultValue.setContainment(true);
 			mmSingleUnsetDefaultValue.setEType(mmNodeWithReferences);
 
 			mmSingleSetNotDefaultValue = EcoreFactory.eINSTANCE.createEReference();
 			mmSingleSetNotDefaultValue.setName("singleSetNotDefaultValue");
 			mmSingleSetNotDefaultValue.setLowerBound(0);
 			mmSingleSetNotDefaultValue.setUpperBound(1);
 			mmSingleSetNotDefaultValue.setUnsettable(false);
 			mmSingleSetNotDefaultValue.setContainment(true);
 			mmSingleSetNotDefaultValue.setEType(mmNodeWithReferences);
 
 			mmManyUnssetDefaultValue = EcoreFactory.eINSTANCE.createEReference();
 			mmManyUnssetDefaultValue.setName("manyUnssetDefaultValue");
 			mmManyUnssetDefaultValue.setLowerBound(0);
 			mmManyUnssetDefaultValue.setUpperBound(ETypedElement.UNBOUNDED_MULTIPLICITY);
 			mmManyUnssetDefaultValue.setUnsettable(false);
 			mmManyUnssetDefaultValue.setContainment(true);
 			mmManyUnssetDefaultValue.setEType(mmNodeWithReferences);
 
 			mmManySetNotDefaultValue = EcoreFactory.eINSTANCE.createEReference();
 			mmManySetNotDefaultValue.setName("manySetNotDefaultValue");
 			mmManySetNotDefaultValue.setLowerBound(0);
 			mmManySetNotDefaultValue.setUpperBound(ETypedElement.UNBOUNDED_MULTIPLICITY);
 			mmManySetNotDefaultValue.setUnsettable(false);
 			mmManySetNotDefaultValue.setContainment(true);
 			mmManySetNotDefaultValue.setEType(mmNodeWithReferences);
 
 			mmNodeWithReferences.getEStructuralFeatures().add(mmSingleUnsetDefaultValue);
 			mmNodeWithReferences.getEStructuralFeatures().add(mmSingleSetNotDefaultValue);
 			mmNodeWithReferences.getEStructuralFeatures().add(mmManyUnssetDefaultValue);
 			mmNodeWithReferences.getEStructuralFeatures().add(mmManySetNotDefaultValue);
 
 			metamodel.getEClassifiers().add(mmNodeWithReferences);
 
 		}
 
 		// not unsetable attributes
 		{
 			EClass nodeWithAttributes = EcoreFactory.eINSTANCE.createEClass();
 			nodeWithAttributes.setName("NodeWithAttributes");
 
 			metamodel.getEClassifiers().add(nodeWithAttributes);
 		}
 
 		EPackage.Registry.INSTANCE.put(metamodel.getNsURI(), metamodel);
 	}
 
 }
