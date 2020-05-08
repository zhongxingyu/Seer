 package org.motechproject.ananya.kilkari.subscription.repository;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ActiveProfiles;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import static org.junit.Assert.assertEquals;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration("classpath:applicationKilkariSubscriptionContext.xml")
 @ActiveProfiles("test")
 public class KilkariPropertiesDataIT {
 
     @Autowired
     private KilkariPropertiesData kilkariProperties;
 
     @Test
     public void shouldBeAbleToLoadThePropertiesFromTheKilkariPropertiesFile() {
         assertEquals(0, kilkariProperties.getCampaignScheduleDeltaDays());
         assertEquals(1, kilkariProperties.getCampaignScheduleDeltaMinutes());
         assertEquals(0, kilkariProperties.getBufferDaysToAllowRenewalForDeactivation());
        assertEquals("BIHAR", kilkariProperties.getDefaultState());
     }
 }
