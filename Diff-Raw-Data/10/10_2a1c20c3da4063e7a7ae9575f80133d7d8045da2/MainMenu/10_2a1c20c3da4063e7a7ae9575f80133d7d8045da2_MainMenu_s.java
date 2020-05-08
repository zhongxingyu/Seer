 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Presentation;
 
 import Controllers.BaseController;
 import Model.ExpenseType;
 import eapli.util.Console;
 
 /**
  *
  * @author Paulo Gandra Sousa
  */
 public class MainMenu{
 
 
     public void doShow() {
       int option=0; 
       do{
           
          
         /*System.out.println("----------------------------------------");
         System.out.println("--------------BALANCE-------------------");
         showBalance();*/
          
         System.out.println("===================");
         System.out.println("  EXPENSE MANAGER  ");
         System.out.println("===================\n");
 
         System.out.println("1. Register an expense");
         System.out.println("2. Add Expense Type");
         System.out.println("3. Register an Expense with Expense Type");
         System.out.println("4. Show expenses");
         System.out.println("5. List Expense Types");
         System.out.println("6. Add Payment Mean");
         System.out.println("7. List Payment Means");
         System.out.println("0. Exit\n\n");
         
        
        

         option = Console.readInteger("Please choose a option");
         switch (option) {
             case 0:
                 System.out.println("bye bye ...");
                 return;
             case 1:
                 ExpenseRegisterUI ui = new ExpenseRegisterUI();
                 ui.mainLoop();
                 break;
             case 2:
                 RegisterExpenseTypeUI uitype = new RegisterExpenseTypeUI();
                 uitype.mainLoop();
                 break;
             case 3:
                 RegisterExpenseTypeUI uitypes = new RegisterExpenseTypeUI();
                 uitypes.mainLoop();
                 break;
             case 4:
                 ExpensesUI exp = new ExpensesUI();
                 exp.doShow();
                 break;
             case 5:
                  ListExpenseTypeUI listexp = new ListExpenseTypeUI();
                  listexp.mainLoop();
                  break;
             case 6:
                 new RegisterPaymentMeanUI().mainLoop();
                 break;
             case 7:
                 new ListPaymentMeanUI().mainLoop();
                 break;
         }
       }while(option!=0);
     }
 
 
     
 }
  
