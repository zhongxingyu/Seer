 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.aisd.martin.frc2013;
 
 import edu.wpi.first.wpilibj.SimpleRobot;
 
 /**
  *
  * @author Neil
  *
  * The VM is configured to automatically run this class, and to call the
  * functions corresponding to each mode, as described in the SimpleRobot
  * documentation. If you change the name of this class or the package after
  * creating this project, you must also update the manifest file in the resource
  * directory.
  */
 public class Team3676Robot extends SimpleRobot {
     
     /**
      * Robot's instance variables
      */
     
     /**
      * This is the robot's initialization
      * Instantiates any objects that will be used by the robot here
      */
     public void robotInit(){
 		Subsystems2013.init();
 		Subsystems2013.pneumatics.compressor.start();
         
     }
 	
 	/*
 	 * This function is called when the robot is disabled
 	 */
 	public void disabled(){
 		
 	}
     
     /**
      * This function is called once each time the robot enters autonomous mode.
      */
     public void autonomous() {
         while(isAutonomous() && isEnabled()){
 			//TODO: Once we need a routine we will put it here
 		}
     }
 
     /**
      * This function is called once each time the robot enters operator control.
      */
     public void operatorControl() {
            while(isOperatorControl() && isEnabled()){
 		Subsystems2013.driveTrain.tick(Subsystems2013.controller1.getLeftAxisButton());
 		Subsystems2013.pneumatics.tick();
                        
 		Subsystems2013.shooter.think(Subsystems2013.controller1.getAButton(), Subsystems2013.controller1.getBButton(), 
 				Subsystems2013.controller1.getXButton(), Subsystems2013.controller1.getYButton(),
 				Subsystems2013.controller1.getLeftBumper());
 		Subsystems2013.climber.think(Subsystems2013.controller1.getSelectButton());
                 Subsystems2013.camera.think(Subsystems2013.controller1.getRightYAxis(false), Subsystems2013.controller1.getRightXAxis(false));
            }
    }
 
 }
 
