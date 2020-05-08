 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package robot;
 
 import edu.wpi.first.wpilibj.DriverStation;
 import edu.wpi.first.wpilibj.Joystick;
 import edu.wpi.first.wpilibj.Victor;
 
 /**
  *
  * @author tanner
  */
 public class Intake {
     //private enum mode {INTAKE, STOP, FIRE}
 
     private Joystick joystick;
     private Victor beltMotorOne, beltMotorTwo, unobtaniumMotor;
     private int intakeMode, previousMode;
     private DriverStation driverStation;
 
     /**
      * Creates our intake system.
      * @param joystick Joystick of which it is to be controlled by.
      * @param intakeMotorOne First belt motor.
      * @param intakeMotorTwo Second belt motor.
      * @param unobtaniumMotor Unobtanium motor.
      */
     public Intake(Joystick joystick, Victor beltMotorOne, Victor beltMotorTwo, Victor unobtaniumMotor) {
         this.joystick = joystick;
         this.beltMotorOne = beltMotorOne;
         this.beltMotorTwo = beltMotorTwo;
         this.unobtaniumMotor = unobtaniumMotor;
 
         intakeMode = 0;
         previousMode = 0;
         driverStation = DriverStation.getInstance();
     }
 
     /**
      * Perform the actions that the mode we are in requires.
      */
     public void doAction() {
         double beltSpeed = 0;
         double unobtaniumSpeed = 0;
 
         //Set the mode of the system, based on our button input
        if (joystick.getRawButton(3)) {
             //Intake Button
             if (intakeMode == 1) {
                 //Mode is at intake, ergo stop.
                 intakeMode = 0;
             } else {
                 //Mode is not at intake
                 intakeMode = 1;
             }
         } else {
             //TODO: Put this function into a function? Maybe...
 
             //Reverse Button
            if (joystick.getRawButton(2)) {
                 //Reverse button was hit
                 previousMode = intakeMode;
                 intakeMode = -1;
            } else if (!joystick.getRawButton(2) && intakeMode == -1) {
                 //We don't want to be in reverse anymore
                 intakeMode = previousMode;
                 previousMode = 0;
             }
             //Fire Button
             if (joystick.getRawButton(1)) {
                 //Fire button was hit
                 previousMode = intakeMode;
                 intakeMode = 2;
             } else if (!joystick.getRawButton(1) && intakeMode == 2) {
                 //We don't want to be firing anymore
                 intakeMode = previousMode;
                 previousMode = 0;
             }
         }
 
         //Put our chosen mode into action!
         //TODO: Make sure values are in the correct direction.
        System.out.println("Intake Mode:"+intakeMode);
         switch (intakeMode) {
             case -1:
                 //Reverse mode
                 beltSpeed = -1;
                 unobtaniumSpeed = 0;
                 break;
             case 0:
                 //Stop mode
                 beltSpeed = 0;
                 unobtaniumSpeed = 0;
                 break;
             case 1:
                 //Intake mode
                 beltSpeed = 1;
                 unobtaniumSpeed = -0.5;
                 break;
             case 2:
                 //Fire mode
                 beltSpeed = 1;
                 //More to it than this, but I can't find the formula
                 unobtaniumSpeed = driverStation.getAnalogIn(1);
                 break;
             default:
                 //Ahhh! mode
                 beltSpeed = 0;
                 unobtaniumSpeed = 0;
                 break;
         }
 
         //Put mode into action
         setBeltSpeed(beltSpeed);
         setUnobtaniumSpeed(unobtaniumSpeed);
     }
 
     /**
      * Set the speed of the both belt motors.
      * @param speed Value of motor speed from -1 to 1
      */
     public void setBeltSpeed(double speed) {
         //As a safety measure of un-tested code, the following line is commented out to prevent *boom*
         //beltMotorOne.set(speed);
         beltMotorTwo.set(speed);
     }
 
     /**
      * Set the speed of the unobtanium motor for easy access.
      * @param speed Value of motor speed from -1 to 1
      */
     public void setUnobtaniumSpeed(double speed) {
         unobtaniumMotor.set(speed);
     }
 }
