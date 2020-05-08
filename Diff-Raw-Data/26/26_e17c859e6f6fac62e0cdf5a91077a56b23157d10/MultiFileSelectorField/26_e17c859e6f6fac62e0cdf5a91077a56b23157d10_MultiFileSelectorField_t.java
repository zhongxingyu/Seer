 package org.iplantc.core.uidiskresource.client.views.widgets;
 
 import java.util.List;
 import java.util.Set;
 
 import org.iplantc.core.resources.client.messages.I18N;
import org.iplantc.core.uicommons.client.events.EventBus;
import org.iplantc.core.uicommons.client.events.UserSettingsUpdatedEvent;
 import org.iplantc.core.uicommons.client.models.HasId;
import org.iplantc.core.uicommons.client.models.UserSettings;
 import org.iplantc.core.uicommons.client.models.diskresources.DiskResource;
import org.iplantc.core.uicommons.client.util.DiskResourceUtil;
 import org.iplantc.core.uidiskresource.client.models.DiskResourceModelKeyProvider;
 import org.iplantc.core.uidiskresource.client.models.DiskResourceProperties;
 import org.iplantc.core.uidiskresource.client.views.dialogs.FileSelectDialog;
 
 import com.google.common.collect.Lists;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.editor.client.EditorDelegate;
 import com.google.gwt.editor.client.ValueAwareEditor;
 import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.event.shared.HandlerRegistration;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiFactory;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.user.client.ui.Widget;
 import com.sencha.gxt.data.shared.ListStore;
 import com.sencha.gxt.widget.core.client.Composite;
 import com.sencha.gxt.widget.core.client.button.TextButton;
 import com.sencha.gxt.widget.core.client.event.HideEvent;
 import com.sencha.gxt.widget.core.client.event.HideEvent.HideHandler;
 import com.sencha.gxt.widget.core.client.event.SelectEvent;
 import com.sencha.gxt.widget.core.client.form.IsField;
 import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
 import com.sencha.gxt.widget.core.client.grid.ColumnModel;
 import com.sencha.gxt.widget.core.client.grid.Grid;
 import com.sencha.gxt.widget.core.client.grid.GridView;
 import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent;
 import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent.SelectionChangedHandler;
 import com.sencha.gxt.widget.core.client.toolbar.ToolBar;
 
 /**
  * TODO JDS Implement drag and drop
  *
  * @author jstroot
  *
  */
 public class MultiFileSelectorField extends Composite implements IsField<List<HasId>>, ValueAwareEditor<List<HasId>>, HasValueChangeHandlers<List<HasId>> {
 
     interface MultiFileSelectorFieldUiBinder extends UiBinder<Widget, MultiFileSelectorField> {
     }
 
     private static MultiFileSelectorFieldUiBinder BINDER = GWT.create(MultiFileSelectorFieldUiBinder.class);
 
     @UiField
     ToolBar toolbar;
 
     @UiField
     TextButton addButton;
 
     @UiField
     TextButton deleteButton;
 
     @UiField
     Grid<DiskResource> grid;
 
     @UiField
     GridView<DiskResource> gridView;
 
     @UiField
     ListStore<DiskResource> listStore;
 
     @UiField
     ColumnModel<DiskResource> cm;
 
     private boolean addDeleteButtonsEnabled = true;
    
    UserSettings userSettings = UserSettings.getInstance();
 
     public MultiFileSelectorField() {
         initWidget(BINDER.createAndBindUi(this));
 
         grid.getSelectionModel().addSelectionChangedHandler(new SelectionChangedHandler<DiskResource>() {
             @Override
             public void onSelectionChanged(SelectionChangedEvent<DiskResource> event) {
                 List<DiskResource> selection = event.getSelection();
                 deleteButton.setEnabled((selection != null) && !selection.isEmpty());
             }
         });
 
         grid.setBorders(true);
     }
 
     @UiFactory
     ColumnModel<DiskResource> createColumnModel() {
         List<ColumnConfig<DiskResource, ?>> list = Lists.newArrayList();
         DiskResourceProperties props = GWT.create(DiskResourceProperties.class);
 
         ColumnConfig<DiskResource, String> name = new ColumnConfig<DiskResource, String>(props.name(), 130, I18N.DISPLAY.name());
         list.add(name);
         return new ColumnModel<DiskResource>(list);
     }
 
     @UiFactory
     ListStore<DiskResource> createListStore() {
         return new ListStore<DiskResource>(new DiskResourceModelKeyProvider());
     }
 
     @UiHandler("addButton")
     void onAddButtonSelected(SelectEvent event) {
         if (!addDeleteButtonsEnabled) {
             return;
         }
         // Open a multiselect file selector
         FileSelectDialog dlg = new FileSelectDialog();
         dlg.addHideHandler(new FileSelectDialogHideHandler(dlg, listStore));
        if (userSettings.isRememberLastPath()) {
            String id = userSettings.getLastPathId();
            if (id != null) {
                dlg = FileSelectDialog.selectParentFolderById(id);
            } else {
                dlg = FileSelectDialog.singleSelect(null);
            }
        } else {
            dlg = FileSelectDialog.singleSelect(null);
        }
         dlg.show();
     }
 
     @UiHandler("deleteButton")
     void onDeleteButtonSelected(SelectEvent event) {
         if (!addDeleteButtonsEnabled) {
             return;
         }
         for (DiskResource dr : grid.getSelectionModel().getSelectedItems()) {
             listStore.remove(dr);
         }
     }
 
     @Override
     public void setValue(List<HasId> value) {
         if ((value == null) || !value.isEmpty())
             return;
 
         // TBI JDS Assume the incoming value is a JSON array of ..... ?
 
     }
 
     @Override
     public List<HasId> getValue() {
         List<HasId> hasIdList = Lists.newArrayList();
         for (DiskResource dr : listStore.getAll()) {
             hasIdList.add(dr);
         }
         return hasIdList;
     }
 
     private final class FileSelectDialogHideHandler implements HideHandler {
         private final FileSelectDialog dlg;
         private final ListStore<DiskResource> store;
 
         public FileSelectDialogHideHandler(final FileSelectDialog dlg, final ListStore<DiskResource> store) {
             this.dlg = dlg;
             this.store = store;
         }
 
         @Override
         public void onHide(HideEvent event) {
             Set<DiskResource> diskResources = dlg.getDiskResources();
             if ((diskResources == null) || diskResources.isEmpty()) {
                 return;
             }
             store.addAll(diskResources);
            if (userSettings.isRememberLastPath()) {
                userSettings.setLastPathId(DiskResourceUtil.parseParent(store.get(0).getId()));
                UserSettingsUpdatedEvent usue = new UserSettingsUpdatedEvent();
                EventBus.getInstance().fireEvent(usue);
            }
             ValueChangeEvent.fire(MultiFileSelectorField.this, Lists.<HasId> newArrayList(store.getAll()));
         }
     }
 
     @Override
     public void clear() {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     public void clearInvalid() {
         // TODO Auto-generated method stub
     }
 
     @Override
     public void reset() {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     public boolean isValid(boolean preventMark) {
         // TODO Auto-generated method stub
         return false;
     }
 
     @Override
     public boolean validate(boolean preventMark) {
         // TODO Auto-generated method stub
         return false;
     }
 
     @Override
     public void setDelegate(EditorDelegate<List<HasId>> delegate) {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     public void flush() {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     public void onPropertyChange(String... paths) {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     public HandlerRegistration addValueChangeHandler(ValueChangeHandler<List<HasId>> handler) {
         return addHandler(handler, ValueChangeEvent.getType());
     }
 
     public void disableAddDeleteButtons() {
         addDeleteButtonsEnabled = false;
     }
 
 }
