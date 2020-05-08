 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator - Launcher
  * Copyright (C) 2012, Integrated Communication Systems Group, TU Ilmenau.
  * 
  * This program and the accompanying materials are dual-licensed under either
  * the terms of the Eclipse Public License v1.0 as published by the Eclipse
  * Foundation
  *  
  *   or (per the licensee's choosing)
  *  
  * under the terms of the GNU General Public License version 2 as published
  * by the Free Software Foundation.
  ******************************************************************************/
 package de.tuilmenau.ics.fog.launcher;
 
 import java.io.IOException;
 import java.util.LinkedList;
 
 import de.tuilmenau.ics.extensionpoint.Extension;
 import de.tuilmenau.ics.extensionpoint.ExtensionRegistry;
 import de.tuilmenau.ics.fog.CommandEvent;
 import de.tuilmenau.ics.fog.Config;
 import de.tuilmenau.ics.fog.ExitEvent;
 import de.tuilmenau.ics.fog.Worker;
 import de.tuilmenau.ics.fog.Config.Simulator.SimulatorMode;
 import de.tuilmenau.ics.fog.importer.ScenarioImporter;
 import de.tuilmenau.ics.fog.importer.ScenarioImporterExtensionPoint;
 import de.tuilmenau.ics.fog.scenario.ScenarioSetup;
 import de.tuilmenau.ics.fog.topology.Simulation;
 import de.tuilmenau.ics.fog.ui.Logging.Level;
 import de.tuilmenau.ics.fog.util.Configuration;
 import de.tuilmenau.ics.fog.util.Logger;
 
 
 /**
  * Launcher for simulations. For each simulation, observer are created and informed
  * about the run status of the simulation (see extension point).
  * 
  * It takes a configuration and starts up a simulation according to the parameters
  * given in this configuration. The parameter names are available as static strings
  * in the class.
  */
 public class FoGLauncher
 {
 	private static final String EXTENSION_POINT_NAME = "de.tuilmenau.ics.fog.simulation";
 	
 	public static final String CONFIG_SCENARIO_IMPORTER = "scenario_importer";
 	public static final String CONFIG_SCENARIO_IMPORTER_DEFAULT = "internal";
 	public static final String CONFIG_SCENARIO_FILE = "scenario_file";
 	public static final String CONFIG_SCENARIO_FILE_DEFAULT = "2";
 	public static final String CONFIG_SCENARIO_OPTIONS = "scenario_options";
 	public static final String CONFIG_SCENARIO_OPTIONS_DEFAULT = "";
 	
 	public static final String CONFIG_START_CMD = "command";
 	public static final String CONFIG_START_CMD_DEFAULT = null;
 
 	public static final String CONFIG_DIRECTORY = "directory";
 	public static final String CONFIG_DIRECTORY_DEFAULT = null;
 	
 	public static final String CONFIG_WORKER = "worker";
 	public static final String CONFIG_WORKER_DEFAULT = "<DEFAULT>";
 	
 	public static final String CONFIG_LOG_LEVEL = "loglevel";
 	public static final String CONFIG_LOG_LEVEL_DEFAULT = Level.LOG.toString();
 	
 	public static final String  CONFIG_EXIT_AT_SEC = "exitAtSec";
 	public static final int     CONFIG_EXIT_AT_SEC_DEFAULT = -1;
 	
 	public static final String  CONFIG_NODE_ROUTING_CONFIGURATOR = "node.routing";
 	public static final String  CONFIG_NODE_APPLICATION_CONFIGURATOR = "node.application";
 	public static final String  CONFIG_NODE_CONFIGURATOR_DEFAULT = null;
 	
 	public static final String PARAMETER_EXECUTE_CMD = "execute";
 	public static final String PARAMETER_WATCHDOG_NAME = "watchdog";
 	
 	private static final double START_COMMAND_DELAY_AFTER_SETUP_SEC = 5.0d;
 
 	
 	public FoGLauncher(Logger parentLogger)
 	{
 		this.logger = new Logger(parentLogger);
 	}
 	
 	public void create(Configuration configuration) throws LauncherException
     {
 		if(sim != null) {
 			throw new LauncherException(this, "Simulation already running.");
 		}
 		
 		// store configuration for other methods
 		this.configuration = configuration;
 		
 		// read configuration from Eclipse launch framework
 		String  baseDirectory   = configuration.get(FoGLauncher.CONFIG_DIRECTORY, FoGLauncher.CONFIG_DIRECTORY_DEFAULT);
 		String  worker          = configuration.get(FoGLauncher.CONFIG_WORKER, FoGLauncher.CONFIG_WORKER_DEFAULT);
 		Level   loglevel        = Level.valueOf(configuration.get(FoGLauncher.CONFIG_LOG_LEVEL, FoGLauncher.CONFIG_LOG_LEVEL_DEFAULT));
 		String  configuratorRS  = configuration.get(FoGLauncher.CONFIG_NODE_ROUTING_CONFIGURATOR, FoGLauncher.CONFIG_NODE_CONFIGURATOR_DEFAULT);
 		String  configuratorApp = configuration.get(FoGLauncher.CONFIG_NODE_APPLICATION_CONFIGURATOR, FoGLauncher.CONFIG_NODE_CONFIGURATOR_DEFAULT);
 		
 		String logLevelParam = System.getProperty(CONFIG_LOG_LEVEL);
 		if(logLevelParam != null) {
 			try {
 				loglevel = Level.valueOf(logLevelParam);
 			}
 			catch(IllegalArgumentException exc) {
 				logger.err(this, "Can not override log level with value '" +logLevelParam +"' from system propterties.", exc);
 			}
 		}
 		
 		// output configuration
 		logger.log(this, CONFIG_DIRECTORY +": " +baseDirectory);
 		logger.log(this, CONFIG_WORKER +": " +worker);
 		logger.log(this, CONFIG_LOG_LEVEL +": " +loglevel);
 		logger.log(this, CONFIG_NODE_ROUTING_CONFIGURATOR +": " + configuratorRS);
 		logger.log(this, CONFIG_NODE_APPLICATION_CONFIGURATOR +": " + configuratorApp);
 
 		//
 		// CREATE LOCAL WORKER
 		//
 		if(CONFIG_WORKER_DEFAULT.equals(worker)) {
 			worker = "worker_" +System.currentTimeMillis();
 		}
 		Worker.createLocalWorker(worker);
 		
 		// set handler for errors like OutOfMemoryError
 		// default handling is to terminate the simulation
 		if(Config.Simulator.MODE == SimulatorMode.FAST_SIM) {
 			if(Thread.getDefaultUncaughtExceptionHandler() == null) {
 				Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
 					@Override
 					public void uncaughtException(Thread pThread, Throwable pError)
 					{
 						if(sim != null) {
 							sim.getLogger().err(this, "Uncaught exception in thread " +pThread +". Terminate simulation.", pError);
 							sim.exit();
 						} else {
 							logger.err(this, "Uncaught exception in thread " +pThread +". No simulation running; terminate VM.", pError);
 							System.exit(1);
 						}
 					}
 				});
 			}
 			// else: it is already set
 		}
 				
 		//
 		// CREATE SIMULATION
 		//
 		sim = new Simulation(baseDirectory, loglevel);
 		
 		// set configuration
 		sim.getConfig().Scenario.ROUTING_CONFIGURATOR = configuratorRS;
 		sim.getConfig().Scenario.APPLICATION_CONFIGURATOR = configuratorApp;
 		
 		observers = getObservers(sim);
 		
 		//
 		// Start watchdog if required
 		//
 		String watchdogSystemProperty = System.getProperty(PARAMETER_WATCHDOG_NAME);
 		if(watchdogSystemProperty != null) {
 			try {
 				new Watchdog(watchdogSystemProperty, sim);
 			}
 			catch(IOException exc) {
 				logger.err(this, "Can not start watchdog " +watchdogSystemProperty +". Continuing without.", exc);
 			}
 		}
     }
 	
 	/**
 	 * Inits (after create) a simulation with a scenario. Calls the importer to import a suitable scenario.
 	 */
 	public void init() throws LauncherException
 	{
 		String  importerName = configuration.get(FoGLauncher.CONFIG_SCENARIO_IMPORTER, FoGLauncher.CONFIG_SCENARIO_IMPORTER_DEFAULT);
 		String  file         = configuration.get(FoGLauncher.CONFIG_SCENARIO_FILE, FoGLauncher.CONFIG_SCENARIO_FILE_DEFAULT);
 		String  options      = configuration.get(FoGLauncher.CONFIG_SCENARIO_OPTIONS, FoGLauncher.CONFIG_SCENARIO_OPTIONS_DEFAULT);
 		
 		// override configuration from configuration with parameters of VM
 		String fileNameParam = System.getProperty(CONFIG_SCENARIO_FILE);
 		if(fileNameParam != null) {
 			file = fileNameParam;
 		}
 		
 		logger.log(this, CONFIG_SCENARIO_IMPORTER +": " +importerName);
 		logger.log(this, CONFIG_SCENARIO_FILE +": " +file);
 		logger.log(this, CONFIG_SCENARIO_OPTIONS +": " +options);
 
 		// debug check
 		if(sim == null) {
 			throw new LauncherException(this, "Simulation not running.");
 		}
 
 		notifyObservers(FUNCTION.INIT);
 
 		// use internal one if it is directly selected and if it is
 		// not set for any reasons.
 		if(FoGLauncher.CONFIG_SCENARIO_IMPORTER_DEFAULT.equals(importerName) || (importerName == null)) {
 			if(!ScenarioSetup.selectScenario(file, options, sim)) {
 				throw new LauncherException(this, "Error during internal setup of scenario '" +file +"' and options " +options +".");
 			}
 		} else {
 			try {
 				ScenarioImporter importer = ScenarioImporterExtensionPoint.createImporter(importerName);
 				if(importer != null) {
 					try {
 						importer.importScenario(file, sim, options);
 					}
 					catch(Exception exc) {
 						throw new LauncherException(this, "Error during import of scenario '" +file +"' and options " +options +" by importer " +importerName +".", exc);
 					}
 				} else {
 					LinkedList<String> importerList = ScenarioImporterExtensionPoint.getImporterNames();
 					StringBuilder availableOnes = new StringBuilder();
 					boolean firstOne = true;
 					for(String imp : importerList) {
 						if(firstOne) firstOne = false;
 						else availableOnes.append(", ");
 						
 						availableOnes.append(imp);
 					}
 					
 					throw new LauncherException(this, "Can not find importer with name '" +importerName +"'. Available importer are " +availableOnes.toString() +".");
 				}
 			}
 			catch(Exception exc) {
 				throw new LauncherException(this, "Can not create importer with name '" +importerName +"'.", exc);
 			}
 		}
 	}
 	
 	/**
 	 * Start (after init)
 	 */
 	public void start() throws LauncherException
 	{
 		// debug check
 		if(sim == null) {
 			throw new LauncherException(this, "Simulation not running.");
 		}
 		
 		//
 		// CONFIGURATION
 		//
 		String cmd    = configuration.get(FoGLauncher.CONFIG_START_CMD, FoGLauncher.CONFIG_START_CMD);
 		int    exitAt = configuration.get(FoGLauncher.CONFIG_EXIT_AT_SEC, FoGLauncher.CONFIG_EXIT_AT_SEC_DEFAULT);
 		
 		// read configuration from VM parameters
 		String cmdSystemProperty = System.getProperty(PARAMETER_EXECUTE_CMD);
 
 		logger.log(this, CONFIG_START_CMD +": " +cmd);
 		logger.log(this, PARAMETER_EXECUTE_CMD +": " +cmdSystemProperty);
 		logger.log(this, CONFIG_EXIT_AT_SEC +": " +exitAt);
 		
 		notifyObservers(FUNCTION.START);
 
 		if(exitAt >= 0) {
 			sim.getTimeBase().scheduleIn(exitAt, new ExitEvent(sim));
 		}
 		
 		//
 		// RUN COMMAND
 		//
 		if(cmd != null) {
 			// is it not just an empty string?
 			if(!"".equals(cmd.trim())) {
 				sim.getTimeBase().scheduleIn(START_COMMAND_DELAY_AFTER_SETUP_SEC, new CommandEvent(sim, cmd));
 			}
 		}
 		if(cmdSystemProperty != null) {
 			// is it not just an empty string?
 			if(!"".equals(cmdSystemProperty.trim())) {
 				sim.getTimeBase().scheduleIn(START_COMMAND_DELAY_AFTER_SETUP_SEC, new CommandEvent(sim, cmdSystemProperty));
 			}
 		}
 		
 		//
 		// END
 		//
 		// do not block: Create a new thread waiting for the end... 
 		new Thread() {
 			public void run()
 			{
 				sim.waitForExit();
 				
 				logger.info(this, "Simulation finished. Informing observers.");
 				notifyObservers(FUNCTION.ENDED);
 				
 				// store old list in order to enable re-start
 				// of simulation during FINISHED callback
 				LinkedList<SimulationObserver> oldObservers = observers;
 				
 				observers = null;
 				sim = null;
 				
 				logger.info(this, "Inform observer about finished cleanup.");
 				notifyObservers(FUNCTION.FINISHED, oldObservers);	
 				oldObservers.clear();
 			}
 		}.start();
 	}
 	
 	/**
 	 * @return Simulation object or null, if no simulation is running
 	 */
 	public Simulation getSim()
 	{
 		return sim;
 	}
 	
 	protected Configuration getConfig()
 	{
 		return configuration;
 	}
 	
 	protected Logger getLogger()
 	{
 		return logger;
 	}
 	
 	/**
 	 * Terminates (old) simulation
 	 */
 	public void terminate()
 	{
 		if(sim != null) {
 			logger.info(this, "Terminating currently running simulation.");
 			sim.exit();
 			
 			// wait for thread to end simulation and to inform observers
 			while(sim != null) {
 				try {
 					Thread.sleep(500);
 				}
 				catch(InterruptedException exc) {
 					// ignore
 				}
 			}
 		}
 	}
 	
 	private LinkedList<SimulationObserver> getObservers(Simulation sim)
 	{
 		LinkedList<SimulationObserver> observers = new LinkedList<SimulationObserver>();
 		
 		try {
 			Extension[] config = ExtensionRegistry.getInstance().getExtensionsFor(EXTENSION_POINT_NAME);
 			
 			for(Extension element : config) {
 				try {
 					Object obj = element.create("class");
 					String early = element.getAttribute("early");
 					boolean addFirst = false;
 					
 					if(early != null) {
 						addFirst = Boolean.parseBoolean(early);
 					}
 	
 					if((obj != null) && (obj instanceof SimulationObserver)) {
 						if(addFirst) {
 							observers.addFirst((SimulationObserver) obj);
 						} else {
 							observers.addLast((SimulationObserver) obj);
 						}
 					}
 				}
 				catch(Exception tExc) {
 					logger.err(this, "Wrong config element " +element, tExc);
 				}
 			}
 		}
 		catch(Exception exc) {
 			logger.err(this, "Error while searching for simulation observers.", exc);
 		}
 		
 		return observers;
 	}
 	
 	protected enum FUNCTION { CREATE, INIT, START, ENDED, FINISHED };
 	
 	private void notifyObservers(FUNCTION func)
 	{
 		notifyObservers(func, observers);
 	}
 	
 	private void notifyObservers(FUNCTION func, LinkedList<SimulationObserver> observerList)
 	{
 		for(SimulationObserver obs : observerList) {
 			try {
 				switch(func) {
 				case INIT:
 					obs.init(sim);
 					break;
 				case START:
 					obs.started();
 					break;
 				case ENDED:
 					obs.ended();
 					break;
 				case FINISHED:
 					obs.finished();
 					break;
				default:
					throw new RuntimeException(this +": function " +func +" can not be signaled to observers.");
 				}
 			}
 			catch(Exception tExc) {
 				logger.err(this, "Observer '" +obs +"' had thrown an exception.", tExc);
 			}
 			catch(Error tErr) {
 				logger.err(this, "Observer '" +obs +"' had thrown an error.", tErr);
 			}
 		}
 	}
 
 	private Configuration configuration;
 	private Logger logger;
 	private static Simulation sim = null;
 	private LinkedList<SimulationObserver> observers = null;	
 }
