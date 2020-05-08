 package com.edinarobotics.zed.commands;
 
 import com.edinarobotics.utils.math.Point2;
 
 public class FixedPointVisionTrackingCommand extends VisionTrackingCommand {
     private double fixedXSetpoint;
     private double fixedYSetpoint;
     
     public static final Point2 PYRAMID_BACK_MIDDLE_NEW = new Point2(-0.018, -0.25);
     public static final Point2 PYRAMID_BACK_RIGHT = new Point2(-0.063829, -0.226891);
     public static final Point2 PYRAMID_BACK_MIDDLE = new Point2(0.0273556, -0.29411);
     public static final Point2 PYRAMID_BACK_MIDDLE_NEW2 = new Point2(-0.143, -0.241);
     
     public FixedPointVisionTrackingCommand(double xSetpoint, double ySetpoint) {
         super();
         this.fixedXSetpoint = xSetpoint;
         this.fixedYSetpoint = ySetpoint;
     }
     
     public FixedPointVisionTrackingCommand(double xSetpoint, double ySetpoint, byte goalType) {
         super(goalType);
         this.fixedXSetpoint = xSetpoint;
         this.fixedYSetpoint = ySetpoint;
     }
     
     public FixedPointVisionTrackingCommand(Point2 point2) {
         this(point2.getX(), point2.getY());
     }
     
     public FixedPointVisionTrackingCommand(Point2 point2, byte goalType) {
         this(point2.getX(), point2.getY(), goalType);
     }
     
     protected double getXSetpoint(double distance) {
         return fixedXSetpoint;
     }
     
     protected double getYSetpoint(double distance) {
         return fixedYSetpoint;
     }
 }
