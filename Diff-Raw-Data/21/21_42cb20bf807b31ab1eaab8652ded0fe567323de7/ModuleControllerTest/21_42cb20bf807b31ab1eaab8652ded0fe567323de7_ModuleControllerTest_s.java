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
 package org.eclipse.riena.navigation.ui.controllers;
 
 import java.util.List;
 
 import junit.framework.TestCase;
 
 import org.eclipse.riena.internal.ui.ridgets.swt.ShellRidget;
 import org.eclipse.riena.navigation.INavigationNode;
 import org.eclipse.riena.navigation.model.ModuleNode;
 import org.eclipse.riena.navigation.model.NavigationProcessor;
 import org.eclipse.riena.navigation.model.SubModuleNode;
 import org.eclipse.swt.widgets.Shell;
 
 /**
  * Tests of the class <code>ModuleController</code>.
  */
 public class ModuleControllerTest extends TestCase {
 
 	public void testAfterBind() throws Exception {
 
 		ModuleNode node = new ModuleNode();
 		node.setClosable(true);
 		node.setLabel("Hello");
 		ModuleController controller = new ModuleController(node);
 		ShellRidget shellRidget = new ShellRidget();
		shellRidget.setUIControl(new Shell());
 		controller.setWindowRidget(shellRidget);
 		controller.afterBind();
 		assertTrue(controller.isCloseable());
 		assertEquals("Hello", shellRidget.getTitle());
 
 		node.setClosable(false);
 		controller.configureRidgets();
 		assertFalse(controller.isCloseable());
 
 	}
 
 	/**
 	 * Tests the method {@code getVisibleChildren}.
 	 */
 	public void testGetVisibleChildren() {
 
 		ModuleNode node = new ModuleNode();
 		node.setNavigationProcessor(new NavigationProcessor());
 		SubModuleNode sub1 = new SubModuleNode();
 		node.addChild(sub1);
 		SubModuleNode sub2 = new SubModuleNode();
 		node.addChild(sub2);
 		SubModuleNode sub3 = new SubModuleNode();
 		node.addChild(sub3);
 
 		ModuleController controller = new ModuleController(node);
 		List<INavigationNode<?>> nodes = controller.getVisibleChildren(node);
 		assertEquals(3, nodes.size());
 		assertSame(sub1, nodes.get(0));
 		assertSame(sub2, nodes.get(1));
 		assertSame(sub3, nodes.get(2));
 
 		sub1.setEnabled(false);
 		sub2.setVisible(false);
 		nodes = controller.getVisibleChildren(node);
 		assertEquals(2, nodes.size());
 		assertSame(sub1, nodes.get(0));
 		assertSame(sub3, nodes.get(1));
 
 	}
 
 	/**
 	 * Tests the method {@code hasSingleLeafChild}.
 	 */
 	public void testHasSingleLeafChild() {
 
 		ModuleNode node = new ModuleNode();
 		node.setNavigationProcessor(new NavigationProcessor());
 		SubModuleNode sub1 = new SubModuleNode();
 		node.addChild(sub1);
 		SubModuleNode sub2 = new SubModuleNode();
 		node.addChild(sub2);
 		SubModuleNode sub3 = new SubModuleNode();
 		sub2.addChild(sub3);
 
 		ModuleController controller = new ModuleController(node);
 		assertFalse(controller.hasSingleLeafChild());
 
 		sub1.setVisible(false);
 		assertFalse(controller.hasSingleLeafChild());
 
 		sub1.setVisible(true);
 		sub2.setVisible(false);
 		assertTrue(controller.hasSingleLeafChild());
 
 		sub2.setVisible(true);
 		assertFalse(controller.hasSingleLeafChild());
 
 		node.removeChild(null, sub2);
 		assertTrue(controller.hasSingleLeafChild());
 
 	}
 
 }
