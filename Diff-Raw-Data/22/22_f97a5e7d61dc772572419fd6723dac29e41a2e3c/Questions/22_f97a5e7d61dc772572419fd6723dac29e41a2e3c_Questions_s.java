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
 
 import java.util.Calendar;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.Resources;
 import android.graphics.Paint;
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
 import com.google.gson.GsonBuilder;
 import com.google.gson.annotations.Expose;
 import com.google.gson.annotations.SerializedName;
 import com.meiste.greg.ptw.GAE.GaeListener;
 import com.meiste.greg.ptw.ObservableScrollView.ScrollViewListener;
 import com.squareup.otto.Subscribe;
 
 public final class Questions extends TabFragment implements View.OnClickListener, ScrollViewListener, GaeListener {
 
     public final static String QCACHE = "question_cache";
     public final static String ACACHE = "answer_cache";
     public final static String CACACHE = "correct_answer_cache";
     public final static String CACHE_PREFIX = Calendar.getInstance().get(Calendar.YEAR) + "_race";
 
     @SuppressWarnings("unused")
     @Expose
     @SerializedName("a1")
     private int mWinner;
 
     @SuppressWarnings("unused")
     @Expose
     @SerializedName("a2")
     private int mA2;
 
     @SuppressWarnings("unused")
     @Expose
     @SerializedName("a3")
     private int mA3;
 
     @SuppressWarnings("unused")
     @Expose
     @SerializedName("a4")
     private int mMostLaps;
 
     @SuppressWarnings("unused")
     @Expose
     @SerializedName("a5")
     private int mNumLeaders;
 
     private int mScroll = 0;
     private boolean mSetupNeeded;
     private boolean mChanged = false;
     private Race mRaceNext;
     private Race mRaceSelected = null;
     private boolean mFailedConnect = false;
     private boolean mSending = false;
     private long mOnCreateViewTime = 0;
     private Spinner mRaceSpinner = null;
     private QuestionsRaceAdapter mRaceAdapter;
 
     public static Questions newInstance(final Context context) {
         final Questions fragment = new Questions();
         fragment.setTitle(context.getString(R.string.tab_questions));
 
         return fragment;
     }
 
     @Override
     public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
         View v;
         mRaceNext = Race.getNext(getActivity(), false, true);
         mSetupNeeded = GAE.isAccountSetupNeeded(getActivity());
         mChanged = false;
         mRaceAdapter = new QuestionsRaceAdapter(getActivity());
         mOnCreateViewTime = System.currentTimeMillis();
         setRetainInstance(true);
         boolean spinnerEnable = true;
 
         if (mRaceSelected == null) {
             if (mRaceNext != null) {
                 mRaceSelected = mRaceNext;
             } else if (mRaceAdapter.getCount() > 0) {
                 mRaceSelected = mRaceAdapter.getItem(mRaceAdapter.getCount()-1);
             }
         }
 
         if (mSetupNeeded) {
             return Util.getAccountSetupView(getActivity(), inflater, container);
         } else if (mRaceSelected == null) {
             return inflater.inflate(R.layout.questions_no_race, container, false);
         } else if (mSending) {
             return inflater.inflate(R.layout.connecting, container, false);
         } else if (mRaceSelected.inProgress() || !mRaceSelected.isFuture()) {
             if (mFailedConnect) {
                 mFailedConnect = false;
                 v = inflater.inflate(R.layout.no_connection, container, false);
 
                 final Button retry = (Button) v.findViewById(R.id.retry);
                 retry.setOnClickListener(new View.OnClickListener() {
                     public void onClick(final View v) {
                         notifyChanged();
                     }
                 });
 
                 return v;
             }
 
             SharedPreferences cache = getActivity().getSharedPreferences(QCACHE, Activity.MODE_PRIVATE);
             String json = cache.getString(CACHE_PREFIX + mRaceSelected.getId(), null);
             if (json == null) {
                 GAE.getInstance(getActivity()).getPage(this, "questions");
                 return inflater.inflate(R.layout.connecting, container, false);
             }
 
             final RaceQuestions rq = RaceQuestions.fromJson(json);
 
             cache = getActivity().getSharedPreferences(ACACHE, Activity.MODE_PRIVATE);
             json = cache.getString(CACHE_PREFIX + mRaceSelected.getId(), null);
             if (json == null) {
                 Util.log("Questions: Showing form");
 
                 v = inflater.inflate(R.layout.questions, container, false);
 
                 final Spinner winner = (Spinner) v.findViewById(R.id.winner);
                 winner.setAdapter(new DriverAdapter(getActivity()));
                 winner.setOnItemSelectedListener(new WinnerSelectedListener());
 
                 final Spinner a2 = (Spinner) v.findViewById(R.id.question2a);
                 final ArrayAdapter<CharSequence> a2_adapter = new ArrayAdapter<CharSequence>(
                         getActivity(), android.R.layout.simple_spinner_item, rq.a2);
                 a2_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                 a2.setAdapter(a2_adapter);
                 a2.setOnItemSelectedListener(new A2SelectedListener());
 
                 final Spinner a3 = (Spinner) v.findViewById(R.id.question3a);
                 final ArrayAdapter<CharSequence> a3_adapter = new ArrayAdapter<CharSequence>(
                         getActivity(), android.R.layout.simple_spinner_item, rq.a3);
                 a3_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                 a3.setAdapter(a3_adapter);
                 a3.setOnItemSelectedListener(new A3SelectedListener());
 
                 final Spinner mostlaps = (Spinner) v.findViewById(R.id.mostlaps);
                 mostlaps.setAdapter(new DriverAdapter(getActivity()));
                 mostlaps.setOnItemSelectedListener(new MostLapsSelectedListener());
 
                 final Spinner numleaders = (Spinner) v.findViewById(R.id.numleaders);
                 final ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                         getActivity(), R.array.num_leaders, android.R.layout.simple_spinner_item);
                 adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                 numleaders.setAdapter(adapter);
                 numleaders.setOnItemSelectedListener(new NumLeadersSelectedListener());
 
                 final Button send = (Button) v.findViewById(R.id.send);
                 send.setOnClickListener(this);
             } else {
                 Util.log("Questions: Showing submitted answers");
 
                 v = inflater.inflate(R.layout.questions_answered, container, false);
                 final Resources res = getActivity().getResources();
                 final RaceAnswers ra = RaceAnswers.fromJson(json);
 
                 final TextView a1 = (TextView) v.findViewById(R.id.answer1);
                 a1.setText(Driver.newInstance(res, ra.a1).getName());
 
                 final TextView a2 = (TextView) v.findViewById(R.id.answer2);
                 a2.setText(rq.a2[ra.a2]);
 
                 final TextView a3 = (TextView) v.findViewById(R.id.answer3);
                 a3.setText(rq.a3[ra.a3]);
 
                 final TextView a4 = (TextView) v.findViewById(R.id.answer4);
                 a4.setText(Driver.newInstance(res, ra.a4).getName());
 
                 final TextView a5 = (TextView) v.findViewById(R.id.answer5);
                 a5.setText(res.getStringArray(R.array.num_leaders)[ra.a5]);
 
                 if (!mRaceSelected.isFuture()) {
                     cache = getActivity().getSharedPreferences(CACACHE, Activity.MODE_PRIVATE);
                     json = cache.getString(CACHE_PREFIX + mRaceSelected.getId(), null);
                     if (json == null) {
                         getSherlockActivity().setSupportProgressBarIndeterminateVisibility(true);
                         spinnerEnable = false;
                         GAE.getInstance(getActivity()).getPage(
                                 new CorrectAnswersListener(mRaceSelected.getId()),
                                 "answers?year=" + mRaceSelected.getStartYear() +
                                 "&race_id=" + mRaceSelected.getId());
                     } else {
                         Util.log("Questions: Correct answers available");
 
                         final RaceAnswers rca = RaceAnswers.fromJson(json);
 
                         // Have to check for null in case there is no correct answer
                         if ((rca.a1 != null) && (rca.a1 >= 0)) {
                             if (rca.a1 == ra.a1) {
                                 a1.setTextColor(res.getColor(R.color.answer_right));
                             } else {
                                 a1.setPaintFlags(a1.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                                 a1.setTextColor(res.getColor(R.color.answer_wrong));
                                 final TextView c1 = (TextView) v.findViewById(R.id.correct1);
                                 c1.setText(Driver.newInstance(res, rca.a1).getName());
                                 c1.setVisibility(View.VISIBLE);
                             }
                         }
                         if ((rca.a2 != null) && (rca.a2 >= 0)) {
                             if (rca.a2 == ra.a2) {
                                 a2.setTextColor(res.getColor(R.color.answer_right));
                             } else {
                                 a2.setPaintFlags(a2.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                                 a2.setTextColor(res.getColor(R.color.answer_wrong));
                                 final TextView c2 = (TextView) v.findViewById(R.id.correct2);
                                 c2.setText(rq.a2[rca.a2]);
                                 c2.setVisibility(View.VISIBLE);
                             }
                         }
                         if ((rca.a3 != null) && (rca.a3 >= 0)) {
                             if (rca.a3 == ra.a3) {
                                 a3.setTextColor(res.getColor(R.color.answer_right));
                             } else {
                                 a3.setPaintFlags(a3.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                                 a3.setTextColor(res.getColor(R.color.answer_wrong));
                                 final TextView c3 = (TextView) v.findViewById(R.id.correct3);
                                 c3.setText(rq.a3[rca.a3]);
                                 c3.setVisibility(View.VISIBLE);
                             }
                         }
                         if ((rca.a4 != null) && (rca.a4 >= 0)) {
                             if (rca.a4 == ra.a4) {
                                 a4.setTextColor(res.getColor(R.color.answer_right));
                             } else {
                                 a4.setPaintFlags(a4.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                                 a4.setTextColor(res.getColor(R.color.answer_wrong));
                                 final TextView c4 = (TextView) v.findViewById(R.id.correct4);
                                 c4.setText(Driver.newInstance(res, rca.a4).getName());
                                 c4.setVisibility(View.VISIBLE);
                             }
                         }
                         if ((rca.a5 != null) && (rca.a5 >= 0)) {
                             if (rca.a5 == ra.a5) {
                                 a5.setTextColor(res.getColor(R.color.answer_right));
                             } else {
                                 a5.setPaintFlags(a5.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                                 a5.setTextColor(res.getColor(R.color.answer_wrong));
                                 final TextView c5 = (TextView) v.findViewById(R.id.correct5);
                                 c5.setText(res.getStringArray(R.array.num_leaders)[rca.a5]);
                                 c5.setVisibility(View.VISIBLE);
                             }
                         }
                     } // Correct answers available
                 } // !mRaceSelected.isFuture()
             }
 
             final TextView q2 = (TextView) v.findViewById(R.id.question2);
             q2.setText(getActivity().getString(R.string.questions_2, rq.q2));
 
             final TextView q3 = (TextView) v.findViewById(R.id.question3);
             q3.setText(getActivity().getString(R.string.questions_3, rq.q3));
 
             final ObservableScrollView sv = (ObservableScrollView) v.findViewById(R.id.scroll_questions);
             sv.postScrollTo(0, mScroll);
             sv.setScrollViewListener(this);
         } else {
             v = inflater.inflate(R.layout.questions_not_yet, container, false);
 
             final TextView time = (TextView) v.findViewById(R.id.questiontime);
             time.setText(mRaceSelected.getQuestionDateTime(getActivity()));
         }
 
         mRaceSpinner = (Spinner) v.findViewById(R.id.race_spinner);
         if (mRaceSpinner != null) {
             mRaceSpinner.setAdapter(mRaceAdapter);
             mRaceSpinner.setSelection(mRaceAdapter.getPosition(mRaceSelected));
             mRaceSpinner.setOnItemSelectedListener(new RaceSelectedListener());
             mRaceSpinner.setEnabled(spinnerEnable);
         }
 
         return v;
     }
 
     @Override
     public void onResume() {
         super.onResume();
         BusProvider.getInstance().register(this);
 
         // Check if user changed their account status
         mChanged = mSetupNeeded != GAE.isAccountSetupNeeded(getActivity());
         // Check if user has switched accounts
        mChanged |= mOnCreateViewTime < Util.getAccountSetupTime(getActivity());
         if (mRaceNext != null) {
             // See if race questions are now available but weren't previously
             mChanged |= (mOnCreateViewTime < mRaceNext.getQuestionTimestamp()) && mRaceNext.inProgress();
             // Check if questions form needs to disappear because race started
             mChanged |= !mRaceNext.isFuture();
         }
 
         if (mChanged) {
             Util.log("Questions: onResume: notifyChanged");
             resetRaceSelected();
         }
     }
 
     @Override
     public void onPause() {
         super.onPause();
         BusProvider.getInstance().unregister(this);
     }
 
     @Override
     public boolean isChanged() {
         return mChanged;
     }
 
     @Override
     protected void notifyChanged() {
         mChanged = true;
         super.notifyChanged();
     }
 
     @Subscribe
     public void onScheduleUpdate(final ScheduleUpdateEvent event) {
         Util.log("Questions: onScheduleUpdate");
         resetRaceSelected();
     }
 
     @Subscribe
     public void onRaceAlarm(final RaceAlarmEvent event) {
         Util.log("Questions: onRaceAlarm");
         resetRaceSelected();
     }
 
     private void resetRaceSelected() {
         // Workaround to ensure the spinner is set back to next race
         if ((mRaceSpinner != null) && (mRaceAdapter.getCount() > 0) &&
                 (mRaceSpinner.getSelectedItemPosition() != (mRaceAdapter.getCount() - 1))) {
             // Making new selection causes notifyChanged to be called by listener
             mRaceSpinner.setSelection(mRaceAdapter.getCount() - 1);
         } else {
             notifyChanged();
         }
     }
 
     private class RaceSelectedListener implements OnItemSelectedListener {
         public void onItemSelected(final AdapterView<?> parent, final View v, final int pos, final long id) {
             final Race race = (Race) parent.getItemAtPosition(pos);
             if (mRaceSelected.getId() != race.getId()) {
                 mRaceSelected = race;
                 Util.log("Questions: Selected race = " + mRaceSelected.getId());
                 notifyChanged();
             }
         }
 
         public void onNothingSelected(final AdapterView<?> parent) {}
     }
 
     private class WinnerSelectedListener implements OnItemSelectedListener {
         public void onItemSelected(final AdapterView<?> parent, final View v, final int pos, final long id) {
             final Driver driver = (Driver) parent.getItemAtPosition(pos);
             mWinner = driver.getNumber();
         }
 
         public void onNothingSelected(final AdapterView<?> parent) {}
     }
 
     private class A2SelectedListener implements OnItemSelectedListener {
         public void onItemSelected(final AdapterView<?> parent, final View v, final int pos, final long id) {
             mA2 = pos;
         }
 
         public void onNothingSelected(final AdapterView<?> parent) {}
     }
 
     private class A3SelectedListener implements OnItemSelectedListener {
         public void onItemSelected(final AdapterView<?> parent, final View v, final int pos, final long id) {
             mA3 = pos;
         }
 
         public void onNothingSelected(final AdapterView<?> parent) {}
     }
 
     private class MostLapsSelectedListener implements OnItemSelectedListener {
         public void onItemSelected(final AdapterView<?> parent, final View v, final int pos, final long id) {
             final Driver driver = (Driver) parent.getItemAtPosition(pos);
             mMostLaps = driver.getNumber();
         }
 
         public void onNothingSelected(final AdapterView<?> parent) {}
     }
 
     private class NumLeadersSelectedListener implements OnItemSelectedListener {
         public void onItemSelected(final AdapterView<?> parent, final View v, final int pos, final long id) {
             mNumLeaders = pos;
         }
 
         public void onNothingSelected(final AdapterView<?> parent) {}
     }
 
     private class CorrectAnswersListener implements GaeListener {
         private final int raceId;
 
         public CorrectAnswersListener(final int id) {
             raceId = id;
         }
 
         @Override
         public void onFailedConnect(final Context context) {
             Util.log("CorrectAnswersListener: onFailedConnect");
 
             // Check for null in case race alarm has fired, changing the view
             if (mRaceSpinner != null) {
                 mRaceSpinner.setEnabled(true);
             }
 
             // Verify application wasn't closed before callback returned
             if (getSherlockActivity() != null) {
                 getSherlockActivity().setSupportProgressBarIndeterminateVisibility(false);
             }
         }
 
         @Override
         public void onGet(final Context context, final String json) {
             Util.log("CorrectAnswersListener: onGet: " + json);
 
             final SharedPreferences cache = context.getSharedPreferences(CACACHE, Activity.MODE_PRIVATE);
             cache.edit().putString(CACHE_PREFIX + raceId, json).commit();
 
             // Check for null in case race alarm has fired, changing the view
             if (mRaceSpinner != null) {
                 mRaceSpinner.setEnabled(true);
             }
 
             // Verify application wasn't closed before callback returned
             if (getSherlockActivity() != null) {
                 getSherlockActivity().setSupportProgressBarIndeterminateVisibility(false);
                 notifyChanged();
             }
         }
 
         @Override
         public void onConnectSuccess(final Context context, final String json) {}
 
         @Override
         public void onLaunchIntent(final Intent launch) {}
     }
 
     @Override
     public void onClick(final View v) {
         mScroll = 0;
         mSending = true;
         notifyChanged();
 
         final Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
         GAE.getInstance(getActivity()).postPage(this, "questions", gson.toJson(this));
     }
 
     @Override
     public void onScrollChanged(final ObservableScrollView sv, final int x, final int y, final int oldx, final int oldy) {
         mScroll = y;
     }
 
     @Override
     public void onFailedConnect(final Context context) {
         Util.log("Questions: onFailedConnect");
 
         // Verify application wasn't closed before callback returned
         if (getActivity() != null) {
             mSending = false;
             mFailedConnect = true;
             notifyChanged();
         }
     }
 
     @Override
     public void onGet(final Context context, final String json) {
         Util.log("Questions: onGet: " + json);
 
         final SharedPreferences cache = context.getSharedPreferences(QCACHE, Activity.MODE_PRIVATE);
         cache.edit().putString(CACHE_PREFIX + mRaceNext.getId(), json).commit();
 
         // Verify application wasn't closed before callback returned
         if (getActivity() != null) {
             notifyChanged();
         }
     }
 
     @Override
     public void onConnectSuccess(final Context context, final String json) {
         Util.log("Questions: onConnectSuccess: " + json);
         Toast.makeText(context, R.string.questions_success, Toast.LENGTH_SHORT).show();
 
         final SharedPreferences cache = context.getSharedPreferences(ACACHE, Activity.MODE_PRIVATE);
         cache.edit().putString(CACHE_PREFIX + mRaceNext.getId(), json).commit();
 
         // Verify application wasn't closed before callback returned
         if (getActivity() != null) {
             mSending = false;
             notifyChanged();
         }
     }
 
     @Override
     public void onLaunchIntent(final Intent launch) {}
 }
