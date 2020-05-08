 /*  	CASi Context Awareness Simulation Software
  *   Copyright (C) 2012  Moritz Bürger, Marvin Frick, Tobias Mende
  *
  *  This program is free software. It is licensed under the
  *  GNU Lesser General Public License with one clarification.
  *  
  *  You should have received a copy of the 
  *  GNU Lesser General Public License along with this program. 
  *  See the LICENSE.txt file in this projects root folder or visit
  *  <http://www.gnu.org/licenses/lgpl.html> for more details.
  */
 package de.uniluebeck.imis.casi;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Calendar;
 import java.util.Locale;
 import java.util.logging.FileHandler;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.swing.SwingUtilities;
 
 import de.uniluebeck.imis.casi.communication.ICommunicationHandler;
 import de.uniluebeck.imis.casi.communication.mack.MACKNetworkHandler;
 import de.uniluebeck.imis.casi.controller.MainController;
 import de.uniluebeck.imis.casi.generator.IWorldGenerator;
 import de.uniluebeck.imis.casi.logging.DevLogFormatter;
 import de.uniluebeck.imis.casi.logging.ExtendedConsoleHandler;
 import de.uniluebeck.imis.casi.logging.SimLogFormatter;
 import de.uniluebeck.imis.casi.ui.IMainView;
 import de.uniluebeck.imis.casi.ui.simplegui.MainViewSimpleGui;
 
 /**
  * CASi is the main class of the entire simulator. Modules like the
  * {@link ICommunicationHandler}, {@link IWorldGenerator} or the
  * {@link IMainView} can be exchanged here.
  * 
  * @author Tobias Mende
  * 
  */
 public class CASi {
 	/** The development logger */
 	private static final Logger log = Logger
 			.getLogger("de.uniluebeck.imis.casi");
 	/**
 	 * Default logger for logging simulation information. Should be used from
 	 * the whole project
 	 */
 	public static final Logger SIM_LOG = Logger.getLogger("SimulationLoggger");
 	/** flag for determining whether productive mode is on or off */
 	public static boolean DEV_MODE = true;
 	/** Enables really verbose output */
 	public static boolean VERBOSE = false;
 	/** Handlers for logging in file */
 	private static FileHandler devFileHandler, simFileHandler;
 	/** Handlers for logging on the console */
 	private static ExtendedConsoleHandler devConsoleHandler, simConsoleHandler;
 
 	/**
 	 * The starting point for the entire program, changes can be made here to
 	 * customize the simulator
 	 * 
 	 * @param args optional arguments: 
 	 * <ul>
 	 *  <li> Verbosity Flag (0,1)
 	 *  <li> DevMode Flag (0,1)
 	 * 	<li> NetworkConfig (String path)
 	 * </ul>
 	 */
 	public static void main(String[] args) {
 		// DON'T REMOVE THESE LINES:
 		setupFlags(args);
 		setupLogging();
 		logHeader();
 		
 		// DO WHAT YOU WANT:
 		final IWorldGenerator generator = new de.uniluebeck.imis.simulations.mate.generator.java.WorldGenerator();
 		Locale.setDefault(Locale.GERMAN);
 		final ICommunicationHandler networkHandler = generateCommunicationHandler(args);
 //		((MACKNetworkHandler)networkHandler).serializeSettings();
 		final IMainView mainView = new MainViewSimpleGui();
 		final MainController mc = new MainController(generator, networkHandler,
 				mainView);
 		
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				// Call the main controller and let it work:
 				mc.init();
 				mc.start();
 			}
 		});
 	}
 	
 	/**
 	 * Sets the DEV_MODE and VERBOSE flag according to the command line arguments
 	 * @param args the command line arguments
 	 */
 	private static void setupFlags(String[] args) {
 		if(args.length > 0) {
 			int value = Integer.parseInt(args[0]);
 			VERBOSE = (value > 0);
 		}
		if(args.length > 1) {
 			int value = Integer.parseInt(args[1]);
 			DEV_MODE = (value > 0);
 		}
 	}
 
 	/**
 	 * Generates a new CommunicationHandler depending on the provided arguments
 	 * @param args the command line arguments
 	 * @return a new communication handler
 	 */
 	private static ICommunicationHandler generateCommunicationHandler(
 			String[] args) {
 		final ICommunicationHandler networkHandler;
 		if(args.length > 2) {
 			networkHandler = new MACKNetworkHandler(args[2]);
 		} else {
 			networkHandler = new MACKNetworkHandler();
 		}
 		return networkHandler;
 	}
 	
 	/**
 	 * Writes the header to the simulation logger
 	 */
 	private static void logHeader() {
 		SIM_LOG.info("===================================================");
 		SIM_LOG.info("CASi - Context Awareness Simulator");
 		SIM_LOG.info("Simulation for testing the MACK Framework");
 		SIM_LOG.info("by Moritz Bürger, Marvin Frick and Tobias Mende");
 		SIM_LOG.info("---------------------------------------------------");
 		SIM_LOG.info("\tsettle back and enjoy the simulation :-)");
 		SIM_LOG.info("===================================================");
 	}
 
 	/**
 	 * Sets up the logging
 	 */
 	public static void setupLogging() {
 		// setup logging
 		log.setUseParentHandlers(false);
 		SIM_LOG.setUseParentHandlers(false);
 		log.setLevel(Level.ALL);
 		SIM_LOG.setLevel(Level.INFO);
 		configureDevelopmentLogging();
 		log.addHandler(devConsoleHandler);
 
 		// configure the lowest levels for different loggers
 		if (DEV_MODE) {
 			// use the dev console for sim logging only in dev mode
 			SIM_LOG.addHandler(devConsoleHandler);
 		} else {
 			// Create the sim log only in productive mode
 			configureSimulationLogging();
 			SIM_LOG.addHandler(simConsoleHandler);
 			if (simFileHandler != null) {
 				SIM_LOG.addHandler(simFileHandler);
 			}
 		}
 		// write simulation information to the dev log in every case
 		if (devFileHandler != null) {
 			log.addHandler(devFileHandler);
 			SIM_LOG.addHandler(devFileHandler);
 		}
 
 	}
 
 	/**
 	 * Configuring the development logging
 	 * 
 	 * @throws SecurityException
 	 *             if no write permissions are given for the log directory
 	 * @throws IOException
 	 *             if an error occurred while writing the log file
 	 */
 	private static void configureDevelopmentLogging() {
 		// Configure console logger
 		devConsoleHandler = new ExtendedConsoleHandler();
 		devConsoleHandler.setFormatter(new DevLogFormatter());
 
 		// Configure file logger
 		long time = Calendar.getInstance().getTimeInMillis();
 		try {
 			new File("./log").mkdir();
 			devFileHandler = new FileHandler(String.format("log/%d.log", time));
 			devFileHandler.setFormatter(new DevLogFormatter()); // Use
 																// HTMLFormatter
 			// for fancy output
 
 			if (DEV_MODE) {
 				// log everything to the log file
 				devFileHandler.setLevel(Level.ALL);
 			} else {
 				// define the behavior of the development handler in productive
 				// mode
 				// log everything important into the dev log file
 				devFileHandler.setLevel(Level.CONFIG);
 			}
 		} catch (Exception e) {
 			System.out.println("Es wird keine Protokolldatei erzeugt: " + e.getMessage());
 		}
 
 		if (DEV_MODE) {
 			// log more information on the console
 			if(VERBOSE) {
 				devConsoleHandler.setLevel(Level.ALL);
 			} else {
 				devConsoleHandler.setLevel(Level.INFO);
 			}
 		} else {
 			// define the behavior of the development handler in productive mode
 			devConsoleHandler.setLevel(Level.SEVERE); // show important errors
 			// on the console
 		}
 	}
 
 	/**
 	 * Configuring the simulation logging
 	 * 
 	 * @throws SecurityException
 	 *             if no write permissions are given for the log directory
 	 * @throws IOException
 	 *             if an error occurred while writing the log file
 	 */
 	private static void configureSimulationLogging() {
 		// Configure console logger
 		simConsoleHandler = new ExtendedConsoleHandler();
 		simConsoleHandler.setFormatter(new SimLogFormatter());
 
 		// Configure file logger
 		long time = Calendar.getInstance().getTimeInMillis();
 
 		try {
 			new File("./log").mkdir();
 			simFileHandler = new FileHandler(String.format("log/sim-%d.log",
 					time));
 			simFileHandler.setFormatter(new SimLogFormatter()); // Use
 																// HTMLFormatter
 			simFileHandler.setLevel(Level.ALL);
 		} catch (Exception e) {
 			System.out.println("Es wird keine Protokolldatei erzeugt: " + e.getMessage());
 		}
 		// for fancy output
 		simConsoleHandler.setLevel(Level.INFO);
 
 	}
 }
