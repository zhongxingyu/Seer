 package lxx.utils;
 
 import lxx.model.LxxRobot;
 import robocode.Rules;
 import robocode.util.Utils;
 
 import java.awt.*;
 import java.awt.geom.Point2D;
 import java.awt.geom.Rectangle2D;
 import java.util.*;
 import java.util.List;
 
 import static java.lang.Math.*;
 
 public class LxxUtils {
 
     private static final int FIFTEEN_BITS = 0x7FFF;
 
     private static final double ROBOT_SQUARE_DIAGONAL = LxxConstants.ROBOT_SIDE_SIZE * sqrt(2);
     private static final double HALF_PI = Math.PI / 2;
     private static final double DOUBLE_PI = Math.PI * 2;
 
     public static double angle(double baseX, double baseY, double x, double y) {
         double theta = QuickMath.asin((y - baseY) / LxxPoint.distance(x, y, baseX, baseY)) - HALF_PI;
         if (x >= baseX && theta < 0) {
             theta = -theta;
         }
         return (theta %= DOUBLE_PI) >= 0 ? theta : (theta + DOUBLE_PI);
     }
 
     public static double angle(APoint p1, APoint p2) {
         return angle(p1.x(), p1.y(), p2.x(), p2.y());
     }
 
     public static double anglesDiff(double alpha1, double alpha2) {
         return abs(Utils.normalRelativeAngle(alpha1 - alpha2));
     }
 
     public static double limit(double minValue, double value, double maxValue) {
         if (value < minValue) {
             return minValue;
         }
 
         if (value > maxValue) {
             return maxValue;
         }
 
         return value;
     }
 
     public static double lateralDirection(APoint center, LxxRobot robot) {
         return lateralDirection(center, robot, robot.velocity, robot.heading);
     }
 
     private static double lateralDirection(APoint center, APoint pos, double velocity, double heading) {
         assert !Double.isNaN(heading);
         if (Utils.isNear(0, velocity)) {
             return 1;
         }
         return signum(lateralVelocity(center, pos, velocity, heading));
     }
 
     public static double lateralVelocity(APoint center, LxxRobot robot) {
         return lateralVelocity(center, robot, robot.velocity, robot.heading);
     }
 
     public static double lateralVelocity(APoint center, APoint pos, double velocity, double heading) {
         assert !Double.isNaN(heading);
         assert heading >= 0 && heading <= LxxConstants.RADIANS_360;
         return velocity * QuickMath.sin(Utils.normalRelativeAngle(heading - center.angleTo(pos)));
     }
 
     public static double advancingVelocity(APoint center, LxxRobot robot) {
         return advancingVelocity(center, robot, robot.velocity, robot.heading);
     }
 
     public static double advancingVelocity(APoint center, APoint pos, double velocity, double heading) {
         assert !Double.isNaN(heading);
         assert heading >= 0 && heading <= LxxConstants.RADIANS_360;
         return velocity * QuickMath.cos(Utils.normalRelativeAngle(heading - center.angleTo(pos)));
     }
 
     public static double getBulletPower(double bulletSpeed) {
         // speed = 20 - 3 * firepower
         // - 3 * firepower = speed - 20
         // firepower = (20 - speed) / 3
         return (20 - bulletSpeed) / 3;
     }
 
     public static double getReturnedEnergy(double bulletPower) {
         return 3 * bulletPower;
     }
 
     public static Rectangle2D getBoundingRectangleAt(APoint point) {
         return getBoundingRectangleAt(point, LxxConstants.ROBOT_SIDE_HALF_SIZE);
     }
 
     public static Rectangle2D getBoundingRectangleAt(APoint point, final int sideHalfSize) {
         return new Rectangle.Double(point.x() - sideHalfSize, point.y() - sideHalfSize,
                 sideHalfSize * 2, sideHalfSize * 2);
     }
 
     public static double bearingOffset(APoint source, APoint dest1, APoint dest2) {
         return Utils.normalRelativeAngle(angle(source, dest2) - angle(source, dest1));
     }
 
     public static double getRobotWidthInRadians(APoint center, APoint robotPos) {
         return getRobotWidthInRadians(angle(center, robotPos), center.aDistance(robotPos));
     }
 
     public static double getRobotWidthInRadians(double angle, double distance) {
         final double alpha = abs(LxxConstants.RADIANS_45 - (angle % LxxConstants.RADIANS_90));
         if (distance < ROBOT_SQUARE_DIAGONAL) {
             distance = ROBOT_SQUARE_DIAGONAL;
         }
         return QuickMath.asin(QuickMath.cos(alpha) * ROBOT_SQUARE_DIAGONAL / distance);
     }
 
     public static double getMaxEscapeAngle(double bulletSpeed) {
        return QuickMath.asin(Rules.MAX_VELOCITY / bulletSpeed) * 1.2;
     }
 
     public static double calculateAcceleration(LxxRobot prevState, LxxRobot curState) {
         if (prevState == null) {
             return 0;
         }
 
         double acceleration;
         if (signum(curState.velocity) == signum(prevState.velocity) || abs(curState.velocity) < 0.001) {
             acceleration = abs(curState.velocity) - abs(prevState.velocity);
         } else {
             acceleration = abs(curState.velocity);
         }
 
         return limit(-Rules.MAX_VELOCITY, acceleration, Rules.ACCELERATION);
     }
 
     @SuppressWarnings({"unchecked"})
     public static <K, V> Map<K, V> toMap(Object... data) {
         if (data.length % 2 != 0) {
             throw new IllegalArgumentException("data length: " + data.length);
         }
         Map map = new HashMap();
 
         for (int i = 0; i < data.length; i += 2) {
             map.put(data[i], data[i + 1]);
         }
 
         return map;
     }
 
     public static double getStopDistance(double speed) {
         double distance = 0;
         while (speed > 0) {
             speed -= Rules.DECELERATION;
             distance += speed;
         }
         return distance;
     }
 
     public static double getStopTime(double speed) {
         int time = 0;
         while (speed > 0) {
             speed -= Rules.DECELERATION;
             time++;
         }
         return time;
     }
 
     // we solve this problem in coordinate system with center in farest pnt and y direction equals to segment angle
     // in this cs following set of equations taking place:
     // / x = 0 - equation of segment
     // \ (x - cx)^2 + (y - cy)^2 = r^2 - equation of circle
     // then y = +/- sqrt(r^2 - cx^2) + cy;
     // because x = 0, y - it's distance from farest pnt to intersection pnt
     // so intersection point it's projection from farest point in direction on segment on y distance
     public static APoint[] intersection(APoint pnt1, APoint pnt2, final APoint center, double r) {
         final APoint farest;
         final APoint closest;
         if (center.aDistance(pnt1) > center.aDistance(pnt2)) {
             farest = pnt1;
             closest = pnt2;
         } else {
             farest = pnt2;
             closest = pnt1;
         }
         final double segmentAlpha = farest.angleTo(closest);
         final double segmentDist = farest.aDistance(closest);
         // calculate circle center in new cs
         final APoint newCircleCenter = new LxxPoint().project(abs(Utils.normalRelativeAngle(farest.angleTo(center) - segmentAlpha)), farest.aDistance(center));
 
         if (r < newCircleCenter.x()) {
             // no intersection
             return new LxxPoint[0];
         }
 
         final double y1 = sqrt(r * r - newCircleCenter.x() * newCircleCenter.x()) + newCircleCenter.y();
         final double y2 = -sqrt(r * r - newCircleCenter.x() * newCircleCenter.x()) + newCircleCenter.y();
 
         final List<APoint> res = new ArrayList<APoint>();
         if (y2 > 0 && y2 < segmentDist) {
             res.add(farest.project(segmentAlpha, y2));
         }
         if (y1 > 0 && y1 < segmentDist) {
             res.add(farest.project(segmentAlpha, y1));
         }
 
         return res.toArray(new APoint[res.size()]);
     }
 
     public static int getRoundTime(long time, int round) {
         if (round > FIFTEEN_BITS || time > FIFTEEN_BITS) {
             throw new IllegalArgumentException("Too large round-time: " + round + " - " + time);
         }
 
         return (int) (((round & FIFTEEN_BITS) << 15) | (time & FIFTEEN_BITS));
     }
 
     public static <T> List<T> asModifiableList(T... items) {
         return new ArrayList<T>(Arrays.asList(items));
     }
 
     public static <T> List<T> List(T ... items) {
         return Arrays.asList(items);
     }
 
     public static <T> List<T> add(List<T> lst, T item) {
         lst.add(item);
         return lst;
     }
 
 }
