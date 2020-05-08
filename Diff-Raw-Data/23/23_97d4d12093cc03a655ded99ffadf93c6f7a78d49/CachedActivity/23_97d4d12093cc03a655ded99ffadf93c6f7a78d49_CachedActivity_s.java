 /*
  *
  * 	This file is part of ASage.
  *
  *    ASage is free software: you can redistribute it and/or modify
  *    it under the terms of the GNU General Public License as published by
  *    the Free Software Foundation, either version 3 of the License, or
  *    (at your option) any later version.
  *
  *    ASage is distributed in the hope that it will be useful,
  *    but WITHOUT ANY WARRANTY; without even the implied warranty of
  *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *    GNU General Public License for more details.
  *
  *   You should have received a copy of the GNU General Public License
  *   along with ASage.  If not, see <http://www.gnu.org/licenses/>.
  *
  *   Copyright 2012 wise 
  *
  *
  */
 
 /**
  CachedActivity.java
  */
 package com.wise.gui;
 
 import java.io.File;
 //import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.util.Log;
 
 //import android.net.http.HttpResponseCache;
 
 /**
  * an Activity that use the same httpResponseCache for saving the feed inside
  * the cache
  * 
  * 
  * @author Giovanni Visentini
  */
 public abstract class CachedActivity extends Activity {
 
 	private final long httpCacheSize = 2 * 1024 * 1024; // 2mb
 	private final static String TAG = "CachedActivity";
 
 	private final static String preICSCache = "com.integralblue.httpresponsecache.HttpResponseCache";
	private final static String postICSCache = "com.integralblue.httpresponsecache.HttpResponseCache";
 
 	private Class<?> HttpResponseCache;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		try {
			// we load the sistem class or the backport ones
 			if (android.os.Build.VERSION.SDK_INT > 
 					android.os.Build.VERSION_CODES.HONEYCOMB_MR2) {
 				HttpResponseCache = Class.forName(postICSCache);
 			} else {
 				HttpResponseCache = Class.forName(preICSCache);
 			}// if-else
 			Method getInstalled = HttpResponseCache.getMethod("getInstalled",
					new Class[] { HttpResponseCache });
 
 			if (getInstalled.invoke(null, (Object[]) null) == null) {
 				setHttpCache();
 			}// if
 		} catch (Exception e) {
 			Log.e(TAG, "Reflection Error: HttpResponseCache " + e);
 			Log.i(TAG, "Http Cache Disabled");
 		} // try-catch
 
 	}// onCreate
 
 	protected void setHttpCache() {
 		try {
 			Method install = HttpResponseCache.getMethod("install",
					new Class[] { File.class, Long.class });
 			try {
 				File httpCacheDir = new File(getCacheDir(), "http");
 				install.invoke(null,
 						new Object[] { httpCacheDir, httpCacheSize });
 			} catch (InvocationTargetException e) {
 				Log.i(TAG,
 						"HTTP response cache installation failed:"
 								+ e.getTargetException());
			}// try-catch
 		} catch (Exception e) {
 			Log.e(TAG, "Reflection Error: HttpResponseCache " + e);
 			Log.i(TAG, "Http Cache Disabled");
 		}// try-catch
 	}
 
 	protected void onStop() {
 		super.onStop();
 		try {
 			Method getInstalled = HttpResponseCache.getMethod("getInstalled",
					new Class[] { HttpResponseCache });
 
 			Object installedCache = getInstalled.invoke(null, (Object[]) null);
 			if (installedCache != null) {
 				Method flush = installedCache.getClass().getMethod("flush",
 						(Class[]) null);
 				flush.invoke(installedCache, (Object[]) null);
 			}// if
 		} catch (Exception e) {
 			Log.e(TAG, "Reflection Error: HttpResponseCache " + e);
 			Log.i(TAG, "Http Cache Deleting Error");
 		}// try-catch
 	}
 }
