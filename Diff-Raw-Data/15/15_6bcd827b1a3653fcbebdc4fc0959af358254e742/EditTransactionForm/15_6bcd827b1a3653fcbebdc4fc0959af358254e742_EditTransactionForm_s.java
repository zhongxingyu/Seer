 package com.marthym.oikonomos.main.client.components;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.HasClickHandlers;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.FormPanel;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.Widget;
 
 public class EditTransactionForm extends Composite {
 	private static EditTransactionFormUiBinder uiBinder = GWT.create(EditTransactionFormUiBinder.class);
 	interface EditTransactionFormUiBinder extends UiBinder<Widget, EditTransactionForm> {}
 	
 	@UiField FormPanel formTransaction;
 	@UiField TextBox transactionDate;
 	@UiField TextBox transactionPayee;
 	@UiField TextBox transactionDebit;
 	@UiField TextBox transactionCredit;
 	@UiField TextBox transactionCategory;
 	@UiField TextBox transactionPaiementMean;
 	@UiField TextBox transactionAccountingDocument;
 	@UiField TextBox transactionComment;
 	@UiField TextBox transactionBudgetaryLine;
 	@UiField Button resetButton;
 	@UiField Button submitButton;
 	
 	public EditTransactionForm() {
 		initWidget(uiBinder.createAndBindUi(this));
 	}
 
 	public HasClickHandlers getValidateButton() {
 		return submitButton;
 	}
 
 	public HasClickHandlers getResetButton() {
 		return resetButton;
 	}
 
 	public FormPanel getForm() {
 		return formTransaction;
 	}
 }
