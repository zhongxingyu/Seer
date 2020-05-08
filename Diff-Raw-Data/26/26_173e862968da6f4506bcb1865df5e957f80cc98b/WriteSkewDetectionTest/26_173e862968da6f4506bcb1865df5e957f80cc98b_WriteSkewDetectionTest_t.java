 package org.multiverse.stms.alpha.integrationtests;
 
import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.multiverse.api.TransactionFactory;
 import org.multiverse.api.exceptions.WriteSkewConflict;
 import org.multiverse.stms.alpha.AlphaStm;
 import org.multiverse.stms.alpha.manualinstrumentation.IntStack;
 import org.multiverse.stms.alpha.transactions.AlphaTransaction;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
 import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.ThreadLocalTransaction.clearThreadLocalTransaction;
 
 /**
  * MVCC suffers from a problem that serialized transactions are not completely serialized. For more information check
  * the following blog. http://pveentjer.wordpress.com/2008/10/04/breaking-oracle-serializable/ This tests makes sure
  * that the error is still in there and that the stm behaves like 'designed'.
  * <p/>
  * The test: There are 2 empty stacks. In 2 parallel transactions, the size of the one is pushed as element on the
  * second, and the size of the second is pushed as element on the first. Afer both transactions have committed, the stm
  * contains 2 stacks with both the element 0 on them. So not a 1 and 0, or 0 and 1 as true serialized execution of
  * transactions would do.
  *
  * @author Peter Veentjer.
  */
 public class WriteSkewDetectionTest {
 
     private IntStack stack1;
     private IntStack stack2;
     private AlphaStm stm;
 
     @Before
     public void setUp() {
         stm = (AlphaStm) getGlobalStmInstance();
        clearThreadLocalTransaction();
         stack1 = new IntStack();
         stack2 = new IntStack();
     }
 
    @After
    public void tearDown() {
        clearThreadLocalTransaction();
    }

     @Test
     public void whenDisabledWriteSkewProblem_thenWriteSkewConflict() {
         TransactionFactory<AlphaTransaction> factory = stm.getTransactionFactoryBuilder()
                .setAutomaticReadTracking(true)
                .setSpeculativeConfigurationEnabled(false)
                .setAllowWriteSkewProblem(false)
                .build();
 
         AlphaTransaction t1 = factory.start();
         AlphaTransaction t2 = factory.start();
 
         stack1.push(t1, stack2.size(t1));
         stack2.push(t2, stack1.size(t2));
 
         t1.commit();
         try {
             t2.commit();
             fail();
         } catch (WriteSkewConflict expected) {
         }
     }
 
     public void assertStackContainsZero(IntStack stack) {
         assertEquals(1, stack.size());
         assertEquals(0, stack.pop());
     }
 
     @Test
     public void whenEnabledWriteSkewProblem_thenWriteSkewHappens() {
         TransactionFactory<AlphaTransaction> txFactory = stm.getTransactionFactoryBuilder()
                .setSpeculativeConfigurationEnabled(false)
                .setAllowWriteSkewProblem(true)
                .build();
 
         AlphaTransaction tx1 = txFactory.start();
         AlphaTransaction tx2 = txFactory.start();
 
         stack1.push(tx1, stack2.size(tx1));
         stack2.push(tx2, stack1.size(tx2));
 
         tx1.commit();
         tx2.commit();
 
         assertStackContainsZero(stack1);
         assertStackContainsZero(stack2);
     }
 
     @Test
     public void whenNonTrackingUpdateTransaction_thenWriteSkewNotDetected() {
         TransactionFactory<AlphaTransaction> txFactory = stm.getTransactionFactoryBuilder()
                .setSpeculativeConfigurationEnabled(false)
                 .setAutomaticReadTracking(false)
                 .setReadonly(false).build();
 
         AlphaTransaction tx1 = txFactory.start();
         AlphaTransaction tx2 = txFactory.start();
 
         stack1.push(tx1, stack2.size(tx1));
         stack2.push(tx2, stack1.size(tx2));
 
         tx1.commit();
         tx2.commit();
 
         assertStackContainsZero(stack1);
         assertStackContainsZero(stack2);
     }
 }
