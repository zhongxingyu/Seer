 package com.ghostrun.model;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import com.ghostrun.util.GeoPointUtils;
 import com.google.android.maps.GeoPoint;
 
 public class Dots {
    private List<GeoPoint> items;
     
     public final double INCREMENT = 200.0;
     public final int EATING_DISTANCE = 200;
     public final int DOT_POINTS = 10;
 
     public Dots(MazeGraph mazeGraph) {
         setMazeGraph(mazeGraph);
     }
 
     class PointPair {
         MazeGraphPoint pt1, pt2;
         PointPair(MazeGraphPoint pt1, MazeGraphPoint pt2) {
             this.pt1 = pt1;
             this.pt2 = pt2;
         }
 
         public boolean equals(Object o) {
             if (o instanceof PointPair) {
                 PointPair p = (PointPair)o;
                 return (p.pt1.equals(this.pt1) && p.pt2.equals(this.pt2)) ||
                 (p.pt1.equals(this.pt2) && p.pt2.equals(this.pt1));
             }
             return false;
         }
         public int hashCode() {
             return 0;
         }
     }
 
     public void setMazeGraph(MazeGraph maze) {
         this.items = new ArrayList<GeoPoint>();
 
         Set<PointPair> doneSet = new HashSet<PointPair>();
         for (MazeGraphPoint p : maze.getPoints()) {
             //this.items.add(p.getLocation());
             for (MazeGraphPoint n : p.getNeighbors()) {
                 PointPair pair = new PointPair(p, n);
                 if (!doneSet.contains(pair)) {
                     //System.out.println("p: " + p.getLocation() + " n: " + n.getLocation());
                     //this.items.add(n.getLocation());
 
                     generateDotsAlongEdge(pair);
                     doneSet.add(pair);
                 }
             }
         }
 
 
         System.out.println("Done adding maze points: " + items.size());
     }
 
     public int eatDotsAt(GeoPoint playerLocation) {
         int pointIncrement = 0; 
         Iterator<GeoPoint> iter = items.iterator();
         while (iter.hasNext()) {
             GeoPoint pt = iter.next();
             //System.out.println("Distance: " + distance(playerLocation, pt));
             if ((int) GeoPointUtils.getDistance(playerLocation, pt) < EATING_DISTANCE) {
                 pointIncrement += DOT_POINTS;
                 iter.remove();
             }
         }
         return pointIncrement;
     }
 
     private int getDifference(double slope, double distance, int direction) {
         return (int)(direction * (Math.sqrt((distance * distance) / (slope * slope + 1))));
     }
 
     private void generateDotsAlongEdge(PointPair p) {
         //System.out.println("generating dots along the edge...");
         double slope = (double)(p.pt1.getLocation().getLatitudeE6() - p.pt2.getLocation().getLatitudeE6())/
         (double)(p.pt1.getLocation().getLongitudeE6() - p.pt2.getLocation().getLongitudeE6());
 
         GeoPoint pt1 = p.pt1.getLocation();
         GeoPoint pt2 = p.pt2.getLocation();
 
         GeoPoint curPoint = p.pt2.getLocation();
         int direction = (pt1.getLongitudeE6() > pt2.getLongitudeE6() ? 1 : -1);
 
         double totalDistance = GeoPointUtils.getDistance(p.pt2.getLocation(), p.pt1.getLocation());
         int times = (int)(totalDistance / INCREMENT);
         int difference = getDifference(slope, INCREMENT, direction);
 
         while (times > 0) {
             curPoint = new GeoPoint((int)(curPoint.getLatitudeE6() + slope * difference),
                     (int)(curPoint.getLongitudeE6() + difference));
             items.add(curPoint);
             times --;
         }
 
         System.out.println("added points: " + items.size());
     }

    public GeoPoint get(int i) {
        return items.get(i);
    }

    public int remaining() {
        return items.size();
    }
 }
