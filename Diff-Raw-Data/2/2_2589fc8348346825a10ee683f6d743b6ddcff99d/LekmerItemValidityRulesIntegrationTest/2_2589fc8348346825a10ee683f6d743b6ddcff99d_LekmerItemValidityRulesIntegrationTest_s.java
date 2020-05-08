 package com.abudko.scheduled.rules.lekmer;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import com.abudko.scheduled.rules.ItemValidityRules;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = { "classpath:/spring/test-app-config.xml" })
 public class LekmerItemValidityRulesIntegrationTest {
     
     @Autowired
     @Qualifier("lekmerItemValidityRules")
     private ItemValidityRules rules;
     
     @Test
     public void testIdNotLekmer() {
         assertTrue(rules.isValid("45264242"));;
     }
     
     @Test
     public void testLekmerIdWithSpacesValid() {
        assertTrue(rules.isValid("LE502316 LIGHT LU"));
     }
     
     @Test
     public void testIdLekmerInvalid() {
         assertFalse(rules.isValid("LEsds"));
     }
     
     @Test
     public void testIdNull() {
         assertTrue(rules.isValid(null));;
     }
 }
