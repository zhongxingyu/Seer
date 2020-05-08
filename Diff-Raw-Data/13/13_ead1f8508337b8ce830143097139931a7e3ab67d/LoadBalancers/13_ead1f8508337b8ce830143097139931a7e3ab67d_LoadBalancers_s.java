 /**
  * Copyright (C) 2009-2013 Dell, Inc.
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
 
 package org.dasein.cloud.opsource.network;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 
 
 import javax.annotation.Nonnull;
 
 import org.apache.log4j.Logger;
 import org.dasein.cloud.CloudException;
 import org.dasein.cloud.InternalException;
 import org.dasein.cloud.OperationNotSupportedException;
 
 import org.dasein.cloud.ResourceStatus;
 import org.dasein.cloud.compute.VirtualMachine;
 import org.dasein.cloud.identity.ServiceAction;
 import org.dasein.cloud.network.*;
 import org.dasein.cloud.opsource.OpSource;
 import org.dasein.cloud.opsource.OpSourceMethod;
 import org.dasein.cloud.opsource.Param;
 import org.dasein.cloud.util.APITrace;
 import org.dasein.util.JitCollection;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 public class LoadBalancers extends AbstractLoadBalancerSupport<OpSource> {
 	static public final String ASSIGN_TO_LOAD_BALANCER_RULE       = "assignToLoadBalancerRule";
 	static public final String CREATE_LOAD_BALANCER_RULE          = "createLoadBalancerRule";
 	static public final String DELETE_LOAD_BALANCER_RULE          = "deleteLoadBalancerRule";
 	static public final String LIST_LOAD_BALANCER_RULES           = "listLoadBalancerRules";
 	static public final String LIST_LOAD_BALANCER_RULE_INSTANCES  = "listLoadBalancerRuleInstances";
 	static public final String REMOVE_FROM_LOAD_BALANCER_RULE     = "removeFromLoadBalancerRule";
 	static private final Logger logger = Logger.getLogger(LoadBalancers.class);
 	private OpSource provider = null;
 
 	LoadBalancers(OpSource provider) {
         super(provider);
 		this.provider = provider;
 	}
 
 	@Override
 	public void addDataCenters(String toLoadBalancerId, String ... dataCenterIds) throws CloudException, InternalException {
 		throw new OperationNotSupportedException("These load balancers does not support add data center.");
 	}
 
 	private Probe getProbe(String probeId, String networkId) throws CloudException, InternalException{
 		ArrayList<Probe> list = (ArrayList<Probe>) this.listProbes(networkId);
 		for(Probe probe : list){
 			if(probe.getProbeId().equals(probeId)){
 				return probe;
 			}    		
 		}
 		return null;
 
 
 	}
 	private Probe getProbe(String networkId, LbProtocol protocol, String listenAddress,  int listenPort) throws CloudException, InternalException{
 
 		ArrayList<Probe> list = (ArrayList<Probe>) listProbes(networkId);
 		if(list == null)
 			return null;
 		for(Probe probe : list){
 
 			LbListener listener = probe.getLbListener();    		
 
 			if(!protocol.equals(LbProtocol.RAW_TCP) && listenPort != listener.getPublicPort())
 				continue;
 
 			if(!protocol.equals(LbProtocol.RAW_TCP) && probe.getListenAddress() != listenAddress)
 				continue;
 
 			return probe;
 		}
 		return null;
 	}
 
 	//https://<Cloud API URL>/oec/0.9/{org-id}/network/{networkid}/
 	// probe
 	private String addProbe(String networkId, LbProtocol protocol, String listenAddress,  int listenPort) throws InternalException, CloudException{
 
 		if(networkId == null ){
 			return null;
 		}
 
 		HashMap<Integer, Param>  parameters = new HashMap<Integer, Param>();
 		Param param = new Param(OpSource.NETWORK_BASE_PATH, null);
 
 		parameters.put(0, param);
 
 		param = new Param(networkId, null);
 
 		parameters.put(1, param);
 
 		param = new Param("probe", null);
 
 		parameters.put(2, param);
 
 		//Create post body
 		Document doc = provider.createDoc();
 		Element probe = doc.createElementNS("http://oec.api.opsource.net/schemas/vip","NewProbe");
 
 
 		Element nameElmt = doc.createElement("name");
 		nameElmt.setTextContent("probe" +System.currentTimeMillis());
 
 		/** TCP, UDP, HTTP, HTTPS, ICMP */
 		Element typeElmt = doc.createElement("type");     
 
 		/** <!-- Integer 15-65535 --> */
 		Element probeIntervalSeconds = doc.createElement("probeIntervalSeconds");
 		probeIntervalSeconds.setTextContent("20");
 
 		/** <!-- Integer 1-65535 --> */
 		Element errorCountBeforeServerFail = doc.createElement("errorCountBeforeServerFail");
 		errorCountBeforeServerFail.setTextContent("10");
 
 		/** <!-- Integer 1-65535 --> */
 		Element successCountBeforeServerEnable = doc.createElement("successCountBeforeServerEnable");
 		successCountBeforeServerEnable.setTextContent("10");
 		/** <!-- Integer 15-65535 --> */
 		Element failedProbeIntervalSeconds = doc.createElement("failedProbeIntervalSeconds");
 		failedProbeIntervalSeconds.setTextContent("300");
 		/** <!-- Integer 2-65535 --> */
 		Element maxReplyWaitSeconds = doc.createElement("maxReplyWaitSeconds");
 		maxReplyWaitSeconds.setTextContent("300");
 
 		probe.appendChild(nameElmt);
         probe.appendChild(typeElmt);
         probe.appendChild(probeIntervalSeconds);
         probe.appendChild(errorCountBeforeServerFail);
         probe.appendChild(successCountBeforeServerEnable);
         probe.appendChild(failedProbeIntervalSeconds);
         probe.appendChild(maxReplyWaitSeconds);
 
         if(protocol.equals(LbProtocol.HTTP)){
             typeElmt.setTextContent("HTTP");
             Element statusCodeRange = doc.createElement("statusCodeRange");
 
             /** <!--Optional: mandatory if type is HTTP/HTTPS, Integer 0-999 for each of
                 * lowerBound and upperBound --> */
             Element lowerBound = doc.createElement("lowerBound");
             lowerBound.setTextContent("0");
 
             Element upperBound = doc.createElement("upperBound");
             upperBound.setTextContent("999");
 
             statusCodeRange.appendChild(lowerBound);
             statusCodeRange.appendChild(upperBound);
 
             /** <!--Optional: mandatory if type is HTTP/HTTPS, one of (GET, HEAD) -->  */
             Element requestMethod = doc.createElement("requestMethod");
             requestMethod.setTextContent("GET");
 
             /**  <!--Optional: only relevant if type is HTTP/HTTPS, String, 255 chars max, no
                ** spaces --> */
 
             Element requestUrl = doc.createElement("requestUrl");
 
             String url = "http://" + listenAddress;
             requestUrl.setTextContent(url);
 
             /** <!--Optional: only relevant if type is HTTP/HTTPS, String, 255 chars max,
                 * alphanumeric, spaces permitted --> */
             Element matchContent = doc.createElement("matchContent");
             matchContent.setTextContent("XXX");
             probe.appendChild(statusCodeRange);
             probe.appendChild(requestMethod);
             probe.appendChild(requestUrl);
             probe.appendChild(matchContent);
 
         }
 		else if(protocol.equals(LbProtocol.HTTPS)){
             typeElmt.setTextContent("HTTPS");
 
             Element statusCodeRange = doc.createElement("statusCodeRange");
 
 			/** <!--Optional: mandatory if type is HTTP/HTTPS, Integer 0-999 for each of
 				lowerBound and upperBound --> */
 			Element lowerBound = doc.createElement("lowerBound");
 			lowerBound.setTextContent("0");
 
 			Element upperBound = doc.createElement("upperBound");
 			upperBound.setTextContent("999");
 
 			statusCodeRange.appendChild(lowerBound);
 			statusCodeRange.appendChild(upperBound);
 			Element requestMethod = doc.createElement("requestMethod");
 			requestMethod.setTextContent("GET");
 
 			Element requestUrl = doc.createElement("requestUrl");
 
 			String url = "https://" + listenAddress;
 			requestUrl.setTextContent(url);   
 
 			Element matchContent = doc.createElement("matchContent");
 			probe.appendChild(statusCodeRange); 
 			probe.appendChild(requestMethod);            
 			probe.appendChild(requestUrl);
 			probe.appendChild(matchContent);           
 		}else{
 			typeElmt.setTextContent("TCP"); 
 		}
 
         /** <!--Optional: only relevant if type is HTTP/HTTPS, Integer 1-65535--> */
         Element port = doc.createElement("port");
         port.setTextContent(String.valueOf(listenPort));
 
         probe.appendChild(port);
         doc.appendChild(probe);
 
 		OpSourceMethod method = new OpSourceMethod(provider, 
 				provider.buildUrl(null,true, parameters),
 				provider.getBasicRequestParameters(OpSource.Content_Type_Value_Single_Para, "POST", provider.convertDomToString(doc)));
 
 		return  method.getRequestResultId("Add probe ", method.invoke(), "result", "resultDetail");
 	}
 
 
 	@Override
 	public void addServers(String toLoadBalancerId, String ... serverIds) throws CloudException, InternalException {
         APITrace.begin(provider, "LB.addServers");
         try {
             String networkId = this.getNetworkIdFromLoadBalancerId(toLoadBalancerId);
             if( networkId == null ) {
                 throw new CloudException("No such load balancer: " + toLoadBalancerId);
             }
 
             if( serverIds == null || serverIds.length < 1 ) {
                 throw new CloudException("Server is null");
             }
 
             /** Check if network of LB and Servers are the same */
             for(String serverId: serverIds){
                 VirtualMachine vm =  provider.getComputeServices().getVirtualMachineSupport().getVirtualMachine(serverId);
                 if(vm != null && serverId.equals(vm.getProviderVirtualMachineId())){
                     String currentVlanId = vm.getProviderVlanId();
                     /** VMs are not within the same VLAN as LB */
                     if(!currentVlanId.equals(networkId)){
                         logger.error("Currently, OpSource does not support to create LB accross multiple network, e.g., the network of LB'Ip address and vm should be the same");
                         throw new CloudException("Currently,LB's network Id is " + networkId +  " while server with Id " + serverId + "'s network Id is " + currentVlanId + "; However OpSource does not support to create LB accross multiple network, e.g., the network of LB'Ip address and vm should be the same");
                     }
                 }else{
                     throw new CloudException("Can not locate VM with the Id " + serverId + " while trying to add server to the LB with Id " + toLoadBalancerId );
                 }
             }
 
             String serverFarmId = getServerFarmIdFromLbId(null, toLoadBalancerId);
             if( serverFarmId == null ) {
                 throw new CloudException("No such load balancer: " + toLoadBalancerId);
             }
 
             ArrayList<String> realServerIds = toRealServerIds(networkId, serverIds);
             LoadBalancer lb = getLoadBalancer(toLoadBalancerId);
             for( String realServerId: realServerIds ) {
                 for(LbListener listener: lb.getListeners()){
                     addRealServerToServerFarm(networkId, realServerId,listener.getPublicPort(),  serverFarmId);
                 }
             }
         }
         finally {
             APITrace.end();
         }
 	}
 	/**
 	 * https://<Cloud API URL>/oec/0.9/{org-id}/network/{networkid}/
 serverFarm
 	 */
 
 	private String addServerFarm(String networkId, String probeId, String predictor, String realServerId, int port) throws InternalException, CloudException{
 		if(networkId == null ){
 			return null;
 		}
 		HashMap<Integer, Param>  parameters = new HashMap<Integer, Param>();
 		Param param = new Param(OpSource.NETWORK_BASE_PATH, null);
 		parameters.put(0, param);
 		param = new Param(networkId, null);
 		parameters.put(1, param);
 		param = new Param("serverFarm", null);
 		parameters.put(2, param);
 
 		/** Create post body */
 		Document doc = provider.createDoc();
 		Element serverFarm = doc.createElementNS("http://oec.api.opsource.net/schemas/vip", "NewServerFarm");
 
 		Element nameElmt = doc.createElement("name");
 		nameElmt.setTextContent("serverFarm_" + System.currentTimeMillis());
 
 		if(probeId != null ){
 			Element probeIdElmt = doc.createElement("probeId");
 			probeIdElmt.setTextContent(probeId);
 			serverFarm.appendChild(probeIdElmt);
 		}
 
 		Element predictorElmt = doc.createElement("predictor");
 		predictorElmt.setTextContent(predictor);
 		
 		Element realServerElmt = doc.createElement("realServer");
 		Element realserverIdElmt = doc.createElement("id");
 		realserverIdElmt.setTextContent(realServerId); 
 
 		Element realserverPortElmt = doc.createElement("port");
 		realserverPortElmt.setTextContent(String.valueOf(port));
 		realServerElmt.appendChild(realserverIdElmt);
 		realServerElmt.appendChild(realserverPortElmt);
 
 		doc.appendChild(serverFarm);
 		serverFarm.appendChild(nameElmt);
 
 		serverFarm.appendChild(predictorElmt);
 		serverFarm.appendChild(realServerElmt);
 
 		OpSourceMethod method = new OpSourceMethod(provider, 
 				provider.buildUrl(null,true, parameters),
 				provider.getBasicRequestParameters(OpSource.Content_Type_Value_Single_Para, "POST", provider.convertDomToString(doc)));
 
 		return method.getRequestResultId("Add server farm", method.invoke(), "result", "resultDetail");
 	}
 
 	private String addRealServer(String networkId, String serverId) throws InternalException, CloudException{
 		if(serverId == null){
 			return null;
 		}
 
 		HashMap<Integer, Param>  parameters = new HashMap<Integer, Param>();
 		Param param = new Param(OpSource.NETWORK_BASE_PATH, null);
 		parameters.put(0, param);
 
 		if(networkId != null){
 			param = new Param(networkId, null);
 		}else{
 			param = new Param(provider.getDefaultVlanId(), null);
 		}
 		parameters.put(1, param);
 
 		param = new Param("realServer", null);
 		parameters.put(2, param);      	
 
 		/** Create post body */
 		Document doc = provider.createDoc();
 		Element realServer = doc.createElementNS("http://oec.api.opsource.net/schemas/vip", "NewRealServer");
 
 		Element nameElmt = doc.createElement("name");
 		nameElmt.setTextContent(serverId);
 
 		Element serverIdElmt = doc.createElement("serverId");
 		serverIdElmt.setTextContent(serverId);      
 
 		Element inServiceElmt = doc.createElement("inService");
 		inServiceElmt.setTextContent("true");
 
 		doc.appendChild(realServer);        
 		realServer.appendChild(nameElmt);
 		realServer.appendChild(serverIdElmt);        
 		realServer.appendChild(inServiceElmt);
 
 		OpSourceMethod method = new OpSourceMethod(provider, 
 				provider.buildUrl(null,true, parameters),
 				provider.getBasicRequestParameters(OpSource.Content_Type_Value_Single_Para, "POST", provider.convertDomToString(doc)));
 
 		if(method.parseRequestResult("Add real server", method.invoke(), "result", "resultDetail")){
 			return getRealServerIdfromServerId(networkId, serverId);
 		}else{
 			throw new CloudException("Fail to add real server");
 		}       	
 	}
 	
 	 /** https://<Cloud API URL>/oec/0.9/{org-id}/network/{networkid}/
 serverFarm/{server-farm-id}/addRealServer
 	 */
 	private String addRealServerToServerFarm(String networkId,String realServerId, int port,String serverFarmId) throws InternalException, CloudException{
 		if(realServerId == null ||  serverFarmId == null){
 			return null;
 		}
 
 		HashMap<Integer, Param>  parameters = new HashMap<Integer, Param>();
 		Param param = new Param(OpSource.NETWORK_BASE_PATH, null);
 		parameters.put(0, param);
 
 		param = new Param(networkId, null);
 		parameters.put(1, param);
 
 		param = new Param("serverFarm", null);
 		parameters.put(2, param);
 
 		param = new Param(serverFarmId, null);
 		parameters.put(3, param);
 
 		param = new Param("addRealServer", null);
 
 		parameters.put(4, param);
 		/** Create post body */
 		String requestBody = "realServerId=";
 		requestBody += realServerId;
 		requestBody += "&realServerPort=" + port;
 
 		OpSourceMethod method = new OpSourceMethod(provider, 
 				provider.buildUrl(null,true, parameters),
 				provider.getBasicRequestParameters(OpSource.Content_Type_Value_Modify, "POST", requestBody));
 
 		return method.requestResult("Add real server to server farm", method.invoke(), "result", "resultCode");
 
 	} 
 	/**
 	 * https://<Cloud API URL>/oec/0.9/{org-id}/network/{networkid}/
 	 *	serverFarm/{server-farm-id}/addProbe
 	 */
 
 	private String addProbeToServerFarm(String networkId,String probeId, String serverFarmId) throws InternalException, CloudException{
 		if(probeId == null &&  serverFarmId == null){
 			return null;
 		}
 
 		HashMap<Integer, Param>  parameters = new HashMap<Integer, Param>();
 		Param param = new Param(OpSource.NETWORK_BASE_PATH, null);
 		parameters.put(0, param);
 
 		param = new Param(networkId, null);
 		parameters.put(1, param);
 
 		param = new Param("serverFarm", null);
 		parameters.put(2, param);
 
 		param = new Param(serverFarmId, null);
 		parameters.put(3, param);
 
 		param = new Param("addProbe", null);
 		parameters.put(4, param);
 
 		/** Create post body */
 		String requestBody = "probeId=";
 		requestBody += probeId;
 
 		OpSourceMethod method = new OpSourceMethod(provider, 
 				provider.buildUrl(null,true, parameters),
 				provider.getBasicRequestParameters(OpSource.Content_Type_Value_Modify, "POST", requestBody));
 
 		return method.requestResult("Add probe to server farm", method.invoke(), "result", "resultDetail");
 	}
 
 	private String convertLbAlgorithToPredictor(LbAlgorithm lbAlgorithm){
 		switch (lbAlgorithm){
 			case ROUND_ROBIN: return "ROUND_ROBIN";
 			case LEAST_CONN	: return "LEAST_CONNECTIONS";
 			default: return "LEAST_CONNECTIONS";  		
 		}
 	}
 	private LbAlgorithm convertPredictorToLbAlgorith( String predictor){
 		if(predictor.equals("ROUND_ROBIN")){
 			return LbAlgorithm.ROUND_ROBIN;
 		}
 		else if(predictor.equals("LEAST_CONNECTIONS")){
 			return LbAlgorithm.LEAST_CONN;
 		}
 		else{
 			return LbAlgorithm.LEAST_CONN;
 		}	
 	}
 	private boolean checkLoadBalancer(String networkId, String addressId, int publicPort) throws CloudException, InternalException{
 		ArrayList<LoadBalancer> list = (ArrayList<LoadBalancer>) this.listLoadBalancers(networkId);
 		if(list == null){
 			return false;
 		}
 		for(LoadBalancer lb: list){
 			boolean sameAddress = false;
 			boolean samePort = false;
 			if(lb.getAddress() != null && lb.getAddress().equals(addressId)){
 				sameAddress = true;
 			}
 			int[] ports = lb.getPublicPorts();
 			for(int port : ports){
 				if(publicPort == port ){
 					samePort = true;
 					break;
 				}    			
 			}
 			if(sameAddress && samePort){
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * LB in opsource can only be created when the VMs, ip address for the LB are within the same VLAN
 	 * https://<Cloud API URL>/oec/0.9/{org-id}/network/{network-id}/vip
 	 */
     @Deprecated
 	@Override
 	public String create(String name, String description, String addressId, String[] dcIds, LbListener[] listeners, String[] servers) throws CloudException, InternalException {
         APITrace.begin(provider, "LB.create");
         try {
             if(servers == null){
                 logger.error("Can not create load balancer with servers becasue servers is null");
                 throw new CloudException("Can not create load balancer with servers becasue servers is null");
             }
 
             /** Use Server's VLan */
             JitCollection<VirtualMachine> vms = (JitCollection<VirtualMachine>) provider.getComputeServices().getVirtualMachineSupport().listVirtualMachines();
             boolean firstServer = true;
             String networkId = null;
             for(String server: servers){
                 for(VirtualMachine vm: vms){
                     if(server.equals(vm.getProviderVirtualMachineId())){
                         String currentVlanId = vm.getProviderVlanId();
                         if(firstServer){
                             networkId = currentVlanId;
                             firstServer = false;
                             break;
                         }else{
                             /** VMs are not within the same VLAN */
                             if(!currentVlanId.equals(networkId)){
                                 logger.error("Currently, OpSource does not support to create LB accross multiple network, e.g., the network of LB'Ip address and vm should be the same");
                                 throw new CloudException("Currently, OpSource does not support to create LB accross multiple network, e.g., the network of LB'Ip address and vm should be the same");
                             }
                         }
                     }
                 }
             }
             return create(name, description, addressId, dcIds, listeners, servers, networkId);
         }
         finally {
             APITrace.end();
         }
 	}
 
 
 	private String create(String name, String description, String addressId, String[] dcIds, LbListener[] listeners, String[] servers, String networkId) throws CloudException, InternalException {
 		String serverFarmId = null; 
 
 		ArrayList <String> realServerIds = new ArrayList <String>();
 		ArrayList <String> probeIds = new ArrayList <String>();
 
 		realServerIds = toRealServerIds(networkId, servers);
 
 		if(realServerIds == null){	
 			String serverIds = "{";
 			for(String serverId: servers){
 				serverIds += serverId + ", ";				
 			}
 			if(serverIds.endsWith(",")){
 				serverIds = serverIds.substring(0, serverIds.length() -1) + "}";
 			}
 			logger.error("Can not create load balancer with servers becasue servers "+ serverIds +  " can not be added as realServer!");
 			throw new CloudException("Can not create load balancer with servers becasue servers "+ serverIds +  " can not be added as realServer!");
 		}
 
 		LbAlgorithm lbAlgorithm = null;	        
 		LbProtocol lbProtocol = null;
 
 		try{ 
 			/** Port for the VIP */
 			int port = -1;	        
 			if(listeners != null){
 				for(LbListener listener : listeners ){
 					lbAlgorithm =  listener.getAlgorithm();
 					lbProtocol = listener.getNetworkProtocol();
 					port = listener.getPublicPort();
 
 					if(checkLoadBalancer(networkId, addressId, port)){	    		
 						throw new CloudException("Balancer already exisit !");
 					}
 
 					if(serverFarmId == null){  					
 						serverFarmId = addServerFarm(networkId, null, 
 								convertLbAlgorithToPredictor(lbAlgorithm), realServerIds.get(0), port);
 
 						/** Add server real servers	*/				
 						for(int i=1;i< realServerIds.size();i++){
 							addRealServerToServerFarm(networkId, realServerIds.get(i), port, serverFarmId);						
 						}
 					}else{
 						/** Add servers */
 						for(int i=0;i< realServerIds.size();i++){
 							addRealServerToServerFarm(networkId, realServerIds.get(i), port, serverFarmId);											
 						}
 					}
 					Probe probe = getProbe(networkId, lbProtocol, addressId, listener.getPublicPort());
 					String probeId = null;
 					if(probe == null){        			 
 						probeId = addProbe(networkId, lbProtocol, addressId, listener.getPublicPort());
 					}else{        			 
 						probeId = probe.getProbeId();        			 
 					}
 					if(probeId != null ){	        			      							
 						/** Add probe to the server farm */
 						addProbeToServerFarm(networkId, probeId, serverFarmId);	
 						probeIds.add(probeId);
 					}        	
 				}			
 			}
 
 			/**  Now add VIP */
 			/** Create post body */
 			Document doc = provider.createDoc();
 			Element vip = doc.createElementNS("http://oec.api.opsource.net/schemas/vip", "NewVip");
 
 			Element nameElmt = doc.createElement("name");
 			nameElmt.setTextContent(name);
 
 			Element protocolElmt = doc.createElement("protocol");
 
 			if(lbProtocol.name().contains("UDP")){
 				protocolElmt.setTextContent("UDP");
 			}else{
 				protocolElmt.setTextContent("TCP");
 			}
 
 			if(addressId != null && isAddressInVip(networkId,addressId)){
 				Element ipAddressElmt = doc.createElement("ipAddress");
 				ipAddressElmt.setTextContent(addressId);
 				vip.appendChild(ipAddressElmt);
 			}
 
 			Element portElmt = doc.createElement("port");
 			if(port != -1){        	
 				portElmt.setTextContent(String.valueOf(port));        	
 			}else{
 				/** Set as 80? */
 				portElmt.setTextContent("80");
 			}
 
 			/** Default SERVER_FARM */
 			Element vipTargetTypeElmt = doc.createElement("vipTargetType");
 			vipTargetTypeElmt.setTextContent("SERVER_FARM");
 
 			Element vipTargetIdElmt = doc.createElement("vipTargetId");
 			vipTargetIdElmt.setTextContent(serverFarmId);        
 
 			Element replyToIcmpElmt = doc.createElement("replyToIcmp");
 			replyToIcmpElmt.setTextContent("true");
 
 			Element inServiceElmt = doc.createElement("inService");
 			inServiceElmt.setTextContent("true");
 
 			doc.appendChild(vip);        
 			vip.appendChild(nameElmt);
 			vip.appendChild(protocolElmt);  
 			vip.appendChild(portElmt);        
 			vip.appendChild(vipTargetTypeElmt);
 			vip.appendChild(vipTargetIdElmt); 
 			vip.appendChild(replyToIcmpElmt);
 			vip.appendChild(inServiceElmt);
 
 			HashMap<Integer, Param>  parameters = new HashMap<Integer, Param>();
 			Param param = new Param(OpSource.NETWORK_BASE_PATH, null);
 			parameters.put(0, param);
 			param = new Param(networkId, null);
 			parameters.put(1, param);
 			param = new Param("vip", null);
 			parameters.put(2, param);
 
 			OpSourceMethod method = new OpSourceMethod(provider, 
 					provider.buildUrl(null,true, parameters),
 					provider.getBasicRequestParameters(OpSource.Content_Type_Value_Single_Para, "POST", provider.convertDomToString(doc)));
 
 			return method.getRequestResultId("Add load balancer", method.invoke(), "result", "resultDetail");
 		}catch (Exception e){
 			logger.error(e.getMessage());			
 			/** If vip fails,then kill all resources */
 			if(serverFarmId != null){
 				logger.info("Failr to create LB,now kill all resources");
 				removeServerFarm(networkId, serverFarmId);
 			}else{
 				/**  Remove real server */	    		
 				for(String realServerId : realServerIds){
 					removeRealServer(networkId, realServerId);
 				}
 				/**  Remove probe */
 				for(String probeId : probeIds){
 					removeProbe(networkId, probeId);
 				}
 			}
 			/** Need to delete server from the pool? */
 			/**
 	    	for(String serverId : servers){    		  
 	    		provider.getComputeServices().getVirtualMachineSupport().terminate(serverId);    		  
 	    	}*/	    	
 			e.printStackTrace();	    	
 		}
 		return null;	  	      
 	}
 	@Override
 	public LoadBalancerAddressType getAddressType() throws CloudException, InternalException {
 		return LoadBalancerAddressType.IP;
 	}
 
 	private String getNetworkIdFromLoadBalancerId(String balancerId) throws InternalException, CloudException{
 		ArrayList<VLAN> networkList = (ArrayList<VLAN>) provider.getNetworkServices().getVlanSupport().listVlans();
 
 		for(VLAN network : networkList){
 			String networkId = network.getProviderVlanId();
 			Node balancerNode = getBalancerNode(networkId, balancerId);
 
 			NodeList attributes = balancerNode.getChildNodes();
 
 			for(int i = 0; i< attributes.getLength(); i++){
 				Node attribute = attributes.item(i);
 				if(attribute.getNodeType() == Node.TEXT_NODE) continue;
 				String name = attribute.getNodeName();
 				String value;
 
 				if( attribute.getChildNodes().getLength() > 0 ) {
 					value = attribute.getFirstChild().getNodeValue();                
 				}else {
 					continue;
 				}
                 String sNS = "";
                 try{
                     sNS = name.substring(0, name.indexOf(":") + 1);
                 }
                 catch(IndexOutOfBoundsException ex){}
 				if( name.equalsIgnoreCase(sNS + "id") ) {
 					if(value.equalsIgnoreCase(balancerId)){
 						return networkId;
 					}            	 
 				}                
 			}
 		}
 		return null;          	
 	}
 
 	private String getNetworkIdFromServerFarmId(String serverFarmId) throws InternalException, CloudException{
 		ArrayList<VLAN> networkList = (ArrayList<VLAN>) provider.getNetworkServices().getVlanSupport().listVlans();
 
 		for(VLAN network : networkList){
 
 			String networkId = network.getProviderVlanId();
 
 			Node serverFarNode = this.getServerFarmNode(networkId, serverFarmId);
 
 			NodeList attributes = serverFarNode.getChildNodes();
 
 			for(int i = 0; i< attributes.getLength(); i++){
 				Node attribute = attributes.item(i);
 
 				if(attribute.getNodeType() == Node.TEXT_NODE) continue;
 
 				String name = attribute.getNodeName();
 				String value;
 
 				if( attribute.getChildNodes().getLength() > 0 ) {
 					value = attribute.getFirstChild().getNodeValue();                
 				}else {
 					continue;
 				}
                 String sNS = "";
                 try{
                     sNS = name.substring(0, name.indexOf(":") + 1);
                 }
                 catch(IndexOutOfBoundsException ex){}
 				if( name.equalsIgnoreCase(sNS + "id") ) {
 					if(value.equalsIgnoreCase(serverFarmId)){
 						return networkId;
 					}            	 
 				}                
 			}
 		}
 
 		return null;
 
 	}
 
 	private Node getBalancerNode(String networkId, String balancerId) throws InternalException, CloudException{
 
 		if(networkId != null){
 			HashMap<Integer, Param>  parameters = new HashMap<Integer, Param>();
 			Param param = new Param(OpSource.NETWORK_BASE_PATH, null);
 			parameters.put(0, param);
 			param = new Param(networkId, null);
 			parameters.put(1, param);   
 			param = new Param("vip", null);
 			parameters.put(2, param);  
 			
 			OpSourceMethod method = new OpSourceMethod(provider, 
 					provider.buildUrl(null,true, parameters),
 					provider.getBasicRequestParameters(OpSource.Content_Type_Value_Single_Para, "GET", null));
 
 			Document doc = method.invoke();
             String sNS = "";
             try{
                 sNS = doc.getDocumentElement().getTagName().substring(0, doc.getDocumentElement().getTagName().indexOf(":") + 1);
             }
             catch(IndexOutOfBoundsException ex){}
 			NodeList matches = doc.getElementsByTagName(sNS + "vip");
 			if(matches != null){
 				for( int i=0; i<matches.getLength(); i++ ) {
 					Node node = matches.item(i);
 					NodeList attributes = node.getChildNodes();	
 					for( int j=0; j<attributes.getLength(); j++ ) {
 						Node attribute = attributes.item(j);
 						if(attribute.getNodeType() == Node.TEXT_NODE) continue;
 
 						String name = attribute.getNodeName();
 						String value;
 
 						if( attribute.getChildNodes().getLength() > 0 ) {
 							value = attribute.getFirstChild().getNodeValue();                
 						}else {
 							continue;
 						}
 						if( name.equalsIgnoreCase(sNS + "id") && value.equals(balancerId) ) {
 							/** Set the default Vlan Id */
 							return node;
 						}
 					}                    
 				}
 			}
 		}
 		return null;
 	}
 	/**
 	 * https://<Cloud API URL>/oec/0.9/{org-id}/network/{networkid}/
 serverFarm/{server-farm-id}
 	 * @param networkId
 	 * @param serverFarmId
 	 */
 	private Node getServerFarmNode(String networkId,String serverFarmId) throws InternalException, CloudException{
 		HashMap<Integer, Param>  parameters = new HashMap<Integer, Param>();
 
 		Param param = new Param(OpSource.NETWORK_BASE_PATH, null);
 		parameters.put(0, param);
 
 		param = new Param(networkId, null);
 		parameters.put(1, param);
 
 		param = new Param("serverFarm", null);
 		parameters.put(2, param);
 
 		param = new Param(serverFarmId, null);
 		parameters.put(3, param);      	
 
 		OpSourceMethod method = new OpSourceMethod(provider, 
 				provider.buildUrl(null,true, parameters),
 				provider.getBasicRequestParameters(OpSource.Content_Type_Value_Single_Para, "GET", null));
 
 		Document doc = method.invoke();
         String sNS = "";
         try{
             sNS = doc.getDocumentElement().getTagName().substring(0, doc.getDocumentElement().getTagName().indexOf(":") + 1);
         }
         catch(IndexOutOfBoundsException ex){}
 		NodeList matches = doc.getElementsByTagName(sNS + "ServerFarm");
 		if(matches != null){
 			return doc.getDocumentElement();
 		}
 		return null;
 	}
 
 	@Override
 	public LoadBalancer getLoadBalancer(String loadBalancerId) throws CloudException, InternalException {
         APITrace.begin(provider, "LB.getLoadBalancer");
         try {
             ArrayList<LoadBalancer> list = (ArrayList<LoadBalancer>) listLoadBalancers();
             if(list == null){
                 return null;
             }
             for(LoadBalancer balancer : list){
                 if(balancer.getProviderLoadBalancerId().equals(loadBalancerId)){
                     return balancer;
                 }
             }
             return null;
         }
         finally {
             APITrace.end();
         }
 	}
 
     @Deprecated
     @Override
     public Iterable<LoadBalancerServer> getLoadBalancerServerHealth(String loadBalancerId) throws CloudException, InternalException {
         return Collections.emptyList(); // todo: implement me
     }
 
     @Deprecated
     @Override
     public Iterable<LoadBalancerServer> getLoadBalancerServerHealth(String loadBalancerId, String... serverIdsToCheck) throws CloudException, InternalException {
         return Collections.emptyList(); //todo implement me
     }
 
     public LoadBalancer getLoadBalancer(String networkId, String loadBalancerId) throws CloudException, InternalException {
         APITrace.begin(provider, "LB.getLoadBalancerOnNetwork");
         try {
             ArrayList<LoadBalancer> list = (ArrayList<LoadBalancer>) listLoadBalancers(networkId);
             if(list == null){
                 return null;
             }
             for(LoadBalancer balancer : list){
                 if(balancer.getProviderLoadBalancerId().equals(loadBalancerId)){
                     return balancer;
                 }
             }
             return null;
         }
         finally {
             APITrace.end();
         }
 	}
 
 	@Override
 	public int getMaxPublicPorts() throws CloudException, InternalException {
 		return 999;
 	}
 
 	@Override
 	public String getProviderTermForLoadBalancer(Locale locale) {
 		return "Vip";
 	}
 
     @Override
     public Iterable<ResourceStatus> listLoadBalancerStatus() throws CloudException, InternalException {
         return null;  //TODO: Implement for 2013.01
     }
 
 
     private String getRealServerIdfromServerId(String networkId, String serverId) throws InternalException, CloudException{
 		ArrayList<RealServer> list = (ArrayList<RealServer>) listAllRealServer(networkId);
 		if(list == null){
 			return null;
 		}
 		for(RealServer realServer : list){
 			if(realServer.getServerId().equals(serverId)){
 				return realServer.getId();
 			}
 		}
 		return null;
 	}
 
 	private String[] getServerIdfromRealServerId(String networkId, String[] realServerIds) throws InternalException, CloudException{
 		ArrayList<RealServer> list = (ArrayList<RealServer>) listAllRealServer(networkId);
 		if(list == null){
 			return null;
 		}
 		ArrayList<String> serverIds = new ArrayList<String>();
 		for(String realServerId : realServerIds ){
 			for(RealServer realServer : list){
 				if(realServer.getId().equals(realServerId)){
 					serverIds.add(realServer.getServerId());
 					break;
 				}
 			}
 		}
 		return serverIds.toArray(new String[serverIds.size()]);
 	}
 
 	private String getServerFarmIdFromLbId(String networkId, String balancerId) throws InternalException, CloudException{
 		Node balancerNode = null;
 		if(networkId != null){
 			balancerNode = getBalancerNode(networkId, balancerId);
 		}else{
 			ArrayList<VLAN> networkList = (ArrayList<VLAN>) provider.getNetworkServices().getVlanSupport().listVlans();
 			if(networkList == null){
 				return null;
 			}
 			for(VLAN network : networkList){
 				networkId = network.getProviderVlanId();
 				balancerNode = getBalancerNode(networkId, balancerId);
 				if(balancerNode != null){
 					break;
 				}
 			}
 		}
 		if(balancerNode == null){
 			return null;
 		}
 
 		NodeList attributes = balancerNode.getChildNodes();
 		boolean isServerFarmType = false;
 		for(int i = 0; i< attributes.getLength(); i++){
 			Node attribute = attributes.item(i);
 			if(attribute.getNodeType() == Node.TEXT_NODE) continue;
 			String name = attribute.getNodeName();
 			String value;
 			if( attribute.getChildNodes().getLength() > 0 ) {
 				value = attribute.getFirstChild().getNodeValue();
 			}
 			else {
 				continue;
 			}
             String sNS = "";
             try{
                 sNS = name.substring(0, name.indexOf(":") + 1);
             }
             catch(IndexOutOfBoundsException ex){}
 			if( name.equalsIgnoreCase(sNS + "vipTargetType") ) {
 				if(value.equalsIgnoreCase("SERVER_FARM")){
 					isServerFarmType = true;
 				}
 			}
 			else if( name.equalsIgnoreCase(sNS + "vipTargetId") && isServerFarmType) {
 				return value;
 			}
 		}
 		return null;
 	}
 
 	static private volatile List<LbAlgorithm> algorithms = null;
 
 	@Override
 	public Iterable<LbAlgorithm> listSupportedAlgorithms() {
 		List<LbAlgorithm> list = algorithms;
 
 		if( list == null ) {
 			list = new ArrayList<LbAlgorithm>();
 			list.add(LbAlgorithm.ROUND_ROBIN);
 			list.add(LbAlgorithm.LEAST_CONN);
 			algorithms = Collections.unmodifiableList(list);
 		}
 		return algorithms;
 	}
 
     @Override
     public @Nonnull Iterable<IPVersion> listSupportedIPVersions() throws CloudException, InternalException {
         return Collections.singletonList(IPVersion.IPV4);
     }
 
     static private volatile List<LbProtocol> protocols = null;
 
 	@Override
 	public Iterable<LbProtocol> listSupportedProtocols() {
 		List<LbProtocol> list = protocols;
 		if( protocols == null ) {
 			list = new ArrayList<LbProtocol>();
 			list.add(LbProtocol.RAW_TCP);
 			list.add(LbProtocol.HTTP);
 			list.add(LbProtocol.HTTPS);
 			protocols = Collections.unmodifiableList(list);
 		}
 		return protocols;
 	}
 
 	private ArrayList<String> toRealServerIds(String networkId, @Nonnull String[] servers) throws InternalException, CloudException{
 		ArrayList<RealServer> list = (ArrayList<RealServer>) listAllRealServer(networkId);
 		ArrayList<String> realServerIds = new ArrayList<String>();
 		if(list == null){
 			for(String serverId : servers){
 				String realServerId = addRealServer(networkId, serverId);
 				if(realServerId != null){
 					realServerIds.add(realServerId);
 				}
 			}
 		}else{
 			for(String serverId : servers){
 				String realServerId = null;
 				for(RealServer realServer: list){
 					if(serverId.equals(realServer.getServerId())){
 						realServerId = realServer.getId();
 						break;
 					}
 				}
 				if(realServerId == null){
 					realServerId = this.addRealServer(networkId, serverId);
 					if(realServerId != null){
 						realServerIds.add(realServerId);
 					}
 				}else{
 					realServerIds.add(realServerId);
 				}
 			}
 		}
 		return realServerIds;
 	}
 
 	@Override
 	public boolean isAddressAssignedByProvider() throws CloudException, InternalException {
 		/** Need to reserve IP address for the first time */
 		return true;
 	}
 
 	private boolean isAddressInVip(String networkId,String addressId) throws CloudException, InternalException{
 		ArrayList<LoadBalancer> list = (ArrayList<LoadBalancer>) this.listLoadBalancers(networkId);
 		for(LoadBalancer lb : list){
 			if(lb.getAddress().equals(addressId)){
 				return true;
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public boolean isDataCenterLimited() {
 		return true;
 	}
 
 	@Override
 	public @Nonnull String[] mapServiceAction(@Nonnull ServiceAction action) {
 		return new String[0];
 	}
 
 	@Override
 	public boolean isSubscribed() throws CloudException, InternalException {
		return true;
 	}
 
 	@Override
 	public boolean supportsMonitoring() {
 		return false;
 	}
 
 	private ArrayList<RealServer> listAllRealServer(String networkId) throws CloudException, InternalException {
 
 		ArrayList<RealServer> list = new ArrayList<RealServer>();
 
 		if(networkId == null){
 			return null;
 		}
 		HashMap<Integer, Param>  parameters = new HashMap<Integer, Param>();
 		Param param = new Param(OpSource.NETWORK_BASE_PATH, null);
 		parameters.put(0, param);
 
 		param = new Param(networkId, null);
 		parameters.put(1, param);
 
 		param = new Param("realServer", null);
 		parameters.put(2, param);
 
 		OpSourceMethod method = new OpSourceMethod(provider,
 				provider.buildUrl(null,true, parameters),
 				provider.getBasicRequestParameters(OpSource.Content_Type_Value_Single_Para, "GET", null));
 
 		Document doc = method.invoke();
         String sNS = "";
         try{
             sNS = doc.getDocumentElement().getTagName().substring(0, doc.getDocumentElement().getTagName().indexOf(":") + 1);
         }
         catch(IndexOutOfBoundsException ex){}
 		NodeList matches = doc.getElementsByTagName(sNS + "realServer");
 		if(matches != null){
 			for( int i=0; i<matches.getLength(); i++ ) {
 				Node node = matches.item(i);
 				RealServer realServer = this.toRealServer(node);
 				if(realServer != null){
 					list.add(realServer);
 				}
 			}
 		}
 		return list;
 	}
 
 	private Iterable<RealServer> listRealServerInServerFarm(String networkId, String serverFarmId) throws CloudException, InternalException {
 		ArrayList<RealServer> list = new ArrayList<RealServer>();
 		if(networkId == null){
 			networkId = this.getNetworkIdFromServerFarmId(serverFarmId);
 			if(networkId == null){
 				throw new CloudException("No such server Farm");
 			}
 		}
 		Node farmNode = this.getServerFarmNode(networkId, serverFarmId);
 
 		if(farmNode == null){
 			throw new CloudException("Server Farm does not exist !!!");
 		}
 
 		NodeList matches = farmNode.getChildNodes();
 		for(int i = 0; i<matches.getLength(); i++){
 			Node attribute = matches.item(i);
 			if(attribute.getNodeType() == Node.TEXT_NODE) continue;
 
 			String name = attribute.getNodeName();
             String sNS = "";
             try{
                 sNS = name.substring(0, name.indexOf(":") + 1);
             }
             catch(IndexOutOfBoundsException ex){}
 			if(name.equalsIgnoreCase(sNS + "realServer")){
 				RealServer realServer = this.toRealServer(attribute);
 				if(realServer != null){
 					list.add(realServer);
 				}
 			}
 		}
 		return list;
 	}
 
 	private ArrayList<String> listTargetInServerFarm(String networkId, String serverFarmId, String targetTagName, String targetIdTagName) throws CloudException, InternalException{
 		if(networkId == null){
 			networkId = this.getNetworkIdFromServerFarmId(serverFarmId);
 			if(networkId == null){
 				throw new CloudException("No such server Farm");
 			}
 		}
 		Node farmNode = this.getServerFarmNode(networkId, serverFarmId);
 
 		if(farmNode == null){
 			throw new CloudException("Server Farm does not exist !!!");
 		}
 		ArrayList<String> list = new ArrayList<String>();
 		NodeList matches = farmNode.getChildNodes();
 
 		for(int i = 0; i<matches.getLength(); i++){
 			Node attribute = matches.item(i);
 			if(attribute.getNodeType() == Node.TEXT_NODE) continue;
 			String name = attribute.getNodeName();
             String sNS = "";
             try{
                 sNS = name.substring(0, name.indexOf(":") + 1);
             }
             catch(IndexOutOfBoundsException ex){}
 			if(name.equalsIgnoreCase(sNS + targetTagName)){
 				NodeList targetItems = attribute.getChildNodes();
 
 				for(int j=0; j< targetItems.getLength();j++){
 					Node item = targetItems.item(j);
 					String itemValue;
 					if(item.getNodeType() == Node.TEXT_NODE) continue;
 
 					if( item.getChildNodes().getLength() > 0 ) {
 						itemValue = item.getFirstChild().getNodeValue();
 					}else {
 						continue;
 					}
 					if(item.getNodeName().equalsIgnoreCase(sNS + targetIdTagName) ){
 						if(itemValue != null && !itemValue.equals("")){
 							list.add(itemValue);
 						}
 					}
 				}
 
 			}
 		}
 		return list;
 	}
 
 	private Iterable<String> listProbeIds(String networkId, String serverFarmId) throws CloudException, InternalException {
 		return listTargetInServerFarm(networkId, serverFarmId, "probe", "id");
 	}
 
 	private Iterable<Probe> listProbes(String networkId) throws CloudException, InternalException {
 		ArrayList<Probe> list = new ArrayList<Probe>();
 
 		if(networkId == null){
 			return Collections.emptyList();
 		}
 		HashMap<Integer, Param>  parameters = new HashMap<Integer, Param>();
 		Param param = new Param(OpSource.NETWORK_BASE_PATH, null);
 
 		parameters.put(0, param);
 
 		param = new Param(networkId, null);
 		parameters.put(1, param);
 
 		param = new Param("probe", null);
 		parameters.put(2, param);
 
 		OpSourceMethod method = new OpSourceMethod(provider,
 				provider.buildUrl(null,true, parameters),
 				provider.getBasicRequestParameters(OpSource.Content_Type_Value_Single_Para, "GET", null));
 
 		Document doc = method.invoke();
 
         String sNS = "";
         try{
             sNS = doc.getDocumentElement().getTagName().substring(0, doc.getDocumentElement().getTagName().indexOf(":") + 1);
         }
         catch(IndexOutOfBoundsException ex){}
 		NodeList matches = doc.getElementsByTagName(sNS + "Probe");
 		if(matches != null){
 			for( int i=0; i<matches.getLength(); i++ ) {
 				Node node = matches.item(i);
 				Probe probe = toProbe(node);
 				if(probe != null){
 					list.add(probe);
 				}
 			}
 		}
 		return list;
 	}
 
 
 	@Override
 	public Iterable<LoadBalancer> listLoadBalancers() throws CloudException, InternalException {
         APITrace.begin(provider, "LB.listLoadBalancers");
         try {
             /** Load balancer is based on network level */
             ArrayList<VLAN> networkList = (ArrayList<VLAN>) provider.getNetworkServices().getVlanSupport().listVlans();
 
             if(networkList == null){
                 return Collections.emptyList();
             }
 
             ArrayList<LoadBalancer> list = new ArrayList<LoadBalancer>();
             for(VLAN network : networkList){
                 ArrayList<LoadBalancer> newlist = (ArrayList<LoadBalancer>) listLoadBalancers(network.getProviderVlanId());
 
                 if(newlist != null){
                     list.addAll(newlist);
                 }
             }
             return list;
         }
         finally {
             APITrace.end();
         }
 	}
 
 	public Iterable<LoadBalancer> listLoadBalancers(String networkId) throws CloudException, InternalException {
         APITrace.begin(provider, "LB.listLoadBalancersOnNetwork");
         try {
             if(networkId == null){
                 return null;
             }
             ArrayList<LoadBalancer> list = new ArrayList<LoadBalancer>();
             HashMap<Integer, Param>  parameters = new HashMap<Integer, Param>();
             Param param = new Param(OpSource.NETWORK_BASE_PATH, null);
             parameters.put(0, param);
             param = new Param(networkId, null);
             parameters.put(1, param);
             param = new Param("vip", null);
             parameters.put(2, param);
 
             OpSourceMethod method = new OpSourceMethod(provider,
                     provider.buildUrl(null,true, parameters),
                     provider.getBasicRequestParameters(OpSource.Content_Type_Value_Single_Para, "GET", null));
 
             Document doc = method.invoke();
             String sNS = "";
             try{
                 sNS = doc.getDocumentElement().getTagName().substring(0, doc.getDocumentElement().getTagName().indexOf(":") + 1);
             }
             catch(IndexOutOfBoundsException ex){}
             NodeList matches = doc.getElementsByTagName(sNS + "vip");
             if(matches != null){
                 for( int i=0; i<matches.getLength(); i++ ) {
                     Node node = matches.item(i);
                     LoadBalancer balancer = toLoadBalancer(node, networkId );
                     if(balancer != null){
                         list.add(balancer);
                     }
                 }
             }
             return list;
         }
         finally {
             APITrace.end();
         }
 	}
 
 	/**
 	 *   //https://<Cloud API URL>/oec/0.9/{org-id}/network/{networkid}/
     probe/{probe-id}?delete
 	 */
 
 	public void removeProbe(String networkId, String probeId) throws InternalException, CloudException{
         APITrace.begin(provider, "LB.removeProbe");
         try {
             HashMap<Integer, Param>  parameters = new HashMap<Integer, Param>();
             Param param = new Param(OpSource.NETWORK_BASE_PATH, null);
 
             parameters.put(0, param);
 
             param = new Param(networkId, null);
 
             parameters.put(1, param);
 
             param = new Param("probe", null);
 
             parameters.put(2, param);
 
             param = new Param(probeId, null);
 
             parameters.put(3, param);
 
             OpSourceMethod method = new OpSourceMethod(provider,
                     provider.buildUrl("delete",true, parameters),
                     provider.getBasicRequestParameters(OpSource.Content_Type_Value_Single_Para, "GET", null));
 
             method.requestResult("Delete probe", method.invoke(), "result", "resultDetail");
         }
         finally {
             APITrace.end();
         }
 	}
 
 
 
 
 
 
 	private void removeAllProbeInServerFarm(String networkId, String serverFarmId) throws CloudException, InternalException{
 		ArrayList<String> probeIds = (ArrayList<String>) this.listProbeIds(networkId, serverFarmId);
 		for(String probeId : probeIds){
 			this.removeProbeFromServerFarm(networkId, probeId, serverFarmId);
 		}     	
 	}
 	private void removeAllRealServersInServerFarm(String networkId, String serverFarmId) throws CloudException, InternalException{
 		ArrayList<RealServer> realServers = (ArrayList<RealServer>) this.listRealServerInServerFarm(networkId, serverFarmId);
 		for(RealServer realServer : realServers){
 			this.removeRealServerFromServerFarm(networkId, realServer.getId(), realServer.getPort(), serverFarmId);    		
 		}
 	}
 
 	private void removeServerFarm(String networkId, String serverFarmId) throws InternalException, CloudException{
 
 		if(networkId == null){ 
 			networkId = this.getNetworkIdFromServerFarmId(serverFarmId);
 			if(networkId == null){    			
 				throw new CloudException("No network found for server Farm");    			
 			}   	
 		}
 		removeAllProbeInServerFarm(networkId, serverFarmId);
 
 		removeAllRealServersInServerFarm(networkId, serverFarmId);
 
 		HashMap<Integer, Param>  parameters = new HashMap<Integer, Param>();
 
 		Param param = new Param(OpSource.NETWORK_BASE_PATH, null);
 		parameters.put(0, param);
 
 		param = new Param(networkId, null);
 		parameters.put(1, param);
 
 		param = new Param("serverFarm", null);
 		parameters.put(2, param);
 
 		param = new Param(serverFarmId, null);
 		parameters.put(3, param);      	
 
 		OpSourceMethod method = new OpSourceMethod(provider, 
 				provider.buildUrl("delete",true, parameters),
 				provider.getBasicRequestParameters(OpSource.Content_Type_Value_Single_Para, "GET", null));
 
 		method.requestResult("Delete server Farm", method.invoke(), "result", "resultDetail");
 	}
 
     @Deprecated
 	@Override
 	public void remove(String loadBalancerId) throws CloudException, InternalException {
         APITrace.begin(provider, "LB.remove");
         try {
             if(loadBalancerId == null){
                 throw new CloudException(" load balancer is null");
             }
             String networkId = this.getNetworkIdFromLoadBalancerId(loadBalancerId);
             if( networkId == null ) {
                 throw new CloudException("Network Id not found while trying to terminating LB");
             }
 
             LoadBalancer lb = getLoadBalancer(networkId, loadBalancerId);
             if( lb == null ) {
                 throw new CloudException("No such load balancer");
             }
 
             String serverFarmId = getServerFarmIdFromLbId(networkId, loadBalancerId);
 
             if( serverFarmId == null ) {
                 logger.error("Cant not locate server farm " + serverFarmId + " while trying to delete lb");
                 throw new CloudException("Cant not locate server farm " + serverFarmId + " while trying to delete lb");
             }
 
             /**  Remove vip */
             removeVip(networkId, loadBalancerId);
             /** Remove Probe from server Farm */
             LbListener[] listeners = lb.getListeners();
             if(listeners != null){
                 for(LbListener listener : listeners ){
                     Probe probe = getProbe(networkId, listener.getNetworkProtocol(), lb.getAddress(), listener.getPublicPort());
 
                     if(probe != null){
                         /** remove from server farm */
                         removeProbeFromServerFarm(networkId, probe.getProbeId(), serverFarmId);
                         /**  remove probe */
                         try{
                             removeProbe(networkId, probe.getProbeId());
                         }
                         catch(Throwable e){
                             /**  Other balancer/server Farm was using this probe */
                             logger.error(e);
                         }
                     }
                 }
             }
             try{
                 /** Remove servers from real server and terminate the servers */
                 ArrayList<RealServer> list = (ArrayList<RealServer>) listRealServerInServerFarm(networkId, serverFarmId);
                 for(RealServer realServer : list){
                     /** Remove real server from server farm first */
                     removeRealServerFromServerFarm(networkId, realServer.getId(), realServer.getPort(), serverFarmId);
                     /** Remove the server? */
                     removeRealServer(networkId, realServer.getId());
                     /** Terminate? */
                     //provider.getComputeServices().getVirtualMachineSupport().terminate(realServer.getServerId());
 
                 }
                 /** Remove server Farm */
                 removeServerFarm(networkId, serverFarmId);
             }
             catch(Throwable e){
                 /** Other balancer/server Farm was using this realserver */
                 logger.error(e);
             }
         }
         finally {
             APITrace.end();
         }
 	}
 
 	@Override
 	public void removeDataCenters(String fromLoadBalancerId, String ... dataCenterIds) throws CloudException, InternalException {
 		throw new OperationNotSupportedException("These load balancers are not data center based.");
 	}
 
 
 	/**
 	 * https://<Cloud API URL>/oec/0.9/{org-id}/network/{networkid}/
 		serverFarm/{server-farm-id}/removeProbe
 	 * @param networkId
 	 * @param probeId
 	 * @param serverFarmId
 	 * @return
 	 * @throws InternalException
 	 * @throws CloudException
 	 */
 
 	private boolean removeProbeFromServerFarm(String networkId,String probeId, String serverFarmId) throws InternalException, CloudException{
 		if(probeId == null ||  serverFarmId == null){
 			return false;
 		}
 		HashMap<Integer, Param>  parameters = new HashMap<Integer, Param>();
 		Param param = new Param(OpSource.NETWORK_BASE_PATH, null);
 		parameters.put(0, param);
 
 		param = new Param(networkId, null);
 		parameters.put(1, param);
 
 		param = new Param("serverFarm", null);
 		parameters.put(2, param);
 
 		param = new Param(serverFarmId, null);
 		parameters.put(3, param);
 
 		param = new Param("removeProbe", null);
 		parameters.put(4, param);
 
 		/** Create post body */
 		String requestBody = "probeId=";
 		requestBody += probeId;
 
 		OpSourceMethod method = new OpSourceMethod(provider, 
 				provider.buildUrl(null,true, parameters),
 				provider.getBasicRequestParameters(OpSource.Content_Type_Value_Modify, "POST", requestBody));
 
 		return method.parseRequestResult("Remove probe from server farm", method.invoke(), "result", "resultDetail");
 	} 
 
 	/**
 	 * https://<Cloud API URL>/oec/0.9/{org-id}/network/{networkid}/
 serverFarm/{server-farm-id}/removeRealServer
 	 * @param networkId
 	 * @param realServerId
 	 * @param serverFarmId
 	 * @throws CloudException 
 	 * @throws InternalException 
 	 */
 
 	private String removeRealServerFromServerFarm(String networkId,String realServerId, int port,String serverFarmId) throws InternalException, CloudException{
 
 		if(realServerId == null &&  serverFarmId == null){
 			return null;
 		}
 
 		HashMap<Integer, Param>  parameters = new HashMap<Integer, Param>();
 		Param param = new Param(OpSource.NETWORK_BASE_PATH, null);
 
 		parameters.put(0, param);
 
 		param = new Param(networkId, null);
 
 		parameters.put(1, param);
 
 		param = new Param("serverFarm", null);
 
 		parameters.put(2, param);
 
 		param = new Param(serverFarmId, null);
 
 		parameters.put(3, param);
 
 		param = new Param("removeRealServer", null);
 
 		parameters.put(4, param);
 
 		//Create post body
 		String requestBody = "realServerId=";
 
 		requestBody += realServerId;
 
 		requestBody += "&realServerPort=" + port;
 
 		OpSourceMethod method = new OpSourceMethod(provider, 
 				provider.buildUrl(null,true, parameters),
 				provider.getBasicRequestParameters(OpSource.Content_Type_Value_Modify, "POST", requestBody));
 
 		return method.requestResult("Remove real server from server farm", method.invoke(), "result", "resultDetail");
 
 	}
 
 	/**https://<Cloud API URL>/oec/0.9/{org-id}/network/{networkid}/
     realServer/{rserver-id}?delete
 	 */
 	private String removeRealServer(String networkId,String realServerId) throws InternalException, CloudException{
 		if(realServerId == null){
 			return null;
 		}
 
 		HashMap<Integer, Param>  parameters = new HashMap<Integer, Param>();
 		Param param = new Param(OpSource.NETWORK_BASE_PATH, null);
 		parameters.put(0, param);
 
 		param = new Param(networkId, null);
 		parameters.put(1, param);
 
 		param = new Param("realServer", null);
 		parameters.put(2, param);
 
 		param = new Param(realServerId, null);
 		parameters.put(3, param);
 
 		OpSourceMethod method = new OpSourceMethod(provider, 
 				provider.buildUrl("delete",true, parameters),
 				provider.getBasicRequestParameters(OpSource.Content_Type_Value_Single_Para, "GET", null));
 
 		return method.requestResult("Remove real server ", method.invoke(), "result", "resultDetail");
 	} 
 
 	@Override
 	public void removeServers(String toLoadBalancerId, String ... serverIds) throws CloudException, InternalException {
         APITrace.begin(provider, "LB.removeServers");
         try {
             String networkId = this.getNetworkIdFromLoadBalancerId(toLoadBalancerId);
             if(networkId == null){
                 throw new CloudException("No network Id found for balancer while trying to remove servers from " + toLoadBalancerId);
             }
 
             String serverFarmId = getServerFarmIdFromLbId(networkId, toLoadBalancerId);
             if( serverFarmId == null ) {
                 throw new CloudException("No such load balancer: " + toLoadBalancerId);
             }
             try{
                 ArrayList<RealServer> list = (ArrayList<RealServer>) listRealServerInServerFarm(networkId, serverFarmId);
                 for(RealServer realServer : list){
                     for(String serverId : serverIds){
                         if(realServer.getServerId().equals(serverId)){
                             /** Remove real server from server farm first */
                             removeRealServerFromServerFarm(networkId, realServer.getId(), realServer.getPort(), serverFarmId);
                             /** Remove the server? */
                             removeRealServer(networkId, realServer.getId());
                             /** Terminate? */
                             //provider.getComputeServices().getVirtualMachineSupport().terminate(realServer.getServerId());
                         }
                     }
                 }
             }
             catch(Throwable e){
                 /** Other balancer/server Farm was using this realserver */
                 logger.error(e);
             }
         }
         finally {
             APITrace.end();
         }
 	}
 
 
 	private void removeVip(String networkId, String vipId) throws CloudException, InternalException {
 		if(vipId == null && networkId == null ){
 			logger.error("LB id or network of LB is null, (networkId, LB Id ) -> " + "(" +  networkId + "," + vipId  +")");
 			throw new CloudException("LB id or network of LB is null, (networkId, LB Id ) -> " + "(" +  networkId + "," + vipId  +")");
 		}
 
 		HashMap<Integer, Param>  parameters = new HashMap<Integer, Param>();
 		Param param = new Param(OpSource.NETWORK_BASE_PATH, null);
 		parameters.put(0, param);
 
 		param = new Param(networkId, null);
 		parameters.put(1, param);
 
 		param = new Param("vip", null);
 		parameters.put(2, param);
 
 		param = new Param(vipId, null);
 		parameters.put(3, param);       	
 
 		OpSourceMethod method = new OpSourceMethod(provider, 
 				provider.buildUrl("delete",true, parameters),
 				provider.getBasicRequestParameters(OpSource.Content_Type_Value_Single_Para, "GET", null));
 
 		method.requestResult("Remove LB from the network", method.invoke(), "result", "resultCode");
 	}
 	private LbProtocol guessLbProtocol(String value){
 		if(value.equalsIgnoreCase("TCP")){
 			return LbProtocol.RAW_TCP;    		
 		}
 		else if(value.equalsIgnoreCase("HTTP")){
 			return LbProtocol.HTTP; 
 		}
 		else if(value.equalsIgnoreCase("HTTPS")){
 			return LbProtocol.HTTPS; 
 		}else{
 			return LbProtocol.RAW_TCP;    		
 		}   	
 	}
 
 	private Probe toProbe(Node node){
 		if( node == null ) {
 			return null;
 		}
 		Probe probe = new Probe();
 		LbListener listener = new LbListener();            	  
 		listener.setNetworkProtocol(LbProtocol.RAW_TCP);
 		NodeList probeList = node.getChildNodes();
 		String probeId = null;
 		for(int j = 0; j< probeList.getLength(); j++){
 			Node probeItem = probeList.item(j);
 
 			if(probeItem.getNodeType() == Node.TEXT_NODE) continue;
 			String name = probeItem.getNodeName().toLowerCase();
 
             String sNS = "";
             try{
                 sNS = name.substring(0, name.indexOf(":") + 1);
             }
             catch(IndexOutOfBoundsException ex){}
 			if(name.equals(sNS + "id")){
 				probeId = probeItem.getFirstChild().getNodeValue();
 			}
 			else if(name.equals(sNS + "type")){
 				/** TCP, UDP, HTTP, HTTPs,ICMP */
 				listener.setNetworkProtocol(guessLbProtocol(probeItem.getFirstChild().getNodeValue()));
 			}
 			else if(name.equals(sNS + "port")){
 				listener.setPublicPort(Integer.parseInt(probeItem.getFirstChild().getNodeValue()));
 			}
 			else if(name.equals(sNS + "probeIntervalSeconds")){
 				//TODO
 			}
 		}
 		if(probeId != null && listener != null){
 			probe.setProbeId(probeId);
 			probe.setLbListener(listener);
 			return probe;
 		}
 		return null;    	
 	}
 
 	private RealServer toRealServer(Node node){
 		if( node == null ) {
 			return null;
 		}
 		RealServer realServer = new RealServer();
 
 		NodeList attributes = node.getChildNodes();
 
 		for(int j = 0; j< attributes.getLength(); j++){
 			Node attribute = attributes.item(j);
 
 			if(attribute.getNodeType() == Node.TEXT_NODE) continue;
 
 			String name = attribute.getNodeName();
 			String value = null;
 
 			if( attribute.getChildNodes().getLength() > 0 ) {
 				value = attribute.getFirstChild().getNodeValue();                
 			}
 			else {
 				continue;
 			}
 			//RealServer in server Farm
             String sNS = "";
             try{
                 sNS = name.substring(0, name.indexOf(":") + 1);
             }
             catch(IndexOutOfBoundsException ex){}
 			if(name.equalsIgnoreCase(sNS + "id")){
 				realServer.setId(value);    			
 			}
 			// The name is equal to the server Id
 			if(name.equalsIgnoreCase(sNS + "name")){
 				realServer.setServerId(value);   			
 			}
 			else if(name.equalsIgnoreCase(sNS + "ipAddress") || name.equalsIgnoreCase(sNS + "serverIp") ){
 				realServer.setIpAddress(value);  
 			}
 			else if(name.equalsIgnoreCase(sNS + "port")){
 				realServer.setPort(Integer.valueOf(value));    			
 			}
 			else if(name.equalsIgnoreCase(sNS + "inService")){
 				realServer.setService(Boolean.valueOf(value));
 			}
 			/** RealServer it self */
 			else if(name.equalsIgnoreCase(sNS + "serverId")){
 				realServer.setServerId(value);
 			}
 
 		}        
 		return realServer;   	  	
 	}
 
 	private void toLbListenerAndServerIds(Node node, LoadBalancer lb, String networkId, int publicPort) throws CloudException, InternalException{
 		if( node == null || networkId == null || lb == null) {
 			return ;
 		}
 		NodeList attributes = node.getChildNodes();
 		ArrayList<LbListener> listenerList= new ArrayList<LbListener>();
 		ArrayList<String> serverIds = new ArrayList<String>();
 		String privatePort = null;
 		String predictor = null;
         String sNS = "";
         try{
             sNS = node.getNodeName().substring(0, node.getNodeName().indexOf(":") + 1);
         }
         catch(IndexOutOfBoundsException ex){}
 		for( int i=0; i<attributes.getLength(); i++ ) {
 			Node attribute = attributes.item(i);
 			if(attribute.getNodeType() == Node.TEXT_NODE) continue;
 			String name = attribute.getNodeName().toLowerCase();
 
 			if(name.equalsIgnoreCase(sNS + "predictor") && attribute.getChildNodes().getLength() > 0) {
 				predictor = attribute.getFirstChild().getNodeValue();				
 			}
 			else if(name.equalsIgnoreCase(sNS + "probe") && attribute.getChildNodes().getLength() > 0) {
 				Probe probe = toProbe(attribute);
 				if(probe != null){
 					LbListener listener = probe.getLbListener();
 					if(listener != null){
 						/** Port from LB is public port*/
 						listener.setPublicPort(publicPort);
 						listenerList.add(listener);
 					}
 				}
 			}
 			else if( name.equalsIgnoreCase(sNS + "realServer") && attribute.getChildNodes().getLength() > 0 ) {
 				NodeList serverList = attribute.getChildNodes();
 
 				for(int j = 0; j< serverList.getLength(); j++){
 					Node serverItem = serverList.item(j);
 					if(serverItem.getNodeName().equals(sNS + "id") && serverItem.getChildNodes().getLength()>0){
 						serverIds.add(serverItem.getFirstChild().getNodeValue());
 					}
 					else if(serverItem.getNodeName().equals(sNS + "port") && serverItem.getChildNodes().getLength()>0){
 						/** Ports from server is private port*/
 						privatePort=  serverItem.getFirstChild().getNodeValue();						
 					}          
 				}
 			}
 		}
 		if(listenerList != null){			
 			if(privatePort != null){
 				for(LbListener listener: listenerList){
 					listener.setPrivatePort(Integer.valueOf(privatePort));
 				}
 			}			
 			if(predictor != null){
 				LbAlgorithm lbAlgorithm = convertPredictorToLbAlgorith(predictor);
 				for(LbListener listener: listenerList){
 					listener.setAlgorithm(lbAlgorithm);
 				}
 			}
 			lb.setListeners(listenerList.toArray(new LbListener[listenerList.size()]));
 		}
 		if(serverIds != null){
 			lb.setProviderServerIds(getServerIdfromRealServerId(networkId,serverIds.toArray(new String[serverIds.size()])));
 		}    	
 	}
 
 	private LoadBalancer toLoadBalancer(Node node, String networkId) throws InternalException, CloudException {
 		if( node == null ) {
 			return null;
 		}
 		NodeList attributes = node.getChildNodes();
 		LoadBalancer balancer = new LoadBalancer();
         String sNS = "";
         try{
             sNS = node.getNodeName().substring(0, node.getNodeName().indexOf(":") + 1);
         }
         catch(IndexOutOfBoundsException ex){}
 
 		balancer.setProviderRegionId(provider.getContext().getRegionId());
 		int publicPort = 80;
 		for( int i=0; i<attributes.getLength(); i++ ) {
 			Node attribute = attributes.item(i);
 			if(attribute.getNodeType() == Node.TEXT_NODE) continue;
 
 			String name = attribute.getNodeName().toLowerCase();
 			String value;
 
 			if( attribute.getChildNodes().getLength() > 0 ) {
 				value = attribute.getFirstChild().getNodeValue();                
 			}else {
 				continue;
 			}
 			if( name.equalsIgnoreCase(sNS + "id") ) {
 				balancer.setProviderLoadBalancerId(value);
 			}
 			else if( name.equalsIgnoreCase(sNS + "name") ) {
 				balancer.setName(value);
 			}
 			else if( name.equalsIgnoreCase(sNS + "ipAddress") ) {
 				balancer.setAddress(value);
 				balancer.setAddressType(LoadBalancerAddressType.IP);
 			}
 			else if( name.equalsIgnoreCase(sNS + "port") && value != null ) {
 				publicPort = Integer.parseInt(value);
 				balancer.setPublicPorts(new int[]{publicPort});
 			}
 			else if( name.equalsIgnoreCase(sNS + "protocol") && value != null ) {
 				//TODO
 			}
 			else if( name.equalsIgnoreCase(sNS + "vipTargetTyp") && value != null ) {
 				//TODO
 			}
 			else if( name.equalsIgnoreCase(sNS + "vipTargetId") && value != null ) {
 				Node serverFarmNode = getServerFarmNode(networkId, value);
 				if(serverFarmNode != null){
 					toLbListenerAndServerIds(serverFarmNode, balancer, networkId, publicPort);            		
 				}
 			}
 			else if( name.equalsIgnoreCase(sNS + "vipTargetName") && value != null ) {
 				//
 			}
 			else if( name.equalsIgnoreCase(sNS + "replyToIcmp") && value != null ) {
 				//
 			}
 			else if( name.equalsIgnoreCase(sNS + "inService") && value != null ) {
 				//
 			}
 		}
 		if( balancer.getProviderLoadBalancerId()== null ) {            
 			return null;
 		}
 		if( balancer.getName() == null ) {
 			balancer.setName(balancer.getProviderLoadBalancerId());
 		}
 		if( balancer.getDescription() == null ) {
 			balancer.setDescription(balancer.getName());
 		}
 		return balancer;
 	}
 
 	class Probe{
 		private LbListener listener;
 		private String probeId;
 		private String listenAddress;
 		Probe(){  		
 		}
 		LbListener getLbListener(){
 			return listener;
 		}
 		String getProbeId(){
 			return probeId;
 		}
 		String getListenAddress(){
 			return this.listenAddress;
 		}
 		void setListenAddress(String listenAddress){
 			this.listenAddress = listenAddress;
 		}
 		void setLbListener(LbListener lbListener){
 			this.listener = lbListener;
 		}
 		void setProbeId(String probeId){
 			this.probeId = probeId;
 		}    	
 	}
 	class RealServer{
 		private String id;
 		private String ipAddress;
 		private int port;
 		private boolean inService;
 		private String serverId;
 
 		RealServer(){
 			/** Default 80 */
 			port = 80; 
 			inService = true; 
 		}
 		String getId(){
 			return id;
 		}
 		String getIpAddress(){
 			return ipAddress;
 		}
 		int getPort(){
 			return port;
 		}
 		boolean getServiceStatus(){
 			return inService;
 		}
 		String getServerId(){
 			return serverId;
 		}
 
 		void setId(String id){
 			this.id = id;
 		}
 		void setIpAddress(String ipAddress){
 			this.ipAddress = ipAddress;
 		}
 		void setPort(int port){
 			this.port = port;
 		}
 		void setService(boolean inService ){
 			this.inService = inService;
 		}
 		void setServerId(String serverId){
 			this.serverId = serverId;
 		}
 	}   
 }
