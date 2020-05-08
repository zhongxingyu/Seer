 package com.vorsk.crossfitr;
 
 import java.util.ArrayList;
 
 import com.vorsk.crossfitr.models.WorkoutModel;
 import com.vorsk.crossfitr.models.WorkoutRow;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 
 // TODO: All these workout activities should just extend 1 WorkoutActivity
 // class or something
 public class CustomActivity extends Activity implements OnClickListener, OnItemClickListener {
 	private static final String tag = "CustomActivity";
 	private ListView customLView;
 	private View add_custom_button;
 	private WorkoutModel model_data;
 	private WorkoutRow[] pulledData;
 	private ArrayList<WorkoutRow> workoutrowList;
 	private CustomListhelper listAdapter;
 	private ListView derp_custom_List;
 	
 	private TextView titleTextHeader1;
 	private TextView titleTextHeader2;
 	private Typeface font;
 
 	public void onCreate(Bundle savedInstanceState) {
 		
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.custom_workouts_list);
 		
 		font = Typeface.createFromAsset(this.getAssets(), "fonts/Roboto-Thin.ttf");
 		
 		titleTextHeader1 = (TextView) findViewById(R.id.workouts_title);		
 		titleTextHeader1.setTypeface(font);		
 		titleTextHeader2 = (TextView) findViewById(R.id.custom_title);		
 		titleTextHeader2.setTypeface(font);
 		
 		workoutrowList = new ArrayList<WorkoutRow>();
 
 		// create the ListView object
 		customLView = (ListView) findViewById(R.id.workout_list_view);
 
 		// add buttons and add to listener
 		add_custom_button = findViewById(R.id.custom_add_button);
 		add_custom_button.setOnClickListener(this);
 
 		model_data = new WorkoutModel(this);
 
 		// Access the database and retrieve all custom workouts
 		model_data.open();
 		pulledData = model_data.getAllByType(WorkoutModel.TYPE_CUSTOM);
 		model_data.close();
 
 		if (pulledData.length != 0) {
 			for (int i = 0; i < pulledData.length; i++) {
 				workoutrowList.add(pulledData[i]);
 				Log.d(tag, "### creatList is working!!");
 			}
 
 			derp_custom_List = (ListView) this.findViewById(R.id.custom_workout_list);
 
 			listAdapter = new CustomListhelper(getApplicationContext(), workoutrowList);
 			listAdapter.notifyDataSetChanged();
 
 			derp_custom_List.setAdapter(listAdapter);
 			
 			derp_custom_List.setOnItemClickListener(this);
 		}
 	}
 	
 	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
 	{
 		WorkoutRow workout = listAdapter.get(position);
 		Intent x = new Intent(this, WorkoutCustomProfileActivity.class);
 		x.putExtra("ID", workout._id);
 		startActivity(x);
 	}
 
 	public void onClick(View v) {
 		switch (v.getId()) {
 		case R.id.custom_add_button:
 			Intent u = new Intent(this, CustomAddActivity.class);
 			startActivity(u);
 			break;
 		}
 	}
 
 	public class CustomListhelper extends BaseAdapter implements OnClickListener {
 
 		private static final String tag = "CustomListhelper";
 		private final Context listContext;
 		private ArrayList<WorkoutRow> arrayList;
 		private ImageView listArrow;
 		private TextView nameTView;
 		private TextView descTView;
 		private LayoutInflater inflater;
 
 		public CustomListhelper(Context _context, ArrayList<WorkoutRow> _data) {
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
 			nameTView.setTextColor(getResources().getColor(R.color.custom));	
 			nameTView.setTypeface(font);	
 
 			descTView = (TextView) convertView
 					.findViewById(R.id.string_description);
 			descTView.setText(arrayList.get(index).description);
 			descTView.setTextColor(getResources().getColor(R.color.white));
 			descTView.setSelected(true);
 			descTView.setTypeface(font);
 
 			return convertView;
 		}
 
 		public int getCount() {
 			return arrayList.size();
 		}
 
 		public String getItem(int arg0) {
 			return arrayList.get(arg0).name;
 		}
 		
 		public <T> T get(int arg0)
 		{
 		   return (T) arrayList.get(arg0);
 		}
 
 		public long getItemId(int id) {
 			return id;
 		}
 
 		public void onClick(View arg0) {
 			// TODO Auto-generated method stub
 			
 		}
 	}
 	
 	public void onBackPressed() {
 		Intent u = new Intent(this, WorkoutsActivity.class);
 		startActivity(u);
 	}
 }
