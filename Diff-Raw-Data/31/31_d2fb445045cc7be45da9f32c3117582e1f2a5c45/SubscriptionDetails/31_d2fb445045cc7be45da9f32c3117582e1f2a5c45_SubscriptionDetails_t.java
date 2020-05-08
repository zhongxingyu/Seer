 package org.motechproject.ananya.kilkari.web.contract.response;
 
 import org.apache.commons.lang.builder.EqualsBuilder;
 import org.apache.commons.lang.builder.HashCodeBuilder;
 import org.apache.commons.lang.builder.ToStringBuilder;
 import org.apache.commons.lang.builder.ToStringStyle;
import org.codehaus.jackson.annotate.JsonIgnore;
 import org.codehaus.jackson.annotate.JsonProperty;
 
 public class SubscriptionDetails {
     @JsonProperty
     private String subscriptionId;
 
     @JsonProperty
     private String pack;
 
     @JsonProperty
     private String status;
 
     SubscriptionDetails() {
     }
 
     public SubscriptionDetails(String subscriptionId, String pack, String status) {
         this.subscriptionId = subscriptionId;
         this.pack = pack;
         this.status = status;
     }
 
    @JsonIgnore
     public String getSubscriptionId() {
         return subscriptionId;
     }
 
    @JsonIgnore
     public String getPack() {
         return pack;
     }
 
    @JsonIgnore
     public String getStatus() {
         return status;
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (!(o instanceof SubscriptionDetails)) return false;
 
         SubscriptionDetails that = (SubscriptionDetails) o;
 
         return new EqualsBuilder().append(this.subscriptionId, that.subscriptionId)
                 .append(this.pack, that.pack)
                 .append(this.status, that.status)
                 .isEquals();
     }
 
     @Override
     public int hashCode() {
         return new HashCodeBuilder()
                 .append(this.subscriptionId)
                 .append(this.pack)
                 .append(this.status).hashCode();
     }
 
     @Override
     public String toString() {
         return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                 .append(subscriptionId)
                 .append(pack)
                 .append(status).toString();
     }
 }
