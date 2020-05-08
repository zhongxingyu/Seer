 /* RepoxClientTest.java - created on Jan 25, 2012, Copyright (c) 2011 The European Library, all rights reserved */
 package eu.europeana.uim.repox.rest.client;
 
 import java.util.logging.Logger;
 
 import org.junit.Assert;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import eu.europeana.uim.repox.rest.RepoxTestUtils;
 import eu.europeana.uim.repox.rest.client.xml.Aggregator;
 import eu.europeana.uim.repox.rest.client.xml.Aggregators;
 import eu.europeana.uim.repox.rest.client.xml.DataProviders;
 import eu.europeana.uim.repox.rest.client.xml.DataSources;
 import eu.europeana.uim.repox.rest.client.xml.HarvestingStatus;
 import eu.europeana.uim.repox.rest.client.xml.Provider;
 import eu.europeana.uim.repox.rest.client.xml.RunningTasks;
 import eu.europeana.uim.repox.rest.client.xml.Source;
 import eu.europeana.uim.repox.rest.utils.DummyXmlObjectCreator;
 
 /**
  * Tests repox client functionality.
  * 
  * @author Markus Muhr (markus.muhr@kb.nl)
  * @since Jan 25, 2012
  */
 public class RepoxRestClientTest {
     private static Logger          logger     = Logger.getLogger(RepoxRestClientTest.class.getName());
     private static boolean         logEnabled = true;
 
     private static RepoxRestClient repoxRestClient;
     private static String          timeStamp;
     private static String          uri;
 
     /**
      * Sets up the repox using host in config properties specific to environment.
      * 
      * @throws Exception
      */
     @BeforeClass
     public static void setupRepoxClient() throws Exception {
         uri = RepoxTestUtils.getUri(RepoxRestClientTest.class, "/config.properties");
         repoxRestClient = new RepoxRestClientImpl(uri);
         timeStamp = Long.toString(System.nanoTime());
 
 //        Aggregators aggrs = repoxRestClient.retrieveAggregators();
 //        for (Aggregator aggr : aggrs.getAggregator()) {
 //            if (aggr.getName().contains("_")) {
 //                repoxRestClient.deleteAggregator(aggr.getId());
 //            }
 //        }
 //
 //        DataProviders provs = repoxRestClient.retrieveProviders();
 //        for (Provider prov : provs.getProvider()) {
 //            if (prov.getName().contains("_")) {
 //                repoxRestClient.deleteProvider(prov.getId());
 //            }
 //        }
 //
 //        DataSources sources = repoxRestClient.retrieveDataSources();
 //        for (Source source : sources.getSource()) {
 //            if (source.getId().contains("_")) {
 //                repoxRestClient.deleteDatasource(source.getId());
 //            }
 //        }
     }
 
     /**
      * Retrieve aggregators aka dummy place holder to group providers by country.
      * 
      * @throws Exception
      */
     @Test
     public void testGetAggregators() throws Exception {
         Aggregators aggrs = repoxRestClient.retrieveAggregators();
         for (Aggregator aggr : aggrs.getAggregator()) {
             Assert.assertFalse(aggr.getName().endsWith(timeStamp));
         }
         if (logEnabled) {
             RepoxTestUtils.logMarshalledObject(aggrs, logger);
         }
     }
 
     /**
      * Retrieve providers.
      * 
      * @throws Exception
      */
     @Test
     public void testGetProviders() throws Exception {
         DataProviders provs = repoxRestClient.retrieveProviders();
         for (Provider prov : provs.getProvider()) {
             Assert.assertFalse(prov.getName().endsWith(timeStamp));
         }
         if (logEnabled) {
             RepoxTestUtils.logMarshalledObject(provs, logger);
         }
     }
 
     /**
      * Retrieve data sources aka collections.
      * 
      * @throws Exception
      */
     @Test
     public void testGetDatasources() throws Exception {
         DataSources sources = repoxRestClient.retrieveDataSources();
         for (Source source : sources.getSource()) {
             Assert.assertFalse(source.getName().endsWith(timeStamp));
         }
         if (logEnabled) {
             RepoxTestUtils.logMarshalledObject(sources, logger);
         }
     }
 
     /**
      * Tests aggregator management functionality.
      * 
      * @throws Exception
      */
     @Test
     public void testCreateUpdateDeleteAggregator() throws Exception {
         // Initialize the Aggregator Object
         Aggregator aggr = DummyXmlObjectCreator.createAggregator("aggr_" + timeStamp);
 
         // Create the Aggregator
         Aggregator rtAggr = repoxRestClient.createAggregator(aggr);
         Assert.assertNotNull(rtAggr);
         Assert.assertEquals(aggr.getName(), rtAggr.getName());
         Assert.assertEquals(aggr.getNameCode(), rtAggr.getNameCode());
         Assert.assertEquals(aggr.getUrl(), rtAggr.getUrl());
         if (logEnabled) {
             RepoxTestUtils.logMarshalledObject(rtAggr, logger);
         }
 
         // Update the Aggregator
         rtAggr.setNameCode("77777");
         Aggregator upAggr = repoxRestClient.updateAggregator(rtAggr);
         Assert.assertNotNull(upAggr);
         Assert.assertEquals(rtAggr.getId(), upAggr.getId());
         Assert.assertEquals(rtAggr.getName(), upAggr.getName());
         Assert.assertEquals(rtAggr.getNameCode(), upAggr.getNameCode());
         Assert.assertEquals(rtAggr.getUrl(), upAggr.getUrl());
         if (logEnabled) {
             RepoxTestUtils.logMarshalledObject(upAggr, logger);
         }
 
         // Delete the Aggregator
         String res = repoxRestClient.deleteAggregator(rtAggr.getId());
         Assert.assertNotNull(res);
     }
 
     /**
      * Test the creation, Update & Deletion of a Provider
      * 
      * @throws Exception
      */
     @Test
     public void testCreateUpdateDeleteProvider() throws Exception {
         // Create an Aggregator for testing purposes
         Aggregator aggr = DummyXmlObjectCreator.createAggregator("aggr_" + timeStamp);
         Aggregator rtAggr = repoxRestClient.createAggregator(aggr);
         Assert.assertNotNull(rtAggr);
         if (logEnabled) {
             RepoxTestUtils.logMarshalledObject(rtAggr, logger);
         }
 
         // Create a Provider
         Provider prov = DummyXmlObjectCreator.createProvider("prov_" + timeStamp);
         Provider respprov = repoxRestClient.createProvider(prov, rtAggr);
         Assert.assertNotNull(respprov);
         if (logEnabled) {
             RepoxTestUtils.logMarshalledObject(respprov, logger);
         }
 
         // Update the provider
         respprov.setName("JunitContainerProviderUPD");
 
         Provider upprov = repoxRestClient.updateProvider(respprov);
         Assert.assertNotNull(upprov);
         Assert.assertEquals(respprov.getId(), upprov.getId());
         Assert.assertEquals(respprov.getName(), upprov.getName());
         if (logEnabled) {
             RepoxTestUtils.logMarshalledObject(upprov, logger);
         }
 
         // Delete the Provider
         String res = repoxRestClient.deleteProvider(upprov.getId());
         Assert.assertNotNull(res);
 
         // Delete the Aggregator
         String aggres = repoxRestClient.deleteAggregator(rtAggr.getId());
         Assert.assertNotNull(aggres);
     }
 
     /**
      * Tests a series of OAIPMH functionalities
      * 
      * @throws Exception
      */
     @Test
     public void testCreateUpdateDeleteOAIDataSource() throws Exception {
         try {
             // Create an Aggregator for testing purposes
             Aggregator aggr = DummyXmlObjectCreator.createAggregator("aggr_" + timeStamp);
             Aggregator rtAggr = repoxRestClient.createAggregator(aggr);
             Assert.assertNotNull(rtAggr);
             if (logEnabled) {
                 RepoxTestUtils.logMarshalledObject(rtAggr, logger);
             }
 
             // Create a Provider
             Provider prov = DummyXmlObjectCreator.createProvider("prov_" + timeStamp);
             Provider respprov = repoxRestClient.createProvider(prov, rtAggr);
             Assert.assertNotNull(respprov);
             if (logEnabled) {
                 RepoxTestUtils.logMarshalledObject(respprov, logger);
             }
 
             // Create an OAI PMH String
             Source oaids = DummyXmlObjectCreator.createOAIDataSource("ds_oai_" + timeStamp);
             Source respOaids = repoxRestClient.createDatasourceOAI(oaids, respprov);
             if (logEnabled) {
                 RepoxTestUtils.logMarshalledObject(respOaids, logger);
             }
 
             // Update an OAI PMH String
             respOaids.setMetadataFormat("edm");
             Source updOaids = repoxRestClient.updateDatasourceOAI(respOaids);
             Assert.assertNotNull(updOaids);
             Assert.assertEquals("edm", updOaids.getMetadataFormat());
 
             // Initialize a harvesting session
             String harvestRes = repoxRestClient.initiateHarvesting(updOaids.getId(), true);
             Assert.assertNotNull(harvestRes);
 
             RunningTasks rt = repoxRestClient.getActiveHarvestingSessions();
             Assert.assertNotNull(rt);
             if (logEnabled) {
                 RepoxTestUtils.logMarshalledObject(rt, logger);
             }
 // List<String> dslist = rt.getDataSource();
 // String dsisregistered = null;
 // for (String ds : dslist) {
 // if (ds.equals(updOaids.getId())) {
 // dsisregistered = ds;
 // }
 // }
 // Assert.assertNotNull(dsisregistered);
 
             // Gets the Harvesting Status for the created String
             HarvestingStatus status = repoxRestClient.getHarvestingStatus(updOaids.getId());
             Assert.assertNotNull(status);
             if (logEnabled) {
                 RepoxTestUtils.logMarshalledObject(status, logger);
             }
 
             String cancelled = repoxRestClient.cancelHarvesting(updOaids.getId());
             Assert.assertNotNull(cancelled);
 
             // TODO:Harvest Logs are not generated if harvesting is not complete. How to test this?
             // Log harvestLog = repoxRestClient.getHarvestLog(updOaids.getId());
             // Assert.assertNotNull(harvestLog);
             // if (logEnabled) { RepoxTestUtils.logMarshalledObject(harvestLog,logger);
 
             String deleted = repoxRestClient.deleteDatasource(updOaids.getId());
             Assert.assertNotNull(deleted);
 
             String aggres = repoxRestClient.deleteAggregator(rtAggr.getId());
             Assert.assertNotNull(aggres);
         } catch (Exception ex) {
             Aggregators aggrs = repoxRestClient.retrieveAggregators();
             for (Aggregator aggr : aggrs.getAggregator()) {
                 if (aggr.getName().equals("aggr_" + timeStamp) && aggr.getNameCode().equals("7777")) {
                     repoxRestClient.deleteAggregator(aggr.getId());
                 }
             }
             throw ex;
         }
     }
 
     /**
      * Tests Z3950Timestamp operations
      * 
      * @throws Exception
      */
     @Test
     public void testCreateUpdateDeleteZ3950TimestampDataSource() throws Exception {
         try {
             // Create an Aggregator for testing purposes
             Aggregator aggr = DummyXmlObjectCreator.createAggregator("aggr_" + timeStamp);
             Aggregator rtAggr = repoxRestClient.createAggregator(aggr);
             Assert.assertNotNull(rtAggr);
             if (logEnabled) {
                 RepoxTestUtils.logMarshalledObject(rtAggr, logger);
             }
 
             // Create a Provider
             Provider prov = DummyXmlObjectCreator.createProvider("prov_" + timeStamp);
             Provider respprov = repoxRestClient.createProvider(prov, rtAggr);
             Assert.assertNotNull(respprov);
             if (logEnabled) {
                 RepoxTestUtils.logMarshalledObject(respprov, logger);
             }
 
             // Create an Z3950Timestamp String
             Source z3950TSds = DummyXmlObjectCreator.createZ3950TimestampDataSource("ds_Z3950Timestamp_" +
                                                                                     timeStamp);
             Source respz3950TSds = repoxRestClient.createDatasourceZ3950Timestamp(z3950TSds,
                     respprov);
             if (logEnabled) {
                 RepoxTestUtils.logMarshalledObject(respz3950TSds, logger);
             }
 
             // Update an Z3950Timestamp String
             respz3950TSds.setDescription("altered!");
             Source updz3950TSds = repoxRestClient.updateDatasourceZ3950Timestamp(respz3950TSds);
             Assert.assertNotNull(updz3950TSds);
             Assert.assertEquals("altered!", updz3950TSds.getDescription());
 
             // Initialize a harvesting session
             String harvestRes = repoxRestClient.initiateHarvesting(updz3950TSds.getId(), true);
             Assert.assertNotNull(harvestRes);
 
             RunningTasks rt = repoxRestClient.getActiveHarvestingSessions();
             Assert.assertNotNull(rt);
             if (logEnabled) {
                 RepoxTestUtils.logMarshalledObject(rt, logger);
             }
 // List<String> dslist = rt.getDataSource();
 // String dsisregistered = null;
 // for (String ds : dslist) {
 // if (ds.equals(updz3950TSds.getId())) {
 // dsisregistered = ds;
 // }
 // }
 // Assert.assertNotNull(dsisregistered);
 
             // Gets the Harvesting Status for the created String
             HarvestingStatus status = repoxRestClient.getHarvestingStatus(updz3950TSds.getId());
             Assert.assertNotNull(status);
             if (logEnabled) {
                 RepoxTestUtils.logMarshalledObject(status, logger);
             }
 
             String cancelled = repoxRestClient.cancelHarvesting(updz3950TSds.getId());
             Assert.assertNotNull(cancelled);
 
             // TODO:Harvest Logs are not generated if harvesting is not complete. How to test this?
             // Log harvestLog = repoxRestClient.getHarvestLog(updOaids.getId());
             // Assert.assertNotNull(harvestLog);
             // if (logEnabled) { RepoxTestUtils.logMarshalledObject(harvestLog,logger);
 
             String deleted = repoxRestClient.deleteDatasource(updz3950TSds.getId());
             Assert.assertNotNull(deleted);
 
             String aggres = repoxRestClient.deleteAggregator(rtAggr.getId());
             Assert.assertNotNull(aggres);
         } catch (Exception ex) {
             Aggregators aggrs = repoxRestClient.retrieveAggregators();
             for (Aggregator aggr : aggrs.getAggregator()) {
                 if (aggr.getName().equals("aggr_" + timeStamp) && aggr.getNameCode().equals("7777")) {
                     repoxRestClient.deleteAggregator(aggr.getId());
                 }
             }
             throw ex;
         }
     }
 
     /**
      * Tests Z3950IDFile operations
      * 
      * @throws Exception
      */
 // @Test
     public void testCreateUpdateDeleteZ3950IDFileDataSource() throws Exception {
         try {
             // Create an Aggregator for testing purposes
             Aggregator aggr = DummyXmlObjectCreator.createAggregator("aggr_" + timeStamp);
             Aggregator rtAggr = repoxRestClient.createAggregator(aggr);
             Assert.assertNotNull(rtAggr);
             if (logEnabled) {
                 RepoxTestUtils.logMarshalledObject(rtAggr, logger);
             }
 
             // Create a Provider
             Provider prov = DummyXmlObjectCreator.createProvider("prov_" + timeStamp);
             Provider respprov = repoxRestClient.createProvider(prov, rtAggr);
             Assert.assertNotNull(respprov);
             if (logEnabled) {
                 RepoxTestUtils.logMarshalledObject(respprov, logger);
             }
 
             // Create an Z3950OIdFile String
             Source z3950IDFileds = DummyXmlObjectCreator.createZ3950IdFileDataSource("ds_Z3950IDFile_" +
                                                                                      timeStamp);
             Source respZ3950IDFileds = repoxRestClient.createDatasourceZ3950IdFile(z3950IDFileds,
                     respprov);
             if (logEnabled) {
                 RepoxTestUtils.logMarshalledObject(respZ3950IDFileds, logger);
             }
 
             // Update an Z3950OIdFile String
             respZ3950IDFileds.setDescription("altered");
 
             Source updZ3950IDFileds = repoxRestClient.updateDatasourceZ3950IdFile(respZ3950IDFileds);
             Assert.assertNotNull(updZ3950IDFileds);
             Assert.assertEquals("altered", updZ3950IDFileds.getDescription());
 
             // Initialize a harvesting session
             String harvestRes = repoxRestClient.initiateHarvesting(updZ3950IDFileds.getId(), true);
             Assert.assertNotNull(harvestRes);
 
             RunningTasks rt = repoxRestClient.getActiveHarvestingSessions();
             Assert.assertNotNull(rt);
             if (logEnabled) {
                 RepoxTestUtils.logMarshalledObject(rt, logger);
             }
 // List<String> dslist = rt.getDataSource();
 // String dsisregistered = null;
 // for (String ds : dslist) {
 // if (ds.equals(updZ3950IDFileds.getId())) {
 // dsisregistered = ds;
 // }
 // }
 // Assert.assertNotNull(dsisregistered);
 
             // Gets the Harvesting Status for the created String
             HarvestingStatus status = repoxRestClient.getHarvestingStatus(updZ3950IDFileds.getId());
             Assert.assertNotNull(status);
             if (logEnabled) {
                 RepoxTestUtils.logMarshalledObject(status, logger);
             }
 
             String cancelled = repoxRestClient.cancelHarvesting(updZ3950IDFileds.getId());
             Assert.assertNotNull(cancelled);
 
             // TODO:Harvest Logs are not generated if harvesting is not complete. How to test this?
             // Log harvestLog = repoxRestClient.getHarvestLog(updOaids.getId());
             // Assert.assertNotNull(harvestLog);
             // if (logEnabled) { RepoxTestUtils.logMarshalledObject(harvestLog,logger);
 
             String deleted = repoxRestClient.deleteDatasource(updZ3950IDFileds.getId());
             Assert.assertNotNull(deleted);
 
             String aggres = repoxRestClient.deleteAggregator(rtAggr.getId());
             Assert.assertNotNull(aggres);
         } catch (Exception ex) {
             Aggregators aggrs = repoxRestClient.retrieveAggregators();
             for (Aggregator aggr : aggrs.getAggregator()) {
                 if (aggr.getName().equals("aggr_" + timeStamp) && aggr.getNameCode().equals("7777")) {
                     repoxRestClient.deleteAggregator(aggr.getId());
                 }
             }
             throw ex;
         }
     }
 
     /**
      * Tests Z3950IdSequence operations
      * 
      * @throws Exception
      */
     @Test
     public void testCreateUpdateDeleteZ3950IdSequenceDataSource() throws Exception {
         try {
             // Create an Aggregator for testing purposes
             Aggregator aggr = DummyXmlObjectCreator.createAggregator("aggr_" + timeStamp);
             Aggregator rtAggr = repoxRestClient.createAggregator(aggr);
             Assert.assertNotNull(rtAggr);
             if (logEnabled) {
                 RepoxTestUtils.logMarshalledObject(rtAggr, logger);
             }
 
             // Create a Provider
             Provider prov = DummyXmlObjectCreator.createProvider("prov_" + timeStamp);
             Provider respprov = repoxRestClient.createProvider(prov, rtAggr);
             Assert.assertNotNull(respprov);
             if (logEnabled) {
                 RepoxTestUtils.logMarshalledObject(respprov, logger);
             }
 
             // Create an Z3950OIdSequence String
             // NPE
             Source Z3950IdSeqds = DummyXmlObjectCreator.createZ3950IdSequenceDataSource("ds_Z3950IdSequence_" +
                                                                                         timeStamp);
             Source respZ3950IdSeqds = repoxRestClient.createDatasourceZ3950IdSequence(Z3950IdSeqds,
                     respprov);
             if (logEnabled) {
                 RepoxTestUtils.logMarshalledObject(respZ3950IdSeqds, logger);
             }
 
             // Update an Z3950OIdSequence String
             respZ3950IdSeqds.setDescription("altered");
 
             Source updZ3950IdSeqds = repoxRestClient.updateDatasourceZ3950IdSequence(respZ3950IdSeqds);
             Assert.assertNotNull(updZ3950IdSeqds);
             Assert.assertEquals("altered", updZ3950IdSeqds.getDescription());
 
             // Initialize a harvesting session
             String harvestRes = repoxRestClient.initiateHarvesting(updZ3950IdSeqds.getId(), true);
             Assert.assertNotNull(harvestRes);
 
             RunningTasks rt = repoxRestClient.getActiveHarvestingSessions();
             Assert.assertNotNull(rt);
             if (logEnabled) {
                 RepoxTestUtils.logMarshalledObject(rt, logger);
             }
 // List<String> dslist = rt.getDataSource();
 // String dsisregistered = null;
 // for (String ds : dslist) {
 // if (ds.equals(updZ3950IdSeqds.getId())) {
 // dsisregistered = ds;
 // }
 // }
 // Assert.assertNotNull(dsisregistered);
 
             // Gets the Harvesting Status for the created String
             HarvestingStatus status = repoxRestClient.getHarvestingStatus(updZ3950IdSeqds.getId());
             Assert.assertNotNull(status);
             if (logEnabled) {
                 RepoxTestUtils.logMarshalledObject(status, logger);
             }
 
             String cancelled = repoxRestClient.cancelHarvesting(updZ3950IdSeqds.getId());
             Assert.assertNotNull(cancelled);
 
             // TODO:Harvest Logs are not generated if harvesting is not complete. How to test this?
             // Log harvestLog = repoxRestClient.getHarvestLog(updOaids.getId());
             // Assert.assertNotNull(harvestLog);
             // if (logEnabled) { RepoxTestUtils.logMarshalledObject(harvestLog,logger);
 
             String deleted = repoxRestClient.deleteDatasource(updZ3950IdSeqds.getId());
             Assert.assertNotNull(deleted);
 
             String aggres = repoxRestClient.deleteAggregator(rtAggr.getId());
             Assert.assertNotNull(aggres);
         } catch (Exception ex) {
             Aggregators aggrs = repoxRestClient.retrieveAggregators();
             for (Aggregator aggr : aggrs.getAggregator()) {
                 if (aggr.getName().equals("aggr_" + timeStamp) && aggr.getNameCode().equals("7777")) {
                     repoxRestClient.deleteAggregator(aggr.getId());
                 }
             }
             throw ex;
         }
     }
 
     /**
      * Tests FTP operations
      * 
      * @throws Exception
      */
     @Test
     public void testCreateUpdateDeleteFtpDataSource() throws Exception {
         try {
             // Create an Aggregator for testing purposes
             Aggregator aggr = DummyXmlObjectCreator.createAggregator("aggr_" + timeStamp);
             Aggregator rtAggr = repoxRestClient.createAggregator(aggr);
             Assert.assertNotNull(rtAggr);
             if (logEnabled) {
                 RepoxTestUtils.logMarshalledObject(rtAggr, logger);
             }
 
             // Create a Provider
             Provider prov = DummyXmlObjectCreator.createProvider("prov_" + timeStamp);
             // Order places an important role?
             Provider respprov = repoxRestClient.createProvider(prov, rtAggr);
             Assert.assertNotNull(respprov);
             if (logEnabled) {
                 RepoxTestUtils.logMarshalledObject(respprov, logger);
             }
 
             // Create an FTP String
             Source Ftpds = DummyXmlObjectCreator.createFtpDataSource("ds_ftp_" + timeStamp);
             Source respFtpds = repoxRestClient.createDatasourceFtp(Ftpds, respprov);
             if (logEnabled) {
                 RepoxTestUtils.logMarshalledObject(respFtpds, logger);
             }
 
             // Temporarily placing values here until issue is fixed
 
             // Update an FTP String
             respFtpds.setDescription("altered");
             // TODO:Temporarily disable this until issues are resolved
             // Source updFtpds = repoxRestClient.updateDatasourceFtp(respFtpds);
             Source updFtpds = repoxRestClient.updateDatasourceFtp(respFtpds);
             Assert.assertNotNull(updFtpds);
             // Assert.assertEquals("altered!@#$%",updFtpds.getDescription());
 
             // Initialize a harvesting session
             String harvestRes = repoxRestClient.initiateHarvesting(updFtpds.getId(), true);
             Assert.assertNotNull(harvestRes);
 
             RunningTasks rt = repoxRestClient.getActiveHarvestingSessions();
             Assert.assertNotNull(rt);
             if (logEnabled) {
                 RepoxTestUtils.logMarshalledObject(rt, logger);
             }
 // List<String> dslist = rt.getDataSource();
 // String dsisregistered = null;
 // for (String ds : dslist) {
 // if (ds.equals(updFtpds.getId())) {
 // dsisregistered = ds;
 // }
 // }
 // Assert.assertNotNull(dsisregistered);
 
             // Gets the Harvesting Status for the created String
             HarvestingStatus status = repoxRestClient.getHarvestingStatus(updFtpds.getId());
             Assert.assertNotNull(status);
             if (logEnabled) {
                 RepoxTestUtils.logMarshalledObject(status, logger);
             }
 
             String cancelled = repoxRestClient.cancelHarvesting(updFtpds.getId());
             Assert.assertNotNull(cancelled);
 
             // TODO:Harvest Logs are not generated if harvesting is not complete. How to test this?
             // Log harvestLog = repoxRestClient.getHarvestLog(updOaids.getId());
             // Assert.assertNotNull(harvestLog);
             // if (logEnabled) { RepoxTestUtils.logMarshalledObject(harvestLog,logger);
 
             String deleted = repoxRestClient.deleteDatasource(updFtpds.getId());
             Assert.assertNotNull(deleted);
 
             String aggres = repoxRestClient.deleteAggregator(rtAggr.getId());
             Assert.assertNotNull(aggres);
         } catch (Exception ex) {
             Aggregators aggrs = repoxRestClient.retrieveAggregators();
             for (Aggregator aggr : aggrs.getAggregator()) {
                 if (aggr.getName().equals("aggr_" + timeStamp) && aggr.getNameCode().equals("7777")) {
                     repoxRestClient.deleteAggregator(aggr.getId());
                 }
             }
             throw ex;
         }
     }
 
     /**
      * Tests Http operations
      * 
      * @throws Exception
      */
    @Test
     public void testCreateUpdateDeleteHttpDataSource() throws Exception {
         try {
             // Create an Aggregator for testing purposes
             Aggregator aggr = DummyXmlObjectCreator.createAggregator("aggr_" + timeStamp);
             Aggregator rtAggr = repoxRestClient.createAggregator(aggr);
             Assert.assertNotNull(rtAggr);
             if (logEnabled) {
                 RepoxTestUtils.logMarshalledObject(rtAggr, logger);
             }
 
             // Create a Provider
             Provider prov = DummyXmlObjectCreator.createProvider("prov_" + timeStamp);
             Provider respprov = repoxRestClient.createProvider(prov, rtAggr);
             Assert.assertNotNull(respprov);
             if (logEnabled) {
                 RepoxTestUtils.logMarshalledObject(respprov, logger);
             }
 
             // Create an HTTP String - NPE????
             Source httpds = DummyXmlObjectCreator.createHttpDataSource("ds_http_" + timeStamp);
             Source respHttpds = repoxRestClient.createDatasourceHttp(httpds, respprov);
             if (logEnabled) {
                 RepoxTestUtils.logMarshalledObject(respHttpds, logger);
             }
 
             // Update an HTTP String
             httpds.setDescription("altered");
 
             // TODO:Temporarily disable this until issues are resolved
             // Source updHttpds = repoxRestClient.updateDatasourceHttp(respHttpds);
             Source updHttpds = repoxRestClient.updateDatasourceHttp(httpds);
 
             Assert.assertNotNull(updHttpds);
             // Assert.assertEquals("altered!@#$%",updHttpds.getDescription());
 
             // Initialize a harvesting session
             String harvestRes = repoxRestClient.initiateHarvesting(updHttpds.getId(), true);
             Assert.assertNotNull(harvestRes);
 
             RunningTasks rt = repoxRestClient.getActiveHarvestingSessions();
             Assert.assertNotNull(rt);
             if (logEnabled) {
                 RepoxTestUtils.logMarshalledObject(rt, logger);
             }
 // List<String> dslist = rt.getDataSource();
 // String dsisregistered = null;
 // for (String ds : dslist) {
 // if (ds.equals(updHttpds.getId())) {
 // dsisregistered = ds;
 // }
 // }
 // Assert.assertNotNull(dsisregistered);
 
             // Gets the Harvesting Status for the created String
             HarvestingStatus status = repoxRestClient.getHarvestingStatus(updHttpds.getId());
             Assert.assertNotNull(status);
             if (logEnabled) {
                 RepoxTestUtils.logMarshalledObject(status, logger);
             }
 
             String cancelled = repoxRestClient.cancelHarvesting(updHttpds.getId());
             Assert.assertNotNull(cancelled);
 
             // TODO:Harvest Logs are not generated if harvesting is not complete. How to test this?
             // Log harvestLog = repoxRestClient.getHarvestLog(updOaids.getId());
             // Assert.assertNotNull(harvestLog);
             // if (logEnabled) { RepoxTestUtils.logMarshalledObject(harvestLog,logger);
 
             String deleted = repoxRestClient.deleteDatasource(updHttpds.getId());
             Assert.assertNotNull(deleted);
 
             String aggres = repoxRestClient.deleteAggregator(rtAggr.getId());
             Assert.assertNotNull(aggres);
         } catch (Exception ex) {
             Aggregators aggrs = repoxRestClient.retrieveAggregators();
             for (Aggregator aggr : aggrs.getAggregator()) {
                 if (aggr.getName().equals("aggr_" + timeStamp) && aggr.getNameCode().equals("7777")) {
                     repoxRestClient.deleteAggregator(aggr.getId());
                 }
             }
             throw ex;
         }
     }
 
     /**
      * Tests Folder operations
      * 
      * @throws Exception
      */
     @Test
     public void testCreateUpdateDeleteFolderDataSource() throws Exception {
         try {
             // Create an Aggregator for testing purposes
             Aggregator aggr = DummyXmlObjectCreator.createAggregator("aggr_" + timeStamp);
             Aggregator rtAggr = repoxRestClient.createAggregator(aggr);
             Assert.assertNotNull(rtAggr);
             if (logEnabled) {
                 RepoxTestUtils.logMarshalledObject(rtAggr, logger);
             }
 
             // Create a Provider
             Provider prov = DummyXmlObjectCreator.createProvider("prov_" + timeStamp);
             // Order plays an important role????
             Provider respprov = repoxRestClient.createProvider(prov, rtAggr);
             Assert.assertNotNull(respprov);
             if (logEnabled) {
                 RepoxTestUtils.logMarshalledObject(respprov, logger);
             }
 
             // Create an Folder String
             Source folderds = DummyXmlObjectCreator.createFolderDataSource("ds_folder_" + timeStamp);
             Source respfolderds = repoxRestClient.createDatasourceFolder(folderds, respprov);
             if (logEnabled) {
                 RepoxTestUtils.logMarshalledObject(respfolderds, logger);
             }
 
             // Update an Folder String
             respfolderds.setDescription("altered");
 
             // TODO:Temporarily disable this until issues are resolved
             // Source updfolderds = repoxRestClient.updateDatasourceFolder(respfolderds);
             Source updfolderds = repoxRestClient.updateDatasourceFolder(respfolderds);
             Assert.assertNotNull(updfolderds);
             // Assert.assertEquals("altered!@#$%",updfolderds.getDescription());
 
             // Initialize a harvesting session
             String harvestRes = repoxRestClient.initiateHarvesting(updfolderds.getId(), true);
             Assert.assertNotNull(harvestRes);
 
             RunningTasks rt = repoxRestClient.getActiveHarvestingSessions();
             Assert.assertNotNull(rt);
             if (logEnabled) {
                 RepoxTestUtils.logMarshalledObject(rt, logger);
             }
 // List<String> dslist = rt.getDataSource();
 // String dsisregistered = null;
 // for (String ds : dslist) {
 // if (ds.equals(updfolderds.getId())) {
 // dsisregistered = ds;
 // }
 // }
 // Assert.assertNotNull(dsisregistered);
 
             // Gets the Harvesting Status for the created String
             HarvestingStatus status = repoxRestClient.getHarvestingStatus(updfolderds.getId());
             Assert.assertNotNull(status);
             if (logEnabled) {
                 RepoxTestUtils.logMarshalledObject(status, logger);
             }
 
             String cancelled = repoxRestClient.cancelHarvesting(updfolderds.getId());
             Assert.assertNotNull(cancelled);
 
             // TODO:Harvest Logs are not generated if harvesting is not complete. How to test this?
             // Log harvestLog = repoxRestClient.getHarvestLog(updOaids.getId());
             // Assert.assertNotNull(harvestLog);
             // if (logEnabled) { RepoxTestUtils.logMarshalledObject(harvestLog,logger);
 
             String deleted = repoxRestClient.deleteDatasource(updfolderds.getId());
             Assert.assertNotNull(deleted);
 
             String aggres = repoxRestClient.deleteAggregator(rtAggr.getId());
             Assert.assertNotNull(aggres);
         } catch (Exception ex) {
             Aggregators aggrs = repoxRestClient.retrieveAggregators();
             for (Aggregator aggr : aggrs.getAggregator()) {
                 if (aggr.getName().equals("aggr_" + timeStamp) && aggr.getNameCode().equals("7777")) {
                     repoxRestClient.deleteAggregator(aggr.getId());
                 }
             }
             throw ex;
         }
     }
 }
