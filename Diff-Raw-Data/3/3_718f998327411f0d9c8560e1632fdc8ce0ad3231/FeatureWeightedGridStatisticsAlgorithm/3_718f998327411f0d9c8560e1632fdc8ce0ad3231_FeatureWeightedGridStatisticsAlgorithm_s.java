 package gov.usgs.cida.gdp.wps.algorithm;
 
 import org.n52.wps.server.AbstractAnnotatedAlgorithm;
 import gov.usgs.cida.gdp.coreprocessing.Delimiter;
 import gov.usgs.cida.gdp.coreprocessing.analysis.grid.FeatureCoverageWeightedGridStatistics;
 import gov.usgs.cida.gdp.coreprocessing.analysis.grid.FeatureCoverageWeightedGridStatistics.GroupBy;
 import gov.usgs.cida.gdp.coreprocessing.analysis.grid.FeatureCoverageWeightedGridStatisticsWriter.Statistic;
 import org.n52.wps.algorithm.annotation.Algorithm;
 import org.n52.wps.algorithm.annotation.ComplexDataInput;
 import org.n52.wps.algorithm.annotation.ComplexDataOutput;
 import org.n52.wps.algorithm.annotation.LiteralDataInput;
 import org.n52.wps.algorithm.annotation.Process;
 import gov.usgs.cida.gdp.wps.binding.CSVFileBinding;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.net.URI;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 import org.apache.commons.io.IOUtils;
 import org.geotools.feature.FeatureCollection;
 import org.geotools.feature.SchemaException;
 import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
 import org.opengis.referencing.FactoryException;
 import org.opengis.referencing.operation.TransformException;
 import ucar.ma2.InvalidRangeException;
 import ucar.ma2.Range;
 import ucar.nc2.dt.GridDatatype;
 import ucar.nc2.ft.FeatureDataset;
 
 import static org.n52.wps.algorithm.annotation.LiteralDataInput.ENUM_COUNT;
 
 /**
  *
  * @author tkunicki
  */
 @Algorithm(
     version="1.0.0",
     title="Feature Weighted Grid Statistics",
     abstrakt="This algorithm generates area weighted statistics of a gridded dataset for a set of vector polygon features. Using the bounding-box that encloses the feature data and the time range, if provided, a subset of the gridded dataset is requested from the remote gridded data server. Polygon representations are generated for cells in the retrieved grid. The polygon grid-cell representations are then projected to the feature data coordinate reference system. The grid-cells are used to calculate per grid-cell feature coverage fractions. Area-weighted statistics are then calculated for each feature using the grid values and fractions as weights. If the gridded dataset has a time range the last step is repeated for each time step within the time range or all time steps if a time range was not supplied.")
 public class FeatureWeightedGridStatisticsAlgorithm extends AbstractAnnotatedAlgorithm {
 
     private FeatureCollection featureCollection;
     private String featureAttributeName;
     private URI datasetURI;
     private List<String> datasetId;
     private boolean requireFullCoverage = true;
     private Date timeStart;
     private Date timeEnd;
     private List<Statistic> statistics;
     private GroupBy groupBy;
     private Delimiter delimiter;
     private boolean summarizeTimeStep = false;
     private boolean summarizeFeatureAttribute = false;
 
     private File output;
 
     @ComplexDataInput(
             identifier=GDPAlgorithmConstants.FEATURE_COLLECTION_IDENTIFIER,
             title=GDPAlgorithmConstants.FEATURE_COLLECTION_TITLE,
             abstrakt=GDPAlgorithmConstants.FEATURE_COLLECTION_ABSTRACT,
             binding=GTVectorDataBinding.class)
     public void setFeatureCollection(FeatureCollection featureCollection) {
         this.featureCollection = featureCollection;
     }
 
     @LiteralDataInput(
             identifier=GDPAlgorithmConstants.FEATURE_ATTRIBUTE_NAME_IDENTIFIER,
             title=GDPAlgorithmConstants.FEATURE_ATTRIBUTE_NAME_TITLE,
             abstrakt=GDPAlgorithmConstants.FEATURE_ATTRIBUTE_NAME_ABSTRACT)
     public void setFeatureAttributeName(String featureAttributeName) {
         this.featureAttributeName = featureAttributeName;
     }
 
     @LiteralDataInput(
             identifier=GDPAlgorithmConstants.DATASET_URI_IDENTIFIER,
             title=GDPAlgorithmConstants.DATASET_URI_TITLE,
             abstrakt=GDPAlgorithmConstants.DATASET_URI_ABSTRACT)
     public void setDatasetURI(URI datasetURI) {
         this.datasetURI = datasetURI;
     }
 
     @LiteralDataInput(
             identifier=GDPAlgorithmConstants.DATASET_ID_IDENTIFIER,
             title=GDPAlgorithmConstants.DATASET_ID_TITLE,
             abstrakt=GDPAlgorithmConstants.DATASET_ID_ABSTRACT,
             maxOccurs= Integer.MAX_VALUE)
     public void setDatasetId(List<String> datasetId) {
         this.datasetId = datasetId;
     }
     
     @LiteralDataInput(
             identifier=GDPAlgorithmConstants.REQUIRE_FULL_COVERAGE_IDENTIFIER,
             title=GDPAlgorithmConstants.REQUIRE_FULL_COVERAGE_TITLE,
             abstrakt=GDPAlgorithmConstants.REQUIRE_FULL_COVERAGE_ABSTRACT,
             defaultValue="true")
     public void setRequireFullCoverage(boolean requireFullCoverage) {
         this.requireFullCoverage = requireFullCoverage;
     }
 
     @LiteralDataInput(
             identifier=GDPAlgorithmConstants.TIME_START_IDENTIFIER,
             title=GDPAlgorithmConstants.TIME_START_TITLE,
             abstrakt=GDPAlgorithmConstants.TIME_START_ABSTRACT,
             minOccurs=0)
     public void setTimeStart(Date timeStart) {
         this.timeStart = timeStart;
     }
 
     @LiteralDataInput(
         identifier=GDPAlgorithmConstants.TIME_END_IDENTIFIER,
         title=GDPAlgorithmConstants.TIME_END_TITLE,
         abstrakt=GDPAlgorithmConstants.TIME_END_ABSTRACT,
         minOccurs=0)
     public void setTimeEnd(Date timeEnd) {
         this.timeEnd = timeEnd;
     }
 
     @LiteralDataInput(
             identifier="STATISTICS",
             title="Statistics",
             abstrakt="Statistics that will be returned for each feature in the processing output.",
             maxOccurs=ENUM_COUNT)
     public void setStatistics(List<Statistic> statistics) {
         this.statistics = statistics;
     }
 
     @LiteralDataInput(
             identifier="GROUP_BY",
             title="Group By",
             abstrakt="If multiple features and statistics are selected, this will change whether the processing output columns are sorted according to statistics or feature attributes.")
     public void setGroupBy(GroupBy groupBy) {
         this.groupBy = groupBy;
     }
 
     @LiteralDataInput(
         identifier=GDPAlgorithmConstants.DELIMITER_IDENTIFIER,
         title=GDPAlgorithmConstants.DELIMITER_TITLE,
         abstrakt=GDPAlgorithmConstants.DELIMITER_ABSTRACT,
         defaultValue="COMMA")
     public void setDelimiter(Delimiter delimiter) {
         this.delimiter = delimiter;
     }
     
     @LiteralDataInput(
             identifier="SUMMARIZE_TIMESTEP",
             title="Summarize Timestep",
             abstrakt="If selected, processing output will include a final row summarizing all time steps.",
             defaultValue="false")
     public void setSummarizeTimeStep(boolean summarizeTimeStep) {
         this.summarizeTimeStep = summarizeTimeStep;
     }
     
     @LiteralDataInput(
             identifier="SUMMARIZE_FEATURE_ATTRIBUTE",
             title="Summarize Feature Attribute",
             abstrakt="If selected, processing output will include a final set of statistics summarizing all polygon analysis features.",
             defaultValue="false")
     public void setSummarizeFeatureAttribute(boolean summarizeFeatureAttribute) {
         this.summarizeFeatureAttribute = summarizeFeatureAttribute;
     }
 
     @ComplexDataOutput(
             identifier="OUTPUT",
             title="Output File",
             abstrakt="A delimited text file containing requested process output.",
             binding=CSVFileBinding.class)
     public File getOutput() {
         return output;
     }
 
     @Process
     public void process() {
 
 //        FeatureDataset featureDataset = null;
         BufferedWriter writer = null;
         try {
             if (featureCollection.getSchema().getDescriptor(featureAttributeName) == null) {
                 addError("Attribute " + featureAttributeName + " not found in feature collection");
                 return;
             }
 
             output = File.createTempFile(getClass().getSimpleName(), ".csv");
             writer = new BufferedWriter(new FileWriter(output));
             
             for (String currentDatasetId : datasetId) {
                 GridDatatype gridDatatype = GDPAlgorithmUtil.generateGridDataType(
                         datasetURI,
                         currentDatasetId,
                         featureCollection.getBounds(),
                         requireFullCoverage);
 
                 Range timeRange = GDPAlgorithmUtil.generateTimeRange(
                         gridDatatype,
                         timeStart,
                         timeEnd);
 
                 writer.write("# " + currentDatasetId);
                 writer.newLine();
                 FeatureCoverageWeightedGridStatistics.execute(
                         featureCollection,
                         featureAttributeName,
                         gridDatatype,
                         timeRange,
                         statistics == null || statistics.isEmpty() ? Arrays.asList(Statistic.values()) : statistics,
                         writer,
                         groupBy == null ? GroupBy.STATISTIC : groupBy,
                         delimiter == null ? Delimiter.COMMA : delimiter,
                         requireFullCoverage,
                         summarizeTimeStep,
                         summarizeFeatureAttribute);
             }
         } catch (InvalidRangeException e) {
             addError("Error subsetting gridded data: " + e.getMessage());
         } catch (IOException e) {
             addError("IO Error :" + e.getMessage());
         } catch (FactoryException e) {
             addError("Error initializing CRS factory: " + e.getMessage());
         } catch (TransformException e) {
             addError("Error attempting CRS transform: " + e.getMessage());
         } catch (SchemaException e) {
             addError("Error subsetting gridded data : " + e.getMessage());
         } catch (Exception e) {
             addError("General Error: " + e.getMessage());
         } finally {
 //            if (featureDataset != null) try { featureDataset.close(); } catch (IOException e) { }
             IOUtils.closeQuietly(writer);
         }
     }
 	
 }
