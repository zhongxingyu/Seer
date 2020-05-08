 package com.ruben;
 // ---------------------------------------------
 // Problem Description:
 //   AccountsProcedural applet
 //
 // Demonstrates:
 //   Using a procedural approach
 // ---------------------------------------------
 
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.text.NumberFormat;
 
 public class AccountsProcedural extends JApplet implements NewInterface {
 
    final int CHECKING = 0,
            SAVINGS = 1,
            CD = 2;
     final int FIELD_WIDTH = 12;
     JTextField balance_field = new JTextField(FIELD_WIDTH);
     String[] s = {"Checking", "Savings", "CD"};
     JButton[] b = new JButton[s.length];
 
     // =================
     // Create components
     // =================
     public void init() {
         Container c = getContentPane();
 
         c.setLayout(new FlowLayout());
         // --------------------------------------------
         // Add JLabel & JTextField Components to applet
         // --------------------------------------------
         c.add(new JLabel("Enter balance"));
         c.add(balance_field);
         // ------------------------------------------------------------------
         // Create JButton Components, make each a listener, and add to applet
         // ------------------------------------------------------------------
         for (int i = 0; i < s.length; i++) {
             b[i] = new JButton(s[i]);
             b[i].addActionListener(this);
             c.add(b[i]);
         }
     }
 
     // ===============
     // Process Buttons
     // ===============
     public void actionPerformed(ActionEvent e) {
        final double CHECKING_RATE = 0.01,
                SAVINGS_RATE = 0.02,
                CD_RATE = 0.03;
         double balance;
 
         if (e.getSource() == b[CHECKING]) {
             if ((balance = getNewBalance()) > 0.0) {
                 BankAccount checking = new BankAccount(CHECKING, CHECKING_RATE, balance);
 
                 interest(checking);
             }
         } else if (e.getSource() == b[SAVINGS]) {
             if ((balance = getNewBalance()) > 0.0) {
                 BankAccount savings = new BankAccount(SAVINGS, SAVINGS_RATE, balance);
 
                 interest(savings);
             }
         } else if (e.getSource() == b[CD]) {
             if ((balance = getNewBalance()) > 0.0) {
                 BankAccount savings = new BankAccount(CD, CD_RATE, balance);
 
                 interest(savings);
             }
         }
     }
 
     // ===========================
     // Get balance from text field
     // ===========================
     private double getNewBalance() {
         String b_string = balance_field.getText();
         double balance = (b_string.length() == 0) ? 0.0 : Double.parseDouble(b_string);
 
         if (balance <= 0.00) {
             showStatus("Entered balance must be greater than $0.00");
         }
         return balance;
     }
 
     // ============================================
     // Choose account specific interest calculation
     // ============================================
     private void interest(BankAccount ba) {
         NumberFormat money = NumberFormat.getCurrencyInstance();
 
         switch (ba.account_type) {
             case CHECKING:
                 checkingInterest(ba);
                 showStatus(s[ba.account_type] + " account balance = "
                         + money.format(ba.balance));
                 break;
             case SAVINGS:
                 savingsInterest(ba);
                 showStatus(s[ba.account_type] + " account balance = "
                         + money.format(ba.balance));
                 break;
             case CD:
                 cdInterest(ba);
                 showStatus(s[ba.account_type] + " account balance = "
                         + money.format(ba.balance));
                 break;
         }
     }
 
     // ==============================================
     // Checking account specific interest calculation
     // ==============================================
     private void checkingInterest(BankAccount ba) {
         final double MIN_BAL = 100.0,
                 PENALTY = 10.0;
 
         ba.balance *= 1.0 + ba.interest_rate;
         if (ba.balance < MIN_BAL) {
             ba.balance -= ba.balance < PENALTY ? ba.balance : PENALTY;
         }
     }
 
     // =============================================
     // Savings account specific interest calculation
     // =============================================
     private void savingsInterest(BankAccount ba) {
         final double MIN_BAL = 1000.0,
                 PENALTY = 25.0;
 
         ba.balance *= 1.0 + ba.interest_rate;
         if (ba.balance < MIN_BAL) {
             ba.balance -= ba.balance < PENALTY ? ba.balance : PENALTY;
         }
     }
 
     private void cdInterest(BankAccount ba) {
        final double MIN_BAL = 1000.0,
                PENALTY = 25.0;
 
         ba.balance *= 1.0 + ba.interest_rate;
     }
 
 // ================================================
 // This class encapsulates the data associated with
 // a bank account and is not involved in any OOP
 // ================================================
     class BankAccount {
 
         int account_type;
         double interest_rate,
                 balance;
 
         public BankAccount(int t, double i, double b) {
             account_type = t;
             interest_rate = i;
             balance = b;
         }
     }
 } // AccountsProcedural
 
