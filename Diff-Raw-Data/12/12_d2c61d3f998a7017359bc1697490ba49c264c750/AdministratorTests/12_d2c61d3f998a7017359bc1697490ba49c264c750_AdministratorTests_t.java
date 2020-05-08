 package test;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import java.util.ArrayList;
 
 import main.AccessTracker;
 import main.Machine;
 import main.SystemAdministrator;
 import main.Tool;
 import main.User;
 
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import GUI.Driver;
 
 public class AdministratorTests {
 
 	static Driver driver;
 	static AccessTracker tracker;
 	static SystemAdministrator testAdmin;
 	static User testUser;
 	
 	static ArrayList<User> users;
 	static ArrayList<String> machineIds;
 	static ArrayList<Integer> toolIds;
 
 	@BeforeClass
 	public static void setup() {
 		driver = new Driver();
 		tracker = driver.getAccessTracker();
 		testAdmin = new SystemAdministrator("", "", 1);
 		testUser = new User("", "", 2);
 		users = new ArrayList<User>();
 	
 		users.add(testUser);
 		
 		testAdmin.addUser(testUser);
 	}
 
 	@Test
 	public void updatePermissionTest() {
 		ArrayList<Machine> machines = new ArrayList<Machine>();
 		Machine machine1 = new Machine("TEST Machine1", "ZZZZ1");
 		Machine machine2 = new Machine("TEST Machine2", "ZZZZ2");
 		machines.add(machine1);
 		machines.add(machine2);
 
 		testAdmin.updatePermission(testUser, machines);
 
 		// Ensures that the user is now certified to use the machines
 		assertTrue(testUser.getCertifiedMachines().size() == 2);
 		assertTrue(testUser.getCertifiedMachines().contains(machine1));
 		assertTrue(testUser.getCertifiedMachines().contains(machine2));
 
 		machines = new ArrayList<Machine>();
 		Machine machine3 = new Machine("TEST Machine3", "ZZZZ3");
 		machines.add(machine3);
 		
 		testAdmin.updatePermission(testUser, machines);
 		
 		//ensure that the user is now unable to user the previous and 
 		//certified on the new machine
 		assertTrue(testUser.getCertifiedMachines().size() == 1);
 		assertFalse(testUser.getCertifiedMachines().contains(machine1));
 		assertFalse(testUser.getCertifiedMachines().contains(machine2));
 		assertTrue(testUser.getCertifiedMachines().contains(machine3));
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
 	}
 
 	@Test
 	public void addAndRemoveToolTest() {
 		Tool testTool = new Tool("Test Tool", "1500");
		testAdmin.addTool(testTool);
 
 		// Ensure the tool was added to the list of tools
		System.out.println(tracker.getTools());
 		assertTrue(tracker.getTools().contains(testTool));
 
 		// Get the list of tools from the databse
 		tracker.loadTools();
 
 		// Ensure the tool was added to the database
 		assertTrue(tracker.getTools().contains(testTool));
 
 		testAdmin.removeTool(testTool.getUPC());
 
 		// Ensure the tool was removed from the list of tools
 		assertFalse(tracker.getTools().contains(testTool));
 
 		// Get the list of tools from the databse
 		tracker.loadTools();
 
 		// Ensure the tool was removed from the database
 		assertFalse(tracker.getTools().contains(testTool));
 	}
 
 	@Test
 	public void addAndRemoveMachineTest() {		
 		Machine testMachine = new Machine("Test Machine4", "ZZZZ4");
		testAdmin.addMachine(testMachine);
 
 		// Ensure the machine was added to the list of machine
 		assertTrue(tracker.getMachines().contains(testMachine));
 
 		// Get the list of machines from the databse
 		tracker.loadMachines();
 
 		// Ensure the machine was added to the database
 		assertTrue(tracker.getMachines().contains(testMachine));
 
 		testAdmin.removeMachine(testMachine.getID());
 
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
 	
 	@AfterClass
 	public static void cleanup() {
 		testAdmin.removeUsers(users);
 	}
 
 }
