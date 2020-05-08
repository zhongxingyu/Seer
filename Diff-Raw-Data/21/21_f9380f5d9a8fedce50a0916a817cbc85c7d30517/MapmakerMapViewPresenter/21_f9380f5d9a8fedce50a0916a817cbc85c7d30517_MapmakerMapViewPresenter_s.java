 /**
  * Copyright 2011 Jason Ferguson.
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
 package org.jason.mapmaker.client.presenter;
 
 import com.google.gwt.event.shared.EventBus;
 import com.google.gwt.user.client.Element;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.inject.Inject;
 import com.gwtplatform.dispatch.shared.DispatchAsync;
 import com.gwtplatform.mvp.client.PresenterWidget;
 import com.gwtplatform.mvp.client.View;
 import org.jason.mapmaker.client.event.EnableRedrawMapButtonEvent;
 import org.jason.mapmaker.client.event.RedrawMapEvent;
 import org.jason.mapmaker.client.event.RedrawMapHandler;
 import org.jason.mapmaker.shared.action.GetMapDataByGeoIdAction;
 import org.jason.mapmaker.shared.action.location.GetLocationDescriptionsAction;
 import org.jason.mapmaker.shared.model.Location;
 import org.jason.mapmaker.shared.result.GetMapDataByGeoIdResult;
 import org.jason.mapmaker.shared.result.location.GetLocationDescriptionsResult;
 
 import java.util.Map;
 
 /**
 * MapmakerMapViewPresenter.java
  *
  * @author Jason Ferguson
  */
public class MapmakerMapViewPresenter extends PresenterWidget<MapmakerMapViewPresenter.MyView> {
 
    public interface MyView extends View {
 
         /**
          * Prepares and draws the map by sorting the borderpoint list by unique id, then passing it and the bounding box to
          * the JSNI method for rendering.
          *
          * @param location    Location object to be rendered
          * @param boundingBox Map containing the bounding box for the Location to be rendered
          * @param e           div element where the map will be drawn
          */
         void prepareAndInitializeMap(Location location, Map<String, Double> boundingBox, Element e);
 
         void getLocationDescriptions(double lng, double lat);
 
         void doAlertMessage(String alertMessage);
     }
 
     private DispatchAsync dispatch;
 
     @Inject
     public MapmakerMapViewPresenter(EventBus eventBus, MyView view, DispatchAsync dispatch) {
         super(eventBus, view);
         this.dispatch = dispatch;
     }
 
     @Override
     protected void onBind() {
 
         // show a default map view
         getView().prepareAndInitializeMap(null, null, getView().asWidget().getElement());
 
         // register the handler for map redraw events
         registerHandler(getEventBus().addHandler(RedrawMapEvent.TYPE, new RedrawMapHandler() {
             @Override
             public void onRedrawMap(RedrawMapEvent event) {
 
                 String geoId = event.getGeoId();
                 String featureClassName = event.getFeatureClassType(); // might be null
 
                 dispatch.execute(new GetMapDataByGeoIdAction(geoId, featureClassName), new AsyncCallback<GetMapDataByGeoIdResult>() {
                     @Override
                     public void onFailure(Throwable throwable) {
                         throwable.printStackTrace();
                     }
 
                     @Override
                     public void onSuccess(GetMapDataByGeoIdResult result) {
                         getView().prepareAndInitializeMap(result.getLocation(), result.getBoundingBox(), getView().asWidget().getElement());
                         getEventBus().fireEvent(new EnableRedrawMapButtonEvent());
                     }
                 });
             }
         }));
     }
 
     public void doGetLocationDescriptions(double lng, double lat) {
 
         dispatch.execute(new GetLocationDescriptionsAction(lng, lat), new AsyncCallback<GetLocationDescriptionsResult>() {
             @Override
             public void onFailure(Throwable caught) {
                throw new UnsupportedOperationException();
             }
 
             @Override
             public void onSuccess(GetLocationDescriptionsResult result) {
                 Map<String, Location> resultMap = result.getResult();
                 StringBuffer message = new StringBuffer();
                 for (String key: resultMap.keySet()) {
                     message.append(key).append(": ").append(resultMap.get(key));
                 }
 
                 getView().doAlertMessage(message.toString());
             }
         });
     }
 }
