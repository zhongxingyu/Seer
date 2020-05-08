 package org.siraya.rent.user.service;
 import org.apache.ibatis.annotations.Param;
 import org.siraya.rent.donttry.service.IDontTryService;
 import org.siraya.rent.donttry.service.IDontTryService.DontTryType;
 import org.siraya.rent.mobile.service.IMobileGatewayService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 import org.siraya.rent.pojo.Device;
 import org.siraya.rent.pojo.MobileAuthResponse;
 import org.siraya.rent.pojo.MobileAuthRequest;
 import org.siraya.rent.user.dao.IDeviceDao;
 import org.siraya.rent.utils.EncodeUtility;
 import org.siraya.rent.utils.IApplicationConfig;
 import org.siraya.rent.pojo.User;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import java.text.MessageFormat;
 import java.util.ResourceBundle;
 import java.util.Map;
 import org.siraya.rent.user.dao.IMobileAuthRequestDao;
 import org.siraya.rent.user.dao.IMobileAuthResponseDao;
 import junit.framework.Assert;
 import org.siraya.rent.user.dao.IUserDAO;
 import org.siraya.rent.utils.RentException;
 import org.siraya.rent.utils.RentException.RentErrorCode;
 @Service("mobileAuthService")
 public class MobileAuthService implements IMobileAuthService {
 
 	@Autowired
     private IMobileGatewayService mobileGatewayService;
     @Autowired
     private IApplicationConfig applicationConfig;
     @Autowired
     protected IDontTryService dontTryService;
 
     @Autowired
     private IMobileAuthRequestDao mobileAuthRequestDao;
 
 	@Autowired
     private IMobileAuthResponseDao mobileAuthResponseDao;   
 	@Autowired
     private IDeviceDao deviceDao;    
 
 	@Autowired
     private IUserDAO userDao;
 	@Autowired
     private EncodeUtility encodeUtility; 
 
 	private static Logger logger = LoggerFactory.getLogger(MobileAuthService.class);
 	
     
     @Transactional(value = "rentTxManager", propagation = Propagation.SUPPORTS, readOnly = false, rollbackFor = java.lang.Throwable.class)
     public void sendAuthMessage(String deviceId,String userId){
     	logger.debug("device id is "+deviceId+" user id is "+userId);
 		Device device = deviceDao.getDeviceByDeviceIdAndUserId(deviceId,userId);
 		if (device == null) {
 			throw new RentException(RentErrorCode.ErrorNotFound, "device id not exist");
 
 		}
 		device.setUser(userDao.getUserByUserId(device.getUserId()));
 		this.sendAuthMessage(device);
     }
     
     public void sendAuthMessage(MobileAuthRequest request,MobileAuthResponse response){
 		//
 		// check status and limit
 		//
 		int status = response.getStatus();
 		if (status != DeviceStatus.Init.getStatus()
 				&& status != DeviceStatus.Authing.getStatus()) {
 			throw new RentException(RentErrorCode.ErrorStatusViolate,
 					"device status isn't init or authing");
 		}
 		int retryLimit = (Integer) applicationConfig.get("general").get(
 				"auth_retry_limit");
 		//
 		// check retry count
 		//
 		dontTryService.doTry(request.getRequestId(), DontTryType.Life,
 				retryLimit);
 		//
 		// prepare message.
 		//
 		String phone = encodeUtility.decrypt(request.getMobilePhone(),
 				User.ENCRYPT_KEY);
		User user = response.getUser();
 		if (user == null) {
 			throw new RentException(RentException.RentErrorCode.ErrorGeneral,
 					"user can't be null in mobile auth response");
 		}
 		ResourceBundle resource = ResourceBundle.getBundle("user",
 				user.getLocale());
 		String message = MessageFormat.format(
 				resource.getString("mobile_auth_request_message"),
 				encodeUtility.decrypt(request.getToken(), Device.ENCRYPT_KEY));
 		logger.debug("message is " + message);
 		//
 		// send message through gateway.
 		//
 		mobileGatewayService.sendSMS(phone, message);
 		Device device = response.getDevice();
 		if (device != null && device.getStatus() == DeviceStatus.Init.getStatus()) {
 			device.setStatus(DeviceStatus.Authing.getStatus());
 			device.setModified(0);
 			int ret = this.deviceDao.updateDeviceStatus(device.getId(),
 					device.getUserId(), DeviceStatus.Authing.getStatus(),
 					DeviceStatus.Init.getStatus(), device.getModified());
 			if (ret != 1) {
 				throw new RentException(
 						RentException.RentErrorCode.ErrorGeneral,
 						"update device status fail");
 			}
 		}
 		int ret = this.mobileAuthResponseDao.updateResponseStatus(response);
 		if (ret != 1) {
 			throw new RentException(RentException.RentErrorCode.ErrorGeneral,
 					"update mobile response status fail");
 		}
     }
     
     /**
 	 * 1. check device status to prevent duplicate auth.
 	 * 2. set auth code by random from 0 - 999999
 	 * 3. send message.
 	 */
     void sendAuthMessage(Device device) {
     	//
 		// check status and limit
 		//
 		int status = device.getStatus();
 		if (status == DeviceStatus.Removed.getStatus()) {
 			logger.info("this device have been removed, but ask auth again");
 			
 		} else if (status != DeviceStatus.Init.getStatus()
 				&& status != DeviceStatus.Authing.getStatus()) {
 			throw new RentException(RentErrorCode.ErrorStatusViolate, "device status isn't init or authing");
 		}
 		//
 		// check retry count
 		//
 		int retryLimit = (Integer)applicationConfig.get("general").get("auth_retry_limit");
 		int currentRetry = device.getAuthRetry();
 		logger.info("current retry count is "+currentRetry);
 		if (retryLimit < currentRetry ) {
 			//
 			// retry too many times, change to suspend status.
 			//
 			logger.debug("update to suspend status");
 			int ret = deviceDao.updateStatusAndRetryCount(device.getId(),
 					device.getUserId(), DeviceStatus.Suspend.getStatus(),
 					DeviceStatus.Authing.getStatus(), device.getModified());
 			if (ret != 1) {
 				throw new RentException(RentErrorCode.ErrorStatusViolate,
 						"update device status count is not 1");
 			}
 			throw new RentException(RentErrorCode.ErrorExceedLimit,
 					"auth retry too many time try");
 		}
 		
 		User user = device.getUser();
 		Assert.assertNotNull("user can't be null",user);
 		
 		//
 		// prepare message.
 		//
 		String phone=encodeUtility.decrypt(user.getMobilePhone(), "general");
 	    ResourceBundle resource = ResourceBundle.getBundle("user",user.getLocale());
 		String message = MessageFormat.format(resource.getString("mobile_auth_message"),
 				device.getId(), encodeUtility.decrypt(device.getToken(), Device.ENCRYPT_KEY));
 		logger.debug("message is "+message);
 		//
 		// update status
 		//
 		device.setModified(0);
 		int ret =deviceDao.updateStatusAndRetryCount(device.getId(), device.getUserId(),
 				DeviceStatus.Authing.getStatus(),
 				DeviceStatus.Init.getStatus(), device.getModified());
 		if (ret != 1) {
 			throw new RentException(RentErrorCode.ErrorStatusViolate,
 					"update device status count is not 1");
 		}
 
 		//
 		// send message through gateway.
 		//
 		mobileGatewayService.sendSMS(phone, message);
 		device.setAuthRetry(device.getAuthRetry()+1);
 		device.setStatus(DeviceStatus.Authing.getStatus());
 	}
 
 	/**
 	 * set user dao for testing
 	 * @param deviceDao
 	 */
 	public void setDeviceDao(IDeviceDao deviceDao) {
 		this.deviceDao = deviceDao;
 	}
 
 	public void setUserDao(IUserDAO userDao){
 		this.userDao = userDao;
 	}
 
 	/**
 	 * step1: check request stauts
 	 * step2: check  expired
 	 * step3: check retry count
 	 * step4: check auth code
 	 * step5: update database
 	 */
 	public MobileAuthResponse verifyMobileAuthRequestCode(MobileAuthRequest request){		
 		//
 		// check status and user match
 		//
 		String authCode = request.getAuthCode();
 		request = this.mobileAuthRequestDao.get(request.getRequestId());
 		if (request == null) {
 			throw new RentException(RentException.RentErrorCode.ErrorNotFound,
 					"request id not found in database");
 		}
 	
 		if (request.getStatus() != DeviceStatus.Authing.getStatus()
 				&& request.getStatus() != DeviceStatus.Authed.getStatus()) {
 			throw new RentException(RentErrorCode.ErrorStatusViolate,
 					"request status isn't authing or authed but " + request.getStatus());
 		}
 		//
 		// check expire
 		//
 		Map<String,Object> setting = applicationConfig.get("general");
     	long time=java.util.Calendar.getInstance().getTimeInMillis()/1000;
 		int timeout = (Integer)setting.get("auth_verify_timeout");
     	if (time > request.getRequestTime() + timeout ) {	
 			throw new RentException(RentErrorCode.ErrorAuthExpired,
 					"verify mobile auth code has been timeout");
     	}		
 		int retryLimit = (Integer)setting.get("auth_retry_limit");
 		//
 		// check retry count
 		//
 		dontTryService.doTry(request.getRequestId(), DontTryType.Life,
 				retryLimit);
 		//
 		// check auth code
 		//
 		String decryptToken = encodeUtility.decrypt(request.getToken(), Device.ENCRYPT_KEY);
 		if (!authCode.equals(decryptToken)) {
 			throw new RentException(RentException.RentErrorCode.ErrorGeneral,
 					"token not match "+decryptToken);
 		}
 		//
 		// update user database
 		//
 		User user = this.userDao.getUserByUserId(request.getUserId());
 		if (user == null) {
 			throw new RentException(RentException.RentErrorCode.ErrorNotFound,
 					"user " + request.getUserId() + " not found");
 		}
 		if (user.getStatus() == UserStatus.Init.getStatus()){
 			logger.debug("update user status to authed");
 			user.setModified((long)0);
 			userDao.updateUserStatus(user.getId(), UserStatus.Authed.getStatus()
 					,  UserStatus.Init.getStatus(), user.getModified());
 		}
 		//
 		// prepare response
 		//
 		MobileAuthResponse response = new MobileAuthResponse();
 		response.setResponseTime(0);
 		response.setUser(user);
 		response.setStatus(DeviceStatus.Authed.getStatus());
 		response.setRequestId(request.getRequestId());
 		//
 		// sign response
 		//
 		Device requestFrom = this.deviceDao.getDeviceByDeviceIdAndUserId(
 				IUserService.SSO_DEVICE_ID, request.getRequestFrom());
 		if (requestFrom == null) {
 			throw new RentException(RentException.RentErrorCode.ErrorNotFound,
 					"request from user not found");
 		}
 		String responseSign = EncodeUtility.sha1(response.toString(requestFrom.getToken()));
 		response.setSign(responseSign);
 		//
 		// update mobile auth response into database.
 		//
 		this.mobileAuthResponseDao.updateResponse(response);
 		return response;
 	}
 	/**
 	 * 
 	 */
 	public void verifyAuthCode(String deviceId, String userId,String authCode) throws Exception{
 		//
 		// check status.
 		//
 		Device device = deviceDao.getDeviceByDeviceIdAndUserId(deviceId,userId);
 		if (device.getUser() == null) {
 			device.setUser(userDao.getUserByUserId(device.getUserId()));			
 		}
 		_verifyAuthCode(device,authCode);
 	}
 
 	/**
 	 * step1: check device stauts
 	 * step2: check auth code expired
 	 * step3: check retry count
 	 * step4: check auth code
 	 * step5: update database
 	 * 
 	 * @param device
 	 * @param authCode
 	 */
 	private void _verifyAuthCode(Device device,String  authCode){
 		device.setModified(0); // reset modified to get current time.
 		if (device.getStatus() != DeviceStatus.Authing.getStatus()) {
 			throw new RentException(RentErrorCode.ErrorStatusViolate,
 					"device auth status not match current status is "+device.getStatus());
 		}
 		//
 		// if device modified + timeout  > current time then throw timeout exception.
 		// check verify timeout
 		//
 		Map<String,Object> setting = applicationConfig.get("general");
     	long time=java.util.Calendar.getInstance().getTimeInMillis()/1000;
 		int timeout = (Integer)setting.get("auth_verify_timeout");
     	if (time > device.getModified() + timeout ) {	
 			throw new RentException(RentErrorCode.ErrorAuthExpired,
 					"verify mobile auth code has been timeout");
     	}
     	
 		//
 		// check retry count
 		//
 		int retryLimit = (Integer)setting.get("auth_retry_limit");
 		if(logger.isDebugEnabled()) {
 			logger.debug("retry limit is "+retryLimit+" current auth retry is "+device.getAuthRetry());
 		}
 		if (device.getAuthRetry() > retryLimit) {
 			throw new RentException(RentErrorCode.ErrorExceedLimit,
 					"mobile verify auth retry too many time try:"
 							+ device.getAuthRetry() + " > " + retryLimit);
 		}
 
 		//
 		// check auth code
 		//
 		String token = device.getToken();
 		if (!authCode.equals(this.encodeUtility.decrypt(token, Device.ENCRYPT_KEY))) {
 			//
 			// if fail update retry status
 			//
 			logger.debug("token not match, token is "+token);
 			int ret =deviceDao.updateStatusAndRetryCount(device.getId(), device.getUserId(),
 					DeviceStatus.Authing.getStatus(),
 					DeviceStatus.Init.getStatus(), device.getModified());
 			if (ret != 1) {
 				throw new RentException(RentErrorCode.ErrorStatusViolate,
 						"update device status count is not 1");
 			}
 			throw new RentException(RentErrorCode.ErrorAuthFail,
 					"device id "+device.getId()+" enter auth code not match");
 
 		}
 		
 		//
 		// update device and user in database.
 		//
 		logger.debug("update device status to authed");
 		deviceDao.updateStatusAndRetryCount(device.getId(),device.getUserId(),
 				DeviceStatus.Authed.getStatus(),
 				DeviceStatus.Authing.getStatus(),
 				device.getModified());
 		User user = device.getUser();
 		if (user.getStatus() == UserStatus.Init.getStatus()){
 			logger.debug("update user status to authed");
 			user.setModified((long)0);
 			userDao.updateUserStatus(user.getId(), UserStatus.Authed.getStatus()
 					,  UserStatus.Init.getStatus(), user.getModified());
 		}
 		
 	}
 	
 	/**
 	 * 
 	 */
 	public void verifyAuthCodeByMobilePhone(String mobildPhone,String authCode){
 		User user = userDao.getUserByMobilePhone(encodeUtility.encrypt(mobildPhone, User.ENCRYPT_KEY));
 		if (user == null) {
 			throw new RentException(RentErrorCode.ErrorNotFound,"use not found");
 		}
 		logger.debug("get device for user "+user.getId());
 		Device device = deviceDao.getDeviceByUserIdAndStatusAuthing(user.getId());
 		if (device == null) {
 			throw new RentException(RentErrorCode.ErrorNotFound,"device not found");			
 		}
 		if (device.getStatus() == DeviceStatus.Authed.getStatus()) {
 			logger.info("device has been authed, dont need to re-auth.");
 			return;
 		}
 		device.setUser(user);
 		_verifyAuthCode(device,authCode);
 	}
     public void setApplicationConfig(IApplicationConfig applicationConfig) {
 		this.applicationConfig = applicationConfig;
 	}
     public void setEncodeUtility(EncodeUtility encodeUtility) {
 		this.encodeUtility = encodeUtility;
 	}
     public void setMobileGatewayService(IMobileGatewayService mobileGatewayService) {
 		this.mobileGatewayService = mobileGatewayService;
 	}
     public void setDontTryService(IDontTryService dontTryService) {
 		this.dontTryService = dontTryService;
 	}
     public IMobileAuthRequestDao getMobileAuthRequestDao() {
 		return mobileAuthRequestDao;
 	}
 
 	public void setMobileAuthRequestDao(IMobileAuthRequestDao mobileAuthRequestDao) {
 		this.mobileAuthRequestDao = mobileAuthRequestDao;
 	}
 
 	public IMobileAuthResponseDao getMobileAuthResponseDao() {
 		return mobileAuthResponseDao;
 	}
 
 	public void setMobileAuthResponseDao(
 			IMobileAuthResponseDao mobileAuthResponseDao) {
 		this.mobileAuthResponseDao = mobileAuthResponseDao;
 	}
 }
