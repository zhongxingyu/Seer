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
  * StreamFS release version 2.1
  */
 package local.rest.resources;
 
 import is4.*;
 import local.db.*;
 import local.rest.*;
 import local.rest.resources.util.*;
 import local.metadata.context.*;
 import local.rest.smap.*;
 import com.mongodb.*;
 
 import net.sf.json.*;
 
 import java.util.*;
 import java.util.logging.Logger;
 import java.util.logging.Level;
 import java.lang.StringBuffer;
 
 import com.sun.net.httpserver.*;
 import javax.naming.InvalidNameException;
 import java.io.*;
 
 /**
  *  Resource object for a device.
  */
 public class PublisherResource extends Resource{
 	protected static transient final Logger logger = Logger.getLogger(PublisherResource.class.getPackage().getName());
 	private static MySqlDriver database = (MySqlDriver)DBAbstractionLayer.database;
 	public UUID publisherId =null;
 	private static final int headCount = 1;
 
 	public final int TYPE = ResourceUtils.PUBLISHER_RSRC;
 	protected long last_data_ts = 0;
 
 	public PublisherResource(String path, UUID pubid) throws Exception, InvalidNameException{
 		super(path);
 		//if((database.isPublisher(pubid)) != null)
 		publisherId = pubid;
 
 		//set type
 		database.setRRType(URI, ResourceUtils.translateType(TYPE).toLowerCase());
 	}
 
 	public synchronized void get(HttpExchange exchange, boolean internalCall, JSONObject internalResp){
 		if(exchange.getAttribute("query") != null &&
 			((String) exchange.getAttribute("query")).equalsIgnoreCase("true")){
 			if(!internalCall)
 				exchange.setAttribute("query", "false");
 			query(exchange, null, internalCall, internalResp);
 			return;
 		}
 
 		logger.info("GET " + this.URI);
 		JSONObject response = new JSONObject();
 		try {
 			JSONObject properties = database.rrGetProperties(this.URI);
 			if(properties == null){
 				properties = new JSONObject();
 			}
 			properties.put("type", "properties");
 			response.put("status", "success");
 
 			UUID assocPubid = database.isRRPublisher(this.URI);
 			if(assocPubid != null){
 				if(publisherId == null)
 					publisherId = assocPubid;
 				response.put("pubid", assocPubid.toString());
 				logger.info("POPULATING");
 				//properties.put("PubId", assocPubid.toString());
 				//properties.put("URI", this.URI);
 				String dataStr = "URI: " + this.URI + "\n\nPubId: " + assocPubid.toString();
 				//properties.put("data", dataStr);
 
 				//get last few values received
 				//db.is4_main_coll.find().sort({timestamp:1}).limit(5);
 				JSONObject queryJSON = new JSONObject();
 				queryJSON.put("PubId", publisherId.toString());
 				queryJSON.put("timestamp", new Long(last_data_ts));
 				JSONObject sortByJSON = new JSONObject();
 				sortByJSON.put("timestamp",1);
 				MongoDBDriver mongoDriver = new MongoDBDriver();
 
 				long startTime = System.currentTimeMillis();
 				JSONObject lastValuesReceived = mongoDriver.queryWithLimit(
 									queryJSON.toString(), 
 									sortByJSON.toString(), 
 									headCount);
 				JSONArray headArray = lastValuesReceived.optJSONArray("results");
 				String head=JSONNull.getInstance().toString();
 				if(headArray!=null && headArray.size()>0){
 					try{
 						head = headArray.getJSONObject(0).toString();
 					} catch(Exception e){
 						logger.log(Level.INFO, "No JSONObject is publisher head", e);
 					}
 				}
 				long endTime = System.currentTimeMillis();
         			float seconds = (endTime - startTime) / 1000F;
 				logger.info("Total fetch time: " + seconds + " sec");
 				
 				//properties.put("head", lastValuesReceived.toString());
 				properties.put("head", head);
 			}
 			response.put("smap_url", database.getSmapUrl(publisherId));
 			response.put("smap_report_id", database.getSmapReportId(publisherId));
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
 		boolean srcalled = false;
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
 				String operation = dataObject.optString("operation");
 				if(operation!= null && operation.equals("")){
 					if(operation.equalsIgnoreCase("create_symlink")){
 						super.put(exchange, data, internalCall, internalResp);
 					} else {
 						super.handlePropsReq(exchange, data, internalCall, internalResp);
 					}
 					srcalled = true;
 				} else {
 					String type = (String) exchange.getAttribute("type");
 					UUID pubid = UUID.fromString((String) exchange.getAttribute("pubid"));
 					if(!internalCall){
 						exchange.setAttribute("type", "");
 						exchange.setAttribute("pubid", "");
 					}
 					if(type != null && pubid != null && 
 							type.equalsIgnoreCase("smap") && pubid.compareTo(publisherId)==0){
 
 						//store and send success
 						handleIncomingData(dataObject);
 						resp.put("status", "success");
 						sendResponse(exchange, 200, resp.toString(), internalCall, internalResp);
 						srcalled = true;
 					}
 				}
 			} catch(Exception e){
 				if(e instanceof JSONException){
 					errors.add("Invalid JSON");
 				}
 				resp.put("status", "fail");
 				resp.put("errors", errors);
 				sendResponse(exchange, 200, resp.toString(), internalCall, internalResp);
 			} finally {
 				try {
 					if (exchange !=null )
 						exchange.close();
 				} catch (Exception e) {
 					logger.log(Level.WARNING, "Issues closing: " + exchange.getLocalAddress().getHostName() + ": " +
 							exchange.getLocalAddress().getPort() + "->" + exchange.getRemoteAddress(), e);
 
 				}
 			}
 		}
 	}
 
 	public void delete(HttpExchange exchange, boolean internalCall, JSONObject internalResp){
 
 		logger.info("Handling DELETE PUBLISHER command for " + this.URI + ", " + this.publisherId);
 
 		//reset properties
 		JSONObject emptyProps = new JSONObject();
 		super.updateProperties(emptyProps);
 		
 		//deleted smap report
 		SmapConnector smapConnector = new SmapConnector();
 		
 		
 		/*delete the report; 
 		if it's the last publisher in a bulk report, the report will
 		be deleted, otherwise it only this resource will be deleted and the
 		report will remain*/
 		SmapConnector.deleteReport(this.publisherId, this.URI);
 		
 		//remove association with device
 		database.removeDeviceEntry(this.URI);
 
 		//delete entry from publishers table
 		database.removePublisher(this.publisherId);
 
 		//remove publisher from registrar
 		Registrar registrar = Registrar.registrarInstance();
 		registrar.unregisterDevice(this.publisherId.toString());
 
 		//delete rest_resource entry
 		database.removeRestResource(this.URI);
 		RESTServer.removeResource(this);
 		
 		//remove subscriptions to this publisher
 		SubMngr submngr = SubMngr.getSubMngrInstance();
		submngr.pubRemoved(exchange, internalCall, internalResp, publisherId.toString());
 
 		//remove from internal representation
 		this.metadataGraph.removeNode(this.URI);
 
 		sendResponse(exchange, 200, null, internalCall, internalResp);
 
 		
 	}
 
 	protected void handleIncomingData(JSONObject data){
 		//Registrar registrar = Registrar.registrarInstance();
 
 		//add timestamp
 		Date date = new Date();
 		long timestamp = date.getTime()/1000;
 		data.put("timestamp", timestamp);
 		
 		String dataStr = data.toString().replace('$', '_');
 		data = (JSONObject) JSONSerializer.toJSON(dataStr);
 		logger.info("data replace:" + dataStr);
 
 		//Forward to subscribers
 		JSONObject dataCopy = (JSONObject)JSONSerializer.toJSON(data);
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
 
 		logger.info("Publsher PUTTING in data repository");
 
 		//put the data entry in the database
 		//database.putInDataRepository(data, publisherId, alias);
 		database.updateLastRecvTs(URI, timestamp);
 
 		//store in the mongodb repos
 		mongoDriver.putEntry(dataCopy);
 		last_data_ts = timestamp;
 	}
 
 	public JSONObject queryTimeseriesRepos(JSONObject queryJson){
 		JSONObject queryResults = new JSONObject();
 		try{
 			//MongoDBDriver mongoDriver = new MongoDBDriver();
 
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
 
 	public void query(HttpExchange exchange, String data, boolean internalCall, JSONObject internalResp){
 		JSONObject resp = new JSONObject();
 		JSONArray errors = new JSONArray();
 		resp.put("path", URI);
 		try{
 			JSONObject tsQueryObj = new JSONObject();
 
 			//get query object from input data
 			logger.info("data: " + data);
 			if(data != null && !data.equals("") && !data.equals("{}")){	
 				JSONObject dataJsonObj = (JSONObject) JSONSerializer.toJSON(data);
 				JSONObject dataTsQuery = dataJsonObj.optJSONObject("ts_query");
 				if(tsQueryObj!=null && dataTsQuery!=null)
 					tsQueryObj.putAll(dataTsQuery);
 			}
 
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
 						tsQueryObj.put(queryKey, conditions);
 					} else{
 						if(isNumber(queryValue)){
 							long val = Long.parseLong(queryValue);
 							tsQueryObj.put(queryKey, val);
 						} else {
 							tsQueryObj.put(queryKey, queryValue);
 						}
 					}
 
 				} else if(thisKey.startsWith("ts")){
 					String queryValue = exchangeJSON.optString(thisKey);
 
 					JSONObject conditions = Resource.genJSONClause(queryValue);
 					if(conditions!=null) {
 						tsQueryObj.putAll(conditions);
 					} else {
 						if(isNumber(queryValue)){
 							long val = Long.parseLong(queryValue);
 							tsQueryObj.put(thisKey, val);
 						} else {
 							logger.warning("Invalid conditions set for generic props query");
 						}
 					}
 				}
 			}
 
 			logger.fine("Timeseries Query: " + tsQueryObj.toString());
 
 			if(!tsQueryObj.toString().equals("{}")){
 				tsQueryObj.put("is4_uri", URI);
 				if(last_props_ts>0 /*&& !tsQueryObj.containsKey("timestamp")*/ && !exchangeJSON.containsKey("query"))
 					tsQueryObj.put("timestamp", last_props_ts);
 
 				JSONObject mqResp = queryTimeseriesRepos(tsQueryObj);
 				logger.fine("mqResp: " + mqResp.toString());
 				resp.put("ts_query_results", mqResp);
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
 		exchangeJSON.clear();
 		sendResponse(exchange, 200, resp.toString(), internalCall, internalResp);
 	}
 
 }
