 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.usfirst.frc3946.UltimateAscent.commands;
 
 import edu.wpi.first.wpilibj.Timer;
 import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
 import java.io.IOException;
 
 /**
  *
  * @author Gustave Michel
  */
 public class AutoAim extends CommandBase {
     private double currentTime = 0;
     private double previousCheckTime = 0;
     private double checkInterum = .4;
     private double previousConnectTime = 0;
     private double connectInterum = 1;
     
     private int center;
     private int distance;
     private int errorAccum = 0;
     
     public AutoAim() {
         // Use requires() here to declare subsystem dependencies
         // eg. requires(chassis);
         requires(driveTrain);
         requires(raspberryPi);
     }
 
     // Called just before this Command runs the first time
     protected void initialize() {
         if(!raspberryPi.getPi().isConnected()) {
             try {
                 raspberryPi.getPi().connect();
             } catch (IOException ex) {
                 System.out.println("AutoAimInit: Conntecting Failed!");
             }
         }
     }
 
     // Called repeatedly when this Command is scheduled to run
     protected void execute() {
         currentTime = Timer.getFPGATimestamp();
         
         if(raspberryPi.getPi().isConnected()) {
             if(currentTime - previousCheckTime > checkInterum) {
                 previousCheckTime = currentTime;
                 try {
                     String rawData = raspberryPi.getPi().getRawData(); //Get Raw String Input
                     if(rawData.length() < 1) {
                         return; //Check for No Data
                     }
                     String[] tokenData = raspberryPi.getPi().tokenizeData(rawData); //Tokenize Raw Input
                     if(tokenData.length < 1) {
                         return; //Check for No Data
                     }
                     if(tokenData[0].equals("n")) {
                         return; //Check if no Image Data was Returned
                     }
                     if(raspberryPi.getPi().isNumeric(tokenData[0])){ //Attempt to Parse first value into Int
                         center = Integer.parseInt(tokenData[0]); 
                     } else {
                         center = -999;
                     }
                     if(raspberryPi.getPi().isNumeric(tokenData[3])) { //Attempt to Parse third value into Int
                         distance = Integer.parseInt(tokenData[3]);
                     } else {
                         distance = -999;
                     }
                     SmartDashboard.putNumber("Offset", center); //Print Data to SmartDashboard
                     SmartDashboard.putNumber("Distance", distance);
                     
                     if(center >= 20) { //Turning
                         driveTrain.tankDrive(.2, 0);
                     } else if(center <= -20) {
                         driveTrain.tankDrive(0, .2);
                     } else {
                         driveTrain.tankDrive(0, 0);
                     }
                     
                     
                     if(distance >= 18) { //Distance
                         driveTrain.tankDrive(.2,.2);
                     } else if(distance <= 14) {
                         driveTrain.tankDrive(-.2,-.2);
                     }
                     
                     errorAccum = 1;
                 } catch (IOException ex) {
                     driveTrain.tankDrive(0, 0);
                     errorAccum++;
                     if(errorAccum >= 5) { //Disconnect after 5 consecutive errors
                         try {
                             raspberryPi.getPi().disconnect();
                         } catch (IOException ex1) {
                             System.out.println("AutoAimExec: Disconnecting Failed!");
                         }
                     }
                 }
             }
         } else {
             
             driveTrain.tankDrive(0, 0);
             if(currentTime - previousConnectTime > connectInterum) {
                 previousConnectTime = currentTime;
                 try {
                     raspberryPi.getPi().connect();
                 } catch (IOException ex) {
                     System.out.println("AutoAimExec: Connecting Failed?");
                 }
             }
         }
     }
 
     // Make this return true when this Command no longer needs to run execute()
     protected boolean isFinished() {
         return false;
     }
 
     // Called once after isFinished returns true
     protected void end() {
        driveTrain.tankDrive(0, 0);
     }
 
     // Called when another command which requires one or more of the same
     // subsystems is scheduled to run
     protected void interrupted() {
        driveTrain.tankDrive(0, 0);
     }
 }
