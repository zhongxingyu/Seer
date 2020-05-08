 package com.nateriver.app.other;
 
 import com.nateriver.app.models.Point;
 
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  *  Get polygon perimeter divisions
  */
 public class SplitPolygon {
     public static List<Point> split(List<Point> points, int n){
         double oneDistance = getPerimeter(points) /n;
         List<Point> res = new LinkedList<Point>();
 
         double target = oneDistance;
         for(int i = 0; i < points.size(); i++){
             Point cur = points.get(i);
            Point next = i+1 > points.size() ? points.get(0) : points.get(i+1);
 
             Point p = getPointBetweenPointAB(cur,next,target);
 
             while (p != null){
                target = oneDistance;
                res.add(p);
                p = getPointBetweenPointAB(p,next,target);
             }
 
             target = oneDistance - getTwoPointDistance(cur,next);
         }
 
         return res;
     }
 
     private static Point getPointBetweenPointAB(Point a, Point b, double distance){
         double abDistance = getTwoPointDistance(a,b);
         if(abDistance < distance)
             return null;
         else{
             double slope = getSlope(a,b);
            //TODO: bug
            return new Point(a.x + abDistance*slope,a.y + abDistance*slope);
         }
     }
 
     private static double getSlope(Point a, Point b){
        if(a.x == b.x)
            return 0.5;
        if(a.y == b.y)
            return 0;
         return (a.y -b.y)/(a.x - b.x);
     }
 
     private static double getPerimeter(List<Point> points){
         double res = 0.0;
         for(int i = 0; i < points.size();i++){
             int next = i + 1 ;
             if(next == points.size())
                 next = 0;
             res += getTwoPointDistance(points.get(i),points.get(next));
         }
 
         return res;
     }
 
     private static double getTwoPointDistance(Point a, Point b){
         return Math.sqrt(Math.pow(a.x- b.x,2) + Math.pow(a.y- b.y,2));
     }
 
     public static void main(String[] args) {
         List<Point> points = new LinkedList<Point>();
         points.add(new Point(0.0,0.0));
        points.add(new Point(0.0,5.0));
        points.add(new Point(5.0,0.0));
 
         List<Point> splitPoints = split(points,5);
 
         for(Point p : splitPoints){
             p.print();
         }
     }
 }
