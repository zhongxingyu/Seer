 package org.motechproject.ananya.kilkari.domain;
 
 import org.apache.commons.lang.StringUtils;
 import org.motechproject.ananya.kilkari.exceptions.ValidationException;
 
public class SubscriptionRequest {
     private final String msisdn;
     private final String pack;
     private final String channel;
 
     public SubscriptionRequest(String msisdn, String pack, String channel) {
         this.msisdn = msisdn;
         this.pack = pack;
         this.channel = channel;
     }
 
     public Subscription getSubscription() {
         return new Subscription(msisdn, SubscriptionPack.getFor(pack));
     }
 
     public String getMsisdn() {
         return msisdn;
     }
 
     public String getPack() {
         return pack;
     }
 
     public String getChannel() {
         return channel;
     }
 
     public void validate() throws ValidationException {
         validateMsisdn(msisdn);
         validatePack(pack);
         validateChannel(channel);
     }
 
     private void validateChannel(String channel) throws ValidationException {
         if (!Channel.isValid(channel))
             throw new ValidationException(String.format("Invalid channel %s", channel));
     }
 
     private void validatePack(String subscriptionPack) throws ValidationException {
         if (!SubscriptionPack.isValid(subscriptionPack))
             throw new ValidationException(String.format("Invalid subscription pack %s", subscriptionPack));
     }
 
     private void validateMsisdn(String msisdn) throws ValidationException {
         if (!isValidMsisdn(msisdn))
             throw new ValidationException(String.format("Invalid msisdn %s", msisdn));
     }
 
     private boolean isValidMsisdn(String msisdn) {
         return (StringUtils.length(msisdn) >= 10 && StringUtils.isNumeric(msisdn));
     }
 
     public SubscriptionActivationRequest getSubscriptionActivationRequest() {
         return new SubscriptionActivationRequest(msisdn, SubscriptionPack.getFor(pack), Channel.getFor(channel));
     }
 }
