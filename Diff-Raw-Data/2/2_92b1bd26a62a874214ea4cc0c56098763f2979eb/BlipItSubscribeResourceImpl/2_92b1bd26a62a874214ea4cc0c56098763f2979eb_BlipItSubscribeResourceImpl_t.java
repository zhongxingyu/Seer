 /*
  * Copyright (c) 2010 BlipIt Committers
  * All rights reserved.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
  * implied. See the License for the specific language governing
  * permissions and limitations under the License.
  *
  * See also http://www.apache.org/licenses/LICENSE-2.0.html for an
  * explanation of the license and how it is applied.
  */
 
 package com.thoughtworks.blipit;
 
 import com.thoughtworks.blipit.domain.Alert;
 import com.thoughtworks.contract.common.ChannelCategory;
 import com.thoughtworks.contract.common.GetChannelsResponse;
 import com.thoughtworks.contract.subscribe.BlipItSubscribeResource;
 import com.thoughtworks.contract.subscribe.GetBlipsRequest;
 import com.thoughtworks.contract.subscribe.GetBlipsResponse;
 import com.thoughtworks.contract.subscribe.UserPrefs;
 import org.restlet.resource.Get;
 import org.restlet.resource.Post;
 
 import java.util.ArrayList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public class BlipItSubscribeResourceImpl extends BlipItCommonServerResource implements BlipItSubscribeResource {
     private static final Logger log = Logger.getLogger(BlipItSubscribeResourceImpl.class.getName());
 
     public BlipItSubscribeResourceImpl() {
         super();
     }
 
     @Get
     public String showMessage() {
         return "Hi there ! You've reached the BlipIt server !! I can only process HTTP post requests !!!";
     }
 
     @Post
     public GetBlipsResponse getBlips(GetBlipsRequest blipItRequest) {
         final GetBlipsResponse blipItResponse = new GetBlipsResponse();
         UserPrefs userPrefs = blipItRequest.getUserPrefs();
         if (userPrefs == null || userPrefs.getChannels() == null || userPrefs.getChannels().isEmpty()) return blipItResponse;
         final ArrayList<Alert> alerts = new ArrayList<Alert>();
         blipItRepository.filterAlerts(blipItRequest.getUserLocation(), userPrefs, new Utils.ResultHandler<Alert>() {
             public void onSuccess(Alert alert) {
                 blipItResponse.setSuccess();
                 alerts.add(alert);
             }
 
             public void onError(Throwable throwable) {
                 log.log(Level.SEVERE, "An error occurred while fetching alerts", throwable);
                 blipItResponse.setFailure(Utils.getBlipItError(throwable.getMessage()));
             }
         });
 
        if(blipItResponse.isFailure()) return blipItResponse;

         final ArrayList<IAlertFilter> alertFilters = GetAlertFilters(blipItRequest, userPrefs);
         for(IAlertFilter alertFilter : alertFilters)
             alertFilter.apply(alerts);
 
         for(Alert alert : alerts){
             blipItResponse.addBlips(alert.toBlip());
         }
         return blipItResponse;
     }
 
     private ArrayList<IAlertFilter> GetAlertFilters(GetBlipsRequest blipItRequest, UserPrefs userPrefs) {
         final ArrayList<IAlertFilter> alertFilters = new ArrayList<IAlertFilter>();
         alertFilters.add(new DistanceFilter(blipItRequest.getUserLocation(), userPrefs.getRadius()));
         return alertFilters;
     }
 
     @Get
     public GetChannelsResponse getAvailableChannels(ChannelCategory channelCategory) {
         return getChannels(channelCategory);
     }
 
 }
