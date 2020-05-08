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
 package org.eclipse.riena.internal.ui.ridgets.swt;
 
 import junit.framework.TestCase;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.ToolBar;
 import org.eclipse.swt.widgets.ToolItem;
 
 import org.eclipse.riena.tests.collect.NonUITestCase;
 import org.eclipse.riena.ui.swt.utils.SwtUtilities;
 
 /**
  * Tests the class {@link AbstractItemRidget}.
  */
 @NonUITestCase
 public class AbstractItemRidgetTest extends TestCase {
 
 	private AbstractItemRidget itemRidget;
 	private Shell shell;
 	private ToolBar toolbar;
 
 	@Override
 	protected void setUp() throws Exception {
 		itemRidget = new ToolItemRidget();
 		shell = new Shell();
 		toolbar = new ToolBar(shell, SWT.NONE);
 	}
 
 	@Override
 	protected void tearDown() throws Exception {
 		itemRidget = null;
 		SwtUtilities.disposeWidget(shell);
 		SwtUtilities.disposeWidget(toolbar);
 	}
 
 	/**
 	 * Tests the method {@code getContributionItem()}.
 	 */
 	public void testGetContributionItem() {
 
		assertNull(itemRidget.getContributionItem());
 
 		ToolItem item = new ToolItem(toolbar, SWT.NONE);
 		itemRidget.setUIControl(item);
		assertNull(itemRidget.getContributionItem());
 
 		item.dispose();
		assertNull(itemRidget.getContributionItem());
 
 	}
 
 	/**
 	 * Tests the method {@code isEnabled(()}.
 	 */
 	public void testIsEnabled() {
 
 		ToolItem item = new ToolItem(toolbar, SWT.NONE);
 		itemRidget.setUIControl(item);
 
 		assertTrue(itemRidget.isEnabled());
 
 		itemRidget.setEnabled(false);
 		assertFalse(itemRidget.isEnabled());
 
 	}
 
 }
