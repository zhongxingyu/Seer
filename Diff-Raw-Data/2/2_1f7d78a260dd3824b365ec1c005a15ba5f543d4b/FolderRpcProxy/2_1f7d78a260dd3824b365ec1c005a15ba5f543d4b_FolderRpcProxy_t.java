 package org.iplantc.core.uidiskresource.client.presenters.proxy;
 
 import java.util.List;
 
 import org.iplantc.core.resources.client.messages.I18N;
 import org.iplantc.core.uicommons.client.ErrorHandler;
 import org.iplantc.core.uicommons.client.gin.ServicesInjector;
 import org.iplantc.core.uicommons.client.models.diskresources.Folder;
 import org.iplantc.core.uicommons.client.models.diskresources.RootFolders;
 import org.iplantc.core.uicommons.client.services.DiskResourceServiceFacade;
 import org.iplantc.core.uidiskresource.client.views.DiskResourceView;
 
 import com.google.common.collect.Lists;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.sencha.gxt.data.client.loader.RpcProxy;
 
 public class FolderRpcProxy extends RpcProxy<Folder, List<Folder>> implements DiskResourceView.Proxy {
 
     private DiskResourceView.Presenter presenter;
     private final DiskResourceServiceFacade drService = ServicesInjector.INSTANCE
             .getDiskResourceServiceFacade();
 
     @Override
     public void load(final Folder parentFolder, final AsyncCallback<List<Folder>> callback) {
 
         if (parentFolder == null) {
             presenter.maskView();
             drService.getRootFolders(new AsyncCallback<RootFolders>() {
 
                 @Override
                 public void onSuccess(final RootFolders rootFolders) {
                     List<Folder> roots = rootFolders.getRoots();
                     if (callback != null) {
                         callback.onSuccess(roots);
                     }
                     presenter.unMaskView();
                 }
 
                 @Override
                 public void onFailure(Throwable caught) {
                     ErrorHandler.post(I18N.ERROR.retrieveFolderInfoFailed(), caught);
 
                     if (callback != null) {
                         callback.onFailure(caught);
                     }
                     presenter.unMaskView(true);
                 }
 
             });
         } else {
             if (parentFolder.isFilter()) {
                 if (callback != null) {
                     List<Folder> emptyResult = Lists.newArrayList();
                     callback.onSuccess(emptyResult);
                 }
                 return;
             }
 
            drService.getSubFolders(parentFolder, new AsyncCallback<List<Folder>>() {
 
                 @Override
                 public void onSuccess(List<Folder> result) {
                     if (callback != null) {
                         callback.onSuccess(result);
                     }
                 }
 
                 @Override
                 public void onFailure(Throwable caught) {
                     ErrorHandler.post(I18N.ERROR.retrieveFolderInfoFailed(), caught);
                     if (callback != null) {
                         callback.onFailure(caught);
                     }
                 }
             });
         }
     }
 
     @Override
     public void setPresenter(DiskResourceView.Presenter presenter) {
         this.presenter = presenter;
     }
 
 }
