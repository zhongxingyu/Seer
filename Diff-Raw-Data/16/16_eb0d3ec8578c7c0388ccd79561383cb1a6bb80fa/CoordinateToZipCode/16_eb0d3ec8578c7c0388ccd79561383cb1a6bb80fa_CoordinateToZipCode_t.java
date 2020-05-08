 package com.pocketcookies.zipcodeconverter;
 
 import com.google.common.collect.ImmutableMap;
 import com.vividsolutions.jts.geom.Coordinate;
 import com.vividsolutions.jts.geom.GeometryFactory;
 import com.vividsolutions.jts.geom.MultiPolygon;
 import java.io.IOException;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Properties;
import java.util.Set;
 import javax.annotation.Nullable;
 import org.geotools.data.shapefile.ShapefileDataStore;
 import org.geotools.data.simple.SimpleFeatureIterator;
 import org.opengis.feature.simple.SimpleFeature;
 
 /**
  * Class for converting {@link Coordinate}s to zip codes.
  *
  * @author john.a.edmonds@gmail.com (John "Jack" Edmonds)
  */
 public class CoordinateToZipCode {
 
     private static String getShapefileName(String id) {
         return "zt" + id + "_d00.shp";
     }
 
     private static String getShapefilePath(String id) {
         return "/zipcodes/" + getShapefileName(id);
     }
     private static final Properties shapefiles = new Properties();
 
     static {
         try {
             shapefiles.load(CoordinateToZipCode.class.getResourceAsStream("/zipcodes/shapefiles.txt"));
         } catch (IOException ex) {
             throw new ExceptionInInitializerError("Failed to load the list of shapefiles.");
         }
     }
 
     /**
      * Figures out which zip codes contain the given {@link Coordinates}.
      *
      * @param coordinates The coordinates for which to find containing zip
      * codes.
      * @return A map from the coordinates to the zip code (e.g. 10011)
      * containing that coordinate. If no zip code contains that coordinate, it
      * will not be in the map.
      * @throws IOException If there's a problem loading or reading from the
      * shapefile. In practice, this should be impossible since the shapefiles
      * should be packaged in the jar.
      */
     @Nullable
    public static Map<Coordinate, String> getZipCodes(Set<Coordinate> coordinates)
             throws IOException {
         final ImmutableMap.Builder<Coordinate, String> zipCodes = ImmutableMap.builder();
         for (Entry<Object, Object> shapefileEntry : shapefiles.entrySet()) {
             ShapefileDataStore shapefile = new ShapefileDataStore(
                     CoordinateToZipCode.class.getResource(
                     getShapefilePath((String) shapefileEntry.getValue())));
             for (Coordinate coordinate : coordinates) {
                 // Check the bounds of this entire shapefile to see if we can
                 // skip it.
                 if (shapefile.getFeatureSource().getBounds()
                         .contains(coordinate)) {
                     SimpleFeatureIterator featureIterator = shapefile
                             .getFeatureSource()
                             .getFeatures()
                             .features();
                     while (featureIterator.hasNext()) {
                         SimpleFeature feature = featureIterator.next();
                         if (((MultiPolygon) feature.getDefaultGeometry())
                                 .contains(
                                 new GeometryFactory()
                                 .createPoint(coordinate))) {
                             zipCodes.put(coordinate, (String) feature.getAttribute("NAME"));
                             break;
                         }
                     }
                 }
             }
         }
         return zipCodes.build();
     }
 }
