 package epfl.sweng.quizzes;
 
 import epfl.sweng.R;
 import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.app.ListActivity;
 
 /**
  * Activity showing the available quizzes (collections of quiz questions)
  */
public class ShowAvailableQuizzesActivity extends ListActivity {
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_show_available_quizzes);
        
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_single_choice,
				new String[] {""});
        
        setListAdapter(adapter);
        
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_show_available_quizzes, menu);
         return true;
     }
     
     
 }
