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
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
import com.meiste.greg.ptw.ObservableScrollView.ScrollViewListener;

public final class Suggest extends TabFragment implements View.OnClickListener, ScrollViewListener {
 	private EditText mQuestion;
	private int mScroll = 0;
 	
 	public static Suggest newInstance(Context context) {
 		Suggest fragment = new Suggest();
 		fragment.setTitle(context.getString(R.string.tab_suggest));
 		
 		return fragment;
 	}
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		Race race = Race.getNext(getActivity(), false, false);
 		
 		if (race == null) {
 			return inflater.inflate(R.layout.suggest_no_race, container, false);
 		}
 		
 		View v = inflater.inflate(R.layout.suggest, container, false);
 		
 		TextView track = (TextView) v.findViewById(R.id.racetrack);
 		track.setText(race.getTrack(Race.NAME_LONG));
 		
 		mQuestion = (EditText) v.findViewById(R.id.question);
 		
 		Button send = (Button) v.findViewById(R.id.send);
 		send.setOnClickListener(this);
 		
		ObservableScrollView sv = (ObservableScrollView) v.findViewById(R.id.scroll_suggest);
		sv.postScrollTo(0, mScroll);
		sv.setScrollViewListener(this);
		
 		return v;
 	}
 
 	@Override
 	public void onClick(View v) {
 		// TODO Auto-generated method stub
 		Toast.makeText(getActivity(), "TODO: Actually send suggestion",
 				Toast.LENGTH_SHORT).show();
 		mQuestion.setText("");
 	}
	
	@Override
	public void onScrollChanged(ObservableScrollView sv, int x, int y, int oldx, int oldy) {
		mScroll = y;
	}
 }
