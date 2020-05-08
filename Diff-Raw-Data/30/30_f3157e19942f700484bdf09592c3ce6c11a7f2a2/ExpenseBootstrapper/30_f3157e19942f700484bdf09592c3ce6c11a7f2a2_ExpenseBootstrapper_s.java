 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package eapli.bootstrap;
 
 import Model.Expense;
 import Model.PaymentMean;
import Persistence.inMemory.ExpenseRepositoryImp;
 import Persistence.inMemory.ExpenseTypeRepositoryImp;
 import Model.ExpenseType;
 import Model.Income;
 import Model.IncomeType;
 import Persistence.inMemory.IncomeRepository;
import Persistence.inMemory.PaymentMeanRepository;
 import eapli.exception.InvalidValue;
 import eapli.util.DateTime;
 import java.math.BigDecimal;
import java.util.Date;
 
 /**
  *
  * @author Paulo Gandra Sousa
  */
 public class ExpenseBootstrapper {
 
     static {
         try {
             ExpenseRepository repo = new ExpenseRepository();
             repo.save(new Expense("Autocarro", DateTime.parseDate("5-10-2012"), new BigDecimal(2.4), new ExpenseType("Despesas de transportes", "Transportes"), new PaymentMean("Teste Autocarro", "money")));
             repo.save(new Expense("Almoço", DateTime.parseDate("5-2-2012"), new BigDecimal(12.4), new ExpenseType("Despesas de alimentação", "Alimentação"), new PaymentMean("Teste Almoço", "money")));
             repo.save(new Expense("Sapatilhas", DateTime.parseDate("5-1-2013"), new BigDecimal(123.4), new ExpenseType("Despesas de vestuario", "Vestuario"), new PaymentMean("Teste Sapatilhas", "money")));
             repo.save(new Expense("Cinema", DateTime.parseDate("8-4-2013"), new BigDecimal(5.0), new ExpenseType("Despesas de lazer", "Lazer"), new PaymentMean("Teste Cinema", "money")));
             repo.save(new Expense("Concerto", DateTime.parseDate("21-5-2013"), new BigDecimal(5.0), new ExpenseType("Despesas de lazer", "Lazer"), new PaymentMean("Teste Concerto", "money")));
             repo.save(new Expense("Comboio", DateTime.parseDate("10-5-2013"), new BigDecimal(2.4), new ExpenseType("Despesas de transportes", "Transportes"), new PaymentMean("Teste Comboio", "money")));
             repo.save(new Expense("Jantar", DateTime.parseDate("5-2-2013"), new BigDecimal(19.4), new ExpenseType("Despesas de alimentação", "Alimentação"), new PaymentMean("Teste Jantar", "money")));
             
             IncomeRepository incRepo = new IncomeRepository();
             incRepo.save(new Income("Salario mes Abril",DateTime.parseDate("5-5-2012"),new BigDecimal(2200),new IncomeType("Salario","Salario")));
             incRepo.save(new Income("Salario mes Janeiro",DateTime.parseDate("5-2-2013"),new BigDecimal(1200),new IncomeType("Salario","Salario")));
             incRepo.save(new Income("Salario mes Fevereiro",DateTime.parseDate("5-3-2013"),new BigDecimal(2200),new IncomeType("Salario","Salario")));
             
             PaymentMeanRepositoryImp repo2 = new PaymentMeanRepositoryImp();
             repo2.save(new PaymentMean("Teste Cartao Debito", "Cartao Debito"));
             repo2.save(new PaymentMean("Teste Cartao Credito", "Cartao Credito"));
             repo2.save(new PaymentMean("Teste Dinheiro", "Dinheiro"));
 
             ExpenseTypeRepositoryImp repo3 = new ExpenseTypeRepositoryImp();
             repo3.save(new ExpenseType("Despesas de alimentação", "Alimentação"));
             repo3.save(new ExpenseType("Despesas de transportes", "Transportes"));
             repo3.save(new ExpenseType("Despesas de vestuario", "Vestuario"));
             repo3.save(new ExpenseType("Despesas de lazer", "Lazer"));
         } catch (InvalidValue ex) {
             System.err.println(ex.getMessage());
         }
 
     }
 }
