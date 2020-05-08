 package com.sobinary.app;
 
 import java.io.FileOutputStream;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import com.sobinary.base.Core;
 import com.sobinary.base.IO;
 import com.sobinary.clockplus.R;
 import com.sobinary.work.CalService;
 import com.sobinary.work.MinuteService;
 import com.sobinary.work.WeatherService;
 
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.appwidget.AppWidgetManager;
 import android.appwidget.AppWidgetProvider;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Color;
 import android.preference.PreferenceManager;
 import android.widget.RemoteViews;
 
 public class BigInfoProvider extends AppWidgetProvider  
 {
 	@Override
 	public void onEnabled(Context cont)
 	{
 		super.onEnabled(cont);
 	}
 	
 	@Override
 	public void onUpdate(Context cont, AppWidgetManager appMan, int[] ids)
 	{
 		super.onUpdate(cont, appMan, ids);
 		Core.print("Generic update");
 		beginTime(cont.getApplicationContext());
 	}
	
 	@Override
 	public void onDisabled(Context cont)
 	{
 		super.onDisabled(cont);
 		Core.print("Provider disabled");
 		endTime(cont);
 	}
 	
 	private static boolean isAlive(Context cont, Class<?>clazz)
 	{
 		ComponentName thisWidget = new ComponentName(cont.getApplicationContext(), clazz);
 		AppWidgetManager appMan = AppWidgetManager.getInstance(cont.getApplicationContext());
 		return (appMan.getAppWidgetIds(thisWidget).length > 0) ? true : false;
 	}
 
 	public static List<Class<?>>getLivingProviders(Context cont)
 	{
 		ArrayList<Class<?>>living = new ArrayList<Class<?>>();
 		if(isAlive(cont, BigInfoProvider.class)) living.add(BigInfoProvider.class);
 		if(isAlive(cont, BigClockProvider.class)) living.add(BigClockProvider.class);
 		if(isAlive(cont, SmlInfoProvider.class)) living.add(SmlInfoProvider.class);
 		return living;
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	/*******************************WIDGET STATE API***********************************/
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 
 	
 	public static void setGlobalDims(Context cont)
 	{
 		float density = cont.getResources().getDisplayMetrics().density;
 		prefs(cont).edit().putFloat("sdens", density).commit();
 		Core.print("Density: " + density);
 	}
 	
 	private static int[] viewIds() 
 	{
 		return new int[]
 		{
 			R.id.line0, 
 			R.id.line11, R.id.line12, 
 			R.id.line21,R.id.line22, 
 			R.id.line31, R.id.line32
 		}; 
 	}
 
 	public static void beginTime(Context cont)
 	{
 		setGlobalDims(cont);
 		
 		AlarmManager alman = (AlarmManager) cont.getSystemService(Context.ALARM_SERVICE);
 		Intent timeService = new Intent(cont, MinuteService.class);
 		PendingIntent timePending = PendingIntent.getService(cont, 0, timeService, 0);
 
 		int rem = 60 - Calendar.getInstance().get(Calendar.SECOND);
 		alman.setRepeating(AlarmManager.RTC, System.currentTimeMillis() + rem * 1000, 60000, timePending);
 		
 		if(rem > 3)
 		{
 			MinuteService.tic(cont);
 			Core.print("Time started ahead of first tic");
 		}
 		Core.print("Time service begun");
 	}
 	
 	public static void endTime(Context cont)
 	{
 		if(getLivingProviders(cont).size() > 0) return;
 		
 		Core.print("Killing minute service");
 		AlarmManager alman = (AlarmManager) cont.getSystemService(Context.ALARM_SERVICE);
 		
 		Intent timeService = new Intent(cont, MinuteService.class);
 		PendingIntent timePending = PendingIntent.getService(cont, 0, timeService, 0);
 		alman.cancel(timePending);
 	}
 	
 	public static void setTextLine(Context cont, int line, String left, String right)
 	{
 		RemoteViews remote = resetRem(cont, true);
 		ComponentName thisWidget = new ComponentName(cont, BigInfoProvider.class);
 		AppWidgetManager appMan = AppWidgetManager.getInstance(cont);
 		
 		int ind = lineToInd(line);
 		int []viewIds = viewIds();
 		int color = Color.parseColor((prefs(cont).getString("macol", "#ffffffff")));
 		if(ind == 0)
 		{
 			remote.setTextViewText(viewIds[0], left);
 			remote.setTextColor(viewIds[0], color);
 			save(cont, left, 0);
 		}
 		else
 		{
 			remote.setTextViewText(viewIds[ind], left+"/");
 			remote.setTextViewText(viewIds[ind+1], right);
 			
 			remote.setTextColor(viewIds[ind], color);
 			remote.setTextColor(viewIds[ind+1], color);
 			
 			save(cont, left+"/", ind);
 			save(cont, right, ind+1);
 		}
 		appMan.updateAppWidget(thisWidget, remote);
 	}
 	
 	private static int lineToInd(int line)
 	{
 		switch(line)
 		{
 			case 0: return 0;
 			case 1: return 1;
 			case 2: return 3;
 			case 3: return 5;
 			default: return 0;
 		}
 	}
 	
 	public static void setImage(Context cont, Bitmap bmp, int id)
 	{
 		saveImage(cont, bmp);
 		RemoteViews remote = resetRem(cont, false);
 		
 		ComponentName thisWidget = new ComponentName(cont, BigInfoProvider.class);
 		AppWidgetManager appMan = AppWidgetManager.getInstance(cont);
 		remote.setImageViewBitmap(id, bmp);
 		appMan.updateAppWidget(thisWidget, remote);  
 	}
 	
 	private static SharedPreferences prefs(Context cont)
 	{
 		return PreferenceManager.getDefaultSharedPreferences(cont);
 	}  
 	
 	private static RemoteViews resetRem(Context cont, boolean img)
 	{
 		RemoteViews remoteV = new RemoteViews( cont.getPackageName(), R.layout.main_phone );
 		int color = Color.parseColor(prefs(cont).getString("macol", "#ffffffff"));
 		String[]old = load(cont);
 
 		if(img)
 		{
 			Bitmap oldI = loadImage(cont.getApplicationContext());
 			if(oldI != null)remoteV.setImageViewBitmap(R.id.clockimg, oldI);
 		}
 
 		int []viewIds = viewIds();
 		for(int i=0; i < viewIds.length; i++) 
 		{
 			remoteV.setTextColor(viewIds[i], color);
 			remoteV.setTextViewText(viewIds[i], old[i]);
 		}
   
 		Intent prefAct = new Intent(cont, PreferencesActivity.class);
 		PendingIntent prefActP = PendingIntent.getActivity( cont, 0, prefAct, 0); 
 
 		Intent weatherService = new Intent(cont, WeatherService.class);
 		weatherService.putExtra(WeatherService.REQ_TYPE, WeatherService.GET_NOW);
 		PendingIntent weatherPending = PendingIntent.getService(cont, 0, weatherService, WeatherService.GET_NOW);
 
 		Intent weatherService2 = new Intent(cont, WeatherService.class);
 		weatherService2.putExtra(WeatherService.REQ_TYPE, WeatherService.GET_OUTLOOK);
 		PendingIntent weatherPending2 = PendingIntent.getService(cont, 0, weatherService2, WeatherService.GET_OUTLOOK);
 		   
 		Intent calServ = new Intent(cont, CalService.class);
 		PendingIntent calServP = PendingIntent.getService( cont, 0, calServ, 0); 
 
 		String lChoice = prefs(cont).getString("launchoice", "com.android.vending");
 		Intent launcher = cont.getPackageManager().getLaunchIntentForPackage(lChoice);
 		PendingIntent launcherP = PendingIntent.getActivity(cont, 0, launcher, 0);
 
 		remoteV.setOnClickPendingIntent(R.id.line0, prefActP);
 		remoteV.setOnClickPendingIntent(R.id.line11, weatherPending);
 		remoteV.setOnClickPendingIntent(R.id.line21, weatherPending2);
 		remoteV.setOnClickPendingIntent(R.id.line31, calServP);
 		remoteV.setOnClickPendingIntent(R.id.clockimg, launcherP);
 		
 		return remoteV;
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	/***********************************PERSISTENT STATE MGMT*****************************************/
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	private static String[] load(Context cont)
 	{
 		String[]old = (String[])IO.load("savedtext", cont.getApplicationContext());
 		if(old == null) 
 		{
 			old = new String[]
 			{
 					"Preferences",
 					"Weather/","Forecast",
 					"Rain/", "Warnings",
 					"Calendar/", "Reminders"
 			};
 		}
 		return old;
 	}
 
 	private static void save(Context cont, String str, int pos)
 	{
 		String[]old = load(cont);
 		old[pos] = str;
 		IO.save("savedtext", old, cont);
 	}
 	
 	private static Bitmap loadImage(Context cont)
 	{
 		try 
 		{
 			return BitmapFactory.decodeStream(cont.openFileInput("clkimgsob"));
 		} 
 		catch (Exception e) 
 		{
 			Core.print("Err loading bmp: " + e.getMessage());
 			return null;
 		}
 		
 	}
 	
 	private static void saveImage(Context cont, Bitmap bmp)
 	{
 		try
 		{
 			FileOutputStream out = cont.openFileOutput("clkimgsob", Context.MODE_PRIVATE);
 			bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
 		}
 		catch(Exception e)
 		{
 			Core.print("Err saving bmp: " + e.getMessage());
 		}
 	}
 }
