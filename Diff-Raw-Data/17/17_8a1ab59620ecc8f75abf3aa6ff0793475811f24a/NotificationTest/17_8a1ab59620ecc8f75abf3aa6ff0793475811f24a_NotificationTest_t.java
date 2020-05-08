 package tests;
 
 import static org.junit.Assert.*;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import facepamphlet.FacePamphletProfile;
 
 public class NotificationTest {
 	FacePamphletProfile testProfile;
 	@Before
 	public void setUp() throws Exception {
 		testProfile = new FacePamphletProfile("Me");
 	}
 	/**
 	 * Tests for the notification system.
 	 * @author Cameron Ross
 	 */
 	@Test
 	public void test() {
		assertTrue(testProfile.getNotifications().isEmpty());
 		
 		//Receiving messages should generate notifications.
 		testProfile.receivePrivateMessage("ALERT", "SIRENS");
		assertFalse(testProfile.getNotifications().isEmpty());
		
		testProfile.getNotifications().clear();
		assertFalse(testProfile.getNotifications().isEmpty()); // We should not be able to clear the notifications this way
 		
 		try {
 			testProfile.clearNotifications();
 		} catch (Exception e) {
 			fail("Could not clear notifications: " + e.getMessage());
 		}
		assertTrue(testProfile.getNotifications().isEmpty());
 		
 		testProfile.receivePublicMessage("Fred", "Wow, Cool!!!");
		assertFalse(testProfile.getNotifications().isEmpty());
 		
 		try {
 			testProfile.clearNotification(0);
 		} catch (Exception e) {
 			fail("Could not clear notification 0: " + e.getMessage());
 		}
		assertTrue(testProfile.getNotifications().isEmpty());
 		try {
 			if (testProfile.getNotifications().size() == 0) {
 				testProfile.clearNotification(0);
 				fail("Clearing nonexistant notification did not generate an exception!");
 			}
 		} catch (Exception e) {
 			
 		}
 	}
 
 }
