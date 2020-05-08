 package kdtree;
 
 import geometry.BoundingBox;
 import geometry.GeometryException;
 import geometry.Point;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 public class KDTreeCell {
     private Integer splitDimension;
     private Float split = 0.0f;
     private Integer dimensions;
     private Integer depth;
     private Integer count = 0;
     private Point sum = null;
     private KDTreeCell[] children = null;
     private Point point = null;
     private BoundingBox cell;
 
     public KDTreeCell(Integer dimensions, Integer depth,
             ArrayList<ArrayList<Point>> points, Point min, Point max) throws KDTreeCellException {
         if (dimensions <= 0)
             throw new KDTreeCellWrongCreationArgumentException();
         if (depth < 0)
             throw new KDTreeCellWrongCreationArgumentException();
         if (points.size() != dimensions)
             throw new KDTreeCellWrongCreationArgumentException();
         if (min == null || max == null)
             throw new KDTreeCellWrongCreationArgumentException();
         try { this.cell = new BoundingBox(min, max); }
         catch (GeometryException ge) { throw new KDTreeCellWrongCreationArgumentException(); }
         this.dimensions = dimensions;
         this.splitDimension = depth % dimensions;
         this.depth = depth;
         if (points != null) {
             if (points.get(0).size() > 2) {
                 ArrayList<ArrayList<Point>> leftPoints = new ArrayList<ArrayList<Point>>();
                 ArrayList<ArrayList<Point>> rightPoints = new ArrayList<ArrayList<Point>>();
                 ArrayList<Point> master = points.get(this.splitDimension);
                 Integer medianID = master.size()/2;
                 try {
                     while (medianID > 0) {
                         if (master.get(medianID-1).getCoordinate(this.splitDimension) >= master.get(medianID).getCoordinate(this.splitDimension)) medianID--;
                         else break;
                     }
                     this.split = master.get(medianID).getCoordinate(this.splitDimension);
                 } catch (GeometryException ge) {throw new KDTreeCellWrongPointCoordinatesException(); }
                 for (int i = 0; i < this.dimensions; i++) {
                     ArrayList<Point> left = new ArrayList<Point>();
                     ArrayList<Point> right = new ArrayList<Point>();
                     if (i == this.splitDimension) {
                         left.addAll(master.subList(0, medianID));
                         right.addAll(master.subList(medianID, master.size()));
                     } else {
                         ArrayList<Point> slave = points.get(i);
                         try {
                             for (Iterator<Point> it = slave.iterator(); it.hasNext();) {
                                 Point v = it.next();
                                 Integer j = -1;
                                 if (v.getCoordinate(this.splitDimension) < this.split)
                                     left.add(v);
                                 else right.add(v);
                             }
                         } catch (GeometryException ge) { throw new KDTreeCellWrongPointCoordinatesException(); }
                     }
                     leftPoints.add(left);
                     rightPoints.add(right);
                 }
                 Float[] leftNewMax = this.cell.getMaxPoint().getCoords();
                 Float[] rightNewMin = this.cell.getMinPoint().getCoords();
                 Integer newSplit = (depth + 1) % dimensions;
                 leftNewMax[newSplit] -= (leftNewMax[newSplit]-rightNewMin[newSplit])/2;
                 rightNewMin[newSplit] = leftNewMax[newSplit];
                 Point newMax = null;
                 Point newMin = null;
                 try {
                     newMax = new Point(leftNewMax);
                     newMin = new Point(rightNewMin);
                 } catch (GeometryException ge) {}
                 this.children = new KDTreeCell[2];
                 this.children[0] = new KDTreeCell(dimensions, depth+1, leftPoints, this.cell.getMinPoint(), newMax);
                 this.children[1] = new KDTreeCell(dimensions, depth+1, rightPoints, newMin, this.cell.getMaxPoint());
                 this.count = points.get(0).size();
                 try {
                    this.sum = this.children[0].getSum().transposedPoint(this.children[1].getSum());
                 } catch (GeometryException ge) {}
             } else switch (points.get(0).size()) {
                 case 2:
                     ArrayList<ArrayList<Point>> leftPoints = new ArrayList<ArrayList<Point>>();
                     ArrayList<ArrayList<Point>> rightPoints = new ArrayList<ArrayList<Point>>();
                     for (int i = 0; i < this.dimensions; i++) {
                         ArrayList<Point> temp = new ArrayList<Point>();
                         ArrayList<Point> temp2 = new ArrayList<Point>();
                         temp.add(points.get(0).get(0));
                         temp2.add(points.get(0).get(1));
                         leftPoints.add(temp);
                         rightPoints.add(temp2);
                     }
                     Float[] leftNewMax = this.cell.getMaxPoint().getCoords();
                     Float[] rightNewMin = this.cell.getMinPoint().getCoords();
                     Integer newSplit = (depth + 1) % dimensions;
                     leftNewMax[newSplit] -= (leftNewMax[newSplit]-rightNewMin[newSplit])/2;
                     rightNewMin[newSplit] = leftNewMax[newSplit];
                     Point newMax = null;
                     Point newMin = null;
                     try {
                         newMax = new Point(leftNewMax);
                         newMin = new Point(rightNewMin);
                         this.sum = points.get(0).get(0).transposedPoint(points.get(0).get(1));
                     } catch (GeometryException ge) {}
                     this.children = new KDTreeCell[2];
                     this.children[0] = new KDTreeCell(dimensions, depth+1, leftPoints, this.cell.getMinPoint(), newMax);
                     this.children[1] = new KDTreeCell(dimensions, depth+1, rightPoints, newMin, this.cell.getMaxPoint());
                     this.count = 2;
                     break;
                 case 1:
                     this.point = points.get(0).get(0);
                     this.count = 1;
                     this.sum = this.point;
                     break;
                case 0:
                    try {
                        this.sum = Point.zeroPoint(this.dimensions);
                    } catch (GeometryException ge) {}
             }
         } else {
             this.children = null;
             this.point = null;
         }
     }
 
     public Boolean isLeafNode() { if (this.children == null) return true; else return false; }
     public Boolean isEmpty() { if (this.point == null) return true; else return false; }
     public BoundingBox getBoundingBox() { return this.cell; }
     public Point getCenter() { return this.cell.getCenter(); }
     public Point getPoint() { return this.point; }
     public Point getSum() { return this.sum; }
     public Point getWeightedSum() { return this.sum.scaledPoint(1.0f/this.count); }
     public Integer getPointCount() { return this.count; }
     public KDTreeCell getChild(Integer id) throws KDTreeCellException {
         if (this.children == null)
             throw new KDTreeCellInvalidCellTypeException();
         if (id < 0 || id > this.children.length)
         throw new KDTreeCellInvalidMethodArgumentException();
         return this.children[id];
     }
     
     public Integer getMaxDepth() {
         if (this.children == null) return this.depth;
         else return Math.max(this.children[0].getMaxDepth(), this.children[1].getMaxDepth());
     }
     
     public Integer getDepth() { return this.depth; }
 }
