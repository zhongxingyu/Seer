 /**
  * Copyright (C) 2009 Jesse Wilson
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
  */
 
 
 package com.publicobject.shush;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.media.AudioManager;
 
 import static android.content.Context.AUDIO_SERVICE;
 import static android.media.AudioManager.RINGER_MODE_NORMAL;
 import static android.media.AudioManager.STREAM_RING;
 
 /**
  * Turns the ringer on full blast when received.
  */
 public class TurnRingerOn extends BroadcastReceiver {
     public void onReceive(Context context, Intent intent) {
         AudioManager am = (AudioManager) context.getSystemService(AUDIO_SERVICE);
        if (am.getRingerMode() == AudioManager.RINGER_MODE_SILENT) {
             am.setRingerMode(RINGER_MODE_NORMAL);
             am.setStreamVolume(STREAM_RING, am.getStreamMaxVolume(STREAM_RING), 0);
         }
     }
 }
