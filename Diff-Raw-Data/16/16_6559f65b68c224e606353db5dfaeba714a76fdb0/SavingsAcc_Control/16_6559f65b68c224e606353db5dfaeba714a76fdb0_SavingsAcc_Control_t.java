 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Beans;
 
 import EJB_Data_Access.RDBSavingsDAO;
 import EJB_Data_Access.RDBTransactionsDAO;
 import EJB_Data_Access.Savings;
 import EJB_Data_Access.SavingsDAO;
 import EJB_Data_Access.Transactions;
 import EJB_Data_Access.TransactionsDAO;
 import java.io.Serializable;
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.text.DecimalFormat;
 import javax.annotation.PostConstruct;
 import javax.annotation.PreDestroy;
 import javax.annotation.Resource;
 import javax.ejb.Stateless;
 import javax.sql.DataSource;
 
 /**
  *
  * @author chngbaron
  */
 @Stateless
 public class SavingsAcc_Control implements SavingsAcc_ControlRemote, Serializable{
 
     @Resource(lookup = "jdbc/ACME_DB_Datasource") 
     private DataSource dataSource;    
     private Connection connection;
     
     @PostConstruct
     public void initialize() 
     { 
         try 
         {
            connection = dataSource.getConnection();
         } 
         catch(SQLException sqle) 
         {
            sqle.printStackTrace(); 
         }
     }
     
     @PreDestroy
     public void close() 
     { 
         try 
         {
            connection.close();
         } 
         catch (SQLException sqle) 
         {
            sqle.printStackTrace(); 
         }
     }
     
     @Override
     public boolean createSavings(int c_id, double balance) {
         try {
             SavingsDAO dao = new RDBSavingsDAO(connection);
             Savings sav_acc = new Savings(c_id, Math.round(balance*100.0)/100.0);
             
             if (!dao.createSavings(sav_acc)) {
                 System.out.println("Customer already has more than 2 savings account.");
                 return false;
             }
         } catch (Exception e) {
             System.out.println("Could not create Savings Account.");
             return false;
         }
         return true;
     }
 
     @Override
     public boolean deposit(int acc_num, double amount) {
         try {
             SavingsDAO dao = new RDBSavingsDAO(connection);
             Savings sav_acc = dao.readSavings(acc_num);
             
             double updateAmount = Math.round((sav_acc.getBalance() + amount)*100.0)/100.0;
             sav_acc.setBalance(updateAmount);
             dao.updateSavings(sav_acc);
             
             TransactionsDAO t_dao = new RDBTransactionsDAO(connection);
            Transactions trans = new Transactions(acc_num, amount, "Deposit $"+amount);
             t_dao.createTransactions(trans);
             
         } catch (Exception e) {
             System.out.println("Could not deposit to account: "+acc_num);
             return false;
         }
         
         return true;
     }
 
     @Override
     public boolean withdraw(int acc_num, double amount) {
         try {
             SavingsDAO dao = new RDBSavingsDAO(connection);
             Savings sav_acc = dao.readSavings(acc_num);
             
             // balance is not enough to be withdrew
             if (sav_acc.getBalance() < amount) {
                 return false;
             }
             
             Double updateAmount = Math.round((sav_acc.getBalance() - amount)*100.0)/100.0;
             sav_acc.setBalance(updateAmount);
             
             dao.updateSavings(sav_acc);
             
             TransactionsDAO t_dao = new RDBTransactionsDAO(connection);
             Transactions trans = new Transactions(acc_num, updateAmount, "Withdraw $"+updateAmount);
             t_dao.createTransactions(trans);
             
         } catch (Exception e) {
             System.out.println("Could not withdraw from account: "+acc_num);
             return false;
         }
         
         return true;
     }
 
     @Override
     public String viewBal(int acc_num) {
         
         String balMessage = "";
         DecimalFormat df = new DecimalFormat( "#,###,###,##0.00" );
         
         try {
             SavingsDAO dao = new RDBSavingsDAO(connection);
             Savings sav_acc = dao.readSavings(acc_num);
             String bal = df.format(sav_acc.getBalance());
             balMessage = "Acc No.:\t"+sav_acc.getAccNum()+
                     "\nBalance:\t"+bal;
 
         } catch (Exception e) {
             return ("Unable to view the balance of account: "+acc_num);
         }
         
         return balMessage;
     }
     
     @Override
     public String getSavingsAcc(int c_id) {
         String savAccNum = "";
         
         try {
             SavingsDAO dao = new RDBSavingsDAO(connection);
             savAccNum = dao.getSavingsAcc(c_id);
 
         } catch (Exception e) {
             return ("Unable to get the Savings account numbers of: "+c_id);
         }
         
         return savAccNum;
     }
 }
