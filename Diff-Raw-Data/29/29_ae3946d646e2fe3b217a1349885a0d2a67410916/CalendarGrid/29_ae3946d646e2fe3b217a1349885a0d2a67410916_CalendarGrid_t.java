 /*CalControl (c) Richard Beech 2013 plug in calendar conrol for android 2.2 up
 features: Selectable android Themes, automatic layout of days in month within a 
 grid, with padding*/
 package com.examples;
 
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 import java.util.Locale;
 
 import android.content.Context;
 import android.graphics.Color;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.Button;
 import android.widget.GridView;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.Toast;
 
 public class CalendarGrid extends LinearLayout   implements OnClickListener
 
 { Context appContext;
 private static final String tag = "CalConntrol";
 private Button currentMonth;
 private ImageView prevMonth;
 private ImageView nextMonth;
 private GridView calendarGrid;
 private GridCellAdapter adapter;
 private Calendar _calendar;
 private int month, year;
 
 public CalendarGrid(Context context) 
 {
 	super(context);
 	if(!isInEditMode())
 		init(context);
 	appContext=context;
 	
 }
 
 public CalendarGrid(Context context, AttributeSet attrs) 
 {
 	super(context,attrs);
 	if(!isInEditMode())
 		init(context);
 	appContext=context;
 	
 }
 
 
 /*private void getRequestParameters()
 			{
 				Intent intent = getIntent();
 				if (intent != null)
 					{
 						Bundle extras = intent.getExtras();
 						if (extras != null)
 							{
 								if (extras != null)
 									{
 										Log.d(tag, "+++++----------------->" + extras.getString("params"));
 									}
 							}
 					}
 			}*/
 
 /** Called when the view is first created. */
 private void init(Context context){
 
 
 	try {
 
 		_calendar = Calendar.getInstance(Locale.getDefault());
 		month = _calendar.get(Calendar.MONTH);
 		year = _calendar.get(Calendar.YEAR);
 
 		LayoutInflater inflater = (LayoutInflater) context
 				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		inflater.inflate(R.layout.calendargrid, this,true);
 
 		calendarGrid = (GridView) this.findViewById(R.id.calendar);
 		prevMonth = (ImageView) this.findViewById(R.id.prevMonth);
 		prevMonth.setOnClickListener(this);
 
 		currentMonth = (Button) this.findViewById(R.id.currentMonth);
 		Date date = _calendar.getTime();
 		String formattedDate = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(date);
 		currentMonth.setText(formattedDate.toString());
 
 		nextMonth = (ImageView) this.findViewById(R.id.nextMonth);
 		nextMonth.setOnClickListener(this);
 
 
 
 		// Initialised
 		adapter = new GridCellAdapter(getContext(), R.id.gridcell, month, year);
 		adapter.notifyDataSetChanged();
 		calendarGrid.setAdapter(adapter);
 		
 	} catch (Exception e) {
 		System.out.println("Calcontrol error "+e);
 		Log.d(tag, "Calcontrol error " + e);
 		e.printStackTrace();
 	}
 
 }
 @Override
 protected void onLayout(boolean changed, int l, int t, int r, int b) {
     // TODO Auto-generated method stub
     for(int i = 0 ; i < getChildCount() ; i++){
         getChildAt(i).layout(l, t, r, b);
     }
 }
 
 @Override
 protected void onFinishInflate(){
 
 
 	super.onFinishInflate();
 }
 
 @Override
 public void onClick(View v)
 {
 	if (v == prevMonth)
 	{
 		if (month <= 1)
 		{
 			month = 11;
 			year--;
 		} else
 		{
 			month--;
 		}
 
 		Log.d(tag, "Before 1 MONTH " + "Month: " + month + " " + "Year: " + year);
 		adapter = new GridCellAdapter(getContext(), R.id.gridcell, month, year);
 		_calendar.set(year, month, _calendar.get(Calendar.DAY_OF_MONTH));
 		Date date = _calendar.getTime();
 		String formattedDate = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(date);
 		currentMonth.setText(formattedDate);
 
 		adapter.notifyDataSetChanged();
 		calendarGrid.setAdapter(adapter);
 	}
 	if (v == nextMonth)
 	{
 		if (month >= 11)
 		{
 			month = 0;
 			year++;
 		} else
 		{
 			month++;
 		}
 
 		Log.d(tag, "After 1 MONTH " + "Month: " + month + " " + "Year: " + year);
 		adapter = new GridCellAdapter(getContext(), R.id.gridcell, month, year);
 		_calendar.set(year, month, _calendar.get(Calendar.DAY_OF_MONTH));
 		Date date = _calendar.getTime();
 		String formattedDate = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(date);
 		currentMonth.setText(formattedDate);
 		adapter.notifyDataSetChanged();
 		calendarGrid.setAdapter(adapter);
 	}
 }
 
 //
 public class GridCellAdapter extends BaseAdapter implements OnClickListener
 {
 	private static final String tag = "GridCellAdapter";
 	private final Context _context;
 	private final List<String> list;
 	private final String[] weekdays = new String[] { "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat" };
 	private final String[] months = { "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December" };
 	private final int[] daysOfMonth = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
 	private final int month, year;
 	int daysInMonth, prevMonthDays;
 	private final int currentDayOfMonth;
 	private Button gridcell;
 
 	// Days in Current Month
 	public GridCellAdapter(Context context, int textViewResourceId, int month, int year)
 	{
 		super();
 		this._context = context;
 		this.list = new ArrayList<String>();
 		this.month = month;
 		this.year = year;
 
 		Log.d(tag, "Month: " + month + " " + "Year: " + year);
 		Calendar calendar = Calendar.getInstance();
 		currentDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
 
 		printMonth(month, year);
 	}
 
 	public String getItem(int position)
 	{
 		return list.get(position);
 	}
 
 	@Override
 	public int getCount()
 	{
 		return list.size();
 	}
 
 	private void printMonth(int mm, int yy)
 	{
 		// The number of days to leave blank at
 		// the start of this month.
 		int trailingSpaces = 0;
 		int leadSpaces = 0;
 		int daysInPrevMonth = 0;
 		int prevMonth = 0;
 		int prevYear = 0;
 		int nextMonth = 0;
 		int nextYear = 0;
 
 		GregorianCalendar cal = new GregorianCalendar(yy, mm, currentDayOfMonth);
 
 		// Days in Current Month
 		daysInMonth = daysOfMonth[mm];
 		int currentMonth = mm;
 		if (currentMonth == 11)
 		{
 			prevMonth = 10;
 			daysInPrevMonth = daysOfMonth[prevMonth];
 			nextMonth = 0;
 			prevYear = yy;
 			nextYear = yy + 1;
 		} else if (currentMonth == 0)
 		{
 			prevMonth = 11;
 			prevYear = yy - 1;
 			nextYear = yy;
 			daysInPrevMonth = daysOfMonth[prevMonth];
 			nextMonth = 1;
 		} else
 		{
 			prevMonth = currentMonth - 1;
 			nextMonth = currentMonth + 1;
 			nextYear = yy;
 			prevYear = yy;
 			daysInPrevMonth = daysOfMonth[prevMonth];
 		}
 
 		// Compute how much to leave before before the first day of the
 		// month.
 		// getDay() returns 0 for Sunday.
 		trailingSpaces = cal.get(Calendar.DAY_OF_WEEK) - 1;
 
 		if (cal.isLeapYear(cal.get(Calendar.YEAR)) && mm == 1)
 		{
 			++daysInMonth;
 		}
 
 		// Trailing Month days
 		for (int i = 0; i < trailingSpaces; i++)
 		{
			list.add(String.valueOf((daysInPrevMonth - trailingSpaces + 1) + i)
					+ "-GRAY" + "-" + months[prevMonth] + "-" + prevYear);
			
 		}
 
 		// Current Month Days
 		for (int i = 1; i <= daysInMonth; i++)
 		{
			if (i==currentDayOfMonth){
				list.add(String.valueOf(i) + "-CYAN"
			+ "-" + months[mm] + "-" + yy);
			}else{
			list.add(String.valueOf(i) + "-WHITE" 
			+ "-" + months[mm] + "-" + yy);
			}
 		}
 
 		// Leading Month days
 		for (int i = 0; i < list.size() % 7; i++)
 		{
 			Log.d(tag, "NEXT MONTH:= " + months[nextMonth]);
			list.add(String.valueOf(i + 1) + "-GRAY"
			+ "-" + months[nextMonth] + "-" + nextYear);
 		}
 	}
 
 	@Override
 	public long getItemId(int position)
 	{
 		return position;
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent)
 	{
 		Log.d(tag, "getView ...");
 		View row = convertView;
 		if (row == null)
 		{
 			// ROW INFLATION
 			Log.d(tag, "Starting XML Row Inflation ... ");
 			LayoutInflater inflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			row = inflater.inflate(R.layout.gridcell, parent, false);
 
 			Log.d(tag, "Successfully completed XML Row Inflation!");
 		}
 
 		gridcell = (Button) row.findViewById(R.id.gridcell);
 		gridcell.setOnClickListener(this);
 
 		// ACCOUNT FOR SPACING
 
 		Log.d(tag, "Current Day: " + currentDayOfMonth);
 		String[] day_color = list.get(position).split("-");
 		gridcell.setText(day_color[0]);
 		gridcell.setTag(day_color[0] + "-" + day_color[2] + "-" + day_color[3]);
 
 		if (day_color[1].equals("BLACK"))
 		{
 			gridcell.setTextColor(Color.BLACK);
 		}
		if (day_color[1].equals("GRAY"))
		{
			gridcell.setTextColor(Color.GRAY);
		}
		if (day_color[1].equals("RED"))
		{
			gridcell.setTextColor(Color.RED);
		}
 		if (day_color[1].equals("WHITE"))
 		{
 			gridcell.setTextColor(Color.WHITE);
 		}
		if (day_color[1].equals("CYAN"))
 		{
 			gridcell.setTextColor(Color.CYAN);
 		}
 
 		return row;
 	}
 
 	@Override
 	public void onClick(View view)
 	{
 		String date_month_year = (String) view.getTag();
 		Toast.makeText(getContext(), date_month_year, Toast.LENGTH_SHORT).show();
 
 	}
 }
 
 
 }
