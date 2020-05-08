 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.ihhira.projects.j2meaccountant;
 
 import com.ihhira.projects.j2meaccountant.model.Database;
 import com.ihhira.projects.j2meaccountant.model.Account;
 import com.ihhira.projects.j2meaccountant.model.Transaction;
 import com.sun.lwuit.Command;
 import com.sun.lwuit.Component;
 import com.sun.lwuit.Dialog;
 import com.sun.lwuit.Font;
 import com.sun.lwuit.Form;
 import com.sun.lwuit.TextArea;
 import com.sun.lwuit.TextField;
 import com.sun.lwuit.events.ActionEvent;
 import com.sun.lwuit.events.ActionListener;
 import com.sun.lwuit.layouts.FlowLayout;
 import com.sun.lwuit.table.DefaultTableModel;
 import com.sun.lwuit.table.Table;
 import java.util.Date;
 import java.util.Vector;
 
 /**
  *
  * @author Imran
  */
 public class TransactionWindow extends Form implements ActionListener {
 
     private final int TRANSACTION_DEPOSITE = -1;
     private final int TRANSACTION_SPEND = -2;
     private final int TRANSACTION_TRANSFER = -3;
     Account account;
     Vector transactions;
     Table transactionTable;
     private final Command backCommand;
     private final Command deleteCommand;
     private final Command editCommand;
     private final Command depositeCommand;
     private final Command spendCommand;
     private final Command transferCommand;
 
     TransactionWindow(Account account) {
         super(account.toString());
         this.account = account;
         setLayout(new FlowLayout(LEFT));
 
         final int dateColumnWidth = Font.getDefaultFont().stringWidth(Util.getDate(new Date()));
         final int amountColumnWidth = Font.getDefaultFont().stringWidth("00000000000");
         transactionTable = new Table() {
             protected Component createCell(Object value, int row, int column, boolean editable) {

                 if (column == 0) {
                     TextField textField = new TextField(value.toString());
                     textField.setEditable(false);
                     textField.setPreferredW(dateColumnWidth);
                     return textField;
                     
                 } else if (column == 2) {
                     TextField textField = new TextField(value.toString());
                     textField.setEditable(false);
                     textField.setAlignment(TextField.RIGHT);
                     textField.setPreferredW(amountColumnWidth);
                     return textField;
 
                 } else {
                     TextArea textArea = new TextArea(value.toString());
                     textArea.setEditable(false);
                     return textArea;
                 }
             }
         };
         removeAll();
         addComponent(transactionTable);
         setScrollable(true);
 
         backCommand = new Command("Back");
         deleteCommand = new Command("Delete");
         editCommand = new Command("Edit");
         depositeCommand = new Command("Add");
         spendCommand = new Command("Spend");
         transferCommand = new Command("Transfer fund");
 
         addCommand(backCommand);
         addCommand(depositeCommand);
         addCommand(spendCommand);
         addCommand(transferCommand);
 
         addCommandListener(this);
 
         refreshTransactions();
 
         if (transactions.size() <= 0) {
             Dialog.show("Empty Transaction list", "Please add a transaction", "Ok", "Cancel");
         }
     }
 
     private void refreshTransactions() {
         //refreshing transactionList
         transactions = Database.getTransactions(account);
         String[][] tableData = new String[transactions.size()][3];
         for (int i = 0; i < transactions.size(); i++) {
             Transaction transaction = (Transaction) transactions.elementAt(i);
             tableData[i][0] = Util.getDate(transaction.date);
             tableData[i][1] = getDetail(transaction);
             tableData[i][2] = getAmount(transaction, account);
         }
         String[] columnNames = new String[]{"Date", "Desc", "Amount"};
         DefaultTableModel tableModel = new DefaultTableModel(columnNames, tableData);
         transactionTable.setModel(tableModel);
         refreshCommands();
     }
 
 //    private void updateTableData(int selectedIndex, Transaction transaction) {
 //        transactionTable.getModel().setValueAt(selectedIndex, 0, Util.getDate(transaction.date));
 //        transactionTable.getModel().setValueAt(selectedIndex, 1, getDetail(transaction));
 //        transactionTable.getModel().setValueAt(selectedIndex, 2, getAmount(transaction, account));
 //    }
     private void updateTransaction(int selectedIndex, Transaction transaction) {
         Database.updateTransaction(transaction);
         refreshTransactions();
     }
 
     private void deleteTransaction(int selectedIndex) {
         Transaction transaction = (Transaction) transactions.elementAt(selectedIndex);
         Database.deleteTransaction(transaction);
         refreshTransactions();
 
     }
 
     private void addTransaction(Transaction transaction) {
         Database.addTransaction(transaction);
         refreshTransactions();
     }
 
     private void showTransactionForm(final int selectedIndex, Transaction transaction) {
         String title = null;
         if (selectedIndex == TRANSACTION_DEPOSITE) {
             title = "Deposite";
         } else if (selectedIndex == TRANSACTION_SPEND) {
             title = "Spend";
         } else if (selectedIndex == TRANSACTION_TRANSFER) {
             title = "Transfer";
         } else {
             title = "Edit Transaction";
         }
 
         TransactionForm transactionForm = new TransactionForm(title, transaction) {
             public void formSubmitted(int responseType, Transaction transaction) {
                 if (responseType == RESPONSE_TYPE_OK) {
                     if (selectedIndex < 0) {
                         if (selectedIndex == TRANSACTION_SPEND) {
                             transaction.amount = -transaction.amount;
                         }
                         addTransaction(transaction);
                     } else {
                         updateTransaction(selectedIndex, transaction);
                     }
                 }
                 TransactionWindow.this.show();
             }
         };
         transactionForm.show();
     }
 
     private void refreshCommands() {
         //refreshing commands
         if (transactions.size() > 0) {
             addCommand(deleteCommand);
             addCommand(editCommand);
         } else {
             removeCommand(deleteCommand);
             removeCommand(editCommand);
         }
     }
 
     private String getDetail(Transaction transaction) {
         StringBuffer sb = new StringBuffer();
         if (transaction.transfer) {
             if (transaction.primaryAccountID == account.id) {
                 sb.append("Transfered to ");
                 sb.append(Database.getAccount(transaction.secondaryAccountID).name).append(" : ");
             } else {
                 sb.append("Deposited from ");
                 sb.append(Database.getAccount(transaction.primaryAccountID).name).append(" : ");
             }
             sb.append(transaction.description);
 
         } else {
             sb.append(Database.getCategory(transaction.categoryID).name).append(" : ");
             sb.append(transaction.description);
         }
 
         return sb.toString();
     }
 
     private String getAmount(Transaction transaction, Account account) {
         if (transaction.transfer && transaction.primaryAccountID == account.id) {
             return "-" + transaction.amount;
         } else {
             return "" + transaction.amount;
         }
     }
 
     public void actionPerformed(ActionEvent ae) {
         Command c = ae.getCommand();
         int selectedIndex = transactionTable.getSelectedRow();
         Log.log("selected index " + selectedIndex);
         if (c == backCommand) {
             AccountsWindow accountWindow = new AccountsWindow();
             accountWindow.show();
         } else if (c == deleteCommand && selectedIndex >= 0) {
             deleteTransaction(selectedIndex);
         } else if (c == editCommand && selectedIndex >= 0) {
             Transaction transaction = ((Transaction) transactions.elementAt(selectedIndex));
             showTransactionForm(selectedIndex, transaction);
         } else if (c == depositeCommand) {
             Transaction transaction = new Transaction();
             transaction.primaryAccountID = account.id;
             showTransactionForm(TRANSACTION_DEPOSITE, transaction);
         } else if (c == spendCommand) {
             Transaction transaction = new Transaction();
             transaction.primaryAccountID = account.id;
             showTransactionForm(TRANSACTION_SPEND, transaction);
         } else if (c == transferCommand) {
             Transaction transaction = new Transaction();
             transaction.primaryAccountID = account.id;
             transaction.transfer = true;
             showTransactionForm(TRANSACTION_TRANSFER, transaction);
         }
     }
 }
