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
 public class StateMachine
 {
     
     //Arm Length Constants
     static double L1 = 12; //7.5cm + 4.5cm for Base + Pivot1
     static double L2 = 10.5;
     static double L3 = 10.0;
     static double L4 = 8.0;
     //This doesn't seem right but I'm just going by the dims
     static double L5 = 19.5; //8 + 2 + 8.5 + height of claw above the board
     
     //Gripper Constants
     static double GRIPPER_OPEN = -30.0;
     static double GRIPPER_CLOSED = 0.0;
     
     double armSubBase;
     double L2Sq;
     double L3Sq;
     
     double angles[] = new double[6];
     ConstraintCheck cc = new ConstraintCheck();
 
     public StateMachine()
     {
         double armSubBase = L4 - L1;
         double L2Sq = L2 * L2;
         double L3Sq = L3 * L3;
         for (int i = 0; i < 6; i++){
             angles[i] = 0;
         }
     }
     
     protected void loadAngles(double angle, double BaseToL2, double L2ToL3, double Wrist){
         angles[0] = angle;
         angles[1] = BaseToL2;
         angles[2] = L2ToL3; 
         angles[3] = Wrist;
     }
     
     protected void openGripper() {
         angles[5] = GRIPPER_OPEN;
         //TODO: Add some feedback that checks when the ball is actually open
     }
     
     protected void closeGripper() {
         angles[5] = GRIPPER_CLOSED;
         //TODO: Add some feedback that checks when gripper is actually closed    
     }
     
     public void pickUp90(double angle, double armDistance){
         //Do angle calculations (this is explained in the README)
 
         //These angle calcs can probably be moved up into constants because they'll never change
 
         double M = Math.sqrt((armDistance*armDistance)+(armSubBase*armSubBase));
         double MSq = M * M;
        double ThetaA = Math.asin((armSubBase-M));
         double ThetaB = Math.asin((armDistance/M));
 
         double BaseToL2 = Math.acos(((L2Sq+MSq-L3Sq)/(2*L2*M)));
         double L2ToL3 = Math.acos(((L3Sq+L2Sq-MSq)/(2*L2*L3)));
         double Wrist = Math.acos(((L3Sq+MSq-L2Sq)/(2*L3*M))) + ThetaB;
 
         //This is just a test right now will need to create a state machine that uses this
        loadAngles(angle, BaseToL2, L2ToL3, Wrist);
        closeGripper();
         cc.check(angles);
     }
     
     public void pickUpStraight(double angles){}
     
     //======================================================================//
     // ballPickUp()                                                         //
     // Determins how far a ball is away from the arm. Depending on it's     //
     // different pick functions are called.                                 //
     //======================================================================//
 	public void ballPickUp(){
 
 		/*while( ! located.isEmpty()){
 			Location curBall = new Location();
 			curBall = located.get(0);
 
 			curBall = mapToBoard(curBall);
 
             System.out.println("XXXXXXXXXXXXXXXX " + curBall.x + " " +  curBall.y);
 
 			double armDistance = Math.sqrt(Math.pow(curBall.x, 2) + Math.pow(curBall.y, 2));
 
 			if(armDistance < Range1){
 				pickUp90(curBall, armDistance);
 			}
 			else if (armDistance < Range2){
 				pickUpStraight(curBall);
 			}
 			else{
 				System.out.println("Ball out of range, why was this put in found!!!!!!!");
 			}
 
 			located.remove(0);
 		}*/
 	}
 
 }
