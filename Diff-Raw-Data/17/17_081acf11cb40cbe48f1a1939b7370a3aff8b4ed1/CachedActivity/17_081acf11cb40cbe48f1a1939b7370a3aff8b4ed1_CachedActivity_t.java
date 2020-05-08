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
 package com.wise;
 
 import java.io.File;
 import java.io.IOException;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.util.Log;
 
 import android.net.http.HttpResponseCache;
 
 /**
 * an Activity that use the same httpResponseCache for saving the feed 
 * inside the cache
 * 
 * 
 * @author Giovanni Visentini
  */
 public class CachedActivity extends Activity {
 	
 	private final long httpCacheSize =2 * 1024*1024; // 2mb
 	private final static String TAG = "CachedActivity";
 
 	
 	/**
 	 * @see android.app.Activity#onCreate(android.os.Bundle)
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		if(HttpResponseCache.getInstalled()==null)
 			setHttpCache();
 	}
 
 	protected void setHttpCache(){
 		try {
            File httpCacheDir = new File(getCacheDir(), "http");
            HttpResponseCache.install(httpCacheDir, httpCacheSize);
 		}catch (IOException e) {
            Log.i(TAG, "HTTP response cache installation failed:" + e);
        }
 	}
 	
 	protected void onStop(){
 		super.onStop();
 		
 	    HttpResponseCache cache = HttpResponseCache.getInstalled();
         if (cache != null) {
             cache.flush();
         }//if
         
 
 		
 	}
 }
