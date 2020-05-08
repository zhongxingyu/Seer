 package org.jz.services;
 
 import org.apache.commons.lang.Validate;
 
 import org.jz.domain.CreditRanking;
 import org.jz.domain.Customer;
 import org.jz.domain.CustomerId;
 import org.jz.domain.Order;
 import org.jz.domain.Product;
 import org.jz.persistence.AccountDao;
 
 /**
  * @author Kristian Rosenvold
  */
 public class AccountService
 {
 
     private final AccountDao accountDao = new AccountDao();
 
     public Order createOrder( CustomerId authenticatedCustomer )
     {
        Validate.notNull( authenticatedCustomer );
         return new Order();
     }
 
     private String getCreditRankingFromExternalSource( Customer customer )
     {
         Validate.notNull( customer );
         return "AAA";
     }
 
     public void orderItem( Order order, Product product )
     {
         Validate.notNull( order );
         Validate.notNull( product );
     }
 
     public int getMaxItemsForOrder( CustomerId customerId )
     {
         Validate.notNull( customerId );
         return 17;
     }
 
     public int getCreditLimit( CustomerId Id, int systemMaxCredit )
     {
         final int creditLimit = accountDao.getCreditLimit( Id );
         return creditLimit > systemMaxCredit ? systemMaxCredit : creditLimit;
     }
 
 
    public CreditRanking getCreditRanking( Customer customer )
     {
         boolean isPrivateCustomer = !customer.isCompanyCustomer();
        if ( !isPrivateCustomer )
        {
             String creditRanking = getCreditRankingFromExternalSource( customer );
             return CreditRanking.forCompanyCustomer( creditRanking );
        }
        else
        {
             return CreditRanking.forPrivateCustomers();
         }
     }
 
 
 }
