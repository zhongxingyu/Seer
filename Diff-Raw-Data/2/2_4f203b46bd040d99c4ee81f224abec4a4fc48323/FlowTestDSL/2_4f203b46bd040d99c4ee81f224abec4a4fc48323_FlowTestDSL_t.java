 package org.amplafi.dsl;
 
 import groovy.lang.Closure;
 
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Scanner;
 import java.util.concurrent.Callable;
 import java.util.concurrent.Future;
 import java.util.concurrent.FutureTask;
 import java.util.concurrent.TimeUnit;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.amplafi.flow.FlowException;
 import org.amplafi.flow.definitions.FarReachesServiceInfo;
 import org.amplafi.flow.utils.AdminTool;
 import org.amplafi.flow.utils.FlowResponse;
 import org.amplafi.flow.utils.GeneralFlowRequest;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 import org.mortbay.jetty.Request;
 import org.mortbay.jetty.Server;
 import org.mortbay.jetty.handler.AbstractHandler;
 
 import com.sworddance.util.CUtilities;
 
 /**
  * This class defines the methods that are callable within the flow test DSL
  */
 public class FlowTestDSL {
 
     private static final String API_DEFAULT = "api";
 
     private static final String PERMANENT_API_KEY_CALL = "PermanentApiKey";
 
     public static final String API_PUBLIC = "public";
 
     public static final String API_READONLY = "readOnly";
 
     public static final String API_PERMANENT = "permanent";
 
     public static final String API_TEMPORARY = "temporary";
 
     public static final String API_SU = "su";
 
     private String default_url;
 
     private String permanentKey;
 
     private String temporaryKey;
 
     private String readOnlyKey;
 
     private ScriptRunner runner;
 
     private Log log;
 
     private FarReachesServiceInfo serviceInfo;
 
     private Server server;
 
     private int currentPort;
 
     private static final Scanner INPUT = new Scanner(System.in);
 
     public FlowTestDSL(FarReachesServiceInfo serviceInfo, ScriptRunner runner) {
         this.serviceInfo = serviceInfo;
         this.runner = runner;
         this.default_url = serviceInfo.getProperty("testPluginUrl");
     }
 
     public void setKey(String api, String key) {
         String keystring = null;
         switch (api) {
         case API_SU:
             keystring = API_SU;
             break;
         case API_TEMPORARY:
             this.temporaryKey = key;
             keystring = API_TEMPORARY;
             break;
         case API_PERMANENT:
             this.permanentKey = key;
             keystring = API_PERMANENT;
             break;
         case API_READONLY:
             this.readOnlyKey = key;
             keystring = "read only";
             break;
         }
         System.out.println(keystring + " has been updated to " + key);
     }
 
     public String getKey(String api) {
         switch (api) {
         case API_SU:
             return this.serviceInfo.getProperty("supKey");
         case API_DEFAULT:
             if (this.readOnlyKey != null) {
                 return this.readOnlyKey;
             } else if (this.permanentKey != null) {
                 return this.permanentKey;
             } else if (this.temporaryKey != null) {
                 String temp = this.temporaryKey;
                 this.temporaryKey = null;
                 return temp;
             } else {
                 return this.getKey(API_SU);
             }
         case API_PUBLIC:
             return null;
         }
         return null;
     }
 
     public FlowResponse request(String api, String flowName, Map<String, String> paramsMap) {
         GeneralFlowRequest request = createGeneralFlowRequest(api, flowName, paramsMap);
         return request.sendRequest();
     }
 
     public FlowResponse request(String api, String flowName) {
         GeneralFlowRequest request = createGeneralFlowRequest(api, flowName, Collections.<String,String>emptyMap());
         return request.sendRequest();
     }
 
     public FlowResponse callbackRequest(String api, String flowName, Map<String, String> parametersMap) {
         return callbackRequest(this.default_url, api, flowName, parametersMap);
     }
 
     public FlowResponse callbackRequest(String rootUrl, String api, String flowName, Map<String, String> parametersMap) {
         parametersMap.put("callbackUri", "http://" + rootUrl + ":1234");
         return openPort(1234, 5, api, flowName, parametersMap);
     }
 
     public void obtainPermanentKey(String rootUrl, String email) {
         FlowResponse response = callbackRequest(rootUrl, API_PUBLIC, "TemporaryApiKey",
             CUtilities.<String, String> createMap("apiCall", PERMANENT_API_KEY_CALL));
         String temporaryApiKey = response.get("temporaryApiKey");
         setKey(API_TEMPORARY, temporaryApiKey);
         response = callbackRequest(API_DEFAULT, PERMANENT_API_KEY_CALL, CUtilities.<String, String> createMap("temporaryApiKey", temporaryApiKey, "usersList",
             "[{'email':'" + email + "','roleType':'adm','displayName':'user','externalId':1}]", "defaultLanguage", "en", "selfName", "user's Blog!",
             "completeList", "true"));
         this.permanentKey = response.get("permanentApiKeys.1");
     }
 
     /**
      * Sends a request to the named flow with the specified parameters.
      *
      * @param flowName to call.
      * @param paramsMap key value map of parameters to send.
      */
     private GeneralFlowRequest createGeneralFlowRequest(String api, String flowName, Map<String, String> paramsMap) {
         if (api == null) {
             api = serviceInfo.getApiVersion();
         }
         String selectedKey = getKey(api);
         Collection<NameValuePair> requestParams = new ArrayList<NameValuePair>();
         if ( paramsMap != null ) {
             for (Map.Entry<String, String> entry : paramsMap.entrySet()) {
                 requestParams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
             }
         }
         FarReachesServiceInfo serviceInfo = new FarReachesServiceInfo(this.serviceInfo);
         serviceInfo.setApiVersion(api);
         GeneralFlowRequest request = new GeneralFlowRequest(serviceInfo, selectedKey, flowName, requestParams);
         return request;
     }
 
     // Kostya: these are used by RealisticParams* tests..
     // /**
     // * Throws a test error if the actual data returned from the server is not
     // the same as.
     // * the expected JSON.
     // * @param expectedJSONData.
     // */
     // void expect(String expectedJSONData){
     // try{
     // JSONObject expected = new JSONObject(expectedJSONData);
     // JSONObject actual = new JSONObject(lastRequestResponse);
     // assertTrue(compare(expected,actual,null));
     // }catch(JSONException ex){
     // def expected = new JSONArray(expectedJSONData);
     // def actual = new JSONArray(lastRequestResponse);
     // assertEquals(expected, actual);
     // }
     // }
     //
     // /**
     // * Throws a test error if the actual data returned from the server is not
     // the same as.
     // * the expected JSON.
     // * @param expectedJSONData, ignorePathList.
     // */
     // void expect(String expectedJSONData,List<String> ignorePathList){
     // try{
     // JSONObject expected = new JSONObject(expectedJSONData);
     // JSONObject actual = new JSONObject(lastRequestResponse);
     // assertTrue(compare(expected,actual,ignorePathList));
     // }catch(JSONException ex){
     // // then see if it is an array
     // def expected = new JSONArray(expectedJSONData);
     // def actual = new JSONArray(lastRequestResponse);
     // assertEquals(expected, actual);
     // }
     // }
 
     // /**
     // * Throws exception if response has an error.
     // * @param response
     // */
     // def checkError(FlowResponse response){
     // if(response.hasError()) {
     // String mesg = "Error in the script " + this.name +
     // ". The response status is " + response.getHttpStatusCode() + "\n";
     // response.getErrorMessage();
     // throw new RuntimeException(mesg);
     // }
     // }
     //
     // /**
     // * Pretty Prints Last Response.
     // */
     // private def prettyPrintResponse(){
     // emitOutput(getResponseData().toString(4));
     // }
     //
     //
     // private def printTaskInfo(info){
     // emitOutput "\n";
     // emitOutput THICK_DIVIDER;
     // emitOutput info;
     // emitOutput THICK_DIVIDER;
     // }
     //
     // /**
     // * Prints data in table format.
     // * @param entries
     // * @param tabularTmpl
     // * @param headers
     // * @param keyPaths
     // * @return
     // */
     // private def printTabular(entries, tabularTmpl, headers, keyPaths){
     // emitOutput sprintf(tabularTmpl, headers);
     // emitOutput THIN_DIVIDER; == false
     // for(int i = 0; i < entries.length(); i++) {
     // def entry = entries.get(i);
     // def value = new String[keyPaths.size() + 1];
     // value[0] = Integer.toString(i + 1);
     // for(int j = 0; j < value.length - 1; j++){
     // value[j + 1] = entry.optStringByPath(keyPaths[j]);
     // }
     // println sprintf(tabularTmpl,value);
     // }
     // }
     //
     // /**
     // * Prints a map in table form.
     // * @param map
     // * @param tabularTmpl
     // * @param headers
     // * @param keys
     // * @return
     // */
     // private def printTabularMap(map, tabularTmpl, headers, keys){
     // emitOutput sprintf(tabularTmpl, headers);
     // emitOutput THIN_DIVIDER;
     // for(entry in map.values()){
     // def value = new String[keys.size()];
     // for(int j = 0; j < value.length; j++){
     // value[j] = entry.get(keys[j]);
     // }
     // println sprintf(tabularTmpl, value);
     // }
     // }
 
     /**
      * Call a script with params.
      *
      * @param scriptName script name.
      * @param callParamsMap script parameters.
      */
     public Object callScript(String scriptName, Map<String, String> callParamsMap) {
         getLog().debug("In callScript() scriptName = " + scriptName);
         Closure exe = runner.createClosure(scriptName, callParamsMap);
         getLog().debug("callScript created closure  for scriptName = " + scriptName);
         Object ret = null;
         if (exe != null) {
             getLog().debug("callScript() closure not null ");
             exe.setDelegate(this);
             getLog().debug("callScript() about to run "+ scriptName);
             ret = exe.call();
             getLog().debug("callScript() finished running "+ scriptName);
         }
         return ret;
     }
 
     /**
      * Call a script with no params.
      *
      * @param scriptName script name.
      */
     public Object callScript(String scriptName) {
         return callScript(scriptName, new HashMap<String, String>());
     }
 
     // /**
     // * method to compare the actual jsonObject return to us with our expected,
     // and can ignore some compared things,return true when they are the same.
     // * @param expected is expected JSONObject
     // * @param actual is actual JSONObject
     // * @param excludePaths is ignore list
     // * @return true if the expected object is same with the actual object
     // */
     // private boolean compare(JSONObject expected, JSONObject actual,
     // List<String> excludePaths){
     // def isEqual = compare(expected,actual,excludePaths,"/");
     // return isEqual;
     // }
     //
     // /**
     // * method to compare the actual jsonObject return to us with our expected,
     // and can ignore some compared things,return true when they are the same.
     // * @param expected is expected JSONObject.
     // * @param actual is actual JSONObject.
     // * @param excludePaths is ignore list.
     // * @param currentPath is path of the property.
     // * @return true if the expected object is same with the actual object.
     // */
     // private boolean compare(JSONObject expected, JSONObject actual,
     // List<String> excludePaths, String currentPath){
     // String newLine = System.getProperty("line.separator");
     // def isEqual = false;
     // // when the compared object is null,return true directly.
     // if(expected == null && actual == null){
     // return true;
     // }
     // if(expected == null || actual == null){
     // fail("After Calling ${lastRequestString}.Response did not match expected:"+
     // newLine
     // + "expected data was " + expected + newLine
     // + "but the actual data was "+ actual);
     // return false;
     // }
     // def expectedNames = expected.names();
     // def actualNames = actual.names();
     // if(expectedNames == null && actualNames == null){
     // return true;
     // }
     // if(expectedNames == null || actualNames == null){
     // fail("After Calling ${lastRequestString}.Response did not match expected names:"
     // + newLine
     // + "expected names was " + expectedNames + newLine
     // + "but the actual names was "+ actualNames + newLine
     // + "expected data was " + expected + newLine
     // + "but the actual data was "+ actual);
     // return false;
     // }
     // int i = 0;
     // //loops all of the property name in the object
     // actualNames.each { actualName ->
     // def expectedName = expectedNames.get(i);
     // def actualValue = actual.get(actualName);
     // def expectedValue = expected.get(expectedName);
     // //if no ignore compared things or current compared thing is not in the
     // ignore,then we go to compare.
     // if(excludePaths == null){
     // excludePaths = new ArrayList<String>();
     // excludePaths.add("there is no ignore path");
     // }
     // if(!excludePaths.contains(currentPath)){
     // if(actualName.equals(expectedName)){
     // if(expectedValue instanceof JSONObject && actualValue instanceof
     // JSONObject ){
     // isEqual = compare(expectedValue,actualValue,excludePaths,currentPath +
     // actualName + "/");
     // }else{
     // if (!excludePaths.contains(currentPath + actualName + "/")){
     // isEqual = actualValue.equals(expectedValue);
     // if(!isEqual){
     // fail("After Calling ${lastRequestString}.Response did not match expected in following path:"
     // + currentPath + newLine + actualName +":" +newLine
     // + "expected was " + expectedValue + newLine
     // + "but the actual was " + actualValue + newLine
     // + "expected data was " + expected + newLine
     // + "but the actual data was "+ actual);
     // }
     // }
     // }
     // }else{
     // isEqual = false;
     // fail("After Calling ${lastRequestString}.Response did not match expected property name:"+
     // newLine
     // + "expected name was "+expectedName + newLine
     // + "but the actual name was " + actualName + newLine
     // + "expected data was " + expected + newLine
     // + "but the actual data was "+ actual);
     // }
     // }else{
     // isEqual = true;
     // return;
     // }
     // if(isEqual == false){
     // return;
     // }
     // i++;
     // }
     // return isEqual;
     // }
 
     /**
      * Gets a jetty server instance for the port.
      *
      * @param portNo
      * @return
      */
     private Server getServer(int portNo) {
         if (server == null || currentPort != portNo) {
             server = new Server(portNo);
         }
         return server;
     }
 
     /**
      * The method is to open a port and listens request.
      *
      * @param portNo is port number.
      * @param timeOutSeconds is time out seconds.
      * @param doNow is the request in script.
      * @param handleRequest is the handle method when recieved a request.
      */
     private FlowResponse openPort(int portNo, int timeOutSeconds, String api, String flowName, Map<String, String> parametersMap) {
         Object monitor = new Object();
         server = getServer(portNo);
         server.setGracefulShutdown(1000);
         MyHandler myHandler = new MyHandler(monitor);
         server.setHandler(myHandler);
         try {
             server.start();
             FlowResponse response = request(api, flowName, parametersMap);
             if (response.hasError()) {
                 throw new FlowException("Async request failed.");
             } else {
                 synchronized (monitor) {
                     monitor.wait(TimeUnit.SECONDS.toMillis(1000));
                 }
                 if (!myHandler.getReceived()) {
                     server.stop();
                     throw new FlowException("Server did not send any request");
                 }
                 if (myHandler.getHandlingError() != null) {
                     throw myHandler.getHandlingError();
                 }
             }
         } catch (Exception e) {
             e.printStackTrace();
         } finally {
             try {
                 server.stop();
             } catch (Exception e) {
 
             }
         }
         return myHandler.handlerReturn;
     }
 
     /**
      * This class defines the handler of the client jetty server.
      */
     public class MyHandler extends AbstractHandler {
         Object monitor;
 
         boolean received = false;
 
         Exception handlerError;
 
         FlowResponse handlerReturn;
 
         /**
          * The method is constructor of the class.
          *
          * @param handleRequest is handleRequest closure.
          * @param monitor is a synchronized lock.
          */
         MyHandler(Object monitor) {
             this.monitor = monitor;
         }
 
         /**
          * The method is handle of the client jetty server.
          *
          * @param target is the target of the request - either a URI or a name.
          * @param request is the request either as the Request object or a wrapper of that request.
          * @param response is the response as the Response object or a wrapper of that request.
          * @param dispatch is the dispatch mode: REQUEST, FORWARD, INCLUDE, ERROR.
          */
         @Override
         public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch) throws IOException, ServletException {
             received = true;
             response.setContentType("text/html");
             response.setStatus(HttpServletResponse.SC_OK);
             ((Request) request).setHandled(true);
             try {
                 handlerReturn = new FlowResponse(request);
             } catch (Exception e) {
                 getLog().debug("In FlowTestBuilder, MyHandler. Request handler in DSL script returned error. " + e);
                 handlerError = e;
             } finally {
                 synchronized (monitor) {
                     monitor.notifyAll();
                 }
             }
         }
 
         /**
          * Get method :get received value.
          */
         boolean getReceived() {
             return received;
         }
 
         /**
          * Get method :get handlerError;
          */
         Exception getHandlingError() {
             return handlerError;
         }
     }
 
     public boolean describeFlow(String api, String flow) {
         String key = getKey(api);
         FarReachesServiceInfo frsi = new FarReachesServiceInfo(this.serviceInfo);
         frsi.setApiVersion(api);
         GeneralFlowRequest request = new GeneralFlowRequest(frsi, key, flow);
         FlowResponse flows = request.describeFlowWithResponse();
         System.out.println(flows.toString());
         return true;
     }
 
     private static final String DEFAULT_ROOT_URL = "example.co.uk";
 
     /**
      * Sends a request to the named flow with the specified parameters.
      *
      * @param flowName to call.
      * @param paramsMap key value map of parameters to send.
      * @return response string
      */
     public FlowResponse request(String flowName, Map<String, String> paramsMap) {
         return request(null, flowName, paramsMap);
     }
 
     public FlowResponse request(String apiKey, String api, String flowName, Map<String, String> paramsMap) {
         GeneralFlowRequest request = createGeneralFlowRequest(apiKey, api, flowName, paramsMap);
         return request.sendRequest();
     }
 
     public Future<FlowResponse> requestAsync(final String flowName, final Map<String, String> paramsMap) {
         FutureTask<FlowResponse> result = new FutureTask<FlowResponse>(new Callable<FlowResponse>() {
 
             @Override
             public FlowResponse call() throws Exception {
                 return request(flowName, paramsMap);
             }
 
         });
         new Thread(result).start();
         return result;
     }
 
     /**
      * This method will automatically add a callbackParam into params and send the request. With a
      * callback uri It will then use openPort to call the flow and return the response.
      *
      * @param flowName to call.
      * @param parametersMap key value map of parameters to send.
      * @return flow response object
      */
     public FlowResponse callbackRequest(String flowName, Map<String, String> parametersMap) {
         return callbackRequest(null, flowName, parametersMap);
     }
 
     public Future<FlowResponse> callbackRequestAsync(final String flowName, final Map<String, String> parametersMap) {
         FutureTask<FlowResponse> result = new FutureTask<>(new Callable<FlowResponse>() {
 
             @Override
             public FlowResponse call() throws Exception {
                 return callbackRequest(flowName, parametersMap);
             }
 
         });
         new Thread(result).start();
         return result;
     }
 
     /**
      * Sends out "secured" request to server. Obtaining a temporary one-off key for the call first.
      *
      * @param flowName
      * @param parametersMap
      * @return
      */
     public FlowResponse securedRequest(String flowName, Map<String, String> parametersMap) {
         FlowResponse response = callbackRequest("TemporaryApiKey", CUtilities.<String, String> createMap("apiCall", flowName));
         if (!response.hasError()) {
             String key = response.get("temporaryApiKey");
             response = request(key, null, flowName, parametersMap);
         }
         return response;
     }
 
     public Future<FlowResponse> securedRequestAsync(final String flowName, final Map<String, String> parametersMap) {
         FutureTask<FlowResponse> result = new FutureTask<>(new Callable<FlowResponse>() {
 
             @Override
             public FlowResponse call() throws Exception {
                 return securedRequest(flowName, parametersMap);
             }
 
         });
         new Thread(result).start();
         return result;
     }
 
     public String obtainPermanentKey(String rootUrl) {
         FlowResponse response = callbackRequest(rootUrl, API_PUBLIC, "TemporaryApiKey",
             CUtilities.<String, String> createMap("apiCall", PERMANENT_API_KEY_CALL));
         String temporaryApiKey = response.get("temporaryApiKey");
         setKey(API_TEMPORARY, temporaryApiKey);
         // HACK TODO FIX: hard coded values TO_KOSTYA
         response = callbackRequest(PERMANENT_API_KEY_CALL, CUtilities.<String, String> createMap("temporaryApiKey", temporaryApiKey, "usersList",
             "[{'email':'admin@example.com','roleType':'adm','displayName':'user','externalId':1}]", "defaultLanguage", "en", "selfName",
             "user's Blog! С русскими буквами.", "completeList", "true"));
         return response.get("permanentApiKeys.1");
     }
 
     public String obtainPermanentKey() {
         return obtainPermanentKey(DEFAULT_ROOT_URL);
     }
 
     /**
      * Sends a request to the named flow with the specified parameters.
      *
      * @param flowName to call.
      * @param flowName2
      * @param paramsMap key value map of parameters to send.
      */
     private GeneralFlowRequest createGeneralFlowRequest(String apiKey, String api, String flowName, Map<String, String> paramsMap) {
         if (api == null) {
             api = serviceInfo.getApiVersion();
         }
         Collection<NameValuePair> requestParams = new ArrayList<NameValuePair>();
         if ( paramsMap != null) {
             for (Map.Entry<String, String> entry : paramsMap.entrySet()) {
                 requestParams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
             }
         }
         FarReachesServiceInfo serviceInfo = new FarReachesServiceInfo(this.serviceInfo);
         serviceInfo.setApiVersion(api);
         GeneralFlowRequest request = new GeneralFlowRequest(serviceInfo, apiKey, flowName, requestParams);
         return request;
     }
 
     // Kostya: these are used by RealisticParams* tests..
     //    /**
     //     * Throws a test error if the actual data returned from the server is not the same as.
     //     * the expected JSON.
     //     * @param expectedJSONData.
     //     */
     //    void expect(String expectedJSONData){
     //        try{
     //            JSONObject expected = new JSONObject(expectedJSONData);
     //            JSONObject actual = new JSONObject(lastRequestResponse);
     //            assertTrue(compare(expected,actual,null));
     //        }catch(JSONException ex){
     //            def expected = new JSONArray(expectedJSONData);
     //            def actual = new JSONArray(lastRequestResponse);
     //            assertEquals(expected, actual);
     //        }
     //    }
     //
     //    /**
     //     * Throws a test error if the actual data returned from the server is not the same as.
     //     * the expected JSON.
     //     * @param expectedJSONData, ignorePathList.
     //     */
     //    void expect(String expectedJSONData,List<String> ignorePathList){
     //        try{
     //            JSONObject expected = new JSONObject(expectedJSONData);
     //            JSONObject actual = new JSONObject(lastRequestResponse);
     //            assertTrue(compare(expected,actual,ignorePathList));
     //        }catch(JSONException ex){
     //            // then see if it is an array
     //            def expected = new JSONArray(expectedJSONData);
     //            def actual = new JSONArray(lastRequestResponse);
     //            assertEquals(expected, actual);
     //        }
     //    }
 
     //    /**
     //     * Throws exception if response has an error.
     //     * @param response
     //     */
     //    def checkError(FlowResponse response){
     //        if(response.hasError()) {
     //            String mesg = "Error in the script " + this.name + ". The response status is " + response.getHttpStatusCode() + "\n";
     //            response.getErrorMessage();
     //            throw new RuntimeException(mesg);
     //        }
     //    }
     //
     //    /**
     //     * Pretty Prints Last Response.
     //     */
     //    private def prettyPrintResponse(){
     //        emitOutput(getResponseData().toString(4));
     //    }
     //
     //
     //    private def printTaskInfo(info){
     //        emitOutput "\n";
     //        emitOutput THICK_DIVIDER;
     //        emitOutput info;
     //        emitOutput THICK_DIVIDER;
     //    }
     //
     //    /**
     //     * Prints data in table  format.
     //     * @param entries
     //     * @param tabularTmpl
     //     * @param headers
     //     * @param keyPaths
     //     * @return
     //     */
     //    private  def printTabular(entries, tabularTmpl, headers, keyPaths){
     //        emitOutput sprintf(tabularTmpl, headers);
     //        emitOutput THIN_DIVIDER; == false
     //        for(int i = 0; i < entries.length(); i++) {
     //            def entry = entries.get(i);
     //            def value = new String[keyPaths.size() + 1];
     //            value[0] = Integer.toString(i + 1);
     //            for(int j = 0; j < value.length - 1; j++){
     //                value[j + 1] = entry.optStringByPath(keyPaths[j]);
     //            }
     //            println sprintf(tabularTmpl,value);
     //        }
     //    }
     //
     //    /**
     //     * Prints a map in table form.
     //     * @param map
     //     * @param tabularTmpl
     //     * @param headers
     //     * @param keys
     //     * @return
     //     */
     //    private def printTabularMap(map, tabularTmpl, headers, keys){
     //        emitOutput sprintf(tabularTmpl, headers);
     //        emitOutput THIN_DIVIDER;
     //        for(entry in map.values()){
     //            def value = new String[keys.size()];
     //            for(int j = 0; j < value.length; j++){
     //                value[j] = entry.get(keys[j]);
     //            }
     //            println sprintf(tabularTmpl, value);
     //        }
     //    }
 
     //    /**
     //     * method to compare the actual jsonObject return to us with our expected, and can ignore some compared things,return true when they are the same.
     //     * @param expected is expected JSONObject
     //     * @param actual is actual JSONObject
     //     * @param excludePaths is ignore list
     //     * @return true if the expected object is same with the actual object
     //     */
     //    private boolean compare(JSONObject expected, JSONObject actual, List<String> excludePaths){
     //        def isEqual = compare(expected,actual,excludePaths,"/");
     //        return isEqual;
     //    }
     //
     //    /**
     //     * method to compare the actual jsonObject return to us with our expected, and can ignore some compared things,return true when they are the same.
     //     * @param expected is expected JSONObject.
     //     * @param actual is actual JSONObject.
     //     * @param excludePaths is ignore list.
     //     * @param currentPath is path of the property.
     //     * @return true if the expected object is same with the actual object.
     //     */
     //    private boolean compare(JSONObject expected, JSONObject actual, List<String> excludePaths, String currentPath){
     //        String newLine = System.getProperty("line.separator");
     //        def isEqual = false;
     //        // when the compared object is null,return true directly.
     //        if(expected == null && actual == null){
     //            return true;
     //        }
     //        if(expected == null || actual == null){
     //            fail("After Calling ${lastRequestString}.Response did not match expected:"+ newLine
     //                    + "expected data was " + expected + newLine
     //                    + "but the actual data was "+ actual);
     //            return false;
     //        }
     //        def expectedNames = expected.names();
     //        def actualNames = actual.names();
     //        if(expectedNames == null && actualNames == null){
     //            return true;
     //        }
     //        if(expectedNames == null || actualNames == null){
     //            fail("After Calling ${lastRequestString}.Response did not match expected names:" + newLine
     //                    + "expected names was " + expectedNames + newLine
     //                    + "but the actual names was "+ actualNames + newLine
     //                    + "expected data was " + expected + newLine
     //                    + "but the actual data was "+ actual);
     //            return false;
     //        }
     //        int i = 0;
     //        //loops all of the property name in the object
     //        actualNames.each { actualName ->
     //            def expectedName = expectedNames.get(i);
     //            def actualValue = actual.get(actualName);
     //            def expectedValue = expected.get(expectedName);
     //            //if no ignore compared things or current compared thing is not in the ignore,then we go to compare.
     //            if(excludePaths == null){
     //                excludePaths = new ArrayList<String>();
     //                excludePaths.add("there is no ignore path");
     //            }
     //            if(!excludePaths.contains(currentPath)){
     //                if(actualName.equals(expectedName)){
     //                    if(expectedValue instanceof JSONObject && actualValue instanceof JSONObject ){
     //                        isEqual = compare(expectedValue,actualValue,excludePaths,currentPath  + actualName + "/");
     //                    }else{
     //                        if (!excludePaths.contains(currentPath  + actualName + "/")){
     //                            isEqual = actualValue.equals(expectedValue);
     //                            if(!isEqual){
     //                                fail("After Calling ${lastRequestString}.Response did not match expected in following path:"
     //                                        + currentPath + newLine + actualName +":" +newLine
     //                                        + "expected was " + expectedValue + newLine
     //                                        + "but the actual was " + actualValue + newLine
     //                                        + "expected data was " + expected + newLine
     //                                        + "but the actual data was "+ actual);
     //                            }
     //                        }
     //                    }
     //                }else{
     //                    isEqual = false;
     //                    fail("After Calling ${lastRequestString}.Response did not match expected property name:"+ newLine
     //                            + "expected name was "+expectedName + newLine
     //                            + "but the actual name was " + actualName + newLine
     //                            + "expected data was " + expected + newLine
     //                            + "but the actual data was "+ actual);
     //                }
     //            }else{
     //                isEqual = true;
     //                return;
     //            }
     //            if(isEqual == false){
     //                return;
     //            }
     //            i++;
     //        }
     //        return isEqual;
     //    }
 
     private static Map<Integer, Server> serverMap = new HashMap<>();
 
     private static int START_PORT = 1234;
 
     /**
      * Gets a jetty server instance for a non-used port.
      *
      * @param portNo
      * @return
      */
     private int initServer() {
         synchronized (serverMap) {
             Server server = null;
             int port = START_PORT;
             while (serverMap.containsKey(port)) {
                 server = serverMap.get(port);
                 if (server.isStopped()) {
                     break;
                 }
                 port++;
             }
             server = new Server(port);
             serverMap.put(port, server);
             return port;
         }
     }
 
     /**
      * The method is to open a port and listens request.
      *
      * @param portNo is port number.
      * @param timeOutSeconds is time out seconds.
      * @param doNow is the request in script.
      * @param handleRequest is the handle method when recieved a request.
      */
     private FlowResponse openPort(int timeOutSeconds, String api, String flowName, Map<String, String> parametersMap, String rootUrl) {
         Object monitor = new Object();
         int port = initServer();
         Server server = serverMap.get(port);
         server.setGracefulShutdown(1000);
         MyHandler myHandler = new MyHandler(monitor);
         server.setHandler(myHandler);
         try {
             server.start();
             parametersMap.put("callbackUri", "http://" + rootUrl + ":" + port);
             FlowResponse response = request(api, flowName, parametersMap);
             if (response.hasError()) {
                 throw new FlowException("Async request failed.");
             } else {
                 synchronized (monitor) {
                     monitor.wait(timeOutSeconds * 1000);
                 }
                 if (!myHandler.getReceived()) {
                     server.stop();
                     throw new FlowException("Server did not send any request");
                 }
                 if (myHandler.getHandlingError() != null) {
                     throw new FlowException("Error Handling Request.", myHandler.getHandlingError());
                 }
             }
         } catch (Exception e) {
             e.printStackTrace();
         } finally {
             try {
                 server.stop();
             } catch (Exception e) {
 
             }
         }
         return myHandler.handlerReturn;
     }
 
     public boolean validateDate(String dateString) {
         return dateString.matches("\\d\\d\\d\\d-\\d\\d-\\d\\d");
     }
 
     public String today() {
         return formatDate(new Date());
     }
 
     public String lastMonth() {
         Calendar now = Calendar.getInstance();
         now.add(Calendar.MONTH, -1);
         return formatDate(now.getTime());
     }
 
     public String formatDate(Date date) {
         DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
         return df.format(date);
     }
 
     /**
      * Get the logger for this class.
      */
     private Log getLog() {
         if (this.log == null) {
             this.log = LogFactory.getLog(AdminTool.class);
         }
         return this.log;
     }
 
     /**
      * allows script makers to ask for a value (using groovy binding mechanism) and in case the
      * empty value "" is returned - for example, pressing enter without typing anything - set a
      * default value and print an error message.
      *
      * @param inputVariable the name you want to appear on screen when using this function
      * @param defaultValue the value to which the variable will be defaulted in case the value can't
      *            be filled
      * @param errorMessage the error message in case you want to print one. Put null for no error
      *            message
      * @return
      */
     public String input(String defaultValue, String request) {
         String defaultIndicator = defaultValue != null ? " [" + defaultValue + "]" : "";
         System.out.println(request + defaultIndicator + ":");
         String inputVariable = INPUT.nextLine();
         if (inputVariable == null || inputVariable.isEmpty()) {
             return defaultValue;
         } else {
             return inputVariable;
         }
     }
 
     public String input(String request) {
         return input(null, request);
     }
 
     public void obtainReadOnlyKey() {
         System.out.println("A readonly key is needed to proceed. Specify provider the key for.");
 
 //        Binding binding = this.runner.getNewBinding(new HashMap());
 //        Object publicUri = binding.getVariable("publicUri");
         String publicUrl = input(/*publicUri+*/"", "Enter provider url");
         String reasonForAccess = input("Customer support tool automatic request.", "Reason for access");
        FlowResponse response = request("su", "SuApiKey", CUtilities.<String,String>createMap(
                             "publicUri" , publicUrl,
                             "reasonForAccess", reasonForAccess));
         if (!response.hasError()) {
             readOnlyKey = response.toString();
         } else {
             System.out.println(response);
             throw new FlowException("Failed to get read only key.");
         }
     }
 
     public void eraseReadOnlyKey() {
         readOnlyKey = null;
     }
 }
