 package com.edinarobotics.zed.commands;
 
 import com.edinarobotics.utils.math.Point2;
 import com.edinarobotics.zed.vision.Target;
 import com.edinarobotics.zed.vision.TargetType;
 import com.edinarobotics.zed.vision.VisionTrackingCommand;
 
 public class FixedPointVisionTrackingCommand extends VisionTrackingCommand {
     public static final double PYRAMID_BACK_MIDDLE_GOAL_WIDTH = 0.632219;
     public static final double PYRAMID_BACK_MIDDLE_GOAL_HEIGHT = 0.142857;
     
     private Point2 fixedPoint;
     private TargetType targetType;
     
     public static final Point2 PYRAMID_BACK_MIDDLE_NEW = new Point2(-0.018, -0.25);
     public static final Point2 PYRAMID_BACK_RIGHT = new Point2(-0.063829, -0.226891);
     public static final Point2 PYRAMID_BACK_MIDDLE = new Point2(0.0273556, -0.29411);
     public static final Point2 PYRAMID_BACK_MIDDLE_NEW2 = new Point2(-0.143, -0.241);
     public static final Point2 PYRAMID_BACK_MIDDLE_TUNNEL = new Point2(PYRAMID_BACK_MIDDLE_NEW.getX() + (1.3 * PYRAMID_BACK_MIDDLE_GOAL_WIDTH),
             PYRAMID_BACK_MIDDLE_NEW.getY() - (1.2 * PYRAMID_BACK_MIDDLE_GOAL_HEIGHT));
     
     public FixedPointVisionTrackingCommand(double xSetpoint, double ySetpoint) {
         this(xSetpoint, ySetpoint, TargetType.ANY_GOAL);
     }
     
     public FixedPointVisionTrackingCommand(double xSetpoint, double ySetpoint, TargetType targetType) {
         this(new Point2(xSetpoint, ySetpoint), targetType);
     }
     
     public FixedPointVisionTrackingCommand(Point2 point2) {
         this(point2, TargetType.ANY_GOAL);
     }
     
     public FixedPointVisionTrackingCommand(Point2 point2, TargetType targetType) {
         fixedPoint = point2;
         this.targetType = targetType;
     }
     
     protected double getXSetpoint() {
         return fixedPoint.getX();
     }
     
     protected double getYSetpoint() {
         return fixedPoint.getY();
     }
     
     protected Target getTarget(){
         return getTargetCollection().getClosestTarget(getXSetpoint(), getYSetpoint(), targetType);
     }
 }
