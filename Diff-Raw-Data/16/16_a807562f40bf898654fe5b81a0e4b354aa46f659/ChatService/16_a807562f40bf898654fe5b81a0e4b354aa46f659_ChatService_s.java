 package de.fhb.mi.paperfly.service;
 
 import android.app.Service;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Binder;
 import android.os.Handler;
 import android.os.IBinder;
 import android.util.Log;
 
 import com.google.gson.Gson;
 
 import org.apache.http.cookie.Cookie;
 import org.apache.http.message.BasicNameValuePair;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import de.fhb.mi.paperfly.PaperFlyApp;
 import de.fhb.mi.paperfly.dto.AccountDTO;
 import de.fhb.mi.paperfly.dto.Message;
 import de.fhb.mi.paperfly.dto.MessageType;
 import de.fhb.mi.paperfly.dto.RoomDTO;
 import de.tavendo.autobahn.WebSocketConnection;
 import de.tavendo.autobahn.WebSocketConnectionHandler;
 import de.tavendo.autobahn.WebSocketException;
 import de.tavendo.autobahn.WebSocketOptions;
 
 /**
  * A Service which holds the connections to two chats. These chats are connected through a webSocket.
  * On startup the service will connect to the global chat.
  * <p/>
  * If the user wants to join another chat you should call {@link de.fhb.mi.paperfly.service.ChatService#connectToRoom(String, de.fhb.mi.paperfly.service.ChatService.MessageReceiver)}.
  *
  * @author Christoph Ott
  */
 public class ChatService extends Service {
 
     public static final String URL_CHAT_BASE = "ws://" + RestConsumerSingleton.AWS_IP + ":" + RestConsumerSingleton.PORT + "/PaperFlyServer-web/ws/chat/";
     private static final String TAG = ChatService.class.getSimpleName();
     private static final int UPDATE_INTERVAL = 1000 * 30;
     public static String ROOM_GLOBAL_NAME = "Global";
     public static final String URL_CHAT_GLOBAL = URL_CHAT_BASE + ROOM_GLOBAL_NAME;
     public static long ROOM_GLOBAL_ID = 1l;
     IBinder binder = new ChatServiceBinder();
     private WebSocketConnection globalConnection = null;
     private WebSocketConnection roomConnection = new WebSocketConnection();
     private String webSocketUriSpecificRoom = "";
     private RoomDTO actualRoom;
 
     private MessageReceiver currentMessageReceiverGlobal;
     private MessageReceiver currentMessageReceiverSpecific;
 
     private Handler globalHandler = new Handler();
     private Handler specificHandler = new Handler();
 
     private Runnable globalRunnable = new GlobalRunnable();
     private Runnable specificRunnable = new SpecificRunnable();
 
     private List<AccountDTO> usersInGlobal = new ArrayList<AccountDTO>();
     private List<AccountDTO> usersInSpecific = new ArrayList<AccountDTO>();
 
     private Timer globalTimer;
     private Timer specificTimer;
 
     private boolean globalTimerRunning = false;
     private boolean specificTimerRunning = false;
 
     private boolean connectToGlobal() {
         if (globalConnection == null) {
             try {
                 globalConnection = new WebSocketConnection();
                 globalConnection.connect(URL_CHAT_GLOBAL, null, new MyWebSocketConnectionHandler(URL_CHAT_GLOBAL, RoomType.GLOBAL), new WebSocketOptions(), createHeaders());
             } catch (WebSocketException e) {
                 e.printStackTrace();
                 return false;
             }
         } else if (!globalConnection.isConnected()) {
             globalConnection.reconnect();
         } else if (!globalTimerRunning) {
             startGlobalTimer();
         }
         return true;
     }
 
     /**
      * This method connects the {@link de.fhb.mi.paperfly.service.ChatService} to the given room and
      * forwards the messages from the connection to the messageReceiver.
      *
      * @param room            the room to connect to and get the messages to the {@link de.fhb.mi.paperfly.service.ChatService.MessageReceiver}
      * @param messageReceiver the messageReceiver which sends the messages to the ui
      *
      * @return true if the connection is established, false if not
      */
     public boolean connectToRoom(final String room, MessageReceiver messageReceiver) {
         RoomType roomType = (room.equalsIgnoreCase(RoomType.GLOBAL.name())) ? RoomType.GLOBAL : RoomType.SPECIFIC;
         boolean success = false;
         switch (roomType) {
             case GLOBAL:
                 this.currentMessageReceiverGlobal = messageReceiver;
                 success = connectToGlobal();
                 break;
             case SPECIFIC:
                 this.currentMessageReceiverSpecific = messageReceiver;
                 success = connectToSpecific(room);
                 actualRoom = ((PaperFlyApp) getApplication()).getActualRoom();
                 break;
         }
         return success;
     }
 
     private boolean connectToSpecific(String room) {
         String webSocketUri = URL_CHAT_BASE + room;
         if (!webSocketUriSpecificRoom.equals(webSocketUri)) {
             // connect to new room
             if (roomConnection.isConnected()) {
                 // if there was a connection to another room close it
                 roomConnection.disconnect();
             }
             try {
                 roomConnection = new WebSocketConnection();
                 roomConnection.connect(webSocketUri, null, new MyWebSocketConnectionHandler(webSocketUri, RoomType.SPECIFIC), new WebSocketOptions(), createHeaders());
             } catch (WebSocketException e) {
                 e.printStackTrace();
                 return false;
             }
             webSocketUriSpecificRoom = webSocketUri;
         } else if (!roomConnection.isConnected()) {
             // room is the same but connection was lost
             roomConnection.reconnect();
         } else if (!specificTimerRunning) {
             startSpecificTimer();
         }
         return true;
     }
 
     private List<BasicNameValuePair> createHeaders() {
         List<BasicNameValuePair> headers = new ArrayList<BasicNameValuePair>();
         List<Cookie> cookies = ((PaperFlyApp) getApplication()).getCookieStore().getCookies();
         for (Cookie cookie : cookies) {
             if (cookie.getName().equals("JSESSIONID")) {
                 Log.d(TAG, "Cookie: " + cookie.toString());
                 headers.add(new BasicNameValuePair("Cookie", cookie.getName() + "=" + cookie.getValue()));
             }
         }
         return headers;
     }
 
     /**
      * Method for getting the current users in the given room.
      * Should only be called if the chat is connected.
      *
      * @param roomType the chat type you want the online users
      *
      * @return a list of accounts who are online in the chat
      */
     public List<AccountDTO> getUsersInRoom(RoomType roomType) {
         if (roomType == RoomType.GLOBAL) {
             return usersInGlobal;
         } else if (roomType == RoomType.SPECIFIC) {
             return usersInSpecific;
         } else {
             return new ArrayList<AccountDTO>();
         }
     }
 
     @Override
     public IBinder onBind(Intent intent) {
         return binder;
     }
 
     @Override
     public void onDestroy() {
         Log.d(TAG, "onDestroy");
         if (globalConnection.isConnected()) {
             globalConnection.disconnect();
         }
         if (globalTimerRunning) {
             globalTimer.cancel();
         }
         if (roomConnection.isConnected()) {
             roomConnection.disconnect();
         }
         if (specificTimerRunning) {
             specificTimer.cancel();
         }
     }
 
     @Override
     public int onStartCommand(Intent intent, int flags, int startId) {
         Log.d(TAG, "onStartCommand");
 
         return super.onStartCommand(intent, flags, startId);
     }
 
     @Override
     public boolean onUnbind(Intent intent) {
         globalTimer.cancel();
         globalTimer.cancel();
         globalTimerRunning = false;
         specificTimerRunning = false;
         currentMessageReceiverGlobal = null;
         currentMessageReceiverSpecific = null;
         return true;
     }
 
     /**
      * Sends a message to the current visible room.
      *
      * @param messageFromUI the message to send
      * @param roomType      the roomType the message should be send
      */
     public void sendTextMessage(String messageFromUI, RoomType roomType) {
         Gson gson = new Gson();
         Message message = new Message("", MessageType.TEXT, new Date(), messageFromUI);
         switch (roomType) {
             case GLOBAL:
                 globalConnection.sendTextMessage(gson.toJson(message));
                 break;
             case SPECIFIC:
                 roomConnection.sendTextMessage(gson.toJson(message));
                 break;
         }
     }
 
     private void startGlobalTimer() {
         globalTimer = new Timer();
         TimerTask doAsynchronousTask = new TimerTask() {
             @Override
             public void run() {
                 globalRunnable = new GlobalRunnable();
                 globalHandler.post(globalRunnable);
             }
         };
         globalTimer.schedule(doAsynchronousTask, 0, UPDATE_INTERVAL);
         globalTimerRunning = true;
     }
 
     private void startSpecificTimer() {
         specificTimer = new Timer();
         TimerTask doAsynchronousTask = new TimerTask() {
             @Override
             public void run() {
                 specificRunnable = new SpecificRunnable();
                 specificHandler.post(specificRunnable);
             }
         };
         specificTimer.schedule(doAsynchronousTask, 0, UPDATE_INTERVAL);
         specificTimerRunning = true;
     }
 
     /**
      * Represents the type of a room.
      */
     public enum RoomType {
         GLOBAL, SPECIFIC
     }
 
     /**
      * An Interface for receiving messages on the ui.
      */
     public interface MessageReceiver {
 
         /**
          * This method is called when the current chat is connected.
          *
          * @param usersInRoom the users in the room
          */
         void onChatConnected(List<AccountDTO> usersInRoom);
 
         /**
          * Gets a message for further processing. (e.g. Displaying on the UI)
          *
          * @param message the message to process
          */
         void receiveMessage(String message);
     }
 
     public class GetAccountsInRoomTask extends AsyncTask<String, Void, Boolean> {
 
         private long roomID;
 
         public GetAccountsInRoomTask(long roomID) {
             this.roomID = roomID;
         }
 
         @Override
         protected Boolean doInBackground(String... params) {
             try {
                usersInGlobal = RestConsumerSingleton.getInstance().getUsersInRoom(roomID);
                currentMessageReceiverGlobal.onChatConnected(usersInGlobal);
             } catch (RestConsumerException e) {
                 Log.e(TAG, e.getMessage(), e);
             }
             return true;
         }
     }
 
     /**
      * A Binder for the BackgroundLocationService
      *
      * @see android.os.Binder
      */
     public class ChatServiceBinder extends Binder {
         public ChatService getServiceInstance() {
             return ChatService.this;
         }
     }
 
     private class MyWebSocketConnectionHandler extends WebSocketConnectionHandler {
 
         private final String webSocketUri;
         private final RoomType roomType;
 
         public MyWebSocketConnectionHandler(String webSocketUri, RoomType roomType) {
             this.webSocketUri = webSocketUri;
             this.roomType = roomType;
         }
 
         @Override
         public void onClose(int code, String reason) {
             Log.d(TAG, "Connection lost to: " + webSocketUri);
         }
 
         @Override
         public void onOpen() {
             Log.d(TAG, "Status: Connected to " + webSocketUri);
             switch (roomType) {
                 case GLOBAL:
                     startGlobalTimer();
                     break;
                 case SPECIFIC:
                     startSpecificTimer();
                     break;
             }
         }
 
         @Override
         public void onTextMessage(String messageJSON) {
             Log.d(TAG, "Got message: " + messageJSON);
             Gson gson = new Gson();
             SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
             Message message = gson.fromJson(messageJSON, Message.class);
             String acutalMessageToUI;
             if (message.getUsername() != null) {
                acutalMessageToUI = "["+sdf.format(message.getSendTime())+"] "+message.getUsername() + ": " + message.getBody();
             } else {
                 acutalMessageToUI = message.getBody();
             }
             switch (roomType) {
                 case GLOBAL:
                     if (currentMessageReceiverGlobal != null) {
                         currentMessageReceiverGlobal.receiveMessage(acutalMessageToUI);
                     }
                     break;
                 case SPECIFIC:
                     if (currentMessageReceiverSpecific != null) {
                         currentMessageReceiverSpecific.receiveMessage(acutalMessageToUI);
                     }
                     break;
             }
         }
     }
 
     private class GlobalRunnable implements Runnable {
         @Override
         public void run() {
             GetAccountsInRoomTask getAccountsInRoomTask = new GetAccountsInRoomTask(ROOM_GLOBAL_ID);
             getAccountsInRoomTask.execute();
         }
     }
 
     private class SpecificRunnable implements Runnable {
         @Override
         public void run() {
             if (actualRoom != null) {
                 GetAccountsInRoomTask getAccountsInRoomTask = new GetAccountsInRoomTask(actualRoom.getId());
                 getAccountsInRoomTask.execute();
             }
         }
     }
 }
