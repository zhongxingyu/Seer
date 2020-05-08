 package btlshp.junit;
 import junit.framework.TestCase;
 import org.junit.Test;
 import ConstructTestStubs.*;
 import btlshp.enums.*;
 
 public class ConstructsTest extends TestCase{
 	Player myPlayer = new Player();
 
 	@Test
 	public void testConstructBasics() {
 		Base myBase = new Base(myPlayer); 
 		ConstructBlock[] myBlocks = myBase.getBlocks();
 // Construct Based Tests
 		// Insure Radar Range is 0 for Base
 		assertTrue(0 == myBase.getRadarRange());
 		// Check Blocks
 		assertTrue(10 == myBlocks.length);
 		for (int i = 0; i <10 ; i++){
 			assertTrue(myBlocks[i].isUntouched());
 		}
 		// Check Location
 		assertTrue(myBase.getLocation() ==null);
 		Location myLoc = new Location(5, 5);
 		myBase.setLocation(myLoc);
 		assertTrue(myBase.getLocation() == myLoc);
 		
 		// Check Direction
 		assertTrue(myBase.getDirection() ==null);
 		Direction myDir = Direction.West;
 		myBase.setDirection(myDir);
 		assertTrue(myBase.getDirection() == myDir);
 	}
 	public void testConstructAndBase() {
 		Base myBase = new Base(myPlayer); 
 		ConstructBlock[] myBlocks = myBase.getBlocks();
 		// Test Damage via Gun
 		myBase.assessDamage(myBlocks[0], Weapon.Gun);
 		assertTrue(myBlocks[0].isDestroyed());
 		for (int i = 1; i <10 ; i++){
 			assertTrue(myBlocks[i].isUntouched());
 		}
 		// Test Damage via Torpedo
 		myBase.assessDamage(myBlocks[0], Weapon.Torpedo);
 		assertTrue(myBlocks[0].isDestroyed());
 		assertTrue(myBlocks[1].isDestroyed());
 		for (int i = 2; i <10 ; i++){
 			assertTrue(myBlocks[i].isUntouched());
 		}
 		// Test Damage with Mine
 		myBase.assessDamage(myBlocks[0], Weapon.Mine);
 		assertTrue(myBlocks[0].isDestroyed());
 		assertTrue(myBlocks[1].isDestroyed());
 		assertTrue(myBlocks[2].isDestroyed());
 		for (int i = 3; i <10 ; i++){
 			assertTrue(myBlocks[i].isUntouched());
 		}
 // Base Specific Tests
 		// Base should be able to repair
 		assertTrue(myBase.canRepairOther());
 		assertTrue(myBase.canRepairSelf());
 		// Base Cannot repair after 3 more mines
 		myBase.assessDamage(myBlocks[0], Weapon.Mine);
 		myBase.assessDamage(myBlocks[0], Weapon.Mine);
 		myBase.assessDamage(myBlocks[0], Weapon.Mine);
 		assertFalse(myBase.canRepairOther());
 		assertTrue(myBase.canRepairSelf());
 		// Repair one and it should be able to repair again
 		myBase.AssesRepair(myBlocks[0]);
 		assertTrue(myBase.canRepairSelf());
 		assertTrue(myBase.canRepairOther());
 		// Verify That All Blocks are correct... blocks 2-6 should be destroyed
 		assertTrue(myBlocks[0].isUntouched());
 		for (int i = 1; i <6 ; i++){
 			assertTrue(myBlocks[i].isDestroyed());
 		}
 		for (int i = 6; i <10 ; i++){
 			assertTrue(myBlocks[i].isUntouched());
 		}
 		
 // Test that Blocks are able to pass the correct information
 		assertTrue(myBlocks[0].isUntouched());
 		myBlocks[0].takeHit(Weapon.Gun);
 		assertTrue(myBlocks[0].isDamaged());
 		
 		myBlocks[8].takeHit(Weapon.Torpedo);
 		for (int i = 0; i <6 ; i++){
 			assertTrue(myBlocks[i].isDestroyed());
 		}
 		assertTrue(myBlocks[6].isUntouched());
 		assertTrue(myBlocks[7].isDestroyed());
 		assertTrue(myBlocks[8].isDestroyed());
 		assertTrue(myBlocks[9].isUntouched());
 // Destroy the base and Check isDestroyed and Can Repair self/other
 		for (int i = 0; i <10; i++){
 			myBase.assessDamage(myBlocks[0], Weapon.Mine);
 		}
 		assertTrue(myBase.isDestroyed());
 		assertFalse(myBase.canRepairOther());
 		assertFalse(myBase.canRepairSelf());
 	}
 /* Ship Specific Tests
 	Test Size of each Ship
 	Test Movement of each Ship
 	Test Capabilities of each Ship
 	For Minesweeper, Test Armored Blocks.
 //private Ship(
  * Player owner, 
  * boolean isArmored, 
  * boolean gun, 
  * boolean torpedo, 
  * boolean mine, 
  * boolean Sonar, 
  * int forward, 
  * int side, 
  * int back, 
  * int gunRange, 
  * int radarRange, 
  * int sonarRange, 
  * int numberOfBlocks) {	
 */
 
 	public void testCruiser(){
 	//Ship myCruiser = new Ship(owner, false, true, false, false, false, 10, 1, 1, 5, 6, 0, 5);
 		Ship myCruiser = Ship.buildCruiser(myPlayer);
 		assertTrue(myCruiser.getPlayer() == myPlayer);
 		assertTrue(myCruiser.canFireGun());
 		assertFalse(myCruiser.canFireTorpedo());
 		assertFalse(myCruiser.canPlaceMine());
 		assertFalse(myCruiser.hasSonar());
 		assertTrue(10 == myCruiser.getMaxForwardMove());
 		assertTrue(1 == myCruiser.getMaxSideMove());
 		assertTrue(1 == myCruiser.getMaxReverseMove());
 		assertTrue(5 == myCruiser.getMaxGunRange());
 		assertTrue(6 == myCruiser.getRadarRange());
 		assertTrue(0 == myCruiser.getSonarRange());
 		assertTrue(5 == myCruiser.getBlocks().length);
 		for (int i = 0; i<5; i++){
 			assertTrue(myCruiser.getBlocks()[i].isUntouched());
 		}
 	}
 	public void testTorpedoBoat(){
 	//Ship myTorpedoBoat = new Ship(owner, false, true, true, false, false, 8, 1, 1, 4, 5, 0, 4)
 		Ship myTorpedoBoat = Ship.buildTorpedoBoat(myPlayer);
 		assertTrue(myTorpedoBoat.getPlayer() == myPlayer);
 		assertTrue(myTorpedoBoat.canFireGun());
 		assertTrue(myTorpedoBoat.canFireTorpedo());
 		assertFalse(myTorpedoBoat.canPlaceMine());
 		assertFalse(myTorpedoBoat.hasSonar());
 		assertTrue(8 == myTorpedoBoat.getMaxForwardMove());
 		assertTrue(1 == myTorpedoBoat.getMaxSideMove());
 		assertTrue(1 == myTorpedoBoat.getMaxReverseMove());
 		assertTrue(4 == myTorpedoBoat.getMaxGunRange());
 		assertTrue(5 == myTorpedoBoat.getRadarRange());
 		assertTrue(0 == myTorpedoBoat.getSonarRange());
 		assertTrue(4 == myTorpedoBoat.getBlocks().length);
 		for (int i = 0; i<4; i++){
 			assertTrue(myTorpedoBoat.getBlocks()[i].isUntouched());
 		}
 	}
 
 	public void testDestroyer(){
 	//Ship myDestroyer = new Ship(owner, false, false, true, false, false, 6, 1, 1, 0, 4, 0, 3);
 		Ship myDestroyer = Ship.buildDestroyer(myPlayer);
 		assertTrue(myDestroyer.getPlayer() == myPlayer);
 		assertFalse(myDestroyer.canFireGun());
 		assertTrue(myDestroyer.canFireTorpedo());
 		assertFalse(myDestroyer.canPlaceMine());
 		assertFalse(myDestroyer.hasSonar());
 		assertTrue(6 == myDestroyer.getMaxForwardMove());
 		assertTrue(1 == myDestroyer.getMaxSideMove());
 		assertTrue(1 == myDestroyer.getMaxReverseMove());
 		assertTrue(0 == myDestroyer.getMaxGunRange());
 		assertTrue(4 == myDestroyer.getRadarRange());
 		assertTrue(0 == myDestroyer.getSonarRange());
 		assertTrue(3 == myDestroyer.getBlocks().length);
 		for (int i = 0; i<3; i++){
 			assertTrue(myDestroyer.getBlocks()[i].isUntouched());
 		}
 	}
 
 	public void Minesweeper(){
 	//Ship myMineSweeper = new Ship(owner, true, false, false, true, true, 4, 1, 1, 0, 2, 2, 2);	
 		Ship myMinesweeper = Ship.buildMineSweeper(myPlayer);
 		assertTrue(myMinesweeper.getPlayer() == myPlayer);
 		assertFalse(myMinesweeper.canFireGun());
 		assertFalse(myMinesweeper.canFireTorpedo());
 		assertTrue(myMinesweeper.canPlaceMine());
 		assertTrue(myMinesweeper.hasSonar());
 		assertTrue(4 == myMinesweeper.getMaxForwardMove());
 		assertTrue(1 == myMinesweeper.getMaxSideMove());
 		assertTrue(1 == myMinesweeper.getMaxReverseMove());
 		assertTrue(0 == myMinesweeper.getMaxGunRange());
 		assertTrue(2 == myMinesweeper.getRadarRange());
 		assertTrue(2 == myMinesweeper.getSonarRange());
 		assertTrue(2 == myMinesweeper.getBlocks().length);
 		for (int i = 0; i<2; i++){
 			assertTrue(myMinesweeper.getBlocks()[i].isUntouched());
 		}
 	//Test the Armored Construct 
 		assertTrue(myMinesweeper.getBlocks()[0].isUntouched());
 		assertTrue(myMinesweeper.getBlocks()[1].isUntouched());
 		myMinesweeper.assessDamage(myMinesweeper.getBlocks()[0], Weapon.Torpedo);
 		assertFalse(myMinesweeper.getBlocks()[0].isUntouched());
 		assertFalse(myMinesweeper.getBlocks()[1].isUntouched());
 		assertTrue(myMinesweeper.getBlocks()[0].isDamaged());
 		assertTrue(myMinesweeper.getBlocks()[1].isDamaged());
 		assertFalse(myMinesweeper.getBlocks()[0].isDestroyed());
 		assertFalse(myMinesweeper.getBlocks()[1].isDestroyed());
 		myMinesweeper.assessDamage(myMinesweeper.getBlocks()[0], Weapon.Torpedo);
 		assertFalse(myMinesweeper.getBlocks()[0].isUntouched());
 		assertFalse(myMinesweeper.getBlocks()[1].isUntouched());
 		assertFalse(myMinesweeper.getBlocks()[0].isDamaged());
 		assertFalse(myMinesweeper.getBlocks()[1].isDamaged());
 		assertTrue(myMinesweeper.getBlocks()[0].isDestroyed());
 		assertTrue(myMinesweeper.getBlocks()[1].isDestroyed());
 	}
 	
 }
