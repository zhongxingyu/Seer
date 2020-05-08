 package epfl.sweng.quizzes;
 
 import epfl.sweng.R;
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 
/**
 * Activity showing the available quizzes (collections of quiz questions)
 */
 public class ShowAvailableQuizzesActivity extends Activity {
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_show_available_quizzes);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_show_available_quizzes, menu);
         return true;
     }
 }
