 /*******************************************************************************
  * Copyright (c) 2007, 2008 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.navigation.ui.swt.presentation;
 
 import org.eclipse.riena.navigation.model.NavigationNodeId;
 import org.eclipse.riena.navigation.model.SubModuleNode;
 import org.eclipse.riena.tests.RienaTestCase;
 
 /**
  * Tests for the SwtPresentationManager.
  */
 public class SwtPresentationManagerTest extends RienaTestCase {
 
 	private SwtPresentationManager swtPresentationManager;
 
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 
 		addPluginXml(SwtPresentationManagerTest.class, "SwtPresentationManagerTest.xml");
 
 		swtPresentationManager = new SwtPresentationManager();
 
 	}
 
 	public void testGetSwtViewIdSharedView() throws Exception {
 
 		SubModuleNode node1 = new SubModuleNode();
 		node1.setPresentationId(new NavigationNodeId("testSharedViewId", "testInstanceId1"));
 		SubModuleNode node2 = new SubModuleNode();
 		node2.setPresentationId(new NavigationNodeId("testSharedViewId", "testInstanceId2"));
 
 		SwtViewId swtViewId1 = swtPresentationManager.getSwtViewId(node1);
 		assertEquals("org.eclipse.riena.navigation.ui.swt.views.TestView", swtViewId1.getId());
 		assertEquals("shared", swtViewId1.getSecondary());
 
		SwtViewId swtViewId2 = swtPresentationManager.getSwtViewId(node1);
 		assertEquals("org.eclipse.riena.navigation.ui.swt.views.TestView", swtViewId2.getId());
 		assertEquals("shared", swtViewId2.getSecondary());
 
 	}
 
 	public void testGetSwtViewIdNotSharedView() throws Exception {
 
 		SubModuleNode node1 = new SubModuleNode();
 		node1.setPresentationId(new NavigationNodeId("testNotSharedViewId", "testInstanceId1"));
 		SubModuleNode node2 = new SubModuleNode();
 		node2.setPresentationId(new NavigationNodeId("testNotSharedViewId", "testInstanceId2"));
 
 		SwtViewId swtViewId1 = swtPresentationManager.getSwtViewId(node1);
 		assertEquals("org.eclipse.riena.navigation.ui.swt.views.TestView", swtViewId1.getId());
 		assertEquals("1", swtViewId1.getSecondary());
 
 		SwtViewId swtViewId2 = swtPresentationManager.getSwtViewId(node2);
 		assertEquals("org.eclipse.riena.navigation.ui.swt.views.TestView", swtViewId2.getId());
 		assertEquals("2", swtViewId2.getSecondary());
 
 		SwtViewId swtViewId1Again = swtPresentationManager.getSwtViewId(node1);
 		assertEquals("org.eclipse.riena.navigation.ui.swt.views.TestView", swtViewId1Again.getId());
 		assertEquals("1", swtViewId1Again.getSecondary());
 	}
 }
