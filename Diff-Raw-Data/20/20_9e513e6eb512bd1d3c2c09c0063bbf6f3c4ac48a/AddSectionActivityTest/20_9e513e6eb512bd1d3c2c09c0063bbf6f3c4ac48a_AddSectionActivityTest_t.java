 package cs428.mobiletaandroidapp.section;
 
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.test.ActivityInstrumentationTestCase2;
 import android.view.KeyEvent;
 
 /**
  * 
  * Test Cases for StudentDatabaseHandler
  * 
  * @author Junhyun
  *
  */
 public class AddSectionActivityTest extends
 		ActivityInstrumentationTestCase2<ViewSectionListActivity> {
 
 	private static final String NAMEKEYS = "N A M E ";
 	
 	private static final String NAME = "name";
 	
 	private static final String COURSEKEYS = "C O U R S E ";
 	
 	private static final String COURSE = "course";
 	
 	private int suffix = 0;
 	
 	SectionDbHandler dbHandler;
 	ViewSectionListActivity viewSectionListActivity;
 
 	public AddSectionActivityTest() {
 		super(ViewSectionListActivity.class);
 	}
 
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		setActivityInitialTouchMode(false);
 
 		viewSectionListActivity = getActivity();
 		dbHandler = new SectionDbHandler(viewSectionListActivity);
 	}
 
 	@Override
 	protected void tearDown() throws Exception {
 		dbHandler.closeSectionDB();
 		super.tearDown();
 	}
 
 	public void testPreConditions() {
 		assertTrue(dbHandler != null);
 	}
 
 	private void getUniqueSuffix() {
 		SQLiteDatabase db = dbHandler.getReadableDatabase();
 		int count;
 		do {
 			Cursor cursor = db.query("table_section", null,
 					"name =? AND course =?", new String[] {
 					NAME+suffix, COURSE+suffix }, null, null, null, null);
 			
 			count = cursor.getCount();
 			if(count != 0)
 				suffix++;
 		} while (count != 0);
 	}
 	
 	private int tryAddingGetCounts(String name, String course) {
 		sendKeys(KeyEvent.KEYCODE_TAB, KeyEvent.KEYCODE_ENTER);
 		sendKeys(name + course);
 		sendKeys(KeyEvent.KEYCODE_ENTER);
 		
 		SQLiteDatabase db = dbHandler.getReadableDatabase();
 		Cursor cursor = db.query("table_section", null,
 				"name =? AND course =?", new String[] {
 				NAME+suffix, COURSE+suffix }, null, null, null, null);
 
 		return cursor.getCount();
 	}
 	
 	public void testAddSectionFromUI() {
 		getUniqueSuffix();
 		
 		String NAME_FIELD = NAMEKEYS + suffix + " TAB ";
 		String COURSE_FIELD = COURSEKEYS + suffix + " TAB ";
 
 		int count = tryAddingGetCounts(NAME_FIELD, COURSE_FIELD);
 		
 		// If this fails, there are data to be cleaned on DB
 		assertEquals(count, 1);
 
 		SQLiteDatabase db = dbHandler.getReadableDatabase();
 		db.delete("table_section", "name =? AND course =?", new String[] {
 				NAME+suffix, COURSE+suffix });
 	}
 	
 	public void testAddSectionFromUIWithEmptyData() {
 		getUniqueSuffix();
 		
 		String NAME_FIELD = NAMEKEYS + suffix + " TAB ";
 		String COURSE_FIELD = COURSEKEYS + suffix + " TAB ";
 		
 		int count = tryAddingGetCounts("TAB ", "TAB ");
 		assertEquals(count, 0);
 		sendKeys(KeyEvent.KEYCODE_BACK);
		sendKeys("TAB TAB ");
 		
 		count = tryAddingGetCounts(NAME_FIELD, "TAB ");
 		assertEquals(count, 0);
 		sendKeys(KeyEvent.KEYCODE_BACK);
		sendKeys("TAB TAB ");
 		
 		count = tryAddingGetCounts("TAB ", COURSE_FIELD);
 		assertEquals(count, 0);
 		sendKeys(KeyEvent.KEYCODE_BACK);
		sendKeys(KeyEvent.KEYCODE_BACK);
 	}
 }
