  package com.gentics.cr;
 
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
  * 
  * Last changed: $Date: 2010-04-01 15:24:02 +0200 (Do, 01 Apr 2010) $
  * @version $Revision: 541 $
  * @author $Author: supnig@constantinopel.at $
  *
  */
 public class CRConfigFileLoader extends CRConfigUtil {
 
   /**
    * 
    */
   private static final long serialVersionUID = -87744244157623456L;
   private static Logger log = Logger.getLogger(CRConfigFileLoader.class);
   private String instancename;
   private String webapproot;
   protected Properties dsprops = new Properties();
   protected Properties handle_props = new Properties();
   protected Properties cache_props = new Properties();
   
   /**
    * Create new instance of CRConfigFileLoader
    * @param name of config
    * @param webapproot root directory of application (config read fallback)
    */
   public CRConfigFileLoader(String name, String webapproot) {
     this(name, webapproot, "");
   }
   
   /**
    * Load config from String with subdir
    * @param name
    * @param webapproot
    * @param subdir
    */
   public CRConfigFileLoader(String name, String webapproot, String subdir) {
 
     super();
     this.instancename = name;
     this.webapproot = webapproot;
     
     //Load Environment Properties
     EnvironmentConfiguration.loadEnvironmentProperties();
     
     this.setName(this.instancename);
     
     //LOAD DEFAULT CONFIGURATION
     loadConfigFile("${com.gentics.portalnode.confpath}/rest/"+subdir+this.getName()+".properties");
     
     //LOAD ENVIRONMENT SPECIFIC CONFIGURATION
     String modePath = ConfigurationSettings.getConfigurationPath();
     if (modePath != null && !"".equals(modePath)) {
       loadConfigFile("${com.gentics.portalnode.confpath}/rest/"+subdir+modePath+this.getName()+".properties");
     }
     
     // INITIALIZE DATASOURCE WITH HANDLE_PROPS AND DSPROPS
     initDS();
 
   }
 
   /**
    * Load the config file(s) for this instance.
    * @param path file system path to the configuration
    */
   private void loadConfigFile(final String path) {
     String errorMessage = "Could not load configuration file at: "
       + CRUtil.resolveSystemProperties("${" + CRUtil.PORTALNODE_CONFPATH
           + "}/rest/" + this.getName() + ".properties") + "!";
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
       FileFilter regexFilter = new RegexFileFilter(
          basename.replaceAll("\\..*", "") + ".[^\\.]+.properties");
       for (java.io.File file : directory.listFiles(regexFilter)) {
         configfiles.add(file.getPath());
       }
 
       //load all found files into config
       for (String file : configfiles) {
         loadConfiguration(this, file, webapproot);
       }
       if (configfiles.size() == 0) {
         throw new FileNotFoundException("Cannot find any valid configfile.");
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
    * Loads a configuration file into a GenericConfig instance and resolves system variables
    * @param emptyConfig
    * @param path
    * @param webapproot
    * @throws IOException
    */
   public static void loadConfiguration(GenericConfiguration emptyConfig,String path,String webapproot) throws IOException
   {
     Properties props = new Properties();
     props.load(new FileInputStream(CRUtil.resolveSystemProperties(path)));
     for (Entry<Object,Object> entry:props.entrySet()) {
       Object value = entry.getValue();
       Object key = entry.getKey();
       setProperty(emptyConfig,(String)key, (String)value, webapproot);
     }
   }
 
   private static void setProperty(GenericConfiguration config,String key, String value, String webapproot) {
     //Resolve system properties, so that they can be used in config values
     value = CRUtil.resolveSystemProperties((String) value);
     //Replace webapproot in the properties values, so that this variable can be
     //used
     if (webapproot != null) {
       value = resolveProperty("\\$\\{webapproot\\}",
           webapproot.replace('\\', '/'), value);
     }
     //Set the property
     config.set(key, value);
     log.debug("CONFIG: " + key + " has been set to " + value);
   }
 
   protected static final String resolveProperty(final String pattern,
       final String replacement, String value) {
     //TODO check if it is really necessary to manipulate the value string or if
     //we can make a copy of it.
     value = value.replaceAll(pattern, replacement);
     return value;
   }
 
 }
