 package org.motechproject.ananya.kilkari.request;
 
 import org.joda.time.DateTime;
 import org.motechproject.ananya.kilkari.obd.domain.CampaignMessageStatus;
 import org.motechproject.ananya.kilkari.subscription.domain.Channel;
 
 import java.io.Serializable;
 
 public class OBDSuccessfulCallRequestWrapper implements Serializable {
 
     private OBDSuccessfulCallRequest successfulCallRequest;
     private String subscriptionId;
     private DateTime createdAt;
     private Channel channel;
     private CampaignMessageStatus status;
 
     public OBDSuccessfulCallRequestWrapper(OBDSuccessfulCallRequest successfulCallRequest, String subscriptionId, DateTime createdAt, Channel channel) {
         this.successfulCallRequest = successfulCallRequest;
         this.subscriptionId = subscriptionId;
         this.createdAt = createdAt;
         this.channel = channel;
         this.status = CampaignMessageStatus.SUCCESS;
     }
 
     public String getSubscriptionId() {
         return subscriptionId;
     }
 
     public OBDSuccessfulCallRequest getSuccessfulCallRequest() {
         return successfulCallRequest;
     }
 
     public DateTime getCreatedAt() {
         return createdAt;
     }
 
     public String getCampaignId() {
         return successfulCallRequest.getCampaignId();
     }
 
     public Channel getChannel() {
         return channel;
     }
 }
