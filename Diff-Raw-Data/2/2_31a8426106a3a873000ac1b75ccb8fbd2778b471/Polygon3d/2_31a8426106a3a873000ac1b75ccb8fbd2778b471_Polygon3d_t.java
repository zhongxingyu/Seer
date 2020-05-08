 /**
  * 
  */
 package cz.cuni.mff.peckam.java.origamist.math;
 
 import static cz.cuni.mff.peckam.java.origamist.math.MathHelper.EPSILON;
 
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.Queue;
 import java.util.Set;
 
 import javax.vecmath.Point3d;
 import javax.vecmath.Vector3d;
 
 import cz.cuni.mff.peckam.java.origamist.utils.ChangeNotification;
 import cz.cuni.mff.peckam.java.origamist.utils.ObservableList.ChangeTypes;
 import cz.cuni.mff.peckam.java.origamist.utils.Observer;
 
 /**
  * A (possibly non-convex and hole-containing, but connected) polygon in 3D space. All the triangles the polygon
  * consists of must lie in the same plane.
  * 
  * @author Martin Pecka
  * 
  * @param T The type of the triangles this polygon consists of.
  */
 public class Polygon3d<T extends Triangle3d>
 {
     /** The triangles the polygon consists of. */
     protected HashSet<T>        triangles          = new HashSet<T>();
 
     /** The read-only view of triangles. */
     protected Set<T>            trianglesRO        = Collections.unmodifiableSet(triangles);
 
     /** The plane the polygon lies in. */
     protected Plane3d           plane              = null;
 
     /** A list of observers of the triangles property. */
     protected List<Observer<T>> trianglesObservers = new LinkedList<Observer<T>>();
 
     /**
      * Create a new polygon consisting of the given triangles.
      * 
      * @param triangles The triangles the polygon consists of. The list can be modified by this function.
      * @throws IllegalStateException If the resulting polygon either wouldn't be connected or one of the triangles
      *             doesn't lie in the same plane as the first triangle does. In the case this exception is thrown, the
      *             polygon's state will remain the same as before calling this function (eg. this will not try to add
      *             all "valid" triangles from the given list, but it either accepts all or none of them).
      */
     public Polygon3d(T... triangles) throws IllegalStateException
     {
         this(Arrays.asList(triangles));
     }
 
     /**
      * Create a new polygon consisting of the given triangles.
      * 
      * @param triangles The triangles the polygon consists of. The list can be modified by this function.
      * @throws IllegalStateException If the resulting polygon either wouldn't be connected or one of the triangles
      *             doesn't lie in the same plane as the first triangle does. In the case this exception is thrown, the
      *             polygon's state will remain the same as before calling this function (eg. this will not try to add
      *             all "valid" triangles from the given list, but it either accepts all or none of them).
      */
     public Polygon3d(List<T> triangles) throws IllegalStateException
     {
         this(new HashSet<T>(triangles));
     }
 
     /**
      * Create a new polygon consisting of the given triangles.
      * 
      * @param triangles The triangles the polygon consists of. The set can be modified by this function.
      * @throws IllegalStateException If the resulting polygon either wouldn't be connected or one of the triangles
      *             doesn't lie in the same plane as the first triangle does. In the case this exception is thrown, the
      *             polygon's state will remain the same as before calling this function (eg. this will not try to add
      *             all "valid" triangles from the given list, but it either accepts all or none of them).
      */
     public Polygon3d(Set<T> triangles) throws IllegalStateException
     {
         if (triangles.size() > 0) {
             addTriangles(triangles);
         }
     }
 
     /**
      * Add all the given triangles to the polygon.
      * 
      * @param triangles The triangles to add. The list can be modified by this function.
      * @throws IllegalStateException If the resulting polygon either wouldn't be connected or one of the triangles
      *             doesn't lie in the same plane as the layer does. In the case this exception is thrown, the polygon's
      *             state will remain the same as before calling this function (eg. this will not try to add all "valid"
      *             triangles from the given list, but it either accepts all or none of them).
      */
     public void addTriangles(T... triangles) throws IllegalStateException
     {
         addTriangles(Arrays.asList(triangles));
     }
 
     /**
      * Add all the given triangles to the polygon.
      * 
      * @param triangles The triangles to add. The list can be modified by this function.
      * @throws IllegalStateException If the resulting polygon either wouldn't be connected or one of the triangles
      *             doesn't lie in the same plane as the layer does. In the case this exception is thrown, the polygon's
      *             state will remain the same as before calling this function (eg. this will not try to add all "valid"
      *             triangles from the given list, but it either accepts all or none of them).
      */
     public void addTriangles(List<T> triangles) throws IllegalStateException
     {
         addTriangles(new HashSet<T>(triangles));
     }
 
     /**
      * Add all the given triangles to the polygon.
      * 
      * @param triangles The triangles to add. The set can be modified by this function.
      * @throws IllegalStateException If the resulting polygon either wouldn't be connected or one of the triangles
      *             doesn't lie in the same plane as the layer does. In the case this exception is thrown, the polygon's
      *             state will remain the same as before calling this function (eg. this will not try to add all "valid"
      *             triangles from the given list, but it either accepts all or none of them).
      */
     static int i = 0;
 
     @SuppressWarnings("unchecked")
     public void addTriangles(Set<T> triangles) throws IllegalStateException
     {
         if (triangles == null || triangles.size() == 0)
             return;
 
         // be sure not to add a triangle for the second time
         triangles.removeAll(this.triangles); // TODO should be done using epsilonEquals
         if (triangles.size() == 0)
             return; // nothing new to add
 
         if (plane == null)
             plane = triangles.iterator().next().getPlane();
 
         i++;
         // check that all the new triangles lie in the polygon's plane
         for (T t : triangles) {
             Vector3d cross = new Vector3d();
             cross.cross(plane.getNormal(), t.getNormal());
             // if the normals are parallel, then the cross product will be zero
             if (!cross.epsilonEquals(new Vector3d(), EPSILON)) {
                 throw new IllegalStateException(
                         "Adding a triangle to polygon but the triangle doesn't lie in the polygon's plane.");
             }
         }
 
         // backup for the case that the resulting polygon is invalid
         HashSet<T> oldTriangles = new HashSet<T>(this.triangles);
 
         this.triangles.addAll(triangles);
 
         T borderTriangle = null; // the triangle from the "old" polygon which neighbors with a new triangle
         if (oldTriangles.size() == 0) {
             // if we have had no triangles in the polygon yet, fake the borderTriangle with any new triangle
             borderTriangle = triangles.iterator().next();
         } else {
             outer: for (T t : triangles) {
                 for (Triangle3d n : t.getNeighbors()) {
                     if (oldTriangles.contains(n)) {
                         borderTriangle = (T) n;
                         break outer;
                     }
                 }
             }
         }
 
         if (borderTriangle == null) {
             this.triangles = oldTriangles;
             throw new IllegalStateException(
                     "Trying to add triangles to polygon, but none of them neighbors to the polygon.");
         }
 
         // check if the polygon is connected (doesn't consist of two or more parts)
         // this can be done by recursively traversing the neighbors of any one triangle and checking that we visited all
         // triangles this way (in a connected space there always exists a path between any two points)
         // two triangles are not considered being neighbors if they only touch in a vertex
         HashSet<T> visited = new HashSet<T>(oldTriangles); // we suppose that the old polygon was connected
         Queue<T> toVisit = new LinkedList<T>();
         toVisit.add(borderTriangle);
         T t;
         while ((t = toVisit.poll()) != null) {
             visited.add(t);
             for (T n : getNeighbors(t)) {
                 if (!visited.contains(n))
                     toVisit.add(n);
             }
         }
 
         if (visited.size() != this.triangles.size()) {
             // if we didn't manage to visit all triangles, the resulting polygon wouldn't be connected
             this.triangles = oldTriangles;
             throw new IllegalStateException("Trying to construct a non-connected polygon.");
         }
 
         if (!additionalAddTrianglesCheck(triangles)) {
             this.triangles = oldTriangles;
             throw new IllegalStateException(
                     "The triangles newly added to this polygon don't conform to the rules for new triangles.");
         }
 
         for (Observer<T> observer : trianglesObservers) {
             for (T triangle : triangles) {
                 observer.changePerformed(new ChangeNotification<T>(triangle, ChangeTypes.ADD));
             }
         }
     }
 
     /**
      * Add the given triangle to the polygon.
      * 
      * For adding just one triangle, this is more efficient than {@link Polygon3d#addTriangles(Set)}.
      * 
      * @param triangle The triangle to add.
      * @throws IllegalStateException If the resulting polygon either wouldn't be connected or the triangle doesn't lie
      *             in the same plane as the layer does. In the case this exception is thrown, the polygon's state will
      *             remain the same as before calling this function.
      */
     public void addTriangle(T triangle) throws IllegalStateException
     {
         if (triangle == null)
             return;
 
         // be sure not to add the triangle for the second time
         if (this.triangles.contains(triangle)) // TODO should be checked using epsilonEquals
             return; // nothing new to add
 
         if (plane == null) {
             plane = triangle.getPlane();
         } else {
             // check that the new triangle lies in the polygon's plane
             Vector3d cross = new Vector3d();
             cross.cross(plane.getNormal(), triangle.getNormal());
             // if the normals are parallel, then the cross product will be zero
             if (!cross.epsilonEquals(new Vector3d(), EPSILON)) {
                 throw new IllegalStateException(
                         "Adding a triangle to polygon but the triangle doesn't lie in the polygon's plane.");
             }
         }
 
         // backup for the case that the resulting polygon is invalid
         HashSet<T> oldTriangles = new HashSet<T>(this.triangles);
 
         this.triangles.add(triangle);
 
         boolean neighborsToOldPolygon = false;
         if (oldTriangles.size() == 0) {
             // if we have had no triangles in the polygon yet, set this flag to true, since it's useless in this case
             neighborsToOldPolygon = true;
         } else {
             for (Triangle3d n : triangle.getNeighbors()) {
                 if (oldTriangles.contains(n)) {
                     neighborsToOldPolygon = true;
                     break;
                 }
             }
         }
 
         if (!neighborsToOldPolygon) {
             this.triangles = oldTriangles;
             throw new IllegalStateException(
                     "Trying to add a triangle to polygon, but it doesn't neighbor to the polygon.");
         }
 
         HashSet<T> set = new HashSet<T>(1);
         set.add(triangle);
 
         if (!additionalAddTrianglesCheck(set)) {
             this.triangles = oldTriangles;
             throw new IllegalStateException(
                     "The triangle newly added to this polygon doesn't conform to the rules for new triangles.");
         }
 
         for (Observer<T> observer : trianglesObservers) {
             observer.changePerformed(new ChangeNotification<T>(triangle, ChangeTypes.ADD));
         }
     }
 
     /**
      * Remove all the given triangles from the polygon. Triangles not present in the polygon are ignored.
      * 
      * @param triangles The triangles to remove. The set can be modified by this function.
      * @throws IllegalStateException If the resulting polygon wouldn't be connected. In the case this exception is
      *             thrown, the polygon's state will remain the same as before calling this function (eg. this will not
      *             try to remove all "valid" triangles from the given list, but it either accepts all or none of them).
      */
     public void removeTriangles(Set<T> triangles) throws IllegalStateException
     {
         if (triangles == null || triangles.size() == 0)
             return;
 
         // ignore triangles not present in the polygon
         try {
             triangles.retainAll(this.triangles); // TODO should be done using epsilonEquals
         } catch (UnsupportedOperationException e) {
             Iterator<T> it = triangles.iterator();
             while (it.hasNext()) {
                 T t = it.next();
                 if (!this.triangles.contains(t))
                     it.remove();
             }
         }
         if (triangles.size() == 0)
             return; // nothing to remove
 
         if (triangles.size() == this.triangles.size()) {
             // we want to remove all triangles
             this.triangles.clear();
             plane = null;
             return;
         }
 
         // backup for the case that the resulting polygon is invalid
         HashSet<T> oldTriangles = new HashSet<T>(this.triangles);
 
         this.triangles.removeAll(triangles);
 
         // check if the polygon is connected (doesn't consist of two or more parts)
         // this can be done by recursively traversing the neighbors of any one triangle and checking that we visited all
         // triangles this way (in a connected space there always exists a path between any two points)
         // two triangles are not considered being neighbors if they only touch in a vertex
         HashSet<T> visited = new HashSet<T>(this.triangles.size());
         Queue<T> toVisit = new LinkedList<T>();
         toVisit.add(this.triangles.iterator().next());
         T t;
         while ((t = toVisit.poll()) != null) {
             visited.add(t);
             List<T> neighbors = getNeighbors(t);
             for (T n : neighbors) {
                 if (!visited.contains(n))
                     toVisit.add(n);
             }
         }
 
         if (visited.size() != this.triangles.size()) {
             // if we didn't manage to visit all triangles, the resulting polygon wouldn't be connected
             this.triangles = oldTriangles;
             throw new IllegalStateException("Trying to construct a non-connected polygon.");
         }
 
         if (!additionalRemoveTrianglesCheck(triangles)) {
             this.triangles = oldTriangles;
             throw new IllegalStateException(
                     "The triangles removed from this polygon don't conform to the rules for removed triangles.");
         }
 
         for (Observer<T> observer : trianglesObservers) {
             for (T triangle : triangles) {
                 observer.changePerformed(new ChangeNotification<T>(triangle, ChangeTypes.REMOVE));
             }
         }
     }
 
     /**
      * Remove the given triangle from the polygon. If it is not present in the polygon, nothing happens.
      * 
      * This function is no more effective than calling {@link Polygon3d#removeTriangles(Set)} with a one-element set.
      * 
      * @param triangle The triangle to remove.
      * @throws IllegalStateException If the resulting polygon wouldn't be connected. In the case this exception is
      *             thrown, the polygon's state will remain the same as before calling this function.
      */
     public void removeTriangle(T triangle) throws IllegalStateException
     {
         HashSet<T> set = new HashSet<T>(1);
         set.add(triangle);
         removeTriangles(set);
     }
 
     /**
      * "Cut" the given triangle by the given segment and return the triangles that are created by the cut.
      * 
      * @param segment The triangle to cut and the segment to cut with.
      * @return The newly created triangles.
      * 
      * @throws IllegalArgumentException If the segment doesn't define a cut of the triangle or if the triangle isn't one
      *             of this polygon's triangles.
      */
     public List<T> subdivideTriangle(IntersectionWithTriangle<T> segment) throws IllegalArgumentException
     {
         if (!triangles.contains(segment.triangle)) { // TODO should be checked using epsilonEquals
             throw new IllegalArgumentException(
                     "Polygon3d#subdivideTriangle(): Trying to subdivide a triangle not present in this polygon.");
         }
 
         if (!segment.triangle.sidesContain(segment.p) || !segment.triangle.sidesContain(segment.p2)) {
             throw new IllegalArgumentException(
                     "Polygon3d#subdivideTriangle(): Trying to subdivide a triangle by an invalid cut segment.");
         }
 
         List<T> triangles = segment.triangle.subdivideTriangle(segment);
 
         if (triangles.size() == 0)
             assert false : "Polygon3d#subdivideTriangle(): 0 triangles after triangle subdivision.";
 
         if (triangles.size() == 1)
             // no subdividing is needed
             return triangles;
 
         this.triangles.remove(segment.triangle);
 
         for (Observer<T> observer : trianglesObservers) {
             observer.changePerformed(new ChangeNotification<T>(segment.triangle, ChangeTypes.REMOVE));
         }
 
         this.triangles.addAll(triangles);
 
         for (Observer<T> observer : trianglesObservers) {
             for (T triangle : triangles) {
                 observer.changePerformed(new ChangeNotification<T>(triangle, ChangeTypes.ADD));
             }
         }
 
         return triangles;
     }
 
     /**
      * Performs additional checks on the newly added triangles.
      * 
      * This is intended to be used by subclasses to specify more precisely the rules for adding new triangles.
      * 
      * @param triangles The newly triangles.
      * @return <code>true</code> if all the checks were ok.
      */
     protected boolean additionalAddTrianglesCheck(Set<T> triangles)
     {
         return true;
     }
 
     /**
      * Performs additional checks on the removed triangles.
      * 
      * This is intended to be used by subclasses to specify more precisely the rules for removing triangles.
      * The check is called in the time when this.triangles and this.neighbors don't contain the removed triangles.
      * 
      * @param triangles The removed triangles.
      * @return <code>true</code> if all the checks were ok.
      */
     protected boolean additionalRemoveTrianglesCheck(Set<T> triangles)
     {
         return true;
     }
 
     /**
      * Return all triangles that have a common edge with the given triangle.
      * 
      * @param triangle The triangle to find neighbors for.
      * @return All triangles that have a common edge with the given triangle.
      */
     @SuppressWarnings("unchecked")
     public List<T> getNeighbors(T triangle)
     {
         List<T> result = new LinkedList<T>();
 
         for (Triangle3d n : triangle.getNeighbors()) {
             if (this.triangles.contains(n))
                 result.add((T) n);
         }
 
         return result;
     }
 
     /**
      * @return An unmodifiable set of the triangles this polygon consists of.
      */
     public Set<T> getTriangles()
     {
         return trianglesRO;
     }
 
     /**
      * Tell whether this polygon contains the given point.
      * 
      * @param point The point to check.
      * @return <code>true</code> if this polygon contains the given point.
      */
     public boolean contains(Point3d point)
     {
         // TODO maybe ineffective
         if (!plane.contains(point))
             return false;
 
         for (T t : triangles) {
             if (t.contains(point))
                 return true;
         }
         return false;
     }
 
     /**
      * Return the segments that are the intersection of the given line and this polygon. If a part of the intersection
      * would be a point, then a segment with zero direction vector will appear in the list.
      * 
      * All segments will have their direction vector pointing in the same direction (the same direction where points the
      * line's direction vector) and will be ordered along this vector.
      * No two segments will have a common point (this means segments with a common point will be joined).
      * 
      * @param line The line we search intersections with.
      * @return the segments that are the intersection of the given line and this polygon. If a part of the intersection
      *         would be a point, then a segment with zero direction vector will appear in the list.
      */
     public List<Segment3d> getIntersections(Line3d line)
     {
         // connect all segments that can be connected into one new segment
         List<IntersectionWithTriangle<T>> intersections = getIntersectionsWithTriangles(line);
         return joinNeighboringSegments(intersections);
     }
 
     /**
      * Return the intersections of the given stripe and this polygon.
      * 
      * @param stripe The stripe to find intersections with.
      * @return The intersections of the given stripe and this polygon. <code>null</code> if the stripe is parallel to
      *         this polygon.
      */
     public List<Segment3d> getIntersections(Stripe3d stripe)
     {
         Line3d stripePlaneAndPolygonPlaneInt = stripe.getPlane().getIntersection(getPlane());
         if (stripePlaneAndPolygonPlaneInt == null)
             return null; // the stripe and the polygon are parallel
 
         Line3d segmentPoint1 = stripe.getHalfspace1().getPlane().getIntersection(stripePlaneAndPolygonPlaneInt);
         Line3d segmentPoint2 = stripe.getHalfspace2().getPlane().getIntersection(stripePlaneAndPolygonPlaneInt);
 
         // we can assume the computed segment points are regular points and that they exist - an intersection line of
         // two non-parallel planes always exists; in addition the intersection line isn't parallel to any of the
         // stripe's border lines (and lies in the same plane), so these lines must intersect
 
         Segment3d intersectionSegment = new Segment3d(segmentPoint1.p, segmentPoint2.p);
         return getIntersections(intersectionSegment);
     }
 
     /**
      * Return the single segment defining the intersection of the given stripe with this polygon (this segment joins the
      * first and last segment returned by {@link Polygon3d#getIntersections}).
      * 
      * @param stripe The stripe to find the intersection with.
      * @return The intersection of the given stripe and this polygon. <code>null</code> if the stripe is parallel to
      *         this polygon (and if it lies in the same plane as the polygon).
      */
     public Segment3d getIntersectionSegment(Stripe3d stripe)
     {
         List<Segment3d> ints = getIntersections(stripe);
        if (ints == null || ints.size() == 0)
             return null;
         Point3d int1 = ints.get(0).getP1();
         Point3d int2 = ints.get(ints.size() - 1).getP2();
         return new Segment3d(int1, int2);
     }
 
     /**
      * Take the list of segments lying on one line. They all must point in the same direction and must be ordered as
      * they go along the line. This method returns the list of segments such, that no two segments will have a common
      * point (segments with common points will be joined).
      * 
      * @param segments The list of segments to join.
      * @return The list of joined segments.
      */
     public List<Segment3d> joinNeighboringSegments(List<IntersectionWithTriangle<T>> segments)
     {
         LinkedList<Segment3d> result = new LinkedList<Segment3d>();
 
         for (IntersectionWithTriangle<T> s : segments) {
             if (result.size() == 0 || !result.getLast().p2.epsilonEquals(s.p, EPSILON)) {
                 result.add(new Segment3d(s.p, s.p2));
             } else {
                 Segment3d last = result.getLast();
                 last = new Segment3d(last.p, s.p2);
                 result.removeLast();
                 result.add(last);
             }
         }
 
         return result;
     }
 
     /**
      * Return the segments that are the intersection of the given line and this polygon. If a part of the intersection
      * would be a point, then a segment with zero direction vector will appear in the list.
      * 
      * All segments will have their direction vector pointing in the same direction (the same direction where points the
      * line's direction vector) and will be ordered along this vector.
      * Each segment will end at intersection with a triangle.
      * 
      * Be aware that if you provide a segment as the argument and the segment starts or ends within a triangle, it will
      * be virtually extended to intersect a side of the triangle. This is implemented as a specific need of the
      * Origamist project to be robust against rounding errors.
      * 
      * @param line The line we search intersections with.
      * @return the segments that are the intersection of the given line and this polygon. If a part of the intersection
      *         would be a point, then a segment with zero direction vector will appear in the list.
      */
     public List<IntersectionWithTriangle<T>> getIntersectionsWithTriangles(final Line3d line)
     {
         // idea: find intersections with triangles and sort them as they go along the line
 
         final Hashtable<Point3d, Double> parameters = new Hashtable<Point3d, Double>(triangles.size());
         List<IntersectionWithTriangle<T>> intersections = new LinkedList<IntersectionWithTriangle<T>>();
 
         for (T t : triangles) {
             Segment3d intersection = t.getIntersection(line);
 
             // this is important to be robust against rounding errors - in most cases we don't handle segments starting
             // or ending inside triangles, and if we encounter such a segment, it is probably due to rounding errors
             if (line instanceof Segment3d && intersection != null
                     && (!t.sidesContain(intersection.p) || !t.sidesContain(intersection.p2))) {
                 intersection = t.getIntersection(new Line3d(line));
             }
 
             if (intersection != null) {
                 if (parameters.get(intersection.p) == null)
                     parameters.put(intersection.p, line.getParameterForPoint(intersection.p));
                 if (parameters.get(intersection.p2) == null)
                     parameters.put(intersection.p2, line.getParameterForPoint(intersection.p2));
                 double p1 = parameters.get(intersection.p);
                 double p2 = parameters.get(intersection.p2);
                 if (p1 > p2) { // here we don't want to compare using epsilon-equals
                     intersection = new Segment3d(intersection.p2, intersection.p);
                 }
                 intersections.add(new IntersectionWithTriangle<T>(t, intersection));
             }
         }
 
         if (intersections.size() <= 1)
             return intersections;
 
         // sort the segments according to the parameters of the border points
         // this means the segments will be ordered "as they go along the line"
         Collections.sort(intersections, new Comparator<IntersectionWithTriangle<T>>() {
             @Override
             public int compare(IntersectionWithTriangle<T> o1, IntersectionWithTriangle<T> o2)
             {
                 double diff = parameters.get(o1.p) - parameters.get(o2.p);
                 if (diff < -EPSILON) {
                     return -1;
                 } else if (diff > EPSILON) {
                     return 1;
                 } else {
                     // if the segments have the same starts, one of them can be a zero-length segment - consider those
                     // as less than "full"-length segments
                     double maxDiff = parameters.get(o1.p2) - parameters.get(o2.p2);
                     return (maxDiff < -EPSILON ? -1 : (maxDiff > EPSILON ? 1 : 0));
                 }
             }
         });
 
         return intersections;
     }
 
     /**
      * Split this polygon to two or more polygons by the given line.
      * 
      * This method requires that the line has either no intersection with a triangle or goes through its edge.
      * 
      * The resulting polygons will be the maximal ones that can be connected.
      * 
      * @param line The line to split around.
      * @param part1 The polygons that are in the direction of the cross product of the line direction vector and the
      *            polygon plane's normal.
      * @param part2 The rest of polygons.
      * @return <code>part1</code>.
      * 
      * @throws IllegalArgumentException If the line goes through the inside of a triangle.
      */
     public List<Polygon3d<T>> splitPolygon(Line3d line, List<Polygon3d<T>> part1, List<Polygon3d<T>> part2)
             throws IllegalArgumentException
     {
         Vector3d direction = new Vector3d();
         direction.cross(line.getVector(), getNormal());
         Point3d dirPoint = new Point3d(direction);
         Point3d p2 = new Point3d(line.p);
         p2.add(line.v);
 
         HalfSpace3d hs = HalfSpace3d.createPerpendicularToTriangle(line.p, p2, dirPoint);
 
         Set<T> part1triangles = new HashSet<T>();
         Set<T> part2triangles = new HashSet<T>();
         for (T triangle : triangles) {
             Segment3d intersection = triangle.getIntersection(line);
             if (intersection == null
                     || (intersection.v.epsilonEquals(new Vector3d(), EPSILON) && triangle.isVertex(intersection.p))
                     || intersection.overlaps(triangle.getS1()) || intersection.overlaps(triangle.getS2())
                     || intersection.overlaps(triangle.getS3())) {
                 if (hs.contains(triangle.p1) && hs.contains(triangle.p2) && hs.contains(triangle.p3)) {
                     part1triangles.add(triangle);
                 } else {
                     part2triangles.add(triangle);
                 }
             } else {
                 throw new IllegalArgumentException(
                         "Polygon3d#splitLayer: a line going through the interior of a triangle detected.");
             }
         }
 
         Hashtable<Set<T>, List<Polygon3d<T>>> parts = new Hashtable<Set<T>, List<Polygon3d<T>>>(2);
         parts.put(part1triangles, part1);
         parts.put(part2triangles, part2);
 
         for (Entry<Set<T>, List<Polygon3d<T>>> e : parts.entrySet()) {
             Set<T> triangles = e.getKey();
             List<Polygon3d<T>> polygons = e.getValue();
 
             while (!triangles.isEmpty()) {
                 Iterator<T> it = triangles.iterator();
                 T triangle = it.next();
                 it.remove();
                 it = null;
                 Queue<T> queue = new LinkedList<T>();
                 queue.add(triangle);
                 List<T> polygonTriangles = new LinkedList<T>();
                 while ((triangle = queue.poll()) != null) {
                     polygonTriangles.add(triangle);
                     triangles.remove(triangle);
                     List<T> tNeighbors = getNeighbors(triangle);
                     for (T n : tNeighbors) {
                         if (triangles.contains(n)) {
                             queue.add(n);
                         }
                     }
                 }
                 polygons.add(new Polygon3d<T>(polygonTriangles));
             }
         }
 
         return part1;
     }
 
     /**
      * Rotate all triangles in this layer around the given axis by the given angle.
      * 
      * @param axis The axis to rotate around.
      * @param angle The angle to rotate the triangles by.
      */
     public void rotate(Line3d axis, double angle)
     {
         List<T> oldTriangles = new LinkedList<T>();
         oldTriangles.addAll(triangles);
 
         // hashset needs the triangles to not change their hashcode, but setting points to something else will change it
         // - on the other side, a LinkedList doesn't care about the hashcode
         // so we need to remove all triangles, change the vertices and then add them back again - this will also "reset"
         // the neighbors map
         removeTriangles(triangles);
         for (T t : oldTriangles) {
             t.setPoints(MathHelper.rotate(t.getP1(), axis, angle), MathHelper.rotate(t.getP2(), axis, angle),
                     MathHelper.rotate(t.getP3(), axis, angle));
         }
         addTriangles(oldTriangles);
     }
 
     /**
      * @return
      * @see cz.cuni.mff.peckam.java.origamist.math.Plane3d#getNormal()
      */
     public Vector3d getNormal()
     {
         return plane.getNormal();
     }
 
     /**
      * @return The plane the polygon lies in.
      */
     public Plane3d getPlane()
     {
         return plane;
     }
 
     /**
      * @return the trianglesObservers
      */
     public List<Observer<T>> getTrianglesObservers()
     {
         return trianglesObservers;
     }
 
     /**
      * Add new observer of the triangles property.
      * 
      * @param observer The observer to add.
      */
     public void addTrianglesObserver(Observer<T> observer)
     {
         trianglesObservers.add(observer);
     }
 
     /**
      * Remove the given observer of the triangles property.
      * 
      * @param observer The observer to remove.
      */
     public void removeTrianglesObserver(Observer<T> observer)
     {
         trianglesObservers.remove(observer);
     }
 
     /**
      * Remove all observers of the triangles property.
      */
     public void clearTrianglesObservers()
     {
         trianglesObservers.clear();
     }
 
     @Override
     public String toString()
     {
         return "Polygon3d [" + triangles + "]";
     }
 }
