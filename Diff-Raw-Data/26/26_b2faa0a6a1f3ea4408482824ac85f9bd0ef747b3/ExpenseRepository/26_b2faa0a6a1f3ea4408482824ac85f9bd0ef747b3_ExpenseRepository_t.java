 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Persistance;
 
 import Model.Expense;
 import java.util.ArrayList;
import java.util.Collections;
 import java.util.List;
 
 /**
  *
  * @author mcn
  */
public class ExpenseRepository implements IExpenseRepository{
       
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
    
    public List<Expense> getAllExpenses() {
        return Collections.unmodifiableList(listExpense); // Retorna uma cópia da lista original
    }

    public Expense getLastExpense() {
        Expense exp = null;
        Expense copy = null;
        
        int size = listExpense.size();
        
        if(size>0){
            exp=listExpense.get(size-1);
        }
        
        if(exp!=null){
            copy = new Expense(exp);
        }
        
        return copy;
    }
 }
