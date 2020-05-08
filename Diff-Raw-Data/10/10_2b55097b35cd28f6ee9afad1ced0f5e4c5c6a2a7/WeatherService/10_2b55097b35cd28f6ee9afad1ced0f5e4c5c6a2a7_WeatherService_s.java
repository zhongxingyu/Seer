 package com.sobinary.work;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import com.sobinary.app.BigInfoProvider;
 import com.sobinary.base.Core;
 
 
 
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.location.Location;
 import android.location.LocationManager;
 import android.os.IBinder;
 import android.preference.PreferenceManager;
 
 public class WeatherService extends Service 
 {
 	public static final String REQ_TYPE = "reqType"; 
 	public static final int GET_NOW = 1;
 	public static final int GET_OUTLOOK = 2;
	private static final String URL_LEFT = "http://api.wunderground.com/api/5880406b8de33379";
 
 	private static String resp;
 
 	@Override 
 	public void onCreate()
 	{
 		super.onCreate();
 	}
 
 	@Override
 	public int onStartCommand(Intent intent, int flags, int id)
 	{
 		super.onStartCommand(intent, flags, id);
 		if(!Core.isOnline(getApplicationContext())) return Service.START_NOT_STICKY;
 		
 		int req = intent.getExtras().getInt(REQ_TYPE);
 		Context cont = getApplicationContext();
 		Core.print("Hello Weather Req: " + req);
 
 		String loc = getLocation(cont);
 		if(loc != null) 
 		{
 			if(req==GET_NOW) getWeather(cont, loc); 
 			else if(req==GET_OUTLOOK) getOutlook(cont, loc);
 			else Core.print("Wrong flags dumbass");
 		}
 		this.stopSelf();
 		return Service.START_NOT_STICKY;
 	}
 
 	private static String getLocation(Context cont)
 	{
 		try
 		{
 			Core.print("Getting System Weather...");
 			LocationManager locMan = (LocationManager) cont.getSystemService(Context.LOCATION_SERVICE);
 			Location netLoc = locMan.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
 			if(netLoc == null)
 			{ 
 				Core.print("NetLoc null"); 
 				BigInfoProvider.setTextLine(cont, 1, "Oops", "GPS Location");
 				return null; 
 			}
 			return netLoc.getLatitude()+","+netLoc.getLongitude();
 		}
 		catch(Exception e)
 		{
 			Core.print("Error getting system location");
 			Core.printe(e);
 			BigInfoProvider.setTextLine(cont, 1, "Oops", "GPS Location");
 			BigInfoProvider.setTextLine(cont, 2, "Oops", "GPS Location");
 			return null;
 		}
 	}
 
 	public static float[] getQuickLook(Context cont)
 	{
 		if(!Core.isOnline(cont)) return null;
 		
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(cont);
 		
 		String loc = getLocation(cont);
 		if(loc == null) return null;
 		boolean celc = prefs.getBoolean("eurotemp", false);
 		Core.print("Weather get for: " + loc);
 		
 		sendReceive(URL_LEFT + "/conditions/q/" + loc + ".json" );
 		String[]ray = extractTemperature(celc);
 		if(ray == null) return null;
 		int t = Integer.parseInt(ray[0]);
 		
 		sendReceive(URL_LEFT + "/forecast/q/" + loc + ".json" );
 		int[]extremae = extractExtremae(cont, celc);
 		if(extremae == null) return null;
 		float hi = extremae[1], lo = extremae[0];
 		float rat = (t - lo) / (hi - lo); 
 		rat = rat > 1 ? 1 : rat;
 		
 		Core.print("[QuickLook]Weather fetch success");
 		return new float[]{t, rat};
 	}
 	
 	private static void getWeather(Context cont, String loc)
 	{
 		BigInfoProvider.setTextLine(cont, 1, "...", "...");
 		sendReceive(URL_LEFT + "/conditions/q/" + loc + ".json");
 		
 		boolean celc = prefs(cont).getBoolean("eurotemp", false);
 		String[]weather = extractTemperature(celc);
 
 		if(weather == null) 
 			BigInfoProvider.setTextLine(cont, 1, "Oops", "Try Again");
 		else
 			BigInfoProvider.setTextLine(cont, 1, weather[1], pretty(Integer.parseInt(weather[0]),celc));
 	}
 
 	private static String[] extractTemperature(boolean celc)
 	{
 		if(resp == null) return null; 
 
 		try 
 		{				
 			JSONObject root = new JSONObject(resp);
 			JSONObject crtObs = root.getJSONObject("current_observation");
 			String condition = crtObs.getString("weather");
 
 			int t = (int)(celc ? crtObs.getDouble("temp_c") : crtObs.getDouble("temp_f"));
 			return new String[]{t+"", condition};
 		}
 		catch (Exception e) 
 		{
 			Core.print("[GetWeather]Error JSON proc");
 			Core.printe(e);
 			return null;
 		}			
 	}
 	
 	private static int[]extractExtremae(Context cont, boolean celc)
 	{
 		if(resp == null) return null;
 		
 		try
 		{
 			JSONObject root = new JSONObject(resp);
 			JSONObject simp = root.getJSONObject("forecast").getJSONObject("simpleforecast");
 			JSONObject now = simp.getJSONArray("forecastday").getJSONObject(0);
 			
 			int low = now.getJSONObject("low").getInt(unit(celc));
 			int high = now.getJSONObject("high").getInt(unit(celc));
 			return new int[]{low, high};
 		}
 		catch(Exception e)
 		{
 			Core.print("Parse failure: " + e.getMessage());
 			return null;
 		}
 	}
 	
 	private void getOutlook(Context cont, String loc)
 	{
 		BigInfoProvider.setTextLine(cont, 2, "...", "...");
 		Core.print("Outlook for: " + loc);
 		long start = System.currentTimeMillis();
 		sendReceive(URL_LEFT + "/hourly/q/" + loc + ".json" );
 		Core.print("Time: " + (System.currentTimeMillis()-start));
 		if(resp != null) 
 		{
 			Core.print("[GetWeather]Got response!");			
 			try 
 			{	
 				JSONObject root = new JSONObject(resp);
 				JSONArray days = root.getJSONArray("hourly_forecast");
 				int first =-1, last=0, dif = 0, pop = 0, popMax = 0, popMin = 100;
 
 				boolean preciping = false, snow = false;
 				int h_offset = Integer.parseInt(days.getJSONObject(0).getJSONObject("FCTTIME").getString("hour"));
 				
 				for(int i=0; i < 12; i++)
 				{
 					JSONObject day = days.getJSONObject(i);
 					pop = Integer.parseInt( day.getString("pop") );
 					
 					if(pop >= 20)
 					{
 						popMax = Core.max(pop, popMax);
 						popMin = Core.min(pop, popMin);
 						dif = pop - dif;
 						preciping = true;
 						first = (first==-1) ? i : first;
 						last = i;
 						snow = snow || day.getString("condition").contains("Snow");
 					}
 					else
 						if(preciping)
 							break;
 				}
 				
 				String precipType = (snow) ? "Snow" : "Rain";
 				boolean euro = prefs(cont).getBoolean("eurotime", false);
 				String[]rainMsg = rainlessText();
 
 				if(first == -1)
 					BigInfoProvider.setTextLine(this, 2, rainMsg[0], rainMsg[1]);
 				else if(first == 0 && last == 0)
 					BigInfoProvider.setTextLine(this, 2, precipType, "Almost Done");
 				else if(first == 0)
 					BigInfoProvider.setTextLine(this, 2, precipType, popMax + "% Until " + iToH(h_offset+last, euro, true));
 				else
 					BigInfoProvider.setTextLine(this, 2, precipType, popMax+ "% at "+iToH(h_offset+first, euro, true));			
 			}
 
 			catch(Exception e)
 			{
 				Core.print("[GetOutlook]Error JSON proc");
 				Core.printe(e);
 				BigInfoProvider.setTextLine(this, 2, "Oops", "Try Again");
 			}
 		}
 		else
 			BigInfoProvider.setTextLine(this, 2, "Oops", "Try Again");
 	}	
 
 	private static String[]rainlessText()
 	{
 		int ind = (int)(Math.random() * 2);
 		switch(ind)
 		{
 			case 0:return new String[]{"No Rain", "In Sight"};
 			case 1:return new String[]{"Lucky","Skies are clear"};
 			default: return new String[]{"Dont","See me"};
 		}
 	}
 	
 	private static String iToH(int h, boolean euro, boolean post)
 	{
 		Core.print("In h: " + h);
 		if(h == 24) return "Midnight";
 		else if(h == 12) return "Noon";
 		else if( euro ) return h+(post ? "h" : "");
 		else
 		{
 			String suf = h < 12 ? "AM" : "PM";
 			return h%12 + (post ? suf : "");
 		}
 	}
 
 	//5355337
 	private static synchronized void sendReceive(final String url)
 	{
 		Thread net = new Thread()
 		{
 			@Override
 			public synchronized void run()
 			{
 				try 
 				{
 					final DefaultHttpClient client = new DefaultHttpClient();
 					final HttpGet getRequest = new HttpGet(url);
 
 					HttpResponse response = client.execute(getRequest);
 					int statusCode = response.getStatusLine().getStatusCode();
 					if( statusCode == HttpStatus.SC_OK )
 					{
 //						Core.print("[S-R]Good response");
 						InputStream in = response.getEntity().getContent();
 						resp = isToS(in);						
 					}
 					else 
 					{
 						Core.print("[S-R]Bad response code: " + statusCode);
 						resp = null;
 					}
 				} 
 				catch (Exception e)
 				{
 					Core.print("[S-R]Fail General I/O");
 					Core.printe(e);
 					resp = null;
 				}		
 			}
 		};
 		net.start();
 		try{ net.join(); }
 		catch(InterruptedException e){Core.print("Interrupted!");}
 	}
 
 	private static String isToS(InputStream is)
 	{
 		try
 		{
 			BufferedReader br = new BufferedReader( new InputStreamReader(is));
 			String line, whole = "";
 			while ((line = br.readLine()) != null)  
 				whole += line;
 			return whole;
 		}
 		catch(Exception e)
 		{
 			Core.print("Error inputStream to String");
 			Core.printe(e);
 			return null;
 		}
 	}
 
 	private static SharedPreferences prefs(Context cont)
 	{
 		return PreferenceManager.getDefaultSharedPreferences(cont);
 	}
 
 	@Override
 	public void onDestroy()
 	{
 		Core.print("Goodbye Service!");
 		super.onDestroy();
 	}
 
 	@Override
 	public IBinder onBind(Intent arg0) {
 		return null;
 	}
 
 	private static String pretty(int t, boolean celc)
 	{
 		return t+""+(celc ? "°" : "°");
 	}
 	
 	private static String unit(boolean celc)
 	{
 		return celc ? "celsius" : "fahrenheit";
 	}
 }
