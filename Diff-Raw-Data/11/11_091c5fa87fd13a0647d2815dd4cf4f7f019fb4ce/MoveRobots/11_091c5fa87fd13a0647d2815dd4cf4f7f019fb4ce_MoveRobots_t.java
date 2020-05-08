 package com.tacohen.killbots.Logic;
 
 import android.util.Log;
 import android.util.Pair;
 
 public class MoveRobots {
 	
 	String TAG = "MoveRobots";
 	
 	public MoveRobots(){
 		super();
 	}
 	
 	public Pair<Integer,Integer> desiredRobotLocation(int robotXLocation, int robotYLocation, int playerXLocation, int playerYLocation){
 		
 		int xValueDifference = (robotXLocation- playerXLocation);
 		Log.i(TAG, "X Value Differnce is: "+xValueDifference);
 		int yValueDifference = (robotYLocation- playerYLocation);
 		Log.i(TAG, "X Value Differnce is: "+xValueDifference);
 		
 		int desiredXValue = 0;
 		int desiredYValue = 0;
 		
 		/**
 		//Have the robot go the direction they need to go the most
 		//If there is a tie, favor X direction arbitrarily
 		//TODO: find a better method than arbitrarily favoring X
 		if (Math.abs(yValueDifference) > Math.abs(xValueDifference)){
 			desiredXValue = robotXLocation;
 			Log.i(TAG, "Moving in Y Direction, desired XValue is: "+desiredXValue);
 			if (yValueDifference > 0){
 				desiredYValue = robotYLocation - 1;
 			}
 			else{
 				desiredYValue = robotYLocation + 1;
 			}
 			Log.i(TAG, "Moving in Y Direction, desired YValue is: "+desiredYValue);
 
 		}
 		//if the robot is going to move in the X direction
 		else{
 			
 			desiredYValue = robotYLocation;
 			Log.i(TAG, "Moving in X Direction, desired YValue is: "+desiredYValue);
 			if (xValueDifference > 0){
 				desiredXValue = robotXLocation - 1;
 			}
 			else{
 				desiredXValue = robotXLocation + 1;
 			}
 			Log.i(TAG, "Moving in X Direction, desired XValue is: "+desiredXValue);
 		}
 		*/
 		if (robotXLocation == playerXLocation){
 			//move in Y direction only
 			desiredXValue = robotXLocation;
 			Log.i(TAG, "Moving in Y Direction, desired XValue is: "+desiredXValue);
 			if (yValueDifference > 0){
				desiredYValue = robotYLocation + 1;
 			}
 			else{
				desiredYValue = robotYLocation - 1;
 			}
 			Log.i(TAG, "Moving in Y Direction, desired YValue is: "+desiredYValue);
 		}
 		else{
 			if (robotYLocation == playerYLocation){
 				desiredYValue = robotYLocation;
 				Log.i(TAG, "Moving in X Direction, desired YValue is: "+desiredYValue);
 				if (xValueDifference > 0){
 					desiredXValue = robotXLocation - 1;
 				}
 				else{
 					desiredXValue = robotXLocation + 1;
 				}
 				Log.i(TAG, "Moving in X Direction, desired XValue is: "+desiredXValue);
 			}
 			else{
 				//move diagonally
 				if (xValueDifference > 0){
 					desiredXValue = robotXLocation - 1;
 				}
 				else{
 					desiredXValue = robotXLocation + 1;
 				}
 				Log.i(TAG, "Moving diagonally, desired XValue is: "+desiredXValue);
 				if (yValueDifference > 0){
					desiredYValue = robotYLocation + 1;
 				}
 				else{
					desiredYValue = robotYLocation - 1;
 				}
 				Log.i(TAG, "Moving diagonally, desired YValue is: "+desiredYValue);
 			}
 		}
 
 		return Pair.create(desiredXValue, desiredYValue);
 		
 	}
 
 }
