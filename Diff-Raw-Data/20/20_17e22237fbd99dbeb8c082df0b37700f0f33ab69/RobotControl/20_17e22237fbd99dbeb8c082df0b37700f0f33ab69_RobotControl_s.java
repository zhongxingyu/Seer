 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package farmington.ultimateascent;
 
 import edu.wpi.first.wpilibj.Timer;
 import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
 
 /**
  * The main control class for our robot.
  *
  * @author 3414
  */
 public class RobotControl extends BaseRobot implements IRobot {
 
     double wheelOneSpeed;
     double wheelTwoSpeed;
     boolean onTargetX;
     boolean onTargetY;
     boolean liftControl;
     double driveScaling;
     double targetVoltage;
     double manualTargetVoltage;
     double offset;
     Waiter LEDCycleControl;
 
     /**
      * Main constructor for RobotControl.
      */
     public RobotControl() {
         super();
         wheelOneSpeed = 0.70;
         wheelTwoSpeed = 1.0;
         onTargetX = false;
         onTargetY = false;
         liftControl = false;
         driveScaling = 1.0;
         manualTargetVoltage = 0.0;
         offset = 0.0;
         LEDCycleControl = new Waiter();
     }
 
     /**
      * Puts all information we need to see in the Driver Station on the
      * SmartDashboard.
      */
     private void updateDashboard() {
         SmartDashboard.putBoolean("Loader Wheel Sensor", myShooterLoader.getLoaderSensor());
         SmartDashboard.putBoolean("Chamber Side Sensor", myShooterLoader.getChamberSideSensor());
         SmartDashboard.putBoolean("Chamber Bottom Sensor", myShooterLoader.getChamberBottomSensor());
         SmartDashboard.putNumber("Potentiometer", myShooterScrew.getVoltage());
     }
 
     /**
      * Redirect method for autonomous control.
      */
     public void autonomous() {
         myPyramidLifter.goDown();
         myShooterLoader.turnOff();
         if (leftStick.getRawAxis(SWITCH_AXIS) < 0 && rightStick.getRawAxis(SWITCH_AXIS) > 0) {
             targetVoltage = 1.870;  //Angle for back of pyramid (was 1.980)
         } else {
            targetVoltage = 1.870;  //Angle for corner of pyramid and SUPER SPECIAL mode (was 1.980)
         }
         myShooterWheels.setWheelSpeeds(wheelOneSpeed, wheelTwoSpeed);
         boolean screwIsGood = false;
         boolean driveIsGood = false;
         double time = 0.000;
         while (!screwIsGood || !driveIsGood) {  //If either the screw or the drive aren't ready:
             if (myShooterScrew.getVoltage() <= targetVoltage) {
                 myShooterScrew.setMovement(false, false, SCREW_OFF);
                 screwIsGood = true;
             } else {
                 myShooterScrew.setMovement(false, true, SCREW_FULL);
             }
 
             //Drive backwards while on the back of the pyramid
             if (rightStick.getRawAxis(SWITCH_AXIS) > 0 && leftStick.getRawAxis(SWITCH_AXIS) < 0) {
                 time += 0.010;
                 if (time >= 0.750) {
                     myDrive.setSpeed(SPEED_STOP);
                     driveIsGood = true;
                 } else {
                     myDrive.setSpeed(SPEED_REVERSE_HALF);
                 }
             } else {
                 driveIsGood = true;
             }
             Timer.delay(0.010);
         }
         Timer.delay(0.250); //Wait for the screw to settle
         int i = 1;
         while (i <= 3) {
             myShooterPiston.set(true);
             Timer.delay(0.24);  //Extended for .24 of a second               
             myShooterPiston.set(false);
             Timer.delay(0.1);  //Wait for .1 of a second
             myShooterLoader.turnOn();
             Timer.delay(0.75); //Wait for the frisbee to drop in
             myShooterLoader.turnOff();
             Timer.delay(0.5); //Wait 1/2 second for the frisbee to settle and wheels to reach speed
             i++;
         }
         myShooterWheels.setWheelSpeeds(SPEED_STOP, SPEED_STOP);
         
         if (rightStick.getRawAxis(SWITCH_AXIS) < 0) {
             myDrive.setSpeed(-0.2, -0.7);    //THIS MAKES IT GO BACKWARDS
             Timer.delay(1.8);
             myDrive.setSpeed(SPEED_STOP);
         } else if (rightStick.getRawAxis(SWITCH_AXIS) > 0 && leftStick.getRawAxis(SWITCH_AXIS) > 0) {
             myDrive.setSpeed(SPEED_REVERSE_HALF);
             Timer.delay(1.0);
             myDrive.setSpeed(SPEED_STOP);
         }
         
         //DEBUG: Disabled because autonomous extends into teleop right now
         //Find a way to break out of this autonomous loop when teleop is activated or the robot is disabled
 //        while (myShooterScrew.getVoltage() < 3.5) {
 //            myShooterScrew.setMovement(true, false, 1.0);
 //        }
         myShooterScrew.setMovement(false, false, 0.0);
     }
 
     public void resetSystems() {
         myShooterPiston.reset();
         myShooterLoader.reset();
 
         //reset autonomous
         myShooterScrew.setMovement(false, false, SCREW_OFF);
         myDrive.setSpeed(SPEED_STOP);
         myShooterPiston.set(false);
         myShooterWheels.setWheelSpeeds(SPEED_STOP, SPEED_STOP);
         myPyramidLifter.goDown();
     }
 
     /**
      * Updates important systems, called every 20 milliseconds in Main.java.
      */
     public void twentyMSLoop() {
         //Drive speed scaling
         if (rightStick.getRawButton(TRIGGER)) {
             driveScaling = 1.0;
         } else {
             driveScaling = 0.5;
         }
 
         //Drive Train
         if (rightStick.getRawButton(BUTTON_TEN)) {
             myDrive.setSpeedWithJoysticks(driveScaling * rightStick.getRawAxis(VERTICAL_AXIS));
         } else {
             myDrive.setSpeedWithJoysticks(driveScaling * leftStick.getRawAxis(VERTICAL_AXIS), driveScaling * rightStick.getRawAxis(VERTICAL_AXIS));
         }
 
         //Screw with manual positioning
         if (!(gamepad.getRawButton(BUTTON_FOUR) || gamepad.getRawButton(BUTTON_TWO))) {
             if (gamepad.getRawButton(BUTTON_THREE)) {
                 //Loading angle
                 manualTargetVoltage = 3.50;
                 offset = 0.025;
             } else if (gamepad.getRawButton(BUTTON_ONE)) {
                 //Shooting angle from back of pyramid
                manualTargetVoltage = 1.790;
                 offset = 0.050;
             }
             if (manualTargetVoltage != 0.0) {
                 int position = myShooterScrew.isOnTarget(manualTargetVoltage, offset);
                 if (position == -1) {
                     myShooterScrew.setMovement(true, false, SCREW_FULL);
                 } else if (position == 1) {
                     myShooterScrew.setMovement(false, true, SCREW_FULL);
                 } else {
                     myShooterScrew.setMovement(false, false, SCREW_OFF);
                     manualTargetVoltage = 0.0;
                 }
             } else {
                 myShooterScrew.setMovement(false, false, SCREW_OFF);
             }
         } else {
             manualTargetVoltage = 0.0;
             double speedFactor;
             if (gamepad.getRawButton(BUTTON_FIVE)) {
                 speedFactor = 0.75;
             } else {
                 speedFactor = 1.0;
             }
             myShooterScrew.setMovement(gamepad.getRawButton(BUTTON_FOUR), gamepad.getRawButton(BUTTON_TWO), speedFactor);
         }
 
         //Piston
         myShooterPiston.shootWithTimeDelay(gamepad.getRawButton(BUTTON_EIGHT));
 
         //Automatic loader update
         myShooterLoader.updateLoader(gamepad.getRawButton(BUTTON_SIX)); //DEBUG: Disabled because sensor is broken
 //         myShooterLoader.manualControl(gamepad.getRawButton(BUTTON_SIX)); //Enable this when the sensor is broke
 
         //Shooter Wheel Speed
         //Matt's button :D
         if (gamepad.getRawButton(BUTTON_NINE)) {
             wheelOneSpeed = 0.4;
             wheelTwoSpeed = 0.65;
         } else {
             wheelOneSpeed = 0.7;
             wheelTwoSpeed = 1.0;
         }
         if (gamepad.getRawButton(BUTTON_SEVEN)) {
             myShooterWheels.setWheelSpeeds(wheelOneSpeed, wheelTwoSpeed);
         } else {
             myShooterWheels.setWheelSpeeds(SPEED_STOP, SPEED_STOP);
         }
 
         //Pyramid lifter logic
         if ((rightStick.getRawButton(BUTTON_EIGHT) || rightStick.getRawButton(BUTTON_TWO)) && liftControl) {
             myPyramidLifter.alternate();
             liftControl = false;
         } else if (!rightStick.getRawButton(BUTTON_EIGHT) && !rightStick.getRawButton(BUTTON_TWO)) {
             liftControl = true;
         }
 
 //        //LED Manual Cycling THIS DON'T WORK
 //        if (leftStick.getRawButton(TRIGGER) && LEDCycleControl.timeUp()) {
 //            myLed.cycleColors();
 //            LEDCycleControl.waitXms(500);
 //        }
 //
 //        //LED Automatic Color changes THIS DON'T WORK EITHER
         //
 //        if (!myShooterLoader.getChamberSideSensor()) {
 //            myLed.setGreen();
 //            myLed.canCycle = false;
 //        } else if (myShooterLoader.getChamberSideSensor() && myShooterLoader.getLoaderSensor()) {
 //            myLed.setBlue();
 //            myLed.canCycle = false;
 //        } else if (myShooterLoader.getLoaderSensor()) {
 //            myLed.setPurple();
 //            myLed.canCycle = true;
 //        }
 
         this.updateDashboard();
     }
 
     /**
      * Called every 100 milliseconds in Main.java.
      */
     public void hundredMSLoop() {
     }
 
     /**
      * Called every second in Main.java.
      */
     public void thousandMSLoop() {
     }
         
 }
