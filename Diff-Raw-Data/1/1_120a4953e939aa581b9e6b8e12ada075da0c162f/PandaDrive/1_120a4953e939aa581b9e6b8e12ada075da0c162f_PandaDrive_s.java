 package Panda.Odometry;
 
 import lcmtypes.*;
 import lcm.lcm.*;
 import april.util.TimeUtil;
 import Panda.sensors.*;
 import java.util.ArrayList;
 
 public class PandaDrive
 {
     static final boolean DEBUG = true;
 
     //Speed Constants
     static final float KP = 0.0015F;
     static final float STOP = 0.0F;
     static final float MAX_SPEED = 1.0F;
     static final float MIN_SPEED = 0.0F;
     static final float REG_SPEED = 0.7F;
     static final float TPM = 5000F;
 	static final float STRAIGHT_GYRO_THRESH = 7.0F;
 	static final float ANGLE = 7.8F;
 	LCM lcm;
 
 	MotorSubscriber ms;
 	PIMUSubscriber ps;
 
     diff_drive_t msg;
 
     float leftSpeed;
     float rightSpeed;
 
     private double gyroOffset;
     private double gyroAngle;
 
     private long prevTimeInMilli;
 
     public PandaDrive(){
         try{
 			ps = new PIMUSubscriber();
 			// Get an LCM Object
             lcm = LCM.getSingleton();
 
 			// CrestLeftEncoder = curLeftEncoder;
 
 
 			msg = new diff_drive_t();
 
 			// Set motors enabled
 			// False means Enabled (lcm weirdness)
 			msg.left_enabled = false;
 			msg.right_enabled = false;
 		}
 		catch(Throwable t) {
 			System.out.println("Error: Exception thrown");
 		}
     }
 
     public void Stop(){
         // Add Timestamp (lcm really cares about this)
         msg.utime = TimeUtil.utime();
         msg.left = STOP;
         msg.right = STOP;
         lcm.publish("10_DIFF_DRIVE", msg);
     }
 
 
 	public void turn(float angle, float k) {
 		// gyro derivatives are positive
 
 		double angled_turned = 0;
 
 		int curRight = 0;
 		int curLeft = 0;
 
         motor_feedback_t motorFeedback;
         motorFeedback = new motor_feedback_t();
 
         motorFeedback = ms.getMessage();
         int initLeft = motorFeedback.encoders[0];
         int initRight = motorFeedback.encoders[1];
 
 
 		while(angled_turned < angle) {
 				msg.utime = TimeUtil.utime();
 
 				// right turn if angle is negative
 				if (angle <=0 ) {
 					msg.left = 0.3F;
 					msg.right = -0.3F;
 
 				}
 				// left turn
 				else {
 					msg.left = -0.3F;
 					msg.right = 0.3F;
 
 				}
 
 				lcm.publish ("10_DIFF_DRIVE", msg);
 
 				motorFeedback = ms.getMessage();
         		curLeft = motorFeedback.encoders[0];
     	    	curRight = motorFeedback.encoders[1];
 				
 				angled_turned = (initRight - curRight)/ANGLE; 	
 	
 				if(angled_turned < 0)
 					angled_turned *= -1;
 
 		}
 		Stop();
 	}
 
 
     public void driveForward (float distance, float kp) {
 
 		float distanceTraveled = 0;
 		int curLeftEncoder = 0, curRightEncoder = 0;
 		float leftDistance = 0, rightDistance = 0;
 		float[] pimuDerivs = new float[2];
 
 
         motor_feedback_t motorFeedback;
 		motorFeedback = new motor_feedback_t();
 
         motorFeedback = ms.getMessage();
         int initLeftEncoder = motorFeedback.encoders[0];
         int initRightEncoder = motorFeedback.encoders[1];
 
 		while (distanceTraveled < distance - .01) {
             // get updated encoder data
 
 			motorFeedback = ms.getMessage();
 			
           	curLeftEncoder = motorFeedback.encoders[0] - initLeftEncoder;
            	curRightEncoder = motorFeedback.encoders[1] - initRightEncoder;
 
             leftSpeed = REG_SPEED ;
             rightSpeed = REG_SPEED;
 
             float KError = KP*( curRightEncoder - curLeftEncoder);
             leftSpeed = speedCheck(leftSpeed + KError);
             rightSpeed = speedCheck(rightSpeed - KError);
 
 
 			msg.utime = TimeUtil.utime();
 			msg.left = leftSpeed;
 			msg.right = rightSpeed;
 
 			lcm.publish("10_DIFF_DRIVE", msg);
 
 			System.out.println ("current encoders " + curLeftEncoder + " " + curRightEncoder);
 
             // distance traveled
 			distanceTraveled = (curLeftEncoder + curRightEncoder) / (2 * kp);
 
 		}
 		Stop();
     }
 
 
     private float speedCheck (float speed){
         //If speed is greater then max
         if (speed > MAX_SPEED)
             return MAX_SPEED;
         if (speed < MIN_SPEED)
             return MIN_SPEED;
         return speed;
     }
 
 }
