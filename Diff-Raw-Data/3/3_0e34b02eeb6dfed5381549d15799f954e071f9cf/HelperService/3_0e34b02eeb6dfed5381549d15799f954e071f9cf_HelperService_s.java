 package seniordesignradioapp.spectral.tweets;
 
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Timer;
 import java.util.TimerTask;
 import android.app.Service;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.net.wifi.ScanResult;
 import android.net.wifi.WifiManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.widget.Toast;
 
 public class HelperService extends Service implements LocationListener
 {
 
 	private static Timer timer = new Timer();
 	private static WifiManager wifi;
 	private static BroadcastReceiver receiver;
 	private static LocationManager location;
 	private static repeatingClass gatherInfoAndTweetIt;
 	
 	private static final int UPDATE_MIN_FREQUENCY_MILLISECONDS = 0;
 	private static final int UPDATE_MIN_DISTANCE_METERS = 0;
 	private static final int TIMER_FREQUENCY = 30 * 1000;
 	private static boolean serviceStarted = false;
 	private static double temp_longitude = 0.0;
 	private static double temp_latitude = 0.0;
 	private static int location_count = 0;
 	private static String latitude;
 	private static String longitude;
 	private static DecimalFormat lon = new DecimalFormat("000.000000");
 	private static DecimalFormat lat = new DecimalFormat("00.000000");
 	private static String gps_info = "";
 	private static String wifi_info = "";
 	private static final String hashtag = "#ajd7v-34 ";
 	private static int display_count = 0;
 	
 	private static final ArrayList<Integer> channelNumbers = new ArrayList<Integer> (Arrays.asList(0, 2412, 2417, 2422, 2427, 2432, 2437, 2442, 2447, 2452, 2457, 2462));
 	private static List <ScanResult> wifiScanResults;
 	private static ScanResult sr;
 	private static Iterator<ScanResult> it;
 	private static ScanResult channel_info[] = new ScanResult[12];
 	private static Map<Integer, String> levels = new HashMap<Integer, String>();
 	private static String empty_channel = "__________";		// 10 spaces
 	private static String stringToTweet = "";
 	
 	private static final Handler handler = new Handler();
 	
 	private static final Runnable updateTextTweetSent = new Runnable()
 	{
 		public void run()
 		{
 			updateTextFromHelperTweetSent();
 		}
 	};
 	
 	private static final Runnable updateTextTweetNotSent = new Runnable()
 	{
 		public void run()
 		{
 			updateTextFromHelperTweetNotSent();
 		}
 	};
 	
 	private static final Runnable updateTextNoNewInfo = new Runnable()
 	{
 		public void run()
 		{
 			updateTextFromHelperNoNewInfo();
 		}
 	};
 	
 	public static void updateTextFromHelperTweetSent()
 	{
 		Main.changeText(display_count + "\t auto-tweeting\n" + stringToTweet);
 	}
 	
 	public static void updateTextFromHelperTweetNotSent()
 	{
 		Main.changeText(display_count + "\t auto-tweet not sent");
 	}
 	
 	public static void updateTextFromHelperNoNewInfo()
 	{
 		Main.changeText(display_count + "\t no new GPS info");
 	}
 	
 	public void onCreate()
 	{
 		super.onCreate();
 		
 		/* setup GPS updates */
 		location = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 		location.requestLocationUpdates(LocationManager.GPS_PROVIDER, UPDATE_MIN_FREQUENCY_MILLISECONDS, UPDATE_MIN_DISTANCE_METERS, this);
 		wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
 		gatherInfoAndTweetIt = new repeatingClass();
 		
 		/* setup WIFI updates */
 		receiver = new WifiScanner();
 		registerReceiver(receiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
 		
 		/* tell user that service has started */
 		Toast.makeText(getApplicationContext(), "service started", Toast.LENGTH_SHORT).show();
 		serviceStarted = true;
 		
 		/* initialize necessary arrays/maps */
 		init_levels();
 		
 		/* start the timer that will run the function that does all the work */
 		timer.scheduleAtFixedRate(gatherInfoAndTweetIt,  0,  TIMER_FREQUENCY);
 	}
 	
 	public void onDestroy()
 	{
 		/* cancel GPS updates */
 		location.removeUpdates(this);
 		
 		/* cancel WIFI updates */
 		unregisterReceiver(receiver);
 		
 		/* tell user that service has stopped */
 		Toast.makeText(getApplicationContext(), "service stopped", Toast.LENGTH_SHORT).show();
 		serviceStarted = false;
 	}
 	
 	static public void doWork()
 	{
 		display_count ++;
 		
 		if (location_count > 0)
 		{
 			/* format the GPS information */
 			gps_info = "";
 			latitude = lat.format(temp_latitude / location_count).replace(".",  "");
 			longitude = lon.format(temp_longitude / location_count).replace(".",  "");
 			gps_info = ((temp_latitude / (double) location_count) > 0 ? "+" : "") + latitude + ((temp_longitude / (double) location_count) > 0 ? "+" : "") + longitude;
 			
 			/* get WIFI scan results and filter.  we want the strongest signal on each channel */
 			wifi_info = "";
 			wifiScanResults = wifi.getScanResults();
 			it = wifiScanResults.iterator();
 			
 			for (int i = 1; i < 12; i++)
 			{
 				channel_info[i] = null;
 			}
 			
 			while (it.hasNext())
 			{
 				sr = it.next();
 				int channel = channelNumbers.indexOf(Integer.valueOf(sr.frequency));
 
 				if (channel_info[channel] == null)
 				{
 					channel_info[channel] = sr;
 				}
 				else
 				{
 					if (channel_info[channel].level < sr.level)
 					{
 						channel_info[channel] = sr;
 					}
 				}
 			}
 			
 			/* format the entry for each channel and add it to the wifi string */
 			for (int i = 1; i < 12; i++)
 			{
 				if (channel_info[i] != null)
 				{
 					wifi_info += (levels.get(channel_info[i].level) == null ? "0" : levels.get(channel_info[i].level))  + channel_info[i].BSSID.replace(":", "").substring(2, 11);
 				}
 				else
 				{
 					wifi_info += empty_channel;
 				}
 			}
 			
 			/* set the final string. should be hashtag, wifi info, and gps info */
 			stringToTweet = hashtag + wifi_info + gps_info;
 			
 			if (Main.twitter != null)
 			{
				Main.twitter.setStatus(stringToTweet);
 				handler.post(updateTextTweetSent);
 			}
 			else
 			{
 				handler.post(updateTextTweetNotSent);
 			}
 		}
 		else
 		{
 			handler.post(updateTextNoNewInfo);
 		}
 	}
 	
 	private static class repeatingClass extends TimerTask
 	{
 
 		@Override
 		public void run()
 		{
 			doWork();
 		}
 		
 	}
 	
 	private static class WifiScanner extends BroadcastReceiver
 	{
 		
 		public WifiScanner()
 		{
 			wifi.startScan();
 		}
 
 		@Override
 		public void onReceive(Context arg0, Intent arg1)
 		{
 			wifi.startScan();
 		}
 		
 	}
 
 	@Override
 	public IBinder onBind(Intent arg0)
 	{
 		return null;
 	}
 
 	public void onLocationChanged(Location location)
 	{
 		if (serviceStarted)
 		{
 			temp_longitude += location.getLongitude();
 			temp_latitude += location.getLatitude();
 			location_count ++;
 		}
 		else
 		{
 			temp_longitude = 0.0;
 			temp_latitude = 0.0;
 			location_count = 0;
 		}
 	}
 
 	public void onProviderDisabled(String provider)
 	{
 		Toast.makeText(getApplicationContext(), "GPS disabled", Toast.LENGTH_SHORT).show();
 	}
 
 	public void onProviderEnabled(String provider)
 	{
 		Toast.makeText(getApplicationContext(), "GPS enabled", Toast.LENGTH_SHORT).show();
 	}
 
 	public void onStatusChanged(String provider, int status, Bundle extras)
 	{
 		
 	}
 	
 	public void init_levels()
 	{
 		levels.put(-20, "1");
 		levels.put(-21, "2");
 		levels.put(-22, "3");
 		levels.put(-23, "4");
 		levels.put(-24, "5");
 		levels.put(-25, "6");
 		levels.put(-26, "7");
 		levels.put(-27, "8");
 		levels.put(-28, "9");
 		levels.put(-29, "a");
 		levels.put(-30, "b");
 		levels.put(-31, "c");
 		levels.put(-32, "d");
 		levels.put(-33, "e");
 		levels.put(-34, "f");
 		levels.put(-35, "g");
 		levels.put(-36, "h");
 		levels.put(-37, "i");
 		levels.put(-38, "j");
 		levels.put(-39, "k");
 		levels.put(-40, "l");
 		levels.put(-41, "m");
 		levels.put(-42, "n");
 		levels.put(-43, "o");
 		levels.put(-44, "p");
 		levels.put(-45, "q");
 		levels.put(-46, "r");
 		levels.put(-47, "s");
 		levels.put(-48, "t");
 		levels.put(-49, "u");
 		levels.put(-50, "v");
 		levels.put(-51, "w");
 		levels.put(-52, "x");
 		levels.put(-53, "y");
 		levels.put(-54, "z");
 		levels.put(-55, "A");
 		levels.put(-56, "B");
 		levels.put(-57, "C");
 		levels.put(-58, "D");
 		levels.put(-59, "E");
 		levels.put(-60, "F");
 		levels.put(-61, "G");
 		levels.put(-62, "H");
 		levels.put(-63, "I");
 		levels.put(-64, "J");
 		levels.put(-65, "K");
 		levels.put(-66, "L");
 		levels.put(-67, "M");
 		levels.put(-68, "N");
 		levels.put(-69, "O");
 		levels.put(-70, "P");
 		levels.put(-71, "Q");
 		levels.put(-71, "R");
 		levels.put(-73, "S");
 		levels.put(-74, "T");
 		levels.put(-75, "U");
 		levels.put(-76, "V");
 		levels.put(-77, "W");
 		levels.put(-78, "X");
 		levels.put(-79, "Y");
 		levels.put(-80, "Z");
 	}
 
 }
