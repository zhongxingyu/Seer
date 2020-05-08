 package org.motechproject.ananya.kilkari.subscription.service.mapper;
 
 import org.apache.commons.lang.math.NumberUtils;
 import org.motechproject.ananya.kilkari.contract.request.SubscriberLocation;
 import org.motechproject.ananya.kilkari.contract.request.SubscriptionReportRequest;
 import org.motechproject.ananya.kilkari.subscription.domain.Channel;
 import org.motechproject.ananya.kilkari.subscription.domain.Subscription;
 import org.motechproject.ananya.kilkari.subscription.request.OMSubscriptionRequest;
 import org.motechproject.ananya.kilkari.subscription.service.request.Location;
 import org.motechproject.ananya.kilkari.subscription.service.request.Subscriber;
 
 public class SubscriptionMapper {
 
     public OMSubscriptionRequest createOMSubscriptionRequest(Subscription subscription, Channel channel) {
         return new OMSubscriptionRequest(subscription.getMsisdn(), subscription.getPack(), channel, subscription.getSubscriptionId());
     }
 
     public SubscriptionReportRequest createSubscriptionCreationReportRequest(Subscription subscription, Channel channel, Location location, Subscriber subscriber) {
         SubscriberLocation subscriberLocation = new SubscriberLocation(location.getDistrict(), location.getBlock(), location.getPanchayat());
         SubscriptionReportRequest subscriptionReportRequest = new SubscriptionReportRequest();

        subscriptionReportRequest.setSubscriptionId(subscription.getSubscriptionId());
         subscriptionReportRequest.setChannel(channel.name());
        subscriptionReportRequest.setMsisdn(NumberUtils.createLong(subscription.getMsisdn()));
        subscriptionReportRequest.setPack(subscription.getPack().name());
         subscriptionReportRequest.setName(subscriber.getBeneficiaryName());
        subscriptionReportRequest.setAgeOfBeneficiary(subscriber.getBeneficiaryAge());
         subscriptionReportRequest.setCreatedAt(subscription.getCreationDate());
        subscriptionReportRequest.setSubscriptionStatus(subscription.getStatus().name());
         subscriptionReportRequest.setEstimatedDateOfDelivery(subscriber.getExpectedDateOfDelivery());
        subscriptionReportRequest.setDateOfBirth(subscriber.getDateOfBirth());
         subscriptionReportRequest.setLocation(subscriberLocation);
        subscriptionReportRequest.setOperator(subscription.getOperator().name());
        subscriptionReportRequest.setStartDate(subscription.getStartDate());
 
         return subscriptionReportRequest;
     }
 }
