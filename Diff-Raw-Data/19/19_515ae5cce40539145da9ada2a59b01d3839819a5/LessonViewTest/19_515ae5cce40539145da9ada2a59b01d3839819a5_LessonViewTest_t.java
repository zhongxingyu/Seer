 package com.huskysoft.eduki.test;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.huskysoft.eduki.LessonsViewActivity;
import com.huskysoft.eduki.QuizzesResultsActivity;
 import com.huskysoft.eduki.data.Lesson;
 import com.jayway.android.robotium.solo.Solo;
 
 import android.content.Intent;
 import android.test.ActivityInstrumentationTestCase2;
 
 /**
  * 
  * @author Cody Thomas LessonViewTest tests the LessonView Activity
  *
  */
 public class LessonViewTest extends ActivityInstrumentationTestCase2<LessonsViewActivity> {
 
     private Solo solo;
     private static final int TIMEOUT = 10000;
     
     /**
      * Constructs the tests
      */
     public LessonViewTest() {
         super(LessonsViewActivity.class);
     }
     
     /**
      * Initializes required variables
      */
     @Before
     public void setUp() throws Exception {
         solo = new Solo(getInstrumentation(), getActivity());
     }
     
     @Override
     public LessonsViewActivity getActivity() {
         Intent i = new Intent();
         i.putExtra("lesson_title", "TEST_LESSON_TITLE");
         i.putExtra("lesson_id", 1);
         i.putExtra("lesson_body", "TEST_LESSON_BODY");
         i.putExtra("course_id", 1);
         setActivityIntent(i);
         return super.getActivity();
     }
     
     /**
      * Assert the a single lesson is displayed correctly
      */
     @Test(timeout=TIMEOUT)
     public void testLessonAppears() {
         solo.assertCurrentActivity("Wrong activity", LessonsViewActivity.class);
        solo.waitForView(solo.getView(com.huskysoft.eduki.R.id.lessonScrollView));
         Lesson lesson = ((LessonsViewActivity) solo.getCurrentActivity()).getLesson();
         assertNotSame(null, lesson);
         assertFalse(lesson.getBody().equals(""));
         assertEquals("TEST_LESSON_TITLE", lesson.getTitle());
     }
 }
