 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Controllers;
 
 import Model.ChekingAccount;
 import Persistence.ChekingAccountRepository;
 import Persistence.PersistenceFactory;
 import java.math.BigDecimal;
 
 /**
  *
  * @author i111051 && raul lima
  */
 public class BasicController {
    private ChekingAccountRepository pConta= 
             PersistenceFactory.buildPersistenceFactory().chekingAccountRepository();
     
     public BasicController(){}
     
     public BigDecimal currentMonthExpenses()
     {
        if (pConta.GetChekingAccount() == null) {
             return( (new ChekingAccount()).getMonthExpenses());
         }
        return pConta.GetChekingAccount().getMonthExpenses();
         
     }
     
     public BigDecimal currentWeekExpenses()
     {
        if (pConta.GetChekingAccount() == null)
             return( (new ChekingAccount()).getWeekExpenses());
        return pConta.GetChekingAccount().getWeekExpenses();
     }
     
     public Double currentBalance() 
     { 
        if (pConta.GetChekingAccount() == null)
             return( (new ChekingAccount()).getSaldo());
        return pConta.GetChekingAccount().getSaldo();
     }
 }
