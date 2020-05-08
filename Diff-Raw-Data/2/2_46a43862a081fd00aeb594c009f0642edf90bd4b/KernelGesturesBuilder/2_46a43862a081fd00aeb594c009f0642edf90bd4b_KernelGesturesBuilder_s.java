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
 
 	private MTView KernelGesturesMTView;
 	private SharedPreferences sharedPrefs;
 	
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
 	 	    	startActivity(new Intent(this, LauchActivities.class ));
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
       					"cp /data/data/ar.com.nivel7.kernelgesturesbuilder/files/* /data/gestures\n" +
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
     			("chmod 755 /data/data/ar.com.nivel7.kernelgesturesbuilder/files/install_gestures.sh\n" +
     			 "/data/data/ar.com.nivel7.kernelgesturesbuilder/files/install_gestures.sh");
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
     			("cp /data/gestures/* /data/data/ar.com.nivel7.kernelgesturesbuilder/files \n" +
     					"chmod 666 /data/data/ar.com.nivel7.kernelgesturesbuilder/files/*");
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
  			fos.write(("mdnie_status=`cat /sys/class/mdnie/mdnie/negative`\n" +
  					"if [ \"$mdnie_status\" -eq \"0\" ]; then\n" +
  					"		echo 1 > /sys/class/mdnie/mdnie/negative\n" +
  					"	else\n" +
  					"		echo 0 > /sys/class/mdnie/mdnie/negative\n" +
  					"	fi;\n").getBytes());
  			fos.close();
  		} catch (FileNotFoundException e) {
  			e.printStackTrace();
  		} catch (IOException e) {
  			e.printStackTrace();
  		}
 
  		FILENAME = "gesture-2.sh";
  		try {
  			fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
  			fos.write(("key=26; service call window 12 i32 1 i32 1 i32 5 i32 0 i32 0 i32 $key i32 0 i32 0 i32 0 i32 8 i32 0 i32 0 i32 0 i32 0; service call window 12 i32 1 i32 1 i32 5 i32 0 i32 1 i32 $key i32 0 i32 0 i32 27 i32 8 i32 0 i32 0 i32 0 i32 0\n").getBytes());
  			fos.close();
  		} catch (FileNotFoundException e) {
  			e.printStackTrace();
  		} catch (IOException e) {
  			e.printStackTrace();
  		}
 
  		FILENAME = "gesture-3.sh";
  		try {
  			fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
  			fos.write(("am start -a android.intent.action.MAIN -n com.gokhanmoral.stweaks.app/.MainActivity;").getBytes());
  			fos.close();
  		} catch (FileNotFoundException e) {
  			e.printStackTrace();
  		} catch (IOException e) {
  			e.printStackTrace();
  		}
 
  		FILENAME = "gesture-4.sh";
  		try {
  			fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
  			fos.write(("service call phone 2 s16 \"your beloved number\"").getBytes());
  			fos.close();
  		} catch (FileNotFoundException e) {
  			e.printStackTrace();
  		} catch (IOException e) {
  			e.printStackTrace();
  		}
 
  		FILENAME = "gesture-5.sh";
  		try {
  			fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
  			fos.write(("am start --activity-exclude-from-recents com.sec.android.app.camera\n" +
  					"am start --activity-exclude-from-recents com.android.camera/.Camera\n").getBytes());
  			fos.close();
  		} catch (FileNotFoundException e) {
  			e.printStackTrace();
  		} catch (IOException e) {
  			e.printStackTrace();
  		}
 
  		FILENAME = "gesture-6.sh";
  		try {
  			fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
  			fos.write(("service call bluetooth 1 | grep \"0 00000000\" > /dev/null\n" +
  					"		if [ \"$?\" -eq \"0\" ]; then\n" +
  					"			service call bluetooth 3 > /dev/null\n" +
  					"		else\n" +
  					"			[ \"$1\" -eq \"1\" ] && service call bluetooth 5 > /dev/null\n" +
  					"			[ \"$1\" -ne \"1\" ] && service call bluetooth 4 > /dev/null\n" +
  					"		fi;\n").getBytes());
  			fos.close();
  		} catch (FileNotFoundException e) {
  			e.printStackTrace();
  		} catch (IOException e) {
  			e.printStackTrace();
  		}
 
  		FILENAME = "gesture-7.sh";
  		try {
  			fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
  			fos.write(("service call wifi 14 | grep \"0 00000001\" > /dev/null\n" +
  					"		if [ \"$?\" -eq \"0\" ]; then\n" +
  					"			service call wifi 13 i32 1 > /dev/null\n" +
  					"		else\n" +
  					"			service call wifi 13 i32 0 > /dev/null\n		fi;\n").getBytes());
  			fos.close();
  		} catch (FileNotFoundException e) {
  			e.printStackTrace();
  		} catch (IOException e) {
  			e.printStackTrace();
  		}
 
  		FILENAME = "gesture-8.sh";
  		try {
  			fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
  			fos.write(("input keyevent 85\n").getBytes());
  			fos.close();
  		} catch (FileNotFoundException e) {
  			e.printStackTrace();
  		} catch (IOException e) {
  			e.printStackTrace();
  		}
 
  		FILENAME = "gesture-9.sh";
  		try {
  			fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
  			fos.write(("input keyevent 164\n").getBytes());
  			fos.close();
  		} catch (FileNotFoundException e) {
  			e.printStackTrace();
  		} catch (IOException e) {
  			e.printStackTrace();
  		}
 
  		FILENAME = "gesture-10.sh";
  		try {
  			fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
  			fos.write(("input keyevent 3\n").getBytes());
  			fos.close();
  		} catch (FileNotFoundException e) {
  			e.printStackTrace();
  		} catch (IOException e) {
  			e.printStackTrace();
  		}
 
  		FILENAME = "gesture-11.sh";
  		try {
  			fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
  			fos.write(("service call vibrator 2 i32 100 i32 0\n        dumpsys activity a | grep \"Recent #1:.* com.anddoes.launcher\"\n        if [ \"$?\" -eq \"0\" ]; then\n            service call activity 24 i32 `dumpsys activity a | grep \"Recent #2:\" | grep -o -E \"#[0-9]+ \" | cut -c2-` i32 2\n        else\n            service call activity 24 i32 `dumpsys activity a | grep \"Recent #1:\" | grep -o -E \"#[0-9]+ \" | cut -c2-` i32 2\n        fi\n").getBytes());
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
 
      
 }
