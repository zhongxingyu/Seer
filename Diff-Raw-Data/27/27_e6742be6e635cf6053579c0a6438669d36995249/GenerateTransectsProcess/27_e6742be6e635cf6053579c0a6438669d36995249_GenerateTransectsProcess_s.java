 package gov.usgs.cida.coastalhazards.wps;
 
 import com.vividsolutions.jts.algorithm.Angle;
 import com.vividsolutions.jts.geom.Coordinate;
 import com.vividsolutions.jts.geom.Geometry;
 import com.vividsolutions.jts.geom.GeometryFactory;
 import com.vividsolutions.jts.geom.LineSegment;
 import com.vividsolutions.jts.geom.LineString;
 import com.vividsolutions.jts.geom.MultiLineString;
 import com.vividsolutions.jts.geom.PrecisionModel;
 import com.vividsolutions.jts.geom.prep.PreparedGeometry;
 import com.vividsolutions.jts.geom.prep.PreparedGeometryFactory;
 import gov.usgs.cida.coastalhazards.util.UTMFinder;
 import gov.usgs.cida.coastalhazards.wps.exceptions.UnsupportedCoordinateReferenceSystemException;
 import gov.usgs.cida.coastalhazards.wps.exceptions.UnsupportedFeatureTypeException;
 import java.util.LinkedList;
 import java.util.List;
 import org.geoserver.catalog.ProjectionPolicy;
 import org.geoserver.wps.gs.GeoServerProcess;
 import org.geoserver.wps.gs.ImportProcess;
 import org.geotools.data.simple.SimpleFeatureCollection;
 import org.geotools.feature.FeatureCollection;
 import org.geotools.feature.FeatureCollections;
 import org.geotools.feature.FeatureIterator;
 import org.geotools.feature.simple.SimpleFeatureBuilder;
 import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
 import org.geotools.geometry.jts.Geometries;
 import org.geotools.geometry.jts.JTS;
 import org.geotools.process.factory.DescribeParameter;
 import org.geotools.process.factory.DescribeProcess;
 import org.geotools.process.factory.DescribeResult;
 import org.geotools.referencing.CRS;
 import org.geotools.referencing.crs.DefaultGeographicCRS;
 import org.opengis.feature.simple.SimpleFeature;
 import org.opengis.feature.simple.SimpleFeatureType;
 import org.opengis.referencing.FactoryException;
 import org.opengis.referencing.crs.CoordinateReferenceSystem;
 import org.opengis.referencing.operation.MathTransform;
 import org.opengis.referencing.operation.TransformException;
 
 /**
  *
  * @author Jordan Walker <jiwalker@usgs.gov>
  */
 @DescribeProcess(
         title = "Generate Transects",
         description = "Create a transect layer from the baseline and shorelines",
         version = "1.0.0")
 public class GenerateTransectsProcess implements GeoServerProcess {
     
    private static final CoordinateReferenceSystem ACCEPTED_CRS = DefaultGeographicCRS.WGS84;
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
         private static final int HEURISTIC_LENGTH = 1000;
         
         private final FeatureCollection<SimpleFeatureType, SimpleFeature> shorelines;
         private final FeatureCollection<SimpleFeatureType, SimpleFeature> baseline;
         private final double spacing;
         private final String workspace;
         private final String store;
         private final String layer;
         
         private CoordinateReferenceSystem utmCrs;
         
         private final GeometryFactory geometryFactory;
         private SimpleFeatureType simpleFeatureType;
         
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
             
         }
         
         private String execute() throws Exception {
             CoordinateReferenceSystem shorelinesCrs = findCRS(shorelines);
             CoordinateReferenceSystem baselineCrs = findCRS(baseline);
            if (!shorelinesCrs.equals(ACCEPTED_CRS)) {
                 throw new UnsupportedCoordinateReferenceSystemException("Shorelines are not in accepted projection");
             }
            if (!baselineCrs.equals(ACCEPTED_CRS)) {
                 throw new UnsupportedCoordinateReferenceSystemException("Baseline is not in accepted projection");
             }
             this.utmCrs = UTMFinder.findUTMZoneForFeatureCollection((SimpleFeatureCollection)baseline);
             if (this.utmCrs == null) {
                 throw new IllegalStateException("Must have usable UTM zone to continue");
             }
             
             SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
             builder.setName("Transects");
             builder.add("geom", LineString.class, utmCrs);
             simpleFeatureType = builder.buildFeatureType();
             
             // probably want to get UTM zone and pass it in
             MultiLineString baselineGeometry = getLinesFromFeatureCollection(baseline);
             VectorCoordAngle[] vectsOnBaseline = getEvenlySpacedOrthoVectorsAlongBaseline(baselineGeometry, spacing);
             
             MultiLineString shorelineGeometry = getLinesFromFeatureCollection(shorelines);
             SimpleFeatureCollection resultingTransects = trimTransectsToFeatureCollection(vectsOnBaseline, shorelineGeometry);
             String layerName = addResultAsLayer(resultingTransects, workspace, store, layer);
             return layerName;
         }
         
         private CoordinateReferenceSystem findCRS(FeatureCollection<SimpleFeatureType, SimpleFeature> simpleFeatureCollection) {
             FeatureCollection<SimpleFeatureType, SimpleFeature> shorelineFeatureCollection = simpleFeatureCollection;
             SimpleFeatureType sft = shorelineFeatureCollection.getSchema();
             CoordinateReferenceSystem coordinateReferenceSystem = sft.getCoordinateReferenceSystem();
             return coordinateReferenceSystem;
         }
         
         /**
          * Need to watch out here, the utm detection is in here, so there is the possibility of using two different projections
          * @param featureCollection
          * @return 
          */
         private MultiLineString getLinesFromFeatureCollection(FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection) {
             List<LineString> lines = new LinkedList<LineString>();
             MathTransform transform = null;
             try {
                transform = CRS.findMathTransform(ACCEPTED_CRS, utmCrs, true);
             }
             catch (FactoryException ex) {
                 return null; // do something better than this
             }
             FeatureIterator<SimpleFeature> features = featureCollection.features();
             SimpleFeature feature = null;
             while (features.hasNext()) {
                 feature = features.next();
                 Geometry geometry = (Geometry)feature.getDefaultGeometry();
                 
                 Geometry utmGeometry = null;
                 try {
                     utmGeometry = JTS.transform(geometry, transform);
                 }
                 catch (TransformException ex) {
                     // TODO handle exceptions
                 }
                 
                 Geometries geomType = Geometries.get(utmGeometry);
                 LineString lineString = null;
                 Coordinate[] coords = null;
                 switch (geomType) {
                     case POLYGON:
                     case MULTIPOLYGON:
                         throw new UnsupportedFeatureTypeException("Polygons not supported in baseline");
                     case LINESTRING:
                         lineString = (LineString)utmGeometry;
                         lines.add(lineString);
                         break;
                     case MULTILINESTRING:
                         MultiLineString multiLineString = (MultiLineString)utmGeometry;
                         for (int i=0; i < multiLineString.getNumGeometries(); i++) {
                             lineString = (LineString)multiLineString.getGeometryN(i);
                             lines.add(lineString);
                         }
                         break;
                     case POINT:
                     case MULTIPOINT:
                         throw new UnsupportedFeatureTypeException("Points not supported in baseline");
                     default:
                         throw new UnsupportedFeatureTypeException("Only line type supported");
                 }
             }
             
             LineString[] linesArr = new LineString[lines.size()];
             lines.toArray(linesArr);
             return geometryFactory.createMultiLineString(linesArr);
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
             SimpleFeatureCollection features = FeatureCollections.newCollection();
             
             PreparedGeometry preparedShorelines = new PreparedGeometryFactory().create(shorelines);
             for (VectorCoordAngle vect : vectsOnBaseline) {
                 LineString testLine = vect.getLineOfLength(HEURISTIC_LENGTH);
                 if (!preparedShorelines.intersects(testLine)) {
                     vect.flipAngle();
                     testLine = vect.getLineOfLength(HEURISTIC_LENGTH);
                     if (!preparedShorelines.intersects(testLine)) {
                         continue; // not sure what to trim to
                     }
                 }
                 double length = 0.0d;
                 Geometry intersection = testLine.intersection(shorelines);
                 for (Coordinate coord : intersection.getCoordinates()) {
                     if (vect.cartesianCoord.distance(coord) > length) {
                         length = vect.cartesianCoord.distance(coord);
                     }
                 }
                 LineString clipped = vect.getLineOfLength(length);
                 SimpleFeature feature = createFeatureInUTMZone(clipped);
                 // using deprecated method since this should be in memory
                 // TODO fix this to not use deprecated
                 features.add(feature);
             }
 
             return features;
         }
         
         // Thought these would be longer, but I'll leave them here
         private SimpleFeature createFeatureInUTMZone(LineString line) {
             return SimpleFeatureBuilder.build(this.simpleFeatureType, new Object[]{line}, null);
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
                    double orthogonal = segment.angle() + Angle.PI_OVER_4;
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
                    double orthogonal = segment.angle() + Angle.PI_OVER_4;
                     VectorCoordAngle vect = new VectorCoordAngle(pointAlong, orthogonal);
                     transectVectors.add(vect);
                     currentCoord = pointAlong;
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
         
         private void flipAngle() {
            angle += Angle.PI_OVER_2;
         }
     }
 }
