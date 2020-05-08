 package com.duggan.workflow.client.ui.admin.formbuilder.component;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.duggan.workflow.shared.model.DataType;
 import com.duggan.workflow.shared.model.StringValue;
 import com.duggan.workflow.shared.model.Value;
 import com.duggan.workflow.shared.model.form.KeyValuePair;
 import com.duggan.workflow.shared.model.form.Property;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.i18n.shared.DirectionEstimator;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.Random;
 import com.google.gwt.user.client.ui.HTMLPanel;
 import com.google.gwt.user.client.ui.RadioButton;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.UIObject;
 import com.google.gwt.user.client.ui.Widget;
 
 public class InlineRadio extends FieldWidget implements IsSelectionField{
 
 	private static InlineRadioUiBinder uiBinder = GWT
 			.create(InlineRadioUiBinder.class);
 
 	interface InlineRadioUiBinder extends UiBinder<Widget, InlineRadio> {
 	}
 	
 	private final Widget widget;
 	
 	@UiField Element lblEl;
 	@UiField HTMLPanel vPanel;
 	Value fieldValue=null;
 	
 	public InlineRadio() {
 		super();
 		addProperty(new Property(MANDATORY, "Mandatory", DataType.CHECKBOX, id));
 		addProperty(new Property(SELECTIONTYPE, "Reference", DataType.STRING));
 		widget= uiBinder.createAndBindUi(this);
 		
 //		List<KeyValuePair> pairs = new ArrayList<KeyValuePair>();
 //		pairs.add(new KeyValuePair("","A"));
 //		pairs.add(new KeyValuePair("","B"));
 //		pairs.add(new KeyValuePair("","C"));
 //		setSelectionValues(pairs);
 		add(widget);
 	}
 
 	@Override
 	public FieldWidget cloneWidget() {
 		return new InlineRadio();
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
 		//txtComponent.setTitle(help);
 	}
 	
 	@Override
 	protected DataType getType() {
 		return DataType.BOOLEAN;
 	}
 
 	@Override
 	public void setSelectionValues(List<KeyValuePair> values) {
 		vPanel.clear();
 		if(values==null){
 			return;
 		}
 		
 		for(KeyValuePair pair: values){
 			vPanel.add((RadioButton)getRadio(pair));
 		}
 		field.setSelectionValues(values);
 	}
 
 	private RadioButton getRadio(KeyValuePair pair) {
 		RadioButton button = new RadioButton(getPropertyValue(SELECTIONTYPE));
 		
		String name = pair.getName();
		if(name==null){
			name="radiobtns";
		}
 		
		button.addStyleName("radiobtns");
		button.getElement().setId(name);
 		button.setDirectionEstimator(true);
 		button.setText(pair.getValue());
 		button.setFormValue(pair.getKey());
 		button.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
 			
 			@Override
 			public void onValueChange(ValueChangeEvent<Boolean> event) {
 				RadioButton src  = (RadioButton)event.getSource();
 				valueChange(src,event.getValue());
 			}
 		});
 		return button;
 	}
 
 	protected void valueChange(RadioButton source, boolean value) {
 		if(value){
 			fieldValue = new StringValue(null, source.getName(), source.getFormValue());
 		}else{
 			fieldValue=null;
 		}
 	}
 
 	@Override
 	public List<KeyValuePair> getValues() {
 		int count = vPanel.getWidgetCount();
 		List<KeyValuePair> list = new ArrayList<KeyValuePair>();
 		
 		for(int i=0; i<count; i++){
 			RadioButton txtBox= (RadioButton)vPanel.getWidget(i);
 			String name = txtBox.getName();
 			String val = txtBox.getFormValue();
 			
 			if(val.isEmpty()){
 				continue;
 			}
 			
 			KeyValuePair pair = new KeyValuePair();
 			pair.setKey(name);
 			pair.setValue(val);
 			list.add(pair);
 		}
 		
 		return list;
 	}
 
 	@Override
 	public void setValue(Object value) {
 		String val = value==null? null: value.toString();
 		int count = vPanel.getWidgetCount();
 		for(int i=0; i<count; i++){
 			RadioButton txtBox= (RadioButton)vPanel.getWidget(i);
 			String key = txtBox.getFormValue();
 			
 			if(val!=null)
 			if(val.equals(key)){
 				txtBox.setValue(true);
 				return;
 			}
 		}
 	}
 	
 	@Override
 	public Value getFieldValue() {
 		return fieldValue;
 	}
 	
 	@Override
 	public Widget getComponent(boolean small) {
 		return vPanel;
 	}
 	
 	@Override
 	public void setReadOnly(boolean isReadOnly) {
 		Boolean isComponentReadOnly = getValue(READONLY)==null? false : (Boolean)getValue(READONLY);
 		if(isComponentReadOnly==null){
 			isComponentReadOnly=false;
 		}
 		
 		this.readOnly = isReadOnly && !isComponentReadOnly;
 		int count = vPanel.getWidgetCount();
 		for(int i=0; i<count; i++){
 			RadioButton btn = (RadioButton)vPanel.getWidget(i);
 			btn.setEnabled(!readOnly);
 			//btn.set
 		}
 	}
 }
