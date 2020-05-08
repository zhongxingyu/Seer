 /*----------------------------------------------------------------------------*/
 /* Copyright (c) FIRST 2008. All Rights Reserved.                             */
 /* Open Source Software - may be modified and shared by FRC teams. The code   */
 /* must be accompanied by the FIRST BSD license file in the root directory of */
 /* the project.                                                               */
 /*----------------------------------------------------------------------------*/
 
 package edu.wpi.first.wpilibj.templates;
 
 
 import edu.wpi.first.wpilibj.DriverStationLCD;
 import edu.wpi.first.wpilibj.IterativeRobot;
 import edu.wpi.first.wpilibj.Joystick;
 import edu.wpi.first.wpilibj.Encoder;
 import edu.wpi.first.wpilibj.Watchdog;
 import edu.wpi.first.wpilibj.camera.AxisCamera;
 
 /**
  * The VM is configured to automatically run this class, and to call the
  * functions corresponding to each mode, as described in the IterativeRobot
  * documentation. If you change the name of this class or the package after
  * creating this project, you must also update the manifest file in the resource
  * directory.
  */
 public class TPARobot extends IterativeRobot {
 
     AxisCamera theAxisCamera;                                     // The camera
     DriverStationLCD theDriverStationLCD;                       // Object representing the driver station
     TPARobotDriver theRobotDrive;                               // Robot Drive Variable
     Joystick theRightStick;                                     // Right joystick
     Joystick theLeftStick;                                      // Left joystick
     Encoder theRightEncoder;                                    // Right E4P Motion Sensor
     Encoder theLeftEncoder;                                     // Left E4P Motion Sensor
     static final boolean DEBUG = true;                          // Debug Trigger
     static final double STOP_VALUE = 0.1;                       // Value drive motors are sent when stopping
     
     // Drive mode selection
     int theDriveMode;                                           // The actual drive mode that is currently selected.
     static final int UNINITIALIZED_DRIVE = 0;                   // Value when no drive mode is selected
     static final int ARCADE_DRIVE = 1;                          // Value when arcade mode is selected 
     static final int TANK_DRIVE = 2;                            // Value when tank drive is selected
     public double theMaxSpeed;                                  // Multiplier for speed, determined by Z-Axis on left stick
 
 
     /*--------------------------------------------------------------------------*/
     /*
     * Author:  Marissa Beene
     * Date:    1/13/2012
     * Purpose: Robot Initialization Function. This function is run once when the
     *          robot is first started up and should be used for any initialization
     *          code.
     * Inputs:  None
     * Outputs: None
     */
     public void robotInit() {
         
         // Create a drive system using standard right/left robot drive on PWMS 1 and 2
         theRobotDrive = new TPARobotDriver(1,2);
         if (DEBUG == true){
             System.out.println("TheRobotDrive constructed successfully");
         }
         
         // Define joysticks being used at USB port #1 and USB port #2 on the Drivers Station
 	theRightStick = new Joystick(1);
 	theLeftStick = new Joystick(2);
         if (DEBUG == true){
            System.out.println("The Joysticks constructed successfully"); 
         }
         
         // Defines two E4P Motion Sensors at ports 1,2,3, and 4
         theLeftEncoder = new Encoder(1,2);
         theRightEncoder = new Encoder(3,4);
         if (DEBUG == true){
             System.out.println("The Encoders constructed successfully");
         }
         
         //Initialize the DriverStationLCD
         theDriverStationLCD = DriverStationLCD.getInstance();
         if (DEBUG) {
             System.out.println("DriverStationLCD initialized");
         }
         
         //Initialize the AxisCamera
         theAxisCamera = AxisCamera.getInstance();
         if (DEBUG) {
             System.out.println("AxisCamer initialized");
         }
         
         // Initialize the Drive Mode to Uninitialized
         theDriveMode = UNINITIALIZED_DRIVE;
         
         if (DEBUG == true){
         System.out.println("RobotInit() completed.\n");
         }
     }
     /*--------------------------------------------------------------------------*/
     
     
     /*--------------------------------------------------------------------------*/
     /*
     * Author:  Marissa Beene
     * Date:    1/13/2012
     * Purpose: TPARobot Constructor
     * Inputs:  None
     * Outputs: None
     * 11/07/2011 - jd - Added comment to test github merge.
     */
     public void TPARobot(){
     }
     /*--------------------------------------------------------------------------*/
     
 
     /*--------------------------------------------------------------------------*/
     /*
      * Author:  Team
      * Date:    1/13/2012
      * Purpose: This function is called periodically during autonomous mode. Allows
      *          for autonomous operation of the robot.
      * Inputs:  None
      * Outputs: None
      */
 
     public void autonomousPeriodic() {
         Watchdog.getInstance().feed();
         theDriverStationLCD.println(DriverStationLCD.Line.kMain6, 1, "Autonomous Mode Called");
        theDriverStationLCD.updateLCD();    //Displays a message whenever in Autonomous Mode.
     }
     /*--------------------------------------------------------------------------*/
     
 
     /*--------------------------------------------------------------------------*/
     /*
      * Author:  Team
      * Date:    1/13/2012
      * Purpose: This function is called periodically during autonomous mode. Allows
      *          for autonomous operation of the robot.
      * Inputs:  None
      * Outputs: None
      */
     public void teleopPeriodic() {
        
         // Feed the user watchdog at every period when in autonomous
         Watchdog.getInstance().feed();
         if (DEBUG == true){
             System.out.println("Teleop Periodic Watchdog Fed");
         }
         
         // Determine whether arcade drive or tank drive is in use
         setDriveMode();
         if (DEBUG == true){
             System.out.println("setDriveMode called");
         }
         
         // Brake the robot when no signal is sent
         brakeOnNeutral();
         if (DEBUG == true){
             System.out.println("brakeOnNeutral called");
         }
         
         //Set the multiplier for max speed
         setMaxSpeed();
         if (DEBUG == true) {
             System.out.println("setMaxSpeed called");
         }
         
         //Get the image from the Axis Camera
         getCameraImage();
         if(DEBUG) {
             System.out.println("getCameraImage called");
         }
         
     }
     /*--------------------------------------------------------------------------*/
     
     
     /*--------------------------------------------------------------------------*/
     /*
      * Author:  Daniel Hughes
      * Date:    1/13/2012 (Daniel Hughes)
      * Purpose: To determine the appropriate drive mode based on the "Z" wheel on 
      *          the right joystick. If the "Z" wheel is up (negative) Robot is put
      *          in arcade mode. Otherwise, robot is put in tank mode.
      * Inputs:  None
      * Outputs: None
      */
         
     public void setDriveMode(){
         
         // determine if tank or arcade mode, based upon position of "Z" wheel on kit joystick
         if (theLeftStick.getZ() <= 0) {    // Logitech Attack3 has z-polarity reversed; up is negative
             // use arcade drive
             if (DEBUG == true){
                 System.out.println("theRightStick.getZ called" );
             }
             theRobotDrive.arcadeDrive(theRightStick, false);	// drive with arcade style (use right stick)
             if (theDriveMode != ARCADE_DRIVE) {
                 // if newly entered arcade drive, print out a message
                 System.out.println("Arcade Drive\n");
                 theDriveMode = ARCADE_DRIVE;
             }
         } else {
             // use tank drive
             theRobotDrive.tankDrive(theLeftStick, theRightStick);	// drive with tank style
             if (theDriveMode != TANK_DRIVE) {
                 // if newly entered tank drive, print out a message
                 System.out.println("Tank Drive\n");
                 theDriveMode = TANK_DRIVE;
             }
         }
     }
     /*--------------------------------------------------------------------------*/
     
     
     /*--------------------------------------------------------------------------*/
     /*
      * Author:  Gennaro De Luca
      * Date:    11/26/2011 (Gennaro De Luca)
      * Purpose: To determine the speed multiplier based on the "Z" wheel on 
      *          the left joystick. If the "Z" wheel is up (negative), the multiplier remains at 1.
      *          Otherwise, the multiplier is set to one-half.
      * Inputs:  None
      * Outputs: None
      */    
     public void setMaxSpeed(){
         
         
         if (theRightStick.getZ() <= 0) {   // Logitech Attack3 has z-polarity reversed; up is negative
             theMaxSpeed = 1;               //set the multiplier to default value of 1
             if (DEBUG == true){
                 System.out.println("theLeftStick.getZ called");
             }
         }
         else if (theRightStick.getZ() > 0) {
             theMaxSpeed = 0.5;             //set the multiplier to half default, 0.5
             if (DEBUG == true) {
                 System.out.println("theLeftStick.getZ called");
             }
         }
         theRobotDrive.setMaxSpeed(theMaxSpeed); //tests the multiplier
     }
     /*--------------------------------------------------------------------------*/
     
     
     /*--------------------------------------------------------------------------*/
     /*
      * Author:  Marissa Beene
      * Date:    10/30/2011 (Marissa Beene)
      * Purpose: To use the motors to brake the robot. Takes the speed from the 
      *          each motor and sends the reverse signal back.
      * Inputs:  Double aSpeedRight - the speed of the right motor
      *          Double aSpeedLeft - the speed of the left motor
      * Outputs: None
      */
     
     public void brake(double aSpeedLeft, double aSpeedRight){
         theRobotDrive.tankDrive(-aSpeedLeft, -aSpeedRight); //drive the robot at opposite values
         }
     /*--------------------------------------------------------------------------*/
     
     
     /*--------------------------------------------------------------------------*/
     
     /*
      * Author:  Marissa Beene
      * Date:    10/30/11
      * Purpose: To determine if there is no signal. First determines the drive
      *          mode and discards the left stick if in arcade mode. If there is 
      *          no signal to the drive train, it will return true, otherwise it 
      *          will return false
      * Inputs:  Joystick aRightStick  - the right joystick
      *          Joystick aLeftStick - the left joystick
      * Outputs: Boolean - returns true if the drive train is not sent a signal
      */
     
     public boolean isNeutral(Joystick aRightStick, Joystick aLeftStick){
         if (DEBUG == true){
             System.out.println("isNeutral Called");
         }
         if(theDriveMode == ARCADE_DRIVE){ //if arcade drive
             if (DEBUG == true){
                 System.out.println("Arcade Drive Recognized by isNeutral");
             }
             if(aRightStick.getY() == 0 && aRightStick.getX() == 0){ //there is no input
                 return true;
             }
             else{
                 return false;
             }
         }
         else if(theDriveMode == TANK_DRIVE){ //if tank drive
             if (DEBUG == true){
                 System.out.println("Tank Drive Recognized by isNeutral");
             }
             if(aRightStick.getY() == 0 && aLeftStick.getY() == 0){
                 return true;
             }
             else{
                 return false;
             }
         }
         else{
             return false;
         } 
     }
     /*--------------------------------------------------------------------------*/
     
     
     /*--------------------------------------------------------------------------*/
     /*
      * Author:  Marissa Beene
      * Date:    11/5/11
      * Purpose: To brake the robot if there is no signal in arcade mode. If the 
      *          wheel is not considered stopped, it will read the direction that the wheel
      *          is turning and send it the stop value in the other direction.
      * Inputs:  None
      * Outputs: None
      */
     
     public void brakeOnNeutral(){
         
         double theLeftSpeedOutput = 0; // value the left motor will be sent
         double theRightSpeedOutput = 0; // value the right motor will be sent
         
         if (DEBUG == true){
             System.out.println("brakeOnNeutral called");
         }
         
         if(isNeutral(theRightStick, theLeftStick)){ // if no signal is sent to the robot
             
             // get the direction of the left motor and store the stop value vector to theLeftSpeedOutput
             if(!theLeftEncoder.getStopped()){
                 if(theLeftEncoder.getDirection()){
                     theLeftSpeedOutput = STOP_VALUE;
                 }
                 else{
                     theLeftSpeedOutput = -STOP_VALUE;
                 }
             }
             
             // get the direction of the right motor and store a stop value vector to theRightSpeedOutput
             if(!theRightEncoder.getStopped()){
                 if(theRightEncoder.getDirection()){
                     theRightSpeedOutput = STOP_VALUE;
                 }
                 else{
                     theRightSpeedOutput = -STOP_VALUE;
                 }
             }
         // brake the robot at the value of the stop value
         brake(theLeftSpeedOutput, theRightSpeedOutput);
         }
     }
     /*--------------------------------------------------------------------------*/
     
     /*--------------------------------------------------------------------------*/
     /*
      * Author:  Gennaro De Luca
      * Date:    11/26/2011 (Gennaro De Luca)
      * Purpose: Do AxisCamera stuff
      * Inputs:  None
      * Outputs: None
      */  
      
     public void getCameraImage() {
         theAxisCamera.writeResolution(AxisCamera.ResolutionT.k160x120);
         theAxisCamera.writeBrightness(0);
         DriverStationLCD.getInstance().updateLCD();
     }
     
      /*--------------------------------------------------------------------------*/
 }
