 package org.csgames.ai.client;
 
 public class Util {
 	
 	private String[][] map;
 	
	public void updateMap(String[][] map){
 		this.map = map;
 	}
 	
	public int[][] search(int x, int y, int max){
 		
 		return new int[0][0];
 	}
 	
	/**
	 * 
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return
	 */
	public double distance(int x1, int y1, int x2, int y2){
		int dX = x2 - x1;
		int dY = y2 - y1;
		return Math.sqrt(dX*dX + dY*dY);
 	}
 	
 	
	public double checkSafety(int x, int y){
 		return 0.0;
 	}
 }
