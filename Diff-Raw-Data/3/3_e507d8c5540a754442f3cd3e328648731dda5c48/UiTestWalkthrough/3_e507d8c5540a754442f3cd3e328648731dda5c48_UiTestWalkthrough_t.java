 package com.huskysoft.eduki.test;
 
 import android.test.ActivityInstrumentationTestCase2;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.TextView;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.huskysoft.eduki.CourseActivity;
 import com.huskysoft.eduki.CoursesListActivity;
 import com.huskysoft.eduki.LoginActivity;
 import com.huskysoft.eduki.MainActivity;
 import com.huskysoft.eduki.QuizzesResultsActivity;
 import com.huskysoft.eduki.QuizzesViewActivity;
 import com.huskysoft.eduki.data.Course;
 import com.huskysoft.eduki.data.Lesson;
 import com.huskysoft.eduki.data.Quiz;
 import com.jayway.android.robotium.solo.Solo;
 
 public class UiTestWalkthrough extends ActivityInstrumentationTestCase2<LoginActivity> {
 
     private Solo solo;
     
     /**
      * Construct the tests
      */
     public UiTestWalkthrough() {
         super(LoginActivity.class);
     }
 
     @Override
     public void tearDown() throws Exception {
         getActivity().logout();
         solo.finishOpenedActivities();
         super.tearDown();
     }
     
     /**
      * Initializes required variables
      */
     @Before
     public void setUp() throws Exception {
         solo = new Solo(getInstrumentation(), getActivity());
     }
     
     @Test
     public void testWalkthroughLoginToDashboardToCoursesToCourseToLesson() {
         //Login first
         EditText user = solo.getEditText(0);
         EditText pass = solo.getEditText(1);
         solo.typeText(user, "test@eduki.com");
         solo.typeText(pass, "password");
         solo.clickOnButton(solo.getString(com.huskysoft.eduki.R.string.login));
         // Need to sleep to allow the activity to finish
         solo.sleep(8000);
         solo.assertCurrentActivity("Wrong activity", MainActivity.class);
         
         //Click on the all courses button
         solo.clickOnActionBarItem(com.huskysoft.eduki.R.id.action_courses);
         solo.sleep(1000);
         solo.assertCurrentActivity("Did not start the Course list Activity", CoursesListActivity.class);
         
         //Wait for the courses list to appear
         solo.waitForView(solo.getView(com.huskysoft.eduki.R.id.courseListView));
         solo.sleep(1000);
         List<Course> courseList = ((CoursesListActivity) solo.getCurrentActivity()).getCourseList();
         assertNotSame(courseList.size(), 0);
         solo.clickOnText(courseList.get(0).toString());
         solo.assertCurrentActivity("Wrong activity", CourseActivity.class);
         
         //Wait for the course page to appear
         solo.waitForView(solo.getView(com.huskysoft.eduki.R.id.course_activity));
         solo.sleep(1000);
         
         //Click a lesson, wait for it to appear
         List<Lesson> lessonList = ((CourseActivity) solo.getCurrentActivity()).getLessonList();
         assertNotSame(lessonList.size(), 0);
         solo.clickOnText(lessonList.get(0).toString());
         
         solo.waitForView(solo.getView(com.huskysoft.eduki.R.id.lessonViewLayoutText));
         solo.sleep(1000);
         String content = ((TextView) solo.getCurrentActivity().findViewById(com.huskysoft.eduki.R.id.lessonViewLayoutText)).getText().toString();
         assertFalse(content.equals(""));
     }
     
     @Test
     public void testWalkthroughLoginToCoursesListToCourseToQuiz() {
         // Need to sleep to allow the activity to finish
         solo.clickOnActionBarItem(com.huskysoft.eduki.R.id.action_courses);
         solo.sleep(1000);
         solo.assertCurrentActivity("Did not start the Course list Activity", CoursesListActivity.class);
         
         //Wait for the courses list to appear
         solo.waitForView(solo.getView(com.huskysoft.eduki.R.id.courseListView));
         solo.sleep(1000);
         List<Course> courseList = ((CoursesListActivity) solo.getCurrentActivity()).getCourseList();
         assertNotSame(courseList.size(), 0);
         solo.clickOnText(courseList.get(0).toString());
         solo.assertCurrentActivity("Wrong activity", CourseActivity.class);
         
         //Wait for the course page to appear
         solo.waitForView(solo.getView(com.huskysoft.eduki.R.id.course_activity));
         solo.sleep(1000);
         
         //Click a lesson, wait for it to appear
         List<Quiz> quizList = ((CourseActivity) solo.getCurrentActivity()).getQuizList();
         assertNotSame(quizList.size(), 0);
         solo.clickOnText(quizList.get(0).toString());
         
         solo.waitForView(solo.getView(com.huskysoft.eduki.R.id.quizScrollView));
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
 }
