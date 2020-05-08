 /**
  * Copyright 2010 Eric Taix
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
  * 
  */
 package com.bigpupdev.synodroid.protocol;
 
 import android.os.Message;
 
 /**
  * A interface which define a method to handle response from server
  * 
  * @author Eric Taix (eric.taix at gmail.com)
  */
 public interface ResponseHandler {
 
 	// Specify an update operation occurs
 	public static final int MSG_OPERATION_PENDING = 10000;
 	// Specify an update operation is finished
 	public static final int MSG_OPERATION_DONE = 10001;
 	// Specify an error has to be shown
 	public static final int MSG_ERROR = 10003;
 	// Specify the obj contains task
 	public static final int MSG_TASKS_UPDATED = 10004;
 	// Connecting to the server
 	public static final int MSG_CONNECTING = 10005;
 	// Connected to the server
 	public static final int MSG_CONNECTED = 10006;
 	// Task's details retrieved
 	public static final int MSG_DETAILS_RETRIEVED = 10007;
 	// Task's files retrieved
 	public static final int MSG_DETAILS_FILES_RETRIEVED = 10008;
 	// Show a task details
 	public static final int MSG_SHOW_DETAILS = 10009;
 	// Shared directories retrieved
 	public static final int MSG_SHARED_DIRECTORIES_RETRIEVED = 100010;
 	// Original file retrieved
 	public static final int MSG_ORIGINAL_FILE_RETRIEVED = 100011;
 
 	public static final int MSG_PROPERTIES_RECEIVED = 100012;
 
 	public static final int MSG_SHARED_NOT_SET = 100013;
 	
 	public static final int MSG_TASK_DL_WAIT = 100014;
 	
 	public static final int MSG_SE_LIST_RETRIEVED = 100015;
 	
	public static final int MSG_OTP_REQUESTED = 100016;
 	
 	public static final int MSG_INFO = 10020;
 	public static final int MSG_ALERT = 10021;
 	public static final int MSG_ERR = 10022;
 	public static final int MSG_CONFIRM = 10023;
 	public static final int MSG_CONNECT_WITH_ACTION = 10024;
 	
 	/**
 	 * Handle the response. BE CAREFUL this method will NOT be called from the main thread. So don't try to interact with the UI. Prefer to use SynodroidActivity subclass and then implements your code in handleMessage method.
 	 * 
 	 * @param msgP
 	 */
 	public void handleReponse(Message msgP);
 
 	/**
 	 * Return a String according to a ressource id and the current locale
 	 * 
 	 * @param idP
 	 * @return
 	 */
 	public String getString(int idP);
 
 	/**
 	 * Return a String according to a ressource id and the current locale and also replace parameters
 	 * 
 	 * @param idP
 	 * @param paramsP
 	 * @return
 	 */
 	public String getString(int idP, Object... paramsP);
 }
