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
 package org.eclipse.riena.internal.ui.ridgets.swt;
 
 import junit.framework.TestCase;
 
 import org.eclipse.riena.core.util.ReflectionUtils;
 import org.eclipse.riena.ui.swt.utils.SWTBindingPropertyLocator;
 import org.eclipse.riena.ui.swt.utils.SwtUtilities;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.ToolBar;
 import org.eclipse.swt.widgets.ToolItem;
 
 /**
  * Tests of the class {@link ToolItemProperties}.
  */
 public class ToolItemPropertiesTest extends TestCase {
 
 	private Shell shell;
 
 	@Override
 	protected void setUp() throws Exception {
 		shell = new Shell();
 	}
 
 	@Override
 	protected void tearDown() throws Exception {
 		SwtUtilities.disposeWidget(shell);
 	}
 
 	/**
 	 * Tests the constructor of the class {@code ToolItemProperties}.
 	 */
 	public void testToolItemProperties() {
 
 		ToolItemRidget ridget = new ToolItemRidget();
 		ToolBar toolbar = new ToolBar(shell, SWT.BORDER);
 		ToolItem item = new ToolItem(toolbar, SWT.PUSH);
 		SWTBindingPropertyLocator.getInstance().setBindingProperty(item, "item1");
 		ridget.setUIControl(item);
 
 		MyToolItemProperties itemProperties = new MyToolItemProperties(ridget);
 		ToolBar parent = ReflectionUtils.getHidden(itemProperties, "parent");
 		assertSame(ridget, itemProperties.getRidget());
 		assertSame(toolbar, parent);
 		int index = ReflectionUtils.invokeHidden(itemProperties, "getIndex");
		assertEquals(0, index);
 
 	}
 
 	/**
 	 * Tests the method {@code createItem}.
 	 */
 	public void testCreateItem() {
 
 		ToolItemRidget ridget = new ToolItemRidget();
 		ToolBar toolbar = new ToolBar(shell, SWT.BORDER);
 		ToolItem item = new ToolItem(toolbar, SWT.PUSH);
 		String text = "toolItem0815";
 		item.setText(text);
 		ridget.setUIControl(item);
 
 		MyToolItemProperties itemProperties = new MyToolItemProperties(ridget);
 		item.dispose();
 		assertEquals(0, toolbar.getItemCount());
 
 		ToolItem item2 = itemProperties.createItem();
 		assertSame(item2, ridget.getUIControl());
 		assertSame(toolbar, item2.getParent());
 		assertEquals(1, toolbar.getItemCount());
 		assertEquals(text, item2.getText());
 
 	}
 
 	private class MyToolItemProperties extends ToolItemProperties {
 
 		public MyToolItemProperties(ToolItemRidget ridget) {
 			super(ridget);
 		}
 
 		@Override
 		public ToolItem createItem() {
 			return super.createItem();
 		}
 
 		@Override
 		public ToolItemRidget getRidget() {
 			return super.getRidget();
 		}
 
 	}
 
 }
