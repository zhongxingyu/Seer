 /*******************************************************************************
  * Copyright (c) 2008, 2010 VMware Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   VMware Inc. - initial contribution
  *******************************************************************************/
 
 package org.eclipse.virgo.kernel.smoketest;
 
 import static org.junit.Assert.assertEquals;
 import org.junit.Test;
 
 public class KernelStartupAndShutdownTests extends AbstractKernelTests {
 
 	@Test
 	public void testKernelStartUpStatus() throws Exception {
 		new Thread(new KernelStartUpThread()).start();
 		AbstractKernelTests.waitForKernelStartFully();
 		assertEquals(STATUS_STARTED, getKernelStartUpStatus());
 	}
 
 	@Test
 	public void testKernelShutdownStatus() throws Exception {
 		new Thread(new KernelShutdownThread()).start();
 		AbstractKernelTests.waitForKernelShutdownFully();
 	}
 
 }
