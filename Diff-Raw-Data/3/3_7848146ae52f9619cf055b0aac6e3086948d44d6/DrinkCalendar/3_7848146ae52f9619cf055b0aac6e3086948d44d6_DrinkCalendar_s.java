 package com.example.drinkingapp;
 
 import java.text.DateFormatSymbols;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.GridView;
 import android.widget.TextView;
 
 public class DrinkCalendar extends Activity implements OnClickListener {
 
 	int selectedMonth, selectedYear;
 	Calendar calendar = Calendar.getInstance();
 	GridView drinkCalendar;
 	TextView monthDisplay, yearDisplay, bottomDisplay;
 	Button back, next;
 	ArrayList<Button> drinkBacButtons = new ArrayList<Button>();
 	ArrayList<String> numbers = new ArrayList<String>();
 	
 	private ArrayList<Integer> drinkingDays;
 	private ArrayList<Double> maxBac;
 	private ArrayList<Integer> bacColors;
 	private DatabaseHandler db;
 	private ArrayList<DatabaseStore> day_colors;
 	private ArrayList<DatabaseStore> day_values;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.drinkcalendar);
 
 		db = new DatabaseHandler(this);
 		drinkCalendar = (GridView) findViewById(R.id.gvDrinkCalendar);
 		monthDisplay = (TextView) findViewById(R.id.tvMonth);
 		yearDisplay = (TextView) findViewById(R.id.tvYear);
 		bottomDisplay = (TextView) findViewById(R.id.tvCalendarBottomDisplay);
 		back = (Button) findViewById(R.id.bPreviousMonth);
 		next = (Button) findViewById(R.id.bNextMonth);
 		back.setOnClickListener(this);
 		next.setOnClickListener(this);
 
 		drinkingDays = new ArrayList<Integer>();
 		maxBac = new ArrayList<Double>();
 		bacColors = new ArrayList<Integer>();
 		
 		//Get the values from the DB
 		Date date = new Date();
 		calculateValues(date);
 
 		selectedMonth = calendar.get(Calendar.MONTH);
 		selectedYear = calendar.get(Calendar.YEAR);
 		setMonthFromInt(selectedMonth);
 		yearDisplay.setText(Integer.toString(selectedYear));
 		ColorAdapter adapter = new ColorAdapter(this, selectedMonth,
 				selectedYear, drinkingDays, maxBac, bacColors);
 		drinkCalendar.setAdapter(adapter);
 		drinkBacButtons = adapter.getButtonView();
 		
 	}
 
 	/*
 	 * Construct necessary Lists for the DB
 	 */
 	private void convertToLists(ArrayList<DatabaseStore> color, ArrayList<DatabaseStore> values){
 		for(int i=0; i<values.size(); i++){
 			DatabaseStore ds = values.get(i);
 			drinkingDays.add(ds.day);
 			maxBac.add(Double.valueOf(ds.value));
 			bacColors.add(Integer.parseInt(color.get(i).value));
 		}
 	}
 	
 	/*
 	 * Must sort both color and values by time before calling.
 	 */
 	private void getMaxForDays(ArrayList<DatabaseStore> colors, ArrayList<DatabaseStore> values){
 		assert(colors.size() == values.size());
 		
 		day_colors = new ArrayList<DatabaseStore>();
 		day_values = new ArrayList<DatabaseStore>();
 		
 		DatabaseStore max_day=null;
 		DatabaseStore max_color=null;
 		for(int i=0; i< values.size(); i++){
 			DatabaseStore s = values.get(i);
 			if(max_day == null){
 				max_day = s;
 				max_color = colors.get(i);
 			}else{
 				if(max_day.day < s.day){
 					day_colors.add(max_color);
 					day_values.add(max_day);
 					max_day = s;
 					max_color = colors.get(i);
 				} else if(Double.valueOf(max_day.value)< Double.valueOf(s.value)){
 					max_day = s;
 					max_color = colors.get(i);
 				}
 			}
 		}
 	}
 	private void calculateValues(Date date){
 		maxBac.clear();
 		bacColors.clear();
 		drinkingDays.clear();
 		ArrayList<DatabaseStore> bac_colors = (ArrayList<DatabaseStore>) db
 				.getVarValuesForMonth("bac_color", date);
 		ArrayList<DatabaseStore> bac_vals = (ArrayList<DatabaseStore>)db.getVarValuesForMonth("bac", date);
 		if(bac_colors != null && bac_vals != null){
 			bac_colors = DatabaseStore.sortByTime(bac_colors);
 			bac_vals = DatabaseStore.sortByTime(bac_vals);
 			getMaxForDays(bac_colors, bac_vals);
 			convertToLists(bac_colors, bac_vals);
 		}
 	}
 	
 	@Override
 	public void onClick(View v) {
 		// TODO Auto-generated method stub
 
 		/*
 		// finds the data in the database
 		ArrayList<Integer> drinkingDays = new ArrayList<Integer>();
 		drinkingDays.add(5);
 		drinkingDays.add(14);
 		drinkingDays.add(16);
 
 		ArrayList<Double> maxBac = new ArrayList<Double>();
 		maxBac.add(0.25);
 		maxBac.add(0.1);
 		maxBac.add(0.7);
 		*/
 		GregorianCalendar gc = new GregorianCalendar(selectedYear, selectedMonth, 1);
 		Date date = new Date();
 		switch (v.getId()) {
 		case R.id.bNextMonth:		
 			
 			gc.add(Calendar.MONTH, 1);
 			date = gc.getTime();
 			
 			if (selectedMonth + 1 > 11) {
 				selectedMonth = 0;
 				selectedYear++;
 				yearDisplay.setText(Integer.toString(selectedYear));
 			} else
 				selectedMonth++;
 			setMonthFromInt(selectedMonth);
 			break;
 		case R.id.bPreviousMonth:
 			
 			gc.add(Calendar.MONTH, -1);
 			date = gc.getTime();
 			
 			if (selectedMonth - 1 < 0) {
 				selectedMonth = 11;
 				selectedYear--;
 				yearDisplay.setText(Integer.toString(selectedYear));
 			} else
 				selectedMonth--;
 			setMonthFromInt(selectedMonth);
 			break;
 		}
 		calculateValues(date);
 		ColorAdapter adapter = new ColorAdapter(this, selectedMonth,
 				selectedYear, drinkingDays, maxBac, bacColors);
 		drinkCalendar.setAdapter(adapter);
 		drinkBacButtons = adapter.getButtonView();
 	}
 
 	// inputs the int value of month and outputs its corresponding name
 	private void setMonthFromInt(int num) {
 		String month = "wrong";
 		DateFormatSymbols dfs = new DateFormatSymbols();
 		String[] months = dfs.getMonths();
 		month = months[num];
 		monthDisplay.setText(month);
 	}
 
 	public void changeBottomDisplay(String entry) {
 		bottomDisplay.setText(entry);
 	}
 
 }
