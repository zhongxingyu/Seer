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
 
 import local.db.*;
 import local.rest.*;
 import local.rest.resources.util.*;
 import local.rest.interfaces.*;
 import local.metadata.context.*;
 import is4.*;
 import is4.stats.SFSStatistics;
 
 import net.sf.json.*;
 
 import java.net.*;
 import java.util.*;
 import java.util.logging.Logger;
 import java.util.logging.Level;
 import java.lang.StringBuffer;
 import java.util.zip.GZIPOutputStream;
 
 import com.sun.net.httpserver.*;
 import javax.naming.InvalidNameException;
 import java.io.*; 
 import java.util.regex.Pattern;
 
 public class Resource extends Filter implements HttpHandler, Serializable, Is4Resource{
 
 	protected static transient final Logger logger = Logger.getLogger(Resource.class.getPackage().getName());
 	protected String URI = null;
 	protected static HttpServer httpServer = null;
 	protected static MySqlDriver database = (MySqlDriver) DBAbstractionLayer.database;
 	protected static MongoDBDriver mongoDriver = new MongoDBDriver();
 	protected static SFSStatistics sfsStats = null;
 
 	//Query structures	
 	protected JSONObject exchangeJSON = new JSONObject();
 	protected static final String[] props_ops = {"props_like", "props_and", "props_or", "props_not"};
 	protected static final String[] ts_ops = {"ts_gt", "ts_lt", "ts_ge", "ts_le"};
 	protected static Vector<String> propsOpsVec = new Vector<String>();
 	protected static Vector<String> tsOpsVec = new Vector<String>();
 	protected long last_props_ts = 0;
 
 	protected static MetadataGraph metadataGraph = null;
 
 	public int TYPE= ResourceUtils.DEFAULT_RSRC;
 
 	//This addresses the HttpContext switch bug
 	//For every call to create a context in the httpServer after the root, the HttpContext object changes, and the filter
 	//is no longer used in the new object.
 	protected HttpContext thisContext = null;
 
     protected String objectIdentifier = null;
     protected String versionNumber = null;
 
 	public Resource(String path) throws Exception, InvalidNameException {
 		resourceSetup(path);
 		if(sfsStats == null)
 			sfsStats = SFSStatistics.getInstance(mongoDriver.openConn());
 	}
 
 	public Resource(){
 		initQueryStructs();
 	}
 	
 	public static void setMetadataGraph(MetadataGraph mgraph){
 		metadataGraph = mgraph;
 	}
 
 	public static void removeFromMetadataGraph(String uri){
 		metadataGraph.removeNode(uri);
 	}
 
 	protected void resourceSetup(String path) throws Exception, InvalidNameException{
 		if(path.indexOf("?") > 0 || path.indexOf("&") > 0)
 			throw new Exception("Invalid path string: No '&' or '?' allowed OR path already exists");
 
 		if(!path.endsWith("/"))
 			path += "/";
 		URI = path;
 	
 		//save if already in database	
 		if(!((MySqlDriver)DBAbstractionLayer.database).rrPathExists(path))
 			((MySqlDriver)DBAbstractionLayer.database).rrPutPath(URI);
 
 		//set last_props_ts
 		last_props_ts = database.getLastPropsTs(URI);
 		if(last_props_ts==0 && mongoDriver.getPropsHistCount(URI)>0){
 			logger.info("Fetching oldest properties values");
 			last_props_ts = mongoDriver.getMaxTsProps(URI);
 			JSONObject propsEntry = mongoDriver.getPropsEntry(URI, last_props_ts);
 			propsEntry.remove("_id");
 			propsEntry.remove("timestamp");
 			propsEntry.remove("is4_uri");
             propsEntry.remove("_keywords");
 			database.rrPutProperties(URI, propsEntry);
 			database.updateLastPropsTs(URI, last_props_ts);
 		}
 		
 
 		initQueryStructs();
 	}
 
 	public void initQueryStructs(){
 		if(propsOpsVec.size()==0){
 			int i =0;
 			for(i=0; i<props_ops.length; i++)
 				propsOpsVec.addElement(props_ops[i]);
 			for(i=0; i<ts_ops.length; i++)
 				tsOpsVec.addElement(ts_ops[i]);
 		}
 	}
 
 	public String getURI(){
 		return URI;
 	}
 
 	public int getType(){
 		return TYPE;
 	}
 
     public JSONObject getProperties(){
         return database.rrGetProperties(URI);
     }
 
 	//////////////// REST-accessible functions ////////////////
 	public void get(HttpExchange exchange, boolean internalCall, JSONObject internalResp){
 		try {
 			if(exchangeJSON.containsKey("query") && 
 				((String)exchangeJSON.getString("query")).equalsIgnoreCase("true") &&
 				exchange.getRequestURI().toString().contains("props_")){
 				logger.info("Handling PROPERTIES query");
 				query(exchange, null, internalCall, internalResp);
 			} 
             
             else if(exchangeJSON.containsKey("agg") && exchangeJSON.containsKey("units")){
                 logger.info("OLAP_QUERY: aggregate data query");
                 boolean queryQ = exchangeJSON.containsKey("query");
                 String queryVal = exchangeJSON.optString("query");
                 queryQ &= (queryVal!=null && queryVal.equalsIgnoreCase("true"));
                 if(queryQ){
                     handleTSAggQuery(exchange, null, internalCall, internalResp);
                 } else {
                     String aggStr = exchangeJSON.getString("agg");
                     String units = exchangeJSON.getString("units");
                     String queryRes =metadataGraph.queryAgg(URI, aggStr, units, null);
                     if(queryRes != null){
                         JSONObject response = (JSONObject)JSONSerializer.toJSON(queryRes);
                         sendResponse(exchange, 200, response.toString(), internalCall, internalResp);
                     } else {
                         sendResponse(exchange, 200, null, internalCall, internalResp);
                     }
                 }
             } 
             
             else {
 				logger.fine("GETTING RESOURCES: " + URI);
 				JSONObject response = new JSONObject();
 				JSONArray subResourceNames = ((MySqlDriver)(DBAbstractionLayer.database)).rrGetChildren(URI);
 				logger.fine(subResourceNames.toString());
 				response.put("status", "success");
 				response.put("type", ResourceUtils.translateType(TYPE));
 				response.put("properties", database.rrGetProperties(URI));
 				findSymlinks(subResourceNames);
 				response.put("children",subResourceNames);
 				sendResponse(exchange, 200, response.toString(), internalCall, internalResp);
 			}
 		} catch (Exception e) {
 			logger.log(Level.WARNING, "Error while responding to GET request",e);
 		} /*finally {
 			try {
 				if(exchange !=null && !internalCall){
 					exchange.getRequestBody().close();
 					exchange.getResponseBody().close();
 					exchange.close();
 				}
 			} catch(Exception e){
 				logger.log(Level.WARNING, "Trouble closing exchange in Resource", e);
 			}
 		}*/
 	}
 
 	public void put(HttpExchange exchange, String data, boolean internalCall, JSONObject internalResp){
 		try{
 			logger.info("PUT " + this.URI);
 			if(data != null){// && !isScriptRequest(exchange)){
 				JSONObject dataObj = (JSONObject) JSONSerializer.toJSON(data);
 				String op = dataObj.optString("operation");
 
 				logger.info("op=" + op);
 
 				String resourceName = dataObj.optString("resourceName");
 				if(op.equalsIgnoreCase("create_resource") && !resourceName.equals("") &&
 						ResourceUtils.putResource(this.URI, dataObj) ){
 
 					String myPath = this.URI;
 					if(!myPath.endsWith("/"))
 						myPath += "/";
 					metadataGraph.addNode(myPath + resourceName);
 
 					sendResponse(exchange, 201, null, internalCall, internalResp);
 				}
 
 				else if (op.equalsIgnoreCase("create_generic_publisher") &&
 					!dataObj.optString("resourceName").equals("")){
 					UUID newPubId = this.createPublisher(dataObj.optString("resourceName"));
 
 					if(newPubId != null && metadataGraph.addNode(this.URI + dataObj.optString("resourceName"))){
 						JSONObject response = new JSONObject();
 						response.put("status", "success");
 						response.put("is4_uri", 
 								this.URI + dataObj.optString("resourceName"));
 						response.put("PubId", newPubId.toString());
 						sendResponse(exchange, 201, response.toString(), internalCall, internalResp);
 					} else {
 						sendResponse(exchange, 500, null, internalCall, internalResp);
 					}
 				}
 
 				else if(op.equalsIgnoreCase("create_symlink")) {
 					String name = dataObj.optString("name");
 					String is4url = dataObj.optString("url");
 					String localUri = dataObj.optString("uri");
 
 					if(!name.equals("") && !localUri.equals("") && RESTServer.isResource(localUri)){
 						SymlinkResource symlink = new SymlinkResource(URI + name + "/", localUri);
 						RESTServer.addResource(symlink);
 
 						//revert the change if the new addition introduces a cycle/loop in the metadata graph
 						JSONObject resp =new JSONObject();
 						JSONArray errors = new JSONArray();
 						JSONObject internal = new JSONObject();
 						if(!metadataGraph.addNode(symlink.getURI())){
 							symlink.loopDelete();
 							errors.add("possible cycle on adding: " + symlink.getURI());
 							resp.put("status", "fail");
 							resp.put("errors", errors);
 							sendResponse(exchange, 500, resp.toString(), internalCall, internalResp);
 						} else {
 							logger.info("added symlink: " + metadataGraph.addNode(URI + name + "/"));
 							sendResponse(exchange, 201, null, internalCall, internalResp);
 						}
 					}
 
 					else if(!name.equals("") && !is4url.equals("")){
 						try{
 							URL is4URL = new URL(is4url);
 							SymlinkResource symlink = new SymlinkResource(URI + name + "/", is4URL);
 							RESTServer.addResource(symlink);
 							logger.info("added symlink: " + metadataGraph.addNode(URI + name + "/"));
 							sendResponse(exchange, 201, null, internalCall, internalResp);
 						} catch(Exception e){
 							logger.log(Level.WARNING, "", e);
 							sendResponse(exchange, 400, "Invalid URL format", internalCall, internalResp);
 						}
 					}
 					
 					else {
 						sendResponse(exchange, 400, 
 								"Missing name, url, and/uri fields; Invalid request", 
 								internalCall, internalResp);
 					}
 				}	
 
 				else if( op.equalsIgnoreCase("create_smap_publisher")){
 					JSONArray errors = new JSONArray();
 					JSONObject newUris = new JSONObject();
 					JSONObject deviceRequest = new JSONObject();
 			
 					//get device name
 					StringTokenizer tokenizer = new StringTokenizer(this.URI, "/");
 					int idx=0;
 					int numTokens = tokenizer.countTokens();
 					String devName = null;
 					while (idx != numTokens-1){
 						tokenizer.nextToken();
 						idx++;
 					}
 					devName = tokenizer.nextToken();
 
 					logger.info("operation: create_smap_publisher, devname="+ devName + " type=" + ResourceUtils.translateType(this.TYPE));
 
 					//populate the add-smap publisher request
 					boolean smapurlsSet = false;
 					deviceRequest.put("deviceName", devName);
 					if(dataObj.optJSONArray("smap_urls") != null){
 						deviceRequest.put("smap_urls", dataObj.optJSONArray("smap_urls"));
 						smapurlsSet = true;
 					} else if(!dataObj.optString("smap_url").equals("")){
 						deviceRequest.put("smap_url", dataObj.optString("smap_url"));
 						smapurlsSet = true;
 					}
 
 					if(!dataObj.optString("alias").equals("")){
 						deviceRequest.put("alias", dataObj.optString("alias"));
 					} else if(dataObj.optJSONArray("aliases") != null) {
 						deviceRequest.put("aliases", dataObj.optJSONArray("aliases"));
 					}
 		
 					logger.info("Data Object:" + dataObj.toString());	
 					logger.info("Device Request:" + deviceRequest.toString());	
 					//check if smap urls are set	
 					if(smapurlsSet){
 						DevicesResource.addSmapPublishersToDevice(this.URI, deviceRequest, errors, newUris);
 					} else {
 						errors.add("smap_url(s) not set in request");
 					}
 
 					//send response
 					JSONObject resp = new JSONObject();
 					if(errors.size()>0){
 						resp.put("status", "fail");
 						resp.put("errors", errors.toString());
 						sendResponse(exchange, 200, resp.toString(), internalCall, internalResp);
 					} else {
 						resp.put("status", "success");
 						resp.put("new_pubs", newUris);
 						sendResponse(exchange, 200, resp.toString(), internalCall, internalResp);
 					}
 				}
 								
 				else {
 					sendResponse(exchange, 200, null, internalCall, internalResp);
 				}
 				return;
 			}
 
 		} catch (Exception e){
 			logger.log(Level.WARNING, "Request document not in proper JSON format", e);
 			sendResponse(exchange, 500, null, internalCall, internalResp);
 			return;
 		} finally {
 			try {
 				if(exchange != null){
 					exchange.getRequestBody().close();
 					exchange.getResponseBody().close();
 					exchange.close();
 				} 
 			} catch(Exception e){
 				logger.log(Level.WARNING, "Trouble closing exchange in Resource", e);
 			}
 		}
 
 		//no content
 		sendResponse(exchange, 204, null, internalCall, internalResp);
 	}
 
 	private boolean isScriptRequest(HttpExchange exchange){
 		boolean scriptReq = false;
 		scriptReq = exchange.getAttribute("script") != null && 
 				((String) exchange.getAttribute("script")).equalsIgnoreCase("true");
 		if(scriptReq)
 			exchange.setAttribute("script", "");
 		return true;
 	}
 
 	public void post(HttpExchange exchange, String data, boolean internalCall, JSONObject internalResp){
 		logger.info("POST called: " + exchange.getRequestURI().toString());
 		try {
 			if(exchangeJSON.containsKey("query")  && 
 					exchangeJSON.getString("query").equalsIgnoreCase("true") &&
 					exchange.getRequestURI().toString().contains("props_")){
 				logger.info("Handling PROPERTIES query");
 				query(exchange, data, internalCall, internalResp);
 			}
 			else if(exchangeJSON.containsKey("query")  && 
 					exchangeJSON.getString("query").equalsIgnoreCase("true")) {
 				query(exchange, data, internalCall, internalResp);
 			} 
             else if(exchangeJSON.containsKey("set_agg") && exchangeJSON.containsKey("units")){
                 String setAggStr = exchangeJSON.getString("set_agg");
                 String unitsStr = exchangeJSON.getString("units");
                 boolean setAggBool = false;
                 if(setAggStr.equalsIgnoreCase("true"))
                     setAggBool = true;
                 if(setAggBool && !unitsStr.equals("")){
                     metadataGraph.setAggPoint(URI, unitsStr, true);
                     logger.info("Set aggregation point: [pathname=" + URI + 
                             ", set_agg=" + setAggStr + ", units=" + unitsStr + "]"); 
                 }
 
                 //add it to the aggregation buffer array in the properties for this resource
                 JSONObject currentProps = database.rrGetProperties(URI);
                 boolean containsAggBufs = currentProps.containsKey("aggBufs");
 
                logger.info("CONTAINS AGGBUFS::" + containsAggBufs);
                 if(setAggBool && containsAggBufs){
                     JSONArray aggBufsArray = currentProps.getJSONArray("aggBufs");
                     if(!aggBufsArray.contains(unitsStr)){
                         aggBufsArray.add(unitsStr);
                         currentProps.put("aggBufs", aggBufsArray);
                         database.rrPutProperties(URI, currentProps);
                         updateProperties(currentProps);
                     }
                 } else if(!setAggBool && containsAggBufs) {
                     JSONArray aggBufsArray = currentProps.getJSONArray("aggBufs");
                     if(aggBufsArray.contains(unitsStr)){
                         metadataGraph.setAggPoint(URI, unitsStr, false);
                         aggBufsArray.remove(unitsStr);
                         currentProps.put("aggBufs", aggBufsArray);
                         database.rrPutProperties(URI, currentProps);
                         updateProperties(currentProps);
                     }
                 } else if(setAggBool && !containsAggBufs){
                     JSONArray aggBufsArray = new JSONArray();
                     aggBufsArray.add(unitsStr);
                     currentProps.put("aggBufs", aggBufsArray);
                     database.rrPutProperties(URI, currentProps);
                     updateProperties(currentProps);
                 }
                 sendResponse(exchange, 200, null, internalCall, internalResp);
             } 
 			else {
 				handlePropsReq(exchange, data, internalCall, internalResp);
 				//sendResponse(exchange, 200, null, internalCall, internalResp);
 			}
 		} catch(Exception e){
 			logger.log(Level.WARNING, "", e);
 		} /*finally {
 			try {
 				if(exchange != null){
 					exchange.getRequestBody().close();
 					exchange.getResponseBody().close();
 					exchange.close();
 				} 
 			} catch(Exception e){
 				logger.log(Level.WARNING, "Trouble closing exchange in Resource", e);
 			}
 		}*/
 
 	}
 
 	public void delete(HttpExchange exchange, boolean internalCall, JSONObject internalResp){
 		try {
 			logger.info("Handling DELETE command for " + this.URI);
 			JSONArray children = database.rrGetChildren(this.URI);
 			if(children.size()==0){
 				//reset properties
 				JSONObject emptyProps = new JSONObject();
 				updateProperties(emptyProps);
 				//delete rest_resource entry
 				database.removeRestResource(this.URI);
 				RESTServer.removeResource(this);
 
 				//remove from internal graph
 				metadataGraph.removeNode(this.URI);
 				
 				sendResponse(exchange, 200, null, internalCall, internalResp);
 			} else {
 				JSONObject response = new JSONObject();
 				JSONArray errors = new JSONArray();
 				response.put("status", "fail");
 				errors.add("Cannot delete resource with children");
 				response.put("errors", errors);
 				sendResponse(exchange, 200, response.toString(), internalCall, internalResp);
 			} 
 		} catch(Exception e){
 			logger.log(Level.WARNING, "", e);
 		} finally {
 			try {
 				if(exchange != null){
 					exchange.getRequestBody().close();
 					exchange.getResponseBody().close();
 					exchange.close();
 				} 
 			} catch(Exception e){
 				logger.log(Level.WARNING, "Trouble closing exchange in Resource", e);
 			}
 		}
 
 	}
 
 
 	//////////////// HttpHandler function implemention ////////////////
 	public synchronized void handle(HttpExchange exchange){
 		logger.info("exchange handler: " + exchange.getLocalAddress().getHostName() + ":" + exchange.getLocalAddress().getPort() + "->" +
 					exchange.getRemoteAddress());
 		try {
 			//get the uri and remove the parameters
 			String eUri = exchange.getRequestURI().toString();
 			if(eUri.contains("?"))
 				eUri = eUri.substring(0, eUri.indexOf("?"));
 
 			if(!eUri.contains("*")){
 
 				//does not have to be an exact match if the request is to a symlink resource
 				//otherwise the request MUST match the uri of this resource EXACTLY
 				logger.info("Invoked handle TYPE=" + ResourceUtils.translateType(TYPE));
 				if(TYPE != ResourceUtils.SYMLINK_RSRC){
 					String URI2 = null;
 					if(this.URI.endsWith("/"))
 						URI2 = this.URI.substring(0, this.URI.length()-1);
 					else 
 						URI2 = this.URI + "/";
 					logger.info("URI=" + this.URI + "\nURI2=" + URI2 + "\nREQ_URI=" + exchange.getRequestURI());
 					String myUri = null;
 					if(eUri.contains("?")){
 						myUri = eUri.substring(0, eUri.indexOf("?"));
 					} else {
 						myUri = eUri;
 					}
 
 					if((!myUri.equalsIgnoreCase(this.URI) && !myUri.equalsIgnoreCase(URI2)) ||
 							!isActiveResource(this.URI)){
 						sendResponse(exchange, 404, null, false, null);
 						return;
 					}
 				}
 				//This addresses the HttpContext switch bug in the library
 				//The filter must be called BEFORE the handler
 				if (exchange.getHttpContext() != thisContext && exchange.getHttpContext().getFilters().size()==0) {
 					this.parseParams(exchange);
 					thisContext = exchange.getHttpContext();
 					thisContext.getFilters().add(this);
 					logger.warning("HttpContext switch bug in the httpserver library");
 				}
 
 				try {
 					String requestMethod = exchange.getRequestMethod();
 					if(requestMethod.equalsIgnoreCase("get")){
 						logger.info("handling GET");
 						sfsStats.incGet();
 						this.get(exchange, false, null);
 						return;
 					} else if (requestMethod.equalsIgnoreCase("put")){
 						logger.info("handling PUT");
 						sfsStats.incPut();
 						String obj = getPutPostData(exchange);
 						this.put(exchange, obj, false, null);
 						sfsStats.docReceived(obj);
 						return;
 					} else if (requestMethod.equalsIgnoreCase("post")) {
 						logger.info("handling POST");
 						sfsStats.incPost();
 						String obj = getPutPostData(exchange);
 						this.post(exchange, obj, false, null);
 						sfsStats.docReceived(obj);
 						return;
 					} else if (requestMethod.equalsIgnoreCase("delete")) {
 						logger.info("handling DELETE");
 						sfsStats.incDelete();
 						this.delete(exchange, false, null);
 						return;
 					}
 				} catch (Exception e){
 					logger.log(Level.WARNING, "Could not carry out the Filter operation", e);
 				}
 			} else {
 				//This address the HttpContext switch bug in the library
 				//The filter must be called BEFORE the handler
 				if (exchange.getHttpContext() != thisContext && exchange.getHttpContext().getFilters().size()==0) {
 					this.parseParams(exchange);
 					thisContext = exchange.getHttpContext();
 					thisContext.getFilters().add(this);
 				}
 
 				handleRecursiveFSQuery(exchange, false, null);//, myUri);
 			}
 		} catch(Exception e){
 			logger.log(Level.WARNING, "",e);
 		} finally {
 			try {
 				if(exchange !=null){
 					exchange.getRequestBody().close();
 					exchange.close();
 				} 
 			} catch(Exception e){
 				logger.log(Level.WARNING, "Trouble closing exchange in Resource", e);
 			}
 		}
 	}
 
 	public String description(){
 		StringBuffer strBuf = new StringBuffer().append("Filter GET and POST requests for ").append(URI);
 		return strBuf.toString();
 	}
 
 	public synchronized void doFilter(HttpExchange exchange, Filter.Chain chain) throws IOException {
 		boolean paramsOk = false;
 		if((paramsOk = parseParams(exchange)) && chain==null)
 			this.handle(exchange);
 		else if (!paramsOk) 
 			sendResponse(exchange, 404, null, false, null);
 		else
 			chain.doFilter(exchange);
 	}
 
 	protected synchronized boolean parseParams(HttpExchange exchange) {
 		logger.info("Request URI: " + exchange.getRequestURI().toString());
 		exchangeJSON.clear();
 		exchangeJSON.put("header", exchange.getRequestHeaders());
 		StringTokenizer tokenizer = new StringTokenizer(exchange.getRequestURI().toString(), "?");
 		if(tokenizer != null && tokenizer.hasMoreTokens()){
 			String thisResourcePath = tokenizer.nextToken();
 			if(URI == null && !thisResourcePath.equals(URI) && !thisResourcePath.equals(URI + "/"))
 				return false;
 			if(tokenizer.countTokens()>0) {
 				StringTokenizer paramStrTokenizer = new StringTokenizer(tokenizer.nextToken(), "&");
 				if(paramStrTokenizer !=null && paramStrTokenizer.hasMoreTokens()){
 					while (paramStrTokenizer.hasMoreTokens()){
 						StringTokenizer paramPairsTokenizer = new StringTokenizer(paramStrTokenizer.nextToken(),"=");
 						if(paramPairsTokenizer != null && paramPairsTokenizer.hasMoreTokens()){
 							String attr = paramPairsTokenizer.nextToken();
 							String val = paramPairsTokenizer.nextToken();
 							exchange.setAttribute(attr, val);
 							exchangeJSON.put(attr, val);
 							logger.info("Added (" + attr + ", " + val + ") pair to exchange session");
 						}
 					}
 				}
 			} else{
 				logger.fine("Not enough tokens");
 			}
 		}
 		logger.finer("ExchangeJSON:  \n\t" + exchangeJSON.toString());
 		return true;
 	}
 
 	protected String getPutPostData(HttpExchange exchange){
 		try {
 			InputStream ist= exchange.getRequestBody();
 			InputStreamReader ir = new InputStreamReader(ist);
 			BufferedReader is = new BufferedReader(ir);
 			String line="";
 			StringBuffer bodyBuf = new StringBuffer();
 			while((line=is.readLine())!=null){
 				line = line.trim();
 				bodyBuf.append(line).append(" ");
 			}
 			//is.close();
 			return bodyBuf.toString();
 		} catch (Exception e) {
 			logger.log(Level.SEVERE, "Exception thrown while processing put/post data", e);
 		}
 		exchange= null;
 		return null;
 	}
 
 	public void sendResponse(HttpExchange exchange, int errorCode, String response, boolean internalCall, JSONObject internalResp){
 		OutputStream responseBody=null;
 		GZIPOutputStream gzipos = null; 
 		try{
 			if(internalCall){
 				copyResponse(response, internalResp);
 				return;
 			}
 
 			logger.info("Sending Response: " + response);
 			JSONObject header = exchangeJSON.getJSONObject("header");
 			boolean gzipResp = header.containsKey("Accept-encoding") && 
 						header.getJSONArray("Accept-encoding").getString(0).contains("gzip");
 			Headers responseHeaders = exchange.getResponseHeaders();
 			responseHeaders.set("Connection", "close");
 			responseHeaders.set("Content-Type", "application/json");
 			if(gzipResp)
 				responseHeaders.set("Content-Encoding", "gzip");
 			
 			exchange.sendResponseHeaders(errorCode, 0);
 			responseBody = exchange.getResponseBody();
 			if(response!=null){
 				if(gzipResp){
 					gzipos = new GZIPOutputStream(responseBody);
 					gzipos.write(response.getBytes());
 					gzipos.close();
 				} else {
 					responseBody.write(response.getBytes());
 					responseBody.close();
 				}
 			}
 
 			sfsStats.docSent(response);
 		}catch(Exception e){
 			logger.log(Level.WARNING, "Exception thrown while sending response, closing exchange object",e);
 		} finally {
 			if(!internalCall){
 				try {
 					if(responseBody !=null){
 						responseBody.close();
 						logger.info("closing responseBody");
 					}
 					if(exchange !=null) {
 						exchange.getResponseBody().close();
 						logger.info("closing exchange: " + exchange.getLocalAddress().getHostName() + ":" + exchange.getLocalAddress().getPort() + "->" + 
 								exchange.getRemoteAddress());
 						exchange.close();
 					}
 					exchange = null;
 				} catch(Exception e){
 					logger.log(Level.WARNING, "", e);
 				}
 			}
 		}
 
 	}
 
 	private void copyResponse(String response, JSONObject internalResp){
 		try{
 			if(internalResp != null){
 				if(response != null){
 					logger.fine("Copying response to internal buffer");
 					JSONObject respObj = (JSONObject) JSONSerializer.toJSON(response);
 					Iterator keys = respObj.keys();
 					while(keys.hasNext()){
 						String thisKey = (String) keys.next();
 						internalResp.put(thisKey, respObj.get(thisKey));
 					}
 				} else {
 					logger.fine("Response was null");
 				}
 			} else {
 				logger.fine("Internal buffer is null");
 			}
 
 		} catch(Exception e){
 			logger.log(Level.WARNING, "",e);
 		}
 	}
 
 	protected boolean isActiveResource(String uri){
 		int id = database.getRRTableId(uri);
 		if(id>=0)
 			return true;
 		return false;
 	}
 
 	public static void main(String[] args){
 		String clause1 = "nin:1,2,3";
 		String clause2 = "or:[smap:true|timestamp:gt:12]";
 		String clause3 = "gt:now,lt:now+2,gte:now-10";
 		String clause4 = "and:[smap:true|timestamp:12345]";
 		String clause5 = "like:jo_ge,J__ge";
 
 		JSONObject gen1 = genJSONClause(clause1);
 		JSONObject gen2 = genJSONClause(clause2);
 		JSONObject gen3 = genJSONClause(clause3);
 		JSONObject gen4 = genJSONClause(clause4);
 		//JSONObject gen5 = genJSONClause(clause5);
 		
 		System.out.println("clause1: " + clause1 + "\tclause1JSON: " + gen1.toString());
 		System.out.println("clause2: " + clause2 + "\tclause2JSON: " + gen2.toString());
 		System.out.println("clause3: " + clause3 + "\tclause3JSON: " + gen3.toString());
 		System.out.println("clause4: " + clause4 + "\tclause4JSON: " + gen4.toString());
 		//System.out.println("clause5: " + clause5 + "\tclause5JSON: " + gen5.toString());
 	}
 
 	public static boolean isNumber(String val){
 		try {
 			Long.parseLong(val);
 			return true;
 		} catch(Exception e){
 			return false;
 		}
 	}
 
 	/**
 	 * This function evaluates a clause string and returns a JSONObject that expresses
 	 * the clause as a valid MongoDB query.  The url query interface is meant to be a simple
 	 * query interface for quickly obtaining values.  If anything more sophististicated
 	 * needs to be done, you may POST a query object following the mongodb query interface.
 	 * 
 	 * ../query=true&props_val=<clause>&props=<clause>
 	 * 
 	 * example queries:
 	 * 		..props_label=in:val1,val2,val3&props_timestamp=gt:12345,lt:23456
 	 *		..props=or:[label:one|title:two]
 	 *
 	 * 	, is used to separate values in an $or, $and array condition or to separate
 	 *		conditions on a value.
 	 * 	| is used between [] to separate conditional JSON objects
 	 *
 	 */
 	public static JSONObject genJSONClause(String clause){
 		JSONObject clauseJSON = new JSONObject();
 		if(clause != null){
 
 			//case: ..&props=or:[label:one|title:two]
 			if(clause.startsWith("and:[") || clause.startsWith("or:[") && 
 					clause.endsWith("]")){
 				JSONArray vals = new JSONArray();
 				String valsStr = clause.substring(clause.indexOf("[")+1, clause.length()-1);
 				StringTokenizer valsToks = new StringTokenizer(valsStr, "|");
 				while(valsToks.hasMoreTokens()){
 					String thisToken = valsToks.nextToken();
 					StringTokenizer innerToks = new StringTokenizer(thisToken, ":");
 					if(innerToks.countTokens()==2){
 						String attr = innerToks.nextToken();
 						String valStr = innerToks.nextToken();
 						JSONObject cond = new JSONObject();
 
 						//convert the value to long if necessary
 						if(isNumber(valStr))
 							cond.put(attr, Long.parseLong(valStr));
 						else
 							cond.put(attr, valStr);
 
 						vals.add(cond);
 					} else{
 						//process inner clauses
 						JSONObject cond = new JSONObject();
 						String newToken = thisToken.substring(thisToken.indexOf(":")+1, thisToken.length());
 
 						JSONObject innerCond = genJSONClause(newToken);
 						if(innerCond != null){
 							cond.put(innerToks.nextToken(), innerCond);
 							vals.add(cond);
 						}
 					}
 				}
 				String op = "$" + clause.substring(0, clause.indexOf(":"));
 				clauseJSON.put(op, vals);
 				
 				return clauseJSON;
 			}
 
 			//case: ..&props_label=in:val1,val2,val3
 			else if(clause.startsWith("in:") || clause.startsWith("nin:") || 
 					clause.startsWith("and:") || clause.startsWith("or:")){
 				JSONArray vals = new JSONArray();
 				String valsStr = clause.substring(clause.indexOf(":")+1, clause.length());
 				StringTokenizer valsToks = new StringTokenizer(valsStr, ",");
 				while(valsToks.hasMoreTokens()){
 					String valStr = valsToks.nextToken();
 					if(isNumber(valStr))
 						vals.add(Long.parseLong(valStr));
 					else
 						vals.add(valStr);
 				}
 				String op = "$" + clause.substring(0, clause.indexOf(":"));
 				clauseJSON.put(op, vals);
 
 				return clauseJSON;
 			}
 
 			//case: ..&props__keywords=like:jo_ge,J__ge
 			/*else if(clause.startsWith("like:") {
 				StringTokenizer tokenizer = new StringTokenizer(clause,",");
 				Vector<String> tokens = new Vector<String>();
 				while(tokenizer.hasMoreTokens())
 					tokens.addElement(tokenizer.nextToken());
 				for(int i=0; i<tokens.size(); ++i){
 					
 				}
 			}*/
 
 			//case: ..&props_timestamp=gt:12345,lt:23456
 			else if(clause.startsWith("gt:") || clause.startsWith("lt:") ||
 					clause.startsWith("gte:") || clause.startsWith("lte:") ||
 					clause.startsWith("ne:") ){
 				StringTokenizer valToks = new StringTokenizer(clause, ",");
 				while(valToks.hasMoreTokens()){
 					String thisToken = valToks.nextToken();
 					StringTokenizer innerToks = new StringTokenizer(thisToken, ":");
 					if(innerToks.countTokens()==2){
 						String op = innerToks.nextToken();
 						String valStr = innerToks.nextToken();
 						long valLong = -1;
 						if(valStr.contains("now")){
 							Date date = new Date();
 							long timestamp = date.getTime()/1000;
 							if(valStr.equalsIgnoreCase("now")){
 								valLong = timestamp;
 							} else if(valStr.startsWith("now-")){
 								String numStr = valStr.substring(valStr.indexOf("-")+1, valStr.length());
 								if(isNumber(numStr)){
 									long sub = Long.parseLong(numStr);
 									valLong = timestamp - sub;
 									//System.out.println("parsedNum: " + num + "; " + val + "=" + val);
 								}
 							} else if(valStr.startsWith("now+")){
 								String numStr = valStr.substring(valStr.indexOf("+")+1, valStr.length());
 								if(isNumber(numStr)){
 									long add = Long.parseLong(numStr);
 									valLong = timestamp + add;
 									//System.out.println("parsedNum: " + num + "; " + val + "=" + val);
 								}
 							}
 						} else if(isNumber(valStr)){
 							valLong = Long.parseLong(valStr);
 						}
 
 						if(valLong!=-1)
 							clauseJSON.put("$" + op, valLong);
 						else
 							clauseJSON.put("$" + op, valStr);
 
 					}
 				}
 				return clauseJSON;
 			}
 
 		}
 
 		return null;
 	}
 
 	//evaluate query
 	public void query(HttpExchange exchange, String data, boolean internalCall, JSONObject internalResp){
 		JSONObject resp = new JSONObject();
 		JSONArray errors = new JSONArray();
 		try{
 			JSONObject propsQueryObj = new JSONObject();
 
 			//get query object from input data
 			if(data != null && !data.equals("")){
 				JSONObject dataJsonObj = (JSONObject) JSONSerializer.toJSON(data);
 				if(TYPE == ResourceUtils.PUBLISHER_RSRC || TYPE == ResourceUtils.GENERIC_PUBLISHER_RSRC){
 					JSONObject dataPropsQuery = dataJsonObj.optJSONObject("props_query");
 					propsQueryObj.putAll(dataPropsQuery);
 				} else {
 					propsQueryObj.putAll(dataJsonObj);
 				}
 			}
 
 			Iterator keys = exchangeJSON.keys();
 			logger.fine("REQUEST_KEYS::" + keys.toString());
 			Vector<String> attributes = new Vector<String>();
 			Vector<String> values = new Vector<String>();
 			while(keys.hasNext()){
 				String thisKey = (String) keys.next();
 				logger.fine("Keys found!; thisKey=" + thisKey);
 				exchange.setAttribute(thisKey, "");
 				/*if(thisKey.startsWith("props_like_")){
 					String str = "props_like_";
 					String queryKey = thisKey.substring(thisKey.indexOf(str)+str.length(), thisKey.length());
 					String queryValue = exchangeJSON.optString(thisKey);
 
 					logger.info("Query Value: " + queryValue);
 
 					JSONObject conditions = Resource.genJSONClause(queryValue);
 
 					logger.info("Conditions: " + conditions);
 					if(conditions!=null)
 						propsQueryObj.put(queryKey, conditions);
 					else
 						propsQueryObj.put(queryKey, queryValue);
 				} else*/ if(thisKey.startsWith("props_")){
 					String str = "props_";
 					String queryKey = thisKey.substring(thisKey.indexOf(str)+str.length(), thisKey.length());
 					String queryValue = exchangeJSON.optString(thisKey);
 
 					logger.info("Query Value: " + queryValue);
 
 					JSONObject conditions = Resource.genJSONClause(queryValue);
 					logger.info("Conditions: " + conditions);
 					if(conditions!=null)
 						propsQueryObj.put(queryKey, conditions);
 					else
 						propsQueryObj.put(queryKey, queryValue);
 
 				} else if(thisKey.startsWith("props")){
 					String queryValue = exchangeJSON.optString(thisKey);
 
 					JSONObject conditions = Resource.genJSONClause(queryValue);
 					if(conditions!=null)
 						propsQueryObj.putAll(conditions);
 					else
 						logger.warning("Invalid conditions set for generic props query");
 				}
 			}
 
 			logger.fine("Props Query: " + propsQueryObj.toString());
 
 			if(!propsQueryObj.toString().equals("{}")){
 				propsQueryObj.put("is4_uri", URI);
 				if(last_props_ts>0)
 					propsQueryObj.put("timestamp", last_props_ts);
 				
 				//check for any regex expressions
 				/*Iterator keys = propsQueryObj.keys();
 				while(keys.hasNext()){
 					String thisKey =keys.next();
 				}*/
 
 				logger.info("Props Query: " + propsQueryObj.toString());
 				JSONObject mqResp = mongoDriver.queryProps(propsQueryObj.toString());
 				logger.fine("mqResp: " + mqResp.toString());
 				JSONArray propsRespObjArray = mqResp.getJSONArray("results");
 				if(propsRespObjArray.size()>0){
 					JSONObject propsRespObj = (JSONObject) propsRespObjArray.get(0);
 					propsRespObj.remove("is4_uri");
 					propsRespObj.remove("timestamp");
 					/*mqResp.put("results", propsRespObj);
 					resp.putAll(mqResp);*/
 					resp.putAll(propsRespObj);
 				}
 			} else {
 				errors.add("Empty or invalid query");
 				
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
 		sendResponse(exchange, 200, resp.toString(), internalCall, internalResp);
 	}
 
 	public UUID createPublisher(String pubName){
 		JSONArray children = database.rrGetChildren(this.URI);
 		try {
 			UUID thisPubIdUUID = null;
 			String pubUri = this.URI + pubName;
 			if(!children.contains(pubName) && 
 				(thisPubIdUUID=database.isPublisher(pubUri, false)) == null){
 
 				//register the publisher
 				Registrar registrar = Registrar.registrarInstance();
 				String newPubId = registrar.registerDevice(pubUri);
 				try{ 
 					thisPubIdUUID = UUID.fromString(newPubId);
 				} catch(Exception e){
 					logger.log(Level.WARNING, "", e);
 				}
 
 				//add to publishers table
 				database.addPublisher(thisPubIdUUID, null, null, pubUri, null);
 				GenericPublisherResource p = new GenericPublisherResource(pubUri, thisPubIdUUID);
 				RESTServer.addResource(p);
 				return thisPubIdUUID;
 			} else {
 				logger.warning("Publisher already registered or publisher name already "+
 						"child of " + this.URI + "\nPubName = " + pubName +
 						"\n\tChildren: " + children.toString() );
 			}
 
 			//bind the device to this context
 			//database.addDeviceEntry(deviceName, this.URI, thisPubIdUUID);
 		} catch (Exception e){
 			logger.log(Level.WARNING, "",e);
 		}
 		return null;
 	}
 
 	protected void setExchangeJSON(JSONObject params){
 		exchangeJSON.putAll(params);
 	}
 
 	/**
 	 * Resolves the uri.  If query has been posted, it is applied to each resource that the uri
 	 * solves to.  The results are returns in the following format.
 	 *
 	 * {
 	 *	"/is4/...":{..}
 	 * }
 	 *
 	 * The attribute is the uri, the value is the results of applying the query to that uri.
 	 */
 	protected void handleRecursiveFSQuery(HttpExchange exchange, boolean internalCall, JSONObject internalResp){//, String uri){
 		String requestUri= null;
 		if(internalCall && exchange.getAttribute("request_uri") != null && 
 			!((String) exchange.getAttribute("request_uri")).equals(""))
 		{
 			requestUri = (String)exchange.getAttribute("request_uri");
 			exchange.setAttribute("request_uri", "");
 		}
 		else{
 			requestUri =  exchange.getRequestURI().toString();
 		}
 		exchange.setAttribute("query","");
 
 		logger.info("FSQuery:: requestUri=" + requestUri + " internalCall: " + internalCall);
 
 		//logger.info("FSQuery Uri: " + uri + " Request URI: " + requestUri);
 		JSONArray resolvedUris = new JSONArray();
 
 		String uriOnly = requestUri;	
 		if(requestUri.contains("?")){	
 			uriOnly = requestUri.substring(0, uriOnly.indexOf("?"));
 			logger.info("Request URI, no params: " + uriOnly + " \ttype=" + (String) exchange.getAttribute("type"));
 			resolvedUris.addAll( database.resolveStarredUri(uriOnly, (String) exchange.getAttribute("type")) );
 		} else{
 			resolvedUris.addAll( database.resolveStarredUri(uriOnly, null) );
 		}
 		logger.info("Resolved Uris: " + resolvedUris.toString());
 
 
 		//get the request method
 		String requestMethod =  exchange.getRequestMethod();
 		String putPostData = null;
 		if(requestMethod.equalsIgnoreCase("put") || requestMethod.equalsIgnoreCase("post"))
 			putPostData = getPutPostData(exchange);
 
 		JSONObject responses = new JSONObject();
 		try{
 			if(requestMethod.equalsIgnoreCase("get")){
 				for(int i=0; i<resolvedUris.size(); i++){
 					Resource thisResource = RESTServer.getResource(resolvedUris.getString(i));
 					if(thisResource != null){
 						JSONObject respBuffer = new JSONObject();
 						thisResource.setExchangeJSON(this.exchangeJSON);
 						thisResource.get(exchange, true, respBuffer);
 						if(thisResource.TYPE == ResourceUtils.SYMLINK_RSRC){
 							processResponse(thisResource, respBuffer);
 							responses.putAll(respBuffer);
 						} else {
 							responses.put(thisResource.URI, respBuffer);
 						}
 					}
 				}
 			} else if(requestMethod.equalsIgnoreCase("put")){
 				for(int i=0; i<resolvedUris.size(); i++){
 					Resource thisResource = RESTServer.getResource(resolvedUris.getString(i));
 					if(thisResource != null){
 						JSONObject respBuffer = new JSONObject();
 						thisResource.setExchangeJSON(this.exchangeJSON);
 						thisResource.put(exchange, putPostData, true, respBuffer);
 						if(thisResource.TYPE == ResourceUtils.SYMLINK_RSRC){
 							processResponse(thisResource, respBuffer);
 							responses.putAll(respBuffer);
 						} else{
 							responses.put(thisResource.URI, respBuffer);
 						}
 					}
 				}
 			} else if(requestMethod.equalsIgnoreCase("post")){
 				for(int i=0; i<resolvedUris.size(); i++){
 					Resource thisResource = RESTServer.getResource(resolvedUris.getString(i));
 					if(thisResource != null){
 						JSONObject respBuffer = new JSONObject();
 						thisResource.setExchangeJSON(this.exchangeJSON);
 						thisResource.post(exchange, putPostData, true, respBuffer);
 						if(thisResource.TYPE == ResourceUtils.SYMLINK_RSRC){
 							processResponse(thisResource, respBuffer);
 							responses.putAll(respBuffer);
 						} else {
 							responses.put(thisResource.URI, respBuffer);
 						}
 					}
 				}
 			} else if(requestMethod.equalsIgnoreCase("delete")){
 				if(resolvedUris.size()>0)
 					depthFirstSort(resolvedUris);
 				for(int i=0; i<resolvedUris.size(); i++){
 					Resource thisResource = RESTServer.getResource(resolvedUris.getString(i));
 					if(thisResource != null){
 						JSONObject respBuffer = new JSONObject();
 						thisResource.setExchangeJSON(this.exchangeJSON);
 						thisResource.delete(exchange, true, respBuffer);
 						if(thisResource.TYPE == ResourceUtils.SYMLINK_RSRC){
 							processResponse(thisResource, respBuffer);
 							responses.putAll(respBuffer);
 						} else {
 							responses.put(thisResource.URI, respBuffer);
 						}
 					}
 				}
 			}
 		} catch (Exception e){
 			logger.log(Level.WARNING, "", e);
 		}
 
 		sendResponse(exchange, 200, responses.toString(), internalCall, internalResp);
 	}
 
 	private void processResponse(Resource resource, JSONObject buffer){
 		if(resource != null && buffer != null && resource.TYPE == ResourceUtils.SYMLINK_RSRC){
 			Iterator keys = buffer.keys();
 			Vector<String> oriKeys = new Vector<String>();
 			StringBuffer allKeysStrBuf = new StringBuffer();
 			while(keys.hasNext()){
 				String thisKey = (String) keys.next();
 				oriKeys.add(thisKey);
 				allKeysStrBuf.append(thisKey + "\n");
 			}
 			logger.info("\n" + allKeysStrBuf.toString());
 			String slinkUri = resource.getURI();
 			String linksToUril = ((SymlinkResource)resource).getLinkString();
 			logger.info("slink uri: " + slinkUri + " links to: " + linksToUril);
 			
 			if(linksToUril.startsWith("http")){
 				try{
 					URL url = new URL(linksToUril);
 					linksToUril = url.getPath();
 					logger.info("Now links to: " + linksToUril);
 				} catch(Exception e){}
 			}
 
 			for(int i=0; i<oriKeys.size(); i++){
 				String thisKey = oriKeys.elementAt(i);
 				logger.info("Old key: " + thisKey);
 				JSONObject thisResp = buffer.getJSONObject(thisKey);
 				buffer.remove(thisKey);
 				thisKey = thisKey.replace(linksToUril, slinkUri);
 				logger.info("New key: " + thisKey);
 				thisKey = thisKey.replaceAll("\\/+", "/");
 				buffer.put(thisKey, thisResp);
 			}
 		}
 	}
 
 	private void depthFirstSort(JSONArray uris){
 		JSONObject buckets = new JSONObject();
 		Vector<Integer> keysVec = new Vector<Integer>();
 		for(int i=0; i< uris.size(); i++){
 			String thisUri = uris.getString(i);
 			logger.info("thisUri: " + thisUri);
 			StringTokenizer tokenizer = new StringTokenizer(thisUri, "/");
 			Integer tokenCount = new Integer(tokenizer.countTokens());
 			JSONArray thisBucket = buckets.optJSONArray(tokenCount.toString());
 			if(thisBucket == null){
 				thisBucket = new JSONArray();
 				keysVec.add(tokenCount);
 			}
 			thisBucket.add(thisUri);
 			buckets.put(tokenCount.toString(), thisBucket);
 		}
 
 		Integer[] bucketSizes = new Integer[keysVec.size()];
 		keysVec.toArray(bucketSizes);
 		Arrays.sort(bucketSizes);
 
 		uris.clear();
 		for(int i=keysVec.size()-1; i>=0; i--){
 			logger.info("Sorted list: " + bucketSizes[i] + "\nKey Vec: " + keysVec.get(i));
 			JSONArray bucket = buckets.getJSONArray(bucketSizes[i].toString());
 			for(int j=0; j<bucket.size(); j++)
 				uris.addAll(bucket);
 		}
 
 	}
 
 	public void updateProperties(JSONObject propsObj){
 		
 		MongoDBDriver mongoDriver = new MongoDBDriver();
 
 		//add an array to support fulltxt search
 		HashMap<String, String> uniqueKeys = new HashMap<String, String>();
 		JSONArray keywords = new JSONArray();
 		Iterator pKeys = propsObj.keys();
 		while(pKeys.hasNext()){
 			String thisKey = (String)pKeys.next();
 			if(!uniqueKeys.containsKey(thisKey.trim())){
 				keywords.add(thisKey.trim());
 				uniqueKeys.put(thisKey.trim(),"y");
 			}
 			String val = propsObj.get(thisKey).toString();
 			val.replace("[","");val.replace("]","");val.replace("{","");val.replace("}","");
 			val.replace(",","");val.replace(";","");
 			String delims = "[ ]+";
 			String[] tokens = val.split(delims);
 			for(int i=0;i<tokens.length; ++i){
 				if(!uniqueKeys.containsKey(tokens[i].trim())){
 					keywords.add(tokens[i].trim());
 					uniqueKeys.put(tokens[i].trim(),"y");
 				}
 			}
 		}
 		logger.fine(uniqueKeys.toString());
 		logger.fine("_keywords:" + keywords.toString());
 		propsObj.put("_keywords", keywords);
 
 		//add is4_uri
 		propsObj.put("is4_uri", URI.toString());
 
 		//add timestamp
 		Date date = new Date();
 		long timestamp = date.getTime()/1000;
 		propsObj.put("timestamp", timestamp);
 
 		//store in mongodb repos
 		mongoDriver.putPropsEntry(propsObj);
 
 
 		//save the last updated timestamp in the database
 		database.updateLastPropsTs(URI, timestamp);
 
 		last_props_ts = timestamp;
 
 		//place it in buffer
 		/*if(database.hasPropertiesBuffered(URI))
 			database.insertPropertiesIntoBuffer(URI, propsObj);
 		else
 			database.updatePropertiesInBuffer(URI, propsObj);*/
 	}
 
 	public void handlePropsReq(HttpExchange exchange, String data, boolean internalCall, JSONObject internalResp){
 		try{
 			JSONObject dataObj = (JSONObject) JSONSerializer.toJSON(data);
 			String op = dataObj.getString("operation");
 			if (op.equalsIgnoreCase("update_properties")){
 				logger.info("processing update_properties");
 				JSONObject response = new JSONObject();
 				JSONObject currentProps = database.rrGetProperties(URI);
 				JSONObject properties = null;
 				try{
 					properties = dataObj.getJSONObject("properties");
 				} catch(Exception e){
 					logger.log(Level.WARNING, "", e);
 					response.put("status", "fail");
 					JSONArray errors = new JSONArray();
 					errors.add("Missing properties object");
 					response.put("errors", errors);
 					sendResponse(exchange, 200, response.toString(), internalCall, internalResp);
 					return;
 				}
 
 				if(currentProps != null){
 					Iterator keys = properties.keys();
 					while(keys.hasNext()){
 						String thisKey = (String) keys.next();
 						currentProps.put(thisKey, properties.get(thisKey));
 					}
 					database.rrPutProperties(URI, currentProps);
 					updateProperties(currentProps);
 				} else {
 					database.rrPutProperties(URI, properties);
 					updateProperties(properties);
 				}
 				response.put("status", "success");
 				sendResponse(exchange, 200, response.toString(), internalCall, internalResp);
 			}
 
 			else if (op.equalsIgnoreCase("overwrite_properties")){
 				logger.info("processing overwrite_properties");
 				JSONObject response = new JSONObject();
 				JSONObject properties = null;
 				try{
 					properties = dataObj.getJSONObject("properties");
 				} catch(Exception e){
 					logger.log(Level.WARNING, "", e);
 					response.put("status", "fail");
 					JSONArray errors = new JSONArray();
 					errors.add("Missing properties object");
 					response.put("errors", errors);
 					sendResponse(exchange, 200, response.toString(), internalCall, internalResp);
 				}
 
 				database.rrPutProperties(URI, properties);
 				updateProperties(properties);
 				response.put("status", "success");
 				sendResponse(exchange, 200, response.toString(), internalCall, internalResp);
 			}
 
 			else{
 				sendResponse(exchange, 200, null, internalCall, internalResp);
 			}
 		} catch (Exception e){
 			//silent fail
 			sendResponse(exchange, 200, null, internalCall, internalResp);
 		}
 	}
 
 	private void findSymlinks(JSONArray children){
 		JSONArray newChildren = new JSONArray();
 		for(int i=0; i<children.size(); i++){
 			String childName = (String) children.get(i);
 			String resourcePath = URI + childName;
 			boolean isSymlink = database.isSymlink(resourcePath);
 			if(isSymlink){
 				logger.info(resourcePath + " is a symlink");
 				SymlinkResource slr = (SymlinkResource) RESTServer.getResource(resourcePath);
 				if(slr != null){
 					String symlinkStr = childName  + " -> " + slr.getLinkString();
 					newChildren.add(symlinkStr);
 				}
 			} else {
 				newChildren.add(childName);
 			}
 		}
 		children.clear();
 		children.addAll(newChildren);
 	}
 
 	private Vector<String> createTokenVector(String target, String delim){
 		StringTokenizer tokens = new StringTokenizer(target, delim);
 		Vector<String> allToks  = new Vector<String>();
 		while(tokens.hasMoreTokens())
 			allToks.add((String)tokens.nextToken());
 		return allToks;
 	}
 
     public JSONArray queryAggTimeseries(String aggStr, String units, JSONObject queryJson){
 		JSONArray queryResults = new JSONArray();
 		try{

			//only run the query for this publisher
			/*queryJson.put("pubid", publisherId.toString());

			//remove the PubId key from the results
			JSONObject keys = new JSONObject();
			keys.put("pubid",0);

			logger.info("QUERY: " + queryJson.toString() + "\nKEYS:  " + keys.toString());

			return mongoDriver.queryTsColl(queryJson.toString(), keys.toString());*/

             String qres = metadataGraph.queryAgg(URI, aggStr, units, queryJson);
             logger.fine("qres=" + qres);
             if(qres !=null){
                 JSONSerializer serializer = new JSONSerializer();
                 JSONObject qresObj = (JSONObject)serializer.toJSON(qres);
                 if(qresObj.containsKey("results"))
                     queryResults = qresObj.getJSONArray("results");
             }
 		} catch(Exception e){
 			logger.log(Level.WARNING, "", e);
 		}
 		return queryResults;
 	}
 
     private JSONObject genTSQueryObject(HttpExchange exchange, String data){
 		JSONObject tsQueryObj2 = new JSONObject();
 		try{
 		
 			//get query object from input data
 			if(data != null && !data.equals("")){	
 				JSONObject dataJsonObj = (JSONObject) JSONSerializer.toJSON(data);
 				JSONObject dataTsQuery = dataJsonObj.optJSONObject("ts_query");
 				tsQueryObj2.putAll(dataTsQuery);
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
 						if(queryKey.equalsIgnoreCase("timestamp"))
 							tsQueryObj2.put("ts", conditions);
 					} else{
 						if(isNumber(queryValue)){
 							long val = Long.parseLong(queryValue);
 							if(queryKey.equalsIgnoreCase("timestamp"))
 								tsQueryObj2.put("ts", val);
 						} else {
 							if(queryKey.equalsIgnoreCase("timestamp"))
 								tsQueryObj2.put("ts", queryValue);
 						}
 					}
 
 				} else if(thisKey.startsWith("ts")){
 					String queryValue = exchangeJSON.optString(thisKey);
 
 					JSONObject conditions = Resource.genJSONClause(queryValue);
 					if(conditions!=null){
 						tsQueryObj2.putAll(conditions);
 					} else{
 						if(isNumber(queryValue)){
 							long val = Long.parseLong(queryValue);
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
         } catch (Exception e){
             logger.log(Level.WARNING, "", e);
         }
 
         logger.fine("Timeseries Query2: " + tsQueryObj2.toString());
         return tsQueryObj2;
     }
 
     private void handleTSAggQuery(HttpExchange exchange, String data, boolean internalCall, JSONObject internalResp){
 		JSONObject resp = new JSONObject();
 		JSONArray errors = new JSONArray();
 		resp.put("path", URI);
 		try{
             JSONObject tsQueryObj2 = genTSQueryObject(exchange, data);
 			if(!tsQueryObj2.toString().equals("{}")){
                 String aggStr = exchangeJSON.getString("agg");
                 String units = exchangeJSON.getString("units");
 				JSONArray mqResp2 = queryAggTimeseries(aggStr, units, tsQueryObj2);
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
 		sendResponse(exchange, 200, resp.toString(), internalCall, internalResp);
 		exchangeJSON.clear(); 
     }
 }
