 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Controllers;
 
 import Model.Expense;
 import Model.ExpenseType;
 import Persistence.ExpenseRepository;
 import Persistence.ExpenseTypeRepository;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  *
  * @author i110557
  */
 public class DisplayExpensesController {
 
     public List<Expense> showMonthlyExpenses(int year, int month){
         
        List<Expense> repo = ExpenseRepository.getInstance().getAllExpenses();
        List<ExpenseType> typerepo=ExpenseTypeRepository.getInstance().getTypeRep();      
         List<Expense> display=new ArrayList<Expense>();
         for (int i = 0; i < typerepo.size(); i++) { //List of ExpenseTypes
             for (int j = 0; j <repo.size(); j++) { //List of Expenses
                if(repo.get(j).compareyear(year) && repo.get(j).comparemonth(month+1)){ 
                      if( typerepo.get(i).getDescription().equals(repo.get(j).getExptype().getDescription()))
                      {
                         display.add(repo.get(j));
                      }
                 }
             }
         }
         return display;
     }
  }
