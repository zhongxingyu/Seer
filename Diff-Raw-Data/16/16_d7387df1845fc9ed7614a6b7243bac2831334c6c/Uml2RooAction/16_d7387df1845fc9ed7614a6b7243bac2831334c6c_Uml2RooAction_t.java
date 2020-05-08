 /*
  * Copyright 2012 jccastrejon
  *  
  * This file is part of Model2Roo.
  *
  * Model2Roo is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * any later version.
  *
  * Model2Roo is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
 
  * You should have received a copy of the GNU General Public License
  * along with Model2Roo.  If not, see <http://www.gnu.org/licenses/>.
  */
 package fr.imag.model2roo.uml.ui.popupMenus;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.ui.IActionDelegate;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.actions.ActionDelegate;
 import org.eclipse.ui.console.ConsolePlugin;
 import org.eclipse.ui.console.IConsole;
 import org.eclipse.ui.console.IConsoleConstants;
 import org.eclipse.ui.console.IConsoleManager;
 import org.eclipse.ui.console.IConsoleView;
 import org.eclipse.ui.console.MessageConsole;
 import org.eclipse.ui.console.MessageConsoleStream;
 
 import fr.imag.model2roo.uml.ui.Activator;
 import fr.imag.model2roo.uml.ui.common.GenerateAll;
 
 /**
  * Uml2Roo code generation.
  */
 public class Uml2RooAction extends ActionDelegate implements IActionDelegate {
 
 	/**
 	 * Eclipse console.
 	 */
 	public static MessageConsole messageConsole;
 
 	/**
 	 * Output Stream to the Eclipse console.
 	 */
 	public static MessageConsoleStream messageConsoleStream;
 
 	/**
 	 * Selected model files.
 	 */
 	protected List<IFile> files;
	
	/**
	 * 
	 */
	public Uml2RooAction() {
		super();
		
		Uml2RooAction.messageConsole = Uml2RooAction.findConsole("Model2Roo");
		Uml2RooAction.messageConsoleStream = Uml2RooAction.messageConsole.newMessageStream();
	}
 
 	/**
 	 * Get the Output Stream to the Eclipse console.
 	 * 
 	 * @return
 	 */
 	public static MessageConsoleStream getConsoleStream() {
 		return Uml2RooAction.messageConsoleStream;
 	}
 
 	/**
 	 * Delete any messages previously written to the Eclipse console.
 	 */
 	public static void clearConsole() {
 		IConsoleView view;
 		IWorkbenchPage page;
 
 		try {
 			Uml2RooAction.messageConsole.clearConsole();
 			page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
 			view = (IConsoleView) page.showView(IConsoleConstants.ID_CONSOLE_VIEW);
 			view.display(messageConsole);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Get the Eclipse console identified with the given name.
 	 * 
 	 * @param name
 	 * @return
 	 */
 	public static MessageConsole findConsole(final String name) {
 		ConsolePlugin plugin;
 		IConsoleManager conMan;
 		IConsole[] existing;
 
 		plugin = ConsolePlugin.getDefault();
 		conMan = plugin.getConsoleManager();
 		existing = conMan.getConsoles();
 		for (int i = 0; i < existing.length; i++) {
 			if (name.equals(existing[i].getName())) {
 				return (MessageConsole) existing[i];
 			}
 		}
 
 		// no console found, so create a new one
 		MessageConsole myConsole = new MessageConsole(name, null);
 		conMan.addConsoles(new IConsole[] { myConsole });
 		return myConsole;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.ui.actions.ActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
 	 *      org.eclipse.jface.viewers.ISelection)
 	 * @generated
 	 */
 	@SuppressWarnings("unchecked")
 	public void selectionChanged(IAction action, ISelection selection) {
 		if (selection instanceof IStructuredSelection) {
 			files = ((IStructuredSelection) selection).toList();
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.ui.actions.ActionDelegate#run(org.eclipse.jface.action.IAction)
 	 * @generated
 	 */
 	public void run(IAction action) {
 		if (files != null) {
 			IRunnableWithProgress operation = new IRunnableWithProgress() {
 				public void run(IProgressMonitor monitor) {
 					try {
 						File rooFile;
 						IContainer target = null;
 						Iterator<IFile> filesIt = files.iterator();
 						MessageConsoleStream consoleStream;
 
 						rooFile = null;
 						consoleStream = Uml2RooAction.getConsoleStream();
 						Uml2RooAction.clearConsole();
 						while (filesIt.hasNext()) {
 							IFile model = (IFile) filesIt.next();
 							URI modelURI = URI.createPlatformResourceURI(model.getFullPath().toString(), true);
 							try {
 								target = model.getParent();
 								rooFile = new File(model.getRawLocation().toString().replaceAll(".uml", ".roo"));
 								this.cleanTargetDirectory(rooFile);
 								GenerateAll generator = new GenerateAll(modelURI, target, getArguments());
 								generator.doGenerate(monitor);
 							} catch (IOException e) {
 								IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e);
 								Activator.getDefault().getLog().log(status);
 							} finally {
 								this.formatOutputFiles(target.getLocation().toFile());
 								model.getProject().refreshLocal(IResource.DEPTH_INFINITE, monitor);
 
 								// Execute Roo script
 								consoleStream.println("Executing Roo script...");
 								new Thread(new RooThread(rooFile, consoleStream)).start();
 							}
 						}
 					} catch (CoreException e) {
 						IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e);
 						Activator.getDefault().getLog().log(status);
 					}
 				}
 
 				/**
 				 * 
 				 * @param targetDirectory
 				 */
 				private void cleanTargetDirectory(final File rooFile) throws IOException {
 					File pomFile;
 
 					// The roo script will be regenerated
 					if (rooFile.exists()) {
 						rooFile.delete();
 						rooFile.createNewFile();
 					}
 
 					// Delete the results of previous executions
 					this.deleteDirectory(new File(rooFile.getParentFile(), "/src"));
 					this.deleteDirectory(new File(rooFile.getParentFile(), "/target"));
 					pomFile = new File(rooFile.getParentFile(), "pom.xml");
 					if (pomFile.exists()) {
 						pomFile.delete();
 					}
 				}
 
 				/**
 				 * Delete a non-empty directory
 				 * 
 				 * @param path
 				 * @return
 				 */
 				private boolean deleteDirectory(File path) {
 					if (path.exists()) {
 						File[] files = path.listFiles();
 						for (int i = 0; i < files.length; i++) {
 							if (files[i].isDirectory()) {
 								deleteDirectory(files[i]);
 							} else {
 								files[i].delete();
 							}
 						}
 					}
 					return (path.delete());
 				}
 
 				/**
 				 * 
 				 * @param targetDirectory
 				 */
 				private void formatOutputFiles(final File targetDirectory) {
 					if (targetDirectory.exists()) {
 						for (File file : targetDirectory.listFiles()) {
 							this.formatOutputFile(file);
 						}
 					}
 				}
 
 				private void formatOutputFile(final File outputFile) {
 					String line;
 					BufferedReader reader;
 					BufferedWriter writer;
 					StringBuilder contents;
 
 					contents = new StringBuilder();
 					reader = null;
 					try {
 						try {
 							// Format content
 							reader = new BufferedReader(new FileReader(outputFile));
 							while ((line = reader.readLine()) != null) {
 								line = line.trim();
 								if (line.length() > 0) {
 									// Separate main sections
 									if (line.startsWith("//")) {
 										contents.append(System.getProperty("line.separator"));
 									}
 
 									// Output content line
 									contents.append(line).append(System.getProperty("line.separator"));
 								}
 							}
 
 							// Output content
 							outputFile.delete();
 							writer = new BufferedWriter(new FileWriter(outputFile));
 							writer.write(contents.toString());
 							writer.close();
 						} finally {
 							if (reader != null) {
 								reader.close();
 							}
 						}
 					} catch (IOException e) {
 						// TODO: Exception handling
 					}
 				}
 			};
 			try {
 				PlatformUI.getWorkbench().getProgressService().run(true, true, operation);
 			} catch (InvocationTargetException e) {
 				IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e);
 				Activator.getDefault().getLog().log(status);
 			} catch (InterruptedException e) {
 				IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e);
 				Activator.getDefault().getLog().log(status);
 			}
 		}
 	}
 
 	/**
 	 * Computes the arguments of the generator.
 	 * 
 	 * @return the arguments
 	 * @generated
 	 */
 	protected List<? extends Object> getArguments() {
 		return new ArrayList<String>();
 	}
 
 	/**
 	 * Thread in which Roo scripts can be executed.
 	 * 
 	 * @author jccastrejon
 	 * 
 	 */
 	static class RooThread implements Runnable {
 		private File rooFile;
 		private MessageConsoleStream consoleStream;
 
 		/**
 		 * Class that redirects Roo messages to an Eclipse console. Based on
 		 * http://www.javaworld.com/jw-12-2000/jw-1229-traps.html?page=4
 		 * 
 		 * @author jccastrejon
 		 * 
 		 */
 		class RooConsoleStream extends Thread {
 			InputStream inputStream;
 			MessageConsoleStream consoleStream;
 
 			/**
 			 * Full constructor.
 			 * 
 			 * @param inputStream
 			 * @param type
 			 * @param consoleStream
 			 */
 			RooConsoleStream(InputStream inputStream, final MessageConsoleStream consoleStream) {
 				this.inputStream = inputStream;
 				this.consoleStream = consoleStream;
 			}
 
 			public void run() {
 				try {
 					String line;
 					BufferedReader bufferedReader;
 					InputStreamReader inputStreamReader;
 
 					inputStreamReader = new InputStreamReader(inputStream);
 					bufferedReader = new BufferedReader(inputStreamReader);
 
 					line = null;
 					while ((line = bufferedReader.readLine()) != null) {
 						// Remove characters intended for console output
 						consoleStream.println(line.replaceAll("\\[\\d*m", ""));
 					}
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 
 		/**
 		 * Full constructor.
 		 * 
 		 * @param rooFile
 		 * @param consoleStream
 		 */
 		public RooThread(final File rooFile, final MessageConsoleStream consoleStream) {
 			this.rooFile = rooFile;
 			this.consoleStream = consoleStream;
 		}
 
 		public void run() {
 			Process process;
 			int processCode;
 			RooConsoleStream errorStream;
 			RooConsoleStream outputStream;
 
 			try {
 				process = Runtime.getRuntime().exec(this.getRooCommand() + " script --file " + rooFile.getName(), null,
 				        rooFile.getParentFile());
 
 				errorStream = new RooConsoleStream(process.getErrorStream(), consoleStream);
 				outputStream = new RooConsoleStream(process.getInputStream(), consoleStream);
 
 				// Prepare console streams and wait for the Roo execution
 				errorStream.start();
 				outputStream.start();
 				processCode = process.waitFor();
 
 				// Check if there was any error during the Roo execution
 				if (processCode != 0) {
 					throw new RuntimeException();
 				} else {
 					consoleStream.println("Roo script successfully executed!");
 				}
 
 				// Show to the user the results of the Roo execution
 				this.refreshWorkspace();
 			} catch (Exception e) {
 				consoleStream.println("The Roo script could not be successfully executed: " + e.getMessage());
 			}
 		}
 
 		/**
 		 * Get the correct invocation of the Roo console depending on the OS.
 		 * 
 		 * @return
 		 */
 		private String getRooCommand() {
 			return (System.getProperty("os.name").toLowerCase().startsWith("windows")) ? "roo.bat" : "roo";
 		}
 
 		/**
          * 
          */
 		private void refreshWorkspace() {
 			IProject[] projects;
 
 			projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
 			for (IProject project : projects) {
 				try {
 					project.refreshLocal(IProject.DEPTH_INFINITE, null);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 }
