 import org.junit.*;
 
 import StarTrekV2.Engine;
 import StarTrekV2.Starship;
 import StarTrekV2.Subsystem;
 
 public class StarshipTests {
 
 	@Test
 	public void testIfShieldIsStillStrong() {
 		Starship ship = new Starship();
 		ship.hitStarship(1000);
 		
 		Assert.assertTrue("Shield should still have energy but it does not", ship.shield.getEnergy() > 0);
 	}
 
 	@Test
 	public void testIfAnyComponentOfStarshipIsDamaged() {
 		Starship ship = new Starship();
 		ship.hitStarship(20000);
 		
 		Assert.assertTrue("Shield is not depleted", ship.shield.getEnergy() == 0);
 		
 		// Check if at list one component was damaged.
 		boolean anythingDamaged = false;
 
 		for (Subsystem subsystem : ship.subSystems) {
 			anythingDamaged = anythingDamaged || subsystem.isDamaged();
 		}
 
 		Assert.assertTrue("Shield is depeleted but there is a subsytem that is active", anythingDamaged);
 	}
 
 	@Test
 	public void testIfAllComponentOfStarshipAreRepaired() {
 		Starship ship = new Starship();
 		ship.hitStarship(20000);
 		
 		Assert.assertTrue("Shield is not depleted", ship.shield.getEnergy() == 0);
 		
 		// Check if at list one component was damaged.
 		boolean anythingDamaged = false;
 
 		for (Subsystem subsystem : ship.subSystems) {
 			anythingDamaged = anythingDamaged || subsystem.isDamaged();
 		}
 
 		Assert.assertTrue("Shield is depeleted but there is a subsytem that is active", anythingDamaged);
 		
		// Repair starship
 		ship.repairStarship();
 		
 		// Check if there is no damaged components.
 		for (Subsystem subSystem : ship.subSystems) {
 			Assert.assertFalse("Subsystem is still damaged after repair", subSystem.isDamaged());
 		}
 	}
 	
 	@Test
 	public void testIsShieldIsDepletedButAllOtherComponentsOfStarshipAreActive() {
 		Starship ship = new Starship();
 		ship.hitStarship(5000);
 		
 		Assert.assertTrue("Shield is not depled", ship.shield.isDepleted());
 		
 		for (Subsystem subsystem : ship.subSystems) {
 			Assert.assertFalse("Subsystem is damaged while it is supposed to be active", subsystem.isDamaged());
 		}
 	}
 
 	@Test
 	public void testIfComponentUnderSubsystemCanBeRepaired() {
 		Subsystem engine = new Engine(1000);
 		
 		engine.takeDamage(10000);
 		Assert.assertTrue("Engine should be damaged but it is not", engine.isDamaged());
 		Assert.assertTrue("Engine should be repairable but it is not", engine.isRepairable());
 
 		engine.takeDamage(100000);
 		Assert.assertFalse("Engine should not be repairable but it is", engine.isRepairable());
 	}
 }
