 package com.raccoonfink.cruisemonkey.server;
 
 
 
 import org.junit.Test;
 
 public class StatusNetServiceTest {
 
	private static final String HOST = "identi.ca";
	private static final int PORT = 80;
	private static final String PATH = "";
	private static final String USERNAME = "RangerRick";

 	@Test
 	public void testHttpAuth() throws Exception {
		new StatusNetService(HOST, PORT, PATH, USERNAME, "M0nkey").authorize();
 	}
 
 	@Test(expected=AuthorizationFailureException.class)
 	public void testHttpAuthFailure() throws Exception {
		new StatusNetService(HOST, 80, PATH, USERNAME, "aoeuaoeu").authorize();
 	}
 
 }
