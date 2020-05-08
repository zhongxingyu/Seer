 package epfl.sweng.quizzes;
 
 import java.util.List;
 
 import epfl.sweng.R;
 import epfl.sweng.tasks.IQuizzesReceivedCallback;
 import epfl.sweng.tasks.LoadQuizzes;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.app.ListActivity;
 import android.content.Intent;
 
 /**
  * Activity showing the available quizzes (collections of quiz questions)
  */
 public class ShowAvailableQuizzesActivity extends ListActivity {
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_show_available_quizzes);
         
         
         new LoadQuizzes(new IQuizzesReceivedCallback() {
 			
 			@Override
 			public void onSuccess(List<Quiz> quizzes) {
 				displayQuizzes(quizzes);
 			}
 			
 			@Override
 			public void onError() {
 				displayError();
 			}
 
 		}).execute();
     }
     
     public void displayQuizzes(List<Quiz> quizzes) {
     	
         final ArrayAdapter<Quiz> adapter = new ArrayAdapter<Quiz>(this,
 				android.R.layout.simple_list_item_single_choice,
 				quizzes);
         setListAdapter(adapter);
         
     }
 
 	private void displayError() {
 	}
     
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
     	Intent intent = new Intent(this, ShowQuizActivity.class);
    	intent.putExtra("id", ((Quiz) l.getSelectedItem()).getId());
     	startActivity(intent);
 	}
 }
