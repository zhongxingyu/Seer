 package com.asseco.aha.test.simple.basic;
 
 import static junit.framework.Assert.assertNotNull;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import com.asseco.aha.test.simple.basic.HelloService;
 
@ContextConfiguration
 @RunWith(SpringJUnit4ClassRunner.class)
 public class HelloServiceSpringConfigurationDirectTest {
 	
 	@Autowired
 	private HelloService service;
 
 	@Test
 	public void testSimpleProperties() throws Exception {
 		assertNotNull(service);
 	}
 	
 }
