 package gov.usgs.cida.coastalhazards.wps;
 
 import com.vividsolutions.jts.geom.GeometryFactory;
 import com.vividsolutions.jts.geom.PrecisionModel;
 import gov.usgs.cida.coastalhazards.wps.exceptions.UnsupportedCoordinateReferenceSystemException;
 import org.geoserver.wps.gs.GeoServerProcess;
 import org.geotools.data.simple.SimpleFeatureCollection;
 import org.geotools.feature.FeatureCollection;
 import org.geotools.feature.FeatureIterator;
 import org.geotools.process.factory.DescribeParameter;
 import org.geotools.process.factory.DescribeProcess;
 import org.geotools.process.factory.DescribeResult;
 import org.geotools.referencing.crs.DefaultGeographicCRS;
 import org.opengis.feature.Feature;
 import org.opengis.feature.GeometryAttribute;
 import org.opengis.feature.simple.SimpleFeature;
 import org.opengis.feature.simple.SimpleFeatureType;
 import org.opengis.feature.type.FeatureType;
 import org.opengis.referencing.crs.CoordinateReferenceSystem;
 
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
 
     /* May actually want to return reference to new layer*/
     @DescribeResult(name = "transects", description = "Layer containing Transects normal to baseline")
     public int execute(
             @DescribeParameter(name = "shorelines", min = 1, max = 1) SimpleFeatureCollection shorelines,
             @DescribeParameter(name = "baseline", min = 1, max = 1) SimpleFeatureCollection baseline,
             @DescribeParameter(name = "spacing", min = 1, max = 1) Float spacing,
             @DescribeParameter(name = "workspace", min = 1, max = 1) String workspace,
             @DescribeParameter(name = "store", min = 1, max = 1) String store,
             @DescribeParameter(name = "layer", min = 1, max = 1) String layer) throws Exception {
         return new Process(shorelines, baseline, spacing, workspace, store, layer).execute();
     }
     
     private class Process {
         
         private final FeatureCollection<SimpleFeatureType, SimpleFeature> shorelines;
         private final FeatureCollection<SimpleFeatureType, SimpleFeature> baseline;
         private final float spacing;
         private final String workspace;
         private final String store;
         private final String layer;
         
         private final GeometryFactory geometryFactory;
         
         private Process(SimpleFeatureCollection shorelines,
                 SimpleFeatureCollection baseline,
                 float spacing,
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
         
         private int execute() throws Exception {
             CoordinateReferenceSystem shorelinesCrs = findCRS(shorelines);
             CoordinateReferenceSystem baselineCrs = findCRS(baseline);
             if (!shorelinesCrs.equals(ACCEPTED_CRS)) {
                 throw new UnsupportedCoordinateReferenceSystemException("Shorelines are not in accepted projection");
             }
             if (!baselineCrs.equals(ACCEPTED_CRS)) {
                 throw new UnsupportedCoordinateReferenceSystemException("Baseline is not in accepted projection");
             }
             
             FeatureCollection pointsOnBaseline = getEvenlySpacedPointsAlongBaseline(baseline, spacing);
             FeatureCollection resultingTransects = getTransectsAtPoints(pointsOnBaseline, baseline, shorelines);
             addResultAsLayer(resultingTransects, workspace, store, layer);
             return 0;
         }
         
         private CoordinateReferenceSystem findCRS(FeatureCollection simpleFeatureCollection) {
             FeatureCollection shorelineFeatureCollection = (FeatureCollection)simpleFeatureCollection;
             FeatureType ft = shorelineFeatureCollection.getSchema();
             CoordinateReferenceSystem coordinateReferenceSystem = ft.getCoordinateReferenceSystem();
             return coordinateReferenceSystem;
         }
         
         private CoordinateReferenceSystem findBestUTMZone(FeatureCollection simpleFeatureCollection) {
             throw new UnsupportedOperationException("Not yet implemented");
         }
 
         private FeatureCollection getEvenlySpacedPointsAlongBaseline(FeatureCollection baseline, float spacing) {
             FeatureIterator features = baseline.features();
             Feature feature = null;
             while (features.hasNext()) {
                 feature = features.next();
                 FeatureType type = feature.getType();
                 GeometryAttribute geom = feature.getDefaultGeometryProperty();
                geom.
             }
             throw new UnsupportedOperationException("Not yet implemented");
         }
         
         private FeatureCollection getTransectsAtPoints(FeatureCollection pointsOnBaseline, FeatureCollection<SimpleFeatureType, SimpleFeature> baseline, FeatureCollection<SimpleFeatureType, SimpleFeature> shorelines) {
             throw new UnsupportedOperationException("Not yet implemented");
         }
         
         private void addResultAsLayer(FeatureCollection transects, String workspace, String store, String layer) {
             throw new UnsupportedOperationException("Not yet implemented");
         }
 
     }
 }
