 package com.test.logging.logback;
 
 import org.junit.After;
 import org.junit.Before;
import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ch.qos.logback.classic.LoggerContext;
 
 import com.test.logging.common.LoggingTest;
 import com.test.logging.common.ProfilerOptions;
 import com.test.logging.common.TestFactoryType;
 
 /** Logback logger test */
 public class LogbackTest {
 
     /** Profiler options */
     private static final ProfilerOptions opts = new ProfilerOptions("Logback Profiler");
 
     /** Different types of tests */
     private static final TestFactoryType[] allTypes = TestFactory.getSupportedTypes();
 
     @BeforeClass
     public static void setUpBeforeClass() throws Exception {
     }
 
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        // Stopping the logger context to force flushing the log events
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        loggerContext.stop();
    }

     @Before
     public void setUp() throws Exception {
     }
 
     @After
     public void tearDown() throws Exception {
     }
 
     @Test
     public void testSyncFileLogger() {
         // Synchronous file logger
         System.out.println("(((((((((((((   SYNC FILE LOGGER   ))))))))))))))))");
         Logger logger = LoggerFactory.getLogger("SyncFileLogger");
         // Iterate through all unit work types and execute test scenarios
         for (TestFactoryType t : allTypes) {
             new LoggingTest<Logger>(t.toString() + "-Sync", new TestFactory(t, logger), opts).run();
         }
     }
 
     @Test
     public void testAsyncFileLogger() {
         // Asynchronous file logger
         System.out.println("(((((((((((((   ASYNC FILE LOGGER   ))))))))))))))))");
         Logger asyncLogger = LoggerFactory.getLogger("AsyncFileLogger");
         // Iterate through all unit work types and execute test scenarios
         for (TestFactoryType t : allTypes) {
             new LoggingTest<Logger>(t.toString() + "-Async", new TestFactory(t, asyncLogger), opts).run();
         }
         // Wait for 20 seconds for I/O to catch up and closing of async appenders
         System.out.println("Waiting for 20 seconds for I/O to catch up and proper closing of async appenders.");
         try {
             Thread.sleep(20000);
         } catch (InterruptedException ie) {
             System.err.println("Interrupted while waiting for I/O to catch up.");
         }
     }
 
 }
