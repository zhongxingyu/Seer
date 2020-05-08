 package com.edinarobotics.zed.commands;
 
 import com.edinarobotics.utils.math.Point2;
import com.edinarobotics.utils.math.VariablePoint2;
 
 public class FixedPointVisionTrackingCommand extends VisionTrackingCommand {
     public static final double PYRAMID_BACK_MIDDLE_GOAL_WIDTH = 0.632219;
     public static final double PYRAMID_BACK_MIDDLE_GOAL_HEIGHT = 0.142857;
     
    private Point2 fixedPoint;
     
     public static final Point2 PYRAMID_BACK_MIDDLE_NEW = new Point2(-0.018, -0.25);
     public static final Point2 PYRAMID_BACK_RIGHT = new Point2(-0.063829, -0.226891);
     public static final Point2 PYRAMID_BACK_MIDDLE = new Point2(0.0273556, -0.29411);
     public static final Point2 PYRAMID_BACK_MIDDLE_NEW2 = new Point2(-0.143, -0.241);
    public static final Point2 PYRAMID_BACK_MIDDLE_TUNNEL = new Point2(PYRAMID_BACK_MIDDLE_NEW.getX() + (1.3 * PYRAMID_BACK_MIDDLE_GOAL_WIDTH),
            PYRAMID_BACK_MIDDLE_NEW.getY() - (1.2 * PYRAMID_BACK_MIDDLE_GOAL_HEIGHT));
     //public static final Point2 PYRAMID_BACK_MIDDLE_AUTO = new Point2(PYRAMID_BACK_MIDDLE_NEW.getX() - (0.5 * PYRAMID_BACK_MIDDLE_GOAL_WIDTH),
     //        PYRAMID_BACK_MIDDLE_NEW.getY() - (0.87 * PYRAMID_BACK_MIDDLE_GOAL_HEIGHT));
     
     /*
      * TO RESET VT POINT: -0.5 goal width and -0.5 goal height
      */
     
     public FixedPointVisionTrackingCommand(double xSetpoint, double ySetpoint) {
         super();
        fixedPoint = new Point2(xSetpoint, ySetpoint);
     }
     
     public FixedPointVisionTrackingCommand(double xSetpoint, double ySetpoint, byte goalType) {
         super(goalType);
        fixedPoint = new Point2(xSetpoint, ySetpoint);
     }
     
     public FixedPointVisionTrackingCommand(Point2 point2) {
        super();
        fixedPoint = point2;
     }
     
     public FixedPointVisionTrackingCommand(Point2 point2, byte goalType) {
        super(goalType);
        fixedPoint = point2;
     }
     
     protected double getXSetpoint(double distance) {
        return fixedPoint.getX();
     }
     
     protected double getYSetpoint(double distance) {
        return fixedPoint.getY();
     }
 }
