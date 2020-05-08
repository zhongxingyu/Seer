 package com.xpandit.fusionplugin;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.util.Properties;
 import java.util.TreeMap;
 
 import org.apache.log4j.Logger;
 import org.pentaho.platform.api.engine.ISolutionFile;
 import org.pentaho.platform.api.repository.ISolutionRepository;
 import org.pentaho.platform.engine.core.solution.ActionInfo;
 import org.pentaho.platform.engine.core.system.PentahoSystem;
 
 import com.xpandit.fusionplugin.exception.InvalidParameterException;
 
 /**
  * 
  * Class thats manages all chart properties. 
  * 
  * 3 types of properties are supported: 
  * 
  * Global - From FusionCharts.properties file in plugin folder 
  * Local - From any file in any solution folder, the file path and solution are set by Url Params(propFile,propPath and propSolution) 
  * Instance - From url params.
  * 
  * The priority of the parameters is Instance->Local->Global. Meaning that on each step they can be overridden.
  * 
  * @author dduque
  * 
  */
 public class PropertiesManager {
     
     //Well known parameter types.
     private static final String NAME = "name";
     private static final String SOLUTION = "solution";
     private static final String PATH = "path";
     private static final String XFUSIONPATH = "xFusionPath";
     
     private Logger log = Logger.getLogger(PropertiesManager.class);// class Logger
     
     // Manager for properties obtained from the .xfusion file
     private TreeMap<String, String> localProperties = null;
     
     // Properties obtained from the instance.
     private TreeMap<String, String> instanceProperties = null;
 
     // name of properties file
     private String propFile = "";
     
     // path of properties file
     private String propPath;
     
     // Solution name of properties file
     private String propSolution;
     
     // Single property for non-legacy mode
     private String xFusionFile = "";
 
     /**
      * 
      * Initialize the properties manager
      * 
      * @param propFile File Name of local file properties
      * @param propPath Path of local file properties
      * @param propSolution Solution Name of local file properties
      * @param parameters Url Parameters
      * @param encoding Encoding of URL params
      * @throws InvalidParameterException
      */
     public PropertiesManager(TreeMap<String, String> instanceProperties, String pathMode) throws InvalidParameterException {
         this.propFile = instanceProperties.get(NAME);
         this.propPath = instanceProperties.get(PATH);
         this.propSolution = instanceProperties.get(SOLUTION);
         this.xFusionFile = instanceProperties.get(XFUSIONPATH);
         this.instanceProperties = instanceProperties;
         this.localProperties = new TreeMap<String, String>();
         
         if(pathMode.equals("legacy")) {
             fillLocalParametersWithActionInfo();
         } else {
             fillLocalParameters();
         }
     }
 
     /**
      * fill all instance parameters based on solution file with an ActionInfo object
      * 
      * @throws InvalidParameterException
      */
     private void fillLocalParametersWithActionInfo() throws InvalidParameterException {
        // get file 
         final ISolutionRepository repository = PentahoSystem.get(ISolutionRepository.class, null);
         final ISolutionFile file = repository.getSolutionFile(
                 new ActionInfo(propSolution, propPath, propFile).toString(), ISolutionRepository.ACTION_EXECUTE);
 
        if(propSolution==null)
        	propSolution="<null>";
        if(propPath==null)
        	propPath="<null>";
        if(propFile==null)
        	propFile="<null>";

         // if is no file and propFile is set log a warning
        if (file == null) {
        	log.warn(InvalidParameterException.ERROR_005 + ":"
        					+ "No solution file found to set properties:" + "propSolution->" + propSolution
        					+ ";propPath-->" + propPath + ";propFile-->" + propFile);
        	return;
         }
 
         // load properties
         Properties properties = new Properties();
         try {
             properties.load(new ByteArrayInputStream(file.getData()));
         } catch (IOException e) {
            log.warn(InvalidParameterException.ERROR_005 + ": Unable to Load properties file. Continue without properties file:" + "propSolution->"
                     + propSolution + ";propPath-->" + propPath + ";propFile-->" + propFile, e);
             return;
         }
 
         // fill properties
         for (Object key : properties.keySet()) {
             String stringKey = (String) key;
             localProperties.put(stringKey.trim(), properties.getProperty(stringKey).trim());
         }
 
     }
 
     /**
      * fill all instance parameters based on solution file
      * 
      * @throws InvalidParameterException
      */
     private void fillLocalParameters() throws InvalidParameterException {
         if(this.xFusionFile == null) {
             return;
         }
 
         // get file
         final ISolutionRepository repository = PentahoSystem.get(ISolutionRepository.class, null);
         final ISolutionFile file = repository.getSolutionFile(xFusionFile, ISolutionRepository.ACTION_EXECUTE);
 
         // if is no file and propFile is set log a warning
         if (file != null) {
             if (file.getData() == null) {
                 if (!xFusionFile.equals(""))
                     throw new InvalidParameterException(InvalidParameterException.ERROR_005 + ":"
                             + "No solution file found to set properties:" + "xFusionFile->" + xFusionFile);
                 return;
             }
         } else {
             if (!xFusionFile.equals(""))
                 throw new InvalidParameterException(InvalidParameterException.ERROR_005 + ":"
                         + "No solution file found to set properties:" + "xFusionFile->" + xFusionFile);
         }
 
         // load properties
         Properties properties = new Properties();
         try {
             properties.load(new ByteArrayInputStream(file.getData()));
         } catch (IOException e) {
             log.error("Unable to Load properties file. Continue without properties file:" + "xFusionFile->"
                     + xFusionFile, e);
             return;
         }
 
         // fill properties
         for (Object key : properties.keySet()) {
             String stringKey = (String) key;
             localProperties.put(stringKey.trim(), properties.getProperty(stringKey).trim());
         }
     }
     
     /**
      * Join all parameter types and returns the object
      * 
      * @return Map with all parameters joined by order
      */
     public TreeMap<String, String> getParams() {
         TreeMap<String, String> params = new TreeMap<String, String>();
 
         // put all properties by order all properties are replaced
         params.putAll(GlobalPropertiesManager.getInstance().getProperties());
         params.putAll(localProperties);
         params.putAll(instanceProperties);
 
         return params;
     }
 
     /**
      * 
      * returns the Solution of the xFusion properties file
      * 
      * @return
      */
     public String getPropSolution() {
         return this.propSolution;
     }
 
     /**
      * 
      * returns the path of the xFusion properties file
      * 
      * @return
      */
     public String getPropPath() {
         return this.propPath;
     }
 }
