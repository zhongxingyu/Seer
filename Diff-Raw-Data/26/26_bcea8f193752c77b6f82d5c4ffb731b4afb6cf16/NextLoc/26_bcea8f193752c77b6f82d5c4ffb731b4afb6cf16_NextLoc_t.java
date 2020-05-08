 package uk.ac.kcl.inf._5ccs2seg.logic;
 import java.util.ArrayList;
 
 import uk.ac.kcl.inf._5ccs2seg.data.GridMap;
 import uk.ac.kcl.inf._5ccs2seg.data.Node;
 
 
 public class NextLoc {
 	private GridMap grid;
 	private int[][] map;
 	private Node start;
 	private Node goal;
 	private Node current;
 	private boolean stop = false;
 	private ArrayList<Node> list = new ArrayList<Node>();
 
 	public NextLoc(Node start, GridMap grid) {
			int x = grid.getMaxX();
			int y = grid.getMaxY();
 			this.grid = grid;
			map = new int[y][x];
			int[][] ma = grid.getMap();
			for (int i = 0; i<y; i++){
				for (int j = 0; j<x; j++){
					map[i][j] = ma[i][j];
				}
			}
 			this.start = start;
 			list.add(this.start);
 		}
 	
 	
 	public Node calc(){
 		
 		main:
 		while(!stop){
 			if (list.isEmpty()){
 				goal = new Node(-1,-1); break;
 			}
 		current = list.get(0);	
 		int ii = current.getArr(0);
 		int jj = current.getArr(1);
		map[ii][jj] = 8;
 		for (int i = ii - 1; i <= ii + 1; i++) {
 			for (int j = jj - 1; j <= jj + 1; j++) {
 				if(ii == i || jj == j ){
 				if (map[i][j] == 0) { 
 					checkCell(i, j);
 					if (stop) {break main;}
 				}
				else if (map[i][j] == 9 || map[i][j] == 2 || map[i][j] == 8){}
				else if (map[i][j] == 1 && list.indexOf(new Node(i,j)) < 0){
 					list.add(new Node(i, j));				
 				}
 			}	
 					
 			}
 			}
 		list.remove(0);
 		}
 		return goal;
 		
 	}
 	
 	public void checkCell(int i, int j){
 		
 		double[] arr = grid.arrayIndexToCoordCalc(i, j);
 		double[] arr2 = grid.arrayIndexToCoordCalc(start.getArr(0), start.getArr(1));
 		double d;
 		//check adj
		if (map[i-1][j] == 1 || map[i+1][j] ==  1 || map[i][j+1] ==  1 || map[i][j-1] == 1 
				|| map[i-1][j] == 8 || map[i+1][j] ==  8 || map[i][j+1] ==  8|| map[i][j-1] == 8){
 			d = Math.sqrt(Math.pow((arr[0]-arr2[0]),2)+
 					Math.pow((arr[1]-arr2[1]),2));
 			if (d > 1.7){
				grid.setSts(j, i, 1);
 				goal = new Node(i,j);
 				stop = true;
 				return;
 			}
 		}
 		
 		map[i][j] = 9;
 		
 	}
 	
 }
