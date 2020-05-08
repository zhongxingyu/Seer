 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package mecard.config;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.Map;
 import java.util.Properties;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author andrew
  */
 public class PropertyReader
 {
    public final static String VERSION           = "0.8.8_28"; // server version
     /** Including this tag with a value like 'user&#64;server.com', will cause 
      * commands to be run remotely through secure shell (ssh).
      * The tag is optional. Leaving it out means 
      * you expect to run the commands on the ILS server itself.
      */
     public final static String SSH_TAG           = "ssh";
     public final static String LOAD_DIR          = "load-dir";
     private static String CONFIG_DIR             = "";
     private static String BIMPORT_PROPERTY_FILE  = "bimport.properties";
     private static String SYMPHONY_PROPERTY_FILE = "symphony.properties";
     private static String POLARIS_PROPERTY_FILE  = "polaris.properties";
     private static String ENVIRONMENT_FILE       = "environment.properties";
     private static String SIP2_FILE              = "sip2.properties";
     private static String BIMPORT_CITY_MAPPING   = "city_st.properties";
     private static String DEBUG_SETTINGS_FILE    = "debug.properties";
     private static String VARIABLES_FILE         = "sysvar.properties"; // these are system specific variables, like PATH.
     private static String MESSAGES_PROPERTY_FILE = "messages.properties";
         // There are no mandatory variables, so no checking is done.
     private static Properties polaris;            // Default properties needed by Polaris.
     private static Properties symphony;           // Default properties needed to create a user in Symphony.
     private static Properties bimport;            // Properties Metro needs to operate BImport.
     private static Properties environment;        // Basic envrionment values.
     private static Properties sip2;               // Variables used to talk to SIP2.
     private static Properties city;               // Required for Horizon users to translate site specific city codes.
     private static Properties debugProperties;    // Optional config for debugging.
     private static Properties systemVariables;    // Optional no mandatory fields.
     private static Properties messagesProperties; // Messages tailored by local library.
     
     /**
      * Sets the config directory, the directory where all the config files can
      * be found.
      * @param configDirectory 
      */
     public static void setConfigDirectory(String configDirectory)
     {
         if(configDirectory != null && configDirectory.isEmpty() == false) 
         {
             if (configDirectory.endsWith(File.separator) == false)
             {
                 configDirectory += File.separator;
             }
             CONFIG_DIR = configDirectory;
         }
         else
         {
             CONFIG_DIR = "." + File.separator;
         }
         System.out.println("Metro (MeCard) server version " + VERSION);
         System.out.println(new Date() + " CONFIG: dir set to '" + CONFIG_DIR + "'");
         BIMPORT_PROPERTY_FILE  = CONFIG_DIR + BIMPORT_PROPERTY_FILE;
         SYMPHONY_PROPERTY_FILE = CONFIG_DIR + SYMPHONY_PROPERTY_FILE;
         POLARIS_PROPERTY_FILE  = CONFIG_DIR + POLARIS_PROPERTY_FILE;
         MESSAGES_PROPERTY_FILE = CONFIG_DIR + MESSAGES_PROPERTY_FILE;
         ENVIRONMENT_FILE       = CONFIG_DIR + ENVIRONMENT_FILE;
         SIP2_FILE              = CONFIG_DIR + SIP2_FILE;
         BIMPORT_CITY_MAPPING   = CONFIG_DIR + BIMPORT_CITY_MAPPING;
         DEBUG_SETTINGS_FILE    = CONFIG_DIR + DEBUG_SETTINGS_FILE;
         VARIABLES_FILE         = CONFIG_DIR + VARIABLES_FILE;
     }
     
     /**
      * Returns the configuration directory
      * @return the configuration directory as a String.
      */
 //    public static String getConfigDirectory()
 //    {
 //        return CONFIG_DIR;
 //    }
     
     /**
      * Gets specific properties from a configuration file.
      * @param type of configuration file to read, ie environment or bimport etc.
      * @return Java properties object filled with values read from the property file.
      */
     public static Properties getProperties(ConfigFileTypes type)
     {
         switch (type)
         {
             case SYMPHONY: // Additional properties that are given to a customer by default at creation time.
                 symphony = readPropertyFile(PropertyReader.SYMPHONY_PROPERTY_FILE);
                 // Now check manditory fields
                 for (SymphonyPropertyTypes sType : SymphonyPropertyTypes.values())
                 {
                     if (symphony.get(sType.toString()) == null)
                     {
                         String msg = "'" + sType + "' unset in " + PropertyReader.SYMPHONY_PROPERTY_FILE;
                         Logger.getLogger(PropertyReader.class.getName()).log(Level.SEVERE, msg, new NullPointerException());
                     }
                 }
                 return symphony;
                 
             case ENVIRONMENT:
                 environment = readPropertyFile(PropertyReader.ENVIRONMENT_FILE);
                 // now check that all mandetory values are here.
                 for (LibraryPropertyTypes lType : LibraryPropertyTypes.values())
                 {
                     if (environment.get(lType.toString()) == null)
                     {
                         String msg = "'" + lType + "' unset in " + PropertyReader.ENVIRONMENT_FILE;
                         Logger.getLogger(PropertyReader.class.getName()).log(Level.SEVERE, msg, new NullPointerException());
                     }
                 }
                 return environment;
                 
             case SIP2:
                 sip2 = readPropertyFile(PropertyReader.SIP2_FILE);
                 // now check that all mandetory values are here.
                 for (SipPropertyTypes sType : SipPropertyTypes.values())
                 {
                     if (sip2.get(sType.toString()) == null)
                     {
                         String msg = "'" + sType + "' unset in " + PropertyReader.SIP2_FILE;
                         Logger.getLogger(PropertyReader.class.getName()).log(Level.SEVERE, msg, new NullPointerException());
                     }
                 }
                 return sip2;
                 
             case BIMPORT:
                 bimport = readPropertyFile(PropertyReader.BIMPORT_PROPERTY_FILE);
                 // now check that all mandetory values are here.
                 for (BImportPropertyTypes bType : BImportPropertyTypes.values())
                 {
                     if (bimport.get(bType.toString()) == null)
                     {
                         String msg = "'" + bType + "' unset in " + PropertyReader.BIMPORT_PROPERTY_FILE;
                         Logger.getLogger(PropertyReader.class.getName()).log(Level.SEVERE, msg, new NullPointerException());
                     }
                 }
                 return bimport;
 
             case BIMPORT_CITY_MAPPING:
                 city = readPropertyFile(PropertyReader.BIMPORT_CITY_MAPPING);
                 // now check that all mandetory values are here.
                 for (MemberTypes mType : MemberTypes.values())
                 {
                     if (city.get(mType.toString()) == null)
                     {
                         String msg = "'" + mType + "' unset in " + PropertyReader.BIMPORT_CITY_MAPPING;
                         Logger.getLogger(PropertyReader.class.getName()).log(Level.SEVERE, msg, new NullPointerException());
                     }
                 }
                 return city;
 
             case DEBUG:
                 debugProperties = readPropertyFile(PropertyReader.DEBUG_SETTINGS_FILE);
                 // now check that all mandetory values are here.
                 for (DebugQueryConfigTypes dType : DebugQueryConfigTypes.values())
                 {
                     if (debugProperties.get(dType.toString()) == null)
                     {
                         String msg = "'" + dType + "' unset in " + PropertyReader.DEBUG_SETTINGS_FILE;
                         Logger.getLogger(PropertyReader.class.getName()).log(Level.SEVERE, msg, new NullPointerException());
                     }
                 }
                 return debugProperties;
 
             case VARS: // Additional variables used by this application as a user on a system like PATH.
                 systemVariables = readPropertyFile(PropertyReader.VARIABLES_FILE);
                 return systemVariables;
                 
             case POLARIS:
                 polaris = readPropertyFile(PropertyReader.POLARIS_PROPERTY_FILE);
                 // now check that all mandetory values are here.
                 for (PolarisPropertyTypes pType : PolarisPropertyTypes.values())
                 {
                     if (polaris.get(pType.toString()) == null)
                     {
                         String msg = "'" + pType + "' unset in " + PropertyReader.POLARIS_PROPERTY_FILE;
                         Logger.getLogger(PropertyReader.class.getName()).log(Level.SEVERE, msg, new NullPointerException());
                     }
                 }
                 return polaris;
                 
             case MESSAGES:
                 messagesProperties = readPropertyFile(PropertyReader.MESSAGES_PROPERTY_FILE);
                 // now check that all mandetory values are here.
                 for (MessagesConfigTypes mType : MessagesConfigTypes.values())
                 {
                     if (messagesProperties.get(mType.toString()) == null)
                     {
                         String msg = "'" + mType + "' unset in " + PropertyReader.MESSAGES_PROPERTY_FILE;
                         Logger.getLogger(PropertyReader.class.getName()).log(Level.SEVERE, msg, new NullPointerException());
                     }
                 }
                 return messagesProperties;
                 
             default:
                 throw new UnsupportedOperationException("unsupported property file");
         }
     }
 
     /**
      * Loads properties from the a given config file and updating an existing
      * property Map or inserts if the values don't exist.
      *
      * @see api.Command
      * @param props
      * @param type
      */
     public static void augmentProperties(Map<String, String> props, ConfigFileTypes type)
     {
         if (props == null)
         {
             return;
         }
         Properties localProps = PropertyReader.getProperties(type);
         Enumeration em = localProps.keys();
         while (em.hasMoreElements())
         {
             String key = (String) em.nextElement();
 //            System.out.println("VAR:'" + key + "'='" + localProps.getProperty(key, "") + "'");
             props.put(key, (String) localProps.getProperty(key, ""));
         }
     }    
 
     /**
      *
      * @param propertyFileName the value of propertyFileName
      * @return the Properties
      */
     private static Properties readPropertyFile(String propertyFileName)
     {
         Properties properties = new Properties();
         try
         {
             FileInputStream fis = new FileInputStream(propertyFileName);
             properties.loadFromXML(fis);
         } catch (FileNotFoundException ex)
         {
             String msg = "Failed to find '" + propertyFileName + "'";
             Logger.getLogger(PropertyReader.class.getName()).log(Level.SEVERE, msg, ex);
         } catch (NullPointerException npe)
         {
             String msg = "Failed to read messages config file. One must be defined.";
             Logger.getLogger(PropertyReader.class.getName()).log(Level.SEVERE, msg, npe);
         } catch (IOException ex)
         {
             Logger.getLogger(PropertyReader.class.getName()).log(Level.SEVERE, null, ex);
         }
         return properties;
     }
 }
