 package org.iplantc.de.client.views.windows;
 
 import org.iplantc.core.uicommons.client.events.EventBus;
 import org.iplantc.core.uicommons.client.models.WindowState;
 import org.iplantc.core.uidiskresource.client.events.DiskResourceRefreshEvent;
 import org.iplantc.de.client.Constants;
 import org.iplantc.de.client.I18N;
 import org.iplantc.de.client.idroplite.presenter.IDropLitePresenter;
 import org.iplantc.de.client.idroplite.util.IDropLiteUtil;
 import org.iplantc.de.client.idroplite.views.IDropLiteView;
 import org.iplantc.de.client.idroplite.views.IDropLiteView.Presenter;
 import org.iplantc.de.client.idroplite.views.IDropLiteViewImpl;
 import org.iplantc.de.client.views.windows.configs.IDropLiteWindowConfig;
 
 import com.google.gwt.user.client.Command;
 import com.sencha.gxt.core.client.Style.HideMode;
 import com.sencha.gxt.widget.core.client.box.ConfirmMessageBox;
 import com.sencha.gxt.widget.core.client.event.HideEvent;
 import com.sencha.gxt.widget.core.client.event.HideEvent.HideHandler;
 
 /**
  * @author sriram
  * 
  */
 public class IDropLiteAppletWindow extends IplantWindowBase {
 
     private IDropLiteWindowConfig idlwc;
 
     public IDropLiteAppletWindow(IDropLiteWindowConfig config) {
         super("");
         this.idlwc = config;
        setSize("850", "430");
         setResizable(false);
         init();
     }
 
     private void init() {
         // These settings enable the window to be minimized or moved without reloading the applet.
         removeFromParentOnHide = false;
         setHideMode(HideMode.VISIBILITY);
         initViewMode();
         IDropLiteView view = new IDropLiteViewImpl();
         Presenter p = new IDropLitePresenter(view, idlwc);
         p.go(this);
     }
 
     private int initViewMode() {
         // Set the heading and add the correct simple mode button based on the applet display mode.
         int displayMode = idlwc.getDisplayMode();
         if (displayMode == IDropLiteUtil.DISPLAY_MODE_UPLOAD) {
             setTitle(I18N.DISPLAY.upload());
 
         } else if (displayMode == IDropLiteUtil.DISPLAY_MODE_DOWNLOAD) {
             setTitle(I18N.DISPLAY.download());
         }
 
         return displayMode;
 
     }
 
     protected void confirmHide() {
         super.doHide();
 
         // refresh manage data window
         String refreshPath = idlwc.getCurrentFolder().getId();
         if (refreshPath != null && !refreshPath.isEmpty()) {
             DiskResourceRefreshEvent event = new DiskResourceRefreshEvent(Constants.CLIENT.myDataTag(),
                     refreshPath, null);
             EventBus.getInstance().fireEvent(event);
         }
     }
 
     @Override
     protected void doHide() {
         promptRemoveApplet(new Command() {
             @Override
             public void execute() {
                 confirmHide();
             }
         });
     }
 
     private void promptRemoveApplet(final Command cmdRemoveAppletConfirmed) {
         final ConfirmMessageBox cmb = new ConfirmMessageBox(I18N.DISPLAY.idropLiteCloseConfirmTitle(),
                 I18N.DISPLAY.idropLiteCloseConfirmMessage());
 
         cmb.addHideHandler(new HideHandler() {
 
             @Override
             public void onHide(HideEvent event) {
                 if (cmb.getHideButton().getText().equalsIgnoreCase("yes")) {
                     // The user confirmed closing the applet.
                     cmdRemoveAppletConfirmed.execute();
                 }
 
             }
         });
 
         cmb.show();
     }
 
     @Override
     public WindowState getWindowState() {
         return createWindowState(idlwc);
     }
 
 }
