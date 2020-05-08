 package ie.ucd.asteroid;
 
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Comparator;
 import java.util.GregorianCalendar;
 import java.util.Scanner;
 import android.app.Activity;
 import android.appwidget.AppWidgetManager;
 import android.content.ComponentName;
 import android.content.Context;
 import android.database.Cursor;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.os.CountDownTimer;
import android.os.Handler;
 import android.util.Log;
 import android.view.Menu;
import android.view.View;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 
 //**************
 //Ok Shane, I've added in some comments that'll help explain what's going on!
 //**************
 
 public class DoomsDayCountdown extends Activity {
 
 	DBAdapter db = new DBAdapter(this);
 	Calc calc = new Calc();
 	private final String TAG = "Breaks";
	private Handler mHandler = new Handler();
	TextView count_down;
	Typeface count_down_Face;
	String doomsday;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_dooms_day_clock);
 		
 		//I created two long variables. milliseconds will hold the number of milliseconds between now and the doomsday,
 		//'interval' is how long in milliseconds will the countdown wait until it updates: 1000ms = 1 second
 		long milliseconds = 1000;
 		long interval = 1000;
 		
 		DBAdapter db = new DBAdapter(this);
 		db.openDB();
 		
 		//get the id returned by the getDoomsday() function. Haven't tested this out yet, so it may not be correct
 		Log.w("DOOMSDAY", "Calling getDoomsday");
 		int doomsdayID = getDoomsday();
 		Log.w("DOOMSDAY", "Finished getDoomsday");
 		
 		//the 'date' string will hold the date, and the 'time' string will hold the time of the doomsday asteroid, based on its row id
 		//which has been found in the getDoomsday() function.
 		String date = db.getApproachDate(doomsdayID);
 		String time = db.getTime(doomsdayID);
 		
 		
 		double min_dia = db.getMinDiameter(doomsdayID);
 		double max_dia = db.getMaxDiameter(doomsdayID);
 		db.closeDB();
 	
 		Log.w("DOOMSDAY", "Calling scanner");
 		//this bit of code turns the date and time into something readable to the calendar class
 		Scanner sc = new Scanner(date).useDelimiter("-");
 		int day = Integer.parseInt(sc.next());
 		int month = Integer.parseInt(sc.next()) - 1; // 0 is January
 		int year = Integer.parseInt(sc.next());
 
 		sc = new Scanner(time).useDelimiter(":");
 		int hour = Integer.parseInt(sc.next());
 		int min = Integer.parseInt(sc.next());
 		Log.w("DOOMSDAY", "Finished scanner");
 
 		
 		//the object 'doomDate' is created, based on the values produced from the scanner above
 		Log.w("DOOMSDAY", "Creating doomDate");
 		Calendar doomDate = new GregorianCalendar(year, month, day, hour, min);
 		Log.w("DOOMSDAY", "Finished doomDate");
 		// *******
 
 		//the creation of a calendar object defaults to now, as in RIGHT NOW, so the difference in milliseconds is the amount
 		//of time until doomsday happens
 		Log.w("DOOMSDAY", "Creating rightNow");
 		Calendar rightNow = new GregorianCalendar();
 		Log.w("DOOMSDAY", "Finished rightNow");
 		//********
 		//Difference in milliseconds is calculated
 		milliseconds = (doomDate.getTimeInMillis() - rightNow.getTimeInMillis());
 		Log.w("DOOMSDAY", "Calculated milliseconds: " + milliseconds);
 
 		//an object of MyCount is created, and passes the milliseconds until doomsday, and the delay in milliseconds 
 		//the timer will update by ie. 1000ms=1second so the counter will update every second
 		//More on MyCount later...
 		Log.w("DOOMSDAY", "Creating MyCount counter");
 		MyCount counter = new MyCount(milliseconds, interval);
 		Log.w("DOOMSDAY", "Finished MyCount counter");
 
 		Log.w("DOOMSDAY", "Starting counter");
 		//Run the start function in the MyCount class
 		counter.start();
 		Log.w("DOOMSDAY", "Back in main thread from counter");
 		double avg_dia = (min_dia + max_dia) / 2;
 
 		int img = calc.asteroidSizeDecider(avg_dia, true);
 		ImageView ast = (ImageView) findViewById(R.id.count_down);
 		ast.setImageResource(img);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 
 	//Welcome to the MyCount class!
 	//It's basically a Countdown timer
 	public class MyCount extends CountDownTimer {
 		ComponentName thisWidget;
 		AppWidgetManager appWidgetManager;
 		Context context;
 		int[] appWidgetIds;
 
 		//this is where the counter object is sent when it was made previously
 		public MyCount(long milliseconds, long interval) {
 			super(milliseconds, interval);
 			Log.w("DOOMSDAY", "Constructor in MyCount called");
 		}
 
 
 		@Override
 		//the onFinish() function allows you to decide what happens when the timer reaches 00:00:00:00
 		//It'll execute this code only when the time runs out
 		//the countdown timer class requires an onFinish() function to be created
 		public void onFinish() {
 			Log.w("DOOMSDAY", "onFinish called");
 		}
 
 		//onTick() is executed as often as the 'interval' dictates, so again 1000ms=1second ie. the function runs each second.
 		//It takes in the amount of milliseconds until the time reaches 00:00:00:00, and calls it 'millisUntilFinished'. This is value is decided
 		//when the 'counter' object was created previously.
 		//Again, it is a required method that the CoundownTimer needs to work.
 		@Override
 		public void onTick(long millisUntilFinished) {
 			Log.w("DOOMSDAY", "onTick started");
			count_down = (TextView) findViewById(R.id.count_down);
 			Log.w("DOOMSDAY", "Initialised count_down TextView");
 
 			//finals are 'threadsafe', not sure if it's needed here, but it's better to be on the safe side
 			final long ms = millisUntilFinished;
 
 			//This bit converts the time in milliseconds into days, hours, minutes, and seconds. 
 			//It's working fine, so I wouldn't worry about trying to figure out what each line means, it's a bit confusing!
 			String days = Integer.toString((int) (ms / 1000) / 86400);
 			String seconds = Integer.toString((int) ((ms) / (1000)) % 60);
 			if (seconds.length() < 2) {
 				seconds = "0" + seconds;
 			}
 			String minutes = Integer.toString((int) (ms / (1000 * 60)) % 60);
 			if (minutes.length() < 2) {
 				minutes = "0" + minutes;
 			}
 			String hours = Integer.toString((int) (ms / (1000 * 60 * 60)) % 24);
 			if (hours.length() < 2) {
 				hours = "0" + hours;
 			}
			doomsday = days + ":" + hours + ":" + minutes + ":"
 					+ seconds;
 
 
			count_down_Face = Typeface.createFromAsset(getAssets(),
 					"fonts/digital-7.ttf");
			mHandler.post(new Runnable() {
                public void run() {
                	count_down.setTypeface(count_down_Face);
        			count_down.setText(doomsday);
                }
            });
			/*count_down.setTypeface(count_down_Face);
			count_down.setText(doomsday);*/
 			
 
 		}
 	}
 	
 	// Retrieve the 'Doomsday' Asteroid id, based on kinetic energy and
 	// proximity to the Earth
 	// NOT TESTED YET
 public int getDoomsday() {
 	db.openDB();
 		
 		//gets total number of asteroids
 		int numberOfAsteroids = db.countAsteroids();
 		
 		//creates a 2d array for storing each asteroid, its rowID and its Doomsday value
 		double[][] asteroidArray = new double[numberOfAsteroids][2];
 		Cursor c = db.getAllAsteroids();
 		
 		//this is the density of an asteroid (2g/cm^3 according to wikipedia)
 		//I converted it to kg /m^3 which makes it 2000 kg / m^3
 		double asteroidDensity = 2000;
 		double asteroidVolume, asteroidMass, asteroidKinetic, asteroidRadius, doomsdayValue;
 		int counter = 0;
 		
 		//allows function to read columns from database 
 		/*int colID = c.getColumnIndex(DBAdapter.KEY_ROWID);
 		int colApproachDistance = c.getColumnIndex(DBAdapter.KEY_APPROACHDISTANCE);
 		int colMaxDiameter = c.getColumnIndex(DBAdapter.KEY_MAXDIAMETER);
 		int colVelocity = c.getColumnIndex(DBAdapter.KEY_RELATIVEVELOCITY);*/
 
 		// The Torino Scale is a method for categorizing the impact hazard
 		// associated with near-Earth objects (NEOs)
 		// such as asteroids and comets. It is intended as a communication tool
 		// for astronomers and the public to
 		// assess the seriousness of collision predictions, by combining
 		// probability statistics and known
 		// kinetic damage potentials into a single threat value.
 
 		// In this case, we will calculate the kinetic energy of each asteroid,
 		// and its proximity to the earth in order to calculate the most
 		// dangerous asteroid
 
 		
 
 		Log.w("DOOMSDAY", "Launching loop");
 		for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
 			
 			//my mistake! moved this into the for loop where it belongs. Hope this makes it clearer!
 			int asteroidID = db.getAsteroidID(c);
 			double asteroidDistance = db.getApproachDistance(c);
 			double asteroidDiameter = (db.getMaxDiameter(c) + db.getMinDiameter(c)) / 2.0;
 			double asteroidVelocity = db.getRelativeVelocity(c);
 			
 			Log.w("DOOMSDAY", "Asteroid ID: " + asteroidID + ", distance: " + asteroidDistance + ", diameter: " + asteroidDiameter + ", velocity: " + asteroidVelocity);
 			
 			//calculates radius
 			asteroidRadius = 1.0 / 2.0 * asteroidDiameter;
 			//calculates volume of asteroid (Assuming it's a sphere, which it isn't really but close enough)
 			asteroidVolume = (4.0 / 3.0) * Math.PI
 					* Math.pow(asteroidRadius, 3);
 			//calculates mass of asteroid
 			asteroidMass = asteroidVolume * asteroidDensity;
 			//calculate kinetic energy
 			asteroidKinetic = (1.0 / 2.0) * asteroidMass * asteroidVelocity;
 			
 			doomsdayValue = asteroidKinetic / asteroidDistance;
 
 			asteroidArray[counter][0] = asteroidID;
 			asteroidArray[counter][1] = doomsdayValue;
 
 			counter++;
 		}
 		Log.w("DOOMSDAY", "Finished loop");
 
 	
 		Arrays.sort(asteroidArray, new Comparator<double[]>() { public int
 		compare(double[] o1, double[] o2) { return
 		Double.valueOf(o2[1]).compareTo(Double.valueOf(o1[1])); } });
 		
 
 		double doubleId = asteroidArray[0][0];
 		int id = (int) doubleId;
 		c.close();
 		db.closeDB();
 		return id;
 		
 	}
 
 
 }
