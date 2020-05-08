 package epfl.sweng.test;
 
 import android.test.ActivityInstrumentationTestCase2;
 import android.view.View;
 import android.widget.ListView;
 import com.jayway.android.robotium.solo.Solo;
 import epfl.sweng.quizquestions.QuizQuestion;
 import epfl.sweng.servercomm.SwengHttpClientFactory;
 import epfl.sweng.showquestions.ShowQuestionsActivity;
 import epfl.sweng.tasks.LoadRandomQuestion;
 import epfl.sweng.tasks.SubmitQuestionVerdict;
 import epfl.sweng.tasks.interfaces.IQuizQuestionReceivedCallback;
 import epfl.sweng.tasks.interfaces.IQuizQuestionVerdictSubmittedCallback;
 import epfl.sweng.test.mocking.MockHttpClient;
 import epfl.sweng.test.tools.TestingTricks;
 /**
  * First test case...
  */
 public class ShowQuestionsActivityTest extends
 		ActivityInstrumentationTestCase2<ShowQuestionsActivity> {
 	
 	private Solo solo;
 	private static final int NUMBER_OF_QUESTIONS = 1;
 	private static final int SLEEP_LISTCHECK = 2000;
 	private static final int SLEEP_CHARACTERSCHECK = 500;
 	
 	public ShowQuestionsActivityTest() {
 		super(ShowQuestionsActivity.class);
 	}
 	
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
         SwengHttpClientFactory.setInstance(new MockHttpClient());
 		solo = new Solo(getInstrumentation(), getActivity());
 	}
 
 	/* Begin list of the different tests to be performed */
 
 	public void testDisplayQuestionMocked() {
 		TestingTricks.authenticateMe(solo);
 		if (solo.searchText("Show a random question")) {
 			solo.clickOnButton("Show a random question");
 		}
 		solo.assertCurrentActivity("A question is being displayed",
                 ShowQuestionsActivity.class);
 		ListView l = solo.getCurrentListViews().get(0);
 		assertNotNull("No list views!", l);
 		solo.sleep(SLEEP_LISTCHECK);
 		assertTrue("No items in list view!", l.getChildCount()>0);
 		
 		for (int i = 0; i < NUMBER_OF_QUESTIONS; i++) {
 			solo.sleep(SLEEP_CHARACTERSCHECK);
 			for (int childIndex = 0; childIndex < l.getAdapter().getCount(); childIndex++) {
 				solo.sleep(SLEEP_CHARACTERSCHECK);
 				System.out.println("Number of answers: " + l.getAdapter().getCount());
 				System.out.println("Index of current answer: " + childIndex);
 				
 				View childView = l.getChildAt(childIndex);
 				if (childView != null) {
 					solo.clickOnView(childView);
 					System.out.println("Index of answer having been clicked: " + childIndex);
 				}
 			}
 			
 			assertTrue(solo.searchText("\u2718") || solo.searchText("\u2714"));
 			
 			solo.clickOnButton("Next question");
 			solo.sleep(SLEEP_CHARACTERSCHECK);
 			assertFalse(solo.searchText("\u2718") && solo.searchText("\u2714"));
 		}
 
 	}
 		
 	public void testNoNetwork() {
 
 		solo.assertCurrentActivity("A question is being displayed",
                 ShowQuestionsActivity.class);
 		
 		final ShowQuestionsActivity activity = getActivity();
 		
 		IQuizQuestionReceivedCallback callback = new IQuizQuestionReceivedCallback() {
 
 			@Override
 			public void onQuestionSuccess(QuizQuestion question) {
 				activity.displayQuestion(question);
 			}
 
 			@Override
 			public void onRatingSuccess(QuizQuestion question) {
 				activity.updateQuestionRating(question);
 			}
 
 			@Override
 			public void onQuestionError() {
 				activity.displayError();
 			}
 
 			@Override
 			public void onRatingError() {
 				activity.displayUpdateRatingError();
 			}
         };
 
     	new LoadRandomQuestion(callback).execute("http://www.google.com");
     	assertTrue(solo.searchText("There was an error retrieving the question"));
     	new LoadRandomQuestion(callback).execute("http://0.0.0.0");
     	assertTrue(solo.searchText("There was an error retrieving the question"));
     	
     	
     	QuizQuestion question = new QuizQuestion();
     	
     	question.setId(-1);
     	
     	new SubmitQuestionVerdict(new IQuizQuestionVerdictSubmittedCallback() {
 			
 			@Override
 			public void onSubmitSuccess(QuizQuestion question) {
 				
 			}
 			
 			@Override
 			public void onReloadedSuccess(QuizQuestion question) {
 			}
 
 			@Override
 			public void onSubmitError() {
 			}
 
 			@Override
 			public void onReloadedError() {
 			}
		}, question, question.getVerdict()).execute();
     	
 	}
 	
 	/* End list of the different tests to be performed */
 	
 	@Override
 	protected void tearDown() throws Exception {
 		solo.finishOpenedActivities();
 
         SwengHttpClientFactory.setInstance(null);
 	}
 
 }
