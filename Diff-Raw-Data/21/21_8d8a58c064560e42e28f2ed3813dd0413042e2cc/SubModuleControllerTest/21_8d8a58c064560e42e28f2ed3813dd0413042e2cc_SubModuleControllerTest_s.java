 /*******************************************************************************
  * Copyright (c) 2007, 2009 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.navigation.ui.controllers;
 
 import java.io.ByteArrayOutputStream;
 import java.io.PrintStream;
 
 import junit.framework.TestCase;
 
 import org.eclipse.core.databinding.observable.Realm;
 import org.eclipse.jface.databinding.swt.SWTObservables;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 
 import org.eclipse.riena.core.RienaStatus;
 import org.eclipse.riena.core.util.ReflectionUtils;
 import org.eclipse.riena.internal.ui.ridgets.swt.ComboRidget;
 import org.eclipse.riena.internal.ui.ridgets.swt.EmbeddedTitleBarRidget;
 import org.eclipse.riena.internal.ui.ridgets.swt.LabelRidget;
 import org.eclipse.riena.navigation.ISubModuleNode;
 import org.eclipse.riena.navigation.model.ModuleNode;
 import org.eclipse.riena.navigation.model.NavigationProcessor;
 import org.eclipse.riena.navigation.model.SubModuleNode;
 import org.eclipse.riena.tests.collect.NonUITestCase;
 import org.eclipse.riena.ui.ridgets.IWindowRidget;
 
 /**
  * Tests of the class {@link SubModuleController}.
  */
 @NonUITestCase
 public class SubModuleControllerTest extends TestCase {
 
 	/**
 	 * Tests the method {@code getFullTitle()}.
 	 */
 	public void testGetFullTitle() {
 
 		ModuleNode module = new ModuleNode("m");
 		new ModuleController(module);
 
 		SubModuleNode subModule1 = new SubModuleNode("sm1");
 		subModule1.setParent(module);
 		module.addChild(subModule1);
 		MySubModuleController smController1 = new MySubModuleController(subModule1);
 		assertEquals("m", smController1.getFullTitle());
 
 		module.setPresentSingleSubModule(true);
 		assertEquals("m - sm1", smController1.getFullTitle());
 
 		SubModuleNode subModule2 = new SubModuleNode("sm2");
 		MySubModuleController smController2 = new MySubModuleController(subModule2);
 		subModule2.setParent(module);
 		module.addChild(subModule2);
 		assertEquals("m - sm1", smController1.getFullTitle());
 		assertEquals("m - sm2", smController2.getFullTitle());
 
 		module.setPresentSingleSubModule(false);
 		assertEquals("m - sm1", smController1.getFullTitle());
 		assertEquals("m - sm2", smController2.getFullTitle());
 
 		SubModuleNode subModule3 = new SubModuleNode("sm3");
 		MySubModuleController smController3 = new MySubModuleController(subModule3);
 		subModule3.setParent(subModule1);
 		subModule1.addChild(subModule3);
 		assertEquals("m - sm1", smController1.getFullTitle());
 		assertEquals("m - sm2", smController2.getFullTitle());
 		assertEquals("m - sm1 - sm3", smController3.getFullTitle());
 
 	}
 
 	/**
 	 * Tests the method {@code updateIcon(IWindowRidget)}.
 	 */
 	public void testUpdateIcon() {
 
 		EmbeddedTitleBarRidget windowRidget = new EmbeddedTitleBarRidget();
 
 		ModuleNode module = new ModuleNode("m");
 		module.setIcon("mIcon");
 		new ModuleController(module);
 
 		SubModuleNode subModule1 = new SubModuleNode("sm1");
 		subModule1.setIcon("sm1Icon");
 		subModule1.setParent(module);
 		module.addChild(subModule1);
 		MySubModuleController smController1 = new MySubModuleController(subModule1);
 		smController1.updateIcon(windowRidget);
 		assertEquals("mIcon", windowRidget.getIcon());
 
 		module.setPresentSingleSubModule(true);
 		assertEquals("m - sm1", smController1.getFullTitle());
 		smController1.updateIcon(windowRidget);
 		assertEquals("sm1Icon", windowRidget.getIcon());
 
 		SubModuleNode subModule2 = new SubModuleNode("sm2");
 		subModule2.setIcon("sm2Icon");
 		MySubModuleController smController2 = new MySubModuleController(subModule2);
 		subModule2.setParent(module);
 		module.addChild(subModule2);
 		smController1.updateIcon(windowRidget);
 		assertEquals("sm1Icon", windowRidget.getIcon());
 		smController2.updateIcon(windowRidget);
 		assertEquals("sm2Icon", windowRidget.getIcon());
 
 	}
 
 	/**
 	 * Tests the <i>private</i> method {@code isInvisibleInTree()}.
 	 */
 	public void testIsInvisibleInTree() {
 
 		SubModuleNode subModule1 = new SubModuleNode("sm1");
 		SubModuleController smController1 = new SubModuleController(subModule1);
 		boolean ret = ReflectionUtils.invokeHidden(smController1, "isInvisibleInTree");
 		assertFalse(ret);
 
 		ModuleNode module = new ModuleNode("m");
 		new ModuleController(module);
 		subModule1.setParent(module);
 		module.addChild(subModule1);
 		ret = ReflectionUtils.invokeHidden(smController1, "isInvisibleInTree");
 		assertTrue(ret);
 
 		module.setPresentSingleSubModule(true);
 		ret = ReflectionUtils.invokeHidden(smController1, "isInvisibleInTree");
 		assertFalse(ret);
 
 		module.setPresentSingleSubModule(false);
 		SubModuleNode subModule2 = new SubModuleNode("sm2");
 		subModule2.setIcon("sm2Icon");
 		SubModuleController smController2 = new SubModuleController(subModule2);
 		subModule2.setParent(module);
 		module.addChild(subModule2);
 		ret = ReflectionUtils.invokeHidden(smController1, "isInvisibleInTree");
 		assertFalse(ret);
 		ret = ReflectionUtils.invokeHidden(smController2, "isInvisibleInTree");
 		assertFalse(ret);
 
 	}
 
 	public void testUpdateAllRidgetsFromModel() {
 
 		Display display = Display.getDefault();
 		Shell shell = new Shell(display);
 		shell.pack();
 		shell.setVisible(true);
 
 		Realm realm = SWTObservables.getRealm(display);
 		assertNotNull(realm);
 		ReflectionUtils.invokeHidden(realm, "setDefault", realm);
 
 		SubModuleNode node = new SubModuleNode();
 		node.setNavigationProcessor(new NavigationProcessor());
 		SubModuleController controller = new SubModuleController(node);
 
 		LabelRidget labelRidget = new LabelRidget();
 		controller.addRidget("label", labelRidget);
 		assertNotNull(controller.getRidgets());
 		assertEquals(1, controller.getRidgets().size());
 
 		ComboRidget comboRidget = new ComboRidget();
 		controller.addRidget("combo ridget", comboRidget);
 		assertNotNull(controller.getRidgets());
 		assertEquals(2, controller.getRidgets().size());
 
		PrintStream beforeErr = System.err;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream err = new PrintStream(baos);
		System.setErr(err);
		controller.updateAllRidgetsFromModel();
		System.setErr(beforeErr);
 		if (RienaStatus.isDevelopment()) {
			assertEquals(true, baos.toString().contains("unsuccessful"));
			assertEquals(true, baos.toString().contains("ridget"));
 		}
		System.err.print(baos.toString());

 	}
 
 	/**
 	 * This extension of {@code SubModuleController} is only necessary to
 	 * increase the visibility of protected methods.
 	 */
 	private static class MySubModuleController extends SubModuleController {
 
 		public MySubModuleController(ISubModuleNode navigationNode) {
 			super(navigationNode);
 		}
 
 		@Override
 		public String getFullTitle() {
 			return super.getFullTitle();
 		}
 
 		@Override
 		public void updateIcon(IWindowRidget windowRidget) {
 			super.updateIcon(windowRidget);
 		}
 
 	}
 
 }
