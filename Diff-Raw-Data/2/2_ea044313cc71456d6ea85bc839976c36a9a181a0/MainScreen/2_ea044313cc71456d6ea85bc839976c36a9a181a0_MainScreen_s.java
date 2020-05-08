 package no.ntnu.stud.fallprevention.activity;
 
 import java.sql.Timestamp;
 import java.util.Calendar;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import no.ntnu.stud.fallprevention.NotificationBroadcastReciever;
 import no.ntnu.stud.fallprevention.R;
 import no.ntnu.stud.fallprevention.R.drawable;
 import no.ntnu.stud.fallprevention.R.id;
 import no.ntnu.stud.fallprevention.R.layout;
 import no.ntnu.stud.fallprevention.R.menu;
 import no.ntnu.stud.fallprevention.R.string;
 import no.ntnu.stud.fallprevention.connectivity.ContentProviderHelper;
 import no.ntnu.stud.fallprevention.connectivity.DatabaseHelper;
 import no.ntnu.stud.fallprevention.datastructures.RiskStatus;
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Bitmap;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.os.Build;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.text.format.DateUtils;
 import android.text.format.Time;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ImageButton;
 import android.widget.TextView;
 
 /**
  * Creates activity: mainscreen, creates option menu, makes updates visible,
  * fires event and shows the item menu bar.
  * 
  * @author Tayfun
  * 
  */
 public class MainScreen extends Activity {
 	RiskStatus status;
 	Thread notificationThread;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_mainscreen);
 
 		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
 		}
 
 		// Intent alarmIntent = new Intent(getBaseContext(),
 		// NotificationBroadcastReciever.class);
 		// PendingIntent pendingIntent = PendingIntent.getBroadcast(
 		// getBaseContext(), 0, alarmIntent,
 		// PendingIntent.FLAG_UPDATE_CURRENT);
 		// AlarmManager alarmManager = (AlarmManager) getBaseContext()
 		// .getSystemService(getBaseContext().ALARM_SERVICE);
 		// alarmManager.set(AlarmManager.RTC, Calendar.getInstance()
 		// .getTimeInMillis(), pendingIntent);
 
 		updateVisible();
 
 		// Change image depending on information from the database
 		Drawable drawable;
 		if (status == null) {
 			status = RiskStatus.OK_JOB;
 		}
 
 		status = new ContentProviderHelper(getApplicationContext())
 				.cpGetStatus(status);
 		if (status == RiskStatus.BAD_JOB) {
 			drawable = getResources().getDrawable(R.drawable.bad_job);
 		} else if (status == RiskStatus.NOT_SO_OK_JOB) {
 			drawable = getResources().getDrawable(R.drawable.not_so_ok_job);
 		} else if (status == RiskStatus.OK_JOB) {
 			drawable = getResources().getDrawable(R.drawable.ok_job);
 		} else if (status == RiskStatus.GOOD_JOB) {
 			drawable = getResources().getDrawable(R.drawable.good_job);
 		} else if (status == RiskStatus.VERY_GOOD_JOB) {
 			drawable = getResources().getDrawable(R.drawable.very_good_job);
 		} else {
 			// Problem
 			drawable = null;
 		}
 		shouldPush();
 		// ContentProviderHelper cph= new
 		// ContentProviderHelper(getApplicationContext());
 		// cph.refreshTimestamp();
 
 		Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
 		Drawable d = new BitmapDrawable(getResources(),
 				Bitmap.createScaledBitmap(bitmap, 200, 200, true));
 		ImageButton imageButton = (ImageButton) findViewById(R.id.mainScreenSmileyImage);
 		imageButton.setBackgroundDrawable(d);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_mainscreen, menu);
 		return true;
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		shouldPush();
 		updateVisible();
 	}
 
 	private void updateVisible() {
 		// Find name from shared prefences file
 		SharedPreferences sp = PreferenceManager
 				.getDefaultSharedPreferences(this);
 		String name = sp.getString("name", "");
 		String displayString = getString(R.string.greeting) + ", " + name + "!";
 		TextView txtGreetingName = (TextView) findViewById(R.id.textView1);
 		txtGreetingName.setText(displayString);
 
 		// Display a message if there are new messages
 		TextView txtSubGreeting = (TextView) findViewById(R.id.mainScreenSubText);
 		DatabaseHelper dbHelper = new DatabaseHelper(this);
 		if (dbHelper.dbHaveEvents()) {
 			String message = getString(R.string.main_got_new_events);
 			txtSubGreeting.setText(message);
 		} else {
 			txtSubGreeting.setVisibility(View.GONE);
 		}
 	}
 
 	public boolean onOptionsItemSelected(MenuItem item) {
 		Intent intent;
 		switch (item.getItemId()) {
 		case R.id.menu_statistics:
 			intent = new Intent(this, Statistics.class);
 			startActivity(intent);
 			break;
 		case R.id.menu_settings:
 			intent = new Intent(this, Settings.class);
 			startActivity(intent);
 			break;
 		case R.id.menu_related:
 			intent = new Intent(this, Related.class);
 			startActivity(intent);
 			break;
 		}
 		return false;
 	}
 
 	@SuppressLint("NewApi")
 	private void shouldPush() {
 		long current = System.currentTimeMillis();
 		Timestamp now = new Timestamp(current);
 		Log.v("Main Screen", "Checking for pushing");
 		Timestamp last;
 		SharedPreferences sp = PreferenceManager
 				.getDefaultSharedPreferences(this);
 		last = new Timestamp(sp.getLong("lastPushed", 0l));
 		Log.v("Main Screen", String.valueOf(last.getTime()));
 		if (DateUtils.HOUR_IN_MILLIS < (now.getTime() - last.getTime())) {
 			Log.v("Main Screen", "Minute passed, notification pushed: "
 					+ (status == null));
 			new ContentProviderHelper(this).pushNotification(status.getCode());
 			SharedPreferences.Editor editor = sp.edit();
 			// Displays the edited name
 			editor.putLong("lastPushed", current);
 			editor.commit();
		} else {
 			Log.v("Main Screen", "Time not smaller");
 			SharedPreferences.Editor editor = sp.edit();
 			// Displays the edited name
 			editor.putLong("lastPushed", current);
 			editor.commit();
 		}
 
 	}
 
 	public void fireEvent(View view) {
 		Intent intent = new Intent(this, EventList.class);
 		startActivity(intent);
 	}
 
 }
