 package clock.sched;
 
 import java.io.UnsupportedEncodingException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 
 import clock.db.DbAdapter;
 import clock.db.Event;
 
 import clock.outsources.GoogleWeatherHandler;
 import clock.outsources.dependencies.WeatherModel;
 import clock.sched.R;
 
 
 import android.app.Activity;
 import android.content.Context;
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
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.BaseAdapter;
 import android.widget.Button;
 import android.widget.GridView;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /*
  * Main Application activity.
  */
 public class CalendarView extends Activity implements OnClickListener 
 {
 	private static final String tag = "AppDate";
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
 	private static final String EDIT = "Edit";
     private static final String DELETE = "Delete";
     private static final String INFO = "Show Info";
     private static final String[] menuItems = {EDIT, DELETE, INFO};
 	
 	private ListView currentDayEventsList;
 	
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
 		alarmsManager = new AlarmsManager(this, new DbAdapter(this));
 		dayOfMonthAdapter = new GridCellAdapter(getApplicationContext(), currentDayEventsList, R.id.calendar_day_gridcell, month, year);
 		dayOfMonthAdapter.notifyDataSetChanged();
 		calendarView.setAdapter(dayOfMonthAdapter);
 		
 		Event e = Event.createNewInstance();
 		e.setDay(1);
 		e.setLocation("  24  ");
 		e.setDateFromSql("2012-08-25 22-22-00");
 		e.setWithAlarmStatus(true);
 				
 //		if(!alarmsManager.hasInitArragmentTime())
 //		{
 //			
 //		}
 		
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
 		dayOfMonthAdapter = new GridCellAdapter(getApplicationContext(), currentDayEventsList, R.id.calendar_day_gridcell, month, year);
 		_calendar.set(year, month - 1, _calendar.get(Calendar.DAY_OF_MONTH));
 		currentMonth.setText(dateFormatter.format(dateTemplate,	_calendar.getTime()));
 		dayOfMonthAdapter.notifyDataSetChanged();
 		calendarView.setAdapter(dayOfMonthAdapter);
 	}
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
 	{
 		super.onActivityResult(requestCode, resultCode, data);
 		// new event should be passed
 		if (resultCode == RESULT_OK)
 		{
 			Bundle b = data.getExtras();
 			String eventStr = b.getString("newEvent");
 			Event newEvent = Event.CreateFromString(eventStr);
 			// adding event to eventsPerMonth map
 			dayOfMonthAdapter.addEventToMonth(newEvent);
 			// adding to the view
 			ArrayAdapter<Event> eventsAdapter = (ArrayAdapter<Event>) currentDayEventsList.getAdapter();
 			eventsAdapter.add(newEvent);
 			eventsAdapter.notifyDataSetChanged();
 			dayOfMonthAdapter.notifyDataSetChanged();
 		}
 	}
 	
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) 
 	{
 		  if (v.getId() == R.id.eventsList) 
 		  {
 			    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
 			    Event pressedEvent = (Event) currentDayEventsList.getAdapter().getItem(info.position);
 			    menu.setHeaderTitle(pressedEvent.toString());
 			    for (int i = 0; i < menuItems.length; i++) 
 			    {
 			      menu.add(Menu.NONE, i, i, menuItems[i]);
 			    }    
 		  }
 	  
 	}
 
 	@Override
 	public boolean onContextItemSelected(MenuItem item) 
 	{
 		  AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
 		  int menuItemIndex = item.getItemId();
 		  String menuItemName = menuItems[menuItemIndex];
 		  Event pressedEvent = (Event) currentDayEventsList.getAdapter().getItem(info.position);
 
 		  if (menuItemName.equals(EDIT))
 		  {
 			  // acting like event is deleted if need to edit
 			  deleteEvent(pressedEvent);
 			  changeToEventView("editEvent", pressedEvent.encodeToString());
 		  }
 		  else if (menuItemName.equals(DELETE))
 		  {
 			  deleteEvent(pressedEvent);
 		  }
 		  else if (menuItemName.equals(INFO))
 		  {
 			  changeToEventInfo("event", pressedEvent.encodeToString());
 		  }
 		  
 		  return true;
 	}	
 	
 	private void deleteEvent(Event pressedEvent) 
 	{
 	  	try 
 	  	{
 			alarmsManager.deleteEvent(pressedEvent);
 		} 
 	  	catch(Exception e) 
 	  	{
 	  		Toast.makeText(this, "Error schedual next event", Toast.LENGTH_LONG).show();
 		}
 	  	
 		dayOfMonthAdapter.removeEventFromMonth(pressedEvent);
 		// removing from the day events list.
 		ArrayAdapter<Event> eventsAdapter = (ArrayAdapter<Event>) currentDayEventsList.getAdapter();
 		eventsAdapter.remove(pressedEvent);
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
 		startActivityForResult(intent, 0);		
 	}
 
 	private void changeToEventInfo(String extraDataKey, String extraData) {
 		Intent intent = new Intent(this, EventInfo.class);	
 		intent.putExtra(extraDataKey, extraData);
 		startActivityForResult(intent, 0);	
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
 			dbAdapter.open();									
 			//TODO: make it efficient by adding buffer to the prev and next monthes ? 
 			eventsPerMonthMap = dbAdapter.getEventsMapForMonth(month, year);
 			dbAdapter.close();
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
 				if (eventsPerMonthMap.containsKey(theday)) 
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
 			String day = selectedDateString.split("-")[0];
 			
 			ArrayAdapter<Event> adapter = new ArrayAdapter<Event>(_context, android.R.layout.simple_list_item_1);
 			List<Event> eventsForDay = eventsPerMonthMap.get(day);
 			
 			//TODO: what do we want to display ? 
 			// this is only for keeping the flow
 			if(eventsForDay != null)
 			{
 				adapter.addAll(eventsForDay);				
 			}
 			
 			adapter.notifyDataSetChanged();
 			eventsList.setAdapter(adapter);
 			
 			try {
 				Date parsedDate = dateFormatter.parse(selectedDateString);
 //				Log.d(tag, "Parsed Date: " + parsedDate.toString());
 
 			} 
 			catch (ParseException e) {
 				e.printStackTrace();
 			}
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
 			dayEvents.remove(event);
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
