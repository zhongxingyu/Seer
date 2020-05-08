 package org.syntax_definition.sdf;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 
 import org.eclipse.core.runtime.Platform;
 
 public class Tools {
 	private static Activator a = Activator.getInstance();
 
 	private static String[] environment;
 
 	/**
 	 * Execute a command, but first set up the environment and prefix the
 	 * command with the absolute path to the installation of commandline tool.
 	 * Note that the command is executed with its installation directory as
 	 * working directory. This is to facilitate relative search paths for
 	 * dynamically linked libraries.
 	 * 
 	 * @param commandline
 	 * @return
 	 * @throws IOException
 	 */
 	static private Process exec(String commandline) throws IOException {
 		initializeSearchPath();
 		String command = resolve(commandline);
 		makeExecutable(command);
 		return Runtime.getRuntime().exec(command, environment, a.getBinaryPrefix());
 	}
 	
 	private static void makeExecutable(String command) throws IOException {
 		String os = Platform.getOS();
 		if (os.equals(Platform.OS_LINUX)
 				|| os.equals(Platform.OS_MACOSX)) {
 			try {
				Process p = Runtime.getRuntime().exec("/bin/chmod +x " + binary(command));
 				p.waitFor();
 				if (p.exitValue() != 0) {
					System.err.println("chmod failed: " + p.exitValue());
//					 TODO don't know what to do here	
 				}
 			} catch (InterruptedException e) {
 			    // TODO don't know what to do here	
 			}
 		}
 	}
 
 	static private String binary(String commandline) {
 	  int beyondCommand = commandline.indexOf(' ');
 	  
 	  if (beyondCommand != -1) {
 	    return commandline.substring(0, beyondCommand);
 	  }
 	  else {
 		  return commandline;
 	  }
 	}
 	
 	static private String resolve(String commandline) throws IOException {
 		int beyondCommand = commandline.indexOf(' ');
 		
 		if (beyondCommand == -1) {
 			return a.getBinaryPrefix() + File.separator + commandline + a.getBinaryPostfix();
 		}
 		else {
 			String command = commandline.substring(0, beyondCommand);
 			String args = commandline.substring(beyondCommand, commandline.length());
 			return a.getBinaryPrefix() + File.separator + command + a.getBinaryPostfix() + " " + args;
 		}
 	}
 	
 	/**
 	 * Convert a string to an input stream for writing to a process
 	 * 
 	 * @param input
 	 * @return
 	 */
 	static public InputStream cat(String input) {
 		return new  ByteArrayInputStream(input.getBytes());
 	}
 
 	/**
 	 * Convert an input stream to a string for reading from a process
 	 * 
 	 * @param input
 	 * @return
 	 * @throws IOException
 	 */
 	static public String uncat(InputStream input) throws IOException {
 		BufferedReader reader = null;
 		String enc = System.getProperty("file.encoding");
 
 		try {
 			StringBuffer buffer = new StringBuffer();
 			char[] part = new char[2048];
 			int read = 0;
 			reader = new BufferedReader(new InputStreamReader(input, enc));
 
 			while ((read = reader.read(part)) != -1)
 				buffer.append(part, 0, read);
 
 			return buffer.toString();
 		} catch (IOException ex) {
 			throw ex;
 		} finally {
 			if (reader != null) {
 				try {
 					reader.close();
 				} catch (IOException ex) {
 					// silently ignored
 				}
 			}
 		}
 	}
 
 	/**
 	 * Execute a command.
 	 * 
 	 * @param command
 	 *            command to run. should start with non-whitespace
 	 * @param input
 	 *            input stream to write to the stdin of the command
 	 * @return
 	 * @throws IOException
 	 * @throws InterruptedException
 	 */
 	static public InputStream exec(String command, InputStream input) throws IOException, InterruptedException {
 		return pipeline(new String[] { command }, input);
 	}
 	
 	/**
 	 * This method schedules a list of processes, and connects their standard
 	 * input and standard output in the appropriate way. The user should supply
 	 * an input stream, and gets an inputstream back.
 	 * 
 	 * Note that the commands should not start with spaces, just the
 	 * Meta-Environment command name, and that a .exe suffix is not required.
 	 * 
 	 * @param commands
 	 * @param input
 	 * @return
 	 * @throws IOException
 	 * @throws InterruptedException
 	 */
 	static public InputStream pipeline(String[] commands, InputStream input)
 			throws IOException, InterruptedException {
 		Process[] procs = new Process[commands.length];
 
 		for (int i = 0; i < commands.length; i++) {
 			procs[i] = exec(commands[i]);
 		}
 
 		pipe(commands[0], input, procs[0].getOutputStream());
 		
 		for (int i = 1; i < commands.length; i++) {
 			pipe(commands[i], procs[i-1].getInputStream(), procs[i].getOutputStream());
 		}
 		
 		for (int i = 0; i < commands.length; i++) {
 			procs[i].waitFor();
 			if (procs[i].exitValue() != 0) {
 				// TODO: throw a different exception, collect all exit codes
 				throw new IOException("Command failed (" + procs[i].exitValue() + ")" + resolve(commands[i]));
 			}
 		}
 		
 		return procs[commands.length - 1].getInputStream();
 	}
 
 	private static void initializeSearchPath() {
 		if (environment == null) {
 			try {
 				environment = new String[] { "PATH="
 						+ a.getBinaryPrefix().getAbsolutePath() };
 			} catch (IOException e) {
 				// This can not happen because Activator.getInstance() would
 				// have failed much earlier.
 			}
 		}
 	}
 
 	/**
 	 * A helper method that pipes an inputstream to an output stream using a
 	 * small intermediate buffer. The pipe is executes asynchronously by 
 	 * starting a small worker thread. This is necessary to prevent deadlocks.
 	 * 
 	 * @param in
 	 * @param out
 	 * @throws IOException
 	 */
 	static private void pipe(String label, final InputStream in, final OutputStream out)
 			throws IOException {
 		Runnable worker = new Runnable() {
 			public void run() {
 				try {
 					byte[] buffer = new byte[8192];
 					int count;
 					while ((count = in.read(buffer)) >= 0) {
 						out.write(buffer, 0, count);
 					}
 					out.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		};
 		
 		new Thread(worker, "pipe: " + label).start();
 	}
 }
