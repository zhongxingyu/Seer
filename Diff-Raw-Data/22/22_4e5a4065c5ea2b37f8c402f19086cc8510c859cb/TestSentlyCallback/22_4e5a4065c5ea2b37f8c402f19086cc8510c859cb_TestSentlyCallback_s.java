 package org.siraya.rent.rest;
 
 import javax.ws.rs.QueryParam;
 
 import org.jmock.Expectations;
 import org.jmock.Mockery;
 import org.jmock.integration.junit4.JUnit4Mockery;
 import org.junit.Before;
 import org.junit.Test;
 import org.siraya.rent.pojo.Device;
 import org.siraya.rent.user.service.IMobileAuthService;
 
 public class TestSentlyCallback {
 	SentlyCallback test;
 
 	private boolean isMock = true;
 	private IMobileAuthService mobileAuthService;
 	private Mockery context;
 
 	@Before
 	public void setUp(){
 		test = new SentlyCallback();
 
 		if (isMock){
 			context = new JUnit4Mockery();
 			mobileAuthService = context.mock(IMobileAuthService.class);
 			test.setMobileAuthService(mobileAuthService);
 		}
 	}
 	
 	@Test   
 	public void testReceiveMessage()throws Exception{
 		String from = "+3123123131";
		String text = "xx @2332 xx";
 		if (isMock) {
 			context.checking(new Expectations() {
 				{
 					one(mobileAuthService).verifyAuthCodeByMobilePhone("3123123131", "2332");
 				}
 			});
 		}
 		test.receiveMessage(from, text);
 	}
 }
