 package com.huskysoft.eduki.test;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import android.content.Intent;
 import android.test.ActivityInstrumentationTestCase2;
 import android.view.View;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 
 import com.huskysoft.eduki.CoursesListActivity;
 import com.huskysoft.eduki.QuizzesResultsActivity;
 import com.huskysoft.eduki.QuizzesViewActivity;
 import com.huskysoft.eduki.data.Quiz;
 import com.jayway.android.robotium.solo.Solo;
 
 public class QuizzesViewTest extends ActivityInstrumentationTestCase2<QuizzesViewActivity> {
     
     private Solo solo;
     private static final int TIMEOUT = 10000;
     
     /**
      * Constructs the tests
      */
     public QuizzesViewTest() {
         super(QuizzesViewActivity.class);
     }
     
     /**
      * Initializes required variables
      */
     @Before
     public void setUp() throws Exception {
         solo = new Solo(getInstrumentation(), getActivity());
         super.setUp();
     }
     
     @Override
     public QuizzesViewActivity getActivity() {
         Intent i = new Intent();
         i.putExtra("quiz_title", "QUIZ_TITLE_EXAMPLE");
         i.putExtra("quiz_id", 1);
         i.putExtra("course_id", 1);
         setActivityIntent(i);
         return super.getActivity();
     }
     
     /**
      * Tests that the quiz appears correctly
      */
     @Test(timeout=TIMEOUT)
     public void testQuizAppears() {
         solo.assertCurrentActivity("Wrong activity", QuizzesViewActivity.class);
         solo.waitForView(solo.getView(com.huskysoft.eduki.R.id.quizScrollView));
         Quiz quiz = ((QuizzesViewActivity) solo.getCurrentActivity()).getQuiz();
         List<String> questions = ((QuizzesViewActivity) solo.getCurrentActivity()).getQuestions();
         assertNotSame("questions list is null", null, questions);
         assertEquals("QUIZ_TITLE_EXAMPLE", quiz.getTitle());        
     }
     
     /**
      * Tests that taking a test works correctly
      */
     @Test(timeout=TIMEOUT)
     public void testQuizTaking() {
         solo.assertCurrentActivity("Wrong activity", QuizzesViewActivity.class);
         solo.waitForView(solo.getView(com.huskysoft.eduki.R.id.quizScrollView));
         solo.sleep(2000); // Sleep to wait for the answers to populate
        List<RadioGroup> answersGroupList = ((QuizzesViewActivity) solo.getCurrentActivity()).getAnswerGroup();
         for (int i = 0; i < answersGroupList.size(); i++) {
             RadioGroup current_rg = answersGroupList.get(i);
             List<RadioButton> list_rb = getRadioButtons(current_rg);
             assertTrue(list_rb.size() != 0);
             solo.clickOnView(list_rb.get(0)); // Answer "A" for everything
         }
         solo.clickOnButton("SUBMIT");
         solo.assertCurrentActivity("Wrong activity", QuizzesResultsActivity.class);
     }
     
     /** 
      * Get the radio buttons in a radio group
      * 
      * @param rg Radio group the get the radio buttons from
      * @return List of the radio buttons in the given radio group
      */
     private List<RadioButton> getRadioButtons(RadioGroup rg) {
         List<RadioButton> buttonList = new ArrayList<RadioButton>();
         int count = rg.getChildCount();
         for (int i = 0; i < count; i++) {
             View o = rg.getChildAt(i);
             if (o instanceof RadioButton) {
                 buttonList.add((RadioButton) o);
             }
         }
         return buttonList;
     }
     
     /**
      * Test that the action bar click renavigates to the CoursesListActivity
      */
     @Test(timeout=TIMEOUT)
     public void testAllCoursesClick() {
         solo.assertCurrentActivity("Wrong activity", QuizzesViewActivity.class);
         solo.clickOnActionBarItem(com.huskysoft.eduki.R.id.action_courses);
         solo.assertCurrentActivity("Did not start the Course list Activity", CoursesListActivity.class);
     }
     
     @Override
     public void tearDown() throws Exception {
         solo.finishOpenedActivities();
         super.tearDown();
     }
 }
