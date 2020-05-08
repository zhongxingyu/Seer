 package uw.cse403.minion.test;
 
 import uw.cse403.minion.EmptyValidatorActivity;
 import uw.cse403.minion.R;
 import uw.cse403.minion.SignupActivity;
 import android.app.Activity;
 import android.test.ActivityInstrumentationTestCase2;
 import android.widget.EditText;
 import android.widget.TextView;
 
 import com.jayway.android.robotium.solo.Solo;
 
 /**
  * White box tests that test the ablility for a user to sign up
  * for an account only with correct credentials
  * @author mj
  *
  */
 public class SignupActivityTest extends ActivityInstrumentationTestCase2<SignupActivity> {
 	private static final int SPINNER_INDEX = 0;
	private static final String SPINNER_QUESTION_1 = "What is your favorite color?";
 	private static final String USED_USERNAME = "test";
 	private static final String UNUSED_USERNAME = "marysignuptest";
 	private static final String VALID_PASSWORD = "abcDEF123@";
 	private static final String INVALID_PASSWORD = "abc";
 	private static final String COLOR = "blue";
 	
 	public SignupActivityTest() {
 		super(SignupActivity.class);
 	}
 	
 	
 	/**
 	 * Test the ability to select an item in the spinner
 	 */
 	public void testCanSelectInSpinner() {
 		Solo solo = new Solo(getInstrumentation(), getActivity());
 		solo.pressSpinnerItem(SPINNER_INDEX, 1);
 		assertTrue(solo.isSpinnerTextSelected(SPINNER_INDEX, SPINNER_QUESTION_1));
 		solo.finishOpenedActivities();
 	}
 	
 	/**
 	 * Test that a user can enter text
 	 */
 	public void testCanEnterText() {
 		Solo solo = new Solo(getInstrumentation(), getActivity());
 		EditText username = (EditText) solo.getView(uw.cse403.minion.R.id.username_input); 
 		EditText password = (EditText) solo.getView(uw.cse403.minion.R.id.password_input);
 		EditText passwordMatch = (EditText) solo.getView(uw.cse403.minion.R.id.confirm_password_input);
 		
 		solo.typeText(username, USED_USERNAME);
 		solo.typeText(password, VALID_PASSWORD);
 		solo.typeText(passwordMatch, VALID_PASSWORD);
 		
 		String usernameString = username.getText().toString();
 		String passwordString = password.getText().toString();
 		String passwordMatchString = passwordMatch.getText().toString();
 		
 		assertTrue(USED_USERNAME.equals(usernameString));
 		assertTrue(VALID_PASSWORD.equals(passwordString));
 		assertTrue(VALID_PASSWORD.equals(passwordMatchString));
 		
 		solo.finishOpenedActivities();
 	}
 	
 	/**
 	 * Test that a username in use cannot be signed up with
 	 */
 	public void testUsernameInUse() {
 		Solo solo = new Solo(getInstrumentation(), getActivity());
 		EditText username = (EditText) getActivity().findViewById(uw.cse403.minion.R.id.username_input); 
 		EditText password = (EditText) getActivity().findViewById(uw.cse403.minion.R.id.password_input);
 		EditText passwordMatch = (EditText) getActivity().findViewById(uw.cse403.minion.R.id.confirm_password_input);
 		solo.typeText(username, USED_USERNAME);
 		solo.typeText(password, VALID_PASSWORD);
 		solo.typeText(passwordMatch, VALID_PASSWORD);
 		solo.pressSpinnerItem(SPINNER_INDEX, 1);
 		EditText answerEditText = (EditText) getActivity().findViewById(R.id.security_answer_input);
 		solo.typeText(answerEditText, COLOR);
 		solo.clickOnButton(solo.getString(uw.cse403.minion.R.string.next_button));
 		solo.waitForActivity(EmptyValidatorActivity.class);
 		assertTrue(solo.waitForActivity(SignupActivity.class));
 		TextView errorView = (TextView) solo.getText(solo.getString(uw.cse403.minion.R.string.invalid_username_message), true);
 		assertTrue(errorView.isShown())	;
 		solo.finishOpenedActivities();
 	}
 	
 	/**
 	 * Test that a user cannot sign up with an invalid password
 	 */
 	public void testInvalidPassword() {
 		Solo solo = new Solo(getInstrumentation(), getActivity());
 		EditText username = (EditText) getActivity().findViewById(uw.cse403.minion.R.id.username_input); 
 		EditText password = (EditText) getActivity().findViewById(uw.cse403.minion.R.id.password_input);
 		EditText passwordMatch = (EditText) getActivity().findViewById(uw.cse403.minion.R.id.confirm_password_input);
 		solo.typeText(username, UNUSED_USERNAME);
 		solo.typeText(password, INVALID_PASSWORD);
 		solo.typeText(passwordMatch, INVALID_PASSWORD);
 		solo.pressSpinnerItem(SPINNER_INDEX, 1);
 		EditText answerEditText = (EditText) getActivity().findViewById(R.id.security_answer_input);
 		solo.typeText(answerEditText, COLOR);
 		Activity signup = getActivity();
 		solo.clickOnButton(solo.getString(uw.cse403.minion.R.string.next_button));
 		solo.waitForActivity(EmptyValidatorActivity.class);
 		//assertTrue(solo.waitForActivity(SignupActivity.class));
 		solo.assertCurrentActivity("in Signup", SignupActivity.class);
 		TextView errorView = (TextView) signup.findViewById(uw.cse403.minion.R.id.invalid_password_error);
 		//assertTrue(errorView.getVisibility() == View.VISIBLE);
 		assertNotNull(errorView);
 		solo.finishOpenedActivities();
 	}
 	
 	/**
 	 * Test that a user cannot sign up with non-matching passwords
 	 */
 	public void testNonMatchingPassword() {
 		Solo solo = new Solo(getInstrumentation(), getActivity());
 		EditText username = (EditText) getActivity().findViewById(uw.cse403.minion.R.id.username_input); 
 		EditText password = (EditText) getActivity().findViewById(uw.cse403.minion.R.id.password_input);
 		EditText passwordMatch = (EditText) getActivity().findViewById(uw.cse403.minion.R.id.confirm_password_input);
 		solo.typeText(username, UNUSED_USERNAME);
 		solo.typeText(password, VALID_PASSWORD);
 		solo.typeText(passwordMatch, INVALID_PASSWORD);
 		solo.pressSpinnerItem(SPINNER_INDEX, 1);
 		EditText answerEditText = (EditText) getActivity().findViewById(R.id.security_answer_input);
 
 		solo.typeText(answerEditText, COLOR);
 		solo.clickOnButton(solo.getString(uw.cse403.minion.R.string.next_button));
 		solo.waitForActivity(EmptyValidatorActivity.class);
 		solo.waitForActivity(SignupActivity.class);
 		TextView errorView = (TextView) solo.getText(solo.getString(uw.cse403.minion.R.string.non_matching_passwords_message));
 		assertTrue(errorView.isShown());
 		solo.finishOpenedActivities();
 	}
 	
 
 }
