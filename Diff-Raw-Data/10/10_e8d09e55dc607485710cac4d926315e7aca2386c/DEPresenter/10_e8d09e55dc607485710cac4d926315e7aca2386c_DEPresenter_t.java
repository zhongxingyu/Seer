 package org.iplantc.de.client.desktop.presenter;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.iplantc.core.jsonutil.JsonUtil;
 import org.iplantc.core.resources.client.IplantResources;
 import org.iplantc.core.uicommons.client.DEServiceFacade;
 import org.iplantc.core.uicommons.client.ErrorHandler;
 import org.iplantc.core.uicommons.client.appearance.widgets.InternalAnchorDefaultAppearance;
 import org.iplantc.core.uicommons.client.events.EventBus;
 import org.iplantc.core.uicommons.client.info.IplantAnnouncementConfig;
 import org.iplantc.core.uicommons.client.info.IplantAnnouncer;
 import org.iplantc.core.uicommons.client.models.DEProperties;
 import org.iplantc.core.uicommons.client.models.UserInfo;
 import org.iplantc.core.uicommons.client.models.UserSettings;
 import org.iplantc.core.uicommons.client.models.WindowState;
 import org.iplantc.core.uicommons.client.requests.KeepaliveTimer;
 import org.iplantc.core.uicommons.client.widgets.InternalAnchor;
 import org.iplantc.de.client.Constants;
 import org.iplantc.de.client.DeResources;
 import org.iplantc.de.client.I18N;
 import org.iplantc.de.client.Services;
 import org.iplantc.de.client.desktop.views.DEFeedbackDialog;
 import org.iplantc.de.client.desktop.views.DEView;
 import org.iplantc.de.client.events.PreferencesUpdatedEvent;
 import org.iplantc.de.client.events.PreferencesUpdatedEvent.PreferencesUpdatedEventHandler;
 import org.iplantc.de.client.events.ShowSystemMessagesEvent;
 import org.iplantc.de.client.events.WindowCloseRequestEvent;
 import org.iplantc.de.client.events.WindowShowRequestEvent;
 import org.iplantc.de.client.notifications.util.NotificationHelper.Category;
 import org.iplantc.de.client.periodic.MessagePoller;
 import org.iplantc.de.client.sysmsgs.events.MessagesUpdatedEvent;
 import org.iplantc.de.client.views.windows.configs.ConfigFactory;
 import org.iplantc.de.shared.services.PropertyServiceFacade;
 import org.iplantc.de.shared.services.ServiceCallWrapper;
 import org.iplantc.de.shared.services.SessionManagementServiceFacade;
 
 import com.google.gwt.core.shared.GWT;
 import com.google.gwt.event.dom.client.KeyPressEvent;
 import com.google.gwt.event.dom.client.KeyPressHandler;
 import com.google.gwt.event.logical.shared.CloseEvent;
 import com.google.gwt.event.logical.shared.CloseHandler;
 import com.google.gwt.event.logical.shared.OpenEvent;
 import com.google.gwt.event.logical.shared.OpenHandler;
 import com.google.gwt.event.logical.shared.ResizeEvent;
 import com.google.gwt.event.logical.shared.ResizeHandler;
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.safehtml.shared.SafeHtml;
 import com.google.gwt.user.client.Command;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.HasOneWidget;
 import com.google.gwt.user.client.ui.InlineHTML;
 import com.google.gwt.user.client.ui.RootLayoutPanel;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.sencha.gxt.core.client.XTemplates;
 import com.sencha.gxt.core.client.dom.XDOM;
 import com.sencha.gxt.core.client.dom.XElement;
 import com.sencha.gxt.core.client.util.Size;
 import com.sencha.gxt.widget.core.client.button.TextButton;
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
     private final HashMap<String, Command> keyboardShortCuts;
     private boolean keyboardEventsAdded;
     private TextButton feedbackBtn;
     
     /**
      * Constructs a default instance of the object.
      */
     public DEPresenter(final DEView view, final DeResources resources, EventBus eventBus) {
         this.view = view;
         this.view.setPresenter(this);
         this.res = resources;
         this.eventBus = eventBus;
         keyboardShortCuts = new HashMap<String, Command>();
         initializeEventHandlers();
         initializeDEProperties();
     }
 
 	private void initializeEventHandlers() {
 		// Add a close handler to detect browser refresh events.
         Window.addCloseHandler(new CloseHandler<Window>() {
 
             @Override
             public void onClose(final CloseEvent<Window> event) {
                 if (UserSettings.getInstance().isSaveSession()) {
                     UserSessionProgressMessageBox uspmb = UserSessionProgressMessageBox
                             .saveSession(DEPresenter.this);
                     uspmb.show();
                 }
             }
         });
 
         eventBus.addHandler(PreferencesUpdatedEvent.TYPE, new PreferencesUpdatedEventHandler() {
 
             @Override
             public void onUpdate(final PreferencesUpdatedEvent event) {
                 keyboardShortCuts.clear();
                 setUpKBShortCuts();
 
             }
         });
         eventBus.addHandler(MessagesUpdatedEvent.TYPE, new MessagesUpdatedEvent.Handler() {
 
 			@Override
 			public void onUpdate(final MessagesUpdatedEvent event) {
 				if (event.areNewMessages()) {
 					indicateNewSysMessages();
 				}
 			}});
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
                 getUserPreferences();
                 setBrowserContextMenuEnabled(DEProperties.getInstance().isContextClickEnabled());
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
                 parseWorkspaceId(result);
                 initializeUserInfoAttributes();
                 initKeepaliveTimer();
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
 
     private void getUserSession() {
         if (UserSettings.getInstance().isSaveSession()) {
             UserSessionProgressMessageBox uspmb = UserSessionProgressMessageBox.restoreSession(this);
             uspmb.show();
         }
     }
 
     private void doWorkspaceDisplay() {
         view.drawHeader();
         RootLayoutPanel.get().add(view.asWidget());
         addFeedbackButton();
         Window.addResizeHandler(new ResizeHandler() {
 
             @Override
             public void onResize(ResizeEvent event) {
                 positionFButton(getViewPortSize());
             }
         });
 
         initMessagePoller();
     }
 
     private void addFeedbackButton() {
         DeResources resources = GWT.create(DeResources.class);
         resources.css().ensureInjected();
         feedbackBtn = new TextButton(I18N.DISPLAY.feedback());
         feedbackBtn.setIcon(IplantResources.RESOURCES.feedback());
         feedbackBtn.addSelectHandler(new SelectHandler() {
 
             @Override
             public void onSelect(SelectEvent event) {
                 DEFeedbackDialog d = new DEFeedbackDialog();
                 d.show();
             }
         });
         positionFButton(getViewPortSize());
         feedbackBtn.addStyleName(resources.css().rotate90());
         feedbackBtn.getElement().updateZIndex(0);
         RootPanel.get().add(feedbackBtn);
     }
 
     private void positionFButton(Size s) {
         int left = s.getWidth() - 50 + XDOM.getBodyScrollLeft();
         feedbackBtn.setPosition(left, s.getHeight() / 2);
     }
 
     private Size getViewPortSize() {
         Size s = XDOM.getViewportSize();
         return s;
     }
 
     public static native void doIntro() /*-{
 		$wnd.introJs().start();
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
 
     private void addKeyBoardEvents() {
         if (!keyboardEventsAdded) {
             RootPanel.get().addDomHandler(new KeyPressHandler() {
                 @Override
                 public void onKeyPress(KeyPressEvent event) {
                     if (event.isShiftKeyDown() && event.isControlKeyDown()) {
                         Command cmd = keyboardShortCuts.get(String.valueOf(event.getCharCode()));
                         if (cmd != null) {
                             cmd.execute();
                         }
                     }
                 }
             }, KeyPressEvent.getType());
             keyboardEventsAdded = true;
         }
     }
 
     private String parseWorkspaceId(String json) {
         JSONObject obj = JsonUtil.getObject(json);
         // Bootstrap the user-info object with session data provided in JSON
         // format
         UserInfo userInfo = UserInfo.getInstance();
         userInfo.init(obj.toString());
         return userInfo.getWorkspaceId();
     }
 
     private void initMessagePoller() {
         MessagePoller poller = MessagePoller.getInstance();
         poller.addTask(new CountUnseenNotifications());
         poller.start();
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
 
     /**
      * Initializes the username and email for a user.
      * 
      * Calls the session management service to get the attributes associated with a user.
      */
     private void initializeUserInfoAttributes() {
         SessionManagementServiceFacade.getInstance().getAttributes(
                 new AsyncCallback<Map<String, String>>() {
                     @Override
                     public void onFailure(Throwable caught) {
                         ErrorHandler.post(I18N.ERROR.retrieveUserInfoFailed(), caught);
                     }
 
                     @Override
                     public void onSuccess(Map<String, String> attributes) {
                         UserInfo userInfo = UserInfo.getInstance();
 
                         userInfo.setEmail(attributes.get(UserInfo.ATTR_EMAIL));
                         userInfo.setUsername(attributes.get(UserInfo.ATTR_UID));
                         userInfo.setFullUsername(attributes.get(UserInfo.ATTR_USERNAME));
                         userInfo.setFirstName(attributes.get(UserInfo.ATTR_FIRSTNAME));
                         userInfo.setLastName(attributes.get(UserInfo.ATTR_LASTNAME));
                         doWorkspaceDisplay();
                         getUserSession();
                     }
                 });
     }
 
     private void loadPreferences(JSONObject obj) {
         UserSettings.getInstance().setValues(obj);
         setUpKBShortCuts();
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
 
         if (UserSettings.getInstance().isSaveSession()) {
             UserSessionProgressMessageBox uspmb = UserSessionProgressMessageBox.saveSession(this);
             uspmb.show();
         }
 
         // Need to perform actual logout redirect.
         Window.Location.assign(Window.Location.getPath() + Constants.CLIENT.logoutUrl());
 
     }
 
     @Override
     public void restoreWindows(List<WindowState> windowStates) {
         view.restoreWindows(windowStates);
     }
 
     @Override
     public List<WindowState> getOrderedWindowStates() {
         return view.getOrderedWindowStates();
     }
     
     // TODO this needs to be a view class
     static final class NewSystemMessageAnnouncement extends InlineHTML {
 
     	interface Templates extends XTemplates {
     		@XTemplate("<div>You have an important announcement. {anchor}</div>")
     		SafeHtml make(SafeHtml anchor);
     	}
     	
     	private static final Templates FACTORY = GWT.create(Templates.class);
         private static final InternalAnchorDefaultAppearance ANCHOR_APPEARANCE
     			= new InternalAnchorDefaultAppearance(true);
     	
     	private final InternalAnchor<Void> anchor;
     	
     	NewSystemMessageAnnouncement() {
     		super(FACTORY.make(ANCHOR_APPEARANCE.render("Read it.")));
     		final XElement elmt = XElement.as(getElement());
 	   		anchor = InternalAnchor.wrap(null, ANCHOR_APPEARANCE.getAnchorElement(elmt));
     		anchor.addOpenHandler(new OpenHandler<Void>() {
 				@Override
 				public void onOpen(final OpenEvent<Void> event) {
 					EventBus.getInstance().fireEvent(new ShowSystemMessagesEvent());
 				}});			
      	}
     }
     
     private void indicateNewSysMessages() {
     	final NewSystemMessageAnnouncement announcement = new NewSystemMessageAnnouncement();
         IplantAnnouncer.getInstance().schedule(announcement, new IplantAnnouncementConfig(true, 0));
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
 }
