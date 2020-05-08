 package org.terracotta.geospatial.coord;
 
 import com.esri.core.geometry.Envelope2D;
 import com.esri.core.geometry.QuadTree;
 
 
 public class LatLonCoordSystem implements CoordinateSystem {
 
     private final double xOffs = -180.0;
     private final double yOffs = -90;
     private final double width = 360.0;
     private final double height = 180.0;
 
     final Envelope2D westEnv = new Envelope2D(-180.0, -180.0, 0, 180.0);
     final QuadTree westQT = new QuadTree(westEnv, 32);
 
     final Envelope2D eastEnv = new Envelope2D(0.0, -180.0, 180.0, 180.0);
    final QuadTree eastQT = new QuadTree(eastEnv, 32);
 
     @Override
     public long computeGeoHash(Coordinate coord) {
         final LatLonCoordinate ll = (LatLonCoordinate) coord.as(Name.LAT_LON_DEG);
 
         final double x = ll.getLon();
         final double y = ll.getLat();
 
         final Envelope2D bbox = new Envelope2D();
         bbox.setCoords(x, y, x, y);
 
         getQuadTree(ll).insert(0, bbox);
 
         return -1;
     }
 
     @Override
     public long[] quadrantsForRectangle(Coordinate nw, Coordinate se) {
         final LatLonCoordinate llnw = (LatLonCoordinate) nw.as(Name.LAT_LON_DEG);
         final LatLonCoordinate llse = (LatLonCoordinate) se.as(Name.LAT_LON_DEG);
 
         final double w = llnw.getLon();
         final double n = llnw.getLat();
         final double s = llse.getLon();
         final double e = llse.getLat();
 
         final Envelope2D bbox = new Envelope2D();
         bbox.setCoords(w, s, e, n);
 
         final QuadTree wqt = getQuadTree(llnw);
         final QuadTree eqt = getQuadTree(llse);
 
 
 
         return new long[0];
     }
 
     @Override
     public long[] quadrantsForRadius(Coordinate center, Distance radius) {
         return new long[0];
     }
 
     @Override
     public long[] quadrantsForRegion(Coordinate[] coords) {
         return new long[0];
     }
 
     private QuadTree getQuadTree(LatLonCoordinate c) {
         return (c.getLon() < 0.0 ? westQT : eastQT);
     }
 
 }
