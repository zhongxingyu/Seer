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
     * shows the expenses of the current week
      */
     public BigDecimal showWeeklyExpenses()
     {
        
        List<Expense> rep = ExpenseRepository.getInstance().getAllExpenses();
        
        /* saves current year and month */
        int week = DateTime.currentWeekNumber();
        int year = DateTime.currentYear();
         /* variable to sum up all weekly expenses */
         BigDecimal weeklyExpense = new BigDecimal(0);
        /* cycles the entire repository */
        int i = 0;
        for(; i < rep.size(); i++)
        {
            /* filters only for the current week */
            if(rep.get(i).getExpenseYear() == year && rep.get(i).getExpenseWeek() == week)
            {
                /* adds item expense to weekly expense variable */
                weeklyExpense = weeklyExpense.add(rep.get(i).getAmount());
            }
        }
         /* returns weekly expense */
         return weeklyExpense;
     }
     
 }
