 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator - Eclipse Console
  * Copyright (c) 2012, Integrated Communication Systems Group, TU Ilmenau.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html.
  ******************************************************************************/
 package de.tuilmenau.ics.fog.eclipse.console;
 
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchListener;
 import org.eclipse.ui.PlatformUI;
 
 import de.tuilmenau.ics.fog.Config;
 import de.tuilmenau.ics.fog.commands.CommandExecutor;
 import de.tuilmenau.ics.fog.launcher.SimulationObserver;
 import de.tuilmenau.ics.fog.topology.Simulation;
 import de.tuilmenau.ics.fog.ui.Logging;
 import de.tuilmenau.ics.fog.ui.Logging.Level;
 
 
 /**
  * Opens and closes a console for a running simulation.
  * 
  * Furthermore, it opens a global log console at the first time a 
  * simulation console is opened.
  */
 public class SimulationConsoleCreator implements SimulationObserver
 {
 	public SimulationConsoleCreator()
 	{
 		// Open global logging console at the very first time.
 		// Do not close it with a simulation, in order to show
 		// all log output to the user.
 		if(sLogConsoleErrWarn == null) {
 			if(Config.Logging.ECLIPSE_CONSOLE_LOG_FULL) {
 				sLogConsole = new ColoredEclipseConsoleLogObserver("FoG Logging");
 				Logging.getInstance().addLogObserver(sLogConsole);
 			}
 			
 			sLogConsoleErrWarn = new ColoredEclipseConsoleLogObserver("FoG Logging - Errors, warnings and infos");
 			sLogConsoleErrWarn.setLogLevel(Level.INFO);
 			Logging.getInstance().addLogObserver(sLogConsoleErrWarn);
 			
 			// close overall consoles only in case of closing workbench
 			PlatformUI.getWorkbench().addWorkbenchListener(new IWorkbenchListener() {
 				@Override
 				public boolean preShutdown(IWorkbench workbench, boolean forced)
 				{
 					return true;
 				}
 				
 				@Override
 				public void postShutdown(IWorkbench workbench)
 				{
 					if(sLogConsole != null) {
 						Logging.getInstance().removeLogObserver(sLogConsole);
 						sLogConsole.close();
 						sLogConsole = null;
 					}
 					
 					if(sLogConsoleErrWarn != null) {
 						Logging.getInstance().removeLogObserver(sLogConsoleErrWarn);
 						sLogConsoleErrWarn.close();
 						sLogConsoleErrWarn = null;
 					}
 				}
 			});
 		}
 		
 		mConsole = new EclipseConsoleLogObserver();
 	}
 	
 	static public ColoredEclipseConsoleLogObserver getLogConsoleErrWarn()
 	{
 		return sLogConsoleErrWarn;
 	}
 	
 	@Override
 	public void created(Simulation sim)
 	{
 		mConsole.setLogLevel(sim.getLogLevel());
 		
 		mConsole.open("Command console: " +sim);
 		mConsole.log(this, "Init new simulation: " +sim);
 
 		Logging.getInstance().addLogObserver(mConsole);
 		mCmdExe = new CommandExecutor(mConsole.getReader(), sim);
 	}
 	
 	public void init()
 	{
 		// nothing to do
 	}
 
 	@Override
 	public void started()
 	{
 		mConsole.log(this, "Start simulation");
 		mCmdExe.start();
 	}
 	
 	@Override
 	public void ended()
 	{
 		mCmdExe.exit();
 		
 		mConsole.log(this, "Simulation ended");
 		mConsole.close();
		Logging.getInstance().removeLogObserver(mConsole);
		mConsole = null;
 	}
 
 	@Override
 	public void finished()
 	{
 		// already ended; ignore it
 	}
 	
 	
 	private CommandExecutor mCmdExe;
 	private EclipseConsoleLogObserver mConsole;
 	
 	private static ColoredEclipseConsoleLogObserver sLogConsole = null;
 	private static ColoredEclipseConsoleLogObserver sLogConsoleErrWarn = null;
 }
