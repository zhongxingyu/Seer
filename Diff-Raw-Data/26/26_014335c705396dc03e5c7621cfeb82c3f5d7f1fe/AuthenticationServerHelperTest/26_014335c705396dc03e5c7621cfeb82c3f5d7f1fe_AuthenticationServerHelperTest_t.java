 package com.cn.server;
 
 import junit.framework.TestCase;
 /**
  * Important:  This test depends on the file src/users/authtest 
  * and src/users/authtest1
  */
 public class AuthenticationServerHelperTest extends TestCase {
 	
 	private String usr = "authtest";
 	private String pw = "1234";
 	
 	private String badUsr = "NOTauthtest";
 	private String badPw = "4321";
 	
 	private String usr1 = "authtest2";
 	private String pw1 = "1234";
 	
 
 	public void testAuthenticated() {
 		AuthenticationServerHelper ash = new AuthenticationServerHelper(usr, pw);
 		assertTrue(ash.isAuthenticated());
 	}
 	
 	public void testisNotAuthenticated() {
 		AuthenticationServerHelper ash = new AuthenticationServerHelper(badUsr, badPw);
 		assertTrue(!ash.isAuthenticated());
 		
 		AuthenticationServerHelper ash2 = new AuthenticationServerHelper(usr, badPw);
 		assertTrue(!ash2.isAuthenticated());
 		
 		AuthenticationServerHelper ash3 = new AuthenticationServerHelper(badUsr, pw);
 		assertTrue(!ash3.isAuthenticated());
 	}
 	
 	public void testUserFound() {
 		AuthenticationServerHelper ash = new AuthenticationServerHelper(usr, pw);
 		assertTrue(ash.isUserFound());
 		
 		AuthenticationServerHelper ash2 = new AuthenticationServerHelper(usr, badPw);
 		assertTrue(ash2.isUserFound());
 	}
 	
 	public void testUserNotFound() {
 		AuthenticationServerHelper ash = new AuthenticationServerHelper(badUsr, badPw);
 		assertTrue(!ash.isUserFound());
 		
 		AuthenticationServerHelper ash2 = new AuthenticationServerHelper(badUsr, pw);
 		assertTrue(!ash2.isUserFound());
 	}
 	
 	public void testCharacterMap() {
 		AuthenticationServerHelper ash = new AuthenticationServerHelper(usr, pw);
 		assertTrue(!ash.getCharacterMap().isEmpty());
 		assertTrue(ash.getCharacterMap().keySet().size() == 5);
 	}
 	
 	public void testCharacterMapNoAuth() {
 		AuthenticationServerHelper ash = new AuthenticationServerHelper(badUsr, badPw);
 		assertTrue(ash.getCharacterMap().isEmpty());
 	}
 	
 	public void testOverwrite() {
 		AuthenticationServerHelper ash = new AuthenticationServerHelper(usr1, pw1);
 		assertTrue(ash.getCharacterMap().keySet().size() == 5);
 		
		String[] stats = new String[6];
		stats[0] = "temp_char";
		for (int i=1; i<stats.length; i++) { 
			stats[i] = "123"; 
 		}
 		ash.getCharacterMap().put("temp_char", stats);
 		ash.overWriteChar();
 		
 		assertTrue(ash.getCharacterMap().keySet().size() == 6);
 		ash.getCharacterMap().remove("temp_char");
 		
 		ash.overWriteChar();
 		assertTrue(ash.getCharacterMap().keySet().size() == 5);
 	}
 	
 	
 }
