 package gov.usgs.cida.gdp.coreprocessing.analysis.grid;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 import gov.usgs.cida.gdp.coreprocessing.Delimiter;
 import gov.usgs.cida.gdp.coreprocessing.analysis.grid.Statistics1DWriter.Statistic;
 import gov.usgs.cida.gdp.coreprocessing.analysis.statistics.Statistics1D;
 import com.vividsolutions.jts.geom.Coordinate;
 import com.vividsolutions.jts.geom.Geometry;
 import com.vividsolutions.jts.geom.GeometryFactory;
 import com.vividsolutions.jts.geom.prep.PreparedGeometry;
 import com.vividsolutions.jts.geom.prep.PreparedGeometryFactory;
 import gov.usgs.cida.gdp.coreprocessing.analysis.grid.GridUtility.IndexToCoordinateBuilder;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 import org.geotools.feature.FeatureCollection;
 import org.geotools.feature.SchemaException;
 import org.geotools.geometry.jts.JTS;
 import org.geotools.referencing.CRS;
 import org.opengis.feature.simple.SimpleFeature;
 import org.opengis.feature.simple.SimpleFeatureType;
 import org.opengis.feature.type.AttributeDescriptor;
 import org.opengis.geometry.BoundingBox;
 import org.opengis.referencing.FactoryException;
 import org.opengis.referencing.crs.CoordinateReferenceSystem;
 import org.opengis.referencing.operation.MathTransform;
 import org.opengis.referencing.operation.TransformException;
 import ucar.ma2.InvalidRangeException;
 import ucar.ma2.Range;
 import ucar.nc2.dt.GridCoordSystem;
 import ucar.nc2.dt.GridDataset;
 import ucar.nc2.dt.GridDatatype;
 
 import gov.usgs.cida.gdp.coreprocessing.analysis.grid.Statistics1DWriter.GroupBy;
 import java.io.Writer;
 import org.geotools.feature.FeatureIterator;
 
 /**
  *
  * @author tkunicki
  */
 public class FeatureCoverageGridStatistics {
 
     public static void execute(
             FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection,
             String attributeName,
             GridDataset gridDataset,
             String variableName,
             Range timeRange,
             List<Statistic> statisticList,
             Writer writer,
             GroupBy groupBy,
             Delimiter delimiter)
             throws IOException, InvalidRangeException, FactoryException, TransformException, SchemaException
     {
         GridDatatype gridDatatype = checkNotNull(
                     gridDataset.findGridDatatype(variableName),
                     "Variable named %s not found in girdded dataset %s",
                     variableName);
 
         execute(featureCollection,
                 attributeName,
                 gridDatatype,
                 timeRange,
                 statisticList,
                 writer,
                 groupBy,
                 delimiter,
                 true,
                 false,
                 false);
     }
 
     public static void execute(
             FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection,
             String attributeName,
             GridDatatype gridDatatype,
             Range timeRange,
             List<Statistic> statisticList,
             Writer writer,
             GroupBy groupBy,
             Delimiter delimiter,
             boolean requireFullCoverage,
             boolean summarizeTimeStep,
             boolean summarizeFeatures)
             throws IOException, InvalidRangeException, FactoryException, TransformException, SchemaException {
 
         GridCoordSystem gcs = gridDatatype.getCoordinateSystem();
         GridType gt = GridType.findGridType(gcs);
         if (gt != GridType.YX) {
             throw new IllegalStateException("Currently require y-x grid for this operation");
         }
 
         // these two calls are used to test for coverage/intersection based on 'requireFullCoverage',
         // if required coverage criterea is not fufilled an exception will be thrown.
         Range[] featureCollectionRanges = GridUtility.getXYRangesFromBoundingBox(featureCollection.getBounds(), gcs, requireFullCoverage);
         gridDatatype = gridDatatype.makeSubset(null, null, null, null, featureCollectionRanges[1], featureCollectionRanges[0]);
 
         CoordinateReferenceSystem gridCRS = CRSUtility.getCRSFromGridCoordSystem(gcs);
         CoordinateReferenceSystem featureCRS =
                 featureCollection.getSchema().getCoordinateReferenceSystem();
 
         MathTransform gridToFeatureTransform = CRS.findMathTransform(
                 gridCRS,
                 featureCRS,
                 true);
 
         AttributeDescriptor attributeDescriptor =
                 featureCollection.getSchema().getDescriptor(attributeName);
         if (attributeDescriptor == null) {
             throw new IllegalArgumentException(
                     "Attribute " + attributeName + " not found in FeatureCollection.");
         }
 
         boolean attributeComparable = Comparable.class.isAssignableFrom(
                 attributeDescriptor.getType().getBinding());
 
         Map<Object, Statistics1D> perTimestepPerAttributeStatisticsMap;
         Statistics1D perTimestepAllAttributeStatistics;
         
         Map<Object, Statistics1D> allTimestepPerAttributeStatisticsMap;
         Statistics1D allTimestepAllAttributeStatistics;
         
         FeatureIterator<SimpleFeature> featureIterator = featureCollection.features();
 
         allTimestepPerAttributeStatisticsMap = create(attributeComparable);
         allTimestepAllAttributeStatistics = new Statistics1D();
         
         try {
             
             perTimestepPerAttributeStatisticsMap = create(attributeComparable);
             perTimestepAllAttributeStatistics = new Statistics1D();
             
             while (featureIterator.hasNext()) {
 
                 SimpleFeature feature = featureIterator.next();
                 Object attribute = feature.getAttribute(attributeName);
                 
                 if (attribute != null) {
 
                     BoundingBox featureBoundingBox = feature.getBounds();
 
                     Geometry featureGeometry = (Geometry) feature.getDefaultGeometry();
 
                     try {
                         Range[] featureRanges = GridUtility.getXYRangesFromBoundingBox(
                                 featureBoundingBox, gridDatatype.getCoordinateSystem(), requireFullCoverage);
 
                         GridDatatype featureGridDataType = gridDatatype.makeSubset(null, null, null, null, featureRanges[1], featureRanges[0]);
 
                         PreparedGeometry preparedGeometry = PreparedGeometryFactory.prepare(featureGeometry);
 
                         Statistics1D perTimestepPerFeatureStatistics = new Statistics1D();
                         GridCellTraverser traverser = new GridCellTraverser(featureGridDataType);
                         traverser.traverse(new FeatureCoverageGridStatistics.FeatureGridCellVisitor(
                                 preparedGeometry,
                                 gridToFeatureTransform,
                                 perTimestepPerFeatureStatistics));
                         
                         extract(perTimestepPerAttributeStatisticsMap, attribute).accumulate(perTimestepPerFeatureStatistics);
                         extract(allTimestepPerAttributeStatisticsMap, attribute).accumulate(perTimestepPerFeatureStatistics);                        
                         perTimestepAllAttributeStatistics.accumulate(perTimestepPerFeatureStatistics);
                         allTimestepAllAttributeStatistics.accumulate(perTimestepPerFeatureStatistics);
 
                     } catch (InvalidRangeException e) {
                         /* this may happen if the feature doesn't intersect the grid, this is OK */
                     }
                 }
             }
         } finally {
             featureIterator.close();
         }
 
 
         String variableUnits = gridDatatype.getVariable().getUnitsString();
         List<Object> attributeList = new ArrayList<Object>(perTimestepPerAttributeStatisticsMap.keySet());
 
         Statistics1DWriter writerX =
                 new Statistics1DWriter(
                     attributeList,
                     gridDatatype.getName(),
                     variableUnits,
                     statisticList,
                     groupBy != GroupBy.FEATURE_ATTRIBUTE,  // != in case value equals null, default to GroupBy.STATISTIC
                     delimiter.delimiter,
                     null, // default block separator used
                     summarizeTimeStep,
                     summarizeFeatures,
                     writer);
 
        writerX.writeHeader(null); // TODO! change if we add support for T or Z
         writerX.writeRow(null, perTimestepPerAttributeStatisticsMap.values(), perTimestepAllAttributeStatistics);
         if (summarizeFeatures) {
             // not yet...
         }
     }
 
     protected static class FeatureGridCellVisitor extends GridCellVisitor {
 
         private final GeometryFactory geometryFactory = new GeometryFactory();
         private final PreparedGeometry preparedGeometry;
         private final MathTransform gridToFeatureTransform;
         private final Statistics1D statistics;
         private IndexToCoordinateBuilder coordinateBuilder;
 
         protected FeatureGridCellVisitor(
                 PreparedGeometry preparedGeometry,
                 MathTransform gridToFeatureTransform,
                 Statistics1D statistics) {
             this.preparedGeometry = preparedGeometry;
             this.gridToFeatureTransform = gridToFeatureTransform;
             this.statistics = statistics;
         }
 
         @Override
         public void traverseStart(GridDatatype gridDatatype) {
             coordinateBuilder = GridUtility.generateIndexToCellCenterCoordinateBuilder(gridDatatype.getCoordinateSystem());
         }
 
         @Override
         public void processGridCell(int xCellIndex, int yCellIndex, double value) {
             Coordinate coordinate =
                     coordinateBuilder.getCoordinate(xCellIndex, yCellIndex);
             try {
                 JTS.transform(coordinate, coordinate, gridToFeatureTransform);
                 if (preparedGeometry.contains(geometryFactory.createPoint(coordinate))) {
                     statistics.accumulate(value);
                 }
             } catch (TransformException e) {
             }
         }
     }
     
     private static Map<Object, Statistics1D> create(boolean attributeComparable) {
         return attributeComparable ?
                 // rely on Comparable to sort
                 new TreeMap<Object, Statistics1D>() :
                 // use order from FeatureCollection.iterator();
                 new LinkedHashMap<Object, Statistics1D>();
     }
     
     private static Statistics1D extract(Map<Object, Statistics1D> map, Object attribute) {
         Statistics1D statistics = map.get(attribute);
         if (statistics == null) {
             statistics = new Statistics1D();
             map.put(attribute, statistics);
         }
         return statistics;
     }
 
 }
