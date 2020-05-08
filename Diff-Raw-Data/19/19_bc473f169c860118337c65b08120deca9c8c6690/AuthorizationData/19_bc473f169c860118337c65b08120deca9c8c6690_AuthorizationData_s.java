 /**
  * Copyright (C) 2003 FEIDE
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 
 package no.feide.moria.authorization;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Vector;
 import java.util.logging.Logger;
 
 import no.feide.moria.Configuration;
 import no.feide.moria.ConfigurationException;
 
 import org.apache.xerces.parsers.DOMParser;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 
 /** 
  * This class is used for storing authorization data for all
  * configured web services. */
 public class AuthorizationData {
 
     /** Used for logging */
     private static Logger log = Logger.getLogger(AuthorizationData.class.toString());
 
     /** All configured web services */
     private Map webServices = Collections.synchronizedMap(new HashMap());
 
     /** Timestamp for configuration file */
     private long fileTimestamp = 0;
 
     /** Singleton instance pointer */
     private static AuthorizationData authData = null;
 
     /** Array of names of attributes that can be used with sso. */
     private Vector ssoAttributes;
 
 
     /** Counstructor. Use getInstance() to get an instance of this
      * singleton object. 
      */
     private AuthorizationData() {
         super();
         log.finer("AuthorizationData()");
     }
 
 
     /**
      * Get singleton instance.
      */
     public static AuthorizationData getInstance() {
         log.finer("getInstance()");
         if (authData == null) 
             authData = new AuthorizationData();
         return authData;
     }
     
 
     /**
      * Regenerate the web service data structure if the xml
      * configuration file has been changed since last update.
      * @throws ConfigurationException
      */
     public void upToDate()
     throws ConfigurationException {
         log.finer("upToDate()");
 
         String xmlFileName = Configuration.getProperty("no.feide.moria.authorization.authConfigFile");
         File file = new File(xmlFileName);
 
         /* Only update if file has changed. */
         if (file.lastModified() > fileTimestamp) {
             if (fileTimestamp == 0)
                 log.config("Generating web service datastructure.");
             else
                 log.config("Web service authorization file changed. Updating web service datastructure.");
 
             double start = new Date().getTime(); // For timing
 
             try{
                 Map newWebServices = webServicesFromXML(xmlFileName);
                 synchronized (webServices) {
                     webServices = newWebServices;
                 }
                 log.config("Datastructure updated in "+(new Date().getTime()-start)+" ms");
                 fileTimestamp = file.lastModified();
 
             }
             catch (Exception e) {
                 log.severe("Unable to update authorization datastructure. \n"+e);
                 return;
             }
         }
 
         else 
             log.fine("Web service authorization file not modified. Datastructure not updated.");
     }
 
 
     
     /**
      * Get WebService for given id.
      * @param id The web service identifier
      */
     public WebService getWebService(String id) {
         log.finer("getWebService(String)");
         return (WebService) webServices.get(id);
     }
 
 
     /**
      * Number of registered web services
      */
     public int numOfWebServices() {
         return webServices.size();
     }
 
 
     /** 
      * Creates a new web service register from an xml file. When the
      * register is rebuilt, replace the existing register with the new
      * one.
      * @param xmlfile Filename for the xml configuration file.
      * @return a synchronized Map with all WebService object (from
      * configuration file).
      */
     private Map webServicesFromXML(String xmlFile) throws Exception{
         log.finer("webServicesFromXML(String)");
 
         HashMap attributes = new HashMap();
         HashMap profiles = new HashMap();
         Map newWebServices = new HashMap();
 
         /* Try to parse the xml file. If it doesn't parse, abort. */
         DOMParser parser = new DOMParser();
         try {
             parser.parse(xmlFile);
         } 
         catch (SAXException e) {
             log.severe("Error parsing \""+xmlFile+"\"\n"+e);
             throw e;
         } 
         catch (IOException e) {
             log.severe("IOException during parsing of \""+xmlFile+"\"\n"+e);
             throw e;
         }
 
 
         Document document = parser.getDocument();
         Element root = document.getDocumentElement();
 
         /* Extract the three main elements; <Attributes> <Profiles>
          * and <WebServices>.*/
         NodeList attributeElems = root.getElementsByTagName("Attributes");
         NodeList profileElems = root.getElementsByTagName("Profiles");
         NodeList wsElems = root.getElementsByTagName("WebServices");
 
         /* Generate a hashmap of all registered attributes. */
         if (attributeElems.getLength() > 0) 
             attributes = getAttributes((Element) attributeElems.item(0), null, true);
 
         /* Generate a hashmap of all registered profiles. */
         if (profileElems.getLength() > 0)
             profiles   = getProfiles((Element) profileElems.item(0), attributes, true);
         
         /* Generate a hashmap of all web services. The WebService
          * objects contains profiles, legal and illegal attributes. */
         if (wsElems.getLength() > 0)
             newWebServices = getWebServices((Element) wsElems.item(0), attributes, profiles);
 
         /* Flatten datastructure for all web service objects. */
         for (Iterator iterator = newWebServices.keySet().iterator(); iterator.hasNext();) {
             WebService ws = (WebService) newWebServices.get(iterator.next());
             ws.generateAttributeList(attributes);
         }
         
         /* Build array of attributes that are allowed to use in SSO. */
         Vector ssoAttributes = new Vector();
         for (Iterator it = attributes.keySet().iterator(); it.hasNext(); ) {
             Attribute attr = (Attribute) attributes.get(it.next());
             if (attr.allowSso())
                 ssoAttributes.add(attr.getName());
         }
 
         synchronized (ssoAttributes) {
             this.ssoAttributes = ssoAttributes;
         }
 
         return newWebServices;
     }
 
 
 
 
 
     /** 
      * Generate a hashmap with all attributes of a dom element. If an
      * existing hash of attributes are supplied, the method performs
      * sanity check - log a warning if the attribute, read from the
      * dom element, doesn't exist. If no existing attributes are
      * supplied (null), then all attributes should be accepted. (This
      * is due to the fact that the method is used to read attributes
      * from the list of legal attributes and from the attributes
      * specified in profiles and web services.
      * @param attrsElem The element that the attributes are located in
      * @param existing The existing attributes used for sanity check.
      */
     private HashMap getAttributes(Element attrsElem, HashMap existing, boolean checkSecLevel) {
         log.finer("getAttributes(Element, HashMap)");
 
         HashMap attributes = new HashMap();
         NodeList attrElems = attrsElem.getElementsByTagName("Attribute");
 
         /* Go through all attribute elements */
         for (int i = 0; i < attrElems.getLength(); i++) { 
             Element attribute = (Element) attrElems.item(i);
 
             String name = attribute.getAttribute("name");
             String sso = attribute.getAttribute("sso");
             String secLevel;
 
             /* Seclevel is not required when specifying
              * deniedAttributes. In this case, just set secLevel to
              * any legal value. */
             if (checkSecLevel)
                 secLevel = attribute.getAttribute("secLevel");
             else
                 secLevel = "HIGH";
 
             /* Warn if we shuld do sanity check and attribute doesn't
                already exist. */
             if (existing != null &&
                 !existing.containsKey(name)) { 
                 log.warning("No such attribute: "+name); 
             }
 
             else
                 attributes.put(name, new Attribute(name, sso, secLevel));
         }
 
         return attributes;
     }
  
 
 
     /** 
      * Read all profiles from xml. Each profile can contain a list of
      * attributes. If a set of existing profiles are supplied the
      * method performs sanity check on each profile (check for
      * existence). If not a new profile is added to the returning
      * hashmap instead of returning a hashmap with pointers to the
      * existing profile objects. 
      * @param profilesElem The element with all the profile elements.
      * @param existing The existing profiles to use profile pointers from.
      * @param newProfiles Flag if new profiles are going to be created.
      */
     private HashMap getProfiles(Element profilesElem, HashMap existing, boolean newProfiles) {
         log.finer("getProfiles(Element, HashMap, boolean)");
 
         HashMap profiles = new HashMap();
         NodeList profileElems = profilesElem.getElementsByTagName("Profile");
 
         for (int i = 0; i < profileElems.getLength(); i++) { 
             Element profileElem = (Element) profileElems.item(i);
 
             /* Generate new profile objects and store in hash. */
             if (newProfiles) {
                 Profile profile = new Profile(profileElem.getAttribute("name"));
                 profiles.put(profile.getName(), profile);
                 profile.setAttributes(getAttributes(profileElem, existing, true));
             }
 
             /* Use pointers to existing profile objects. */
             else {
                 String name = profileElem.getAttribute("name");
 
                 if (existing.containsKey(name)) {
                     profiles.put(name, existing.get(name));
                 }
                 else 
                     log.warning("No such profile: "+name);
             }
 
         }
         return profiles;
     }
 
 
 
     
     /** 
      * Read all webservices from xml. Each web service has an id,
      * name and might be assigned to a set of profiles and attributes.
      * It can also contain a list of illegal attributes.
      * @param wssElem The element that contains the web services
      * @param attributes Legal attributes
      * @param profiles Legal profiles
      * @return HashMap All web services
      */
     private HashMap getWebServices(Element wssElem, HashMap attributes, 
                                    HashMap profiles) {
         log.finer("getWebServices(Element, HashMap, HashMap");
 
         HashMap webServices = new HashMap();
         NodeList wsElems = wssElem.getElementsByTagName("WebService");
 
         /* All profiles */
         for (int i = 0; i < wsElems.getLength(); i++) { 
             Element wsElem = (Element) wsElems.item(i);
 
             /* Create a new WebService with id */
             WebService ws = new WebService(wsElem.getAttribute("id"));
             webServices.put(ws.getId(), ws);
 
             /* Set WebService's parameters.*/
             String name = wsElem.getElementsByTagName("Name").item(0).getFirstChild().getNodeValue();
             String url = wsElem.getElementsByTagName("URL").item(0).getFirstChild().getNodeValue();
 			String defaultLang = ((Element)wsElem.getElementsByTagName("DefaultLang").item(0)).getAttribute("name");
 			String defaultOrg = ((Element)wsElem.getElementsByTagName("DefaultOrg").item(0)).getAttribute("name");
 			boolean allowsLocalAuth = wsElem.getElementsByTagName("AllowLocalAuth").getLength() > 0;
             ws.setName(name);
             ws.setUrl(url);
             ws.setDefaultLang(defaultLang);
             ws.setDefaultOrg(defaultOrg);
             ws.setAllowLocalAuth(allowsLocalAuth);
             
             /* Set WebService's affiliation. The organizations is ordered by a "order"-attribute. */
             Element affiliationList = (Element) wsElem.getElementsByTagName("Affiliation").item(0);
			NodeList organizations = affiliationList.getElementsByTagName("Organization");
            Vector affiliations = new Vector();
            
            for (int j = 0; j < organizations.getLength(); j++) {
				affiliations.add(((Element) organizations.item(j)).getAttribute("name"));
             }
            ws.setAffiliations((String[]) affiliations.toArray(new String[affiliations.size()]));
                      
             /* Set WebService's attributes (allowed and denied) and
              * profiles */
             NodeList aaElems = wsElem.getElementsByTagName("AllowedAttributes");
             NodeList daElems = wsElem.getElementsByTagName("DeniedAttributes");
             NodeList profileElems = wsElem.getElementsByTagName("WSProfiles");
             
             if (aaElems.getLength() > 0) 
                 ws.setAllowedAttributes(getAttributes((Element) aaElems.item(0), attributes, true));
 
             if (daElems.getLength() > 0) 
                 ws.setDeniedAttributes(getAttributes((Element) daElems.item(0), attributes, false));
 
             if (profileElems.getLength() > 0) 
                 ws.setProfiles(getProfiles((Element) profileElems.item(0), profiles, false));
 
         }
         return webServices;
     }
 
 
     /** Return names of attributes that can be used in sso. */
     public Vector getSsoAttributes() {
         return ssoAttributes;
     }
 
 
     
 
     /********************************************************
      * Static method for "offline" testing.                 *
      ********************************************************/
 
 
     
     /** 
      * Used for test generation of data structure. (Typically run
      * test before putting a new xml configuration file in
      * production.) 
      */
     public static void main (String[] args) throws Exception {
         AuthorizationData wa = new AuthorizationData();        
         
         /* At least file name is required. */
         if (args.length == 0) {
             usage();
             return;
         }
 
         Map allWS = wa.webServicesFromXML(args[0]);
         
         /* If web service id is specified, only display data for one
          * web service. */
         if (args.length >= 2) {
             if (allWS.containsKey(args[1])) 
                 printWSData((WebService) allWS.get(args[1]), wa);
             else 
                 System.err.println("No WebService with id: "+args[1]);
         }
                 
         /* Display data for all web services. */
         else {
             for (Iterator iterator = allWS.keySet().iterator(); iterator.hasNext();) 
                 printWSData((WebService) allWS.get(iterator.next()), wa);
         }
     }
 
 
 
     /** Display usage help.*/
     private static void usage() {
         System.err.println("Usage: java AuthorizationData XML_FILE [wsid]");
     }
 
 
 
     /** 
      * Print all web service data to standard out. Name, id, URL and
      * flattened attribute structure is displayed. 
      */
     private static void printWSData(WebService ws, AuthorizationData wa) {
         System.out.println("***************************************************");
         System.out.println("* "+ws.getName());
         System.out.println("***************************************************");
         System.out.println("  ID:\t\t"+ws.getId());
         System.out.println("  URL:\t\t"+ws.getUrl());
 		System.out.println("  Local Auth:\t"+ws.allowsLocalAuth());
 		System.out.println("  Default Lang:\t"+ws.getDefaultLang());
 		System.out.println("  Default Org:\t"+ws.getDefaultOrg());
 		System.out.print(  "  Affilations:\t");
 		
 		String[] affiliations = ws.getAffiliations();
 		for (int i = 0; i < affiliations.length; i++) {
 			System.out.print(affiliations[i]+" ");
 		}
 		System.out.println("");
 		
         System.out.println("  Attribute\t\t\tSSO\tSecLevel");
         System.out.println("  -------------------------------------------------");
 
         HashMap attributes = ws.getAttributes();
         Vector  ssoAttributes = wa.getSsoAttributes();
 
         for (Iterator iterator = attributes.keySet().iterator(); iterator.hasNext();) { 
             String key = (String) iterator.next();
             Attribute attribute = (Attribute) attributes.get(key);
             System.out.println("  "+key+"\t"+(attribute.allowSso() && ssoAttributes.contains(key))+"\t"+Attribute.secLevelName(attribute.getSecLevel()));
         }
 
         System.out.println("");
     }
 }
 
 
