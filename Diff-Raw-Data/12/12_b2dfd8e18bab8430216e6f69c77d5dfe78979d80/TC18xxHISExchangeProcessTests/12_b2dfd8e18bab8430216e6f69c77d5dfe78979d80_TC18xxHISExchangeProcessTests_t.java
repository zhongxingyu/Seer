 /**
  * Copyright (c) 2012, 2013 itemis AG.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Mark Broerkens - initial API and implementation
  *
  */
 package org.eclipse.rmf.tests.reqif10.serialization.uc003.tc18xx;
 
 import static junit.framework.Assert.assertEquals;
 import static junit.framework.Assert.assertNotNull;
 import static junit.framework.Assert.assertNull;
 import static junit.framework.Assert.assertTrue;
 
 import java.io.IOException;
 
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EcorePackage;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.ecore.util.FeatureMap;
 import org.eclipse.emf.ecore.util.FeatureMap.Entry;
 import org.eclipse.rmf.reqif10.AttributeValueXHTML;
 import org.eclipse.rmf.reqif10.ReqIF;
 import org.eclipse.rmf.reqif10.SpecElementWithAttributes;
 import org.eclipse.rmf.reqif10.SpecHierarchy;
 import org.eclipse.rmf.reqif10.SpecObject;
 import org.eclipse.rmf.reqif10.Specification;
 import org.eclipse.rmf.reqif10.XhtmlContent;
 import org.eclipse.rmf.reqif10.xhtml.XhtmlPType;
 import org.eclipse.rmf.tests.reqif10.serialization.util.AbstractTestCase;
 import org.eclipse.rmf.tests.reqif10.serialization.util.CommonSystemAttributes;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 @SuppressWarnings("nls")
 public class TC18xxHISExchangeProcessTests extends AbstractTestCase implements CommonSystemAttributes {
 	static final String TC1800_FILENAME = getWorkingFileName(getReferenceDataFileName("TC1800", false));
 	static final String TC1801_FILENAME = getWorkingFileName(getReferenceDataFileName("TC1801", false));
 	static final String TC1802_FILENAME = getWorkingFileName(getReferenceDataFileName("TC1802", false));
 	static final String TC1803_FILENAME = getWorkingFileName(getReferenceDataFileName("TC1803", false));
 
 	static ReqIF tc1800ReqIF = null;
 	static ReqIF tc1801ReqIF = null;
 	static ReqIF tc1802ReqIF = null;
 	static ReqIF tc1803ReqIF = null;
 
 	@BeforeClass
 	public static void setupOnce() throws Exception {
 		AbstractTestCase.setupOnce();
 		// ___
 		tc1800ReqIF = new TC1800HISExchangeProcessModelBuilder().getReqIF();
 		doSaveReqIFFile(tc1800ReqIF, TC1800_FILENAME);
		tc1800ReqIF = loadReqIFFile(TC1800_FILENAME); // Ensure all references to the old filename are gone
 
 		tc1801ReqIF = new TC1801HISExchangeProcessModelBuilder(loadReqIFFile(TC1800_FILENAME)).getReqIF();
 		doSaveReqIFFile(tc1801ReqIF, TC1801_FILENAME);
 		tc1801ReqIF = loadReqIFFile(TC1801_FILENAME); // Ensure all references to the old filename are gone
 
 		tc1802ReqIF = new TC1802HISExchangeProcessModelBuilder(loadReqIFFile(TC1801_FILENAME)).getReqIF();
 		doSaveReqIFFile(tc1802ReqIF, TC1802_FILENAME);
 		tc1802ReqIF = loadReqIFFile(TC1802_FILENAME); // Ensure all references to the old filename are gone
 
 		tc1803ReqIF = new TC1803HISExchangeProcessModelBuilder(loadReqIFFile(TC1802_FILENAME)).getReqIF();
 		doSaveReqIFFile(tc1803ReqIF, TC1803_FILENAME);
 		tc1803ReqIF = loadReqIFFile(TC1803_FILENAME); // Ensure all references to the old filename are gone
 	}
 
 	@Test
 	public void testSchemaCompliance() throws Exception {
 		validateAgainstSchema(TC1800_FILENAME);
 		validateAgainstSchema(TC1801_FILENAME);
 		validateAgainstSchema(TC1802_FILENAME);
 		validateAgainstSchema(TC1803_FILENAME);
 	}
 
 	/**
 	 * Looks for the given {@link SpecObject} by {@link CommonSystemAttributes#REQIF_NAME}. Returns null if none is
 	 * found, and throws an {@link IllegalStateException} when more than one is found.
 	 */
 	SpecObject getSpecObjectByName(ReqIF reqif, String name) {
 		return (SpecObject) getSpecElementByName(reqif.getCoreContent().getSpecObjects(), name);
 	}
 
 	/**
 	 * Looks for the given {@link Specification} by {@link CommonSystemAttributes#REQIF_NAME}. Returns null if none is
 	 * found, and throws an {@link IllegalStateException} when more than one is found.
 	 */
 	Specification getSpecificationByName(ReqIF reqif, String name) {
 		return (Specification) getSpecElementByName(reqif.getCoreContent().getSpecifications(), name);
 	}
 
 	private SpecElementWithAttributes getSpecElementByName(EList<? extends SpecElementWithAttributes> list, String name) {
 		SpecElementWithAttributes target = null;
 		for (SpecElementWithAttributes specObject : list) {
 			AttributeValueXHTML value = (AttributeValueXHTML) ReqIFUtil.getAttributeValueForLabel(specObject, REQIF_NAME);
 			XhtmlContent content = value.getTheValue();
 			XhtmlPType p = (XhtmlPType) content.getXhtml();
 
 			EAttribute mixedAttribute = null;
 			for (EAttribute eAttribute : p.eClass().getEAllAttributes()) {
 				if ("mixed".equals(eAttribute.getName()) && EcorePackage.eINSTANCE.getEFeatureMapEntry() == eAttribute.getEAttributeType()) {
 					mixedAttribute = eAttribute;
 					break;
 				}
 			}
 			FeatureMap featureMap = (FeatureMap) p.eGet(mixedAttribute);
 			Entry entry = featureMap.get(0);
 
 			if (name.equals(entry.getValue())) {
 				if (target != null) {
 					throw new IllegalStateException("More than one element with name " + name);
 				}
 				target = specObject;
 			}
 		}
 		return target;
 	}
 
 	/**
 	 * Returns true if the given {@link SpecObject} resides in the Spec with the given name.
 	 */
 	SpecHierarchy findInSpec(String name, SpecObject specObject) {
 		Specification spec = getSpecificationByName(ReqIFUtil.getReqIF(specObject), name);
 		if (spec == null) {
 			throw new NullPointerException("Spec does not exist: " + name);
 		}
 
 		for (SpecHierarchy specHierarchy : spec.getChildren()) {
 			SpecObject so = specHierarchy.getObject();
 			if (EcoreUtil.equals(so, specObject)) {
 				return specHierarchy;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Returns the object with the given name from all four files as an array. Elements may be null.
 	 */
 	SpecObject[] getObjectsByName(String name) {
 		return new SpecObject[] { getSpecObjectByName(tc1800ReqIF, name), getSpecObjectByName(tc1801ReqIF, name),
 				getSpecObjectByName(tc1802ReqIF, name), getSpecObjectByName(tc1803ReqIF, name) };
 	}
 
 	@Test
 	public void testObj01() throws Exception {
 		SpecObject[] so = getObjectsByName("Obj-01");
 
 		for (int i = 0; i < so.length; i++) {
 			assertNotNull(so[i]);
 			for (int j = i + 1; j < so.length; j++) {
 				assertTrue(EcoreUtil.equals(so[i], so[j]));
 			}
 		}
 	}
 
 	@Test
 	public void testObj03() throws Exception {
 		SpecObject[] so = getObjectsByName("Obj-03");
 
 		assertTrue(so[0].getLastChange().getTimeInMillis() < so[1].getLastChange().getTimeInMillis());
 		assertTrue(so[1].getLastChange().getTimeInMillis() < so[2].getLastChange().getTimeInMillis());
 		assertTrue(so[2].getLastChange().getTimeInMillis() < so[3].getLastChange().getTimeInMillis());
 
 		// Check values
 		assertEquals("O3.A1 initial", ReqIFUtil.getTheValue(ReqIFUtil.getAttributeValueForLabel(so[0], "A1")));
 		assertEquals("O3.A2 initial", ReqIFUtil.getTheValue(ReqIFUtil.getAttributeValueForLabel(so[0], "A2")));
 
 		assertEquals("O3.A1 initial", ReqIFUtil.getTheValue(ReqIFUtil.getAttributeValueForLabel(so[1], "A1")));
 		assertEquals("O3.A2 once", ReqIFUtil.getTheValue(ReqIFUtil.getAttributeValueForLabel(so[1], "A2")));
 
 		assertEquals("O3.A1 once", ReqIFUtil.getTheValue(ReqIFUtil.getAttributeValueForLabel(so[2], "A1")));
 		assertEquals("O3.A2 once", ReqIFUtil.getTheValue(ReqIFUtil.getAttributeValueForLabel(so[2], "A2")));
 
 		assertEquals("O3.A1 once", ReqIFUtil.getTheValue(ReqIFUtil.getAttributeValueForLabel(so[3], "A1")));
 		assertEquals("O3.A2 twice", ReqIFUtil.getTheValue(ReqIFUtil.getAttributeValueForLabel(so[3], "A2")));
 	}
 
 	@Test
 	public void testObj04() throws Exception {
 		SpecObject[] so = getObjectsByName("Obj-04");
 		assertNull(so[0]);
 
 		for (int i = 1; i < so.length; i++) {
 			assertNotNull(so[i]);
 			for (int j = i + 1; j < so.length; j++) {
 				assertTrue("i=" + i + ", j=" + j, EcoreUtil.equals(so[i], so[j]));
 			}
 		}
 	}
 
 	@Test
 	public void testObj05() throws Exception {
 		SpecObject[] so = getObjectsByName("Obj-05");
 		assertNull(so[0]);
 		assertNull(so[1]);
 		assertNull(so[2]);
 		assertEquals("no change", ReqIFUtil.getTheValue(ReqIFUtil.getAttributeValueForLabel(so[3], "A1")));
 		assertEquals("no change", ReqIFUtil.getTheValue(ReqIFUtil.getAttributeValueForLabel(so[3], "A2")));
 	}
 
 	@Test
 	public void testObj06() throws Exception {
 		SpecObject[] so = getObjectsByName("Obj-06");
 		assertEquals("no change", ReqIFUtil.getTheValue(ReqIFUtil.getAttributeValueForLabel(so[0], "A1")));
 		assertEquals("no change", ReqIFUtil.getTheValue(ReqIFUtil.getAttributeValueForLabel(so[0], "A2")));
 		assertNull(so[1]);
 		assertNull(so[2]);
 		assertNull(so[3]);
 	}
 
 	@Test
 	public void testObj07() throws Exception {
 		SpecObject[] so = getObjectsByName("Obj-07");
 		assertNull(so[3]);
 
 		for (int i = 0; i < 3; i++) {
 			assertNotNull(so[i]);
 			for (int j = i + 1; j < 3; j++) {
 				assertTrue("i=" + i + ", j=" + j, EcoreUtil.equals(so[i], so[j]));
 			}
 		}
 	}
 
 	@Test
 	public void testObj08() throws Exception {
 		SpecObject[] so = getObjectsByName("Obj-08");
 
 		for (int i = 0; i < so.length; i++) {
 			assertNotNull(so[i]);
 			for (int j = i + 1; j < so.length; j++) {
 				assertTrue("i=" + i + ", j=" + j, EcoreUtil.equals(so[i], so[j]));
 			}
 		}
 		SpecHierarchy sh0 = findInSpec("Spec1", so[0]);
 		assertNotNull(sh0);
 		assertNull(findInSpec("Spec2", so[0]));
 
 		for (int i = 1; i < 4; i++) {
 			assertNull(findInSpec("Spec1", so[i]));
 			SpecHierarchy shx = findInSpec("Spec2", so[i]);
 			assertNotNull(shx);
 			assertTrue(sh0.getLastChange().getTimeInMillis() < shx.getLastChange().getTimeInMillis());
 
 		}
 	}
 
 	@Test
 	public void testObj09() throws Exception {
 		SpecObject[] so = getObjectsByName("Obj-09");
 
 		for (int i = 0; i < so.length; i++) {
 			assertNotNull(so[i]);
 			for (int j = i + 1; j < so.length; j++) {
 				assertTrue("i=" + i + ", j=" + j, EcoreUtil.equals(so[i], so[j]));
 			}
 		}
 		for (int i = 0; i < 3; i++) {
 			assertNotNull(findInSpec("Spec1", so[i]));
 			assertNull(findInSpec("Spec2", so[i]));
 		}
 
 		assertNull(findInSpec("Spec1", so[3]));
 		assertNotNull(findInSpec("Spec2", so[3]));
 
 	}
 
 	protected static void doSaveReqIFFile(EObject reqif, String fileName) throws IOException {
 		URI emfURI = createEMFURI(fileName);
 		Resource resource = getXMLPersistenceMappingResourceSet().createResource(emfURI);
 		resource.getContents().add(reqif);
 		resource.save(getSaveOptions());
 	}
 
 }
