 package edu.wpi.first.wpilibj.templates;
 
 import edu.wpi.first.wpilibj.*;
 
 public class RobotTemplate extends IterativeRobot {
     //YUM!
     static final double PI = 3.1415926535897932384626433832795;
 
     //Maximum Robot Speed in m/s
     static final double NORMAL_WHEEL_SPEED = 2.7764;
     static final double SLOW_WHEEL_SPEED = 1.0;
     static final double AUTONOMOUS_WHEEL_SPEED = 0.333;
 
     //Number of Encoder Counts per Metre Travelled
     static final double COUNTS_PER_METRE = 900.0;
 
     //Maximum Robot Rotational Speed in rad/s
     static final double NORMAL_ANGULAR_VELOCITY = 4*PI;
     static final double SLOW_ANGULAR_VELOCITY = PI;
     static final double AUTONOMOUS_ANGULAR_VELOCITY = PI / 2;
 
     //Mecanum Dimensions: A = Half Robot Width, B = Half Robot Length (in metres)
     static final double DISTANCE_A = 0.375;
     static final double DISTANCE_B = 0.255;
 
     //Minimum Size of Target
     static final double MINIMUM_SCORE = 0.01;
 
     //Number of camera frames per second
     static final int FRAME_SKIP = 10;
 
     //Kicker encoder counts
     static final int MAX_KICKER_ENCODER_COUNTS = 850;
 
     //Max power for the winch
     static final double KICKER_POWER = 0.75;
 
     //dont start winching until this delay has passed after a kick
     static final double KICK_WINCH_DELAY = 1.25;
 
     //Delay after kick
     static final double KICK_MIN_DELAY = 2.0 - KICK_WINCH_DELAY;
 
     //Driver buttons
     static final int SLOW_BUTTON = 6;
     static final int TRACK_TARGET_BUTTON = 8;
     static final int REVERSE_BUTTON = 5;
 
     //Operator buttons
     static final int KICK_BUTTON = 1;
     static final int ROLLER_IN_BUTTON = 2;
     static final int ROLLER_OUT_BUTTON = 3;
     static final int HANGER_UP_BUTTON = 4;
     static final int HANGER_DOWN_BUTTON = 5;
 
     //Joysticks
     Joystick stickOne = new Joystick(1);
     Joystick stickTwo = new Joystick(2);
 
     //Wheel jaguars
     Jaguar jagFrontLeft = new Jaguar(1);
     Jaguar jagBackLeft = new Jaguar(2);
     Jaguar jagFrontRight = new Jaguar(3);
     Jaguar jagBackRight = new Jaguar(4);
 
     //Kicker
     Victor vicKicker = new Victor(5);
 
     //Roller
     Victor vicRoller = new Victor(7);
 
     //Sea shell
     Victor vicRelease = new Victor(8);
 
     //Extra victor
     Victor vicTest = new Victor(9);
 
 
     //Wheel encoders, reversed if needed to be
     PIDEncoder encFrontLeft = new PIDEncoder(1, 2, true); //Reversed
     PIDEncoder encBackLeft = new PIDEncoder(3, 4, true); //Reversed
     PIDEncoder encFrontRight = new PIDEncoder(5, 6);
     PIDEncoder encBackRight = new PIDEncoder(7, 8);
 
     //Encoder on the kicker
     Encoder encKicker = new Encoder(9, 10, false, Encoder.EncodingType.k1X);
 
     //PID Controllers for each wheel, with their speeds negated as neccessary
     PIDController pidFrontLeft = new PIDController(0.0, -0.00003, 0.0, encFrontLeft, jagFrontLeft, 0.005);
     PIDController pidBackLeft = new PIDController(0.0, -0.00003, 0.0, encBackLeft, jagBackLeft, 0.005);
     PIDController pidFrontRight = new PIDController(0.0, 0.00003, 0.0, encFrontRight, jagFrontRight, 0.005);
     PIDController pidBackRight = new PIDController(0.0, 0.00003, 0.0, encBackRight, jagBackRight, 0.005);
 
     //Light sensor on the sea shell
     DigitalInput releaseSensor = new DigitalInput(11);
 
     //Light sensor check if we have a ball
     DigitalInput ballSensor = new DigitalInput(12);
 
     //Kill switch if kicker has raised too far
     DigitalInput kickerKillSwitch = new DigitalInput(14);
 
     //Reverse driving
     boolean driveReverse;
     boolean reverseButtonReleased;
 
     int kickerSetpoint;
 
     //Autonomous mode
     int autonomousMode;
 
     //Last time the kicker was kicked
     double lastKickTime;
 
     //Autonomous balls kicked
     int ballsLeft;
 
     //If the kicker was released the last run
     static boolean prevRelease = false;
 
     public class KickState
     {
         //Kicker is at its setpoint
         static final int Default = 0;
         //Kicker is moving to its setpoint
         static final int Loading = 1;
         //Trigger was pressed
         static final int Kicking = 2;
     };
 
     public class TriggerState
     {
         //Sea shell in default position, light sensor is on the tape
         static final int Engaged = 0;
 
         //Run the sea shell until the light sensor doesnt see the tape
         static final int Releasing = 1;
 
         //Run the sea shell until the light is seen again
         static final int Released = 2;
 
         //Identify the state of the sea shell
         static final int Unknown = 3;
     };
 
     //Current states for state machines
     int currentKickState;
     int currentTriggerState;
 
     //Runs when the robot is turned
     public void robotInit() {
         //Reverse driving
         driveReverse = true;
         //
         reverseButtonReleased = true;
 
         //Kicker target setpoint
         kickerSetpoint = 0;
 
         //Autonomous mode
         autonomousMode = 0;
 
         //Last time the kicker was kicked
         lastKickTime = 0.0;
 
         //Autonomous balls kicked
         ballsLeft = 0;
     }
 
     //Runs at the beginning of autonomous period
     public void autonomousInit() {
         
     }
 
     //Runs periodically during autonomous period
     public void autonomousPeriodic() {
 
     }
 
     //Runs continuously during autonomous period
     public void autonomousContinuous() {
 
     }
 
     //Runs at the beginning of teleoperated period
     public void teleopInit() {
 
     }
 
     //Runs periodically during teleoperated period
     public void teleopPeriodic() {
        System.out.println(encKicker.get());
     }
 
     //A boolean object, who's value can be changed from inside a function
     public class BooleanHolder {
         //Value of object
         boolean value;
 
         //Defaults to false
         public BooleanHolder() {
             value = false;
         }
 
         //Overloaded constructor to take any value of boolean
         public BooleanHolder(boolean val) {
             value = val;
         }
 
         //Get boolean
         public final boolean get() {
             return value;
         }
 
         //Set boolean
         public void set(boolean val) {
             value = val;
         }
 
     }
 
     //Kicker state machine
     int runKickStateMachine(int curState, int setpoint, int encoder, boolean trigger, boolean ballReady, boolean forceKick, BooleanHolder winch, BooleanHolder release) {
         //Turn off the winch and the sea shell by default
         winch.set(false);
         release.set(false);
 
         switch(curState)
         {
         //Default (setpoint reached)
         case KickState.Default:
                 {
                     //Do not run the motors
                     winch.set(false);
                     release.set(false);
 
                     //If you havent reached the setpoint yet
                     if(setpoint > encoder)
                     {
                         //Loading
                         return KickState.Loading;
                     }
                     //If the trigger is pressed and there is a ball or we are force kicking, and if we have waited long enough since the last kick
                     if(trigger && (ballReady || forceKick) && (Timer.getFPGATimestamp() - KICK_MIN_DELAY) > lastKickTime)
                     {
                         //Reset lastKickTime to the current time
                         lastKickTime = Timer.getFPGATimestamp();
 
                         //Kicking
                         return KickState.Kicking;
                     }
                 }
                 break;
         //Loading (havent reached setpoint)
         case KickState.Loading:
                 {
                     //Run the winch, not the sea shell
                     winch.set(true);
                     release.set(false);
 
                     //If we have reached our setpoint
                     if(encoder >= setpoint)
                     {
                         //Go back to default state
                         return KickState.Default;
                     }
                 }
                 break;
         //Kicking (release kicker)
         case KickState.Kicking:
                 {
                     //Dont run winch, run release
                     winch.set(false);
                     release.set(true);
 
                     //If weve delayed long enough and joystick trigger is not pressed
                     if(Timer.getFPGATimestamp() - KICK_WINCH_DELAY > lastKickTime && !trigger)
                     {
                         //Reset the encoder to our initial position
                         encKicker.reset();
 
                         //Autonomous balls left in the zone
                         --ballsLeft;
 
                         //kickerSetpoint -= MAX_KICKER_ENCODER_COUNTS / 20;
 
                         //Loading
                         return KickState.Loading;
                     }
                 }
                 break;
         default:
                 return KickState.Default;
         }
         return curState;
     }
 
     //Trigger state machine (controls sea shell)
     double runTriggerStateMachine(boolean trigger, boolean sensor) {
         //Motor speed constants
         final double MOTOR_ON = 0.75;
         final double MOTOR_OFF = 0.0;
 
         //Motor is initially off
         double motorSpeed = MOTOR_OFF;
 
         switch(currentTriggerState)
         {
         //Engaged (light sensor is on the tape)
         case TriggerState.Engaged:
             {
                 //Stop motor
                 motorSpeed = MOTOR_OFF;
 
                 if(trigger)
                 {
                     //Start releasing
                     currentTriggerState = TriggerState.Releasing;
                 }
             }
             break;
         //Releasing
         case TriggerState.Releasing:
             {
                 //Run motor
                 motorSpeed = MOTOR_ON;
 
                 if(!sensor)
                 {
                     //Reached end of tape
                     currentTriggerState = TriggerState.Released;
                 }
             }
             break;
         //Released
         case TriggerState.Released:
             {
                 //Run motor
                 motorSpeed = MOTOR_ON;
 
                 if(sensor)
                 {
                     //Tape reached again
                     currentTriggerState = TriggerState.Engaged;
                 }
             }
             break;
         //Unknown
         case TriggerState.Unknown:
         default:
             //Stop motor
             motorSpeed = MOTOR_OFF;
 
             //If it sees the tape, it is engaged, otherwise it is released
             currentTriggerState = sensor ? TriggerState.Engaged : TriggerState.Released;
         }
 
         return motorSpeed;
     }
 
     //Runs continuously during teleoperated period
     public void teleopContinuous() {
         getWatchdog().feed();
 
         //Operator trigger
         boolean curTrigger = stickTwo.getRawButton(KICK_BUTTON);
 
         //Booleans to drive winch and sea shell, changed by call to kicker state machine
         BooleanHolder winch = new BooleanHolder(false);
         BooleanHolder release = new BooleanHolder(false);
 
         //Forcekick if trigger and hanger down button pressed
         boolean forceKick = stickTwo.getRawButton(KICK_BUTTON) && stickTwo.getRawButton(HANGER_DOWN_BUTTON);
 
         //Release kicker if trigger is pressed of if you need to forcekick
         boolean trigger = curTrigger || forceKick;
 
         //Kicker setpoint (only if we are going to release
         kickerSetpoint = trigger ? 0 : (int)(((stickTwo.getRawAxis(1) + 1.0) / 2.0) * MAX_KICKER_ENCODER_COUNTS);
 
         //Setpoint cannot be less than 0
         kickerSetpoint = kickerSetpoint < 0 ? 0 : kickerSetpoint;
 
         //Kicker state machine (winch and release are changed by this function [Java version of pointers])
         currentKickState = runKickStateMachine(currentKickState, kickerSetpoint, -encKicker.getRaw(), trigger, ballSensor.get(), forceKick, winch, release);
         
         //Run the winch based on how the kicker state machine wants it to
         vicKicker.set((winch.get() && !kickerKillSwitch.get()) ? KICKER_POWER : 0.0);
 
         //Releasing this run through and didnt the last run through
         final boolean releaseTriggered = release.get() && !prevRelease;
 
         //Trigger state machine
         double vicReleaseSpeed = runTriggerStateMachine(releaseTriggered, releaseSensor.get());
 
         //Update prevRelease to be the current value of release
         prevRelease = release.get();
 
         //Run the sea shell with the speed the state machine wants
         vicRelease.set(-vicReleaseSpeed);
 
         //Reverse button is pressed and the reverse button was released in the last run
         if(stickOne.getRawButton(REVERSE_BUTTON) && reverseButtonReleased)
         {
             //It is no longer released
             reverseButtonReleased = false;
 
             //Reverse driving
             driveReverse = !driveReverse;
         }
         //Reverse button is released
         else if(!stickOne.getRawButton(REVERSE_BUTTON))
         {
             reverseButtonReleased = true;
         }
 
         //mecanumDrive(stickOne.GetRawAxis(2), stickOne.GetRawAxis(1), stickOne.GetRawAxis(3), stickOne.GetRawButton(SLOW_BUTTON), stickOne.GetRawButton(TRACK_TARGET_BUTTON));
 
        double speedFrontLeft, speedBackLeft, speedFrontRight, speedBackRight;

        //Temporary mecanum drive calculations
        speedFrontLeft = (-stickOne.getRawAxis(2) / 3) + (stickOne.getRawAxis(1) / 3) + (stickOne.getRawAxis(3) / 3);
        speedBackLeft = (-stickOne.getRawAxis(2) / 3) + (-stickOne.getRawAxis(1) / 3) + (stickOne.getRawAxis(3) / 3);
        speedFrontRight = (stickOne.getRawAxis(2) / 3) + (stickOne.getRawAxis(1) / 3) + (stickOne.getRawAxis(3) / 3);
        speedBackRight = (stickOne.getRawAxis(2) / 3) + (-stickOne.getRawAxis(1) / 3) + (stickOne.getRawAxis(3) / 3);

        //Reverse drive
        speedFrontLeft = driveReverse ? -speedFrontLeft : speedFrontLeft;
        speedBackLeft = driveReverse ? -speedBackLeft : speedBackLeft;
        speedFrontRight = driveReverse ? -speedFrontRight : speedFrontRight;
        speedBackRight = driveReverse ? -speedBackRight : speedBackRight;

        //Drive motors at calculated speed
        jagFrontLeft.set(speedFrontLeft);
        jagBackLeft.set(speedBackLeft);
        jagFrontRight.set(speedFrontRight);
        jagBackRight.set(speedBackRight);
 
         double rollerSpeed;
         //If roller in button is pressed then run the roller at 1.0, otherwise 0.0
         rollerSpeed = stickTwo.getRawButton(ROLLER_IN_BUTTON) ? 1.0 : 0.0;
         //If roller out button is pressed then run the roller at -1.0, otherwise the previous value of rollerSpeed
         rollerSpeed = stickTwo.getRawButton(ROLLER_OUT_BUTTON) ? -1.0 : rollerSpeed;
 
         //Run the roller
         vicRoller.set(rollerSpeed);
     }
     
 }
