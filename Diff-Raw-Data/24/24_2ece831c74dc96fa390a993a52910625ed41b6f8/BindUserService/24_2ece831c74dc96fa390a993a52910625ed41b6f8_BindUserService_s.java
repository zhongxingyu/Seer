 package com.orange.groupbuy.api.service.user;
 
 import javax.servlet.http.HttpServletRequest;
 
 import com.orange.common.mail.MailSender;
 import com.orange.common.utils.StringUtil;
 import com.orange.groupbuy.api.service.CommonGroupBuyService;
 import com.orange.groupbuy.constant.DBConstants;
 import com.orange.groupbuy.constant.ErrorCode;
 import com.orange.groupbuy.constant.ServiceConstant;
 import com.orange.groupbuy.dao.User;
 import com.orange.groupbuy.manager.UserManager;
 
 public class BindUserService extends CommonGroupBuyService {
 
 	String userId;
 	String appId;	
 	int registerType;	
 	private String strRegisterType;// by email, sina weibo, tecent weibo, etc	
 	
 	String email;
 	String password;
 	boolean needVerification = true;
 
 	String snsId;
 	// optional, shared, for sina weibo or tecent weibo 
 	String nickName;
 	String avatar;
 	String accessToken;
 	String accessTokenSecret;
 	String province;
 	String city;
 	String location;
 	String gender;
 	String birthday;
 	String domain;
 
 	@Override
 	public String toString() {
 		return "BindUserService [userId=" + userId + ", appId=" + appId
 				+ ", email=" + email + ", password=" + password
 				+ ", needVerification=" +  ", snsId=" + snsId
 				+ ", nickName=" + nickName + ", avatar=" + avatar
 				+ ", accessToken=" + accessToken + ", accessTokenSecret="
 				+ accessTokenSecret + ", province=" + province + ", city="
 				+ city + ", location=" + location + ", gender=" + gender
 				+ ", birthday=" + birthday + ", domain=" + domain
 				+ ", registerType=" + registerType + ", needVerfication="
 				+ needVerification + "]";
 	}
 
 	@Override
 	public void handleData() {
 		User user = UserManager.findUserByUserId(mongoClient, userId);
 		if (user == null){
			log.info("Cannot find user"+userId);
 		}
 		else{
 			switch (registerType){
 			case ServiceConstant.REGISTER_TYPE_EMAIL:
 				if (UserManager.findUserByEmail(mongoClient, email) != null){
 					log.info("<registerEmail> user email("+email+") exist");
 					resultCode = ErrorCode.ERROR_EMAIL_EXIST;
 					return;
 				}
 				
 				UserManager.bindUserByEmail(mongoClient, appId, user, email, password, needVerification);
 
 				if (needVerification) {
 					sendVerification(user);
 				}
 				break;
 			case ServiceConstant.REGISTER_TYPE_SINA:
 				if(UserManager.findUserBySinaId(mongoClient, snsId) != null){
 					log.info("<registerSns> user ID ("+snsId+")exist");
 					resultCode = ErrorCode.ERROR_SNS_ID_EXIST;
 					return;
 				}
 				
 				UserManager.bindUserBySnsId(mongoClient, user, snsId, nickName, avatar, accessToken, accessTokenSecret, province, city, location, gender, birthday, domain, registerType);
 				break;
 			case ServiceConstant.REGISTER_TYPE_QQ:
 				if(UserManager.findUserByTencentId(mongoClient, snsId) != null){
 					log.info("<registerSns> user ID ("+snsId+")exist");
 					resultCode = ErrorCode.ERROR_SNS_ID_EXIST;
 					return;
 				}
 				
 				UserManager.bindUserBySnsId(mongoClient, user, snsId, nickName, avatar, accessToken, accessTokenSecret, province, city, location, gender, birthday, domain, registerType);
 				break;
 			default:
 				
 				break;
 		
 			} 
 		}
 	}
 
 	@Override
 	public boolean needSecurityCheck() {
 		return false;
 	}
 
 	@Override
 	public boolean setDataFromRequest(HttpServletRequest request) {
 		
 		appId = request.getParameter(ServiceConstant.PARA_APPID);
 		userId = request.getParameter(ServiceConstant.PARA_USERID);
 		strRegisterType = request.getParameter(ServiceConstant.PARA_REGISTER_TYPE);
 		registerType = Integer.parseInt(strRegisterType);
 		
 		email = request.getParameter(ServiceConstant.PARA_EMAIL);
 		password = request.getParameter(ServiceConstant.PARA_PASSWORD);
 		needVerification = Boolean.parseBoolean(request.getParameter(ServiceConstant.PARA_VERIFICATION));
 		
 		nickName = request.getParameter(ServiceConstant.PARA_NICKNAME);
 		avatar = request.getParameter(ServiceConstant.PARA_AVATAR);
 		accessToken = request.getParameter(ServiceConstant.PARA_ACCESS_TOKEN);
 		accessTokenSecret = request.getParameter(ServiceConstant.PARA_ACCESS_TOKEN_SECRET);
 		province = request.getParameter(ServiceConstant.PARA_PROVINCE);
 		city = request.getParameter(ServiceConstant.PARA_CITY);
 		location = request.getParameter(ServiceConstant.PARA_LOCATION);
 		gender = request.getParameter(ServiceConstant.PARA_GENDER);
 		birthday = request.getParameter(ServiceConstant.PARA_BIRTHDAY);
 		domain = request.getParameter(ServiceConstant.PARA_DOMAIN);	
 		
 
 		if (!check(userId, ErrorCode.ERROR_PARAMETER_USERID_EMPTY,
 				ErrorCode.ERROR_PARAMETER_USERID_NULL))
 			return false;
 		
 		if (!check(appId, ErrorCode.ERROR_PARAMETER_APPID_EMPTY,
 				ErrorCode.ERROR_PARAMETER_APPID_NULL))
 			return false;
 		
 		if(!check(strRegisterType,ErrorCode.ERROR_PARAMETER_REGISTER_TYPE_EMPTY,ErrorCode.ERROR_PARAMETER_REGISTER_TYPE_NULL))
 			return false;		
 		
 		switch(registerType){
 		case ServiceConstant.REGISTER_TYPE_EMAIL:
 			if (!StringUtil.isValidMail(email)){
 				log.info("<registerUser> user email("+email+") not valid");
 				resultCode = ErrorCode.ERROR_EMAIL_NOT_VALID;
 				return false;
 				}
 			if (!check(password, ErrorCode.ERROR_PARAMETER_PASSWORD_EMPTY, ErrorCode.ERROR_PARAMETER_PASSWORD_NULL))
 				return false;
 
 			if (!check(email, ErrorCode.ERROR_PARAMETER_EMAIL_EMPTY, ErrorCode.ERROR_PARAMETER_PASSWORD_NULL))
 				return false;
 			break;
 		case ServiceConstant.REGISTER_TYPE_SINA:
 			snsId = request.getParameter(ServiceConstant.PARA_SINAID);			
 			if(!check(snsId, ErrorCode.ERROR_PARAMETER_SNSID_EMPTY, ErrorCode.ERROR_PARAMETER_SNSID_NULL))
 				return false;
 			break;
 		case ServiceConstant.REGISTER_TYPE_QQ:
 			snsId = request.getParameter(ServiceConstant.PARA_QQID);
 			if(!check(snsId, ErrorCode.ERROR_PARAMETER_SNSID_EMPTY, ErrorCode.ERROR_PARAMETER_SNSID_NULL))
 				return false;			
 			break;
 		default:
 			resultCode = ErrorCode.ERROR_PARAMETER_UNKNOWN_REGISTER_TYPE;
 			log.info("<registerUser> unknown register type:"+strRegisterType);
 			return false;
 		}
 		
 		return true;
 		
 	}
 
 	public void sendVerification(User user){
 		String verifyCode = user.getString(DBConstants.F_VERIFYCODE);
 
 		String confirmUrl = ServiceConstant.VERIFY_URL + "?"
 						  + ServiceConstant.PARA_VERIFYCODE + "=" + verifyCode;
 
 		MailSender sm = new MailSender();
 		sm.send(email, confirmUrl);
 	}
 
 
 }
