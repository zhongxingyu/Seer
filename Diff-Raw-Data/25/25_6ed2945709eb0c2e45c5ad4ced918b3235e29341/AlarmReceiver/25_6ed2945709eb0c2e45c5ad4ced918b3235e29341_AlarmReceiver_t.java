 /*
  * Copyright (C) 2012 Joakim Persson, Daniel Augurell, Adrian Bjugard, Andreas Rolen
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
 package edu.chalmers.dat255.group09.Alarmed.receiver;
 
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import edu.chalmers.dat255.group09.Alarmed.constants.Constants;
 import edu.chalmers.dat255.group09.Alarmed.controller.AlarmController;
 import edu.chalmers.dat255.group09.Alarmed.modules.factory.ModuleFactory;
 
 /**
  * A class that would be activated when an alarm activates and then set the
  * module to be started.
  * 
  * @author Daniel Augurell
  * @author Joakim Persson
  * 
  */
 public class AlarmReceiver extends BroadcastReceiver {
 
 	@Override
 	public void onReceive(Context context, Intent intent) {
 		AlarmController aControl = AlarmController.getInstance();
 		aControl.init(context);
 		String[] data = intent.getData().toString().split(":::");
 		if (aControl.alarmReceived(Integer.parseInt(data[0]))) {
 			Intent activateIntent = new Intent(context,
 					ModuleFactory.getModule(data[1]));
 			activateIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 			activateIntent.putExtra(Constants.VOLUME, data[2]);
 			activateIntent.putExtra(Constants.TONEURI, data[3]);
 			activateIntent.putExtra(Constants.VIBRATION, data[4]);
			if (!isModuleRunning(context)) {
				context.startActivity(activateIntent);
			}
 		}
 		aControl.destroy();
 	}
 
	public boolean isModuleRunning(Context context) {
		ActivityManager activityManager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> tasks = activityManager
				.getRunningTasks(Integer.MAX_VALUE);

		for (RunningTaskInfo task : tasks) {
			String className = task.topActivity.getClassName();
			if (className.contains(Constants.MODULE)) {
				return true;
			}
		}
		return false;
	}

 }
