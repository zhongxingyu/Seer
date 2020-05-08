 package pacman.entries.ghosts;
 
 import java.util.Random;
 
 import pacman.game.Game;
 import pacman.game.Constants.MOVE;
 
 public class Maze {	
 	private class Route {
 		int from;
 		MOVE startDir;
 		int dist;
 		MOVE arrivalDir;
 		int	to;
 	}
 	private Route 			links[][];
 	private int 			mazeNo;
 	private short[][][][]	junctions;
 	private Game					game;
	private static final Random		rnd = new Random(0);
 
 	
 	public Maze(Game game) {
 		mazeNo = game.getMazeIndex();
 		junctions = new short[game.getJunctionIndices().length][game.getJunctionIndices().length][4][4];
 		this.game = game;
 		
 		/*
 		 * Work out distances from each junction to its neighbour junctions
 		 */
 		int[] jns = game.getJunctionIndices();
 		links = new Route[jns.length][4];
 		for (int j=0; j<jns.length; j++) {
 			for (MOVE d: MOVE.values()) {
 				if (d != MOVE.NEUTRAL && getNeighbour(jns[j], d) != -1)
 					links[j][d.ordinal()] = walkPath(jns[j], d);
 			}
 		}
 		
 		for (int j=0; j<jns.length; j++) {
 			for (MOVE d: MOVE.values()){
 				if (d != MOVE.NEUTRAL && links[j][d.ordinal()] != null)
 					scan(j, links[j][d.ordinal()].to, d, links[j][d.ordinal()].arrivalDir, links[j][d.ordinal()].dist);
 			}
 		}
 	}
 
 	/*
 	 * Given a node, return the node in the given direction or -1 if not a valid move
 	 */
 	public int getNeighbour(int node, MOVE d)
 	{
 		MOVE[]	m = game.getPossibleMoves(node);
 		int[]	n = game.getNeighbouringNodes(node);
 		for (int i=0; i<m.length; i++) {
 			if (m[i] == d)
 				return n[i];
 		}
 		return -1;
 	}
 	
 	private MOVE nextDir(int node, MOVE dir) {
 		for (MOVE m: game.getPossibleMoves(node))
 			if (m != dir.opposite())
 				return m;
 
 		return MOVE.NEUTRAL;
 	}
 	
 	private int junctionID(int node) {
 		int id = 0;
 		for (int j: game.getJunctionIndices()) {
 			if (j == node)
 				return id;
 			id++;
 		}
 		return -1;
 	}
 			
 	private Route walkPath(int from, MOVE dir) {
 		Route r = new Route();
 		r.from = from;
 		r.startDir = dir;
 		r.dist = 1;
 		from = getNeighbour(from, dir);
 		
 		while (dir != MOVE.NEUTRAL && !game.isJunction(from)) {			
 			dir = nextDir(from, dir);
 			from = getNeighbour(from, dir);
 			r.dist++;
 		}
 		
 		r.arrivalDir = dir;
 		r.to = junctionID(from);
 	
 		return r;
 	}
 	
 	private void scan(int from, int to, MOVE fromDir, MOVE toDir, int dist) {
 		if (dist > 350)
 			return;
 		if (junctions[from][to][fromDir.ordinal()][toDir.ordinal()] == 0 || dist < junctions[from][to][fromDir.ordinal()][toDir.ordinal()]) {
 			junctions[from][to][fromDir.ordinal()][toDir.ordinal()] = (short)dist;
 			for (MOVE dir: MOVE.values()) {
 				if (dir != MOVE.NEUTRAL && links[to][dir.ordinal()] != null && toDir != dir.opposite()) {
 					scan(from, links[to][dir.ordinal()].to, fromDir, links[to][dir.ordinal()].arrivalDir, dist+links[to][dir.ordinal()].dist);
 				}
 			}
 		}
 	}
 	
 	public int getCurMaze() {
 		return mazeNo;
 	}
 	
 	/*
 	 * Find the shortest route from the given node to the given node.
 	 * If a toDir is specified then we must arrive in that direction otherwise if a banned direction is specified we cannot arrive in that direction
 	 */
 	public Route ghostRoute(int from , int to, MOVE fromDir, MOVE toDir, MOVE banned) {
 		/*
 		 * Check parameters
 		 */
 		if (from < 0 || from >= game.getNumberOfNodes() || to < 0 || to >= game.getNumberOfNodes() ||
 				getNeighbour(from, fromDir.opposite()) == -1 ||
 				(toDir != MOVE.NEUTRAL && getNeighbour(to, toDir.opposite()) == -1))
 			return null;
 		
 		/*
 		 * If arrival dir is unspecified we must find the shortest route arriving in any direction
 		 */
 		if (toDir == MOVE.NEUTRAL) {
 			Route	options[] = new Route[4];
 			float	dist[] = new float[4];
 			MOVE best = MOVE.NEUTRAL;
 			for (MOVE d: MOVE.values()) {
 				if (getNeighbour(to, d) != -1 && d.opposite() != banned) {
 					options[d.ordinal()] = ghostRoute(from, to, fromDir, d.opposite(), MOVE.NEUTRAL);
 					if (options[d.ordinal()] != null) {
 						dist[d.ordinal()] = options[d.ordinal()].dist + rnd.nextFloat();
 						if (best == MOVE.NEUTRAL || dist[d.ordinal()] < dist[best.ordinal()])
 							best = d;
 					}
 				}
 			}
 			if (best == MOVE.NEUTRAL)
 				return null;
 			return options[best.ordinal()];
 		}		
 		
 		/*
 		 * To get here we must have a defined start and arrival direction
 		 */
 		Route result = new Route();
 		result.from = from;
 		result.startDir = nextDir(from, fromDir);
 		result.dist = 0;
 		result.to = to;
 		result.arrivalDir = toDir;
 		
 		/*
 		 * Scan from end node to the first junction
 		 */		
 		MOVE[] moves;
 		if (game.isJunction(to)) {
 			moves = new MOVE[1];
 			moves[0] = toDir;
 			banned = MOVE.NEUTRAL;
 		} else {
 			MOVE dir = toDir.opposite();
 
 			while (!game.isJunction(to)) {
 				result.dist++;
 				dir = nextDir(to, dir);
 				to = getNeighbour(to, dir);
 				if (to == from && fromDir != dir) {
 					result.startDir = dir.opposite();
 					return result;
 				}
 			}
 			moves = MOVE.values();
 			banned = dir;
 			toDir = dir;
 		}
 		/*
 		 * Scan from start node to first junction
 		 */
 		MOVE dir = fromDir;
 		boolean setStart = true;
 		while (!game.isJunction(from)) {
 			result.dist++;
 			setStart = false;
 			dir = nextDir(from, dir);
 			from = getNeighbour(from, dir);
 		}
 		fromDir = dir;	
 		
 		/*
 		 * At this point the from and to nodes have been updated to the first junctions we reach travelling from the original from and to nodes
 		 * fromDir is the direction of arrival at the from junction (its reverse is therefore the banned direction to leave from this jn)
 		 * toDir is the direction we must head when we get to the end junction
 		 */
 		if (from == to && result.dist > 0) { //we arrived at the same junction - do we need to add on a loop
 			if (banned == MOVE.NEUTRAL && fromDir == toDir) //End node is a junction and we arrived in the right direction
 				return result;
 			if (setStart && fromDir == toDir.opposite()) { //From node is a junction and we travelled from end point to here in the right direction
 				result.startDir = toDir.opposite();
 				return result;
 			}
 			if (banned != MOVE.NEUTRAL && !setStart && fromDir != toDir) //Normal nodes and arrived in different directions
 				return result;
 		}
 		
 		/*
 		 * Find shortest junction to junction route
 		 */
 		int best = Integer.MAX_VALUE;
 		int fjn = junctionID(from);
 		int tjn = junctionID(to);
 		for (MOVE f: MOVE.values())
 			if (f != MOVE.NEUTRAL && f != fromDir.opposite())
 				for (MOVE d: moves)
 					if (d != MOVE.NEUTRAL && d != banned && junctions[fjn][tjn][f.ordinal()][d.ordinal()] > 0 && junctions[fjn][tjn][f.ordinal()][d.ordinal()] < best) {
 						best = junctions[fjn][tjn][f.ordinal()][d.ordinal()];
 						if (setStart)
 							result.startDir = f;
 						if (banned == MOVE.NEUTRAL)
 							result.arrivalDir = d;
 					}
 		result.dist += best;
 		return result;
 	}
 	
 	public int getDistance(int from , int to, MOVE fromDir, MOVE toDir, MOVE banned) {
 		Route r = ghostRoute(from, to, fromDir, toDir, banned);
 		if (r == null)
 			return -1;
 		return r.dist;
 	}
 	
 	public MOVE getStartDir(int from , int to, MOVE fromDir, MOVE toDir, MOVE banned) {
 		Route r = ghostRoute(from, to, fromDir, toDir, banned);
 		if (r == null)
 			return MOVE.NEUTRAL;
 		return r.startDir;
 	}
 	
 	public MOVE getArrivalDir(int from , int to, MOVE fromDir, MOVE toDir, MOVE banned) {
 		Route r = ghostRoute(from, to, fromDir, toDir, banned);
 		if (r == null)
 			return MOVE.NEUTRAL;
 		return r.arrivalDir;
 	}
 }
