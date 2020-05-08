 package uw.cse403.minion.test;
 import com.jayway.android.robotium.solo.Solo;
 
 import uw.cse403.minion.AbilityScoresActivity;
import uw.cse403.minion.BasicInfoActivity;
 import uw.cse403.minion.CharCreateMainActivity;
 import uw.cse403.minion.CharacterDataSource;
 import uw.cse403.minion.CharactersActivity;
 import uw.cse403.minion.HomeActivity;
 import uw.cse403.minion.LoginActivity;
 import uw.cse403.minion.SaveSharedPreference;
 
 import android.app.Activity;
 import android.test.ActivityInstrumentationTestCase2;
 import android.widget.EditText;
 import android.widget.TextView;
 
 
 public class EditExistingCharacterTest extends
 	ActivityInstrumentationTestCase2<LoginActivity>{
 
 	private static final String VALID_USERNAME = "UseForTestingOnly";
 	private static final String VALID_PASSWORD = "abcDEF123@";
 	private static final String EMPTY = "";
 	private static final String CHAR_NAME = "UseForTestingOnlyCharacter";
 	
 	private static final String RANK_0 = "10";
 	private static final String RANK_1 = "11";
 	private static final String RANK_2 = "12";
 	private static final String RANK_3 = "13";
 	private static final String RANK_4 = "14";
 	private static final String RANK_5 = "15";
 	private static final String MOD_0 = "0";
 	private static final String MOD_1 = "1";
 	private static final String MOD_2 = "2";
 	
 	private Solo solo;
 	private Activity loginActivity;
 
 	public EditExistingCharacterTest() {
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
 		SaveSharedPreference.clearPreferences(loginActivity);
 	}
 	
 	public void testEditExisting() {
 		//Login
 		SaveSharedPreference.clearPreferences(loginActivity);
 		EditText usernameField = (EditText) getActivity().findViewById(uw.cse403.minion.R.id.username_input); 
 		EditText passwordField = (EditText) getActivity().findViewById(uw.cse403.minion.R.id.password_input); 
 		
 		solo.typeText(usernameField, VALID_USERNAME);
 		solo.typeText(passwordField, VALID_PASSWORD);
 		solo.clickOnButton(solo.getString(uw.cse403.minion.R.string.login_button));
 		solo.assertCurrentActivity("Login successful", HomeActivity.class);
 		//Login Finished
 		
 		//Go To Character Activity
 		solo.clickOnButton(solo.getString(uw.cse403.minion.R.string.button_manage_characters));	
 		solo.assertCurrentActivity("CharactersActivity", CharactersActivity.class);
 		
 		//Edit Character
 		solo.clickOnText(CHAR_NAME);
 		
 		//Edit some Basic Info
 		solo.clickOnButton(solo.getString(uw.cse403.minion.R.string.button_basic_info));
 		solo.assertCurrentActivity("basic info", BasicInfoActivity.class);
 		
 		solo.pressSpinnerItem(1, 5);
 		boolean sizeVal = solo.isSpinnerTextSelected(1, "Large");
 		assertTrue(sizeVal);
 	
 		solo.clickOnButton(solo.getString(uw.cse403.minion.R.string.save));
 		solo.assertCurrentActivity("save basic info", CharCreateMainActivity.class);
 		
 		//Assert Ability Score stuff has been saved properly
 		solo.clickOnButton(solo.getString(uw.cse403.minion.R.string.button_ability_scores));
 		solo.assertCurrentActivity("ability scores", AbilityScoresActivity.class);
 		
 		//Make sure information is saved and totals updated
 		TextView strTotal = (TextView) solo.getView(uw.cse403.minion.R.id.str_ab_score);
 		TextView dexTotal = (TextView) solo.getView(uw.cse403.minion.R.id.dex_ab_score);
 		TextView conTotal = (TextView) solo.getView(uw.cse403.minion.R.id.con_ab_score);
 		TextView intTotal = (TextView) solo.getView(uw.cse403.minion.R.id.int_ab_score);
 		TextView wisTotal = (TextView) solo.getView(uw.cse403.minion.R.id.wis_ab_score);
 		TextView chaTotal = (TextView) solo.getView(uw.cse403.minion.R.id.cha_ab_score);
 
 		TextView strMod = (TextView) solo.getView(uw.cse403.minion.R.id.str_ab_mod);
 		TextView dexMod = (TextView) solo.getView(uw.cse403.minion.R.id.dex_ab_mod);
 		TextView conMod = (TextView) solo.getView(uw.cse403.minion.R.id.con_ab_mod);
 		TextView intMod = (TextView) solo.getView(uw.cse403.minion.R.id.int_ab_mod);
 		TextView wisMod = (TextView) solo.getView(uw.cse403.minion.R.id.wis_ab_mod);
 		TextView chaMod = (TextView) solo.getView(uw.cse403.minion.R.id.cha_ab_mod);
 		
 		String strTotalText = strTotal.getText().toString();
 		String dexTotalText = dexTotal.getText().toString();
 		String conTotalText = conTotal.getText().toString();
 		String intTotalText = intTotal.getText().toString();
 		String wisTotalText = wisTotal.getText().toString();
 		String chaTotalText = chaTotal.getText().toString();
 		
 		String strModText = strMod.getText().toString();
 		String dexModText = dexMod.getText().toString();
 		String conModText = conMod.getText().toString();
 		String intModText = intMod.getText().toString();
 		String wisModText = wisMod.getText().toString();
 		String chaModText = chaMod.getText().toString();
 
 		assertTrue(RANK_0.equals(strTotalText));
 		assertTrue(RANK_1.equals(dexTotalText));
 		assertTrue(RANK_2.equals(conTotalText));
 		assertTrue(RANK_3.equals(intTotalText));
 		assertTrue(RANK_4.equals(wisTotalText));
 		assertTrue(RANK_5.equals(chaTotalText));
 		
 		assertTrue(MOD_0.equals(strModText));
 		assertTrue(MOD_0.equals(dexModText));
 		assertTrue(MOD_1.equals(conModText));
 		assertTrue(MOD_1.equals(intModText));
 		assertTrue(MOD_2.equals(wisModText));
 		assertTrue(MOD_2.equals(chaModText));
 
 		solo.clickOnButton(solo.getString(uw.cse403.minion.R.string.save));
 		solo.assertCurrentActivity("save basic info", CharCreateMainActivity.class);
 
 	}
 	
 	@Override
 	protected void tearDown() throws Exception {
 		CharacterDataSource.deleteAllCharacters();
 		SaveSharedPreference.setUserName(loginActivity, EMPTY);
 		solo.finishOpenedActivities();
 		
 	}
 }
