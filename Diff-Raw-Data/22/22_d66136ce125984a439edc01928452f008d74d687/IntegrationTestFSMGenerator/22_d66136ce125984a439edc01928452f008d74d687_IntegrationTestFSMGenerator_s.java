 /*******************************************************************************
  * Copyright (c) 2010 protos software gmbh (http://www.protos.de).
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * CONTRIBUTORS:
  * 		Thomas Schuetz and Henrik Rentz-Reichert (initial contribution)
  * 
  *******************************************************************************/
 
 package org.eclipse.etrice.integration.tests;
 
 
 import static org.junit.Assert.assertEquals;
 
 import org.eclipse.emf.mwe2.launch.runtime.Mwe2Launcher;
 import org.eclipse.etrice.integration.tests.SubSystemHFSMTest;
 import org.eclipse.etrice.integration.tests.a_HFSM_Tester;
 import org.eclipse.etrice.runtime.java.messaging.RTServices;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.IncrementalProjectBuilder;
 import org.eclipse.core.resources.ResourcesPlugin;
 
 
 public class IntegrationTestFSMGenerator {
 
 	@Before
 	public void setUp() throws Exception {
 		// we have to launch a JUnit Plugin test since for the build we need an Eclipse environment
 		// in this context the Mwe2Launcher suffers from https://bugs.eclipse.org/bugs/show_bug.cgi?id=318721
 		/*
 		Mwe2Launcher.main(new String[]{"/org.eclipse.etrice.integration.tests/src/de/protos/etrice/integration/test/IntegrationTestFSMGenerator.mwe2"});
 		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
         final IProject project = workspace.getRoot().getProject("/org.eclipse.etrice.integration.tests");
 		project.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
 		*/
 	}
 	
 	@Test(timeout=1000)
 	public void testHFSM(){
 		SubSystemHFSMTest main_component = new SubSystemHFSMTest(null,"MainComponent");
 		main_component.init(); // lifecycle init
 		main_component.start(); // lifecycle start
 		
 		RTServices.getInstance().getMsgSvcCtrl().waitTerminate();
 		
		// TODOTS: Test broken -> repair after changes for multi threading
//		main_component.waitTerminate();
		
 		assertEquals(a_HFSM_Tester.STATE_TestPass ,main_component.getInstance("/MainComponent/application/HFSM_Tests/Tester").getState());
 		
 		// end the lifecycle
 		main_component.stop(); // lifecycle stop
 		main_component.destroy(); // lifecycle destroy
 	}
 
 	@After
 	public void tearDown() throws Exception {
 	}
 
 	
 }
