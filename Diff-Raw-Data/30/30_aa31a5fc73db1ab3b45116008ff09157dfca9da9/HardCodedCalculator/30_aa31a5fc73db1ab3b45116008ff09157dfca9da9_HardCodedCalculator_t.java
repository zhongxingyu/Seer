 /*
  * Created Dec 1, 2011
  */
 package ltg.ps.phenomena.wallcology.population_calculators;
 
 import java.util.List;
 
 import ltg.ps.phenomena.wallcology.Wall;
 import ltg.ps.phenomena.wallcology.WallcologyPhase;
 
 /**
  * This is the calculator used to test or during emergencies.
  * The population of each creature is set in each wall according to
  * the data array <code>wall_population</code>.
  * Each line is a wall and the column represent scum, fuzz, scum-eaters,
  * fuzz-eaters and predator amounts respectively.
  *
  * @author Gugo
  */
 public class HardCodedCalculator extends PopulationCalculator {
 	
 	// Data array
 	private int[][] wall_population = {
				{64, 	45, 	24, 	10, 	5},
				{32, 	45, 	10,		3,		3},
				{32,	75, 	10, 	10,		6},
				{64,	15, 	24,		3, 		5},
				{64, 	45, 	24, 	10, 	5},
				{32, 	45, 	10,		3,		3},
				{32,	75, 	10, 	10,		6},
				{64,	15, 	24,		3, 		5}
 		};
 
 	
 	/* (non-Javadoc)
 	 * @see ltg.ps.phenomena.wallcology.population_calculators.PopulationCalculator#updatePopulationStable(java.util.List, ltg.ps.phenomena.wallcology.WallcologyPhase)
 	 */
 	@Override
 	public void updatePopulationStable(List<Wall> currentPhaseWalls, WallcologyPhase currentPhase) {
 		for (Wall w: currentPhaseWalls) {
 			int id = Integer.parseInt(w.getId()) - 1;
 			int[] ca = wall_population[id];
 			ca = addNoise(ca);
 			w.setPopulationsFromModel(ca);
 		}
 	}
 
 	
 	/* (non-Javadoc)
 	 * @see ltg.ps.phenomena.wallcology.population_calculators.PopulationCalculator#updatePopulationTransit(ltg.ps.phenomena.wallcology.WallcologyPhase, java.util.List, java.util.List, java.util.List, long, long)
 	 */
 	@Override
 	public void updatePopulationTransit(WallcologyPhase nextPhase,
 			List<Wall> walls, List<Wall> prevPhaseWalls,
 			List<Wall> nextPhaseWalls, long totTransTime, long elapsedTransTime) {
 		// TODO Auto-generated method stub
 
 	}
 
 }
