 package is4;
 
 import java.util.logging.Logger;
 import java.util.logging.Level;
 import java.util.*;
 import java.io.*;
 import java.net.*;
 import java.util.concurrent.Semaphore;
 
 import com.sun.net.httpserver.*;
 import local.db.*;
 import local.rest.*;
 import local.rest.resources.*;
 import local.rest.resources.proc.*;
 import local.rest.resources.util.*;
 import java.util.concurrent.ConcurrentHashMap;
 import java.nio.ByteBuffer;
 import java.nio.channels.Pipe;
 import java.nio.channels.Pipe.SinkChannel;
 import java.nio.channels.Pipe.SourceChannel;
 
 import net.sf.json.*;
 import is4.exceptions.*;
 
 /**
  * Subscription manager.  Maintains subscriber and flow information.
  */
 
 public class SubMngr {
 
 	private static SubMngr subManager = null;
 
 	protected static transient final Logger logger = Logger.getLogger(SubMngr.class.getPackage().getName());
 	
 	protected static MySqlDriver database = (MySqlDriver) DBAbstractionLayer.database;
 
 	//subscription identifer associated with a pipe that writes to the model thread 
 	//private static ConcurrentHashMap<UUID, Pipe> activePipes = new ConcurrentHashMap<UUID, Pipe>();
 
 	//subscription identifier associated with a semaphore that used by the thread that this subscription writes to
 	//private static ConcurrentHashMap<UUID, Semaphore> readSemaphores = new ConcurrentHashMap<UUID, Semaphore>();
 
 	//same as above, but references by publisher identifer --this is the right way to do it since the 
 	//subscription source may be a wildcard
 	private static ConcurrentHashMap<UUID, Pipe> activePipesByPubId = new ConcurrentHashMap<UUID, Pipe>();
 	private static ConcurrentHashMap<UUID, Semaphore> readSemaphoresByPubId = new ConcurrentHashMap<UUID, Semaphore>();
 	
 	private SubMngr(){
 	}
 
 	/**
 	 *  Constructor.
 	 */
 	public static SubMngr getSubMngrInstance(){
 		if(subManager==null)
 			subManager = new SubMngr();
 		return subManager;
 	}
 
 	/**
 	 *  Return the list of subscription IDs.
 	 */
 	public List<String> getSubIds(){
 		return (List<String>) database.getAllSubIds();
 	}
 
 	/**
 	 *  Return the streams subscribed to by the process with the given subscription ID.
 	 */
 	public List<String> getStreamIds(String subId){
 		try {
 			UUID sid = UUID.fromString(subId);
 			return (List<String>) database.getAssocStreamUris(sid);
 		} catch (Exception e){
 			return (List<String>)new JSONArray();
 		}
 	}
 
 	/**
 	 * The associated subscriber url for this subscriber, null if it doesn't have one or the subcriber does not exist.
 	 */
 	public String getSubUrl(String subId){
 		try {
 			UUID sid = UUID.fromString(subId);
 			return database.getSubDestUrlStr(sid);
 		} catch(Exception e){
 			return null;
 		}
 	}
 
 	/**
 	 * Initializes a new subscription using the specified wild-card path to the target (path or url).
 	 * Any publisher that matches the wildcard expression will push data to the specified target. The wildcard
 	 * must match at least one publisher in order to be installed.  Wildcard subscription MUST be explicitly
 	 * deleted, unlike single-publisher subscriptions.
 	 * 
 	 * @param wildcardPath a path with the * wildcard character in it. <br>
 	 * 	example:<br>
 	 * 		/buildings/SodaHall/electrical/*<br>
 	 *		All publishers witht eh /buildings/SodaHall/eletrical prefix in their path will
 	 * 		forward their data to the specified target.<br><br>
 	 * @param target either a path to a model or processing resource within StreamFS, or a URL.  The
 	 * 		URL must begin with "http://".
 	 * @return subscription creation message or a message with errors
 	 */
 	public synchronized JSONObject initSubscription(String wildcardPath, String target){
 		JSONObject response = new JSONObject();
 		JSONArray errors = new JSONArray();
 		try {
 			String newSubId = null;
 			logger.info("Trying to create subscription from " + wildcardPath + " to " + target);
 			String usid = database.getSubscriptionId(null, wildcardPath, target);
 			boolean pubMatch = database.wildcardPubMatch(wildcardPath);
 
 			if( pubMatch && usid==null) {
 				newSubId  = generateNewId();
 				response.put("subid", newSubId);
 				
 				if(target.startsWith("http://")){
 					//create initial entry
 					database.insertNewSubEntry(UUID.fromString(newSubId), null, null, target, null, null, wildcardPath);
 					return response;
 				}
 			} else {
 				String e=null;
 				if(!pubMatch){
 					e = wildcardPath + " does not match any valid publisher";
 					logger.warning(e);
 					errors.add(e);
 				}
 
 				if(usid !=null){
 					e = "The subscriber with (wildcard, target):(" + wildcardPath + ", " + target 
 										+ ") is already a subscriber; Could not add as new subscriber";
 					logger.warning(e);
 					errors.add(e);
 				}
 				response.put("errors", errors);
 				return response;
 			}
 			
 			if(target.startsWith("/")){
 				//the target uri/path; try to create a model thread and pipe
 				Resource r = RESTServer.getResource(target);
 				if(r != null && (r.TYPE==ResourceUtils.MODEL_RSRC || r.TYPE==ResourceUtils.MODEL_GENERIC_PUBLISHER_RSRC ||
                                 r.TYPE==ResourceUtils.PROCESS_RSRC || r.TYPE==ResourceUtils.PROCESS_PUBLISHER_RSRC)){
 
 					if(r.TYPE==ResourceUtils.MODEL_RSRC){
 						Pipe newPipe = Pipe.open();
 						Semaphore s = new Semaphore(1, true);
 						ModelResource modelres = (ModelResource)r;
 
 						Thread modelresThreadInstance = ModelResource.startNewModelThread(s, newPipe.source(),modelres);
 						/*UUID unewid = UUID.fromString(newSubId);
 						if(activePipes.containsKey(unewid)){
 							activePipes.replace( unewid, newPipe);
 							readSemaphores.put(unewid, s);
 						} else {
 							activePipes.put(unewid, newPipe);
 							readSemaphores.put(unewid, s);
 						}*/
 
 						//populate pipes references by publisher id/thread name
 						UUID modelPubId = UUID.fromString(modelresThreadInstance.getName());
 						if(activePipesByPubId.containsKey(modelPubId)){
 							activePipesByPubId.replace(modelPubId, newPipe);
 						} else {
 							activePipesByPubId.put(modelPubId, newPipe);
 						}
 						if(readSemaphoresByPubId.containsKey(modelPubId)){
 							readSemaphoresByPubId.replace(modelPubId, s);
 						} else {
 							readSemaphoresByPubId.put(modelPubId, s);
 						}
 						String newTargetPubUri = database.getIs4RRPath(UUID.fromString(modelresThreadInstance.getName()));
 						logger.info("Registered new pipe for [" + wildcardPath + ", " + newTargetPubUri + "]=(subid="+ newSubId + ")");
 					
 						//create initial entry
 						database.insertNewSubEntry(UUID.fromString(newSubId), null, null, null, newTargetPubUri, null, wildcardPath);
 					} else if(r.TYPE==ResourceUtils.MODEL_GENERIC_PUBLISHER_RSRC){
 						//this is a subscription where the model_instance is the target
 						//there is a pipe associated with this target, just add a subscription entry
 
 						//create initial entry
 						database.insertNewSubEntry(UUID.fromString(newSubId), null, null, null, r.getURI(), null, wildcardPath);
 					}
 
 
                     else if(r.TYPE == ResourceUtils.PROCESS_RSRC){
                         //create a new publisher, get the path to it and set the target to that path
                         logger.info("Installing subscription and creating publisher for associated process for target (" + target + ")");
                     }
 
                     else if(r.TYPE == ResourceUtils.PROCESS_PUBLISHER_RSRC){
                         //add this publisher to the subscription for this resource
                         logger.info("Adding stream to subscription for a publisher resource (" + target + ")");
                     }
 				} else {
 					errors.add("Target path must be a MODEL or PROCESSING resource");
 					response = response.discard("subid");
 					response.put("errors", errors);
 				}
 			} else {
 				errors.add("Target path must ABSOLUTE (start with \"/\")");
 				response = response.discard("subid");
 				response.put("errors", errors);
 			}
 			return response;
 		} catch (Exception m){
 			logger.log(Level.WARNING, "Exception caught in addSub(String url_, List<String> streams)", m);
 		}
 		response.put("errors",errors);
 		return response;
 	}
 
 
 
 	/**
 	 *  Initialize a new subscription from the publisher (pubid) to the target (path or url).
 	 */
 	public synchronized JSONObject initSubscription(UUID pubid, String target){
 		JSONObject response = new JSONObject();
 		JSONArray errors = new JSONArray();
 		try {
 			String newId = null;
 			logger.info("Trying to create subscription from " + pubid.toString() + " to " + target);
 			String usid = database.getSubscriptionId(pubid, null, target);
 			boolean isValidPub = database.isPublisher(pubid);
 			if( isValidPub && usid==null) {
 				newId  = generateNewId();
 				response.put("subid", newId);
 				
 				if(target.startsWith("http://")){
 					//create initial entry
 					database.insertNewSubEntry(UUID.fromString(newId), null, null, target, null, pubid, null);
 					return response;
 				}
 			} else {
 				String e=null;
 				if(!isValidPub){
 					e = pubid.toString() + " is not a valid publisher";
 					logger.warning(e);
 					errors.add(e);
 				}
 
 				if(usid !=null){
 					String pubPath = database.getIs4RRPath(pubid);
 					e = "The subscriber with (source, target):(" + pubPath + ", " + target 
 										+ ") is already a subscriber; Could not add as new subscriber";
 					logger.warning(e);
 					errors.add(e);
 				}
 				response.put("errors", errors);
 				return response;
 			}
 			
 			if(target.startsWith("/")){
 				//the target uri/path; try to create a model thread and pipe
 				Resource r = RESTServer.getResource(target);
 				if(r != null && (r.TYPE==ResourceUtils.MODEL_RSRC || r.TYPE==ResourceUtils.MODEL_GENERIC_PUBLISHER_RSRC ||
                                 r.TYPE==ResourceUtils.PROCESS_RSRC || r.TYPE==ResourceUtils.PROCESS_PUBLISHER_RSRC)){
 					
 					if(r.TYPE==ResourceUtils.MODEL_RSRC){
 						Pipe newPipe = Pipe.open();
 						Semaphore s = new Semaphore(1, true);
 
 						ModelResource modelres = (ModelResource)r;
 						
 						Thread modelresThreadInstance = ModelResource.startNewModelThread(s, newPipe.source(),modelres);
 						/*UUID unewid = UUID.fromString(newId);
 						if(activePipes.containsKey(unewid)){
 							activePipes.replace( unewid, newPipe);
 							readSemaphores.put(unewid, s);
 						} else {
 							activePipes.put( unewid, newPipe);
 							readSemaphores.put(unewid, s);
 						}*/
 
 						//populate pipes references by publisher id/thread name
 						UUID modelPubId = UUID.fromString(modelresThreadInstance.getName());
 						if(activePipesByPubId.containsKey(modelPubId)){
 							activePipesByPubId.replace(modelPubId, newPipe);
 						} else {
 							activePipesByPubId.put(modelPubId, newPipe);
 						}
 						if(readSemaphoresByPubId.containsKey(modelPubId)){
 							readSemaphoresByPubId.replace(modelPubId, s);
 						} else {
 							readSemaphoresByPubId.put(modelPubId, s);
 						}
 						String newTargetPubUri = database.getIs4RRPath(modelPubId);
 						logger.info("Registered new pipe for [" + pubid.toString()  + ", " + newTargetPubUri + "]=(subid="+ newId + ")");
 					
 						//create initial entry
 						database.insertNewSubEntry(UUID.fromString(newId), null, null, null, newTargetPubUri, pubid, null);
 					} else if(r.TYPE==ResourceUtils.MODEL_GENERIC_PUBLISHER_RSRC){
 						//this is a subscription where the model_instance is the target
 						//there is a pipe associated with this target, just add a subscription entry
 
 						//create initial entry
 						database.insertNewSubEntry(UUID.fromString(newId), null, null, null, r.getURI(), pubid, null);
 					} 
 
                     else if(r.TYPE == ResourceUtils.PROCESS_RSRC){
                         //create a new publisher, get the path to it and set the target to that path
                         logger.info("Installing subscription and creating publisher for associated process for target (" + target + ")");
                         JSONObject pubinfo = ((ProcessResource)r).startNewProcess(newId);
                         if(pubinfo !=null){
                             try {
                                 String path = pubinfo.getString("path");
                                 database.insertNewSubEntry(UUID.fromString(newId), null, null, null, path, pubid, null);
                                 if(!ProcessManagerResource.updateSubEntry(newId)){
                                     Resource resource_ = RESTServer.getResource(path);
                                     resource_.delete(null, true, new JSONObject());
                                     database.removeSubEntry(UUID.fromString(newId));
 
                                     response.clear();
                                     errors.add("Could not start process on process server");
                                     errors.add("Check that process servers are all up");
                                     response.put("errors", errors);
                                 }
                                     
                                 return response;
                             } catch(Exception e){
                                 logger.log(Level.WARNING, "", e);
                                 response.clear();
                                 errors.add("Exception thrown while creating process publisher during subscription installation");
                                 response.put("errors", errors);
                             }
                         }
                     }
 
                     else if(r.TYPE == ResourceUtils.PROCESS_PUBLISHER_RSRC){
                         //add this publisher to the subscription for this resource
                         logger.info("Adding stream to subscription for a publisher resource (" + target + ")");
                     }
 				} else {
 					errors.add("Target path must be a MODEL resource");
 					response = response.discard("subid");
 					response.put("errors", errors);
 				}
 			} else {
 				errors.add("Target path must ABSOLUTE (start with \"/\")");
 				response = response.discard("subid");
 				response.put("errors", errors);
 			}
 			return response;
 		} catch (Exception m){
 			logger.log(Level.WARNING, "Exception caught in addSub(String url_, List<String> streams)", m);
 		}
 		response.put("errors",errors);
 		return response;
 	}
 
 	public boolean restartActiveModels(ModelResource modelres){
 
 		boolean allok = true;
 		if(modelres != null){
 			JSONArray modelPubIdsArray = database.getModelPubliserIds(modelres.getURI());
 
 			for(int i=0; i<modelPubIdsArray.size(); i++){
 				try {
 					Pipe newPipe = Pipe.open();
 					Semaphore s = new Semaphore(1, true);
 					UUID pubid = UUID.fromString((String)modelPubIdsArray.get(i));
 					Thread modelresThreadInstance = ModelResource.restartModelThread(s, newPipe.source(),modelres, pubid);
 
 					if(modelresThreadInstance != null){
 						//populate pipes references by publisher id/thread name
 						if(activePipesByPubId.containsKey(pubid)){
 							activePipesByPubId.replace(pubid, newPipe);
 						} else {
 							activePipesByPubId.put(pubid, newPipe);
 						}
 						if(readSemaphoresByPubId.containsKey(pubid)){
 							readSemaphoresByPubId.replace(pubid, s);
 						} else {
 							readSemaphoresByPubId.put(pubid, s);
 						}
 						logger.info("Thread-" + modelresThreadInstance.getName() + " restarted!");
 					} else {
 						allok=false;
 					}
 				} catch(Exception e){
 					logger.log(Level.WARNING, "", e);
 					allok=false;
 				}
 					
 				//logger.info("Registered new pipe for [" + wildcardPath + ", " + target + "]=(subid="+ newId + ")");
 			
 				//create initial entry
 				//database.insertNewSubEntry(UUID.fromString(newId), null, null, null, target, null, wildcardPath);
 			}
 			return allok;
 		} else {
 			logger.warning("Model resource null");
 			return false;
 		}
 	}
 
 	//check if the newly added publisher should included in any bulk subscription
 	//if so, it add it and returns true, otherwise it does not and returns false
 	public boolean publisherAdded(UUID pubid){ 
 		return true;
 	}
 
 	/**
 	 *  Publisher removed.  Called when a publisher is deleted.  Remove all subscriptions
 	 *  associated with the removed pubid.
 	 */
 	public void pubRemoved(HttpExchange exchange, boolean internalCall, JSONObject internalResp, String pubId){
 		try {
 			UUID pid = UUID.fromString(pubId);
 			
 			//get each path for every sid and delete the associated rest resource
 			JSONArray sids = database.getSubIdsByPubId(pid);
 			for(int i=0; i<sids.size(); i++){
 				UUID tsid = UUID.fromString((String)sids.get(i));
 				String subUri = database.getSubUriBySubId(tsid);
 				Resource r = RESTServer.getResource(subUri);
 				if(r!=null){
                     logger.info("Deleting " + subUri);
					r.delete(exchange, internalCall, internalResp);
                 }
 			}
 			
 			//now remove all of them
 			database.removeSubByPubId(pid);
 		} catch(Exception e){
 			logger.log(Level.WARNING, "", e);
 		}
 	}
 	
 	/**
 	 *  Checks that all the streams in the list of streams are registered publishers.  All unregistered
 	 *  pubids are returned.
 	 */
 	public JSONArray checkPubIds(JSONArray pubids){
 		JSONArray invalidPubIds = new JSONArray();
 		logger.info("Checking if pubids in list are valid");
 		for(int i=0; i<pubids.size(); i++){
 			UUID thisPubid = UUID.fromString((String)pubids.get(i));
 			if(database.isPublisher(thisPubid)==false)
 				invalidPubIds.add(thisPubid);
 		}
 		return invalidPubIds;
 	}
 	
 	public JSONArray checkPubPaths(JSONArray paths){
 		JSONArray invalidPubIds = new JSONArray();
 		logger.info("Checking if pubids in list are valid");
 		for(int i=0; i<paths.size(); i++){
 			String thisPath = (String) paths.get(i);
 			Resource r = RESTServer.getResource(thisPath);
 			if(r ==null || (r.TYPE != ResourceUtils.PUBLISHER_RSRC && r.TYPE != ResourceUtils.MODEL_RSRC))
 				invalidPubIds.add(r);
 		}
 		return invalidPubIds;
 	}
 
 	/**
 	 *  Checks if this URL is already in the system.
 	 */
 	public boolean isSubscriber(String sid) {
 		try{
 			UUID usid = UUID.fromString(sid);
 			return ((database.isSubscription(usid))!=null);
 		}catch(Exception e){
 			return false;
 		}
 	}
 
 	/**
 	 *  Generates a new id.
 	 */
 	private String generateNewId() throws NoMoreRegistrantsException{
 		//generate a new id
 		String id = UUID.randomUUID().toString();
 		return id;
 	}
 	
 	/**
 	 *  Get the publisher id from the data object.
 	 */
 	private UUID getpubid(JSONObject dataObject){
 		try {
 			Iterator keys  = dataObject.keys();
 			String pubidKey = null;
 			while(keys.hasNext()){
 				pubidKey = (String)keys.next();
 				if(pubidKey.equalsIgnoreCase("pubid"))
 					return UUID.fromString(dataObject.getString(pubidKey));
 			}
 		} catch (Exception e){
 			logger.warning("No pubid found in data object");
 		}
 		return null;
 	}
 
 	
 	/**
 	 *  Forward this data object to the proper set of subscribers.
 	 */
 	public synchronized void dataReceived(JSONObject dataObject){
 
 		logger.fine("dataReceived called: " + dataObject.toString());
 		try {
 			URL thisSubUrl = null;
 			if(dataObject != null){
 				//get the pub id from the data object
 				UUID upid = getpubid(dataObject);
 				if(upid == null)
 					return;
 				
 				logger.fine("pid " + upid.toString() + " found!"); 
 		
 				//get the list of subscriptions for this publisher streams
 				JSONArray subids = database.getSubIdsByPubId(upid);
 				
 				//enable the information bus resource
 				JSONArray busSubids = database.getSubIdsByPubId(UUID.fromString("00000000-0000-0000-0000-000000000000"));
 				subids.addAll(busSubids);
 				//////////////////////////////////////
 				
 				logger.info("SubMngr::" + upid.toString() + ":" + subids.toString() + " \tSIZE=" + subids.size());
 			
 				if(subids != null && subids.size()>0){
 
 					//forward this data object to every subscriber
 					for(int i=0; i<subids.size(); ++i){
 						try{
 							UUID usid = UUID.fromString((String)subids.get(i));
 							String thisSubUrlStr = database.getSubDestUrlStr(usid);
 							String thisSubUriStr = database.getSubDestUriStr(usid);
 							
 							logger.fine("SubMngr:: URL=" + thisSubUrlStr + "; URI=" + thisSubUriStr);
 
 							//subscription target is an external url
 							if(thisSubUrlStr != null && thisSubUrlStr.length()>0) {
 								String urlStr = thisSubUrlStr;
 								String urlParamsStr = null;
 								if(thisSubUrlStr.contains("?")){
 									//strip out url params
 									urlStr = thisSubUrlStr.substring(0, thisSubUrlStr.indexOf("?"));
 									urlParamsStr = thisSubUrlStr.substring(thisSubUrlStr.indexOf("?")+1, 
 															thisSubUrlStr.length());
 									StringTokenizer tk = new StringTokenizer(urlParamsStr, "&");
 									logger.fine("URL_only: " + urlStr + "; params: " + urlParamsStr);
 									JSONObject urlParamsObj = new JSONObject();
 									while(tk.hasMoreTokens()){
 										StringTokenizer tk2 = new StringTokenizer(tk.nextToken(),"=");
 										String attr =null, value=null;
 										while(tk2.hasMoreTokens()){
 											attr = tk2.nextToken();
 											value = tk2.nextToken();
 											urlParamsObj.put(attr,value);
 										}
 									}
 									dataObject.put("urlparams", urlParamsObj);
 								}
 								thisSubUrl = new URL(urlStr);
 								logger.info("Pushing data to URL: " + thisSubUrl.toString() + " " + dataObject.toString());
 								if(thisSubUrl != null){
 									URLConnection urlConn = thisSubUrl.openConnection();
                                     urlConn.setDoInput (true);
 									urlConn.setDoOutput(true);
                                     urlConn.setUseCaches (false);
 									urlConn.setRequestProperty("Content-Type", "application/json");
 									OutputStreamWriter wr = new OutputStreamWriter(urlConn.getOutputStream());
                                     String dataStr = dataObject.toString();
                                     logger.info("Length: " + dataStr.getBytes().length);
 									wr.write(dataStr, 0, dataStr.length());
 									wr.flush();
 
 									BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
                                     /*String l = null;
                                     while((l=in.readLine())!=null)
                                         logger.info(l);*/
 
                                     wr.close();
 									in.close();
 								}
 							}
                 
                             else if(thisSubUriStr != null && 
                                         RESTServer.getResource(thisSubUriStr).getType()==ResourceUtils.PROCESS_PUBLISHER_RSRC){
                                     ProcessManagerResource.dataReceived(usid.toString(), dataObject);
                             }
 							
 							//this target is a model, lets see which instance to model thread to forward the data to
 							else if(thisSubUriStr != null && 
                                         RESTServer.getResource(thisSubUriStr).getType()==ResourceUtils.MODEL_RSRC){
 								logger.info("Looking up pipe for subid: " + usid.toString());
 								String destUri = database.getSubDestUriStr(usid);
 								UUID pubid = database.isPublisher(destUri, false);
 								//lookup the associated pipe and write to it
 								Pipe thisPipe = (Pipe)activePipesByPubId.get(pubid);
 								Semaphore thisSem = (Semaphore)readSemaphoresByPubId.get(pubid);
 								if(thisPipe != null){
 									logger.info("Associated pipe found: " + usid.toString());
 									Pipe.SinkChannel sink = thisPipe.sink();
 									sink.configureBlocking(true);
 								
 									//write dataByteBuf to pipe
 									if(thisPipe.sink().isOpen()){
 										logger.info(usid.toString() + ": Pipe open, writing to it");
 										int bytesWritten = writeDataToPipeSink(dataObject, thisSem, sink, usid);
 										if(bytesWritten != dataObject.toString().getBytes().length+4){
 											logger.warning("Warning: Bytes written not equal to buffer length;  " +
 															"bytesWritten=" + bytesWritten +
 															"write Expected=" + dataObject.toString().getBytes().length+4 +
 															" Subscription ID="  +
 															usid.toString());
 										}
 									} else {
 										logger.info(usid.toString() + ": Pipe closed, reopening");
 										//the pipe was closed
 										//create new pipe, start new thread, write to pipe
 										Resource model = RESTServer.getResource(thisSubUriStr);
 										if(model.TYPE == ResourceUtils.MODEL_RSRC){
 											Pipe newPipe = Pipe.open();
 											Semaphore s = new Semaphore(1, true);
 											ModelResource modelres = (ModelResource)model;
 											Thread modelresInstance = ModelResource.startNewModelThread(s, newPipe.source(),modelres);
 											newPipe.open();
 											activePipesByPubId.replace(pubid, newPipe);
 											readSemaphoresByPubId.replace(pubid, s);
 											writeDataToPipeSink(dataObject, s, newPipe.sink(), pubid);
 										} else {
 											logger.warning("Resource " + thisSubUriStr + " not a MODEL");
 										}
 									} 
 								} else {
 									//create new pipe for this subscription
 									logger.info(usid.toString() + ": No pipe, opening new one");
 									//create new pipe, start new thread, write to pipe
 									Resource model = RESTServer.getResource(thisSubUriStr);
 									if(model.TYPE == ResourceUtils.MODEL_RSRC){
 										Pipe newPipe = Pipe.open();
 										Semaphore s= new Semaphore(1, true);
 										ModelResource modelres = (ModelResource)model;
 										Thread modelresInstance = ModelResource.restartModelThread(s, newPipe.source(),modelres,pubid);
 										newPipe.open();
 										activePipesByPubId.replace(pubid, newPipe);
 										readSemaphoresByPubId.replace(pubid, s);
 										writeDataToPipeSink(dataObject, s, newPipe.sink(), pubid);
 									} else {
 										logger.warning("Resource " + thisSubUriStr + " not a MODEL");
 									}
 								}
 							}
 							
 							//subscription target is a model  -- OLD WAY OF DOING IT (BY SUBID)
 							/*else if(thisSubUriStr != null && !thisSubUriStr.contains("*"){
 								logger.info("Looking up pipe for subid: " + usid.toString());
 								//lookup the associated pipe and write to it
 								Pipe thisPipe = (Pipe)activePipes.get(usid);
 								Semaphore thisSem = (Semaphore)readSemaphores.get(usid);
 								if(thisPipe != null){
 									logger.info("Associated pipe found: " + usid.toString());
 									Pipe.SinkChannel sink = thisPipe.sink();
 									sink.configureBlocking(true);
 								
 									//write dataByteBuf to pipe
 									if(thisPipe.sink().isOpen()){
 										logger.info(usid.toString() + ": Pipe open, writing to it");
 										int bytesWritten = writeDataToPipeSink(dataObject, thisSem, sink, usid);
 										if(bytesWritten != dataObject.toString().getBytes().length+4){
 											logger.warning("Warning: Bytes written not equal to buffer length;  " +
 															"bytesWritten=" + bytesWritten +
 															"write Expected=" + dataObject.toString().getBytes().length+4 +
 															" Subscription ID="  +
 															usid.toString());
 										}
 									} else {
 										logger.info(usid.toString() + ": Pipe closed, reopening");
 										//the pipe was closed
 										//create new pipe, start new thread, write to pipe
 										Resource model = RESTServer.getResource(thisSubUriStr);
 										if(model.TYPE == ResourceUtils.MODEL_RSRC){
 											Pipe newPipe = Pipe.open();
 											Semaphore s = new Semaphore(1, true);
 											ModelResource modelres = (ModelResource)model;
 											Thread modelresInstance = ModelResource.startNewModelThread(s, newPipe.source(),modelres);
 											newPipe.open();
 											activePipes.replace(usid, newPipe);
 											readSemaphores.replace(usid, s);
 											writeDataToPipeSink(dataObject, s, newPipe.sink(), usid);
 										} else {
 											logger.warning("Resource " + thisSubUriStr + " not a MODEL");
 										}
 									} 
 								} else {
 									//create new pipe for this subscription
 									logger.info(usid.toString() + ": No pipe, opening new one");
 									//create new pipe, start new thread, write to pipe
 									Resource model = RESTServer.getResource(thisSubUriStr);
 									if(model.TYPE == ResourceUtils.MODEL_RSRC){
 										Pipe newPipe = Pipe.open();
 										Semaphore s= new Semaphore(1, true);
 										ModelResource modelres = (ModelResource)model;
 										Thread modelresInstance = ModelResource.restartModelThread(s, newPipe.source(),modelres,usid);
 										newPipe.open();
 										activePipes.replace(usid, newPipe);
 										readSemaphores.replace(usid, s);
 										writeDataToPipeSink(dataObject, s, newPipe.sink(), usid);
 									} else {
 										logger.warning("Resource " + thisSubUriStr + " not a MODEL");
 									}
 								}
 							}*/ else {
 								logger.info("Could not find target for subid: " + usid.toString());
 							}
 						}
 						catch(Exception e){
 							logger.log(Level.WARNING, "Error while forwarding data", e);
 							if(e instanceof ConnectException){
 								logger.log(Level.WARNING, "Could not connect to subscriber", e);
 							} else if(e instanceof InstantiationException){
 								logger.warning(e.getMessage());
 							}
 						}
 					}
 				}
 			}
 		} catch (Exception e){
 			logger.log(Level.WARNING, "", e);
 		}
 	}
 	
 	private static int writeDataToPipeSink(JSONObject dataObj, Semaphore s, Pipe.SinkChannel sink, UUID modelPubId){
 		try {
 			//write json object to byte buffer (prepended by buffer length)
 			byte[] data_obj_buf = (dataObj.toString().trim() + "\n").getBytes();
 			int bufferLength = data_obj_buf.length + 4;
 			byte[] data= new byte[bufferLength];
 			ByteBuffer dataByteBuf = ByteBuffer.wrap(data);
 		
 			//write the length of the data section followed by the data
 			dataByteBuf = dataByteBuf.putInt(bufferLength);
 			dataByteBuf = dataByteBuf.put(data_obj_buf);
 		
 			//write dataByteBuf to pipe
 			if(sink.isOpen()){
 				dataByteBuf.rewind();
 				int bytesWritten = sink.write(dataByteBuf);
 				if(bytesWritten != dataByteBuf.array().length){
 					logger.warning("Warning: Bytes written less then buffer length; Model_Publisher ID=" 
 									+ modelPubId.toString());
 				}
 				logger.finer("SUBMNGR:BEFORE_LEASE_COUNT=" + s.availablePermits());
 				s.release();
 				logger.finer("SUBMNGR:AFTER_LEASE_COUNT=" + s.availablePermits());
 				return bytesWritten;
 			}
 		} catch(Exception e){
 			logger.log(Level.WARNING, "",e);
 		}
 		
 		return 0;
 	}
 	
 	
 	public void removeSub(UUID subid){
 		try {
 			String destStr = database.getSubDestUriStr(subid);
 			logger.info("Removing subid: " + subid.toString() + "\n\tdestStr=" + destStr + "\n\tsub_count=" + database.getSubCountToModelPub(destStr));
 			if(destStr != null && destStr.startsWith("/models") && database.getSubCountToModelPub(destStr)==1){
 				UUID mpubid = database.isRRPublisher2(destStr);
 				logger.info("\n\tpubid_found=" + mpubid + "\n\tinternal_pipe_found=" + activePipesByPubId.containsKey(mpubid));
 				if(mpubid!=null && activePipesByPubId.containsKey(mpubid)){
 					logger.fine("Sending kill command to thread-" + mpubid.toString());
 					Pipe p = activePipesByPubId.get(mpubid);
 					Pipe.SinkChannel sink = p.sink();
 				
 					//send kill op to thread
 					JSONObject c = new JSONObject();
 					c.put("operation", "kill");
 					ByteBuffer dataByteBuf = ByteBuffer.wrap(c.toString().getBytes());
 					dataByteBuf.rewind();
 					int v = sink.write(dataByteBuf);
 					Thread.yield();
 					sink.close();
 
 					//signal message ready
 					Semaphore s = readSemaphoresByPubId.get(mpubid);
 					if(s!=null)
 						s.release();
 
 					//remove internal pub and semaphore
 					activePipesByPubId.remove(mpubid);
 					readSemaphoresByPubId.remove(mpubid);
 
 					Resource r = RESTServer.getResource(destStr);
 					//remove the associated publisher resource
 					RESTServer.removeResource(r);
 
 					//remove the publisher entry
 					database.removePublisher(mpubid);
 					database.removeRestResource(r.getURI());
 					
 					//remove from internal graph
 					Resource.removeFromMetadataGraph(r.getURI());
 				}
 			} else if (destStr != null && destStr.startsWith("/proc") && database.getSubCountToModelPub(destStr)==1){
                 UUID mpubid = database.isRRPublisher2(destStr);
                 if(mpubid!=null){
                     Resource r = (ProcessPublisherResource) RESTServer.getResource(destStr);
                     r.delete(null, true, new JSONObject());
 				}
             }
 		} catch (Exception e){
 			logger.log(Level.WARNING, "",e);
             
 		}
 		
 		//remove all subscriber from table
 		database.removeSubEntry(subid);
 	}
 
 	public void signalModelThreadKill(Resource r){
 		logger.fine("Thread-kill signaled from external resource");
 		try {
 			if(r !=null && r instanceof ModelGenericPublisherResource){
 				UUID mpubid = database.isRRPublisher2(r.getURI());
 				if(mpubid!=null && activePipesByPubId.containsKey(mpubid)){
 					logger.fine("Sending kill command to thread-" + mpubid.toString());
 					Pipe p = activePipesByPubId.get(mpubid);
 					Pipe.SinkChannel sink = p.sink();
 				
 					//send kill op to thread
 					JSONObject c = new JSONObject();
 					c.put("operation", "kill");
 					ByteBuffer dataByteBuf = ByteBuffer.wrap(c.toString().getBytes());
 					dataByteBuf.rewind();
 					int v = sink.write(dataByteBuf);
 					Thread.yield();
 					sink.close();
 
 					//signal message ready
 					Semaphore s = readSemaphoresByPubId.get(mpubid);
 					if(s!=null)
 						s.release();
 
 					//remove internal pub and semaphore
 					activePipesByPubId.remove(mpubid);
 					readSemaphoresByPubId.remove(mpubid);
 				} else {
 					logger.info("No associated pubid found for " + r.getURI());
 				}
 			}
 		} catch(Exception e){
 			logger.log(Level.WARNING, "", e);
 		}
 	}
 
 }
