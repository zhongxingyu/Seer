 package com.duggan.workflow.client.ui.admin.formbuilder.component;
 
 import java.util.List;
 
 import com.duggan.workflow.client.ui.component.DropDownList;
 import com.duggan.workflow.shared.model.DataType;
 import com.duggan.workflow.shared.model.StringValue;
 import com.duggan.workflow.shared.model.Value;
 import com.duggan.workflow.shared.model.form.KeyValuePair;
 import com.duggan.workflow.shared.model.form.Property;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.ui.HTMLPanel;
 import com.google.gwt.user.client.ui.InlineLabel;
 import com.google.gwt.user.client.ui.UIObject;
 import com.google.gwt.user.client.ui.Widget;
 
 public class SelectBasic extends FieldWidget implements IsSelectionField{
 
 	private static SelectBasicUiBinder uiBinder = GWT
 			.create(SelectBasicUiBinder.class);
 	
 	private final Widget widget;
 
 	@UiField Element lblEl;
 	@UiField DropDownList<KeyValuePair> lstItems;
 	@UiField HTMLPanel panelControls;
 	@UiField InlineLabel lblComponent;
 	
 	interface SelectBasicUiBinder extends UiBinder<Widget, SelectBasic> {
 	}
 
 	public SelectBasic() {
 		super();
 		addProperty(new Property(MANDATORY, "Mandatory", DataType.CHECKBOX, id));
 		addProperty(new Property(SELECTIONTYPE, "Reference", DataType.STRING));
 		widget= uiBinder.createAndBindUi(this);
 		add(widget);
 	}
 	
 	public SelectBasic(final Property property){
 		this();
 		lblEl.setInnerText(property.getCaption());
 		setSelectionValues(property.getSelectionValues());
 		Value val = property.getValue();
 		if(val!=null)
 			setValue(val.getValue());
 		
 		lstItems.addValueChangeHandler(new ValueChangeHandler<KeyValuePair>() {
 			
 			@Override
 			public void onValueChange(ValueChangeEvent<KeyValuePair> event) {
 				KeyValuePair p = event.getValue();
 				
 				Value value = new StringValue(null,property.getName(),p==null? null: p.getKey());
 				property.setValue(value);
 			}
 		});
 		
 	}
 
 	@Override
 	public FieldWidget cloneWidget() {
 		return new SelectBasic();
 	}
 	
 	@Override
 	protected void setCaption(String caption) {
 		lblEl.setInnerHTML(caption);
 	}
 	
 	@Override
 	protected void setPlaceHolder(String placeHolder) {
 		//txtComponent.setPlaceholder(placeHolder);
 	}
 	
 	@Override
 	protected void setHelp(String help) {
 		lstItems.setTitle(help);
 	}
 	
 	@Override
 	public Value getFieldValue() {
 		KeyValuePair kvp = lstItems.getValue();
 		
 		String value = null;
 		
 		if(kvp!=null){
 			value = kvp.getKey();
 		}
 		
 		if(value!=null){
 			return new StringValue(value);
 		}
 		
 		return null;
 	}
 	
 	@Override
 	protected DataType getType() {
 		return DataType.SELECTBASIC;
 	}
 	
 	@Override
 	public void setReadOnly(boolean isReadOnly) {
 		this.readOnly = isReadOnly;
 		UIObject.setVisible(lstItems.getElement(),!isReadOnly);
 		UIObject.setVisible(lblComponent.getElement(), isReadOnly);
 	}
 
 	@Override
 	public void setSelectionValues(List<KeyValuePair> values) {
 		
 		if(values!=null){
 			lstItems.setItems(values);
 		}
 		
 		//design mode values set here before save is called
 		field.setSelectionValues(values);
 	}
 
 	@Override
 	public List<KeyValuePair> getValues() {
 		return lstItems.values();
 	}
 	
 	@Override
 	public void setValue(Object value) {
 		if(value==null){
 			return;
 		}
 		
 		String key = (String)value;
 		lstItems.setValueByKey(key);
 		
 		KeyValuePair keyValuePair = lstItems.getValue();		
 		if(keyValuePair!=null){
 			lblComponent.setText(keyValuePair.getValue());
 		}else{
 			lblComponent.setText(value.toString());
 		}
 	}
 	
 	@Override
 	public Widget getComponent(boolean small) {
 				
 //		if(!readOnly){
 //			return lstItems;
 //		}
 		return panelControls;
 	}
 }
