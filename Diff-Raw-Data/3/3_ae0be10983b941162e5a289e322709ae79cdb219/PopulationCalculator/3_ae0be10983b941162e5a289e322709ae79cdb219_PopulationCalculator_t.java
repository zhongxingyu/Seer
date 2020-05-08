 package ltg.ps.phenomena.wallcology.population_calculators;
 
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ltg.ps.phenomena.wallcology.Wall;
 import ltg.ps.phenomena.wallcology.WallcologyPhase;
 
 public abstract class PopulationCalculator {
 	
 	// Logger
     protected final Logger log = LoggerFactory.getLogger(this.getClass());
     // Noise amount
  	public static final double noisePercent = 0.02; 
 	
  	
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
 		int[] ra = new int[ca.length];
 		for(int i=0; i< ca.length; i++) {
 			dev = Math.round(Math.random()*((double)ca[i])*noisePercent);
			// Make sure that the deviation is always at least one for all creatures that are not 0
			if (dev < 1 && ca[i] != 0) dev = 1; 
 			if(Math.random()<.5) {
 				ra[i] = ca[i] + (int)dev;
 			} else {
 				ra[i] = ca[i] - (int)dev;
 			}
 		}
 		return ra;
 	}
 }
