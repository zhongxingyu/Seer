 package org.siraya.rent.user.service;
 
 import junit.framework.Assert;
 
 import org.jmock.Expectations;
 import org.jmock.Mockery;
 import org.jmock.integration.junit4.JUnit4Mockery;
 import org.junit.Test;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
 import org.siraya.rent.pojo.User;
 import org.siraya.rent.user.service.IMobileAuthService;
 import org.siraya.rent.pojo.Device;
 import org.junit.Before;
 import org.siraya.rent.user.dao.IDeviceDao;
 import org.siraya.rent.user.dao.IUserDAO;
 @ContextConfiguration(locations = {"classpath*:/applicationContext*.xml"})
 
 public class TestMobileAuthService  extends AbstractJUnit4SpringContextTests{
 	@Autowired
 	private IMobileAuthService mobileAuthService;
 	private Mockery context;
 	String deviceId= "123";
 	Device device =null;
 	private String authCode = "1234";	
 	private IDeviceDao deviceDao;
 	private IUserDAO userDao;
 	private boolean isMock = true;
 	@Before
 	public void setUp(){
 		if (isMock){
 			context = new JUnit4Mockery();
 		}
 		User user = new User();
 		user.setId("userid123");
 		user.setCc("TW");
 		user.setLang("zh");
 		user.setStatus(0);
 		user.setMobilePhone("886936072283");
 		device = new Device();
 		device.setId("test id");
 		device.setUser(user);
 		device.setStatus(0);
 		device.setToken(authCode);
 		if (isMock){
 			deviceDao = context.mock(IDeviceDao.class);	
 			userDao = context.mock(IUserDAO.class);	
 			mobileAuthService.setDeviceDao(deviceDao);
 			mobileAuthService.setUserDao(userDao);
 		}
 	}
 	
 	@Test   
 	public void testSendAuthMessage()throws Exception{
 		//expectation
 		if (isMock) {
 			context.checking(new Expectations() {
 				{
 					one(deviceDao).getDeviceByDeviceId(deviceId);								
 					will(returnValue(device));
 
 					one(userDao).getUserByUserId(device.getUserId());
 					will(returnValue(device.getUser()));
 					
 					one(deviceDao)
 							.updateStatusAndRetryCount(
 									with(any(String.class)),
 									with(any(int.class)), 
 									with(any(int.class)),
 									with(any(long.class)));
 					will(returnValue(1));
 					one(deviceDao).getDeviceByDeviceId(device.getId());
 					will(returnValue(device));
 					
 
 				}
 			});	
 		}
 		
 		
 		mobileAuthService.sendAuthMessage(deviceId);
 	}
 	
 	@Test   
 	public void testVerifyAuthCode() throws Exception{
 		//expectation
 		if (isMock) {
 			device.setStatus(1);
 			context.checking(new Expectations() {
 				{
 					one(deviceDao).getDeviceByDeviceId(deviceId);								
 					will(returnValue(device));
 
 					one(userDao).getUserByUserId(device.getUserId());
 					will(returnValue(device.getUser()));
 					
 					one(deviceDao)
 							.updateStatusAndRetryCount(
 									with(any(String.class)),
 									with(any(int.class)), 
 									with(any(int.class)),
 									with(any(long.class)));
 					will(returnValue(1));
 					one(deviceDao).getDeviceByDeviceId(device.getId());
 					will(returnValue(device));
 					
 
 					
 					one(userDao).updateUserStatus(
 							with(any(String.class)),
 							with(any(int.class)),
 							with(any(int.class)),
 							with(any(long.class)));
 					will(returnValue(1));
 				}
 			});	
 		}
 		
 
 		
 		mobileAuthService.verifyAuthCode(device.getId(), authCode);		
 	}
 	@Test(expected=junit.framework.AssertionFailedError.class)   
 	public void testRetryMax()throws Exception{
 		try {
 			if (isMock) {
 				context.checking(new Expectations() {
 					{
 						one(deviceDao).getDeviceByDeviceId(deviceId);								
 						device.setAuthRetry(5);
 						will(returnValue(device));
 
 						one(userDao).getUserByUserId(device.getUserId());
 						will(returnValue(device.getUser()));
 
 					}
 				});	
 			}
 			mobileAuthService.sendAuthMessage(deviceId);
 		}catch(Exception e){
 			System.out.println(e.getMessage());
 			throw e;
 		}
 	}
 	
 	@Test(expected=junit.framework.AssertionFailedError.class)   
 	public void testRetryMaxInVerify()throws Exception{
 		//expectation
 		if (isMock) {
 			context.checking(new Expectations() {
 				{
 					one(deviceDao).getDeviceByDeviceId(device.getId());
 					will(returnValue(device));
					one(userDao).getUserByUserId(device.getUserId());
					will(returnValue(device.getUser()));
 					
 				}
 			});	
 		}
 		device.setStatus(DeviceStatus.Authing.getStatus());
 		device.setAuthRetry(5);
 		mobileAuthService.verifyAuthCode(device.getId(), authCode);
 	}
 	
 }
