 package org.motechproject.ananya.kilkari.smoke;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.motechproject.ananya.kilkari.smoke.domain.BaseResponse;
 import org.motechproject.ananya.kilkari.smoke.domain.SubscriberResponse;
 import org.motechproject.ananya.kilkari.smoke.domain.SubscriptionDetails;
 import org.motechproject.ananya.kilkari.smoke.domain.SubscriptionRequest;
 import org.motechproject.ananya.kilkari.smoke.domain.builder.SubscriptionRequestBuilder;
 import org.motechproject.ananya.kilkari.smoke.service.SubscriptionService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.web.client.RestTemplate;
 
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.Map;
 
 import static junit.framework.Assert.assertEquals;
 import static org.motechproject.ananya.kilkari.smoke.utils.TestUtils.constructUrl;
 import static org.motechproject.ananya.kilkari.smoke.utils.TestUtils.fromJsonWithResponse;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration("classpath:applicationKilkariSmokeContext.xml")
 public class CallCenterSmokeTest extends BaseSmokeTest {
     @Autowired
     private SubscriptionService subscriptionService;
     private RestTemplate restTemplate;
 
     @Before
     public void setUp() throws SQLException {
         restTemplate = new RestTemplate();
     }
 
     @Test
     public void shouldPostHttpRequestAndVerifyEntriesInReportDbAndCouchDb() throws InterruptedException {
         String expectedStatus = "Pending Subscription";
        SubscriptionRequest subscriptionRequest = new SubscriptionRequestBuilder().withDefaults().withEDD(null).withDOB(null).build();
         Map<String, String> parametersMap = new HashMap<>();
         String channel = "CONTACT_CENTER";
         parametersMap.put("channel", channel);
 
         String responseEntity = restTemplate.postForObject(constructUrl(baseUrl(), "subscription", parametersMap), subscriptionRequest, String.class);
 
         BaseResponse baseResponse = fromJsonWithResponse(responseEntity, BaseResponse.class);
         assertEquals("SUCCESS", baseResponse.getStatus());
 
         SubscriberResponse response = subscriptionService.getSubscriptionData(subscriptionRequest.getMsisdn(), channel, expectedStatus);
         assertEquals(1, response.getSubscriptionDetails().size());
         assertKilkariData(subscriptionRequest.getPack(), expectedStatus, response);
     }
 
     private void assertKilkariData(String pack, String expectedStatus, SubscriberResponse response) {
         SubscriptionDetails subscriptionDetails = response.getSubscriptionDetails().get(0);
         assertEquals(pack.toUpperCase(), subscriptionDetails.getPack().toUpperCase());
         assertEquals(expectedStatus, subscriptionDetails.getStatus());
     }
 }
