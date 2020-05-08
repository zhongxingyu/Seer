 package com.solidstategroup.radar.test.service;
 
 import com.solidstategroup.radar.service.EmailManager;
 import com.solidstategroup.radar.test.DatabaseBackedTest;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 
 import java.util.HashMap;
 
 import static org.junit.Assert.*;
 
 @RunWith(org.springframework.test.context.junit4.SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {"classpath:context.xml"})
 public class EmailManagerTest extends DatabaseBackedTest {
 
     @Autowired
     private EmailManager emailManager;
 
     @Test
     public void testSendEmail() throws Exception {
         emailManager.sendEmail("abul@solidstategroup.com", new String[]{"abul@solidstategroup.com"},
                new String[]{"test bcc"}, "Test subject","Test Body");
     }
 
     @Test
     public void testRender() throws Exception {
       String text = emailManager.renderTemplate(new HashMap<String, Object>(), "patient-registration.vm");
       assertTrue(text != null && !text.isEmpty());
     }
 }
