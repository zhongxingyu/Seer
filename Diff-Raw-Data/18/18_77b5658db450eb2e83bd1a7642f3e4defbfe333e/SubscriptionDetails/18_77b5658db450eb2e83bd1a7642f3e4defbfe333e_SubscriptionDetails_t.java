 package org.motechproject.ananya.kilkari.web.contract.response;
 
import org.apache.commons.lang.builder.ToStringBuilder;
 import org.codehaus.jackson.annotate.JsonProperty;
 
 public class SubscriptionDetails {
     @JsonProperty
     private String subscriptionId;
 
     @JsonProperty
     private String pack;
 
     @JsonProperty
     private String status;
 
     public SubscriptionDetails(String subscriptionId, String pack, String status) {
         this.subscriptionId = subscriptionId;
         this.pack = pack;
         this.status = status;
     }
 
     public String getSubscriptionId() {
         return subscriptionId;
     }
 
     public String getPack() {
         return pack;
     }
 
     public String getStatus() {
         return status;
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (!(o instanceof SubscriptionDetails)) return false;
 
         SubscriptionDetails that = (SubscriptionDetails) o;
 
         if (pack != null ? !pack.equals(that.pack) : that.pack != null) return false;
         if (status != null ? !status.equals(that.status) : that.status != null) return false;
         if (subscriptionId != null ? !subscriptionId.equals(that.subscriptionId) : that.subscriptionId != null)
             return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         int result = subscriptionId != null ? subscriptionId.hashCode() : 0;
         result = 31 * result + (pack != null ? pack.hashCode() : 0);
         result = 31 * result + (status != null ? status.hashCode() : 0);
         return result;
     }
 
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
 }
