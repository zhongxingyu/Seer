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
    public void testCanGetSessionData() {
		Session session = Session.find("byName", "session1").first();
		Response response = login();
        response = GET("/api/sessions/" + session.id);
        assertIsOk(response);
        assertContentType("application/json", response);
        assertCharset(play.Play.defaultWebEncoding, response);
        assertContentMatch("sounds/hope/solo_guitar.ogg", response);
    }
	
	@Test
    public void testCanUpdateSessionData() {
		Session session = Session.find("byName", "session1").first();
		Response response = login();
		Map<String, String> args = new HashMap<String, String>();
		args.put("data", "newData");
		
		response = POST("/api/sessions/" + session.id, args);
		assertIsOk(response);
		
        response = GET("/api/sessions/" + session.id.toString());
        assertIsOk(response);
        assertContentType("application/json", response);
        assertCharset(play.Play.defaultWebEncoding, response);
        assertContentMatch("newData", response);
    }
 }
