  /*
  * ====================================================================
  *
  * GeoBatch - Intersection Engine
  *
  * Copyright (C) 2007 - 2011 GeoSolutions S.A.S.
  * http://www.geo-solutions.it
  *
  * GPLv3 + Classpath exception
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.
  *
  * ====================================================================
  *
  * This software consists of voluntary contributions made by developers
  * of GeoSolutions.  For more information on GeoSolutions, please see
  * <http://www.geo-solutions.it/>.
  *
  */
 package it.geosolutions.geobatch.figis.intersection;
 
 
 import static org.geotools.jdbc.JDBCDataStoreFactory.DATABASE;
 import static org.geotools.jdbc.JDBCDataStoreFactory.DBTYPE;
 import static org.geotools.jdbc.JDBCDataStoreFactory.HOST;
 import static org.geotools.jdbc.JDBCDataStoreFactory.MAXCONN;
 import static org.geotools.jdbc.JDBCDataStoreFactory.MAXWAIT;
 import static org.geotools.jdbc.JDBCDataStoreFactory.MINCONN;
 import static org.geotools.jdbc.JDBCDataStoreFactory.PASSWD;
 import static org.geotools.jdbc.JDBCDataStoreFactory.PORT;
 import static org.geotools.jdbc.JDBCDataStoreFactory.SCHEMA;
 import static org.geotools.jdbc.JDBCDataStoreFactory.USER;
 import static org.geotools.jdbc.JDBCDataStoreFactory.VALIDATECONN;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.Semaphore;
 
 import org.geotools.data.DataStore;
 import org.geotools.data.DefaultTransaction;
 import org.geotools.data.FeatureStore;
 import org.geotools.data.Transaction;
 import org.geotools.data.oracle.OracleNGDataStoreFactory;
 import org.geotools.data.simple.SimpleFeatureCollection;
 import org.geotools.data.simple.SimpleFeatureIterator;
 import org.geotools.factory.CommonFactoryFinder;
 import org.geotools.feature.FeatureCollections;
 import org.geotools.feature.simple.SimpleFeatureBuilder;
 import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
 import org.geotools.geometry.jts.JTS;
 import org.geotools.jdbc.JDBCDataStoreFactory;
 import org.geotools.referencing.CRS;
 import org.geotools.referencing.crs.DefaultGeographicCRS;
 import org.opengis.feature.simple.SimpleFeature;
 import org.opengis.feature.simple.SimpleFeatureType;
 import org.opengis.filter.Filter;
 import org.opengis.filter.FilterFactory;
 import org.opengis.referencing.crs.CoordinateReferenceSystem;
 import org.opengis.referencing.operation.MathTransform;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.vividsolutions.jts.geom.MultiPolygon;
 
 
 public class OracleDataStoreManager
 {
 
     static final int DEFAULT_PAGE_SIZE = 50;
 
 	private static final Logger LOGGER = LoggerFactory.getLogger(OracleDataStoreManager.class);
 
     private static final Semaphore SYNCH = new Semaphore(1);
 
 	private static final JDBCDataStoreFactory ORACLE_FACTORY = new OracleNGDataStoreFactory();
 
 
     final static String STATS_TABLE = "STATISTICAL_TABLE";
     final static String SPATIAL_TABLE = "SPATIAL_TABLE";
     final static String STATS_TMP_TABLE = "STATISTICAL_TMP_TABLE";
     final static String SPATIAL_TMP_TABLE = "SPATIAL_TMP_TABLE";
 
     private final Map<String, Serializable> orclMap = new HashMap<String, Serializable>();
 
     private static String DEFAULT_DBTYPE = "oracle";
 
 
     SimpleFeatureType sfTmpGeom = null;
     SimpleFeatureType sfTmpStats = null;
     SimpleFeatureType sfStats = null;
     SimpleFeatureType sfGeom = null;
 
 	private final DataStore orclDataStore;
 
     /**
      * Accepts params for connecting to the Oracle datastore
      */
     public OracleDataStoreManager(
     		String hostname, 
     		Integer port, 
     		String database,
     		String schema, 
     		String user, 
     		String password) throws Exception
     {
 
         Transaction orclTransaction = null;
         try
         {
         	// init arguments
             orclMap.put(DBTYPE.key, DEFAULT_DBTYPE);
             orclMap.put(HOST.key, hostname);
             orclMap.put(PORT.key, port);
             orclMap.put(DATABASE.key, database);
             orclMap.put(SCHEMA.key, schema);
             orclMap.put(USER.key, user);
             orclMap.put(PASSWD.key, password);
             orclMap.put(MINCONN.key, 10);
             orclMap.put(MAXCONN.key, 25);
             orclMap.put(MAXWAIT.key, 100000);
             orclMap.put(VALIDATECONN.key, true);
             
             // create the underlying store and check that all table are there
             try
             {
                 orclDataStore = ORACLE_FACTORY.createDataStore(orclMap);
 
             }
             catch (Exception e)
             {
                 throw new Exception("Error creating the ORACLE data store, check the connection parameters", e);
             }
             
             orclTransaction = new DefaultTransaction();
             
             SYNCH.acquire();
             initTables(orclDataStore, orclTransaction); 
             
             // commit the transaction
             orclTransaction.commit();
         }
         catch (Exception e)
         {
         	try{
                 orclTransaction.rollback();
         	} catch (Exception e1) {
 				if(LOGGER.isErrorEnabled()){
 					LOGGER.error(e1.getLocalizedMessage(),e1);
 				}
 			}
             throw new Exception("Error creating tables in ORACLE", e);
         }
         finally
         {
             close(orclTransaction);
             
             //permits to other threads to check if the tables are there or not
             SYNCH.release();
         }
 
     }
 
 
     /**
      * Disposes the underlying GeoTools {@link DataStore} once for all.
      * 
      */
     public void dispose(){
     	if(orclDataStore!=null){
     		try{
     			orclDataStore.dispose();
     		} catch (Exception e) {
 				LOGGER.trace(e.getLocalizedMessage(),e);
 			}
     	}
     }
 
 
 
     /************
      * initialize the tables: create them if they do not exist
      * @param tx
      * @throws IOException
      */
     private void initTables(DataStore ds, Transaction tx) throws Exception
     {
         // init of the Temp table for stats
         SimpleFeatureTypeBuilder sftbTmpStats = new SimpleFeatureTypeBuilder();
         sftbTmpStats.setName(STATS_TMP_TABLE);
         sftbTmpStats.add("INTERSECTION_ID", String.class);
         sftbTmpStats.add("SRCODE", String.class);
         sftbTmpStats.add("TRGCODE", String.class);
         sftbTmpStats.add("SRCLAYER", String.class);
         sftbTmpStats.add("TRGLAYER", String.class);
         sftbTmpStats.add("SRCAREA", String.class);
         sftbTmpStats.add("TRGAREA", String.class);
         sftbTmpStats.add("SRCOVLPCT", String.class);
         sftbTmpStats.add("TRGOVLPCT", String.class);
         sftbTmpStats.add("SRCCODENAME", String.class);
         sftbTmpStats.add("TRGCODENAME", String.class);
         sfTmpStats = sftbTmpStats.buildFeatureType();
 
 
         // init of the permanent table for stats
         SimpleFeatureTypeBuilder sftbStats = new SimpleFeatureTypeBuilder();
         sftbStats.setName(STATS_TABLE);
         sftbStats.add("INTERSECTION_ID", String.class);
         sftbStats.add("SRCODE", String.class);
         sftbStats.add("TRGCODE", String.class);
         sftbStats.add("SRCLAYER", String.class);
         sftbStats.add("TRGLAYER", String.class);
         sftbStats.add("SRCAREA", String.class);
         sftbStats.add("TRGAREA", String.class);
         sftbStats.add("SRCOVLPCT", String.class);
         sftbStats.add("TRGOVLPCT", String.class);
         sftbStats.add("SRCCODENAME", String.class);
         sftbStats.add("TRGCODENAME", String.class);
         sfStats = sftbStats.buildFeatureType();
 
 
         // init of the temp table for the list of geometries
         SimpleFeatureTypeBuilder sftbTempGeoms = new SimpleFeatureTypeBuilder();
 
         sftbTempGeoms.setName(SPATIAL_TMP_TABLE);
         sftbTempGeoms.add("THE_GEOM", MultiPolygon.class);
         sftbTempGeoms.add("INTERSECTION_ID", String.class);
 
 
         sftbTempGeoms.setCRS(DefaultGeographicCRS.WGS84);
 
         sfTmpGeom = sftbTempGeoms.buildFeatureType();
 
         // init of the table for the list of geometries
         SimpleFeatureTypeBuilder sftbGeoms = new SimpleFeatureTypeBuilder();
 
         sftbGeoms.setName(SPATIAL_TABLE);
         sftbGeoms.add("THE_GEOM", MultiPolygon.class);
         sftbGeoms.add("INTERSECTION_ID", String.class);
         sftbGeoms.setCRS(DefaultGeographicCRS.WGS84);
 
         sfGeom = sftbGeoms.buildFeatureType();
 
         // check if tables exist, if not create them
 
         String[] listTables = ds.getTypeNames();
 
         boolean statsTmpTableExists = false;
         boolean statsTableExists = false;
         boolean spatialTmpTableExists = false;
         boolean spatialTableExists = false;
         for (int i = 0; i < listTables.length; i++)
         {
             if (listTables[i].equals(STATS_TMP_TABLE))
             {
                 statsTmpTableExists = true;
             }
             if (listTables[i].equals(STATS_TABLE))
             {
                 statsTableExists = true;
             }
             if (listTables[i].equals(SPATIAL_TABLE))
             {
                 spatialTableExists = true;
             }
             if (listTables[i].equals(SPATIAL_TMP_TABLE))
             {
                 spatialTmpTableExists = true;
             }
         }
         if (!statsTmpTableExists)
         {
             ds.createSchema(sfTmpStats);
         }
         if (!statsTableExists)
         {
             ds.createSchema(sfStats);
         }
         if (!spatialTableExists)
         {
             ds.createSchema(sfGeom);
         }
         if (!spatialTmpTableExists)
         {
             ds.createSchema(sfTmpGeom);
         }
     }
 
     /***********
      * delete all from temporary tables and then save intersections in them
      * @param ds
      * @param tx
      * @param collection
      * @param srcLayer
      * @param trgLayer
      * @param srcCode
      * @param trgCode
      * @return
      * @throws Exception
      */
     private void actionTemp(Transaction tx, SimpleFeatureCollection collection, String srcLayer,
         String trgLayer, String srcCode, String trgCode, int itemsPerPage) throws Exception
     {
        cleanTempTables(tx);
         saveToTemp(tx, collection, srcLayer, trgLayer, srcCode, trgCode, itemsPerPage);
     }
 
     /*********
      * delete all the instances of srcLayer and trgLayer from the permanent tables
      * copy the intersections and the stats temporary information into the permanent tables
      * @param ds
      * @param tx
      * @param srcLayer
      * @param trgLayer
      * @throws Exception
      */
     private void action(Transaction tx, String srcLayer, String trgLayer, String srcCode,
         String trgCode) throws Exception
     {
         FeatureStore featureStoreTmpData = (FeatureStore) orclDataStore.getFeatureSource(STATS_TMP_TABLE);
         FeatureStore featureStoreTmpGeom = (FeatureStore) orclDataStore.getFeatureSource(STATS_TMP_TABLE);
 
         featureStoreTmpData.setTransaction(tx);
         featureStoreTmpGeom.setTransaction(tx);
 
         FeatureStore featureStoreData = (FeatureStore) orclDataStore.getFeatureSource(STATS_TABLE);
         FeatureStore featureStoreGeom = (FeatureStore) orclDataStore.getFeatureSource(SPATIAL_TABLE);
 
         featureStoreData.setTransaction(tx);
         featureStoreGeom.setTransaction(tx);
 
         deleteOldInstancesFromPermanent(srcLayer, trgLayer, srcCode, trgCode, featureStoreData, featureStoreGeom);
         
         if(LOGGER.isTraceEnabled()){
         	LOGGER.trace("Saving data from permanent table.");
         }
         featureStoreData.addFeatures(featureStoreTmpData.getFeatures());
         featureStoreGeom.addFeatures(featureStoreTmpGeom.getFeatures());
     }
 
     /*********
      * delete all the information about the srcLayer and trgLayer pair from the permanent tables
      * @param tx
      * @param srcLayer
      * @param trgLayer
      * @param trgLayerCode
      * @param srcLayerCode
      * @param featureStoreData
      * @param featureStoreGeom
      * @param ds
      * @throws IOException
      */
     private void deleteOldInstancesFromPermanent(String srcLayer, String trgLayer,
         String srcLayerCode, String trgLayerCode, FeatureStore featureStoreData, FeatureStore featureStoreGeom)
         throws Exception
     {
         LOGGER.info("Deleting old instances of the intersection between " + srcLayer + " and " + trgLayer);
 
         SimpleFeatureIterator iterator = null;
 
         try
         {
             final FilterFactory ff = CommonFactoryFinder.getFilterFactory(null);
             Filter filter1 = ff.equals(ff.property("SRCLAYER"), ff.literal(srcLayer));
             Filter filter2 = ff.equals(ff.property("TRGLAYER"), ff.literal(trgLayer));
             Filter filter3 = ff.equals(ff.property("SRCCODENAME"), ff.literal(srcLayerCode));
             Filter filter4 = ff.equals(ff.property("TRGCODENAME"), ff.literal(trgLayerCode));
             Filter filterAnd = ff.and(Arrays.asList(filter1, filter2, filter3, filter4));
             iterator = (SimpleFeatureIterator) featureStoreData.getFeatures(filterAnd).features();
 
             while (iterator.hasNext())
             {
                 String id = (String) iterator.next().getAttribute("INTERSECTION_ID");
                 LOGGER.debug("Cascade delete of " + id);
 
                 Filter filter = ff.equals(ff.property("INTERSECTION_ID"), ff.literal(id));
                 featureStoreGeom.removeFeatures(filter);
                 featureStoreData.removeFeatures(filter);
             }
         }
         catch (Exception e)
         {
             throw e;
         }
         finally
         {
             if (iterator != null)
             {
             	try{
             		iterator.close();
             	}catch(Exception e){
             		LOGGER.error("ERROR on closing Features iterator: ", e);
             	}
             }
 
         }
 
     }
 
     /***
      * close the transactions and the datastore
      * @param tx
      * @param ds
      */
     private void close(Transaction tx)
     {
         if (tx != null) {
             try {
             	tx.close();        
             }
             catch (Exception e)
             {
                 LOGGER.error("Exception closing the transaction", e);
             }    
         }    	
  	
 
     }
 
    private  void cleanTempTables(Transaction tx) throws IOException
     {
         LOGGER.trace("Cleaning temp tables");
       
         FeatureStore featureStoreData = (FeatureStore) orclDataStore.getFeatureSource(SPATIAL_TMP_TABLE);
         featureStoreData.setTransaction(tx);
         FeatureStore featureStoreGeom = (FeatureStore) orclDataStore.getFeatureSource(STATS_TMP_TABLE);
         featureStoreGeom.setTransaction(tx);
         featureStoreData.removeFeatures(Filter.INCLUDE);
         featureStoreGeom.removeFeatures(Filter.INCLUDE);
     }
 
     /*****
      * perform the intersections, split the results into the two temporary tables
      * @param ds
      * @param tx
      * @param source
      * @param srcLayer
      * @param trgLayer
      * @param srcCode
      * @param trgCode
      * @return
      * @throws Exception
      */
     private void saveToTemp(Transaction tx, SimpleFeatureCollection source, String srcLayer,
         String trgLayer, String srcCode, String trgCode, int itemsPerPage) throws Exception
     {
         itemsPerPage = (itemsPerPage <= 1) ? DEFAULT_PAGE_SIZE : itemsPerPage;
 
         LOGGER.info("Saving intersections between " + srcLayer + " and " + trgLayer + " into temporary table");
         if (LOGGER.isDebugEnabled())
         {
             LOGGER.debug("Attributes " + srcCode + " and " + trgCode);
             LOGGER.debug("Items per page " + itemsPerPage);
         }
 
         FeatureStore featureStoreData = null;
         FeatureStore featureStoreGeom = null;
         SimpleFeatureIterator iterator = null;
         try
         {
             if (source == null)
             {
                 throw new Exception("Source cannot be null");
             }
 
             // initialize CRS transformation. It is performed in case source CRS is different from WGS84
             CoordinateReferenceSystem sourceCRS = source.getSchema().getCoordinateReferenceSystem();
             String geomName = source.getSchema().getGeometryDescriptor().getLocalName();
             CoordinateReferenceSystem targetCRS = DefaultGeographicCRS.WGS84;
 
             MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS);
 
             iterator = source.features();
 
             SimpleFeatureBuilder featureBuilderData = new SimpleFeatureBuilder(sfTmpStats);
             SimpleFeatureBuilder featureBuilderGeom = new SimpleFeatureBuilder(sfTmpGeom);
             int i = 0;
 
             featureStoreData = (FeatureStore) orclDataStore.getFeatureSource(STATS_TMP_TABLE);
             featureStoreData.setTransaction(tx);
 
             featureStoreGeom = (FeatureStore) orclDataStore.getFeatureSource(SPATIAL_TMP_TABLE);
             featureStoreGeom.setTransaction(tx);
 
             // this cycle is necessary to save itemsPerPage items at time
             int page = 0;
             if(LOGGER.isDebugEnabled()){
             	LOGGER.debug("PAGE : " + (page));
             }
             SimpleFeatureCollection sfcData = FeatureCollections.newCollection();
             SimpleFeatureCollection sfcGeom = FeatureCollections.newCollection();
             while (iterator.hasNext())
             {
 
                 String intersectionID = srcLayer + "_" + srcCode + "_" + trgLayer + "_" + trgCode + i;
                 
                 SimpleFeature sf = iterator.next();
 
                 featureBuilderData.set("SRCODE", sf.getAttribute(srcLayer + "_" + srcCode));
 
                 featureBuilderData.set("SRCLAYER", srcLayer);
 
                 featureBuilderData.set("TRGLAYER", trgLayer);
 
                 featureBuilderData.set("TRGCODE", sf.getAttribute(trgLayer + "_" + trgCode));
 
                 featureBuilderData.set("INTERSECTION_ID", intersectionID);
                 featureBuilderData.set("SRCCODENAME", srcCode);
                 featureBuilderData.set("TRGCODENAME", trgCode);
 
                 if(LOGGER.isDebugEnabled()){
                 	LOGGER.debug("INTERSECTION_ID : " + intersectionID);
                 }
                
                 if (sf.getAttribute("areaA") != null)
                 {
                     featureBuilderData.set("SRCAREA", sf.getAttribute("areaA"));
                 }
                 if (sf.getAttribute("areaB") != null)
                 {
                     featureBuilderData.set("TRGAREA", sf.getAttribute("areaB"));
                 }
                 if (sf.getAttribute("percentageA") != null)
                 {
                     featureBuilderData.set("SRCOVLPCT", sf.getAttribute("percentageA"));
                 }
                 if (sf.getAttribute("percentageB") != null)
                 {
                     featureBuilderData.set("TRGOVLPCT", sf.getAttribute("percentageB"));
                 }
 
                 SimpleFeature sfwData = featureBuilderData.buildFeature(intersectionID);
                 sfcData.add(sfwData);
 
                 MultiPolygon geometry = (MultiPolygon) sf.getAttribute(geomName);
 
                 MultiPolygon targetGeometry = (MultiPolygon) JTS.transform(geometry, transform);
                 targetGeometry.setSRID(4326);
 
                 featureBuilderGeom.set("THE_GEOM", targetGeometry);
                 featureBuilderGeom.set("INTERSECTION_ID", intersectionID);
 
                 SimpleFeature sfwGeom = featureBuilderGeom.buildFeature(intersectionID);
                 sfcGeom.add(sfwGeom);
 
                 i++;
                 if ((i % itemsPerPage) == 0){
                     // save statistics to the statistics temporary table
                     featureStoreData.addFeatures(sfcData);
                     // save geometries to the statistics temporary table
                     featureStoreGeom.addFeatures(sfcGeom);
                     
                     // clear
                     sfcData.clear();
                     sfcGeom.clear();
                     
                     //increment page
                     page++;
                     
                     if(LOGGER.isDebugEnabled()){
                     	LOGGER.debug("PAGE : " + (page));
                     }
                 }
 
                 
             }
         }
         finally
         {
             if (iterator != null)
             {
                 iterator.close();
             }
         }
     }
 
     /********************************************************************************************************************/
 
     /**
      * recall the steps from updating the database
      * firstly delete all the information from temp tables
      * then, perform the intersection, update temporary and finally update permanent
      * @param collection
      * @param srcLayer
      * @param trgLayer
      * @param srcCode
      * @param trgCode
      * @throws IOException
      */
     public boolean saveAll(SimpleFeatureCollection collection, String srcLayer,
         String trgLayer, String srcCode,
         String trgCode, int itemsPerPage) throws Exception
     {
 
         boolean res = false;
         Transaction tx = null;
         try
         {
             tx = new DefaultTransaction();
             LOGGER.trace("Performing data saving: SRCLAYER " + srcLayer + ", SRCCODE " + srcCode + ", TRGLAYER " + trgLayer + ", TRGLAYER " + trgCode);
             actionTemp(tx, collection, srcLayer, trgLayer, srcCode, trgCode, itemsPerPage);
             tx.commit();
             res = true;
 
         }
         catch (Exception e)
         {
             try{
             	tx.rollback();
             } catch (Exception e1) {
 				LOGGER.trace(e1.getLocalizedMessage(),e1);
 			}
             throw new IOException("Exception during ORACLE saving. Rolling back ", e);
         }
         finally
         {
             close(tx);
         }
 
         try
         {
 
             
             if (res)
             {
             	tx = new DefaultTransaction();
                 action(tx, srcLayer, trgLayer, srcCode, trgCode);
                 tx.commit();
                 res = true;
             }
             else
             {
                 res = false;
                 throw new IOException("The collection cannot be null ");
             }
         }
         catch (Exception e)
         {
             try{
             	tx.rollback();
             } catch (Exception e1) {
 				LOGGER.trace(e1.getLocalizedMessage(),e1);
 			}
             throw new IOException("Exception during ORACLE saving. Rolling back ", e);
         }
         finally
         {
             close(tx);
         }
 
         return res;
     }
 
     /**
      * manage the delete all the information about the srcLayer and trgLayer pair from the permanent tables from a transaction
      * @param srcLayer
      * @param trgLayer
      * @throws IOException
      */
     public void deleteAll(String srcLayer, String trgLayer, String srcCode, String trgCode) throws IOException
     {
 
         Transaction orclTransaction = null;
         try
         {
             orclTransaction = new DefaultTransaction();
 
             FeatureStore featureStoreTmpData = (FeatureStore) orclDataStore.getFeatureSource(STATS_TMP_TABLE);
             FeatureStore featureStoreTmpGeom = (FeatureStore) orclDataStore.getFeatureSource(STATS_TMP_TABLE);
 
             featureStoreTmpData.setTransaction(orclTransaction);
             featureStoreTmpGeom.setTransaction(orclTransaction);
 
             FeatureStore featureStoreData = (FeatureStore) orclDataStore.getFeatureSource(STATS_TABLE);
             FeatureStore featureStoreGeom = (FeatureStore) orclDataStore.getFeatureSource(SPATIAL_TABLE);
 
             featureStoreData.setTransaction(orclTransaction);
             featureStoreGeom.setTransaction(orclTransaction);
 
             deleteOldInstancesFromPermanent(srcLayer, trgLayer, srcCode, trgCode, featureStoreData,featureStoreGeom);
 
             //commit!
             orclTransaction.commit();
         }
         catch (Exception e)
         {
             try{
             	orclTransaction.rollback();
             } catch (Exception e1) {
 				if(LOGGER.isInfoEnabled())
 					LOGGER.info(e1.getLocalizedMessage(),e1);
 			}
             throw new IOException("Delete all raised an exception ", e);
         }
         finally
         {
             close(orclTransaction);
         }
 
     }
 
 }
