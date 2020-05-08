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
 		byte[] inet = {(byte)192, (byte)168, (byte)237, (byte)7};
 		Orc o = new orc.Orc(java.net.Inet4Address.getByAddress(inet));
 		leftMotorWeight[idx] = 0.5f;
 		rightMotorWeight[idx] = 0.5f;
 		AnalogInput leftIR = new AnalogInput(o, 7);
 		AnalogInput rightIR = new AnalogInput(o, 1);
 		final double desv = 100.0;
		final double kp = 0.002;
		final double kd = 0.001;
 		double prevleft = leftIR.getVoltage();
 		double prevright = rightIR.getVoltage();
 		double[] leftIRreadings = new double[3];
 		double[] rightIRreadings = new double[3];
 		java.util.Arrays.fill(leftIRreadings, prevleft);
 		java.util.Arrays.fill(rightIRreadings, prevright);
 		while (running) {
 			shiftleft(leftIRreadings, 62.5/leftIR.getVoltage());
 			shiftleft(rightIRreadings, 62.5/rightIR.getVoltage());
 			double left = averageArray(leftIRreadings);//a.getVoltage()
 			//double left = 999999.0;
 			double right = averageArray(rightIRreadings);
 			//double right = averageArray(rightIRreadings);
 			System.out.println("left is "+left+"right is "+right);
 			//System.out.println(right);
 			double lspeed;
 			double rspeed;
			if (left > 200.0 && right > 200.0) { // just go straight
				lspeed = 0.6;
				rspeed = 0.6;
			}
			else if (left > right) {
 				double error = left-desv;
				if (error > 100.0) error = 100.0;
				if (error < -100.0) error = -100.0;
				double basevel = bound(1.0-error, 0.7, 0.4);
 				lspeed = -(kp*error-kd*(left-prevleft))+basevel;
 				rspeed = (kp*error-kd*(left-prevleft))+basevel;
 			} else {
 				double error = right-desv;
				if (error > 100.0) error = 100.0;
				if (error < -100.0) error = -100.0;
				double basevel = bound(1.0-error, 0.7, 0.4);
 				lspeed = (kp*error-kd*(right-prevright))+basevel;
 				rspeed = -(kp*error-kd*(right-prevright))+basevel;
 			}
 			prevleft = left;
 			prevright = right;
 			/*
 			if (lspeed > rspeed) {
 				rspeed += basevel-Math.abs(lspeed);
 				lspeed = basevel;
 			} else {
 				lspeed += basevel-Math.abs(rspeed);
 				rspeed = basevel;
 			}
 			*/
 			leftMotorAction[idx] = (float)lspeed;
 			rightMotorAction[idx] = (float)rspeed;
 			java.lang.Thread.sleep(20);
 		}
 		/* log distance ir
 		final float dd = 30.0f; // desired distance
 		final float k = 0.05f; // proportionality constant
 		while (running) {
 			float d = (62.5f/(float)a.getVoltage())-20.0f;
 			float lspeed = k*(dd-d);
 			float rspeed = -lspeed;
 			if (lspeed > rspeed) {
 				rspeed += (0.7f - lspeed);
 				lspeed = 0.7f;
 			} else {
 				lspeed += (0.7f - rspeed);
 				rspeed = 0.7f;
 			}
 			leftMotorAction[idx] = lspeed;
 			rightMotorAction[idx] = rspeed;
 			System.out.println(d);
 		}
 		*/
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
 	}
 
 	public void bye() {
 		running = false;
 	}
 }
