 package edu.wpi.first.wpilibj.templates.commands;
 
 import edu.wpi.first.wpilibj.networktables.NetworkTable;
 import edu.wpi.first.wpilibj.networktables.NetworkTableKeyNotDefined;
 
 /**
  *
  * @author bradmiller
  */
 public class Aim extends CommandBase {
 
     public Aim() {
         // Use requires() here to declare subsystem dependencies
         // eg. requires(chassis);
         requires(vision);
     }
 
     // Called just before this Command runs the first time
     protected void initialize() {
     }
 
     // Called repeatedly when this Command is scheduled to run
     protected void execute() {
        
         try {
             NetworkTable SDTable = new NetworkTable();
             SDTable = NetworkTable.getTable("SmartDashboard");    
  
            for (int i = 0; i < SDTable.getSubTable("camera").getKeys().size(); i++) {
         double x = SDTable.getSubTable("camera").getDouble("x" + i, 0);
         double y = SDTable.getSubTable("camera").getDouble("y" + i, 0);
         System.out.println("particle #" + i + " center:(" + x + "," + y + ")");
             }
        } catch (Exception ex) {
             System.out.println(ex);
       }
         
     }
 
     // Make this return true when this Command no longer needs to run execute()
     protected boolean isFinished() {
         return false;
     }
 
     // Called once after isFinished returns true
     protected void end() {
     }
 
     // Called when another command which requires one or more of the same
     // subsystems is scheduled to run
     protected void interrupted() {
     }
 }
