 package pacman.entries.pacman;
 
 
 import java.awt.Color;
 import java.util.ArrayList;
 import java.util.Random;
 
 import pacman.controllers.Controller;
 import pacman.game.Constants.GHOST;
 import pacman.game.Constants.MOVE;
 import pacman.game.Game;
 import pacman.game.GameView;
 
 import static pacman.game.Constants.*;
 
 /*
  * This is the class you need to modify for your entry. In particular, you need to
  * fill in the getAction() method. Any additional classes you write should either
  * be placed in this package or sub-packages (e.g., game.entries.pacman.mypackage).
  */
 public class MyPacMan extends Controller<MOVE>
 {
 	private class ghostInfo {
 		public int		distance;	// Distance to the node
 		public MOVE		dir;		//Direction of arrival
 		public boolean	hunter;		//true if the ghost is hunting, false if edible
 		public int		turns;		//How many turn choices ghost makes to get here
 		
 		public ghostInfo() {
 			this.distance = Integer.MAX_VALUE;
 			this.dir = MOVE.NEUTRAL;
 			this.hunter = false;
 			this.turns = 0;
 		}
 	}
 	
 	private class pacmanInfo {
 		public int		distance;	//Distance to node
 		public MOVE		dir;		//Direction of arrival
 		
 		public pacmanInfo() {
 			this.distance = Integer.MAX_VALUE;
 			this.dir = MOVE.NEUTRAL;
 		}
 	}
 	
 	private Game 			game;
 	private pacmanInfo[]	pacman;	//Distance of pacman from each node - updated each game tick
 	private ghostInfo[][]	ghosts; //Distance of each ghost from each node - updated each game tick
 	private float[] 		block; //The combined odds for the ghosts to block each node
 	private float[]			scores; //The score for each node
 	private int[]			bestPath; //The nodes that make up the best path for the pacman to take
 	private float			bestScore; //The total score of all nodes on the best path
 	
 	private static final Random			rnd = new Random(); //Used to break tie breaks in paths with the same score
 	
 	private static final int	ESCAPE = 70; //Score given to an escape route (as opposed to a power pill which is 50)
 	private static final boolean PATH_DEBUG = false;
 	private static final boolean ZONE_DEBUG = false;
 	
 	//Place your game logic here to play the game as Ms Pac-Man
 	public MOVE getMove(Game game,long timeDue)
 	{		
 		this.game = game;
         int best = bestNode();
         MOVE dir = bestDir(best);
         
         return dir;
 	}
 	
 	private int ghostIndex(GHOST g)
 	{
 		switch (g) {
 		case BLINKY: return 0;
 		case PINKY: return 1;
 		case INKY: return 2;
 		case SUE: return 3;
 		}
 		return -1;
 	}
 	
 	private float initialiseScores() {
 		/*
 		 * Set the score for each reachable node to 1 and unreachable to -1
 		 */
 		float result = 0; //Percentage of node that are safe
 		block = new float[game.getNumberOfNodes()];
 		scores = new float[game.getNumberOfNodes()];
 		for (int node=0; node<scores.length; node++) {
 			scores[node] = -1;
 			float chance = 1f;
 			for (GHOST g: GHOST.values())
 				if (ghosts[ghostIndex(g)][node].hunter)
 					chance *= (1f-1f/(1+ghosts[ghostIndex(g)][node].turns));
 			block[node] = 1f-chance;
 			if (game.getNeighbouringNodes(node).length > 0) {
 				GHOST nearestHunter = nearestBlocker(node);
 				if (nearestHunter == null || ghosts[ghostIndex(nearestHunter)][node].distance - pacman[node].distance > EAT_DISTANCE) { //Safe
 					scores[node] = 1;
 					result++;
 					if (ZONE_DEBUG)
 						GameView.addPoints(game, new Color(0, weighting(node), 0), node);
 				}
 			}
 		}
 		return (100*result) / game.getNumberOfNodes();
 	}
 	
 	private int scoreEscapeRoutes() {
 		final Color[] ghostColour = new Color[] { Color.red, Color.pink, Color.orange, Color.cyan };
 		
 		//Score escape routes
 		int []eventHorizon = getEventHorizon();
 		for (int n: eventHorizon) {
 			int jn = nearestJunction(n);
 			if (jn != -1)
 				scores[jn] += ESCAPE*weighting(jn);
 			if (ZONE_DEBUG)
 				GameView.addPoints(game, ghostColour[ghostIndex(nearestBlocker(n))], n);
 		}
 		
 		return eventHorizon.length;
 	}
 	
 	private GHOST nearestBlocker(int node)
 	{
 		int nearestHunter = Integer.MAX_VALUE;
 		GHOST id = null;
 		
 		//Find nearest Hunting Ghost going in a different direction to the pacman
 		for (GHOST g: GHOST.values()) {
 			int dist = ghosts[ghostIndex(g)][node].distance;
 			if (ghosts[ghostIndex(g)][node].hunter && dist < nearestHunter && ghosts[ghostIndex(g)][node].dir != pacman[node].dir) {
 				nearestHunter = dist;
 				id = g;
 			}
 		}
 		
 		return id;
 	}
 	
 	private float weighting(int node)
 	{
 		return 1f-block[node];
 	}
 
 	private void scorePills(boolean safe) {
 		for (int p: game.getActivePillsIndices())
 			if (scores[p] >= 0)
 				scores[p] += PILL;
 		
 		/*
 		 * Score the best power pill - we want to wait near to these until we have to eat it
 		 * This is achieved by setting the score to zero if we are within 1 of the EAT_DISTANCE but the nearest ghost is not
 		 * Pick the nearest reachable power pill that wants to be eaten.
 		 */
 		int dist = Integer.MAX_VALUE;
 		int pp = -1;
 		boolean preferred = false; //set to true if we are only comparing power pills we want to eat
 		for (int p: game.getActivePowerPillsIndices()) {
 			boolean eat = eatMe(p); //Do we want to eat this power pill
 			if (scores[p] >= 0 && ((eat && !preferred) || ((eat || !preferred) && pacman[p].distance < dist))) {
 				if (eat)
 					preferred = true;
 				pp = p;
 				dist = pacman[p].distance;
 			}
 		}
 		if (pp != -1) { //This is the nearest power accessible power pill
 			boolean eat = eatMe(pp);
 			//If we don't want to eat the power pill yet we hover near by
 			//This is achieved by not scoring the pp when we are 1 node away if we are safe
 			boolean scoreIt = (!safe || eat);
 			if (!scoreIt) {
 				if (pacman[pp].distance - EAT_DISTANCE > 1)
 					scoreIt = true;
 				else {
 					GHOST g = nearestBlocker(pp);
 					if (g != null && pacman[game.getGhostCurrentNodeIndex(g)].distance <= EAT_DISTANCE + 4)
 						scoreIt = true;
 				}
 			}
 			if (scoreIt) {
 				scores[pp] += POWER_PILL;
 				if (!safe)
 					scores[pp] += ESCAPE*weighting(pp);
 				if (eat)
 					scores[pp] += 350;	//This makes it more valuable than a single ghost but not more than the second ghost
 			}
 		}
 	}
 	
 	private void scoreEdibleGhosts() {
 		/*
 		 * Score edible ghosts
 		 * If the ghost can reach a junction before we catch it then we will have to chase it, otherwise we might be quicker intercepting it
 		 */
 		int ng = -1;	//nearest edible ghost
 		int nd = Integer.MAX_VALUE;	//distance to nearest edible ghost
 		for (GHOST g: GHOST.values()) {
 			if (game.getGhostEdibleTime(g) > 0) {
 				int jn = getGhostNode(g, Integer.MAX_VALUE);	//The junction the ghost is heading to
 				int chase; //Distance travelled to catch ghost
 				int target;
 				if (pacman[jn].distance <= ghosts[ghostIndex(g)][jn].distance) { //We are closer to the junction so we will catch it on the current path
					//Work out if it is quicker to chase the ghost or intercept it
					if (pacman[game.getGhostCurrentNodeIndex(g)].dir == game.getGhostLastMoveMade(g)) { //Chase distance is twice current distance
						target = getGhostNode(g, pacman[game.getGhostCurrentNodeIndex(g)].distance);
						chase = 2*pacman[target].distance;
					} else { //Intercept distance is 2/3 current distance
						target = getGhostNode(g, pacman[game.getGhostCurrentNodeIndex(g)].distance/3);
						chase = 2*pacman[target].distance/3;
					}
 				} else { //We head to junction then chase it
 					target = jn;
					chase = 2*pacman[target].distance-ghosts[ghostIndex(g)][target].distance; //DT - bug fix, was 3*
 				}
				if (scores[target] >= 0 && game.getGhostEdibleTime(g) - chase > EAT_DISTANCE && chase < nd) {
 					nd = chase;
 					ng = target;
 				}
 			}
 		}
 		if (ng != -1) //Add bonus points for nearest ghost
 			scores[ng] += 400;
 	}
 	
 	/*
 	 * Populates the score for each node
 	 * -1 means unsafe, anything positive means safe, the higher the score the better
 	 */
 	private void scoreNodes() {
 		walkMaze();		
 		float safeZone = initialiseScores();
 		int routes = Integer.MAX_VALUE;
 		
 		if (safeZone < 80)
 			routes = scoreEscapeRoutes();
 		boolean safe = true;
 		if (routes <= NUM_GHOSTS && safeZone < 50)
 			safe = false;
 		else if (safeZone < 20)
 			safe = false;
 		scorePills(safe);	
 		scoreEdibleGhosts();
 	}
 	
 	/*
 	 * Find the node where the ghost will be in N moves time
 	 * Stop if we reach a junction
 	 */
 	private int getGhostNode(GHOST g, int turns) {
 		MOVE dir = game.getGhostLastMoveMade(g);
 		int node = game.getGhostCurrentNodeIndex(g);
 		
 		if (game.getGhostLairTime(g) > 0)
 			return node;
 		
 		while (!game.isJunction(node) && turns-- > 0) {
 			dir = game.getPossibleMoves(node, dir)[0];
 			node = game.getNeighbour(node, dir);
 		}
 		
 		return node;
 	}
 	
 	private boolean isPowerPill(int node) {
 		for (int pp: game.getActivePowerPillsIndices())
 			if (pp == node)
 				return true;
 		return false;
 	}
 	
 	/*
 	 * Find all nodes that are unsafe and have an safe neighbour
 	 */
 	private int [] getEventHorizon() {
 		ArrayList<Integer> edge = new ArrayList<Integer>();
 		
 		for (int n=0; n<game.getNumberOfNodes(); n++) {
 			if (scores[n] < 0) {
 				for (int next: game.getNeighbouringNodes(n)) {
 					if (scores[next] >= 0 && !isPowerPill(next)) {
 						edge.add(n);
 						break;
 					}
 				}
 			}
 		}
 		int [] result = new int[edge.size()];
 		for (int i=0; i<edge.size(); i++)
 			result[i] = edge.get(i);
 		return result;
 	}
 	
 	/*
 	 * Find the nearest junction to this node in the safe zone
 	 */
 	private int nearestJunction(int node) {	
 		int prev = -1;
 		while (!game.isJunction(node)) {
 			boolean trapped = true;
 			for (int next: game.getNeighbouringNodes(node))
 				if (next != prev && scores[next] != -1) {
 					prev = node;
 					node = next;
 					trapped = false;
 					break;
 				}
 			if (trapped)
 				return -1;
 		}
 		return node;
 	}
 	
 	/*
 	 * Returns false if we don't need to eat this power pill
 	 * We allow a power pill to be eaten at the end of each quarter of the level (i.e. every 750 ticks)
 	 * We eat it if 3 ghosts are within range
 	 * If 4 ghosts are in range we allow the next pill to be eaten early.
 	 * At the end of the level we eat power pills regardless of how many ghosts are in range as it keeps up safe
 	 */
 	private boolean eatMe(int node)
 	{
 		int edibleTime = (int)(EDIBLE_TIME*(Math.pow(EDIBLE_TIME_REDUCTION,game.getCurrentLevel())));
 		int timeRemaining = LEVEL_LIMIT - game.getCurrentLevelTime();
 		int powerPillsRemaining = game.getActivePowerPillsIndices().length;
 		int timeToEat = pacman[node].distance + (edibleTime+120)*powerPillsRemaining;
 		if (timeRemaining < timeToEat)
 			return true;
 		
 		/*
 		 * Estimate how many ghosts we could eat for this power pill
 		 * We head to the nearest ghost first and then repeat for the next nearest ghost.
 		 * We assume worst case scenario, where we have to chase down the edible ghost which doubles the distance travelled.
 		 */
 		int count = 0;
 		int dist = 0;
 		boolean processed[] = new boolean[NUM_GHOSTS];
 		int from = game.getPacmanCurrentNodeIndex();
 		for (int added=0; added<NUM_GHOSTS; added++) {
 			//Find nearest unprocessed ghost that would be edible
 			GHOST ng = null;
 			int gd = Integer.MAX_VALUE;
 			for (GHOST g: GHOST.values()) {
 				if (!processed[ghostIndex(g)] && game.getGhostLairTime(g) < pacman[node].distance + EAT_DISTANCE) {
 					int path = (int)game.getDistance(from, (game.getGhostLairTime(g) > 0)?game.getGhostInitialNodeIndex():game.getGhostCurrentNodeIndex(g), DM.PATH);
 					if (path < gd) {
 						ng = g;
 						gd = path;
 					}
 				}
 			}
 			if (ng == null)
 				break;	//No more ghosts
 			
 			processed[ghostIndex(ng)] = true;
 			if (added > 0)
 				dist += gd;
 			dist += 2*gd; //It takes twice the distance to chase down a ghost moving at half speed.
 			from = game.getGhostCurrentNodeIndex(ng);
 			if (dist <= edibleTime + EAT_DISTANCE)
 				count++;
 			else
 				break;
 		}
 		
 		int maxPowerPills = game.getPowerPillIndices().length;
 		int pillsToEat = maxPowerPills*game.getCurrentLevelTime()/LEVEL_LIMIT;
 		if (count >= NUM_GHOSTS -1 && pillsToEat > maxPowerPills - powerPillsRemaining)
 			return true;
 		if (count == NUM_GHOSTS && pillsToEat >= maxPowerPills - powerPillsRemaining)
 			return true;
 		return false;
 	}
 	/*
 	 * How many valid move choices does a ghost have at this node
 	 */
 	private int countChoices(int node)
 	{
 		int choices = game.getNeighbouringNodes(node).length;
 		if (choices > 2)
 			return choices - 1;	//Can't go backwards so one less choice
 		return 0; //if we are not at a junction we have no choices (only 1 valid move)
 	}
 	
 	/*
 	 * Power pills and hunter ghosts block the maze walk
 	 */
 	private boolean isBlocked(int node) {
 		if (isPowerPill(node))
 			return true;
 		for (GHOST g: GHOST.values()) {
 			/*
 			 * Only hunter ghosts can block and then only if they are
 			 * 1. Get to the node before us from a different direction
 			 * 2. Are travelling in the same direction and we are within 2 nodes of the eat distance from us as a game.reverse would kill us
 			 */
 			if (ghosts[ghostIndex(g)][node].hunter &&
 					((ghosts[ghostIndex(g)][node].dir != pacman[node].dir && ghosts[ghostIndex(g)][node].distance - pacman[node].distance <= EAT_DISTANCE) ||
 					 (ghosts[ghostIndex(g)][node].dir == pacman[node].dir && ghosts[ghostIndex(g)][node].distance < pacman[node].distance && pacman[node].distance - ghosts[ghostIndex(g)][node].distance <= EAT_DISTANCE + 2)))
 				return true;
 		}
 		return false;
 	}
 	
 	/*
 	 * Returns the id of the highest scoring node
 	 */
 	private int bestNode() {
 		int best = -1;
 		scoreNodes();
 		for (int i=0; i<scores.length; i++)
 			if (scores[i] >= 0 && (best == -1 || scores[i] > scores[best]) && i != game.getPacmanCurrentNodeIndex())
 				best = i;
 
 		return best;
 	}
 
 	/*
 	 * Travel backwards from the end point to the current pacman position
 	 */
 	private void travel(float [] pathScores, int node, int dist, float score, ArrayList<Integer> path) {
 		if (pathScores[node] == 0 || score > pathScores[node]) {
 			pathScores[node] = score;
 			path.add(0, node);
 			if (dist > 1) {
 				for (MOVE m: game.getPossibleMoves(node)) {
 					int next = game.getNeighbour(node, m);
 					if (pacman[next].distance == dist-1)
 						travel(pathScores, next, dist-1, score+scores[next], path);
 				}
 			} else if (bestPath == null || path.size() < bestPath.length ||
 					(path.size() == bestPath.length && (score > bestScore || (score == bestScore && rnd.nextInt(2) == 0)))) {
 				bestPath = new int[path.size()+1];
 				bestPath[0] = game.getPacmanCurrentNodeIndex();
 				bestScore = score;
 				for (int i=0; i<path.size(); i++)
 					bestPath[i+1] = path.get(i);
 			}
 			path.remove(0);
 		}
 	}
 	
 	/*
 	 * Works out the path to the given node and returns the direction to head safely to the first node in the path
 	 */
 	private MOVE bestDir(int to) {
 		if (to == -1) {
 			//System.out.printf("No best move found - tick %d\n", game.getCurrentLevelTime());
 			return MOVE.NEUTRAL;
 		}
 
 		//Walk the safe nodes (score > 0) and find the path with the shortest distance
 		float[] pathScore = new float[game.getNumberOfNodes()]; //Total score of all nodes on path
 		bestPath = null;
 		bestScore = 0;
 		travel(pathScore, to, pacman[to].distance, scores[game.getPacmanCurrentNodeIndex()], new ArrayList<Integer>());
 		if (bestPath != null && bestPath.length > 1) {
 			for (int i=1; i<bestPath.length; i++)
 			if (PATH_DEBUG && game.getNodeXCood(bestPath[i]) != 0 && game.getNodeXCood(bestPath[i-1]) != 0)
 				GameView.addLines(game, Color.YELLOW, bestPath[i], bestPath[i-1]);
 
 			for (MOVE d: game.getPossibleMoves(bestPath[0]))
 				if (game.getNeighbour(bestPath[0], d) == bestPath[1])
 					return d;
 		}
 		
 		return MOVE.NEUTRAL;
 	}
 	
 	class nodeMove {
 		int node;
 		MOVE move;
 		GHOST ghost;
 		int turns;
 		
 		public nodeMove(int n, MOVE m, GHOST g, int t) {
 			node = n;
 			move = m;
 			ghost = g;
 			turns = t;
 		}
 		
 		public nodeMove(int n, MOVE m) {
 			node = n;
 			move = m;
 			turns = -1;
 		}
 		
 		@Override
 	    public boolean equals (Object x) {
 	      if (x instanceof nodeMove)
 	         return ((nodeMove)x).node == node && ((nodeMove)x).move == move && ((nodeMove)x).ghost == ghost;
 	      return false;
 	    }
 
 	    @Override
 	    public int hashCode ()
 	    {
 	        return node;
 	    }
 	}
 	
 	/*
 	 * Using a breadth first search we spread out from the edge of our reachable area for each ghost and the pacman
 	 * At each node we record the distance travelled and the arrival direction
 	 */
 	private void walkMaze() {
 		ArrayList<nodeMove>	pacmanEdge = new ArrayList<nodeMove>();
 		ArrayList<nodeMove>	ghostsEdge = new ArrayList<nodeMove>();
 		pacman = new pacmanInfo[game.getNumberOfNodes()];
 		ghosts = new ghostInfo[NUM_GHOSTS][game.getNumberOfNodes()];
 		
 		for (int i=0; i<game.getNumberOfNodes(); i++) {
 			pacman[i] = new pacmanInfo();
 			for (int g=0; g<NUM_GHOSTS; g++)
 				ghosts[g][i] = new ghostInfo();
 		}
 		
 		/*
 		 * Populate starting edges
 		 */
 		int distance = 0;
 		pacmanEdge.add(new nodeMove(game.getPacmanCurrentNodeIndex(), MOVE.NEUTRAL));
 		for (GHOST g: GHOST.values())
 			if (game.getGhostLairTime(g) == 0)
 				ghostsEdge.add(new nodeMove(game.getGhostCurrentNodeIndex(g), game.getGhostLastMoveMade(g), g, 0));
 		
 		
 		while (ghostsEdge.size() == 0) { //The the ghosts must be in the lair
 			distance++;
 			for (GHOST g: GHOST.values())
 				if (game.getGhostLairTime(g) == distance)
 					ghostsEdge.add(new nodeMove(game.getGhostInitialNodeIndex(), MOVE.NEUTRAL, g, 0));
 		}
 		//First store the ghost distances
 		while (ghostsEdge.size() > 0) { //Keep walking while we have somewhere to go
 			//Record distance travelled to all edge nodes	
 			for (nodeMove n: ghostsEdge) {
 				int gi = ghostIndex(n.ghost);
 				ghosts[gi][n.node].distance = distance;
 				ghosts[gi][n.node].dir = n.move;
 				ghosts[gi][n.node].hunter = (ghosts[gi][n.node].distance > game.getGhostEdibleTime(n.ghost));
 				ghosts[gi][n.node].turns = n.turns;
 			}
 				
 			/*
 			 * New edge is 1 move from each point on the current edge
 			 * Ghosts stop when they reach a node they have already been to
 			 */
 			ArrayList<nodeMove>	nextGhosts = new ArrayList<nodeMove>();			
 			for (nodeMove gi: ghostsEdge) {
 				int edibleTime = game.getGhostEdibleTime(gi.ghost) - distance;
 				if (edibleTime <= 0 || edibleTime%GHOST_SPEED_REDUCTION != 0) {
 					for (MOVE gm: game.getPossibleMoves(gi.node, gi.move)) {
 						int node = game.getNeighbour(gi.node, gm);
 						if (ghosts[ghostIndex(gi.ghost)][node].distance == Integer.MAX_VALUE) {
 							nodeMove next = new nodeMove(node, gm, gi.ghost, gi.turns + countChoices(gi.node));
 							if (!nextGhosts.contains(next))
 								nextGhosts.add(next);
 						}
 					}
 				} else //edible ghost doesn't move - add in the current location
 					nextGhosts.add(gi);
 			}
 			
 			
 			ghostsEdge = nextGhosts;
 			distance++;
 			/*
 			 * Add in a new entry if a ghost is due to leave the lair
 			 */
 			for (GHOST g: GHOST.values())
 				if (game.getGhostLairTime(g) > 0 && game.getGhostLairTime(g) == distance)
 					ghostsEdge.add(new nodeMove(game.getGhostInitialNodeIndex(), MOVE.NEUTRAL, g, 0));
 		}	
 		
 		//Now store the pacman distances - the pacman is blocked by hunting ghosts
 		distance = 0;
 		while (pacmanEdge.size() > 0) {
 			for (nodeMove n: pacmanEdge) {
 				pacman[n.node].distance = distance;
 				pacman[n.node].dir = n.move;
 			}
 			/*
 			 * New edge is 1 move from each point on the current edge
 			 * Pacman will not move onto power pills, hunting ghosts or on a node we have already been to
 			 */
 			ArrayList<nodeMove>	nextPacman = new ArrayList<nodeMove>();
 			for (nodeMove n: pacmanEdge)
 				for (MOVE d: game.getPossibleMoves(n.node)) {
 					int node = game.getNeighbour(n.node, d);
 					if (pacman[node].distance == Integer.MAX_VALUE && !isBlocked(n.node)) {
 						if (!nextPacman.contains(node))
 							nextPacman.add(new nodeMove(node, d));
 					}
 				}
 			
 			pacmanEdge = nextPacman;
 			distance++;
 		}
 	}
 }
