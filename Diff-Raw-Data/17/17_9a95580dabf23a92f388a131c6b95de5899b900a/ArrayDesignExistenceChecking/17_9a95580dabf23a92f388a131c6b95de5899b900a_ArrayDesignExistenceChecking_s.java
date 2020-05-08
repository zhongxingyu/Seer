 package uk.ac.ebi.fgpt.conan.process.atlas;
 
 import uk.ac.ebi.fgpt.conan.rest.AbstractRESTAPISubprocess;
 import java.util.*;
 
 /**
  * Process to check array design existence in Atlas
  *
  * @author Natalja Kurbatova
  * @date 15/02/11
  */
 
 public class ArrayDesignExistenceChecking extends AbstractRESTAPISubprocess {
 
   private CommonAtlasProcesses atlas = new CommonAtlasProcesses();
 
   /**
    * Parses the response from Atlas REST API for the monitored process Returns
    * true if process has been finished.
    *
    * @param response response received from Atlas REST API in HashMap format
    * @return true when process is finished, false otherwise
    */
   @Override protected boolean isComplete(HashMap<String, Object> response) {
     return atlas.isComplete(response);
   }
 
   /**
    * Parses the response from Atlas REST API, extracts message and returns it.
    *
    * @param response response received from Atlas REST API in HashMap format
    * @return message text extracted from the REST API response
    */
   @Override protected String getMessage(HashMap<String, Object> response) {
     return atlas.getMessage(response);
   }
 
 
   /**
    * Parses the response from Atlas REST API, by using parameters extracts Atlas
    * job ID.
    *
    * @param response response received from Atlas REST API in HashMap format
    * @return Atlas job ID to monitor the job status
    */
   @Override
   protected String getResultValue(HashMap<String, Object> response,
                                   String parameters) {
     String result = "empty";
     try {
 
       if (Integer.parseInt(response.get("numTotal").toString()) > 0) {
         result = "is";
        System.out.println(response.get("arraydesigns").toString().toLowerCase());
         if (response.get("arraydesigns").toString().toLowerCase()
             .contains("affy")) {
           result = "affy";
         }
       }
       else {
         result = "no";
       }
       return result;
     }
     catch (Exception e) {
       e.printStackTrace();
       return result;
     }
   }
 
   /**
    * Creates Atlas Rest API request to update experiment
    *
    * @param parameters path to the mage-tab file
    * @return restApiRequest string
    * @throws IllegalArgumentException
    */
   @Override protected String getRestApiRequest(String parameters) {
 
     String restApiRequest = atlas.ArrayDesignSearch + parameters;
     return restApiRequest;
 
   }
 
   /**
    * Returns Atlas REST API request to log in
    *
    * @return Atlas REST API request
    */
   @Override protected String getLoginRequest() {
     return atlas.LogIn;
   }
 
 
 }
