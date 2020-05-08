 package com.vorsk.crossfitr;
 
 import java.sql.Date;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 
 import com.vorsk.crossfitr.models.AchievementModel;
 import com.vorsk.crossfitr.models.AchievementRow;
 import com.vorsk.crossfitr.models.WorkoutRow;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Typeface;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView;
 import android.widget.BaseAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class AchievementActivity extends Activity implements OnItemClickListener {
 	private AchievementModel model_data;
 	private AchievementRow[] pulledData;
 	private ArrayList<AchievementRow> achievementrowList;
 	private ListView derp_achievements_list;
 	private AchievementListHelper listAdapter;
 
 	private Typeface font;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState){
 				
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.achievements_list);	
 		
 		font = Typeface.createFromAsset(this.getAssets(), "fonts/Roboto-Thin.ttf");		
 				
 		achievementrowList = new ArrayList<AchievementRow>();	
 		
 		model_data= new AchievementModel(this);
 	
 		//Access the database and retrieve all the achievements
 		model_data.open();	
 		pulledData = model_data.getAll();
 		model_data.close();
 
 		if (pulledData.length != 0) {
 			for (int i = 0; i < pulledData.length; i++) {
 				achievementrowList.add(pulledData[i]);
 			}		
 
 			derp_achievements_list = (ListView) findViewById(R.id.achievement_list_view);
 			
 			listAdapter = new AchievementListHelper(getApplicationContext(), achievementrowList);
 			listAdapter.notifyDataSetChanged();
 			
 			derp_achievements_list.setAdapter(listAdapter);
 			
 			derp_achievements_list.setOnItemClickListener(this);
 		
 		}
 	}
 	
 	public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
 	{
 		
 		AchievementRow achievement = listAdapter.get(position);
 		
 		Context context = getApplicationContext();
 		CharSequence text;
 		int duration;
 		Toast toast;
 		duration = Toast.LENGTH_SHORT;
 		
 		if(achievement.count >= 1){
 			Date date = new Date(achievement.date_modified);
			SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yy");
 			text = "Achieved on " + formatter.format(date);
 		}
 		else{
 			int difference = achievement.progress_thresh - achievement.progress;
			text = difference + " more workout(s) to go";
 		}
 		
 		toast = Toast.makeText(context, text, duration);
 		toast.show();
 	}
 
 
 	public class AchievementListHelper extends BaseAdapter implements OnClickListener {
 
 		private static final String tag = "AchievementListHelper";
 		private final Context listContext;
 		private ArrayList<AchievementRow> arrayList;
 		private TextView nameTView;
 		private TextView descTView;
 		private LayoutInflater inflater;
 
 		public AchievementListHelper(Context _context, ArrayList<AchievementRow> _data) {
 			this.listContext = _context;
 			this.arrayList = _data;
 			inflater = (LayoutInflater) _context
 					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		}
 
 		public View getView(int index, View convertView, ViewGroup parent) {
 			
 			if (convertView == null)
 				convertView = inflater
 						.inflate(R.layout.custom_list_item, parent, false);
 
 
 			Log.d(tag, "arrayList.get(" + index + ").name : "
 					+ arrayList.get(index).name);
 			
 			// achievement name
 			nameTView = (TextView) convertView
 					.findViewById(R.id.string_nameofworkout);
 			nameTView.setText(arrayList.get(index).name);
 			if(arrayList.get(index).count < 1){
 				nameTView.setTextColor(getResources().getColor(R.color.blue));
 			}
 			else{
 				nameTView.setTextColor(getResources().getColor(R.color.heroes));
 			}
 			nameTView.setTypeface(font);		
 			
 			//achievement description
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
 
 		public String getItem(int index) {
 			return arrayList.get(index).name;
 		}
 
 		public <T> T get(int arg0) {
 			return (T) arrayList.get(arg0);
 		}
 
 		public long getItemId(int id) {
 			return id;
 		}
 
 		public void onClick(View arg0) {
 
 		}
 
 	}
 }
