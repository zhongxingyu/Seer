 /* 
  * Copyright 2011 Google Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.galactogolf;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.preference.PreferenceManager;
 
 /*
  * Used to access useer preferences by the Game logic
  */
 public class UserPreferences {
 
 	private static Context _currentContext;
 	private static Boolean _methodTracing = null;
 
 	public static void init(Context ctx) {
 		_currentContext = ctx;
 	}
 
 	public static boolean ShowFramesPerSecond() {
 		Boolean _showFPS = null;
 
 		if (_showFPS == null) {
 			SharedPreferences prefs = PreferenceManager
 					.getDefaultSharedPreferences(_currentContext);
 			_showFPS = prefs.getBoolean("show_fps_preference", false);
 		}
 		return _showFPS;
 	}
 
	public static boolean EditorEnabled = false;
 
 	public static boolean MethodTracingEnabled() {
 		return false;
 	}
 }
