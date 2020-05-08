 /*----------------------------------------------------------------------------*/
 /* Copyright (c) FIRST 2008. All Rights Reserved.                             */
 /* Open Source Software - may be modified and shared by FRC teams. The code   */
 /* must be accompanied by the FIRST BSD license file in the root directory of */
 /* the project.                                                               */
 /*----------------------------------------------------------------------------*/
 
 package com.edinarobotics.zephyr;
 
 
 import com.edinarobotics.utils.autonomous.AutonomousManager;
 import com.edinarobotics.utils.autonomous.AutonomousStep;
 import com.edinarobotics.utils.gamepad.FilterSet;
 import com.edinarobotics.utils.gamepad.Gamepad;
 import com.edinarobotics.utils.gamepad.GamepadResult;
 import com.edinarobotics.utils.gamepad.ToggleHelper;
 import com.edinarobotics.utils.gamepad.filters.DeadzoneFilter;
 import com.edinarobotics.utils.gamepad.filters.QuarticScalingFilter;
 import com.edinarobotics.utils.gamepad.filters.ScalingFilter;
 import edu.wpi.first.wpilibj.DriverStationLCD;
 import edu.wpi.first.wpilibj.Relay;
 import edu.wpi.first.wpilibj.SimpleRobot;
 import com.edinarobotics.utils.sensors.FIRFilter;
 import com.edinarobotics.zephyr.autonomous.AutonomousStepFactory;
 import com.edinarobotics.zephyr.autonomous.IdleStopStep;
 import com.edinarobotics.zephyr.autonomous.IdleWaitStep;
 import com.edinarobotics.zephyr.parts.CollectorComponents;
 import com.edinarobotics.zephyr.parts.CypressComponents;
 import com.edinarobotics.zephyr.parts.ShooterComponents;
 import edu.wpi.first.wpilibj.*;
 import edu.wpi.first.wpilibj.DriverStationEnhancedIO.EnhancedIOException;
 
 /**
  * The VM is configured to automatically run this class, and to call the
  * functions corresponding to each mode, as described in the SimpleRobot
  * documentation. If you change the name of this class or the package after
  * creating this project, you must also update the manifest file in the resource
  * directory.
  */
 public class Zephyr extends SimpleRobot {
     //Driving Variables
     public double leftDrive = 0;
     public double rightDrive = 0;
     private final double ONE_STICK_MULTIPLIER = 0.5;
    
     //Shooter Variables
     public double shooterSpeed = 0;
     public boolean ballLoaderUp = false;
     public double shooterRotateSpeed = 0;
     private final double SHOOTER_LARGE_SPEED_STEP = 50;
     private final double SHOOTER_SMALL_SPEED_STEP = 10;
     private double lastManualSpeed = 0;
     public final double KEY_SHOOTER_SPEED_RPS = 46;
     
     //Sensor Variables
      private FIRFilter firFiltering = FIRFilter.autoWeightedFilter(20);
      //Camera Variables
      public double cameraSetX;
      public double cameraSetY;
      private double CAMERA_STEP = .005;
      //Collector Variables
      public double collectorLift = 0;
      public int collectorSpin = CollectorComponents.COLLECTOR_STOP;
      public int convMove = 0;
      private final double COLLECTOR_LIFT_DOWN = -0.25;
      private final double COLLECTOR_LIFT_UP = 0.9;
      private final double COLLECTOR_LIFT_STOP = 0;
      private final double COLLECTOR_LIFT_DOWN_FAST = -1;
      public boolean shifters = false;
      //Problem variables
      public static boolean exceptionProblem = false;
      public static boolean genericProblem = false;
      
      /**
       * This function initializes the robot by constructing objects for each
       * of its components and starting the compressor.
       */
      protected void robotInit(){
          Components.getInstance();
      }
      
      /**
       * When we are disabled, stop the robot and stop the watchdog.
       */
      protected void disabled(){
          stop();
          getWatchdog().setEnabled(false); //Disable the watchdog when disabled.
      }
      
     /**
      * This function is called once each time the robot enters autonomous mode.
      */
     public void autonomous() {
         stop();
         //Cypress switch constants
         final int POSITION_LEFT_SWITCH = 6;
         final int POSITION_RIGHT_SWITCH = 4;
         final int COLLECT_SWITCH = 2;
         final int SHOOTING_DELAY_1 = 5;
         final int SHOOTING_DELAY_2 = 3;
         
         //Autonomous constants
         final int NO_AUTONOMOUS = 0;
         final int KEY_LEFT = 1;
         final int KEY_RIGHT = 2;
         final int KEY_MIDDLE = 3;
         final int DELAY_MULTIPLIER = 1;
         Components parts = Components.getInstance();
         CypressComponents cypress = parts.cypress;
         
         //Autonomous program constants
         final double LEFT_KEY_SHOOTER_SPEED = 51.5;
         final double RIGHT_KEY_SHOOTER_SPEED = 51.5;
         final double MIDDLE_KEY_SHOOTER_SPEED = KEY_SHOOTER_SPEED_RPS;
         
         //Autonomous config values
         int shootingDelayValue = 1;
         boolean driveToCollect = false;
         int keyPosition = KEY_MIDDLE;
         
         //Determine shooting delay value
         shootingDelayValue = (((cypress.getDigital(SHOOTING_DELAY_2)?1:0)<<1)+
                              (cypress.getDigital(SHOOTING_DELAY_1)?1:0))*
                              DELAY_MULTIPLIER;
         
         //Determine if we should collect
         driveToCollect = cypress.getDigital(COLLECT_SWITCH);
         
         //Determine position on the key
         keyPosition = ((cypress.getDigital(POSITION_RIGHT_SWITCH)?1:0)<<1)+
                       (cypress.getDigital(POSITION_LEFT_SWITCH)?1:0);
         
         //Create autonomous program
         AutonomousStepFactory stepFactory = new AutonomousStepFactory(this);
         //Create our pre-shooting delay step
         AutonomousStep shootDelayStep = new IdleWaitStep(shootingDelayValue, this);
         
         //Create out shooting step
         AutonomousStep shootStep;
         switch(keyPosition){
             case KEY_LEFT: shootStep = stepFactory.getShooterFireStep(LEFT_KEY_SHOOTER_SPEED, 2); break;
             case KEY_RIGHT: shootStep = stepFactory.getShooterFireStep(RIGHT_KEY_SHOOTER_SPEED, 2); break;
             case KEY_MIDDLE: shootStep = stepFactory.getShooterFireStep(MIDDLE_KEY_SHOOTER_SPEED, 2); break;
             default: shootStep = new IdleWaitStep(0, this);
         }
         AutonomousStep[] steps = new AutonomousStep[3];
         steps[0] = shootDelayStep;
         steps[1] = shootStep;
         steps[2] = new IdleStopStep(this);
         AutonomousManager manager = new AutonomousManager(steps, this);
         getWatchdog().setEnabled(true); //Start the watchdog for autonomous
         manager.start();
         stop();
     }
 
     /**
      * This function is called once each time the robot enters operator control.
      */
     public void operatorControl() 
     {
         stop();
         //Add 2 to componensate for the fact we are not at the very top of the key
         final double PRESET_RPS_SPEED = KEY_SHOOTER_SPEED_RPS+2;
         FilterSet driveFilters = new FilterSet();
         driveFilters.addFilter(new DeadzoneFilter(0.5));
         driveFilters.addFilter(new ScalingFilter());
         
         FilterSet shootFilters = new FilterSet();
         shootFilters.addFilter(new DeadzoneFilter(0.5));
         shootFilters.addFilter(new QuarticScalingFilter());
         
         // Gamepads
         Gamepad driveGamepad = new Gamepad(1);
         Gamepad shootGamepad = new Gamepad(2);
         
         // Initiate components
         Components components = Components.getInstance();
         ToggleHelper shifterHelper = new ToggleHelper();
         ToggleHelper button3 = new ToggleHelper();
        ToggleHelper dPadXToggler = new ToggleHelper();
         getWatchdog().setEnabled(true); //Start the watchdog for teleop
         while(this.isOperatorControl()&&this.isEnabled())
         {
             //Gamepad 1*********************************************************
             //Control collector brushes
             if(driveGamepad.getRawButton(Gamepad.LEFT_BUMPER)){
                 collectorSpin = CollectorComponents.COLLECTOR_IN;
             }
             else if(driveGamepad.getRawButton(Gamepad.BUTTON_9)){
                 collectorSpin = CollectorComponents.COLLECTOR_OUT;
             }
             else{
                 collectorSpin = CollectorComponents.COLLECTOR_STOP;
             }
             //
             if(shifterHelper.isToggled(driveGamepad.getRawButton(Gamepad.LEFT_TRIGGER))){
                 shifters = false;
             }
             //Control collector deployment
             if(driveGamepad.getRawButton(Gamepad.RIGHT_TRIGGER)){
                 collectorLift = COLLECTOR_LIFT_DOWN;
             }
             else if(driveGamepad.getRawButton(Gamepad.BUTTON_4)){
                 collectorLift = COLLECTOR_LIFT_DOWN_FAST;
             }
             else if(driveGamepad.getRawButton(Gamepad.RIGHT_BUMPER)){
                 collectorLift = COLLECTOR_LIFT_UP;
             }
             else{
                 collectorLift = COLLECTOR_LIFT_STOP;
             }
             //Driving with joysticks
             if(Math.abs(driveGamepad.getDPadY()) <= 0.9){
                 //D-Pad not in use, normal joystick drive.
                 GamepadResult joystick = driveFilters.filter(driveGamepad.getJoysticks());
                 leftDrive = joystick.getLeftY();
                 rightDrive = joystick.getRightY();
             }
             else{
                 //D-Pad is in use, use one-joystick drive
                 double oneStickValue = driveGamepad.getDPadY() * ONE_STICK_MULTIPLIER;
                 leftDrive = oneStickValue;
                 rightDrive = oneStickValue;
             }
             
             //Gamepad 2*********************************************************
             //Control firing piston
             ballLoaderUp = shootGamepad.getRawButton(Gamepad.LEFT_BUMPER);
             //Shooter speed control
             // If the right bumper on the shootGamepad is pushed, speed up the
             // shooter
             if(shootGamepad.getRawButton(Gamepad.RIGHT_BUMPER))
             {
                 //Step speed of shooter up.
                 shooterSpeed += SHOOTER_LARGE_SPEED_STEP;
                 
                 // Limit the speed of the shooter to not exceed -1
                 if(shooterSpeed >= ShooterComponents.MAX_SHOOTER_SPEED)
                 {
                     shooterSpeed = ShooterComponents.MAX_SHOOTER_SPEED;
                 }
                 
                 // Store the speed of the shooter to a second variable
                 lastManualSpeed = shooterSpeed;
             }
             
             // If the left bumper on the shootGamepad is pushed, slow down the
             // shooter
             else if(shootGamepad.getRawButton(Gamepad.RIGHT_TRIGGER))
             {
                 //Step speed of shooter down.
                 shooterSpeed -= SHOOTER_LARGE_SPEED_STEP;
                 
                 // Limit the speed of the shooter to not go past 0
                 if(shooterSpeed <= ShooterComponents.MIN_SHOOTER_SPEED)
                 {
                     shooterSpeed = ShooterComponents.MIN_SHOOTER_SPEED;
                 }
                 
                 // Store the speed of the shooter to a second variable
                 lastManualSpeed = shooterSpeed;
             }
            else if(shootGamepad.getDPadX() == 1&&dPadXToggler.isToggled(true)){
                shooterSpeed+=SHOOTER_SMALL_SPEED_STEP;//increment shooter speed
                //if it exceeds bounds, set it to the maxiumum speed
                 if(shooterSpeed>=ShooterComponents.MAX_SHOOTER_SPEED){
                     shooterSpeed = ShooterComponents.MAX_SHOOTER_SPEED;
                 }
                //Store the shooters speed
                lastManualSpeed = shooterSpeed;
             }
            else if(shootGamepad.getDPadX() == -1&&dPadXToggler.isToggled(true)){
                shooterSpeed-=SHOOTER_SMALL_SPEED_STEP;//decrement shooter speed
                //if shooterSpeed is below minimum set it to the min speed
                 if(shooterSpeed>=ShooterComponents.MIN_SHOOTER_SPEED){
                     shooterSpeed = ShooterComponents.MIN_SHOOTER_SPEED;
                 }
                //store the speed
                lastManualSpeed = shooterSpeed;
            }
            if(shootGamepad.getDPadX() == 0){
                dPadXToggler.isToggled(false); //update the dPadX Toggler
             }
             else if(shootGamepad.getRawButton(Gamepad.BUTTON_1)){
                 shooterSpeed = ShooterComponents.MAX_SHOOTER_SPEED;
             }
             else if(shootGamepad.getRawButton(Gamepad.BUTTON_2)){
                 shooterSpeed = ShooterComponents.MIN_SHOOTER_SPEED;
             }
             else if(shootGamepad.getRawButton(Gamepad.BUTTON_3)){
                 shooterSpeed = 0.5*ShooterComponents.MAX_SHOOTER_SPEED;
             }
             else if(shootGamepad.getRawButton(Gamepad.BUTTON_4)){
                 shooterSpeed = lastManualSpeed;
             }
             else if(shootGamepad.getRawButton(Gamepad.BUTTON_10)){
                shooterSpeed = PRESET_RPS_SPEED; //50 RPS estimate button
             }
             shooterRotateSpeed = shootFilters.filter(shootGamepad.getJoysticks()).getRightX();
             
             if(shootGamepad.getRawButton(Gamepad.BUTTON_9)){
                 convMove = CollectorComponents.CONVEYOR_DOWN;
             }
             else{
                 convMove = (shootGamepad.getRawButton(Gamepad.LEFT_TRIGGER)?
                         CollectorComponents.CONVEYOR_UP:
                         CollectorComponents.CONVEYOR_STOP);
             }
             
             mechanismSet();
             Timer.delay(0.005);
         }
         stop();
     }
     
     /**
      * Updates all parts of the robot to avoid safety timeouts
      */
     public void mechanismSet(){
         getWatchdog().feed(); //Feed the watchdog
         //Driving Assignments
         Components robotParts = Components.getInstance();
         robotParts.drive.setDrivingSpeed(leftDrive, rightDrive);
         robotParts.drive.shift(shifters);
         //Shooter Assignments
         robotParts.shooter.setSpeed(shooterSpeed);
         robotParts.shooter.firePiston(ballLoaderUp);
         robotParts.shooter.rotate(shooterRotateSpeed);
         //Collector Assignments
         robotParts.collector.conveyorMove(convMove);
         robotParts.collector.collect(collectorSpin);
         robotParts.collector.lift(collectorLift);
         //Servo Assignments
         robotParts.cameraServoVertical.set(cameraSetY);
         //Sonar Processing
         String shooterPowerString = "Shooter Targ: "+shooterSpeed;
         String shooterActualString = "Shooter V: "+robotParts.shooter.getEncoderValue();
         int sonarVal = (int) robotParts.sonar.getFilteredValue();
         String sonarValue = "Sonar reads: " + String.valueOf((sonarVal/2)+5);
         String servoPositions = "Y-Axis Servo: "+robotParts.cameraServoVertical.get();
         String problemValue = "Prb: "+(exceptionProblem?"except! ":"")+(genericProblem?"prblm! ":"")+
                                        (getWatchdog().isAlive()?"":"wtchdg! ");
         robotParts.textOutput.println(DriverStationLCD.Line.kUser3,1, "                                                       ");
         robotParts.textOutput.println(DriverStationLCD.Line.kUser2, 1, shooterPowerString+"                                   ");
         robotParts.textOutput.println(DriverStationLCD.Line.kUser3, 1, shooterActualString+"                                  ");
         robotParts.textOutput.println(DriverStationLCD.Line.kUser4, 1, sonarValue+"                                           ");
         robotParts.textOutput.println(DriverStationLCD.Line.kUser5, 1, problemValue+"                                         ");
         robotParts.textOutput.updateLCD();
         
     }
     
     /**
      * Stop the robot from moving
      */
     public void stop(){
         leftDrive = 0;
         rightDrive = 0;
         shooterSpeed = 0;
         shooterRotateSpeed = 0;
         ballLoaderUp = false;
         collectorLift = CollectorComponents.COLLECTOR_LIFT_STOP;
         collectorSpin = CollectorComponents.COLLECTOR_STOP;
         convMove = CollectorComponents.CONVEYOR_STOP;
         mechanismSet();
     }
 }
