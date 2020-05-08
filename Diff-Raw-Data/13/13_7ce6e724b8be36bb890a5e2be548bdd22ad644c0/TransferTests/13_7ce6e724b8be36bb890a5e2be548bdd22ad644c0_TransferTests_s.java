 package ca.hilikus.jrobocom.robot;
 
 import static org.mockito.Matchers.eq;
 import static org.mockito.Matchers.isA;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 import static org.testng.Assert.assertEquals;
 import static org.testng.Assert.assertFalse;
 import static org.testng.Assert.assertNotEquals;
 import static org.testng.Assert.assertTrue;
 
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import ca.hilikus.jrobocom.AbstractTest;
 import ca.hilikus.jrobocom.Player;
 import ca.hilikus.jrobocom.World;
 import ca.hilikus.jrobocom.player.Bank;
 import ca.hilikus.jrobocom.player.InstructionSet;
 import ca.hilikus.jrobocom.robot.api.RobotAction;
 import ca.hilikus.jrobocom.timing.Delayer;
 
 /**
  * Tests the two transfer actions {@link RobotAction#transfer(int, int)} and
  * {@link RobotAction#reverseTransfer(int, int)}
  * 
  * @author hilikus
  */
 public class TransferTests extends AbstractTest {
 
     private World mockWorld;
     private Delayer mockDelayer;
     private Bank[] banks;
     private Player mockPlayer;
     private Robot TU;
     private Robot dest;
     private Bank[] banks2;
 
     /**
      * Dummy banks used in testing
      * 
      * @author hilikus
      */
     public static class DummyBank extends Bank {
 
 	public DummyBank(int pTeamId) {
 	    super(pTeamId);
 	}
 
 	@Override
 	public void run() {
 	}
 
     }
 
     /**
      */
     public TransferTests() {
 	super(Robot.class);
     }
 
     /**
      * Configures the testing unit
      */
     @BeforeMethod
     public void setUp() {
 	mockWorld = mock(World.class);
 	mockDelayer = mock(Delayer.class);
 	banks = new Bank[] { new DummyBank(1), new DummyBank(2), null };
 	banks2 = new Bank[] { new DummyBank(8), new DummyBank(9), null };
 	mockPlayer = mock(Player.class);
 
 	TU = new Robot(mockWorld, mockDelayer, banks, "Unity-src", mockPlayer);
 	dest = new Robot(mockWorld, mockDelayer, banks2, "Unity-dest", mockPlayer);
     }
 
     /**
      * Checks for common requirements at the end of each test
      */
     @AfterMethod
     public void commonCheck() {
 	assertTrue(dest.isAlive(), "Robot died unnecessarily");
     }
 
     /**
      * A normal transfer
      */
     @Test
     public void normalTransfer() {
 	when(mockWorld.getNeighbour(TU)).thenReturn(dest);
 
 	assertNotEquals(TU.transfer(0, 0), 0, "Transfer failed");
 	dest.changeBank(0);
 	assertEquals(dest.getRunningBankTeamId(), 1, "Transfer failed");
 
 	assertNotEquals(TU.reverseTransfer(1, 1), 0, "Transfer failed");
 	TU.changeBank(1);
 	assertEquals(TU.getRunningBankTeamId(), 9, "Reverse transfer failed");
 
 	assertTrue(TU.isAlive(), "Robot died unnecessarily");
     }
 
     /**
      * Tests transferring a bank when the active field is empty
      */
     @Test
     public void transferToEmpty() {
 	when(mockWorld.getNeighbour(TU)).thenReturn(null);
 	assertEquals(TU.transfer(0, 0), 0, "Invalid transfer succeeded");
 	assertEquals(TU.reverseTransfer(1, 1), 0, "Invalid transfer succeeded");
 
 	assertTrue(TU.isAlive(), "Robot died unnecessarily");
     }
 
     /**
      * Tests transferring where the destination bank is out of bounds
      */
     @Test
     public void transferToOutOfBounds() {
 	when(mockWorld.getNeighbour(TU)).thenReturn(dest);
 
 	assertEquals(TU.transfer(0, 10), 0, "Invalid transfer succeeded");
 	assertTrue(TU.isAlive(), "Robot died unnecessarily");
 
 	assertEquals(TU.reverseTransfer(1, 11), 0, "Invalid transfer succeeded");
 	assertFalse(TU.isAlive(), "Robot should have died"); // should have died always goes last
     }
 
     /**
      * Tests transferring where the source bank is out of bounds
      */
     @Test
     public void transferFromOutOfBounds() {
 	when(mockWorld.getNeighbour(TU)).thenReturn(dest);
 
 	assertEquals(TU.reverseTransfer(11, 1), 0, "Invalid transfer succeded");
 	assertTrue(TU.isAlive(), "Robot died unnecessarily");
 
 	assertEquals(TU.transfer(10, 0), 0, "Invalid transfer succeeded");
 	assertFalse(TU.isAlive(), "Robot should have died"); // should have died always goes last
     }
 
     /**
      * Tests whether transferring an empty bank succeeds
      */
     @Test
     public void transferEmptyBank() {
 	when(mockWorld.getNeighbour(TU)).thenReturn(dest);
 
 	assertEquals(TU.transfer(2, 1), 0, "Should have 0 due to empty transfer");
 	assertTrue(TU.isAlive(), "Robot died unnecessarily");
 
 	assertEquals(TU.reverseTransfer(2, 1), 0, "Should have 0 due to empty transfer");
 	assertTrue(TU.isAlive(), "Robot died unnecessarily");
     }
 
     /**
      * Tests that a robot dies when it tries to transfer but it doesn't have the necessary
      * instruction set
      */
     @Test
     public void transferBadInstructionSet() {
 	when(mockWorld.add(eq(TU), isA(Robot.class))).thenReturn(true);
 	TU.createRobot("Unit-test", InstructionSet.BASIC, 1, false);
 	Robot child = CreateRobotTests.getChild(mockWorld, TU);
 
 	assertEquals(child.transfer(1, 1), 0, "Invalid transfer succeeded");
 	assertFalse(child.isAlive(), "Robot should have died");
     }
 }
