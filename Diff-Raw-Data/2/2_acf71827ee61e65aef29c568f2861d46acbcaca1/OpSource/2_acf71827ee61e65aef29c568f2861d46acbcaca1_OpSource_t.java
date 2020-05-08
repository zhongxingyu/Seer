 /**
  * Copyright (C) 2009-2012 enStratus Networks Inc
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
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.TransformerFactoryConfigurationError;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 
 import org.apache.log4j.Logger;
 import org.dasein.cloud.AbstractCloud;
 import org.dasein.cloud.CloudErrorType;
 import org.dasein.cloud.CloudException;
 import org.dasein.cloud.InternalException;
 import org.dasein.cloud.ProviderContext;
 import org.dasein.cloud.compute.MachineImage;
 import org.dasein.cloud.dc.Region;
 import org.dasein.cloud.network.VLAN;
 import org.dasein.cloud.network.VLANSupport;
 import org.dasein.cloud.opsource.compute.OpSourceComputeServices;
 import org.dasein.cloud.opsource.network.OpSourceNetworkServices;
 import org.w3c.dom.Document;
 
 import org.w3c.dom.NodeList;
 
 
 public class OpSource extends AbstractCloud {
 	
 	public OpSource(){}
 
     static private final Lock lock = new ReentrantLock();
 	
 	/** Request URL path */
 	static public final String IMAGE_BASE_PATH             		= "image";
 	static public final String SERVER_BASE_PATH             	= "server";
 	static public final String NETWORK_BASE_PATH            	= "network";
 	static public final String LOCATION_BASE_PATH             	= "datacenterWithLimits";
 	/** Response Tag */
 	static public final String RESPONSE_RESULT_TAG             	= "result";
 	static public final String RESPONSE_RESULT_DETAIL_TAG       = "result";
 	/** Response value */
 	static public final String RESPONSE_RESULT_ERROR_VALUE       = "ERROR";
 	static public final String RESPONSE_RESULT_SUCCESS_VALUE     = "SUCCESS";
 	
 	static final public String HTTP_Method_Key = "HTTPMethod";
 	static final public String HTTP_Post_Body_Key = "PostKey";
 	
 	static final public String Content_Type_Key = "Content-Type";
 	static final public String Content_Type_Value_Single_Para = "application/xml";
 	static final public String Content_Type_Value_Modify = "application/x-www-form-urlencoded"; //Post
 	
 	/** Cloud Name */
 	final String OpSource_OpSource_Name = "OpSource Cloud";
 	final String OpSource_Dimension_Name = "Dimension Data Cloud";
 	final String OpSource_BlueFire_Name = "BlueFire Cloud";
 	final String OpSource_NTTA_Name = "NTTA Cloud";
 	final String OpSource_NTTE_Name = "NTTE Cloud";
 	final String OpSource_Tenzing_Name = "Tenzing Everest Cloud";
 	final String OpSource_Alvea_Name = "Alvea IAAS";
 	final String OpSource_RootAxcess_Name = "RootAxcess Cloud";
 	final String OpSource_OPTiMO_Name = "OPTiMO Cloud Solutions";
 	final String OpSource_PWW_Name = "PWW Cloud Connect";
 	
 	final String OpSource_OrgId_Key = "orgId";
 	final String OpSource_VERSION = "/oec/0.9";
 		
 	private String orgId = null;
 	public String defaultVlanId = null;
 	private String defaultRegionId = null;
 	private String defaultAdminPasswordForVM = null;
 	
 	public String buildUrl(String command, boolean isDeployed, Map<Integer, Param> parameters) throws InternalException, CloudException {
 		StringBuilder str = new StringBuilder();
 
         String endpoint = getEndpoint();
         str.append(endpoint);
         if(endpoint != null && !endpoint.contains("oec/0.9")){
         	if(endpoint.endsWith("/")){
         		str.append("oec/0.9");
         	}else{
         		str.append("/oec/0.9");
         	}        	
         }
         if(isDeployed){
         	str.append("/"+ getOrgId());
         }else{
         	str.append("/"+ "base");
         }
         for(int i=0 ; i<parameters.size(); i++ ){
         	Param param = parameters.get(i);
         	String value = param.getValue();
         	if(value==null){
         		str.append("/"+param.getKey());	            		
         	}
         }
         
         if(command !=null ){
         	 str.append("?");
         	 str.append(command);
         }
         
         boolean firstPara = true;
         
         for(int i=0 ; i<parameters.size(); i++ ){
         	Param param = parameters.get(i);
         	String value = param.getValue();
         	
         	if(value!=null){
         		if(firstPara && command ==null  ){   //&& command ==null 
         			str.append("?");
         		    str.append(param.getKey());
                     str.append("=");
                     str.append(param.getValue());
                     firstPara = false;
         		}else{
     			   str.append("&");
                    str.append(param.getKey());
                    str.append("=");
                    str.append(param.getValue());
         		}             
         	}        	
         }
      
         return str.toString();        
     }
 	
 	public Document createDoc() throws InternalException{
 		try {
 			return DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
 		} catch (ParserConfigurationException e) {
 			throw new InternalException(e);
 		}		
 	}
 	
 	/**
 	 *  Convert a xml document to string
 	 */
 	public String convertDomToString( Document doc)  throws CloudException, InternalException{
 		try {
 			 if(doc == null) return null;
 			 StringWriter stw = new StringWriter();
 	         Transformer serializer = TransformerFactory.newInstance().newTransformer();
 	         serializer.transform(new DOMSource(doc), new StreamResult(stw));
 	         
 	         return stw.toString();
 	        
 		} catch (TransformerConfigurationException e) {			
 			throw new InternalException(e);
 		} catch (TransformerFactoryConfigurationError e) {
 			throw new InternalException(e);
 		} catch (TransformerException e) {
 			throw new InternalException(e);
 		}
 	}
 	
 	public String getVlanResourcePathFromVlanId(@Nonnull String vlanId) throws InternalException, CloudException{
 		return "/oec/"+ getOrgId()+"/network/" + vlanId;		
 	}
 	
 	/**
 	 * Get vlanResourcePath
 	 */	
 	public String getVlanIdFromVlanResourcePath(@Nonnull String vlanResourcePath) throws InternalException, CloudException{
 		
 		return  vlanResourcePath.substring(vlanResourcePath.lastIndexOf("/")+1);
 	}
 	public String getImageIdFromImageResourcePath(@Nonnull String imageResourcePath) throws InternalException, CloudException{
 		
 		return  imageResourcePath.substring(imageResourcePath.lastIndexOf("/")+1);
 	}
 	public String getImageResourcePathFromImaged(@Nonnull String imageId) throws InternalException, CloudException{
 		MachineImage image = this.getComputeServices().getImageSupport().getMachineImage(imageId);
 		if(image == null){
 			throw new CloudException ("No such image");
 		}
 		if(image.getProviderOwnerId() == this.getContext().getAccountNumber()){
 			return "/oec/"+ getOraginzationId()+"/image/"+ imageId;	
 		}else{
 			return "/oec/base/image/"+ imageId;	
 		}		
 	}
 	
 	//public MachineImage  getTargetImage(int cpuNum, int memoryInMb){
 		
 	//}
 	
 	/**
 	 * OpSource require to enter a password for VM 
 	 * In dasein implementation the default password would
 	 * be set as the private key or private key plus public key
 	 * if the length of the private key is less than 8
 	 */
 	public String getDefaultAdminPasswordForVM(){
 		//private + public
 		if(defaultAdminPasswordForVM  == null){
 			defaultAdminPasswordForVM = new String (getContext().getAccessPrivate());
         	if(defaultAdminPasswordForVM.length() < 8){        		
         		defaultAdminPasswordForVM += new String (getContext().getAccessPublic());        	}
 		}
 		return defaultAdminPasswordForVM;
 		
 	}
 	public String getDefaultRegionId() throws InternalException, CloudException{
 		if(defaultRegionId == null){
 			defaultRegionId = getContext().getRegionId();		
 		}
 		if(defaultRegionId == null){			
 			ArrayList<Region> list =  (ArrayList<Region>) getDataCenterServices().listRegions();
 			defaultRegionId = list.get(0).getProviderRegionId();
 		}
 		return defaultRegionId;
 		
 	}
 	public String getDefaultVlanId() throws CloudException, InternalException{
         synchronized (lock) {
             if (defaultVlanId != null) {
                 try {
                     getNetworkServices().getVlanSupport().getVlan(defaultVlanId);
                     return defaultVlanId;
                 } catch (Exception ignored) {
                     defaultVlanId = null;
                 }
             }
             VLANSupport vlanSupport = getNetworkServices().getVlanSupport();
             ArrayList<VLAN> lists = (ArrayList<VLAN>) vlanSupport.listVlans();
             for (VLAN vlan : lists) {
                 String vlanRegionId = vlan.getProviderRegionId();
                 if (getDefaultRegionId() != null && getDefaultRegionId().equals(vlanRegionId)) {
                     defaultVlanId = vlan.getProviderVlanId();
                     break;
                 }
             }
 
             if (defaultVlanId == null) {
                 //Create a default VLan
                 VLAN vlan = vlanSupport.createVlan("defaultVlan", "Default Network", null, null, null, null);
                 defaultVlanId = vlan.getProviderVlanId();
             }
             return defaultVlanId;
         }
     }
 
 	public Map<String,String> getBasicRequestParameters(String contentType, String requestMethod, String requestBody){
 		HashMap<String,String> parameters = new HashMap<String,String>();
 		
 		parameters.put(Content_Type_Key, contentType);
 		parameters.put(HTTP_Method_Key, requestMethod);
 		if(requestBody != null){
 			parameters.put(OpSource.HTTP_Post_Body_Key, requestBody);			
 		}
 		return parameters;
 	}
 	
 	//Return the request url for data and region services
 	public String getRegionServiceUrl() throws CloudException, InternalException{
 		String orgIdUrl = getOrgUrl();
 		String requestPara = "datacenterWithLimits";
 		return orgIdUrl+ requestPara;		
 	}
 	
 	//Return the request url for data and region services
 	public String getOrgUrl() throws CloudException, InternalException{
 		String basicUrl = this.getBasicUrl();
 		String orgId = this.getOrgId();
 		
 		if(basicUrl != null && orgId != null ){
 			return basicUrl+ "/" + orgId + "/";
 		}else{
 			throw new CloudException("Wrong endpoint");
 		}	
 	}
 	
 	//Return the request url for data and region services
 	public String getServerImageUrl() throws CloudException, InternalException{
 		String basicUrl = this.getBasicUrl();
 		String requestPara = "/base/image";
 		if(basicUrl != null ){
 			return basicUrl+ requestPara;
 		}else{
 			throw new CloudException("Wrong endpoint");
 		}	
 	}
 	
 	public String getEndpoint(){
 
         String endpoint = getContext().getEndpoint();
         if(endpoint == null){
             return null;
         }
         else{
             String t = endpoint.toLowerCase();
             if(!(t.startsWith("http://") 
                     || t.startsWith("https://")
                     || t.matches("^[a-z]+://.*"))){
             	endpoint = "https://" + endpoint;
             }
         }
         return endpoint;
 	}
 	
 	public URL getEndpointURL() throws CloudException{
 		try {
 			return new URL(getEndpoint());
 		} catch (MalformedURLException e) {
 			throw new CloudException("Wrong endpoint");
 		}
 		
 	}
 	
 	
 	public String getOrgId() throws InternalException,CloudException{
 		if(orgId == null){
 			String url = "https://api.opsourcecloud.net/oec/0.9/myaccount";
 			HashMap<String,String> parameters = new HashMap<String,String>();
 			
 			parameters.put(Content_Type_Key, Content_Type_Value_Single_Para);
 			parameters.put(HTTP_Method_Key, "GET");
 		
 			OpSourceMethod method = new OpSourceMethod(this, url, parameters);
 			Document doc = method.invoke();
             String sNS = "";
             try{
                 sNS = doc.getDocumentElement().getTagName().substring(0, doc.getDocumentElement().getTagName().indexOf(":") + 1);
             }
             catch(IndexOutOfBoundsException ex){}
 			NodeList blocks = doc.getElementsByTagName(sNS + "orgId");
 			if(blocks != null){
 				orgId = blocks.item(0).getFirstChild().getNodeValue();
 			
 		    }else{
 		    	throw new CloudException("Can not load orgId information!!!");
 		    }		
 		}
 		return orgId;		
 	}
 	
  	static private @Nonnull String getLastItem(@Nonnull String name) {
         int idx = name.lastIndexOf('.');
         
         if( idx < 0 ) {
             return name;
         }
         else if( idx == (name.length()-1) ) {
             return "";
         }
         return name.substring(idx+1);
     }
 
    static public @Nonnull Logger getLogger(@Nonnull Class<?> cls, String context) {
        String pkg = getLastItem(cls.getPackage().getName());
         
        return Logger.getLogger("dasein.cloud.opsource." + context + "." + pkg + "." + getLastItem(cls.getName()));
    }
    
    public String getBasicUrl() throws CloudException{
	   String endpoint = getEndpoint();
 	   if(endpoint == null){
 		  throw new CloudException("Endpoint is null !!!");
 	   }
 	   return (endpoint  + OpSource_VERSION);
    }	
    
    public HashMap<String, ArrayList<String>> getProivderEndpointMap(){
 		
 		HashMap<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();
 		String providerName = OpSource_OpSource_Name;
 		ArrayList<String> endpointList = new ArrayList<String>();
 		endpointList.add("api.opsourcecloud.net");
 		endpointList.add("euapi.opsourcecloud.net");
 		endpointList.add("auapi.opsourcecloud.net");
 		map.put(providerName, endpointList);
 		
 		providerName = OpSource_Dimension_Name;
 		endpointList = new ArrayList<String>();
 		endpointList.add("api-na.dimensiondata.com");
 		endpointList.add("api-eu.dimensiondata.com");
 		endpointList.add("api-au.dimensiondata.com");
 		map.put(providerName, endpointList);
 		
 		providerName = OpSource_BlueFire_Name;
 		endpointList = new ArrayList<String>();
 		endpointList.add("usapi.bluefirecloud.com.au");
 		endpointList.add("euapi.bluefirecloud.com.au");	
 		endpointList.add("auapi.bluefirecloud.com.au");
 		map.put(providerName, endpointList);
 		
 		providerName = OpSource_NTTA_Name;
 		endpointList = new ArrayList<String>();
 		endpointList.add("cloudapi.nttamerica.com");
 		endpointList.add("eucloudapi.nttamerica.com");
 		endpointList.add("aucloudapi.nttamerica.com");
 		map.put(providerName, endpointList);
 		
 		providerName = OpSource_NTTE_Name;
 		endpointList = new ArrayList<String>();
 		endpointList.add("ntteapi.opsourcecloud.net");
 		//The same as OpSource cloud?
 		endpointList.add("euapi.opsourcecloud.net");
 		//The same as OpSource cloud?
 		endpointList.add("auapi.opsourcecloud.net");		
 		map.put(providerName, endpointList);
 		
 		providerName = OpSource_Tenzing_Name;
 		endpointList = new ArrayList<String>();
 		endpointList.add("api.cloud.tenzing.com");		
 		map.put(providerName, endpointList);
 		
 		providerName = OpSource_Tenzing_Name;
 		endpointList = new ArrayList<String>();
 		endpointList.add("api.cloud.tenzing.com");		
 		map.put(providerName, endpointList);
 				
 		providerName = OpSource_Alvea_Name;
 		endpointList = new ArrayList<String>();
 		endpointList.add("iaasapi.alvea-services.com");		
 		map.put(providerName, endpointList);
 		
 		providerName = OpSource_Alvea_Name;
 		endpointList = new ArrayList<String>();
 		endpointList.add("iaasapi.alvea-services.com");		
 		map.put(providerName, endpointList);
 		
 		providerName = OpSource_RootAxcess_Name;
 		endpointList = new ArrayList<String>();
 		endpointList.add("iaasapi.alvea-services.com");		
 		map.put(providerName, endpointList);
 		
 		providerName = OpSource_OPTiMO_Name;
 		endpointList = new ArrayList<String>();
 		endpointList.add("api.optimo-cloud.com");		
 		map.put(providerName, endpointList);
 		
 		providerName = OpSource_PWW_Name;
 		endpointList = new ArrayList<String>();
 		endpointList.add("api.pwwcloudconnect.net");		
 		map.put(providerName, endpointList);	
 		
 		return map;	
    }	
 	
 	@Override
 	public String getCloudName() {
        ProviderContext ctx = getContext();
 
         if( ctx == null ) {
             return "OpSource";
         }
         String name = ctx.getProviderName();
         
         if( name == null ) {
             return "OpSource";
         }
         return name;
 	}
 
 	@Override
 	public String getProviderName() {
        ProviderContext ctx = getContext();
 
         if( ctx == null ) {
             return "OpSource";
         }
         String name = ctx.getProviderName();
         
         if( name == null ) {
             return "OpSource";
         }
         return name;
 	}
 		
 	public String getOraginzationId(){
 		return orgId;
 	}
 		
 	@Override
     public @Nonnull OpSourceComputeServices getComputeServices() {
         return new OpSourceComputeServices(this);
     }
     
     @Override
     public @Nonnull OpSourceLocation getDataCenterServices() {
         return new OpSourceLocation(this);
     }
     
     public String getDataCenterId(String regionId){
     	return regionId;
     }
     
     @Override
     public @Nonnull OpSourceNetworkServices getNetworkServices() {
         return new OpSourceNetworkServices(this);
     }
     
 	/**
 	 * Check if the vlan is created within the region, called in toVirtualMachine();
 	*/	
 	public boolean isVlanInRegion(String vlanId) throws CloudException, InternalException{
 
 		VLAN vlan = this.getNetworkServices().getVlanSupport().getVlan(vlanId);
 		if(vlan != null){
 			return true;
 		}
 		return false;	
 	}
 
     @Override
     public @Nullable String testContext() {
         Logger logger = getLogger(OpSource.class, "std");
         
         if( logger.isTraceEnabled() ) {
             logger.trace("enter - " + OpSource.class.getName() + ".textContext()");
         }
         try {
             try {
                 ProviderContext ctx = getContext();
 
                 if( ctx == null ) {
                     return null;
                 }
                 String pk = new String(ctx.getAccessPublic(), "utf-8");
 
         		OpSourceMethod method = new OpSourceMethod(this, getRegionServiceUrl(), getBasicRequestParameters(OpSource.Content_Type_Value_Single_Para, "GET", null));
                 
                 try {
             		Document doc = method.invoke();
             		if( logger.isDebugEnabled()) {
             			logger.debug("Found regions: "+ convertDomToString(doc));
             		}
                     return pk;
                 }
                 catch( CloudException e ) {
                     if( e.getErrorType().equals(CloudErrorType.AUTHENTICATION) ) {
                         return null;
                     }
                     logger.warn("Cloud error testing OpSource context: " + e.getMessage());
                     if( logger.isTraceEnabled() ) {
                         e.printStackTrace();
                     }
                 }
                 return null;
             }
             catch( Throwable t ) {
                 logger.warn("Failed to test OpSource connection context: " + t.getMessage());
                 if( logger.isTraceEnabled() ) {
                     t.printStackTrace();
                 }
                 return null;
             }
         }
         finally {
             if( logger.isTraceEnabled() ) {
                 logger.trace("exit - " + OpSource.class.getName() + ".testContext()");
             }
         }
     }
 }
