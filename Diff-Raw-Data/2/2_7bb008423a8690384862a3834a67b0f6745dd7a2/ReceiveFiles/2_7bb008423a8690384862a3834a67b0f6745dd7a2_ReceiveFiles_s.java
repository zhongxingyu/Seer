 package gov.usgs.cida.gdp.wps.algorithm.filemanagement;
 
 import gov.usgs.cida.gdp.constants.AppConstant;
 import gov.usgs.cida.gdp.dataaccess.GeoserverManager;
 import gov.usgs.cida.gdp.dataaccess.helper.ShapeFileEPSGHelper;
 import gov.usgs.cida.gdp.io.data.ZippedGenericFileDataBinding;
 import gov.usgs.cida.gdp.utilities.FileHelper;
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.UUID;
 import org.apache.commons.io.filefilter.RegexFileFilter;
 import org.apache.commons.lang.StringUtils;
 import org.n52.wps.io.data.GenericFileData;
 import org.n52.wps.io.data.IData;
 import org.n52.wps.io.data.binding.complex.GenericFileDataBinding;
 import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
 import org.n52.wps.server.AbstractSelfDescribingAlgorithm;
 import org.opengis.referencing.FactoryException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * This algorithm allows a client to upload a file to the server via WPS.
  * More info: http://privusgs2.er.usgs.gov/display/GDP/Adding+a+Shapefile+as+a+GeoServer+WFS+EndPoint
  *
  * @author isuftin
  */
 public class ReceiveFiles extends AbstractSelfDescribingAlgorithm {
     private Logger log = LoggerFactory.getLogger(ReceiveFiles.class);
     private List<String> errors = new ArrayList<String>();
     private static final String SUFFIX_SHP = ".shp";
     private static final String SUFFIX_SHX = ".shx";
     private static final String SUFFIX_PRJ = ".prj";
     private static final String SUFFIX_DBF = ".dbf";
     private static final String UPLOAD_WORKSPACE = "upload";
     private static final String PARAM_FILE = "file";
     private static final String PARAM_WFS_URL = "wfs-url";
     private static final String PARAM_FILENAME = "filename";
     private static final String PARAM_RESULT = "result";
     private static final String PARAM_FEATURETYPE = "featuretype";
         
 
     @Override
     public Map<String, IData> run(Map<String, List<IData>> inputData) {
         
         if (inputData == null)  {
             throw new RuntimeException("Error while allocating input parameters.");
         }
         if (!inputData.containsKey(PARAM_FILE))  {
             throw new RuntimeException("Error: Missing input parameter 'file'");
         }
         if (!inputData.containsKey(PARAM_WFS_URL))  {
             throw new RuntimeException("Error: Missing input parameter 'wfs-url'");
         }
         if (!inputData.containsKey(PARAM_FILENAME)) {
             throw new RuntimeException("Error: Missing input parameter 'filename'");
         }
 
         // "gdp.shapefile.temp.path" should be set in the tomcat startup script or setenv.sh as JAVA_OPTS="-Dgdp.shapefile.temp.path=/wherever/you/want/this/file/placed"
         String fileDump = AppConstant.SHAPEFILE_LOCATION.getValue() + File.separator + UUID.randomUUID();
 
         // Ensure that the temp directory exists
         File temp = new File(fileDump);
         temp.mkdirs();
 
         List<IData> dataList = inputData.get(PARAM_FILENAME);
         String desiredFilename = (((LiteralStringBinding) dataList.get(0)).getPayload()).replace(" ", "_");
 
         List<IData> wfsEndpointList = inputData.get(PARAM_WFS_URL);
         String wfsEndpoint = ((LiteralStringBinding) wfsEndpointList.get(0)).getPayload();
 
         dataList = inputData.get(PARAM_FILE);
         IData data = dataList.get(0);
 
         // Process each input one at a time
         GenericFileData file = ((ZippedGenericFileDataBinding) data).getPayload();
         if (file == null) {
             errors.add("Error while processing file: Could not get file from server");
             throw new RuntimeException("Error while processing file: Could not get file from server");
         }
 
         String shapefilePath = file.writeData(temp);
         if (shapefilePath == null) { // Not sure if that is the only reason newFilename would be null
             errors.add("Error while processing file: Malformed zip file or incomplete shapefile");
             throw new RuntimeException("Error while processing file: Malformed zip file or incomplete shapefile");
         }
 
         
 
         File shapefileFile = new File(shapefilePath);
         File shapefileDir = shapefileFile.getParentFile();
         
         String shapefileName = shapefileFile.getName();
         String shapefileNamePrefix = shapefileName.substring(0, shapefileName.lastIndexOf("."));
 
         // Find all files with filename with any extension
         String pattern = shapefileNamePrefix + "\\..*";
         FileFilter filter = new RegexFileFilter(pattern);
 
         String[] filenames = shapefileDir.list((FilenameFilter) filter);
         List<String> filenamesList = Arrays.asList(filenames);
 
         // Make sure required files are present
         String[] requiredFiles = { SUFFIX_SHP, SUFFIX_SHX, SUFFIX_PRJ, SUFFIX_DBF };
         for (String requiredFile : requiredFiles) {
             if (!filenamesList.contains(shapefileNamePrefix + requiredFile)) {
                 throw new RuntimeException("Zip file missing " + requiredFile + " file.");
             }
         }
 
         // Rename the files to the desired filenames
         File[] files = shapefileDir.listFiles(filter);
         for (File f : files) {
             String name = f.getName();
             String extension = name.substring(name.lastIndexOf("."));
 
             f.renameTo(new File(shapefileDir.getPath() + File.separator + desiredFilename + extension));
         }
 
         String renamedShpPath = shapefileDir.getPath() + File.separator + desiredFilename + SUFFIX_SHP;
         String renamedPrjPath = shapefileDir.getPath() + File.separator + desiredFilename + SUFFIX_PRJ;
 
         // Do EPSG processing
         String declaredCRS = null;
         String nativeCRS = null;
         String warning = "";
         try {
             nativeCRS = new String(FileHelper.getByteArrayFromFile(new File(renamedPrjPath)));
             if (nativeCRS == null || nativeCRS.isEmpty()) {
                 throw new RuntimeException("Error while getting Prj/WKT information from PRJ file. Function halted.");
             }
             // The behavior of this method requires that the layer always force
             // projection from native to declared...
             declaredCRS = ShapeFileEPSGHelper.getDeclaredEPSGFromWKT(nativeCRS, false);
             if (declaredCRS == null || declaredCRS.isEmpty()) {
                 declaredCRS = ShapeFileEPSGHelper.getDeclaredEPSGFromWKT(nativeCRS, true);
                 warning = "Could not find EPSG code for prj definition. The geographic coordinate system '"+declaredCRS+"' will be used";
             }
             if (declaredCRS == null || declaredCRS.isEmpty()) {
                 throw new RuntimeException("Could not attain EPSG code from shapefile. Please ensure proper projection and a valid PRJ file.");
             }
         } catch (IOException ex) {
             errors.add(ex.getMessage());
             throw new RuntimeException("Error while getting EPSG information from PRJ file. Function halted.",ex);
         } catch (FactoryException ex) {
             errors.add(ex.getMessage());
             throw new RuntimeException("Error while getting EPSG information from PRJ file. Function halted.",ex);
         }
 
         
         String workspace = UPLOAD_WORKSPACE;
         try {
             GeoserverManager mws = new GeoserverManager(wfsEndpoint,
                     AppConstant.WFS_USER.getValue(), AppConstant.WFS_PASS.getValue());
             
             mws.createDataStore(renamedShpPath, desiredFilename, workspace, nativeCRS, declaredCRS);
         } catch (IOException ex) {
             errors.add(ex.getMessage());
             throw new RuntimeException("Error while communicating with WFS server. Please try again or contact system administrator.");
         }
         
         Map<String, IData> result = new HashMap<String, IData>(3);
         // GeoServer has accepted the shapefile. Send the success response to the client.
         if (StringUtils.isBlank(warning)) {
             result.put(PARAM_RESULT, new LiteralStringBinding("OK: " + desiredFilename + " successfully uploaded to workspace '" + workspace + "'!"));
         } else {
             result.put(PARAM_RESULT, new LiteralStringBinding("WARNING: " + warning));
         }
         result.put(PARAM_WFS_URL, new LiteralStringBinding(wfsEndpoint + "?Service=WFS&Version=1.0.0&"));
         result.put(PARAM_FEATURETYPE, new LiteralStringBinding(workspace + ":" + desiredFilename));
         return result;
     }
 
     @Override
     public BigInteger getMaxOccurs(String identifier) {
         if (PARAM_WFS_URL.equals(identifier)) {
             return BigInteger.valueOf(1);
         }
         if (PARAM_FILENAME.equals(identifier)) {
             return BigInteger.valueOf(1);
         }
         if (PARAM_FILE.equals(identifier)) {
             return BigInteger.valueOf(1);
         }
         return super.getMaxOccurs(identifier);
     }
 
     @Override
     public BigInteger getMinOccurs(String identifier) {
         if (PARAM_WFS_URL.equals(identifier)) {
             return BigInteger.valueOf(1);
         }
         if (PARAM_FILENAME.equals(identifier)) {
             return BigInteger.valueOf(1);
         }
         if (PARAM_FILE.equals(identifier)) {
             return BigInteger.valueOf(1);
         }
 
         return super.getMinOccurs(identifier);
     }
 
     @Override
     public List<String> getInputIdentifiers() {
         List<String> result = new ArrayList<String>(3);
         result.add(PARAM_WFS_URL);
         result.add(PARAM_FILENAME);
         result.add(PARAM_FILE);
         return result;
     }
 
     @Override
     public List<String> getOutputIdentifiers() {
         List<String> result = new ArrayList<String>(3);
         result.add(PARAM_RESULT);
         result.add(PARAM_WFS_URL);
         result.add(PARAM_FEATURETYPE);
         return result;
     }
 
     @Override
     public Class getInputDataType(String id) {
         if (PARAM_WFS_URL.equals(id)) {
             return LiteralStringBinding.class;
         }
         if (PARAM_FILENAME.equals(id)) {
             return LiteralStringBinding.class;
         }
         if (PARAM_FILE.equals(id)) {
             return GenericFileDataBinding.class;
         }
         return null;
     }
 
     @Override
     public Class getOutputDataType(String id) {
         if (id.equals(PARAM_RESULT)) {
             return LiteralStringBinding.class;
         }
         if (id.equals(PARAM_WFS_URL)) {
             return LiteralStringBinding.class;
         }
         if (id.equals(PARAM_FEATURETYPE)) {
             return LiteralStringBinding.class;
         }
         return null;
     }
 
     @Override
     public List<String> getErrors() {
         return errors;
     }
 
     /**
      * @return the log
      */
     public Logger getLog() {
         return log;
     }
 
     /**
      * @param log the log to set
      */
     public void setLog(Logger log) {
         this.log = log;
     }
 }
