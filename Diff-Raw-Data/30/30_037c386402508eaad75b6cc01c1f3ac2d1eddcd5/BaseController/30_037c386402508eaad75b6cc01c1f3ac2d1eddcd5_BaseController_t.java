 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Controllers;
 
 import Model.*;
 import Persistence.*;
 import eapli.util.DateTime;
 import java.math.BigDecimal;
 import java.util.List;
 
 /**
  *
  * @author i100596 
  * @author i100570
  */
 public class BaseController {
     
     public BaseController(){
     }
     
     public BigDecimal getMonthlyExpenses(int month, int year){
         
         List<Expense> listExpense;
         BigDecimal sum;
         
         listExpense = ExpenseRepository.getInstance().getExpenses(month, year);
         
         ExpenseRecord expRec = new ExpenseRecord();
         
         sum = expRec.getSumExpenses(listExpense);
         
         return sum;
     }
     
     /**
      *@author i110455
      *@author i110295
      *@author i110637
      */
     
         public BigDecimal getCurrentMonthExpenses(){
     
         DateTime date = new DateTime();
         List<Expense> listExpense;
         BigDecimal sum;
         
         listExpense = ExpenseRepository.getInstance().getExpenses(date.currentMonth(), date.currentYear());
         
         ExpenseRecord expRec = new ExpenseRecord();
         
         sum = expRec.getSumExpenses(listExpense);
         
         return sum;
     
     }
     
     /**
     * calculates the sum of the current week's expenses
     * @return sum of current week's expense
      */
     public BigDecimal showWeeklyExpenses()
     {
        /* expense list with all expenses */
        List<Expense> listExpense;
         /* variable to sum up all weekly expenses */
         BigDecimal weeklyExpense = new BigDecimal(0);
        /* gets a list with current week's expenses */
        listExpense = ExpenseRepository.getInstance().getCurrentWeekExpenses(DateTime.currentWeekNumber(), DateTime.currentYear());
        /* new Expense Record */
        ExpenseRecord expRec = new ExpenseRecord();
        /* adds up all values of the list */
        weeklyExpense = expRec.getSumExpenses(listExpense);
         /* returns weekly expense */
         return weeklyExpense;
     }
     
 }
