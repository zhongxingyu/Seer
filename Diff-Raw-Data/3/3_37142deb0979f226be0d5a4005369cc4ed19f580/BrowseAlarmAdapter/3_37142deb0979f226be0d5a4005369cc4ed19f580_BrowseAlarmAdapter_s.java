 /*
  * Copyright (C) 2012 Joakim Persson, Daniel Augurell, Adrian Bjugrd, Andreas Roln
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package edu.chalmers.dat255.group09.Alarmed.adapter;
 
 import java.util.List;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnCreateContextMenuListener;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.CheckBox;
 import android.widget.TextView;
 import edu.chalmers.dat255.group09.Alarmed.R;
 import edu.chalmers.dat255.group09.Alarmed.model.Alarm;
 
 /**
  * Adapter to set the view of alarms. 
  * 
  * @author Daniel Augurell
  * @author Joakim Persson
  *
  */
 public class BrowseAlarmAdapter extends ArrayAdapter<Alarm> {
 
 	private OnCreateContextMenuListener listener;
 	
 	/**
 	 * Constructor for the BrowseAlarmAdapter.
 	 * @param context The android context
 	 * @param textViewResourceId The id of the TextView
 	 * @param alarms The alarms to be shown
 	 */
 	public BrowseAlarmAdapter(Context context, int textViewResourceId,
 			List<Alarm> alarms) {
 		super(context, textViewResourceId, alarms);
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		View view = convertView;
 		if (convertView == null) {
 			LayoutInflater inflater = LayoutInflater.from(parent.getContext());
 			view = inflater.inflate(R.layout.alarms_list_item, parent, false);
 		}
 		TextView textView = (TextView) view.findViewById(R.id.alarm_time_text);
 		CheckBox checkBox = (CheckBox) view.findViewById(R.id.alarm_enabled);
 
		textView.setText(getItem(position).getAlarmHours() + ":"
				+ getItem(position).getAlarmMinutes());
 		checkBox.setChecked(getItem(position).isEnabled());
 		checkBox.setTag(getItem(position).getId());
 		
 		view.setOnCreateContextMenuListener(listener);
 
 		return view;
 	}
 
 	/**
 	 * Updates the views with a new list.
 	 * @param list The new list to be shown
 	 */
 	public void updateList(List<Alarm> list) {
 		clear();
 		for (Alarm a : list) {
 			add(a);
 		}
 		notifyDataSetChanged();
 	}
 
 	/**
 	 * Sets a listener to listen on clicks on the views.
 	 * @param contextListener Listener to listen on clicks
 	 */
 	public void setContexMenuListner(OnCreateContextMenuListener contextListener) {
 		this.listener = contextListener;
 	}
 
 }
