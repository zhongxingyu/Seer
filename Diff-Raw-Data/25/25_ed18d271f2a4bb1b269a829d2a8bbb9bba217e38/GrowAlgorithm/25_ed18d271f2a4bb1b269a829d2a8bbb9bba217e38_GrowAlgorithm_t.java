 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package tikouka.nl.wps.algorithm;
 
 import com.vividsolutions.jts.geom.Geometry;
 import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
 import java.awt.image.BufferedImage;
 import java.awt.image.WritableRaster;
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.UUID;
 import javax.xml.namespace.QName;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 import org.geotools.coverage.grid.GridCoverage2D;
 import org.geotools.coverage.grid.GridCoverageFactory;
 import org.geotools.coverage.processing.AbstractProcessor;
 import org.geotools.coverage.processing.DefaultProcessor;
 import org.geotools.feature.DefaultFeatureCollections;
 import org.geotools.feature.FeatureCollection;
 import org.geotools.geometry.Envelope2D;
 import org.geotools.geometry.jts.ReferencedEnvelope;
 import org.geotools.process.ProcessException;
 import org.geotools.process.raster.RasterToVectorProcess;
 import org.geotools.process.raster.VectorToRasterException;
 import org.geotools.process.raster.VectorToRasterProcess;
 import org.n52.wps.io.SchemaRepository;
 import org.n52.wps.io.data.IData;
 import org.n52.wps.io.data.binding.complex.GTRasterDataBinding;
 import org.n52.wps.io.data.binding.literal.LiteralDoubleBinding;
 import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
 import org.n52.wps.io.datahandler.xml.GTHelper;
 import org.n52.wps.server.AbstractObservableAlgorithm;
 import org.opengis.feature.simple.SimpleFeature;
 import org.opengis.feature.simple.SimpleFeatureType;
 import org.opengis.referencing.crs.CoordinateReferenceSystem;
 import org.xml.sax.SAXException;
 import tikouka.nl.wps.algorithm.util.Table;
 import tikouka.nl.wps.handler.XMLHandler;
 
 /**
  *
  * @author niels
  */
 public class GrowAlgorithm extends AbstractObservableAlgorithm
 {
     private List<String> errors = new ArrayList<String>();
     final AbstractProcessor processor = new DefaultProcessor(null);
 
     public GrowAlgorithm()
     {
         super();
     }
 
     public Class getOutputDataType(String id) {
         return GTRasterDataBinding.class;
     }
 /*
      * @see org.n52.wps.server.IAlgorithm#getErrors()
      */
     public List<String> getErrors()
     {
         return errors;
     }
 
     public Class getInputDataType(String id) {
         if(id.equalsIgnoreCase("nz_woody")){
 				return GTRasterDataBinding.class;
         }
        else if (id.equalsIgnoreCase("nz_woody_lookup")){
            return LiteralStringBinding.class;
        }
         else if (id.equalsIgnoreCase("g")){
             return LiteralDoubleBinding.class;
         }
 	throw new RuntimeException("Could not find datatype for id " + id);
     }
 
     /*
      * @see org.n52.wps.server.IAlgorithm#run(java.util.Map)
      */
     public Map<String, IData> run(Map<String, List<IData>> inputData) {
        // ############################################################
         // READ THE INPUT DATA
         // ############################################################
         if(inputData==null || !inputData.containsKey("nz_woody")){
 			throw new RuntimeException("Error while allocating input parameters 'nz_woody'");
 		}
         GridCoverage2D nz_woody = ((GTRasterDataBinding) inputData.get("nz_woody").get(0)).getPayload();
 
         if(inputData==null || !inputData.containsKey("nz_woody_lookup")){
 			throw new RuntimeException("Error while allocating input parameters");
 		}
         List<IData> nz_woody_lookup = inputData.get("nz_woody_lookup");
 
         if(inputData==null || !inputData.containsKey("g")){
 			throw new RuntimeException("Error while allocating input parameters");
 		}
         double grow_factor[] = new double[1];
         grow_factor[0] = ((LiteralDoubleBinding) inputData.get("g").get(0)).getPayload();
 
         // ############################################################
         //  PARSE THE LOOKUPTABLE
         // ############################################################
         List<Table> woodylutList = getLookupTableData(nz_woody_lookup);
 
         // ############################################################
         //  RUN THE MODEL
         // ############################################################
 
         Envelope2D res_env = new Envelope2D(nz_woody.getEnvelope2D());
 
         int minX = (int)res_env.getMinX();
         int maxX = (int)res_env.getMaxX();
         int width = (int)res_env.getWidth()/100;
         int x_x =  (int)((maxX - minX)/(width));
 
         int minY = (int)res_env.getMinY();
         int maxY = (int)res_env.getMaxY();
         int height = (int)res_env.getHeight()/100;
         int y_y =  (int)((maxY - minY)/(height));
 
         BufferedImage image = new BufferedImage((int)width,(int)height, BufferedImage.TYPE_BYTE_GRAY);
         WritableRaster raster = image.getRaster();
 
         /* Algorithm:
          * a is the geology & slope information
          * C is the land use, either woody/forest or other bare ground/grass
          * D is the connectivity to water/streams(a constant of 1 in the case of New Zealand)
          * R is the mean annual rainfall squared
          * SE is the resulting soil erosion
          *
          * a * grow(C,woody/forest, g) * D * r^2 * R = SE, with g and r provided by the student.
          *
          *  The grow function can be thought of as similar to buffer, but it acts on a single category
          * (ie woody/forest) within a raster and ?grows? the extent of that category by a fixed width that
          * I have specified as g in the formula, which would be provided by the student.
          * g values can be +ve or -ve, which would indicate a contraction in forest area.
          *
          * I think SE is in t/km^2/yr/mm^2
          */
 
         ArrayList<Double> outsideValues = new ArrayList<Double>();
         outsideValues.add(Double.parseDouble("0"));
         outsideValues.add(Double.parseDouble("10"));
 
        //FeatureCollection fc = null;
        FeatureCollection<SimpleFeatureType, SimpleFeature> fc = null;
        Dimension dim = new Dimension(x_x, y_y);
        ReferencedEnvelope refEnv = new ReferencedEnvelope(res_env);

         try{
            fc = RasterToVectorProcess.process(nz_woody,0, res_env, outsideValues , null);
         }catch(ProcessException pe){
             pe.printStackTrace();
         }
 
         FeatureCollection buffer = runBuffer(fc, grow_factor[0]);
 
        
         GridCoverageFactory coverageFactory = new GridCoverageFactory();
         GridCoverage2D coverage = coverageFactory.create("output", raster,res_env);
 
         try{
             coverage = VectorToRasterProcess.process(buffer, "1", dim, refEnv,"nz_woody_grow",null);
         }catch(VectorToRasterException vre){
             vre.printStackTrace();
         }
 
         // ############################################################
         //  WRITE THE OUTPUT DATA
         // ############################################################
         HashMap<String,IData> resulthash = new HashMap<String,IData>();
         try{
             //GridCoverage2D coverage = coverageFactory.create("output", raster,res_env);
             resulthash.put("result", new GTRasterDataBinding(coverage));
          }catch(Exception e){
             e.printStackTrace();
         }
         return resulthash;
     }
 
     private List<Table> getLookupTableData(List<IData> lookup)
 	{
                 XMLHandler handler = new XMLHandler();
 		try
 		{
                     SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
 
                     parser.setProperty("http://xml.org/sax/properties/lexical-handler", handler);
                     parser.parse(new ByteArrayInputStream(((LiteralStringBinding) lookup.get(0)).getPayload().getBytes()), handler);
 
                 }catch(SAXException se){
                     se.printStackTrace();
                     throw new RuntimeException(se);
                 }catch(ParserConfigurationException pe){
                     pe.printStackTrace();
                     throw new RuntimeException(pe);
                 }catch(IOException ioe){
                     ioe.printStackTrace();
                     throw new RuntimeException(ioe);
                 }
 
                 return handler.getRasterRable().getTable();
 
 	}
 
     private FeatureCollection runBuffer(FeatureCollection fcA, double width)	{
 		  //Collection resultColl = new ArrayList();
 		  double i = 0;
 		  int totalNumberOfFeatures = fcA.size();
 		  String uuid = UUID.randomUUID().toString();
 		  FeatureCollection featureCollection = DefaultFeatureCollections.newCollection();
 		  SimpleFeatureType featureType = null;
 		  for (Iterator ia = fcA.iterator(); ia.hasNext(); ) {
 			/********* How to publish percentage results *************/
 			i= i+1;
 			//percentage = (i/totalNumberOfFeatures)*100;
 			//this.update(new Integer(percentage.intValue()));
 
 			/*********************/
 			SimpleFeature fa = (SimpleFeature) ia.next();
 			Geometry geometry = (Geometry) fa.getDefaultGeometry();
 			Geometry result = runBuffer(geometry, width);;
 
 			if(i==1){
 				CoordinateReferenceSystem crs = fa.getFeatureType().getCoordinateReferenceSystem();
 				if(geometry.getUserData() instanceof CoordinateReferenceSystem){
 					crs = ((CoordinateReferenceSystem) geometry.getUserData());
 				}
 				 featureType = GTHelper.createFeatureType(fa.getProperties(), result, uuid, crs);
 				 QName qname = GTHelper.createGML3SchemaForFeatureType(featureType);
 				 SchemaRepository.registerSchemaLocation(qname.getNamespaceURI(), qname.getLocalPart());
 
 			}
 
 			if (result != null) {
 				SimpleFeature feature = (SimpleFeature) GTHelper.createFeature("ID"+new Double(i).intValue(),result,(SimpleFeatureType) featureType,fa.getProperties());
 				fa.setDefaultGeometry(result);
 				featureCollection.add(feature);
 			}
 
 
 			else {
 				//LOGGER.warn("GeometryCollections are not supported, or result null. Original dataset will be returned");
 			}
 		  }
 
 		  return featureCollection;
 		}
 
 
 		private Geometry runBuffer(Geometry a, double width) {
 		  Geometry result = null;
 
 		  try {
 
 			result = a.buffer(width);
 			return result;
 		  }
 		  catch (RuntimeException ex) {
 			// simply eat exceptions and report them by returning null
 		  }
 		  return null;
 		}
 }
