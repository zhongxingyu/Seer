 package lu.mir.android.pomodorobox;
 
 import java.io.IOException;
 import java.util.Locale;
 
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.CountDownTimer;
 import android.speech.tts.TextToSpeech;
 import android.speech.tts.TextToSpeech.OnInitListener;
 import android.support.v4.app.NavUtils;
 import android.view.MenuItem;
 import android.widget.TextView;
 
 import com.dropbox.sync.android.DbxPath.InvalidPathException;
 
 /**
  * The TextToSpeech code is from:
  * http://android-developers.blogspot.ch/2009/09/introduction
  * -to-text-to-speech-in.html
  * 
  * @author mircea
  * 
  */
 
 public class TimerActivity extends Activity implements OnInitListener {
 	// Used to save the state between Activity restarts (which can happen even when orientation is changed)
 	static final String STATE_MILLIS = "millisRemaining";
 	
 	private TextToSpeech tts;
 	private String message;
 	private static long SECOND = 1000;
 	private long initial_count;
 	private long current_count;
 
 	private CountDownTimer timer;
 	
 
 	protected void updateTimer(long millisUntilFinished) {
 		
 		long minsToFinish = millisUntilFinished / 1000 / 60;
 		long secs = millisUntilFinished / 1000 % 60;
 		
 		String minuteString = ((minsToFinish < 10)?"0":"") + minsToFinish;
 		String secondsString = (secs<10?"0":"") + secs;
 
 		TextView counterView = (TextView) findViewById(R.id.counter);
 		counterView.setText( minuteString + ":" + secondsString);
 	}
 
 	protected void speak(String text) {
 		tts.setLanguage(Locale.US);
 		tts.speak(text, TextToSpeech.QUEUE_ADD, null);
 	}
 
 	protected void logPomodoroToDropbox() throws InvalidPathException, IOException {
 		DropBoxConnection.logPomodoroToDropbox(message);
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		// Set the text view as the activity layout
 		setContentView(R.layout.activity_countdown);
 
 		Intent intent = getIntent();
 		message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
 		
 		 if (savedInstanceState != null) {
 			 initial_count = savedInstanceState.getLong(STATE_MILLIS);
 		 } else {
 			 initial_count = SECOND * intent.getLongExtra(MainActivity.EXTRA_TIME_IN_SECONDS, 7);
 		 }
 
 		// Create the text view
 		TextView activityView = (TextView) findViewById(R.id.activity);
 		activityView.setText(message);
 
 		tts = new TextToSpeech(this, this);
 
 		timer = new CountDownTimer(initial_count, SECOND) {
 			public void onTick(long millisUntilFinished) {
 				current_count = millisUntilFinished;
 				updateTimer(millisUntilFinished);
 			}
 
 			public void onFinish() {
 				updateTimer(1);
 				speak("Well done!");
 				finish();
 				try {
 					logPomodoroToDropbox();
 				} catch (InvalidPathException e) {
 					e.printStackTrace();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}.start();
 	}
 	
 	
 
 	/**
 	 * Set up the {@link android.app.ActionBar}, if the API is available.
 	 */
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	private void setupActionBar() {
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 			getActionBar().setDisplayHomeAsUpEnabled(true);
 		}
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			// This ID represents the Home or Up button. In the case of this
 			// activity, the Up button is shown. Use NavUtils to allow users
 			// to navigate up one level in the application structure. For
 			// more details, see the Navigation pattern on Android Design:
 			//
 			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
 			//
 			NavUtils.navigateUpFromSameTask(this);
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	@Override
 	public void onInit(int arg0) {
 		// TODO Auto-generated method stub
 
 	}
 	
 	@Override
 	public void onSaveInstanceState(Bundle savedInstanceState) {
 	    // Save the user's current game state
 	    savedInstanceState.putLong(STATE_MILLIS, current_count);
	    // If not canceled, the timer which is a separate thread will keep running, and we'll end up with multiple 
	    // timers finishing and thus multiple actions logged. Not cool.
	    timer.cancel();
 	    
 	    // Always call the superclass so it can save the view hierarchy state
 	    super.onSaveInstanceState(savedInstanceState);
 
 	}
 	
 	public void onRestoreInstanceState(Bundle savedInstanceState) {
 	    // Always call the superclass so it can restore the view hierarchy
 	    super.onRestoreInstanceState(savedInstanceState);
 	   
 	    // Restore state members from saved instance
 	    initial_count = savedInstanceState.getLong(STATE_MILLIS);
 	    
 	}
 	
 	@Override
 	public void onBackPressed() {
 	    new AlertDialog.Builder(this)
 	        .setIcon(android.R.drawable.ic_dialog_alert)
 	        .setTitle("Stopping the counter")
 	        .setMessage("This will reset the pomodoro. Sure you want to do this?")
 	        .setPositiveButton("Yes", new DialogInterface.OnClickListener()
 	    {
 	        @Override
 	        public void onClick(DialogInterface dialog, int which) {
 	            timer.cancel();
 	            finish();
 	        }
 
 	    })
 	    .setNegativeButton("No", null)
 	    .show();
 	}
 }
