 package at.frikiteysch.repong;
 
 import java.util.logging.Logger;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.graphics.Point;
 import android.os.Bundle;
 import android.support.v4.content.LocalBroadcastManager;
 import android.util.DisplayMetrics;
 import android.view.Display;
 import android.view.Gravity;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.view.WindowManager;
 import android.widget.FrameLayout;
 import android.widget.ImageView;
 import android.widget.Toast;
 import at.frikiteysch.repong.communication.AsyncTaskSend;
 import at.frikiteysch.repong.communication.AsyncTaskSendReceive;
 import at.frikiteysch.repong.communication.AsyncTaskSendReceive.AsyncTaskStateReceiver;
 import at.frikiteysch.repong.defines.Position;
 import at.frikiteysch.repong.defines.RePongDefines.PaddleOrientation;
 import at.frikiteysch.repong.services.GamePlayService;
 import at.frikiteysch.repong.storage.ProfileManager;
 
 public class ActivityGame extends Activity implements OnTouchListener, AsyncTaskStateReceiver<ComGameData> {
 	 
 	ImageView paddleSouth, paddleWest, paddleEast, paddleNorth;
 	ImageView ball;
 	Context paddleSouthContext, paddleWestContext, paddleEastContext, paddleNorthContext;
 	float lastX = 0, lastY = 0;
	int displayWidth, displayHeight, paddleHalfWidth, lastLeftMargin;
 	private int screenWidth, screenHeight;
 	private int gameId;
 	
 	private static final Logger LOGGER = Logger.getLogger(ActivityGame.class.getName());
 	
 
 	@SuppressLint("NewApi")
 	@SuppressWarnings("deprecation")
 	protected void onCreate(Bundle savedInstanceState) 
 	{
 	    super.onCreate(savedInstanceState);
 	    setContentView(R.layout.game_field);
 	    
 	    DisplayMetrics displayMetrics = new DisplayMetrics();
 	    WindowManager wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE); // the results will be higher than using the activity context object or the getWindowManager() shortcut
 	    wm.getDefaultDisplay().getMetrics(displayMetrics);
 	    screenWidth = displayMetrics.widthPixels;
 	    screenHeight = displayMetrics.heightPixels;
 	    //Intent intent = getIntent();
 	    //String value = intent.getStringExtra("key"); //if it's a string you stored.
 	
 	    paddleSouth = (ImageView) findViewById(R.id.paddleSouth);
 	    paddleWest = (ImageView) findViewById(R.id.paddleWest);
 		paddleEast = (ImageView) findViewById(R.id.paddleEast);
 		paddleNorth = (ImageView) findViewById(R.id.paddleNorth);
 		
 		paddleSouthContext = paddleSouth.getContext();
 		paddleWestContext = paddleWest.getContext();
 		paddleEastContext = paddleEast.getContext();
 		paddleNorthContext = paddleNorth.getContext();
 		
 	    ball = (ImageView) findViewById(R.id.ball);
 	    
 	    gameId = getIntent().getIntExtra("gameId", -1);
 	    FrameLayout layout = (FrameLayout) findViewById(R.id.gameField);
         layout.setOnTouchListener(this);
         
         Display display = getWindowManager().getDefaultDisplay(); 
         
         if (android.os.Build.VERSION.SDK_INT >= 13) {
         	Point size = new Point();
         	display.getSize(size);
         	displayWidth = size.x;
         	displayHeight = size.y;
         }
         else {
         	displayWidth = display.getWidth();  // deprecated
         	displayHeight = display.getHeight();  // deprecated
         }
         
         FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) paddleSouth.getLayoutParams();
         params.gravity = Gravity.LEFT | Gravity.BOTTOM;
 		params.leftMargin = 0;
 		params.topMargin = 0;
 		
 		paddleSouth.setLayoutParams(params);
 		paddleSouth.invalidate();
 		
         
         Intent intent = new Intent(this, GamePlayService.class);
         startService(intent);
 	}
 	
 	@Override 
 	public void onWindowFocusChanged (boolean hasFocus) 
 	{
		paddleHalfWidth = paddleSouth.getWidth() / 2;
 	}
 
 	@Override
 	public boolean onTouch(View v, MotionEvent event) 
 	{
 		switch (event.getAction())
 		{
 			case MotionEvent.ACTION_DOWN:
 	        {       
 	            // Here u can write code which is executed after the user touch on the screen 
 	        	lastX = event.getX();
 	        	lastLeftMargin = ((FrameLayout.LayoutParams) paddleSouth.getLayoutParams()).leftMargin;
 	        	break; 
 	        }
 	        case MotionEvent.ACTION_UP:
 	        {             
 	            // Here u can write code which is executed after the user release the touch on the screen    
 	        	
 	            break;
 	        }
 	        case MotionEvent.ACTION_MOVE:
 	        {  
 	           // Here u can write code which is executed when user move the finger on the screen 
 	        	
 	        	FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) paddleSouth.getLayoutParams();
 	        	/*
 	        	float tmpY = event.getY();
 	        	float tmpX = event.getX();
 	        	
 	        	if (tmpX < lastX)
 	        		params.leftMargin -= 10;
 	        	else
 	        		params.leftMargin += 10;*/
 	        	
 	        	params.gravity = Gravity.LEFT | Gravity.BOTTOM;
 	        	
 	        	//int tmpCalc = (int) (event.getX() - paddleHalfWidth);
 	        	int tmpCalc = lastLeftMargin + (int) (event.getX() - lastX);
 	        	
 	        	//Toast.makeText(getApplicationContext(), lastLeftMargin + " " + event.getX() + " " + lastX, Toast.LENGTH_SHORT).show();
 	        	
 	        	if (tmpCalc < 0)
 	        	{
 	        		params.leftMargin = 0;
 	        		lastX = event.getX();
 	        		lastLeftMargin = ((FrameLayout.LayoutParams) paddleSouth.getLayoutParams()).leftMargin;
 	        	}
	        	else if (tmpCalc + paddleHalfWidth * 2 > displayWidth)
 	        	{
	        		params.leftMargin = displayWidth - paddleHalfWidth * 2;
 	        		lastX = event.getX();
 	        		lastLeftMargin = ((FrameLayout.LayoutParams) paddleSouth.getLayoutParams()).leftMargin;
 	        	}
 	        	else	        	
 	        		params.leftMargin = tmpCalc;
 
 	            //params.bottomMargin = (int) event.getY();
 	            paddleSouth.setLayoutParams(params);
 	            paddleSouth.invalidate();
 	            break;
 	        }
 	    }
 		
 	    return true;
 	}
 	
 	@Override
 	protected void onStop() {
 		super.onStop();
 		Intent intent = new Intent(this, GamePlayService.class);
 		stopService(intent);
 	};
 	
 	@Override
 	public void onBackPressed() {
 		Intent intent = new Intent(this, GamePlayService.class);
 		stopService(intent);
 		
 		ComLeaveGame leaveGame = new ComLeaveGame();
 		leaveGame.setGameId(gameId);
 		leaveGame.setUserId(ProfileManager.getInstance().getProfile().getUserId());
 		
 		AsyncTaskSend<ComLeaveGame> task = new AsyncTaskSend<ComLeaveGame>(leaveGame);
 		task.execute();
 		
 		Intent myIntent = new Intent(this, ActivityStartScreen.class);
 		myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 		this.startActivity(myIntent);
 	}
 	
 	
 	private BroadcastReceiver receiver = new BroadcastReceiver(){
 		@Override
 		public void onReceive(Context ctx, Intent intent) {
 			sendPaddlePosition();
 		}
 	};
 	
 	private void sendPaddlePosition()
 	{
 		ComPaddlePosition position = new ComPaddlePosition();
 		position.setGameId(gameId);
 		position.setUserId(ProfileManager.getInstance().getProfile().getUserId());
 		
 		int paddleLeftMargin = (int) (paddleSouth.getLeft() * (1000D/((double)screenWidth)));
 		int paddleWidth = (int) (paddleSouth.getWidth() * (1000D/((double)screenWidth)));
 		
 		position.setPositionNorm(paddleLeftMargin);
 		position.setWidthNorm(paddleWidth);
 		AsyncTaskSendReceive<ComPaddlePosition, ComGameData> task = new AsyncTaskSendReceive<ComPaddlePosition, ComGameData>(ComGameData.class, this, position);
 		task.execute();
 	}
 	
 	@Override
 	protected void onResume()
 	{
 		super.onResume();
 		IntentFilter filter = new IntentFilter();
 	    filter.addAction(GamePlayService.updateRequestAction);
 	    LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);
 	}
 	
 	@Override
 	protected void onPause()
 	{
 		super.onPause();
 		LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
 	}
 
 	@Override
 	public void receivedOkResult(ComGameData resultObject) {		
 		int leftMargin = 0, topMargin = 0;
 		PaddleOrientation myOrientation = PaddleOrientation.SOUTH;
 		FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) ball.getLayoutParams();
 		Position p = resultObject.getBall().getPosition();
 		
 		FrameLayout.LayoutParams paramsNorth = (FrameLayout.LayoutParams) paddleNorth.getLayoutParams();
 		paramsNorth.gravity = Gravity.LEFT | Gravity.TOP;
 		
 		FrameLayout.LayoutParams paramsWest = (FrameLayout.LayoutParams) paddleWest.getLayoutParams();
 		paramsWest.gravity = Gravity.LEFT | Gravity.TOP;
 		
 		FrameLayout.LayoutParams paramsEast = (FrameLayout.LayoutParams) paddleEast.getLayoutParams();
 		paramsEast.gravity = Gravity.RIGHT | Gravity.TOP;
 
 		// Set ball position according to orientation
 		for (Player player : resultObject.getPlayerList()) {
 			if (player.getUserId() == ProfileManager.getInstance().getProfile().getUserId()) {
 				myOrientation = player.getOrientation();
 				
 				switch(myOrientation)
 				{
 					case SOUTH:
 						leftMargin = (int) (p.getX() * (((double)screenWidth)/1000D));
 						topMargin = (int) (p.getY() * (((double)screenHeight)/1000D));
 						switch(resultObject.getPlayerList().size())
 						{
 							case 1:
 								paddleNorth.setVisibility(View.GONE);
 						
 							case 2:
 								paddleWest.setVisibility(View.GONE);
 								
 							case 3:
 								paddleEast.setVisibility(View.GONE);
 								
 							case 4:
 								break;
 						}
 						break;
 						
 					case NORTH:
 						leftMargin = screenWidth - (int) (p.getX() * (((double)screenWidth)/1000D));
 						topMargin = screenHeight - (int) (p.getY() * (((double)screenHeight)/1000D));
 						switch(resultObject.getPlayerList().size())
 						{
 							case 2:
 								paddleEast.setVisibility(View.GONE);
 								
 							case 3:
 								paddleWest.setVisibility(View.GONE);
 								
 							case 4:
 								break;
 						}
 						break;
 						
 					case WEST:
 						leftMargin = (int) (p.getY() * (((double)screenWidth)/1000D));
 						topMargin = screenHeight - (int) (p.getX() * (((double)screenHeight)/1000D));
 						switch(resultObject.getPlayerList().size())
 						{								
 							case 3:
 								paddleNorth.setVisibility(View.GONE);
 								
 							case 4:
 								break;
 						}
 						break;
 						
 					case EAST:
 						leftMargin = screenWidth - (int) (p.getY() * (((double)screenWidth)/1000D));
 						topMargin = (int) (p.getX() * (((double)screenHeight)/1000D));
 						break;
 				}
 			}
 		}
 		
 		
 		// paddle color
 		int i;
 		for (Player player : resultObject.getPlayerList()) {
 			switch(myOrientation)
 			{
 				case SOUTH:
 					switch(player.getOrientation())
 					{
 						case SOUTH:
 							paddleSouth.setVisibility(View.VISIBLE);
 							i = paddleSouthContext.getResources().getIdentifier("paddlehorizontalrot" + player.getLifes(), "drawable", paddleSouthContext.getPackageName());
 							paddleSouth.setImageResource(i);
 							break;
 					
 						case NORTH:
 							paddleNorth.setVisibility(View.VISIBLE);
 							i = paddleNorthContext.getResources().getIdentifier("paddlehorizontalblau" + player.getLifes(), "drawable", paddleNorthContext.getPackageName());
 							paddleNorth.setImageResource(i);
 							break;
 							
 						case WEST:
 							paddleWest.setVisibility(View.VISIBLE);
 							i = paddleWestContext.getResources().getIdentifier("paddlevertikalgelb" + player.getLifes(), "drawable", paddleWestContext.getPackageName());
 							paddleWest.setImageResource(i);
 							break;
 							
 						case EAST:
 							paddleEast.setVisibility(View.VISIBLE);
 							i = paddleEastContext.getResources().getIdentifier("paddlevertikalgruen" + player.getLifes(), "drawable", paddleEastContext.getPackageName());
 							paddleEast.setImageResource(i);
 							break;
 					}
 					break;
 					
 				case NORTH:
 					switch(player.getOrientation())
 					{
 						case SOUTH:
 							paddleNorth.setVisibility(View.VISIBLE);
 							i = paddleNorthContext.getResources().getIdentifier("paddlehorizontalrot" + player.getLifes(), "drawable", paddleNorthContext.getPackageName());
 							paddleNorth.setImageResource(i);
 							break;
 					
 						case NORTH:
 							paddleSouth.setVisibility(View.VISIBLE);
 							i = paddleSouthContext.getResources().getIdentifier("paddlehorizontalblau" + player.getLifes(), "drawable", paddleSouthContext.getPackageName());
 							paddleSouth.setImageResource(i);
 							break;
 							
 						case WEST:
 							paddleEast.setVisibility(View.VISIBLE);
 							i = paddleEastContext.getResources().getIdentifier("paddlevertikalgelb" + player.getLifes(), "drawable", paddleEastContext.getPackageName());
 							paddleEast.setImageResource(i);
 							break;
 							
 						case EAST:
 							paddleWest.setVisibility(View.VISIBLE);
 							i = paddleWestContext.getResources().getIdentifier("paddlevertikalgruen" + player.getLifes(), "drawable", paddleWestContext.getPackageName());
 							paddleWest.setImageResource(i);
 							break;
 					}
 					break;
 					
 				case WEST:
 					switch(player.getOrientation())
 					{
 						case SOUTH:
 							paddleEast.setVisibility(View.VISIBLE);
 							i = paddleEastContext.getResources().getIdentifier("paddlevertikalrot" + player.getLifes(), "drawable", paddleEastContext.getPackageName());
 							paddleEast.setImageResource(i);
 							break;
 					
 						case NORTH:
 							paddleWest.setVisibility(View.VISIBLE);
 							i = paddleWestContext.getResources().getIdentifier("paddlevertikalblau" + player.getLifes(), "drawable", paddleWestContext.getPackageName());
 							paddleWest.setImageResource(i);
 							break;
 							
 						case WEST:
 							paddleSouth.setVisibility(View.VISIBLE);
 							i = paddleSouthContext.getResources().getIdentifier("paddlehorizontalgelb" + player.getLifes(), "drawable", paddleSouthContext.getPackageName());
 							paddleSouth.setImageResource(i);
 							break;
 							
 						case EAST:
 							paddleNorth.setVisibility(View.VISIBLE);
 							i = paddleNorthContext.getResources().getIdentifier("paddlehorizontalgruen" + player.getLifes(), "drawable", paddleNorthContext.getPackageName());
 							paddleNorth.setImageResource(i);
 							break;
 					}
 					break;
 					
 				case EAST:
 					switch(player.getOrientation())
 					{
 						case SOUTH:
 							paddleWest.setVisibility(View.VISIBLE);
 							i = paddleWestContext.getResources().getIdentifier("paddlevertikalrot" + player.getLifes(), "drawable", paddleWestContext.getPackageName());
 							paddleWest.setImageResource(i);
 							break;
 					
 						case NORTH:
 							paddleEast.setVisibility(View.VISIBLE);
 							i = paddleEastContext.getResources().getIdentifier("paddlevertikalblau" + player.getLifes(), "drawable", paddleEastContext.getPackageName());
 							paddleEast.setImageResource(i);
 							break;
 							
 						case WEST:
 							paddleNorth.setVisibility(View.VISIBLE);
 							i = paddleNorthContext.getResources().getIdentifier("paddlehorizontalgelb" + player.getLifes(), "drawable", paddleNorthContext.getPackageName());
 							paddleNorth.setImageResource(i);
 							break;
 							
 						case EAST:
 							paddleSouth.setVisibility(View.VISIBLE);
 							i = paddleSouthContext.getResources().getIdentifier("paddlehorizontalgruen" + player.getLifes(), "drawable", paddleSouthContext.getPackageName());
 							paddleSouth.setImageResource(i);
 							break;
 					}
 					break;
 			}
 		}
 		
 		
 		// Set paddle position according to orientation
 		for (Player player : resultObject.getPlayerList()) {
 			if (player.getUserId() != ProfileManager.getInstance().getProfile().getUserId()) {
 				switch(myOrientation)
 				{
 					case SOUTH:
 						switch(player.getOrientation())
 						{
 							case NORTH:
 								paramsNorth.leftMargin = screenWidth - (int) (player.getPosition() * (((double)screenWidth)/1000D)) - paddleNorth.getWidth();
 								paramsNorth.topMargin = 0;
 								
 								paddleNorth.setLayoutParams(paramsNorth);
 								paddleNorth.invalidate();
 								break;
 								
 							case WEST:
 								paramsWest.leftMargin = 0;
 								paramsWest.topMargin = (int) (player.getPosition() * (((double)screenHeight)/1000D));
 								
 								paddleWest.setLayoutParams(paramsWest);
 								paddleWest.invalidate();
 								break;
 								
 							case EAST:
 								paramsEast.leftMargin = 0;
 								paramsEast.topMargin = screenHeight - (int) (player.getPosition() * (((double)screenHeight)/1000D)) - paddleEast.getWidth();
 								
 								paddleEast.setLayoutParams(paramsEast);
 								paddleEast.invalidate();
 								break;
 								
 							default:
 								break;
 						}
 						break;
 						
 					case NORTH:
 						switch(player.getOrientation())
 						{
 							case SOUTH:
 								paramsNorth.leftMargin = screenWidth - (int) (player.getPosition() * (((double)screenWidth)/1000D)) - paddleNorth.getWidth();
 								paramsNorth.topMargin = 0;
 								
 								paddleNorth.setLayoutParams(paramsNorth);
 								paddleNorth.invalidate();
 								break;
 								
 							case WEST:
 								paramsEast.leftMargin = 0;
 								paramsEast.topMargin = screenHeight - (int) (player.getPosition() * (((double)screenHeight)/1000D)) - paddleEast.getWidth();
 								
 								paddleEast.setLayoutParams(paramsEast);
 								paddleEast.invalidate();
 								break;
 								
 							case EAST:
 								paramsWest.leftMargin = 0;
 								paramsWest.topMargin = (int) (player.getPosition() * (((double)screenHeight)/1000D));
 								
 								paddleWest.setLayoutParams(paramsWest);
 								paddleWest.invalidate();
 								break;
 								
 							default:
 								break;
 						}
 						break;
 						
 					case WEST:
 						switch(player.getOrientation())
 						{
 							case SOUTH:
 								paramsEast.leftMargin = 0;
 								paramsEast.topMargin = screenHeight - (int) (player.getPosition() * (((double)screenHeight)/1000D)) - paddleEast.getWidth();
 								
 								paddleEast.setLayoutParams(paramsEast);
 								paddleEast.invalidate();
 								break;
 								
 							case NORTH:
 								paramsWest.leftMargin = 0;
 								paramsWest.topMargin = (int) (player.getPosition() * (((double)screenHeight)/1000D));
 								
 								paddleWest.setLayoutParams(paramsWest);
 								paddleWest.invalidate();
 								break;
 								
 							case EAST:
 								paramsNorth.leftMargin = screenWidth - (int) (player.getPosition() * (((double)screenWidth)/1000D)) - paddleNorth.getWidth();
 								paramsNorth.topMargin = 0;
 								
 								paddleNorth.setLayoutParams(paramsNorth);
 								paddleNorth.invalidate();
 								break;
 								
 							default:
 								break;
 						}
 						break;
 						
 					case EAST:
 						switch(player.getOrientation())
 						{
 							case SOUTH:
 								paramsWest.leftMargin = 0;
 								paramsWest.topMargin = (int) (player.getPosition() * (((double)screenHeight)/1000D));
 								
 								paddleWest.setLayoutParams(paramsWest);
 								paddleWest.invalidate();
 								break;
 								
 							case NORTH:
 								paramsEast.leftMargin = 0;
 								paramsEast.topMargin = screenHeight - (int) (player.getPosition() * (((double)screenHeight)/1000D)) - paddleEast.getWidth();
 								
 								paddleEast.setLayoutParams(paramsEast);
 								paddleEast.invalidate();
 								break;
 								
 							case WEST:
 								paramsNorth.leftMargin = screenWidth - (int) (player.getPosition() * (((double)screenWidth)/1000D)) - paddleNorth.getWidth();
 								paramsNorth.topMargin = 0;
 								
 								paddleNorth.setLayoutParams(paramsNorth);
 								paddleNorth.invalidate();
 								break;
 								
 							default:
 								break;
 						}
 						break;
 				}
 			}
 		}
 		
 		params.gravity = Gravity.TOP;
 		params.leftMargin = leftMargin;
 		params.topMargin = topMargin;
 		
 		ball.setLayoutParams(params);
 		ball.invalidate();
 	}
 
 	@Override
 	public void receivedError(ComError errorObject) {
 		LOGGER.info("received error in ActivityGame, so creator left the game");
 		
 		Intent intent = new Intent(this, GamePlayService.class);
 		stopService(intent);
 		
 		Toast.makeText(this.getApplicationContext(), "Creator left the game, so game ended unexpectly", Toast.LENGTH_LONG).show();
 		onBackPressed();
 	}
 }
