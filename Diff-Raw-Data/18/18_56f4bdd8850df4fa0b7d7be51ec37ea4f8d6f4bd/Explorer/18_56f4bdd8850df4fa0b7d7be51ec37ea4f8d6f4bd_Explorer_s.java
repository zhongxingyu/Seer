 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /**
  *
  * @author geza
  */
 
 import orc.*;
 
 public class Explorer extends java.lang.Thread {
 	public boolean running = true;
 	public float[] leftMotorAction = null;
 	public float[] leftMotorWeight = null;
 	public float[] rightMotorAction = null;
 	public float[] rightMotorWeight = null;
 	public int idx = 0;
 	public int state = 0;
 	public String[] names = {"forward", "left", "right", "rotateleft", "rotateright", "backupleft", "backupright"};
	public int[] timeouts = {150, 80, 80, 60, 60, 20, 20};
 	public float[] weights = {0.5f, 0.97f, 0.97f, 0.97f, 0.97f, 0.97f, 0.97f};
 	public int[] transitions = {0, 4, 3, 0, 0, 1, 2};
 	public int statetimeout = 0;
 	public Orc o = null;
 	public Gyroscope g = null;
 	public int angendi = 0;
 	public double maxheu = 0;
 	public int targang = 0;
 	public int prevtargdiff = Integer.MAX_VALUE;
 
 	public void setState(int newstate) {
 		System.err.println("transition to "+names[newstate]);
 		state = newstate;
 		statetimeout = 0;
 		leftMotorWeight[idx] = weights[newstate];
 		rightMotorWeight[idx] = weights[newstate];
 		prevtargdiff = Integer.MAX_VALUE;
 		//angendi = g.anglei;
 		//if (newstate != 3 && newstate != 4) {
 		//maxheu = 0;
 		//targang = angrefi;
 		//}
 		if (newstate == 1) {
 		maxheu = 0;
 		targang = g.anglei;
 		// angle increases ccw
 		//   0
 		//90   270
 		//  180
		angendi = (targang + 180) % 360;
 		} else if (newstate == 2) {
 		maxheu = 0;
 		targang = g.anglei;
		angendi = (targang + 180) % 360;
 		}
 	}
 
 	public static double bound(double v, double max, double min) {
 		if (v > max) return max;
 		else if (v < min) return min;
 		else return v;
 	}
 
 	public static void shiftleft(double[] a, double v) {
 		int i = 0;
 		for (; i < a.length-1; ++i) {
 			a[i] = a[i+1];
 		}
 		a[i] = v;
 	}
 
 	public static double averageArray(double[] a) {
 		double total = 0.0;
 		for (int i = 0; i < a.length; ++i) {
 			total += a[i];
 		}
 		return total / (double)a.length;
 	}
 
 	public static double median3(double a, double b, double c) {
 		if (a < b) {
 			if (c < b) { // a,c < b
 				if (a < c) { // a < c < b
 					return c;
 				} else { // c < a < b
 					return a;
 				}
 			} else { // a < b < c
 				return b;
 			}
 		} else { // b < a
 			if ( c < a) { // b,c < a
 				if (b < c) { // b < c < a
 					return c;
 				} else { // c < b < a
 					return b;
 				}
 			} else { // b < a < c
 				return a;
 			}
 		}
 	}
 
 	public int circdiff(int ang1, int ang2) {
 		if (ang1 > ang2) {
 			return Math.min(Math.abs(ang2+360-ang1), Math.abs(ang1-ang2));
 		} else {
 			return Math.min(Math.abs(ang1+360-ang2), Math.abs(ang1-ang2));
 		}
 	}
 
 	public void run() {
 		try {
 		//byte[] inet = {(byte)192, (byte)168, (byte)237, (byte)7};
 		//Orc o = new orc.Orc(java.net.Inet4Address.getByAddress(inet));
 		AnalogInput leftIR = new AnalogInput(o, 7);
 		AnalogInput rightIR = new AnalogInput(o, 0);
 		AnalogInput crossLeftIR = new AnalogInput(o, 5);
 		AnalogInput crossRightIR = new AnalogInput(o, 2);
 		final double desv = 120.0;
 		final double desvCross = 30.0;
 		final double kp = 0.001;
 		final double kd = 0.000;
 		double prevleft = leftIR.getVoltage();
 		double prevright = rightIR.getVoltage();
 		double prevCrossLeft = crossLeftIR.getVoltage();
 		double prevCrossRight = crossRightIR.getVoltage();
 		double[] leftIRreadings = new double[3];
 		double[] rightIRreadings = new double[3];
 		double[] crossLeftIRreadings = new double[3];
 		double[] crossRightIRreadings = new double[3];
 		java.util.Arrays.fill(leftIRreadings, prevleft);
 		java.util.Arrays.fill(rightIRreadings, prevright);
 		java.util.Arrays.fill(crossRightIRreadings, prevCrossLeft);
 		java.util.Arrays.fill(crossLeftIRreadings, prevCrossRight);
 		int rightcooldown = 0;
 		int leftcooldown = 0;
 		setState(0);
 		while (running) {
 			if (statetimeout >= timeouts[state]) { // state timed out, make transition
 				setState(transitions[state]);
 			} else {
 				++statetimeout;
 			}
 			shiftleft(leftIRreadings, 62.5/leftIR.getVoltage());
 			shiftleft(rightIRreadings, 62.5/rightIR.getVoltage());
 			shiftleft(crossLeftIRreadings, 62.5/crossLeftIR.getVoltage());
 			shiftleft(crossRightIRreadings, 62.5/crossRightIR.getVoltage());
 			double left = median3(leftIRreadings[0], leftIRreadings[1], leftIRreadings[2]);//a.getVoltage()
 			//double left = 999999.0;
 			double right = median3(rightIRreadings[0], rightIRreadings[1], rightIRreadings[2]);
 			//double right = averageArray(rightIRreadings);
 
 			double crossLeft = median3(crossLeftIRreadings[0], crossLeftIRreadings[1], crossLeftIRreadings[2]);
 			double crossRight = median3(crossRightIRreadings[0], crossRightIRreadings[1], crossRightIRreadings[2]);
 
 			System.out.println("left is "+left+"right is "+right);
 			//System.out.println(right);
 			double lspeed = 0.0;
 			double rspeed = 0.0;
 			double lspeedCross = 0.0;
 			double rspeedCross = 0.0;
 			boolean sideVote = false;
 			boolean crossVote = false;
 			boolean escape = false;
 			if (leftcooldown > 0) --leftcooldown;
 			if (rightcooldown > 0) --rightcooldown;
 			if (state == 0) { // forwards
 				if (/*crossLeft < 30 ||*/ right < 100) { // rotate left
 					lspeed = 0.0;
 					rspeed = 0.0;
 					setState(5);
 				} else if (crossRight < 130 || left < 100) { // rotate right
 					lspeed = 0.0;
 					rspeed = 0.0;
 					setState(6);
 				} else {
 			if (left > right) {
 				//leftMotorWeight[idx] = 0.8f;
 				//rightMotorWeight[idx] = 0.8f;
 				double error = left-desv;
 				if (error > 100.0) error = 100.0;
 				if (error < -100.0) error = -100.0;
 				double basevel = 0.6;
 				//double basevel = bound(1.0-error, 0.7, 0.6);
 				lspeed = -(kp*error-kd*(left-prevleft))+basevel;
 				rspeed = (kp*error-kd*(left-prevleft))+basevel;
 				/*
 				if (lspeed > rspeed) {
 					rspeed += basevel-Math.abs(lspeed);
 					lspeed = basevel;
 				} else {
 					lspeed += basevel-Math.abs(rspeed);
 					rspeed = basevel;
 				}*/
 				sideVote = true;
 			} else {
 				//leftMotorWeight[idx] = 0.8f;
 				//rightMotorWeight[idx] = 0.8f;
 				double error = right-desv;
 				if (error > 100.0) error = 100.0;
 				if (error < -100.0) error = -100.0;
 				//double basevel = bound(1.0-error, 0.7, 0.6);
 				double basevel = 0.6;
 				lspeed = (kp*error-kd*(right-prevright))+basevel;
 				rspeed = -(kp*error-kd*(right-prevright))+basevel;
 				/*
 				if (lspeed > rspeed) {
 					rspeed += basevel-Math.abs(lspeed);
 					lspeed = basevel;
 				} else {
 					lspeed += basevel-Math.abs(rspeed);
 					rspeed = basevel;
 				}*/
 			}
 			//rspeed = 0;
 			//lspeed = 0;
 			}
 			} if (state ==  1) { // scan left
 				int curang = g.anglei;
 				double heuv = Math.log(left)/2.0+Math.log(right)/2.0+Math.log(crossLeft)+Math.log(crossRight);
 				//double heuv = Math.min(left, 800.0)+Math.min(right, 800.0)+Math.min(crossLeft, 800.0)+Math.min(crossRight, 800.0);
 				if (heuv > maxheu) {
 					maxheu = heuv;
 					targang = curang;
 				}
 				int targdiff = circdiff(curang,angendi);
 				if (targdiff < 70 && (targdiff == 0 || targdiff > prevtargdiff)) { // done rotating
 					rspeed = 0.0;
 					lspeed = 0.0;
 					setState(4);
 				} else {
 					rspeed = 0.7;
 					lspeed = -0.7;
 					prevtargdiff = targdiff;
 				}
 			} if (state ==  2) { // scan right
 				int curang = g.anglei;
 				double heuv = Math.log(left)/2.0+Math.log(right)/2.0+Math.log(crossLeft)+Math.log(crossRight);
 				//double heuv = Math.min(left, 800.0)+Math.min(right, 800.0)+Math.min(crossLeft, 800.0)+Math.min(crossRight, 800.0);
 				if (heuv > maxheu) {
 					maxheu = heuv;
 					targang = curang;
 				}
 				int targdiff = circdiff(curang,angendi);
 				if (targdiff < 70 && (targdiff == 0 || targdiff > prevtargdiff)) { // done rotating
 					rspeed = 0.0;
 					lspeed = 0.0;
 					setState(3);
 				} else {
 					rspeed = -0.7;
 					lspeed = 0.7;
 					prevtargdiff = targdiff;
 				}
 			} if (state == 3) { // rotate to target angle by the left
 				int targdiff = circdiff(g.anglei,targang);
 				if (targdiff < 70 && (targdiff == 0 || targdiff > prevtargdiff)) { // done rotating
 					rspeed = 0.0;
 					lspeed = 0.0;
 					setState(0);
 				} else {
 					rspeed = 0.7;
 					lspeed = -0.7;
 					prevtargdiff = targdiff;
 				}
 			} if (state == 4) { // rotate to target angle by the right
 				int targdiff = circdiff(g.anglei,targang);
 				if (targdiff < 70 && (targdiff == 0 || targdiff > prevtargdiff)) { // done rotating
 					rspeed = 0.0;
 					lspeed = 0.0;
 					setState(0);
 				} else {
 					rspeed = -0.7;
 					lspeed = 0.7;
 					prevtargdiff = targdiff;
 				}
 			} if (state == 5) { // backup left
 				rspeed = -0.8;
 				lspeed = -0.6;
 			} if (state == 6) { // backup right
 				rspeed = -0.6;
 				lspeed = -0.8;
 			}
 			prevleft = left;
 			prevright = right;
 			prevCrossLeft = crossLeft;
 			prevCrossRight = crossRight;
 			leftMotorAction[idx] = (float)lspeed;
 			rightMotorAction[idx] = (float)rspeed;
 			java.lang.Thread.sleep(20);
 		}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void setup(Arbiter a, Gyroscope ng, int ActionWeightIndex) {
 		idx = ActionWeightIndex;
 		leftMotorAction = a.leftMotorAction;
 		leftMotorWeight = a.leftMotorWeight;
 		rightMotorAction = a.rightMotorAction;
 		rightMotorWeight = a.rightMotorWeight;
 		o = a.o;
 		g = ng;
 	}
 
 	public void bye() {
 		running = false;
 	}
 }
