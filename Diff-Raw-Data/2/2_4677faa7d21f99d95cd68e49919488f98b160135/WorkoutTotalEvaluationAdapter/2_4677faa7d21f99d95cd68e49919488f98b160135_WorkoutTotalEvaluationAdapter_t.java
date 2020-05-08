 package de.dhbw.contents;
 
 import java.text.SimpleDateFormat;
 import java.util.List;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 import de.dhbw.container.R;
 import de.dhbw.database.Workout;
 
 public class WorkoutTotalEvaluationAdapter extends ArrayAdapter <Workout>{
 	private Context mContext;
 	private List <Workout> workouts;
	SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
 	public WorkoutTotalEvaluationAdapter(Context context, int resource, List <Workout> workouts) {
 		super(context, resource, workouts);
 		this.mContext = context;
 		this.setWorkouts(workouts);
 	}
 	public List <Workout> getWorkouts() {
 		return workouts;
 	}
 	public void setWorkouts(List <Workout> workouts) {
 		this.workouts = workouts;
 	}
 	
 	
 	
 	 @Override
      public View getView(int position, View convertView, ViewGroup parent) {
              View v = convertView;
              if (v == null) {
                  LayoutInflater vi = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                  v = vi.inflate(R.layout.total_evaluation_row, null);
              }
              Workout w = workouts.get(position);
              if (w != null) {
                      TextView textDatum = (TextView) v.findViewById(R.id.date);
                      TextView textDistance = (TextView) v.findViewById(R.id.distance);
                      TextView textDuration = (TextView) v.findViewById(R.id.duration);
                      if (textDatum != null) {
                     	 textDatum.setText(df.format(w.get_date()));                            }
                      if(textDistance != null){
                     	 textDistance.setText(w.get_distance().toString() + " " + "km");
                      }
                      if(textDuration != null){
                     	 textDuration.setText(w.getDuration());
                      }
              }
              return v;
      }
 }
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 
