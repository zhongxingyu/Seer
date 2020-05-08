 package com.photon.connecttodoor.uiadapter;
 
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.graphics.Color;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.TextView;
 
 import com.photon.connecttodoor.R;
 import com.photon.connecttodoor.datamodel.DailyAttendanceListModel;
 
 public class ListGeneratedDailyArrayAdapter extends BaseAdapter {
 
 	private final Context context;
 	private ArrayList<DailyAttendanceListModel> values;
 
 
 	public ListGeneratedDailyArrayAdapter(Context context ,ArrayList<DailyAttendanceListModel> arrayList) {
 		//super(context, textViewResourceId);
 		this.context = context;
 		this.values = arrayList;
 	}
 
 	@Override
 	public int getCount() {
 		// TODO Auto-generated method stub
 		return values.size();
 	}
 
 
 	@Override
 	public Object getItem(int position) {
 		// TODO Auto-generated method stub
 		return values.get(position);
 	}
 
 
 	@Override
 	public long getItemId(int position) {
 		// TODO Auto-generated method stub
 		return position;
 	}
 
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 
 		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		convertView = inflater.inflate(R.layout.text_table_daily_attendance, null);
 		TextView numberTextView = (TextView) convertView.findViewById(R.id.number_text);
 		TextView nameTextView = (TextView) convertView.findViewById(R.id.name_text);
 		TextView checkInTextView = (TextView) convertView.findViewById(R.id.check_in_text);
 		TextView checkOutTextView = (TextView) convertView.findViewById(R.id.check_out_text);
		convertView.setBackgroundColor(position % 2 == 0 ? Color.WHITE : Color.parseColor("#cfe9d0"));
 
 		numberTextView.setText(values.get(position).getNumber());
 		nameTextView.setText(values.get(position).getName());
 		checkInTextView.setText(values.get(position).getCheckIn());
 		checkOutTextView.setText(values.get(position).getCheckOut());
 
 
 		return convertView;
 	}
 }
