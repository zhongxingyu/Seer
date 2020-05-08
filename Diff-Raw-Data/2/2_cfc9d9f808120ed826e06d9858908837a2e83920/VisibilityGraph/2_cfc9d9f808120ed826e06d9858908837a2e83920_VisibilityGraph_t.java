 /*
  * A very simple visibility graph builder
  * Copyright (C) 2012 Matt Conway.
  * This is open-source software; if you modify it or redistribute it, please
 * place a comment referring to http://github.com/mattwigway/Visibility-Graph 
  * in your code.
  */
 
 import java.util.Scanner;
 import com.vividsolutions.jts.io.WKTReader;
 import com.vividsolutions.jts.io.WKTWriter;
 import com.vividsolutions.jts.geom.Geometry;
 import com.vividsolutions.jts.geom.Polygon;
 import com.vividsolutions.jts.geom.LineString;
 import com.vividsolutions.jts.geom.Coordinate;
 import com.vividsolutions.jts.geom.GeometryFactory;
 import com.vividsolutions.jts.io.ParseException;
 
 public class VisibilityGraph {
     
     /**
      * This sets up the variables and loads the shapefile
      * @param args not used
      */
     public static void main (String[] args) {
         String wktIn;
         Scanner in = new Scanner(System.in);
 
         // assumption is WKT is on one line
         wktIn = in.nextLine();
 
         // create the geometry
         GeometryFactory factory = new GeometryFactory();
         WKTReader reader = new WKTReader(factory);
         Geometry poly = null;
 
         try { 
             poly = reader.read(wktIn);
         } catch (ParseException e) {
             System.err.println("Error reading WKT; is it all on one line?");
             System.err.println(wktIn);
             System.exit(1);
         }
         
         // we'll need this later
         WKTWriter writer = new WKTWriter();
 
         Coordinate[] coords = poly.getCoordinates();
 
         System.err.println("Polygon has " + coords.length + " vertices.\n");
 
 
         long edges = 0;
 
         // store the time to see how long it takes
         long startTime = System.currentTimeMillis();
 
         for (Coordinate from : coords) {
             for (Coordinate to : coords) {
                 // no loop edges
                 if (from.equals(to)) break;
 
                 // only increment if not loop edge
                 edges++;
 
                 Coordinate[] edgeCoords = new Coordinate[] {from, to};
                 LineString edge = factory.createLineString(edgeCoords);
 
                 // determine if it lies within the polygon
                 if (poly.contains(edge))
                     System.out.println(writer.writeFormatted(edge));
 
                 if (edges % 1000 == 0)
                    System.err.println("edges: " + edges);
             }
         } // end nested loop
 
         long endTime = System.currentTimeMillis();
         long totalTime = endTime - startTime;
 
         System.err.println("Calculated visibility graph in " + totalTime + " ms");
     }
 }
