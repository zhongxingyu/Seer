 package it.geosolutions.geobatch.figis.intersection.test;
 
 import java.io.File;
 import java.util.EventObject;
 import java.util.Queue;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import it.geosolutions.figis.model.Config;
 import it.geosolutions.figis.model.Intersection;
 import it.geosolutions.figis.model.Intersection.Status;
 import it.geosolutions.figis.requester.requester.dao.IEConfigDAO;
 import it.geosolutions.figis.requester.requester.util.IEConfigUtils;
 import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
 import it.geosolutions.filesystemmonitor.monitor.FileSystemEventType;
 import it.geosolutions.geobatch.figis.intersection.IntersectionAction;
 import it.geosolutions.geobatch.figis.intersection.IntersectionConfiguration;
 import it.geosolutions.geobatch.figis.intersection.OracleDataStoreManager;
 import it.geosolutions.geobatch.figis.intersection.test.utils.TestingIEConfigDAOImpl;
 
 import junit.framework.TestCase;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 public class TestIntersectionAction extends TestCase
 {
 
     private static final Logger log = LoggerFactory.getLogger(TestIntersectionAction.class);
 
     Queue<EventObject> queue;
 
     private IntersectionAction intersectionAction = null;
     private Config config = null;
 
     @Before
     public void setUp() throws Exception
     {
         File inputFile = loadXMLConfig(null);
 
         queue = new LinkedBlockingQueue<EventObject>();
         queue.add(new FileSystemEvent(inputFile, FileSystemEventType.FILE_ADDED));
 
        config = IEConfigUtils.parseXMLConfig(loadXMLConfig("ie-config.xml").getAbsolutePath());

         IntersectionConfiguration cronConfiguration = new IntersectionConfiguration("id", "name", " description");
         cronConfiguration.setPersistencyHost("http://localhost:8181");
         cronConfiguration.setItemsPerPages(50);
         cronConfiguration.setIeServiceUsername("admin");
         cronConfiguration.setIeServicePassword("admin");
 
         intersectionAction = new IntersectionAction(cronConfiguration);
     }
 
     /**
      * @param configName
      * @return
      * @throws Exception
      */
     private File loadXMLConfig(String configName) throws Exception
     {
         configName = ((configName == null) || configName.isEmpty()) ? "ie-config.xml" : configName;
 
         File inputFile = null;
         try
         {
             inputFile = new File(TestIntersectionAction.class.getResource(configName).toURI());
         }
         catch (Exception e)
         {
             log.error(e.getLocalizedMessage(), e);
             throw e;
         }
 
         return inputFile;
     }
 
     @Test
     public void testDBOracleConnection()
     {
         try
         {
             OracleDataStoreManager dataStore = new OracleDataStoreManager(
                     config.getGlobal().getDb().getHost(),
                     Integer.parseInt(config.getGlobal().getDb().getPort()),
                     config.getGlobal().getDb().getDatabase(),
                     config.getGlobal().getDb().getSchema(),
                     config.getGlobal().getDb().getUser(),
                     config.getGlobal().getDb().getPassword());
             assertNotNull(dataStore);
         }
         catch (Exception e)
         {
             log.error(e.getMessage(), e);
             assertTrue(e.getMessage(), false);
         }
 
     }
 
     @Test
     public void test1_IntersectionsComputation() throws Exception
     {
         config = IEConfigUtils.parseXMLConfig(loadXMLConfig("ie-config.xml").getAbsolutePath());
 
         IEConfigDAO ieConfigDAO = new TestingIEConfigDAOImpl(config);
 
         intersectionAction.setIeConfigDAO(ieConfigDAO);
         intersectionAction.execute(queue);
 
         for (Intersection intersection : config.intersections)
         {
             assertTrue(intersection.getStatus().equals(Status.COMPUTED));
         }
     }
 
     @Test
     public void test2_IntersectionReomputationWithForce() throws Exception
     {
         config = IEConfigUtils.parseXMLConfig(loadXMLConfig("ie-config-force.xml").getAbsolutePath());
 
         IEConfigDAO ieConfigDAO = new TestingIEConfigDAOImpl(config);
 
         intersectionAction.setIeConfigDAO(ieConfigDAO);
         intersectionAction.execute(queue);
 
         for (Intersection intersection : config.intersections)
         {
             assertTrue(intersection.getStatus().equals(Status.COMPUTED));
         }
     }
 
     @Test
     public void test3_IntersectionDeletion() throws Exception
     {
         config = IEConfigUtils.parseXMLConfig(loadXMLConfig("ie-config-delete.xml").getAbsolutePath());
 
         IEConfigDAO ieConfigDAO = new TestingIEConfigDAOImpl(config);
 
         intersectionAction.setIeConfigDAO(ieConfigDAO);
         intersectionAction.execute(queue);
 
         assertTrue(config.intersections.size() == 0);
     }
 
 }
