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
 import android.view.*;
 import android.os.Bundle;
 import android.util.Log;
 import android.widget.FrameLayout;
 
 //MOOS
 import com.robots.MOOS.JMOOSCommObject;
 import com.robots.MOOS.JMOOSMsg;
 //MOOS View
 import com.robots.MOOS.MOOSView;
 
 //import java.net.*;
 //import java.nio.*;
 //import java.io.*;
 
 public class MOOS extends Activity
 {
 	protected JMOOSCommClient 	jMOOS;
 
 	private static final String TAG = "MyActivity";
 
 	/** Called when the activity is first created. */
 	    @Override
 		public void onCreate(Bundle savedInstanceState)
 		{
 			super.onCreate(savedInstanceState);
 
             //Set up view
             setContentView( R.layout.main );
             FrameLayout main  = (FrameLayout) findViewById( R.id.main_view );
             
             MOOSView v = new MOOSView( this, 50, 50, 25 );
             
             main.addView( v );
              
             main.setOnTouchListener(  v );
 
             //RUN THE MOOS
            jMOOS = new JMOOSCommClient();
            jMOOS.Run( "solinus.robots.ox.ac.uk", 9000 );          
 
 		}
 
 	    @Override
         protected void onStart()
         {
             super.onStart();
             System.out.println( "Running: " + System.currentTimeMillis() );
         }
         
 	    @Override
         protected void onPause() 
         {   
             super.onPause();
             System.out.println( "---------------CALLLING ONPAUSE" );
             //jMOOS.DisconnectFromServer();
         }
 
 	    @Override
         protected void onStop() 
         {
             super.onStop();
             System.out.println( "---------------CALLLING ONSTOP" );
             //jMOOS.DisconnectFromServer();
         }
 
 
 	static 
 	{
 		System.loadLibrary("MOOS");
 	}
   
 }
