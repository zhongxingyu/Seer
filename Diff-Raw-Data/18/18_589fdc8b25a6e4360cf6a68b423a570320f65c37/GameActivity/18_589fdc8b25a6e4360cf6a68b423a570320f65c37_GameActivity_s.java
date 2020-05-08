 package de.thm.ateam.memory.game;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Collections;
 
 import android.app.Activity;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.util.Log;
 import android.widget.Toast;
 import de.thm.ateam.memory.ImageAdapter;
 import de.thm.ateam.memory.R;
 import de.thm.ateam.memory.engine.type.NetworkPlayer;
 import de.thm.ateam.memory.engine.type.Player;
 import de.thm.ateam.memory.network.MyAlertDialog;
 import de.thm.ateam.memory.network.MyAlertDialog.MyAlertDialogListener;
 import de.thm.ateam.memory.network.NetworkMemory;
 
 /**
  * 
  * the heart and central activity for a network game and initializes
  * a singleplayer game
  *
  */
 public class GameActivity extends FragmentActivity implements MyAlertDialogListener{
 
 
 
   Game game = null;
   private final int ROWS = PlayerList.getInstance().row;
   private final int COLUMNS = PlayerList.getInstance().col;
 
 
   private static final String TAG = GameActivity.class.getSimpleName();
 
   NetworkPlayer currentPlayer = null;
   PrintWriter out = null;
   boolean hasReceivedFinishMessage = false;
 
   /**
    * 
    * waits for incoming messages from the server
    *
    */
   private class ReadingTask extends AsyncTask<Void, String, Integer>{
 
     @Override
     protected Integer doInBackground(Void... unused) {
       BufferedReader bf = null;
       String inputLine;
       try {
         bf = new BufferedReader(new InputStreamReader(currentPlayer.sock.getInputStream()));
         Log.i(TAG, "Waiting for messages");
         while ((inputLine = bf.readLine()) != null) {
           Log.i(TAG, "Received a message");
           publishProgress(inputLine);
           if(inputLine.startsWith("[finish]")){
             /* game is finished so get out of here */
             break;
           }
         }
         Log.i(TAG, "Client waiting loop has ended");
       } catch (IOException e) {
         Log.e(TAG, "ERROR: while creating the Reader.");
         /* display an alert dialog */
         MyAlertDialog alertDialog = new MyAlertDialog(R.string.could_not_connect_to_server);
         FragmentManager fm = getSupportFragmentManager();
         alertDialog.show(fm, "dialog");
       }
 
       /* return value is not important */
       return 0;
     }
 
     /**
      * handle the different incoming messages
      */
     @Override
     protected void onProgressUpdate(String... messages){
       Log.i(TAG, "client received " + messages.length + " new message(s).");
       for(String message : messages){
         if(message.startsWith("[token]")){
           currentPlayer.hasToken = true;
           Log.i(TAG, "client received the token");
           Toast t = Toast.makeText(GameActivity.this,  R.string.your_turn, Toast.LENGTH_SHORT);
           t.show();
           NetworkMemory.getInstance(GameActivity.this, null).infoView.setText(currentPlayer.nick);
         }else if(message.startsWith("[field]")){
           Log.i(TAG, "received field");
           /** update game field, set it as content view */
           int host_rows = Integer.parseInt(message.substring(7, 8));
           int host_columns = Integer.parseInt(message.substring(8,9));
           NetworkMemory.getInstance(GameActivity.this, null).imageAdapter = new ImageAdapter(GameActivity.this, host_rows, host_columns);
           NetworkMemory.getInstance(GameActivity.this, null).imageAdapter.buildField(message.substring(9), host_rows * host_columns, host_columns);
           String field = "";
           for(Card[]c : NetworkMemory.getInstance(GameActivity.this, null).imageAdapter.getPositions(host_columns)){
             field += c[0] +";"+ c[1] +"Ende";
           }
           setContentView(NetworkMemory.getInstance(GameActivity.this, null).assembleLayout());
           Log.i("new field", field);
         }else if(message.startsWith("[flip]")){
           /* flip a card */
           int pos = Integer.parseInt(message.substring(6));
           NetworkMemory.getInstance(GameActivity.this, null).flip(pos);
         }else if(message.startsWith("[delete]")){
           /* a pair was hit */
           String[] pick = message.substring(8).split(",");
           
           NetworkMemory.getInstance(GameActivity.this, null).deleted.add(Integer.parseInt(pick[0])); //TODO: test
           NetworkMemory.getInstance(GameActivity.this, null).deleted.add(Integer.parseInt(pick[1]));
           
           NetworkMemory.getInstance(GameActivity.this, null).delete(
               Integer.parseInt(pick[0]), 
               Integer.parseInt(pick[1]));
           NetworkMemory.getInstance(GameActivity.this, null).left -= 2;
           /* check if the game is now finished */
           if(NetworkMemory.getInstance(GameActivity.this, null).left <= 0 && currentPlayer.hasToken){
             try {
               out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(currentPlayer.sock.getOutputStream())), true);
             } catch (IOException e) {
               Log.e(TAG, "couldn't open outputStream");
               MyAlertDialog alertDialog = new MyAlertDialog(R.string.could_not_connect_to_server);
               FragmentManager fm = getSupportFragmentManager();
               alertDialog.show(fm, "dialog");
               
             }
             /* remove the token for the next round */
             currentPlayer.hasToken = false;
             finish();
           }
         }else if(message.startsWith("[reset]")){
           /* reset two cards, which were not a pair */
           String[] pick = message.substring(7).split(",");
           NetworkMemory.getInstance(GameActivity.this, null).reset(
               Integer.parseInt(pick[0]),
               Integer.parseInt(pick[1]));
         }else if(message.startsWith("[finish]")){
           String winner = message.substring(8);
           if(winner.equals("")){
             Toast.makeText(GameActivity.this, "No single winner is here", Toast.LENGTH_SHORT).show();
           }else{
             Toast.makeText(GameActivity.this, winner + " has won!", Toast.LENGTH_SHORT).show();
           }
           setResult(Activity.RESULT_OK, getIntent().putExtra("msg", "foo"));
           hasReceivedFinishMessage = true;
           finish();
         }else if(message.startsWith("[currentPlayer]")){
           /* print the currentPlayer name */
           String username = message.substring(15);
           Toast t = Toast.makeText(GameActivity.this, "it's "+ username +"s turn!", Toast.LENGTH_SHORT);
           t.show();
           NetworkMemory.getInstance(GameActivity.this, null).infoView.setText(username);
         }
       }
 
     }
 
 
     @Override
     protected void onPostExecute(Integer v){
       // unused
     }
 
   }
 
   /**
    * initialize a game, dependent on if its a singleplayer or network game
    */
   @Override
   public void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
 
     Bundle b = getIntent().getExtras();
     if(b == null){
       // it's a local game
       ArrayList<Player> players = PlayerList.getInstance().session; // just a reference
 
       Collections.shuffle(PlayerList.getInstance().session); // we want a different order each time
 
       /*
        * this is where the gameactivity initializes its specific game, e.g. a descendant from "Game.java",
        * the method assembleLayout() creates a grid view holding the specific cards.
        * 
        * This is also the spot where the Game should be merged into the rest of the Application.
        * assembleLayout() does not need any kind of XML File, which makes it very versatile in its use.
        * 
        */
       game = new Memory(this,new MemoryAttributes(players, ROWS, COLUMNS));
       setContentView(game.assembleLayout());
     }else{
       /* it's a network game */
       currentPlayer = PlayerList.getInstance().currentPlayer;
       new ReadingTask().execute();
      game = NetworkMemory.getInstance(this, new MemoryAttributes(ROWS, COLUMNS));
     }
 
   }
 
   @Override
   public void onResume(){
     super.onResume();
     Bundle b = getIntent().getExtras();
     if(b != null){
       //it's a multiplayer game
 
       boolean host = b.getBoolean("host");
      game = NetworkMemory.getInstance(this, new MemoryAttributes(ROWS, COLUMNS));
       /* only the host player should go here */
       if(host){
         String field =  ((NetworkMemory)game).createField();
         try {
           out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(currentPlayer.sock.getOutputStream())), true);
         } catch (IOException e) {
           Log.e(TAG, "couldn't open outputStream");
           e.printStackTrace();
         }
 
         Log.i(TAG, "Send field string");
         /* hostPlayer holds the token on start */
         currentPlayer.hasToken = true;
         out.println("[field]"+ ROWS +""+ COLUMNS + "" + field);
         out.println("[currentPlayer]"+ currentPlayer.nick);
       }
     }
   }
 
   /**
    * 
    * Function to clear the Pictures
    * 
    */
   @Override
   public void onDestroy() {
     super.onDestroy();
     game.onDestroy();
   }
   
   /* the first player who goes here should notify the other players via
    * server that the game has ended
    * 
    * (non-Javadoc)
    * @see android.support.v4.app.FragmentActivity#onStop()
    */
   @Override
   public void onStop(){
     super.onStop();
     Bundle b = getIntent().getExtras();
     /* if it's not a local game (b would contain "msg") and this player
      * has not a received a finish message yet, notify the other players
      * about the end of the game
      */
     if(b != null && !hasReceivedFinishMessage && !b.containsKey("msg")){
       try {
         out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(currentPlayer.sock.getOutputStream())), true);
       } catch (IOException e) {
         Log.e(TAG, "couldn't open outputStream");
         MyAlertDialog alertDialog = new MyAlertDialog(R.string.connection_lost);
         FragmentManager fm = getSupportFragmentManager();
         alertDialog.show(fm, "dialog");
       }
       out.println("[finish]");
     }
   }
 
   /* after the alertDialog was displayed, finish this activity */
   public void onFinishAlertDialog() {
     finish();
     
   }
 }
