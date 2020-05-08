 package cs428.mobiletaandroidapp.section;
 
 import android.test.ActivityInstrumentationTestCase2;
 import android.view.KeyEvent;
 import android.widget.ListView;
 import cs428.mobiletaandroidapp.R;
 
 //Test cases to be added:
 	// 1) When deleting, try un-checking.
 	// 2) When deleting, try selecting all.
 	// 3) click the item.
 	// 4) Check if it actually selects the desired one.
 /**
  * 
  * Test Cases for ViewStudentListActivity
  * 
  * @author Junhyun Park
  *
  */
 public class ViewSectionListActivityTest extends
 		ActivityInstrumentationTestCase2<ViewSectionListActivity> {
 
 	private static final int TEST_SECTIONS_NUM = 10;
 	
 	private static final int TEST_POSITION = 5;
 
 	private ViewSectionListActivity viewSectionListActivity;
 	
 	private SectionDbHandler dbHandler;
 	
 	private int[] sectionId;
 	
 	private int originalSectionCount;
 
 	public ViewSectionListActivityTest() {
 		super(ViewSectionListActivity.class);
 	}
 
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 
 		setActivityInitialTouchMode(false);
 
 		viewSectionListActivity = getActivity();
 
 		dbHandler = new SectionDbHandler(viewSectionListActivity);
 		sectionId = new int[TEST_SECTIONS_NUM];
 		originalSectionCount = dbHandler.getAllSections().size();
 
 		for (int i = 0; i < TEST_SECTIONS_NUM; i++) {
 			Section section = new Section("TestSection" + i, "TestCourse" + i);
 			sectionId[i] = dbHandler.insertSection(section);
 		}
 	}
 
 	@Override
 	protected void tearDown() throws Exception {
 		for (int i = 0; i < TEST_SECTIONS_NUM; i++)
 			dbHandler.deleteSection(sectionId[i]);
 
 		dbHandler.closeSectionDB();
 		super.tearDown();
 	}
 
 	public void testNavigateListUI() {
		sendKeys(KeyEvent.KEYCODE_TAB, KeyEvent.KEYCODE_ENTER);
		sendKeys(KeyEvent.KEYCODE_BACK);
 		
 		for (int i = 1; i <= TEST_SECTIONS_NUM + originalSectionCount; i++) {
 			sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
 		} // navigate down
 
 		for (int i = 1; i <= TEST_SECTIONS_NUM; i++) {
 			sendKeys(KeyEvent.KEYCODE_DPAD_UP);
 		} // navigate up
 
 		for (int i = 1; i <= TEST_POSITION; i++) {
 			sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
 		} // navigate to desired position
 		
 		sendKeys(KeyEvent.KEYCODE_DPAD_CENTER);
 		sendKeys(KeyEvent.KEYCODE_BACK);
 
 		//ListView listView = (ListView) viewSectionListActivity.findViewById(R.id.listSection);
 		//int expectedPos = listView.get;
 		//int testPos = TEST_POSITION + originalSectionCount - 1; 
 		//assertEquals("mPos: " + expectedPos + "TI: " + testPos, expectedPos, testPos);
 	}
 	
 	public void testDeleteFromListUI() {
		sendKeys(KeyEvent.KEYCODE_TAB, KeyEvent.KEYCODE_ENTER);
		sendKeys(KeyEvent.KEYCODE_BACK);
 		
 		sendKeys("TAB ");
 		sendKeys(KeyEvent.KEYCODE_ENTER);
 		sendKeys("TAB TAB TAB ");
 		
 		for (int i = 0; i < originalSectionCount; i++) {
 			sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
 		} 
 		
 		for (int i = 1; i <= TEST_SECTIONS_NUM; i++) {
 			sendKeys(KeyEvent.KEYCODE_ENTER);
 			sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
 		} 
 		
 		sendKeys("TAB TAB TAB ");
 		sendKeys(KeyEvent.KEYCODE_ENTER);
 		
 		ListView listView = (ListView) viewSectionListActivity.findViewById(R.id.listSection);
 		
 		assertEquals(originalSectionCount, listView.getCount());
 	}
 	
 }
