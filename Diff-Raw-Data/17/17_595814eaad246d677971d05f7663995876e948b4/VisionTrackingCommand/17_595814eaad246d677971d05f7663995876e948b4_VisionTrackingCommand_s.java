 package com.edinarobotics.zed.vision;
 
 import com.edinarobotics.utils.pid.PIDConfig;
 import com.edinarobotics.utils.pid.PIDTuningManager;
 import com.edinarobotics.zed.Components;
 import com.edinarobotics.zed.subsystems.DrivetrainRotation;
 import com.edinarobotics.zed.subsystems.Lifter;
 import edu.wpi.first.wpilibj.DriverStationLCD;
 import edu.wpi.first.wpilibj.PIDController;
 import edu.wpi.first.wpilibj.command.Command;
 import edu.wpi.first.wpilibj.networktables.NetworkTable;
 
 public abstract class VisionTrackingCommand extends Command {
     private static final double xP = -1.2; //-1.5, -1.5
     private static final double xI = -0.0001; //-0.08, -0.008
     private static final double xD = 0;
     private static final double xF = 0;
     private static final double yP = 10;
     private static final double yI = 0;
     private static final double yD = 0;
     private static final double yF = 0;
     
     private DriverStationLCD textOutput;
     private NetworkTable visionTable;
     private PIDController xPIDController;
     private PIDConfig xPIDConfig;
     private TargetXPIDSource xPIDSource;
     private TargetYPIDSource yPIDSource;
     private PIDController yPIDController;
     private PIDConfig yPIDConfig;
     private Lifter lifter;
     private DrivetrainRotation drivetrainRotation;
     
     public VisionTrackingCommand(){
         super("VisionTracking");
         lifter = Components.getInstance().lifter;
         drivetrainRotation = Components.getInstance().drivetrainRotation;
         requires(lifter);
         requires(drivetrainRotation);
         visionTable = NetworkTable.getTable("vision");
         xPIDSource = new TargetXPIDSource();
         yPIDSource = new TargetYPIDSource();
         xPIDController = new PIDController(xP, xI, xD, xF, xPIDSource, drivetrainRotation);
         xPIDConfig = PIDTuningManager.getInstance().getPIDConfig("Vision Horizontal");
         yPIDController = new PIDController(yP, yI, yD, yF, yPIDSource, lifter);
         yPIDConfig = PIDTuningManager.getInstance().getPIDConfig("Vision Vertical");
         textOutput = DriverStationLCD.getInstance();
     }
     
     protected void initialize(){
         xPIDController.setSetpoint(getXSetpoint());
         xPIDController.setAbsoluteTolerance(getXTolerance());
         xPIDController.reset();
         xPIDController.enable();
         yPIDController.setSetpoint(getYSetpoint());
         yPIDController.setAbsoluteTolerance(getYTolerance());
         yPIDController.reset();
         yPIDController.enable();
     }
     
     protected void execute(){
         Target workingTarget = getTarget();
         
         if(workingTarget != null){
             xPIDController.enable();
             yPIDController.enable();
             xPIDSource.setTarget(workingTarget);
             yPIDSource.setTarget(workingTarget);
             xPIDController.setSetpoint(getXSetpoint());
             yPIDController.setSetpoint(getYSetpoint());
             xPIDController.setAbsoluteTolerance(getXTolerance());
             yPIDController.setAbsoluteTolerance(getYTolerance());
             xPIDController.setPID(xPIDConfig.getP(xP), xPIDConfig.getI(xI), xPIDConfig.getD(xD), xPIDConfig.getF(xF));
             yPIDController.setPID(yPIDConfig.getP(yP), yPIDConfig.getI(yI), xPIDConfig.getD(yD), xPIDConfig.getF(yF));
             xPIDConfig.setSetpoint(xPIDController.getSetpoint()); //Tuning feedback
             xPIDConfig.setValue(xPIDSource.pidGet()); //Tuning feedback
             yPIDConfig.setSetpoint(yPIDController.getSetpoint()); //Tuning feedback
             yPIDConfig.setValue(yPIDSource.pidGet()); //Tuning feedback
         }
         else{
             reportStatus("NO TARGET");
             xPIDController.disable();
             yPIDController.disable();
             drivetrainRotation.mecanumPolarRotate(0);
             lifter.setLifterDirection(Lifter.LIFTER_STOP);
         }
     }
     
     private void reportMotion(boolean up){
         if(up && lifter.getUpperLimitSwitch()){
             reportStatus("BACK UP");
         }
         else if(!up && lifter.getLowerLimitSwitch()){
             reportStatus("GO FORWARD");
         }
         else{
             reportStatus("WORKING");
         }
     }
     
     private void reportStatus(String message){
         textOutput.println(DriverStationLCD.Line.kUser1, 1, "VT: "+message+"                                                                ");
         textOutput.updateLCD();
     }
     
     protected void end(){
         xPIDController.disable();
         xPIDController.reset();
         yPIDController.disable();
         yPIDController.reset();
         lifter.setLifterDirection(Lifter.LIFTER_STOP);
         drivetrainRotation.mecanumPolarRotate(0);
         textOutput.println(DriverStationLCD.Line.kUser1, 1, "                                                                ");
         textOutput.updateLCD();
     }
     
     protected void interrupted(){
         end();
     }
     
     protected boolean isFinished(){
         return xPIDController.onTarget() && yPIDController.onTarget();
     }
     
     protected abstract double getXSetpoint();
     
     protected abstract double getYSetpoint();
     
     protected double getXTolerance(){
         return 0;
     }
     
     protected double getYTolerance(){
         return 0;
     }
     
     protected TargetCollection getTargetCollection(){
         return new TargetCollection(visionTable.getString("vtdata", ""));
     }
     
     protected Target getTarget(){
         return getTargetCollection().getClosestTarget(getXSetpoint(), getYSetpoint());
     }
 }
