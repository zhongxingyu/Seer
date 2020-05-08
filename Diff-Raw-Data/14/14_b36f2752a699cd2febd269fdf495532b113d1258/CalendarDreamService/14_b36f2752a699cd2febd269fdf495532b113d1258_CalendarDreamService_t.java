 package calendar.daydream;
 
 import java.util.Calendar;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.content.ContentResolver;
 import android.content.ContentUris;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Handler;
 import android.os.Looper;
 import android.provider.CalendarContract.Instances;
 import android.service.dreams.DreamService;
 import android.util.Log;
 import android.view.TextureView;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.BaseAdapter;
 import android.widget.LinearLayout;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.TableLayout;
 import android.widget.TextView;
 import calendar.daydream.data.CalendarAttendee;
 import calendar.daydream.data.CalendarCursorAdapter;
 import calendar.daydream.data.CalendarCursorPresenter;
 import calendar.daydream.ui.AttendeeAdapter;
 import calendar.daydream.util.CalendarDreamContants;
 
 public class CalendarDreamService extends DreamService implements OnItemClickListener {
 
 	private Timer refreshTimer;
 	private Cursor cursor;
 	
 	@Override
 	public void onAttachedToWindow() {
 		super.onAttachedToWindow();
 
 		// Allow user touch
 		setInteractive(true);
 		
 		// Hide system UI
 		setFullscreen(false);
 
 		displayCalendarListView();
 	}
 
 	private void displayCalendarListView() {
 		setContentView(R.layout.calendar_dream);
 		ListView listView = (ListView) findViewById(R.id.cal_list_view);
 		listView.setOnItemClickListener(this);
 		
 		refreshView();
 	}
 
 	private Cursor getCalendarCursor() {
 		// Specify the date range you want to search for recurring
 		// event instances
 		Calendar beginTime = Calendar.getInstance();
 		long startMillis = beginTime.getTimeInMillis();
 		Calendar endTime = Calendar.getInstance();
 		endTime.add(Calendar.DATE, CalendarDreamContants.DAYS_TO_DISPLAY);
 		long endMillis = endTime.getTimeInMillis();
 
 		Cursor cur = null;
 		ContentResolver cr = getContentResolver();
 		
 		// Construct the query with the desired date range.
 		Uri.Builder builder = Instances.CONTENT_URI.buildUpon();
 		ContentUris.appendId(builder, startMillis);
 		ContentUris.appendId(builder, endMillis);
 		
 		// Note: All day events are not displayed
 		cur = cr.query(builder.build(),
 				CalendarCursorPresenter.CURSOR_PROJECTION, 
 				Instances.ALL_DAY + " = ? and " + Instances.END + " < ?", 
 				new String[] { "0", (endMillis + 50000) + "" }, 
 				Instances.BEGIN);
 		return cur;
 	}
 
 	@Override
 	public void onDreamingStarted() {
 		super.onDreamingStarted();
 		setupPeriodicRefresh();
 	}
 
 	private void setupPeriodicRefresh() {
 		refreshTimer = new Timer();
 		refreshTimer.schedule(new TimerTask() {
 			@Override
 			public void run() {
 				Handler handler = new Handler(Looper.getMainLooper());
 				handler.post(new Runnable() {
 					@Override
 					public void run() {
 						Log.i(CalendarDreamContants.DEBUG_TAG, "Refreshing dream view");
 						refreshView();
 					}
 				});
 			}
 		}, 10000, 60000); // updates each min
 	}
 
 	private void refreshView() {
 		if(cursor != null) {
 			cursor.close();
 		}
 		cursor = getCalendarCursor();
 		ListAdapter adapter = new CalendarCursorAdapter(this, cursor, true);
 		
 		ListView listView = (ListView) findViewById(R.id.cal_list_view);
 		if(listView != null) {
 			listView.setAdapter(adapter);	
 		}
 	}
 	
 	@Override
 	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 		CalendarCursorPresenter presenter = new CalendarCursorPresenter((Cursor)((ListView) parent).getItemAtPosition(position), this);
 		Log.i(CalendarDreamContants.DEBUG_TAG, "Item clicked " + presenter.getTitle());
 
 		this.setContentView(R.layout.calendar_item_detailed);
 		TextView titleView = (TextView) findViewById(R.id.cal_detailed_title);
 		titleView.setText(presenter.getTitle());
 		titleView.setTextColor(presenter.getColor());
		((TextView) findViewById(R.id.cal_detailed_subtitle)).setText(presenter.getDate());
		((TextView) findViewById(R.id.cal_detailed_time)).setText(presenter.getTime());
 		((TextView) findViewById(R.id.cal_detailed_summary)).setText(presenter.getLocation());
 		((TextView) findViewById(R.id.cal_detailed_duration)).setText(presenter.getDuration());
 		
 		AttendeeAdapter attendeesAdapter = new AttendeeAdapter(presenter.getAttendees(), this);
 		ListView attendeesView = (ListView) findViewById(R.id.cal_detailed_attendees);
 		attendeesView.setAdapter(attendeesAdapter);
 		
		//Need to do this or clicking on the attendees list does nothing ...
		attendeesView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				detailedViewClicked(null);
			}
		});
 	}
 
 	public void detailedViewClicked(View view) {
 		displayCalendarListView();
 	}
 	
 	@Override
 	public void onDreamingStopped() {
 		super.onDreamingStopped();
 		if(refreshTimer != null) {
 			refreshTimer.cancel();
 		}
 	}
 
 }
