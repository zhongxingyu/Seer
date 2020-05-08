 package ch.almana.android.stechkarte.view;
 
 import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 import ch.almana.android.stechkarte.R;
 import ch.almana.android.stechkarte.model.Day;
 import ch.almana.android.stechkarte.model.DayAccess;
 import ch.almana.android.stechkarte.model.Timestamp;
 import ch.almana.android.stechkarte.model.TimestampAccess;
 import ch.almana.android.stechkarte.model.io.TimestampsCsvIO;
 import ch.almana.android.stechkarte.utils.Formater;
 import ch.almana.android.stechkarte.utils.Settings;
 
 public class CheckinActivity extends Activity {
 	
 	// private static final int MENU_ITEM_DAY_LIST = Menu.FIRST;
 	// private static final int MENU_ITEM_TIMESTAMP_LIST = Menu.FIRST + 1;
 	// private static final int MENU_ITEM_READ_IN_TIMESTAMPS = Menu.FIRST + 2;
	private static final SimpleDateFormat hhmmSimpleDateFormat = new SimpleDateFormat("hh:mm");
 	private TextView status;
 	private TextView overtime;
 	private TextView hoursWorked;
 	private TextView leaveAt;
 	
 	private TextView holidaysLeft;
 	
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		
 		if (!Settings.getInstance().isFreeVersion()) {
 			writeTimestampsToCsv();
 		}
 		
 		Button buttonIn = (Button) findViewById(R.id.ButtonIn);
 		Button buttonOut = (Button) findViewById(R.id.ButtonOut);
 		int width = getWindowManager().getDefaultDisplay().getWidth();
 		width = Math.round(width / 2);
 		int size = Math.round(width / 5);
 		buttonIn.setWidth(width);
 		buttonIn.setHeight(width);
 		buttonIn.setTextSize(size);
 		buttonOut.setWidth(width);
 		buttonOut.setHeight(width);
 		buttonOut.setTextSize(size);
 		
 		buttonIn.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				TimestampAccess.getInstance().addInNow(CheckinActivity.this);
 				updateFields();
 			}
 		});
 		
 		buttonOut.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				TimestampAccess.getInstance().addOutNow(CheckinActivity.this);
 				updateFields();
 			}
 		});
 		
 		status = (TextView) findViewById(R.id.TextViewStatus);
 		overtime = (TextView) findViewById(R.id.TextViewOvertime);
 		hoursWorked = (TextView) findViewById(R.id.TextViewHoursWorked);
 		holidaysLeft = (TextView) findViewById(R.id.TextViewHolidaysLeft);
 		leaveAt = (TextView) findViewById(R.id.TextViewLeave);
 	}
 	
 	@Override
 	protected void onResume() {
 		updateFields();
 		super.onResume();
 	}
 	
 	private void updateFields() {
 		Cursor c = DayAccess.getInstance().query(null);
 		Day day;
 		if (c.moveToFirst()) {
 			day = new Day(c);
 		} else {
 			day = new Day(0);
 		}
 		c.close();
 		
 		c = day.getTimestamps(this);
 		Timestamp ts = null;
 		
 		float delta = 0;
 		String inOut = Timestamp.getTimestampTypeAsString(getApplicationContext(), Timestamp.TYPE_OUT);
 		if (c.moveToLast()) {
 			ts = new Timestamp(c);
 			inOut = ts.getTimestampTypeAsString(this);
 			if (ts.getTimestampType() == Timestamp.TYPE_IN) {
 				delta = (System.currentTimeMillis() - ts.getTimestamp()) / DayAccess.HOURS_IN_MILLIES;
 			}
 		}
 		
		Calendar cal = Calendar.getInstance();
		float leave = day.getHoursTarget() - day.getHoursWorked();
 		if (leave > 0f) {
			cal.add(Calendar.HOUR, Math.round(leave));
			leaveAt.setText(hhmmSimpleDateFormat.format(cal.getTime()) + " "
					+ cal.getTimeZone().getDisplayName(true, TimeZone.SHORT));
 		} else {
 			leaveAt.setText("now");
 		}
 		
 		status.setText("You are " + inOut);
 		holidaysLeft.setText(day.getHolydayLeft() + "");
 		
 		overtime.setText(Formater.formatHourMinFromHours(day.getOvertime() + delta));
		hoursWorked.setText(Formater.formatHourMinFromHours((day.getHoursWorked() + delta)));
 		
 	}
 	
 	private void writeTimestampsToCsv() {
 		TimestampsCsvIO csv = new TimestampsCsvIO();
 		Cursor c = TimestampAccess.getInstance().query(null, null);
 		csv.writeTimestamps(c);
 		c.close();
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 		getMenuInflater().inflate(R.menu.chekin_option, menu);
 		menu.getItem(2).setVisible(!Settings.getInstance().isFreeVersion());
 		return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		Intent i;
 		switch (item.getItemId()) {
 		case R.id.itemDaysList:
 			i = new Intent(this, ListDays.class);
 			startActivity(i);
 			break;
 		case R.id.itemExportTimestamps:
 			if (Settings.getInstance().isFreeVersion()) {
 				showFreeVersionDialog();
 			} else {
 				i = new Intent(this, ExportTimestamps.class);
 				startActivity(i);
 			}
 			break;
 		
 		case R.id.itemTimestampList:
 			i = new Intent(this, ListTimeStamps.class);
 			startActivity(i);
 			break;
 		
 		case R.id.itemReadInTimestmaps:
 			if (Settings.getInstance().isFreeVersion()) {
 				showFreeVersionDialog();
 			} else {
 				TimestampsCsvIO timestampsCsvIO = new TimestampsCsvIO();
 				timestampsCsvIO.readTimestamps(TimestampsCsvIO.getPath() + "timestamps.csv", TimestampAccess
 						.getInstance());
 			}
 			break;
 		
 		case R.id.itemPreferences:
 			i = new Intent(getApplicationContext(), StechkartePreferenceActivity.class);
 			startActivity(i);
 			break;
 		
 		}
 		return super.onOptionsItemSelected(item);
 	}
 	
 	private void showFreeVersionDialog() {
 		Intent i = new Intent(this, BuyFullVersion.class);
 		startActivity(i);
 	}
 	
 }
