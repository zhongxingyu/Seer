 /**
  *	Copyright 2012 Norio bvba
  *
  *	This program is free software: you can redistribute it and/or modify
  *	it under the terms of the GNU General Public License as published by
  *	the Free Software Foundation, either version 3 of the License, or
  *	(at your option) any later version.
  *	
  *	This program is distributed in the hope that it will be useful,
  *	but WITHOUT ANY WARRANTY; without even the implied warranty of
  *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *	GNU General Public License for more details.
  *	
  *	You should have received a copy of the GNU General Public License
  *	along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package be.norio.twunch.android.util;
 
 import android.app.Application;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.preference.PreferenceManager;
 import android.util.Log;
 
 public class PrefsUtils {
 
 	private static final String TAG = PrefsUtils.class.getSimpleName();
 	private static final boolean LOGV = true;
 	private static final boolean LOGD = false;
 
 	private static final int VER_LEGACY = 0;
 	private static final int VER_LAUNCH = 1;
 
 	private static final int PREFS_VERSION = VER_LAUNCH;
 
 	private static final String KEY_PREFS_VERSION = "prefs_version";
 
 	private static boolean APPLY_AVAILABLE = false;
 
 	// Keys
 	public static final String KEY_LAST_UPDATE = "last_update";
 	public static final String KEY_LAST_TAB = "last_tab";
 	private static final String KEY_LAST_RUN_VERSION = "last_run_version";
 
 	// Default values
 	public static final long DEFAULT_LAST_UPDATE = 0;
 	public static final int DEFAULT_LAST_TAB = 0;
 
 	private static Context CONTEXT;
 
 	static {
 		try {
 			SharedPreferences.Editor.class.getMethod("apply", new Class[0]);
 			APPLY_AVAILABLE = true;
 		} catch (NoSuchMethodException e) {
 			APPLY_AVAILABLE = false;
 		}
 	}
 
 	private PrefsUtils() {
 	}
 
 	public static void apply(SharedPreferences.Editor editor) {
 		if (APPLY_AVAILABLE) {
 			if (LOGD)
 				Log.v(TAG, "Using apply");
 			editor.apply();
 		} else {
 			if (LOGD)
 				Log.v(TAG, "Using commit");
 			editor.commit();
 		}
 	}
 
 	private static SharedPreferences getPrefs() {
 		return PreferenceManager.getDefaultSharedPreferences(CONTEXT);
 	}
 
 	public static void initialize(Application application) {
 		CONTEXT = application;
 
 		int version = getPrefs().getInt(KEY_PREFS_VERSION, VER_LEGACY);
 
 		switch (version) {
 		case VER_LEGACY:
 			if (LOGV)
 				Log.v(TAG, "Upgrading settings from version " + version + " to version " + VER_LAUNCH);
 			version = VER_LAUNCH;
 		}
 
 		PrefsUtils.apply(getPrefs().edit().putInt(KEY_PREFS_VERSION, version));
 	}
 
 	private static boolean contains(String key) {
 		return getPrefs().contains(key);
 	}
 
 	public static long getLastUpdate() {
 		return getPrefs().getLong(KEY_LAST_UPDATE, DEFAULT_LAST_UPDATE);
 	}
 
 	public static void setLastUpdate(long value) {
 		PrefsUtils.apply(getPrefs().edit().putLong(KEY_LAST_UPDATE, value));
 	}
 
 	public static int getLastTab() {
 		return getPrefs().getInt(KEY_LAST_TAB, DEFAULT_LAST_TAB);
 	}
 
 	public static void setLastTab(int value) {
 		PrefsUtils.apply(getPrefs().edit().putInt(KEY_LAST_TAB, value));
 	}
 
 	public static int getLastRunVersion() {
 		return getPrefs().getInt(KEY_LAST_RUN_VERSION, 0);
 	}
 
 	public static void setLastRunVersion(int value) {
 		PrefsUtils.apply(getPrefs().edit().putInt(KEY_LAST_RUN_VERSION, value));
 	}
 
 }
