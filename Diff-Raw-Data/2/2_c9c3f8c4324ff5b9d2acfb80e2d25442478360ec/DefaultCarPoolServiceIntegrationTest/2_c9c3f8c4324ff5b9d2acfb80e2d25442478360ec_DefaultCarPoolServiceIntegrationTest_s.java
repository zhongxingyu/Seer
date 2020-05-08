 package com.javaid.bolaky.carpool.service.impl;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.CoreMatchers.notNullValue;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.assertTrue;
 
 import java.util.Set;
 
 import javax.annotation.Resource;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.test.context.transaction.TransactionConfiguration;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.javaid.bolaky.carpool.service.api.CarPoolService;
 import com.javaid.bolaky.carpool.service.vo.UserVO;
 import com.javaid.bolaky.carpool.service.vo.enumerated.CarPoolError;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {
 		"classpath:/default-carpool-service-context.xml",
 		"classpath:/default-carpool-service-test-context.xml" })
 @TransactionConfiguration(transactionManager = "transactionManager")
 @Transactional(propagation = Propagation.REQUIRED)
 public class DefaultCarPoolServiceIntegrationTest {
 
 	@Resource(name = "carpool_service_DefaultCarPoolService")
 	private CarPoolService carPoolService;
 
 	@Test
 	public void testTestRegisterUserReturningNull() {
 
 		UserVO userVO = new UserVO();
 
 		Set<CarPoolError> carPoolErrors = carPoolService.validate(userVO);
 
 		assertThat(carPoolErrors, is(notNullValue()));
		assertTrue(carPoolErrors.size()>0);
 	}
 }
