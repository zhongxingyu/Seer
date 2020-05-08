 package nl.tuestudent.thermostaat;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import nl.tuestudent.thermostaat.data.DayProgram;
 import nl.tuestudent.thermostaat.data.DayProgram.ProgramSwitch;
 
 import android.content.Context;
 import android.graphics.Color;
 import android.graphics.drawable.Drawable;
 import android.support.v4.app.DialogFragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.webkit.WebView.FindListener;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 public class LazyAdapter extends ArrayAdapter<String>{
 	private final Context context;
 	HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();
 	private String[] values;
 	private ArrayList<String> times;
 	
 	private ProgramSwitch prevSwitch = null;
 
 	public LazyAdapter(Context context, int textViewResourceId, ArrayList<String> objects) {
 		super(context,R.layout.row_change_day, R.id.label, objects);
 		
 		for (int i = 0; i < objects.size(); ++i) {
 			mIdMap.put(objects.get(i), i);
 		}
 		values = new String[24];
 		for(int i=0; i< values.length; i++) {
 			values[i] = "";
 		}
 		
 		this.times = objects;
 		this.context = context;
 		this.prevSwitch = null;
 	}
 
 	@Override
 	public long getItemId(int position) {
 		String item = getItem(position);
 		return mIdMap.get(item);
 	}
 
 	@Override
 	public boolean hasStableIds() {
 		return true;
 	}
 	
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		View rowView = inflater.inflate(R.layout.row_change_day, parent, false);
 		ImageView imageView = (ImageView) rowView.findViewById(R.id.dayNightImg);
 
 		TextView label = (TextView) rowView.findViewById(R.id.label);
 		label.setText(times.get(position));
 
 		DayProgram dp = ChangeDay.dayProgram;
 		LinearLayout day = (LinearLayout)rowView.findViewById(R.id.dayLayout);
 		LinearLayout night = (LinearLayout)rowView.findViewById(R.id.nightLayout);
 		TextView dnText = (TextView) rowView.findViewById(R.id.dayNightText);
 		Drawable nightbg = rowView.getResources().getDrawable(R.drawable.background_night);
 		Drawable daybg = rowView.getResources().getDrawable(R.drawable.background_day);
 
 		ProgramSwitch ps = dp.getSwitchByHour(position);
 		if(ps == null) {
 			if(prevSwitch == null) {
 				night.setBackground(nightbg);
 				if(position == 0) {
 					dnText.setText("00:00");
 				} else {
 					dnText.setText(" ");
 				}
 				//TODO maantje
 			} else {
 				final Drawable bg = (prevSwitch.getType().equals("day"))? daybg : nightbg;
 				night.setBackground(bg);
 				dnText.setText(" ");
 				//TODO plaatje van prevSwitch
 			}
 			return rowView;
 		}
 
 		if(ps.getHour() == position) {
 			Log.d("LazyAdapter", "ps.hour: " + ps.getHour() + " pos: " + position);
 			if(ps.getType().equals("day")) {
				night.setBackground(daybg);
 				dnText.setTextColor(Color.BLACK);
 			} else {
				night.setBackground(nightbg);
 				dnText.setTextColor(Color.WHITE);
 			}
 			dnText.setText(String.format("%02d:%02d", ps.getHour(), ps.getMin()));
 			dnText.invalidate();
 			//TODO plaatje
 
 			prevSwitch = ps;
 		}
 
 		return rowView;
 	}
 }
 
