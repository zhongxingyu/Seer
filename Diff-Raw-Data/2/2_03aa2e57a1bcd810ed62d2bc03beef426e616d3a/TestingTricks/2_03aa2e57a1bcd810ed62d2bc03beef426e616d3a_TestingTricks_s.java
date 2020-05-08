 package epfl.sweng.test.tools;
 
 import com.jayway.android.robotium.solo.Solo;
 
 import android.app.Activity;
 import android.widget.EditText;
 
 /**
  * Various GUI manipulation tools...
  */
 public class TestingTricks {
 	private final static String TEST_USERNAME = "valid";
 	private final static String TEST_PASSWORD = "tutu";
 	private final static String TEST_BAD_USERNAME = "tatutata";
 	
 	public static void authenticateMe(Solo solo, String username) {
 		doAuthentication(solo, username);
 	}
 	public static void authenticateMe(Solo solo) {
 		doAuthentication(solo, TEST_USERNAME);
 	}
 	public static void authenticateMeBadly(Solo solo) {
 		doAuthentication(solo, TEST_BAD_USERNAME);
 	}
 	
 	private static void doAuthentication(Solo solo, String username) {
 		Activity activity = solo.getCurrentActivity();
 		boolean needsAuth = false;
 		
 		for (EditText et: solo.getCurrentEditTexts()) {
 			if (et.getTag().toString() 
 				== activity.getResources().getText(epfl.sweng.R.string.auth_login_hint)) {
				solo.enterText(et, TEST_USERNAME);
 				needsAuth = true;
 			} else if (et.getTag().toString()
 				== activity.getResources().getText(epfl.sweng.R.string.auth_pass_hint)) {
 				solo.enterText(et, TEST_PASSWORD);
 			}
 		}				
 		
 		if (needsAuth) {
 			solo.clickOnButton("Log in using Tequila");
 		}
 	}
 }
