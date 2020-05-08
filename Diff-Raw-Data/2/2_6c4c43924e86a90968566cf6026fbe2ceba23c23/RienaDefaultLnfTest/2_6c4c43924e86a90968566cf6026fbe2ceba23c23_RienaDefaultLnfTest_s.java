 /*******************************************************************************
  * Copyright (c) 2007, 2009 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *    Florian Pirchner - FontDescriptor
  *******************************************************************************/
 package org.eclipse.riena.ui.swt.lnf.rienadefault;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.FontData;
 
 import org.eclipse.riena.core.util.ReflectionUtils;
 import org.eclipse.riena.internal.core.test.RienaTestCase;
 import org.eclipse.riena.internal.core.test.collect.NonUITestCase;
 import org.eclipse.riena.ui.ridgets.AbstractMarkerSupport;
 import org.eclipse.riena.ui.ridgets.swt.BorderMarkerSupport;
 import org.eclipse.riena.ui.ridgets.swt.MarkerSupport;
 import org.eclipse.riena.ui.swt.lnf.ILnfCustomizer;
 import org.eclipse.riena.ui.swt.lnf.ILnfMarkerSupportExtension;
 import org.eclipse.riena.ui.swt.lnf.ILnfTheme;
 import org.eclipse.riena.ui.swt.lnf.LnfKeyConstants;
 
 /**
  * Tests of the class <code>RienaDefaultLnf</code>.
  */
 @NonUITestCase
 public class RienaDefaultLnfTest extends RienaTestCase {
 
 	private static final boolean BOOLEAN_VALUE = true;
 	private static final Integer INTEGER_VALUE = 4;
 	private static final String BOOLEAN_KEY = "boolean";
 	private static final String INTEGER_KEY = "integer";
 
 	private RienaDefaultLnf lnf;
 
 	/**
 	 * @see junit.framework.TestCase#setUp()
 	 */
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		lnf = new RienaDefaultLnf();
 		lnf.initialize();
 	}
 
 	/**
 	 * @see junit.framework.TestCase#tearDown()
 	 */
 	@Override
 	protected void tearDown() throws Exception {
 		lnf.setTheme(null);
 		lnf.uninitialize();
 		lnf = null;
 		super.tearDown();
 	}
 
 	/**
 	 * Test of the method <code>initialize()</code>.
 	 * 
 	 * @throws Exception
 	 *             handled by JUnit
 	 */
 	public void testInitialize() throws Exception {
 
 		lnf.uninitialize();
 
 		assertNull(lnf.getRenderer(LnfKeyConstants.SUB_MODULE_VIEW_BORDER_RENDERER));
 		assertNull(lnf.getFont(LnfKeyConstants.EMBEDDED_TITLEBAR_FONT));
 		assertNull(lnf.getColor(LnfKeyConstants.EMBEDDED_TITLEBAR_ACTIVE_FOREGROUND));
 
 		lnf.initialize();
 
 		assertNotNull(lnf.getFont(LnfKeyConstants.EMBEDDED_TITLEBAR_FONT));
 		assertNotNull(lnf.getColor(LnfKeyConstants.EMBEDDED_TITLEBAR_ACTIVE_FOREGROUND));
 
 	}
 
 	/**
 	 * Test of the method <code>uninitialize()</code>.
 	 * 
 	 * @throws Exception
 	 *             handled by JUnit
 	 */
 	public void testUninitialize() throws Exception {
 
 		final Color color = lnf.getColor(LnfKeyConstants.EMBEDDED_TITLEBAR_ACTIVE_FOREGROUND);
 		assertNotNull(color);
 		final Font font = lnf.getFont(LnfKeyConstants.EMBEDDED_TITLEBAR_FONT);
 		assertNotNull(font);
 
 		lnf.uninitialize();
 
 		assertFalse(font.isDisposed());
 		assertNull(lnf.getFont(LnfKeyConstants.EMBEDDED_TITLEBAR_FONT));
 		assertFalse(color.isDisposed());
 		assertNull(lnf.getColor(LnfKeyConstants.EMBEDDED_TITLEBAR_ACTIVE_FOREGROUND));
 		assertNotSame(color, lnf.getColor(LnfKeyConstants.EMBEDDED_TITLEBAR_ACTIVE_FOREGROUND)); // TODO Could be removed
 
 	}
 
 	/**
 	 * Test of the method <code>getColor(String)</code>.
 	 * 
 	 * @throws Exception
 	 *             handled by JUnit
 	 */
 	public void testGetColor() throws Exception {
 
 		lnf.initialize();
 		assertNotNull(lnf.getColor(LnfKeyConstants.EMBEDDED_TITLEBAR_ACTIVE_FOREGROUND));
 		assertNull(lnf.getColor(LnfKeyConstants.EMBEDDED_TITLEBAR_FONT));
 		assertNull(lnf.getColor("dummy"));
 
 	}
 
 	/**
 	 * Test of the method <code>getFont(String)</code>.
 	 * 
 	 * @throws Exception
 	 *             handled by JUnit
 	 */
 	public void testGetFont() throws Exception {
 
 		lnf.initialize();
 		assertNull(lnf.getFont(LnfKeyConstants.EMBEDDED_TITLEBAR_ACTIVE_FOREGROUND));
 		assertNotNull(lnf.getFont(LnfKeyConstants.EMBEDDED_TITLEBAR_FONT));
 		assertNull(lnf.getFont("dummy"));
 
 	}
 
 	/**
 	 * Test of the method <code>getFont(String, int, int)</code>.
 	 * 
 	 * @throws Exception
 	 *             handled by JUnit
 	 */
 	public void testGetFontWithProps() throws Exception {
 
 		lnf.initialize();
 		final Font font = lnf.getFont(LnfKeyConstants.EMBEDDED_TITLEBAR_FONT, 10, SWT.BOLD | SWT.ITALIC);
 		FontData data = font.getFontData()[0];
 		assertEquals(SWT.BOLD | SWT.ITALIC, data.getStyle());
 
 		final Font font1 = lnf.getFont(LnfKeyConstants.EMBEDDED_TITLEBAR_FONT, 12, SWT.BOLD);
 		data = font1.getFontData()[0];
 		assertEquals(SWT.BOLD, data.getStyle());
 
 		final Font font2 = lnf.getFont(LnfKeyConstants.EMBEDDED_TITLEBAR_FONT, 12, SWT.BOLD);
 		assertSame(font1, font2);
 
 		final Font fontNull = lnf.getFont(LnfKeyConstants.EMBEDDED_TITLEBAR_ACTIVE_FOREGROUND, 12, SWT.BOLD);
 		assertNull(fontNull);
 	}
 
 	/**
 	 * Test of the method <code>getTheme()</code>.
 	 * 
 	 * @throws Exception
 	 *             handled by JUnit
 	 */
 	public void testGetTheme() throws Exception {
 
 		assertEquals(RienaDefaultTheme.class, lnf.getTheme().getClass());
 
 		lnf.setTheme(new DummyTheme());
 		assertEquals(DummyTheme.class, lnf.getTheme().getClass());
 
 	}
 
 	/**
 	 * Test of the method <code>setTheme()</code>.
 	 * 
 	 * @throws Exception
 	 *             handled by JUnit
 	 */
 	public void testSetTheme() throws Exception {
 
 		final Color color1 = lnf.getColor(LnfKeyConstants.EMBEDDED_TITLEBAR_ACTIVE_FOREGROUND);
 		assertNotNull(color1);
 
 		lnf.setTheme(new DummyTheme());
 		lnf.initialize();
 		assertFalse(color1.isDisposed());
 
 		final Color color2 = lnf.getColor(LnfKeyConstants.EMBEDDED_TITLEBAR_ACTIVE_FOREGROUND);
 		assertNull(color2);
 
 	}
 
 	/**
 	 * Test of the method {@code getIntegerSetting(String, Integer)}.
 	 * 
 	 * @throws Exception
 	 *             handled by JUnit
 	 */
 	public void testGetIntegerSetting() throws Exception {
 
 		Integer value = lnf.getIntegerSetting(INTEGER_KEY, 300);
 		assertEquals(300, value.intValue());
 
 		lnf.setTheme(new DummyTheme());
 		lnf.initialize();
 		value = lnf.getIntegerSetting(INTEGER_KEY, 300);
 		assertEquals(INTEGER_VALUE.intValue(), value.intValue());
 
 	}
 
 	/**
 	 * Test of the method {@code getBooleanSetting(String, boolean)}.
 	 * 
 	 * @throws Exception
 	 *             handled by JUnit
 	 */
 	public void testGetBooleanSetting() throws Exception {
 
 		boolean value = lnf.getBooleanSetting(BOOLEAN_KEY, true);
 		assertTrue(value);
 		value = lnf.getBooleanSetting(BOOLEAN_KEY, false);
 		assertFalse(value);
 
 		lnf.setTheme(new DummyTheme());
 		lnf.initialize();
 		value = lnf.getBooleanSetting(BOOLEAN_KEY, false);
 		assertEquals(BOOLEAN_VALUE, value);
 
 	}
 
 	/**
 	 * Tests the method {@code getMarkerSupport}.
 	 */
 	public void testGetMarkerSupport() {
 
 		lnf.update(new ILnfMarkerSupportExtension[] { new ILnfMarkerSupportExtension() {
 			public String getId() {
 				return "defaultMarkerSupport";
 			}
 
 			public AbstractMarkerSupport createMarkerSupport() {
 				return new MarkerSupport();
 			}
 		}, new ILnfMarkerSupportExtension() {
 			public String getId() {
 				return "borderMarkerSupport";
 			}
 
 			public AbstractMarkerSupport createMarkerSupport() {
 				return new BorderMarkerSupport();
 			}
 		} });
 
 		AbstractMarkerSupport markerSupport = lnf.getMarkerSupport(null);
 		assertNotNull(markerSupport);
		assertTrue(markerSupport.getClass() == MarkerSupport.class);
 
 		lnf.setTheme(new DummyTheme());
 		lnf.initialize();
 		markerSupport = lnf.getMarkerSupport(null);
 		assertNull(markerSupport);
 
 		lnf.setTheme(new DummyTheme2());
 		lnf.initialize();
 		markerSupport = lnf.getMarkerSupport(null);
 		assertNotNull(markerSupport);
 		assertTrue(markerSupport.getClass() == BorderMarkerSupport.class);
 
 	}
 
 	/**
 	 * Tests the <i>private</i> method {@code readSystemProperties()}.
 	 */
 	public void testReadSystemProperties() {
 
 		lnf.setTheme(new DummyTheme());
 		lnf.initialize();
 		assertEquals(INTEGER_VALUE, lnf.getIntegerSetting(INTEGER_KEY));
 
 		System.setProperty("riena.lnf.setting." + INTEGER_KEY, "4711");
 		ReflectionUtils.invokeHidden(lnf, "readSystemProperties");
 		assertEquals(Integer.valueOf(4711), lnf.getIntegerSetting(INTEGER_KEY));
 
 	}
 
 	/**
 	 * A simple look and feel theme with a couple of custom settings.
 	 */
 	private static class DummyTheme implements ILnfTheme {
 
 		public void customizeLnf(final ILnfCustomizer customizer) {
 			customizer.putLnfSetting(INTEGER_KEY, INTEGER_VALUE);
 			customizer.putLnfSetting(BOOLEAN_KEY, BOOLEAN_VALUE);
 			customizer.putLnfSetting(LnfKeyConstants.MARKER_SUPPORT_ID, "0815");
 		}
 
 	}
 
 	/**
 	 * A simple look and feel theme thats overwrites settings of the
 	 * {@code DummyTheme}.
 	 */
 	private static class DummyTheme2 extends DummyTheme {
 
 		@Override
 		public void customizeLnf(final ILnfCustomizer customizer) {
 			super.customizeLnf(customizer);
 			customizer.putLnfSetting(LnfKeyConstants.MARKER_SUPPORT_ID, "borderMarkerSupport");
 		}
 
 	}
 
 }
