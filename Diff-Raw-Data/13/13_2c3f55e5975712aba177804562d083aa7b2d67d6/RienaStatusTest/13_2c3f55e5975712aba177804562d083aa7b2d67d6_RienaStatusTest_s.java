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
 package org.eclipse.riena.core;
 
 import org.eclipse.riena.tests.RienaTestCase;
 import org.eclipse.riena.tests.collect.NonUITestCase;
 
 /**
  *
  */
 @NonUITestCase
 public class RienaStatusTest extends RienaTestCase {
 
 	public void testIsDevelopment() {
 		String savedValue = System.getProperty(RienaStatus.RIENA_DEVELOPMENT_SYSTEM_PROPERTY);
 		try {
 			System.clearProperty(RienaStatus.RIENA_DEVELOPMENT_SYSTEM_PROPERTY);
 			assertTrue(RienaStatus.isDevelopment());
 			System.setProperty(RienaStatus.RIENA_DEVELOPMENT_SYSTEM_PROPERTY, "false");
 			assertFalse(RienaStatus.isDevelopment());
 			System.setProperty(RienaStatus.RIENA_DEVELOPMENT_SYSTEM_PROPERTY, "true");
 			assertTrue(RienaStatus.isDevelopment());
 		} finally {
			System.setProperty(RienaStatus.RIENA_DEVELOPMENT_SYSTEM_PROPERTY, savedValue);
 		}
 	}
 }
