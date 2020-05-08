 package com.example.shutapp;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.app.ListActivity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 
 public class NearbyConversationsActivity extends Activity implements OnItemClickListener{
 
 	public final static String EXTRA_MESSAGE = "com.example.shutapp.MESSAGE";
 	//private List<Chatroom> nearbyCR;
 	private ArrayAdapter<String> adapter;
 	private List<String> nearbyCRnames;
 	private String clickedChatroom = "No chatroom";
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_nearby_conversations);
 		nearbyCRnames = new ArrayList<String>();
 		createArrayAdapter();
 
 
 	}
 	
 	
 	
 	public void addChatroom(String name){
 		Chatroom cr = new Chatroom(name);
 		Chatrooms.add(cr.getName(), cr);
 		nearbyCRnames.add(name);
 	}
 
 
 	private void createArrayAdapter() {
 		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, nearbyCRnames);
 		ListView listView = (ListView) findViewById(R.id.listOfNearbyConversations);
 		listView.setAdapter(adapter);
 
 
 		listView.setOnItemClickListener(this); 
 	}
 
 
 	public void toNearbyConversationsActivity(View view){
 		Intent intent = new Intent(this, NearbyConversationsActivity.class);
 		startActivity(intent);
 		overridePendingTransition(0, 0);
 	}
 
 	public void toChatActivity(View view){
 		Intent intentToRedirect = new Intent(this, ChatActivity.class);
 		intentToRedirect.putExtra(EXTRA_MESSAGE, clickedChatroom);
 		startActivity(intentToRedirect);
 		overridePendingTransition(0, 0);
 	}
 
 	public void toGpsActivity(View view){
 		Intent intentToRedirect = new Intent(this, GpsActivity.class);
 		startActivity(intentToRedirect);
 		overridePendingTransition(0, 0);
 	}
 
 	public void toSettingsActivity(View view){
 		Intent intent = new Intent(this, SettingsActivity.class);
 		startActivity(intent);
 		overridePendingTransition(0, 0);
 	}
 
 	public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
 		Log.d("listan", "you clicked item " + position);
 		clickedChatroom = nearbyCRnames.get(position);
 		toChatActivity(view);
 		
 	}
 	
 	public void showCreateChatroomDialog(View view){
 		final Dialog newChatroomDialog = new Dialog(this);
 		newChatroomDialog.setContentView(R.layout.create_chatroom_dialog);
 		final EditText etChatroomInput = (EditText) newChatroomDialog.findViewById(R.id.chatroom_name);
 		
 		Button btnDialogCreateChatroom = (Button) newChatroomDialog.findViewById(R.id.create_chatroom);
 		btnDialogCreateChatroom.setOnClickListener(new OnClickListener() {
 			
 			public void onClick(View view){
				addChatroom(etChatroomInput.getText().toString());
 				newChatroomDialog.cancel();
 				
 			}
 		});
 		newChatroomDialog.show();
 	}
 	
 
 	
 
 }
