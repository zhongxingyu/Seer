 package com.lastcrusade.soundstream.service;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import android.app.Service;
 import android.content.Intent;
 import android.os.Binder;
 import android.os.IBinder;
 import android.util.Log;
 
 import com.lastcrusade.soundstream.model.SongMetadata;
 import com.lastcrusade.soundstream.model.UserList;
 import com.lastcrusade.soundstream.net.MessageThreadMessageDispatch;
 import com.lastcrusade.soundstream.net.MessageThreadMessageDispatch.IMessageHandler;
 import com.lastcrusade.soundstream.net.message.ConnectFansMessage;
 import com.lastcrusade.soundstream.net.message.FindNewFansMessage;
 import com.lastcrusade.soundstream.net.message.FoundFansMessage;
 import com.lastcrusade.soundstream.net.message.IMessage;
 import com.lastcrusade.soundstream.net.message.LibraryMessage;
 import com.lastcrusade.soundstream.net.message.PauseMessage;
 import com.lastcrusade.soundstream.net.message.PlayMessage;
 import com.lastcrusade.soundstream.net.message.SkipMessage;
 import com.lastcrusade.soundstream.net.message.StringMessage;
 import com.lastcrusade.soundstream.net.message.UserListMessage;
 import com.lastcrusade.soundstream.service.ConnectionService.ConnectionServiceBinder;
 import com.lastcrusade.soundstream.util.BroadcastIntent;
 import com.lastcrusade.soundstream.util.BroadcastRegistrar;
 
 public class MessagingService extends Service implements IMessagingService {
 
     private static final String TAG = MessagingService.class.getName();
 
     public static final String ACTION_STRING_MESSAGE = MessagingService.class.getName() + ".action.StringMessage";
     public static final String EXTRA_STRING          = MessagingService.class.getName() + ".extra.String";
     
     public static final String ACTION_FOUND_FANS_MESSAGE = MessagingService.class.getName() + ".action.FoundFansMessage";
     public static final String EXTRA_FOUND_FANS          = MessagingService.class.getName() + ".extra.FoundFans";
     
     public static final String ACTION_CONNECT_FANS_MESSAGE = MessagingService.class.getName() + ".action.ConnectFansMessage";
     public static final String EXTRA_FAN_ADDRESSES         = MessagingService.class.getName() + ".extra.FanAddresses";
     
     public static final String ACTION_FIND_FANS_MESSAGE = MessagingService.class.getName() + ".action.FindFansMessage";
     public static final String EXTRA_REQUEST_ADDRESS    = MessagingService.class.getName() + ".extra.RequestAddress";
     
     public static final String ACTION_PAUSE_MESSAGE = MessagingService.class.getName() + ".action.PauseMessage";
     public static final String ACTION_PLAY_MESSAGE  = MessagingService.class.getName() + ".action.PlayMessage";
     public static final String ACTION_SKIP_MESSAGE  = MessagingService.class.getName() + ".action.SkipMessage";
 
     public static final String ACTION_LIBRARY_MESSAGE = MessagingService.class.getName() + ".action.LibraryMessage";
     public static final String EXTRA_SONG_METADATA    = MessagingService.class.getName() + ".extra.SongMetadata";
 
     public static final String ACTION_NEW_CONNECTED_USERS_MESSAGE = MessagingService.class.getName() + ".action.UserListMessage";
    public static final String EXTRA_USER_LIST                    = MessagingService.class.getName() + ".extra.UserList";
 
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
         this.messageDispatch.handleMessage(messageNo, message, fromAddr);
     }
 
     private void registerMessageHandlers() {
         this.messageDispatch = new MessageThreadMessageDispatch();
         registerStringMessageHandler();
         registerFindNewFansMessageHandler();
         registerLibraryMessageHandler();
         registerConnectFansMessageHandler();
         registerFoundFansHandler();
         registerPauseMessageHandler();
         registerPlayMessageHandler();
         registerSkipMessageHandler();
         registerUserListMessageHandler();
     }
 
     private void registerFoundFansHandler() {
         this.messageDispatch.registerHandler(FoundFansMessage.class, new IMessageHandler<FoundFansMessage>() {
 
             @Override
             public void handleMessage(int messageNo,
                     FoundFansMessage message, String fromAddr) {
                 new BroadcastIntent(ACTION_FOUND_FANS_MESSAGE)
                     .putParcelableArrayListExtra(EXTRA_FOUND_FANS, message.getFoundFans())
                     .send(MessagingService.this);
             }
         });
     }
 
     private void registerConnectFansMessageHandler() {
         this.messageDispatch.registerHandler(ConnectFansMessage.class, new IMessageHandler<ConnectFansMessage>() {
 
             @Override
             public void handleMessage(int messageNo,
                     ConnectFansMessage message, String fromAddr) {
                 new BroadcastIntent(ACTION_CONNECT_FANS_MESSAGE)
                     .putStringArrayListExtra(EXTRA_FAN_ADDRESSES, message.getAddresses())
                     .send(MessagingService.this);
             }
         });
     }
 
     private void registerFindNewFansMessageHandler() {
         this.messageDispatch.registerHandler(FindNewFansMessage.class, new IMessageHandler<FindNewFansMessage>() {
 
             @Override
             public void handleMessage(int messageNo,
                     FindNewFansMessage message, String fromAddr) {
                 new BroadcastIntent(ACTION_FIND_FANS_MESSAGE)
                     .putExtra(EXTRA_REQUEST_ADDRESS, fromAddr)
                     .send(MessagingService.this);
             }
         });
     }
 
     private void registerLibraryMessageHandler() {
         this.messageDispatch.registerHandler(LibraryMessage.class, new IMessageHandler<LibraryMessage>() {
 
             @Override
             public void handleMessage(int messageNo,
                     LibraryMessage message, String fromAddr) {
                 //sanity check...make sure the mac address is set properly, or
                 // raise a flag if its not
                 //JR, 03/13/13, I didn't want to set it here, because if we ever allow
                 // fans to send on other fans libraries, this would cause issues.  Rather
                 // how we want to handle that, or if that is even a thing, just check
                 // that our current assumptions are met and raise purgatory if not.
                 ArrayList<SongMetadata> remoteLibrary = new ArrayList<SongMetadata>();
                 for (SongMetadata meta : message.getLibrary()) {
                     if (!meta.getMacAddress().equals(fromAddr)) {
                         Log.wtf(TAG, "Song received from " + fromAddr + " with mac address " + meta.getMacAddress() + "\n" + meta.toString());
                         //continue on to the next one
                     } else {
                         remoteLibrary.add(meta);
                     }
                 }
 
                 new BroadcastIntent(ACTION_LIBRARY_MESSAGE)
                     .putParcelableArrayListExtra(EXTRA_SONG_METADATA, remoteLibrary)
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
 
     private void broadcastMessageToFans(IMessage msg) {
         try {
             this.connectServiceLocator.getService().broadcastMessageToFans(msg);
         } catch (ServiceNotBoundException e) {
             Log.wtf(TAG, e);
         }
     }
 
     private void sendMessageToHost(IMessage msg) {
         try {
             this.connectServiceLocator.getService().sendMessageToHost(msg);
         } catch (ServiceNotBoundException e) {
             Log.wtf(TAG, e);
         }
     }
     
     public void sendFindNewFansMessage() {
         FindNewFansMessage msg = new FindNewFansMessage();
         //send the message to the host
         sendMessageToHost(msg);
     }
 
     @Override
     public void sendLibraryMessage(List<SongMetadata> library) {
         LibraryMessage msg = new LibraryMessage(library);
         //send the message to the host
         sendMessageToHost(msg);
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
 
     public void sendStringMessage(String message) {
         StringMessage sm = new StringMessage();
         sm.setString(message);
         //JR, 03/02/12, TODO: the connection service should be changed to only deal with "connections".  The mode of connection will
         // be determined by which method is called initially (braodcastFan vs findNewFans), but after that point, it should just
         // work with connections
         try {
             //send the message to the host
             if (this.connectServiceLocator.getService().isHostConnected()) {
                 sendMessageToHost(sm);
             }
             
             if (this.connectServiceLocator.getService().isFanConnected()) {
                 broadcastMessageToFans(sm);
             }
         } catch (ServiceNotBoundException e) {
             Log.wtf(TAG, e);
         }
     }
     
     //sends the user list out to everyone
     public void sendUserListMessage(UserList userlist){
         UserListMessage ulm = new UserListMessage(userlist);
         broadcastMessageToFans(ulm);
     }
 }
