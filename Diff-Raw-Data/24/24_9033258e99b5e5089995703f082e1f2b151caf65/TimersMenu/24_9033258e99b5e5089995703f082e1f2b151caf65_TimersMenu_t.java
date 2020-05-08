 /**
  * Package of menus
  */
 package com.japtar.chessclock.menus;
 
 import android.content.Context;
 import android.media.AudioManager;
 import android.media.Ringtone;
 import android.media.RingtoneManager;
 import android.os.Handler;
 import android.view.HapticFeedbackConstants;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.view.animation.Animation.AnimationListener;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.TextView;
 
 import com.japtar.chessclock.Global;
 import com.japtar.chessclock.MainActivity;
 import com.japtar.chessclock.R;
 import com.japtar.chessclock.enums.TimerCondition;
 import com.japtar.chessclock.gui.OutlinedTextView;
 
 /**
  * Menu for Timer
  * @author japtar10101
  */
 public class TimersMenu implements MenuInterface,
 		View.OnClickListener, View.OnTouchListener {
 	/* ===========================================================
 	 * Members
 	 * =========================================================== */
 	/** The main activities class */
 	private final MainActivity mParentActivity;
 
 	// == Timer ==
 	/** The handler running {@link mTask} */
 	private final Handler mTimer;
 	/** Task that decrements the timer */
 	private final DecrementTimerTask mTask;
 	
 	// == Sub-Menus ==
 	/** Helper class taking care of the Start Menu functionality */
 	private final SubMenu mStartMenu;
 	/** Helper class taking care of the Pause Menu functionality */
 	private final SubMenu mPauseMenu;
 	/** Helper class taking care of the Times-Up Menu functionality */
 	private final SubMenu mTimesUpMenu;
 	
 	// == Buttons ==
 	/** Left player's button */
 	private ImageButton mLeftButton = null;
 	/** Right player's button */
 	private ImageButton mRightButton = null;
 	/** Pause button */
 	private Button mPauseButton = null;
 		
 	// == Labels ==
 	/** Label indicating delay */
 	private TextView mDelayLabel = null;
 	/** Left player's label */
 	private OutlinedTextView mLeftLabel = null;
 	/** Right player's label */
 	private OutlinedTextView mRightLabel = null;
 	
 	// == Animations ==
 	/** Shows the delay label */
 	private Animation mShowAnimation = null;
 	/** Hides the delay label */
 	private Animation mHideAnimation = null;
 	private final AnimationListener mShowDelayLabel;
 	private final AnimationListener mHideDelayLabel;
 	
 	// == Misc. ==
 	/** Sound of alarm */
 	private Ringtone mRingtone = null;
 	
 	/* ===========================================================
 	 * Constructors
 	 * =========================================================== */
 	/**
 	 * @param parent the menu's parent activity
 	 */
 	public TimersMenu(final MainActivity parent) {
 		// Setup activity
 		mParentActivity = parent;
 		
 		// Setup the timer-related stuff
 		mTimer = new Handler();
 		mTask = new DecrementTimerTask(this, mTimer);
 		
 		// Setup the pause menu
 		mPauseMenu = new PauseSubMenu(mParentActivity, this);
 		mStartMenu = new StartSubMenu(mParentActivity, this);
 		mTimesUpMenu = new TimesUpSubMenu(mParentActivity, this);
 		
 		// Setup animation listeners
 		mShowDelayLabel = new AnimationListener() {
 			@Override
 			public void onAnimationStart(Animation animation) {
 				if(mDelayLabel != null) {
 					mDelayLabel.setVisibility(View.VISIBLE);
 				}
 			}
 			@Override
 			public void onAnimationEnd(Animation animation) { }
 			@Override
 			public void onAnimationRepeat(Animation animation) { }
 		};
 		mHideDelayLabel = new AnimationListener() {
 			@Override
 			public void onAnimationEnd(Animation animation) {
 				if(mDelayLabel != null) {
 					mDelayLabel.setVisibility(View.INVISIBLE);
 				}
 			}
 			@Override
 			public void onAnimationStart(Animation animation) { }
 			@Override
 			public void onAnimationRepeat(Animation animation) { }
 		};
 	}
 
 	/* ===========================================================
 	 * Overrides
 	 * =========================================================== */
 	/**
 	 * Updates the player's turns, based on the button clicked
 	 * @see android.view.View.OnClickListener#onClick(android.view.View)
 	 */
 	@Override
 	public void onClick(final View v) {
 		// Stop the handler
 		mTimer.removeCallbacks(mTask);
 		
 		if(v != null) {
 			// Check the sub-menus first.
 			// If any are shown, have them update the layouts
 			if(mStartMenu.isMenuShown()) {
 				mStartMenu.onClick(v);
 			} else if(mPauseMenu.isMenuShown()) {
 				mPauseMenu.onClick(v);
 			} else if(mTimesUpMenu.isMenuShown()) {
 				mTimesUpMenu.onClick(v);
 			}
 			
 			// Check if the pause button was click
 			else if(v.equals(mPauseButton)) {
 				// We're pausing the game
 				this.paused();
 			} else {
 				// Check which game button was pressed
 				final boolean leftPlayersTurn = v.equals(mRightButton);
 				if(leftPlayersTurn || v.equals(mLeftButton)) {
 					// Update the player's turn
 					this.updatePlayersTurn(leftPlayersTurn);
 					
 					// Resume the game
 					this.resume();
 				}
 			}
 		}
 	}
 	
 	/**
 	 * @param arg0
 	 * @param arg1
 	 */
 	@Override
 	public boolean onTouch(final View v, final MotionEvent event) {
 		// Make sure if the vibration is enabled or not
 		if(Global.OPTIONS.enableVibrate && (v != null) &&
 				(event.getAction() == MotionEvent.ACTION_DOWN)) {
 			// Play haptic feedback on mouse down
 			v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
 		}
 		return false;
 	}
 	
 	/**
 	 * Resumes the timer if paused, or starts it over.
 	 */
 	public void setupMenu() {
 		// First, setup the UI
 		mParentActivity.setContentView(R.layout.main);
 		
 		// Stop the handler
 		mTimer.removeCallbacks(mTask);
 
 		// == Load up all the member variables ==
 		this.setupMemberVariables();
 		
 		// == Setup the member variables ==
 		
 		// Update the haptic feedback
 		mLeftButton.setHapticFeedbackEnabled(Global.OPTIONS.enableVibrate);
 		mRightButton.setHapticFeedbackEnabled(Global.OPTIONS.enableVibrate);
 		
 		// Update the text size on everything
 		mLeftLabel.setTextSize(MainActivity.msTextSize);
 		mRightLabel.setTextSize(MainActivity.msTextSize);
 		mPauseButton.setTextSize(MainActivity.msTextSize * 0.5f);
 		mDelayLabel.setTextSize(MainActivity.msTextSize * 0.7f);
 		
 		// Update the sub menu
 		mStartMenu.setupMenu();
 		mPauseMenu.setupMenu();
 		mTimesUpMenu.setupMenu();
 		
 		// Update the animations
 		mShowAnimation.setAnimationListener(mShowDelayLabel);
 		mHideAnimation.setAnimationListener(mHideDelayLabel);
 		
 		// == Determine the game state to jump to ==
 		
 		// Determine the condition to begin this game at
 		switch(Global.GAME_STATE.timerCondition) {
 			case TimerCondition.TIMES_UP:
 			case TimerCondition.STARTING:
 				// Run the startup function
 				this.startup();
 				break;
 			default:
 				// Figure out which color to set each player
 				if(Global.GAME_STATE.leftIsWhite) {
					mLeftButton.setImageResource(R.drawable.left_white_button);
					mRightButton.setImageResource(R.drawable.right_black_button);
 				} else {
					mLeftButton.setImageResource(R.drawable.left_black_button);
					mRightButton.setImageResource(R.drawable.right_white_button);
 				}
 				
 				// Run the pause function
 				this.paused();
 				break;
 		}
 	}
 
 	/* ===========================================================
 	 * Public Methods
 	 * =========================================================== */
 	/**
 	 * Pauses the timer, unless the time is up.
 	 */
 	public void exitMenu() {
 		// Stop the time handler
 		mTimer.removeCallbacks(mTask);
 		if(mRingtone != null) {
 			mRingtone.stop();
 		}
 		
 		// Set the option's state
 		switch(Global.GAME_STATE.timerCondition) {
 			case TimerCondition.TIMES_UP:
 			case TimerCondition.STARTING:
 				Global.GAME_STATE.timerCondition = TimerCondition.STARTING;
 				break;
 			default:
 				Global.GAME_STATE.timerCondition = TimerCondition.PAUSE;
 				break;
 		}
 	}
 	
 	/* ===========================================================
 	 * Package-space Methods
 	 * =========================================================== */
 	/**
 	 * Updates the text on {@link mLeftButton}, {@link mRightButton},
 	 * and {@link mDelayLabel}
 	 */
 	void updateButtonAndLabelText() {
 		// Update button texts
 		mLeftLabel.setText(Global.GAME_STATE.leftPlayerTime());
 		mRightLabel.setText(Global.GAME_STATE.rightPlayerTime());
 		
 		// Update the delay label's text or visibility
 		switch(Global.GAME_STATE.timerCondition) {
 			case TimerCondition.RUNNING:
 			case TimerCondition.PAUSE:
 				this.updateDelayLabel();
 		}
 	}
 	
 	Button getButton(final View v, final int buttonId) {
 		// Find a view based on ID
 		final View foundView = v.findViewById(buttonId);
 		return this.convertToButton(foundView);
 	}
 	
 	/**
 	 * Indicate the game just started
 	 */
 	void startup() {
 		// Stop the ringtone
 		if(mRingtone != null) {
 			mRingtone.stop();
 		}
 
 		// Set the condition to time up
 		Global.GAME_STATE.timerCondition = TimerCondition.STARTING;
 		
 		// Make both buttons display a neutral piece
		mLeftButton.setImageResource(R.drawable.left_neutral_button);
		mRightButton.setImageResource(R.drawable.right_neutral_button);
 		
 		// Reset the time
 		Global.GAME_STATE.resetTime();
 		
 		// Show the start menu
 		mStartMenu.showMenu();
 		
 		// Do whatever else is necessary
 		this.changeConditionSetup();
 		
 		// Hide the delay label
 		mDelayLabel.setVisibility(View.INVISIBLE);
 	}
 	
 	/**
 	 * Indicate the game is paused
 	 */
 	void paused() {
 		// Stop the handler
 		mTimer.removeCallbacks(mTask);
 
 		// Set the condition to time up
 		Global.GAME_STATE.timerCondition = TimerCondition.PAUSE;
 		
 		// Show the pause menu
 		mPauseMenu.showMenu();
 		
 		// Hide the delay label if at zero
 		if(Global.GAME_STATE.delayTime() == null) {
 			mDelayLabel.setVisibility(View.INVISIBLE);
 		} else {
 			mDelayLabel.setVisibility(View.VISIBLE);
 		}
 		
 		// Do whatever else is necessary
 		this.changeConditionSetup();
 	}
 	
 	/**
 	 * Indicate the time is up
 	 */
 	void timesUp() {
 		// Set the condition to time up
 		Global.GAME_STATE.timerCondition = TimerCondition.TIMES_UP;
 				
 		// Check the volume and ringer
 		if(mRingtone != null) {
 			final AudioManager audioManager = (AudioManager)
 				mParentActivity.getSystemService(Context.AUDIO_SERVICE);
 			if(audioManager.getStreamVolume(AudioManager.STREAM_ALARM) > 0) {
 				// If they're valid, play it as an alarm
 				mRingtone.setStreamType(AudioManager.STREAM_ALARM);
 				mRingtone.play();
 			}
 		}
 		
 		// Show the time-up menu
 		mTimesUpMenu.showMenu();
 		
 		// Do whatever else is necessary
 		this.changeConditionSetup();
 		
 		// Hide the delay label
 		mDelayLabel.setVisibility(View.INVISIBLE);
 	}
 	
 	/**
 	 * Updates layout based on which button is pressed
 	 * @param buttonPressed the button that was pressed
 	 */
 	void updatePlayersTurn(final boolean leftPlayersTurn) {
 		if(Global.GAME_STATE.timerCondition == TimerCondition.STARTING) {
 			// Update the color of the game buttons
 			Global.GAME_STATE.leftIsWhite = !leftPlayersTurn;
 			if(leftPlayersTurn) {
				mLeftButton.setImageResource(R.drawable.left_white_button);
				mRightButton.setImageResource(R.drawable.right_black_button);
 			} else {
				mLeftButton.setImageResource(R.drawable.left_black_button);
				mRightButton.setImageResource(R.drawable.right_white_button);
 			}
 		}
 		
 		// If clicked by right/left player button,
 		// update the current player
 		Global.GAME_STATE.leftPlayersTurn = leftPlayersTurn;
 		
 		// Reset the delay time
 		Global.GAME_STATE.resetDelay();
 		
 		// Update the Delay label
 		this.updateDelayLabel();
 		
 		// Play the click sound
 		if(Global.OPTIONS.enableClick) {
 			mParentActivity.playSound();
 		}
 	}
 	/**
 	 * Updates layout based on which button is pressed
 	 * @param buttonPressed the button that was pressed
 	 */
 	void resume() {
 		// If just starting, update the labels
 		if(Global.GAME_STATE.timerCondition == TimerCondition.STARTING) {
 			// Update to the time text
 			this.updateButtonAndLabelText();
 		}
 		Global.GAME_STATE.timerCondition = TimerCondition.RUNNING;
 		
 		// Enable only one button
 		mLeftButton.setEnabled(Global.GAME_STATE.leftPlayersTurn);
 		mRightButton.setEnabled(!Global.GAME_STATE.leftPlayersTurn);
 		
 		// Make the pause button visible
 		mPauseButton.setVisibility(View.VISIBLE);
 		
 		// Start the timer
 		mTask.reset();
 		mTimer.postDelayed(mTask, 1000);
 	}
 	
 	/* ===========================================================
 	 * Private/Protected Methods
 	 * =========================================================== */	
 	/**
 	 * Grabs all the member variables from the parent activity
 	 */
 	private void setupMemberVariables() {
 		// Grab the label
 		mDelayLabel = (TextView) mParentActivity.findViewById(R.id.labelDelay);
 		mLeftLabel = (OutlinedTextView)
 			mParentActivity.findViewById(R.id.labelLeftTime);
 		mRightLabel = (OutlinedTextView)
 			mParentActivity.findViewById(R.id.labelRightTime);
 		
 		// Grab the buttons
 		mLeftButton = this.getImageButton(R.id.buttonLeftTime);
 		mRightButton = this.getImageButton(R.id.buttonRightTime);
 		mPauseButton = this.getButton(R.id.buttonPause);
 				
 		// Get the ringtone
 		mRingtone = null;
 		if(Global.OPTIONS.alarmUri != null) {
 			mRingtone = RingtoneManager.getRingtone(mParentActivity,
 					Global.OPTIONS.alarmUri);
 		}
 		
 		// Get animations
 		mShowAnimation = AnimationUtils.loadAnimation(mParentActivity,
 				R.anim.show_delay_label);
 		mHideAnimation = AnimationUtils.loadAnimation(mParentActivity,
 				R.anim.hide_delay_label);
 	}
 	
 	private void changeConditionSetup() {
 		// Update the buttons/labels text
 		this.updateButtonAndLabelText();
 		
 		// Disable game buttons, hide pause
 		mLeftButton.setEnabled(false);
 		mRightButton.setEnabled(false);
 		mPauseButton.setVisibility(View.INVISIBLE);	
 	}
 	
 	/**
 	 * Updates the text on {@link mDelayLabel}
 	 */
 	private void updateDelayLabel() {
 		// Update the delay label's text or visibility
 		String delayText = Global.GAME_STATE.delayTime();
 		if(delayText == null) {
 			// If no text is provided, play the hide animation
 			if(mDelayLabel.getVisibility() == View.VISIBLE) {
 				mDelayLabel.startAnimation(mHideAnimation);
 			}
 			// Update the delay text
 			delayText = Global.GAME_STATE.defaultDelayLabelString();
 		} else if(mDelayLabel.getVisibility() == View.INVISIBLE) {
 			// If text IS provided, play the show-label animation
 			mDelayLabel.startAnimation(mShowAnimation);
 		}
 		
 		// Update the text
 		mDelayLabel.setText(delayText);
 	}
 	
 	private ImageButton getImageButton(final int buttonId) {
 		// Return value
 		ImageButton toReturn = null;
 		
 		// Find a view based on ID
 		final View foundView = mParentActivity.findViewById(buttonId);
 		if(foundView instanceof ImageButton) {
 			toReturn = (ImageButton) foundView;
 			toReturn.setOnClickListener(this);
 			toReturn.setOnTouchListener(this);
 		}
 		return toReturn;
 	}
 	
 	private Button getButton(final int buttonId) {
 		// Find a view based on ID
 		final View foundView = mParentActivity.findViewById(buttonId);
 		return this.convertToButton(foundView);
 	}
 
 	private Button convertToButton(final View v) {
 		// Return value
 		Button toReturn = null;
 		if(v instanceof Button) {
 			toReturn = (Button) v;
 			toReturn.setOnClickListener(this);
 			toReturn.setOnTouchListener(this);
 		}
 		return toReturn;
 	}
 }
