 /**
  * 
  */
 package com.ns.app;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.io.File;
 import java.io.UnsupportedEncodingException;
 import java.net.URL;
 import java.net.URLDecoder;
 
 import junit.framework.TestCase;
 
 /**
  * A simple regression test for the Launcher class.
  */
 public class LauncherTest extends TestCase {
 
 	public void testExecShellScript() {
 
 		// Find the required resources.
 		final File scriptFile;
 		final File outputFile;
 		try {
 			final URL url = this.getClass().getResource("/test-echo.sh");
 			scriptFile = new File(URLDecoder.decode(url.getFile(), "UTF-8"));
 			outputFile = new File(scriptFile.getParent().concat(
 					"/test-echo-output.txt"));
 		} catch (UnsupportedEncodingException e) {
 			throw new RuntimeException(e);
 		}
 
 		// Remove the output file.
		assertTrue(outputFile.exists());
 		if (outputFile.exists()) {
 			outputFile.delete();
 		}
 		assertTrue(!outputFile.exists());
 
 		// Execute a bash script that will create the output file.
 		final List<String> args = new ArrayList<String>(1);
 		args.add(outputFile.getAbsolutePath());
 		Launcher.launchBashScript(scriptFile, args, true);
 
 		// Make sure the output file has been created successfully.
 		try {
 			// HACK! This test relies on the scheduler granting enough time to
 			// refresh the output directory.
 			Thread.sleep(1000);
 		} catch (InterruptedException e) {
 			throw new RuntimeException(e);
 		}
 		assertTrue(outputFile.exists());
 	}
 }
