 package edu.itba.skbsolver;
 
 import java.util.ArrayList;
 import java.util.Deque;
 import java.util.LinkedList;
 import java.util.List;
 
 public class StateSpawner {
 
 	public static final int[] dx = { 0, 1, 0, -1 };
 	public static final int[] dy = { 1, 0, -1, 0 };
 
 	private PositionsTable posTable;
 	private Level level;
 
 	final static Logger logger = Logger.getLogger(false);
 
 	public int countDeadlocks;
 	public int countCapacity;
 	public int countRevisited;
 	public int countNewFreeze;
 	
 	public StateSpawner(PositionsTable posTable, Level level) {
 		this.posTable = posTable;
 		this.level = level;		
 		
 	}
 
 	public List<State> childs(State s, boolean review) {
 		logger.debug("Listing childs for state: \n"+s.toString());
 		
 		List<State> newStates = new ArrayList<State>();
 		Deque<Integer> queue = new LinkedList<Integer>();
 		
 		int[][] distance = new int[level.xsize][level.ysize];
 		int[][] boxIndex = new int[level.xsize][level.ysize];
 
 		countDeadlocks = 0;
 		countCapacity = 0;
 		countRevisited = 0;
 		countNewFreeze = 0;
 		
 		int px, py, rx, ry, tx, ty, p, boxMoved, newHash = 0;
 		boolean noDeadlock;
 
 		// Initialize auxiliar vectors
 		for (int i = 0; i < level.xsize; i++) {
 			for (int j = 0; j < level.ysize; j++) {
 				distance[i][j] = -1;
 				boxIndex[i][j] = -1;
 			}
 		}
 		for (Capacitor cap : level.getCapacitors()) {
 			cap.reset();
 		}
 		for (int i = 0; i < s.boxes.length; i++) {
 			boxIndex[s.boxes[i] >> 16][s.boxes[i] & 0xFFFF] = i;
 			for (Capacitor cap : level.getCapacitorsByPos(s.boxes[i] >> 16,
 					s.boxes[i] & 0xFFFF)) {
 				if (!cap.canIstepInto()){
 					logger.info("But happened!");
 					return new LinkedList<State>();	
 				}
 				cap.countPlus();
 			}
 		}
 
 		// Put first element on queue
 		queue.addLast(s.player);
 		distance[s.px()][s.py()] = 0;
 
 		// BFS for pushes
 		while (!queue.isEmpty()) {
 			
 			p = queue.removeFirst();
 			px = p >> 16;
 			py = p & 0xFFFF;
 			
 			for (int d = 0; d < 4; d++) {
 				rx = px + dx[d];
 				ry = py + dy[d];
 
 				tx = rx + dx[d];
 				ty = ry + dy[d];
 
 				if (distance[rx][ry] == -1 && // Si no visité este tile, y
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
 
 						))) {
 					noDeadlock = true;
 
 					boxMoved = boxIndex[rx][ry];
 
 					if (boxMoved != -1) {
 
 						if (level.isBasicDeadlock(tx, ty)){
 							noDeadlock = false;
 							countDeadlocks++;
 						}
 
 						if (noDeadlock){
 							newHash = s.hashIfMove(d, boxMoved);
 	
 							if (posTable.has(newHash)) {
 								if (review){
 									State st = posTable.get(newHash);
 									
 									if (st.moves > s.moves + distance[px][py]+1){
 										st.moves = s.moves + distance[px][py]+1;
 										st.parent = s;
 										newStates.add(st);
 										countRevisited++;
 									}
 								}
 								
 								noDeadlock = false;
 							}
 						}
 
 						// Si no dispara un Capacitor Deadlock
 						if (noDeadlock) {
 							for (Capacitor cap : level.getCapacitorsByPos(rx, ry)){
 								if (!cap.isEmpty()){
 									cap.countMinus();
 								} else {
 									noDeadlock = false;
 									countCapacity++;
 								}
 							}
 							for (Capacitor cap : level.getCapacitorsByPos(tx, ty)) {
 								if (!cap.canIstepInto()) {
 									noDeadlock = false;
 									countCapacity++;
 								}
 							}
 							for (Capacitor cap : level.getCapacitorsByPos(rx, ry)){
 								if (!cap.isFull()){
 									cap.countPlus();
 								} else {
 									noDeadlock = false;
 									countCapacity++;
 								}
 							}
 						}
 
 						if (noDeadlock) {
 							if (review || !s.triggersFreezeDeadlock(boxMoved, d)) {
 
 								State newState = new State(s, boxMoved, d,
 										distance[px][py]+1, newHash);
 
 								posTable.add(newHash, newState);
 
 								newStates.add(newState);
 								
 								noDeadlock = false;
 							}
 							if (!noDeadlock){
 								countNewFreeze++;
 							}
 						}
 					} else {
 						// Lo agrego a la cola
 
 						distance[rx][ry] = distance[px][py] + 1;
 						
 						queue.addLast((rx << 16) + ry);
 					}
 				}
 			}
 		}
 			
 		logger.debug("Found " + newStates.size() + " childs.");
 		return newStates;
 	}
 
 }
