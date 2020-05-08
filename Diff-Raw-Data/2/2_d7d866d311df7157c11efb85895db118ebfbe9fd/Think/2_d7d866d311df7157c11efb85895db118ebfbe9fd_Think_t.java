 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.wpi.first.wpilibj.templates;
 
 /**
  *
  * @author first1
  */
 public class Think {
 
     public static double newJoystickLeft;
     public static double newJoystickRight;
     public static boolean bShooterOn;
     public static double dShooterPower;
     public static boolean bClimb1;
     public static boolean bClimb2;
     
     public static double[] aimAdjust(double right, double left, CameraData cd){
         return new double[]{left, right};
     }
     
     /*
      * Converts the joystick values to usable values.
      * @param rawRight the value of the right joystick.
      * @param rawLeft the value of the left joystick.
      * @returns usable values as an array. The left value is at index 0
      *      The right is at index 1
      */
     public static double[] processJoystick(double rawRight, double rawLeft){ 
         double[] retVal = new double[2];
         
         if(rawLeft>0){
             retVal[0]= rawLeft * rawLeft;   
         }
         else{
             retVal[0]= (-1) * (rawLeft * rawLeft);
         }
         if(rawRight>0){
             retVal[1]= rawRight * rawRight;
         }
         else{
             retVal[1]= (-1) * (rawRight * rawRight);
         }
         
         retVal[1] *= (-1);
         
         return retVal;
     }
     
     public static void robotThink(){
         double[] temp = new double[2];
         temp= processJoystick(Input.rightY, Input.leftY);
         newJoystickLeft= temp[0];
         newJoystickRight= temp[1];
         bShooterOn = Input.bTriggerDown;
         
             
         if (Input.bSlowSpeedRight||Input.bSlowSpeedLeft){
             newJoystickLeft *= .75;
             newJoystickRight *= .75;
         }
         if (Input.bClimb1Left||Input.bClimb1Right){
             bClimb1= true;
         }
         
         if (Input.bClimb2Left||Input.bClimb2Right){
             bClimb2= true;
         }
                 
         if (Input.bAim){
           // temp = aimAdjust(newJoystickLeft, newJoystickRight, Input.cd);
             newJoystickLeft= temp[0];
             newJoystickRight= temp[1];
             
         }
         if (bShooterOn== false){
             dShooterPower = 0;
            
         } // Test Commit
         else {
             dShooterPower = 1.0;
         }
     }
     
     
 }
