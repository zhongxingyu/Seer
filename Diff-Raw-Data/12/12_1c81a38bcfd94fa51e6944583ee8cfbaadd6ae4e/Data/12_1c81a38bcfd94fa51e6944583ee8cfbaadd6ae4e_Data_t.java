 /* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.global;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.NoSuchElementException;
 import java.util.Set;
 import java.util.logging.Level;
 
 import org.geotools.data.DataStore;
 import org.geotools.data.DefaultTransaction;
 import org.geotools.data.FeatureSource;
 import org.geotools.data.LockingManager;
 import org.geotools.data.Transaction;
 import org.geotools.feature.AttributeType;
 import org.geotools.feature.FeatureType;
 import org.geotools.styling.SLDParser;
 import org.geotools.styling.Style;
 import org.geotools.styling.StyleFactory;
 import org.vfny.geoserver.global.dto.AttributeTypeInfoDTO;
 import org.vfny.geoserver.global.dto.DataDTO;
 import org.vfny.geoserver.global.dto.DataStoreInfoDTO;
 import org.vfny.geoserver.global.dto.DataTransferObjectFactory;
 import org.vfny.geoserver.global.dto.FeatureTypeInfoDTO;
 import org.vfny.geoserver.global.dto.NameSpaceInfoDTO;
 import org.vfny.geoserver.global.dto.StyleDTO;
 
 
 /**
  * This class stores all the information that a catalog would (and
  * CatalogConfig used to).
  *
  * @author Gabriel Roldan, Axios Engineering
  * @author Chris Holmes
  * @author dzwiers
  * @version $Id: Data.java,v 1.45 2004/09/13 16:05:10 cholmesny Exp $
  */
 public class Data extends GlobalLayerSupertype /*implements Repository*/ {
     public static final String WEB_CONTAINER_KEY = "DATA";
 
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
     private Map stFiles;
 
     /**
      * Map of <code>FeatureTypeInfo</code>'s stored by full qualified name
      * (NameSpaceInfo prefix + PREFIX_DELIMITER + typeName)
      */
     private Map featureTypes;
 
     /** Base directory for use with file based relative paths */
     private File baseDir;
 
     /**
      * Data constructor.
      * 
      * <p>
      * Creates a Data object from the data provided.
      * </p>
      */
     private GeoServer gs;
 
     /**
      * map of all featureTypeDTO -> load status (Boolean.True, Boolean.False,
      * Exception) Boolean.True when feature was loaded. Boolean.False when
      * something was disabled. Exception the error.
      */
     private Map errors;
 
     public Data(DataDTO config, File dir, GeoServer g)
         throws ConfigurationException {
         baseDir = dir;
         load(config);
         gs = g;
     }
 
     public Data(File dir, GeoServer g) throws ConfigurationException {
         baseDir = dir;
         gs = g;
     }
 
     GeoServer getGeoServer() {
         return gs;
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
     public void load(DataDTO config) {
         if (config == null) {
             throw new NullPointerException("Non null DataDTO required for load");
         }
 
         // Step 1: load dataStores and Namespaces
         dataStores = loadDataStores(config);
         nameSpaces = loadNamespaces(config);
         defaultNameSpace = (NameSpaceInfo) nameSpaces.get(config
                 .getDefaultNameSpacePrefix());
 
         // Step 2: set up styles
         styles = loadStyles(config);
 
         // Step 3: load featureTypes
         featureTypes = loadFeatureTypes(config);
     }
 
     public Set getDataStores() {
         return new HashSet(dataStores.values());
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
 
             DataStoreInfo dataStoreInfo = new DataStoreInfo(dataStoreDTO, this);
             map.put(id, dataStoreInfo);
 
             if (dataStoreDTO.isEnabled()) {
                 LOGGER.finer("Register DataStore '" + id + "'");
             } else {
                 LOGGER.fine("Did not Register DataStore '" + id
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
         featureTypes = new HashMap(); // to fix lazy loading
 
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
 
                 DataStoreInfoDTO tmp = (DataStoreInfoDTO) dto.getDataStores()
                                                              .get(dataStoreId);
 
                 if ((tmp != null) && (!tmp.isEnabled())) {
                     errors.put(featureTypeDTO, Boolean.FALSE);
                 } else {
                     errors.put(featureTypeDTO,
                         new ConfigurationException("FeatureTypeInfo " + key
                             + " could not be used - DataStore " + dataStoreId
                             + " is not defined!"));
                 }
 
                 continue;
             } else {
                 LOGGER.finest(key + " datastore found :" + dataStoreInfo);
             }
 
             Style s = getStyle(featureTypeDTO.getDefaultStyle());
             
             if (s == null) {
                 LOGGER.severe("FeatureTypeInfo " + key + " ignored - Style '"
                     + featureTypeDTO.getDefaultStyle() + "' not found!");
 
                 errors.put(featureTypeDTO,
                     new ConfigurationException("FeatureTypeInfo " + key
                         + " ignored - Style '"
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
 
                 //as far as I can tell an emtpy list indicates that no 
                 //schema.xml file was found.  I may be approaching this 
                 //all wrong, is this logic contained elsewhere?
                 //CH: Yeah, this shit was super messed up.  It was causing null pointer
                 //exceptions, and then it created this createAttrDTO flag that wasn't
                 //then used by anyone.  So I fixed the null and made it so it creates
                 //AttributeTypeInfoDTO's (once again, I hate these) from the FeatureType
                 //of the real datastore.
                 //boolean createAttrDTO = (featureTypeDTO.getSchemaAttributes().size() == 0);
                 LOGGER.fine("loading datastore " + typeName);
 
                 boolean createAttrDTO;
 
                 if (featureTypeDTO.getSchemaAttributes() == null) {
                     createAttrDTO = true;
                 } else {
                     createAttrDTO = featureTypeDTO.getSchemaAttributes().size() == 0;
                 }
 
                 if (createAttrDTO) {
                     List attributeDTOs = createAttrDTOsFromSchema(featureType);
                     featureTypeDTO.setSchemaAttributes(attributeDTOs);
                     LOGGER.finer(
                         "No schema found, setting featureTypeDTO with "
                         + attributeDTOs);
                 } else {
                     for (int index = 0;
                             index < featureType.getAttributeCount(); index++) {
                         AttributeType attrib = featureType.getAttributeType(index);
                         attributeNames.add(attrib.getName());
                         ATTRIBUTENames.add(attrib.getName().toUpperCase());
                     }
 
                     if (featureTypeDTO.getSchemaAttributes() != null) {
                         for (Iterator a = featureTypeDTO.getSchemaAttributes()
                                                         .iterator();
                                 a.hasNext();) {
                             AttributeTypeInfoDTO attribDTO = (AttributeTypeInfoDTO) a
                                 .next();
                             String attributeName = attribDTO.getName();
 
                             if (!attributeNames.contains(attributeName)) {
                                 if (ATTRIBUTENames.contains(
                                             attributeName.toUpperCase())) {
                                     LOGGER.severe("FeatureTypeInfo " + key
                                         + " ignored - attribute '"
                                         + attributeName
                                         + "' not found - please check captialization");
                                 } else {
                                     LOGGER.severe("FeatureTypeInfo " + key
                                         + " ignored - attribute '"
                                         + attributeName + "' not found!");
 
                                     String names = "";
                                     Iterator x = attributeNames.iterator();
 
                                     if (x.hasNext()) {
                                         names = x.next().toString();
                                     }
 
                                     while (x.hasNext())
                                         names = ":::" + x.next().toString();
 
                                     LOGGER.severe(
                                         "Valid attribute names include :::"
                                         + names);
                                 }
 
                                 errors.put(featureTypeDTO,
                                     new ConfigurationException(
                                         "FeatureTypeInfo " + key
                                         + " could not be used - DataStore "
                                         + dataStoreId + " is not defined!"));
 
                                 continue SCHEMA;
                             }
                         }
                     }
                 }
             } catch (IllegalStateException illegalState) {
                 LOGGER.severe("FeatureTypeInfo " + key
                     + " ignored - as DataStore " + dataStoreId
                     + " is disabled!");
                 errors.put(featureTypeDTO, Boolean.FALSE);
 
                 continue;
             } catch (IOException ioException) {
                 LOGGER.log(Level.SEVERE,
                     "FeatureTypeInfo " + key + " ignored - as DataStore "
                     + dataStoreId + " is unavailable:" + ioException);
                 LOGGER.log(Level.FINEST, key + " unavailable", ioException);
                 errors.put(featureTypeDTO, ioException);
 
                 continue;
             } catch (Throwable unExpected) {
                 LOGGER.log(Level.SEVERE,
                     "FeatureTypeInfo " + key + " ignored - as DataStore "
                     + dataStoreId + " is broken:" + unExpected);
                 unExpected.printStackTrace();
                 LOGGER.log(Level.FINEST, key + " unavailable", unExpected);
 
                 errors.put(featureTypeDTO, unExpected);
 
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
                     + " ignored - configuration problem:" + configException);
                 LOGGER.log(Level.FINEST, key + " unavailable", configException);
                 errors.put(featureTypeDTO, configException);
 
                 continue;
             }
 
             String key2 = prefix + ":" + typeName;
 
             if (map.containsKey(key2)) {
                 LOGGER.severe("FeatureTypeInfo '" + key2
                     + "' already defined - you must have duplicate defined?");
                 errors.put(featureTypeDTO,
                     new ConfigurationException("FeatureTypeInfo '" + key2
                         + "' already defined - you must have duplicate defined?"));
             } else {
                 LOGGER.finest("FeatureTypeInfo " + key2
                     + " has been created...");
                 map.put(key2, featureTypeInfo);
 
                 LOGGER.finest("FeatureTypeInfo '" + key2 + "' is registered:"
                     + dataStoreInfo);
                 errors.put(featureTypeDTO, Boolean.TRUE);
             }
         }
 
         return map;
     }
 
     private List createAttrDTOsFromSchema(FeatureType featureType) {
         List attrList = DataTransferObjectFactory.generateAttributes(featureType);
 
         /*   new ArrayList(featureType.getAttributeCount());
            for (int index = 0; index < featureType.getAttributeCount(); index++) {
                AttributeType attrib = featureType.getAttributeType(index);
                attrList.add(new AttributeTypeInfoDTO(attrib));
            }*/
         return attrList;
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
         stFiles = new HashMap();
 
         if ((dto == null) || (dto.getStyles() == null)) {
             throw new NullPointerException("List of styles is required");
         }
 
         for (Iterator i = dto.getStyles().values().iterator(); i.hasNext();) {
             StyleDTO styleDTO = (StyleDTO) i.next();
             String id = styleDTO.getId();
             Style style;
 
             try {
                 style = loadStyle(styleDTO.getFilename());
                 //HACK: due to our crappy weird id shit we rename the style's specified
                 //name with the id we (and our clients) refer to the style as.
                 //Should redo style management to not make use of our weird ids, just
                 //use the style's name out of the styles directory.
                 style.setName(id);
             } catch (Exception ioException) { // was IOException
                 LOGGER.log(Level.SEVERE, "Could not load style " + id,
                     ioException);
 
                 continue;
             }
 
             stFiles.put(id, styleDTO.getFilename());
             map.put(id, style);
         }
 
         LOGGER.finer("returning styles " + map + "\n and set map " + stFiles);
 
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
                 LOGGER.fine(key + ": ready");
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
 
         while (i.hasNext()) {
             Map.Entry e = (Map.Entry) i.next();
             FeatureTypeInfoDTO ftdto = (FeatureTypeInfoDTO) e.getKey();
             m.put(ftdto.getDataStoreId() + ":" + ftdto.getName(), e.getValue());
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
 
         while (i.hasNext()) {
             Map.Entry e = (Map.Entry) i.next();
             FeatureTypeInfoDTO ftdto = (FeatureTypeInfoDTO) e.getKey();
             DataStoreInfo dsdto = (DataStoreInfo) dataStores.get(ftdto
                     .getDataStoreId());
 
             if (dsdto != null) {
                 m.put(dsdto.getNamesSpacePrefix() + ":" + ftdto.getName(),
                     e.getValue());
             }
         }
 
         return m;
     }
 
     
 
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
     public Object toDTO() {
         DataDTO dto = new DataDTO();
 
         HashMap tmp;
         Iterator i;
         tmp = new HashMap();
         i = nameSpaces.keySet().iterator();
 
         while (i.hasNext()) {
             NameSpaceInfo nsi = (NameSpaceInfo) nameSpaces.get(i.next());
             tmp.put(nsi.getPrefix(), nsi.toDTO());
         }
 
         dto.setNameSpaces(tmp);
 
         if (defaultNameSpace != null) {
             dto.setDefaultNameSpacePrefix(defaultNameSpace.getPrefix());
         }
 
         tmp = new HashMap();
         i = styles.keySet().iterator();
 
         while (i.hasNext()) {
             String id = (String) i.next();
             Style st = (Style) styles.get(id);
             StyleDTO sdto = new StyleDTO();
             sdto.setDefault(st.isDefault());
             sdto.setFilename((File) stFiles.get(id));
             sdto.setId(id);
             tmp.put(id, sdto);
         }
 
         LOGGER.finer("setting styles to: " + tmp);
         dto.setStyles(tmp);
 
         tmp = new HashMap();
         i = dataStores.keySet().iterator();
 
         while (i.hasNext()) {
             DataStoreInfo dsi = (DataStoreInfo) dataStores.get(i.next());
             tmp.put(dsi.getId(), dsi.toDTO());
         }
 
         dto.setDataStores(tmp);
 
         tmp = new HashMap();
         i = errors.keySet().iterator();
 
         while (i.hasNext()) {
             FeatureTypeInfoDTO fti = (FeatureTypeInfoDTO) i.next();
            tmp.put(fti.getKey(), fti.clone());   //DJB:  changed to getKey() from getName() which was NOT unique!
         }
 
         dto.setFeaturesTypes(tmp);
 
         return dto;
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
         DataStoreInfo dsi = (DataStoreInfo) dataStores.get(id);
 
         if ((dsi != null) && dsi.isEnabled()) {
             return dsi;
         }
 
         return null;
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
      * Locate FeatureTypeInfo by name
      * 
      * <p>
      * The following searchness is used::
      * 
      * <ul>
      * <li>
      * search prefix:typeName for direct match with name
      * </li>
      * <li>
      * search prefix:typeName for match with defaultnamespaceprefix:name
      * </li>
      * <li>
      * linear search of typeName for direct match
      * </li>
      * </ul>
      * </p>
      * 
      * <p>
      * Yes this is the magic method used by TransasctionResponse. If you
      * wondered what it was doing - this is it.
      * </p>
      *
      * @param name String The FeatureTypeInfo Name
      *
      * @return FeatureTypeInfo
      *
      * @throws NoSuchElementException
      */
     public FeatureTypeInfo getFeatureTypeInfo(String name)
         throws NoSuchElementException {
         LOGGER.fine("getting type " + name);
 
         FeatureTypeInfo found = null;
 
         found = (FeatureTypeInfo) featureTypes.get(name);
 
         if (found != null) {
             return found;
         }
 
         String defaultPrefix = defaultNameSpace.getPrefix();
         found = (FeatureTypeInfo) featureTypes.get(defaultPrefix + ":" + name);
 
         if (found != null) {
             return found;
         }
 
         for (Iterator i = featureTypes.values().iterator(); i.hasNext();) {
             FeatureTypeInfo fto = (FeatureTypeInfo) i.next();
 
             if ((name != null) && name.equals(fto.getName())) {
                 found = fto;
             }
         }
 
         if (found != null) {
             return found;
         }
 
         throw new NoSuchElementException("Could not locate FeatureTypeConfig '"
             + name + "'");
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
      * @param typename Name NameSpaceInfo name
      * @param uri NameSpaceInfo uri
      *
      * @return FeatureTypeInfo
      */
     public FeatureTypeInfo getFeatureTypeInfo(String typename, String uri) {
         //System.out.println("Finding TypeName = "+typename+" URI = "+uri);
         for (Iterator it = featureTypes.values().iterator(); it.hasNext();) {
             FeatureTypeInfo fType = (FeatureTypeInfo) it.next();
 
             if (fType.isEnabled()) {
                 String typeId = fType.getNameSpace().getPrefix() + ":"
                     + typename;
                 boolean t1 = fType.getName().equals(typeId);
                 boolean t2 = fType.getNameSpace().getUri().equals(uri);
 
                 //System.out.println("Type id = "+typeId+" real name = "+fType.getName()+" T1="+t1+" T2="+t2);
                 //if (t1 && t2) {
                 /**
                  * GR:
                  * @HACK it seems not to be working, so I'm just comparing
                  * the prefixed name (don't should it be enough?)
                  */
                 if (t1) {
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
         return loadStyle(new File(base + fileName));
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
         SLDParser stylereader = new SLDParser(styleFactory, fileName);
 
         return stylereader.readXML()[0];
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
      * The number of connections currently held.
      * 
      * <p>
      * We will need to modify DataStore to provide access to the current count
      * of its connection pool (if appropriate). Right now we are asumming a
      * one DataStore equals One "Connection".
      * </p>
      * 
      * <p>
      * This is a good compromize since I just want to indicate the amount of
      * resources currently tied up by GeoServer.
      * </p>
      *
      * @return Number of available connections.
      */
     public int getConnectionCount() {
         int count = 0;
 
         for (Iterator i = dataStores.values().iterator(); i.hasNext();) {
             DataStoreInfo meta = (DataStoreInfo) i.next();
 
             if (!meta.isEnabled()) {
                 continue; // disabled
             }
 
             DataStore dataStore;
 
             try {
                 dataStore = meta.getDataStore();
             } catch (Throwable notAvailable) {
                 continue; // not available
             }
 
             // TODO: All DataStore to indicate number of connections
             count += 1;
         }
 
         return count;
     }
 
     /**
      * Count locks currently held.
      * 
      * <p>
      * Not sure if this should be the number of features locked, or the number
      * of FeatureLocks in existence (a FeatureLock may lock several features.
      * </p>
      *
      * @return number of locks currently held
      */
     public int getLockCount() {
         int count = 0;
 
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
             } catch (Throwable huh) {
                 continue; // not even working
             }
 
             LockingManager lockingManager = dataStore.getLockingManager();
 
             if (lockingManager == null) {
                 continue; // locks not supported
             }
 
             // TODO: implement LockingManger.getLockSet()
             // count += lockingManager.getLockSet();             
         }
 
         return count;
     }
 
     /**
      * Release all feature locks currently held.
      * 
      * <p>
      * This is the implementation for the Admin "free lock" action, transaction
      * locks are not released.
      * </p>
      *
      * @return Number of locks released
      */
     public int lockReleaseAll() {
         int count = 0;
 
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
             } catch (Throwable huh) {
                 continue; // not even working
             }
 
             LockingManager lockingManager = dataStore.getLockingManager();
 
             if (lockingManager == null) {
                 continue; // locks not supported
             }
 
             // TODO: implement LockingManger.releaseAll()
             //count += lockingManager.releaseAll();            
         }
 
         return count;
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
     public NameSpaceInfo getNamespaceMetaData(String prefix) {
         return getNameSpace(prefix);
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
         if ((prefix == null) || (prefix == "")) {
             prefix = defaultNameSpace.getPrefix();
         }
 
         NameSpaceInfo namespace = getNamespaceMetaData(prefix);
         FeatureTypeInfo featureType = namespace.getFeatureTypeInfo(typeName);
         DataStoreInfo dataStore = featureType.getDataStoreMetaData();
 
         return dataStore.getDataStore().getFeatureSource(typeName);
     }
 
     /**
      * Returns the baseDir for use with relative paths.
      *
      * @return Returns the baseDir.
      */
     public File getBaseDir() {
         return baseDir;
     }
 }
