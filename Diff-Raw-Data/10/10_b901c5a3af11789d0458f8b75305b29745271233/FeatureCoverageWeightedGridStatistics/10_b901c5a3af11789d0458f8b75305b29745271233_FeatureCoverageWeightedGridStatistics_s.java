 package gov.usgs.cida.gdp.coreprocessing.analysis.grid;
 
 import org.slf4j.Logger;
 import gov.usgs.cida.gdp.coreprocessing.Delimiter;
 import gov.usgs.cida.gdp.coreprocessing.analysis.grid.FeatureCoverageWeightedGridStatisticsWriter.Statistic;
 import gov.usgs.cida.gdp.coreprocessing.analysis.statistics.WeightedStatistics1D;
 import gov.usgs.cida.gdp.coreprocessing.analysis.grid.GridCellCoverageFactory.GridCellCoverageByIndex;
 import gov.usgs.cida.gdp.coreprocessing.analysis.grid.GridCellCoverageFactory.GridCellIndexCoverage;
 
 import java.util.Date;
 import java.text.SimpleDateFormat;
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.TimeZone;
 
 import org.geotools.feature.FeatureCollection;
 import org.geotools.feature.SchemaException;
 import org.opengis.feature.simple.SimpleFeature;
 import org.opengis.feature.simple.SimpleFeatureType;
 import org.opengis.referencing.FactoryException;
 import org.opengis.referencing.operation.TransformException;
 import org.slf4j.LoggerFactory;
 
 import ucar.ma2.InvalidRangeException;
 import ucar.ma2.Range;
 import ucar.nc2.dataset.CoordinateAxis1DTime;
 import ucar.nc2.dt.GridCoordSystem;
 import ucar.nc2.dt.GridDataset;
 import ucar.nc2.dt.GridDatatype;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 
 public class FeatureCoverageWeightedGridStatistics {
     
     public final static Logger LOGGER = LoggerFactory.getLogger(FeatureCoverageWeightedGridStatistics.class);
 
     public enum GroupBy {
         STATISTIC,
         FEATURE_ATTRIBUTE;
     }
 
     public static void execute(
             FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection,
             String attributeName,
             GridDataset gridDataset,
             String variableName,
             Range timeRange,
             List<Statistic> statisticList,
             BufferedWriter writer,
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
             BufferedWriter writer,
             GroupBy groupBy,
             Delimiter delimiter,
             boolean requireFullCoverage,
             boolean summarizeTimeStep,
             boolean summarizeFeatures)
             throws IOException, InvalidRangeException, FactoryException, TransformException, SchemaException
     {
         
         GridType gt = GridType.findGridType(gridDatatype.getCoordinateSystem());
         if( !(gt == GridType.YX || gt == GridType.TYX) ) {
             throw new IllegalStateException("Currently require y-x or t-y-x grid for this operation");
         }
 
         Range[] ranges = GridUtility.getXYRangesFromBoundingBox(
                 featureCollection.getBounds(),
                 gridDatatype.getCoordinateSystem(),
                 requireFullCoverage);
         gridDatatype = gridDatatype.makeSubset(null, null, timeRange, null, ranges[1], ranges[0]);
 
         GridCoordSystem gcs = gridDatatype.getCoordinateSystem();
 
         GridCellCoverageByIndex coverageByIndex =
 				GridCellCoverageFactory.generateFeatureAttributeCoverageByIndex(
                     featureCollection,
                     attributeName,
                     gridDatatype.getCoordinateSystem());
 
         String variableUnits = gridDatatype.getVariable().getUnitsString();
 
         List<Object> attributeList = coverageByIndex.getAttributeValueList();
 
         FeatureCoverageWeightedGridStatisticsWriter writerX =
                 new FeatureCoverageWeightedGridStatisticsWriter(
                     attributeList,
                     gridDatatype.getName(),
                     variableUnits,
                     statisticList,
                     groupBy != groupBy.FEATURE_ATTRIBUTE,  // != in case value equals null, default to GroupBy.STATISTIC
                     delimiter.delimiter,
                     summarizeTimeStep,
                     summarizeFeatures,
                     writer);
 
         WeightedGridStatisticsVisitor v = null;
         switch (gt) {
             case YX:
                 v = new WeightedGridStatisticsVisitor_YX(coverageByIndex, writerX);
             break;
             case TYX:
                 v = new WeightedGridStatisticsVisitor_TYX(coverageByIndex, writerX);
             break;
             default:
                 throw new IllegalStateException("Currently require y-x or t-y-x grid for this operation");
         }
 
         GridCellTraverser gct = new GridCellTraverser(gridDatatype);
 
         gct.traverse(v);
     }
 
     public static abstract class FeatureCoverageGridCellVisitor extends GridCellVisitor {
 
         final protected GridCellCoverageByIndex coverageByIndex;
 
         public FeatureCoverageGridCellVisitor(GridCellCoverageByIndex coverageByIndex) {
             this.coverageByIndex = coverageByIndex;
         }
 
         @Override
         public void processGridCell(int xCellIndex, int yCellIndex, double value) {
             double coverageTotal = 0;
 			List<GridCellIndexCoverage> list = coverageByIndex.getCoverageList(xCellIndex, yCellIndex);
 			if (list != null) {
 				for (GridCellIndexCoverage c : list) {
 					if (c.coverage > 0.0) {
 						processPerAttributeGridCellCoverage(value, c.coverage, c.attribute);
 					}
 					coverageTotal += c.coverage;
 				}
 			}
             if (coverageTotal > 0.0) {
                 processAllAttributeGridCellCoverage(value, coverageTotal);
             }
         }
 
         public abstract void processPerAttributeGridCellCoverage(double value, double coverage, Object attribute);
 
         public abstract void processAllAttributeGridCellCoverage(double value, double coverage);
 
     }
 
 
     protected static abstract class WeightedGridStatisticsVisitor extends FeatureCoverageGridCellVisitor {
 
         protected Map<Object, WeightedStatistics1D> perAttributeStatistics;
         protected WeightedStatistics1D allAttributeStatistics;
 
         public WeightedGridStatisticsVisitor(GridCellCoverageByIndex coverageByIndex) {
             super(coverageByIndex);
         }
 
         protected Map<Object, WeightedStatistics1D> createPerAttributeStatisticsMap() {
             Map map = new LinkedHashMap<Object, WeightedStatistics1D>();
             for (Object attributeValue : coverageByIndex.getAttributeValueList()) {
                 map.put(attributeValue, new WeightedStatistics1D());
             }
             return map;
         }
 
         @Override
         public void yxStart() {
             perAttributeStatistics = createPerAttributeStatisticsMap();
             allAttributeStatistics = new WeightedStatistics1D();
         }
 
         @Override
         public void processPerAttributeGridCellCoverage(double value, double coverage, Object attribute) {
             perAttributeStatistics.get(attribute).accumulate(value, coverage);
         }
 
         @Override
         public void processAllAttributeGridCellCoverage(double value, double coverage) {
             allAttributeStatistics.accumulate(value, coverage);
         }
     }
 
     protected static class WeightedGridStatisticsVisitor_YX extends WeightedGridStatisticsVisitor {
 
         FeatureCoverageWeightedGridStatisticsWriter writer;
 
         WeightedGridStatisticsVisitor_YX(
                 GridCellCoverageByIndex coverageByIndex,
                 FeatureCoverageWeightedGridStatisticsWriter writer) {
             super(coverageByIndex);
             this.writer = writer;
         }
 
         @Override
         public void traverseStart(GridCoordSystem gridCoordSystem) {
             try {
                 writer.writerHeader(null);
             } catch (IOException ex) {
                 // TODO
             }
         }
 
         @Override
         public void traverseEnd() {
             try {
                if (writer.isSummarizeTimeStep()) {
                    writer.writeRow(
                            null,
                            perAttributeStatistics.values(),
                            allAttributeStatistics);
                }
             } catch (IOException ex) {
                 // TODO
             }
         }
 
     }
 
     protected static class WeightedGridStatisticsVisitor_TYX extends WeightedGridStatisticsVisitor {
         
         public final static String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
         public final static String TIMEZONE = "UTC";
 
         protected Map<Object, WeightedStatistics1D> allTimestepPerAttributeStatistics;
         protected WeightedStatistics1D allTimestepAllAttributeStatistics;
 
         protected FeatureCoverageWeightedGridStatisticsWriter writer;
         
         protected CoordinateAxis1DTime tAxis;
         
         protected SimpleDateFormat dateFormat;
 
         public WeightedGridStatisticsVisitor_TYX(
                 GridCellCoverageByIndex coverageByIndex,
                 FeatureCoverageWeightedGridStatisticsWriter writer) {
             super(coverageByIndex);
             this.writer = writer;
             
             dateFormat = new SimpleDateFormat(DATE_FORMAT);
             dateFormat.setTimeZone(TimeZone.getTimeZone(TIMEZONE));
         }
 
         @Override
         public void traverseStart(GridCoordSystem gridCoordSystem) {
             
             try {
                 writer.writerHeader(FeatureCoverageWeightedGridStatisticsWriter.TIMESTEPS_LABEL);
             } catch (IOException ex) {
                 // TODO
             }
 
             allTimestepPerAttributeStatistics =
                     createPerAttributeStatisticsMap();
             allTimestepAllAttributeStatistics = new WeightedStatistics1D();
             
             tAxis = gridCoordSystem.getTimeAxis1D();
         }
 
         @Override
         public void processPerAttributeGridCellCoverage(double value, double coverage, Object attribute) {
             super.processPerAttributeGridCellCoverage(value, coverage, attribute);
             allTimestepPerAttributeStatistics.get(attribute).accumulate(value, coverage);
         }
 
         @Override
         public void processAllAttributeGridCellCoverage(double value, double coverage) {
             super.processAllAttributeGridCellCoverage(value, coverage);
             allTimestepAllAttributeStatistics.accumulate(value, coverage);
         }
 
         @Override
         public void tEnd(int tIndex) {
             try {
                 Date date = tAxis.getTimeDate(tIndex);
                 writer.writeRow(
                         dateFormat.format(date),
                         perAttributeStatistics.values(),
                         allAttributeStatistics);
             } catch (IOException e) {
                 // TODO
             }
         }
 
         @Override
         public void traverseEnd() {
             try {
                 if (writer.isSummarizeFeatureAttribute()) {
                     writer.writeRow(
                             FeatureCoverageWeightedGridStatisticsWriter.ALL_TIMESTEPS_LABEL,
                             allTimestepPerAttributeStatistics.values(),
                             allTimestepAllAttributeStatistics);
                 }
             } catch (IOException ex) {
                 // TODO
             }
         }
     }
 
 }
