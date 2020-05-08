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
 package org.cyclades.engine.nyxlet.templates.xstroma.target;
 
 import org.cyclades.engine.exception.AuthException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 import java.util.HashMap;
 import org.json.JSONArray;
 import org.json.JSONObject;
 import org.cyclades.engine.nyxlet.templates.xstroma.ServiceBrokerNyxletImpl;
 import org.cyclades.engine.nyxlet.templates.xstroma.message.api.MessageProducer;
 import org.cyclades.engine.util.MapHelper;
 import org.cyclades.engine.NyxletSession;
 
 public class ProducerTarget {
 
     /**
      * Constructor
      *
      * @param authenticationData    Data to use for the auth strategy
      * @param authDataForwarding    Automatically include auth data in requests if true
      * @param theClass              The class to instantiate as a service client (wrapped by this class)
      * @param targetInitData        Initialization data in the form of a JSONObject, straight from the config file
      * @param isLocal               Mark this target as local if true
      * @throws Exception
      */
     public ProducerTarget (String authenticationData, boolean authDataForwarding, String theClass, JSONObject targetInitData, boolean isLocal, ServiceBrokerNyxletImpl service)  throws Exception {
         final String eLabel = "ProducerTarget.ProducerTarget: ";
         try {
             this.authenticationData = authenticationData;
             this.authDataForwarding = authDataForwarding;
             this.theClass = theClass;
             this.isLocal = isLocal;
             if (!isLocal) {
                 messageProducer = (MessageProducer)service.getClass().getClassLoader().loadClass(theClass).newInstance();
                 messageProducer.init(MapHelper.mapFromMetaObject(targetInitData));
             }
         } catch (Exception e) {
             e.printStackTrace();
             throw new Exception(eLabel + e);
         }
     }
     
     /**
     * This constructor is for external use of a ProducerTarget...i.e. client software.
      * 
      * Clients will need to use the ServiceBrokerNyxletImpl implementation of choice as a dependency for this to
      * build...i.e. the "servicebroker" Nyxlet would be used in order to reuse the Producer targets of the existing Nyxlet.
      *
      * @param theClass              The class to instantiate as a service client (wrapped by this class)
      * @param targetInitData        Initialization data in the form of a JSONObject, straight from the config file
      * @throws Exception
      */
     public ProducerTarget (String theClass, JSONObject targetInitData)  throws Exception {
         final String eLabel = "ProducerTarget.ProducerTarget: ";
         try {
             this.authenticationData = null;
             this.authDataForwarding = false;
             this.theClass = theClass;
             messageProducer = (MessageProducer)this.getClass().getClassLoader().loadClass(theClass).newInstance();
             messageProducer.init(MapHelper.mapFromMetaObject(targetInitData));
         } catch (Exception e) {
             e.printStackTrace();
             throw new Exception(eLabel + e);
         }
     }
 
     public void destroy () throws Exception {
         final String eLabel = "ProducerTarget.destroy: ";
         try {
             if (messageProducer != null) messageProducer.destroy();
         } catch (Exception e) {
             throw new Exception(eLabel + e);
         }
     }
 
     public String getAuthenticationData () {
         return authenticationData;
     }
 
     public boolean forwardAuthData () {
         return authDataForwarding;
     }
 
     @SuppressWarnings("unchecked")
     public boolean auth (NyxletSession sessionDelegate) throws Exception {
         final String eLabel = "ProducerTarget.auth: ";
         try {
             if (authenticationData == null) return true;
             // Pass in all the query parameters from the request for auth...remember this is the broker, not a STROMA compliant service..so meta parameters
             // do not apply
             return (sessionDelegate.auth(sessionDelegate.getParameterMap(), authenticationData, false) != null);
         } catch (AuthException e) {
             throw e;
         } catch (Exception e) {
             throw new Exception(eLabel + e);
         }
     }
 
     /**
      * Get a map representation of any auth data we want to send over the wire for any reason. Right now, we will only
      * return one item in the map, the authDelegateObject as a String. This will be under the key "authDelegateObject".
      *
      * @param sessionDelegate
      * @return a map of <String>Lists
      * @throws Exception
      */
     public Map<String, List<String>> getAuthDataMap (NyxletSession sessionDelegate) throws Exception {
         final String eLabel = "ProducerTarget.getAuthDataMap: ";
         try {
             HashMap<String, List<String>> map = new HashMap<String, List<String>>();
             map.put(AUTH_DELEGATE_OBJECT, new ArrayList<String>(Arrays.asList(sessionDelegate.getAuthDelegateObject().toString())));
             return map;
         } catch (Exception e) {
             throw new Exception(eLabel + e);
         }
     }
 
     public MessageProducer getMessageProducer () {
         return messageProducer;
     }
 
     /**
      * Load the service producer targets from list of JSONObjects (each JSONObject is a service producer target)
      * 
      * Each JSONObject entry will look like the following in JSON:
      *
      * {"target":"localhost","local":"true"}
      * 
      * or
      * 
      * {"target":"sample_http","authentication_data":"authenticate","auth_data_forwarding":"false","class":"org.cyclades.nyxlet.servicebrokernyxlet.message.producer.HTTPMessageProducer","target_init_data":{"uri":"http://localhost:8080/cycladesengine/servicebroker"}}
      * 
      * Aliases:
      * 
      * {"target":"localhost","aliases":["sample_alias_1","sample_alias_2"]}
      *
      * @param producerJSONObjectTargets JSONObject list of targets
      * @param producerJSONObjectTargetAliases JSONObject list of target aliases
      * @return map of ProducerTargets
      * @throws Exception
      */
     public static Map loadTargets (List<JSONObject> producerJSONObjectTargets, List<JSONObject> producerJSONObjectTargetAliases, ServiceBrokerNyxletImpl service) throws Exception {
         final String eLabel = "ProducerTarget.loadTargets: ";
         Map<String, ProducerTarget> targetsMap = null;
         try {
             targetsMap = new HashMap<String, ProducerTarget>();
             String targetauthenticationData;
             boolean isLocal;
             String className;
             JSONObject targetInitJSONObject;
             boolean forwardUserData;
             // Load producer targets
             for (JSONObject target : producerJSONObjectTargets) {
                 targetauthenticationData = (target.has(AUTHENTICATION_DATA)) ?  target.getString(AUTHENTICATION_DATA) : null;
                 if (target.has(LOCAL) && target.getString(LOCAL).equalsIgnoreCase("true")) {
                     isLocal = true;
                     className = null;
                     targetInitJSONObject = null;
                     forwardUserData = false;
                 } else {
                     isLocal = false;
                     className = target.getString(CLASS);
                     targetInitJSONObject = target.getJSONObject(TARGET_INITIALIZATION_DATA);
                     forwardUserData = (target.has(AUTH_DATA_FORWARDING)) ?  target.getString(AUTH_DATA_FORWARDING).equalsIgnoreCase("true") : false;
                 }
                 targetsMap.put(target.getString("target"), new ProducerTarget(targetauthenticationData, forwardUserData, className, targetInitJSONObject, isLocal, service));
             }
             // Load producer target aliases
             String targetName;
             JSONArray targetAliasArray;
             for (JSONObject targetAlias : producerJSONObjectTargetAliases) {
                 targetName = targetAlias.getString("target");
                 targetAliasArray = targetAlias.getJSONArray("aliases");
                 if (!targetsMap.containsKey(targetName)) throw new Exception("Target does not exist: " + targetName);
                 for (int j = 0; j < targetAliasArray.length(); j++) {
                     targetsMap.put(targetAliasArray.getString(j), targetsMap.get(targetName));
                 }
             }
             return targetsMap;
         } catch (Exception e) {
             if (targetsMap != null) {
                 for (Map.Entry<String, ProducerTarget> entry : targetsMap.entrySet()) {
                     try {entry.getValue().destroy();} catch (Exception ex) {}
                 }
             }
             throw new Exception(eLabel + e);
         }
     }
 
     public String toString () {
         return theClass;
     }
 
     public boolean isLocal () {
         return isLocal;
     }
 
     public boolean isHealthy () throws Exception {
         return ((isLocal) ? true : messageProducer.isHealthy());
     }
 
     private final String authenticationData;
     private final boolean authDataForwarding;
     private MessageProducer messageProducer;
     private final String theClass;
     boolean isLocal;
     private static final String AUTHENTICATION_DATA         = "authentication_data";
     private static final String AUTH_DATA_FORWARDING        = "auth_data_forwarding";
     private static final String AUTH_DELEGATE_OBJECT        = "authDelegateObject";
     private static final String CLASS                       = "class";
     private static final String TARGET_INITIALIZATION_DATA  = "target_init_data";
     private static final String LOCAL                       = "local";
     
 }
