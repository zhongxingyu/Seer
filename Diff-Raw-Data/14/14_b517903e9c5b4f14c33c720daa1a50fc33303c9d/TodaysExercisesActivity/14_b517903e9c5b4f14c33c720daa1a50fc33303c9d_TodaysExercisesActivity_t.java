 package ufit.profilecreation;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.TextView;
 import ufit.namespace.R;
 
 
 public class TodaysExercisesActivity extends Activity implements OnClickListener {
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.todaysexercises);
         initialiseButtons();
         
         //name of items
         String[] items = {"red", "blue","green"};
 
         ListView listView = (ListView) findViewById(R.id.ListViewId);
         listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items));
         
     }
     
     
     
     
     public class ExercisesAdapter extends ArrayAdapter<ListExercises>{
     	private ArrayList<ListExercises> exercises;
     	
     	public ExercisesAdapter(Context context, int textViewResourceId, ArrayList<ListExercises> exercises){
     		super(context, textViewResourceId, exercises);
     		this.exercises = exercises;
     	}
     	
     	@Override
     	public View getView(int position, View convertView, ViewGroup parent){
     		View v = convertView;
     		
     		if(v == null) {
     			LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
     			v = vi.inflate(R.layout.exerciseslists, null);
     		}
     		
     		ListExercises exercise = exercises.get(position);
     		if(exercise != null) {
     			TextView exerciseName = (TextView) v.findViewById(R.id.exerciseName);
     			
     			if(exerciseName != null){
     				exerciseName.setText(exercise.exerciseName);	
     			}
     		}
     		return v;
     	}
     }
     
     
     public class ListExercises {
     	public String exerciseName;
     	
     	public ListExercises(String exerciseName){
     		this.exerciseName = exerciseName;
     	}
     	
     	public void setText(String exerciseName)
     	{
     		this.exerciseName = exerciseName;
     	
     	}
     }
    
    public void initialiseButtons() {
        Button weekly = (Button) findViewById(R.id.weeklyplanner);
        weekly.setOnClickListener(this);
        
    }    
    //@Override
	public void onClick(View v) {
		if(v.getId() == R.id.weeklyplanner){
			Intent intent = new Intent(this,WeeklyPlannerActivity.class);
			this.startActivity(intent);	        
		}
    }
     
 }
