 /*******************************************************************************
  * Copyright (c) 2007, 2014 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.ui.swt.facades.internal;
 
 import junit.framework.TestCase;
 
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.widgets.Shell;
 
 import org.eclipse.riena.core.test.collect.UITestCase;
 import org.eclipse.riena.core.util.ReflectionUtils;
 import org.eclipse.riena.ui.swt.lnf.LnfManager;
 import org.eclipse.riena.ui.swt.lnf.rienadefault.RienaDefaultLnf;
 import org.eclipse.riena.ui.swt.utils.SwtUtilities;
 
 /**
  * Tests of the class {@link InfoFlyoutRCP}.
  */
 @UITestCase
 public class InfoFlyoutRCPTest extends TestCase {
 
 	private InfoFlyoutRCP infoFLyout;
 	private Shell shell;
 
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
		ReflectionUtils.setHidden(SwtUtilities.class, "cacheDpiFactors", new float[] { 0.0f, 0.0f }); //$NON-NLS-1$
 		LnfManager.setLnf(new MyLnf());
 		shell = new Shell();
 		infoFLyout = new InfoFlyoutRCP(shell);
 	}
 
 	@Override
 	protected void tearDown() throws Exception {
 		infoFLyout = null;
 		SwtUtilities.dispose(shell);
 		shell = null;
 		super.tearDown();
 	}
 
 	/**
 	 * Tests the <i>private</i> method {@code getLnfSettingAndConvertX(String)}.
 	 * 
 	 * @throws Exception
 	 *             handled by JUnit
 	 */
 	public void testGetLnfSettingAndConvertX() throws Exception {
 
 		final int dpiValue = ReflectionUtils.invokeHidden(infoFLyout, "getLnfSettingAndConvertX", "testIntValue"); //$NON-NLS-1$ //$NON-NLS-2$
 		assertEquals(10, dpiValue);
 
 	}
 
 	/**
 	 * Tests the <i>private</i> method {@code getLnfSettingAndConvertY(String)}.
 	 * 
 	 * @throws Exception
 	 *             handled by JUnit
 	 */
 	public void testGetLnfSettingAndConvertY() throws Exception {
 
 		final int dpiValue = ReflectionUtils.invokeHidden(infoFLyout, "getLnfSettingAndConvertY", "testIntValue"); //$NON-NLS-1$ //$NON-NLS-2$
 		assertEquals(16, dpiValue);
 
 	}
 
 	private static class MyLnf extends RienaDefaultLnf {
 
 		@Override
 		protected void initializeTheme() {
 			super.initializeTheme();
 			putLnfSetting("testIntValue", 5); //$NON-NLS-1$
 		}
 
 		@Override
 		public float[] getDpiFactors(final Point dpi) {
 			return new float[] { 2.0f, 3.2f };
 		}
 
 	}
 
 }
