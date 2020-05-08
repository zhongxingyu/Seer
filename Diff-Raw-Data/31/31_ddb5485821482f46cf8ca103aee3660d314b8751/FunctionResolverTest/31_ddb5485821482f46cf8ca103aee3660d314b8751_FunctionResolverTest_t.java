 /*******************************************************************************
  * Copyright (c) 2007, 2013 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.core.util;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 
 import org.eclipse.core.runtime.CoreException;
 
 import org.eclipse.riena.internal.core.test.RienaTestCase;
 import org.eclipse.riena.internal.core.test.collect.NonUITestCase;
 
 /**
  * Test the {@code FunctionResolver}.
  */
 @NonUITestCase
 public class FunctionResolverTest extends RienaTestCase {
 
 	public void testResolveNoFunction() throws CoreException, IOException {
 		try {
 			VariableManagerUtil.substitute("${fn:}"); //$NON-NLS-1$
 			fail();
 		} catch (final CoreException e) {
 			ok();
 		}
 	}
 
 	public void testResolveUnknownFunction() throws CoreException, IOException {
 		try {
 			VariableManagerUtil.substitute("${fn:LetsHaveFun,vale}"); //$NON-NLS-1$
 			fail();
 		} catch (final CoreException e) {
 			ok();
 		}
 	}
 
 	public void testResolveToFile() throws CoreException, IOException {
 		final String property = System.getProperty("osgi.instance.area"); //$NON-NLS-1$
 		final URL url = new URL(property);
 		final String expected = new File(url.getPath()).getCanonicalPath().replace('\\', '/');
		assertEquals(expected, VariableManagerUtil.substitute("${fn:toFile,${java.system.property:osgi.instance.area}}")); //$NON-NLS-1$
	}

	public void testResolveUNCToFile() throws CoreException, IOException {
		System.setProperty("uncFileName", "file://RUV/PGL/ZI/SE/PUMA"); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("//RUV/PGL/ZI/SE/PUMA", VariableManagerUtil.substitute("${fn:toFile,${java.system.property:uncFileName}}")); //$NON-NLS-1$ //$NON-NLS-2$
 	}
 }
