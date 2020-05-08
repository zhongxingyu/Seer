 package org.iplantc.de.client.views;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.iplantc.core.client.widgets.Hyperlink;
 import org.iplantc.core.jsonutil.JsonUtil;
 import org.iplantc.core.uicommons.client.events.EventBus;
 import org.iplantc.core.uicommons.client.util.CommonStoreSorter;
 import org.iplantc.core.uicommons.client.views.dialogs.IPlantDialog;
 import org.iplantc.core.uicommons.client.views.panels.IPlantDialogPanel;
 import org.iplantc.core.uidiskresource.client.models.DiskResource;
 import org.iplantc.core.uidiskresource.client.models.File;
 import org.iplantc.core.uidiskresource.client.models.Folder;
 import org.iplantc.de.client.Constants;
 import org.iplantc.de.client.I18N;
 import org.iplantc.de.client.dispatchers.IDropLiteWindowDispatcher;
 import org.iplantc.de.client.dispatchers.SimpleDownloadWindowDispatcher;
 import org.iplantc.de.client.events.disk.mgmt.DiskResourceSelectedEvent;
 import org.iplantc.de.client.events.disk.mgmt.DiskResourceSelectedEventHandler;
 import org.iplantc.de.client.images.Resources;
 import org.iplantc.de.client.models.ClientDataModel;
 import org.iplantc.de.client.services.FileDeleteCallback;
 import org.iplantc.de.client.services.FolderDeleteCallback;
 import org.iplantc.de.client.services.FolderServiceFacade;
 import org.iplantc.de.client.utils.DataUtils;
 import org.iplantc.de.client.utils.DataViewContextExecutor;
 import org.iplantc.de.client.utils.TreeViewContextExecutor;
 import org.iplantc.de.client.utils.builders.context.DataContextBuilder;
 import org.iplantc.de.client.views.dialogs.MetadataEditorDialog;
 import org.iplantc.de.client.views.dialogs.SharingDialog;
 import org.iplantc.de.client.views.panels.AddFolderDialogPanel;
 import org.iplantc.de.client.views.panels.DataPreviewPanel;
 import org.iplantc.de.client.views.panels.DiskresourceMetadataEditorPanel;
 import org.iplantc.de.client.views.panels.MetadataEditorPanel;
 import org.iplantc.de.client.views.panels.RenameFileDialogPanel;
 import org.iplantc.de.client.views.panels.RenameFolderDialogPanel;
 
 import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
 import com.extjs.gxt.ui.client.Style.SelectionMode;
 import com.extjs.gxt.ui.client.event.BaseEvent;
 import com.extjs.gxt.ui.client.event.Events;
 import com.extjs.gxt.ui.client.event.GridEvent;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.event.MenuEvent;
 import com.extjs.gxt.ui.client.event.MessageBoxEvent;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.store.ListStore;
 import com.extjs.gxt.ui.client.widget.Component;
 import com.extjs.gxt.ui.client.widget.Dialog;
 import com.extjs.gxt.ui.client.widget.LayoutContainer;
 import com.extjs.gxt.ui.client.widget.MessageBox;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.extjs.gxt.ui.client.widget.grid.CheckBoxSelectionModel;
 import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
 import com.extjs.gxt.ui.client.widget.grid.ColumnData;
 import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
 import com.extjs.gxt.ui.client.widget.grid.Grid;
 import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
 import com.extjs.gxt.ui.client.widget.grid.filters.GridFilters;
 import com.extjs.gxt.ui.client.widget.grid.filters.StringFilter;
 import com.extjs.gxt.ui.client.widget.menu.Menu;
 import com.extjs.gxt.ui.client.widget.menu.MenuItem;
 import com.extjs.gxt.ui.client.widget.tips.ToolTip;
 import com.extjs.gxt.ui.client.widget.tips.ToolTipConfig;
 import com.google.gwt.event.shared.HandlerRegistration;
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.resources.client.ImageResource;
 import com.google.gwt.user.client.Element;
 import com.google.gwt.user.client.ui.AbstractImagePrototype;
 
 /**
  * A grid that displays users files and folders. Provides floating menus with support for delete, rename,
  * view
  * 
  * @author sriram
  * 
  */
 public class MyDataGrid extends Grid<DiskResource> {
     public static final String COLUMN_ID_NAME = DiskResource.NAME;
     public static final String COLUMN_ID_DATE_MODIFIED = DiskResource.DATE_MODIFIED;
     public static final String COLUMN_ID_DATE_CREATED = DiskResource.DATE_CREATED;
     public static final String COLUMN_ID_SIZE = File.SIZE;
     public static final String COLUMN_ID_MENU = "menu"; //$NON-NLS-1$
 
     protected ClientDataModel controller;
     protected ArrayList<HandlerRegistration> handlers;
     protected static CheckBoxSelectionModel<DiskResource> sm;
     protected String callertag;
     protected String currentFolderId;
 
     private Menu menuActions;
     private MenuItem itemAddFolder;
     private MenuItem itemRenameResource;
     private MenuItem itemViewResource;
     private MenuItem itemViewTree;
     private MenuItem itemSimpleDownloadResource;
     private MenuItem itemBulkDownloadResource;
     private MenuItem itemDeleteResource;
     private MenuItem itemMetaData;
     private MenuItem itemShareResource;
 
     private final DataViewContextExecutor executor;
     private final Component maskingParent;
 
     /**
      * Create a new MyDataGrid
      * 
      * 
      * @param store store to be used by the grid
      * @param colModel column model describing the columns in the grid
      * @param currentFolderId id of the current folder to be displayed
      * @param executor data view context executor called when appropriate grid row is clicked
      */
     private MyDataGrid(ListStore<DiskResource> store, ColumnModel colModel, String currentFolderId,
             DataViewContextExecutor executor, Component maskingParent) {
         super(store, colModel);
 
         this.currentFolderId = currentFolderId;
         this.executor = executor;
         this.maskingParent = maskingParent;
 
         init();
         registerHandlers();
     }
 
     /**
      * Initialize the grid's properties
      */
     protected void init() {
         setBorders(true);
         setHeight(260);
 
         getView().setEmptyText(I18N.DISPLAY.selectFolderToViewContents());
         getView().setShowDirtyCells(false);
 
         menuActions = buildActionsMenu();
     }
 
     /**
      * Handle click event of grid row
      * 
      * @param dr disk resource on which the click was made
      * @param tag caller tag
      */
     protected void handleRowClick(final DiskResource dr, final String tag) {
         if (executor != null && dr instanceof File && this.callertag.equals(tag)) {
             DataContextBuilder builder = new DataContextBuilder();
 
             executor.execute(builder.build(dr.getId()));
         }
     }
 
     /**
      * Builds an action menu with items for adding, renaming, viewing, downloading, and deleting disk
      * resources.
      * 
      * @return The grid rows' actions menu
      */
     private Menu buildActionsMenu() {
         Menu actionMenu = new Menu();
 
         buildAddfolderMenuItem();
 
         buildRenameResourceMenuItem();
 
         buildViewResourceMenuItem();
 
         buildViewTreeMenuItem();
 
         buildSimpleDownloadMenuItem();
 
         buildBulDownloadMenuItem();
 
         buildDeleteResourceMenuItem();
 
         buildMetaDataMenuItem();
 
         buildShareResourceMenuItem();
 
         actionMenu.add(itemAddFolder);
         actionMenu.add(itemRenameResource);
         actionMenu.add(itemViewResource);
         actionMenu.add(itemViewTree);
         actionMenu.add(itemSimpleDownloadResource);
         actionMenu.add(itemBulkDownloadResource);
         actionMenu.add(itemDeleteResource);
         actionMenu.add(itemMetaData);
         actionMenu.add(itemShareResource);
 
         return actionMenu;
     }
 
     private void buildShareResourceMenuItem() {
         itemShareResource = new MenuItem();
         itemShareResource.setText(I18N.DISPLAY.share());
         itemShareResource.setIcon(AbstractImagePrototype.create(Resources.ICONS.share()));
         itemShareResource.addSelectionListener(new ShareListenerImpl());
     }
 
     private void buildMetaDataMenuItem() {
         itemMetaData = new MenuItem();
         itemMetaData.setText(I18N.DISPLAY.metadata());
         itemMetaData.setIcon(AbstractImagePrototype.create(Resources.ICONS.metadata()));
         itemMetaData.addSelectionListener(new MetadataListenerImpl());
     }
 
     private void buildDeleteResourceMenuItem() {
         itemDeleteResource = new MenuItem();
         itemDeleteResource.setText(I18N.DISPLAY.delete());
         itemDeleteResource.setIcon(AbstractImagePrototype.create(Resources.ICONS.folderDelete()));
         itemDeleteResource.addSelectionListener(new DeleteListenerImpl());
     }
 
     private void buildBulDownloadMenuItem() {
         itemBulkDownloadResource = new MenuItem();
         itemBulkDownloadResource.setText(I18N.DISPLAY.bulkDownload());
         itemBulkDownloadResource.setIcon(AbstractImagePrototype.create(Resources.ICONS.download()));
         itemBulkDownloadResource.addSelectionListener(new BulkDownloadListenerImpl());
     }
 
     private void buildSimpleDownloadMenuItem() {
         itemSimpleDownloadResource = new MenuItem();
         itemSimpleDownloadResource.setText(I18N.DISPLAY.simpleDownload());
         itemSimpleDownloadResource.setIcon(AbstractImagePrototype.create(Resources.ICONS.download()));
         itemSimpleDownloadResource.addSelectionListener(new SimpleDownloadListenerImpl());
     }
 
     private void buildViewTreeMenuItem() {
         itemViewTree = new MenuItem();
         itemViewTree.setText(I18N.DISPLAY.viewTreeViewer());
         itemViewTree.setIcon(AbstractImagePrototype.create(Resources.ICONS.fileView()));
         itemViewTree.addSelectionListener(new ViewTreeListenerImpl());
     }
 
     private void buildViewResourceMenuItem() {
         itemViewResource = new MenuItem();
         itemViewResource.setText(I18N.DISPLAY.view());
         itemViewResource.setIcon(AbstractImagePrototype.create(Resources.ICONS.fileView()));
         itemViewResource.addSelectionListener(new ViewListenerImpl());
     }
 
     private void buildRenameResourceMenuItem() {
         itemRenameResource = new MenuItem();
         itemRenameResource.setText(I18N.DISPLAY.rename());
         itemRenameResource.setIcon(AbstractImagePrototype.create(Resources.ICONS.folderRename()));
         itemRenameResource.addSelectionListener(new RenameListenerImpl());
     }
 
     private void buildAddfolderMenuItem() {
         itemAddFolder = new MenuItem();
         itemAddFolder.setText(I18N.DISPLAY.newFolder());
         itemAddFolder.setIcon(AbstractImagePrototype.create(Resources.ICONS.folderAdd()));
         itemAddFolder.addSelectionListener(new NewFolderListenerImpl());
     }
 
     /**
      * Show the Actions menu at the given absolute x, y position. The items displayed in the menu will
      * depend on the resources selected in the grid, according to DataUtils.getSupportedActions.
      * 
      * @param x
      * @param y
      * 
      * @see org.iplantc.de.client.utils.DataUtils
      */
     public void showMenu(int x, int y) {
         List<DiskResource> selected = getSelectionModel().getSelectedItems();
         List<DataUtils.Action> actions = DataUtils.getSupportedActions(selected);
 
         if (menuActions != null && actions.size() > 0) {
             for (Component item : menuActions.getItems()) {
                 item.disable();
                 item.hide();
             }
 
             boolean folderActionsEnabled = DataUtils.hasFolders(selected);
 
             if (folderActionsEnabled && selected.size() == 1) {
                 // Enable the "Add Folder" item as well.
                 itemAddFolder.enable();
                 itemAddFolder.show();
             }
 
             for (DataUtils.Action action : actions) {
                 switch (action) {
                     case RenameFolder:
                         itemRenameResource.setIcon(AbstractImagePrototype.create(Resources.ICONS
                                 .folderRename()));
                         itemRenameResource.enable();
                         itemRenameResource.show();
                         break;
 
                     case RenameFile:
                         itemRenameResource.setIcon(AbstractImagePrototype.create(Resources.ICONS
                                 .fileRename()));
                         itemRenameResource.enable();
                         itemRenameResource.show();
                         break;
 
                     case View:
                         itemViewResource.enable();
                         itemViewResource.show();
                         break;
 
                     case ViewTree:
                         itemViewTree.enable();
                         itemViewTree.show();
                         break;
 
                     case SimpleDownload:
                         itemSimpleDownloadResource.enable();
                         itemSimpleDownloadResource.show();
                         break;
 
                     case BulkDownload:
                         itemBulkDownloadResource.enable();
                         itemBulkDownloadResource.show();
                         break;
 
                     case Delete:
                         ImageResource delIcon = folderActionsEnabled ? Resources.ICONS.folderDelete()
                                 : Resources.ICONS.fileDelete();
 
                         itemDeleteResource.setIcon(AbstractImagePrototype.create(delIcon));
                         itemDeleteResource.enable();
                         itemDeleteResource.show();
                         break;
                     case Metadata:
                         itemMetaData.enable();
                         itemMetaData.show();
                         break;
                     case Share:
                         itemShareResource.enable();
                        itemShareResource.show();
                         break;
                 }
             }
 
             menuActions.showAt(x, y);
         }
     }
 
     /**
      * Builds the CheckBoxSelectionModel used in the ColumnDisplay.ALL ColumnModel.
      */
     protected static void buildCheckBoxSelectionModel() {
         if (sm != null) {
             return;
         }
 
         sm = new CheckBoxSelectionModel<DiskResource>() {
             @Override
             protected void handleMouseClick(GridEvent<DiskResource> event) {
                 super.handleMouseClick(event);
 
                 MyDataGrid grid = (MyDataGrid)event.getGrid();
 
                 // Show the actions menu if the menu grid column was clicked.
                 String colId = grid.getColumnModel().getColumnId(event.getColIndex());
                 if (colId != null && colId.equals(COLUMN_ID_MENU)) {
                     // if the row clicked is not selected, then select it
                     DiskResource resource = listStore.getAt(event.getRowIndex());
                     if (resource != null && !isSelected(resource)) {
                         select(resource, true);
                     }
 
                     // show the menu just under the row and column clicked.
                     Element target = event.getTarget();
                     grid.showMenu(target.getAbsoluteRight() - 10, target.getAbsoluteBottom());
                 }
             }
 
             @Override
             protected void handleMouseDown(GridEvent<DiskResource> event) {
                 // Only select the row if the checkbox is explicitly selected.
                 // Assume the checkbox is set as the first column of the row.
                 if (event.getColIndex() == 0) {
                     super.handleMouseDown(event);
                 }
             }
         };
 
         sm.getColumn().setAlignment(HorizontalAlignment.CENTER);
     }
 
     /**
      * Create the column model for the tree grid.
      * 
      * @return an instance of ColumnModel representing the columns visible in a grid
      */
     protected static ColumnModel buildColumnModel(String tag) {
         // build column configs and add them to a list for the ColumnModel.
         List<ColumnConfig> columns = new ArrayList<ColumnConfig>();
 
         ColumnConfig name = new ColumnConfig(COLUMN_ID_NAME, I18N.DISPLAY.name(), 235);
         name.setRenderer(new NameCellRenderer(tag));
         name.setMenuDisabled(true);
 
         ColumnConfig date = new ColumnConfig(COLUMN_ID_DATE_MODIFIED, I18N.DISPLAY.lastModified(), 150);
         date.setDateTimeFormat(DateTimeFormat
                 .getFormat(DateTimeFormat.PredefinedFormat.DATE_TIME_MEDIUM));
         date.setMenuDisabled(true);
 
         if (tag.equals(Constants.CLIENT.myDataTag())) {
             // add the checkbox as the first column of the row
             buildCheckBoxSelectionModel();
             columns.add(sm.getColumn());
         }
 
         ColumnConfig created = new ColumnConfig(COLUMN_ID_DATE_CREATED, I18N.DISPLAY.createdDate(), 150);
         created.setDateTimeFormat(DateTimeFormat
                 .getFormat(DateTimeFormat.PredefinedFormat.DATE_TIME_MEDIUM));
         created.setHidden(true);
         created.setMenuDisabled(true);
 
         ColumnConfig size = new ColumnConfig(COLUMN_ID_SIZE, I18N.DISPLAY.size(), 100);
         size.setRenderer(new SizeCellRenderer());
         size.setHidden(true);
         size.setMenuDisabled(true);
 
         ColumnConfig menu = new ColumnConfig(COLUMN_ID_MENU, "", 25); //$NON-NLS-1$
         menu.setSortable(false);
         menu.setMenuDisabled(true);
 
         columns.addAll(Arrays.asList(name, date, created, size, menu));
 
         return new ColumnModel(columns);
     }
 
     @SuppressWarnings("unchecked")
     private static MyDataGrid createInstanceImpl(String currentFolderId, String tag,
             ClientDataModel controller, Component maskingParent) {
         final ListStore<DiskResource> store = new ListStore<DiskResource>();
         final ColumnModel colModel = buildColumnModel(tag);
 
         boolean isMyDataWindow = tag.equals(Constants.CLIENT.myDataTag());
 
         MyDataGrid ret;
 
         DataViewContextExecutor executor = null;
         if (isMyDataWindow) {
             executor = new DataViewContextExecutor();
         }
 
         store.setStoreSorter(new CommonStoreSorter());
         ret = new MyDataGrid(store, colModel, currentFolderId, executor, maskingParent);
 
         if (isMyDataWindow) {
             ret.setSelectionModel(sm);
             ret.addPlugin(sm);
             ret.addStyleName("menu-row-over"); //$NON-NLS-1$
         }
 
         ret.getSelectionModel().setSelectionMode(SelectionMode.SIMPLE);
         ret.getView().setForceFit(false);
         ret.setAutoExpandMax(2048);
 
         ret.callertag = tag;
         ret.controller = controller;
 
         GridFilters filters = new GridFilters();
         filters.setLocal(true);
         StringFilter nameFilter = new StringFilter("name"); //$NON-NLS-1$
         filters.addFilter(nameFilter);
         ret.addPlugin(filters);
 
         return ret;
     }
 
     /**
      * Allocate default instance.
      * 
      * @return newly allocated my data grid.
      */
     public static MyDataGrid createInstance(String currentFolderId, String tag,
             ClientDataModel controller, Component maskingParent) {
         return createInstanceImpl(currentFolderId, tag, controller, maskingParent);
     }
 
     /**
      * get root folder id
      * 
      * @return id of the root folder t
      */
     public String getRootFolderId() {
         return controller.getRootFolderId();
     }
 
     /**
      * Free any unneeded resources.
      */
     public void cleanup() {
         removeEventHandlers();
         removeAllListeners();
     }
 
     private void removeEventHandlers() {
         EventBus eventbus = EventBus.getInstance();
 
         // unregister
         for (HandlerRegistration reg : handlers) {
             eventbus.removeHandler(reg);
         }
 
         // clear our list
         handlers.clear();
     }
 
     /**
      * register event handlers
      * 
      */
     protected void registerHandlers() {
         handlers = new ArrayList<HandlerRegistration>();
         EventBus eventbus = EventBus.getInstance();
 
         // disk resource selected
         handlers.add(eventbus.addHandler(DiskResourceSelectedEvent.TYPE,
                 new DiskResourceSelectedEventHandler() {
                     @Override
                     public void onSelected(DiskResourceSelectedEvent event) {
                         handleRowClick(event.getResource(), event.getTag());
                     }
                 }));
 
     }
 
     private void showErrorMsg() {
         MessageBox.alert(I18N.DISPLAY.permissionErrorTitle(), I18N.DISPLAY.permissionErrorMessage(),
                 null);
     }
 
     private class NewFolderListenerImpl extends SelectionListener<MenuEvent> {
         @Override
         public void componentSelected(MenuEvent ce) {
             for (DiskResource resource : getSelectionModel().getSelectedItems()) {
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
             for (DiskResource resource : getSelectionModel().getSelectedItems()) {
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
             }
         }
     }
 
     private class ViewListenerImpl extends SelectionListener<MenuEvent> {
         @Override
         public void componentSelected(MenuEvent ce) {
             List<DiskResource> resources = getSelectionModel().getSelectedItems();
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
 
     private class ShareListenerImpl extends SelectionListener<MenuEvent> {
 
         @Override
         public void componentSelected(MenuEvent ce) {
             List<DiskResource> resources = getSelectionModel().getSelectedItems();
             if (DataUtils.isSharable(resources)) {
                 showSharingDialog(resources);
             } else {
                 showErrorMsg();
             }
 
         }
 
     }
 
     private void showSharingDialog(List<DiskResource> resources) {
         SharingDialog sd = new SharingDialog(resources);
         sd.show();
     }
 
     private class ViewTreeListenerImpl extends SelectionListener<MenuEvent> {
         @Override
         public void componentSelected(MenuEvent ce) {
             List<DiskResource> resources = getSelectionModel().getSelectedItems();
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
 
     private class MetadataListenerImpl extends SelectionListener<MenuEvent> {
         @Override
         public void componentSelected(MenuEvent ce) {
             DiskResource dr = getSelectionModel().getSelectedItems().get(0);
             final MetadataEditorPanel mep = new DiskresourceMetadataEditorPanel(dr);
 
             MetadataEditorDialog d = new MetadataEditorDialog(
                     I18N.DISPLAY.metadata() + ":" + dr.getId(), mep); //$NON-NLS-1$
 
             d.setSize(500, 300);
             d.setResizable(false);
             d.show();
         }
     }
 
     private class SimpleDownloadListenerImpl extends SelectionListener<MenuEvent> {
         @Override
         public void componentSelected(MenuEvent ce) {
 
             List<DiskResource> resources = getSelectionModel().getSelectedItems();
 
             if (DataUtils.isDownloadable(resources)) {
                 if (resources.size() == 1) {
                     downloadNow(resources.get(0).getId());
                 } else {
                     launchDownloadWindow(resources);
                 }
             } else {
                 showErrorMsg();
             }
         }
 
         private void downloadNow(String path) {
             FolderServiceFacade service = new FolderServiceFacade();
             service.simpleDownload(path);
         }
 
         private void launchDownloadWindow(List<DiskResource> resources) {
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
 
     private class BulkDownloadListenerImpl extends SelectionListener<MenuEvent> {
         @Override
         public void componentSelected(MenuEvent ce) {
 
             List<DiskResource> resources = getSelectionModel().getSelectedItems();
 
             if (DataUtils.isDownloadable(resources)) {
                 IDropLiteWindowDispatcher dispatcher = new IDropLiteWindowDispatcher();
                 dispatcher.launchDownloadWindow(resources);
             } else {
                 showErrorMsg();
             }
         }
     }
 
     private class DeleteListenerImpl extends SelectionListener<MenuEvent> {
         @Override
         public void componentSelected(MenuEvent ce) {
             final Listener<MessageBoxEvent> callback = new Listener<MessageBoxEvent>() {
                 @Override
                 public void handleEvent(MessageBoxEvent ce) {
                     Button btn = ce.getButtonClicked();
 
                     // did the user click yes?
                     if (btn.getItemId().equals(Dialog.YES)) {
                         confirmDelete();
                     }
                 }
             };
 
             // if folders are selected, display a "folder delete" confirmation
             if (DataUtils.hasFolders(getSelectionModel().getSelectedItems())) {
                 MessageBox.confirm(I18N.DISPLAY.warning(), I18N.DISPLAY.folderDeleteWarning(), callback);
             } else {
                 confirmDelete();
             }
         }
 
         private void confirmDelete() {
             final Listener<MessageBoxEvent> callback = new Listener<MessageBoxEvent>() {
                 @Override
                 public void handleEvent(MessageBoxEvent ce) {
                     Button btn = ce.getButtonClicked();
                     if (btn.getItemId().equals(Dialog.YES)) {
                         doDelete();
                     }
                 }
             };
 
             MessageBox.confirm(I18N.DISPLAY.deleteFilesTitle(), I18N.DISPLAY.deleteFilesMsg(), callback);
         }
 
         private void doDelete() {
             // first we need to fill our id lists
             List<String> idFolders = new ArrayList<String>();
             List<String> idFiles = new ArrayList<String>();
 
             List<DiskResource> resources = getSelectionModel().getSelectedItems();
 
             if (DataUtils.isDeletable(resources)) {
 
                 for (DiskResource resource : getSelectionModel().getSelectedItems()) {
                     if (resource instanceof Folder) {
                         idFolders.add(resource.getId());
                     } else if (resource instanceof File) {
                         idFiles.add(resource.getId());
                     }
                 }
 
                 // call the appropriate delete services
                 FolderServiceFacade facade = new FolderServiceFacade(maskingParent);
 
                 if (idFiles.size() > 0) {
                     facade.deleteFiles(JsonUtil.buildJsonArrayString(idFiles), new FileDeleteCallback(
                             idFiles));
                 }
 
                 if (idFolders.size() > 0) {
                     facade.deleteFolders(JsonUtil.buildJsonArrayString(idFolders),
                             new FolderDeleteCallback(idFolders));
                 }
             } else {
                 showErrorMsg();
             }
         }
     }
 }
 
 /**
  * A custom renderer that renders folder / file names as hyperlink
  * 
  * @author sriram
  * 
  */
 class NameCellRenderer implements GridCellRenderer<DiskResource> {
     private final String callertag;
 
     NameCellRenderer(String caller) {
         callertag = caller;
     }
 
     @Override
     public Object render(final DiskResource model, String property, ColumnData config, int rowIndex,
             int colIndex, ListStore<DiskResource> store, final Grid<DiskResource> grid) {
         Hyperlink link = null;
 
         if (model instanceof Folder) {
             link = new Hyperlink("<img src='./gxt/images/default/tree/folder.gif'/>&nbsp;" //$NON-NLS-1$
                     + model.getName(), "mydata_name"); //$NON-NLS-1$
         } else {
             link = new Hyperlink("<img src='./images/file.gif'/>&nbsp;" + model.getName(), "mydata_name"); //$NON-NLS-1$ //$NON-NLS-2$
             addPreviewToolTip(link, model);
         }
 
         link.addListener(Events.OnClick, new Listener<BaseEvent>() {
 
             @Override
             public void handleEvent(BaseEvent be) {
                 DiskResourceSelectedEvent e = new DiskResourceSelectedEvent(model, callertag);
                 EventBus.getInstance().fireEvent(e);
             }
         });
 
         link.setWidth(model.getName().length());
 
         return link;
     }
 
     private void addPreviewToolTip(Component target, final DiskResource resource) {
         ToolTipConfig ttConfig = new ToolTipConfig();
         ttConfig.setShowDelay(1000);
         ttConfig.setDismissDelay(0); // never hide tool tip while mouse is still over it
         ttConfig.setAnchorToTarget(true);
         ttConfig.setTitle(I18N.DISPLAY.preview() + ": " + resource.getName()); //$NON-NLS-1$
 
         final LayoutContainer pnl = new LayoutContainer();
         final DataPreviewPanel previewPanel = new DataPreviewPanel();
         pnl.add(previewPanel);
         ToolTip tip = new ToolTip(target, ttConfig) {
             // overridden to populate the preview
             @Override
             protected void updateContent() {
                 getHeader().setText(title);
                 if (resource != null) {
                     previewPanel.update(resource);
                 }
             }
         };
         tip.setWidth(312);
         tip.add(pnl);
     }
 }
 
 class SizeCellRenderer implements GridCellRenderer<DiskResource> {
 
     @Override
     public Object render(DiskResource model, String property, ColumnData config, int rowIndex,
             int colIndex, ListStore<DiskResource> store, Grid<DiskResource> grid) {
         if (model instanceof Folder) {
             return null;
         } else {
             File f = (File)model;
             return DataUtils.getSizeForDisplay(f.getSize());
         }
     }
 
 }
