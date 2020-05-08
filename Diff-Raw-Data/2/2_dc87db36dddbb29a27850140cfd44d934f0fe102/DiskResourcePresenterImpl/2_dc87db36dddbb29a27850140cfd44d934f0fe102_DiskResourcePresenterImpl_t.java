 package org.iplantc.de.diskResource.client.presenters;
 
 import org.iplantc.de.client.events.EventBus;
 import org.iplantc.de.client.events.diskResources.DiskResourceRefreshEvent;
 import org.iplantc.de.client.models.HasId;
 import org.iplantc.de.client.models.HasPaths;
 import org.iplantc.de.client.models.UserInfo;
 import org.iplantc.de.client.models.diskResources.DiskResource;
 import org.iplantc.de.client.models.diskResources.DiskResourceAutoBeanFactory;
 import org.iplantc.de.client.models.diskResources.Folder;
 import org.iplantc.de.client.models.search.DiskResourceQueryTemplate;
 import org.iplantc.de.client.services.DiskResourceServiceFacade;
 import org.iplantc.de.client.util.DiskResourceUtil;
 import org.iplantc.de.client.util.JsonUtil;
 import org.iplantc.de.commons.client.ErrorHandler;
 import org.iplantc.de.commons.client.info.ErrorAnnouncementConfig;
 import org.iplantc.de.commons.client.info.IplantAnnouncer;
 import org.iplantc.de.commons.client.views.gxt3.dialogs.IPlantDialog;
 import org.iplantc.de.diskResource.client.dataLink.presenter.DataLinkPresenter;
 import org.iplantc.de.diskResource.client.dataLink.view.DataLinkPanel;
 import org.iplantc.de.diskResource.client.events.CreateNewFileEvent;
 import org.iplantc.de.diskResource.client.events.DiskResourceRenamedEvent;
 import org.iplantc.de.diskResource.client.events.DiskResourceSelectedEvent;
 import org.iplantc.de.diskResource.client.events.DiskResourcesDeletedEvent;
 import org.iplantc.de.diskResource.client.events.DiskResourcesMovedEvent;
 import org.iplantc.de.diskResource.client.events.FolderCreatedEvent;
 import org.iplantc.de.diskResource.client.events.FolderSelectedEvent;
 import org.iplantc.de.diskResource.client.events.RequestBulkDownloadEvent;
 import org.iplantc.de.diskResource.client.events.RequestBulkUploadEvent;
 import org.iplantc.de.diskResource.client.events.RequestImportFromUrlEvent;
 import org.iplantc.de.diskResource.client.events.RequestSimpleDownloadEvent;
 import org.iplantc.de.diskResource.client.events.RequestSimpleUploadEvent;
 import org.iplantc.de.diskResource.client.metadata.presenter.DiskResourceMetadataUpdateCallback;
 import org.iplantc.de.diskResource.client.metadata.presenter.MetadataPresenter;
 import org.iplantc.de.diskResource.client.metadata.view.DiskResourceMetadataView;
 import org.iplantc.de.diskResource.client.presenters.callbacks.CreateFolderCallback;
 import org.iplantc.de.diskResource.client.presenters.callbacks.DiskResourceDeleteCallback;
 import org.iplantc.de.diskResource.client.presenters.callbacks.DiskResourceMoveCallback;
 import org.iplantc.de.diskResource.client.presenters.callbacks.DiskResourceRestoreCallback;
 import org.iplantc.de.diskResource.client.presenters.callbacks.GetDiskResourceDetailsCallback;
 import org.iplantc.de.diskResource.client.presenters.callbacks.RenameDiskResourceCallback;
 import org.iplantc.de.diskResource.client.presenters.handlers.CachedFolderTreeStoreBinding;
 import org.iplantc.de.diskResource.client.presenters.handlers.DiskResourcesEventHandler;
 import org.iplantc.de.diskResource.client.presenters.handlers.ToolbarButtonVisibilityGridHandler;
 import org.iplantc.de.diskResource.client.presenters.handlers.ToolbarButtonVisibilityNavigationHandler;
 import org.iplantc.de.diskResource.client.presenters.proxy.FolderContentsLoadConfig;
 import org.iplantc.de.diskResource.client.presenters.proxy.FolderContentsRpcProxy;
 import org.iplantc.de.diskResource.client.presenters.proxy.SelectDiskResourceByIdStoreAddHandler;
 import org.iplantc.de.diskResource.client.presenters.proxy.SelectFolderByIdLoadHandler;
 import org.iplantc.de.diskResource.client.search.events.SaveDiskResourceQueryEvent;
 import org.iplantc.de.diskResource.client.search.events.SubmitDiskResourceQueryEvent;
 import org.iplantc.de.diskResource.client.search.presenter.DataSearchPresenter;
 import org.iplantc.de.diskResource.client.sharing.views.DataSharingDialog;
 import org.iplantc.de.diskResource.client.views.DiskResourceView;
 import org.iplantc.de.diskResource.client.views.dialogs.FolderSelectDialog;
 import org.iplantc.de.diskResource.client.views.dialogs.InfoTypeEditorDialog;
 import org.iplantc.de.diskResource.client.views.widgets.DiskResourceViewToolbar;
 import org.iplantc.de.resources.client.messages.I18N;
 import org.iplantc.de.resources.client.messages.IplantDisplayStrings;
 
 import com.google.common.base.Strings;
 import com.google.common.collect.Sets;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.event.logical.shared.SelectionHandler;
 import com.google.gwt.event.shared.EventHandler;
 import com.google.gwt.event.shared.HandlerRegistration;
 import com.google.gwt.json.client.JSONArray;
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.json.client.JSONString;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HasOneWidget;
 import com.google.gwt.user.client.ui.IsWidget;
 import com.google.inject.Inject;
 
 import com.sencha.gxt.data.shared.SortInfo;
 import com.sencha.gxt.data.shared.loader.LoadHandler;
 import com.sencha.gxt.data.shared.loader.PagingLoadResult;
 import com.sencha.gxt.data.shared.loader.PagingLoader;
 import com.sencha.gxt.data.shared.loader.TreeLoader;
 import com.sencha.gxt.widget.core.client.Dialog.PredefinedButton;
 import com.sencha.gxt.widget.core.client.box.ConfirmMessageBox;
 import com.sencha.gxt.widget.core.client.box.MessageBox;
 import com.sencha.gxt.widget.core.client.event.HideEvent;
 import com.sencha.gxt.widget.core.client.event.HideEvent.HideHandler;
 import com.sencha.gxt.widget.core.client.event.SelectEvent;
 import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
 import com.sencha.gxt.widget.core.client.info.Info;
 import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent.SelectionChangedHandler;
 import com.sencha.gxt.widget.core.client.tree.Tree.TreeNode;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.Set;
 
 /**
  * 
  * @author jstroot
  * 
  */
 public class DiskResourcePresenterImpl implements DiskResourceView.Presenter {
 
     final DiskResourceView view;
     private final DiskResourceView.Proxy proxy;
     private final TreeLoader<Folder> treeLoader;
     private final HashMap<EventHandler, HandlerRegistration> registeredHandlers = new HashMap<EventHandler, HandlerRegistration>();
     private final List<HandlerRegistration> dreventHandlers = new ArrayList<HandlerRegistration>();
     private final DiskResourceServiceFacade diskResourceService;
     private final IplantDisplayStrings DISPLAY;
     private final DiskResourceAutoBeanFactory drFactory;
     private final Builder builder;
     private FolderContentsRpcProxy rpc_proxy;
     private PagingLoader<FolderContentsLoadConfig, PagingLoadResult<DiskResource>> gridLoader;
     private DataSearchPresenter dataSearchPresenter;
 
     @Inject
     public DiskResourcePresenterImpl(final DiskResourceView view, final DiskResourceView.Proxy proxy, final FolderContentsRpcProxy folderRpcProxy, final DiskResourceServiceFacade diskResourceService,
             final IplantDisplayStrings display, final DiskResourceAutoBeanFactory factory, final DataSearchPresenter dataSearchPresenter) {
         this.view = view;
         this.proxy = proxy;
         this.rpc_proxy = folderRpcProxy;
         this.diskResourceService = diskResourceService;
         this.DISPLAY = display;
         this.drFactory = factory;
         this.dataSearchPresenter = dataSearchPresenter;
 
         builder = new MyBuilder(this);
 
         treeLoader = new TreeLoader<Folder>(this.proxy) {
             @Override
             public boolean hasChildren(Folder parent) {
                 return parent.hasSubDirs();
             }
         };
 
         this.proxy.init(dataSearchPresenter, this);
         this.dataSearchPresenter.searchInit(getView(), getView(), this, getView().getTreeStore(), getView().getToolbar().getSearchField());
         this.rpc_proxy.init(view.getCenterPanelHeader());
 
         this.view.setTreeLoader(treeLoader);
         this.view.setPresenter(this);
         this.view.addFolderSelectedEventHandler(this);
 
         initHandlers();
         initDragAndDrop();
         loadUserTrashPath();
         initFolderContentRpc();
 
     }
 
     private void initFolderContentRpc() {
         gridLoader = new PagingLoader<FolderContentsLoadConfig, PagingLoadResult<DiskResource>>(rpc_proxy);
         gridLoader.useLoadConfig(new FolderContentsLoadConfig());
         gridLoader.setReuseLoadConfig(true);
         view.setViewLoader(gridLoader);
     }
 
     private void initDragAndDrop() {
 
     }
 
     @Override
     public void loadUserTrashPath() {
         final String userName = UserInfo.getInstance().getUsername();
         diskResourceService.getUserTrashPath(userName, new AsyncCallback<String>() {
 
             @Override
             public void onFailure(Throwable caught) {
                 // best guess of user trash. this is horrible
                 UserInfo.getInstance().setTrashPath("/iplant/trash/home/rods/" + userName); //$NON-NLS-1$
             }
 
             @Override
             public void onSuccess(String result) {
                 JSONObject obj = JsonUtil.getObject(result);
                 UserInfo.getInstance().setTrashPath(JsonUtil.getString(obj, "path")); //$NON-NLS-1$
             }
         });
     }
 
     private void initHandlers() {
         // Add selection handlers which will control the visibility of the
         // toolbar buttons
         DiskResourceViewToolbar toolbar = view.getToolbar();
         initToolbar(toolbar);
         ToolbarButtonVisibilityNavigationHandler buttonVisNavHandler = new ToolbarButtonVisibilityNavigationHandler(toolbar);
         toolbar.getSearchField().addSubmitDiskResourceQueryEventHandler(buttonVisNavHandler);
         addFolderSelectionHandler(buttonVisNavHandler);
         addFileSelectChangedHandler(new ToolbarButtonVisibilityGridHandler(toolbar));
 
         treeLoader.addLoadHandler(new CachedFolderTreeStoreBinding(view.getTreeStore()));
 
         EventBus eventBus = EventBus.getInstance();
         DiskResourcesEventHandler diskResourcesEventHandler = new DiskResourcesEventHandler(this);
         dreventHandlers.add(eventBus.addHandler(DiskResourceRefreshEvent.TYPE, diskResourcesEventHandler));
         dreventHandlers.add(eventBus.addHandler(DiskResourcesDeletedEvent.TYPE, diskResourcesEventHandler));
         dreventHandlers.add(eventBus.addHandler(FolderCreatedEvent.TYPE, diskResourcesEventHandler));
         dreventHandlers.add(eventBus.addHandler(DiskResourceRenamedEvent.TYPE, diskResourcesEventHandler));
         dreventHandlers.add(eventBus.addHandler(DiskResourceSelectedEvent.TYPE, diskResourcesEventHandler));
         dreventHandlers.add(eventBus.addHandler(DiskResourcesMovedEvent.TYPE, diskResourcesEventHandler));
     }
 
     private void initToolbar(DiskResourceViewToolbar toolbar) {
         // Disable all buttons, except for Uploads.
         toolbar.setNewFolderButtonEnabled(false);
         toolbar.setNewFileButtonEnabled(false);
         toolbar.setNewButtonEnabled(false);
         toolbar.setRefreshButtonEnabled(false);
         toolbar.setDownloadsEnabled(false);
         toolbar.setBulkDownloadButtonEnabled(false);
         toolbar.setSimpleDowloadButtonEnabled(false);
         toolbar.setRenameButtonEnabled(false);
         toolbar.setShareButtonEnabled(false);
         toolbar.setDeleteButtonEnabled(false);
         toolbar.setRestoreMenuItemEnabled(false);
         toolbar.setEditEnabled(false);
         toolbar.setMoveButtonEnabled(false);
     }
 
     @Override
     public void cleanUp() {
         EventBus eventBus = EventBus.getInstance();
         for (HandlerRegistration hr : dreventHandlers) {
             eventBus.removeHandler(hr);
         }
     }
 
     @Override
     public DiskResourceView getView() {
         return view;
     }
 
     @Override
     public void go(HasOneWidget container) {
         container.setWidget(view);
         // JDS Re-select currently selected folder in order to load center
         // panel.
         setSelectedFolderById(getSelectedFolder());
     }
 
     @Override
     public void go(HasOneWidget container, HasId folderToSelect, final List<? extends HasId> diskResourcesToSelect) {
 
         if ((folderToSelect == null) || Strings.isNullOrEmpty(folderToSelect.getId())) {
             go(container);
         } else {
             container.setWidget(view);
             setSelectedFolderById(folderToSelect);
             setSelectedDiskResourcesById(diskResourcesToSelect);
         }
     }
 
     @Override
     public void setSelectedDiskResourcesById(final List<? extends HasId> diskResourcesToSelect) {
         SelectDiskResourceByIdStoreAddHandler diskResourceStoreAddHandler = new SelectDiskResourceByIdStoreAddHandler(diskResourcesToSelect, this);
         HandlerRegistration diskResHandlerReg = view.getListStore().addStoreAddHandler(diskResourceStoreAddHandler);
         addEventHandlerRegistration(diskResourceStoreAddHandler, diskResHandlerReg);
     }
 
     @Override
     public void setSelectedFolderById(final HasId folderToSelect) {
         if ((folderToSelect == null) || Strings.isNullOrEmpty(folderToSelect.getId())) {
             return;
         }
 
         Folder folder = view.getFolderById(folderToSelect.getId());
         if (folder != null) {
             final Folder selectedFolder = view.getSelectedFolder();
             if (folder == selectedFolder) {
                 // If the folder IS the currently selected folder, then trigger
                 // reload of center panel.
                 onFolderSelected(folder);
             } else {
                 /*
                  * If it is NOT the currently selected folder, then deselect the
                  * current folder and re-select it.
                  */
                 view.deSelectNavigationFolder();
                 view.setSelectedFolder(folder);
             }
         } else {
             // Create and add the SelectFolderByIdLoadHandler to the treeLoader.
             final SelectFolderByIdLoadHandler handler = new SelectFolderByIdLoadHandler(folderToSelect, this, IplantAnnouncer.getInstance());
             HandlerRegistration reg = treeLoader.addLoadHandler(handler);
             addEventHandlerRegistration(handler, reg);
         }
     }
 
     @Override
     public Folder getSelectedFolder() {
         return view.getSelectedFolder();
     }
 
     @Override
     public Set<DiskResource> getSelectedDiskResources() {
         return view.getSelectedDiskResources();
     }
 
     @Override
     public void onFolderSelected(final Folder folder) {
         view.showDataListingWidget();
         view.deSelectDiskResources();
         FolderContentsLoadConfig config = gridLoader.getLastLoadConfig();
         config.setFolder(folder);
         gridLoader.load(0, 200);
     }
 
     @Override
     public void onFolderSelected(FolderSelectedEvent event) {
         if (event.getSelectedFolder() instanceof DiskResourceQueryTemplate) {
             // If the given query has not been saved, we need to deselect
             // everything
             if (!((DiskResourceQueryTemplate)event.getSelectedFolder()).isSaved()) {
                 deSelectDiskResources();
                 getView().deSelectNavigationFolder();
             }
             view.setAllowSelectAll(false);
         } else {
             view.setAllowSelectAll(true);
         }
         onFolderSelected(event.getSelectedFolder());
     }
 
     @Override
     public void onDiskResourceSelected(Set<DiskResource> selection) {
         if (selection != null && selection.size() == 1) {
             Iterator<DiskResource> it = selection.iterator();
             getDetails(it.next().getPath());
         } else {
             view.resetDetailsPanel();
         }
 
     }
 
     private void getDetails(String path) {
         JSONObject obj = new JSONObject();
         JSONArray arr = new JSONArray();
         arr.set(0, new JSONString(path));
         obj.put("paths", arr); //$NON-NLS-1$
         diskResourceService.getStat(obj.toString(), new GetDiskResourceDetailsCallback(this, path, drFactory));
 
     }
 
     @Override
     public void doBulkUpload() {
         EventBus.getInstance().fireEvent(new RequestBulkUploadEvent(this, getSelectedUploadFolder()));
     }
 
     @Override
     public void doSimpleUpload() {
         EventBus.getInstance().fireEvent(new RequestSimpleUploadEvent(this, getSelectedUploadFolder()));
     }
 
     @Override
     public void doImport() {
         EventBus.getInstance().fireEvent(new RequestImportFromUrlEvent(this, getSelectedUploadFolder()));
     }
 
     private Folder getSelectedUploadFolder() {
         Folder selectedFolder = getSelectedFolder();
 
         if (selectedFolder == null) {
             for (Folder root : view.getTreeStore().getRootItems()) {
                 if (root.getName().equals(UserInfo.getInstance().getUsername())) {
                     return root;
                 }
             }
         }
 
         return selectedFolder;
     }
 
     @Override
     public void doCreateNewFolder(final Folder parentFolder, final String newFolderName) {
         view.mask(DISPLAY.loadingMask());
         diskResourceService.createFolder(parentFolder, newFolderName, new CreateFolderCallback(parentFolder, view));
     }
 
     @Override
     public void doRefresh(Folder folder) {
         if (folder == null || Strings.isNullOrEmpty(folder.getId())) {
             dataSearchPresenter.refreshQuery();
             return;
         }
 
         EventBus.getInstance().fireEvent(new DiskResourceRefreshEvent(folder));
     }
 
     @Override
     public void onFolderRefresh(Folder folder) {
         folder = view.getFolderById(folder.getId());
         if (folder == null) {
             return;
         }
 
         Folder selectedFolder = getSelectedFolder();
         view.refreshFolder(folder);
 
         if (!(folder instanceof DiskResourceQueryTemplate) && selectedFolder == folder) {
             // If the folder is currently selected, then trigger reload of
             // center panel.
             onFolderSelected(folder);
         }
     }
 
     @Override
     public void doSimpleDownload() {
         EventBus.getInstance().fireEvent(new RequestSimpleDownloadEvent(this, getSelectedDiskResources(), getSelectedFolder()));
     }
 
     @Override
     public void doBulkDownload() {
         EventBus.getInstance().fireEvent(new RequestBulkDownloadEvent(this, view.isSelectAll(), getSelectedDiskResources(), getSelectedFolder()));
     }
 
     @Override
     public void doRename(final DiskResource dr, final String newName) {
         view.mask(DISPLAY.loadingMask());
         diskResourceService.renameDiskResource(dr, newName, new RenameDiskResourceCallback(dr, view));
     }
 
     @Override
     public void doShare() {
         DataSharingDialog dlg = new DataSharingDialog(getSelectedDiskResources());
         dlg.show();
         dlg.addOkButtonSelectHandler(new SelectHandler() {
 
             @Override
             public void onSelect(SelectEvent event) {
                 onDiskResourceSelected(getSelectedDiskResources());
             }
         });
     }
 
     @Override
     public void requestDelete() {
         doDelete();
     }
 
     @Override
     public void doDelete() {
         Set<DiskResource> selectedResources = getSelectedDiskResources();
         if (!selectedResources.isEmpty() && DiskResourceUtil.isOwner(selectedResources)) {
             HashSet<DiskResource> drSet = Sets.newHashSet(selectedResources);
 
             if (DiskResourceUtil.containsTrashedResource(drSet)) {
                 confirmDelete(drSet);
             } else {
                 delete(drSet, DISPLAY.deleteMsg());
             }
         }
     }
 
     private void confirmDelete(final Set<DiskResource> drSet) {
         final MessageBox confirm = new ConfirmMessageBox(DISPLAY.warning(), DISPLAY.emptyTrashWarning());
 
         confirm.addHideHandler(new HideHandler() {
             @Override
             public void onHide(HideEvent event) {
                 if (confirm.getHideButton() == confirm.getButtonById(PredefinedButton.YES.name())) {
                     delete(drSet, DISPLAY.deleteTrash());
                 }
             }
         });
 
         confirm.show();
     }
 
     private void delete(Set<DiskResource> drSet, String announce) {
         view.mask(DISPLAY.loadingMask());
         Folder selectedFolder = getSelectedFolder();
         final AsyncCallback<HasPaths> callback = new DiskResourceDeleteCallback(drSet, selectedFolder, view, announce);
 
         if (view.isSelectAll() && selectedFolder != null) {
            diskResourceService.deleteContents(selectedFolder.getPath(), callback);
 
         } else {
             diskResourceService.deleteDiskResources(drSet, callback);
         }
     }
 
     @Override
     public void doMetadata() {
         DiskResource selected = getSelectedDiskResources().iterator().next();
         final DiskResourceMetadataView mview = new DiskResourceMetadataView(selected);
         final DiskResourceMetadataView.Presenter p = new MetadataPresenter(selected, mview);
         final IPlantDialog ipd = new IPlantDialog(true);
 
         ipd.setSize("600", "400"); //$NON-NLS-1$ //$NON-NLS-2$
         ipd.setHeadingText(I18N.DISPLAY.metadata() + ":" + selected.getId()); //$NON-NLS-1$
         ipd.setResizable(true);
         ipd.addHelp(new HTML(I18N.HELP.metadataHelp()));
         p.go(ipd);
         if (selected.getPermissions().isWritable()) {
             ipd.setHideOnButtonClick(false);
 
             ipd.addOkButtonSelectHandler(new SelectHandler() {
 
                 @Override
                 public void onSelect(SelectEvent event) {
                     if (mview.isValid()) {
                         p.setDiskResourceMetaData(mview.getMetadataToAdd(), mview.getMetadataToDelete(), new DiskResourceMetadataUpdateCallback());
                         ipd.hide();
                     } else {
                         IplantAnnouncer.getInstance().schedule(new ErrorAnnouncementConfig(I18N.ERROR.metadataFormInvalid()));
                     }
                 }
             });
 
             ipd.addCancelButtonSelectHandler(new SelectHandler() {
 
                 @Override
                 public void onSelect(SelectEvent event) {
                     ipd.hide();
                 }
             });
         }
 
         ipd.show();
 
     }
 
     @Override
     public void doDataQuota() {
         Info.display("You clicked something!", "doDataQuota");
     }
 
     @Override
     public void addFileSelectChangedHandler(SelectionChangedHandler<DiskResource> selectionChangedHandler) {
         view.addDiskResourceSelectChangedHandler(selectionChangedHandler);
     }
 
     @Override
     public void addFolderSelectionHandler(SelectionHandler<Folder> selectionHandler) {
         view.addFolderSelectionHandler(selectionHandler);
     }
 
     @Override
     public void unregisterHandler(EventHandler handler) {
         if (registeredHandlers.containsKey(handler)) {
             registeredHandlers.remove(handler).removeHandler();
         }
     }
 
     @Override
     public void addEventHandlerRegistration(EventHandler handler, HandlerRegistration reg) {
         registeredHandlers.put(handler, reg);
     }
 
     @Override
     public boolean canDragDataToTargetFolder(final Folder targetFolder, final Collection<DiskResource> dropData) {
         if (targetFolder instanceof DiskResourceQueryTemplate) {
             return false;
         }
 
         if (targetFolder.isFilter()) {
             return false;
         }
 
         // Assuming that ownership is of no concern.
         for (DiskResource dr : dropData) {
             // if the resource is a direct child of target folder
             if (DiskResourceUtil.isChildOfFolder(targetFolder, dr)) {
                 return false;
             }
 
             if (dr instanceof Folder) {
                 if (targetFolder.getPath().equals(dr.getPath())) {
                     return false;
                 }
 
                 // cannot drag an ancestor (parent, grandparent, etc) onto a
                 // child and/or descendant
                 if (DiskResourceUtil.isDescendantOfFolder((Folder)dr, targetFolder)) {
                     return false;
                 }
             }
         }
 
         return true;
     }
 
     @Override
     public void doMoveDiskResources(Folder targetFolder, Set<DiskResource> resources) {
         Folder parent = getSelectedFolder();
         view.mask(I18N.DISPLAY.loadingMask());
         if (view.isSelectAll()) {
             diskResourceService.moveContents(parent.getPath(), targetFolder, new DiskResourceMoveCallback(view, true, parent, targetFolder, resources));
         } else {
             diskResourceService.moveDiskResources(resources, targetFolder, new DiskResourceMoveCallback(view, false, parent, targetFolder, resources));
         }
     }
 
     @Override
     public Set<? extends DiskResource> getDragSources(IsWidget source, Element dragStartEl) {
         // Verify the drag started from a valid item in the tree or grid, then
         // return the selected items.
         if (isViewGrid(source)) {
             Set<DiskResource> selectedResources = getSelectedDiskResources();
 
             if (!selectedResources.isEmpty()) {
                 // Verify the dragStartEl is a row within the grid.
                 Element targetRow = view.findGridRow(dragStartEl);
 
                 if (targetRow != null) {
                     int dropIndex = view.findRowIndex(targetRow);
 
                     DiskResource selDiskResource = view.getListStore().get(dropIndex);
                     if (selDiskResource != null) {
                         return Sets.newHashSet(selectedResources);
                     }
                 }
             }
         } else if (isViewTree(source) && (getSelectedFolder() != null)) {
             // Verify the dragStartEl is a folder within the tree.
             Folder srcFolder = getDropTargetFolder(source, dragStartEl);
 
             if (srcFolder != null) {
                 return Sets.newHashSet(srcFolder);
             }
         }
 
         return null;
     }
 
     @Override
     public Folder getDropTargetFolder(IsWidget target, Element eventTargetElement) {
         Folder ret = null;
         if (view.isViewTree(target) && (view.findTreeNode(eventTargetElement) != null)) {
             TreeNode<Folder> targetTreeNode = view.findTreeNode(eventTargetElement);
             ret = targetTreeNode.getModel();
         } else if (view.isViewGrid(target)) {
             Element targetRow = view.findGridRow(eventTargetElement).cast();
 
             if (targetRow != null) {
                 int dropIndex = view.findRowIndex(targetRow);
 
                 DiskResource selDiskResource = view.getListStore().get(dropIndex);
                 ret = (selDiskResource instanceof Folder) ? (Folder)selDiskResource : null;
             }
         }
         return ret;
     }
 
     @Override
     public boolean isViewGrid(IsWidget widget) {
         return view.isViewGrid(widget);
     }
 
     @Override
     public boolean isViewTree(IsWidget widget) {
         return view.isViewTree(widget);
     }
 
     @Override
     public Builder builder() {
         return builder;
     }
 
     @Override
     public void doSaveDiskResourceQueryTemplate(SaveDiskResourceQueryEvent event) {
 
     }
 
     @Override
     public void doSubmitDiskResourceQuery(SubmitDiskResourceQueryEvent event) {}
 
     private class MyBuilder implements Builder {
 
         private final DiskResourceView.Presenter presenter;
 
         public MyBuilder(DiskResourceView.Presenter presenter) {
             this.presenter = presenter;
         }
 
         @Override
         public void go(HasOneWidget container) {
             presenter.go(container);
         }
 
         @Override
         public Builder hideNorth() {
             presenter.getView().setNorthWidgetHidden(true);
             return this;
         }
 
         @Override
         public Builder hideWest() {
             presenter.getView().setWestWidgetHidden(true);
             return this;
         }
 
         @Override
         public Builder hideCenter() {
             presenter.getView().setCenterWidgetHidden(true);
             return this;
         }
 
         @Override
         public Builder hideEast() {
             presenter.getView().setEastWidgetHidden(true);
             return this;
         }
 
         @Override
         public Builder singleSelect() {
             presenter.getView().setSingleSelect();
             return this;
         }
 
         @Override
         public Builder disableFilePreview() {
             presenter.getView().disableFilePreview();
             return this;
         }
 
     }
 
     @Override
     public void deSelectDiskResources() {
         view.deSelectDiskResources();
     }
 
     @Override
     public void emptyTrash() {
         view.mask(I18N.DISPLAY.loadingMask());
         diskResourceService.emptyTrash(UserInfo.getInstance().getUsername(), new AsyncCallback<String>() {
 
             @Override
             public void onSuccess(String result) {
                 doRefresh(view.getFolderById(UserInfo.getInstance().getTrashPath()));
                 view.unmask();
             }
 
             @Override
             public void onFailure(Throwable caught) {
                 ErrorHandler.post(caught);
                 view.unmask();
             }
         });
 
     }
 
     @Override
     public void restore() {
         final Set<DiskResource> selectedResources = getSelectedDiskResources();
 
         if (selectedResources == null || selectedResources.isEmpty()) {
             return;
         }
 
         mask(""); //$NON-NLS-1$
 
         DiskResourceRestoreCallback callback = new DiskResourceRestoreCallback(this, drFactory, selectedResources);
         if (view.isSelectAll()) {
             diskResourceService.restoreAll(callback);
         } else {
             HasPaths request = drFactory.pathsList().as();
             request.setPaths(DiskResourceUtil.asStringIdList(selectedResources));
             diskResourceService.restoreDiskResource(request, callback);
         }
     }
 
     @Override
     public void doDataLinks() {
         IPlantDialog dlg = new IPlantDialog(true);
         dlg.setHeadingText(I18N.DISPLAY.manageDataLinks());
         dlg.setHideOnButtonClick(true);
         dlg.setWidth(550);
         dlg.setOkButtonText(I18N.DISPLAY.done());
         DataLinkPanel.Presenter<DiskResource> dlPresenter = new DataLinkPresenter<DiskResource>(new ArrayList<DiskResource>(getSelectedDiskResources()));
         dlPresenter.go(dlg);
         dlg.addHelp(new HTML(I18N.HELP.manageDataLinksHelp()));
         dlg.show();
     }
 
     @Override
     public void OnInfoTypeClick(final String id, final String type) {
         final InfoTypeEditorDialog dialog = new InfoTypeEditorDialog(type);
         dialog.show();
         dialog.addOkButtonSelectHandler(new SelectHandler() {
 
             @Override
             public void onSelect(SelectEvent event) {
                 String newType = dialog.getSelectedValue();
                 setInfoType(id, newType);
             }
         });
 
     }
 
     @Override
     public void onMove() {
         final FolderSelectDialog fsd = new FolderSelectDialog();
         fsd.show();
         fsd.addOkButtonSelectHandler(new SelectHandler() {
 
             @Override
             public void onSelect(SelectEvent event) {
                 Folder targetFolder = fsd.getValue();
                 final Set<DiskResource> selectedResources = getSelectedDiskResources();
                 if (DiskResourceUtil.isMovable(targetFolder, selectedResources)) {
                     if (canDragDataToTargetFolder(targetFolder, selectedResources)) {
                         doMoveDiskResources(targetFolder, selectedResources);
                     } else {
                         IplantAnnouncer.getInstance().schedule(new ErrorAnnouncementConfig(I18N.ERROR.diskResourceIncompleteMove()));
                         view.unmask();
                     }
                 } else {
                     IplantAnnouncer.getInstance().schedule(new ErrorAnnouncementConfig(I18N.ERROR.permissionErrorMessage()));
                     view.unmask();
                 }
             }
         });
 
     }
 
     @Override
     public void updateSortInfo(SortInfo sortInfo) {
         FolderContentsLoadConfig config = gridLoader.getLastLoadConfig();
         config.setSortInfo(Arrays.asList(sortInfo));
         gridLoader.load();
     }
 
     @Override
     public void onNewFile() {
         CreateNewFileEvent event = new CreateNewFileEvent(getSelectedUploadFolder().getPath());
         EventBus.getInstance().fireEvent(event);
     }
 
     @Override
     public void mask(String loadingMask) {
         view.mask((Strings.isNullOrEmpty(loadingMask)) ? DISPLAY.loadingMask() : loadingMask);
     }
 
     @Override
     public void unmask() {
         boolean hasLoadHandlers = false;
         for (Entry<EventHandler, HandlerRegistration> entry : registeredHandlers.entrySet()) {
             if (entry.getKey() instanceof LoadHandler<?, ?>) {
                 hasLoadHandlers = true;
             }
 
         }
         if (!hasLoadHandlers) {
             view.unmask();
         }
 
     }
 
     @Override
     public void resetInfoType() {
         if (getSelectedDiskResources().size() > 0) {
             Iterator<DiskResource> it = getSelectedDiskResources().iterator();
             if (it != null && it.hasNext()) {
                 setInfoType(it.next().getPath(), "");
             }
         }
     }
 
     private void setInfoType(final String id, String newType) {
         diskResourceService.setFileType(id, newType, new AsyncCallback<String>() {
 
             @Override
             public void onFailure(Throwable arg0) {
                 ErrorHandler.post(arg0);
             }
 
             @Override
             public void onSuccess(String arg0) {
                 getDetails(id);
             }
         });
     }
 
 }
