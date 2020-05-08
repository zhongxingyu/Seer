 package edu.voicelabs.vst;
 
 import java.io.File;
 
 import android.content.Intent;
 import android.graphics.Typeface;
 import android.graphics.drawable.AnimationDrawable;
 import android.media.MediaPlayer;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.view.ViewGroup;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import edu.voicelabs.vst.RecognizerTask.Mode;
 
 
 public class PhonemeGameActivity extends AbstractGameActivity implements OnTouchListener {
 	
 	// Layout elements
 	protected RelativeLayout gameLayout;		
 
 	// Menu
 	private ImageButton buttonSkip;
 	private ImageButton buttonMenu;
 	private ImageButton buttonStart;
 	
 	//helper red circle
 	private ImageButton leoHelper;
 	private boolean leoPressed = false;
 	
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		// Basic inherited fields to set
 		this.subPattern = "L";
 		this.maxCorrectMatches = 2;
 		this.maxAttempts = 6;
 		
 		setContentView(R.layout.phoneme_game);		
 		
 		// Menu
 		this.buttonSkip = (ImageButton) findViewById(R.id.buttonSkip);
 		this.buttonMenu = (ImageButton) findViewById(R.id.buttonMenu);
 		
 		this.buttonSkip.setOnTouchListener(this);
 		this.buttonMenu.setOnTouchListener(this);
 		
 		//UI
 		this.message = (TextView) findViewById(R.id.txt_game1_l);
 		this.message.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Mabel.ttf"));
 		
 		this.prompt = (ImageView) findViewById(R.id.imageViewPrompt);
 		
 		// Click on leo to start the game
 		this.buttonStart = (ImageButton) findViewById(R.id.buttonStartWord);	
 		this.buttonStart.setOnTouchListener(this);
 		
 		//touch helper red circle
 		this.leoHelper = (ImageButton) findViewById(R.id.leo_helper);
 		this.leoHelper.setOnTouchListener(this);
 		AnimationHelper.runKeyframeAnimation(this, R.id.leo_helper, R.anim.anim_btn_red_circle5);	
 
 		setState(InteractionState.IDLE);
 	}
 
 	protected Mode getMode() {
 		return Mode.PHONEME;
 	}
 	
 	protected ViewGroup getGameLayout() {
 		return (ViewGroup) findViewById(R.id.game_layout_phoneme);
 	}
 
 	protected void fullSuccess() {		
 		this.playingRef = R.raw.feedback_pos_really_cool;
 		this.message.setText("You got it!");
 		setState(InteractionState.PLAY);
 
 		Handler handler = new Handler(); 
 	    handler.postDelayed(new Runnable() { 
 	         public void run() { 
 	        	 runGameCompletion("Phoneme");
 	         } 
 	    }, 3000); 
 	}
 	
 	protected void partSuccess() {		
 		this.playingRef = R.raw.feedback_partial_try_one_more;
 		this.message.setText("Great! Say it again.");
 		setState(InteractionState.PLAY_THEN_RECORD);
 	}
 	
 	protected void fullAttempts() {
 		this.playingRef = R.raw.feedback_neg_have_another_go;
 		this.message.setText("Keep trying...");
 //		setState(InteractionState.PLAY_THEN_RECORD);
 		setState(InteractionState.PLAY_THEN_RERUN);
 	}
 	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		
		MediaPlayer leoInstructions = MediaPlayer.create(getApplicationContext(), R.raw.leo_now_your_turn);
		leoInstructions.start();
	}
 	
 
 	@Override
 	public boolean onTouch(View v, MotionEvent event) {
 		
 		if (event.getAction() == MotionEvent.ACTION_UP) {
 			if (v == this.buttonSkip) {
 				// Skip to the games
 				Intent intent = new Intent(getApplicationContext(), SyllableGameActivity.class);
 	            startActivity(intent); 
 			}
 			else if (v == this.buttonMenu) {
 				// Skip to the Menu
 				Intent intent = new Intent(getApplicationContext(), LessonProgressActivity.class);
 	            startActivity(intent); 
 			}
 			else if ((v == this.buttonStart) || (v == this.leoHelper)) {
 				//First run
 				if (leoPressed == false){
 					this.leoHelper.setVisibility(View.INVISIBLE);			
 					leoPressed = true;
 					
 					// Play animation manually 
 					buttonStart.setBackgroundResource(R.anim.anim_leo_hand_to_ear);
 					AnimationDrawable leoAnimation = (AnimationDrawable) buttonStart.getBackground();
 					leoAnimation.start();
 					
 				}
 				
 				
 				this.playingRef = R.raw.phoneme_lll;
 				setState(InteractionState.PLAY_THEN_RECORD);
 				
 
 			}
 		
 		}
 	return false;
 	}
 	
 }
 
