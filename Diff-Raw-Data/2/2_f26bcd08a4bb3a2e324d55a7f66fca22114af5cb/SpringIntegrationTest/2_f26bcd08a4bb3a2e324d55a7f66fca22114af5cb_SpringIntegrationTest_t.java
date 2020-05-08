 package org.motechproject.care.reporting.repository;
 
 import org.junit.Before;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.transaction.annotation.Transactional;
 
 import java.util.ArrayList;
 import java.util.List;
 
 @RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:META-INF/motech/applicationCareReportingTest.xml")
 @Transactional
 public abstract class SpringIntegrationTest {
 
     @Autowired
     @Qualifier("testDataAccessTemplate")
     protected TestDataAccessTemplate template;
 
     @Before
     public void setUp() {
         template.setAlwaysUseNewSession(false);
     }
 
     private List<Object> toDelete = new ArrayList<Object>();
 
     protected void markForDeletion(Object entity) {
         toDelete.add(entity);
     }
 
     protected void tearDown() {
         template.deleteAll(toDelete);
     }
 }
