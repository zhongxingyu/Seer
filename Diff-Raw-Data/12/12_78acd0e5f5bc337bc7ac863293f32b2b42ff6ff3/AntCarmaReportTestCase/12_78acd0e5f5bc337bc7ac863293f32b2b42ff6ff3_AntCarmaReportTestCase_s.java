 /**
  *
  *   Copyright Alexander Rau, Mike Groezinger, Retroduction.org - All rights reserved
  *
  *   This file is part of Carma. Carma is licensed under the OSL 3.0. The OSL 3.0 is
  *   available here: http://www.opensource.org/licenses/osl-3.0.php
  *
  */
 package com.retroduction.carma.ant;
 
 import java.io.File;
 import java.io.IOException;
 import java.security.Permission;
 
 import junit.framework.TestCase;
 import junitx.framework.FileAssert;
 
 import org.apache.tools.ant.Main;
 
 public class AntCarmaReportTestCase extends TestCase {
 
 	private SecurityManager originalSecManager;
 
 	@Override
 	public void setUp() {
 
 		this.originalSecManager = System.getSecurityManager();
 		System.setSecurityManager(new NoSystemExitSecurityManager());
 	}
 
 	@Override
 	public void tearDown() {
 		System.setSecurityManager(this.originalSecManager);
 	}
 
 	class NoSystemExitSecurityManager extends SecurityManager {
 
 		@Override
 		public void checkPermission(Permission perm) {
 
 			if (perm.implies(new RuntimePermission("setSecurityManager"))) {
 				return;
 			}
 
 		}
 
 		@Override
 		public void checkExit(int arg0) {
 			throw new SecurityException("Muahahahaaa... Noob! You aren't allowed to terminate VM");
 		}
 
 	}
	
 
 
 	public void test_ProjectUnderTestWithNoDependencies_CarmaTestAndReportAntRun() throws IOException {
 
 		File file = new File("target/report.xml");
 
 		if (file.exists()) {
 			file.delete();
 		}
 
 		assertTrue(file.getCanonicalPath() + " could not be cleanup before execution", !file.exists());
 
 		try {
 			Main.main(new String[] { "-v", "-f", "src/test/it/it0001/build.xml", "all" });
 		} catch (SecurityException e) {
 		}
 
 		assertTrue(file.getCanonicalPath() + " file has not been written", file.exists());
 
		FileAssert.assertEquals("Report.xml content unexpected", new File("src/test/it/it0001/target/report.xml"),
 				new File("target/report.xml"));
 	}
 
 }
