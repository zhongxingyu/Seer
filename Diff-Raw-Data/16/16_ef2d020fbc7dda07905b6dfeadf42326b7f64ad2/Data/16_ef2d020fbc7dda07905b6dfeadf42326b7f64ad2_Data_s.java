 /* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.global;
 
 import org.geotools.data.Catalog;
 import org.geotools.data.DataSourceException;
 import org.geotools.data.DataStore;
 import org.geotools.data.DataStoreMetaData;
 import org.geotools.data.DefaultTransaction;
 import org.geotools.data.FeatureReader;
 import org.geotools.data.FeatureResults;
 import org.geotools.data.FeatureSource;
 import org.geotools.data.FeatureTypeMetaData;
 import org.geotools.data.LockingManager;
 import org.geotools.data.NamespaceMetaData;
 import org.geotools.data.Query;
 import org.geotools.data.Transaction;
 import org.geotools.feature.AttributeType;
 import org.geotools.feature.FeatureType;
 import org.geotools.feature.IllegalAttributeException;
 import org.geotools.styling.SLDStyle;
 import org.geotools.styling.Style;
 import org.geotools.styling.StyleFactory;
 import org.vfny.geoserver.global.dto.AttributeTypeInfoDTO;
 import org.vfny.geoserver.global.dto.DataDTO;
 import org.vfny.geoserver.global.dto.DataStoreInfoDTO;
 import org.vfny.geoserver.global.dto.FeatureTypeInfoDTO;
 import org.vfny.geoserver.global.dto.NameSpaceInfoDTO;
 import org.vfny.geoserver.global.dto.StyleDTO;
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.NoSuchElementException;
 import java.util.Set;
 import java.util.SortedMap;
 import java.util.TreeMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 
 /**
  * This class stores all the information that a catalog would (and
  * CatalogConfig used to).
  *
  * @author Gabriel Roldn
  * @author Chris Holmes
  * @author dzwiers
 * @version $Id: Data.java,v 1.25 2004/01/31 00:27:23 jive Exp $
  */
 public class Data extends GlobalLayerSupertype implements Catalog {
     /** for debugging */
     private static final Logger LOGGER = Logger.getLogger(
             "org.vfny.geoserver.global");
 
     /** Default name of feature type information */
     private static final String INFO_FILE = "info.xml";
 
     /** used to create styles */
     private static StyleFactory styleFactory = StyleFactory.createStyleFactory();
 
     /** holds the mappings between prefixes and NameSpaceInfo objects */
     private Map nameSpaces;
 
     /** NameSpaceInfo */
     private NameSpaceInfo defaultNameSpace;
 
     /** Mapping of DataStoreInfo by dataStoreId */
     private Map dataStores;
 
     /** holds the mapping of Styles and style names */
     private Map styles;
 
     /**
      * Map of <code>FeatureTypeInfo</code>'s stored by full qualified name
      * (NameSpaceInfo prefix + PREFIX_DELIMITER + typeName)
      */
     private Map featureTypes;
 
     /** The DTO for this object */
     private DataDTO catalog;
 
     /**
      * Data constructor.
      * 
      * <p>
      * Creates a Data object from the data provided.
      * </p>
      *
      * @param config DataDTO initial data.
      *
      * @throws ConfigurationException
      */
     public Data(DataDTO config) throws ConfigurationException {
         load(config);
     }
 
     /**
      * Data constructor.
      * 
      * <p>
      * package only constructor for GeoServer to call.
      * </p>
      *
      * @param config DOCUMENT ME!
      *
      * @throws NullPointerException DOCUMENT ME!
      */
 
     /*Data() {
        nameSpaces = new HashMap();
        styles = new HashMap();
        featureTypes = new HashMap();
        dataStores = new HashMap();
        }*/
 
     /**
      * Places the data in this container and innitializes it. Complex tests are
      * performed to detect existing datasources,  while the remainder only
      * include simplistic id checks.
      *
      * @param config
      *
      * @throws NullPointerException
      */
     void load(DataDTO config) {
         catalog = config;
 
         if (config == null) {
             throw new NullPointerException("Non null DataDTO required for load");
         }
 
         // Step 1: load dataStores and Namespaces
         dataStores = loadDataStores(config);
         nameSpaces = loadNamespaces(config);
 
         // Step 2: set up styles
         styles = loadStyles(config);
 
         // Step 3: load featureTypes
         featureTypes = loadFeatureTypes(config);
     }
 
     /**
      * Configure a map of DataStoreInfo by dataStoreId.
      * 
      * <p>
      * This method is order dependent and should be called by load( DataDTO ).
      * This method may have to be smarter in the face of reloads.
      * </p>
      * 
      * <p>
      * Note that any disabled DTO will not be added to the map.
      * </p>
      * 
      * <p>
      * This method is just to make laod( DataDTO ) readable, making it private
      * final will help
      * </p>
      *
      * @param dto
      *
      * @return DOCUMENT ME!
      *
      * @throws NullPointerException DOCUMENT ME!
      */
     private final Map loadDataStores(DataDTO dto) {
         if ((dto == null) || (dto.getDataStores() == null)) {
             throw new NullPointerException(
                 "Non null list of DataStores required");
         }
 
         Map map = new HashMap();
         for (Iterator i = dto.getDataStores().values().iterator(); i.hasNext();) {
             DataStoreInfoDTO dataStoreDTO = (DataStoreInfoDTO) i.next();
             String id = dataStoreDTO.getId();
 
             if (dataStoreDTO.isEnabled()) {
                 DataStoreInfo dataStoreInfo = new DataStoreInfo(dataStoreDTO,
                         this);
 
                 LOGGER.fine("Register DataStore '" + id + "'");
                 map.put(id, dataStoreInfo);
             } else {
                 LOGGER.finer("Did not Register DataStore '" + id
                     + "' as it was not enabled");
             }
         }
 
         return map;
     }
 
     /**
      * Configure a map of NamespaceInfo by prefix.
      * 
      * <p>
      * This method is order dependent and should be called by load( DataDTO ).
      * This method may have to be smarter in the face of reloads.
      * </p>
      * 
      * <p>
      * This method is just to make laod( DataDTO ) readable, making it private
      * final will help
      * </p>
      *
      * @param dto
      *
      * @return DOCUMENT ME!
      *
      * @throws NullPointerException DOCUMENT ME!
      */
     private final Map loadNamespaces(DataDTO dto) {
         if ((dto == null) || (dto.getNameSpaces() == null)) {
             throw new NullPointerException(
                 "Non null list of NameSpaces required");
         }
 
         Map map = new HashMap();
 
         for (Iterator i = dto.getNameSpaces().values().iterator(); i.hasNext();) {
             NameSpaceInfoDTO namespaceDto = (NameSpaceInfoDTO) i.next();
             String prefix = namespaceDto.getPrefix();
             NameSpaceInfo namespaceInfo = new NameSpaceInfo(this, namespaceDto);
 
             map.put(prefix, namespaceInfo);
         }
 
         return map;
     }
     
     /**
      * map of all featureTypeDTO -> load status (Boolean.True, Boolean.False, Exception)
      * Boolean.True when feature was loaded.
      * Boolean.False when something was disabled.
      * Exception the error. 
      */
     private Map errors;
     
     /**
      * Configure a map of FeatureTypeInfo by prefix:typeName.
      * 
      * <p>
      * Note that this map uses namespace prefix (not datastore ID like the the
      * configuration system). That is because this is the actual runtime, in
      * which we access FeatureTypes by namespace. The configuration system
      * uses dataStoreId which is assumed to be more stable across changes (one
      * can reassing a FeatureType to a different namespace, but not a
      * different dataStore).
      * </p>
      * 
      * <p>
      * Note loadDataStores() and loadNamespaces() must be called prior to using
      * this function!
      * </p>
      *
      * @param dto configDTO
      *
      * @return
      *
      * @throws NullPointerException DOCUMENT ME!
      */
     private final Map loadFeatureTypes(DataDTO dto) {
     	errors = new HashMap();
         if ((dto == null) || (dto.getFeaturesTypes() == null)) {
         	errors = null;
             throw new NullPointerException(
                 "Non null list of FeatureTypes required");
         }
         
         Map map = new HashMap();
 
 SCHEMA: 
         for (Iterator i = dto.getFeaturesTypes().values().iterator();
                 i.hasNext();) {
             FeatureTypeInfoDTO featureTypeDTO = (FeatureTypeInfoDTO) i.next();
 
             if (featureTypeDTO == null) {
                 LOGGER.warning("Ignore null FeatureTypeInfo DTO!");
 
                 continue;
             }
 
             String key = featureTypeDTO.getKey(); // dataStoreId:typeName
 
             LOGGER.finer("FeatureType " + key
                 + ": loading feature type info dto:" + featureTypeDTO);
 
             String dataStoreId = featureTypeDTO.getDataStoreId();
             LOGGER.finest("FeatureType " + key + " looking up :" + dataStoreId);
 
             DataStoreInfo dataStoreInfo = (DataStoreInfo) dataStores.get(dataStoreId);
 
             if (dataStoreInfo == null) {
                 LOGGER.severe("FeatureTypeInfo " + key
                     + " could not be used - DataStore " + dataStoreId
                     + " is not defined!");
                 DataStoreInfoDTO tmp = (DataStoreInfoDTO)dto.getDataStores().get(dataStoreId);
                 if(tmp!=null && (!tmp.isEnabled()))
                 	errors.put(featureTypeDTO,Boolean.FALSE);
                 else
                 	errors.put(featureTypeDTO,new ConfigurationException("FeatureTypeInfo " + key
                     + " could not be used - DataStore " + dataStoreId
                     + " is not defined!"));
 
                 continue;
             } else {
                 LOGGER.finest(key + " datastore found :" + dataStoreInfo);
             }
 
             Style s = getStyle(featureTypeDTO.getDefaultStyle());
 
             if (s == null) {
                 LOGGER.severe("FeatureTypeInfo " + key + " ignored - Style '"
                     + featureTypeDTO.getDefaultStyle() + "' not found!");
 
  
                 errors.put(featureTypeDTO,new ConfigurationException("FeatureTypeInfo " + key + " ignored - Style '"
                     + featureTypeDTO.getDefaultStyle() + "' not found!"));
                 continue SCHEMA;
             }
 
             // Check attributes configured correctly against schema
             String typeName = featureTypeDTO.getName();
 
             try {
                 DataStore dataStore = dataStoreInfo.getDataStore();
                 FeatureType featureType = dataStore.getSchema(typeName);
 
                 Set attributeNames = new HashSet();
                 Set ATTRIBUTENames = new HashSet();
 
                 for (int index = 0; index < featureType.getAttributeCount();
                         index++) {
                     AttributeType attrib = featureType.getAttributeType(index);
                     attributeNames.add(attrib.getName());
                     ATTRIBUTENames.add(attrib.getName().toUpperCase());
                 }
                 if(featureTypeDTO.getSchemaAttributes()!=null)
                 for (Iterator a = featureTypeDTO.getSchemaAttributes().iterator();
                         a.hasNext();) {
                     AttributeTypeInfoDTO attribDTO = (AttributeTypeInfoDTO) a
                         .next();
                     String attributeName = attribDTO.getName();
 
                     if (!attributeNames.contains(attributeName)) {
                         if (ATTRIBUTENames.contains(attributeName.toUpperCase())) {
                             LOGGER.severe("FeatureTypeInfo " + key
                                 + " ignored - attribute '" + attributeName
                                 + "' not found - please check captialization");
                         } else {
                             LOGGER.severe("FeatureTypeInfo " + key
                                 + " ignored - attribute '" + attributeName
                                 + "' not found!");
                         }
                        	errors.put(featureTypeDTO,new ConfigurationException("FeatureTypeInfo " + key
                         			+ " could not be used - DataStore " + dataStoreId
 									+ " is not defined!"));
                         continue SCHEMA;
                     }
                 }
             } catch (IllegalStateException e) {
                 LOGGER.severe("FeatureTypeInfo " + key
                     + " ignored - as DataStore " + dataStoreId
                     + " is disabled!");
                 errors.put(featureTypeDTO,Boolean.FALSE);
                 continue;
             } catch (IOException ioException) {
                 LOGGER.log(Level.SEVERE,
                    "FeatureTypeInfo " + key + " ignored - ad DataStore "
                    + dataStoreId + " is broken", ioException);
 
                 errors.put(featureTypeDTO,ioException);
                 continue;
             }
 
             String prefix = dataStoreInfo.getNamesSpacePrefix();
 
             LOGGER.finest("FeatureType " + key
                 + " creating FeatureTypeInfo for " + prefix + ":" + typeName);
 
             FeatureTypeInfo featureTypeInfo = null;
 
             try {
                 featureTypeInfo = new FeatureTypeInfo(featureTypeDTO, this);
             } catch (ConfigurationException configException) {
                 LOGGER.log(Level.SEVERE,
                     "FeatureTypeInfo " + key
                     + " ignored - due to a configuration problem",
                     configException);
 
                 errors.put(featureTypeDTO,configException);
                 continue;
             }
 
             String key2 = prefix + ":" + typeName;
 
             if (map.containsKey(key2)) {
                 LOGGER.severe("FeatureTypeInfo '" + key2
                     + "' already defined - you must have duplicate defined?");
                 errors.put(featureTypeDTO,new ConfigurationException("FeatureTypeInfo '" + key2
                 		+ "' already defined - you must have duplicate defined?"));
             } else {
                 LOGGER.finest("FeatureTypeInfo " + key2
                     + " has been created...");
                 map.put(key2, featureTypeInfo);
 
                 LOGGER.finest("FeatureTypeInfo '" + key2 + "' is registered:"
                     + dataStoreInfo);
                 errors.put(featureTypeDTO,Boolean.TRUE);
             }
         }
 
         return map;
     }
 
     /**
      * Generate map of geotools2 Styles by id.
      * 
      * <p>
      * The filename specified by the StyleDTO will be used to generate the
      * resulting Styles.
      * </p>
      *
      * @param dto requested configuration
      *
      * @return Map of Style by id
      *
      * @throws NullPointerException If the style could not be loaded from the
      *         filename
      *
      * @see Data.loadStyle() for more information
      */
     private final Map loadStyles(DataDTO dto) {
         Map map = new HashMap();
 
         if ((dto == null) || (dto.getStyles() == null)) {
             throw new NullPointerException("List of styles is required");
         }
 
         for (Iterator i = dto.getStyles().values().iterator(); i.hasNext();) {
             StyleDTO styleDTO = (StyleDTO) i.next();
             String id = styleDTO.getId();
             Style style;
 
             try {
                 style = loadStyle(styleDTO.getFilename());
             } catch (IOException ioException) {
                 LOGGER.log(Level.SEVERE, "Could not load style " + id,
                     ioException);
 
                 continue;
             }
 
             map.put(id, style);
         }
 
         return map;
     }
 
     /**
      * Status output
      *
      * @param title DOCUMENT ME!
      * @param status DOCUMENT ME!
      */
     static final void outputStatus(String title, Map status) {
         LOGGER.info(title);
 
         for (Iterator i = status.entrySet().iterator(); i.hasNext();) {
             Map.Entry entry = (Map.Entry) i.next();
             String key = (String) entry.getKey();
             Object value = entry.getValue();
 
             if (value == Boolean.TRUE) {
                 LOGGER.info(key + ": ready");
             } else if (value instanceof Throwable) {
                 Throwable t = (Throwable) value;
                 LOGGER.log(Level.SEVERE, key + " not ready", t);
             } else {
                 LOGGER.warning(key + ": '" + value + "'");
             }
         }
     }
 
     /**
      * Dynamically tries to connect to every DataStore!
      * 
      * <p>
      * Returns a map of Exception by dataStoreId:typeName. If by some marvel
      * the we could connect to a FeatureSource we will record Boolean.TRUE.
      * </p>
      *
      * @return Map of Exception by dataStoreId:typeName
      */
     public Map statusDataStores() {
     	Map m = new HashMap();
     	Iterator i = errors.entrySet().iterator();
     	while(i.hasNext()){
     		Map.Entry e = (Map.Entry)i.next();
     		FeatureTypeInfoDTO ftdto = (FeatureTypeInfoDTO)e.getKey();
     		m.put(ftdto.getDataStoreId()+":"+ftdto.getName(),e.getValue());
     	}
     	return m;
     }
 
     /**
      * Dynamically tries to connect to every Namespace!
      * 
      * <p>
      * Returns a map of Exception by prefix:typeName. If by some marvel the we
      * could connect to a FeatureSource we will record Boolean.TRUE.
      * </p>
      *
      * @return Map of Exception by prefix:typeName
      */
     public Map statusNamespaces() {
     	Map m = new HashMap();
     	Iterator i = errors.entrySet().iterator();
     	while(i.hasNext()){
     		Map.Entry e = (Map.Entry)i.next();
     		FeatureTypeInfoDTO ftdto = (FeatureTypeInfoDTO)e.getKey();
     		DataStoreInfoDTO dsdto = (DataStoreInfoDTO)catalog.getDataStores().get(ftdto.getDataStoreId());
     		if(dsdto != null){
     			m.put(dsdto.getNameSpaceId()+":"+ftdto.getName(),e.getValue());
     		}
     	}
     	return m;
     }
 
     /**
      * Dynamically tries to connect to this DataStore!
      * 
      * <p>
      * Returns a map of Exception by dataStoreId:typeName. If by some marvel
      * the we could connect to a FeatureSource we will record Boolean.TRUE.
      * </p>
      * 
      * <p>
      * We will make an initial attempt to connect to the real dataStore (with
      * out any metadata based filtering or retyping. Only if this proceeds
      * will we try for getStatus( FeatureTypeInfo ).
      * </p>
      *
      * @param info DOCUMENT ME!
      *
      * @return Map of Exception by dataStoreId:typeName
      */
     /*public SortedMap status(DataStoreInfo info) {
         SortedMap status = new TreeMap();
 
         String id = info.getId();
         LOGGER.finer(id + ": checking status of DataStore!");
         LOGGER.finest(id + ": namespace prefix '" + info.getNamesSpacePrefix()
             + "'");
         LOGGER.finest(id + ": title '" + info.getTitle() + "'");
         LOGGER.finest(id + ": enabled " + info.isEnabled());
 
         DataStore store = null;
 
         try {
             store = info.getDataStore();
         } catch (Throwable couldNotConnect) {
             LOGGER.warning(id + ": Could not connect to DataStore!");
 
             //couldNotConnect.printStackTrace();
             status.put(id, couldNotConnect);
 
             return status;
         }
 
         String[] typeNames = store.getTypeNames();
 
         for (int t = 0; t < typeNames.length; t++) {
             String typeName = typeNames[t];
 
             try {
                 assertWorking(store, typeName);
                 status.put(id + ":" + typeName, Boolean.TRUE);
             } catch (Throwable didNotWork) {
                 LOGGER.warning(id + ":" + typeName
                     + ": geotools2 FeatureSource did not work!");
 
                 //didNotWork.printStackTrace();
                 status.put(id + ":" + typeName, didNotWork);
             }
         }
 
         return status;
     }*/
 
     /*public SortedMap status(NameSpaceInfo info) {
         SortedMap status = new TreeMap();
 
         String prefix = info.getPrefix();
         LOGGER.finer(prefix + ": checking status of Namespace!");
         LOGGER.finest(prefix + ": namespace prefix '" + info.getPrefix() + "'");
         LOGGER.finest(prefix + ": uri '" + info.getURI() + "'");
         LOGGER.finest(prefix + ": default " + info.isDefault());
 
         for (Iterator i = info.getTypeNames().iterator(); i.hasNext();) {
             String typeName = (String) i.next();
 
             FeatureTypeInfo typeInfo = null;
             if(!typeInfo.isEnabled()){
             	status.put(prefix+":"+typeName, Boolean.FALSE);
             	continue;
             }
             try {
                 typeInfo = (FeatureTypeInfo) info.getFeatureTypeMetaData(typeName);
                 assertWorking(typeInfo);
                 status.put(prefix + ":" + typeName, Boolean.TRUE);
             } catch (Throwable badInfo) {
                 LOGGER.warning(prefix + ":" + typeName
                     + ": FeatureTypeInfo did not work!");
 
                 //badInfo.printStackTrace();
                 status.put(prefix + ":" + typeName, badInfo);
             }
         }
 
         return status;
     }*/
 
     /*public void assertWorking(FeatureTypeInfo info) throws IOException {
         String id = info.getPrefix() + ":" + info.getName();
 
         LOGGER.finest(id + ": check status of GeoServer FeatureTypeInfo");
         LOGGER.finest(id + ": name:'" + info.getName() + "'");
         LOGGER.finest(id + ": prefix:'" + info.getPrefix() + "'");
         LOGGER.finest(id + ": schema base:'" + info.getSchemaBase() + "'");
         LOGGER.finest(id + ": schema name:'" + info.getSchemaName() + "'");
         LOGGER.finest(id + ": schema title:'" + info.getTitle() + "'");
         LOGGER.finest(id + ": schema abstract:'" + info.getAbstract() + "'");
         LOGGER.finest(id + ": schema typeName:'" + info.getTypeName() + "'");
         LOGGER.finest(id + ": schema query:'" + info.getDefinitionQuery() + "'");
         LOGGER.finest(id + ": schema keywords:'" + info.getKeywords() + "'");
         LOGGER.finest(id + ": schema bounds:'" + info.getLatLongBoundingBox()
             + "'");
 
         FeatureType featureType = info.getFeatureType();
         LOGGER.finest(id + ": featureType '" + featureType + "'");
 
         FeatureSource source = info.getFeatureSource();
         LOGGER.finest(id + ": source aquired '" + source + "'");
 
         assertWorking(source);
 
         LOGGER.finest(id + ": schema attributeNames:'"
             + info.getAttributeNames() + "'");
         LOGGER.finest(id + ": schema schema:'" + info.getXMLSchema() + "'");
     }*/
 
     /**
      * Assert that GeoTools2 typeName exists and works for typeName
      *
      * @param datastore DOCUMENT ME!
      * @param typeName DOCUMENT ME!
      *
      * @throws IOException DOCUMENT ME!
      */
     /*public void assertWorking(DataStore datastore, String typeName)
         throws IOException {
         LOGGER.finest(typeName + ": check status of GeoTools2 FeatureType");
 
         FeatureType featureType = datastore.getSchema(typeName);
         LOGGER.finest(typeName + ": featureType '" + featureType + "'");
 
         FeatureSource source = null;
         source = datastore.getFeatureSource(typeName);
         LOGGER.finest(typeName + ": source aquired '" + source + "'");
         assertWorking(source);
     }*/
 
     /**
      * Test that the FeatureSource works.
      * 
      * <p>
      * A smattering of tests, used to check the status of a FeatureSource.
      * </p>
      *
      * @param source FeatureSource being tested
      *
      * @throws IOException If the FeatureSource does not work
      * @throws DataSourceException DOCUMENT ME!
      */
     /*public void assertWorking(FeatureSource source) throws IOException {
         String id = source.getSchema().getTypeName();
 
         // Test optimized getCount()
         //
         LOGGER.finest(id + ": source count optimized:'"
             + source.getCount(Query.ALL) + "'");
 
         FeatureResults all = source.getFeatures();
 
         // Test High Level FeatureResults API
         LOGGER.finest(id + ": source count results:'" + all.getCount() + "'");
 
         // Test Low Level FeatureReader API
         //
         FeatureReader reader = all.reader();
 
         try {
             boolean hasNext = reader.hasNext();
             LOGGER.finest(id + ": reader hasNext()" + hasNext);
 
             if (hasNext) {
                 LOGGER.finest(id + ": reader next()" + reader.next());
             }
         } catch (NoSuchElementException e) {
             throw new DataSourceException(e.getMessage(), e);
         } catch (IllegalAttributeException e) {
             throw new DataSourceException(e.getMessage(), e);
         } finally {
             reader.close();
         }
 
         LOGGER.finest(id + ": source aquired '" + source + "'");
     }*/
 
     /**
      * getDataStoreInfos purpose.
      * 
      * <p>
      * A list of the posible DataStoreInfo
      * </p>
      *
      * @return DataStoreInfo[] list of the posible DataStoreInfo
      */
 
     /*public DataStoreInfo[] getDataStoreInfos() {
        List dslist = new ArrayList(dataStores.values());
        DataStoreInfo[] dStores = new DataStoreInfo[dslist.size()];
        dStores = (DataStoreInfo[]) dslist.toArray(dStores);
        return dStores;
        }*/
 
     /**
      * toDTO purpose.
      * 
      * <p>
      * This method is package visible only, and returns a reference to the
      * GeoServerDTO. This method is unsafe, and should only be used with
      * extreme caution.
      * </p>
      *
      * @return DataDTO the generated object
      */
     Object toDTO() {
         return catalog;
     }
 
     /**
      * Locate a DataStoreInfo by its id attribute.
      *
      * @param id the DataStoreInfo id looked for
      *
      * @return the DataStoreInfo with id attribute equals to <code>id</code> or
      *         null if there no exists
      */
     public DataStoreInfo getDataStoreInfo(String id) {
         return (DataStoreInfo) dataStores.get(id);
     }
 
     /**
      * getNameSpaces purpose.
      * 
      * <p>
      * List of all relevant namespaces
      * </p>
      *
      * @return NameSpaceInfo[]
      */
     public NameSpaceInfo[] getNameSpaces() {
         NameSpaceInfo[] ns = new NameSpaceInfo[nameSpaces.values().size()];
 
         return (NameSpaceInfo[]) new ArrayList(nameSpaces.values()).toArray(ns);
     }
 
     /**
      * getNameSpace purpose.
      * 
      * <p>
      * The NameSpaceInfo from the specified prefix
      * </p>
      *
      * @param prefix
      *
      * @return NameSpaceInfo resulting from the specified prefix
      */
     public NameSpaceInfo getNameSpace(String prefix) {
         NameSpaceInfo retNS = (NameSpaceInfo) nameSpaces.get(prefix);
 
         return retNS;
     }
 
     /**
      * getDefaultNameSpace purpose.
      * 
      * <p>
      * Returns the default NameSpaceInfo for this Data object.
      * </p>
      *
      * @return NameSpaceInfo the default name space
      */
     public NameSpaceInfo getDefaultNameSpace() {
         return defaultNameSpace;
     }
 
     /**
      * getStyles purpose.
      * 
      * <p>
      * A reference to the map of styles
      * </p>
      *
      * @return Map A map containing the Styles.
      */
     public Map getStyles() {
         return this.styles;
     }
 
     public Style getStyle(String id) {
         return (Style) styles.get(id);
     }
 
     /**
      * getFeatureTypeInfo purpose.
      * 
      * <p>
      * returns the FeatureTypeInfo for the specified unique name
      * </p>
      *
      * @param typeName String The FeatureTypeInfo Name
      *
      * @return FeatureTypeInfo
      *
      * @throws NoSuchElementException
      */
     public FeatureTypeInfo getFeatureTypeInfo(String typeName)
         throws NoSuchElementException {
         LOGGER.finest("getting type " + typeName);
 
         FeatureTypeInfo ftype = (FeatureTypeInfo) featureTypes.get(typeName);
 
         if (ftype == null) {
             throw new NoSuchElementException(
                 "there is no FeatureTypeConfig named " + typeName
                 + " configured in this server");
         }
 
         return ftype;
     }
 
     /**
      * Gets a FeatureTypeInfo from a local type name (ie unprefixed), and a
      * uri.
      * 
      * <p>
      * This method is slow, use getFeatureType(String typeName), where
      * possible.  For not he only user should be TransactionFeatureHandler.
      * </p>
      * 
      * <p>
      * TODO: Jody here - David is this still correct?
      * </p>
      *
      * @param namespacePrefix Name NameSpaceInfo name
      * @param uri NameSpaceInfo uri
      *
      * @return FeatureTypeInfo
      */
     public FeatureTypeInfo getFeatureTypeInfo(String namespacePrefix, String uri) {
         for (Iterator it = featureTypes.values().iterator(); it.hasNext();) {
             FeatureTypeInfo fType = (FeatureTypeInfo) it.next();
 
             if (fType.isEnabled()) {
                 if (fType.getShortName().equals(namespacePrefix)
                         && fType.getNameSpace().getUri().equals(uri)) {
                     return fType;
                 }
             }
         }
 
         return null;
     }
 
     /**
      * Retrieve map of FeatureTypeInfo by prefix:typeName.
      * 
      * <p>
      * Returns all the featuretype information objects
      * </p>
      *
      * @return Map of FetureTypeInfo by prefix:typeName
      */
     public Map getFeatureTypeInfos() {
         return Collections.unmodifiableMap(featureTypes);
     }
 
     //TODO: detect if a user put a full url, instead of just one to be resolved, and
     //use that instead.
     public Style loadStyle(String fileName, String base)
         throws IOException {
         URL url;
 
         //HACK: but I'm not sure if we can get the GeoServer instance.  This is one thing
         //that will benefit from splitting up of config loading from representation.
         url = new File(base + fileName).toURL();
 
         SLDStyle stylereader = new SLDStyle(styleFactory, url);
         Style[] layerstyle = stylereader.readXML();
 
         return layerstyle[0];
     }
 
     /**
      * Load GeoTools2 Style from a fileName
      *
      * @param fileName DOCUMENT ME!
      *
      * @return DOCUMENT ME!
      *
      * @throws IOException DOCUMENT ME!
      */
     public Style loadStyle(File fileName) throws IOException {
         URL url;
 
         //HACK: but I'm not sure if we can get the GeoServer instance.  This is one thing
         //that will benefit from splitting up of config loading from representation.
         //
         url = (fileName).toURL();
 
         SLDStyle stylereader = new SLDStyle(styleFactory, url);
         Style[] layerstyle = stylereader.readXML();
 
         return layerstyle[0];
     }
 
     /**
      * tests whether a given file is a file containing type information.
      *
      * @param testFile the file to test.
      *
      * @return <tt>true</tt> if the file has type info.
      */
     private static boolean isInfoFile(File testFile) {
         String testName = testFile.getAbsolutePath();
         int start = testName.length() - INFO_FILE.length();
         int end = testName.length();
 
         return testName.substring(start, end).equals(INFO_FILE);
     }
 
     /**
      * Release lock by authorization
      *
      * @param lockID
      */
     public void lockRelease(String lockID) {
         boolean refresh = false;
 
         for (Iterator i = dataStores.values().iterator(); i.hasNext();) {
             DataStoreInfo meta = (DataStoreInfo) i.next();
 
             if (!meta.isEnabled()) {
                 continue; // disabled
             }
 
             DataStore dataStore;
 
             try {
                 dataStore = meta.getDataStore();
             } catch (IllegalStateException notAvailable) {
                 continue; // not available
             }
 
             LockingManager lockingManager = dataStore.getLockingManager();
 
             if (lockingManager == null) {
                 continue; // locks not supported
             }
 
             Transaction t = new DefaultTransaction("Refresh "
                     + meta.getNameSpace());
 
             try {
                 t.addAuthorization(lockID);
 
                 if (lockingManager.release(lockID, t)) {
                     refresh = true;
                 }
             } catch (IOException e) {
                 LOGGER.log(Level.WARNING, e.getMessage(), e);
             } finally {
                 try {
                     t.close();
                 } catch (IOException closeException) {
                     LOGGER.log(Level.FINEST, closeException.getMessage(),
                         closeException);
                 }
             }
         }
 
         if (!refresh) {
             // throw exception? or ignore...
         }
     }
 
     /**
      * Refresh lock by authorization
      * 
      * <p>
      * Should use your own transaction?
      * </p>
      *
      * @param lockID
      */
     public void lockRefresh(String lockID) {
         boolean refresh = false;
 
         for (Iterator i = dataStores.values().iterator(); i.hasNext();) {
             DataStoreInfo meta = (DataStoreInfo) i.next();
 
             if (!meta.isEnabled()) {
                 continue; // disabled
             }
 
             DataStore dataStore;
 
             try {
                 dataStore = meta.getDataStore();
             } catch (IllegalStateException notAvailable) {
                 continue; // not available
             }
 
             LockingManager lockingManager = dataStore.getLockingManager();
 
             if (lockingManager == null) {
                 continue; // locks not supported
             }
 
             Transaction t = new DefaultTransaction("Refresh "
                     + meta.getNameSpace());
 
             try {
                 t.addAuthorization(lockID);
 
                 if (lockingManager.refresh(lockID, t)) {
                     refresh = true;
                 }
             } catch (IOException e) {
                 LOGGER.log(Level.WARNING, e.getMessage(), e);
             } finally {
                 try {
                     t.close();
                 } catch (IOException closeException) {
                     LOGGER.log(Level.FINEST, closeException.getMessage(),
                         closeException);
                 }
             }
         }
 
         if (!refresh) {
             // throw exception? or ignore...
         }
     }
 
     /**
      * Implement lockRefresh.
      *
      * @param lockID
      * @param t
      *
      * @return true if lock was found and refreshed
      *
      * @throws IOException
      *
      * @see org.geotools.data.Data#lockRefresh(java.lang.String,
      *      org.geotools.data.Transaction)
      */
     public boolean lockRefresh(String lockID, Transaction t)
         throws IOException {
         boolean refresh = false;
 
         for (Iterator i = dataStores.values().iterator(); i.hasNext();) {
             DataStoreInfo meta = (DataStoreInfo) i.next();
 
             if (!meta.isEnabled()) {
                 continue; // disabled
             }
 
             DataStore dataStore;
 
             try {
                 dataStore = meta.getDataStore();
             } catch (IllegalStateException notAvailable) {
                 continue; // not available
             }
 
             LockingManager lockingManager = dataStore.getLockingManager();
 
             if (lockingManager == null) {
                 continue; // locks not supported
             }
 
             if (lockingManager.refresh(lockID, t)) {
                 refresh = true;
             }
         }
 
         return refresh;
     }
 
     /**
      * Implement lockRelease.
      *
      * @param lockID
      * @param t
      *
      * @return true if the lock was found and released
      *
      * @throws IOException
      *
      * @see org.geotools.data.Data#lockRelease(java.lang.String,
      *      org.geotools.data.Transaction)
      */
     public boolean lockRelease(String lockID, Transaction t)
         throws IOException {
         boolean release = false;
 
         for (Iterator i = dataStores.values().iterator(); i.hasNext();) {
             DataStoreInfo meta = (DataStoreInfo) i.next();
 
             if (!meta.isEnabled()) {
                 continue; // disabled
             }
 
             DataStore dataStore;
 
             try {
                 dataStore = meta.getDataStore();
             } catch (IllegalStateException notAvailable) {
                 continue; // not available
             }
 
             LockingManager lockingManager = dataStore.getLockingManager();
 
             if (lockingManager == null) {
                 continue; // locks not supported
             }
 
             if (lockingManager.release(lockID, t)) {
                 release = true;
             }
         }
 
         return release;
     }
 
     /**
      * Implement lockExists.
      *
      * @param lockID
      *
      * @return true if lockID exists
      *
      * @see org.geotools.data.Data#lockExists(java.lang.String)
      */
     public boolean lockExists(String lockID) {
         for (Iterator i = dataStores.values().iterator(); i.hasNext();) {
             DataStoreInfo meta = (DataStoreInfo) i.next();
 
             if (!meta.isEnabled()) {
                 continue; // disabled
             }
 
             DataStore dataStore;
 
             try {
                 dataStore = meta.getDataStore();
             } catch (IllegalStateException notAvailable) {
                 continue; // not available
             }
 
             LockingManager lockingManager = dataStore.getLockingManager();
 
             if (lockingManager == null) {
                 continue; // locks not supported
             }
 
             if (lockingManager.exists(lockID)) {
                 return true;
             }
         }
 
         return false;
     }
 
     //
     // GeoTools2 Catalog API
     // 
 
     /**
      * Set of available Namespace prefixes.
      *
      * @return Set of namespace Prefixes
      *
      * @see org.geotools.data.Catalog#getPrefixes()
      */
     public Set getPrefixes() {
         return Collections.unmodifiableSet(nameSpaces.keySet());
     }
 
     /**
      * Prefix of the defaultNamespace.
      *
      * @return prefix of the default namespace
      *
      * @see org.geotools.data.Catalog#getDefaultPrefix()
      */
     public String getDefaultPrefix() {
         return defaultNameSpace.getPrefix();
     }
 
     /**
      * Implement getNamespace.
      * 
      * <p>
      * Description ...
      * </p>
      *
      * @param prefix
      *
      * @return
      *
      * @see org.geotools.data.Catalog#getNamespace(java.lang.String)
      */
     public NamespaceMetaData getNamespaceMetaData(String prefix) {
         return (NamespaceMetaData) getNameSpace(prefix);
     }
 
     /**
      * Register a DataStore with this Catalog.
      * 
      * <p>
      * This is part of the public CatalogAPI, the fact that we don't want to
      * support it here may be gounds for it's removal.
      * </p>
      * 
      * <p>
      * GeoSever and the global package would really like to have
      * <b>complete</b> control over the DataStores in use by the application.
      * It recognize that this may not always be possible. As GeoServer is
      * extend with additional Modules (such as config) that wish to locate and
      * talk to DataStores independently of GeoServer the best we can do is ask
      * them to register with the this Catalog in global.
      * </p>
      * 
      * <p>
      * This reveals what may be a deisgn flaw in GeoTools2 DataStore. We have
      * know way of knowing if the dataStore has already been placed into our
      * care as DataStores are not good at identifying themselves. To
      * complicate matters most keep a static connectionPool around in their
      * Factory - it could be that the Factories are supposed to be smart
      * enough to prevent duplication.
      * </p>
      *
      * @param dataStore
      *
      * @throws IOException
      *
      * @see org.geotools.data.Catalog#registerDataStore(org.geotools.data.DataStore)
      */
     public void registerDataStore(DataStore dataStore)
         throws IOException {
     }
 
     /**
      * Access to the set of DataStores in use by GeoServer.
      * 
      * <p>
      * The provided Set may not be modified :-)
      * </p>
      *
      * @return
      *
      * @see org.geotools.data.Catalog#getDataStores(java.lang.String)
      */
     public Set getDataStores() {
         return Collections.unmodifiableSet(new HashSet(dataStores.values()));
     }
 
     /**
      * Convience method for Accessing FeatureSource by prefix:typeName.
      * 
      * <p>
      * This method is part of the public Catalog API. It allows the Validation
      * framework to be writen using only public Geotools2 interfaces.
      * </p>
      *
      * @param prefix Namespace prefix in which the FeatureType available
      * @param typeName typeNamed used to identify FeatureType
      *
      * @return
      *
      * @throws IOException DOCUMENT ME!
      *
      * @see org.geotools.data.Catalog#getFeatureSource(java.lang.String,
      *      java.lang.String)
      */
     public FeatureSource getFeatureSource(String prefix, String typeName)
         throws IOException {
         NamespaceMetaData namespace = getNamespaceMetaData(prefix);
         FeatureTypeMetaData featureType = namespace.getFeatureTypeMetaData(typeName);
         DataStoreMetaData dataStore = featureType.getDataStoreMetaData();
 
         return dataStore.getDataStore().getFeatureSource(typeName);
     }
 }
