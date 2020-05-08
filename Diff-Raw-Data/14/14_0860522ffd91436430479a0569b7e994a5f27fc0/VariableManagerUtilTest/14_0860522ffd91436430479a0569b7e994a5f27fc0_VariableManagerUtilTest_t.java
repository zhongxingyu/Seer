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
 package org.eclipse.riena.core.util;
 
 import java.lang.reflect.Field;
 
 import org.eclipse.core.internal.variables.StringVariableManager;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.riena.tests.RienaTestCase;
 
 /**
  * Test the <code>ConfigSymbolReplace</code> class.
  */
 public class VariableManagerUtilTest extends RienaTestCase {
 
 	private static Field fgManager;
 	private static final String REF_KEY = "ref";
 	private static final String WWW_ECLIPSE_ORG = "www.eclipse.org";
 
 	static {
 		try {
 			fgManager = StringVariableManager.class.getDeclaredField("fgManager");
 			fgManager.setAccessible(true);
 		} catch (Throwable e) {
 			fail("Could not access field fgManager from StringVariableManager!");
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.riena.tests.RienaTestCase#setUp()
 	 */
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		// Need every time a fresh manager
 		fgManager.set(null, null);
 	}
 
 	public void testUpdatedWrongKey() throws CoreException {
 		VariableManagerUtil.addVariable("host", "${host");
 		assertEquals("${host", VariableManagerUtil.substitute("${host}"));
 	}
 
 	public void testUpdatedRecursiveKeyDirectly() throws CoreException {
 		try {
 			VariableManagerUtil.addVariable("host", "${host}");
 			VariableManagerUtil.substitute("${host}");
 			fail();
 		} catch (CoreException e) {
 			assertTrue(e.getMessage().contains("host"));
 		}
 	}
 
 	public void testUpdatedRecursiveKeyIndirectly1() throws CoreException {
 		try {
 			VariableManagerUtil.addVariable("a", "${b}");
 			VariableManagerUtil.addVariable("b", "${a}");
 			VariableManagerUtil.substitute("${a}");
 			fail();
 		} catch (CoreException e) {
			String problemVariableList = e.getMessage().substring(e.getMessage().indexOf(",") - 1,
					e.getMessage().indexOf(",") + 3);
			assertTrue(problemVariableList.contains("a"));
			assertTrue(problemVariableList.contains("b"));
 		}
 	}
 
 	public void testUpdatedRecursiveKeyIndirectly2() throws CoreException {
 		try {
 			VariableManagerUtil.addVariable("a", "${b}");
 			VariableManagerUtil.addVariable("b", "${c}");
 			VariableManagerUtil.addVariable("c", "${a}");
 			VariableManagerUtil.substitute("${a}");
 			fail();
 		} catch (CoreException e) {
			String problemVariableList = e.getMessage().substring(e.getMessage().indexOf(",") - 1,
					e.getMessage().indexOf(",") + 6);
			assertTrue(problemVariableList.contains("a"));
			assertTrue(problemVariableList.contains("b"));
			assertTrue(problemVariableList.contains("c"));
 		}
 	}
 
 	public void testModifyWrongKey() throws CoreException {
 		VariableManagerUtil.addVariable("host", WWW_ECLIPSE_ORG);
 		assertEquals("${host", VariableManagerUtil.substitute("${host"));
 	}
 
 	public void testModifyEmptyKey() throws CoreException {
 		VariableManagerUtil.addVariable("", WWW_ECLIPSE_ORG);
 		assertEquals(WWW_ECLIPSE_ORG, VariableManagerUtil.substitute("${}"));
 	}
 
 	public void testModifySingle() throws CoreException {
 		VariableManagerUtil.addVariable("host", WWW_ECLIPSE_ORG);
 		assertEquals(WWW_ECLIPSE_ORG, VariableManagerUtil.substitute("${host}"));
 	}
 
 	public void testModifyDoubleSequentialy() throws CoreException {
 		VariableManagerUtil.addVariable("host", WWW_ECLIPSE_ORG);
 		assertEquals(WWW_ECLIPSE_ORG + WWW_ECLIPSE_ORG, VariableManagerUtil.substitute("${host}${host}"));
 	}
 
 	public void testModifyDoubleNested() throws CoreException {
 		VariableManagerUtil.addVariable("url", "http://${host}/path");
 		VariableManagerUtil.addVariable("host", WWW_ECLIPSE_ORG);
 		assertEquals("http://" + WWW_ECLIPSE_ORG + "/path", VariableManagerUtil.substitute("${url}"));
 	}
 }
