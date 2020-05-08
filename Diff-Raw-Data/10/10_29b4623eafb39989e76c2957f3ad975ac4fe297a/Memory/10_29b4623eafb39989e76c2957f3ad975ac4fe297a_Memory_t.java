 package de.thm.ateam.memory.game;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.content.Context;
import android.graphics.Typeface;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.View;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.GridView;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 //import android.widget.Toast;
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
 	private TextView infoView;
 
 	private UpdateCardsHandler resHandler;
 	private DeleteCardsHandler delHandler;
 	private static Object lock = new Object(); //synchronous actions suck.
 
 	private Activity envActivity;
 
 
 
 	/*game function related stuff*/
 	private int ROW_COUNT;
 	private int COL_COUNT;
 	private int left; //how many cards are left
 	private int card = -1; //should better be an object of card
 	private int numberOfPicks = 0;
 
 	private Player current;
 
 	public Memory(Context ctx, MemoryAttributes attr){
 		super(ctx,attr);
 		this.attr = attr;
 		this.envActivity = (Activity)ctx; // just a funny cast, that will let us access more functionality
 		resHandler = new UpdateCardsHandler();
 		delHandler = new DeleteCardsHandler();
 	}
 
 	public void newGame(){
 		ROW_COUNT = attr.getRows();
 		COL_COUNT = attr.getColumns();
 		left = ROW_COUNT * COL_COUNT;
 		current = turn();
 		infoView.setText(current.nick);
 	}
 
 
 
 	public View assembleLayout(){
 		
 		infoView = new TextView(ctx);
 		infoView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0.05f)); //this is fixed value, should be fine though
 		infoView.setText("Initializing ...");
		infoView.setTypeface(null,Typeface.BOLD_ITALIC);
		infoView.setTextSize(20); // looks good to me
 
 		newGame();
 		
 		imageAdapter = new ImageAdapter(ctx, ROW_COUNT, COL_COUNT);
 		theme = imageAdapter.getTheme();
 
 		mainView = new GridView(ctx);
 		mainView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f)); //relations are 1 to .05
 		mainView.setNumColumns(attr.getColumns());
 		mainView.getLayoutParams().width = imageAdapter.maxSize();
 
 		mainView.setAdapter(imageAdapter);
 		mainView.setOnItemClickListener(new MemoryClickListener());
 
 
 		LinearLayout linLay = new LinearLayout(envActivity.getApplicationContext());
 		linLay.setOrientation(LinearLayout.VERTICAL); // Hor-izontal is Hor - joke - rible
 		
 		linLay.addView(infoView);
 		linLay.addView(mainView);
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
 	 * Function is exclusively used to turn a pair of cards to the back side.
 	 * Launches a TimedTask, a ResetTask to be exact.
 	 * 
 	 * @param pos
 	 * @param pos2
 	 * 
 	 */
 	public void reset(int pos, int pos2) {
 		ResetTask task = new ResetTask(pos, pos2);
 		Timer t = new Timer(false);
 		t.schedule(task, 1000);
 	}
 
 	/**
 	 * Function is exclusively used to make a pair of cards invisible and unclickable.
 	 * Launches a TimedTask, a DeleteTask to be exact.
 	 * 
 	 * @param pos
 	 * @param pos2
 	 * 
 	 */
 	public void delete(int pos, int pos2) {
 
 		DeleteTask task = new DeleteTask(pos, pos2);
 		Timer t = new Timer(false);
 		t.schedule(task, 1000);
 
 	}
 
 	// TODO add java doc
 	class ResetTask extends TimerTask  {
 		private int pos, pos2;
 		public ResetTask(int pos, int pos2) {
 			this.pos = pos;
 			this.pos2 = pos2;
 		}
 
 		@Override
 		public void run() {
 			Bundle data = new Bundle();
 			data.putInt("pos", pos);
 			data.putInt("pos2", pos2);
 			Message msg = new Message();
 			msg.setData(data);
 			resHandler.sendMessage(msg);
 		}
 	}
 
 	class DeleteTask extends TimerTask {
 		private int pos, pos2;
 
 		public DeleteTask(int pos, int pos2){
 			this.pos = pos;
 			this.pos2 = pos2;
 		}
 
 		@Override
 		public void run(){
 			Bundle data = new Bundle();
 			data.putInt("pos", pos);
 			data.putInt("pos2", pos2);
 			Message msg = new Message();
 			msg.setData(data);
 			delHandler.sendMessage(msg);
 		}
 	}
 
 	@SuppressLint("HandlerLeak")
 	class UpdateCardsHandler extends Handler{
 		@Override
 		public  void handleMessage(Message msg) {
 			synchronized (lock) {
 				doJob(msg);
 			}
 		}
 		public void doJob(Message msg){
 			ImageView clicked = (ImageView) imageAdapter.getItem(msg.getData().getInt("pos"));
 			clicked.setImageBitmap(theme.getBackSide());
 			clicked = (ImageView) imageAdapter.getItem(msg.getData().getInt("pos2"));
 			clicked.setImageBitmap(theme.getBackSide());
 			numberOfPicks = 0;
 		}
 	}
 
 	@SuppressLint("HandlerLeak")
 	class DeleteCardsHandler extends Handler{
 		@Override
 		public  void handleMessage(Message msg) {
 			synchronized (lock) {
 				doJob(msg);
 			}
 		}
 		public void doJob(Message msg){
 			ImageView clicked = (ImageView) imageAdapter.getItem(msg.getData().getInt("pos"));
 			clicked.setImageBitmap(null);
 			clicked.setEnabled(false);
 
 			clicked = (ImageView) imageAdapter.getItem(msg.getData().getInt("pos2"));
 			clicked.setImageBitmap(null);
 			clicked.setEnabled(false);
 			numberOfPicks = 0; 
 		}
 	}
 
 	class MemoryClickListener implements OnItemClickListener {
 		public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
 			numberOfPicks++;
 			if(numberOfPicks > 2){
 				return;
 			}
 
 			/*
 			 * simple recognition of hits or misses,
 			 * must be overridden by a round-based player system
 			 */
 
 			synchronized (lock) { //just to avoid unwanted behavior
 				if(card == -1){
 					flip(position);
 					card = position;
 					/* Increase the number of turns */
 					current.turn();
 					//Toast.makeText(ctx,"select " +position+ " first move", Toast.LENGTH_SHORT).show();
 				}else{ 
 					if(card != position) {
 						numberOfPicks++;
 						flip(position);
 						if(imageAdapter.getItemId(card) == imageAdapter.getItemId(position)){
 							delete(position, card); 
 							//Toast.makeText(ctx,"card "+ " select " +position+ " hit, next player", Toast.LENGTH_SHORT).show();
 							card = -1;
 							current.hit();
 							left -= 2;
 							if(left<=0){
 								Memory.this.getWinner();
 								numberOfPicks = 0;
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
 							reset(position, card);
 							card = -1;
 							current = turn();
 							infoView.setText(current.nick);
 						}
 					}
 				}
 			}
 
 		}
 	}
 
 
 }
 
 
 
 
 
