 package com.worldalarm.adapters;
 
 import java.util.HashMap;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Typeface;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.BaseExpandableListAdapter;
 import android.widget.CompoundButton;
 import android.widget.Switch;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.TextView;
 
 import com.worldalarm.R;
 import com.worldalarm.activities.ListAlarmsSwipeViewActivity;
 import com.worldalarm.activities.UpdateAlarmActivity;
 import com.worldalarm.adapters.AlarmAdapter.AlarmHolder;
 import com.worldalarm.db.Alarm;
 import com.worldalarm.preferences.AlarmPreferences;
 
 public class ExpandableListAdapter extends BaseExpandableListAdapter {
 
 	private Context context;
 	private List<String> listTimeZones;
 	private HashMap<String, List<Alarm>> listAlarms;
 	
 	private static final int REQUEST_CODE_RESOLVE_ERR_UPDATE_ALARM = 6000;
 	
 	public ExpandableListAdapter(Context context, List<String> listTimeZones, HashMap<String, List<Alarm>> listAlarms) {
 		this.context = context;
 		this.listTimeZones = listTimeZones;
 		this.listAlarms = listAlarms;
 	}
 	
 	
 	@Override
 	public Object getChild(int groupPosition, int childPosition) {
 		return listAlarms.get(listTimeZones.get(groupPosition)).get(childPosition);
 	}
 
 	@Override
 	public long getChildId(int groupPosition, int childPosition) {
 		return childPosition;
 	}
 
 	@Override
 	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
 		View row = convertView;
 		AlarmHolder holder;
 		
 		if(row == null) {
 			
 			LayoutInflater inflater = ((Activity)context).getLayoutInflater();
 			row = inflater.inflate(R.layout.alarm, parent, false);
 			
 			holder = new AlarmHolder();
 			holder.alarmId = (TextView)row.findViewById(R.id.alarmId);
 			holder.alarmHour = (TextView)row.findViewById(R.id.alarmHour);
 			holder.alarmDate = (TextView)row.findViewById(R.id.alarmDate);
 			holder.alarmCity = (TextView)row.findViewById(R.id.alarmCity);
 			holder.alarmHourLocal = (TextView)row.findViewById(R.id.alarmHourLocal);
 			holder.alarmDateLocal = (TextView)row.findViewById(R.id.alarmDateLocal);
 			holder.alarmSwitchButton = (Switch)row.findViewById(R.id.alarmSwitchButton); 
 			holder.repeatDay_Sun = (TextView)row.findViewById(R.id.repeat_day_sun);
 			holder.repeatDay_Mon = (TextView)row.findViewById(R.id.repeat_day_mon);
 			holder.repeatDay_Tue = (TextView)row.findViewById(R.id.repeat_day_tue);
 			holder.repeatDay_Wed = (TextView)row.findViewById(R.id.repeat_day_wed);
 			holder.repeatDay_Thu = (TextView)row.findViewById(R.id.repeat_day_thu);
 			holder.repeatDay_Fri = (TextView)row.findViewById(R.id.repeat_day_fri);
 			holder.repeatDay_Sat = (TextView)row.findViewById(R.id.repeat_day_sat);
 			
 			row.setTag(holder);
 			
 		} else {
 			holder = (AlarmHolder)row.getTag();
 		}
 		
 		final Alarm alarm = (Alarm) getChild(groupPosition, childPosition);
 		
 		holder.alarmId.setText(String.valueOf(alarm.getId()));
 		holder.alarmHour.setText(alarm.getHour());
 		holder.alarmDate.setText(alarm.getDate());
 		holder.alarmCity.setText(alarm.getCity().getCityName());
 		holder.alarmHourLocal.setText(alarm.getHourLocal());
 		holder.alarmDateLocal.setText(alarm.getDateLocal());
 		
 		holder.alarmSwitchButton.setOnCheckedChangeListener(null);
 		holder.alarmSwitchButton.setChecked(alarm.isActive());
 		
 		holder.alarmSwitchButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 			
 			@Override
 			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 				alarm.setActive(isChecked);
 				
 				AlarmPreferences.updateAlarm(alarm, context);
 				
				((ListAlarmsSwipeViewActivity) context).getmSectionsPagerAdapter().notifyDataSetChanged();
 			}
 		});
 		
 		
 		List<Integer> repeatDays = alarm.getRepeatDays();
 		
 		for(Integer day : repeatDays) {
 		
 			if(Alarm.REPEAT_DAY_SUN.equals(day)) {
 				holder.repeatDay_Sun.setTextColor(context.getResources().getColor(R.color.blue_light));
 				holder.repeatDay_Sun.setTypeface(Typeface.DEFAULT_BOLD);
 			} else if(Alarm.REPEAT_DAY_MON.equals(day)) {
 				holder.repeatDay_Mon.setTextColor(context.getResources().getColor(R.color.blue_light));
 				holder.repeatDay_Mon.setTypeface(Typeface.DEFAULT_BOLD);
 			} if(Alarm.REPEAT_DAY_TUE.equals(day)) {
 				holder.repeatDay_Tue.setTextColor(context.getResources().getColor(R.color.blue_light));
 				holder.repeatDay_Tue.setTypeface(Typeface.DEFAULT_BOLD);
 			} else if(Alarm.REPEAT_DAY_WED.equals(day)) {
 				holder.repeatDay_Wed.setTextColor(context.getResources().getColor(R.color.blue_light));
 				holder.repeatDay_Wed.setTypeface(Typeface.DEFAULT_BOLD);
 			} else if(Alarm.REPEAT_DAY_THU.equals(day)) {
 				holder.repeatDay_Thu.setTextColor(context.getResources().getColor(R.color.blue_light));
 				holder.repeatDay_Thu.setTypeface(Typeface.DEFAULT_BOLD);
 			} else if(Alarm.REPEAT_DAY_FRI.equals(day)) {
 				holder.repeatDay_Fri.setTextColor(context.getResources().getColor(R.color.blue_light));
 				holder.repeatDay_Fri.setTypeface(Typeface.DEFAULT_BOLD);
 			} else if(Alarm.REPEAT_DAY_SAT.equals(day)) {
 				holder.repeatDay_Sat.setTextColor(context.getResources().getColor(R.color.blue_light));
 				holder.repeatDay_Sat.setTypeface(Typeface.DEFAULT_BOLD);
 			}
 		}
 		
 		row.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				
 				Intent intent = new Intent(context, UpdateAlarmActivity.class);
 				intent.putExtra("alamToUpdate", alarm);
 				
 				((Activity)context).startActivityForResult(intent, REQUEST_CODE_RESOLVE_ERR_UPDATE_ALARM);
 			}
 		});
 		
 		row.setPadding(25, 0, 0, 0);
 		
 		return row;
 	}
 
 	@Override
 	public int getChildrenCount(int groupPosition) {
 		List<Alarm> list = listAlarms.get(listTimeZones.get(groupPosition));
 		
 		if(list != null) {
 			return list.size();
 		} else {
 			return 0;
 		}
 		
 	}
 
 	@Override
 	public Object getGroup(int groupPosition) {
 		return listTimeZones.get(groupPosition);
 	}
 
 	@Override
 	public int getGroupCount() {
 		return listTimeZones.size();
 	}
 
 	@Override
 	public long getGroupId(int groupPosition) {
 		return groupPosition;
 	}
 
 	@Override
 	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
 
 		String headerTitle = (String) getGroup(groupPosition);
 		
 		View row = convertView;
 		
 		if(row == null) {
 			LayoutInflater inflater = ((Activity)context).getLayoutInflater();
 			row = inflater.inflate(R.layout.expandable_group_header_alarms, parent, false);
 		}
 		
 		TextView headerTextView = (TextView) row.findViewById(R.id.expandableHeader);
 		headerTextView.setText(headerTitle);
 		
 		return row;
 	}
 
 	@Override
 	public boolean hasStableIds() {
 		return false;
 	}
 
 	@Override
 	public boolean isChildSelectable(int groupPosition, int childPosition) {
 		return true;
 	}
 }
