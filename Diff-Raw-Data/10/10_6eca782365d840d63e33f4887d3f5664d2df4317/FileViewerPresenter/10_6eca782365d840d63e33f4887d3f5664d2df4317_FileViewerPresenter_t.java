 package org.iplantc.de.client.viewer.presenter;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.iplantc.core.jsonutil.JsonUtil;
 import org.iplantc.core.uicommons.client.ErrorHandler;
 import org.iplantc.core.uicommons.client.models.diskresources.File;
 import org.iplantc.core.uicommons.client.views.gxt3.dialogs.IplantInfoBox;
 import org.iplantc.de.client.I18N;
 import org.iplantc.de.client.Services;
 import org.iplantc.de.client.viewer.commands.ViewCommand;
 import org.iplantc.de.client.viewer.factory.MimeTypeViewerResolverFactory;
 import org.iplantc.de.client.viewer.models.MimeType;
 import org.iplantc.de.client.viewer.models.TreeUrlAutoBeanFactory;
 import org.iplantc.de.client.viewer.models.VizUrlList;
 import org.iplantc.de.client.viewer.models.VizUrl;
 import org.iplantc.de.client.viewer.views.FileViewer;
 import org.iplantc.de.client.views.windows.FileViewerWindow;
 
 import com.google.common.base.Strings;
 import com.google.gwt.core.shared.GWT;
 import com.google.gwt.json.client.JSONArray;
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.json.client.JSONString;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HasOneWidget;
 import com.google.web.bindery.autobean.shared.AutoBean;
 import com.google.web.bindery.autobean.shared.AutoBeanCodex;
 import com.sencha.gxt.widget.core.client.Dialog.PredefinedButton;
 import com.sencha.gxt.widget.core.client.box.ConfirmMessageBox;
 import com.sencha.gxt.widget.core.client.event.HideEvent;
 import com.sencha.gxt.widget.core.client.event.HideEvent.HideHandler;
 
 /**
  * @author sriram
  * 
  */
 public class FileViewerPresenter implements FileViewer.Presenter {
 
     // A presenter can handle more than one view of the same data at a time
     private final List<FileViewer> viewers;
 
     private FileViewerWindow container;
 
     /**
      * The file shown in the window.
      */
     private final File file;
 
     /**
      * The manifest of file contents
      */
     private final JSONObject manifest;
 
     private boolean treeViewer;
 
     private boolean genomeViewer;
 
     private final TreeUrlAutoBeanFactory factory = GWT.create(TreeUrlAutoBeanFactory.class);
 
     public FileViewerPresenter(File file, JSONObject manifest) {
         this.manifest = manifest;
         viewers = new ArrayList<FileViewer>();
         this.file = file;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * org.iplantc.core.uicommons.client.presenter.Presenter#go(com.google.gwt.user.client.ui.HasOneWidget
      * )
      */
     @Override
     public void go(HasOneWidget container) {
         this.container = (FileViewerWindow)container;
         composeView(manifest);
     }
 
     private boolean checkManifest(JSONObject obj) {
         if (obj == null) {
             return false;
         }
         String info_type = JsonUtil.getString(obj, "info-type");
         if (info_type == null || info_type.isEmpty()) {
             return false;
         }
 
         return true;
     }
 
     private boolean isTreeTab(JSONObject obj) {
         if (checkManifest(obj)) {
             String info_type = JsonUtil.getString(obj, "info-type");
             return (info_type.equalsIgnoreCase("nexus") || info_type.equalsIgnoreCase("nexml")
                     || info_type.equalsIgnoreCase("newick") || info_type.equalsIgnoreCase("phyloxml"));
         }
 
         return false;
 
     }
 
     private boolean isGenomeVizTab(JSONObject obj) {
         if (checkManifest(obj)) {
             String info_type = JsonUtil.getString(obj, "info-type");
             return (info_type.equals("fasta"));
         }
 
         return false;
     }
 
     @Override
     public void composeView(JSONObject manifest) {
         container.mask(I18N.DISPLAY.loadingMask());
         String mimeType = JsonUtil.getString(manifest, "content-type");
         ViewCommand cmd = MimeTypeViewerResolverFactory.getViewerCommand(MimeType
                 .fromTypeString(mimeType));
         String infoType = JsonUtil.getString(manifest, "info-type");
         List<? extends FileViewer> viewers_list = cmd.execute(file, infoType);
 
         if (viewers_list != null && viewers_list.size() > 0) {
             viewers.addAll(viewers_list);
             for (FileViewer view : viewers) {
                 container.getWidget().add(view.asWidget(), view.getViewName());
             }
             container.unmask();
         }
 
         treeViewer = isTreeTab(manifest);
         /**
          * XXX - SRIRAM 12/10/2013 Disabling Coge integrartion since it not complete yet.
          * 
          */
         genomeViewer = isGenomeVizTab(manifest);
         if (treeViewer || genomeViewer) {
             cmd = MimeTypeViewerResolverFactory.getViewerCommand(MimeType.fromTypeString("viz"));
             List<? extends FileViewer> vizViewers = cmd.execute(file, infoType);
             List<VizUrl> urls = getManifestVizUrls();
             if (urls != null && urls.size() > 0) {
                 vizViewers.get(0).setData(urls);
             } else {
                 if (treeViewer) {
                     callTreeCreateService(vizViewers.get(0));
                 } else if (genomeViewer) {
                    final ConfirmMessageBox cmb = new ConfirmMessageBox("Visualization",
                             "Do you like to load this genome in CoGe ?");
                     cmb.addHideHandler(new HideHandler() {
 
                         @Override
                         public void onHide(HideEvent event) {
                             if (cmb.getHideButton() == cmb.getButtonById(PredefinedButton.YES.name())) {
                                 loadInCoge(file);
                             }
                             // else do nothing
 
                         }
                     });
                     cmb.show();
                 }
             }
 
             viewers.add(vizViewers.get(0));
             container.getWidget().add(vizViewers.get(0).asWidget(), vizViewers.get(0).getViewName());
         }
 
         if (viewers.size() == 0) {
             container.unmask();
             container.add(new HTML(I18N.DISPLAY.fileOpenMsg()));
         }
 
     }
 
     /**
      * Gets the tree-urls json array from the manifest.
      * 
      * @return A json array of at least one tree URL, or null otherwise.
      */
     private List<VizUrl> getManifestVizUrls() {
         return getTreeUrls(manifest.toString());
 
     }
 
     private List<VizUrl> getTreeUrls(String urls) {
         if (urls != null) {
             AutoBean<VizUrlList> bean = AutoBeanCodex.decode(factory, VizUrlList.class, urls.toString());
             return bean.as().getUrls();
         }
 
         return null;
     }
 
     /**
      * Calls the tree URL service to fetch the URLs to display in the grid.
      */
     public void callTreeCreateService(final FileViewer viewer) {
         container.mask(I18N.DISPLAY.loadingMask());
 
         Services.FILE_EDITOR_SERVICE.getTreeUrl(file.getId(), new AsyncCallback<String>() {
             @Override
             public void onSuccess(String result) {
                 if (result != null && !result.isEmpty()) {
                     List<VizUrl> urlsList = getTreeUrls(result);
                     if (urlsList != null) {
                         viewer.setData(urlsList);
                         container.unmask();
                     } else {
                         container.unmask();
                         // couldn't find any tree URLs in the response, so display an error.
                         onFailure(new Exception(result));
                     }
 
                 } else {
                     // couldn't find any tree URLs in the response, so display an error.
                     onFailure(new Exception(result));
                     container.unmask();
                 }
 
             }
 
             @Override
             public void onFailure(Throwable caught) {
                 container.unmask();
 
                 String errMsg = I18N.ERROR.unableToRetrieveTreeUrls(file.getName());
                 ErrorHandler.post(errMsg, caught);
             }
         });
     }
 
     private void loadInCoge(File file) {
         container.mask(I18N.DISPLAY.loadingMask());
         JSONObject obj = new JSONObject();
         JSONArray pathArr = new JSONArray();
         pathArr.set(0, new JSONString(file.getPath()));
         obj.put("paths", pathArr);
         Services.FILE_EDITOR_SERVICE.viewGenomes(obj, new AsyncCallback<String>() {
 
             @Override
             public void onFailure(Throwable caught) {
                 container.unmask();
                 ErrorHandler.post("Unable to load genome in CoGe. Please try again later.", caught);
 
             }
 
             @Override
             public void onSuccess(String result) {
                 JSONObject resultObj = JsonUtil.getObject(result);
                 String url = JsonUtil.getString(resultObj, "coge_genome_url");
                 if (!Strings.isNullOrEmpty(url)) {
                    IplantInfoBox iib = new IplantInfoBox("CoGe", "Please click <a href='" + url
                            + "' target='_blank'>here</a> to load and visualize your genome in CoGe.");
                     iib.show();
                 } else {
                     onFailure(null);
                 }
                container.unmask();
             }
         });
     }
 }
