 /*
  * Kernel Gestures Builder
  * Build Gestures definitions on android kernels that support gestures
  * Kernel feature developed by Tungstwenty
  * http://forum.xda-developers.com/showthread.php?t=1831254
  * 
  * Copyright (C) 2012  Guillermo Joandet
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 
 */
 
 package ar.com.nivel7.kernelgesturesbuilder;
 
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.Toast;
 import com.google.analytics.tracking.android.EasyTracker;
 
 
 public class KernelGesturesBuilder extends Activity {
 
 	private static MTView KernelGesturesMTView;
 	private SharedPreferences sharedPrefs;
 	String datapath = null;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
 				WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		
 		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
 		KernelGesturesMTView = new MTView(this);
 		KernelGesturesMTView.setGesturenumber(Integer.parseInt(sharedPrefs.getString("gesture_number", "1")));
 		KernelGesturesMTView.setGridcolumns(Integer.parseInt(sharedPrefs.getString("grid_columns", "3")));
 		KernelGesturesMTView.setGridrows(Integer.parseInt(sharedPrefs.getString("grid_rows", "5")));
 		setContentView(KernelGesturesMTView);
 		datapath=getApplicationContext().getFilesDir().getPath();
 		
 	}
 	
 	public static int getGesturenumber() {
 		return KernelGesturesMTView.getGesturenumber();
 	}
 	
 	@Override
 	  public void onStart() {
 	    super.onStart();
 	    EasyTracker.getInstance().activityStart(this); 
 	  }
 
 	  @Override
 	  public void onStop() {
 	    super.onStop();
 	    EasyTracker.getInstance().activityStop(this);
 	  }
 
 	@Override
 	public void onNewIntent(Intent intent) {
 		KernelGesturesMTView.setGesturenumber(Integer.parseInt(sharedPrefs.getString("gesture_number", "1")));
 		KernelGesturesMTView.setGridcolumns(Integer.parseInt(sharedPrefs.getString("grid_columns", "3")));
 		KernelGesturesMTView.setGridrows(Integer.parseInt(sharedPrefs.getString("grid_rows", "5")));
 		setContentView(KernelGesturesMTView);
 		KernelGesturesMTView.redrawGrid();
 	}
 	
 	  @Override public boolean onCreateOptionsMenu(Menu menu) {
 	  getMenuInflater().inflate(R.menu.activity_kernel_gestures_builder, menu);
 	  return true; }
 	 
 	
       @Override
 	  public boolean onOptionsItemSelected(MenuItem item)
 	  {
 	        switch (item.getItemId())
 	        {
 	 	    case R.id.menu_settings:
 	        	startActivity(new Intent(this, Settings.class ));
 	        	return true;
 	 	    case R.id.menu_resetgestures:
 	        	ResetGestures();
 	        	return true;
 	 	    case R.id.menu_resetactions:
 	        	ResetActions();
 	        	return true;
 	 	    case R.id.menu_loadgestures:
 	        	LoadGestures();
 	        	return true;
 	 	    case R.id.menu_installgestures:
 	        	InstallGestures();
 	        	return true;
 	 	    case R.id.menu_launchactivities:
 	 	    	startActivity(new Intent(this, LaunchActivities.class ));
 	 	    	return true;
 	 	    case R.id.menu_actions:
 	 	    	startActivity(new Intent(this, Actions.class ));
 	 	    	return true;
 	 	    case R.id.menu_testaction:
 	 	    	TestAction();
 	 	    	return true;
 	        default:
 	            return super.onOptionsItemSelected(item);
 	        }
 	   }
 
       public boolean InstallGestures() {
     		String FILENAME = "gesture_set.sh";
       		FileOutputStream fos;
 
       		try {
       			fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
       			fos.write(("#!/sbin/busybox sh\n" +
       					"\n" +
       					"\n" +
       					"#\n" +
       					"# Sample touch gesture actions by Tungstwenty - forum.xda-developers.com\n" +
       					"# Modded by GM , dorimanx and flint2.\n" +
       					"\n" +
       					"GESTURES_PATH=/data/gestures\n" +
       					"\n" +
       					"FILE_NAME=$0;\n" +
       					"\n" +
       					"# Load all gesture definitions, removing comments to reduce the total size\n" +
       					"cat $GESTURES_PATH/gesture-*.config | sed -e 's/#.*$//' > /sys/devices/virtual/misc/touch_gestures/gesture_patterns\n" +
       					"# Detect ICS or JB - bluetooth calls are different\ncase \"`getprop ro.build.version.release`\" in\n" +
       					"	4.2* ) is_jb=1;;\n" +
       					"	4.1* ) is_jb=1;;\n" +
       					"	* )    is_jb=0;;\n" +
       					"esac\n" +
       					"\n" +
       					"# Start loop listening for triggered gestures\n" +
       					"( while [ 1 ]\n" +
       					"do\n" +
       					"\n" +
       					"    GESTURE=`cat /sys/devices/virtual/misc/touch_gestures/wait_for_gesture`\n" +
       					"    		\n" +
       					"    # Launch the action script if it exists, not spawning a separate process\n" +
       					"    GESTURE_SCRIPT=\"$GESTURES_PATH/gesture-$GESTURE.sh\"\n" +
       					"    if [ -f $GESTURE_SCRIPT ]; then\n" +
       					"		\n" +
       					"    	log -p i -t $FILE_NAME \"*** GESTURE ***: gesture $GESTURE detected, executing $GESTURE_SCRIPT\";\n" +
       					"    	\n        . $GESTURE_SCRIPT $is_jb\n" +
       					"        \n" +
       					"                service call vibrator 2 i32 100 i32 0\n" +
       					"        \n" +
       					"    fi\n" +
       					"    \n" +
       					"    sleep 1\n" +
       					"   \n" +
       					"done ) > /dev/null 2>&1 &\n").getBytes());
       			fos.close();
       		} catch (FileNotFoundException e) {
       			e.printStackTrace();
       		} catch (IOException e) {
       			e.printStackTrace();
       		}
 
       		FILENAME = "install_gestures.sh";
       		try {
       			fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
       			fos.write(("#!/sbin/busybox sh\n" +
       					"if [ ! -d /data/gestures ]; then\n" +
       					"   mkdir /data/gestures\n" +
       					"fi\n" +
       					"cp "+datapath+"/* /data/gestures\n" +
       					"chmod 755 /data/gestures/*.sh\n" +
       					"chmod 644 /data/gestures/*.config\n" +
       					"if [ -f /data/gesture_set.sh ]; then\n" +
       					"   cat /data/gestures/gesture_set.sh > /data/gesture_set.sh\n" +
       					"   chmod 755 /data/gesture_set.sh\n" +
       					"   if [ -f /system/etc/init.d/S50GestureActions ]; then\n" +
       					"      mount -o remount,rw /system\n" +
       					"      cat /data/gestures/gesture_set.sh > /system/etc/init.d/S50GestureActions\n" +
       					"      chmod 755 /system/etc/init.d/S50GestureActions\n" +
       					"   fi\n" +
       					"else\n" +
       					"   mount -o remount,rw /system\n" +
       					"   cat /data/gestures/gesture_set.sh > /system/etc/init.d/S50GestureActions\n" +
       					"   chmod 755 /system/etc/init.d/S50GestureActions\n" +
       					"   kill `ps |grep S50Gesture|cut -d ' ' -f 2 ` 2> /dev/null \n"+
       					"	busybox nohup /system/etc/init.d/S50GestureActions  > /dev/null 2> /dev/null \n"+
       					"fi\n" +
       					"rm /data/gestures/gesture_set.sh \n" +
       					"rm /data/gestures/install_gestures.sh \n" +
       					"\n").getBytes());
       			fos.close();
       		} catch (FileNotFoundException e) {
       			e.printStackTrace();
       		} catch (IOException e) {
       			e.printStackTrace();
       		}
 
       		CharSequence toastText;
       		if (Utils.canRunRootCommandsInThread()) {
       			Utils.executeRootCommandInThread
     			("chmod 755 "+datapath+"/install_gestures.sh\n"+datapath+"/install_gestures.sh");
       			toastText = getString(R.string.toastInstallGesturesOK);
       			Toast.makeText(this , toastText , Toast.LENGTH_SHORT).show();
       		} else {
       			toastText = getString(R.string.toastInstallGesturesERR);
       			Toast.makeText(this , toastText, Toast.LENGTH_SHORT).show();
       		}
       		
      
     	  return true;
       }
       
       public boolean LoadGestures() {
   
       		CharSequence toastText;
       		if (Utils.canRunRootCommandsInThread()) {
       			Utils.executeRootCommandInThread
     			("cp /data/gestures/* "+datapath+" \n"+"chmod 666 "+datapath+"/*");
       			toastText = getString(R.string.toastLoadGesturesOK);
       			Toast.makeText(this , toastText , Toast.LENGTH_SHORT).show();
       		} else {
       			toastText = getString(R.string.toastLoadGesturesERR);
       			Toast.makeText(this , toastText, Toast.LENGTH_SHORT).show();
      			
       		}
       		
      
     	  return true;
       }
       
      
      public boolean ResetGestures() {
   		
   		String FILENAME = "gesture-1.config";
   		FileOutputStream fos;
 
   		try {
   			fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
   			fos.write(getString(R.string.gesture_1).getBytes());
   			fos.close();
   		} catch (FileNotFoundException e) {
   			e.printStackTrace();
   		} catch (IOException e) {
   			e.printStackTrace();
   		}
 
   		FILENAME = "gesture-2.config";
   		try {
   			fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
   			fos.write(getString(R.string.gesture_2).getBytes());
   			fos.close();
   		} catch (FileNotFoundException e) {
   			e.printStackTrace();
   		} catch (IOException e) {
   			e.printStackTrace();
   		}
 
   		FILENAME = "gesture-3.config";
   		try {
   			fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
   			fos.write(getString(R.string.gesture_3).getBytes());
   			fos.close();
   		} catch (FileNotFoundException e) {
   			e.printStackTrace();
   		} catch (IOException e) {
   			e.printStackTrace();
   		}
 
   		FILENAME = "gesture-4.config";
   		try {
   			fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
   			fos.write(getString(R.string.gesture_4).getBytes());
   			fos.close();
   		} catch (FileNotFoundException e) {
   			e.printStackTrace();
   		} catch (IOException e) {
   			e.printStackTrace();
   		}
 
   		FILENAME = "gesture-5.config";
   		try {
   			fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
   			fos.write(getString(R.string.gesture_5).getBytes());
   			fos.close();
   		} catch (FileNotFoundException e) {
   			e.printStackTrace();
   		} catch (IOException e) {
   			e.printStackTrace();
   		}
 
   		FILENAME = "gesture-6.config";
   		try {
   			fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
   			fos.write(getString(R.string.gesture_6).getBytes());
   			fos.close();
   		} catch (FileNotFoundException e) {
   			e.printStackTrace();
   		} catch (IOException e) {
   			e.printStackTrace();
   		}
 
   		FILENAME = "gesture-7.config";
   		try {
   			fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
   			fos.write(getString(R.string.gesture_7).getBytes());
   			fos.close();
   		} catch (FileNotFoundException e) {
   			e.printStackTrace();
   		} catch (IOException e) {
   			e.printStackTrace();
   		}
 
   		FILENAME = "gesture-8.config";
   		try {
   			fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
   			fos.write(getString(R.string.gesture_8).getBytes());
   			fos.close();
   		} catch (FileNotFoundException e) {
   			e.printStackTrace();
   		} catch (IOException e) {
   			e.printStackTrace();
   		}
 
   		FILENAME = "gesture-9.config";
   		try {
   			fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
   			fos.write(getString(R.string.gesture_9).getBytes());
   			fos.close();
   		} catch (FileNotFoundException e) {
   			e.printStackTrace();
   		} catch (IOException e) {
   			e.printStackTrace();
   		}
 
   		FILENAME = "gesture-10.config";
   		try {
   			fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
   			fos.write(getString(R.string.gesture_10).getBytes());
   			fos.close();
   		} catch (FileNotFoundException e) {
   			e.printStackTrace();
   		} catch (IOException e) {
   			e.printStackTrace();
   		}
 
   		FILENAME = "gesture-11.config";
   		try {
   			fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
   			fos.write(getString(R.string.gesture_11).getBytes());
   			fos.close();
   		} catch (FileNotFoundException e) {
   			e.printStackTrace();
   		} catch (IOException e) {
   			e.printStackTrace();
   		}
 
   		CharSequence toastText = getString(R.string.toastResetGesturesOK);
   		Toast.makeText(this , toastText, Toast.LENGTH_SHORT).show();
   		
   		return true;
   	}
  
      
     public boolean ResetActions() {
  		
  		String FILENAME = "gesture-1.sh";
  		FileOutputStream fos;
 
  		try {
  			fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
   			fos.write(getString(R.string.action_1).getBytes());
  			fos.close();
  		} catch (FileNotFoundException e) {
  			e.printStackTrace();
  		} catch (IOException e) {
  			e.printStackTrace();
  		}
 
  		FILENAME = "gesture-2.sh";
  		try {
  			fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
   			fos.write(getString(R.string.action_2).getBytes());
  			fos.close();
  		} catch (FileNotFoundException e) {
  			e.printStackTrace();
  		} catch (IOException e) {
  			e.printStackTrace();
  		}
 
  		FILENAME = "gesture-3.sh";
  		try {
  			fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
   			fos.write(getString(R.string.action_3).getBytes());
  			fos.close();
  		} catch (FileNotFoundException e) {
  			e.printStackTrace();
  		} catch (IOException e) {
  			e.printStackTrace();
  		}
 
  		FILENAME = "gesture-4.sh";
  		try {
  			fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
   			fos.write(getString(R.string.action_4).getBytes());
  			fos.close();
  		} catch (FileNotFoundException e) {
  			e.printStackTrace();
  		} catch (IOException e) {
  			e.printStackTrace();
  		}
 
  		FILENAME = "gesture-5.sh";
  		try {
  			fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
   			fos.write(getString(R.string.action_5).getBytes());
  			fos.close();
  		} catch (FileNotFoundException e) {
  			e.printStackTrace();
  		} catch (IOException e) {
  			e.printStackTrace();
  		}
 
  		FILENAME = "gesture-6.sh";
  		try {
  			fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
   			fos.write(getString(R.string.action_6).getBytes());
  			fos.close();
  		} catch (FileNotFoundException e) {
  			e.printStackTrace();
  		} catch (IOException e) {
  			e.printStackTrace();
  		}
 
  		FILENAME = "gesture-7.sh";
  		try {
  			fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
   			fos.write(getString(R.string.action_7).getBytes());
  			fos.close();
  		} catch (FileNotFoundException e) {
  			e.printStackTrace();
  		} catch (IOException e) {
  			e.printStackTrace();
  		}
 
  		FILENAME = "gesture-8.sh";
  		try {
  			fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
   			fos.write(getString(R.string.action_8).getBytes());
  			fos.close();
  		} catch (FileNotFoundException e) {
  			e.printStackTrace();
  		} catch (IOException e) {
  			e.printStackTrace();
  		}
 
  		FILENAME = "gesture-9.sh";
  		try {
  			fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
   			fos.write(getString(R.string.action_9).getBytes());
  			fos.close();
  		} catch (FileNotFoundException e) {
  			e.printStackTrace();
  		} catch (IOException e) {
  			e.printStackTrace();
  		}
 
  		FILENAME = "gesture-10.sh";
  		try {
  			fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
   			fos.write(getString(R.string.action_10).getBytes());
  			fos.close();
  		} catch (FileNotFoundException e) {
  			e.printStackTrace();
  		} catch (IOException e) {
  			e.printStackTrace();
  		}
 
  		FILENAME = "gesture-11.sh";
  		try {
  			fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
   			fos.write(getString(R.string.action_11).getBytes());
  			fos.close();
  		} catch (FileNotFoundException e) {
  			e.printStackTrace();
  		} catch (IOException e) {
  			e.printStackTrace();
  		}
 
  		CharSequence toastText = getString(R.string.toastResetActionsOK);
  		Toast.makeText(this , toastText, Toast.LENGTH_SHORT).show();
  		
  		return true;
  	}
 
     public boolean TestAction() {
     	  
   		CharSequence toastText;
   		String scriptname = datapath+"/gesture-"+getGesturenumber()+".sh";
   		
   		if (Utils.canRunRootCommandsInThread()) {
   			Utils.executeRootCommandInThread
   			("chmod 777 "+scriptname+"\n"+scriptname);
   		} else {
   			toastText = getString(R.string.toastLoadGesturesERR);
   			Toast.makeText(this , toastText, Toast.LENGTH_SHORT).show();
   		}
 
   		return true;
   }
      
 }
