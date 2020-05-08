 package org.amanzi.awe.catalog.json;
 
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import net.sf.json.JSONArray;
 import net.sf.json.JSONObject;
 
 import org.geotools.geometry.jts.ReferencedEnvelope;
 import org.geotools.referencing.CRS;
 import org.geotools.referencing.crs.DefaultGeographicCRS;
 import org.opengis.referencing.crs.CoordinateReferenceSystem;
 
 import com.csvreader.CsvReader;
 import com.vividsolutions.jts.geom.Coordinate;
 import com.vividsolutions.jts.geom.Geometry;
 import com.vividsolutions.jts.geom.GeometryFactory;
 import com.vividsolutions.jts.geom.LineString;
 import com.vividsolutions.jts.geom.LinearRing;
 import com.vividsolutions.jts.geom.Point;
 import com.vividsolutions.jts.geom.Polygon;
 import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
 
 /**
  * This class is a utility class for reading from CSV files, using the CSVReader library. This class
  * needs to be resolved by the CSVGeoResource class that uDIG uses when placing the data into the
  * map. Currently this class assumes the data contains "x" and "y" headings, and that the projection
  * matches the one returned by the CSVGeoResourceInfo metadata.
  * 
  * @author craig
  */
 public class JSONReader {
     private URL url; // URL to start reading (might be only a header URL)
     private URL dataURL; // optional data URL in case it was automatically determined from header
     // URL (or vice versa)
     private JSONObject data;
     private CoordinateReferenceSystem crs;
     private ReferencedEnvelope bounds;
     private String name;
     private static GeometryFactory geometryFactory = new GeometryFactory();
 
     private Boolean networkGeoJSON;
     final CharSequence csSite = "site";
 
     /**
      * Create a JSON reader on the specified URL. This class opens the URL connection on demand for
      * any data request. The URL is not checked until opened, so if it is likely to be invalid, test
      * before this point.
      * 
      * @param url
      */
     public JSONReader( URL url ) {
         this.url = url;
     }
 
     public JSONReader( JSONService service ) {
         this.url = service.getValidURL();
         this.dataURL = service.getURL();
     }
 
     /**
      * This method is used internally by the other data methods, and caches its results for future
      * queries. It is available here for code that wishes to investigate the JSON more specifically.
      * 
      * @return the entire dataset as a JSONObject instance
      * @throws IOException
      */
     public JSONObject jsonObject() throws IOException {
         if (data == null) {
             data = JSONObject.fromObject(readURL(url));
         }
         return data;
     }
     private static String readURL( URL url ) throws IOException {
         Reader reader = new InputStreamReader(url.openStream());
         char[] buffer = new char[1024];
         int bytesRead = 0;
         StringBuffer sb = new StringBuffer();
         while( (bytesRead = reader.read(buffer)) >= 0 ) {
             if (bytesRead > 0) {
                 sb.append(buffer);
             }
         }
         return sb.toString();
     }
 
     /**
      * Find the Coordinate Reference System in the JSON, or default to WGS84 if none found.
      * 
      * @return CoordinateReferenceSystem
      */
     public CoordinateReferenceSystem getCRS() {
         return getCRS(DefaultGeographicCRS.WGS84);
     }
 
     /**
      * Find the Coordinate Reference System in the JSON, or default to the specified default if no
      * CRS is found in the JSON.
      * 
      * @return CoordinateReferenceSystem
      */
     public CoordinateReferenceSystem getCRS( CoordinateReferenceSystem defaultCRS ) {
         if (crs == null) {
             crs = defaultCRS; // default if crs cannot be found below
             try {
                 JSONObject jsonCRS = jsonObject().getJSONObject("crs");
                 System.out.println("Determining CRS from JSON: " + jsonCRS.toString());
                 JSONObject jsonCRSProperties = jsonCRS.getJSONObject("properties");
                 if (jsonCRS.getString("type").equals("name")) {
                     // The simple approach is to name the CRS, eg. EPSG:4326 (GeoJSON spec prefers a
                     // new naming standard, but I'm not sure geotools knows it)
                     crs = CRS.decode(jsonCRSProperties.getString("name"));
                 } else if (jsonCRS.getString("type").equals("link")) {
                     // TODO: This type is specified in GeoJSON spec, but what the HREF means is not,
                     // so we assume it is a live URL that will feed a CRS specification directly
                     URL crsURL = new URL(jsonCRSProperties.getString("href"));
                     crs = CRS.decode(crsURL.getContent().toString());
                 }
             } catch (Exception crs_e) {
                 System.err.println("Failed to interpret CRS: " + crs_e.getMessage());
                 crs_e.printStackTrace(System.err);
             }
         }
         return crs;
     }
 
     /**
      * Find the bounding box for the data set as a ReferenceEnvelope. It uses the getCRS method to
      * find the reference system then looks for explicit "bbox" elements, and finally, if no bbox
      * was found, scans all feature geometries for coordinates and builds the bounds on those. The
      * result is cached for future calls.
      * 
      * @return ReferencedEnvelope for bounding box
      */
     public ReferencedEnvelope getBounds() {
         if (bounds == null) {
             // Create Null envelope
            this.bounds = new ReferencedEnvelope(getCRS());
             // First try to find the BBOX definition in the JSON directly
             try {
                 JSONArray jsonBBox = jsonObject().getJSONArray("bbox");
                 if (jsonBBox != null) {
                     System.out.println("Interpreting GeoJSON BBox: " + jsonBBox.toString());
                     double minX = jsonBBox.getDouble(0);
                     double minY = jsonBBox.getDouble(1);
                     double maxX = jsonBBox.getDouble(2);
                     double maxY = jsonBBox.getDouble(3);
                     this.bounds = new ReferencedEnvelope(minX, maxX, minY, maxY, crs);
                 } else {
                     System.err.println("No BBox defined in the GeoJSON object");
                 }
             } catch (Exception bbox_e) {
                 System.err.println("Failed to interpret BBOX: " + bbox_e.getMessage());
                 bbox_e.printStackTrace(System.err);
             }
             // Secondly, if bounds is still empty, try find all feature geometries and calculate
             // bounds
             // This should only work if the JSON actually contains the data, which should not be the
             // case for a header-only JSON stream.
             try {
                 if (this.bounds.isNull()) {
                     // Try to create envelope from any data contained in the GeoJSON object (fails
                     // for header, but then that should have a bbox)
                     JSONArray features = jsonObject().getJSONArray("features");
                     if (features != null && features.size() > 0) {
                         for( int i = 0; i < features.size(); i++ ) {
                             JSONObject feature = features.getJSONObject(i);
                             JSONObject geometry = feature.getJSONObject("geometry");
                             if (geometry != null) {
                                 String geometryType = geometry.getString("type");
                                 JSONArray coordinates = geometry.getJSONArray("coordinates");
                                 if (geometryType.equals("Point")) {
                                     this.bounds.expandToInclude(coordinates.getDouble(0),
                                             coordinates.getDouble(1));
                                 } else {
                                     for( int x = 0; x < coordinates.size(); x++ ) {
                                         JSONArray coords = coordinates.getJSONArray(x);
                                         this.bounds.expandToInclude(coords.getDouble(0), coords
                                                 .getDouble(1));
                                     }
                                 }
                             } else {
                                 System.err.println("Failed to find geometry in feature: "
                                         + feature.toString());
                             }
                         }
                     }
                 }
             } catch (Exception bbox_e) {
                 System.err.println("Failed to interpret BBOX: " + bbox_e.getMessage());
                 bbox_e.printStackTrace(System.err);
             }
         }
         return bounds;
     }
 
     /**
      * Return the name of the dataset as specified in the JSON, or default to the URL.getFile().
      * 
      * @return dataset name
      */
     public String getName() {
         if (name == null) {
             try {
                 name = jsonObject().getString("name");
             } catch (IOException e) {
                 System.err.println("Failed to find name element: " + e.getMessage());
                 e.printStackTrace(System.err);
             }
             if (name == null)
                 name = url.getFile();
         }
         return name;
     }
 
     /**
      * Checks does this FeatureCollection contains name field and does name contains site word in
      * it.
      * 
      * @return Network GeoJSON indicator, false if it is not, true if it is
      */
     public boolean isNetworkGeoJSON() {
         if (networkGeoJSON == null) {
             try {
                 boolean containsKeyName = jsonObject().containsKey("name");
                 if (containsKeyName) {
                     final String strName = jsonObject().getString("name");
                     if (strName.toLowerCase().contains(csSite)) {
                         networkGeoJSON = true;
                     } else {
                         // it contains name, but name does not contain 'site'
                         // string, its not network GeoJSON
                         networkGeoJSON = false;
                     }
                 } else {
                     // does not contain name, its not network GeoJSON
                     networkGeoJSON = false;
                 }
             } catch (IOException e) {
                 networkGeoJSON = false;
                 // this can never occur, since we execution can end up if
                 // jsonObject().getString("name") throws exception, but if there
                 // is no 'name' we will not try to fetch it
             }
         }
         return networkGeoJSON;
     }
 
     /**
      * Return a descriptive string of this dataset. This is based on the name, crs and bounding box.
      * 
      * @return descriptive string
      */
     public String toString() {
         return "JSON[" + getName() + "]: CRS:" + getCRS() + " Bounds:" + getBounds();
     }
 
     /**
      * This is the main method called by the application to produce the data for display on the map.
      * Should be called repeatedly until it returns null.
      * 
      * @param reader
      * @return instance of com.vividsolutions.jts.geom.Point
      * @throws IOException
      */
     public static Point getPointX( JSONObject jsonObject ) throws IOException {
         if (jsonObject == null)
             return null;
         double x = Double.valueOf(jsonObject.get("x").toString());
         double y = Double.valueOf(jsonObject.get("y").toString());
         Coordinate coordinate = new Coordinate(x, y);
         return geometryFactory.createPoint(coordinate);
     }
 
     /**
      * A generic interface for all features, whether made from JSON or from some other source of
      * data. This is to allow for more compact data formats like CSV, or hex streams. It is modeled
      * however on the types of data found in GeoJSON features.
      * 
      * @author craig
      */
     public static interface Feature {
         /** get the type, as specified in the GeoJSON spec, eg. Point, MultiPoint, Polygon, etc. */
         public String getType();
         /** get the set of points representing this feature, or array of length 1 for Point type */
         public Point[] getPoints();
         /** get the map of additional properties for this data type, eg. domain specific data */
         public Map<String, Object> getProperties();
         /**
          * Creates geometry object for this feature.
          * 
          * @return {@link Geometry} object
          */
         Geometry createGeometry();
     }
 
     /**
      * This is the based class for all features that are genuinely build from JSON. As such a
      * JSONObject is passed to the constructor, and used to generate the feature data required.
      * 
      * @author craig
      */
     public static class JSONFeature implements Feature {
         private String type;
         private JSONObject geometry;
         private JSONObject properties;
         private Point[] points;
         private HashMap<String, Object> propMap;
         private Geometry objGeometry;
 
         public JSONFeature( JSONObject jsonObject ) {
             this.geometry = jsonObject.getJSONObject("geometry");
             this.properties = jsonObject.getJSONObject("properties");
             this.type = this.geometry.getString("type"); // We only care about the geometry type,
             // because the feature type is by
             // definition "Feature"
         }
         public String getType() {
             return type;
         }
         public Point[] getPoints() {
             if (points == null) {
                 JSONArray coordinates = geometry.getJSONArray("coordinates");
                 if (type.equals("Point")) {
                     points = new Point[]{makePoint(coordinates)};
                 } else {
                     // Assume 2D array of points
                     int countPoints = coordinates.size();
                     points = new Point[countPoints];
                     for( int i = 0; i < countPoints; i++ ) {
                         points[i] = makePoint(coordinates.getJSONArray(i));
                     }
                 }
             }
             return points;
         }
         private Point makePoint( JSONArray jsonPoint ) {
             Coordinate coordinate = new Coordinate(jsonPoint.getDouble(0), jsonPoint.getDouble(1));
             return geometryFactory.createPoint(coordinate);
         }
 
         public Geometry createGeometry() {
             if (objGeometry == null) {
                 if (geometry != null) {
                     final JSONGeoFeatureType featureType = JSONGeoFeatureType.fromCode(type);
                     final JSONArray coordinates = geometry.getJSONArray("coordinates");
                     switch( featureType ) {
                     case POINT:
                         objGeometry = createPoint(coordinates);
                         break;
                     case MULTI_POINT:
                         objGeometry = createMultiPoint(coordinates);
                         break;
                     case LINE:
                         objGeometry = createLine(coordinates);
                         break;
                     case MULTI_LINE_STRING:
                         objGeometry = createMultiLine(coordinates);
                         break;
                     case POLYGON:
                         objGeometry = createPolygon(coordinates);
                         break;
                     case MULTI_POLYGON:
                         objGeometry = createMultiPolygon(coordinates);
                     default:
                         break;
                     }
                 }
             }
             return objGeometry;
         }
 
         /**
          * Creates {@link Point} object from json string.
          * 
          * @param jsonCoordinates {@link JSONArray} object
          * @return {@link Point} object
          */
         private Geometry createPoint( JSONArray coordinates ) {
             return geometryFactory.createPoint(createCoordinate(coordinates));
         }
 
         /**
          * Creates {@link MultiPoint} object from json string.
          * 
          * @param jsonCoordinates json representation of MultiPoint.
          * @return {@link MultiPoint} object
          * @throws JSONException json is malformed
          */
         private Geometry createMultiPoint( final JSONArray jsonCoordinates ) {
             return geometryFactory.createMultiPoint(createCoordinates(jsonCoordinates));
         }
 
         /**
          * Creates {@link LineString} object from json string.
          * 
          * @param jsonCoordinates json representation of LineString.
          * @return {@link LineString} object
          */
         private Geometry createLine( final JSONArray jsonCoordinates ) {
             return geometryFactory.createLineString(createCoordinates(jsonCoordinates));
         }
 
         /**
          * Creates {@link MultiLineString} object from json string.
          * 
          * @param jsonCoordinates json representation of MultiLineString.
          * @return {@link MultiLineString} object
          */
         private Geometry createMultiLine( final JSONArray jsonCoordinates ) {
             List<LineString> lineStringList = new ArrayList<LineString>();
             for( int i = 0; i < jsonCoordinates.size(); i++ ) {
                 JSONArray jsonLine = jsonCoordinates.getJSONArray(i);
                 lineStringList.add(geometryFactory.createLineString(createCoordinates(jsonLine)));
             }
             return geometryFactory.createMultiLineString(lineStringList
                     .toArray(new LineString[lineStringList.size()]));
         }
 
         /**
          * Creates {@link Polygon} object from {@link JSONArray} object.
          * 
          * @param jsonCoordinates {@link JSONArray} object
          * @return {@link Polygon} object
          */
         private Polygon createPolygon( final JSONArray jsonCoordinates ) {
             LinearRing linearRing = null;
 
             final List<LinearRing> holeLinearRings = new ArrayList<LinearRing>();
             for( int i = 0; i < jsonCoordinates.size(); i++ ) {
                 if (i == 0) {
                     linearRing = createLinearRing(jsonCoordinates.getJSONArray(i));
                 } else {
                     holeLinearRings.add(createLinearRing(jsonCoordinates.getJSONArray(i)));
                 }
             }
             return geometryFactory.createPolygon(linearRing, holeLinearRings
                     .toArray(new LinearRing[holeLinearRings.size()]));
         }
 
         /**
          * Creates {@link MultiPolygon} object from json string.
          * 
          * @param jsonCoordinates json representation of MultiPolygon.
          * @return {@link MultiPolygon} object
          */
         private Geometry createMultiPolygon( final JSONArray jsonCoordinates ) {
 
             List<Polygon> polygons = new ArrayList<Polygon>();
             for( int i = 0; i < jsonCoordinates.size(); i++ ) {
                 JSONArray jsonPolygon = jsonCoordinates.getJSONArray(i);
                 polygons.add(createPolygon(jsonPolygon));
             }
             return geometryFactory.createMultiPolygon(polygons
                     .toArray(new Polygon[polygons.size()]));
         }
 
         /**
          * Creates {@link LinearRing} object from given json.
          * 
          * @param jsonCoordinates {@link JSONArray} object
          * @return {@link LinearRing} object
          */
         private LinearRing createLinearRing( final JSONArray jsonCoordinates ) {
             final Coordinate[] coordinates = new Coordinate[jsonCoordinates.size()];
             for( int i = 0; i < coordinates.length; i++ ) {
                 coordinates[i] = createCoordinate(jsonCoordinates.getJSONArray(i));
             }
             final CoordinateArraySequence sequence = new CoordinateArraySequence(coordinates);
             return new LinearRing(sequence, geometryFactory);
         }
 
         /**
          * Creates {@link Coordinate} array out of given {@link JSONArray} object.
          * 
          * @param jsonCoordinates {@link JSONArray} object that represents array of coordinates
          * @return array of {@link Coordinate} objects
          */
         private Coordinate[] createCoordinates( final JSONArray jsonCoordinates ) {
             Coordinate[] coordinates = new Coordinate[jsonCoordinates.size()];
             for( int i = 0; i < jsonCoordinates.size(); i++ ) {
                 coordinates[i] = createCoordinate(jsonCoordinates.getJSONArray(i));
             }
             return coordinates;
         }
 
         /**
          * Creates {@link Coordinate} object out of given {@link JSONArray} object.
          * 
          * @param json {@link JSONArray} object
          * @return {@link Coordinate} object
          */
         private Coordinate createCoordinate( final JSONArray json ) {
             return new Coordinate(json.getDouble(0), json.getDouble(1));
         }
 
         public Map<String, Object> getProperties() {
             if (propMap == null) {
                 this.propMap = new HashMap<String, Object>();
                 if (properties != null) {
                     for( Object key : properties.keySet() ) {
                         propMap.put(key.toString(), properties.get(key));
                     }
                 }
             }
             return propMap;
         }
         public String toString() {
             if (getProperties().containsKey("name"))
                 return getProperties().get("name").toString();
             else
                 return points[0].toString();
         }
     }
     public static class SimplePointFeature implements Feature {
         private Point point;
         private HashMap<String, Object> properties;
         public SimplePointFeature( double x, double y, HashMap<String, Object> properties ) {
             Coordinate coordinate = new Coordinate(x, y);
             this.point = geometryFactory.createPoint(coordinate);
             this.properties = properties;
         }
         public Point[] getPoints() {
             return new Point[]{this.point};
         }
         public Map<String, Object> getProperties() {
             return properties;
         }
         public String getType() {
             return "Point";
         }
 
         public Geometry createGeometry() {
             return point;
         }
 
         public String toString() {
             if (properties.containsKey("name"))
                 return properties.get("name").toString();
             else
                 return point.toString();
         }
     }
 
     /**
      * This class provides the API for various anonymous inner classes that can produce a stream of
      * JSON features. We implement both the Iterator and the Enumeration interfaces to be friendly
      * to both the java5-style 'for loop' and the JRuby 'each' method. This allows, for example, the
      * JSON to contain the features directly as well as reference another data source, like a file
      * or another URL, that will provide the necessary data.
      * 
      * @author craig
      */
     public static abstract class FeatureIterator implements Iterable<Feature>, Enumeration<Feature> {
         private Iterator<Feature> iter = null;
         public boolean hasMoreElements() {
             if (iter == null)
                 iter = iterator();
             return iter.hasNext();
         }
         public Feature nextElement() {
             return hasMoreElements() ? iter.next() : null;
         }
     }
     private static class JSONFeatureReader extends FeatureIterator {
         protected JSONArray features;
         private int index;
         public JSONFeatureReader( JSONArray features ) {
             this.index = 0;
             this.features = features;
         }
         private Feature getFeature() {
             try {
                 JSONObject feature = features.getJSONObject(index++);
                 return new JSONFeature(feature);
             } catch (Throwable e) {
                 return null;
             }
         }
         /** provide an iterator reset to the first element, if any */
         public Iterator<Feature> iterator() {
             index = 0;
             return new Iterator<Feature>(){
                 public boolean hasNext() {
                     return features != null && features.size() > index;
                 }
                 public Feature next() {
                     return getFeature();
                 }
                 public void remove() {
                 }
             };
         }
     }
     private static class JSONURLFeatureReader extends JSONFeatureReader {
         private URL feature_url;
         public JSONURLFeatureReader( URL feature_url ) {
             super(null);
             this.feature_url = feature_url;
         }
         private void setupFeatures() {
             try {
                 String content = readURL(feature_url);
                 JSONObject json = JSONObject.fromObject(content);
                 features = json.getJSONArray("features");
             } catch (IOException e) {
                 System.err.println("Failed to get features from url '" + feature_url + "': " + e);
                 e.printStackTrace(System.err);
             }
         }
         /** provide an iterator reset to the first element, if any */
         public Iterator<Feature> iterator() {
             setupFeatures();
             return super.iterator();
         }
     }
     private static class CSVURLFeatureReader extends FeatureIterator {
         private URL feature_url;
         private CsvReader reader;
         private int x_col = -1;
         private int y_col = -1;
         private int name_col = -1;
         public CSVURLFeatureReader( URL feature_url ) {
             this.feature_url = feature_url;
         }
         private void setupFeatures() {
             try {
                 if (reader != null)
                     reader.close();
                 reader = new CsvReader(new InputStreamReader(feature_url.openStream()));
                 reader.readHeaders(); // Assume all CSV files have a header line
                 HashMap<String, Integer> headers = new HashMap<String, Integer>();
                 for( String header : reader.getHeaders() )
                     headers.put(header.toLowerCase(), headers.size());
                 for( String head : new String[]{"long", "longitude", "x"} ) {
                     if (headers.containsKey(head))
                         x_col = headers.get(head);
                 }
                 for( String head : new String[]{"lat", "latitude", "y"} ) {
                     if (headers.containsKey(head))
                         y_col = headers.get(head);
                 }
                 for( String head : new String[]{"description", "name"} ) {
                     if (headers.containsKey(head))
                         name_col = headers.get(head);
                 }
                 // test for invalid x and y columns
                 if (x_col < 0 || x_col >= reader.getHeaderCount())
                     throw new Exception("Invalid easting column: " + x_col);
                 if (y_col < 0 || y_col >= reader.getHeaderCount())
                     throw new Exception("Invalid northing column: " + y_col);
                 // fix invalid name_col
                 int loops = 0;
                 while( loops < 2 && invalidNameCol() ) {
                     name_col++;
                     if (name_col >= reader.getHeaderCount()) {
                         name_col = 0;
                         loops++;
                     }
                 }
                 if (invalidNameCol())
                     name_col = -1; // deal with this later
             } catch (Exception e) {
                 System.err.println("Failed to get features from url '" + feature_url + "': " + e);
                 e.printStackTrace(System.err);
             }
         }
         private Feature getFeature() {
             if (reader == null)
                 setupFeatures();
             try {
                 double x = Double.valueOf(reader.get(x_col));
                 double y = Double.valueOf(reader.get(y_col));
                 String name = (name_col < 0) ? "Point[" + x + ":" + y + "]" : reader.get(name_col);
                 HashMap<String, Object> properties = new HashMap<String, Object>();
                 properties.put("name", name);
                 for( int i = 0; i < reader.getColumnCount(); i++ ) {
                     if (i != x_col && i != y_col && i != name_col)
                         properties.put(reader.getHeader(i), reader.get(i));
                 }
                 return new SimplePointFeature(x, y, properties);
             } catch (Exception e) {
                 System.err.println("Failed to get features from url '" + feature_url + "': " + e);
                 e.printStackTrace(System.err);
                 return null;
             }
         }
         private boolean invalidNameCol() {
             return (name_col == x_col || name_col == y_col || name_col >= reader.getHeaderCount() || name_col < 0);
         }
         public Iterator<Feature> iterator() {
             setupFeatures();
             return new Iterator<Feature>(){
                 private Feature next = null;
                 public boolean hasNext() {
                     return (next = getFeature()) != null;
                 }
                 public Feature next() {
                     if (next == null)
                         next = getFeature();
                     return next;
                 }
                 public void remove() {
                 }
             };
         }
     }
 
     public FeatureIterator getFeatures() {
         JSONArray features = null;
         JSONObject featureSource = null;
         try {
             if (jsonObject().has("features")) {
                 features = jsonObject().getJSONArray("features");
             }
             if (jsonObject().has("feature_source")) {
                 featureSource = jsonObject().getJSONObject("feature_source");
             }
         } catch (Exception e) {
             System.err.println("Failed to find features collection: " + e);
         }
         if (features != null) {
             // We have a feature collection, so let's use that directly as the data
             return new JSONFeatureReader(features);
         } else if (featureSource != null) {
             // We have a reference to an alternative data source, use that
             try {
                 String dataType = featureSource.getString("type");
                 URL feature_url = new URL(url, featureSource.getString("href"));
                 if (dataType != null && dataType.toLowerCase().equals("csv")) {
                     return new CSVURLFeatureReader(feature_url); // Connect to a URL and
                     // interpret as CSV stream
                 } else if (dataType != null && dataType.toLowerCase().endsWith("json")) {
                     // support .json and .geo_json
                     return new JSONURLFeatureReader(feature_url); // Connect to a URL and
                     // interpret as JSON stream
                 } else if (this.dataURL != null) {
                     return new JSONURLFeatureReader(dataURL); // Connect to data URL if specified,
                     // and assume it's JSON (we get here
                     // if the data URL is a simple
                     // modification of the header URL)
                 } else {
                     System.err
                             .println("JSON URL contained no features, nor a reference to an alternative feature source: "
                                     + this.url);
                     return null;
                 }
             } catch (MalformedURLException e) {
                 System.err.println("Failed to determine feature source URL: " + e);
                 e.printStackTrace(System.err);
                 return null;
             }
         } else {
             // TODO: Support geometries collection also (like features but without bbox and
             // properties)
             System.err
                     .println("JSON contains no features collection, or a featues_source reference");
             return null;
         }
     }
 }
