 package com.lastcrusade.soundstream.audio;
 
 import android.content.Context;
 import android.content.Intent;
 
 import com.lastcrusade.soundstream.CustomApp;
 import com.lastcrusade.soundstream.service.MessagingService;
 import com.lastcrusade.soundstream.service.PlaylistService;
 import com.lastcrusade.soundstream.util.BroadcastIntent;
 import com.lastcrusade.soundstream.util.BroadcastRegistrar;
 import com.lastcrusade.soundstream.util.IBroadcastActionHandler;
 
 public class RemoteAudioPlayer implements IPlayer {
 
     private CustomApp application;
     private boolean playing;
     
     BroadcastRegistrar registrar;
 
     public RemoteAudioPlayer(CustomApp application) {
         this.application = application;
         this.playing = false;
         registerReceivers();
     }
 
     @Override
     public boolean isPlaying() {
         return this.playing;
     }
 
     @Override
     public void play() {
         //TODO: a hack, because we really should be getting this info from the messaging system (i.e.
         // the host needs to send out a message to say if its playing or paused)
         this.playing = true;
         this.application.getMessagingService().sendPlayMessage();
     }
 
     @Override
     public void pause() {
         //TODO: see above
         this.playing = false;
         this.application.getMessagingService().sendPauseMessage();
     }
 
     @Override
     public void skip() {
         this.application.getMessagingService().sendSkipMessage();
     }
     
     private void registerReceivers() {
     	this.registrar = new BroadcastRegistrar();
    	this.registrar.addAction(MessagingService.ACTION_PLAY_STATUS_MESSAGE, new IBroadcastActionHandler() {
 			
 			@Override
 			public void onReceiveAction(Context context, Intent intent) {
 				playing = intent.getBooleanExtra(MessagingService.EXTRA_IS_PLAYING, false);
 				if(playing) {
 					new BroadcastIntent(PlaylistService.ACTION_PLAYING_AUDIO).send(application);
 				}
 				else {
 					new BroadcastIntent(PlaylistService.ACTION_PAUSED_AUDIO).send(application);
 				}
 			}
 		}).register(this.application);
     }
 }
