 package com.zarcode.data.resources.v1;
 
 import java.net.URLDecoder;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.SecurityContext;
 import javax.ws.rs.core.UriInfo;
 
 import com.dominicsayers.isemail.GeneralState;
 import com.dominicsayers.isemail.IsEMail;
 import com.dominicsayers.isemail.IsEMailResult;
 import com.google.gson.Gson;
 import com.zarcode.app.AppCommon;
 import com.zarcode.common.ApplicationProps;
 import com.zarcode.common.Util;
 import com.zarcode.data.dao.UserDao;
 import com.zarcode.data.dao.UserTokenDao;
 import com.zarcode.data.dao.WaterResourceDao;
 import com.zarcode.data.exception.BadUserDataProvidedException;
 import com.zarcode.data.exception.UnableToDecodeRequestException;
 import com.zarcode.data.model.LocalStatusDO;
 import com.zarcode.data.model.PingDataDO;
 import com.zarcode.data.model.ReadOnlyUserDO;
 import com.zarcode.data.model.RegisterTokenDO;
 import com.zarcode.data.model.SecurityTokenDO;
 import com.zarcode.data.model.UpdateTaskDO;
 import com.zarcode.data.model.UserDO;
 import com.zarcode.data.model.UserTokenDO;
 import com.zarcode.data.model.WaterResourceDO;
 import com.zarcode.data.resources.ResourceBase;
 import com.zarcode.platform.model.AppPropDO;
 import com.zarcode.security.AppRegister;
 import com.zarcode.security.BlockTea;
 
