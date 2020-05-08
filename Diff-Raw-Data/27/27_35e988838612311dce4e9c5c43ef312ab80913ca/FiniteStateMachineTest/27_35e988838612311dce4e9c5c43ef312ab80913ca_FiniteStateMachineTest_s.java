 package fsm;
 
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 
 public class FiniteStateMachineTest {
 
 	@Test
 	public void test() {
		FiniteStateMachine fsm = null;
 		try {
			fsm = new FiniteStateMachine(new Eating());
			fail("Could not create/enter FSM");
		} catch (RuntimeException enter) {
 			try {
				fsm.changeState(new Waiting());
				fail("Could not change FSM state");
			} catch (RuntimeException change) {
 				try {
					fsm.revertToPreviousState();
					fail("Could not revert to previous state");
				} catch (RuntimeException previous) {
 				}
 			}
 		}
 	}

 }
