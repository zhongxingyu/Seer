 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package bankproject;
 
 /**
  *
  * @author s504
  */
 public class BankProject {
 
     public AccountManager accountManager = new AccountManager();
     public OperationManager operationManager = new OperationManager();
     public Vault bankVault;
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         BankProject bankProject = new BankProject();
         // TODO code application logic here
         //populate bank 
         for (int i=0; i < 10; i++) {
            // Object options = [];
             AccountHolder object = new AccountHolder();
             object.name = "Ivan" + i;
             object.ballance = i * 10.00;
             bankProject.accountManager.newAccount(object);
         }
         
         //get element at 3 position
         AccountHolder element3 = bankProject.accountManager.getAccount(3);
         System.out.println(element3.name + " " + element3.ballance);
         
        //deposit money for 5
         bankProject.operationManager.deposit(element3, 65.44);
         
         //get element at 3 position
         element3 = bankProject.accountManager.getAccount(3);
         System.out.println(element3.name + " " + element3.ballance);
         
         System.out.println(Vault.showVault());
     }
 }
