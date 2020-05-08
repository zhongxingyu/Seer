 package com.frc4343.robot2.Systems;
 
 import com.frc4343.robot2.Mappings;
 import com.frc4343.robot2.Piston;
 import com.frc4343.robot2.RobotTemplate;
 import edu.wpi.first.wpilibj.DigitalInput;
 import edu.wpi.first.wpilibj.Jaguar;
 import edu.wpi.first.wpilibj.Relay;
 import edu.wpi.first.wpilibj.Timer;
 import edu.wpi.first.wpilibj.Victor;
 
 public final class FiringSystem extends System {
 
     Timer indexingTimer = new Timer();
     Timer loadingDelayTimer = new Timer();
     Timer launchTimer = new Timer();
     Jaguar launcherMotor = new Jaguar(Mappings.LAUNCHER_MOTOR_PORT);
     Jaguar indexerMotor = new Jaguar(Mappings.INDEXER_MOTOR_PORT);
     //Relay indexerMotor = new Relay(Mappings.INDEXER_MOTOR_PORT);
     Piston firingPiston = new Piston(Mappings.FIRING_PISTON_SOLENOID_ONE, Mappings.FIRING_PISTON_SOLENOID_TWO, Mappings.FIRING_PISTON_EXTENDED_BY_DEFAULT);
     DigitalInput indexerLimitSwitch = new DigitalInput(Mappings.INDEXER_LIMIT_SWITCH_PORT);
     // The default speed for the launch motor to start at.
     double launcherMotorSpeed = 0.4;
     // Motor Booleans
     boolean isLauncherMotorRunning = false;
     boolean isIndexerMotorRunning = false;
     // Button Checks
     boolean triggerHeld = false;
     boolean adjustedSpeed = false;
     // Autonomous-only variables
     final double defaultLauncherMotorSpeed = 0.4;
     public boolean initialAutonomousDelayOver = false;
     byte maxFrisbeesToFireInAutonomous = 3;
     byte numberOfFrisbeesFiredInAutonomous = 0;
     // Teleop-only variables
     boolean firingAllFrisbees = false;
     // IDLE indicates no activity.
     static final byte IDLE = 0;
     // INDEXING indicates an attempt to feed a frisbee from the hopper to the loader.
     static final byte INDEXING = 1;
     // LOADING indicates that a frisbee is moving from the hopper to the chamber.
     static final byte LOADING = 2;
     // READY indicates that a frisbee is in the chamber and ready to fire.
     static final byte READY = 3;
     // FIRING indicates that a frisbee is now being expelled from the chamber.
     static final byte FIRING = 4;
     // RESETTING indicates that a frisbee has just been fired and the chamber is preparing to index another frisbee.
     static final byte RESETTING = 5;
 
     public FiringSystem(RobotTemplate robot) {
         super(robot);
     }
 
     public void switchMode() {
         // Reset the timers.
         loadingDelayTimer.reset();
         loadingDelayTimer.stop();
         indexingTimer.reset();
         indexingTimer.stop();
         launchTimer.reset();
         launchTimer.stop();
 
         // Reset the piston to its default position.
         firingPiston.extend();
         // Launcher motor will be enabled and reset to the default speed in case the drivers forget.
         isLauncherMotorRunning = true;
         isIndexerMotorRunning = false;
 
         launcherMotorSpeed = defaultLauncherMotorSpeed;
 
         // Reset the teleop auto-fire
         firingAllFrisbees = false;
 
         if (robot.isAutonomous()) {
             // Reset the number of fired frisbees in autonomous to zero.
             numberOfFrisbeesFiredInAutonomous = 0;
             // Reset the total number of frisbees to fire.
             maxFrisbeesToFireInAutonomous = 3;
             // The delay which occurs at the beginning of autonomous must be reset.
             initialAutonomousDelayOver = false;
             // Enable the timer which will control the initial firing delay during autonomous.
             loadingDelayTimer.start();
         }
 
         systemState = IDLE;
     }
 
     public void run() {
         switch (systemState) {
             case IDLE:
                 if (robot.isAutonomous()) {
                     // If the autonomous delay has not finished previously and the delay is now passed, set the boolean and reset the timer.
                     if (!initialAutonomousDelayOver) {
                         if (loadingDelayTimer.get() >= Mappings.AUTONOMOUS_DELAY_BEFORE_FIRST_SHOT) {
                             loadingDelayTimer.reset();
                             initialAutonomousDelayOver = true;
                         }
                     } else {
                         // If the number of frisbees already fired does not exceed the number of frisbees we want to fire during autonomous, and we have passed the delay between each shot, we attempt to load and fire another one.
                         if (numberOfFrisbeesFiredInAutonomous <= maxFrisbeesToFireInAutonomous && loadingDelayTimer.get() >= Mappings.AUTONOMOUS_DELAY_BETWEEN_EACH_SHOT) {
                             loadingDelayTimer.reset();
                             loadingDelayTimer.stop();
                             systemState = INDEXING;
                         }
                     }
                 } else {
                     // If the trigger has been pressed and is not being held, OR if we are firing all the frisbees in the robot, we begin the firing cycle.
                     if (robot.joystickSystem.getJoystick(1).getRawButton(Mappings.TRIGGER) && !triggerHeld || firingAllFrisbees == true) {
                         indexingTimer.reset();
                         indexingTimer.start();
                         systemState = INDEXING;
                     }
                 }
 
                 break;
             case INDEXING:
                 index();
                 break;
             case LOADING:
                 if (robot.isAutonomous || firingAllFrisbees == true) {
                     // Sets the motor speed to 100% for a small amount of time so as to allow for the wheel to spin back up to speed for firing.
                     launcherMotorSpeed = 1;
                 }
 
                 load();
                 break;
             case READY:
                 if (!robot.isAutonomous()) {
                     // If the trigger has been pressed and is not being held, OR if we are firing all the frisbees in the robot, we handle frisbee firing.
                     if (robot.joystickSystem.getJoystick(1).getRawButton(Mappings.TRIGGER) && !triggerHeld) {
                         launchTimer.start();
                     }
                 }
 
                 ready();
                 break;
             case FIRING:
                 fire();
                 break;
             case RESETTING:
                 reset();
                 break;
             default:
                 break;
         }
 
         if (!robot.isAutonomous()) {
             input();
         }
 
         // Store the state of whether or not the buttons have been pressed, to know if they are being held down in the next iteration.
         triggerHeld = robot.joystickSystem.getJoystick(1).getRawButton(Mappings.TRIGGER);
         adjustedSpeed = robot.joystickSystem.getJoystick(1).getRawButton(Mappings.SPEED_INCREASE) ^ robot.joystickSystem.getJoystick(1).getRawButton(Mappings.SPEED_DECREASE);
 
         // Set the state of the motors based on the values of the booleans controlling them.
         //indexerMotor.set(isIndexerMotorRunning ? Relay.Value.kForward : Relay.Value.kOff);
         indexerMotor.set(isIndexerMotorRunning ? 0.2 : 0);
         launcherMotor.set(isLauncherMotorRunning ? launcherMotorSpeed : 0);
     }
 
     private void index() {
         isIndexerMotorRunning = true;
 
         // If a frisbee triggers the limit switch while indexing, we begin LOADING.
         if (indexerLimitSwitch.get()) {
             loadingDelayTimer.reset();
             loadingDelayTimer.start();
             systemState = LOADING;
         }
         // If a frisbee is entering the loader, or if we have passed the indexer waiting time, we disable the indexer motor, and stop and reset the timer.
         if (indexerLimitSwitch.get() || indexingTimer.get() >= Mappings.INDEXER_TIMEOUT) {
             isIndexerMotorRunning = false;
 
             if (!robot.isAutonomous()) {
                 if (indexingTimer.get() >= Mappings.INDEXER_TIMEOUT) {
                     // If we were automatically firing frisbees, we stop, as there are no more frisbees left.
                     firingAllFrisbees = false;
 
                     systemState = IDLE;
                 }
 
                 // Reset the indexingTimer as we no longer have to monitor the time a frisbee has been indexing for until we enter this stage again.
                 indexingTimer.reset();
                 indexingTimer.stop();
             }
         }
     }
 
     private void load() {
         // Assumes that once the loadingDelayTimer has reached the loadingDelay, there is a frisbee in the chamber.
         if (loadingDelayTimer.get() >= Mappings.LOADING_DELAY) {
             // Reset and stop the loadingDelayTimer as we are no longer using it to track the time between individual frisbees.
             loadingDelayTimer.reset();
             loadingDelayTimer.stop();
 
             launchTimer.reset();
 
             // If the robot is in autonomous mode, we instantly begin the READY state countdown as there is no user input before firing.
             if (robot.isAutonomous()) {
                 launchTimer.start();
             } else {
                 launchTimer.stop();
             }
 
             systemState = READY;
         }
     }
 
     private void ready() {
         if (launchTimer.get() >= Mappings.ACCELERATION_DELAY) {
             // Reset the speed of the launcher motor back to the target speed.
             launcherMotorSpeed = defaultLauncherMotorSpeed;
             launchTimer.reset();
             systemState = FIRING;
         }
     }
 
     private void fire() {
         // Retract the piston to expel the frisbee.
         firingPiston.retract();
 
         if (launchTimer.get() >= Mappings.EXTEND_TIME) {
             // Increment the number of frisbees fired.
             numberOfFrisbeesFiredInAutonomous++;
             launchTimer.reset();
 
             systemState = RESETTING;
         }
     }
 
     private void reset() {
         // We extend the piston to its initial state, as there is no longer a frisbee in the chamber.
         firingPiston.extend();
 
         if (launchTimer.get() >= Mappings.RETRACT_TIME) {
             // Start the loading delay timer to measure the time between launched frisbees.
             loadingDelayTimer.reset();
             loadingDelayTimer.start();
             // After giving the piston a small amount of time to retract, we are ready to commence the cycle once more.
             systemState = IDLE;
         }
     }
 
     private void input() {
         // Handle forced (manual) ejection of a loaded frisbee.
         if (robot.joystickSystem.getJoystick(1).getRawButton(Mappings.MANUAL_EJECT)) {
             launchTimer.reset();
             launchTimer.start();
 
             systemState = FIRING;
         }
 
         // Attempt to fire all frisbees contained in the hopper.
         if (robot.joystickSystem.getJoystick(1).getRawButton(Mappings.FLUSH_HOPPER)) {
             firingAllFrisbees = true;
         }
 
         // Manually control the state of the launcherMotor motor. (Not intended to be used in competition)
         if (robot.joystickSystem.getJoystick(1).getRawButton(Mappings.LAUNCHER_MOTOR_ENABLE) || robot.joystickSystem.getJoystick(2).getRawButton(Mappings.LAUNCHER_MOTOR_ENABLE)) {
             isLauncherMotorRunning = true;
         } else if (robot.joystickSystem.getJoystick(1).getRawButton(Mappings.LAUNCHER_MOTOR_DISABLE) || robot.joystickSystem.getJoystick(2).getRawButton(Mappings.LAUNCHER_MOTOR_DISABLE)) {
             isLauncherMotorRunning = false;
         }
 
         // If the buttons are not being held down or pressed together, increase or decrease the speed of the launcherMotor motor.
         if (!adjustedSpeed) {
             if (robot.joystickSystem.getJoystick(1).getRawButton(Mappings.SPEED_INCREASE)) {
                 launcherMotorSpeed += 0.001;
             } else if (robot.joystickSystem.getJoystick(1).getRawButton(Mappings.SPEED_DECREASE)) {
                 launcherMotorSpeed -= 0.001;
             }
         }
     }
 
     public String getState() {
         switch (systemState) {
             case 0:
                 return "IDLE";
             case 1:
                 return "INDEXING";
             case 2:
                 return "LOADING";
             case 3:
                 return "READY";
             case 4:
                 return "FIRING";
             case 5:
                 return "RESETTING";
             default:
                 return "ERROR";
         }
     }
 
     public double getLauncherSpeed() {
         return launcherMotorSpeed;
     }
 
     public boolean getLauncherMotorState() {
         return isLauncherMotorRunning;
     }
 
     public boolean getIndexerMotorState() {
         return isIndexerMotorRunning;
     }
 
     public boolean isFinishedFiring() {
         return numberOfFrisbeesFiredInAutonomous == maxFrisbeesToFireInAutonomous;
     }
 
     public void setNumberOfFrisbeesToFireInAutonomous(int frisbees) {
        maxFrisbeesToFireInAutonomous = 2;
     }
 }
