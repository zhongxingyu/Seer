 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Presentation;
 
 import eapli.util.Console;
 /**
  *
  * @author mcn
  */
 class MainMenu {
 
       public MainMenu() {
       }
        public void mainLoop() {
             int op;
             do {
                   op = menu();
                   switch (op) {
                         case 0:
                               System.out.println("bye bye ...");
                               break;
                         case 1:
                               ExpenseRegisterUI ui = new ExpenseRegisterUI();
                               ui.run();
                               break;
                       case 2:
                               ShowExpensesUI showExpensesUI = new ShowExpensesUI();
                               showExpensesUI.loop();
                               break;

                           
                      case 3:
                               PaymentMeansUI pmUI = new PaymentMeansUI();
                               pmUI.run();
                               break;

                       case 4:
                           ShowTotalExpensesUI showTotalExpensesUI = new ShowTotalExpensesUI();
                           showTotalExpensesUI.loop();
                           break;

                   }
             } while (op != 0);
 
       }
              private  int menu() {
             System.out.println("===================");
             System.out.println("  EXPENSE MANAGER  ");
             System.out.println("===================\n");
             System.out.println("1. Register an expense");
             System.out.println("2. Show expenses");
            //System.out.println("3. Register an expense type");
            System.out.println("3. Register a payment mean");
             System.out.println("4. Show Total Expenses by month");
             System.out.println("0. Exit\n\n");
 
             int option = Console.readInteger("Please choose an option");
             return option;
       }
 }
 
 
