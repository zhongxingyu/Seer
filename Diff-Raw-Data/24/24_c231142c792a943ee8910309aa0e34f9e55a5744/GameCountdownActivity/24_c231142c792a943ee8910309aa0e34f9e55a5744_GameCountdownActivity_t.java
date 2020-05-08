 package com.example.pokertimer;
 
 import java.util.List;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Color;
 import android.graphics.Typeface;
 import android.media.RingtoneManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.CountDownTimer;
 import android.support.v4.app.NotificationCompat;
 import android.view.GestureDetector;
 import android.view.GestureDetector.SimpleOnGestureListener;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.view.animation.AccelerateInterpolator;
 import android.view.animation.Animation;
 import android.view.animation.TranslateAnimation;
 import android.widget.TextView;
 import android.widget.ViewFlipper;
 
 
 public class GameCountdownActivity extends Activity {
 	
 	private int roundNumber = 1;
 	private Game game;
 	
 	private TextView textBlinds;
 	private TextView textAnte;
 	private TextView textNextBlinds;
 	private TextView textTime;
 	private List<Round> rounds;
 	
 	private int time = -1; // stop timer
 	
 	private Round round;
 	private Round next_round;
 	
 	private CountDownTimer countDownTimer = null;
 	
 	private boolean is_paused = true;
 	
 	View buttonPlayPause;
 	
 	
 	private Animation mInFromRight;
     private Animation mOutToLeft;
     private Animation mInFromLeft;
     private Animation mOutToRight;
     private ViewFlipper mViewFlipper;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.countdown);
 		
 		 mViewFlipper = (ViewFlipper) findViewById(R.id.view_flipper);
 	        mViewFlipper.setDisplayedChild(0);
 	        initAnimations();
 		
 		
 		this.processIntent();
 		
 		textBlinds = (TextView) findViewById(R.id.blinds);
 		textAnte = (TextView) findViewById(R.id.ante);
 		textNextBlinds = (TextView) findViewById(R.id.next_blinds);
 		textTime = (TextView) findViewById(R.id.time);
 		buttonPlayPause = findViewById(R.id.button_play_pause);;
 		
 		// Set name of tournament
         setTitle(game.getName());
 		this.setRounds();
 		refreshGameInfo();
 		
 		
 		//Changing fonts for clock
 		Typeface tf;
 		//tf = Typeface.createFromAsset(getAssets(),"fonts/Swiss 721 Bold Rounded AT.TTF");
 		tf = Typeface.createFromAsset(getAssets(),"fonts/UNVR47W.TTF");
 		//tf = Typeface.createFromAsset(getAssets(),"fonts/BRLNSR.TTF");
 		textTime.setTypeface(tf);
 		
 		ActionBar actionBar = getActionBar();
 		//actionBar.setDisplayShowHomeEnabled(false);
 		actionBar.setDisplayHomeAsUpEnabled(true);
 	}
 	
 	/*public void onConfigurationChanged(Configuration newConfig) {
 		super.onConfigurationChanged(newConfig);
 
 
 	    	setContentView(R.layout.countdown);
 
 
 	  }
 	
 	*/
 	
 	 private void initAnimations() {
 	        mInFromRight = new TranslateAnimation(Animation.RELATIVE_TO_PARENT,
 	                +1.0f, Animation.RELATIVE_TO_PARENT, 0.0f,
 	                Animation.RELATIVE_TO_PARENT, 0.0f,
 	                Animation.RELATIVE_TO_PARENT, 0.0f);
 	        mInFromRight.setDuration(500);
 	        AccelerateInterpolator accelerateInterpolator = new AccelerateInterpolator();
 	        mInFromRight.setInterpolator(accelerateInterpolator);
 
 	        mInFromLeft = new TranslateAnimation(Animation.RELATIVE_TO_PARENT,
 	                -1.0f, Animation.RELATIVE_TO_PARENT, 0.0f,
 	                Animation.RELATIVE_TO_PARENT, 0.0f,
 	                Animation.RELATIVE_TO_PARENT, 0.0f);
 	        mInFromLeft.setDuration(500);
 	        mInFromLeft.setInterpolator(accelerateInterpolator);
 
 	        mOutToRight = new TranslateAnimation(Animation.RELATIVE_TO_PARENT,
 	                0.0f, Animation.RELATIVE_TO_PARENT, +1.0f,
 	                Animation.RELATIVE_TO_PARENT, 0.0f,
 	                Animation.RELATIVE_TO_PARENT, 0.0f);
 	        mOutToRight.setDuration(500);
 	        mOutToRight.setInterpolator(accelerateInterpolator);
 
 	        mOutToLeft = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f,
 	                Animation.RELATIVE_TO_PARENT, -1.0f,
 	                Animation.RELATIVE_TO_PARENT, 0.0f,
 	                Animation.RELATIVE_TO_PARENT, 0.0f);
 	        mOutToLeft.setDuration(500);
 	        mOutToLeft.setInterpolator(accelerateInterpolator);
 
 	        final GestureDetector gestureDetector;
 	        gestureDetector = new GestureDetector(new MyGestureDetector());
 
 	        mViewFlipper.setOnTouchListener(new OnTouchListener() {
 
 	            public boolean onTouch(View v, MotionEvent event) {
 	                if (gestureDetector.onTouchEvent(event)) {
 	                    return false;
 	                } else {
 	                    return true;
 	                }
 	            }
 	        });
 	    }
 
 	    private class MyGestureDetector extends SimpleOnGestureListener {
 
 	        private static final int SWIPE_MIN_DISTANCE = 120;
 	        private static final int SWIPE_MAX_OFF_PATH = 250;
 	        private static final int SWIPE_THRESHOLD_VELOCITY = 200;
 
 	        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
 	                float velocityY) {
 	            System.out.println(" in onFling() :: ");
 	            if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
 	                return false;
 	            if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
 	                    && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
 	                mViewFlipper.setInAnimation(mInFromRight);
 	                mViewFlipper.setOutAnimation(mOutToLeft);
 	                mViewFlipper.showNext();
 	            } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
 	                    && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
 	                mViewFlipper.setInAnimation(mInFromLeft);
 	                mViewFlipper.setOutAnimation(mOutToRight);
 	                mViewFlipper.showPrevious();
 	            }
 	            return super.onFling(e1, e2, velocityX, velocityY);
 	        }
 	    }
 	
 	/**
 	 * Gets info from intent
 	 */
 	private void processIntent(){
 		Intent countdownIntent = getIntent();
 		this.game = (Game) countdownIntent.getSerializableExtra("Game");
 		this.rounds = game.getRounds();
 	}
 	private void notifyNewRound(){
		String text = new String();
		String title = new String();
		
		if(next_round != null){
			title = "Next round:";
			text = "Blinds:  " + this.next_round.toString() + ",  Duration:  " 
			        + String.format("%02d", next_round.getMinutes()) +":"
			        + String.format("%02d", next_round.getSeconds());
		}	
		else{
			title = "Game finished!";
			text = "";
		}
		
 		NotificationCompat.Builder builder =  
 		        new NotificationCompat.Builder(this)  
 		        .setSmallIcon(R.drawable.forward)  
		        .setContentTitle(title)  
		        .setContentText(text);  
 
 
 		Intent notificationIntent = new Intent(this, GameCountdownActivity.class);  
 		
 		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
 		
 		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,   
 		        PendingIntent.FLAG_UPDATE_CURRENT);  
 
 		Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
 		builder.setSound(alarmSound);
 		
 		builder.setContentIntent(contentIntent);  
 		builder.setAutoCancel(true);
 		builder.setLights(Color.BLUE, 500, 500);
 		long[] pattern = {500,500,500,500,500,500,500,500,500};
 		builder.setVibrate(pattern);
 		builder.setStyle(new NotificationCompat.InboxStyle());
 		// Add as notification  
 		NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);  
 		manager.notify(1, builder.build());  
 		
 		
 		
 	}
 	/**
 	 * Starts Countdown
 	 */
 	public void startCountDown(){
 		if(countDownTimer != null){
 			stopCountDown();
 		}
 		
 		if(time != -1){
 			countDownTimer = new CountDownTimer((this.time+1)*1000, 1000) {
 				
 				@Override
 				public void onTick(long millisUntilFinished) {
 					time = time-1;
 					if(time >= 0){
 						refreshTimeInfo();
 					}else{
 						notifyNewRound();
 						setNextRound();
 					}
 				}
 				
 				@Override
 				public void onFinish() {
 					startCountDown();
 				}
 			};
 					  
 			countDownTimer.start();
 		}
 	}
 	
 	/**
 	 * Stops countDown
 	 */
 	public void stopCountDown(){
 		if(countDownTimer != null){
 			countDownTimer.cancel();
 		}
 		
 		countDownTimer = null;
 	}
 	
 	/**
 	 * Starts next round
 	 */
 	public void setNextRound(){
 		if(roundNumber < rounds.size()){
 			roundNumber++;
 			setRounds();
 			stopCountDown();
 		}else{
 			if(countDownTimer != null){
 				time = -1;
 				setToStop();
 			}
 		}
 		
 		refreshGameInfo();
 	}
 	
 	/**
 	 * Starts previous round
 	 */
 	public void setPreviousRound(){
 		if(roundNumber - 1 > 0){
 			roundNumber--;
 			setRounds();
 			stopCountDown();
 		}
 		
 		refreshGameInfo();
 	}
 	
 	@Override
     public boolean onOptionsItemSelected(MenuItem menuItem)
     {
 		super.onBackPressed();
 		return true;
     }
 	
 	/**
 	 * Sets round and next_round
 	 */
 	private void setRounds(){
 		this.round = rounds.get(roundNumber-1);
 		
 		this.time = round.getTime();
 		
 		if(roundNumber < rounds.size()){
 			this.next_round = rounds.get(roundNumber);
 		}else{
 			this.next_round = null;		
 		}
 	}
 	
 	/**
 	 * Adds all texts to view from this round
 	 */
 	public void refreshGameInfo(){		
 		
 		if(next_round != null){
 			textNextBlinds.setText(this.next_round.toString());
 		}else{
 			textNextBlinds.setText("");
 		}
 		
 		textBlinds.setText(this.round.toShortString());
 		textAnte.setText(this.round.getAnte()+"");
 		refreshTimeInfo();
 	}
 	
 	/**
 	 * Refresh time
 	 */
 	private void refreshTimeInfo(){
 		if(this.time >= 0){
 			int minutes = this.time / 60;
 			int seconds = this.time % 60;
 			textTime.setText(twoDigitString(minutes) + ":" + twoDigitString(seconds));
 		}
 	}
 	
 	/**
 	 * Creates two digits from one digit  (e. c. 9 -> "09"; 25 -> "25")
 	 * @param number
 	 * @return
 	 */
 	private String twoDigitString(Integer number){
 		if(number < 10){
 			return "0"+number;
 		}else{
 			return number.toString();
 		}
 	}
 	
 	public void onClick(View v) {
 		if(v == buttonPlayPause) {
 			if(is_paused){
 				setToPlay();
 			} else {
 				setToStop();
 			}
 		}else if(v == findViewById(R.id.button_next)){
 			setNextRound();
 			setToStop();
 		}else if(v == findViewById(R.id.button_previous)){
 			setPreviousRound();
 			setToStop();
 		}
 	
 	}
 	
 	private void setToPlay(){
 		buttonPlayPause.setBackgroundResource(R.drawable.pause_button);
 		is_paused = false;
 		startCountDown();
 	}
 	
 	private void setToStop(){
 		buttonPlayPause.setBackgroundResource(R.drawable.play_button);
 		is_paused = true;
 		stopCountDown();
 	}
 }
