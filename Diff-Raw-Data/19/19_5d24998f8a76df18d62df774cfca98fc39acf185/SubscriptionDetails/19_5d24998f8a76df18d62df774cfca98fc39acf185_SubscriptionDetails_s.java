 package org.motechproject.ananya.kilkari.smoke.domain;
 
 import org.codehaus.jackson.annotate.JsonIgnore;
 import org.codehaus.jackson.annotate.JsonProperty;
 
 public class SubscriptionDetails {
     @JsonProperty
     private String subscriptionId;
     @JsonProperty
     private String pack;
     @JsonProperty
     private String status;
     @JsonProperty
     private String lastCampaignId;
     @JsonProperty
     private String beneficiaryName;
     @JsonProperty
     private String beneficiaryAge;
     @JsonProperty
     private String weekNumber;
     @JsonProperty
     private String expectedDateOfDelivery;
     @JsonProperty
     private String dateOfBirth;
     @JsonProperty
     private Location location;

 
     public SubscriptionDetails() {
     }
 
    public SubscriptionDetails(String subscriptionId, String pack, String status,
                               String lastCampaignId, String beneficiaryName, String beneficiaryAge,
                               String weekNumber, String expectedDateOfDelivery, String dateOfBirth, Location location) {
        this.subscriptionId = subscriptionId;
        this.pack = pack;
        this.status = status;
        this.lastCampaignId = lastCampaignId;
        this.beneficiaryName = beneficiaryName;
        this.beneficiaryAge = beneficiaryAge;
        this.weekNumber = weekNumber;
        this.expectedDateOfDelivery = expectedDateOfDelivery;
        this.dateOfBirth = dateOfBirth;
        this.location = location;
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
 
     @JsonIgnore
     public String getLastCampaignId() {
         return lastCampaignId;
     }
 }
