 package de.fhb.mi.paperfly.chat;
 
 import android.app.Activity;
 import android.app.Fragment;
 import android.app.SearchManager;
 import android.content.Context;
 import android.os.AsyncTask;
 import android.os.Bundle;
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
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.ListView;
 import android.widget.SearchView;
 import android.widget.Toast;
 
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
 import de.fhb.mi.paperfly.service.RestConsumerException;
 import de.fhb.mi.paperfly.service.RestConsumerSingleton;
 import de.tavendo.autobahn.WebSocketConnection;
 import de.tavendo.autobahn.WebSocketConnectionHandler;
 import de.tavendo.autobahn.WebSocketException;
 import de.tavendo.autobahn.WebSocketOptions;
 
 /**
  * @author Christoph Ott
  */
 public class ChatFragment extends Fragment {
 
     public static final String TAG = ChatFragment.class.getSimpleName();
     public static final String TAG_GLOBAL = TAG + "_Global";
     public static final String TAG_ROOM = TAG + "Room";
     public static final String ARG_CHAT_ROOM = "chat_room";
     public static String ROOM_GLOBAL = "1";
     private final WebSocketConnection mConnection = new WebSocketConnection();
     private View rootView;
     private ListView messagesList;
     private EditText messageInput;
     private ImageButton buSend;
     private ArrayAdapter<String> messagesAdapter;
     private boolean globalRoom;
     private DrawerLayout drawerLayout;
 
     private GetAccountsInRoomTask mGetAccountsInRoomTask = null;
     private String room = ROOM_GLOBAL;
 
 
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
 
         room = getArguments().getString(ARG_CHAT_ROOM);
 
         if (room.equals(ROOM_GLOBAL)) {
             globalRoom = true;
             getActivity().setTitle(ROOM_GLOBAL);
         } else {
             globalRoom = false;
             getActivity().setTitle(room);
         }
 
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
                 mConnection.sendTextMessage(gson.toJson(message));
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
         connectToWebsocket(RestConsumerSingleton.URL_CHAT_GLOBAL);
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
 
        ((PaperFlyApp) getActivity().getApplication()).setCurrentChatRoomID(room);
        mGetAccountsInRoomTask = new GetAccountsInRoomTask();
        mGetAccountsInRoomTask.execute();
     }
 
     private void initViewsById() {
         messagesList = (ListView) this.rootView.findViewById(R.id.messagesList);
         messageInput = (EditText) this.rootView.findViewById(R.id.messageInput);
         buSend = (ImageButton) this.rootView.findViewById(R.id.buSend);
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
                         messagesAdapter.add(message.getBody());
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
 
     /**
      * Represents an asynchronous GetAccountsInRoomTask used to get the accounts in a room
      */
     public class GetAccountsInRoomTask extends AsyncTask<String, Void, Boolean> {
 
         @Override
         protected Boolean doInBackground(String... params) {
             List<AccountDTO> usersInRoom = null;
 
             try {
                 String roomID = ((PaperFlyApp) getActivity().getApplication()).getCurrentChatRoomID();
                 usersInRoom = RestConsumerSingleton.getInstance().getUsersInRoom(roomID);
 
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
                 Toast.makeText(getActivity().getApplicationContext(), "UserInRoom successful!", Toast.LENGTH_SHORT).show();
             }
 
         }
     }
 }
