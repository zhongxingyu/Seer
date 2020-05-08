 package se.mah.k3.cards;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.content.Intent;
 import android.graphics.drawable.AnimationDrawable;
 import android.media.MediaPlayer;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.view.WindowManager;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
 
 	Controller controller;
 	ImageView[] iv;
 	ImageView[] selectedImg;
 	ImageView[] animView;
 	Animation[] placeCards;
 	MediaPlayer selectSound;
 	MediaPlayer bgMusic;
 	Dialog exitDialog, winDialog;
 	AnimationDrawable[] select_Anim;
 	TextView leftInDeck, setsOnTable;
 	Card currCard, compareCard1, compareCard2, compareCard3;
 	private boolean[] toggle;
 	private int pressedCount;
 	private int index, compareCard1Index, compareCard2Index, compareCard3Index;
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
 
 		// fullscreen
 		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
 		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
 				WindowManager.LayoutParams.FLAG_FULLSCREEN);
 
 		setContentView(R.layout.activity_main);
 
 		// Create custom toasts
 		setupToasts();
 
 		// Highscore textview
 		highscore = (TextView) findViewById(R.id.highscoreView);
 		highscore.setText("Highscore" + Integer.toString(score));
 
 		// Create custom dialogs
 		exitDialog = new Dialog(MainActivity.this);
 		exitDialog = controller.createCustomDialog(exitDialog,
 				R.layout.exit_dialog);
 		winDialog = new Dialog(MainActivity.this);
 		winDialog = controller.createCustomDialog(winDialog,
 				R.layout.win_dialog);
 
		controller = new Controller();
		scoreClass = new Score();
		iv = new ImageView[12];
		selectedImg = new ImageView[12];
		toggle = new boolean[12];
		animView = new ImageView[12];
		select_Anim = new AnimationDrawable[12];
		placeCards = new Animation[12];
		leftInDeck = (TextView) findViewById(R.id.textView1);
		setsOnTable = (TextView) findViewById(R.id.textView2);
		selectSound = MediaPlayer.create(getApplicationContext(),
				R.raw.playbutton);
		pressedCount = 0;

 		setupImageViews();
 		updateUI(controller.getActiveCards(12));
 	}
 
 	public OnClickListener onClickListener = new OnClickListener() {
 		@Override
 		public void onClick(View v) {
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
 			if (pressedCount == 1) {
 				compareCard1 = currCard;
 				compareCard1Index = index;
 			} else if (pressedCount == 2) {
 				compareCard2 = currCard;
 				compareCard2Index = index;
 			} else if (pressedCount == 3) {
 				compareCard3 = currCard;
 				compareCard3Index = index;
 				checkSelection();
 			}
 		}
 
 	};
 
 	public void updateUI(ArrayList<Card> activeCards) {
 		for (int i = 0; i < iv.length; i++) {
 			iv[i].setImageResource(activeCards.get(i).getResId());
 			if (newset == true) {
 				iv[i].startAnimation(placeCards[i]);
 			}
 		}
 		if (newset == true) {
 			newset = false;
 		}
 		leftInDeck.setText("Left in deck: " + controller.getNbrOfCardsLeft());
 		setsOnTable.setText("Set on table: " + controller.getNbrOfSets());
 	}
 
 	public void toggleState(int pos) {
 		toggle[pos] = !toggle[pos];
 		if (toggle[pos] == true) {
 			selectedImg[pos].setVisibility(View.VISIBLE);
 			select_Anim[pos].stop();
 			select_Anim[pos].start();
 			currCard = controller.getActiveArray().get(pos);
 			index = pos;
 			selectSound.seekTo(0);
 			selectSound.start();
 			pressedCount++;
 		} else if (toggle[pos] == false) {
 			selectedImg[pos].setVisibility(View.INVISIBLE);
 			pressedCount--;
 		}
 	}
 
 	public void resetSelect() {
 		for (int i = 0; i < toggle.length; i++) {
 			toggle[i] = false;
 			selectedImg[i].setVisibility(View.INVISIBLE);
 			pressedCount = 0;
 		}
 	}
 
 	public void checkSelection() {
 		set = controller.isSet(compareCard1, compareCard2, compareCard3);
 		if (set == true) {
 
 			scoreClass.killOldTimer();
 			scoreClass.add1000Points();
 			scoreClass.startComboTimer();
 
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
 
 			highscore.setText("Highscore: " + Integer.toString(score));
 			scoreClass.clearAll();
 
 			// Toast.makeText(MainActivity.this, "SET",
 			// Toast.LENGTH_SHORT).show();
 			if (controller.getDeckArray().size() > 3) {
 				updateUI(controller.getNewCards(compareCard1Index,
 						compareCard2Index, compareCard3Index));
 			} else if (controller.getDeckArray().size() == 3) {
 				updateUI(controller.getLastCards(compareCard1Index,
 						compareCard2Index, compareCard3Index));
 				Toast.makeText(MainActivity.this, "WIN", Toast.LENGTH_SHORT)
 						.show();
 				win();
 			}
 			set = false;
 		} else if (set == false) {
 			Toast.makeText(MainActivity.this, "No SET", Toast.LENGTH_SHORT)
 					.show();
 		}
 		resetSelect();
 	}
 
 	public void win() {
 		// placholder
 		int minHighScore = 0;
 		if (score > minHighScore) {
 			// till skriv highscore
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
 
 	public void onBackPressed() {
 		exitDialog.show();
 		return;
 	}
 
 	public void setupToasts() {
 		// Create custom toasts
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
 
 	@Override
 	protected void onDestroy() {
 		scoreClass.killOldTimer();
 		super.onDestroy();
 	}
 
 	@Override
 	protected void onPause() {
 		scoreClass.killOldTimer();
 		super.onPause();
 	}
 }
