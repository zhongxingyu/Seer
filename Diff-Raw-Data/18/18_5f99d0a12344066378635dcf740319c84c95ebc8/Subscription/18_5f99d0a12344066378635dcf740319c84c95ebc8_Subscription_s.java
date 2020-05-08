 package org.motechproject.ananya.kilkari.subscription.domain;
 
 import org.apache.commons.lang.builder.EqualsBuilder;
 import org.apache.commons.lang.builder.HashCodeBuilder;
 import org.apache.commons.lang.builder.ToStringBuilder;
 import org.apache.commons.lang.builder.ToStringStyle;
 import org.codehaus.jackson.annotate.JsonIgnore;
 import org.codehaus.jackson.annotate.JsonProperty;
 import org.ektorp.support.TypeDiscriminator;
 import org.joda.time.DateTime;
 import org.joda.time.Weeks;
 import org.motechproject.model.MotechBaseDataObject;
 
 import java.util.UUID;
 
 @TypeDiscriminator("doc.type === 'Subscription'")
 public class Subscription extends MotechBaseDataObject {
     @JsonProperty
     private String msisdn;
 
     @JsonProperty
     private Operator operator;
 
     @JsonProperty
     private String subscriptionId;
 
     @JsonProperty
     private SubscriptionStatus status;
 
     @JsonProperty
     private SubscriptionPack pack;
 
     @JsonProperty
     private DateTime creationDate;
 
     @JsonProperty
     private DateTime activationDate;
 
     @JsonProperty
     private DateTime startDate;
 
     public Subscription() {
     }
 
     public Subscription(String msisdn, SubscriptionPack pack, DateTime createdAt, SubscriptionStatus status) {
         this.pack = pack;
         this.msisdn = msisdn;
         this.creationDate = createdAt;
         this.startDate = creationDate;
         this.status = status;
         this.subscriptionId = UUID.randomUUID().toString();
     }
 
     public String getMsisdn() {
         return msisdn;
     }
 
     public String getSubscriptionId() {
         return subscriptionId;
     }
 
     public DateTime getCreationDate() {
         return creationDate;
     }
 
     public SubscriptionStatus getStatus() {
         return status;
     }
 
     public SubscriptionPack getPack() {
         return pack;
     }
 
     public void setStatus(SubscriptionStatus status) {
         this.status = status;
     }
 
     public Operator getOperator() {
         return operator;
     }
 
     public void setOperator(Operator operator) {
         this.operator = operator;
     }
 
     public DateTime getStartDate() {
         return startDate;
     }
 
     public void setStartDate(DateTime startDate) {
         this.startDate = startDate;
     }
 
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (!(o instanceof Subscription)) return false;
 
         Subscription that = (Subscription) o;
 
         return new EqualsBuilder().append(this.msisdn, that.msisdn)
                 .append(this.pack, that.pack)
                 .append(this.subscriptionId, that.subscriptionId)
                 .append(this.operator, that.operator)
                 .isEquals();
     }
 
     @Override
     public int hashCode() {
         return new HashCodeBuilder()
                 .append(this.msisdn)
                 .append(this.subscriptionId)
                 .append(this.pack)
                 .hashCode();
     }
 
     @Override
     public String toString() {
         return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                 .append(this.msisdn)
                 .append(this.subscriptionId)
                 .append(this.pack)
                 .append(this.status)
                 .append(this.creationDate)
                 .append(this.startDate)
                 .toString();
     }
 
     @JsonIgnore
     public boolean isInProgress() {
         return status.isInProgress();
     }
 
     @JsonIgnore
     public boolean isActive() {
         return status.isActive();
     }
 
     @JsonIgnore
     public boolean isNewEarly() {
         return status.isNewEarly();
     }
 
     @JsonIgnore
     public boolean isActiveOrSuspended() {
         return status.isActive() || status.isSuspended();
     }
 
     public void activateOnRenewal() {
         setStatus(SubscriptionStatus.ACTIVE);
     }
 
     public void suspendOnRenewal() {
         setStatus(SubscriptionStatus.SUSPENDED);
     }
 
     public void activate(String operator, DateTime scheduledStartDate) {
         setStatus(SubscriptionStatus.ACTIVE);
         setOperator(Operator.getFor(operator));
         this.startDate = scheduledStartDate;
     }
 
     public void activationFailed(String operator) {
         setStatus(SubscriptionStatus.ACTIVATION_FAILED);
         setOperator(Operator.getFor(operator));
     }
 
     public void activationRequestSent() {
         setStatus(SubscriptionStatus.PENDING_ACTIVATION);
     }
 
     public void deactivationRequestSent() {
         setStatus(SubscriptionStatus.PENDING_DEACTIVATION);
     }
 
     public void deactivationRequestReceived() {
         setStatus(SubscriptionStatus.DEACTIVATION_REQUEST_RECEIVED);
     }
 
     public void deactivate() {
         if (isSubscriptionCompletionRequestSent())
             setStatus(SubscriptionStatus.COMPLETED);
         else
             setStatus(SubscriptionStatus.DEACTIVATED);
     }
 
     @JsonIgnore
     public boolean isSubscriptionCompletionRequestSent() {
         return SubscriptionStatus.PENDING_COMPLETION.equals(status);
     }
 
     public void complete() {
         setStatus(SubscriptionStatus.PENDING_COMPLETION);
     }
 
     public DateTime endDate() {
         return getStartDate().plusWeeks(getPack().getTotalWeeks());
     }
 
     @JsonIgnore
     public boolean isInDeactivatedState() {
         return status.isInDeactivatedState();
     }
 
     @JsonIgnore
     public DateTime getCurrentWeeksMessageExpiryDate() {
         return startDate.plusWeeks(getWeeksElapsedAfterStartDate() + 1);
     }
 
     @JsonIgnore
     public int getWeeksElapsedAfterStartDate() {
         return Weeks.weeksBetween(startDate, DateTime.now()).getWeeks();
     }
 
     @JsonIgnore
     public int getCurrentWeekOfSubscription() {
 //        if (!status.isActive()) return -1;
         return pack.getStartWeek() + getWeeksElapsedAfterStartDate();
     }
 
     /*
      * Returns the next week number for this subscription devoid in absolute terms, ie. it is
      * devoid of pack start week initial count.
      */
     @JsonIgnore
     public int getNextWeekNumber() {
         DateTime now = DateTime.now();
         if (startDate.isAfter(now)) return 1;
 
         return
                 1 +                                             // First week
                 Weeks.weeksBetween(startDate, now).getWeeks() + // Weeks elapsed
                 1;                                              // Next week increment
     }
 
     public boolean hasBeenActivated() {
         return status.hasBeenActivated();
     }
 
     public void setMsisdn(String msisdn) {
         this.msisdn = msisdn;
     }
 
     @JsonIgnore
     public boolean isLateSubscription() {
         return startDate.isBefore(creationDate);
     }
 
     @JsonIgnore
     public DateTime getStartDateForSubscription(DateTime activatedOn) {
        return startDate.plus(activatedOn.getMillis() - creationDate.getMillis());
     }
 }
