 package org.siraya.rent.user.dao;
 
 import junit.framework.Assert;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.siraya.rent.pojo.MobileAuthRequest;
 import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
 import java.util.List;
 @ContextConfiguration(locations = {"classpath*:/applicationContext.xml","classpath*:/applicationContext-mybatis.xml"})
 public class TestMobileAuthRequestDao extends AbstractJUnit4SpringContextTests{
     @Autowired
     private IMobileAuthRequestDao mobileAuthRequestDao;
     @Autowired
     private IMobileAuthResponseDao mobileAuthResponseDao;
     private MobileAuthRequest request;
     @Before
 	public void setUp(){
     	request = new MobileAuthRequest();
     	long time=java.util.Calendar.getInstance().getTimeInMillis();
     	request.setRequestId("r"+time/1000);
     	request.setForceReauth(false);
     	request.setRequestTime(time/1000);
    	request.setToken("Tset");
     	request.setAuthUserId("test id");
     	request.setRequestFrom("test xxx");
     	request.setDone("http://www.yahoo.com");
     	request.setStatus(1);
     }
 	
     @Test   
     public void testCRUD(){
 		mobileAuthRequestDao.newRequest(request);
 
 		mobileAuthResponseDao.updateResponse(request);
 		MobileAuthRequest request2 = mobileAuthRequestDao.get(request
 				.getRequestId());
 		Assert.assertEquals(request.getAuthUserId(), request2.getAuthUserId());
		Assert.assertEquals(request.getToken(), request2.getToken());	
 		Assert.assertEquals(request.getStatus(), request2.getStatus());
 		
 		List<MobileAuthRequest> list = mobileAuthRequestDao
 				.getRequestsByAuthUser(request.getAuthUserId(), 10, 0);
 		Assert.assertNotSame(0, list.size());
 
 		list = mobileAuthRequestDao.getRequestsByFrom(request.getRequestFrom(),
 				10, 0);
 		Assert.assertNotSame(0, list.size());
 		
     
     }
 }
