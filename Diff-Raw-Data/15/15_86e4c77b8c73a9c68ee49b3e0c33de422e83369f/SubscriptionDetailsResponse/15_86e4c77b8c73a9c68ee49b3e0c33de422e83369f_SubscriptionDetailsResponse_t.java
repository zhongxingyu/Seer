 package org.motechproject.ananya.kilkari.subscription.service.response;
 
 import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
 import org.motechproject.ananya.kilkari.subscription.domain.SubscriptionPack;
 import org.motechproject.ananya.kilkari.subscription.domain.SubscriptionStatus;
 import org.motechproject.ananya.kilkari.subscription.service.request.Location;
 import org.motechproject.ananya.kilkari.subscription.validators.DateUtils;
 
 
 public class SubscriptionDetailsResponse {
     private String subscriptionId;
     private SubscriptionPack pack;
     private SubscriptionStatus status;
     private String campaignId;
     private String beneficiaryName;
     private Integer beneficiaryAge;
     private String lastWeeklyMessageScheduledDate;
     private Integer startWeekNumber;
     private String expectedDateOfDelivery;
     private String dateOfBirth;
     private Location location;
 
     public SubscriptionDetailsResponse(String subscriptionId, SubscriptionPack pack, SubscriptionStatus status, String campaignId) {
         this.subscriptionId = subscriptionId;
         this.pack = pack;
         this.status = status;
         this.campaignId = campaignId;
     }
 
     public SubscriptionDetailsResponse(String subscriptionId, SubscriptionPack pack, SubscriptionStatus status, String campaignId, String beneficiaryName, Integer beneficiaryAge, DateTime dateOfBirth, DateTime expectedDateOfDelivery, Integer startWeekNumber, Location location, DateTime lastWeeklyMessageScheduledDate) {
         this(subscriptionId, pack, status, campaignId);
         this.beneficiaryName = beneficiaryName;
         this.beneficiaryAge = beneficiaryAge;
         this.dateOfBirth = DateUtils.formatDate(dateOfBirth, DateUtils.ISTTimeZone);
         this.expectedDateOfDelivery = DateUtils.formatDate(expectedDateOfDelivery, DateUtils.ISTTimeZone);
         this.startWeekNumber = startWeekNumber;
         this.location = location;
        this.lastWeeklyMessageScheduledDate = DateUtils.formatDate(lastWeeklyMessageScheduledDate, DateTimeZone.UTC);
     }
 
     public String getSubscriptionId() {
         return subscriptionId;
     }
 
     public SubscriptionPack getPack() {
         return pack;
     }
 
     public SubscriptionStatus getStatus() {
         return status;
     }
 
     public String getCampaignId() {
         return campaignId;
     }
 
     public String getBeneficiaryName() {
         return beneficiaryName;
     }
 
     public Integer getBeneficiaryAge() {
         return beneficiaryAge;
     }
 
     public Integer getStartWeekNumber() {
         return startWeekNumber;
     }
 
     public String getExpectedDateOfDelivery() {
         return expectedDateOfDelivery;
     }
 
     public String getDateOfBirth() {
         return dateOfBirth;
     }
 
     public Location getLocation() {
         return location;
     }
 
     public String getLastWeeklyMessageScheduledDate() {
         return lastWeeklyMessageScheduledDate;
     }
 }
