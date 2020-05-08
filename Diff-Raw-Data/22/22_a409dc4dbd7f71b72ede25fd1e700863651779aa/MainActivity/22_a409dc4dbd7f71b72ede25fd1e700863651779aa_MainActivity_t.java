 package nl.rug.snoozzz;
 
 import android.app.Activity;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
import android.widget.EditText;
 import android.widget.ProgressBar;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
 
 	private static final String TAG = "MainActivity";
 	
 	private Intent measurementIntent;
 	
 	private static final int SNOOZZZ_GUID = 1;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.main);
 
 		createTaskBarIcon();
 		
 		measurementIntent = new Intent(this, MeasurementService.class);
 
 	}
 
 	public void onClickDisconnectButton(View view) {
 		
 		stopService(measurementIntent);
 
 		((ProgressBar) findViewById(R.id.progressBar))
 				.setVisibility(View.INVISIBLE);
 
 	}
 	
 	public void onClickConnectButton(View view) {
		//Wowowow
		String ip = ((EditText) findViewById(R.id.ip)).getText().toString();
 		
 		measurementIntent.putExtra("ip",ip);
 
 		startService(measurementIntent);
 		
 		((ProgressBar) findViewById(R.id.progressBar))
 				.setVisibility(View.VISIBLE);
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 
 		MenuInflater inflater = getMenuInflater();
 
 		inflater.inflate(R.menu.main_menu, menu);
 
 		return true;
 
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 
 		switch (item.getItemId()) {
 
 		case R.id.about:
 
 			Toast.makeText(this, R.string.copyright, 2000).show();
 
 			return true;
 
 		default:
 
 			return super.onContextItemSelected(item);
 
 		}
 	}
 
 	public void createTaskBarIcon() {
 
 		String notificationService = Context.NOTIFICATION_SERVICE;
 
 		NotificationManager mNotificationManager = (NotificationManager) getSystemService(notificationService);
 
 		int icon = R.drawable.ic_taskbar;
 
 		String tickerText = "SnoozZz Started..";
 
 		long when = System.currentTimeMillis();
 
 		Notification notification = new Notification(icon, tickerText, when);
 
 		Context context = getApplicationContext();
 
 		String contentTitle = "SnoozZz";
 
 		String contentText = "Sleep monitor running..";
 
 		Intent notificationIntent = new Intent(this, MainActivity.class);
 
 		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
 				notificationIntent, 0);
 
 		notification.setLatestEventInfo(context, contentTitle, contentText,
 				contentIntent);
 
 		notification.flags |= Notification.FLAG_ONGOING_EVENT;
 
 		mNotificationManager.notify(SNOOZZZ_GUID, notification);
 
 	}
 
 }
