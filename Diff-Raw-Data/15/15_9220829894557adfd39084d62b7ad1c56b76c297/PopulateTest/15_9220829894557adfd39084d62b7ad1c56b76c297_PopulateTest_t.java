 package com.jclarity.had_one_dismissal.web;
 
 import static org.junit.Assert.assertEquals;
 
 import java.lang.reflect.Method;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import com.jclarity.had_one_dismissal.domain.Applicant;
 import com.jclarity.had_one_dismissal.domain.Company;
 import com.jclarity.had_one_dismissal.domain.Location;
 import com.jclarity.had_one_dismissal.domain.Tag;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = { "classpath:/META-INF/spring/applicationContext.xml", "file:src/test/resources/webmvc-test-config.xml" })
 public class PopulateTest {
 
     private static final int STRIDE = 500;
     private static final int APPLICANT_COUNT = 10000;
 
     private static final Logger LOGGER = LoggerFactory.getLogger(PopulateTest.class);
 
     @Autowired private Populate populate;
 
     @Test
     public void loadsDataFromFiles() throws Exception {
        int poolSize = Runtime.getRuntime().availableProcessors();
         ExecutorService executor = Executors.newFixedThreadPool(poolSize);
         //        The following could be run in a while loop for microbenchmarking purposes
         LOGGER.info("Populate Test");
         long total = System.currentTimeMillis();
         long time = total;
         populate.deleteAll();
         time = updateStopwatch(time, "deleteAll");
 
         int blockCount = 2 + (APPLICANT_COUNT / STRIDE);
         final CountDownLatch latch = new CountDownLatch(blockCount);
         executor.execute(runnableOf(latch, "loadLocations"));
         executor.execute(runnableOf(latch, "loadTags"));
         for (int i = 0; i < APPLICANT_COUNT; i += STRIDE) {
             executor.execute(runnableOf(latch, Populate.class.getMethod("loadApplicants", Integer.TYPE, Integer.TYPE), i, i + STRIDE));
         }
         latch.await();
 
         time = System.currentTimeMillis();
         populate.loadCompanies();
         time = updateStopwatch(time, "loadCompanies");
         populate.loadJobListings();
         updateStopwatch(time, "loadJobListings");
 
         total = System.currentTimeMillis() - total;
         LOGGER.info("TOTAL: {}", total);
 
         assertEquals(272L, Location.countLocations());
         assertEquals(658L, Tag.countTags());
         assertEquals(APPLICANT_COUNT, Applicant.countApplicants());
         assertEquals(2155, Company.countCompanys());
     }
 
     private Runnable runnableOf(final CountDownLatch latch, final String methodName, final Object ... args) throws SecurityException, NoSuchMethodException {
         return runnableOf(latch, Populate.class.getMethod(methodName), args);
     }
 
     private Runnable runnableOf(final CountDownLatch latch, final Method method, final Object ... args) {
         return new Runnable() {
             @Override
             public void run() {
                 long time = System.currentTimeMillis();
                 try {
                     method.invoke(populate, args);
                     updateStopwatch(time, method.getName());
                     latch.countDown();
                 } catch (Exception e) {
                     LOGGER.error(e.getMessage(), e);
                 }
             }
         };
     }
 
     private long updateStopwatch(long time, String name) {
         time = System.currentTimeMillis() - time;
         LOGGER.info("Time in {}: {}", name, time);
         return System.currentTimeMillis();
     }
 
 }
