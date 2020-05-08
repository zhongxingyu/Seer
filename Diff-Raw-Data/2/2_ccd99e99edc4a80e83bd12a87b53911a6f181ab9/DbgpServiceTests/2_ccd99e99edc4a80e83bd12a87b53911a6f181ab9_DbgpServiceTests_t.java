 /*******************************************************************************
  * Copyright (c) 2008 xored software, Inc.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     xored software, Inc. - initial API and Implementation (Alex Panchenko)
  *******************************************************************************/
 package org.eclipse.dltk.debug.dbgp.tests.service;
 
 import java.io.IOException;
 
 import org.eclipse.dltk.dbgp.DbgpServer;
 import org.eclipse.dltk.internal.debug.core.model.DbgpService;
 
 public class DbgpServiceTests extends AbstractDbgpServiceTests {
 
 	public void testConnect() throws IOException {
 		DbgpService service = createService(ANY_PORT);
 		try {
 			connect(service.getPort());
 		} finally {
 			service.shutdown();
 		}
 	}
 
 	public void testConnectDelayed() throws IOException {
 		DbgpService service = new DbgpService(ANY_PORT) {
 
 			protected DbgpServer createServer(int port) {
 				return new DbgpServer(port, CLIENT_SOCKET_TIMEOUT) {
 
 					protected void workingCycle() throws Exception, IOException {
 						Thread.sleep(1000);
 						super.workingCycle();
 					}
 
 				};
 			}
 		};
 		assertTrue(service.waitStarted());
 		try {
 			connect(service.getPort());
 		} finally {
 			service.shutdown();
 		}
 	}
 
 	public void testShutdown() throws IOException {
 		final int port = findAvailablePort(MIN_PORT, MAX_PORT);
 		for (int i = 0; i < 4; ++i) {
 			final DbgpService service = createService(port);
 			try {
 				connect(port);
 			} finally {
 				service.shutdown();
 			}
 			try {
 				connect(port);
 				fail("Should fail - the service is shutted down");
 			} catch (IOException e) {
 				// ignore
 			}
 		}
 	}
 
	public void _testRestart() throws IOException {
 		int port1 = findAvailablePort(MIN_PORT, MAX_PORT);
 		int port2 = findAvailablePort(port1 + 1, MAX_PORT);
 		for (int i = 0; i < 4; ++i) {
 			final DbgpService service = createService(port1);
 			try {
 				assertEquals(port1, service.getPort());
 				connect(port1);
 				try {
 					connect(port2);
 					fail("Should fail - the service is listen on port1 now");
 				} catch (IOException e) {
 					// ignore
 				}
 				service.restart(port2);
 				try {
 					connect(port1);
 					fail("Should fail - the service is listen on port2 now");
 				} catch (IOException e) {
 					// ignore
 				}
 				connect(port2);
 			} finally {
 				service.shutdown();
 			}
 		}
 	}
 
 }
