 /*
  * "Copyright (c) 2010-11 The Regents of the University  of California. 
  * All rights reserved.
  *
  * Permission to use, copy, modify, and distribute this software and its
  * documentation for any purpose, without fee, and without written agreement is
  * hereby granted, provided that the above copyright notice, the following
  * two paragraphs and the author appear in all copies of this software.
  *
  * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR
  * DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT
  * OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF
  * CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
  * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
  * AND FITNESS FOR A PARTICULAR PURPOSE.  THE SOFTWARE PROVIDED HEREUNDER IS
  * ON AN "AS IS" BASIS, AND THE UNIVERSITY OF CALIFORNIA HAS NO OBLIGATION TO
  * PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS."
  *
  * Author:  Jorge Ortiz (jortiz@cs.berkeley.edu)
  * IS4 release version 2.0
  */
 package local.rest.resources;
 
 import local.analytics.*;
 import is4.*;
 import local.db.*;
 import local.rest.*;
 import local.rest.resources.util.*;
 import local.metadata.context.*;
 import com.mongodb.*;
 
 import net.sf.json.*;
 
 import java.util.*;
 import java.util.logging.Logger;
 import java.util.logging.Level;
 import java.lang.StringBuffer;
 
 import com.sun.net.httpserver.*;
 import javax.naming.InvalidNameException;
 import java.io.*;
 import java.net.*;
 
 /**
  *  Resource object for a device.
  */
 public class GenericPublisherResource extends Resource{
 	protected static transient Logger logger = Logger.getLogger(GenericPublisherResource.class.getPackage().getName());
 	protected static MySqlDriver database = (MySqlDriver)DBAbstractionLayer.database;
 	public UUID publisherId =null;
 	protected static final int headCount = 5;
 
 	//public static int TYPE = ResourceUtils.GENERIC_PUBLISHER_RSRC;
 	protected long last_data_ts = 0;
 
     private ObjectInputStream routerIn = null;
     private ObjectOutputStream routerOut = null;
 
 	public GenericPublisherResource(String uri, UUID pubId) throws Exception, InvalidNameException{
 		super(uri);
 		if (pubId != null)
 			publisherId = pubId;
 		else
 			throw new Exception("Null pointer to pubId");
 
 		//set type to generic_publisher
 		TYPE=ResourceUtils.GENERIC_PUBLISHER_RSRC;
 		database.setRRType(URI, ResourceUtils.translateType(TYPE).toLowerCase());
 	}
 
     public UUID getPubId(){
         return publisherId;
     }
 
 	public synchronized void get(HttpExchange exchange, boolean internalCall, JSONObject internalResp){
 		
 		if(exchange.getAttribute("query") != null &&
 			((String) exchange.getAttribute("query")).equalsIgnoreCase("true")){
 			if(!internalCall)
 				exchange.setAttribute("query", "false");
 			query(exchange, null, internalCall, internalResp);
 			return;
 		}
 
         if(exchangeJSON.containsKey("incident_paths")){
             super.get(exchange, internalCall, internalResp);
             return;
         }
 		
 		logger.info("GET " + this.URI);
 		JSONObject response = new JSONObject();
 		try {
 			JSONObject properties = database.rrGetProperties(this.URI);
 			if(properties == null){
 				properties = new JSONObject();
 			}
 			//properties.put("type", "properties");
 			response.put("status", "success");
 
 			UUID assocPubid = database.isRRPublisher2(this.URI);
 			if(assocPubid != null){
 				response.put("pubid", assocPubid.toString());
 				logger.info("POPULATING");
 				//properties.put("PubId", assocPubid.toString());
 				//properties.put("URI", this.URI);
 				//String dataStr = "URI: " + this.URI + "\n\nPubId: " + assocPubid.toString();
 				//properties.put("data", dataStr);
 
 				//get last few values received
 				//db.is4_main_coll.find().sort({timestamp:1}).limit(5);
 				JSONObject queryJSON = new JSONObject();
 				//queryJSON.put("PubId", publisherId.toString());
 				//queryJSON.put("timestamp", new Long(last_data_ts));
 				queryJSON.put("pubid", publisherId.toString());
 				queryJSON.put("ts", new Long(last_data_ts));
 				JSONObject sortByJSON = new JSONObject();
 				sortByJSON.put("timestamp",1);
 				//MongoDBDriver mongoDriver = new MongoDBDriver();
 				JSONObject lastValuesReceived = mongoDriver.queryWithLimit(
 									queryJSON.toString(), 
 									sortByJSON.toString(), 
 									headCount);
                 JSONArray res = lastValuesReceived.optJSONArray("results");
                 if(res!=null && res.size()>0)
                     lastValuesReceived = (JSONObject) res.get(0);
                 else
                     lastValuesReceived = new JSONObject();
 				response.put("head", lastValuesReceived.toString());
 			}
 			response.put("properties", properties);
 			sendResponse(exchange, 200, response.toString(), internalCall, internalResp);
 			return;
 		} catch (Exception e){
 			logger.log(Level.WARNING, "", e);
 		}
 		sendResponse(exchange, 200, null, internalCall, internalResp);
 	}
 
 	public synchronized void put(HttpExchange exchange, String data, boolean internalCall, JSONObject internalResp){
 		post(exchange, data, internalCall, internalResp);
 	}
 
 	public synchronized void post(HttpExchange exchange, String data, boolean internalCall, JSONObject internalResp){
 		logger.info("Publisher handling PUT/POST data request");
 		if(exchange.getAttribute("query") != null &&
 		((String) exchange.getAttribute("query")).equalsIgnoreCase("true")){
 			if(!internalCall)
 				exchange.setAttribute("query", "false");
 			query(exchange, data, internalCall, internalResp);
 		} else {
 			JSONObject resp = new JSONObject();
 			JSONArray errors = new JSONArray();
 			try{
 				JSONObject dataObject = (JSONObject) JSONSerializer.toJSON(data);
 				logger.info("data: " + dataObject.toString());
 				String operation = dataObject.optString("operation");
 				if(operation!= null && !operation.equals("")){
 					if(operation.equalsIgnoreCase("create_symlink")){
 						super.put(exchange, data, internalCall, internalResp);
 					} else {
 						super.handlePropsReq(exchange, data, internalCall, internalResp);
 					}
 				}else {
 					String type = (String) exchange.getAttribute("type");
                     UUID pubid = null;
                     try {
 					    pubid = UUID.fromString((String) exchange.getAttribute("pubid"));
                     } catch(Exception e){
                         logger.warning("\"pubid\" was not set");
                     }
 					String addts = (String) exchange.getAttribute("addts");
 
                     if(pubid!=null)
 				        logger.info("type: " + type +"; pubid: " + pubid.toString());
 
 					if(!internalCall){
 						exchange.setAttribute("pubid", "");
 						exchange.setAttribute("type", "");
 					}
 					
 					if(type != null && pubid != null &&  !type.equals("") && !pubid.equals("") &&
 							type.equalsIgnoreCase("generic") && pubid.compareTo(publisherId)==0){
 
 						//store and send success
 						if(addts != null && !addts.equals("") && addts.equalsIgnoreCase("false"))
 							handleIncomingData(dataObject, false);
 						else
 							handleIncomingData(dataObject, true);
 						resp.put("status", "success");
 						sendResponse(exchange, 200, resp.toString(), internalCall, internalResp);
 					} else {
 						resp.put("status", "fail");
 						if(type == null || type.equalsIgnoreCase(""))
 							errors.add("type parameter missing");
 						if(pubid == null || pubid.equals(""))
 							errors.add("pubid parameter missing");
 						if(type != null)
 							errors.add("Unknown type");
 						if(pubid !=null && pubid.compareTo(publisherId) != 0)
 							errors.add("pubid does not match that of this generic publisher");
 						resp.put("errors", errors);
 						sendResponse(exchange, 200, resp.toString(), internalCall, internalResp);
 					}
 				}
 			} catch(Exception e){
 				e.printStackTrace();
 				if(e instanceof JSONException){
 					errors.add("Invalid JSON");
 				}
 				resp.put("status", "fail");
 				resp.put("errors", errors);
 				sendResponse(exchange, 200, resp.toString(), internalCall, internalResp);
 			}
 		}
 	}
 
 	public void delete(HttpExchange exchange,boolean internalCall, JSONObject internalResp){
 
 		logger.info("Handling DELETE PUBLISHER command for " + this.URI);
 
 		//reset properties
 		JSONObject emptyProps = new JSONObject();
 		super.updateProperties(emptyProps);
 
 		//remove association with device
 		database.removeDeviceEntry(this.URI);
 
 		//delete entry from publishers table
 		database.removePublisher(this.publisherId);
 
 		//delete rest_resource entry
 		database.removeRestResource(this.URI);
 		RESTServer.removeResource(this);
 		
 		//remove subscriptions to this publisher
 		SubMngr submngr = SubMngr.getSubMngrInstance();
		submngr.pubRemoved(exchange, true, internalResp, publisherId.toString());
 
 		//remove from internal graph
 		this.metadataGraph.removeNode(this.URI);
 
 		sendResponse(exchange, 200, null, internalCall, internalResp);
 		
 	}
 
 	protected void handleIncomingData(JSONObject data, boolean addTimestamp){
 
 		long timestamp;
 		if(addTimestamp || !data.containsKey("ts")){
 			//add timestamp
 			Date date = new Date();
 			timestamp = date.getTime()/1000;
 			data.put("ts", timestamp);
 			logger.info("adding ts: " + timestamp);
 		} else {
 			try {
 				timestamp = data.getLong("ts");
 			} catch(Exception e){
 				logger.log(Level.WARNING, "", e);
 				timestamp = 0L;
 			}
 		}
 		data.put("pubid", publisherId.toString());
 
 		//Forward to subscribers
 		String dataStr = data.toString();
 		dataStr = dataStr.replace("$","d_");
 		JSONObject dataCopy = (JSONObject)JSONSerializer.toJSON(dataStr);
 	
 		dataCopy.put("timestamp", timestamp);
 		dataCopy.put("PubId", publisherId.toString());
 		dataCopy.put("is4_uri", this.URI.toString());
 		SubMngr submngr = SubMngr.getSubMngrInstance();
 		logger.info("SubMngr Copy: " + dataCopy.toString());
 		submngr.dataReceived(dataCopy);
 
 		//get the alias associated with this publisher
 		String alias = null;
 		if(URI.endsWith(publisherId.toString() + "/") ||
 				URI.endsWith(publisherId.toString())){
 			alias = publisherId.toString();
 		} else {
 			String thisuri = URI;
 			if(thisuri.endsWith("/"))
 				thisuri = thisuri.substring(0, thisuri.length()-1);
 			alias = thisuri.substring(thisuri.lastIndexOf("/"), thisuri.length());
 		}
 
         //forward up the olap graph
         JSONObject properties = database.rrGetProperties(this.URI);
         String unitsStr = properties.optString("units");
         if(dataCopy.containsKey("timestamp")){
             Long ts = dataCopy.getLong("timestamp");
             dataCopy.remove("timestamp");
             dataCopy.put("ts", ts);
         }
         
         if(dataCopy.containsKey("value")){
             Double v = dataCopy.getDouble("value");
             dataCopy.remove("value");
             dataCopy.put("v", v);
         } else if(dataCopy.containsKey("val")){
             Double v = dataCopy.getDouble("val");
             dataCopy.remove("val");
             dataCopy.put("v", v);
         }
 
         if(RESTServer.tellRouter && !unitsStr.equals(""))
            metadataGraph.streamPush(URI, unitsStr, dataCopy.toString()); 
 
 		logger.info("Publsher PUTTING in data repository");
 
 		//put the data entry in the database
 		//database.putInDataRepository(data, publisherId, alias);
 		database.updateLastRecvTs(URI, timestamp);
 
 		//store in the mongodb repos
 		//MongoDBDriver mongod = new MongoDBDriver();
 		//mongod.putEntry(dataCopy);
 		mongoDriver.putTsEntry(data);
 		last_data_ts = timestamp;
 	}
 
     public void setRouterCommInfo(String routerHost, int routerPort){
         try {
             Socket s = new Socket(InetAddress.getByName(routerHost), routerPort);
             routerOut = new ObjectOutputStream(s.getOutputStream());
             routerOut.flush();
             routerIn = new ObjectInputStream(s.getInputStream());
         } catch(Exception e) {
             logger.log(Level.SEVERE, "", e);
             System.exit(1);
         }
     }
 
 	public JSONObject queryTimeseriesRepos(JSONObject queryJson){
 		JSONObject queryResults = new JSONObject();
 		try{
 			//only run the query for this publisher
 			queryJson.put("PubId", publisherId.toString());
 			
 			//remove the PubId key from the results
 			JSONObject keys = new JSONObject();
 			keys.put("PubId",0);
 
 			logger.info("QUERY: " + queryJson.toString() + "\nKEYS:  " + keys.toString());
 
 			JSONObject queryR = mongoDriver.query(queryJson.toString(), keys.toString());
 			if(queryR != null)
 				queryResults.putAll(queryR);
 		} catch(Exception e){
 			logger.log(Level.WARNING, "", e);
 		}
 		return queryResults;
 	}
 
 	public JSONArray queryTimeseriesRepos2(JSONObject queryJson){
 		JSONArray queryResults = new JSONArray();
 		try{
 
 			//only run the query for this publisher
 			queryJson.put("pubid", publisherId.toString());
 
 			//remove the PubId key from the results
 			JSONObject keys = new JSONObject();
 			keys.put("pubid",0);
 
 			logger.info("QUERY: " + queryJson.toString() + "\nKEYS:  " + keys.toString());
 
 			return mongoDriver.queryTsColl(queryJson.toString(), keys.toString());
 		} catch(Exception e){
 			logger.log(Level.WARNING, "", e);
 		}
 		return queryResults;
 	}
 
 	public void query(HttpExchange exchange, String data, boolean internalCall, JSONObject internalResp){
 		JSONObject resp = new JSONObject();
 		JSONArray errors = new JSONArray();
 		resp.put("path", URI);
 		try{
 			//JSONObject tsQueryObj = new JSONObject();
 			JSONObject tsQueryObj2 = new JSONObject();
 		
 			//get query object from input data
 			if(data != null && !data.equals("")){	
 				JSONObject dataJsonObj = (JSONObject) JSONSerializer.toJSON(data);
 				JSONObject dataTsQuery = dataJsonObj.optJSONObject("ts_query");
 				//tsQueryObj.putAll(dataTsQuery);
 				tsQueryObj2.putAll(dataTsQuery);
 			}
 
             logger.fine("query->exchangeJSON::" + exchangeJSON.toString());
 			Iterator keys = exchangeJSON.keys();
 			Vector<String> attributes = new Vector<String>();
 			Vector<String> values = new Vector<String>();
 			while(keys.hasNext()){
 				String thisKey = (String) keys.next();
 				logger.fine("Keys found!; thisKey=" + thisKey);
 				if(thisKey.startsWith("ts_")){
 					String str = "ts_";
 					String queryKey = thisKey.substring(thisKey.indexOf(str)+str.length(), thisKey.length());
 					String queryValue = exchangeJSON.optString(thisKey);
 
 					logger.info("Query Value: " + queryValue);
 
 					JSONObject conditions = Resource.genJSONClause(queryValue);
 					logger.info("Conditions: " + conditions);
 					if(conditions!=null){
 						//tsQueryObj.put(queryKey, conditions);
 						if(queryKey.equalsIgnoreCase("timestamp"))
 							tsQueryObj2.put("ts", conditions);
 					} else{
 						if(isNumber(queryValue)){
 							long val = Long.parseLong(queryValue);
 							//tsQueryObj.put(queryKey, val);
 							if(queryKey.equalsIgnoreCase("timestamp"))
 								tsQueryObj2.put("ts", val);
 						} else {
 							//tsQueryObj.put(queryKey, queryValue);
 							if(queryKey.equalsIgnoreCase("timestamp"))
 								tsQueryObj2.put("ts", queryValue);
 						}
 					}
 
 				} else if(thisKey.startsWith("ts")){
 					String queryValue = exchangeJSON.optString(thisKey);
 
 					JSONObject conditions = Resource.genJSONClause(queryValue);
 					if(conditions!=null){
 						//tsQueryObj.putAll(conditions);
 						tsQueryObj2.putAll(conditions);
 					} else{
 						if(isNumber(queryValue)){
 							long val = Long.parseLong(queryValue);
 							//tsQueryObj.put(thisKey, val);
 							if(thisKey.equalsIgnoreCase("timestamp"))
 								tsQueryObj2.put("ts", queryValue);
 							else
 								tsQueryObj2.put(thisKey, val);
 						} else {
 							logger.warning("Invalid conditions set for generic props query");
 						}
 					}
 						
 				}
 			}
 
 			//logger.fine("Timeseries Query: " + tsQueryObj.toString());
 			logger.fine("Timeseries Query2: " + tsQueryObj2.toString());
 
 			if(!tsQueryObj2.toString().equals("{}")){
 				//tsQueryObj.put("is4_uri", URI);
 				/*if(last_props_ts>0)
 					tsQueryObj.put("timestamp", last_props_ts);*/
 
 				//JSONObject mqResp = queryTimeseriesRepos(tsQueryObj);
 				JSONArray mqResp2 = queryTimeseriesRepos2(tsQueryObj2);
 				//logger.fine("mqResp: " + mqResp.toString());
 				logger.fine("mqResp2: " + mqResp2.toString());
 				resp.put("ts_query_results", mqResp2);
 			} else {
 				errors.add("TS Query Error: Empty or invalid query");
 				logger.warning(errors.toString());
 				resp.put("errors", errors);
 			}
 		} catch (Exception e){
 			logger.log(Level.WARNING, "", e);
 			if(e instanceof JSONException){
 				errors.add("Invalid JSON for POST data; url params ignored");
 				resp.put(errors, errors);
 				sendResponse(exchange, 200, resp.toString(), internalCall, internalResp);
 				return;
 			}
 		}
 		JSONObject propsQueryResultsBuffer = new JSONObject();
 		super.query(exchange, data, true, propsQueryResultsBuffer);
 		resp.put("props_query_results", propsQueryResultsBuffer);
 		sendResponse(exchange, 200, resp.toString(), internalCall, internalResp);
 		exchangeJSON.clear();
 	}
 
 }
