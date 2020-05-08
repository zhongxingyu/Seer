 package realtalk.activities;
 
 import org.junit.Before;
 import org.junit.Test;
 import android.test.ActivityInstrumentationTestCase2;
 import android.widget.EditText;
 
 import realtalk.activities.LoginActivity;
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
 	} 
 	
 	public void deleteAccount() {
 		solo.clickOnMenuItem("Account Settings");
 		solo.clickOnButton("Click Here to Delete Account");
 		solo.clickOnButton("Yes");
 		solo.sleep(5000);
 		solo.clickOnButton("Close");
 	}
 	
 	@Test
 	public void testSuccessfulLogin() {
 		setUpAccount();
 		EditText edittextUsername = (EditText) solo.getView(R.id.editQuery);
 		solo.enterText(edittextUsername, "ab3r5i9");
 		EditText edittextPassword = (EditText) solo.getView(R.id.editPword);
 		solo.enterText(edittextPassword, "test");
 		solo.clickOnButton("Login");
 		solo.sleep(5000);
 		solo.assertCurrentActivity("Check on current page activity", SelectRoomActivity.class);
 		deleteAccount();
 	} 
 	
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
 		assertTrue("Could not find the dialog", solo.searchText("Passwords do not match.  Please try again."));
 		solo.clickOnButton("Close");
 		solo.goBack();
 	}
 	
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
 		assertTrue("Could not find the dialog", solo.searchText("Please fill in all of the fields."));
 		solo.clickOnButton("Close");
 		solo.goBack();
 	}
 	
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
 		assertTrue("Could not find the dialog", solo.searchText("Username may not contain spaces.  Please try again."));
 		solo.clickOnButton("Close");
 		solo.goBack();
 	}
 	
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
 		assertTrue("Could not find the dialog", solo.searchText("Username must not exceed 20 characters.  Please try again."));
 		solo.clickOnButton("Close");
 		solo.goBack();
 	}  
 	
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
 		assertTrue("Could not find the dialog", solo.searchText("Password must not exceed 20 characters.  Please try again."));
 		solo.clickOnButton("Close");
 		solo.goBack();
 	}
 
 }
