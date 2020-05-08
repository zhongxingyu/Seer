 package cytoscape.launcher;
 
 
 import java.io.BufferedInputStream;
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Properties;
 import javax.swing.JOptionPane;
 
 
 public class CytoscapeLauncher {
 	final static String VMCONFIG_FILENAME = "cytoscape.vmconfig";
 
 
 	static MemSettings getMemSettings() {
 		final Properties props = new Properties();
 		try {
 			props.load(new FileInputStream(VMCONFIG_FILENAME));
 		} catch (final java.io.IOException e) {
 			System.err.println("Failed to load properties from \"" + VMCONFIG_FILENAME + "\"!");
 			System.exit(-1);
 		}
 
 		final String threadStackSize = props.getProperty("threadStackSize");
 		if (threadStackSize == null) {
 			System.err.println("Can't find \"threadStackSize\" property in \"" + VMCONFIG_FILENAME + "\"!");
 			System.exit(-1);
 		}
 
 		final String memoryAllocationPoolMaximumSize = props.getProperty("memoryAllocationPoolMaximumSize");
 		if (memoryAllocationPoolMaximumSize == null) {
 			System.err.println("Can't find \"memoryAllocationPoolMaximumSize\" property in \"" + VMCONFIG_FILENAME + "\"!");
 			System.exit(-1);
 		}
 
 		return new MemSettings(threadStackSize, memoryAllocationPoolMaximumSize);
 	}
 
 
 	static public void main(final String args[]) {
 		final boolean verbose = false;
 
 		final MemSettings memSettings = getMemSettings();
 
 		final ArrayList<String> execArgs = new ArrayList<String>();
 		execArgs.add("java");
 		execArgs.add("-d64");
 		execArgs.add("-Dswing.aatext=true");
 		execArgs.add("-Dawt.useSystemAAFontSettings=lcd");
 		execArgs.add("-Xss" + memSettings.getThreadStackSize());
 		execArgs.add("-Xmx" + memSettings.getMemoryAllocationPoolMaximumSize());
 		execArgs.add("-cp");
 		execArgs.add("cytoscape.jar");
 		execArgs.add("cytoscape.CyMain");
 		execArgs.add("-p");
 		execArgs.add("plugins");
 		for (final String arg : args)
 			execArgs.add(arg);
 
 		// If Cytoscape won't run and the user agrees to run it with default settings, we try again.
 		if (!runCytoscape(execArgs, verbose) && continueWithDefaults(memSettings)) {
 			execArgs.clear();
 			execArgs.add("java");
 			execArgs.add("-Dswing.aatext=true");
 			execArgs.add("-Dawt.useSystemAAFontSettings=lcd");
 			execArgs.add("-Xss10M");
 			execArgs.add("-Xmx1024M");
 			execArgs.add("-cp");
 			execArgs.add("cytoscape.jar");
 			execArgs.add("cytoscape.CyMain");
 			execArgs.add("-p");
 			execArgs.add("plugins");
 			for (final String arg : args)
 				execArgs.add(arg);
 
 			System.exit(runCytoscape(execArgs, verbose) ? 0 : -1);
 		}
 
 		System.exit(0);
 	}
 
 
 	static boolean continueWithDefaults(final MemSettings memSettings) {
 		final int answer =
 			JOptionPane.showConfirmDialog(
 			        null,
 				"Cytoscape failed to start, possibly due to\n"
 				+ "invalid JVM memory memory settings!\n"
 				+ "(threadStackSize = " + memSettings.getThreadStackSize() + ")\n"
 				+ "(memoryAllocationPoolMaximumSize = "
 				+ memSettings.getMemoryAllocationPoolMaximumSize() + ")\n"
 				+ "Continue with default settings?",
 				"Continue?", JOptionPane.YES_NO_OPTION);
 
 		return answer == JOptionPane.YES_OPTION;
 	}
 
 
 	static boolean runCytoscape(final ArrayList<String> execArgs, final boolean verbose) {
 		if (verbose) {
 			System.err.print("Attempting to run: ");
 			for (final String arg : execArgs)
 				System.err.print(arg + " ");
 			System.err.println();
 		}
 
 		try {
 			final Process child =
 				Runtime.getRuntime().exec(
 				        execArgs.toArray(new String[execArgs.size()]));
 			final long startTime = System.currentTimeMillis();
 
 			final InputStreamReader childStdout =
 				new InputStreamReader(child.getInputStream());
 			final InputStreamReader childStderr =
 				new InputStreamReader(child.getErrorStream());
 
 			final Object mutex = new Object();
 			final Thread mapStdout = new StreamMapper(childStdout, System.out, mutex);
			final Thread mapStderr = new StreamMapper(childStdout, System.err, mutex);
 			mapStdout.start();
 			mapStderr.start();
 
 			try {
 				child.waitFor();
 				mapStdout.join();
 				mapStderr.join();
 
 				// If the subprocess exits less than 5 seconds after it started we
 				// assume that Cytoscape never managed to start up and we try to run
 				// it with a set of default arguments.
 				final long endTime = System.currentTimeMillis();
 				if (endTime - startTime < 5000)
 					return false;
 
 				// Return the exit code from Cytoscape to the OS.
 				final int exitCode = child.exitValue();
 				System.exit(exitCode);
 			} catch (final Exception e) {
 				System.exit(-1);
 			}
 		} catch (final java.io.IOException e) {
 			System.out.println("Failed to execute subprocess:");
 			System.out.println(e.toString());
 			System.exit(-1);
 		}
 
 		return true; // Keep the compiler happy!
 	}
 }
