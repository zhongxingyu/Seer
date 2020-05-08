 package isnork.g3;
 
 import static org.junit.Assert.*;
 import isnork.sim.SeaLifePrototype;
 
 import org.junit.Test;
 
 import com.google.common.collect.ImmutableSet;
 
 public class PrecomputationTest {
 	
 	@Test
 	public void testMetrics(){
 		boolean danger = true;
 		int happiness = 7;
 		int max = 3;
 		int min = 2;
 		boolean stationary = true;
 
 		SeaLifePrototype charmander = new SeaLifePrototypeBuilder("charmander")
 			.dangerous(danger)
 			.happiness(happiness)
 			.minCount(min)
 			.maxCount(max)
 			.moving(stationary)
 			.create();
 		
 		SeaLifePrototype squirtle = new SeaLifePrototypeBuilder("squirtle")
 			.dangerous(!danger)
 			.happiness(happiness * 2)
 			.minCount(min * 2)
 			.maxCount(max * 2)
 			.moving(!stationary)
 			.create();
 		
 		SeaLifePrototype bulbasaur = new SeaLifePrototypeBuilder("bulbasaur")
 			.dangerous(danger)
 			.happiness(happiness * 3)
 			.minCount(min * 3)
 			.maxCount(max * 3)
 			.moving(!stationary)
 			.create();
 		
 		WaterProofPokedex dex = new WaterProofPokedex(ImmutableSet.of(charmander, squirtle, bulbasaur));
 		int sideLength = 10;
 		int radius = 5; 
 		
 		WaterProofPrecomputation precomp = new WaterProofPrecomputation(sideLength, radius, dex);
 		
 		assertTrue(precomp.creatureDensity() == .14);
 		assertTrue(precomp.topToMedianHappinessRatio() == 1.5);
 		assertTrue(precomp.dangerousToTotalRatio() == (9.0/14));
 		assertTrue(precomp.movingToStationaryDangerousRatio() == (2.0 / 7));
		assertTrue(precomp.naiveHighScore() == 70);
 		
 		
 	}
 
 }
