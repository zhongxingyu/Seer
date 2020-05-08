 /*
 * LocGoto.java
 * 
 * @author Karl Berger
 * 
 * Goto functions for localization.  Uses potential fields for path following and
 * obstacle avoidance.
 */
 
 import javaclient3.*;
 import javaclient3.structures.PlayerConstants;
 import java.awt.Point;
 import java.util.ArrayList;
 
 public class LocGoto {
 
 	public static void executePath( PlayerClient pc, ArrayList<Point> wps ) {
 
 		Position2DInterface pos = pc.requestInterfacePosition2D(0,PlayerConstants.PLAYER_OPEN_MODE);
 		RangerInterface ranger = pc.requestInterfaceRanger( 0, PlayerConstants.PLAYER_OPEN_MODE );
 
 		double turnrate = 0;
 		double speed = 0;
 		double omega = 20*Math.PI/180;
 		int ptCount = 0;
 		Point currTarget = wps.get( ptCount );
 		boolean reached = false;
 
 
 		while( true ) {
 			pc.readAll();
 
 			if( !ranger.isDataReady() ) {
                 		continue;
             		}
 			if( !pos.isDataReady() ) {
                 		continue;
             		}
 
 			double[] ranges = ranger.getData().getRanges();
 
 			if( ptCount == wps.size() ) { // If we have reached the last point, then stop
 				speed = 0;
 				turnrate = 0;
 				// TODO: Rather exit or just allow the user to kill the program when they want?
 			}else if( reached ) { 
 				// If we have reached our current target, increase the 
 				// point counter and see if we have any remaining points to hit
 				ptCount++;
 				// If there are remaining points, update current target
 				if( ptCount != wps.size() ) {
 					currTarget = wps.get( ptCount );
 					reached = false;
 				}
 				speed = 0;
 				turnrate = 0;
 			}else { 
 				/*
 				// We have not reached our target, so carry on!
 				double xToGo = currTarget.getX() - pos.getX();
 				double yToGo = currTarget.getY() - pos.getY();
 				double hypToGo = Math.sqrt( Math.pow(xToGo,2) + Math.pow(yToGo,2) );
 				double angle = Math.atan2( yToGo, xToGo );
 				System.out.println( "HypToGo: " + hypToGo );
 				System.out.println( "AngleDif: " + Math.abs( angle - pos.getYaw() ) );
 				*/
 
 				// Only working with every 5 lasers just to cut down on data being processed
 				// Just looking for the closest obstacle
 				double closest = Double.MAX_VALUE;
 				int closestLaser = 0;
 				for( int i = 0; i < ranges.length; i += 5 ) {
 					if( ranges[i] < closest ) {
 						closest = ranges[i];
 						closestLaser = i;
 					}
 				}
 
 				/*
 				// Angle of the laser beam relative to the world (in the same coordinates as the robot),
             			// not with respect to the robot.
 		            	double sampleTheta = closestLaser * Localization.RADIAN_PER_LASER - Localization.LASER_ROBOT_OFFSET + pos.getYaw();
             			// Components of the obstacle.
             			double xComponent = Math.cos(sampleTheta);
             			double yComponent = Math.sin(sampleTheta);
 				*/
 
 				//Now for potential field stuff.
 				//Alter the percieved distance of the obstacle to pretend we are in workspace
				double obsDistance = ranges[closestLaser] - 0.165;
 				double obsTheta = closestLaser * Localization.RADIAN_PER_LASER - 
 					Localization.LASER_ROBOT_OFFSET + pos.getYaw();
 				//X and Y position relative to the robot;
 				double obsX = Math.cos(obsTheta) * obsDistance;
 				double obsY = Math.sin(obsTheta) * obsDistance;
 				
 				double obsForceX = (-obsX) / Math.pow(obsDistance,3);
 				double obsForceY = (-obsY) / Math.pow(obsDistance,3);
 
 				double goalForceX = (currTarget.getX() - pos.getX()) / 
 					Math.pow(Math.sqrt(Math.pow(currTarget.getX() - 
 					pos.getX(),2) + Math.pow(currTarget.getY() - pos.getY(),2)),3);
 				double goalForceY = (currTarget.getY() - pos.getY()) /
 					Math.pow(Math.sqrt(Math.pow(currTarget.getX() - 
 					pos.getX(),2) + Math.pow(currTarget.getY() - pos.getY(),2)),3);
 
 				//Force exerted on the robot
 				double robotFX = Localization.OBSTACLE_POTENTIAL_CONSTANT * obsForceX + 
 					Localization.GOAL_POTENTIAL_CONSTANT * goalForceX;
 				double robotFY = Localization.OBSTACLE_POTENTIAL_CONSTANT * obsForceY + 
 					Localization.GOAL_POTENTIAL_CONSTANT * goalForceY;
 				double robotForce = Math.sqrt( robotFX*robotFX + robotFY*robotFY );
 				double robotForceAngle = Math.atan2(robotFY, robotFX);
 
 				//Now to translate to speed and turnrate for the robot.
 				speed = 0.15;
 				turnrate = (robotForceAngle - pos.getYaw())*robotForce;
 			}
 	    		//System.out.printf( "(%7f,%7f,%7f)\n",
 			//      pos.getX(),pos.getY(),pos.getYaw() );
 			pos.setSpeed(speed, turnrate);
 		}
 	}//executePath
 } //LocGoto.java
