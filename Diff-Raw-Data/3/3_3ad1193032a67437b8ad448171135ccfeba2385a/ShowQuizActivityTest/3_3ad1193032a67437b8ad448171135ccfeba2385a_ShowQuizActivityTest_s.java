 package epfl.sweng.test;
 
 import android.content.Intent;
 import android.test.ActivityInstrumentationTestCase2;
 import com.jayway.android.robotium.solo.Solo;
 import epfl.sweng.servercomm.SwengHttpClientFactory;
 import epfl.sweng.quizzes.ShowQuizActivity;
 import epfl.sweng.test.mocking.MockHttpClient;
 import epfl.sweng.test.tools.TestingTricks;
 /**
  * First test case...
  */
 public class ShowQuizActivityTest extends
 		ActivityInstrumentationTestCase2<ShowQuizActivity> {
 	
 	private Solo solo;
 	
 	private static final double TEST_SCORE = 13.58;
 
 	private static final int QUIZ_ID = 125;
 	
 	public ShowQuizActivityTest() {
 		super(ShowQuizActivity.class);
 
 	}
 	
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		Intent intent = new Intent(getInstrumentation().getContext(), ShowQuizActivity.class);
 		intent.putExtra("id", QUIZ_ID);
 		setActivityIntent(intent);
 		SwengHttpClientFactory.setInstance(new MockHttpClient());
 		solo = new Solo(getInstrumentation(), getActivity());
 	}
 
 
 	public void testAlertDialog() {
 		TestingTricks.authenticateMe(solo);
 		solo.assertCurrentActivity("A quiz is being displayed",
 				ShowQuizActivity.class);
 	    getActivity().runOnUiThread(new Runnable() {
 	        public void run() {
 	        	getActivity().displayScoreAlertDialog(TEST_SCORE);
 	        }
 	    });
 		assertTrue("Could not find the dialog!", solo.searchText("13.58"));
 		solo.clickOnText("OK");
 	}
 	
 	public void testNavigation() {
 		TestingTricks.authenticateMe(solo);
 		solo.assertCurrentActivity("A quiz is being displayed",
 				ShowQuizActivity.class);
 		
 		
 		solo.clickOnText("Next question");
 
 		assertTrue("Wrong question", solo.searchText("How much is 1 \\+ 1 \\?"));
 		assertTrue("Answer not found", solo.searchText("It all depends on the semantics of the '\\+' operator"));
 
 		solo.clickOnText("Next question");
 
 		assertTrue("Wrong question", solo.searchText("How much is 2 \\+ 2 \\?"));
 		
 		solo.clickOnText("Previous question");
 		assertTrue("Wrong question", solo.searchText("How much is 1 \\+ 1 \\?"));
 		
 		solo.clickOnText("It all depends on the semantics of the '\\+' operator");
 		assertTrue("Question marked", solo.searchText("\u2724"));
 		
 
 		solo.clickOnText("Previous question");
 		solo.clickOnText("Next question");
 
		assertTrue("Right Question still marked", solo.searchText("It all depends on the semantics of the '\\+' operator \u2724"));
 		
 		solo.clickOnText("Hand in quiz");
 		assertTrue("Could not find the dialog!", solo.searchText("13.58"));
 		solo.clickOnText("OK");
 	}
 	
 	
 	/* End list of the different tests to be performed */
 	
 	@Override
 	protected void tearDown() throws Exception {
 		solo.finishOpenedActivities();
 
         SwengHttpClientFactory.setInstance(null);
 	}
 
 }
