 package ltg.ps.phenomena.wallcology.population_calculators;
 
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ltg.ps.phenomena.wallcology.Wall;
 import ltg.ps.phenomena.wallcology.WallcologyPhase;
 
 public abstract class PopulationCalculator {
 	
 	// Logger
     protected final Logger log = LoggerFactory.getLogger(this.getClass());
     // Noise abount
 	private final double noisePercent = 0.04; 
 	
  	
  	/**
  	 * 
  	 * TODO Description
  	 *
  	 * @param currentPhaseWalls
  	 * @param currentPhase
  	 */
 	abstract public void updatePopulationStable(List<Wall> currentPhaseWalls, WallcologyPhase currentPhase);
 	
 	
 	/**
 	 * 
 	 * TODO Description
 	 *
 	 * @param nextPhase
 	 * @param walls
 	 * @param prevPhaseWalls
 	 * @param nextPhaseWalls
 	 * @param totTransTime
 	 * @param elapsedTransTime
 	 */
 	abstract public void updatePopulationTransit(WallcologyPhase nextPhase, List<Wall> walls, List<Wall> prevPhaseWalls, List<Wall> nextPhaseWalls, long totTransTime, long elapsedTransTime);
 	
 	
 	/**
 	 * TODO Description
 	 *
 	 * @param ca
 	 * @return
 	 */
 	protected int[] addNoise(int[] ca) {
 		// Randomize +/- noisePercent
 		double dev;
 		for(int i=0; i<5; i++) {
 			dev = Math.random()*((double)ca[i])*noisePercent;
 			if(Math.random()<.5) {
 				ca[i] = (int) Math.round((((double)ca[i]) + dev));
 			} else {
 				ca[i] = (int) Math.round((((double)ca[i]) - dev));
 			}
 		}
 		return ca;
 	}
 }
