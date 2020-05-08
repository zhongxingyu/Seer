 /**
  * 
  */
 package cz.cuni.mff.peckam.java.origamist.modelstate;
 
 import static cz.cuni.mff.peckam.java.origamist.math.MathHelper.EPSILON;
 import static java.lang.Math.abs;
 import static java.lang.Math.cos;
 import static java.lang.Math.sin;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.Queue;
 import java.util.Set;
 
 import javax.media.j3d.GeometryArray;
 import javax.media.j3d.LineArray;
 import javax.media.j3d.TriangleArray;
 import javax.vecmath.Color4b;
 import javax.vecmath.Matrix3d;
 import javax.vecmath.Point2d;
 import javax.vecmath.Point3d;
 import javax.vecmath.Point4d;
 import javax.vecmath.Vector3d;
 
 import org.apache.log4j.Logger;
 
 import cz.cuni.mff.peckam.java.origamist.math.CanonicLine2d;
 import cz.cuni.mff.peckam.java.origamist.math.CanonicLine3d;
 import cz.cuni.mff.peckam.java.origamist.math.HalfSpace3d;
 import cz.cuni.mff.peckam.java.origamist.math.IntersectionWithTriangle;
 import cz.cuni.mff.peckam.java.origamist.math.Line2d;
 import cz.cuni.mff.peckam.java.origamist.math.Line3d;
 import cz.cuni.mff.peckam.java.origamist.math.Polygon3d;
 import cz.cuni.mff.peckam.java.origamist.math.Segment2d;
 import cz.cuni.mff.peckam.java.origamist.math.Segment3d;
 import cz.cuni.mff.peckam.java.origamist.math.Stripe3d;
 import cz.cuni.mff.peckam.java.origamist.math.Triangle2d;
 import cz.cuni.mff.peckam.java.origamist.math.Triangle3d;
 import cz.cuni.mff.peckam.java.origamist.model.Origami;
 import cz.cuni.mff.peckam.java.origamist.model.Step;
 import cz.cuni.mff.peckam.java.origamist.model.UnitDimension;
 import cz.cuni.mff.peckam.java.origamist.model.UnitHelper;
 import cz.cuni.mff.peckam.java.origamist.model.jaxb.Unit;
 import cz.cuni.mff.peckam.java.origamist.modelstate.Fold.FoldLine;
 import cz.cuni.mff.peckam.java.origamist.utils.ChangeNotification;
 import cz.cuni.mff.peckam.java.origamist.utils.ObservableList;
 import cz.cuni.mff.peckam.java.origamist.utils.ObservableList.ChangeTypes;
 import cz.cuni.mff.peckam.java.origamist.utils.Observer;
 
 /**
  * The internal state of the model after some steps.
  * 
  * @author Martin Pecka
  */
 public class ModelState implements Cloneable
 {
     /**
      * Folds on this paper.
      */
     protected ObservableList<Fold>                          folds                 = new ObservableList<Fold>();
 
     /** The list of fold lines converted to 3D. Use getFoldLines3d() to get the list. */
     protected List<Segment3d>                               foldLines3d           = new LinkedList<Segment3d>();
 
     /**
      * Cache for array of the lines representing folds.
      */
     protected LineArray                                     foldLineArray         = null;
 
     /**
      * If true, the value of foldLineArray doesn't have to be consistent and a call to updateLineArray is needed.
      */
     protected boolean                                       foldLineArrayDirty    = true;
 
     /**
      * The triangles this model state consists of.
      */
     protected ObservableList<ModelTriangle>                 triangles             = new ObservableList<ModelTriangle>();
 
     /** The triangles on the paper. Automatically updated when <code>triangles</code> change. */
     protected List<Triangle2d>                              triangles2d           = new LinkedList<Triangle2d>();
 
     /**
      * The list of triangles that have an edge on the line in key. <b>Note that these aren't real neighbors!</b> If a
      * triangle has no neighbor, the list will contain only 1 element. If there is no triangle with the given edge,
      * <code>null</code> is returned. Automatically updated when <code>triangles</code> change.
      */
     protected Hashtable<CanonicLine3d, List<ModelTriangle>> neighbors             = new Hashtable<CanonicLine3d, List<ModelTriangle>>();
 
     /**
      * The list of triangles that have an edge on the line in key. <b>Note that these aren't real neighbors!</b> If a
      * triangle has no neighbor, the list will contain only 1 element. If there is no triangle with the given edge,
      * <code>null</code> is returned. Automatically updated when <code>triangles</code> change.
      */
     protected Hashtable<CanonicLine2d, List<Triangle2d>>    neighbors2d           = new Hashtable<CanonicLine2d, List<Triangle2d>>();
 
     /** The layers of the paper. */
     protected ObservableList<Layer>                         layers                = new ObservableList<Layer>();
 
     /** The mapping of triangles to their containing layer. Automatically updated when <code>layers</code> change */
     protected Hashtable<ModelTriangle, Layer>               trianglesToLayers     = new Hashtable<ModelTriangle, Layer>();
 
     /**
      * A cache for quick finding of a 3D triangle corresponding to the given 2D triangle. Automatically updated when
      * <code>triangles</code> change.
      */
     protected Hashtable<Triangle2d, ModelTriangle>          paperToSpaceTriangles = new Hashtable<Triangle2d, ModelTriangle>();
 
     /**
      * The triangles the model state consists of. This representation can be directly used by Java3D.
      */
     protected TriangleArray                                 trianglesArray        = null;
 
     /**
      * If true, the value of trianglesArray doesn't have to be consistent and a call to updateVerticesArray is needed.
      */
     protected boolean                                       trianglesArrayDirty   = true;
 
     /**
      * Rotation of the model (around the axis from eyes to display) in radians.
      */
     protected double                                        rotationAngle         = 0;
 
     /**
      * The angle the model is viewed from (angle between eyes and the unfolded paper surface) in radians.
      * 
      * PI/2 means top view, -PI/2 means bottom view
      */
     protected double                                        viewingAngle          = Math.PI / 2.0;
 
     /**
      * The step this state belongs to.
      */
     protected Step                                          step;
 
     /**
      * The origami model which is this the state of.
      */
     protected Origami                                       origami;
 
     /**
      * The number of steps a foldline remains visible.
      */
     protected int                                           stepBlendingTreshold  = 5;
 
     public ModelState()
     {
         folds.addObserver(new Observer<Fold>() {
             @Override
             public void changePerformed(ChangeNotification<Fold> change)
             {
                 ModelState.this.foldLineArrayDirty = true;
 
                 if (change.getChangeType() == ChangeTypes.ADD) {
                     change.getItem().lines.addObserver(new Observer<FoldLine>() {
                         @Override
                         public void changePerformed(ChangeNotification<FoldLine> change)
                         {
                             ModelState.this.foldLineArrayDirty = true;
                         }
                     });
                 }
             }
         });
 
         triangles.addObserver(new Observer<ModelTriangle>() {
             @Override
             public void changePerformed(ChangeNotification<ModelTriangle> change)
             {
                 ModelState.this.trianglesArrayDirty = true;
                 if (change.getChangeType() != ChangeTypes.ADD) {
                     ModelTriangle t = change.getOldItem();
                     paperToSpaceTriangles.remove(t.originalPosition);
                     triangles2d.remove(t.originalPosition);
                     for (Segment3d edge : t.getEdges()) {
                         CanonicLine3d line = new CanonicLine3d(edge);
                         neighbors.get(line).remove(t);
                         if (neighbors.get(line).size() == 0)
                             neighbors.remove(line);
                     }
                     for (Segment2d edge : t.originalPosition.getEdges()) {
                         CanonicLine2d line = new CanonicLine2d(edge);
                         neighbors2d.get(line).remove(t.originalPosition);
                         if (neighbors2d.get(line).size() == 0)
                             neighbors2d.remove(line);
                     }
                 } else if (change.getChangeType() != ChangeTypes.REMOVE) {
                     ModelTriangle t = change.getItem();
                     paperToSpaceTriangles.put(t.originalPosition, t);
                     triangles2d.add(t.originalPosition);
                     for (Segment3d edge : t.getEdges()) {
                         CanonicLine3d line = new CanonicLine3d(edge);
                         if (neighbors.get(line) == null)
                             neighbors.put(line, new LinkedList<ModelTriangle>());
                         neighbors.get(line).add(t);
                     }
                     for (Segment2d edge : t.originalPosition.getEdges()) {
                         CanonicLine2d line = new CanonicLine2d(edge);
                         if (neighbors2d.get(line) == null)
                             neighbors2d.put(line, new LinkedList<Triangle2d>());
                         neighbors2d.get(line).add(t.originalPosition);
                     }
                 }
             }
         });
 
         layers.addObserver(new Observer<Layer>() {
             @Override
             public void changePerformed(ChangeNotification<Layer> change)
             {
                 if (change.getChangeType() != ChangeTypes.ADD) {
                     Layer old = change.getOldItem();
                     for (ModelTriangle t : old.getTriangles()) {
                         trianglesToLayers.remove(t);
                     }
                     old.clearTrianglesObservers();
                 } else if (change.getChangeType() != ChangeTypes.REMOVE) {
                     final Layer layer = change.getItem();
                     for (ModelTriangle t : layer.getTriangles()) {
                         trianglesToLayers.put(t, layer);
                     }
                     layer.addTrianglesObserver(new Observer<ModelTriangle>() {
                         @Override
                         public void changePerformed(ChangeNotification<ModelTriangle> change)
                         {
                             if (change.getChangeType() != ChangeTypes.ADD) {
                                 trianglesToLayers.remove(change.getOldItem());
                             } else if (change.getChangeType() != ChangeTypes.REMOVE) {
                                 ModelTriangle triangle = change.getItem();
                                 trianglesToLayers.put(triangle, layer);
                             }
                         }
                     });
                 }
             }
 
         });
     }
 
     /**
      * Set the step this model state belongs to.
      * 
      * @param step The step to set.
      */
     public void setStep(Step step)
     {
         this.step = step;
     }
 
     /**
      * Set the origami model this step will work with.
      * 
      * @param origami The origami model.
      */
     public void setOrigami(Origami origami)
     {
         this.origami = origami;
     }
 
     /**
      * Takes a point defined in the 2D paper relative coordinates and returns the position of the point in the 3D model
      * state (also in relative coordinates).
      * 
      * @param point
      * @return
      */
     protected Point3d locatePointFromPaperTo3D(Point2d point)
     {
         ModelTriangle containingTriangle = null;
         // TODO possible performance loss, try to use some kind of Voronoi diagram??? But it seems that this section
         // won't be preformance-bottle-neck
         for (ModelTriangle t : triangles) {
             if (t.getOriginalPosition().contains(point)) {
                 containingTriangle = t;
                 break;
             }
         }
 
         if (containingTriangle == null) {
             Logger.getLogger(getClass()).warn("locatePointFromPaperTo3D: Couldn't locate point " + point);
             return new Point3d();
         }
 
         Vector3d barycentric = containingTriangle.getOriginalPosition().getBarycentricCoords(point);
 
         return containingTriangle.interpolatePointFromBarycentric(barycentric);
     }
 
     /**
      * @return The list of all fold lines converted to 3D.
      */
     protected List<Segment3d> getFoldLines3d()
     {
         if (foldLineArrayDirty)
             updateLineArray();
 
         return foldLines3d;
     }
 
     /**
      * Update the contents of the foldLineArray so that it corresponds to the actual contents of the folds variable.
      */
     protected synchronized void updateLineArray()
     {
         int linesCount = 0;
         for (Fold fold : folds) {
             linesCount += fold.lines.size();
         }
 
         UnitDimension paperSize = origami.getModel().getPaper().getSize();
         double ratio = UnitHelper.convertTo(Unit.REL, Unit.M, 1, paperSize.getUnit(), paperSize.getMax());
 
         foldLineArray = new LineArray(2 * linesCount, GeometryArray.COORDINATES | GeometryArray.COLOR_4);
         foldLines3d.clear();
         int i = 0;
         for (Fold fold : folds) {
             for (FoldLine line : fold.lines) {
                 Point2d startPoint = line.line.getP1();
                 Point3d startPoint2 = locatePointFromPaperTo3D(startPoint);
                 startPoint2.scale(ratio);
                 foldLineArray.setCoordinate(2 * i, startPoint2);
 
                 Point2d endPoint = line.line.getP2();
                 Point3d endPoint2 = locatePointFromPaperTo3D(endPoint);
                 endPoint2.scale(ratio);
                 foldLineArray.setCoordinate(2 * i + 1, endPoint2);
 
                 foldLines3d.add(new Segment3d(startPoint2, endPoint2));
 
                 // TODO implement some more line thickness and style possibilities
                 byte alpha = (byte) 255;
                 if (line.direction != null) {
                     // TODO invent some more sophisticated way to determine the fold "age"
                     int diff = step.getId() - fold.originatingStepId;
                     if (diff <= stepBlendingTreshold) {
                         alpha = (byte) (255 - (diff / stepBlendingTreshold) * 255);
                     } else {
                         alpha = 0;
                     }
                 }
                 foldLineArray.setColor(2 * i, new Color4b((byte) 0, (byte) 0, (byte) 0, alpha));
                 foldLineArray.setColor(2 * i + 1, new Color4b((byte) 0, (byte) 0, (byte) 0, alpha));
                 i++;
             }
         }
 
         foldLineArrayDirty = false;
     }
 
     /**
      * Retrurn the line array corresponding to the list of folds.
      * 
      * @return The line array corresponding to the list of folds.
      */
     public synchronized LineArray getLineArray()
     {
         if (foldLineArrayDirty)
             updateLineArray();
 
         return foldLineArray;
     }
 
     /**
      * Update the contents of the trianglesArray so that it corresponds to the actual contents of the triangles
      * variable.
      */
     protected synchronized void updateTrianglesArray()
     {
         trianglesArray = new TriangleArray(triangles.size() * 3, TriangleArray.COORDINATES);
 
         UnitDimension paperSize = origami.getModel().getPaper().getSize();
         double ratio = 1.0 / UnitHelper.convertTo(Unit.REL, Unit.M, 1, paperSize.getUnit(), paperSize.getMax());
 
         int i = 0;
         Point3d p1, p2, p3;
         for (Triangle3d triangle : triangles) {
             p1 = (Point3d) triangle.getP1().clone();
             p1.project(new Point4d(p1.x, p1.y, p1.z, ratio));
 
             p2 = (Point3d) triangle.getP2().clone();
             p2.project(new Point4d(p2.x, p2.y, p2.z, ratio));
 
             p3 = (Point3d) triangle.getP3().clone();
             p3.project(new Point4d(p3.x, p3.y, p3.z, ratio));
 
             trianglesArray.setCoordinate(3 * i, p1);
             trianglesArray.setCoordinate(3 * i + 1, p2);
             trianglesArray.setCoordinate(3 * i + 2, p3);
 
             i++;
         }
 
         trianglesArrayDirty = false;
     }
 
     /**
      * Return the triangle array.
      * 
      * @return The triangle array.
      */
     public synchronized TriangleArray getTrianglesArray()
     {
         if (trianglesArrayDirty)
             updateTrianglesArray();
 
         return trianglesArray;
     }
 
     /**
      * Performs a valley/mountain fold.
      * 
      * @param direction The direction of the fold - VALLEY/MOUNTAIN.
      * @param startPoint Starting point of the fold (in 2D paper relative coordinates).
      * @param endPoint Ending point of the fold (in 2D paper relative coordinates).
      * @param affectedLayers The layers the fold will be performed on.
      * @param angle The angle the paper should be bent by (in radians). Value in (0, PI) means that the down right part
      *            of the paper (with respect to the line) will be moved; value in (-PI,0) means that the other part of
      *            paper will be moved.
      */
     public void makeFold(Direction direction, Point2d startPoint, Point2d endPoint, List<Integer> affectedLayers,
             double angle)
     {
         // TODO implement some way of defining which part of the paper will stay on its place and which will move
         Point3d start = locatePointFromPaperTo3D(startPoint);
         Point3d end = locatePointFromPaperTo3D(endPoint);
         Segment3d segment = new Segment3d(start, end);
 
         List<Layer> layers = getLayers(segment);
 
         int i = 1;
         List<Segment3d> foldLines = new LinkedList<Segment3d>();
         for (Layer layer : layers) {
             if (!affectedLayers.contains(i++))
                 continue;
             // TODO handle direction in some appropriate way
             List<Segment3d> layerFoldLines = makeFoldInLayer(layer, direction, segment);
             foldLines.addAll(layerFoldLines);
         }
 
         bendPaper(direction, segment, foldLines, affectedLayers, angle);
     }
 
     /**
      * Bends the paper. Requires that the fold line goes only along triangle edges, not through the interiors of them.
      * 
      * To specify the part of the paper that will be rotated, the segment's direction vector is used. Make cross product
      * of the normal of the layer the segment lies in and the direction vector of the segment. The cross product points
      * to the part of the paper that will be moved.
      * 
      * @param direction The direction of the fold - VALLEY/MOUNTAIN.
      * @param segment The segment to bend around. Note that the direction vector of the segment specifies which part of
      *            the paper will be rotated.
      * @param foldLines The fold lines that were created by doing the last fold.
      * @param affectedLayers The layers the fold will be performed on.
      * @param angle The angle the paper should be bent by (in radians). Value in (0, PI) means that the down right part
      *            of the paper (with respect to the line) will be moved; value in (-PI,0) means that the other part of
      *            paper will be moved.
      */
     protected void bendPaper(Direction direction, Segment3d segment, List<Segment3d> foldLines,
             List<Integer> affectedLayers, double angle)
     {
         double angle1 = angle;
         if (abs(angle1) < EPSILON)
             return;
 
         if (direction == Direction.MOUNTAIN)
             angle1 = -angle1;
 
         Point3d segCenter = new Point3d(segment.getP1());
         segCenter.add(segment.getP2());
         segCenter.scale(0.5d);
 
         Layer segLayer = getLayerForPoint(segCenter);
         Vector3d layerNormalSegmentDirCross = new Vector3d();
         layerNormalSegmentDirCross.cross(segLayer.getNormal(), segment.getVector());
         Point3d r = new Point3d(layerNormalSegmentDirCross);
 
         HalfSpace3d halfspace = HalfSpace3d.createPerpendicularToTriangle(segment.getP1(), segment.getP2(), r);
 
         List<Layer> layers = getLayers(segment);
         int i = 1;
         Queue<ModelTriangle> queue = new LinkedList<ModelTriangle>();
         for (Layer layer : layers) {
             if (!affectedLayers.contains(i++))
                 continue;
 
             List<Polygon3d<ModelTriangle>> part1 = new LinkedList<Polygon3d<ModelTriangle>>();
             List<Polygon3d<ModelTriangle>> part2 = new LinkedList<Polygon3d<ModelTriangle>>();
             layer.splitPolygon(segment, part1, part2);
 
             boolean swapParts = false;
             if (part1.size() > 0) {
                 Triangle3d part1t = part1.get(0).getTriangles().iterator().next();
                 if (!(halfspace.contains(part1t.getP1()) && halfspace.contains(part1t.getP2()) && halfspace
                         .contains(part1t.getP3()))) {
                     swapParts = true;
                 }
             } else {
                 Triangle3d part2t = part2.get(0).getTriangles().iterator().next();
                 if (!(halfspace.contains(part2t.getP1()) && halfspace.contains(part2t.getP2()) && halfspace
                         .contains(part2t.getP3()))) {
                     swapParts = true;
                 }
             }
 
             if (swapParts) {
                 List<Polygon3d<ModelTriangle>> tmp = part1;
                 part1 = part2;
                 part2 = tmp;
             }
 
             this.layers.remove(layer);
             for (Polygon3d<ModelTriangle> l : part1)
                 this.layers.add(new Layer(l));
             for (Polygon3d<ModelTriangle> l : part2)
                 this.layers.add(new Layer(l));
 
             for (Polygon3d<ModelTriangle> l : part1) {
                 queue.addAll(l.getTriangles());
             }
         }
 
         // to find all triangles that have to be rotated, first add all triangles in "affected" layers that lie in the
         // right halfspace, and then go over neighbors of all found triangles to rotate and add them, if the neighbor
         // doesn't lie on an opposite side of a fold line.
 
        Set<ModelTriangle> inQueue = new HashSet<ModelTriangle>(queue);
         Set<ModelTriangle> trianglesToRotate = new HashSet<ModelTriangle>();
         ModelTriangle t;
         while ((t = queue.poll()) != null) {
             trianglesToRotate.add(t);
             Hashtable<Segment3d, Segment3d> overlaps = new Hashtable<Segment3d, Segment3d>();
             for (Segment3d edge : t.getEdges()) {
                 for (Segment3d foldLine : foldLines) {
                     if (edge.overlaps(foldLine)) {
                         overlaps.put(edge, foldLine);
                     }
                 }
             }
             List<ModelTriangle> neighbors = findNeighbors(t);
             n: for (ModelTriangle n : neighbors) {
                if (inQueue.contains(n))
                     continue;
 
                 for (Entry<Segment3d, Segment3d> overlap : overlaps.entrySet()) {
                     if (n.getS1().overlaps(overlap.getValue()) || n.getS2().overlaps(overlap.getValue())
                             || n.getS3().overlaps(overlap.getValue())) {
                         if (!halfspace.contains(n.getP1()) || !halfspace.contains(n.getP2())
                                 || !halfspace.contains(n.getP3()))
                             continue n;
                     }
                 }
 
                 queue.add(n);
                inQueue.add(n);
             }
         }
 
         Set<Layer> layersToRotate = new HashSet<Layer>();
 
         for (ModelTriangle tr : trianglesToRotate) {
             layersToRotate.add(trianglesToLayers.get(tr));
         }
 
         for (Layer l : layersToRotate) {
             // remove, rotate, and then add the triangles back to make sure all caches and maps will hold the correct
             // value
             triangles.removeAll(l.getTriangles());
             l.rotate(segment, angle1);
             triangles.addAll(l.getTriangles());
         }
         trianglesArrayDirty = true;
     }
 
     /**
      * Returns a list of triangles having a common point with the given triangle.
      * 
      * @param t The triangle to find neighbors to.
      * @return The list of neighbors of t.
      */
     protected List<ModelTriangle> findNeighbors(ModelTriangle triangle)
     {
         List<ModelTriangle> result = new LinkedList<ModelTriangle>();
 
         for (Segment3d edge : triangle.getEdges()) {
             Line3d line = new CanonicLine3d(edge);
             for (ModelTriangle t : neighbors.get(line)) {
                 if (t.hasCommonEdge(triangle, false)
                 // don't forget to check if the triangles are neighbors on the paper
                         && t.originalPosition.hasCommonEdge(triangle.originalPosition, false)) {
 
                     result.add(t);
                 }
             }
         }
 
         return result;
     }
 
     /**
      * Returns a list of triangles having a common point with the given triangle.
      * 
      * @param t The triangle to find neighbors to.
      * @return The list of neighbors of t.
      */
     protected List<Triangle2d> findNeighbors(Triangle2d triangle)
     {
         List<Triangle2d> result = new LinkedList<Triangle2d>();
 
         for (Segment2d edge : triangle.getEdges()) {
             Line2d line = new CanonicLine2d(edge);
             for (Triangle2d t : neighbors2d.get(line)) {
                 if (t.hasCommonEdge(triangle, false)) {
                     result.add(t);
                 }
             }
         }
 
         return result;
     }
 
     /**
      * Returns a sorted list of layers defined by the given segment.
      * 
      * A layer is a part of the paper surrounded by either fold lines or paper boundaries.
      * This function returns the layers that intersect with a stripe defined by the two given points and and that is
      * perpendicular to the layer the line lies in.
      * The list is sorted in the order the layers intersect with the stripe. The very first layer is the one that is the
      * closest to the viewer.
      * 
      * @param segment The segment we search layers for.
      * @return A list of layers defined by the given line.
      */
     protected List<Layer> getLayers(Segment3d segment)
     {
         List<Layer> result = new ArrayList<Layer>();
 
         // finds the top layer
         Point3d center = new Point3d(segment.getP1());
         center.add(segment.getP2());
         center.scale(0.5d);
 
         Layer firstLayer = getLayerForPoint(center);
         if (firstLayer == null)
             return result;
 
         // find another layers: is done by creating a stripe perpendicular to the first layer and finding intersections
         // of the stripe with triangles
 
         Vector3d stripeDirection = firstLayer.getNormal();
         Line3d p1line = new Line3d(segment.getP1(), stripeDirection);
         Line3d p2line = new Line3d(segment.getP2(), stripeDirection);
 
         Stripe3d stripe = new Stripe3d(p1line, p2line);
 
         for (Layer l : layers) {
             Line3d stripePlaneAndLayerPlaneInt = stripe.getPlane().getIntersection(l.getPlane());
             if (stripePlaneAndLayerPlaneInt == null)
                 continue; // the stripe and the layer are parallel
 
             Point3d segmentPoint1 = stripe.getHalfspace1().getPlane().getIntersection(stripePlaneAndLayerPlaneInt);
             Point3d segmentPoint2 = stripe.getHalfspace2().getPlane().getIntersection(stripePlaneAndLayerPlaneInt);
 
             Segment3d intersectionSegment = new Segment3d(segmentPoint1, segmentPoint2);
             List<Segment3d> layerInts = l.getIntersections(intersectionSegment);
             if (layerInts.size() > 0)
                 result.add(l);
         }
 
         // sort the layers so that the first one will be the one nearest to the viewer
         // assuming that layers don't intersect, one can compare the order of layers by comparing the order of any two
         // triangles from the layers
         Collections.sort(result, new Comparator<Layer>() {
 
             /** The axis from the user's display to the center point. */
             private Line3d axis = null;
 
             {
                 Matrix3d transform = new Matrix3d();
 
                 double a1 = viewingAngle - Math.PI / 2.0;
                 double a2 = 0;
                 double a3 = rotationAngle;
 
                 double c1 = cos(a1);
                 double c2 = cos(a2);
                 double c3 = cos(a3);
 
                 double s1 = sin(a1);
                 double s2 = sin(a2);
                 double s3 = sin(a3);
 
                 // see http://en.wikipedia.org/wiki/Euler_angles
                 transform.setRow(0, c2 * c3, -c2 * s3, s2);
                 transform.setRow(1, c1 * s3 + c3 * s1 * s2, c1 * c3 - s1 * s2 * s3, -c2 * s1);
                 transform.setRow(2, s1 * s3 - c1 * c3 * s2, c1 * s2 * s3 + c3 * s1, c1 * c2);
 
                 Matrix3d invTransform = new Matrix3d();
                 invTransform.invert(transform);
 
                 Vector3d axisDir = new Vector3d();
                 invTransform.transform(new Vector3d(0, 0, 1), axisDir);
 
                 axis = new Line3d(new Point3d(), axisDir);
             }
 
             @Override
             public int compare(Layer o1, Layer o2)
             {
                 Point3d int1 = o1.getPlane().getIntersection(axis);
                 Point3d int2 = o2.getPlane().getIntersection(axis);
 
                 double t1 = axis.getParameterForPoint(int1);
                 double t2 = axis.getParameterForPoint(int2);
                 return (t1 - t2 > EPSILON) ? 1 : (t1 - t2 < -EPSILON ? -1 : 0);
             }
         });
 
         return result;
     }
 
     /**
      * Return the layer that contains the given point.
      * 
      * @param point The point to find layer for.
      * @return The layer that contains the given point.
      */
     protected Layer getLayerForPoint(Point3d point)
     {
         for (Layer l : layers) {
             if (l.contains(point))
                 return l;
         }
         Logger.getLogger(getClass()).warn("getLayerForPoint: cannot find layer for point " + point);
         return null;
     }
 
     /**
      * Given all triangles in one layer, divides all triangles in the layer by the given line to smaller triangles.
      * 
      * @param layer The triangles in the layer with the appropriate intersection points.
      * @param direction The direction of the created fold.
      * @param segment The segment defining the fold.
      * 
      * @return The intersections of the given segment with the layer.
      */
     protected List<Segment3d> makeFoldInLayer(Layer layer, Direction direction, Segment3d segment)
     {
         List<IntersectionWithTriangle<ModelTriangle>> intersections = layer.getIntersectionsWithTriangles(segment);
 
         for (IntersectionWithTriangle<ModelTriangle> intersection : intersections) {
             if (intersection == null) {
                 // no intersection with the triangle - something's weird (we loop over intersections with triangles)
                 throw new IllegalStateException(
                         "Invalid diagram: no intersection found in IntersectionWithTriangle in step " + step.getId());
             }
 
             List<ModelTriangle> newTriangles = layer.subdivideTriangle(intersection);
             if (newTriangles.size() > 1) {
                 triangles.remove(intersection.triangle);
                 triangles.addAll(newTriangles);
             }
         }
 
         List<Segment3d> foldLines = layer.joinNeighboringSegments(intersections);
         List<Segment2d> foldLines2d = new LinkedList<Segment2d>();
         ModelTriangle triangle = layer.getTriangles().iterator().next();
         for (Segment3d fold : foldLines) {
             foldLines3d.add(fold);
 
             // barycentric coordinates can handle even points outside the triangle, so it doesn't matter which triangle
             // we choose - but it is important that all the triangles lie in one plane
             Vector3d bp1 = triangle.getBarycentricCoordinates(fold.getP1());
             Vector3d bp2 = triangle.getBarycentricCoordinates(fold.getP2());
 
             Point2d p1 = triangle.getOriginalPosition().interpolatePointFromBarycentric(bp1);
             Point2d p2 = triangle.getOriginalPosition().interpolatePointFromBarycentric(bp2);
 
             foldLines2d.add(new Segment2d(p1, p2));
         }
 
         Fold fold = new Fold();
         fold.originatingStepId = this.step.getId();
         for (Segment2d line : foldLines2d) {
             FoldLine fLine = new FoldLine();
             fLine.direction = direction;
             fLine.line = line;
             fold.lines.add(fLine);
         }
         folds.add(fold);
 
         return foldLines;
     }
 
     /**
      * Adds the given angle to the current angle of rotation.
      * 
      * @param rotation The angle to add (in radians).
      */
     public void addRotation(double rotation)
     {
         setRotation(rotationAngle + rotation);
     }
 
     /**
      * Sets the current angle of rotation to the given value.
      * 
      * @param rotation The angle to set (in radians).
      */
     public void setRotation(double rotation)
     {
         rotationAngle = rotation;
 
         while (rotationAngle > Math.PI)
             rotationAngle -= Math.PI;
         while (rotationAngle < -Math.PI)
             rotationAngle += Math.PI;
     }
 
     /**
      * Return the rotation of the paper.
      * 
      * @return The rotation of the paper (in radians).
      */
     public double getRotation()
     {
         return rotationAngle;
     }
 
     /**
      * Adds the given angle to the current viewing angle of the paper.
      * 
      * The angle will be "cropped" to <-PI/2,PI/2> interval.
      * 
      * @param angle The angle to add (in radians).
      */
     public void addViewingAngle(double angle)
     {
         setViewingAngle(viewingAngle + angle);
     }
 
     /**
      * Changes the viewing angle from top to bottom and vice versa.
      */
     public void flipViewingAngle()
     {
         setViewingAngle(-viewingAngle);
     }
 
     /**
      * Sets the current viewing angle to the given value.
      * 
      * The angle will be "cropped" to <-PI/2,PI/2> interval.
      * 
      * @param angle The angle to set (in radians).
      */
     public void setViewingAngle(double angle)
     {
         viewingAngle = angle;
 
         if (viewingAngle > Math.PI / 2.0)
             viewingAngle = Math.PI / 2.0;
         if (viewingAngle < -Math.PI / 2.0)
             viewingAngle = -Math.PI / 2.0;
     }
 
     /**
      * Get the current viewing angle.
      * 
      * @return The viewing angle (in radians).
      */
     public double getViewingAngle()
     {
         return viewingAngle;
     }
 
     @Override
     public ModelState clone() throws CloneNotSupportedException
     {
         ModelState result = new ModelState();
 
         result.step = this.step;
         result.origami = this.origami;
         result.rotationAngle = this.rotationAngle;
         result.viewingAngle = this.viewingAngle;
         result.stepBlendingTreshold = this.stepBlendingTreshold;
 
         for (Fold fold : folds)
             result.folds.add((Fold) fold.clone());
 
         for (Triangle3d t : triangles)
             result.triangles.add((ModelTriangle) t.clone());
         result.trianglesArrayDirty = true;
 
         for (Layer l : layers)
             result.layers.add(l);
 
         return result;
     }
 }
