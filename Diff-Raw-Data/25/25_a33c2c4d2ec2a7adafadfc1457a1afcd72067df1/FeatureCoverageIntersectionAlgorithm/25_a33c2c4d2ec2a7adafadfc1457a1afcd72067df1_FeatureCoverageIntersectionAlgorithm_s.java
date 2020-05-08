 package gov.usgs.cida.gdp.wps.algorithm;
 
 import gov.usgs.cida.gdp.wps.algorithm.annotation.Algorithm;
 import gov.usgs.cida.gdp.wps.algorithm.annotation.ComplexDataInput;
 import gov.usgs.cida.gdp.wps.algorithm.annotation.ComplexDataOutput;
 import gov.usgs.cida.gdp.wps.algorithm.annotation.LiteralDataInput;
 import gov.usgs.cida.gdp.wps.algorithm.annotation.Process;
 import gov.usgs.cida.gdp.wps.binding.GeoTIFFFileBinding;
 import gov.usgs.cida.gdp.wps.util.WCSUtil;
 import java.io.File;
 import java.net.URI;
 import org.geotools.feature.FeatureCollection;
 import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
 
 /**
  *
  * @author tkunicki
  */
@Algorithm(title = "Feature Coverage Intersection", version = "1.0.0")
 public class FeatureCoverageIntersectionAlgorithm extends AbstractAnnotatedAlgorithm {
 
     private FeatureCollection featureCollection;
     private URI datasetURI;
     private String datasetId;
 
     private File output;
 
     @ComplexDataInput(identifier=GDPAlgorithmUtil.INPUT_FEATURE_COLLECTION, binding=GTVectorDataBinding.class)
     public void setFeatureCollection(FeatureCollection featureCollection) {
         this.featureCollection = featureCollection;
     }
 
     @LiteralDataInput(identifier=GDPAlgorithmUtil.INPUT_DATASET_URI)
     public void setDatasetURI(URI datasetURI) {
         this.datasetURI = datasetURI;
     }
 
     @LiteralDataInput(identifier=GDPAlgorithmUtil.INPUT_DATASET_ID)
     public void setDatasetId(String datasetId) {
         this.datasetId = datasetId;
     }
 
     @ComplexDataOutput(identifier="OUTPUT", binding=GeoTIFFFileBinding.class)
     public File getOutput() {
         return output;
     }
 
     @Process
     public void process() {
         output = WCSUtil.generateTIFFFile(datasetURI, datasetId, featureCollection.getBounds());
     }
 
 
 }
