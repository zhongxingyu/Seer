 package realtalk.activities;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import android.test.ActivityInstrumentationTestCase2;
 import android.widget.EditText;
 
 import com.jayway.android.robotium.solo.Solo;
 import com.realtalk.R;
 
 /**
  * Black Box Tests that test functionality of Login Page and Create Account page
  * @author Brandon
  *
  */
 public class LoginActivityTest extends ActivityInstrumentationTestCase2<LoginActivity> {
 	private Solo solo;
 	  
 	public LoginActivityTest() {
 		super(LoginActivity.class);
 	}
 	
 	@Before
 	public void setUp() throws Exception {
 		solo = new Solo(getInstrumentation(), getActivity());
 	}
 		
 	@Test
 	public void testButtonsAndTextDisplay() {
 		assertTrue(solo.searchButton("Login"));
 		assertTrue(solo.searchButton("Create Account"));
 		assertFalse(solo.searchButton("WRONG_BUTTON"));
 	}
 	 
 	/**
 	 * Tests when user enters empty username when logging in
 	 */
 	@Test
 	public void testEmptyUsername() {
 		EditText edittextUsername = (EditText) solo.getView(R.id.editQuery);
 		solo.enterText(edittextUsername, " ");
 		EditText edittextPassword = (EditText) solo.getView(R.id.editPword);
 		solo.enterText(edittextPassword, "test");
 		solo.clickOnButton("Login");
 		assertTrue("Could not find the dialog", solo.searchText("Invalid input"));
 		solo.clickOnButton("Close");
 	}  
 	
 	/**
 	 * Tests when user enters empty password when logging in
 	 */
 	@Test
 	public void testEmptyPassword() {
 		EditText edittextUsername = (EditText) solo.getView(R.id.editQuery);
 		solo.enterText(edittextUsername, "test");
 		EditText edittextPassword = (EditText) solo.getView(R.id.editPword);
 		solo.enterText(edittextPassword, "");
 		solo.clickOnButton("Login");
 		assertTrue("Could not find the dialog", solo.searchText("Invalid input"));
 		solo.clickOnButton("Close");
 	}
 	 
 	
 	/**
 	 * Sets up test account
 	 */
 	public void setUpAccount() {
 		solo.clickOnButton("Create Account");
 		solo.assertCurrentActivity("Check on current page activity", CreateAccountActivity.class);
 		EditText edittextUsername = (EditText) solo.getView(R.id.user);
 		solo.enterText(edittextUsername, "ab3r5i9");
 		EditText edittextPassword = (EditText) solo.getView(R.id.pword);
 		solo.enterText(edittextPassword, "test");
 		EditText edittextConfPassword = (EditText) solo.getView(R.id.conf_pword);
 		solo.enterText(edittextConfPassword, "test");
 		solo.clickOnButton("Create Account");
 		solo.sleep(5000);
 	} 
 	
 	/**
 	 * Deletes account
 	 */
 	public void deleteAccount() {
 		solo.clickOnMenuItem("Account Settings");
 		solo.clickOnButton("Click Here to Delete Account");
 		solo.clickOnButton("Yes");
 		solo.sleep(30000);
 		solo.clickOnButton("Close");
 	}
 	
 	/**
 	 * Tests Creating an account, logging in, changing the password, and deleting the account
 	 */
 	@Test
 	public void testSuccessfulLogin() {		
 		setUpAccount();
 		solo.waitForActivity(LoginActivity.class, 30000);
 		EditText edittextUsername = (EditText) solo.getView(R.id.editQuery);
 		solo.enterText(edittextUsername, "ab3r5i9");
 		EditText edittextPassword = (EditText) solo.getView(R.id.editPword);
 		solo.enterText(edittextPassword, "test");
 		solo.clickOnButton("Login");
 		solo.waitForActivity(SelectRoomActivity.class, 30000);
 		solo.assertCurrentActivity("Check on current page activity", SelectRoomActivity.class);
 		solo.clickOnMenuItem("Account Settings");
 		EditText edittextOld = (EditText) solo.getView(R.id.oldpword);
 		solo.enterText(edittextOld, "test");
 		EditText edittextNew= (EditText) solo.getView(R.id.newpword);
 		solo.enterText(edittextNew, "tets");
 		EditText edittextConf = (EditText) solo.getView(R.id.confpword);
 		solo.enterText(edittextConf, "tets");
 		solo.clickOnButton("Submit");
 		solo.sleep(30000);
 		solo.clickOnButton("Close");
 		solo.goBack();
 		deleteAccount();
 	} 
 	
 	/**
 	 * Tests if users enter non-matching passwords in Create Account page
 	 */
 	@Test
 	public void testNonmatchingPasswords() {
 		solo.clickOnButton("Create Account");
 		solo.assertCurrentActivity("Check on current page activity", CreateAccountActivity.class);
 		EditText edittextUsername = (EditText) solo.getView(R.id.user);
 		solo.enterText(edittextUsername, "ab3r5i9");
 		EditText edittextPassword = (EditText) solo.getView(R.id.pword);
 		solo.enterText(edittextPassword, "tets");
 		EditText edittextConfPassword = (EditText) solo.getView(R.id.conf_pword);
 		solo.enterText(edittextConfPassword, "test");
 		solo.clickOnButton("Create Account");
 		solo.clickOnButton("Close");
 		solo.goBack();
 	}
 	
 	/**
 	 * Tests if user enters empty fields in Create Account page
 	 */
 	@Test
 	public void testEmptyFields() {
 		solo.clickOnButton("Create Account");
 		solo.assertCurrentActivity("Check on current page activity", CreateAccountActivity.class);
 		EditText edittextUsername = (EditText) solo.getView(R.id.user);
 		solo.enterText(edittextUsername, " ");
 		EditText edittextPassword = (EditText) solo.getView(R.id.pword);
 		solo.enterText(edittextPassword, "tets");
 		EditText edittextConfPassword = (EditText) solo.getView(R.id.conf_pword);
 		solo.enterText(edittextConfPassword, "test");
 		solo.clickOnButton("Create Account");
 		solo.clickOnButton("Close");
 		solo.goBack();
 	}
 	
 	/**
 	 * Tests if user tries to create an account with a space in the middle
 	 */
 	@Test
 	public void testQsernameWithSpace() {
 		solo.clickOnButton("Create Account");
 		solo.assertCurrentActivity("Check on current page activity", CreateAccountActivity.class);
 		EditText edittextUsername = (EditText) solo.getView(R.id.user);
 		solo.enterText(edittextUsername, "jordan brandon");
 		EditText edittextPassword = (EditText) solo.getView(R.id.pword);
 		solo.enterText(edittextPassword, "test");
 		EditText edittextConfPassword = (EditText) solo.getView(R.id.conf_pword);
 		solo.enterText(edittextConfPassword, "test");
 		solo.clickOnButton("Create Account");
 		solo.clickOnButton("Close");
 		solo.goBack();
 	}
 	
 	/**
 	 * Tests if user tries to create an account with a username that's too long
 	 */
 	@Test
 	public void testQsernameTooLong() {
 		solo.clickOnButton("Create Account");
 		solo.assertCurrentActivity("Check on current page activity", CreateAccountActivity.class);
 		EditText edittextUsername = (EditText) solo.getView(R.id.user);
 		solo.enterText(edittextUsername, "abcdefghijklmnopqrstuvwxyz");
 		EditText edittextPassword = (EditText) solo.getView(R.id.pword);
 		solo.enterText(edittextPassword, "test");
 		EditText edittextConfPassword = (EditText) solo.getView(R.id.conf_pword);
 		solo.enterText(edittextConfPassword, "test");
 		solo.clickOnButton("Create Account");
 		solo.clickOnButton("Close");
 		solo.goBack();
 	}  
 	
 	/**
 	 * Tests if user tries to create an account with a password that's too long
 	 */
 	@Test
 	public void testPasswordTooLong() {
 		solo.clickOnButton("Create Account");
 		solo.assertCurrentActivity("Check on current page activity", CreateAccountActivity.class);
 		EditText edittextUsername = (EditText) solo.getView(R.id.user);
 		solo.enterText(edittextUsername, "test");
 		EditText edittextPassword = (EditText) solo.getView(R.id.pword);
 		solo.enterText(edittextPassword, "abcdefghijklmnopqrstuvwxyz");
 		EditText edittextConfPassword = (EditText) solo.getView(R.id.conf_pword);
 		solo.enterText(edittextConfPassword, "abcdefghijklmnopqrstuvwxyz");
 		solo.clickOnButton("Create Account");
 		solo.clickOnButton("Close");
 		solo.goBack();
 	}
 
 }
