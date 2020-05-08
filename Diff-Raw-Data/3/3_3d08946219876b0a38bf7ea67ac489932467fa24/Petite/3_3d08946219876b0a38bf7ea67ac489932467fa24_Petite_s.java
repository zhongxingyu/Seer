 /* 
  * License: source-license.txt
  * If this code is used independently, copy the license here.
  */
 
 package wombat.scheme;
 
 import java.awt.BorderLayout;
 import java.io.*;
 import java.net.URISyntaxException;
 import java.util.*;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 
 import javax.swing.*;
 
 import wombat.scheme.util.InteropAPI;
 import wombat.util.OS;
 
 /**
  * Class to wrap Petite bindings.
  */
 public class Petite {
 	static final boolean DEBUG_DISPLAY = false;
 	static final boolean DEBUG_INTEROP = false;
 	static final boolean DEBUG_LISTENER = false;
 	static int DEBUG_LISTENER_COUNT = 0;
 	
 	// Keep track of which state the Petite bindings are in.
 	private enum PetiteState {
 		Startup, // getting everything running
 		Interop, // communicating between Scheme and Java
 		Command, // reading a command from the user
 	}
 	PetiteState State = PetiteState.Startup;
 	boolean HalfPrompt = false;
 	
 	// Remember if this is the first line responding to a new command.
 	boolean FirstResponse = false;
 
 	// Choose the different prompt characters.
 	static final char Prompt1 = '|';
 	static final char Prompt2 = '`';
 	static final char Interop = '!';
 
 	// Listen to events from Petite machine.
 	List<PetiteListener> Listeners = new ArrayList<PetiteListener>();
 
 	// Communicate to and from the Petite process.
 	Writer ToPetite;
 	Reader FromPetite;
 	Process NativeProcess;
 	Thread FromPetiteThread;
 	
 	// Buffers for commands from the Petite process.
 	StringBuffer Buffer = new StringBuffer();
 	StringBuffer InteropBuffer = new StringBuffer();
 
 	// The root is either this directory or a nested 'lib' directory.
 	static File[] searchDirs;
 	static {
 		try {
 			searchDirs = new File[] {
 				new File("").getCanonicalFile(),
 				new File(new File("").getCanonicalFile(), "lib").getCanonicalFile(),
 				new File(Petite.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getCanonicalFile(),
 				new File(new File(Petite.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()),"lib").getCanonicalFile(),
 				new File(Petite.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile().getCanonicalFile(),
 				new File(new File(Petite.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile(), "lib").getCanonicalFile(), 
 			};
 		} catch (IOException ex) {
 		} catch (URISyntaxException e) {
 		}
 	};
 
 	/**
 	 * Create a new Petite binding.
 	 * 
 	 * @throws IOException If we fail to access the Petite process.
 	 * @throws URISyntaxException If we have problems getting the path from a JAR file.
 	 */
 	public Petite() throws IOException, URISyntaxException {
 		// Add a debug listener if requested.
 		if (DEBUG_LISTENER) {
 			final int myCount = DEBUG_LISTENER_COUNT++;
 			Listeners.add(new PetiteListener() {
 				@Override public void onStop() {
 					System.out.println("** (" + myCount + ") onStop");
 				}
 				
 				@Override public void onReady() {
 					System.out.println("** (" + myCount + ") onReady");
 				}
 				
 				@Override public void onOutput(String output) {
 					System.out.print("** (" + myCount + ") onOutput:\n** | ");
 					System.out.println(output.replace("\n", "\n** | "));
 				}
 				
 				@Override public void onError(Exception ex) {
 					System.err.println("** (" + myCount + ") onError:");
 					ex.printStackTrace();
 				}
 
 				@Override public void onReset() {
 					System.out.println("** (" + myCount + ") onReset");
 				}
 			});
 		}
 		
 		// Find a Petite installation.
 		File pdir = findPetiteDirectory();
 		
 		// If that fails, look for a version that's still archived.
 		if (pdir == null) {
 			findPetiteArchive();
 			pdir = findPetiteDirectory();
 		}
 
 		// If *that* fails, we're screwed.
 		if (pdir == null)
 			throw new IOException("Unable to find Petite directory.");
 
 		// Choose the binary and boot file.
 		String petiteBinary = null;
 		String petiteBoot = null;
 		if (OS.IsWindows) {
 		    petiteBinary = "petite.exe";
 		    petiteBoot = "petite.boot";
 		} else {
 		    if (OS.Is64Bit) {
 			petiteBinary = "petite64";
 			petiteBoot = "petite64.boot";
 		    } else {
 			petiteBinary = "petite";
 			petiteBoot = "petite.boot";
 		    }
 		}
 
 		// Debugging information.
 		System.out.println("Directory: " + pdir.getCanonicalPath() + ", Binary: " + petiteBinary + ", Boot: " + petiteBoot);
 
 		// Create the process builder.
 		ProcessBuilder pb = new ProcessBuilder(
 		    new File(pdir, petiteBinary).getCanonicalPath(), 
 		    "-b", 
 		    new File(pdir, petiteBoot).getCanonicalPath()
 		);
 		pb.directory(pdir.getParentFile().getParentFile());
 		pb.redirectErrorStream(true);
 
 		// Start the process.
 		NativeProcess = pb.start();
 
 		// Set up the print writer.
 		ToPetite = new PrintWriter(NativeProcess.getOutputStream());
 		FromPetite = new InputStreamReader(NativeProcess.getInputStream());
 
 		// Immediately send the command to reset the prompt to set everything up the first time.
 		reset();
 
 		// Create a listener thread.
 		FromPetiteThread = new Thread("Read from Petite") {
 			@SuppressWarnings("unused")
 			public void run() {
 				
 				final JTextArea text = new JTextArea();
 				if (DEBUG_DISPLAY) {
 					final JFrame frame = new JFrame();
 					frame.setSize(400, 400);
 					frame.setLayout(new BorderLayout());
 					frame.add(text);
 					frame.setVisible(true);
 				}
 				
 				char c;
 				try {
 					while (true) {
 						// Read from the buffer.
 						c = (char) FromPetite.read();
 						
 						// Display debug.
 						if (DEBUG_DISPLAY && c != (char) 65535) {
 							text.setText(text.getText() + c);
 						}
 
 						// Ignore end of file characters.
 						if (c == (char) 65535) {
 						}
 
 						// Potential start of a prompt.
 						else if (c == Prompt1) {
 							HalfPrompt = true;
 						}
 
 						// First prompt after startup.
 						else if (HalfPrompt && c == Prompt2 && State == PetiteState.Startup) {
 							Buffer.delete(0, Buffer.length());
 							HalfPrompt = false;
 							State = PetiteState.Command;
 							
 							synchronized (Listeners) {
 								for (PetiteListener pl : Listeners)
 									pl.onReady();
 							}
 						}
 						
 						// Prompt while running, means the process is ready for more.
 						else if (HalfPrompt && c == Prompt2 && State == PetiteState.Command) {
 							String output = Buffer.toString();
 							
 							Buffer.delete(0, Buffer.length());
 							HalfPrompt = false;
 							State = PetiteState.Command;
 							
 							synchronized (Listeners) {
 								for (PetiteListener pl : Listeners) {
 									if (FirstResponse && output.startsWith(" ")) {
 										pl.onOutput(output.substring(1));
 									} else {
 										pl.onOutput(output);
 									}
 									pl.onReady();
 								}
 								FirstResponse = false;
 							}
 						}
 
 						// Interop mode.
 						else if (HalfPrompt && c == Interop) {
 							if (State == PetiteState.Interop) {
 								String[] parts = InteropBuffer.toString().split(" ", 2);
 								String key = parts[0];
 								String val = (parts.length > 1 ? parts[1] : null);
 
 								if (DEBUG_INTEROP) System.out.println("calling interop: " + key + " with " + val); // debug
 								String result = InteropAPI.interop(key, val);
 								if (DEBUG_INTEROP) System.out.println("interop returns: " + (result.length() > 10 ? result .subSequence(0, 10) + "..." : result)); // debug
 								if (result != null) {
 									ToPetite.write(result + " ");
 									ToPetite.flush();
 								}
 								if (DEBUG_INTEROP) System.out.println("exiting interop");
 
 								InteropBuffer.delete(0, InteropBuffer.length());
 								HalfPrompt = false;
 								State = PetiteState.Command;
 							} else {
 								if (DEBUG_INTEROP) System.out.println("entering interop"); // debug
 								HalfPrompt = false;
 								State = PetiteState.Interop;
 							}
 						}
 
 						// Thought it was a prompt, but we were wrong.
 						// Remember to store the first half of the prompt.
 						else if (HalfPrompt) {
 							HalfPrompt = false;
 
 							if (State == PetiteState.Interop) {
 								InteropBuffer.append(Prompt1);
 								InteropBuffer.append(c);
 							} else {
 								Buffer.append(Prompt1);
 								Buffer.append(c);
 							}
 						}
 						
 						// Go ahead and force output on newlines (if not in interop)
 						else if (State == PetiteState.Command && c == '\n') {
 							Buffer.append(c);
 							String output = Buffer.toString();
 							Buffer.delete(0, Buffer.length());
 							synchronized (Listeners) {
 								for (PetiteListener pl : Listeners) {
 									if (FirstResponse && output.startsWith(" "))
 										pl.onOutput(output.substring(1));
 									else
 										pl.onOutput(output);
 								}
 								FirstResponse = false;
 							}
 						}
 
 						// Normal case, no new characters.
 						else {
 							if (State == PetiteState.Interop) {
 								InteropBuffer.append(c);
 							} else {
 								Buffer.append(c);
 							}
 						}
 					}
 
 				} catch (IOException e) {
 					synchronized (Listeners) {
 						for (PetiteListener pl : Listeners) {
 							pl.onError(e);
 						}
 					}
 				}
 			}
 		}; 
 		FromPetiteThread.setDaemon(true);
 		FromPetiteThread.start();
 		
 		// Show down the thread when done.
 		Runtime.getRuntime().addShutdownHook(new Thread("Petite Shutdown") {
 			public void run() {
 				NativeProcess.destroy();
 			}
 		});
 	}
 	
 	/**
 	 * Find the Petite directory..
 	 * @return The directory or null if one could not be found.
 	 */
 	private File findPetiteDirectory() {
 		// Find the correct Petite directory.
 		File pdir = null;
 		for (File dir : searchDirs) {
 			if (dir != null && dir.exists() && dir.isDirectory()) {
 				for (String path : dir.list()) {
 					if (path.startsWith("petite") && path.endsWith(OS.IsWindows ? "win" : OS.IsOSX ? "osx" : OS.IsLinux ? "linux" : "unknown")) {
 						pdir = new File(dir, path);
 						break;
 					}
 				}
 			}
 			if (pdir != null) 
 				break;
 		}
 		return pdir;
 	}
 	
 	/**
 	 * Find and unzip the proper Petite archive if it hasn't already been.
 	 */
 	private void findPetiteArchive() {
 		try {
 			for (File dir : searchDirs) {
 				if (dir != null && dir.exists() && dir.isDirectory()) {
 					for (String path : dir.list()) {
 						if (path.startsWith("petite") && path.endsWith((OS.IsWindows ? "win" : OS.IsOSX ? "osx" : OS.IsLinux ? "linux" : "unknown") + ".zip")) {
 							ZipFile zip = new ZipFile(new File(dir, path));
 	
 							@SuppressWarnings("unchecked")
 							Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) zip.entries();
 	
 							while (entries.hasMoreElements()) {
 								ZipEntry entry = entries.nextElement();
 	
 								if (entry.isDirectory()) {
 									new File(dir, entry.getName()).getCanonicalFile().mkdirs();
 								} else {
 							    	new File(dir, entry.getName()).getCanonicalFile().getParentFile().mkdirs();
 							    
 									File targetFile = new File(dir, entry.getName());
 									InputStream zipin = zip.getInputStream(entry);
 									OutputStream zipout = new BufferedOutputStream(new FileOutputStream(targetFile));
 												    
 									byte[] buffer = new byte[1024];
 									int len;
 												    
 									while((len = zipin.read(buffer)) >= 0)
 										zipout.write(buffer, 0, len);
 											    
 									zipin.close();
 									zipout.close();
 									
 									if (targetFile.getName().toLowerCase().startsWith("petite")) {
 										targetFile.setExecutable(true);
 								    }
 							    }
 							}
 	
 							zip.close();
 							return;
 						}
 					}
 				}
 			}
 		} catch(IOException ex) {
 			System.err.println("Unable to open petite archive: " + ex.getMessage());
 		}
 	}
 
 	/**
 	 * Reset Petite's environment.
 	 */
 	public void reset() {
 		// Actually clear the environment
 		sendCommand("(interaction-environment (copy-environment (scheme-environment) #t))");
 
		// So that (eq? 'A 'a) => #t
		sendCommand("(case-sensitive #f)");

 		// So that gensyms look at least semi-sane (it's not like anyone will need them)
 		sendCommand("(print-gensym #f)");
 
 		// To test infinite loops
 		sendCommand("(define (omega) ((lambda (x) (x x)) (lambda (x) (x x))))");
 
 		// Reset the library directories.
 		sendCommand("(library-directories '((\"lib\" . \"lib\") (\".\" . \".\") (\"..\" . \"..\") (\"dist\" . \"dist\") (\"dist/lib\" . \"dist/lib\")))");
 
 		// Fix error message that give define/lambda names
 		sendCommand("(import (wombat define))");
 		
 		// Make sure that the prompt is set as we want it
 		// Set this last so all of the startup commands have time to run
 		sendCommand("(waiter-prompt-string \"|`\")");
 		
 		// Tell the listeners what we did.
 		synchronized (Listeners) {
 			for (PetiteListener pl : Listeners)
 				pl.onReset();	
 		}
 	}
 
 	/**
 	 * Stop the running process.
 	 * 
 	 * @throws IOException If we cannot connect.
 	 * @throws URISyntaxException Botched file from JAR.
 	 */
 	public void stop() {
 		// Shut down connection.
 		NativeProcess.destroy();
 		try {
 			NativeProcess.waitFor();
 		} catch (InterruptedException e) {
 		}
 		
 		// Tell any listeners that we got it.
 		synchronized (Listeners) {
 			for (PetiteListener pl : Listeners)
 				pl.onStop();
 		}
 	}
 
 	/**
 	 * Listen for state changes in the Petite binding.
 	 * @param pl A listener
 	 */
 	public void addPetiteListener(final PetiteListener pl) {
 		Listeners.add(pl);
 	}
 	
 	/**
 	 * Stop a certain Petite listener.
 	 * @param pl The listener that we are watching.
 	 */
 	public void removePetiteListener(final PetiteListener pl) {
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				Listeners.remove(pl);	
 			}
 		});
 	}
 	
 	/**
 	 * Send a command to the Petite process.
 	 * 
 	 * @param cmd The command to send.
 	 */
 	public void sendCommand(String cmd) {
 		try {
 			// Swap out lambda character for string
 			cmd = cmd.replace("\u03BB", "lambda");
 			
 			// Note that this is the first process.
 			FirstResponse = true;
 
 			// Send it, make sure there's a newline.
 			// Use flush to force it to actually run.
 			ToPetite.write(cmd);
 			if (!cmd.endsWith("\n"))
 				ToPetite.write("\n");
 			ToPetite.flush();
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 }
 
