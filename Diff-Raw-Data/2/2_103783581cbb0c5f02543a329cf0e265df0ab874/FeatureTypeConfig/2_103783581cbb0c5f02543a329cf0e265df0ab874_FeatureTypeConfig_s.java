 /* Copyright (c) 2001 - 2007 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.config;
 
 import com.vividsolutions.jts.geom.Envelope;
 import com.vividsolutions.jts.geom.Envelope;
 import com.vividsolutions.jts.geom.GeometryFactory;
 import com.vividsolutions.jts.geom.GeometryFactory;
 import org.geotools.referencing.CRS;
 import org.opengis.feature.simple.SimpleFeatureType;
 import org.opengis.feature.type.AttributeDescriptor;
 import org.opengis.feature.type.GeometryDescriptor;
 import org.opengis.filter.Filter;
 import org.opengis.referencing.FactoryException;
 import org.opengis.referencing.crs.CoordinateReferenceSystem;
 import org.vfny.geoserver.global.FeatureTypeInfo;
 import org.vfny.geoserver.global.dto.AttributeTypeInfoDTO;
 import org.vfny.geoserver.global.dto.CloneLibrary;
 import org.vfny.geoserver.global.dto.FeatureTypeInfoDTO;
 import org.vfny.geoserver.global.dto.FeatureTypeInfoDTO;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 import java.util.logging.Logger;
 
 
 /**
  * User interface SimpleFeatureType staging area.
  *
  * @author dzwiers, Refractions Research, Inc.
  * @version $Id$
  */
 public class FeatureTypeConfig {
     protected static Logger LOGGER = org.geotools.util.logging.Logging.getLogger("org.vfny.geoserver.config");
 
     /** The Id of the datastore which should be used to get this featuretype. */
     private String dataStoreId;
 
     /** An EPSG:4326 bounding box for this featuretype */
     private Envelope latLongBBox;
 
     /** A native CRS bounding box for this featuretype */
     private Envelope nativeBBox;
 
     /** native wich EPGS code for the FeatureTypeInfo */
     private int SRS;
     
     /** 
      * Either force declared or reproject from native to declared, see {@link FeatureTypeInfo#FORCE}
      * and {@link FeatureTypeInfo#REPROJECT}
      */
     private int SRSHandling;
 
     /**
      * This is an ordered list of AttributeTypeInfoConfig.
      * <p>
      * These attribtue have been defined by the user (or schema.xml file).
      * Additional attribute may be assumed based on the schemaBase
      * </p>
      * <p>
      * If this is <code>null</code>, all Attribtue information
      * will be generated. An empty list is used to indicate that only
      * attribtues indicated by the schemaBase will be returned.
      * </p>
      */
     private List schemaAttributes;
 
     /** Name (must match DataStore typeName). */
     private String name;
     
     /**
      * The user facing alias for this feature type, if any
      */
     private String alias;
 
     /**
      *
      */
     private String wmsPath;
 
     /**
     * The schema name.
     * <p>
     * Usually  name + "_Type"
     * </p>
     */
     private String schemaName;
 
     /**
      * The schema base.
      * <p>
      * The schema base is used to indicate additional attribtues, not defined
      * by the user. These attribute are fixed -not be edited by the user.
      * </p>
      * <p>
      * This easiest is "AbstractFeatureType"
      * </p>
      */
     private String schemaBase;
 
     /**
      * The featuretype directory name.
      * <p>
      * This is used to write to, and is  stored because it may be longer than
      * the name, as this often includes information about the source of the
      * featuretype.
      * </p>
      * <p>
      * A common naming convention is: <code>dataStoreId + "_" + name</code>
      * </p>
      */
     private String dirName;
 
     /**
      * The featuretype title.
      * <p>
      * Not sure what this is used for - usually name+"_Type"
      */
     private String title;
 
     /** The feature type abstract, short explanation of this featuretype. */
     private String _abstract;
 
     /**
      * A list of keywords to associate with this featuretype.
      * <p>
      * Keywords are destinct strings, often rendered surrounded by brackets
      * to aid search engines.
      * </p>
      */
     private Set keywords;
 
     /**
      * A list of metadata links to associate with this featuretype.
      * <p>
      * Metadata URLs are distinct URLs.
      * </p>
      */
     private Set metadataLinks;
 
     /** Configuration information used to specify numeric percision */
     private int numDecimals;
 
     /**
      * Filter used to limit query.
      * <p>
      * TODO: Check the following comment - I don't belive it.
      * The list of exposed attributes. If the list is empty or not present at
      * all, all the FeatureTypeInfo's attributes are exposed, if is present,
      * only those oattributes in this list will be exposed by the services
      * </p>
      */
     private Filter definitionQuery = null;
 
     /**
      * The default style name.
      */
     private String defaultStyle;
 
     /**
      * Other WMS Styles
      */
     private ArrayList styles;
 
     /**
      * A featureType-specific override for the defaultMaxAge defined in WMSConfig.  This value is added the
      * headers of generated maps, marking them as being both "cache-able" and designating the time for which
      * they are to remain valid.  The specific header added is "CacheControl: max-age="
      */
     private String cacheMaxAge;
 
     /**
      * Should we be adding the CacheControl: max-age header to outgoing maps which include this layer?
      */
     private boolean cachingEnabled;
 
     /**
      * Should we list this layer when crawlers request the sitemap?
      */
     private boolean indexingEnabled;
 
     /**
      * The name of the property to use when regionating using the attribute strategy.
      */
     private String regionateAttribute;
 
     private String regionateStrategy;
 
     private int regionateFeatureLimit;
     
     /**
      * The maximum number of features to be served for this feature type (it's understood
      * it's less than the global maxFeatures). 0 is used as the "no limit" flag
      */
     private int maxFeatures = 0;
 
     /**
      * Package visible constructor for test cases
      */
     FeatureTypeConfig() {
     }
 
     /**
      * Creates a FeatureTypeInfo to represent an instance with default data.
      *
      * @param dataStoreId ID for data store in catalog
      * @param schema Geotools2 SimpleFeatureType
      * @param generate True to generate entries for all attribtues
      */
     public FeatureTypeConfig(String dataStoreId, SimpleFeatureType schema, boolean generate) {
         if ((dataStoreId == null) || (dataStoreId.length() == 0)) {
             throw new IllegalArgumentException("dataStoreId is required for FeatureTypeConfig");
         }
 
         if (schema == null) {
             throw new IllegalArgumentException("SimpleFeatureType is required for FeatureTypeConfig");
         }
 
         this.dataStoreId = dataStoreId;
         latLongBBox = new Envelope();
         nativeBBox = new Envelope();
         SRS = lookupSRS(schema.getGeometryDescriptor());
 
         if (generate) {
             this.schemaAttributes = new ArrayList();
 
             for (int i = 0; i < schema.getAttributeCount(); i++) {
                 AttributeDescriptor attrib = schema.getDescriptor(i);
                 this.schemaAttributes.add(new AttributeTypeInfoConfig(attrib));
             }
         } else {
             this.schemaAttributes = null;
         }
 
         defaultStyle = "";
         styles = new ArrayList();
         name = schema.getTypeName();
         wmsPath = "/";
         title = schema.getTypeName() + "_Type";
         _abstract = "Generated from " + dataStoreId;
         keywords = new HashSet();
         keywords.add(dataStoreId);
         keywords.add(name);
         metadataLinks = new HashSet();
         numDecimals = 8;
         definitionQuery = null;
         dirName = dataStoreId + "_" + name;
         schemaName = name + "_Type";
         schemaBase = "gml:AbstractFeatureType";
 
         cachingEnabled = false;
         cacheMaxAge = null;
 
         indexingEnabled = false;
         regionateAttribute = "";
        regionateStrategy = "sld";
         regionateFeatureLimit = 15;
     }
 
     /**
      * TODO: this method is duplicated with CoveragesEditorAction and should be replaced by
      * an equivalent method in CRS class. Once the methods is added, forward to the CRS class.
      * @param defaultGeometry
      * @return
      */
     private int lookupSRS(GeometryDescriptor defaultGeometry) {
         // NPE avoidance
         if (defaultGeometry == null) {
             return -1;
         }
 
         // try the (deprecated) geometry factory, we don't want to break data stores that
         // do correctly set it
         //GeometryFactory geometryFactory = defaultGeometry.getGeometryFactory();
         Integer epsgCode = null;
         try {
             if(defaultGeometry.getCoordinateReferenceSystem() != null)
                 epsgCode = CRS.lookupEpsgCode(defaultGeometry.getCoordinateReferenceSystem(),true);
         } catch (FactoryException e) {
             //log this?
         }
         if ( epsgCode != null ) {
             return epsgCode.intValue();
         }
         
         return 0;
 
         //        // try to reverse engineer the SRID from the coordinate system
         //        CoordinateReferenceSystem ref = defaultGeometry.getCoordinateSystem();
         //        String code = CRS.lookupIdentifier(ref, Collections.singleton("EPSG"), true);
         //        if(code == null)
         //            return 0;
         //        if(code.startsWith("EPSG:")) {
         //            code = code.substring(5);
         //        }
         //        try {
         //            return Integer.parseInt(code);
         //        } catch(NumberFormatException e) {
         //            LOGGER.severe("Could not parse EPSG code: " + code);
         //            return 0;
         //        }
     }
 
     /**
      * FeatureTypeInfo constructor.
      *
      * <p>
      * Creates a copy of the FeatureTypeInfoDTO provided. All the data
      * structures are cloned.
      * </p>
      *
      * @param dto The FeatureTypeInfoDTO to copy.
      *
      * @throws NullPointerException DOCUMENT ME!
      */
     public FeatureTypeConfig(FeatureTypeInfoDTO dto) {
         if (dto == null) {
             throw new NullPointerException("Non null FeatureTypeInfoDTO required");
         }
 
         dataStoreId = dto.getDataStoreId();
         latLongBBox = new Envelope(dto.getLatLongBBox());
         nativeBBox = dto.getNativeBBox() != null ? new Envelope(dto.getNativeBBox()) : null;
         SRS = dto.getSRS();
         SRSHandling = dto.getSRSHandling();
 
         if (dto.getSchemaAttributes() == null) {
             schemaAttributes = null;
         } else {
             schemaAttributes = new LinkedList();
 
             Iterator i = dto.getSchemaAttributes().iterator();
 
             while (i.hasNext()) {
                 schemaAttributes.add(new AttributeTypeInfoConfig((AttributeTypeInfoDTO) i.next()));
             }
         }
 
         name = dto.getName();
         alias = dto.getAlias();
         wmsPath = dto.getWmsPath();
         title = dto.getTitle();
         _abstract = dto.getAbstract();
         numDecimals = dto.getNumDecimals();
         definitionQuery = dto.getDefinitionQuery();
 
         try {
             keywords = new HashSet(dto.getKeywords());
         } catch (Exception e) {
             keywords = new HashSet();
         }
 
         try {
             metadataLinks = new HashSet(dto.getMetadataLinks());
         } catch (Exception e) {
             metadataLinks = new HashSet();
         }
 
         defaultStyle = dto.getDefaultStyle();
         styles = dto.getStyles();
         dirName = dto.getDirName();
         schemaName = dto.getSchemaName();
         schemaBase = dto.getSchemaBase();
 
         cachingEnabled = dto.isCachingEnabled();
         cacheMaxAge = dto.getCacheMaxAge();
         maxFeatures = dto.getMaxFeatures();
 
         indexingEnabled = dto.isIndexingEnabled();
         regionateAttribute = dto.getRegionateAttribute();
         regionateStrategy = dto.getRegionateStrategy();
         regionateFeatureLimit = dto.getRegionateFeatureLimit();
     }
 
     /**
      * Implement toDTO.
      *
      * <p>
      * Creates a represetation of this object as a FeatureTypeInfoDTO
      * </p>
      *
      * @return a representation of this object as a FeatureTypeInfoDTO
      *
      * @see org.vfny.geoserver.config.DataStructure#toDTO()
      */
     public FeatureTypeInfoDTO toDTO() {
         FeatureTypeInfoDTO f = new FeatureTypeInfoDTO();
         f.setDataStoreId(dataStoreId);
         f.setLatLongBBox(CloneLibrary.clone(latLongBBox));
         f.setNativeBBox(CloneLibrary.clone(nativeBBox));
         f.setSRS(SRS);
         f.setSRSHandling(SRSHandling);
 
         if (schemaAttributes == null) {
             // Use generated default attributes
             f.setSchemaAttributes(null);
         } else {
             // Use user provided attribtue + schemaBase attribtues
             List s = new ArrayList();
 
             for (int i = 0; i < schemaAttributes.size(); i++) {
                 s.add(((AttributeTypeInfoConfig) schemaAttributes.get(i)).toDTO());
             }
 
             f.setSchemaAttributes(s);
         }
 
         f.setName(name);
         f.setAlias(alias);
         f.setWmsPath(wmsPath);
         f.setTitle(title);
         f.setAbstract(_abstract);
         f.setNumDecimals(numDecimals);
         f.setDefinitionQuery(definitionQuery);
 
         try {
             f.setKeywords(new ArrayList(keywords));
         } catch (Exception e) {
             // do nothing, defaults already exist.
         }
 
         try {
             f.setMetadataLinks(new ArrayList(metadataLinks));
         } catch (Exception e) {
             // do nothing, defaults already exist.
         }
 
         f.setDefaultStyle(defaultStyle);
         f.setStyles(styles);
         // override the dir name to make sure 
         if(alias != null)
             f.setDirName(dataStoreId + "_" + alias);
         else if(dirName.endsWith(name))
             f.setDirName(dirName);
         else
             f.setDirName(dataStoreId + "_" + name);
         f.setSchemaBase(schemaBase);
         f.setSchemaName(schemaName);
 
         f.setCachingEnabled(cachingEnabled);
         f.setCacheMaxAge(cacheMaxAge);
         f.setMaxFeatures(maxFeatures);
 
         f.setIndexingEnabled(indexingEnabled);
         f.setRegionateAttribute(regionateAttribute);
         f.setRegionateStrategy(regionateStrategy);
         f.setRegionateFeatureLimit(regionateFeatureLimit);
 
         return f;
     }
 
     /**
      * Searches through the schema looking for an AttributeTypeInfoConfig that
      * matches the name passed in attributeTypeName
      *
      * @param attributeTypeName the name of the AttributeTypeInfo to search
      *        for.
      *
      * @return AttributeTypeInfoConfig from the schema, if found
      */
     public AttributeTypeInfoConfig getAttributeFromSchema(String attributeTypeName) {
         Iterator iter = schemaAttributes.iterator();
 
         while (iter.hasNext()) {
             AttributeTypeInfoConfig atiConfig = (AttributeTypeInfoConfig) iter.next();
 
             if (atiConfig.getName().equals(attributeTypeName)) {
                 return atiConfig;
             }
         }
 
         return null;
     }
 
     /**
      * Convience method for dataStoreId.typeName.
      *
      * <p>
      * This key may be used to store this SimpleFeatureType in a Map for later.
      * </p>
      *
      * @return dataStoreId.typeName
      */
     public String getKey() {
         return getDataStoreId() + DataConfig.SEPARATOR + getName();
     }
 
     /**
      * Access _abstract property.
      *
      * @return Returns the _abstract.
      */
     public String getAbstract() {
         return _abstract;
     }
 
     /**
      * Set _abstract to _abstract.
      *
      * @param _abstract The _abstract to set.
      */
     public void setAbstract(String _abstract) {
         this._abstract = _abstract;
     }
 
     /**
      * Access dataStoreId property.
      *
      * @return Returns the dataStoreId.
      */
     public String getDataStoreId() {
         return dataStoreId;
     }
 
     /**
      * Set dataStoreId to dataStoreId.
      *
      * @param dataStoreId The dataStoreId to set.
      */
     public void setDataStoreId(String dataStoreId) {
         this.dataStoreId = dataStoreId;
     }
 
     /**
      * Access defaultStyle property.
      *
      * @return Returns the defaultStyle.
      */
     public String getDefaultStyle() {
         return defaultStyle;
     }
 
     /**
      * Set defaultStyle to defaultStyle.
      *
      * @param defaultStyle The defaultStyle to set.
      */
     public void setDefaultStyle(String defaultStyle) {
         this.defaultStyle = defaultStyle;
     }
 
     public ArrayList getStyles() {
         return styles;
     }
 
     public void setStyles(ArrayList styles) {
         this.styles = styles;
     }
 
     public void addStyle(String style) {
         if (!this.styles.contains(style)) {
             this.styles.add(style);
         }
     }
 
     /**
      * Access definitionQuery property.
      *
      * @return Returns the definitionQuery.
      */
     public Filter getDefinitionQuery() {
         return definitionQuery;
     }
 
     /**
      * Set definitionQuery to definitionQuery.
      *
      * @param definitionQuery The definitionQuery to set.
      */
     public void setDefinitionQuery(Filter definitionQuery) {
         this.definitionQuery = definitionQuery;
     }
 
     /**
      * Access dirName property.
      *
      * @return Returns the dirName.
      */
     public String getDirName() {
         return dirName;
     }
 
     /**
      * Set dirName to dirName.
      *
      * @param dirName The dirName to set.
      */
     public void setDirName(String dirName) {
         this.dirName = dirName;
     }
 
     /**
      * Access keywords property.
      *
      * @return Returns the keywords.
      */
     public Set getKeywords() {
         return keywords;
     }
 
     /**
      * Set keywords to keywords.
      *
      * @param keywords The keywords to set.
      */
     public void setKeywords(Set keywords) {
         this.keywords = keywords;
     }
 
     /**
      * Access metadataURLs property.
      *
      * @return Returns the metadataURLs.
      */
     public Set getMetadataLinks() {
         return metadataLinks;
     }
 
     /**
      * Set metadataURLs to metadataURLs.
      *
      * @param metadataURLs The metadataURLs to set.
      */
     public void setMetadataLinks(Set metadataURLs) {
         this.metadataLinks = metadataURLs;
     }
 
     /**
      * Access latLongBBox property.
      *
      * @return Returns the latLongBBox.
      */
     public Envelope getLatLongBBox() {
         return latLongBBox;
     }
 
     /**
      * Set latLongBBox to latLongBBox.
      *
      * @param latLongBBox The latLongBBox to set.
      */
     public void setLatLongBBox(Envelope latLongBBox) {
         this.latLongBBox = latLongBBox;
     }
 
     /**
      * Access nativeBBox property.
      *
      * @return Returns the nativeBBox.
      */
     public Envelope getNativeBBox() {
         return nativeBBox;
     }
 
     /**
      * Set nativeBBox to nativeBBox.
      *
      * @param nativeBBox The nativeBBox to set.
      */
     public void setNativeBBox(Envelope nativeBBox) {
         this.nativeBBox = nativeBBox;
     }
 
     /**
      * Access name property.
      *
      * @return Returns the name.
      */
     public String getName() {
         return name;
     }
 
     /**
      * Set name to name.
      *
      * @param name The name to set.
      */
     public void setName(String name) {
         this.name = name;
     }
 
     /**
      * Access numDecimals property.
      *
      * @return Returns the numDecimals.
      */
     public int getNumDecimals() {
         return numDecimals;
     }
 
     /**
      * Set numDecimals to numDecimals.
      *
      * @param numDecimals The numDecimals to set.
      */
     public void setNumDecimals(int numDecimals) {
         this.numDecimals = numDecimals;
     }
 
     /**
      * Access schemaAttributes property.
      *
      * @return Returns the schemaAttributes.
      */
     public List getSchemaAttributes() {
         return schemaAttributes;
     }
 
     /**
      * Set schemaAttributes to schemaAttributes.
      *
      * @param schemaAttributes The schemaAttributes to set.
      */
     public void setSchemaAttributes(List schemaAttributes) {
         this.schemaAttributes = schemaAttributes;
     }
 
     /**
      * Access schemaBase property.
      *
      * @return Returns the schemaBase.
      */
     public String getSchemaBase() {
         return schemaBase;
     }
 
     /**
      * Set schemaBase to schemaBase.
      *
      * @param schemaBase The schemaBase to set.
      */
     public void setSchemaBase(String schemaBase) {
         this.schemaBase = schemaBase;
     }
 
     /**
      * Access schemaName property.
      *
      * @return Returns the schemaName.
      */
     public String getSchemaName() {
         return schemaName;
     }
 
     /**
      * Set schemaName to schemaName.
      *
      * @param schemaName The schemaName to set.
      */
     public void setSchemaName(String schemaName) {
         this.schemaName = schemaName;
     }
 
     /**
      * Access sRS property.
      *
      * @return Returns the sRS.
      */
     public int getSRS() {
         return SRS;
     }
     
     
 
     /**
      * Set sRS to srs.
      *
      * @param srs The sRS to set.
      */
     public void setSRS(int srs) {
         SRS = srs;
     }
     
     public int getSRSHandling() {
         return SRSHandling;
     }
     
     public void setSRSHandling(int srsHandling) {
         this.SRSHandling = srsHandling;
     }
 
     /**
      * Access title property.
      *
      * @return Returns the title.
      */
     public String getTitle() {
         return title;
     }
 
     /**
      * Set title to title.
      *
      * @param title The title to set.
      */
     public void setTitle(String title) {
         this.title = title;
     }
 
     public String toString() {
         return "FeatureTypeConfig[name: " + name + " alias: " + alias + " schemaName: " + schemaName + " SRS: " + SRS
         + " schemaAttributes: " + schemaAttributes + " schemaBase " + schemaBase + "]";
     }
 
     public String getWmsPath() {
         return wmsPath;
     }
 
     public void setWmsPath(String wmsPath) {
         this.wmsPath = wmsPath;
     }
 
     public boolean isCachingEnabled() {
         return cachingEnabled;
     }
 
     public boolean isIndexingEnabled() {
         return indexingEnabled;
     }
 
     /**
      * Which property should we use when regionating using the attribute strategy?
      * @return the name of the property
      */
     public String getRegionateAttribute(){
         return regionateAttribute;
     }
 
     public String getRegionateStrategy(){
         return regionateStrategy;
     }
 
     public int getRegionateFeatureLimit(){
         return regionateFeatureLimit;
     }
 
     public void setCachingEnabled(boolean cachingEnabled) {
         this.cachingEnabled = cachingEnabled;
     }
 
     public void setIndexingEnabled(boolean indexingEnabled){
         this.indexingEnabled = indexingEnabled;
     }
 
     /**
      * Set which property to use when regionating using the attribute strategy.
      * @param attr the name of the property
      */
     public void setRegionateAttribute(String attr){
         this.regionateAttribute = attr;
     }
 
     public void setRegionateStrategy(String strategy){
         this.regionateStrategy = strategy;
     }
 
     public void setRegionateFeatureLimit(int limit){
         this.regionateFeatureLimit = limit;
     }
 
     public String getCacheMaxAge() {
         return cacheMaxAge;
     }
 
     public void setCacheMaxAge(String cacheMaxAge) {
         this.cacheMaxAge = cacheMaxAge;
     }
 
     public int getMaxFeatures() {
         return maxFeatures;
     }
 
     public void setMaxFeatures(int maxFeatures) {
         this.maxFeatures = maxFeatures;
     }
     
     public String getAlias() {
         return alias;
     }
 
     public void setAlias(String alias) {
         this.alias = alias;
     }
 }
