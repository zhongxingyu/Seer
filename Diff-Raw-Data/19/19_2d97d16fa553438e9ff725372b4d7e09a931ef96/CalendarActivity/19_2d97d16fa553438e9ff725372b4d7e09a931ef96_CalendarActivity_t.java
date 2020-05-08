 package uk.ac.dur.duchess.activity;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Color;
 import android.graphics.Typeface;
 import android.graphics.drawable.GradientDrawable;
 import android.graphics.drawable.GradientDrawable.Orientation;
 import android.os.Bundle;
 import android.view.Display;
 import android.view.Gravity;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.LinearLayout.LayoutParams;
 import android.widget.TextView;
 
 public class CalendarActivity extends Activity
 {
 	private Calendar calendar;
 	private int lowerBound;
 	private int upperBound;
 	private int lastBound;
 	
 	private LinearLayout layout;
 	private LayoutParams params;
 	private LinearLayout header;
 	private LinearLayout subHeader;
 	
 	private boolean[] dates = new boolean[42];
 	
 	private Button confirmButton;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		
 		layout = new LinearLayout(this);
 		params = new LayoutParams(-1, -2, 0);
 		
 		Display display = ((android.view.WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
 		int screenOrientation = display.getOrientation();
 		
 		if(screenOrientation != 0) params = new LayoutParams(-1, -1, 1);
 		
 		layout.setLayoutParams(params);
 		
 		calendar = Calendar.getInstance();
 		
 		setMonthBounds(calendar.get(Calendar.MONTH));
 		
 		int cell = 1 - lowerBound;
 		
 		header = new LinearLayout(this);
 		header.setLayoutParams(new LinearLayout.LayoutParams(-1, -2, 0));
 		header.setOrientation(1);
 		
 		subHeader = new LinearLayout(this);
 		subHeader.setLayoutParams(new LinearLayout.LayoutParams(-1, -2, 1));
 		
 		TextView monthText = new TextView(this);
 		
 		SimpleDateFormat month = new SimpleDateFormat("MMMMM yyyy");
 		
 		monthText.setText(month.format(calendar.getTime()));
 		monthText.setTextColor(Color.BLACK);
 		monthText.setTextSize(15);
 		monthText.setTypeface(Typeface.SERIF, Typeface.BOLD);
 		monthText.setPadding(5, 5, 5, 5);
 		
 		int[] colors = {Color.parseColor("#7E317B"), Color.parseColor("#D8ACE0")};
 		
 		GradientDrawable gradient = new GradientDrawable(Orientation.BOTTOM_TOP, colors);
 		gradient.setDither(true);
 		
 		header.setBackgroundDrawable(gradient);
 		
 		subHeader.addView(monthText, new LinearLayout.LayoutParams(-2, -2, 1));
 		
 		confirmButton = new Button(this);
 		confirmButton.setText("Confirm");
 		
 		LayoutParams buttonParams = new LinearLayout.LayoutParams(-2, -2, 0);
 		buttonParams.setMargins(0, 5, 0, 5);
 		
 		confirmButton.setOnClickListener(new View.OnClickListener()
 		{
 			@Override
 			public void onClick(View v)
 			{
 				Intent returnIntent = new Intent();
 				
				String dateString = getDatesString();
 				
				if(!dateString.equals(""))
				{
					returnIntent.putExtra("dates", dateString);
					setResult(RESULT_OK, returnIntent);
					finish();
				}
				else
				{
					setResult(RESULT_CANCELED);
					finish();
				}
 			}
 		});
 		
 		subHeader.addView(confirmButton, buttonParams);
 		
 		header.addView(subHeader);
 		
 		LinearLayout days = new LinearLayout(this);
 		days.setLayoutParams(new LinearLayout.LayoutParams(-1, -2, 0));
 		
 		String[] dayStrings = {"Mo", "Tu", "We", "Th", "Fr", "Sa", "Su"};
 		
 		for(int i = 0; i < 7; i++)
 		{
 			TextView day = new TextView(this);
 			day.setText(dayStrings[i]);
 			day.setGravity(Gravity.CENTER);
 			day.setPadding(0, 0, 0, 5);
 			day.setTextColor(Color.BLACK);
 			day.setTextSize(10);
 			days.addView(day, new LinearLayout.LayoutParams(0, -2, 1));
 		}
 		
 		header.addView(days);
 		
 		layout.addView(header);
 		
 		for(int row = 0; row < 6; row++)
 		{
 			LinearLayout tableRow = new LinearLayout(this);
 			tableRow.setLayoutParams(new LinearLayout.LayoutParams(-1, LayoutParams.FILL_PARENT, 1));
 			
 			for(int col = 0; col < 7; col++)
 			{
 				tableRow.addView(getMonthButton(cell), new LinearLayout.LayoutParams(0, LayoutParams.FILL_PARENT, 1));
 				
 				cell++;
 			}
 			
 			layout.addView(tableRow);
 		}
 		
 		layout.setOrientation(1);
 		
 		setContentView(layout);
 	}
 	
 	private Button getMonthButton(int cell)
 	{
 		int day = cell;
 		int color = Color.BLACK;
 		
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
 		
 		button.setText(String.valueOf(((day < 10) ? " " : "") + day));
 		button.setTextColor(color);
 		button.setBackgroundColor(Color.WHITE);
 		
 		button.setOnClickListener(new OnClickListener()
 		{
 			@Override
 			public void onClick(View v)
 			{
 				CalendarButton b = (CalendarButton) v;
 				
 				if(b.clicked)
 				{
 					b.clicked = false;
 					b.setTextColor(b.color);
 					v.setBackgroundColor(Color.WHITE);
 					dates[mapCellToArray(b.cell)] = false;
 				}
 				else
 				{
 					b.clicked = true;
 					b.setTextColor(Color.parseColor("#7E317B"));
 					v.setBackgroundColor(Color.parseColor("#D8ACE0"));
 					dates[mapCellToArray(b.cell)] = true;
 				}
 			}
 			
 		});
 
 		return button;
 	}
 	
 	private class CalendarButton extends Button
 	{
 		private boolean clicked = false;
 		private int cell;
 		private int color;
 		
 		public CalendarButton(Context context, int cell, int color)
 		{
 			super(context);
 			this.cell = cell;
 			this.color = color;
 		}	
 	}
 	
 	private void setMonthBounds(int month)
 	{
 		Calendar calendar = Calendar.getInstance();
 		
 		calendar.set(Calendar.MONTH, month);
 		calendar.set(Calendar.DAY_OF_MONTH, 1);
 		
 		lowerBound = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7;
 		upperBound = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
 		
 		calendar.set(Calendar.MONTH, month - 1);
 		
 		lastBound = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
 	}
 	
 	private String cellToDate(int cell)
 	{
 		SimpleDateFormat range = new SimpleDateFormat("yyyy-MM-dd");
 		
 		Calendar cal = Calendar.getInstance();
 			
 		cal.set(Calendar.MONTH, calendar.get(Calendar.MONTH));
 		cal.set(Calendar.DAY_OF_MONTH, cell);
 		
 		return range.format(cal.getTime());
 
 	}
 	
 	private int mapCellToArray(int cell)
 	{
 		int offset = 1 - lowerBound;
 		
 		return cell - offset;
 	}
 	
 	private int mapArrayToCell(int n)
 	{
 		int offset = 1 - lowerBound;
 		
 		return n + offset;
 	}
 	
 	private String getDatesString()
 	{
 		String str = "";
 		
 		for(int i = 0; i < dates.length; i++)
			if(dates[i]) str += cellToDate(mapArrayToCell(i)) + ", ";
 		
 		return str;
 	}
 }
