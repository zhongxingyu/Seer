 /*
  * Copyright (C) 2012 The Android Open Source Project
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
 
 package com.android.deskclock.timer;
 
 import android.app.NotificationManager;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import android.content.res.Resources;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.ListView;
 
 import com.android.deskclock.DeskClockFragment;
 import com.android.deskclock.R;
 import com.android.deskclock.TimerSetupView;
 import com.android.deskclock.Utils;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 
 
 public class TimerFragment extends DeskClockFragment
         implements OnClickListener, OnSharedPreferenceChangeListener {
 
     private static final String TAG = "TimerFragment";
     private ListView mTimersList;
     private View mNewTimerPage;
     private View mTimersListPage;
     private Button mCancel, mStart;
     private ImageButton mAddTimer;
     private TimerSetupView mTimerSetup;
     private TimersListAdapter mAdapter;
     private boolean mTicking = false;
     private SharedPreferences mPrefs;
     private NotificationManager mNotificationManager;
     private OnEmptyListListener mOnEmptyListListener;
 
     public TimerFragment() {
     }
 
     class ClickAction {
         public static final int ACTION_STOP = 1;
         public static final int ACTION_PLUS_ONE = 2;
         public static final int ACTION_DELETE = 3;
 
         public int mAction;
         public TimerObj mTimer;
 
         public ClickAction(int action, TimerObj t) {
             mAction = action;
             mTimer = t;
         }
     }
 
     // Container Activity that requests TIMESUP_MODE must implement this interface
     public interface OnEmptyListListener {
         public void onEmptyList();
     }
 
     TimersListAdapter createAdapter(Context context, SharedPreferences prefs) {
         if (mOnEmptyListListener == null) {
             return new TimersListAdapter(context, prefs);
         } else {
             return new TimesUpListAdapter(context, prefs);
         }
     }
 
     class TimersListAdapter extends BaseAdapter {
 
         ArrayList<TimerObj> mTimers = new ArrayList<TimerObj> ();
         private final LayoutInflater mInflater;
         Context mContext;
         SharedPreferences mmPrefs;
 
         public TimersListAdapter(Context context, SharedPreferences prefs) {
             mContext = context;
             mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
             mmPrefs = prefs;
         }
 
         @Override
         public int getCount() {
             return mTimers.size();
         }
 
         @Override
         public Object getItem(int p) {
             return mTimers.get(p);
         }
 
         @Override
         public long getItemId(int p) {
             if (p >= 0 && p < mTimers.size()) {
                 return mTimers.get(p).mTimerId;
             }
             return 0;
         }
 
         public void deleteTimer(int id) {
             for (int i = 0; i < mTimers.size(); i++) {
                 TimerObj t = mTimers.get(i);
                 if (t.mTimerId == id) {
                     ((TimerListItem)t.mView).stop();
                     t.deleteFromSharedPref(mmPrefs);
                     mTimers.remove(i);
                     notifyDataSetChanged();
                     return;
                 }
             }
         }
 
         protected int findTimerPositionById(int id) {
             for (int i = 0; i < mTimers.size(); i++) {
                 TimerObj t = mTimers.get(i);
                 if (t.mTimerId == id) {
                     return i;
                 }
             }
             return -1;
         }
 
         public void removeTimer(TimerObj timerObj) {
             int position = findTimerPositionById(timerObj.mTimerId);
             if (position >= 0) {
                 mTimers.remove(position);
                 notifyDataSetChanged();
             }
         }
 
         @Override
         public View getView(int position, View convertView, ViewGroup parent) {
             TimerListItem v;
 
    //         if (convertView != null) {
      //           v = (TimerListItem) convertView;
        //     } else {
                 v = new TimerListItem (mContext);
          //   }
 
             final TimerObj o = (TimerObj)getItem(position);
             o.mView = v;
             long timeLeft =  o.updateTimeLeft(false);
             boolean drawRed = o.mState != TimerObj.STATE_RESTART;
             v.set(o.mOriginalLength, timeLeft, drawRed);
             v.setTime(timeLeft, true);
             switch (o.mState) {
             case TimerObj.STATE_RUNNING:
                 v.start();
                 break;
             case TimerObj.STATE_TIMESUP:
                 v.timesUp();
                 break;
             case TimerObj.STATE_DONE:
                 v.done();
                 break;
             default:
                 break;
             }
 
             // Timer text serves as a virtual start/stop button.
             final CountingTimerView countingTimerView = (CountingTimerView)
                     v.findViewById(R.id.timer_time_text);
             countingTimerView.registerVirtualButtonAction(new Runnable() {
                 @Override
                 public void run() {
                     TimerFragment.this.onClickHelper(
                             new ClickAction(ClickAction.ACTION_STOP, o));
                 }
             });
 
             ImageButton delete = (ImageButton)v.findViewById(R.id.timer_delete);
             delete.setOnClickListener(TimerFragment.this);
             delete.setTag(new ClickAction(ClickAction.ACTION_DELETE, o));
             ImageButton plusOne = (ImageButton)v. findViewById(R.id.timer_plus_one);
             plusOne.setOnClickListener(TimerFragment.this);
             plusOne.setTag(new ClickAction(ClickAction.ACTION_PLUS_ONE, o));
             ImageButton stop = (ImageButton)v. findViewById(R.id.timer_stop);
             stop.setOnClickListener(TimerFragment.this);
             stop.setTag(new ClickAction(ClickAction.ACTION_STOP, o));
             TimerFragment.this.setTimerButtons(o);
             return v;
         }
 
         public void addTimer(TimerObj t) {
             mTimers.add(0, t);
             notifyDataSetChanged();
         }
 
         public void onSaveInstanceState(Bundle outState) {
             TimerObj.putTimersInSharedPrefs(mmPrefs, mTimers);
         }
 
         public void onRestoreInstanceState(Bundle outState) {
             TimerObj.getTimersFromSharedPrefs(mmPrefs, mTimers);
             notifyDataSetChanged();
         }
 
         public void saveGlobalState() {
             TimerObj.putTimersInSharedPrefs(mmPrefs, mTimers);
         }
     }
 
     class TimesUpListAdapter extends TimersListAdapter {
 
         public TimesUpListAdapter(Context context, SharedPreferences prefs) {
             super(context, prefs);
         }
 
         @Override
         public void onSaveInstanceState(Bundle outState) {
             // This adapter has a data subset and never updates entire database
             // Individual timers are updated in button handlers.
         }
 
         @Override
         public void saveGlobalState() {
             // This adapter has a data subset and never updates entire database
             // Individual timers are updated in button handlers.
         }
 
         @Override
         public void onRestoreInstanceState(Bundle outState) {
             // This adapter loads a subset
             TimerObj.getTimersFromSharedPrefs(mmPrefs, mTimers, TimerObj.STATE_TIMESUP);
 
             if (getCount() == 0) {
                 mOnEmptyListListener.onEmptyList();
             } else {
                 Collections.sort(mTimers, new Comparator<TimerObj>() {
                     @Override
                     public int compare(TimerObj o1, TimerObj o2) {
                        return (int)(o1.mTimeLeft - o2.mTimeLeft);
                     }
                 });
             }
         }
     }
 
     private final Runnable mClockTick = new Runnable() {
         boolean mVisible = true;
         final static int TIME_PERIOD_MS = 1000;
         final static int SPLIT = TIME_PERIOD_MS / 2;
 
         @Override
         public void run() {
             // Setup for blinking
             boolean visible = Utils.getTimeNow() % TIME_PERIOD_MS < SPLIT;
             boolean toggle = mVisible != visible;
             mVisible = visible;
             for (int i = 0; i < mAdapter.getCount(); i ++) {
                 TimerObj t = (TimerObj) mAdapter.getItem(i);
                 if (t.mState == TimerObj.STATE_RUNNING || t.mState == TimerObj.STATE_TIMESUP) {
                     long timeLeft = t.updateTimeLeft(false);
                     if ((TimerListItem)(t.mView) != null) {
                         ((TimerListItem)(t.mView)).setTime(timeLeft, false);
                     }
                 }
                 if (t.mTimeLeft <= 0 && t.mState != TimerObj.STATE_DONE
                         && t.mState != TimerObj.STATE_RESTART) {
                     t.mState = TimerObj.STATE_TIMESUP;
                     TimerFragment.this.setTimerButtons(t);
                     if ((TimerListItem)(t.mView) != null) {
                         ((TimerListItem)(t.mView)).timesUp();
                     }
                 }
 
                 // The blinking
                 if (toggle && (TimerListItem)(t.mView) != null) {
                     if (t.mState == TimerObj.STATE_TIMESUP) {
                         ((TimerListItem)(t.mView)).setCircleBlink(mVisible);
                     }
                     if (t.mState == TimerObj.STATE_STOPPED) {
                         ((TimerListItem)(t.mView)).setTextBlink(mVisible);
                     }
                 }
             }
             mTimersList.postDelayed(mClockTick, 20);
         }
     };
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
         // Inflate the layout for this fragment
         View v = inflater.inflate(R.layout.timer_fragment, container, false);
 
         // Handle arguments from parent
         Bundle bundle = getArguments();
         if (bundle != null && bundle.containsKey(Timers.TIMESUP_MODE)) {
             if (bundle.getBoolean(Timers.TIMESUP_MODE, false)) {
                 try {
                     mOnEmptyListListener = (OnEmptyListListener) getActivity();
                 } catch (ClassCastException e) {
                     Log.wtf(TAG, getActivity().toString() + " must implement OnEmptyListListener");
                 }
             }
         }
 
         mTimersList = (ListView)v.findViewById(R.id.timers_list);
         mNewTimerPage = v.findViewById(R.id.new_timer_page);
         mTimersListPage = v.findViewById(R.id.timers_list_page);
         mTimerSetup = (TimerSetupView)v.findViewById(R.id.timer_setup);
         mCancel = (Button)v.findViewById(R.id.timer_cancel);
         mCancel.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 if (mAdapter.getCount() != 0) {
                     gotoTimersView();
                 }
             }
         });
         mStart = (Button)v.findViewById(R.id.timer_start);
         mStart.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 // New timer create if timer length is not zero
                 // Create a new timer object to track the timer and
                 // switch to the timers view.
                 int timerLength = mTimerSetup.getTime();
                 if (timerLength == 0) {
                     return;
                 }
                 TimerObj t = new TimerObj(timerLength * 1000);
                 t.mState = TimerObj.STATE_RUNNING;
                 mAdapter.addTimer(t);
                 updateTimersState(t, Timers.START_TIMER);
                 gotoTimersView();
             }
 
         });
         mAddTimer = (ImageButton)v.findViewById(R.id.timer_add_timer);
         mAddTimer.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 mTimerSetup.reset();
                 gotoSetupView();
             }
 
         });
         mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
         mNotificationManager = (NotificationManager)
                 getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
 
         return v;
     }
 
     @Override
     public void onResume() {
         super.onResume();
         mPrefs.registerOnSharedPreferenceChangeListener(this);
 
         mAdapter = createAdapter(getActivity(), mPrefs);
         mAdapter.onRestoreInstanceState(null);
 
         if (mPrefs.getBoolean(Timers.FROM_NOTIFICATION, false)) {
             // We need to know if this onresume is being called by the user clicking a
             // buzzing timer notification. If so, we need to set that timer to have "stopped"
             // at the moment the notification was hit.
             long now = mPrefs.getLong(Timers.NOTIF_TIME, System.currentTimeMillis());
             int timerId = mPrefs.getInt(Timers.NOTIF_ID, -1);
             if (timerId != -1) {
                 TimerObj t = Timers.findTimer(mAdapter.mTimers, timerId);
                 t.mTimeLeft = t.mOriginalLength - (now - t.mStartTime);
                 cancelTimerNotification(timerId);
             }
             SharedPreferences.Editor editor = mPrefs.edit();
             editor.putBoolean(Timers.FROM_NOTIFICATION, false);
             editor.apply();
         }
 
         mTimersList.setAdapter(mAdapter);
         if (mAdapter.getCount() == 0) {
            mCancel.setVisibility(View.INVISIBLE);
         }

         setPage();
     }
 
     @Override
     public void onPause() {
         super.onPause();
         stopClockTicks();
         saveGlobalState();
         mPrefs.unregisterOnSharedPreferenceChangeListener(this);
     }
 
     @Override
     public void onSaveInstanceState (Bundle outState) {
         super.onSaveInstanceState(outState);
         if (mAdapter != null) {
             mAdapter.onSaveInstanceState (outState);
         }
     }
 
     @Override
     public void saveGlobalState () {
         super.saveGlobalState();
         if (mAdapter != null) {
             mAdapter.saveGlobalState ();
         }
     }
 
     public void setPage() {
         if (mAdapter.getCount() != 0) {
             gotoTimersView();
         } else {
             gotoSetupView();
         }
     }
 
     public void stopAllTimesUpTimers() {
         while (0 < mAdapter.getCount()) {
             TimerObj timerObj = (TimerObj) mAdapter.getItem(0);
             if (timerObj.mState == TimerObj.STATE_TIMESUP) {
                 onStopButtonPressed(timerObj);
             }
         }
     }
 
     private void gotoSetupView() {
         mNewTimerPage.setVisibility(View.VISIBLE);
         mTimersListPage.setVisibility(View.GONE);
         stopClockTicks();
         if (mAdapter.getCount() == 0) {
            mCancel.setVisibility(View.INVISIBLE);
         } else {
             mCancel.setVisibility(View.VISIBLE);
         }
     }
     private void gotoTimersView() {
         mNewTimerPage.setVisibility(View.GONE);
         mTimersListPage.setVisibility(View.VISIBLE);
         startClockTicks();
     }
 
     @Override
     public void onClick(View v) {
         ClickAction tag = (ClickAction) v.getTag();
         onClickHelper(tag);
     }
 
     private void onClickHelper(ClickAction clickAction) {
         switch (clickAction.mAction) {
             case ClickAction.ACTION_DELETE:
                 TimerObj t = clickAction.mTimer;
                 if (t.mState == TimerObj.STATE_TIMESUP) {
                     cancelTimerNotification(t.mTimerId);
                 }
                 mAdapter.deleteTimer(t.mTimerId);
                 if (mAdapter.getCount() == 0) {
                     if (mOnEmptyListListener == null) {
                         mTimerSetup.reset();
                         gotoSetupView();
                     } else {
                         mOnEmptyListListener.onEmptyList();
                     }
                 }
                 // Tell receiver the timer was deleted.
                 // It will stop all activity related to the timer
                 updateTimersState(t, Timers.DELETE_TIMER);
                 break;
             case ClickAction.ACTION_PLUS_ONE:
                 onPlusOneButtonPressed(clickAction.mTimer);
                 setTimerButtons(clickAction.mTimer);
                 break;
             case ClickAction.ACTION_STOP:
                 onStopButtonPressed(clickAction.mTimer);
                 setTimerButtons(clickAction.mTimer);
                 break;
             default:
                 break;
         }
     }
 
     private void onPlusOneButtonPressed(TimerObj t) {
         switch(t.mState) {
             case TimerObj.STATE_RUNNING:
                  t.addTime(60000); //60 seconds in millis
                  long timeLeft = t.updateTimeLeft(false);
                  ((TimerListItem)(t.mView)).setTime(timeLeft, false);
                  ((TimerListItem)(t.mView)).setLength(timeLeft);
                  mAdapter.notifyDataSetChanged();
                  updateTimersState(t, Timers.TIMER_UPDATE);
                 break;
             case TimerObj.STATE_TIMESUP:
                 // +1 min when the time is up will restart the timer with 1 minute left.
                 t.mState = TimerObj.STATE_RUNNING;
                 t.mStartTime = System.currentTimeMillis();
                 t.mTimeLeft = t. mOriginalLength = 60000;
                 ((TimerListItem)t.mView).setTime(t.mTimeLeft, false);
                 ((TimerListItem)t.mView).set(t.mOriginalLength, t.mTimeLeft, true);
                 ((TimerListItem) t.mView).start();
                 updateTimersState(t, Timers.TIMER_RESET);
                 updateTimersState(t, Timers.START_TIMER);
                 updateTimesUpMode(t);
                 cancelTimerNotification(t.mTimerId);
                 break;
             case TimerObj.STATE_STOPPED:
             case TimerObj.STATE_DONE:
                 t.mState = TimerObj.STATE_RESTART;
                 t.mTimeLeft = t. mOriginalLength = t.mSetupLength;
                 ((TimerListItem)t.mView).stop();
                 ((TimerListItem)t.mView).setTime(t.mTimeLeft, false);
                 ((TimerListItem)t.mView).set(t.mOriginalLength, t.mTimeLeft, false);
                 updateTimersState(t, Timers.TIMER_RESET);
                 break;
             default:
                 break;
         }
     }
 
 
 
 
     private void onStopButtonPressed(TimerObj t) {
         switch(t.mState) {
             case TimerObj.STATE_RUNNING:
                 // Stop timer and save the remaining time of the timer
                 t.mState = TimerObj.STATE_STOPPED;
                 ((TimerListItem) t.mView).pause();
                 t.updateTimeLeft(true);
                 updateTimersState(t, Timers.TIMER_STOP);
                 break;
             case TimerObj.STATE_STOPPED:
                 // Reset the remaining time and continue timer
                 t.mState = TimerObj.STATE_RUNNING;
                 t.mStartTime = System.currentTimeMillis() - (t.mOriginalLength - t.mTimeLeft);
                 ((TimerListItem) t.mView).start();
                 updateTimersState(t, Timers.START_TIMER);
                 break;
             case TimerObj.STATE_TIMESUP:
                 t.mState = TimerObj.STATE_DONE;
                 ((TimerListItem) t.mView).done();
                 updateTimersState(t, Timers.TIMER_DONE);
                 cancelTimerNotification(t.mTimerId);
                 updateTimesUpMode(t);
                 break;
             case TimerObj.STATE_DONE:
                 break;
             case TimerObj.STATE_RESTART:
                 t.mState = TimerObj.STATE_RUNNING;
                 t.mStartTime = System.currentTimeMillis() - (t.mOriginalLength - t.mTimeLeft);
                 ((TimerListItem) t.mView).start();
                 updateTimersState(t, Timers.START_TIMER);
                 break;
             default:
                 break;
         }
     }
 
     private void setTimerButtons(TimerObj t) {
         Context a = getActivity();
         if (a == null || t == null || t.mView == null) {
             return;
         }
         ImageButton plusOne = (ImageButton) t.mView.findViewById(R.id.timer_plus_one);
         ImageButton stop = (ImageButton) t.mView.findViewById(R.id.timer_stop);
         CountingTimerView countingTimerView = (CountingTimerView)
                 t.mView.findViewById(R.id.timer_time_text);
         Resources r = a.getResources();
         switch (t.mState) {
             case TimerObj.STATE_RUNNING:
                 plusOne.setVisibility(View.VISIBLE);
                 plusOne.setContentDescription(r.getString(R.string.timer_plus_one));
                 plusOne.setImageResource(R.drawable.ic_plusone);
                 stop.setContentDescription(r.getString(R.string.timer_stop));
                 stop.setImageResource(R.drawable.ic_stop_normal);
                 stop.setEnabled(true);
                 countingTimerView.setVirtualButtonEnabled(true);
                 break;
             case TimerObj.STATE_STOPPED:
                 plusOne.setVisibility(View.VISIBLE);
                 plusOne.setContentDescription(r.getString(R.string.timer_reset));
                 plusOne.setImageResource(R.drawable.ic_reset);
                 stop.setContentDescription(r.getString(R.string.timer_start));
                 stop.setImageResource(R.drawable.ic_start_normal);
                 stop.setEnabled(true);
                 countingTimerView.setVirtualButtonEnabled(true);
                 break;
             case TimerObj.STATE_TIMESUP:
                 plusOne.setVisibility(View.VISIBLE);
                 plusOne.setImageResource(R.drawable.ic_plusone);
                 stop.setContentDescription(r.getString(R.string.timer_stop));
                 stop.setEnabled(true);
                 countingTimerView.setVirtualButtonEnabled(true);
                 break;
             case TimerObj.STATE_DONE:
                 plusOne.setVisibility(View.VISIBLE);
                 plusOne.setContentDescription(r.getString(R.string.timer_reset));
                 plusOne.setImageResource(R.drawable.ic_reset);
                 stop.setContentDescription(r.getString(R.string.timer_start));
                 stop.setImageResource(R.drawable.ic_start_disabled);
                 stop.setEnabled(false);
                 countingTimerView.setVirtualButtonEnabled(false);
                 break;
             case TimerObj.STATE_RESTART:
                 plusOne.setVisibility(View.INVISIBLE);
                 stop.setContentDescription(r.getString(R.string.timer_start));
                 stop.setImageResource(R.drawable.ic_start_normal);
                 stop.setEnabled(true);
                 countingTimerView.setVirtualButtonEnabled(true);
                 break;
             default:
                 break;
         }
     }
 
     private void startClockTicks() {
         mTimersList.postDelayed(mClockTick, 20);
         mTicking = true;
     }
     private void stopClockTicks() {
         if (mTicking) {
             mTimersList.removeCallbacks(mClockTick);
             mTicking = false;
         }
     }
 
     private void updateTimersState(TimerObj t, String action) {
         if (!Timers.DELETE_TIMER.equals(action)) {
             t.writeToSharedPref(mPrefs);
         }
         Intent i = new Intent();
         i.setAction(action);
         i.putExtra(Timers.TIMER_INTENT_EXTRA, t.mTimerId);
         getActivity().sendBroadcast(i);
     }
 
     private void cancelTimerNotification(int timerId) {
         mNotificationManager.cancel(timerId);
     }
 
     private void updateTimesUpMode(TimerObj timerObj) {
         if (mOnEmptyListListener != null && timerObj.mState != TimerObj.STATE_TIMESUP) {
             mAdapter.removeTimer(timerObj);
             if (mAdapter.getCount() == 0) {
                 mOnEmptyListListener.onEmptyList();
             }
         }
     }
 
     public void restartAdapter() {
         mAdapter = createAdapter(getActivity(), mPrefs);
         mAdapter.onRestoreInstanceState(null);
     }
 
     @Override
     public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
         if (prefs.equals(mPrefs)) {
             if ( (key.equals(Timers.FROM_NOTIFICATION) || key.equals(Timers.NOTIF_ID)
                     || key.equals(Timers.NOTIF_TIME)) &&
                     prefs.getBoolean(Timers.FROM_NOTIFICATION, false) ) {
                 // We need to know if the user has clicked the buzzing timer notification
                 // while the fragment is still open. If so, this listener will catch that event,
                 // and allow the timers to be re-instated based on the updated stop time.
                 // Because this method gets called with every change to the sharedprefs, we ensure
                 // that we only recalculate the timers if the change was specifically set by the
                 // user interacting with the notification.
                 long now = prefs.getLong(Timers.NOTIF_TIME, System.currentTimeMillis());
                 int timerId = prefs.getInt(Timers.NOTIF_ID, -1);
                 mAdapter = createAdapter(getActivity(), mPrefs);
                 mAdapter.onRestoreInstanceState(null);
                 if (timerId != -1) {
                     TimerObj t = Timers.findTimer(mAdapter.mTimers, timerId);
                     t.mTimeLeft = t.mOriginalLength - (now - t.mStartTime);
                     cancelTimerNotification(timerId);
                 }
                 mTimersList.setAdapter(mAdapter);
                 SharedPreferences.Editor editor = prefs.edit();
                 editor.putBoolean(Timers.FROM_NOTIFICATION, false);
                 editor.apply();
             }
         }
     }
 
 
 }
