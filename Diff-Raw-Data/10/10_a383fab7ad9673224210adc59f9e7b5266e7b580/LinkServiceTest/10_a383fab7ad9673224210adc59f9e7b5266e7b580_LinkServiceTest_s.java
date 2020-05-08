 /*******************************************************************************
  * <copyright>
  *
  * Copyright (c) 2005, 2010 SAP AG.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    SAP AG - initial API, implementation and documentation
  *
  * </copyright>
  *
  *******************************************************************************/
 package org.eclipse.graphiti.tests.cases;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EcoreFactory;
 import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
 import org.eclipse.graphiti.mm.Property;
 import org.eclipse.graphiti.mm.pictograms.Diagram;
 import org.eclipse.graphiti.mm.pictograms.PictogramElement;
 import org.eclipse.graphiti.mm.pictograms.PictogramLink;
 import org.eclipse.graphiti.mm.pictograms.PictogramsFactory;
 import org.eclipse.graphiti.mm.pictograms.Shape;
 import org.eclipse.graphiti.services.Graphiti;
 import org.eclipse.graphiti.services.ILinkService;
 import org.eclipse.graphiti.tests.reuse.GFAbstractTestCase;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 /**
  *
  */
 public class LinkServiceTest extends GFAbstractTestCase {
 
 	// Fixture
 	private Diagram d;
 	private Shape s1;
 	private static String PROPERTY_VALUE_1 = "value1";
 
 	public static ILinkService linkService = Graphiti.getLinkService();
 
 	@BeforeClass
 	public static void prepareClass() {
 	}
 
 	@Before
 	public void initializeTest() {
 		d = Graphiti.getPeCreateService().createDiagram("tutorial", "test", true);
 		assertNotNull(d);
 		s1 = Graphiti.getPeCreateService().createShape(d, true);
 		s1.setGraphicsAlgorithm(Graphiti.getGaCreateService().createRectangle(d));
 		assertNotNull(s1);
 		ResourceImpl resource = new ResourceImpl();
 		resource.getContents().add(d);
 	}
 
 	@Test
 	public void getBusinessObjectForLinkedPictogramElement() {
 		EClass bo = EcoreFactory.eINSTANCE.createEClass();
 		d.eResource().getContents().add(bo);
 		PictogramLink link = createOrGetPictogramLink(s1);
 		link.getBusinessObjects().add(bo);
 
 		EObject linkedBo = linkService.getBusinessObjectForLinkedPictogramElement(s1);
 		assertNotNull(linkedBo);
 		assertEquals(bo, linkedBo);
 	}
 
 	@Test
 	public void getAllBusinessObjectsForLinkedPictogramElement() {
 		PictogramLink link = createOrGetPictogramLink(s1);
 		EClass bo1 = EcoreFactory.eINSTANCE.createEClass();
 		d.eResource().getContents().add(bo1);
 		link.getBusinessObjects().add(bo1);
 		EClass bo2 = EcoreFactory.eINSTANCE.createEClass();
 		d.eResource().getContents().add(bo2);
 		link.getBusinessObjects().add(bo2);
 
 		EObject linkedBos[] = linkService.getAllBusinessObjectsForLinkedPictogramElement(s1);
 		assertEquals(2, linkedBos.length);
 		List<EObject> boList = Arrays.asList(linkedBos);
 		assertTrue(boList.contains(bo1));
 		assertTrue(boList.contains(bo2));
 	}
 
 	@Test
 	public void linkProperty() {
 		// hasLinkProperty()
 		// setLinkProperty()
 		// getLinkProperty()
 		boolean hasLinkProperty = linkService.hasLinkProperty(s1, PROPERTY_VALUE_1);
 		assertFalse(hasLinkProperty);
 
 		Property linkProperty = linkService.getLinkProperty(s1);
 		assertNull(linkProperty);
 
 		linkService.setLinkProperty(s1, PROPERTY_VALUE_1);
 
 		hasLinkProperty = linkService.hasLinkProperty(s1, PROPERTY_VALUE_1);
 		assertTrue(hasLinkProperty);
 
 		linkProperty = linkService.getLinkProperty(s1);
 		assertNotNull(linkProperty);
 
 		String value = linkProperty.getValue();
 		assertEquals(PROPERTY_VALUE_1, value);
 	}
 
 	@Test
 	public void getPictogramElements() {
 
 		Shape s1a = Graphiti.getPeCreateService().createShape(d, true);
 		Shape s2a = Graphiti.getPeCreateService().createShape(d, true);
 		Shape s3i = Graphiti.getPeCreateService().createShape(d, false);
 
 		EClass bo1 = EcoreFactory.eINSTANCE.createEClass();
 		bo1.setName("bo1");
 		d.eResource().getContents().add(bo1);
 		EClass bo2 = EcoreFactory.eINSTANCE.createEClass();
 		bo2.setName("bo2");
 		d.eResource().getContents().add(bo2);
 
 		List<EObject> boList = new ArrayList<EObject>();
 		boList.add(bo1);
 
 		List<PictogramElement> pes = linkService.getPictogramElements(d, boList, false);
 		assertTrue(pes.isEmpty());
 
 		link(s3i, bo1);
 		pes = linkService.getPictogramElements(d, boList, true);
 		assertTrue(pes.isEmpty());
 		pes = linkService.getPictogramElements(d, boList, false);
 		assertFalse(pes.isEmpty());
 		assertEquals(s3i, pes.get(0));
 
 		link(s1a, bo1);
 		link(s2a, bo2);
 		pes = linkService.getPictogramElements(d, boList, true);
 		assertFalse(pes.isEmpty());
 		assertEquals(s1a, pes.get(0));
 
 		pes = linkService.getPictogramElements(d, bo1);
 		assertTrue(pes.size() == 2);
 		assertTrue(pes.contains(s1a));
 		assertTrue(pes.contains(s3i));
 	}
 
 	private PictogramLink createOrGetPictogramLink(PictogramElement pe) {
 		PictogramLink link = pe.getLink();
 		if (link == null) {
 			link = PictogramsFactory.eINSTANCE.createPictogramLink();
 			link.setPictogramElement(pe);
 			d.getPictogramLinks().add(link);
 		}
 		return link;
 	}
 
 	private void link(PictogramElement pe, EObject bo) {
 		PictogramLink link = createOrGetPictogramLink(pe);
 		link.getBusinessObjects().add(bo);
 	}
 }
