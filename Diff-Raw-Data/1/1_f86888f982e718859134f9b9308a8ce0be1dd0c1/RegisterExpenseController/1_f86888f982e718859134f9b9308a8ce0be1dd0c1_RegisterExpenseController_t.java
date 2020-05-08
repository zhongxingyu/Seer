 package Controllers;
 
 import Model.Expense;
 import Model.ExpenseType;
 import Model.PaymentMean;
 import Model.RecordExpense;
 import Persistence.ExpenseTypeRepository;
 import Persistence.PaymentMeanRepository;
 import eapli.exception.EmptyList;
 import java.math.BigDecimal;
 import java.util.Date;
 import java.util.List;
 
 /***
  * @author nbento
  * 
  */
 public class RegisterExpenseController extends BaseController {
 
     /**
      * @autor nbento
      * @return List com todos os tipos de despesas
      * @throws IllegalArgumentException, EmptyList
      */
     public List<ExpenseType> getExpenseType() throws IllegalArgumentException, EmptyList {
 
         List<ExpenseType> expenseTypes;
 
         ExpenseTypeRepository expenseTypeRepository = new ExpenseTypeRepository();
 
         //Lista completa dos tipos de despesas
         expenseTypes = expenseTypeRepository.getAllExpenseType();
 
         if (expenseTypes == null) {
             throw new IllegalArgumentException();
         }
 
         if (expenseTypes.isEmpty()) {
             throw new EmptyList("Exception EmptyList");
         }
 
         return expenseTypes;
 
     }
 
     /**
      * @autor nbento
      * @return List com todos os meios de pagamento
      * @throws IllegalArgumentException, EmptyList
      */
     public List<PaymentMean> getPaymentMean() throws IllegalArgumentException, EmptyList {
 
         List<PaymentMean> paymentMeans;
 
         PaymentMeanRepository payMeansRepository = new PaymentMeanRepository();
 
         //Lista completa dos cartões de debito
         paymentMeans = payMeansRepository.getAllPaymentMean();
 
         if (paymentMeans == null) {
             throw new IllegalArgumentException();
         }
 
         if (paymentMeans.isEmpty()) {
             throw new EmptyList("Exception EmptyList");
         }
 
         return paymentMeans;
     }
 
     /**
      * @autor nbento
      * @return 
      */
     public void createExpense(String what, Date date, BigDecimal amount, ExpenseType expType, PaymentMean pM) {
 
         Expense expense = new Expense(what, date, amount, expType, pM);
         
         //Lancar exception
         if(expense!=null){
             RecordExpense repo = new RecordExpense();
             repo.register(expense);
         } else {
             throw new IllegalArgumentException();
         }
     }
 }
