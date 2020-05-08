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
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.google.gson.Gson;
 import com.meiste.greg.ptw.GAE.GaeListener;
 import com.meiste.greg.ptw.ObservableScrollView.ScrollViewListener;
 
 public final class Questions extends TabFragment implements View.OnClickListener, ScrollViewListener, GaeListener {
 
     private final static String QCACHE = "question_cache";
 
     private int mWinner;
     private int mA2;
     private int mA3;
     private int mMostLaps;
     private int mNumLeaders;
 
     private int mScroll = 0;
     private boolean mSetupNeeded;
     private boolean mChanged = false;
     private Race mRace;
     private boolean mFailedConnect = false;
 
     public static Questions newInstance(Context context) {
         Questions fragment = new Questions();
         fragment.setTitle(context.getString(R.string.tab_questions));
 
         return fragment;
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         View v;
         mRace = Race.getNext(getActivity(), false, true);
         mSetupNeeded = GAE.isAccountSetupNeeded(getActivity());
         mChanged = false;
 
         if (mRace == null) {
             return inflater.inflate(R.layout.questions_no_race, container, false);
         } else if (mSetupNeeded) {
             return inflater.inflate(R.layout.no_account, container, false);
         } else if (mRace.inProgress()) {
             if (mFailedConnect) {
                 mFailedConnect = false;
                 v = inflater.inflate(R.layout.no_connection, container, false);
 
                 Button retry = (Button) v.findViewById(R.id.retry);
                 retry.setOnClickListener(new View.OnClickListener() {
                     public void onClick(View v) {
                         mChanged = true;
                         notifyChanged();
                     }
                 });
 
                 return v;
             }
 
             SharedPreferences cache = getActivity().getSharedPreferences(QCACHE, Activity.MODE_PRIVATE);
             String json = cache.getString("race" + mRace.getId(), null);
             if (json == null) {
                 new GAE(getActivity(), this).getPage("questions");
                 return inflater.inflate(R.layout.connecting, container, false);
             }
 
             // TODO: Only show form if user hasn't submitted answers yet
             v = inflater.inflate(R.layout.questions, container, false);
             RaceQuestions rq = RaceQuestions.fromJson(json);
 
             Spinner winner = (Spinner) v.findViewById(R.id.winner);
             winner.setAdapter(new DriverAdapter(getActivity(), android.R.layout.simple_spinner_item));
             winner.setOnItemSelectedListener(new WinnerSelectedListener());
 
             TextView q2 = (TextView) v.findViewById(R.id.question2);
             q2.setText(getActivity().getString(R.string.questions_2, rq.q2));
 
             Spinner a2 = (Spinner) v.findViewById(R.id.question2a);
             ArrayAdapter<CharSequence> a2_adapter = new ArrayAdapter<CharSequence>(
                     getActivity(), android.R.layout.simple_spinner_item, rq.a2);
             a2_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
             a2.setAdapter(a2_adapter);
             a2.setOnItemSelectedListener(new A2SelectedListener());
 
             TextView q3 = (TextView) v.findViewById(R.id.question3);
             q3.setText(getActivity().getString(R.string.questions_3, rq.q3));
 
             Spinner a3 = (Spinner) v.findViewById(R.id.question3a);
             ArrayAdapter<CharSequence> a3_adapter = new ArrayAdapter<CharSequence>(
                     getActivity(), android.R.layout.simple_spinner_item, rq.a3);
             a3_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
             a3.setAdapter(a3_adapter);
             a3.setOnItemSelectedListener(new A3SelectedListener());
 
             Spinner mostlaps = (Spinner) v.findViewById(R.id.mostlaps);
             mostlaps.setAdapter(new DriverAdapter(getActivity(), android.R.layout.simple_spinner_item));
             mostlaps.setOnItemSelectedListener(new MostLapsSelectedListener());
 
             Spinner numleaders = (Spinner) v.findViewById(R.id.numleaders);
             ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                     getActivity(), R.array.num_leaders, android.R.layout.simple_spinner_item);
             adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
             numleaders.setAdapter(adapter);
             numleaders.setOnItemSelectedListener(new NumLeadersSelectedListener());
 
             Button send = (Button) v.findViewById(R.id.send);
             send.setOnClickListener(this);
 
             ObservableScrollView sv = (ObservableScrollView) v.findViewById(R.id.scroll_questions);
             sv.postScrollTo(0, mScroll);
             sv.setScrollViewListener(this);
         } else {
             v = inflater.inflate(R.layout.questions_not_yet, container, false);
 
             TextView time = (TextView) v.findViewById(R.id.questiontime);
             time.setText(mRace.getQuestionDateTime());
         }
 
         TextView name = (TextView) v.findViewById(R.id.racename);
         name.setText(mRace.getName());
 
         TextView track = (TextView) v.findViewById(R.id.racetrack);
         track.setText(mRace.getTrack(Race.NAME_LONG));
 
         return v;
     }
 
     @Override
     public void onResume() {
         super.onResume();
 
        if (mSetupNeeded != GAE.isAccountSetupNeeded(getActivity())) {
            mChanged = true;
             notifyChanged();
         }
     }
 
     @Override
     public boolean isChanged() {
         return mChanged;
     }
 
     private static class RaceQuestions {
         public String q2;
         public String[] a2;
         public String q3;
         public String[] a3;
 
         public static RaceQuestions fromJson(String json) {
             Gson gson = new Gson();
             return gson.fromJson(json, RaceQuestions.class);
         }
     }
 
     private class DriverAdapter extends ArrayAdapter<Driver> {
         private Context mContext;
 
         public DriverAdapter(Context context, int textViewResourceId) {
             super(context, textViewResourceId);
             mContext = context;
 
             setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         }
 
         @Override
         public int getCount() {
             return Driver.getNumDrivers(mContext);
         }
 
         @Override
         public Driver getItem(int position) {
             return new Driver(mContext, position);
         }
     }
 
     private class WinnerSelectedListener implements OnItemSelectedListener {
         public void onItemSelected(AdapterView<?> parent, View v, int pos, long id) {
             Driver driver = (Driver) parent.getItemAtPosition(pos);
             mWinner = driver.getNumber();
         }
 
         public void onNothingSelected(AdapterView<?> parent) {}
     }
 
     private class A2SelectedListener implements OnItemSelectedListener {
         public void onItemSelected(AdapterView<?> parent, View v, int pos, long id) {
             mA2 = pos;
         }
 
         public void onNothingSelected(AdapterView<?> parent) {}
     }
 
     private class A3SelectedListener implements OnItemSelectedListener {
         public void onItemSelected(AdapterView<?> parent, View v, int pos, long id) {
             mA3 = pos;
         }
 
         public void onNothingSelected(AdapterView<?> parent) {}
     }
 
     private class MostLapsSelectedListener implements OnItemSelectedListener {
         public void onItemSelected(AdapterView<?> parent, View v, int pos, long id) {
             Driver driver = (Driver) parent.getItemAtPosition(pos);
             mMostLaps = driver.getNumber();
         }
 
         public void onNothingSelected(AdapterView<?> parent) {}
     }
 
     private class NumLeadersSelectedListener implements OnItemSelectedListener {
         public void onItemSelected(AdapterView<?> parent, View v, int pos, long id) {
             mNumLeaders = pos;
         }
 
         public void onNothingSelected(AdapterView<?> parent) {}
     }
 
     @Override
     public void onClick(View v) {
         // TODO Auto-generated method stub
         Util.log("Sending: a1=" + mWinner + ", a2=" + mA2 + ", a3=" + mA3 +
                 ", a4=" + mMostLaps + ", a5=" + mNumLeaders);
         Toast.makeText(getActivity(), "TODO: Actually send answers",
                 Toast.LENGTH_SHORT).show();
 
         mScroll = 0;
         mChanged = true;
         notifyChanged();
     }
 
     @Override
     public void onScrollChanged(ObservableScrollView sv, int x, int y, int oldx, int oldy) {
         mScroll = y;
     }
 
     @Override
     public void onFailedConnect() {
         Util.log("Questions: onFailedConnect");
         mFailedConnect = mChanged = true;
         notifyChanged();
     }
 
     @Override
     public void onGet(String json) {
         Util.log("Questions: onGet: " + json);
 
         SharedPreferences cache = getActivity().getSharedPreferences(QCACHE, Activity.MODE_PRIVATE);
         cache.edit().putString("race" + mRace.getId(), json).commit();
         mChanged = true;
         notifyChanged();
     }
 
     @Override
     public void onLaunchIntent(Intent launch) {}
 
     @Override
     public void onConnectSuccess() {}
 }
