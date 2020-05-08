 /*
  * Copyright (C) 2012 Gregory S. Meiste  <http://gregmeiste.com>
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
 package com.meiste.greg.ptw;
 
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 
 public final class Schedule extends TabFragment {
 	public static Schedule newInstance(Context context) {
 		Schedule fragment = new Schedule();
 		fragment.setTitle(context.getString(R.string.tab_schedule));
 		
 		return fragment;
 	}
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		View v = inflater.inflate(R.layout.schedule, container, false);
 		
 		Race[] races = new Race[Race.getNumRaces(getActivity())];
 		ListView lv = (ListView) v.findViewById(R.id.schedule);
 		lv.setAdapter(new RaceItemAdapter(getActivity(), R.layout.schedule_row, races));
 		
 		lv.setOnItemClickListener(new OnItemClickListener() {
 		    public void onItemClick(AdapterView<?> parent, View v, int pos, long id) {
 		    	Util.log("Starting activity for race " + pos);
 		    	
 		    	Intent intent = new Intent(getActivity(), RaceActivity.class);
 		    	intent.putExtra(RaceActivity.INTENT_ID, pos);
		    	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
 		    	startActivity(intent);
 		    }
 		});
 		
 		return v;
 	}
 }
