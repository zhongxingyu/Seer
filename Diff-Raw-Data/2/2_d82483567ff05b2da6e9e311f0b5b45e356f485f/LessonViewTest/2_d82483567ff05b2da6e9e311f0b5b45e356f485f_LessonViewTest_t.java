 package com.huskysoft.eduki.test;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.huskysoft.eduki.LessonsViewActivity;
 import com.huskysoft.eduki.data.Lesson;
 import com.jayway.android.robotium.solo.Solo;
 
 import android.content.Intent;
 import android.test.ActivityInstrumentationTestCase2;
 
 public class LessonViewTest extends ActivityInstrumentationTestCase2<LessonsViewActivity> {
 
     private Solo solo;
     private static final int TIMEOUT = 10000;
     
     public LessonViewTest() {
         super(LessonsViewActivity.class);
     }
     
     @Before
     public void setUp() throws Exception {
         solo = new Solo(getInstrumentation(), getActivity());
     }
     
     @Override
     public LessonsViewActivity getActivity() {
         Intent i = new Intent();
         i.putExtra("lesson_title", "test_lesson_title");
         i.putExtra("lesson_id", 1);
         i.putExtra("lesson_body", "test_lesson_body");
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
         solo.waitForView(solo.getView(com.huskysoft.eduki.R.id.lessonViewLayoutText));
         Lesson lesson = ((LessonsViewActivity) solo.getCurrentActivity()).getLesson();
         assertNotSame(null, lesson);
        assertFalse(lesson.getBody().equals(""));
         assertEquals("test_lesson_title", lesson.getTitle());
     }
 }
