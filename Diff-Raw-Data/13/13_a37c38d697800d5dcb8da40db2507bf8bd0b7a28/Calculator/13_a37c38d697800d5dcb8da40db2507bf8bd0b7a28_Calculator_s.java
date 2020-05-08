 package ee.ttu.kinect.calc;
 
 import ee.ttu.kinect.model.Joint;
 
 public class Calculator {
 
 	// Calculates trajectory in 3D space (m)
 	// t = sqrt((x1-x2)^2 + (y1-y2)^2 + (z1-z2)^2)
 	public static double calculateTrajectoryLength3D(Joint joint1, Joint joint2) {
 		double s1 = joint1.getPositionX() - joint2.getPositionX();
 		double s2 = joint1.getPositionY() - joint2.getPositionY();
 		double s3 = joint1.getPositionZ() - joint2.getPositionZ();
 		return Math.sqrt(s1*s1 + s2*s2 + s3*s3);
 	}
 	
 	// Calculates velocity in 3D space (m/s)
 	// v = sqrt((x1-x2)^2 + (y1-y2)^2 + (z1-z2)^2) / (t2 - t1)
 	public static double calculateVelocity3D(Joint joint1, Joint joint2, long time1, long time2) {
 		double s1 = joint2.getPositionX() - joint1.getPositionX();
 		double s2 = joint2.getPositionY() - joint1.getPositionY();
 		double s3 = joint2.getPositionZ() - joint1.getPositionZ();
 		return (Math.sqrt(s1*s1 + s2*s2 + s3*s3) / (time2 - time1) * 1000);
 	}
 	
 	// Calculates acceleration in 3D space (m/s^2)
 	// a = (v2 - v1) / (t2 - t1)
 	public static double calculateAcceleration3D(Joint joint1, Joint joint2, Joint joint3, long time1, long time2, long time3) {
 		double v1 = Calculator.calculateVelocity3D(joint1, joint2, time1, time2);// / (time2 - time1) * 1000;
 		double v2 = Calculator.calculateVelocity3D(joint2, joint3, time2, time3);// / (time3 - time2) * 1000;
 		
		return (v2 - v1) / (time3 - time2);
 	}
 	
 	// Calculates velocity between 2 positions
 	// v = s / t
 	public static double calculateVelocity(double position1, double position2, long time1, long time2) {
 //		System.out.printf("%f %f %d %d %f \n", position1, position2, time1, time2, ((position2 - position1) / (time2 - time1) * 1000));
		return (position2 - position1) / (time2 - time1) * 1000;
 	}
 	
 	// Calculates acceleration between 2 positions (position2 and position3) having a velocities
 	// a = (v2 - v1) / (t2-t1)
 	public static double calculateAcceleration(double position1, double position2, double position3, long time1, long time2, long time3) {
 		double v1 = Calculator.calculateVelocity(position1, position2, time1, time2);
 		double v2 = Calculator.calculateVelocity(position2, position3, time2, time3);
 		
 		return (v2 - v1) / (time3 - time2);
 	}
 	
 }
