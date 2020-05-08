 /* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.config;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.NoSuchElementException;
 import java.util.SortedSet;
 import java.util.TreeSet;
 import java.util.Arrays;
 
 import org.geotools.data.DataStore;
 import org.vfny.geoserver.global.dto.DataDTO;
 import org.vfny.geoserver.global.dto.DataStoreInfoDTO;
 import org.vfny.geoserver.global.dto.FeatureTypeInfoDTO;
 import org.vfny.geoserver.global.dto.NameSpaceInfoDTO;
 import org.vfny.geoserver.global.dto.StyleDTO;
 
 
 /**
  * Data purpose.
  * 
  * <p>
  * Represents an instance of the catalog.xml file in the configuration of the
  * server, along with associated configuration files for the feature types.
  * </p>
  * 
  * <p></p>
  *
  * @author dzwiers, Refractions Research, Inc.
 * @version $Id: DataConfig.java,v 1.7 2004/01/17 22:33:12 dmzwiers Exp $
  *
  * @see DataSource
  * @see FeatureTypeInfo
  * @see StyleConfig
  */
 public class DataConfig {
     public static final String CONFIG_KEY = "Config.Data";
     public static final String SEPARATOR  = ".";
 
     /**
      * A set of datastores and their names.
      *
      * @see org.vfny.geoserver.config.data.DataStoreInfo
      */
     private Map dataStores;
 
     /**
      * A set of namespaces and their names.
      *
      * @see org.vfny.geoserver.config.data.NameSpaceConfig
      */
     private Map nameSpaces;
 
     /**
      * FeatureTypesInfoConfig referenced by key "<code>dataStoreID + SEPARATOR + typeName</code>"
      * 
      * @see org.vfny.geoserver.global.dto.FeatureTypeInfoConfig
      */
     private Map featuresTypes;
 
     /**
      * A set of styles and their names.
      *
      * @see org.vfny.geoserver.config.data.StyleConfig
      */
     private Map styles;
 
     /**
      * the default namespace for the server instance.
      *
      * @see org.vfny.geoserver.config.data.NameSpaceConfig
      */
     private NameSpaceConfig defaultNameSpace;
 
     /**
      * Data constructor.
      * 
      * <p>
      * Creates a Data to represent an instance with default data.
      * </p>
      *
      * @see defaultSettings()
      */
     public DataConfig() {
         dataStores = new HashMap();
         nameSpaces = new HashMap();
         styles = new HashMap();
         featuresTypes = new HashMap();
         defaultNameSpace = new NameSpaceConfig();
     }
 
     /**
      * Data constructor.
      * 
      * <p>
      * Creates a copy of the DataDTO provided. If the Data provided  is null
      * then default values are used. All the datastructures are cloned.
      * </p>
      *
      * @param data The catalog to copy.
      */
     public DataConfig(DataDTO data) {
         Iterator i = null;
 
         i = data.getDataStores().keySet().iterator();
         dataStores = new HashMap();
 
         while (i.hasNext()) {
             Object key = i.next();
             dataStores.put(key,
                 new DataStoreConfig(
                     (DataStoreInfoDTO) data.getDataStores().get(key)));
         }
 
         i = data.getNameSpaces().keySet().iterator();
         nameSpaces = new HashMap();
 
         while (i.hasNext()) {
             Object key = i.next();
             nameSpaces.put(key,
                 new NameSpaceConfig(
                     (NameSpaceInfoDTO) data.getNameSpaces().get(key)));
 
             if (((NameSpaceConfig) nameSpaces.get(key)).isDefault()) {
                 defaultNameSpace = (NameSpaceConfig) nameSpaces.get(key);
             }
         }
 
         i = data.getFeaturesTypes().keySet().iterator();
         featuresTypes = new HashMap();
 
         while (i.hasNext()) {
             Object key = i.next();
             System.out.println("DataConf ConstruktDTO key = "+key);
             featuresTypes.put(key,
                 new FeatureTypeConfig(
                     (FeatureTypeInfoDTO) data.getFeaturesTypes().get(key)));
         }
 
         i = data.getStyles().keySet().iterator();
         styles = new HashMap();
 
         while (i.hasNext()) {
             Object key = i.next();
             styles.put(key,
                 new StyleConfig((StyleDTO) data.getStyles().get(key)));
         }
     }
 
     /**
      * Implement loadDTO.
      * 
      * <p>
      * Populates the object with the param passed.
      * </p>
      *
      * @param data An instance of DataDTO to populate this object
      *
      * @throws NullPointerException DOCUMENT ME!
      *
      * @see org.vfny.geoserver.config.DataStructure#loadDTO(java.lang.Object)
      */
     public void update(DataDTO data) {
         if (data == null) {
             throw new NullPointerException("Data Data Transfer Object required");
         }
 
         Iterator i = null;
 
         i = data.getDataStores().keySet().iterator();
         dataStores = new HashMap();
 
         while (i.hasNext()) {
             Object key = i.next();
             dataStores.put(key,
                 new DataStoreConfig(
                     (DataStoreInfoDTO) data.getDataStores().get(key)));
         }
 
         i = data.getNameSpaces().keySet().iterator();
         nameSpaces = new HashMap();
 
         while (i.hasNext()) {
             Object key = i.next();
             nameSpaces.put(key,
                 new NameSpaceConfig(
                     (NameSpaceInfoDTO) data.getNameSpaces().get(key)));
 
             if (((NameSpaceConfig) nameSpaces.get(key)).isDefault()) {
                 defaultNameSpace = (NameSpaceConfig) nameSpaces.get(key);
             }
         }
 
         i = data.getFeaturesTypes().keySet().iterator();
         featuresTypes = new HashMap();
 
         while (i.hasNext()) {
             Object key = i.next();
             System.out.println("DataConf updateDTO key = "+key);
             FeatureTypeInfoDTO f = (FeatureTypeInfoDTO) data.getFeaturesTypes()
                                                             .get(key);
             featuresTypes.put(f.getDataStoreId() + f.getName(),
                 new FeatureTypeConfig(f));
         }
 
         i = data.getStyles().keySet().iterator();
         styles = new HashMap();
 
         while (i.hasNext()) {
             Object key = i.next();
             styles.put(key,
                 new StyleConfig((StyleDTO) data.getStyles().get(key)));
         }
     }
 
     public DataDTO toDTO() {
         DataDTO dt = new DataDTO();
         HashMap tmp = null;
         Iterator i = null;
 
         tmp = new HashMap();
        dt.setDataStores(tmp);
         i = dataStores.keySet().iterator();
 
         while (i.hasNext()) {
             Object key = i.next();
             tmp.put(key, ((DataStoreConfig) dataStores.get(key)).toDTO());
         }
 
 
         tmp = new HashMap();
        dt.setFeaturesTypes(tmp);
         i = featuresTypes.keySet().iterator();
 
         while (i.hasNext()) {
             Object key = i.next();
             tmp.put(key, ((FeatureTypeConfig) featuresTypes.get(key)).toDTO());
         }
 
 
         tmp = new HashMap();
        dt.setStyles(tmp);
         i = styles.keySet().iterator();
 
         while (i.hasNext()) {
             Object key = i.next();
             tmp.put(key, ((StyleConfig) styles.get(key)).toDTO());
         }
 
 
         tmp = new HashMap();
        dt.setNameSpaces(tmp);
         i = nameSpaces.keySet().iterator();
 
         while (i.hasNext()) {
             Object key = i.next();
             tmp.put(key, ((NameSpaceConfig) nameSpaces.get(key)).toDTO());
 
             if (((NameSpaceInfoDTO) tmp.get(key)).isDefault()) {
                 dt.setDefaultNameSpacePrefix(((NameSpaceInfoDTO) tmp.get(key))
                     .getPrefix());
             }
         }
 
 
         return dt;
     }
 
     public List getFeatureTypeConfigKeys() {
         return new ArrayList(featuresTypes.keySet());
     }
 
     /**
      * Lookup FeatureTypeConfig for things like WMS.
      *
      * @param key Key based on <code>dataStoreID.typeName</code>
      *
      * @return FeatureTypeInfo or null if not found
      *
      * @throws NoSuchElementException DOCUMENT ME!
      */
     public FeatureTypeConfig lookupFeatureTypeConfig(String key) {
         if (featuresTypes.containsKey(key)) {
             return (FeatureTypeConfig) featuresTypes.get(key);
         } else {
             throw new NoSuchElementException(
                 "Could not find FeatureTypeConfig '" + key + "'.");
         }
     }
 
     /**
      * getDataStores purpose.
      * 
      * <p>
      * Description ...
      * </p>
      *
      * @return
      */
     public Map getDataStores() {
         return dataStores;
     }
 
     /**
      * List of DataStoreIds
      *
      * @return DOCUMENT ME!
      */
     public List listDataStoreIds() {
         return new ArrayList(dataStores.keySet());
     }
     public List getDataStoreIds(){
         return listDataStoreIds();
     }
 
     /**
      * getDataStores purpose.
      * 
      * <p>
      * Description ...
      * </p>
      *
      * @param key DOCUMENT ME!
      *
      * @return
      */
     public DataStoreConfig getDataStore(String key) {
         return (DataStoreConfig) dataStores.get(key);
     }
 
     /**
      * getDefaultNameSpace purpose.
      * 
      * <p>
      * Description ...
      * </p>
      *
      * @return
      */
     public NameSpaceConfig getDefaultNameSpace() {
         return defaultNameSpace;
     }
 
     /**
      * getFeatures purpose.
      * 
      * <p>
      * Description ...
      * </p>
      *
      * @return
      */
     public Map getFeaturesTypes() {
         return featuresTypes;
     }
 
     /**
      * getFeatures purpose.
      * 
      * <p>
      * Description ...
      * </p>
      *
      * @param key DOCUMENT ME!
      *
      * @return
      */
     public FeatureTypeConfig getFeatureTypeConfig(String key) {
     	System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~FTCONFIG KEYS:::::::");
     	System.out.println(featuresTypes.keySet());
         return (FeatureTypeConfig) featuresTypes.get(key);
     }
 
     /**
      * getNameSpaces purpose.
      * 
      * <p>
      * Description ...
      * </p>
      *
      * @return
      */
     public Map getNameSpaces() {
         return nameSpaces;
     }
 
     /**
      * getNameSpaces purpose.
      * 
      * <p>
      * Description ...
      * </p>
      *
      * @param key DOCUMENT ME!
      *
      * @return
      */
     public NameSpaceConfig getNameSpace(String key) {
         return (NameSpaceConfig) nameSpaces.get(key);
     }
 
     /**
      * getStyles purpose.
      * 
      * <p>
      * Description ...
      * </p>
      *
      * @return
      */
     public Map getStyles() {
         return styles;
     }
 
     /**
      * getStyles purpose.
      * 
      * <p>
      * Description ...
      * </p>
      *
      * @param key DOCUMENT ME!
      *
      * @return
      */
     public StyleConfig getStyle(String key) {
         return (StyleConfig) styles.get(key);
     }
 
     /**
      * setDataStores purpose.
      * 
      * <p>
      * Description ...
      * </p>
      *
      * @param map
      */
     public void setDataStores(Map map) {
         if (map != null) {
             dataStores = map;
         }
     }
 
     /**
      * Add a new DataStoreConfig for the user to edit
      * <p>
      * The DataStoreCondig will be added under its id name
      * </p>
      * @param dataStoreConfig
      */
     public void addDataStore(DataStoreConfig dataStoreConfig) {
         if (dataStores == null) {
             dataStores = new HashMap();
         }
         dataStores.put(dataStoreConfig.getId(), dataStoreConfig);
     }
 
     /**
      * setDataStores purpose.
      * 
      * <p>
      * Description ...
      * </p>
      *
      * @param key
      *
      * @return DOCUMENT ME!
      */
     public DataStoreConfig removeDataStore(String key) {
         if (dataStores == null) {
             dataStores = new HashMap();
         }
 
         return (DataStoreConfig) dataStores.remove(key);
     }
 
     /**
      * setDefaultNameSpace purpose.
      * 
      * <p>
      * Description ...
      * </p>
      *
      * @param support
      */
     public void setDefaultNameSpace(NameSpaceConfig support) {
         if (support != null) {
             defaultNameSpace = support;
         }
     }
 
     /**
      * setFeatures purpose.
      * 
      * <p>
      * Description ...
      * </p>
      *
      * @param map
      */
     public void setFeaturesTypes(Map map) {
         if (map != null) {
             featuresTypes = map;
         }
     }
 
     /**
      * setFeatures purpose.
      * 
      * <p>
      * Description ...
      * </p>
      *
      * @param key
      * @param ft DOCUMENT ME!
      */
     public void addFeatureType(String key, FeatureTypeConfig ft) {
         if (featuresTypes == null) {
             featuresTypes = new HashMap();
         }
 System.out.println("DataConf.addFeatureType key = " +key);
         if ((key != null) && (ft != null)) {
             featuresTypes.put(key, ft);
         }
     }
 
     /**
      * setFeatures purpose.
      * 
      * <p>
      * Description ...
      * </p>
      *
      * @param key
      *
      * @return DOCUMENT ME!
      */
     public FeatureTypeConfig removeFeatureType(String key) {
         if (featuresTypes == null) {
             featuresTypes = new HashMap();
         }
 
         return (FeatureTypeConfig) featuresTypes.remove(key);
     }
 
     /**
      * setNameSpaces purpose.
      * 
      * <p>
      * Description ...
      * </p>
      *
      * @param map
      */
     public void setNameSpaces(Map map) {
         if (map != null) {
             nameSpaces = map;
         }
     }
 
     /**
      * setNameSpaces purpose.
      * 
      * <p>
      * Description ...
      * </p>
      *
      * @param key
      * @param ns DOCUMENT ME!
      */
     public void addNameSpace(String key, NameSpaceConfig ns) {
         if (nameSpaces == null) {
             nameSpaces = new HashMap();
         }
 
         if ((key != null) && (ns != null)) {
             nameSpaces.put(key, ns);
         }
     }
 
     /**
      * setNameSpaces purpose.
      * 
      * <p>
      * Description ...
      * </p>
      *
      * @param key
      *
      * @return DOCUMENT ME!
      */
     public NameSpaceConfig removeNameSpace(String key) {
         if (nameSpaces == null) {
             nameSpaces = new HashMap();
         }
 
         return (NameSpaceConfig) nameSpaces.remove(key);
     }
 
     /**
      * setStyles purpose.
      * 
      * <p>
      * Description ...
      * </p>
      *
      * @param map
      */
     public void setStyles(Map map) {
         if (map != null) {
             styles = map;
         }
     }
 
     /**
      * setStyles purpose.
      * 
      * <p>
      * Description ...
      * </p>
      *
      * @param key
      * @param s DOCUMENT ME!
      */
     public void addStyle(String key, StyleConfig s) {
         if (styles == null) {
             styles = new HashMap();
         }
 
         if ((key != null) && (s != null)) {
             styles.put(key, s);
         }
     }
 
     /**
      * setStyles purpose.
      * 
      * <p>
      * Description ...
      * </p>
      *
      * @param key
      *
      * @return DOCUMENT ME!
      */
     public StyleConfig removeStyle(String key) {
         if (styles == null) {
             styles = new HashMap();
         }
 
         return (StyleConfig) styles.remove(key);
     }
     
     /**
      * 
      * @return a set of all "DataStoreId.TypeName"
      */
     public SortedSet getFeatureTypeIdentifiers(){
     	
     	TreeSet set = new TreeSet();
     	
     	for (Iterator iter = dataStores.values().iterator(); iter.hasNext();) {
 			DataStoreConfig dataStoreConfig = (DataStoreConfig) iter.next();
 			
 			try {
 				DataStore dataStore = dataStoreConfig.findDataStore();
 				
 				String[] typeNames = dataStore.getTypeNames();
 				
 				for (int i = 0; i < typeNames.length; i++) {
 					typeNames[i] = dataStoreConfig.getId()+SEPARATOR+typeNames[i];
 				}
 				
 				List typeNamesList = Arrays.asList(typeNames);
 				
 				set.addAll(typeNamesList);
 				
 			} catch (Throwable ignore) {
 				continue;
 			}
 			
 		}
     	
     	return Collections.unmodifiableSortedSet(set);
     }
 }
