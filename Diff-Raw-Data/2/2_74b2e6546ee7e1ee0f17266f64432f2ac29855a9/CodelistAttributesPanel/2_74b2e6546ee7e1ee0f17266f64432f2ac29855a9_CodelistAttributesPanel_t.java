 package org.cotrix.web.manage.client.codelist;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.cotrix.web.common.client.feature.FeatureBinder;
 import org.cotrix.web.common.client.feature.FeatureToggler;
 import org.cotrix.web.common.client.widgets.HasEditing;
 import org.cotrix.web.common.client.widgets.ItemToolbar;
 import org.cotrix.web.common.client.widgets.ItemToolbar.ButtonClickedEvent;
 import org.cotrix.web.common.client.widgets.ItemToolbar.ButtonClickedHandler;
 import org.cotrix.web.common.client.widgets.ItemToolbar.ItemButton;
 import org.cotrix.web.common.shared.codelist.UIAttribute;
 import org.cotrix.web.common.shared.codelist.UICode;
 import org.cotrix.web.manage.client.codelist.attribute.AttributePanel;
 import org.cotrix.web.manage.client.codelist.attribute.CodeAttributeEditingPanelFactory;
 import org.cotrix.web.manage.client.codelist.attribute.GroupFactory;
 import org.cotrix.web.manage.client.codelist.attribute.RemoveItemController;
 import org.cotrix.web.manage.client.codelist.common.ItemsEditingPanel;
 import org.cotrix.web.manage.client.codelist.common.ItemsEditingPanel.ItemsEditingListener;
 import org.cotrix.web.manage.client.codelist.common.ItemsEditingPanel.ItemsEditingListener.SwitchState;
 import org.cotrix.web.manage.client.codelist.event.CodeSelectedEvent;
 import org.cotrix.web.manage.client.codelist.event.CodeUpdatedEvent;
 import org.cotrix.web.manage.client.codelist.event.GroupSwitchType;
 import org.cotrix.web.manage.client.codelist.event.GroupSwitchedEvent;
 import org.cotrix.web.manage.client.codelist.event.SwitchGroupEvent;
 import org.cotrix.web.manage.client.data.CodeAttribute;
 import org.cotrix.web.manage.client.data.DataEditor;
 import org.cotrix.web.manage.client.data.event.DataEditEvent;
 import org.cotrix.web.manage.client.data.event.DataEditEvent.DataEditHandler;
 import org.cotrix.web.manage.client.di.CurrentCodelist;
 import org.cotrix.web.manage.client.event.EditorBus;
 import org.cotrix.web.manage.client.resources.CotrixManagerResources;
 import org.cotrix.web.manage.client.util.Attributes;
 import org.cotrix.web.manage.shared.AttributeGroup;
 import org.cotrix.web.manage.shared.Group;
 import org.cotrix.web.manage.shared.ManagerUIFeature;
 
 import com.allen_sauer.gwt.log.client.Log;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
 import com.google.gwt.safehtml.shared.SafeHtmlUtils;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.ui.ResizeComposite;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.gwt.view.client.SelectionChangeEvent;
 import com.google.inject.Inject;
 import com.google.web.bindery.event.shared.EventBus;
 import com.google.web.bindery.event.shared.binder.EventBinder;
 import com.google.web.bindery.event.shared.binder.EventHandler;
 
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 public class CodelistAttributesPanel extends ResizeComposite implements HasEditing {
 
 	interface Binder extends UiBinder<Widget, CodelistAttributesPanel> {}
 	interface CodelistAttributesPanelEventBinder extends EventBinder<CodelistAttributesPanel> {}
 
 	@UiField(provided = true)
 	ItemsEditingPanel<UIAttribute, AttributePanel> attributesGrid;
 
 	@UiField
 	ItemToolbar toolBar;
 
 	private Set<AttributeGroup> groupsAsColumn = new HashSet<AttributeGroup>();
 
 	@Inject @EditorBus
 	private EventBus editorBus;
 
 	private UICode visualizedCode;
 
 	private DataEditor<CodeAttribute> attributeEditor;
 
 	@Inject
 	private CotrixManagerResources resources;
 
 	@Inject
 	private RemoveItemController attributeController;
 	
 	@Inject
 	private CodeAttributeEditingPanelFactory editingPanelFactory;
 
 	@Inject
 	public void init() {
 
 		this.attributeEditor = DataEditor.build(this);
 
 		attributesGrid = new ItemsEditingPanel<UIAttribute, AttributePanel>("Attributes", "No attributes", editingPanelFactory);
 
 		// Create the UiBinder.
 		Binder uiBinder = GWT.create(Binder.class);
 		initWidget(uiBinder.createAndBindUi(this));
 		
 		attributesGrid.setListener(new ItemsEditingListener<UIAttribute>() {
 			
 			@Override
 			public void onUpdate(UIAttribute item) {
 				Log.trace("updated attribute "+item);
 				attributeEditor.updated(new CodeAttribute(visualizedCode, item));
 			}
 			
 			@Override
 			public void onSwitch(UIAttribute item, SwitchState state) {
 				switchAttribute(item, state);
 			}
 			
 			@Override
 			public void onCreate(UIAttribute item) {
 				visualizedCode.addAttribute(item);
 				attributeEditor.added(new CodeAttribute(visualizedCode, item));
 			}
 		});
 
 		
 		toolBar.addButtonClickedHandler(new ButtonClickedHandler() {
 
 			@Override
 			public void onButtonClicked(ButtonClickedEvent event) {
 				switch (event.getButton()) {
 					case PLUS: addNewAttribute(); break;
 					case MINUS: removeSelectedAttribute(); break;
 				}
 			}
 		});
 
 		attributesGrid.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
 
 			@Override
 			public void onSelectionChange(SelectionChangeEvent event) {
 				selectedAttributeChanged();
 			}
 		});
 
 		updateBackground();
 	}
 
 	@Inject
 	protected void bind(@CurrentCodelist String codelistId)
 	{
 		FeatureBinder.bind(new FeatureToggler() {
 
 			@Override
 			public void toggleFeature(boolean active) {
 				toolBar.setVisible(ItemButton.PLUS, active);
 			}
 		}, codelistId, ManagerUIFeature.EDIT_CODELIST);
 
 		FeatureBinder.bind(new FeatureToggler() {
 
 			@Override
 			public void toggleFeature(boolean active) {
 				attributeController.setUserCanEdit(active);
 				//we animate only if the user obtain the edit permission
 				updateRemoveButtonVisibility(active);
 			}
 		}, codelistId, ManagerUIFeature.EDIT_CODELIST);
 		
 		editorBus.addHandler(DataEditEvent.getType(UICode.class), new DataEditHandler<UICode>() {
 
 			@Override
 			public void onDataEdit(DataEditEvent<UICode> event) {
 				if (visualizedCode!=null && visualizedCode.equals(event.getData())) {
 					switch (event.getEditType()) {
 						case UPDATE: updateVisualizedCode(event.getData()); break;
 						case REMOVE: clearVisualizedCode(); break;
 						default:
 					}
 				}
 			}
 		});
 
 		editorBus.addHandler(DataEditEvent.getType(CodeAttribute.class), new DataEditHandler<CodeAttribute>() {
 
 			@Override
 			public void onDataEdit(DataEditEvent<CodeAttribute> event) {
 				if (visualizedCode!=null && visualizedCode.equals(event.getData().getCode())) {
 					UIAttribute attribute = event.getData().getAttribute();
 					switch (event.getEditType()) {
 						case ADD: {
 							if (event.getSource() != CodelistAttributesPanel.this) {
 								attributesGrid.addItemPanel(attribute);
 							}
 						} break;
 						case UPDATE: attributesGrid.synchWithModel(attribute); break;
 						default:
 					}
 				}
 			}
 		});
 
 	}
 
 	@Inject
 	protected void bind(CodelistAttributesPanelEventBinder binder) {
 		binder.bindEventHandlers(this, editorBus);
 	}
 
 	@EventHandler
 	void onCodeSelected(CodeSelectedEvent event) {
 		updateVisualizedCode(event.getCode());
 	}
 
 	@EventHandler
 	void onCodeUpdated(CodeUpdatedEvent event) {
 		updateVisualizedCode(event.getCode());
 	}
 
 	@EventHandler
 	void onGroupSwitched(GroupSwitchedEvent event) {
 		Group group = event.getGroup();
 
 		if (group instanceof AttributeGroup) {
 			AttributeGroup attributeGroup = (AttributeGroup) group;
 			Log.trace("onAttributeSwitched group: "+attributeGroup+" type: "+event.getSwitchType());
 
 			updateGroups(attributeGroup, event.getSwitchType());
 			
 			if (visualizedCode!=null && attributeGroup.match(visualizedCode.getAttributes())!=null) refreshSwitches();
 		}
 	}
 
 	private void updateRemoveButtonVisibility(boolean animate) {
 		toolBar.setVisible(ItemButton.MINUS, attributeController.canRemove(), animate);
 	}
 
 
 	/** 
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void setVisible(boolean visible) {
 		super.setVisible(visible);
 		//GWT issue 7188 workaround
 		onResize();
 	}
 
 	private void addNewAttribute()
 	{
 		if (visualizedCode!=null) {
 			UIAttribute attribute = new UIAttribute();
 			attributesGrid.addNewItemPanel(attribute);
 		}
 	}
 
 	private void removeSelectedAttribute()
 	{
 		if (visualizedCode!=null && attributesGrid.getSelectedItem()!=null) {
 			UIAttribute selectedAttribute = attributesGrid.getSelectedItem();
 			if (Attributes.isSystemAttribute(selectedAttribute)) return; 
 			attributesGrid.removeItem(selectedAttribute);
 			visualizedCode.removeAttribute(selectedAttribute);
 			attributeEditor.removed(new CodeAttribute(visualizedCode, selectedAttribute));
 		}
 	}
 
 	private void selectedAttributeChanged()
 	{
 		if (visualizedCode!=null && attributesGrid.getSelectedItem()!=null) {
 			UIAttribute selectedAttribute = attributesGrid.getSelectedItem();
 			attributeController.setItemCanBeRemoved(!Attributes.isSystemAttribute(selectedAttribute));
 			updateRemoveButtonVisibility(false);
 		}
 	}
 
 	private void updateVisualizedCode(UICode code)
 	{
 		visualizedCode = code;
 		setHeader();
 		updateBackground();
 
 		attributesGrid.clear();
 		for (UIAttribute attribute:visualizedCode.getAttributes()) {
 			attributesGrid.addItemPanel(attribute);
 		}
 		
		refreshSwitches();
		
 		Log.trace("request refresh of "+visualizedCode.getAttributes().size()+" attributes");
 	}
 
 	private void clearVisualizedCode()
 	{
 		visualizedCode = null;
 		setHeader();
 		updateBackground();
 
 		attributesGrid.clear();
 	}
 
 	private void updateBackground()
 	{
 		setStyleName(CotrixManagerResources.INSTANCE.css().noItemsBackground(), visualizedCode == null || visualizedCode.getAttributes().isEmpty());
 	}
 
 	private void setHeader()
 	{
 		SafeHtmlBuilder sb = new SafeHtmlBuilder();
 		sb.appendHtmlConstant("<span>Attributes</span>");
 		if (visualizedCode!=null) {
 			sb.appendHtmlConstant("&nbsp;for&nbsp;<span class=\""+resources.css().headerCode()+"\">");
 			sb.append(SafeHtmlUtils.fromString(visualizedCode.getName().getLocalPart()));
 			sb.appendHtmlConstant("</span>");
 		}
 		
 		attributesGrid.setHeaderText(sb.toSafeHtml());
 	}
 
 	private void switchAttribute(UIAttribute attribute, SwitchState attributeSwitchState)
 	{
 		AttributeGroup group = GroupFactory.getGroup(attribute);
 		group.calculatePosition(visualizedCode.getAttributes(), attribute);
 
 		switch (attributeSwitchState) {
 			case UP: editorBus.fireEvent(new SwitchGroupEvent(group, GroupSwitchType.TO_NORMAL)); break;
 			case DOWN: editorBus.fireEvent(new SwitchGroupEvent(group, GroupSwitchType.TO_COLUMN)); break;
 		}
 	}
 	
 	private void updateGroups(AttributeGroup group, GroupSwitchType state) {
 		switch (state) {
 			case TO_NORMAL: groupsAsColumn.remove(group); break;
 			case TO_COLUMN: groupsAsColumn.add(group); break;
 		}
 	}
 	
 	private void refreshSwitches() {
 		Log.trace("refreshSwitches");
 		if (visualizedCode == null) return;
 		for (UIAttribute attribute:visualizedCode.getAttributes()) {
 			attributesGrid.setSwitchState(attribute, isInGroupAsColumn(attribute)?SwitchState.DOWN:SwitchState.UP);
 		}
 	}
 	
 	private boolean isInGroupAsColumn(UIAttribute attribute)
 	{
 		for (AttributeGroup group:groupsAsColumn) if (group.accept(visualizedCode.getAttributes(), attribute)) return true;
 		return false;
 	}
 
 	@Override
 	public void setEditable(boolean editable) {
 		attributesGrid.setEditable(editable);
 	}
 }
