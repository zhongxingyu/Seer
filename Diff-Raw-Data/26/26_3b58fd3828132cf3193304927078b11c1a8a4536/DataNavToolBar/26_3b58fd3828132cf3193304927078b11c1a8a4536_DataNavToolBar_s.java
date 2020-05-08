 package org.iplantc.de.client.views.panels;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.iplantc.core.jsonutil.JsonUtil;
 import org.iplantc.core.uicommons.client.models.UserInfo;
 import org.iplantc.core.uicommons.client.views.dialogs.IPlantDialog;
 import org.iplantc.core.uicommons.client.views.panels.IPlantDialogPanel;
 import org.iplantc.core.uidiskresource.client.models.Folder;
 import org.iplantc.de.client.I18N;
 import org.iplantc.de.client.events.AsyncUploadCompleteHandler;
 import org.iplantc.de.client.events.DefaultUploadCompleteHandler;
 import org.iplantc.de.client.events.UploadCompleteHandler;
 import org.iplantc.de.client.images.Resources;
 import org.iplantc.de.client.services.FolderDeleteCallback;
 import org.iplantc.de.client.services.FolderServiceFacade;
 import org.iplantc.de.client.utils.DataUtils;
 import org.iplantc.de.client.utils.PanelHelper;
 import org.iplantc.de.client.views.dialogs.IPlantSubmittableDialog;
 import org.iplantc.de.client.views.dialogs.URLImportDialog;
 
 import com.extjs.gxt.ui.client.Style.ButtonArrowAlign;
 import com.extjs.gxt.ui.client.core.FastMap;
 import com.extjs.gxt.ui.client.event.ButtonEvent;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.event.MenuEvent;
 import com.extjs.gxt.ui.client.event.MessageBoxEvent;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.util.Point;
 import com.extjs.gxt.ui.client.widget.Component;
 import com.extjs.gxt.ui.client.widget.Dialog;
 import com.extjs.gxt.ui.client.widget.MessageBox;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.extjs.gxt.ui.client.widget.menu.Menu;
 import com.extjs.gxt.ui.client.widget.menu.MenuItem;
 import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
 import com.extjs.gxt.ui.client.widget.treepanel.TreePanelSelectionModel;
 import com.google.gwt.user.client.ui.AbstractImagePrototype;
 
 /**
  * 
  * A toolbar for the data navigation panel.
  * 
  * @author sriram
  * 
  */
 public class DataNavToolBar extends ToolBar {
 
     @SuppressWarnings("unused")
     private final String tag;
     private String parentFolderId;
     private Dialog dlgUpload;
     private URLImportDialog dlgURLImport;
     private TreePanelSelectionModel<Folder> selectionModel;
     private Component maskingParent;
 
     /**
      * create a new instance of this tool bar
      * 
      * @param tag a tag for this widget
      */
     public DataNavToolBar(final String tag) {
         this.tag = tag;
         add(buildImportButton());
         add(buildActionsMenuButton());
     }
 
     /**
      * set parent folder id
      * 
      * @param id
      */
     public void setParentFolderId(String id) {
         this.parentFolderId = id;
     }
 
     /**
      * set selection model used by the tree panel
      * 
      * @param selectionModel
      */
     public void setSelectionModel(TreePanelSelectionModel<Folder> selectionModel) {
         this.selectionModel = selectionModel;
     }
 
     public void setMaskingParent(Component maskingParent) {
         this.maskingParent = maskingParent;
     }
 
     private Button buildImportButton() {
         Button ret = PanelHelper.buildButton("idDataImportBtn", I18N.DISPLAY.importLabel(), null); //$NON-NLS-1$
         // ret.setIcon(AbstractImagePrototype.create(org.iplantc.de.client.images.Resources.ICONS
         // .importData()));
         ret.setArrowAlign(ButtonArrowAlign.RIGHT);
         ret.setMenu(buildImportMenu());
 
         return ret;
     }
 
     private Menu buildImportMenu() {
         Menu menu = new Menu();
 
         menu.add(buildMenuItem("idDataBrowserUploadMenuItem", I18N.DISPLAY.desktop(), null, //$NON-NLS-1$
                 new SelectionListener<MenuEvent>() {
                     @Override
                     public void componentSelected(MenuEvent ce) {
                         promptUpload(ce.getXY());
                     }
                 }));
 
         // import from url menu item
         menu.add(buildMenuItem("idDataBrowserUrlImportMenuItem", I18N.DISPLAY.urlImport(), null, //$NON-NLS-1$
                 new SelectionListener<MenuEvent>() {
                     @Override
                     public void componentSelected(MenuEvent ce) {
                         promptUrlImport(ce.getXY());
                     }
                 }));
 
         // CORE-1974 - Phylota Import is currently broken and is being
         // pulled from 0.4 M1 until it can be fixed. (alenards)
         // import menu item
         // menu.add(buildImportMenuItem());
 
         return menu;
     }
 
     private void promptUpload(Point p) {
        if (canUpload(selectionModel.getSelectedItem())) {
             String idParentFolder = getCurrentPath();
             String username = UserInfo.getInstance().getUsername();
 
             // provide key/value pairs for hidden fields
             FastMap<String> hiddenFields = new FastMap<String>();
             hiddenFields.put(FileUploadDialogPanel.HDN_PARENT_ID_KEY, idParentFolder);
             hiddenFields.put(FileUploadDialogPanel.HDN_USER_ID_KEY, username);
 
             // define a handler for upload completion
             UploadCompleteHandler handler = new DefaultUploadCompleteHandler(idParentFolder) {
                 /** {@inheritDoc} */
                 @Override
                 public void onAfterCompletion() {
                     if (dlgUpload != null) {
                         dlgUpload.hide();
                     }
                 }
             };
 
             FileUploadDialogPanel pnlUpload = new FileUploadDialogPanel(hiddenFields, "servlet.gupld", //$NON-NLS-1$
                     handler);
 
             dlgUpload = new IPlantSubmittableDialog(I18N.DISPLAY.upload(), 375, pnlUpload);
             dlgUpload.setPagePosition(getPosition(false));
             dlgUpload.show();
         }
     }
 
     private void promptUrlImport(Point p) {
         String idParentFolder = getCurrentPath();
 
        if (canUpload(selectionModel.getSelectedItem())) {
             UploadCompleteHandler handler = new AsyncUploadCompleteHandler(idParentFolder) {
                 /** {@inheritDoc} */
                 @Override
                 public void onAfterCompletion() {
                     if (dlgURLImport != null) {
                         dlgURLImport.hide();
                     }
                 }
             };
 
             dlgURLImport = new URLImportDialog(idParentFolder, handler);
             dlgURLImport.setPagePosition(getPosition(false));
             dlgURLImport.setWidth(388);
             dlgURLImport.show();
         }
     }
 
     private boolean canUpload(Folder destination) {
         if (destination != null && DataUtils.canUploadToThisFolder(destination)) {
             return true;
         } else {
             showErrorMsg();
             return false;
         }
     }
 
     private MenuItem buildMenuItem(String id, String text, AbstractImagePrototype icon,
             SelectionListener<MenuEvent> listener) {
         MenuItem ret = new MenuItem(text, icon, listener);
         ret.setId(id);
 
         return ret;
     }
 
     private String getCurrentPath() {
         if (selectionModel == null) {
             return null;
         } else {
             return selectionModel.getSelectedItem().getId();
         }
     }
 
     /**
      * Builds a menu button with items for adding, renaming, and deleting folders.
      * 
      * @return The navigation panel's toolbar's Actions menu button.
      */
     private Button buildActionsMenuButton() {
         // build the menu and menu actions
         Menu actionMenu = new Menu();
 
         final MenuItem addFolder = buildAddFolderMenuItem();
         final MenuItem renameFolder = buildRenameFolder();
         final MenuItem deleteFolder = buildDeleteFolderMenuItem();
 
         actionMenu.add(addFolder);
         actionMenu.add(renameFolder);
         actionMenu.add(deleteFolder);
 
         // create the menu button
         Button ret = new Button(I18N.DISPLAY.moreActions());
 
         ret.setMenu(actionMenu);
 
         // Add a selection listener that will disable or enable actions based on the selected folder.
         ret.addSelectionListener(new MoreActionsListener(addFolder, renameFolder, deleteFolder));
 
         return ret;
     }
 
     private void showErrorMsg() {
         MessageBox.alert(I18N.DISPLAY.permissionErrorTitle(), I18N.DISPLAY.permissionErrorMessage(),
                 null);
     }
 
     private MenuItem buildDeleteFolderMenuItem() {
         MenuItem deleteFolder = new MenuItem();
         deleteFolder.setText(I18N.DISPLAY.delete());
         deleteFolder.setIcon(AbstractImagePrototype.create(Resources.ICONS.folderDelete()));
         deleteFolder.addSelectionListener(new SelectionListener<MenuEvent>() {
             @SuppressWarnings("unchecked")
             @Override
             public void componentSelected(MenuEvent ce) {
                 if (selectionModel == null) {
                     return;
                 }
 
                 @SuppressWarnings("rawtypes")
                 List resources = selectionModel.getSelectedItems();
                 Listener<MessageBoxEvent> callback = new Listener<MessageBoxEvent>() {
                     @Override
                     public void handleEvent(MessageBoxEvent ce) {
                         // did the user click yes?
                         if (ce.getButtonClicked().getItemId().equals(Dialog.YES)) {
                             List<String> idFolders = new ArrayList<String>();
                             for (Folder resource : selectionModel.getSelectedItems()) {
                                 idFolders.add(resource.getId());
                             }
 
                             if (idFolders.size() > 0) {
                                 FolderServiceFacade facade = new FolderServiceFacade(maskingParent);
                                 facade.deleteFolders(JsonUtil.buildJsonArrayString(idFolders),
                                         new FolderDeleteCallback(idFolders));
                             }
                         }
                     }
                 };
                 
                 if (!DataUtils.isDeletable(resources)) {
                     showErrorMsg();
                 } else {
                     MessageBox.confirm(I18N.DISPLAY.warning(), I18N.DISPLAY.folderDeleteWarning(),
                             callback);
                 }
             }
         });
         return deleteFolder;
     }
 
     private MenuItem buildRenameFolder() {
         MenuItem renameFolder = new MenuItem();
         renameFolder.setText(I18N.DISPLAY.rename());
         renameFolder.setIcon(AbstractImagePrototype.create(Resources.ICONS.folderRename()));
         renameFolder.addSelectionListener(new SelectionListener<MenuEvent>() {
             @Override
             public void componentSelected(MenuEvent ce) {
                 if (selectionModel == null) {
                     return;
                 }
 
                 Folder resource = selectionModel.getSelectedItem();
                 if (!DataUtils.isRenamable(resource)) {
                     showErrorMsg();
                 } else {
                     IPlantDialogPanel panel = new RenameFolderDialogPanel(resource.getId(), resource
                             .getName(), maskingParent);
                     IPlantDialog dlg = new IPlantDialog(I18N.DISPLAY.rename(), 340, panel);
 
                     dlg.show();
                 }
             }
         });
         return renameFolder;
     }
 
     private MenuItem buildAddFolderMenuItem() {
         MenuItem addFolder = new MenuItem();
         addFolder.setText(I18N.DISPLAY.newFolder());
         addFolder.setIcon(AbstractImagePrototype.create(Resources.ICONS.folderAdd()));
         addFolder.addSelectionListener(new SelectionListener<MenuEvent>() {
             @Override
             public void componentSelected(MenuEvent ce) {
                 if (selectionModel == null) {
                     return;
                 }
 
                 Folder dest = selectionModel.getSelectedItem();
 
                 if (!DataUtils.canCreateFolderInThisFolder(dest)) {
                     showErrorMsg();
 
                 } else {
                     IPlantDialog dlg = new IPlantDialog(I18N.DISPLAY.newFolder(), 340,
                             new AddFolderDialogPanel(dest.getId(), maskingParent));
                     dlg.disableOkButton();
                     dlg.show();
 
                 }
             }
         });
         return addFolder;
     }
 
     private final class MoreActionsListener extends SelectionListener<ButtonEvent> {
         private final MenuItem addFolder;
         private final MenuItem renameFolder;
         private final MenuItem deleteFolder;
 
         private MoreActionsListener(MenuItem addFolder, MenuItem renameFolder, MenuItem deleteFolder) {
             this.addFolder = addFolder;
             this.renameFolder = renameFolder;
             this.deleteFolder = deleteFolder;
         }
 
         @Override
         public void componentSelected(ButtonEvent ce) {
             // disable all actions by default
             boolean addMenuItemsEnabled = false;
             boolean editMenuItemsEnabled = false;
 
             if (selectionModel != null) {
                 if (parentFolderId != null && !parentFolderId.isEmpty()) {
                     // check the selected folder
                     Folder selectedFolder = selectionModel.getSelectedItem();
 
                     if (selectedFolder != null) {
                         // we can at least add folders to a selected path under the home folder.
                         addMenuItemsEnabled = true;
 
                         // disable editing items for the home folder
                         editMenuItemsEnabled = !parentFolderId.equals(selectedFolder.getId());
                     }
                 }
             }
 
             // enable or disable the correct actions
             addFolder.setEnabled(addMenuItemsEnabled);
             renameFolder.setEnabled(editMenuItemsEnabled);
             deleteFolder.setEnabled(editMenuItemsEnabled);
         }
 
     }
 }
