 package com.psywerx.dh;
 
 import javax.microedition.khronos.egl.EGLConfig;
 import javax.microedition.khronos.opengles.GL10;
 
 import android.annotation.TargetApi;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.content.pm.ActivityInfo;
 import android.opengl.GLSurfaceView;
 import android.os.Build;
 import android.os.Bundle;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.RelativeLayout;
 import android.widget.Toast;
 
 import com.google.analytics.tracking.android.EasyTracker;
 import com.google.android.gms.games.GamesClient;
 import com.google.android.gms.games.leaderboard.LeaderboardBuffer;
 import com.google.android.gms.games.leaderboard.LeaderboardScore;
 import com.google.android.gms.games.leaderboard.LeaderboardScoreBuffer;
 import com.google.android.gms.games.leaderboard.LeaderboardVariant;
 import com.google.android.gms.games.leaderboard.OnLeaderboardScoresLoadedListener;
 import com.google.example.games.basegameutils.BaseGameActivity;
 import com.mobimicro.mobimicrosdk.Checkout;
 import com.mobimicro.mobimicrosdk.MobiMicroView;
 
 public class MainActivity extends BaseGameActivity {
 
     private MyGLSurfaceView mGLView;
     // request codes we use when invoking an external activity
     final int RC_RESOLVE = 5000, RC_UNUSED = 5001;
     private MobiMicroView ad;
     public SharedPreferences settings;
     public boolean isSignedIn(){
 	
 	return mHelper.isSignedIn();
     }
     public void login(){
 	runOnUiThread(new Runnable() {
 	    
 	    @Override
 	    public void run() {
 		if(mHelper.isSignedIn()){
 		    signOut();
 		    Toast.makeText(getApplicationContext(), "Signed out", Toast.LENGTH_SHORT).show();
 		    
 		}
 		else{
 		    beginUserInitiatedSignIn();
 		}
 		
 	    }
 	});
     }
     public void showAchievements(){
 	runOnUiThread(new Runnable() {
 
 	    @Override
 	    public void run() {
 		
 		
 		startActivityForResult(getGamesClient().getAchievementsIntent(), RC_UNUSED);
 	    }
 
 	});
     }
     public void unlockAchievement(final int id){
 	runOnUiThread(new Runnable() {
 
 	    @Override
 	    public void run() {
 		if (isSignedIn()) {
 		    getGamesClient().unlockAchievement(getString(id));
 		}
 	    }
 
 	});
     }
     public void incrementAchievement(final int id, final int amount){
 	runOnUiThread(new Runnable() {
 	    @Override
 	    public void run() {
 		if (isSignedIn()) {
 		    getGamesClient().incrementAchievement(getString(id), amount);
 		}
 	    }
 	});
     }
     public void showDonation(){
 	runOnUiThread(new Runnable() {
 	    @Override
 	    public void run() {
 		Intent intent = new Intent(ad.getContext(), Checkout.class);
 		ad.getContext().startActivity(intent);
 	    }
 
 	});
 	
     }
     
     public void showScores() {
 	runOnUiThread(new Runnable() {
 	    @Override
 	    public void run() {
 		startActivityForResult(getGamesClient().getLeaderboardIntent(getString(R.string.leaderboard)), RC_UNUSED);
 	    }
 
 	});
     }
 
     public boolean newScore(final int score) {
 	
 	
 	runOnUiThread(new Runnable() {
 
 	    @Override
 	    public void run() {
 		if (isSignedIn()) {
 		    
 		    getGamesClient().submitScore(
 			    getString(R.string.leaderboard), score);
 
 		}
 	    }
 	});
 	if(score > settings.getInt("score", 0)){
 	    Editor editor = settings.edit();
 	    editor.putInt("score", score);
 	    editor.commit();
 	    return true;
 	}
 	return false;
     }
     private void create(){
 	getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
 		WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
 	mGLView = new MyGLSurfaceView(this);
 
 	setContentView(mGLView);
 	
 	EasyTracker.getInstance().setContext(this);
     }
     @Override
     protected void onCreate(Bundle savedInstanceState) {
 	requestWindowFeature(Window.FEATURE_NO_TITLE);
 	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 	
 	
 	super.onCreate(savedInstanceState);
 	settings = getPreferences(MODE_PRIVATE);
 	create();
 	
 	int wrap = RelativeLayout.LayoutParams.WRAP_CONTENT;
 	RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(wrap, wrap);
 	params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
 	params.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
 		
 	ad = new MobiMicroView(this);
 	ad.setDeveloperID("1esagTSfY4l");
 	ad.setToken("6n810HZRQFk");
 	ad.setAppID("DuckingHipster");
 	ad.setType(MobiMicroView.CUSTOM_BANNER);
     }
     
     private void pauseGame() {
 	Sound.pauseGame();
 
 	if (Game.state != 'G')
 	    return;
 	Game.prevState = Game.state;
 	
     }
 
     private void resumeGame() {
 	MyRenderer.prev = System.currentTimeMillis();
 	Sound.resumeGame();
 	
     }
 
     @Override
     protected void onStop() {
 	super.onStop();
 
 	pauseGame();
 	EasyTracker.getInstance().activityStop(this);
 
     }
 
     @Override
     protected void onPause() {
 	
 	super.onPause();
 
 	pauseGame();
 	EasyTracker.getInstance().activityStop(this);
 	mGLView.onPause();
     }
     
     @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
     @Override
     protected void onResume() {
 	super.onResume();
 	
 	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
 	    // IMMERSIVE MODE:
 	    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
 	                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
 	                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
 	                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
 	                | View.SYSTEM_UI_FLAG_FULLSCREEN
 	                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
 	}
 	else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
 	    // DIMMED MODE:
 	    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
 	}
 	
 	
 	mGLView.onResume();
 	resumeGame();
 	EasyTracker.getInstance().activityStart(this);
     }
 
     @Override
     protected void onStart() {
 	super.onStart();
 	resumeGame();
 	
 	EasyTracker.getInstance().activityStart(this);
     }
 
     @Override
     public void onSignInFailed() {
     }
 
     @Override
     public void onSignInSucceeded() {
 	
 	getGamesClient().loadPlayerCenteredScores(new OnLeaderboardScoresLoadedListener() {
 	    
 	    @Override
 	    public void onLeaderboardScoresLoaded(int status, LeaderboardBuffer board,
 		    LeaderboardScoreBuffer scores) {
 
 		if(status == GamesClient.STATUS_OK && scores.getCount() > 0){
 		    LeaderboardScore score = scores.get(0);
 		    int highScore = (int) score.getRawScore();
 		    int localScore = settings.getInt("score", 0);
 		    if(highScore > localScore){
 			Editor editor = settings.edit();
 			editor.putInt("score", (int)highScore);
 			editor.commit();
 		    }
 		    else if(highScore < localScore){
 			getGamesClient().submitScore(
 				    getString(R.string.leaderboard), localScore);
 		    }
 		}
 		else{
 		}
 	    }
 	}, getString(R.string.leaderboard), LeaderboardVariant.TIME_SPAN_ALL_TIME, LeaderboardVariant.COLLECTION_PUBLIC, 1);
     }
 }
 
 class MyGLSurfaceView extends GLSurfaceView {
 
     private Context c;
 
     public MyGLSurfaceView(Context context) {
 	super(context);
 	c = context;
 	// Set the Renderer for drawing on the GLSurfaceView
 	setEGLContextClientVersion(2);
 	setRenderer(new MyRenderer(context));
     }
 
     @Override
     public boolean onTouchEvent(MotionEvent e) {
 	return Input.update(e, (MainActivity)c);
 	
     }
 }
 
 class MyRenderer implements GLSurfaceView.Renderer {
 
     static protected Context context;
     static protected MainActivity ma;
     static public long prev;
 
     public MyRenderer(Context context) {
 	MyRenderer.context = context;
 	MyRenderer.ma = (MainActivity)context;
     }
 
     @Override
     public void onDrawFrame(GL10 unused) {
 	
 	Game.isSignedIn = MyRenderer.ma.isSignedIn();
 
 	long now = System.currentTimeMillis();
 
 	Game.draw();
 	Game.tick((float) (now - prev));
 	prev = now;
     }
 
     @Override
     public void onSurfaceChanged(GL10 gl, int width, int height) {
	Game.HEIGHT = height;
 	Game.WIDTH = width;
     }
 
     @Override
     public void onSurfaceCreated(GL10 gl, EGLConfig config) {
 	GlProgram program = new GlProgram(context);
 	Game.create(program, MyRenderer.ma.settings);
 	prev = System.currentTimeMillis();
     }
 
     
     
 }
