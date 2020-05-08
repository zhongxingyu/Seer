 package clock.sched;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 
 import clock.db.DbAdapter;
 import clock.db.Event;
 
 import clock.sched.R;
 
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.AssetManager;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.text.format.DateFormat;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.BaseAdapter;
 import android.widget.Button;
 import android.widget.GridView;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class CalendarView extends Activity implements OnClickListener {
 	private static final String tag = "SimpleCalendarViewActivity";
 	private Button selectedDayMonthYearButton;
 	private Button currentMonth;
 	private ImageView prevMonth;
 	private ImageView nextMonth;
 	private Button newEventBtn;
 	private GridView calendarView;
 	private GridCellAdapter adapter;
 	private Calendar _calendar;
 	private int month, year;
 	private final DateFormat dateFormatter = new DateFormat();
 	private static final String dateTemplate = "MMMM yyyy";
 	
 	private ListView eventsList;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) 
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.simple_calendar_view);
 
 		initCalander();
 		initUI();
 		
 		adapter = new GridCellAdapter(getApplicationContext(), eventsList, R.id.calendar_day_gridcell, month, year);
 		adapter.notifyDataSetChanged();
 		calendarView.setAdapter(adapter);
 	}
 
 	private void initUI() 
 	{
 		selectedDayMonthYearButton = (Button) this
 				.findViewById(R.id.selected_date);
 		selectedDayMonthYearButton.setText("Selected: ");
 		
 		newEventBtn = (Button) this.findViewById(R.id.new_eve_btn);
 		newEventBtn.setOnClickListener(this);
 
 		prevMonth = (ImageView) this.findViewById(R.id.prevMonth);
 		prevMonth.setOnClickListener(this);
 
 		currentMonth = (Button) this.findViewById(R.id.currentMonth);
 		currentMonth.setText(dateFormatter.format(dateTemplate,	_calendar.getTime()));
 
 		nextMonth = (ImageView) this.findViewById(R.id.nextMonth);
 		nextMonth.setOnClickListener(this);
 
 		calendarView = (GridView) this.findViewById(R.id.calendar);
 		eventsList = (ListView) this.findViewById(R.id.eventsList);
 	}
 
 	private void initCalander() 
 	{
 		_calendar = Calendar.getInstance(Locale.getDefault());
 		month = _calendar.get(Calendar.MONTH) + 1;
 		year = _calendar.get(Calendar.YEAR);
 		Log.d(tag, "Calendar Instance:= " + "Month: " + month + " " + "Year: "
 				+ year);
 	}
 
 	/**
 	 * 
 	 * @param month
 	 * @param year
 	 */
 	private void setGridCellAdapterToDate(int month, int year) 
 	{
 		//TODO: why are we create new instance each month ? 
 		adapter = new GridCellAdapter(getApplicationContext(), eventsList, R.id.calendar_day_gridcell, month, year);
 		_calendar.set(year, month - 1, _calendar.get(Calendar.DAY_OF_MONTH));
 		currentMonth.setText(dateFormatter.format(dateTemplate,	_calendar.getTime()));
 		adapter.notifyDataSetChanged();
 		calendarView.setAdapter(adapter);
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
 			adapter.addEventToMonth(newEvent);
 			// adding to the view
 			ArrayAdapter<String> eventsAdapter = (ArrayAdapter<String>) eventsList.getAdapter();
 			eventsAdapter.add(newEvent.getLocation());
 			eventsAdapter.notifyDataSetChanged();			
 		}
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
 
 	private void changeToEventView() 
 	{
 		//TODO: send new event with current time and date
 		GridCellAdapter grid = (GridCellAdapter) calendarView.getAdapter();
 		Intent intent = new Intent(this, EventView.class);	
 		intent.putExtra("selectedDate", grid.getSelectedDate());
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
 		
 		Log.d(tag, "Setting Next Month in GridCellAdapter: " + "Month: " + month + " Year: " + year);
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
 		Log.d(tag, "Setting Prev Month in GridCellAdapter: " + "Month: " + month + " Year: " + year);
 		setGridCellAdapterToDate(month, year);		
 	}
 
 	@Override
 	public void onDestroy() 
 	{
 	}
 	
 
 	// ///////////////////////////////////////////////////////////////////////////////////////
 	// Inner Class
 	public class GridCellAdapter extends BaseAdapter implements OnClickListener {
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
 		private final HashMap<Integer, List<Event>> eventsPerMonthMap;
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
 			ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1);
 			adapter.add("Events list");
 			this.eventsList.setAdapter(adapter);
 			
 			Log.d(tag, "==> Passed in Date FOR Month: " + month + " "
 					+ "Year: " + year);
 			Calendar calendar = Calendar.getInstance();
 			setCurrentDayOfMonth(calendar.get(Calendar.DAY_OF_MONTH));
 			setCurrentWeekDay(calendar.get(Calendar.DAY_OF_WEEK));
 			Log.d(tag, "New Calendar:= " + calendar.getTime().toString());
 			Log.d(tag, "CurrentDayOfWeek :" + getCurrentWeekDay());
			Log.d(tag, "CurrentDayOfMonth :" + getCurrentDayOfMonth());			
 			// Set selected date string for current date (if '+' button hit before date selected)
 			selectedDateString = calendar.get(Calendar.DAY_OF_MONTH) + "-" 
 								+ (calendar.get(Calendar.MONTH) + 1) + "-"
 								+ calendar.get(Calendar.YEAR);
 			selectedDayMonthYearButton.setText("Selected: " + selectedDateString);
 			// Print Month
 			printMonth(month, year);
			dbAdapter.open();									
 			// Get events per month
 			//TODO: make it efficient by adding buffer to the prev and next monthes ? 
 			// and add year param
			eventsPerMonthMap = dbAdapter.getEventsMapForMonth(month, year);
 			dbAdapter.close();
 		}
 		
 		public String getSelectedDate()
 		{
 			return selectedDateString;
 		}
 		
 		private String getMonthAsString(int i) {
 			return months[i];
 		}
 
 		private String getWeekDayAsString(int i) {
 			return weekdays[i];
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
 			Log.d(tag, "==> printMonth: mm: " + mm + " " + "yy: " + yy);
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
 
 			Log.d(tag, "Current Month: " + " " + currentMonthName + " having "
 					+ daysInMonth + " days.");
 
 			// Gregorian Calendar : MINUS 1, set to FIRST OF MONTH
 			GregorianCalendar cal = new GregorianCalendar(yy, currentMonth, 1);
 			Log.d(tag, "Gregorian Calendar:= " + cal.getTime().toString());
 
 			if (currentMonth == 11) {
 				prevMonth = currentMonth - 1;
 				daysInPrevMonth = getNumberOfDaysOfMonth(prevMonth);
 				nextMonth = 0;
 				prevYear = yy;
 				nextYear = yy + 1;
 				Log.d(tag, "*->PrevYear: " + prevYear + " PrevMonth:"
 						+ prevMonth + " NextMonth: " + nextMonth
 						+ " NextYear: " + nextYear);
 			} else if (currentMonth == 0) {
 				prevMonth = 11;
 				prevYear = yy - 1;
 				nextYear = yy;
 				daysInPrevMonth = getNumberOfDaysOfMonth(prevMonth);
 				nextMonth = 1;
 				Log.d(tag, "**--> PrevYear: " + prevYear + " PrevMonth:"
 						+ prevMonth + " NextMonth: " + nextMonth
 						+ " NextYear: " + nextYear);
 			} else {
 				prevMonth = currentMonth - 1;
 				nextMonth = currentMonth + 1;
 				nextYear = yy;
 				prevYear = yy;
 				daysInPrevMonth = getNumberOfDaysOfMonth(prevMonth);
 				Log.d(tag, "***---> PrevYear: " + prevYear + " PrevMonth:"
 						+ prevMonth + " NextMonth: " + nextMonth
 						+ " NextYear: " + nextYear);
 			}
 
 			// Compute how much to leave before before the first day of the
 			// month.
 			// getDay() returns 0 for Sunday.
 			int currentWeekDay = cal.get(Calendar.DAY_OF_WEEK) - 1;
 			trailingSpaces = currentWeekDay;
 
 			Log.d(tag, "Week Day:" + currentWeekDay + " is "
 					+ getWeekDayAsString(currentWeekDay));
 			Log.d(tag, "No. Trailing space to Add: " + trailingSpaces);
 			Log.d(tag, "No. of Days in Previous Month: " + daysInPrevMonth);
 
 			if (cal.isLeapYear(cal.get(Calendar.YEAR)) && mm == 1) {
 				++daysInMonth;
 			}
 
 			// Trailing Month days
 			for (int i = 0; i < trailingSpaces; i++) {
 				Log.d(tag, "PREV MONTH:= "
 							+ prevMonth
 							+ " => "
 							+ getMonthAsString(prevMonth)
 							+ " "
 							+ String.valueOf((daysInPrevMonth - trailingSpaces + DAY_OFFSET) + i));
 				list.add(String.valueOf((daysInPrevMonth - trailingSpaces + DAY_OFFSET)	+ i)
 						+ "-GREY"
 						+ "-"
 						+ prevMonth
 						+ "-"
 						+ prevYear);
 			}
 
 			// Current Month Days
 			for (int i = 1; i <= daysInMonth; i++) {
 				Log.d(currentMonthName, String.valueOf(i) + " "
 						+ getMonthAsString(currentMonth) + " " + yy);
 				if (i == getCurrentDayOfMonth()) 
 				{
 					list.add(String.valueOf(i) + "-BLUE" + "-"
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
 				Log.d(tag, "NEXT MONTH:= " + getMonthAsString(nextMonth));
 				list.add(String.valueOf(i + 1) + "-GREY" + "-"
 						+ nextMonth + "-" + nextYear);
 			}
 		}
 
 		@Override
 		public long getItemId(int position) {
 			return position;
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			View row = convertView;
 			if (row == null) {
 				LayoutInflater inflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 				row = inflater.inflate(R.layout.calendar_day_gridcell, parent, false);
 			}
 
 			// Get a reference to the Day gridcell
 			gridcell = (Button) row.findViewById(R.id.calendar_day_gridcell);
 			gridcell.setOnClickListener(this);
 
 			// ACCOUNT FOR SPACING
 
 			Log.d(tag, "Current Day: " + getCurrentDayOfMonth());
 			String[] day_color = list.get(position).split("-");
 			String theday = day_color[0];
 			String themonth = String.valueOf((Integer.parseInt(day_color[2]) + 1));
 			String theyear = day_color[3];
 			if ((!eventsPerMonthMap.isEmpty()) && (eventsPerMonthMap != null)) 
 			{
 				if (eventsPerMonthMap.containsKey(theday)) 
 				{
 					num_events_per_day = (TextView) row.findViewById(R.id.num_events_per_day);
 					Log.d("DATE:", theday + ": " + num_events_per_day);
 					Integer numEvents = (Integer) eventsPerMonthMap.get(theday).size();
 					num_events_per_day.setText(numEvents.toString());
 				}
 			}
 
 			// Set the Day GridCell
 			gridcell.setText(theday);
 			gridcell.setTag(theday + "-" + themonth + "-" + theyear);
 			Log.d(tag, "Setting GridCell " + theday + "-" + themonth + "-"
 					+ theyear);
 
 			if (day_color[1].equals("GREY")) {
 				gridcell.setTextColor(Color.LTGRAY);
 			}
 			if (day_color[1].equals("WHITE")) {
 				gridcell.setTextColor(Color.WHITE);
 			}
 			if (day_color[1].equals("BLUE")) {
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
 			int day = Integer.parseInt(selectedDateString.split("-")[0]);
 			
 			ArrayAdapter<String> adapter = new ArrayAdapter<String>(_context, android.R.layout.simple_list_item_1);
 			List<Event> eventsForDay = eventsPerMonthMap.get(day);
 			
 			//TODO: what do we want to display ? 
 			// this is only for keeping the flow
 			if(eventsForDay != null)
 			{
 				addDataToAdapter(adapter, eventsForDay);				
 			}
 			
 			adapter.notifyDataSetChanged();
 			eventsList.setAdapter(adapter);
 			
 			try {
 				Date parsedDate = dateFormatter.parse(selectedDateString);
 				Log.d(tag, "Parsed Date: " + parsedDate.toString());
 
 			} 
 			catch (ParseException e) {
 				e.printStackTrace();
 			}
 		}
 
 		private void addDataToAdapter(ArrayAdapter<String> adapter, List<Event> eventsForDay) 
 		{
 			for (Iterator<Event> iterator = eventsForDay.iterator(); iterator.hasNext();) {
 				Event event = iterator.next();
 				adapter.add(event.getLocation());				
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
 		
 		public void addEventToMonth(Event newEvent) 
 		{
 			List<Event> dayEvents = eventsPerMonthMap.get(newEvent.getDay());
 			if (dayEvents == null) // first event
 			{
 				dayEvents = new ArrayList<Event>(2);
 				eventsPerMonthMap.put(newEvent.getDay(), dayEvents);
 			}
 			
 			dayEvents.add(newEvent);
 		}
 	}
 }
