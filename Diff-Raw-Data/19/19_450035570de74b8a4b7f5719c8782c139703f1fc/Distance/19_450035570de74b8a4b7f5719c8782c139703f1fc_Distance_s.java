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
 
 	public int[] getDirection(int x1,int y1,int x2,int y2){
 		int UP = calulateUpDistance(x1,y1,x2,y2);
 		int RIGHT = calulateRightDistance(x1,y1,x2,y2);
 		int DOWN = calulateDownDistance(x1,y1,x2,y2);
 		int LEFT = calulateLeftDistance(x1,y1,x2,y2);
 		
 		int X = Math.max(RIGHT, LEFT);
 		int Y = Math.max(UP,DOWN);
 		
		if (){
			
		}
 		return null;
 	}
 }
