 /*******************************************************************************
  * Copyright (c) 2012, THE BOARD OF TRUSTEES OF THE LELAND STANFORD JUNIOR UNIVERSITY
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification,
  * are permitted provided that the following conditions are met:
  *
  *    Redistributions of source code must retain the above copyright notice,
  *    this list of conditions and the following disclaimer.
  *    Redistributions in binary form must reproduce the above copyright notice,
  *    this list of conditions and the following disclaimer in the documentation
  *    and/or other materials provided with the distribution.
  *    Neither the name of the STANFORD UNIVERSITY nor the names of its contributors
  *    may be used to endorse or promote products derived from this software without
  *    specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
  * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
  * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
  * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
  * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
  * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
  * OF THE POSSIBILITY OF SUCH DAMAGE.
  *******************************************************************************/
 package org.cyclades.client;
 
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import org.cyclades.engine.stroma.STROMAResponse;
 import org.cyclades.engine.stroma.xstroma.STROMARequest;
 import org.cyclades.engine.stroma.xstroma.XSTROMABrokerRequest;
 import org.cyclades.engine.stroma.xstroma.XSTROMABrokerResponse;
 import org.cyclades.engine.util.MapHelper;
 import org.cyclades.io.ResourceRequestUtils;
 import org.cyclades.io.StreamUtils;
 
 /**
  * Helper class with methods for various DevOps tasks:
  * 
  *  - Clustered orchestration requests via HTTP
  *  - Clustered file uploads
  *  - Engine reloads
  *  - Etc...
  *  
  *  This functionality can alternatively be written by a developer in Java or any other language...these are just provided for
  *  convenience for folks that can reuse them from a Java friendly client.
  */
 public class DevOps {
     
     private static enum OnFaultStrategy {
         NOTHING, RETURN, EXCEPTION;
     }
     
     /**
      * Execute one to N X-STROMA requests on one to N nodes/servers
      * 
      * @param urls List of urls to the nodes/servers to run the commands on
      * @param requests The list of XSTROMABrokerRequests to execute
      * @param onFaultStrategy The strategy to use if an orchestration fault is encountered...one of NOTHING, RETURN or EXCEPTION
      * @param printResponses Verbose output if true
      * @return The responses of the last node accessed successfully, also taking into account the onFaultStrategy specified 
      * @throws Exception
      */
     public static List<Object> executeClusteredXSTROMARequests (String[] urls, List<XSTROMABrokerRequest> requests, 
             String onFaultStrategy, boolean printResponses) throws Exception {
         return executeClusteredXSTROMARequests (urls, "servicebroker", requests, onFaultStrategy, printResponses);
     }
     
     public static List<Object> executeClusteredXSTROMARequests (String[] urls, String serviceBrokerName, 
             List<XSTROMABrokerRequest> requests, String onFaultStrategy, boolean printResponses) throws Exception {
         final String eLabel = "DevOps.executeClusteredXSTROMARequests: ";
         List<Object> lastNodesReponseList = null;
         for (String url : urls) {
             try {
                 if (printResponses) System.out.println(eLabel + "Servicing URL: " + url);
                 lastNodesReponseList = executeXSTROMARequests(url, serviceBrokerName, requests, onFaultStrategy, printResponses);
                 if (printResponses) System.out.println(eLabel + "Success Servicing URL: " + url);
             } catch (Exception e) {
                 if (printResponses) System.out.println(eLabel + "Failed Servicing URL: " + url);
                 switch (OnFaultStrategy.valueOf(onFaultStrategy.toUpperCase())) {
                 case RETURN:
                      return lastNodesReponseList;
                 case EXCEPTION:
                     throw new Exception(eLabel + "Service fault encountered: " + e);
                 case NOTHING:
                     break;
                 }
             }
         }
         return lastNodesReponseList;
     }
     
     /**
      * Simple helper method to run a series of independent XSTROMABrokerRequests and return their responses in order. This method
      * assumes the target service will be "servicebroker", which will most likely, but not necessarily be reflected in the url
      * parameter. An alternate method will be provided for the rare case where a custom service broker is used with a name other
      * than "servicebroker". 
      * 
      * @param url The url to the designated servicebroker implementation
      * @param requests The list of XSTROMABrokerRequests to execute
      * @param onFaultStrategy The strategy to use if an orchestration fault is encountered...one of NOTHING, RETURN or EXCEPTION
      * @param printResponses Verbose output if true
      * @return A list of Objects, will be one of either XSTROMABrokerResponse or STROMAResponse if "chained" is set to
      *     "true" for an XSTROMABrokerRequest. Normally these will all be of the type XSTROMABrokerResponse
      * @throws Exception
      */
     public static List<Object> executeXSTROMARequests (String url, List<XSTROMABrokerRequest> requests, 
             String onFaultStrategy, boolean printResponses) throws Exception {
         return executeXSTROMARequests(url, "servicebroker", requests, onFaultStrategy, printResponses);
     }
    
     public static List<Object> executeXSTROMARequests (String url, String serviceBokerName, List<XSTROMABrokerRequest> requests, 
             String onFaultStrategy, boolean printResponses) throws Exception {
         List<Object> responseList = new ArrayList<Object>();
         for (XSTROMABrokerRequest request : requests) {
             String faultRaised = null;
             String xstromaResponseString =  new String(
                     Http.execute(new StringBuilder(url).append("/").append(serviceBokerName).toString(), request));
             XSTROMABrokerResponse xstromaResponseObject = XSTROMABrokerResponse.parse(xstromaResponseString);
             if (!xstromaResponseObject.getServiceName().equals(serviceBokerName)) {
                 STROMAResponse response = new STROMAResponse(xstromaResponseString);
                 responseList.add(response);
                 if (printResponses) printSTROMAResponse(response);
                 if (response.getErrorCode() != 0) faultRaised = response.getErrorMessage();
             } else {
                 responseList.add(xstromaResponseObject);
                 if (printResponses) printXSTROMAResponse(xstromaResponseObject);
                 if (xstromaResponseObject.getOrchestrationFault()) faultRaised = xstromaResponseObject.getResponses().get(
                         xstromaResponseObject.getResponses().size() - 1).getErrorMessage();
             }
             if (faultRaised != null) {
                 switch (OnFaultStrategy.valueOf(onFaultStrategy.toUpperCase())) {
                 case RETURN:
                      return responseList;
                 case EXCEPTION:
                     throw new Exception("Service fault encountered: " + faultRaised);
                 case NOTHING:
                     break;
                 }
             }
         }
         return responseList;
     }
     
     /**
      * Output the contents of a XSTROMABrokerResponse to stdout
      * 
      * @param xstromaResponseObject The response to output
      * @throws Exception
      */
     public static void printXSTROMAResponse (XSTROMABrokerResponse xstromaResponseObject) throws Exception {
         if (xstromaResponseObject.getOrchestrationFault()) System.out.println("[ORCHESTRATION FAULT RAISED]");
         System.out.println("\nX-STROMA Level Parameters");
         System.out.println("error-code: " + xstromaResponseObject.getErrorCode());
         System.out.println("transaction-data: " + xstromaResponseObject.getTransactionData());
         System.out.println("service-agent: " + xstromaResponseObject.getServiceAgent());
         System.out.println("duration: " + xstromaResponseObject.getDuration());
         System.out.println("parameters:" + MapHelper.parameterMapToJSON((xstromaResponseObject.getParameters() != null) ? 
                 xstromaResponseObject.getParameters() : new HashMap<String, List<String>>()));
         if (xstromaResponseObject.getErrorCode() != 0) {
             System.out.println("error-message: " + xstromaResponseObject.getErrorMessage());
             return;
         }
         for (STROMAResponse sr : xstromaResponseObject.getResponses()) printSTROMAResponse(sr);
     }
     
     /**
      * Execute one to N STROMA requests on one to N nodes/servers
      * 
      * @param urls List of urls to the nodes/servers to run the commands on
      * @param requests The list of STROMARequests to execute
      * @param onFaultStrategy The strategy to use if a fault is encountered...one of NOTHING, RETURN or EXCEPTION
      * @param printResponses Verbose output if true
      * @return The responses of the last node accessed successfully, also taking into account the onFaultStrategy specified
      * @throws Exception
      */
     public static List<STROMAResponse> executeClusteredSTROMARequests (String[] urls, List<STROMARequest> requests, 
             String onFaultStrategy, boolean printResponses) throws Exception {
         final String eLabel = "DevOps.executeClusteredSTROMARequests: ";
         List<STROMAResponse> lastNodesReponseList = null;
         for (String url : urls) {
             try {
                 if (printResponses) System.out.println(eLabel + "Servicing URL: " + url);
                 lastNodesReponseList = executeSTROMARequests(url, requests, onFaultStrategy, printResponses);
                 if (printResponses) System.out.println(eLabel + "Success Servicing URL: " + url);
             } catch (Exception e) {
                 if (printResponses) System.out.println(eLabel + "Failed Servicing URL: " + url);
                 switch (OnFaultStrategy.valueOf(onFaultStrategy.toUpperCase())) {
                 case RETURN:
                      return lastNodesReponseList;
                 case EXCEPTION:
                     throw new Exception(eLabel + "Service fault encountered: " + e);
                 case NOTHING:
                     break;
                 }
             }
         }
         return lastNodesReponseList;
     }
     
     /**
      * Simple helper method to run a series of independent STROMABrokerRequests and return their responses in order. 
      * 
      * @param url The url to the designated Cyclades Service Engine
      * @param requests The list of STROMARequests to execute
      * @param onFaultStrategy The strategy to use if a fault is encountered...one of NOTHING, RETURN or EXCEPTION
      * @param printResponses Verbose output if true
      * @return A list of STROMAResponses
      * @throws Exception
      */
     public static List<STROMAResponse> executeSTROMARequests (String url, List<STROMARequest> requests, 
             String onFaultStrategy, boolean printResponses) throws Exception {
         List<STROMAResponse> responseList = new ArrayList<STROMAResponse>();
         for (STROMARequest request : requests) {
             STROMAResponse response = new STROMAResponse(new String(
                     Http.execute(new StringBuilder(url).append("/").append(request.getServiceName()).toString(), request)));
             responseList.add(response);
             if (printResponses) printSTROMAResponse(response);
             if (response.getErrorCode() != 0) {
                 switch (OnFaultStrategy.valueOf(onFaultStrategy.toUpperCase())) {
                 case RETURN:
                      return responseList;
                 case EXCEPTION:
                     throw new Exception("Service fault encountered: " + response.getErrorMessage());
                 case NOTHING:
                     break;
                 }
             }
         }
         return responseList;
     }
 
     /**
      * Output the contents of a XSTROMABrokerResponse to stdout
      * 
      * @param stromaResponse The response to output
      * @throws Exception
      */
     public static void printSTROMAResponse (STROMAResponse stromaResponse) throws Exception {
         System.out.println("\n\tService (STROMA) Level Parameters");
         System.out.println("\tservice: " + stromaResponse.getServiceName());
         System.out.println("\taction: " + stromaResponse.getAction());
         System.out.println("\terror-code: " + stromaResponse.getErrorCode());
         System.out.println("\ttransaction-data: " + stromaResponse.getTransactionData());
         System.out.println("\tservice-agent: " + stromaResponse.getServiceAgent());
         System.out.println("\tduration: " + stromaResponse.getDuration());
         System.out.println("\tparameters:" + MapHelper.parameterMapToJSON((stromaResponse.getParameters() != null) ? 
                 stromaResponse.getParameters() : new HashMap<String, List<String>>()));
         if (stromaResponse.getErrorCode() != 0) {
             System.out.println("\terror-message: " + stromaResponse.getErrorMessage());
         }
         // getData() retrieves any raw payload information embedded in the response (as a JSONObject or Node class type)
         // depending on the meta type requested
         //println "\t" + sr.getData().getClass()
     }
     
     /**
      * Upload a resource to one to N nodes/servers, via the "admin" service/Nyxlet
      * 
      * @param urls List of urls to the nodes/servers to run the commands on
      * @param password The password for the target admin services (null OK if no password is set on target nodes)
      * @param sourceResourcePath Path to the resource to upload (can also start with "http:" if a web resource)
      * @param destinationUploadPath Path on the remote nodes to upload the resource
      * @param deleteFirst Delete the resource at destinationUploadPath on the remote nodes if it exists
      * @param onFaultStrategy The strategy to use if a fault is encountered...one of NOTHING, RETURN or EXCEPTION
      * @param printResponses Verbose output if true
      * @throws Exception
      */
     public static void uploadFileToCluster (String[] urls, String password, String sourceResourcePath, 
             String destinationUploadPath, boolean deleteFirst, String onFaultStrategy, 
             boolean printResponses) throws Exception {
         final String eLabel = "DevOps.uploadFileToCluster: ";
         for (String url : urls) {
             try {
                 if (printResponses) System.out.println(eLabel + "Servicing URL: " + url);
                 uploadFile(url, password, sourceResourcePath, destinationUploadPath, deleteFirst);
                 if (printResponses) System.out.println(eLabel + "Success Servicing URL: " + url);
             } catch (Exception e) {
                 if (printResponses) System.out.println(eLabel + "Failed Servicing URL: " + url);
                 switch (OnFaultStrategy.valueOf(onFaultStrategy.toUpperCase())) {
                 case RETURN:
                      return;
                 case EXCEPTION:
                     throw new Exception(eLabel + "Service fault encountered: " + e);
                 case NOTHING:
                     break;
                 }
             }
         }
     }
     
     /**
     * Wrapper as not to break previously named (typo) initial version of this method. Please use correctly named version 
     * instead: "uploadFileToCluster"
      */
     public static void uploadFIleToCluster (String[] urls, String password, String sourceResourcePath, 
             String destinationUploadPath, boolean deleteFirst, String onFaultStrategy, 
             boolean printResponses) throws Exception {
         uploadFileToCluster(urls, password, sourceResourcePath, destinationUploadPath, deleteFirst, onFaultStrategy, 
                 printResponses);
     }
     
     /**
      * Upload a resource to a node/server, via the "admin" service/Nyxlet
      * 
      * @param url the url of the node/server to run the command on
      * @param password The password for the target admin service (null OK if no password is set on target node)
      * @param sourceResourcePath Path to the resource to upload (can also start with "http:" if a web resource)
      * @param destinationUploadPath Path on the remote node to upload the resource
      * @param deleteFirst Delete the resource at destinationUploadPath on the remote node if it exists
      * @throws Exception
      */
     public static void uploadFile (String url, String password, String sourceResourcePath, String destinationUploadPath, 
             boolean deleteFirst) throws Exception {
         InputStream sourceInputStream = null;
         InputStream responseInputStream = null;
         try {
             StringBuilder requestURI = new StringBuilder("/admin?uri=").append(destinationUploadPath);
             if (password != null) requestURI.append("&password=").append(password);
             if (deleteFirst) ResourceRequestUtils.getData(url + requestURI.toString() + "&action=delete", null);
             sourceInputStream = ResourceRequestUtils.getInputStream(sourceResourcePath, null);
             Map<String, String> attributeMap = new HashMap<String, String>();
             attributeMap.put("Content-Type", "");
             HttpURLConnection connection = ResourceRequestUtils.getHttpURLConnection(url + 
                     requestURI.toString() + "&raw-response&action=PUT", "PUT", sourceInputStream, attributeMap, -1, -1);
             responseInputStream = connection.getInputStream();
             String response = new String(StreamUtils.toByteArray(responseInputStream));
             if (connection.getResponseCode() != 200) throw new Exception("Invalid response code returned: " + 
                 connection.getResponseCode() + ": " + response);  
         } finally {
             try { sourceInputStream.close(); } catch (Exception e) {}
             try { responseInputStream.close(); } catch (Exception e) {}
         }
     }
     
     /**
      * Reload the Cyclades Service Engines at the given urls
      * 
      * @param urls List of urls to the nodes/servers to run the commands on
      * @param onFaultStrategy The strategy to use if a fault is encountered...one of NOTHING, RETURN or EXCEPTION
      * @param safetyMode Reload the Cyclades Service Engine with only the "admin" service/Nyxlet loaded
      * @param printResponses Verbose output if true
      * @throws Exception
      */
     public static void reloadServiceEnginesOnCluster (String[] urls, String onFaultStrategy, boolean safetyMode,
             boolean printResponses) throws Exception {
         final String eLabel = "DevOps.reloadServiceEnginesOnCluster: ";
         for (String url : urls) {
             try {
                 if (printResponses) System.out.println(eLabel + "Servicing URL: " + url);
                 reloadServiceEngine(url, safetyMode);
                 if (printResponses) System.out.println(eLabel + "Success Servicing URL: " + url);
             } catch (Exception e) {
                 if (printResponses) System.out.println(eLabel + "Failed Servicing URL: " + url);
                 switch (OnFaultStrategy.valueOf(onFaultStrategy.toUpperCase())) {
                 case RETURN:
                      return;
                 case EXCEPTION:
                     throw new Exception(eLabel + "Service fault encountered: " + e);
                 case NOTHING:
                     break;
                 }
             }
         }
     }
     
     /**
      * Reload the Cyclades Service Engine at the given url
      * 
      * @param url The url to the nodes/servers to run the commands on
      * @param safetyMode Reload the Cyclades Service Engine with only the "admin" service/Nyxlet loaded
      * @throws Exception
      */
     public static void reloadServiceEngine (String url, boolean safetyMode) throws Exception {
         StringBuilder sb = new StringBuilder(url);
         sb.append("?action=reload");
         if (safetyMode) sb.append("&uris=admin");
         String result = new String(ResourceRequestUtils.getData(sb.toString(), null));
         if (safetyMode) {
             if (result.indexOf("admin") < 0) throw new Exception("Invalid Result Encountered " + result);
         } else {
             if (result.indexOf("servicebroker") < 0) throw new Exception("Invalid Result Encountered " + result);
         }
     }
     
 }
