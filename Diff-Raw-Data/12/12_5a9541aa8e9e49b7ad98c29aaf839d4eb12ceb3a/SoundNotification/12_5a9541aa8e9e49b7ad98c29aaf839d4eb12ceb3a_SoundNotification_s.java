 /**
  * Copyright (C) 2012 Emil Edholm, Emil Johansson, Johan Andersson, Johan Gustafsson
  * 
  * This file is part of dat255-bearded-octo-lama
  *
  *  dat255-bearded-octo-lama is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  dat255-bearded-octo-lama is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with dat255-bearded-octo-lama.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 package it.chalmers.dat255_bearded_octo_lama.activities.notifications;
 
 
 import it.chalmers.dat255_bearded_octo_lama.utilities.RingtoneFinder;
 
 import java.util.Collections;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Context;
 import android.media.MediaPlayer;
 import android.media.Ringtone;
 import android.media.RingtoneManager;
 import android.net.Uri;
 import android.provider.Settings;
 import android.util.Log;
 
 /**
  * Used to describe a sound notification.
  * @author Johan Andersson
  * @modified Emil Edholm
  */
 public class SoundNotification extends NotificationDecorator {
 
 	private List<Ringtone> notificationSounds = Collections.emptyList();
 	private Ringtone playing = null;
 	private final Context context;
 	private final Activity act;
 	MediaPlayer mp = null;
 
 	public SoundNotification(Notification decoratedNotification, List<Ringtone> ringtones, Activity act) {
 		super(decoratedNotification);
 		this.context = (Context)act;
 		this.act = act;
 		notificationSounds = ringtones;
 	}
 
 	@Override
 	public void start() {
 		super.start();
 		Collections.shuffle(notificationSounds);
 		if(!notificationSounds.isEmpty()){
 			playing = notificationSounds.get(0);
 		} else {
 			// Use default sound if no sounds listed previously.
 			playing = RingtoneManager.getRingtone(context.getApplicationContext(), 
 					Settings.System.DEFAULT_ALARM_ALERT_URI);
 		}
		Uri uri = RingtoneFinder.findRingtoneUri(act, playing);
		if(uri == null){
			uri = Settings.System.DEFAULT_ALARM_ALERT_URI;
			Log.d("SoundNotification", "Uri is null replaced with "+uri);
 		}
 
 		//If clause if you use the emulator or device without sound
 		if(uri != null){
 			mp = MediaPlayer.create(context, uri);
 			mp.setLooping(true);
 			//TODO: None-hardcoded-volume
 			mp.setVolume(1f, 1f);
 			mp.start();
 		}
 	}
 
 	@Override
 	public void stop() {
 		super.stop();
 		if(mp != null && mp.isPlaying()){
 			mp.stop();
 		}
 	}
 }
