 package planner.app;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import org.junit.Test;
 
 public class TestWorkSet extends SampleData {
 
 	@Test
 	public void testRegisterWork() {
 		User user = createTempUser();
 		Work work = createTempWork();
		//Testing registering
		assertTrue(user.getWorkSet().isEmpty());
 		try {
 			user.registerWork(work);
 		} catch (OperationNotAllowedException e) {
 			fail("An OperationNotAllowedException should not have been thrown.");
 		}
 		assertTrue(user.getWorkSet().contains(work));
		//Testing double registering
 		try {
 			user.registerWork(work);
 			fail("An OperationNotAllowedException should have been thrown.");
 		} catch (OperationNotAllowedException e) {
 			assertEquals(e.getOperation(), Operation.USER_REGISTER_WORK);
 			assertEquals(e.getMessage(), User.MSG_WORK_OVERLAP);
 			assertTrue(user.getWorkSet().size() == 1);
 		}
		//Testing removing
 		user.removeWork(work);
 		assertFalse(user.getWorkSet().contains(work));
 	}
 
 }
