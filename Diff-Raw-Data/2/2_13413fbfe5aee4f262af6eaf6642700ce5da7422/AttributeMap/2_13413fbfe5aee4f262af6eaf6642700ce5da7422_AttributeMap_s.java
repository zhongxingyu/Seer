 //: "The contents of this file are subject to the Mozilla Public License
 //: Version 1.1 (the "License"); you may not use this file except in
 //: compliance with the License. You may obtain a copy of the License at
 //: http://www.mozilla.org/MPL/
 //:
 //: Software distributed under the License is distributed on an "AS IS"
 //: basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 //: License for the specific language governing rights and limitations
 //: under the License.
 //:
 //: The Original Code is Guanxi (http://www.guanxi.uhi.ac.uk).
 //:
 //: The Initial Developer of the Original Code is Alistair Young alistair@codebrane.com
 //: All Rights Reserved.
 //:
 
 package org.guanxi.idp.util;
 
 import org.guanxi.common.GuanxiException;
 import org.guanxi.common.GuanxiPrincipal;
 import org.guanxi.xal.idp.*;
 import org.guanxi.idp.persistence.PersistenceEngine;
 import org.guanxi.idp.farm.rule.AttributeRule;
 import org.apache.xmlbeans.XmlException;
 import org.springframework.web.context.ServletContextAware;
 
 import javax.servlet.ServletContext;
 import java.io.File;
 import java.io.IOException;
 import java.util.regex.Pattern;
 import java.util.regex.Matcher;
 import java.util.Vector;
 import java.util.HashMap;
 
 /**
  * <p>AttributeMap</p>
  * Provides mapping functionality for attributes.
  *
  * @author Alistair Young alistair@smo.uhi.ac.uk
  * @author Aggie Booth bmb6agb@ds.leeds.ac.uk
  */
 public class AttributeMap implements ServletContextAware {
   /** The token that denotes a map variable in a map value */
   public static final String MAP_VARIABLE_TOKEN_START = "${";
   public static final String MAP_VARIABLE_TOKEN_END = "}";
 
   /** The ServletContext, passed to us by Spring as we are ServletContextAware */
   private ServletContext servletContext = null;
   /** Our provider groupings and mapping rules */
   private Vector<Map> maps = null;
   private Vector<MapProvider> providers = null;
   /** The new names of the attribute passed to map() */
   private Vector<String> mappedNames = null;
   /** The new values of the attribute passed to map() */
   private Vector<String> mappedValues = null;
   /** List of map variables */
   private HashMap<String,String> mapVariables = null;
   /** The map file to use */
   private String mapFile = null;
   /** The persistence engine to use */
   private PersistenceEngine persistenceEngine = null;
   /** The attribute rules that we have access to */
   private AttributeRule[] attributeRules = null;
 
   public void init() {
     maps = new Vector<Map>();
     providers = new Vector<MapProvider>();
     mappedNames = new Vector<String>();
     mappedValues = new Vector<String>();
     mapVariables = new HashMap<String, String>();
 
     try {
       loadMaps(mapFile);
     }
     catch(GuanxiException ge) {
     }
   }
 
   /**
    * Maps attributes and values.
    * Once the mapping has been done, the application should use the helper methods to retrieve
    * the mapped attribute name and value:
    * getMappedName
    * getMappedValue
    *
    * The process of mapping is thus:
    *   Attribute is renamed if there is a mappedName entry
    *   Attribute value is changed to what is in the mappedValue entry if there is one
    *   Attribute value is further changed by any rule defined in the mappedRule entry if there is one
    *
    * @param principal the principal describing the user the attribute refers to
    * @param spProviderId This should be the content of a 'providerId' attribute on a map element.
    * If no maps are found that match this value then no attributes will be mapped. If any mapping
    * rules are service provider agnostic, they should have a "providerId" set to "*" on their
    * provider element.
    * @param attrName The name of the attribute to map
    * @param attrValue The value to give the mapped attribute
    * @return true if the attribute was mapped otherwise false
    */
   public boolean map(GuanxiPrincipal principal, String spProviderId, String attrName, String attrValue) {
     int index = -1;
     boolean mapped = false;
     Pattern pattern = null;
     Matcher matcher = null;
 
     mappedNames.clear();
     mappedValues.clear();
 
     // Look for provider groups that are either specific to this providerId or wildcard
     for (int providersCount = 0; providersCount < providers.size(); providersCount++) {
       MapProvider provider = (MapProvider)providers.get(providersCount);
 
       if ((interpolate(provider.getProviderId()).equals(spProviderId)) || provider.getProviderId().equals("*")) {
         // Load up the mapping references for this provider
         for (int mapRefsCount = 0; mapRefsCount < provider.getMapRefArray().length; mapRefsCount++) {
 
           /* Look for exceptions to the mapping process. If we find the current service provider
            * as an exception in the mapping rules, we ignore the rule.
            */
           boolean blockedFromMap = false;
           if (provider.getMapRefArray(mapRefsCount).getExceptArray() != null) {
             String[] providerExceptions = provider.getMapRefArray(mapRefsCount).getExceptArray();
             for (String providerException : providerExceptions) {
               if (providerException != null) {
                 if (interpolate(providerException).equals(spProviderId)) {
                    blockedFromMap = true;
                 }
               }
             }
           }
           if (blockedFromMap) continue;
 
           // Look for the map in the maps cache
           for (int mapsCount = 0; mapsCount < maps.size(); mapsCount++) {
             Map map = (Map)maps.get(mapsCount);
             if (map.getName().equals(provider.getMapRefArray(mapRefsCount).getName())) {
               // Have we got the correct attribute to map?
               if (attrName.equals(interpolate(map.getAttrName()))) {
                 pattern = Pattern.compile(interpolate(map.getAttrValue()));
                 matcher = pattern.matcher(attrValue);
 
                 // Match the value of the attribute before mapping
                 if (matcher.find()) {
                   index++;
                   mapped = true;
 
                   // Rename the attribute...
                   String mappedAttrName = null;
                   if (map.getMappedName() != null)
                     mappedAttrName = interpolate(map.getMappedName());
                   else
                     mappedAttrName = attrName;
                   mappedNames.add(mappedAttrName);
 
                   // If it's a persistent attribute, see if its value has been persisted already
                   boolean retrievedPersistentAttribute = false;
                   String mappedAttrValue = null;
                   if (map.getPersistent()) {
                     if (persistenceEngine.attributeExists(principal, spProviderId, mappedAttrName)) {
                       // No interpolation required for database values as they're absolute
                       mappedAttrValue = persistenceEngine.getAttributeValue(principal, spProviderId, mappedAttrName);
                       retrievedPersistentAttribute = true;
                     }
                   }
 
                   if ((!map.getPersistent()) || ((map.getPersistent()) && (!retrievedPersistentAttribute))) {
                     // Attribute value is what it says in the map...
                     if (map.getMappedValue() != null)
                       mappedAttrValue = interpolate(map.getMappedValue());
                     // ...or just use the original attribute value
                     else
                       mappedAttrValue = attrValue;
                   }
                   mappedValues.add(mappedAttrValue);
 
                   // ...and transform the value if required
                   if ((!map.getPersistent()) || ((map.getPersistent()) && (!retrievedPersistentAttribute))) {
                     if (map.getMappedRule() != null) {
                       // Sort out any chained rules
                       String[] rules = null;
                       if (map.getMappedRule().contains(";")) {
                         rules = map.getMappedRule().split(";");
                       }
                       else {
                         rules = new String[] {map.getMappedRule()};
                       }
 
                       // Loop through the mapping rules for the attribute
                       for (String rule : rules) {
                         for (AttributeRule attributeRule : attributeRules) {
                           if (attributeRule.getRuleName().equals(rule)) {
                             String valueToBeModified = null;
                             if (map.getUnique()) {
                              valueToBeModified += spProviderId;
                             }
                             else {
                               valueToBeModified = (String)mappedValues.get(index);
                             }
                             mappedAttrValue = attributeRule.applyRule((String)mappedNames.get(index), valueToBeModified);
                             mappedValues.set(index, mappedAttrValue);
                           }
                         }
                       }
                     }
                   }
 
                   // Persist the attribute if required, after all maps and rules have been applied
                   if ((map.getPersistent()) && (!retrievedPersistentAttribute)) {
                     persistenceEngine.persistAttribute(principal, spProviderId, mappedAttrName, mappedAttrValue);
                   }
 
                   // Finally, scope the attribute if required
                   if (map.getScope() != null) {
                     mappedValues.set(index, mappedAttrValue + "@" + interpolate(map.getScope()));
                   }
 
                   //return true;
                 } // if (matcher.find()) {
               } // if (attrName.equals(map.getAttrName()))
             } // if (map.getName().equals(provider.getMapRefArray(mapRefsCount)))
           } // for (int mapsCount = 0; mapsCount < maps.size(); mapsCount++)
         } // for (int mapRefsCount = 0; mapRefsCount < provider.getMapRefArray().length; mapRefsCount++)
       } // if ((provider.getProviderId().equals(spProviderId)) || provider.getProviderId().equals("*"))
     } // for (int providersCount = 0; providersCount < providers.size(); providersCount++)
 
     // No mappings found for the attribute
     return mapped;
   }
 
   /**
    * Retrieves the new name of the attribute passed to the map method
    *
    * @return the new name of the attribute passed to the map method
    * or null if map has not been called or no mappings were found.
    */
   public String[] getMappedNames() {
     return (String[])mappedNames.toArray(new String[mappedNames.size()]);
   }
 
   /**
    * Retrieves the new value of the attribute passed to the map method
    *
    * @return the new value of the attribute passed to the map method
    * or null if map has not been called or no mappings were found.
    */
   public String[] getMappedValues() {
     return (String[])mappedValues.toArray(new String[mappedValues.size()]);
   }
 
   /**
    * Loads up the chain of map files to use. The chain will always have at least one in it.
    *
    * @param mapXMLFile The full path and name of the root map file
    * @throws GuanxiException if an error occurs
    */
   private void loadMaps(String mapXMLFile) throws GuanxiException {
     try {
       // Sort out the path to the ARP file
       String mapFile = null;
       if ((mapXMLFile.startsWith("WEB-INF")) ||
           (mapXMLFile.startsWith("/WEB-INF"))) {
         mapFile = servletContext.getRealPath(mapXMLFile);
       }
       else
         mapFile = mapXMLFile;
 
       // Load up the root map file
       AttributeMapDocument attrMapDoc = AttributeMapDocument.Factory.parse(new File(mapFile));
 
       // Load any map variables
       if ((attrMapDoc.getAttributeMap().getVarArray() != null) && (attrMapDoc.getAttributeMap().getVarArray().length > 0)) {
         MapVar[] mapVars = attrMapDoc.getAttributeMap().getVarArray();
         for (MapVar mapVar : mapVars) {
           mapVariables.put(mapVar.getName(), mapVar.getValue());
         }
       }
 
       // Cache all the maps...
       for (int c = 0; c < attrMapDoc.getAttributeMap().getMapArray().length; c++ ) {
         maps.add(attrMapDoc.getAttributeMap().getMapArray(c));
       }
 
       // ...and providers
       for (int c = 0; c < attrMapDoc.getAttributeMap().getProviderArray().length; c++ ) {
         providers.add(attrMapDoc.getAttributeMap().getProviderArray(c));
       }
 
       // Do we have any other map files to include?
       if (attrMapDoc.getAttributeMap().getIncludeArray() != null) {
         for (int c=0; c < attrMapDoc.getAttributeMap().getIncludeArray().length; c++) {
           // Load up any further included map files
           loadMaps(attrMapDoc.getAttributeMap().getIncludeArray(c).getMapFile());
         }
       }
     }
     catch(XmlException xe) {
       throw new GuanxiException(xe);
     }
     catch(IOException ioe) {
       throw new GuanxiException(ioe);
     }
   }
 
   /**
    * Interpolates a map variable if present in a map value.
    * Map variables start with the recognised token:
    * MAP_VARIABLE_TOKENuni.ac.uk
    * e.g.
    * #uni.ac.uk
    *
    * @param value the map value
    * @return the interpolated value if a map variable is present, otherwise the original value.
    * If a map variable is present but not defined, the original value including the token is
    * returned.${x}
    */
   private String interpolate(String value) {
     if ((value.startsWith(MAP_VARIABLE_TOKEN_START)) && (value.endsWith(MAP_VARIABLE_TOKEN_END))) {
       String var = value.substring(MAP_VARIABLE_TOKEN_START.length(), (value.length() - 1));
       if (mapVariables.get(var) != null) {
         return mapVariables.get(var);
       }
       else {
         return value;
       }
     }
     else {
       return value;
     }
   }
 
   public void setServletContext(ServletContext servletContext) { this.servletContext = servletContext; }
   public void setMapFile(String mapFile) { this.mapFile = mapFile; }
   public String getMapFile() { return mapFile; }
   public void setPersistenceEngine(PersistenceEngine persistenceEngine) { this.persistenceEngine = persistenceEngine; }
   public void setAttributeRules(AttributeRule[] attributeRules) { this.attributeRules = attributeRules; }
 }
