 package com.lastcrusade.soundstream.components;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.telephony.TelephonyManager;
 import android.view.KeyEvent;
 
 import com.lastcrusade.soundstream.service.PlaylistService;
 import com.lastcrusade.soundstream.util.BroadcastIntent;
 import com.lastcrusade.soundstream.util.IBroadcastActionHandler;
 
 /**
  * Handle external music control events that the Android OS may
  * send to a music player.  Unlike most of our broadcast receivers,
  * this one is defined in the Android Manifest and is always listening.
  * 
  * @author Jesse Rosalia
  *
  */
 public class ExternalMusicControlHandler extends BroadcastReceiver {
 
     @Override
     public void onReceive(Context context, Intent intent) {
        //phone state change (incoming phone call
        if (intent.getAction().equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
            //pause the song for incoming phone calls
            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            if (state.equals(TelephonyManager.CALL_STATE_RINGING)) {
                new BroadcastIntent(PlaylistService.ACTION_PAUSE).send(context);
            }
        }
         //media button or disconnnected headset or ICS (or greater) remote control
        else if (intent.getAction().equals(Intent.ACTION_MEDIA_BUTTON)) {
             
             KeyEvent keyEvent = (KeyEvent) intent.getExtras().get(Intent.EXTRA_KEY_EVENT);
             if (keyEvent.getAction() != KeyEvent.ACTION_DOWN)
                 return;
 
             switch (keyEvent.getKeyCode()) {
                 case KeyEvent.KEYCODE_HEADSETHOOK:
                     new BroadcastIntent(PlaylistService.ACTION_PAUSE).send(context);
                     break;
                 case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                     new BroadcastIntent(PlaylistService.ACTION_PLAY_PAUSE).send(context);
                     break;
                 case KeyEvent.KEYCODE_MEDIA_NEXT:
                     new BroadcastIntent(PlaylistService.ACTION_SKIP).send(context);
                     break;
 //                    case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
 //                        // TODO: ensure that doing this in rapid succession actually plays the
 //                        // previous song
 //                        context.startService(new Intent(MusicService.ACTION_REWIND));
 //                        break;
             }
         }
     }
 }
