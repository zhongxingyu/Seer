 package com.ucb.dcm.list;
 
 import android.app.Activity;
 import android.content.Context;
 import android.database.Cursor;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.CursorAdapter;
 import android.widget.TextView;
 import com.emilsjolander.components.stickylistheaders.StickyListHeadersAdapter;
 import com.ucb.dcm.R;
 import com.ucb.dcm.data.Performance;
 import com.ucb.dcm.data.Show;
 import com.ucb.dcm.data.Venue;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 /**
  * Created by kurtguenther on 6/9/13.
  */
 public class VenueScheduleAdapter extends CursorAdapter implements StickyListHeadersAdapter {
 
     Cursor cursor;
     Activity activity;
 
 
     public VenueScheduleAdapter(android.content.Context context, android.database.Cursor c){
         super(context, c);
         this.activity = (Activity) context;
         this.cursor = c;
     }
 
 
     private class ScheduleHolder{
         TextView time;
         TextView name;
     }
 
     @Override
     public View newView(Context context, Cursor cursor, ViewGroup parent) {
         View retVal = activity.getLayoutInflater().inflate(R.layout.list_schedule, parent, false);
 
         ScheduleHolder sh = new ScheduleHolder();
         sh.time = (TextView) retVal.findViewById(R.id.schedule_time);
         sh.name = (TextView) retVal.findViewById(R.id.schedule_show_name);
         retVal.setTag(sh);
 
         bindView(retVal, context, cursor);
 
         return retVal;
     }
 
     @Override
     public void bindView(View view, Context context, Cursor cursor) {
 //        Performance p = new Performance(cursor);
 //        Show s = new Show(cursor);
 //
 //        int start_date = p.start_date;
 //        String name = s.name;
 
         int start_date = cursor.getInt(cursor.getColumnIndex("start_date"));
         String name = cursor.getString(cursor.getColumnIndex("name"));
 
         ScheduleHolder sh = (ScheduleHolder) view.getTag();
 
         SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
         sh.time.setText(sdf.format(new Date(((long)start_date) * 1000)));
 
         TextView showName = (TextView) view.findViewById(R.id.schedule_show_name);
         sh.name.setText(name);
     }
 
     @Override
     public View getHeaderView(int position, View convertView, ViewGroup parent) {
         if(convertView == null){
             convertView = activity.getLayoutInflater().inflate(R.layout.list_show_header, parent, false);
         }
         TextView title = (TextView) convertView.findViewById(R.id.header_title);
 
        Date date = new Date(); // TODO make this real
         SimpleDateFormat sdf = new SimpleDateFormat("EEEE");
         String headerTitle = sdf.format(date);
 
         title.setText(headerTitle);
         return convertView;
     }
 
     @Override
     public long getHeaderId(int position) {
        Date date = new Date(); //Todo
         return date.getDay();
     }
 }
