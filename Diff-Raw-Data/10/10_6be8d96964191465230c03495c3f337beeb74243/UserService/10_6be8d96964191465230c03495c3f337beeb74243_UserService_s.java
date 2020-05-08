 package org.siraya.rent.user.service;
 
 import junit.framework.Assert;
 
 import org.siraya.rent.pojo.User;
 import org.siraya.rent.pojo.VerifyEvent;
 import org.siraya.rent.user.dao.IUserDAO;
 import org.siraya.rent.user.dao.IVerifyEventDao;
 import org.siraya.rent.utils.EncodeUtility;
 import org.siraya.rent.utils.IApplicationConfig;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 
 import java.util.regex.Pattern;
 import java.util.Map;
 import java.util.Random;
 
 import javax.ws.rs.GET;
 import org.siraya.rent.pojo.Device;
 import org.siraya.rent.user.dao.IDeviceDao;
 
 @Service("userService")
 public class UserService implements IUserService {
     @Autowired
     private IUserDAO userDao;
     @Autowired
     private IApplicationConfig applicationConfig;
     @Autowired
     private IDeviceDao deviceDao;	
     @Autowired
     private IVerifyEventDao verifyEventDao;
     private static Logger logger = LoggerFactory.getLogger(UserService.class);
 
     
     @Transactional(value = "rentTxManager", propagation = Propagation.SUPPORTS, readOnly = false, rollbackFor = java.lang.Throwable.class)
     public void removeDevice (Device device) throws Exception{
     	logger.debug("remove device ");
     	int ret =deviceDao.updateRemovedDeviceStatus(device.getId(),device.getUserId(),
     			device.getModified());
     	if (ret != 1) {
     		throw new Exception("update count is not 1");
     	}
     }
     
     /**
      * get device info, only can get information with correct device id and user id
      * @param device
      */
     @Transactional(value = "rentTxManager", propagation = Propagation.SUPPORTS, readOnly = true)
     public Device getDevice(Device device){
     	String userId = device.getUserId();
    	logger.debug("get device from database");
     	Device tmp = deviceDao.getDeviceByDeviceIdAndUserId(device.getId(), userId);
     	if (tmp == null) {
     		logger.debug("device is null");
     		return null;
     	}
    	if (tmp.getStatus() == DeviceStatus.Removed.getStatus()) {
     		logger.debug("device status is removed ");
     		return null;
     	}
     	if (device.getStatus() == DeviceStatus.Authed.getStatus()) {
     		//
     		// only authed device can get user info
     		//
     		logger.debug("get user from database");
         	User user = userDao.getUserByUserId(userId);
         	logger.debug("mobile phone is "+user.getMobilePhone());
         	device.setUser(user);    		
     	}
     	return device;
     }
     
 	/**
 	 * new user mobile number
 	 * 
 	 * @param cc country code
 	 * @param moblie phone number
 	 * @exception DuplicateKeyException duplicate mobile number
 	 */    
     @Transactional(value = "rentTxManager", propagation = Propagation.SUPPORTS, readOnly = false, rollbackFor = java.lang.Throwable.class)
 	public User newUserByMobileNumber(int cc,String mobilePhone) throws Exception {
 		//
 		// verify format
 		//
 		logger.debug("check cc code");
 		mobilePhone = mobilePhone.trim();
 		Map<String, Object> mobileCountryCode = applicationConfig.get("mobile_country_code");
 		Assert.assertTrue("cc not exist in mobile country code " + cc,
 				mobileCountryCode.containsKey(cc));
 		Assert.assertTrue("cc code not start with "+cc,
 				mobilePhone.startsWith(Integer.toString(cc)));
 
 		//
 		// setup user object
 		//
 		Map<String, Object> map = (Map<String, Object>) mobileCountryCode
 				.get(cc);
 		User user = new User();
 		String id = java.util.UUID.randomUUID().toString();
 		user.setId(id);
 
 
 		user.setMobilePhone(mobilePhone);
 		user.setCc((String) map.get("country"));
 		user.setLang((String) map.get("lang"));
 		user.setTimezone((String) map.get("timezone"));
 		user.setStatus(UserStatus.Init.getStatus());
 		try{
 
 			//
 			// insert into database.
 			//
 			userDao.newUser(user);
 			logger.info("create new user id is " + id);
 			return user;
 		}catch(org.springframework.dao.DuplicateKeyException e){
 			logger.debug("phone number "+mobilePhone+" have been exist in database.");
 			return userDao.getUserByMobilePhone(mobilePhone);
 		}
 	}
 
 
 	/**
 	 * create new device
 	 * 
 	 *    1.check max device number.
 	 *    2.create new record in database.
 	 *    3.send auth code through mobile number.
 	 */
     @Transactional(value = "rentTxManager", propagation = Propagation.SUPPORTS, readOnly = false, rollbackFor = java.lang.Throwable.class)
 	public Device newDevice(Device device) throws Exception {
     	User user = device.getUser();
     	//
 		// check device count.
 		//
 		int count=deviceDao.getDeviceCountByUserId(device.getUserId());
 		int maxDevice = ((Integer)applicationConfig.get("general").get("max_device_count")).intValue();
 		logger.debug("user id is "+device.getUserId()+" device count is "+count+" max device is "+maxDevice);
 		if (count > maxDevice) {
 			throw new Exception("current user have too many device");
 		}
 		//
 		// check user status
 		//
 		if (user.getStatus() == UserStatus.Remove.getStatus()) {
 			throw new Exception("user status is removed");
 		}
 		//
 		// generate new device id
 		//
 		if (device.getId() == null) {
 			String id = java.util.UUID.randomUUID().toString();			
 			device.setId(id);
 		}
 		Device oldDevice = deviceDao.getDeviceByDeviceIdAndUserId(
 				device.getId(), user.getId());
 		if (oldDevice != null) {
 			logger.debug("old device exist");
 		} else {
 			// old device not exist
 
 			device.setStatus(DeviceStatus.Init.getStatus());
 			Random r = new Random();
 			device.setToken(String.valueOf(r.nextInt(9999)));
 			deviceDao.newDevice(device);
 			logger.debug("insert device");
 
 		}
 		return device;
 			
 	}
 
 
     /**
      * set up email. only when email still not exist.
      * 
      */
     @Transactional(value = "rentTxManager", propagation = Propagation.SUPPORTS, readOnly = false, rollbackFor = java.lang.Throwable.class) 
 	public void setupEmail(User user) throws Exception {
     	String userId = user.getId();
     	String newEmail = user.getEmail();
     	logger.info("update email for user "+userId+ " new email "+newEmail);
     	VerifyEvent verifyEvent = null;
     	user = userDao.getUserByUserId(user.getId());
     	String oldEmail = user.getEmail();
     	
     	if (oldEmail != null && !oldEmail.equals("")) {
     		logger.debug("old email exist");
     		
     		if (oldEmail.equals(newEmail)) {
     			//
     			// same email.
     			//
     			throw new Exception("same email have been setted");
     		}
     		
     		//
     		// if old email exist and already verified, then throw exception
     		//  to prevent overwrite primary email 
     		//  change email must call by different process.
     		//
     		verifyEvent = verifyEventDao.getEventByVerifyDetailAndType(VerifyEvent.VerifyType.Email.getType(),
     				oldEmail);
 			
     		if (verifyEvent != null
 					&& verifyEvent.getStatus() == VerifyEvent.VerifyStatus.Authed
 							.getStatus()) {
 				throw new Exception("old email have been verified. can't reset email");
 			}
     	}
     	
     	user.setModified(new Long(0));
     	user.setEmail(newEmail);
 
     	logger.debug("insert verify event");
     	verifyEvent = new VerifyEvent();
     	verifyEvent.setUserId(userId);
     	verifyEvent.setStatus(VerifyEvent.VerifyStatus.Init.getStatus());
     	verifyEvent.setVerifyDetail(newEmail);
     	verifyEvent.setVerifyType(VerifyEvent.VerifyType.Email.getType());    	
     	verifyEventDao.newVerifyEvent(verifyEvent);
 
     	
     	logger.debug("update email in database");
     	userDao.updateUserEmail(user);
     	
     }
 
     /**
      * update login id and password
      * 
      */
     @Transactional(value = "rentTxManager", propagation = Propagation.SUPPORTS, readOnly = false, rollbackFor = java.lang.Throwable.class) 
 	public void updateLoginIdAndPassowrd(User user) throws Exception{
     	String userId = user.getId();
     	User user2 = userDao.getUserByUserId(user.getId());
     	//
     	// check original login id must be null or empty
     	//
     	if (user2.getLoginId() != null && user2.getLoginId() != "") {
     		throw new Exception("can't reset login id");
     	}
     	user.setId(user2.getId());
     	user.setModified(new Long(0));
     	//
     	// sha1
     	//
     	user.setPassword(EncodeUtility.sha1(user.getPassword()));
     	logger.debug("update login id and password in database");
     	int ret =userDao.updateUserLoginIdAndPassword(user);	
     	if (ret == 0 ) {
     		throw new Exception("update cnt =0, only empty login id can be update");
     	}
 	}
 
 	@Override
 	public void verifyEmail(String userId, String authCode) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void updateUserInfo(User user) {
 		// TODO Auto-generated method stub
 
 	}
 	
 	public void setUserDao(IUserDAO userDao){
 		this.userDao = userDao;
 	}
 	
 	public void setDeviceDao(IDeviceDao deviceDao){
 		this.deviceDao = deviceDao;
 	}
 }
