 package com.yoharnu.newontv.android;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.PrintStream;
 import java.util.LinkedList;
 import java.util.Scanner;
 import java.util.concurrent.ExecutionException;
 
 import com.yoharnu.newontv.android.shows.Series;
 
 import android.app.Application;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.os.Environment;
 import android.preference.PreferenceManager;
 
 public class App extends Application {
 
 	public static final String MIRRORPATH = "http://thetvdb.com";
 	public static final String LANGUAGE = "en";
 	public static final String API_KEY = "768A3A72ACDABC4A";
 	protected static Context context;
 	public static SharedPreferences preferences;
 	public static LinkedList<Series> shows;
 	private static boolean mExternalStorageAvailable = false;
 	private static boolean mExternalStorageWriteable = false;
 
 	public void onCreate() {
 		super.onCreate();
 
 		context = getApplicationContext();
 		preferences = PreferenceManager.getDefaultSharedPreferences(context);
 		shows = new LinkedList<Series>();
 		checkPermissions();
 		load();
 	}
 
 	public static Context getContext() {
 		return context;
 	}
 
 	public static void add(String seriesId) {
 		Series temp = new Series(seriesId, Series.ID);
 		try {
 			if (temp.task != null)
 				temp.task.get();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
			return;
 		} catch (ExecutionException e) {
 			e.printStackTrace();
			return;
 		}
 		boolean present = false;
 		for (int i = 0; i < shows.size(); i++) {
 			if (shows.get(i).getSeriesId().equals(seriesId)) {
 				present = true;
 			}
 		}
 		if (!present) {
 			shows.add(temp);
 			save();
 		}
 	}
 
 	public static void add(Series series) {
 		boolean present = false;
 		for (int i = 0; i < shows.size(); i++) {
 			if (shows.get(i).getSeriesId().equals(series.getSeriesId())) {
 				present = true;
 			}
 		}
 		if (!present) {
 			shows.add(series);
 			save();
 		}
 	}
 
 	public static void save() {
 		try {
 			PrintStream out = new PrintStream(new File(context.getFilesDir(),
 					"shows"));
 			for (int i = 0; i < shows.size(); i++) {
 				out.println(shows.get(i).getSeriesId());
 			}
 		} catch (FileNotFoundException e) {
 		}
 	}
 
 	private static void load() {
 		try {
 			Scanner s = new Scanner(new File(context.getFilesDir(), "shows"));
 			shows.clear();
 			while (s.hasNextLine()) {
 				add(s.nextLine());
 			}
 			if (s != null) {
 				s.close();
 			}
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static void sort() {
 		for (int i = 0; i < App.shows.size(); i++) {
 			Series temp = App.shows.get(i);
 			int iHole = i;
 			while (iHole > 0
 					&& App.shows.get(iHole - 1).getSeriesName()
 							.compareTo(temp.getSeriesName()) > 0) {
 				App.shows.set(iHole, App.shows.get(iHole - 1));
 				iHole--;
 			}
 			App.shows.set(iHole, temp);
 		}
 	}
 
 	protected void checkPermissions() {
 		String state = Environment.getExternalStorageState();
 
 		if (Environment.MEDIA_MOUNTED.equals(state)) {
 			// We can read and write the media
 			mExternalStorageAvailable = true;
 			mExternalStorageWriteable = true;
 		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
 			// We can only read the media
 			mExternalStorageAvailable = true;
 			mExternalStorageWriteable = false;
 		} else {
 			// Something else is wrong. It may be one of many other states, but
 			// all we need
 			// to know is we can neither read nor write
 			mExternalStorageAvailable = false;
 			mExternalStorageWriteable = false;
 		}
 	}
 
 	public static boolean isExternalStorageWriteable() {
 		return mExternalStorageWriteable;
 	}
 
 	public static boolean isExternalStorageAvailable() {
 		return mExternalStorageAvailable;
 	}
 }
