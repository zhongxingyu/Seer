 package se.mah.k3.cards;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.content.Intent;
 import android.graphics.Typeface;
 import android.graphics.drawable.AnimationDrawable;
 import android.media.MediaPlayer;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.view.WindowManager;
 import android.view.animation.Animation;
 import android.view.animation.Animation.AnimationListener;
 import android.view.animation.AnimationUtils;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
 
 	Controller controller;
 	Highscore hs;
 	ImageView[] iv;
 	ImageView[] selectedImg;
 	ImageView[] animView;
 	ImageView candleAnimView, branchView;
 	Animation[] placeCards;
 	Animation[] replaceCards;
 	MediaPlayer selectSound, setsound, nosetsound;
 	MediaPlayer bgMusic;
 	Dialog exitDialog, winDialog;
 	AnimationDrawable[] select_Anim;
 	AnimationDrawable timeglassAnimation, candleAnim, branchViewAnim;
 	Typeface typeFace;
 	TextView leftInDeck, setsOnTable;
 	Card currCard, compareCard1, compareCard2, compareCard3;
 	Button exitYes, exitNo, winYes, winNo;
 	private boolean[] toggle;
 	private int pressedCount;
 	private boolean set;
 	private boolean newset = true;
 	private Score scoreClass;
 	private int score = 0; // An int that saves your total score
 	private Toast toast1000, toast1500, toast2000, toast3000, toast5000,
 			toast10000;
 	private TextView highscore;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		// commit
 		// fullscreen
 		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
 		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
 				WindowManager.LayoutParams.FLAG_FULLSCREEN);
 
 		setContentView(R.layout.activity_main);
 		bgMusic = new MediaPlayer().create(getApplicationContext(),
 				R.raw.mainmusic);
 		controller = new Controller();
 		hs = new Highscore(this);
 		scoreClass = new Score();
 		iv = new ImageView[12];
 		selectedImg = new ImageView[12];
 		toggle = new boolean[12];
 		animView = new ImageView[12];
 		
 		//Background animations
 		candleAnimView = (ImageView) findViewById(R.id.candleanim);
 		candleAnimView.setBackgroundResource(R.drawable.candle_anim);
 		candleAnim = (AnimationDrawable) candleAnimView.getBackground();
 		candleAnim.start();
 		branchView = (ImageView) findViewById(R.id.branchViewAnim);
 		branchView.setBackgroundResource(R.drawable.branch_animation);
 		branchViewAnim = (AnimationDrawable) branchView.getBackground();
 		branchViewAnim.start();
 		
 		select_Anim = new AnimationDrawable[12];
 		placeCards = new Animation[12];
 		replaceCards = new Animation[3];
 		leftInDeck = (TextView) findViewById(R.id.textView1);
 		setsOnTable = (TextView) findViewById(R.id.textView2);
 		selectSound = MediaPlayer.create(getApplicationContext(),
 				R.raw.selectsound);
 		setsound = MediaPlayer.create(getApplicationContext(), R.raw.set);
 		nosetsound = MediaPlayer.create(getApplicationContext(), R.raw.noset);
 		selectSound.setVolume(0.2f, 0.2f);
 		pressedCount = 0;
 
 		typeFace = Typeface.createFromAsset(getAssets(), "fonts/black.ttf");
 
 		// Create custom toasts
 		setupToasts();
 
 		// Highscore textview
 		highscore = (TextView) findViewById(R.id.highscoreView);
 		highscore.setTypeface(typeFace);
 		highscore.setText(Integer.toString(score));
 
 		// Timeglass ImageView
 		ImageView timeglassImage = (ImageView) findViewById(R.id.timeglassView);
 		timeglassImage.setBackgroundResource(R.drawable.timeglass_animation);
 		timeglassAnimation = (AnimationDrawable) timeglassImage.getBackground();
 
 		// Create custom dialogs
 		setupCustomDialogs();
 
 		bgMusic.setLooping(true);
 		bgMusic.start();
 		bgMusic.setVolume(0.5f, 0.5f);
 		setupImageViews();
 		updateUI(controller.getActiveCards(12));
 	}
 
 	// Listener to dialog buttons
 	public OnClickListener dialogListener = new OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			switch (v.getId()) {
 			case R.id.yes:
 				Intent i = new Intent(MainActivity.this, StartScreen.class);
 				startActivity(i);
 				finish();
 				break;
 			case R.id.no:
 				exitDialog.cancel();
 				break;
 			case R.id.winYes:
 				recreate();
 				winDialog.cancel();
 				break;
 			case R.id.winNo:
 				Intent toStart = new Intent(MainActivity.this,
 						StartScreen.class);
 				startActivity(toStart);
 				finish();
 				break;
 			}
 		}
 	};
 
 	// listener to cards
 	public OnClickListener onClickListener = new OnClickListener() {
 		@Override
 		public void onClick(View v) {
 			Log.i("TagBag", "NY TRYCKNING");
 			switch (v.getId()) {
 			case R.id.card1:
 				toggleState(0);
 				break;
 			case R.id.card2:
 				toggleState(1);
 				break;
 			case R.id.card3:
 				toggleState(2);
 				break;
 			case R.id.card4:
 				toggleState(3);
 				break;
 			case R.id.card5:
 				toggleState(4);
 				break;
 			case R.id.card6:
 				toggleState(5);
 				break;
 			case R.id.card7:
 				toggleState(6);
 				break;
 			case R.id.card8:
 				toggleState(7);
 				break;
 			case R.id.card9:
 				toggleState(8);
 				break;
 			case R.id.card10:
 				toggleState(9);
 				break;
 			case R.id.card11:
 				toggleState(10);
 				break;
 			case R.id.card12:
 				toggleState(11);
 				break;
 			}
 			Log.i("TagBag", "BUG: index "+(currCard.getIndex()+1));
 			Log.i("TagBag", "BUG: tryckCount "+pressedCount);
 			if (pressedCount == 1) {
 				compareCard1 = currCard;
 			} else if (pressedCount == 2) {
 				compareCard2 = currCard;
 			} else if (pressedCount == 3) {
 				compareCard3 = currCard;
 				checkSelection();
 			}
 		}
 
 	};
 
 	// update cards(imageViews) and textViews
 	public void updateUI(ArrayList<Card> activeCards) {
 		for (int i = 0; i < iv.length; i++) {
 			iv[i].setImageResource(activeCards.get(i).getResId());
 		}
 		if (newset == true) {
 			for (int i = 0; i < iv.length; i++) {
 				iv[i].startAnimation(placeCards[i]);
 			}
 			newset = false;
 		} else if (newset == false) {
 			// Byta bild animation.
 			iv[compareCard1.getIndex()].startAnimation(replaceCards[0]);
 			iv[compareCard2.getIndex()].startAnimation(replaceCards[1]);
 			iv[compareCard3.getIndex()].startAnimation(replaceCards[2]);
 			replaceCards[2].setAnimationListener(new AnimationListener() {
 
 				@Override
 				public void onAnimationStart(Animation animation) {
 				}
 
 				@Override
 				public void onAnimationRepeat(Animation animation) {
 				}
 
 				@Override
 				public void onAnimationEnd(Animation animation) {
 					iv[compareCard1.getIndex()].clearAnimation();
 					iv[compareCard2.getIndex()].clearAnimation();
 					iv[compareCard3.getIndex()].clearAnimation();
 				}
 			});
 		}
 		leftInDeck.setText("Left in deck: " + controller.getNbrOfCardsLeft());
 		setsOnTable.setText("Set on table: " + controller.getNbrOfSets());
 	}
 
 	// cards works as togglebuttons
 	public void toggleState(int pos) {
 		toggle[pos] = !toggle[pos];
 		if (toggle[pos] == true) {
 			selectedImg[pos].setVisibility(View.VISIBLE);
 			select_Anim[pos].stop();
 			select_Anim[pos].start();
 			currCard = controller.getActiveArray().get(pos);
 			selectSound.seekTo(0);
 			selectSound.start();
 			pressedCount++;
 		} else if (toggle[pos] == false) {
 			selectedImg[pos].setVisibility(View.INVISIBLE);
 			currCard = controller.getActiveArray().get(0);
 			pressedCount--;
 		}
 	}
 
 	// resets frames and selection logic
 	public void resetSelect() {
 		for (int i = 0; i < toggle.length; i++) {
 			toggle[i] = false;
 			selectedImg[i].setVisibility(View.INVISIBLE);
 			pressedCount = 0;
 		}
 	}
 
 	// runs when three cards has been selected
 	public void checkSelection() {
 		Log.i("TagBag", "BUG: " + compareCard1.toString());
 		Log.i("TagBag", "BUG: " + compareCard2.toString());
 		Log.i("TagBag", "BUG: " + compareCard3.toString());
 		set = controller.isSet(compareCard1, compareCard2, compareCard3);
 		if (set == true) {
 
 			scoreClass.killOldTimer();
 			scoreClass.add1000Points();
 			scoreClass.startComboTimer();
 			setsound.seekTo(0);
 			setsound.start();
 			// Add the score you get to the total score
 			score = score + scoreClass.getPoints();
 
 			// Show custom toast based on how much points you get from your set
 			if (scoreClass.getPoints() == 1000) {
 				toast1000.show();
 			}
 			if (scoreClass.getPoints() == 1500) {
 				toast1500.show();
 			}
 			if (scoreClass.getPoints() == 2000) {
 				toast2000.show();
 			}
 			if (scoreClass.getPoints() == 3000) {
 				toast3000.show();
 			}
 			if (scoreClass.getPoints() == 5000) {
 				toast5000.show();
 			}
 			if (scoreClass.getPoints() == 10000) {
 				toast10000.show();
 			}
 
 			// Start timeglass animation, and if it is running; restart it
 			if (timeglassAnimation.isRunning()) {
 				timeglassAnimation.stop();
 			}
 			timeglassAnimation.start();
 
 			highscore.setText(Integer.toString(score));
 			scoreClass.clearAll();
 
 			if (!controller.getDeckArray().isEmpty()) {
 
 				updateUI(controller.getNewCards(compareCard1.getIndex(),
 						compareCard2.getIndex(), compareCard3.getIndex()));
 
 			} else if (controller.getDeckArray().isEmpty()) {
 				win();
 			}
 			set = false;
 		} else if (set == false) {
 
 			Toast.makeText(MainActivity.this, "No SET", Toast.LENGTH_SHORT)
 					.show();
 			nosetsound.seekTo(0);
 			nosetsound.start();
 		}
 		resetSelect();
 	}
 
 	// run when there's not card left in deck
 	public void win() {
 		if (score > hs.getScore(9)) {
 			// if you got a new highscore
 			Intent highScoreIntent = new Intent(getApplicationContext(),
 					WriteHighScore.class);
 			highScoreIntent.putExtra("score", score);
 			startActivity(highScoreIntent);
 			finish();
 		} else {
 			// popup
 			winDialog.show();
 		}
 	}
 
 	// hardware back-button pressed
 	public void onBackPressed() {
 		exitDialog.show();
 		return;
 	}
 
 	// Create custom toasts
 	public void setupToasts() {
 		LayoutInflater inflater = getLayoutInflater();
 		View layout1 = inflater.inflate(R.layout.toast_layout,
 				(ViewGroup) findViewById(R.id.toast_layout_root));
 		View layout2 = inflater.inflate(R.layout.toast_layout_1500,
 				(ViewGroup) findViewById(R.id.toast_layout_1500_root));
 		View layout3 = inflater.inflate(R.layout.toast_layout_2000,
 				(ViewGroup) findViewById(R.id.toast_layout_2000_root));
 		View layout4 = inflater.inflate(R.layout.toast_layout_3000,
 				(ViewGroup) findViewById(R.id.toast_layout_3000_root));
 		View layout5 = inflater.inflate(R.layout.toast_layout_5000,
 				(ViewGroup) findViewById(R.id.toast_layout_5000_root));
 		View layout6 = inflater.inflate(R.layout.toast_layout_10000,
 				(ViewGroup) findViewById(R.id.toast_layout_10000_root));
 		toast1000 = new Toast(getApplicationContext());
 		toast1000.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
 		toast1000.setDuration(Toast.LENGTH_SHORT);
 		toast1000.setView(layout1);
 
 		toast1500 = new Toast(getApplicationContext());
 		toast1500.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
 		toast1500.setDuration(Toast.LENGTH_SHORT);
 		toast1500.setView(layout2);
 
 		toast2000 = new Toast(getApplicationContext());
 		toast2000.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
 		toast2000.setDuration(Toast.LENGTH_SHORT);
 		toast2000.setView(layout3);
 
 		toast3000 = new Toast(getApplicationContext());
 		toast3000.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
 		toast3000.setDuration(Toast.LENGTH_SHORT);
 		toast3000.setView(layout4);
 
 		toast5000 = new Toast(getApplicationContext());
 		toast5000.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
 		toast5000.setDuration(Toast.LENGTH_SHORT);
 		toast5000.setView(layout5);
 
 		toast10000 = new Toast(getApplicationContext());
 		toast10000.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
 		toast10000.setDuration(Toast.LENGTH_SHORT);
 		toast10000.setView(layout6);
 	}
 
 	// sets up imageViews(Cards), frames and animations
 	public void setupImageViews() {
 		iv[0] = (ImageView) findViewById(R.id.card1);
 		iv[1] = (ImageView) findViewById(R.id.card2);
 		iv[2] = (ImageView) findViewById(R.id.card3);
 		iv[3] = (ImageView) findViewById(R.id.card4);
 		iv[4] = (ImageView) findViewById(R.id.card5);
 		iv[5] = (ImageView) findViewById(R.id.card6);
 		iv[6] = (ImageView) findViewById(R.id.card7);
 		iv[7] = (ImageView) findViewById(R.id.card8);
 		iv[8] = (ImageView) findViewById(R.id.card9);
 		iv[9] = (ImageView) findViewById(R.id.card10);
 		iv[10] = (ImageView) findViewById(R.id.card11);
 		iv[11] = (ImageView) findViewById(R.id.card12);
 
 		animView[0] = (ImageView) findViewById(R.id.cardAnim1);
 		animView[1] = (ImageView) findViewById(R.id.cardAnim2);
 		animView[2] = (ImageView) findViewById(R.id.cardAnim3);
 		animView[3] = (ImageView) findViewById(R.id.cardAnim4);
 		animView[4] = (ImageView) findViewById(R.id.cardAnim5);
 		animView[5] = (ImageView) findViewById(R.id.cardAnim6);
 		animView[6] = (ImageView) findViewById(R.id.cardAnim7);
 		animView[7] = (ImageView) findViewById(R.id.cardAnim8);
 		animView[8] = (ImageView) findViewById(R.id.cardAnim9);
 		animView[9] = (ImageView) findViewById(R.id.cardAnim10);
 		animView[10] = (ImageView) findViewById(R.id.cardAnim11);
 		animView[11] = (ImageView) findViewById(R.id.cardAnim12);
 
 		placeCards[0] = AnimationUtils.loadAnimation(this,
 				R.anim.placecard_anim1);
 		placeCards[1] = AnimationUtils.loadAnimation(this,
 				R.anim.placecard_anim2);
 		placeCards[2] = AnimationUtils.loadAnimation(this,
 				R.anim.placecard_anim3);
 		placeCards[3] = AnimationUtils.loadAnimation(this,
 				R.anim.placecard_anim4);
 		placeCards[4] = AnimationUtils.loadAnimation(this,
 				R.anim.placecard_anim5);
 		placeCards[5] = AnimationUtils.loadAnimation(this,
 				R.anim.placecard_anim6);
 		placeCards[6] = AnimationUtils.loadAnimation(this,
 				R.anim.placecard_anim7);
 		placeCards[7] = AnimationUtils.loadAnimation(this,
 				R.anim.placecard_anim8);
 		placeCards[8] = AnimationUtils.loadAnimation(this,
 				R.anim.placecard_anim9);
 		placeCards[9] = AnimationUtils.loadAnimation(this,
 				R.anim.placecard_anim10);
 		placeCards[10] = AnimationUtils.loadAnimation(this,
 				R.anim.placecard_anim11);
 		placeCards[11] = AnimationUtils.loadAnimation(this,
 				R.anim.placecard_anim12);
 
 		replaceCards[0] = AnimationUtils.loadAnimation(this,
 				R.anim.replacecard_anim1);
 		replaceCards[1] = AnimationUtils.loadAnimation(this,
 				R.anim.replacecard_anim2);
 		replaceCards[2] = AnimationUtils.loadAnimation(this,
 				R.anim.replacecard_anim3);
 
 		for (int i = 0; i < animView.length; i++) {
 			animView[i].setBackgroundResource(R.drawable.select_anim);
 			select_Anim[i] = (AnimationDrawable) animView[i].getBackground();
 		}
 
 		for (int i = 0; i < iv.length; i++) {
 			iv[i].setOnClickListener(onClickListener);
 		}
 
 		selectedImg[0] = (ImageView) findViewById(R.id.frame1);
 		selectedImg[1] = (ImageView) findViewById(R.id.frame2);
 		selectedImg[2] = (ImageView) findViewById(R.id.frame3);
 		selectedImg[3] = (ImageView) findViewById(R.id.frame4);
 		selectedImg[4] = (ImageView) findViewById(R.id.frame5);
 		selectedImg[5] = (ImageView) findViewById(R.id.frame6);
 		selectedImg[6] = (ImageView) findViewById(R.id.frame7);
 		selectedImg[7] = (ImageView) findViewById(R.id.frame8);
 		selectedImg[8] = (ImageView) findViewById(R.id.frame9);
 		selectedImg[9] = (ImageView) findViewById(R.id.frame10);
 		selectedImg[10] = (ImageView) findViewById(R.id.frame11);
 		selectedImg[11] = (ImageView) findViewById(R.id.frame12);
 	}
 
 	public void setupCustomDialogs() {
 		exitDialog = new Dialog(MainActivity.this);
 		exitDialog = controller.createCustomDialog(exitDialog,
 				R.layout.exit_dialog);
 		exitYes = (Button) exitDialog.findViewById(R.id.yes);
 		exitYes.setOnClickListener(dialogListener);
 		exitNo = (Button) exitDialog.findViewById(R.id.no);
 		exitNo.setOnClickListener(dialogListener);
 
 		winDialog = new Dialog(MainActivity.this);
 		winDialog = controller.createCustomDialog(winDialog,
 				R.layout.win_dialog);
 		winYes = (Button) winDialog.findViewById(R.id.winYes);
 		winYes.setOnClickListener(dialogListener);
 		winNo = (Button) winDialog.findViewById(R.id.winNo);
 		winNo.setOnClickListener(dialogListener);
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		bgMusic.start();
 	}
 
 	@Override
 	protected void onDestroy() {
 		// kills timers and media
 		scoreClass.killOldTimer();
 		super.onDestroy();
 		bgMusic.release();
 		selectSound.release();
 	}
 
 	@Override
 	protected void onPause() {
 		// kills timers and media
 		scoreClass.killOldTimer();
 		bgMusic.pause();
 		candleAnim.stop();
 		super.onPause();
 	}
 }
