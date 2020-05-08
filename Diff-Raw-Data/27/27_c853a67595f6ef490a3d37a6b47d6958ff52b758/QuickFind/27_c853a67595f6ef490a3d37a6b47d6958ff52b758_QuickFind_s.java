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
 		for (int k = 0; k < connections.length; k++) {
			if (connections[k] == connections[i]) {
				connections[k] = connections[j];
 			}
 		}
 	}
 	
 	@Override
 	public boolean isConnected(int i, int j) {
 		return connections[i] == connections[j];
 	}
 	
 	// --------------------- TEST
 
 	public static void main(String[] args) {
		Connectivity con = new QuickFind(5);
			con.union(0, 1);
			con.union(1, 3);
			con.union(2, 4);
		System.out.println(con.isConnected(0, 4));
		System.out.println(con.isConnected(0, 3));
		System.out.println(con.isConnected(0, 2));
			con.union(4, 1);
		System.out.println(con.isConnected(0, 4));
		System.out.println(con.isConnected(0, 2));
 	}
 }
