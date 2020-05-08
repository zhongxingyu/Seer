 package com.duggan.workflow.client.ui.admin.formbuilder.component;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.allen_sauer.gwt.dnd.client.HasDragHandle;
 import com.duggan.workflow.client.service.TaskServiceCallback;
 import com.duggan.workflow.client.ui.AppManager;
 import com.duggan.workflow.client.ui.admin.formbuilder.HasProperties;
 import com.duggan.workflow.client.ui.events.PropertyChangedEvent;
 import com.duggan.workflow.client.ui.events.PropertyChangedEvent.PropertyChangedHandler;
 import com.duggan.workflow.client.ui.events.ResetFormPositionEvent;
 import com.duggan.workflow.client.ui.events.ResetFormPositionEvent.ResetFormPositionHandler;
 import com.duggan.workflow.client.ui.events.SavePropertiesEvent;
 import com.duggan.workflow.client.ui.events.SavePropertiesEvent.SavePropertiesHandler;
 import com.duggan.workflow.client.util.AppContext;
 import com.duggan.workflow.shared.model.DataType;
 import com.duggan.workflow.shared.model.StringValue;
 import com.duggan.workflow.shared.model.Value;
 import com.duggan.workflow.shared.model.form.Field;
 import com.duggan.workflow.shared.model.form.FormModel;
 import com.duggan.workflow.shared.model.form.KeyValuePair;
 import com.duggan.workflow.shared.model.form.Property;
 import com.duggan.workflow.shared.requests.CreateFieldRequest;
 import com.duggan.workflow.shared.requests.DeleteFormModelRequest;
 import com.duggan.workflow.shared.responses.CreateFieldResponse;
 import com.duggan.workflow.shared.responses.DeleteFormModelResponse;
 import com.google.gwt.dom.client.Style.Position;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.shared.EventHandler;
 import com.google.gwt.event.shared.GwtEvent;
 import com.google.gwt.event.shared.GwtEvent.Type;
 import com.google.gwt.event.shared.HandlerRegistration;
 import com.google.gwt.user.client.ui.AbsolutePanel;
 import com.google.gwt.user.client.ui.FocusPanel;
 import com.google.gwt.user.client.ui.InlineLabel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 public abstract class FieldWidget extends AbsolutePanel implements
 		HasDragHandle, PropertyChangedHandler, HasProperties,
 		SavePropertiesHandler, ResetFormPositionHandler {
 
 	private FocusPanel shim = new FocusPanel();
 	protected long id = System.currentTimeMillis();
 	protected Map<String, Property> props = new LinkedHashMap<String, Property>();
 	
 	Field field = new Field();
 	protected boolean showShim = true;
 	protected boolean readOnly=false;
 	protected boolean popUpActivated = false;
 	List<HandlerRegistration> handlers = new ArrayList<HandlerRegistration>();
 	
 	public FieldWidget() {
 		shim.addStyleName("demo-PaletteWidget-shim");
 		defaultProperties();
 		initField();
 	}
 	
 	public void defaultProperties(){
 		addProperty(new Property(NAME, "Name", DataType.STRING, id));
 		addProperty(new Property(CAPTION, "Label Text", DataType.STRING, id));
 		addProperty(new Property(HELP, "Help", DataType.STRING, id));
 	}
 
 	private void initField() {
 		field.setCaption(getPropertyValue(CAPTION));
 		field.setName(getPropertyValue(NAME));
 		field.setType(DataType.STRING);
 		field.setValue(null);
 		field.setType(getType());
 	}
 	
 
 	protected void afterInit() {
 		
 	}
 
 	public void addProperty(Property property) {
 		assert props != null;
 		assert property != null;
 		assert property.getName() != null;
 
 		
 		if(property.getType().isDropdown()){
 			Property previous=props.get(property.getName());
 			//need to copy lookup values
 			if(previous!=null){
 				property.setSelectionValues(previous.getSelectionValues());
 			}
 		}
 		
 		props.put(property.getName(), property);
 	}
 
 	public abstract FieldWidget cloneWidget();
 
 	public void activatePopup() {
 	}
 	
 	public void activateShimHandler(){
 		shim.addClickHandler(new ClickHandler() {
 
 			@Override
 			public void onClick(ClickEvent event) {
 				int arrowPosition = shim.getAbsoluteTop() - 30;
 				showProperties(arrowPosition);
 			}
 		});
 	}
 
 	public void showProperties(int arrowPosition) {
 
 		/* 
 		 * Position of the pop-over 
 		 */
 		int top = 7;
 		int left = 75;
 		//System.err.println("Field ="+field+"\n Properties = "+getProperties());
 		AppManager.showPropertyPanel(field, getProperties(), top, left,
 				arrowPosition);
 	}
 
 	public Field getField() {
 		field.setType(getType());
 		return field;
 	}
 
 	public String getPropertyValue(String key) {
 
 		Property property = props.get(key);
 
 		if (property == null)
 			return null;
 
 		Value value = property.getValue();
 		if (value == null)
 			return null;
 
 		return value.getValue() == null ? null : value.getValue().toString();
 	}
 
 	@Override
 	public Object getValue(String propertyName) {
 		Property property = props.get(propertyName);
 
 		if (property == null)
 			return null;
 
 		Value value = property.getValue();
 		if (value == null)
 			return null;
 
 		return value.getValue();
 	}
 
 	/**
 	 * Overriden by children to provide value
 	 */
 	@Override
 	public Value getFieldValue() {
 		return null;
 	}
 
 	@Override
 	public Widget getDragHandle() {
 		return shim;
 	}
 
 	/**
 	 * Let shim size match our size.
 	 * 
 	 * @param width
 	 *            the desired pixel width
 	 * @param height
 	 *            the desired pixel height
 	 */
 	@Override
 	public void setPixelSize(int width, int height) {
 		super.setPixelSize(width, height);
 		shim.setPixelSize(width, height);
 	}
 
 	/**
 	 * Let shim size match our size.
 	 * 
 	 * @param width
 	 *            the desired CSS width
 	 * @param height
 	 *            the desired CSS height
 	 */
 	@Override
 	public void setSize(String width, String height) {
 		super.setSize(width, height);
 		shim.setSize(width, height);
 	}
 
 	/**
 	 * Adjust the shim size and attach once our widget dimensions are known.
 	 */
 	@Override
 	protected void onLoad() {
 		super.onLoad();
 		initShim();	
 		addRegisteredHandler(SavePropertiesEvent.TYPE, this);
 		
 		if (!popUpActivated) {
 			popUpActivated = true;
 			addRegisteredHandler(ResetFormPositionEvent.TYPE, this);
 			activateShimHandler();
 		}
 		//register default events
 		
 	}
 	
 	/**
 	 * Remove the shim to allow the widget to size itself when reattached.
 	 */
 	@Override
 	protected void onUnload() {
 		super.onUnload();
 		shim.removeFromParent();		
 		cleanUpEvents();
 	}
 	
 	/**
 	 * 
 	 * @param type
 	 * @param handler
 	 */
 	public void addRegisteredHandler(Type<? extends EventHandler> type, FieldWidget handler){
 		@SuppressWarnings("unchecked")
 		HandlerRegistration hr = AppContext.getEventBus().addHandler(
 				(GwtEvent.Type<EventHandler>)type, handler);
 		handlers.add(hr);
 	}
 	
 	/**
 	 * 
 	 */
 	private void cleanUpEvents() {
 		for(HandlerRegistration hr: handlers){
 			hr.removeHandler();
 		}
 		handlers.clear();
 	}
 	
 	private void initShim() {
 		if (!showShim){
 			return;
 		}
 		
 		int offSetWidth = getOffsetWidth();
 		int offSetHeight = getOffsetHeight();
 		
 		/**
 		 * Non-visible or detached elements have no offsetParent<br/>
 		 * AbsolutePanel.class - Line 265<br/>
 		 *   if (child.getElement().getOffsetParent() == null) {
 		 *     return;
 		 *   }
 		 *   
 		 *   The code below forces the shim to be displayed, even for components
 		 *   that may not have been visible during initialization
 		 */
 		if(offSetWidth==0){
 			offSetWidth = 433;
 		}
 		
 		if(offSetHeight==0){
 			offSetHeight = 50;
 		}
 		 
 		
 		shim.setPixelSize(offSetWidth, offSetHeight);
 		
 		getElement().getStyle().setPosition(Position.RELATIVE);
 
 		// should we do this only if this is not a property field?
 		
 		addShim(0,0,offSetWidth, offSetHeight);
 
 	}
 
 	public void addShim(int left, int top, int offSetWidth, int offSetHeight){
 		add(shim, left, top);
 	}
 
 	public List<Property> getProperties() {
 		List<Property> values = new ArrayList<Property>();
 		for (Property prop : props.values()) {
 			values.add(prop);
 		}
 
 		return values;
 	}
 
 	@Override
 	public void onPropertyChanged(PropertyChangedEvent event) {
 		if (!event.isForField()) {
 			return;
 		}
 
 		assert event.getComponentId()!=null;
 		if (this.id != event.getComponentId()) {
 			return;
 		}
 
 		String property = event.getPropertyName();
 		Object value = event.getPropertyValue();
 
 		if (property.equals(CAPTION))
 			setCaption(value == null ? null : value.toString());
 
 		if (property.equals(PLACEHOLDER))
 			setPlaceHolder(value == null ? null : value.toString());
 
 		if (property.equals(HELP))
 			setHelp(value == null ? null : value.toString());
 		
 		if(property.equals(SELECTIONTYPE) && (this instanceof IsSelectionField)){
 			
 			IsSelectionField fld = (IsSelectionField)this;
 			fld.setSelectionValues((List<KeyValuePair>)event.getPropertyValue());
 		}
 
 	}
 
 	public void setReadOnly(boolean readOnly) {
 
 	}
 
 	protected void setCaption(String caption) {
 	};
 
 	protected void setPlaceHolder(String placeHolder) {
 	}
 
 	protected void setHelp(String help) {
 	}
 
 	protected abstract DataType getType();
 
 	public void setValue(Object value) {
 
 	}
 
 	public void setFormId(Long formId) {
 		field.setFormId(formId);
 	}
 
 	private void save(Field model) {
 
 		model.setType(getType());
 		model.setProperties(getProperties());
 		
//		System.err.println("Save Props>>>> "+model+" :: "+model.getProperties()
//				+" \n Fields = "+model.getFields());
 
 		model.setName(getPropertyValue(NAME));
 		model.setCaption(getPropertyValue(CAPTION));
 	
 		AppContext.getDispatcher().execute(new CreateFieldRequest(model),
 				new TaskServiceCallback<CreateFieldResponse>() {
 					@Override
 					public void processResult(CreateFieldResponse result) {
 						Field savedfield = result.getField();
 						savedfield.sortFields();
 						setField(savedfield);
 						onAfterSave();
 					}
 				});
 	}
 	
 	public void onAfterSave() {
 		
 	}
 
 	public void setField(Field field) {
 		this.field = field;
 		
 		if(field.getId()!=null)
 			this.id = field.getId();
 		
 		//reset all fieldids in previous properties
 		for(Property prop: props.values()){
 			prop.setFieldId(id);
 		}
 		
 		setProperties(field.getProperties());
 
 		String caption = getPropertyValue(CAPTION);
 		if (caption != null && !caption.isEmpty()){
 			setCaption(caption);
 		}else{
 		
 			//field widgets created from FieldModels - see GridColumn
 			Property prop = props.get(CAPTION);
 			if(prop!=null){
 				prop.setValue(new StringValue(field.getCaption()));
 			}
 		}
 		
 		//set place holder
 		String placeHolder = getPropertyValue(PLACEHOLDER);
 		if (placeHolder != null && !placeHolder.isEmpty()){
 			setPlaceHolder(placeHolder);
 		}
 
 		//set help
 		String help = getPropertyValue(HELP);
 		if (help != null) {
 			setHelp(help);
 		}
 
 		//Set Read only
 		boolean readOnly = false;
 		Object val = getValue(READONLY);
 		if (val != null && val instanceof Boolean) {
 			readOnly = (Boolean) val;
 		}
 		setReadOnly(readOnly);
 
 		String alignment = getPropertyValue(ALIGNMENT);
 		if(alignment!=null){
 			setAlignment(alignment);
 		}
 		//set dropdown choices
 		if(this instanceof IsSelectionField){
 			((IsSelectionField)this).setSelectionValues(field.getSelectionValues());
 		}
 
 		//Set Value
 		Value value = field.getValue();
 		if (value != null) {
 			setValue(value.getValue());
 		}else{
 			setValue(null);
 		}
 
 	}
 
 	protected void setAlignment(String alignment) {
 		
 	}
 
 	private void setProperties(List<Property> properties) {
 		if (properties != null) {
 			for (Property prop : properties) {
 				addProperty(prop);
 			}
 		}
 	}
 
 	@Override
 	public void onSaveProperties(SavePropertiesEvent event) {
 		FormModel model = event.getParent();
 
 		if (model == null) {
 			return;
 		}
 
 		if (model.equals(field)) {
 			save((Field) model);
 		}
 	}
 
 	public void save() {
 		save(field);
 	}
 
 	/**
 	 * This allows visual properties including Caption, Place Holder, help to be
 	 * Synched with the form field, so that the changes are observed immediately
 	 * 
 	 * All other Properties need not be synched this way
 	 * 
 	 * @param property
 	 * @param value
 	 *            Boolean, Double
 	 */
 	protected void firePropertyChanged(Property property, Object value) {
 		boolean isForField = property.getFieldId() != null;
 
 		Long componentId = property.getFieldId() != null ? property
 				.getFieldId() : property.getFormId();
 
 		AppContext.getEventBus().fireEventFromSource(
 				new PropertyChangedEvent(componentId, property.getName(),
 						value, isForField), this);
 	}
 
 	public static FieldWidget getWidget(DataType type, Field fld,
 			boolean activatePopup) {
 		FieldWidget widget = null;
 
 		switch (type) {
 		case BOOLEAN:
 			widget = new InlineRadio();
 			break;
 
 		case DATE:
 			widget = new DateField();
 			break;
 
 		case DOUBLE:
 			widget = new TextField();
 			break;
 
 		case INTEGER:
 			widget = new TextField();
 			break;
 
 		case STRING:
 			widget = new TextField();
 			break;
 
 		case STRINGLONG:
 			widget = new TextArea();
 			break;
 
 		case BUTTON:
 			widget = new SingleButton();
 			break;
 
 		case CHECKBOX:
 			widget = new InlineCheckBox();
 			break;
 
 		case MULTIBUTTON:
 			widget = new MultipleButton();
 			break;
 
 		case SELECTBASIC:
 			widget = new SelectBasic();
 			break;
 
 		case SELECTMULTIPLE:
 			widget = new SelectMultiple();
 			break;
 
 		case LABEL:
 			widget = new LabelField();
 			break;
 			
 		case LAYOUTHR:
 			widget= new HR();
 			break;
 			
 		case GRID:
 			widget = new GridLayout();
 			/*widget = new GridField();*/
 			break;
 
 		}
 
 		widget.showShim = activatePopup;
 		widget.setField(fld);
 		
 		if(activatePopup)
 			widget.activatePopup();
 		
 		widget.afterInit();
 		
 		return widget;
 	}
 
 	public void delete() {
 		if (field.getId() != null) {
 			AppContext.getDispatcher().execute(
 					new DeleteFormModelRequest(field),
 					new TaskServiceCallback<DeleteFormModelResponse>() {
 						@Override
 						public void processResult(DeleteFormModelResponse result) {
 						}
 					});
 		}
 	}
 
 	@Override
 	public void onResetFormPosition(ResetFormPositionEvent event) {
 		if (event.getInsertPosition() >= field.getPosition()) {
 			if (getParent() != null && getParent() instanceof VerticalPanel) {
 				VerticalPanel panel = (VerticalPanel) getParent();
 				int idx = panel.getWidgetIndex(this);
 
 				if (idx == -1) {
 					return;
 				} else {
 
 					int previousPosition = field.getPosition();
 					// System.err.println("B4 :: "+field +"[Idx="+idx+"]," +
 					// " [ Previous Pos="+previousPosition+"])");
 					//
 					if (idx == previousPosition) {
 						return;
 					}
 
 					field.setPosition(idx);
 					event.addCount();
 				}
 			}
 		}
 	}
 
 	/**
 	 * Get Property Field
 	 * @param property
 	 * @return
 	 */
 	public static FieldWidget getWidget(Property property) {
 
 		FieldWidget widget = null;
 		
 		switch (property.getType()) {
 		case BOOLEAN:
 			widget = new InlineCheckBox();
 			break;
 
 		case DATE:
 			widget = new DateField(property);
 			break;
 
 		case DOUBLE:
 			widget = new TextField(property);
 			break;
 
 		case INTEGER:
 			widget = new TextField(property);
 			break;
 
 		case STRING:
 			widget = new TextField(property);
 			break;
 
 		case STRINGLONG:
 			widget = new TextArea(property);
 			break;
 
 		case CHECKBOX:
 			widget = new InlineCheckBox(property);
 			break;
 
 		case LABEL:
 			widget = new LabelField();
 			break;
 			
 		case SELECTBASIC:
 			widget = new SelectBasic(property);
 			break;
 			
 		}
 		
 		
 
 		// System.err.println(">>>"+property.getType()+" :: "+property.getCaption());
 
 		assert widget != null;
 
 		if (widget != null) {
 			widget.showShim = false;
 		}
 
 		return widget;
 	}
 
 	public boolean isReadOnly() {
 
 		Object readOnly = getValue(READONLY);
 
 		if (readOnly == null)
 			return false;
 
 		return (Boolean) readOnly;
 	}
 
 	public boolean isMandatory() {
 		Object isMandatory = getValue(MANDATORY);
 
 		if (isMandatory == null)
 			return false;
 
 		return (Boolean) isMandatory;
 	}
 	
 	public FocusPanel getShim() {
 		return shim;
 	}
 	
 	public Widget getComponent(boolean small) {
 		return null;
 	}
 }
