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
 
     private static final Logger LOGGER = LoggerFactory.getLogger(OracleDataStoreManager.class);
 
     private static final Semaphore SYNCH= new Semaphore(1);
 
 	private static final JDBCDataStoreFactory ORACLE_FACTORY = new OracleNGDataStoreFactory();
 
 
     final static String STATS_TABLE = "STATISTICAL_TABLE";
     final static String SPATIAL_TABLE = "SPATIAL_TABLE";
     final static String STATS_TMP_TABLE = "STATISTICAL_TMP_TABLE";
     final static String SPATIAL_TMP_TABLE = "SPATIAL_TMP_TABLE";
 
     private final Map<String, Serializable> orclMap = new HashMap<String, Serializable>();
 
     private static String dbtype = "oracle";
 
 
     SimpleFeatureType sfTmpGeom = null;
     SimpleFeatureType sfTmpStats = null;
     SimpleFeatureType sfStats = null;
     SimpleFeatureType sfGeom = null;
 
     /*
      * Accepts params for connecting to the Oracle datastore
      */
     public OracleDataStoreManager(String hostname, Integer port, String database,
         String schema, String user, String password) throws Exception
     {
 
         DataStore orclDataStore = null;
         Transaction orclTransaction = null;
         try
         {
             initOrclMap(hostname, port, database, schema, user, password);
             
             // check that all table are there
             orclDataStore = createOracleDataStore();
             
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
             close(orclDataStore, orclTransaction);
             
             //permits to other threads to check if the tables are there or not
             SYNCH.release();
         }
 
     }
 
 
     /**
      * @return
      * @throws Exception
      */
     private DataStore createOracleDataStore() throws Exception
     {
         try
         {
             return ORACLE_FACTORY.createDataStore(orclMap);
 
         }
         catch (Exception e)
         {
             throw new Exception("Error creating the ORACLE data store, check the connection parameters", e);
         }
     }
 
 
     /***********
      * initialize connection parameters to the Oracle dataStore
      * @param hostname
      * @param port
      * @param database
      * @param schema
      * @param user
      * @param password
      */
 
     private void initOrclMap(String hostname, Integer port, String database, String schema, String user,
         String password)
     {
     	
         orclMap.put(DBTYPE.key, dbtype);
         orclMap.put(HOST.key, hostname);
         orclMap.put(PORT.key, port);
         orclMap.put(DATABASE.key, database);
         orclMap.put(SCHEMA.key, schema);
         orclMap.put(USER.key, user);
         orclMap.put(PASSWD.key, password);
         orclMap.put(MINCONN.key, 1);
         orclMap.put(MAXCONN.key, 10);
         orclMap.put(MAXWAIT.key, 100000);
         orclMap.put(VALIDATECONN.key, true);
 
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
     private void actionTemp(DataStore ds, Transaction tx, SimpleFeatureCollection collection, String srcLayer,
         String trgLayer, String srcCode, String trgCode, int itemsPerPage) throws Exception
     {
         cleanTemp(ds, tx);
         saveToTemp(ds, tx, collection, srcLayer, trgLayer, srcCode, trgCode, itemsPerPage);
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
     private void action(DataStore ds, Transaction tx, String srcLayer, String trgLayer, String srcCode,
         String trgCode) throws Exception
     {
         FeatureStore featureStoreTmpData = (FeatureStore) ds.getFeatureSource(STATS_TMP_TABLE);
         FeatureStore featureStoreTmpGeom = (FeatureStore) ds.getFeatureSource(STATS_TMP_TABLE);
 
         featureStoreTmpData.setTransaction(tx);
         featureStoreTmpGeom.setTransaction(tx);
 
         FeatureStore featureStoreData = (FeatureStore) ds.getFeatureSource(STATS_TABLE);
         FeatureStore featureStoreGeom = (FeatureStore) ds.getFeatureSource(SPATIAL_TABLE);
 
         featureStoreData.setTransaction(tx);
         featureStoreGeom.setTransaction(tx);
 
         deleteOldInstancesFromPermanent(srcLayer, trgLayer, srcCode, trgCode, featureStoreData, featureStoreGeom);
 
         saveToPermanent(featureStoreTmpData, featureStoreData);
         saveToPermanent(featureStoreTmpGeom, featureStoreGeom);
     }
 
 
     /******
      * copy the intersections and the stats temporary information into the permanent tables
      * @param tx
      * @param tableFrom
      * @param tableTo
      * @param ds
      * @throws IOException
      */
     private void saveToPermanent(FeatureStore featureStoreFrom, FeatureStore featureStoreTo) throws IOException
     {
         LOGGER.trace("Saving data from permanent table.");
 
         try
         {
             featureStoreTo.addFeatures(featureStoreFrom.getFeatures());
         }
         finally
         {
             if (featureStoreFrom != null)
             {
                 featureStoreFrom.getDataStore().dispose();
             }
 
             if (featureStoreTo != null)
             {
                 featureStoreTo.getDataStore().dispose();
             }
         }
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
     private void close(DataStore ds, Transaction tx)
     {
         try
         {
             if (tx != null)
             {
                 tx.close();
             }
         
         }
         catch (Exception e)
         {
            LOGGER.error("Exception closing the transaction", e);
         }    	
     	
         try
         {
             if (ds != null)
             {
                 ds.dispose();
             }
 
         }
         catch (Exception e)
         {
            LOGGER.error("Exception closing the transaction", e);
         }
     }
 
     private void cleanTemp(DataStore ds, Transaction tx) throws IOException
     {
         LOGGER.trace("Cleaning temp table");
 
         FeatureStore featureStoreData = null;
         FeatureStore featureStoreGeom = null;
         try
         {
             featureStoreData = (FeatureStore) ds.getFeatureSource(SPATIAL_TMP_TABLE);
             featureStoreGeom = (FeatureStore) ds.getFeatureSource(STATS_TMP_TABLE);
 
             featureStoreData.setTransaction(tx);
             featureStoreGeom.setTransaction(tx);
             featureStoreData.removeFeatures(Filter.INCLUDE);
             featureStoreGeom.removeFeatures(Filter.INCLUDE);
         }
         finally
         {
             if (featureStoreGeom != null)
             {
                 featureStoreGeom.getDataStore().dispose();
             }
             if (featureStoreData != null)
             {
                 featureStoreData.getDataStore().dispose();
             }
         }
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
     private void saveToTemp(DataStore ds, Transaction tx, SimpleFeatureCollection source, String srcLayer,
         String trgLayer, String srcCode, String trgCode, int itemsPerPage) throws Exception
     {
         itemsPerPage = (itemsPerPage == 0) ? 50 : itemsPerPage;
 
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
 
             featureStoreData = (FeatureStore) ds.getFeatureSource(STATS_TMP_TABLE);
             featureStoreData.setTransaction(tx);
 
             featureStoreGeom = (FeatureStore) ds.getFeatureSource(SPATIAL_TMP_TABLE);
             featureStoreGeom.setTransaction(tx);
 
             // follow the iterator of the resulting set and divide information into two collection: geometrical and statistical
             boolean iteratorHasFinished = false;
             // this cycle is necessary to save itemsPerPage items at time
             int page = 0;
 
             while (!iteratorHasFinished)
             {
                 LOGGER.debug("PAGE : " + (page++));
 
                 SimpleFeatureCollection sfcData = FeatureCollections.newCollection();
                 SimpleFeatureCollection sfcGeom = FeatureCollections.newCollection();
                 boolean save = false;
                 while (iterator.hasNext() && !save)
                 {
                     i++;
                     if ((i % itemsPerPage) == 0){
                         save = true;
                     }
 
                     String intersectionID = srcLayer + "_" + srcCode + "_" + trgLayer + "_" + trgCode + i;
                     
                     SimpleFeature sf = iterator.next();
 
                     featureBuilderData.set("SRCODE", sf.getAttribute(srcLayer + "_" + srcCode));
 
                     featureBuilderData.set("SRCLAYER", srcLayer);
 
                     featureBuilderData.set("TRGLAYER", trgLayer);
 
                     featureBuilderData.set("TRGCODE", sf.getAttribute(trgLayer + "_" + trgCode));
 
                     featureBuilderData.set("INTERSECTION_ID", intersectionID);
                     featureBuilderData.set("SRCCODENAME", srcCode);
                     featureBuilderData.set("TRGCODENAME", trgCode);
 
                     LOGGER.debug("INTERSECTION_ID : " + intersectionID);
                    
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
                 }
 
                 // save statistics to the statistics temporary table
                 featureStoreData.addFeatures(sfcData);
                 // save geometries to the statistics temporary table
                 featureStoreGeom.addFeatures(sfcGeom);
                 if (save == false)
                 {
                     iteratorHasFinished = true;
                 }
             }
         }
         finally
         {
             if (featureStoreGeom != null)
             {
                 featureStoreGeom.getDataStore().dispose();
             }
             if (featureStoreData != null)
             {
                 featureStoreData.getDataStore().dispose();
             }
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
 
         DataStore ds = null;
         Transaction tx = null;
         try
         {
             ds = createOracleDataStore();
             tx = new DefaultTransaction();
             if (ds != null)
             {
                 LOGGER.trace("Performing data saving: SRCLAYER " + srcLayer + ", SRCCODE " + srcCode + ", TRGLAYER " + trgLayer + ", TRGLAYER " + trgCode);
 
                 actionTemp(ds, tx, collection, srcLayer, trgLayer, srcCode, trgCode, itemsPerPage);
                 tx.commit();
                 res = true;
             }
             else
             {
                 LOGGER.error("The collection cannot be null");
                 res = false;
                 throw new IOException("The collection cannot be null ");
             }
         }
         catch (Exception e)
         {
             LOGGER.error("Exception during ORACLE saving. Rolling back ", e);
             tx.rollback();
             throw new IOException("Exception during ORACLE saving. Rolling back ", e);
         }
         finally
         {
             close(ds, tx);
         }
 
         try
         {
             ds = createOracleDataStore();
             tx = new DefaultTransaction();
             if (res && (ds != null))
             {
                 action(ds, tx, srcLayer, trgLayer, srcCode, trgCode);
                 tx.commit();
                 res = true;
             }
             else
             {
                 LOGGER.error("The collection cannot be null");
                 res = false;
                 throw new IOException("The collection cannot be null ");
             }
         }
         catch (Exception e)
         {
             LOGGER.error("Exception during ORACLE saving. Rolling back ", e);
             tx.rollback();
             throw new IOException("Exception during ORACLE saving. Rolling back ", e);
         }
         finally
         {
             close(ds, tx);
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
         DataStore orclDatastore = null;
         Transaction orclTransaction = null;
         try
         {
             orclDatastore = createOracleDataStore();
             orclTransaction = new DefaultTransaction();
 
             FeatureStore featureStoreTmpData = (FeatureStore) orclDatastore.getFeatureSource(STATS_TMP_TABLE);
             FeatureStore featureStoreTmpGeom = (FeatureStore) orclDatastore.getFeatureSource(STATS_TMP_TABLE);
 
             featureStoreTmpData.setTransaction(orclTransaction);
             featureStoreTmpGeom.setTransaction(orclTransaction);
 
             FeatureStore featureStoreData = (FeatureStore) orclDatastore.getFeatureSource(STATS_TABLE);
             FeatureStore featureStoreGeom = (FeatureStore) orclDatastore.getFeatureSource(SPATIAL_TABLE);
 
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
             close(orclDatastore, orclTransaction);
         }
 
     }
 
 }
