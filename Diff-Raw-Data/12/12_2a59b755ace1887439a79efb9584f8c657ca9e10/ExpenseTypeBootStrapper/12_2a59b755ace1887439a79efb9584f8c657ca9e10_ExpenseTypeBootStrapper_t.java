 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package eapli;
 
 import Model.ExpenseType;
 import Persistence.IExpenseTypeRepository;
import Persistence.PersistenceFactory;
 
 /**
  *
  * @author Pedro
  */
 public class ExpenseTypeBootStrapper {

    static {
        IExpenseTypeRepository repo = PersistenceFactory.getInstance().buildRepositoryFactory().getExpenseTypeRepository();

         repo.defineExpenseType(new ExpenseType("vestuário"));
         repo.defineExpenseType(new ExpenseType("refeições"));
         repo.defineExpenseType(new ExpenseType("transportes"));
         repo.defineExpenseType(new ExpenseType("lazer"));
     }
 }
