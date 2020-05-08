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
 package org.eclipse.rmf.tests.serialization.load;
 
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.rmf.serialization.XMLPersistenceMappingResourceFactoryImpl;
 import org.eclipse.rmf.tests.serialization.model.nodes.Node;
 import org.eclipse.rmf.tests.serialization.model.nodes.NodesPackage;
 import org.eclipse.rmf.tests.serialization.model.nodes.SubNode;
 import org.eclipse.rmf.tests.serialization.util.AbstractTestCase;
 import org.junit.Before;
 import org.junit.Test;
 
 // Junit 3.8 test 
 @SuppressWarnings("nls")
 public class LoadTests extends AbstractTestCase {
 	static final String INPUT_PATH = "org.eclipse.rmf.tests.serialization.load/";
 
 	@Override
 	@Before
 	public void setUp() throws Exception {
 		super.setUp();
 		EPackage.Registry.INSTANCE.put(NodesPackage.eNS_URI, NodesPackage.eINSTANCE);
 	}
 
 	@Test
 	public void testFeatureContainmentReferenceSerialization1001_Single() {
 		String inputFileName = INPUT_PATH + "FeatureContainmentReferenceSerialization1001_Single.xml";
 		try {
 			EObject modelRoot = loadInputFile(inputFileName, new XMLPersistenceMappingResourceFactoryImpl(), null);
 			validateModelSingle(modelRoot, NodesPackage.eINSTANCE.getNode_FeatureWithSerialization1001_Single());
 		} catch (Exception ex) {
 			assertTrue(ex.getMessage(), false);
 		}
 	}
 
 	@Test
 	public void testFeatureContainmentRTeferenceSerialization0001_Many() {
 		String inputFileName = INPUT_PATH + "FeatureContainmentReferenceSerialization0001_Many.xml";
 		try {
 			EObject modelRoot = loadInputFile(inputFileName, new XMLPersistenceMappingResourceFactoryImpl(), null);
 			validateModelMulti(modelRoot, NodesPackage.eINSTANCE.getNode_FeatureWithSerialization0001_Multi(),
 					NodesPackage.eINSTANCE.getNode_FeatureWithSerialization0100_Multi());
 		} catch (Exception ex) {
 			assertTrue(ex.getMessage(), false);
 		}
 	}
 
 	@Test
 	public void testFeatureContainmentRTeferenceSerialization0100_Many() {
 		String inputFileName = INPUT_PATH + "FeatureContainmentReferenceSerialization0100_Many.xml";
 		try {
 			EObject modelRoot = loadInputFile(inputFileName, new XMLPersistenceMappingResourceFactoryImpl(), null);
 			validateModelMulti(modelRoot, NodesPackage.eINSTANCE.getNode_FeatureWithSerialization0100_Multi(),
 					NodesPackage.eINSTANCE.getNode_FeatureWithSerialization0100_Multi());
 		} catch (Exception ex) {
 			assertTrue(ex.getMessage(), false);
 		}
 	}
 
 	@Test
 	public void testFeatureContainmentReferenceSerialization0101_Many() {
 		String inputFileName = INPUT_PATH + "FeatureContainmentReferenceSerialization0101_Many.xml";
 		try {
 			EObject modelRoot = loadInputFile(inputFileName, new XMLPersistenceMappingResourceFactoryImpl(), null);
 			validateModelMulti(modelRoot, NodesPackage.eINSTANCE.getNode_FeatureWithSerialization0101_Multi(),
 					NodesPackage.eINSTANCE.getNode_FeatureWithSerialization0100_Multi());
 		} catch (Exception ex) {
 			assertTrue(ex.getMessage(), false);
 		}
 	}
 
 	@Test
 	public void testFeatureContainmentReferenceSerialization1001_Many() {
 		String inputFileName = INPUT_PATH + "FeatureContainmentReferenceSerialization1001_Many.xml";
 		try {
 			EObject modelRoot = loadInputFile(inputFileName, new XMLPersistenceMappingResourceFactoryImpl(), null);
 			validateModelMulti(modelRoot, NodesPackage.eINSTANCE.getNode_FeatureWithSerialization1001_Multi(),
 					NodesPackage.eINSTANCE.getNode_FeatureWithSerialization0100_Multi());
 		} catch (Exception ex) {
 			assertTrue(ex.getMessage(), false);
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	protected void validateModelMulti(EObject modelRoot, EStructuralFeature topFeature, EStructuralFeature subFeature) {
 		assert topFeature.isMany();
 		// check root node
 		assertNotNull(modelRoot);
 		assertSame(NodesPackage.eINSTANCE.getNode(), modelRoot.eClass());
 		Node node = (Node) modelRoot;
 
 		// check intermediate nodes
 		EList<Object> intermediateNodes = (EList<Object>) node.eGet(topFeature);
 		assertSame(4, intermediateNodes.size());
 
 		Object intermediateNodeObject1 = intermediateNodes.get(0);
 		assertTrue(intermediateNodeObject1 instanceof Node);
 		Node intermediateNode1 = (Node) intermediateNodeObject1;
 		assertEquals("intermediateNode1", intermediateNode1.getName());
 
 		Object intermediateNodeObject2 = intermediateNodes.get(1);
 		assertTrue(intermediateNodeObject2 instanceof Node);
 		Node intermediateNode2 = (Node) intermediateNodeObject2;
 		assertEquals("intermediateNode2", intermediateNode2.getName());
 
 		Object intermediateSubNodeObject3 = intermediateNodes.get(2);
 		assertTrue(intermediateSubNodeObject3 instanceof SubNode);
 		Node intermediateSubNode3 = (Node) intermediateSubNodeObject3;
 		assertEquals("intermediateSubNode3", intermediateSubNode3.getName());
 
 		Object intermediateSubNodeObject4 = intermediateNodes.get(3);
 		assertTrue(intermediateSubNodeObject4 instanceof SubNode);
 		Node intermediateSubNode4 = (Node) intermediateSubNodeObject4;
 		assertEquals("intermediateSubNode4", intermediateSubNode4.getName());
 
 		// check leaf nodes
 		EList<Object> leafNodes1 = (EList<Object>) intermediateNode1.eGet(subFeature);
 		assertSame(2, leafNodes1.size());
 
 		Object leafNodeObject11 = leafNodes1.get(0);
 		assertTrue(leafNodeObject11 instanceof Node);
 		Node leafNode11 = (Node) leafNodeObject11;
 		assertEquals("leafNode11", leafNode11.getName());
 		EList<Object> leafNodes11 = (EList<Object>) leafNode11.eGet(subFeature);
 		assertSame(0, leafNodes11.size());
 
 		Object leafNodeObject12 = leafNodes1.get(1);
 		assertTrue(leafNodeObject12 instanceof Node);
 		Node leafNode12 = (Node) leafNodeObject12;
 		assertEquals("leafNode12", leafNode12.getName());
 		EList<Object> leafNodes12 = (EList<Object>) leafNode12.eGet(subFeature);
 		assertSame(0, leafNodes12.size());
 
 		// check leaf nodes
 		EList<Object> leafNodes2 = (EList<Object>) intermediateNode2.eGet(subFeature);
 		assertSame(2, leafNodes2.size());
 
 		Object leafNodeObject21 = leafNodes2.get(0);
 		assertTrue(leafNodeObject21 instanceof Node);
 		Node leafNode21 = (Node) leafNodeObject21;
 		assertEquals("leafNode21", leafNode21.getName());
 		EList<Object> leafNodes21 = (EList<Object>) leafNode21.eGet(subFeature);
 		assertSame(0, leafNodes21.size());
 
 		Object leafNodeObject22 = leafNodes2.get(1);
 		assertTrue(leafNodeObject22 instanceof Node);
 		Node leafNode22 = (Node) leafNodeObject22;
 		assertEquals("leafNode22", leafNode22.getName());
 		EList<Object> leafNodes22 = (EList<Object>) leafNode22.eGet(subFeature);
 		assertSame(0, leafNodes22.size());
 
 		// check leaf nodes
 		EList<Object> leafNodes3 = (EList<Object>) intermediateSubNode3.eGet(subFeature);
 		assertSame(2, leafNodes3.size());
 
 		Object leafNodeObject31 = leafNodes3.get(0);
 		assertTrue(leafNodeObject31 instanceof Node);
 		Node leafNode31 = (Node) leafNodeObject31;
 		assertEquals("leafNode31", leafNode31.getName());
 		EList<Object> leafNodes31 = (EList<Object>) leafNode31.eGet(subFeature);
 		assertSame(0, leafNodes31.size());
 
 		Object leafNodeObject32 = leafNodes3.get(1);
 		assertTrue(leafNodeObject32 instanceof Node);
 		Node leafNode32 = (Node) leafNodeObject32;
 		assertEquals("leafNode32", leafNode32.getName());
 		EList<Object> leafNodes32 = (EList<Object>) leafNode32.eGet(subFeature);
 		assertSame(0, leafNodes32.size());
 
 		// check leaf nodes
 		EList<Object> leafNodes4 = (EList<Object>) intermediateSubNode4.eGet(subFeature);
 		assertSame(2, leafNodes4.size());
 
 		Object leafNodeObject41 = leafNodes4.get(0);
 		assertTrue(leafNodeObject41 instanceof Node);
 		Node leafNode41 = (Node) leafNodeObject41;
 		assertEquals("leafNode41", leafNode41.getName());
 		EList<Object> leafNodes41 = (EList<Object>) leafNode41.eGet(subFeature);
 		assertSame(0, leafNodes41.size());
 
 		Object leafNodeObject42 = leafNodes4.get(1);
 		assertTrue(leafNodeObject42 instanceof Node);
 		Node leafNode42 = (Node) leafNodeObject42;
 		assertEquals("leafNode42", leafNode42.getName());
 		EList<Object> leafNodes42 = (EList<Object>) leafNode42.eGet(subFeature);
 		assertSame(0, leafNodes42.size());
 
 	}
 
 	protected void validateModelSingle(EObject modelRoot, EStructuralFeature feature) {
 		// check root node
 		assertNotNull(modelRoot);
 		assertSame(NodesPackage.eINSTANCE.getNode(), modelRoot.eClass());
 		Node node = (Node) modelRoot;
 
 		// check intermediate node
 		Object intermediateNodeObject1 = node.eGet(feature);
 		assertNotNull(intermediateNodeObject1);
 		assertTrue(intermediateNodeObject1 instanceof Node);
 		Node intermediateNode1 = (Node) intermediateNodeObject1;
 		assertEquals("intermediateNode1", intermediateNode1.getName());
 
 		// check leaf node
 		Object leafNodeObject11 = intermediateNode1.eGet(feature);
 		assertNotNull(leafNodeObject11);
 		assertTrue(leafNodeObject11 instanceof Node);
 		Node leafNode11 = (Node) leafNodeObject11;
 		assertEquals("leafNode11", leafNode11.getName());
 	}
 
 }
