 package com.onb.otp.service.impl;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 
 import java.util.Date;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import com.onb.otp.domain.OneTimePasswordList;
 import com.onb.otp.domain.OneTimePasswordListBatch;
 import com.onb.otp.domain.User;
 import com.onb.otp.persistence.impl.OneTimePasswordListBatchDao;
 import com.onb.otp.persistence.impl.OneTimePasswordListDao;
import com.onb.otp.service.base.PasswordServiceBase;
 
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.doNothing;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration({"/applicationContext.xml", "/otp-servlet.xml"})
 public class PasswordServiceTest {
 	private PasswordService passwordService;
 	private OneTimePasswordListDao passwordListDao;
 	private OneTimePasswordListBatchDao passwordListBatchDao;
 	
 	@Before
 	public void setup() {
 		passwordService = new PasswordService();
 
 		passwordListDao = mock(OneTimePasswordListDao.class);
 		passwordService.passwordListDao = passwordListDao;
 		
 		passwordListBatchDao = mock(OneTimePasswordListBatchDao.class);
 		passwordService.passwordListBatchDao = passwordListBatchDao;
 	}
 	
 	@Test
 	public void generatePasswordList() {
 		OneTimePasswordList passwordList = passwordService.generatePasswordList(new Date());
 		
 		assertNotNull(passwordList);
 		assertNotNull(passwordList.getPasswords());
 	}
 	
 	@Test
 	public void generateBatchPasswordList() {
 		OneTimePasswordListBatch batchPasswordList = passwordService.generateBatchPasswordList(new Date(), 10);
 		
 		assertNotNull(batchPasswordList);
 	}
 	
 	@Test
 	public void associateOtpListWithUser() {
 		
 		OneTimePasswordList list = new OneTimePasswordList();
 		User user = new User();
 		
 		doNothing().when(passwordListDao).update(list);
 		OneTimePasswordList passwordList = passwordService.associateOtpListWithUser(list, user);
 		
 		assertEquals(user, passwordList.getUser());
		assertEquals(OneTimePasswordList.Status.ASSOCIATED, passwordList.getStatus());
 	}
 }
