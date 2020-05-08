 package shapes;
 
 import static java.lang.Math.*;
 
 // TODO: debugging, remove
 import java.awt.*;
 
 abstract class Geometry {
 
   // used for general double arithmetic
   static final double EPSILON = 0.001;
   // used for judging closeness (0.5 pixels apart == touching)
   static final double TOLERANCE = 0.5;
 
   // Returns false if the code for the given shape pair hasn't been written yet (in distance(Shape, Shape))
   // When writing new touching() methods, remember to use TOLERANCE. This means some other Geometry methods
   // (such as intersection(Circle, Segment)) won't be reliable.
   static boolean touching(Shape s, Shape t) {
     // Eww, gross code. Not sure of the best way to do this. Could instead have
     // identical isTouching() methods in each shape subclass, but that's also
     // not nice, since the actual computations should be in Geometry to avoid
     // duplication.
     if (s instanceof Circle) {
       if (t instanceof Circle) {
         return touching((Circle) s, (Circle) t);
       } else if (t instanceof ConvexPolygon) {
         return touching((Circle) s, (ConvexPolygon) t);
       } else {
         return false;
       }
     } else if (s instanceof ConvexPolygon) {
       if (t instanceof Circle) {
         return touching((ConvexPolygon) s, (Circle) t);
       } else if (t instanceof ConvexPolygon) {
         return touching((ConvexPolygon) s, (ConvexPolygon) t);
       } else {
         return false;
       }
     } else {
       return false;
     }
   }
 
   static boolean touching(ConvexPolygon poly, Circle circle) {
     return touching(circle, poly);
   }
 
   static boolean touching(Circle circle, ConvexPolygon poly) {
    System.out.println("Derp");
     for (Point corner : poly.getCorners()) {
       if (distance(corner, circle.getCenter()) <
           circle.getRadius() + TOLERANCE) {
         return true;
       }
     }
 
     for (Segment side : poly.getSides()) {
       Segment perp = perpendicularThrough(side, circle.getCenter());
       if (perp.length() < circle.getRadius() + TOLERANCE &&
           side.contains(perp.getEnd())) {
         return true;
       }
     }
 
     if (circle.contains(poly) || poly.contains(circle)) {
       return true;
     }
 
     return false;
   }
 
   static boolean touching(ConvexPolygon s, ConvexPolygon t) {
     for (Point corner : s.getCorners()) {
       for (Segment side : t.getSides()) {
         Segment perp = perpendicularThrough(side, corner);
         if (perp.length() < TOLERANCE && side.contains(perp.getEnd())) {
           return true;
         }
       }
     }
 
     for (Segment side : s.getSides()) {
       for (Point corner : t.getCorners()) {
         Segment perp = perpendicularThrough(side, corner);
         if (perp.length() < TOLERANCE && side.contains(perp.getEnd())) {
           return true;
         }
       }
     }
 
     for (Point cornerA : s.getCorners()) {
       for (Point cornerB : t.getCorners()) {
         if (distance(cornerA, cornerB) < TOLERANCE) {
           return true;
         }
       }
     }
 
     for (Segment sideA : s.getSides()) {
       for (Segment sideB : t.getSides()) {
         if (segmentIntersection(sideA, sideB) != null) {
           return true;
         }
       }
     }
 
     if (s.contains(t) || t.contains(s)) {
       return true;
     }
 
     return false;
   }
 
   static boolean touching(Circle s, Circle t) {
     double centerDistance = distance(s.getCenter(), t.getCenter());
     return centerDistance < s.getRadius() + t.getRadius() + TOLERANCE;
   }
 
   // Returns false if the code for the given shape pair hasn't been written yet.
   // TODO: test
   // TODO: move into each Shape?
   static boolean touching(Shape shape, Segment seg) {
     if (shape instanceof Circle) {
       Circle circle = (Circle) shape;
       Segment perp = perpendicularThrough(seg, circle.getCenter());
       return perp.length() < circle.getRadius();
     } else {
       return false;
     }
   }
 
   // TODO: test
   static Segment intersection(Circle circle, Segment seg) {
     Segment perp = perpendicularThrough(seg, circle.getCenter());
     if (perp.length() > circle.getRadius()) {
       return null;
     }
 
     // angle (at the center of the circle) between the perpendicular and
     // the radii to points of intersection
     double radiansFromPerp = acos(perp.length() / circle.getRadius());
 
     // direction of radii to points of intersection
     Direction[] radiusDirection = new Direction[] {
       perp.direction().rotationByRadians(radiansFromPerp),
       perp.direction().rotationByRadians(-1.0 * radiansFromPerp)
     };
 
     Point[] pointOfIntersection = new Point[radiusDirection.length];
     for (int i = 0; i < radiusDirection.length; i++) {
       Vector radius = new Vector(radiusDirection[i], circle.getRadius());
       pointOfIntersection[i] =
         circle.getCenter().translation(radius);
     }
 
     return new Segment(
       pointOfIntersection[0],
       pointOfIntersection[1]
     );
   }
 
   // returns null for parallel lines
   static Point lineIntersection(Segment s, Segment t) {
     double denominator = s.getA() * t.getB() - s.getB() * t.getA();
     if (Math.abs(denominator) < EPSILON) {
       return null;
     }
 
     double x = (s.getC() * t.getB() - s.getB() * t.getC()) / denominator;
     double y = (s.getA() * t.getC() - s.getC() * t.getA()) / denominator;
     return new Point(x, y);
   }
 
   static Point segmentIntersection(Segment s, Segment t) {
     Point intersection = lineIntersection(s, t);
     if (intersection == null) {
       return null;
     }
     if (s.contains(intersection) && t.contains(intersection)) {
       return intersection;
     } else {
       return null;
     }
   }
 
   static Point segmentLineIntersection(Segment segment, Segment line) {
     Point intersection = lineIntersection(segment, line);
     if (intersection == null) {
       return null;
     }
     if (segment.contains(intersection)) {
       return intersection;
     } else {
       return null;
     }
   }
 
   // Returns NaN if the code for the given shape pair hasn't been written yet.
   static double distance(Shape s, Shape t) {
     if (s instanceof Circle && t instanceof Circle) {
       Circle c1 = (Circle) s;
       Circle c2 = (Circle) t;
       double distance = distance(c1.getCenter(), c2.getCenter()) -
                         c1.getRadius() - c2.getRadius();
       return distance;
     } else {
       return Double.NaN;
     }
   }
 
   // Returns a segment perpendicular to the original segment, starting at the
   // given point and ending on the segment (or corresponding line).
   static Segment perpendicularThrough(Segment original, Point through) {
     // perp is perpendicular and has the correct start, but does not have the
     // correct end
     Segment perp = new Segment(
       through,
       through.translation(original.vector().perpendicular())
     );
 
     Point intersection = lineIntersection(original, perp);
 
     return new Segment(through, intersection);
   }
 
   // if mover wants to go to target, but obstacle is in the way,
   // how far can it go?
   static Point maxMovement(Circle mover, Point target, Shape obstacle) {
     Segment path = new Segment(mover.getCenter(), target);
     Point maxMove = target;
 
     if (obstacle instanceof Circle) {
       Circle obs = (Circle) obstacle;
       Segment obstacleToPath = perpendicularThrough(path, obs.getCenter());
       double distanceToPath = obstacleToPath.length();
       double distanceBetweenCenters = mover.getRadius() + obs.getRadius();
 
       if (distanceToPath > distanceBetweenCenters) {
         return target;   // no collision
       }
 
       // TODO: rename variables
       // closest is the point on path closest to obstacle
       // stopPoint is where the center of mover will be after moving
       Point closest = obstacleToPath.getEnd();
       double distanceToStopPoint =
         sqrt(sq(distanceBetweenCenters) - sq(distanceToPath));
       maxMove =
         closest.translation(path.direction().reverse(), distanceToStopPoint);
     } else if (obstacle instanceof ConvexPolygon) {
       ConvexPolygon obs = (ConvexPolygon) obstacle;
       for (Point corner : obs.getCorners()) {
         Segment perp = perpendicularThrough(path, corner);
         double perpDistance = perp.length();
         if (perpDistance > mover.getRadius()) {
           continue;
         }
 
         Point closest = perp.getEnd();
         double centerDestinationToClosest =
           sqrt(sq(mover.getRadius()) - sq(perpDistance));
         Point centerDestination = closest.translation(
           path.direction().reverse(),
           centerDestinationToClosest
         );
         
         if (isShorterMovement(centerDestination, maxMove, path)) {
           maxMove = centerDestination;
         }
       }
 
       for (Segment side : obs.getSides()) {
         Point sidePathIntersection =
           lineIntersection(side, path);
         if (sidePathIntersection == null) {
           continue;
         }
         double sidePathAngle =
           interiorRadians(path.direction(), side.direction());
         double hypoteneuse = mover.getRadius() / sin(sidePathAngle);
         Point centerDestination = sidePathIntersection.translation(
           path.direction().reverse(),
           hypoteneuse
         );
 
         if (
           distance(mover.getCenter(), centerDestination) >
           distance(mover.getCenter(), sidePathIntersection)
         ) {
           continue;   // side is facing the wrong way for a collision to occur
         }
 
         Segment destinationRadius =
           perpendicularThrough(side, centerDestination);
 
         if (!side.contains(destinationRadius.getEnd())) {
           continue;
         }
         
         if (isShorterMovement(centerDestination, maxMove, path)) {
           maxMove = centerDestination;
         }
       }
     }
 
     return maxMove;
   }
 
   static double interiorRadians(Direction dirA, Direction dirB) {
     double difference = abs(dirA.getRadians() - dirB.getRadians());
     if (difference > PI) {
       difference = 2 * PI - difference;
     }
     if (difference > PI / 2) {
       difference = PI - difference;
     }
     return difference;
   }
 
   static Point maxMovement(ConvexPolygon mover, Point target, Shape obstacle) {
     Segment path = new Segment(mover.getCenter(), target);
     Point maxMove = target;
 
     if (obstacle instanceof Circle) {
       Circle obs = (Circle) obstacle;
 
       // check corner collisions
       for (Point corner : mover.getCorners()) {
         Vector cornerOffset = new Vector(mover.getCenter(), corner);
         Point cornerTarget = target.translation(cornerOffset);
         Segment cornerPath = new Segment(corner, cornerTarget);
 
         Segment intersection = intersection(obs, cornerPath);
         if (intersection == null) {
           continue;
         }
 
         // of the two intersection points, this one is closer to mover's starting point
         Point closer; 
         if (
           distance(intersection.getStart(), corner) <
           distance(intersection.getEnd(), corner)
         ) {
           closer = intersection.getStart();
         } else {
           closer = intersection.getEnd();
         }
 
         Point centerDestination = closer.translation(cornerOffset.reverse());
         if (isShorterMovement(centerDestination, maxMove, path)) {
           maxMove = centerDestination;
         }
       }
 
       // check side collisions
       for (Segment side : mover.getSides()) {
         Segment perp = perpendicularThrough(side, obs.getCenter());
         Vector radiusToIntersection =
           new Vector(perp.direction(), obs.getRadius());   // TODO: also check 180 degrees?
         Point intersection = obs.getCenter().translation(radiusToIntersection);
 
         // intersectionPath has the correction direction, but not
         // the correct endpoints
         Segment intersectionPath =
           new Segment(intersection, intersection.translation(path.vector()));
         Point intersectionOrigin =
           segmentLineIntersection(side, intersectionPath);
         
         if (intersectionOrigin == null) {
           continue;
         }
 
         Vector intersectionOffset =
           new Vector(mover.getCenter(), intersectionOrigin);
         Point centerDestination =
           intersection.translation(intersectionOffset.reverse());
 
         if (isShorterMovement(centerDestination, maxMove, path)) {
           maxMove = centerDestination;
         }
       }
 
     } else if (obstacle instanceof ConvexPolygon) {
       ConvexPolygon obs = (ConvexPolygon) obstacle;
       // Two cases: corner hits side, side hits corner
       for (Segment obstacleSide : obs.getSides()) {
         for (Point corner : mover.getCorners()) {
           Vector cornerOffset = new Vector(mover.getCenter(), corner);
           Segment cornerPath =
             new Segment(corner, target.translation(cornerOffset));
           Point intersection =
             segmentLineIntersection(obstacleSide, cornerPath);
           if (intersection == null) {
             continue;
           }
           Point centerDestination =
             intersection.translation(cornerOffset.reverse());
 
           if (isShorterMovement(centerDestination, maxMove, path)) {
             maxMove = centerDestination;
           }
         }
       }
 
       for (Point obstacleCorner : obs.getCorners()) {
         for (Segment side : mover.getSides()) {
           // intersectionPath is a line parallel to path through obstacleCorner
           Segment intersectionPath = new Segment(
             obstacleCorner, 
             obstacleCorner.translation(path.vector())
           );
           Point intersectionOrigin = segmentLineIntersection(side, intersectionPath);
           if (intersectionOrigin == null) {
             continue;
           }
           Vector intersectionOffset =
             new Vector(mover.getCenter(), intersectionOrigin);
           Point centerDestination =
             obstacleCorner.translation(intersectionOffset.reverse());
 
           if (isShorterMovement(centerDestination, maxMove, path)) {
             maxMove = centerDestination;
           }
         }
       }
     }
 
     // TODO: put buffer between mover and obstacle, not along mover's path?
 //    if (!maxMove.equals(target)) {
       // give a little buffer, so mover doesn't get "stuck" on obstacle
       Vector backwards =
         new Vector(path.vector().getDirection().reverse(), TOLERANCE / 2.0);
       maxMove = maxMove.translation(backwards);
       if (!path.contains(maxMove)) {
         // but don't overcompensate
         maxMove = mover.getCenter();
       }
 //    }
     return maxMove;
   }
 
   // helper method for maxMovement()
   private static boolean isShorterMovement(
     Point candidate,
     Point leader,
     Segment path
   ) {
 
     if (!path.contains(candidate)) {
       return false;
     }
 
     return
       distance(candidate, path.getStart()) <
       distance(leader, path.getStart());
   }
 
   static boolean offscreen(Point point) {
     if (
       point.getX() < 0 || point.getX() > Game.getCanvas().WIDTH ||
       point.getY() < 0 || point.getY() > Game.getCanvas().HEIGHT
     ) {
       return true;
     }
 
     return false;
   }
 
   static double cross(Vector s, Vector t) {
     return s.getXComponent() * t.getYComponent() -
            s.getYComponent() * t.getXComponent();
   }
 
   // NOTE: this returns NaN if the points are horizontally aligned
   static double slope(Point s, Point t) {
     return (s.getY() - t.getY()) / (s.getX() - t.getX());
   }
 
   static double fromCanvasX(double canvasX) {
     return canvasX;
   }
 
   static double fromCanvasY(double canvasY) {
     return Game.getCanvas().HEIGHT - canvasY;
   }
 
   static double distance(Point s, Point t) {
     return sqrt(sq(s.getX() - t.getX()) + sq(s.getY() - t.getY()));
   }
 
   static double sq(double x) {
     return x*x;
   }
 }
