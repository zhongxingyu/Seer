 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator - Eclipse Launcher
  * Copyright (c) 2012, Integrated Communication Systems Group, TU Ilmenau.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html.
  ******************************************************************************/
 package de.tuilmenau.ics.fog.eclipse.launcher;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.debug.core.ILaunch;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
 
 import de.tuilmenau.ics.fog.launcher.FoGLauncher;
 import de.tuilmenau.ics.fog.topology.Simulation;
 import de.tuilmenau.ics.fog.ui.Logging;
 
 
 /**
  * Integrates launcher for simulations in the Eclipse launcher framework.
  * 
  * Plug-in is based on the following tutorial:
  * http://www.eclipse.org/articles/Article-Launch-Framework/launch.html
  */
 public class FoGLaunchConfigurationDelegate extends FoGLauncher implements ILaunchConfigurationDelegate
 {
 	private static final String PLUGIN_ID = "de.tuilmenau.ics.fog.eclipse.launcher";
 	
 	public static final String EXTENSION_NAME = "de.tuilmenau.ics.fog.launcher";
 	
 	public static final String CONFIG_DIRECTORY_BY_PROJECT = "project";
 	public static final String CONFIG_DIRECTORY_BY_PROJECT_DEFAULT = null;
 	
 	public static final String  CONFIG_TERMINATE_OLD = "terminate";
 	public static final boolean CONFIG_TERMINATE_OLD_DEFAULT = false;
 	
 	
 	public FoGLaunchConfigurationDelegate()
 	{
 		super();
 	}
 	
 	@Override
 	public void launch(ILaunchConfiguration configuration, 
             String mode, 
             ILaunch launch, 
             IProgressMonitor monitor) throws CoreException
     {
 		EclipseLaunchConfiguration config = new EclipseLaunchConfiguration(configuration);
 		
 		boolean terminate = config.get(FoGLaunchConfigurationDelegate.CONFIG_TERMINATE_OLD, FoGLaunchConfigurationDelegate.CONFIG_TERMINATE_OLD_DEFAULT);
 		
 		Logging.log(this, "Starting in mode: " +mode);
 		Logging.log(this, "Launch configuration name: " +configuration);
 		Logging.log(this, CONFIG_TERMINATE_OLD +": " +terminate);
 		
 		if(monitor == null)	monitor = new NullProgressMonitor();
 		
 		try {
 			monitor.beginTask(toString(), 4);
 
 			//
 			// TERMINATE OLD
 			//
 			monitor.setTaskName(FUNCTION.ENDED.toString());
 			if(terminate) {
 				terminate();
 			}
 			monitor.worked(1);
 
 			// simulation not already running?
 			if(getSim() == null) {
 				//
 				// CREATE SIMULATION
 				//
 				monitor.setTaskName(FUNCTION.CREATE.toString());
 				
 				// getting base directory for the files of the simulation
 				String project       = config.get(CONFIG_DIRECTORY_BY_PROJECT, CONFIG_DIRECTORY_BY_PROJECT_DEFAULT);
 				String baseDirectory = config.get(FoGLauncher.CONFIG_DIRECTORY, FoGLauncher.CONFIG_DIRECTORY_DEFAULT);
 				
 				if(baseDirectory == null) {
 					try {
 						IWorkspace workspace = ResourcesPlugin.getWorkspace();
 						baseDirectory = workspace.getRoot().getLocationURI().getPath();
 
 						// log to project and not to workspace?
 						// -> add relative path to project folder
 						if(project != null) {
 							IProject projectObj = workspace.getRoot().getProject(project);
 							
 							if(projectObj != null) {
 								getLogger().log(this, CONFIG_DIRECTORY_BY_PROJECT +": " +project);
 								
 								baseDirectory += projectObj.getFullPath().toString();
 							} else {
 								throw new CoreException(new Status(Status.ERROR, PLUGIN_ID, "Project '" +project +"' not found."));
 							}
 						}
 					}
 					catch(IllegalArgumentException exc) {
 						throw new CoreException(new Status(Status.ERROR, PLUGIN_ID, "Error while getting project '" +project +"' from Eclipse.", exc));
 					}
 					
 					Logging.log(this, "set new " +FoGLauncher.CONFIG_DIRECTORY +": " +baseDirectory);
 					config.set(FoGLauncher.CONFIG_DIRECTORY, baseDirectory);
 				}
 				
 				create(config);
 				monitor.worked(2);
 				
 				//
 				// INIT
 				//
 				monitor.setTaskName(FUNCTION.INIT.toString());
 				init();
 				monitor.worked(3);
 				
 				//
 				// START
 				//
 				monitor.setTaskName(FUNCTION.START.toString());
 				start();
 				monitor.worked(4);
 			} else {
				Logging.err(this, "Simulation is already running.");
 			}
 		}
 		catch(Exception exc) {
 			// write error to log
 			Simulation sim = getSim();
 			if(sim != null) {
 				sim.getLogger().err(this, "Error while launching. Terminating again.", exc);
 			}
 			
 			// terminate started simulation
 			terminate();
 			
 			throw new CoreException(new Status(Status.ERROR, PLUGIN_ID, "Error during launching configuration '" +configuration +"'.", exc));
 		}
 		finally {
 			monitor.done();
 		}
 	}
 	
 }
