 package de.fhb.mi.paperfly.chat;
 
 import android.app.Activity;
 import android.app.Fragment;
 import android.app.SearchManager;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.os.Bundle;
 import android.os.IBinder;
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
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.SearchView;
 import android.widget.TextView;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.List;
 
 import de.fhb.mi.paperfly.MainActivity;
 import de.fhb.mi.paperfly.PaperFlyApp;
 import de.fhb.mi.paperfly.R;
 import de.fhb.mi.paperfly.dto.AccountDTO;
 import de.fhb.mi.paperfly.dto.Message;
 import de.fhb.mi.paperfly.service.ChatService;
 
 /**
  * @author Christoph Ott
  */
 public class ChatFragment extends Fragment implements ChatService.MessageReceiver {
 
     public static final String TAG = ChatFragment.class.getSimpleName();
    public static final String TAG_GLOBAL = TAG + "_Global";
     public static final String TAG_ROOM = TAG + "Room";
     public static final String ARG_CHAT_ROOM = "chat_room";
     private View rootView;
     private ListView messagesList;
     private EditText messageInput;
     private ImageButton buSend;
     private ChatMessagesAdapter messagesAdapter;
     private DrawerLayout drawerLayout;
 
     private ListView drawerRightList;
 
     private ChatService chatService;
     private boolean boundChatService = false;
     private ServiceConnection connectionChatService = new ServiceConnection() {
 
         @Override
         public void onServiceConnected(ComponentName className, IBinder service) {
             // We've bound to LocalService, cast the IBinder and get LocalService instance
             ChatService.ChatServiceBinder binder = (ChatService.ChatServiceBinder) service;
             chatService = binder.getServiceInstance();
             boundChatService = true;
            String currentVisibleChatRoom = ((PaperFlyApp) getActivity().getApplication()).getCurrentVisibleChatRoom();
            chatService.connectToRoom(currentVisibleChatRoom, ChatFragment.this);
            if (currentVisibleChatRoom.equalsIgnoreCase(ChatService.ROOM_GLOBAL_NAME)) {
                messagesAdapter.addAll(chatService.getGlobalMessages());
            } else {
                messagesAdapter.addAll(chatService.getSpecificMessages());
             }
            messagesAdapter.notifyDataSetChanged();
         }
 
         @Override
         public void onServiceDisconnected(ComponentName arg0) {
             boundChatService = false;
         }
     };
 
     public ChatFragment() {
         super();
     }
 
     private void initViewsById() {
         messagesList = (ListView) this.rootView.findViewById(R.id.messagesList);
         messageInput = (EditText) this.rootView.findViewById(R.id.messageInput);
         buSend = (ImageButton) this.rootView.findViewById(R.id.buSend);
         drawerRightList = (ListView) this.drawerLayout.findViewById(R.id.right_drawer);
     }
 
     @Override
     public void onAttach(Activity activity) {
         super.onAttach(activity);
         Log.d(TAG, "onAttach");
     }
 
     @Override
     public void onChatConnected(List<AccountDTO> usersInChat) {
         List<String> drawerRightValues = new ArrayList<String>();
         for (AccountDTO accountDTO : usersInChat) {
             drawerRightValues.add(accountDTO.getUsername());
         }
         if (getActivity() != null) {
             final ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.drawer_list_item, drawerRightValues);
             getActivity().runOnUiThread(new Runnable() {
                 @Override
                 public void run() {
                     // This code will always run on the UI thread, therefore is safe to modify UI elements.
                     drawerRightList.setAdapter(adapter);
                 }
             });
 
         }
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setHasOptionsMenu(true);
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
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
 
         this.rootView = inflater.inflate(R.layout.fragment_chat, container, false);
         this.drawerLayout = (DrawerLayout) container.getParent();
         initViewsById();
 
         String room = getArguments().getString(ARG_CHAT_ROOM);
         if (room.equalsIgnoreCase(ChatService.ROOM_GLOBAL_NAME)) {
             getActivity().setTitle(ChatService.ROOM_GLOBAL_NAME);
         } else {
             getActivity().setTitle(room);
         }
         ((PaperFlyApp) getActivity().getApplication()).setCurrentVisibleChatRoom(room);
 
         messagesAdapter = new ChatMessagesAdapter(rootView.getContext());
 
         messageInput.addTextChangedListener(new TextWatcher() {
             @Override
             public void afterTextChanged(Editable s) {
             }
 
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
         });
 
         // make button not clickable
         buSend.setAlpha(0.5f);
         buSend.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 String currentVisibleRoom = ((PaperFlyApp) getActivity().getApplication()).getCurrentVisibleChatRoom();
                 if (currentVisibleRoom.equalsIgnoreCase(ChatService.RoomType.GLOBAL.name())) {
                     chatService.sendTextMessage(messageInput.getText().toString(), ChatService.RoomType.GLOBAL);
                 } else {
                     chatService.sendTextMessage(messageInput.getText().toString(), ChatService.RoomType.SPECIFIC);
                 }
 
                 messageInput.setText("");
             }
         });
         buSend.setClickable(false);
 
         return rootView;
     }
 
     @Override
     public void onDestroy() {
         super.onDestroy();
         Log.d(TAG, "onDestroy");
     }
 
     @Override
     public void onDetach() {
         super.onDetach();
         Log.d(TAG, "onDetach");
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.action_search_user:
                 return false;
             case R.id.action_show_persons:
                 ((MainActivity) getActivity()).openDrawerAndCloseOther(Gravity.RIGHT);
                 return true;
         }
         return super.onOptionsItemSelected(item);
     }
 
     @Override
     public void onPause() {
         super.onPause();
         Log.d(TAG, "onPause");
     }
 
     @Override
     public void onResume() {
         super.onResume();
         Log.d(TAG, "onResume");
 
         messagesAdapter = new ChatMessagesAdapter(rootView.getContext());
         messagesList.setAdapter(messagesAdapter);
     }
 
     @Override
     public void onStart() {
         super.onStart();
         Log.d(TAG, "onStart");
 
         Intent serviceIntent = new Intent(getActivity(), ChatService.class);
         getActivity().bindService(serviceIntent, connectionChatService, Context.BIND_AUTO_CREATE);
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
     public void receiveMessage(Message message) {
         Log.d(TAG, "receiveMessage");
         messagesAdapter.add(message);
         messagesAdapter.notifyDataSetChanged();
     }
 
     public class ChatMessagesAdapter extends ArrayAdapter<Message> {
         private final Context context;
 
         public ChatMessagesAdapter(Context context) {
             super(context, 0);
             this.context = context;
         }
 
         @Override
         public View getView(int position, View convertView, ViewGroup parent) {
             LayoutInflater inflater = (LayoutInflater) context
                     .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
             LinearLayout rowView = (LinearLayout) inflater.inflate(R.layout.chat_messages_time, parent, false);
             TextView textViewMessage = (TextView) rowView.findViewById(R.id.message);
             TextView textViewTime = (TextView) rowView.findViewById(R.id.time);
             SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
             final Message message = this.getItem(position);
             if (message.getUsername() != null) {
                 String actualUsername = ((PaperFlyApp) getActivity().getApplication()).getAccount().getUsername();
                 if (actualUsername.equals(message.getUsername())) {
                     textViewMessage.setText(message.getBody());
                     rowView.setGravity(Gravity.RIGHT);
                     rowView.setBackgroundResource(R.drawable.bubble_right);
                 } else {
                     textViewMessage.setText(message.getUsername() + ": " + message.getBody());
                     rowView.setGravity(Gravity.LEFT);
                     rowView.setBackgroundResource(R.drawable.bubble_left);
                 }
             } else {
                 textViewMessage.setText(message.getBody());
                 rowView.setGravity(Gravity.CENTER);
                 rowView.setBackgroundResource(R.drawable.bubble_system);
             }
             if (message.getSendTime() != null) {
                 textViewTime.setText(sdf.format(message.getSendTime()));
             }
 
             return rowView;
         }
     }
 }
