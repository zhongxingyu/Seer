 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.ihhira.projects.j2meaccountant;
 
 import com.ihhira.projects.j2meaccountant.model.Category;
 import com.ihhira.projects.j2meaccountant.model.Database;
 import com.ihhira.projects.j2meaccountant.model.Account;
 import com.ihhira.projects.j2meaccountant.model.Transaction;
 import com.sun.lwuit.ComboBox;
 import com.sun.lwuit.Command;
 import com.sun.lwuit.Dialog;
 import com.sun.lwuit.Form;
 import com.sun.lwuit.Label;
 import com.sun.lwuit.TextField;
 import com.sun.lwuit.events.ActionEvent;
 import com.sun.lwuit.events.ActionListener;
 import com.sun.lwuit.layouts.BoxLayout;
 import java.util.Vector;
 
 /**
  *
  * @author Imran
  */
 public abstract class TransactionForm extends Form {
 
     public static final int RESPONSE_TYPE_OK = 0;
     public static final int RESPONSE_TYPE_CANCEL = 1;
     Vector accountsVector;
     Vector categoriesVector;
     ComboBox secondaryAccountComboBox;
     ComboBox categoryComboBox;
     TextField description;
     TextField amount;
 
     public TransactionForm(String title, final Transaction transaction) {
         super(title);
         setLayout(new BoxLayout(BoxLayout.Y_AXIS));
 
         if (transaction.transfer) {
             prepateSecondaryAccountChoiceGroup(transaction);
         } else {
             prepareCategoryChoiceGroup(transaction);
         }
 
         //        
         addComponent(new Label("Description"));
         description = new TextField(transaction.description);
         description.setConstraint(TextField.ANY);
         addComponent(description);
 
 
         //
         addComponent(new Label("Amount"));
         amount = new TextField("" + transaction.amount);
        amount.setConstraint(TextField.ANY);
         addComponent(amount);
 
 
 
         addCommand(new Command("OK", RESPONSE_TYPE_OK));
         addCommand(new Command("Cancel", RESPONSE_TYPE_CANCEL));
 
         addCommandListener(new ActionListener() {
             public void actionPerformed(ActionEvent ae) {
                 Command command = ae.getCommand();
                 try {
                     if (command.getId() == RESPONSE_TYPE_OK) {
                         transaction.description = description.getText();
                         try {
                             transaction.amount = Double.parseDouble(amount.getText());
                         } catch (Exception ex) {
                             throw new Exception("amount is not correct");
                         }
 
                         if (transaction.transfer) {
                             Log.log("selected account index = " + secondaryAccountComboBox.getSelectedIndex());
                             transaction.secondaryAccountID = ((Account) accountsVector.elementAt(secondaryAccountComboBox.getSelectedIndex())).id;
                             if (transaction.secondaryAccountID == transaction.primaryAccountID) {
                                 throw new Exception("Please select another account");
                             }
                         } else {
                             Log.log("selected category index = " + categoryComboBox.getSelectedIndex());
                             transaction.categoryID = ((Category) categoriesVector.elementAt(categoryComboBox.getSelectedIndex())).id;
                         }
                     }
                     formSubmitted(command.getId(), transaction);
                 } catch (Exception e) {
                     e.printStackTrace();
                     Dialog.show("Error", e.getMessage(), "Ok", "Cancel");
                 }
             }
         });
     }
 
     public abstract void formSubmitted(int responseType, Transaction transaction);
 
     private void prepateSecondaryAccountChoiceGroup(final Transaction transaction) {
         accountsVector = Database.getAccounts();
         String[] accountNames = new String[accountsVector.size()];
         int selectedAccountIndex = -1;
         for (int i = 0; i < accountNames.length; i++) {
             accountNames[i] = ((Account) accountsVector.elementAt(i)).name;
             if (transaction.secondaryAccountID == ((Account) accountsVector.elementAt(i)).id) {
                 selectedAccountIndex = i;
             }
         }
         addComponent(new Label("To"));
         secondaryAccountComboBox = new ComboBox(accountNames);
         addComponent(secondaryAccountComboBox);
         if (selectedAccountIndex != -1) {
             secondaryAccountComboBox.setSelectedIndex(selectedAccountIndex, true);
         }
     }
 
     private void prepareCategoryChoiceGroup(final Transaction transaction) {
         categoriesVector = Database.getCategories();
         String[] categoryNames = new String[categoriesVector.size()];
         int selectedCategoryIndex = -1;
         for (int i = 0; i < categoryNames.length; i++) {
             categoryNames[i] = ((Category) categoriesVector.elementAt(i)).name;
             if (transaction.id == ((Category) categoriesVector.elementAt(i)).id) {
                 selectedCategoryIndex = i;
             }
         }
         addComponent(new Label("Category"));
         categoryComboBox = new ComboBox(categoryNames);
         addComponent(categoryComboBox);
         if (selectedCategoryIndex != -1) {
             categoryComboBox.setSelectedIndex(selectedCategoryIndex, true);
         }
     }
 }
