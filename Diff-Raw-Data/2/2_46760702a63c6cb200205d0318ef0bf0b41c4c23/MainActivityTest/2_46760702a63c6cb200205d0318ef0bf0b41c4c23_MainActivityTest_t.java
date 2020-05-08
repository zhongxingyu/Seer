 package epfl.sweng.test;
 import android.test.ActivityInstrumentationTestCase2;
 
 import com.jayway.android.robotium.solo.Solo;
 
 import epfl.sweng.editquestions.EditQuestionActivity;
 import epfl.sweng.entry.MainActivity;
 
 /**
  * First test case...
  */
 public class MainActivityTest extends
 		ActivityInstrumentationTestCase2<MainActivity> {
 	
 	private Solo solo;
	private final static int WAIT_TIME = 2000;
 	
 	public MainActivityTest() {
 		super(MainActivity.class);
 	}
 	
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		solo = new Solo(getInstrumentation(), getActivity());
 	}
 
 	/* Begin list of the different tests to be performed */
 
 	public void testEditQuestion() {
 		solo.assertCurrentActivity("Main menu is being displayed",
                 MainActivity.class);
 
 		solo.clickOnButton("Submit quiz question");
 		
 		solo.assertCurrentActivity("Edit Question Form is being displayed",
                 EditQuestionActivity.class);
 		assertTrue(solo.searchText("Submit"));
 		solo.clickOnButton("\\+");
 		
 		solo.clickOnButton("Submit");
 		
 		solo.sleep(WAIT_TIME);
 	}
 
 
 	/* End list of the different tests to be performed */
 	
 	@Override
 	protected void tearDown() throws Exception {
 		solo.finishOpenedActivities();
 	}
 
 }
