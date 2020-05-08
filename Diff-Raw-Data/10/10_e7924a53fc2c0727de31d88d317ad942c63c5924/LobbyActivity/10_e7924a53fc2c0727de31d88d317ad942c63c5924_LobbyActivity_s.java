 package com.angrykings.activities;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 
 import com.angrykings.Action;
 import com.angrykings.ServerConnection;
 import com.angrykings.ServerConnection.OnMessageHandler;
 import com.angrykings.utils.ServerJSONBuilder;
 
 public class LobbyActivity extends ListActivity {
 
 	private String username;
 	private List<String> users;
 	private Map<String, String> listItemToName = new HashMap<String, String>();
 
 	private void updateLobby(List<String> user) {
 		setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, user));
 	}
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		Bundle extras = getIntent().getExtras();
 		users = new ArrayList<String>();
 		if (extras != null) {
 			this.username = extras.getString("username");
 		}
 		ServerConnection
 				.getInstance()
 				.getConnection()
 				.sendTextMessage(
 						new ServerJSONBuilder().create(
 								Action.Client.GO_TO_LOBBY).build());
 		displayLobby();
 	}
 
 	private void displayLobby() {
 		updateLobby(users);
 		getListView().setTextFilterEnabled(true);
 		ServerConnection.getInstance().setHandler(new OnMessageHandler() {
 
 			@Override
 			public void onMessage(String payload) {
 				try {
					JSONObject jObj = new JSONObject(payload);
 					if (jObj.getInt("action") == Action.Server.REQUEST) {
 						new AlertDialog.Builder(LobbyActivity.this)
 								.setTitle("Request")
 								.setMessage(
 										jObj.getString("partner")
 												+ " requested a match!")
 								.setPositiveButton("Okay",
 										new DialogInterface.OnClickListener() {
 
 											public void onClick(
 													DialogInterface dialog,
 													int which) {
 												ServerConnection
 														.getInstance()
 														.getConnection()
 														.sendTextMessage(
 																new ServerJSONBuilder()
 																		.create(Action.Client.ACCEPT)
 																		.build());
 												Intent intent = new Intent(
 														LobbyActivity.this,
 														OnlineGameActivity.class);
 												intent.putExtra("myTurn", true);
 												intent.putExtra("username",
 														username);
 												startActivity(intent);
 											}
 										})
 								.setNegativeButton("Deny",
 										new DialogInterface.OnClickListener() {
 
 											public void onClick(
 													DialogInterface dialog,
 													int which) {
 												ServerConnection
 														.getInstance()
 														.getConnection()
 														.sendTextMessage(
 																new ServerJSONBuilder()
 																		.create(Action.Client.DENY)
 																		.build());
 											}
 										}).show();
 					} else if (jObj.getInt("action") == Action.Server.LOBBY_UPDATE) {
 						Log.d("AngryKings", "received lobby update: "+jObj.get("names"));
 
 						JSONArray userArray = new JSONArray(jObj .getString("names"));
 						users.clear();
 
 						for (int i = 0; i < userArray.length(); i++) {
 							String eingabe = userArray.getJSONArray(i)
 									.getString(0)
 									+ "   Gewonnen: "
 									+ userArray.getJSONArray(i).getString(1)
 									+ "   Verloren: "
 									+ userArray.getJSONArray(i).getString(2);
 
 							users.add(eingabe);
 							listItemToName.put(eingabe, userArray.getJSONArray(i).getString(0));
 						}
 
 						updateLobby(users);
 					}
 				} catch (JSONException e) {
 					Log.e("JSON Parser", "Error parsing data " + e.toString());
 				}
 			}
 		});
 	}
 
 	@Override
 	protected void onListItemClick(final ListView l, final View v, final int position, final long id) {
 		super.onListItemClick(l, v, position, id);
 
 		final String partnerName = listItemToName.get(
 				getListView().getItemAtPosition(
 						position).toString());
 		
 		final AlertDialog dialog = new AlertDialog.Builder(this)
 				.setTitle("Please Wait").setMessage("Waiting for partner")
 				.show();
 
 		ServerConnection.getInstance().setHandler(new OnMessageHandler() {
 
 			@Override
 			public void onMessage(final String payload) {
 				try {
 					final JSONObject jObj = new JSONObject(payload);
 					if (jObj.getInt("action") == Action.Server.DENIED) {
 						dialog.cancel();
 						displayLobby();
 					} else {
 						dialog.dismiss();
 						Intent intent = new Intent(LobbyActivity.this, OnlineGameActivity.class);
 						intent.putExtra("myTurn", false)
 						.putExtra("username", username)
 						.putExtra("partnername", partnerName)
 						.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
 						startActivity(intent);
 					}
 				} catch (final JSONException e) {
 					e.printStackTrace();
 				}
 			}
 		});
 
 		ServerConnection
 				.getInstance()
 				.getConnection()
 				.sendTextMessage(
 						new ServerJSONBuilder()
 								.create(Action.Client.PAIR)
 								.option("partner",partnerName).build());
 
 	}
 }
