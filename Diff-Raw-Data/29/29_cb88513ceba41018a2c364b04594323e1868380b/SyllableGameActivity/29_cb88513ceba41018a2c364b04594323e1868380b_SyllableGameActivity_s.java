 package edu.voicelabs.vst;
 
 import android.content.Intent;
 import android.graphics.Typeface;
 import android.graphics.drawable.AnimationDrawable;
 import android.media.MediaPlayer;
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
 
 public class SyllableGameActivity extends AbstractGameActivity implements OnTouchListener {
 	
 	// Layout elements
 	protected RelativeLayout gameLayout;	
 
 	// Menu
 	private ImageButton buttonSkip;
 	private ImageButton buttonMenu;
 	
 	private ImageButton buttonStart;
 	
 	// Loops through the set of syllables to speak
 	private final String[] syllables = {"LA", "LI", "LU", "LE", "LO"};
 	private final int[] syllableSounds = {R.raw.syllable_la, R.raw.syllable_li, R.raw.syllable_lu, R.raw.syllable_le, R.raw.syllable_lo};
 	private int syllableIndex = 0;
 	
 	private boolean started = false;
 	
 	//Touch circle helper
 	private ImageButton leoHelper;
 	private boolean leoPressed = false;
 	
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		subPattern = "L";
 		maxCorrectMatches = 1;
 		maxAttempts = 2;
 		
 		setContentView(R.layout.syllable_game);
 		
 		// Menu
 		this.buttonSkip = (ImageButton) findViewById(R.id.buttonSkip);
 		this.buttonMenu = (ImageButton) findViewById(R.id.buttonMenu);
 		
 		this.buttonSkip.setOnTouchListener(this);
 		this.buttonMenu.setOnTouchListener(this);
 		
 		//UI
 		this.message = (TextView) findViewById(R.id.txt_label_syllable);
 		this.message.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Mabel.ttf"));
 		
 		this.prompt = (ImageView) findViewById(R.id.imageViewPrompt);
 		
 		// Click on leo to start the game
 		this.buttonStart = (ImageButton) findViewById(R.id.buttonStartWord);	
 		this.buttonStart.setOnTouchListener(this);		
 
 		setState(InteractionState.IDLE);
 		
 		// Touch helper circle
 		this.leoHelper = (ImageButton) findViewById(R.id.leo_helper);
 		this.leoHelper.setOnTouchListener(this);
 		AnimationHelper.runKeyframeAnimation(this, R.id.leo_helper, R.anim.anim_btn_red_circle5);
 	}
 	
 
 	protected Mode getMode() {
 		return Mode.SYLLABLE;
 	}
 	
 	protected ViewGroup getGameLayout() {
 		return (ViewGroup) findViewById(R.id.game_layout_syllable);
 	}
 
 	protected void fullSuccess() {
 		// Move to the next syllable, or complete the game
 		this.syllableIndex++;
 		if (this.syllableIndex >= this.syllables.length) {
 			this.playingRef = R.raw.feedback_pos_really_cool;
 			this.message.setText("Finished!");
 			setState(InteractionState.PLAY);
 			
 			// Last game, so go to the victory screen after 3 sec delay
 			Handler handler = new Handler(); 
 		    handler.postDelayed(new Runnable() { 
 		         public void run() { 
 		        	 runGameCompletion("Syllable");
 		         } 
 		    }, 2000); 
 		}
 		else {
 			setState(InteractionState.IDLE);
 			this.message.setText("Say " + this.syllables[this.syllableIndex]);
 			MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.feedback_pos_super);
 			mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
 	            @Override
 	            public void onCompletion(MediaPlayer mp) {
 	    			playingRef = syllableSounds[syllableIndex];
 	    			setState(InteractionState.PLAY_THEN_RERUN);
 	            }
 			});
 			mediaPlayer.start();
 		}
 	}
 	
 	protected void partSuccess() {
 		// Encourage the same syllable		
 		this.playingRef = R.raw.feedback_partial_try_one_more;
 		this.message.setText("Good, do it again!");
 		setState(InteractionState.PLAY_THEN_RECORD);
 	}
 	
 	protected void fullAttempts() {
 		this.playingRef = syllableSounds[this.syllableIndex];
 		this.message.setText("Try it again!");
 		setState(InteractionState.PLAY_THEN_RERUN);
 //		wipeRecognizer();
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
 				Intent intent = new Intent(getApplicationContext(), WordGameActivity.class);
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
 					
 				}
 				
 				
 				//Change text to first syllable
 //				this.syllableIndex = 0;
 				this.message.setText(this.syllables[this.syllableIndex]);
 				this.playingRef = syllableSounds[this.syllableIndex];
 				if (this.started) {
 					setState(InteractionState.PLAY_THEN_RECORD);
 				}
 				else {		
 					this.started = true;
 					setState(InteractionState.PLAY_THEN_RERUN);
 					
 					// Play animation manually 
 					buttonStart.setBackgroundResource(R.anim.anim_leo_hand_to_ear);
 					AnimationDrawable leoAnimation = (AnimationDrawable) buttonStart.getBackground();
 					leoAnimation.start();
 				}
 			}
 		}
 	return false;
 	}
 }
 
