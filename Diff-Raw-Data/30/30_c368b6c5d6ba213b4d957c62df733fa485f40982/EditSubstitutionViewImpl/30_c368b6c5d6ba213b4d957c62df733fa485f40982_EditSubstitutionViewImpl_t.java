 package com.example.test.task.client.view;
 
 import java.util.Date;
 import java.util.Map;
 import java.util.Map.Entry;
 
import com.example.test.task.client.Messages;
import com.example.test.task.client.NonStringMessages;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ChangeEvent;
 import com.google.gwt.event.dom.client.ChangeHandler;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.user.client.ui.DialogBox;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.PushButton;
 import com.google.gwt.user.datepicker.client.DateBox;
import com.google.gwt.user.datepicker.client.DateBox.Format;
import com.google.gwt.i18n.client.DateTimeFormat;
 
 public class EditSubstitutionViewImpl implements EditSubstitutionView {
 
 	private static EditSubstitutionViewImplUiBinder uiBinder = GWT
 			.create(EditSubstitutionViewImplUiBinder.class);
	private NonStringMessages nonStringMessages = GWT
			.create(NonStringMessages.class);
	
	private Messages messages = GWT
			.create(Messages.class);
	
 	@UiField
 	PushButton cancelButton;
 	@UiField
 	PushButton saveButton;
 	@UiField
 	ListBox roleLbx;
 	@UiField
 	ListBox substituteLbx;
 	@UiField
 	ListBox ruleTypeLbx;
 	@UiField
 	DateBox beginDbx;
 	@UiField
 	DateBox endDbx;
 
 	@UiField
 	DialogBox dialogBox;
 	@UiField WindowContainer windowContainer;
 
 	Presenter presenter;
 
 	interface EditSubstitutionViewImplUiBinder extends
 			UiBinder<DialogBox, EditSubstitutionViewImpl> {
 	}
 
 	public EditSubstitutionViewImpl() {
 		dialogBox = uiBinder.createAndBindUi(this);
 		ruleTypeLbx.addChangeHandler(new ChangeHandler() {
 			public void onChange(ChangeEvent event) {
 				presenter.onRuleTypesSelection();
 			}
 		});
		beginDbx.setFormat(new DateBoxFormat());
		endDbx.setFormat(new DateBoxFormat());
 	}
 
 	public void setPresenter(Presenter presenter) {
 		this.presenter = presenter;
 	}
 
 
 
 	public void go() {
 		dialogBox.center();
 		dialogBox.show();
 	}
 
 	@UiHandler("cancelButton")
 	void onCancelButtonClick(ClickEvent event) {
 		presenter.onCancelAction();
 
 		//TODO: if should be here?
 		dialogBox.hide();
 	}
 
 	@UiHandler("saveButton")
 	void onSaveButtonClick(ClickEvent event) {
 		presenter.onSaveAction();
 	}
 
 	public void dataSaved() {
 		dialogBox.hide();
 	}
 
 	public void setRoles(Map<String, String> roles) {
 		roleLbx.clear();
 		fillListBoxState(roles,roleLbx);
 	}
 
 	public void setSubstitors(Map<String, String> substitutors) {
 		substituteLbx.clear();
 		fillListBoxState(substitutors,substituteLbx);
 	}
 
 	public void setRuleTypes(Map<String, String> ruleTypes) {
 		ruleTypeLbx.clear();
 		fillListBoxState(ruleTypes,ruleTypeLbx);
 	}
 
 	public void setBeginDate(Date beginDate) {
 		beginDbx.setValue(beginDate);
 	}
 
 	public void setEndDate(Date endDate) {
 		endDbx.setValue(endDate);
 	}
 
 	public void setTimeIntervalEnabled(boolean isTimeIntervalEnabled) {
 		beginDbx.setEnabled(isTimeIntervalEnabled);
 		endDbx.setEnabled(isTimeIntervalEnabled);
 	}
 	
 	private void fillListBoxState(Map<String,String> data,ListBox listBox){
 		for(Entry<String, String> entry:data.entrySet()){
 			listBox.addItem(entry.getKey(), entry.getValue());
 		}
 	}
 	
 	private void setSelectedItemByValue(ListBox listBox, String value){
 		for(int i=0;i<listBox.getItemCount();i++){
 			if(value.equals(listBox.getValue(i)))
 				listBox.setSelectedIndex(i);
 		}
 	}
 
 	public String getRole() {
 		return roleLbx.getValue(roleLbx.getSelectedIndex());
 	}
 
 	public String getSubstituror() {
 		return substituteLbx.getValue(substituteLbx.getSelectedIndex());
 	}
 
 	public String getRuleType() {
 		return ruleTypeLbx.getValue(ruleTypeLbx.getSelectedIndex());
 	}
 
 	public Date getBeginDate() {
 		return beginDbx.getValue();
 	}
 
 	public Date getEndDate() {
 		return endDbx.getValue();
 	}
 
 	public void setRole(String id) {
 		setSelectedItemByValue(roleLbx, id);
 	}
 
 	public void setSubstitutor(String id) {
 		setSelectedItemByValue(substituteLbx, id);
 	}
 
 	public void setRuleType(String id) {
 		setSelectedItemByValue(ruleTypeLbx, id);
 	}
 
 	public void setCancelControllEnabled(boolean enabled) {
 		cancelButton.setEnabled(enabled);
 	}
 
 	public void setSaveControllEnabled(boolean enabled) {
 		saveButton.setEnabled(enabled);
 	}
 
 	public void onDataSavingOk() {
 		dialogBox.hide();
 	}
 	
 	@UiHandler("windowContainer")
 	void onWindowContainerClick(ClickEvent event) {
 		onCancelButtonClick(event);
 	}
	
	public class DateBoxFormat implements Format{
		public void reset(DateBox dateBox, boolean abandon) {
			
		}
		
		public Date parse(DateBox dateBox, String text, boolean reportError) {
			return (text==null|"".equals(text))?null:DateTimeFormat.getFormat(messages.dateBoxDateFormat()).parse(text);
		}
		
		public String format(DateBox dateBox, Date date) {
			return date==null?"":nonStringMessages.dateFormat(date);
		}
	}
 }
