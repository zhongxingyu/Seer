 package com.huskysoft.eduki;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.Menu;
 import android.widget.TextView;
 
 import com.huskysoft.eduki.R;
 import com.huskysoft.eduki.data.Lesson;
 import com.huskysoft.eduki.data.LessonQuery;
 
 /**
  * @author Rafael Vertido Class LessonsViewActivity shows a specific lesson's content
  */
 
 public class LessonsViewActivity extends Activity implements TaskComplete {
 	
 	/** Specific lesson this view is tied to */
 	private Lesson lesson;
 	
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         Bundle extras = getIntent().getExtras();
         if (extras != null) {
             String lesson_title = extras.getString("lesson_title");
             String lesson_body = extras.getString("lesson_body");
             int lesson_id = extras.getInt("lesson_id");          
             int course_id = extras.getInt("course_id");
             lesson = new Lesson(lesson_id, lesson_title, course_id, lesson_body);
            LessonQuery.getSpecificLesson(this, course_id, lesson_id);
         }
         setContentView(R.layout.loading_screen);
     }
     
     /**
      * 
      * @return A copy of the lesson attached to this lessonView
      */
     public Lesson getLesson() {
         return new Lesson(lesson.getId(), lesson.getTitle(), lesson.getCourseId(), lesson.getBody());
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
 
 
     @Override
     public void taskComplete(String data) {
         String lessonBody = lesson.getBody();
         setContentView(R.layout.activity_lessonview);
         this.setTitle(lesson.getTitle());
         TextView contentView = (TextView) findViewById(R.id.lessonViewLayoutText);
         
         // Check if there was useful content, if there was display it,
         // otherwise, display a message that there is no body found.
         if (lessonBody.equals("")) {
             contentView.setText("No body found for this lesson");
         } else {
             contentView.setText(lessonBody);
         }
     }
 }
