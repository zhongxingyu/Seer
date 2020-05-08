 package gov.usgs.cida.gdp.coreprocessing.analysis.grid;
 
 import ucar.nc2.dataset.CoordinateAxis1D;
 import org.slf4j.Logger;
 import gov.usgs.cida.gdp.coreprocessing.Delimiter;
 import gov.usgs.cida.gdp.coreprocessing.analysis.grid.Statistics1DWriter.GroupBy;
 import gov.usgs.cida.gdp.coreprocessing.analysis.grid.Statistics1DWriter.Statistic;
 import gov.usgs.cida.gdp.coreprocessing.analysis.statistics.WeightedStatistics1D;
 import gov.usgs.cida.gdp.coreprocessing.analysis.grid.GridCellCoverageFactory.GridCellCoverageByIndex;
 import gov.usgs.cida.gdp.coreprocessing.analysis.grid.GridCellCoverageFactory.GridCellIndexCoverage;
 
 import java.text.SimpleDateFormat;
 import java.io.IOException;
 import java.util.ArrayList;
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
 import ucar.nc2.dt.GridDataset;
 import ucar.nc2.dt.GridDatatype;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 import java.io.Writer;
 
 public class FeatureCoverageWeightedGridStatistics {
     
     public final static Logger LOGGER = LoggerFactory.getLogger(FeatureCoverageWeightedGridStatistics.class);
 
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
                    "Variable named %s not found in gridded dataset %s",
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
             throws IOException, InvalidRangeException, FactoryException, TransformException, SchemaException
     {
         
         GridType gt = GridType.findGridType(gridDatatype.getCoordinateSystem());
         
         if( !(gt == GridType.ZYX || gt == GridType.TZYX || gt == GridType.YX || gt == GridType.TYX) ) {
             throw new IllegalStateException("Currently require y-x or t-y-x grid with this operation");
         }
 
         Range[] ranges = GridUtility.getXYRangesFromBoundingBox(
                 featureCollection.getBounds(),
                 gridDatatype.getCoordinateSystem(),
                 requireFullCoverage);
         gridDatatype = gridDatatype.makeSubset(null, null, timeRange, null, ranges[1], ranges[0]);
 
         GridCellCoverageByIndex coverageByIndex =
 				GridCellCoverageFactory.generateFeatureAttributeCoverageByIndex(
                     featureCollection,
                     attributeName,
                     gridDatatype.getCoordinateSystem());
 
         String variableUnits = gridDatatype.getVariable().getUnitsString();
 
         List<Object> attributeList = coverageByIndex.getAttributeValueList();
 
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
 
         WeightedGridStatisticsVisitor v = new WeightedGridStatisticsVisitor(coverageByIndex, writerX);
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
 
 
     protected static class WeightedGridStatisticsVisitor extends FeatureCoverageGridCellVisitor {
 
         public final static String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
         public final static String TIMEZONE = "UTC";
         
         protected final SimpleDateFormat dateFormat;
         
         protected final Statistics1DWriter writer;
         
         protected Map<Object, WeightedStatistics1D> perAttributeStatistics;
         protected WeightedStatistics1D allAttributeStatistics;
         
         protected Map<Object, WeightedStatistics1D> allTimestepPerAttributeStatistics;
         protected WeightedStatistics1D allTimestepAllAttributeStatistics;
         
         protected CoordinateAxis1D zAxis;
         protected String zLabel;
         
         protected CoordinateAxis1DTime tAxis;
         protected String tLabel;
         
         
         public WeightedGridStatisticsVisitor(GridCellCoverageByIndex coverageByIndex, Statistics1DWriter writer) {
             super(coverageByIndex);
             this.writer = writer;
             
             dateFormat = new SimpleDateFormat(DATE_FORMAT);
             dateFormat.setTimeZone(TimeZone.getTimeZone(TIMEZONE));
         }
 
         protected Map<Object, WeightedStatistics1D> createPerAttributeStatisticsMap() {
             Map map = new LinkedHashMap<Object, WeightedStatistics1D>();
             for (Object attributeValue : coverageByIndex.getAttributeValueList()) {
                 map.put(attributeValue, new WeightedStatistics1D());
             }
             return map;
         }
 
         @Override
         public void traverseStart(GridDatatype gridDataType) {
             super.traverseStart(gridDataType);
             tAxis = gridDataType.getCoordinateSystem().getTimeAxis1D();
             zAxis = gridDataType.getCoordinateSystem().getVerticalAxis();
 
             allTimestepPerAttributeStatistics = createPerAttributeStatisticsMap();
             allTimestepAllAttributeStatistics = new WeightedStatistics1D();
             
             try {
                 writer.writerHeader(buildRowLabel(
                         tAxis == null ? "" : Statistics1DWriter.TIMESTEPS_LABEL,
                         zAxis == null ? null : String.format("%s(%s)", zAxis.getShortName(), zAxis.getUnitsString())));
             } catch (IOException e) {
                 
             }
         }
         
         @Override
         public boolean tStart(int tIndex) {
             super.tStart(tIndex);
             tLabel = dateFormat.format(tAxis.getCalendarDate(tIndex).toDate());
             return true;
         }
         
         @Override
         public boolean zStart(int zIndex) {
             super.zStart(zIndex);
             zLabel = Double.toString(zAxis.getCoordValue(zIndex));
             return true;
         }
         
         @Override
         public void yxStart() {
             super.yxStart();
             perAttributeStatistics = createPerAttributeStatisticsMap();
             allAttributeStatistics = new WeightedStatistics1D();
         }
 
         @Override
         public void processPerAttributeGridCellCoverage(double value, double coverage, Object attribute) {
             perAttributeStatistics.get(attribute).accumulate(value, coverage);
             allTimestepPerAttributeStatistics.get(attribute).accumulate(value, coverage);
         }
 
         @Override
         public void processAllAttributeGridCellCoverage(double value, double coverage) {
             allAttributeStatistics.accumulate(value, coverage);
             allTimestepAllAttributeStatistics.accumulate(value, coverage);
         }
         
         @Override
         public void yxEnd() {
             super.yxEnd();
             try {
                 writer.writeRow(
                             buildRowLabel(),
                             perAttributeStatistics.values(),
                             allAttributeStatistics);
             } catch (IOException e) {
                 
             }
         }
         
         @Override
         public void traverseEnd() {
             super.traverseEnd();
             try {
                 if (writer.isSummarizeFeatureAttribute() && tAxis != null) {
                     writer.writeRow(
                             buildRowLabel(Statistics1DWriter.ALL_TIMESTEPS_LABEL, zLabel == null ? null : ""),
                             allTimestepPerAttributeStatistics.values(),
                             allTimestepAllAttributeStatistics);
                 }
             } catch (IOException ex) {
                 // TODO
             }
         }
         
         private List<String> buildRowLabel() {
             return buildRowLabel(tLabel, zLabel);
         }
         
         private List<String> buildRowLabel(String tLabel, String zLabel) {
             List<String> rowLabelList = new ArrayList<String>(2);
             rowLabelList.add(tLabel == null ? "" : tLabel);
             if (zLabel != null) {
                 rowLabelList.add(zLabel);
             }
             return rowLabelList;
         }
     }
 }
