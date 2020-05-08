 /* Copyright (c) 2001 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root 
  * application directory.
  */
 
 package org.vfny.geoserver.config;
 
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.logging.Logger;
 import java.io.Reader;
 import java.io.Serializable;
 import java.io.Writer;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException; 
 
 import org.w3c.dom.*;
 import org.xml.sax.InputSource;
 import org.xml.sax.DocumentHandler;
 import org.xml.sax.SAXException;
 
 /**
  * Represents the various service elements used by OGC 
  * @author Chris Holmes, TOPP
  * @version $VERSION$
 **/
 public class ServiceConfig implements java.io.Serializable {
 
     public static final String NAME_TAG = "Name";
     
     public static final String TITLE_TAG = "Title";
     
     public static final String ABSTRACT_TAG = "Abstract";
 
     public static final String KEYWORDS_TAG = "Keywords";
 
     public static final String KEYWORD_TAG = "Keyword";
 
     public static final String ONLINE_TAG = "OnlineResource";
 
     public static final String FEES_TAG = "Fees";
 
     public static final String ACCESS_TAG = "AccessConstraints";
 
     public static final String ROOT_TAG = "ServiceConfiguration";
 
     public static final String CAPABILITIES_TAG = "Service";
 
     public static final String OLD_ROOT_TAG = "GlobalConfiguration";
 
     public static final String VERBOSE_TAG = "Verbose";
 
     /** Regular Expression to split values from spaces or commas */
     public static final String WHITE_SPACE_OR_COMMA = ",\\s+";
 
 
         /** Standard logging instance for class */
     private static final Logger LOGGER = 
         Logger.getLogger("org.vfny.geoserver.config");
 
     /** The name the service provider assigns to this instance.*/
     private String name;
 
     /** A human-readable title to breifly identify this server in menus*/
     private String title;
 
     /** A narrative for more information about the server*/
     private String _abstract;
 
      /** Short words to aid catalog searching*/
     private List keywords = new ArrayList();
 
     /** The top-level HTTP URL of this service.*/
     private String onlineResource;
 
     /** Indicates the fees imposed by the service provider for usage.*/
     private String fees;
 
      /** Describes any access constraints imposed by the service provider*/
     private String accessConstraints;
     
     /** determines whether spaces and line feeds should be added to output*/
     //put in wfs-config?  Maybe if we have some more config options.
     private boolean verbose = false;
     
     /**
      * Constructor with the minimum required service elements.
      *
      * @param name service provider assigned for what to call this service.
      * @param title a human-readable title to identify this in menus.
      * @param onlineResource the top-level HTTP URL of this service.
      */
     public ServiceConfig(String name, String title, 
 				String onlineResource) {
 	this.name = name;
 	this.title = title;
 	this.onlineResource = onlineResource;
 	LOGGER.finer("Made new service config with values: " + name + ", " + 
 		     title + ", and " + onlineResource);
     } 
 
     /**
      * static factory, reads a ServiceConfig from an xml file, using
      * the default root tag.
      *
      * @param configFile the path to the configuration file.
      * @return the ServiceConfig object constructed from the xml elements
      * of the file.
      */
     public static ServiceConfig getInstance(String configFile) 
     throws ConfigurationException {
 	return getInstance(configFile, ROOT_TAG);
     }
 
     /**
      * static factory, reads a ServiceConfig from an xml file, using
      * the passed in root tag.
      *
      * @param configFile the path to the configuration file.
      * @param rootTag the tag of the element whose children are the appropriate
      * configuration elements.
      * @return the ServiceConfig object constructed from the xml elements
      * of the file.
      */
     public static ServiceConfig getInstance(String configFile, String rootTag) 
 	throws ConfigurationException{
 	ServiceConfig servConfig = null;
 	try {
 	    FileInputStream fis = new FileInputStream(configFile);
 	    InputSource in = new InputSource(fis);
 	    DocumentBuilderFactory dfactory = 
 		DocumentBuilderFactory.newInstance();
 	    dfactory.setNamespaceAware(true);
 	    Document serviceDoc = dfactory.newDocumentBuilder().parse(in);
 	    Element configElem = serviceDoc.getDocumentElement();
 	    String configTag = configElem.getTagName();
 	    if (!configTag.equals(rootTag) && !configTag.equals(OLD_ROOT_TAG)){
 		configElem = 
 		    (Element)configElem.getElementsByTagName(rootTag).item(0);
 		if (configElem == null) {
 		    String message = "could not find root tag: " + rootTag + 
 			" in file: " + configFile;
 		    LOGGER.warning(message);
 		    //throw new ConfigurationException(message);
 		    //instead of throwing exception, just search whole document
 		    configElem = serviceDoc.getDocumentElement();
 
 		}
 	    }
 		
 	    String name = findTextFromTag(configElem, NAME_TAG);
 	    String title = findTextFromTag(configElem, TITLE_TAG);
 	    String url = findTextFromTag(configElem, ONLINE_TAG);
 	    if (name == null || title == null || url == null) {
 		String message = "<Name>, <Title>, and <OnlineResource> are all " +
 		    "required for a proper a valid Service section.";
 		//REVISIT: if we don't throw exception then the user will just have
 		//an invalid Capabilities document.  Is that what we want?
 		//throw new ConfigurationException(message);
 		LOGGER.warning(message);
 	    }
 	    servConfig = new ServiceConfig(name, title, url);
 	    servConfig.setAbstract(findTextFromTag(configElem, ABSTRACT_TAG));
 	    servConfig.setFees(findTextFromTag(configElem, FEES_TAG));
 	    servConfig.setAccessConstraints(findTextFromTag(configElem, ACCESS_TAG));
 	    servConfig.setKeywords(getKeywords(configElem));
 	    if (configElem.getElementsByTagName(VERBOSE_TAG).item(0) != null){
 		servConfig.setVerbose(true);
 	    }
 
 	    fis.close();
 
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
 	return servConfig;
     }
 
     /**
      * Searches for keyword elements beneath the given root element.
      * They can be comma delimited or in different keyword elements, both
      * beneath the Keywords element.
      */
     static List getKeywords(Element root) {
 	List keywords = new ArrayList();
 	//try to get text in Keywords element.
 	String keywordsText = findTextFromTag(root, KEYWORDS_TAG);
	if (keywordsText != null) { //keywords are comma delimited in one field,
 	    //wfs style.
 	    String[] keywordArr = keywordsText.split(WHITE_SPACE_OR_COMMA); 
 	    for (int i = 0; i < keywordArr.length; i++){
 		keywords.add(keywordArr[i]);
 	    }
 	} else { //keywords are in Keyword elements
 	    NodeList keywordNodes = root.getElementsByTagName(KEYWORD_TAG);
 	    for (int j = 0; j < keywordNodes.getLength(); j++){
 		Node text = keywordNodes.item(j).getFirstChild();
 		if (text instanceof org.w3c.dom.Text){
 		    keywords.add(((Text)text).getData());
 		}
 	    }
 	}
 	return keywords;
     }
     /**
      * gets the string of the first descendant node in root that matches the
      * tag name.
      *
      * @param root the node whose descendants should be searched.
      * @param tag the tag to match.
      * @return the text of the first descendant node that matches the tag name,
      * an empty string if nothing is found.
      */
     //should be in sort of config utility class, as others use it as well.
     static String findTextFromTag(Element root, String tag){
 	String retString = new String();
 	Node firstElement = root.getElementsByTagName(tag).item(0);
 	if (firstElement != null) {
 	    Node text = firstElement.getFirstChild();
 	    if (text instanceof org.w3c.dom.Text){
 		retString = ((Text)text).getData();
 		
 	    }
 	}
 	LOGGER.finest("tag " + tag + " found text: " + retString);
 	return retString;
     }
 
     /** returns true if the Verbose tag is present in the config file. */
     public boolean isVerbose(){
 	return verbose;
     }
 
     void setVerbose(boolean verbose){
 	this.verbose = verbose;
     }
 
     /** 
     * Gets the abstract field of this service.
     **/
     public String getAbstract()
     {
         return this._abstract;
     }
 
     /** gets the access constraints.*/
     public String getAccessConstraints()
     {
         return this.accessConstraints;
     } 
 
     /** gets the fees.   
      * @return  the fees imposed by the service provider for usage*/
     public String getFees()
     {
         return this.fees;
     } 
 
     /** Gets the keywords of this service.*/
     public List getKeywords()
     {
         return this.keywords;
     } 
 
 
     /** returns the name */
     public String getName()
     {
         return this.name;
     } 
 
     /** gets the url of this service.*/
     public String getOnlineResource()
     {
         return this.onlineResource;
     } 
 
     /** Gets the human-readable title */
     public String getTitle()
     {
         return this.title;
     }
 
     /**
      * 
      * @param abstract
      */
     void setAbstract(String _abstract)
     {
         this._abstract = _abstract;
     } 
 
     /**
      * 
      * @param accessConstraints
      */
     void setAccessConstraints(String accessConstraints)
     {
         this.accessConstraints = accessConstraints;
     } 
 
     /**
      * 
      * @param fees Indicates the fees imposed by the service provider for usage
      */
     void setFees(String fees)
     {
         this.fees = fees;
     } 
 
     /**
      * 
      * @param keywords
      */
     void setKeywords(List keywords)
     {
         this.keywords = keywords;
     } 
 
     /**
      * adds a keyword to the current list of keywords.
      *
      * @param keyword the word to add.
      */
     void addKeyword(String keyword) {
 	this.keywords.add(keyword);
     }
 
 
     /**
      * 
      * @param name
      */
     void setName(String name)
     {
         this.name = name;
     } 
 
     /**
      * 
      * @param onlineResource
      */
     void setOnlineResource(String onlineResource)
     {
         this.onlineResource = onlineResource;
     } 
 
     /**
      * 
      * @param title
      */
     void setTitle(String title)
     {
         this.title = title;
     } 
 
     /**
      * returns the xml for a service section of a wfs (versions 1.0.0 and 
      * 0.0.14
      */
     public String getWfsXml(){
 	 StringBuffer tempResponse = new StringBuffer();
         
         // Set service section of Response, based on Configuration input
         tempResponse.append("\n  <Service>\n");
         tempResponse.append("    <Name>" + name + "</Name>\n");
         tempResponse.append("    <Title>" + title +
             "</Title>\n");
         tempResponse.append("    <Abstract>" + _abstract + 
             "</Abstract>\n");
         tempResponse.append("    <Keywords>");
 	Iterator keywordIter = keywords.iterator();
 	while (keywordIter.hasNext()) {
 	    tempResponse.append(keywordIter.next().toString());
 	    if (keywordIter.hasNext()){
 		tempResponse.append(". ");
 	    }
 	}
 	tempResponse.append("</Keywords>\n");
         tempResponse.append("    <OnlineResource>" + 
             onlineResource + "</OnlineResource>\n");
         tempResponse.append("    <Fees>" + fees + "</Fees>\n");
         tempResponse.append("    <AccessConstraints>" + 
             accessConstraints + "</AccessConstraints>\n");
         tempResponse.append("  </Service>\n");
 
 	  
         // Concatenate into XML output stream
         return tempResponse.toString();
     }
 
     /**
      * Override of toString method. */
     public String toString() {
         StringBuffer returnString = new StringBuffer("\nServiceConfig:");
 	returnString.append("\n   [name: " + name + "] ");
 	returnString.append("\n   [title: " + title + "] ");
 	returnString.append("\n   [abstract: " + _abstract + "] ");
 	returnString.append("\n   [keywords: "); 
         Iterator i = keywords.iterator();
         while(i.hasNext()) {
             String keyword = (String) i.next();
             returnString.append(keyword + " ");
         }
 	returnString.append("] ");
 	returnString.append("\n   [url: " + onlineResource + "] ");
 	returnString.append("\n   [fees: " + fees + "] ");
 	returnString.append("\n   [access constraints: " + accessConstraints + "] ");
 	
 
         return returnString.toString();
     }
 
 
 }
