 package com.liongrid.infectomancer;
 
 import com.liongrid.gameengine.LCamera;
 import com.liongrid.gameengine.LGameActivityInterface;
 import com.liongrid.gameengine.LGamePointers;
 import com.liongrid.gameengine.LGestureDetector;
 import com.liongrid.gameengine.LInputDeligator;
 import com.liongrid.gameengine.LButton;
 import com.liongrid.gameengine.LView;
 import com.liongrid.gameengine.LSurfaceViewPanel;
 import com.liongrid.gameengine.LUpgrade;
 import com.liongrid.gameengine.LMap;
 import com.liongrid.infectomancer.upgrades.IUpgrade;
 import com.liongrid.infectomancer.R;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.graphics.Bitmap;
 import android.graphics.drawable.BitmapDrawable;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.PowerManager;
 import android.util.Log;
 import android.view.Display;
 import android.view.GestureDetector;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.widget.Button;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 /**
  * @author Lastis
  *		This activity is the upper class for the whole game play
  */
 public class IGameActivity extends Activity implements LGameActivityInterface{
 
 	private static final String SAVE_PREF_NAME = "infectoSave";
 
 	public static Context CONTEXT;
 
 	//To keep screen alive
 	private PowerManager.WakeLock wl;
 
 
 	private LGestureDetector mGestureDetector;
 
 	private static final boolean useScreenshot = false;
 	public static IGamePointers infectoPointers;
 
 	private Handler mHandler;
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		Log.d(IMainMenuActivity.TAG,"In IGameActivity onCreate");
 		
 		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
 		wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "DoNotDimScreen");
 		
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		
 		setScreenDimensionsAndScale();
 		
 		CONTEXT = this;
 		setContentView(R.layout.loading_game);
 		LSurfaceViewPanel panel = new LSurfaceViewPanel(this);
 		if(savedInstanceState == null){
 			mHandler = new Handler();
 			ProgressBar progress = (ProgressBar) findViewById(R.id.loadingGameBar);
 			progress.setIndeterminate(false);
 			
 			(new Thread(new IGameLoader(panel,this,mHandler,progress))).start();
 		}else{
 			LGamePointers.panel = panel;
 			setContentView(LGamePointers.panel);
 		}
 		
 		
 	}
 
 	public void onFinishGameLoad(){
 		setUpInputHandler();
 		Log.d("Infectosaurus", "Game loaded");
 		setContentView(LGamePointers.panel);
 	}
 	
 	private void setUpInputHandler(){
 		IGameScreenInput gameInput = new IGameScreenInput();
 		LView hudInput = new LButton();
		LView notherInput = new LButton();
 		LGamePointers.panel.addToRoot(hudInput);
		LGamePointers.panel.addToRoot(notherInput);
 		
 		setGestureDetector(new LGestureDetector
 				(this, new LInputDeligator(hudInput,gameInput)));
 	}
 
 
 	public static void loadData(Context context){
 
 		SharedPreferences data = context.getSharedPreferences(SAVE_PREF_NAME, 0);
 		IUpgrade[] upgrades = IUpgrade.values();
 		int upgradeCount = upgrades.length; 
 		for (int i = 0; i < upgradeCount; i++) {
 			LUpgrade<?> upgrade = upgrades[i].get();
 			int newRank = data.getInt(upgrades[i].name(), -1);
 			if(newRank < 0) continue;
 			upgrade.setRank(newRank);
 		}
 
 		int coins = data.getInt("coins", -1);
 		if(coins != -1){
 			IGamePointers.coins = data.getInt("coins", 0);
 		}
 	}
 
 	public static void saveData(Context context){
 		SharedPreferences data = context.getSharedPreferences(SAVE_PREF_NAME, 0);
 		SharedPreferences.Editor editor = data.edit();
 
 		editor.putInt("coins", IGamePointers.coins);
 
 		IUpgrade[] upgrades = IUpgrade.values();
 		int upgradeCount = upgrades.length; 
 		for (int i = 0; i < upgradeCount; i++) {
 			LUpgrade<?> upgrade = upgrades[i].get();
 			editor.putInt(upgrades[i].name(), upgrade.getRank());
 		}
 		editor.commit();
 	}
 
 
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		//outState.putSerializable("GamePointers", (Serializable) LBaseObject.gamePointers);
 	}
 
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		// Gesture detection
 		if (mGestureDetector.onTouchEvent(event)) {
 			return true;
 		}
 		return false;
 	}
 
 	public void setScreenDimensionsAndScale() {
 		Display display = getWindowManager().getDefaultDisplay(); 
 		/* Now we can retrieve all display-related infos */
 		LCamera.init(display.getHeight(), 
 				display.getWidth(), 
 				LMap.TILE_SIZE);
 		LCamera.setUnitsPerHeight(12);
 	}
 
 	@Override
 	public void finish() {
 		LGamePointers.panel.finish();
 		super.finish();
 	}
 
 	@Override
 	protected void onPause(){
 		super.onPause();
 		Log.d("Infectosaurus", "onPause()");
 		wl.release();
 		if(LGamePointers.panel != null){
 			LGamePointers.panel.onPause();
 		}
 		saveData(getApplicationContext());
 	}
 
 	@Override
 	protected void onResume(){
 		super.onResume();
 		Log.d("Infectosaurus", "onResume()"); 
 		wl.acquire();
 		if(LGamePointers.panel != null){
 			LGamePointers.panel.onResume();
 		}
 		
 		loadData(getApplicationContext());
 	}
 
 
 
 	public void roundOver(final int humansKilled, final int coinsGained) {
 
 		if(useScreenshot){
 			LGamePointers.renderThread.takeScreenShot();
 			while(LGamePointers.renderThread.lastScreenshot == null){
 				try {
 					Thread.sleep(10);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 
 
 		LGamePointers.gameThread.stopRunning();
 
 
 		runOnUiThread(new Runnable() {
 
 			public void run() {
 				setContentView(R.layout.round_over);
 				
 				if(useScreenshot){
 					View mainView = findViewById(R.id.roundOverMainLayout);
 					Bitmap bmp = LGamePointers.renderThread.lastScreenshot;
 					if(bmp != null){
 						mainView.setBackgroundDrawable(new BitmapDrawable(bmp));
 					}
 				}
 				
 				Button backButton = (Button) findViewById(R.id.returnButton);
 				backButton.setOnClickListener(new OnClickListener() {
 					public void onClick(View v) {
 						finish();
 					}
 				});
 				
 				TextView roundOverText = (TextView) findViewById(R.id.roundOverText);
 				int totalCoins =  IGamePointers.coins;
 				roundOverText.setText("Round over! Humans killed: "+humansKilled+
 						" Coins gained: " + coinsGained +
 						". Total coins: " + totalCoins+". ");
 			}
 		});
 	}
 
 	public void setGestureDetector(LGestureDetector lGestureDetector) {
 		this.mGestureDetector = lGestureDetector;
 	}
 }
