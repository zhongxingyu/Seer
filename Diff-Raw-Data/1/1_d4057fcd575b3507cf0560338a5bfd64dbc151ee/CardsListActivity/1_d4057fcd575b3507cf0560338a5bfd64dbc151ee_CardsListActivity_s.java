 package se.flashcards;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.provider.MediaStore;
 import android.support.v4.view.ViewPager;
 import android.util.Log;
 import android.widget.ArrayAdapter;
 import android.widget.Gallery;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 
 public class CardsListActivity extends SherlockActivity {
 	private static final int MAKE_NEW_CARD = 1;
 	private static final int EDIT_CARD = 2;
 	
 	private CardPagerAdapter cardAdapter;
 	private List<Card> cardList;
 	private ViewPager viewPager;
 	private CardContentView answerView;
 	private BitmapDownsampler downSampler;
 	private WrappingSlidingDrawer drawer;
 	private String name;
 	private InfoSaver infoSaver;
 	private int currentPosition;
 	private LoadCardsTask loadCards;
 	private boolean hasLoaded;
 	private MenuItem deleteCardMenuItem;
 	private MenuItem editCardMenuItem;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);   
         
         setContentView(R.layout.cards_list);
         setTheme(R.style.Theme_Sherlock);
         
         hasLoaded = false;
  
         ActionBar actionBar = getSupportActionBar();
         actionBar.setDisplayHomeAsUpEnabled(true);
         
         Intent intent = getIntent();
         name = intent.getStringExtra(FlashcardsActivity.CARD_LIST_NAME);
         actionBar.setTitle(name);
         
         downSampler = new BitmapDownsampler(this, 600, 1000); //600, 1000
         
         answerView = (CardContentView)findViewById(R.id.content);
         drawer = (WrappingSlidingDrawer)findViewById(R.id.drawer);
         drawer.lock();
         
         infoSaver = InfoSaver.getInfoSaver(this);
 
     	cardList = new ArrayList<Card>();
         cardAdapter = new CardPagerAdapter(this, cardList);
     	loadCards = new LoadCardsTask(this, name, downSampler) {
     		private boolean answerImageSet = false;
     		
     		@Override
     		protected void onProgressUpdate (Card... values) {
     			 cardList.add(values[0]);
     			 cardAdapter.notifyDataSetChanged();
     			 
     			 if (!answerImageSet) {
     				 answerView.setCardContent(values[0].getAnswer());
     				 answerImageSet = true;
     			 }
     			 drawer.unlock();
     		}
     	};
 
         viewPager = (ViewPager)findViewById(R.id.viewpager);
         viewPager.setAdapter(cardAdapter);    
         
         viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
 			@Override
 			public void onPageSelected(int position) {
 				drawer.close();
 				answerView.setCardContent(cardList.get(position).getAnswer());
 				currentPosition = position;
 			}
         });
         
 //        if (savedInstanceState != null) {
 //	        int currentCard = savedInstanceState.getInt("current_card", 0);
 //	        viewPager.setCurrentItem(currentCard);
 //	        boolean drawerOpen = savedInstanceState.getBoolean("drawer_open", false);
 //	        if (drawerOpen) {
 //	        	drawer.open();
 //	        } else {
 //	        	drawer.close();
 //	        }
 //        }
     }
     
 	@Override
 	public void onSaveInstanceState (Bundle outState) {
 //		outState.putInt("current_card", currentPosition);
 //		outState.putBoolean("drawer_open", drawer.isOpened());
 	}
     
     @Override
     public void onResume() {
     	super.onResume();
     	if (!hasLoaded) {
     		loadCards.execute();
     		hasLoaded = true;
     	}
     }
     
     private void removeCard(int pos) {   		
     	cardList.remove(pos);
     	cardAdapter.notifyDataSetChanged(); 	
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getSupportMenuInflater();
         inflater.inflate(R.layout.cardslist_actionbar_menu, menu);
         
         deleteCardMenuItem = menu.findItem(R.id.menu_delete_card);
         editCardMenuItem = menu.findItem(R.id.menu_edit_card);
         
         //TEMP
 		deleteCardMenuItem.setVisible(true);
 		editCardMenuItem.setVisible(true);
         
         return true;
     }
     
 	@Override
     public boolean onOptionsItemSelected(MenuItem item) {
     	switch(item.getItemId()) {
     		case android.R.id.home:
     	    	Intent intent = new Intent(this, FlashcardsActivity.class);
     	    	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
     	    	intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
     	    	startActivity(intent);
     	    	finish(); //??
     		break;
     		case R.id.menu_make_new:
     			Intent makeNew = new Intent(this, NewCardActivity.class);
     			startActivityForResult(makeNew, MAKE_NEW_CARD);
     		break;
     		case R.id.menu_edit_card:
     			Intent edit = new Intent(this, NewCardActivity.class);
     			edit.putExtra("question_content", cardList.get(currentPosition).getQuestion());
     			edit.putExtra("answer_content", cardList.get(currentPosition).getAnswer());
     			startActivityForResult(edit, EDIT_CARD);
     		break;
     		case R.id.menu_delete_card:
     			removeCard(currentPosition); 
     		break;
     	}
     	return true;
     }
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent intent) { 
 	    super.onActivityResult(requestCode, resultCode, intent);
 		if (resultCode == RESULT_OK) {
 		    switch (requestCode) {
 		    	case MAKE_NEW_CARD: {
 		    		CardContent question = intent.getParcelableExtra(NewCardActivity.QUESTION_EXTRA);
 		    		CardContent answer = intent.getParcelableExtra(NewCardActivity.ANSWER_EXTRA);
 					try {
 						question.reloadBitmap(downSampler);
 			    		answer.reloadBitmap(downSampler);
 					} catch (IOException e) {
 						Log.v("flashcards", "Couldn't load image.");
 					}
 					
 					if (!hasLoaded) {
 						loadCards.addLastLater(new Card(question, answer));
 					} else {
 			   			cardList.add(new Card(question, answer));
 		    			cardAdapter.notifyDataSetChanged();
 		    			drawer.unlock();
 					}
 		    	}
 		    	break;
 		    	case EDIT_CARD: {
 		    		CardContent question = intent.getParcelableExtra(NewCardActivity.QUESTION_EXTRA);
 		    		CardContent answer = intent.getParcelableExtra(NewCardActivity.ANSWER_EXTRA);
 					try {
 						question.reloadBitmap(downSampler);
 			    		answer.reloadBitmap(downSampler);
 					} catch (IOException e) {
 						Log.v("flashcards", "Couldn't load image.");
 					}
 
 					cardList.remove(currentPosition);
 					cardList.add(currentPosition, new Card(question, answer));
 					cardAdapter.notifyDataSetChanged();
 		    	}
 		    	break;
 		    }
 		}
 	}
 	
 	@Override
 	protected void onPause() {
 		super.onPause();
 		infoSaver.saveCards(name, cardList);
 	}
 }
