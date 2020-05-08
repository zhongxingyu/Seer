 package com.zarcode.data.resources.v1;
 
 import java.net.URLDecoder;
 import java.nio.ByteBuffer;
 import java.text.Format;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 import java.util.logging.Logger;
 
 import javax.jdo.JDOObjectNotFoundException;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.HttpHeaders;
 import javax.ws.rs.core.Request;
 import javax.ws.rs.core.SecurityContext;
 import javax.ws.rs.core.UriInfo;
 
 import com.google.gson.Gson;
 import com.zarcode.app.AppCommon;
 import com.zarcode.common.ApplicationProps;
 import com.zarcode.common.Util;
 import com.zarcode.data.dao.BuzzDao;
 import com.zarcode.data.dao.HotSpotDao;
 import com.zarcode.data.dao.PegCounterDao;
 import com.zarcode.data.dao.UserDao;
 import com.zarcode.data.dao.WaterResourceDao;
 import com.zarcode.data.exception.BadRequestAppDataException;
 import com.zarcode.data.exception.BadUserDataProvidedException;
 import com.zarcode.data.exception.UnableToDecodeRequestException;
 import com.zarcode.data.maint.PegCounter;
 import com.zarcode.data.model.BuzzMsgDO;
 import com.zarcode.data.model.CommentDO;
 import com.zarcode.data.model.HotSpotDO;
 import com.zarcode.data.model.UserDO;
 import com.zarcode.data.model.WaterResourceDO;
 import com.zarcode.platform.model.AppPropDO;
 import com.zarcode.security.BlockTea;
 
 import com.zarcode.data.resources.ResourceBase;
 
 @Path("/v1/buzz")
 public class Buzz extends ResourceBase {
 	
 	private Logger logger = Logger.getLogger(Buzz.class.getName());
 	
 	private PegCounterDao pegCounter = new PegCounterDao();
 	
 	@Context 
 	HttpHeaders headers = null;
 	
 	@Context 
 	SecurityContext context = null;
 	
 	@Context 
 	UriInfo uriInfo = null;
     
 	@Context 
     Request request = null;
 	
 	String container = null;
 	
 	private static final int MAXPAGE = 10;
 	
 	@POST
 	@Produces("application/json")
 	@Path("/{resourceId}/newMsg")
 	public BuzzMsgDO addBuzzMsgToLake(@PathParam("resourceId") Long resourceId, @QueryParam("addToMyHotSpots") boolean addToMyHotSpots, String rawBuzzMsg) {
 		List<BuzzMsgDO> res = null;
 		BuzzDao dao = null;
 		WaterResourceDao waterResDao = null;
 		UserDao userDao = null;
 		int rows = 0;
 		BuzzMsgDO buzzMsg = null;
 		BuzzMsgDO newBuzzMsg = null;
 		String llId = null;
 	
 		logger.info("Process Incoming JSON Buzz Msg: " + rawBuzzMsg);
 		
 		requireSSL(context, logger);
 		
 		if (rawBuzzMsg != null && rawBuzzMsg.length() > 0) {
 			buzzMsg = new Gson().fromJson(rawBuzzMsg, BuzzMsgDO.class);
 			try {
 				if (buzzMsg != null) {
 					buzzMsg.postCreation();
 				
 					/*
 					 * check if user is ANONYMOUS
 					 */
 					if (AppCommon.ANONYMOUS.equalsIgnoreCase(buzzMsg.getLlId())) {
 						logger.warning("*** REJECTED -- ANONYMOUS users cannot post buzzMsgs");
 						throw new BadUserDataProvidedException();
 					}
 				
 					//
 					// check if this is a REAL user
 					//
 					userDao = new UserDao();
 					UserDO user = userDao.getUserByIdClear(buzzMsg.getIdClear());
 					if (user == null) {
 						logger.warning("*** REJECTED -- Unable to find a matching user for llId=" + llId);
 						throw new BadUserDataProvidedException();
 					}
 				
 					//
 					// message is saved with a clear llId
 					//
 					AppPropDO p2 = ApplicationProps.getInstance().getProp("SERVER_TO_CLIENT_SECRET");
 					BlockTea.BIG_ENDIAN = false;
 					String llIdCipherText = BlockTea.encrypt(buzzMsg.getIdClear(), p2.getStringValue());
 					buzzMsg.setLlId(llIdCipherText);
 					
 					
 					dao = new BuzzDao();
 					newBuzzMsg = dao.addMsg(buzzMsg);
 					PegCounter.incr(PegCounter.NO_BUZZ_MSG, PegCounter.DAILY);
 					PegCounter.incr(PegCounter.NO_BUZZ_COMMENTS, PegCounter.DAILY);
 					
 					//
 					// since event was created inside lake area, update last communication
 					//
 					if (buzzMsg.getResourceId() > 0) {
 						try {
 							waterResDao = new WaterResourceDao();
 							waterResDao.updateLastUpdate(buzzMsg.getResourceId());
 							logger.info("Updated lastUpdated for resource=" + buzzMsg.getResourceId());
 						}
 						catch (JDOObjectNotFoundException ex) {
 							logger.severe("Unable to update lastUpdated timestamp for water resource");
 							throw new BadRequestAppDataException();
 						}
 					}
 					logger.info("Successfully added new buzzMsg -- " + newBuzzMsg);
 					
 					/*
 					 * if user requested it, we update this HotSpots for this location as well
 					 */
 					if (addToMyHotSpots) {
 						logger.info("Adding generic hotspot for user idClear=" + buzzMsg.getIdClear());
 						HotSpotDO spot = new HotSpotDO();
 						spot.setLLId(buzzMsg.getLlId());
 						spot.setIdClear(buzzMsg.getIdClear());
 						spot.setLocation(buzzMsg.getLocation());
 						spot.setDesc("HotSpot @" + buzzMsg.getLocation());
 						spot.setNotes("*** Generated from Buzz Msg [ " + buzzMsg.getUserLocalTime() + " ] ***");
 						spot.setLat(buzzMsg.getLat());
 						spot.setLng(buzzMsg.getLng());
 						spot.setRating(0);
 						spot.setResourceId(buzzMsg.getResourceId());
 						spot.postCreation();
 						HotSpotDao hotSpotDao = new HotSpotDao();
 						hotSpotDao.addHotSpot(spot);
 					}
 				}
 			}
 			catch (Exception e) {
 				logger.severe("[EXCEPTION]\n" + Util.getStackTrace(e));
 				throw new BadRequestAppDataException();
 			}
 		}
 		else {
 			throw new UnableToDecodeRequestException();
 		}
 		return newBuzzMsg;
 	}
 	
 	@POST
 	@Produces("application/json")
 	@Path("/{resourceId}/comment")
	public CommentDO addCommentToBuzzMsg(@PathParam("resourceId") Long resourceId, @QueryParam("id") String id, String rawCommentObj) {
 		List<BuzzMsgDO> res = null;
 		BuzzDao dao = null;
 		UserDao userDao = null;
 		WaterResourceDao waterResDao = null;
 		int rows = 0;
 		CommentDO comm = null;
 		CommentDO newComm = null;
 		String llId = null;
 	
 		logger.info("Process NEW comment: " + rawCommentObj);
 		
 		requireSSL(context, logger);
 		
 		
 		if (rawCommentObj != null && rawCommentObj.length() > 0) {
 			comm = new Gson().fromJson(rawCommentObj, CommentDO.class);
 			try {
 				if (comm != null && comm.getMsgId() > 0) {
 					comm.postCreation();
 					
 					if (AppCommon.ANONYMOUS.equalsIgnoreCase(comm.getLlId())) {
 						logger.warning("*** REJECTED -- ANONYMOUS users cannot post buzzMsgs");
 						throw new BadUserDataProvidedException();
 					}
 					
 					//
 					// check if this is a REAL user
 					//
 					userDao = new UserDao();
 					UserDO user = userDao.getUserByIdClear(comm.getIdClear());
 					if (user == null) {
 						logger.warning("*** REJECTED -- Unable to find a matching user for llId=" + llId);
 						throw new BadUserDataProvidedException();
 					}
 					
 					userDao.updateProfileUrl(user, comm.getProfileUrl());
 					dao = new BuzzDao();
 					newComm = dao.addComment(comm);
 					newComm.postReturn();
 					dao.incrementCommentCounter(comm.getMsgId());
 					//
 					// since event was created inside lake area, update last communication
 					//
 					if (comm.getResourceId() > 0) {
 						try {
 							waterResDao = new WaterResourceDao();
 							waterResDao.updateLastUpdate(comm.getResourceId());
 							logger.info("Updated lastUpdated for resource=" + comm.getResourceId());
 						}
 						catch (JDOObjectNotFoundException ex) {
 							logger.severe("Unable to update lastUpdated timestamp for water resource");
 							throw new BadRequestAppDataException();
 						}
 					}
 					
 					logger.info("Successfully added new comment -- " + newComm);
 				}
 				else {
 					logger.severe("*** Incoming comment did not contain REQUIRED msgEventId ***");
 					throw new BadRequestAppDataException();
 				}
 			}
 			catch (Exception e) {
 				logger.severe("[EXCEPTION]\n" + Util.getStackTrace(e));
 				throw new BadRequestAppDataException();
 			}
 		}
 		else {
 			throw new UnableToDecodeRequestException();
 		}
 		logger.info("Returning: " + newComm);
 		
 		return newComm;
 	}
 	
 	@GET 
 	@Path("/bylatlng")
 	@Produces("application/json")
 	public List<BuzzMsgDO> getMsgEventsByLatLng(@QueryParam("lat") double lat, @QueryParam("lng") double lng) {
 		int i = 0;
 		List<BuzzMsgDO> results = null;
 		List<BuzzMsgDO> list = null;
 		List<WaterResourceDO> resourceList = null;
 		WaterResourceDO res = null;
 		BuzzDao buzzDao = null;
 		WaterResourceDao waterResDao = null;
 		boolean bFindAll = false;
 		
 		logger.info("Entered");
 		
 		try {
 			buzzDao = new BuzzDao();
 			results = new ArrayList<BuzzMsgDO>();
 			waterResDao = new WaterResourceDao();
 			logger.info("QUERY: Searching for local water resources ...");
 			resourceList = waterResDao.findClosest(lat, lng, 3);
 			//
 			// if we have found some local lakes, let's see what recents events that they 
 			// have
 			//
 			if (resourceList != null && resourceList.size() > 0) {
 				logger.info("RESULT: Found " + resourceList.size() + " local lakes ...");
 				for (i=0; i<resourceList.size(); i++) {
 					res = resourceList.get(i);
 					list = buzzDao.getNextEventsByResourceId(res.getResourceId());
 					if (list != null && list.size() > 0) {
 						logger.info("Num of message(s): " + list.size() + " -- at resource --> "  + res.getName() + 
 								" id=" +  res.getResourceId());
 						results.addAll(list);
 					}
 					else {
 						logger.info("ZERO messages at resource --> " + res.getName() + 
 								" id=" + res.getResourceId());
 					}
 				}
 				logger.info("# of recent event(s) from resources --> " + (results == null ? 0 : results.size()));
 			}
 			else {
 				logger.info("RESULT: No Matches.");
 			}
 			//
 			// sort events
 			//
 			if (results.size() > 0) {
 				Collections.sort(results);
 				if (results.size() >= BuzzDao.PAGESIZE) {
 					int start = results.size() - BuzzDao.PAGESIZE;
 					results = results.subList(start, BuzzDao.PAGESIZE);
 				}
 			}
 		
 		}
 		catch (Exception e) {
 			logger.severe("[EXCEPTION]\n" + Util.getStackTrace(e));
 			throw new BadRequestAppDataException();
 		}
 		logger.info("Exit");
 		
 		return results;
 		
 	}
 	
 	private void testEncrypt() {
 		AppPropDO prop = ApplicationProps.getInstance().getProp("SERVER_TO_CLIENT_SECRET");
 		byte key[] = prop.getStringValue().getBytes();
 		String src = "lrkirven@gmail.com000";
 		byte plainSource[] = src.getBytes();
 		logger.info("*** Test 1");
 		BlockTea.BIG_ENDIAN = false;
 		String encrypted = BlockTea.encrypt(src, prop.getStringValue());
 		logger.info("Encrypted ---->" + encrypted + "<---");
 		String decrypted = BlockTea.decrypt(encrypted, prop.getStringValue());
 		logger.info("Decrypted ---->" + decrypted + "<---");
 		
 		logger.info("*** Test 2");
 		String str = "hello world";
 		try {
 			int[] l = BlockTea.strToLongs2(str.getBytes("UTF-8"));
 			for (int i=0; i<l.length; i++) {
 				logger.info(i + ") " + Integer.toHexString(l[i])); 
 			}
 			ByteBuffer buf = BlockTea.longsToStr2(l);
 			logger.info("RESULT: " + new String(buf.array()));
 		}
 		catch (Exception e) {
 		}
 	}
 	
 	@GET 
 	@Path("/{resourceId}")
 	@Produces("application/json")
 	public List<BuzzMsgDO> getBuzzMsgsByLake(@PathParam("resourceId") Long resourceId, @QueryParam("lat") double lat, @QueryParam("lng") double lng) {
 		int i = 0;
 		List<BuzzMsgDO> results = null;
 		List<BuzzMsgDO> list = null;
 		List<WaterResourceDO> resourceList = null;
 		WaterResourceDO res = null;
 		BuzzDao eventDao = null;
 		WaterResourceDao waterResDao = null;
 		boolean bFindAll = false;
 		
 		try {
 			eventDao = new BuzzDao();
 			
 			if (bFindAll) {
 				results = new ArrayList<BuzzMsgDO>();
 				waterResDao = new WaterResourceDao();
 				logger.info("QUERY: Searching for local water resources ...");
 				resourceList = waterResDao.findClosest(lat, lng, 3);
 				//
 				// if we have found some local lakes, let's see what recents events that they 
 				// have
 				//
 				if (resourceList != null && resourceList.size() > 0) {
 					logger.info("RESULT: Found " + resourceList.size() + " local lakes ...");
 					for (i=0; i<resourceList.size(); i++) {
 						res = resourceList.get(i);
 						list = eventDao.getNextEventsByResourceId(res.getResourceId());
 						if (list != null && list.size() > 0) {
 							results.addAll(list);
 						}
 					}
 					logger.info("# of recent event(s) from resources --> " + (results == null ? 0 : results.size()));
 				}
 				else {
 					logger.info("RESULT: No Matches.");
 				}
 				//
 				// sort events
 				//
 				if (results.size() > 0) {
 					Collections.sort(results);
 					if (results.size() >= BuzzDao.PAGESIZE) {
 						int start = results.size() - BuzzDao.PAGESIZE;
 						results = results.subList(start, BuzzDao.PAGESIZE);
 					}
 				}
 			}
 			else {
 				logger.info("Trying query with resourceId=" + resourceId);
 				list = eventDao.getNextEventsByResourceId(resourceId);
 				results = list;
 				if (results != null && results.size() > BuzzDao.PAGESIZE) {
 					results = results.subList(0, BuzzDao.PAGESIZE);
 				}
 			}
 		}
 		catch (Exception e) {
 			logger.severe("[EXCEPTION]\n" + Util.getStackTrace(e));
 			throw new BadRequestAppDataException();
 		}
 		logger.info("Exit");
 		
 		return results;
 		
 	}
 	
 } // Buzz
