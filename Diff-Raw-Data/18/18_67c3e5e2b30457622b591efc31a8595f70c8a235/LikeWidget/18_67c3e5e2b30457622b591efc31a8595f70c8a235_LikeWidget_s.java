 /*
  * Copyright (c) 2010 Lockheed Martin Corporation
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.eurekastreams.web.client.ui.common.stream.renderers;
 
 import org.eurekastreams.server.action.request.stream.SetActivityLikeRequest;
 import org.eurekastreams.server.action.request.stream.SetActivityLikeRequest.LikeActionType;
 import org.eurekastreams.web.client.model.ActivityLikeModel;
 import org.eurekastreams.web.client.ui.pages.master.StaticResourceBundle;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.ui.Anchor;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.FlowPanel;
 
 /**
  * Widget for liking activity.
  */
 public class LikeWidget extends Composite
 {
     /** Current state of activity. */
     private Boolean liked;
 
     /**
      * The widget.
      */
     private FlowPanel widget = new FlowPanel();
 
     /**
      * The like link.
      */
     private Anchor likeLink = new Anchor();
 
     /**
      * Default constructor.
      * 
      * @param isLiked
      *            whether its liked by the current user.
      * @param activityId
      *            the activity ID.
      */
     public LikeWidget(final Boolean isLiked, final Long activityId)
     {
         widget.addStyleName(StaticResourceBundle.INSTANCE.coreCss().likeWrapper());
         liked = isLiked;
 
         likeLink.addStyleName(StaticResourceBundle.INSTANCE.coreCss().linkedLabel());
 
         likeLink.setText(isLiked ? "Unlike" : "Like");
 
         widget.add(likeLink);
 
         likeLink.addClickHandler(new ClickHandler()
         {
             public void onClick(final ClickEvent event)
             {
                 final LikeActionType actionType = liked ? LikeActionType.REMOVE_LIKE : LikeActionType.ADD_LIKE;
                 liked = !liked;
                 likeLink.setText(liked ? "Unlike" : "Like");
                 ActivityLikeModel.getInstance().update(new SetActivityLikeRequest(activityId, actionType));
             }
 
         });
 
         initWidget(widget);
     }
 }
