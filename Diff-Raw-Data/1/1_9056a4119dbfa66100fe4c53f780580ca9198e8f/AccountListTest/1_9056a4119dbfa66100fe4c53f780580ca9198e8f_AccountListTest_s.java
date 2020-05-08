 package fr.geobert.radis.robotium;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 
 import android.test.ActivityInstrumentationTestCase2;
 import android.util.Log;
 import android.widget.EditText;
 import android.widget.TextView;
 
 import com.jayway.android.robotium.solo.Solo;
 
 import fr.geobert.radis.db.CommonDbAdapter;
 import fr.geobert.radis.tools.PrefsManager;
 import fr.geobert.radis.tools.Tools;
 
 @SuppressWarnings({ "unchecked", "rawtypes" })
 public class AccountListTest extends ActivityInstrumentationTestCase2 {
 	public static final String TAG = "RadisRobotium";
 	protected static final String TARGET_PACKAGE_ID = "fr.geobert.radis";
 	protected static final String LAUNCHER_ACTIVITY_FULL_CLASSNAME = "fr.geobert.radis.AccountList";
 
 	/*
 	 * Account Tests
 	 */
 	public static final String ACCOUNT_NAME = "Test";
 	public static final String ACCOUNT_START_SUM = "1000,50";
 	public static final String ACCOUNT_START_SUM_FORMATED_IN_EDITOR = "1 000,50";
 	public static final String ACCOUNT_START_SUM_FORMATED_ON_LIST = "1 000,50 €";
 	public static final String ACCOUNT_DESC = "Test Description";
 	public static final String ACCOUNT_NAME_2 = "Test2";
 	public static final String ACCOUNT_START_SUM_2 = "2000,50";
 	public static final String ACCOUNT_START_SUM_FORMATED_ON_LIST_2 = "2 000,50 €";
 	public static final String ACCOUNT_DESC_2 = "Test Description 2";
 
 	private static Class<?> launcherActivityClass;
 	static {
 		try {
 			launcherActivityClass = Class
 					.forName(LAUNCHER_ACTIVITY_FULL_CLASSNAME);
 		} catch (ClassNotFoundException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public AccountListTest() throws ClassNotFoundException {
 		super(TARGET_PACKAGE_ID, launcherActivityClass);
 	}
 
 	protected Solo solo;
 
 	protected void trashDb() {
 		CommonDbAdapter db = CommonDbAdapter.getInstance(getActivity());
 		db.trashDatabase();
 	}
 
 	private void printCurrentTextViews() {
 		ArrayList<TextView> tvs = solo.getCurrentTextViews(null);
 		for (int i = 0; i < tvs.size(); ++i) {
 			TextView v = tvs.get(i);
 			Log.i(AccountListTest.TAG, "TextView " + i + ": " + v.getText());
 		}
 	}
 
 	private void printCurrentEditTexts() {
 		ArrayList<EditText> tvs = solo.getCurrentEditTexts();
 		for (int i = 0; i < tvs.size(); ++i) {
 			EditText v = tvs.get(i);
 			Log.i(AccountListTest.TAG, "EditText " + i + ": " + v.getText());
 		}
 	}
 
 	private void sleep(long ms) {
 		try {
 			Thread.sleep(ms);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	protected void setUp() throws Exception {
 		solo = new Solo(getInstrumentation(), getActivity());
 		trashDb();
 	}
 
 	@Override
 	public void tearDown() throws Exception {
 		try {
 			solo.finalize();
 			trashDb();
 		} catch (Throwable e) {
 			e.printStackTrace();
 		}
 		getActivity().finish();
 		super.tearDown();
 	}
 
 	public void testPreConditions() {
 		assertEquals(0, solo.getCurrentListViews().get(0).getCount());
 	}
 
 	public void addAccount() {
 		solo.clickOnButton(0);
 		solo.enterText(0, ACCOUNT_NAME);
 		solo.enterText(1, ACCOUNT_START_SUM);
 		solo.enterText(3, ACCOUNT_DESC);
 		solo.clickOnText("Ok");
 		assertEquals(1, solo.getCurrentListViews().get(0).getCount());
 		assertEquals(ACCOUNT_NAME, solo.getText(2).getText().toString());
 		assertEquals(ACCOUNT_START_SUM_FORMATED_ON_LIST, solo.getText(3)
 				.getText().toString());
 	}
 
 	public void addAccount2() {
 		solo.clickOnButton(0);
 		solo.enterText(0, ACCOUNT_NAME_2);
 		solo.enterText(1, ACCOUNT_START_SUM_2);
 		solo.enterText(3, ACCOUNT_DESC_2);
 		solo.clickOnText("Ok");
 		assertEquals(2, solo.getCurrentListViews().get(0).getCount());
 	}
 
 	public void editAccount() {
 		solo.clickLongInList(0);
 		solo.clickOnMenuItem("Modifier");
 		assertEquals(ACCOUNT_NAME, solo.getEditText(0).getText().toString());
 		assertEquals(ACCOUNT_START_SUM_FORMATED_IN_EDITOR, solo.getEditText(1)
 				.getText().toString());
 		assertEquals(ACCOUNT_DESC, solo.getEditText(3).getText().toString());
 		solo.clearEditText(0);
 		solo.enterText(0, ACCOUNT_NAME_2);
 		solo.clearEditText(1);
 		solo.enterText(1, ACCOUNT_START_SUM_2);
 		solo.clearEditText(3);
 		solo.enterText(3, ACCOUNT_DESC_2);
 		solo.clickOnText("Ok");
 		// assertEquals(1, solo.getCurrentListViews().get(0).getCount());
 		assertEquals(ACCOUNT_NAME_2, solo.getText(2).getText().toString());
 		assertEquals(ACCOUNT_START_SUM_FORMATED_ON_LIST_2, solo.getText(3)
 				.getText().toString());
 	}
 
 	public void deleteAccount(int originalCount) {
 		solo.clickLongInList(0);
 		solo.clickOnMenuItem("Supprimer");
 		solo.clickOnButton("Oui");
 		assertEquals(originalCount - 1, solo.getCurrentListViews().get(0)
 				.getCount());
 	}
 
 	public void testQuickAddStateOnHomeScreen() {
 		PrefsManager.getInstance(getActivity()).resetAll();
 
 		assertFalse(solo.getEditText(0).isEnabled());
 		assertFalse(solo.getEditText(1).isEnabled());
 		assertFalse(solo.getButton(2).isEnabled());
 		printCurrentTextViews();
 		TextView v = solo.getText(4);
 		assertEquals(
 				getActivity().getString(
 						fr.geobert.radis.R.string.quickadd_target_no_account),
 				v.getText());
 		addAccount();
 		assertEquals(
 				getActivity().getString(
 						fr.geobert.radis.R.string.quickadd_target_to_configure),
 				v.getText());
 		solo.pressMenuItem(1);
 		solo.clickInList(2);
 		solo.clickInList(0);
 		solo.goBack();
 		assertEquals(
 				getActivity()
 						.getString(fr.geobert.radis.R.string.quickadd_target,
 								ACCOUNT_NAME), v.getText());
 		assertTrue(solo.getEditText(0).isEnabled());
 		assertTrue(solo.getEditText(1).isEnabled());
 		assertFalse(solo.getButton(2).isEnabled());
 
 		// test adding op
 		solo.enterText(0, "quick add");
 		solo.enterText(1, "10,50");
 		solo.clickOnButton(2);
 		assertEquals("990,00 €", solo.getText(3).getText().toString());
 		solo.clickInList(0);
 		assertNotNull(solo.getText("quick add"));
 		solo.goBack();
 
 		solo.enterText(0, "quick add2");
 		solo.enterText(1, "+10,50");
 		solo.clickOnButton(2);
 		assertEquals("1 000,50 €", solo.getText(3).getText().toString());
 		solo.clickInList(0);
 		assertNotNull(solo.getText("quick add"));
 		assertNotNull(solo.getText("quick add2"));
 		solo.goBack();
 
 		// test labels and state
 		editAccount();
 		assertEquals(
 				getActivity().getString(
 						fr.geobert.radis.R.string.quickadd_target,
 						ACCOUNT_NAME_2), v.getText());
 
 		addAccount2();
 		deleteAccount(2);
 		assertEquals(
 				getActivity().getString(
 						fr.geobert.radis.R.string.quickadd_target_to_configure),
 				v.getText());
 
 		deleteAccount(1);
 		assertFalse(solo.getEditText(0).isEnabled());
 		assertFalse(solo.getEditText(1).isEnabled());
 		assertFalse(solo.getButton(2).isEnabled());
 		assertEquals(
 				getActivity().getString(
 						fr.geobert.radis.R.string.quickadd_target_no_account),
 				v.getText());
 	}
 
 	/*
 	 * Operations Tests
 	 */
 
 	private static final String OP_TP = "Operation 1";
 	private static final String OP_AMOUNT = "10.50";
 	private static final String OP_AMOUNT_FORMATED = "-10,50";
 	private static final String OP_TAG = "Tag 1";
 	private static final String OP_MODE = "Carte bleue";
 	private static final String OP_DESC = "Robotium Operation 1";
 
 	private static final String OP_TP_2 = "Third party";
 	private static final String OP_AMOUNT_2 = "100";
 	private static final String OP_AMOUNT_FORMATED_2 = "100,00";
 	private static final String OP_TAG_2 = "Tag 1";
 	private static final String OP_MODE_2 = "Virement";
 	private static final String OP_DESC_2 = "Robotium Operation 2";
 
 	private void setUpOpTest() {
 		addAccount();
 		solo.clickInList(0);
 	}
 
 	public void addOp() {
 		setUpOpTest();
 
 		solo.pressMenuItem(0);
 		solo.enterText(3, OP_TP);
 		for (int i = 0; i < OP_AMOUNT.length(); ++i) {
 			solo.enterText(4, String.valueOf(OP_AMOUNT.charAt(i)));
 		}
 		solo.enterText(5, OP_TAG);
 		solo.enterText(6, OP_MODE);
 		solo.enterText(7, OP_DESC);
 		solo.clickOnButton("Ok");
 		assertTrue(solo.getText(1).getText().toString().contains("= 990,00"));
 		assertTrue(solo.getText(4).getText().toString()
 				.equals(OP_AMOUNT_FORMATED));
 	}
 
 	public void addManyOps() {
 		setUpOpTest();
 		for (int j = 0; j < 40; ++j) {
 			solo.pressMenuItem(0);
 			solo.enterText(3, OP_TP + j);
 			solo.enterText(4, OP_AMOUNT_2);
 			solo.enterText(5, OP_TAG);
 			solo.enterText(6, OP_MODE);
 			solo.enterText(7, OP_DESC);
 			solo.clickOnButton("Ok");
 		}
 		assertTrue(solo.getText(1).getText().toString().contains("= -2 999,50"));
 	}
 
 	public void testEditOp() {
 		addManyOps();
 		solo.clickLongInList(0);
 		solo.clickOnMenuItem("Modifier");
 		solo.clearEditText(4);
 		solo.enterText(4, "103");
 		solo.clickOnButton("Ok");
 		assertTrue(solo.getText(1).getText().toString().contains("= -2 796,50"));
 	}
 
 	public void testDisableAutoNegate() {
 		setUpOpTest();
 		solo.pressMenuItem(0);
 		solo.enterText(3, OP_TP);
 		solo.enterText(4, "+");
 		for (int i = 0; i < OP_AMOUNT.length(); ++i) {
 			solo.enterText(4, String.valueOf(OP_AMOUNT.charAt(i)));
 		}
 		printCurrentEditTexts();
 		assertTrue(solo.getEditText(4).getText().toString().equals("+10,50"));
 		solo.clickOnButton("Ok");
 		printCurrentTextViews();
 		assertTrue(solo.getText(1).getText().toString().contains("= 1 011,00"));
 		assertTrue(solo.getText(4).getText().toString().equals("10,50"));
 	}
 
 	/**
 	 * Schedule ops
 	 */
 
 	public void testNoAccount() {
 		assertFalse(solo.getButton("Échéancier").isEnabled());
 	}
 
 	private void setUpSchOp() {
 		addAccount();
 		assertTrue(solo.getButton("Échéancier").isEnabled());
 		solo.clickOnButton("Échéancier");
 		assertEquals(0, solo.getCurrentListViews().get(0).getCount());
 	}
 
 	public void addScheduleOp() {
 		setUpSchOp();
 		solo.pressMenuItem(0);
 		GregorianCalendar today = new GregorianCalendar();
 		Tools.clearTimeOfCalendar(today);
 		// today.add(Calendar.MONTH, -1);
 		solo.setDatePicker(0, today.get(Calendar.YEAR),
 				today.get(Calendar.MONTH), 4);
 		solo.enterText(3, OP_TP);
 		solo.enterText(4, "9,50");
 		solo.enterText(5, OP_TAG);
 		solo.enterText(6, OP_MODE);
 		solo.enterText(7, OP_DESC);
 		solo.clickOnButton("Ok");
 		assertEquals(1, solo.getCurrentListViews().get(0).getCount());
 		solo.goBack();
 		solo.clickInList(0);
 		sleep(1000);
 
 		// -1 is for "get more ops" line
 		assertEquals(1, solo.getCurrentListViews().get(0).getCount() - 1);
 		printCurrentTextViews();
 		assertTrue(solo.getText(1).getText().toString().contains("= 991,00"));
 	}
 
 	public void testEditScheduledOp() {
 		addScheduleOp();
 		solo.clickLongInList(0);
 		solo.clickOnMenuItem("Modifier");
 		solo.clearEditText(4);
 		solo.enterText(4, "-7,50");
 		solo.clickOnButton("Ok");
 		solo.clickOnButton("Mettre à jour");
 		printCurrentTextViews();
 		assertTrue(solo.getText(1).getText().toString().contains("= 993,00"));
 	}
 
 	// issue 59 test
 	public void testDeleteAllOccurences() {
 		setUpSchOp();
 		solo.pressMenuItem(0);
 		GregorianCalendar today = new GregorianCalendar();
 		Tools.clearTimeOfCalendar(today);
 		today.add(Calendar.MONTH, -2);
 		solo.setDatePicker(0, today.get(Calendar.YEAR),
 				today.get(Calendar.MONTH), 4);
 		solo.enterText(3, OP_TP);
 		solo.enterText(4, "9,50");
 		solo.enterText(5, OP_TAG);
 		solo.enterText(6, OP_MODE);
 		solo.enterText(7, OP_DESC);
 		solo.clickOnButton("Ok");
 		sleep(1000);
 		solo.goBack();
 		solo.clickInList(0);
 		solo.clickInList(2);
 		sleep(2000);
 		solo.clickInList(3);
 		sleep(2000);
 
 		// -1 is for "get more ops" line
 		assertEquals(3, solo.getCurrentListViews().get(0).getCount() - 1);
 		solo.pressMenuItem(1);
 		solo.clickLongInList(0);
 		solo.clickOnMenuItem("Supprimer");
 		solo.clickOnButton("Tout");
 		solo.goBack();
 
 		// -1 is for "get more ops" line
 		assertEquals(0, solo.getCurrentListViews().get(0).getCount() - 1);
 	}
 
 	/**
 	 * Infos
 	 */
 
 	// test adding info with different casing
 	public void testAddExistingInfo() {
 		setUpOpTest();
 		solo.pressMenuItem(0);
 		solo.enterText(3, OP_TP);
 		for (int i = 0; i < OP_AMOUNT.length(); ++i) {
 			solo.enterText(4, String.valueOf(OP_AMOUNT.charAt(i)));
 		}
 		solo.clickOnButton(2);
 		solo.clickOnButton("Créer");
 		solo.enterText(0, "Atest");
 		solo.clickOnButton("Ok");
 		sleep(1000);
 		assertEquals(1, solo.getCurrentListViews().get(0).getCount());
 		solo.clickOnButton(2);
 		solo.clickOnButton("Créer");
 		solo.enterText(0, "ATest");
 		solo.clickOnButton("Ok");
 		assertNotNull(solo.getText(getActivity().getString(
 				fr.geobert.radis.R.string.item_exists)));
 	}
 
 	// issue 50 test
 	public void testAddInfoAndCreateOp() {
 		setUpOpTest();
 		solo.pressMenuItem(0);
 		solo.enterText(3, OP_TP);
 		for (int i = 0; i < OP_AMOUNT.length(); ++i) {
 			solo.enterText(4, String.valueOf(OP_AMOUNT.charAt(i)));
 		}
 		solo.clickOnButton(2);
 		solo.clickOnButton("Créer");
 		solo.enterText(0, "Atest");
 		solo.clickOnButton("Ok");
 		solo.clickInList(0);
 		solo.clickOnButton("Ok");
 		solo.clickOnButton("Ok");
 		solo.pressMenuItem(0);
 		solo.clickOnButton(2);
 		assertEquals(1, solo.getCurrentListViews().get(0).getCount());
 	}
 
 	private void addOpOnDate(GregorianCalendar t) {
 		solo.pressMenuItem(0);
 		solo.setDatePicker(0, t.get(Calendar.YEAR), t.get(Calendar.MONTH),
 				t.get(Calendar.DAY_OF_MONTH));
 		solo.enterText(3, OP_TP);
 		solo.enterText(4, "1");
 		solo.clickOnButton("Ok");
 	}
 
 	private void setUpProjTest1() {
 		addAccount();
 		solo.clickInList(0);
 		GregorianCalendar today = new GregorianCalendar();
 		Tools.clearTimeOfCalendar(today);
 		today.roll(Calendar.MONTH, -2);
 		for (int i = 0; i < 6; ++i) {
 			addOpOnDate(today);
 			today.roll(Calendar.MONTH, +1);
 		}
 	}
 
 	public void testProjectionFromOpList() {
 		// test mode 0
 		setUpProjTest1();
 		assertTrue(solo.getButton(0).getText().toString().contains("= 994,50"));
 		assertTrue(solo.getText(0).getText().toString().contains("= 997,50"));
 
 		// test mode 1
 		solo.clickOnButton(0);
 		solo.pressSpinnerItem(0, 1);
 		assertTrue(solo.getEditText(0).isEnabled());
 		GregorianCalendar today = new GregorianCalendar();
 		Tools.clearTimeOfCalendar(today);
 		solo.enterText(0, Integer.toString(today.get(Calendar.DAY_OF_MONTH)));
 		solo.clickOnButton("Ok");
 		assertTrue(solo.getButton(0).getText().toString().contains("= 996,50"));
 		assertTrue(solo.getText(0).getText().toString().contains("= 997,50"));
 
 		// test mode 2
 		solo.clickOnButton(0);
 		solo.pressSpinnerItem(0, 1);
 		assertTrue(solo.getEditText(0).isEnabled());
 		today.roll(Calendar.MONTH, +3);
 		solo.enterText(0, Integer.toString(today.get(Calendar.DAY_OF_MONTH))
 				+ "/" + Integer.toString(today.get(Calendar.MONTH)) + "/"
 				+ Integer.toString(today.get(Calendar.YEAR)));
 		solo.clickOnButton("Ok");
 		assertTrue(solo.getButton(0).getText().toString().contains("= 995,50"));
 		assertTrue(solo.getText(0).getText().toString().contains("= 997,50"));
 
 		solo.clickInList(solo.getCurrentListViews().get(0).getCount());
 		sleep(1000);
 		solo.clickInList(solo.getCurrentListViews().get(0).getCount());
 		sleep(1000);
 		solo.clickInList(5);
 		sleep(1000);
 		assertTrue(solo.getButton(0).getText().toString().contains("= 995,50"));
 		assertTrue(solo.getText(0).getText().toString().contains("= 998,50"));
 
 		// test back to mode 0
 		solo.clickOnButton(0);
 		solo.pressSpinnerItem(0, -2);
 		assertFalse(solo.getEditText(0).isEnabled());
 		solo.clickOnButton("Ok");
 		assertTrue(solo.getButton(0).getText().toString().contains("= 994,50"));
 		assertTrue(solo.getText(0).getText().toString().contains("= 998,50"));
 	}
 
 	public void testProjectionFromAccount() {
 		setUpProjTest1();
 		assertTrue(solo.getButton(0).getText().toString().contains("= 994,50"));
 		assertTrue(solo.getText(0).getText().toString().contains("= 997,50"));
 
 		// test mode 1
 		solo.goBack();
 		solo.clickLongInList(0);
 		solo.clickOnMenuItem("Modifier");
 		solo.pressSpinnerItem(1, 1);
 		assertTrue(solo.getEditText(2).isEnabled());
 		GregorianCalendar today = new GregorianCalendar();
 		Tools.clearTimeOfCalendar(today);
 		solo.enterText(2, Integer.toString(today.get(Calendar.DAY_OF_MONTH)));
 		solo.clickOnButton("Ok");
 		assertTrue(solo.getText(3).getText().toString().contains("996,50"));
 
 		// test mode 2
 		solo.clickLongInList(0);
 		solo.clickOnMenuItem("Modifier");
 		solo.pressSpinnerItem(1, 1);
 		assertTrue(solo.getEditText(2).isEnabled());
 		today.roll(Calendar.MONTH, +3);
 		solo.enterText(2, Integer.toString(today.get(Calendar.DAY_OF_MONTH))
 				+ "/" + Integer.toString(today.get(Calendar.MONTH)) + "/"
 				+ Integer.toString(today.get(Calendar.YEAR)));
 		solo.clickOnButton("Ok");
 		assertTrue(solo.getText(3).getText().toString().contains("995,50"));
 
 		// test back to mode 0
 		solo.clickLongInList(0);
 		solo.clickOnMenuItem("Modifier");
 		solo.pressSpinnerItem(1, -2);
 		assertFalse(solo.getEditText(2).isEnabled());
 		solo.clickOnButton("Ok");
 		assertTrue(solo.getText(3).getText().toString().contains("994,50"));
 	}
 
 	public void addOpMode1() {
 		// add account
 		solo.clickOnButton(0);
 		solo.enterText(0, ACCOUNT_NAME);
 		solo.enterText(1, ACCOUNT_START_SUM);
 		solo.enterText(3, ACCOUNT_DESC);
 		solo.pressSpinnerItem(1, 1);
 		assertTrue(solo.getEditText(2).isEnabled());
 		GregorianCalendar today = new GregorianCalendar();
 		Tools.clearTimeOfCalendar(today);
 		today.roll(Calendar.DAY_OF_MONTH, 1);
 		solo.enterText(2, Integer.toString(today.get(Calendar.DAY_OF_MONTH)));
 		solo.clickOnButton("Ok");
 		solo.clickInList(0);
 		assertTrue(solo.getText(0).getText().toString().contains("= 1 000,50"));
 
 		today.roll(Calendar.DAY_OF_MONTH, -1);
 		addOpOnDate(today);
 		assertTrue(solo.getButton(0).getText().toString().contains("= 999,50"));
 		assertTrue(solo.getText(0).getText().toString().contains("= 999,50"));
 
 		// add op after X
 		today.roll(Calendar.MONTH, +1);
 		addOpOnDate(today);
 		solo.clickInList(0);
 		assertTrue(solo.getButton(0).getText().toString().contains("= 999,50"));
 		assertTrue(solo.getText(0).getText().toString().contains("= 998,50"));
 
 		// add op before X of next month, should update the current sum
 		today.roll(Calendar.MONTH, -2);
 		addOpOnDate(today);
 		solo.clickInList(0);
 		assertTrue(solo.getButton(0).getText().toString().contains("= 998,50"));
 		assertTrue(solo.getText(0).getText().toString().contains("= 997,50"));
 	}
 
 	public void editOpMode1() {
 		addOpMode1();
 		
 		solo.clickLongInList(0);
 		solo.clickOnMenuItem("Modifier");
 		solo.clearEditText(4);
 		solo.enterText(4, "+2");
 		solo.clickOnButton("Ok");
 		solo.clickInList(0);
 		assertTrue(solo.getButton(0).getText().toString().contains("= 998,50"));
 		assertTrue(solo.getText(0).getText().toString().contains("= 1 000,50"));
 		
 		solo.clickInList(3);
 		solo.clickLongInList(3);
 		solo.clickOnMenuItem("Modifier");
 		solo.clearEditText(4);
 		solo.enterText(4, "+2");
 		solo.clickOnButton("Ok");
 		solo.clickInList(0);
 		assertTrue(solo.getButton(0).getText().toString().contains("= 1 001,50"));
 		assertTrue(solo.getText(0).getText().toString().contains("= 1 003,50"));
 	}
 	
 	public void addOpMode2() {
 		// add account
 		solo.clickOnButton(0);
 		solo.enterText(0, ACCOUNT_NAME);
 		solo.enterText(1, ACCOUNT_START_SUM);
 		solo.enterText(3, ACCOUNT_DESC);
 		solo.pressSpinnerItem(1, 2);
 		assertTrue(solo.getEditText(2).isEnabled());
 		GregorianCalendar today = new GregorianCalendar();
 		Tools.clearTimeOfCalendar(today);
 		today.roll(Calendar.DAY_OF_MONTH, 1);
 		solo.enterText(2, Integer.toString(today.get(Calendar.DAY_OF_MONTH))
 				+ "/" + Integer.toString(today.get(Calendar.MONTH) + 1) + "/"
 				+ Integer.toString(today.get(Calendar.YEAR)));
 		solo.clickOnButton("Ok");
 		solo.clickInList(0);
 		assertTrue(solo.getText(0).getText().toString().contains("= 1 000,50"));
 
 		today.roll(Calendar.DAY_OF_MONTH, -1);
 		addOpOnDate(today);
 		assertTrue(solo.getButton(0).getText().toString().contains("= 999,50"));
 		assertTrue(solo.getText(0).getText().toString().contains("= 999,50"));
 
 		// add op after X
 		today.roll(Calendar.MONTH, +1);
 		addOpOnDate(today);
 		solo.clickInList(0);
 		assertTrue(solo.getButton(0).getText().toString().contains("= 999,50"));
 		assertTrue(solo.getText(0).getText().toString().contains("= 998,50"));
 
 		// add op before X of next month, should update the current sum
 		today.roll(Calendar.MONTH, -2);
 		addOpOnDate(today);
 		solo.clickInList(0);
 		assertTrue(solo.getButton(0).getText().toString().contains("= 998,50"));
 		assertTrue(solo.getText(0).getText().toString().contains("= 997,50"));
 	}
 	
 	public void editOpMode2() {
 		addOpMode2();
 		
 		solo.clickLongInList(0);
 		solo.clickOnMenuItem("Modifier");
 		solo.clearEditText(4);
 		solo.enterText(4, "+2");
 		solo.clickOnButton("Ok");
 		solo.clickInList(0);
 		assertTrue(solo.getButton(0).getText().toString().contains("= 998,50"));
 		assertTrue(solo.getText(0).getText().toString().contains("= 1 000,50"));
 		
 		solo.clickInList(3);
 		solo.clickLongInList(3);
 		solo.clickOnMenuItem("Modifier");
 		solo.clearEditText(4);
 		solo.enterText(4, "+2");
 		solo.clickOnButton("Ok");
 		solo.clickInList(0);
 		assertTrue(solo.getButton(0).getText().toString().contains("= 1 001,50"));
 		assertTrue(solo.getText(0).getText().toString().contains("= 1 003,50"));
 	}
 	
 	private void delOps() {
 		solo.clickLongInList(0);
 		solo.clickOnMenuItem("Supprimer");
 		solo.clickOnButton("Oui");
 		assertTrue(solo.getButton(0).getText().toString().contains("= 1 001,50"));
 		assertTrue(solo.getText(0).getText().toString().contains("= 1 001,50"));
 		
 		solo.clickLongInList(2);
 		solo.clickOnMenuItem("Supprimer");
 		solo.clickOnButton("Oui");
 		assertTrue(solo.getButton(0).getText().toString().contains("= 999,50"));
 		assertTrue(solo.getText(0).getText().toString().contains("= 999,50"));
 	}
 	
 	public void testDelOpMode1() {
 		editOpMode1();
 		delOps();
 	}
 	
 	public void testDelOpMode2() {
 		editOpMode2();
 		delOps();
 	}
 }
