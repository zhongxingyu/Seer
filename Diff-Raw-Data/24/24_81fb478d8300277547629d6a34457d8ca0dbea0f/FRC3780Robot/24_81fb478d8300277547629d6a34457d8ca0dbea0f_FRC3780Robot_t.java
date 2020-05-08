 /* All FRC 3780 code follows the following License:
  * Copyright 2013 FRC 3780 (Brian Fogarty, Oliver Graff) Licensed under the
  * Educational Community License, Version 2.0 (the "License"); you may not use 
  * this file except in compliance with the License. You may obtain a copy of the
  * License at 
  *      http://www.osedu.org/licenses/ECL-2.0 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and 
  * limitations under the License.
  */
 
 /*----------------------------------------------------------------------------*/
 /* Copyright (c) FIRST 2008. All Rights Reserved.                             */
 /* Open Source Software - may be modified and shared by FRC teams. The code   */
 /* must be accompanied by the FIRST BSD license file in the root directory of */
 /* the project.                                                               */
 /*----------------------------------------------------------------------------*/
 
 package org.usfirst.frc3780.robot;
 
 
 import edu.wpi.first.wpilibj.*;
 import edu.wpi.first.wpilibj.command.Scheduler;
 import edu.wpi.first.wpilibj.livewindow.LiveWindow;
import org.usfirst.frc3780.commands.JesusCrucify;
 import org.usfirst.frc3780.subsystems.*;
 
 /**
  * The VM is configured to automatically run this class, and to call the
  * functions corresponding to each mode, as described in the IterativeRobot
  * documentation. If you change the name of this class or the package after
  * creating this project, you must also update the manifest file in the resource
  * directory.
  */
 public class FRC3780Robot extends IterativeRobot {
 
     private Compressor _compressor;
     //  Command autonomousCommand;
     
     /**
      * This function is run when the robot is first started up and should be
      * used for any initialization code.
      */
     public void robotInit() {
         
         //  TODO: instantiate the command used for the autonomous period
         //  autonomousCommand = new ExampleCommand();
 
         //  Compressor code.
         _compressor = new Compressor(RobotMap.compressorPressureSwitchChannel, RobotMap.compressorRelayChannel);
         _compressor.start();
         
         //  Initialize subsystems.
         Chassis.getInstance();
         Jesus.getInstance();
         WallE.getInstance();
         OI.getInstance();
         
        Scheduler.getInstance().add(new JesusCrucify());
        Scheduler.getInstance().run();
        
     }
 
     /**
      * This function runs when autonomous begins.
      */
     public void autonomousInit() {
         // TODO: schedule the autonomous command
         // autonomousCommand.start();
     }
 
     /**
      * This function is called periodically during autonomous
      */
     public void autonomousPeriodic() {
         Scheduler.getInstance().run();
     }
 
     /**
      * This function runs when teleop begins.
      */
     public void teleopInit() {
         // TODO: Make sure autonomous stops running.
         // autonomousCommand.cancel();
     }
 
     /**
      * This function is called periodically during operator control
      */
     public void teleopPeriodic() {
         Scheduler.getInstance().run();
     }
     
     /**
      * This function is called periodically during test mode
      */
     public void testPeriodic() {
         LiveWindow.run();
     }
 }
