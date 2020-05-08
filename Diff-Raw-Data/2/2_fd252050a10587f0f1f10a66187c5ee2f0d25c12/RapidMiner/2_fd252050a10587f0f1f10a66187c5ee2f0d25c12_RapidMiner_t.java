 /*
  *  RapidMiner
  *
  *  Copyright (C) 2001-2007 by Rapid-I and the contributors
  *
  *  Complete list of developers available at our web site:
  *
  *       http://rapid-i.com
  *
  *  This program is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU General Public License as 
  *  published by the Free Software Foundation; either version 2 of the
  *  License, or (at your option) any later version. 
  *
  *  This program is distributed in the hope that it will be useful, but
  *  WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  *  General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program; if not, write to the Free Software
  *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
  *  USA.
  */
 package com.rapidminer;
 
 import java.awt.Image;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.Locale;
 
 import javax.imageio.ImageIO;
 
 import com.rapidminer.gui.tools.SplashScreen;
 import com.rapidminer.parameter.ParameterType;
 import com.rapidminer.parameter.ParameterTypeBoolean;
 import com.rapidminer.parameter.ParameterTypeInt;
 import com.rapidminer.parameter.ParameterTypeString;
 import com.rapidminer.tools.LogService;
 import com.rapidminer.tools.ObjectVisualizerService;
 import com.rapidminer.tools.OperatorService;
 import com.rapidminer.tools.ParameterService;
 import com.rapidminer.tools.Tools;
 import com.rapidminer.tools.XMLException;
 import com.rapidminer.tools.XMLSerialization;
 import com.rapidminer.tools.jdbc.DatabaseService;
 import com.rapidminer.tools.plugin.Plugin;
 
 /**
  * Main program. Entry point for command line programm, GUI and wrappers. Please note 
  * that applications which use RapidMiner as a data mining library will have to invoke one of the 
  * init methods provided by this class before applying processes or operators.
  * Several init methods exist and choosing the correct one with optimal parameters
  * might drastically reduce runtime and / or initialization time.
  * 
  * @author Ingo Mierswa
  * @version $Id: RapidMiner.java,v 1.14 2007/07/18 12:17:30 ingomierswa Exp $
  */
 public class RapidMiner {
 
     // ---  GENERAL PROPERTIES  ---
     
     /** The name of the property indicating the home directory of RapidMiner. */
     public static final String PROPERTY_RAPIDMINER_HOME = "rapidminer.home";
 
     /** The name of the property indicating the version of RapidMiner. */
     public static final String PROPERTY_RAPIDMINER_VERSION = "rapidminer.version";
     
     /** The name of the property indicating the path to an additional operator description XML file. */
     public static final String PROPERTY_RAPIDMINER_OPERATORS_ADDITIONAL = "rapidminer.operators.additional";
 
     /** The name of the property indicating the path to an RC file (settings). */
     public static final String PROPERTY_RAPIDMINER_RC_FILE = "rapidminer.rcfile";
     
     /** The name of the property indicating the path to the Weka Jar file. */
     public static final String PROPERTY_RAPIDMINER_WEKA_JAR = "rapidminer.weka.jar";
 
     /** The name of the property indicating the path to the global logging file. */
     public static final String PROPERTY_RAPIDMINER_GLOBAL_LOG_FILE = "rapidminer.global.logging.file";
 
     /** The name of the property indicating the path to the global logging file. */
     public static final String PROPERTY_RAPIDMINER_GLOBAL_LOG_VERBOSITY = "rapidminer.global.logging.verbosity";
     
     // ---  INIT PROPERTIES  ---
     
     /** A file path to an operator description XML file. */
     public static final String PROPERTY_RAPIDMINER_INIT_OPERATORS = "rapidminer.init.operators";
 
     /** A file path to the directory containing the plugin Jar files. */
     public static final String PROPERTY_RAPIDMINER_INIT_PLUGINS_LOCATION = "rapidminer.init.plugins.location";
     
     /** Boolean parameter indicating if the operators based on Weka should be initialized. */
     public static final String PROPERTY_RAPIDMINER_INIT_WEKA = "rapidminer.init.weka";
     
     /** Boolean parameter indicating if the drivers located in the lib directory of RapidMiner should be initialized. */
     public static final String PROPERTY_RAPIDMINER_INIT_JDBC_LIB = "rapidminer.init.jdbc.lib";
     
     /** Boolean parameter indicating if the drivers located somewhere in the classpath should be initialized. */
     public static final String PROPERTY_RAPIDMINER_INIT_JDBC_CLASSPATH = "rapidminer.init.jdbc.classpath";
     
     /** Boolean parameter indicating if the plugins should be initialized at all. */
     public static final String PROPERTY_RAPIDMINER_INIT_PLUGINS = "rapidminer.init.plugins";
     
     
     // ---  OTHER PROPERTIES  ---
     
     /** The property name for &quot;The number of fraction digits of formatted numbers.&quot; */
     public static final String PROPERTY_RAPIDMINER_GENERAL_FRACTIONDIGITS_NUMBERS = "rapidminer.general.fractiondigits.numbers";
 
     /** The property name for &quot;The number of fraction digits of formatted percent values.&quot; */
     public static final String PROPERTY_RAPIDMINER_GENERAL_FRACTIONDIGITS_PERCENT = "rapidminer.general.fractiondigits.percent";
     
 	/** The property name for &quot;Path to external Java editor. %f is replaced by filename and %l by the linenumber.&quot; */
 	public static final String PROPERTY_RAPIDMINER_TOOLS_EDITOR = "rapidminer.tools.editor";
 
 	/** The property name for &quot;Path to sendmail. Used for email notifications.&quot; */
 	public static final String PROPERTY_RAPIDMINER_TOOLS_SENDMAIL_COMMAND = "rapidminer.tools.sendmail.command";
 
 	/** The property name for &quot;Use unix special characters for logfile highlighting (requires new RapidMiner instance).&quot; */
 	public static final String PROPERTY_RAPIDMINER_GENERAL_LOGFILE_FORMAT = "rapidminer.general.logfile.format";
 
 	/** The property name for &quot;Indicates if RapidMiner should be used in debug mode (print exception stacks and shows more technical error messages)&quot; */
 	public static final String PROPERTY_RAPIDMINER_GENERAL_DEBUGMODE = "rapidminer.general.debugmode";
     
     /** The name of the property indicating the default encoding for files. */
     public static final String PROPERTY_RAPIDMINER_GENERAL_DEFAULT_ENCODING = "rapidminer.general.encoding";
     
     
 	/**
 	 * A set of some non-gui and operator related system properties (starting with "rapidminer."). Properties
 	 * can be registered using {@link RapidMiner#registerRapidMinerProperty(ParameterType)}.
 	 */
 	private static final java.util.Set<ParameterType> PROPERTY_TYPES = new java.util.TreeSet<ParameterType>();
 
 	static {
 		System.setProperty(PROPERTY_RAPIDMINER_VERSION, RapidMiner.getVersion());
         registerRapidMinerProperty(new ParameterTypeInt(PROPERTY_RAPIDMINER_GENERAL_FRACTIONDIGITS_NUMBERS, "The number of fraction digits of formatted numbers.", 0, Integer.MAX_VALUE, 3));
         registerRapidMinerProperty(new ParameterTypeInt(PROPERTY_RAPIDMINER_GENERAL_FRACTIONDIGITS_PERCENT, "The number of fraction digits of formatted percent values.", 0, Integer.MAX_VALUE, 2));
 		registerRapidMinerProperty(new ParameterTypeString(PROPERTY_RAPIDMINER_TOOLS_EDITOR, "Path to external Java editor. %f is replaced by filename and %l by the linenumber.", true));
 		registerRapidMinerProperty(new ParameterTypeString(PROPERTY_RAPIDMINER_TOOLS_SENDMAIL_COMMAND, "Path to sendmail. Used for email notifications.", true));
 		registerRapidMinerProperty(new ParameterTypeBoolean(PROPERTY_RAPIDMINER_GENERAL_LOGFILE_FORMAT, "Use unix special characters for logfile highlighting (requires new RapidMiner instance).", false));
 		registerRapidMinerProperty(new ParameterTypeBoolean(PROPERTY_RAPIDMINER_GENERAL_DEBUGMODE, "Indicates if RapidMiner should be used in debug mode (print exception stacks and shows more technical error messages)", false));
 		registerRapidMinerProperty(new ParameterTypeString(PROPERTY_RAPIDMINER_GENERAL_DEFAULT_ENCODING, "The default encoding used for file operations.", "UTF-8"));
 	}
 	
 	private static InputHandler inputHandler = new ConsoleInputHandler();
 	
 	private static SplashScreen splashScreen;
 	
 	public static String getVersion() {
 		return Version.getVersion();
 	}
 	
 	/**
 	 * @deprecated Use {@link #readProcessFile(File)} instead
 	 */
 	@Deprecated
 	public static Process readExperimentFile(File experimentfile) throws XMLException, IOException, InstantiationException, IllegalAccessException {
 		return readProcessFile(experimentfile);
 	}
 
 	public static Process readProcessFile(File processFile) throws XMLException, IOException, InstantiationException, IllegalAccessException {
 		try {
 			LogService.getGlobal().log("Reading process file '" + processFile + "'.", LogService.STATUS);
 			if (!processFile.exists() || !processFile.canRead()) {
 				LogService.getGlobal().log("Cannot read config file '" + processFile + "'!", LogService.FATAL);
 			}
 			return new Process(processFile);
 		} catch (XMLException e) {
 			throw new XMLException(processFile.getName() + ":" + e.getMessage());
 		}
 	}
 
 	/**
 	 * Initializes RapidMiner.
 	 * 
 	 * @param operatorsXMLStream the stream to the operators.xml (operator description), use core operators.xml if null
 	 * @param pluginDir the directory where plugins are located, use core plugin directory if null
 	 * @param addWekaOperators inidcates if the operator wrappers for Weka should be loaded
 	 * @param searchJDBCInLibDir indicates if JDBC drivers from the directory RAPID_MINER_HOME/lib/jdbc should be loaded
 	 * @param searchJDBCInClasspath indicates if JDBC drivers from the classpath libraries should be loaded
 	 * @param addPlugins indicates if the plugins should be loaded 
 	 * @throws IOException if something goes wrong during initialization
 	 */
 	public static void init(InputStream operatorsXMLStream, 
 			                File pluginDir, 
 			                boolean addWekaOperators, 
 			                boolean searchJDBCInLibDir, 
 			                boolean searchJDBCInClasspath, 
 			                boolean addPlugins) throws IOException {		
 	    // set locale fix to US
 	    RapidMiner.splashMessage("Using US Local");
 	    Locale.setDefault(Locale.US);
 	    
 		// ensure rapidminer.home is set
 	    RapidMiner.splashMessage("Ensure RapidMiner Home is set");
 		ParameterService.ensureRapidMinerHomeSet();
 		
 		RapidMiner.splashMessage("Init Setup");
 		File wekaJar = ParameterService.getLibraryFile("weka.jar");
 		String wekaMessage = addWekaOperators + "";
 		if ((wekaJar == null) || (!wekaJar.exists())) {
 			wekaMessage = "weka not found";
 		}
 		LogService.getGlobal().log("Initialization Settings", LogService.INIT);
 		LogService.getGlobal().log("----------------------------------------------------", LogService.INIT);
 		LogService.getGlobal().log("Load " + (operatorsXMLStream == null ? "core" : "specific") + " operators...", LogService.INIT);
 	    LogService.getGlobal().log("Load Weka operators: " + wekaMessage, LogService.INIT);
 		LogService.getGlobal().log("Load JDBC drivers from lib directory: " + searchJDBCInLibDir, LogService.INIT);
 		LogService.getGlobal().log("Load JDBC drivers from classpath: " + searchJDBCInClasspath, LogService.INIT);
 	    LogService.getGlobal().log("Load plugins: " + addPlugins, LogService.INIT);
 	    LogService.getGlobal().log("Load plugins from '" + (pluginDir == null ? ParameterService.getPluginDir() : pluginDir) + "'", LogService.INIT);
 	    LogService.getGlobal().log("----------------------------------------------------", LogService.INIT);
 	    
 	    RapidMiner.splashMessage("Initialising Operators");
 		ParameterService.init(operatorsXMLStream, addWekaOperators);
 		
 	    RapidMiner.splashMessage("Loading JDBC Drivers");
 	    DatabaseService.init(searchJDBCInLibDir, searchJDBCInClasspath);
 	    
 	    if (addPlugins) {
 	    	RapidMiner.splashMessage("Register Plugins");
 	    	Plugin.registerAllPlugins(pluginDir);
 	    }
 		
 		RapidMiner.splashMessage("Initialize XML serialization");
 		XMLSerialization.init(Plugin.getMajorClassLoader());
 		
 		RapidMiner.splashMessage("Define XML Serialization Alias Pairs");
 		OperatorService.defineXMLAliasPairs();
 	}
 
 	/**
 	 * Initializes RapidMiner.
 	 * 
 	 * @param operatorsXMLStream the stream to the operators.xml (operator description), use core operators.xml if null
 	 * @param addWekaOperators inidcates if the operator wrappers for Weka should be loaded
 	 * @param searchJDBCInLibDir indicates if JDBC drivers from the directory RAPID_MINER_HOME/lib/jdbc should be loaded
 	 * @param searchJDBCInClasspath indicates if JDBC drivers from the classpath libraries should be loaded
 	 * @param addPlugins indicates if the plugins should be loaded 
 	 * @throws IOException if something goes wrong during initialization
 	 */
 	public static void init(InputStream operatorsXMLStream, 
 			                boolean addWekaOperators, 
 			                boolean searchJDBCInLibDir, 
 			                boolean searchJDBCInClasspath, 
 			                boolean addPlugins) throws IOException {
		init(operatorsXMLStream, null, addWekaOperators, searchJDBCInLibDir, searchJDBCInClasspath, addPlugins);
 	}
 
 	/**
 	 * Initializes RapidMiner with its core operators.
 	 * 
 	 * @param addWekaOperators inidcates if the operator wrappers for Weka should be loaded
 	 * @param searchJDBCInLibDir indicates if JDBC drivers from the directory RAPID_MINER_HOME/lib/jdbc should be loaded
 	 * @param searchJDBCInClasspath indicates if JDBC drivers from the classpath libraries should be loaded
 	 * @param addPlugins indicates if the plugins should be loaded
 	 * @throws IOException if something goes wrong during initialization
 	 */
 	public static void init(boolean addWekaOperators, boolean searchJDBCInLibDir, boolean searchJDBCInClasspath, boolean addPlugins) throws IOException {
 		init(null, addWekaOperators, searchJDBCInLibDir, searchJDBCInClasspath, addPlugins);
 	}
 
 	/**
 	 * Initializes RapidMiner. Will use the core operators.xml operator description, all 
 	 * available Weka operators, and all JDBC drivers found in the directory 
 	 * RAPID_MINER_HOME/lib/jdbc. Will not search for JDBC drivers in other classpath
 	 * libraries. Will use all plugins in the plugins directory. 
 	 * Use the method {@link #init(InputStream, File, boolean, boolean, boolean, boolean)}
 	 * for more sophisticated initialization possibilities. Alternatively, you could
 	 * also set the following system properties, e.g. during startup via 
 	 * &quot;-Drapidminer.init.weka=false&quot;:
 	 * <ul>
 	 * <li>rapidminer.init.operators</li>
 	 * <li>rapidminer.init.plugins.location</li>
 	 * <li>rapidminer.init.weka</li>
 	 * <li>rapidminer.init.jdbc.lib</li>
 	 * <li>rapidminer.init.jdbc.classpath</li>
 	 * <li>rapidminer.init.plugins</li>
 	 * </ul>
 	 * 
 	 * @throws IOException if something goes wrong during initialization
 	 */
 	public static void init() throws IOException {
 		InputStream operatorStream = null;
 		String operatorsXML = System.getProperty(PROPERTY_RAPIDMINER_INIT_OPERATORS);
 		if (operatorsXML != null) {
 			operatorStream = new FileInputStream(operatorsXML);
 		}
 		
 		File pluginDir = null;
 		String pluginDirString = System.getProperty(PROPERTY_RAPIDMINER_INIT_PLUGINS_LOCATION);
 		if (pluginDirString != null)
 			pluginDir = new File(pluginDirString);
 		
 	    String loadWekaString = System.getProperty(PROPERTY_RAPIDMINER_INIT_WEKA);
 	    boolean loadWeka = Tools.booleanValue(loadWekaString, true);
 	    
 	    String loadJDBCDirString = System.getProperty(PROPERTY_RAPIDMINER_INIT_JDBC_LIB);
 	    boolean loadJDBCDir = Tools.booleanValue(loadJDBCDirString, true);
 	    
 	    String loadJDBCClasspathString = System.getProperty(PROPERTY_RAPIDMINER_INIT_JDBC_CLASSPATH);
 	    boolean loadJDBCClasspath = Tools.booleanValue(loadJDBCClasspathString, false);
 	
 	    String loadPluginsString = System.getProperty(PROPERTY_RAPIDMINER_INIT_PLUGINS);
 	    boolean loadPlugins = Tools.booleanValue(loadPluginsString, true);
 	    
 		init(operatorStream, pluginDir, loadWeka, loadJDBCDir, loadJDBCClasspath, loadPlugins);
 		
 		if (operatorStream != null)
 			operatorStream.close();
 	}
 
 	/** Cleans up the object visualizers available for this process and clears the 
 	 *  current temp directory. This method should be performed in cases where RapidMiner
 	 *  is embedded into other applications and only single operators (in contrast to
 	 *  a complete process) are performed within several runs, e.g. in a loop.
 	 *  
 	 *  TODO: bind object visualizers and temp file service to a 
 	 *  process instead of managing these things in a static way.
 	 */
 	public static void cleanUp() {
 	    ObjectVisualizerService.clearVisualizers();
 	}
 
 	public static SplashScreen showSplash() {
 		try {
 			URL url = Tools.getResource("rapidminer_logo.png");
 			Image logo = null;
 			if (url != null) {
 				logo = ImageIO.read(url);
 			}
 			RapidMiner.splashScreen = new SplashScreen("RapidMiner", getVersion(), logo);
 			RapidMiner.splashScreen.showSplashScreen();
 			return RapidMiner.splashScreen;
 		} catch (Exception e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	public static void hideSplash() {
 		RapidMiner.splashScreen.dispose();
 	}
 
 	public static void splashMessage(String message) {
 		if (RapidMiner.splashScreen != null) {
 			RapidMiner.splashScreen.setMessage(message);
 		}
 	}
 
 	public static void setInputHandler(InputHandler inputHandler) {
 		RapidMiner.inputHandler = inputHandler;
 	}
 
 	public static InputHandler getInputHandler() {
 		return inputHandler;
 	}
 
 	/** Returns a set of {@link ParameterType}s for the RapidMiner system properties. 
 	 * @deprecated Use {@link #getRapidMinerProperties()} instead*/
 	@Deprecated
 	public static java.util.Set<ParameterType> getYaleProperties() {
 		return getRapidMinerProperties();
 	}
 
 	/** Returns a set of {@link ParameterType}s for the RapidMiner system properties. */
 	public static java.util.Set<ParameterType> getRapidMinerProperties() {
 		return PROPERTY_TYPES;
 	}
 
 	/**
 	 * @deprecated Use {@link #registerRapidMinerProperty(ParameterType)} instead
 	 */
 	@Deprecated
 	public static void registerYaleProperty(ParameterType type) {
 		registerRapidMinerProperty(type);
 	}
 
 	public static void registerRapidMinerProperty(ParameterType type) {
 		PROPERTY_TYPES.add(type);
 	}
 
 	public static void quit(int errorcode) {
 		Runtime.getRuntime().runFinalization();
 		System.exit(errorcode);
 	}
 }
