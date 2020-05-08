 /*******************************************************************************
  * Copyright (c) 2013 EclipseSource Muenchen GmbH.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  ******************************************************************************/
 package org.eclipse.emf.emfstore.client.test.api;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.fail;
 
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.emf.emfstore.client.ESLocalProject;
 import org.eclipse.emf.emfstore.client.ESServer;
 import org.eclipse.emf.emfstore.client.ESWorkspace;
 import org.eclipse.emf.emfstore.client.ESWorkspaceProvider;
 import org.eclipse.emf.emfstore.client.exceptions.ESServerNotFoundException;
 import org.eclipse.emf.emfstore.internal.client.model.connectionmanager.KeyStoreManager;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 public class WorkspaceTest {
 
 	private static ESWorkspace workspace;
 	private ESLocalProject localProject;
 
 	public static int port = 8080;
 
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception {
 		workspace = ESWorkspaceProvider.INSTANCE.getWorkspace();
 	}
 
 	@Before
 	public void setUp() throws Exception {
 		assertEquals(0, workspace.getLocalProjects().size());
 		localProject = workspace.createLocalProject("TestProject");
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		for (ESLocalProject lp : workspace.getLocalProjects())
 			lp.delete(new NullProgressMonitor());
 	}
 
 	@Test
 	public void testCreateLocalProject() {
 		assertNotNull(localProject);
 		assertEquals(1, workspace.getLocalProjects().size());
 		workspace.createLocalProject("TestProject2");
 		assertEquals(2, workspace.getLocalProjects().size());
 	}
 
 	@Test
 	public void testAddServer() {
 		int servers = workspace.getServers().size();
 		ESServer server = ESServer.FACTORY.createServer("foo.net", 1234, KeyStoreManager.DEFAULT_CERTIFICATE);
 		workspace.addServer(server);
 		assertEquals(servers + 1, workspace.getServers().size());
 		try {
 			workspace.removeServer(server);
 		} catch (ESServerNotFoundException e) {
 			fail(e.getMessage());
 		}
 		assertEquals(servers, workspace.getServers().size());
 	}
 
 	@Test(expected = ESServerNotFoundException.class)
 	public void testRemoveNotExistingServer() throws ESServerNotFoundException {
 		workspace.removeServer(
 			ESServer.FACTORY.createServer("foo.net", 1234, KeyStoreManager.DEFAULT_CERTIFICATE));
 	}
 }
