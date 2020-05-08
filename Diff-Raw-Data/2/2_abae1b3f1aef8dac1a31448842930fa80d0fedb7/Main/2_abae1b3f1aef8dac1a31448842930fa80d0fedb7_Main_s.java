 /*----------------------------------------------------------------------------*/
 /* Copyright (c) FIRST 2008. All Rights Reserved.                             */
 /* Open Source Software - may be modified and shared by FRC teams. The code   */
 /* must be accompanied by the FIRST BSD license file in the root directory of */
 /* the project.                                                               */
 /*----------------------------------------------------------------------------*/
 
 package core;
 import autonomous.Autonomous;
 import utilities.Robot;
 import utilities.Vars;
 import utilities.MyJoystick;
 import edu.wpi.first.wpilibj.IterativeRobot;
 
 /**
  * The VM is configured to automatically run this class, and to call the
  * functions corresponding to each mode, as described in the IterativeRobot
  * documentation. If you change the name of this class or the package after
  * creating this project, you must also update the manifest file in the resource
  * directory.
  */
 public class Main extends IterativeRobot 
 {
    final String sCodeVersion = "Welcome to Bubblefish 2.2 Series !";
     MyJoystick ps3Joy = new MyJoystick(1, Vars.iPs3Buttons);
     Robot bot = new Robot(ps3Joy);
     Compressor compressor = new Compressor();
     Autonomous autonomous = new Autonomous(ps3Joy, bot);
     
     /*
      * This function is run when the robot is first started up and should be
      * used for any initialization code.
      */
     public void robotInit() 
     {
         Vars.fnPutDashBoardStringBox(Vars.skCodeVersion, sCodeVersion);
     }
 
     // This function is called when we disable the robot.
     public void disabledInit()
     {
         // Resets the replay to false if it was true before
         autonomous.resetAutonomous(); 
     }
     
     public void disabledPeriodic()
     {
         compressor.run();
     }
     
     /* Called once in autonomous, tells autonomous which file to play based on
      * the value of "iFileType"
      */
     public void autonomousInit()
     {
         autonomous.setFileBasedOnDriverInput();
     }
     
     // This function is called periodically during autonomous
     public void autonomousPeriodic() 
     {
         autonomous.replay();
     }
 
     // This function is called periodically during operator control
     public void teleopPeriodic()
     {
         bot.run();
         compressor.run();
         autonomous.run();
     }
 }
