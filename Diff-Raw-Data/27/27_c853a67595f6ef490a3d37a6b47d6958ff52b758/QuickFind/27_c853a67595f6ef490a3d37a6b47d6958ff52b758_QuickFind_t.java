 package com.mishadoff.algo.connectivity;
 
 /**
  * Simplest algorithm for connectivity problem.
  * 
  * Compexity:
  * union O(n)
  * isConnected O(1)
  * 
  * @author mishadoff
  *
  */
 public class QuickFind implements Connectivity {
 
 	private int[] connections;
 	
 	public QuickFind(int N) {
 		connections = new int[N];
 		for (int i = 0; i < N; i++) {
 			connections[i] = i;
 		}
 	}
 	
 	@Override
 	public void union(int i, int j) {
		int aid = connections[i];
		int bid = connections[j];
 		for (int k = 0; k < connections.length; k++) {
			if (connections[k] == aid) {
				connections[k] = bid;
 			}
 		}
 	}
 	
 	@Override
 	public boolean isConnected(int i, int j) {
 		return connections[i] == connections[j];
 	}
 	
 	// --------------------- TEST
 
 	public static void main(String[] args) {
		Connectivity con = new QuickFind(10);
		//6-3 3-7 4-0 3-8 9-6 5-0
			con.union(6, 3);
			con.union(3, 7);
			con.union(4, 0);
			con.union(3, 8);
			con.union(9, 6);
			con.union(5, 0);
		System.out.println(con.isConnected(1, 1));
 	}
 }
