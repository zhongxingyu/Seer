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
 package org.eclipse.riena.navigation.ui.swt.lnf;
 
 import junit.framework.TestCase;
 
 import org.osgi.framework.Bundle;
 import org.osgi.framework.FrameworkUtil;
 
 import org.eclipse.riena.internal.core.test.collect.NonUITestCase;
 import org.eclipse.riena.ui.swt.lnf.LnfKeyConstants;
 import org.eclipse.riena.ui.swt.lnf.LnfManager;
 import org.eclipse.riena.ui.swt.lnf.rienadefault.RienaDefaultLnf;
 
 /**
  * Tests of the class <code>LnfManager</code>.
  */
 @NonUITestCase
 public class LnfManagerTest extends TestCase {
 
 	// save & restore the current Look and Feel
 	private RienaDefaultLnf oldLnf;
 
 	@Override
 	protected void setUp() throws Exception {
 		oldLnf = LnfManager.getLnf();
 	}
 
 	@Override
 	protected void tearDown() throws Exception {
 		LnfManager.setLnf(oldLnf);
 	}
 
 	/**
 	 * Test of the method <code>getLnfClassName()</code>.
 	 */
 	public void testGetLnfClassNameAndGetLnfUnderDifferentConfigurations() {
 		// check for riena default L&F
 		assertEquals(RienaDefaultLnf.class.getName(), LnfManager.getLnfClassName());
 		assertEquals(RienaDefaultLnf.class, LnfManager.getLnf().getClass());
 
 		// check for some other default L&F
 		RienaDefaultLnf otherDefaultLnf = new OtherDefaultTestLnf();
 		LnfManager.setDefaultLnf(otherDefaultLnf);
 
 		assertEquals(otherDefaultLnf.getClass().getName(), LnfManager.getLnfClassName());
 		assertEquals(otherDefaultLnf.getClass(), LnfManager.getLnf().getClass());
 
 		// check for non-default L&F set
 		LnfManager.setLnf(FrameworkUtil.getBundle(LnfManagerTest.class).getSymbolicName() + ":"
 				+ TestLnf1.class.getName());
 
 		assertEquals(TestLnf1.class.getName(), LnfManager.getLnfClassName());
 		assertEquals(TestLnf1.class, LnfManager.getLnf().getClass());
 
 		// check for clearing non-default L&F
 		LnfManager.setLnf((String) null);
 
 		assertEquals(otherDefaultLnf.getClass().getName(), LnfManager.getLnfClassName());
 		assertEquals(otherDefaultLnf.getClass(), LnfManager.getLnf().getClass());
 	}
 
 	public void testGetLnfClassNameAndGetLnfWithSystemProperty() {
 		LnfManager.setLnf((RienaDefaultLnf) null);
 
 		Bundle bundle = FrameworkUtil.getBundle(LnfManagerTest.class);
 
 		assertNotNull(bundle);
 
 		try {
 			// check for system property L&F setting
 			System.setProperty("riena.lnf", bundle.getSymbolicName() + ":" + TestLnf1.class.getName());
 
 			assertEquals(TestLnf1.class.getName(), LnfManager.getLnfClassName());
 			assertEquals(TestLnf1.class, LnfManager.getLnf().getClass());
 
 			// check for non-override of system property
 			LnfManager.setLnf(new TestLnf2());
 
 			assertEquals(TestLnf2.class.getName(), LnfManager.getLnfClassName());
 			assertEquals(TestLnf2.class, LnfManager.getLnf().getClass());
 		} finally {
 			System.clearProperty("riena.lnf");
 		}
 	}
 
 	/**
 	 * Test of the method <code>dispose()</code>.
 	 */
 	public void testDispose() {
 		RienaDefaultLnf myLnf = new RienaDefaultLnf();
 		LnfManager.setLnf(myLnf);
 		myLnf.initialize();
 
		assertNotNull(myLnf.getFont(LnfKeyConstants.EMBEDDED_TITLEBAR_FONT));
 
 		LnfManager.dispose();
 
		assertNull(myLnf.getFont(LnfKeyConstants.EMBEDDED_TITLEBAR_FONT));
 	}
 
 	public static class TestLnf1 extends RienaDefaultLnf {
 	};
 
 	public static class TestLnf2 extends RienaDefaultLnf {
 	};
 
 	public static class OtherDefaultTestLnf extends RienaDefaultLnf {
 	};
 }
