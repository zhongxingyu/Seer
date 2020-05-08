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
 package org.eclipse.riena.navigation.ui.swt.views;
 
 import junit.framework.TestCase;
 
 import org.eclipse.riena.navigation.model.ModuleGroupNode;
 import org.eclipse.riena.navigation.model.ModuleNode;
 import org.eclipse.riena.navigation.model.NavigationProcessor;
 import org.eclipse.riena.navigation.ui.swt.lnf.renderer.ModuleGroupRenderer;
 import org.eclipse.riena.ui.swt.lnf.ILnfKeyConstants;
 import org.eclipse.riena.ui.swt.lnf.LnfManager;
 import org.eclipse.riena.ui.swt.lnf.rienadefault.RienaDefaultLnf;
 import org.eclipse.riena.ui.swt.utils.SwtUtilities;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.widgets.Shell;
 
 /**
  * Tests of the class {@link ModuleGroupView}.
  */
 public class ModuleGroupViewTest extends TestCase {
 
 	private ModuleGroupView view;
 	private ModuleGroupNode node;
 	private Shell shell;
 
 	@Override
 	protected void setUp() throws Exception {
 
 		shell = new Shell();
 		view = new ModuleGroupView(shell, SWT.NONE);
 		node = new ModuleGroupNode();
 		node.setNavigationProcessor(new NavigationProcessor());
 		view.bind(node);
 
 	}
 
 	@Override
 	protected void tearDown() throws Exception {
 		SwtUtilities.disposeWidget(view);
 		SwtUtilities.disposeWidget(shell);
 		node = null;
 	}
 
 	/**
 	 * Tests the method {@code calculateBounds(int)}.
 	 */
 	public void testCalculateBounds() {
 
 		LnfManager.setLnf(new MyLnF());
 
 		int y = view.calculateBounds(10);
 		assertEquals(10, y);
 
 		ModuleView moduleView = new ModuleView(shell);
 		ModuleNode moduleNode = new ModuleNode();
		node.addChild(moduleNode);
 		moduleView.bind(moduleNode);
 
 		view.registerModuleView(moduleView);
 		y = view.calculateBounds(10);
 		assertTrue(y > 10);
 		FormData data = (FormData) view.getLayoutData();
 		assertEquals(10, data.top.offset);
 		assertTrue((data.bottom.offset > 10) && (data.bottom.offset < y));
 
 		node.setVisible(false);
 		y = view.calculateBounds(10);
 		assertEquals(10, y);
 
 	}
 
 	private class MyLnF extends RienaDefaultLnf {
 
 		@Override
 		protected void initWidgetRendererDefaults() {
 			getRendererTable().put(ILnfKeyConstants.MODULE_GROUP_RENDERER, new ModuleGroupRenderer());
 		}
 
 	}
 
 }
