 /*******************************************************************************
  * Copyright (c) 2004, 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - Initial API and implementation
  *******************************************************************************/
 package org.eclipse.wst.server.core.tests;
 
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.wst.server.core.IModuleType;
 import org.eclipse.wst.server.core.ServerUtil;
 import org.eclipse.wst.server.core.internal.ResourceManager;
 
 public class ServerUtilTestCase extends TestCase {
 	public static Test suite() {
 		return new TestSuite(ServerUtilTestCase.class, "ServerUtilTestCase");
 	}
 	
 	public void testFindServer0Extension() throws Exception {
 		try {
 			ResourceManager.findServer(null);
 			assertTrue("Should throw exception", false);
 		} catch (Exception e) {
 			// ignore
 		}
 	}
 
 	public void testFindServer1Extension() throws Exception {
 		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path("missingproject/test"));
 		assertTrue(ResourceManager.findServer(file) == null);
 	}
 
 	public void testGetModule1() throws Exception {
 		try {
 			ServerUtil.getModule((String)null);
 			assertTrue("Should throw exception", false);
 		} catch (Exception e) {
 			// ignore
 		}
 	}
 
 	public void testGetModule2() throws Exception {
 		ServerUtil.getModule("x");
 	}
 
 	public void testGetModules0() throws Exception {
 		ServerUtil.getModules((IModuleType[]) null);
 	}
 
 	public void testGetModules1() throws Exception {
		ServerUtil.getModule((IProject) null);
 	}
 
 	public void testGetModules2() throws Exception {
 		ServerUtil.getModules((String) null);
 	}
 	
 	public void testIsSupportedModule0() throws Exception {
 		try {
 			ServerUtil.isSupportedModule((IModuleType) null, null);
 			assertTrue("Should throw exception", false);
 		} catch (Exception e) {
 			// ignore
 		}
 	}
 	
 	public void testIsSupportedModule1() throws Exception {
 		ServerUtil.isSupportedModule((IModuleType[]) null, null);
 	}
 	
 	public void testIsSupportedModule2() throws Exception {
 		ServerUtil.isSupportedModule(null, null, null);
 	}
 	
 	public void testModifyModules() throws Exception {
 		try {
 			ServerUtil.modifyModules(null, null, null, null);
 			assertTrue("Should throw exception", false);
 		} catch (Exception e) {
 			// ignore
 		}
 	}
 	
 	public void testSetServerDefaultName() throws Exception {
 		try {
 			ServerUtil.setServerDefaultName(null);
 			assertTrue("Should throw exception", false);
 		} catch (Exception e) {
 			// ignore
 		}
 	}
 	
 	public void testGetUnusedServerFile() throws Exception {
 		try {
 			ServerUtil.getUnusedServerFile(null, null);
 			assertTrue("Should throw exception", false);
 		} catch (Exception e) {
 			// ignore
 		}
 	}
 	
 	public void testGetRuntimes() throws Exception {
 		ServerUtil.getRuntimes(null, null);
 	}
 	
 	public void testGetRuntimeTypes() throws Exception {
 		ServerUtil.getRuntimeTypes(null, null, null);
 	}
 	
 	public void testGetAvailableServersForModule() throws Exception {
 		ServerUtil.getAvailableServersForModule(null, false, null);
 	}
 	
 	public void testGetServersByModule() throws Exception {
 		ServerUtil.getServersByModule(null, null);
 	}
 	
 	public void testContainsModule() throws Exception {
 		ServerUtil.containsModule(null, null, null);
 	}
 	
 	public void testGetServer() throws Exception {
 		try {
 			ServerUtil.getServer(null);
 		} catch (Exception e) {
 			// ignore
 		}
 	}
 	
 	public void testValidateEdit() throws Exception {
 		try {
 			ServerUtil.validateEdit(null, null);
 		} catch (Exception e) {
 			// ignore
 		}
 	}
 }
