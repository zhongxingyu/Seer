 /**
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  *
  * Copyright (c) 2006 Sun Microsystems Inc. All Rights Reserved
  *
  * The contents of this file are subject to the terms
  * of the Common Development and Distribution License
  * (the License). You may not use this file except in
  * compliance with the License.
  *
  * You can obtain a copy of the License at
  * https://opensso.dev.java.net/public/CDDLv1.0.html or
  * opensso/legal/CDDLv1.0.txt
  * See the License for the specific language governing
  * permission and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL
  * Header Notice in each file and include the License file
  * at opensso/legal/CDDLv1.0.txt.
  * If applicable, add the following below the CDDL Header,
  * with the fields enclosed by brackets [] replaced by
  * your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  *
  *
  */
 
 package com.sun.identity.install.tools.configurator;
 
 import java.util.Map;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.InetAddress;
 import java.lang.Exception;
 import java.net.UnknownHostException;
 import java.net.ConnectException;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.StringTokenizer;
 
 import com.sun.identity.install.tools.util.Debug;
 import com.sun.identity.install.tools.util.LocalizedMessage;
 
 public class ValidateURL extends ValidatorBase {
 
     public ValidateURL() throws InstallException {
         super();
     }
 
     /*
      * Checks if server url is valid
      * 
      * @param url @param props @param state
      * 
      * @return ValidationResult
      */
     public ValidationResult isServerURLValid(String url, Map props,
             IStateAccess state) {
            
         // delegate this function to its proxy with timeout.
         return new URLValidatorProxy(url, props, state).isServerURLValid();
     }
     
     /*
      * Checks if server url is valid
      * 
      * @param url @param props @param state
      * 
      * @return ValidationResult
      */
     private ValidationResult isServerURLValidIntenal(String url, Map props,
             IStateAccess state) {
 
         LocalizedMessage returnMessage = null;
         ValidationResultStatus validRes = ValidationResultStatus.STATUS_FAILED;
         boolean invalidDeploymentURI = false;
 	
         try {
 	    URL serverUrl = new URL(url);
             String protocol = serverUrl.getProtocol();
             String hostName = serverUrl.getHost();
             int portNum = serverUrl.getPort();           
             if (portNum == -1) {
                 if (protocol.equals("http")) {
                     portNum = 80;
                 } else if (protocol.equals("https")) {
                     portNum = 443;
                 }
             }
             String sPortNum = new Integer(portNum).toString();
             String deploymentURI = serverUrl.getPath();
             if (deploymentURI.length() > 0) {
                 Map tokens = state.getData();
                 
                 tokens.put("AM_SERVICES_PROTO", protocol);
                 tokens.put("AM_SERVICES_HOST", hostName);
                 tokens.put("AM_SERVICES_PORT", sPortNum);
                 tokens.put("AM_SERVICES_DEPLOY_URI", deploymentURI);
 
                 state.putData(tokens);               
 		// Establish the connection
 	        URLConnection urlConnect = serverUrl.openConnection();
 	        urlConnect.connect();
                 returnMessage = LocalizedMessage.get(LOC_VA_MSG_VAL_SERVER_URL,
                             new Object[] { url });
                 state.put("isServerURLValid", "true");
                 validRes = ValidationResultStatus.STATUS_SUCCESS;
             } else {
                 invalidDeploymentURI = true;
                 validRes = ValidationResultStatus.STATUS_FAILED;
                 returnMessage = LocalizedMessage.get(
                                 LOC_VA_MSG_IN_VAL_DEPLOYMENT_URI,
                                 new Object[] { url });
             }    
         } catch (UnknownHostException uhe) {
                 Debug.log("ValidateURL.isServerUrlValid threw exception :",
                                 uhe);
                 returnMessage = LocalizedMessage.get(
                                 LOC_VA_WRN_UN_REACHABLE_SERVER_URL,
                                 new Object[] { url });
                 state.put("isServerURLValid", "false");
 	        validRes = ValidationResultStatus.STATUS_WARNING;
         } catch (ConnectException ce) {
                 Debug.log("ValidateURL.isServerUrlValid threw exception :",
                                 ce);
                 returnMessage = LocalizedMessage.get(
                                 LOC_VA_WRN_UN_REACHABLE_SERVER_URL,
                                 new Object[] { url });
                 state.put("isServerURLValid", "false");
 	        validRes = ValidationResultStatus.STATUS_WARNING;               
         } catch (Exception e) {
             if (url.toLowerCase().startsWith("https")) {
                 Debug.log("ValidateURL.isServerUrlValid threw exception :",
                         e);
                 returnMessage = LocalizedMessage.get(
                         LOC_VA_WRN_UN_REACHABLE_SSL_SERVER_URL,
                         new Object[] { url });
                 state.put("isServerURLValid", "false");
                 validRes = ValidationResultStatus.STATUS_WARNING;
             } else {
                 Debug.log("ValidateURL.isServerUrlValid threw exception :",
                         e);
             } 	 
        }
 	 if (validRes.getIntValue() == ValidationResultStatus.INT_STATUS_FAILED)
          {
              if (!invalidDeploymentURI) {
                 returnMessage = LocalizedMessage.get(
                                 LOC_VA_WRN_IN_VAL_SERVER_URL,
                                 new Object[] { url });
              }
          }
         
          return new ValidationResult(validRes, null, returnMessage);
     }
 
     /*
      * Checks if agent url is valid
      * 
      * @param url @param props @param state
      * 
      * @return ValidationResult
      */
     public ValidationResult isAgentURLValid(String url, Map props,
             IStateAccess state) {
 
         ValidationResultStatus validRes = ValidationResultStatus.STATUS_FAILED;
         LocalizedMessage returnMessage = null;
         boolean invalidDeploymentURI = false;
 
         try {
 	    URL agentUrl = new URL(url);
             returnMessage = LocalizedMessage.get(LOC_VA_MSG_VAL_AGENT_URL,
                             new Object[] { url });
             String protocol = agentUrl.getProtocol();
             String hostName = agentUrl.getHost();
             int portNum = agentUrl.getPort();
             if (portNum == -1) {
                 if (protocol.equals("http")) {
                     portNum = 80;
                 } else if (protocol.equals("https")) {
                     portNum = 443;
                 }
             }
             String sPortNum = new Integer(portNum).toString();           
             Map tokens = state.getData();
                 
             tokens.put("AGENT_PREF_PROTO", protocol);
             tokens.put("AGENT_HOST", hostName);
             tokens.put("AGENT_PREF_PORT", sPortNum);     
             
 	    String agentType = (String) props.get(STR_AGENT_TYPE);
 	    if (agentType != null && agentType.equals("webagent")) {
                 state.putData(tokens);
                 validRes = ValidationResultStatus.STATUS_SUCCESS;
 	    } else {
                 String deploymentURI = agentUrl.getPath();           
                 Debug.log("deploymentURI ==> " + deploymentURI);
                 if (deploymentURI.length() > 1) {
                     tokens.put("AGENT_APP_URI", deploymentURI);
                 
                     state.putData(tokens);
                     validRes = ValidationResultStatus.STATUS_SUCCESS;
                 } else {
                     invalidDeploymentURI = true;
                     validRes = ValidationResultStatus.STATUS_FAILED;
                     returnMessage = LocalizedMessage.get(
                                     LOC_VA_MSG_IN_VAL_DEPLOYMENT_URI,
                                     new Object[] { url });
                 }
             }
          } catch (MalformedURLException mfe) {
                 Debug.log("ValidateURL.isAgentUrlValid threw exception :",
                                 mfe);
          }
         
          if (validRes.getIntValue() == ValidationResultStatus.INT_STATUS_FAILED)
 	 {
              if (!invalidDeploymentURI) {
                  returnMessage = LocalizedMessage.get(
                                  LOC_VA_WRN_IN_VAL_AGENT_URL,
                                  new Object[] { url });
              }
 	 }
          return new ValidationResult(validRes, null, returnMessage);
     }
 
     public void initializeValidatorMap() throws InstallException {
 
         Class[] paramObjs = { String.class, Map.class, IStateAccess.class };
 
         try {
             getValidatorMap().put("VALID_SERVER_URL",
                     this.getClass().getMethod("isServerURLValid", paramObjs));
 
             getValidatorMap().put("VALID_AGENT_URL",
                     this.getClass().getMethod("isAgentURLValid", paramObjs));
 
         } catch (NoSuchMethodException nsme) {
             Debug.log("ValidateURL: "
                     + "NoSuchMethodException thrown while loading method :",
                     nsme);
             throw new InstallException(LocalizedMessage
                     .get(LOC_VA_ERR_VAL_METHOD_NOT_FOUND), nsme);
         } catch (SecurityException se) {
             Debug.log("ValidateURL: "
                     + "SecurityException thrown while loading method :", se);
             throw new InstallException(LocalizedMessage
                     .get(LOC_VA_ERR_VAL_METHOD_NOT_FOUND), se);
         } catch (Exception ex) {
             Debug.log("ValidateURL: "
                     + "Exception thrown while loading method :", ex);
             throw new InstallException(LocalizedMessage
                     .get(LOC_VA_ERR_VAL_METHOD_NOT_FOUND), ex);
         }
 
     }
 
     /*
      * This is a Runnable class used to validate url with timeout.
      */
     private class URLValidatorProxy implements Runnable {
 
         private String url = null;
         private Map props = null;
         private IStateAccess state = null;
         private ValidationResult result = null;
         
         public URLValidatorProxy(String url, Map props,
                 IStateAccess state) {
             this.url = url;
             this.props = props;
             this.state = state;
             
             ValidationResultStatus validRes = 
                     ValidationResultStatus.STATUS_WARNING;
             LocalizedMessage returnMessage = LocalizedMessage.get(
                                 LOC_VA_WRN_UN_REACHABLE_SERVER_URL,
                                 new Object[] { url });
             this.result = new ValidationResult(validRes, null, returnMessage);
         }
         
         public ValidationResult isServerURLValid() {
             
             try {
                 long timeout = 10000;
                 Thread thread = new Thread(this);
                 thread.start();
                 thread.join(timeout);
                 
                 if (thread.isAlive()) {
                     thread.interrupt();
                 }
                 
             } catch (InterruptedException ex) {
                 Debug.log("ValidateURL$URLValidatorProxy.isServerURLValid(): " +
                         "the url " + url + " is not available", ex);
             }
             
             return this.result;
         }
         
         public void run() {
             result = ValidateURL.this.isServerURLValidIntenal(url, props,
                     state);
         }
         
     } // end of URLValidatorProxy class
     
     public static String STR_AGENT_TYPE = "AGENT_TYPE";
     /*
      * Localized messages
      */
     public static String LOC_VA_MSG_VAL_SERVER_URL = "VA_MSG_VAL_SERVER_URL";
     public static String LOC_VA_WRN_IN_VAL_SERVER_URL = 
                                             "VA_WRN_IN_VAL_SERVER_URL";
     public static String LOC_VA_WRN_UN_REACHABLE_SERVER_URL = 
                                             "VA_WRN_UN_REACHABLE_SERVER_URL";
    public static String LOC_VA_WRN_UN_REACHABLE_SSL_SERVER_URL =
            "VA_WRN_UN_REACHABLE_SSL_SERVER_URL";
     public static String LOC_VA_MSG_VAL_AGENT_URL = "VA_MSG_VAL_AGENT_URL";
     public static String LOC_VA_WRN_IN_VAL_AGENT_URL= 
                                             "VA_WRN_IN_VAL_AGENT_URL";
     public static String LOC_VA_WRN_UN_REACHABLE_AGENT_URL = 
                                             "VA_WRN_UN_REACHABLE_AGENT_URL";
     public static String LOC_VA_MSG_IN_VAL_DEPLOYMENT_URI  = 
                                             "VA_MSG_IN_VAL_DEPLOYMENT_URI";
 }
