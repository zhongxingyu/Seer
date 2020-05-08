 package net.floodlightcontroller.hand;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import net.floodlightcontroller.packet.IPv4;
 import org.restlet.resource.Get;
 import org.restlet.resource.Post;
 import org.restlet.resource.Delete;
 import org.restlet.resource.ServerResource;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.codehaus.jackson.JsonParseException;
 import org.codehaus.jackson.JsonParser;
 import org.codehaus.jackson.JsonToken;
 import org.codehaus.jackson.map.MappingJsonFactory;
 
 public class HANDGangliaHostsResource extends ServerResource {
 	public static Logger logger = LoggerFactory.getLogger(HANDGangliaHostsResource.class);
 	
 	
 	@Get("json")
 	public Object handleRequest(){
 		IHANDService hand = 
 				(IHANDService)getContext().getAttributes().
 				get(IHANDService.class.getCanonicalName());
 		
 		String status = null;
 		
 		if(hand.isHANDEnabled()){
 			return hand.getHosts();
 		}
 		else{
 			status = "Please enable Host Aware Networking Decisions";
 			return ("{\"status\" : \"" + status + "\"}");
 		}
 	}
 	
 	/**
 	 * 
 	 * @param hostJson
 	 * @return
 	 */
 	@Post
 	public String addHost(String hostJson){
 		IHANDService hand = 
 					(IHANDService)getContext().getAttributes().
 					get(IHANDService.class.getCanonicalName());
 			
 		HANDGangliaHost host;
 		String status = null;
 			
 		try{
 			host = jsonToGangliaHost(hostJson);
 		}
 		catch(IOException e){
 			logger.error("Error parsing Ganglia host to JSON: {}, Error: {}", hostJson, e);
     		e.printStackTrace();
     		return "{\"status\" : \"Error! Could not parse Ganglia host, see log for details.\"}";
 		}
 		if(hostExists(host, hand.getHosts())){
 			status = "Error!, This host already exists!";
 			logger.error(status);
 		}
 		else{
 			if(hand.isHANDEnabled()){
 				
 				/**Required Field Checks**/
 				//hostname and ip is required. b/c Ganglia stores RRD's by them.
 				if(host.hostName == null || host.ipAddress == 0){
 					status = "{\"status\" : \"Must provide Hostname and IP Address.\"}";
 				}
 				else if(host.cluster == null){
 					logger.info("Fetching Cluster Information...");
 					if(existsInAnyCluster(host)){
 						//Send to HAND Module for addition.
 						hand.addGangliaHost(host);
 						status = "{\"status\" : \"Adding host to Host Aware Network\"}";
 					}else{
 						status = "{\"status\" : \"Host does not exist in any cluster\"}";
 					}
 				}
 				//else just add it, it has enough information
 				else{
 					/** Should add a check cluster method here?**/
 					hand.addGangliaHost(host);
 					status = "{\"status\" : \"Adding host to Host Aware Network\"}";
 				}
 				/**
 				 * Cluster will be add by existsInAnyCluster() if host RRD is found.
 				 * 
 				 * MAC,and First Hop Switch will be added later b/c
 				 * Floodlight will already has this info if not provided.
 				 */
 			}
 		}
 		return "{\"status\" : "+ status +"}";
 	 
 	 }
 	 
 	/**
 	 * 
 	 * @param hostJson
 	 * @return
 	 */
 	 @Delete
 	 public String deleteHost(String hostJson){
 		 
 		//TODO
 		 /**
 		  * 
 		  *  !!!!!!!!!!!!!!!!!!!!
 		  * 
 		  * 
 		  */
 		 
 		return null;
 		 
 	 }
 
 	private static HANDGangliaHost jsonToGangliaHost(String hostJson) throws IOException{
 		HANDGangliaHost host = new HANDGangliaHost();
 		
 		MappingJsonFactory jsonFactory = new MappingJsonFactory();
 		JsonParser parser;
 		
 		
 		try{
 			parser = jsonFactory.createJsonParser(hostJson);
 		}catch(JsonParseException e){
 			throw new IOException (e);
 		}
 		
 		JsonToken token = parser.getCurrentToken();
 		if(token != JsonToken.START_OBJECT){
 			parser.nextToken();
 			if(parser.getCurrentToken() != JsonToken.START_OBJECT){
 				logger.error("did not recieve json start token, current token is: {}"
 						+ parser.getCurrentToken());
 			}
 		}
 		//Start parsing
 		while(parser.nextToken() != JsonToken.END_OBJECT){
 			if(parser.getCurrentToken() != JsonToken.FIELD_NAME){
     			throw new IOException("FIELD_NAME expected");
     		}
     		
     		try{
     			String name = parser.getCurrentName();
     			parser.nextToken();
     			
     			//current text in parser
     			String jsonText = parser.getText();
     			
     			logger.info("JSON Parser text is: {}", jsonText); //DEBUG
     			
     			if(jsonText.equals("")){
     				//ignore empty string, return to loop
     				continue;
     			}
     			else if(name == "host_name"){
     				host.hostName = jsonText;
     			}
     			else if(name == "ip_address"){
     				host.ipAddress = IPv4.toIPv4Address(jsonText);
     				
     			}
     			else if(name == "domain"){
     				host.domain = jsonText;
     			}
     			else if(name == "mac_address"){
     				host.macAddress = Long.parseLong(jsonText);
     			}
     			else if(name == "cluster"){
     				host.cluster = jsonText;
     			}
     			//adds a list of SwitchDPIDs
     			//ex: {"first_hops":"123456789,987654321"}
     			else if(name == "first_hops"){
     				if(jsonText.contains(",")){
     					String[] hops = jsonText.split(",");
     					for(String s : hops){
     						host.firstHops.add(Long.valueOf(s));
     					}
     				}else{
     					//only one first hop
     					//ex: {"first_hops":"123456789"}
     					host.firstHops.add(Long.valueOf(jsonText));
     				}
     			}
     			
     		}catch(JsonParseException e){
     			logger.debug("Error getting current FIELD_NAME {}", e);
     		}catch(IOException e){
     			logger.debug("Error procession Json {}", e);
     		}
 		}
 		//Return the HANDGangliaHost Object to the GangliaHostResource
 		return host;
 	}
 	
 	/**
 	 * 
 	 * @param host
 	 * @param hostList
 	 * @return
 	 */
 	public static boolean hostExists(HANDGangliaHost host,
 			ArrayList<HANDGangliaHost> hostList){
 		
 		Iterator<HANDGangliaHost> hostIter = hostList.iterator();
 		while(hostIter.hasNext()){
 			HANDGangliaHost h = hostIter.next();
 			if(host.isSameAs(h) || host.macAddress == h.macAddress){
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	/**
 	 * Checks to make sure a cluster exists for this Host
 	 * By searching the RRD directory given in properties
 	 * and making sure the host RRD exists within a cluster directory.
 	 * @param host
 	 * @return
 	 */
 	public static boolean existsInAnyCluster(HANDGangliaHost host){
 		//if found
 		boolean found = false;
 		
 		//Get the consumer to get basepath.
 		MetricConsumer aConsumer = new MetricConsumer();
 		String dir = aConsumer.metricPath;
 		
 		String fqdn;
 		if(host.domain == "" && !(host.hostName.contains("."))){
 			logger.info("hostname seems to not have a domain, adding .local");
 			host.domain = ".local";
 			fqdn = host.hostName+host.domain;
 		}else{
 			logger.info("hostname seems to contain it's domain name");
 			fqdn = host.hostName;
 		}
 		
 		File containingPath = new File(dir);
 		logger.info("Searching for clusters in: {}", containingPath.getPath()); //debug
 		for( File child : containingPath.listFiles()){
 			//check if directory, clusters are listed as directories in Unix File Systems
 			if(child.isDirectory() && !(child.toString().contains("unspecified")) 
 					&& !(child.toString().contains("__SummaryInfo__")) ){
 				logger.info("Checking Cluster: {}", child.toString()); //debug
 				String[] strDirs = child.toString().split("/");
 				String curDir = strDirs[strDirs.length-1];
 				for(File rrd : child.listFiles()){
 					String[] strRRDs = rrd.toString().split("/");
 					String compareRRD = strRRDs[strRRDs.length-1];
 					logger.debug("Comparing : "+fqdn+" or "+IPv4.fromIPv4Address(
 							host.ipAddress).toLowerCase()+ "  to {}", compareRRD ); //DEBUG
 					if(compareRRD.contains(fqdn.toLowerCase())){
 						found = true;
 						//this is the current directory 
 						//being searched.
 						host.cluster = curDir;
 						break;
 					}
 					else {
 						new String();
 						if(compareRRD.contains(IPv4.fromIPv4Address(
 								host.ipAddress).toLowerCase())){
 							found = true;
 							//We only want what ganglia can see.
 							host.hostName = "";
 							//set the host's cluster if found
 							//this is the current directory 
 							//being searched.
 							host.cluster = curDir;
 							break;
 						}
 					}
 				}
 			}
 			
 		}
 		return found;
 	}
 
 }
