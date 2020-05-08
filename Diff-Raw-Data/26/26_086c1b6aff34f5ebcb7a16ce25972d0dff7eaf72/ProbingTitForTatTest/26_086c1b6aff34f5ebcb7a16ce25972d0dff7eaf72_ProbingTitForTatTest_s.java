 package galapagos.tests;
 
import galapagos.*;
 
 /**
  * Test of the Probing Tit For Tat behavior.
  */
 public class ProbingTitForTatTest extends BehaviorTest {
 	public void testDecide() {
 		// should clean unknown finches
     	assertEquals(behavior.decide(opponent), Action.CLEANING);
         assertEquals(behavior.decide(opponent2), Action.CLEANING);
         
         // Should do what opponent did last time in the first few rounds.
         behavior.response(opponent, Action.IGNORING);
         assertEquals(behavior.decide(opponent), Action.IGNORING);
         
         behavior.response(opponent2, Action.CLEANING);
         assertEquals(behavior.decide(opponent2), Action.CLEANING);
         
         behavior.response(opponent, Action.CLEANING);
         assertEquals(behavior.decide(opponent), Action.CLEANING);
         
         behavior.response(opponent2, Action.IGNORING);
         assertEquals(behavior.decide(opponent2), Action.IGNORING);
         
         behavior.response(opponent, Action.CLEANING);
         assertEquals(behavior.decide(opponent), Action.CLEANING);
         
         behavior.response(opponent2, Action.IGNORING);
         assertEquals(behavior.decide(opponent2), Action.IGNORING);
         
         // Thereafter it get unpredictable.
         // We cannot test the random qualities of Probing Tit-for-tat.
     }
     
     public Behavior getBehavior() {
         return new ProbingTitForTat();
     }
     
     public String behaviorName () {
     	return "Probing Tit for Tat";
     }
 }
