 /**
  * 
  */
 package cz.cuni.mff.peckam.java.origamist.modelstate;
 
 import java.util.AbstractList;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.vecmath.Point2d;
 import javax.vecmath.Point3d;
 import javax.vecmath.Vector3d;
 
 import cz.cuni.mff.peckam.java.origamist.math.IntersectionWithTriangle;
 import cz.cuni.mff.peckam.java.origamist.math.MathHelper;
 import cz.cuni.mff.peckam.java.origamist.math.Segment3d;
 import cz.cuni.mff.peckam.java.origamist.math.Triangle2d;
 import cz.cuni.mff.peckam.java.origamist.math.Triangle3d;
 
 /**
  * A 3D triangle holding information about its coordinates in the original sheet of paper. Units for the points should
  * be the relative units.
  * 
  * @author Martin Pecka
  */
 public class ModelTriangle extends Triangle3d
 {
 
     /**
      * The original position of this triangle on the sheet of paper.
      */
     protected Triangle2d     originalPosition;
 
     /** The fold lines for the first edge of the triangle. */
     protected List<FoldLine> s1FoldLines = null;
 
     /** The fold lines for the second edge of the triangle. */
     protected List<FoldLine> s2FoldLines = null;
 
     /** The fold lines for the third edge of the triangle. */
     protected List<FoldLine> s3FoldLines = null;
 
     /**
      * @param p1
      * @param p2
      * @param p3
      * @param o The original position on the sheet of paper.
      */
     public ModelTriangle(Point3d p1, Point3d p2, Point3d p3, Triangle2d o)
     {
         super(p1, p2, p3);
         originalPosition = o;
     }
 
     /**
      * @param p1
      * @param p2
      * @param p3
      * @param o1 The first vertex of the original position on the sheet of paper.
      * @param o2 The second vertex of the original position on the sheet of paper.
      * @param o3 The third vertex of the original position on the sheet of paper.
      */
     public ModelTriangle(Point3d p1, Point3d p2, Point3d p3, Point2d o1, Point2d o2, Point2d o3)
     {
         this(p1, p2, p3, new Triangle2d(o1, o2, o3));
     }
 
     /**
      * @param p1
      * @param p2
      * @param p3
      * @param o1x x-coordinate of the first vertex of the original position on the sheet of paper.
      * @param o1y y-coordinate of the first vertex of the original position on the sheet of paper.
      * @param o2x x-coordinate of the second vertex of the original position on the sheet of paper.
      * @param o2y y-coordinate of the second vertex of the original position on the sheet of paper.
      * @param o3x x-coordinate of the third vertex of the original position on the sheet of paper.
      * @param o3y y-coordinate of the third vertex of the original position on the sheet of paper.
      */
     public ModelTriangle(Point3d p1, Point3d p2, Point3d p3, double o1x, double o1y, double o2x, double o2y,
             double o3x, double o3y)
     {
         this(p1, p2, p3, new Triangle2d(o1x, o1y, o2x, o2y, o3x, o3y));
     }
 
     /**
      * @param p1x
      * @param p1y
      * @param p1z
      * @param p2x
      * @param p2y
      * @param p2z
      * @param p3x
      * @param p3y
      * @param p3z
      * @param o The original position on the sheet of paper.
      */
     public ModelTriangle(double p1x, double p1y, double p1z, double p2x, double p2y, double p2z, double p3x,
             double p3y, double p3z, Triangle2d o)
     {
         this(new Point3d(p1x, p1y, p1z), new Point3d(p2x, p2y, p2z), new Point3d(p3x, p3y, p3z), o);
     }
 
     /**
      * @param p1x
      * @param p1y
      * @param p1z
      * @param p2x
      * @param p2y
      * @param p2z
      * @param p3x
      * @param p3y
      * @param p3z
      * @param o1 The first vertex of the original position on the sheet of paper.
      * @param o2 The second vertex of the original position on the sheet of paper.
      * @param o3 The third vertex of the original position on the sheet of paper.
      */
     public ModelTriangle(double p1x, double p1y, double p1z, double p2x, double p2y, double p2z, double p3x,
             double p3y, double p3z, Point2d o1, Point2d o2, Point2d o3)
     {
         this(new Point3d(p1x, p1y, p1z), new Point3d(p2x, p2y, p2z), new Point3d(p3x, p3y, p3z), o1, o2, o3);
     }
 
     /**
      * @param p1x
      * @param p1y
      * @param p1z
      * @param p2x
      * @param p2y
      * @param p2z
      * @param p3x
      * @param p3y
      * @param p3z
      * @param o1x x-coordinate of the first vertex of the original position on the sheet of paper.
      * @param o1y y-coordinate of the first vertex of the original position on the sheet of paper.
      * @param o2x x-coordinate of the second vertex of the original position on the sheet of paper.
      * @param o2y y-coordinate of the second vertex of the original position on the sheet of paper.
      * @param o3x x-coordinate of the third vertex of the original position on the sheet of paper.
      * @param o3y y-coordinate of the third vertex of the original position on the sheet of paper.
      */
     public ModelTriangle(double p1x, double p1y, double p1z, double p2x, double p2y, double p2z, double p3x,
             double p3y, double p3z, double o1x, double o1y, double o2x, double o2y, double o3x, double o3y)
     {
         this(new Point3d(p1x, p1y, p1z), new Point3d(p2x, p2y, p2z), new Point3d(p3x, p3y, p3z), o1x, o1y, o2x, o2y,
                 o3x, o3y);
     }
 
     /**
      * Sets the original position of this triangle on the sheet of paper.
      * 
      * @param o The original 2D position.
      */
     public void setOriginalPosition(Triangle2d o)
     {
         this.originalPosition = o;
     }
 
     /**
      * Sets the original position of this triangle on the sheet of paper.
      * 
      * @param o1 The first vertex of the original 2D position.
      * @param o2 The second vertex of the original 2D position.
      * @param o3 The third vertex of the original 2D position.
      */
     public void setOriginalPosition(Point2d o1, Point2d o2, Point2d o3)
     {
         this.originalPosition = new Triangle2d(o1, o2, o3);
     }
 
     /**
      * Sets the original position of this triangle on the sheet of paper.
      * 
      * @param o1x x-coordinate of the first vertex of the original 2D position.
      * @param o1y x-coordinate of the first vertex of the original 2D position.
      * @param o2x y-coordinate of the second vertex of the original 2D position.
      * @param o2y x-coordinate of the second vertex of the original 2D position.
      * @param o3x y-coordinate of the third vertex of the original 2D position.
      * @param o3y x-coordinate of the third vertex of the original 2D position.
      */
     public void setOriginalPosition(double o1x, double o1y, double o2x, double o2y, double o3x, double o3y)
     {
         this.originalPosition = new Triangle2d(o1x, o1y, o2x, o2y, o3x, o3y);
     }
 
     /**
      * Returns the original position of this triangle on the sheet of paper.
      * 
      * @return The original position of this triangle on the sheet of paper.
      */
     public Triangle2d getOriginalPosition()
     {
         return originalPosition;
     }
 
     @Override
     public List<ModelTriangle> getNeighbors()
     {
         return new AbstractList<ModelTriangle>() {
             @Override
             public ModelTriangle get(int index)
             {
                 return (ModelTriangle) neighborsRO.get(index);
             }
 
             @Override
             public int size()
             {
                 return neighborsRO.size();
             }
         };
     }
 
     /**
      * @return The raw (modifiable) list of neighboring triangles.
      */
     List<ModelTriangle> getRawNeighbors()
     {
         return new AbstractList<ModelTriangle>() {
             @Override
             public ModelTriangle get(int index)
             {
                 return (ModelTriangle) neighbors.get(index);
             }
 
             @Override
             public int size()
             {
                 return neighbors.size();
             }
 
             @Override
             public ModelTriangle set(int index, ModelTriangle element)
             {
                 return (ModelTriangle) neighbors.set(index, element);
             }
 
             @Override
             public void add(int index, ModelTriangle element)
             {
                 neighbors.add(index, element);
             }
 
             @Override
             public ModelTriangle remove(int index)
             {
                 return (ModelTriangle) neighbors.remove(index);
             }
 
         };
 
     }
 
     @Override
     protected Triangle3d createSubtriangle(Point3d p1, Point3d p2, Point3d p3)
     {
         // recompute also the original position triangle
         Vector3d bp1 = getBarycentricCoordinates(p1);
         Vector3d bp2 = getBarycentricCoordinates(p2);
         Vector3d bp3 = getBarycentricCoordinates(p3);
 
         Point2d pp1 = originalPosition.interpolatePointFromBarycentric(bp1);
         Point2d pp2 = originalPosition.interpolatePointFromBarycentric(bp2);
         Point2d pp3 = originalPosition.interpolatePointFromBarycentric(bp3);
 
         return new ModelTriangle(p1, p2, p3, new Triangle2d(pp1, pp2, pp3));
     }
 
     /**
      * Return the fold lines for the edge with the given index.
      * 
      * @param i The index of the edge. An integer from 0 to 2.
      * @return The fold lines for the edge with the given index.
      */
     protected List<FoldLine> getFoldLines(int i)
     {
         switch (i) {
             case 0:
                 return s1FoldLines;
             case 1:
                 return s2FoldLines;
             case 2:
                 return s3FoldLines;
             default:
                 throw new ArrayIndexOutOfBoundsException();
         }
     }
 
     /**
      * Sets the fold lines for the edge with the given index.
      * 
      * @param i The index of the edge. An integer from 0 to 2.
      * @param lines The new list of fold lines.
      */
     protected void setFoldLines(int i, List<FoldLine> lines)
     {
         switch (i) {
             case 0:
                 s1FoldLines = lines;
                 break;
             case 1:
                 s2FoldLines = lines;
                 break;
             case 2:
                 s3FoldLines = lines;
                 break;
             default:
                 throw new ArrayIndexOutOfBoundsException();
         }
     }
 
     @Override
     public <T extends Triangle3d> List<T> subdivideTriangle(IntersectionWithTriangle<T> segment)
             throws IllegalArgumentException
     {
         List<T> result = super.subdivideTriangle(segment);
 
         // subdivide also foldLines
         if (result.size() > 1 && result.get(0) instanceof ModelTriangle) {
             int i = 0;
             for (Segment3d edge : getEdges()) {
                 List<FoldLine> lines = getFoldLines(i);
                 if (lines != null && lines.size() > 0) {
                     loop: for (T t : result) {
                         ModelTriangle mt = (ModelTriangle) t;
                         int j = 0;
                         for (Segment3d tEdge : t.getEdges()) {
                             Segment3d intersection = edge.getIntersection(tEdge);
                             if (intersection != null
                                     && !intersection.getVector().epsilonEquals(new Vector3d(), MathHelper.EPSILON)) {
 
                                 List<FoldLine> newLines = mt.getFoldLines(j);
                                 if (newLines == null) {
                                     mt.setFoldLines(j, new LinkedList<FoldLine>());
                                     newLines = mt.getFoldLines(j);
                                 }
 
                                 ModelTriangleEdge newEdge = new ModelTriangleEdge(mt, j);
                                 for (FoldLine line : lines) {
                                     FoldLine newLine;
                                     try {
                                         newLine = line.clone();
                                     } catch (CloneNotSupportedException e) {
                                         assert false : "FoldLine doesn't support clone().";
                                         throw new IllegalStateException("FoldLine doesn't support clone().");
                                     }
                                     newLine.setLine(newEdge);
                                     newLine.getFold().getLines().add(newLine);
                                     newLines.add(newLine);
                                 }
 
                                 continue loop;
                             }
                             j++;
                         }
                     }
 
                     for (FoldLine line : lines) {
                         line.getFold().getLines().remove(line);
                     }
                     setFoldLines(i, null);
                 }
                 i++;
             }
         }
 
         return result;
     }
 
     /**
      * <p>
      * <b>The lists of fold lines belonging to edges are cloned, but the Folds the FoldLines belong to aren't changed to
      * reference the newly created fold lines (but the newly created fold lines reference the same parent folds as the
      * source ones).</b>
      * </p>
      * 
      * {@inheritDoc}
      */
     @Override
     public ModelTriangle clone() throws CloneNotSupportedException
     {
         ModelTriangle result = new ModelTriangle(getP1(), getP2(), getP3(), this.originalPosition.clone());
 
         for (int i = 0; i < 3; i++) {
             List<FoldLine> lines = getFoldLines(i);
             if (lines != null) {
                 result.setFoldLines(i, new LinkedList<FoldLine>());
                 List<FoldLine> newLines = result.getFoldLines(i);
                 for (FoldLine line : lines) {
                     FoldLine newLine = line.clone();
                     newLine.setLine(new ModelTriangleEdge(result, line.getLine().getIndex()));
                     newLines.add(newLine);
                 }
             }
         }
 
         result.neighbors.addAll(this.neighbors);
 
         return result;
     }
 
 }
