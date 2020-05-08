 package org.amplafi.flow;
 
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.amplafi.flow.utils.GeneralFlowRequest;
 import org.amplafi.json.JSONArray;
 import org.amplafi.json.JSONException;
 import org.amplafi.json.JSONObject;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 import org.testng.annotations.DataProvider;
 import org.testng.annotations.Factory;
 import org.testng.annotations.Test;
 import static org.testng.Assert.*;
 
 /**
  * Provides convenient access to the system properties used by wire server tests
  * @author Paul Smout
  */
 public class TestProperties {
 
     /**
      * These static variables are overridden in the static initializer below.
      */
     public static String requestUriString;
     
 	/**
 	 * The api key for the wire server.
 	 */
     public static String apiKey = "dummyKey";    
     
     /**
      * The following Set contains flows that seem broken or suspicious. Having been identified and
      * reported they are awaiting fixing and will be exempt from the test These values are set in
      * the pom.xml in test configuration.
      */
     public static Set<String> ignoredFlows;    
 
     private static final String JSON_ACTIVITY_KEY = "activities";
     private static final String JSON_PARAMETER_KEY = "parameters";
     private static final String JSON_PARAMETER_NAME_KEY = "name";
     private static final String JSON_GENERIC_ERROR_MESSAGE_KEY = "errorMessage";
     private static final String JSON_GENERIC_ERROR_MESSAGE = "Exception while running flowState";
     private static final String HOST_PROPERTY_KEY = "host";
     private static final String PORT_PROPERTY_KEY = "port";
     private static final String API_PROPERTY_KEY = "key";   
	
 
     /*
      * Obtain the system properties for the test These may be set up in the pom.xml or passed in
      * from the command line with -D options.
      */
     static {
         apiKey = System.getProperty(API_PROPERTY_KEY,"");
         String host = System.getProperty(HOST_PROPERTY_KEY ,"sandbox.farreach.es");
         String port = System.getProperty(PORT_PROPERTY_KEY,"8080");
         
         requestUriString = host + ":" + port + "/c/" + apiKey   + "/apiv1"; 
         
         String ignoredFlowsStr = System.getProperty("ignoreFlows","");
         String[] ignoredFlowsArr = ignoredFlowsStr.split(",");
         ignoredFlows = new HashSet<String>(Arrays.asList(ignoredFlowsArr));
 
 
     }
 
 
 
 }
