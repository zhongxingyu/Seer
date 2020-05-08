 package com.frc4343.robot2;
 
 import edu.wpi.first.wpilibj.Compressor;
 import edu.wpi.first.wpilibj.DigitalInput;
 import edu.wpi.first.wpilibj.DriverStationLCD.Line;
 import edu.wpi.first.wpilibj.IterativeRobot;
 import edu.wpi.first.wpilibj.Joystick;
 import edu.wpi.first.wpilibj.Relay;
 import edu.wpi.first.wpilibj.RobotDrive;
 import edu.wpi.first.wpilibj.Timer;
 import edu.wpi.first.wpilibj.Victor;
 
 public class RobotTemplate extends IterativeRobot {
 
     Logger logger = new Logger();
     Timer indexingTimer = new Timer();
     Timer loadingDelayTimer = new Timer();
     Timer accelerationTimer = new Timer();
     Joystick j = new Joystick(1);
     Joystick j2 = new Joystick(2);
     Victor launcher = new Victor(3);
     Relay indexer = new Relay(2);
     RobotDrive robotDrive = new RobotDrive(1, 2);
     Piston firingPiston = new Piston((byte) 1, (byte) 2, true);
     Piston climbingPiston = new Piston((byte) 3, (byte) 4, true);
     Compressor compressor = new Compressor(1, 1);
     DigitalInput indexerLimitSwitch = new DigitalInput(2);
     
     // The default speed for the launch motor to start at.
     double launcherSpeed = 0.4;
     double axisCompensation = 0.8;
     double indexerTimeoutInSeconds = 1.5;
     double loadingDelay = 0.15;
     double accelerationDelay = 0.1;
 
     // Motor Booleans
     boolean launcherMotor = false;
     boolean indexerMotor = false;
     
     // Auto-Fire Booleans
     boolean frisbeeLoaded = false;
 
     // Button mappings
     final byte TRIGGER = 1;
     final byte SPEED_DECREASE = 4;
     final byte SPEED_INCREASE = 5;
     final byte LAUNCHER_MOTOR_ENABLE = 6;
     final byte LAUNCHER_MOTOR_DISABLE = 7;
     final byte EXTEND_CLIMBING_PISTONS = 3;
     final byte RETRACT_CLIMBING_PISTONS = 2;
     // Button Checks
     boolean triggerHeld = false;
     boolean adjustedSpeed = false;
     
     // This section is relevant only to autonomous.
     boolean initialAutonomousDelayOver = false;
     byte numberOfFrisbeesFiredInAutonomous = 0;
     byte maxFrisbeesToFireInAutonomous = 3;
 
     final double autonomousDelayBetweenEachShot = 3;
     final double autonomousDelayBeforeFirstShot = 4;
     final double launcherSpeedAtPyramidBack = 0.4;
 
 
     private void resetRobot() {
         compressor.start();
         // Reset the timer.
         loadingDelayTimer.reset();
         loadingDelayTimer.stop();
         // Reset the launcher piston to it's retracted position.
         firingPiston.extend();
         climbingPiston.extend();
         // Reset the number of fired frisbees in autonomous to zero and reset the timer delay to allow for the re-enabling of autonomous.
         numberOfFrisbeesFiredInAutonomous = 0;
     }
 
     public void teleopInit() {
         // Initialize the compressor, reset the values, disable the motors.
         resetRobot();
         launcherMotor = true;
         indexerMotor = false;
     }
 
     public void teleopPeriodic() {
         /*// This combines the axes in order to allow for both js to control the robot's movement.
         // One of the js will be made less sensitive to allow for precision control.
         double sumXAxes = j2.getAxis(Joystick.AxisType.kY) + (j.getAxis(Joystick.AxisType.kY) * 0.5);
         double sumYAxes = -j2.getAxis(Joystick.AxisType.kX) * axisCompensation + ((-j.getAxis(Joystick.AxisType.kX) * axisCompensation) * 0.4);
 
         // Floor the values of the combined joysticks in case they are above 1 or below -1.
         sumXAxes = sumXAxes > 1 ? 1 : sumXAxes;
         sumXAxes = sumXAxes < -1 ? -1 : sumXAxes;
         sumYAxes = sumYAxes > 1 ? 1 : sumYAxes;
         sumYAxes = sumYAxes < -1 ? -1 : sumYAxes;
 
         robotDrive.arcadeDrive(sumXAxes, sumYAxes);*/
         
         robotDrive.arcadeDrive(j.getAxis(Joystick.AxisType.kX), j.getAxis(Joystick.AxisType.kY) * axisCompensation);
         // Percision Control
         robotDrive.arcadeDrive(j2.getAxis(Joystick.AxisType.kX) * 0.5, j2.getAxis(Joystick.AxisType.kY) * 0.5);
         
         firingHandler();
         launcherMotorHandler();
         climbingHandler();
         handleConsoleOutputAndMotorBooleans();
     }
 
     public void autonomousInit() {
         resetRobot();
         initialAutonomousDelayOver = false;
         launcherMotor = true;
         indexerMotor = true; 
         // Potential Double Indexing
         // Fix Check
         if (indexerLimitSwitch.get()) {
             indexerMotor = false;
             loadingDelayTimer.start();
         }
         launcherSpeed = launcherSpeedAtPyramidBack;
     }
 
     public void autonomousPeriodic() {
         frisbeeLoaded = loadingDelayTimer.get() >= loadingDelay;
         // Disable the indexer motor if a frisbee triggers the limit switch.
         if (indexerLimitSwitch.get()) {
             indexerMotor = false;
         }
         // Default Position of Piston
         if (!frisbeeLoaded) {
             firingPiston.extend();
         }
         
         // If the autonomous delay has not finished previously and the is now over, set the boolean and reset the timer.
         if (loadingDelayTimer.get() >= autonomousDelayBeforeFirstShot && !initialAutonomousDelayOver) {
             initialAutonomousDelayOver = true;
             loadingDelayTimer.reset();
         }
 
         if (initialAutonomousDelayOver) {
             if (numberOfFrisbeesFiredInAutonomous <= maxFrisbeesToFireInAutonomous) {
                 if (frisbeeLoaded) {
                 if (loadingDelayTimer.get() >= autonomousDelayBetweenEachShot) {
                     numberOfFrisbeesFiredInAutonomous++;
                     launcherSpeed = 1;
                     accelerationTimer.start();
                 }
              }
            }
         }
         
         if (accelerationTimer.get() >= accelerationDelay) {
             firingPiston.retract();
             frisbeeLoaded = false;
             loadingDelayTimer.reset();
             launcherSpeed = 0.4;
             accelerationTimer.stop();
             accelerationTimer.reset();
         }
         
         if (initialAutonomousDelayOver && !frisbeeLoaded) {
             indexerMotor = true;
         }
         
         handleConsoleOutputAndMotorBooleans();
     }
 
     private void handleConsoleOutputAndMotorBooleans() {
         indexer.set(indexerMotor ? Relay.Value.kForward : Relay.Value.kOff);
         launcher.set(launcherMotor ? launcherSpeed : 0);
         // Update the output screen.
         printConsoleOutput();
     }
 
     private void launcherMotorHandler() {
         // Manual Enable/Disable
         if (j.getRawButton(LAUNCHER_MOTOR_ENABLE) || j2.getRawButton(LAUNCHER_MOTOR_ENABLE)) {
             launcherMotor = true;
         } else if (j.getRawButton(LAUNCHER_MOTOR_DISABLE) || j2.getRawButton(LAUNCHER_MOTOR_DISABLE)) {
             launcherMotor = false;
         }
 
         // Manual Speed Settings
         if (j.getRawButton(10)) {
             launcherSpeed = 0.32;
         } else if (j.getRawButton(11)) {
             launcherSpeed = 0.4;
         }
 
         if (j.getRawButton(SPEED_INCREASE) ^ j.getRawButton(SPEED_DECREASE)) {
             // If the buttons have not been pressed previously.
             if (!adjustedSpeed) {
                 // speed change
                 if (j.getRawButton(SPEED_INCREASE)) {
                     launcherSpeed += 0.001;
                 }
                 if (j.getRawButton(SPEED_DECREASE)) {
                     launcherSpeed -= 0.001;
                 }
             }
             // button pressed check
             adjustedSpeed = j.getRawButton(SPEED_INCREASE) ^ j.getRawButton(SPEED_DECREASE);
         }
     }
 
     private void firingHandler() {
         frisbeeLoaded = loadingDelayTimer.get() >= loadingDelay;
 
         if (frisbeeLoaded) {
             loadingDelayTimer.stop();
             loadingDelayTimer.reset();
         } else if (!frisbeeLoaded) {
             firingPiston.extend();
         }
 
         if (indexerLimitSwitch.get() || indexingTimer.get() >= indexerTimeoutInSeconds) {
             indexerMotor = false;
             indexingTimer.stop();
             indexingTimer.reset();
             if (indexerLimitSwitch.get() && !(indexingTimer.get() >= indexerTimeoutInSeconds)) {
                 loadingDelayTimer.start();
             }
         }
 
         if (j.getRawButton(TRIGGER)) {
             if (!triggerHeld) {
                 if (frisbeeLoaded) {
                     launcherSpeed = 1;
                     accelerationTimer.start();
                 } else if (!frisbeeLoaded) {
                     indexerMotor = true;
                     indexingTimer.start();
                 }
             }
             triggerHeld = j.getRawButton(TRIGGER);
         }
         if (accelerationTimer.get() >= accelerationDelay) {
             firingPiston.retract();
             frisbeeLoaded = false;
             launcherSpeed = 0.4;
             accelerationTimer.stop();
             accelerationTimer.reset();
         }
         // Just in case, keeping manual eject :P
         if (j.getRawButton(9)) {
             firingPiston.retract();
             frisbeeLoaded = false;
         }
     }
 
     private void climbingHandler() {
         if (j.getRawButton(EXTEND_CLIMBING_PISTONS)) {
             climbingPiston.extend();
         } else if (j.getRawButton(RETRACT_CLIMBING_PISTONS)) {
             climbingPiston.retract();
         }
     }
 
     private void printConsoleOutput() {
         // Clears driverStation text.
         logger.clearWindow();
         // Prints State of Frisbee
         logger.printLine(Line.kUser1, frisbeeLoaded ? "Frisbee Loaded: YES" : "Frisbee Loaded: NO");
         // Print the speed.
         logger.printLine(Line.kUser2, "Launcher Speed: " + (byte)(launcherSpeed * 100) + "%");
         // Prints State of Launcher Motor
         logger.printLine(Line.kUser3, launcherMotor ? "Launcher Motor: ON" : "Launcher Motor: OFF");
         // Prints State of Launcher Motor
         logger.printLine(Line.kUser4, indexerMotor ? "Indexer Motor: ON" : "Indexer Motor: OFF");
         // Print the tank pressurization state.
         logger.printLine(Line.kUser5, compressor.getPressureSwitchValue() ? "Tanks Full: YES" : "Tanks Full: NO");
         // Updates the output window.
         logger.updateLCD();
     }
 }
