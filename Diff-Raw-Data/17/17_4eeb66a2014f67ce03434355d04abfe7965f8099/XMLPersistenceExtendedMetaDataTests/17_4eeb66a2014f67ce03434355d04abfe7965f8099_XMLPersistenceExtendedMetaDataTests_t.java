 /**
  * Copyright (c) 2013 itemis AG.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *   itemis AG - initial API and implementation
  */
 package org.eclipse.rmf.tests.serialization.metadata;
 
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertSame;
 
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EClassifier;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.EcorePackage;
 import org.eclipse.rmf.serialization.XMLPersistenceMappingExtendedMetaData;
 import org.eclipse.rmf.serialization.XMLPersistenceMappingExtendedMetaDataImpl;
 import org.eclipse.rmf.tests.serialization.model.nodes.NodesPackage;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 @SuppressWarnings("nls")
 public class XMLPersistenceExtendedMetaDataTests {
 	int[] identitySerializationStructureConfiguration = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 }; // identity
 																													// mapping
 
 	@BeforeClass
 	public static void setup() {
 		EPackage.Registry.INSTANCE.put(NodesPackage.eNS_URI, NodesPackage.eINSTANCE);
		EPackage.Registry.INSTANCE.put(EcorePackage.eNS_URI, EcorePackage.eINSTANCE);
 	}
 
 	@Test
 	public void testMetadataIsSingleton() {
 		XMLPersistenceMappingExtendedMetaData metadata1 = XMLPersistenceMappingExtendedMetaData.INSTANCE;
 		XMLPersistenceMappingExtendedMetaData metadata2 = XMLPersistenceMappingExtendedMetaData.INSTANCE;
 		assertSame(metadata1, metadata2);
 	}
 
 	@Test
 	public void testGetFeatureSerializationStructureSingle() {
 		XMLPersistenceMappingExtendedMetaData metadata = new XMLPersistenceMappingExtendedMetaDataImpl(identitySerializationStructureConfiguration);
 
 		EClass nodeEClass = NodesPackage.eINSTANCE.getNode();
 		for (int i = 0; i < identitySerializationStructureConfiguration.length; i++) {
 			String featureName = "eReference_Contained" + toBinaryString(i) + "Single";
 			EStructuralFeature feature = nodeEClass.getEStructuralFeature(featureName);
 			assertNotNull("EReference '" + featureName + "' not found", feature);
 
 			int featureSerializationStructure = metadata.getFeatureSerializationStructure(feature);
 			assertSame(i, featureSerializationStructure);
 		}
 	}
 
 	@Test
 	public void testGetFeatureSerializationStructureMany() {
 		XMLPersistenceMappingExtendedMetaData metadata = new XMLPersistenceMappingExtendedMetaDataImpl(identitySerializationStructureConfiguration);
 
 		EClass nodeEClass = NodesPackage.eINSTANCE.getNode();
 		for (int i = 0; i < identitySerializationStructureConfiguration.length; i++) {
 			String featureName = "eReference_Contained" + toBinaryString(i) + "Many";
 			EStructuralFeature feature = nodeEClass.getEStructuralFeature(featureName);
 			assertNotNull("EReference '" + featureName + "' not found", feature);
 
 			int featureSerializationStructure = metadata.getFeatureSerializationStructure(feature);
 			assertSame(i, featureSerializationStructure);
 		}
 	}
 
 	@Test
 	public void testGetFeatureSerializationStructure_EReference_ReferencedxxxxSingle() {
 		XMLPersistenceMappingExtendedMetaData metadata = new XMLPersistenceMappingExtendedMetaDataImpl(identitySerializationStructureConfiguration);
 
 		EClass nodeEClass = NodesPackage.eINSTANCE.getNode();
 		for (int i = 0; i < identitySerializationStructureConfiguration.length; i++) {
 			String featureName = "eReference_Referenced" + toBinaryString(i) + "Single";
 			EStructuralFeature feature = nodeEClass.getEStructuralFeature(featureName);
 			assertNotNull("EReference '" + featureName + "' not found", feature);
 
 			int featureSerializationStructure = metadata.getFeatureSerializationStructure(feature);
 			assertSame(i, featureSerializationStructure);
 		}
 	}
 
 	@Test
 	public void testGetFeatureSerializationStructure_EReference_ReferencedxxxxMany() {
 		XMLPersistenceMappingExtendedMetaData metadata = new XMLPersistenceMappingExtendedMetaDataImpl(identitySerializationStructureConfiguration);
 
 		EClass nodeEClass = NodesPackage.eINSTANCE.getNode();
 		for (int i = 0; i < identitySerializationStructureConfiguration.length; i++) {
 			String featureName = "eReference_Referenced" + toBinaryString(i) + "Many";
 			EStructuralFeature feature = nodeEClass.getEStructuralFeature(featureName);
 			assertNotNull("EReference '" + featureName + "' not found", feature);
 
 			int featureSerializationStructure = metadata.getFeatureSerializationStructure(feature);
 			assertSame(i, featureSerializationStructure);
 		}
 	}
 
 	@Test
 	public void testGetFeatureSerializationStructure_EAttribute_AttributexxxxSingle() {
 		XMLPersistenceMappingExtendedMetaData metadata = new XMLPersistenceMappingExtendedMetaDataImpl(identitySerializationStructureConfiguration);
 
 		EClass nodeEClass = NodesPackage.eINSTANCE.getNode();
 		for (int i = 0; i < identitySerializationStructureConfiguration.length; i++) {
 			String featureName = "eAttribute_Attribute" + toBinaryString(i) + "Single";
 			EStructuralFeature feature = nodeEClass.getEStructuralFeature(featureName);
 			assertNotNull("EAttribute '" + featureName + "' not found", feature);
 
 			int featureSerializationStructure = metadata.getFeatureSerializationStructure(feature);
 			assertSame(i, featureSerializationStructure);
 		}
 	}
 
 	@Test
 	public void testGetFeatureSerializationStructure_EAttribute_AttributexxxxMany() {
 		XMLPersistenceMappingExtendedMetaData metadata = new XMLPersistenceMappingExtendedMetaDataImpl(identitySerializationStructureConfiguration);
 
 		EClass nodeEClass = NodesPackage.eINSTANCE.getNode();
 		for (int i = 0; i < identitySerializationStructureConfiguration.length; i++) {
 			String featureName = "eAttribute_Attribute" + toBinaryString(i) + "Many";
 			EStructuralFeature feature = nodeEClass.getEStructuralFeature(featureName);
 			assertNotNull("Attribute '" + featureName + "' not found", feature);
 
 			int featureSerializationStructure = metadata.getFeatureSerializationStructure(feature);
 			assertSame(i, featureSerializationStructure);
 		}
 	}
 
 	@Test
 	public void testGetFeatureSerializationStructure_EmptyAnnotation_Single() {
 		XMLPersistenceMappingExtendedMetaData metadata = new XMLPersistenceMappingExtendedMetaDataImpl(identitySerializationStructureConfiguration);
 
 		EStructuralFeature feature = NodesPackage.eINSTANCE.getNode_EReference_EmptyAnnotationSingle();
 		int featureSerializationStructure = metadata.getFeatureSerializationStructure(feature);
 		assertSame(XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__1001__FEATURE_WRAPPER_ELEMENT__CLASSIFIER_ELEMENT,
 				featureSerializationStructure);
 	}
 
 	@Test
 	public void testGetFeatureSerializationStructure_NoAnnotation_Single() {
 		XMLPersistenceMappingExtendedMetaData metadata = new XMLPersistenceMappingExtendedMetaDataImpl(identitySerializationStructureConfiguration);
 
 		EStructuralFeature feature = NodesPackage.eINSTANCE.getNode_EReference_NoAnnotationSingle();
 		int featureSerializationStructure = metadata.getFeatureSerializationStructure(feature);
 		// assume standard EMF serialization if not specified explicitly
 		assertSame(XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__0100__FEATURE_ELEMENT, featureSerializationStructure);
 	}
 
 	@Test
 	public void testGetFeatureSerializationStructure_EmptyAnnotationMany() {
 		XMLPersistenceMappingExtendedMetaData metadata = new XMLPersistenceMappingExtendedMetaDataImpl(identitySerializationStructureConfiguration);
 
 		EStructuralFeature feature = NodesPackage.eINSTANCE.getNode_EReference_EmptyAnnotationMany();
 		int featureSerializationStructure = metadata.getFeatureSerializationStructure(feature);
 		assertSame(XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__1001__FEATURE_WRAPPER_ELEMENT__CLASSIFIER_ELEMENT,
 				featureSerializationStructure);
 	}
 
 	@Test
 	public void testGetFeatureSerializationStructure_NoAnnotationMany() {
 		XMLPersistenceMappingExtendedMetaData metadata = new XMLPersistenceMappingExtendedMetaDataImpl(identitySerializationStructureConfiguration);
 
 		EStructuralFeature feature = NodesPackage.eINSTANCE.getNode_EReference_NoAnnotationMany();
 		int featureSerializationStructure = metadata.getFeatureSerializationStructure(feature);
 		// use default: standard emf serialization structure
 		assertSame(XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__0100__FEATURE_ELEMENT, featureSerializationStructure);
 	}
 
 	@Test
 	public void testGetTypeByXMLName_Namespace_knownType() {
 		XMLPersistenceMappingExtendedMetaData metadata = new XMLPersistenceMappingExtendedMetaDataImpl();
 
 		EClassifier eClassifier = metadata.getTypeByXMLName(NodesPackage.eNS_URI, "NODE");
 		assertSame(NodesPackage.eINSTANCE.getNode(), eClassifier);
 	}
 
 	@Test
 	public void testGetTypeByXMLName_EPackage_knownType() {
 		XMLPersistenceMappingExtendedMetaData metadata = new XMLPersistenceMappingExtendedMetaDataImpl();
 
 		EClassifier eClassifier = metadata.getTypeByXMLName(NodesPackage.eINSTANCE, "NODE");
 		assertSame(NodesPackage.eINSTANCE.getNode(), eClassifier);
 	}
 
 	@Test
 	public void testGetTypeByXMLName_EPackage_unKnownType() {
 		XMLPersistenceMappingExtendedMetaData metadata = new XMLPersistenceMappingExtendedMetaDataImpl();
 
 		EClassifier eClassifier = metadata.getTypeByXMLName(NodesPackage.eINSTANCE, "Node");
 		assertNull(eClassifier);
 	}
 
 	@Test
 	public void testGetFeatureByXMLElementName_EReference_Contained0000Many() {
 		XMLPersistenceMappingExtendedMetaData metadata = new XMLPersistenceMappingExtendedMetaDataImpl(identitySerializationStructureConfiguration);
 
 		EClass nodeEClass = NodesPackage.eINSTANCE.getNode();
 		EStructuralFeature feature = metadata.getFeatureByXMLElementName(nodeEClass, NodesPackage.eNS_URI, "ATTRIBUTE-FROM-NESTED-CLASS");
 		assertSame(NodesPackage.eINSTANCE.getNode_EReference_Contained0000Many(), feature);
 	}
 
 	@Test
 	public void testGetFeatureByXMLElementName_EReference_Contained0001Many() {
 		XMLPersistenceMappingExtendedMetaData metadata = new XMLPersistenceMappingExtendedMetaDataImpl(identitySerializationStructureConfiguration);
 
 		EClass nodeEClass = NodesPackage.eINSTANCE.getNode();
 		EStructuralFeature feature = metadata.getFeatureByXMLElementName(nodeEClass, NodesPackage.eNS_URI, "NODE");
 		assertSame(NodesPackage.eINSTANCE.getNode_EReference_Contained0001Many(), feature);
 	}
 
 	@Test
 	public void testGetFeatureByXMLElementName_EReference_Contained0010Many() {
 		XMLPersistenceMappingExtendedMetaData metadata = new XMLPersistenceMappingExtendedMetaDataImpl(identitySerializationStructureConfiguration);
 
 		EClass nodeEClass = NodesPackage.eINSTANCE.getNode();
 		EStructuralFeature feature = metadata.getFeatureByXMLElementName(nodeEClass, NodesPackage.eNS_URI, "NODES");
 		assertSame(NodesPackage.eINSTANCE.getNode_EReference_Contained0010Many(), feature);
 	}
 
 	@Test
 	public void testGetFeatureByXMLElementName_EReference_Contained0011Many() {
 		XMLPersistenceMappingExtendedMetaData metadata = new XMLPersistenceMappingExtendedMetaDataImpl(identitySerializationStructureConfiguration);
 
 		EClass nodeEClass = NodesPackage.eINSTANCE.getNode();
 		EStructuralFeature feature = metadata.getFeatureByXMLElementName(nodeEClass, NodesPackage.eNS_URI, "NODES");
 		// we assume 0010 here since there is no look ahead implemented to avoid ambiguity
 		assertSame(NodesPackage.eINSTANCE.getNode_EReference_Contained0010Many(), feature);
 	}
 
 	@Test
 	public void testGetFeatureByXMLElementName_EReference_Contained0100Many() {
 		XMLPersistenceMappingExtendedMetaData metadata = new XMLPersistenceMappingExtendedMetaDataImpl(identitySerializationStructureConfiguration);
 
 		EClass nodeEClass = NodesPackage.eINSTANCE.getNode();
 		EStructuralFeature feature = metadata.getFeatureByXMLElementName(nodeEClass, NodesPackage.eNS_URI, "FEATURE-WITH-SERIALIZATION-0100-MULTI");
 		assertSame(NodesPackage.eINSTANCE.getNode_EReference_Contained0100Many(), feature);
 	}
 
 	@Test
 	public void testGetFeatureByXMLElementName_EReference_Contained0101Many() {
 		XMLPersistenceMappingExtendedMetaData metadata = new XMLPersistenceMappingExtendedMetaDataImpl(identitySerializationStructureConfiguration);
 
 		EClass nodeEClass = NodesPackage.eINSTANCE.getNode();
 		EStructuralFeature feature = metadata.getFeatureByXMLElementName(nodeEClass, NodesPackage.eNS_URI, "FEATURE-WITH-SERIALIZATION-0101-MULTI");
 		assertSame(NodesPackage.eINSTANCE.getNode_EReference_Contained0101Many(), feature);
 	}
 
 	@Test
 	public void testGetFeatureByXMLElementName_EReference_Contained0110Many() {
 		XMLPersistenceMappingExtendedMetaData metadata = new XMLPersistenceMappingExtendedMetaDataImpl(identitySerializationStructureConfiguration);
 
 		EClass nodeEClass = NodesPackage.eINSTANCE.getNode();
 		EStructuralFeature feature = metadata.getFeatureByXMLElementName(nodeEClass, NodesPackage.eNS_URI, "FEATURE-WITH-SERIALIZATION-0110-MULTI");
 		assertSame(NodesPackage.eINSTANCE.getNode_EReference_Contained0110Many(), feature);
 	}
 
 	@Test
 	public void testGetFeatureByXMLElementName_EReference_Contained0111Many() {
 		XMLPersistenceMappingExtendedMetaData metadata = new XMLPersistenceMappingExtendedMetaDataImpl(identitySerializationStructureConfiguration);
 
 		EClass nodeEClass = NodesPackage.eINSTANCE.getNode();
 		EStructuralFeature feature = metadata.getFeatureByXMLElementName(nodeEClass, NodesPackage.eNS_URI, "FEATURE-WITH-SERIALIZATION-0111-MULTI");
 		assertSame(NodesPackage.eINSTANCE.getNode_EReference_Contained0111Many(), feature);
 	}
 
 	@Test
 	public void testGetFeatureByXMLElementName_EReference_Contained1000Many() {
 		XMLPersistenceMappingExtendedMetaData metadata = new XMLPersistenceMappingExtendedMetaDataImpl(identitySerializationStructureConfiguration);
 
 		EClass nodeEClass = NodesPackage.eINSTANCE.getNode();
 		EStructuralFeature feature = metadata.getFeatureByXMLElementName(nodeEClass, NodesPackage.eNS_URI, "FEATURE-WITH-SERIALIZATION-1000-MULTIS");
 		assertSame(NodesPackage.eINSTANCE.getNode_EReference_Contained1000Many(), feature);
 	}
 
 	@Test
 	public void testGetFeatureByXMLElementName_EReference_Contained1001Many() {
 		XMLPersistenceMappingExtendedMetaData metadata = new XMLPersistenceMappingExtendedMetaDataImpl(identitySerializationStructureConfiguration);
 
 		EClass nodeEClass = NodesPackage.eINSTANCE.getNode();
 		EStructuralFeature feature = metadata.getFeatureByXMLElementName(nodeEClass, NodesPackage.eNS_URI, "FEATURE-WITH-SERIALIZATION-1001-MULTIS");
 		assertSame(NodesPackage.eINSTANCE.getNode_EReference_Contained1001Many(), feature);
 	}
 
 	@Test
 	public void testGetFeatureByXMLElementName_EReference_Contained1010Many() {
 		XMLPersistenceMappingExtendedMetaData metadata = new XMLPersistenceMappingExtendedMetaDataImpl(identitySerializationStructureConfiguration);
 
 		EClass nodeEClass = NodesPackage.eINSTANCE.getNode();
 		EStructuralFeature feature = metadata.getFeatureByXMLElementName(nodeEClass, NodesPackage.eNS_URI, "FEATURE-WITH-SERIALIZATION-1010-MULTIS");
 		assertSame(NodesPackage.eINSTANCE.getNode_EReference_Contained1010Many(), feature);
 	}
 
 	@Test
 	public void testGetFeatureByXMLElementName_EReference_Contained1011Many() {
 		XMLPersistenceMappingExtendedMetaData metadata = new XMLPersistenceMappingExtendedMetaDataImpl(identitySerializationStructureConfiguration);
 
 		EClass nodeEClass = NodesPackage.eINSTANCE.getNode();
 		EStructuralFeature feature = metadata.getFeatureByXMLElementName(nodeEClass, NodesPackage.eNS_URI, "FEATURE-WITH-SERIALIZATION-1011-MULTIS");
 		assertSame(NodesPackage.eINSTANCE.getNode_EReference_Contained1011Many(), feature);
 	}
 
 	@Test
 	public void testGetFeatureByXMLElementName_EReference_Contained1100Many() {
 		XMLPersistenceMappingExtendedMetaData metadata = new XMLPersistenceMappingExtendedMetaDataImpl(identitySerializationStructureConfiguration);
 
 		EClass nodeEClass = NodesPackage.eINSTANCE.getNode();
 		EStructuralFeature feature = metadata.getFeatureByXMLElementName(nodeEClass, NodesPackage.eNS_URI, "FEATURE-WITH-SERIALIZATION-1100-MULTIS");
 		assertSame(NodesPackage.eINSTANCE.getNode_EReference_Contained1100Many(), feature);
 	}
 
 	@Test
 	public void testGetFeatureByXMLElementName_EReference_Contained1101Many() {
 		XMLPersistenceMappingExtendedMetaData metadata = new XMLPersistenceMappingExtendedMetaDataImpl(identitySerializationStructureConfiguration);
 
 		EClass nodeEClass = NodesPackage.eINSTANCE.getNode();
 		EStructuralFeature feature = metadata.getFeatureByXMLElementName(nodeEClass, NodesPackage.eNS_URI, "FEATURE-WITH-SERIALIZATION-1101-MULTIS");
 		assertSame(NodesPackage.eINSTANCE.getNode_EReference_Contained1101Many(), feature);
 	}
 
 	@Test
 	public void testGetFeatureByXMLElementName_EReference_Contained1110Many() {
 		XMLPersistenceMappingExtendedMetaData metadata = new XMLPersistenceMappingExtendedMetaDataImpl(identitySerializationStructureConfiguration);
 
 		EClass nodeEClass = NodesPackage.eINSTANCE.getNode();
 		EStructuralFeature feature = metadata.getFeatureByXMLElementName(nodeEClass, NodesPackage.eNS_URI, "FEATURE-WITH-SERIALIZATION-1110-MULTIS");
 		assertSame(NodesPackage.eINSTANCE.getNode_EReference_Contained1110Many(), feature);
 	}
 
 	@Test
 	public void testGetFeatureByXMLElementName_EReference_Contained1111Many() {
 		XMLPersistenceMappingExtendedMetaData metadata = new XMLPersistenceMappingExtendedMetaDataImpl(identitySerializationStructureConfiguration);
 
 		EClass nodeEClass = NodesPackage.eINSTANCE.getNode();
 		EStructuralFeature feature = metadata.getFeatureByXMLElementName(nodeEClass, NodesPackage.eNS_URI, "FEATURE-WITH-SERIALIZATION-1111-MULTIS");
 		assertSame(NodesPackage.eINSTANCE.getNode_EReference_Contained1111Many(), feature);
 	}
 
 	@Test
 	public void testGetFeatureByXMLElementName_EReference_NoAnnotationMany() {
 		XMLPersistenceMappingExtendedMetaData metadata = new XMLPersistenceMappingExtendedMetaDataImpl(identitySerializationStructureConfiguration);
 
 		EClass nodeEClass = NodesPackage.eINSTANCE.getNode();
 		EStructuralFeature feature = metadata.getFeatureByXMLElementName(nodeEClass, NodesPackage.eNS_URI, "eReference_NoAnnotationMany");
 		assertSame(NodesPackage.eINSTANCE.getNode_EReference_NoAnnotationMany(), feature);
 	}
 
 	@Test
 	public void testGetFeatureByXMLElementName_EAttribute_Attribute0010Many() {
 		XMLPersistenceMappingExtendedMetaData metadata = new XMLPersistenceMappingExtendedMetaDataImpl(identitySerializationStructureConfiguration);
 
 		EClass nodeEClass = NodesPackage.eINSTANCE.getNode();
 		EStructuralFeature feature = metadata.getFeatureByXMLElementName(nodeEClass, NodesPackage.eNS_URI, "EStrings");
 		assertSame(NodesPackage.eINSTANCE.getNode_EAttribute_Attribute0010Many(), feature);
 	}
 
 	@Test
 	public void testGetFeatureByXMLElementName_EAttribute_Attribute0011Many() {
 		XMLPersistenceMappingExtendedMetaData metadata = new XMLPersistenceMappingExtendedMetaDataImpl(identitySerializationStructureConfiguration);
 
 		EClass nodeEClass = NodesPackage.eINSTANCE.getNode();
 		EStructuralFeature feature = metadata.getFeatureByXMLElementName(nodeEClass, NodesPackage.eNS_URI, "EStrings");
 		// we assume 0010 here since there is no look ahead implemented to avoid ambiguity
 		assertSame(NodesPackage.eINSTANCE.getNode_EAttribute_Attribute0010Many(), feature);
 	}
 
 	@Test
	public void testGetFeatureByXMLElementName_EAttribute_Attribute0010Many2() {
 		XMLPersistenceMappingExtendedMetaData metadata = new XMLPersistenceMappingExtendedMetaDataImpl(identitySerializationStructureConfiguration);
 
 		EClass nodeEClass = NodesPackage.eINSTANCE.getNode();
 		EStructuralFeature feature = metadata.getFeatureByXMLElementName(nodeEClass, EcorePackage.eNS_URI, "EStrings");
		assertSame(NodesPackage.eINSTANCE.getNode_EAttribute_Attribute0010Many(), feature);
 	}
 
 	@Test
 	public void testGetFeatureByXMLElementName_EAttribute_Attribute0100Many() {
 		XMLPersistenceMappingExtendedMetaData metadata = new XMLPersistenceMappingExtendedMetaDataImpl(identitySerializationStructureConfiguration);
 
 		EClass nodeEClass = NodesPackage.eINSTANCE.getNode();
 		EStructuralFeature feature = metadata.getFeatureByXMLElementName(nodeEClass, NodesPackage.eNS_URI, "EATTRIBUTE-ATTRIBUTE-0100-MANY");
 		assertSame(NodesPackage.eINSTANCE.getNode_EAttribute_Attribute0100Many(), feature);
 	}
 
 	@Test
 	public void testGetFeatureByXMLElementName_EAttribute_Attribute0101Many() {
 		XMLPersistenceMappingExtendedMetaData metadata = new XMLPersistenceMappingExtendedMetaDataImpl(identitySerializationStructureConfiguration);
 
 		EClass nodeEClass = NodesPackage.eINSTANCE.getNode();
 		EStructuralFeature feature = metadata.getFeatureByXMLElementName(nodeEClass, NodesPackage.eNS_URI, "EATTRIBUTE-ATTRIBUTE-0101-MANY");
 		assertSame(NodesPackage.eINSTANCE.getNode_EAttribute_Attribute0101Many(), feature);
 	}
 
 	@Test
 	public void testGetFeatureByXMLElementName_EAttribute_Attribute0110Many() {
 		XMLPersistenceMappingExtendedMetaData metadata = new XMLPersistenceMappingExtendedMetaDataImpl(identitySerializationStructureConfiguration);
 
 		EClass nodeEClass = NodesPackage.eINSTANCE.getNode();
 		EStructuralFeature feature = metadata.getFeatureByXMLElementName(nodeEClass, NodesPackage.eNS_URI, "EATTRIBUTE-ATTRIBUTE-0110-MANY");
 		assertSame(NodesPackage.eINSTANCE.getNode_EAttribute_Attribute0110Many(), feature);
 	}
 
 	@Test
 	public void testGetFeatureByXMLElementName_EAttribute_Attribute0111Many() {
 		XMLPersistenceMappingExtendedMetaData metadata = new XMLPersistenceMappingExtendedMetaDataImpl(identitySerializationStructureConfiguration);
 
 		EClass nodeEClass = NodesPackage.eINSTANCE.getNode();
 		EStructuralFeature feature = metadata.getFeatureByXMLElementName(nodeEClass, NodesPackage.eNS_URI, "EATTRIBUTE-ATTRIBUTE-0111-MANY");
 		assertSame(NodesPackage.eINSTANCE.getNode_EAttribute_Attribute0111Many(), feature);
 	}
 
 	@Test
 	public void testGetFeatureByXMLElementName_EAttribute_Attribute1000Many() {
 		XMLPersistenceMappingExtendedMetaData metadata = new XMLPersistenceMappingExtendedMetaDataImpl(identitySerializationStructureConfiguration);
 
 		EClass nodeEClass = NodesPackage.eINSTANCE.getNode();
 		EStructuralFeature feature = metadata.getFeatureByXMLElementName(nodeEClass, NodesPackage.eNS_URI, "EATTRIBUTE-ATTRIBUTE-1000-MANIES");
 		assertSame(NodesPackage.eINSTANCE.getNode_EAttribute_Attribute1000Many(), feature);
 	}
 
 	@Test
 	public void testGetFeatureByXMLElementName_EAttribute_Attribute1001Many() {
 		XMLPersistenceMappingExtendedMetaData metadata = new XMLPersistenceMappingExtendedMetaDataImpl(identitySerializationStructureConfiguration);
 
 		EClass nodeEClass = NodesPackage.eINSTANCE.getNode();
 		EStructuralFeature feature = metadata.getFeatureByXMLElementName(nodeEClass, NodesPackage.eNS_URI, "EATTRIBUTE-ATTRIBUTE-1001-MANIES");
 		assertSame(NodesPackage.eINSTANCE.getNode_EAttribute_Attribute1001Many(), feature);
 	}
 
 	@Test
 	public void testGetFeatureByXMLElementName_EAttribute_Attribute1010Many() {
 		XMLPersistenceMappingExtendedMetaData metadata = new XMLPersistenceMappingExtendedMetaDataImpl(identitySerializationStructureConfiguration);
 
 		EClass nodeEClass = NodesPackage.eINSTANCE.getNode();
 		EStructuralFeature feature = metadata.getFeatureByXMLElementName(nodeEClass, NodesPackage.eNS_URI, "EATTRIBUTE-ATTRIBUTE-1010-MANIES");
 		assertSame(NodesPackage.eINSTANCE.getNode_EAttribute_Attribute1010Many(), feature);
 	}
 
 	@Test
 	public void testGetFeatureByXMLElementName_EAttribute_Attribute1011Many() {
 		XMLPersistenceMappingExtendedMetaData metadata = new XMLPersistenceMappingExtendedMetaDataImpl(identitySerializationStructureConfiguration);
 
 		EClass nodeEClass = NodesPackage.eINSTANCE.getNode();
 		EStructuralFeature feature = metadata.getFeatureByXMLElementName(nodeEClass, NodesPackage.eNS_URI, "EATTRIBUTE-ATTRIBUTE-1011-MANIES");
 		assertSame(NodesPackage.eINSTANCE.getNode_EAttribute_Attribute1011Many(), feature);
 	}
 
 	@Test
 	public void testGetFeatureByXMLElementName_EAttribute_Attribute1100Many() {
 		XMLPersistenceMappingExtendedMetaData metadata = new XMLPersistenceMappingExtendedMetaDataImpl(identitySerializationStructureConfiguration);
 
 		EClass nodeEClass = NodesPackage.eINSTANCE.getNode();
 		EStructuralFeature feature = metadata.getFeatureByXMLElementName(nodeEClass, NodesPackage.eNS_URI, "EATTRIBUTE-ATTRIBUTE-1100-MANIES");
 		assertSame(NodesPackage.eINSTANCE.getNode_EAttribute_Attribute1100Many(), feature);
 	}
 
 	@Test
 	public void testGetFeatureByXMLElementName_EAttribute_Attribute1101Many() {
 		XMLPersistenceMappingExtendedMetaData metadata = new XMLPersistenceMappingExtendedMetaDataImpl(identitySerializationStructureConfiguration);
 
 		EClass nodeEClass = NodesPackage.eINSTANCE.getNode();
 		EStructuralFeature feature = metadata.getFeatureByXMLElementName(nodeEClass, NodesPackage.eNS_URI, "EATTRIBUTE-ATTRIBUTE-1101-MANIES");
 		assertSame(NodesPackage.eINSTANCE.getNode_EAttribute_Attribute1101Many(), feature);
 	}
 
 	@Test
 	public void testGetFeatureByXMLElementName_EAttribute_Attribute1110Many() {
 		XMLPersistenceMappingExtendedMetaData metadata = new XMLPersistenceMappingExtendedMetaDataImpl(identitySerializationStructureConfiguration);
 
 		EClass nodeEClass = NodesPackage.eINSTANCE.getNode();
 		EStructuralFeature feature = metadata.getFeatureByXMLElementName(nodeEClass, NodesPackage.eNS_URI, "EATTRIBUTE-ATTRIBUTE-1110-MANIES");
 		assertSame(NodesPackage.eINSTANCE.getNode_EAttribute_Attribute1110Many(), feature);
 	}
 
 	@Test
 	public void testGetFeatureByXMLElementName_EAttribute_Attribute1111Many() {
 		XMLPersistenceMappingExtendedMetaData metadata = new XMLPersistenceMappingExtendedMetaDataImpl(identitySerializationStructureConfiguration);
 
 		EClass nodeEClass = NodesPackage.eINSTANCE.getNode();
 		EStructuralFeature feature = metadata.getFeatureByXMLElementName(nodeEClass, NodesPackage.eNS_URI, "EATTRIBUTE-ATTRIBUTE-1111-MANIES");
 		assertSame(NodesPackage.eINSTANCE.getNode_EAttribute_Attribute1111Many(), feature);
 	}
 
 	/*
 	 * @Test public void testGetFeatureByXMLElementName_EAttribute_NoAnnotationMany() {
 	 * XMLPersistenceMappingExtendedMetaData metadata = new
 	 * XMLPersistenceMappingExtendedMetaDataImpl(identitySerializationStructureConfiguration); EClass nodeEClass =
 	 * NodesPackage.eINSTANCE.getNode(); EStructuralFeature feature = metadata.getFeatureByXMLElementName(nodeEClass,
 	 * NodesPackage.eNS_URI, "eReference_NoAnnotationManys");
 	 * assertSame(NodesPackage.eINSTANCE.getNode_EReference_NoAnnotationMany(), feature); }
 	 */
 	private String toBinaryString(int serializationStructure) {
 		StringBuffer buffer = new StringBuffer();
 
 		buffer.append(getBinaryString(serializationStructure, 8));
 		buffer.append(getBinaryString(serializationStructure, 4));
 		buffer.append(getBinaryString(serializationStructure, 2));
 		buffer.append(getBinaryString(serializationStructure, 1));
 
 		return buffer.toString();
 	}
 
 	private String getBinaryString(int serializationStructure, int mask) {
 		if (mask == (serializationStructure & mask)) {
 			return "1";
 		} else {
 			return "0";
 		}
 	}
 
 }
