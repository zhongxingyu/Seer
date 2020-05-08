 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.ihhira.projects.j2meaccountant;
 
 import com.ihhira.projects.j2meaccountant.model.Database;
 import com.ihhira.projects.j2meaccountant.model.Account;
 import com.sun.lwuit.Command;
 import com.sun.lwuit.Dialog;
 import com.sun.lwuit.Form;
 import com.sun.lwuit.List;
 import com.sun.lwuit.events.ActionEvent;
 import com.sun.lwuit.events.ActionListener;
 import com.sun.lwuit.layouts.BoxLayout;
 import com.sun.lwuit.list.DefaultListModel;
 import java.util.Vector;
 
 /**
  *
  * @author Imran
  */
 public class AccountsWindow extends Form implements ActionListener {
 
     Vector accounts;
     List accountsList;
     private final Command exitCommand;
     private final Command settingsCommand;
     private final Command categoriesCommand;
     private final Command deleteCommand;
     private final Command editCommand;
     private final Command addCommand;
     private final Command showCommand;
     private final Command aboutCommand;
 
     public AccountsWindow() {
         super("All Accounts");
 
         setLayout(new BoxLayout(BoxLayout.Y_AXIS));
 
         accountsList = new List();
         addComponent(accountsList);
 
         exitCommand = new Command("Exit");
         aboutCommand = new Command("About");
         settingsCommand = new Command("Settings");
         categoriesCommand = new Command("Show Categories");
         deleteCommand = new Command("Delete Account");
         editCommand = new Command("Edit Account");
         addCommand = new Command("Add Account");
 
         showCommand = new Command("Transactions");
 
         addCommand(exitCommand);
         addCommand(aboutCommand);
         addCommand(settingsCommand);
         addCommand(categoriesCommand);
         addCommand(deleteCommand);
         addCommand(editCommand);
         addCommand(addCommand);
         addCommand(showCommand);
 
         addCommandListener(this);
 
         refreshAccountList();
 
     }
 
     private void showNewAccountForm() {
         showAcountForm(null);
     }
 
     private void showExistingAccountForm(Account existingAccount) {
         showAcountForm(existingAccount);
     }
 
     /**
      *
      * @param <Account> existingAccount Performs two task. Add new account ( if
      * param is null
      */
     private void showAcountForm(final Account existingAccount) {
         String title = existingAccount == null ? "New account" : "Edit account";
         String existingName = (existingAccount == null ? "" : existingAccount.name);
 
 
         InputForm accountInputForm = new InputForm(this, title, "name", existingName) {
             protected void formSubmitted(int response, String text) {
                 if (response == InputForm.RESPONSE_TYPE_OK && text.length() > 0) {
                     if (existingAccount == null) {
                         Account acc = new Account(text);
                         Database.addAccount(acc);
                     } else {
                         existingAccount.name = text;
                         Database.updateAccount(existingAccount);
                     }
                     refreshAccountList();
                 }
             }
         };
         accountInputForm.show();
     }
 
     private void refreshAccountList() {
         accounts = Database.getAccounts();
         DefaultListModel listModel = new DefaultListModel();
         for (int i = 0; i < accounts.size(); i++) {
             Account acc = (Account) accounts.elementAt(i);
             listModel.addItem(acc.name + " : " + Database.getAccountBalance(acc));
         }
         accountsList.setModel(listModel);
     }
 
     public void actionPerformed(ActionEvent ae) {
         Command c = ae.getCommand();
 
         int selectedIndex = accountsList.getSelectedIndex();
         if (c == exitCommand) {
             AccountantMidlet.exit();
         } else if (c == aboutCommand) {
             Dialog.show("About", "This app has been developed by Md Imran Hasan Hira."
                     + " For any contact please email me at imranhasanhira@gmail.com", Dialog.TYPE_INFO, null, "Ok", "Cancel");
         } else if (c == settingsCommand) {
 //            SettingsWindow settingsWindow = new SettingsWindow();
 //            settingsWindow.show();
             Dialog.show("Wait", "Coming soon...", "Ok", "Cancel");
         } else if (c == categoriesCommand) {
             CategoriesWindow categoriesWindow = new CategoriesWindow("Categories");
             categoriesWindow.show();
 
         } else if (c == deleteCommand) {
            if (selectedIndex != -1) {
                 Account account = (Account) accounts.elementAt(selectedIndex);
 
                 Dialog dialog = new Dialog();
                 boolean responseOK = dialog.show("Delete Account",
                         "Do you want to delete '" + account.name + "'",
                         Dialog.TYPE_CONFIRMATION, null, "Yes", "No");
                 if (responseOK) {
                     Database.deleteAccount(account);
                     accounts.removeElementAt(selectedIndex);
                     accountsList.getModel().removeItem(selectedIndex);
                 }
             }
 
         } else if (c == editCommand) {
            if (selectedIndex != -1) {
                 Account account = (Account) accounts.elementAt(selectedIndex);
                 Log.log("Editing account - " + account);
                 showExistingAccountForm(account);
             }
 
         } else if (c == addCommand) {
             Log.log("Adding account");
             showNewAccountForm();
 
         } else if (c == showCommand) {
            if (selectedIndex != -1) {
                 Account account = (Account) accounts.elementAt(selectedIndex);
                 Log.log("Showing account - " + account);
                 TransactionWindow transactionWindow = new TransactionWindow(account);
                 transactionWindow.show();
             }
 
         }
     }
 }
