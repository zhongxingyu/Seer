 package games.distetris.presentation;
 
 import games.distetris.domain.CtrlDomain;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.GestureDetector;
 import android.view.KeyEvent;
 import android.view.MotionEvent;
 import android.widget.Toast;
 
 
 /**
  * Game activity. Player's zone
  * 
  * @author Jordi Castells
  *
  */
 public class Game extends Activity implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {
 	private GameView v;
 	private CtrlDomain dc;
 	private TimerTask gamelooptask;
 	private TimerTask refreshviewtask;
 	private Timer gamelooptimer = new Timer();
 	private Timer refreshviewtimer = new Timer();
 	private int mseconds_actualize = 500;
 	private int mseconds_viewactualize = 10;
 	private GestureDetector gestureScanner;
 	private static int threshold_vy = 800;
 	private static int threshold_vx = 500;
 	
 	private Handler handler = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
 			super.handleMessage(msg);
 
 			Bundle b = msg.getData();
 			String type = b.getString("type");
 
 			if (type.equals("SHUTDOWN")) {
 				Toast.makeText(getBaseContext(), "There was a problem with the connection", Toast.LENGTH_SHORT).show();
 				finish();
 			}
 
 		}
 	};
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         
         v = new GameView(getBaseContext());
         setContentView(v);
         
         gestureScanner = new GestureDetector(this);
 		
 		dc = CtrlDomain.getInstance();
 		dc.setHandlerUI(handler);
 		if(this.getIntent().getBooleanExtra("single", false)) setSinglePlayer();
 
     }
 
     /**
      * Sets the domain Controller for single player working
      */
 	private void setSinglePlayer() {
 		dc.setSingleplay(true);
 		dc.setIsMyTurn(true);
 		dc.startGame();
 	}
 
 	/**
 	 * A simple touch event
 	 */
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {		
 		return gestureScanner.onTouchEvent(event);
 	}
     
 	/**
 	 * Actions to realize when a GameOver occurs
 	 */
 	private void GameOverActions(boolean loser){
 		Log.d("DISTETRIS","I'm the loser?:"+loser);
 		v.gameover = true;
 		this.gamelooptimer.cancel();
		this.refreshviewtimer.cancel();
 		v.invalidate();
 		if(loser) dc.GameOverActionsLoser();
 		else dc.GameOverActionsOther();
 	}
 	
 	/**
 	 * Main Game Loop executed every x seconds
 	 */
 	private void gameLoop(){
 		if(dc.isMyTurn()){
 			//if current piece collides
 			if(dc.nextStepPieceCollision()){
 				if(dc.isGameOver()) {GameOverActions(true); return;}
 				try{dc.addCurrentPieceToBoard();}
 				catch(Exception e){GameOverActions(true);}
 				v.deletelines = dc.cleanBoard();
 				dc.setNewRandomPiece();
 			}
 			dc.gameStep();
 		}
 		else{
 			if(dc.isGameOver()) {GameOverActions(false); return;}
 		}
 	}
 
 	/**
 	 * Hook to control onKeyDown
 	 * 
 	 * @param keyCode	The keyCode pressed
 	 * @param event		The event referring the keycode
 	 */
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		switch(keyCode){
 		case KeyEvent.KEYCODE_DPAD_DOWN:
 			gameLoop();
 			break;
 		case KeyEvent.KEYCODE_DPAD_LEFT:
 			if(!dc.currentPieceOffsetCollision(-1)){
 				dc.getCurrentPiece().y = dc.getCurrentPiece().y - 1;
 			}
 			break;
 		case KeyEvent.KEYCODE_DPAD_RIGHT:
 			if(!dc.currentPieceOffsetCollision(+1)){
 				dc.getCurrentPiece().y = dc.getCurrentPiece().y + 1;
 			}
 			break;
 		case KeyEvent.KEYCODE_DPAD_UP:
 			dc.currentPieceRotateLeft();
 			break;
 		}
 
 		if (keyCode == KeyEvent.KEYCODE_BACK) {
 			dc.stopGame();
 			return super.onKeyDown(keyCode, event);
 		}
 		
 		v.invalidate();
 		return true;
 	}
 	
 	
 	/**
 	 * Start Game Loop timer
 	 */
 	public void doGameLoop() {
 		gamelooptask = new TimerTask() {
 			public void run() {
 				handler.post(new Runnable() {
 					public void run() {
 						gameLoop();
 					}
 				});
 			}
 		};
 		gamelooptimer.schedule(gamelooptask, 0, mseconds_actualize);
 	}
 
 	/**
 	 * Start view invalidate timer
 	 */
 	public void doViewInvalidate(){
 		this.refreshviewtask = new TimerTask() {
 		        public void run() {
 		                handler.post(new Runnable() {
 		                        public void run() {
 		                    		v.invalidate();
 		                        }
 		               });
 		        }};
 		    this.refreshviewtimer.schedule(refreshviewtask, 0, this.mseconds_viewactualize); 
 
 	}
 
 	@Override
 	public void onWindowFocusChanged(boolean hasFocus) {
 		if(hasFocus) doGameLoop();
 		
 	}
 
 	@Override
 	protected void onStop() {
 		gamelooptask.cancel();
 		super.onStop();
 	}
 
 
 	public void onLongPress(MotionEvent e)
 	{
 
 	}
 
 	@Override
 	public boolean onDown(MotionEvent e) {
 
 		return false;
 	}
 
 	/**
 	 * A fling movement
 	 * 
 	 * On fling left -> move piece left
 	 * On fling right -> move piece right
 	 * On fling down -> move piece down
 	 */
 	@Override
 	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
 			float velocityY) {
 		//swipe down
 		if(velocityY>threshold_vy){
 			dc.currentPieceFastFall();
 		}
 		//swipe left
 		else if(velocityX>threshold_vx){
 			if(!dc.currentPieceOffsetCollision(+1)){
 				dc.getCurrentPiece().y = dc.getCurrentPiece().y + 1;
 			}
 		}
 		//swipe right
 		else if(velocityX<-threshold_vx){
 			if(!dc.currentPieceOffsetCollision(-1)){
 				dc.getCurrentPiece().y = dc.getCurrentPiece().y - 1;
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
 			float distanceY) {
 
 		return false;
 	}
 
 	@Override
 	public void onShowPress(MotionEvent e) {
 
 		
 	}
 
 	@Override
 	public boolean onSingleTapUp(MotionEvent e) {
 
 		return false;
 	}
 
 	@Override
 	public boolean onDoubleTap(MotionEvent e) {
 		// TODO Auto-generated method stub
 		return true;
 	}
 
 	@Override
 	public boolean onDoubleTapEvent(MotionEvent e) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	/**
 	 * Single Tap
 	 * 
 	 * Rotate the piece
 	 */
 	@Override
 	public boolean onSingleTapConfirmed(MotionEvent e) {		
 		dc.currentPieceRotateLeft();
 		return false;
 	}
 
 
 
     
 }
