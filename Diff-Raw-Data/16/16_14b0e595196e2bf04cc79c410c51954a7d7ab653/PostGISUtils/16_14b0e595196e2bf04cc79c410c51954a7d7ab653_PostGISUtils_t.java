 /*
  *  GeoBatch - Open Source geospatial batch processing system
  *  https://github.com/nfms4redd/nfms-geobatch
  *  Copyright (C) 2007-2008-2009 GeoSolutions S.A.S.
  *  http://www.geo-solutions.it
  *
  *  GPLv3 + Classpath exception
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package it.geosolutions.geobatch.unredd.script.util;
 
 import it.geosolutions.geobatch.unredd.script.exception.PostGisException;
 import it.geosolutions.geobatch.unredd.script.model.PostGisConfig;
 
 import java.io.File;
 import java.io.IOException;
 import java.math.BigDecimal;
import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.geotools.data.DataStore;
 import org.geotools.data.DataStoreFinder;
 import org.geotools.data.DefaultTransaction;
 import org.geotools.data.FeatureSource;
 import org.geotools.data.FileDataStore;
 import org.geotools.data.FileDataStoreFinder;
 import org.geotools.data.Transaction;
 import org.geotools.data.collection.ListFeatureCollection;
 import org.geotools.data.simple.SimpleFeatureCollection;
 import org.geotools.data.simple.SimpleFeatureIterator;
 import org.geotools.data.simple.SimpleFeatureSource;
 import org.geotools.data.simple.SimpleFeatureStore;
 import org.geotools.factory.CommonFactoryFinder;
 import org.geotools.factory.GeoTools;
 import org.geotools.feature.simple.SimpleFeatureBuilder;
 import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
 import org.geotools.jdbc.JDBCFeatureStore;
 import org.opengis.feature.Property;
 import org.opengis.feature.simple.SimpleFeature;
 import org.opengis.feature.simple.SimpleFeatureType;
 import org.opengis.feature.type.AttributeDescriptor;
 import org.opengis.feature.type.PropertyDescriptor;
 import org.opengis.filter.Filter;
 import org.opengis.filter.FilterFactory2;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Various utilities for interacting with postgis
  * 
  * 
  * @author ETJ, GeoSolutions
  * @author Simone GeoSolutions, GeoSolutions
  *
  */
 public class PostGISUtils {
 
     private final static Logger LOGGER = LoggerFactory.getLogger(PostGISUtils.class);
 
     public final static int ITEM_X_PAGE = 1000;
     
     public final static FilterFactory2 FF = CommonFactoryFinder.getFilterFactory2(GeoTools.getDefaultHints());
 
     /**
      * @deprecated this string should be specified in the configuration
      */
     public final static String YEARATTRIBUTENAME = "unredd_year";
     /**
      * @deprecated this string should be specified in the configuration
      */
     public final static String MONTHATTRIBUTENAME = "unredd_month";
     /**
      * @deprecated this string should be specified in the configuration
      */
     public final static String DAYATTRIBUTENAME = "unredd_day";
     public final static String DATEATTRIBUTENAME = "unredd_date";
 
 
     public static DataStore createDatastore(PostGisConfig cfg) throws PostGisException {
 
         DataStore ret = null;
         try {
             ret = (DataStore)DataStoreFinder.getDataStore(cfg.buildGeoToolsMap());
         } catch (IOException ex) {
             throw new PostGisException("Can't create PostGIS datastore ["+cfg+"]", ex);
         }
         if ( ret == null ) {
             LOGGER.error("DataStore not found: " + cfg);
             throw new PostGisException("DataStore not found: " + cfg);
         }
         return ret;
     }
 
     public static FileDataStore createDatastore(File shapeFile) throws PostGisException {
 
         FileDataStore ret = null;
         try {
             ret = FileDataStoreFinder.getDataStore(shapeFile);
         } catch (IOException ex) {
             throw new PostGisException("Can't create shapeFile datastore ["+shapeFile+"]", ex);
         }
         if ( ret == null ) {
             LOGGER.error("DataStore not found for " + shapeFile);
             throw new PostGisException("DataStore not found: " + shapeFile);
         }
         return ret;
     }
 
 
     /**
      * Load new features from a ShapeFile into PostGis.
      *
      * <br/>Attrbiutes concerning year, month and date will be automatically added.
      * <br/>If the related table does not exists, it will be automatically created.
      * <br/>If the table already exist, attributes in the shapefile must match the ones in the table - except for the 3 added fields.
      *
      * @return the count of features copied
      * TODO this must become generic and NOT create a store per se for the postgis layer!!!
      */
     public static int shapeToPostGis(File srcShapeFile, PostGisConfig dstPg, String layer, String year, String month, String day) throws PostGisException {
         FileDataStore srcStore = null;
         try {
             // read the shape file and put it into a SimpeFeatureCollection
             srcStore = createDatastore(srcShapeFile);
             
             SimpleFeatureSource featureSource = srcStore.getFeatureSource();
             SimpleFeatureCollection sfc = (SimpleFeatureCollection) featureSource.getFeatures();
             return enrichAndAddFeatures(sfc, dstPg, layer, year, month, day, true);
         } catch (IOException ex) {
             throw new PostGisException("Error copying features: " + ex.getMessage(), ex);
         } finally {
             quietDisposeStore(srcStore);
         }
     }
 
 
     /**
      * ************
      * this method read features (filtered by layer, year, month) from the postgis db specified in param and save them into the
      * current postgis
      *
      * @param params
      * @param layer the layer filter parameter
      * @param year the year filter parameter
      * @param month the month filter parameter
      * @param day the day of the month filter parameter
      * @param itemsForPage it indicates how many featues should be saved time by time
      * @param forceCreation if the destination table does not exist, create it
      * @throws IOException
      *
      * TODO REVIEW! filter&copy, used in publishing?
      */
     public static void copyFeatures(DataStore srcDs, DataStore dstDs, 
             String layer, String year, String month, String day, boolean forceCreation) throws PostGisException {
 
         LOGGER.debug("Copy snapshot : " + layer + ", " + year + ", " + month + ", " + day);
        
         try {
         
 	        // read the shape file and put it into a SimpeFeatureCollection
 	        SimpleFeatureSource srcFs = srcDs.getFeatureSource(layer);
         
             // try to look for the destination table
             if(Arrays.asList(dstDs.getTypeNames()).contains(layer)) {
                 LOGGER.error("******* "+layer +" EXISTS *******");
             } else
                 LOGGER.error("******* "+layer +" DOES NOT EXIST *******");
 	        try {
 	            SimpleFeatureSource dstFs = (SimpleFeatureSource) dstDs.getFeatureSource(layer);
 	        } catch (Exception e) {
 	            LOGGER.debug("Exception while getting dst featureSource from " + layer +": " +e.getMessage(), e );
 	            
 	            if (forceCreation) {
 	                // force creation of the target table
 	                LOGGER.warn("An exception was raised when connecting to " + layer + " of the dissemintation system. Forcing creation");
 	                SimpleFeatureType sft = SimpleFeatureTypeBuilder.copy((SimpleFeatureType) srcFs.getSchema());
 	                dstDs.createSchema(sft);
 	
 	
 	            } else {
 	                LOGGER.error("An exception was raised when connecting to " + layer);
 	                throw new PostGisException("The layer " + layer + " does not exist in the dissemintation system", e);
 	            }
 	
 	        }
             
             Filter filter = (Filter) FF.equals(FF.property(YEARATTRIBUTENAME), FF.literal(Integer.parseInt(year)));
             if (month != null) {
                 Filter monthFilter = (Filter) FF.equals(FF.property(MONTHATTRIBUTENAME), FF.literal(Integer.parseInt(month)));
                 filter = (Filter) FF.and(filter, monthFilter);
             }
             
             if (day != null) {
                 Filter dayFilter = (Filter) FF.equals(FF.property(DAYATTRIBUTENAME), FF.literal(Integer.parseInt(day)));
                 filter = (Filter) FF.and(filter, dayFilter);
             }
 
             LOGGER.info("Filter: " + filter);
             
             final SimpleFeatureCollection filteredSF = srcFs.getFeatures(filter);
 
 	        if (filteredSF == null || (filteredSF != null && filteredSF.isEmpty())) {
 	            LOGGER.warn(" The filtered collection is empty. Skip copying");
 	            return;
 	        }
 	        LOGGER.info("The filtered collection is not empty. Starting copy " + filteredSF.size() + " features");
 	        copyFeatures(layer, filteredSF, dstDs);
       } catch (Exception e) {
 	      LOGGER.error("The source layer " + layer + " cannot be accessed. Maybe it does not exist. Skip execution", e);
 	      throw new PostGisException("Source layer " + layer + " cannot be accessed.");
       } 
 
     }
 
     /**
      * **************
      * this method insert into the layer datastore of postgis the feature contained into layerFile moreover for each feature
      * insert the year and the month attributes
      * 
      * TODO review!!
      */
     public static int enrichAndAddFeatures(
     		SimpleFeatureCollection sourceFC, 
     		PostGisConfig dstPg,
             String layer, 
             String year, 
             String month,
             String day,
             boolean forceCreation)
                 throws PostGisException {
 
     	DataStore dstDs=null;
         SimpleFeatureSource fsLayer = null;
         Transaction tx = new DefaultTransaction();
 
         //== check schema: create new or check they are aligned
         try {
         	// craete destination store
             dstDs = createDatastore(dstPg);
             
             // check if destinationlayer exists
             boolean layerExists = existFeatureTable(dstDs, layer);
             if( ! layerExists ) {
                 if(forceCreation) {
                     fsLayer = createEnrichedSchema(dstDs, (SimpleFeatureType) sourceFC.getSchema(), layer);
                 } else {
                     throw new PostGisException("The layer " + layer + " does not exist");
                 }
             } else {
                 fsLayer =  dstDs.getFeatureSource(layer);
                 checkAttributesMatch(sourceFC, ((JDBCFeatureStore)dstDs.getFeatureSource(layer)).getFeatureSource());
             }
 
 	        //== schemas are ok: transfer data
 	
 	        int iYear = Integer.parseInt(year);
 	        int iMonth = month==null? -1 : Integer.parseInt(month);
 	        int iDay = day==null? -1 : Integer.parseInt(day);
 	        Date date = new Date(iYear-1900, iMonth==-1?0:iMonth-1, iDay==-1?1:iDay);
 	
 	        SimpleFeatureStore featureStoreData = (SimpleFeatureStore) fsLayer;
 	
 	        // update the layer store with the new SimpleFeature coming from the shape file
 	        // data are saved itemsForPage elements at time
 
             SimpleFeatureType dstSchema = dstDs.getSchema(layer);
             featureStoreData.setTransaction(tx);
             SimpleFeatureType srcSchema = sourceFC.getSchema();
             List<AttributeDescriptor> srcAttributeDescriptor = srcSchema.getAttributeDescriptors();
 
             SimpleFeatureBuilder featureBuilderData = new SimpleFeatureBuilder(dstSchema);
             SimpleFeatureIterator featureIterator = sourceFC.features();
             boolean hasFinished = false;
             int i = 0;
             Set<String> loggedMissingAttrib = new HashSet<String>();
             while (!hasFinished) { // TODO: refacotr this nested loop!!!
 
            	//DamianoGiampaoli 03/11/2014
            	//TEST IT!!!
            	// Due to the upgrade needed from the Geotools 8-SNAPSHOT to geotools 10.8 in order to support GeoBatch 1.4.x 
            	// The add(SimpleFeature) method of SimpleFeatureCollection cannot be used anymore so a List<SimpleFeature> must be used.
                //SimpleFeatureCollection sfcData = FeatureCollections.newCollection();
                List<SimpleFeature> sfcData = new ArrayList<SimpleFeature>();
                
                 boolean exitForIntermediateSaving = false;
 
                 while (featureIterator.hasNext() && !exitForIntermediateSaving) {
                     i++;
 
                     exitForIntermediateSaving = ((i % ITEM_X_PAGE) == 0);
 
                     SimpleFeature sf = featureIterator.next();
                     SimpleFeature data = featureBuilderData.buildFeature(sf.getID());
                     for (int j = 0; j < srcAttributeDescriptor.size(); j++) {
                         String srcName = srcAttributeDescriptor.get(j).getLocalName();
                         String dstName = srcName.toLowerCase(); // FIXME: this is a worksroung for SHP 2 PG attrib name conversion. make it general!
 
                         Property p = sf.getProperty(srcName);
                         if( p!= null) // be lenient about inexistent attributes: consistency checks have already bben performed.
                             data.setAttribute(dstName, sf.getAttribute(srcName));
                         else {
                             if(LOGGER.isDebugEnabled() && ! loggedMissingAttrib.contains(srcName) ) {
                                 LOGGER.debug("Skipping attrib "+srcName+" in feature #"+i);
                                 loggedMissingAttrib.add(srcName);
                             }
                         }
                     }
 
                     data.setAttribute(YEARATTRIBUTENAME, iYear);
                     if(iMonth != -1){
                         data.setAttribute(MONTHATTRIBUTENAME, month);
                     }
                     if(iDay != -1){
                         data.setAttribute(DAYATTRIBUTENAME, day);
                     }
                     data.setAttribute(DATEATTRIBUTENAME, date);
                     sfcData.add(data);
 
                 }
                 if (!exitForIntermediateSaving) {
                     hasFinished = true;
                 }
                SimpleFeatureCollection featureCollection = new ListFeatureCollection(srcSchema, sfcData);
                featureStoreData.addFeatures(featureCollection);
             }
             
             tx.commit();
             LOGGER.info("Copied " + i + " features for "+ layer+"/"+year+"/"+month+"/"+day+ " to " + dstPg);
             return i;
 
         } catch (Exception e) {
             quietRollbackTransaction(tx);
             LOGGER.error("Exception while copying shp into db", e);
             throw new PostGisException("Exception while copying shp into db", e);
         } finally {
         	// clean up
             quietCloseTransaction(tx);
             quietDisposeStore(dstDs);
         }
     }
 
     private static SimpleFeatureSource createEnrichedSchema(DataStore dstDs, SimpleFeatureType sourceFC, String layer) throws PostGisException {
       // generate the target schema
         try {
             LOGGER.warn("Creating new table for layer " + layer);
 
             SimpleFeatureTypeBuilder dstSchemaBuilder = new SimpleFeatureTypeBuilder();
             dstSchemaBuilder.setName(layer);
             dstSchemaBuilder.addAll(sourceFC.getAttributeDescriptors());
             dstSchemaBuilder.add(YEARATTRIBUTENAME, Integer.class);
             dstSchemaBuilder.add(MONTHATTRIBUTENAME, Integer.class);
             dstSchemaBuilder.add(DAYATTRIBUTENAME, Integer.class);
             dstSchemaBuilder.add(DATEATTRIBUTENAME, Date.class);
             SimpleFeatureType sft = dstSchemaBuilder.buildFeatureType();
 
             if(LOGGER.isDebugEnabled()) {
                 LOGGER.debug("Adding source attribs:");
                 for (AttributeDescriptor attr : sourceFC.getAttributeDescriptors()) {
                     LOGGER.debug( " Attr: "+attr);
                 }
             }
             dstDs.createSchema(sft);
             SimpleFeatureStore fsLayer = (SimpleFeatureStore)dstDs.getFeatureSource(layer);
             SimpleFeatureType dstSft = dstDs.getFeatureSource(layer).getSchema();
 
             if ( LOGGER.isInfoEnabled() ) {
                 for (PropertyDescriptor pd : dstSft.getDescriptors()) {
                     LOGGER.info("Attribute : " + pd.getName() + " " + pd.getType());
                 }
             }
 
             return fsLayer;
         } catch (IOException e) {
             throw new PostGisException("Error while creating table " + layer+ ": " + e.getMessage(), e);
         }
     }
 
     private static void checkAttributesMatch(SimpleFeatureCollection sourceFC, FeatureSource dstFs) throws PostGisException {
         SimpleFeatureType srcSchema = sourceFC.getSchema();
         SimpleFeatureType dstSchema = (SimpleFeatureType)dstFs.getSchema();
 
         // adapt case: converto to lowercase
         Map<String, AttributeDescriptor> dstAttrbiutes = new HashMap<String, AttributeDescriptor>();
         for (AttributeDescriptor srcAttr : dstSchema.getAttributeDescriptors()) {
             String dstName = srcAttr.getLocalName();
             dstAttrbiutes.put(dstName.toLowerCase(), srcAttr);
         }
 
         // now check for attribs ignoring case
         StringBuilder sbErr = new StringBuilder();
 
         for (AttributeDescriptor srcAttr : srcSchema.getAttributeDescriptors()) {
             String srcName = srcAttr.getLocalName();
 //            AttributeDescriptor dstAttr = dstSchema.getDescriptor(srcName);
             AttributeDescriptor dstAttr = dstAttrbiutes.get(srcName.toLowerCase());
             if(dstAttr==null) {
                 sbErr.append("[No such dst attribute '").append(srcName).append("']");
                 continue;
             }
 //            LOGGER.debug("Checking SRC["
 ////                    +srcAttr+"]["
 //                    +srcAttr.getLocalName()+"]["
 //                   +srcAttr.getType()+"]["
 //                    +srcAttr.getType().getName()+"]["
 //                    +srcAttr.getType().getBinding()+"]["
 //                    +srcAttr.getType().getClass()+"]["
 ////                    +srcAttr+"]["
 //                    +"]");
 //            LOGGER.debug("Checking DST["
 ////                    +dstAttr+"]["
 //                    +dstAttr.getLocalName()+"]["
 //                    +dstAttr.getType()+"]["
 //                    +dstAttr.getType().getName()+"]["
 //                    +dstAttr.getType().getBinding()+"]["
 //                    +dstAttr.getType().getClass()+"]["
 ////                    +dstAttr+"]["
 //                    +"]");
 
 //            if(! srcAttr.getType().getBinding().equals(dstAttr.getType().getBinding()) ) {
             if( ! isCompat(srcAttr, dstAttr)) {
 //                if(LOGGER.isDebugEnabled() )
 //                    LOGGER.debug("Attrib mismatch: src:"+srcAttr + " dst:"+dstAttr
 //                            + " binding s:["+srcAttr.getType().getBinding()+"] d:["+dstAttr.getType().getBinding()+"]");
                 sbErr.append("[Attribute type mismatch ").append(srcName)
                         .append("' src:").append(srcAttr.getType())
                         .append(" dst:").append(dstAttr.getType())
                         .append("]");
             }
         }
 
         // todo: should also check for missing [attribs in dst but not in src] attribs?
 
         if(sbErr.length() > 0)
             throw new PostGisException("Schema mismatch: " + sbErr);
     }
 
     /**
      * Quick 'n' dirty compatibility checker.
      * @TODO: isthere something in GT that already performs such checks?
      */
     public static boolean isCompat(AttributeDescriptor srcAttr, AttributeDescriptor dstAttr) {
         Class b1 = srcAttr.getType().getBinding();
         Class b2 = dstAttr.getType().getBinding();
 
         // some euristic for combinations found in tested cases -- FIXME!
         if(b1 == Double.class && b2 == BigDecimal.class) {
             return true;
         }
         if(b1 == java.util.Date.class && b2 == java.sql.Date.class) {
             return true;
         }
 
         if(! srcAttr.getType().getBinding().equals(dstAttr.getType().getBinding()) ) {
             if(LOGGER.isDebugEnabled() )
                 LOGGER.debug("Attrib mismatch: src:"+srcAttr + " dst:"+dstAttr
                         + " binding s:["+srcAttr.getType().getBinding()+"] d:["+dstAttr.getType().getBinding()+"]");
             return false;
         }
         
         return true;
     }
     
     /**
      * 
      * @param cfg
      * @param featureName
      * @return a List of attribute that describe this feature
      * @throws IOException 
      */
     public static List<AttributeDescriptor> getFeatureAttributes(DataStore ds, String featureName) throws IOException{
     
     	SimpleFeatureSource featureSource = ds.getFeatureSource(featureName);
         SimpleFeatureCollection sfc = (SimpleFeatureCollection) featureSource.getFeatures();
         return sfc.getSchema().getAttributeDescriptors();
     }
     
 
     
     /**
      * Check if the provided table exists inside the specified datastore
      * 
      * @param ds the {@link DataStore} to inspect
      * @param layer the feature type to look for
      * @return True if the Table for supplyed features exist, else otherwise
      */
     public static boolean existFeatureTable(DataStore ds, String layer){
     	
     	boolean  exist = false;
     	try {
     		final String[] typeNames=ds.getTypeNames();
     		if(typeNames!=null && typeNames.length>0){
     			exist = Arrays.asList(typeNames).contains(layer);
     		}
 		} catch (Exception e) {
 			LOGGER.debug(e.getMessage(), e);
 			exist = false;
 		}
 		return exist;
 		
     	
     }
     
     /**
      * Check if a table for a supplied feature exist. If not exist create it.
      * 
      * @param cfg
      * @param featureName
      * @param attrDescriptorList
      * @return True if a table is created, false otherwise
      */
 	public static boolean checkExistAndCreateFeatureTable(DataStore ds, String featureName, List<AttributeDescriptor> attrDescriptorList) throws PostGisException{
 
 		try {
 		
 			LOGGER.warn("Creating new table for layer " + featureName);
 			
 			if (!existFeatureTable(ds, featureName)) {
 				SimpleFeatureTypeBuilder dstSchemaBuilder = new SimpleFeatureTypeBuilder();
 				dstSchemaBuilder.setName(featureName);
 				dstSchemaBuilder.addAll(attrDescriptorList);
 				SimpleFeatureType sft = dstSchemaBuilder.buildFeatureType();
 	
 				ds.createSchema(sft);
 				return true;
 			}
 		} catch (IOException e) {
 			LOGGER.error(e.getMessage(), e);
 			throw new PostGisException(e.getMessage());
 		}
 		return false;
 
 	}    
 
     public static void removeFeatures(DataStore ds, String layer, String year, String month, String day) throws PostGisException, IOException {
 
         Transaction tx = new DefaultTransaction();
         try{
 
 	        LOGGER.debug("remove features : " + layer + ", " + year + ", " + month + ", " + day);
 	        Filter filter = (Filter) FF.equals(FF.property(YEARATTRIBUTENAME), FF.literal(Integer.parseInt(year)));
 	        if (month != null) {
 	            Filter monthFilter = (Filter) FF.equals(FF.property(MONTHATTRIBUTENAME), FF.literal(Integer.parseInt(month)));
 	            filter = (Filter) FF.and(filter, monthFilter);
 	        }
 	        if (day != null) {
 	            Filter dayFilter = (Filter) FF.equals(FF.property(DAYATTRIBUTENAME), FF.literal(Integer.parseInt(day)));
 	            filter = (Filter) FF.and(filter, dayFilter);
 	        }
 	
 	        LOGGER.debug("Filter: " + filter);
 
             // try to look for the destination table
             SimpleFeatureStore store = (SimpleFeatureStore) ds.getFeatureSource(layer);
             store.removeFeatures(filter);
             tx.commit();
         } catch (Exception e) {
             quietRollbackTransaction(tx);
             LOGGER.error("An exception was raised when deleting features from " + layer + "", e);
             throw new PostGisException("An exception was raised when deleting features from " + layer, e);
         } finally {
             quietCloseTransaction(tx);
         }
     }
     
     /**
      * this method copy a feature from a postgis DB to another
      * 
      * @param layer the feature to copy
      * @param sourceFC
      * @param dstDS
      * @throws PostGisException
      */
     private static void copyFeatures(String layer, SimpleFeatureCollection sourceFC, DataStore dstDS) throws PostGisException {
         
         SimpleFeatureSource fsDestLayer = null;
         try {
             fsDestLayer = dstDS.getFeatureSource(layer);
         } catch (Exception e) {
             LOGGER.error("Destination layer " +layer + " does not exist:" + e.getMessage(), e);
             throw new PostGisException("Destination layer " +layer + " does not exist:" + e.getMessage(), e);
         }
 
         final SimpleFeatureStore featureStoreData = (SimpleFeatureStore) fsDestLayer;
         SimpleFeatureIterator iterator = null;
         final Transaction tx = new DefaultTransaction();
         featureStoreData.setTransaction(tx);
         // update the layer store with the new SimpleFeature coming from the shape file
         // data are saved itemsForPage elements at time
         
         try {
         	
         	// open iterator
             iterator = sourceFC.features();
         	// prepare feature holder
         	final ListFeatureCollection lfc= new ListFeatureCollection(sourceFC.getSchema());
         	int count=0;
         	int numPag = sourceFC.size() / ITEM_X_PAGE;
         	int currentPage = 0;
         	LOGGER.info("Page size: " + ITEM_X_PAGE + ", Page amount: " + numPag);
         	while (iterator.hasNext()) {
 
             	// copy over
                final SimpleFeature sf = iterator.next();
                lfc.add(sf);
 //               LOGGER.debug("Feature " + count + " - page: " + currentPage);
                // paging check
                if(count++>=ITEM_X_PAGE){
             	   // commit to relief load from destination DB
                    featureStoreData.addFeatures(lfc);                       
                    tx.commit();
                    lfc.clear();
                    count=0;
 //                   System.gc();
                    LOGGER.debug("Page" + currentPage++ + "/" + numPag + " committed... ");
 
                }
             }
         	LOGGER.info("Commit latest features...");
             if(!lfc.isEmpty()){
 	     	   // commit to relief load from destination DB
 	            featureStoreData.addFeatures(lfc);
 	           
 	            // final commit
 	            tx.commit();    
             }
         } catch (Exception e) {
             quietRollbackTransaction(tx);
             LOGGER.error("An exception was raised when executing storing into the database");
 
             throw new PostGisException("An exception was raised when executing storing into the database", e);
         } finally {
         	// close transaction
             quietCloseTransaction(tx);
             
             // close iterator
             quietCloseIterator(iterator);
             LOGGER.info("features copy succesfully completed!");
         }
     }
 
     public static void quietCloseIterator(SimpleFeatureIterator it) {
     	if(it==null){
     		return;
     	}
         try {
             it.close();
         } catch (Exception ex) {
             LOGGER.warn("Error in closing iterator: " + ex.getMessage(), ex);
         }
     }
     
     public static void quietRollbackTransaction(Transaction tx) {
     	if(tx==null){
     		return;
     	}
         try {
             tx.rollback();
         } catch (IOException ex) {
             LOGGER.warn("Error in rolling back transaction: " + ex.getMessage(), ex);
         }
     }
 
     public static void quietDisposeStore(DataStore ds) {
     	if(ds==null){
     		return;
     	}    	
         try {
             ds.dispose();
         } catch (Exception ex) {
             LOGGER.warn("Error in closing store: " + ex.getMessage(), ex);
         }
     }
     public static void quietCloseTransaction(Transaction tx) {
     	if(tx==null){
     		return;
     	}    	
         try {
             tx.close();
         } catch (IOException ex) {
             LOGGER.warn("Error in closing transaction: " + ex.getMessage(), ex);
         }
     }
 
 }
