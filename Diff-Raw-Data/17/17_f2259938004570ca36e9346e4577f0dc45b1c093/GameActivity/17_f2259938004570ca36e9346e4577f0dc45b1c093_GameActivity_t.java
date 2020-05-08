 package ioio.examples.hello;
 
 import ioio.examples.hello.R;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 
 import com.google.android.gcm.GCMRegistrar;
 
 import database.Reuploader;
 
 import game.BackgroundHandler;
 import game.BracDataHandler;
 import game.GameDB;
 import game.GameMenuHandler;
 import game.GamePopupWindowHandler;
 import game.GameState;
 import game.TreeGame;
 import game.interaction.InteractiveGameHandler;
 import game.interaction.InteractivePopupWindowHandler;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Point;
 import android.graphics.Rect;
 import android.util.Log;
 import android.view.Display;
 import android.view.View;
 import android.view.animation.AlphaAnimation;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.widget.ImageView;
 
 
 public class GameActivity extends Activity{
 	
 	/*communication with MainACtivity*/
 	private static GameActivity ga = null;
 	public static final int REQUEST_TEST = 99;
 	private static final int MAX_COIN = GameState.MAX_COINS; 
 	private ImageView[] coin_image = new ImageView[MAX_COIN];
 	private ImageView background;
 	private ImageView background_anime;
 	private ImageView tree;
 	private ImageView tree_anime;
 	private TreeGame treeGame=null;
 	private GameDB gDB=null;
 	private Animation appear_anim;
 	private Animation disappear_anim;
 	private ImageView setting_image;
 	private GamePopupWindowHandler gPopWindow;
 	private InteractivePopupWindowHandler iPopWindow;
 	private GameMenuHandler gMenu;
 	private InteractiveGameHandler gInteractiveGame;
 	private Reuploader reuploader;
 	
 	
 	private Bitmap cur_bg = null;
 	private Bitmap bg_now = null;
 	private Bitmap tree_now = null;
 	private Bitmap tree_prev = null;
 	
 	ArrayList<HashMap<String,Object>> game_list = new ArrayList<HashMap<String,Object>>();
 
 	public Context context;
 
 	private Point screen_size;
 	
 	AsyncTask<Void, Void, Void> mRegisterTask;
 	String regId;
 	@SuppressWarnings("deprecation")
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		 
 		 Display display = getWindowManager().getDefaultDisplay();
 		 if (Build.VERSION.SDK_INT<13){
 			 int w = display.getWidth();
 			 int h = display.getHeight();
 			 screen_size = new Point(w,h);
 		 }
 		 else{
 			 screen_size = getSize(display);
 		 }
 		
 		setContentView(R.layout.activity_game);
 		gDB = new GameDB(this);
 		GameState gState = gDB.getLatestGameState();
 		treeGame=new TreeGame(gState);
 		initBackground();
 		initAnim();
 		initTreeImage();
 		initCoinImage();
 		initSettingButton();
 		setImage();
 		gPopWindow = new GamePopupWindowHandler(this);
 		iPopWindow = new InteractivePopupWindowHandler(this);
 		gMenu = new GameMenuHandler(this);
 		gInteractiveGame = new InteractiveGameHandler(this);
 		context = this;
 		reuploader = new Reuploader(this);
 		reuploader.reTransmission();
 		initRegistration();
 		
 		Intent intent = this.getIntent();
 		if (intent != null){
 			boolean notify = intent.getBooleanExtra("notify", false);
 			if (notify){
 				Intent newActivity;
 				newActivity = new Intent(context, MainActivity.class);  
 				startActivityForResult(newActivity, REQUEST_TEST);
 			}
 			String msg = intent.getStringExtra("msgmsg");
 			if (msg != null){
 				iPopWindow.showPopWindow(msg);
 			}
 		}
 		
       	Intent service_intent = new Intent(this, TimerService.class);
       	startService(service_intent);
       	
       	//iPopWindow.showPopWindow("xxx");
 	}
 	
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
 	private Point getSize(Display display){
 		Point p = new Point();
 		display.getSize(p);
 		return p;
 	}
 	
 	public Point getSize(){
 		return screen_size;
 	}
 	
 	protected void onPause(){
 		super.onPause();
 		ga = null;
 		if (gInteractiveGame != null)
 			gInteractiveGame.clear();
 
 	}
 	
 	protected void onDestroy(){
 		ga = null;
         if (mRegisterTask != null) {
             mRegisterTask.cancel(true);
         }
         unregisterReceiver(mHandleMessageReceiver);
 		if (cur_bg != null){
 			cur_bg.recycle();
 			cur_bg = null;
 		}
 		if (bg_now != null){
 			bg_now.recycle();
 			bg_now = null;
 		}
 		if (tree_now != null){
 			tree_now.recycle();
 			tree_now = null;
 		}
 		if (tree_prev != null){
 			tree_prev.recycle();
 			tree_prev = null;
 		}
 		super.onDestroy();
 	}
 	
 	protected void onResume(){
 		super.onResume();
 		ga = this;
 		System.gc();
 		gInteractiveGame.update();
 	}
 	
 	protected void onNewIntent(Intent intent){
 		super.onNewIntent(intent);
 		if (intent != null){
 			boolean notify = intent.getBooleanExtra("notify", false);
 			if (notify){
 				Intent newActivity;
 				newActivity = new Intent(context, MainActivity.class);  
 				startActivityForResult(newActivity, REQUEST_TEST);
 			}
 			String msg = intent.getStringExtra("msgmsg");
 			if (msg != null){
 				iPopWindow.showPopWindow(msg);
 			}
 		}
 	}
 	public static void showCheerMessage(String msg){
 		if (ga!= null){
 			if (msg != null)
				if (ga.iPopWindow != null)
					
 				ga.iPopWindow.showPopWindow(msg);
 		}
 	}
 	
 	
 	private void initBackground(){
 		background = (ImageView) findViewById(R.id.background);
 		background_anime = (ImageView) findViewById(R.id.background_anime);
 		background_anime.setVisibility(View.INVISIBLE);
 	}
 	
 	private void initAnim(){
 		/*used for initializing animations*/
 		appear_anim = new AlphaAnimation(0.f,1.f);
 		appear_anim.setDuration(1000);
 		appear_anim.setStartOffset(30);
 		disappear_anim = new AlphaAnimation(1.f,0.f);
 		disappear_anim.setDuration(1000);
 		disappear_anim.setStartOffset(30);
 	}
 	
 	private final int[] treeImg = {
 			R.drawable.tree1,R.drawable.tree2,R.drawable.tree4,
 			R.drawable.tree4,R.drawable.tree5,R.drawable.tree6,
 			R.drawable.tree7
 	};
 	
 	private void initTreeImage(){
 		/*used for initializing tree images*/
 		tree = (ImageView) findViewById(R.id.tree1);
 		tree_anime = (ImageView) findViewById(R.id.tree2);
 	}
 	private void initCoinImage(){
 		/*used for initializing coin images*/
 		coin_image[0] = (ImageView) findViewById(R.id.coin1);
 		coin_image[1] = (ImageView) findViewById(R.id.coin2);
 		coin_image[2] = (ImageView) findViewById(R.id.coin3);
 		coin_image[3] = (ImageView) findViewById(R.id.coin4);
 	}	
 	
 	private void initRegistration(){
 		
 		GCMRegistrar.checkDevice(this);
 		GCMRegistrar.checkManifest(this);
 		registerReceiver(mHandleMessageReceiver,  new IntentFilter("GCM_RECEIVE_ACTION"));
 		regId = GCMRegistrar.getRegistrationId(this);
         if (regId.equals("")){
         	Log.d("GCM","start register 0");
             GCMRegistrar.register(this, CommonUtilities.SENDER_ID);
         }
 		else {
             if (GCMRegistrar.isRegisteredOnServer(this))  Log.d("GCM","skip register");
             else {
             	Log.d("GCM","start register");
                 mRegisterTask = new AsyncTask<Void, Void, Void>() {
                     @Override
                     protected Void doInBackground(Void... params) {
                         boolean registered = ServerUtilities.register(context, regId);
                         if (!registered)  GCMRegistrar.unregister(context);
                         return null;
                     }
                     @Override
                     protected void onPostExecute(Void result) {mRegisterTask = null; }
                 };
                 mRegisterTask.execute(null, null, null);
             }
         }
 	}
 	
 	
 	private void initSettingButton(){
 		setting_image= (ImageView) findViewById(R.id.setting_button);
 		setting_image.setClickable(true);
 		setting_image.setOnClickListener(new SettingButtonOnClickListener());
 	}
 	
 	private void setImage(){
 		/*set image visibility*/
 		if (cur_bg != null)
 			cur_bg.recycle();
 		GameState gState=treeGame.getGameState();
 		Bitmap tmp = BitmapFactory.decodeResource(this.getResources(),treeImg[gState.state]);
 		tree_now = Bitmap.createScaledBitmap(tmp, 256, 256, true);
 		tmp.recycle();
 		tree.setImageBitmap(tree_now);
 		for (int i=0;i<MAX_COIN;++i)
 			coin_image[i].setVisibility(ImageView.INVISIBLE);
 		for (int i=0;i<gState.coin;++i)
 			coin_image[i].setVisibility(ImageView.VISIBLE);
 		cur_bg = BitmapFactory.decodeResource(this.getResources(), BackgroundHandler.getBackgroundDrawableId(gState.state, gState.coin));
 		background.setImageBitmap(cur_bg);
 	}
 	
 	
 	private void setImageChange(GameState oldState){
 		GameState gState=treeGame.getGameState();
 		if (oldState.coin < gState.coin){
 			if (oldState.coin ==GameState.MIN_COINS && gState.coin == GameState.MAX_COINS){
 				//get coin because state drop
 				for (int i=oldState.coin;i<gState.coin;++i){
 					coin_image[i].setVisibility(View.VISIBLE);
 					coin_image[i].startAnimation(appear_anim);
 				}
 			}
 			else{
 				Animation a;
 				a = AnimationUtils.loadAnimation(this, R.anim.coin_appear_anim);
 				for (int i=oldState.coin;i<gState.coin;++i){
 					coin_image[i].setVisibility(View.VISIBLE);
 					coin_image[i].clearAnimation();
 					coin_image[i].startAnimation(a);
 				}
 			}
 		}
 		else if (oldState.coin > gState.coin){
 			if (oldState.coin ==GameState.MAX_COINS && gState.coin == GameState.MIN_COINS){
 				//lose coin because state increases
 				for (int i=gState.coin;i<oldState.coin;++i){
 					coin_image[i].startAnimation(disappear_anim);
 					coin_image[i].setVisibility(ImageView.INVISIBLE);
 				}
 			}
 			else{
 				Animation a;
 				a = AnimationUtils.loadAnimation(this, R.anim.coin_disappear_anim_type1);
 				for (int i=gState.coin;i<oldState.coin;++i){
 					coin_image[i].clearAnimation();
 					coin_image[i].startAnimation(a);
 					coin_image[i].setVisibility(ImageView.INVISIBLE);
 				}
 			}
 		}
 		
 		if (oldState.state != gState.state){
 			if (tree_prev != null){
 				tree_prev.recycle();
 				tree_prev = null;
 			}
 			tree_prev = tree_now;
 			Bitmap tmp = BitmapFactory.decodeResource(this.getResources(),treeImg[gState.state]);
 			tree_now = Bitmap.createScaledBitmap(tmp, 256, 256, true);
 			tmp.recycle();
 			tree_anime.setImageBitmap(tree_prev);
 			tree_anime.setVisibility(View.VISIBLE);
 			tree.setImageBitmap(tree_now);
 			tree_anime.startAnimation(disappear_anim);
 			tree.startAnimation(appear_anim);
 			tree_anime.setVisibility(View.INVISIBLE);
 		}
 		
 		if (oldState.coin != gState.coin || oldState.state != gState.state){
 			if (bg_now != null){
 				bg_now.recycle();
 				bg_now = null;
 			}
 			bg_now = cur_bg;
 			cur_bg = BitmapFactory.decodeResource(this.getResources(), BackgroundHandler.getBackgroundDrawableId(gState.state, gState.coin));
 			background_anime.setImageBitmap(bg_now);
 			background_anime.setVisibility(View.VISIBLE);
 			background.setImageBitmap(cur_bg);
 			background_anime.startAnimation(disappear_anim);
 			background.setAnimation(appear_anim);
 			background_anime.setVisibility(View.INVISIBLE);			
 		}
 	}	
 	
 	public void getCoin(){
 		GameState oldState = treeGame.getPrevState();
 		this.setImageChange(oldState);
 	}
 	public void loseCoin(){
 		GameState oldState = treeGame.getPrevState();
 		this.setImageChange(oldState);
 	}
 	
 	/*OnListenerForSettingButton*/
 	private class SettingButtonOnClickListener implements View.OnClickListener{
 		public void onClick(View v) {
 			gMenu.changeMenuVisibility();
 		}
 	}
 
 	public InteractiveGameHandler getInteractiveGameHandler(){
 		return gInteractiveGame;
 	}
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data){
 		if (requestCode == REQUEST_TEST && resultCode == RESULT_OK){
 			Bundle ts_bundle = data.getExtras();  
             String ts = ts_bundle.getString("testfilename");  
             startBracDataHandler(ts);
 		}
 	}
 
 	private void startBracDataHandler(String ts){
 		BracDataHandler bdh = new BracDataHandler(ts,context,treeGame,gDB);
 		int result = bdh.start();
 		gPopWindow.showPopWindow(result);
 	}
 
     private final BroadcastReceiver mHandleMessageReceiver =
             new BroadcastReceiver() {
         @Override
         public void onReceive(Context context, Intent intent) {
         }
     };
 }
