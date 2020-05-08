 package com.fyodorwolf.studybudy;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.animation.AccelerateInterpolator;
 import android.widget.Button;
 import android.widget.RelativeLayout;
 
 public class DeckActivity extends Activity {
 
 	private static final String TAG = "ListActivity";
 	private DatabaseAdapter myDB;
 	
 	private RelativeLayout cardFront;
 	private RelativeLayout cardBack;
 	private boolean isFirstImage = true;
 	boolean animating = false;
 	
     
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState){
 
 		setContentView(R.layout.card_view);
 		cardFront = (RelativeLayout)findViewById(R.id.card_front);
 		cardBack  = (RelativeLayout)findViewById(R.id.card_back);
		Log.d(TAG,cardBack.toString());
 	    getActionBar().setDisplayHomeAsUpEnabled(true);
 
 		long deckId =  getIntent().getExtras().getLong("com.example.studyBudy.deckId");
 		Log.d(TAG,"SectionID: "+String.valueOf(deckId));
         setTitle("DeckName");
 		
         DeckGetter getCards = new DeckGetter();
         getCards.execute(DatabaseAdapter.cardsWithDeckIdQuery(deckId));
 
     	myDB = DatabaseAdapter.getInstance(this);
         super.onCreate(savedInstanceState);
 	}
 
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case android.R.id.home:
                 // This is called when the Home (Up) button is pressed
                 // in the Action Bar.
                 Intent parentActivityIntent = new Intent(this, MainActivity.class);
                 parentActivityIntent.addFlags(
                         Intent.FLAG_ACTIVITY_NO_ANIMATION|
                         Intent.FLAG_ACTIVITY_CLEAR_TOP |
                         Intent.FLAG_ACTIVITY_NEW_TASK);
                 startActivity(parentActivityIntent);
                 overridePendingTransition(0,0);
                 finish();
                 return true;
         }
         return super.onOptionsItemSelected(item);
     }
     
     public void rotationComplete(){
     	this.animating = false;
     }
 
 
 	private void applyRotation(float start, float end) {
 		// Find the center of image
 		final float centerX = cardFront.getWidth() / 2.0f;
 		final float centerY = cardFront.getHeight() / 2.0f;
 		
 		// Create a new 3D rotation with the supplied parameter
 		// The animation listener is used to trigger the next animation
 		final Flip3dAnimation rotation = new Flip3dAnimation(start, end, centerX, centerY);
 		rotation.setDuration(300);
 		rotation.setFillAfter(true);
 		rotation.setInterpolator(new AccelerateInterpolator());
 		rotation.setAnimationListener(new DisplayNextView(isFirstImage, cardFront, cardBack, this));
 		if (isFirstImage){
 			cardFront.startAnimation(rotation);
 		} else {
 			cardBack.startAnimation(rotation);
 		}
 	
 	}
     
 /********************************************************************************************************************************************
  * 							Private Classes		 																							*
  ********************************************************************************************************************************************/
 	private class DeckGetter extends AsyncTask<String,Integer,Cursor>{
 
 		@Override
 		protected Cursor doInBackground(String... params) {
 			Log.d(TAG,"gettingDeck: "+params[0]);
 			return myDB.getCursor(params[0]);
 		}
 
 		@Override
 		protected void onPostExecute(final Cursor result) {
 
 			OnClickListener onClick = new OnClickListener() {
 				
 				@Override
 				public void onClick(View view) {
 					if(!animating){
 						animating = true;
 						if (isFirstImage) {      
 							applyRotation(0, 90);
 						} else { 
 							applyRotation(0, -90);
 						}
 						isFirstImage = !isFirstImage;
 					}
 				}
 			};
 			Log.d(TAG,cardFront.toString());
 			Button flipBack  = (Button)cardFront.findViewById(R.id.flip_to_back);
 			Button flipFront = (Button)cardBack.findViewById(R.id.flip_to_front);
 			
 			flipBack.setOnClickListener(onClick); 
 			flipFront.setOnClickListener(onClick);  
 		}
 		
 	}
 	
 	
 	
 }
