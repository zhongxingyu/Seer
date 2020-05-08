  package com.gentics.cr;
 
  import java.io.File;
 import java.io.FileFilter;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Properties;
 import java.util.Vector;
 import java.util.Map.Entry;
 
 import org.apache.log4j.Logger;
 
 import com.gentics.cr.configuration.ConfigurationSettings;
 import com.gentics.cr.configuration.EnvironmentConfiguration;
 import com.gentics.cr.configuration.GenericConfiguration;
 import com.gentics.cr.util.CRUtil;
 import com.gentics.cr.util.RegexFileFilter;
 /**
  * Loads a configuration from a given file.
  * Last changed: $Date: 2010-04-01 15:24:02 +0200 (Do, 01 Apr 2010) $
  * @version $Revision: 541 $
  * @author $Author: supnig@constantinopel.at $
  *
  */
 public class CRConfigFileLoader extends CRConfigUtil {
 	/**
 	 * Config directory. (${com.gentics.portalnode.confpath}/rest/)
 	 */
 	private static final String CONFIG_DIR = EnvironmentConfiguration.getConfigPath()
 		+ "/rest/";
 	/**
 	 * Generated unique serial version id.
 	 */
 	private static final long serialVersionUID = -87744244157623456L;
 	/**
 	 * Log4j logger for error and debug messages.
 	 */
 	private static Logger log = Logger.getLogger(CRConfigFileLoader.class);
 	/**
 	 * webapproot for resolving in property values.
 	 */
 	private String webapproot;
 	
 	/**
 	 * Create new instance of CRConfigFileLoader.
 	 * @param name of config
 	 * @param givenWebapproot root directory of application
 	 * (config read fallback)
 	 */
 	public CRConfigFileLoader(final String name, final String givenWebapproot) {
 		this(name, givenWebapproot, "");
 	}
 	
 	/**
 	 * Load config from String with subdir.
 	 * @param name configuration name
 	 * @param givenWebapproot root directory of application
 	 * (config read fallback)
 	 * @param subdir sub directory of .../conf/gentics/rest/ to use in this
 	 * configuration.
 	 */
 	public CRConfigFileLoader(final String name, final String givenWebapproot,
 			final String subdir) {
 		super();
 		webapproot = givenWebapproot;
 		
 		//Load Environment Properties
 		EnvironmentConfiguration.loadEnvironmentProperties();
 		checkForSanity();
 		setName(name);
 		
 		//load default configuration
 		loadConfigFile(CONFIG_DIR + subdir
 				+ name + ".properties");
 		
 		//load environment specific configuration
 		String modePath = ConfigurationSettings.getConfigurationPath();
 		if (modePath != null && !"".equals(modePath)) {
 			loadConfigFile(CONFIG_DIR + subdir
 					+ modePath + name + ".properties");
 		}
 		
 		// initialize datasource with handle_props and dsprops
 		initDS();
 
 	}
 	
 	/**
 	 * Checks for common configuration mistakes.
 	 */
 	private void checkForSanity() {
 		String dir = CRUtil.resolveSystemProperties(CONFIG_DIR);
 		File confDir = new File(dir);
 		if (confDir.exists() && confDir.isDirectory()) { 
 			log.debug("Loading configuration from " + dir);
 		} else {
 			String errMsg = "CONFIGURATION ERROR: Configuration directory does"
 				+ " not seem to contain a"
 				+ " valid configuration. The directory " + dir
 				+ " must exist and contain valid configuration files.";
 			log.error(errMsg);
 			System.err.println(errMsg);
 		}
 	}
 
 	/**
 	 * Load the config file(s) for this instance.
 	 * @param path file system path to the configuration
 	 */
 	private void loadConfigFile(final String path) {
 		String errorMessage = "Could not load configuration file at: "
 			+ CRUtil.resolveSystemProperties(path) + "!";
 		try {
 			//LOAD SERVLET CONFIGURATION
 			String confpath = CRUtil.resolveSystemProperties(path);
 			java.io.File defaultConfigfile = new java.io.File(confpath);
 			String basename = defaultConfigfile.getName();
 			String dirname = defaultConfigfile.getParent();
 			Vector<String> configfiles = new Vector<String>(1);
 			if (defaultConfigfile.canRead()) {
 				configfiles.add(confpath);
 			}
 
 			//add all files matching the regex "name.*.properties"
 			java.io.File directory = new java.io.File(dirname);
 			FileFilter regexFilter = new RegexFileFilter(basename
 					.replaceAll("\\..*", "") + "\\.[^\\.]+\\.properties");
 			for (java.io.File file : directory.listFiles(regexFilter)) {
 				configfiles.add(file.getPath());
 			}
 
 			//load all found files into config
 			for (String file : configfiles) {
 				loadConfiguration(this, file, webapproot);
 			}
 			if (configfiles.size() == 0) {
 				throw new FileNotFoundException(
 						"Cannot find any valid configfile.");
 			}
 
 		} catch (FileNotFoundException e) {
 			log.error(errorMessage, e);
 		} catch (IOException e) {
 			log.error(errorMessage, e);
 		} catch (NullPointerException e) {
 			log.error(errorMessage, e);
 		}
 	}
 
 	/**
 	 * Loads a configuration file into a GenericConfig instance and resolves
 	 * system variables.
 	 * @param emptyConfig - configuration to load the file into.
 	 * @param path - path of the file to load
 	 * @param webapproot - root directory of the web application for resolving
 	 * ${webapproot} in property values.
 	 * @throws IOException - if configuration file cannot be read.
 	 */
 	public static void loadConfiguration(final GenericConfiguration emptyConfig,
 			final String path, final String webapproot) throws IOException {
 		Properties props = new Properties();
 		props.load(new FileInputStream(CRUtil.resolveSystemProperties(path)));
		loadConfiguration(emptyConfig, props, webapproot);
 	}
 	
 	/**
 	 * Loads configuration properties into a GenericConfig instance and resolves
 	 * system variables.
 	 * @param emptyConfig - configuration to load the properties into.
 	 * @param properties - properties to load into the configuration
 	 * @param webapproot - root directory of the web application for resolving
 	 * ${webapproot} in property values.
 	 * @throws IOException - if configuration file cannot be read.
 	 */
 	public static void loadConfiguration(final GenericConfiguration emptyConfig, Properties props, final String webapproot) {
 		for (Entry<Object, Object> entry : props.entrySet()) {
 			Object value = entry.getValue();
 			Object key = entry.getKey();
 			setProperty(emptyConfig, (String) key, (String) value, webapproot);
 		}
 	}
 
 	/**
 	 * Set a property for this configuration. Resolves system properties in
 	 * values.
 	 * @param config - configuration to set the property for
 	 * @param key - property to set
 	 * @param value - value to set for the property
 	 * @param webapproot - root directory of the webapplication. this is used to
 	 * resolve ${webapproot} in the property values.
 	 */
 	private static void setProperty(final GenericConfiguration config,
 			final String key, final String value, final String webapproot) {
 		//Resolve system properties, so that they can be used in config values
 		String resolvedValue = CRUtil.resolveSystemProperties((String) value);
 		//Replace webapproot in the properties values, so that this variable can
 		//be used
 		if (webapproot != null) {
 			resolvedValue = resolvedValue.replaceAll("\\$\\{webapproot\\}",
 					webapproot.replace('\\', '/'));
 		}
 		//Set the property
 		config.set(key, resolvedValue);
 		log.debug("CONFIG: " + key + " has been set to " + resolvedValue);
 	}
 
 }
