 package org.iplantc.core.tito.client.panels;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.iplantc.core.metadata.client.JSONMetaDataObject;
import org.iplantc.core.metadata.client.property.DataObject;
 import org.iplantc.core.metadata.client.property.Property;
 import org.iplantc.core.metadata.client.property.groups.PropertyGroup;
 import org.iplantc.core.metadata.client.property.groups.PropertyGroupContainer;
 import org.iplantc.core.tito.client.I18N;
 import org.iplantc.core.tito.client.events.JSONMetaDataObjectChangedEvent;
 import org.iplantc.core.tito.client.events.JSONMetaDataObjectChangedEventHandler;
 import org.iplantc.core.tito.client.events.NavigationTreeAddEvent;
 import org.iplantc.core.tito.client.events.NavigationTreeBeforeSelectionEvent;
 import org.iplantc.core.tito.client.events.NavigationTreeDeleteEvent;
 import org.iplantc.core.tito.client.events.NavigationTreeSelectionChangeEvent;
 import org.iplantc.core.tito.client.events.TemplateNameChangeEvent;
 import org.iplantc.core.tito.client.events.TemplateNameChangeEventHandler;
 import org.iplantc.core.tito.client.images.Resources;
 import org.iplantc.core.tito.client.models.MetaDataTreeModel;
 import org.iplantc.core.tito.client.models.MetaDataTreeModel.TreeElementType;
 import org.iplantc.core.tito.client.services.EnumerationServices;
 import org.iplantc.core.tito.client.utils.PanelHelper;
 import org.iplantc.core.tito.client.utils.PropertyGroupContainerUtil;
 import org.iplantc.core.tito.client.utils.PropertyUtil;
 import org.iplantc.core.uicommons.client.ErrorHandler;
 import org.iplantc.core.uicommons.client.events.EventBus;
 
 import com.extjs.gxt.ui.client.Style.Scroll;
 import com.extjs.gxt.ui.client.data.ModelData;
 import com.extjs.gxt.ui.client.data.ModelIconProvider;
 import com.extjs.gxt.ui.client.data.ModelStringProvider;
 import com.extjs.gxt.ui.client.dnd.DND.Feedback;
 import com.extjs.gxt.ui.client.dnd.TreePanelDragSource;
 import com.extjs.gxt.ui.client.dnd.TreePanelDropTarget;
 import com.extjs.gxt.ui.client.event.BaseEvent;
 import com.extjs.gxt.ui.client.event.ButtonEvent;
 import com.extjs.gxt.ui.client.event.ComponentEvent;
 import com.extjs.gxt.ui.client.event.DNDEvent;
 import com.extjs.gxt.ui.client.event.Events;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.event.TreePanelEvent;
 import com.extjs.gxt.ui.client.store.TreeStore;
 import com.extjs.gxt.ui.client.util.Format;
 import com.extjs.gxt.ui.client.widget.ContentPanel;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.extjs.gxt.ui.client.widget.layout.FitLayout;
 import com.extjs.gxt.ui.client.widget.menu.Menu;
 import com.extjs.gxt.ui.client.widget.menu.MenuItem;
 import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
 import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
 import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
 import com.extjs.gxt.ui.client.widget.treepanel.TreePanel.TreeNode;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.AbstractImagePrototype;
 
 /**
  * Displays properties and property groups in a tree view.
  */
 public class NavigationTreePanel extends ContentPanel {
     private static final String ID_PROPERTY_MENU_ITEM = "idAddPropertyItem"; //$NON-NLS-1$
     private static final String ID_STATIC_TEXT_MENU_ITEM = "idAddDescTextMenuItem"; //$NON-NLS-1$
     private static final String ID_PROPERTY_GROUP_MENU_ITEM = "idAddGroupItem"; //$NON-NLS-1$
 
     // default type when a property is first created
    private static final String DEFAULT_TYPE_PROPERTY = DataObject.INPUT_TYPE;
 
     private TreePanel<MetaDataTreeModel> tree;
     private Menu propertyMenu;
     private Menu groupMenu;
     private Button delete;
     private Menu addMenu;
     private final PropertyGroupContainer container;
 
     /**
      * Creates a new NavigationTreePanel and populates it with the elements of a PropertyGroupContainer.
      * 
      * @param container
      * @param dataObjects
      */
     public NavigationTreePanel(PropertyGroupContainer container) {
         this.container = container;
 
         init();
         initTree();
         addContextMenu();
         buildMenuToolBar();
         compose();
     }
 
     private void init() {
         setHeaderVisible(false);
         setLayout(new FitLayout());
     }
 
     private void addListeners() {
         tree.getSelectionModel().addListener(Events.BeforeSelect, new Listener<BaseEvent>() {
             @Override
             public void handleEvent(BaseEvent be) {
                 boolean cancelled = fireBeforeSelectionChange();
 
                 // if the tree selection change got cancelled (i.e. validation failed),
                 // cancel the select event as well
                 if (cancelled) {
                     be.setCancelled(true);
                 }
             }
         });
 
         tree.getSelectionModel().addListener(Events.SelectionChange, new Listener<BaseEvent>() {
             @Override
             public void handleEvent(BaseEvent be) {
                 updateButtonOptions();
                 fireTreeSelectionChange();
             }
         });
 
         // update tree node when the label of the corresponding property sheet changes
         EventBus.getInstance().addHandler(JSONMetaDataObjectChangedEvent.TYPE,
                 new JSONMetaDataObjectChangedEventHandlerImpl());
 
         EventBus.getInstance().addHandler(TemplateNameChangeEvent.TYPE,
                 new TemplateNameChangeEventHandlerImpl());
 
     }
 
     private void buildMenuToolBar() {
         delete = buildDeleteButton();
 
         Button btnAdd = new Button(I18N.DISPLAY.add(), AbstractImagePrototype.create(Resources.ICONS.add()));
 
         buildAddMenu();
 
         btnAdd.setMenu(addMenu);
 
         ToolBar tool = new ToolBar();
         tool.add(btnAdd);
         tool.add(delete);
 
         tool.add(new FillToolItem());
 
         setTopComponent(tool);
         // addContextMenu();
     }
 
     private void buildAddMenu() {
         addMenu = new Menu();
         addMenu.add(buildAddPropertyMenuItem());
         addMenu.add(buildAddStaticTextMenuItem());
         addMenu.add(buildAddPropertyGroupMenuItem());
 
         // need to do manually since listeners are not added before initial selection.
         addMenu.getItemByItemId(ID_PROPERTY_GROUP_MENU_ITEM).enable();
         addMenu.getItemByItemId(ID_PROPERTY_MENU_ITEM).disable();
         addMenu.getItemByItemId(ID_STATIC_TEXT_MENU_ITEM).disable();
     }
 
     private Button buildDeleteButton() {
         Button ret = PanelHelper.buildButton("idDeleteBtn", I18N.DISPLAY.delete(), //$NON-NLS-1$
                 new DeleteButtonSelectionListenerImpl());
         ret.setEnabled(false);
         ret.setIcon(AbstractImagePrototype.create(Resources.ICONS.cancel()));
         return ret;
     }
 
     /**
      * Returns true if the event was cancelled by a receiver, false if not cancelled.
      * 
      * @return
      */
     private boolean fireBeforeSelectionChange() {
         NavigationTreeBeforeSelectionEvent e = new NavigationTreeBeforeSelectionEvent(tree
                 .getSelectionModel().getSelectedItem());
         EventBus.getInstance().fireEvent(e);
 
         return e.isCancelled();
     }
 
     private void fireTreeSelectionChange() {
         NavigationTreeSelectionChangeEvent e = new NavigationTreeSelectionChangeEvent(tree
                 .getSelectionModel().getSelectedItem());
 
         EventBus.getInstance().fireEvent(e);
     }
 
     private void addContextMenu() {
         propertyMenu = new Menu();
         propertyMenu.add(buildAddPropertyMenuItem());
         propertyMenu.add(buildAddStaticTextMenuItem());
         propertyMenu.add(buildAddPropertyGroupMenuItem());
 
         groupMenu = new Menu();
         groupMenu.add(buildAddPropertyGroupMenuItem());
 
         tree.addListener(Events.OnMouseDown, new TreePanelMouseDownEventListener());
         tree.setContextMenu(new Menu());
     }
 
     private MenuItem buildAddPropertyMenuItem() {
         @SuppressWarnings("unchecked")
         MenuItem addGroupItem = new MenuItem(I18N.DISPLAY.addWidget(),
                 createAddListener(TreeElementType.PARAMETER));
         addGroupItem.setId(ID_PROPERTY_MENU_ITEM);
 
         return addGroupItem;
     }
 
     private MenuItem buildAddStaticTextMenuItem() {
         @SuppressWarnings("unchecked")
         MenuItem addDescTextItem = new MenuItem(I18N.DISPLAY.addStaticText(),
                 createAddListener(TreeElementType.STATIC_TEXT));
         addDescTextItem.setId(ID_STATIC_TEXT_MENU_ITEM);
 
         return addDescTextItem;
     }
 
     private MenuItem buildAddPropertyGroupMenuItem() {
         @SuppressWarnings("unchecked")
         MenuItem addPropertyItem = new MenuItem(I18N.DISPLAY.addPanel(),
                 createAddListener(TreeElementType.GROUP));
         addPropertyItem.setId(ID_PROPERTY_GROUP_MENU_ITEM);
 
         return addPropertyItem;
     }
 
     /**
      * This method can create a SelectionListener<MenuEvent> or a SelectionListener<ButtonEvent>. An
      * alternative would be to have two versions of this method, one for MenuEvent and one for
      * ButtonEvent, which would not require SuppressWarnings but create redundant code. There may be a
      * better solution, but I couldn't think of one :-(
      * 
      * @param <E>
      * @param treeElementType
      * @return
      */
     @SuppressWarnings("rawtypes")
     private <E extends ComponentEvent> SelectionListener createAddListener(
             final TreeElementType treeElementType) {
         return new SelectionListener<E>() {
             @Override
             public void componentSelected(E ce) {
                 addChild(treeElementType);
             }
         };
     }
 
     private void addChild(final TreeElementType treeElementType) {
         EnumerationServices services = new EnumerationServices();
         services.getUUID(new AsyncCallback<String>() {
             @Override
             public void onSuccess(String newId) {
                 doAdd(treeElementType, newId);
             }
 
             @Override
             public void onFailure(Throwable arg0) {
                 ErrorHandler.post(I18N.DISPLAY.cantGenerateUuid(), arg0);
             }
         });
     }
 
     /**
      * Inserts a new element of a given TreeElementType into the tree and fires a NavigationTreeAddEvent
      * to update the PropertyGroupContainer.
      * 
      * @param treeElementType
      * @param newId the ID to set on the new tree element
      */
     private void doAdd(TreeElementType treeElementType, String newId) {
         MetaDataTreeModel selectedItem = tree.getSelectionModel().getSelectedItem();
 
         if (treeElementType == TreeElementType.GROUP) {
             // always add new groups under the root
             selectedItem = tree.getStore().getRootItems().get(0);
         }
         else {
             // items can be added when a data object, parameter, or static text is selected; add them to the
             // parent of the selected item if this is the case
             switch (selectedItem.getType()) {
                 case PARAMETER:
                 case STATIC_TEXT:
                     selectedItem = (MetaDataTreeModel)selectedItem.getParent();
                     break;
             }
         }
 
         JSONMetaDataObject object = null;
         String title = getDefaultTitle(treeElementType);
 
         switch (treeElementType) {
             case GROUP:
                 object = new PropertyGroup();
                 break;
 
             case PARAMETER:
                 object = new Property();
                 object.setType(DEFAULT_TYPE_PROPERTY);
                 break;
 
             case STATIC_TEXT:
                 object = new Property();
                 object.setType(PropertyUtil.TYPE_STATIC_TEXT);
                 break;
         }
 
         if (object == null) {
             ErrorHandler.post(new Exception("Could not add type: " + treeElementType.toString()));
             return;
         }
 
         object.setId(newId);
         object.setLabel(title);
 
         MetaDataTreeModel newElement = new MetaDataTreeModel(treeElementType, object);
         newElement.setParent(selectedItem);
 
         tree.getStore().add(selectedItem, newElement, true);
         tree.setExpanded(selectedItem, true);
         tree.getSelectionModel().select(newElement, false);
 
         NavigationTreeAddEvent e = new NavigationTreeAddEvent(selectedItem, object);
         EventBus.getInstance().fireEvent(e);
 
         updateButtonOptions();
     }
 
     /**
      * Returns a string of the form I18N.DISPLAY.newPropertyLabel(i) that doesn't exist yet (or
      * I18N.DISPLAY.newPropertyLabel(i) if treeElementType=TreeElementType.GROUP)
      * 
      * @param treeElementType
      * @return
      */
     private String getDefaultTitle(TreeElementType treeElementType) {
         String label = ""; //$NON-NLS-1$
 
         int i = 0;
         boolean labelExists;
         do {
             i++;
             labelExists = false;
 
             switch (treeElementType) {
                 case STATIC_TEXT:
                     label = I18N.DISPLAY.newStaticTextLabel(i);
                     break;
                 case GROUP:
                     label = I18N.DISPLAY.newPropertyGroupLabel(i);
                     break;
                 case PARAMETER:
                 default:
                     label = I18N.DISPLAY.newPropertyLabel(i);
                     break;
             }
 
             // could be made more efficient
             for (MetaDataTreeModel node : tree.getStore().getAllItems()) {
                 if (node.getType() == treeElementType && label.equals(node.getLabel())) {
                     labelExists = true;
                     break;
                 }
             }
         } while (labelExists);
 
         return label;
     }
 
     /**
      * Returns the MetaDataTreeModel that matches a given ID, or null if no such model exists.
      * 
      * @param id
      * @return
      */
     private MetaDataTreeModel findTreeModelById(String id) {
         MetaDataTreeModel ret = null; // assume failure
 
         List<MetaDataTreeModel> allNodes = tree.getStore().getAllItems();
 
         for (MetaDataTreeModel node : allNodes) {
             if (id.equals(node.getId())) {
                 ret = node;
                 break;
             }
         }
 
         return ret;
     }
 
     private void compose() {
         setSize(200, 500);
         setScrollMode(Scroll.AUTO);
         add(tree);
     }
 
     private void initTree() {
         TreeStore<MetaDataTreeModel> store = PropertyGroupContainerUtil.buildStore(container);
 
         tree = new TreePanel<MetaDataTreeModel>(store);
         tree.setDisplayProperty(MetaDataTreeModel.LABEL_PROPERTY);
         tree.setIconProvider(new ModelIconProvider<MetaDataTreeModel>() {
             @Override
             public AbstractImagePrototype getIcon(MetaDataTreeModel model) {
                 if (model.getType() == TreeElementType.GROUP
                         || model.getType() == TreeElementType.CONTAINER) {
                     return (tree.isExpanded(model)) ? tree.getStyle().getNodeOpenIcon() : tree
                             .getStyle().getNodeCloseIcon();
                 }
 
                 return null;
             }
         });
 
         tree.addListener(Events.Render, new Listener<BaseEvent>() {
             @Override
             public void handleEvent(BaseEvent be) {
                 tree.expandAll();
                 tree.getSelectionModel().select(false, tree.getStore().getRootItems().get(0));
                 addListeners();
             }
         });
 
         tree.setLabelProvider(new ModelStringProvider<MetaDataTreeModel>() {
             @Override
             public String getStringValue(MetaDataTreeModel model, String property) {
                 String label = model.getLabel();
                 if (label == null || label.isEmpty()) {
                     label = I18N.DISPLAY.noTreeLabel();
                 }
                 return Format.ellipse(label, 25);
             }
         });
 
         @SuppressWarnings("unused")
         TreePanelDragSource source = new TreePanelDragSourceImpl(tree);
 
         TreePanelDropTarget target = new TreePanelDropTargetImpl(tree);
         target.setAllowSelfAsSource(true);
         target.setAllowDropOnLeaf(true);
         target.setFeedback(Feedback.BOTH);
         target.setScrollElementId(getId());
     }
 
     private class TreePanelMouseDownEventListener implements Listener<TreePanelEvent<MetaDataTreeModel>> {
         @Override
         public void handleEvent(TreePanelEvent<MetaDataTreeModel> be) {
             updateButtonOptions();
         }
     }
 
     private void updateButtonOptions() {
         MetaDataTreeModel selectedItem = tree.getSelectionModel().getSelectedItem();
 
         if (selectedItem != null) {
             switch (selectedItem.getType()) {
                 case GROUP:
                 case PARAMETER:
                 case STATIC_TEXT:
                     tree.setContextMenu(propertyMenu);
 
                     // enable these menu items
                     delete.enable();
                     addMenu.getItemByItemId(ID_PROPERTY_MENU_ITEM).enable();
                     addMenu.getItemByItemId(ID_STATIC_TEXT_MENU_ITEM).enable();
 
                     break;
 
                 case CONTAINER:
                     tree.setContextMenu(groupMenu);
 
                     // disable these menu items
                     delete.disable();
                     addMenu.getItemByItemId(ID_PROPERTY_MENU_ITEM).disable();
                     addMenu.getItemByItemId(ID_STATIC_TEXT_MENU_ITEM).disable();
 
                     break;
 
             }
         }
     }
 
     private class DeleteButtonSelectionListenerImpl extends SelectionListener<ButtonEvent> {
         @Override
         public void componentSelected(ButtonEvent ce) {
             List<MetaDataTreeModel> models = tree.getSelectionModel().getSelectedItems();
             for (MetaDataTreeModel model: models) {
                 MetaDataTreeModel parent = (MetaDataTreeModel)model.getParent();
                 if (parent != null) {
                     tree.getStore().remove(model);
                     tree.getSelectionModel().select(false, parent);
     
                     NavigationTreeDeleteEvent event = new NavigationTreeDeleteEvent(model, parent,
                             NavigationTreePanel.this);
                     EventBus.getInstance().fireEvent(event);
                 }
             }
         }
     }
 
     /**
      * Updates the label on the root node when a TemplateNameChangeEvent is received
      */
     private class TemplateNameChangeEventHandlerImpl implements TemplateNameChangeEventHandler {
         @Override
         public void onSelectionChange(TemplateNameChangeEvent event) {
             MetaDataTreeModel root = tree.getStore().getRootItems().get(0);
             root.setLabel(event.getNewValue());
             tree.getStore().update(root);
         }
     }
 
     private class JSONMetaDataObjectChangedEventHandlerImpl implements JSONMetaDataObjectChangedEventHandler {
         @Override
         public void onFire(JSONMetaDataObjectChangedEvent event) {
             JSONMetaDataObject metaDataObject = event.getJSONMetaDataObject();
             MetaDataTreeModel sheetModel = findTreeModelById(metaDataObject.getId());
 
             if (sheetModel != null) {
                 sheetModel.setObject(metaDataObject);
                 tree.getStore().update(sheetModel);
             }
         }
     }
 
     /**
      * An implementation of a TreePanelDragSource that only allows Properties to be dragged.
      * 
      * @author psarando
      */
     private class TreePanelDragSourceImpl extends TreePanelDragSource {
         public TreePanelDragSourceImpl(TreePanel<MetaDataTreeModel> tree) {
             super(tree);
         }
 
         /**
          * {@inheritDoc}
          * 
          * The event is canceled if the selected item is not a Property or Property Group.
          */
         @Override
         public void onDragStart(DNDEvent e) {
             for (ModelData selectedItem : tree.getSelectionModel().getSelectedItems()) {
                 MetaDataTreeModel selected = (MetaDataTreeModel)selectedItem;
 
                 JSONMetaDataObject source = selected.get(MetaDataTreeModel.OBJECT_PROPERTY);
                 if (!PropertyGroupContainerUtil.isProperty(source)
                         && !PropertyGroupContainerUtil.isPropertyGroup(source)) {
                     e.setCancelled(true);
                     e.getStatus().setStatus(false);
 
                     return;
                 }
             }
 
             super.onDragStart(e);
         }
     }
 
     /**
      * An implementation of a TreePanelDropTarget that only allows Properties to be dropped in Property
      * Groups.
      * 
      * @author psarando
      */
     private class TreePanelDropTargetImpl extends TreePanelDropTarget {
         private boolean skipHandleInsert = false;
 
         public TreePanelDropTargetImpl(TreePanel<MetaDataTreeModel> tree) {
             super(tree);
         }
 
         /**
          * {@inheritDoc}
          * 
          * The event is canceled if the target is not a Property Group.
          */
         @Override
         @SuppressWarnings("rawtypes")
         protected void handleAppend(DNDEvent event, final TreeNode overItem) {
             super.handleAppend(event, overItem);
 
             // super may have canceled the event
             if (event.isCancelled()) {
                 return;
             }
 
             // this method may be called from TreePanelDropTarget::handleInsert
             skipHandleInsert = true;
 
             // if the target is not a Property Group, don't allow a drop there
             JSONMetaDataObject target = null;
 
             if (overItem != null) {
                 // get our dest folder
                 MetaDataTreeModel targetModel = (MetaDataTreeModel)overItem.getModel();
                 target = targetModel.get(MetaDataTreeModel.OBJECT_PROPERTY);
             }
 
             if (!PropertyGroupContainerUtil.isPropertyGroup(target)) {
                 event.setCancelled(true);
                 event.getStatus().setStatus(false);
 
                 // this method might be called independently by TreePanelDropTarget::showFeedback
                 skipHandleInsert = false;
 
                 return;
             }
 
             // don't allow property groups to be dropped into other folders.
             for (ModelData selectedItem : tree.getSelectionModel().getSelectedItems()) {
                 MetaDataTreeModel selected = (MetaDataTreeModel)selectedItem;
 
                 JSONMetaDataObject source = selected.get(MetaDataTreeModel.OBJECT_PROPERTY);
 
                 if (PropertyGroupContainerUtil.isPropertyGroup(source)) {
                     event.setCancelled(true);
                     event.getStatus().setStatus(false);
                     skipHandleInsert = false;
                 }
             }
         }
 
         /**
          * {@inheritDoc}
          * 
          * The event is canceled if the insert target is not a Property.
          */
         @Override
         @SuppressWarnings("rawtypes")
         protected void handleInsert(DNDEvent event, final TreeNode overItem) {
             super.handleInsert(event, overItem);
 
             // super may have canceled the event or called handleAppend
             if (event.isCancelled() || skipHandleInsert) {
                 skipHandleInsert = false;
                 return;
             }
 
             // if the target is not a Property, don't allow an insert there
             JSONMetaDataObject target = null;
 
             if (overItem != null) {
                 // get our insert before/after target
                 MetaDataTreeModel targetModel = (MetaDataTreeModel)overItem.getModel();
                 target = targetModel.get(MetaDataTreeModel.OBJECT_PROPERTY);
             }
 
             // make sure each drag source item can be inserted before or after the target
             boolean dstIsProperty = PropertyGroupContainerUtil.isProperty(target);
             boolean dstIsPropertyGroup = PropertyGroupContainerUtil.isPropertyGroup(target);
 
             for (ModelData selectedItem : tree.getSelectionModel().getSelectedItems()) {
                 MetaDataTreeModel selected = (MetaDataTreeModel)selectedItem;
                 JSONMetaDataObject source = selected.get(MetaDataTreeModel.OBJECT_PROPERTY);
 
                 boolean srcIsProperty = PropertyGroupContainerUtil.isProperty(source);
                 boolean srcIsPropertyGroup = PropertyGroupContainerUtil.isPropertyGroup(source);
 
                 if ((srcIsProperty && !dstIsProperty) || (srcIsPropertyGroup && !dstIsPropertyGroup)) {
                     event.setCancelled(true);
                     event.getStatus().setStatus(false);
                     return;
                 }
             }
         }
 
         /**
          * {@inheritDoc}
          * 
          * Moves the underlying Properties of the appended models to the underlying Property Group of the
          * given ModelData parent.
          */
         @Override
         protected void appendModel(ModelData parent, List<ModelData> models, int index) {
             if (parent == null || models == null || models.size() == 0) {
                 return;
             }
 
             MetaDataTreeModel destParent = (MetaDataTreeModel)parent;
             PropertyGroupContainer destPropertyGroupContainer = (PropertyGroupContainer)destParent
                     .get(MetaDataTreeModel.OBJECT_PROPERTY);
 
             ArrayList<JSONMetaDataObject> propertyCollection = new ArrayList<JSONMetaDataObject>();
             ArrayList<PropertyGroup> propertyGroupCollection = new ArrayList<PropertyGroup>();
             for (ModelData treeModel : models) {
                 MetaDataTreeModel childModel = treeModel.get("model"); //$NON-NLS-1$
                 JSONMetaDataObject jsonmetaChild = (JSONMetaDataObject)childModel
                         .get(MetaDataTreeModel.OBJECT_PROPERTY);
 
                 MetaDataTreeModel srcParent = (MetaDataTreeModel)childModel.getParent();
                 JSONMetaDataObject jsonmetaParent = (JSONMetaDataObject)srcParent
                         .get(MetaDataTreeModel.OBJECT_PROPERTY);
 
                 srcParent.remove(childModel);
                 childModel.setParent(destParent);
 
                 if (PropertyGroupContainerUtil.isPropertyGroup(jsonmetaChild)) {
                     PropertyGroup childGroup = (PropertyGroup)jsonmetaChild;
                     PropertyGroupContainer srcContainer = (PropertyGroupContainer)jsonmetaParent;
 
                     srcContainer.remove(childGroup);
                     propertyGroupCollection.add(childGroup);
                 } else if (PropertyGroupContainerUtil.isProperty(jsonmetaChild)
                         && PropertyGroupContainerUtil.isPropertyGroup(jsonmetaParent)) {
                     Property childProperty = (Property)jsonmetaChild;
                     PropertyGroup srcGroup = (PropertyGroup)jsonmetaParent;
 
                     srcGroup.remove(childProperty);
                     propertyCollection.add(childProperty);
                 }
             }
 
             if (propertyGroupCollection.size() > 0) {
                 destPropertyGroupContainer.insertGroups(index, propertyGroupCollection);
             } else if (propertyCollection.size() > 0
                     && PropertyGroupContainerUtil.isPropertyGroup(destPropertyGroupContainer)) {
                 PropertyGroup destPropertyGroup = (PropertyGroup)destPropertyGroupContainer;
                 destPropertyGroup.insertProperties(index, propertyCollection);
             }
 
             super.appendModel(parent, models, index);
         }
     }
 
     public PropertyGroupContainer getPropertyGroupContainer() {
         return container;
     }
 }
