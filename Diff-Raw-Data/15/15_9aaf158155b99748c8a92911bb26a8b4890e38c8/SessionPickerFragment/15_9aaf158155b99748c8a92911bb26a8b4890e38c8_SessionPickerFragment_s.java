 package org.devnexus.fragments;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.database.Cursor;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 
 import com.google.gson.Gson;
 
 import org.devnexus.R;
 import org.devnexus.adapters.SessionAdapter;
 import org.devnexus.util.GsonUtils;
 import org.devnexus.util.SessionPickerReceiver;
 import org.devnexus.vo.Schedule;
 import org.devnexus.vo.ScheduleItem;
 import org.devnexus.vo.UserCalendar;
 import org.devnexus.vo.contract.ScheduleContract;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 /**
  * Created by summers on 12/11/13.
  */
 public class SessionPickerFragment extends DialogFragment {
 
 
     private static final DateFormat format = new SimpleDateFormat("h:mm a");
 
 
     private static final String SCHEDULE = "SessionPickerFragment.SCHEDULE";
     private static final String CALENDAR = "SessionPickerFragment.CALENDAR";
     private static final String DATE = "SessionPickerFragment.DATE";
     private static final String TAG = SessionPickerFragment.class.getSimpleName();
     private SessionAdapter adapter;
     private UserCalendar calendarItem;
    private static List<ScheduleItem> schedule;
     private SessionPickerReceiver receiver;
     private Date time;
     private ListView listView;
     private ProgressBar progress;
 
     private static final Gson GSON = GsonUtils.GSON;
     private LoaderTask loaderTask = null;
 
     public static SessionPickerFragment newInstance(UserCalendar calendarItem) {
         SessionPickerFragment fragment = new SessionPickerFragment();
         Bundle args = new Bundle();
         args.putSerializable(DATE, calendarItem.fromTime);
         args.putSerializable(CALENDAR, calendarItem);
         fragment.setArguments(args);
         return fragment;
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         if (savedInstanceState != null && savedInstanceState.get(SCHEDULE) != null) {
             schedule = (List<ScheduleItem>) savedInstanceState.get(SCHEDULE);
         }
 
     }
 
     @Override
     public Dialog onCreateDialog(Bundle savedInstanceState) {
         Dialog dialog = super.onCreateDialog(savedInstanceState);
         dialog.setTitle("Sessions");
         return dialog;
     }
 
     public void setReceiver(SessionPickerReceiver receiver) {
         this.receiver = receiver;
     }
 
     @Override
     public void onAttach(Activity activity) {
         super.onAttach(activity);
 
 
         Bundle args = getArguments();
         time = (Date) args.getSerializable(DATE);
         calendarItem = (UserCalendar) args.getSerializable(CALENDAR);
 
         if (adapter == null) {
             adapter = new SessionAdapter(activity.getApplicationContext(), R.layout.schedule_list_item);
         }
 
 
         if (listView != null) {
             listView.requestLayout();
             listView.refreshDrawableState();
         }
 
         if (activity instanceof SessionPickerReceiver) {
             this.receiver = (SessionPickerReceiver) activity;
         }
 
     }
 
     @Override
     public void onResume() {
         super.onResume();
         loaderTask = new LoaderTask();
         if (schedule != null) {
             adapter.clear();
             for (ScheduleItem item : schedule) {
                 adapter.add(item);
             }
 
             adapter.notifyDataSetChanged();
             if (listView != null) {
                 progress.setVisibility(View.GONE);
                 listView.getParent().requestLayout();
                 listView.refreshDrawableState();
             }
         } else {
             loaderTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
         }
 
     }
 
     @Override
     public void onPause() {
         super.onPause();
         loaderTask.cancel(true);
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         View view = inflater.inflate(R.layout.session_picker, null);
         progress = (ProgressBar) view.findViewById(R.id.progress);
         listView = (ListView) view.findViewById(R.id.listView);
         listView.setAdapter(adapter);
 
         listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                 if (receiver != null) {
                     receiver.receiveSessionItem(calendarItem, adapter.getItem(position));
                     dismiss();
                 }
             }
         });
 
         return view;
     }
 
     private class LoaderTask extends AsyncTask<Void, Void, Void> {
 
         @Override
         protected Void doInBackground(Void... params) {
 
             Cursor cursor = null;
             try {
                cursor = getActivity().getContentResolver().query(ScheduleContract.URI, null, null, null, null);
                 ArrayList schedule = new ArrayList<ScheduleItem>(10);
                 if (cursor != null && cursor.moveToNext()) {
                     Schedule scheduleFromDb = GSON.fromJson(cursor.getString(0), Schedule.class);
                     for (ScheduleItem scheduleItem : scheduleFromDb.scheduleItemList.scheduleItems) {
                         if (time == null) {
                             Log.e(TAG, "time is null!!!");
                         }
                         Log.e(TAG, format.format(scheduleItem.fromTime) + " vs " + format.format(time));
                         if (scheduleItem.fromTime.equals(time)) {
                             schedule.add(scheduleItem);
                         }
                     }
                 } else {
                     //???
                 }
                SessionPickerFragment.schedule = schedule;
 
             } finally {
                 if (cursor != null) {
                     cursor.close();
                 }
             }
 
 
             return null;
         }
 
 
         @Override
         protected void onPostExecute(Void aVoid) {
             if (!isCancelled()) {
                 adapter.clear();
                 for (ScheduleItem item : schedule) {
                     adapter.add(item);
                 }
 
                 adapter.notifyDataSetChanged();
                 if (listView != null) {
                     progress.setVisibility(View.GONE);
                     listView.getParent().requestLayout();
                     listView.refreshDrawableState();
                 }
             }
         }
     }
 
 }
