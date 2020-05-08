 /*
  * Copyright 2004-5 Enigmatec Corporation Ltd
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  *
  * Change History:
  * Feb 17, 2005 : Initial version created by gary
  */
 package org.savara.tools.scenario.designer.simulate;
 
 import java.io.File;
 import java.io.IOException;
 import java.text.MessageFormat;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.debug.core.ILaunch;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.IStreamListener;
 import org.eclipse.debug.core.model.IStreamMonitor;
 import org.eclipse.debug.core.model.IProcess;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.launching.AbstractJavaLaunchConfigurationDelegate;
 import org.eclipse.jdt.launching.ExecutionArguments;
 import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
 import org.eclipse.jdt.launching.IVMInstall;
 import org.eclipse.jdt.launching.IVMRunner;
 import org.eclipse.jdt.launching.VMRunnerConfiguration;
 import org.eclipse.osgi.util.ManifestElement;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.Constants;
 import org.osgi.framework.ServiceReference;
 import org.savara.scenario.simulation.ScenarioSimulatorMain;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.runtime.Path;
 
 /**
  * This class is responsible for launching a scenario test against
  * a test scenario.
  */
 public class ScenarioSimulationLauncher
 			extends AbstractJavaLaunchConfigurationDelegate {
 	
 	private static Logger logger = Logger.getLogger(ScenarioSimulationLauncher.class.getName());
 
 	/**
 	 * This is the default constructor.
 	 *
 	 */
 	public ScenarioSimulationLauncher() {
 	}
 	
 	/**
 	 * This method launches the scenario test.
 	 * 
 	 * @param configuration The launch configuration
 	 * @param mode The mode (run or debug)
 	 * @param launch The launch object
 	 * @param monitor The optional progress monitor
 	 */
 	public void launch(ILaunchConfiguration configuration,
             String mode, ILaunch launch, IProgressMonitor monitor)
 						throws CoreException {
 		if (monitor == null) {
 			monitor = new NullProgressMonitor();
 		}
 		
 		monitor.beginTask(MessageFormat.format("{0}...", new String[]{configuration.getName()}), 3); //$NON-NLS-1$
 		// check for cancellation
 		if (monitor.isCanceled()) {
 			return;
 		}
 		
 		monitor.subTask("Verifying launch configuration....");
 						
 		String mainTypeName = ScenarioSimulatorMain.class.getName(); 
 
 		IVMInstall vm = verifyVMInstall(configuration);
 
 		IVMRunner runner = vm.getVMRunner(mode);
 		if (runner == null) {
 			abort("VM runner does not exist",
 					null, IJavaLaunchConfigurationConstants.ERR_VM_RUNNER_DOES_NOT_EXIST); //$NON-NLS-1$
 		}
 
 		File workingDir = verifyWorkingDirectory(configuration);
 		String workingDirName = null;
 		if (workingDir != null) {
 			workingDirName = workingDir.getAbsolutePath();
 		}
 		
 		// Environment variables
 		String[] envp= DebugPlugin.getDefault().getLaunchManager().getEnvironment(configuration);
 		
 		// Program & VM args
 		//String filename=configuration.getAttribute(
 		//		ScenarioSimulationLaunchConfigurationConstants.ATTR_PROJECT_NAME, "")+
 		//		"/"+configuration.getAttribute(
 		//		ScenarioSimulationLaunchConfigurationConstants.ATTR_SCENARIO, "");
 		
 		String simulationFile=configuration.getAttribute(
 				ScenarioSimulationLaunchConfigurationConstants.ATTR_SIMULATION_FILE,
 					"");
 		
 		String pgmArgs="\""+simulationFile+"\"";
 		
 		logger.fine("Launching scenario test with args: "+pgmArgs);
 			
 		String vmArgs = getVMArguments(configuration);
 		ExecutionArguments execArgs = new ExecutionArguments(vmArgs, pgmArgs);
 		
 		// VM-specific attributes
 		Map vmAttributesMap = getVMSpecificAttributesMap(configuration);
 		
 		// Classpath
 		String[] classpath = getClasspath(configuration);
 		
 		// Create VM config
 		VMRunnerConfiguration runConfig = new VMRunnerConfiguration(mainTypeName, classpath);
 		runConfig.setProgramArguments(execArgs.getProgramArgumentsArray());
 		runConfig.setEnvironment(envp);
 		runConfig.setVMArguments(execArgs.getVMArgumentsArray());
 		runConfig.setWorkingDirectory(workingDirName);
 		runConfig.setVMSpecificAttributesMap(vmAttributesMap);
 
 		// Bootpath
 		runConfig.setBootClassPath(getBootpath(configuration));
 				
 		// check for cancellation
 		if (monitor.isCanceled()) {
 			return;
 		}		
 		
 		// stop in main
 		prepareStopInMain(configuration);
 		
 		// done the verification phase
 		monitor.worked(1);
 		
 		// Launch the configuration - 1 unit of work
 		runner.run(runConfig, launch, monitor);
 		
 		IProcess[] processes=launch.getProcesses();
 		if (processes.length > 0) {
 			processes[0].getStreamsProxy().getOutputStreamMonitor().
 						addListener(new IStreamListener() {
 				public void streamAppended(String str, IStreamMonitor mon) {
 					handleResults(str, false);
 				}
 			});
 			processes[0].getStreamsProxy().getErrorStreamMonitor().
 						addListener(new IStreamListener() {
 				public void streamAppended(String str, IStreamMonitor mon) {
 					handleResults(str, true);
 				}
 			});
 		}
 		
 		// check for cancellation
 		if (monitor.isCanceled()) {
 			return;
 		}	
 		
 		monitor.done();
 	}
 	
 	/**
 	 * This method handles the results produced by the launched
 	 * test.
 	 * 
 	 * @param results The results
 	 * @param errorStream Whether the results are from the error
 	 * 						stream
 	 */
 	protected void handleResults(String results, boolean errorStream) {
 		System.out.println(results);
 	}
 	
 	/**
 	 * This method returns the full path to the scenario.
 	 * 
 	 * @param relativePath The is the scenario path begining at
 	 * 					the project
 	 * @return The full path
 	 */
 	protected String getPathForScenario(String relativePath) {
 		String ret=null;
 		
 		IFile file=ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(relativePath));
 		if (file != null && file.exists()) {
 			ret = file.getLocation().toString();
 		}
 		
 		return(ret);
 	}
 	
 	/**
 	 * This method derives the classpath required to run the 
 	 * ScenarioTester utility.
 	 * 
 	 * @param configuration The launch configuation
 	 * @return The list of classpath entries
 	 */
 	public String[] getClasspath(ILaunchConfiguration configuration) {
 		String[] ret=null;
 		java.util.Vector<String> classpathEntries=new java.util.Vector<String>();
 					
 		// Add classpath entry for current Java project
 		String projnames=null;
 		
 		try {
 			projnames = configuration.getAttribute(
 					ScenarioSimulationLaunchConfigurationConstants.ATTR_PROJECT_NAME, "");
 			
 			String[] projname=projnames.split(",");
 			
 			java.util.List<String> outputPaths=new java.util.Vector<String>();
 			
 			for (int n=0; n < projname.length; n++) {
 				try {
 					if (logger.isLoggable(Level.FINE)) {
 						logger.fine("Building classpath for project: "+projname[n]);
 					}
 					
 					IProject project=
 						ResourcesPlugin.getWorkspace().getRoot().getProject(projname[n]);
 		
 					IJavaProject jproject=JavaCore.create(project); 
 					
 					// Add output location
 					IPath outputLocation=jproject.getOutputLocation();
 					
 					IFolder folder=
 						ResourcesPlugin.getWorkspace().getRoot().getFolder(outputLocation);
 					
 					String path=folder.getLocation().toString();
 		
 					outputPaths.add(path);
 					
 					// Add other libraries to the classpath
 					IClasspathEntry[] curclspath=jproject.getRawClasspath();
 					for (int i=0; curclspath != null &&
 								i < curclspath.length; i++) {
 						
 						if (curclspath[i].getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
 							IFile file=
 								ResourcesPlugin.getWorkspace().
 									getRoot().getFile(curclspath[i].getPath());
 		
 							if (file.exists()) {
 								// Library is within the workspace
 								classpathEntries.add(file.getLocation().toString());
 							} else {
 								// Assume library is external to workspace
 								classpathEntries.add(curclspath[i].getPath().toString());
 							}
 							
 						} else if (curclspath[i].getEntryKind() ==
 										IClasspathEntry.CPE_CONTAINER) {
 							// Container's not currently handled - but
 							// problem need to retrieve from project and
 							// iterate over container entries
 						} else if (curclspath[i].getEntryKind() ==
 										IClasspathEntry.CPE_SOURCE) {
 							// TODO: Possibly temporary addition to classpath list
 							// as SCA Java simulator needs to access composite from
 							// classpath :(, and so need to convert path to a location
 							// on the classpath, which may actually be in the source
 							// location
 							IFile file=
 									ResourcesPlugin.getWorkspace().
 										getRoot().getFile(curclspath[i].getPath());
 		
 							classpathEntries.add(file.getLocation().toString());
 						}
 					}
 					
 				} catch(Exception e) {
 					// TODO: report error
 				}
 			}
 			
 			if (outputPaths.size() == 1) {
 				classpathEntries.add(outputPaths.get(0));
 			} else if (outputPaths.size() > 0) {
 				// Need to merge output folders into one location
 				java.io.File dir=new java.io.File(System.getProperty("java.io.tmpdir")+
 						java.io.File.separatorChar+"savara"+java.io.File.separatorChar+
 						"simulation"+System.currentTimeMillis());
 				dir.deleteOnExit();
 				
 				dir.mkdirs();
 				
 				classpathEntries.add(dir.getAbsolutePath());
 				
 				for (String path : outputPaths) {
 					copy(new java.io.File(path), dir);
 				}
 			}
 		} catch(Exception ex) {
 			ex.printStackTrace();
 		}
 		
 		java.util.List<Bundle> bundles=getBundles();
 		
 		for (Bundle b : bundles) {
 			buildClassPath(b, classpathEntries);
 		}		
 		
 		ret = new String[classpathEntries.size()];
 		classpathEntries.copyInto(ret);
 		
 		if (logger.isLoggable(Level.FINEST)) {
 			logger.finest("Scenario Simulation Classpath:");
 			for (int i=0; i < ret.length; i++) {
 				logger.finest("    ["+i+"] "+ret[i]);
 			}
 		}
 		
 		return(ret);
 	}
 	
 	protected void copy(java.io.File src, java.io.File target) throws java.io.IOException {
 
 		if (src.isDirectory()) 	{
 			
 			// Check if target folder needs to be created
 			if (target.exists() == false) {
 				if (target.mkdirs() == false) {
 					throw new IOException("Could not create target direcotry: "+
 									target.getPath());
 				}
 			}
 			
 			target.deleteOnExit();
 
 			java.io.File[] children=src.listFiles();
 			
 			for (int i=0; i < children.length; i++) {
 				copy(children[i], new File(target, children[i].getName()));
 			}
 		} else { 
 			java.io.FileInputStream fis=new java.io.FileInputStream(src);
 			
 			byte[] b=new byte[fis.available()];
 			fis.read(b);
 			
 			fis.close();
 			
 			java.io.FileOutputStream fos=new java.io.FileOutputStream(target);
 			
 			fos.write(b);
 			
 			fos.close();
 			
 			target.deleteOnExit();
 		}
 	}
 	
 	protected java.util.List<Bundle> getBundles() {
 		java.util.List<Bundle> ret=new java.util.Vector<Bundle>();
 		
 		Bundle bundle=Platform.getBundle("org.savara.tools.scenario");
 		
 		if (bundle != null) {
 			getBundles(bundle, ret);
 		}
 
 		if (logger.isLoggable(Level.FINE)) {
 			logger.fine("Number of bundles is: "+ret.size());
 		}
 		
 		return(ret);
 	}
 	
 	protected void getBundles(Bundle bundle, java.util.List<Bundle> list) {
 		
 		if (list.contains(bundle) == false) {
 			list.add(bundle);
 			
 			// Check for role simulators
 			ServiceReference<?>[] refs=bundle.getServicesInUse();
 			
 			if (refs != null) {
 				for (ServiceReference<?> ref : refs) {
 					if (logger.isLoggable(Level.FINER)) {
 						logger.finer("Bundle: "+bundle+" Referenced="+ref.getBundle());	
 					}
 					getBundles(ref.getBundle(), list);
 				}
 			}
 
 			// Get required bundles
 			String required=bundle.getHeaders().get(Constants.REQUIRE_BUNDLE);
 			
 			if (required != null) {
 				String[] bundles=required.split(",");
 				
 				for (int i=0; i < bundles.length; i++) {
 					String bundleId=bundles[i];
 					
 					int index=0;
 					if ((index=bundleId.indexOf(';')) != -1) {
 						bundleId = bundleId.substring(0, index);
 					}
 					
 					Bundle other=Platform.getBundle(bundleId);
 					
 					if (logger.isLoggable(Level.FINER)) {
 						logger.finer("Bundle: "+bundle+" Required="+other);	
 					}
 					
 					if (other == null) {
 						logger.finest("Failed to find bundle '"+bundleId+"'");
 					} else {
 						getBundles(other, list);
 					}
 				}
 			}
 		}
 	}
 	
 	protected void buildClassPath(Bundle bundle, java.util.List<String> entries) {
 		java.net.URL installLocation= bundle.getEntry("/");
 		java.net.URL local= null;
 		try {
 			local= Platform.asLocalURL(installLocation);
 		} catch (java.io.IOException e) {
 			e.printStackTrace();
 		}
 		
 		String baseLocation = local.getFile();
 
 		try {
 			String projectClassPath=getProjectClasspath(baseLocation);
 			
 			// TODO: If classpath has been set, but the items are not available,
 			// then resort to the .classpath file. Issue - how to resolve variables?
 			
 			String requires = (String)bundle.getHeaders().get(Constants.BUNDLE_CLASSPATH);
 			ManifestElement[] elements = ManifestElement.parseHeader(Constants.BUNDLE_CLASSPATH, requires);
 			
 			for (int i=0; elements != null && i < elements.length; i++) {
 				
 				String path=baseLocation+elements[i].getValue();
 				
 				// Check if path is for a Jar and that the
 				// file exists - if not see if a classes
 				// directory exists
 				if (path.endsWith(".jar")) {
 					
 					if ((new File(path)).exists() == false) {
 						String jarPath=null;
 						
 						// Check if .classpath file exists - may be running in test workbench
 						// and need to access local maven repo
 						if (projectClassPath != null) {
 							jarPath = getJarPath(projectClassPath, elements[i].getValue());
 						}
 						
 						if (jarPath != null) {
 							path = jarPath;
 						} else {
 							if ((new File(baseLocation+"classes")).exists()) {
 								path = baseLocation+"classes";
 							} else if ((new File(baseLocation+"target"+File.separatorChar+"classes")).exists()) {
 								path = baseLocation+"target"+File.separatorChar+"classes";
 							} else if ((new File(baseLocation+"bin")).exists()) {
 								path = baseLocation+"bin";
 							} else {
 								path = baseLocation;
 							}
 						}
 					}
 				} else if (elements[i].getValue().equals(".")) {
 					if ((new File(baseLocation+"classes")).exists()) {
 						path = baseLocation+"classes";
 					} else if ((new File(baseLocation+"target"+File.separatorChar+"classes")).exists()) {
 						path = baseLocation+"target"+File.separatorChar+"classes";
 					} else if ((new File(baseLocation+"bin")).exists()) {
 						path = baseLocation+"bin";
 					} else {
 						path = baseLocation;
 					}
 				}
 				
 				if (entries.contains(path) == false) {
 					if (logger.isLoggable(Level.FINE)) {
 						logger.fine("Adding classpath entry '"+
 								path+"'");
 					}
 					entries.add(path);
 					
 					if (elements[i].getValue().equals(".")) {
 						if ((new File(baseLocation+"classes")).exists()) {
 							path = baseLocation+"classes";
 							
 							entries.add(path);
 						}
 					}
 				}
 			}
 			
 			if (elements == null) {
 				if (logger.isLoggable(Level.FINE)) {
 					logger.fine("Adding classpath entry '"+
 							baseLocation+"'");
 				}
 				
 				if ((new File(baseLocation+"classes")).exists()) {
 					entries.add(baseLocation+"classes");
 				} else if ((new File(baseLocation+"target"+File.separatorChar+"classes")).exists()) {
 					entries.add(baseLocation+"target"+File.separatorChar+"classes");
 				} else if ((new File(baseLocation+"bin")).exists()) {
 					entries.add(baseLocation+"bin");
 				} else {
 
 					entries.add(baseLocation);
 				}
 			}
 			
 		} catch(Exception e) {
 			logger.severe("Failed to construct classpath: "+e);
 			e.printStackTrace();
 		}
 	}
 	
 	protected String getProjectClasspath(String baseLocation) {
 		String ret=null;
 		
 		File cppath=new File(baseLocation+".classpath");
 		if (cppath.exists()) {
 			try {
 				java.io.FileInputStream fis=new java.io.FileInputStream(cppath);
 				
 				byte[] b=new byte[fis.available()];
 				
 				fis.read(b);
 				
 				fis.close();
 				
 				ret = new String(b);
 			} catch(Exception e) {
 				e.printStackTrace();
 			}
 		}
 		
 		return(ret);
 	}
 	
 	protected String getJarPath(String projectClassPath, String element) {
 		String ret=null;
 		
 		// Extract the jar name (without preceding folders or the file suffix)
 		int startindex=element.lastIndexOf('/');
 		int endindex=element.lastIndexOf('.');
 		
 		String jarName=element.substring(startindex+1, endindex);
 		
 		String locator="/"+jarName+"/";
 		
 		int index=projectClassPath.indexOf(locator);
 		
 		if (index != -1) {
 			int startpos=index;
 			for (; projectClassPath.charAt(startpos) != '"'; startpos--);
 			
 			int endpos=projectClassPath.indexOf('"', index);
 			
 			String newpath=projectClassPath.substring(startpos+1, endpos);
 			
 			ret=newpath.replaceAll("M2_REPO", System.getenv("HOME")+"/.m2/repository");
 		}
 		
 		return(ret);
 	}
 }
