  /* Copyright (c) 2001 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root 
  * application directory.
  */
 
 package org.vfny.geoserver.config;
 
 import java.util.Map;
 import java.util.HashMap;
 import java.util.Properties;
 import java.util.logging.Logger;
 import java.io.Reader;
 import java.io.Serializable;
 import java.io.Writer;
 import java.io.FileInputStream;  
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.FileReader;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException; 
 
 import org.w3c.dom.*;
 import org.xml.sax.InputSource;
 import org.xml.sax.DocumentHandler;
 import org.xml.sax.SAXException;
 
 import org.geotools.resources.Geotools;
 import org.geotools.resources.Log4JFormatter;
 import org.geotools.resources.MonolineFormatter;
 
 /**
  * This class represents the global geoserver configuration options that
  * are not part of the Service element of a capabilities response.  They
  * are currently part of the same config file, but making a seperate class
  * allows us to easily seperate the elements out to different configuration
  * files in the future, such as for a WMS or WCS.  
  *
  * @author Chris Holmes, TOPP
  * @version $VERSION$
 **/
 public class ZServerConfig implements java.io.Serializable {
 
     public static final String PORT_TAG = "Port";
     
     public static final String ROOT_TAG = "ZServer";
 
     public static final String FIELDMAP_TAG = "FieldMap";
 
     public static final String DATABASE_TAG = "OnlineResource";
 
     public static final String RUN_ATTR = "run";
     
         /** Standard logging in stance for class */
     private static final Logger LOGGER = 
         Logger.getLogger("org.vfny.geoserver.config");
     
     private static ConfigInfo cfgInfo = ConfigInfo.getInstance();
    
     private String port = "5210";
     
     private String dataFolder = cfgInfo.getTypeDir();
 
     private String fieldmap;
 
     private String database;
     
     private boolean runServer = true;
 
     /** The holds the mappings between prefixes and uri's*/
     private Properties zserverProps;
 
     private ZServerConfig(boolean runServer){
 	this.runServer = false;
     }
 
     private ZServerConfig(String port, String fields, String database){
	if (port != null && !port.equals("")) {
	    this.port = port;
	}
 	this.fieldmap = fields;
 	this.database = database;
     } 
 
     /**
      * static factory, reads a ZServerConfig from an xml file, using
      * the default root tag.
      *
      * @param configFile the path to the configuration file.
      * @return the ZServerConfig object constructed from the xml elements
      * of the file.
      */
     public static ZServerConfig getInstance(String configFile) 
     throws ConfigurationException {
 	return getInstance(configFile, ROOT_TAG);
     }
 
     /**
      * static factory, reads a ZServerConfig from an xml file, using
      * the passed in root tag.
      *
      * @param configFile the path to the configuration file.
      * @param rootTag the tag of the element whose children are the appropriate
      * configuration elements.
      * @return the ZServerConfig object constructed from the xml elements
      * of the file.
      */
     public static ZServerConfig getInstance(String configFile, String rootTag) 
 	throws ConfigurationException{
 	try {
 	    //FileInputStream fis = new FileInputStream(configFile);
 	    FileReader fis = new FileReader(configFile);
 	    InputSource in = new InputSource(fis);
 	     DocumentBuilderFactory dfactory = new org.apache.xerces.jaxp.DocumentBuilderFactoryImpl();
 	    dfactory.setNamespaceAware(true);
 	    Document wfsDoc = dfactory.newDocumentBuilder().parse(in);
 	    Element configElem = wfsDoc.getDocumentElement();
 	    String configTag = configElem.getTagName();
 	    LOGGER.finer("root tag is " + configTag);
 	    if (!configTag.equals(rootTag)){
 		configElem = 
 		    (Element)configElem.getElementsByTagName(rootTag).item(0);
 		if (configElem == null) {
 		    String message = "could not find zserver tag: "+ rootTag + 
 			" in file: " + configFile + ".  Zserver module is not"
 			+ " being run";
 		    LOGGER.info(message);
 		    //throw new ConfigurationException(message);
 		    //instead of throwing exception, just search whole document.
 		    return new ZServerConfig(false);
 		}
 	    }
 	
 	    String runZserver = configElem.getAttribute(RUN_ATTR);
 	    if (runZserver == null || !runZserver.equals("false")) {
 		String port = findTextFromTag(configElem, PORT_TAG);
 		String rootDir = cfgInfo.getRootDir();
 		String fieldMap = rootDir + cfgInfo.GEO_MAP_FILE;
		String database = rootDir + "zserver-index";		
 		return new ZServerConfig(port, fieldMap, database);
 	    } 
 
 	} catch (IOException ioe) {
 	    String message = "problem reading file " + configFile  + "due to: "
 		+ ioe.getMessage();
 	    LOGGER.warning(message);
 	    throw new ConfigurationException(message, ioe);
 	} catch (ParserConfigurationException pce) {
 	    String message = "trouble with parser to read xml, make sure class"
 		+ "path is correct, reading file " + configFile;
 	    LOGGER.warning(message);
 	    throw new ConfigurationException(message, pce);
 	} catch (SAXException saxe){
 	    String message = "trouble parsing XML in " + configFile 
 		+ ": " + saxe.getMessage();
 	    LOGGER.warning(message);
 	    throw new ConfigurationException(message, saxe);
 	}
 	return new ZServerConfig(false);
 	
     }
     
     //TODO: put this in a common utility file, as 3 classes use it, and 
     //it's a bit confusing getting the ServiceConfig log messages.
     private static String findTextFromTag(Element root, String tag){
 	return ServiceConfig.findTextFromTag(root, tag);
     }
 
     public boolean run() {
 	return this.runServer;
     }
 
     public Properties getProps() {
 	Properties zserverProps = new Properties();
 	zserverProps.put("port", port);
 	zserverProps.put("datafolder", dataFolder);
 	zserverProps.put("fieldmap", fieldmap);
 	zserverProps.put("database", database);
 	return zserverProps;
     }
 }
