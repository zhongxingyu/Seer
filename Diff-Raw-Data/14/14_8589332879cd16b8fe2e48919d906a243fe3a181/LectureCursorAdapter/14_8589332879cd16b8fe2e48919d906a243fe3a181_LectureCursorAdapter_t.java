 package com.tobbentm.higreader;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.graphics.Color;
import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.CursorAdapter;
 import android.widget.TextView;
 
 import com.tobbentm.higreader.db.DBHelper;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 /**
  * Created by Tobias on 31.08.13.
  */
 public class LectureCursorAdapter extends CursorAdapter {
 
     private LayoutInflater inflater;
    private final SimpleDateFormat orgDate = new SimpleDateFormat("yyyy-MM-dd");
     private final SimpleDateFormat newDate = new SimpleDateFormat("EEE dd/MM");
 
     public LectureCursorAdapter(Context context, Cursor c, int flags) {
         super(context, c, flags);
         inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
     }
 
     @Override
     public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
         return inflater.inflate(R.layout.lecture_list_item, viewGroup, false);
     }
 
     @Override
     public void bindView(View view, Context context, Cursor cursor) {
         Date date = new Date();
         try {
            date = orgDate.parse(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_DATE)).replaceAll("^.{4}", ""));
         } catch (ParseException e) {
             e.printStackTrace();
         }
         String currentDate = newDate.format(date);
 
         if(currentDate.contains(newDate.format(new Date()))){
             currentDate = "Today";
         }
 
         if(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_NAME)) != null &&
                 cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_NAME)).contains("HIGREADER") ){
             view.findViewById(R.id.time_container).setVisibility(View.GONE);
             view.findViewById(R.id.text_container).setVisibility(View.GONE);
             TextView tvDate = (TextView) view.findViewById(R.id.text_date);
             tvDate.setVisibility(View.VISIBLE);
 
             tvDate.setText(currentDate);
             view.setBackgroundResource(R.drawable.list_date_background);
             //Log.d("BIND", "Binding date view, name="+cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_NAME)));
         }else{
             view.findViewById(R.id.text_date).setVisibility(View.GONE);
             view.findViewById(R.id.time_container).setVisibility(View.VISIBLE);
             view.findViewById(R.id.text_container).setVisibility(View.VISIBLE);
             TextView tvName = (TextView) view.findViewById(R.id.text_name);
             TextView tvRoom = (TextView) view.findViewById(R.id.text_room);
             TextView tvLecturer = (TextView) view.findViewById(R.id.text_lecturer);
             TextView tvTime = (TextView) view.findViewById(R.id.text_time);
 
             String time = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_TIME));
             String startTime, endTime;
             if(time.split("\n").length >= 3){
                 startTime = time.split("\n")[0].replace(":", "");
                 endTime = time.split("\n")[2].replace(":", "");
             }else{
                 startTime = "0000";
                 endTime = "0000";
             }
             SimpleDateFormat timef = new SimpleDateFormat("kkmm");
             String currenttime = timef.format(new Date());
             long t = date.getTime();
             String quartertime = timef.format(new Date(t-(15*60000)));
            Log.d("TIME", ""+t);
            Log.d("TIME", quartertime);
 
             tvName.setText(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_NAME)));
             tvRoom.setText("Room:\t\t"+cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_ROOM)));
             tvLecturer.setText("Lecturer:\t"+cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_LECTURER)));
             tvTime.setText(time);
             view.setBackgroundResource(R.drawable.list_item_background);
 
             Boolean today = currentDate.contains("Today");
             if(today &&
                     Integer.parseInt(startTime) <= Integer.parseInt(currenttime) &&
                     Integer.parseInt(endTime) >= Integer.parseInt(currenttime)){
                 tvName.setTextColor(Color.parseColor("#C21B1B"));
             } else if(today &&
                     Integer.parseInt(endTime) < Integer.parseInt(currenttime)){
                 tvName.setTextColor(Color.parseColor("#1ABD1A"));
             } else if(today &&
                    Integer.parseInt(quartertime) < Integer.parseInt(startTime) &&
                    Integer.parseInt(currenttime) < Integer.parseInt(startTime)){
                 tvName.setTextColor(Color.parseColor("#F0B12B"));
             } else {
                 tvName.setTextColor(tvRoom.getCurrentTextColor());
             }
 
             //Log.d("BIND", "Binding normal view, name="+cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_NAME)));
         }
     }
 }
