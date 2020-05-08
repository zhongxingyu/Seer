 package org.csgames.ai.client;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 public class Util {
 
 	public final static String PLAYER_1 = "1";
 	public final static String PLAYER_2 = "2";
 	public final static String PLAYER_3 = "3";
 	public final static String PLAYER_4 = "4";
 	public final static String MYSELF = "Y";
 	public final static String BRICK_WALL = "W";
 	public final static String HARD_WALL = "H";
 	public final static String EMPTY = "";
 	public final static String BOMB = "B";
 	public final static String EXPLOSION = "E";
 	public final static String SUDDEN_DEATH_ALERT = "A";
 	public final static String POW_UP_BOMB = "b";
 	public final static String POW_UP_RANGE = "r";
 	public final static String POW_UP_DET = "d";
 		
 	public static class Values{
 		public static final int DEFAULT_BOMB_RANGE = 2;
 	}
 	
 	private String[][] mMap;
 	private int mBombRange = Values.DEFAULT_BOMB_RANGE;
 	
 	private List<Point2D> mPowerBomb = new ArrayList<Point2D>();
 	private List<Point2D> mPowerRange = new ArrayList<Point2D>();
 	private List<Point2D> mPowerDet = new ArrayList<Point2D>();
 	
 	public void updateMap(String[][] map){
 		
 		updatePowerBomb(map);
 		updatePowerRange(map);
 		updatePowerDet(map);
 		this.mMap = map;
 	}
 	
 	private void updatePowerDet(String[][] map) {
		Point2D me = getMyLocation();
 		for(int i = 0; i < map.length; i++){
 			for(int j = 0; j < map[0].length; j++){
				Point2D mapCell = new Point2D(i,j);
				if( at(mapCell) == POW_UP_DET ){
					mapCell.equals(me);
					//mSelf
				}
 			}
 		}
 	}
 
 	private void updatePowerRange(String[][] map) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	private void updatePowerBomb(String[][] map) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public List<Point2D> search(int x, int y, int max, String type){
 		ArrayList<Point2D> list = new ArrayList<Point2D>();
 		
 		int lowBoundX = Math.max(0, x - max);
 		int lowBoundY = Math.max(0, y - max);
 		int hiBoundX = Math.min(mMap.length, x + max);
 		int hiBoundY = Math.min(mMap[0].length, y + max);
 		
 		for(int col = lowBoundX; col < hiBoundX; col++){
 			for(int row = lowBoundY; row < hiBoundY; row++){
 				
 				if( mMap[col][row].equals(type) ){
 					list.add(new Point2D(col, row));
 				}
 			}
 		}
 		
 		return list;
 	}
 	
 	public double distance(Point2D first, Point2D second){
 		return distance(first.x, first.y, second.x, second.y);
 	}
 	
 	public double distance(int x1, int y1, int x2, int y2){
 		int dX = x2 - x1;
 		int dY = y2 - y1;
 		return Math.sqrt(dX*dX + dY*dY);
 	}
 	
 	public double checkSafety(Point2D p) { return checkSafety(p.x, p.y); }
 	
 	public double checkSafety(int x, int y){
 		if( at(x,y) == BOMB || at(x,y) == EXPLOSION ) return 0.0;
 		
 		Point2D checkedPoint = new Point2D(x,y);
 		
 		List<Point2D> bombList = search(x,y, mBombRange, BOMB);
 		double distanceToClosestBomb = Double.MAX_VALUE;
 		
 		for(Point2D aBomb : bombList){
 			double distanceToBomb = distance(aBomb, checkedPoint);
 			if( distanceToBomb < distanceToClosestBomb ){
 				distanceToClosestBomb = distanceToBomb;
 			}
 		}
 		
 		double rangedDistance = Math.max(distanceToClosestBomb, (double) Values.DEFAULT_BOMB_RANGE);
 		
 		return rangedDistance / (double) Values.DEFAULT_BOMB_RANGE;
 	}
 	
 	public Point2D getMyLocation(){
 		for(int i = 0; i < mMap.length; i++)
 			for(int j = 0; j < mMap[0].length; j++)
 				if(mMap[i][j].equals("Y")) return new Point2D(i, j);
 		
 		return new Point2D(-1, -1);
 	}
 	
 	public String at(Point2D p) { return at(p.x, p.y); }
 	
 	public String at(int x, int y){
		if(x < 0 ||
				x >= mMap.length ||
				y < 0 || 
				y > mMap[0].length) {
			return HARD_WALL;
		}
 		return mMap[x][y];
 	}
 	
 	public static class Point2D{
 		public final int x;
 		public final int y;
 		
 		public Point2D(int x, int y){ 
 			this.x = x; 
 			this.y = y; 
 		}
 		
 		public boolean equals(Object other){
 			if(other == null) return false;
 			if(!(other instanceof Point2D)) return false;
 			Point2D p = (Point2D) other;
 			return p.x == x && p.y == y;
 		}
 		
 		public String toString(){
 			return String.format("[%d, %d]", x, y);
 		}
 	}
 }
