 package org.iplantc.de.client.views.panels;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.iplantc.core.uicommons.client.models.UserInfo;
 import org.iplantc.core.uidiskresource.client.models.DiskResource;
 import org.iplantc.core.uidiskresource.client.models.Permissions;
 import org.iplantc.de.client.I18N;
 import org.iplantc.de.client.images.Resources;
 import org.iplantc.de.client.models.Collaborator;
 import org.iplantc.de.client.models.DataSharing;
 import org.iplantc.de.client.models.Sharing;
 import org.iplantc.de.client.views.dialogs.SelectCollaboratorsDialog;
 
 import com.extjs.gxt.ui.client.Style.SelectionMode;
 import com.extjs.gxt.ui.client.core.FastMap;
 import com.extjs.gxt.ui.client.data.ModelData;
 import com.extjs.gxt.ui.client.data.ModelKeyProvider;
 import com.extjs.gxt.ui.client.event.BaseEvent;
 import com.extjs.gxt.ui.client.event.ButtonEvent;
 import com.extjs.gxt.ui.client.event.Events;
 import com.extjs.gxt.ui.client.event.FieldEvent;
 import com.extjs.gxt.ui.client.event.GridEvent;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.store.ListStore;
 import com.extjs.gxt.ui.client.widget.ContentPanel;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
 import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
 import com.extjs.gxt.ui.client.widget.form.SimpleComboValue;
 import com.extjs.gxt.ui.client.widget.grid.CellEditor;
 import com.extjs.gxt.ui.client.widget.grid.CheckBoxSelectionModel;
 import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
 import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
 import com.extjs.gxt.ui.client.widget.grid.EditorGrid;
 import com.extjs.gxt.ui.client.widget.grid.EditorGrid.ClicksToEdit;
 import com.extjs.gxt.ui.client.widget.layout.FitLayout;
 import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
 import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
 import com.google.gwt.user.client.ui.AbstractImagePrototype;
 
 /**
  * A panel to display a list of sharee and their permissions
  * 
  * @author sriram
  * 
  */
 public class SharePanel extends ContentPanel {
     private static final String ID_PERM_GROUP = "idPermGroup"; //$NON-NLS-1$
     private EditorGrid<DataSharing> grid;
     private final FastMap<List<DataSharing>> unshareList;
     private final FastMap<DiskResource> resources;
     private ToolBar toolbar;
     private static final String ID_BTN_ADD_COLLABS = "idBtnAddCollabs"; //$NON-NLS-1$
     private static final String ID_BTN_REMOVE = "idBtnRemove"; //$NON-NLS-1$
     private FastMap<List<DataSharing>> sharingMap;
     private FastMap<List<DataSharing>> originalList;
 
     public SharePanel(List<DiskResource> resourceList) {
         unshareList = new FastMap<List<DataSharing>>();
         resources = new FastMap<DiskResource>();
 
         for (DiskResource data : resourceList) {
             resources.put(data.getId(), data);
         }
 
         init();
     }
 
     private void init() {
         setLayout(new FitLayout());
 
         initGrid();
 
         add(grid);
         addToolBar();
     }
 
     private void initGrid() {
         ListStore<DataSharing> store = new ListStore<DataSharing>();
         store.setKeyProvider(new ModelKeyProvider<DataSharing>() {
             @Override
             public String getKey(DataSharing model) {
                 return model.getKey();
             }
         });
 
         CheckBoxSelectionModel<DataSharing> sm = new CheckBoxSelectionModel<DataSharing>();
         sm.setSelectionMode(SelectionMode.MULTI);
 
         grid = new EditorGrid<DataSharing>(store, buildColumnModel(sm.getColumn()));
 
         grid.getView().setEmptyText(I18N.DISPLAY.sharePanelEmptyText());
         grid.setClicksToEdit(ClicksToEdit.ONE);
 
         grid.setSelectionModel(sm);
         grid.addPlugin(sm);
 
         sm.addListener(Events.SelectionChange, new Listener<BaseEvent>() {
             @Override
             public void handleEvent(BaseEvent be) {
                 if (grid.getSelectionModel().getSelectedItems().size() > 0) {
                     toolbar.getItemByItemId(ID_BTN_REMOVE).enable();
                     toolbar.getItemByItemId(ID_PERM_GROUP).enable();
                 } else {
                     toolbar.getItemByItemId(ID_BTN_REMOVE).disable();
                     toolbar.getItemByItemId(ID_PERM_GROUP).disable();
                 }
 
             }
         });
 
         grid.addListener(Events.AfterEdit, new Listener<GridEvent<Sharing>>() {
             @Override
             public void handleEvent(GridEvent<Sharing> be) {
                 // edited row can only be of instance DataSharing
                 Object value = be.getValue();
                 if (value != null) {
                     DataSharing ds = (DataSharing)be.getRecord().getModel();
                     updatePermissions(value.toString(), ds.getUserName());
                 }
             }
         });
     }
 
     private void addToolBar() {
         toolbar = new ToolBar();
 
         toolbar.add(buildAddCollabsButton());
         Button removeBtn = buildUnshareButton();
         toolbar.add(removeBtn);
 
         toolbar.add(new FillToolItem());
 
         SimpleComboBox<String> permissionsCombo = buildPermissionsCombo();
         permissionsCombo.setEmptyText(I18N.DISPLAY.permissions());
         permissionsCombo.disable();
         permissionsCombo.addListener(Events.Select, new PermissionsChangeListenerImpl());
         toolbar.add(permissionsCombo);
 
         setTopComponent(toolbar);
     }
 
     private Button buildAddCollabsButton() {
         Button addCollabsBtn = new Button(I18N.DISPLAY.selectCollabs(),
                 AbstractImagePrototype.create(Resources.ICONS.viewCurrentCollabs()));
         addCollabsBtn.setId(ID_BTN_ADD_COLLABS);
         addCollabsBtn.addSelectionListener(new AddCollaboratorsListener());
 
         return addCollabsBtn;
     }
 
     private class AddCollaboratorsListener extends SelectionListener<ButtonEvent> {
         @Override
         public void componentSelected(ButtonEvent ce) {
             final SelectCollaboratorsDialog sd = new SelectCollaboratorsDialog();
             sd.getDoneButton().setText(I18N.DISPLAY.add());
             sd.getDoneButton().addSelectionListener(new SelectionListener<ButtonEvent>() {
 
                 @Override
                 public void componentSelected(ButtonEvent ce) {
                     List<Collaborator> selectedCollaborators = sd.getSelectedCollaborators();
                     if (selectedCollaborators != null) {
                         for (Collaborator user : selectedCollaborators) {
                             addCollaborator(user);
                         }
                     }
                 }
             });
             sd.showCurrentCollborators();
             sd.show();
         }
 
     }
 
     private void addCollaborator(Collaborator user) {
         String userName = user.getUserName();
         if (sharingMap.get(userName) == null) {
             List<DataSharing> shareList = new ArrayList<DataSharing>();
             DataSharing displayShare = null;
 
             for (String path : resources.keySet()) {
                 DataSharing share = new DataSharing(user, new Permissions(true, false, false), path);
                 shareList.add(share);
 
                 if (displayShare == null) {
                    displayShare = share;
                     grid.getStore().add(displayShare);
                 }
             }
 
             sharingMap.put(userName, shareList);
         }
     }
 
     private Button buildUnshareButton() {
         Button removeBtn = new Button(I18N.DISPLAY.unshare(),
                 AbstractImagePrototype.create(Resources.ICONS.deleteIcon()));
         removeBtn.setId(ID_BTN_REMOVE);
         removeBtn.addSelectionListener(new RemoveButtonSelectionListener());
         removeBtn.disable();
         return removeBtn;
     }
 
     public void loadSharingData(FastMap<List<DataSharing>> sharingMap) {
         this.sharingMap = sharingMap;
         originalList = new FastMap<List<DataSharing>>();
 
         ListStore<DataSharing> store = grid.getStore();
         store.removeAll();
 
         for (String userName : sharingMap.keySet()) {
             List<DataSharing> list = sharingMap.get(userName);
             List<DataSharing> newList = new ArrayList<DataSharing>();
             if (list != null && list.size() > 0) {
                DataSharing displayShare = list.get(0);
                 String displayPermission = displayShare.getDisplayPermission();
 
                 for (DataSharing share : list) {
                     DataSharing copyShare = share.copy();
                     newList.add(copyShare);
 
                     if (!displayPermission.equals(copyShare.getDisplayPermission())) {
                         displayPermission = I18N.DISPLAY.varies();
                     }
                 }
                 originalList.put(userName, newList);
 
                 if (resources.keySet().size() != newList.size()) {
                     displayPermission = I18N.DISPLAY.varies();
                 }
 
                 displayShare.setDisplayPermission(displayPermission);
                 store.add(displayShare);
             }
         }
     }
 
     private ColumnModel buildColumnModel(ColumnConfig checkCol) {
         ColumnConfig sharee = new ColumnConfig(DataSharing.USER, I18N.DISPLAY.name(), 170);
         sharee.setMenuDisabled(true);
 
         ColumnConfig permissions = new ColumnConfig(DataSharing.DISPLAY_PERMISSION,
                 I18N.DISPLAY.permissions(), 100);
         permissions.setEditor(buildPermissionsEditor());
         permissions.setMenuDisabled(true);
 
         return new ColumnModel(Arrays.asList(checkCol, sharee, permissions));
     }
 
     private CellEditor buildPermissionsEditor() {
         final SimpleComboBox<String> combo = buildPermissionsCombo();
 
         CellEditor editor = new CellEditor(combo) {
             @Override
             public Object preProcessValue(Object value) {
                 if (value == null) {
                     return value;
                 }
                 return combo.findModel(value.toString());
             }
 
             @Override
             public Object postProcessValue(Object value) {
                 if (value == null) {
                     return value;
                 }
                 return ((ModelData)value).get("value"); //$NON-NLS-1$
             }
         };
 
         return editor;
 
     }
 
     private SimpleComboBox<String> buildPermissionsCombo() {
         final SimpleComboBox<String> combo = new SimpleComboBox<String>();
         combo.setId(ID_PERM_GROUP);
         combo.setForceSelection(true);
         combo.setAllowBlank(false);
         combo.add(I18N.DISPLAY.read());
         combo.add(I18N.DISPLAY.write());
         combo.add(I18N.DISPLAY.own());
         combo.setEditable(false);
 
         combo.setTriggerAction(TriggerAction.ALL);
         return combo;
     }
 
     /**
      * 
      * 
      * @return the sharing list
      */
     public FastMap<List<DataSharing>> getSharingMap() {
         FastMap<List<DataSharing>> sharingList = new FastMap<List<DataSharing>>();
         for (DataSharing share : grid.getStore().getModels()) {
             String userName = share.getUserName();
             List<DataSharing> dataShares = sharingMap.get(userName);
             List<DataSharing> updatedSharingList = getUpdatedSharingList(userName, dataShares);
             if (updatedSharingList != null && updatedSharingList.size() > 0) {
                 sharingList.put(userName, updatedSharingList);
             }
         }
 
         return sharingList;
     }
 
     /**
      * check the list with original to see if things have changed. ignore unchanged records
      * 
      * @param userName
      * @param list
      * @return
      */
     private List<DataSharing> getUpdatedSharingList(String userName, List<DataSharing> list) {
         List<DataSharing> updateList = new ArrayList<DataSharing>();
         if (list != null && userName != null) {
             List<DataSharing> fromOriginal = originalList.get(userName);
 
             if (fromOriginal == null || fromOriginal.isEmpty()) {
                 updateList = list;
             } else {
                 for (DataSharing s : list) {
                     if (!fromOriginal.contains(s)) {
                         updateList.add(s);
                     }
                 }
             }
         }
 
         return updateList;
     }
 
     /**
      * check if a sharing recored originally existed. Needed to remove false submission of unshare list
      * 
      * @return
      */
     private boolean isExistedOriginally(DataSharing s) {
         String userName = s.getUserName();
         List<DataSharing> fromOriginal = originalList.get(userName);
         if (fromOriginal != null && fromOriginal.contains(s)) {
             return true;
         } else {
             return false;
         }
 
     }
 
     /**
      * @return the unshareList
      */
     public FastMap<List<DataSharing>> getUnshareList() {
         return unshareList;
     }
 
     private final class PermissionsChangeListenerImpl implements Listener<FieldEvent> {
         @Override
         public void handleEvent(FieldEvent be) {
             List<DataSharing> selected = grid.getSelectionModel().getSelectedItems();
             if (selected != null) {
                 @SuppressWarnings("unchecked")
                 SimpleComboBox<String> perm = (SimpleComboBox<String>)be.getField();
                 SimpleComboValue<String> value = perm.getValue();
 
                 if (value != null) {
                     for (Sharing sharing : selected) {
                         updatePermissions(value.getValue(), sharing.getUserName());
                     }
                 }
             }
 
         }
     }
 
     private void updatePermissions(String perm, String username) {
         List<DataSharing> models = sharingMap.get(username);
         if (models != null) {
            DataSharing displayShare = models.get(0);

             boolean own = perm.equals(I18N.DISPLAY.own());
             boolean write = own || perm.equals(I18N.DISPLAY.write());
             boolean read = true;
 
             for (DataSharing share : models) {
                 if (own) {
                     share.setOwner(true);
                 } else if (write) {
                     share.setWritable(true);
                 } else {
                     share.setReadable(true);
                 }
             }
 
            grid.getStore().update(displayShare);

             if (resources.size() != models.size()) {
                 Permissions perms = new Permissions(read, write, own);
 
                 for (String path : resources.keySet()) {
                     boolean shared = false;
                     for (DataSharing existingShare : models) {
                         if (path.equals(existingShare.getPath())) {
                             shared = true;
                             break;
                         }
                     }
 
                     if (!shared) {
                        models.add(new DataSharing(displayShare.getCollaborator(), perms, path));
                     }
                 }
             }
         }
     }
 
     private class RemoveButtonSelectionListener extends SelectionListener<ButtonEvent> {
 
         @Override
         public void componentSelected(ButtonEvent ce) {
             List<DataSharing> models = grid.getSelectionModel().getSelectedItems();
             removeModels(models);
         }
     }
 
     private void removeModels(List<DataSharing> models) {
         // prepared unshared list here
         ListStore<DataSharing> store = grid.getStore();
         for (DataSharing model : models) {
             String userName = model.getUserName();
             List<DataSharing> list = unshareList.get(userName);
             if (list == null) {
                 list = new ArrayList<DataSharing>();
             }
 
             DataSharing sharing = store.findModel(model);
             if (sharing != null) {
                 List<DataSharing> removeList = sharingMap.get(userName);
                 for (DataSharing remItem : removeList) {
                     if (isExistedOriginally(remItem)) {
                         list.add(remItem);
                     }
                 }
 
                 store.remove(sharing);
             }
 
             if (!list.isEmpty()) {
                 unshareList.put(userName, list);
             }
         }
     }
 
     private boolean canShare(Collaborator c, String path) {
         ListStore<DataSharing> store = grid.getStore();
         DataSharing s = new DataSharing(c, new Permissions(true, false, false), path);
         if (!store.contains(s)
                 && (!c.getUserName().equalsIgnoreCase(UserInfo.getInstance().getUsername()))) {
             return true;
         }
 
         return false;
     }
 }
