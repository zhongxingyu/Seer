 package org.xmms2.server;
 
 import android.media.AudioManager;
 import org.xmms2.server.plugins.Output;
 
 import java.util.HashSet;
 import java.util.Set;
 
 /**
  * @author Eclipser
  */
 public class AudioFocusHandler implements AudioManager.OnAudioFocusChangeListener, PlaybackStatusListener, HeadsetListener
 {
     private final Output output;
     private HeadsetState headsetState;
     private HeadsetState headsetStateWhenPaused;
     private final Set<AudioFocusListener> audioFocusListeners = new HashSet<AudioFocusListener>();
     private AudioFocusState focusState = AudioFocusState.UNFOCUSED;
 
     public AudioFocusHandler(Output output)
     {
         this.output = output;
     }
 
     public void registerFocusListener(AudioFocusListener listener)
     {
         listener.audioFocusChanged(focusState);
         audioFocusListeners.add(listener);
     }
 
     public void setFocus(boolean focus)
     {
         if (focus) {
             focusState = AudioFocusState.FOCUSED;
         } else {
             focusState = AudioFocusState.UNFOCUSED;
         }
 
         for (AudioFocusListener audioFocusListener : audioFocusListeners) {
             audioFocusListener.audioFocusChanged(focusState);
         }
     }
 
     private enum PlaybackState
     {
         STOPPED,
         PAUSED,
         PLAYING,
         DUCKED, // implies playing
     }
 
     private PlaybackState playbackState;
     private PlaybackState playbackStateWhenLostFocus = PlaybackState.STOPPED;
 
     @Override
     public void onAudioFocusChange(int focusChange)
     {
         if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
             switch (playbackState) {
                 case STOPPED:
                     break;
                 case PAUSED:
                     if (playbackStateWhenLostFocus == PlaybackState.PLAYING &&
                        (headsetStateWhenPaused == headsetState || headsetStateWhenPaused == HeadsetState.UNPLUGGED)) {
                         Server.play();
                     }
                     break;
                 case PLAYING:
                     break;
                 case DUCKED:
                     output.adjustVolume(1.0f, 1.0f);
                     playbackState = PlaybackState.PLAYING;
                     break;
             }
 
             focusState = AudioFocusState.FOCUSED;
         } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS || focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
             playbackStateWhenLostFocus = playbackState;
             if (playbackState == PlaybackState.PLAYING || playbackState == PlaybackState.DUCKED) {
                 Server.pause();
             }
             focusState = AudioFocusState.UNFOCUSED;
         } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
             if (playbackState == PlaybackState.PLAYING) {
                 playbackState = PlaybackState.DUCKED;
                 output.adjustVolume(0.15f, 0.15f);
             }
         }
 
         for (AudioFocusListener audioFocusListener : audioFocusListeners) {
             audioFocusListener.audioFocusChanged(focusState);
         }
 
     }
 
     @Override
     public void playbackStatusChanged(PlaybackStatus newStatus)
     {
         output.playbackStatusChanged(newStatus);
         if (playbackState == PlaybackState.DUCKED) {
             output.adjustVolume(1.0f, 1.0f);
         }
         switch (newStatus) {
             case STOPPED:
                 playbackState = PlaybackState.STOPPED;
                 break;
             case PLAYING:
                 playbackState = PlaybackState.PLAYING;
                 break;
             case PAUSED:
                 headsetStateWhenPaused = headsetState;
                 playbackState = PlaybackState.PAUSED;
                 break;
         }
     }
 
     @Override
     public void headsetStateChanged(HeadsetState state)
     {
         headsetState = state;
     }
 }
