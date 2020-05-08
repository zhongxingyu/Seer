 package com.example.projectv1;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 import net.fortuna.ical4j.data.CalendarBuilder;
 import net.fortuna.ical4j.data.ParserException;
 import net.fortuna.ical4j.model.Component;
 import net.fortuna.ical4j.model.Parameter;
 import net.fortuna.ical4j.model.Property;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 
 import android.app.Activity;
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.AssetManager;
 import android.content.res.Configuration;
 import android.content.res.Resources;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.DisplayMetrics;
 import android.view.Menu;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.widget.Button;
 import android.widget.DigitalClock;
 import android.widget.TextView;
 
 import com.example.projectv1.timeline.TimelineImageView;
 
 public class MainActivity extends Activity {
 	// Create our linkedlist of class bookings
 	ArrayList<ClassBooking> cb = new ArrayList<ClassBooking>();
 
 	String language;
 	FiveMinRefresh fiveMin;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		 Button menu=(Button)findViewById(R.id.menu);
 		 Button eng=(Button)findViewById(R.id.english);
 		 Button ie=(Button)findViewById(R.id.gaeilge);
 		 TextView status=(TextView)findViewById(R.id.content);
 		 DigitalClock clock=(DigitalClock)findViewById(R.id.digitalClock1);		 
 		 TextView currentdate=(TextView)findViewById(R.id.date);
 
 		 Typeface qs1=Typeface.createFromAsset(getAssets(), "fonts/Quicksand-Bold.ttf");
 		 Typeface qs2=Typeface.createFromAsset(getAssets(), "fonts/Quicksand-Regular.ttf");
 
 		 menu.setTypeface(qs2);
 		 eng.setTypeface(qs2);
 		 ie.setTypeface(qs2);
 		 status.setTypeface(qs2);
 		 clock.setTypeface(qs2);
 		 currentdate.setTypeface(qs2);
 		 
 		 
 		 Date d=new Date();
 		 SimpleDateFormat dateFormat = new SimpleDateFormat("EE, MMM dd yyyy");
 		 String formattedDate = dateFormat.format(d);
 		 currentdate.setText(formattedDate);
 		
 		
 		try {
 			cb = getClassBooking();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ParserException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		// initialise preferences
 		
 		// create alarm to start device at predefined time next day
         SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
         int startHour = Integer.parseInt(settings.getString("startTime", "8"));
 		createStartAlarm(startHour,0);
 
 		TextView content = new TextView(this);
 		content = (TextView) findViewById(R.id.content);
 
 		content.setText("Getting data");
 		fiveMin = new FiveMinRefresh(cb, content);
 		fiveMin.execute();
 
 		// Timeline Listener.
 		final TimelineImageView timelineView = (TimelineImageView) findViewById(R.id.timeline);
 		timelineView.setTimes(cb);
 		timelineView.invalidate(); // Redraw timelineView
 		timelineView.setBackgroundColor(0xFFb0d4e8);
 
 		OnTouchListener timelineListener = new OnTouchListener() {
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				float positionX = event.getX();
 				int width = timelineView.getWidth();
 
 				if (event.getAction() == MotionEvent.ACTION_DOWN) {
 					Map<String, Integer[]> draw_map = TimelineImageView
 							.createDrawMap(cb, width);
 
					boolean occupied = false;
 					String class_id = "";
 					for (Map.Entry<String, Integer[]> entry : draw_map
 							.entrySet()) {
 						if (entry.getValue()[0] < positionX
 								&& entry.getValue()[1] > positionX) {
							occupied = true;
 							class_id = entry.getKey();
 						}
 					}
 					/*
 					 * String message; if (occupied) { message =
 					 * "The room is occupied - class UID " + class_id; } else {
 					 * message = "The room is available"; }
 					 * 
 					 * Toast.makeText(getApplicationContext(),
 					 * "You clicked at: " + positionX + "\n" + message,
 					 * Toast.LENGTH_SHORT).show();
 					 */
 					String summary="", start="", end="", url="",organizer="";
 					
 					for(ClassBooking key:cb){
 						if(key.getUID().equals(class_id)){
 							
 							summary = key.getSummary();
 							start   = key.getStartTime();
 							end     = key.getEndTime();
 							url     = key.getURL();
 							organizer= key.getOrganizer();
 							break;
 						}
 					}
 					Intent openDetails = new Intent("android.intent.action.DETAILS");
 					openDetails.putExtra("uid", class_id);
 					openDetails.putExtra("summary", summary);
 					openDetails.putExtra("start", start);
 					openDetails.putExtra("end", end);
 					openDetails.putExtra("url", url);
 					openDetails.putExtra("organizer", organizer);
 					startActivity(openDetails);
 					return true;
 				}
 				return false;
 			}
 		};
 
 		timelineView.setOnTouchListener(timelineListener);
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 
 	public void setLanguage(String lang) {
 		Resources standardResources = getBaseContext().getResources();
 		AssetManager assets = standardResources.getAssets();
 		DisplayMetrics metrics = standardResources.getDisplayMetrics();
 		Configuration config = new Configuration(
 				standardResources.getConfiguration());
 		config.locale = new Locale(lang);
 		Resources defaultResources = new Resources(assets, metrics, config);
 
 	}
 
 	// is called from XML when button is clicked
 	public void buttonClick(View v) {
 		switch (v.getId()) {
 		case R.id.menu:
 			Intent openMenu = new Intent("com.project.MENU");
 			startActivity(openMenu);
 			break;
 
 		case R.id.english:
 
 			setLanguage("en");
 
 			break;
 
 		case R.id.gaeilge:
 
 			setLanguage("ga");
 
 			break;
 		}
 
 	}
 
 	public static String eventActionToString(int eventAction) {
 		switch (eventAction) {
 		case MotionEvent.ACTION_CANCEL:
 			return "Cancel";
 		case MotionEvent.ACTION_DOWN:
 			return "Down";
 		case MotionEvent.ACTION_MOVE:
 			return "Move";
 		case MotionEvent.ACTION_OUTSIDE:
 			return "Outside";
 		case MotionEvent.ACTION_UP:
 			return "Up";
 		case MotionEvent.ACTION_POINTER_DOWN:
 			return "Pointer Down";
 		case MotionEvent.ACTION_POINTER_UP:
 			return "Pointer Up";
 		}
 		return "";
 	}
 
 	// Calendar calendar = Calendars.load(new
 	// URL("http://ical4j.cvs.sourceforge.net/viewvc/*checkout*/ical4j/iCal4j/etc/samples/valid/Australian32Holidays.ics"));
 	public java.util.ArrayList<ClassBooking> getClassBooking()
 			throws IOException, ParserException {
 		String uid = "";
 		String summary = "";
 		String st = "";
 		String et = "";
 		String url = "";
 		String organizer = "";
 		URL add = null;
 		
 		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);   
 		String calurl = preferences.getString("urliCalendar", "www.ucd.ie");
 		
 		try {
 			add = new URL("http://www.chartspms.com/android/calendar.ics");
 		} catch (MalformedURLException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		net.fortuna.ical4j.model.Calendar calendar = new net.fortuna.ical4j.model.Calendar();
 		CalendarBuilder builder = new CalendarBuilder();
 		// CalendarBuilder builder = new CalendarBuilder();
 
 		calendar = builder.build(add.openStream());
 		//calendar = builder.build(getResources().openRawResource(0) .openStream());
 		//InputStream inputStream = getResources().openRawResource(R.raw.calendar);
 		//calendar = builder.build(inputStream);
 		if (calendar != null) {
 
 			// Iterating over a Calendar
 			for (Iterator i = calendar.getComponents().iterator(); i.hasNext();) {
 				Component component = (Component) i.next();
 
 				System.out.println("Component [" + component.getName() + "]");
 
 				for (Iterator j = component.getProperties().iterator(); j
 						.hasNext();) {
 					Property property = (Property) j.next();
 
 					if (property.getName().equals("UID")) {
 						uid = property.getValue();
 					}
 					if (property.getName().equals("SUMMARY")) {
 						summary = property.getValue();
 					}
 					if (property.getName().equals("DTSTART")) {
 						st = property.getValue();
 					}
 					if (property.getName().equals("DTEND")) {
 						et = property.getValue();
 					}
 					if (property.getName().equals("URL")) {
 						url = property.getValue();
 					}
 					if (property.getName().equals("ORGANIZER")) {
 						organizer= property.toString().substring(13);
 						int end = organizer.indexOf(":");
 						organizer = organizer.substring(0, end);
 					}
 				
 				}
 				cb.add(new ClassBooking(uid, summary, st, et, url,organizer));
 			}
 		}
 		for (ClassBooking booking : cb) {
 			/**
 			 * String message = "UID:" + booking.getUID() + "ST: " +
 			 * booking.getStartTime() + "ET: " + booking.getEndTime();
 			 * Log.i("pab", message);
 			 **/
 
 			System.out.println("UID:" + booking.getUID() + "ST: "
 					+ booking.getStartTime() + "ET: " + booking.getEndTime());
 
 		}
 
 		return cb;
 	}
 	
 	// creates AlarmManager object to restore device from standby at specified time
 	public void createStartAlarm(int hour, int minute)
 	{
 		// create calendar object for tomorrow using time parameters
         Calendar cal = Calendar.getInstance();
         cal.setTimeInMillis(System.currentTimeMillis());
         cal.add(Calendar.DATE, 1);
         cal.set(Calendar.HOUR_OF_DAY, hour);
         cal.set(Calendar.MINUTE, minute);
         cal.set(Calendar.SECOND, 0);
         cal.set(Calendar.MILLISECOND, 0);
 		
 		// initialise intent to start boot app service
         Intent myIntent = new Intent(getBaseContext(), MyScheduledReceiver.class);
 
         PendingIntent pendingIntent
          = PendingIntent.getBroadcast(getBaseContext(),
            0, myIntent, 0);
       
         // bind intent to alarm
         AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
         
         alarmManager.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);
 	}
 	
 	// method to send statistics report to server
 	// this is called at the end of the day prior to standby
 	public void sendStats()
 	{
 		// Create holder to access preferences
 		SharedPreferences pref;
 		pref = getPreferences(Context.MODE_PRIVATE);
 		
         // Create HttpClient and header
         HttpClient httpclient = new DefaultHttpClient();
         HttpPost httppost = new HttpPost("http://www.chartspms.com/android/processStats.php"); // ## update to live server
 
         try {
             // Add your data
             List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(4);
             nameValuePairs.add(new BasicNameValuePair("deviceid", "1"));
             nameValuePairs.add(new BasicNameValuePair("restart", "" + pref.getInt("restart", 0)));
             nameValuePairs.add(new BasicNameValuePair("interact", "" + pref.getInt("interact", 0)));
             nameValuePairs.add(new BasicNameValuePair("network", "" + pref.getInt("network", 0)));
             httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
 
             // Execute HTTP Post Request
             HttpResponse response = httpclient.execute(httppost);
             
         } catch (Exception e)
         {
             // update nNetwork in pref
             SharedPreferences.Editor editor = pref.edit();
             editor.putInt("nInteract", pref.getInt("nInteract", 0) + 1);
         }
 	}
 
 }
