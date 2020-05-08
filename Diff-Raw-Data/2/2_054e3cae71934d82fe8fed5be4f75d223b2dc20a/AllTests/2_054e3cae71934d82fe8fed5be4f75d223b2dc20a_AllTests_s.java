 import junit.framework.Test;
 import junit.framework.TestSuite;
 
 public class AllTests extends TestSuite {
 
   // @SuppressWarnings("unchecked")
     public static Test suite() throws Exception {
 		TestSuite suite = new TestSuite(AllTests.class.getName());
 		//$JUnit-BEGIN$
         suite.addTestSuite(shuttleatwork.actions.ActionsTest.class);
         suite.addTestSuite(shuttleatwork.system.JSonTest.class);
         suite.addTestSuite(shuttleatwork.server.ScriptServiceTest.class);
         suite.addTestSuite(shuttleatwork.model.FacadeTest.class);
         suite.addTestSuite(gtfs.graph.GraphTest.class);
         suite.addTestSuite(gtfs.reader.FeedReaderTest.class);
         suite.addTestSuite(utils.csv.ReaderTest.class);
         suite.addTestSuite(utils.geo.PositionTest.class);
         suite.addTestSuite(utils.geo.AreaTest.class);
         suite.addTestSuite(utils.geo.GeosetTest.class);
         //$JUnit-END$
 		return suite;
 	}
 
 }
