 package gov.usgs.cida.gdp.wps.algorithm;
 
 import gov.usgs.cida.gdp.coreprocessing.Delimiter;
 import gov.usgs.cida.gdp.coreprocessing.analysis.grid.FeatureCategoricalGridCoverage;
 import gov.usgs.cida.gdp.wps.algorithm.annotation.Algorithm;
 import gov.usgs.cida.gdp.wps.algorithm.annotation.ComplexDataInput;
 import gov.usgs.cida.gdp.wps.algorithm.annotation.ComplexDataOutput;
 import gov.usgs.cida.gdp.wps.algorithm.annotation.LiteralDataInput;
 import gov.usgs.cida.gdp.wps.algorithm.annotation.Process;
 import gov.usgs.cida.gdp.wps.binding.CSVFileBinding;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.net.URI;
 import java.util.List;
 import org.apache.commons.io.IOUtils;
 import org.geotools.feature.FeatureCollection;
 import org.geotools.feature.SchemaException;
 import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
 import org.opengis.referencing.FactoryException;
 import org.opengis.referencing.operation.TransformException;
 import ucar.ma2.InvalidRangeException;
 import ucar.nc2.dt.GridDatatype;
 import ucar.nc2.ft.FeatureDataset;
 
 /**
  *
  * @author tkunicki
  */
@Algorithm(title="Feature Categorical Grid Coverage", version="1.0.0")
 public class FeatureCategoricalGridCoverageAlgorithm extends AbstractAnnotatedAlgorithm {
 
     private FeatureCollection featureCollection;
     private String featureAttributeName;
     private URI datasetURI;
     private List<String> datasetId;
     private Delimiter delimiter;
 
     private File output;
 
     @ComplexDataInput(identifier=GDPAlgorithmUtil.INPUT_FEATURE_COLLECTION, binding=GTVectorDataBinding.class)
     public void setFeatureCollection(FeatureCollection featureCollection) {
         this.featureCollection = featureCollection;
     }
 
     @LiteralDataInput(identifier=GDPAlgorithmUtil.INPUT_FEATURE_ATTRIBUTE_NAME)
     public void setFeatureAttributeName(String featureAttributeName) {
         this.featureAttributeName = featureAttributeName;
     }
 
     @LiteralDataInput(identifier=GDPAlgorithmUtil.INPUT_DATASET_URI)
     public void setDatasetURI(URI datasetURI) {
         this.datasetURI = datasetURI;
     }
 
     @LiteralDataInput(identifier=GDPAlgorithmUtil.INPUT_DATASET_ID, maxOccurs= Integer.MAX_VALUE)
     public void setDatasetId(List<String> datasetId) {
         this.datasetId = datasetId;
     }
 
     @LiteralDataInput(identifier="DELIMITER", defaultValue="COMMA")
     public void setDelimiter(Delimiter delimiter) {
         this.delimiter = delimiter;
     }
 
     @ComplexDataOutput(identifier="OUTPUT", binding=CSVFileBinding.class)
     public File getOutput() {
         return output;
     }
 
     @Process
     public void process() {
 
         FeatureDataset featureDataset = null;
         BufferedWriter writer = null;
 
         try {
 
             output = File.createTempFile("gdp", "csv");
             writer = new BufferedWriter(new FileWriter(output));
 
             for (String currentDatasetId : datasetId) {
                 GridDatatype gridDatatype = GDPAlgorithmUtil.generateGridDataType(
                         datasetURI,
                         currentDatasetId,
                         featureCollection.getBounds());
                 
                 writer.write("# " + currentDatasetId);
                 writer.newLine();
                 FeatureCategoricalGridCoverage.execute(
                         featureCollection,
                         featureAttributeName,
                         gridDatatype,
                         writer,
                         delimiter == null ? Delimiter.getDefault() : delimiter);
             }
 
         } catch (InvalidRangeException e) {
             throw new RuntimeException(e);
         } catch (IOException e) {
             throw new RuntimeException(e);
         } catch (FactoryException e) {
             throw new RuntimeException(e);
         } catch (TransformException e) {
             throw new RuntimeException(e);
         } catch (SchemaException e) {
             throw new RuntimeException(e);
         } finally {
             if (featureDataset != null) try { featureDataset.close(); } catch (IOException e) { }
             IOUtils.closeQuietly(writer);
         }
     }
 
 }
