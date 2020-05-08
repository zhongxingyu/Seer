 package edu.itba.skbsolver;
 
 import java.util.Deque;
 import java.util.LinkedList;
 import java.util.List;
 
 import edu.itba.skbsolver.exception.TileSetCapacityExceeded;
 
 public class StateSpawner {
 
 	public static final int[] dx = { 0, 1, 0, -1 };
 	public static final int[] dy = { 1, 0, -1, 0 };
 
 	private PositionsTable posTable;
 	private Level level;
 
 	public StateSpawner(PositionsTable posTable, Level level) {
 		this.posTable = posTable;
 		this.level = level;
 	}
 
 	public List<State> childs(State s) {
 		level.logger.info("Listing childs for state: \n"+s.toString());
 		
 		List<State> newStates = new LinkedList<State>();
 		Deque<Integer> queue = new LinkedList<Integer>();
 		Deque<Integer> how = new LinkedList<Integer>();
 		int[][][] distance = new int[level.xsize][level.ysize][4];
 		int[][] boxIndex = new int[level.xsize][level.ysize];
 
 		int px, py, rx, ry, tx, ty, h, p, r, boxMoved;
 		boolean noDeadlock;
 
 		// Initialize auxiliar vectors
 		for (int i = 0; i < level.xsize; i++) {
 			for (int j = 0; j < level.ysize; j++) {
 				boxIndex[i][j] = -1;
 				distance[i][j][0] = -1;
 				distance[i][j][1] = -1;
 				distance[i][j][2] = -1;
 				distance[i][j][3] = -1;
 			}
 		}
 		for (Capacitor cap : level.getCapacitors()) {
 			cap.reset();
 		}
 		try {
 			for (int i = 0; i < s.boxes.length; i++) {
 				boxIndex[s.boxes[i] >> 16][s.boxes[i] & 0xFFFF] = i;
 				for (Capacitor cap : level.getCapacitorsByPos(s.boxes[i] >> 16,
 						s.boxes[i] & 0xFFFF)) {
 					cap.countPlus();
 				}
 			}
 		} catch (TileSetCapacityExceeded tS) { // This should not happen :)
 			return new LinkedList<State>();
 		}
 
 		// Put first element on queue
 		queue.addLast(s.player);
 		how.addLast(0);
 		distance[s.px()][s.py()][0] = 0;
 		distance[s.px()][s.py()][1] = 0;
 		distance[s.px()][s.py()][2] = 0;
 		distance[s.px()][s.py()][3] = 0;
 
 		// BFS for pushes
 		while (!queue.isEmpty()) {
 			p = queue.removeFirst();
 			h = how.removeFirst();
 			px = p >> 16;
 			py = p & 0xFFFF;
 			for (int d = 0; d < 4; d++) {
 				rx = px + dx[d];
 				ry = py + dy[d];
 
 				tx = rx + dx[d];
 				ty = ry + dy[d];
 
 				if (distance[rx][ry][d] == -1 && // Si no visité este tile, y
 												// además:
 
 						level.get(rx, ry) != '#' && // si no hay una pared,
 													// entro, pero solo sí:
 						(boxIndex[rx][ry] == -1 || // <- No hay una caja,
 													// entonces el tile está
 													// vacío
 						(
 						// O hay una caja ahí
 
 						// y es movible (no se choca con nada después):
 						level.get(tx, ty) != '#'
 						&& boxIndex[tx][ty] == -1
 
 						// and is a "step-able" tile
 						&& !level.isBasicDeadlock(tx, ty)
 
 						))) {
 					noDeadlock = true;
 
 					boxMoved = boxIndex[rx][ry];
 
 					// Add the new position to the queue
 					distance[rx][ry][d] = distance[px][py][h] + 1;
 
 					if (boxMoved != -1) {
 
 						int newHash = s.hashIfMove(d, boxMoved);
 
 						if (posTable.has(newHash)) {
 							noDeadlock = false;
 						}
 
 						// Si no dispara un Capacitor Deadlock
 						// This is kind of a easy check, let's do this before
 						// the freeze deadlock
 						if (noDeadlock) {
 							for (Capacitor cap : level.getCapacitorsByPos(tx,
 									ty)) {
 								if (!cap.canIstepInto()) {
 									noDeadlock = false;
 								}
 							}
 						}
 
 						if (noDeadlock) {
 							if (!s.triggersFreezeDeadlock(boxMoved, d)) {
 
 								posTable.add(newHash);
 
 								State newState = new State(s, boxMoved, d,
 										distance[rx][ry][h], newHash);
 
 								// TODO: Bipartite deadlock?
 								// I think bipartite deadlocks should be checked
 								// only if we just inserted a box into a target
 								// :D
 
 								// TODO: Other deadlock???
 
 								newStates.add(newState);
 
 							}
 						}
 					} else {
 						// Lo agrego a la cola
 						queue.addLast((rx << 16) + ry);
 						how.addLast(d);
 					}
 				}
 			}
 		}
 			
 		level.logger.info("Found " + newStates.size() + " childs.");
 		return newStates;
 	}
 
 }
