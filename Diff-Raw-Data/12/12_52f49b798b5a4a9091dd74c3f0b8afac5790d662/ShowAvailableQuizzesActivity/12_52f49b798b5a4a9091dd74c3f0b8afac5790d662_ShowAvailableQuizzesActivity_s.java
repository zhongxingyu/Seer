 package epfl.sweng.quizzes;
 
 import epfl.sweng.R;
import epfl.sweng.R.layout;
import epfl.sweng.R.menu;
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 
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
