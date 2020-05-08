 import java.awt.*;
 import java.io.*;
 import static java.lang.System.out;
 
 import javax.swing.*;
 
 import lcm.lcm.*;
 import april.jmat.*;
 import april.util.*;
 import april.vis.*;
 import armlab.lcmtypes.*;
 import april.*;
 
 //======================================================================//
 // StateMachine class                                                   //
 // Class that takes in ball locations and acts as an arm state machine  //
 // moving the arm to the require postions.                              //
 //                                                                      //
 // Should take in a location an angle and distance and outputs an array //
 // of six angle values for the arm                                      //
 //======================================================================//
 public class StateMachine implements LCMSubscriber
 {
     
     static double withinConstant = 0.05;
 
     //Arm Length Constants
     static double L1 = 1.2; //7.5cm + 4.5cm for Base + Pivot1
     static double L2 = 1.05;
     static double L3 = 1.0;
     //static double L4 = 8.0;
     //This doesn't seem right but I'm just going by the dims
     static double L4 = 1.95; //8 + 2 + 8.5 + height of claw above the board
     
     //Gripper Constants
     static double GRIPPER_OPEN = 1.047;
     static double GRIPPER_CLOSED = 1.57;
     
    static double RANGE1 = 2.1;
 	static double RANGE2 = 3.9;
 	
 	//Swing position constants
 	static double BASESWING = 0.0;
 	static double L2TOL3SWING = -1.2;
 
     double armSubBase;
     double L2Sq;
     double L3Sq;
 
     double angles[] = new double[6];
     volatile double actual_angles[] = new double[6];
     ConstraintCheck cc = new ConstraintCheck();
     LCMSend send = new LCMSend();
 
     public StateMachine()
     {
         armSubBase = L4 - L1;
         L2Sq = L2 * L2;
         L3Sq = L3 * L3;
         for (int i = 0; i < 6; i++){
             angles[i] = 0;
         }
         send.send(angles);
     }
 
     protected void loadAngles(double angle, double BaseToL2, double L2ToL3, double Wrist){
         angles[0] = angle;
         angles[1] = BaseToL2;
         angles[2] = L2ToL3;
         angles[3] = Wrist;
         
         send.send(angles);
         waitUntilAngle(angle, 0);
         waitUntilAngle(BaseToL2, 1);
         waitUntilAngle(L2ToL3, 2);
         waitUntilAngle(Wrist, 3);
     }
     
     protected void swingArm(double angle){
         angles[0] = angle;
         send.send(angles);
         System.out.println("Swing entered");
         waitUntilAngle(angle, 0);
         System.out.println("Swing asdfasdfd");
     }
     
     protected void armUp(double Wrist){
         angles[1] = BASESWING;
         angles[2] = L2TOL3SWING;
         angles[3] = Wrist;
         //cc.check(angles);
         send.send(angles);
         System.out.println("Angles sent");
         waitUntilAngle(BASESWING, 1);
         System.out.println("First while executed");
         waitUntilAngle(L2TOL3SWING, 2);
         System.out.println("Second while executed");
         waitUntilAngle(Wrist, 3);
         System.out.println("Third while executed");
     }
     
     public void waitUntilAngle(double angle, int index){
         while (!((actual_angles[index] < angle+withinConstant) && (actual_angles[index] > angle-withinConstant))) {
             
             //System.out.println(actual_angles[index]);
         }
     }
 
     protected void openGripper() {
         angles[5] = GRIPPER_OPEN;
         send.send(angles);
         waitUntilAngle(GRIPPER_OPEN, 5);
     }
 
     protected void closeGripper() {
         angles[5] = GRIPPER_CLOSED;
         send.send(angles);
         waitUntilAngle(GRIPPER_CLOSED, 5);
     }
     
     protected void armUp() {
         angles[2] += .4;
         send.send(angles);
         waitUntilAngle(angles[2], 2);
     }
     
     protected void returnBall(double swing){
         armUp(-1.3);
         
         if(swing < 0)
             swingArm(-3.14);
         else
             swingArm(3.14);
             
         openGripper();
     }
     
     public void stop(){
         for (int i = 0; i < 6; i++){
             angles[i] = 0;
         }
         send.send(angles);
     }
 
     public void pickUp90(double angle, double armDistance){
         //Do arm location calculations
         System.out.println("PickUp90");
         double M = Math.sqrt((armDistance*armDistance)+(armSubBase*armSubBase));
         double MSq = M * M;
         double ThetaA = Math.asin((armSubBase/M));
         double ThetaB = Math.asin((armDistance/M));
 
         double BaseToL2 = Math.acos(((L2Sq+MSq-L3Sq)/(2*L2*M)));
         double servo2 = 1.57 - (BaseToL2 + ThetaA);
         double L2ToL3 = Math.acos(((L3Sq+L2Sq-MSq)/(2*L2*L3)));
         double servo3 = ((2*1.57) - L2ToL3);
         double Wrist = Math.acos(((L3Sq+MSq-L2Sq)/(2*L3*M)));
         double servo4 = ((2*1.57) - (Wrist + ThetaB));
 
         //This is just a test right now will need to create a state machine that uses this
         System.out.println("Angles before contraining:");
         System.out.println(servo2);
         System.out.println(servo3);
         System.out.println(servo4);
         
         openGripper();
         armUp(-servo4);
       
         swingArm(angle+.1);
         loadAngles(angles[0], -servo2, -servo3, -servo4);
         
         closeGripper();
         
         returnBall(angle); 
         
     }
 
     public void pickUpStraight(double angle, double armDistance){
         System.out.println("Straight");
         double M = Math.sqrt((armDistance*armDistance)+(L1*L1));
         double MSq = M * M;
 	    double L2L3Sq = (L2+L3)*(L2+L3);
         double Theta2 = Math.acos(((L2L3Sq)-(L4*L4)+MSq)/(2*(L2+L3)*M));
         double Theta3 = Math.asin((armDistance/M));
 	
 	    System.out.println("L2: " + L2);
 	    System.out.println("L3: " + L3);
 	    System.out.println("M: " + M);
 	    System.out.println("Theta2: " + Theta2);
 	    System.out.println("Theta3: " + Theta3);
 
         double servo2 = ((2*1.57) - (Theta2 + Theta3));
         double servo3 = 0;
         double Theta4 = Math.acos(((L2L3Sq)+(L4*L4)-MSq)/(2*(L2+L3)*L4));
         double servo4 = (2*1.57)-Theta4;
         //This is just a test right now will need to create a state machine that uses this
         System.out.println("Angles before contraining:");
         System.out.println(servo2);
         System.out.println(servo3);
         System.out.println(servo4);
         
         openGripper();
         armUp(-servo4);
         
         swingArm(angle + 0.5);
         loadAngles(angle, -servo2 + .13, -servo3, -servo4);
         
         closeGripper();
         
         armUp();
         
         returnBall(angle);
 
     }
 
     //======================================================================//
     // ballPickUp()                                                         //
     // Determins how far a ball is away from the arm. Depending on it's     //
     // different pick functions are called.                                 //
     //======================================================================//
 	public void startMachine(double angle, double armDistance){
 	    if(armDistance < .4){
 	        return;
 	    }
 	    else if(armDistance < RANGE1){
 			pickUp90(angle+0.1, armDistance);
 		}
 		else if (armDistance < RANGE2){
 			pickUpStraight(angle+0.1, armDistance);
 		}
 		else{
 			System.out.print("Ball out of range at distanc: ");
 			System.out.print(armDistance);
 			System.out.print(" Angle: ");
 			System.out.print(angle);
 		}
 	}
 
 
     @Override
     public void messageReceived(LCM lcm, String channel, LCMDataInputStream dins)
     {
         try {
             dynamixel_status_list_t arm_status = new dynamixel_status_list_t(dins);
             /* access positions using arm_status.statuses[i].position_radians */
             //System.out.println("LCM");
             for (int i = 0; i < 6; i++){
                 actual_angles[i] = arm_status.statuses[i].position_radians;
             }
         }
         catch (Exception e) {
             e.printStackTrace();
         }
     }
 }
