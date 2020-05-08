 /**
  * Copyright (C) 2011-2012 enStratus Networks Inc
  *
  * ====================================================================
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * ====================================================================
  */
 
 package org.dasein.cloud.opsource;
 
 import java.io.StringWriter;
 import java.util.*;
 
 import org.dasein.cloud.CloudException;
 import org.dasein.cloud.InternalException;
 
 import org.dasein.cloud.dc.DataCenter;
 import org.dasein.cloud.dc.DataCenterServices;
 import org.dasein.cloud.dc.Region;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 public class OpSourceLocation implements DataCenterServices {
 	
 	private OpSource provider = null;
 	OpSourceLocation(OpSource provider) {
 		this.provider = provider;
 	}
 
 	@Override
 	public DataCenter getDataCenter(String dataCenterId) throws InternalException,CloudException {
 		ArrayList<Region> regions = (ArrayList<Region>) listRegions();
 		if(regions == null) return null;
 			
 		for(Region region: regions){
 			ArrayList<DataCenter> dataCenters = (ArrayList<DataCenter>) this.listDataCenters(region.getProviderRegionId());
 			
 			if(dataCenters == null ) continue;			
 			for(DataCenter dc : dataCenters ){
 				if(dc.getProviderDataCenterId().equals(dataCenterId)){
 					return dc;							
 				}						
 			}				
 		}
 		return null;
 		
 	}
 
 	@Override
 	public String getProviderTermForDataCenter(Locale locale) {
 		return "Data Center";
 	}
 
 	@Override
 	public String getProviderTermForRegion(Locale locale) {
 		return "Location";
 	}
 
 	@Override
 	public Region getRegion(String regionId) throws InternalException,CloudException {
 		ArrayList<Region> regions = (ArrayList<Region>) listRegions();
 		if(regions != null){
 			for(Region region: regions){
 				if(regionId.equals(region.getProviderRegionId())){
 					return region;
 				}				
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public Collection<DataCenter> listDataCenters(String regionId)throws InternalException, CloudException {
 		if(regionId == null) return null;
 		Region region = this.getRegion(regionId);
 		if(region == null){
 			throw new CloudException("No such region with regionId -> " + regionId);
 		}
 		
 		ArrayList<DataCenter> list = new ArrayList <DataCenter>();
 		DataCenter dc = new DataCenter();
 		dc.setActive(true);
 		dc.setAvailable(true);
 		dc.setName(region.getName() + " (DC)");
 		dc.setRegionId(regionId);
 		dc.setProviderDataCenterId(regionId);
 		list.add(dc);		
 		return list;
 	}
 
 	@Override
 	public Collection<Region> listRegions() throws InternalException,CloudException {
 		ArrayList<Region> list = new ArrayList <Region>();
 	
         HashMap<Integer, Param>  parameters = new HashMap<Integer, Param>();
         Param param = new Param(OpSource.LOCATION_BASE_PATH, null);
     	parameters.put(0, param);
 
         String cloudName = getCloudNameFromEndpoint();
         if(cloudName == null){
             //Error retrieving cloud from endpoint, use old method
             /*OpSourceMethod method = new OpSourceMethod(provider,
     			provider.buildUrl(null,true, parameters),
     			provider.getBasicRequestParameters(OpSource.Content_Type_Value_Single_Para, "GET",null));
 		    Document doc = method.invoke();*/
             Document doc = CallCache.getInstance().getAPICall(OpSource.LOCATION_BASE_PATH, provider, parameters, "");
             String sNS = "";
             try{
                 sNS = doc.getDocumentElement().getTagName().substring(0, doc.getDocumentElement().getTagName().indexOf(":") + 1);
             }
             catch(IndexOutOfBoundsException ex){}
             NodeList blocks = doc.getElementsByTagName(sNS + "datacenterWithLimits");
             if(blocks != null){
                 for(int i=0; i< blocks.getLength();i++){
                     Node item = blocks.item(i);
                     Region region = toRegion(item, sNS);
                     if(region != null){
                         list.add(region);
                         provider.setRegionEndpoint(region.getProviderRegionId(), provider.getContext().getEndpoint());
                     }
                 }
             }
         }
         else{
             HashMap<String, ArrayList<String>> endpointMap = provider.getProivderEndpointMap();
             ArrayList<String> currentCloudEndpoints = endpointMap.get(cloudName);
             for(String endpoint : currentCloudEndpoints){
                 try{
                     String t = endpoint.toLowerCase();
                     if(!(t.startsWith("http://") || t.startsWith("https://") || t.matches("^[a-z]+://.*"))){
                         endpoint = "https://" + endpoint;
                     }
 
                     OpSourceMethod method = new OpSourceMethod(provider,
                             provider.buildUrlWithEndpoint(endpoint, null,true, parameters),
                             provider.getBasicRequestParameters(OpSource.Content_Type_Value_Single_Para, "GET",null));
                     Document doc = method.invoke();
 
                     //Document doc = CallCache.getInstance().getAPICall(OpSource.LOCATION_BASE_PATH, provider, parameters, "");
                     String sNS = "";
                     try{
                         sNS = doc.getDocumentElement().getTagName().substring(0, doc.getDocumentElement().getTagName().indexOf(":") + 1);
                     }
                     catch(IndexOutOfBoundsException ex){}
                     NodeList blocks = doc.getElementsByTagName(sNS + "datacenterWithLimits");
                     if(blocks != null){
                         for(int i=0; i< blocks.getLength();i++){
                             Node item = blocks.item(i);
                             Region region = toRegion(item, sNS);
                             if(region != null){
                                 list.add(region);
                                 provider.setRegionEndpoint(region.getProviderRegionId(), endpoint);
                             }
                         }
                     }
                 }
                 catch(Exception ex){
                    System.out.println("OpSourceLocation error");
                    ex.printStackTrace();
                     /*
                     If this fails it is likely a 401 authentication error against the endpoint.
                     Rather than getting a nice XML API error response however, OpSource returns the default apache htaccess 401 error
                     so it fails to parse and throws an exception. We're not really interested in this exception as some accounts
                     legitimately don't have access to all the endpoints.
                      */
                 }
             }
         }
         return list;
 	}
 
     public String getCloudNameFromEndpoint(){
         String endpoint = provider.getEndpoint(null);
         endpoint = endpoint.substring(endpoint.indexOf("://") + 3);
         if(endpoint.contains("/oec/0.9/"))endpoint = endpoint.substring(0, endpoint.indexOf("/oec/0.9/"));
 
         HashMap<String, ArrayList<String>> endpointMap = provider.getProivderEndpointMap();
         Set<Map.Entry<String, ArrayList<String>>> endpointList = endpointMap.entrySet();
         Iterator<Map.Entry<String, ArrayList<String>>> it = endpointList.iterator();
         while(it.hasNext()){
             Map.Entry<String, ArrayList<String>> current = it.next();
             ArrayList<String> endpoints = current.getValue();
             for(int i=0;i<endpoints.size();i++){
                 if(endpoints.get(i).equals(endpoint))return current.getKey();
             }
         }
         return null;
     }
 
 	public Region toRegion( Node region, String nameSpace) throws CloudException{
 		if(region == null){
 			return null;
 		}
 		
 		NodeList data;
     	
 		data = region.getChildNodes();
 
         String country = "US";
 		Region r = new Region();
 		for( int i=0; i<data.getLength(); i++ ) {
 			Node item = data.item(i);
 			if(item.getNodeType() == Node.TEXT_NODE) continue;
 			
 			if( item.getNodeName().equals(nameSpace + "location") ) {
 				r.setProviderRegionId(item.getFirstChild().getNodeValue());
 			}
 			else if( item.getNodeName().equals(nameSpace + "displayName") ) {
 				r.setName(item.getFirstChild().getNodeValue());
 			}
             else if(item.getNodeName().equals(nameSpace + "country")){
                 country = item.getFirstChild().getNodeValue();
             }
 		}	
 		r.setActive(true);
 		r.setAvailable(true);
 
         if(country.equals("US")){
             r.setJurisdiction("US");
         }
         else if(country.equals("Australia")){
             r.setJurisdiction("AU");
         }
         else if(country.equals("South Africa")){
             r.setJurisdiction("ZA");
         }
         else{
             //The only one where the country is different
             r.setJurisdiction("EU");
         }
 		return r;
 	}
 }
