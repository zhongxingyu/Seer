 package com.devworkz.shopcart;
 
 import static org.junit.Assert.assertNotNull;
 
 import org.junit.Test;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
 
 import com.devworkz.shopcart.service.impl.CustomUserDetailsService;
 
 @ContextConfiguration(locations="classpath:spring/app-config.xml")
 public class SpringConfigTest extends AbstractJUnit4SpringContextTests{
 	@Autowired
 	private CustomUserDetailsService customUserDetailsService;
 	
 	@Test
 	public void testCustomUserDetailsService(){
 		assertNotNull(customUserDetailsService);
 		assertNotNull(customUserDetailsService.getUserRepository());
 	}
 }
