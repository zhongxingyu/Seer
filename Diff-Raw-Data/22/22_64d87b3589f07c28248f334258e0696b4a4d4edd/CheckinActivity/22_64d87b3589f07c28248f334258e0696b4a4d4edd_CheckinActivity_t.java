 package ch.almana.android.stechkarte;
 
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
 import ch.almana.android.stechkarte.model.Day;
 import ch.almana.android.stechkarte.model.DayAccess;
import ch.almana.android.stechkarte.model.Formater;
 import ch.almana.android.stechkarte.model.Timestamp;
 import ch.almana.android.stechkarte.model.TimestampAccess;
 import ch.almana.android.stechkarte.model.io.TimestampsCsvIO;
 import ch.almana.android.stechkarte.view.ListDays;
 import ch.almana.android.stechkarte.view.ListTimeStamps;
 
 public class CheckinActivity extends Activity {
 
 	private static final int MENU_ITEM_DAY_LIST = Menu.FIRST;
 	private static final int MENU_ITEM_TIMESTAMP_LIST = Menu.FIRST + 1;
 	private static final int MENU_ITEM_READ_IN_TIMESTAMPS = Menu.FIRST + 2;
 	private TextView status;
 	private TextView overtime;
 	private TextView hoursWorked;
 
 	private TextView holidaysLeft;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		writeTimestampsToCsv();
 
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
 				TimestampAccess.getInstance(v.getContext()).addInNow();
 				updateFields();
 			}
 		});
 
 		buttonOut.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				TimestampAccess.getInstance(v.getContext()).addOutNow();
 				updateFields();
 			}
 		});
 
 		status = (TextView) findViewById(R.id.TextViewStatus);
 		overtime = (TextView) findViewById(R.id.TextViewOvertime);
 		hoursWorked = (TextView) findViewById(R.id.TextViewHoursWorked);
 		holidaysLeft = (TextView) findViewById(R.id.TextViewHolidaysLeft);
 
 	}
 
 	@Override
 	protected void onResume() {
 		updateFields();
 		super.onResume();
 	}
 
 	private void updateFields() {
 		Cursor c = DayAccess.getInstance(this).query(null);
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
 
 		status.setText("You are " + inOut);
 		holidaysLeft.setText("" + day.getHolydayLeft());
 
		overtime.setText(Formater.formatHourMinFromHours(day.getOvertime() + delta));
		hoursWorked.setText(Formater.formatHourMinFromHours((day.getHoursWorked() + delta)));
 
 	}
 
 	private void writeTimestampsToCsv() {
 		TimestampsCsvIO csv = new TimestampsCsvIO();
 		Cursor c = TimestampAccess.getInstance(getApplicationContext()).query(null, null);
 		csv.writeTimestamps(c);
 		c.close();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		menu.add(0, MENU_ITEM_DAY_LIST, 0, R.string.ButtonDayList).setShortcut('3', 'a');
 		menu.add(0, MENU_ITEM_TIMESTAMP_LIST, 1, R.string.ButtonTSList).setShortcut('3', 'a');
 		menu.add(0, MENU_ITEM_READ_IN_TIMESTAMPS, 2, R.string.ButtonReadTimestamps).setShortcut('3', 'a');
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		Intent i;
 		switch (item.getItemId()) {
 		case MENU_ITEM_DAY_LIST:
 			i = new Intent(this, ListDays.class);
 			startActivity(i);
 			break;
 		case MENU_ITEM_TIMESTAMP_LIST:
 			i = new Intent(this, ListTimeStamps.class);
 			startActivity(i);
 			break;
 
 		case MENU_ITEM_READ_IN_TIMESTAMPS:
 			TimestampsCsvIO timestampsCsvIO = new TimestampsCsvIO();
 			timestampsCsvIO.readTimestamps(TimestampsCsvIO.PATH + "timestamps.csv", TimestampAccess
 					.getInstance(getApplicationContext()));
 			break;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 }
