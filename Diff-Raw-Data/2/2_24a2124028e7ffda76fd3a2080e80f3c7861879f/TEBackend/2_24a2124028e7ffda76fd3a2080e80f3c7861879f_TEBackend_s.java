 /*
  *  Copyright (C) 2013, DECOIT GmbH
  *
  *	This file is part of VISA Topology-Editor.
  *
  *	VISA Topology-Editor is free software: you can redistribute it and/or modify
  *	it under the terms of the GNU General Public License as published by the
  *	Free Software Foundation, either version 3 of the License, or (at your option)
  *	any later version.
  *
  *	VISA Topology-Editor is distributed in the hope that it will be useful, but
  *	WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
  *	or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
  *	more details.
  *
  *	You should have received a copy of the GNU General Public License along with
  *	VISA Topology-Editor. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.decoit.visa;
 
 import java.io.IOException;
 import java.nio.file.Files;
 import java.nio.file.Paths;
 import java.nio.file.Path;
 import java.nio.file.attribute.PosixFilePermission;
 import java.nio.file.attribute.PosixFilePermissions;
 import java.util.Set;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 import de.decoit.visa.http.ajax.AJAXServer;
 import de.decoit.visa.http.ajax.ModificationQueue;
 import de.decoit.visa.ioconnector.IOConnector;
 import de.decoit.visa.ioconnector.IOToolException;
 import de.decoit.visa.rdf.RDFManager;
 import de.decoit.visa.rdf.RDFSourceException;
 import de.decoit.visa.topology.TopologyStorage;
 import de.decoit.visa.topology.VLAN;
 
 
 /**
  * This is the main class of the Topology-Editor. It contains the main method,
  * references to the globally used TopologyStorage and RDFManager instances,
  * several global utility methods and methods to receive global preferences.
  *
  * @author Thomas Rix
  */
 public class TEBackend {
 	/**
 	 * Global instance of the TopologyStorage
 	 */
 	public static TopologyStorage TOPOLOGY_STORAGE;
 
 	/**
 	 * Global instance of the RDFManager
 	 */
 	public static RDFManager RDF_MANAGER;
 
 	private static final int MB = 1024 * 1024;
 
 	private static AJAXServer ajaxServer;
 	private static Logger log;
 	private static Path exportPath;
 	private static Path importPath;
 	private static Dimension2D gridDimensions;
 	private static int cellSize;
 	private static int componentMargin;
 	private static String version = "GitHub Release 1";
 	private static String tdbStorage = "tdb";
 	private static IOConnector ioConn;
 
 
 	/**
 	 * Main-Routine
 	 *
 	 * @param args Command line arguments
 	 */
 	public static void main(String[] args) {
 		// Configure log4j system
 		PropertyConfigurator.configure("res/log4j.properties");
 		log = Logger.getLogger(TEBackend.class.getName());
 
 		try {
 			int argc = args.length;
 			boolean printHelp = false;
 			boolean printVersion = false;
 			boolean missingSSL = false;
 
 			// Default port for AJAX server is 8080
 			int serverPort = 8080;
 
 			String sslKeyStore = null;
 			String sslKeyStorePWD = null;
 			String sslTrustStore = null;
 			String sslTrustStorePWD = null;
 
 			// Process command line switches, terminate loop if help or version
 			// switches are found
 			for(int i = 0; i < argc && !printHelp && !printVersion; i++) {
 				if(log.isDebugEnabled()) {
 					StringBuilder sb = new StringBuilder("Processing command line parameter: ");
 					sb.append(args[i]);
 					log.debug(sb.toString());
 				}
 
 				switch(args[i]) {
 				// Print help message switch
 					case "--sslkey":
 						sslKeyStore = args[++i];
 						break;
 					case "--sslkeyp":
 						sslKeyStorePWD = args[++i];
 						break;
 					case "--ssltrust":
 						sslTrustStore = args[++i];
 						break;
 					case "--ssltrustp":
 						sslTrustStorePWD = args[++i];
 						break;
 					case "-h":
 					case "--help":
 						printHelp = true;
 						break;
 					// Change AJAX server port switch
 					case "-p":
 					case "--port":
 						serverPort = Integer.valueOf(args[++i]);
 						break;
 					// Print version switch
 					case "-v":
 					case "--version":
 						printVersion = true;
 						break;
 					// Unknown switch detected, print info and help message
 					default:
 						System.out.println("Unknown command line switch: " + args[i]);
 						System.out.println();
 						printHelp = true;
 				}
 			}
 
 			if(sslKeyStore == null || sslKeyStorePWD == null || sslTrustStore == null || sslTrustStorePWD == null) {
 				missingSSL = true;
 			}
 
 			if(printHelp) {
 				System.out.println("Usage information");
 				System.out.println("Command line switches:");
 				System.out.println("--sslkey [path]\t\tSpecify [path] to SSL keystore file");
 				System.out.println("--sslkeyp [pwd]\t\tUse password [pwd] to access SSL keystore");
 				System.out.println("--ssltrust [path]\tSpecify [path] to SSL truststore file");
 				System.out.println("--ssltrustp [pwd]\tUse password [pwd] to access SSL truststore");
 				System.out.println("-p [port],--port [port]\tAJAX server will listen on [port]");
 				System.out.println();
 				System.out.println("-v,--version\t\tPrint version information");
 				System.out.println("-h,--help\t\tPrint this message");
 			}
 			else if(printVersion) {
 				System.out.println("VISA Topologieeditor Backend");
 				System.out.println("Version: " + version);
 				System.out.println("(c)2012-2013, VISA-Konsortium");
 			}
 			else if(missingSSL) {
 				System.out.println("Missing SSL information");
 				System.out.println("Specification of SSL key- and truststore information is required! Please use the following command line switches:");
 				System.out.println("--sslkey [path]\t\tSpecify [path] to SSL keystore file");
 				System.out.println("--sslkeyp [pwd]\t\tUse password [pwd] to access SSL keystore");
 				System.out.println("--ssltrust [path]\tSpecify [path] to SSL truststore file");
 				System.out.println("--ssltrustp [pwd]\tUse password [pwd] to access SSL truststore");
 			}
 			else {
 				if(log.isDebugEnabled()) {
 					log.debug("Setting SSL properties:");
 					log.debug("KeyStore: " + sslKeyStore);
 					log.debug("KeyStorePWD: " + sslKeyStorePWD);
 					log.debug("TrustStore: " + sslTrustStore);
 					log.debug("TrustStorePWD: " + sslTrustStorePWD);
 				}
 				System.setProperty("javax.net.ssl.keyStore", sslKeyStore);
 				System.setProperty("javax.net.ssl.keyStorePassword", sslKeyStorePWD);
 				System.setProperty("javax.net.ssl.trustStore", sslTrustStore);
 				System.setProperty("javax.net.ssl.trustStorePassword", sslTrustStorePWD);
 
 				ioConn = null;
 
 				TEBackend.TOPOLOGY_STORAGE = TopologyStorage.getInstance();
 				TEBackend.RDF_MANAGER = new RDFManager(tdbStorage);
 
 				TEBackend.RDF_MANAGER.createEmptyModel("VISA Default Model");
 
 				if(log.isInfoEnabled()) {
 					log.info("Created empty RDF model");
 				}
 
 				TEBackend.ajaxServer = new AJAXServer(serverPort);
 				ajaxServer.run();
 			}
 		}
 		catch(Throwable ex) {
 			System.err.println("Exception caught in main(): [" + ex.getClass().getSimpleName() + "] " + ex.getMessage());
 			log.fatal("Exception caught in main(): [" + ex.getClass().getSimpleName() + "] " + ex.getMessage());
 			if(log.isDebugEnabled()) {
 				for(StackTraceElement ste : ex.getStackTrace()) {
 					log.debug(ste.toString());
 				}
 			}
 
 			try {
 				TEBackend.stopBackend();
 			}
 			catch(Throwable e) {
 				StringBuilder sb = new StringBuilder("Caught during shutdown: [");
 				sb.append(e.getClass().getSimpleName());
 				sb.append("] ");
 				sb.append(e.getMessage());
 				log.error(sb.toString());
 
 				if(log.isDebugEnabled()) {
 					for(StackTraceElement ste : e.getStackTrace()) {
 						log.debug(ste.toString());
 					}
 				}
 			}
 		}
 	}
 
 
 	/**
 	 * Remove all information stored in the backend. After completion of this
 	 * method the backend will be in a state as if it was just started.
 	 *
 	 * @throws IOException
 	 */
 	public static void clearBackend() throws IOException {
 		if(log.isInfoEnabled()) {
 			log.info("Backend reset (clear) requested");
 		}
 
 		TEBackend.TOPOLOGY_STORAGE.clear();
 		VLAN.resetColorChooser();
 
 		TEBackend.RDF_MANAGER.createEmptyModel("VISA Default Model");
 
 		if(log.isInfoEnabled()) {
 			log.info("Backend reset (clear) successful");
 		}
 	}
 
 
 	/**
 	 * Roll the backend back to a specific state that was saved before.
 	 *
 	 * @param pState Numeric ID of the state which will be restored
 	 * @throws RDFSourceException
 	 * @throws IOException
 	 */
 	public static void restoreBackend(int pState) throws RDFSourceException, IOException {
 		if(log.isInfoEnabled()) {
 			log.info("Backend reset (restore) requested");
 		}
 
 		TEBackend.TOPOLOGY_STORAGE.clear();
 		VLAN.resetColorChooser();
 
 		TEBackend.RDF_MANAGER.restore(pState);
 
 		if(log.isInfoEnabled()) {
 			log.info("Backend reset (restore) successful");
 		}
 	}
 
 
 	/**
 	 * Shutdown the backend by closing any open resources and stopping the AJAX
 	 * server. After doing that the application will terminate.
 	 *
 	 * @throws IOException
 	 * @throws IOToolException
 	 */
 	public static void stopBackend() throws IOException, IOToolException {
 		if(log.isInfoEnabled()) {
 			log.info("Backend termination requested");
 		}
 
 		// Close the socket to the IO-Tool
 		closeIOConnector(true);
 		if(TEBackend.ajaxServer != null) {
 			TEBackend.ajaxServer.shutdown();
 		}
 
 		RDF_MANAGER.close();
 
 		if(log.isInfoEnabled()) {
 			log.info("Backend terminated by stopBackend call");
 		}
 	}
 
 
 	/**
 	 * Set the export path for RDF/XML files
 	 *
 	 * @param pPath Absolute path to export folder
 	 * @throws IOException
 	 */
 	public static void setExportPath(String pPath) throws IOException {
 		Path outPath = Paths.get(pPath);
 
 		if(!Files.exists(outPath)) {
 			Files.createDirectory(outPath);
 		}
 		Set<PosixFilePermission> attrSet = PosixFilePermissions.fromString("rwxrwxrwx");
 		Files.setPosixFilePermissions(outPath, attrSet);
 
 		exportPath = outPath;
 
 		if(log.isDebugEnabled()) {
 			StringBuilder sb = new StringBuilder("Export path set to '");
 			sb.append(outPath.toString());
 			sb.append(" with permissions ");
 			sb.append(PosixFilePermissions.toString(Files.getPosixFilePermissions(outPath)));
 
 			log.debug(sb.toString());
 		}
 	}
 
 
 	/**
 	 * Return the path to the export folder
 	 *
 	 * @return Absolute path to export folder
 	 */
 	public static Path getExportPath() {
 		return exportPath;
 	}
 
 
 	/**
 	 * Set the import path for RDF/XML files
 	 *
 	 * @param pPath Absolute path to export folder
 	 * @throws IOException
 	 */
 	public static void setImportPath(String pPath) throws IOException {
 		Path inPath = Paths.get(pPath);
 
 		if(!Files.exists(inPath)) {
 			Files.createDirectory(inPath);
 		}
 		Set<PosixFilePermission> attrSet = PosixFilePermissions.fromString("rwxrwxrwx");
 		Files.setPosixFilePermissions(inPath, attrSet);
 
 		importPath = inPath;
 
 		if(log.isDebugEnabled()) {
 			StringBuilder sb = new StringBuilder("Import path set to '");
 			sb.append(inPath.toString());
 			sb.append(" with permissions ");
 			sb.append(PosixFilePermissions.toString(Files.getPosixFilePermissions(inPath)));
 
 			log.debug(sb.toString());
 		}
 	}
 
 
 	/**
 	 * Return the path to the export folder
 	 *
 	 * @return Absolute path to export folder
 	 */
 	public static Path getImportPath() {
 		return importPath;
 	}
 
 
 	/**
 	 * Set the dimensions of the editor grid of the frontend
 	 *
 	 * @param pX Horizontal dimension (number of grid cells)
 	 * @param pY Vertical dimension (number of grid cells)
 	 */
 	public static void setGridDimensions(int pX, int pY) {
 		gridDimensions = new Dimension2D(pX, pY);
 
 		if(log.isDebugEnabled()) {
 			StringBuilder sb = new StringBuilder("Grid dimensions set: x=");
 			sb.append(pX);
 			sb.append(", y=");
 			sb.append(pY);
 
 			log.debug(sb.toString());
 		}
 	}
 
 
 	/**
 	 * Return the dimensions of the editor grid of the frontend
 	 *
 	 * @return The dimensions of the editor grid of the frontend
 	 */
 	public static Dimension2D getGridDimensions() {
 		return gridDimensions;
 	}
 
 
 	/**
 	 * Set the size of a grid cell. Cells are squares so only one size
 	 * definition is needed.
 	 *
 	 * @param pSize Size of a grid cell, must be greater than 0
 	 */
 	public static void setCellSize(int pSize) {
 		if(pSize > 0) {
 			cellSize = pSize;
 
 			if(log.isDebugEnabled()) {
 				StringBuilder sb = new StringBuilder("Cell size set: ");
 				sb.append(cellSize);
 				sb.append("px");
 
 				log.debug(sb.toString());
 			}
 		}
 		else {
 			throw new IllegalArgumentException("Cell size cannot be zero or negative");
 		}
 	}
 
 
 	/**
 	 * Set the size of the margin left around all components on the editor grid.
 	 * This is used for the calculation of dragbox dimensions.
 	 *
 	 * @param pMargin Margin size in grid cells
 	 */
 	public static void setComponentMargin(int pMargin) {
 		if(pMargin >= 0) {
 			componentMargin = pMargin;
 
 			if(log.isDebugEnabled()) {
 				StringBuilder sb = new StringBuilder("Component margin size set: ");
 				sb.append(pMargin);
 
 				log.debug(sb.toString());
 			}
 		}
 		else {
 			throw new IllegalArgumentException("Component margin cannot be negative");
 		}
 	}
 
 
 	/**
 	 * Return the currently set component margin
 	 *
 	 * @return The currently set component margin
 	 */
 	public static int getComponentMargin() {
 		return componentMargin;
 	}
 
 
 	/**
 	 * Create a connection to the IO-Tool using the given address and port. If
 	 * there is any existing IO-Tool connection, it will be closed before
 	 * opening a new one.
 	 *
 	 * @param pHost Address of the IO-Tool, can either be a host name or an IP
 	 *            address
 	 * @param pPort Port which the IO-Tool listens on
 	 * @throws IOException
 	 * @throws IOToolException
 	 */
 	public static void createIOConnector(String pHost, int pPort) throws IOException, IOToolException {
 		if(ioConn != null) {
 			ioConn.disconnect(false);
 
 			if(log.isDebugEnabled()) {
 				log.debug("Connection to IO-Tool closed");
 			}
 
 			ioConn = null;
 		}
 
 		ioConn = new IOConnector(pHost, pPort);
 
 		if(log.isDebugEnabled()) {
 			log.debug("Connection to IO-Tool established");
 		}
 	}
 
 
 	/**
 	 * Return the currently connected IOConnector
 	 *
 	 * @return The currently connected IOConnector
 	 */
 	public static IOConnector getIOConnector() {
 		return ioConn;
 	}
 
 
 	/**
 	 * Close the currently connected IOConnector
 	 *
 	 * @param pForce
 	 * @throws IOException
 	 * @throws IOToolException
 	 */
 	public static void closeIOConnector(boolean pForce) throws IOException, IOToolException {
 		if(ioConn != null) {
 			ioConn.disconnect(pForce);
 			ioConn = null;
 
 			if(log.isDebugEnabled()) {
 				log.debug("Connection to IO-Tool closed");
 			}
 		}
 	}
 
 
 	/**
 	 * Wrapper to get a ModificationQueue from the AJAXServer. Prevents direct
 	 * access to the AJAXServer from the outside.
 	 *
 	 * @param pQueueID ID for this queue
 	 * @return A modification queue using the provided values as target
 	 */
 	public static ModificationQueue getModificationQueue(String pQueueID) {
 		return ajaxServer.getModificationQueue(pQueueID);
 	}
 
 
 	/**
 	 * Log the type, message and stack trace of a Throwable to the log file
 	 * using the provided Logger object. Type and message are logged with
 	 * severity error, the stack trace is logged as debug message and is only
 	 * printed to the file if debug logging is enabled for the used Logger.
 	 *
 	 * @param pEX Throwable which will be logged
 	 * @param pLog Logger object to use for logging
 	 */
 	public static synchronized void logException(Throwable pEX, Logger pLog) {
 		StringBuilder sb = new StringBuilder("Caught: [");
 		sb.append(pEX.getClass().getSimpleName());
 		sb.append("] ");
 		sb.append(pEX.getMessage());
 		pLog.error(sb.toString());
 
 		if(pLog.isDebugEnabled()) {
 			for(StackTraceElement ste : pEX.getStackTrace()) {
 				pLog.debug(ste.toString());
 			}
 		}
 	}
 
 
 	/**
 	 * Log the detailed memory usage of the backend to the log file. Uses
	 * loglevel DEBUG and will not log anything if that level is disabled.
 	 */
 	@SuppressWarnings("ucd")
 	public static void logMemoryUsage() {
 		if(log.isDebugEnabled()) {
 			long freeMemory = Runtime.getRuntime().freeMemory() / MB;
 			long totalMemory = Runtime.getRuntime().totalMemory() / MB;
 			long maxMemory = Runtime.getRuntime().maxMemory() / MB;
 
 			StringBuilder sb = new StringBuilder("Memory in use: ");
 			sb.append(totalMemory - freeMemory);
 			sb.append("MB");
 			log.debug(sb.toString());
 
 			sb = new StringBuilder("Memory free: ");
 			sb.append(freeMemory);
 			sb.append("MB");
 			log.debug(sb.toString());
 
 			sb = new StringBuilder("Total memory: ");
 			sb.append(totalMemory);
 			sb.append("MB");
 			log.debug(sb.toString());
 
 			sb = new StringBuilder("Maximum memory: ");
 			sb.append(maxMemory);
 			sb.append("MB");
 			log.debug(sb.toString());
 		}
 	}
 
 
 	/**
 	 * Empty private constructor, this class is not meant to be instantiated
 	 */
 	private TEBackend() {
 		/* Ignored */
 	}
 }
