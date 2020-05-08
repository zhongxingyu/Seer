 /*
  * Copyright (C) 2010 Chris Boyle
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
 
 package name.boyle.chris.timer;
 
 import android.app.AlarmManager;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.media.AudioManager;
 import android.media.RingtoneManager;
 import android.net.Uri;
 import android.text.format.Time;
 
 public class Timer
 {
 	public long id = -1;
 	public String name = "";
 	public boolean enabled = false;
 	public long nextMillis = 0,
 			intervalSecs = 4*60*60,
 			nightStart=0,
 			nightStop=8*60*60;
 	public Uri dayTone = null, nightTone = null;
 	public boolean nightNext = false,
 			dayLED = true, dayWait = true,
 			nightLED = false, nightWait = true;
 
 	public Timer()
 	{
 		dayTone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
 	}
 
 	protected void setNextAlarm(Context context)
 	{
 		AlarmManager alarms = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(Receiver.ACTION_ALARM, Uri.parse("timer:"+id));
 		PendingIntent p = PendingIntent.getBroadcast(context, 0, i, 0);
 		if (enabled) {
 			alarms.setRepeating(AlarmManager.RTC_WAKEUP, nextMillis, 300000, p);  // 5 minutes
 		} else {
 			alarms.cancel(p);
 		}
 	}
 	
 	protected void reset()
 	{
 		nextMillis = (enabled ? System.currentTimeMillis() : 0) + intervalSecs*1000 + 3;
 	}
 	
 	protected static long occurrence(int timeOfDay, long from, boolean forwards)
 	{
 		Time t = new Time();
 		t.set(from);
 		t.second = timeOfDay % 60;
 		timeOfDay /= 60;
 		t.minute = timeOfDay % 60;
 		timeOfDay /= 60;
 		t.hour = timeOfDay;
 		long m = t.toMillis(true);
 		if (forwards && m < from) {
 			t.monthDay++;
 			t.normalize(true);
 			m = t.toMillis(true);
 		} else if (! forwards && m > from) {
 			t.monthDay--;
 			t.normalize(true);
 			m = t.toMillis(true);
 		}
 		return m;
 	}
 	
 	protected boolean isNight()
 	{
 		if (nightNext) return true;
 		long now = System.currentTimeMillis(),
 				lastNightStart = occurrence((int)nightStart, now, false),
 				nextNightStop = occurrence((int)nightStop, lastNightStart, true);
 		return ! (lastNightStart <= nextMillis && nextNightStop <= nextMillis);
 	}
 	
 	protected boolean shouldWait()
 	{
 		return isNight() ? nightWait : dayWait;
 	}
 	
 	protected void unNotify(Context context)
 	{
 		NotificationManager notifications = (NotificationManager)
 				context.getSystemService(Context.NOTIFICATION_SERVICE);
 		notifications.cancel((int)id);
 	}
 	
 	protected boolean notify(Context context)
 	{
 		if (! enabled) {
 			unNotify(context);
 			return false;
 		}
 		NotificationManager notifications = (NotificationManager)
 				context.getSystemService(Context.NOTIFICATION_SERVICE);
 		boolean needSave = nightNext;  // a one-time flag is about to be cleared
 		boolean isNight = isNight();
 		nightNext = false;
 		boolean useLED = isNight ? nightLED : dayLED; 
 		String text = name.length() > 0 ? name : "Timer";
 		Notification n = new Notification(R.drawable.icon, text,
 				nextMillis);
 		n.setLatestEventInfo(context, text, null, PendingIntent.getActivity(
 				context, 0, new Intent(context, TimerActivity.class)
 				.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK), 0));
 		n.flags = Notification.FLAG_NO_CLEAR
 				| (useLED ? Notification.FLAG_SHOW_LIGHTS : 0);
 		if (useLED) {
 			n.ledOnMS = 250;
 			n.ledOffMS = 1250;
 			n.ledARGB = 0xff2222ff;
 		}
 		n.audioStreamType = AudioManager.STREAM_ALARM;
 		n.sound = isNight ? nightTone : dayTone;
 		notifications.notify((int)id, n);
 		if (! shouldWait() && intervalSecs > 0) {
 			reset();
 			needSave = true;  // to save new alarm time
 			setNextAlarm(context);
 		}
 		return needSave;
 	}
 }
