 package edu.itba.skbsolver;
 
 import java.awt.Point;
 import java.util.Deque;
 import java.util.LinkedList;
 
 public class Solution {
 	
 	public static final int[] dx = {0,1,0,-1};
 	public static final int[] dy = {1,0,-1,0};
 	
 	Deque<String> transitions;
 	Deque<State> states;
 	int movements;
 
 	public Solution(State winner) {
 		this.states = new LinkedList<State>();
 		this.transitions = new LinkedList<String>();
 		this.movements = winner.moves;
 		
 		states.addFirst(winner);
 		
 		while (winner.parent != null){
 			State current = winner;
 			winner = winner.parent;
 			transitions.addFirst(getTransition(current, winner));
 			states.addFirst(winner);
 		}
 	}
 
 	private static String getTransition(State current, State next) {
 		int px, py, rx, ry;
 		px = current.player >> 16;
 		py = current.player & 0xFFFF;
 		
 		int[][] lug = new int[current.map.xsize][current.map.ysize];
 		char[][] map = new char[current.map.xsize][current.map.ysize];
 		
 		for (int i = 0; i < current.map.xsize; i++){
 			for (int j = 0; j < current.map.ysize; j++){
 				lug[i][j] = -1;
 				map[i][j] = current.map.get(i, j);
 			}
 		}
 		for (int box: current.boxes){
 			map[box >> 16][box & 0xFFFF] = '#';
 		}
 		
 		Deque<Point> queue = new LinkedList<Point>();
 		
 		queue.addLast(new Point(px, py));
 		lug[px][py] = 0;
		map[next.player>>16][next.player & 0xFFFF] = ' ';
 		
 		while (!queue.isEmpty()){
 			Point p = queue.removeFirst();
 			
 			px = p.x;
 			py = p.y;
 			
 			for (int d = 0; d < 4; d++){
 				rx = px + dx[d];
 				ry = py + dy[d];
 				
 				if (map[rx][ry] != '#' && lug[rx][ry] == -1){
 					lug[rx][ry] = d;
 					queue.addLast(new Point(rx, ry));
 				}
 			}
 		}
 		
 		StringBuffer sol = new StringBuffer();
 		rx = next.player >> 16;
 		ry = next.player & 0xFFFF;
 
 		px = current.player >> 16;
 		py = current.player & 0xFFFF;
 		
 		char[] how = {'r', 'd', 'l', 'u'};
 		
 		while (px != rx && py != ry){
 			sol.append(how[lug[rx][ry]]);
 			int tx = dx[lug[rx][ry]];
 			ry -= dy[lug[rx][ry]];
 			rx -= tx;
 		}
 		
 		return sol.toString();
 	}
 
 }
