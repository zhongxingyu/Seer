 package edu.itba.skbsolver;
 
 import java.awt.Point;
 import java.util.ArrayList;
 import java.util.Deque;
 import java.util.LinkedList;
 import java.util.List;
 
 public class Solution {
 
 	final static Logger logger = Logger.getLogger(false);
 
 	public static final int[] dx = { 0, 1, 0, -1 };
 	public static final int[] dy = { 1, 0, -1, 0 };
 
 	Deque<String> transitions = null;
 	Deque<State> states = null;
 	int movements = -1;
 
 	public Solution(State winner) {
 		if (winner != null) {
 			this.states = new LinkedList<State>();
 			this.transitions = new LinkedList<String>();
 			this.movements = winner.moves;
 
 			states.addFirst(winner);
 
 			logger.info("The solution is: \n" + winner.toString());
 			
 			while (winner.parent != null) {
 				State current = winner.parent;
 				transitions.addFirst(getTransition(current, winner));
 				states.addFirst(winner);
 				winner = winner.parent;
 			}
 		}
 	}
 
 	private static String getTransition(State current, State next) {
 		int px, py, rx, ry;
 		px = current.player >> 16;
 		py = current.player & 0xFFFF;
 
 		int[][] lug = new int[current.map.xsize][current.map.ysize];
 		char[][] map = new char[current.map.xsize][current.map.ysize];
 
 		for (int i = 0; i < current.map.xsize; i++) {
 			for (int j = 0; j < current.map.ysize; j++) {
 				lug[i][j] = -1;
 				map[i][j] = current.map.get(i, j);
 			}
 		}
 		for (int box : current.boxes) {
 			map[box >> 16][box & 0xFFFF] = '#';
 		}
 
 		Deque<Point> queue = new LinkedList<Point>();
 
 		queue.addLast(new Point(px, py));
 		lug[px][py] = 0;
 
 		StringBuilder sol = new StringBuilder();
 		List<String> sl = new ArrayList<String>();
 
 		while (!queue.isEmpty()) {
 			Point p = queue.removeFirst();
 
 			px = p.x;
 			py = p.y;
 
 			for (int d = 0; d < 4; d++) {
 				rx = px + dx[d];
 				ry = py + dy[d];
 
 				if (d == next.direction
 						&& (rx<<16) + ry == next.player
 					){
 
 					lug[rx][ry] = d;
 					
 					px = current.player >> 16;
 					py = current.player & 0xFFFF;
 					
					char[] how = { 'l', 'u', 'r', 'd' };
 					
 					StringBuffer temp = new StringBuffer();
 
 					while (px != rx || py != ry) {
 						d = lug[rx][ry];
 						temp.append(how[d]);
 						rx -= dx[d];
 						ry -= dy[d];
 					}
 
					temp = temp.reverse();
					sl.add(temp.toString());
					
					queue.clear();
					break;
 
 				} else if (map[rx][ry] != '#' && lug[rx][ry] == -1) {
 				
 					lug[rx][ry] = d;
 					queue.addLast(new Point(rx, ry));
 				}
 			}
 		}
 		
		for(int i = sl.size()-1; i >= 0; i--){
			sol.append(sl.get(i));
		}
		
		return sol.toString();
 	}
 
 }
