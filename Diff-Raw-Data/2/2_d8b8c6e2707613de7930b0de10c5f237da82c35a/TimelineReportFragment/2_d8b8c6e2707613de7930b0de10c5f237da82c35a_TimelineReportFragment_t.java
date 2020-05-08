 package com.timetracker.ui.activities;
 
 import android.app.Activity;
 import android.graphics.Color;
 import android.graphics.drawable.GradientDrawable;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import com.j256.ormlite.android.apptools.OpenHelperManager;
 import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
 import com.timetracker.R;
 import com.timetracker.domain.Task;
 import com.timetracker.domain.TaskSwitchEvent;
 import com.timetracker.domain.persistance.DatabaseHelper;
 
 import java.sql.SQLException;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 /**
  * Created by Anton Chernetskij
  */
 public class TimelineReportFragment extends Fragment {
 
     public static final String ARG_OBJECT = "object";
     private View view;
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
             Bundle savedInstanceState) {
         view = inflater.inflate(R.layout.timeline_report, container, false);
         Bundle args = getArguments();
         Date date = new Date(args.getLong(ARG_OBJECT));
 
         int hourHeight = 60;    //todo add zoom
         initTimeRuler(hourHeight);
         initTasksStack(hourHeight, date);
         return view;
     }
 
     private void initTimeRuler(int hourHeight) {
         LinearLayout ruler = (LinearLayout) view.findViewById(R.id.timeRuler);
 
         for (int i = 0; i < 24; i++) {
             TextView textView = new TextView(view.getContext());
             textView.setText(String.format("%2d:00", i));
             textView.setHeight(hourHeight);
             ruler.addView(textView);
         }
     }
 
     private void initTasksStack(int hourHeight, Date date) {
         try {
             List<TaskSwitchEvent> events = getHelper().getTaskEvents(date, date);
 
             if (events.isEmpty()) {
                 return;
             }
 
             Task currentTask = null;
             Integer firstEventId = events.get(0).id;
             if (firstEventId > 1) {
                 TaskSwitchEvent previousEvent = getHelper().getEventsDao().queryForId(firstEventId - 1);
                 currentTask = previousEvent.task;
             }
             int hours = 0;
             int minutes = 0;
 
             LinearLayout tasksLine = (LinearLayout) view.findViewById(R.id.tasksLine);
             for (TaskSwitchEvent event : events) {
                 TextView taskBox = new TextView(view.getContext());
                 if (currentTask != null) {
                     taskBox.setText(currentTask.name);
                     taskBox.setBackground(getFill(currentTask.color));
                 } else {
                     taskBox.setBackground(getFill(0));
                 }
                 Calendar calendar = Calendar.getInstance();
                 calendar.setTime(event.switchTime);
                 taskBox.setHeight((int) (((calendar.get(Calendar.HOUR_OF_DAY) - hours) * 60.0 + (calendar.get(Calendar.MINUTE) - minutes)) * hourHeight / 60));
                 taskBox.setGravity(Gravity.CENTER);
                 tasksLine.addView(taskBox, getLayoutParams());
 
                 hours = calendar.get(Calendar.HOUR_OF_DAY);
                 minutes = calendar.get(Calendar.MINUTE);
                 currentTask = event.task;
             }
             TextView taskBox = new TextView(view.getContext());
             if (currentTask != null) {
                 taskBox.setText(currentTask.name);
                 taskBox.setBackground(getFill(currentTask.color));
             } else {
                 taskBox.setBackground(getFill(0));
             }
             Calendar calendar = Calendar.getInstance();
             taskBox.setHeight((int) (((calendar.get(Calendar.HOUR_OF_DAY) - hours) * 60.0 + (calendar.get(Calendar.MINUTE) - minutes)) * hourHeight / 60));
             taskBox.setGravity(Gravity.CENTER);
 
             tasksLine.addView(taskBox, getLayoutParams());
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
     }
 
     private GradientDrawable getFill(int color) {
         GradientDrawable drawable = new GradientDrawable();
         drawable.setShape(GradientDrawable.RECTANGLE);
         drawable.setStroke(1, Color.WHITE);
         drawable.setColor(color);
//        drawable.setAlpha(Task.DEFAULT_COLOR_ALPHA);     todo fix performance issue here
         return drawable;
     }
 
     private LinearLayout.LayoutParams getLayoutParams() {
         LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                 ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
         params.gravity = Gravity.CENTER;
         return params;
     }
 
     private DatabaseHelper getHelper(){
         return OpenHelperManager.getHelper(view.getContext(), DatabaseHelper.class);
     }
 }
