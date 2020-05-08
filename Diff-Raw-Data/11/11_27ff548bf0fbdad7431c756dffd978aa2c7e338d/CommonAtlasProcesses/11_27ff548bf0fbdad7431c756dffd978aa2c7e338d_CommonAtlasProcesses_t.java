 package uk.ac.ebi.fgpt.conan.process.atlas;
 
 import uk.ac.ebi.fgpt.conan.properties.ConanProperties;
 import uk.ac.ebi.fgpt.conan.rest.AbstractRESTAPIProcess;
 import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
 
 import java.util.HashMap;
 
 /**
  * Common methods to work with Atlas REST API
  *
  * @author Natalja Kurbatova
  * @date 05/05/11
  */
 public class CommonAtlasProcesses {
 
   public enum AtlasEvents {
     SCHEDULED, STARTED, FAILED;
   }
 
   private static final String atlasPath = ConanProperties.getProperty("atlas.path");
   private static final String atlasUsername = ConanProperties.getProperty("atlas.username");
   private static final String atlasPassword = ConanProperties.getProperty("atlas.password");
 
   public static final String ExperimentUpdatePrivate = atlasPath +
       "admin?op=schedule&runMode=RESTART&type=" +
       "makeexperimentprivate&autoDepends=false&accession=";
 
   public static final String ExperimentUpdatePublic = atlasPath +
       "admin?op=schedule&runMode=RESTART&type=" +
       "makeexperimentpublic&autoDepends=false&accession=";
 
   public static final String ExperimentLoad = atlasPath +
       "admin?op=schedule&runMode=RESTART&type=" +
       "loadexperiment&autoDepends=false&private=true&useRawData=true&curated=true&accession=";
 
   public static final String ExperimentUnload = atlasPath +
       "admin?op=schedule&runMode=RESTART&type=" +
       "unloadexperiment&autoDepends=false&accession=";
 
   public static final String ExperimentUpdateNETCDF = atlasPath +
       "admin?op=schedule&runMode=RESTART&type=" +
       "updateexperiment&autoDepends=false&accession=";
 
   public static final String ArrayDesignSearch = atlasPath +
       "admin?op=searchad&p=0&n=1" +
       "&search=";
 
   public static final String Monitoring = atlasPath +
       "admin?op=tasklog&event=";
 
   public static final String LogIn = atlasPath +
       "admin?op=login&userName=" +
       atlasUsername +
       "&password=" +
       atlasPassword;
 
 
   /**
    * Parses the response from Atlas REST API, extracts and evaluates the
    * event.
    *
    * @param response response received from Atlas REST API in HashMap format
    * @return exit code for the process
    */
   public int getExitCode(HashMap<String, Object> response) {
     HashMap<String, Object> items = items(response.get("items").toString());
     if (items.get("event").equals(
         AtlasEvents.FAILED.toString())) {
       return 1;
     }
     else {
       return 0;
     }
   }
 
   /**
    * Parses the response from Atlas REST API, extracts message and returns
    * it.
    *
    * @param response response received from Atlas REST API in HashMap format
    * @return message text extracted from the REST API response
    */
   public String getMessage(HashMap<String, Object> response) {
     String result = "";
     HashMap<String, Object> items = items(response.get("items").toString());
     result = items.get("message").toString();
 
     return result;
   }
 
   /**
    * Parses the response from Atlas REST API for the monitored process Returns
    * true if process has been finished.
    *
    * @param response response received from Atlas REST API in HashMap format
    * @return true when process is finished, false otherwise
    */
   public boolean isComplete(HashMap<String, Object> response) {
     boolean result = false;
 
     HashMap<String, Object> items = items(response.get("items").toString());
     System.out.println("EVENT:" + items.get("event"));
 
     if (!items.get("event").equals(
         CommonAtlasProcesses.AtlasEvents.SCHEDULED.toString()) &&
         !items.get("event").equals(
             CommonAtlasProcesses.AtlasEvents.STARTED.toString())) {
       result = true;
     }
     return result;
   }
 
   /**
    * Parses the response from Atlas REST API, by using parameters extracts
    * Atlas job ID.
    *
    * @param response response received from Atlas REST API in HashMap format
    * @return Atlas job ID to monitor the job status
    */
   public String getResultValue(HashMap<String, Object> response,
                                String parameters) {
     String jobID = "WITHOUT_MONITORING";
     try {
       jobID = response.get(parameters).toString();
     }
     catch (Exception e) {
       jobID =
           AbstractRESTAPIProcess.RESTAPIEvents.WITHOUT_MONITORING.toString();
       e.printStackTrace();
     }
 
     return jobID;
   }
 
   /**
    * Parses the nested response from Atlas REST API. Items are stored as
    * key,value.
    *
    * @param response entry from the main HashMap with items pairs inside
    * @return HashMap with parsed items
    */
   public HashMap<String, Object> items(String response) {
     //output parsing and HashMap object creation
     String delimiter = ",";
     String[] temp = response.split(delimiter);
     HashMap<String, Object> items = new HashMap<String, Object>();
     for (int i = 0; i < temp.length; i++) {
       delimiter = "=";
       String val = temp[i].replaceAll("\\[", "").replaceAll("\\{", "")
           .replaceAll("\\]", "").replaceAll("\\}", "");
       items.put(val.split(delimiter)[0].trim(),
                 (val.split(delimiter).length > 1 ? val.split(delimiter)[1]
                     : null));
     }
 
     return items;
   }
 }
