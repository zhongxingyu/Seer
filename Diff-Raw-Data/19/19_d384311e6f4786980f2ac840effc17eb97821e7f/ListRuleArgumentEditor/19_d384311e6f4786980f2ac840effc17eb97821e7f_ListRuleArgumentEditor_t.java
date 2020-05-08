 package org.iplantc.core.tito.client.dialogs.validation;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.iplantc.core.metadata.client.validation.ListRuleArgument;
 import org.iplantc.core.metadata.client.validation.ListRuleArgumentFactory;
 import org.iplantc.core.metadata.client.validation.ListRuleArgumentGroup;
 import org.iplantc.core.tito.client.I18N;
 import org.iplantc.core.tito.client.images.Resources;
 import org.iplantc.core.tito.client.services.EnumerationServices;
 import org.iplantc.core.uicommons.client.ErrorHandler;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ChangeEvent;
 import com.google.gwt.event.dom.client.ChangeHandler;
 import com.google.gwt.user.client.Command;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.sencha.gxt.cell.core.client.form.CheckBoxCell;
 import com.sencha.gxt.core.client.Style.SelectionMode;
 import com.sencha.gxt.core.client.ValueProvider;
 import com.sencha.gxt.core.client.dom.XElement;
 import com.sencha.gxt.data.shared.ModelKeyProvider;
 import com.sencha.gxt.data.shared.TreeStore;
 import com.sencha.gxt.data.shared.event.StoreAddEvent;
 import com.sencha.gxt.data.shared.event.StoreClearEvent;
 import com.sencha.gxt.data.shared.event.StoreDataChangeEvent;
 import com.sencha.gxt.data.shared.event.StoreFilterEvent;
 import com.sencha.gxt.data.shared.event.StoreHandlers;
 import com.sencha.gxt.data.shared.event.StoreRecordChangeEvent;
 import com.sencha.gxt.data.shared.event.StoreRemoveEvent;
 import com.sencha.gxt.data.shared.event.StoreSortEvent;
 import com.sencha.gxt.data.shared.event.StoreUpdateEvent;
 import com.sencha.gxt.widget.core.client.button.TextButton;
 import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
 import com.sencha.gxt.widget.core.client.event.BeforeStartEditEvent;
 import com.sencha.gxt.widget.core.client.event.BeforeStartEditEvent.BeforeStartEditHandler;
 import com.sencha.gxt.widget.core.client.event.SelectEvent;
 import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
 import com.sencha.gxt.widget.core.client.event.ViewReadyEvent;
 import com.sencha.gxt.widget.core.client.event.ViewReadyEvent.ViewReadyHandler;
 import com.sencha.gxt.widget.core.client.form.CheckBox;
 import com.sencha.gxt.widget.core.client.form.TextField;
 import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
 import com.sencha.gxt.widget.core.client.grid.ColumnModel;
 import com.sencha.gxt.widget.core.client.grid.editing.GridInlineEditing;
 import com.sencha.gxt.widget.core.client.toolbar.FillToolItem;
 import com.sencha.gxt.widget.core.client.toolbar.ToolBar;
 import com.sencha.gxt.widget.core.client.treegrid.TreeGrid;
 
 /**
  * A container with a TreeGrid editor and button toolbar for editing hierarchical list selectors.
  * 
  * @author psarando
  * 
  */
 public class ListRuleArgumentEditor extends VerticalLayoutContainer {
 
     private final ListRuleArgumentFactory factory = GWT.create(ListRuleArgumentFactory.class);
     private TreeGrid<ListRuleArgument> treeEditor;
     private CheckBox forceSingleSelection;
     private int countGroupLabel = 1;
     private int countArgLabel = 1;
 
     public ListRuleArgumentEditor(ListRuleArgumentGroup root) {
         init();
         setValues(root);
     }
 
     public void addUpdateCommand(final Command cmdUpdate) {
         // TODO clean up handlers?
         if (cmdUpdate != null) {
             if (forceSingleSelection != null) {
                 forceSingleSelection.addChangeHandler(new ChangeHandler() {
 
                     @Override
                     public void onChange(ChangeEvent event) {
                         cmdUpdate.execute();
                     }
                 });
             }
 
             if (treeEditor != null) {
                 treeEditor.getStore().addStoreHandlers(new StoreHandlers<ListRuleArgument>() {
 
                     @Override
                     public void onSort(StoreSortEvent<ListRuleArgument> event) {
                         // do nothing, sorting the grid does not sort the store.
                     }
 
                     @Override
                     public void onRecordChange(StoreRecordChangeEvent<ListRuleArgument> event) {
                         cmdUpdate.execute();
                     }
 
                     @Override
                     public void onDataChange(StoreDataChangeEvent<ListRuleArgument> event) {
                         cmdUpdate.execute();
                     }
 
                     @Override
                     public void onUpdate(StoreUpdateEvent<ListRuleArgument> event) {
                         cmdUpdate.execute();
                     }
 
                     @Override
                     public void onClear(StoreClearEvent<ListRuleArgument> event) {
                         cmdUpdate.execute();
                     }
 
                     @Override
                     public void onFilter(StoreFilterEvent<ListRuleArgument> event) {
                         // do nothing
                     }
 
                     @Override
                     public void onRemove(StoreRemoveEvent<ListRuleArgument> event) {
                         cmdUpdate.execute();
                     }
 
                     @Override
                     public void onAdd(StoreAddEvent<ListRuleArgument> event) {
                         cmdUpdate.execute();
                     }
                 });
             }
         }
     }
 
     private void init() {
         setHeight(200);
         setBorders(true);
 
         initTreeEditor();
 
         add(buildToolBar(), new VerticalLayoutData(1, -1));
         add(treeEditor, new VerticalLayoutData(1, 1));
     }
 
     private void initTreeEditor() {
         ColumnConfig<ListRuleArgument, Boolean> defaultConfig = buildIsDefaultConfig();
         ColumnConfig<ListRuleArgument, String> displayConfig = buildDisplayConfig();
         ColumnConfig<ListRuleArgument, String> nameConfig = buildNameConfig();
         ColumnConfig<ListRuleArgument, String> valueConfig = buildValueConfig();
         ColumnConfig<ListRuleArgument, String> descriptionConfig = buildDescriptionConfig();
 
         List<ColumnConfig<ListRuleArgument, ?>> cmList = new ArrayList<ColumnConfig<ListRuleArgument, ?>>();
         cmList.add(defaultConfig);
         cmList.add(displayConfig);
         cmList.add(nameConfig);
         cmList.add(valueConfig);
         cmList.add(descriptionConfig);
 
         final ColumnModel<ListRuleArgument> cm = new ColumnModel<ListRuleArgument>(cmList);
 
         treeEditor = new TreeGrid<ListRuleArgument>(buildStore(), cm, displayConfig);
         treeEditor.getView().setAutoExpandColumn(displayConfig);
         treeEditor.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
 
         // These settings ensure that empty groups still display with a group icon.
         treeEditor.setAutoExpand(true);
         treeEditor.addViewReadyHandler(new ViewReadyHandler() {
             @Override
             public void onViewReady(ViewReadyEvent event) {
                 TreeStore<ListRuleArgument> store = treeEditor.getTreeStore();
 
                 for (ListRuleArgument arg : store.getAll()) {
                     if (arg instanceof ListRuleArgumentGroup) {
                         treeEditor.setLeaf(arg, false);
                         store.update(arg);
                     }
                 }
             }
         });
         
         GridInlineEditing<ListRuleArgument> editing = new GridInlineEditing<ListRuleArgument>(treeEditor);
         editing.addEditor(displayConfig, new TextField());
         editing.addEditor(nameConfig, new TextField());
         editing.addEditor(valueConfig, new TextField());
         editing.addEditor(descriptionConfig, new TextField());
 
         addInlineEditingHandlers(editing);
     }
 
     private ColumnConfig<ListRuleArgument, Boolean> buildIsDefaultConfig() {
         ColumnConfig<ListRuleArgument, Boolean> defaultConfig = new ColumnConfig<ListRuleArgument, Boolean>(
                 new ValueProvider<ListRuleArgument, Boolean>() {
 
                     @Override
                     public Boolean getValue(ListRuleArgument object) {
                         return object.isDefault();
                     }
 
                     @Override
                     public void setValue(ListRuleArgument object, Boolean value) {
                         // Do not allow a group to be checked if SingleSelection is enabled.
                         boolean isSingleSelection = forceSingleSelection.getValue();
                         boolean isGroup = object instanceof ListRuleArgumentGroup;
 
                        if (value && isSingleSelection) {
                            if (isGroup) {
                                // Do not allow a group to be checked if SingleSelection is enabled.
                                return;
                            }

                            // If the user is checking an argument, uncheck all other arguments.
                            TreeStore<ListRuleArgument> store = treeEditor.getTreeStore();
                            for (ListRuleArgument ruleArg : store.getAll()) {
                                if (ruleArg.isDefault()) {
                                    ruleArg.setDefault(false);
                                    store.update(ruleArg);
                                }
                            }
                         }

                        setDefaultValue(object, value);
                     }
 
                     @Override
                     public String getPath() {
                         return "isDefault"; //$NON-NLS-1$
                     }
                 });
 
         defaultConfig.setHeader(I18N.DISPLAY.defaultVal());
         defaultConfig.setWidth(50);
         defaultConfig.setCell(new CheckBoxCell());
 
         return defaultConfig;
     }
 
     private void setDefaultValue(ListRuleArgument object, Boolean value) {
         if (object == null) {
             return;
         }
 
         TreeStore<ListRuleArgument> store = treeEditor.getTreeStore();
 
         object.setDefault(value);
         store.update(object);
 
         if (object instanceof ListRuleArgumentGroup) {
             ListRuleArgumentGroup group = (ListRuleArgumentGroup)object;
 
             List<ListRuleArgumentGroup> groups = group.getGroups();
             if (groups != null) {
                 for (ListRuleArgument child : groups) {
                     setDefaultValue(child, value);
                 }
             }
 
             List<ListRuleArgument> children = group.getArguments();
             if (children != null) {
                 for (ListRuleArgument child : children) {
                     setDefaultValue(child, value);
                 }
             }
         }
 
         if (!value) {
             for (ListRuleArgument parent = store.getParent(object); parent != null; parent = store
                     .getParent(parent)) {
                 parent.setDefault(value);
                 store.update(parent);
             }
         }
     }
 
     private ColumnConfig<ListRuleArgument, String> buildDisplayConfig() {
         ColumnConfig<ListRuleArgument, String> displayConfig = new ColumnConfig<ListRuleArgument, String>(
                 new ValueProvider<ListRuleArgument, String>() {
 
                     @Override
                     public String getValue(ListRuleArgument object) {
                         return object.getDisplay();
                     }
 
                     @Override
                     public void setValue(ListRuleArgument object, String value) {
                         object.setDisplay(value);
                     }
 
                     @Override
                     public String getPath() {
                         return "display"; //$NON-NLS-1$
                     }
                 });
 
         displayConfig.setHeader(I18N.DISPLAY.display());
 
         return displayConfig;
     }
 
     private ColumnConfig<ListRuleArgument, String> buildNameConfig() {
         ColumnConfig<ListRuleArgument, String> nameConfig = new ColumnConfig<ListRuleArgument, String>(
                 new ValueProvider<ListRuleArgument, String>() {
 
                     @Override
                     public String getValue(ListRuleArgument object) {
                         return object.getName();
                     }
 
                     @Override
                     public void setValue(ListRuleArgument object, String value) {
                         object.setName(value);
                     }
 
                     @Override
                     public String getPath() {
                         return "name"; //$NON-NLS-1$
                     }
                 });
 
         nameConfig.setHeader(I18N.DISPLAY.parameter());
 
         return nameConfig;
     }
 
     private ColumnConfig<ListRuleArgument, String> buildValueConfig() {
         ColumnConfig<ListRuleArgument, String> valueConfig = new ColumnConfig<ListRuleArgument, String>(
                 new ValueProvider<ListRuleArgument, String>() {
 
                     @Override
                     public String getValue(ListRuleArgument object) {
                         return object.getValue();
                     }
 
                     @Override
                     public void setValue(ListRuleArgument object, String value) {
                         object.setValue(value);
                     }
 
                     @Override
                     public String getPath() {
                         return "value"; //$NON-NLS-1$
                     }
                 });
 
         valueConfig.setHeader(I18N.DISPLAY.values());
 
         return valueConfig;
     }
 
     private ColumnConfig<ListRuleArgument, String> buildDescriptionConfig() {
         ColumnConfig<ListRuleArgument, String> descriptionConfig = new ColumnConfig<ListRuleArgument, String>(
                 new ValueProvider<ListRuleArgument, String>() {
 
                     @Override
                     public String getValue(ListRuleArgument object) {
                         return object.getDescription();
                     }
 
                     @Override
                     public void setValue(ListRuleArgument object, String value) {
                         object.setDescription(value);
                     }
 
                     @Override
                     public String getPath() {
                         return "description"; //$NON-NLS-1$
                     }
                 });
 
         descriptionConfig.setHeader(I18N.DISPLAY.toolTipText());
 
         return descriptionConfig;
     }
 
     private TreeStore<ListRuleArgument> buildStore() {
         TreeStore<ListRuleArgument> store = new TreeStore<ListRuleArgument>(
                 new ModelKeyProvider<ListRuleArgument>() {
                     @Override
                     public String getKey(ListRuleArgument item) {
                         return item.getId();
                     }
                 });
 
         store.setAutoCommit(true);
 
         return store;
     }
 
     private void addInlineEditingHandlers(GridInlineEditing<ListRuleArgument> editing) {
         // TODO clean up handlers?
         editing.addBeforeStartEditHandler(new BeforeStartEditHandler<ListRuleArgument>() {
 
             @Override
             public void onBeforeStartEdit(BeforeStartEditEvent<ListRuleArgument> event) {
                 ListRuleArgument arg = treeEditor.getStore().get(event.getEditCell().getRow());
 
                 if (arg instanceof ListRuleArgumentGroup) {
                     int colIndex = event.getEditCell().getCol();
                     ColumnConfig<ListRuleArgument, String> editingCol = treeEditor.getColumnModel()
                             .getColumn(colIndex);
 
                     // TODO allow tooltips
                     if (!treeEditor.getTreeColumn().equals(editingCol)) {
                         event.setCancelled(true);
                     }
                 }
             }
         });
     }
 
     private ToolBar buildToolBar() {
         ToolBar buttonBar = new ToolBar();
 
         buttonBar.add(buildForceSingleSelectionButton());
         buttonBar.add(buildAddGroupButton());
         buttonBar.add(buildAddArgumentButton());
         buttonBar.add(new FillToolItem());
         buttonBar.add(buildDeleteButton());
 
         return buttonBar;
     }
 
     private CheckBox buildForceSingleSelectionButton() {
         forceSingleSelection = new CheckBox();
 
         forceSingleSelection.setBoxLabel("Force Single Selection");
         forceSingleSelection.addChangeHandler(new ChangeHandler() {
 
             @Override
             public void onChange(ChangeEvent event) {
                 if (forceSingleSelection.getValue()) {
                     List<ListRuleArgument> checked = new ArrayList<ListRuleArgument>();
 
                     for (ListRuleArgument ruleArg : treeEditor.getTreeStore().getAll()) {
                         if (ruleArg.isDefault() && !(ruleArg instanceof ListRuleArgumentGroup)) {
                             checked.add(ruleArg);
                         }
                     }
 
                     if (checked.size() > 1) {
                         for (ListRuleArgument ruleArg : checked) {
                             setDefaultValue(ruleArg, false);
                         }
                     }
                 }
             }
         });
 
         return forceSingleSelection;
     }
 
     private TextButton buildAddGroupButton() {
         TextButton ret = new TextButton("Add Group", Resources.ICONS.add());
 
         ret.addSelectHandler(new SelectHandler() {
             @Override
             public void onSelect(SelectEvent event) {
                 EnumerationServices service = new EnumerationServices();
                 service.getUUID(new AsyncCallback<String>() {
                     @Override
                     public void onSuccess(String uuid) {
                         addGroup(uuid);
                     }
 
                     @Override
                     public void onFailure(Throwable caught) {
                         ErrorHandler.post(I18N.DISPLAY.cantGenerateUuid(), caught);
                     }
                 });
             }
         });
 
         return ret;
     }
 
     private TextButton buildAddArgumentButton() {
         TextButton ret = new TextButton("Add Argument", Resources.ICONS.add());
 
         ret.addSelectHandler(new SelectHandler() {
             @Override
             public void onSelect(SelectEvent event) {
                 EnumerationServices service = new EnumerationServices();
                 service.getUUID(new AsyncCallback<String>() {
                     @Override
                     public void onSuccess(String uuid) {
                         addArgument(uuid);
                     }
 
                     @Override
                     public void onFailure(Throwable caught) {
                         ErrorHandler.post(I18N.DISPLAY.cantGenerateUuid(), caught);
                     }
                 });
             }
         });
 
         return ret;
     }
 
     private TextButton buildDeleteButton() {
         TextButton ret = new TextButton(I18N.DISPLAY.delete(), Resources.ICONS.cancel());
 
         ret.addSelectHandler(new SelectHandler() {
             @Override
             public void onSelect(SelectEvent event) {
                 ListRuleArgument selected = treeEditor.getSelectionModel().getSelectedItem();
                 if (selected != null) {
                     TreeStore<ListRuleArgument> store = treeEditor.getTreeStore();
 
                     ListRuleArgument parent = store.getParent(selected);
                     if (parent != null) {
                         ListRuleArgumentGroup group = (ListRuleArgumentGroup)parent;
 
                         if (selected instanceof ListRuleArgumentGroup) {
                             group.getGroups().remove(selected);
                         } else {
                             group.getArguments().remove(selected);
                         }
                     }
 
                     store.remove(selected);
                 }
             }
         });
 
         return ret;
     }
 
     private void addGroup(String uuid) {
         ListRuleArgumentGroup group = createGroup(uuid);
 
         ListRuleArgumentGroup selectedGroup = getSelectedGroup();
         if (selectedGroup != null) {
             List<ListRuleArgumentGroup> groups = selectedGroup.getGroups();
 
             if (groups == null) {
                 groups = new ArrayList<ListRuleArgumentGroup>();
                 selectedGroup.setGroups(groups);
             }
 
             groups.add(group);
         }
 
         addRuleArgument(selectedGroup, group);
 
         treeEditor.setLeaf(group, false);
         treeEditor.getTreeStore().update(group);
     }
 
     private void addArgument(String uuid) {
         ListRuleArgument ruleArg = createArgument(uuid);
 
         ListRuleArgumentGroup selectedGroup = getSelectedGroup();
         if (selectedGroup != null) {
             List<ListRuleArgument> arguments = selectedGroup.getArguments();
 
             if (arguments == null) {
                 arguments = new ArrayList<ListRuleArgument>();
                 selectedGroup.setArguments(arguments);
             }
 
             arguments.add(ruleArg);
         }
 
         addRuleArgument(selectedGroup, ruleArg);
     }
 
     private ListRuleArgumentGroup getSelectedGroup() {
         ListRuleArgument selected = treeEditor.getSelectionModel().getSelectedItem();
 
         if (selected != null) {
             if (selected instanceof ListRuleArgumentGroup) {
                 return (ListRuleArgumentGroup)selected;
             } else {
                 selected = treeEditor.getTreeStore().getParent(selected);
                 if (selected != null) {
                     return (ListRuleArgumentGroup)selected;
                 }
             }
         }
 
         return null;
     }
 
     private void addRuleArgument(ListRuleArgumentGroup selectedGroup, ListRuleArgument ruleArg) {
         if (selectedGroup != null) {
             ruleArg.setDefault(selectedGroup.isDefault());
 
             treeEditor.getTreeStore().add(selectedGroup, ruleArg);
             treeEditor.setExpanded(selectedGroup, true);
         } else {
             treeEditor.getTreeStore().add(ruleArg);
         }
 
         XElement fxEl = XElement.as(treeEditor.getTreeView().getRow(ruleArg));
         fxEl.scrollIntoView();
     }
 
     private ListRuleArgumentGroup createGroup(String uuid) {
         ListRuleArgumentGroup group = factory.group().as();
         group.setId(uuid);
 
         group.setDisplay(I18N.DISPLAY.newPropertyGroupLabel(countGroupLabel++));
 
         return group;
     }
 
     private ListRuleArgument createArgument(String uuid) {
         ListRuleArgument argString = factory.argument().as();
         argString.setId(uuid);
 
         argString.setDisplay(I18N.DISPLAY.newPropertyLabel(countArgLabel++));
 
         return argString;
     }
 
     /**
      * Resets the editor with the values in the given root group.
      * 
      * @param root
      */
     public void setValues(ListRuleArgumentGroup root) {
         TreeStore<ListRuleArgument> store = treeEditor.getTreeStore();
         store.clear();
 
         if (root != null) {
             forceSingleSelection.setValue(root.isSingleSelect());
 
             if (root.getGroups() != null) {
                 for (ListRuleArgumentGroup group : root.getGroups()) {
                     addGroupToStore(null, group);
                 }
             }
 
             if (root.getArguments() != null) {
                 for (ListRuleArgument ruleArg : root.getArguments()) {
                     store.add(ruleArg);
                 }
             }
         }
     }
 
     private void addGroupToStore(ListRuleArgumentGroup parent, ListRuleArgumentGroup group) {
         if (group != null) {
             TreeStore<ListRuleArgument> store = treeEditor.getTreeStore();
 
             if (parent != null) {
                 store.add(parent, group);
             } else {
                 store.add(group);
             }
 
             treeEditor.setLeaf(group, false);
             store.update(group);
 
             if (group.getGroups() != null) {
                 for (ListRuleArgumentGroup child : group.getGroups()) {
                     addGroupToStore(group, child);
                 }
             }
 
             if (group.getArguments() != null) {
                 for (ListRuleArgument child : group.getArguments()) {
                     store.add(group, child);
                 }
             }
         }
     }
 
     /**
      * Returns a group of values added by the editor.
      * 
      * @return A root group containing the groups and args added by the editor.
      */
     public ListRuleArgumentGroup getValues() {
         List<ListRuleArgumentGroup> groups = new ArrayList<ListRuleArgumentGroup>();
         List<ListRuleArgument> arguments = new ArrayList<ListRuleArgument>();
 
         for (ListRuleArgument ruleArg : treeEditor.getTreeStore().getRootItems()) {
             if (ruleArg instanceof ListRuleArgumentGroup) {
                 groups.add((ListRuleArgumentGroup)ruleArg);
             } else {
                 arguments.add(ruleArg);
             }
         }
 
         ListRuleArgumentGroup root = factory.group().as();
         root.setGroups(groups);
         root.setArguments(arguments);
         root.setSingleSelect(forceSingleSelection.getValue());
 
         return root;
     }
 }
