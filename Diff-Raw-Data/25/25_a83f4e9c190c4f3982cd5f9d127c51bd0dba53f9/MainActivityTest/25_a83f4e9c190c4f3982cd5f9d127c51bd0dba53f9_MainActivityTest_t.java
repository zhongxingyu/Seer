 package br.com.cavedon.passwordgenerator.activity.test;
 
 import org.apache.commons.lang3.StringUtils;
 
 import com.jayway.android.robotium.solo.Solo;
 
 import android.test.ActivityInstrumentationTestCase2;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import br.com.passwordgenerator.R;
 import br.com.cavedon.passwordgenerator.activity.MainActivity_;
 
 public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity_> {
 
 	private Solo solo;
 
 	private Button generateButton;	
 	private EditText masterPwdInput;
 	private EditText websiteInput;
 	private EditText generatedPwd;
 	private EditText limitSizeInput;
 	private CheckBox numbersOnlyCheckBox;
 	private CheckBox specialCharsCheckBox;
 	
 	
 	private final int NUMBERS_ONLY_CHECK_BOX = 0;
 	private final int SPECIAL_CHARS_CHECK_BOX = 1;
 	private final int LIMIT_SIZE_INPUT = 2;
 	
 	private static final String MASTER_PWD = "abc123";
 	private static final String WEBSITE_NAME = "www.google.com";
 	
 	public MainActivityTest() {
 		super(MainActivity_.class);
 	}
 	
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		
 		solo = new Solo(getInstrumentation(), getActivity());
 		generateButton = (Button) solo.getView(R.id.generate_pwd_btn);
 		masterPwdInput = (EditText) solo.getView(R.id.master_pwd_input);
 		websiteInput = (EditText) solo.getView(R.id.website_input);
 		limitSizeInput = (EditText) solo.getView(R.id.limit_size_input);
 		generatedPwd = (EditText) solo.getView(R.id.generated_pwd_text);
 		numbersOnlyCheckBox = (CheckBox) solo.getView(R.id.number_only_checkbox);
 		specialCharsCheckBox = (CheckBox) solo.getView(R.id.special_chars_checkbox);
 		
 		solo.clearEditText(masterPwdInput);
 		solo.clearEditText(websiteInput);
 	}
 	
 	@Override
 	protected void tearDown() throws Exception {
 		solo.finishOpenedActivities();
 		super.tearDown();
 	}
 	
 	public void testGenerateBtnEnabledConditions() {
 		assertFalse(generateButton.isEnabled());
 		
 		solo.enterText(masterPwdInput, MASTER_PWD);		
 		assertFalse(generateButton.isEnabled());
 		
 		solo.enterText(websiteInput, WEBSITE_NAME);		
 		assertTrue(generateButton.isEnabled());
 		
 		solo.clearEditText(masterPwdInput);
 		assertFalse(generateButton.isEnabled());
 	}
 	
 	public void testGeneratePassword() {		
 		solo.enterText(masterPwdInput, MASTER_PWD);
 		solo.enterText(websiteInput, WEBSITE_NAME);
 		
		solo.clickOnButton("Generate!");
 		assertTrue(StringUtils.isAlphanumeric(generatedPwd.getText().toString()));		
 		assertEquals("6e0f3a7cc3c55f566887556816a3d844", generatedPwd.getText().toString());
 	}
 	
 	public void testNumbersOnlyPassword() {
 		solo.clickOnCheckBox(NUMBERS_ONLY_CHECK_BOX);
 		assertFalse(specialCharsCheckBox.isEnabled());
 		
 		solo.enterText(masterPwdInput, MASTER_PWD);
 		solo.enterText(websiteInput, WEBSITE_NAME);
 		
		solo.clickOnButton("Generate!");
 		assertTrue(StringUtils.isNumeric(generatedPwd.getText().toString()));
 		assertEquals("61023779939552566887556816730844", generatedPwd.getText().toString());
 	}
 	
 	public void testSpecialCharsPassword() {
 		solo.clickOnCheckBox(SPECIAL_CHARS_CHECK_BOX);
 		assertFalse(numbersOnlyCheckBox.isEnabled());
 		
 		solo.enterText(masterPwdInput, MASTER_PWD);
 		solo.enterText(websiteInput, WEBSITE_NAME);
 		
		solo.clickOnButton("Generate!");
 		assertEquals("P#!6e0f3a7cc3c55f566887556816a3d844", generatedPwd.getText().toString());
 	}
 	
 	public void testLimitedSizePassword() {
 		solo.clickOnCheckBox(LIMIT_SIZE_INPUT);
 		assertTrue(limitSizeInput.isEnabled());
 		
 		solo.enterText(masterPwdInput, MASTER_PWD);
 		solo.enterText(websiteInput, WEBSITE_NAME);
 		solo.enterText(limitSizeInput, "8");
 		
		solo.clickOnButton("Generate!");
 		assertEquals(8, generatedPwd.getText().length());
 		assertEquals("6e0f3a7c", generatedPwd.getText().toString());		
 	}
 
 }
