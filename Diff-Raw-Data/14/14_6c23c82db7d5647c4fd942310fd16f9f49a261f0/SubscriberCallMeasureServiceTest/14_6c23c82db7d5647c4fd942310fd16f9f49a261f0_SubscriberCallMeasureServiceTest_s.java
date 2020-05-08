 package org.motechproject.ananya.kilkari.reports.service;
 
 import org.joda.time.DateTime;
 import org.joda.time.format.DateTimeFormat;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.ArgumentCaptor;
 import org.mockito.Mock;
 import org.motechproject.ananya.kilkari.contract.request.CallDetailRecordRequest;
 import org.motechproject.ananya.kilkari.contract.request.CallDetailsReportRequest;
 import org.motechproject.ananya.kilkari.reports.domain.dimension.*;
 import org.motechproject.ananya.kilkari.reports.domain.measure.SubscriberCallMeasure;
 import org.motechproject.ananya.kilkari.reports.repository.AllCampaignDimensions;
 import org.motechproject.ananya.kilkari.reports.repository.AllDateDimensions;
 import org.motechproject.ananya.kilkari.reports.repository.AllSubscriberCallMeasures;
 import org.motechproject.ananya.kilkari.reports.repository.AllTimeDimensions;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNull;
 import static org.mockito.Mockito.*;
 import static org.mockito.MockitoAnnotations.initMocks;
 
 public class SubscriberCallMeasureServiceTest {
 
 
     private SubscriberCallMeasureService subscriberCallMeasureService;
     @Mock
     private AllSubscriberCallMeasures allSubscriberCallMeasures;
     @Mock
     private SubscriptionService subscriptionService;
     @Mock
     private AllCampaignDimensions allCampaignDimensions;
     @Mock
     private AllDateDimensions allDateDimensions;
     @Mock
     private AllTimeDimensions allTimeDimensions;
 
     @Before
     public void setUp() {
         initMocks(this);
         subscriberCallMeasureService = new SubscriberCallMeasureService(allSubscriberCallMeasures, subscriptionService, allCampaignDimensions, allDateDimensions, allTimeDimensions);
         subscriberCallMeasureService.setWelcomeMessageDuration(60);
     }
 
     @Test
     public void shouldCreateSubscriberCallDetails() {
         //Given
         String subscriptionId = "subId";
         String msisdn = "9876543210";
         String campaignId = "1";
         String retryCount = "2";
         String serviceOption = "HELP";
        Integer percentageListened = 50;
         String status = "DNP";
         String startTime = "01-01-2012 01-10-00";
        String endTime = "01-01-2012 01-41-00";
         DateTime startDateTime = DateTimeFormat.forPattern("dd-MM-yyyy HH-mm-ss").parseDateTime(startTime);
         DateTime endDateTime = DateTimeFormat.forPattern("dd-MM-yyyy HH-mm-ss").parseDateTime(endTime);
         CallDetailsReportRequest callDetailsReportRequest = new CallDetailsReportRequest(subscriptionId, msisdn, campaignId, serviceOption, retryCount, status, new CallDetailRecordRequest(startDateTime, endDateTime), "OBD");
 
         Subscription mockedSubscription = mock(Subscription.class);
         OperatorDimension mockedOperatorDimension = mock(OperatorDimension.class);
         CampaignDimension mockedCampaignDimension = mock(CampaignDimension.class);
         DateDimension mockedStartDateDimension = mock(DateDimension.class);
         TimeDimension mockedStartTimeDimension = mock(TimeDimension.class);
         DateDimension mockedEndDateDimension = mock(DateDimension.class);
         TimeDimension mockedEndTimeDimension = mock(TimeDimension.class);
         SubscriptionPackDimension mockedSubscriptionPackDimension = mock(SubscriptionPackDimension.class);
 
         when(subscriptionService.fetchFor(subscriptionId)).thenReturn(mockedSubscription);
         when(allCampaignDimensions.fetchFor(campaignId)).thenReturn(mockedCampaignDimension);
         when(allDateDimensions.fetchFor(startDateTime)).thenReturn(mockedStartDateDimension);
         when(allTimeDimensions.fetchFor(startDateTime)).thenReturn(mockedStartTimeDimension);
         when(allDateDimensions.fetchFor(endDateTime)).thenReturn(mockedEndDateDimension);
         when(allTimeDimensions.fetchFor(endDateTime)).thenReturn(mockedEndTimeDimension);
         when(mockedSubscription.getOperatorDimension()).thenReturn(mockedOperatorDimension);
         when(mockedSubscription.getSubscriptionPackDimension()).thenReturn(mockedSubscriptionPackDimension);
         when(mockedCampaignDimension.getMessageDuration()).thenReturn(3600);
 
         //When
         subscriberCallMeasureService.createSubscriberCallDetails(callDetailsReportRequest);
 
         //Expected
         verify(subscriptionService).fetchFor(subscriptionId);
 
         verify(allCampaignDimensions).fetchFor(campaignId);
         verify(allDateDimensions).fetchFor(startDateTime);
         verify(allTimeDimensions).fetchFor(startDateTime);
         verify(allDateDimensions).fetchFor(endDateTime);
         verify(allTimeDimensions).fetchFor(endDateTime);
 
         ArgumentCaptor<SubscriberCallMeasure> subscriberCallMeasureCaptor = ArgumentCaptor.forClass(SubscriberCallMeasure.class);
         verify(allSubscriberCallMeasures).createFor(subscriberCallMeasureCaptor.capture());
         SubscriberCallMeasure subscriberCallMeasure = subscriberCallMeasureCaptor.getValue();
         assertEquals(callDetailsReportRequest.getDuration(), subscriberCallMeasure.getDuration());
         assertEquals(callDetailsReportRequest.getServiceOption(), subscriberCallMeasure.getServiceOption());
         assertEquals(mockedSubscription, subscriberCallMeasure.getSubscription());
         assertEquals(mockedOperatorDimension, subscriberCallMeasure.getOperatorDimension());
         assertEquals(mockedSubscriptionPackDimension, subscriberCallMeasure.getSubscriptionPackDimension());
         assertEquals(mockedStartDateDimension, subscriberCallMeasure.getStartDate());
         assertEquals(mockedStartTimeDimension, subscriberCallMeasure.getStartTime());
         assertEquals(mockedEndDateDimension, subscriberCallMeasure.getEndDate());
         assertEquals(mockedEndTimeDimension, subscriberCallMeasure.getEndTime());
         assertEquals(percentageListened, subscriberCallMeasure.getPercentageListened());
         assertEquals(callDetailsReportRequest.getStatus(), subscriberCallMeasure.getCallStatus());
         assertEquals((Integer) 2, subscriberCallMeasure.getRetryCount());
         assertEquals("OBD", subscriberCallMeasure.getCallSource());
     }
 
 
     @Test
     public void shouldCreateSubscriberCallDetailsWithNullValues() {
         //Given
         String subscriptionId = "subId";
         String msisdn = "9876543210";
         String campaignId = "1";
         String retryCount = null;
         String serviceOption = null;
         Integer percentageListened = 0;
         String status = "DNP";
         String startTime = "01-01-2012 01-10-00";
         DateTime startDateTime = DateTimeFormat.forPattern("dd-MM-yyyy HH-mm-ss").parseDateTime(startTime);
         CallDetailsReportRequest callDetailsReportRequest = new CallDetailsReportRequest(subscriptionId, msisdn, campaignId, serviceOption, retryCount, status, new CallDetailRecordRequest(startDateTime, startDateTime), "OBD");
 
         Subscription mockedSubscription = mock(Subscription.class);
         OperatorDimension mockedOperatorDimension = mock(OperatorDimension.class);
         CampaignDimension mockedCampaignDimension = mock(CampaignDimension.class);
         DateDimension mockedStartDateDimension = mock(DateDimension.class);
         TimeDimension mockedStartTimeDimension = mock(TimeDimension.class);
         SubscriptionPackDimension mockedSubscriptionPackDimension = mock(SubscriptionPackDimension.class);
 
         when(subscriptionService.fetchFor(subscriptionId)).thenReturn(mockedSubscription);
         when(allCampaignDimensions.fetchFor(campaignId)).thenReturn(mockedCampaignDimension);
         when(allDateDimensions.fetchFor(startDateTime)).thenReturn(mockedStartDateDimension);
         when(allTimeDimensions.fetchFor(startDateTime)).thenReturn(mockedStartTimeDimension);
         when(mockedSubscription.getOperatorDimension()).thenReturn(mockedOperatorDimension);
         when(mockedSubscription.getSubscriptionPackDimension()).thenReturn(mockedSubscriptionPackDimension);
         when(mockedCampaignDimension.getMessageDuration()).thenReturn(3600);
 
         //When
         subscriberCallMeasureService.createSubscriberCallDetails(callDetailsReportRequest);
 
         //Expected
         verify(subscriptionService).fetchFor(subscriptionId);
 
         verify(allCampaignDimensions).fetchFor(campaignId);
         verify(allDateDimensions, times(2)).fetchFor(startDateTime);
         verify(allTimeDimensions, times(2)).fetchFor(startDateTime);
 
         ArgumentCaptor<SubscriberCallMeasure> subscriberCallMeasureCaptor = ArgumentCaptor.forClass(SubscriberCallMeasure.class);
         verify(allSubscriberCallMeasures).createFor(subscriberCallMeasureCaptor.capture());
         SubscriberCallMeasure subscriberCallMeasure = subscriberCallMeasureCaptor.getValue();
         assertEquals(Integer.valueOf(0), subscriberCallMeasure.getDuration());
         assertNull(subscriberCallMeasure.getServiceOption());
         assertEquals(mockedSubscription, subscriberCallMeasure.getSubscription());
         assertEquals(mockedOperatorDimension, subscriberCallMeasure.getOperatorDimension());
         assertEquals(mockedSubscriptionPackDimension, subscriberCallMeasure.getSubscriptionPackDimension());
         assertEquals(mockedStartDateDimension, subscriberCallMeasure.getStartDate());
         assertEquals(mockedStartTimeDimension, subscriberCallMeasure.getStartTime());
         assertEquals(mockedStartDateDimension, subscriberCallMeasure.getEndDate());
         assertEquals(mockedStartTimeDimension, subscriberCallMeasure.getEndTime());
         assertEquals((Integer)0, subscriberCallMeasure.getPercentageListened());
         assertEquals(callDetailsReportRequest.getStatus(), subscriberCallMeasure.getCallStatus());
         assertNull(subscriberCallMeasure.getRetryCount());
         assertEquals("OBD", subscriberCallMeasure.getCallSource());
     }
 }
