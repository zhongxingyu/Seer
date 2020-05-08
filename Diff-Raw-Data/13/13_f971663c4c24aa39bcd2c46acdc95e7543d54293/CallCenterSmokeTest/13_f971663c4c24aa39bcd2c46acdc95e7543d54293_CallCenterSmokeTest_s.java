 package org.motechproject.ananya.kilkari.smoke;
 
import org.joda.time.DateTime;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.motechproject.ananya.kilkari.smoke.domain.kilkari.BaseResponse;
 import org.motechproject.ananya.kilkari.smoke.domain.kilkari.SubscriberResponse;
 import org.motechproject.ananya.kilkari.smoke.domain.kilkari.SubscriptionDetails;
 import org.motechproject.ananya.kilkari.smoke.domain.kilkari.SubscriptionRequest;
 import org.motechproject.ananya.kilkari.smoke.domain.report.SubscriptionStatusMeasure;
 import org.motechproject.ananya.kilkari.smoke.service.ReportService;
 import org.motechproject.ananya.kilkari.smoke.service.SubscriptionService;
 import org.motechproject.ananya.kilkari.smoke.utils.SubscriptionRequestBuilder;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.web.client.RestTemplate;
 
 import java.sql.SQLException;
 import java.util.List;
 
 import static junit.framework.Assert.assertEquals;
 import static org.motechproject.ananya.kilkari.smoke.utils.TestUtils.KILKARI_SUBSCRIPTION_POST_URL;
 import static org.motechproject.ananya.kilkari.smoke.utils.TestUtils.fromJsonWithResponse;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration("classpath:applicationKilkariSmokeContext.xml")
 public class CallCenterSmokeTest {
     RestTemplate restTemplate;
     SubscriptionService subscriptionService;
     @Autowired
     ReportService reportService;
 
     @Before
     public void setUp() throws SQLException {
         subscriptionService = new SubscriptionService();
         restTemplate = new RestTemplate();
         reportService.deleteAll();
         reportService.createNewLocation("D1", "B1", "P1");
     }
 
     @Test
     public void shouldPostHttpRequestAndVerifyEntriesInReportDbAndCouchDb() throws InterruptedException {
         String expectedStatus = "PENDING_ACTIVATION";
        SubscriptionRequest subscriptionRequest = new SubscriptionRequestBuilder().withDefaults().build();
 
         String responseEntity = restTemplate.postForObject(KILKARI_SUBSCRIPTION_POST_URL, subscriptionRequest, String.class);
 
         BaseResponse baseResponse = fromJsonWithResponse(responseEntity, BaseResponse.class);
         assertEquals("SUCCESS", baseResponse.getStatus());
 
         SubscriberResponse response = subscriptionService.getSubscriptionData(subscriptionRequest.getMsisdn(), subscriptionRequest.getChannel(), expectedStatus);
         assertEquals(1, response.getSubscriptionDetails().size());
         assertKilkariData(subscriptionRequest.getPack(), expectedStatus, response);
 
         List<SubscriptionStatusMeasure> subscriptionStatusMeasures = reportService.getSubscriptionStatusMeasureForMsisdn(subscriptionRequest.getMsisdn());
         assertEquals(2, subscriptionStatusMeasures.size());
        assertReportData(subscriptionStatusMeasures, subscriptionRequest.getChannel(), subscriptionRequest.getMsisdn(), subscriptionRequest.getPack(), expectedStatus, subscriptionRequest.getBeneficiaryName(), subscriptionRequest.getBeneficiaryAge(), subscriptionRequest.getDateOfBirth(), subscriptionRequest.getExpectedDateOfDelivery());
     }
 
     private void assertKilkariData(String pack, String expectedStatus, SubscriberResponse response) {
         SubscriptionDetails subscriptionDetails = response.getSubscriptionDetails().get(0);
         assertEquals(pack, subscriptionDetails.getPack());
         assertEquals(expectedStatus, subscriptionDetails.getStatus());
     }
 
    private void assertReportData(List<SubscriptionStatusMeasure> subscriptionStatusMeasures, String channel, String msisdn, String pack, String expectedStatus, String beneficiaryName, String beneficiaryAge, String dateOfBirth, String estimatedDateOfDelivery) {
         SubscriptionStatusMeasure subscriptionStatusMeasure0 = subscriptionStatusMeasures.get(0);
         SubscriptionStatusMeasure subscriptionStatusMeasure1 = subscriptionStatusMeasures.get(1);
 
         assertEquals(msisdn, subscriptionStatusMeasure0.getMsisdn());
         assertEquals(pack, subscriptionStatusMeasure0.getPack().toUpperCase());
         assertEquals(channel, subscriptionStatusMeasure0.getChannel().toUpperCase());
         assertEquals("NEW", subscriptionStatusMeasure0.getStatus().toUpperCase());
         assertEquals(beneficiaryName, subscriptionStatusMeasure0.getName());
         assertEquals(beneficiaryAge, subscriptionStatusMeasure0.getAge());
        assertEquals(dateOfBirth, new DateTime(subscriptionStatusMeasure0.getDateOfBirth()).toString("dd-MM-yyyy"));
        assertEquals(estimatedDateOfDelivery, new DateTime(subscriptionStatusMeasure0.getEstimatedDateOfDelivery()).toString("dd-MM-yyyy"));
 
         assertEquals(msisdn, subscriptionStatusMeasure1.getMsisdn());
         assertEquals(pack, subscriptionStatusMeasure1.getPack().toUpperCase());
         assertEquals(channel, subscriptionStatusMeasure1.getChannel().toUpperCase());
         assertEquals(expectedStatus, subscriptionStatusMeasure1.getStatus().toUpperCase());
     }
 }
