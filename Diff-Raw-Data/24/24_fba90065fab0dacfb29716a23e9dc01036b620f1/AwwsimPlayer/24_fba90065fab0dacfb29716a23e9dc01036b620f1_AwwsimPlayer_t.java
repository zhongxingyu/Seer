 package isnork.g3;
 
 import java.awt.geom.Point2D;
 import java.util.Set;
 
 import isnork.sim.GameObject.Direction;
 import isnork.sim.Observation;
 import isnork.sim.Player;
 import isnork.sim.SeaLifePrototype;
 import isnork.sim.iSnorkMessage;
 
 public class AwwsimPlayer extends Player {
 	String name = "g3: Awwsim Player";
 	private Direction nextMove;
 	private int numDivers;
 	private int viewRadius;
 	// private int penalty;
 	private Cartogram carto;
 
 	private AbstractPokedex dex;
 
 	@Override
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * Reset defaults, set up strategy
 	 */
 	@Override
 	public void newGame(Set<SeaLifePrototype> seaLifePossibilities,
 			int penalty, int d, int r, int n) {
 		int mapWidth = d * 2 + 1;
 		dex = new WaterProofPokedex(seaLifePossibilities);
 		viewRadius = r;
 		numDivers = n;
 
 		carto = new WaterProofCartogram(mapWidth, viewRadius, numDivers, dex);
 		WaterProofPrecomputation precomp = new WaterProofPrecomputation(mapWidth, r, dex);
 		System.out.println(precomp.naiveHighScore());
		System.out.println(precomp.creatureDensity()*precomp.dangerousToTotalRatio()*precomp.movingToStationaryDangerousRatio());
 		
 
 		// TODO add these back in when we need them
 		// commented them out because they were triggering warnings on
 		// dead code which made me nervous
 
 		// this.penalty = penalty;
 
 		// TODO analysis on species precomputation
 		// TODO swimming strategy precomputation
 	}
 
 	/**
 	 * Process input, determine message to be sent
 	 */
 	@Override
 	public String tick(Point2D myPosition, Set<Observation> whatYouSee,
 			Set<iSnorkMessage> incomingMessages,
 			Set<Observation> playerLocations) {
 		try {
 			carto.update(myPosition, whatYouSee, playerLocations,
 					incomingMessages);
 			nextMove = carto.getNextDirection();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		return carto.getMessage();
 	}
 
 	/**
 	 * Move should be stored based on information in tick, then return it
 	 */
 	@Override
 	public Direction getMove() {
 		return nextMove;
 	}
 }
