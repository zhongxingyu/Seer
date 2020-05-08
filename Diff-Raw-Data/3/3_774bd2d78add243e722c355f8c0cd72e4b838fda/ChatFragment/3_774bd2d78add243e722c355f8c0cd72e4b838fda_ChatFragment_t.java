 package de.fhb.mi.paperfly.fragments;
 
 import android.app.Activity;
 import android.app.Fragment;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.*;
 import de.fhb.mi.paperfly.PaperFlyApp;
 import de.fhb.mi.paperfly.R;
 import de.fhb.mi.paperfly.auth.AuthHelper;
 import de.tavendo.autobahn.WebSocketConnection;
 import de.tavendo.autobahn.WebSocketConnectionHandler;
 import de.tavendo.autobahn.WebSocketException;
 import de.tavendo.autobahn.WebSocketOptions;
 
 import java.util.List;
 
 /**
  * @author Christoph Ott
  */
 public class ChatFragment extends Fragment {
 
     public static final String TAG = "ChatFragment";
     public static final String ARG_CHAT_ROOM = "chat_room";
     public static String ROOM_GLOBAL = "Global";
     private final WebSocketConnection mConnection = new WebSocketConnection();
     private View rootView;
     private ListView messagesList;
     private EditText messageInput;
     private ImageButton buSend;
     private ArrayAdapter<String> messagesAdapter;
     private boolean globalRoom;
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
 
         this.rootView = inflater.inflate(R.layout.fragment_chat, container, false);
         initViewsById();
 
         String room = getArguments().getString(ARG_CHAT_ROOM);
         if (room == ROOM_GLOBAL) {
             globalRoom = true;
         } else {
             globalRoom = false;
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
                 String message = "{'text': '" + messageInput.getText().toString() + "'}";
                 mConnection.sendTextMessage(message);
                 messageInput.setText("");
             }
         });
         buSend.setClickable(false);
 
 //        messagesList.setAdapter(messagesAdapter);
         messagesList.setStackFromBottom(true);
         messagesList.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
 
 
         return rootView;
     }
 
     @Override
     public void onStart() {
         super.onStart();
         Log.d(TAG, "onStart");
         connectToWebsocket(AuthHelper.URL_CHAT_GLOBAL);
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
 
         List<String> chatList;
         if (globalRoom) {
             chatList = ((PaperFlyApp) getActivity().getApplication()).getChatGlobal();
         } else {
             chatList = ((PaperFlyApp) getActivity().getApplication()).getChatRoom();
         }
         messagesAdapter = new ArrayAdapter<String>(rootView.getContext(), android.R.layout.simple_list_item_1, chatList);
         messagesList.setAdapter(messagesAdapter);
         messageInput.requestFocus();
     }
 
     private void initViewsById() {
         messagesList = (ListView) this.rootView.findViewById(R.id.messagesList);
         messageInput = (EditText) this.rootView.findViewById(R.id.messageInput);
         buSend = (ImageButton) this.rootView.findViewById(R.id.buSend);
     }
 
     private void connectToWebsocket(final String wsuri) {
         WebSocketOptions asd = new WebSocketOptions();
         try {
             mConnection.connect(wsuri, new WebSocketConnectionHandler() {
 
                 @Override
                 public void onOpen() {
                     Log.d(TAG, "Status: Connected to " + wsuri);
                 }
 
                 @Override
                 public void onTextMessage(String payload) {
                     Log.d(TAG, "Got payload: " + payload);
                     messagesAdapter.add(payload);
                     messagesAdapter.notifyDataSetChanged();
                 }
 
                 @Override
                 public void onClose(int code, String reason) {
                     Toast.makeText(rootView.getContext(), "Connection lost.", Toast.LENGTH_LONG).show();
                     Log.d(TAG, "Connection lost.");
                 }
             });
         } catch (WebSocketException e) {
             Log.d(TAG, e.toString());
         }
     }
 }
