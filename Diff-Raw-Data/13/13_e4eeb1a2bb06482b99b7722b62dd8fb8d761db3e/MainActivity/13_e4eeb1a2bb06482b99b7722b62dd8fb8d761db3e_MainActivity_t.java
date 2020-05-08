 
 package com.huskysoft.eduki;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.huskysoft.eduki.data.Course;
 import com.huskysoft.eduki.data.CourseQuery;
 import com.huskysoft.eduki.data.Enrollment;
 import com.huskysoft.eduki.data.UserQuery;
 import com.huskysoft.eduki.data.ViewPopulator;
 
 /**
  * @author Cody Thomas MainActivity is the first activity to be loaded when the
  *         application begins. It gives a choice of options for which the user
  *         can continue, such as a button for the list of courses
  */
 
 public class MainActivity extends Activity implements TaskComplete {
 
     private static final int ENROLLMENTS = 0;
     private static final int COURSES = 1;
     private static final int ENROLLED_COURSE = 2;
 
     private Integer tasksCompleted;
     private Integer enrollmentsFound;
     private LinearLayout mainLayout;
 
     private List<Course> enrolledCourses;
     private List<Course> myCourses;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
         boolean authenticated = prefs.getBoolean("authenticated", false);
         if (!authenticated) {
             startLogin();
         } else {
             setContentView(R.layout.loading_screen);
             mainLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.activity_main, null);
             tasksCompleted = 0;
             enrolledCourses = new ArrayList<Course>();
             String name = prefs.getString("user_name", null);
             int userId = prefs.getInt("user_id", 2);
             String dashboardName;
             if (name == null) {
                 dashboardName = "MY";
             } else {
                 if (name.toLowerCase().charAt(name.length() - 1) == 's') {
                     dashboardName = name.toUpperCase() + "'";
                 } else {
                     dashboardName = name.toUpperCase() + "'S";
                 }
             }
             dashboardName = dashboardName + " DASHBOARD";
             ((TextView) mainLayout.findViewById(R.id.title)).setText(dashboardName);
             CourseQuery.getAllUserCourses(this, COURSES, userId);
             UserQuery.getEnrollments(this, userId, ENROLLMENTS);
 
             // Set enrolled carousel title
             TextView lessonTitle = (TextView) mainLayout.findViewById(R.id.enrollmentsTitle);
             lessonTitle.setText(R.string.enrollment);
 
             // Set mycourses carousel title
             TextView quizzesTitle = (TextView) mainLayout.findViewById(R.id.myCoursesTitle);
             quizzesTitle.setText(R.string.myCourses);
         }
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         menu.findItem(R.id.action_dash).setVisible(false);
         return true;
     }
 
     public void startLogin() {
         Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
         startActivity(intent);
        finish();
     }
 
     @Override
     public void taskComplete(String data, int id) {
         boolean complete = false;
         if (id == ENROLLMENTS) {
             List<Enrollment> enrollments = UserQuery.parseEnrollments(data);
             enrollmentsFound = enrollments.size();
             if (enrollmentsFound == 0) {
                 complete = true;
                 LinearLayout layout = (LinearLayout) mainLayout
                         .findViewById(R.id.enrollment_rowview);
                 ViewPopulator.populateCarouselEmpty(layout, this, "NO COURSES ENROLLED BY YOU");
             }
             for (Enrollment e : enrollments) {
                 int course_id = e.getCourseId();
                 CourseQuery.getCourse(this, ENROLLED_COURSE, course_id);
             }
         } else if (id == COURSES) {
             complete = true;
             myCourses = CourseQuery.parseCourseList(data);
             LinearLayout layout = (LinearLayout) mainLayout.findViewById(R.id.mycourses_rowview);
             View.OnClickListener v = new View.OnClickListener() {
                 @Override
                 public void onClick(View v) {
                     myCourseSelected(v.getId());
                 }
             };
             ViewPopulator.populateCarousel(myCourses, layout, R.layout.red_carousel_item, v, this,
                     "NO COURSES CREATED BY YOU");
 
         } else if (id == ENROLLED_COURSE) {
             Course c = CourseQuery.parseCourse(data);
             enrolledCourses.add(c);
             synchronized (enrollmentsFound) {
                 enrollmentsFound--;
                 if (enrollmentsFound == 0) {
                     complete = true;
 
                     LinearLayout layout = (LinearLayout) mainLayout
                             .findViewById(R.id.enrollment_rowview);
                     View.OnClickListener v = new View.OnClickListener() {
                         @Override
                         public void onClick(View v) {
                             enrollmentSelected(v.getId());
                         }
                     };
                     ViewPopulator.populateCarousel(enrolledCourses, layout,
                             R.layout.blue_carousel_item, v, this, "NO COURSES ENROLLED BY YOU");
                 }
             }
 
         }
         if (complete) {
             synchronized (tasksCompleted) {
                 tasksCompleted++;
                 if (tasksCompleted == 2) {
                     setContentView(mainLayout);
                 }
             }
         }
     }
 
     /**
      * Will use the position parameter and find that course in the list of
      * courses, calling the lessons list activity.
      * 
      * @param position the position in the list of the button pressed
      */
     private void enrollmentSelected(int position) {
         Course chosen = enrolledCourses.get(position);
         Intent i = new Intent(this, CourseActivity.class);
         i.putExtra("course_title", chosen.getTitle());
         i.putExtra("course_id", chosen.getId());
         startActivity(i);
     }
 
     /**
      * Will use the position parameter and find that course in the list of
      * courses, calling the lessons list activity.
      * 
      * @param position the position in the list of the button pressed
      */
     private void myCourseSelected(int position) {
         Course chosen = myCourses.get(position);
         Intent i = new Intent(this, CourseActivity.class);
         i.putExtra("course_title", chosen.getTitle());
         i.putExtra("course_id", chosen.getId());
         startActivity(i);
     }
 
     /**
      * Logs the current user out
      * 
      * @param v
      */
     public void logout(View v) {
         PreferenceManager.getDefaultSharedPreferences(this).edit().clear().commit();
         startLogin();
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.action_courses:
                 // app icon in action bar clicked; go home
                 Intent intent = new Intent(this, CoursesListActivity.class);
                 intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                 startActivity(intent);
                 return true;
             case R.id.action_dash:
                 Intent intentMain = new Intent(this, MainActivity.class);
                 intentMain.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                 startActivity(intentMain);
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 }
