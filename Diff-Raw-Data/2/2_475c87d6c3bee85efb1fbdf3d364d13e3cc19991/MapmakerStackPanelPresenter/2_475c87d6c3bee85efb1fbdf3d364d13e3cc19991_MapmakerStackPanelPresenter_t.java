 /**
  * Copyright 2011 Jason Ferguson.
  * <p/>
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * <p/>
  * http://www.apache.org/licenses/LICENSE-2.0
  * <p/>
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package org.jason.mapmaker.client.presenter;
 
 import com.google.gwt.event.dom.client.ChangeEvent;
 import com.google.gwt.event.dom.client.ChangeHandler;
 import com.google.gwt.event.shared.EventBus;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.inject.Inject;
 import com.gwtplatform.dispatch.shared.DispatchAsync;
 import com.gwtplatform.mvp.client.HasUiHandlers;
 import com.gwtplatform.mvp.client.PresenterWidget;
 import com.gwtplatform.mvp.client.View;
 import com.gwtplatform.mvp.client.proxy.RevealRootPopupContentEvent;
 import org.jason.mapmaker.client.event.*;
 import org.jason.mapmaker.client.presenter.dataVersions.ManageDataVersionsPresenter;
 import org.jason.mapmaker.client.presenter.featuresMetadata.ManageFeaturesMetadataPresenter;
 import org.jason.mapmaker.client.presenter.help.DisplayHelpPresenter;
 import org.jason.mapmaker.client.presenter.shapefileMetadata.ManageShapefileMetadataPresenter;
 import org.jason.mapmaker.client.view.StackPanelUiHandlers;
 import org.jason.mapmaker.shared.action.*;
 import org.jason.mapmaker.shared.result.*;
 import org.jason.mapmaker.shared.util.GeographyUtils;
 import org.jason.mapmaker.shared.util.MtfccUtil;
 
 import java.util.List;
 import java.util.Map;
 
 /**
  * Presenter for the StackPanelView
  * <p/>
  * This class is responsible for handling the interaction with the model for the MapmakerStackPanelView, which is
  * the most complicated one in the app. Everything is set up in the onBind() method, including:
  * <p/>
  * - registering the handler for disabling the Redraw Map button (one of those things that sounds minor but isn't)
  * - populating the borderTypeListBox with the available border types that have been uploaded
  * - populating the primaryListBox with the list of states that are available for the given border type
  * - populating the secondaryListBox with counties if the BTLB is county-based and the state has been selected
  * - populating the tertiaryListBox with the individual features that can be selected (county names, congressional
  * districts, etc)
  *
  * @author Jason Ferguson
  * @since 0.1
  */
 public class MapmakerStackPanelPresenter extends PresenterWidget<MapmakerStackPanelPresenter.MyView>
         implements StackPanelUiHandlers {
 
     public interface MyView extends View, HasUiHandlers<StackPanelUiHandlers> {
 
         /**
          * Return the BorderType Listbox, which contains all of the available border types which can be displayed in
          * the application
          *
          * @return ListBox
          */
         ListBox getBorderTypeListBox();
 
         /**
          * Return the Primary Listbox, which usually contains the states that the selected BorderType is available for
          *
          * @return Listbox
          */
         ListBox getPrimaryListBox();
 
         /**
          * Return the Secondary Listbox, which usually contains the counties that the selected BorderType is available
          * for (for county-based features only)
          *
          * @return Listbox
          */
         ListBox getSecondaryListBox();
 
         /**
          * Return the Tertiary Listbox, which usually contains the specific items that the selected BorderType is
          * available for (excluding States and Counties themselves)
          *
          * @return Listbox
          */
         ListBox getTertiaryListBox();
 
         /**
          * Convenience message for displaying Window.alert() messages
          *
          * @param msg String containing message to display in a Javascript alert
          */
         void displayAngryMessage(String msg);
 
         /**
          * Return the button used to redraw the map on the screen
          *
          * @return Button
          */
         Button getRedrawMapButton();
 
         /**
          * Return the Listbox containing the USGS features which can be displayed on the map
          *
          * @return Listbox
          */
         ListBox getFeaturesListBox();
 
     }
 
     private DispatchAsync dispatch;
     private DisplayHelpPresenter displayHelpPresenter;
     private ManageShapefileMetadataPresenter manageShapefileMetadataPresenter;
     private ManageFeaturesMetadataPresenter manageFeaturesMetadataPresenter;
     private ManageDataVersionsPresenter manageDataVersionsPresenter;
 
     @Inject
     public MapmakerStackPanelPresenter(EventBus eventBus,
                                        MyView view,
                                        DispatchAsync dispatchAsync,
                                        DisplayHelpPresenter displayHelpPresenter,
                                        ManageShapefileMetadataPresenter manageShapefileMetadataPresenter,
                                        ManageFeaturesMetadataPresenter manageFeaturesMetadataPresenter,
                                         ManageDataVersionsPresenter manageDataVersionsPresenter) {
         super(eventBus, view);
         this.dispatch = dispatchAsync;
         this.displayHelpPresenter = displayHelpPresenter;
         this.manageShapefileMetadataPresenter = manageShapefileMetadataPresenter;
         this.manageFeaturesMetadataPresenter = manageFeaturesMetadataPresenter;
         this.manageDataVersionsPresenter = manageDataVersionsPresenter;
 
         getView().setUiHandlers(this);
     }
 
     // since the interface doesn't change much, I'll do this in onBind(). If it changed regularly, I'd probably do it
     // in onReveal()
     @Override
     protected void onBind() {
         super.onBind();
 
         // Register the event to enable the Redraw Map button
         registerHandler(getEventBus().addHandler(EnableRedrawMapButtonEvent.TYPE, new EnableRedrawMapButtonHandler() {
             @Override
             public void onEnableRedrawMapButton(EnableRedrawMapButtonEvent event) {
                 getView().getRedrawMapButton().setEnabled(true);
             }
         }));
 
         // register the event to repopulate the BTLB and reset the primary/secondary/tertiary listboxen
         registerHandler(getEventBus().addHandler(RepopulateBorderTypeListboxEvent.TYPE, new RepopulateBorderTypeListboxHandler() {
             @Override
             public void onRepopulateBorderTypeListBox(RepopulateBorderTypeListboxEvent event) {
                 populateBorderTypeListBox();
             }
         }));
 
         registerHandler(getEventBus().addHandler(RepopulateFeatureTypeListboxEvent.TYPE, new RepopulateFeatureTypeListboxHandler() {
             @Override
             public void doRepopulateFeatureTypeListbox(RepopulateFeatureTypeListboxEvent event) {
                 populateFeaturesListBox();
             }
         }));
 
         populateBorderTypeListBox();// initially populate the BTLB
         populateFeaturesListBox();  // TODO: do we really want to do this before selecting a state?
 
         // change handler to update the listboxes when the BTLB changes
         getView().getBorderTypeListBox().addChangeHandler(new ChangeHandler() {
 
             @Override
             public void onChange(ChangeEvent event) {
                 getView().getPrimaryListBox().clear();
                 getView().getSecondaryListBox().clear();
                 getView().getTertiaryListBox().clear();
 
                 populatePrimaryListBox();
                 getView().getSecondaryListBox().addItem("-- Select A State --");
 
                 // if a county-based feature is selected, enable the county box
                 int mtfccValue = getView().getBorderTypeListBox().getSelectedIndex();
                 String mtfcc = getView().getBorderTypeListBox().getValue(mtfccValue);
 
                 if (MtfccUtil.countyBasedMtfccs.contains(mtfcc)) {
                     getView().getSecondaryListBox().setEnabled(true);
                 } else {
                     getView().getSecondaryListBox().setEnabled(false);
                 }
 
                 getView().getPrimaryListBox().setEnabled(true);
             }
         });
 
         getView().getPrimaryListBox().addChangeHandler(new ChangeHandler() {
             @Override
             public void onChange(ChangeEvent event) {
 
                 getView().getSecondaryListBox().clear();
                 getView().getTertiaryListBox().clear();
 
                 // as usual, determine the mtfcc from the BTLB
                 String mtfcc = getBTLBValue();
 
                 // make sure that if it's just a state, we don't do anything
                 if (mtfcc.equals(GeographyUtils.MTFCC.STATE)) {
                     return; // exit onChange() so that we don't process via any call to GeographyUtils.isStateBasedMtfcc()
                 }
 
                 if (mtfcc.equals(GeographyUtils.MTFCC.COUNTY)) {
 
                     // okay, I'm violating DRY. Hate me if you want.
 
                     String stateFP = getPLBValue();
 
                     dispatch.execute(new GetLocationsByStateAndMtfccAction(stateFP, GeographyUtils.MTFCC.COUNTY), new AsyncCallback<GetLocationsByStateAndMtfccResult>() {
                         @Override
                         public void onFailure(Throwable throwable) {
                             getView().getSecondaryListBox().setEnabled(false);
                             throwable.printStackTrace();
                         }
 
                         @Override
                         public void onSuccess(GetLocationsByStateAndMtfccResult result) {
 
                             getView().getSecondaryListBox().clear();
                             getView().getSecondaryListBox().addItem("-- Select County --");
                             Map<String, String> resultMap = result.getResult();
                             resultMap.remove(null);
                             for (String key : resultMap.keySet()) {
                                 getView().getSecondaryListBox().addItem(resultMap.get(key), key);
                             }
                             getView().getSecondaryListBox().setEnabled(true);
 
                         }
                     });
 
                     return;
                 }
 
                 // if it's a state-based mtfcc, populate the TLB with the location names
                 if (GeographyUtils.isStateBasedMtfcc(mtfcc)) {
 
                     String stateFP = getPLBValue();
 
                     dispatch.execute(new GetStateBasedLocationsAction(mtfcc, stateFP), new AsyncCallback<GetStateBasedLocationsResult>() {
                         @Override
                         public void onFailure(Throwable caught) {
                             caught.printStackTrace();
                         }
 
                         @Override
                         public void onSuccess(GetStateBasedLocationsResult result) {
                             getView().getTertiaryListBox().setEnabled(true);
                             Map<String, String> resultMap = result.getResult();
                             resultMap.remove(null);
                             resultMap.remove(getPLBValue());
                             for (String key : resultMap.keySet()) {
                                 getView().getTertiaryListBox().addItem(resultMap.get(key), key);
                             }
                         }
                     });
 
                     return;
                 }
 
                 if (GeographyUtils.isCountyBasedMtfcc(mtfcc)) {
 
                     String stateFP = getPLBValue();
 
                     dispatch.execute(new GetLocationsByStateAndMtfccAction(stateFP, GeographyUtils.MTFCC.COUNTY), new AsyncCallback<GetLocationsByStateAndMtfccResult>() {
                         @Override
                         public void onFailure(Throwable throwable) {
                             getView().getSecondaryListBox().setEnabled(false);
                             throwable.printStackTrace();
                         }
 
                         @Override
                         public void onSuccess(GetLocationsByStateAndMtfccResult result) {
 
                             getView().getSecondaryListBox().clear();
                             getView().getSecondaryListBox().addItem("-- Select County --");
                             Map<String, String> resultMap = result.getResult();
                             resultMap.remove(null);
                             for (String key : resultMap.keySet()) {
                                 getView().getSecondaryListBox().addItem(resultMap.get(key), key);
                             }
                             getView().getSecondaryListBox().setEnabled(true);
 
                         }
                     });
 
                     return; // exit function to ensure that we don't process via isCountyBasedMtfcc()
                 }
 
             }
         });
 
         getView().getSecondaryListBox().addChangeHandler(new ChangeHandler() {
             @Override
             public void onChange(ChangeEvent event) {
 
                 // as usual, determine the mtfcc from the BTLB
                 String mtfcc = getBTLBValue();
 
 
                 // if the mtfcc is a county based feature but is not a county...
                 if (!mtfcc.equals(GeographyUtils.MTFCC.COUNTY) && GeographyUtils.isCountyBasedMtfcc(mtfcc)) {
 
                     // get the state and county FIPS codes
                     String geoId = getSLBValue();
                     String stateFP = geoId.substring(0,2);
                     String countyFP = geoId.substring(2);
 
                     dispatch.execute(new GetCountyBasedLocationsAction(mtfcc, stateFP, countyFP), new AsyncCallback<GetCountyBasedLocationsResult>() {
                         @Override
                         public void onFailure(Throwable throwable) {
                             throwable.printStackTrace();
                         }
 
                         @Override
                         public void onSuccess(GetCountyBasedLocationsResult result) {
 
                             getView().getTertiaryListBox().clear();
                             getView().getTertiaryListBox().addItem("-- Select Feature --");
                             Map<String, String> resultMap = result.getResult();
                             for (String fp : resultMap.keySet()) {
                                 getView().getTertiaryListBox().addItem(resultMap.get(fp), fp);
                             }
                         }
                     });
 
                     getView().getTertiaryListBox().setEnabled(true);
                 }
             }
         });
     }
 
     public void populateBorderTypeListBox() {
 
         getView().getBorderTypeListBox().clear(); // need this for when the method is called via an event
 
         dispatch.execute(new GetMtfccTypesAction(), new AsyncCallback<GetMtfccTypesResult>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
             }
 
             @Override
             public void onSuccess(final GetMtfccTypesResult result) {
                 Map<String, String> resultMap = result.getResult();
 
                 // we can get away with this as the implementation is actually a LinkedHashMap. This is
                 // much cleaner than the previous code.
                 for (String key : resultMap.keySet()) {
                     getView().getBorderTypeListBox().addItem(key, resultMap.get(key));
                 }
 
                 getView().getPrimaryListBox().clear();
                 getView().getSecondaryListBox().clear();
 
                 getView().getPrimaryListBox().addItem("-- Select A Border Type --", "");
                 getView().getSecondaryListBox().addItem("-- Select A Border Type --", "");
             }
         });
     }
 
     /**
      * Populate the PrimaryListBox() - the PLB is only supposed to be populated with states that have the border type
      * selected in the BTLB loaded.
      */
     public void populatePrimaryListBox() {
 
         // get the mtfcc code from the BTLB
         int mtfccValue = getView().getBorderTypeListBox().getSelectedIndex();
         String mtfcc = getView().getBorderTypeListBox().getValue(mtfccValue);
 
         // dispatch to get the states that have borders from the given mtfcc code loaded
         dispatch.execute(new GetStatesByMtfccAction(mtfcc), new AsyncCallback<GetStatesByMtfccResult>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
             }
 
             @Override
             public void onSuccess(GetStatesByMtfccResult result) {
                 List<Map<String, String>> resultMapList = result.getResult();
 
                 if (resultMapList.size() > 0) {
 
                     getView().getPrimaryListBox().clear();
                     getView().getPrimaryListBox().addItem("-- Select A State --", "");
 
                     for (Map<String, String> resultMap : resultMapList) {
                         getView().getPrimaryListBox().addItem(resultMap.get("name"), resultMap.get("stateFP"));
                     }
 
                     getView().getSecondaryListBox().clear();
                     getView().getSecondaryListBox().addItem("-- Select A State --", "");
                 } else {
                     Window.alert("No states available for selected Border type.");
                 }
 
             }
         });
     }
 
     private void populateFeaturesListBox() {
 
         dispatch.execute(new GetFeatureClassesAction(), new AsyncCallback<GetFeatureClassesResult>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
             }
 
             @Override
             public void onSuccess(GetFeatureClassesResult getFeatureClassesResult) {
 
                 getView().getFeaturesListBox().clear(); // just in case, we don't want to keep appending to the existing
                 getView().getFeaturesListBox().addItem("-- Select Feature --","-1");
                 List<String> result = getFeatureClassesResult.getResult();
                 for (String fc : result) {
                     getView().getFeaturesListBox().addItem(fc);
                 }
             }
         });
     }
 
     /**
      * StackPanelUiHandlers Implementation  *
      */
 
     @Override
     public void onDisplayHelp() {
         RevealRootPopupContentEvent.fire(this, displayHelpPresenter);
     }
 
     @Override
     public void onDisplayManageBorders() {
         RevealRootPopupContentEvent.fire(this, manageShapefileMetadataPresenter);
     }
 
     @Override
     public void onDisplayManageFeatures() {
         RevealRootPopupContentEvent.fire(this, manageFeaturesMetadataPresenter);
     }
 
     @Override
     public void onDisplayManageAvailableData() {
         RevealRootPopupContentEvent.fire(this, manageDataVersionsPresenter);
     }
 
     @Override
     public void onRedrawMap() {
 
         // figure out which MTFCC the user wants to see
         int mtfccValue = getView().getBorderTypeListBox().getSelectedIndex();
         if (mtfccValue == -1) {
             getView().displayAngryMessage("You must select a border type!");
             return;
         }
         String mtfcc = getView().getBorderTypeListBox().getValue(mtfccValue);
 
        String generatedGeoId;
         if (mtfcc.equals(GeographyUtils.MTFCC.STATE)) {
             generatedGeoId = getPLBValue();
         } else if (mtfcc.equals(GeographyUtils.MTFCC.COUNTY)) {
             generatedGeoId = getSLBValue();
         } else if (!mtfcc.equals(GeographyUtils.MTFCC.STATE) && GeographyUtils.isStateBasedMtfcc(mtfcc)) {
             generatedGeoId = getTLBValue();
         } else if (!mtfcc.equals(GeographyUtils.MTFCC.COUNTY) && GeographyUtils.isCountyBasedMtfcc(mtfcc)) {
             generatedGeoId = getTLBValue();
         } else {
             return;
         }
 
         // get the feature class name
         int fcnIndex = getView().getFeaturesListBox().getSelectedIndex();
         String featureClassName;
         if (fcnIndex != -1) {
             featureClassName = getView().getFeaturesListBox().getValue(fcnIndex);  // rot roh! throws arrayindexoutofbounds if no feature selected!
             getEventBus().fireEvent(new RedrawMapEvent(generatedGeoId, featureClassName));
         } else {
             getEventBus().fireEvent(new RedrawMapEvent(generatedGeoId));
         }
 
     }
 
     @Override
     public void doClearBorderTypeListBox() {
         getView().getBorderTypeListBox().clear();
     }
 
     @Override
     public void doClearPrimaryListBox() {
         getView().getPrimaryListBox().clear();
         getView().getPrimaryListBox().addItem("-- Select A Feature --");
     }
 
     @Override
     public void doClearSecondaryListBox() {
         getView().getSecondaryListBox().clear();
         getView().getSecondaryListBox().addItem("-- Select A State --");
     }
 
     @Override
     public void doClearTertiaryListBox() {
         getView().getTertiaryListBox().clear();
         getView().getTertiaryListBox().addItem("-- Select A County --");
     }
 
     private String getBTLBValue() {
         int index = getView().getBorderTypeListBox().getSelectedIndex();
         return getView().getBorderTypeListBox().getValue(index);
     }
 
     private String getPLBValue() {
         int index = getView().getPrimaryListBox().getSelectedIndex();
         return getView().getPrimaryListBox().getValue(index);
     }
 
     private String getSLBValue() {
         int index = getView().getSecondaryListBox().getSelectedIndex();
         return getView().getSecondaryListBox().getValue(index);
     }
 
     private String getTLBValue() {
         int index = getView().getTertiaryListBox().getSelectedIndex();
         return getView().getTertiaryListBox().getValue(index);
     }
 }
