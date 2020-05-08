 package org.iplantc.de.client.views.windows;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.iplantc.core.client.widgets.Hyperlink;
 import org.iplantc.core.jsonutil.JsonUtil;
 import org.iplantc.core.uicommons.client.ErrorHandler;
 import org.iplantc.core.uicommons.client.events.EventBus;
 import org.iplantc.core.uicommons.client.models.UserInfo;
 import org.iplantc.core.uidiskresource.client.models.DiskResource;
 import org.iplantc.de.client.Constants;
 import org.iplantc.de.client.I18N;
 import org.iplantc.de.client.events.AsyncUploadCompleteHandler;
 import org.iplantc.de.client.events.ManageDataRefreshEvent;
 import org.iplantc.de.client.factories.EventJSONFactory;
 import org.iplantc.de.client.factories.EventJSONFactory.ActionType;
 import org.iplantc.de.client.factories.WindowConfigFactory;
 import org.iplantc.de.client.models.IDropLiteWindowConfig;
 import org.iplantc.de.client.services.FolderServiceFacade;
 import org.iplantc.de.client.util.WindowUtil;
 import org.iplantc.de.client.utils.DataUtils;
 import org.iplantc.de.client.utils.IDropLite;
 import org.iplantc.de.client.utils.MessageDispatcher;
 import org.iplantc.de.client.views.dialogs.IPlantSubmittableDialog;
 import org.iplantc.de.client.views.panels.FileUploadDialogPanel;
 
