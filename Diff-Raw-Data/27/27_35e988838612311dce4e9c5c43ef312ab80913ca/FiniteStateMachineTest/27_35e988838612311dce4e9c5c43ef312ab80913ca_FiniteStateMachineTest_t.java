 package fsm;
 
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 
 public class FiniteStateMachineTest {
 
 	@Test
 	public void test() {
		FiniteStateMachine fsm = new FiniteStateMachine(new Waiting());

 		try {
			fsm.changeState(new Eating());
			fail("Could not change FSM state");
		} catch (RuntimeException change) {
 			try {
				fsm.revertToPreviousState();
				fail("Could not revert to previous state");
			} catch (RuntimeException previous) {
 				try {
					fsm.run();
					fail("Could not start the FSM");
				} catch (RuntimeException e) {
					
 				}
 			}
 		}
 	}
 }
