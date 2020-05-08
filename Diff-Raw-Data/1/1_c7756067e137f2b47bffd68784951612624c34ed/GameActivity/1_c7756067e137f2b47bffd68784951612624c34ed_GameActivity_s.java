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
 import android.util.Log;
 import android.widget.Toast;
 import de.thm.ateam.memory.ImageAdapter;
 import de.thm.ateam.memory.R;
 import de.thm.ateam.memory.engine.type.Player;
 import de.thm.ateam.memory.network.NetworkMemory;
 
 public class GameActivity extends Activity{
 	
 	Memory mem = null;
 	NetworkMemory netMem = null;
 	private static final int ROWS = 2;
 	private static final int COLUMNS = 4;
 	
 	
 	private static final String TAG = GameActivity.class.getSimpleName();
 
   Player currentPlayer = null;
   int numberOfClicks = 0;
   PrintWriter out = null;
 
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
         }
         Log.i(TAG, "Client waiting loop has ended");
       } catch (IOException e) {
         Log.e(TAG, "ERROR: while creating the Reader.");
       }
 
       return 0;
     }
 
     @Override
     protected void onProgressUpdate(String... messages){
       Log.i(TAG, "client received " + messages.length + " new message(s).");
       for(String message : messages){
         if(message.startsWith("[token]")){
           currentPlayer.hasToken = true;
           Log.i(TAG, "client received the token");
           Toast t = Toast.makeText(GameActivity.this, R.string.your_turn, Toast.LENGTH_SHORT);
           t.show();
           NetworkMemory.getInstance(GameActivity.this, null).infoView.setText(currentPlayer.nick);
         }else if(message.startsWith("[next]")){
           Log.i(TAG, "it's no longer this clients turn");
         }else if(message.startsWith("[field]")){
           Log.i(TAG, "received field");
           NetworkMemory.getInstance(GameActivity.this, null).imageAdapter = new ImageAdapter(GameActivity.this, ROWS, COLUMNS);
           NetworkMemory.getInstance(GameActivity.this, null).imageAdapter.buildField(message.substring(7), ROWS * COLUMNS);
           String field = "";
           for(Card[]c : NetworkMemory.getInstance(GameActivity.this, null).imageAdapter.getPositions()){
             field += c[0] +";"+ c[1] +"Ende";
           }
           setContentView(NetworkMemory.getInstance(GameActivity.this, null).assembleLayout());
           Log.i("new field", field);
         }else if(message.startsWith("[flip]")){
           int pos = Integer.parseInt(message.substring(6));
           NetworkMemory.getInstance(GameActivity.this, null).flip(pos);
         }else if(message.startsWith("[delete]")){
           String[] pick = message.substring(8).split(",");
           NetworkMemory.getInstance(GameActivity.this, null).delete(
               Integer.parseInt(pick[0]), 
               Integer.parseInt(pick[1]));
         }else if(message.startsWith("[reset]")){
           String[] pick = message.substring(7).split(",");
           NetworkMemory.getInstance(GameActivity.this, null).reset(
               Integer.parseInt(pick[0]),
               Integer.parseInt(pick[1]));
         }else if(message.startsWith("[finish]")){
           setResult(Activity.RESULT_OK, getIntent().putExtra("msg", "foo"));
           finish();
         }else if(message.startsWith("[currentPlayer]")){
           String username = message.substring(15);
           Toast t = Toast.makeText(GameActivity.this, "it's "+ username +"s turn!", Toast.LENGTH_SHORT);
           t.show();
           NetworkMemory.getInstance(GameActivity.this, null).infoView.setText(username);
         }
       }
 
     }
 
 
     @Override
     protected void onPostExecute(Integer v){
     }
 
   }
 
 
 
   
   
   /*@Override
   public void onResume(){
     super.onResume();
     Log.i(TAG, "return from afk");
     out.println("[resume]");
   }*/
 
  
 
   
 
   
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		Bundle b = getIntent().getExtras();
 		//setContentView(R.layout.test); // i hate xml files, so fuck them.
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
   		mem = new Memory(this,new MemoryAttributes(players, ROWS, COLUMNS));
   		setContentView(mem.assembleLayout());
 		}else{
 		  currentPlayer = PlayerList.getInstance().currentPlayer;
 		  new ReadingTask().execute();
 		  //setContentView(R.layout.game_host);
 		  // it's a network game
 		  boolean host = b.getBoolean("host");
       netMem = NetworkMemory.getInstance(this, new MemoryAttributes(ROWS, COLUMNS));
 		  if(host){
 		    //setContentView(netMem.assembleLayout());
 		    String field =  netMem.createField();
 		    try {
           out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(currentPlayer.sock.getOutputStream())), true);
         } catch (IOException e) {
           Log.e(TAG, "couldn't open outputStream");
           e.printStackTrace();
         }
         
         Log.i(TAG, "Send field string");
         //host player hat am Anfang das Token
         currentPlayer.hasToken = true;
         out.println("[field]"+ field);
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
		out.close();
 		if(mem != null) mem.onDestroy();
 		if(netMem != null) netMem.onDestroy();
 	}
 	/*
 	 * if we decide to do some eventhandling for network usage (messages to be more specific) we should do that here.
 	 * A descendant of a Game will have to handle that specific request. 
 	 */
 }
