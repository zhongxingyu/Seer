 package org.motechproject.care;
 
 import org.ektorp.CouchDbConnector;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.runner.RunWith;
 import org.motechproject.model.MotechBaseDataObject;
 import org.motechproject.scheduletracking.api.service.ScheduleTrackingService;
 import org.quartz.utils.Pair;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import java.util.ArrayList;
 import java.util.Properties;
 
 @RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath*:applicationContext-FunctionalTests.xml")
 public abstract class SpringIntegrationTest {
 
     @Qualifier("ananyaCareDbConnector")
     @Autowired
     protected CouchDbConnector ananyaCareDbConnector;
 
 
     @Qualifier("ananyaCareProperties")
     @Autowired
     protected Properties ananyaCareProperties;
 
     @Autowired
     protected ScheduleTrackingService trackingService;
 
     protected ArrayList<MotechBaseDataObject> toDelete;
     protected ArrayList<Pair> schedulesToDelete;
 
     @Before
     public void before() {
         toDelete = new ArrayList<MotechBaseDataObject>();
         schedulesToDelete = new ArrayList<Pair>();
     }
 
     @After
     public void after() {
         for(MotechBaseDataObject obj : toDelete){
             ananyaCareDbConnector.delete(obj);
         }
         for(int i=0 ;i< schedulesToDelete.size(); i++){
             Pair s = schedulesToDelete.get(i);
             String externalId = s.getFirst().toString();
             String scheduleName = s.getSecond().toString();
             ArrayList<String> scheduleNames = new ArrayList<String>();
             scheduleNames.add(scheduleName);
             trackingService.unenroll(externalId, scheduleNames);
         }
     }
 
 
     protected void markForDeletion(MotechBaseDataObject document) {
         toDelete.add(document);
     }
 
     protected void markScheduleForUnEnrollment(String externalId, String scheduleName) {
         schedulesToDelete.add(new Pair(externalId, scheduleName));
     }
 
     private String getAppServerPort() {
         return ananyaCareProperties.getProperty("app.server.port");
     }
 
     private String getAppServerHost() {
         return ananyaCareProperties.getProperty("app.server.host");
     }
 
     protected String getAppServerUrl() {
         return "http://" + getAppServerHost() + ":" + getAppServerPort()+"/ananya-care/care/process";
     }
 }
