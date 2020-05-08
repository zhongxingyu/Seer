 package com.nxj;
 
 import com.nxj.application.Logging;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 /**
  *
  * @author Felix
  */
 public class LoggingTest {
 
     public LoggingTest() {
     }
 
     @BeforeClass
     public static void setUpClass() throws Exception {
     }
 
     @AfterClass
     public static void tearDownClass() throws Exception {
     }
 
     @Before
     public void setUp() {
     }
 
     @After
     public void tearDown() {
     }
 
     @Test
     public void fileLogger1() {
         Logging logging = new Logging();
         logging.enableFileLogger("com.nxj", Level.ALL, "./muj.logger");
         invokeLogs();
        logging.flush();
     }
 
     @Test
     public void consoleLogger1() {
         Logging logging = new Logging();
         logging.enableConsoleLogger("com.nxj", Level.ALL);
         invokeLogs();
        logging.flush();
     }
 
     private void invokeLogs() {
         Logger logger = Logger.getLogger("com.nxj");
         logger.severe("Sever log");
         logger.warning("Warning log");
         logger.info("Info log");
         logger.config("Config log");
         logger.fine("Fine log");
         logger.finer("Finer log");
         logger.finest("Finest log");
     }
 }
