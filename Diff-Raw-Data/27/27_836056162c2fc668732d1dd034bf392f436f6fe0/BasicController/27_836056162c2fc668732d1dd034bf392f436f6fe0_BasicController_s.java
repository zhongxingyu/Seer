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
    public ChekingAccountRepository conta= 
             PersistenceFactory.buildPersistenceFactory().chekingAccountRepository();
     
     public BasicController(){}
     
     public BigDecimal currentMonthExpenses()
     {
        if (conta.GetChekingAccount() == null) {
             return( (new ChekingAccount()).getMonthExpenses());
         }
        return conta.GetChekingAccount().getMonthExpenses();
         
     }
     
     public BigDecimal currentWeekExpenses()
     {
        if (conta.GetChekingAccount() == null)
             return( (new ChekingAccount()).getWeekExpenses());
        return conta.GetChekingAccount().getWeekExpenses();
     }
     
     public Double currentBalance() 
     { 
        if (conta.GetChekingAccount() == null)
             return( (new ChekingAccount()).getSaldo());
        return conta.GetChekingAccount().getSaldo();
     }
 }
