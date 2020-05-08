 package test;
 
 import static org.junit.Assert.*;

 import java.util.ArrayList;
 
 import main.AccessTracker;
 import main.Machine;
 import main.SystemAdministrator;
 import main.Tool;
 import main.User;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class AdministratorTests {
 
 	AccessTracker tracker;
 	SystemAdministrator testAdmin;
 	User testUser;
 
 	@Before
 	public void setup() {
 		testAdmin = new SystemAdministrator("", "", 1);
 		tracker = new AccessTracker();
 		testUser = new User("", "", 2);
 	}
 
 	@Test
 	public void addAndRemovePermissionTest() {
 		Machine testMachine = new Machine("TPDD", "T-1");
 		testAdmin.addPermission(testUser, testMachine);
 
 		// Ensures that the user is now certified to use the machine
 		assertTrue(testUser.getCertifiedMachines().contains(testMachine));
 
 		testAdmin.removePermission(testUser, testMachine);
 
 		// Ensures that the user is no longer certified on the machine
 		assertFalse(testUser.getCertifiedMachines().contains(testMachine));
 	}
 
 	@Test
 	public void removeUsersTest() {	
 		ArrayList<User> testUsers = new ArrayList<User>();
 
 		// Must pass in an ArrayList to removeUsers(), so we
 		// create a dummy list of users to remove and add our
 		// test user to this list.
 		testUsers.add(testUser);
 		tracker.createUser("", "", 2);
 
 		// Ensure the testUser was added to the list of current users
 		assertTrue(tracker.getCurrentUsers().contains(testUser));
 
 		testAdmin.removeUsers(testUsers);
 
 		// Ensure the testUser was removed from the list of current users
 		assertFalse(tracker.getCurrentUsers().contains(testUser));
 
 		testUser = tracker.loadUser(2);
 		// Try to load the user. They should be gone now, so the query
 		// should return null.
 		assertNull(testUser);
 
 	}
 
 	@Test
 	public void addAndRemoveToolTest() {
 		Tool testTool = new Tool("HITCOO", 15);
 		testAdmin.addTool(testTool);
 
 		// Ensure the tool was added to the list of tools
 		assertTrue(tracker.getTools().contains(testTool));
 
 		// Get the list of tools from the databse
 		tracker.loadTools();
 
 		// Ensure the tool was added to the database
 		assertTrue(tracker.getTools().contains(testTool));
 
 		testAdmin.removeTool(testTool);
 
 		// Ensure the tool was removed from the list of tools
 		assertFalse(tracker.getTools().contains(testTool));
 
 		// Get the list of tools from the databse
 		tracker.loadTools();
 
 		// Ensure the tool was removed from the database
 		assertFalse(tracker.getTools().contains(testTool));
 	}
 
 	@Test
 	public void addAndRemoveMachineTest() {
 		Machine testMachine = new Machine("TPDD", "T-1");
 		testAdmin.addMachine(testMachine);
 
 		// Ensure the machine was added to the list of machine
 		assertTrue(tracker.getMachines().contains(testMachine));
 
 		// Get the list of machines from the databse
 		tracker.loadMachines();
 
 		// Ensure the machine was added to the database
 		assertTrue(tracker.getMachines().contains(testMachine));
 
 		testAdmin.removeMachine(testMachine);
 
 		// Ensure the machine was removed from the list of machine
 		assertFalse(tracker.getMachines().contains(testMachine));
 
 		// Get the list of machines from the databse
 		tracker.loadMachines();
 
 		// Ensure the machine was removed from the database
 		assertFalse(tracker.getMachines().contains(testMachine));
 		
 	}
 
 	@Test
 	public void lockAndUnlockUserTest() {
 		testAdmin.lockUser(testUser);
 		
 		tracker.loadUser(testUser.getCWID());
 		
 		// Ensure that the user is locked in RAM and in database
 		assertTrue(testUser.isLocked());
 		
 		testAdmin.unlockUser(testUser);
 		
 		tracker.loadUser(testUser.getCWID());
 		
 		// Ensure that the user is unlocked in RAM and in database
 		assertFalse(testUser.isLocked());
 	}
 
 }
