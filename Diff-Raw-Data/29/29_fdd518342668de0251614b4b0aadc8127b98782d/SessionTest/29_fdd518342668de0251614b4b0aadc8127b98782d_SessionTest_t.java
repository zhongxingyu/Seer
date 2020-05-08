 package com.ninedials.test.functional;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import jobs.Bootstrap;
 
 import models.Session;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import play.mvc.Http.Response;
 import play.test.Fixtures;
 import play.test.FunctionalTest;
 
 public class SessionTest extends FunctionalTest {
 
 	@Before
 	public void setup() throws Exception {
 		new Bootstrap().reloadData();
 	}
 	
 	public Response login() {
 		Response response = GET("/auth/userpass?userName=s%40s.com&password=password");
 		assertStatus(302, response);
 		Logger log = LoggerFactory.getLogger(SessionTest.class);
 		return response;
 	}
 	
 	@Test
	public void pass() {
	    assertTrue(true);
	}
 }
