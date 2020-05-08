 package com.github.desmaster.Devio.util.gamemath;
 
 public class Distance {
 
 	public static int calulateTotalDistance(int x1,int y1,int x2,int y2){
 		return (int) Math.round(Math.sqrt((x2-x1)*(x2-x1) + (y2-y1)*(y2-y1)));
 	}
 	
 	public static int calulateFaceDistance(int x1,int y1,int x2,int y2,int face){
 		switch(face){
 		case 0:
 			return calulateUpDistance(x1,y1,x2,y2);
 		case 1:
 			return calulateRightDistance(x1,y1,x2,y2);
 		case 2:
 			return calulateDownDistance(x1,y1,x2,y2);
 		case 3:
 			return calulateLeftDistance(x1,y1,x2,y2);
 		}
 		return 0;
 	}
 	
 	public static int calulateUpDistance(int x1,int y1,int x2,int y2){
 		return y1 - y2;
 	}
 	public static int calulateRightDistance(int x1,int y1,int x2,int y2){
 		return x2 - x1;
 	}
 	public static int calulateDownDistance(int x1,int y1,int x2,int y2){
 		return y2 - y1;
 	}
 	public static int calulateLeftDistance(int x1,int y1,int x2,int y2){
 		return x1 - x2;
 	}
 
 	public static int[] getDirection(int x1,int y1,int x2,int y2){
 		int[] returnvalue = new int[2];
 		
 		int UP = calulateUpDistance(x1,y1,x2,y2);
 		int RIGHT = calulateRightDistance(x1,y1,x2,y2);
 		int DOWN = calulateDownDistance(x1,y1,x2,y2);
 		int LEFT = calulateLeftDistance(x1,y1,x2,y2);
 		
 		int X = Math.max(RIGHT, LEFT);
 		int Y = Math.max(UP,DOWN);
 		
 		int DIR1 = Math.max(X,Y);
 		int DIR2 = Math.min(X,Y);
 		
 		if (DIR1 == UP) returnvalue[0] = 0;
 		if (DIR1 == RIGHT) returnvalue[0] = 1;
 		if (DIR1 == DOWN) returnvalue[0] = 2;
 		if (DIR1 == LEFT) returnvalue[0] = 3;
 		
 		if (DIR2 == UP) returnvalue[1] = 0;
 		if (DIR2 == RIGHT) returnvalue[1] = 1;
 		if (DIR2 == DOWN) returnvalue[1] = 2;
 		if (DIR2 == LEFT) returnvalue[1] = 3;
 		
		return null;
 	}
 }
