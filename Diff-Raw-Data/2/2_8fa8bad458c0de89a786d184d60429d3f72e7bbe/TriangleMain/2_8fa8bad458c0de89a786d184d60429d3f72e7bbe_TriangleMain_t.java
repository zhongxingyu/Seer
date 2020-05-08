 package main;
 
 import geometry.point.ColoredPoint;
 import geometry.polygon.ColoredPolygon;
 import util.io.Reader;
 import util.io.Writer;
 import search.TriangleSearch;
 
 import java.io.IOException;
 import java.text.ParseException;
 import java.util.List;
 
 /**
  * Main class for starting up the point-disjoint triangle search.
  *
  * @author Kim-Anh Tran
  */
 public class TriangleMain {
 
     /**
      * Reads in 2d points from std:in, searches for triangles and writes them to std:out.
      *
      * @param args // None
      */
     public static void main(String[] args) {
         List<ColoredPoint> points;
 
         try {
             points = Reader.readPoints(System.in);
 
             List<ColoredPolygon> triangles;
             triangles = new TriangleSearch(points).searchForTriangles();
 
             Writer.writeTriangles(System.out, triangles);
 
         } catch (ParseException e) {
            System.err.println(e.getMessage() + "Occurred at parsing point number " + e.getErrorOffset());
             System.exit(1);
 
         } catch (IOException e) {
             System.err.println("Writing triangles to std:out failed unexpected.");
             System.exit(1);
         }
     }
 }
