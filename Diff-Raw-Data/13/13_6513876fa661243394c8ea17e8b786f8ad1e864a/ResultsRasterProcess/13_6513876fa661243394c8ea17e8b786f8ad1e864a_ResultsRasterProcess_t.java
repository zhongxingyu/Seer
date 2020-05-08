 package gov.usgs.cida.coastalhazards.wps;
 
 import com.vividsolutions.jts.geom.Coordinate;
 import com.vividsolutions.jts.geom.Geometry;
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.GraphicsEnvironment;
 import java.awt.Transparency;
 import java.awt.image.BufferedImage;
 import java.util.Map;
 import java.util.UUID;
 import java.util.WeakHashMap;
 import org.geoserver.wps.gs.GeoServerProcess;
 import org.geotools.coverage.grid.GridCoordinates2D;
 import org.geotools.coverage.grid.GridCoverage2D;
 import org.geotools.coverage.grid.GridCoverageFactory;
 import org.geotools.coverage.grid.GridEnvelope2D;
 import org.geotools.coverage.grid.GridGeometry2D;
 import org.geotools.data.simple.SimpleFeatureCollection;
 import org.geotools.data.simple.SimpleFeatureIterator;
 import org.geotools.geometry.DirectPosition2D;
 import org.geotools.geometry.jts.Geometries;
 import org.geotools.geometry.jts.JTS;
 import org.geotools.geometry.jts.ReferencedEnvelope;
 import org.geotools.process.factory.DescribeParameter;
 import org.geotools.process.factory.DescribeProcess;
 import org.geotools.process.factory.DescribeResult;
 import org.geotools.referencing.CRS;
 import org.opengis.feature.simple.SimpleFeature;
 import org.opengis.feature.type.AttributeDescriptor;
 import org.opengis.geometry.Envelope;
 import org.opengis.geometry.MismatchedDimensionException;
 import org.opengis.referencing.crs.CoordinateReferenceSystem;
 import org.opengis.referencing.operation.MathTransform;
 import org.opengis.referencing.operation.TransformException;
 
 /**
  *
  * @author tkunicki
  */
 @DescribeProcess(
         title = "Results Raster",
         description = "Rasterize Results by Attribute",
         version = "1.0.0")
 public class ResultsRasterProcess implements GeoServerProcess {
     
     public static class AttributeRange {
         public final double min;
         public final double max;
         public final double extent;
         public AttributeRange(double min, double max) {
             this.min = min;
             this.max = max;
             this.extent = max - min;
         }
     }
     
     Map<String, Map<String, AttributeRange>> featureAttributeRangeMap = new WeakHashMap<String, Map<String, AttributeRange>>();
 
     private static final int COORD_GRID_CHUNK_SIZE = 256;
 
     @DescribeResult(name = "coverage", description = "coverage")
     public GridCoverage2D execute(
             @DescribeParameter(name = "features", min = 1, max = 1) SimpleFeatureCollection features,
             @DescribeParameter(name = "attribute", min = 1, max = 1) String attribute,
             @DescribeParameter(name = "bbox", min = 0, max = 1) ReferencedEnvelope bbox,
             @DescribeParameter(name = "width", min = 1, max = 1) Integer width,
             @DescribeParameter(name = "height", min = 1, max = 1) Integer height) throws Exception {
 
         return new Process(features, attribute, bbox, width, height).execute();
 
     }
 
     private class Process {
 
         private final SimpleFeatureCollection featureCollection;
         private final String attributeName;
         
         private final ReferencedEnvelope coverageEnvelope;
         private final int coverageWidth;
         private final int coverageHeight;
         private ReferencedEnvelope extent;
         private GridGeometry2D gridGeometry;
         private MathTransform featureToRasterTransform;
         private int[] coordGridX = new int[COORD_GRID_CHUNK_SIZE];
         private int[] coordGridY = new int[COORD_GRID_CHUNK_SIZE];
         
         private BufferedImage image;
         private Graphics2D graphics;
 
         private AttributeRange attributeRange;
         
         private Process(SimpleFeatureCollection featureCollection,
                 String className,
                 ReferencedEnvelope coverageEnvelope,
                 int coverageWidth,
                 int coverageHeight) {
             this.featureCollection = featureCollection;
             this.attributeName = className;
             this.coverageEnvelope = coverageEnvelope;
             this.coverageWidth = coverageWidth;
             this.coverageHeight = coverageHeight;
         }
 
         private GridCoverage2D execute() throws Exception {
 
             initialize();
 
             SimpleFeatureIterator featureIterator = featureCollection.features();
             try {
                 while (featureIterator.hasNext()) {
                     processFeature(featureIterator.next(), attributeName);
                 }
             } finally {
                 featureIterator.close();
             }
 
             GridCoverageFactory gcf = new GridCoverageFactory();
             return gcf.create(
                     getClass().getSimpleName() + "-" + UUID.randomUUID().toString(),
                     image, extent);
         }
 
         private void initialize() {
             
             AttributeDescriptor attributeDescriptor = featureCollection.getSchema().getDescriptor(attributeName);
             if (attributeDescriptor == null) {
                 throw new RuntimeException(attributeName + " not found");
             }
 
             Class<?> attClass = attributeDescriptor.getType().getBinding();
             if (!Number.class.isAssignableFrom(attClass)) {
                 throw new RuntimeException(attributeName + " is not numeric type");
             }
 
             try {
                 setBounds(featureCollection, coverageEnvelope);
             } catch (TransformException ex) {
                 throw new RuntimeException(ex);
             }
 
             createImage();
 
             gridGeometry = new GridGeometry2D(new GridEnvelope2D(0, 0, coverageWidth, coverageHeight), extent);
             
             String id = featureCollection.getID().intern();
             synchronized (id) {
                 Map<String, AttributeRange> attributeRangeMap = featureAttributeRangeMap.get(id);
                 if (attributeRangeMap == null) {
                     attributeRangeMap = new WeakHashMap<String, AttributeRange>();
                     featureAttributeRangeMap.put(id, attributeRangeMap);
                 }
                 attributeRange = attributeRangeMap.get(attributeName);
                 if (attributeRange == null) {
                     SimpleFeatureIterator iterator = featureCollection.features();
                     if (iterator.hasNext()) {
                         double value = ((Number)iterator.next().getAttribute(attributeName)).doubleValue();
                         double minimum = value;
                         double maximum = value;
                         while (iterator.hasNext()) {
                             SimpleFeature feature = iterator.next();
                             value = ((Number)feature.getAttribute(attributeName)).doubleValue();
                             if (value > maximum) {
                                 maximum = value;
                             } else if (value < minimum) {
                                 minimum = value;
                             }
                         }
                         attributeRange = new AttributeRange(minimum, maximum);
                         attributeRangeMap.put(attributeName, attributeRange);
                     }
                 }
             }
         }
 
         private void processFeature(SimpleFeature feature, String attributeName) throws Exception {
 
             Geometry geometry = (Geometry) feature.getDefaultGeometry();
             Object attributeValue = feature.getAttribute(attributeName);
             if (!(attributeValue instanceof Number)) {
                 return;
             }
 
             if (extent.intersects(feature.getBounds().toBounds(extent.getCoordinateReferenceSystem()))) {
                 graphics.setColor(valueToColor(((Number)attributeValue)));
                 Geometries geomType = Geometries.get(geometry);
                 switch (geomType) {
                     case MULTIPOLYGON:
                     case MULTILINESTRING:
                     case MULTIPOINT:
                         final int numGeom = geometry.getNumGeometries();
                         for (int i = 0; i < numGeom; i++) {
                             Geometry geomN = geometry.getGeometryN(i);
                             drawGeometry(Geometries.get(geomN), geomN);
                         }
                         break;
                     case POLYGON:
                     case LINESTRING:
                     case POINT:
                         drawGeometry(geomType, geometry);
                         break;
                     default:
                     // TODO:  Log!
                 }
             }
         }
 
        private void setBounds(SimpleFeatureCollection features, ReferencedEnvelope requestBounds) throws TransformException {
 
             ReferencedEnvelope featureBounds = features.getBounds();
            if (featureBounds.getCoordinateReferenceSystem() == null) {
                // bug in GeoTools where ReprojectFeatureStore.getBounds() doesn't include CRS
                featureBounds = new ReferencedEnvelope(featureBounds, features.getSchema().getCoordinateReferenceSystem());
            }
             if (requestBounds == null) {
                 requestBounds = featureBounds;
             }
             
            extent = requestBounds;
 
             CoordinateReferenceSystem featuresCRS = featureBounds.getCoordinateReferenceSystem();
             CoordinateReferenceSystem requestCRS = requestBounds.getCoordinateReferenceSystem();
 
             if (featuresCRS != null && requestCRS != null && !CRS.equalsIgnoreMetadata(requestCRS, featuresCRS)) {
                 try {
                     featureToRasterTransform = CRS.findMathTransform(featuresCRS, requestCRS, true);
                 } catch (Exception ex) {
                     throw new TransformException("Unable to transform features into output coordinate reference system", ex);
                 }
             }
         }
 
         private void createImage() {
 
             if (GraphicsEnvironment.isHeadless()) {
                 image = new BufferedImage(coverageWidth, coverageHeight, BufferedImage.TYPE_4BYTE_ABGR);
             } else {
                 image = GraphicsEnvironment.
                         getLocalGraphicsEnvironment().
                         getDefaultScreenDevice().
                         getDefaultConfiguration().
                         createCompatibleImage(coverageWidth, coverageHeight, Transparency.TRANSLUCENT);
             }
             image.setAccelerationPriority(1f);
             graphics = image.createGraphics();
         }
 
         private void drawGeometry(Geometries geomType, Geometry geometry) throws TransformException {
             if (featureToRasterTransform != null) {
                 try {
                     geometry = JTS.transform(geometry, featureToRasterTransform);
                 } catch (TransformException ex) {
                     throw ex;
                 } catch (MismatchedDimensionException ex) {
                     throw new RuntimeException(ex);
                 }
             }
 
             Coordinate[] coords = geometry.getCoordinates();
 
             // enlarge if needed
             if (coords.length > coordGridX.length) {
                 int n = coords.length / COORD_GRID_CHUNK_SIZE + 1;
                 coordGridX = new int[n * COORD_GRID_CHUNK_SIZE];
                 coordGridY = new int[n * COORD_GRID_CHUNK_SIZE];
             }
 
             // Go through coordinate array in order received
             DirectPosition2D worldPos = new DirectPosition2D();
             for (int n = 0; n < coords.length; n++) {
                 worldPos.setLocation(coords[n].x, coords[n].y);
                 GridCoordinates2D gridPos = gridGeometry.worldToGrid(worldPos);
                 coordGridX[n] = gridPos.x;
                 coordGridY[n] = gridPos.y;
             }
 
             switch (geomType) {
                 case POLYGON:
                     graphics.fillPolygon(coordGridX, coordGridY, coords.length);
                     break;
                 case LINESTRING:
                     graphics.drawPolyline(coordGridX, coordGridY, coords.length);
                     break;
                 case POINT:
                     graphics.fillRect(coordGridX[0], coordGridY[0], 1, 1);
                     break;
                 default:
                    // nothing to do...
             }
         }
 
         private Color valueToColor(Number value) {
             int valueAsInt = (int)((value.doubleValue() - attributeRange.min) / attributeRange.extent * 255d);
             if (valueAsInt > -1 && valueAsInt < 256) {
             return new Color(valueAsInt, valueAsInt, valueAsInt, 255);
             } else {
                 return Color.white;
             }
         }
     }
 }
