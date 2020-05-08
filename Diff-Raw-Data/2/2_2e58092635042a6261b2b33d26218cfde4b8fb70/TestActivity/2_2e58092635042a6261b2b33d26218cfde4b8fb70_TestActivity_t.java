 package com.googlecode.mgwt.examples.showcase.client.activities.test;
 
 import com.google.gwt.event.shared.EventBus;
 import com.google.gwt.user.client.ui.AcceptsOneWidget;
 import com.google.gwt.xml.client.Document;
 import com.google.gwt.xml.client.XMLParser;
 import com.googlecode.mgwt.dom.client.event.tap.TapEvent;
 import com.googlecode.mgwt.dom.client.event.tap.TapHandler;
 import com.googlecode.mgwt.examples.showcase.client.ClientFactory;
 import com.googlecode.mgwt.examples.showcase.client.DetailActivity;
 import com.googlecode.mgwt.examples.showcase.client.activities.animation.AnimationSelectedEvent;
 import com.googlecode.mgwt.examples.showcase.client.event.ActionEvent;
 import com.googlecode.mgwt.examples.showcase.client.event.ActionNames;
 import com.googlecode.mgwt.mvp.client.MGWTAbstractActivity;
 import com.googlecode.mgwt.ui.client.widget.celllist.CellSelectedEvent;
 import com.googlecode.mgwt.ui.client.widget.celllist.CellSelectedHandler;
 
 public class TestActivity extends MGWTAbstractActivity {
 
     private final ClientFactory clientFactory;
 
     public TestActivity(ClientFactory clientFactory) {
         this.clientFactory = clientFactory;
 
     }
 
     @Override
     public void start(AcceptsOneWidget panel, final EventBus eventBus) {
         super.start(panel, eventBus);
         TestView view = clientFactory.getTestView();
 
         view.setTitle("Test");
         view.renderItems(clientFactory.getStationUtil().getAllStation());
 
         addHandlerRegistration(view.getBackButton().addTapHandler(new TapHandler() {
 
             @Override
             public void onTap(TapEvent event) {
                 ActionEvent.fire(eventBus, ActionNames.BACK);
 
             }
         }));
 
         addHandlerRegistration(view.getCellSelectedHandler().addCellSelectedHandler(
                 new CellSelectedHandler() {
 
                     @Override
                     public void onCellSelected(CellSelectedEvent event) {
                         int index = event.getIndex();
                         Document messageDom = XMLParser.parse(event.getTargetElement().toString());
                        String stationName = messageDom.getElementsByTagName("div").item(0).getFirstChild().getNodeValue();
                         StationSelectedEvent.fire(eventBus, stationName);
                     }
                 }));
 
         panel.setWidget(view);
     }
 }
