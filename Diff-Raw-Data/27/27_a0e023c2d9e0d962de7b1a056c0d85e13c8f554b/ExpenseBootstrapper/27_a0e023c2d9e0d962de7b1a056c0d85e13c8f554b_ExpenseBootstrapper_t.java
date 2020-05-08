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
import java.util.Date;
 
 /**
  *
  * @author Paulo Gandra Sousa
  */
 public class ExpenseBootstrapper {
 
     static {
         ExpenseRepository repo = new ExpenseRepository();
        repo.save(new Expense("Autocarro", new Date(2013, 4, 9), new BigDecimal(2.4), new ExpenseType("Despesas de transportes", "Transportes"), new PaymentMean("Teste Autocarro", "money")));
        repo.save(new Expense("Almoço", new Date(2013, 3, 5), new BigDecimal(12.4), new ExpenseType("Despesas de alimentação", "Alimentação"), new PaymentMean("Teste Almoço", "money")));
        repo.save(new Expense("Sapatilhas", new Date(2013, 3, 4), new BigDecimal(123.4), new ExpenseType("Despesas de vestuario", "Vestuario"), new PaymentMean("Teste Sapatilhas", "money")));
        repo.save(new Expense("Cinema", new Date(2013, 3, 4), new BigDecimal(5.0), new ExpenseType("Despesas de lazer", "Lazer"), new PaymentMean("Teste Cinema", "money")));

        PaymentMeanRepository repo2 = new PaymentMeanRepository();
        repo2.save(new PaymentMean("Teste Cartao Debito", "Cartao Debito"));
        repo2.save(new PaymentMean("Teste Cartao Credito", "Cartao Credito"));
        repo2.save(new PaymentMean("Teste Dinheiro", "Dinheiro"));

        ExpenseTypeRepository repo3 = new ExpenseTypeRepository();
         repo3.save(new ExpenseType("Despesas de alimentação", "Alimentação"));
         repo3.save(new ExpenseType("Despesas de transportes", "Transportes"));

     }
 }
