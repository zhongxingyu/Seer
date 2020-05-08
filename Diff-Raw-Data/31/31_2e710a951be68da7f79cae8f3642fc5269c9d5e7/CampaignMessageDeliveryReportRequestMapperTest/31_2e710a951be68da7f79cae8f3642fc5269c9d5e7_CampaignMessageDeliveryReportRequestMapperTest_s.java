 package org.motechproject.ananya.kilkari.domain;
 
 import org.joda.time.DateTime;
 import org.junit.Test;
 import org.motechproject.ananya.kilkari.obd.domain.CallDetailRecord;
 import org.motechproject.ananya.kilkari.reporting.domain.CampaignMessageDeliveryReportRequest;
 import org.motechproject.ananya.kilkari.request.OBDSuccessfulCallRequest;
 import org.motechproject.ananya.kilkari.request.OBDSuccessfulCallRequestWrapper;
 import org.motechproject.ananya.kilkari.subscription.domain.Channel;
 
 import static junit.framework.Assert.assertEquals;
 
 public class CampaignMessageDeliveryReportRequestMapperTest {
 
     @Test
     public void shouldMapCampaignMessageDeliveryReportRequest() {
         DateTime startTime = DateTime.now();
         DateTime endTime = DateTime.now().plusMinutes(2);
         CallDetailRecord callDetailRecord = new CallDetailRecord();
         callDetailRecord.setStartTime(startTime.toString());
         callDetailRecord.setEndTime(endTime.toString());
 
         OBDSuccessfulCallRequest successfulCallRequest = new OBDSuccessfulCallRequest();
         successfulCallRequest.setMsisdn("1234567890");
         successfulCallRequest.setServiceOption("HELP");
         successfulCallRequest.setCampaignId("CampaignId");
         successfulCallRequest.setCallDetailRecord(callDetailRecord);
 
         Integer retryCount = 3;
         String subscriptionId = "subscriptionId";
         CampaignMessageDeliveryReportRequestMapper campaignMessageDeliveryReportRequestMapper = new CampaignMessageDeliveryReportRequestMapper();
         OBDSuccessfulCallRequestWrapper successfulCallRequestWrapper = new OBDSuccessfulCallRequestWrapper(successfulCallRequest, subscriptionId, DateTime.now(), Channel.IVR);
 
         CampaignMessageDeliveryReportRequest actualDeliveryReportRequest = campaignMessageDeliveryReportRequestMapper.mapFrom(successfulCallRequestWrapper, retryCount);
 
         assertEquals(subscriptionId, actualDeliveryReportRequest.getSubscriptionId());
         assertEquals(successfulCallRequest.getMsisdn(), actualDeliveryReportRequest.getMsisdn());
         assertEquals(successfulCallRequest.getCampaignId(), actualDeliveryReportRequest.getCampaignId());
         assertEquals(successfulCallRequest.getServiceOption(), actualDeliveryReportRequest.getServiceOption());
        assertEquals(successfulCallRequest.getStatus(), actualDeliveryReportRequest.getStatus());
         assertEquals(retryCount.toString(), actualDeliveryReportRequest.getRetryCount());
         assertEquals(startTime.toString(), successfulCallRequest.getCallDetailRecord().getStartTime());
         assertEquals(endTime.toString(), successfulCallRequest.getCallDetailRecord().getEndTime());
     }
 }
