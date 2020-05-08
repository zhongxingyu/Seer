 package com.utopia.lijiang;
 
 import java.util.List;
 
 import com.utopia.lijiang.alarm.Alarm;
 import com.utopia.lijiang.alarm.AlarmManager;
 
 import android.content.Context;
 import android.content.Intent;
import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 public class AlarmAdapter extends BaseAdapter {
 
 	private List<Alarm> data = null;
 	private LayoutInflater layoutInflater; 
 	
 	
 	public AlarmAdapter(Context context,List<Alarm> data){
 		this.data = data;
 		this.layoutInflater = LayoutInflater.from(context); 
 	}
 	
 	@Override
 	public int getCount() {
 		return data.size();
 	}
 
 	@Override
 	public Object getItem(int position) {
 		return data.get(position);
 	}
 
 	@Override
 	public long getItemId(int position) {
 		return data.get(position).getId();
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
      
		if(convertView == null){
			Log.d("lijiang","inflate a new view");
			convertView = layoutInflater.inflate(R.layout.alarm_listitem, null);
		}
		
         Alarm item = (Alarm)getItem(position);
        
        String msg = "getView :"+ String.valueOf(item.getId()) + String.valueOf(item.isActive());
		Log.d("lijiang",msg);
        
         setText(item,convertView);
         setTitle(item,convertView);
         setMessage(item,convertView);
         setActive(item,convertView);
 
 		return convertView;
 	}	
 	
 	private void setText(final Alarm item, View convertView){
 		LinearLayout text = (LinearLayout)convertView.findViewById(R.id.alarmText);
 		text.setOnClickListener(new OnClickListener(){
 
 			@Override
 			public void onClick(View arg0) {
 				// TODO Auto-generated method stub
 				Context ctx = arg0.getContext();
 				Intent intent = new Intent(ctx,AddSimpleAlarmActivity.class);
 				intent.putExtra(
 						AddSimpleAlarmActivity.ALARM_LOCATION, 
 						AlarmManager.getInstance().getLocation(item));
 				ctx.startActivity(intent);
 			}
 			
 		});
 	}
 	
 	private void setTitle(Alarm item, View convertView){
 		TextView title = (TextView)convertView.findViewById(R.id.alarmTitle);
 		title.setText(item.getTitle());
 	}
 	
 	private void setMessage(Alarm item, View convertView){
 		TextView msg = (TextView)convertView.findViewById(R.id.alarmMsg);
 		msg.setText(item.getMessage());
 	}
 	
	private void setActive(final Alarm item, final View convertView){
		
 		CheckBox active = (CheckBox)convertView.findViewById(R.id.alarmActive);
		
		active.setOnCheckedChangeListener(null); //Clear old listener, because the view will be reused
 		active.setChecked(item.isActive());	
 		active.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 			@Override
 			public void onCheckedChanged(CompoundButton buttonView,
 					boolean isChecked) {
				
				String msg = "onCheckedChanged :"+ String.valueOf(item.getId()) + String.valueOf(item.isActive());
				Log.d("lijiang",msg);
 				item.setActive(isChecked);
				AlarmManager.getInstance().save2DB(convertView.getContext());
 			}
 			
 		});
 	}
 }
