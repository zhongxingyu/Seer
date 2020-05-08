 package com.lastcrusade.fanclub.service;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import android.app.Service;
 import android.content.Intent;
 import android.os.Binder;
 import android.os.IBinder;
 import android.util.Log;
 
 import com.lastcrusade.fanclub.net.MessageThreadMessageDispatch;
 import com.lastcrusade.fanclub.net.MessageThreadMessageDispatch.IMessageHandler;
 import com.lastcrusade.fanclub.net.message.ConnectFansMessage;
 import com.lastcrusade.fanclub.net.message.FindNewFansMessage;
 import com.lastcrusade.fanclub.net.message.FoundFansMessage;
 import com.lastcrusade.fanclub.net.message.IMessage;
 import com.lastcrusade.fanclub.net.message.PauseMessage;
 import com.lastcrusade.fanclub.net.message.PlayMessage;
 import com.lastcrusade.fanclub.net.message.SkipMessage;
 import com.lastcrusade.fanclub.net.message.StringMessage;
 import com.lastcrusade.fanclub.service.ConnectionService.ConnectionServiceBinder;
 import com.lastcrusade.fanclub.util.BroadcastIntent;
 import com.lastcrusade.fanclub.util.BroadcastRegistrar;
 
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
         registerConnectFansMessageHandler();
         registerFoundFansHandler();
         registerPauseMessageHandler();
         registerPlayMessageHandler();
         registerSkipMessageHandler();
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
 
     private void broadcastMessageToFans(IMessage msg) {
         try {
             this.connectServiceLocator.getService().broadcastMessageToFans(msg);
         } catch (ServiceNotBoundException e) {
             Log.wtf(TAG, e);
         }
     }
 
     public void sendFindNewFansMessage() {
         FindNewFansMessage msg = new FindNewFansMessage();
         //send the message to the host
         sendMessageToHost(msg);
     }
 
     private void sendMessageToHost(IMessage msg) {
         try {
             this.connectServiceLocator.getService().sendMessageToHost(msg);
         } catch (ServiceNotBoundException e) {
             Log.wtf(TAG, e);
         }
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
 }
