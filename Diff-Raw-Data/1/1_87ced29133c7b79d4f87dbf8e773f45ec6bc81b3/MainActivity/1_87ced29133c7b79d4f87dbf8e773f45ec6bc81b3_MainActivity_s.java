 /*
  * Copyright (C) 2010 France Telecom
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
 
 package com.orange.memoplayer;
 
 import javax.microedition.lcdui.Canvas;
 import javax.microedition.lcdui.Command;
 import javax.microedition.lcdui.CommandListener;
 import javax.microedition.lcdui.Display;
 import javax.microedition.lcdui.Displayable;
 import javax.microedition.midlet.MIDlet;
 
 import android.app.Activity;
 import android.content.res.Configuration;
 import android.graphics.PixelFormat;
 import android.os.Bundle;
 import android.os.Debug;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.Window;
 import android.content.pm.ActivityInfo;
 
 
 public class MainActivity extends Activity {
    
 	private static final boolean trace = false;
 	private MIDlet midlet;
 	private View defaultView;
 	protected int bootMode = Common.BOOT_NORMAL;
 	
 	public MainActivity()
 	{
 		System.setProperty ("microedition.configuration","CLDC-1.1");
 		System.setProperty ("microedition.profiles", "MIDP-2.1");
 		System.setProperty ("microedition.platform", "android");
 		System.setProperty ("microedition.encoding", "UTF-8");
 		System.setProperty ("microedition.locale", java.util.Locale.getDefault().toString());
 		System.setProperty ("wireless.messaging.sms.smsc", ""); //TODO SMS-C is not avail in 1.0
 		System.setProperty ("wireless.messaging.mms.mmsc", ""); //TODO MMS-C is not avail in 1.0
 	}
 	
 	public void onConfigurationChanged (Configuration newConfig) {
 		super.onConfigurationChanged(newConfig);
 		Log.i("Activity", "onConfigurationChanged !");
 	}
 	
 	protected void onCreate(Bundle icicle) 
 	{
     	super.onCreate( icicle );
     	// Force fullscreen
     	this.requestWindowFeature(Window.FEATURE_NO_TITLE);
         //this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
         //		WindowManager.LayoutParams.FLAG_FULLSCREEN);
 
         // Mandatory for video playback
         getWindow().setFormat(PixelFormat.TRANSPARENT);
             	
     	// throw in a dummy window so we can get accurate screen sizes for full screen components prior to them 
     	// having been shown
     	// this is a nasty hack
     	/*TextView splash = new TextView (this);
     	splash.setLayoutParams(new LayoutParams (LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
     	splash.setText("Loading...");
     	//View splash = this.getLayoutInflater().inflate( R.layout.splash, null, false );
     	this.defaultView = splash;
     	setContentView( splash );*/
 
         // Fix orientation according to jad parameter
         setFixedOrientation();
     }
 
 	protected void setFixedOrientation() {
         if( midlet!=null ) {
             String androidOrientation = midlet.getAppProperty("MEMO-ANDROID-ORIENTATION");
             if( androidOrientation != null ) {
 	            if( androidOrientation.equals("portrait") ) {
 	                setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_PORTRAIT );
 		        } else if( androidOrientation.equals("landscape") ) {
 		            setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE );
 		        }
 	        }
         }
 	}
 	
     @Override
     protected void onDestroy()
     {
         try
         {
         	if( this.midlet != null )
         	{
         		this.midlet.doDestroyApp( true );
         		this.midlet = null;
         	}
         }
         catch( Exception ex )
         {
             throw new RuntimeException( "unable to destroy", ex );
         }
         // stop tracing
         if (trace) Debug.stopMethodTracing();
         //this.resources.getAssets().release();
         super.onDestroy();
         System.exit(0);
     }
     
     
 
     @Override
 	protected void onPause() 
     {
         try
         {
         	if( this.midlet != null )
         	{
         		this.midlet.doPauseApp();
         	}
         }
         catch( Exception ex )
         {
         	ex.printStackTrace();
             throw new RuntimeException( "unable to freeze app", ex );
         }
     	super.onPause();
 	}
 
     @Override
     protected void onResume()
     {
         super.onResume();
 
         if( this.midlet == null )
         {
 	    	/*Thread thread = new Thread() {
 	    		public void run(){
 	    			while( defaultView.getWidth() == 0 )
 	    			{
 		    			try
 		    			{
 		    				Thread.sleep( 500 );
 		    				System.out.println( "W:"+defaultView.getWidth()+",H:"+defaultView.getHeight()+",MW:"+defaultView.getMeasuredWidth()+",MH:"+defaultView.getMeasuredHeight() );
 		    			}catch( Exception ex )
 		    			{
 		    				ex.printStackTrace();
 		    			}
 	    			}*/
 	    			MIDlet midlet = Common.createMIDlet(MainActivity.this, bootMode);
 	    			MainActivity.this.midlet = midlet;
 	    	        try
 	    	        {
 	    	        	if( midlet != null )
 	    	        	{
 	    	        		midlet.doStartApp();
                             MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                 public void run() {
                                     MainActivity.this.setFixedOrientation();
                                 }
                             });
 	    	        	}
 	    	        }
 	    	        catch( Exception ex )
 	    	        {
 	    	        	ex.printStackTrace();
 	    	        }
 	    		/*}
 	    	};
 	    	thread.start();*/
         }
         else
         {
 	        try
 	        {
         		this.midlet.doStartApp();
 	        }
 	        catch( Exception ex )
 	        {
 	        	ex.printStackTrace();
 	        	throw new RuntimeException( "couldn't start MIDlet" );
 	        }
         }
 
     }
 
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		Displayable d = Display.getDisplay (midlet).getCurrent ();
         if (d != null) {
         	return d.onPrepareOptionsMenu(menu);
         }
         return false;
 	}
 	
 	public boolean onOptionsItemSelected(MenuItem item) {
 		Displayable d = Display.getDisplay (midlet).getCurrent ();
         if (d != null) {
         	return d.onOptionsItemSelected (item);
         }
         return super.onOptionsItemSelected(item);
 	}
 	
 	public boolean onKeyDown( int keyCode, KeyEvent event ) {
 		if (keyCode == KeyEvent.KEYCODE_BACK) {
 			Displayable d = Display.getDisplay (midlet).getCurrent ();
 			if (d != null) {
 				if (d instanceof Canvas) {
 					((Canvas)d).fakeKeyPressed (keyCode);
 					return true;
 				}
 				Command c = d.getBackCommand ();
 				CommandListener cl = d.getCommandListener ();
 				if (c != null && cl != null) {
 					cl.commandAction (c, d);
 					return true;
 				}
 			}
 		} else if (keyCode == KeyEvent.KEYCODE_MENU) {
 			Displayable d = Display.getDisplay (midlet).getCurrent ();
 			if (d != null && d instanceof Canvas) {
 				((Canvas)d).fakeKeyPressed (keyCode);
 				return true;
 			}
 		}
 		return super.onKeyDown (keyCode, event);
 	}
 	
 	public boolean onKeyUp(int keyCode, KeyEvent event) {
 		if (keyCode == KeyEvent.KEYCODE_BACK ||
 			keyCode == KeyEvent.KEYCODE_MENU) {
 			Displayable d = Display.getDisplay (midlet).getCurrent ();
 			if (d != null && d instanceof Canvas) {
 				((Canvas)d).fakeKeyReleased (keyCode);
 				return true;
 			}
 		}
 		return super.onKeyUp(keyCode, event);
 	}
 }
