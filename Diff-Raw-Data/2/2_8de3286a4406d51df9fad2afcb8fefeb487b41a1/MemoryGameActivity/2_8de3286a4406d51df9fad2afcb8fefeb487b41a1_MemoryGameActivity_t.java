 package ca.team615.memorygameandroid;
 
 import java.util.Random;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.os.Handler;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class MemoryGameActivity extends Activity implements OnClickListener {
 
 	private int[] viewIds = {R.id.card_0, R.id.card_1, R.id.card_2, R.id.card_3,
 			R.id.card_4, R.id.card_5, R.id.card_6, R.id.card_7,
 			R.id.card_8, R.id.card_9, R.id.card_10, R.id.card_11,
 			R.id.card_12, R.id.card_13, R.id.card_14, R.id.card_15,};
 
 	private int[] drawableIds = {R.drawable.card_0, R.drawable.card_1, R.drawable.card_2, R.drawable.card_3,
 			R.drawable.card_4, R.drawable.card_5, R.drawable.card_6, R.drawable.card_7, R.drawable.card_back};
 
 	private int[] assignments;	//Holds the assigned positions of the cards
 
 	private ImageView[] imageviews;
 	
 	private static final int SOUND_FLIP = 1;
 	private static final int SOUND_FLOP = 2;
 	static final int SOUND_BACKGROUND = 3;
 
 	/** the number of cards currently face up */
 	private int flippedCards;
 
 	/** the index of the card turned up most recently */
 	private int currentIndex = -1;
 	/** the index of the card turned up previously */
 	private int lastIndex = -1;
 
 	private int foundPairs = 0;
 	private TextView foundPairsLabel;
 	
 	private int turns_taken=0;
 	private TextView turns_taken_label;
 
 	Handler handler;
 
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.game_screen_layout);
 		
 		SoundManager.addSound(SOUND_FLIP, R.raw.flip);
 		SoundManager.addSound(SOUND_FLOP, R.raw.flop);
 
 		handler = new Handler();
 
 		foundPairs = 0;
 		foundPairsLabel=(TextView)MemoryGameActivity.this.findViewById(R.id.pairs_counter);
 		turns_taken_label=(TextView)MemoryGameActivity.this.findViewById(R.id.turns);
 
 		//create a new array to hold the card positions
 		assignments = new int[16];
 
 		//set the card at each position to -1 (unset)
 		for(int i = 0; i < 16; i++){
 			assignments[i] = -1;
 		}
 
 		imageviews = new ImageView[viewIds.length];
 		for(int i = 0; i < viewIds.length; i++){
 			imageviews[i] = (ImageView)findViewById(viewIds[i]);
 		}
 
 		Random random = new Random();
 
 		//for each card, (we have 8) loop through.
 		for(int i = 0; i < 8; i++){
 			//each card goes in 2 slots
 			for (int j = 0; j < 2; j++){
 				//generate a random slot
 				int randomSlot = random.nextInt(16);
 				//make sure that the slot isn't already populated
 				while(assignments[randomSlot] != -1){
 					randomSlot = random.nextInt(16);
 				}
 				//set this card to that slot
 				assignments[randomSlot] = i;
 				System.out.println("Putting " + i + " in slot " + randomSlot);
 			}
 
 		}
 
 		//set click listeners for each veiw
 		for(int i = 0; i < 16; i++){
 			((ImageView)findViewById(viewIds[i])).setOnClickListener(this);
 		}
 		//set each image to blank
 		for(int i = 0; i < 16; i++){
 			((ImageView)findViewById(viewIds[i])).setImageResource(R.drawable.card_back);
 		}
 		
 
 		SoundManager.playLoopedSound(SOUND_BACKGROUND);
 		
 		//soundManager.playSound_Delayed(SOUND_BACKGROUND, 1000);
 
 	}
 
 	@Override
 	public void onClick(View v) {
 		int index = Integer.parseInt((String)v.getTag());
 		System.out.println("index is " + index);
 		
 		SoundManager.playSound(SOUND_FLIP);
 
 		for(int i =0; i < 16; i++)	{
 			//determine which id we're dealing with
 			if(v.getId() == viewIds[i]){
 				//set the face up image for each
 				index = i;
 				((ImageView)findViewById(viewIds[i])).setImageResource(drawableIds[assignments[i]]);
 				imageviews[i].setFocusable(false);
 				imageviews[i].setClickable(false);
 				break;
 			}
 		}	
 		flippedCards++;
 		
 		
 
 		if(flippedCards == 2){
 			turns_taken++;
 			turns_taken_label.setText(String.valueOf(turns_taken));
 			
 			currentIndex = index;
 			
 			for(ImageView view:imageviews){
 				view.setFocusable(false);
 				view.setClickable(false);
 			}
 
 			flippedCards = 0;
 			
 			handler.postDelayed(flipCardsBack, 1000); 
 
 		}else{
 			lastIndex = index;
 		}
 
 	}
 	
 	Runnable flipCardsBack = new Runnable() { 
 		public void run() { 
 			SoundManager.playSound(SOUND_FLOP);
 			if(assignments[currentIndex] == assignments[lastIndex]){
 				((ImageView)findViewById(viewIds[lastIndex])).setVisibility(View.INVISIBLE);
 				((ImageView)findViewById(viewIds[currentIndex])).setVisibility(View.INVISIBLE);
 				foundPairs++;
 				foundPairsLabel.setText(String.valueOf(foundPairs));	//Update label of matches
 			}else{
 				((ImageView)findViewById(viewIds[currentIndex])).setImageResource(R.drawable.card_back);
 				((ImageView)findViewById(viewIds[lastIndex])).setImageResource(R.drawable.card_back);
 				
 			}
 
 			for(ImageView view: imageviews){
 				view.setFocusable(true);
 				view.setClickable(true);
 			}
 		} 
 	};
 
 
 	@Override
 	protected void onPause() {
 		// TODO Auto-generated method stub
		SoundManager.pauseLoopedSound();
 		super.onPause();
 	}
 
 
 	
 }
 
