 package org.hackystat.utilities.logger;
 
 import static org.junit.Assert.assertEquals;
 import java.util.logging.Logger;
 import org.junit.Test;
 
 /**
  * Tests the HackystatLogger class.
  * 
  * @author Philip Johnson
  */
 
 public class TestHackystatLogger {
 
   /**
    * Tests the logger. Instantiates the logger and writes a test message.
    * 
    */
   @Test
   public void testLogging() {
     Logger logger = HackystatLogger.getLogger("org.hackystat.utilities.testlogger");
    logger.fine("(Test message)");
    logger = HackystatLogger.getLogger("org.hackystat.nestedlogger.test", "testlogging");
    HackystatLogger.setLoggingLevel(logger, "FINE");
    logger.fine("(Test message2)");
    assertEquals("Checking identity", "org.hackystat.nestedlogger.test", logger.getName());
   }
 }
