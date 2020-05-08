 package uk.ac.dur.duchess.ui.activity;
 
 import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
 import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 
 import uk.ac.dur.duchess.R;
 import uk.ac.dur.duchess.ui.view.EventListView;
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.content.res.Configuration;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.LinearLayout.LayoutParams;
 import android.widget.TextView;
 
 import com.actionbarsherlock.view.Menu;
 
 public class CalendarActivity extends SortableListActivity
 {
 	private Calendar calendar;
 	private int lowerBound;
 	private int upperBound;
 	private int lastBound;
 	private int currentDay = -1;
 
 	private Bitmap todayIndicator;
 
 	private TextView monthText;
 	private ImageView prevMonthButton;
 	private ImageView nextMonthButton;
 
 	private LinearLayout calendarView;
 	
 	private CalendarButton currentCell;
 
 	private Context context;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.calendar_layout);
 
 		context = this;
 
 		monthText = (TextView) findViewById(R.id.monthText);
 		prevMonthButton = (ImageView) findViewById(R.id.prevMonthButton);
 		nextMonthButton = (ImageView) findViewById(R.id.nextMonthButton);
 
 		calendarView = (LinearLayout) findViewById(R.id.calendarView);
 
 		listView = (EventListView) findViewById(R.id.calendarListView);
 
 		calendar = Calendar.getInstance();
 
 		setMonthBounds(calendar.get(Calendar.MONTH));
 
 		SimpleDateFormat month = new SimpleDateFormat("MMMMM yyyy");
 
 		monthText.setText(month.format(calendar.getTime()));
 
 		prevMonthButton.setOnClickListener(new View.OnClickListener()
 		{
 			@Override
 			public void onClick(View v)
 			{
 				calendar.add(Calendar.MONTH, -1);
 				setMonthBounds(calendar.get(Calendar.MONTH));
 
 				SimpleDateFormat month = new SimpleDateFormat("MMMMM yyyy");
 
 				monthText.setText(month.format(calendar.getTime()));
 
 				setupCalendarLayout();
 			}
 		});
 
 		nextMonthButton.setOnClickListener(new View.OnClickListener()
 		{
 			@Override
 			public void onClick(View v)
 			{
 				calendar.add(Calendar.MONTH, 1);
 				setMonthBounds(calendar.get(Calendar.MONTH));
 
 				SimpleDateFormat month = new SimpleDateFormat("MMMMM yyyy");
 
 				monthText.setText(month.format(calendar.getTime()));
 
 				setupCalendarLayout();
 			}
 		});
 
 		todayIndicator =
 				BitmapFactory.decodeResource(getResources(), R.drawable.today_indicator);
 
 		listView.setBackgroundDrawable(getResources().getDrawable(R.drawable.top_bottom_border));
 
 		setupCalendarLayout();
 
 		currentCell.setTextColor(Color.WHITE);
 		currentCell.setBackgroundColor(Color.parseColor("#8560A8"));
 
 		String fromDate = cellToDate(currentCell.cell);
 		String toDate   = cellToDate(currentCell.cell + 1);
 
 		Bundle bundle = new Bundle();
 		bundle.putString("from_date", fromDate);
 		bundle.putString("to_date", toDate);
 
 		if(isLongScreen(this)) listView.loadAllEvents(this, bundle);
 	}
 
 	private void setupCalendarLayout()
 	{
 		calendarView.removeAllViews();
 
 		int cell = 1 - lowerBound;
 
 		for(int row = 0; row < 6; row++)
 		{
 			LinearLayout tableRow = new LinearLayout(this);
 			tableRow.setLayoutParams(new LayoutParams(MATCH_PARENT, WRAP_CONTENT, 0));
 
 			for(int col = 0; col < 7; col++)
 			{
 				LayoutParams cellParams = new LayoutParams(0, WRAP_CONTENT, 1);
 
 				int left   = (col == 0) ? 6 : 3;
 				int top    = (row == 0) ? 6 : 3;
 				int right  = (col == 6) ? 6 : 3;
 				int bottom = (row == 5) ? 6 : 3;
 
 				cellParams.setMargins(left, top, right, bottom);
 
 				tableRow.addView(getMonthButton(cell, col), cellParams);
 
 				cell++;
 			}
 
 			calendarView.addView(tableRow);
 		}
 
 		listView.clearAdapter();
 	}
 
 	private Button getMonthButton(int cell, int col)
 	{
 		int day = cell;
 		int color = Color.BLACK;
 
 		if(col == 5) color = Color.parseColor("#0076A3");
 		else if(col == 6) color = Color.parseColor("#ED1C24");
 
 		if(day < 1)
 		{
 			day += lastBound;
 			color = Color.GRAY;
 		}
 		else if(day > upperBound)
 		{
 			day %= upperBound;
 			color = Color.GRAY;
 		}
 
 		CalendarButton button = new CalendarButton(this, cell, color);
 
 		button.setText(String.valueOf(day));
 		button.setTextSize(16);
 		
 		if(isLongScreen(context)) button.setPadding(5, 15, 5, 15);
 		else button.setPadding(5, 5, 5, 5);
 		button.setTextColor(color);
 		button.setBackgroundColor(Color.WHITE);
 
 		button.setOnClickListener(new OnClickListener()
 		{
 			@Override
 			public void onClick(View v)
 			{
 				CalendarButton b = (CalendarButton) v;
 
 				currentCell.setTextColor(currentCell.color);
 				currentCell.setBackgroundColor(Color.WHITE);
 
 				b.setTextColor(Color.WHITE);
 				v.setBackgroundColor(Color.parseColor("#8560A8"));
 				currentCell = b;
 
 				String fromDate = cellToDate(b.cell);
 				String toDate   = cellToDate(b.cell + 1);
 
 				if(isLongScreen(context))
 					listView.filterByDateRange(fromDate, toDate);
 				else
 				{
 					Intent i = new Intent(v.getContext(), CalendarEventListActivity.class);
 					i.putExtra("from_date", fromDate);
 					i.putExtra("to_date", toDate);
 					startActivity(i);
 				}
 			}
 		});
 
 		return button;
 	}
 
 	private class CalendarButton extends Button
 	{
 		private int cell;
 		private int color;
 
 		public CalendarButton(Context context, int cell, int color)
 		{
 			super(context);
 			this.cell = cell;
 			this.color = color;
 
 			if(cell == currentDay) currentCell = this;
 		}
 
 		@Override
 		protected void onDraw(Canvas canvas)
 		{
 			super.onDraw(canvas);		 
 
 			if(cell == currentDay && currentDay != -1)
 			{
 				float w = getWidth()  / 2;
 				float h = getHeight() / 2;
 
 				w -= todayIndicator.getWidth()  / 2;
 				h -= todayIndicator.getHeight() / 2;
 
 				canvas.drawBitmap(todayIndicator, w, h, null);
 			}
 		}
 	}
 
 	private void setMonthBounds(int month)
 	{
 		Calendar cal = Calendar.getInstance();
 
 		if(month == cal.get(Calendar.MONTH) &&
 				calendar.get(Calendar.YEAR) == cal.get(Calendar.YEAR))
 			currentDay = cal.get(Calendar.DAY_OF_MONTH);
 		else currentDay = -1;
 
 		cal.set(Calendar.YEAR, calendar.get(Calendar.YEAR));
 		cal.set(Calendar.MONTH, month);
 		cal.set(Calendar.DAY_OF_MONTH, 1);
 
 		lowerBound = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7;
 		upperBound = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
 
 		cal.set(Calendar.MONTH, month - 1);
 
 		lastBound = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
 	}
 
 	private String cellToDate(int cell)
 	{
 		SimpleDateFormat range = new SimpleDateFormat("yyyy-MM-dd");
 
 		Calendar cal = Calendar.getInstance();
 
 		cal.set(Calendar.YEAR, calendar.get(Calendar.YEAR));
 		cal.set(Calendar.MONTH, calendar.get(Calendar.MONTH));
 		cal.set(Calendar.DAY_OF_MONTH, cell);
 
 		return range.format(cal.getTime());
 	}
 
 	@Override
 	public void onResume()
 	{
 		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 		super.onResume();
 	}
 	
 	@Override
 	public void onConfigurationChanged(Configuration newConfig)
 	{
 	    super.onConfigurationChanged(newConfig);
 	    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 	}
 	
 	public boolean onCreateOptionsMenu(Menu menu)
 	{
 		if(isLongScreen(context)) return super.onCreateOptionsMenu(menu);
 		else return false;
 	}
 
 	@SuppressWarnings("deprecation")
 	public boolean isLongScreen(Context context)
	{
		if ((context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_LONG_MASK)
				== Configuration.SCREENLAYOUT_LONG_YES) return true;
		
 		int calendarHeight = findViewById(R.id.calendarView).getHeight() + calendarView.getHeight();
 		int screenHeight = getWindowManager().getDefaultDisplay().getHeight();
 
 		Log.d("HEIGHT", "Calendar: " + calendarHeight + ", Screen: " + screenHeight);
 		
 		if(calendarHeight != 0 && screenHeight - calendarHeight > 100) return true;
 		
		return false;
 	}
 }
