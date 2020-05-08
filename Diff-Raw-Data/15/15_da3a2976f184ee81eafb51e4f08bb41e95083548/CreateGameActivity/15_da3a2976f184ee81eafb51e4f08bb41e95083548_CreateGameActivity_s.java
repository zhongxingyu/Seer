 package eltoraz.pug.android;
 
 import eltoraz.pug.*;
 
 import java.io.IOException;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.List;
 import java.util.Locale;
 
 import android.app.Activity;
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.app.TimePickerDialog;
 import android.content.Context;
 import android.location.Address;
 import android.location.Geocoder;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.View;
 import android.widget.*;
import android.widget.AdapterView.OnItemSelectedListener;
 
 // TODO: Javadoc comments
 
 /**
  * An <code>Activity</code> for game creation based on user specifications
  * @author Bill Jameson
  * @version 0.1
  */
 public class CreateGameActivity extends Activity {
 	private Person user;
 	private Game game;
 	
 	// UI ELEMENTS
 	private Button createGameButton;
 	private Spinner sportSelectSpinner;
 	private EditText locationEditText;
 	private Button findLocationButton;
 	private Button datePickButton;
 	private Button timePickButton;
 	private EditText maxPlayersEditText;
 	private ToggleButton visibilityToggleButton;
 	
 	private int mHour, mMinute;
 	private int mYear, mMonth, mDay;
 	
 	static final int DATE_DIALOG_ID = 0;
 	static final int TIME_DIALOG_ID = 1;
 	
 	// callback received when user sets the date in the dialog
 	private DatePickerDialog.OnDateSetListener dateSetListener =
 			new DatePickerDialog.OnDateSetListener() {
 				@Override
 				public void onDateSet(DatePicker view, int year,
 									  int monthOfYear, int dayOfMonth) {
 					mYear = year;
 					mMonth = monthOfYear;
 					mDay = dayOfMonth;
 					updateDate();
 				}
 			};
 			
 	// callback received when user sets the time in the dialog 
 	private TimePickerDialog.OnTimeSetListener timeSetListener =
 			new TimePickerDialog.OnTimeSetListener() {
 				@Override
 				public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
 					mHour = hourOfDay;
 					mMinute = minute;
 					updateTime();
 				}
 			};
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.creategame);
 		
 		Bundle extras = getIntent().getExtras();
 		if (extras != null)
 			user = (Person) extras.get("user");
 		else					// This should theoretically never be called
 			user = new Person("Test User", -1);
 		
 		/* ***** CAPTURE VIEW ELEMENTS ***** */
 		createGameButton = (Button) findViewById(R.id.createButton);
 		sportSelectSpinner = (Spinner) findViewById(R.id.sportSelectSpinner);
 		locationEditText = (EditText) findViewById(R.id.locationEditText);
 		findLocationButton = (Button) findViewById(R.id.findLocationButton);
 		datePickButton = (Button) findViewById(R.id.datePickButton);
 		timePickButton = (Button) findViewById(R.id.timePickButton);
 		maxPlayersEditText = (EditText) findViewById(R.id.maxPlayersEditText);
 		visibilityToggleButton = (ToggleButton) findViewById(R.id.visibilityToggleButton);
 		
 		/* ***** DEFINE	UI ELEMENT FUNCTIONALITY ***** */
 		
 		// GAME SELECTION SPINNER
 		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.sports_array,
 																			 android.R.layout.simple_spinner_item);
 		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		sportSelectSpinner.setAdapter(adapter);
 		
 		// LOCATION TEXT FIELD
 		// TODO: input verification
 		locationEditText.setOnKeyListener(new View.OnKeyListener() {
 			public boolean onKey(View v, int keyCode, KeyEvent event) {
 				if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
 					(keyCode == KeyEvent.KEYCODE_ENTER)) {
 					// TODO: once findLocationButton is implemented, have Enter on this text field be equiv. to pressing it
 					return true;
 				}
 				return false;
 			}
 		});
 		
 		// FIND LOCATION BUTTON
 		findLocationButton.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				// TODO: bring up a map centered on either specified or GPS location
 			}
 		});
 		
 		// DATE PICKER BUTTON
 		final Calendar c = Calendar.getInstance();
 		mYear = c.get(Calendar.YEAR);
 		mMonth = c.get(Calendar.MONTH);
 		mDay = c.get(Calendar.DAY_OF_MONTH);
 		updateDate();
 		datePickButton.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				showDialog(DATE_DIALOG_ID);
 			}
 		});
 		
 		// TIME PICKER BUTTON
 		mHour = c.get(Calendar.HOUR_OF_DAY);
 		mMinute = c.get(Calendar.MINUTE);
 		updateTime();
 		timePickButton.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				showDialog(TIME_DIALOG_ID);
 			}
 		});
 		
 		// MAX PLAYERS TEXT FIELD
 		maxPlayersEditText.setText("2");
 		// maybe some input validation here
 		
 		// CREATE GAME BUTTON
 		createGameButton.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				// Get all the parameters to create a new game
 				String sport = sportSelectSpinner.getSelectedItem().toString();
 				int maxPlayers = Integer.parseInt(maxPlayersEditText.getText().toString());
 				boolean privacy = visibilityToggleButton.isChecked();
 				String addr = locationEditText.getText().toString();
 				Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
 				List<Address> locations = null;
 				Location loc = null;
 				GregorianCalendar dt = new GregorianCalendar(mYear, mMonth, mDay, mHour, mMinute);
 				
 				// Note: There's a known issue with Android emulator > API level 8 where this will always throw an
				//   IOException. It should work on an actual device. 
 				try {
 					locations = geocoder.getFromLocationName(addr, 1);
 				}
 				catch (IOException e) {
 					Log.e("IOException", e.getMessage());
 					Context context = getApplicationContext();
					CharSequence errmsg = "Error: Network unavailable, try again later. IOException: " + e.getMessage();
 					Toast.makeText(context, errmsg, Toast.LENGTH_SHORT).show();
 					e.printStackTrace();
 				}
 				
 				if (locations != null && locations.size()>0) {
 					int lat = (int) (locations.get(0).getLatitude() * 1000000);
 					int lon = (int) (locations.get(0).getLongitude() * 1000000);
 					loc = new Location(lat, lon, addr);
 				}
 				else
 					loc = new Location();
 				
 				// create the game using reflection
 				// To avoid exceptions, make sure the spinner only has sports that correspond to a -Game class
 				//   in the eltoraz.pug package, and the corresponding class has a constructor taking one Person as an arg.
 				try {
 					String gameType = "eltoraz.pug." + sport + "Game";
 					Class<?> cl = Class.forName(gameType);
 					java.lang.reflect.Constructor<?> constructor = cl.getConstructor(new Class[] {Person.class});
 					Object newGameType = constructor.newInstance(new Object[] {user});
 					game = (Game) newGameType;
 					
 					game.setDateTime(dt);
 					game.setLocation(loc);
 					game.setPrivate(privacy);
 					game.setMaxPlayers(maxPlayers);
 				}
 				catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 	
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		switch (id) {
 		case DATE_DIALOG_ID:
 			return new DatePickerDialog(this, dateSetListener, mYear, mMonth, mDay);
 		case TIME_DIALOG_ID:
 			return new TimePickerDialog(this, timeSetListener, mHour, mMinute, false);
 		}
 		return null;
 	}
 	
 	private void updateDate() {
 		CharSequence text = new StringBuilder().append(mMonth+1).append("/")
 											   .append(mDay).append("/")
 											   .append(mYear);
 		datePickButton.setText(text);
 	}
 	
 	// time displayed in 24-hour format, maybe have an option for AM/PM display later
 	private void updateTime() {
 		CharSequence text = new StringBuilder().append(pad(mHour)).append(":")
 											   .append(mMinute);
 		timePickButton.setText(text);
 	}
 	
 	private static String pad(int c) {
 		if (c >= 10)
 			return String.valueOf(c);
 		else
 			return "0" + String.valueOf(c);
 	}
 }
