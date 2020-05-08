 package org.siraya.rent.mobile;
 
 import org.jmock.Expectations;
 import org.jmock.Mockery;
 import org.jmock.integration.junit4.JUnit4Mockery;
 import org.junit.Before;
 import org.junit.Test;
 import org.siraya.rent.mobile.service.IMobileGatewayService;
 import org.siraya.rent.mobile.provider.SentlyMobileGatewayProvider;
 import org.siraya.rent.pojo.Device;
 import org.siraya.rent.utils.IApplicationConfig;
 
 import org.springframework.web.client.RestOperations;
 import java.util.HashMap;
 
 public class TestSentlyMobileGatewayProvider {
     private IMobileGatewayService mobileGatewayService;
 	private Mockery context;
 	private boolean isMock = true;
     private IApplicationConfig applicationConfig;
     private RestOperations restTemplate;
     // for test 
     class SentlyMobileGatewayProviderForTest extends SentlyMobileGatewayProvider{
     	public SentlyMobileGatewayProviderForTest(IApplicationConfig iApplicationConfig,
     			RestOperations restTemplate){
     		super(restTemplate);
     		this.applicationConfig = iApplicationConfig;
     	}
     }
     
     @Before
 	public void setUp(){
 		if (isMock){
 			context = new JUnit4Mockery();
 			applicationConfig = context.mock(IApplicationConfig.class);	
 			restTemplate = context.mock(RestOperations.class);	
 			mobileGatewayService = new SentlyMobileGatewayProviderForTest(applicationConfig,restTemplate);
 		}
 	}
     @Test   
     public void testSendSMS()throws Exception{
 		if (isMock) {
 			context.checking(new Expectations() {
 				{
 					one(applicationConfig).get("sently");
 					HashMap<String,Object> setting = new HashMap<String,Object>();
 					setting.put("debug", false);
 					will(returnValue(setting));
 					one(restTemplate).getForObject(with(any(String.class)),
 							with(any(Class.class)), with(any(HashMap.class)));
 					will(returnValue("OK"));
 				}
 			});
 		}
    	mobileGatewayService.sendSMS("886936072281", "Hello world");
     }
     
     
     @Test(expected=org.siraya.rent.utils.RentException.class)
     public void testSendSMSWithError() throws Exception{
 		if (isMock) {
 			context.checking(new Expectations() {
 				{
 					one(applicationConfig).get("sently");
 					HashMap<String,Object> setting = new HashMap<String,Object>();
 					setting.put("debug", false);
 					will(returnValue(setting));
 					one(restTemplate).getForObject(with(any(String.class)),
 							with(any(Class.class)), with(any(HashMap.class)));
 					will(returnValue("Error:1"));
 				}
 			});
 		}
     	mobileGatewayService.sendSMS("886936072281", "Hello world");
     }
 }
