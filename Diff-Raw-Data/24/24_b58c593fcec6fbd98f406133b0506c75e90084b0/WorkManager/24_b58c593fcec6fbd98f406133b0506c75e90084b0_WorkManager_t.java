 package org.tptp.model;
 
 /**
  * Helper for doing a transaction. Simply pass a {@link java.lang.Runnable} to the run() method
  * and the helper will setup a transaction and execute the Runnable in it.
  *
  * Example:
  * <pre><code>
 *  final Algebra algebra = ...
  *  WorkManager.run(new Runnable() { public void run() {
  *    Formula f = new Formula("Zeissler_1", "x = y");
  *    FormulaRepo.getInstance().save(f);
  *
  *    AlgebraRepository.getInstane().link(algebra, f, true);
  *  }});
  * </code></pre>
  * If this code fails for any reason, the transaction gets rolled back and the formula is no longer saved.
  */
 public class WorkManager {
     private static ThreadLocal<Transaction> transaction = new ThreadLocal<Transaction>();
     
     /**
      * Returns the transaction of the current thread, if there is one. Otherwise this returns null.
      */
     public static Transaction getCurrentTransaction() {
         return transaction.get();
     }
     
     /**
      * Executes this runnable after setting up a new transaction in the backend. After the Runnable
      * has finished, the transaction is commited and closed. If any exception occures, the transaction
      * is rolled back and the exception gets rethrown. The Runnable can get access to the transaction
      * while getting executed with the {@link #getCurrentTransaction()} method.
      *
      * @see #getCurrentTransaction()
      * @see Transaction
     */ 
     public static void run(Runnable run) {
         // Create a new transaction object
         Transaction t = RepositoryFactory.getInstance().createTransaction();
        if ( t == null ) {
            throw new IllegalStateException("Unable to get a new transaction object from RepositoryFactory.");
        }
         transaction.set(t); // Store the transaction for the getter method
         
         try {
             t.beginTransaction(); 
             run.run();
             t.commit();
         } catch (RuntimeException exc) {
             t.rollback();
             throw exc;
         } finally {
             t.endTransaction();
             transaction.remove();
         }
     }
 }
