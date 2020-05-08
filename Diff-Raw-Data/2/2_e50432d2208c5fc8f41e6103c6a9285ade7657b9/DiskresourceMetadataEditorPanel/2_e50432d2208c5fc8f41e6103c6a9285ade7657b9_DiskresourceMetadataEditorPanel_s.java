 /**
  * 
  */
 package org.iplantc.de.client.views.panels;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.iplantc.core.jsonutil.JsonUtil;
 import org.iplantc.core.uicommons.client.ErrorHandler;
 import org.iplantc.core.uidiskresource.client.models.DiskResource;
 import org.iplantc.core.uidiskresource.client.models.DiskResourceMetadata;
 import org.iplantc.core.uidiskresource.client.models.JsDiskResourceMetaData;
 import org.iplantc.de.client.I18N;
 import org.iplantc.de.client.images.Resources;
 import org.iplantc.de.client.services.DiskResourceMetadataUpdateCallback;
 import org.iplantc.de.client.services.FolderServiceFacade;
 import org.iplantc.de.client.utils.DataUtils;
 
 import com.extjs.gxt.ui.client.event.ButtonEvent;
 import com.extjs.gxt.ui.client.event.Events;
 import com.extjs.gxt.ui.client.event.GridEvent;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
 import com.extjs.gxt.ui.client.event.SelectionChangedListener;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.store.ListStore;
 import com.extjs.gxt.ui.client.widget.MessageBox;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.extjs.gxt.ui.client.widget.form.Field;
 import com.extjs.gxt.ui.client.widget.form.TextField;
 import com.extjs.gxt.ui.client.widget.form.Validator;
 import com.extjs.gxt.ui.client.widget.grid.CellEditor;
 import com.extjs.gxt.ui.client.widget.grid.CheckBoxSelectionModel;
 import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
 import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
 import com.extjs.gxt.ui.client.widget.grid.EditorGrid;
 import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
 import com.google.gwt.core.client.JsArray;
 import com.google.gwt.json.client.JSONArray;
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.json.client.JSONParser;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.AbstractImagePrototype;
 
 /**
  * @author sriram
  * 
  *         A panel to display and edit DiskResource Metadata
  * 
  */
 public class DiskresourceMetadataEditorPanel extends MetadataEditorPanel {
 
     private static final String ID_DELETE = "idDelete";
     private static final String ID_ADD = "idAdd";
     private DiskResource resource;
     private EditorGrid<DiskResourceMetadata> grid;
     private ToolBar toolbar;
     private CheckBoxSelectionModel<DiskResourceMetadata> sm;
     private Set<String> toDelete;
     private int initial_count;
 
     public DiskresourceMetadataEditorPanel(DiskResource resource) {
         this.resource = resource;
         toDelete = new HashSet<String>();
         retrieveMetaData();
         initToolbar();
     }
 
     private HashMap<String, Object> getRecordsToUpdate() {
         HashMap<String, Object> map = new HashMap<String, Object>();
         map.put("add", grid.getStore().getModels());
         map.put("delete", toDelete);
         return map;
     }
 
     @Override
     protected void retrieveMetaData() {
         FolderServiceFacade facade = new FolderServiceFacade();
         facade.getMetaData(resource, new RetrieveMetadataCallback());
     }
 
     private void initToolbar() {
         toolbar = new ToolBar();
         setTopComponent(toolbar);
         Button add = buildButton(ID_ADD, I18N.DISPLAY.add(),
                 AbstractImagePrototype.create(Resources.ICONS.add()), new AddButtonSelectionListener());
         toolbar.add(add);
 
         Button delete = buildButton(ID_DELETE, I18N.DISPLAY.delete(),
                 AbstractImagePrototype.create(Resources.ICONS.cancel()),
                 new DeleteButtonSelectionListener());
         delete.setEnabled(false);
         toolbar.add(delete);
     }
 
     private Button buildButton(String id, String text,
             com.google.gwt.user.client.ui.AbstractImagePrototype icon,
             SelectionListener<ButtonEvent> listener) {
 
         Button b = new Button(text, icon, listener);
         b.setId(id);
         return b;
 
     }
 
     private void initGrid(List<DiskResourceMetadata> metadata) {
         ListStore<DiskResourceMetadata> store = new ListStore<DiskResourceMetadata>();
         store.add(metadata);
         grid = new EditorGrid<DiskResourceMetadata>(store, buildColumnModel());
         grid.setStripeRows(true);
         grid.setColumnLines(true);
         grid.setAutoExpandColumn(DiskResourceMetadata.ATTRIBUTE);
         grid.setSelectionModel(sm);
         grid.addPlugin(sm);
         grid.getSelectionModel().addSelectionChangedListener(new MetadataSelectionChangeListener());
         grid.addListener(Events.BeforeEdit, new GridBeforeEditListener());
        grid.addListener(Events.AfterEdit, new GridAfterEditListener());
         add(grid);
         layout();
     }
 
     private ColumnModel buildColumnModel() {
         // build column configs and add them to a list for the ColumnModel.
         List<ColumnConfig> columns = new ArrayList<ColumnConfig>();
 
         sm = new CheckBoxSelectionModel<DiskResourceMetadata>();
 
         ColumnConfig attr = new ColumnConfig(DiskResourceMetadata.ATTRIBUTE, "Attribute", 150);
         attr.setEditor(buildCellEditor(false));
         ColumnConfig val = new ColumnConfig(DiskResourceMetadata.VALUE, "Value", 150);
         val.setEditor(buildCellEditor(false));
         ColumnConfig unit = new ColumnConfig(DiskResourceMetadata.UNIT, "Unit", 75);
         unit.setEditor(buildCellEditor(true));
         columns.addAll(Arrays.asList(sm.getColumn(), attr, val, unit));
         return new ColumnModel(columns);
     }
 
     private CellEditor buildCellEditor(boolean allowBlank) {
         TextField<String> text = new TextField<String>();
         text.setAllowBlank(allowBlank);
         return new CellEditor(text);
     }
 
     private CellEditor buildAttributeCellEditor(boolean allowBlank) {
         TextField<String> text = new TextField<String>();
         text.setAllowBlank(allowBlank);
         text.setValidator(new Validator() {
 
             @Override
             public String validate(Field<?> field, String value) {
                 // TODO Auto-generated method stub
                 return null;
             }
         });
 
         return new CellEditor(text);
     }
 
     private final class DeleteButtonSelectionListener extends SelectionListener<ButtonEvent> {
         @Override
         public void componentSelected(ButtonEvent ce) {
             if (DataUtils.isMetadtaUpdatable(resource)) {
                 for (DiskResourceMetadata md : sm.getSelectedItems()) {
                     toDelete.add(md.getId());
                     grid.getStore().remove(md);
                 }
             } else {
                 MessageBox.alert(I18N.DISPLAY.permissionErrorTitle(),
                         I18N.DISPLAY.permissionErrorMessage(), null);
             }
         }
     }
 
     private class AddButtonSelectionListener extends SelectionListener<ButtonEvent> {
         @Override
         public void componentSelected(ButtonEvent ce) {
             // to distinguish new rows, id is set to null
             if (DataUtils.isMetadtaUpdatable(resource)) {
                 DiskResourceMetadata metadata = new DiskResourceMetadata(null, "attr", "value", "unit");
                 if (grid != null) {
                     grid.stopEditing();
                     grid.getStore().insert(metadata, 0);
                     grid.startEditing(grid.getStore().indexOf(metadata), 1);
                 }
             } else {
                 MessageBox.alert(I18N.DISPLAY.permissionErrorTitle(),
                         I18N.DISPLAY.permissionErrorMessage(), null);
             }
         }
     }
 
     @SuppressWarnings("rawtypes")
     private class GridBeforeEditListener implements Listener<GridEvent> {
         @Override
         public void handleEvent(GridEvent be) {
             if (be.getProperty().equals(DiskResourceMetadata.ATTRIBUTE)) {
                 // cache attributes that are going to be edited
                 Object o = be.getModel();
                 if (o != null && !o.toString().isEmpty()) {
                     toDelete.add(o.toString());
                 }
             }
         }
     }
 
     // @SuppressWarnings("rawtypes")
     // private class GridAfterEditListener implements Listener<GridEvent> {
     // @Override
     // public void handleEvent(GridEvent be) {
     // if (be.getProperty().equals(DiskResourceMetadata.ATTRIBUTE)) {
     // // cache attributes that are going to be edited
     // DiskResourceMetadata o = (DiskResourceMetadata)be.getModel();
     // for (DiskResourceMetadata drmd : grid.getStore().getModels()) {
     // if (drmd.equals(o)) {
     // continue;
     // } else {
     // if (drmd.getAttribute().equals(o.getAttribute())) {
     // grid.stopEditing();
     // ColumnConfig c = grid.getColumnModel().getColumn(be.getColIndex());
     //
     //
     // }
     // }
     // }
     // }
     // }
     // }
 
     private class MetadataSelectionChangeListener extends SelectionChangedListener<DiskResourceMetadata> {
         @Override
         public void selectionChanged(SelectionChangedEvent<DiskResourceMetadata> se) {
             if (se.getSelection().size() > 0) {
                 toolbar.getItemByItemId(ID_DELETE).enable();
             } else {
                 toolbar.getItemByItemId(ID_DELETE).disable();
             }
 
         }
     }
 
     private class RetrieveMetadataCallback implements AsyncCallback<String> {
 
         @Override
         public void onFailure(Throwable caught) {
             ErrorHandler.post(caught);
         }
 
         @Override
         public void onSuccess(String result) {
             JSONObject obj = JSONParser.parseStrict(result).isObject();
             JSONArray arr = obj.get("metadata").isArray();
             List<DiskResourceMetadata> metadata_list = new ArrayList<DiskResourceMetadata>();
             if (arr != null) {
                 JsArray<JsDiskResourceMetaData> jsmetadata = JsonUtil.asArrayOf(arr.toString());
                 for (int i = 0; i < jsmetadata.length(); i++) {
                     DiskResourceMetadata metadata = new DiskResourceMetadata(
                             jsmetadata.get(i).getAttr(), jsmetadata.get(i).getAttr(), jsmetadata.get(i)
                                     .getVal(), jsmetadata.get(i).getUnit());
                     metadata_list.add(metadata);
                 }
             }
 
             initGrid(metadata_list);
             initial_count = metadata_list.size();
 
         }
     }
 
     @Override
     public void UpdateMetadata() {
         JSONArray arr = new JSONArray();
         FolderServiceFacade facade = new FolderServiceFacade();
         int i = 0;
         @SuppressWarnings("unchecked")
         List<DiskResourceMetadata> metadatas = (List<DiskResourceMetadata>)getRecordsToUpdate().get(
                 "add");
         for (DiskResourceMetadata r : metadatas) {
             arr.set(i++, r.toJson());
         }
         JSONObject obj = new JSONObject();
         obj.put("avus", arr);
         facade.setMetaData(resource, obj.toString(), new DiskResourceMetadataUpdateCallback());
 
     }
 
     @Override
     public boolean isDirty() {
         return initial_count != grid.getStore().getCount() || !toDelete.isEmpty()
                 || grid.getStore().getModifiedRecords().size() > 0;
     }
 
 }
