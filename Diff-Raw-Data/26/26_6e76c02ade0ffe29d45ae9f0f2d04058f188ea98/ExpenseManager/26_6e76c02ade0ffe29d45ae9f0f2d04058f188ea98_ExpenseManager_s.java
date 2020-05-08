 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Presentation;
 
 import eapli.ExpenseBootstarpper;
 import eapli.ExpenseTypeBootStrapper;
 import eapli.PaymentMeansBootStrapper;
 import eapli.*;
 
 /**
  *
  * @author
  */
 public class ExpenseManager {
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         
         
         ExpenseTypeBootStrapper bootType = new ExpenseTypeBootStrapper(); 
         PaymentMeansBootStrapper bootMean=new PaymentMeansBootStrapper();
         ExpenseBootstarpper boot = new ExpenseBootstarpper();
        IncomeBootstrapper bootIncome = new IncomeBootstrapper();
        IncomeTypeBootstrapper bootinctype = new IncomeTypeBootstrapper();
         
         
         MainMenu menu = new MainMenu();
         menu.mainLoop();
     }
 }
