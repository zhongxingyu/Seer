 /**
  *
  */
 package com.dask.pent;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import android.util.Log;
 
 /**
  * @author pent
  *
  */
 public class Navigation {
 	private Vincenty convert = new Vincenty();
 	
 	public ArrayList<double[]> geometry;
 	public ArrayList<CMRouteInstruction> instructions;
 	public double[] globalEnd;
 	
 	private int flagStart = 0;
 	public int LatLng = 1337;
 
 	public int indxPtStart;
 	public int indxPtCurr;
 	public int indxPtNext;
 	public int indxPtLast;
 
 	private double distCurrNode2NextNode;
 	private int idealAngle;
 	private int pathAngle;
 	private int turn;
 	private int position;
 	private int side;
 
 	private double threshold_pt = 15;
 	private double steps_dist = 0.2;
 
 	public Navigation(CMJson cmjson, double[] pseudoEnd) {
 		geometry = cmjson.route_geometry;
 		instructions = cmjson.route_instructions;
 		globalEnd = pseudoEnd;
 	}
 
 	public String[] Navigate(double[] userPos, int userBearing, int steps) {
 		String[] res = {
 				"Exiting Point...",
 				"Instruction",
 				"distance",
 				"Angle",
 				"Deviation"};
 
 		if(flagStart == 0)
 			return startNavigate(userPos);
 		else if(flagStart == 1) {
 			Log.d("Navigation.Navigation", "Starting 'Navigate()' w/ "
 					+ Arrays.toString(userPos)
 					+ " ub: " + userBearing
 					+ " st: " + steps);
 
 			double[] stateNext = choose2NextNode(userPos, steps),
 					stateStray = choose2Stray(userPos, userBearing, steps),
 					stateEnd = shortcutTaken(userPos);
 
 			Log.d("Navigation.Navigate", "stateNext: " + 
 					Arrays.toString(stateNext));
 			Log.d("Navigation.Navigate", "stateStray: " + 
 					Arrays.toString(stateStray));
 			Log.d("Navigation.Navigate", "stateEnd: " + 
 					Arrays.toString(stateEnd));
 			
 			/* TODO 0. The user is within range of the next node. */
 			if(stateNext[0] == 1) {
 				indxPtCurr = indxPtNext;
 				
 				/* TODO 0.0 The next node is the last node of the route AND
 				 * the user in within the range of the next node. */
 				if(indxPtNext == indxPtLast) {
 					res[0] = "0.0";
 					res[1] = "Your destination will be reached in "
 							+Integer.toString((int) stateNext[3]) + " meters.";
 					res[2] = "" + stateNext[3];
 					res[3] = "" + stateNext[5];
 					res[4] = "" + stateStray[5];
 					flagStart = -1;
 				}
 				/* TODO 0.1 The next node is NOT the last node of the route
 				 * AND the user is within range of the next node. */
 				else {
 					int indxInstruct = getInstructionIndex(indxPtNext);
 					if(indxInstruct != -1)
 						res[1] = instructions.get(indxInstruct).instruction;
 					else
 						res[1] = "Beware of street crossings!";
 					
 					indxPtNext++;
 					pathAngle = calcPathAngle();
 					indxInstruct = getInstructionIndex(indxPtNext);
 					
 					/* TODO 0.1.0 The user must turn at the next node AND the
 					 * user is within range of the current node AND the current
 					 * node is not the last node. */
 					if(indxInstruct != -1) {
 						idealAngle = calculateIdealAngle();
 						res[0] = "0.1.0";
 					}
 					/* TODO 0.1.1 The user does not have to turn at the next
 					 * node AND the user is in range of the current node AND
 					 * the current node is not the last node. */
 					else {
 						idealAngle = 9001;
 						res[0] = "0.1.1";
 					}
 					
 					res[2] = "" + convert.distVincenty(
 							userPos[0],
 							userPos[1],
 							geometry.get(indxPtNext)[0],
 							geometry.get(indxPtNext)[1]); //TODO change back to convert.dist
 					res[3] = "" + calculateUserAngle(userPos);
 					res[4] = "" + calcDeviAngle(userBearing);
 				}
 			}
 			/* TODO 1. The user is not within range of the next node. */
 			else {
 				/* TODO 1.0 The user is close to the global end point AND the
 				 * user is not within the range of the next node. */
 				if(stateEnd[0] == 1) {
 					res[0] = "1.0";
 					res[1] = "Your destination will be reached in "
 							+Integer.toString((int) stateEnd[1]) + " meters.";
 					res[2] = "" + stateEnd[1];
 					res[3] = "";
 					res[4] = "";
 					flagStart = -1;
 				}
 				/* TODO 1.1 The user is straying off path AND the user is not
 				 * within range of the next node. */
 				else if(stateStray[0] == 1) {
 					res[0] = "1.1";
 					res[1] = "You have strayed from the route. " +
 							"Please turn " +Integer.toString((int) stateStray[5]) + " Degrees.";
 					res[2] = "" + stateNext[3];
 					res[3] = "" + stateNext[5];
 					res[4] = "" + stateStray[5];
 				}
 				/* TODO 1.2 The user is not straying off path AND the user is
 				 * not within range of the next node. */
 				else {
 					res[0] = "1.2";
 					res[1] = "Continue on for " +Integer.toString((int) stateNext[3]) +
 							" meters.";
 					res[2] = "" + stateNext[3];
 					res[3] = "" + stateNext[5];
 					res[4] = "" + stateStray[5];
 				}	
 			}
 
 			Log.d("Navigation.Navigation", "Exiting 'Navigate()' @"
 					+ res[0] + " w/ usr*= " + res[3]
 					+ ", dev*= "+res[4]);
 		}
 		return res;
 	}
 
 	private String[] startNavigate(double[] currPosition) {
 		Log.d("Navigation.startNavigate", "Starting 'startNavigate()' w/ " + 
 				Arrays.toString(currPosition));
 
 		flagStart	= 1;
 
 		indxPtStart	= 0;
 		indxPtLast	= geometry.size()-1;
 		indxPtCurr	= indxPtStart;
 		indxPtNext	= indxPtCurr+1;
 
 		int indxInstruct = getInstructionIndex(indxPtNext);
 		if(indxInstruct == -1)
 			idealAngle = 9001;
 		else
 			idealAngle = calculateIdealAngle();
 
 		pathAngle = calcPathAngle();
 
 		distCurrNode2NextNode = convert.distVincenty(
 				geometry.get(indxPtCurr)[0],	// Latitude		= y1
 				geometry.get(indxPtCurr)[1],	// Longtitude	= x1
 				geometry.get(indxPtNext)[0],	// Latitude		= y2
 				geometry.get(indxPtNext)[1]);	// Longtitude	= x2
 
 		String[] res = {
 				"-1",
 				instructions.get(0).instruction,
 				""+distCurrNode2NextNode,
 				""+calculateUserAngle(currPosition),
 				""+pathAngle};
 
 		Log.d("Navigation.startNavigate", "Exiting 'startNavigate()' @" + 
 				res[0] + " w/ " + res[2]);
 		return res;
 	}
 
 	public String getState() {
 		String res = "";
 
 		res = "flagStart: "+flagStart+
 				", \tindxPtStart: "+indxPtStart+
 				", \tindxPtCurr: "+indxPtCurr+
 				",\nindxPtNext: "+indxPtNext+
 				", \tindxPtLast: "+indxPtLast+
 				", \tdistCurrNode2NextNode: "+distCurrNode2NextNode+
 				",\nidealAngle: "+idealAngle+
 				", \tpathAngle: "+pathAngle+
 				", \tturn: "+turn+
 				",\nposition: "+position+
 				", \tside: "+side;
 		
 		Log.d("Navigation.getState", res);
 		return res;
 	}
 
 	private int getInstructionIndex(int geoIndex) {
 		for(int i=0; i<instructions.size(); i++) {
 			if(instructions.get(i).position == geoIndex)
 				return i;
 		}
 
 		return -1;
 	}
 
 	public void debugger() {
 		System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
 //		String[] res = startNavigate(new double[]{0, 0});
 //		System.out.println(getState()+"\n"+res[1]+"\n==========================================================");
 //
 //		for(double i=0.1; i<2.0; i+=0.2) {
 //			System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
 //			res = Navigate(new double[]{0.1, i}, 5, 0);
 //			System.out.println(getState()+"\n"+res[1]+"\n==========================================================");
 //		}
 //
 //		for(double i=0.1; i<1.0; i+=0.2) {
 //			System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
 //			res = Navigate(new double[]{i, 1.9}, 85, 0);
 //			System.out.println(getState()+"\n"+res[1]+"\n==========================================================");
 //		}
 //
 //		for(double i=0.1; i<1.1; i+=0.2) {
 //			System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
 //			res = Navigate(new double[]{1.0 + i, 2.0 + i}, 85, 0);
 //			System.out.println(getState()+"\n"+res[1]+"\n==========================================================");
 //		}
 		
 		double[][] pts = {
 				{40.821285,-73.947937}, // front of ST
 				{40.821397,-73.948216}, // right side on ST
 				{40.821505,-73.948468}, // end of ST
 				{40.82144,-73.948516}, // begin of SH
 				{40.821159,-73.948706}, // GND entrance of SH
 				{40.820542,-73.949162}, // MID entrance of SH
 				{40.820185,-73.949417}, // front of ADMIN
 				{40.819883,-73.949621}, // front of MRK
 				{40.81998,-73.949948}, // front of NAC
 				{40.820065,-73.950281} // end
 		};
 		
 		
 		String[] res = startNavigate(pts[0]);
 		System.out.println(getState()+"\n"+res[1]+"\n==========================================================");
 
 		System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
 		res = Navigate(pts[1], 300, 0);
 		System.out.println(getState()+"\n"+res[1]+"\n==========================================================");
 			
 		for(int i=2; i<7; i++) {
 			System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
 			res = Navigate(pts[i], 220, 0);
 			System.out.println(getState()+"\n"+res[1]+"\n==========================================================");
 		}
 		
 		for(int i=7; i<pts.length; i++) {
 			System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
 			res = Navigate(pts[i], 285, 0);
 			System.out.println(getState()+"\n"+res[1]+"\n==========================================================");
 		}
 
 	}
 
 	public int angleL2L(double[][] line1, double[][] line2, int flag) {
 		// Swap the lat and lon for x and y.
 		if(flag == LatLng) {
 			double temp;
 			temp = line1[0][0];
 			line1[0][0] = line1[0][1];
 			line1[0][1] = temp;
 			
 			temp = line1[1][0];
 			line1[1][0] = line1 [1][1];
 			line1[1][1] = temp;
 			
 			temp = line2[0][0];
 			line2[0][0] = line2[0][1];
 			line2[0][1] = temp;
 			
 			temp = line2[1][0];
 			line2[1][0] = line2 [1][1];
 			line2[1][1] = temp;
 		}
 		double[] vec1 = {line1[1][0]-line1[0][0], line1[1][1]-line1[0][1]};
 		double[] vec2 = {line2[1][0]-line2[0][0], line2[1][1]-line2[0][1]};
 
 		double angle = ( Math.atan2(vec2[1], vec2[0])
 				- Math.atan2(vec1[1], vec1[0]) )*(180/Math.PI);
 
 		if(angle < 0)
 			angle = -180 - angle;
 		else
 			angle = 180 - angle;
 
 		return (int)Math.round(angle);
 	}
 
 	public int calcTrueNorth(double[][] line, int flag) {
 		// Swap the lat and lon for x and y.
 		if(flag == LatLng) {
 			double temp = line[0][0];
 			line[0][0] = line [0][1];
 			line[0][1] = temp;
 			
 			temp = line[1][0];
 			line[1][0] = line [1][1];
 			line[1][1] = temp;
 		}
         double[][] north = {{0,0}, {0,1}}; // Fixed North
         double[] vec1 = {line[1][0] - line[0][0], line[1][1] - line[0][1]};
         double[] vec2 = {north[1][0] - north[0][0], north[1][1] - north[0][1]};
 
         return (int) ( ( Math.atan2(vec2[1], vec2[0])
         		- Math.atan2(vec1[1], vec1[0]) )*(180/Math.PI) + 360)%360;
     }
 
 	public int calcDeviAngle(int thetaUser) {
 		int theta = thetaUser - pathAngle,
 				phi = 180 - Math.abs(theta);
 
 		if((theta > 0 && phi > 0) || (theta < 0 && phi < 0))
 			turn = 1;
 		else
 			turn = -1;
		
		//return Math.abs(phi);
		return theta;
 	}
 
 	public double[] choose2NextNode(double[] userPos, double steps) {
 		double[] state = {
 				0,	// 0 = Move to the next node.
 				0,	// 1 = How many have fired.
 				0,	// 2 = Distance fired?
 				0,	// 3 = Distance to next node.
 				0,	// 4 = Angle fired?
 				0,	// 5 = Angle of user to next node +1.
 				0,	// 6 = Steps fired?
 				0	// 7 = Total distance traveled.
 				};
 
 		//TODO change back to convert.dist
 		state[3] = convert.distVincenty(
 				userPos[0],
 				userPos[1],
 				geometry.get(indxPtNext)[0],
 				geometry.get(indxPtNext)[1]);
 		state[5] = calculateUserAngle(userPos);
 		state[7] = steps*steps_dist;
 
 		if(state[3] <= threshold_pt) {
 			state[1]++;
 			state[2] = 1;
 		}
 		if(idealAngle == 9001 || Math.abs(idealAngle-state[5]) <= 15){
 			state[1]++;
 			state[4] = 1;
 		}
 		if(state[7] >= distCurrNode2NextNode){
 			state[1]++;
 			state[6] = 1;
 		}
 
 		if(state[1] >= 2)
 			state[0] = 1;
 
 		return state;
 	}
 
 	public double[] choose2Stray(double[] userPos, int userBearing, double steps) {
 		double[] state = {
 				0,	// 0 = User is straying from path.
 				0,	// 1 = How many have fired.
 				0,	// 2 = Distance fired?
 				0,	// 3 = Distance to next node.
 				0,	// 4 = Deviation fired?
 				0,	// 5 = Deviation angle.
 				0,	// 6 = Steps fired?
 				0	// 7 = Total distance traveled.
 				};
 
 		//TODO change back to convert.dist
 		state[3] = convert.distVincenty(
 				userPos[0],
 				userPos[1],
 				geometry.get(indxPtNext)[0],
 				geometry.get(indxPtNext)[1]);
 		state[5] = calcDeviAngle(userBearing);
 		state[7] = steps*steps_dist;
 
 		if(state[3] >= threshold_pt){
 			state[1]++;
 			state[2] = 1;
 		}
 		if((state[5] >= 75 && state[5] <= 105)){
 			state[1]++;
 			state[4] = 1;
 		}
 		if(state[7] >= distCurrNode2NextNode){
 			state[1]++;
 			state[6] = 1;
 		}
 
 		if(state[1] >= 2)
 			state[0] = 1;
 
 		return state;
 	}
 
 	public double[] shortcutTaken(double[] userPos) {
 		double[] state = {
 			0,	// 0 = User is within range of the global end.
 			0,	// 1 = Distance to the global end.
 			};
 		
 		state[1] = convert.distVincenty(
 				userPos[0], userPos[1], 
 				globalEnd[0], globalEnd[1]);
 		
 		if(state[1] <= threshold_pt)
 			state[0] = 1;
 		else
 			state[0] = 0;
 		
 		return state;
 	}
 	
 	public int calcPathAngle() {
 		return calcTrueNorth(new double[][] {
 				{geometry.get(indxPtCurr)[0], geometry.get(indxPtCurr)[1]},
 				{geometry.get(indxPtNext)[0], geometry.get(indxPtNext)[1]}}, LatLng);
 	}
 
 	public int calculateIdealAngle() {
 		if(indxPtNext != indxPtLast) {
 			double[][] curr2next = {geometry.get(indxPtCurr), geometry.get(indxPtNext)}; //Line from Current Position to next Node
 			double[][] next2nextTwo = {geometry.get(indxPtNext),geometry.get(indxPtNext+1)}; //Line from Next Node to Node + 1
 
 			return angleL2L(curr2next,next2nextTwo, LatLng);
 		}
 		else
 			return 9001;
 	}
 
 	public int calculateUserAngle(double[] user_pos) {
 		if(indxPtNext != indxPtLast) {
 			double[][] curr2user = {geometry.get(indxPtCurr), user_pos}; //Line from user Position to next node + 1
 			double[][] user2nextTwo = {user_pos, geometry.get(indxPtNext+1)}; //Line from user position to Node + 1
 
 			return angleL2L(curr2user, user2nextTwo, LatLng);
 		}
 		else
 			return 0;
 	}
 
 	public boolean thresholdAngle(double[] curr_pos)
 	{
 		double[][] curr2next = {geometry.get(indxPtCurr), geometry.get(indxPtNext)}; //Line from Current Position to next Node
 		double[][] next2nextTwo = {geometry.get(indxPtNext),geometry.get(indxPtNext+1)}; //Line from Next Node to Node + 1
 		double[][] curr2nextTwo = {curr_pos,geometry.get(indxPtNext+1)}; //Line from Current Position to next node + 1
 
 		int angleCurr2NextTwo = angleL2L(curr2next,curr2nextTwo, LatLng);
 		int angleNext2NextTwo = angleL2L(curr2next,next2nextTwo, LatLng);
 
 		System.out.println("The Current Angle is: " + (angleCurr2NextTwo));
 		System.out.println("The angle between next node and next node + 1 is: " + angleNext2NextTwo);
 
 		if(((angleCurr2NextTwo) < angleNext2NextTwo -15) || ((angleCurr2NextTwo) > angleNext2NextTwo + 15)){
 			return false;
 		}
 		else{
 			return true;
 		}
 	}
 	
 	public void debugCalcTrueNorth() {
       // Quadrant 1
       double[][] line = {{0,0}, {0,1}};
       System.out.println("vec1: " + Arrays.deepToString(line) 
       		+ ",\t angle: " + calcTrueNorth(line, 0));
       
       line = new double[][] {{0,0}, {1,Math.sqrt(3)}};
       System.out.println("vec1: " + Arrays.deepToString(line) 
       		+ ",\t angle: " + calcTrueNorth(line, 0));
       
       line = new double[][] {{0,0}, {1,1}};
       System.out.println("vec1: " + Arrays.deepToString(line) 
       		+ ",\t angle: " + calcTrueNorth(line, 0));
       
       line = new double[][] {{0,0}, {Math.sqrt(3), 1}};
       System.out.println("vec1: " + Arrays.deepToString(line) 
       		+ ",\t angle: " + calcTrueNorth(line, 0));
       
       // Quadrant 4
       line = new double[][] {{0,0}, {1,0}};
       System.out.println("vec1: " + Arrays.deepToString(line) 
       		+ ",\t angle: " + calcTrueNorth(line, 0));
       
       line = new double[][] {{0,0}, {Math.sqrt(3),-1}};
       System.out.println("vec1: " + Arrays.deepToString(line) 
       		+ ",\t angle: " + calcTrueNorth(line, 0));
       
       line = new double[][] {{0,0}, {1,-1}};
       System.out.println("vec1: " + Arrays.deepToString(line) 
       		+ ",\t angle: " + calcTrueNorth(line, 0));
 
       line = new double[][] {{0,0}, {1,-Math.sqrt(3)}};
       System.out.println("vec1: " + Arrays.deepToString(line) 
       		+ ",\t angle: " + calcTrueNorth(line, 0));
       
       // Quadrant 3
       line = new double[][] {{0,0}, {0,-1}};
       System.out.println("vec1: " + Arrays.deepToString(line) 
       		+ ",\t angle: " + calcTrueNorth(line, 0));
       
       line = new double[][] {{0,0}, {-1, -Math.sqrt(3)}};
       System.out.println("vec1: " + Arrays.deepToString(line) 
       		+ ",\t angle: " + calcTrueNorth(line, 0));
       
       line = new double[][] {{0,0}, {-1,-1}};
       System.out.println("vec1: " + Arrays.deepToString(line) 
       		+ ",\t angle: " + calcTrueNorth(line, 0));
 
       line = new double[][] {{0,0}, {-Math.sqrt(3),-1}};
       System.out.println("vec1: " + Arrays.deepToString(line) 
       		+ ",\t angle: " + calcTrueNorth(line, 0));
       
       // Quadrant 2
       line = new double[][] {{0,0}, {-1,0}};
       System.out.println("vec1: " + Arrays.deepToString(line) 
       		+ ",\t angle: " + calcTrueNorth(line, 0));
       
       line = new double[][] {{0,0}, {-Math.sqrt(3),1}};
       System.out.println("vec1: " + Arrays.deepToString(line) 
       		+ ",\t angle: " + calcTrueNorth(line, 0));
       
       line = new double[][] {{0,0}, {-1,1}};
       System.out.println("vec1: " + Arrays.deepToString(line) 
       		+ ",\t angle: " + calcTrueNorth(line, 0));
 
       line = new double[][] {{0,0}, {-1,Math.sqrt(3)}};
       System.out.println("vec1: " + Arrays.deepToString(line) 
       		+ ",\t angle: " + calcTrueNorth(line, 0));
 	}
 }
