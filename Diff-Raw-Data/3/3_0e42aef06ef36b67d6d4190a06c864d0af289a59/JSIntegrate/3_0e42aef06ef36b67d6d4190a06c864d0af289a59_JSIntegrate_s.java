 package org.iplantc.js.integrate.client;
 
 import java.util.Map;
 
 import org.iplantc.core.uicommons.client.ErrorHandler;
 import org.iplantc.core.uicommons.client.models.UserInfo;
 import org.iplantc.de.shared.services.PropertyServiceFacade;
 import org.iplantc.de.shared.services.SessionManagementServiceFacade;
 import org.iplantc.js.integrate.client.controllers.ApplicationController;
 import org.iplantc.js.integrate.client.models.TitoProperties;
 
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.RootPanel;
 
 /**
  * Entry point classes define <code>onModuleLoad()</code>.
  */
 public class JSIntegrate implements EntryPoint {
     /**
      * This is the entry point method.
      */
     public void onModuleLoad() {
         setEntryPointTitle();
         initUserInfo();
     }
 
     private void initApp() {
         ApplicationLayout layoutApplication = new ApplicationLayout();
 
         ApplicationController controller = ApplicationController.getInstance();
         controller.init(layoutApplication);
 
         RootPanel.get().add(layoutApplication);
     }
 
     private void setEntryPointTitle() {
         Window.setTitle(I18N.DISPLAY.header());
     }
 
     private void initUserInfo() {
         SessionManagementServiceFacade.getInstance().getAttributes(
                 new AsyncCallback<Map<String, String>>() {
                     @Override
                     public void onFailure(Throwable caught) {
                         ErrorHandler.post(I18N.DISPLAY.cantLoadUserInfo(), caught);
                     }
 
                     @Override
                     public void onSuccess(Map<String, String> attributes) {
                         UserInfo userInfo = UserInfo.getInstance();
                         userInfo.setEmail(attributes.get(UserInfo.ATTR_EMAIL));
                         userInfo.setUsername(attributes.get(UserInfo.ATTR_UID));
                         userInfo.setFullUsername(attributes.get(UserInfo.ATTR_USERNAME));
                         initializeTitoProperties();
                     }
                 });
     }
     
     /** Initializes the Tito configuration properties object.
     */
    private void initializeTitoProperties() {
        PropertyServiceFacade.getInstance().getProperties(new AsyncCallback<Map<String, String>>() {
            @Override
            public void onFailure(Throwable caught) {
                ErrorHandler.post(I18N.DISPLAY.cantLoadUserInfo(), caught);
            }
 
            @Override
            public void onSuccess(Map<String, String> result) {
                TitoProperties.getInstance().initialize(result);
               setBrowserContextMenuEnabled(TitoProperties.getInstance().isContextClickEnabled());
                initApp();
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
 
 }
