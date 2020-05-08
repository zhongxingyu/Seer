 package clock.sched;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.text.format.DateFormat;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.BaseAdapter;
 import android.widget.Button;
 import android.widget.GridView;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import clock.Parse.ParseHandler;
 import clock.db.DbAdapter;
 import clock.db.Event;
 import clock.db.InvitedEvent;
 
 import com.parse.Parse;
 import com.parse.PushService;
 
 /*
  * Main Application activity.
  */
 public class CalendarView extends Activity implements OnClickListener 
 {
 	private enum eMenuItems {
 		EDIT,
 		DELETE,
 		INFO,
 		INVITE;
 
 		public static eMenuItems getById(int index) {
 			return values()[index];
 		}
 	}
 	
 	private static final int NEW_EVENT = 1;
 	private static final int INVITED_EVENT = 2;
 	private static final int INIT_DATA = 3;
 	private static final String APP_KEY = "2jo7e9GelT811A2KsuJDJsP6sV7eeDYg2Jskyy4v";
 	private static final String CLIENT_KEY = "5siGRhsEIOCimLy18zV9dv4ashRfJ9WPit2Y3Dmx";
 	private Button selectedDayMonthYearButton;
 	private Button currentMonth;
 	private ImageView prevMonth;
 	private ImageView nextMonth;
 	private ImageView newEventBtn;
 	private GridView calendarView;
 	private GridCellAdapter dayOfMonthAdapter;
 	private Calendar _calendar;
 	private int month, year;
 	private AlarmsManager alarmsManager;
 	private final DateFormat dateFormatter = new DateFormat();
 	private static final String dateTemplate = "MMMM yyyy";	
 	private ListView currentDayEventsList;
 	private DbAdapter dbAdapter;
 	
 	private String userName;
 	private String userChannel;
 	private ImageView invitationBtn;
 	private InvitedEvent[] waintingInvatation;
 	private String[] waintingInvatationList;
 	protected InvitedEvent confirmedEvent;
 	private int eventDayBeforeEdit;
 	private Event inEditEvent;
 	
 	/** 
 	 * Called when the activity is first created. 
 	 * */
 	@Override
 	public void onCreate(Bundle savedInstanceState) 
 	{
 		super.onCreate(savedInstanceState);
 		LocationHandler.setFirstLocationRequest(this);
 		setContentView(R.layout.simple_calendar_view);
 
 		initCalander();
 		initUI();
 		
 		registerForContextMenu(currentDayEventsList);
 		dbAdapter = new DbAdapter(this);
 		alarmsManager = new AlarmsManager(this, dbAdapter);
 		dayOfMonthAdapter = new GridCellAdapter(getApplicationContext(), currentDayEventsList, R.id.calendar_day_gridcell, month, year);
 		dayOfMonthAdapter.notifyDataSetChanged();
 		calendarView.setAdapter(dayOfMonthAdapter);			
 		Parse.initialize(this, APP_KEY, CLIENT_KEY);
 		if(checkAndGetInitData())
 		{
 			setUserNameAndChannel();
 			PushService.subscribe(this, userChannel, this.getClass());
 			PushService.subscribe(this, "", this.getClass());
 		}		
 	}
 	
 	/*
 	 * checks if user has initial data: user name, phone, and arrange time
 	 * if not, switching to InitDataView, and waiting for results.
 	 * Returns if the user had initial data.
 	 */
 	private boolean checkAndGetInitData() 
 	{
 		boolean hasInitData = dbAdapter.hasInitialData();
 		if(!hasInitData)
 		{
 			Intent intent = new Intent(CalendarView.this, InitDataView.class);
 			startActivityForResult(intent, INIT_DATA);
 		}
 		
 		return hasInitData;
 	}
 
 	@Override
 	protected void onResume() 
 	{
 		super.onResume();
 		waintingInvatation = dbAdapter.getWaitingInvitation();
 		if(waintingInvatation != null)
 		{
 			waintingInvatationList = new String[waintingInvatation.length];
 			for (int i = 0; i < waintingInvatation.length; i++) {
 				waintingInvatationList[i] = waintingInvatation[i].toString();
 			}
 
 			invitationBtn.setActivated(true);
 			Toast.makeText(this, "Waiting Invitation", Toast.LENGTH_LONG).show();
 		}
 		else
 		{
 			waintingInvatation = null;			
 		}		
 
 	}
 
 	/**
 	 * Saves all UI object to data members for later refernce.
 	 */
 	private void initUI() 
 	{
 		selectedDayMonthYearButton = (Button) this
 				.findViewById(R.id.selected_date);
 		selectedDayMonthYearButton.setText("Selected: ");
 		
 		newEventBtn = (ImageView) this.findViewById(R.id.new_eve_btn);
 		newEventBtn.setOnClickListener(this);
 
 		prevMonth = (ImageView) this.findViewById(R.id.prevMonth);
 		prevMonth.setOnClickListener(this);
 
 		currentMonth = (Button) this.findViewById(R.id.currentMonth);
 		currentMonth.setText(dateFormatter.format(dateTemplate,	_calendar.getTime()));		
 		nextMonth = (ImageView) this.findViewById(R.id.nextMonth);
 		nextMonth.setOnClickListener(this);
 
 		calendarView = (GridView) this.findViewById(R.id.calendar);
 		currentDayEventsList = (ListView) this.findViewById(R.id.eventsList);
 
 		invitationBtn = (ImageView) this.findViewById(R.id.invitations_list_btn);
 		invitationBtn.setOnClickListener(this);
 
 		
 	}
 
 	private void initCalander() 
 	{
 		_calendar = Calendar.getInstance(Locale.getDefault());
 		month = _calendar.get(Calendar.MONTH) + 1;
 		year = _calendar.get(Calendar.YEAR);
 	}
 
 	/**
 	 * 
 	 * @param month
 	 * @param year
 	 */
 	private void setGridCellAdapterToDate(int month, int year) 
 	{
 		Log.d("UI", "Before new GridCellAdapter");
 		dayOfMonthAdapter = new GridCellAdapter(getApplicationContext(), currentDayEventsList, R.id.calendar_day_gridcell, month, year);
 		Log.d("UI", "after new GridCell");
 		_calendar.set(year, month - 1, _calendar.get(Calendar.DAY_OF_MONTH));
 		currentMonth.setText(dateFormatter.format(dateTemplate,	_calendar.getTime()));
 		dayOfMonthAdapter.notifyDataSetChanged();
 		Log.d("UI", "beforeSetAdapter");
 		calendarView.setAdapter(dayOfMonthAdapter);
 		Log.d("UI", "after SetAdapter");
 	}
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
 	{
 		super.onActivityResult(requestCode, resultCode, data);
 		// new event should be passed
 		if(requestCode == NEW_EVENT)
 		{
 			if (resultCode == RESULT_OK)
 			{
 				Bundle b = data.getExtras();
 				boolean isNewEvent = b.getString("newEvent") == null ? false : true; // could get with null if came back from 'edit mode'
 				if (isNewEvent) {
 					String eventStr = b.getString("newEvent");
 					Event newEvent = Event.CreateFromString(eventStr);
 					addNewEventToUI(newEvent);
 				}
 				else {
 					String updateEventStr = b.getString("updatedEvent");
 					Event updatedEvent = Event.CreateFromString(updateEventStr);
 					deleteEventFromUI(inEditEvent);
 					if(updatedEvent.getMonth() == month && updatedEvent.getYear() == year) {
 						addNewEventToUI(updatedEvent);
 					}
 					
 				}
 				
 			}
 			
 		}
 		if(requestCode == INVITED_EVENT)
 		{
 			if(resultCode == RESULT_OK)
 			{
 				Bundle b = data.getExtras();
 				boolean eventConfirmed = b.getString("newEventId") == null ? false : true;
 				if (eventConfirmed) {					
 					confirmedEvent.setId(Long.valueOf(b.getString("newEventId")));
 					Log.d("UI", "THIS M:Y:" + month + year + " OTHER: " + confirmedEvent.getMonth() + confirmedEvent.getYear());
 					if (month == confirmedEvent.getMonth() && year == confirmedEvent.getYear()) 
 					{
 						addNewEventToUI(confirmedEvent);
 					}
 					
 				}
 				
 			}
 		}
 		if(requestCode == INIT_DATA)
 		{
 			if(resultCode == RESULT_OK)
 			{
 				setUserNameAndChannel();
 				PushService.subscribe(this, userChannel, this.getClass());
 				PushService.subscribe(this, "", this.getClass());				
 			}
 			else
 			{
 				Toast.makeText(this, "Cannot continue without initial data. closing.", Toast.LENGTH_LONG).show();
 				finish();
 			}
 		}
 		
 	}
 	
 	/**
 	 * sets user name and channel from the db.
 	 */
 	private void setUserNameAndChannel()
 	{
 		userName = dbAdapter.getUserName();
 		userChannel = dbAdapter.getUserChannel();
 	}
 	
 	/**
 	 * Adds a new Event to the UI. 
 	 * Event should be in the current month.
 	 * @param newEvent
 	 */
 	private void addNewEventToUI(Event newEvent)
 	{
 		// adding event to eventsPerMonth map
 		dayOfMonthAdapter.addEventToMonth(newEvent);
 		// adding to the view
 		ArrayAdapter<Event> eventsAdapter = (ArrayAdapter<Event>) currentDayEventsList.getAdapter();
 		eventsAdapter.add(newEvent);
 		eventsAdapter.notifyDataSetChanged();
 		dayOfMonthAdapter.notifyDataSetChanged();
 		
 	}
 	
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) 
 	{
 		  if (v.getId() == R.id.eventsList) 
 		  {
 			    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
 			    Event pressedEvent = (Event) currentDayEventsList.getAdapter().getItem(info.position);
 			    Log.d("PRESS", "Pressed eventId: " + pressedEvent.getId());
 			    menu.setHeaderTitle(pressedEvent.toString());
 			    int i = 0;
 			    for (eMenuItems menuItem : eMenuItems.values()) {
 					menu.add(Menu.NONE, i, i, menuItem.toString());
 					i++;
 				}
 		  }
 	  
 	}
 
 	@Override
 	public boolean onContextItemSelected(MenuItem item) 
 	{
 		  AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
 		  int menuItemIndex = item.getItemId();
 		  eMenuItems menuItemName = eMenuItems.getById(menuItemIndex);
 		  final Event pressedEvent = (Event) currentDayEventsList.getAdapter().getItem(info.position);
 		  Log.d("PRESS", "Pressed eventId: " + pressedEvent.getId());
 		  switch (menuItemName) {
 			case DELETE:
 				deleteEvent(pressedEvent);
 				break;
 			case EDIT:
 					try {
 						alarmsManager.updateStart(pressedEvent);
 						inEditEvent = pressedEvent;
 						changeToEventView("editEvent", pressedEvent.encodeToString());
 					} catch (Exception e) {
 						e.printStackTrace();
 				  		Toast.makeText(this, "Error with edit event", Toast.LENGTH_LONG).show();
 					}
 				break;
 			case INFO:
 				changeToEventInfo("event", pressedEvent.encodeToString());
 				break;
 			case INVITE:
 				final Dialog dialog = new Dialog(this);
 
 				dialog.setContentView(R.layout.invite_dialog);
 				dialog.setTitle("Enter Phone Number:");
 				final TextView text = (TextView) dialog.findViewById(R.id.invited_phone);
 				Button okBtn = (Button) dialog.findViewById(R.id.invitation_OK);
 				okBtn.setOnClickListener(new OnClickListener() {					
 					@Override
 					public void onClick(View arg0) {
 						InvitedEvent invitedEvent = InvitedEvent.newInstanceFromEvent(pressedEvent, userName, userChannel);
 						String phoneNumber = text.getText().toString();
 						ParseHandler.sendInvitation(invitedEvent, ParseHandler.numberToChannelHash(phoneNumber));
 						dialog.dismiss();
 					}
 				});
 				
 				dialog.show();
 				break;
 			default:
 				Toast.makeText(this, "Error with menu", Toast.LENGTH_LONG).show();
 			}
 		  
 		  return true;
 	}	
 	
 	private void deleteEvent(Event pressedEvent) 
 	{
 	  	try 
 	  	{
 			alarmsManager.deleteEvent(pressedEvent);
 			deleteEventFromUI(pressedEvent);
 		} 
 	  	catch(Exception e) 
 	  	{
 	  		e.printStackTrace();
 	  		Toast.makeText(this, "Error schedual next event", Toast.LENGTH_LONG).show();
 		}
 	}
 	
 	/**
 	 * remove an event from the days event list, and update the number of events per day.
 	 * changedEvent should be in of the current month !
 	 * @param changedEvent
 	 */
 	private void deleteEventFromUI(Event changedEvent) {
 		dayOfMonthAdapter.removeEventFromMonth(changedEvent);
 		// removing from the day events list.
 		ArrayAdapter<Event> eventsAdapter = (ArrayAdapter<Event>) currentDayEventsList.getAdapter();
 		eventsAdapter.remove(changedEvent);
 		eventsAdapter.notifyDataSetChanged();
 		dayOfMonthAdapter.notifyDataSetChanged();		
 	}
 
 	@Override
 	public void onClick(View v)
 	{		
 		if (v == prevMonth)
 		{
 			backwardMonth();			
 		}
 		if (v == nextMonth)
 		{
 			forwardMonth();
 		}
 		if (v == newEventBtn)
 		{
 			changeToEventView();
 		}
 		if (v == invitationBtn)
 		{
 			showInvitedEventDialog();
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			if(waintingInvatation == null)
 			{
 				builder.setTitle("No Waiting Events");
 			}
 			else
 			{
 				showInvitationDialog(builder);
 			}
 			
 		}
 
 	}
 	
 	private void showInvitationDialog(AlertDialog.Builder builder)
 	{
 		builder.setTitle("Click For Details");
 		builder.setItems(waintingInvatationList, new DialogInterface.OnClickListener() {
 		    public void onClick(DialogInterface dialog, int item) {
 		        Log.d("INVI", waintingInvatationList[item]);
 		        try {
 		        	confirmedEvent = waintingInvatation[item];
 		        	changeToIntivedInfo(confirmedEvent);					
 				} catch (Exception e) {
 			        Log.d("INVI", "ERROR");
 					e.printStackTrace();
 				}
 		    }
 		});
 		AlertDialog alert = builder.create();
 		alert.show();		
 	}
 	
 	protected void changeToIntivedInfo(InvitedEvent event) {
 		Intent intent = new Intent(CalendarView.this, InvitedEventInfo.class);	
 		intent.putExtra("event", event.encodeToString());
 		intent.putExtra("user_name", userName);
 		startActivityForResult(intent, INVITED_EVENT);
 	}
 
 	private void showInvitedEventDialog() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/**
 	 * Change to Event View for a new event.
 	 */
 	private void changeToEventView()
 	{
 		GridCellAdapter grid = (GridCellAdapter) calendarView.getAdapter();
 		this.changeToEventView("selectedDate", grid.getSelectedDate());
 	}
 	
 	
 	private void changeToEventView(String extraDataKey, String extraData) 
 	{
 		//TODO: send new event with current time and date
 		Intent intent = new Intent(this, EventView.class);	
 		intent.putExtra(extraDataKey, extraData);
 		startActivityForResult(intent, NEW_EVENT);		
 	}
 
 	private void changeToEventInfo(String extraDataKey, String extraData) {
 		Intent intent = new Intent(this, EventInfo.class);	
 		intent.putExtra(extraDataKey, extraData);
 		startActivity(intent);	
 	}
 	
 	private void forwardMonth() 
 	{
 		if (month > 11)
 		{
 			month = 1;
 			year++;
 		}
 		else
 		{
 			month++;
 		}
 		
 //		Log.d(tag, "Setting Next Month in GridCellAdapter: " + "Month: " + month + " Year: " + year);
 		setGridCellAdapterToDate(month, year);
 	}
 
 	private void backwardMonth() 
 	{
 		if (month <= 1)
 		{
 			month = 12;
 			year--;
 		}
 		else
 		{
 			month--;
 		}
 //		Log.d(tag, "Setting Prev Month in GridCellAdapter: " + "Month: " + month + " Year: " + year);
 		setGridCellAdapterToDate(month, year);		
 	}
 
 	// ///////////////////////////////////////////////////////////////////////////////////////
 	// Inner Class
 	public class GridCellAdapter extends BaseAdapter implements OnClickListener 
 	{
 		private static final String tag = "GridCellAdapter";
 		private final Context _context;
 
 		private final List<String> list;
 		private static final int DAY_OFFSET = 1;
 		//TODO: move all this static arrays to another class
 		private final String[] weekdays = new String[] { "Sun", "Mon", "Tue",
 				"Wed", "Thu", "Fri", "Sat" };
 		private final String[] months = { "January", "February", "March",
 				"April", "May", "June", "July", "August", "September",
 				"October", "November", "December" };
 		private final int[] daysOfMonth = { 31, 28, 31, 30, 31, 30, 31, 31, 30,	31, 30, 31 };
 		private final int month, year;
 		private int daysInMonth, prevMonthDays;
 		private int currentDayOfMonth;
 		private int currentWeekDay;
 		private Button gridcell;
 		private TextView num_events_per_day;
 		private final HashMap<String, List<Event>> eventsPerMonthMap;
 		private final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy");
 		
 		private String selectedDateString;
 		private DbAdapter dbAdapter;
 		private ListView eventsList;
 
 		// Days in Current Month
 		public GridCellAdapter(Context context, ListView eventsList, int textViewResourceId,	int month, int year) 
 		{
 			super();
 			this._context = context;
 			this.dbAdapter = new DbAdapter(context);
 			this.list = new ArrayList<String>();
 			this.month = month;
 			this.year = year;
 			this.eventsList = eventsList;
 //			Log.d(tag, "==> Passed in Date FOR Month: " + month + " "
 //					+ "Year: " + year);
 			Calendar calendar = Calendar.getInstance();
 			setCurrentDayOfMonth(calendar.get(Calendar.DAY_OF_MONTH));
 			setCurrentWeekDay(calendar.get(Calendar.DAY_OF_WEEK));
 //			Log.d(tag, "New Calendar:= " + calendar.getTime().toString());
 //			Log.d(tag, "CurrentDayOfWeek :" + getCurrentWeekDay());
 //			Log.d(tag, "CurrentDayOfMonth :" + getCurrentDayOfMonth());			
 			// Set selected date string for current date (if '+' button hit before date selected)
 			selectedDateString = calendar.get(Calendar.DAY_OF_MONTH) + "-" 
 								+ (calendar.get(Calendar.MONTH) + 1) + "-"
 								+ calendar.get(Calendar.YEAR);
 			selectedDayMonthYearButton.setText("Selected: " + selectedDateString);
 			// Print Month									
 			//TODO: make it efficient by adding buffer to the prev and next monthes ? 
 			eventsPerMonthMap = dbAdapter.getEventsMapForMonth(month, year);
 			ArrayAdapter<Event> eventsListAdapter = new ArrayAdapter<Event>(context, android.R.layout.simple_list_item_1);
 			if(eventsPerMonthMap.containsKey(String.valueOf(currentDayOfMonth)))
 			{
 				eventsListAdapter.addAll(eventsPerMonthMap.get(String.valueOf(currentDayOfMonth)));
 			}
 			//addEventsToAdapter(eventsListAdapter, eventsPerMonthMap.get(String.valueOf(currentDayOfMonth)));
 			this.eventsList.setAdapter(eventsListAdapter);		
 			printMonth(month, year);
 		}
 		
 		public String getSelectedDate()
 		{
 			return selectedDateString;
 		}
 		
 		private String getMonthAsString(int i) {
 			return months[i];
 		}
 
 		private int getNumberOfDaysOfMonth(int i) {
 			return daysOfMonth[i];
 		}
 
 		public String getItem(int position) {
 			return list.get(position);
 		}
 
 		@Override
 		public int getCount() {
 			return list.size();
 		}
 
 		/**
 		 * Prints Month
 		 * 
 		 * @param mm
 		 * @param yy
 		 */
 		private void printMonth(int mm, int yy) 
 		{
 //			Log.d(tag, "==> printMonth: mm: " + mm + " " + "yy: " + yy);
 			// The number of days to leave blank at
 			// the start of this month.
 			int trailingSpaces = 0;
 			int daysInPrevMonth = 0;
 			int prevMonth = 0;
 			int prevYear = 0;
 			int nextMonth = 0;
 			int nextYear = 0;
 
 			int currentMonth = mm - 1;
 			String currentMonthName = getMonthAsString(currentMonth);
 			daysInMonth = getNumberOfDaysOfMonth(currentMonth);
 
 //			Log.d(tag, "Current Month: " + " " + currentMonthName + " having "
 //					+ daysInMonth + " days.");
 
 			// Gregorian Calendar : MINUS 1, set to FIRST OF MONTH
 			GregorianCalendar cal = new GregorianCalendar(yy, currentMonth, 1);
 //			Log.d(tag, "Gregorian Calendar:= " + cal.getTime().toString());
 
 			if (currentMonth == 11) {
 				prevMonth = currentMonth - 1;
 				daysInPrevMonth = getNumberOfDaysOfMonth(prevMonth);
 				nextMonth = 0;
 				prevYear = yy;
 				nextYear = yy + 1;
 //				Log.d(tag, "*->PrevYear: " + prevYear + " PrevMonth:"
 //						+ prevMonth + " NextMonth: " + nextMonth
 //						+ " NextYear: " + nextYear);
 			} else if (currentMonth == 0) {
 				prevMonth = 11;
 				prevYear = yy - 1;
 				nextYear = yy;
 				daysInPrevMonth = getNumberOfDaysOfMonth(prevMonth);
 				nextMonth = 1;
 //				Log.d(tag, "**--> PrevYear: " + prevYear + " PrevMonth:"
 //						+ prevMonth + " NextMonth: " + nextMonth
 //						+ " NextYear: " + nextYear);
 			} else {
 				prevMonth = currentMonth - 1;
 				nextMonth = currentMonth + 1;
 				nextYear = yy;
 				prevYear = yy;
 				daysInPrevMonth = getNumberOfDaysOfMonth(prevMonth);
 //				Log.d(tag, "***---> PrevYear: " + prevYear + " PrevMonth:"
 //						+ prevMonth + " NextMonth: " + nextMonth
 //						+ " NextYear: " + nextYear);
 			}
 
 			// Compute how much to leave before before the first day of the
 			// month.
 			// getDay() returns 0 for Sunday.
 			int currentWeekDay = cal.get(Calendar.DAY_OF_WEEK) - 1;
 			trailingSpaces = currentWeekDay;
 
 //			Log.d(tag, "Week Day:" + currentWeekDay + " is "
 //					+ getWeekDayAsString(currentWeekDay));
 //			Log.d(tag, "No. Trailing space to Add: " + trailingSpaces);
 //			Log.d(tag, "No. of Days in Previous Month: " + daysInPrevMonth);
 
 			if (cal.isLeapYear(cal.get(Calendar.YEAR)) && mm == 1) {
 				++daysInMonth;
 			}
 
 			// Trailing Month days
 			for (int i = 0; i < trailingSpaces; i++) {
 //				Log.d(tag, "PREV MONTH:= "
 //							+ prevMonth
 //							+ " => "
 //							+ getMonthAsString(prevMonth)
 //							+ " "
 //							+ String.valueOf((daysInPrevMonth - trailingSpaces + DAY_OFFSET) + i));
 				list.add(String.valueOf((daysInPrevMonth - trailingSpaces + DAY_OFFSET)	+ i)
 						+ "-GREY"
 						+ "-"
 						+ prevMonth
 						+ "-"
 						+ prevYear);
 			}
 
 			// Current Month Days
 			Calendar currDayCal = Calendar.getInstance();
 			for (int i = 1; i <= daysInMonth; i++) {
 //				Log.d(currentMonthName, String.valueOf(i) + " "
 //						+ getMonthAsString(currentMonth) + " " + yy);
 				if (i == getCurrentDayOfMonth() && currDayCal.get(Calendar.MONTH) == currentMonth) 
 				{
 					list.add(String.valueOf(i) + "-RED" + "-"
 							+ currentMonth + "-" + yy);
 				} 
 				else 
 				{
 					list.add(String.valueOf(i) + "-WHITE" + "-"
 							+ currentMonth + "-" + yy);
 				}
 			}
 
 			// Leading Month days
 			for (int i = 0; i < list.size() % 7; i++) {
 //				Log.d(tag, "NEXT MONTH:= " + getMonthAsString(nextMonth));
 				list.add(String.valueOf(i + 1) + "-GREY" + "-"
 						+ nextMonth + "-" + nextYear);
 			}
 		}
 
 		@Override
 		public long getItemId(int position) {
 			return position;
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) 
 		{
 			View row = convertView;
 			if (row == null) {
 				LayoutInflater inflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 				row = inflater.inflate(R.layout.calendar_day_gridcell, parent, false);
 			}
 
 			// Get a reference to the Day gridcell
 			gridcell = (Button) row.findViewById(R.id.calendar_day_gridcell);
 			gridcell.setOnClickListener(this);
 
 			// ACCOUNT FOR SPACING
 
 //			Log.d(tag, "Current Day: " + getCurrentDayOfMonth());
 			String[] day_color = list.get(position).split("-");
 			String theday = day_color[0];
 			String themonth = String.valueOf((Integer.parseInt(day_color[2]) + 1));
 			String theyear = day_color[3];
 			if ((!eventsPerMonthMap.isEmpty()) && (eventsPerMonthMap != null)) 
 			{
				if (eventsPerMonthMap.containsKey(theday) && day_color[1].equals("WHITE")) 
 				{
 					num_events_per_day = (TextView) row.findViewById(R.id.num_events_per_day);
 //					Log.d("DATE:", theday + ": " + num_events_per_day);
 					Integer numEvents = (Integer) eventsPerMonthMap.get(theday).size();
 					String numOfEventsStr = numEvents == 0 ? "" : numEvents.toString();
 					num_events_per_day.setText(numOfEventsStr);
 				}
 				
 			}
 
 			// Set the Day GridCell
 			gridcell.setText(theday);
 			gridcell.setTag(theday + "-" + themonth + "-" + theyear);
 //			Log.d(tag, "Setting GridCell " + theday + "-" + themonth + "-"
 //					+ theyear);
 
 			if (day_color[1].equals("GREY")) {
 				gridcell.setTextColor(Color.LTGRAY);
 			}
 			if (day_color[1].equals("WHITE")) {
 				gridcell.setTextColor(Color.WHITE);
 			}
 			if (day_color[1].equals("RED")) {
 				gridcell.setTextColor(getResources().getColor(
 						R.color.static_text_color));
 			}
 			return row;
 		}
 
 		@Override
 		public void onClick(View view) 
 		{
 			selectedDateString = (String) view.getTag();
 			selectedDayMonthYearButton.setText("Selected: " + selectedDateString);
 			String[] dateSplited = selectedDateString.split("-");
 			String day = dateSplited[0];
 			int choosedMonth = Integer.parseInt(dateSplited[1]);
 			List<Event> eventsForDay = null;
 			ArrayAdapter<Event> adapter = new ArrayAdapter<Event>(_context, android.R.layout.simple_list_item_1);
 			if (choosedMonth == month) {
 				eventsForDay = eventsPerMonthMap.get(day);				
 			}
 			
 			//TODO: what do we want to display ? 
 			// this is only for keeping the flow
 			if(eventsForDay != null)
 			{
 				adapter.addAll(eventsForDay);				
 			}
 			
 			adapter.notifyDataSetChanged();
 			eventsList.setAdapter(adapter);
 		}
 
 		public int getCurrentDayOfMonth() {
 			return currentDayOfMonth;
 		}
 
 		private void setCurrentDayOfMonth(int currentDayOfMonth) {
 			this.currentDayOfMonth = currentDayOfMonth;
 		}
 
 		public void setCurrentWeekDay(int currentWeekDay) {
 			this.currentWeekDay = currentWeekDay;
 		}
 
 		public int getCurrentWeekDay() {
 			return currentWeekDay;
 		}
 		
 		public void removeEventFromMonth(Event event) 
 		{
 			List<Event> dayEvents = eventsPerMonthMap.get(String.valueOf(event.getDay()));
 			if(dayEvents != null) {
 				dayEvents.remove(event);				
 			}
 
 		}
 
 		public void addEventToMonth(Event newEvent) 
 		{
 			List<Event> dayEvents = eventsPerMonthMap.get(String.valueOf(newEvent.getDay()));
 			if (dayEvents == null) // first event
 			{
 				dayEvents = new ArrayList<Event>(2);
 				eventsPerMonthMap.put(String.valueOf(newEvent.getDay()), dayEvents);
 			}
 			dayEvents.add(newEvent);
 		}
 	}
 }
