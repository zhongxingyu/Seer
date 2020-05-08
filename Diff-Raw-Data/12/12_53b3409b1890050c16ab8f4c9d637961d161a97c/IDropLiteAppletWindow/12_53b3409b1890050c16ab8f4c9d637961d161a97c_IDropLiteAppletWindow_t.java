 package org.iplantc.de.client.views.windows;
 
 import org.iplantc.core.jsonutil.JsonUtil;
 import org.iplantc.core.uicommons.client.ErrorHandler;
 import org.iplantc.core.uicommons.client.events.EventBus;
 import org.iplantc.core.uicommons.client.models.UserInfo;
 import org.iplantc.core.uicommons.client.views.dialogs.IPlantSubmittableDialog;
 import org.iplantc.de.client.Constants;
 import org.iplantc.de.client.I18N;
 import org.iplantc.de.client.dispatchers.SimpleDownloadWindowDispatcher;
 import org.iplantc.de.client.dispatchers.WindowDispatcher;
 import org.iplantc.de.client.events.AsyncUploadCompleteHandler;
 import org.iplantc.de.client.events.ManageDataRefreshEvent;
 import org.iplantc.de.client.factories.EventJSONFactory.ActionType;
 import org.iplantc.de.client.factories.WindowConfigFactory;
 import org.iplantc.de.client.models.IDropLiteWindowConfig;
 import org.iplantc.de.client.services.DiskResourceServiceFacade;
 import org.iplantc.de.client.utils.IDropLite;
 import org.iplantc.de.client.views.panels.FileUploadDialogPanel;
 
 import com.extjs.gxt.ui.client.Style.HideMode;
 import com.extjs.gxt.ui.client.core.FastMap;
 import com.extjs.gxt.ui.client.event.ButtonEvent;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.event.MessageBoxEvent;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.event.WindowEvent;
 import com.extjs.gxt.ui.client.event.WindowListener;
 import com.extjs.gxt.ui.client.util.Format;
 import com.extjs.gxt.ui.client.widget.Dialog;
 import com.extjs.gxt.ui.client.widget.Html;
 import com.extjs.gxt.ui.client.widget.Label;
 import com.extjs.gxt.ui.client.widget.LayoutContainer;
 import com.extjs.gxt.ui.client.widget.MessageBox;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.extjs.gxt.ui.client.widget.layout.FitLayout;
 import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.json.client.JSONString;
 import com.google.gwt.user.client.Command;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 
 /**
  * An iPlant window for displaying the iDrop Lite Applet.
  * 
  * @author psarando
  * 
  */
 public class IDropLiteAppletWindow extends IPlantWindow {
     private final int CONTENT_PADDING = 5;
     private final IDropLiteWindowConfig config;
 
     private LayoutContainer contents;
     private Html htmlApplet;
     private Dialog dlgUpload;
     private ToolBar toolbar;
    private boolean closeit;
 
     public IDropLiteAppletWindow(String tag, IDropLiteWindowConfig config) {
         super(tag);
 
         this.config = config;
 
         init();
         initListeners();
     }
 
     private void init() {
         setSize(800, 410);
         setResizable(false);
         setLayout(new FitLayout());
 
        closeit = false;

         // Add window contents container for the applet
         contents = new LayoutContainer();
         contents.setStyleAttribute("padding", Format.substitute("{0}px", CONTENT_PADDING)); //$NON-NLS-1$ //$NON-NLS-2$
 
         add(contents);
 
         // Add a toobar for a simple mode button.
         toolbar = new ToolBar();
         setTopComponent(toolbar);
 
         // These settings enable the window to be minimized or moved without reloading the applet.
         removeFromParentOnHide = false;
         setHideMode(HideMode.VISIBILITY);
 
         initViewMode();
     }
 
     private void initViewMode() {
         // Set the heading and add the correct simple mode button based on the applet display mode.
         int displayMode = config.getDisplayMode();
         if (displayMode == IDropLite.DISPLAY_MODE_UPLOAD) {
             setHeading(I18N.DISPLAY.upload());
 
             // Add button to launch alternative, simple upload form.
             toolbar.add(buildSimpleUploadButton());
         } else if (displayMode == IDropLite.DISPLAY_MODE_DOWNLOAD) {
             setHeading(I18N.DISPLAY.download());
 
             // Add button for alternative, simple download window.
             toolbar.add(buildSimpleDownloadButton());
             contents.add(new Label(I18N.DISPLAY.idropLiteDownloadNotice()));
         }
     }
 
     private void initListeners() {
         addWindowListener(new WindowListener() {
             @Override
             public void windowDeactivate(WindowEvent we) {
                 // In Chrome, IE, or in Windows 7, the applet always stays on top, blocking access to
                 // everything else.
                if (getData("minimize") == null && !closeit) {
                    minimize();
                }
             }
         });
     }
 
     private Button buildSimpleUploadButton() {
         Button btnSimpleUpload = new Button(I18N.DISPLAY.simpleUploadForm(),
                 new SimpleFormSelectionListener() {
                     @Override
                     public void removeAppletConfirmed() {
                         confirmHide();
                         launchSimpleUploadDialog();
                     }
                 });
 
         return btnSimpleUpload;
     }
 
     private void setWindowDisplayState() {
         if (config == null) {
             return;
         }
 
         if (config.isWindowMinimized()) {
             minimize();
             return;
         }
     }
 
     private void launchSimpleUploadDialog() {
         String uploadDestId = config.getUploadDest();
         String username = UserInfo.getInstance().getUsername();
 
         // provide key/value pairs for hidden fields
         FastMap<String> hiddenFields = new FastMap<String>();
         hiddenFields.put(FileUploadDialogPanel.HDN_PARENT_ID_KEY, uploadDestId);
         hiddenFields.put(FileUploadDialogPanel.HDN_USER_ID_KEY, username);
 
         // define a handler for upload completion
         AsyncUploadCompleteHandler handler = new AsyncUploadCompleteHandler(uploadDestId) {
             /**
              * {@inheritDoc}
              */
             @Override
             public void onAfterCompletion() {
                 if (dlgUpload != null) {
                     dlgUpload.hide();
                 }
             }
         };
 
         FileUploadDialogPanel pnlUpload = new FileUploadDialogPanel(hiddenFields,
                 Constants.CLIENT.fileUploadServlet(), handler, FileUploadDialogPanel.MODE.FILE_AND_URL);
 
         dlgUpload = new IPlantSubmittableDialog(I18N.DISPLAY.upload(), 536, pnlUpload);
         dlgUpload.show();
     }
 
     private Button buildSimpleDownloadButton() {
         Button btnSimpleDownload = new Button(I18N.DISPLAY.simpleDownloadForm(),
                 new SimpleFormSelectionListener() {
                     @Override
                     public void removeAppletConfirmed() {
                         confirmHide();
                         launchSimpleDownloadWindow();
                     }
                 });
 
         return btnSimpleDownload;
     }
 
     private void launchSimpleDownloadWindow() {
         SimpleDownloadWindowDispatcher dispatcher = new SimpleDownloadWindowDispatcher();
         dispatcher.launchDownloadWindow(config.getFileDownloadPaths());
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
         // In Chrome, IE, or in Windows 7, the applet always stays on top, blocking access to the
         // confirmation dialog, which is modal and blocks access to everything else.
         minimize();
 
         MessageBox.confirm(I18N.DISPLAY.idropLiteCloseConfirmTitle(),
                 I18N.DISPLAY.idropLiteCloseConfirmMessage(), new Listener<MessageBoxEvent>() {
                     @Override
                     public void handleEvent(MessageBoxEvent be) {
                         show();
                        closeit = true;
                         if (be.getButtonClicked().getItemId().equals(Dialog.YES)) {
                             // The user confirmed closing the applet.
                             cmdRemoveAppletConfirmed.execute();
                         }
                     }
                 });
     }
 
     protected void confirmHide() {
         // Ensure the applet is removed from the DOM when this window is closed.
         contents.removeAll();
 
         super.doHide();
 
         // refresh manage data window
         String refreshPath = config.getCurrentPath();
         if (refreshPath != null && !refreshPath.isEmpty()) {
             ManageDataRefreshEvent event = new ManageDataRefreshEvent(Constants.CLIENT.myDataTag(),
                     refreshPath, null);
             EventBus.getInstance().fireEvent(event);
         }
     }
 
     @Override
     public void show() {
         super.show();
 
         // build the applet after this window is shown, so that it has the correct width and height.
         if (htmlApplet == null) {
             contents.mask(I18N.DISPLAY.loadingMask());
 
             int displayMode = config.getDisplayMode();
             if (displayMode == IDropLite.DISPLAY_MODE_UPLOAD) {
                 buildUploadApplet();
             } else if (displayMode == IDropLite.DISPLAY_MODE_DOWNLOAD) {
                 buildDownloadApplet();
             }
         }
     }
 
     private void buildUploadApplet() {
         DiskResourceServiceFacade facade = new DiskResourceServiceFacade();
         facade.upload(new IDropLiteServiceCallback() {
             @Override
             protected Html buildAppletHtml(JSONObject appletData) {
                 int adjustSize = CONTENT_PADDING * 2;
 
                 appletData.put("uploadDest", new JSONString(config.getUploadDest())); //$NON-NLS-1$
 
                 return IDropLite.getAppletForUpload(appletData, contents.getWidth() - adjustSize,
                         contents.getHeight() - adjustSize);
             }
         });
     }
 
     private void buildDownloadApplet() {
         DiskResourceServiceFacade facade = new DiskResourceServiceFacade();
         facade.download(config.getDownloadPaths(), new IDropLiteServiceCallback() {
             @Override
             protected Html buildAppletHtml(JSONObject appletData) {
                 int adjustSize = CONTENT_PADDING * 2;
 
                 return IDropLite.getAppletForDownload(appletData, contents.getWidth() - adjustSize,
                         contents.getHeight() - adjustSize - 20);
             }
         });
     }
 
     /**
      * Common success and failure handling for upload and download service calls.
      * 
      * @author psarando
      * 
      */
     private abstract class IDropLiteServiceCallback implements AsyncCallback<String> {
         /**
          * Builds the Html for the idrop-lite applet from the given JSON applet data returned by the
          * service call.
          * 
          * @param appletData
          * @return Html applet with the given applet params.
          */
         protected abstract Html buildAppletHtml(JSONObject appletData);
 
         @Override
         public void onSuccess(String response) {
             contents.unmask();
 
             htmlApplet = buildAppletHtml(JsonUtil.getObject(JsonUtil.getObject(response), "data")); //$NON-NLS-1$
 
             contents.add(htmlApplet);
 
             if (isVisible()) {
                 layout();
             }
 
             setWindowDisplayState();
         }
 
         @Override
         public void onFailure(Throwable caught) {
             contents.unmask();
 
             ErrorHandler.post(caught);
         }
     }
 
     /**
      * A SelectionListener for the Simple Upload and Download buttons, that will implement their own
      * removeAppletConfirmed actions, called in the common componentSelected method that will prompt the
      * user before closing the iDrop Lite applet.
      * 
      * @author psarando
      * 
      */
     private abstract class SimpleFormSelectionListener extends SelectionListener<ButtonEvent> {
         protected abstract void removeAppletConfirmed();
 
         @Override
         public void componentSelected(ButtonEvent ce) {
             // Selecting this listener's button will remove the Applet, so confirm this action first.
             promptRemoveApplet(new Command() {
                 @Override
                 public void execute() {
                     removeAppletConfirmed();
                 }
             });
         }
     }
 
     @Override
     public JSONObject getWindowState() {
         // Build window config
         IDropLiteWindowConfig configData = new IDropLiteWindowConfig(config);
         storeWindowViewState(configData);
 
         WindowConfigFactory configFactory = new WindowConfigFactory();
         JSONObject windowConfig = configFactory.buildWindowConfig(Constants.CLIENT.iDropLiteTag(),
                 configData);
         WindowDispatcher dispatcher = new WindowDispatcher(windowConfig);
         return dispatcher.getDispatchJson(Constants.CLIENT.iDropLiteTag(), ActionType.DISPLAY_WINDOW);
     }
 }
