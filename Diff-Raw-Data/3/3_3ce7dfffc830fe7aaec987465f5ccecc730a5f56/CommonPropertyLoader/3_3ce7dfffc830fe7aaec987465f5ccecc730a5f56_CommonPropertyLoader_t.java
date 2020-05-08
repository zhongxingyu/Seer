 package edu.wustl.cab2b.common.util;
 
 import java.util.Properties;
 
 import org.apache.log4j.Logger;
 
 /**
  * This class handles fetching properties from cab2b.properties file
  * 
  * @author Chandrakant_Talele
  * @author lalit_chand
  */
 public class CommonPropertyLoader {
     private static final Logger logger = edu.wustl.common.util.logger.Logger.getLogger(CommonPropertyLoader.class);
 
     private static final String propertyfile = "cab2b.properties";
 
     private static Properties props = Utility.getPropertiesFromFile(propertyfile);
 
     /**
      * Returns the Path of domain model XML file
      * 
      * @param applicationName
      *            Name of the application
      * @return Returns the File Path
      */
     public static String getModelPath(String applicationName) {
         String path = props.getProperty(applicationName + ".ModelPath");
         if (path == null || path.length() == 0) {
             logger.error("Model path for application : " + applicationName + " is not configured in "
                     + propertyfile);
         }
         return path;
     }
 
     /**
      * Returns names of all application for which caB2B is configured
      * 
      * @return Returns the Application Names
      */
     public static String[] getAllApplications() {
         String[] allApplications = props.getProperty("all.applications").split(",");
         if (allApplications == null || allApplications.length == 0) {
             logger.error("No value for key 'all.applications' is found in " + propertyfile);
         }
 
         return allApplications;
     }
 
     /**
      * @return The URL of JNDI service running on caB2B server
      */
     public static String getJndiUrl() {
         String serverIP = props.getProperty("caB2B.server.ip");
         String jndiPort = props.getProperty("caB2B.server.port");
         return "jnp://" + serverIP + ":" + jndiPort;
     }
 
     /**
      * @return all the index urls used to get the service information
      */
     public static String[] getIndexServiceUrls() {
         String allUrls = props.getProperty("indexurls");
        return allUrls.split(",");
     }
     
     /**
 	 * 
 	 * @param gridType
 	 * @return returns the sys-description file for GTS
 	 */
 
 	public static String getSyncDesFile(String gridType) {
 		return props.getProperty(gridType + "_sync_description_file");
 	}
 	
 	/**
 	 * @param gridType
 	 * @return signing policy for given idP
 	 */
 	public static String getSigningPolicy(String gridType) {
 		return props.getProperty(gridType + "_signing_policy");
 	}
 
 	/**
 	 * @param gridType
 	 * @return certificate for given idP
 	 */
 	public static String getCertificate(String gridType) {
 		return props.getProperty(gridType + "_certificate");
 	}
 
 }
