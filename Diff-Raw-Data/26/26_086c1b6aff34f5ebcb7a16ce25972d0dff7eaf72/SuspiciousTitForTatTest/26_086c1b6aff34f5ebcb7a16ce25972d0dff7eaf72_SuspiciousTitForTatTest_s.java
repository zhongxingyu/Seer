 package galapagos.tests;
 
import galapagos.*;
 
 /**
  * Test of the Suspicious Tit for tat.
  */
 public class SuspiciousTitForTatTest extends BehaviorTest {
 	
 	/**
 	 * Tests that the SuspiciousTitForTat always does the same as 
 	 * what the other finch did last time they met.
 	 * And that it ignores unknown finches
 	 */
 	public void testDecide() {
 		// should ignore unknown finches
     	assertEquals(behavior.decide(opponent), Action.IGNORING);
         assertEquals(behavior.decide(opponent2), Action.IGNORING);
         
         // Should do what opponent did last time.
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
     }
     
     public Behavior getBehavior() {
         return new SuspiciousTitForTat();
     }
     
 
     public String behaviorName () {
     	return "Suspicious Tit for Tat";
     }
 }
