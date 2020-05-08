 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Persistence;
 import Model.*;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.List;
 /**
  *
  * @author losa
  */
 public class ExpenseRepository  implements IExpenseRepository
 {
     // class member
    private static List<Expense> listExpense= new ArrayList<Expense>();
 
    public ExpenseRepository() {}
     
     public void save(Expense exp)
     {
         if (exp==null) throw new IllegalArgumentException();
         listExpense.add(exp);
       
     }
     public List<Expense> getAllExpenses(){
         return listExpense;
     }
     
     public List<Expense> getExpenses(int month, int year){
         
         List<Expense> expenseList = new ArrayList<Expense>();
         
         for(int i = 0; i < listExpense.size(); i++){
             if(listExpense.get(i).getExpenseMonth() == month && listExpense.get(i).getExpenseYear() == year)
                 expenseList.add(listExpense.get(i));
         }
         
         return expenseList;
     }
     
     /**
      * Retrieves an arraylist containing the expenses recorded in a given week and year
      * @param week (yearly week, ranging from 1 to 52; e.g., January 1st is 1)
      * @param year (e.g., 2012)
      * @return arraylist as described
      */
     public List<Expense> getCurrentWeekExpenses(int week, int year)
     {
         /* creates new list where current week expenses are added */
         List<Expense> tempList = new ArrayList<Expense>();
         
         /* cycles the entire expense list */
         int i = 0;
         for(; i < listExpense.size(); i++)
         {
             /* only current week objects are added */
             if(listExpense.get(i).getExpenseWeek() == week && listExpense.get(i).getExpenseYear() == year)
             {
                 tempList.add(listExpense.get(i));
             }
         }
         return tempList;
     }
 }
