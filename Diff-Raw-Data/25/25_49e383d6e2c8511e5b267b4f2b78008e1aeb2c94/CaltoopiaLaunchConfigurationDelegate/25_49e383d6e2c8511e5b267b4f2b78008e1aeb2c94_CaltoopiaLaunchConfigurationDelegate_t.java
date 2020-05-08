 /* 
  * Copyright (c) Ericsson AB, 2013
  * All rights reserved.
  *
  * License terms:
  *
  * Redistribution and use in source and binary forms, 
  * with or without modification, are permitted provided 
  * that the following conditions are met:
  *     * Redistributions of source code must retain the above 
  *       copyright notice, this list of conditions and the 
  *       following disclaimer.
  *     * Redistributions in binary form must reproduce the 
  *       above copyright notice, this list of conditions and 
  *       the following disclaimer in the documentation and/or 
  *       other materials provided with the distribution.
  *     * Neither the name of the copyright holder nor the names 
  *       of its contributors may be used to endorse or promote 
  *       products derived from this software without specific 
  *       prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND 
  * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, 
  * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
  * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR 
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
  * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR 
  * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS 
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package org.caltoopia.frontend.ui.launch;
 
 import java.io.File;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import org.caltoopia.analysis.test.Analysis;
 import java.util.Set;
 import org.caltoopia.cli.ActorDirectory;
 import org.caltoopia.cli.Cal2C;
 import org.caltoopia.cli.CompilationSession;
 import org.caltoopia.cli.SimulationSession;
 import org.caltoopia.frontend.ui.preferences.CalRootPreferencePage;
 import org.caltoopia.visual.test.PluginNetworkView;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.IWorkspaceDescription;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.debug.core.ILaunch;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.ui.console.ConsolePlugin;
 import org.eclipse.ui.console.IConsole;
 import org.eclipse.ui.console.IConsoleManager;
 import org.eclipse.ui.console.MessageConsole;
 import org.eclipse.ui.console.MessageConsoleStream;
 import org.caltoopia.visual.test.PluginAnalysisNetworkView;
 
 public class CaltoopiaLaunchConfigurationDelegate extends org.eclipse.debug.core.model.LaunchConfigurationDelegate {
 	
 	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
 		// Start by turning off auto build
 		IWorkspace workspace = ResourcesPlugin.getWorkspace();
 		IWorkspaceDescription description = workspace.getDescription();
 		boolean autobuild = false;
 		if (description.isAutoBuilding()) {
 			description.setAutoBuilding(false);
 			autobuild = true;
 			workspace.setDescription(description);
 		}
 
 		String output = configuration.getAttribute(CaltoopiaMainTab.CONFIG_ID_OUTPUT_FOLDER, "");
 		File outFile = new File(output);
 		if(!outFile.exists())
 			outFile.mkdir();
 		List<String> paths = configuration.getAttribute(CaltoopiaMainTab.CONFIG_ID_PATHS, new ArrayList<String>());
 		String top = configuration.getAttribute(CaltoopiaMainTab.CONFIG_ID_TOP_NETWORK, "");
 		String projectName = configuration.getAttribute(CaltoopiaMainTab.CONFIG_ID_PROJECT, "");
 		boolean visualize = configuration.getAttribute(CaltoopiaMainTab.CONFIG_ID_VISUALIZE, false);
 		boolean generateCdtProject = configuration.getAttribute(CaltoopiaMainTab.CONFIG_ID_CDT, false);
 		boolean generateDot = configuration.getAttribute(CaltoopiaMainTab.CONFIG_ID_DOT, false);
 		int debug = configuration.getAttribute(CaltoopiaMainTab.CONFIG_ID_DEBUG, CompilationSession.DEBUG_TYPE_DEFAULT);
 		String workingDirectory = configuration.getAttribute(CaltoopiaMainTab.CONFIG_ID_WORKING_FOLDER, "");
 		String runOptions = configuration.getAttribute(CaltoopiaMainTab.CONFIG_ID_RUN_OPTIONS, "");
 		boolean simulation = configuration.getAttribute(CaltoopiaMainTab.CONFIG_ID_CALSIM, false);
 		int analysis = configuration.getAttribute(CaltoopiaMainTab.CONFIG_ID_ANALYSIS, 0);
 		MessageConsole myConsole = findConsole("Caltoopia");
 		MessageConsoleStream out = myConsole.newMessageStream();	
 		
 		Cal2C cal2c = new Cal2C();
 		
 		//Check enviroment variables
 		String runTimePath = null;
 		String calsimPath = null;
 		String systemcPath = null;
 		String sdf3Path = null;
 		
 		try{
 			runTimePath = Platform.getPreferencesService().getString("org.caltoopia.frontend.Cal", "rts", null, null);
 			if(runTimePath == null || runTimePath.equals(CalRootPreferencePage.RUNTIME_ENVIRONMENT_VARIABLE))
 				runTimePath = System.getenv(CalRootPreferencePage.RUNTIME_ENVIRONMENT_VARIABLE);
 			
 			if(runTimePath == null || runTimePath.equals(""))
 				throw new Exception("C code generation failed since neither the run-time installation path nor the environemnt variable 'RUNTIME_HOME' is set.");
 						
 			calsimPath = Platform.getPreferencesService().getString("org.caltoopia.frontend.Cal", "calsim", null, null);
 			if(calsimPath == null || calsimPath.equals(CalRootPreferencePage.CALSIM_ENVIRONMENT_VARIABLE))
 				calsimPath = System.getenv(CalRootPreferencePage.CALSIM_ENVIRONMENT_VARIABLE);
 
 			systemcPath = Platform.getPreferencesService().getString("org.caltoopia.frontend.Cal", "systemc", null, null);
 			if(systemcPath == null || systemcPath.equals(CalRootPreferencePage.SYSTEMC_ENVIRONMENT_VARIABLE))
 				systemcPath = System.getenv(CalRootPreferencePage.SYSTEMC_ENVIRONMENT_VARIABLE);
 			
 			sdf3Path = Platform.getPreferencesService().getString("org.caltoopia.frontend.Cal", "sdf3", null, null);
 			if(sdf3Path == null || sdf3Path.equals(CalRootPreferencePage.SDF3_ENVIRONMENT_VARIABLE))
 				sdf3Path = System.getenv(CalRootPreferencePage.SDF3_ENVIRONMENT_VARIABLE);
 			
 			if(simulation){
 				if(calsimPath == null || calsimPath.equals(""))
 					throw new Exception("Simulation is selected, but neither the CALSim installation path nor the variable 'CALSIM_HOME' is set.");
 				
 				if(systemcPath == null || systemcPath.equals(""))
 					throw new Exception("Simulation is selected, but neither the SystemC installation path nor variable 'SYSTEMC_HOME' is set.");
 			}			
 			
 			if(analysis > 4){
 				if(sdf3Path == null || sdf3Path.equals(""))
 					throw new Exception("Classification with analysis is selected, but neither the SDF3 installation path nor the variable 'SDF3_HOME' is set.");
 			}
 		} catch(Exception e){
 			System.out.println(e.getMessage());
 			out.print("Error: " + e.getMessage());
 			return;
 		}
 		
 		CompilationSession session = new CompilationSession(paths, new ArrayList<String>(), output, new PrintStream(out), runTimePath, 
 				                                            true, System.getProperty("os.name").toLowerCase(), generateCdtProject,
 				                                            workingDirectory, runOptions, generateDot,debug);    		
 		session.setLaunchConfigName(configuration.getName());
 		session.setMonitor(monitor);
 		ActorDirectory.initCompilation(session);
 		
 		try { 
  			session.elaborateNetwork(top);
  			if(simulation){
  				cal2c.generate(top, session, new SimulationSession(calsimPath, systemcPath));								
  			} else {
  				cal2c.generate(top, session,null); 			
  			}
  				
  			if (analysis > 0){
  				Analysis networkAnalysis = new Analysis(session,top, analysis, sdf3Path); 	
  				networkAnalysis.analyze();
  				
  				if(visualize){
  					PluginAnalysisNetworkView panv = new PluginAnalysisNetworkView(analysis);
  					panv.view(session);
  				}
  			}
  			
  			if (analysis == 0 && visualize) {
  				PluginNetworkView panv = new PluginNetworkView();
  				panv.view(session);
  			}
  				
  		} catch (Exception x) {
  			out.println("Compilation aborted. Error: " + x.getMessage());
			Cal2C.generateErrorMakefile(session);
  			// x.printStackTrace();
  		} 		
 		
 		// Now turn it back on
 		if (autobuild) {
 			description.setAutoBuilding(true);			
 			workspace.setDescription(description);
 		}
 	}
 	
 	protected IProject[] getProjectsForProblemSearch(ILaunchConfiguration configuration, String mode) throws CoreException {
 		Set<IProject> projects = new HashSet<IProject>();
 		String projectName = configuration.getAttribute(CaltoopiaMainTab.CONFIG_ID_PROJECT, "");
 		
 		IWorkspaceRoot myWorkspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
 		IProject project = myWorkspaceRoot.getProject(projectName);
 		
 		addProjects(project, projects);
 		return  projects.toArray(new IProject[0]);
 	}
 	
 	private void addProjects(IProject project, Set<IProject> projects) {
 		projects.add(project);
 		try {
 			for (IProject p : project.getReferencedProjects()) {
 				if (!projects.contains(p)) {
 					addProjects(p, projects);
 				}
 			}
 		} catch (Exception x) {
 			//
 		}
 	}
 	
 	private MessageConsole findConsole(String name) {
 		ConsolePlugin plugin = ConsolePlugin.getDefault();
 		IConsoleManager conMan = plugin.getConsoleManager();
 		IConsole[] existing = conMan.getConsoles();
 		for (int i = 0; i < existing.length; i++) {
 			if (name.equals(existing[i].getName())) {
 				return (MessageConsole) existing[i];
 			}
 		}
 	    //no console found, so create a new one
 
 		MessageConsole myConsole = new MessageConsole(name, null);
 		conMan.addConsoles(new IConsole[]{myConsole});
 		return myConsole;
 	}	
 }
