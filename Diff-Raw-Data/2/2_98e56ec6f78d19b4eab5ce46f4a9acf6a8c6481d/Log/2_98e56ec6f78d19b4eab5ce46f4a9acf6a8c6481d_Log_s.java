 /**
  * Copyright (c) 2006-2011 Floggy Open Source Group. All rights reserved.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.floggy.persistence.android;
 
 /**
 * DOCUMENT ME!
 *
 * @author <a href="mailto:thiago.moreira@floggy.org">Thiago Moreira</a>
 * @version $Revision$
  */
 public class Log {
 	public static final int VERBOSE = 2;
 	public static final int DEBUG = 3;
 	public static final int INFO = 4;
 	public static final int WARN = 5;
 	public static final int ERROR = 6;
 	public static final int ASSERT = 7;
 	private static boolean isAndroidEnvironment;
 
 	static {
		isAndroidEnvironment = false;
 	}
 
 	/**
 	* DOCUMENT ME!
 	*
 	* @param tag DOCUMENT ME!
 	* @param msg DOCUMENT ME!
 	*
 	* @return DOCUMENT ME!
 	*/
 	public static int d(String tag, String msg) {
 		return log(DEBUG, tag, msg);
 	}
 
 	/**
 	* DOCUMENT ME!
 	*
 	* @param tag DOCUMENT ME!
 	* @param msg DOCUMENT ME!
 	* @param tr DOCUMENT ME!
 	*
 	* @return DOCUMENT ME!
 	*/
 	public static int d(String tag, String msg, Throwable tr) {
 		return log(DEBUG, tag, msg, tr);
 	}
 
 	/**
 	* DOCUMENT ME!
 	*
 	* @param tag DOCUMENT ME!
 	* @param msg DOCUMENT ME!
 	*
 	* @return DOCUMENT ME!
 	*/
 	public static int e(String tag, String msg) {
 		return log(ERROR, tag, msg);
 	}
 
 	/**
 	* DOCUMENT ME!
 	*
 	* @param tag DOCUMENT ME!
 	* @param msg DOCUMENT ME!
 	* @param tr DOCUMENT ME!
 	*
 	* @return DOCUMENT ME!
 	*/
 	public static int e(String tag, String msg, Throwable tr) {
 		return log(ERROR, tag, msg, tr);
 	}
 
 	/**
 	* DOCUMENT ME!
 	*
 	* @param tag DOCUMENT ME!
 	* @param msg DOCUMENT ME!
 	*
 	* @return DOCUMENT ME!
 	*/
 	public static int i(String tag, String msg) {
 		return log(INFO, tag, msg);
 	}
 
 	/**
 	* DOCUMENT ME!
 	*
 	* @param tag DOCUMENT ME!
 	* @param msg DOCUMENT ME!
 	* @param tr DOCUMENT ME!
 	*
 	* @return DOCUMENT ME!
 	*/
 	public static int i(String tag, String msg, Throwable tr) {
 		return log(INFO, tag, msg, tr);
 	}
 
 	/**
 	* DOCUMENT ME!
 	*
 	* @param tag DOCUMENT ME!
 	* @param level DOCUMENT ME!
 	*
 	* @return DOCUMENT ME!
 	*/
 	public static boolean isLoggable(String tag, int level) {
 		if (isAndroidEnvironment) {
 			return android.util.Log.isLoggable(tag, level);
 		} else {
 			return true;
 		}
 	}
 
 	/**
 	* DOCUMENT ME!
 	*
 	* @param tag DOCUMENT ME!
 	* @param msg DOCUMENT ME!
 	*
 	* @return DOCUMENT ME!
 	*/
 	public static int v(String tag, String msg) {
 		return log(VERBOSE, tag, msg);
 	}
 
 	/**
 	* DOCUMENT ME!
 	*
 	* @param tag DOCUMENT ME!
 	* @param msg DOCUMENT ME!
 	* @param tr DOCUMENT ME!
 	*
 	* @return DOCUMENT ME!
 	*/
 	public static int v(String tag, String msg, Throwable tr) {
 		return log(VERBOSE, tag, msg, tr);
 	}
 
 	/**
 	* DOCUMENT ME!
 	*
 	* @param tag DOCUMENT ME!
 	* @param msg DOCUMENT ME!
 	*
 	* @return DOCUMENT ME!
 	*/
 	public static int w(String tag, String msg) {
 		return log(WARN, tag, msg);
 	}
 
 	/**
 	* DOCUMENT ME!
 	*
 	* @param tag DOCUMENT ME!
 	* @param msg DOCUMENT ME!
 	* @param tr DOCUMENT ME!
 	*
 	* @return DOCUMENT ME!
 	*/
 	public static int w(String tag, String msg, Throwable tr) {
 		return log(WARN, tag, msg, tr);
 	}
 
 	private static int log(int priority, String tag, String msg) {
 		if (isAndroidEnvironment) {
 			switch (priority) {
 			case VERBOSE:
 				return android.util.Log.v(tag, msg);
 
 			case DEBUG:
 				return android.util.Log.d(tag, msg);
 
 			case INFO:
 				return android.util.Log.i(tag, msg);
 
 			case WARN:
 				return android.util.Log.w(tag, msg);
 
 			case ERROR:
 				return android.util.Log.e(tag, msg);
 
 			default:
 				return 0;
 			}
 		} else {
 			System.out.println(tag + ": " + msg);
 
 			return 0;
 		}
 	}
 
 	private static int log(int priority, String tag, String msg, Throwable tr) {
 		if (isAndroidEnvironment) {
 			switch (priority) {
 			case VERBOSE:
 				return android.util.Log.v(tag, msg);
 
 			case DEBUG:
 				return android.util.Log.d(tag, msg);
 
 			case INFO:
 				return android.util.Log.i(tag, msg);
 
 			case WARN:
 				return android.util.Log.w(tag, msg);
 
 			case ERROR:
 				return android.util.Log.e(tag, msg);
 
 			default:
 				return 0;
 			}
 		} else {
 			System.out.println(tag + ": " + msg);
 
 			return 0;
 		}
 	}
 }
