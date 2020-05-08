 package org.motechproject.ananya.kilkari.functional.test.verifiers;
 
 import org.mockito.ArgumentCaptor;
 import org.mockito.Mock;
 import org.mockito.Mockito;
 import org.motechproject.ananya.kilkari.contract.request.SubscriptionReportRequest;
 import org.motechproject.ananya.kilkari.contract.response.LocationResponse;
 import org.motechproject.ananya.kilkari.functional.test.domain.SubscriptionData;
 import org.motechproject.ananya.kilkari.reporting.service.ReportingService;
 import org.motechproject.ananya.kilkari.reporting.service.StubReportingService;
 import org.motechproject.ananya.kilkari.request.LocationRequest;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import static junit.framework.Assert.assertEquals;
 import static org.mockito.Matchers.anyString;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 import static org.mockito.MockitoAnnotations.initMocks;
 
 @Component
 public class ReportVerifier {
 
 
     private StubReportingService stubReportingService;
 
     @Mock
     protected ReportingService reportingService;
 
 
     @Autowired
     public ReportVerifier(StubReportingService stubReportingService) {
         initMocks(this);
         this.stubReportingService = stubReportingService;
         stubReportingService.setBehavior(reportingService);
     }
 
     public void verifySubscriptionCreationRequest(SubscriptionData subscriptionData) {
         ArgumentCaptor<SubscriptionReportRequest> requestArgumentCaptor = ArgumentCaptor.forClass(SubscriptionReportRequest.class);
         verify(reportingService).reportSubscriptionCreation(requestArgumentCaptor.capture());
 
         SubscriptionReportRequest reportRequest = requestArgumentCaptor.getValue();
        assertEquals(subscriptionData.getMsisdn(), reportRequest.getMsisdn().toString());
         assertEquals(subscriptionData.getBeneficiaryName(), reportRequest.getName());
         assertEquals(subscriptionData.getPack().name(), reportRequest.getPack());
         assertEquals(subscriptionData.getChannel(), reportRequest.getChannel());
         assertEquals(subscriptionData.getExpectedDateOfDelivery(), reportRequest.getEstimatedDateOfDelivery().toString("dd-MM-yyyy"));
         assertEquals(subscriptionData.getBeneficiaryAge(), String.valueOf(reportRequest.getAgeOfBeneficiary()));
         LocationRequest location = subscriptionData.getLocation();
         assertEquals(location.getBlock(), reportRequest.getLocation().getBlock());
         assertEquals(location.getDistrict(), reportRequest.getLocation().getDistrict());
         assertEquals(location.getPanchayat(), reportRequest.getLocation().getPanchayat());
     }
 
     public void setUpReporting(SubscriptionData subscriptionData) {
         LocationRequest location = subscriptionData.getLocation();
         if (location == null) return;
         when(reportingService.getLocation(anyString(), anyString(), anyString())).thenReturn(new LocationResponse(location.getDistrict(), location.getBlock(), location.getPanchayat()));
     }
 
     public void resetMockBehaviour() {
         Mockito.reset(reportingService);
     }
 }
