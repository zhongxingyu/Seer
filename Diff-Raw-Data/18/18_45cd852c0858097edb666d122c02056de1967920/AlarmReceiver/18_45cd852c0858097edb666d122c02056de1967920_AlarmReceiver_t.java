 /*
  * Copyright (C) 2012 Joakim Persson, Daniel Augurell, Adrian Bjugrd, Andreas Roln
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
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import edu.chalmers.dat255.group09.Alarmed.controller.AlarmController;
 import edu.chalmers.dat255.group09.Alarmed.modules.factory.ModuleFactory;
 
/**
 * A class that would be activated when an alarm activates and then set the module to be started.
 * 
 * @author Daniel Augurell
 * @author Joakim Persson
 * 
 */
 public class AlarmReceiver extends BroadcastReceiver {
 
 	@Override
 	public void onReceive(Context context, Intent intent) {
		AlarmController aControll = AlarmController.getInstance(context);
		if (aControll.alarmReceived(Integer.parseInt(intent.getData()
				.toString()))) {
			Intent activateIntent = new Intent(context,
					ModuleFactory.getModule(intent.getStringExtra("module")));
 			activateIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 			context.startActivity(activateIntent);
 		}
 		aControll.destroy();
 	}
 
 }
