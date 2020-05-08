 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.slidellrobotics.reboundrumble.commands;
 
 import edu.wpi.first.wpilibj.image.BinaryImage;
 import edu.wpi.first.wpilibj.image.NIVisionException;
 import edu.wpi.first.wpilibj.image.ParticleAnalysisReport;
 
 /**
  *
  * @author 10491477
  */
 public class FindDistance extends CommandBase {
     private BinaryImage partReport = null;      //
     private ParticleAnalysisReport leftGoal;    //
     private ParticleAnalysisReport rightGoal;   //
     private double leftWidth;                   //
     private double rightWidth;                  //
     private ParticleAnalysisReport targetGoal;  //
     private double targetHeight;                //
     private double totalHeight;                 //  Varible Constructors
     private double vertFOV;                     //
     private double d;                           //
     private double launchSpeed;                 //
     
     
     public FindDistance() {
         // Use requires() here to declare subsystem dependencies
         // eg. requires(chassis);
         requires(camera);
        requires(shootingMotors);
     }
 
     // Called just before this Command runs the first time
     protected void initialize() {
     }
 
     // Called repeatedly when this Command is scheduled to run
     protected void execute() {
         try {
             leftGoal = partReport.getParticleAnalysisReport(2);     //Recognizes the
             rightGoal = partReport.getParticleAnalysisReport(3);    //middle goals.
             leftWidth = leftGoal.boundingRectWidth;     //Finds the widths of
             rightWidth = rightGoal.boundingRectWidth;   //both middle goals.
             while(leftWidth >= rightWidth) {        //
                 targetGoal = rightGoal;             //Decides which goal we are
             } while(rightWidth < leftWidth){        //closer to and targets it.
                 targetGoal = leftGoal;              //
             }
             targetHeight = targetGoal.boundingRectHeight;   //Sets the height of our target.
             totalHeight = partReport.getHeight();           //Gets the pixel height of the image
             
             vertFOV = ((1.5*totalHeight)/targetHeight);     //Gets the foot equivalent of our vertical Field of View
             d = ((vertFOV/2)/.445228685);       //Finds the distance from the camera to the Goal (tangent45)
             launchSpeed = 60*(d/(((11/6)-d)/((-1)*16))/((2/3)*3.1415926));  //Calcs the required rpms for firing
         } catch(NIVisionException ex) {
         }    
     }
 
     // Make this return true when this Command no longer needs to run execute()
     protected boolean isFinished() {
         return false;                   //never ends
     }
 
     // Called once after isFinished returns true
     protected void end() {
     }
 
     // Called when another command which requires one or more of the same
     // subsystems is scheduled to run
     protected void interrupted() {
     }
 }
