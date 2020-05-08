 package com.duggan.workflow.client.ui.save.form;
 
 import java.util.Collection;
 import java.util.Date;
 import java.util.Map;
 
 import com.duggan.workflow.client.ui.AppManager;
 import com.duggan.workflow.client.ui.component.IssuesPanel;
 import com.duggan.workflow.client.ui.delegate.FormDelegate;
 import com.duggan.workflow.shared.model.Document;
 import com.duggan.workflow.shared.model.DocumentLine;
 import com.duggan.workflow.shared.model.GridValue;
 import com.duggan.workflow.shared.model.IntValue;
 import com.duggan.workflow.shared.model.Priority;
 import com.duggan.workflow.shared.model.Value;
 import com.duggan.workflow.shared.model.form.Form;
import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.event.dom.client.HasClickHandlers;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.event.shared.EventBus;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.ui.CheckBox;
 import com.google.gwt.user.client.ui.DialogBox;
 import com.google.gwt.user.client.ui.HTMLPanel;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.inject.Inject;
 import com.gwtplatform.mvp.client.PopupViewImpl;
 
 public class GenericFormView extends PopupViewImpl implements
 		GenericFormPresenter.ICreateDocView {
 
 	private final Widget widget;
 
 	public interface Binder extends UiBinder<Widget, GenericFormView> {
 	}
 
 	@UiField DialogBox diaBox;
 	
 	@UiField HTMLPanel panelFields;
 	
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
 	
 	FormDelegate formDelegate = new FormDelegate();
 
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
 		
 		int[] position=AppManager.calculatePosition(5, 50);
 		diaBox.setPopupPosition(position[1],position[0]);
		//diaBox.getElement().getStyle().setLeft(position[0], Unit.PX);
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
 		doc.setPriority(getPriority().ordinal());
 		doc.setValue("priority", new IntValue(null,"priority",doc.getPriority()));
 
 		Map<String,Value> values = formDelegate.getValues(panelFields);
 		doc.setValues(values);
 		
 		for(Value val: values.values()){
 			if(val instanceof GridValue){
 				GridValue gridVal = (GridValue)val;
 				Collection<DocumentLine> lines = gridVal.getValue();
 				for(DocumentLine line: lines){
 					line.setName(gridVal.getKey());
 					doc.addDetail(line);
 				}
 				
 			}
 		}
 		return doc;
 	}
 
 	@Override
 	public boolean isValid() {
 		return formDelegate.isValid(issues, panelFields);
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
 		formDelegate.setFields(form.getFields(), panelFields);
 	}
 
 
 	@Override
 	public HasClickHandlers getForwardForApproval() {
 		return btnApproval;
 	}
 
 }
