 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Controllers;
 
 import Model.Expense;
 import Model.ExpenseType;
 import Model.PaymentMeans;
 import Persistence.IExpenseRepository;
 import Persistence.IPaymentMeansRepository;
 import Persistence.PersistenceRegistry;
 import java.math.BigDecimal;
 import java.util.Date;
 import Persistence.PersistenceFactory;
 import java.util.List;
 
 /**
  *
  * @author 
  */
 public class ExpenseRegisterController extends BaseController{
     
     public ExpenseRegisterController() {
     }
     
     public void registerExpense(String desc, Date date, BigDecimal amount, ExpenseType exptype, String comment, PaymentMeans pay) {
             Expense expense = new Expense(desc, date, amount, exptype, comment, pay);
             IExpenseRepository repo = PersistenceFactory.getInstance().buildRepositoryFactory().getExpenseRepository();
             repo.saveExpense(expense);
     }
     
     public List<ExpenseType> ListAllTypes()
     {
        return PersistenceRegistry.getInstance().expenseTypeRepository().ListAllTypes();
     }
     
     public List<PaymentMeans> ListAllMeans()
     {
         IPaymentMeansRepository repo = PersistenceFactory.getInstance().buildRepositoryFactory().getPaymentMeansRepository();
         return repo.getAllMeans();
     }
 }
