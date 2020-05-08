 /*******************************************************************************
  * Copyright (c) 2007, 2011 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.core;
 
 import org.eclipse.core.runtime.CoreException;
 
 import org.eclipse.riena.core.util.VariableManagerUtil;
 import org.eclipse.riena.internal.core.test.RienaTestCase;
 import org.eclipse.riena.internal.core.test.collect.NonUITestCase;
 
 /**
  *
  */
 @NonUITestCase
 public class RienaStatusTest extends RienaTestCase {
 
 	public void testIsDevelopment() {
 		final String savedValue = System.getProperty(RienaStatus.RIENA_DEVELOPMENT_SYSTEM_PROPERTY);
 		try {
 			System.clearProperty(RienaStatus.RIENA_DEVELOPMENT_SYSTEM_PROPERTY);

			// Unfortunately this can not be tested because the default value will be retrieved on RienaStatus class load time!
			//
			//			System.setProperty("osgi.dev", "");
			//			assertTrue(RienaStatus.isDevelopment());
			//
			//			System.clearProperty("osgi.dev");
			//			assertFalse(RienaStatus.isDevelopment());

 			System.setProperty(RienaStatus.RIENA_DEVELOPMENT_SYSTEM_PROPERTY, "false");
 			assertFalse(RienaStatus.isDevelopment());

 			System.setProperty(RienaStatus.RIENA_DEVELOPMENT_SYSTEM_PROPERTY, "true");
 			assertTrue(RienaStatus.isDevelopment());

 		} finally {
 			if (savedValue != null) {
 				System.setProperty(RienaStatus.RIENA_DEVELOPMENT_SYSTEM_PROPERTY, savedValue);
 			}
 		}
 	}
 
 	public void testGetStage() throws CoreException {
 		VariableManagerUtil.addVariable("riena.stage", "Test");
 		assertEquals("Test", RienaStatus.getStage());
 
 		VariableManagerUtil.removeVariable("riena.stage");
 		assertEquals(RienaStatus.UNKNOWN_STAGE, RienaStatus.getStage());
 	}
 }
