 package epfl.sweng.test;
 
 import android.test.ActivityInstrumentationTestCase2;
 import android.view.View;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.ListView;
 
 import com.jayway.android.robotium.solo.Solo;
 
 import epfl.sweng.editquestions.EditQuestionActivity;
 import epfl.sweng.entry.MainActivity;
 import epfl.sweng.servercomm.SwengHttpClientFactory;
 import epfl.sweng.showquestions.ShowQuestionsActivity;
 import epfl.sweng.test.mocking.MockHttpClient;
 import epfl.sweng.test.tools.TestingTricks;
 
 /**
  * Test class for the rating system 
  */
 public class CachingTest extends ActivityInstrumentationTestCase2<MainActivity> {
 	
 	private Solo solo;
 	private final static int WAIT_TIME = 4000;
 	private final static String TEST_QUESTION = "Test question...";
 	private final static String TEST_FALSEANSWER = "False Answer";
 	private final static String TEST_RIGHTANSWER = "Right Answer";
 	private final static String TEST_TAGS = "test";
 	private static final int NUMBER_OF_QUESTIONS = 1;
 	private static final int SLEEP_LISTCHECK = 2000;
 	private static final int SLEEP_CHARACTERSCHECK = 500;
 	
 	public CachingTest() {
 		super(MainActivity.class);
 	}
 	
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
         SwengHttpClientFactory.setInstance(new MockHttpClient());
 		solo = new Solo(getInstrumentation(), getActivity());
 	}
 	
 	public void testSwitchMode() {
 		TestingTricks.authenticateMe(solo);
 		solo.goBackToActivity("MainActivity");
 		CheckBox chkBox = (CheckBox) solo.getView(epfl.sweng.R.id.main_checkbox_offline);
 		solo.clickOnView(chkBox);
 		solo.waitForText((String) getActivity().getResources().getText(epfl.sweng.R.string.you_are_offline));
 		solo.clickOnView(chkBox);
 		solo.waitForText((String) getActivity().getResources().getText(epfl.sweng.R.string.you_are_online));
 	}
 	
 	public void testEditAndShowQuestionInCacheAndGoBackOnline() {
 		TestingTricks.authenticateMe(solo);
 		CheckBox chkBox = (CheckBox) solo.getView(epfl.sweng.R.id.main_checkbox_offline);
 		Boolean isChecked = chkBox.isChecked();
 		if (!isChecked) {
 			solo.clickOnView(chkBox);
 		}
 		if (solo.searchText("Submit quiz question")) {
 			solo.clickOnButton("Submit quiz question");
 		}
 		
 		solo.assertCurrentActivity("Edit Question Form is being displayed",
                 EditQuestionActivity.class);
 
 		assertTrue(((EditQuestionActivity) solo.getCurrentActivity()).auditErrors()==0);
 		solo.clickOnButton("\\+");
 		assertTrue(((EditQuestionActivity) solo.getCurrentActivity()).auditErrors()==0);
 		solo.clickOnButton("\\-");
 		assertTrue(((EditQuestionActivity) solo.getCurrentActivity()).auditErrors()==0);
 		solo.clickOnButton("\u2718");
 		assertTrue(((EditQuestionActivity) solo.getCurrentActivity()).auditErrors()==0);
 		assertTrue(solo.searchText("\u2714"));
 		solo.clickOnButton("\u2714");
 		assertTrue(((EditQuestionActivity) solo.getCurrentActivity()).auditErrors()==0);
     	assertTrue(solo.waitForText("One answer should be marked as correct"));		
 		assertTrue(((EditQuestionActivity) solo.getCurrentActivity()).auditErrors()==0);
     	solo.sleep(WAIT_TIME);
 		
 		solo.clickOnButton("\\+");
     	boolean rightAnswerEntered = false;
 		for (EditText et: solo.getCurrentEditTexts()) {
 			if (et.getTag().toString() 
 				== getActivity().getResources().getText(epfl.sweng.R.string.edit_question_hint)) {
 				solo.enterText(et, TEST_QUESTION);
 			} else if (et.getTag().toString() 
 				== getActivity().getResources().getText(epfl.sweng.R.string.edit_tags_hint)) {
 				solo.enterText(et, TEST_TAGS);
 			} else if (et.getTag().toString() 
 				== getActivity().getResources().getText(epfl.sweng.R.string.edit_answer_hint)) {
 				
 				if (rightAnswerEntered) {
 					solo.enterText(et, TEST_FALSEANSWER);
 				} else {
 					solo.enterText(et, TEST_RIGHTANSWER);
 					rightAnswerEntered = true;
 				}
 			}
 			assertTrue(((EditQuestionActivity) solo.getCurrentActivity()).auditErrors()==0);
 		}
 		assertTrue(((EditQuestionActivity) solo.getCurrentActivity()).auditErrors()==0);
 		solo.clickOnButton("\u2718");
 		assertTrue(((EditQuestionActivity) solo.getCurrentActivity()).auditErrors()==0);
 		solo.sleep(WAIT_TIME);
 		solo.clickOnButton("Submit");
 		assertTrue(solo.waitForText("\u2714 Question successfully submitted"));
 		
 		
 		
 		solo.goBackToActivity("MainActivity");
 		
 		
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
 		
 
 		solo.goBackToActivity("MainActivity");
 		
 		assert !chkBox.isChecked();
 		solo.clickOnView(chkBox);
 		assert chkBox.isChecked();
 	}
 }
 
 
 
 
