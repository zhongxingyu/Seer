 package com.jcheed06.myhealthapp.test;
 
import com.jcheed06.myhealthapp.LoginActivity;

 import android.test.ActivityInstrumentationTestCase2;
 
 public class LoginActivityTest extends ActivityInstrumentationTestCase2<LoginActivity> {
 	private static final String CORRECT_USERNAME = "cristianhalman";  
 	private static final String INCORRECT_PASSWORD = "lblalababllabla";  
 	private static final String CORRECT_PASSWORD = "password";  
 
 	private int incorrectLogins;
 	
 	public LoginActivityTest() {
 		super("com.jcheed06.myhealthapp", LoginActivity.class);
 	}
 
 	protected void setUp() throws Exception {
 		super.setUp();
 		LoginActivity loginActivity = getActivity();
 		incorrectLogins = loginActivity.getIncorrectLogins();
 	}
 
 	public void testIncrementIncorrectLoginsIntIfWrongCredentials() {  
 		   sendKeys(CORRECT_USERNAME);
 		   sendKeys("ENTER");
 		   sendKeys(INCORRECT_PASSWORD);
 		   sendKeys("ENTER");
		   sendKeys("ENTER");
 		   
 		   LoginActivity loginActivity = getActivity();
 		   Integer updatetIncorrectLogins = new Integer(loginActivity.getIncorrectLogins());
		   assertTrue("Add result should be " + (incorrectLogins + 1), updatetIncorrectLogins.equals((incorrectLogins + 1)));  
 		}  
 }
