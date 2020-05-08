 package com.ns.app;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 
import org.apache.commons.lang3.StringUtils;
 import org.apache.commons.lang3.Validate;
 
 /**
  * Launches external bash shell scripts.
  */
 final class Launcher {
 
 	/**
 	 * Consumes a stream. For any Process, the input and error streams must read
 	 * even if the data written to these streams is not used by the application.
 	 * The generally accepted solution for this problem is a stream gobbler
 	 * thread that does nothing but consume data from an input stream until
 	 * stopped.
 	 */
 	private static class StreamGobbler extends Thread {
 		final InputStream is;
 
 		public StreamGobbler(final InputStream is) {
 			Validate.notNull(is, "The input stream must not be null");
 			this.is = is;
 		}
 
 		public void run() {
 			try {
 				final InputStreamReader isr = new InputStreamReader(is);
 				final BufferedReader br = new BufferedReader(isr);
 				while (br.readLine() != null) {
 				}
 			} catch (IOException ioe) {
 				ioe.printStackTrace();
 			}
 		}
 	}
 
 	/**
 	 * True if the current operating system is windows based.
 	 * 
 	 * @return true if the current operating system is windows based
 	 */
 	private static boolean isWindows() {
 		String os = System.getProperty("os.name").toLowerCase();
 		return (os.indexOf("win") >= 0);
 	}
 
 	/**
 	 * Executes a bash script given a path, and can optionally wait for it to
 	 * complete. The output and error streams are discarded.
 	 * 
 	 * @param script
 	 *            file object containing the path of the script to execute
 	 * @param scriptArgs
 	 *            arguments that will be passed to the script. If there are no
 	 *            required arguments then this parameter should be null.
 	 * @param wait
 	 *            true if the method should wait until the script has finished
 	 *            executing before returning.
 	 */
 	public static void launchBashScript(final File bashScript,
 			final List<String> scriptArgs, final boolean wait) {
 		try {
 			Validate.notNull(bashScript, "The script must not be null");
 
 			final List<String> cmdList = new ArrayList<String>();
 			if (isWindows()) {
 				cmdList.add("cmd.exe");
 				cmdList.add("/c");
 				cmdList.add("start");
 				cmdList.add("bash.exe");
 				cmdList.add(bashScript.getAbsolutePath());
				if (scriptArgs != null) {
					cmdList.addAll(scriptArgs);
				}
 			} else {
 				cmdList.add("/bin/bash");
 				cmdList.add("-c");
				if (scriptArgs != null) {
					cmdList.add(bashScript.getAbsolutePath() + " "
							+ StringUtils.join(scriptArgs.iterator(), " "));
				} else {
					cmdList.add(bashScript.getAbsolutePath());
				}
 			}
 			launch(cmdList, wait);
 
 		} catch (Exception e) {
 			System.out.println(e);
 		}
 	}
 
 	/**
 	 * Executes the specified command and arguments in a separate process, and
 	 * can optionally wait for it to complete. The output and error streams are
 	 * discarded.
 	 * 
 	 * @param cmdList
 	 *            list containing the command to call and its arguments.
 	 * @param wait
 	 *            true if the method should wait until the script has finished
 	 *            executing before returning.
 	 */
 	public static void launch(final List<String> cmdList, final boolean wait)
 			throws IOException, InterruptedException {
 		Validate.notNull(cmdList, "The script must not be null");
 
 		final Process process = Runtime.getRuntime().exec(
 				cmdList.toArray(new String[0]));
 		new StreamGobbler(process.getErrorStream()).start();
 		new StreamGobbler(process.getInputStream()).start();
 
 		if (wait) {
 			process.waitFor();
 		}
 	}
 }
