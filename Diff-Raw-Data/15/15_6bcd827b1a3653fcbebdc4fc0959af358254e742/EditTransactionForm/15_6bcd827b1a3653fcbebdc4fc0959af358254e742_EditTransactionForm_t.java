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
import com.marthym.oikonomos.main.client.i18n.AccountTransactionsConstants;
 
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
 	
	AccountTransactionsConstants constants = GWT.create(AccountTransactionsConstants.class);
	
 	public EditTransactionForm() {
 		initWidget(uiBinder.createAndBindUi(this));
		
		transactionDate.getElement().setAttribute("placeholder", constants.placeholder_date());
		transactionPayee.getElement().setAttribute("placeholder", constants.placeholder_payee());
		transactionDebit.getElement().setAttribute("placeholder", constants.placeholder_debit());
		transactionCredit.getElement().setAttribute("placeholder", constants.placeholder_credit());
		transactionCategory.getElement().setAttribute("placeholder", constants.placeholder_category());
		transactionPaiementMean.getElement().setAttribute("placeholder", constants.placeholder_paiementMean());
		transactionAccountingDocument.getElement().setAttribute("placeholder", constants.placeholder_accountingDocument());
		transactionComment.getElement().setAttribute("placeholder", constants.placeholder_comment());
		transactionBudgetaryLine.getElement().setAttribute("placeholder", constants.placeholder_budgetaryLine());
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
