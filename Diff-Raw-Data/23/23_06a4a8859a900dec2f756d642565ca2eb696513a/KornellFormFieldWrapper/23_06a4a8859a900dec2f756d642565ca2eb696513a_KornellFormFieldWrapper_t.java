 package kornell.gui.client.uidget.formfield;
 
 import com.github.gwtbootstrap.client.ui.ListBox;
 import com.github.gwtbootstrap.client.ui.TextBox;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.Widget;
 
 public class KornellFormFieldWrapper extends Composite {
 	interface MyUiBinder extends UiBinder<Widget, KornellFormFieldWrapper> {
 	}
 
 	private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
 	
 	@UiField Label fieldLabel;
 	@UiField FlowPanel fieldPanel;
 	
 	TextBox fieldTextBox;
 	ListBox fieldListBox;
 	SimpleDatePicker fieldSimpleDatePicker;
 	
 	Label fieldError;
 	Label fieldTxt;
 	
 	boolean isEditMode;
 	KornellFormField<?> formField;
 	
 	public KornellFormFieldWrapper(String label, KornellFormField<?> formField) {
 		this(label, formField, true);
 	}
 	
 	public KornellFormFieldWrapper(String label, KornellFormField<?> formField, boolean isEditMode) {
 
 		initWidget(uiBinder.createAndBindUi(this));
 		fieldLabel.setText(label);
 		this.formField = formField;
 		this.isEditMode = isEditMode;
 		
 		initData(formField);
 	}
 	
 	public void initData(KornellFormField<?> formField) {
 		fieldPanel.clear();
		this.formField = formField;
 		if(!isEditMode){
 			fieldTxt = new Label();
 			fieldTxt.addStyleName("lblValue");
 			fieldTxt.setText(formField.getDisplayText());
 			fieldPanel.add(fieldTxt);
 		} else {
 			fieldPanel.add(formField.getFieldWidget());
 			fieldError = new Label();
 			fieldError.addStyleName("error");
 			fieldPanel.add(fieldError);
 		}
 		
 	}
 	
 	public KornellFormField<?> getFormField(){
 		return formField;
 	}
 	
 	public Widget getFieldWidget(){
 		return formField.getFieldWidget();
 	}
 	
 	public String getFieldDisplayText(){
 		return formField.getDisplayText();
 	}
 	
 	public String getFieldPersistText(){
 		return formField.getPersistText();
 	}
 	
 	public void setError(String text){
 		fieldError.setText(text);
 	}
 	
 	public String getError(){
 		return fieldError != null ? fieldError.getText() : "";
 	}
 
 	public void clearError(){
 		if(fieldError != null){
 			fieldError.setText("");
 		}
 	}
 }
