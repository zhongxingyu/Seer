 package com.drabyte.init.service;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 @ContextConfiguration("/test-context.xml")
 @RunWith(SpringJUnit4ClassRunner.class)
 public class UserServiceTest {
 
 	@Autowired
 	private UserService userService;
 
	@Test(expected= UsernameNotFoundException.class)
 	public void shouldReturnLogin() {
	 userService.loadUserByUsername("bleble");
 	}
 
 
 }
