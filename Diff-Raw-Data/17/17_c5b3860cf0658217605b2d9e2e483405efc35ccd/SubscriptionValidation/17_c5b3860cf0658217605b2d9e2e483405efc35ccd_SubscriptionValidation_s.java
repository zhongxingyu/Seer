 package org.motechproject.ghana.mtn.process;
 
 import org.motechproject.ghana.mtn.billing.dto.BillingServiceRequest;
 import org.motechproject.ghana.mtn.billing.dto.BillingServiceResponse;
 import org.motechproject.ghana.mtn.billing.service.BillingService;
 import org.motechproject.ghana.mtn.domain.MessageBundle;
 import org.motechproject.ghana.mtn.domain.Subscription;
 import org.motechproject.ghana.mtn.matchers.ProgramTypeMatcher;
 import org.motechproject.ghana.mtn.repository.AllSubscriptions;
 import org.motechproject.ghana.mtn.service.SMSService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 import org.springframework.util.CollectionUtils;
 
 import java.util.List;
 
 import static ch.lambdaj.Lambda.having;
 import static ch.lambdaj.Lambda.on;
 import static ch.lambdaj.Lambda.select;
 
 @Component
 public class SubscriptionValidation extends BaseSubscriptionProcess implements ISubscriptionProcessFlow {
     private AllSubscriptions allSubscriptions;
     private BillingService billingService;
 
     @Autowired
     protected SubscriptionValidation(SMSService smsService, MessageBundle messageBundle,
                                      AllSubscriptions allSubscriptions,
                                      BillingService billingService) {
         super(smsService, messageBundle);
         this.allSubscriptions = allSubscriptions;
         this.billingService = billingService;
     }
 
     @Override
     public Boolean startFor(Subscription subscription) {
         String subscriberNumber = subscription.subscriberNumber();
         if (subscription.isNotValid()) {
             sendMessage(subscription, messageFor(MessageBundle.ENROLLMENT_FAILURE));
             return false;
         }
         if (hasActiveSubscription(subscriberNumber, subscription)) {
            sendMessage(subscription, messageFor(MessageBundle.ACTIVE_SUBSCRIPTION_PRESENT));
             return false;
         }
         BillingServiceRequest request = new BillingServiceRequest(subscriberNumber, subscription.getProgramType());
         BillingServiceResponse response = billingService.checkIfUserHasFunds(request);
         if (response.hasErrors()) {
             sendMessage(subscription, messageFor(response.getValidationErrors()));
             return false;
         }
         return true;
     }
 
     private boolean hasActiveSubscription(String subscriberNumber, Subscription subscription) {
         List<Subscription> activeSubscriptions = allSubscriptions.getAllActiveSubscriptionsForSubscriber(subscriberNumber);
         List<Subscription> subscriptions = select(activeSubscriptions, having(on(Subscription.class).getProgramType(),
                 new ProgramTypeMatcher(subscription.getProgramType())));
         return !CollectionUtils.isEmpty(subscriptions);
     }
 
     @Override
     public Boolean stopFor(Subscription subscription) {
         return true;
     }
 }
