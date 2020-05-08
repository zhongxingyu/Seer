 package com.orange.place.api.service;
 
 import javax.servlet.http.HttpServletRequest;
 
 import com.orange.place.constant.ErrorCode;
 import com.orange.place.constant.ServiceConstant;
 import com.orange.place.dao.User;
 import com.orange.place.manager.UserManager;
 
 public class RegisterUserService extends CommonService {
 
 	String userId;
 	String loginId;
 	String loginIdType;
 	String appId;
 	String deviceModel;
 	String deviceId;
 	String deviceOS;
 	String deviceToken;	
 
 	String language;
 	String countryCode;
 	String nickName;
 	String avatar;
 	String accessToken;
 	String accessTokenSecret;
 	String province;
 	String city;
 	String location;
 	String gender;
 	String birthday;
 	String sinaNickName;
 	String sinaDomain;
 	String qqNickName;
 	String qqDomain;
 	
 	String password;
 	
 	@Override
 	public void handleData() {
 		// TODO Auto-generated method stub
 		
 		boolean isLoginIdExist = UserManager.isLoginIdExist(cassandraClient, loginId, loginIdType);
 		boolean isDeviceIdExist = UserManager.isDeviceIdExist(cassandraClient, deviceId);
 		if (isLoginIdExist && isDeviceIdExist){
 			log.info("<registerUser> user loginId("+loginId+") and deviceId("+deviceId+") exist");
 			resultCode = ErrorCode.ERROR_LOGINID_DEVICE_BOTH_EXIST;
 			return;
 		}
 		else if (isLoginIdExist){
 			resultCode = ErrorCode.ERROR_LOGINID_EXIST;
 			log.info("<registerUser> user loginId exist, loginId="+loginId);
 			return;
 		}
 		else if (isDeviceIdExist){
 			resultCode = ErrorCode.ERROR_DEVICEID_BIND;
 			log.info("<registerUser> user deviceId exist, deviceId="+deviceId);
 			return;
 		}		
 		
 		User user = UserManager.createUser(cassandraClient, loginId, loginIdType, appId,
 				deviceModel, deviceId, deviceOS,
 				deviceToken, language, countryCode,
 				password, 
 				nickName, avatar,
 				accessToken, accessTokenSecret,
 				province, city, location, gender, birthday,
 				sinaNickName, sinaDomain, qqNickName, qqDomain);
 
 		if (user == null){
 			resultCode = ErrorCode.ERROR_CREATE_USER;
 			log.info("<registerUser> fail to create user");
 			return;
 		}
 		
 		// TODO if there is any exception, shall clean all inconsistent data and index
 	}
 
 	@Override
 	public boolean needSecurityCheck() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public void printData() {
 		// TODO Auto-generated method stub
 		log.info(String.format("userId=%s, loginId=%s, loginIdType=%s, appId=%s, deviceModel=%s, " +
 				"deviceId=%s, deviceOS=%s, deviceToken=%s, " +
 				"language=%s, countryCode=%s, nickName=%s", userId, loginId, loginIdType,
 				appId, deviceModel, deviceId, deviceOS, deviceToken,
 				language, countryCode, nickName));
 	}
 
 	@Override
 	public boolean setDataFromRequest(HttpServletRequest request) {
 		// TODO Auto-generated method stub
 		userId = request.getParameter(ServiceConstant.PARA_USERID);
 		loginId = request.getParameter(ServiceConstant.PARA_LOGINID);
 		loginIdType = request.getParameter(ServiceConstant.PARA_LOGINIDTYPE);
 		deviceId = request.getParameter(ServiceConstant.PARA_DEVICEID);
 		deviceModel = request.getParameter(ServiceConstant.PARA_DEVICEMODEL);
 		deviceOS = request.getParameter(ServiceConstant.PARA_DEVICEOS);
 		password = request.getParameter(ServiceConstant.PARA_PASSWORD);
 		countryCode = request.getParameter(ServiceConstant.PARA_COUNTRYCODE);
 		language = request.getParameter(ServiceConstant.PARA_LANGUAGE);
 		appId = request.getParameter(ServiceConstant.PARA_APPID);
 		nickName = request.getParameter(ServiceConstant.PARA_NICKNAME);
 		avatar = request.getParameter(ServiceConstant.PARA_AVATAR);
 		deviceToken = request.getParameter(ServiceConstant.PARA_DEVICETOKEN);
 		accessToken = request.getParameter(ServiceConstant.PARA_ACCESS_TOKEN);
 		accessTokenSecret = request.getParameter(ServiceConstant.PARA_ACCESS_TOKEN_SECRET);
 		province = request.getParameter(ServiceConstant.PARA_PROVINCE);
 		city = request.getParameter(ServiceConstant.PARA_CITY);
 		location = request.getParameter(ServiceConstant.PARA_LOCATION);
 		gender = request.getParameter(ServiceConstant.PARA_GENDER);
 		birthday = request.getParameter(ServiceConstant.PARA_BIRTHDAY);
 		sinaNickName = request.getParameter(ServiceConstant.PARA_SINA_NICKNAME);
 		sinaDomain = request.getParameter(ServiceConstant.PARA_SINA_DOMAIN);
 		qqNickName = request.getParameter(ServiceConstant.PARA_QQ_NICKNAME);
 		qqDomain = request.getParameter(ServiceConstant.PARA_QQ_DOMAIN);
 		
 		if (!check(loginId, ErrorCode.ERROR_PARAMETER_USERID_EMPTY, ErrorCode.ERROR_PARAMETER_USERID_NULL))
 			return false;
 		
 		if (!check(loginIdType, ErrorCode.ERROR_PARAMETER_USERTYPE_EMPTY, ErrorCode.ERROR_PARAMETER_USERTYPE_NULL))
 			return false;
 		
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
 
 		// set nick name the same as login Id if it doesn't exist
 		if (nickName == null || nickName.length() == 0){
 			nickName = loginId;
 		}
 		
 		return true;
 	}
 
 }
