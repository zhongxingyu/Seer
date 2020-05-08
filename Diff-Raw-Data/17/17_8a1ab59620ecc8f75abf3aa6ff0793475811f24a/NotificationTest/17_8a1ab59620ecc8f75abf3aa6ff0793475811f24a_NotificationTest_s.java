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
		assertNull(testProfile.getNotifications());
 		
 		//Receiving messages should generate notifications.
 		testProfile.receivePrivateMessage("ALERT", "SIRENS");
		assertNotNull(testProfile.getNotifications());
 		
 		try {
 			testProfile.clearNotifications();
 		} catch (Exception e) {
 			fail("Could not clear notifications: " + e.getMessage());
 		}
		assertNull(testProfile.getNotifications());
 		
 		testProfile.receivePublicMessage("Fred", "Wow, Cool!!!");
		assertNotNull(testProfile.getNotifications());
 		
 		try {
 			testProfile.clearNotification(0);
 		} catch (Exception e) {
 			fail("Could not clear notification 0: " + e.getMessage());
 		}
		assertNull(testProfile.getNotifications());
 		try {
 			if (testProfile.getNotifications().size() == 0) {
 				testProfile.clearNotification(0);
 				fail("Clearing nonexistant notification did not generate an exception!");
 			}
 		} catch (Exception e) {
 			
 		}
 	}
 
 }
