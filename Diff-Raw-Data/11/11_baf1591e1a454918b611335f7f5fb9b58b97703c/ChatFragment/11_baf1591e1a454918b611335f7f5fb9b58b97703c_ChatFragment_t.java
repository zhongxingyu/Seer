 package de.fhb.mi.paperfly.chat;
 
 import android.app.Activity;
 import android.app.Fragment;
 import android.app.SearchManager;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.graphics.Rect;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Messenger;
 import android.support.v4.widget.DrawerLayout;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ViewTreeObserver;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.ListView;
 import android.widget.SearchView;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonSyntaxException;
 
 import org.apache.http.cookie.Cookie;
 import org.apache.http.message.BasicNameValuePair;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import de.fhb.mi.paperfly.PaperFlyApp;
 import de.fhb.mi.paperfly.R;
 import de.fhb.mi.paperfly.dto.AccountDTO;
 import de.fhb.mi.paperfly.dto.Message;
 import de.fhb.mi.paperfly.dto.MessageType;
 import de.fhb.mi.paperfly.service.ChatService;
 import de.fhb.mi.paperfly.service.RestConsumerException;
 import de.fhb.mi.paperfly.service.RestConsumerSingleton;
 import de.fhb.mi.paperfly.util.AsyncDelegate;
 import de.tavendo.autobahn.WebSocketConnection;
 import de.tavendo.autobahn.WebSocketConnectionHandler;
 import de.tavendo.autobahn.WebSocketException;
 import de.tavendo.autobahn.WebSocketOptions;
 
 /**
  * @author Christoph Ott
  */
 public class ChatFragment extends Fragment implements AsyncDelegate, ChatService.MessageReceiverGlobal, ChatService.MessageReceiverSpecific {
 
     public static final String TAG = ChatFragment.class.getSimpleName();
     public static final String TAG_GLOBAL = TAG + "_Global";
     public static final String TAG_ROOM = TAG + "Room";
     public static final String ARG_CHAT_ROOM = "chat_room";
     public static long ROOM_GLOBAL = 1;
     public static String ROOM_GLOBAL_NAME = "Global";
     private final WebSocketConnection mConnection = new WebSocketConnection();
     private View rootView;
     private ListView messagesList;
     private EditText messageInput;
     private ImageButton buSend;
     private ArrayAdapter<String> messagesAdapter;
     private boolean globalRoom;
     private DrawerLayout drawerLayout;
 
     private GetAccountsInRoomTask mGetAccountsInRoomTask = null;
     private ListView drawerRightList;
 
     private ChatService chatService;
     private boolean boundChatService = false;
     private final Messenger messenger = new Messenger(new IncomingHandler());
     private ServiceConnection connectionChatService = new ServiceConnection() {
 
         @Override
         public void onServiceConnected(ComponentName className, IBinder service) {
             // We've bound to LocalService, cast the IBinder and get LocalService instance
             ChatService.ChatServiceBinder binder = (ChatService.ChatServiceBinder) service;
             chatService = binder.getServiceInstance();
             boundChatService = true;
             String currentVisibleChatRoom = ((PaperFlyApp) getActivity().getApplication()).getCurrentVisibleChatRoom();
             if (currentVisibleChatRoom.equalsIgnoreCase(ChatService.RoomType.GLOBAL.name())) {
 //                chatService.registerMessenger(messenger, ChatService.RoomType.GLOBAL);
                 chatService.setCurrentMessageReceiverGlobal(ChatFragment.this);
                 chatService.setCurrentMessageReceiverSpecific(null);
             } else {
                 chatService.connectToRoomChat(currentVisibleChatRoom);
 //                chatService.registerMessenger(messenger, ChatService.RoomType.SPECIFIC);
                 chatService.setCurrentMessageReceiverSpecific(ChatFragment.this);
                 chatService.setCurrentMessageReceiverGlobal(null);
             }
         }
 
         @Override
         public void onServiceDisconnected(ComponentName arg0) {
             boundChatService = false;
         }
     };
 
     @Override
     public void receiveMessageGlobal(String messageFromService) {
         Log.d(TAG, "receiveMessageGlobal");
         Gson gson = new Gson();
         Message message = gson.fromJson(messageFromService, Message.class);
         if (message.getUsername() != null) {
             messagesAdapter.add(message.getUsername() + ": " + message.getBody());
         } else {
             messagesAdapter.add(message.getBody());
         }
         messagesAdapter.notifyDataSetChanged();
     }
 
     @Override
     public void receiveMessageSpecific(String messageFromService) {
         Log.d(TAG, "receiveMessageGlobal");
         Gson gson = new Gson();
         Message message = gson.fromJson(messageFromService, Message.class);
         if (message.getUsername() != null) {
             messagesAdapter.add(message.getUsername() + ": " + message.getBody());
         } else {
             messagesAdapter.add(message.getBody());
         }
         messagesAdapter.notifyDataSetChanged();
     }
 
     class IncomingHandler extends Handler {
         @Override
         public void handleMessage(android.os.Message msg) {
             this.obtainMessage(msg.what);
             switch (msg.what) {
                 case ChatService.MSG_SEND_TO_UI:
                     String jsonMessage = msg.getData().getString(ChatService.ARGS_FROM_SERVICE);
                     Gson gson = new Gson();
                     Message message = gson.fromJson(jsonMessage, Message.class);
                     if (message.getUsername() != null) {
                         messagesAdapter.add(message.getUsername() + ": " + message.getBody());
                     } else {
                         messagesAdapter.add(message.getBody());
                     }
                     messagesAdapter.notifyDataSetChanged();
 
                     break;
                 default:
                     super.handleMessage(msg);
             }
         }
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setHasOptionsMenu(true);
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
 
         this.rootView = inflater.inflate(R.layout.fragment_chat, container, false);
         this.drawerLayout = (DrawerLayout) container.getParent();
         initViewsById();
 
         // checks if the keyboard is visible
         rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
             @Override
             public void onGlobalLayout() {
                 Rect r = new Rect();
                 //r will be populated with the coordinates of your view that area still visible.
                 rootView.getWindowVisibleDisplayFrame(r);
 
                 int heightDiff = rootView.getRootView().getHeight() - (r.bottom - r.top);
                 if (heightDiff > 100) { // if more than 100 pixels, its probably a keyboard...
                     drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                 } else {
                     drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                 }
             }
         });
 
         String room = getArguments().getString(ARG_CHAT_ROOM);
        if (room.equalsIgnoreCase(ROOM_GLOBAL_NAME)) {
             globalRoom = true;
             getActivity().setTitle(ROOM_GLOBAL_NAME);
         } else {
             globalRoom = false;
             getActivity().setTitle(room);
         }
         ((PaperFlyApp) getActivity().getApplication()).setCurrentVisibleChatRoom(room);
         mGetAccountsInRoomTask = new GetAccountsInRoomTask(this);
         mGetAccountsInRoomTask.execute();
 
         messagesAdapter = new ArrayAdapter<String>(rootView.getContext(), android.R.layout.simple_list_item_1);
 
         messageInput.addTextChangedListener(new TextWatcher() {
             @Override
             public void beforeTextChanged(CharSequence s, int start, int count, int after) {
             }
 
             @Override
             public void onTextChanged(CharSequence s, int start, int before, int count) {
                 if (s.length() > 0) {
                     buSend.setAlpha(1.0f);
                     buSend.setClickable(true);
                 } else {
                     buSend.setAlpha(0.5f);
                     buSend.setClickable(false);
                 }
             }
 
             @Override
             public void afterTextChanged(Editable s) {
             }
         });
 
         // make button not clickable
         buSend.setAlpha(0.5f);
         buSend.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Gson gson = new Gson();
                 Message message = new Message("heinz", MessageType.TEXT, new Date(), messageInput.getText().toString());
                 String currentVisibleRoom = ((PaperFlyApp) getActivity().getApplication()).getCurrentVisibleChatRoom();
                 if (currentVisibleRoom.equalsIgnoreCase(ChatService.RoomType.GLOBAL.name())) {
                     chatService.sendTextMessage(gson.toJson(message), ChatService.RoomType.GLOBAL);
                 } else {
                     chatService.sendTextMessage(gson.toJson(message), ChatService.RoomType.SPECIFIC);
                 }
 
                 messageInput.setText("");
             }
         });
         buSend.setClickable(false);
 
         return rootView;
     }
 
     @Override
     public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
         if (menu != null) {
             inflater.inflate(R.menu.chat, menu);
 
             SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
             SearchView searchView = (SearchView) menu.findItem(R.id.action_search_user).getActionView();
 
             // Get the menu item from the action bar
             MenuItem menuItem = menu.findItem(R.id.action_search_user);
             menuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
 
                 @Override
                 public boolean onMenuItemActionExpand(MenuItem item) {
                     Log.d(TAG, "Search activated. Locking drawers.");
                     drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                     return true;
                 }
 
                 @Override
                 public boolean onMenuItemActionCollapse(MenuItem item) {
                     Log.d(TAG, "Search deactivated. Unlocking drawers.");
                     drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                     return true;
                 }
             });
             // Assumes current activity is the searchable activity
             searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
         }
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.action_search_user:
                 return false;
             case R.id.action_show_persons:
                 openDrawerAndCloseOther(Gravity.RIGHT);
                 return true;
         }
         return super.onOptionsItemSelected(item);
     }
 
     /**
      * Opens the specified drawer and closes the other one, if it is visible
      *
      * @param drawerGravity the drawer to be opened
      */
     private void openDrawerAndCloseOther(int drawerGravity) {
         Log.d(TAG, "openDrawerAndCloseOther");
         // TODO duplicated from MainActivity
         switch (drawerGravity) {
             case Gravity.LEFT:
                 if (drawerLayout.isDrawerVisible(Gravity.LEFT)) {
                     drawerLayout.closeDrawer(Gravity.LEFT);
                 } else if (drawerLayout.isDrawerVisible(Gravity.RIGHT)) {
                     drawerLayout.closeDrawer(Gravity.RIGHT);
                     drawerLayout.openDrawer(Gravity.LEFT);
                 } else {
                     drawerLayout.openDrawer(Gravity.LEFT);
                 }
                 break;
             case Gravity.RIGHT:
                 if (drawerLayout.isDrawerVisible(Gravity.RIGHT)) {
                     drawerLayout.closeDrawer(Gravity.RIGHT);
                 } else if (drawerLayout.isDrawerVisible(Gravity.LEFT)) {
                     drawerLayout.closeDrawer(Gravity.LEFT);
                     drawerLayout.openDrawer(Gravity.RIGHT);
                 } else {
                     drawerLayout.openDrawer(Gravity.RIGHT);
                 }
                 break;
         }
     }
 
     @Override
     public void onStart() {
         super.onStart();
         Log.d(TAG, "onStart");
 //        connectToWebsocket(ChatService.URL_CHAT_BASE + ((PaperFlyApp) getActivity().getApplication()).getCurrentVisibleChatRoom());
 
         Intent serviceIntent = new Intent(getActivity(), ChatService.class);
         getActivity().bindService(serviceIntent, connectionChatService, Context.BIND_AUTO_CREATE);
     }
 
     @Override
     public void onDestroy() {
         super.onDestroy();
         Log.d(TAG, "onDestroy");
         mConnection.disconnect();
     }
 
     @Override
     public void onPause() {
         super.onPause();
         Log.d(TAG, "onPause");
     }
 
     @Override
     public void onStop() {
         super.onStop();
         Log.d(TAG, "onStop");
         if (boundChatService) {
             getActivity().unbindService(connectionChatService);
             boundChatService = false;
         }
     }
 
     @Override
     public void onDetach() {
         super.onDetach();
         Log.d(TAG, "onDetach");
     }
 
     @Override
     public void onAttach(Activity activity) {
         super.onAttach(activity);
         Log.d(TAG, "onAttach");
     }
 
     @Override
     public void onResume() {
         super.onResume();
         Log.d(TAG, "onResume");
 
         messagesAdapter = new ArrayAdapter<String>(rootView.getContext(), android.R.layout.simple_list_item_1, new ArrayList<String>());
         messagesList.setAdapter(messagesAdapter);
 
     }
 
     private void initViewsById() {
         messagesList = (ListView) this.rootView.findViewById(R.id.messagesList);
         messageInput = (EditText) this.rootView.findViewById(R.id.messageInput);
         buSend = (ImageButton) this.rootView.findViewById(R.id.buSend);
         drawerRightList = (ListView) this.drawerLayout.findViewById(R.id.right_drawer);
     }
 
     private void connectToWebsocket(final String wsuri) {
         List<BasicNameValuePair> headers = new ArrayList<BasicNameValuePair>();
         List<Cookie> cookies = ((PaperFlyApp) getActivity().getApplication()).getCookieStore().getCookies();
         for (Cookie cookie : cookies) {
             if (cookie.getName().equals("JSESSIONID")) {
                 Log.d(TAG, "Cookie: " + cookie.toString());
                 headers.add(new BasicNameValuePair("Cookie", cookie.getName() + "=" + cookie.getValue()));
             }
         }
 
         try {
             WebSocketConnectionHandler wsHandler = new WebSocketConnectionHandler() {
 
                 @Override
                 public void onOpen() {
                     Log.d(TAG, "Status: Connected to " + wsuri);
                 }
 
                 @Override
                 public void onTextMessage(String payload) {
                     Log.d(TAG, "Got payload: " + payload);
                     Gson gson = new Gson();
                     Message message = null;
                     try {
                         message = gson.fromJson(payload, Message.class);
                         if (message.getUsername() != null) {
                             messagesAdapter.add(message.getUsername() + ": " + message.getBody());
                         } else {
                             messagesAdapter.add(message.getBody());
                         }
                         messagesAdapter.notifyDataSetChanged();
                     } catch (JsonSyntaxException e) {
                         Log.e(TAG, e.getMessage());
                     }
                 }
 
                 @Override
                 public void onClose(int code, String reason) {
                     Log.d(TAG, "Connection lost.");
                 }
             };
             mConnection.connect(wsuri, null, wsHandler, new WebSocketOptions(), headers);
         } catch (WebSocketException e) {
             Log.d(TAG, e.toString());
         }
     }
 
     @Override
     public void asyncComplete(boolean success) {
         ArrayAdapter adapter = (ArrayAdapter) drawerRightList.getAdapter();
         adapter.clear();
         for (AccountDTO accountDTO : ((PaperFlyApp) getActivity().getApplication()).getUsersInRoom()) {
             adapter.add(accountDTO.getUsername());
         }
         adapter.notifyDataSetChanged();
     }
 
     /**
      * Represents an asynchronous GetAccountsInRoomTask used to get the accounts in a room
      */
     public class GetAccountsInRoomTask extends AsyncTask<String, Void, Boolean> {
 
         private AsyncDelegate delegate;
 
         public GetAccountsInRoomTask(AsyncDelegate delegate) {
             this.delegate = delegate;
         }
 
         @Override
         protected Boolean doInBackground(String... params) {
             List<AccountDTO> usersInRoom = null;
 
             try {
                 if (((PaperFlyApp) getActivity().getApplication()).getCurrentVisibleChatRoom().equals(ROOM_GLOBAL_NAME)) {
                     usersInRoom = RestConsumerSingleton.getInstance().getUsersInRoom(ROOM_GLOBAL);
                 } else {
                     usersInRoom = RestConsumerSingleton.getInstance().getUsersInRoom(((PaperFlyApp) getActivity().getApplication()).getActualRoom().getId());
                 }
 
                 ((PaperFlyApp) getActivity().getApplication()).setUsersInRoom(usersInRoom);
 
             } catch (RestConsumerException e) {
                 e.printStackTrace();
             }
 
             return usersInRoom != null;
         }
 
         @Override
         protected void onPostExecute(final Boolean success) {
             mGetAccountsInRoomTask = null;
 
             if (success) {
                 Log.d("onPostExecute", "success");
                 delegate.asyncComplete(true);
             }
 
         }
     }
 }
