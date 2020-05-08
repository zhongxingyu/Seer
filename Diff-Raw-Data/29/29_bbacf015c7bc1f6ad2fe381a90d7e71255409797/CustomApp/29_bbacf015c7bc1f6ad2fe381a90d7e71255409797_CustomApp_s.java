 package com.lastcrusade.soundstream;
 
 import java.util.List;
 
 import android.annotation.SuppressLint;
 import android.app.Application;
 import android.app.PendingIntent;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.media.AudioManager;
 import android.media.AudioManager.OnAudioFocusChangeListener;
 import android.media.MediaMetadataRetriever;
 import android.media.RemoteControlClient;
 import android.util.Log;
 
 import com.lastcrusade.soundstream.components.ExternalMusicControlHandler;
 import com.lastcrusade.soundstream.model.SongMetadata;
 import com.lastcrusade.soundstream.model.UserList;
 import com.lastcrusade.soundstream.service.ConnectionService;
 import com.lastcrusade.soundstream.service.ConnectionService.ConnectionServiceBinder;
 import com.lastcrusade.soundstream.service.IMessagingService;
 import com.lastcrusade.soundstream.service.MessagingService;
 import com.lastcrusade.soundstream.service.MessagingService.MessagingServiceBinder;
 import com.lastcrusade.soundstream.service.MusicLibraryService;
 import com.lastcrusade.soundstream.service.MusicLibraryService.MusicLibraryServiceBinder;
 import com.lastcrusade.soundstream.service.PlaylistService;
 import com.lastcrusade.soundstream.service.ServiceLocator;
 import com.lastcrusade.soundstream.service.ServiceNotBoundException;
 import com.lastcrusade.soundstream.util.BroadcastIntent;
 import com.lastcrusade.soundstream.util.BroadcastRegistrar;
 import com.lastcrusade.soundstream.util.IBroadcastActionHandler;
 import com.lastcrusade.soundstream.util.RemoteControlClientCompat;
 
 public class CustomApp extends Application {
     private final String TAG = CustomApp.class.getName();
     
     private UserList userList;
     
     private ServiceLocator<ConnectionService>   connectionServiceLocator;
     private ServiceLocator<MessagingService>    messagingServiceLocator;
     private ServiceLocator<MusicLibraryService> musicLibraryLocator;
     private ServiceLocator<PlaylistService>     playlistServiceLocator;
 
     private BroadcastRegistrar registrar;
 
     private SoundStreamExternalControlClient externalControlClient;
 
     public CustomApp() {
         super();
     }
     
     @Override
     public void onCreate() {
         super.onCreate();
         
         userList = new UserList();
         
         connectionServiceLocator = new ServiceLocator<ConnectionService>(
                 this, ConnectionService.class, ConnectionServiceBinder.class);
         
         messagingServiceLocator = new ServiceLocator<MessagingService>(
                 this, MessagingService.class, MessagingServiceBinder.class);
         
         musicLibraryLocator = new ServiceLocator<MusicLibraryService>(
                 this, MusicLibraryService.class, MusicLibraryServiceBinder.class);
 
         playlistServiceLocator = new ServiceLocator<PlaylistService>(
                 this, PlaylistService.class, PlaylistService.PlaylistServiceBinder.class);
 
         registerReceivers();
         
         registerExternalControlClient();
         
         requestAudio();
     }
     
     private void registerExternalControlClient() {
         externalControlClient = new SoundStreamExternalControlClient(this);
     }
 
     private void unregisterExtranlControlClient() {
         externalControlClient.unregister();
     }
 
     private void requestAudio() {
         //NOTE: we need to request the audio for the remote controls to work, but
         // we also want to handle things like audio ducking and pausing here.
         AudioManager myAudioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
         myAudioManager.requestAudioFocus(new OnAudioFocusChangeListener() {
 
             @Override
             public void onAudioFocusChange(int focusChange) {
                //TODO: duck audio or pause when focus has changed.
             }
             
         }, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
     }
 
     @Override
     public void onTerminate() {
         unregisterReceivers();
         super.onTerminate();
     }
 
     private void registerReceivers() {
         this.registrar = new BroadcastRegistrar();
         this.registrar
             .addAction(ConnectionService.ACTION_GUEST_CONNECTED, new IBroadcastActionHandler() {
                 @Override
                 public void onReceiveAction(Context context, Intent intent) {
                     String bluetoothID = intent.getStringExtra(ConnectionService.EXTRA_GUEST_NAME);
                     String macAddress  = intent.getStringExtra(ConnectionService.EXTRA_GUEST_ADDRESS);
                     userList.addUser(bluetoothID, macAddress);
                     notifyUserListUpdate();
                 }
             })
             .addAction(ConnectionService.ACTION_GUEST_DISCONNECTED, new IBroadcastActionHandler() {
                 
                 @Override
                 public void onReceiveAction(Context context, Intent intent) {
                     String macAddress  = intent.getStringExtra(ConnectionService.EXTRA_GUEST_ADDRESS);
                     userList.removeUser(macAddress);
                     notifyUserListUpdate();
                 }
             })
             .addAction(ConnectionService.ACTION_HOST_CONNECTED, new IBroadcastActionHandler() {
 
                 @Override
                 public void onReceiveAction(Context context, Intent intent) {
                     //send the library to the connected host
                     List<SongMetadata> metadata = getMusicLibraryService().getMyLibrary();
                     getMessagingService().sendLibraryMessageToHost(metadata);
                 }
             })
             .addAction(MessagingService.ACTION_NEW_CONNECTED_USERS_MESSAGE, new IBroadcastActionHandler() {
                 
                 @Override
                 public void onReceiveAction(Context context, Intent intent) {
                     //extract the new user list from the intent
                     userList.copyFrom((UserList) intent.getParcelableExtra(MessagingService.EXTRA_USER_LIST));
                     //tell app to update the user list in all the UI
                     new BroadcastIntent(UserList.ACTION_USER_LIST_UPDATE).send(CustomApp.this);
                 }
             })
             .register(this);
     }
  
     private void unregisterReceivers() {
         this.registrar.unregister();
     }
 
     public UserList getUserList(){
         return userList;
     }
     
     public ConnectionService getConnectionService() {
         ConnectionService connectionService = null;
         try {
             connectionService = this.connectionServiceLocator.getService();
         } catch (ServiceNotBoundException e) {
             Log.wtf(TAG, e);
         }
         return connectionService;
     }
 
     public IMessagingService getMessagingService() {
         MessagingService messagingService = null;
         try {
             messagingService = this.messagingServiceLocator.getService();
         } catch (ServiceNotBoundException e) {
             Log.wtf(TAG, e);
         }
         return messagingService;
     }
     
     public MusicLibraryService getMusicLibraryService() {
         MusicLibraryService musicLibraryService = null;
         try {
             musicLibraryService = this.musicLibraryLocator.getService();
         } catch (ServiceNotBoundException e) {
             Log.wtf(TAG, e);
         }
         return musicLibraryService;
     }
     
     public PlaylistService getPlaylistService() {
         PlaylistService playlistService = null;
         try{
             playlistService = this.playlistServiceLocator.getService();
         } catch (ServiceNotBoundException e) {
             Log.wtf(TAG, e);
         }
         return playlistService;
     }
 
     public void notifyUserListUpdate() {
         new BroadcastIntent(UserList.ACTION_USER_LIST_UPDATE).send(this);
         getMessagingService().sendUserListMessage(userList);
     }
 
 }
