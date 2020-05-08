 package com.angrykings.activities;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ListView;
 
 import com.angrykings.Action;
 import com.angrykings.pregame.LobbyPlayer;
 import com.angrykings.pregame.LobbyPlayerAdapter;
 import com.angrykings.R;
 import com.angrykings.ServerConnection;
 import com.angrykings.utils.ServerMessage;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class RunningGamesActivity extends Activity{
 
     private ListView runningGamesList;
     private String username;
     private List<LobbyPlayer> runningGames;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         setContentView(R.layout.activity_runninggames);
         runningGamesList = (ListView) findViewById(R.id.runningGamesList);
 
         runningGames = new ArrayList<LobbyPlayer>();
 
         Bundle extras = getIntent().getExtras();
         if(extras != null){
             this.username = extras.getString("username");
         }
 
         ServerConnection
                 .getInstance()
                 .sendTextMessage(ServerMessage.getRunningGames());
 
         runningGamesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                 challengePlayer(runningGames.get(position));
             }
         });
 
         updateRunningGames(runningGames);
 
         ServerConnection.getInstance().setHandler(new ServerConnection.OnMessageHandler() {
 
             @Override
             public void onMessage(String payload) {
                 try {
                     final JSONObject jObj = new JSONObject(payload);
                     if (jObj.getInt("action") == Action.Server.GAMES) {
                         Log.d("AngryKings maan", "received running Games update: " + jObj.get("games"));
 
                         JSONArray userArray = new JSONArray(jObj.getString("games"));
                         runningGames.clear();
 
                         for (int i = 0; i < userArray.length(); i++) {
                             final JSONObject jsonObject = userArray.getJSONObject(i);
 
                             runningGames.add(new LobbyPlayer(jsonObject.getString("name"),
                                     jsonObject.getInt("public_id"),
                                     "0",
                                     "0"));
                         }
 
                         updateRunningGames(runningGames);
                     }
                 } catch (JSONException e) {
                     Log.e("JSON Parser", "Error parsing data " + e.toString());
                 }
             }
         });
     }
 
     private void updateRunningGames(List<LobbyPlayer> runningGames) {
         runningGamesList.setAdapter(new LobbyPlayerAdapter(this, R.layout.list_row, runningGames));
     }
 
     private void challengePlayer(final LobbyPlayer partner) {
         new AlertDialog.Builder(RunningGamesActivity.this)
                 .setTitle("Challenge")
                 .setMessage("Mit " + partner.name + " weiterspielen?")
                 .setPositiveButton("Klar!",
                         new DialogInterface.OnClickListener() {
 
                             @Override
                             public void onClick(final DialogInterface dialog, final int which) {
                                Intent intent = new Intent(RunningGamesActivity.this, OnlineGameActivity.class);
                                intent.putExtra("partnerId", String.valueOf(partner.id));
                                startActivity(intent);
                             }
                         })
                 .setNegativeButton("Lieber nicht",
                         new DialogInterface.OnClickListener() {
 
                             @Override
                             public void onClick(DialogInterface dialog, int which) {
                                 dialog.cancel();
                             }
                         }).show();
     }
 }
