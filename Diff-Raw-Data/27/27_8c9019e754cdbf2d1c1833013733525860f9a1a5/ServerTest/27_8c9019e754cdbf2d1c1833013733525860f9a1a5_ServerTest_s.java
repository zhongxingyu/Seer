 /*--------------------------------------------------------------------------
  *  Copyright 2008 utgenome.org
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *--------------------------------------------------------------------------*/
 //--------------------------------------
 // utgb-shell Project
 //
 // ServerTest.java
 // Since: May 7, 2009
 //
 // $URL$ 
 // $Author$
 //--------------------------------------
 package org.utgenome.shell;
 
 import static org.junit.Assert.*;
 
 import java.io.File;
 import java.io.InputStream;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.utgenome.shell.ProjectGenerator.ProjectInfo;
 import org.xerial.util.log.Logger;
 
 public class ServerTest {
 
 	private static Logger _logger = Logger.getLogger(ServerTest.class);
 	private static ProjectInfo temporatyProject;
 
 	@BeforeClass
 	public static void setUp() throws Exception {
 
 		temporatyProject = ProjectGenerator.createTemporatyProject();
 
 	}
 
 	@AfterClass
 	public static void tearUp() {
 		rmdir(new File(temporatyProject.projectRoot));
 	}
 
 	static boolean rmdir(File path) {
 		if (path.exists()) {
 			for (File each : path.listFiles()) {
 				if (each.isDirectory())
 					rmdir(each);
 				else {
 					each.delete();
 				}
 			}
 		}
 
 		return path.delete();
 	}
 
 	@Test
 	public void compile() throws Exception {
 		// UTGBShell.main(new String[] { "gwt", "-d", new File(tmpDir, appName).getAbsolutePath() });
 
 		ClassLoader cl = Thread.currentThread().getContextClassLoader();
 
 		UTGBShell.runCommand(new String[] { "action", "-d", temporatyProject.projectRoot, "hello" });
 		UTGBShell.runCommand(new String[] { "compile", "-d", temporatyProject.projectRoot });
 	}
 
 	@Test
 	public void server() throws Exception {
 		// UTGBShell.main(new String[] { "gwt", "-d", new File(tmpDir, appName).getAbsolutePath() });
 
 		ClassLoader cl = Thread.currentThread().getContextClassLoader();
 
 		UTGBShell.runCommand(new String[] { "action", "-d", temporatyProject.projectRoot, "hello" });
 		UTGBShell.runCommand(new String[] { "compile", "-d", temporatyProject.projectRoot });
 
 		ExecutorService es = Executors.newFixedThreadPool(1);
 		es.submit(new Callable<Void>() {
 			public Void call() throws Exception {
 				UTGBShell.runCommand(new String[] { "server", "-d", temporatyProject.projectRoot });
 				return null;
 			}
 		});
 
 		URL serverPage = new URL("http://localhost:8989/" + temporatyProject.appName + "/utgb-core/roundcircle");
		URL actionPage = new URL("http://localhost:8989/" + temporatyProject.appName + "/utgb-core/hello");
 
 		Thread.sleep(8 * 1000);
 
 		{
 			URLConnection conn = serverPage.openConnection();
 			String ct = conn.getContentType();
 
 			_logger.debug("content-type: " + ct);
 			assertEquals("image/png", ct);
 		}
 
 		{
 			URLConnection conn2 = actionPage.openConnection();
 			String ct2 = conn2.getContentType();
 			_logger.debug("content-type: " + ct2);
 		}
 
 		es.shutdownNow();
 		es.awaitTermination(1, TimeUnit.SECONDS);
 
 		Thread.sleep(2 * 1000);
 	}
 
 	@Test
 	public void envSwitchTest() throws Exception {
 		// UTGBShell.main(new String[] { "gwt", "-d", new File(tmpDir, appName).getAbsolutePath() });
 
 		final String projectPath = temporatyProject.projectRoot;
 
 		UTGBShell.runCommand(new String[] { "compile", "-d", projectPath });
 
 		ExecutorService es = Executors.newFixedThreadPool(1);
 		es.submit(new Callable<Void>() {
 			public Void call() throws Exception {
 				UTGBShell.runCommand(new String[] { "server", "-d", projectPath, "-e", "test" });
 				return null;
 			}
 		});
 
 		Thread.sleep(8 * 1000);
 
 		es.shutdownNow();
 		es.awaitTermination(1, TimeUnit.SECONDS);
 
 		Thread.sleep(2 * 1000);
 	}
 
 }
