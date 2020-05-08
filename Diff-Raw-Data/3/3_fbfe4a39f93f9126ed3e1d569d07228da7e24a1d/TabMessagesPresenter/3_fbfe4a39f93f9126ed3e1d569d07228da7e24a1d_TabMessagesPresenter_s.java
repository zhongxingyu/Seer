 package com.scurab.gwt.rlw.client.presenter;
 
 import com.google.gwt.event.dom.client.ChangeEvent;
 import com.google.gwt.event.dom.client.ChangeHandler;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.shared.HandlerManager;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.HTMLPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.TextArea;
 import com.scurab.gwt.rlw.client.DataServiceAsync;
 import com.scurab.gwt.rlw.client.RemoteLogWeb;
 import com.scurab.gwt.rlw.client.view.PushMessageView;
 import com.scurab.gwt.rlw.shared.model.Device;
 import com.scurab.gwt.rlw.shared.model.PushMessage;
 import com.scurab.gwt.rlw.shared.model.PushMessageRequest;
 import com.scurab.gwt.rlw.shared.model.PushMessageRespond;
 
 public class TabMessagesPresenter extends TabBasePresenter {
 
     private DataServiceAsync mDataService;
     private HandlerManager mEventBus;
     private String mApp;
     private HTMLPanel mContainer;
     private PushMessageView mDisplay;
     
     private Device mDevice;
     
     public TabMessagesPresenter(DataServiceAsync dataService, HandlerManager eventBus, String appName,
             HTMLPanel tabPanel) {
         super(dataService, eventBus, appName, tabPanel);
         
         mDataService = dataService;
         mEventBus = eventBus;
         mApp = appName;
         mContainer = tabPanel;
         
         init();
         bind();
         
         onSelectMessage(null);//simulate null value selection
     }
     
     private void init(){
         mDisplay = onCreateView();
         mContainer.add(mDisplay);
     }
     
     private void bind(){
         mDisplay.getSendButton().addClickHandler(new ClickHandler() {
             @Override
             public void onClick(ClickEvent event) {
                 onSendClick();
             }
         });
         
         ListBox lb = mDisplay.getMessageListBox();
         lb.addChangeHandler(new ChangeHandler() {
             @Override
             public void onChange(ChangeEvent event) {
                 onSelectMessage(getSelectedMessage());
             }
         });
         
         
     }
     
     private void initMessages(Device d){
         ListBox lb = mDisplay.getMessageListBox();
         lb.clear();
         
         if(d != null){
             String platform = d.getPlatform();
             
             lb.addItem("","");
             for(PushMessage pm : RemoteLogWeb.PUSH_MESSAGES){
                 if(!pm.isPlatformSupported(platform)){
                     continue;
                 }
                 if(pm.isOnlyForApp() && mApp == null){
                     continue;
                 }
                 lb.addItem(pm.getName());
             }
         }
         
         onSelectMessage(null);//simulate null value selection
     }
     
     protected PushMessage getSelectedMessage(){
         ListBox lb = mDisplay.getMessageListBox();
         int index = lb.getSelectedIndex()-1;//null values is first
         PushMessage m = null;
         if(index > -1){
             m = RemoteLogWeb.PUSH_MESSAGES[index];
         }
         return m;
     }
     
     protected void onSendClick() {
         String json = getMessageRequest().toJson().toString();
         notifyStartDownloading(null);
         final long ts = System.currentTimeMillis();
         final Button btnSend = mDisplay.getSendButton(); 
         btnSend.setEnabled(false);
         btnSend.setText(RemoteLogWeb.WORDS.Sending());
         mDataService.sendMessage(json, new AsyncCallback<PushMessageRespond>() {
             @Override
             public void onFailure(Throwable caught) {
                 mDisplay.getSendButton().setEnabled(true);
                 notifyStopDownloading();
             }
 
             @Override
             public void onSuccess(final PushMessageRespond result) {
                 //notifiaction via button, so if respond is too quick, ill wait for at least 500ms                
                 
                 final long now = System.currentTimeMillis();
                 long diff = now - ts;
                 if (diff < 1000) {
                     Timer t = new Timer() {
                         public void run() {
                             onPushRespond(result);
                         }
                     };
                     t.schedule((int) Math.max(500, 1000 - diff));
                 } else {
                     onPushRespond(result);
                 }
             }
         });
     }
     
     protected void onPushRespond(PushMessageRespond rmd){
         final Button btnSend = mDisplay.getSendButton(); 
         btnSend.setEnabled(true);
         btnSend.setText(RemoteLogWeb.WORDS.Send());
         notifyStopDownloading();
     }
     
     private PushMessageRequest getMessageRequest(){
         PushMessage pm = getSelectedMessage();
         PushMessageRequest pmr = new PushMessageRequest();
         pmr.setDeviceID(mDevice.getDeviceID());
         pmr.setDevicePlatform(mDevice.getPlatform());
         pmr.setPushToken(mDevice.getPushID());
         pmr.setMessage(pm);
         if(pm.hasParams()){
             pmr.setMessageParams(mDisplay.getMessageParams().getText());
         }
         pmr.setMessageContext(mDisplay.getContext().getValue());
         pmr.setWaitForRespond(mDisplay.getWaitCheckBox().getValue());
         return pmr;
     }
     
     protected void onSelectMessage(PushMessage m){
         mDisplay.getSendButton().setEnabled(m != null);
         if(m != null){
             TextArea ta = mDisplay.getMessageParams();
             boolean hasParams = m.hasParams();
             ta.setEnabled(hasParams);
             ta.setText(m.getParamExample());
         }
     }
 
     public PushMessageView onCreateView(){
         return new PushMessageView();
     }
 
     public Device getDevice() {
         return mDevice;
     }
 
     public void setDevice(Device device) {
         mDevice = device;
         
         String pushId = device != null ? device.getPushID() : null;
         initMessages(pushId != null ? device : null);
         mDisplay.getSendButton().setVisible(pushId != null ? true : false);
         Label errMsg = mDisplay.getErrorMessage();
         errMsg.setVisible(pushId == null ? true : false);
         if(pushId == null){
             errMsg.setText(RemoteLogWeb.WORDS.MissingPushID());
         }
         mDisplay.setVisible(pushId != null);
     }
 }
