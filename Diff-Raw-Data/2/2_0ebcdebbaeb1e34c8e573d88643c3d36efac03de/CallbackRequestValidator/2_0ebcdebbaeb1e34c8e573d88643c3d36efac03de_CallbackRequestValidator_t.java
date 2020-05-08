 package org.motechproject.ananya.kilkari.web.validators;
 
 
 import org.apache.commons.lang.StringUtils;
 import org.motechproject.ananya.kilkari.domain.CallbackAction;
 import org.motechproject.ananya.kilkari.domain.CallbackStatus;
 import org.motechproject.ananya.kilkari.factory.SubscriptionStateHandlerFactory;
 import org.motechproject.ananya.kilkari.obd.domain.PhoneNumber;
 import org.motechproject.ananya.kilkari.obd.service.validator.Errors;
 import org.motechproject.ananya.kilkari.request.CallbackRequestWrapper;
 import org.motechproject.ananya.kilkari.subscription.domain.Operator;
 import org.motechproject.ananya.kilkari.subscription.domain.Subscription;
 import org.motechproject.ananya.kilkari.subscription.domain.SubscriptionPack;
 import org.motechproject.ananya.kilkari.subscription.domain.SubscriptionStatus;
 import org.motechproject.ananya.kilkari.subscription.service.SubscriptionService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 
 @Component
 public class CallbackRequestValidator {
 
     private final SubscriptionStateHandlerFactory subscriptionStateHandlerFactory;
     private final SubscriptionService subscriptionService;
 
     @Autowired
     public CallbackRequestValidator(SubscriptionStateHandlerFactory subscriptionStateHandlerFactory, SubscriptionService subscriptionService) {
         this.subscriptionStateHandlerFactory = subscriptionStateHandlerFactory;
         this.subscriptionService = subscriptionService;
     }
 
 	public Errors validate(CallbackRequestWrapper callbackRequestWrapper, boolean isReqFromSM) {
 		Errors errors = new Errors();
 		final boolean isValidCallbackAction = validateCallbackAction(callbackRequestWrapper, errors);
 		final boolean isValidCallbackStatus = validateCallbackStatus(callbackRequestWrapper, errors);
 		if(isReqFromSM){
 			final boolean isPackValid = validatePack(callbackRequestWrapper, errors);
 			if (isValidCallbackAction && isValidCallbackStatus && isPackValid) {
 				errors.addAll(validateSubscriptionRequestForSM(callbackRequestWrapper));
 			}
 		}else{
 			if (isValidCallbackAction && isValidCallbackStatus) {
 				errors.addAll(validateSubscriptionRequest(callbackRequestWrapper));
 			}
 		}
         validateMsisdn(callbackRequestWrapper, errors);
         validateOperator(callbackRequestWrapper, errors);
 
         return errors;
     }
 
 	private boolean validatePack(CallbackRequestWrapper callbackRequestWrapper,
 			Errors errors) {
 		if(callbackRequestWrapper.getPack()==null || StringUtils.isEmpty(callbackRequestWrapper.getPack().toString())){
 			errors.add(String.format("subscription pack cannot be empty or null"));
 			return false;
 		}
 		String pack = callbackRequestWrapper.getPack().toString();
 		if (!SubscriptionPack.isValid(pack)) {
 			errors.add(String.format("Invalid subscription pack %s", pack));
 			return false;
 		}
 		return true;
 	}
 
 	public static boolean validatePack (String pack, Errors errors){
 	if (!SubscriptionPack.isValid(pack)) {
 		errors.add(String.format("Invalid subscription pack %s", pack));
 		return false;
 	}
 	return true;
 }
 	public static void main(String[] args) {
 		System.out.println("print"+validatePack("BARI_KILKARI01",new Errors()));
 	}
 	
 	private Errors validateSubscriptionRequestForSM(CallbackRequestWrapper callbackRequestWrapper) {
 		Errors errors = new Errors();
 		final String requestStatus = callbackRequestWrapper.getStatus();
 		final String requestAction = callbackRequestWrapper.getAction();
 
 		if (subscriptionStateHandlerFactory.getHandler(callbackRequestWrapper) == null) {
 			errors.add(String.format("Invalid status %s for action %s for subscription %s", requestStatus, requestAction, callbackRequestWrapper.getSubscriptionId()));
 		}
 
 		if (CallbackAction.ACT.name().equals(requestAction) && (CallbackStatus.SUCCESS.getStatus().equals(requestStatus) || CallbackStatus.BAL_LOW.getStatus().equals(requestStatus))) {
 			boolean canNotBeActivated = false;
 			java.util.List<Subscription> subscriptions = subscriptionService.findByMsisdnAndPack(callbackRequestWrapper.getMsisdn(), callbackRequestWrapper.getPack());
 			if(subscriptions == null || subscriptions.isEmpty())	//checking if subscription is new or not
 				return errors;
 			else{
 				for (Subscription subscription : subscriptions) {
 					if(subscription.getStatus().canNotActivateForSM())
 						canNotBeActivated = true;	
 				}
 				if(canNotBeActivated)
 					errors.add(String.format("Cannot activate. The subscription state cannot be transitioned to ACTIVE state"));
 			}
 
 		}
 
 		//adding code to block multiple DCT request
 		
 		
 		if (CallbackAction.DCT.name().equals(requestAction)) {
 			boolean canBeDeactivated = false;
 			java.util.List<Subscription> subscriptionsByPack = subscriptionService.findByMsisdnAndPack(callbackRequestWrapper.getMsisdn(), callbackRequestWrapper.getPack());
 			if(subscriptionsByPack == null || subscriptionsByPack.isEmpty()){
 				errors.add(String.format("No subscription for msisdn: %s and pack: %s", callbackRequestWrapper.getMsisdn(), callbackRequestWrapper.getPack()));
 				return errors;
 			}else{
 				for (Subscription subscription : subscriptionsByPack) {
 					if(subscription.getStatus().canDeactivateOnRenewal() && CallbackStatus.BAL_LOW.getStatus().equals(requestStatus))
 						canBeDeactivated = true;	
 					if(subscription.getStatus().canTransitionTo(SubscriptionStatus.DEACTIVATED))
 						canBeDeactivated = true;
 				}
 				if(!canBeDeactivated)
					errors.add(String.format("Cannot deactivate on renewal. present subscription status cannot be transitioned to deactivated state"));
 			}
 			return errors;		
 		}
 
 		if (CallbackAction.REN.name().equals(requestAction)) {
 			boolean canBeRenewed = false;
 			java.util.List<Subscription> subscriptionsByPack = subscriptionService.findByMsisdnAndPack(callbackRequestWrapper.getMsisdn(), callbackRequestWrapper.getPack());
 			if(subscriptionsByPack == null || subscriptionsByPack.isEmpty()){
 				errors.add(String.format("No subscription for msisdn: %s and pack: %s", callbackRequestWrapper.getMsisdn(), callbackRequestWrapper.getPack()));
 				return errors;
 			}
 			else{
 				for (Subscription subscription : subscriptionsByPack) {
 					if(subscription.getStatus().canRenew())
 						canBeRenewed = true;	
 				}
 				if(!canBeRenewed)
 					errors.add(String.format("Cannot renew. Subscription status is not ACTIVE or SUSPENDED"));
 			}
 		}
 
 		return errors;
 	}
 
 
     private Errors validateSubscriptionRequest(CallbackRequestWrapper callbackRequestWrapper) {
         Errors errors = new Errors();
         final String requestStatus = callbackRequestWrapper.getStatus();
         final String requestAction = callbackRequestWrapper.getAction();
 
         if (subscriptionStateHandlerFactory.getHandler(callbackRequestWrapper) == null) {
             errors.add(String.format("Invalid status %s for action %s for subscription %s", requestStatus, requestAction, callbackRequestWrapper.getSubscriptionId()));
         }
 
         Subscription subscription = subscriptionService.findBySubscriptionId(callbackRequestWrapper.getSubscriptionId());
         if (subscription == null) {
             errors.add(String.format("No subscription for subscriptionId : %s", callbackRequestWrapper.getSubscriptionId()));
             return errors;
         }
 
 
         if (CallbackAction.REN.name().equals(requestAction)) {
             final SubscriptionStatus subscriptionStatus = subscription.getStatus();
             if (!subscriptionStatus.canRenew()) {
                 errors.add(String.format("Cannot renew. Subscription %s in %s status", callbackRequestWrapper.getSubscriptionId(), subscriptionStatus));
             }
         }
 
         if (CallbackAction.DCT.name().equals(requestAction) && CallbackStatus.BAL_LOW.getStatus().equals(requestStatus)) {
             final SubscriptionStatus subscriptionStatus = subscription.getStatus();
             if (!subscriptionStatus.canDeactivateOnRenewal())
                 errors.add(String.format("Cannot deactivate on renewal. Subscription %s in %s status", callbackRequestWrapper.getSubscriptionId(), subscriptionStatus));
         }
 
         if (CallbackAction.ACT.name().equals(requestAction) && (CallbackStatus.SUCCESS.getStatus().equals(requestStatus) || CallbackStatus.BAL_LOW.getStatus().equals(requestStatus))) {
             final SubscriptionStatus subscriptionStatus = subscription.getStatus();
             if (!subscriptionStatus.canActivate())
                 errors.add(String.format("Cannot activate. Subscription %s in %s status", callbackRequestWrapper.getSubscriptionId(), subscriptionStatus));
         }
 
         return errors;
     }
 
 
     private void validateOperator(CallbackRequestWrapper callbackRequestWrapper, Errors errors) {
         String operator = callbackRequestWrapper.getOperator();
         if (!Operator.isValid(operator))
             errors.add(String.format("Invalid operator %s for subscription %s", callbackRequestWrapper.getOperator(), callbackRequestWrapper.getSubscriptionId()));
     }
 
     private void validateMsisdn(CallbackRequestWrapper callbackRequestWrapper, Errors errors) {
         String msisdn = callbackRequestWrapper.getMsisdn();
         if (PhoneNumber.isNotValid(msisdn))
             errors.add(String.format("Invalid msisdn %s for subscription id %s", msisdn, callbackRequestWrapper.getSubscriptionId()));
     }
 
     private boolean validateCallbackAction(CallbackRequestWrapper callbackRequestWrapper, Errors errors) {
         String callbackAction = callbackRequestWrapper.getAction();
         if (!CallbackAction.isValid(callbackAction)) {
             errors.add(String.format("Invalid callbackAction %s  for subscription %s", callbackAction, callbackRequestWrapper.getSubscriptionId()));
             return false;
         }
         return true;
     }
 
     private boolean validateCallbackStatus(CallbackRequestWrapper callbackRequestWrapper, Errors errors) {
         String callbackStatus = callbackRequestWrapper.getStatus();
         if (!CallbackStatus.isValid(callbackStatus)) {
             errors.add(String.format("Invalid callbackStatus %s for subscription %s", callbackStatus, callbackRequestWrapper.getSubscriptionId()));
             return false;
         }
         return true;
     }
 }
