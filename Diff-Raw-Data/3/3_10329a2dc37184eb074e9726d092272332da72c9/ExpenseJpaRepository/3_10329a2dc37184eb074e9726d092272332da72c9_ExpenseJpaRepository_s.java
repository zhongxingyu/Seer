 package Persistence.Jpa;
 
 import Model.Expense;
 import Model.ExpenseType;
 import Persistence.IExpenseRepository;
 import java.util.List;
 import javax.persistence.EntityManager;
 
 /**
  *
  * @author i060752
  */
 public class ExpenseJpaRepository extends JpaGeneric<Expense, Integer> implements IExpenseRepository {
     
       @Override
       public void saveExpense(Expense exp){
             save(exp);
       }
 
       @Override
       public List<Expense> getAllExpenses(){
             return super.all(); //ou apenas return all();
       }
       
       @Override
       public Expense getLastExpense(){
            return null;
       }
       
       @Override
       public ExpenseType getByDescription(String description){
           EntityManager em=getEntityManager();
           //Query q=
           return null;
       }
 }
