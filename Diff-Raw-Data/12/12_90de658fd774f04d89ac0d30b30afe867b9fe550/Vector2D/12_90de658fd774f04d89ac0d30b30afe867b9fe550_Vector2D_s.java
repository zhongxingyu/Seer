 package controller;
 
 import java.awt.*;
 
 public class Vector2D {
     final double x;
     final double y;
 
     public Vector2D(Point org, Point dst){
         this.x = dst.x-org.x;
         this.y = dst.y-org.y;
     }
 
     private Vector2D(double x, double y){
         this.x = x;
         this.y = y;
     }
 
     // constructs vector out of 2 integers
     public Vector2D(int x, int y){
         this.x = x;
         this.y = y;
     }
 
     // compares 2 vectors
     private static boolean equals(Vector2D a, Vector2D b){
        return (Math.abs(a.x-b.x)<0.00000000001 &&  Math.abs(a.y-b.y)<0.000000001); // threshold necessary because floats
     }
 
     // normalizes v and returns the normalized vectors
     private static Vector2D normalize(Vector2D v){
         double lA = Math.sqrt((v.x*v.x)+(v.y*v.y));
         double x=v.x/lA;
         double y=v.y/lA;
         return new Vector2D(x,y);
     }
 
     // checks if the vectors point in same direction
     // (first normalize the vectors and then compare them)
     public static boolean sameDirection(Vector2D a, Vector2D b){
 
         return equals(normalize(a),normalize(b));
     }
 }