import com.extjs.gxt.ui.client.GXT;
 import com.extjs.gxt.ui.client.Style.HideMode;
 import com.extjs.gxt.ui.client.Style.Scroll;
 import com.extjs.gxt.ui.client.core.FastMap;
 import com.extjs.gxt.ui.client.event.ButtonEvent;
 import com.extjs.gxt.ui.client.event.ComponentEvent;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.event.MessageBoxEvent;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.util.Format;
 import com.extjs.gxt.ui.client.widget.Component;
 import com.extjs.gxt.ui.client.widget.Dialog;
 import com.extjs.gxt.ui.client.widget.Html;
 import com.extjs.gxt.ui.client.widget.LayoutContainer;
 import com.extjs.gxt.ui.client.widget.MessageBox;
 import com.extjs.gxt.ui.client.widget.VerticalPanel;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.extjs.gxt.ui.client.widget.layout.FitLayout;
 import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.http.client.URL;
 import com.google.gwt.json.client.JSONArray;
 import com.google.gwt.json.client.JSONNumber;
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
     private boolean promptHide = true;
 
     /**
      * Opens an IDropLiteAppletWindow in Upload Mode for the given uploadDest, via the Window Manager.
      * Closing this window will fire a ManageDataRefreshEvent with the given refreshPath.
      * 
      * @param uploadDest
      * @param refreshPath
      */
     public static void launchIDropLiteUploadWindow(String uploadDest, String refreshPath) {
         JSONObject windowConfigData = new JSONObject();
 
         windowConfigData.put(IDropLiteWindowConfig.DISPLAY_MODE, new JSONNumber(
                 IDropLite.DISPLAY_MODE_UPLOAD));
         windowConfigData.put(IDropLiteWindowConfig.UPLOAD_DEST, new JSONString(uploadDest));
         windowConfigData
                 .put(IDropLiteWindowConfig.MANAGE_DATA_CURRENT_PATH, new JSONString(refreshPath));
 
         dispatchWindowDisplayMessage(windowConfigData);
     }
 
     /**
      * Opens an IDropLiteAppletWindow in Download Mode for the given resources, via the Window Manager.
      * 
      * @param resources
      */
     public static void launchIDropLiteDownloadWindow(List<DiskResource> resources) {
         ArrayList<String> resourceIds = new ArrayList<String>();
 
         for (DiskResource resource : resources) {
             resourceIds.add(resource.getId());
         }
 
         JSONObject windowConfigData = new JSONObject();
 
         windowConfigData.put(IDropLiteWindowConfig.DISPLAY_MODE, new JSONNumber(
                 IDropLite.DISPLAY_MODE_DOWNLOAD));
         windowConfigData.put(IDropLiteWindowConfig.DOWNLOAD_PATHS,
                 JsonUtil.buildArrayFromStrings(resourceIds));
 
         dispatchWindowDisplayMessage(windowConfigData);
     }
 
     private static void dispatchWindowDisplayMessage(JSONObject windowConfigData) {
         WindowConfigFactory configFactory = new WindowConfigFactory();
         JSONObject windowPayload = configFactory.buildConfigPayload(Constants.CLIENT.iDropLiteTag(),
                 Constants.CLIENT.iDropLiteTag(), windowConfigData);
 
         String json = EventJSONFactory.build(ActionType.DISPLAY_WINDOW, windowPayload.toString());
 
         MessageDispatcher dispatcher = MessageDispatcher.getInstance();
         dispatcher.processMessage(json);
     }
 
     public IDropLiteAppletWindow(String tag, IDropLiteWindowConfig config) {
         super(tag);
 
         this.config = config;
 
         init();
     }
 
     private void init() {
         if (config.getDisplayMode().intValue() == IDropLite.DISPLAY_MODE_UPLOAD) {
             setHeading(I18N.DISPLAY.upload());
 
             // Also add an alternative, simple upload form launcher.
             setTopComponent(buildSimpleUploadToolbar());
         } else if (config.getDisplayMode().intValue() == IDropLite.DISPLAY_MODE_DOWNLOAD) {
             setHeading(I18N.DISPLAY.download());
 
             // Also add an alternative, simple download link panel.
             setTopComponent(buildSimpleDownloadToolbar());
         }
 
         setSize(640, 480);
         setResizable(false);
         setLayout(new FitLayout());
 
         contents = new LayoutContainer();
         contents.setStyleAttribute("padding", Format.substitute("{0}px", CONTENT_PADDING)); //$NON-NLS-1$ //$NON-NLS-2$
         add(contents);
 
         // These settings enable the window to be minimized or moved without reloading the applet.
         removeFromParentOnHide = false;
         setHideMode(HideMode.VISIBILITY);
     }
 
     private ToolBar buildSimpleUploadToolbar() {
         Button btnSimpleUpload = new Button(I18N.DISPLAY.simpleUploadForm(),
                 new SimpleFormSelectionListener() {
                     @Override
                     public void removeAppletConfirmed() {
                         confirmHide();
                         launchSimpleUploadDialog();
                     }
                 });
 
         ToolBar ret = new ToolBar();
         ret.add(btnSimpleUpload);
 
         return ret;
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
 
     private Component buildSimpleDownloadToolbar() {
         Button btnSimpleDownload = new Button(I18N.DISPLAY.simpleDownloadForm(),
                 new SimpleFormSelectionListener() {
                     @Override
                     public void removeAppletConfirmed() {
                         promptHide = false;
                         showSimpleDownloadPanel();
                     }
                 });
 
         ToolBar ret = new ToolBar();
         ret.add(btnSimpleDownload);
 
         return ret;
     }
 
     private void showSimpleDownloadPanel() {
         // Replace the applet with the links panel.
         contents.removeAll();
 
         VerticalPanel pnlLinks = new VerticalPanel();
 
         // We must proxy the download requests through a servlet, since the actual download service may
         // be on a port behind a firewall that the servlet can access, but the client can not.
         final String address = GWT.getModuleBaseURL() + Constants.CLIENT.fileDownloadServlet()
                 + "?user=" + UserInfo.getInstance().getUsername(); //$NON-NLS-1$
 
         JSONArray downloadPaths = config.getDownloadPaths();
         for (int i = 0,size = downloadPaths.size(); i < size; i++) {
             final String path = JsonUtil.getRawValueAsString(downloadPaths.get(i));
 
             Hyperlink link = new Hyperlink(DataUtils.parseNameFromPath(path), "de_hyperlink"); //$NON-NLS-1$
             link.addClickListener(new Listener<ComponentEvent>() {
                 @Override
                 public void handleEvent(ComponentEvent be) {
                     WindowUtil.open(URL.encode(address + "&path=" + path), "width=100,height=100"); //$NON-NLS-1$ //$NON-NLS-2$
                 }
             });
 
             pnlLinks.add(link);
         }
 
         contents.add(pnlLinks);
 
         // Turn on scrolling here, in case there are more links than can fit in the window.
         setScrollMode(Scroll.AUTO);
         layout();
     }
 
     @Override
     protected void doHide() {
         if (promptHide) {
             promptRemoveApplet(new Command() {
                 @Override
                 public void execute() {
                     confirmHide();
                 }
             });
         } else {
             confirmHide();
         }
     }
 
     private void promptRemoveApplet(final Command cmdRemoveAppletConfirmed) {
        // In Chrome and IE, the applet always stays on top, blocking access to the confirmation dialog,
        // which is modal and blocks access to everything else.
        final boolean appletFocusWorkaround = GXT.isChrome || GXT.isIE;

        if (appletFocusWorkaround) {
            minimize();
        }

         MessageBox.confirm(I18N.DISPLAY.idropLiteCloseConfirmTitle(),
                 I18N.DISPLAY.idropLiteCloseConfirmMessage(), new Listener<MessageBoxEvent>() {
                     @Override
                     public void handleEvent(MessageBoxEvent be) {
                        if (appletFocusWorkaround) {
                            show();
                        }

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
                     refreshPath);
             EventBus.getInstance().fireEvent(event);
         }
     }
 
     @Override
     public void show() {
         super.show();
 
         // build the applet after this window is shown, so that it has the correct width and height.
         if (htmlApplet == null) {
             contents.mask(I18N.DISPLAY.loadingMask());
 
             if (config.getDisplayMode().intValue() == IDropLite.DISPLAY_MODE_UPLOAD) {
                 buildUploadApplet();
             } else if (config.getDisplayMode().intValue() == IDropLite.DISPLAY_MODE_DOWNLOAD) {
                 buildDownloadApplet();
             }
         }
     }
 
     private void buildUploadApplet() {
         FolderServiceFacade facade = new FolderServiceFacade();
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
         FolderServiceFacade facade = new FolderServiceFacade();
         facade.download(config.getDownloadPaths(), new IDropLiteServiceCallback() {
             @Override
             protected Html buildAppletHtml(JSONObject appletData) {
                 int adjustSize = CONTENT_PADDING * 2;
 
                 return IDropLite.getAppletForDownload(appletData, contents.getWidth() - adjustSize,
                         contents.getHeight() - adjustSize);
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
 }
