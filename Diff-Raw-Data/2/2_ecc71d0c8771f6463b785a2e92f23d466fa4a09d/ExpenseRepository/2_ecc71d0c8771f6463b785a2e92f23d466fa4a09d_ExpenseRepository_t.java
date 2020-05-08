 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Persistence.inMemory;
 
 import Model.*;
import Persistence.Interfaces.IExpenseRepository;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Classe Repositorio em memoria de despesas
  *
  * @autor 1110186 & 1110590
  */
 public class ExpenseRepository implements IExpenseRepository {
 
     private static List<Expense> listExpense = new ArrayList<Expense>();
 
     public ExpenseRepository() {
     }
 
     @Override
     public void save(Expense exp) {
         if (exp == null) {
             throw new IllegalArgumentException();
         }
         listExpense.add(exp);
     }
 
     
     public Expense findLast() {
 
         if (listExpense.isEmpty()) {
             System.out.println(" No Expense recorded!");
             return null;
         } else {
             Expense exp = listExpense.get(listExpense.size() - 1);
             return exp;
         }
     }
 
     public List<Expense> getList() {
         List<Expense> aExp = listExpense;
         return aExp;
     }
     
 }
