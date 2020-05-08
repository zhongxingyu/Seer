 package net.floodlightcontroller.hand;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import org.codehaus.jackson.JsonParseException;
 import org.codehaus.jackson.JsonParser;
 import org.codehaus.jackson.JsonToken;
 import org.codehaus.jackson.map.MappingJsonFactory;
 import org.restlet.resource.Delete;
 import org.restlet.resource.Get;
 import org.restlet.resource.Post;
 import org.restlet.resource.ServerResource;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class HANDRulesResource extends ServerResource {
 public static Logger logger = LoggerFactory.getLogger(HANDGangliaHostsResource.class);
 	
 	
 	@Get("json")
 	public Object handleRequest(){
 		IHANDService hand = 
 				(IHANDService)getContext().getAttributes().
 				get(IHANDService.class.getCanonicalName());
 		
 		String status = null;
 		
 		if(hand.isHANDEnabled()){
 			return hand.getRules();
 		}
 		else{
 			status = "Please enable Host Aware Networking Decisions";
 			return ("{\"status\" : \"" + status + "\"}");
 		}
 	}
 	
 	/**
 	 * 
 	 * @param ruleJson
 	 * @return
 	 */
 	@Post
 	public String addRule(String ruleJson){
 		IHANDService hand = 
 					(IHANDService)getContext().getAttributes().
 					get(IHANDService.class.getCanonicalName());
 			
 		HANDRule rule;
 		String status = null;
 			
 		try{
 			rule = jsonToGangliaRule(ruleJson);
 		}
 		catch(IOException e){
 			logger.error("Error parsing rule to JSON: {}, Error: {}", ruleJson, e);
     		e.printStackTrace();
     		return "{\"status\" : \"Error! Could not parse rule, see log for details.\"}";
 		}
 		if(ruleExists(rule, hand.getRules())){
 			status = "Error!, This rule already exists!";
 			logger.error(status);
 		}
 		else{
 			if(hand.isHANDEnabled()){
 				
 				/**
 				 * Add Code to handle checks
 				 * e.g 
 				 * 
 				 * is firewall rule, is the firewall enabled?
 				 * is a qos rule, is QoS enabled?
 				 * is the polling time good? make sense?
 				 * 
 				 * *Host Assoc is check in json parse
 				 * *ActionType is checked in json parse
 				 * *Metrics are parsed in json parse
 				 * 
 				 * 
 				 * Then add the rule
 				 * 
 				 * TODO !!!!!!**************
 				 */
 			}
 		}
 		return "{\"status\" : "+ status +"}";
 	 
 	 }
 	 
 	/**
 	 * 
 	 * @param ruleJson
 	 * @return
 	 */
 	 @Delete
 	 public String deleteRule(String ruleJson){
 		 
 		//TODO
 		 /**
 		  * 
 		  *  !!!!!!!!!!!!!!!!!!!!
 		  * 
 		  * 
 		  */
 		 
 		return null;
 		 
 	 }
 
 	private static HANDRule jsonToGangliaRule(String ruleJson) throws IOException{
 		HANDRule rule = new HANDRule();
 		
 		MappingJsonFactory jsonFactory = new MappingJsonFactory();
 		JsonParser parser;
 		
 		
 		try{
 			parser = jsonFactory.createJsonParser(ruleJson);
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
     			else if(name == "rule_name"){
     				rule.name = jsonText;
     			}
     			else if(name == "priority"){
     				//TODO
     			}
     			else if(name == "polling_time"){
     				//TODO
     			}
     			else if(name == "active"){
     				//TODO
     			}
     			else if(name == "check_limit"){
     				//TODO
     			}
     			else if(name == "attached_to"){
     				//TODO given host X, get the ID from the list of
     				//hosts known by HAND
     			}
     			else if(name == "metrics"){
    			//"metrics":{"<m1>":"<threshold>"}....(list assumes "and" )
     		    //TODO store key value pars in a json string.
     			//Call parseMetrics
     			//Return HashMap<String, HANDThreshold>
     			//e.g a {"load_one":"gt-5"} should result in a 
     			// rule that tracks Load of 1 Min that triggers when it is >5 Load/Procs
     			//Also, make sure ganglia can see this metric
     			//TODO checkGangliaMetrics(HashMap<String, HANDThreshold> metrics)
     				
     			}
     			else if(name == "action_type"){
     				//TODO
     				/** Can be one of the following from
     				 * net.floodlightcontroller.hand.HandRule.java
     				 *  RPRT,	//Report threshold is reported
 						AFR, 	//Add Firewall Rule
 						DFW, 	//Delete Firewall Rule
 						LB, 	//LoadBalance (placeholder for now //TODO )
 						PSF, 	//Push Static Flow
 						AQOS, 	//Add QoS Rule
 						DQOS,	//Delete QoS Rule
 						KR,		//Kill Route
     				 */
     				//TODO call boolean checkActionType from HandRule
     				// change the string to ALL CAPS.
     			}
     			else if(name == "action_string"){
     				//TODO
     				/**
     				 * This is an odd one, depending on the
     				 * ActionType, we will need different information from
     				 * the actionString. I.E a Adding a Firewall Rule needs
     				 * specific info rather than Pushing a Static Flow
     				 */
     				//TODO Call parseActionString(rule.action, rule.actionString)
     				//parsing the action string first looks at the ActionType,
     				//depending on the ActionType, the functions will determine 
     				//what information it needs to call to the correct
     				//controller API and checl the action_string if has given the
     				//correct information via key:value pairs.
     				rule.actionString = jsonText;
     			}
     			
     			
     		}catch(JsonParseException e){
     			logger.debug("Error getting current FIELD_NAME {}", e);
     		}catch(IOException e){
     			logger.debug("Error procession Json {}", e);
     		}
 		}
 		//Return the HANDGangliaHost Object to the GangliaHostResource
 		return rule;
 	}
 	
 	/**
 	 * 
 	 * @param rule
 	 * @param ruleList
 	 * @return
 	 */
 	public static boolean ruleExists(HANDRule rule,
 			ArrayList<HANDRule> ruleList){
 		
 		Iterator<HANDRule> ruleIter = ruleList.iterator();
 		while(ruleIter.hasNext()){
 			HANDRule r = ruleIter.next();
 			if(rule.isSameAs(r) || rule.name == r.name ||
 					rule.equals(r)){
 				return true;
 			}
 		}
 		
 		return false;
 	}
 
 }
