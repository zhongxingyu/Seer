 package com.edinarobotics.zed.commands;
 
 import com.edinarobotics.utils.pid.PIDConfig;
 import com.edinarobotics.utils.pid.PIDTuningManager;
 import com.edinarobotics.zed.Components;
 import com.edinarobotics.zed.subsystems.DrivetrainRotation;
 import com.edinarobotics.zed.subsystems.Lifter;
 import com.edinarobotics.zed.vision.LifterTargetY;
 import com.edinarobotics.zed.vision.PIDTargetX;
 import com.edinarobotics.zed.vision.Target;
 import com.edinarobotics.zed.vision.TargetCollection;
 import edu.wpi.first.wpilibj.DriverStationLCD;
 import edu.wpi.first.wpilibj.PIDController;
 import edu.wpi.first.wpilibj.command.Command;
 import edu.wpi.first.wpilibj.networktables.NetworkTable;
 
 public class VisionTrackingCommand extends Command {
     public static final byte HIGH_GOAL = 1;
     public static final byte MIDDLE_GOAL = 2;
     public static final byte ANY_GOAL = 3;
     
     private static final DriverStationLCD.Line OUTPUT_LINE = DriverStationLCD.Line.kUser1;
     
     private NetworkTable visionTable = NetworkTable.getTable("vision");
     private TargetCollection targetCollection;
     private DrivetrainRotation drivetrainRotation;
     private Lifter lifter;
     private byte goalType;
     private DriverStationLCD textOutput;
     
     // X Fields
     private PIDController pidControllerX;
     private PIDTargetX pidTargetX;
     private PIDConfig xPIDConfig;
     private double xSetpoint;
     private double xTolerance;
     
     // Y Fields
     LifterTargetY lifterTargetY;
     private double ySetpoint;
     private double yTolerance;
     
     private final double X_P = -0.99;
     private final double X_I = -0.01;
     private final double X_D = -3.25;
     private final double X_F = 0;
     
     public VisionTrackingCommand() {
         this(ANY_GOAL);
     }
     
     public VisionTrackingCommand(byte goalType) {
         super("VisionTracking");
         this.goalType = goalType;
         drivetrainRotation = Components.getInstance().drivetrainRotation;
         lifter = Components.getInstance().lifter;
         textOutput = DriverStationLCD.getInstance();
         
         pidTargetX = new PIDTargetX();
         pidControllerX = new PIDController(X_P, X_I, X_D, pidTargetX, drivetrainRotation);
         xPIDConfig = PIDTuningManager.getInstance().getPIDConfig("Vision Horizontal");
         xSetpoint = 0;
         xTolerance = 0;
         
         lifterTargetY = new LifterTargetY();
         ySetpoint = 0;
        yTolerance = 0.1;
         
         requires(drivetrainRotation);
         requires(lifter);
     }
     
     protected void initialize() {
         pidControllerX.setSetpoint(0);
         pidControllerX.enable();
         pidControllerX.setAbsoluteTolerance(0);
     }
 
     protected void execute() {
         targetCollection = new TargetCollection(visionTable.getString("vtdata", ""));
         Target target;
         
         if(goalType == HIGH_GOAL) {
             target = targetCollection.getClosestTarget(xSetpoint, ySetpoint, true);
         } else if(goalType == MIDDLE_GOAL) {
             target = targetCollection.getClosestTarget(xSetpoint, ySetpoint, false);
         } else {
             target = targetCollection.getClosestTarget(xSetpoint, ySetpoint);
         }
         
         if(target != null) {
             xSetpoint = getXSetpoint(target.getDistance());
             ySetpoint = getYSetpoint(target.getDistance());
             pidTargetX.setTarget(target);
             pidControllerX.setSetpoint(xSetpoint);
             pidControllerX.setAbsoluteTolerance(xTolerance);
             lifterTargetY.setTarget(target);
             lifterTargetY.setYSetpoint(ySetpoint);
             lifterTargetY.setYTolerance(yTolerance);
             lifter.setLifterDirection(lifterTargetY.targetY());
             //Print necessary movements to driver
             if(lifterTargetY.targetY().equals(Lifter.LifterDirection.LIFTER_UP) && lifter.getUpperLimitSwitch()){
                 textOutput.println(OUTPUT_LINE, 1, "VT: BACK UP                                                       ");
             }
             else if(lifterTargetY.targetY().equals(Lifter.LifterDirection.LIFTER_DOWN) && lifter.getLowerLimitSwitch()){
                 textOutput.println(OUTPUT_LINE, 1, "VT: GO FORWARD                                                       ");
             }
             else{
                 textOutput.println(OUTPUT_LINE, 1, "VT: WORKING                                                      ");
             }
             textOutput.updateLCD();
         } else {
             drivetrainRotation.update();
             lifter.update();
             textOutput.println(OUTPUT_LINE, 1, "VT: NO TARGET                                                      ");
             textOutput.updateLCD();
         }
         
         //PID tuning code
         pidControllerX.setPID(xPIDConfig.getP(X_P), xPIDConfig.getI(X_I), xPIDConfig.getD(X_D), xPIDConfig.getF(X_F));
         xPIDConfig.setSetpoint(pidControllerX.getSetpoint());
         xPIDConfig.setValue(pidTargetX.pidGet());
     }
 
     protected boolean isFinished() {
         return pidControllerX.onTarget() && lifterTargetY.onTarget();
     }
 
     protected void end() {
         pidControllerX.disable();
         pidControllerX.reset();
         lifter.setLifterDirection(Lifter.LifterDirection.LIFTER_STOP);
         drivetrainRotation.mecanumPolarRotate(0);
         //Clear the first line on the user messages screen
         textOutput.println(OUTPUT_LINE, 1, "                                                       ");
         textOutput.updateLCD();
     }
 
     protected void interrupted() {
         end();
     }
     
     //Empirically determined functions for vision tracking
      private double getXSetpoint(double distance){
          return 0.0278026829*distance - 0.6818562776;
      }
      
      private double getYSetpoint(double distance){
          return -0.023267714*distance + 0.4098144504 - 0.175;
      }
 }
