 package com.scurab.gwt.rlw.client.presenter;
 
 import java.util.HashMap;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.shared.HandlerManager;
 import com.google.gwt.json.client.JSONNumber;
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.json.client.JSONParser;
 import com.google.gwt.json.client.JSONString;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.HTMLPanel;
 import com.scurab.gwt.rlw.client.DataServiceAsync;
 import com.scurab.gwt.rlw.client.RemoteLogWeb;
 import com.scurab.gwt.rlw.client.view.SettingsView;
 import com.scurab.gwt.rlw.shared.TableColumns;
 import com.scurab.gwt.rlw.shared.model.Device;
 import com.scurab.gwt.rlw.shared.model.Settings;
 
 public class TabSettingsPresenter extends TabBasePresenter {
 
     private DataServiceAsync mDataService;
     private HandlerManager mEventBus;
     private String mApp;
     private HTMLPanel mContainer;
     private Device mDevice;
     private SettingsView mDisplay;
     
     private Settings mSettings;
     
     public TabSettingsPresenter(DataServiceAsync dataService, HandlerManager eventBus, String appName, HTMLPanel tabPanel) {
         super(dataService, eventBus, appName, tabPanel);
         
         mDataService = dataService;
         mEventBus = eventBus;
         mApp = appName;
         mContainer = tabPanel;
         onCreateView();
         bind();
     }
     
     private void bind(){
         mDisplay.getDeleteDeviceSettings().addClickHandler(new ClickHandler() {
             @Override
             public void onClick(ClickEvent event) {
                 onDeleteDeviceSettingsClick();
             }
         });
         
         mDisplay.getSave().addClickHandler(new ClickHandler() {
             @Override
             public void onClick(ClickEvent event) {
                 onSaveClick();
             }
         });
     }
     
     protected void onSaveClick() {
         String text = mDisplay.getTextArea().getText().trim();
         if(text.length() > 0){//try check JSON validity
             try{
                 JSONParser.parseStrict(text);
             }catch(Exception e){
                 Window.alert("JSON Parsing error\n"+ e.getMessage());
                 return;
             }
         }
         
         if(mSettings == null){//create settings object if necessary
             mSettings = new Settings();
             mSettings.setAppName(mApp);
             if(mDevice != null){
                 mSettings.setDeviceID(mDevice.getDeviceID());
             }
         }
         mSettings.setJsonValue(text);
         String jsonParams = mSettings.toJson().toString();
         //start notify
         notifyStartDownloading(RemoteLogWeb.WORDS.Save());
         mDisplay.getSave().setEnabled(false);        
         //save
         mDataService.saveSettings(jsonParams, new AsyncCallback<Settings>() {
             
             @Override
             public void onSuccess(Settings result) {
                 mSettings = result;
                 notifyStopDownloading();
                 mDisplay.getSave().setEnabled(true);
             }
             
             @Override
             public void onFailure(Throwable caught) {
                 Window.alert(caught.getMessage());
                 notifyStopDownloading();
                 mDisplay.getSave().setEnabled(true);
             }
         });
         
     }
 
     protected void onDeleteDeviceSettingsClick() {
       //start notify
         notifyStartDownloading(RemoteLogWeb.WORDS.DeleteDeviceSettings());
         mDisplay.getDeleteDeviceSettings().setEnabled(false);   
         
         mDataService.deleteDeviceSettings(mApp, new AsyncCallback<Integer>() {
             @Override
             public void onSuccess(Integer result) {
                 Window.alert("Updated records:" + result);
                 mDisplay.getDeleteDeviceSettings().setEnabled(true);
                 notifyStopDownloading();
             }
             
             @Override
             public void onFailure(Throwable caught) {
                 Window.alert(caught.getMessage());
                 mDisplay.getDeleteDeviceSettings().setEnabled(true);
                 notifyStopDownloading();
             }
         });
     }
 
     protected void onCreateView(){
         mDisplay = new SettingsView();
         mContainer.add(mDisplay);        
     }
 
     public Device getDevice() {
         return mDevice;
     }
 
     public void setDevice(Device device) {
         mDevice = device;
         mDisplay.getTextArea().setText(null);
         mDisplay.getDeleteDeviceSettings().setVisible(device == null);
         onLoadingSettings();
     }
     
     public void onLoadingSettings(){
         JSONObject obj = new JSONObject();
         obj.put(TableColumns.SettingsAppName, new JSONString(mApp));
         if(mDevice != null){
             obj.put(TableColumns.SettingsDeviceID, new JSONNumber(mDevice.getDeviceID()));
         }
         notifyStartDownloading(RemoteLogWeb.WORDS.Settings());
         
         String json = obj.toString();
         mDataService.getSettings(json, new AsyncCallback<Settings>() {
             
             @Override
             public void onSuccess(Settings result) {
                 onLoadSettings(result);
                 notifyStopDownloading();
             }
             
             @Override
             public void onFailure(Throwable caught) {
                 Window.alert(caught.getMessage());
                 notifyStopDownloading();
             }
         });
     }
     
     public void onLoadSettings(Settings result){
         if(result != null){
            mSettings = result;
             mDisplay.getTextArea().setText(mSettings.getJsonValue());
         }else{
             mDisplay.getTextArea().setText(null);
         }
     }
 }
