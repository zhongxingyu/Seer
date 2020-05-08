 package org.iplantc.de.client.views.windows;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.iplantc.core.jsonutil.JsonUtil;
 import org.iplantc.core.uicommons.client.events.EventBus;
 import org.iplantc.core.uidiskresource.client.models.File;
 import org.iplantc.core.uidiskresource.client.models.FileIdentifier;
 import org.iplantc.de.client.Constants;
 import org.iplantc.de.client.I18N;
 import org.iplantc.de.client.commands.RPCSuccessCommand;
 import org.iplantc.de.client.controllers.DataMonitor;
 import org.iplantc.de.client.events.FileEditorWindowDirtyEvent;
 import org.iplantc.de.client.events.FileEditorWindowDirtyEventHandler;
 import org.iplantc.de.client.services.DiskResourceServiceCallback;
 import org.iplantc.de.client.services.FileEditorServiceFacade;
 import org.iplantc.de.client.util.WindowUtil;
 import org.iplantc.de.client.views.panels.FilePreviewPanel;
 import org.iplantc.de.client.views.panels.ImagePanel;
 import org.iplantc.de.client.views.panels.RawDataPanel;
 import org.iplantc.de.client.views.panels.TreeHyperlinkGridPanel;
 import org.iplantc.de.client.views.panels.ViewerWindowTabPanel;
 
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.event.MessageBoxEvent;
 import com.extjs.gxt.ui.client.widget.MessageBox;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.json.client.JSONArray;
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.json.client.JSONString;
 import com.google.gwt.json.client.JSONValue;
 import com.google.gwt.user.client.Element;
 
 /**
  * Provides a user interface for editing of file data.
  * 
  * @author amuir
  * 
  */
 public class FileEditorWindow extends FileWindow implements DataMonitor {
     private int numLoadingTabs;
     private ViewerWindowTabPanel panel;
     private int treeUrlTabIndex;
     private boolean isPdfPanel;
     private boolean isDirty;
     private Map<String, RPCSuccessCommand> commands;
 
     /**
      * Constructs an instance given a window identifier (tag) and file identifier.
      * 
      * @param tag a string that uniquely identifies each instance of a window
      * @param file a unique identifier for a file
      */
     public FileEditorWindow(final String tag, final FileIdentifier file, final String manifest) {
         super(tag, file, manifest);
 
         treeUrlTabIndex = 1;
 
         // add a tree url tab, if the manifest already has urls
         JSONArray urls = getManifestTreeUrls();
         if (urls != null) {
             addTreeTab(urls);
         }
     }
 
     /**
      * Gets the tree-urls json array from the manifest.
      * 
      * @return A json array of at least one tree URL, or null otherwise.
      */
     private JSONArray getManifestTreeUrls() {
         JSONArray urls = JsonUtil.getArray(manifest, "tree-urls"); //$NON-NLS-1$
 
         // make sure the json array has at least one element that is not a json null value.
         if (urls != null) {
             for (int i = 0,urlCount = urls.size(); i < urlCount; i++) {
                 if (urls.get(i).isNull() == null) {
                     return urls;
                 }
             }
         }
 
         return null;
     }
 
     /**
      * Adds a tab with a Hyperlink panel of tree URLs to the panel
      */
     private TreeHyperlinkGridPanel addTreeTab(JSONArray urls) {
         if (panel == null) {
             return null;
         }
 
         // build the tab
         TreeHyperlinkGridPanel pnlTreeUrlTab = new TreeHyperlinkGridPanel(file, urls);
         pnlTreeUrlTab.setTabIndex(treeUrlTabIndex);
 
        panel.addTab(pnlTreeUrlTab);
 
         return pnlTreeUrlTab;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void init() {
         panel = new ViewerWindowTabPanel();
         isPdfPanel = false;
 
         initCommands();
 
         super.init();
     }
 
     private void initCommands() {
         commands = new HashMap<String, RPCSuccessCommand>();
 
         commands.put("rawcontents", new RawDataSuccessCommand()); //$NON-NLS-1$
         commands.put("png", new ImageDataSuccessCommand()); //$NON-NLS-1$
         commands.put("pdf", new PdfDataSuccessCommand()); //$NON-NLS-1$
         commands.put("preview", new PreviewSuccessCommand()); //$NON-NLS-1$
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void clearPanel() {
         // if (panel != null) {
         // panel.removeAll();
         // }
     }
 
     private void updateStatus(int numTabs) {
         numLoadingTabs += numTabs;
 
         // are we done loading?
         if (numLoadingTabs == 0) {
             status.clearStatus(""); //$NON-NLS-1$
         }
     }
 
     private void getImage(String url) {
         if (url != null && !url.isEmpty() && panel != null) {
             // we got the url of an image... lets add a tab
             ImagePanel pnlImage = new ImagePanel(file, getServletDownloadUrl(url));
 
             panel.addTab(pnlImage);
         }
     }
 
     private void getPdfFile(String url) {
         if (url != null && !url.isEmpty()) {
             // we got the url of the PDF file, so open it in a new window
             WindowUtil.open(getServletDownloadUrl(url));
 
             isPdfPanel = true;
         }
     }
 
     private String getServletDownloadUrl(String downloadUrl) {
         // we must route the request through the download servlet, in case the download URL is not
         // publicly accessible, so pass only the url query to the download servlet URL.
         String[] urlParts = downloadUrl.split("\\?"); //$NON-NLS-1$
 
         String urlParams;
         if (urlParts.length > 1) {
             urlParams = "url=" + urlParts[0] + "&" + urlParts[1]; //$NON-NLS-1$ //$NON-NLS-2$
         } else {
             urlParams = urlParts[0];
         }
 
         return GWT.getModuleBaseURL() + Constants.CLIENT.fileDownloadServlet() + "?" + urlParams; //$NON-NLS-1$
     }
 
     private void createViews() {
         RPCSuccessCommand cmd;
 
         Set<String> keys = manifest.keySet();
 
         // parse
         for (String key : keys) {
             cmd = commands.get(key);
 
             if (cmd != null) {
                 JSONValue value = getItems(key);
 
                 if (value != null) {
                     JSONString strValue = value.isString();
                     JSONArray items = value.isArray();
 
                     if (strValue != null) {
                         // the value is just a string
                         updateStatus(1);
 
                         cmd.execute(strValue.stringValue());
                     } else if (items != null) {
                         // the value is an array
                         updateStatus(items.size());
 
                         // loop through our previews
                         for (int i = 0,len = items.size(); i < len; i++) {
                             // get the service url
                             String url = items.get(i).isString().stringValue();
                             cmd.execute(url);
                         }
                     }
                 }
             }
 
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void constructPanel() {
         showStatus();
 
         createViews();
     }
 
     /**
      * Calls the tree URL service to fetch and show the URLs in a panel inserted as the first tab, if
      * URLs are not already present in the file manifest.
      */
     public void loadTreeTab() {
         treeUrlTabIndex = 0;
 
         JSONArray urls = getManifestTreeUrls();
         if (urls != null) {
             return;
         }
 
         TreeHyperlinkGridPanel pnlTreeUrlTab = addTreeTab(null);
 
         if (pnlTreeUrlTab != null) {
             pnlTreeUrlTab.callTreeCreateService();
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void registerEventHandlers() {
         EventBus eventbus = EventBus.getInstance();
 
         // handle window contents changed - we may need to update our header
         handlers.add(eventbus.addHandler(FileEditorWindowDirtyEvent.TYPE,
                 new FileEditorWindowDirtyEventHandler() {
                     @Override
                     public void onClean(FileEditorWindowDirtyEvent event) {
                         if (event.getFileId().equals(file.getFileId())) {
                             init();
                             isDirty = false;
                         }
                     }
 
                     @Override
                     public void onDirty(FileEditorWindowDirtyEvent event) {
                         if (event.getFileId().equals(file.getFileId())) {
                             setHeading("*" + file.getFilename()); //$NON-NLS-1$
                             isDirty = true;
                         }
                     }
                 }));
     }
 
     /**
      * {@inheritDoc}
      */
     // @Override
     // protected void updatePanelProvenance(String provenance) {
     // this.provenance = provenance;
     // panel.updateProvenance(provenance);
     // }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void onRender(Element parent, int index) {
         super.onRender(parent, index);
         add(panel);
     }
 
     @Override
     public void show() {
         super.show();
 
         // PDF files will not display any tabs in this window
         // so we'll hide this window since there are no tabs to render
         if (isPdfPanel) {
             hide();
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void doHide() {
         if (isDirty) {
             final MessageBox msgBox = MessageBox.confirm(I18N.DISPLAY.confirmAction(),
                     I18N.DISPLAY.closeDirtyWindow(), null);
 
             msgBox.close();
 
             final Listener<MessageBoxEvent> callback = new Listener<MessageBoxEvent>() {
                 @Override
                 public void handleEvent(MessageBoxEvent ce) {
                     Button btn = ce.getButtonClicked();
 
                     if (!btn.getText().equalsIgnoreCase("NO")) { //$NON-NLS-1$
                         hide();
                     }
 
                     msgBox.close();
                 }
             };
 
             msgBox.addCallback(callback);
             msgBox.show();
         } else {
             hide();
         }
     }
 
     abstract class FetchDataCommand implements RPCSuccessCommand {
         protected abstract void addTab(String result);
 
         @Override
         public void execute(String url) {
             FileEditorServiceFacade facade = new FileEditorServiceFacade();
             facade.getData(url, new DiskResourceServiceCallback() {
                 @Override
                 public void onFailure(Throwable caught) {
                     updateStatus(-1);
                     super.onFailure(caught);
                 }
 
                 @Override
                 public void onSuccess(String result) {
                     updateStatus(-1);
                     addTab(result);
                 }
 
                 @Override
                 protected String getErrorMessageDefault() {
                     return I18N.ERROR.unableToRetrieveFileData(file.getFilename());
                 }
 
                 @Override
                 protected String getErrorMessageByCode(ErrorCode code, JSONObject jsonError) {
                     return getErrorMessageForFiles(code, file.getFilename());
                 }
             });
         }
     }
 
     class RawDataSuccessCommand extends FetchDataCommand {
         @Override
         public void addTab(String result) {
             // add a raw data tab
             RawDataPanel panelRaw = new RawDataPanel(file, result);
             panelRaw.setTabIndex(treeUrlTabIndex == 0 ? 1 : 0);
             panel.addTab(panelRaw);
         }
     }
 
     class ImageDataSuccessCommand implements RPCSuccessCommand {
         @Override
         public void execute(String result) {
             updateStatus(-1);
             getImage(result);
         }
     }
 
     class PdfDataSuccessCommand implements RPCSuccessCommand {
         @Override
         public void execute(String result) {
             updateStatus(-1);
             getPdfFile(result);
         }
     }
 
     class PreviewSuccessCommand extends FetchDataCommand {
         @Override
         public void addTab(String result) {
             FilePreviewPanel previewPanel = new FilePreviewPanel(file, JsonUtil.getString(
                     JsonUtil.getObject(result), "preview"), false); //$NON-NLS-1$
 
             panel.addTab(previewPanel);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void fileSavedAs(String idOrig, String idParent, File info) {
         // did we get saved as something else?
         if (idParent.equals(file.getParentId()) && idOrig.equals(file.getFileId())) {
             file = new FileIdentifier(info.getName(), idParent, info.getId());
             init();
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void folderCreated(final String idParentFolder, final JSONObject jsonFolder) {
         // intentionally do nothing
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void fileRename(final String id, final String name) {
         // has our file been renamed?
         if (file.getFileId().equals(id)) {
             // we need to reset our heading and update our provenance
             setHeading(name);
             // updateProvenance();
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void folderRename(final String id, final String name) {
         // intentionally do nothing
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void deleteResources(List<String> folders, List<String> files) {
         // intentionally do nothing
     }
 
     @Override
     public void addFile(String path, File info) {
         // intentionally do nothing.
     }
 
     @Override
     public void fileMove(Map<String, String> files) {
         // intentionally do nothing... for now
     }
 
     @Override
     public void folderMove(Map<String, String> folders) {
         // intentionally do nothing... for now
     }
 }
