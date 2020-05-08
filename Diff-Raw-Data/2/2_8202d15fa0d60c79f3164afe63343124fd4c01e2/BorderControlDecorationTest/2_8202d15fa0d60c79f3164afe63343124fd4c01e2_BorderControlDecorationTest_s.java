 /*******************************************************************************
  * Copyright (c) 2007, 2012 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.ui.swt;
 
 import junit.framework.TestCase;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.nebula.widgets.compositetable.AbsoluteLayout;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 
 import org.eclipse.riena.core.util.ReflectionUtils;
 import org.eclipse.riena.internal.core.test.collect.UITestCase;
 import org.eclipse.riena.ui.swt.utils.SwtUtilities;
 
 /**
  * Test the classes {@link BorderControlDecoration} and {@link BorderDrawer}
  */
 @UITestCase
 public class BorderControlDecorationTest extends TestCase {
 
 	private Display display;
 	private Shell shell;
 	private Text text;
 
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		display = Display.getDefault();
 		shell = new Shell(display);
 		shell.setBounds(10, 10, 200, 100);
 		shell.setLayout(new AbsoluteLayout());
 		text = new Text(shell, SWT.BORDER);
 		text.setLayoutData(new Rectangle(4, 6, 40, 20));
 	}
 
 	@Override
 	protected void tearDown() {
 		SwtUtilities.dispose(text);
 		SwtUtilities.dispose(shell);
 	}
 
 	/**
 	 * Tests the <i>private</i> method {@code shouldShowDecoration()}.
 	 */
 	public void testShouldShowDecoration() {
 		BorderControlDecoration deco = new BorderControlDecoration(text, 2);
 		final BorderDrawer borderDrawer = ReflectionUtils.getHidden(deco, "borderDrawer");
 		boolean ret = ReflectionUtils.invokeHidden(borderDrawer, "shouldShowDecoration");
 		assertFalse(ret);
 
 		shell.setVisible(true);
 		deco.show();
 		ret = ReflectionUtils.invokeHidden(borderDrawer, "shouldShowDecoration");
 		assertTrue(ret);
 
 		deco.hide();
 		ret = ReflectionUtils.invokeHidden(borderDrawer, "shouldShowDecoration");
 		assertFalse(ret);
 
 		deco.dispose();
 		deco = new BorderControlDecoration(text, 0);
 		deco.show();
 		ret = ReflectionUtils.invokeHidden(borderDrawer, "shouldShowDecoration");
 		assertFalse(ret);
 
 		deco.dispose();
 		shell.setVisible(false);
 
 	}
 
 	public void testDecorationRectangle() {
 		final int borderWidth = 3;
		final BorderControlDecoration deco = new BorderControlDecoration(text, borderWidth);
 		deco.show();
 		shell.setVisible(true);
 		final BorderDrawer borderDrawer = ReflectionUtils.getHidden(deco, "borderDrawer");
 		final Rectangle decoRect = ReflectionUtils.getHidden(borderDrawer, "visibleControlAreaOnDisplay");
 
 		final Point onDisplay = text.toDisplay(0, 0);
 		final int border = borderWidth + text.getBorderWidth();
 		final int expectedWidth = text.getBounds().width - 1 + 2 * borderWidth;
 		final int expectedHeight = text.getBounds().height - 1 + 2 * borderWidth;
 		assertEquals(new Rectangle(onDisplay.x - border, onDisplay.y - border, expectedWidth, expectedHeight), decoRect);
 
 		shell.setVisible(false);
 
 	}
 
 }
