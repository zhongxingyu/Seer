 package edu.rhit.petitjam_coblebj.roboarena;
 
 import android.animation.AnimatorSet;
 import android.animation.ObjectAnimator;
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 import android.view.GestureDetector;
 import android.view.MotionEvent;
 import android.widget.ImageView;
 import android.widget.TextView;
 import edu.rhit.petitjam_coblebj.game.BoxerGame;
 import edu.rhit.petitjam_coblebj.game.ComputerPlayer;
 
 public class ArenaActivity extends Activity {
 
 	public static final String KEY_GAME_MODE = "key_game_mode";
 	public static final String KEY_GAME_ID = "key_firebase_ref";
 	protected static final String KEY_PLAYER_ID = "key_player_id";
 	
 	// Fields
 	private GestureDetector mDetector;
 	private BoxerGame mGame;
 
 	// Images
 	public ImageView rightGlove;
 	public ImageView leftGlove;
 
 	// TextView
 	public TextView l_jab;
 	public TextView l_hook;
 	public TextView l_up;
 	public TextView r_jab;
 	public TextView r_hook;
 	public TextView r_up;
 	public TextView block;
 	
 	public TextView player1_hp_textview;
 	public TextView player2_hp_textview;
 
 	// Actions
 	private static final float jab_distance = -15;
 	private static final float hook_distance = -30;
 	private static final float uppercut_distance = -50;
 
 	// Cooldown - half what the boxer game has because each animation is a two part action
 	private static final int jab_cooldown = 500;
 	private static final int hook_cooldown = 600;
 	private static final int uppercut_cooldown = 1000;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_arena);
 
 		/* TEMP TEXTVIEWS */
 		l_jab = (TextView) findViewById(R.id.l_jab_tv);
 		l_hook = (TextView) findViewById(R.id.l_hook_tv);
 		l_up = (TextView) findViewById(R.id.l_up_tv);
 		r_jab = (TextView) findViewById(R.id.r_jab_tv);
 		r_hook = (TextView) findViewById(R.id.r_hook_tv);
 		r_up = (TextView) findViewById(R.id.r_up_tv);
 		// block = (TextView)findViewById(R.id.);
 		/* ************** */
 
 		player1_hp_textview = (TextView) findViewById(R.id.player1_hp);
 		player2_hp_textview = (TextView) findViewById(R.id.player2_hp);
 
 		rightGlove = (ImageView) findViewById(R.id.right_glove);
 		leftGlove = (ImageView) findViewById(R.id.left_glove);
 
 		int gameMode = getIntent().getIntExtra(ArenaActivity.KEY_GAME_MODE,
 				BoxerGame.GAME_MODE_COMPUTER);
 		int computerDifficulty = getIntent().getIntExtra(
 				ComputerPlayer.KEY_COMPUTER_DIFFICULTY,
 				ComputerPlayer.COMPUTER_PLAYER_DIFFICULTY_EASY);
 		
 		String gameId = getIntent().getStringExtra(KEY_GAME_ID);
 		String playerId = getIntent().getStringExtra(KEY_PLAYER_ID);
 
 		if (gameMode == BoxerGame.GAME_MODE_HUMAN) {
 			Log.d("RA", "Creating BoxerGame vs HUMAN");
 			mGame = new BoxerGame(this, gameId, playerId);
 			
 		} else if (gameMode == BoxerGame.GAME_MODE_COMPUTER) {
 			Log.d("RA", "Creating BoxerGame vs COMPUTER");
 			mGame = new BoxerGame(this, computerDifficulty);
 		}
 
 		
 		////////
 		// Erases the background of the textviews every 2 seconds
 		final Handler handler = new Handler();
 		final Runnable r = new Runnable() {
 			@Override
 			public void run() {
 				l_jab.setBackgroundColor(Color.WHITE);
 				l_hook.setBackgroundColor(Color.WHITE);
 				l_up.setBackgroundColor(Color.WHITE);
 				r_jab.setBackgroundColor(Color.WHITE);
 				r_hook.setBackgroundColor(Color.WHITE);
 				r_up.setBackgroundColor(Color.WHITE);
 
 				handler.postDelayed(this, 2000);
 			}
 		};
 		handler.postDelayed(r, 2000);
 		////////
 
 		mDetector = new GestureDetector(this, new PlayerGestureDetector());
 
 		mGame.startGame();
 
 	}
 
 	public void GameOver() {
 
 		Intent endScreenIntent = new Intent(this, MatchDetailsActivity.class);
 		startActivity(endScreenIntent);
 
 	}
 	
 	@Override
 	protected void onPause() {
 		super.onPause();
 		mGame.destroy();
 	}
 
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		return mDetector.onTouchEvent(event);
 	}
 
 	public void AnimateRightJab() {
 		AnimatorSet aSet = new AnimatorSet();
 		ObjectAnimator a1, a2;
 		a1 = ObjectAnimator.ofFloat(rightGlove, "translationY", jab_distance);
 		a1.setDuration(jab_cooldown);
 
 		a2 = ObjectAnimator.ofFloat(rightGlove, "translationY", -jab_distance);
 		a2.setDuration(jab_cooldown);
 
 		aSet.playSequentially(a1, a2);
 		aSet.start();
 	}
 
 	public void AnimateLeftJab() {
 		AnimatorSet aSet = new AnimatorSet();
 		ObjectAnimator a1, a2;
 		a1 = ObjectAnimator.ofFloat(leftGlove, "translationY", jab_distance);
 		a1.setDuration(jab_cooldown);
 
 		a2 = ObjectAnimator.ofFloat(leftGlove, "translationY", -jab_distance);
 		a2.setDuration(jab_cooldown);
 
 		aSet.playSequentially(a1, a2);
 		aSet.start();
 	}
 
 	public void AnimateRightHook() {
 		AnimatorSet aSet = new AnimatorSet();
 		ObjectAnimator a1, a2;
 		a1 = ObjectAnimator.ofFloat(rightGlove, "translationX", hook_distance);
 		a1.setDuration(hook_cooldown);
 
 		a2 = ObjectAnimator.ofFloat(rightGlove, "translationX", -hook_distance);
 		a2.setDuration(hook_cooldown);
 
 		aSet.playSequentially(a1, a2);
 		aSet.start();
 	}
 
 	public void AnimateLeftHook() {
 		AnimatorSet aSet = new AnimatorSet();
 		ObjectAnimator a1, a2;
 		a1 = ObjectAnimator.ofFloat(leftGlove, "translationX", -hook_distance);
 		a1.setDuration(hook_cooldown);
 
 		a2 = ObjectAnimator.ofFloat(leftGlove, "translationX", hook_distance);
 		a2.setDuration(hook_cooldown);
 
 		aSet.playSequentially(a1, a2);
 		aSet.start();
 	}
 
 	public void AnimateRightUppercut() {
 		AnimatorSet aSet = new AnimatorSet();
 		ObjectAnimator a1, a2;
 		a1 = ObjectAnimator.ofFloat(rightGlove, "translationY",
 				uppercut_distance);
 		a1.setDuration(uppercut_cooldown);
 
 		a2 = ObjectAnimator.ofFloat(rightGlove, "translationY",
 				-uppercut_distance);
 		a2.setDuration(uppercut_cooldown);
 
 		aSet.playSequentially(a1, a2);
 		aSet.start();
 	}
 
 	public void AnimateLeftUppercut() {
 		AnimatorSet aSet = new AnimatorSet();
 		ObjectAnimator a1, a2;
 		a1 = ObjectAnimator.ofFloat(leftGlove, "translationY",
 				uppercut_distance);
 		a1.setDuration(750);
 
 		a2 = ObjectAnimator.ofFloat(leftGlove, "translationY",
 				-uppercut_distance);
 		a2.setDuration(750);
 
 		aSet.playSequentially(a1, a2);
 		aSet.start();
 	}
 
 	private class PlayerGestureDetector extends
 			GestureDetector.SimpleOnGestureListener {
 		private static final String PGL = "PGL";
 
 		/* Constants for gesture regions */
 		private final int CENTER_X = ArenaActivity.this.getResources()
 				.getDisplayMetrics().widthPixels / 2;
 
 		/* Constants for gesture swipe restrictions */
 		private static final int SWIPE_MIN_VELOCITY = 100;
 		private static final int SWIPE_MAX_OFF_PATH = 100;
 
 		/*
 		 * Handles Jabs
 		 */
 		@Override
 		public boolean onSingleTapUp(MotionEvent e) {
 			float x = e.getX();
 
 			if (x >= CENTER_X) {
 				Log.d(PGL, "right jab");
 				mGame.localRightJab();
 				AnimateRightJab();
 
 			} else {
 				Log.d(PGL, "left jab");
 				mGame.localLeftJab();
 				AnimateLeftJab();
 			}
 
 			return true;
 		}
 
 		/*
 		 * Handles uppercuts, hooks, and blocks
 		 */
 		@Override
 		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
 				float velocityY) {
 
 			float e1_x = e1.getX();
 			float e2_x = e2.getX();
 			float e1_y = e1.getY();
 			float e2_y = e2.getY();
 
 			float dx = e2_x - e1_x;
 			float dy = e2_y - e1_y;
 
 			if (Math.abs(velocityX) >= SWIPE_MIN_VELOCITY
 					|| Math.abs(velocityY) >= SWIPE_MIN_VELOCITY) { // Swipe
 																	// fast
 																	// enough
 
 				if (e1_x >= CENTER_X) { // Right hits
					if (dy < SWIPE_MAX_OFF_PATH && dx < 0) { // swipe r->l
						Log.d(PGL, "right hook");
						mGame.localRightHook();
						AnimateRightHook();
 						
 					} else if (dx < SWIPE_MAX_OFF_PATH && dy > 0) {
 						Log.d(PGL, "block (right)");
 						mGame.localBlock();
 						
					} else if (dx < SWIPE_MAX_OFF_PATH && dy < 0) { // swipe up
						Log.d(PGL, "right uppercut");
						mGame.localRightUppercut();
						AnimateRightUppercut();
 						
 					} else {
 						Log.d(PGL, "right nothing");
 					}
 
 				} else { // Left hits
 
 					if (dx < SWIPE_MAX_OFF_PATH && dy < 0) { // swipe up
 						Log.d(PGL, "left uppercut");
 						mGame.localLeftUppercut();
 						AnimateLeftUppercut();
 						
 					} else if (dx < SWIPE_MAX_OFF_PATH && dy > 0) {
 						Log.d(PGL, "block (left)");
 						mGame.localBlock();
 						
 					} else if (dy < SWIPE_MAX_OFF_PATH && dx > 0) { // swipe r->l
 						Log.d(PGL, "left hook");
 						mGame.localLeftHook();
 						AnimateLeftHook();
 						
 					} else {
 						Log.d(PGL, "left nothing");
 					}
 
 				}
 			} else {
 				Log.d(PGL, "not fast enough");
 			}
 
 			return true;
 		}
 	}
 }
