 package Presentation;
 
 import Controllers.BaseController;
 import Controllers.DisplayExpensesController;
 import Model.Expense;
 import eapli.util.Console;
 import java.math.BigDecimal;
 import java.util.List;
 
 /**
  * User interface displaying the available options within "Display Expense"
  * @author João Carreira
  */
 public class DisplayExpensesUI extends BaseUI {
     
     private BaseController controller = new BaseController();
     
     @Override
     public BaseController controller(){
         return controller;
     }
 
     @Override
     public void show() {
         headline();
         System.out.println(controller.showWeeklyExpenses()+"            |||            "+controller.getCurrentMonthExpenses());
         mainLoop();
     }
     
     @Override
     protected void headline() {
         System.out.println("===================");
         System.out.println("  Display Expenses  ");
         System.out.println("===================\n");
     }
 
     @Override
     public void mainLoop(){
         int option,option1,option2;
         DisplayExpensesController dec= new DisplayExpensesController();
         BaseController baseCont = new BaseController();
         BigDecimal sum = BigDecimal.ZERO;
         do{
             
             System.out.println("1. Monthly expenses");
             System.out.println("2. Weekly expenses");
             System.out.println("3. Monthly Expenses Value");
             System.out.println("0. Go back\n\n");
             option = Console.readInteger("Please choose an option");
             switch(option){
                 case 1:
                     option1 = Console.readInteger("Which Year?");
                    option2 = Console.readInteger("Which Month(0-January until 11-December)?");
                     System.out.println(option1);
                     System.out.println("Year: " + option1 + "  Month: " + (option2+1));
                     showMonthlyExpenses(option1,option2);
                     sum = baseCont.getMonthlyExpenses(option2, option1);
                     
                     System.out.println("Monthly Expense: " + sum);
                     break;
                 case 2:
                     /* replace the following code for appropriate function once it's implemented */
                     UnavailableFunctionUI un2 = new UnavailableFunctionUI();
                     un2.mainLoop();
                     break;
                 case 3:
                     option1 = Console.readInteger("Which Year?");
                    option2 = Console.readInteger("Which Month(0-January until 11-December)?");
                     sum = baseCont.getMonthlyExpenses(option2, option1);
                     System.out.println(option1);
                     System.out.println("Year: " + option1 + "  Month: " + option2);
                     System.out.println("Monthly Expense: " + sum.setScale(2));
                 case 0:
                     /* returns to previous menu */
                     return;
                 default:
                     System.out.println("Unknown option\nPlease try again!");
             } // end switch
         } while(option != 0);
     }
     
        public void showMonthlyExpenses(int option1, int option2){ 
         DisplayExpensesController dec= new DisplayExpensesController();
         List<Expense> display;
         display=dec.showMonthlyExpenses(option1, option2);
         System.out.println("-------------------------------------------");
         for (int i = 1; i < display.size(); i++) {
               System.out.println(display.get(i).toString());
         }
         System.out.println("-------------------------------------------");
     }
       
 
 }
 
     
