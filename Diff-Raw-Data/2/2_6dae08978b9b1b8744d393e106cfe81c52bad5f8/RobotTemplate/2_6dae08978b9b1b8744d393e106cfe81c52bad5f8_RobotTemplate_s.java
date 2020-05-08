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
     Timer timer = new Timer();
     Joystick joystick = new Joystick(1);
     Joystick joystick2 = new Joystick(2);
     Victor launcherMotor = new Victor(3);
     Relay indexerMotor = new Relay(2);
     RobotDrive robotDrive = new RobotDrive(1, 2);
     Piston firingPiston = new Piston(1, 2, true);
     Piston climbingPiston = new Piston(3, 4, true);
     Compressor compressor = new Compressor(1, 1);
     DigitalInput indexerLimitSwitch = new DigitalInput(2);
 
     // The default speed for the launch motor to start at.
     double speed = 0.32;
     double axisCompensation = 0.8;
     double indexerTimeoutInSeconds = 1.5;
 
     // Whether or not the launch speed launcherMotor buttons are being pressed.
     boolean isLauncherMotorRunning = false;
     boolean isIndexerMotorRunning = false;
     boolean isFrisbeeLoaded = false;
     boolean isTriggerHeld = false;
     boolean adjustSpeed = false;
     boolean previouslyChangedSpeeds = false;
 
     // Button mappings
     final byte TRIGGER = 1;
     final byte SPEED_DECREASE = 4;
     final byte SPEED_INCREASE = 5;
     final byte LAUNCHER_MOTOR_ENABLE = 6;
     final byte LAUNCHER_MOTOR_DISABLE = 7;
     final byte EXTEND_CLIMBING_PISTONS = 3;
     final byte RETRACT_CLIMBING_PISTONS = 2;
 
     // This section is relevant only to autonomous.
     boolean isInitialAutonomousDelayOver = false;
     boolean readyToIndexNextFrisbee = false;
     byte numberOfFrisbeesFiredInAutonomous = 0;
     byte maximumFrisbeesToFireInAutonomous = 3;
     final byte autonomousFireType = 0;
     final double autonomousDelayBetweenEachShot = 3;
     final double autonomousDelayBeforeFirstShot = 4;
     final double delayToPistonRetraction = 0.1;
     final double speedWhenStartingAtBackOfPyramid = 0.4;
     final double speedWhenStartingAtSideOfPyramid = 0.4;
 
     private void resetRobot() {
         compressor.start();
         // Reset the timer.
         timer.reset();
         timer.stop();
         // Reset the launcher piston to it's retracted position.
         firingPiston.extend();
         climbingPiston.extend();
         // Reset the number of fired frisbees in autonomous to zero and reset the timer delay to allow for the re-enabling of autonomous.
         numberOfFrisbeesFiredInAutonomous = 0;
         readyToIndexNextFrisbee = true;
         isLauncherMotorRunning = true;
     }
 
     public void teleopInit() {
         resetRobot();
         isIndexerMotorRunning = false;
         isInitialAutonomousDelayOver = true;
     }
 
     public void teleopPeriodic() {
         // This combines the axes in order to allow for both joysticks to control the robot's movement.
         // One of the joysticks will be made less sensitive to allow for precision control.
         double sumXAxes = joystick2.getAxis(Joystick.AxisType.kY) + (joystick.getAxis(Joystick.AxisType.kY) * 0.5);
         double sumYAxes = -joystick2.getAxis(Joystick.AxisType.kX) * axisCompensation + (-joystick.getAxis(Joystick.AxisType.kX) * axisCompensation);
 
         // Floor the values of the combined joysticks in case they are above 1 or below -1.
         sumXAxes = sumXAxes > 1 ? 1 : sumXAxes;
         sumXAxes = sumXAxes < -1 ? -1 : sumXAxes;
         sumYAxes = sumYAxes > 1 ? 1 : sumYAxes;
         sumYAxes = sumYAxes < -1 ? -1 : sumYAxes;
 
         robotDrive.arcadeDrive(sumXAxes, sumYAxes);
 
         handleTimerAndFrisbeeLoadedState();
         handleLauncherMotor();
         solenoidHandler();
 
         handleConsoleOutputAndMotorBooleans();
     }
 
     public void autonomousInit() {
         resetRobot();
         isInitialAutonomousDelayOver = false;
         isIndexerMotorRunning = true;
 
         // Depending on which type of autonomous we're using, change the initial frisbee count and launcher speed.
         if (autonomousFireType == 0) {
             speed = speedWhenStartingAtBackOfPyramid;
             maximumFrisbeesToFireInAutonomous = 3;
         } else if (autonomousFireType == 1) {
             speed = speedWhenStartingAtSideOfPyramid;
             maximumFrisbeesToFireInAutonomous = 3;
         }
 
         // Initialize the timer to begin the autonomous launch delay.
         timer.start();
     }
 
     public void autonomousPeriodic() {
         // Disable the indexer motor if a frisbee triggers the limit switch.
         if (indexerLimitSwitch.get()) {
             isIndexerMotorRunning = false;
             readyToIndexNextFrisbee = false;
             isFrisbeeLoaded = true;
         }
 
         if (!isInitialAutonomousDelayOver) {
             // If the autonomous delay has not finished previously and the is now over, set the boolean and reset the timer.
             if (timer.get() >= autonomousDelayBeforeFirstShot) {
                 isInitialAutonomousDelayOver = true;
                 timer.reset();
             }
         }
         else {
            if (numberOfFrisbeesFiredInAutonomous <= maximumFrisbeesToFireInAutonomous) {
                     // Once the delay per shot has been reached, fire the next frisbee.
                     if (timer.get() >= autonomousDelayBetweenEachShot) {
                         // Increment the frisbee count, retract the piston, and reset the timer.
                         numberOfFrisbeesFiredInAutonomous++;
                         firingPiston.retract();
                         timer.reset();
 
                         // If we can still fire another frisbee, prepare to index.
                         if (numberOfFrisbeesFiredInAutonomous != maximumFrisbeesToFireInAutonomous) {
                             readyToIndexNextFrisbee = true;
                         }
                     }
             }
         }
 
         handleConsoleOutputAndMotorBooleans();
     }
 
     private void handleConsoleOutputAndMotorBooleans() {
         // If the piston retraction delay has passed, begin to retract the piston.
         if (timer.get() > delayToPistonRetraction && timer.get() < (delayToPistonRetraction + 0.5)){
             firingPiston.extend();
         }
         // If 0.4 s has passed since the piston retracted, we can index the next frisbee, if there is still one left.
         // If there are no frisbees left, we do not attempt to index another, but rather reset and stop the the timer.
         if (timer.get() > delayToPistonRetraction + 0.4 && isInitialAutonomousDelayOver && readyToIndexNextFrisbee) {
             isIndexerMotorRunning = true;
         }
 
         indexerMotor.set(isIndexerMotorRunning ? Relay.Value.kForward : Relay.Value.kOff);
         launcherMotor.set(isLauncherMotorRunning ? speed : 0);
 
         // Update the output screen.
         printConsoleOutput();
     }
 
     private void handleTimerAndFrisbeeLoadedState() {
         // If the frisbee hits limit switch, registers it as loaded and turns off the loader motor.
         if (indexerLimitSwitch.get() || timer.get() >= indexerTimeoutInSeconds) {
             // Disables the indexer motor if a frisbee is detected or if the timer runs out, also resets the timer and stops it.
             isIndexerMotorRunning = false;
             timer.reset();
             timer.stop();
 
             // If a frisbee hit the limit switch, we store the fact that we have a frisbee loaded and that we're not ready to index.
             // If the timer ran out, we do the opposite.
             isFrisbeeLoaded = indexerLimitSwitch.get();
             readyToIndexNextFrisbee = !indexerLimitSwitch.get();
         }
     }
 
     private void handleLauncherMotor() {
         // Check if the motor is being run.
         if (joystick.getRawButton(LAUNCHER_MOTOR_ENABLE) || joystick2.getRawButton(LAUNCHER_MOTOR_ENABLE)) {
             isLauncherMotorRunning = true;
         } else if (joystick.getRawButton(LAUNCHER_MOTOR_DISABLE) || joystick2.getRawButton(LAUNCHER_MOTOR_DISABLE)) {
             isLauncherMotorRunning = false;
         }
 
         boolean speedUpLauncherMotor = joystick.getRawButton(SPEED_INCREASE);
         boolean slowDownLauncherMotor = joystick.getRawButton(SPEED_DECREASE);
 
         if (joystick.getRawButton(10)) {
             speed = 0.32;
         } else if (joystick.getRawButton(11)) {
             speed = 0.4;
         }
 
         // Manually eject the frisbee
         if (joystick.getRawButton(9)) {
             firingPiston.retract();
             isFrisbeeLoaded = false;
             timer.start();
             readyToIndexNextFrisbee = true;
         }
 
         // Checks to see if either the speed increase or decrease buttons are pressed.
         if (speedUpLauncherMotor ^ slowDownLauncherMotor) {
             // If the buttons have not been pressed previously.
             if (!previouslyChangedSpeeds) {
                 // Handle the speed change.
                 if (speedUpLauncherMotor) {
                     speed += 0.001;
                 }
                 if (slowDownLauncherMotor) {
                     speed -= 0.001;
                 }
             }
 
             // Set the button state to indicate the buttons have been pressed.
             previouslyChangedSpeeds = true;
         } else {
             previouslyChangedSpeeds = false;
         }
     }
 
     private void solenoidHandler() {
         if (joystick.getRawButton(EXTEND_CLIMBING_PISTONS)) {
             climbingPiston.extend();
         } else if (joystick.getRawButton(RETRACT_CLIMBING_PISTONS)) {
             climbingPiston.retract();
         }
 
         // If the trigger is pressed.
         if (joystick.getRawButton(TRIGGER)) {
             if (!isTriggerHeld) {
                 // If there is no frisbee in the launcher, turns on the motor to load a new one.
                 if (isFrisbeeLoaded) {
                     // If there is a frisbee in the launcher, then it launches it.
                     firingPiston.retract();
                     isFrisbeeLoaded = false;
                 }
                 timer.start();
                 readyToIndexNextFrisbee = true;
             }
 
             isTriggerHeld = true;
         } else {
             isTriggerHeld = false;
         }
     }
 
     private void printConsoleOutput() {
         // Clears driverStation text.
         logger.clearWindow();
         // Prints State of Frisbee
         logger.printLine(Line.kUser1, isFrisbeeLoaded ? "Frisbee Loaded: True" : "Frisbee Loaded: False");
         // Print the speed.
         logger.printLine(Line.kUser2, "Launcher Speed: " + speed * 100 + "%");
         // Prints State of Launcher Motor
         logger.printLine(Line.kUser3, isLauncherMotorRunning ? "Launcher Motor: True" : "Launcher Motor: False");
         // Prints State of Launcher Motor
         logger.printLine(Line.kUser4, isIndexerMotorRunning ? "Indexer Motor: True" : "Indexer Motor: False");
         // Print the tank pressurization state.
         logger.printLine(Line.kUser5, compressor.getPressureSwitchValue() ? "Tanks Full: YES" : "Tanks Full: NO");
         // Displays the timer value.
         logger.printLine(Line.kUser6, Double.toString(timer.get()));
         // Updates the output window.
         logger.updateLCD();
     }
 }
