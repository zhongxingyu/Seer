 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Persistance;
 
 import Model.Expense;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  *
  * @author mcn
  */
public class ExpenseRepository {
       
       // class member
     private static List<Expense> listExpense= new ArrayList<>();
 
     public ExpenseRepository() {}
     
    
     public void saveExpense(Expense exp)
     {
         if (exp==null) {
 			throw new IllegalArgumentException();
         }
         listExpense.add(exp);
       
     }
 }
