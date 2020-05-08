 package com.vorsk.crossfitr;
 
 import java.util.ArrayList;
 
 import com.vorsk.crossfitr.models.WorkoutModel;
 import com.vorsk.crossfitr.models.WorkoutRow;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.content.Context;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 
public class GirlsActivity extends Activity{
 	private static final String tag = "HeroesActivity";
 	private ListView heroesView;
 	private WorkoutModel model_data;
 	private WorkoutRow[] pulledData;
 	private ArrayList<WorkoutRow> workoutrowList;
 	private ListView derp_heroes_list;
 	private GirlsListHelper listAdapter;
 	
 	public void onCreate(Bundle savedInstanceState){
 		
 		Log.d(tag,"at least here!!!  #1");
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.heroes_workout_main);		
 		workoutrowList = new ArrayList<WorkoutRow>();
 		
 		heroesView = (ListView) findViewById(R.id.workout_list_view);		
 		
 		model_data= new WorkoutModel(this);
 
 		Log.d(tag,"at least here!!!  #2");
 	
 		//Access the database and retrieve all heroes workouts
 		model_data.open();	
 		pulledData = model_data.getAllByType(WorkoutModel.TYPE_GIRL);
 		model_data.close();
 
 		Log.d(tag,"at least here!!!  #3");
 	
 		Log.d(tag,"at least here!!!  #4");
 		
 		if (pulledData.length != 0) {
 			for (int i = 0; i < pulledData.length; i++) {
 				workoutrowList.add(pulledData[i]);
 			}		
 
 			Log.d(tag,"at least here!!!  #5");
 		derp_heroes_list = (ListView) findViewById(R.id.heroes_workout_list);
 		listAdapter = new GirlsListHelper(getApplicationContext(), workoutrowList);
 		listAdapter.notifyDataSetChanged();
 		
 		derp_heroes_list.setAdapter(listAdapter);
 		}
 	}
 
 	public class GirlsListHelper extends BaseAdapter implements OnClickListener{
 		
 		private static final String tag = "HeroesListHelper";
 		private final Context listContext;
 		private ArrayList<WorkoutRow> arrayList;
 		private ImageView listArrow;
 		private TextView nameTView;
 		private TextView descTView;
 		private LayoutInflater inflater;		
 		
 		public GirlsListHelper(Context _context, ArrayList<WorkoutRow> _data){
 			this.listContext = _context;
 			this.arrayList = _data;
 			inflater = (LayoutInflater) _context
 					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		}
 		
 		public View getView(int index, View convertView, ViewGroup parent) {
 			if (convertView == null)
 				convertView = inflater
 						.inflate(R.layout.custom_list_item, parent, false);
 
 			listArrow = (ImageView) convertView.findViewById(R.id.image_arrow);
 			listArrow.setOnClickListener(this);
 
 			Log.d(tag, "arrayList.get(" + index + ").name : "
 					+ arrayList.get(index).name);
 
 			nameTView = (TextView) convertView
 					.findViewById(R.id.string_nameofworkout);
 			nameTView.setText(arrayList.get(index).name);
 
 			descTView = (TextView) convertView
 					.findViewById(R.id.string_description);
 			descTView.setText(arrayList.get(index).description);
 			descTView.setSelected(true);
 
 			return convertView;
 		}
 		
 
 		public int getCount() {
 			// TODO Auto-generated method stub
 			return arrayList.size();
 		}
 
 		public String getItem(int index) {
 			return arrayList.get(index).name;
 		}
 
 		public long getItemId(int id) {			
 			return id;
 		}
 
 
 		public void onClick(View arg0) {
 			
 		}
 
 	}	
 }
