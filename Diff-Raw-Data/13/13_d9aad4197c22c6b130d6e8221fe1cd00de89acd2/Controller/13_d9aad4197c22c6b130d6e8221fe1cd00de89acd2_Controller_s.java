 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package project.gui;
 
 import project.model.*;
 
 import java.awt.*;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  *
  * @author rich
  */
 public class Controller {
    
    public static void main(String[] args) throws OverdraftException {
         setLookAndFeel();        
         initializeBank();
         newLoginWindow();
     }
     
     public static void closeWindows(List<Window> windows){
         for(Window w : windows){
             w.dispose();
         }
     }
     
     public static void setLookAndFeel(){
         try {
             for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                 if ("Nimbus".equals(info.getName())) {
                     javax.swing.UIManager.setLookAndFeel(info.getClassName());
                     break;
                 }
             }
         } catch (ClassNotFoundException ex) {
             java.util.logging.Logger.getLogger(project.gui.LoginWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (InstantiationException ex) {
             java.util.logging.Logger.getLogger(project.gui.LoginWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (IllegalAccessException ex) {
             java.util.logging.Logger.getLogger(project.gui.LoginWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (javax.swing.UnsupportedLookAndFeelException ex) {
             java.util.logging.Logger.getLogger(project.gui.LoginWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         }
     }
     
     public static void newLoginWindow(){
         java.awt.EventQueue.invokeLater(new Runnable() {
             @Override
             public void run() {
                 project.gui.LoginWindow login = new project.gui.LoginWindow(new javax.swing.JFrame(), true);
                 login.addWindowListener(new java.awt.event.WindowAdapter() {
                     @Override
                     public void windowClosing(java.awt.event.WindowEvent e) {
                         System.exit(0);
                     }
                 });
                 login.setVisible(true);
             }
         });
     }
     
    private static void initializeBank() throws OverdraftException{
         Bank.getInstance().addUser("Bob", new User());
         Account acc = new SavingsAccount();
 		System.out.println(acc.getBalance());
 		acc.deposit(new BigDecimal(100.25));
 		System.out.println(acc.getBalance());
 		acc.withdraw(new BigDecimal(50));
 		System.out.println(acc.getBalance());
 		//acc.doPayments();
 		System.out.println(acc.getBalance());
                 System.out.println(acc.getAccountType());
         Bank.getInstance().getUser("Bob").addAccount(acc);
     }
     
     public static void newAccountHolderFrame(final User user){
         //create Account Holder Window
         java.awt.EventQueue.invokeLater(new Runnable() {
             @Override
             public void run() {
                 List<AccountTab> accounts = new ArrayList<AccountTab>();
                 for(Account acc : user.getAccounts()){
                     Account.Type accType = acc.getAccountType();
                     switch(accType){
                         case SAVINGS_ACCOUNT:
                         case CHECKING_ACCOUNT:
                         case CD_ACCOUNT:
                             accounts.add(newAccountTab(acc));
                             break;
                         case LOAN_ACCOUNT:
                         case LOC_ACCOUNT:
                             //TODO
                             break;
                         default: assert false;
                     }
                 }
                 AccountHolderFrame accountHolder = new AccountHolderFrame(accounts);
                 accountHolder.addWindowListener(new java.awt.event.WindowAdapter() {
                     @Override
                     public void windowClosing(java.awt.event.WindowEvent e) {
                         System.exit(0);
                     }
                 });
                 accountHolder.setVisible(true);
             }
         });
     }
     public static AccountTab newAccountTab(Account account){
         AccountTab accTab = new AccountTab(account);
         accTab.setVisible(true);
         return accTab;
     }
 }
