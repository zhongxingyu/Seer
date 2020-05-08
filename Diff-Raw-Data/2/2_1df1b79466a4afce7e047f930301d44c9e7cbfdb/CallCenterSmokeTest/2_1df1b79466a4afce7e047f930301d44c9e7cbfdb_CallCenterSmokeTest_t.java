 package org.motechproject.ananya.kilkari.smoke;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.motechproject.ananya.kilkari.smoke.domain.kilkari.BaseResponse;
 import org.motechproject.ananya.kilkari.smoke.domain.kilkari.SubscriberResponse;
 import org.motechproject.ananya.kilkari.smoke.domain.kilkari.SubscriptionDetails;
 import org.motechproject.ananya.kilkari.smoke.domain.kilkari.SubscriptionRequest;
 import org.motechproject.ananya.kilkari.smoke.service.SubscriptionService;
 import org.motechproject.ananya.kilkari.smoke.utils.SubscriptionRequestBuilder;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.web.client.RestTemplate;
 
 import java.sql.SQLException;
 
 import static junit.framework.Assert.assertEquals;
 import static org.motechproject.ananya.kilkari.smoke.utils.TestUtils.KILKARI_SUBSCRIPTION_POST_URL;
 import static org.motechproject.ananya.kilkari.smoke.utils.TestUtils.fromJsonWithResponse;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration("classpath:applicationKilkariSmokeContext.xml")
 public class CallCenterSmokeTest {
     RestTemplate restTemplate;
     SubscriptionService subscriptionService;
 
     @Before
     public void setUp() throws SQLException {
         subscriptionService = new SubscriptionService();
         restTemplate = new RestTemplate();
     }
 
     @Test
     public void shouldPostHttpRequestAndVerifyEntriesInReportDbAndCouchDb() throws InterruptedException {
         String expectedStatus = "PENDING_ACTIVATION";
        SubscriptionRequest subscriptionRequest = new SubscriptionRequestBuilder().withDefaults().withEDD(null).withDOB(null).withLocation(null).build();
 
         String responseEntity = restTemplate.postForObject(KILKARI_SUBSCRIPTION_POST_URL, subscriptionRequest, String.class);
 
         BaseResponse baseResponse = fromJsonWithResponse(responseEntity, BaseResponse.class);
         assertEquals("SUCCESS", baseResponse.getStatus());
 
         SubscriberResponse response = subscriptionService.getSubscriptionData(subscriptionRequest.getMsisdn(), subscriptionRequest.getChannel(), expectedStatus);
         assertEquals(1, response.getSubscriptionDetails().size());
         assertKilkariData(subscriptionRequest.getPack(), expectedStatus, response);
     }
 
     private void assertKilkariData(String pack, String expectedStatus, SubscriberResponse response) {
         SubscriptionDetails subscriptionDetails = response.getSubscriptionDetails().get(0);
         assertEquals(pack, subscriptionDetails.getPack());
         assertEquals(expectedStatus, subscriptionDetails.getStatus());
     }
 }
