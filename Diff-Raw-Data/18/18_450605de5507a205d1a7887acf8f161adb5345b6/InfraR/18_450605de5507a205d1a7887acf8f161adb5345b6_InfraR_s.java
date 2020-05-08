 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /**
  *
  * @author geza
  */
 
 import orc.*;
 
 public class InfraR extends java.lang.Thread {
 	public boolean running = true;
 	public float[] leftMotorAction = null;
 	public float[] leftMotorWeight = null;
 	public float[] rightMotorAction = null;
 	public float[] rightMotorWeight = null;
 	public int idx = 0;
 	public int state = 0;
 	public String[] names = {"forward", "left", "right", "back"};
 	public float[] weights = {0.5f, 0.97f, 0.97f, 0.97f};
 	public Orc o = null;
 
 	public void setState(int newstate) {
 		System.err.println("transition to "+names[newstate]);
 		state = newstate;
 		leftMotorWeight[idx] = weights[newstate];
 		rightMotorWeight[idx] = weights[newstate];
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
 		final double kp = 0.002;
 		final double kd = 0.001;
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
 			shiftleft(leftIRreadings, 62.5/leftIR.getVoltage());
 			shiftleft(rightIRreadings, 62.5/rightIR.getVoltage());
 			shiftleft(crossLeftIRreadings, 62.5/crossLeftIR.getVoltage());
 			shiftleft(crossRightIRreadings, 62.5/crossRightIR.getVoltage());
 			double left = averageArray(leftIRreadings);//a.getVoltage()
 			//double left = 999999.0;
 			double right = averageArray(rightIRreadings);
 			//double right = averageArray(rightIRreadings);
 			
 			double crossLeft = averageArray(crossLeftIRreadings);
 			double crossRight = averageArray(crossRightIRreadings);
 			
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
 				if (/*crossLeft < 30 ||*/ crossRight < 120) { // rotate left
 					setState(1);
 				} else if (crossRight < 120 /*|| left < 40*/) { // rotate right
 					setState(2);
 				} else {
 			if (left > right) {
 				//leftMotorWeight[idx] = 0.8f;
 				//rightMotorWeight[idx] = 0.8f;
 				double error = left-desv;
 				if (error > 100.0) error = 100.0;
 				if (error < -100.0) error = -100.0;
				double basevel = 0.8;
 				//double basevel = bound(1.0-error, 0.7, 0.6);
 				lspeed = -(kp*error-kd*(left-prevleft));//+basevel;
 				rspeed = (kp*error-kd*(left-prevleft));//+basevel;
 				if (lspeed > rspeed) {
 					rspeed += basevel-Math.abs(lspeed);
 					lspeed = basevel;
 				} else {
 					lspeed += basevel-Math.abs(rspeed);
 					rspeed = basevel;
 				}
 				sideVote = true;
 			} else {
 				//leftMotorWeight[idx] = 0.8f;
 				//rightMotorWeight[idx] = 0.8f;
 				double error = right-desv;
 				if (error > 100.0) error = 100.0;
 				if (error < -100.0) error = -100.0;
 				//double basevel = bound(1.0-error, 0.7, 0.6);
				double basevel = 0.8;
 				lspeed = (kp*error-kd*(right-prevright));//+basevel;
 				rspeed = -(kp*error-kd*(right-prevright));//+basevel;
 				if (lspeed > rspeed) {
 					rspeed += basevel-Math.abs(lspeed);
 					lspeed = basevel;
 				} else {
 					lspeed += basevel-Math.abs(rspeed);
 					rspeed = basevel;
 				}
 			}
 			}
 			} if (state ==  1) { // rotate left
 				rspeed = 0.7;
 				lspeed = -0.7;
 				if (/*crossLeft > 100 &&*/ crossRight > 120 && left > 50 && right > 50) {
 					setState(0);
 				}
 			} if (state == 2) { // rotate right
 				rspeed = -0.7;
 				lspeed = 0.7;
 				if (/*crossLeft > 40 &&*/ crossRight > 120 && left > 50 && right > 50) {
 					setState(0);
 				}
 			}
 			prevleft = left;
 			prevright = right;
 			leftMotorAction[idx] = (float)lspeed;
 			rightMotorAction[idx] = (float)rspeed;
 			java.lang.Thread.sleep(20);
 		}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void setup(Arbiter a, int ActionWeightIndex) {
 		idx = ActionWeightIndex;
 		leftMotorAction = a.leftMotorAction;
 		leftMotorWeight = a.leftMotorWeight;
 		rightMotorAction = a.rightMotorAction;
 		rightMotorWeight = a.rightMotorWeight;
 		o = a.o;
 	}
 
 	public void bye() {
 		running = false;
 	}
 }
