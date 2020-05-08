 /**
  * Copyright 2013 Centretown Software, Dave Marsh
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package com.centretown.tuba.client.app.widget.request;
 
 import java.util.Map;
 import java.util.TreeMap;
 
 import com.centretown.tuba.client.event.VideoInfoEvent;
 import com.centretown.tuba.client.youtube.request.DecodeUtils;
 import com.centretown.tuba.client.youtube.request.YoutubeInfo;
 import com.gwtplatform.mvp.client.HasUiHandlers;
 import com.gwtplatform.mvp.client.PresenterWidget;
 import com.gwtplatform.mvp.client.View;
 import com.google.gwt.core.client.Callback;
 import com.google.inject.Inject;
 import com.google.web.bindery.event.shared.EventBus;
 
 public class RequestPresenter extends PresenterWidget<RequestPresenter.MyView>
     implements RequestUiHandlers {
 
   public interface MyView extends View, HasUiHandlers<RequestUiHandlers> {
     void go(String id, Callback<String, String> callback);
   }
 
   @Inject
   public RequestPresenter(final EventBus eventBus, final MyView view) {
     super(eventBus, view);
     getView().setUiHandlers(this);
   }
 
   @Override
   protected void onBind() {
     super.onBind();
   }
 
   private Callback<String, String> callback = new Callback<String, String>() {
     @Override
     public void onFailure(String reason) {
       onSuccess("&status=fail&reason=" + reason);
     }
 
     @Override
     public void onSuccess(String result) {
       YoutubeInfo info = new YoutubeInfo(result);
       VideoInfoEvent event = new VideoInfoEvent(info);
       getEventBus().fireEvent(event);
     }
   };
 
   @Override
   public void onGo(String input) {
     String id = null;
    // check for id
    if (input.length() == 11) {
       id = input;
    } else {
      // check for url
      Map<String, String> properties = new TreeMap<String, String>();
      DecodeUtils.decodeUrl(input, properties);
      id = properties.get("v");
    }
    if (id != null)
      getView().go(id, callback);
    else {
      String reason = "Unable to parse:" + input;
      YoutubeInfo info = new YoutubeInfo("&status=fail&reason=" + reason);
      VideoInfoEvent event = new VideoInfoEvent(info);
      getEventBus().fireEvent(event);
    }
   }
 }
