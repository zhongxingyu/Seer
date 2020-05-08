 /* Copyright (c) 2001 - 2007 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.global;
 
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
 import org.geotools.styling.StyleFactoryFinder;
 import org.vfny.geoserver.global.dto.AttributeTypeInfoDTO;
 import org.vfny.geoserver.global.dto.CoverageInfoDTO;
 import org.vfny.geoserver.global.dto.CoverageStoreInfoDTO;
 import org.vfny.geoserver.global.dto.DataDTO;
 import org.vfny.geoserver.global.dto.DataStoreInfoDTO;
 import org.vfny.geoserver.global.dto.DataTransferObjectFactory;
 import org.vfny.geoserver.global.dto.FeatureTypeInfoDTO;
 import org.vfny.geoserver.global.dto.NameSpaceInfoDTO;
 import org.vfny.geoserver.global.dto.StyleDTO;
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
 import javax.xml.namespace.QName;
 
 
 /**
  * This class stores all the information that a catalog would (and CatalogConfig
  * used to).
  * <p>
  * All public methods besides constructors and stuff used for dependency injection
  * setters is synchronized to avoid response failures during the Geoserver reconfiguration
  * process (basically, each time you apply a new configuration set on the user interface).
  * </p>
  * <p>
  * A quick benchar did not show significant scalability loss. If one is to be encountered,
  * a more complex Reader/Write synchronization will be used, based on the java 5 concurrency
  * classes or their backport for java 1.4
  * </p>
  *
  * @author Gabriel Roldan, Axios Engineering
  * @author Chris Holmes
  * @author dzwiers
  * @author $Author: Alessio Fabiani (alessio.fabiani@gmail.com) $ (last
  *         modification)
  * @author $Author: Simone Giannecchini (simboss1@gmail.com) $ (last
  *         modification)
  * @version $Id$
  */
 public class Data extends GlobalLayerSupertype /* implements Repository */ {
     public static final String WEB_CONTAINER_KEY = "DATA";
     public static final Integer TYPE_VECTOR = new Integer(0);
     public static final Integer TYPE_RASTER = new Integer(1);
 
     /** Default name of feature type information */
     private static final String INFO_FILE = "info.xml";
 
     /** used to create styles */
     private static StyleFactory styleFactory = StyleFactoryFinder.createStyleFactory();
 
     /** used to cache layer names and types **/
     private volatile Map layerNames = new HashMap();
 
     /** holds the mappings between prefixes and NameSpaceInfo objects */
     private Map nameSpaces;
 
     /** NameSpaceInfo */
     private NameSpaceInfo defaultNameSpace;
 
     /** Mapping of DataStoreInfo by dataStoreId */
     private Map dataStores;
 
     /**
      * Mapping of CoverageStoreInfo by formatId
      *
      * @uml.property name="formats"
      * @uml.associationEnd elementType="org.vfny.geoserver.global.dto.FeatureTypeInfoDTO"
      *                     qualifier="next:java.lang.String
      *                     org.vfny.geoserver.global.CoverageStoreInfo"
      *                     multiplicity= "(0 -1)" ordering="ordered"
      */
     private Map formats;
 
     /**
      * holds the mapping of Styles and style names
      *
      * @uml.property name="styles"
      * @uml.associationEnd elementType="org.vfny.geoserver.global.dto.FeatureTypeInfoDTO"
      *                     qualifier="id:java.lang.String
      *                     org.geotools.styling.Style" multiplicity="(0 -1) "
      *                     ordering="ordered"
      */
     private Map styles;
 
     /**
      *
      * @uml.property name="stFiles"
      * @uml.associationEnd qualifier="id:java.lang.String java.io.File"
      *                     multiplicity="(0 1)"
      */
     private Map stFiles;
 
     /**
      * Map of <code>FeatureTypeInfo</code>'s stored by full qualified name
      * (NameSpaceInfo prefix + PREFIX_DELIMITER + typeName)
      *
      * @uml.property name="featureTypes"
      * @uml.associationEnd inverse="data:org.vfny.geoserver.global.FeatureTypeInfo"
      *                     qualifier= "name:java.lang.String
      *                     org.vfny.geoserver.global.FeatureTypeInfo"
      *                     multiplicity= "(0 1)"
      */
     private Map featureTypes;
 
     /**
      *
      * @uml.property name="coverages"
      * @uml.associationEnd inverse="data:org.vfny.geoserver.global.CoverageInfo"
      *                     qualifier= "next:java.lang.String
      *                     org.vfny.geoserver.global.CoverageInfo"
      *                     multiplicity="(0 1)"
      */
     private Map coverages;
 
     /**
      * Base directory for use with file based relative paths
      *
      * @uml.property name="baseDir" multiplicity="(0 1)"
      */
     private File baseDir;
 
     /**
      * Data constructor.
      *
      * <p>
      * Creates a Data object from the data provided.
      * </p>
      *
      * @uml.property name="gs"
      * @uml.associationEnd multiplicity="(1 1)"
      */
     private GeoServer gs;
 
     /**
      * map of all featureTypeDTO -> load status (Boolean.True, Boolean.False,
      * Exception) Boolean.True when feature was loaded. Boolean.False when
      * something was disabled. Exception the error.
      *
      * @uml.property name="errors"
      * @uml.associationEnd qualifier="featureTypeDTO:org.vfny.geoserver.global.dto.FeatureTypeInfoDTO
      *                     org.vfny.geoserver.global.ConfigurationException"
      *                     multiplicity="(0 1)"
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
 
     public Data(Config config, GeoServer g) throws ConfigurationException {
         this(config.getXMLReader().getData(), config.dataDirectory(), g);
     }
 
     GeoServer getGeoServer() {
         return gs;
     }
 
     public void setDataDirectory(File dataDirectory) {
         this.baseDir = dataDirectory;
     }
 
     public File getDataDirectory() {
         return baseDir;
     }
 
     /*
      * Places the data in this container and innitializes it. Complex tests are
      * performed to detect existing datasources, while the remainder only
      * include simplistic id checks.
      *
      * @param config
      *
      * @throws NullPointerException
      */
     public synchronized void load(DataDTO config) {
         if (config == null) {
             throw new NullPointerException("Non null DataDTO required for load");
         }
 
         // Step 1: load formats, dataStores and Namespaces
         formats = loadFormats(config);
         dataStores = loadDataStores(config);
         nameSpaces = loadNamespaces(config);
         defaultNameSpace = (NameSpaceInfo) nameSpaces.get(config.getDefaultNameSpacePrefix());
 
         // Step 2: set up styles
         styles = loadStyles(config);
 
         // Step 3: load featureTypes
         layerNames.clear();
         featureTypes = loadFeatureTypes(config);
         coverages = loadCoverages(config);
     }
 
     public synchronized Set getDataStores() {
         return new HashSet(dataStores.values());
     }
 
     public synchronized Set getFormats() {
         return new HashSet(formats.values());
     }
 
     private final Map loadFormats(DataDTO dto) {
         if ((dto == null) || (dto.getFormats() == null)) {
             return Collections.EMPTY_MAP; // we *are* allowed no datasets
         }
 
         Map map = new HashMap();
 
         for (Iterator i = dto.getFormats().values().iterator(); i.hasNext();) {
             CoverageStoreInfoDTO formatDTO = (CoverageStoreInfoDTO) i.next();
             String id = formatDTO.getId();
 
             CoverageStoreInfo formatInfo = new CoverageStoreInfo(formatDTO, this);
             map.put(id, formatInfo);
 
             if (formatDTO.isEnabled()) {
                 if (LOGGER.isLoggable(Level.FINE)) {
                     LOGGER.fine(new StringBuffer("Register Format '").append(id).append("'")
                                                                      .toString());
                 }
             } else {
                 if (LOGGER.isLoggable(Level.FINE)) {
                     LOGGER.fine(new StringBuffer("Did not Register Format '").append(id)
                                                                              .append("' as it was not enabled")
                                                                              .toString());
                 }
             }
         }
 
         return map;
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
      * @throws NullPointerException
      *             DOCUMENT ME!
      */
     private final Map loadDataStores(DataDTO dto) {
         if ((dto == null) || (dto.getDataStores() == null)) {
             return Collections.EMPTY_MAP; // we *are* allowed no datasets
         }
 
         Map map = new HashMap(dto.getDataStores().size());
         DataStoreInfoDTO dataStoreDTO;
         String id;
         DataStoreInfo dataStoreInfo;
 
         for (Iterator i = dto.getDataStores().values().iterator(); i.hasNext();) {
             dataStoreDTO = (DataStoreInfoDTO) i.next();
             id = dataStoreDTO.getId();
             dataStoreInfo = new DataStoreInfo(dataStoreDTO, this);
             map.put(id, dataStoreInfo);
 
             if (dataStoreDTO.isEnabled()) {
                 if (LOGGER.isLoggable(Level.FINE)) {
                     LOGGER.fine(new StringBuffer("Register DataStore '").append(id).append("'")
                                                                         .toString());
                 }
             } else {
                 if (LOGGER.isLoggable(Level.FINE)) {
                     LOGGER.fine(new StringBuffer("Did not Register DataStore '").append(id)
                                                                                 .append("' as it was not enabled")
                                                                                 .toString());
                 }
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
      * @throws NullPointerException
      *             DOCUMENT ME!
      */
     private final Map loadNamespaces(DataDTO dto) {
         if ((dto == null) || (dto.getNameSpaces() == null)) {
             return Collections.EMPTY_MAP; // we *are* allowed no datasets
         }
 
         Map map = new HashMap(dto.getNameSpaces().size());
         NameSpaceInfoDTO namespaceDto;
         String prefix;
         NameSpaceInfo namespaceInfo;
 
         for (Iterator i = dto.getNameSpaces().values().iterator(); i.hasNext();) {
             namespaceDto = (NameSpaceInfoDTO) i.next();
             prefix = namespaceDto.getPrefix();
             namespaceInfo = new NameSpaceInfo(this, namespaceDto);
             map.put(prefix, namespaceInfo);
         }
 
         return map;
     }
 
     private final Map loadCoverages(DataDTO dto) {
         if ((dto == null) || (dto.getCoverages() == null)) {
             return Collections.EMPTY_MAP; // we *are* allowed no datasets
         }
 
         Map map = new HashMap(dto.getCoverages().size());
         CoverageInfoDTO coverageDTO;
         String id;
         CoverageInfo coverageInfo;
 
         for (Iterator i = dto.getCoverages().values().iterator(); i.hasNext();) {
             coverageDTO = (CoverageInfoDTO) i.next();
             id = coverageDTO.getName();
 
             try {
                 coverageInfo = new CoverageInfo(coverageDTO, this);
             } catch (ConfigurationException e) {
                 coverageInfo = null;
             }
 
             map.put(id, coverageInfo);
             // set layer name, type raster (1)
             layerNames.put(id, TYPE_RASTER);
 
             if ((dto.getFormats() != null)
                     && (dto.getFormats().get(coverageDTO.getFormatId()) != null)) {
                 if (LOGGER.isLoggable(Level.FINE)) {
                     LOGGER.fine(new StringBuffer("Register Coverage '").append(id).append("'")
                                                                        .toString());
                 }
             } else {
                 if (LOGGER.isLoggable(Level.FINE)) {
                     LOGGER.fine(new StringBuffer("Did not Register Coverage '").append(id)
                                                                                .append("' as didn't exist a valid Format")
                                                                                .toString());
                 }
             }
         }
 
         return map;
     }
 
     /**
      * Configure a map of FeatureTypeInfo by prefix:typeName.
      *
      * <p>
      * Note that this map uses namespace prefix (not datastore ID like the the
      * configuration system). That is because this is the actual runtime, in
      * which we access FeatureTypes by namespace. The configuration system uses
      * dataStoreId which is assumed to be more stable across changes (one can
      * reassing a FeatureType to a different namespace, but not a different
      * dataStore).
      * </p>
      *
      * <p>
      * Note loadDataStores() and loadNamespaces() must be called prior to using
      * this function!
      * </p>
      *
      * @param dto
      *            configDTO
      *
      * @return
      *
      * @throws NullPointerException
      *             DOCUMENT ME!
      */
     private final Map loadFeatureTypes(DataDTO dto) {
         errors = new HashMap();
         featureTypes = new HashMap(); // to fix lazy loading
 
         if ((dto == null) || (dto.getFeaturesTypes() == null)) {
             errors = null;
 
             return Collections.EMPTY_MAP; // we *are* allowed no datasets
         }
 
         Map map = new HashMap(dto.getFeaturesTypes().size());
         FeatureTypeInfoDTO featureTypeDTO;
         String key;
         DataStoreInfo dataStoreInfo;
         String dataStoreId;
         String typeName;
         Style s;
 SCHEMA: 
         for (Iterator i = dto.getFeaturesTypes().values().iterator(); i.hasNext();) {
             featureTypeDTO = (FeatureTypeInfoDTO) i.next();
 
             if (featureTypeDTO == null) {
                 if (LOGGER.isLoggable(Level.WARNING)) {
                     LOGGER.warning(new StringBuffer("Ignore null FeatureTypeInfo DTO!").toString());
                 }
 
                 continue;
             }
 
             key = featureTypeDTO.getKey(); // dataStoreId:typeName
 
             if (LOGGER.isLoggable(Level.FINER)) {
                 LOGGER.finer(new StringBuffer("FeatureType ").append(key)
                                                              .append(": loading feature type info dto:")
                                                              .append(featureTypeDTO).toString());
             }
 
             dataStoreId = featureTypeDTO.getDataStoreId();
 
             if (LOGGER.isLoggable(Level.FINEST)) {
                 LOGGER.finest(new StringBuffer("FeatureType ").append(key).append(" looking up :")
                                                               .append(dataStoreId).toString());
             }
 
             dataStoreInfo = (DataStoreInfo) dataStores.get(dataStoreId);
 
             if (dataStoreInfo == null) {
                 if (LOGGER.isLoggable(Level.SEVERE)) {
                     LOGGER.severe(new StringBuffer("FeatureTypeInfo ").append(key)
                                                                       .append(" could not be used - DataStore ")
                                                                       .append(dataStoreId)
                                                                       .append(" is not defined!")
                                                                       .toString());
                 }
 
                 DataStoreInfoDTO tmp = (DataStoreInfoDTO) dto.getDataStores().get(dataStoreId);
 
                 if ((tmp != null) && (!tmp.isEnabled())) {
                     errors.put(featureTypeDTO, Boolean.FALSE);
                 } else {
                     errors.put(featureTypeDTO,
                         new ConfigurationException("FeatureTypeInfo " + key
                             + " could not be used - DataStore " + dataStoreId + " is not defined!"));
                 }
 
                 continue;
             } else {
                 if (LOGGER.isLoggable(Level.FINEST)) {
                     LOGGER.finest(new StringBuffer(key).append(" datastore found :")
                                                        .append(dataStoreInfo).toString());
                 }
             }
 
             s = getStyle(featureTypeDTO.getDefaultStyle());
 
             if (s == null) {
                 if (LOGGER.isLoggable(Level.SEVERE)) {
                     LOGGER.severe(new StringBuffer("FeatureTypeInfo ").append(key)
                                                                       .append(" ignored - Style '")
                                                                       .append(featureTypeDTO
                             .getDefaultStyle()).append("' not found!").toString());
                 }
 
                 errors.put(featureTypeDTO,
                     new ConfigurationException("FeatureTypeInfo " + key + " ignored - Style '"
                         + featureTypeDTO.getDefaultStyle() + "' not found!"));
 
                 continue SCHEMA;
             }
 
             // Check attributes configured correctly against schema
             typeName = featureTypeDTO.getName();
 
             try {
                 DataStore dataStore = dataStoreInfo.getDataStore();
                 FeatureType featureType = dataStore.getSchema(typeName);
 
                 Set attributeNames = new HashSet();
                 Set ATTRIBUTENames = new HashSet();
 
                 // as far as I can tell an emtpy list indicates that no
                 // schema.xml file was found. I may be approaching this
                 // all wrong, is this logic contained elsewhere?
                 // CH: Yeah, this shit was super messed up. It was causing null
                 // pointer
                 // exceptions, and then it created this createAttrDTO flag that
                 // wasn't
                 // then used by anyone. So I fixed the null and made it so it
                 // creates
                 // AttributeTypeInfoDTO's (once again, I hate these) from the
                 // FeatureType
                 // of the real datastore.
                 // boolean createAttrDTO =
                 // (featureTypeDTO.getSchemaAttributes().size() == 0);
                 if (LOGGER.isLoggable(Level.FINE)) {
                     LOGGER.fine(new StringBuffer("locading datastore ").append(typeName).toString());
                 }
 
                 boolean createAttrDTO;
 
                 if (featureTypeDTO.getSchemaAttributes() == null) {
                     createAttrDTO = true;
                 } else {
                     createAttrDTO = featureTypeDTO.getSchemaAttributes().size() == 0;
                 }
 
                 if (createAttrDTO) {
                     List attributeDTOs = createAttrDTOsFromSchema(featureType);
                     featureTypeDTO.setSchemaAttributes(attributeDTOs);
 
                     if (LOGGER.isLoggable(Level.FINER)) {
                         LOGGER.finer(new StringBuffer(
                                 "No schema found, setting featureTypeDTO with ").append(
                                 attributeDTOs).toString());
                     }
                 } else {
                     for (int index = 0; index < featureType.getAttributeCount(); index++) {
                         AttributeType attrib = featureType.getAttributeType(index);
                         attributeNames.add(attrib.getName());
                         ATTRIBUTENames.add(attrib.getName().toUpperCase());
                     }
 
                     if (featureTypeDTO.getSchemaAttributes() != null) {
                         for (Iterator a = featureTypeDTO.getSchemaAttributes().iterator();
                                 a.hasNext();) {
                             AttributeTypeInfoDTO attribDTO = (AttributeTypeInfoDTO) a.next();
                             String attributeName = attribDTO.getName();
 
                             if (!attributeNames.contains(attributeName)) {
                                 if (ATTRIBUTENames.contains(attributeName.toUpperCase())) {
                                     if (LOGGER.isLoggable(Level.SEVERE)) {
                                         LOGGER.severe(new StringBuffer("FeatureTypeInfo ").append(
                                                 key).append(" ignored - attribute '")
                                                                                           .append(attributeName)
                                                                                           .append("' not found - please check captialization")
                                                                                           .toString());
                                     }
                                 } else {
                                     if (LOGGER.isLoggable(Level.SEVERE)) {
                                         LOGGER.severe(new StringBuffer("FeatureTypeInfo ").append(
                                                 key).append(" ignored - attribute '")
                                                                                           .append(attributeName)
                                                                                           .append("' not found!")
                                                                                           .toString());
                                     }
 
                                     String names = "";
                                     Iterator x = attributeNames.iterator();
 
                                     if (x.hasNext()) {
                                         names = x.next().toString();
                                     }
 
                                     while (x.hasNext())
                                         names = ":::" + x.next().toString();
 
                                     if (LOGGER.isLoggable(Level.SEVERE)) {
                                         LOGGER.severe(new StringBuffer(
                                                 "Valid attribute names include :::").append(names)
                                                                                                            .toString());
                                     }
                                 }
 
                                 errors.put(featureTypeDTO,
                                     new ConfigurationException(
                                         new StringBuffer("FeatureTypeInfo ").append(key)
                                                                             .append(" could not be used - DataStore ")
                                                                             .append(dataStoreId)
                                                                             .append(" is not defined!")
                                                                             .toString()));
 
                                 continue SCHEMA;
                             }
                         }
                     }
                 }
             } catch (IllegalStateException illegalState) {
                 if (LOGGER.isLoggable(Level.SEVERE)) {
                     LOGGER.severe(new StringBuffer("FeatureTypeInfo ").append(key)
                                                                       .append(" ignored - as DataStore ")
                                                                       .append(dataStoreId)
                                                                       .append(" is disabled!")
                                                                       .toString());
                 }
 
                 errors.put(featureTypeDTO, Boolean.FALSE);
 
                 continue;
             } catch (IOException ioException) {
                 if (LOGGER.isLoggable(Level.SEVERE)) {
                     LOGGER.log(Level.SEVERE,
                         new StringBuffer("FeatureTypeInfo ").append(key)
                                                             .append(" ignored - as DataStore ")
                                                             .append(dataStoreId)
                                                             .append(" is unavailable:")
                                                             .append(ioException).toString());
                 }
 
                 if (LOGGER.isLoggable(Level.FINE)) {
                     LOGGER.log(Level.FINE, new StringBuffer(key).append(" unavailable").toString(),
                         ioException);
                 }
 
                 errors.put(featureTypeDTO, ioException);
 
                 continue;
             } catch (NoSuchElementException nse) {
                 if (LOGGER.isLoggable(Level.SEVERE)) {
                     LOGGER.log(Level.SEVERE,
                         new StringBuffer("FeatureTypeInfo ").append(key)
                                                             .append(" ignored - as DataStore ")
                                                             .append(dataStoreId)
                                                             .append(" can't find FeatureType '"
                             + typeName + "'.  Error was:\n").append(nse).toString());
                 }
 
                 if (LOGGER.isLoggable(Level.FINE)) {
                     LOGGER.log(Level.FINE, typeName + " not found", nse);
                 }
 
                 continue;
             } catch (Throwable unExpected) {
                 if (LOGGER.isLoggable(Level.SEVERE)) {
                     LOGGER.log(Level.SEVERE,
                         new StringBuffer("FeatureTypeInfo ").append(key)
                                                             .append(" ignored - as DataStore ")
                                                             .append(dataStoreId)
                                                             .append(" is broken:").append(unExpected)
                                                             .toString());
                 }
 
                 if (LOGGER.isLoggable(Level.FINE)) {
                     LOGGER.log(Level.FINE, new StringBuffer(key).append(" unavailable").toString(),
                         unExpected);
                 }
 
                 errors.put(featureTypeDTO, unExpected);
 
                 continue;
             }
 
             String prefix = dataStoreInfo.getNamesSpacePrefix();
 
             if (LOGGER.isLoggable(Level.FINEST)) {
                 LOGGER.finest(new StringBuffer("FeatureType ").append(key)
                                                               .append(" creating FeatureTypeInfo for ")
                                                               .append(prefix).append(":")
                                                               .append(typeName).toString());
             }
 
             FeatureTypeInfo featureTypeInfo = null;
 
             try {
                 featureTypeInfo = new FeatureTypeInfo(featureTypeDTO, this);
             } catch (ConfigurationException configException) {
                 if (LOGGER.isLoggable(Level.SEVERE)) {
                     LOGGER.log(Level.SEVERE,
                         new StringBuffer("FeatureTypeInfo ").append(key)
                                                             .append(" ignored - configuration problem:")
                                                             .append(configException).toString());
                 }
 
                 if (LOGGER.isLoggable(Level.FINEST)) {
                     LOGGER.log(Level.FINEST,
                         new StringBuffer(key).append(" unavailable").toString(), configException);
                 }
 
                 errors.put(featureTypeDTO, configException);
 
                 continue;
             }
 
             String key2 = prefix + ":" + typeName;
 
             if (map.containsKey(key2)) {
                 if (LOGGER.isLoggable(Level.SEVERE)) {
                     LOGGER.severe(new StringBuffer("FeatureTypeInfo '").append(key2)
                                                                        .append("' already defined - you must have duplicate defined?")
                                                                        .toString());
                 }
 
                 errors.put(featureTypeDTO,
                     new ConfigurationException("FeatureTypeInfo '" + key2
                         + "' already defined - you must have duplicate defined?"));
             } else {
                 if (LOGGER.isLoggable(Level.FINEST)) {
                     LOGGER.finest(new StringBuffer("FeatureTypeInfo ").append(key2)
                                                                       .append(" has been created...")
                                                                       .toString());
                 }
 
                 map.put(key2, featureTypeInfo);
                 // set layer name, type vector (0)
                 layerNames.put(key2, TYPE_VECTOR);
 
                 if (LOGGER.isLoggable(Level.FINEST)) {
                     LOGGER.finest(new StringBuffer("FeatureTypeInfo '").append(key2)
                                                                        .append("' is registered:")
                                                                        .append(dataStoreInfo)
                                                                        .toString());
                 }
 
                 errors.put(featureTypeDTO, Boolean.TRUE);
             }
         }
 
         return map;
     }
 
     private List createAttrDTOsFromSchema(FeatureType featureType) {
         List attrList = DataTransferObjectFactory.generateAttributes(featureType);
 
         /*
          * new ArrayList(featureType.getAttributeCount()); for (int index = 0;
          * index < featureType.getAttributeCount(); index++) { AttributeType
          * attrib = featureType.getAttributeType(index); attrList.add(new
          * AttributeTypeInfoDTO(attrib)); }
          */
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
      * @param dto
      *            requested configuration
      *
      * @return Map of Style by id
      *
      * @throws NullPointerException
      *             If the style could not be loaded from the filename
      *
      * @see Data.loadStyle() for more information
      */
     private final Map loadStyles(DataDTO dto) {
         Map map = new HashMap();
         stFiles = new HashMap();
 
         if ((dto == null) || (dto.getStyles() == null)) {
             return Collections.EMPTY_MAP; // we *are* allowed no datasets
         }
 
         for (Iterator i = dto.getStyles().values().iterator(); i.hasNext();) {
             StyleDTO styleDTO = (StyleDTO) i.next();
             String id = styleDTO.getId();
             Style style;
 
             try {
                 style = loadStyle(styleDTO.getFilename());
                 // HACK: due to our crappy weird id shit we rename the style's
                 // specified
                 // name with the id we (and our clients) refer to the style as.
                 // Should redo style management to not make use of our weird
                 // ids, just
                 // use the style's name out of the styles directory.
                 style.setName(id);
             } catch (Exception ioException) { // was IOException
 
                 if (LOGGER.isLoggable(Level.SEVERE)) {
                     LOGGER.log(Level.SEVERE,
                         new StringBuffer("Could not load style ").append(id).toString(), ioException);
                 }
 
                 continue;
             }
 
             stFiles.put(id, styleDTO.getFilename());
             map.put(id, style);
         }
 
         if (LOGGER.isLoggable(Level.FINER)) {
             LOGGER.finer(new StringBuffer("returning styles ").append(map).append("\n and set map ")
                                                               .append(stFiles).toString());
         }
 
         return map;
     }
 
     /**
      * Status output
      *
      * @param title
      *            DOCUMENT ME!
      * @param status
      *            DOCUMENT ME!
      */
     static final void outputStatus(String title, Map status) {
         if (LOGGER.isLoggable(Level.INFO)) {
             LOGGER.info(title);
         }
 
         for (Iterator i = status.entrySet().iterator(); i.hasNext();) {
             Map.Entry entry = (Map.Entry) i.next();
             String key = (String) entry.getKey();
             Object value = entry.getValue();
 
             if (value == Boolean.TRUE) {
                 if (LOGGER.isLoggable(Level.FINE)) {
                     LOGGER.fine(new StringBuffer(key).append(": ready").toString());
                 }
             } else if (value instanceof Throwable) {
                 Throwable t = (Throwable) value;
 
                 if (LOGGER.isLoggable(Level.SEVERE)) {
                     LOGGER.log(Level.SEVERE, new StringBuffer(key).append(" not ready").toString(),
                         t);
                 }
             } else {
                 if (LOGGER.isLoggable(Level.WARNING)) {
                     LOGGER.warning(new StringBuffer(key).append(": '").append(value).append("'")
                                                         .toString());
                 }
             }
         }
     }
 
     /**
      * Dynamically tries to connect to every DataStore!
      *
      * <p>
      * Returns a map of Exception by dataStoreId:typeName. If by some marvel the
      * we could connect to a FeatureSource we will record Boolean.TRUE.
      * </p>
      *
      * @return Map of Exception by dataStoreId:typeName
      */
     public synchronized Map statusDataStores() {
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
     public synchronized Map statusNamespaces() {
         Map m = new HashMap();
         Iterator i = errors.entrySet().iterator();
 
         while (i.hasNext()) {
             Map.Entry e = (Map.Entry) i.next();
             FeatureTypeInfoDTO ftdto = (FeatureTypeInfoDTO) e.getKey();
             DataStoreInfo dsdto = (DataStoreInfo) dataStores.get(ftdto.getDataStoreId());
 
             if (dsdto != null) {
                 m.put(dsdto.getNamesSpacePrefix() + ":" + ftdto.getName(), e.getValue());
             }
         }
 
         return m;
     }
 
     /**
      * toDTO purpose.
      *
      * <p>
      * This method is package visible only, and returns a reference to the
      * GeoServerDTO. This method is unsafe, and should only be used with extreme
      * caution.
      * </p>
      *
      * @return DataDTO the generated object
      */
     public synchronized Object toDTO() {
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
 
         if (LOGGER.isLoggable(Level.FINER)) {
             LOGGER.finer(new StringBuffer("setting styles to: ").append(tmp).toString());
         }
 
         dto.setStyles(tmp);
 
         tmp = new HashMap();
         i = formats.keySet().iterator();
 
         while (i.hasNext()) {
             CoverageStoreInfo fmi = (CoverageStoreInfo) formats.get(i.next());
             tmp.put(fmi.getId(), fmi.toDTO());
         }
 
         dto.setFormats(tmp);
 
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
             tmp.put(fti.getKey(), fti.clone()); // DJB: changed to getKey() from
                                                 // getName() which was NOT
                                                 // unique!
         }
 
         dto.setFeaturesTypes(tmp);
 
         tmp = new HashMap();
         i = coverages.keySet().iterator();
 
         while (i.hasNext()) {
             CoverageInfo cvi = (CoverageInfo) coverages.get(i.next());
             tmp.put(cvi.getName(), cvi.toDTO());
         }
 
         dto.setCoverages(tmp);
 
         return dto;
     }
 
     /**
      * Locate a DataStoreInfo by its id attribute.
      *
      * @param id
      *            the DataStoreInfo id looked for
      *
      * @return the DataStoreInfo with id attribute equals to <code>id</code>
      *         or null if there no exists
      */
     public synchronized DataStoreInfo getDataStoreInfo(String id) {
         DataStoreInfo dsi = (DataStoreInfo) dataStores.get(id);
 
         if ((dsi != null) && dsi.isEnabled()) {
             return dsi;
         }
 
         return null;
     }
 
     /**
      * Locate a CoverageStoreInfo by its id attribute.
      *
      * @param id
      *            the CoverageStoreInfo id looked for
      *
      * @return the CoverageStoreInfo with id attribute equals to <code>id</code>
      *         or null if there no exists
      */
     public synchronized CoverageStoreInfo getFormatInfo(String id) {
         CoverageStoreInfo dfi = (CoverageStoreInfo) formats.get(id);
 
         if ((dfi != null) && dfi.isEnabled()) {
             return dfi;
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
     public synchronized NameSpaceInfo[] getNameSpaces() {
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
     public synchronized NameSpaceInfo getNameSpace(String prefix) {
         NameSpaceInfo retNS = (NameSpaceInfo) nameSpaces.get(prefix);
 
         return retNS;
     }
 
     /**
      * Returns the NamespaceINfo object corresponding to a particular
      * namespace uri.
      * <p>
      * If a namespace info object could not be found with mathces <param>uri</param>
      * then <code>null</code> is returned.
      * </p>
      *
      * @param uri A namespace uri, non-null
      *
      * @return NameSpaceInfo resulting from the specified uri.
      */
     public synchronized NameSpaceInfo getNameSpaceFromURI(String uri) {
         for (Iterator i = nameSpaces.values().iterator(); i.hasNext();) {
             NameSpaceInfo nsInfo = (NameSpaceInfo) i.next();
 
             if (nsInfo.getURI().equals(uri)) {
                 return nsInfo;
             }
         }
 
         return null;
     }
 
     /**
      * getDefaultNameSpace purpose.
      *
      * <p>
      * Returns the default NameSpaceInfo for this Data object.
      * </p>
      *
      * @return NameSpaceInfo the default name space
      *
      * @uml.property name="defaultNameSpace"
      */
     public synchronized NameSpaceInfo getDefaultNameSpace() {
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
      *
      * @uml.property name="styles"
      */
     public synchronized Map getStyles() {
         return this.styles;
     }
 
     public synchronized Style getStyle(String id) {
         return (Style) styles.get(id);
     }
 
     /**
      * Locate FeatureTypeInfo by name
      *
      * <p>
      * The following searchness is used::
      *
      * <ul>
      * <li> search prefix:typeName for direct match with name </li>
      * <li> search prefix:typeName for match with defaultnamespaceprefix:name
      * </li>
      * <li> linear search of typeName for direct match </li>
      * </ul>
      * </p>
      *
      * <p>
      * Yes this is the magic method used by TransasctionResponse. If you
      * wondered what it was doing - this is it.
      * </p>
      *
      * @param name
      *            String The FeatureTypeInfo Name
      *
      * @return FeatureTypeInfo
      *
      * @throws NoSuchElementException
      */
     public synchronized FeatureTypeInfo getFeatureTypeInfo(String name)
         throws NoSuchElementException {
         if (LOGGER.isLoggable(Level.FINE)) {
             LOGGER.fine(new StringBuffer("getting type ").append(name).toString());
         }
 
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
 
         throw new NoSuchElementException("Could not locate FeatureTypeConfig '" + name + "'");
     }
 
     /**
      * Gets a FeatureTypeINfo from a qualified name.
      * <p>
      * This method calls through to {@link #getFeatureTypeInfo(String, String)}.
      * </p>
      * @param name The qualified name of the feature type.
      */
     public FeatureTypeInfo getFeatureTypeInfo(QName name) {
         return getFeatureTypeInfo(name.getLocalPart(), name.getNamespaceURI());
     }
 
     /**
      * Gets a FeatureTypeInfo from a local type name (ie unprefixed), and a uri.
      *
      * <p>
      * This method is slow, use getFeatureType(String typeName), where possible.
      * For not he only user should be TransactionFeatureHandler.
      * </p>
      *
      * <p>
      * TODO: Jody here - David is this still correct?
      * </p>
      *
      * @param typename
      *            Name NameSpaceInfo name
      * @param uri
      *            NameSpaceInfo uri
      *
      * @return FeatureTypeInfo
      */
     public synchronized FeatureTypeInfo getFeatureTypeInfo(String typename, String uri) {
        // System.out.println("Finding TypeName = "+typename+" URI = "+uri);
         for (Iterator it = featureTypes.values().iterator(); it.hasNext();) {
             FeatureTypeInfo fType = (FeatureTypeInfo) it.next();
 
             if (fType.isEnabled()) {
                 String typeId = fType.getNameSpace().getPrefix() + ":" + typename;
                 boolean t1 = fType.getName().equals(typeId);
                 boolean t2 = fType.getNameSpace().getUri().equals(uri);
 
                // System.out.println("Type id = "+typeId+" real name =
                // "+fType.getName()+" T1="+t1+" T2="+t2);
                // if (t1 && t2) {
                 /**
                  * GR:
                  *
                  * @HACK it seems not to be working, so I'm just comparing the
                  *       prefixed name (don't should it be enough?)
                  */
                if (t1) {
                     return fType;
                 }
             }
         }
 
        return null;
     }
 
     public synchronized CoverageInfo getCoverageInfo(String name)
         throws NoSuchElementException {
         LOGGER.fine("getting coverage " + name);
 
         CoverageInfo found = null;
 
         found = (CoverageInfo) coverages.get(name);
 
         if (found != null) {
             return found;
         }
 
         String defaultPrefix = defaultNameSpace.getPrefix();
         found = (CoverageInfo) coverages.get(defaultPrefix + ":" + name);
 
         if (found != null) {
             return found;
         }
 
         for (Iterator i = coverages.values().iterator(); i.hasNext();) {
             CoverageInfo cvo = (CoverageInfo) i.next();
 
             if ((name != null) && name.equals(cvo.getName())) {
                 found = cvo;
             }
         }
 
         if (found != null) {
             return found;
         }
 
         throw new NoSuchElementException("Could not locate CoverageConfig '" + name + "'");
     }
 
     public synchronized CoverageInfo getCoverageInfo(String name, String uri) {
         for (Iterator it = coverages.values().iterator(); it.hasNext();) {
             CoverageInfo cvo = (CoverageInfo) it.next();
 
             if (cvo.isEnabled()) {
                 String cvId = cvo.getNameSpace().getPrefix() + ":" + name;
                 boolean t1 = cvo.getName().equals(cvId);
                 boolean t2 = cvo.getNameSpace().getUri().equals(uri);
 
                 if (t1) {
                     return cvo;
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
     public synchronized Map getFeatureTypeInfos() {
         return Collections.unmodifiableMap(featureTypes);
     }
 
     public synchronized Map getCoverageInfos() {
         return Collections.unmodifiableMap(coverages);
     }
 
     // TODO: detect if a user put a full url, instead of just one to be
     // resolved, and
     // use that instead.
     public Style loadStyle(String fileName, String base)
         throws IOException {
         return loadStyle(new File(base + fileName));
     }
 
     /**
      * Load GeoTools2 Style from a fileName
      *
      * @param fileName
      *            DOCUMENT ME!
      *
      * @return DOCUMENT ME!
      *
      * @throws IOException
      *             DOCUMENT ME!
      */
     public Style loadStyle(File fileName) throws IOException {
         SLDParser stylereader = new SLDParser(styleFactory, fileName);
 
         return stylereader.readXML()[0];
     }
 
     /**
      * tests whether a given file is a file containing type information.
      *
      * @param testFile
      *            the file to test.
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
      * of its connection pool (if appropriate). Right now we are asumming a one
      * DataStore equals One "Connection".
      * </p>
      *
      * <p>
      * This is a good compromize since I just want to indicate the amount of
      * resources currently tied up by GeoServer.
      * </p>
      *
      * @return Number of available connections.
      */
     public synchronized int getConnectionCount() {
         int count = 0;
         DataStoreInfo meta;
         DataStore dataStore;
 
         for (Iterator i = dataStores.values().iterator(); i.hasNext();) {
             meta = (DataStoreInfo) i.next();
 
             if (!meta.isEnabled()) {
                 continue; // disabled
             }
 
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
     public synchronized int getLockCount() {
         int count = 0;
         DataStore dataStore;
         ;
 
         LockingManager lockingManager;
 
         for (Iterator i = dataStores.values().iterator(); i.hasNext();) {
             DataStoreInfo meta = (DataStoreInfo) i.next();
 
             if (!meta.isEnabled()) {
                 continue; // disabled
             }
 
             try {
                 dataStore = meta.getDataStore();
             } catch (IllegalStateException notAvailable) {
                 continue; // not available
             } catch (Throwable huh) {
                 continue; // not even working
             }
 
             lockingManager = dataStore.getLockingManager();
 
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
     public synchronized int lockReleaseAll() {
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
             // count += lockingManager.releaseAll();
         }
 
         return count;
     }
 
     /**
      * Release lock by authorization
      *
      * @param lockID
      */
     public synchronized void lockRelease(String lockID) {
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
 
             Transaction t = new DefaultTransaction("Refresh " + meta.getNameSpace());
 
             try {
                 t.addAuthorization(lockID);
 
                 if (lockingManager.release(lockID, t)) {
                     refresh = true;
                 }
             } catch (IOException e) {
                 if (LOGGER.isLoggable(Level.WARNING)) {
                     LOGGER.log(Level.WARNING, e.getMessage(), e);
                 }
             } finally {
                 try {
                     t.close();
                 } catch (IOException closeException) {
                     if (LOGGER.isLoggable(Level.FINEST)) {
                         LOGGER.log(Level.FINEST, closeException.getMessage(), closeException);
                     }
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
     public synchronized void lockRefresh(String lockID) {
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
 
             Transaction t = new DefaultTransaction("Refresh " + meta.getNameSpace());
 
             try {
                 t.addAuthorization(lockID);
 
                 if (lockingManager.refresh(lockID, t)) {
                     refresh = true;
                 }
             } catch (IOException e) {
                 if (LOGGER.isLoggable(Level.WARNING)) {
                     LOGGER.log(Level.WARNING, e.getMessage(), e);
                 }
             } finally {
                 try {
                     t.close();
                 } catch (IOException closeException) {
                     if (LOGGER.isLoggable(Level.FINEST)) {
                         LOGGER.log(Level.FINEST, closeException.getMessage(), closeException);
                     }
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
     public synchronized boolean lockRefresh(String lockID, Transaction t)
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
     public synchronized boolean lockRelease(String lockID, Transaction t)
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
     public synchronized boolean lockExists(String lockID) {
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
     public synchronized Set getPrefixes() {
         return Collections.unmodifiableSet(nameSpaces.keySet());
     }
 
     /**
      * Prefix of the defaultNamespace.
      *
      * @return prefix of the default namespace
      *
      * @see org.geotools.data.Catalog#getDefaultPrefix()
      */
     public synchronized String getDefaultPrefix() {
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
     public synchronized NameSpaceInfo getNamespaceMetaData(String prefix) {
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
      * GeoSever and the global package would really like to have <b>complete</b>
      * control over the DataStores in use by the application. It recognize that
      * this may not always be possible. As GeoServer is extend with additional
      * Modules (such as config) that wish to locate and talk to DataStores
      * independently of GeoServer the best we can do is ask them to register
      * with the this Catalog in global.
      * </p>
      *
      * <p>
      * This reveals what may be a deisgn flaw in GeoTools2 DataStore. We have
      * know way of knowing if the dataStore has already been placed into our
      * care as DataStores are not good at identifying themselves. To complicate
      * matters most keep a static connectionPool around in their GDSFactory - it
      * could be that the Factories are supposed to be smart enough to prevent
      * duplication.
      * </p>
      *
      * @param dataStore
      *
      * @throws IOException
      *
      * @see org.geotools.data.Catalog#registerDataStore(org.geotools.data.DataStore)
      */
     public synchronized void registerDataStore(DataStore dataStore)
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
      * @param prefix
      *            Namespace prefix in which the FeatureType available
      * @param typeName
      *            typeNamed used to identify FeatureType
      *
      * @return
      *
      * @throws IOException
      *             DOCUMENT ME!
      *
      * @see org.geotools.data.Catalog#getFeatureSource(java.lang.String,
      *      java.lang.String)
      */
     public synchronized FeatureSource getFeatureSource(String prefix, String typeName)
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
      *
      * @uml.property name="baseDir"
      */
     public File getBaseDir() {
         return baseDir;
     }
 
     /**
      * Given a layer name will return its type, or null if the layer is not there
      * @param layerName the layer name, either fully qualified (namespace:name) or
      *        just the name if the layers happens to be in the default namespace
      * @return the layer type (see {@link #TYPE_VECTOR} and {@link #TYPE_RASTER})
      */
     public Integer getLayerType(final String layerName) {
         Integer layerType = (Integer) layerNames.get(layerName);
 
         if (layerType != null) {
             return layerType;
         }
 
         // vector layers are namespace prefixed, coverages are not
         if (layerName.indexOf(":") == -1) {
             final String prefixedName = defaultNameSpace.getPrefix() + ":" + layerName;
 
             return (Integer) layerNames.get(prefixedName);
         } else {
             final String strippedName = layerName.substring(layerName.indexOf(":") + 1,
                     layerName.length());
 
             return (Integer) layerNames.get(strippedName);
         }
     }
 
     /**
      * Returns an unmodifiable set of known layer names (feature type and coverages)
      * @return
      */
     public Set getLayerNames() {
         return Collections.unmodifiableSet(layerNames.keySet());
     }
 }
