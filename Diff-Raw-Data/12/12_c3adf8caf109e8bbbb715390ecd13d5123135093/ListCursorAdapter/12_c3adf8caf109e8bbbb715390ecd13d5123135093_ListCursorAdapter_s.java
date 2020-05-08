 /**
  * 
  */
 package open.gtaskdroid.adaptors;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 
 import open.Gtaskdroid.R;
 import open.gtaskdroid.activity.EventEditActivity;
 import open.gtaskdroid.dataaccess.EventsDbAdapter;
 import android.content.Context;
 import android.database.Cursor;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.SimpleCursorAdapter;
 import android.widget.TextView;
 
 /**
  * @author rajith
  *
  */
 public class ListCursorAdapter extends SimpleCursorAdapter {
 
 		
 	private static final String DATE_FORMAT="MMM dd, yy";
 	private static final String DATE_OF_THE_WEEK_FORMAT="EEE";
 	private static final String TIME_FORMAT="HH:mm";
 	
 	private int layout;
 	private Calendar mToday;
 	private Calendar mEventDate;
 	
 	public ListCursorAdapter(Context context, int layout, Cursor c,
 			String[] from, int[] to) {
 		super(context, layout, c, from, to);
 		this.layout=layout;
 		mToday=Calendar.getInstance();
 		mEventDate=Calendar.getInstance();
 	}
 
 	/* (non-Javadoc)
 	 * @see android.widget.CursorAdapter#bindView(android.view.View, android.content.Context, android.database.Cursor)
 	 */
 	@Override
 	public void bindView(View v, Context context, Cursor cursor) {		
 		updateRow(cursor, v);
 	 }
 
 	/* (non-Javadoc)
 	 * @see android.widget.CursorAdapter#newView(android.content.Context, android.database.Cursor, android.view.ViewGroup)
 	 */
 	@Override
 	public View newView(Context context, Cursor cursor, ViewGroup parent) {
 		
 		
 		final LayoutInflater inflater = LayoutInflater.from(context);
 		View v = inflater.inflate(layout, parent, false);
 		updateRow(cursor, v);
 		return v;
 
 	}
 
 	/**
 	 * @param cursor
 	 * @param v
 	 */
 	private void updateRow(Cursor cursor, View v) {
 		
 		String title=cursor.getString(cursor.getColumnIndexOrThrow(EventsDbAdapter.KEY_TITLE));
 		String dateDue=cursor.getString(cursor.getColumnIndexOrThrow(EventsDbAdapter.KEY_EVENT_START_DATE_TIME));
 		int eventStatus=cursor.getInt(cursor.getColumnIndexOrThrow(EventsDbAdapter.KEY_STATUS));
 		  /**
 		  * Next set the name of the entry.
 		  */    
 		TextView taskTitleView= (TextView) v.findViewById(R.id.taskTitle);
 		TextView taskDueTimeView= (TextView) v.findViewById(R.id.taskDueTime);
 		TextView taskDueDateView= (TextView) v.findViewById(R.id.taskDueDate);
 		 
 		taskTitleView.setText(title);
 		
 		
 		if(eventStatus!=0){
 			v.setBackgroundColor(0x00000000);
 		}else{
 			v.setBackgroundColor(0xff444444);
 		}
 		
 		if(!" ".equalsIgnoreCase(dateDue)){	 
 		SimpleDateFormat dateTimeFormat = new SimpleDateFormat(EventEditActivity.DATE_TIME_FORMAT);
 		SimpleDateFormat dateDueFormat = new SimpleDateFormat(DATE_FORMAT);
 		SimpleDateFormat dueTimeFormat = new SimpleDateFormat(TIME_FORMAT);
 		SimpleDateFormat dueDateOfWeekFormat = new SimpleDateFormat(DATE_OF_THE_WEEK_FORMAT);
 		
 				
 				Date date = null;
 				
 				try {
 					date = dateTimeFormat.parse(dateDue);
 					mEventDate.setTime(date);
					if(mToday.get(Calendar.DAY_OF_MONTH)>mEventDate.get(Calendar.DAY_OF_MONTH)){
 						taskDueTimeView.setText(dateDueFormat.format(date));
 						taskDueTimeView.setTextColor(0xffff0000);
 						taskDueDateView.setText(dueDateOfWeekFormat.format(date));
					}else if(mToday.get(Calendar.DAY_OF_MONTH)==mEventDate.get(Calendar.DAY_OF_MONTH)){
 						taskDueTimeView.setText(dueTimeFormat.format(date));
 						taskDueTimeView.setTextColor(0xffffffff);
 						taskDueDateView.setText("Today");
 					}else {
 						taskDueTimeView.setText(dateDueFormat.format(date));
 						taskDueTimeView.setTextColor(0xffffffff);
 						taskDueDateView.setText(dueDateOfWeekFormat.format(date));
 					}
 
 				} catch (ParseException e) {
 					e.printStackTrace();
 					Log.e("listCursor", "here");
 				}
 		}else {
 			taskDueTimeView.setText(" ");
 			taskDueDateView.setText(" ");
 		}
 	}
 
 }
