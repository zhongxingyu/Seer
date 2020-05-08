 package de.thm.ateam.memory.game;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.View;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.GridView;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.Toast;
 import de.thm.ateam.memory.ImageAdapter;
 import de.thm.ateam.memory.Theme;
 import de.thm.ateam.memory.engine.type.Player;
 
 
 
 public class Memory extends Game{
 
 	/*environement stuff*/
 	private static final String TAG = Memory.class.getSimpleName();
 	
 	private MemoryAttributes attr;
 	private GridView mainView;
 	private Theme theme;
 	private ImageAdapter imageAdapter;
 
 	private UpdateCardsHandler handler;
 	private static Object lock = new Object(); //sperrobjekt
 
 	private Activity envActivity;
 
 	
 	/*game function related stuff*/
 	private int ROW_COUNT;
 	private int COL_COUNT;
 	private int left; //how many cards are left
 	private int card = -1; //should better be an object of card
 	
 	private Player current;
 
 	
 	
 	public Memory(Context ctx, MemoryAttributes attr){
 		super(ctx,attr);
 		this.attr = attr;
 		this.envActivity = (Activity)ctx; // just a funny cast, that will let us access more functionality
 		handler = new UpdateCardsHandler();
 	}
 
 	public void newGame(){
 		ROW_COUNT = attr.getRows();
 		COL_COUNT = attr.getColumns();
 		left = ROW_COUNT * COL_COUNT;
 		current = turn();
 	}
 
 
 
 	public View assembleLayout(){
 
 		newGame();
 		for(Player p :attr.getPlayers()){
 		  Log.i("demo", p.nick);
 		}
 		imageAdapter = new ImageAdapter(ctx, ROW_COUNT, COL_COUNT);
 		theme = imageAdapter.getTheme();
 
 		mainView = new GridView(ctx);
 
 		//mainView.setLayoutParams(new GridView.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
 		mainView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
 		mainView.setNumColumns(attr.getColumns());
 		mainView.getLayoutParams().width = imageAdapter.maxSize();
 		
 
 		mainView.setAdapter(imageAdapter);
 
 		mainView.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
 
 				/*
 				 * simple recognition of hits or misses,
 				 * must be overridden by a round-based player system
 				 * 
 				 */
 
 				synchronized (lock) { //just to avoid unwanted behaviour
 					if(card == -1){
 						flip(position);
 						card = position;
 						//Toast.makeText(ctx,"select " +position+ " first move", Toast.LENGTH_SHORT).show();
 					}else{ 
 						if(card != position) {
 							flip(position);
 							if(imageAdapter.getItemId(card) == imageAdapter.getItemId(position)){
 							  
 								delete(position, card);
 								//Toast.makeText(ctx,"card "+ " select " +position+ " hit, next player", Toast.LENGTH_SHORT).show();
 								card = -1;
								
 								current.hit();
 								
 								left -= 2;
 								if(left<=0){
 								  Memory.this.getWinner();
 								  String victoryMsg = "";
 								  for(Player p : attr.getPlayers()){
 								    if(p.roundWin)
 								      victoryMsg += p.nick + ",";
 								  }
 								  // deletes last comma if there is a winner
 								  if(!victoryMsg.equals("")){
 								    victoryMsg = victoryMsg.substring(0, victoryMsg.length()-1);
 								    victoryMsg += " has won!!!";
 								  }else{
 								    victoryMsg = "Last round was a draw.";
 								  }
 								  Thread t = new Thread(new StatsUpdate(envActivity.getApplicationContext()));
 								  t.run();
 								  
 									
 									for (Player p : attr.getPlayers()) {
 										Log.i(TAG,p.nick+" turns: "+p.roundTurns+" hits: "+p.roundHits);
 									}
 									
 									envActivity.setResult(Activity.RESULT_OK, envActivity.getIntent().putExtra("msg", victoryMsg));
 									envActivity.finish();
 								}
 								
 								
 							}else{
 								//Toast.makeText(ctx,"card "+ " select " +position+ "miss, next player", Toast.LENGTH_SHORT).show();
 								reset(position);
 								reset(card);
 								card = -1;
 								//current.turn();
 								current = turn();
 							}
 						}
 					}
 				}
 
 			}
 		});
 		
 		LinearLayout linLay = new LinearLayout(envActivity.getApplicationContext());
 		linLay.setOrientation(LinearLayout.HORIZONTAL);
 		linLay.addView(mainView);
 		//linLay.addView(new Button(envActivity.getApplicationContext()));
 		return linLay;
 	}
 
 
 
 	/**
 	 * 
 	 * Function which flips the card on the position
 	 * 
 	 * @param position
 	 */
 	public void flip(int position) {
 		ImageView clicked = (ImageView) imageAdapter.getItem(position);
 		clicked.setImageBitmap(theme.getPicture(clicked.getId()));
 
 	}
 	
 	public void onDestroy() {
 		for(int i = 0; i < theme.getCount(); i++) {
 			if(theme.getPicture(i) != null)
 				theme.getPicture(i).recycle();
 		}
 	}
 
 
 	/**
 	 * 
 	 * Function which resets the card on the position to the backside
 	 * note: added a Timed Task to make everything look a bit cooler
 	 * 
 	 * @param position
 	 */
 	public void reset(int position) {
 		/* 
 		 * clicked.setImageBitmap(theme.getBackSide());
 		 * is now in the Handler that receives a message from the TimedTask
 		 */
 		ResetTask task = new ResetTask(position);
 		Timer t = new Timer(false);
 		t.schedule(task, 1000);
 	}
 
 	public void delete(int pos1, int pos2) {
 		ImageView clicked = (ImageView) imageAdapter.getItem(pos1);
 		ImageView clicked2 = (ImageView) imageAdapter.getItem(pos2);
 
 		clicked.setImageBitmap(null);
 		clicked.setEnabled(false);
 		clicked2.setImageBitmap(null);
 		clicked2.setEnabled(false);
 
 	}
 
 
 	class ResetTask extends TimerTask  {
 		private int pos;
 		public ResetTask(int pos) {
 			this.pos = pos;
 		}
 
 		@Override
 		public void run() {
 			Bundle data = new Bundle();
 			data.putInt("pos", pos);
 			Message msg = new Message();
 			msg.setData(data);
 			handler.sendMessage(msg);
 		}
 	}
 
 	class UpdateCardsHandler extends Handler{
 
 		@Override
 		public void handleMessage(Message msg) {
 			synchronized (lock) {
 				doJob(msg);
 			}
 		}
 		public void doJob(Message msg){
 
 			ImageView clicked = (ImageView) imageAdapter.getItem(msg.getData().getInt("pos"));
 			clicked.setImageBitmap(theme.getBackSide());

 		}
 	}
 
 
 
 
 }
