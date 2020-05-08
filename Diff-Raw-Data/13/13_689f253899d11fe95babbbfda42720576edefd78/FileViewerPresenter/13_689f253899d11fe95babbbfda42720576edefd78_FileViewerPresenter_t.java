 package org.iplantc.de.client.viewer.presenter;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.iplantc.core.jsonutil.JsonUtil;
 import org.iplantc.core.uicommons.client.ErrorHandler;
 import org.iplantc.core.uicommons.client.models.diskresources.File;
 import org.iplantc.de.client.I18N;
 import org.iplantc.de.client.Services;
 import org.iplantc.de.client.viewer.commands.ViewCommand;
 import org.iplantc.de.client.viewer.factory.MimeTypeViewerResolverFactory;
 import org.iplantc.de.client.viewer.models.MimeType;
 import org.iplantc.de.client.viewer.models.TreeUrl;
 import org.iplantc.de.client.viewer.models.TreeUrlAutoBeanFactory;
 import org.iplantc.de.client.viewer.models.TreeUrlList;
 import org.iplantc.de.client.viewer.views.FileViewer;
 import org.iplantc.de.client.views.windows.FileViewerWindow;
 
 import com.google.gwt.core.shared.GWT;
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HasOneWidget;
 import com.google.web.bindery.autobean.shared.AutoBean;
 import com.google.web.bindery.autobean.shared.AutoBeanCodex;
 
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
 
     private final boolean treeViewer;
 
     private final TreeUrlAutoBeanFactory factory = GWT.create(TreeUrlAutoBeanFactory.class);
 
     public FileViewerPresenter(File file, JSONObject manifest, boolean treeViewer) {
         this.manifest = manifest;
         viewers = new ArrayList<FileViewer>();
         this.file = file;
         this.treeViewer = treeViewer;
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
 
     @Override
     public void composeView(JSONObject manifest) {
         container.mask(I18N.DISPLAY.loadingMask());
         String mimeType = JsonUtil.getString(manifest, "content-type");
         ViewCommand cmd = MimeTypeViewerResolverFactory.getViewerCommand(MimeType
                 .fromTypeString(mimeType));
         FileViewer viewer = cmd.execute(file);
 
         if (viewer != null) {
             viewers.add(viewer);
             container.getWidget().add(viewer.asWidget(), file.getName());
             container.unmask();
         }
 
         if (treeViewer) {
             cmd = MimeTypeViewerResolverFactory.getViewerCommand(MimeType.fromTypeString("tree"));
             FileViewer treeViewer = cmd.execute(file);
             List<TreeUrl> urls = getManifestTreeUrls();
             if (urls != null && urls.size() > 0) {
                 treeViewer.setData(urls);
             } else {
                 callTreeCreateService(treeViewer);
             }
 
             viewers.add(treeViewer);
             container.getWidget().add(treeViewer.asWidget(), "Tree view");
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
     private List<TreeUrl> getManifestTreeUrls() {
         return getTreeUrls(manifest.toString());
 
     }
 
     private List<TreeUrl> getTreeUrls(String urls) {
         if (urls != null) {
             AutoBean<TreeUrlList> bean = AutoBeanCodex.decode(factory, TreeUrlList.class,
                     urls.toString());
             return bean.as().getTreeUrls();
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
                     List<TreeUrl> urlsList = getTreeUrls(result);
                     if (urlsList != null) {
                         viewer.setData(urlsList);
                         container.unmask();
                     } else {
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
 
 }
