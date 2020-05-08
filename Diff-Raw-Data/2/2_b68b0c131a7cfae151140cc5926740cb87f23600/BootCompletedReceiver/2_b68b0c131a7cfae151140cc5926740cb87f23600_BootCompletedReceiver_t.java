 /*
  * Modifications Copyright (C) 2013 The OmniROM Project
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 3
  * of the License, or (at your option) any later version.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */
 package com.bel.android.dspmanager.receiver;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 
 import com.bel.android.dspmanager.activity.WM8994;
 import com.bel.android.dspmanager.modules.soundcontrol.SoundControlHelper;
 import com.bel.android.dspmanager.modules.boefflasoundcontrol.BoefflaSoundControlHelper;
 import com.bel.android.dspmanager.service.HeadsetService;
 
 /**
  * This receiver starts our {@link HeadsetService} after system boot. Since
  * Android 2.3, we will always need a persistent process, because we are forced
  * to keep track of all open audio sessions.
  *
  * @author alankila
  */
 public class BootCompletedReceiver extends BroadcastReceiver {
     @Override
     public void onReceive(Context context, Intent intent) {
         context.startService(new Intent(context, HeadsetService.class));
         if (WM8994.isSupported(context)) {
             WM8994.restore(context);
         }
         if (SoundControlHelper.getSoundControlHelper(context).isSupported()) {
             SoundControlHelper.getSoundControlHelper(context).applyValues();
         }
        if (BoefflaSoundControlHelper.getBoefflaSoundControlHelper(context).getBoefflaSound() && BoefflaSoundControlHelper.getBoefflaSoundControlHelper(context).readBoefflaSound() != 1) {
             BoefflaSoundControlHelper.getBoefflaSoundControlHelper(context).applyValues();
         }
     }
 }