@Path("/v1/users")
 public class User extends ResourceBase {
 	
 	private Logger logger = Logger.getLogger(User.class.getName());
 	
 	@Context 
 	UriInfo uriInfo = null;
 	
 	@Context 
 	SecurityContext context = null;
     
 	String container = null;
 	
 	private static final int MAXPAGE = 10;
 	
 	private static final String ANONYMOUS_KEY = "ABCDEF7891011121314";
 	
 	@POST
 	@Produces("application/json")
 	@Path("/register")
 	public SecurityTokenDO register(String rawRegisterToken) {
 		UserDO res = null;
 		UserDao dao = null;
 		int rows = 0;
 		String resp = null;
 		UserDO newUser = null;
 		UpdateTaskDO task = null;
 		String emailAddr = null;
 		String displayName = null;
 		String value = null;
 		SecurityTokenDO sToken = null;
 		RegisterTokenDO rToken = null;
 		String registerSecret = null;
 		
 		requireSSL(context, logger);
 	
 		if (rawRegisterToken != null && rawRegisterToken.length() > 0) {
 			rToken = new Gson().fromJson(rawRegisterToken, RegisterTokenDO.class);
 			try {
 				emailAddr = rToken.getEmailAddr();
 				emailAddr = URLDecoder.decode(emailAddr);
 				logger.info("emailAddr [" + emailAddr + "]");
 				displayName = rToken.getDisplayName();
 				displayName = URLDecoder.decode(displayName);
 				logger.info("displayName [" + displayName + "]");
 				registerSecret = rToken.getRegisterSecret();
 				registerSecret = URLDecoder.decode(registerSecret);
 				logger.info("registerSecret [" + registerSecret + "]");
 			}
 			catch (Exception e1) {
 				logger.severe("Unable to decode incoming JSON from client -- " + Util.getStackTrace(e1));
 				throw new UnableToDecodeRequestException();
 			}
 		}
 
 		/*
 		 * Check register secret
 		 */
 		if (registerSecret != null) {
 			AppPropDO p = ApplicationProps.getInstance().getProp("REGISTER_SECRET");
 			BlockTea.BIG_ENDIAN = false;
 			String plainTextSecret = BlockTea.decrypt(registerSecret, ANONYMOUS_KEY);
 			String savedSecret = p.getStringValue();
 			if (!savedSecret.equalsIgnoreCase(plainTextSecret)) {
 				logger.severe("*** Register Secret [" + savedSecret + "] does not match incoming secret [" + plainTextSecret + "]");
 				throw new BadUserDataProvidedException();
 			}
 		}
 		
 		/*
 		 * Invalid email address
 		 */
 		try {
 			IsEMailResult result = IsEMail.is_email_verbose(emailAddr, false);
 			if (result.getState() != GeneralState.OK) {
 				logger.warning("*** Incoming email address is not VALID ***");
 				return (new SecurityTokenDO(66, "Email address is not VALID"));
 			}
 		}
 		catch (Exception e1) {
 			return (new SecurityTokenDO(66, "Incoming email address is not VALID [" + e1.getMessage() + "]"));
 		}
 	
 		/*
 		 * Duplicate email address
 		 */
 		dao = new UserDao();
 		if (dao.userExists(emailAddr)) {
 			return (new SecurityTokenDO(55, "Email address has already been registered"));
 		}
 		
 		try {
 			newUser = AppRegister.createNewUserAccountByEmailAddr2(emailAddr, displayName);
 			if (newUser != null) {
 				String nickname = newUser.getDisplayName();
 				String llId = newUser.getLLId();
 				logger.warning("Converting user acct to Security Token ---> dispayName=" + nickname + " llId=" + llId);
 				logger.warning("Getting private app paramters ...");
 				AppPropDO p0 = ApplicationProps.getInstance().getProp("PICASA_USER");
 				AppPropDO p1 = ApplicationProps.getInstance().getProp("PICASA_PASSWORD");
 				AppPropDO p2 = ApplicationProps.getInstance().getProp("FB_API_KEY");
 				AppPropDO p3 = ApplicationProps.getInstance().getProp("FB_SECRET");
 				logger.warning("Encrypting data and adding to SecurityToken ...");
 				sToken = new SecurityTokenDO();
 				sToken.encryptThenSetEmailAddr(emailAddr);
 				sToken.encryptThenSetLLId(llId);
 				sToken.setNickname(nickname);
 				sToken.encryptThenSetPicasaUser(p0.getStringValue());
 				sToken.encryptThenSetPicasaPassword(p1.getStringValue());
 				sToken.encryptThenSetFbKey(p2.getStringValue());
 				sToken.encryptThenSetFbSecret(p3.getStringValue());
 				logger.warning("SecurityToken is COMPLETE.");
 			}
 			else {
 				return (new SecurityTokenDO(50, "System is rejecting your registration. Please contact support."));
 			}
 		}
 		catch (Exception e) {
 			logger.severe("EXCEPTION :: " + e.getMessage());
 			return (new SecurityTokenDO(44, "System is rejecting your registration. Please contact support."));
 		}
 		return sToken;
 		
 	} // register
 
 	@POST
 	@Produces("application/json")
 	@Path("/update")
 	public UpdateTaskDO update(String rawUpdateTask) {
 		UserDO res = null;
 		UserDao dao = null;
 		int rows = 0;
 		String resp = null;
 		UserDO newUser = null;
 		UpdateTaskDO task = null;
 		String llId = null;
 		String object = null;
 		String field = null;
 		String value = null;
 		String plainText = null;
 		
 		
 		requireSSL(context, logger);
 		
 		if (rawUpdateTask != null && rawUpdateTask.length() > 0) {
 			task = new Gson().fromJson(rawUpdateTask, UpdateTaskDO.class);
 			try {
 				llId = task.getLlId();
 				logger.info("BEFORE llId [" + llId + "]");
 				// llId = new URI(llId).toASCIIString();
 				llId = URLDecoder.decode(llId);
 				logger.info("AFTER llId [" + llId + "]");
 				field = task.getField();
 				value = task.getValue();
 				// value = new URI(value).toASCIIString();
 				value = URLDecoder.decode(value);
 			}
 			catch (Exception e1) {
 				logger.warning("EXCEPTION ::: " + Util.getStackTrace(e1));
 			throw new BadUserDataProvidedException();
 			}
 		}
 		
 		if (llId == null || llId.length() == 0) {
 			logger.warning("*** Incoming llId is not VALID ***");
 			throw new BadUserDataProvidedException();
 		}
 		
 		if (!AppCommon.ANONYMOUS.equalsIgnoreCase(llId)) {
 			AppPropDO prop = ApplicationProps.getInstance().getProp("CLIENT_TO_SERVER_SECRET");
 			BlockTea.BIG_ENDIAN = false;
 			plainText = BlockTea.decrypt(llId, prop.getStringValue());
 			logger.info("Decrypted llId: " + plainText + " Encrypted llId: " + llId);
 			llId = plainText;
 		}
 		else {
 			logger.warning("*** Anonymous user can update the displayName ***");
 			throw new BadUserDataProvidedException();
 		}
 		
 		dao = new UserDao();
 		res = dao.getUserByIdClear(plainText);
 		if (res != null) {
 			try {
 				if (field != null && field.equalsIgnoreCase("displayName")) {
 					newUser = dao.updateDisplayName(res, value);
 				}
 				if (field != null && field.equalsIgnoreCase("profileUrl")) {
 					if ("NULL".equalsIgnoreCase(value)) {
 						value = null;
 					}
 					newUser = dao.updateProfileUrl(res, value);
 				}
 				if (newUser != null) {
 					task.setResult(1);
 				}
 			}
 			catch (Exception e) {
 				logger.severe("[EXCEPTION]\n" + Util.getStackTrace(e));
 			}
 		}
 		else {
 			logger.warning("**** Unable to find user account with llId = " + llId);
 		}
 		return task;
 	}
 	
 	/*
 	@GET
 	@Produces({"application/json", "application/xml"})
 	@Path("/active")
     private List<UserDO> getActiveUsers() {
     */
     private List<UserDO> getActiveUsers() {
 		List<UserDO> activeUsers = null;
 		UserDao userDao = null;
 		
 		try {
 			userDao = new UserDao();
 			activeUsers = userDao.getActiveUsers();
 		}
 		catch (Exception e) {
 			logger.severe("[EXCEPTION]\n" + Util.getStackTrace(e));
 		}
 		logger.info("# of users returned: " + (activeUsers == null ? 0 : activeUsers.size()));
         return activeUsers;
 	}
 	
 	private void testEncrypt() {
 		String src = "MY_DEVICE_ID=48A9371F-DADD-5CA2-B491-A425DB3C5C8A";
 		logger.info("*** Test 1");
 		BlockTea.BIG_ENDIAN = false;
 		String encrypted = BlockTea.encrypt(src, "ABCDEF7891011121314");
 		logger.info("Encrypted ---->" + encrypted + "<---");
 		String decrypted = BlockTea.decrypt(encrypted, "ABCDEF7891011121314");
 		logger.info("Decrypted ---->" + decrypted + "<---");
 	}
 	
 	@POST
 	@Produces("application/json")
 	@Path("/ping")
     public LocalStatusDO ping(String rawPingData) {
 		String resp = null;
 		UserDao userDao = null;
 		UserDO user = null;
 		PingDataDO pingData = null;
 		ReadOnlyUserDO readOnlyUser = null;
 		WaterResourceDao waterResDao = null;
 		UserTokenDao tokenDao = null;
 		List<UserDO> usersAtLake = null;
 		List<UserDO> totalActiveUsers = null;
 		LocalStatusDO empty = new LocalStatusDO();
 		UserTokenDO userToken = null;
 		boolean anonymous = false;
 		
 		String llId = null;
 		String idClear = null;
 		double lat = 0;
 		double lng = 0;
 		String deviceId = null;
 		
 		requireSSL(context, logger);
 		
 		if (rawPingData != null && rawPingData.length() > 0) {
 			pingData = new Gson().fromJson(rawPingData, PingDataDO.class);
 			try {
 				llId = pingData.getLlId();
 				logger.info("BEFORE llId [" + llId + "]");
 				llId = URLDecoder.decode(llId);
 				logger.info("AFTER llId [" + llId + "]");
 				lat = pingData.getLat();
 				lng = pingData.getLng();
 				deviceId = pingData.getDeviceId();
 				deviceId = URLDecoder.decode(deviceId);
 			}
 			catch (Exception e1) {
 				logger.severe("Unable to decode incoming JSON from client -- " + Util.getStackTrace(e1));
 				throw new UnableToDecodeRequestException();
 			}
 		}
 		
 		if (llId == null || llId.length() == 0) {
 			return empty;
 		}
 		
 		if (AppCommon.ANONYMOUS.equalsIgnoreCase(llId)) {
 			anonymous = true;
 			logger.info("ANONYMOUS user accessing the service.");
 		}
 		else {
 			AppPropDO prop = ApplicationProps.getInstance().getProp("CLIENT_TO_SERVER_SECRET");
 			BlockTea.BIG_ENDIAN = false;
 			String plainText = BlockTea.decrypt(llId, prop.getStringValue());
 			logger.info("Decrypted llId: " + plainText + " Encrypted llId: " + llId);
 			idClear = plainText;
 		}
 		
 		
 		//
 		// status to return to user
 		//
 		LocalStatusDO status = new LocalStatusDO();
 		
 		try {
 			userDao = new UserDao();
 			if (anonymous) {
 				BlockTea.BIG_ENDIAN = false;
 				logger.info("Incoming deviceId: " + deviceId);
 				String plainText = BlockTea.decrypt(deviceId, ANONYMOUS_KEY);
 				String [] keys = plainText.split("=");
 				if (keys.length == 2 && keys[0].equalsIgnoreCase("MY_DEVICE_ID")) {
 					readOnlyUser = userDao.getReadOnlyUsersByDeviceId(keys[1]);
 					//
 					// if null, add the anonymous user
 					//
 					if (readOnlyUser == null) {
 						logger.info("Adding anonymous user --- deviceId=" + keys[1]);
 						readOnlyUser = new ReadOnlyUserDO();
 						readOnlyUser.setDeviceId(keys[1]);
 						userDao.addReadOnlyUser(readOnlyUser);
 					}
 					else {
 						logger.info("Found previously anonymous user from device id -- " + keys[1]);
 					}
 				}
 				else {
 					logger.severe("*** A client is trying to access service with invalid deviceId --> " + plainText);
 					throw new BadUserDataProvidedException();
 				}
 			}
 			else {
 				user = userDao.getAndUpdateUser(idClear, lat, lng);
 				tokenDao = new UserTokenDao();
 				userToken = tokenDao.getTokenByIdClear(idClear);
 				if (userToken == null || userToken.isExpired()) {
 					userToken = tokenDao.generateTokenByIdClear(idClear);
 					logger.info("Generated a new user token=" + userToken.getToken() + " for llId=" + llId);
 				}
 				status.setUserToken(userToken.getToken());
 			}
 	
 			waterResDao = new WaterResourceDao();
 			//
 			// best indicates that the user is inside my water resource polygon
 			//
 			WaterResourceDO best = waterResDao.findBestResource(lat, lng);
 			//
 			// build status to return to user
 			//
 			if (best != null) {
 				status.setUserOnWater(true);
 				Long resId = best.getResourceId();
 				if (resId != null) {
 					status.setResourceId(resId.intValue());
 				}
 				status.setResourceState(best.getState());
 				status.setResourceName(best.getName());
 				status.setResourceLastUpdate(best.getLastUpdate());
 				if (anonymous) {
 					userDao.updateResourceAnonymous(readOnlyUser.getUserId(), best.getResourceId());
 					logger.info("Updated anonymous user [ " + readOnlyUser.getDeviceId() + " ] at resourceId=" +  best.getResourceId());
 				}
 				else {
 					userDao.updateResource(user.getUserId(), best.getResourceId());
 					logger.info("Updated user [ " + user.getDisplayName() + " ] at resourceId=" +  best.getResourceId());
 				}
 				usersAtLake = userDao.getUsersByResourceId(best.getResourceId());
 				int totalUsers = userDao.getTotalUsersByResourceId(best.getResourceId());
 				status.setHowManyOnWater(totalUsers);
 			}
 			else {
 				if (anonymous) {
 					userDao.updateResourceAnonymous(readOnlyUser.getUserId(), null);
 				}
 				else {
 					userDao.updateResource(user.getUserId(), null);
 					
 				}
 			}
 			totalActiveUsers = userDao.getActiveUsers();
 			if (totalActiveUsers != null) {
 				status.setNumOfLazyLakers(totalActiveUsers.size());
 			}
 		}
 		catch (Exception e) {
 			logger.severe("[EXCEPTION]\n" + Util.getStackTrace(e));
 		}
         return status;
     }
 
 	/*
 	@GET 
 	@Path("/local")
 	@Produces("application/json")
 	public List<UserDO> getLocalUsers(@QueryParam("lat") double lat, @QueryParam("lng") double lng) {
 	*/
 	private List<UserDO> getLocalUsers(double lat, double lng) {
 		List<UserDO> list = null;
 		List<UserDO> empty = new ArrayList<UserDO>();
 		UserDao userDao = null;
 		
 		logger.info("Entered");
 		try {
 			userDao = new UserDao();
 			list = userDao.findClosest(lat, lng);
 		}
 		catch (Exception e) {
 			logger.severe("[EXCEPTION]\n" + Util.getStackTrace(e));
 		}
 		logger.info("Exit");
 		return (list == null ? empty : list);
 		
 	} // getLocalUsers
 
 	/*
 	@GET 
 	@Path("/lake/{resourceId}")
 	@Produces("application/json")
 	public List<UserDO> getUsersByResourceId(@PathParam("resourceId") Long resourceId) {
 	*/
 	private List<UserDO> getUsersByResourceId(Long resourceId) {
 		List<UserDO> list = null;
 		List<UserDO> empty = new ArrayList<UserDO>();
 		UserDao userDao = null;
 		
 		logger.info("Entered");
 		try {
 			userDao = new UserDao();
 			list = userDao.getUsersByResourceId(resourceId);
 		}
 		catch (Exception e) {
 			logger.severe("[EXCEPTION]\n" + Util.getStackTrace(e));
 		}
 		logger.info("Exit");
 		
 		return (list == null ? empty : list);
 		
 	} 
 	
 	
 } // User
