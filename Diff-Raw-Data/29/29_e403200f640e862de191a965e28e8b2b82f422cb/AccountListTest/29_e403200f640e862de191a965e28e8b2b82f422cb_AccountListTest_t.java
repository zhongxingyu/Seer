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
 
 @SuppressWarnings({ "unchecked", "rawtypes" })
 public class AccountListTest extends ActivityInstrumentationTestCase2 {
 	public static final String TAG = "RadisRobotium";
 	protected static final String TARGET_PACKAGE_ID = "fr.geobert.radis";
 	protected static final String LAUNCHER_ACTIVITY_FULL_CLASSNAME = "fr.geobert.radis.AccountList";
 
 	/*
 	 * Account Tests
 	 */
 	private static final String ACCOUNT_NAME = "Test";
 	private static final String ACCOUNT_START_SUM = "1000,50";
 	private static final String ACCOUNT_START_SUM_FORMATED_IN_EDITOR = "1 000,50";
 	private static final String ACCOUNT_START_SUM_FORMATED_ON_LIST = "1 000,50 €";
 	private static final String ACCOUNT_DESC = "Test Description";
 	private static final String ACCOUNT_NAME_2 = "Test2";
 	private static final String ACCOUNT_START_SUM_2 = "2000,50";
 	private static final String ACCOUNT_START_SUM_FORMATED_ON_LIST_2 = "2 000,50 €";
 	private static final String ACCOUNT_DESC_2 = "Test Description 2";
 
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
 		solo.pressMenuItem(0);
 		solo.enterText(0, ACCOUNT_NAME);
 		solo.enterText(1, ACCOUNT_START_SUM);
 		solo.enterText(2, ACCOUNT_DESC);
 		solo.clickOnText("Ok");
 		assertEquals(1, solo.getCurrentListViews().get(0).getCount());
 		assertTrue(ACCOUNT_NAME.equals(solo.getText(2).getText().toString()));
 		assertTrue(ACCOUNT_START_SUM_FORMATED_ON_LIST.equals(solo.getText(3)
 				.getText().toString()));
 	}
 
 	public void testEditAccount() {
 		addAccount();
 		solo.clickLongInList(0);
 		solo.clickOnMenuItem("Modifier");
 		assertTrue(ACCOUNT_NAME
 				.equals(solo.getEditText(0).getText().toString()));
 		assertTrue(ACCOUNT_START_SUM_FORMATED_IN_EDITOR.equals(solo
 				.getEditText(1).getText().toString()));
 		assertTrue(ACCOUNT_DESC
 				.equals(solo.getEditText(2).getText().toString()));
 		solo.clearEditText(0);
 		solo.enterText(0, ACCOUNT_NAME_2);
 		solo.clearEditText(1);
 		solo.enterText(1, ACCOUNT_START_SUM_2);
 		solo.clearEditText(2);
 		solo.enterText(2, ACCOUNT_DESC_2);
 		solo.clickOnText("Ok");
 		assertEquals(1, solo.getCurrentListViews().get(0).getCount());
 		assertTrue(ACCOUNT_NAME_2.equals(solo.getText(2).getText().toString()));
 		assertTrue(ACCOUNT_START_SUM_FORMATED_ON_LIST_2.equals(solo.getText(3)
 				.getText().toString()));
 	}
 
 	public void testDeleteAccount() {
 		addAccount();
 		solo.clickLongInList(0);
 		solo.clickOnMenuItem("Supprimer");
 		solo.clickOnButton("Oui");
 		assertEquals(0, solo.getCurrentListViews().get(0).getCount());
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
 		assertTrue(solo.getText(4).getText().toString().equals(OP_AMOUNT_FORMATED));
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
 		printCurrentTextViews();
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
 		assertTrue(solo.getText(1).getText().toString().contains("= 1 011,00"));
 		assertTrue(solo.getText(4).getText().toString().equals("10,50"));
 	}
 	/**
 	 * Schedule ops
 	 */
 	
 	public void testNoAccount() {
 		assertFalse(solo.getButton("Échéancier").isEnabled());
 	}
 	
 	public void addScheduleOp() {
 		addAccount();
 		assertTrue(solo.getButton("Échéancier").isEnabled());
 		solo.clickOnButton("Échéancier");
 		assertEquals(0, solo.getCurrentListViews().get(0).getCount());
 		solo.pressMenuItem(0);
 		GregorianCalendar today = new GregorianCalendar();
 		//today.add(Calendar.MONTH, -1);
 		solo.setDatePicker(0, today.get(Calendar.YEAR), today.get(Calendar.MONTH), 4);
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
 		assertEquals(1, solo.getCurrentListViews().get(0).getCount() - 1); // -1 is for "get more ops" line
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
 		assertNotNull(solo.getText(getActivity().getString(fr.geobert.radis.R.string.item_exists)));
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
	
 	private void sleep(long ms) {
 		try {
 			Thread.sleep(ms);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 }
