 package org.jonblack.bluetrack.activities;
 
 import org.joda.time.DateTime;
 import org.joda.time.Period;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 import org.joda.time.format.PeriodFormat;
 import org.jonblack.bluetrack.R;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.CursorAdapter;
 import android.widget.TextView;
 
 public class SessionListCursorAdapter extends CursorAdapter
 {
   public SessionListCursorAdapter(Context context, Cursor c, int flags)
   {
     super(context, c, flags);
   }
   
   @Override
   public View newView(Context context, Cursor cursor, ViewGroup parent)
   {
     LayoutInflater inflater = LayoutInflater.from(context);
     return inflater.inflate(R.layout.session_list_row, parent, false);
   }
   
   @Override
   public void bindView(View view, Context context, Cursor cursor)
   {
     int idStartDateTime = cursor.getColumnIndexOrThrow("start_date_time");
    int idEndDateTime = cursor.getColumnIndexOrThrow("end_date_time");
     
     String startDateTimeStr = cursor.getString(idStartDateTime);
    String endDateTimeStr = cursor.getString(idEndDateTime);
    
    // Format start date time for locale
    DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
     
     DateTime startDateTime = fmt.parseDateTime(startDateTimeStr);
    DateTime endDateTime = fmt.parseDateTime(endDateTimeStr);
    
     assert(startDateTime != null);
    assert(endDateTime != null);
     
     TextView tvStartDateTime = (TextView) view.findViewById(R.id.session_row_start_date_time);
     tvStartDateTime.setText(startDateTime.toString("EEE dd MMM, YYYY @ HH:mm:ss", null));
     
     // Calculate duration
    Period period = new Period(startDateTime, endDateTime);
    
    TextView tvDuration = (TextView) view.findViewById(R.id.session_row_duration);
    tvDuration.setText(period.toString(PeriodFormat.getDefault()));
   }
 }
