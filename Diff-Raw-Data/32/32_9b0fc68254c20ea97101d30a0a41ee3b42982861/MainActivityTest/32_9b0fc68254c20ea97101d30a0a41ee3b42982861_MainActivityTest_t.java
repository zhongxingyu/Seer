 package epfl.sweng.test;
 import android.test.ActivityInstrumentationTestCase2;
 
 import com.jayway.android.robotium.solo.Solo;
 
 import epfl.sweng.editquestions.EditQuestionActivity;
 import epfl.sweng.entry.MainActivity;
 import epfl.sweng.servercomm.SwengHttpClientFactory;
 import epfl.sweng.showquestions.ShowQuestionsActivity;
 import epfl.sweng.test.mocking.MockHttpClient;
 import epfl.sweng.test.tools.TestingTricks;
 /**
  * First test case...
  */
 public class MainActivityTest extends
 		ActivityInstrumentationTestCase2<MainActivity> {
 	
 	private Solo solo;
 	
 	public MainActivityTest() {
 		super(MainActivity.class);
 	}
 	
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
         SwengHttpClientFactory.setInstance(new MockHttpClient());		
 		solo = new Solo(getInstrumentation(), getActivity());
 	}
 
 	/* Begin list of the different tests to be performed */
 
 	public void testMenu() {
		TestingTricks.authenticateMe(solo);
 		solo.assertCurrentActivity("Main menu is being displayed",
                 MainActivity.class);
 
 		solo.clickOnButton("Show a random question");
 
 		solo.assertCurrentActivity("Random Question is being displayed",
                 ShowQuestionsActivity.class);
 		
 		solo.goBack();
 		
 		
 		solo.clickOnButton("Submit quiz question");
 		solo.assertCurrentActivity("Edit Question Form is being displayed",
                 EditQuestionActivity.class);
 
 		
 		assertTrue(solo.searchText("Submit"));
 
 		
 	}
 
 
 	/* End list of the different tests to be performed */
 	
 	@Override
 	protected void tearDown() throws Exception {
 		solo.finishOpenedActivities();
 	}
 
 }
