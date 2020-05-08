 package gov.usgs.cida.coastalhazards.wps;
 
 import com.vividsolutions.jts.algorithm.Angle;
 import com.vividsolutions.jts.algorithm.LineIntersector;
 import com.vividsolutions.jts.algorithm.RobustLineIntersector;
 import com.vividsolutions.jts.geom.Coordinate;
 import com.vividsolutions.jts.geom.Geometry;
 import com.vividsolutions.jts.geom.GeometryFactory;
 import com.vividsolutions.jts.geom.LineSegment;
 import com.vividsolutions.jts.geom.LineString;
 import com.vividsolutions.jts.geom.MultiLineString;
 import com.vividsolutions.jts.geom.Point;
 import com.vividsolutions.jts.geom.PrecisionModel;
 import com.vividsolutions.jts.geom.prep.PreparedGeometry;
 import com.vividsolutions.jts.geom.prep.PreparedGeometryFactory;
 import com.vividsolutions.jts.index.strtree.STRtree;
 import gov.usgs.cida.coastalhazards.util.CRSUtils;
 import gov.usgs.cida.coastalhazards.util.UTMFinder;
 import gov.usgs.cida.coastalhazards.wps.exceptions.UnsupportedCoordinateReferenceSystemException;
 import gov.usgs.cida.coastalhazards.wps.geom.ShorelineSTRTreeBuilder;
 import java.util.LinkedList;
 import java.util.List;
 import org.geoserver.catalog.ProjectionPolicy;
 import org.geoserver.wps.gs.GeoServerProcess;
 import org.geoserver.wps.gs.ImportProcess;
 import org.geotools.data.DataUtilities;
 import org.geotools.data.simple.SimpleFeatureCollection;
 import org.geotools.feature.FeatureCollection;
 import org.geotools.feature.simple.SimpleFeatureBuilder;
 import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
 import org.geotools.process.factory.DescribeParameter;
 import org.geotools.process.factory.DescribeProcess;
 import org.geotools.process.factory.DescribeResult;
 import org.geotools.referencing.CRS;
 import org.geotools.referencing.crs.DefaultGeographicCRS;
 import org.opengis.feature.simple.SimpleFeature;
 import org.opengis.feature.simple.SimpleFeatureType;
 import org.opengis.referencing.crs.CoordinateReferenceSystem;
 
 /* Can you hear me now? */
 
 // test 
 
 /**
  *
  * @author Jordan Walker <jiwalker@usgs.gov>
  */
 @DescribeProcess(
         title = "Generate Transects",
         description = "Create a transect layer from the baseline and shorelines",
         version = "1.0.0")
 public class GenerateTransectsProcess implements GeoServerProcess {
     
     public static final String TRANSECT_ID_ATTR = "TransectID";
     
     private static final CoordinateReferenceSystem REQUIRED_CRS_WGS84 = DefaultGeographicCRS.WGS84;
     private ImportProcess importProcess;
     
     public GenerateTransectsProcess(ImportProcess importProcess) {
         this.importProcess = importProcess;
     }
 
     /** May actually want to return reference to new layer
      *  Check whether we need an offset at the start of the baseline
      */
     @DescribeResult(name = "transects", description = "Layer containing Transects normal to baseline")
     public String execute(
             @DescribeParameter(name = "shorelines", min = 1, max = 1) SimpleFeatureCollection shorelines,
             @DescribeParameter(name = "baseline", min = 1, max = 1) SimpleFeatureCollection baseline,
             @DescribeParameter(name = "spacing", min = 1, max = 1) Double spacing,
             @DescribeParameter(name = "workspace", min = 1, max = 1) String workspace,
             @DescribeParameter(name = "store", min = 1, max = 1) String store,
             @DescribeParameter(name = "layer", min = 1, max = 1) String layer) throws Exception {
         return new Process(shorelines, baseline, spacing, workspace, store, layer).execute();
     }
     
     private class Process {
         private static final double MIN_TRANSECT_LENGTH = 50.0d; // meters
        private static final double TRANSECT_BUFFER = 5.0d; // meters
        private static final double LONG_TEST_LENGTH = 1000.d; // meters
         
         private final FeatureCollection<SimpleFeatureType, SimpleFeature> shorelines;
         private final FeatureCollection<SimpleFeatureType, SimpleFeature> baseline;
         private final double spacing;
         private final String workspace;
         private final String store;
         private final String layer;
         
         private CoordinateReferenceSystem utmCrs;
         
         private final GeometryFactory geometryFactory;
         private SimpleFeatureType simpleFeatureType;
         private int transectId;
         
         private Process(FeatureCollection<SimpleFeatureType, SimpleFeature> shorelines,
                 FeatureCollection<SimpleFeatureType, SimpleFeature> baseline,
                 double spacing,
                 String workspace,
                 String store,
                 String layer) {
             this.shorelines = shorelines;
             this.baseline = baseline;
             this.spacing = spacing;
             this.workspace = workspace;
             this.store = store;
             this.layer = layer;
             
             this.geometryFactory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING));
             this.transectId = 0; // start ids at 0
         }
         
         private String execute() throws Exception {
             CoordinateReferenceSystem shorelinesCrs = CRSUtils.getCRSFromFeatureCollection(shorelines);
             CoordinateReferenceSystem baselineCrs = CRSUtils.getCRSFromFeatureCollection(baseline);
             if (!CRS.equalsIgnoreMetadata(shorelinesCrs, REQUIRED_CRS_WGS84)) {
                 throw new UnsupportedCoordinateReferenceSystemException("Shorelines are not in accepted projection");
             }
             if (!CRS.equalsIgnoreMetadata(baselineCrs, REQUIRED_CRS_WGS84)) {
                 throw new UnsupportedCoordinateReferenceSystemException("Baseline is not in accepted projection");
             }
             this.utmCrs = UTMFinder.findUTMZoneForFeatureCollection((SimpleFeatureCollection)baseline);
             if (this.utmCrs == null) {
                 throw new IllegalStateException("Must have usable UTM zone to continue");
             }
             
             SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
             builder.setName("Transects");
             builder.add("geom", LineString.class, utmCrs);
             builder.add(TRANSECT_ID_ATTR, Integer.class);
             this.simpleFeatureType = builder.buildFeatureType();
             
             // probably want to get UTM zone and pass it in
             MultiLineString baselineGeometry = CRSUtils.getLinesFromFeatureCollection(baseline, REQUIRED_CRS_WGS84, utmCrs);
             VectorCoordAngle[] vectsOnBaseline = getEvenlySpacedOrthoVectorsAlongBaseline(baselineGeometry, spacing);
             
             MultiLineString shorelineGeometry = CRSUtils.getLinesFromFeatureCollection(shorelines, REQUIRED_CRS_WGS84, utmCrs);
             SimpleFeatureCollection resultingTransects = trimTransectsToFeatureCollection(vectsOnBaseline, shorelineGeometry);
             String layerName = addResultAsLayer(resultingTransects, workspace, store, layer);
             return layerName;
         }
 
         private VectorCoordAngle[] getEvenlySpacedOrthoVectorsAlongBaseline(MultiLineString baseline, double spacing) {
             List<VectorCoordAngle> vectList = new LinkedList<VectorCoordAngle>();
             
             for (int i=0; i < baseline.getNumGeometries(); i++) {
                 LineString line = (LineString)baseline.getGeometryN(i);
                 vectList.addAll(handleLineString(line, spacing));
             }
             VectorCoordAngle[] vectArr = new VectorCoordAngle[vectList.size()];
             return vectList.toArray(vectArr);
         }
         
         /**
          * 
          * @param vectsOnBaseline
          * @param baseline
          * @param shorelines
          * @return 
          */
         private SimpleFeatureCollection trimTransectsToFeatureCollection(VectorCoordAngle[] vectsOnBaseline, MultiLineString shorelines) {
             if (vectsOnBaseline.length == 0) {
                 return DataUtilities.collection(new SimpleFeature[0]);
             } 
             List<SimpleFeature> sfList = new LinkedList<SimpleFeature>();
             
             PreparedGeometry preparedShorelines = PreparedGeometryFactory.prepare(shorelines);
             STRtree tree = new ShorelineSTRTreeBuilder(shorelines).build();
             
            double testLength = LONG_TEST_LENGTH;
             while (!shorelines.isWithinDistance(vectsOnBaseline[0].getStartAsPoint(), testLength)) {
                 testLength *= 2;
             }
             // add an extra 1k for good measure
            testLength += LONG_TEST_LENGTH;
             
             for (VectorCoordAngle vect : vectsOnBaseline) {
                 LineString testLine = vect.getLineOfLength(testLength);
                 if (!preparedShorelines.intersects(testLine)) {
                     vect.flipAngle();
                     testLine = vect.getLineOfLength(testLength);
                     if (!preparedShorelines.intersects(testLine)) {
                         continue; // not sure what to trim to, skip
                     }
                 }
                 List<LineString> lines = tree.query(testLine.getEnvelopeInternal());
                 double maxDistance = MIN_TRANSECT_LENGTH;
                 for (LineString line : lines) {
                     if (line.intersects(testLine)) {
                         // must be a point
                         Point intersection = (Point) line.intersection(testLine);
                         double distance = vect.cartesianCoord.distance(intersection.getCoordinate());
                         if (distance > maxDistance) {
                             maxDistance = distance;
                         }
                     }
                 }
                LineString clipped = vect.getLineOfLength(maxDistance + TRANSECT_BUFFER);
                 SimpleFeature feature = createFeatureInUTMZone(clipped);
                 
                 sfList.add(feature);
             }
             return DataUtilities.collection(sfList);
         }
         
         // Thought these would be longer, but I'll leave them here
         private SimpleFeature createFeatureInUTMZone(LineString line) {
             SimpleFeature feature = SimpleFeatureBuilder.build(this.simpleFeatureType, new Object[]{line, new Integer(transectId)}, null);
             transectId++;
             return feature;
         }
         
         private String addResultAsLayer(SimpleFeatureCollection transects, String workspace, String store, String layer) {
             return importProcess.execute(transects, workspace, store, layer, utmCrs, ProjectionPolicy.REPROJECT_TO_DECLARED, null);
         }
 
         /**
          * Vectors point 90&deg; counterclockwise currently
          * @param lineString line along which to get vectors
          * @param spacing how often to create vectors along line
          * @return List of fancy vectors I concocted
          */
         private List<VectorCoordAngle> handleLineString(LineString lineString, double spacing) {
             Coordinate currentCoord = null;
             List<VectorCoordAngle> transectVectors = new LinkedList<VectorCoordAngle>();
             double accumulatedDistance = 0.0d;
             for (int i=0; i<lineString.getNumPoints(); i++) {
                 Coordinate coord = lineString.getCoordinateN(i);
                 if (currentCoord == null) {
                     currentCoord = coord;
                     Coordinate nextCoord = lineString.getCoordinateN(i+1);
                     if (nextCoord == null) {
                         throw new IllegalStateException("Line must have at least two points");
                     }
                     LineSegment segment = new LineSegment(currentCoord, nextCoord);
                     double orthogonal = segment.angle() + Angle.PI_OVER_2;
                     VectorCoordAngle vect = new VectorCoordAngle(currentCoord, orthogonal);
                     transectVectors.add(vect);
                     continue;
                 }
                 double distance = coord.distance(currentCoord);
                 if ((accumulatedDistance + distance) > spacing) {
                     double distanceToNewPoint = spacing - accumulatedDistance;
                     double fraction = distanceToNewPoint / distance;
                     LineSegment segment = new LineSegment(currentCoord, coord);
                     Coordinate pointAlong = segment.pointAlong(fraction);
                     double orthogonal = segment.angle() + Angle.PI_OVER_2;
                     VectorCoordAngle vect = new VectorCoordAngle(pointAlong, orthogonal);
                     transectVectors.add(vect);
                     
                     // this retries to next coordinate
                     currentCoord = pointAlong;
                     i--;
                     accumulatedDistance = 0.0d;
                 }
                 else {
                     accumulatedDistance += distance;
                     currentCoord = coord;
                 }
             }
             return transectVectors;
         }
 
     }
     
     /**
      * Creating this class because I'm having a hard time wrapping my head 
      * around polar vs. cartesian arithmetic.  Holding a cartesian coord with
      * a polar angle seemed to be my best way around it.
      */
     private class VectorCoordAngle {
         
         private Coordinate cartesianCoord;
         private double angle;
         private GeometryFactory gf;
         
         private VectorCoordAngle(Coordinate coord, double angle) {
             this.cartesianCoord = coord;
             this.angle = angle;
             this.gf = new GeometryFactory();
         }
         
         private VectorCoordAngle(double x, double y, double angle) {
             this(new Coordinate(x, y), angle);
         }
         
         private LineString getLineOfLength(double length) {
             double rise = length * Math.sin(angle);
             double run = length * Math.cos(angle);
             Coordinate endpoint = new Coordinate(cartesianCoord.x + run, cartesianCoord.y + rise);
             LineString newLineString = gf.createLineString(new Coordinate[] {cartesianCoord, endpoint});
             return newLineString;
         }
         
         private Point getStartAsPoint() {
             return gf.createPoint(cartesianCoord);
         }
         
         private void flipAngle() {
             angle += Math.PI;
         }
     }
 }
