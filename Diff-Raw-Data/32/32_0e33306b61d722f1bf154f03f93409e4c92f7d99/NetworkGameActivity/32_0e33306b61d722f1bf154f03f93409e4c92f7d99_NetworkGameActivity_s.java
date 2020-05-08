 package ca.team615.memorygameandroid;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.net.InetSocketAddress;
 import java.net.Socket;
 import java.net.SocketException;
 import java.net.SocketTimeoutException;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 public class NetworkGameActivity extends Activity implements OnClickListener {
 
 	private static final String tag = "NetworkGame";
 
 	private int[] viewIds = {R.id.card_0, R.id.card_1, R.id.card_2, R.id.card_3,
 			R.id.card_4, R.id.card_5, R.id.card_6, R.id.card_7,
 			R.id.card_8, R.id.card_9, R.id.card_10, R.id.card_11,
 			R.id.card_12, R.id.card_13, R.id.card_14, R.id.card_15,};
 
 	private int[] drawableIds = {R.drawable.card_0, R.drawable.card_1, R.drawable.card_2, R.drawable.card_3,
 			R.drawable.card_4, R.drawable.card_5, R.drawable.card_6, R.drawable.card_7, R.drawable.card_back};
 
 	private int[] assignments;	//Holds the assigned positions of the cards
 
 	private ImageView[] imageviews;
 
 	/** the number of cards currently face up */
 
 	private TextView playerPairsLabel;
 	private TextView opponentPairsLabel;
 
 	boolean quitting = false;
 
 	Handler handler;
 
 	OutputStream oStream = null;
 	PrintWriter writer = null;
 
 	InputStream iStream = null;
 	BufferedReader reader = null;
 
 	Socket socket;
 
 	int flippedCards = 0;
 
 	String connectAddress;
 	int connectPort;
 
 	ProgressDialog progressDlg;
 	
 	LinearLayout outcomeLayout;
 	TextView outcomeText;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.game_screen_layout);
 
 		handler = new Handler();
 
 		connectAddress = "";
 		connectPort = 0;
 		try{
 			Intent i = this.getIntent();
 			connectAddress = i.getStringExtra("address");
 			connectPort = i.getIntExtra("port", 0);
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 
 		((TextView)this.findViewById(R.id.found_pairs_label)).setText(R.string.playerscore_label);
 		playerPairsLabel=(TextView)NetworkGameActivity.this.findViewById(R.id.pairs_counter);
 
 		((TextView)this.findViewById(R.id.turns_label)).setText(R.string.opponentscore_label);
 		opponentPairsLabel=(TextView)NetworkGameActivity.this.findViewById(R.id.turns);
 		
 		outcomeText = (TextView)this.findViewById(R.id.outcome_text);
 		outcomeLayout = (LinearLayout)this.findViewById(R.id.outcome_layout);
 
 		//create a new array to hold the card positions
 		assignments = new int[16];
 
 		//get the imageviews
 		imageviews = new ImageView[viewIds.length];
 		for(int i = 0; i < viewIds.length; i++){
 			imageviews[i] = (ImageView)findViewById(viewIds[i]);
 		}
 
 		//set click listeners for each veiw
 		for(int i = 0; i < 16; i++){
 			((ImageView)findViewById(viewIds[i])).setOnClickListener(this);
 			((ImageView)findViewById(viewIds[i])).setTag(new Integer(i));
 		}
 
 		//set each image to blank
 		for(int i = 0; i < 16; i++){
 			((ImageView)findViewById(viewIds[i])).setImageResource(R.drawable.card_back);
 		}
 
 		progressDlg = null;
 		progressDlg = ProgressDialog.show(this,
 				this.getString(R.string.dialog_title_waitin),
 				this.getString(R.string.dialog_message_connect), true);
 		progressDlg.setCancelable(true);
 
 		disableCards();
 		new Thread(new InputHandler()).start();
 
 	}
 
 	@Override
 	public void onBackPressed() {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setMessage("Are you sure you want to exit?")
 		.setCancelable(false)
 		.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int id) {
 				NetworkGameActivity.this.quit();
 			}
 		})
 		.setNegativeButton("No", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int id) {
 				dialog.cancel();
 			}
 		});
 		AlertDialog alert =	builder.create();
 		alert.show();
 	}
 
 	public void quit(){
 		quitting = true;
 		writer.println("quit");
 		writer.flush();
 		try{
 			iStream.close();
 		}catch(Exception e){}
 		try{
 			oStream.close();
 		}catch(Exception e){}
 		try{
 			socket.close();
 		}catch(Exception e){}
 		this.finish();
 	}
 
 	@Override
 	public void onClick(View v) {
 
 		int index = 0;
 		for(int i = 0; i < 16; i++){
 			if(v.getId() == viewIds[i]){
 				index = i;
 				break;
 			}
 		}
 		Log.i(tag, "Clicked " + index);
 		try{
 			writer.println("select " + index);
 			flippedCards++;
 			v.setClickable(false);
 			v.setFocusable(false);
 			if(flippedCards == 2){
 				disableCards();
 			}
 			writer.flush();
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Flip the card the server wants us to flip.
 	 * @param index	The index of the card to be flipped.
 	 */
 	void flipCard(int index){
 
 
 		SoundManager.playSound(SoundManager.SOUND_FLIP);
 		Log.i(tag, "Showing card " +assignments[index] + " at position " + index);
 		((ImageView)findViewById(viewIds[index])).setImageResource(drawableIds[assignments[index]]);
 		imageviews[index].setFocusable(false);
 		imageviews[index].setClickable(false);
 
 	}
 
 	/**
 	 * Disable all the cards when the server tells us to
 	 */
 	void disableCards(){
 		for(ImageView view:imageviews){
 			view.setFocusable(false);
 			view.setClickable(false);
 		}
 		((LinearLayout)findViewById(R.id.bottomtoolbarcontainer)).setBackgroundResource(R.drawable.toolbar_bg);
 		((TextView)findViewById(R.id.turn_indicator)).setText("");
 	}
 
 	/**
 	 * Enable all the cards when the server tells us to
 	 */
 	void enableCards(){
 		for(ImageView view: imageviews){
 			view.setFocusable(true);
 			view.setClickable(true);
 		}
 		((LinearLayout)findViewById(R.id.bottomtoolbarcontainer)).setBackgroundResource(R.drawable.toolbar_playerturn);
 		((TextView)findViewById(R.id.turn_indicator)).setText(R.string.player_turn);
 		flippedCards = 0;
 	}
 
 	void updateProgress(){
 		progressDlg.setMessage("Waiting for the other player");
 	}
 
 	void removeProgress(){
 		progressDlg.dismiss();
 		progressDlg = null;
 	}
 
 	/**
 	 * Either set the cards back to the back image or remove them
 	 * @param currentIndex	Card index 1
 	 * @param lastIndex		Card index 2
 	 * @param found			if true, hide the cards, if false, show the back
 	 */
 	void flipCardsBack(int currentIndex, int lastIndex, boolean found) { 
 		SoundManager.playSound(SoundManager.SOUND_FLOP);
 
 		if(found){
 			((ImageView)findViewById(viewIds[lastIndex])).setVisibility(View.INVISIBLE);
 			((ImageView)findViewById(viewIds[currentIndex])).setVisibility(View.INVISIBLE);
 
 		}else{
 			((ImageView)findViewById(viewIds[currentIndex])).setImageResource(R.drawable.card_back);
 			((ImageView)findViewById(viewIds[lastIndex])).setImageResource(R.drawable.card_back);
 
 		}
 
 	} 
 
 	private void win(){
 		outcomeText.setText(R.string.outcome_win);
 		outcomeLayout.setVisibility(View.VISIBLE);
 		SoundManager.playLoopedSound(SoundManager.SOUND_WINNER);
 	}
 
 	private void lose(){
 		outcomeText.setText(R.string.outcome_lose);
 		outcomeLayout.setVisibility(View.VISIBLE);
 	}
 
 	private void draw(){
 		outcomeText.setText(R.string.outcome_draw);
 		outcomeLayout.setVisibility(View.VISIBLE);
 	}
 	
 	private void opponentLost(){
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setMessage(R.string.dialog_message_opponent_lost)
 		.setCancelable(false)
 		.setNeutralButton("Quit", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int id) {
 				NetworkGameActivity.this.quit();
 			}
 		});
 
 		AlertDialog alert =	builder.create();
 		alert.show();
 	}
 	
 	private void opponentQuit(){
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setMessage(R.string.dialog_message_opponent_quit)
 		.setCancelable(false)
 		.setNeutralButton("Quit", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int id) {
 				NetworkGameActivity.this.quit();
 			}
 		});
 
 		AlertDialog alert =	builder.create();
 		alert.show();
 	}
 
 	/**
 	 * Update the scores on the screen.
 	 * @param player
 	 * @param opponent
 	 */
 	private void updateScores(int player, int opponent){
 		opponentPairsLabel.setText(Integer.toString(opponent));
 		playerPairsLabel.setText(Integer.toString(player));
 	}
 
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 
 		SoundManager.getInstance(getBaseContext());
 
 		if(socket != null){
 			writer.println("resume");
 			writer.flush();
 		}
 	}
 
 	/** To be called when the socket gets screwed over. */
 	private void connectionClosed(){
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setMessage(R.string.network_lostconnection)
 		.setCancelable(false)
 		.setNeutralButton("Quit", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int id) {
 				NetworkGameActivity.this.finish();
 			}
 		});
 
 		AlertDialog alert =	builder.create();
 		if(progressDlg != null)
 			progressDlg.dismiss();
 		alert.show();
 
 	}
 
 	/** To be called when there's not connection to the server */
 	private void noConnection(){
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setMessage(R.string.network_noconnection)
 		.setCancelable(false)
 		.setNeutralButton("Quit", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int id) {
 				NetworkGameActivity.this.finish();
 			}
 		});
 
 		AlertDialog alert =	builder.create();
 		if(progressDlg != null)
 			progressDlg.dismiss();
 		alert.show();
 
 	}
 
 	@Override
 	protected void onStop() {
 		if(progressDlg != null){
 			progressDlg.dismiss();
 			progressDlg = null;
 		}
 		super.onStop();
 	}
 
 	@Override
 	protected void onPause() {
 		SoundManager.pauseLoopedSound(SoundManager.SOUND_WINNER);
 		if(socket != null){
 			writer.println("pause");
 			writer.flush();
 		}
 		super.onPause();
 	}
 
 	protected void assignCards(String raw){
 		Log.i(tag, raw);
 		String[] values = raw.split(" ");
 		for(int i = 1; i < values.length; i++){
 			assignments[i-1] = Integer.parseInt(values[i]);
 			Log.i(tag, "Card " + i + " is " + assignments[i-1]);
 		}
 		Log.i(tag, "Saved assignments");
 	}
 
 	private class InputHandler implements Runnable{
 
 
 		@Override
 		public void run() {
 
 			//try {
 			Log.i(tag, "Trying to connect to socket");
 			InetSocketAddress addr = new InetSocketAddress(connectAddress, connectPort);
 
 			socket = new Socket();
 			int attempts = 6;
 			for(int i = 1; i <= attempts; i++){
 
 				try{
 					socket.connect(addr, 1000 * i);
 				}catch(SocketException e){
 				}catch(SocketTimeoutException e){
 				}catch(IOException e) {
 				}
 
 				Log.i(tag, "Attempt " + i + " failed.");
 				if(socket.isConnected()){
 					break;
 				}else{
 					if(i == attempts){
 						Log.i(tag, "Couldn't Connect");
 						handler.post(new Runnable(){
 							@Override
 							public void run() {
 								noConnection();
 							}});
 						return;
 					}else{
 						socket = new Socket();
 					}
 				}
 			}
 
 			Log.i(tag, "Connected to socket");
 
 			try {
 				iStream = socket.getInputStream();
 				reader = new BufferedReader(new InputStreamReader(iStream));
 				oStream = socket.getOutputStream();
 				writer = new PrintWriter(oStream);
 			} catch (IOException e) {
 				//this may have to be expanded later. 
 			}
 
 			handler.post(new Runnable(){
 				@Override
 				public void run() {
 					updateProgress();
 				}});
 
 			while(true){
 				Log.i(tag, "Waiting for a command");
 				String command;
 				try {
 					command = reader.readLine();
 					if(command == null){
 						if(!quitting);
 						throw new IOException();
 					}
 					if(command.startsWith("quit")){
 						handler.post(new Runnable(){
 							@Override
 							public void run() {
 								connectionClosed();
 							}});
 						break;
 					}else if(command.startsWith("loaded")){
 						handler.post(new Runnable(){
 							@Override
 							public void run() {
 								removeProgress();
 							}});
 					}else if(command.startsWith("cardorder")){
 						final String buffer = command;
 						handler.post(new Runnable(){
 							@Override
 							public void run() {
 								assignCards(buffer);
 
 							}});
 					}else if(command.startsWith("flip")){
 						final int index = Integer.parseInt(command.substring(5, command.length()));
 						handler.post(new Runnable(){
 							@Override
 							public void run() {
 								flipCard(index);
 
 							}});
 					}else if(command.startsWith("disableall")){
 						handler.post(new Runnable(){
 							@Override
 							public void run() {
 								disableCards();
 
 							}});
 					}else if(command.startsWith("enableall")){
 						handler.post(new Runnable(){
 							@Override
 							public void run() {
 								enableCards();
 
 							}});
 					}else if(command.startsWith("win")){
 						handler.post(new Runnable(){
 							@Override
 							public void run() {
 								win();
 
 							}});
 					}else if(command.startsWith("lose")){
 						handler.post(new Runnable(){
 							@Override
 							public void run() {
 								lose();
 
 							}});
 					}else if(command.startsWith("draw")){
 						handler.post(new Runnable(){
 							@Override
 							public void run() {
 								draw();
 							}});
 					}else if(command.startsWith("opponentlost")){
 						handler.post(new Runnable(){
 							@Override
 							public void run() {
 								opponentLost();
 							}});
 					}else if(command.startsWith("opponentquit")){
 						handler.post(new Runnable(){
 							@Override
 							public void run() {
 								opponentQuit();
 							}});
 					}else if(command.startsWith("flop")){
 						final String[] parts = command.split(" ");
 						handler.post(new Runnable(){
 							@Override
 							public void run() {
 								flipCardsBack(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), false);
 							}});
 					}else if(command.startsWith("remove")){
 						final String[] parts = command.split(" ");
 						handler.post(new Runnable(){
 							@Override
 							public void run() {
 								flipCardsBack(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), true);
 							}});
 					}else if(command.startsWith("score")){
 						final String[] parts = command.split(" ");
 						handler.post(new Runnable(){
 							@Override
 							public void run() {
 								updateScores(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
 							}});
 					}
 
 
 				} catch (IOException e) {
 					Log.i(tag, "Server Closed Connection.");
 					if(!quitting)
 						handler.post(new Runnable(){
 							@Override
 							public void run() {
 								connectionClosed();
 							}});
 					break;
 
 				}
 
 			}
 
 		}
 
 	}
 
 }
 
