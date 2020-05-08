 package org.motechproject.ananya.kilkari.request;
 
 import org.apache.commons.lang.builder.EqualsBuilder;
 import org.apache.commons.lang.builder.HashCodeBuilder;
 import org.codehaus.jackson.annotate.JsonIgnore;
 import org.codehaus.jackson.annotate.JsonProperty;
 import org.motechproject.ananya.kilkari.obd.domain.CallDetailRecord;
 
 import java.io.Serializable;
 
 public class OBDSuccessfulCallRequest implements Serializable {
     @JsonProperty
     private String msisdn;
     @JsonProperty
     private String campaignId;
     @JsonProperty
     private String serviceOption;
     @JsonProperty
     private CallDetailRecord callDetailRecord;
    @JsonProperty
    private String status;
 
     public void setMsisdn(String msisdn) {
         this.msisdn = msisdn;
     }
 
     public void setCampaignId(String campaignId) {
         this.campaignId = campaignId;
     }
 
     public void setServiceOption(String serviceOption) {
         this.serviceOption = serviceOption;
     }
 
     public void setCallDetailRecord(CallDetailRecord callDetailRecord) {
         this.callDetailRecord = callDetailRecord;
     }
 
     @JsonIgnore
     public String getMsisdn() {
         return msisdn;
     }
 
     @JsonIgnore
     public String getServiceOption() {
         return serviceOption;
     }
 
     @JsonIgnore
     public String getCampaignId() {
         return campaignId;
     }
 
     @JsonIgnore
     public CallDetailRecord getCallDetailRecord() {
         return callDetailRecord;
     }
 
    @JsonIgnore
    public String getStatus() {
        return status;
    }

     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (!(o instanceof OBDSuccessfulCallRequest)) return false;
 
         OBDSuccessfulCallRequest that = (OBDSuccessfulCallRequest) o;
 
         return new EqualsBuilder()
                 .append(this.msisdn, that.msisdn)
                 .append(this.campaignId, that.campaignId)
                 .append(this.serviceOption, that.serviceOption)
                 .append(this.callDetailRecord, that.callDetailRecord)
                 .isEquals();
     }
 
     @Override
     public int hashCode() {
         return new HashCodeBuilder()
                 .append(this.msisdn)
                 .append(this.campaignId)
                 .append(this.serviceOption)
                 .append(this.callDetailRecord)
                 .hashCode();
     }
 }
