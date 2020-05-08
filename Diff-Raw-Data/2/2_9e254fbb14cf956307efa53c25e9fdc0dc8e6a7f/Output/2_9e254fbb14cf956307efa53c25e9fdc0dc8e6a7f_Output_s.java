 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.wpi.first.wpilibj.templates;
 
 import edu.wpi.first.wpilibj.DigitalModule;
 import edu.wpi.first.wpilibj.Victor;
 
 /**
  * Generates outputs for robot
  * @author first1
  */
 public class Output {
     
     public static Victor leftDriveMotor;
     public static Victor rightDriveMotor;
     public static Victor shooterMotor;
     public static Victor climbMotor1;
     public static Victor climbMotor2;
     public static Victor kickerMotor;
     public static double dClimbPower = 1.0;
     public static int iRightDriveMotorSlot = 6;
     public static int iLeftDriveMotorSlot = 5;
     public static int iShooterMotorSlot = 3;
     public static int iClimbMotor1Slot = 2;
     public static int iClimbMotor2Slot = 1;
     public static int iKickerMotorSlot = 4;
     public static int iDropSlot1 = 1;
     public static int iDropSlot2 = 2;
     public static int iDropSlot3 = 3;
     
     public static DigitalModule digimod;
     
     public static ScreenOutput display;
     
     /**
      * Initializes the screen display
      */
     public static void initScreen(){
         display= new ScreenOutput();
     }
     
    /**
     * Turns Camera Light On
     */
    public static void setCameraLight(boolean state){
        digimod.setRelayForward(1, state);
        //display.screenWrite("Light: " + state, 1);
        
    }
    
    /**
     * Initializes the digital module
     */
    public static void initModule (){
         digimod = DigitalModule.getInstance(1);
        
     }
    
    /**
     * Opening up to drop discs in goal
     */
     public static void drop () {
         digimod.setRelayForward(iDropSlot1, true);
         digimod.setRelayForward(iDropSlot2, true);
     }
     
     /**
      * Closes claw
      */
     public static void close () {
         digimod.setRelayForward(iDropSlot1, false);
         digimod.setRelayForward(iDropSlot2, false);
         
     }
     /**
      * Reaches pyramid rung
      * @param power sets power value for motors
      */
     public static void climb (double power) {
         climbMotor1.set(power);
         climbMotor2.set(-1*power);
     }
     
     /**
      * Lifts robot above rung
      */
     public static void ascend () {
         digimod.setRelayForward(iDropSlot3, true);
     }
     
     /**
      * Creates new motor objects
      */
     public static void initMotors(){
         rightDriveMotor= new Victor(iRightDriveMotorSlot);
         leftDriveMotor= new Victor(iLeftDriveMotorSlot);
         shooterMotor= new Victor(iShooterMotorSlot);
         climbMotor1 = new Victor (iClimbMotor1Slot);
         climbMotor2 = new Victor (iClimbMotor2Slot);
         kickerMotor = new Victor (iKickerMotorSlot);
     }
     
     /**
      * Sets motor's power based on input
      * @param leftPower - Power value for left motor
      * @param rightPower - Power value for right motor
      * @param shooterPower  - Power value for the shooter motor
      */
     public static void setPower(double leftPower, double rightPower, double shooterPower){
         rightDriveMotor.set(rightPower);
         leftDriveMotor.set((-1) * leftPower);
         shooterMotor.set(shooterPower);
         
     }
     
     /**
      * Sets the motor's power based on Think.newJoystickLeft and
      * Think.newJoystickRight generated after a Think.robotThink()
      */
     public static void sendOutput(){
         setPower(Think.newJoystickLeft, Think.newJoystickRight, Think.dShooterPower);
         if (Think.bClimb1) {
             climb (dClimbPower);            
         }
         if (Think.bClimb2) {
             ascend ();
         }
  
        if(Think.image != null) {
         if (Think.currentTarget== 0) {
             display.screenWrite("Current Target: High", 0);
             display.screenWrite("Distance: Not Found...", 1);
             if (Think.image.high != null)
                 display.screenWrite("Distance: " + Think.image.highDistance, 1);
         } else if (Think.currentTarget== 1) {
             display.screenWrite("Current Target: Low Left", 0);
             display.screenWrite("Distance: Not Found...", 1);
             if (Think.image.lowLeft != null)
                 display.screenWrite("Distance " + Think.image.lowLeftDistance, 1);
         } else if (Think.currentTarget== 2) {
             display.screenWrite("Current Target: Low Right", 0);
             display.screenWrite("Distance: Not Found...", 1);
             if (Think.image.lowRight != null)
                 display.screenWrite("Distance: " + Think.image.lowRightDistance, 1);
         }
         } else {
             display.screenWrite("No Targets/Image!", 0);
             display.screenWrite("", 1);
         }
     }
 }
    }
}
 
