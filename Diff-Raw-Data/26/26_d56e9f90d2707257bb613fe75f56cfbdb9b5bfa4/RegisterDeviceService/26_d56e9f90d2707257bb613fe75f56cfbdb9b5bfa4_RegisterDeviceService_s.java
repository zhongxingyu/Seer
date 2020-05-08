 package com.orange.groupbuy.api.service;
 
 import javax.servlet.http.HttpServletRequest;
 
 import net.sf.json.JSONObject;
 
 import com.mongodb.BasicDBObject;
 import com.orange.common.api.service.CommonService;
 import com.orange.common.mongodb.MongoDBClient;
 import com.orange.groupbuy.constant.ErrorCode;
 import com.orange.groupbuy.constant.ServiceConstant;
 import com.orange.groupbuy.manager.UserManager;
 
 public class RegisterDeviceService extends CommonGroupBuyService {
 
 	String appId;
 	String deviceModel;
 	String deviceId;
 	String deviceOS;
 	String deviceToken;	
 	String language;
 	String countryCode;
 	
 	@Override
 	public void handleData() {
 
 		// TODO check if device exist
		if (UserManager.findUserByDeviceId(mongoClient, deviceId) == null){
 			log.info("<registerDevice> user deviceId("+deviceId+") exist");
 			resultCode = ErrorCode.ERROR_LOGINID_DEVICE_BOTH_EXIST;
 			return;
 		}
 		
 		BasicDBObject user = UserManager.createDeviceUser(mongoClient, appId,
 				deviceModel, deviceId, deviceOS,
 				deviceToken, language, countryCode);
 
 		if (user == null){
 			resultCode = ErrorCode.ERROR_CREATE_USER;
 			log.info("<registerDevice> fail to create user");
 			return;
 		} 
 		
 		String userId = user.getString(MongoDBClient.ID);
 		
 		// set result data, return userId
 		JSONObject obj = new JSONObject();
 		obj.put(ServiceConstant.PARA_USERID, userId);
 		resultData = obj;
 	}
 
 	@Override
 	public boolean needSecurityCheck() {
 		return false;
 	}
 
 	@Override
 	public void printData() {
 		log.info(toString());
 	}
 
 	@Override
 	public boolean setDataFromRequest(HttpServletRequest request) {
 		deviceId = request.getParameter(ServiceConstant.PARA_DEVICEID);
 		deviceModel = request.getParameter(ServiceConstant.PARA_DEVICEMODEL);
 		deviceOS = request.getParameter(ServiceConstant.PARA_DEVICEOS);
 		countryCode = request.getParameter(ServiceConstant.PARA_COUNTRYCODE);
 		language = request.getParameter(ServiceConstant.PARA_LANGUAGE);
 		appId = request.getParameter(ServiceConstant.PARA_APPID);
 		deviceToken = request.getParameter(ServiceConstant.PARA_DEVICETOKEN);
 		
 //		if (!check(loginIdType, ErrorCode.ERROR_PARAMETER_USERTYPE_EMPTY, ErrorCode.ERROR_PARAMETER_USERTYPE_NULL))
 //			return false;
 		
 		if (!check(deviceId, ErrorCode.ERROR_PARAMETER_DEVICEID_EMPTY, ErrorCode.ERROR_PARAMETER_DEVICEID_NULL))
 			return false;
 
 		if (!check(deviceModel, ErrorCode.ERROR_PARAMETER_DEVICEMODEL_EMPTY, ErrorCode.ERROR_PARAMETER_DEVICEMODEL_NULL))
 			return false;
 
 		if (!check(deviceOS, ErrorCode.ERROR_PARAMETER_DEVICEOS_EMPTY, ErrorCode.ERROR_PARAMETER_DEVICEOS_NULL))
 			return false;
 
 //		if (!check(countryCode, ErrorCode.ERROR_PARAMETER_COUNTRYCODE_EMPTY, ErrorCode.ERROR_PARAMETER_COUNTRYCODE_NULL))
 //			return false;
 //
 //		if (!check(language, ErrorCode.ERROR_PARAMETER_LANGUAGE_EMPTY, ErrorCode.ERROR_PARAMETER_LANGUAGE_NULL))
 //			return false;
 
 		if (!check(appId, ErrorCode.ERROR_PARAMETER_APPID_EMPTY, ErrorCode.ERROR_PARAMETER_APPID_NULL))
 			return false;
 		
 		return true;
 	}
 
 }
