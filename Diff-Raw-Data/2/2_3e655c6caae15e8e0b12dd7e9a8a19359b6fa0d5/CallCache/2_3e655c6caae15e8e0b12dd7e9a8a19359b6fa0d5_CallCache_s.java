 package org.dasein.cloud.opsource;
 
 import org.apache.log4j.Logger;
 import org.dasein.cloud.CloudException;
 import org.dasein.cloud.InternalException;
 import org.w3c.dom.Document;
 import org.w3c.dom.NodeList;
 
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import java.io.StringWriter;
 import java.util.HashMap;
 
 /**
  * Copyright (C) 2009-2012 enStratus Networks Inc
  * <p/>
  * ====================================================================
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * <p/>
  * http://www.apache.org/licenses/LICENSE-2.0
  * <p/>
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * ====================================================================
  */
 
 public class CallCache {
     Logger wire = OpSource.getLogger(CallCache.class);
 
     private static CallCache cache;
 
     private HashMap<String, Document> cachedAPICalls;
 
     private String NETWORK_WITH_LOCATION = "networkWithLocation";
     private String DATACENTER_WITH_LIMITS = "datacenterWithLimits";
 
     private long networkWithLocationCacheTime = 0L;
     private long networkWithLocationThreshold;
 
     private long datacenterWithLimitsCacheTime = 0L;
     private long datacenterWithLimitsThreshold;
 
     public static CallCache getInstance(){
         if(cache == null){
             cache = new CallCache();
         }
         return cache;
     }
 
     protected CallCache(){
         cachedAPICalls = new HashMap();
         networkWithLocationThreshold = (60*5*1000);//Timeouts set to five minutes
         datacenterWithLimitsThreshold = (60*5*1000);
     }
 
     private Document getRealAPICall(OpSource provider, HashMap<Integer, Param> parameters, String resource, String regionServiceURL) throws CloudException, InternalException{
        OpSourceMethod method = new OpSourceMethod(provider, resource.equals(NETWORK_WITH_LOCATION) ? provider.buildUrl(null,true, parameters) : regionServiceURL, provider.getBasicRequestParameters(OpSource.Content_Type_Value_Single_Para, "GET",null));
         return method.invoke();
     }
 
     public Document getAPICall(String resource, OpSource provider, HashMap<Integer, Param> parameters, String regionServiceURL) throws CloudException, InternalException{
         boolean timeElapsed = false;
 
         java.util.Date now = new java.util.Date();
         if(resource.equals(NETWORK_WITH_LOCATION)){
             if(now.getTime() > (networkWithLocationCacheTime + networkWithLocationThreshold)){
                 timeElapsed = true;
                 networkWithLocationCacheTime = now.getTime();
             }
         }
         else if (resource.equals(DATACENTER_WITH_LIMITS)){
             if(now.getTime() > (datacenterWithLimitsCacheTime + datacenterWithLimitsThreshold)){
                 timeElapsed = true;
                 datacenterWithLimitsCacheTime = now.getTime();
             }
         }
 
         Document doc;
         if(!cachedAPICalls.containsKey(provider.getContext().getAccountNumber() + "-" + provider.getContext().getRegionId() + "-" + resource) || timeElapsed){
             if(wire.isDebugEnabled()){
                 wire.debug("Getting real OpSource data: " + resource);
             }
             doc = getRealAPICall(provider, parameters, resource, regionServiceURL);
             cachedAPICalls.put(provider.getContext().getAccountNumber() + "-" + provider.getContext().getRegionId() + "-" + resource, doc);
         }
         else{
             if(wire.isDebugEnabled()){
                 wire.debug("Getting OpSource data from cache: " + provider.getContext().getAccountNumber() + "-" + provider.getContext().getRegionId() + "-" + resource);
             }
             doc = cachedAPICalls.get(provider.getContext().getAccountNumber() + "-" + provider.getContext().getRegionId() + "-" + resource);
         }
 
         if(wire.isDebugEnabled()){
             try{
                 TransformerFactory transfac = TransformerFactory.newInstance();
                 Transformer trans = transfac.newTransformer();
                 trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                 trans.setOutputProperty(OutputKeys.INDENT, "yes");
 
                 StringWriter sw = new StringWriter();
                 StreamResult result = new StreamResult(sw);
                 DOMSource source = new DOMSource(doc);
                 trans.transform(source, result);
                 String xmlString = sw.toString();
                 System.out.println(xmlString);
             }
             catch(Exception ex){
                 ex.printStackTrace();
             }
         }
 
         return doc;
     }
 
     public void resetCacheTimer(String resource){
 
         System.out.println("Reset cache timer for: " + resource);
 
         if(resource.equals(NETWORK_WITH_LOCATION)){
             networkWithLocationCacheTime = 0L;
         }
         else if(resource.equals(DATACENTER_WITH_LIMITS)){
             datacenterWithLimitsCacheTime = 0L;
         }
     }
 }
