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
 
 import junit.framework.TestCase;
 
 import org.eclipse.riena.internal.ui.ridgets.swt.ShellRidget;
 import org.eclipse.riena.navigation.model.ModuleNode;
 import org.eclipse.swt.widgets.Shell;
 
 /**
  * Tests of the class <code>ModuleController</code>.
  */
 public class ModuleControllerTest extends TestCase {
 
 	public void testAfterBind() throws Exception {
 
 		ModuleNode node = new ModuleNode(null);
 		node.setCloseable(true);
 		node.setLabel("Hello");
 		ModuleController controller = new ModuleController(node);
 		ShellRidget shellRidget = new ShellRidget();
 		shellRidget.setUIControl(new Shell());
 		controller.setWindowRidget(shellRidget);
 		controller.afterBind();
 		assertTrue(controller.isCloseable());
 		assertEquals("Hello", shellRidget.getTitle());
 
 		node.setCloseable(false);
		controller.afterBind();
 		assertFalse(controller.isCloseable());
 
 	}
 }
