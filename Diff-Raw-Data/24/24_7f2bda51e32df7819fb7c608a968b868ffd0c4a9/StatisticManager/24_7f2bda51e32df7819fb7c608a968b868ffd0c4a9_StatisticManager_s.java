 package com.nttdata.masterthesis.javabackend.manager;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import javax.ejb.EJB;
 import javax.ejb.LocalBean;
 import javax.ejb.Stateless;
 
 import org.joda.time.DateTime;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.nttdata.masterthesis.javabackend.manager.exceptions.ForbiddenException;
 import com.nttdata.masterthesis.javabackend.ressource.TransactionDTO;
 
 /**
  * Statistic Manager creates statistic values from content.
  * @author MATHAF
  */
 @Stateless
 @LocalBean
 public class StatisticManager
 {
     /**
      * Logger Object.
      */
     public static final Logger LOG = LoggerFactory.getLogger( StatisticManager.class );
 
     @EJB
     private TransactionManager transactionManager;
 
     public Map<String, Float> getTransactionAmountPerWeekOfYear( String user,
                                                                  Long bankAccountId ) throws ForbiddenException
     {
         List<TransactionDTO> transactions = transactionManager.getTransactionList( user, bankAccountId );
         Map<String, Float> statistics = new HashMap<String, Float>();
 
         for ( TransactionDTO transaction : transactions )
         {
             DateTime billingDate = new DateTime( transaction.getBillingDate() );
             String weekText = Integer.toString( billingDate.getWeekOfWeekyear() );
             Float currentAmount = statistics.get( weekText );
             if ( currentAmount == null )
             {
                 currentAmount = 0f;
             }
             currentAmount += transaction.getAmount();
             statistics.put( weekText, currentAmount );
         }
 
         return statistics;
     }
 
     public Map<String, Float> getTransactionByCategory( String user,
                                                                 Long bankAccountId ) throws ForbiddenException
     {
         List<TransactionDTO> transactions = transactionManager.getTransactionList( user, bankAccountId );
         Map<String, Float> statistics = new HashMap<String, Float>();
 
         for ( TransactionDTO transaction : transactions )
         {
             String categoryText = transaction.getCategoryName();
             Float currentAmount = statistics.get( categoryText );
             if ( currentAmount == null )
             {
                 currentAmount = 0f;
             }
            currentAmount += Math.abs( transaction.getAmount());
             statistics.put( categoryText, currentAmount );
         }
 
         return statistics;
     }
 }
