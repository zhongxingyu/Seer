 package uw.cse403.minion.test;
 
 import uw.cse403.minion.HomeActivity;
 import uw.cse403.minion.LoginActivity;
 import uw.cse403.minion.PasswordRecoveryActivity;
 import uw.cse403.minion.SaveSharedPreference;
 import uw.cse403.minion.SignupActivity;
 import android.app.Activity;
 import android.test.ActivityInstrumentationTestCase2;
 import android.widget.EditText;
 import android.widget.TextView;
 
 import com.jayway.android.robotium.solo.Solo;
 
 /**
  * @author Mary Jones (mlidge)
  * 
  * LoginActivityTest tests the LoginActivity class by using Robotium and 
  * by checking application preferences.
  */
 
 public class LoginActivityTest extends
 	ActivityInstrumentationTestCase2<LoginActivity> {
 
 	private static final String VALID_USERNAME = "test";
 	private static final String VALID_PASSWORD = "abcDEF123@";
 	private static final String INVALID_CREDENTIAL = "failure";
 	private static final String EMPTY = "";
 	private static final int REMEMBER_ME = 0;
 	private Solo solo;
 	private Activity loginActivity;
 
 	public LoginActivityTest() {
 		super(LoginActivity.class);
 	}
 	
 	/**
 	 * setup() instantiates Solo and stores the LoginActivity.
 	 * loginActivity is later used to clear out the "Remember me" settings
 	 * from the application preferences to ensure clean state for each test.
 	 */
 	@Override
 	protected void setUp() throws Exception {
 
 		solo = new Solo(getInstrumentation(), getActivity());
 		loginActivity = getActivity();
 		SaveSharedPreference.setUserName(loginActivity, EMPTY);
 		
 	}
 	
 	/**
 	 * testCanLoginValid() tests the ability to login with a valid
 	 * username and password.
 	 * This is a white box test because it relies on the knowledge
 	 * that a successful login will redirect to the home activity.
 	 */
 	public void testCanLoginValid() {
 		SaveSharedPreference.setUserName(loginActivity, EMPTY);
 		EditText usernameField = (EditText) getActivity().findViewById(uw.cse403.minion.R.id.username_input); 
 		EditText passwordField = (EditText) getActivity().findViewById(uw.cse403.minion.R.id.password_input); 
 		
 		solo.typeText(usernameField, VALID_USERNAME);
 		solo.typeText(passwordField, VALID_PASSWORD);
 		solo.clickOnButton(solo.getString(uw.cse403.minion.R.string.login_button));
 		solo.assertCurrentActivity("Login successful", HomeActivity.class);
 		getActivity().finish();
 	}
 	
 	/**
 	 * testCannotLogingInvalidUsername tests that a login fails 
 	 * if an invalid (not registered) username is provided.
 	 * This is a white box test because it relies on the knowledge 
 	 * that a failed login will bring up the error text view.
 	 */
 	public void testCannotLoginInvalidUsername() {
 		SaveSharedPreference.setUserName(loginActivity, EMPTY);
 		EditText usernameField = (EditText) getActivity().findViewById(uw.cse403.minion.R.id.username_input); 
 		EditText passwordField = (EditText) getActivity().findViewById(uw.cse403.minion.R.id.password_input); 
 		
 		solo.typeText(usernameField, INVALID_CREDENTIAL);
 		solo.typeText(passwordField, VALID_PASSWORD);
 		solo.clickOnButton(solo.getString(uw.cse403.minion.R.string.login_button));
 		
 		
		TextView errorView = solo.getText(solo.getString(uw.cse403.minion.R.string.invalid_login), true);
 		assertNotNull(errorView);
 		solo.assertCurrentActivity("Login failed", LoginActivity.class);
 		getActivity().finish();
 	}
 	
 	/**
 	 * testCannotLogingInvalidPassword tests that a login fails 
 	 * if an invalid password is provided.
 	 * This is a white box test because it relies on the knowledge 
 	 * that a failed login will bring up the error text view.
 	 */
 	public void testCannotLoginInvalidPassword() {
 		SaveSharedPreference.setUserName(loginActivity, EMPTY);
 		EditText usernameField = (EditText) getActivity().findViewById(uw.cse403.minion.R.id.username_input); 
 		EditText passwordField = (EditText) getActivity().findViewById(uw.cse403.minion.R.id.password_input); 
 		
 		solo.typeText(usernameField, VALID_USERNAME);
 		solo.typeText(passwordField, INVALID_CREDENTIAL);
 		solo.clickOnButton(solo.getString(uw.cse403.minion.R.string.login_button));
 		
 		
		TextView errorView = solo.getText(solo.getString(uw.cse403.minion.R.string.invalid_login), true);
 		assertNotNull(errorView);
 		solo.assertCurrentActivity("Login failed", LoginActivity.class);
 		getActivity().finish();
 	}
 	
 	/**
 	 * testCannotLogingInvalidBoth tests that a login fails 
 	 * if both credentials are invalid.
 	 * This is a white box test because it relies on the knowledge 
 	 * that a failed login will bring up the error text view.
 	 */
 	public void testCannotLoginInvalidBoth() {
 		SaveSharedPreference.setUserName(loginActivity, EMPTY);
 		EditText usernameField = (EditText) getActivity().findViewById(uw.cse403.minion.R.id.username_input); 
 		EditText passwordField = (EditText) getActivity().findViewById(uw.cse403.minion.R.id.password_input); 
 		
 		solo.typeText(usernameField, INVALID_CREDENTIAL);
 		solo.typeText(passwordField, INVALID_CREDENTIAL);
 		solo.clickOnButton(solo.getString(uw.cse403.minion.R.string.login_button));
 		
 		
		TextView errorView = solo.getText(solo.getString(uw.cse403.minion.R.string.invalid_login), true);
 		assertNotNull(errorView);
 		solo.assertCurrentActivity("Login failed", LoginActivity.class);
 		getActivity().finish();
 	}
 	
 	/**
 	 * testKeepLoggedInTrue tests that when the "Remember me" checkbox is checked
 	 * the username is stored in preferences to be remembered for the next time
 	 * the app is started. 
 	 * This is a black box test that checks if the desired result is stored.
 	 */
 	public void testKeepLoggedInTrue() {
 		SaveSharedPreference.setUserName(loginActivity, EMPTY);
 		EditText usernameField = (EditText) getActivity().findViewById(uw.cse403.minion.R.id.username_input); 
 		EditText passwordField = (EditText) getActivity().findViewById(uw.cse403.minion.R.id.password_input); 
 		
 		solo.typeText(usernameField, VALID_USERNAME);
 		solo.typeText(passwordField, VALID_PASSWORD);
 		solo.clickOnCheckBox(REMEMBER_ME);
 		solo.clickOnButton(solo.getString(uw.cse403.minion.R.string.login_button));
 		
 		solo.assertCurrentActivity("Login successful", HomeActivity.class);
 		String username = SaveSharedPreference.getUserName(getActivity());
 		assertTrue(VALID_USERNAME.equals(username));
 		solo.finishOpenedActivities();
 		
 	}
 	
 	/**
 	 * testKeepLoggedInFalse tests that when the "Remember me" checkbox is unchecked
 	 * that the username is not stored in preferences.
 	 * This is a black box test that checks if the desired result is stored.
 	 */
 	public void testKeepLoggedInFalse() {
 		SaveSharedPreference.setUserName(loginActivity, EMPTY);
 		EditText usernameField = (EditText) getActivity().findViewById(uw.cse403.minion.R.id.username_input); 
 		EditText passwordField = (EditText) getActivity().findViewById(uw.cse403.minion.R.id.password_input); 
 		
 		solo.typeText(usernameField, VALID_USERNAME);
 		solo.typeText(passwordField, VALID_PASSWORD);
 		solo.clickOnButton(solo.getString(uw.cse403.minion.R.string.login_button));
 		
 		solo.assertCurrentActivity("Login successful", HomeActivity.class);
 		String username = SaveSharedPreference.getUserName(getActivity());
 		assertFalse(VALID_USERNAME.equals(username));
 		solo.finishOpenedActivities();
 	}
 
 	/**
 	 * testGoToSignup tests that when the signup link is clicked that the user
 	 * is redirected to the SignupActivity
 	 */
 	public void testGoToSignUp() {
 		solo.clickOnText(solo.getString(uw.cse403.minion.R.string.signup_clickable_text));
 		solo.assertCurrentActivity("Went to SignpActivity", SignupActivity.class);
 		solo.finishOpenedActivities();
 	}
 	
 	/**
 	 * testGoToPasswordRecovery tests that when the password recovery link is clicked that the user
 	 * is redirected to the PasswordRecoveryActivity
 	 */
 	public void testGoToPasswordRecovery() {
 		solo.clickOnText(solo.getString(uw.cse403.minion.R.string.forgot_password_clickable_text));
 		solo.assertCurrentActivity("Went to PasswordRecoveryActivity", PasswordRecoveryActivity.class);
 		solo.finishOpenedActivities();
 	}
 	
 	@Override
 	protected void tearDown() throws Exception {
 		SaveSharedPreference.setUserName(loginActivity, EMPTY);
 		solo.finishOpenedActivities();
 		
 	}
 }
