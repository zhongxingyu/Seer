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
 import de.tuilmenau.ics.fog.IEvent;
 import de.tuilmenau.ics.fog.Worker;
 import de.tuilmenau.ics.fog.Config.Simulator.SimulatorMode;
 import de.tuilmenau.ics.fog.importer.ScenarioImporter;
 import de.tuilmenau.ics.fog.importer.ScenarioImporterExtensionPoint;
 import de.tuilmenau.ics.fog.scenario.ScenarioSetup;
 import de.tuilmenau.ics.fog.topology.Simulation;
 import de.tuilmenau.ics.fog.ui.Logging;
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
 	public static final String CONFIG_SCENARIO_CYCLES = "scenario_cycles";
 	public static final String CONFIG_SCENARIO_OPTIONS_DEFAULT = "";
 	public static final int CONFIG_SCENARIO_CYCLES_DEFAULT = 1;
 	
 	public static final String CONFIG_START_CMD = "command";
 	public static final String CONFIG_START_CMD_DEFAULT = null;
 
 	public static final String CONFIG_DIRECTORY = "directory";
 	public static final String CONFIG_DIRECTORY_DEFAULT = null;
 
 	public static final String  CONFIG_LINK_DATA_RATE = "link.datarate";
 	public static final int     CONFIG_LINK_DATA_RATE_DEFAULT = -1;
 	public static final String  CONFIG_LINK_DELAY = "link.delay";
 	public static final int     CONFIG_LINK_DELAY_DEFAULT = 0;
 	public static final String  CONFIG_LINK_DELAY_CONSTANT = "link.delay.constant";
 	public static final boolean CONFIG_LINK_DELAY_CONSTANT_DEFAULT = true;
 	public static final String  CONFIG_LINK_LOSS_PROB = "link.loss";
 	public static final int     CONFIG_LINK_LOSS_PROB_DEFAULT = 0;
 	public static final String  CONFIG_LINK_BIT_ERROR = "link.biterror";
 	public static final int     CONFIG_LINK_BIT_ERROR_DEFAULT = 0;
 
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
 	public static final String PARAMETER_OUTPUT_FILE_PREFIX = "outputprefix";
 	
 	private static final double START_COMMAND_DELAY_AFTER_SETUP_SEC = 5.0d;
 
 	private FoGLauncher mFoGLauncher = this;
 	
 	public FoGLauncher()
 	{
 		mLogger = Logging.getInstance();
 	}
 	
 	protected void create(Configuration pConfiguration) throws LauncherException
     {
 		if(mSimulation != null) {
 			throw new LauncherException(this, "Simulation already running.");
 		}
 		
 		// store configuration for other methods
 		if(pConfiguration != null){
 			mConfiguration = pConfiguration;
 		}
 		
 		// read configuration from Eclipse launch framework
 		String  baseDirectory   = mConfiguration.get(CONFIG_DIRECTORY, CONFIG_DIRECTORY_DEFAULT);
 		String  worker          = mConfiguration.get(CONFIG_WORKER, CONFIG_WORKER_DEFAULT);
 		Level   loglevel        = Level.valueOf(mConfiguration.get(CONFIG_LOG_LEVEL, CONFIG_LOG_LEVEL_DEFAULT));
 		String  configuratorRS  = mConfiguration.get(CONFIG_NODE_ROUTING_CONFIGURATOR, CONFIG_NODE_CONFIGURATOR_DEFAULT);
 		String  configuratorApp = mConfiguration.get(CONFIG_NODE_APPLICATION_CONFIGURATOR, CONFIG_NODE_CONFIGURATOR_DEFAULT);
 		
 		int linkDatarate = mConfiguration.get(CONFIG_LINK_DATA_RATE, CONFIG_LINK_DATA_RATE_DEFAULT);
 		int linkDelay = mConfiguration.get(CONFIG_LINK_DELAY, CONFIG_LINK_DELAY_DEFAULT);
 		boolean linkDelayConstant = mConfiguration.get(CONFIG_LINK_DELAY_CONSTANT, CONFIG_LINK_DELAY_CONSTANT_DEFAULT);
 		int linkLoss = mConfiguration.get(CONFIG_LINK_LOSS_PROB, CONFIG_LINK_LOSS_PROB_DEFAULT);
 		int linkBitError = mConfiguration.get(CONFIG_LINK_BIT_ERROR, CONFIG_LINK_BIT_ERROR_DEFAULT);
 		
 		// Overwrite log level with system properties
 		String logLevelParam = System.getProperty(CONFIG_LOG_LEVEL);
 		if(logLevelParam != null) {
 			try {
 				loglevel = Level.valueOf(logLevelParam);
 			}
 			catch(IllegalArgumentException exc) {
 				mLogger.err(this, "Can not override log level with value '" +logLevelParam +"' from system propterties.", exc);
 			}
 		}
 		
 		// ensure ending "/" of directory name
 		if(!baseDirectory.endsWith("/") && !baseDirectory.endsWith("\\")) {
 			baseDirectory += "/";
 		}
 		
 		// determine file prefix
 		String outputPrefix = System.getProperty(PARAMETER_OUTPUT_FILE_PREFIX);
 		if(outputPrefix == null) {
 			outputPrefix = Long.toString(System.currentTimeMillis()) +"_";
 		}
 		
 		baseDirectory = baseDirectory +outputPrefix;
 		
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
 						if(mSimulation != null) {
 							mSimulation.getLogger().err(this, "Uncaught exception in thread " +pThread +". Terminate simulation.", pError);
 							mSimulation.exit();
 						} else {
 							mLogger.err(this, "Uncaught exception in thread " +pThread +". No simulation running; terminate VM.", pError);
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
 		mSimulation = new Simulation(baseDirectory, loglevel);
 		
 		mObservers = getObservers(mSimulation);
 		notifyObservers(FUNCTION.CREATE);
 		
 		// switch to logger from simulation
 		// -> errors and parameters will appear in simulation log
 		mLogger = mSimulation.getLogger();
 
 		// output configuration
 		mLogger.log(this, CONFIG_DIRECTORY +": " +baseDirectory);
 		mLogger.log(this, CONFIG_WORKER +": " +worker);
 		mLogger.log(this, CONFIG_LOG_LEVEL +": " +loglevel);
 		mLogger.log(this, CONFIG_NODE_ROUTING_CONFIGURATOR +": " + configuratorRS);
 		mLogger.log(this, CONFIG_NODE_APPLICATION_CONFIGURATOR +": " + configuratorApp);
 		
 		mLogger.log(this, CONFIG_LINK_DATA_RATE +": " + linkDatarate +"kbit/s");
 		mLogger.log(this, CONFIG_LINK_DELAY +": " + linkDelay +"ms");
 		mLogger.log(this, CONFIG_LINK_DELAY_CONSTANT +": " + linkDelayConstant);
 		mLogger.log(this, CONFIG_LINK_LOSS_PROB +": " + linkLoss +"%");
 		mLogger.log(this, CONFIG_LINK_BIT_ERROR +": " + linkBitError +"%");
 		
 		// set configuration
 		mSimulation.getConfig().Scenario.ROUTING_CONFIGURATOR = configuratorRS;
 		mSimulation.getConfig().Scenario.APPLICATION_CONFIGURATOR = configuratorApp;
 		
 		mSimulation.getConfig().Scenario.DEFAULT_DATA_RATE_KBIT = linkDatarate;
 		mSimulation.getConfig().Scenario.DEFAULT_DELAY_MSEC = linkDelay;
 		mSimulation.getConfig().Scenario.DEFAULT_DELAY_CONSTANT = linkDelayConstant;
 		mSimulation.getConfig().Scenario.DEFAULT_PACKET_LOSS_PROP = linkLoss;
 		mSimulation.getConfig().Scenario.DEFAULT_BIT_ERROR_PROP = linkBitError;
 		
 		//
 		// Start watchdog if required
 		//
 		String watchdogSystemProperty = System.getProperty(PARAMETER_WATCHDOG_NAME);
 		if(watchdogSystemProperty != null) {
 			try {
 				new Watchdog(watchdogSystemProperty, mSimulation);
 			}
 			catch(IOException exc) {
 				mLogger.err(this, "Can not start watchdog " +watchdogSystemProperty +". Continuing without.", exc);
 			}
 		}
 		
 		//
 		// End
 		//
 		// do not block: Create a new thread waiting for the end... 
 		if(pConfiguration != null){
 			new Thread() {
 				public void run()
 				{
 					Thread.currentThread().setName("SimulationWaitForExt");
 	
 					do{
 						mSimulation.waitForExit();
 						
 						mLogger.info(this, "Simulation finished. Informing observers.");
 						notifyObservers(FUNCTION.ENDED);
 						
 						// store old list in order to enable re-start
 						// of simulation during FINISHED callback
 						LinkedList<SimulationObserver> oldObservers = mObservers;
 						
 						mObservers = null;
 						mSimulation = null;
 						
 						mLogger.info(this, "Inform observer about finished cleanup.");
 						notifyObservers(FUNCTION.FINISHED, oldObservers);	
 						oldObservers.clear();
 						mLogger.info(this, "############ SIMULATION END ###############");
 						
 						if(Simulation.remainingPlannedSimulations() > 0){
							mLogger.info(this, "############ SIMULATION RESTART - run: " + Simulation.mStartedSimulations + " ###############");
 							try{
 								mLogger.info(this, "   ..CREATE");
 								mFoGLauncher.create(null);
 								mLogger.info(this, "   ..INIT");
 								mFoGLauncher.init();
 								mLogger.info(this, "   ..START");
 								mFoGLauncher.start();
 							}catch(Exception exc) {
 								// write error to log
 								Simulation sim = getSim();
 								if(sim != null) {
 									sim.getLogger().err(this, "Error while RELAUNCHING. Terminating again.", exc);
 								}
 								
 								// terminate started simulation
 								terminate();
 								
 								Simulation.setPlannedSimulations(0);
 							}
 						}
 					}while(Simulation.remainingPlannedSimulations() > 0);				
 				}
 			}.start();
 		}else{
 			// we restarted ourself
 		}
     }
 	
 	/**
 	 * Inits (after create) a simulation with a scenario. Calls the importer to import a suitable scenario.
 	 */
 	protected void init() throws LauncherException
 	{
 		String  importerName = mConfiguration.get(FoGLauncher.CONFIG_SCENARIO_IMPORTER, FoGLauncher.CONFIG_SCENARIO_IMPORTER_DEFAULT);
 		String  file         = mConfiguration.get(FoGLauncher.CONFIG_SCENARIO_FILE, FoGLauncher.CONFIG_SCENARIO_FILE_DEFAULT);
 		String  options      = mConfiguration.get(FoGLauncher.CONFIG_SCENARIO_OPTIONS, FoGLauncher.CONFIG_SCENARIO_OPTIONS_DEFAULT);
 		int  cycles      	 = mConfiguration.get(FoGLauncher.CONFIG_SCENARIO_CYCLES, FoGLauncher.CONFIG_SCENARIO_CYCLES_DEFAULT);
 		
 		// debug check
 		if(mSimulation == null) {
 			throw new LauncherException(this, "Simulation not running.");
 		}
 
 		// override configuration from configuration with parameters of VM
 		String fileNameParam = System.getProperty(CONFIG_SCENARIO_FILE);
 		if(fileNameParam != null) {
 			file = fileNameParam;
 		}
 		
 		mLogger.log(this, CONFIG_SCENARIO_IMPORTER +": " +importerName);
 		mLogger.log(this, CONFIG_SCENARIO_FILE +": " +file);
 		mLogger.log(this, CONFIG_SCENARIO_OPTIONS +": " +options);
 		mLogger.log(this, CONFIG_SCENARIO_CYCLES + ":" + cycles);
 
 		notifyObservers(FUNCTION.INIT);
 
 		// use internal one if it is directly selected and if it is
 		// not set for any reasons.
 		if(FoGLauncher.CONFIG_SCENARIO_IMPORTER_DEFAULT.equals(importerName) || (importerName == null)) {
 			if(!ScenarioSetup.selectScenario(file, options, mSimulation)) {
 				throw new LauncherException(this, "Error during internal setup of scenario '" +file +"' and options " +options +".");
 			}
 		} else {
 			try {
 				ScenarioImporter importer = ScenarioImporterExtensionPoint.createImporter(importerName);
 				if(importer != null) {
 					try {
 						importer.importScenario(file, mSimulation, options);
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
 	protected void start() throws LauncherException
 	{
 		// debug check
 		if(mSimulation == null) {
 			throw new LauncherException(this, "Simulation not running.");
 		}
 		
 		//
 		// CONFIGURATION
 		//
 		String cmd    = mConfiguration.get(FoGLauncher.CONFIG_START_CMD, FoGLauncher.CONFIG_START_CMD);
 		int    exitAt = mConfiguration.get(FoGLauncher.CONFIG_EXIT_AT_SEC, FoGLauncher.CONFIG_EXIT_AT_SEC_DEFAULT);
 		int    cycles = mConfiguration.get(FoGLauncher.CONFIG_SCENARIO_CYCLES, FoGLauncher.CONFIG_SCENARIO_CYCLES_DEFAULT);
 		
 		// read configuration from VM parameters
 		String cmdSystemProperty = System.getProperty(PARAMETER_EXECUTE_CMD);
 
 		mLogger.log(this, CONFIG_START_CMD +": " +cmd);
 		mLogger.log(this, PARAMETER_EXECUTE_CMD +": " +cmdSystemProperty);
 		mLogger.log(this, CONFIG_EXIT_AT_SEC +": " +exitAt);
 		
 		notifyObservers(FUNCTION.START);
 
 		//
 		// EXIT command
 		//
 		if(exitAt >= 0) {
 			mSimulation.getTimeBase().scheduleIn(exitAt, new ExitEvent(mSimulation));
 		}
 		
 		//
 		// QUEUED EVENTS for simulation start (e.g., start HRM election/hierarchy creations)
 		//
 		if(mSimulation.getPendingEvents() != null) {
 			for(IEvent tEvent: mSimulation.getPendingEvents()) {
 				mSimulation.getLogger().log(this, "Scheduling election event");
 				mSimulation.getTimeBase().scheduleIn(0, tEvent);
 			}
 		}
 		
 		//
 		// RUN COMMAND
 		//
 		if(cmd != null) {
 			// is it not just an empty string?
 			if(!"".equals(cmd.trim())) {
 				mSimulation.getTimeBase().scheduleIn(START_COMMAND_DELAY_AFTER_SETUP_SEC, new CommandEvent(mSimulation, cmd));
 			}
 		}
 		if(cmdSystemProperty != null) {
 			// is it not just an empty string?
 			if(!"".equals(cmdSystemProperty.trim())) {
 				mSimulation.getTimeBase().scheduleIn(START_COMMAND_DELAY_AFTER_SETUP_SEC, new CommandEvent(mSimulation, cmdSystemProperty));
 			}
 		}
 	}
 	
 	/**
 	 * @return Simulation object or null, if no simulation is running
 	 */
 	public Simulation getSim()
 	{
 		return mSimulation;
 	}
 	
 	protected Configuration getConfig()
 	{
 		return mConfiguration;
 	}
 	
 	protected Logger getLogger()
 	{
 		return mLogger;
 	}
 	
 	/**
 	 * Terminates (old) simulation
 	 */
 	public void terminate()
 	{
 		if(mSimulation != null) {
 			mLogger.info(this, "Terminating currently running simulation.");
 			mSimulation.exit();
 			
 			// wait for thread to end simulation and to inform observers
 			while(mSimulation != null) {
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
 					mLogger.err(this, "Wrong config element " +element, tExc);
 				}
 			}
 		}
 		catch(Exception exc) {
 			mLogger.err(this, "Error while searching for simulation observers.", exc);
 		}
 		
 		return observers;
 	}
 	
 	protected enum FUNCTION { CREATE, INIT, START, ENDED, FINISHED };
 	
 	private void notifyObservers(FUNCTION func)
 	{
 		notifyObservers(func, mObservers);
 	}
 	
 	private void notifyObservers(FUNCTION func, LinkedList<SimulationObserver> observerList)
 	{
 		for(SimulationObserver obs : observerList) {
 			try {
 				switch(func) {
 				case CREATE:
 					obs.created(mSimulation);
 					break;
 				case INIT:
 					obs.init();
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
 				mLogger.err(this, "Observer '" +obs +"' had thrown an exception.", tExc);
 			}
 			catch(Error tErr) {
 				mLogger.err(this, "Observer '" +obs +"' had thrown an error.", tErr);
 			}
 		}
 	}
 
 	private Configuration mConfiguration;
 	private Logger mLogger;
 	private static Simulation mSimulation = null;
 	private LinkedList<SimulationObserver> mObservers = null;	
 }
