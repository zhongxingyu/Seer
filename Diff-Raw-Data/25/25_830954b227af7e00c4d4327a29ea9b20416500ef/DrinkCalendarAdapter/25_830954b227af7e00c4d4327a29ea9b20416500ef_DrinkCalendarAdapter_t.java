 package cornell.drinkingapp;
 
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 
 import android.content.Context;
 import android.graphics.Color;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class DrinkCalendarAdapter extends BaseAdapter implements OnClickListener {
 	private Context mContext;
 	int monthSelected, yearSelected;
 	ArrayList<Integer> drinkingDays;
 	private ArrayList<Integer> bacColors;
 	ArrayList<Double> maxbac;
 	int daysSetBack = 0;
 	int daysInMonth = 0;
 	static ArrayList<Button> buttonStore = new ArrayList<Button>();
 	static int focused = 0;
 	TextView test;
 
 	public DrinkCalendarAdapter(Context c, int m, int y, ArrayList<Integer> d,
 			ArrayList<Double> mb, ArrayList<Integer> colors) {
 		mContext = c;
 		monthSelected = m;
 		yearSelected = y;
 		drinkingDays = d;
 		maxbac = mb;
 		bacColors = colors;
 
 	}
 
 	// given month and year, it detects the 1st day of the month and see what
 	// day it is, days in that month
 	// and shift the calender accordingly.
 	public int getCount() {
 		Calendar selectedCalendar = Calendar.getInstance();
 		selectedCalendar.set(yearSelected, monthSelected, 1);
 		int daysInSelectedMonth = selectedCalendar
 				.getActualMaximum(Calendar.DAY_OF_MONTH);
 		int dayOfTheWeekSelected = selectedCalendar.get(Calendar.DAY_OF_WEEK);
 		daysSetBack = dayOfTheWeekSelected - 1;
 		return daysInSelectedMonth + daysSetBack + 7;
 	}
 
 	@Override
 	public Object getItem(int position) {
 
 		return null;
 	}
 
 	@Override
 	public long getItemId(int position) {
 
 		return 0;
 	}
 
 	@Override
 	public View getView(final int position, View convertView,
 			final ViewGroup parent) {
 		
 		final Button view = new Button(mContext);
		view.setBackgroundResource(android.R.drawable.btn_default);
 		double bacLevel;
 		if (!drinkingDays.contains(position)){
 			view.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					((DrinkCalendar) mContext)
 							.changeBottomDisplay("", 0);
 					if (focused > 0) {
 						View child = parent.getChildAt(focused);
 						if(drinkingDays.contains((Integer)focused)){
 							child.setBackgroundColor(bacColors.get(focused));
 						}else{
 							child.setBackgroundResource(android.R.drawable.btn_default);
 						}
 					}
 					focused = position;
 					v.setSelected(true);
 					view.setBackgroundResource(R.drawable.border);
 				}
 
 			});
 		}
 		for (int n = 0; n < drinkingDays.size(); n++) {
 			if (drinkingDays.get(n) + daysSetBack + 7 == position + 1) {
 				view.setBackgroundColor(bacColors.get(n));
 				bacLevel = maxbac.get(n);
 				final double bac_lev = bacLevel;
 				view.setOnClickListener(new OnClickListener() {
 
 					@Override
 					public void onClick(View v) {
 						DecimalFormat formatter = new DecimalFormat("#.###");
 						((DrinkCalendar) mContext)
 								.changeBottomDisplay(formatter.format(bac_lev), bac_lev);
 						if (focused > 0) {
 							View child = parent.getChildAt(focused);
 							if(drinkingDays.contains((Integer)focused)){
 								child.setBackgroundColor(bacColors.get(focused));
 							}else{
 								child.setBackgroundResource(android.R.drawable.btn_default);
 							}
 						}
 						focused = position;
 						v.setSelected(true);
 						view.setBackgroundResource(R.drawable.border);
 					}
 
 				});
 			}
 		}
 		if (position > 6 && position < 7 + daysSetBack) {
 			view.setVisibility(8);
 		} else {
 
 			view.setText("" + (position + 1 - daysSetBack - 7));
 			view.setTextSize(10f);
 			// sets up the Days of the week display
 		}
 		switch (position) {
 		case 0:
 			view.setBackgroundColor(Color.WHITE);
 			view.setText("Su");
 			break;
 		case 1:
 			view.setBackgroundColor(Color.WHITE);
 			view.setText("M");
 			break;
 		case 2:
 			view.setBackgroundColor(Color.WHITE);
 			view.setText("T");
 			break;
 		case 3:
 			view.setBackgroundColor(Color.WHITE);
 			view.setText("W");
 			break;
 		case 4:
 			view.setBackgroundColor(Color.WHITE);
 			view.setText("Th");
 			break;
 		case 5:
 			view.setBackgroundColor(Color.WHITE);
 			view.setText("F");
 			break;
 		case 6:
 			view.setBackgroundColor(Color.WHITE);
 			view.setText("Sa");
 			break;
 		}
 
 		return view;
 	}
 
 	public ArrayList<Button> getButtonView() {
 		return buttonStore;
 
 	}
 
 	@Override
 	public void onClick(View v) {
 	}
 }
