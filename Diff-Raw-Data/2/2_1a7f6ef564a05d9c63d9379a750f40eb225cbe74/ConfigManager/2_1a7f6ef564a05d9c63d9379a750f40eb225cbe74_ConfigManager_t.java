 import org.w3c.dom.Document;
 import org.w3c.dom.NodeList;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import java.io.File;
 import java.util.ArrayList;
 
 /**
  * The configuration manager. It loads the site configuration files for each of
  * the sites supported by the server. This class follows the singleton pattern.
  * @author Benjamin Russell (brr1922@rit.edu)
  */
 public class ConfigManager {
     // CONSTANTS ///////////////////////////////////////////////////////////
     private static final String siteConfigFolder = "siteConfigs";
 
     // MEMBER VARIABLES ////////////////////////////////////////////////////
     private static ConfigManager ourInstance = new ConfigManager();
 
     private File configFolder;
 
     // CONSTRUCTOR /////////////////////////////////////////////////////////
     private ConfigManager() {
         // Open the folder of configurations
         configFolder = new File(siteConfigFolder);
         if(!configFolder.exists()) {
             throw new ConfigurationException("Site configurations folder does not exist");
         }
         if(!configFolder.isDirectory()) {
             throw new ConfigurationException("Site configurations folder is not a directory");
         }
     }
 
     // METHODS /////////////////////////////////////////////////////////////
     public static ConfigManager getInstance() {
         return ourInstance;
     }
 
     public ArrayList<SiteConfiguration> getSiteConfigurations() {
         // Load the files in the configuration
         File[] siteConfigurationFiles = configFolder.listFiles();
         ArrayList<SiteConfiguration> siteConfigs = new ArrayList<SiteConfiguration>();
         for(File siteFile : siteConfigurationFiles) {
             SiteConfiguration config = readSiteConfiguration(siteFile);
             if(config != null) {
                 siteConfigs.add(config);
                 System.out.println(config);
             }
         }
 
         return siteConfigs;
     }
 
     private SiteConfiguration readSiteConfiguration(File siteFile) {
         try {
             // Read in the XML for the site configuration
             DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
             Document xmlDoc = documentBuilder.parse(siteFile);
             xmlDoc.normalizeDocument();
 
             // Grab some elements from the configuration that defines the site
             NodeList rootPath = xmlDoc.getElementsByTagName("rootPath");
             NodeList host     = xmlDoc.getElementsByTagName("host");
             NodeList port     = xmlDoc.getElementsByTagName("port");
 
             // Verify that it's a site configuration
             // At a minimum we require a site tag, a root path, a host, and a port
             if(xmlDoc.getElementsByTagName("site").getLength() != 1) {
                 // <site> not provided
                 throw new ConfigurationException(siteFile.getName()
                         + " is not a valid site configuration. No site tag is provided.");
             } else if(rootPath.getLength() != 1) {
                 // <rootPath> not provided
                 throw new ConfigurationException(siteFile.getName()
                         + " is not a valid site configuration. No root path tag is provided");
             } else if(host.getLength() != 1) {
                 // <host> not provided
                 throw new ConfigurationException(siteFile.getName()
                         + " is not a valid site configuration. No host tag provided.");
             } else if(port.getLength() != 1) {
                 // <port> not provided
                 throw new ConfigurationException(siteFile.getName()
                         + " is not a valid site configuration. No port tag provided.");
             }
 
             // Generate a new site configuration class
             SiteConfiguration config = new SiteConfiguration();
             config.setRoot(rootPath.item(0).getTextContent());
             config.setHost(host.item(0).getTextContent());
             try {
                 config.setPort(Integer.parseInt(port.item(0).getTextContent()));
             } catch(NumberFormatException e) {
                 throw new ConfigurationException(siteFile.getName() + " is not a valid site configuration. Port is not numeric.");
             }
 
             return config;
 
         } catch(Exception e) {
             System.err.println("*** Failed to parse site configuration for " + siteFile.getName());
            System.err.println("    " + e.getMessage());
             return null;
         }
     }
 }
