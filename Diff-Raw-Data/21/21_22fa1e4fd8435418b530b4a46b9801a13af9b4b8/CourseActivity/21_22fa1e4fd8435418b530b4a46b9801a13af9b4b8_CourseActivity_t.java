 
 package com.huskysoft.eduki;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 import java.util.Collections;
 import java.util.List;
 
 import com.huskysoft.eduki.data.Course;
 import com.huskysoft.eduki.data.Lesson;
 import com.huskysoft.eduki.data.LessonQuery;
 import com.huskysoft.eduki.data.Quiz;
 import com.huskysoft.eduki.data.QuizQuery;
 import com.huskysoft.eduki.data.ViewPopulator;
 
 public class CourseActivity extends Activity implements TaskComplete {
 
     private static final int LESSON_ID = 0;
     private static final int QUIZZES_ID = 1;
     
     private Course course;
     private List<Lesson> lessonList;
     private List<Quiz> quizList;
     private LinearLayout mainLayout;
     private Integer taskCompleteCount;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         taskCompleteCount = 0;
         Bundle extras = getIntent().getExtras();
         if (extras != null) {
             String course_title = extras.getString("course_title");
             int course_id = extras.getInt("course_id");
             Log.w("Eduki", "Eduki: Querying lessons");
             course = new Course(course_id, course_title);
             LessonQuery.getAllLessons(this, course_id, LESSON_ID);
             Log.w("Eduki", "Eduki: Querying quiz");
             QuizQuery.getAllQuizzes(this, course_id, QUIZZES_ID);
         }
         mainLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.activity_course, null);
         
         // Set Course title
         TextView coursesTitle = (TextView) mainLayout.findViewById(R.id.title);
         coursesTitle.setText(course.getTitle());
         
         // Set Lessons carousel title
         LinearLayout lessonsLayout = (LinearLayout) mainLayout.findViewById(R.id.lessonsCarouselTitle);
         TextView lessonTitle = (TextView) lessonsLayout.findViewById(R.id.subtitle);
         lessonTitle.setText(R.string.lessonsTitle);
         
         // Set quizzes carousel title
         LinearLayout quizzesLayout = (LinearLayout) mainLayout.findViewById(R.id.quizzesCarouselTitle);
         TextView quizzesTitle = (TextView) quizzesLayout.findViewById(R.id.subtitle);
         quizzesTitle.setText(R.string.quizzesTitle);
         
         setContentView(R.layout.loading_screen);
     }
 
     @Override
     public void taskComplete(String data, int id) {
         Log.w("Eduki", "Eduki: Task Complete!");
         if (id == LESSON_ID) {
             lessonList = LessonQuery.parseLessonsList(data);
            LinearLayout layout = (LinearLayout) mainLayout.findViewById(R.id.lesson_rowview);
            View.OnClickListener v = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    lessonSelected(v.getId());
                }
            };
            Log.w("Eduki LessonListSiz", "Eduki: Task Complete!");
            ViewPopulator.populateCarousel(lessonList, layout, R.layout.red_carousel_item, v, this, "NO LESSONS FOR THIS COURSE");
         } else if(id == QUIZZES_ID) {
             Log.w("Eduki", "Eduki: Parsing quizzes data");
             quizList = QuizQuery.parseQuizzesList(data);
             LinearLayout layout = (LinearLayout) mainLayout.findViewById(R.id.quiz_rowview);
             View.OnClickListener v = new View.OnClickListener() {
                 @Override
                 public void onClick(View v) {
                     quizSelected(v.getId());
                 }
             };
             ViewPopulator.populateCarousel(quizList, layout, R.layout.yellow_carousel_item, v, this, "NO QUIZZES FOR THIS COURSE");
         }
         synchronized(taskCompleteCount) {
             taskCompleteCount++;
             if(taskCompleteCount == 2) {
                 Log.w("Eduki", "Eduki: Setting main layout");
                 setContentView(mainLayout);
             }
         }
     }
 
     /**
      * Will use the position parameter and find that course in the list of
      * lessons, calling the lesson view activity.
      * 
      * @param position the position in the list of the button pressed
      */
     private void lessonSelected(int position) {
         Lesson chosen = lessonList.get(position);
         Intent i = new Intent(this, LessonsViewActivity.class);
         i.putExtra("lesson_title", chosen.getTitle());
         i.putExtra("lesson_id", chosen.getId());
         i.putExtra("lesson_body", chosen.getBody());
         i.putExtra("course_id", course.getId());
         startActivity(i);
     }
     
     /**
      * Will use the position parameter and find that course in the list of quizzes,
      * calling the quiz view activity.
      * @param position the position in the list of the button pressed
      */
     private void quizSelected(int position) {
         Quiz chosen = quizList.get(position);
         Intent i = new Intent(this, QuizzesViewActivity.class);
         i.putExtra("quiz_title", chosen.getTitle());
         i.putExtra("quiz_id", chosen.getId());
         i.putExtra("course_id", chosen.getCourseId());
         startActivity(i);
     }
     
     /**
      * 
      * @return The current list of quizzes in the course selected
      */
     public List<Quiz> getQuizList() {
         return Collections.unmodifiableList(quizList);
     }
 
     /**
      * @return Copy of the list of lessons
      */
     public List<Lesson> getLessonList() {
         return Collections.unmodifiableList(lessonList);
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
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
