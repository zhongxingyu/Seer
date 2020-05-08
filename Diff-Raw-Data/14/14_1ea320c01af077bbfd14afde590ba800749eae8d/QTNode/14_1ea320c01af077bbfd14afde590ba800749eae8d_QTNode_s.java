 package com.quadtree.clustering;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Queue;
 
 /**
  * @author colriot
  * @since Jul 9, 2012
  * 
  */
 public class QTNode {
     private static final String TAG = "QTNode";
 
     public static final int DEFAULT_CHILDREN_COUNT = 4;
 
     public static final int DEFAULT_POINTS_COUNT_THRESHOLD = 6;
     private static final int MAX_PARENT_NODES_COUNT = 4;
 
     private static final int MIN_COORD_SPAN = 500;
 
     public static final GeoRect WHOLE_WORLD = new GeoRect(new GeoPointInternal(-180000000, -90000000),
             new GeoPointInternal(180000000, 90000000));
 
 
     private QTNode[] children;
     private Collection<IGeoPoint> points = new ArrayList<IGeoPoint>();
 
     private GeoRect boundBox;
 
     private long avgX, avgY;
     private int count;
 
     private GeoCluster cluster;
 
     private final int MAX_POINTS;
 
     /**
      * Constructs tree with the whole world in it's root from given collection of points.
      * 
      * @param pts
      *            points to construct quad-tree
      */
     public QTNode(Collection<? extends IGeoPoint> pts) {
         this(pts, WHOLE_WORLD, DEFAULT_POINTS_COUNT_THRESHOLD);
     }
 
     /**
      * @param boundingBox
      *            bounding box of constructed node
      * @param maxPoints
      *            maximum number of points in a leaf-node
      */
     public QTNode(GeoRect boundingBox, int maxPoints) {
         boundBox = boundingBox;
         MAX_POINTS = maxPoints;
     }
 
     /**
      * @param pts
      *            collection of geopoint
      * @param maxPoints
      *            maximum number of points in a leaf-node
      */
     public QTNode(Collection<? extends IGeoPoint> pts, int maxPoints) {
         this(pts, WHOLE_WORLD, maxPoints);
     }
 
     /**
      * Generic quad-tree constructor
      * 
      * @param pts
      *            collection of geopoint
      * @param boundingBox
      *            bounding box of constructed node
      * @param maxPoints
      *            maximum number of points in a leaf-node
      */
     public QTNode(Collection<? extends IGeoPoint> pts, GeoRect boundingBox, int maxPoints) {
         boundBox = boundingBox;
         MAX_POINTS = maxPoints;
 
         populate(pts);
     }
 
     public void insertAll(Collection<? extends IGeoPoint> points) {
         for (IGeoPoint point : points) {
             insert(point);
         }
     }
 
     /**
      * Inserts given point into quad-tree
      * 
      * @param p
      *            point for insertion
      */
     public void insert(IGeoPoint p) {
         if (!boundBox.contains(p)) {
             throw new IllegalArgumentException("Bounding box " + boundBox + " does not containt point: " + p);
         }
 
         avgX += p.getLng();
         avgY += p.getLat();
         count++;
 
         updateCluster();
 
         if (children == null) {
             points.add(p);
             if (points.size() > MAX_POINTS) {
                 split();
             }
         } else {
             for (QTNode child : children) {
                 if (child.boundBox.contains(p)) {
                     child.insert(p);
                 }
             }
         }
     }
 
     /**
      * Splits current node into four subnodes
      */
     private void split() {
 //        int cX = (int) (avgX / count);
 //        int cY = (int) (avgY / count);
         int cX = (boundBox.tR.x + boundBox.bL.x) / 2;
         int cY = (boundBox.tR.y + boundBox.bL.y) / 2;
         split(cX, cY);
     }
 
     private void split(int cX, int cY) {
         children = new QTNode[DEFAULT_CHILDREN_COUNT];
 
         children[0] = new QTNode(new GeoRect(boundBox.bL.x, cY, cX, boundBox.tR.y), MAX_POINTS);
         children[1] = new QTNode(new GeoRect(cX, cY, boundBox.tR.x, boundBox.tR.y), MAX_POINTS);
         children[2] = new QTNode(new GeoRect(cX, boundBox.bL.y, boundBox.tR.x, cY), MAX_POINTS);
         children[3] = new QTNode(new GeoRect(boundBox.bL.x, boundBox.bL.y, cX, cY), MAX_POINTS);
 
         @SuppressWarnings("unchecked")
         List<IGeoPoint>[] childrenPoints = new ArrayList[DEFAULT_CHILDREN_COUNT];
         for (int i = 0; i < childrenPoints.length; i++) {
             childrenPoints[i] = new ArrayList<IGeoPoint>();
         }
 
         for (IGeoPoint p : points) {
             childrenPoints[getQuadrantIndex(p)].add(p);
         }
 
         children[0].populate(childrenPoints[0]);
         children[1].populate(childrenPoints[1]);
         children[2].populate(childrenPoints[2]);
         children[3].populate(childrenPoints[3]);
 
         points = null;
     }
 
     @SuppressWarnings("unchecked")
     private void populate(Collection<? extends IGeoPoint> pts) {
         points = (Collection<IGeoPoint>) pts;
         for (IGeoPoint p : points) {
             avgX += p.getLng();
             avgY += p.getLat();
         }
         count = points.size();
 
         //if (points.size() > MAX_POINTS/* && boundBox.tR.x - boundBox.bL.x > MIN_COORD_SPAN
         //                              && boundBox.tR.y - boundBox.bL.y > MIN_COORD_SPAN*/) {
         updateCluster();
         if (points.size() > MAX_POINTS
                 && (boundBox.getLatSpan() > MIN_COORD_SPAN ||
                     boundBox.getLngSpan() > MIN_COORD_SPAN)) {
             split();
         }
     }
 
     private int getQuadrantIndex(IGeoPoint p) {
         for (int i = 0; i < children.length; i++) {
             if (children[i].boundBox.contains(p)) {
                 return i;
             }
         }
         return 0;
     }
 
    public Collection<IGeoPoint> getNearestPoints(int maxCount, IGeoPoint where) {
        Collection<IGeoPoint> result = new ArrayList<IGeoPoint>(maxCount);
        QTNode node = findNodeWithNPoints(maxCount, where);
         if (node != null) {
             node.collectPoints(result);
         }
         return result;
     }
 
     private void collectPoints(Collection<IGeoPoint> out) {
        out.addAll(points);
         if (children != null) {
             for (QTNode child : children) {
                 child.collectPoints(out);
             }
         }
     }
 
     private QTNode findNodeWithNPoints(int count, IGeoPoint point) {
         if (children != null) {
             for (int i = 0; i < children.length; i++) {
                 if (children[i].boundBox.contains(point)) {
                     QTNode res = children[i];
                     if (res.count > count) {
                         QTNode subRes = res.findNodeWithNPoints(count, point);
                        if (subRes != null) {
                             res = subRes;
                         }
                     }
                     return res;
                 }
             }
         }
         return null;
     }
 
     public Collection<? extends IGeoPoint> query(GeoRect range) {
         Queue<QTNode> queue = new LinkedList<QTNode>();
         List<QTNode> result = new ArrayList<QTNode>();
         List<QTNode> buffer = new ArrayList<QTNode>();
 
         queue.offer(this);
 
         while (!queue.isEmpty() && !Thread.currentThread().isInterrupted()) {
             QTNode node = queue.poll();
 
             if (node.children == null) {
                 result.add(node);
             } else {
                 for (QTNode child : node.children) {
                     if (!child.isEmpty() && range.intersects(child.boundBox)) {
                         buffer.add(child);
                     }
                 }
             }
             if (!queue.isEmpty()) {
                 continue;
             }
 
             if (range.getLngSpan() < node.boundBox.getLngSpan() && range.getLatSpan() < node.boundBox.getLatSpan()) {
                 queue.addAll(buffer);
             } else {
                 result.addAll(buffer);
             }
             buffer.clear();
         }
 
 //        if (BuildConfig.DEBUG) {
 //            Log.d(TAG, "search res for " + range + ":\n" + result);
 //        }
         List<IGeoPoint> res = new ArrayList<IGeoPoint>();
         for (QTNode node : result) {
             res.addAll(node.getSuccessors(range));
         }
 //        if (BuildConfig.DEBUG) {
 //            Log.d(TAG, "final res for " + range + ":\n" + result);
 //        }
 
         return res;
     }
 
     /**
      * @return successors (points or clusters) within given bounding box
      */
     private Collection<? extends IGeoPoint> getSuccessors(GeoRect rect) {
         List<IGeoPoint> res = new ArrayList<IGeoPoint>();
         if (children != null) {
             for (QTNode cluster : children) {
                 if (!cluster.isEmpty() && rect.intersects(cluster.boundBox)) {
                     if (cluster.count == 1) {
                         res.addAll(cluster.points);
                     } else {
                         res.add(cluster.getCluster());
                     }
                 }
             }
         } else {
             res.addAll(points);
         }
         return res;
     }
 
     public boolean isEmpty() {
         return children == null && points.isEmpty();
     }
 
     /**
      * @return cluster representation of current node
      */
     private GeoCluster getCluster() {
         return cluster;
     }
 
     private void updateCluster() {
         if (count > 0) {
             this.cluster = new GeoCluster((int) (avgX / count), (int) (avgY / count), count);
         }
 //        int cX = (boundBox.tR.x + boundBox.bL.x) / 2;
 //        int cY = (boundBox.tR.y + boundBox.bL.y) / 2;
 //        return new GeoCluster(cX, cY, count);
     }
 
     @Override
     public String toString() {
         StringBuilder sb = new StringBuilder("Node[\n\tbBox: ").append(boundBox).append("\n\t");
 
         if (children != null) {
             sb.append("Children:\n");
             for (QTNode child : children) {
                 sb.append(child).append('\n');
             }
         } else {
             sb.append("Points:\n");
             for (IGeoPoint p : points) {
                 sb.append('\t').append(p).append('\n');
             }
         }
 
         return sb.toString();
     }
 }
