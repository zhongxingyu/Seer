 package com.huskysoft.eduki.test;
 
 import android.test.ActivityInstrumentationTestCase2;
 
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.huskysoft.eduki.CoursesListActivity;
 import com.huskysoft.eduki.LessonsListActivity;
 import com.huskysoft.eduki.data.Course;
 import com.jayway.android.robotium.solo.Solo;
 
public class CoursesListActivityTest extends ActivityInstrumentationTestCase2<CoursesListActivity> {
 
     private Solo solo;
     private static final int TIMEOUT = 10000;
     
     public CoursesListActivityTest() {
         super(CoursesListActivity.class);
     }
     
     @Before
     public void setUp() throws Exception {
         solo = new Solo(getInstrumentation(), getActivity());
     }
     
     /**
      * Assert the list of test courses appear properly
      */
     @Test(timeout=TIMEOUT)
     public void testCoursesAppear() {
         solo.assertCurrentActivity("Wrong activity", CoursesListActivity.class);
         solo.waitForView(solo.getView(com.huskysoft.eduki.R.id.courseListView));
         List<Course> courseList = ((CoursesListActivity) solo.getCurrentActivity()).getCourseList();
         assertNotSame(courseList.size(), 0);
         assertTrue(solo.searchText(courseList.get(0).toString()));
         assertTrue(solo.searchText(courseList.get(courseList.size() / 2).toString()));
     }
     
     /**
      * Assert the list of test courses appear properly
      */
     @Test(timeout=TIMEOUT)
     public void testNewActivityStarted() {
         solo.assertCurrentActivity("Wrong activity", CoursesListActivity.class);
         solo.waitForView(solo.getView(com.huskysoft.eduki.R.id.courseListView));
         List<Course> courseList = ((CoursesListActivity) solo.getCurrentActivity()).getCourseList();
         assertNotSame(courseList.size(), 0);
         solo.clickOnText(courseList.get(0).toString());
         solo.assertCurrentActivity("Wrong activity", LessonsListActivity.class);
     }
 }
 
