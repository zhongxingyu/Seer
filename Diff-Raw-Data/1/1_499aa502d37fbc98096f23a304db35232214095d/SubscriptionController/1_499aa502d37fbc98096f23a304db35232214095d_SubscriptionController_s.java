 package org.motechproject.ananya.reports.web.kilkari.controller;
 
 import org.motechproject.ananya.reports.kilkari.contract.request.*;
 import org.motechproject.ananya.reports.kilkari.contract.response.SubscriberResponse;
 import org.motechproject.ananya.reports.kilkari.domain.dimension.Subscription;
 import org.motechproject.ananya.reports.kilkari.service.CampaignScheduleAlertService;
 import org.motechproject.ananya.reports.kilkari.service.SubscriberService;
 import org.motechproject.ananya.reports.kilkari.service.SubscriptionService;
 import org.motechproject.ananya.reports.kilkari.service.SubscriptionStatusMeasureService;
 import org.motechproject.ananya.reports.web.kilkari.controller.mapper.SubscriberResponseMapper;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.*;
 
 import java.util.ArrayList;
 import java.util.List;
 
 @Controller
 public class SubscriptionController {
 
     private SubscriptionStatusMeasureService subscriptionStatusMeasureService;
     private SubscriptionService subscriptionService;
     private SubscriberService subscriberService;
     private CampaignScheduleAlertService campaignScheduleAlertService;
 
     @Autowired
     public SubscriptionController(SubscriptionStatusMeasureService subscriptionStatusMeasureService,
                                   SubscriptionService subscriptionService,
                                   SubscriberService subscriberService, CampaignScheduleAlertService campaignScheduleAlertService) {
         this.subscriptionStatusMeasureService = subscriptionStatusMeasureService;
         this.subscriptionService = subscriptionService;
         this.subscriberService = subscriberService;
         this.campaignScheduleAlertService = campaignScheduleAlertService;
     }
 
     @RequestMapping(method = RequestMethod.POST, value = "/subscription")
     @ResponseBody
     public void createSubscription(@RequestBody SubscriptionReportRequest subscriptionReportRequest) {
         subscriptionStatusMeasureService.createSubscription(subscriptionReportRequest);
     }
 
     @RequestMapping(method = RequestMethod.PUT, value = "/subscriber/{subscriptionId}")
     @ResponseBody
     public void updateSubscriber(@RequestBody SubscriberReportRequest subscriberReportRequest, @PathVariable("subscriptionId") String subscriptionId) {
         subscriberService.update(subscriberReportRequest, subscriptionId);
     }
 
     @RequestMapping(method = RequestMethod.PUT, value = "/subscription/{subscriptionId}")
     @ResponseBody
     public void updateSubscription(@RequestBody SubscriptionStateChangeRequest subscriptionStateChangeRequest, @PathVariable String subscriptionId) {
         subscriptionStatusMeasureService.update(subscriptionStateChangeRequest);
     }
 
     @RequestMapping(method = RequestMethod.GET, value = "/subscriber")
     @ResponseBody
     public List<SubscriberResponse> getSubscriberDetails(@RequestParam String msisdn) {
         List<Subscription> subscriptionList = subscriptionService.findByMsisdn(msisdn);
         List<SubscriberResponse> subscriberResponseList = new ArrayList<>();
         for (Subscription subscription : subscriptionList) {
             subscriberResponseList.add(SubscriberResponseMapper.mapFrom(subscription));
         }
         return subscriberResponseList;
     }
 
     @RequestMapping(method = RequestMethod.GET, value = "/subscription/subscriber/{subscriptionId}")
     @ResponseBody
     public SubscriberResponse getSubscriber(@PathVariable String subscriptionId) {
         Subscription subscription = subscriptionService.fetchFor(subscriptionId);
         return SubscriberResponseMapper.mapFrom(subscription);
     }
 
     @RequestMapping(method = RequestMethod.POST, value = "/subscription/changemsisdn")
     public void updateNewEarlySubscriptionForChangedMsisdn(@RequestBody SubscriberChangeMsisdnReportRequest subscriberChangeMsisdnReportRequest) {
         subscriptionStatusMeasureService.changeMsisdnForNewEarlySubscription(subscriberChangeMsisdnReportRequest);
     }
 
     @RequestMapping(method = RequestMethod.POST, value = "/subscription/campaignScheduleAlert")
     @ResponseBody
     public void createCampaignScheduleAlert(@RequestBody CampaignScheduleAlertRequest campaignScheduleAlertRequest) {
         campaignScheduleAlertService.createCampaignScheduleAlert(campaignScheduleAlertRequest);
     }
 }
