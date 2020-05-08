 package com.example.tomatroid;
 
 import java.util.ArrayList;
 
 import org.joda.time.DateTime;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 
 import com.example.tomatroid.digram.BarChart;
 import com.example.tomatroid.digram.LineChart;
 import com.example.tomatroid.digram.PieChart;
 import com.example.tomatroid.sql.SQHelper;
 import com.example.tomatroid.util.NavigationBarManager;
 import com.example.tomatroid.util.StoredAnimation;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Context;
 import android.database.Cursor;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.ArrayAdapter;
 import android.widget.CursorAdapter;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TableLayout;
 import android.widget.TextView;
 import android.widget.ViewFlipper;
 
 public class StatisicActivity extends Activity {
 
 	final int ACTIVITYNUMBER = 1; 
 	
 	LinearLayout overview;
 	ViewFlipper statistic_flipper;
 	ImageButton prev;
 	LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,
 			LayoutParams.WRAP_CONTENT);
 	LayoutParams barParams = new TableLayout.LayoutParams(
 			LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f);
 	SQHelper sqHelper = new SQHelper(this);
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_statictic);
 
 		NavigationBarManager navi = new NavigationBarManager(this, ACTIVITYNUMBER);
 		
 		overview = (LinearLayout) findViewById(R.id.statistic_overview);
 		statistic_flipper = (ViewFlipper) findViewById(R.id.statistic_flipper);
 		statistic_flipper.setAnimateFirstView(false);
 
 		prev = (ImageButton) findViewById(R.id.statistic_prev);
 		prev.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				statistic_flipper.setAnimation(StoredAnimation
 						.inFromRightAnimation(1));
 				statistic_flipper.showPrevious();
 				Log.e("Image Button", "clicked");
 			}
 		});
 
 		// Pomodoro Overview
 		int[] pomodoroInfoArray = sqHelper.getTotalCountTimeDays(
 				new int[] { SQHelper.TYPE_POMODORO }, new int[] {});
 		TextView pomodoroCount = (TextView) findViewById(R.id.statistic_pomodoroCount);
 		pomodoroCount.setText("" + pomodoroInfoArray[0]);
 		TextView pomodoroInfo = (TextView) findViewById(R.id.statistic_pomodoroInfo);
 		pomodoroInfo.setText(prepareInfoText(pomodoroInfoArray));
 
 		// Long Break Overview
 		int[] breakInfoArray = sqHelper.getTotalCountTimeDays(new int[] {
 				SQHelper.TYPE_LONGBREAK, SQHelper.TYPE_SHORTBREAK },
 				new int[] {});
 		TextView breakCount = (TextView) findViewById(R.id.statistic_breakCount);
 		breakCount.setText("" + breakInfoArray[0]);
 		TextView breakInfo = (TextView) findViewById(R.id.statistic_breakInfo);
 		breakInfo.setText(prepareInfoText(breakInfoArray));
 
 		// Sleep Overview
 		int[] sleepInfoArray = sqHelper.getTotalCountTimeDays(
 				new int[] { SQHelper.TYPE_SLEEPING }, new int[] {});
 		TextView sleepCount = (TextView) findViewById(R.id.statistic_sleepCount);
 		sleepCount.setText("" + sleepInfoArray[0]);
 		TextView sleepInfo = (TextView) findViewById(R.id.statistic_sleepInfo);
 		sleepInfo.setText(prepareInfoText(sleepInfoArray));
 
 		// Theme List Overview
 		ArrayList<String> valueList = new ArrayList<String>();
 		Cursor c = sqHelper.getThemeCursor();
 		if (c.moveToFirst()) {
 			do {
 				valueList.add(c.getString(c.getColumnIndex(SQHelper.KEY_NAME)));
 			} while (c.moveToNext());
 		}
 		c.close();
 		ListView themelist = (ListView) findViewById(R.id.statistic_themelist);
 		themelist.setAdapter(new ThemeListAdapter(getApplicationContext(),
 				R.layout.theme_statistic_list_row, R.id.themeText, valueList));
 
 		int[] barValues = new int[] { 3, 10, 4, 5, 6, 7, 8 };
 		
 		BarChart multiBar = new BarChart(this, barValues);
 		statistic_flipper.addView(multiBar);
 		
 		
 		int[][] barValues1 = new int[][] { new int[] { 3, 10, 4, 5, 6, 7, 8 },
 				new int[] { 3, 3, 3, 3, 3, 3, 3 },
 				new int[] { 1, 1, 1, 1, 1, 1, 1 }, };
 		String[] axisLables = new String[]{ "Mo", "Di", "Mi", "Do", "Fr", "Sa", "So"};
 		String[] lineNames = new String[]{ "Pomodoro", "Breaks", "Sleep"};
 		LineChart multiline = new LineChart(this, barValues1, axisLables, lineNames);
 		statistic_flipper.addView(multiline);
 		
 		// float values[] = { 3456, 5734, 5735, 5477, 9345, 3477 };
 		// PieChart pie = new PieChart(this, values);
 		// overview.addView(pie, 0, barParams);
 
 		// themelist.addView(pie);
 		//
 
 		// TextView t1 = new TextView(this);
 		// t1.setText("Total Pomodoro");
 		// overview.addView(t1, params);
 
 	}
 	
 	@Override
 	protected void onResume() {
 		super.onResume();
 		getActionBar().setSelectedNavigationItem(ACTIVITYNUMBER);
 	}
 
 	public String prepareInfoText(int[] array) {
 		if (array[2] != 0) {
 			return array[1] + " mins\n" + array[0] / array[2]
 					+ " per Day\n(over " + array[2] + " days)";
 		} else {
 			return "Keine Informationen";
 		}
 	}
 
 	class ThemeListAdapter extends ArrayAdapter<String> {
 
 		ArrayList<String> values;
 
 		public ThemeListAdapter(Context context, int layoutViewResourceId,
 				int textViewResourceId, ArrayList<String> values) {
 			super(context, layoutViewResourceId, textViewResourceId, values);
 			this.values = values;
 		}
 
 		int rank = 1;
 
 		@Override
 		public View getView(int position, View v, ViewGroup parent) {
 			LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			v = vi.inflate(R.layout.theme_statistic_list_row, null);
 			
 //			Log.e("Statistic Theme Adapter", "getCalled");
 //			Log.e("Statistic Theme Adapter", "position " +position);
 //			Log.e("Statistic Theme Adapter", "value " +values.get(position));
 			
 			int themeId = sqHelper.getTheme(values.get(position));
 
 			TextView rankText = (TextView) v.findViewById(R.id.rankText);
 			rankText.setText("" + rank);
 			rankText.append(". ");
 
 			int[] themeInfo = sqHelper.getTotalCountTimeDays(new int[] {},
 					new int[] { themeId });
 			TextView infoText = (TextView) v.findViewById(R.id.infoText);
 			infoText.setText(prepareInfoText(themeInfo));
 
 //			getTotalCountTimeDays(int[] types, int[] themes, int[] date)
			
			
 //			int[] barValues = new int[] { 3, 10, 4, 5, 6, 7, 8 };
			DateTime dt = new DateTime();
			dt = dt.minusDays(6);
 			int[] barValues = new int[7]; 
 			for(int i=0; i<7;i++){
				dt = dt.plusDays(i);
 				int[] date = new int[3];
				date[0] = dt.getDayOfMonth();
				date[1] = dt.getMonthOfYear();
				date[2] = dt.getYear();
 				int[] answer = sqHelper.getTotalCountTimeDays(new int[]{}, new int[]{themeId}, date);
 				barValues[i] = answer[1];
 //				Log.e("Statistic Theme Adapter", "theme " +themeId+ " barvalue " +answer[1]);
 			}
 			BarChart bars = new BarChart(getContext(), barValues);

 			// LinearLayout ll = (LinearLayout) v.findViewById(R.id.infoLayout);
 			// ll.addView(bars, 100, 100);
 
 			BarChart bars2 = (BarChart) v.findViewById(R.id.barChart);
 			bars2.setValues(barValues);
 
 			rank++;
 			return super.getView(position, v, parent);
 		}
 	}
 }
