 /**
  * Calendar
  * 
  * @author Darren Seung Won
  * @Process: In development 
  * TODO: 1. Make a list below the calendar
  *       2. Access to the database
  *       3. A little mark on the cell which has workout record
  *          (got an idea. I will implement it soon)
  *       
  * Last update: May 26 12
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
 import android.content.Intent;
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
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class CalendarActivity extends Activity implements OnClickListener {
 
 	private ImageView calJournalButton, preMonth, nextMonth;
 	private Button currentMonth;
 	private TextView nameofWorkout, durationtime;
 
 	private ListView calendarList;
 	private Listadapter cal_adapter;
 
 	private GridView calView;
 	private GridAdapter gridAdapter; // inner class to handle adapter
 	private Calendar derpCal;
 	private int month, year;
 	private final DateFormat dateFormatter = new DateFormat();
 	private static final String dateTemplate = "MMMM yyyy";
 
 	private static final String tag = "CalendarActivity";
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.calendar_main);
 
 		// declare a new calendar object with built-in calendar
 		derpCal = Calendar.getInstance(Locale.getDefault());
 		month = derpCal.get(Calendar.MONTH) + 1;
 		year = derpCal.get(Calendar.YEAR);
 
 		preMonth = (ImageView) this.findViewById(R.id.preMonth);
 		preMonth.setOnClickListener(this);
 		// ImageView is used like a button
 
 		currentMonth = (Button) this.findViewById(R.id.currentMonth);
 		currentMonth.setText(dateFormatter.format(dateTemplate, derpCal.getTime()));
 
 		nextMonth = (ImageView) this.findViewById(R.id.nextMonth);
 		nextMonth.setOnClickListener(this);
 
 		calendarList = (ListView) this.findViewById(R.id.calendar_list);
 		cal_adapter = new Listadapter(getApplicationContext(), month, year);
 		cal_adapter.notifyDataSetChanged();
 		calendarList.setAdapter(cal_adapter);
 
 		nameofWorkout = (TextView) this.findViewById(R.id.cal_workoutname);
 		durationtime = (TextView) this.findViewById(R.id.cal_durationtime);
 
 		calView = (GridView) this.findViewById(R.id.calendargrid);
 		gridAdapter = new GridAdapter(getApplicationContext(), month, year);
 		gridAdapter.notifyDataSetChanged();
 		calView.setAdapter(gridAdapter);
 	}
 
 	public void onClick(View view) {
 		if (view == preMonth) {
 			if (month <= 1) {
 				month = 12;
 				year--;
 			} else {
 				month--;
 			}
 			setGridAdapterToDate(month, year);
 		}
 
 		if (view == nextMonth) {
 			if (month > 11) {
 				month = 1;
 				year++;
 			} else
 				month++;
 		}
 		setGridAdapterToDate(month, year);
 	}
 
 	public void onDestroy() {
 		super.onDestroy();
 		// Activity shut down
 	}
 
 	private void setGridAdapterToDate(int month, int year) {
 		gridAdapter = new GridAdapter(getApplicationContext(), month, year);
 		derpCal.set(year, month - 1, derpCal.get(Calendar.DAY_OF_MONTH));
 		// Field number for get and set indicating the day of the month.
 		// This is a synonym for DATE. The first day of the month has value 1.
 
 		currentMonth.setText(dateFormatter.format(dateTemplate, derpCal.getTime()));
 		gridAdapter.notifyDataSetChanged();
 		calView.setAdapter(gridAdapter);
 	}
 
 	/**
 	 * Name: CalendarListActivity Inner class to handle the calendar list adapter
 	 * 
 	 * 
 	 * 
 	 */
 	public class Listadapter extends BaseAdapter {
 
 		private Context context;
 		int listMonth, listYear;
 
 		String temp_nameofworkout, temp_durationtime;
 
 		public Listadapter(Context _context, int month, int year) {
 			super();
 			this.context = _context;
 			listMonth = month;
 			listYear = year;
 
 		}
 
 		public void setListAdapter(int month, int year) {
 
 		}
 
 		/**
 		 * Name: getData Description: When the user clicked one of days on the
 		 * calendar, this method, getData, find out there is workout data in that
 		 * selected day. If the user has done some workout in that day, this method
 		 * retrieves data, name of workout and duration time.
 		 * 
 		 * @param Listyear
 		 * @param Listmonth
 		 * @return
 		 */
 		// TODO: Make this method work right.
 		private HashMap gettingData(int Listyear, int Listmonth) {
 
 			HashMap map = new HashMap<String, Integer>();
 
 			return map;
 		}
 
 		public int getCount() {
 			// TODO Auto-generated method stub
 			return 0;
 		}
 
 		public Object getItem(int arg0) {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		public long getItemId(int arg0) {
 			// TODO Auto-generated method stub
 			return 0;
 		}
 
 		public View getView(int arg0, View arg1, ViewGroup arg2) {
 			// TODO Auto-generated method stub
 			return null;
 		}
 	}
 
 	/**
 	 * Name: GridAdapter Inner class to handle the calendar grid adapter
 	 * 
 	 * 
 	 * 
 	 */
 
 	public class GridAdapter extends BaseAdapter implements OnClickListener {
 
 		private static final String tag = "GridAdapter";
 
 		private final Context cal_context;
 		private List<String> list;
 		private static final int DAY_OFFSET = 1;
 		private final String[] weekdays = new String[] { "Sun", "Mon", "Tue",
 				"Wed", "Thu", "Fri", "Sat" };
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
 		private int currentMonth_value, currentYear_value;
 
 		private final SimpleDateFormat dateFormatter = new SimpleDateFormat(
 				"dd-MMM-yyyy");
 
 		public GridAdapter(Context context, int month, int year) {
 			super();
 			this.cal_context = context;
 			this.list = new ArrayList<String>();
 			this.month = month;
 			this.year = year;
 
 			Calendar tempcal = Calendar.getInstance();
 			setCurrentDayOfMonth(tempcal.get(Calendar.DAY_OF_MONTH));
 			setCurrentWeekDay(tempcal.get(Calendar.DAY_OF_WEEK));
 			currentMonth_value = tempcal.get(Calendar.MONTH) + 1;
			currentYear_value = tempcal.get(Calendar.YEAR);
 			createMonth(month, year);
 
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
 
 		private void createMonth(int mon, int year) {
 
 			int trailingSpaces = 0;
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
 
 			if (cal.isLeapYear(cal.get(Calendar.YEAR)) && month == 2) {
 				daysInMonth++;
 			}
 
 			for (int i = 0; i < trailingSpaces; i++) {
 				list.add(String.valueOf((daysInPrevMonth - trailingSpaces + DAY_OFFSET)
 						+ i)
 						+ "-GREY" + "-" + getMonthAsString(prevMonth) + "-" + prevYear);
 			}
 
 			int indexCount = trailingSpaces - 1;
 			String tempString;
 
 			// Current Month Days
 			for (int i = 1; i <= daysInMonth; i++) {
 				list.add(String.valueOf(i) + "-WHITE" + "-"
 						+ getMonthAsString(currentMonth) + "-" + year);
 				indexCount++;
 
 				if (indexCount % 7 == 6) {
 					tempString = String.valueOf(i) + "-BLUE" + "-"
 							+ getMonthAsString(currentMonth) + "-" + year;
 					list.set(indexCount, tempString);
 				} else if (indexCount % 7 == 0) {
 					tempString = String.valueOf(i) + "-RED" + "-"
 							+ getMonthAsString(currentMonth) + "-" + year;
 					list.set(indexCount, tempString);
 				}
 
 				if (i == getCurrentDayOfMonth() && month == currentMonth_value
 						&& year == currentYear_value) {
 					tempString = String.valueOf(i) + "-YELLOW" + "-"
 							+ getMonthAsString(currentMonth) + "-" + year;
 					list.set(indexCount, tempString);
 				}
 			}
 
 			// Leading Month days
 			for (int i = 0; i < list.size() % 7; i++) {
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
 
 			// Get a reference to the Day
 			gridcell = (Button) row.findViewById(R.id.calendar_day_gridcell);
 			gridcell.setOnClickListener(this);
 
 			String[] day_color = list.get(position).split("-");
 			String theday = day_color[0];
 			String themonth = day_color[2];
 			String theyear = day_color[3];
 
 			// set color for days
 			gridcell.setText(theday);
 			gridcell.setTag(theday + "-" + themonth + "-" + theyear);
 
 			if (day_color[1].equals("GREY")) {
 				gridcell.setTextColor(Color.GRAY);
 			}
 			if (day_color[1].equals("WHITE")) {
 				gridcell.setTextColor(Color.WHITE);
 			}
 			if (day_color[1].equals("BLUE")) {
 				gridcell.setTextColor(getResources().getColor(
 						R.color.static_text_blue));
 			}
 			if (day_color[1].equals("YELLOW")) {
 				gridcell.setTextColor(getResources().getColor(
 						R.color.static_text_yellow));
 			}
 			if (day_color[1].equals("RED")) {
 				gridcell.setTextColor(getResources().getColor(
 						R.color.static_text_red));
 			}
 			return row;
 		}
 
 		// TODO: Need to implement onClick method to retrieve data and put those on
 		// the list
 		public void onClick(View view) {
 			
 			String date_month_year = (String) view.getTag();
 			try {
 				Date parsedDate = dateFormatter.parse(date_month_year);
 				Log.d(tag, "Touched date :" + parsedDate.toString());
 
 			} catch (ParseException e) {
 				e.printStackTrace();
 			}
 
 		}
 
 		public int getCurrentDayOfMonth() {
 			return currentDayOfMonth - 1;
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
 
 	}
 }
