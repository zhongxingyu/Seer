 /**
  * Copyright (C) 2012 Jesse Wilson
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.publicobject.rounds;
 
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.text.method.LinkMovementMethod;
 import android.view.View;
 import android.widget.Button;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.EditText;
 import android.widget.RadioButton;
 import android.widget.TextView;
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.MenuItem;
 import com.google.gson.stream.JsonWriter;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.net.HttpURLConnection;
 import java.net.URL;
 
 public final class ShareActivity extends SherlockActivity {
     private static final String POST_URL = "http://www.roundsapp.com/post";
     // private static final String POST_URL = "http://192.168.1.221:8080/post";
 
     private Game game;
     private GameSaver gameSaver;
     private GameSummarizer gameSummarizer;
 
     private TextView messagePreview;
     private Button share;
     private EditText gameName;
     private RadioButton highScoreWins;
     private RadioButton lowScoreWins;
 
     @Override protected void onCreate(Bundle savedState) {
         super.onCreate(savedState);
 
         GameDatabase database = GameDatabase.getInstance(getApplicationContext());
         Intent intent = getIntent();
         String gameId = savedState != null
                 ? savedState.getString(IntentExtras.GAME_ID)
                 : intent.getStringExtra(IntentExtras.GAME_ID);
         game = database.get(gameId);
         gameSaver = new GameSaver(database, game);
 
         View layout = getLayoutInflater().inflate(R.layout.share, null);
         gameName = (EditText) layout.findViewById(R.id.gameName);
         highScoreWins = (RadioButton) layout.findViewById(R.id.highScoreWins);
         lowScoreWins = (RadioButton) layout.findViewById(R.id.lowScoreWins);
         messagePreview = (TextView) layout.findViewById(R.id.messagePreview);
         messagePreview.setMovementMethod(LinkMovementMethod.getInstance());
         share = (Button) layout.findViewById(R.id.share);
 
         if (game.getName() == null) {
             game.setName("");
         }
         gameName.setText(game.getName());
         gameName.setSelection(game.getName().length());
         highScoreWins.setChecked(game.getWinCondition() == WinCondition.HIGH_SCORE);
         lowScoreWins.setChecked(game.getWinCondition() == WinCondition.LOW_SCORE);
         gameSummarizer = new GameSummarizer(this);
         updateMessagePreview();
 
         gameName.addTextChangedListener(new TextWatcher() {
             @Override public void beforeTextChanged(CharSequence text, int a, int b, int c) {
             }
             @Override public void onTextChanged(CharSequence text, int a, int b, int c) {
                 game.setName(gameName.getText().toString().trim());
                 updateMessagePreview();
                 gameSaver.saveLater();
             }
             @Override public void afterTextChanged(Editable textView) {
             }
         });
 
         OnCheckedChangeListener checkedChangeListener = new OnCheckedChangeListener() {
             @Override public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                 WinCondition winCondition = highScoreWins.isChecked()
                         ? WinCondition.HIGH_SCORE
                         : WinCondition.LOW_SCORE;
                 game.setWinCondition(winCondition);
                 updateMessagePreview();
                 gameSaver.saveLater();
             }
         };
         highScoreWins.setOnCheckedChangeListener(checkedChangeListener);
         lowScoreWins.setOnCheckedChangeListener(checkedChangeListener);
 
         share.setOnClickListener(new View.OnClickListener() {
             @Override public void onClick(View view) {
                 new ShareGameTask(game).execute();
             }
         });
 
         setContentView(layout);
 
         ActionBar actionBar = getSupportActionBar();
         actionBar.setDisplayHomeAsUpEnabled(true);
     }
 
     @Override protected void onPause() {
         super.onPause();
         gameSaver.onPause();
     }
 
     void updateMessagePreview() {
         messagePreview.setText(gameSummarizer.summarize(game, "roundsapp.com/..."));
 
         String gameName = game.getName();
        if (gameName == null || gameName.length() == 0) {
             messagePreview.setVisibility(View.INVISIBLE);
             share.setEnabled(false);
         } else {
             messagePreview.setVisibility(View.VISIBLE);
             share.setEnabled(true);
         }
     }
 
     @Override protected void onSaveInstanceState(Bundle savedState) {
         super.onSaveInstanceState(savedState);
         savedState.putString(IntentExtras.GAME_ID, game.getId());
     }
 
     @Override public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case android.R.id.home:
                 Intent intent = new Intent(this, GameActivity.class);
                 intent.putExtra(IntentExtras.GAME_ID, game.getId());
                 intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                 startActivity(intent);
                 overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
                 finish();
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
     private class ShareGameTask extends AsyncTask<Void, Void, String> {
         private final Game game;
 
         public ShareGameTask(Game game) {
             this.game = game.clone();
         }
 
         @Override protected void onPreExecute() {
             gameName.setEnabled(false);
             highScoreWins.setEnabled(false);
             lowScoreWins.setEnabled(false);
             share.setEnabled(false);
             messagePreview.setEnabled(false);
         }
 
         @Override protected String doInBackground(Void... unused) {
             String gameUrl = "roundsapp.com";
             try {
                 URL postUrl = new URL(POST_URL);
                 HttpURLConnection connection = (HttpURLConnection) postUrl.openConnection();
                 connection.setDoOutput(true);
                 connection.setInstanceFollowRedirects(false);
                 connection.setRequestMethod("POST");
                 JsonWriter writer = new JsonWriter(new OutputStreamWriter(
                         connection.getOutputStream(), "UTF-8"));
                 Json.gson.toJson(game, Game.class, writer);
                 writer.close();
                 if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                     throw new IOException("Unexpected HTTP response: "
                             + connection.getResponseCode() + " " + connection.getResponseMessage());
                 }
                 BufferedReader reader = new BufferedReader(new InputStreamReader(
                         connection.getInputStream(), "UTF-8"));
                 gameUrl = reader.readLine();
                 if (gameUrl.startsWith("http://www.")) {
                     gameUrl = gameUrl.substring("http://www.".length());
                 }
                 reader.close();
             } catch (IOException ignored) {
                 ignored.printStackTrace();
             }
             return gameUrl;
         }
 
         @Override protected void onPostExecute(String url) {
             gameName.setEnabled(true);
             highScoreWins.setEnabled(true);
             lowScoreWins.setEnabled(true);
             share.setEnabled(true);
             messagePreview.setEnabled(true);
 
             String messageWithUrl = gameSummarizer.summarize(game, url).toString();
             Intent intent = new Intent(Intent.ACTION_SEND);
             intent.setType("text/plain");
             intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
             intent.putExtra(Intent.EXTRA_SUBJECT, game.getName());
             intent.putExtra(Intent.EXTRA_TEXT, messageWithUrl);
             startActivity(Intent.createChooser(intent, "Share"));
         }
     }
 }
