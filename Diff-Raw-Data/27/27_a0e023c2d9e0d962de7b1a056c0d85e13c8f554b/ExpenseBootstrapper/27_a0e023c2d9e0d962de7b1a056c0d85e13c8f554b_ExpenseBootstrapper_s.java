 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package eapli.bootstrap;
 
 import Model.Expense;
 import Model.PaymentMean;
 import Persistence.ExpenseRepository;
 import Persistence.ExpenseTypeRepository;
 import Model.ExpenseType;
 import Persistence.PaymentMeanRepository;
 import java.math.BigDecimal;
 
 /**
  *
  * @author Paulo Gandra Sousa
  */
 public class ExpenseBootstrapper {
 
     static {
         ExpenseRepository repo = new ExpenseRepository();
        repo.save(new Expense("Autocarro", 2013, 4, 9, new BigDecimal(2.4), new PaymentMean("Teste Autocarro", "money") ));
        repo.save(new Expense("Almoço", 2013, 3, 5, new BigDecimal(12.4),new PaymentMean("Teste Almoço","money")));
        repo.save(new Expense("Sapatilhas", 2013, 3, 4, new BigDecimal(123.4), new PaymentMean("Teste Sapatilhas","money")));
        repo.save(new Expense("Cinema", 2013, 3, 4, new BigDecimal(5.0), new PaymentMean("Teste Cinema","money")));
    
        PaymentMeanRepository repo2=new PaymentMeanRepository();
        repo2.save(new PaymentMean("Teste Cartao Debito","Cartao Debito"));
        repo2.save(new PaymentMean("Teste Cartao Credito","Cartao Credito"));
        repo2.save(new PaymentMean("Teste Dinheiro","Dinheiro"));
        
        ExpenseTypeRepository repo3=new ExpenseTypeRepository();
         repo3.save(new ExpenseType("Despesas de alimentação", "Alimentação"));
         repo3.save(new ExpenseType("Despesas de transportes", "Transportes"));
    
     }
 }
