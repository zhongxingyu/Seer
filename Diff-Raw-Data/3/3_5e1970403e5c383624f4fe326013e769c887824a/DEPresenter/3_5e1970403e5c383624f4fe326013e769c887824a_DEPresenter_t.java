 package org.iplantc.de.client.desktop.presenter;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.iplantc.core.jsonutil.JsonUtil;
 import org.iplantc.core.resources.client.DEFeedbackStyle;
 import org.iplantc.core.resources.client.IplantResources;
 import org.iplantc.core.uicommons.client.DEServiceFacade;
 import org.iplantc.core.uicommons.client.ErrorHandler;
 import org.iplantc.core.uicommons.client.events.EventBus;
 import org.iplantc.core.uicommons.client.events.UserSettingsUpdatedEvent;
 import org.iplantc.core.uicommons.client.events.UserSettingsUpdatedEventHandler;
 import org.iplantc.core.uicommons.client.info.IplantAnnouncer;
 import org.iplantc.core.uicommons.client.models.DEProperties;
 import org.iplantc.core.uicommons.client.models.HasId;
 import org.iplantc.core.uicommons.client.models.UserInfo;
 import org.iplantc.core.uicommons.client.models.UserSettings;
 import org.iplantc.core.uicommons.client.models.WindowState;
 import org.iplantc.core.uicommons.client.models.diskresources.DiskResourceAutoBeanFactory;
 import org.iplantc.core.uicommons.client.models.diskresources.Folder;
 import org.iplantc.core.uicommons.client.requests.KeepaliveTimer;
 import org.iplantc.core.uidiskresource.client.events.FileUploadedEvent;
 import org.iplantc.core.uidiskresource.client.events.FileUploadedEvent.FileUploadedEventHandler;
 import org.iplantc.de.client.Constants;
 import org.iplantc.de.client.DeResources;
 import org.iplantc.de.client.I18N;
 import org.iplantc.de.client.Services;
 import org.iplantc.de.client.desktop.views.DEFeedbackDialog;
 import org.iplantc.de.client.desktop.views.DEView;
 import org.iplantc.de.client.events.DefaultUploadCompleteHandler;
 import org.iplantc.de.client.events.PreferencesUpdatedEvent;
 import org.iplantc.de.client.events.PreferencesUpdatedEvent.PreferencesUpdatedEventHandler;
 import org.iplantc.de.client.events.SystemMessageCountUpdateEvent;
 import org.iplantc.de.client.events.WindowCloseRequestEvent;
 import org.iplantc.de.client.events.WindowShowRequestEvent;
 import org.iplantc.de.client.notifications.util.NotificationHelper.Category;
 import org.iplantc.de.client.periodic.MessagePoller;
 import org.iplantc.de.client.services.UserSessionServiceFacade;
 import org.iplantc.de.client.sysmsgs.presenter.NewMessagePresenter;
 import org.iplantc.de.client.views.windows.configs.ConfigFactory;
 import org.iplantc.de.client.views.windows.configs.DiskResourceWindowConfig;
 import org.iplantc.de.shared.services.PropertyServiceFacade;
 import org.iplantc.de.shared.services.ServiceCallWrapper;
 
 import com.google.common.base.Strings;
 import com.google.common.collect.Lists;
 import com.google.gwt.core.client.Scheduler;
 import com.google.gwt.core.shared.GWT;
 import com.google.gwt.dom.client.NativeEvent;
 import com.google.gwt.event.logical.shared.ResizeEvent;
 import com.google.gwt.event.logical.shared.ResizeHandler;
 import com.google.gwt.event.shared.HandlerRegistration;
 import com.google.gwt.http.client.URL;
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.user.client.Command;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.HasOneWidget;
 import com.google.gwt.user.client.ui.RootLayoutPanel;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.web.bindery.autobean.shared.AutoBean;
 import com.google.web.bindery.autobean.shared.AutoBeanCodex;
 import com.sencha.gxt.core.client.dom.XDOM;
 import com.sencha.gxt.core.client.util.KeyNav;
 import com.sencha.gxt.core.client.util.Size;
 import com.sencha.gxt.widget.core.client.Dialog;
 import com.sencha.gxt.widget.core.client.Dialog.PredefinedButton;
 import com.sencha.gxt.widget.core.client.box.MessageBox;
 import com.sencha.gxt.widget.core.client.button.IconButton;
 import com.sencha.gxt.widget.core.client.event.HideEvent;
 import com.sencha.gxt.widget.core.client.event.HideEvent.HideHandler;
 import com.sencha.gxt.widget.core.client.event.SelectEvent;
 import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
 
 /**
  * Defines the default view of the workspace.
  * 
  * @author sriram
  */
 public class DEPresenter implements DEView.Presenter {
 
     private final DEView view;
     private final DeResources res;
     private final EventBus eventBus;
     private final NewMessagePresenter newSysMsgPresenter;
     private final HashMap<String, Command> keyboardShortCuts;
     private final List<HandlerRegistration> eventHandlers = new ArrayList<HandlerRegistration>();
     private boolean keyboardEventsAdded;
     private IconButton feedbackBtn;
     private final SaveSessionPeriodic ssp;
 
     /**
      * Constructs a default instance of the object.
      */
     public DEPresenter(final DEView view, final DeResources resources, EventBus eventBus) {
         this.view = view;
         this.view.setPresenter(this);
         this.res = resources;
         this.eventBus = eventBus;
         newSysMsgPresenter = new NewMessagePresenter(eventBus, IplantAnnouncer.getInstance());
         keyboardShortCuts = new HashMap<String, Command>();
         initializeEventHandlers();
         initializeDEProperties();
         ssp = new SaveSessionPeriodic(this);
     }
 
     private void initializeEventHandlers() {
 
         eventHandlers.add(eventBus.addHandler(PreferencesUpdatedEvent.TYPE,
                 new PreferencesUpdatedEventHandler() {
 
                     @Override
                     public void onUpdate(final PreferencesUpdatedEvent event) {
                         keyboardShortCuts.clear();
                         setUpKBShortCuts();
                         doPeriodicSessionSave();
                     }
                 }));
         eventHandlers.add(eventBus.addHandler(SystemMessageCountUpdateEvent.TYPE,
                 new SystemMessageCountUpdateEvent.Handler() {
                     @Override
                     public void onCountUpdate(final SystemMessageCountUpdateEvent event) {
                         view.updateUnseenSystemMessageCount(event.getCount());
                     }
                 }));
         eventHandlers.add(eventBus.addHandler(UserSettingsUpdatedEvent.TYPE,
                 new UserSettingsUpdatedEventHandler() {
 
                     @Override
                     public void onUpdate(UserSettingsUpdatedEvent usue) {
                         saveSettings();
 
                     }
                 }));
 
         eventHandlers.add(eventBus.addHandler(FileUploadedEvent.TYPE, new FileUploadedEventHandler() {
             @Override
             public void onFileUploaded(FileUploadedEvent event) {
                 DefaultUploadCompleteHandler duc = new DefaultUploadCompleteHandler(event
                         .getUploadDestFolderFolder().toString());
                 JSONObject obj = JsonUtil.getObject(event.getResponse());
                 String fileJson = JsonUtil.getObject(obj, "file").toString();
                 duc.onCompletion(event.getFilepath(), fileJson);
             }
         }));
 
     }
 
     @Override
     public void cleanUp() {
         EventBus eventBus = EventBus.getInstance();
         for (HandlerRegistration hr : eventHandlers) {
             eventBus.removeHandler(hr);
         }
         view.cleanUp();
     }
 
     /**
      * Initializes the discovery environment configuration properties object.
      */
     private void initializeDEProperties() {
         PropertyServiceFacade.getInstance().getProperties(new AsyncCallback<Map<String, String>>() {
             @Override
             public void onFailure(Throwable caught) {
                 ErrorHandler.post(I18N.ERROR.systemInitializationError(), caught);
             }
 
             @Override
             public void onSuccess(Map<String, String> result) {
                 DEProperties.getInstance().initialize(result);
                 getUserInfo();
                 setBrowserContextMenuEnabled(DEProperties.getInstance().isContextClickEnabled());
             }
         });
     }
 
     private void saveSettings() {
         UserSettings us = UserSettings.getInstance();
         UserSessionServiceFacade facade = new UserSessionServiceFacade();
         facade.saveUserPreferences(us.toJson(), new AsyncCallback<String>() {
 
             @Override
             public void onSuccess(String result) {
                 // do nothing intentionally
 
             }
 
             @Override
             public void onFailure(Throwable caught) {
                 ErrorHandler.post(caught);
             }
         });
     }
 
     /**
      * Retrieves the user information from the server.
      */
     private void getUserInfo() {
         String address = DEProperties.getInstance().getMuleServiceBaseUrl() + "bootstrap"; //$NON-NLS-1$
         ServiceCallWrapper wrapper = new ServiceCallWrapper(address);
 
         DEServiceFacade.getInstance().getServiceData(wrapper, new AsyncCallback<String>() {
             @Override
             public void onFailure(Throwable caught) {
                 ErrorHandler.post(I18N.ERROR.retrieveUserInfoFailed(), caught);
             }
 
             @Override
             public void onSuccess(String result) {
                 parseWorkspaceInfo(result);
                 initKeepaliveTimer();
                 initUserHomeDir();
                 doWorkspaceDisplay();
                 getUserPreferences();
                 processQueryStrings();
 
             }
         });
     }
 
     private void getUserPreferences() {
         Services.USER_SESSION_SERVICE.getUserPreferences(new AsyncCallback<String>() {
 
             @Override
             public void onFailure(Throwable caught) {
                 ErrorHandler.post(caught);
             }
 
             @Override
             public void onSuccess(String result) {
                 loadPreferences(JsonUtil.getObject(result));
             }
         });
     }
 
     private void loadPreferences(JSONObject obj) {
         UserSettings.getInstance().setValues(obj);
         setUpKBShortCuts();
         getUserSession();
     }
 
     private void getUserSession() {
         if (UserSettings.getInstance().isSaveSession()) {
             // This restoreSession's callback will also init periodic session saving.
             UserSessionProgressMessageBox uspmb = UserSessionProgressMessageBox.restoreSession(this);
             uspmb.show();
         }
     }
 
     private void doWorkspaceDisplay() {
         Window.addResizeHandler(new ResizeHandler() {
 
             @Override
             public void onResize(ResizeEvent event) {
                 positionFButton(getViewPortSize());
             }
         });
         RootLayoutPanel.get().clear();
         view.drawHeader();
         RootLayoutPanel.get().add(view.asWidget());
         addFeedbackButton();
         initMessagePoller();
     }
 
     private void addFeedbackButton() {
         DEFeedbackStyle style = IplantResources.RESOURCES.getFeedbackStyle();
         style.ensureInjected();
         feedbackBtn = new IconButton(style.feedback());
         feedbackBtn.addSelectHandler(new SelectHandler() {
 
             @Override
             public void onSelect(SelectEvent event) {
                 DEFeedbackDialog d = new DEFeedbackDialog();
                 d.show();
             }
         });
         positionFButton(getViewPortSize());
         feedbackBtn.getElement().updateZIndex(0);
         RootPanel.get().add(feedbackBtn);
         feedbackBtn.getElement().setAttribute("data-intro",
                 org.iplantc.core.resources.client.messages.I18N.TOUR.introFeedback());
         feedbackBtn.getElement().setAttribute("data-position", "top");
         feedbackBtn.getElement().setAttribute("data-step", "6");
     }
 
     private void positionFButton(Size s) {
         int left = s.getWidth() - 235 + XDOM.getBodyScrollLeft();
         if (feedbackBtn != null) {
             feedbackBtn.setPosition(left, s.getHeight() - 80);
         }
     }
 
     private Size getViewPortSize() {
         Size s = XDOM.getViewportSize();
         return s;
     }
 
     public static native void doIntro() /*-{
 		var introjs = $wnd.introJs();
 		introjs.setOption("showStepNumbers", false);
 		introjs.setOption("skipLabel", "Exit");
 		introjs.start();
 
     }-*/;
 
     private void setUpKBShortCuts() {
         UserSettings us = UserSettings.getInstance();
         keyboardShortCuts.put(us.getDataShortCut(), new DataKBShortCutCmd());
         keyboardShortCuts.put(us.getAppsShortCut(), new AppsKBShortCutCmd());
         keyboardShortCuts.put(us.getAnalysesShortCut(), new AnalysesKBShortCutCmd());
         keyboardShortCuts.put(us.getNotifiShortCut(), new NotifyKBShortCutCmd());
         keyboardShortCuts.put(us.getCloseShortCut(), new CloseKBShortCutCmd());
         addKeyBoardEvents();
     }
 
     // Sriram : We need a generic way to process query strings. This is temp. solution for CORE-4694
     private void processQueryStrings() {
         Map<String, List<String>> params = Window.Location.getParameterMap();
         for (String key : params.keySet()) {
             if (key.equalsIgnoreCase("type")) {
                 String val = params.get(key).get(0);
                 if (val.equalsIgnoreCase("data")) {
                     String selectedFolder = URL.decode(Window.Location.getParameter("folder"));
                     DiskResourceWindowConfig diskResourceWindowConfig = ConfigFactory
                             .diskResourceWindowConfig();
                     diskResourceWindowConfig.setMaximized(true);
                     if (!Strings.isNullOrEmpty(selectedFolder)) {
                         final DiskResourceAutoBeanFactory drFactory = GWT
                                 .create(DiskResourceAutoBeanFactory.class);
                         AutoBean<Folder> fAb = AutoBeanCodex.decode(drFactory, Folder.class,
                                 "{\"id\":\"" + selectedFolder + "\"}");
                         ArrayList<HasId> newArrayList = Lists.newArrayList();
                         Folder folder = fAb.as();
                         newArrayList.add(folder);
                         diskResourceWindowConfig.setSelectedFolder(folder);
                         diskResourceWindowConfig.setSelectedDiskResources(newArrayList);
                         EventBus.getInstance().fireEvent(
                                 new WindowShowRequestEvent(diskResourceWindowConfig, true));
                     } else {
                         eventBus.fireEvent(new WindowShowRequestEvent(diskResourceWindowConfig));
                     }
                 }
             }
         }
     }
 
     private void addKeyBoardEvents() {
         if (!keyboardEventsAdded) {
             new KeyNav(RootPanel.get()) {
                 @Override
                 public void handleEvent(NativeEvent event) {
                     if (event.getCtrlKey() && event.getShiftKey()) {
                         Command cmd = keyboardShortCuts.get(String.valueOf((char)event.getKeyCode()));
                         if (cmd != null) {
                             cmd.execute();
                         }
                     }
                 }
 
             };
 
             keyboardEventsAdded = true;
         }
 
     }
 
     private void parseWorkspaceInfo(String json) {
         // Bootstrap the user-info object with workspace info provided in JSON format.
         UserInfo.getInstance().init(json);
         initIntro();
     }
 
     private void initIntro() {
         Scheduler.get().scheduleDeferred(new Command() {
 
             @Override
             public void execute() {
                 if (UserInfo.getInstance().isNewUser()) {
                     MessageBox box = new MessageBox(I18N.DISPLAY.welcome(),
                             org.iplantc.core.resources.client.messages.I18N.TOUR.introWelcome());
                     box.setPredefinedButtons(PredefinedButton.YES, PredefinedButton.NO);
                     box.setIcon(MessageBox.ICONS.question());
                     box.addHideHandler(new HideHandler() {
 
                         @Override
                         public void onHide(HideEvent event) {
                             Dialog btn = (Dialog)event.getSource();
                             if (btn.getHideButton().getText().equalsIgnoreCase("yes")) { //$NON-NLS-1$
                                 doIntro();
                             }
                         }
                     });
                     box.show();
                 }
             }
 
         });
     }
 
     private void initMessagePoller() {
         // Do an initial fetch of message counts, otherwise the initial count will not be fetched until
         // after an entire poll-length of the MessagePoller's timer (15 seconds by default).
         GetMessageCounts notificationCounts = new GetMessageCounts();
         notificationCounts.run();
         MessagePoller poller = MessagePoller.getInstance();
         poller.addTask(notificationCounts);
         poller.start();
     }
 
     @Override
     public void doPeriodicSessionSave() {
         MessagePoller poller = MessagePoller.getInstance();
         if (UserSettings.getInstance().isSaveSession()) {
 
             ssp.run();
             poller.addTask(ssp);
             // start if not started...
             poller.start();
         } else {
             poller.removeTask(ssp);
         }
     }
 
     /**
      * Initializes the session keepalive timer.
      */
     private void initKeepaliveTimer() {
         String target = DEProperties.getInstance().getKeepaliveTarget();
         int interval = DEProperties.getInstance().getKeepaliveInterval();
         if (target != null && !target.equals("") && interval > 0) {
             KeepaliveTimer.getInstance().start(target, interval);
         }
     }
 
     private void initUserHomeDir() {
         Services.DISK_RESOURCE_SERVICE.getHomeFolder(new AsyncCallback<String>() {
 
             @Override
             public void onSuccess(String result) {
                JSONObject obj = JsonUtil.getObject(result);
                UserInfo.getInstance().setHomePath(obj.get("path").isString().toString());
             }
 
             @Override
             public void onFailure(Throwable caught) {
                 // best guess with username. this is horrible...
                 UserInfo userInfo = UserInfo.getInstance();
                 userInfo.setHomePath("/iplant/home/" + userInfo.getUsername());
             }
         });
     }
 
     /**
      * Disable the context menu of the browser using native JavaScript.
      * 
      * This disables the user's ability to right-click on this widget and get the browser's context menu
      */
     private native void setBrowserContextMenuEnabled(boolean enabled)
     /*-{
 		$doc.oncontextmenu = function() {
 			return enabled;
 		};
     }-*/;
 
     @Override
     public void go(HasOneWidget container) {/* Do Nothing */
     }
 
     @Override
     public void doLogout() {
         // Need to stop polling
         MessagePoller.getInstance().stop();
         cleanUp();
 
         String address = DEProperties.getInstance().getMuleServiceBaseUrl()
                 + "logout?login-time=" + UserInfo.getInstance().getLoginTime(); //$NON-NLS-1$
         ServiceCallWrapper wrapper = new ServiceCallWrapper(address);
 
         DEServiceFacade.getInstance().getServiceData(wrapper, new AsyncCallback<String>() {
 
             @Override
             public void onFailure(Throwable arg0) {
                 GWT.log("error on logout:" + arg0.getMessage());
                 // logout anyway
                 logout();
             }
 
             @Override
             public void onSuccess(String arg0) {
                 GWT.log("logout service success:" + arg0);
                 logout();
             }
 
             private void logout() {
                 String redirectUrl = com.google.gwt.core.client.GWT.getHostPageBaseURL()
                         + Constants.CLIENT.logoutUrl();
                 if (UserSettings.getInstance().isSaveSession()) {
                     UserSessionProgressMessageBox uspmb = UserSessionProgressMessageBox.saveSession(
                             DEPresenter.this, redirectUrl);
                     uspmb.show();
                 } else {
                     Window.Location.assign(redirectUrl);
                 }
             }
 
         });
     }
 
     @Override
     public void restoreWindows(List<WindowState> windowStates) {
         view.restoreWindows(windowStates);
     }
 
     @Override
     public List<WindowState> getOrderedWindowStates() {
         return view.getOrderedWindowStates();
     }
 
     private class DataKBShortCutCmd implements Command {
 
         @Override
         public void execute() {
             eventBus.fireEvent(new WindowShowRequestEvent(ConfigFactory.diskResourceWindowConfig()));
 
         }
 
     }
 
     private class AppsKBShortCutCmd implements Command {
 
         @Override
         public void execute() {
             eventBus.fireEvent(new WindowShowRequestEvent(ConfigFactory.appsWindowConfig()));
 
         }
 
     }
 
     private class AnalysesKBShortCutCmd implements Command {
 
         @Override
         public void execute() {
             eventBus.fireEvent(new WindowShowRequestEvent(ConfigFactory.analysisWindowConfig()));
 
         }
 
     }
 
     private class NotifyKBShortCutCmd implements Command {
 
         @Override
         public void execute() {
             eventBus.fireEvent(new WindowShowRequestEvent(ConfigFactory.notifyWindowConfig(Category.ALL)));
 
         }
 
     }
 
     private class CloseKBShortCutCmd implements Command {
 
         @Override
         public void execute() {
             eventBus.fireEvent(new WindowCloseRequestEvent());
 
         }
     }
 
     @Override
     public void doWelcomeIntro() {
         // call intro.js
         doIntro();
 
     }
 }
