 package org.nebulostore.appcore;
 
 import org.junit.Test;
 import org.nebulostore.appcore.exceptions.NebuloException;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
 
 /**
  * Tests for TwoStepReturningJobModule class.
  * @author bolek
  */
 public final class TwoStepReturningJobModuleTest extends ReturningJobModuleBase {
   private static final int SHORT_TIMEOUT_SEC = 1;
   private static final Integer ANSWER_VALUE = 432;
   private static final Boolean SEMI_RESULT = true;
 
   @Test
   public void testReturnTwoCorrectResults() throws NebuloException {
     TwoStepReturningJobModule<Integer, Boolean, Integer> module = new SuccessModule();
     setupModule(module);
 
     // Final result should not be available now.
     try {
       module.getResult(TIMEOUT_SEC);
       fail();
     } catch (NebuloException e) {
       assertEquals(e.getMessage(), "Waiting for second result before first.");
     }
 
     // Get semi-result.
     assertEquals(module.getSemiResult(TIMEOUT_SEC), SEMI_RESULT);
 
     // Final result should still not be available.
     try {
       module.getResult(SHORT_TIMEOUT_SEC);
       fail();
     } catch (NebuloException e) {
      assertEquals(e.getMessage(), "Timeout in getResult().");
     }
 
     // Provide answer to semi-result.
     module.setAnswer(ANSWER_VALUE);
 
     // Get final result.
     assertEquals(module.getResult(SHORT_TIMEOUT_SEC), SUCCESS_VALUE);
   }
 
   @Test
   public void testGetResultIsNotBlockedAfterErrorOnSemiResult() {
     TwoStepReturningJobModule<Integer, Boolean, Integer> module = new ImmediateErrorModule();
     setupModule(module);
 
     // Get semi-result and receive error.
     try {
       module.getSemiResult(TIMEOUT_SEC);
       fail();
     } catch (NebuloException e) {
       assertEquals(e.getMessage(), ERROR_VALUE);
     }
     // getResult() should also return the same exception.
     try {
       module.getResult(SHORT_TIMEOUT_SEC);
       fail();
     } catch (NebuloException e) {
       assertEquals(e.getMessage(), ERROR_VALUE);
     }
   }
 
   /**
    * Module that ends with success.
    */
   private class SuccessModule extends TwoStepReturningJobModule<Integer, Boolean, Integer> {
     private int state_;
 
     @Override
     protected void processMessage(Message message) throws NebuloException {
       if (state_ == 0) {
         returnSemiResult(SEMI_RESULT);
         ++state_;
       } else if (state_ == 1) {
         endWithSuccess(SUCCESS_VALUE);
       } else {
         throw new IllegalStateException();
       }
     }
 
     @Override
     protected void performSecondPhase(Integer answer) {
       if (state_ == 1 && answer == ANSWER_VALUE) {
         inQueue_.add(new TestMessage());
       } else {
         throw new IllegalStateException();
       }
     }
   };
 
   /**
    * Module that immediately ends with error.
    */
   private class ImmediateErrorModule extends TwoStepReturningJobModule<Integer, Boolean, Integer> {
     @Override
     protected void processMessage(Message message) throws NebuloException {
       endWithError(new NebuloException(ERROR_VALUE));
     }
 
     @Override
     protected void performSecondPhase(Integer answer) { /* Unused. */ }
   };
 }
