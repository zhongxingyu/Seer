 package cs.dsn.dwc;
 
 import cs.dsn.dwc.client.CountServerConnectionTest;
 import cs.dsn.dwc.logger.Logger;
 import cs.dsn.dwc.server.CountServerTest;
 import cs.dsn.dwc.swp.server.SimpleWorkProtocolTest;
 import org.junit.BeforeClass;
 import org.junit.runner.RunWith;
 import org.junit.runners.Suite;
 
 /**
  * Project test suite
  */
 @RunWith(Suite.class)
 @Suite.SuiteClasses({
         // client
         CountServerConnectionTest.class,
 
         // server
         CountServerTest.class,
 
         // swp.server
         SimpleWorkProtocolTest.class,
 
         // word counter
         WordCounterTest.class,
         WordCountMapTest.class
 })
 public class DwcTestSuite {
   @BeforeClass
   public static void setUp() {
     Logger.setVerbose(false);
   }
 }
