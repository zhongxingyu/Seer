 package org.motechproject.care.reporting.ft;
 
 import org.junit.runner.RunWith;
 import org.motechproject.care.reporting.ft.couch.MRSDatabase;
 import org.motechproject.care.reporting.ft.reporting.ReportingDatabase;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 @RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationCareReportingFunctionTests.xml")
 public abstract class BaseTestCase {
 
     @Autowired
     private ReportingDatabase reportingDatabase;
 
     @Autowired
     private MRSDatabase mrsDatabase;
 
     public ReportingDatabase reportingDatabase() {
         return reportingDatabase;
     }
 
     public MRSDatabase mrsDatabase() {
         return mrsDatabase;
     }
 
     protected String constructRequestTemplateUrl(String template) {
         return String.format("%s/%s/%s.st", "requests", getTestIdentifier(), template);
     }
 
     protected String constructExpectedUrl(String expected) {
         return String.format("%s/%s/%s.properties", "expected", getTestIdentifier(), expected);
     }
 
     protected abstract  String getTestIdentifier();
 }
