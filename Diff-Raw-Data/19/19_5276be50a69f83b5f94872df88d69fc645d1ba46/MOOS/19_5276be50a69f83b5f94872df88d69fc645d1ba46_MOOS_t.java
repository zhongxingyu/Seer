 /*
  * Copyright (C) 2009 The Android Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.robots.MOOS;
 
 import android.app.Activity;
 import android.widget.TextView;
 import android.os.Bundle;
 import android.util.Log;
 import java.lang.InterruptedException;
 
 import com.robots.MOOS.JMOOSCommObject;
 import com.robots.MOOS.JMOOSMsg;
 
 import java.net.*;
 import java.nio.*;
 import java.io.*;
 
 public class MOOS extends Activity
 {
 	protected JMOOSCommClient 	jMOOS;
 
 	public static native long createMessage();
 	public static native ByteBuffer serializeMessage( long ptr );
 
 	private static final String TAG = "MyActivity";
 
 	/** Called when the activity is first created. */
 	@Override
 
 		public void onCreate(Bundle savedInstanceState)
 		{
 			super.onCreate(savedInstanceState);
 
             jMOOS = new JMOOSCommClient();
 
             jMOOS.Run( "solinus.robots.ox.ac.uk", 9000 );          
 
 		}
 
 
 	static 
 	{
 		System.loadLibrary("MOOS");
 	}
   
 }
