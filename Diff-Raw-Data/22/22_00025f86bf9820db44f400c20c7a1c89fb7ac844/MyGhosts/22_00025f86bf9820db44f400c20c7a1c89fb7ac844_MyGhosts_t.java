 package pacman.entries.ghosts;
 
 import java.util.EnumMap;
 import pacman.controllers.Controller;
 import pacman.game.Constants.GHOST;
 import pacman.game.Constants.MOVE;
 import pacman.game.Game;
 import static pacman.game.Constants.*;
 
 /*
 * A simple implementation based on roles assigned to each ghost
 * 1. Chase
 * 2. Intercept
 * 3. Protect power pill
 * 4. Protect pill
  */
 public final class MyGhosts extends Controller<EnumMap<GHOST,MOVE>>
 {
 	private Game 				game;
 	private Maze				maze;
 	
 	public EnumMap<GHOST,MOVE> getMove(Game game,long timeDue)
 	{
 		EnumMap<GHOST,MOVE> myMoves=new EnumMap<GHOST,MOVE>(GHOST.class);
 		
 		this.game = game;
 		if (maze == null || maze.getCurMaze() != game.getMazeIndex())
 			maze = new Maze(game);
 		
 		for (GHOST g: GHOST.values()) {
 			if (game.doesGhostRequireAction(g)) {
 				myMoves.put(g, getDir(g));
 			}
 		}
 		return myMoves;
 	}
 	
 	/*
 	 * Called when an edible ghosts needs a place to hide
 	 * Head away from the pacman and away from other edible ghosts and towards hunting ghosts
 	 * Return the direction of travel
 	 */
 	private MOVE getFrightenedDir(GHOST me) {
 		MOVE best = MOVE.NEUTRAL;
 		double score = -100;
 		int dist[] = new int[NUM_GHOSTS];
 		int nodes[] = new int[NUM_GHOSTS];
 		double scores[] = new double[NUM_GHOSTS]; //Points awarded for moving towards node
 		
 		for (GHOST g: GHOST.values()) {
 			if (g == me) {
 				nodes[g.ordinal()] = game.getPacmanCurrentNodeIndex();
 				scores[g.ordinal()] = -10;
 			} else {
 				nodes[g.ordinal()] = game.getGhostCurrentNodeIndex(g);
 				if (game.isGhostEdible(g))
 					scores[g.ordinal()] = -1;
 				else
 					scores[g.ordinal()] = 1;
 			}
 			dist[g.ordinal()] = (int)game.getDistance(game.getGhostCurrentNodeIndex(me), nodes[g.ordinal()], DM.PATH);
 			if (dist[g.ordinal()] != 0)
 				scores[g.ordinal()] /= dist[g.ordinal()];
 		}
 		
 		for (MOVE d: game.getPossibleMoves(game.getGhostCurrentNodeIndex(me))) {
 			double myScore = 0;
 			int node = maze.getNeighbour(game.getGhostCurrentNodeIndex(me), d);
 			if (node != -1 && d.opposite() != game.getGhostLastMoveMade(me)) {
 				for (GHOST g: GHOST.values()) {
 					int x = (int)game.getDistance(node, nodes[g.ordinal()], DM.PATH);
 					if (x < dist[g.ordinal()])
 						myScore += scores[g.ordinal()];
 					if (x > dist[g.ordinal()])
 						myScore -= scores[g.ordinal()];
 				}
 				if (myScore > score) {
 					score = myScore;
 					best = d;
 				}
 			}
 		}
 
 		return best;	
 	}
 	
 	/*
 	 * Assign the ghost to one of the 4 options
 	 * 1. Head to the PacMan
 	 * 2. Head to the PacMan but arrive in a different direction
 	 * 3. Head to Power pill nearest to PacMan (if non - head to pill nearest to PacMan)
 	 * 4. Head to the pill nearest the PacMan
 	 */
 	private MOVE getDir(GHOST ghost) {
 		int nearestPowerPill = game.getClosestNodeIndexFromNodeIndex(game.getPacmanCurrentNodeIndex(), game.getActivePowerPillsIndices(), DM.PATH);		
 		int nearestPill = game.getClosestNodeIndexFromNodeIndex(game.getPacmanCurrentNodeIndex(), game.getActivePillsIndices(), DM.PATH);
 		if (nearestPowerPill == -1)
 			nearestPowerPill = nearestPill;
 		
 		int[] nodes = { game.getPacmanCurrentNodeIndex(), game.getPacmanCurrentNodeIndex(), nearestPowerPill, nearestPill };
 		MOVE[] banned = new MOVE[NUM_GHOSTS]; //The banned arrival direction for each option
 		int[] choices = new int[NUM_GHOSTS]; //For each ghost - which option we head to
 		
 		for (int g=0;g<NUM_GHOSTS;g++) {
 			banned[g] = MOVE.NEUTRAL;
 			choices[g] = -1;
 		}
 					
 		//Assign the nearest ghost to chase the PacMan
 		for (int o=0; o<NUM_GHOSTS; o++) {
 			GHOST best = null; //The best ghost to fulfil this option
 			int nearest = Integer.MAX_VALUE;
 			for (GHOST g: GHOST.values()) {				
 				if (choices[g.ordinal()] == -1) {
 					int distance;
 					if (game.getGhostLairTime(g) > 0)
 						distance = game.getGhostLairTime(g) + maze.getDistance(game.getGhostInitialNodeIndex(), nodes[o], MOVE.NEUTRAL, MOVE.NEUTRAL, banned[o]);
 					else {
 						distance = maze.getDistance(game.getGhostCurrentNodeIndex(g), nodes[o], game.getGhostLastMoveMade(g), MOVE.NEUTRAL, MOVE.NEUTRAL);
 						if (game.getGhostEdibleTime(g) > distance - EAT_DISTANCE) //Would be edible on arrival
 							distance = Integer.MAX_VALUE;
 						else
 							distance += game.getGhostEdibleTime(g) / 2;
 					}
 					if (distance < nearest) {
 						best = g;
 						nearest = distance;
 					}
 				}
 			}
 			if (best != null) {
 				choices[best.ordinal()] = o;
 				if (o+1 < NUM_GHOSTS && nodes[o] == nodes[o+1]) { //The ghost cannot arrive in the same direction as the previous one
 					if (game.getGhostLairTime(best) > 0)
 						banned[o+1] = maze.getArrivalDir(game.getGhostInitialNodeIndex(), nodes[o], MOVE.NEUTRAL, MOVE.NEUTRAL, MOVE.NEUTRAL);
 					else
 						banned[o+1] = maze.getArrivalDir(game.getGhostCurrentNodeIndex(best), nodes[o], game.getGhostLastMoveMade(best), MOVE.NEUTRAL, MOVE.NEUTRAL);
 				}
 			}
 		}
 		
 		if (choices[ghost.ordinal()] == -1)
 			return getFrightenedDir(ghost);
 		//System.out.printf("Ghost %d - option %d\n", ghost, choices[ghost]);
 		return maze.getStartDir(game.getGhostCurrentNodeIndex(ghost), nodes[choices[ghost.ordinal()]], game.getGhostLastMoveMade(ghost), MOVE.NEUTRAL, banned[choices[ghost.ordinal()]]);
 	}
 }
