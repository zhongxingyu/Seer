 package org.siraya.rent.rest;
 
 import org.jmock.Expectations;
 import java.util.HashMap;
 import org.jmock.Mockery;
 import org.jmock.integration.junit4.JUnit4Mockery;
 import org.junit.Before;
 import org.junit.Test;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
 import org.siraya.rent.rest.UserRestApi;
 import org.siraya.rent.user.service.IUserService;
 import org.siraya.rent.user.dao.IDeviceDao;
 import org.siraya.rent.pojo.Device;
 import org.siraya.rent.pojo.User;
 
 import javax.ws.rs.core.Response;
 
 import junit.framework.Assert;
 import org.siraya.rent.user.service.IMobileAuthService;
 @ContextConfiguration(locations = {"classpath*:/applicationContext*.xml"})
 public class TestUserRestApi extends AbstractJUnit4SpringContextTests{
 	@Autowired
 	UserRestApi userRestApi;
 	private Mockery context;
 	private IMobileAuthService mobileAuthService;
 	private IUserService userService;
 	private boolean isMock = true;
 	private String deviceId= "23131";
 	private String userId = "12313";
 	private String authCode="123";
 	private int  cc=886;
 	private String mobilePhone= "886234242342";
 	private Device device = new Device();
 	private User  user = new User();
	private java.util.Map<String, String> request;
 	@Before
 	public void setUp(){
		request = new java.util.HashMap<String,String>();
 		if (isMock){
 			context = new JUnit4Mockery();
 			mobileAuthService = context.mock(IMobileAuthService.class);	
 			userRestApi.setMobileAuthService(mobileAuthService);
 			userService = context.mock(IUserService.class);	
 			userRestApi.setUserService(userService);
 			
 		}
 	}
 	@Test   
 	public void testNewDevice()throws Exception{	
 		if (isMock) {
 			context.checking(new Expectations() {
 				{
 					one(userService).newDevice(with(any(Device.class)));
 					will(returnValue(device));
 					one(userService).newUserByMobileNumber(cc, mobilePhone);
 					will(returnValue(user));
 				}
 			});
 		}
     	long time=java.util.Calendar.getInstance().getTimeInMillis();    	
     	request.put("country_code", Integer.toString(cc));
     	request.put("mobile_phone",mobilePhone);
 		Response response = userRestApi.newDevice(this.deviceId,this.userId,request);
 		Assert.assertEquals(200, response.getStatus());
 	}
 
 	@Test   
 	public void testSendMobileAuthMessage() throws Exception{
 		if (isMock) {
 			context.checking(new Expectations() {
 				{
 					one(mobileAuthService).sendAuthMessage(deviceId,userId);
 				}
 			});
 		};
 		request.put("device_id", this.deviceId);
 		Response response = userRestApi.sendMobileAuthMessage(deviceId,userId);
 		Assert.assertEquals(200, response.getStatus());
 	}
 	
 	@Test 
 	public void testMobileAuthCode()throws Exception{
 		if (isMock) {
 			context.checking(new Expectations() {
 				{
 					one(mobileAuthService).verifyAuthCode(deviceId, userId, authCode);
 				}
 			});
 		};
 		request.put("device_id", this.deviceId);
 		request.put("auth_code", this.authCode);
 
 		Response response = userRestApi.verifyMobileAuthCode(deviceId, userId,request);
 		Assert.assertEquals(200, response.getStatus());
 	}
 }
