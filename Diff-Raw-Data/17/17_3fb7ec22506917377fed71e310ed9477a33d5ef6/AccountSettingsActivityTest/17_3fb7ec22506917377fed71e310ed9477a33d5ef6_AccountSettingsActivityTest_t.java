 package realtalk.activities;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import realtalk.controller.ChatControllerStub;
 import realtalk.util.UserInfo;
 
 import android.test.ActivityInstrumentationTestCase2;
 import android.widget.EditText;
 
 import com.jayway.android.robotium.solo.Solo;
 import com.realtalk.R;
 
 /**
  * Robotium test that tests the account settings activity.
  * 
  * @author Brandon
  *
  */
 public class AccountSettingsActivityTest extends ActivityInstrumentationTestCase2<AccountSettingsActivity> {
 	private Solo solo;
 	
 	public AccountSettingsActivityTest() {
 		super(AccountSettingsActivity.class);
 		ChatControllerStub.getInstance().fInitialize(new UserInfo("leeb92", "brandon", "aa"));
 	}
 	
 	@Before
 	public void setUp() throws Exception {
 		solo = new Solo(getInstrumentation(), getActivity());
 	}
 	
 	/**
 	 * tests controls are present
 	 */
 	@Test
 	public void testButtonsAndTextDisplay() {
 		assertTrue(solo.searchButton("Submit"));
 		assertTrue(solo.searchButton("Click Here to Delete Account"));
 		assertFalse(solo.searchButton("WRONG_BUTTON"));
 	}
 	
 	/**
 	 * Tests for an empty password and whether it is handled.
 	 */
 	@Test
 	public void testEmptyPassword() {
 		EditText edittextOld = (EditText) solo.getView(R.id.oldpword);
 		solo.enterText(edittextOld, " ");
 		EditText edittextNew = (EditText) solo.getView(R.id.newpword);
 		solo.enterText(edittextNew, "test");
 		EditText edittextConf = (EditText) solo.getView(R.id.confpword);
 		solo.enterText(edittextConf, " ");
 		solo.clickOnButton("Submit");
 		assertTrue("Could not find the dialog", solo.searchText("Please fill in all of the fields."));
 		solo.clickOnButton("Close");
 	} 
 	 
 	/**
 	 * Tests if non matching passwords is handled
 	 */
 	@Test
 	public void testNonMatchingPasswords() {
 		EditText edittextOld = (EditText) solo.getView(R.id.oldpword);
 		solo.enterText(edittextOld, "yo");
 		EditText edittextNew = (EditText) solo.getView(R.id.newpword);
 		solo.enterText(edittextNew, "test");
 		EditText edittextConf = (EditText) solo.getView(R.id.confpword);
 		solo.enterText(edittextConf, "testt");
 		solo.clickOnButton("Submit");
 		solo.clickOnButton("Close");
 	}
 	
 	/**
 	 * Tests if password is too long.
 	 */
 	@Test
 	public void testPasswordTooLong() {
 		EditText edittextOld = (EditText) solo.getView(R.id.oldpword);
 		solo.enterText(edittextOld, "yo");
 		EditText edittextNew = (EditText) solo.getView(R.id.newpword);
 		solo.enterText(edittextNew, "abababjdfjlkffffffffffasdfjlkdjaflkdsjffdfd");
 		EditText edittextConf = (EditText) solo.getView(R.id.confpword);
 		solo.enterText(edittextConf, "abababjdfjlkffffffffffasdfjlkdjaflkdsjffdfd");
 		solo.clickOnButton("Submit");
 		solo.clickOnButton("Close");
 	}
 	
 	/**
 	 * Tests if delete is denied if fields are poorly formed
 	 */
 	@Test
 	public void testDenyingDeleteAccount() {
 		solo.clickOnButton("Click Here to Delete Account");
 		solo.clickOnButton("No");
 	}
 }
