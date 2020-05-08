 package project;
 
 public class Graph {
 	private boolean array[][];
 	private int size;
 	protected Graph(int s) {
 		size = s*s; // s is the 'side length' of the graph, which represents a 2D square quoridor board for our purposes
 		array = new boolean[size][size];
 		int n=0;
 		while (n < size) { //place edges between adjacent squares
 			if (n % s < s - 1) {
 				array[n][n+1] = true;
 				array[n+1][n] = true;
 			}
 			if (n / s < s - 1) {
 				array[n][n+s] = true;
 				array[n+s][n] = true;
 			}
			n++;
 		}
 	}
 	
 	public void removeEdge (int n1, int n2) { //remove edge between nodes n1 and n2
 		array[n1][n2] = false;
 		array[n2][n1] = false;
 	}
 	
 	public void insertEdge (int n1, int n2) {
 		array[n1][n2] = true;
 		array[n2][n1] = true;
 	}
 	
 	public boolean isEdge (int n1, int n2) {
 		return array[n1][n2];
 	}
 }
