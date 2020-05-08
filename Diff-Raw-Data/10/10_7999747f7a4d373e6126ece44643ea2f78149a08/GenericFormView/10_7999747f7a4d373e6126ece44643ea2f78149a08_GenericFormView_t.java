 package com.duggan.workflow.client.ui.save.form;
 
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.List;
 
 import com.duggan.workflow.client.ui.admin.formbuilder.component.FieldWidget;
 import com.duggan.workflow.client.ui.component.IssuesPanel;
 import com.duggan.workflow.shared.model.DataType;
 import com.duggan.workflow.shared.model.Document;
 import com.duggan.workflow.shared.model.DocumentType;
 import com.duggan.workflow.shared.model.IntValue;
 import com.duggan.workflow.shared.model.Priority;
 import com.duggan.workflow.shared.model.Value;
 import com.duggan.workflow.shared.model.form.Field;
 import com.duggan.workflow.shared.model.form.Form;
 import com.duggan.workflow.shared.model.form.FormModel;
 import com.google.gwt.event.dom.client.HasClickHandlers;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.event.shared.EventBus;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.ui.CheckBox;
 import com.google.gwt.user.client.ui.DialogBox;
 import com.google.gwt.user.client.ui.HTMLPanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.inject.Inject;
 import com.gwtplatform.mvp.client.PopupViewImpl;
 
 public class GenericFormView extends PopupViewImpl implements
 		GenericFormPresenter.ICreateDocView {
 
 	private final Widget widget;
 
 	public interface Binder extends UiBinder<Widget, GenericFormView> {
 	}
 
 	@UiField DialogBox diaBox;
 	
 	@UiField VerticalPanel panelFields;
 	
 	@UiField
 	HasClickHandlers btnSave;
 	@UiField
 	HasClickHandlers btnApproval;
 	@UiField
 	HasClickHandlers btnCancel;
 
 	@UiField
 	CheckBox chkNormal;
 	@UiField
 	CheckBox chkHigh;
 	@UiField
 	CheckBox chkCritical;
 
 	@UiField
 	IssuesPanel issues;
 
 	@Inject
 	public GenericFormView(final EventBus eventBus, final Binder binder) {
 		super(eventBus);
 		widget = binder.createAndBindUi(this);
 		ValueChangeHandler<Boolean> changeHandler = new ValueChangeHandler<Boolean>() {
 
 			@Override
 			public void onValueChange(ValueChangeEvent<Boolean> event) {
 
 				boolean v = event.getValue();
 
 				// if(v){
 				chkNormal.setValue(event.getSource().equals(chkNormal) ? v
 						: false);
 				chkHigh.setValue(event.getSource().equals(chkHigh) ? v : false);
 				chkCritical.setValue(event.getSource().equals(chkCritical) ? v
 						: false);
 				// }
 			}
 		};
 
 		chkCritical.addValueChangeHandler(changeHandler);
 		chkHigh.addValueChangeHandler(changeHandler);
 		chkNormal.addValueChangeHandler(changeHandler);
 
 	}
 
 	@Override
 	public Widget asWidget() {
 		return widget;
 	}
 
 
 	@Override
 	public HasClickHandlers getSave() {
 
 		return btnSave;
 	}
 
 	@Override
 	public HasClickHandlers getCancel() {
 
 		return btnCancel;
 	}
 
 	@Override
 	public HasClickHandlers getForward() {
 
 		return btnApproval;
 	}
 
 	@Override
 	public Document getDocument() {
 		Document doc = new Document();
 		doc.setDocumentDate(new Date());
 		doc.setId(null);
 		doc.setType(new DocumentType(3L,"REQUISITION","Requisition","color-win8"));
 		doc.setPriority(getPriority().ordinal());
 		doc.setValue("priority", new IntValue(doc.getPriority()));
 
 		int fields = panelFields.getWidgetCount();
 		
 		for(int i=0; i<fields; i++){
 			Widget widget = panelFields.getWidget(i);
 			if(!(widget instanceof FieldWidget)){
 				continue;
 			}
 			
 			FieldWidget fieldWidget = (FieldWidget)widget;
 			Field field = fieldWidget.getField();
 			
 			Value fieldValue = fieldWidget.getFieldValue();
 			if(field.getType()==DataType.STRING ||
 					field.getType()==DataType.STRINGLONG
 					|| field.getType()==DataType.DATE) {
 				
 				doc.setValue(field.getName(), fieldValue);
 				
 			}
 			
 			Object val = null;
 			
 			if(fieldValue!=null){
 				val = fieldValue.getValue();
 			}
 			
 			if(field.getName().equals("description"))
 				doc.setDescription(val==null? null : val.toString());
 			
 			if(field.getName().equals("dueDate"))
 				doc.setDateDue(val==null? null : (Date)val);
 			
 			if(field.getName().equals("docDate"))
 				doc.setDocumentDate(val==null? null : (Date)val);
 			
 			if(field.getName().equals("partner"))
 				doc.setPartner(val==null? null : val.toString());
 			
 			if(field.getName().equals("subject"))
 				doc.setSubject(val==null? null : val.toString());
 			
 			if(field.getName().equals("docType"))
 				doc.setType(val==null? null : (DocumentType)val);
 			
 			if(field.getName().equals("value"))
 				doc.setValue(val==null? null : val.toString());
 		}
 		
 		return doc;
 	}
 
 	@Override
 	public boolean isValid() {
 		// txtDescription.getValue();
 		boolean isValid = true;
 		issues.clear();
 		
 		int fields = panelFields.getWidgetCount();
 		
 		for(int i=0; i<fields; i++){
 			Widget widget = panelFields.getWidget(i);
 			
 			if(!(widget instanceof FieldWidget)){
 				continue;
 			}
 			
 			FieldWidget fieldWidget = (FieldWidget)widget;
 			Field field = fieldWidget.getField();
 			
 			Object obj = fieldWidget.getValue(FieldWidget.MANDATORY);
 			if(obj==null){
 				continue;
 			}
 			
 			Boolean mandatory = (Boolean)obj;
 			if(mandatory){
 				Value fieldValue = fieldWidget.getFieldValue();
 				Object val = null;
 				
 				if(fieldValue!=null){
 					val = fieldValue.getValue();
 				}
 				
 				if(val==null){
 					isValid=false;
 					issues.addError("'"+field.getCaption()+"' is Mandatory");
 				}
 				
 			}
 		}
 		
 		return isValid;
 	}
 
 	boolean isNullOrEmpty(String value) {
 		return value == null || value.trim().length() == 0;
 	}
 
 	public void setValues() {
 	}
 
 	private void setPriority(Priority priority) {
 		switch (priority) {
 		case CRITICAL:
 			chkCritical.setValue(true);
 			break;
 		case HIGH:
 			chkHigh.setValue(true);
 			break;
 		case NORMAL:
 			chkNormal.setValue(true);
 			break;
 		}
 	}
 
 	public Priority getPriority() {
 		Priority priority = Priority.NORMAL;
 
 		if (chkCritical.getValue()) {
 			priority = Priority.CRITICAL;
 		}
 
 		if (chkHigh.getValue()) {
 			priority = Priority.HIGH;
 		}
 
 		return priority;
 	}
 
 	@Override
 	public void setForm(Form form) {
		//this.
 		//paint the elements
 		diaBox.setText(form.getCaption());
 		setFields(form.getFields());
 	}
 
 	private void setFields(List<Field> fields) {
 		Collections.sort(fields, new Comparator<FormModel>() {
 			public int compare(FormModel o1, FormModel o2) {
 				Field field1 = (Field)o1;
 				Field field2 = (Field)o2;
 				
 				Integer pos1 = field1.getPosition();
 				Integer pos2 = field2.getPosition();
 				
 				return pos1.compareTo(pos2);
 			};
 			
 		});
 		for(Field field: fields){
 			FieldWidget fieldWidget = FieldWidget.getWidget(field.getType(), field, false);
 			panelFields.add(fieldWidget);
 		}
 	}
 
 }
