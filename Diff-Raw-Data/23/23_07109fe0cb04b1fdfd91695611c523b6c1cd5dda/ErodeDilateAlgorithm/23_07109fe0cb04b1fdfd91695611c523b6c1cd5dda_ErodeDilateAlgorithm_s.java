 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package tikouka.nl.wps.algorithm;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.geotools.coverage.grid.GridCoverage2D;
 import org.geotools.geometry.Envelope2D;
 import org.n52.wps.io.data.IData;
 import org.n52.wps.io.data.binding.complex.GTRasterDataBinding;
 import org.n52.wps.io.data.binding.literal.LiteralDoubleBinding;
 import org.n52.wps.server.AbstractAlgorithm;
 
 import es.unex.sextante.core.OutputFactory;
 import es.unex.sextante.core.OutputObjectsSet;
 import es.unex.sextante.core.ParametersSet;
 import es.unex.sextante.core.Sextante;
 import es.unex.sextante.dataObjects.IRasterLayer;
 import es.unex.sextante.geotools.GTOutputFactory;
 import es.unex.sextante.geotools.GTRasterLayer;
 import es.unex.sextante.imageAnalysis.erosionDilation.ErosionDilationAlgorithm;
 import es.unex.sextante.outputs.Output;
 
 /**
  * 
  * @author Andrew Seales
  */
 public class ErodeDilateAlgorithm extends AbstractAlgorithm {
 
   public ErodeDilateAlgorithm() {
     super();
   }
 
   private List<String> errors = new ArrayList<String>();
 
   public List<String> getErrors() {
     return errors;
   }
 
   public Class getInputDataType( String id ) {
     if ( id.equalsIgnoreCase( "landcover" ) ) {
      return GTRasterDataBinding.class;
    } else if ( id.equalsIgnoreCase( "growFactor" ) ) {
      return LiteralDoubleBinding.class;
    }
    throw new RuntimeException( "Could not find datatype for id " + id );
   }
 
   public Class getOutputDataType( String id ) {
     return GTRasterDataBinding.class;
   }
 
   public Map<String, IData> run( Map<String, List<IData>> inputData ) {
     if ( inputData == null || !inputData.containsKey( "growFactor" ) ) {
       throw new RuntimeException( "Error while allocating input parameters 'growFactor'" );
     }
     double growFactor = ((LiteralDoubleBinding)inputData.get( "growFactor" ).get( 0 )).getPayload();
     
     if ( inputData == null || !inputData.containsKey( "landcover" ) ) {
       throw new RuntimeException( "Error while allocating input parameters 'landcover'" );
     }
     GridCoverage2D landCover = ((GTRasterDataBinding)inputData.get( "landcover" ).get( 0 )).getPayload();
 
     HashMap<String, IData> resulthash = new HashMap<String, IData>();
     
     // Converts metres to pixels
     Envelope2D env = landCover.getEnvelope2D();
     double minX = env.getMinX(); // min x extent in CRS
     double maxX = env.getMaxX(); // max x extent in CRS
     int width = (int)landCover.getGridGeometry().getGridRange2D().getWidth(); // width in pixels
     double xPixelSize = (maxX - minX) / width; // width of pixel in CRS
 
     double minY = env.getMinY();
     double maxY = env.getMaxY();
     int height = (int)landCover.getGridGeometry().getGridRange2D().getHeight();
     double yPixelSize = (maxY - minY) / height;
     
     double avgPixelSize = (xPixelSize + yPixelSize) / 2.0;
     int growFactorPixels = (int)Math.round( growFactor / avgPixelSize );
     
     // Grow test
     try {
       Sextante.initialize();
       GTRasterLayer landCoverRaster = new GTRasterLayer();
       landCoverRaster.create( landCover );
       ErosionDilationAlgorithm alg = new ErosionDilationAlgorithm();
       
       ParametersSet params = alg.getParameters();
       if ( growFactorPixels < 0 ) {
         params.getParameter( ErosionDilationAlgorithm.OPERATION )
               .setParameterValue( ErosionDilationAlgorithm.ERODE );
       }
       else if ( growFactorPixels > 0 ) {
         params.getParameter( ErosionDilationAlgorithm.OPERATION )
               .setParameterValue( ErosionDilationAlgorithm.DILATE );
       }
       else {
         resulthash.put( "result", new GTRasterDataBinding( landCover ) );
         return resulthash;
       }
       params.getParameter( ErosionDilationAlgorithm.LAYER ).setParameterValue( landCoverRaster );
       params.getParameter( ErosionDilationAlgorithm.RADIUS ).setParameterValue( growFactorPixels );
       
       OutputObjectsSet outputs = alg.getOutputObjects();
       Output output = outputs.getOutput( ErosionDilationAlgorithm.RESULT );
       OutputFactory outputFactory = new GTOutputFactory();
       alg.execute( null, outputFactory );
       IRasterLayer result = (IRasterLayer)output.getOutputObject();
       GridCoverage2D resultCoverage = (GridCoverage2D)result.getBaseDataObject();
 
       resulthash.put( "result", new GTRasterDataBinding( resultCoverage ) );
     } catch ( Exception e ) {
       e.printStackTrace();
     }
     return resulthash;
   }
 }
