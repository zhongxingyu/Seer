 package com.raccoonfink.cruisemonkey.server;
 
 
 
 import org.junit.Test;
 
 public class StatusNetServiceTest {
 
 	@Test
 	public void testHttpAuth() throws Exception {
		new StatusNetService("192.168.211.118", 80, "/statusnet", "RangerRick", "M0nkey").authorize();
 	}
 
 	@Test(expected=AuthorizationFailureException.class)
 	public void testHttpAuthFailure() throws Exception {
		new StatusNetService("192.168.211.118", 80, "/statusnet", "RangerRick", "aoeuaoeu").authorize();
 	}
 
 }
