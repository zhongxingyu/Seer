 /**
  * Calendar
  * 
  * @author Darren Seung Won
  * @Process: In development 
  * TODO: Make a list below the calendar
  *       Access to the database
  *       a little mark on the cell which has workout record
  * Last update: May 25 12
  */
 
 package com.vorsk.crossfitr;
 
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
 import android.content.Context;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.text.format.DateFormat;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.BaseAdapter;
 import android.widget.Button;
 import android.widget.GridView;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 
 
 public class CalendarActivity extends Activity implements OnClickListener,
 		OnItemClickListener {
 	
 	private static final String tag = "CalendarActivity";
 	
 	private ImageView calJournalButton;
 	private Button currentMonth;
 	private ImageView preMonth, nextMonth;
 	private GridView calView;
 	private GridAdapter gridAdapter; //inner class to handle adapter
 	private Calendar derpCal;
 	private int month, year;
 	private final DateFormat dateFormatter = new DateFormat();
 	private static final String dateTemplate = "MMMM yyyy";
 	
 	public void onCreate(Bundle savedInstanceState){
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.calendar_main);
 		
 		// declare a new calendar object with built-in calendar
 		derpCal = Calendar.getInstance(Locale.getDefault());
 		month = derpCal.get(Calendar.MONTH) + 1;
 		year = derpCal.get(Calendar.YEAR);
 //		Log.d(tag, "Calendar Instance: = " + "Month " + month + " " + " Year: " + year);
 		
 //		selectedButton = (Button) this.findViewById(R.id.selectedButton);
 		
 		preMonth = (ImageView) this.findViewById(R.id.preMonth);
 		preMonth.setOnClickListener(this);
 		// ImageView is used like a button
 		
 		currentMonth = (Button) this.findViewById(R.id.currentMonth);
 		currentMonth.setText(dateFormatter.format(dateTemplate, derpCal.getTime()));
 		
 		nextMonth = (ImageView) this.findViewById(R.id.nextMonth);
 		nextMonth.setOnClickListener(this);
 		
 		calView = (GridView) this.findViewById(R.id.calendargrid);
 		
 		gridAdapter = new GridAdapter(getApplicationContext(), R.id.calendar_day_gridcell, month, year);
 		gridAdapter.notifyDataSetChanged();
 		calView.setAdapter(gridAdapter);		
 	}
 	
 	public void onClick(View view){
 		if(view == preMonth){
 			if(month <= 1){
 				month += 12;
 				year--;
 			}else{
 				month--;
 		}
 			setGridAdapterToDate(month, year);
 	}
 	
 		if(view == nextMonth){
 			if(month > 11){
 				month = 1;
 				year++;
 			}else
 				month ++;
 		}
 		setGridAdapterToDate(month, year);
 	}
 	
 	public void onDestroy(){
 		super.onDestroy();
 		// Activity shut down
 	}
 		
 	private void setGridAdapterToDate(int month, int year){
 		gridAdapter = new GridAdapter(getApplicationContext(), R.id.calendar_day_gridcell, month, year);
 		derpCal.set(year, month - 1, derpCal.get(Calendar.DAY_OF_MONTH));
 		//Field number for get and set indicating the day of the month. 
 		// This is a synonym for DATE. The first day of the month has value 1.
 		
 		currentMonth.setText(dateFormatter.format(dateTemplate, derpCal.getTime()));
 		gridAdapter.notifyDataSetChanged();
 		calView.setAdapter(gridAdapter);
 	}
 	
 
 	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
 	  // TODO Auto-generated method stub
 	  
   }
 	
 	public class GridAdapter extends BaseAdapter implements OnClickListener {
 
 		private static final String tag = "GridAdapter";
 
 		private final Context cal_context;
 		private List<String> list;
 		private static final int DAY_OFFSET = 1;
 		private final String[] weekdays = new String[] { "Sun", "Mon", "Tue", "Wed",
 		    "Thu", "Fri", "Sat" };
 		// Strings for day
 		private final String[] months = { "January", "February", "March", "April",
 		    "May", "June", "July", "August", "September", "October", "November",
 		    "December" }; // Strings for month
 		private final int[] daysOfMonth = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31,
 		    30, 31 }; // the number of days of each month
 		private final int month, year;
 		private int daysInMonth, prevMonthDays;
 		private int currentDayOfMonth;
 		private int currentWeekDay;
 		private Button gridcell;
 		private TextView num_events_per_day;
 		private final HashMap eventsPerMonthMap;
 		private final SimpleDateFormat dateFormatter = new SimpleDateFormat(
 		    "dd-MMM-yyyy");
 
 		public GridAdapter(Context context, int tViewId, int month, int year) {
 			super();
 			this.cal_context = context;
 			this.list = new ArrayList<String>();
 			this.month = month;
 			this.year = year;
 
 			Calendar tempcal = Calendar.getInstance();
 			setCurrentDayOfMonth(tempcal.get(Calendar.DAY_OF_MONTH));
 			setCurrentWeekDay(tempcal.get(Calendar.DAY_OF_WEEK));
 
 			printMonth(month, year);
 			eventsPerMonthMap = findNumberOfEventsPerMonth(year, month);
 
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
 
 		public String getItem(int arg0) {
 			return list.get(arg0);
 		}
 
 		public int getCount() {
 			return list.size();
 		}
 
 		public long getItemId(int arg0) {
 			return arg0;
 		}
 
 		private void printMonth(int mon, int year) {
 
 			int trailingSpaces = 0;
 			int leadSpaces = 0;
 
 			int daysInPrevMonth = 0;
 			int prevMonth = 0;
 			int prevYear = 0;
 			int nextMonth = 0;
 			int nextYear = 0;
 
 			int currentMonth = mon - 1;
 			String NameofCurrentMonth = getMonthAsString(currentMonth);
 			daysInMonth = getNumberOfDaysOfMonth(currentMonth);
 
 			GregorianCalendar cal = new GregorianCalendar(year, currentMonth, 1);
 
 			if (currentMonth == 11) {
 				prevMonth = currentMonth - 1;
 				daysInPrevMonth = getNumberOfDaysOfMonth(prevMonth);
 				nextMonth = 0;
 				prevYear = year;
 				nextYear = year + 1;
 			} else if (currentMonth == 0) {
 				prevMonth = 11;
 				prevYear = year - 1;
 				nextYear = year;
 				daysInPrevMonth = getNumberOfDaysOfMonth(prevMonth);
 				nextMonth = 1;
 			} else {
 				prevMonth = currentMonth - 1;
 				nextMonth = currentMonth + 1;
 				nextYear = year;
 				prevYear = year;
 				daysInPrevMonth = getNumberOfDaysOfMonth(prevMonth);
 			}
 
 			int currentWeekDay = cal.get(Calendar.DAY_OF_WEEK) - 1;
 			trailingSpaces = currentWeekDay;
 
 			if (cal.isLeapYear(cal.get(Calendar.YEAR)) && month == 1) {
 				++daysInMonth;
 			}
 
 			for (int i = 0; i < trailingSpaces; i++) {
 				Log.d(
 				    tag,
 				    "PREV MONTH:= "
 				        + prevMonth
 				        + " => "
 				        + getMonthAsString(prevMonth)
 				        + " "
 				        + String.valueOf((daysInPrevMonth - trailingSpaces + DAY_OFFSET)
 				            + i));
 				list.add(String.valueOf((daysInPrevMonth - trailingSpaces + DAY_OFFSET)
 				    + i)
 				    + "-GREY" + "-" + getMonthAsString(prevMonth) + "-" + prevYear);
 			}
 
 			// Current Month Days
 			for (int i = 1; i <= daysInMonth; i++) {
 				if (i == getCurrentDayOfMonth()) {
 					list.add(String.valueOf(i) + "-BLUE" + "-"
 					    + getMonthAsString(currentMonth) + "-" + year);
 				} else {
 					list.add(String.valueOf(i) + "-WHITE" + "-"
 					    + getMonthAsString(currentMonth) + "-" + year);
 				}
 			}
 
 			// Leading Month days
 			for (int i = 0; i < list.size() % 7; i++) {
 				Log.d(tag, "NEXT MONTH:= " + getMonthAsString(nextMonth));
 				list.add(String.valueOf(i + 1) + "-GREY" + "-"
 				    + getMonthAsString(nextMonth) + "-" + nextYear);
 			}
 		}
 
 		public View getView(int position, View convertView, ViewGroup parent) {
 			View row = convertView;
 			if (row == null) {
 				LayoutInflater inflater = (LayoutInflater) cal_context
 				    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 				row = inflater.inflate(R.layout.calendar_gridcell, parent, false);
 			}
 
 			// Get a reference to the Day gridcell
 			gridcell = (Button) row.findViewById(R.id.calendar_day_gridcell);
 			gridcell.setOnClickListener(this);
 
 			String[] day_color = list.get(position).split("-");
 			String theday = day_color[0];
 			String themonth = day_color[2];
 			String theyear = day_color[3];
 			if ((!eventsPerMonthMap.isEmpty()) && (eventsPerMonthMap != null)) {
 				if (eventsPerMonthMap.containsKey(theday)) {
 					num_events_per_day = (TextView) row
 					    .findViewById(R.id.num_events_per_day);
 					Integer numEvents = (Integer) eventsPerMonthMap.get(theday);
 					num_events_per_day.setText(numEvents.toString());
 				}
 			}
 
 			// Set the Day GridCell
 			gridcell.setText(theday);
 			gridcell.setTag(theday + "-" + themonth + "-" + theyear);
 			Log.d(tag, "Setting GridCell " + theday + "-" + themonth + "-" + theyear);
 
 			if (day_color[1].equals("GREY")) {
 				gridcell.setTextColor(Color.LTGRAY);
 			}
 			if (day_color[1].equals("WHITE")) {
 				gridcell.setTextColor(Color.WHITE);
 			}
 			if (day_color[1].equals("BLUE")) {
 				gridcell.setTextColor(getResources()
 				    .getColor(R.color.static_text_color));
 			}
 			return row;
 		}
 
 		public void onClick(View view) {
 			String date_month_year = (String) view.getTag();
 			CalendarActivity calendar = new CalendarActivity();
 //			calendar.selectedButton.setText("Selected: " + date_month_year);
 
 			try {
 				Date parsedDate = dateFormatter.parse(date_month_year);
 				Log.d(tag, "Parsed Date: " + parsedDate.toString());
 
 			} catch (ParseException e) {
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
 		
 		/**
 		 * retrieve ALL entries from a SQLite database for that month. Iterate over the List of
 		 * All entries, and get the dateCreated, which is converted into day.
 		 * @param year
 		 * @param month
 		 * @return
 		 */
 		private HashMap findNumberOfEventsPerMonth(int year, int month) {
 			HashMap map = new HashMap<String, Integer>();
 
 			return map;
 		}
 	}
 	
 }
