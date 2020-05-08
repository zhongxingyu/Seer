 package org.motechproject.ananya.kilkari;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.motechproject.ananya.kilkari.domain.kilkari.BaseResponse;
 import org.motechproject.ananya.kilkari.domain.kilkari.SubscriberResponse;
 import org.motechproject.ananya.kilkari.domain.kilkari.SubscriptionDetails;
 import org.motechproject.ananya.kilkari.domain.report.SubscriptionStatusMeasure;
 import org.motechproject.ananya.kilkari.service.ReportServiceAsync;
 import org.motechproject.ananya.kilkari.service.SubscriptionServiceAsync;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.ResponseEntity;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.web.client.RestTemplate;
 
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import static junit.framework.Assert.assertEquals;
 import static org.motechproject.ananya.kilkari.utils.TestUtils.*;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration("classpath:applicationKilkariSmokeContext.xml")
 public class IvrSmokeTest {
     RestTemplate restTemplate;
     SubscriptionServiceAsync subscriptionServiceAsync;
     @Autowired
     ReportServiceAsync reportServiceAsync;
 
     @Before
     public void setUp() {
         subscriptionServiceAsync = new SubscriptionServiceAsync();
         restTemplate = new RestTemplate();
     }
 
     @Test(timeout = 10000)
     public void shouldPostHttpRequestAndVerifyEntriesInReportDbAndCouchDb() throws InterruptedException, SQLException {
        String channel = "ivr";
         String msisdn = "9000000001";
         String pack = "FIFTEEN_MONTHS";
 
         Map<String, String> parametersMap = new HashMap<>();
         parametersMap.put("msisdn", msisdn);
         parametersMap.put("channel", channel);
         parametersMap.put("pack", pack);
 
         ResponseEntity<String> responseEntity = restTemplate.getForEntity(constructUrl(KILKARI_URL, "subscription", parametersMap), String.class);
 
         BaseResponse baseResponse = fromJson(responseEntity.getBody().replace("var response = ", ""), BaseResponse.class);
         assertEquals("SUCCESS", baseResponse.getStatus());
 
         SubscriberResponse response = subscriptionServiceAsync.getSubscriptionData(msisdn, channel);
         assertEquals(1, response.getSubscriptionDetails().size());
         SubscriptionDetails subscriptionDetails = response.getSubscriptionDetails().get(0);
         assertEquals(pack, subscriptionDetails.getPack());
         assertEquals("PENDING_ACTIVATION", subscriptionDetails.getStatus());
 
         List<SubscriptionStatusMeasure> subscriptionStatusMeasures = reportServiceAsync.getSubscriptionStatusMeasure();
        assertEquals(1, subscriptionStatusMeasures.size());
         assertEquals(msisdn, subscriptionStatusMeasures.get(0).getMsisdn());
         assertEquals(pack, subscriptionStatusMeasures.get(0).getPack().toUpperCase());
     }
 }
