 package com.jclarity.had_one_dismissal.web;
 
 import static org.junit.Assert.assertEquals;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.jclarity.had_one_dismissal.domain.Applicant;
 import com.jclarity.had_one_dismissal.domain.Company;
 import com.jclarity.had_one_dismissal.domain.Location;
 import com.jclarity.had_one_dismissal.domain.Tag;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = { "classpath:/META-INF/spring/applicationContext.xml", "file:src/test/resources/webmvc-test-config.xml" })
 public class PopulateTest {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(PopulateTest.class);
 
     @Transactional
     @Test
     public void loadsDataFromFiles() throws Exception {
         LOGGER.info("Populate Test");
         long total = System.currentTimeMillis();
         long time = total;
         Populate.loadLocations();
         time = updateStopwatch(time, "loadLocations");
         Populate.loadTags();
         time = updateStopwatch(time, "loadTags");
         Populate.loadNames();
         time = updateStopwatch(time, "loadNames");
         Populate.loadCompanies();
         time = updateStopwatch(time, "loadCompanies");
         Populate.loadJobListings();
         updateStopwatch(time, "loadJobListings");
 
         total = System.currentTimeMillis() - total;
         LOGGER.info("TOTAL: {}", total);
 
         assertEquals(272L, Location.countLocations());
         assertEquals(658L, Tag.countTags());
         assertEquals(10000, Applicant.countApplicants());
         assertEquals(2155, Company.countCompanys());
     }
 
     private long updateStopwatch(long time, String name) {
         time = System.currentTimeMillis() - time;
         LOGGER.info("Time in {}: {}", name, time);
         return System.currentTimeMillis();
     }
 
 }
