 
 /*
  * Created on May 8, 2009
  *
  */
 package org.reactome.cytoscape3;
 
 import java.awt.event.WindowAdapter;
 
 import java.awt.event.WindowEvent;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.Properties;
 
 import javax.swing.ImageIcon;
 import javax.swing.JFrame;
 
 import org.cytoscape.application.swing.CySwingApplication;
 import org.reactome.cancerindex.model.CancerIndexSentenceDisplayFrame;
 import org.slf4j.LoggerFactory;
 import org.slf4j.Logger;
 
 
 
 /**
  * A singleton to manage other singleton objects, and some utility methods.
  * @author wgm
  * ported July 2013 by Eric T Dawson
  */
 public class PlugInScopeObjectManager {
     private static Logger logger = LoggerFactory.getLogger(PlugInScopeObjectManager.class);
     private static PlugInScopeObjectManager manager;
     // Properties setting for this Cytoscape
     private Properties properties;
     // Don't cache it in case FI network version has been changed
     private FINetworkService networkService;
     // Try to track CancerIndexSentenceDisplayFrame
     private CancerIndexSentenceDisplayFrame cgiFrame;
     // Currently selected FI network version
     private String fiNetworkVersion;
    private static final String userGuideURL = "http://wiki.reactome.org/index.php/Reactome_FI_Cytoscape_Plugin";
     
     private PlugInScopeObjectManager() {
     }
     
     public static PlugInScopeObjectManager getManager() {
         if (manager == null)
             manager = new PlugInScopeObjectManager();
         return manager;
     }
     
     public String getFiNetworkVersion() {
         return this.fiNetworkVersion;
     }
 
     public String getDefaultFINeworkVersion() {
         Properties prop = getProperties();
         String fiVersions = prop.getProperty("FINetworkVersions");
         String[] tokens = fiVersions.split(",");
         for (String token : tokens) {
             token = token.trim();
             if (token.toLowerCase().contains("default"))
                 return token;
         }
         return null;
     }
     
     public void setFiNetworkVersion(String fiNetworkVersion) {
         this.fiNetworkVersion = fiNetworkVersion;
     }
 
     public CancerIndexSentenceDisplayFrame getCancerIndexFrame( CySwingApplication desktopApp) {
         if (cgiFrame == null) {
             cgiFrame = new CancerIndexSentenceDisplayFrame();
             cgiFrame.setTitle("Cancer Index Annotations");
             cgiFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
             cgiFrame.addWindowListener(new WindowAdapter() {
                 public void windowClosing(WindowEvent e) {
                     cgiFrame = null; // Enable to GC.
                 }
             });
             cgiFrame.setSize(800, 600);
             cgiFrame.setLocationRelativeTo(desktopApp.getJFrame());
             cgiFrame.setVisible(true);
         }
         else {
             cgiFrame.setState(JFrame.NORMAL);
             cgiFrame.toFront();
         }
         return cgiFrame;
     }
     
     public Properties getProperties() {
         if (properties == null) {
             try {
                 properties = new Properties();
                 InputStream is = RESTFulFIService.class.getResourceAsStream("Config.prop");
                 properties.load(is);
             }
             catch(IOException e) {
                 System.err.println("PlugInScopeObjectManager.getProperties(): " + e);
                 e.printStackTrace();
                 logger.error("Cannot initialize RESTFulFIService: " + e.getMessage(), e);
             }
         }
         return this.properties;
     }
     
     public FINetworkService getNetworkService() throws Exception {
         Properties prop = getProperties();
         String clsName = prop.getProperty("networkService",
                                           "org.reactome.cytoscape3.LocalService");
         FINetworkService networkService = (FINetworkService) Class.forName(clsName).newInstance();
 //        FINetworkService networkService = (FINetworkService) new LocalService();
         return networkService;
     }
     
     public ImageIcon createImageIcon(String imgFileName) {
         String urlName = "org/reactome/cytoscape/" + imgFileName;
         URL url = getClass().getClassLoader().getResource(urlName);
         ImageIcon icon = null;
         if (url == null)
             icon = new ImageIcon(imgFileName);
         else
             icon = new ImageIcon(url);
         return icon;
     }
     
     /**
      * Get the RESTful URL
      * @param fiVersion
      * @return
      */
     public String getRestfulURL(String fiVersion) {
         fiVersion = fiVersion.replaceAll(" ", "_");
         String key = fiVersion + "_restfulURL";
         Properties prop = getProperties();
         return prop.getProperty(key);
     }
     
     public String getRestfulURL() {
         return getRestfulURL(getFiNetworkVersion());
     }
     
     public String getDataSourceURL(String fiVerion) {
         String dataSourceURL = getProperties().getProperty("dataSourceURL");
         fiVerion = fiVerion.replaceAll(" ", "_");
         String dbName = getProperties().getProperty(fiVerion + "_sourceDb");
         String rtn = dataSourceURL.replace("${DB_NAME}",
                                            dbName);
         return rtn;
     }
     
     public String getDataSourceURL() {
         return getDataSourceURL(getFiNetworkVersion());
     }
    public static String getUserGuideURL()
    {
        return userGuideURL;
    }
 }
