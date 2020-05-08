 package com.lastcrusade.soundstream.service;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.UUID;
 
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Binder;
 import android.os.IBinder;
 import android.util.Log;
 
 import com.lastcrusade.soundstream.model.SongMetadata;
 import com.lastcrusade.soundstream.model.UserList;
 import com.lastcrusade.soundstream.net.MessageThreadMessageDispatch;
 import com.lastcrusade.soundstream.net.MessageThreadMessageDispatch.IMessageHandler;
 import com.lastcrusade.soundstream.net.message.IMessage;
 import com.lastcrusade.soundstream.net.message.LibraryMessage;
 import com.lastcrusade.soundstream.net.message.PauseMessage;
 import com.lastcrusade.soundstream.net.message.PlayMessage;
 import com.lastcrusade.soundstream.net.message.PlayStatusMessage;
 import com.lastcrusade.soundstream.net.message.PlaylistMessage;
 import com.lastcrusade.soundstream.net.message.RequestSongMessage;
 import com.lastcrusade.soundstream.net.message.SkipMessage;
 import com.lastcrusade.soundstream.net.message.StringMessage;
 import com.lastcrusade.soundstream.net.message.TransferSongMessage;
 import com.lastcrusade.soundstream.net.message.UserListMessage;
 import com.lastcrusade.soundstream.service.ConnectionService.ConnectionServiceBinder;
 import com.lastcrusade.soundstream.util.BroadcastIntent;
 import com.lastcrusade.soundstream.util.BroadcastRegistrar;
 
 public class MessagingService extends Service implements IMessagingService {
 
     private static final String TAG = MessagingService.class.getName();
 
     public static final String ACTION_STRING_MESSAGE = MessagingService.class.getName() + ".action.StringMessage";
     public static final String EXTRA_STRING          = MessagingService.class.getName() + ".extra.String";
     
     public static final String ACTION_PAUSE_MESSAGE = MessagingService.class.getName() + ".action.PauseMessage";
     public static final String ACTION_PLAY_MESSAGE  = MessagingService.class.getName() + ".action.PlayMessage";
     public static final String ACTION_SKIP_MESSAGE  = MessagingService.class.getName() + ".action.SkipMessage";
     
     public static final String ACTION_PLAY_STATUS_MESSAGE = MessagingService.class.getName() + ".action.PlayStatusMessage";
     public static final String EXTRA_IS_PLAYING = MessagingService.class.getName() + ".extra.IsPlaying";
     
     public static final String ACTION_LIBRARY_MESSAGE = MessagingService.class.getName() + ".action.LibraryMessage";
     public static final String EXTRA_SONG_METADATA    = MessagingService.class.getName() + ".extra.SongMetadata";
 
     //This also uses EXTRA_SONG_METADATA
     public static final String ACTION_PLAYLIST_UPDATED_MESSAGE = MessagingService.class.getName() + ".action.PlaylistUpdated";
     
     public static final String ACTION_NEW_CONNECTED_USERS_MESSAGE = MessagingService.class.getName() + ".action.UserListMessage";
     public static final String EXTRA_USER_LIST                    = MessagingService.class.getName() + ".extra.UserList";
 
     public static final String ACTION_REQUEST_SONG_MESSAGE        = MessagingService.class.getName() + ".action.RequestSongMessage";
     public static final String EXTRA_ADDRESS                      = MessagingService.class.getName() + ".extra.Address";
     public static final String EXTRA_SONG_ID                      = MessagingService.class.getName() + ".extra.SongId";
 
     public static final String ACTION_TRANSFER_SONG_MESSAGE       = MessagingService.class.getName() + ".action.TransferSongMessage";
     //also uses ADDRESS and SONG_ID
     public static final String EXTRA_SONG_FILE_NAME               = MessagingService.class.getName() + ".extra.SongFileName";
     public static final String EXTRA_SONG_TEMP_FILE               = MessagingService.class.getName() + ".extra.SongTempFile";
 
 
     /**
      * A default handler for command messages (messages that do not have any data).  These messages
      * just map to an action.
      * 
      * @author Jesse Rosalia
      *
      * @param <T>
      */
     private class CommandHandler<T extends IMessage> implements IMessageHandler<T> {
 
         private String action;
         public CommandHandler(String action) {
             this.action = action;
         }
 
         @Override
         public void handleMessage(int messageNo, T message, String fromAddr) {
             new BroadcastIntent(this.action).send(MessagingService.this);
         }
     }
 
     /**
      * Class for clients to access.  Because we know this service always
      * runs in the same process as its clients, we don't need to deal with
      * IPC.
      */
     public class MessagingServiceBinder extends Binder implements ILocalBinder<MessagingService> {
         public MessagingService getService() {
             return MessagingService.this;
         }
     }
 
     private BroadcastRegistrar                broadcastRegistrar;
     private MessageThreadMessageDispatch      messageDispatch;
     private Map<IMessage, String>             actionDispatchMap;
     private ServiceLocator<ConnectionService> connectServiceLocator;
 
     @Override
     public void onCreate() {
         super.onCreate();
         this.actionDispatchMap = new HashMap<IMessage, String>();
         this.connectServiceLocator = new ServiceLocator<ConnectionService>(
                 this, ConnectionService.class, ConnectionServiceBinder.class);
         
         registerMessageHandlers();
     }
 
     @Override
     public IBinder onBind(Intent intent) {
         return new MessagingServiceBinder();
     }
     
     @Override
     public boolean onUnbind(Intent intent) {
         return super.onUnbind(intent);
     }
 
     @Override
     public void onDestroy() {
         super.onDestroy();
         this.connectServiceLocator.unbind();
     }
 
     public void receiveMessage(int messageNo, IMessage message, String fromAddr) {
         //for ease of implementation, this also uses a message dispatch object.
         this.messageDispatch.handleMessage(messageNo, message, fromAddr);
     }
 
     private void registerMessageHandlers() {
         this.messageDispatch = new MessageThreadMessageDispatch();
         registerStringMessageHandler();
         registerLibraryMessageHandler();
         registerPauseMessageHandler();
         registerPlayMessageHandler();
         registerSkipMessageHandler();
         registerPlaylistMessageHandler();
         registerPlayStatusMessageHandler();
         registerRequestSongMessageHandler();
         registerTransferSongMessageHandler();
         registerUserListMessageHandler();
     }
 
     private void registerLibraryMessageHandler() {
         this.messageDispatch.registerHandler(LibraryMessage.class, new IMessageHandler<LibraryMessage>() {
 
             @Override
             public void handleMessage(int messageNo,
                     LibraryMessage message, String fromAddr) {
                 new BroadcastIntent(ACTION_LIBRARY_MESSAGE)
                    .putParcelableArrayListExtra(EXTRA_SONG_METADATA, message.getLibrary())
                     .send(MessagingService.this);
             }
         });
     }
 
     private void registerStringMessageHandler() {
         this.messageDispatch.registerHandler(StringMessage.class, new IMessageHandler<StringMessage>() {
 
             @Override
             public void handleMessage(int messageNo,
                     StringMessage message, String fromAddr) {
                 StringMessage sm = (StringMessage) message;
                 new BroadcastIntent(ACTION_STRING_MESSAGE)
                     .putExtra(EXTRA_STRING, sm.getString())
                     .send(MessagingService.this);
             }            
         });
     }
 
     private void registerPauseMessageHandler() {
         this.messageDispatch.registerHandler(PauseMessage.class,
                 new CommandHandler<PauseMessage>(ACTION_PAUSE_MESSAGE));
     }
     
     private void registerPlayMessageHandler() {
         this.messageDispatch.registerHandler(PlayMessage.class,
                 new CommandHandler<PlayMessage>(ACTION_PLAY_MESSAGE));
     }
     
     private void registerSkipMessageHandler() {
         this.messageDispatch.registerHandler(SkipMessage.class,
                 new CommandHandler<SkipMessage>(ACTION_SKIP_MESSAGE));
     }
     
     private void registerPlayStatusMessageHandler() {
     	this.messageDispatch.registerHandler(PlayStatusMessage.class,
     			new IMessageHandler<PlayStatusMessage>() {
 					
 					@Override
 					public void handleMessage(int messageNo, PlayStatusMessage message,
 							String fromAddr) {
 						new BroadcastIntent(ACTION_PLAY_STATUS_MESSAGE)
 							.putExtra(EXTRA_IS_PLAYING, message.getString().equals("Play"))
 							.send(MessagingService.this);
 					}
 				});
     }
     
     private void registerRequestSongMessageHandler() {
         this.messageDispatch.registerHandler(RequestSongMessage.class, new IMessageHandler<RequestSongMessage>() {
 
             @Override
             public void handleMessage(int messageNo,
                     RequestSongMessage message, String fromAddr) {
                 new BroadcastIntent(ACTION_REQUEST_SONG_MESSAGE)
                     .putExtra(EXTRA_ADDRESS, fromAddr)
                     .putExtra(EXTRA_SONG_ID, message.getSongId())
                     .send(MessagingService.this);
             }
         });
     }
 
     private void registerTransferSongMessageHandler() {
         this.messageDispatch.registerHandler(TransferSongMessage.class, new IMessageHandler<TransferSongMessage>() {
 
             @Override
             public void handleMessage(int messageNo,
                     TransferSongMessage message, String fromAddr) {
                 try {
                     //write the song data to a temporary file
                     //...we cannot send large file data thru broadcast intents
                     // and this is faster for even smaller files
                     File outputFile = createTempFile(message);
                     FileOutputStream fos = new FileOutputStream(outputFile);
                     fos.write(message.getSongData());
                     fos.close();
                     
                     new BroadcastIntent(ACTION_TRANSFER_SONG_MESSAGE)
                         .putExtra(EXTRA_ADDRESS,        fromAddr)
                         .putExtra(EXTRA_SONG_ID,        message.getSongId())
                         .putExtra(EXTRA_SONG_FILE_NAME, message.getSongFileName())
                         .putExtra(EXTRA_SONG_TEMP_FILE, outputFile.getCanonicalPath())
                         .send(MessagingService.this);
                 } catch (Exception e) {
                     Log.wtf(TAG, e);
                 }
 
             }
         });
     }
 
     private void registerUserListMessageHandler(){
         this.messageDispatch.registerHandler(UserListMessage.class, new IMessageHandler<UserListMessage>() {
 
             @Override
             public void handleMessage(int messageNo, UserListMessage message,
                     String fromAddr) {
                 new BroadcastIntent(ACTION_NEW_CONNECTED_USERS_MESSAGE)
                     .putExtra(EXTRA_USER_LIST, message.getUserList())
                     .send(MessagingService.this);
                 
             }
         });
     }
 
     private void registerPlaylistMessageHandler() {
         this.messageDispatch.registerHandler(PlaylistMessage.class,
                 new IMessageHandler<PlaylistMessage>() {
 
             @Override
             public void handleMessage(int messageNo,
                     PlaylistMessage message, String fromAddr) {
 
                 new BroadcastIntent(ACTION_PLAYLIST_UPDATED_MESSAGE)
                     .putParcelableArrayListExtra(EXTRA_SONG_METADATA, message.getSongsToPlay())
                     .send(MessagingService.this);
             }
         });
     }
 
     private void sendMessageToGuest(String address, IMessage msg) {
         try {
             if (this.connectServiceLocator.getService().isGuestConnected(address)) {
                 this.connectServiceLocator.getService().sendMessageToGuest(address, msg);
             }
         } catch (ServiceNotBoundException e) {
             Log.wtf(TAG, e);
         }
     }
 
     private void sendMessageToGuests(IMessage msg) {
         try {
             if (this.connectServiceLocator.getService().isGuestConnected()) {
                 this.connectServiceLocator.getService().broadcastMessageToGuests(msg);
             }
         } catch (ServiceNotBoundException e) {
             Log.wtf(TAG, e);
         }
     }
 
     private void sendMessageToHost(IMessage msg) {
         try {
             if (this.connectServiceLocator.getService().isHostConnected()) {
                 this.connectServiceLocator.getService().sendMessageToHost(msg);
             }
         } catch (ServiceNotBoundException e) {
             Log.wtf(TAG, e);
         }
     }
 
     @Override
     public void sendLibraryMessageToHost(List<SongMetadata> library) {
         LibraryMessage msg = new LibraryMessage(library);
         //send the message to the host
         sendMessageToHost(msg);
     }
     
     @Override
     public void sendLibraryMessageToGuests(List<SongMetadata> library) {
         LibraryMessage msg = new LibraryMessage(library);
         //send the message to the guests
         sendMessageToGuests(msg);
     }
 
     @Override
     public void sendPauseMessage() {
         PauseMessage msg = new PauseMessage();
         //send the message to the host
         sendMessageToHost(msg);
     }
 
     @Override
     public void sendPlayMessage() {
         PlayMessage msg = new PlayMessage();
         //send the message to the host
         sendMessageToHost(msg);
     }
 
     @Override
     public void sendSkipMessage() {
         SkipMessage msg = new SkipMessage();
         //send the message to the host
         sendMessageToHost(msg);
     }
     
     public void sendPlayStatusMessage(String playStatusMessage) {
     	PlayStatusMessage msg = new PlayStatusMessage(playStatusMessage);
     	//send the message to the guests
     	sendMessageToGuests(msg);
     }
 
     public void sendStringMessage(String message) {
         StringMessage sm = new StringMessage();
         sm.setString(message);
         //JR, 03/02/12, TODO: the connection service should be changed to only deal with "connections".  The mode of connection will
         // be determined by which method is called initially (braodcastGuest vs findNewGuests), but after that point, it should just
         // work with connections
         try {
             //send the message to the host
             if (this.connectServiceLocator.getService().isHostConnected()) {
                 sendMessageToHost(sm);
             }
             
             if (this.connectServiceLocator.getService().isGuestConnected()) {
                 sendMessageToGuests(sm);
             }
         } catch (ServiceNotBoundException e) {
             Log.wtf(TAG, e);
         }
     }
     
     public void sendPlaylistMessage(List<? extends SongMetadata> songsToPlay){
         try {
             PlaylistMessage playlistMessage = new PlaylistMessage(songsToPlay);
             //send the message to the host
             if (this.connectServiceLocator.getService().isHostConnected()) {
                 sendMessageToHost(playlistMessage);
             }
 
             if (this.connectServiceLocator.getService().isGuestConnected()) {
                 sendMessageToGuests(playlistMessage);
             }
         } catch (ServiceNotBoundException e) {
             Log.wtf(TAG, e);
         }
 
     }
     
     @Override
     public void sendRequestSongMessage(String address, long songId) {
         RequestSongMessage msg = new RequestSongMessage(songId);
         //send the message to the fans
         sendMessageToGuest(address, msg);
     }
     
     @Override
     public void sendTransferSongMessage(String address, long songId,
             String fileName, byte[] bytes) {
         TransferSongMessage msg = new TransferSongMessage(songId, fileName, bytes);
         //send the message to the fans
         sendMessageToHost(msg);
         
     }
 
     //sends the user list out to everyone
     public void sendUserListMessage(UserList userlist){
         UserListMessage ulm = new UserListMessage(userlist);
         try {
             //send the message to the host
             if (this.connectServiceLocator.getService().isHostConnected()) {
                 sendMessageToHost(ulm);
             }
 
             if (this.connectServiceLocator.getService().isGuestConnected()) {
                 sendMessageToGuests(ulm);
             }
         } catch (ServiceNotBoundException e) {
             Log.wtf(TAG, e);
         }
     }
     
     
     /**
      * Helper method to create a temporary file.
      * 
      * TODO: Android doesn't really have temporary files...it will proactively clear the cache folder,
      * but we should be better citizens and clean up after ourself.  This is not an immediate (read: alpha)
      * concern, because we consume all cache files in PlaylistDataManager (which will clean up after itself)
      * but before this gets to final, we should make sure all bases are covered.
      * 
      * @param message
      * @return
      * @throws IOException
      */
     private File createTempFile(TransferSongMessage message)
             throws IOException {
         File outputDir = MessagingService.this.getCacheDir(); // context being the Activity pointer
         String filePrefix = UUID.randomUUID().toString().replace("-", "");
         int inx = message.getSongFileName().lastIndexOf(".");
         String extension = message.getSongFileName().substring(inx + 1);
         File outputFile = File.createTempFile(filePrefix, extension, outputDir);
         return outputFile;
     }
 
 }
