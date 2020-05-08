 package vn.hust.zoo.ui;
 
 import java.util.ArrayList;
 
 import vn.hust.zoo.R;
 import vn.hust.zoo.logic.GameLogic;
import vn.hust.zoo.logic.Score;
 import android.app.Activity;
 import android.app.Fragment;
 import android.app.FragmentManager;
 import android.content.Intent;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 import android.view.GestureDetector;
 import android.view.GestureDetector.SimpleOnGestureListener;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 public class GameActivity extends Activity  implements FragmentManager.OnBackStackChangedListener {
 	public static boolean touchable = true;
 	
 	public static int RIGHT2LEFT = 0;
 	public static int LEFT2RIGHT = 1;
 	
 	public int direction = LEFT2RIGHT;
 
     private boolean mShowingBack = false;
 	public static Typeface t;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_card_flip);
 		if(null == t) t = Typeface.createFromAsset(getAssets(), "fonts/mcfont.ttf");
 
 		if (savedInstanceState == null) {
 			CardFrontFragment c = new CardFrontFragment();
 			c.set(this);
 			getFragmentManager().beginTransaction().add(R.id.container, c).commit();
 		} else {
 			mShowingBack = (getFragmentManager().getBackStackEntryCount() > 0);
 		}
 
 		getFragmentManager().addOnBackStackChangedListener(this);
 		
 		// setup gameLogic properties
 		Intent i = getIntent();
 		int level = i.getIntExtra("level", 0);
 		GameLogic.clear();
 		GameLogic.initLevel(this, level);
 		GameLogic.initCharacter();
 	}
 	
 	public void onDeleteAnswer(View v){
 		GameLogic.deleteAnswer((Button) v);
 		((ImageView) findViewById(R.id.result_false)).setVisibility(View.INVISIBLE);
 	}
 	
 	public void onAnswer(View v){
 //		Log.d("Alphabet","" + ((Button)v).getText());
 		if(GameLogic.answer((Button)v)){
 			Log.d("Result", "True");
 			
 			GameLogic.displayAnimalNameAcc();
 			
 			TextView n = (TextView) findViewById(R.id.game_name_acc);
 			n.setTypeface(GameActivity.t);
 			n.setText(GameLogic.getNameAcc());
 			n.setVisibility(View.VISIBLE);
 			
 			((TextView) findViewById(R.id.game_hint)).setVisibility(View.INVISIBLE);
 			((LinearLayout) findViewById(R.id.answerRow1)).setVisibility(View.INVISIBLE);
 			((LinearLayout) findViewById(R.id.answerRow2)).setVisibility(View.INVISIBLE);
 			((Button) findViewById(R.id.game_level_select)).setVisibility(View.INVISIBLE);
 			
 			((ImageView) findViewById(R.id.result_true)).setVisibility(View.VISIBLE);
 			
 			GameLogic.loadStar(this);
 			((ImageView) findViewById(R.id.game_star)).setVisibility(View.VISIBLE);
 			((ImageView) findViewById(R.id.game_star)).setBackgroundResource(GameLogic.getStar());
 
 			((LinearLayout) findViewById(R.id.swipe_answer)).setOnTouchListener(null);
 			
 			((Button) findViewById(R.id.replay)).setVisibility(View.VISIBLE);
 			((Button) findViewById(R.id.next)).setVisibility(View.VISIBLE);
 			((Button) findViewById(R.id.menu)).setVisibility(View.VISIBLE);
			
			int score = Score.getScore(GameLogic.getLevel());
			if(score < 3)Score.setScore(GameLogic.getLevel(), score+1);
			
 		}else{
 			Log.d("Result", "False");
 			if(GameLogic.isAnswerFullOfChar()) ((ImageView) findViewById(R.id.result_false)).setVisibility(View.VISIBLE);
 		}
 	}
 	
 	public void onReplay(View v){
 		if(!GameActivity.touchable) return;
 		else GameActivity.touchable = false;
 		
 		if(v.getId() == R.id.replay) v.setBackgroundResource(R.drawable.button_replay_pressed);
 		Handler h = new Handler();
 		h.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				Intent i = new Intent(GameActivity.this, GameActivity.class);
 				if(GameLogic.getLevel() == 26)  i.putExtra("level", 0);
 				else							i.putExtra("level", GameLogic.getLevel());
 				
 				startActivity(i);
 				
 				GameActivity.touchable = true;
 				finish();
 			}
 		},MainActivity.BUTTON_DELAY);
 	}
 	
 	public void onNext(View v){
 		if(!GameActivity.touchable) return;
 		else GameActivity.touchable = false;
 		
 		if(v.getId() == R.id.next) v.setBackgroundResource(R.drawable.button_next_pressed);
 		Handler h = new Handler();
 		h.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				Intent i = new Intent(GameActivity.this, GameActivity.class);
 				if(GameLogic.getLevel() == 26)  i.putExtra("level", 0);
 				else							i.putExtra("level", GameLogic.getLevel() + 1);
 				
 				startActivity(i);
 				
 				GameActivity.touchable = true;
 				finish();
 			}
 		},MainActivity.BUTTON_DELAY);
 	}
 	
 	public void onMenuSelect(View v){
 		if(!GameActivity.touchable) return;
 		else GameActivity.touchable = false;
 		
 		if(v.getId() == R.id.menu) v.setBackgroundResource(R.drawable.button_level_select_completed_pressed);
 		if(v.getId() == R.id.game_question || v.getId() == R.id.game_level_select) v.setBackgroundResource(R.drawable.button_level_select_pressed);
 		Handler h = new Handler();
 		h.postDelayed(new Runnable() {
 			@Override
 			public void run() {				
 				GameActivity.touchable = true;
 				finish();
 			}
 		},MainActivity.BUTTON_DELAY);
 	}
 	
 	public void onSwitch(View v){
 		switch(v.getId()){
 		case R.id.swipe1:
 		case R.id.swipe2:
 			flipCard(); 
 			break;
 		}
 	}
 	
 	public void flipCard() {
 		Object fragment;
 
 		mShowingBack = !mShowingBack; // track
 		
 		// Create card side
 		if(mShowingBack){
 			fragment = new CardBackFragment();
 			((CardBackFragment) fragment).set(this);
 		}else{
 			fragment = new CardFrontFragment();
 			((CardFrontFragment) fragment).set(this);
 		}
 
 		// Create animation flip card
 		if (direction == LEFT2RIGHT) {
 			getFragmentManager()
 					.beginTransaction()
 					.setCustomAnimations(R.animator.card_flip_left_in,
 							R.animator.card_flip_left_out,
 							0,
 							0) 
 					.replace(R.id.container, (Fragment) fragment).commit();
 		} else if (direction == RIGHT2LEFT) {
 			getFragmentManager()
 					.beginTransaction()
 					.setCustomAnimations(R.animator.card_flip_right_in,
 							R.animator.card_flip_right_out,
 							0,
 							0) 
 					.replace(R.id.container, (Fragment) fragment).commit();
 		}
     }
 
     @Override
     public void onBackStackChanged() {
         mShowingBack = (getFragmentManager().getBackStackEntryCount() > 0);
     }
 
     public static class SwingGestureDetection extends SimpleOnGestureListener{
     	private static int SWIPE_MIN_DISTANCE = 70;
     	private static int SWIPE_THRESHOLD_VELOCITY = 10;
     	
     	private Button i;
     	private GameActivity mActivity;
     	
     	public SwingGestureDetection(GameActivity mActivity, Button i){
     		this.i = i;
     		this.mActivity = mActivity;
     	}
     	
 		@Override
 		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,float velocityY) {
 			if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
 					&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
 				mActivity.direction = GameActivity.RIGHT2LEFT;
 				Log.d("Swing","Right2Left");
 				i.performClick();
 			} else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
 					&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
 				mActivity.direction = GameActivity.LEFT2RIGHT;
 				Log.d("Swing","Left2Right");
 				i.performClick();
 			}
 			return true;
 		}
     }
     
     public static class CardFrontFragment extends Fragment {
     	private GameActivity mGameActivity;
 		private GestureDetector gestureDetector;
 
         public CardFrontFragment() {
         }
 
         public void set(GameActivity mGameActivity) {
         	this.mGameActivity = mGameActivity;
         }
         
         @Override
         public View onCreateView(LayoutInflater inflater, ViewGroup container,
                 Bundle savedInstanceState) {
             return inflater.inflate(R.layout.game_question, container, false);
         }
 
         @Override
 		public void onActivityCreated(Bundle savedInstanceState) {
 			super.onActivityCreated(savedInstanceState);
 			
 			// setup image
 			((ImageView) mGameActivity.findViewById(R.id.game_image)).setBackgroundResource(GameLogic.getImageID());
 						
 			
 			// Gesture
 			LinearLayout l1 = (LinearLayout) mGameActivity.findViewById(R.id.swipe_question);
 			Button i = (Button) mGameActivity.findViewById(R.id.swipe1);
 			gestureDetector = new GestureDetector(new SwingGestureDetection(mGameActivity, i));
 			l1.setOnTouchListener(new OnTouchListener() {
 		         @Override
 		            public boolean onTouch(View v, MotionEvent event) {
 		        	 	gestureDetector.onTouchEvent(event);
 		                return true;
 		         }
 		     });
         	
         }
     }
 
     public static class CardBackFragment extends Fragment {
     	private GameActivity mGameActivity;
 		private GestureDetector gestureDetector;
 
     	public CardBackFragment(){
     	}
     	
         public void set(GameActivity mGameActivity) {
         	this.mGameActivity = mGameActivity;
         }
 
         @Override
         public View onCreateView(LayoutInflater inflater, ViewGroup container,
                 Bundle savedInstanceState) {
             return inflater.inflate(R.layout.game_answer, container, false);
         }
         
         @Override
 		public void onActivityCreated(Bundle savedInstanceState) {
 			super.onActivityCreated(savedInstanceState);
 			
 			// answer screen setup
 			
 			// prepare logic indicator
 
 			// view
 			LinearLayout l = (LinearLayout) mGameActivity.findViewById(R.id.correct);
 			
 			// font
 			
 			// correct non-white-char answer
 			for(int i = 0; i < GameLogic.getAnswer().size(); i++){
 				String button = "correct" + i;
 				int buttonID = getResources().getIdentifier(button, "id", mGameActivity.getPackageName());
 				Button b = (Button) mGameActivity.findViewById(buttonID);
 				if(b == null) Log.d("Error", "Game Activity");
 				//  b.setText(GameLogic.getAnswer().get(i));
 				try{
 				b.setText(" ");
 
 				b.setBackgroundResource(R.drawable.button_character_empty);
 				b.setTypeface(t, Typeface.BOLD);
 				
 				if(!GameLogic.getAnswer().get(i).equals(" "))GameLogic.addPlayerAnswer(b);
 				else										 b.setVisibility(View.INVISIBLE);
 				}catch(Exception e){
 				}
 			}
 
 			// redundant char
 			for(int i = GameLogic.getAnswer().size(); i < 6; i++){
 				String button = "correct" + i;
 				int buttonID = getResources().getIdentifier(button, "id", mGameActivity.getPackageName());
 				Button b = (Button) mGameActivity.findViewById(buttonID);
 				if(b.getText().equals("")) l.removeView(b);
 			}
 
 			// answer char 
 			for(int i = 0; i < 8; i++){
 				String button = "answer" + i;
 				int buttonID = getResources().getIdentifier(button, "id", mGameActivity.getPackageName());
 				Button b = (Button) mGameActivity.findViewById(buttonID);
 				b.setText(GameLogic.getAll().get(i));
 				
 				b.setBackgroundResource(R.drawable.button_character_empty);
 				b.setTypeface(GameActivity.t, Typeface.BOLD);
 			}
 			
 			// Hint
 			TextView t = (TextView) mGameActivity.findViewById(R.id.game_hint);
 			t.setText("" + GameLogic.getHint());
 			t.setTypeface(GameActivity.t, Typeface.ITALIC);
 			
 			// restore choice char
 			ArrayList<Button> bu = new ArrayList<Button>();
 			for(Button b : GameLogic.getIndicator().getCharAnswer()){
 				Button b2 = (Button) mGameActivity.findViewById(b.getId());
 				b2.setVisibility(View.INVISIBLE);
 				bu.add(b2);
 			}
 			GameLogic.getIndicator().getCharAnswer().clear();
 			for(Button b2 : bu)
 				GameLogic.getIndicator().getCharAnswer().push(b2);
 			
 			// restore player answer char
 			ArrayList<Button> buffer = new ArrayList<Button>();
 			for (Button b : GameLogic.getIndicator().getPlayerAnswer()) {
 				Button b2 = (Button)mGameActivity.findViewById(b.getId());
 				buffer.add(b2);
 				b2.setText(b.getText());
 			}
 			
 			GameLogic.getIndicator().getPlayerAnswer().clear();
 			for (Button b2 : buffer)
 				GameLogic.getIndicator().addPlayerAnswerButton(b2);
 			
 			
 			// Gesture
 			gestureDetector = new GestureDetector(new SwingGestureDetection(mGameActivity, (Button) mGameActivity.findViewById(R.id.swipe2)));
 			((LinearLayout) mGameActivity.findViewById(R.id.swipe_answer)).setOnTouchListener(new OnTouchListener() {
 				@Override
 				public boolean onTouch(View v, MotionEvent event) {
 					gestureDetector.onTouchEvent(event);
 					return true;
 				}
 			});
 		}
     }
 }
