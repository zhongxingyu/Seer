 package org.iplantc.de.client.views;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.iplantc.core.jsonutil.JsonUtil;
 import org.iplantc.core.uicommons.client.events.EventBus;
 import org.iplantc.core.uicommons.client.models.DEProperties;
 import org.iplantc.core.uicommons.client.models.UserInfo;
 import org.iplantc.core.uicommons.client.views.dialogs.IPlantDialog;
 import org.iplantc.core.uicommons.client.views.panels.IPlantDialogPanel;
 import org.iplantc.core.uidiskresource.client.models.DiskResource;
 import org.iplantc.core.uidiskresource.client.models.File;
 import org.iplantc.core.uidiskresource.client.models.Folder;
 import org.iplantc.de.client.I18N;
 import org.iplantc.de.client.dataLink.presenter.DataLinkPresenter;
 import org.iplantc.de.client.dataLink.view.DataLinkPanel;
 import org.iplantc.de.client.dispatchers.IDropLiteWindowDispatcher;
 import org.iplantc.de.client.dispatchers.SimpleDownloadWindowDispatcher;
 import org.iplantc.de.client.events.DiskResourceSelectionChangedEvent;
 import org.iplantc.de.client.events.DiskResourceSelectionChangedEventHandler;
 import org.iplantc.de.client.events.ManageDataRefreshEvent;
 import org.iplantc.de.client.images.Resources;
 import org.iplantc.de.client.services.DiskResourceCopyCallback;
 import org.iplantc.de.client.services.DiskResourceDeleteCallback;
 import org.iplantc.de.client.services.DiskResourceServiceCallback;
 import org.iplantc.de.client.services.DiskResourceServiceFacade;
 import org.iplantc.de.client.utils.DataUtils;
 import org.iplantc.de.client.utils.DataUtils.Action;
 import org.iplantc.de.client.utils.DataViewContextExecutor;
 import org.iplantc.de.client.utils.NotifyInfo;
 import org.iplantc.de.client.utils.TreeViewContextExecutor;
 import org.iplantc.de.client.utils.builders.context.DataContextBuilder;
 import org.iplantc.de.client.views.dialogs.DataSharingDialog;
 import org.iplantc.de.client.views.dialogs.MetadataEditorDialog;
 import org.iplantc.de.client.views.panels.AddFolderDialogPanel;
 import org.iplantc.de.client.views.panels.DiskresourceMetadataEditorPanel;
 import org.iplantc.de.client.views.panels.MetadataEditorPanel;
 import org.iplantc.de.client.views.panels.RenameFileDialogPanel;
 import org.iplantc.de.client.views.panels.RenameFolderDialogPanel;
 
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.event.MenuEvent;
 import com.extjs.gxt.ui.client.event.MessageBoxEvent;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.widget.Component;
 import com.extjs.gxt.ui.client.widget.Dialog;
 import com.extjs.gxt.ui.client.widget.MessageBox;
 import com.extjs.gxt.ui.client.widget.button.ToolButton;
 import com.extjs.gxt.ui.client.widget.layout.FitLayout;
 import com.extjs.gxt.ui.client.widget.menu.Menu;
 import com.extjs.gxt.ui.client.widget.menu.MenuItem;
 import com.extjs.gxt.ui.client.widget.tips.ToolTipConfig;
 import com.google.gwt.event.shared.HandlerRegistration;
 import com.google.gwt.json.client.JSONArray;
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.json.client.JSONString;
 import com.google.gwt.resources.client.ImageResource;
 import com.google.gwt.user.client.ui.AbstractImagePrototype;
 
 public final class DataActionsMenu extends Menu {
     private static final String MI_ADD_FOLDER_ID = "idDataActionsMenuAddFolder"; //$NON-NLS-1$
     private static final String MI_RENAME_RESOURCE_ID = "idDataActionsMenuRename"; //$NON-NLS-1$
     private static final String MI_VIEW_RESOURCE_ID = "idDataActionsMenuView"; //$NON-NLS-1$
     private static final String MI_VIEW_RAW_ID = "idDataActionsMenuViewRaw"; //$NON-NLS-1$
     private static final String MI_VIEW_TREE_ID = "idDataActionsMenuViewTree"; //$NON-NLS-1$
     private static final String MI_DOWNLOAD_RESOURCE_ID = "idDataActionsMenuDownload"; //$NON-NLS-1$
     private static final String MI_SIMPLE_DOWNLOAD_ID = "idDataActionsMenuSimpleDownload"; //$NON-NLS-1$
     private static final String MI_BULK_DOWNLOAD_ID = "idDataActionsMenuBulkDownload"; //$NON-NLS-1$
     private static final String MI_DELETE_RESOURCE_ID = "idDataActionsMenuDelete"; //$NON-NLS-1$
     private static final String MI_METADATA_ID = "idDataActionsMenuMetadata"; //$NON-NLS-1$
     private static final String MI_SHARE_RESOURCE_ID = "idDataActionsMenuShare"; //$NON-NLS-1$
     private static final String MI_COPY_RESOURCE_ID = "idDataActionsMenuCopy"; //$NON-NLS-1$
     private static final String MI_PASTE_RESOURCE_ID = "idDataActionsMenuPaste"; //$NON-NLS-1$
     private static final String MI_RESTORE_RESOURCE_ID = "idDataActionsMenuRestore"; //$NON-NLS-1$
     private static final String MI_MANAGE_DATA_LINKS_ID = "idDataActionsManageDataLinks";
 
     private final ArrayList<HandlerRegistration> handlers = new ArrayList<HandlerRegistration>();
 
     private final String tag;
 
     private List<DiskResource> resources;
     private List<DiskResource> copyBuffer;
 
     private String currentPage;
 
     private Component maskingParent;
 
     private MenuItem itemAddFolder;
     private MenuItem itemRenameResource;
     private MenuItem itemViewResource;
     private MenuItem itemViewRawResource;
     private MenuItem itemViewTree;
     private MenuItem itemDownloadResource;
     private MenuItem itemSimpleDownloadResource;
     private MenuItem itemBulkDownloadResource;
     private MenuItem itemDeleteResource;
     private MenuItem itemMetaData;
     private MenuItem itemShareResource;
     private MenuItem itemCopyResource;
     private MenuItem itemPasteResource;
     private MenuItem itemRestore;
     private MenuItem itemManageDataLinks;
 
     public DataActionsMenu(final String tag) {
         this.tag = tag;
         resources = Collections.<DiskResource> emptyList();
         initMenuItems();
         registerHandlers();
     }
 
     /**
      * Builds the menu items for adding, renaming, viewing, downloading, and deleting disk resources.
      */
     private void initMenuItems() {
         itemAddFolder = buildLeafMenuItem(MI_ADD_FOLDER_ID, I18N.DISPLAY.newFolder(),
                 Resources.ICONS.folderAdd(), new NewFolderListenerImpl());
         itemRenameResource = buildLeafMenuItem(MI_RENAME_RESOURCE_ID, I18N.DISPLAY.rename(),
                 Resources.ICONS.folderRename(), new RenameListenerImpl());
 
         itemViewRawResource = buildLeafMenuItem(MI_VIEW_RAW_ID, I18N.DISPLAY.viewRaw(),
                 Resources.ICONS.fileView(), new ViewListenerImpl());
         itemViewTree = buildLeafMenuItem(MI_VIEW_TREE_ID, I18N.DISPLAY.viewTreeViewer(),
                 Resources.ICONS.fileView(), new ViewTreeListenerImpl());
         itemViewResource = buildMenuMenuItem(MI_VIEW_RESOURCE_ID, I18N.DISPLAY.view(),
                 Resources.ICONS.fileView(), itemViewRawResource, itemViewTree);
 
         itemSimpleDownloadResource = buildLeafMenuItem(MI_SIMPLE_DOWNLOAD_ID,
                 I18N.DISPLAY.simpleDownload(), Resources.ICONS.download(),
                 new SimpleDownloadListenerImpl());
         itemBulkDownloadResource = buildLeafMenuItem(MI_BULK_DOWNLOAD_ID, I18N.DISPLAY.bulkDownload(),
                 Resources.ICONS.download(), new BulkDownloadListenerImpl());
         itemDownloadResource = buildMenuMenuItem(MI_DOWNLOAD_RESOURCE_ID, I18N.DISPLAY.download(),
                 Resources.ICONS.download(), itemSimpleDownloadResource, itemBulkDownloadResource);
 
         itemDeleteResource = buildLeafMenuItem(MI_DELETE_RESOURCE_ID, I18N.DISPLAY.delete(),
                 Resources.ICONS.folderDelete(), new DeleteListenerImpl());
         itemMetaData = buildLeafMenuItem(MI_METADATA_ID, I18N.DISPLAY.metadata(),
                 Resources.ICONS.metadata(), new MetadataListenerImpl());
         itemShareResource = buildLeafMenuItem(MI_SHARE_RESOURCE_ID, I18N.DISPLAY.manageSharing(),
                 Resources.ICONS.share(), new ShareResourceListenerImpl());
         itemCopyResource = buildLeafMenuItem(MI_COPY_RESOURCE_ID, I18N.DISPLAY.copy(),
                 Resources.ICONS.copy(), new CopyResourceListenerImpl());
         itemPasteResource = buildLeafMenuItem(MI_PASTE_RESOURCE_ID, I18N.DISPLAY.paste(),
                 Resources.ICONS.copy(), new PasteResourceListenerImpl());
 
         itemRestore = buildLeafMenuItem(MI_RESTORE_RESOURCE_ID, I18N.DISPLAY.restore(),
                 Resources.ICONS.arrow_undo(), new RestoreResourceListenerImpl());
         itemManageDataLinks = buildLeafMenuItem(MI_MANAGE_DATA_LINKS_ID, I18N.DISPLAY.manageDataLinks(),
                 Resources.ICONS.dataLink(), new ManageDatalinksListener());
 
         add(itemAddFolder);
         add(itemRenameResource);
         add(itemViewResource);
         // add(itemCopyResource);
         // add(itemPasteResource);
         add(itemDownloadResource);
         add(itemDeleteResource);
         add(itemMetaData);
         add(itemShareResource);
         add(itemRestore);
 
         if (DEProperties.getInstance().isTicketsEnabled()) {
             add(itemManageDataLinks);
         }
     }
 
     private MenuItem buildLeafMenuItem(final String id, final String text,
             final ImageResource iconResource, final SelectionListener<? extends MenuEvent> listener) {
         final MenuItem res = new MenuItem(text, listener);
         res.setId(id);
         res.setIcon(AbstractImagePrototype.create(iconResource));
         return res;
     }
 
     private MenuItem buildMenuMenuItem(final String id, final String text,
             final ImageResource iconResource, final MenuItem... items) {
         final Menu submenu = new Menu();
 
         for (MenuItem item : items) {
             submenu.add(item);
         }
 
         final MenuItem res = new MenuItem(text);
         res.setId(id);
         res.setIcon(AbstractImagePrototype.create(iconResource));
         res.setSubMenu(submenu);
         return res;
     }
 
     // changed from private to public so that i can re-add handlers after refresh.
     public void registerHandlers() {
         EventBus eventbus = EventBus.getInstance();
 
         handlers.add(eventbus.addHandler(DiskResourceSelectionChangedEvent.TYPE,
                 new DiskResourceSelectionChangedEventHandler() {
                     @Override
                     public void onChange(final DiskResourceSelectionChangedEvent event) {
                         if (event.getTag().equals(tag)) {
                             update(event.getSelected(), event.getCurrentPath());
                         }
                     }
                 }));
     }
 
     public void cleanup() {
         // unregister
         for (HandlerRegistration reg : handlers) {
             reg.removeHandler();
         }
 
         // clear our list
         handlers.clear();
     }
 
     public void update(List<DiskResource> resources, String currentPath) {
         currentPage = currentPath;
         this.resources = resources == null ? Collections.<DiskResource> emptyList() : resources;
         prepareMenuItems(DataUtils.getSupportedActions(this.resources, currentPage));
     }
 
     private void prepareMenuItems(final Iterable<Action> actions) {
         hideMenuItems(this);
         hideMenuItems(itemViewResource.getSubMenu());
         hideMenuItems(itemDownloadResource.getSubMenu());
 
         boolean folderActionsEnabled = DataUtils.hasFolders(resources);
 
         if (folderActionsEnabled && resources.size() == 1) {
             // Enable the "Add Folder" item as well.
             showMenuItem(itemAddFolder);
         }
 
         for (DataUtils.Action action : actions) {
             switch (action) {
                 case RenameFolder:
                     itemRenameResource.setIcon(AbstractImagePrototype.create(Resources.ICONS
                             .folderRename()));
                     showMenuItem(itemRenameResource);
                     break;
 
                 case RenameFile:
                     itemRenameResource.setIcon(AbstractImagePrototype.create(Resources.ICONS
                             .fileRename()));
                     showMenuItem(itemRenameResource);
                     break;
 
                 case View:
                     showMenuItem(itemViewResource);
                     showMenuItem(itemViewRawResource);
                     break;
 
                 case ViewTree:
                     showMenuItem(itemViewResource);
                     showMenuItem(itemViewTree);
                     break;
 
                 case SimpleDownload:
                     showMenuItem(itemDownloadResource);
                     showMenuItem(itemSimpleDownloadResource);
                     break;
 
                 case BulkDownload:
                     showMenuItem(itemDownloadResource);
                     showMenuItem(itemBulkDownloadResource);
                     break;
 
                 case Delete:
                     ImageResource delIcon = folderActionsEnabled ? Resources.ICONS.folderDelete()
                             : Resources.ICONS.fileDelete();
 
                     itemDeleteResource.setIcon(AbstractImagePrototype.create(delIcon));
                     showMenuItem(itemDeleteResource);
                     break;
                 case Metadata:
                     showMenuItem(itemMetaData);
                     break;
                 case Share:
                     showMenuItem(itemShareResource);
                     if (DEProperties.getInstance().isTicketsEnabled()) {
                         showMenuItem(itemManageDataLinks);
                     }
                     break;
                 // case Copy:
                 // showMenuItem(itemCopyResource);
                 // break;
                 // case Paste:
                 // showMenuItem(itemPasteResource);
                 // if (copyBuffer == null) {
                 // itemPasteResource.disable();
                 // }
                 // break;
                 case Restore:
                     showMenuItem(itemRestore);
                     break;
                 default:
                     break;
 
             }
         }
     }
 
     private void hideMenuItems(final Menu menu) {
         for (Component item : menu.getItems()) {
             item.disable();
             item.hide();
         }
     }
 
     private void showMenuItem(final MenuItem item) {
         item.enable();
         item.show();
     }
 
     public void setMaskingParent(final Component maskingParent) {
         this.maskingParent = maskingParent;
     }
 
     private final class ManageDatalinksListener extends SelectionListener<MenuEvent> {
 
         @Override
         public void componentSelected(MenuEvent ce) {
             // TODO JDS Move the creation of the dialog into the presenter.
             DataLinkPanel.Presenter<DiskResource> dlPresenter = new DataLinkPresenter<DiskResource>(
                     resources);
             Dialog dlg = new Dialog();
             dlg.setHeading(I18N.DISPLAY.manageDataLinks());
             dlg.setLayout(new FitLayout());
             dlg.setResizable(false);
             dlg.setModal(true);
             dlg.setHideOnButtonClick(true);
             dlg.setWidth(550);
             dlg.getButtonById(Dialog.OK).setText("Done");
             dlg.add(dlPresenter.getView().getDisplayWidget());
 
             ToolButton helpBtn = new ToolButton("x-tool-help");
             ToolTipConfig ttc = new ToolTipConfig();
             ttc.setMouseOffset(new int[] {0, 0});
             ttc.setAnchor("left");
             ttc.setTitle(I18N.DISPLAY.help());
             ttc.setText(I18N.HELP.manageDataLinksHelp());
             ttc.setCloseable(true);
             helpBtn.setToolTip(ttc);
             dlg.getHeader().addTool(helpBtn);
             dlg.show();
         }
     }
 
     private class NewFolderListenerImpl extends SelectionListener<MenuEvent> {
         @Override
         public void componentSelected(MenuEvent ce) {
             for (DiskResource resource : resources) {
                 if (resource instanceof Folder) {
                     if (resource != null && resource.getId() != null) {
                         IPlantDialog dlg = new IPlantDialog(I18N.DISPLAY.newFolder(), 340,
                                 new AddFolderDialogPanel(resource.getId(), maskingParent));
                         dlg.disableOkButton();
                         dlg.show();
                     }
                 }
             }
         }
     }
 
     private class RenameListenerImpl extends SelectionListener<MenuEvent> {
         @Override
         public void componentSelected(MenuEvent ce) {
             for (DiskResource resource : resources) {
                 if (DataUtils.isRenamable(resource)) {
                     IPlantDialogPanel panel = null;
                     if (resource instanceof Folder) {
                         panel = new RenameFolderDialogPanel(resource.getId(), resource.getName(),
                                 maskingParent);
                     } else if (resource instanceof File) {
                         panel = new RenameFileDialogPanel(resource.getId(), resource.getName(),
                                 maskingParent);
                     }
 
                     IPlantDialog dlg = new IPlantDialog(I18N.DISPLAY.rename(), 340, panel);
 
                     dlg.show();
                 } else {
                     showErrorMsg();
                 }
             }
         }
     }
 
     private class ShareResourceListenerImpl extends SelectionListener<MenuEvent> {
         @Override
         public void componentSelected(MenuEvent ce) {
             if (DataUtils.isSharable(resources)) {
                 DataSharingDialog sd = new DataSharingDialog(resources);
                 sd.initView();
                sd.show();
             } else {
                 showErrorMsg();
             }
         }
 
     }
 
     private class CopyResourceListenerImpl extends SelectionListener<MenuEvent> {
         @Override
         public void componentSelected(MenuEvent ce) {
             if (DataUtils.isCopyable(resources)) {
                 copyBuffer = resources;
                 itemPasteResource.enable();
             } else {
                 showErrorMsg();
             }
         }
 
     }
 
     private class PasteResourceListenerImpl extends SelectionListener<MenuEvent> {
         @Override
         public void componentSelected(MenuEvent ce) {
             // if (DataUtils.isWritablbe(currentPage)) {
             doCopy();
             // } else {
             // showErrorMsg();
             // }
         }
 
     }
 
     private class RestoreResourceListenerImpl extends SelectionListener<MenuEvent> {
         @Override
         public void componentSelected(MenuEvent ce) {
             if (DataUtils.isViewable(resources)) {
                 doRestore();
             }
         }
     }
 
     private void doRestore() {
         JSONObject obj = new JSONObject();
         JSONArray pathArr = new JSONArray();
         int i = 0;
         for (DiskResource r : resources) {
             pathArr.set(i++, new JSONString(r.getId()));
         }
         obj.put("paths", pathArr);
         DiskResourceServiceFacade facade = new DiskResourceServiceFacade();
         facade.restoreDiskResource(obj, new DiskResourceServiceCallback() {
 
             @Override
             public void onSuccess(String result) {
                 ManageDataRefreshEvent event = new ManageDataRefreshEvent(tag, currentPage, null);
                 EventBus.getInstance().fireEvent(event);
                 NotifyInfo.display(I18N.DISPLAY.restore(), I18N.DISPLAY.restoreMsg());
             }
 
             @Override
             protected String getErrorMessageDefault() {
                 return I18N.ERROR.restoreDefaultMsg();
             }
 
             @Override
             protected String getErrorMessageByCode(ErrorCode code, JSONObject jsonError) {
                 return getErrorMessage(code, parsePathsToNameList(jsonError));
             }
 
         });
 
     }
 
     private void doCopy() {
         JSONObject obj = new JSONObject();
         JSONArray fromArr = new JSONArray();
         int i = 0;
         maskingParent.mask("Copying...");
         for (DiskResource r : copyBuffer) {
             fromArr.set(i++, new JSONString(r.getId()));
         }
         obj.put("paths", fromArr);
         obj.put("destination", new JSONString(currentPage));
 
         DiskResourceServiceFacade facade = new DiskResourceServiceFacade();
         DiskResourceCopyCallback callback = new DiskResourceCopyCallback();
         callback.setMaskedCaller(maskingParent);
         facade.copyDiskResource(obj, new DiskResourceCopyCallback());
     }
 
     private class BulkDownloadListenerImpl extends SelectionListener<MenuEvent> {
         @Override
         public void componentSelected(MenuEvent ce) {
             if (DataUtils.isDownloadable(resources)) {
                 IDropLiteWindowDispatcher dispatcher = new IDropLiteWindowDispatcher();
                 dispatcher.launchDownloadWindow(resources);
             } else {
                 showErrorMsg();
             }
         }
     }
 
     private class MetadataListenerImpl extends SelectionListener<MenuEvent> {
 
         @Override
         public void componentSelected(MenuEvent ce) {
             DiskResource dr = resources.get(0);
             final MetadataEditorPanel mep = new DiskresourceMetadataEditorPanel(dr);
 
             MetadataEditorDialog d = new MetadataEditorDialog(
                     I18N.DISPLAY.metadata() + ":" + dr.getId(), mep); //$NON-NLS-1$
 
             d.setSize(500, 300);
             d.setResizable(false);
             d.show();
         }
     }
 
     private void showErrorMsg() {
         MessageBox.alert(I18N.DISPLAY.permissionErrorTitle(), I18N.DISPLAY.permissionErrorMessage(),
                 null);
     }
 
     private class DeleteListenerImpl extends SelectionListener<MenuEvent> {
         @Override
         public void componentSelected(MenuEvent ce) {
 
             if (currentPage != null && currentPage.startsWith(UserInfo.getInstance().getTrashPath())) {
                 Listener<MessageBoxEvent> callback = new Listener<MessageBoxEvent>() {
                     @Override
                     public void handleEvent(MessageBoxEvent ce) {
                         // did the user click yes?
                         if (ce.getButtonClicked().getItemId().equals(Dialog.YES)) {
                             doDelete(false);
                         }
                     }
                 };
 
                 MessageBox.confirm(I18N.DISPLAY.warning(), I18N.DISPLAY.emptyTrashWarning(), callback);
             } else {
                 doDelete(true);
             }
         }
 
         private void doDelete(boolean notify) {
             // first we need to fill our id lists
             List<String> idSrc = new ArrayList<String>();
 
             if (DataUtils.isDeletable(resources)) {
                 for (DiskResource resource : resources) {
                     idSrc.add(resource.getId());
                 }
 
                 // call the appropriate delete services
                 DiskResourceServiceFacade facade = new DiskResourceServiceFacade(maskingParent);
                 if (idSrc.size() > 0) {
                     facade.delete(JsonUtil.buildJsonArrayString(idSrc), new DiskResourceDeleteCallback(
                             idSrc, notify));
                 }
             } else {
                 showErrorMsg();
             }
         }
     }
 
     private class ViewListenerImpl extends SelectionListener<MenuEvent> {
         @Override
         public void componentSelected(MenuEvent ce) {
 
             if (DataUtils.isViewable(resources)) {
                 List<String> contexts = new ArrayList<String>();
 
                 DataContextBuilder builder = new DataContextBuilder();
 
                 for (DiskResource resource : resources) {
                     contexts.add(builder.build(resource.getId()));
                 }
 
                 DataViewContextExecutor executor = new DataViewContextExecutor();
                 executor.execute(contexts);
             } else {
                 showErrorMsg();
             }
         }
     }
 
     private class ViewTreeListenerImpl extends SelectionListener<MenuEvent> {
         @Override
         public void componentSelected(MenuEvent ce) {
             if (DataUtils.isViewable(resources)) {
                 DataContextBuilder builder = new DataContextBuilder();
 
                 TreeViewContextExecutor executor = new TreeViewContextExecutor();
 
                 for (DiskResource resource : resources) {
                     executor.execute(builder.build(resource.getId()));
                 }
             } else {
                 showErrorMsg();
             }
         }
     }
 
     private class SimpleDownloadListenerImpl extends SelectionListener<MenuEvent> {
         @Override
         public void componentSelected(MenuEvent ce) {
             if (DataUtils.isDownloadable(resources)) {
                 if (resources.size() == 1) {
                     downloadNow(resources.get(0).getId());
                 } else {
                     launchDownloadWindow();
                 }
             } else {
                 showErrorMsg();
             }
         }
 
         private void downloadNow(String path) {
             DiskResourceServiceFacade service = new DiskResourceServiceFacade();
             service.simpleDownload(path);
         }
 
         private void launchDownloadWindow() {
             List<String> paths = new ArrayList<String>();
 
             for (DiskResource resource : resources) {
                 if (resource instanceof File) {
                     paths.add(resource.getId());
                 }
             }
 
             SimpleDownloadWindowDispatcher dispatcher = new SimpleDownloadWindowDispatcher();
             dispatcher.launchDownloadWindow(paths);
         }
     }
 }
