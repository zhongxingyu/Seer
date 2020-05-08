 package com.successfactors.library.rest.resource;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 
 import org.json.JSONException;
 import org.restlet.ext.json.JsonRepresentation;
 import org.restlet.representation.Representation;
 import org.stringtree.json.JSONReader;
 import org.stringtree.json.JSONValidatingReader;
 
 import com.successfactors.library.rest.dao.SLUserDao;
 import com.successfactors.library.rest.model.SLUser;
 import com.successfactors.library.rest.utils.RestCallInfo;
 import com.successfactors.library.rest.utils.RestCallInfo.RestCallErrorCode;
 import com.successfactors.library.rest.utils.RestCallInfo.RestCallStatus;
 import com.successfactors.library.rest.utils.SLSessionManager;
 
 @SuppressWarnings({ "rawtypes", "unchecked"})
 @Path("user")
 public class UserResource {
 	
 	private SLUserDao slUserDao = SLUserDao.getDao();
 	
 	/**
 	 * 用户登录，成功返回SessionKey
 	 * */
	@POST
 	@Path("login")
 	@Produces("application/json")
 	public Representation login(Representation entity) {
 
 		String sessionKey = "";
 		
 		// 解析Json信息
 		JSONReader reader = new JSONValidatingReader();
 		Map comeInfo = null;
 		Map returnInfo = null;
 		
 		try {
 			comeInfo = (Map) reader.read(entity.getText());
 		} catch (IOException e) {
 			e.printStackTrace();
 			returnInfo = RestCallInfo.getInitRestCallInfo(
 					RestCallStatus.fail,
 					RestCallErrorCode.json_format_error);
 			return new JsonRepresentation(returnInfo);
 		}
 		
 		if (comeInfo == null || !comeInfo.containsKey("email") || !comeInfo.containsKey("password")) {
 			returnInfo = RestCallInfo.getInitRestCallInfo(
 					RestCallStatus.fail,
 					RestCallErrorCode.json_format_error);
 			return new JsonRepresentation(returnInfo);
 		}
 
 		String strEmail = comeInfo.get("email").toString();
 		String strPassword = comeInfo.get("password").toString();
 		
 		SLUser slUser = new SLUser();
 		slUser = slUserDao.getSLUserByEmail(strEmail);
 		if (slUser != null) {
 			if (!strPassword.equals(slUser.getUserPassword())) {
 				returnInfo = RestCallInfo.getInitRestCallInfo(
 						RestCallStatus.fail,
 						RestCallErrorCode.need_login);
 				return new JsonRepresentation(returnInfo);
 			} else {
 				sessionKey = SLSessionManager.newSession(slUser);
 			}
 		}
 
 		returnInfo = RestCallInfo.getInitRestCallInfo(
 				RestCallStatus.success,
 				null);
 		returnInfo.put("sessionKey", sessionKey);
 		return new JsonRepresentation(returnInfo);
 	}
 	
 	/**
 	 * 用户注销，删除相关Session
 	 * */
 	@GET
 	@Path("logout/{sessionKey}")
 	public void logout(@PathParam("sessionKey") String sessionKey) {
 		SLSessionManager.removeSession(sessionKey);
 	}
 
 	/**
 	 * 根据SessionKey获取用户信息
 	 * */
 	@GET
 	@Path("getUserInfo/{sessionKey}")
 	@Produces("application/json")
 	public Representation getUserInfo(@PathParam("sessionKey") String sessionKey) {
 		
 		SLUser ret = SLSessionManager.getSession(sessionKey);
 		JsonRepresentation retRep = new JsonRepresentation(ret);
 		try {
 			retRep.getJsonObject().put(RestCallInfo.REST_STATUS, RestCallStatus.success);
 			retRep.getJsonObject().put(RestCallInfo.REST_ERROR_CODE, "");
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		return retRep;
 	}
 
 	/**
 	 * 更新用户信息
 	 * */
 	@POST
 	@Path("updateuserinfo")
 	@Produces("application/json")
 	public Representation saveUserInfo(Representation entity) {
 		
 		JSONReader reader = new JSONValidatingReader();
 		HashMap result = null;
 		
 		HashMap returnInfo = new HashMap();
 		
 		try {
 			result = (HashMap) reader.read(entity.getText());
 		} catch (IOException e) {
 			e.printStackTrace();
 			returnInfo.put(RestCallInfo.REST_STATUS, RestCallStatus.fail);
 			returnInfo.put(RestCallInfo.REST_ERROR_CODE, RestCallErrorCode.json_format_error);
 			return new JsonRepresentation(returnInfo);
 		}
 		
 		if (result == null || !result.containsKey("sessionKey")) {
 			returnInfo.put(RestCallInfo.REST_STATUS, RestCallStatus.fail);
 			returnInfo.put(RestCallInfo.REST_ERROR_CODE, RestCallErrorCode.json_format_error);
 			return new JsonRepresentation(returnInfo);
 		}
 		String sessionKey = result.get("sessionKey").toString();
 		
 		SLUser slUser = SLSessionManager.getSession(sessionKey);
 		if (slUser == null) {
 			returnInfo.put(RestCallInfo.REST_STATUS, RestCallStatus.fail);
 			returnInfo.put(RestCallInfo.REST_ERROR_CODE, RestCallErrorCode.need_login);
 			return new JsonRepresentation(returnInfo);
 		}
 		
 		SLUser updatedUser = new SLUser();
 		updatedUser.parseMap(result);
 		
 		if (!updatedUser.getUserEmail().equals(slUser.getUserEmail())) {
 			returnInfo.put(RestCallInfo.REST_STATUS, RestCallStatus.fail);
 			returnInfo.put(RestCallInfo.REST_ERROR_CODE, RestCallErrorCode.can_not_modify_other_person);
 			return new JsonRepresentation(returnInfo);
 		}
 		
 
 		if (!slUserDao.update(slUser)) {
 			returnInfo.put(RestCallInfo.REST_STATUS, RestCallStatus.fail);
 			returnInfo.put(RestCallInfo.REST_ERROR_CODE, RestCallErrorCode.db_operate_error);
 			return new JsonRepresentation(returnInfo);
 		}
 
 		returnInfo.put(RestCallInfo.REST_STATUS, RestCallStatus.success);
 		returnInfo.put(RestCallInfo.REST_ERROR_CODE, RestCallErrorCode.no_error);
 		return new JsonRepresentation(returnInfo);
 	}
 
 	// ------------------------------------- Waiting --------------------------------------
 	
 //	/**
 //	 * 用户注册，成功返回注册信息，失败返回NULL
 //	 * */
 //	public SLUser register(SLUser newUser) {
 //		SLUser result = getUserByEmail(newUser.getUserEmail());
 //		if (result == null) {
 //			if (slUserDao.save(newUser)) {
 //				return newUser;
 //			}
 //		}
 //		return null;
 //	}
 //	/**
 //	 * 根据Email删除用户记录
 //	 * */
 //	public boolean deleteUserByEmail(String userEmail) {
 //		SLUser slUser = getUserByEmail(userEmail);
 //		if (slUser == null) {
 //			return false;
 //		} else if (slUserDao.remove(slUser)) {
 //			return true;
 //		}
 //		return false;
 //	}
 	
 }
