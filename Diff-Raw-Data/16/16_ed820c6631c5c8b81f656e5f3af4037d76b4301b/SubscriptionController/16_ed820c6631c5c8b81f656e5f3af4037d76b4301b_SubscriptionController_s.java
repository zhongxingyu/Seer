 package org.motechproject.ananya.kilkari.web.controller;
 
 import org.apache.commons.lang.StringUtils;
 import org.motechproject.ananya.kilkari.domain.Subscription;
 import org.motechproject.ananya.kilkari.service.KilkariSubscriptionService;
 import org.motechproject.ananya.kilkari.web.mapper.SubscriptionDetailsMapper;
 import org.motechproject.ananya.kilkari.web.response.BaseResponse;
 import org.motechproject.ananya.kilkari.web.response.SubscriberResponse;
 import org.motechproject.ananya.kilkari.web.services.PublishService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import java.util.List;
 
 @Controller
 public class SubscriptionController {
 
     private KilkariSubscriptionService kilkariSubscriptionService;
     private PublishService publishService;
 
     @Autowired
     public SubscriptionController(KilkariSubscriptionService kilkariSubscriptionService, PublishService publishService) {
         this.kilkariSubscriptionService = kilkariSubscriptionService;
         this.publishService = publishService;
     }
 
     @RequestMapping(value = "/subscription", method = RequestMethod.GET)
     @ResponseBody
     public BaseResponse createSubscription(@RequestParam String msisdn, @RequestParam String pack, @RequestParam String channel) {
         publishService.createSubscription(msisdn, pack);
         return new BaseResponse("SUCCESS", "Subscription request submitted successfully");
     }
 
//    @RequestMapping(value = "/subscription", method = RequestMethod.POST)
//    @ResponseBody
//    public BaseResponse createSubscription(@RequestParam String channel, @RequestParam String data) {
//
//        publishService.createSubscription(msisdn, pack);
//        return new BaseResponse("SUCCESS", "Subscription request submitted successfully");
//    }

     @RequestMapping(value = "/subscriber", method = RequestMethod.GET)
     @ResponseBody
     public SubscriberResponse getSubscriptions(@RequestParam String msisdn, @RequestParam String channel) {
         SubscriberResponse subscriberResponse = new SubscriberResponse();
 
         if (!isValidMsisdn(msisdn)) return subscriberResponse.forInvalidMsisdn();
 
         List<Subscription> subscriptions = kilkariSubscriptionService.findByMsisdn(msisdn);
 
         if (subscriptions != null) {
             for (Subscription subscription : subscriptions)
                 subscriberResponse.addSubscriptionDetail(SubscriptionDetailsMapper.mapFrom(subscription));
         }
 
         return subscriberResponse;
     }
 
     private boolean isValidMsisdn(String msisdn) {
         return (StringUtils.length(msisdn) >= 10 && StringUtils.isNumeric(msisdn));
     }
 }
