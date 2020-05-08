 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.slidellrobotics.reboundrumble.commands;
 
 import com.slidellrobotics.reboundrumble.subsystems.TrackingCamera;
 
 /**
  *
  * @author 10491477
  */
 public class FindDistance extends CommandBase {  
     double tgtHght = TrackingCamera.targetHeight;   //  Create a few necesarry local variables
     double tgtWdth = TrackingCamera.targetWidth;    //  for concise code and calcs.
     
     double tgtHghtFt;   //  Target Height in Feet
     double tgtWdthFt;   //  Target Width in Feet
     double ttlHght; //  Total Height in Pixels
     double ttlWdth; //  Total Width imn Pixels
                       
     double vertFOV; //  Vertical Field of View in Feet
     double horFOV;  //  Horizontal Field of View in Feet
     double vertVA;  //  Vertical Camera Viewing Angle
     double horVA;   //  Horizontal Camera Viewing Angle
     
     double leftRight;   //  Horizontal off-centerness of center of goal
     double upDown;  //  Vertical off-centerness of center of goal
     double wdth1Px; //  Distance from the center of a Goal to the nearest Horizontal edge
     double hght1Px; //  Distance from the center of a Goal to the nearest Vertical edge
     double horThet1;    //  Horizontal Angle from the Edge to Camera to center of Goal
     double vertThet1;   //  Verticle Angle from the Edge to Camera to center of Goal
     
     double d = 0;   //  Distance Variable to be used in firing Calculation
    double pi = Math.PI;    //  Slightly shorter version using Pi
     
     public FindDistance() {
         // Use requires() here to declare subsystem dependencies
         // eg. requires(chassis);
         requires(leftShootingMotors);   //  Sets requires for Left Shooting Motors
         requires(rightShootingMotors);  //  Sets requires for Right hooting Motors
     }
 
     // Called just before this Command runs the first time
     protected void initialize() {
         ttlHght = TrackingCamera.totalHeight;  //  Target Height from the Tracking Camera's static variable
         ttlWdth = TrackingCamera.totalWidth;   //  Target Width from the Tracking Camera's static variable
     }
 
     // Called repeatedly when this Command is scheduled to run
     protected void execute() {
         if (TrackingCamera.targetGoal == null){ //  If no target is found
             leftShootingMotors.setSetpoint(1000);   //  Set Left shooting Motors to Full Speed
             rightShootingMotors.setSetpoint(1000);  //  Set Right Shooting Motors to Full Speed
             System.out.println("No target set");    //  Debug Print Statement
             return;
         }
         ttlHght = 480;  //  Target Height
         ttlWdth = 640;  //  Target Width
         tgtHght = TrackingCamera.targetGoal.boundingRectHeight; //  Sets the height of our target.
         tgtHghtFt = 1.5;    //  Defines goal's constant ft height
         vertFOV = tgtHghtFt / tgtHght * ttlHght;    //  Gets the Foot Value of our Vertical Field of View
 
         vertVA = 47*180/pi; //  Defines the Viewing
         horVA = 47*180/pi;  //  Angles of our camera
 
         tgtWdth = TrackingCamera.targetGoal.boundingRectWidth;  //  Sets the width of our target.
         tgtWdthFt = 2.0;    //  Defines goal's constant ft width
         horFOV = tgtWdthFt / tgtWdth * ttlWdth; //  Gets the ft value of our horizontal Field of View
 
         leftRight = Math.abs(TrackingCamera.targetGoal.center_mass_x - (ttlWdth/2));    //  Finds the horizontal off-centerness
         upDown = Math.abs(TrackingCamera.targetGoal.center_mass_y - (ttlHght/2));   //  Finds the vertical off-ceneterness
         
         wdth1Px = (ttlWdth/2) - leftRight;  //  Defines the distance from the Horizontal Edge to center of Goal in Pixels
         hght1Px = (ttlHght/2) - upDown; //  Defines the distance from the Vertical Edge to center of Goal in Pixels
         
         horThet1 = horVA * wdth1Px/ttlWdth; //  Finds the angle from Horizontal Edge<>camera<>center of goal
         vertThet1 = vertVA * hght1Px/ttlHght;   //  Finds the angle from Vertical Edge<>camera<>center of goal
         
         TrackingCamera.d1 = (hght1Px) / Math.tan(vertThet1);    //  Gets a distance from the center of our goal using Horizontal Theta
         TrackingCamera.d2 = (wdth1Px) / Math.tan(horThet1);  //  Double checks distance with a Vertcial Theta
 
         TrackingCamera.distanceToTarget = (TrackingCamera.d1 + TrackingCamera.d2) / 2;  //  Take the average to try get a more accurate measurement
         
         //if distance to target is invalid, justset it to some number
         if (TrackingCamera.distanceToTarget > 60 || TrackingCamera.distanceToTarget <= 0)
             TrackingCamera.distanceToTarget = 60;
         
         d = TrackingCamera.distanceToTarget;    //  See below Calculation for conciseness
 
         TrackingCamera.launchSpeed = 60 * (d / Math.sqrt((11 / 6 - d) / -16.1) / (2 / 3 * pi));  //Calcs the required rpms for firing
         leftShootingMotors.setSetpoint(TrackingCamera.launchSpeed);     //  Sets the shooting Left Shooting Motors
         rightShootingMotors.setSetpoint(TrackingCamera.launchSpeed);    //  Sets the Right Shooting Motors
         
         /* A String of Debug Print Statements */
         System.out.println();
         System.out.println("D1: "+TrackingCamera.d1);
         System.out.println("D2: "+TrackingCamera.d2);
         System.out.println("D: "+d);
         System.out.println("Camera Launch Speed: "+TrackingCamera.launchSpeed);
         System.out.println();
     }
 
     // Make this return true when this Command no longer needs to run execute()
     protected boolean isFinished() {
         return true;
     }
 
     // Called once after isFinished returns true
     protected void end() {
     }
 
     // Called when another command which requires one or more of the same
     // subsystems is scheduled to run
     protected void interrupted() {
     }
 }
