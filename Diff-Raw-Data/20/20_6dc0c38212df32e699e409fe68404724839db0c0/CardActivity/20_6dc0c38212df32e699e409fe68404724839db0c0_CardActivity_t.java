 package suggestcorp.suggestible;
 
 import java.io.InputStream;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import suggestcorp.suggestible.GetServerResponse.Suggestion;
 import uk.co.jasonfry.android.tools.ui.SwipeView.OnPageChangedListener;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Typeface;
 import android.graphics.drawable.Drawable;
 import android.location.Location;
 import android.location.LocationManager;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 import android.widget.RelativeLayout.LayoutParams;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class CardActivity extends Activity {
 
 	LocationManager locManager;
 	Location location;
 	TextView txt;
 	RelativeLayout card1;
 	uk.co.jasonfry.android.tools.ui.SwipeView swiper;
 	OnDemandCardmaker cardmaker;
 	final int MAX_PAGES = 100;
 	int remainingPages = MAX_PAGES;
 	ImageButton[] filterButtons;
 	LinearLayout filters;
 	List<Suggestion> movies;
 	List<Suggestion> books;
 	List<Suggestion> restaurants;
 	List<Suggestion> outings;
 //	List<Drawable> imagesToLoad;
 //	List<Suggestion> cardsNeedingImages;
 	int fetchedCount = 0;
 	
 	public enum CardType {
 		outing, book, movie,restaurant 
 	}
 	
 	public ArrayList<String> suggestionType = new ArrayList<String>();
 		
 	
 	private class MovieFetcher extends AsyncTask<Void, Void, List<Suggestion>> {
 		@Override
 		protected List<Suggestion> doInBackground(Void... params) {
 			return GetServerResponse.getMovies();
 		}
 		@Override
 		protected void onPostExecute(List<Suggestion> suggestions) {
 			movies = suggestions;
 			fetchedCount++;
 			if (fetchedCount >3) {
 				doneFetching();
 			}
 		}
 	}
 	
 	private class BookFetcher extends AsyncTask<Void, Void, List<Suggestion>> {
 		@Override
 		protected List<Suggestion> doInBackground(Void... params) {
 			return GetServerResponse.getBooks();
 		}
 		@Override
 		protected void onPostExecute(List<Suggestion> suggestions) {
 			books = suggestions;
 			fetchedCount++;
 			if (fetchedCount >3) {
 				doneFetching();
 			}
 			//Log.d("Suggestible", "first cardType: " + cardType.values()[1]);
 		}
 	}
 	
 	private class RestaurantFetcher extends AsyncTask<Double, Void, List<Suggestion>> {
 		@Override
 		protected List<Suggestion> doInBackground(Double... location) {
 			return GetServerResponse.getRestaurants(location[0], location[1]);
 		}
 		@Override
 		protected void onPostExecute(List<Suggestion> suggestions) {
 			restaurants = suggestions;
 			fetchedCount++;
 			if (fetchedCount >3) {
 				doneFetching();
 			}
 		}
 	}
 	
 	private class OutingFetcher extends AsyncTask<Double, Void, List<Suggestion>> {
 		@Override
 		protected List<Suggestion> doInBackground(Double... location) {
 			return GetServerResponse.getOutings(location[0], location[1]);
 		}
 		@Override
 		protected void onPostExecute(List<Suggestion> suggestions) {
 			outings = suggestions;
 			fetchedCount++;
 			if (fetchedCount >3) {
 				doneFetching();
 			}
 			//Log.d("Suggestible", outings.get(0).title);
 		}
 	}
 	
 	private void doneFetching() {
 		cardmaker.onPageChanged(-1, 0);
 		for (int i = 0; i < 4; i++) {
         	filterButtons[i].setOnClickListener(new OnClickListener() {
 
 				public void onClick(View v) {
 					if (v.getTag().equals("on")) {
 						if (updateFilters() > 1) {
 							v.setTag("off");
 							filterCards();
 						} else {
 							Toast.makeText(CardActivity.this, "Leave at least one type selected!", Toast.LENGTH_SHORT).show();
 						}
 						
 					} else {
 						v.setTag("on");
 						filterCards();
 					}
 					
 					updateFilters();
 				}
         	});
         }
 		
 		ImageView next = (ImageView) findViewById(R.id.next);
 		ImageView back = (ImageView) findViewById(R.id.back);
 		
 		next.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				int page = swiper.getCurrentPage();
 				swiper.scrollToPage(page+1);
 		        ((ImageView) v).setAlpha(100);
 			}
 		});
 		
 		back.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				int page = swiper.getCurrentPage();
 				swiper.scrollToPage(page-1);
 				((ImageView) v).setAlpha(100);
 			}
 		});
 		back.setOnTouchListener(new OnTouchListener() {
 			@Override
 			public boolean onTouch(View v, MotionEvent e) {
 				if (e.getAction() == MotionEvent.ACTION_DOWN) {
 					((ImageView) v).setAlpha(255);
 				}
 				return false;
 			}
 
 		});
 		next.setOnTouchListener(new OnTouchListener() {
 			@Override
 			public boolean onTouch(View v, MotionEvent e) {
 				if (e.getAction() == MotionEvent.ACTION_DOWN) {
 					((ImageView) v).setAlpha(255);
 				}
 				return false;
 			}
 
 		});
 	}
 	
 	private class DrawableFetcher extends AsyncTask<Suggestion, Void, Drawable> {
 		
 		//int cardnum;
 		Suggestion suggestion;
 		String imageurl;
 		@Override
 		protected Drawable doInBackground(Suggestion... suggestions) {
 			
 			suggestion = suggestions[0];
 			try {
				imageurl = suggestion.imageurl;
				Log.d("Suggestible", "Loading image at '" + imageurl + "'");
			
 	            InputStream is = (InputStream) new URL(imageurl).getContent();
 	            Drawable d = Drawable.createFromStream(is, "fetched image");
 	            return d;
 	        } catch (Exception e) {
 	        	Log.e("Suggestible", "Could not load image at " + suggestion.imageurl);
 	        	e.printStackTrace();
 	            return null;
 	        }
 		}
 		
 		@Override
 		protected void onPostExecute(Drawable imgDrawable) {
 			
 			try {
 				ViewGroup card = (ViewGroup) ((ViewGroup) swiper.getChildAt(0)).findViewWithTag(suggestion);
 				getImageViewChild(card).setImageDrawable(imgDrawable);
 			} catch(NullPointerException e) {
 				Log.e("Suggestible", "Card for image could not be found.");
 			}
 		}
 	}
 	
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
     	
         super.onCreate(savedInstanceState);
         setContentView(R.layout.card_layout);
         Log.d("Suggestible", "I'm running!");
         
 //        SharedPreferences prefs = getPreferences(0);
 //        SharedPreferences.Editor prefEdit = prefs.edit();
 //        prefEdit.putInt("length", 0);
 
 		suggestionType.add("outing");
     	suggestionType.add("book");
     	suggestionType.add("movie");
     	suggestionType.add("restaurant");
     	filterButtons = new ImageButton[4];
     	
         filters = (LinearLayout) findViewById(R.id.filters);
 		for (int i = 0; i < 4; i++) {
         	filterButtons[i] = (ImageButton) filters.getChildAt(i);
         	filterButtons[i].setTag("on");
 		}
         
         locManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
         location = locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
         Log.d("Suggestible", "lat/long: " + location.getLatitude() + "/" + location.getLongitude());
         
         new MovieFetcher().execute();
         new BookFetcher().execute();
         new RestaurantFetcher().execute(location.getLatitude(), location.getLongitude());
         new OutingFetcher().execute(location.getLatitude(), location.getLongitude());
         
         cardmaker = new OnDemandCardmaker();
         swiper = (uk.co.jasonfry.android.tools.ui.SwipeView) findViewById(R.id.flipper);
         
                         
         
             
         //updateFilters();
         fixFonts();
 		
 		swiper.setOnPageChangedListener(cardmaker);
 		
 		ImageView arrowleft = (ImageView) findViewById(R.id.back);
         ImageView arrowright = (ImageView) findViewById(R.id.next);
         arrowleft.setAlpha(100);
         arrowright.setAlpha(100);
 
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.layout.main_menu, menu);
         return true;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         // Handle item selection
         switch (item.getItemId()) {
             case R.id.about:
                 Intent aboutIntent = new Intent(CardActivity.this, About.class);
                 CardActivity.this.startActivity(aboutIntent);
                 return true;
             case R.id.deleted:
                 Intent detailsIntent = new Intent(CardActivity.this, UndeleteActivity.class);
                 CardActivity.this.startActivity(detailsIntent);
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
     
     public void filterCards() {
     	
     	ArrayList<View> cards = new ArrayList<View>(0);
     	for(int i = 0; i < swiper.getPageCount(); i++) {
     		cards.add(getCardByPageNum(i));
     	}
     	
     	int currentPage = swiper.getCurrentPage();
     	for(View card : cards) {
     		String typeString = ((Suggestion) card.getTag()).type;
     		int type = suggestionType.indexOf(typeString);
     		
     		// who knows wtf this view is - remove it
     		if (type == -1) {
     			((ViewGroup) swiper.getChildAt(0)).removeView(card);
     			
     			// adjust the page we'll end up afterward, but only if the deleted view was before the current one.
     			if (cards.indexOf(card) <= currentPage) {
     				currentPage--;
     			}
     			
     		// the filter for this card is off
     		} else if ( filterButtons[type].getTag().equals("off") ) {
     			((ViewGroup) swiper.getChildAt(0)).removeView(card);
     			// adjust the page we'll end up afterward, but only if the deleted view was before the current one.
     			if (cards.indexOf(card) <= currentPage) {
     				currentPage--;
     			}
 
     		}
     	}
     	
     	swiper.scrollToPage(currentPage);
 
     	// replenish!
     	cardmaker.addCard();
     }
     
     public class OnDemandCardmaker implements OnPageChangedListener {
 
 		public void onPageChanged(final int oldpage, final int newpage) {
 			
 			if (newpage == 0) {
 				addCard();
 			}
 			
 			if (newpage > oldpage) {
 				addCard();
 				fixFonts();
 			}
 			
 		}
 		
 		private int pickCardType() {
 			Random rand = new Random(); 
 			int i = rand.nextInt(4);
 			int count = 0;
 			if (updateFilters() == 0) {
 				Toast.makeText(CardActivity.this, "No more suggestions available. Try enabling more types!", Toast.LENGTH_SHORT).show();
 			}
 			while(filterButtons[i].getTag().equals("off")) {
 				i++;
 				count++;
 				i = i % 4;
 				
 				if (count > 4) {
 					Toast.makeText(CardActivity.this, "No filters enabled!", Toast.LENGTH_SHORT).show();
 					return 0;
 				}
 			}
 			return i;
 		}
 		
 		public void addCard() {
 			if (swiper.getPageCount() == 1) {
 				((ViewGroup) swiper.getChildAt(0)).removeViewAt(0);
 				addCard();
 			}
 			
 			if (swiper.getPageCount() - swiper.getCurrentPage() < 2) {
 				
 				int cardNum = swiper.getPageCount();
 				View newCard = new View(CardActivity.this);
 				newCard.inflate(CardActivity.this, R.layout.main_card_layout, (ViewGroup) swiper);
 
 				
 				Intent detailsIntent = null;
 				
 				int typeImageResource = R.drawable.footprint;
 				Suggestion card = null;
 				Suggestion tag = null;
 				int currentCardType = 0;
 				try {
 					while(card == null) {
 						currentCardType = pickCardType();
 					
 						switch(currentCardType) {
 						case 0:
 							card = outings.remove(0);
 							tag = card;
 							typeImageResource = R.drawable.footprint;
 							
 							detailsIntent = new Intent(CardActivity.this, PlaceInfoActivity.class);
 //							// Pass JSON variables to the new info display activity (id not included for books)
 							detailsIntent.putExtra("title", card.title);
 							detailsIntent.putExtra("imagesrc", card.imageurl);
 							detailsIntent.putExtra("rating", card.rating * 20);
 							detailsIntent.putExtra("description", card.description);
 							detailsIntent.putExtra("location", card.maplocation);
 							detailsIntent.putExtra("latitude", location.getLatitude() / 1000);
 							detailsIntent.putExtra("longitude", location.getLongitude() / 1000);
 							detailsIntent.putExtra("url", card.url);
 							break;
 						case 1:
 							card = books.remove(0);
 							tag = card;
 							getImageViewByPageNum(cardNum).setScaleType(ImageView.ScaleType.FIT_XY);
 							typeImageResource = R.drawable.book;
 							
 							detailsIntent = new Intent(CardActivity.this, BookInfoActivity.class);
 //							// Pass JSON variables to the new info display activity (id not included for books)
 							detailsIntent.putExtra("title", card.title);
 							detailsIntent.putExtra("author", card.author);
 							detailsIntent.putExtra("description", card.description);
 							detailsIntent.putExtra("imagesrc", card.imageurl);
 							Log.d("Suggestible", "book image source: " + card.imageurl);
 							
 							break;
 						case 2:
 							card = movies.remove(0);
 							tag = card;
 							getImageViewByPageNum(cardNum).setScaleType(ImageView.ScaleType.FIT_XY);
 							getImageViewByPageNum(cardNum).setLayoutParams(new LayoutParams(250, 400));
 							typeImageResource = R.drawable.movie;
 							
 							detailsIntent = new Intent(CardActivity.this, MovieInfoActivity.class);
 //							// Pass JSON variables to the new info display activity (id not included for books)
 							detailsIntent.putExtra("title", card.title);
 							detailsIntent.putExtra("imagesrc", card.imageurl);
 							detailsIntent.putExtra("rating", card.rating);
 							detailsIntent.putExtra("description", card.description);
 							
 							//fix these
 							detailsIntent.putExtra("mpaa", card.mpaa_rating);
 							detailsIntent.putExtra("runtime", card.runtime);
 							break;
 						case 3:
 							card = restaurants.remove(0);
 							tag = card;
 							typeImageResource = R.drawable.food;
 							
 							detailsIntent = new Intent(CardActivity.this, RestaurantInfoActivity.class);
 //							// Pass JSON variables to the new info display activity (id not included for books)
 							detailsIntent.putExtra("title", card.title);
 							detailsIntent.putExtra("rating", card.rating * 20);
 							detailsIntent.putExtra("description", card.description);
 							detailsIntent.putExtra("imagesrc", card.imageurl);
 							detailsIntent.putExtra("location", card.maplocation);
 							detailsIntent.putExtra("latitude", location.getLatitude() / 1000);
 							detailsIntent.putExtra("longitude", location.getLongitude() / 1000);
 							detailsIntent.putExtra("url", card.url);
 							break;
 						}
 					} 
 				} catch(IndexOutOfBoundsException e) {
 					if(updateFilters() > 1) {
 						Toast.makeText(CardActivity.this, "No more " + suggestionType.get(currentCardType) + "s available.", Toast.LENGTH_SHORT).show();
 						filterButtons[currentCardType].setTag("off");
 						updateFilters();
 					} else {
 						Toast.makeText(CardActivity.this, "No more " + suggestionType.get(currentCardType) + "s available.", Toast.LENGTH_SHORT).show();
 						((ViewGroup) swiper.getChildAt(0)).removeView(getCardByPageNum(cardNum));
 						return;
 					}
 				}
 				final Intent finalDetails = detailsIntent;
 				
 				ImageView typeIcon = (ImageView) getCardByPageNum(cardNum).findViewWithTag("typeicon");
 				//Log.d("Suggestible", typeIcon.toString());
 				typeIcon.setImageResource(typeImageResource);
 
 				getCardByPageNum(cardNum).setTag(tag);
				String textToSet = "";
				try {
					textToSet = card.title;
				} catch(NullPointerException e) {
				}
 				getTextViewByPageNum(cardNum).setText(textToSet);
 
 				
				//Log.d("Suggestible", "Trying to load image " + card.imageurl);
 				
 				new DrawableFetcher().execute(card);
 				
 				getCardButtonsByPageNum(swiper.getPageCount() - 1)[0].setOnClickListener(new OnClickListener() {
 					public void onClick(View v) {	
 						CardActivity.this.startActivity(finalDetails);
 					}
 				});
 				
 				//Log.d("Suggestible", "new card tag: " + getCardByPageNum(cardNum).getTag());
 				
 				getCardButtonsByPageNum(swiper.getPageCount() - 1)[1].setOnClickListener(new OnClickListener() {
 
 					public void onClick(View v) {
 						remainingPages--;
 //						deleteSuggestion( ((Suggestion) ((View) v.getParent()).getTag()).title );
 //						Log.d("Suggestible", "deleting title: " + ((Suggestion) ((View) v.getParent()).getTag()).title);
 						((ViewGroup) swiper.getChildAt(0)).removeView((View) v.getParent());
 						addCard();
 						
 					}
 					
 				});
 
 			}
 			
 
			if(swiper.getPageCount() > 12) {
 				int current = swiper.getCurrentPage();
 				((ViewGroup) swiper.getChildAt(0)).removeViewAt(0);
 				swiper.scrollToPage(current - 1);
 			}
 		}
     }
     /*
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {
         if ( keyCode == KeyEvent.KEYCODE_MENU ) {
         	Intent detailsIntent = new Intent(CardActivity.this, UndeleteActivity.class);
 			CardActivity.this.startActivity(detailsIntent);
             return true;
         }
         return super.onKeyDown(keyCode, event);
     }
     */
     public int updateFilters() {
     	int active = 0;
     	for (int i = 0; i < 4; i++) {
     		if (filterButtons[i].getTag().equals("on")) {
     			active ++;
     			filterButtons[i].setBackgroundResource(R.drawable.button);
     			switch(i) {
     				case 0:
     					filterButtons[i].setImageResource(R.drawable.footprint);
     					break;
     				case 1:
     					filterButtons[i].setImageResource(R.drawable.book);
     					break;
     				case 2:
     					filterButtons[i].setImageResource(R.drawable.movie);
     					break;
     				case 3:
     					filterButtons[i].setImageResource(R.drawable.food);
     					break;
     			}
     		} else {
     			filterButtons[i].setBackgroundResource(R.drawable.button_dark2);
     			switch(i) {
 					case 0:
 						filterButtons[i].setImageResource(R.drawable.footprintlight);
 						break;
 					case 1:
 						filterButtons[i].setImageResource(R.drawable.booklight);
 						break;
 					case 2:
 						filterButtons[i].setImageResource(R.drawable.movielight);
 						break;
 					case 3:
 						filterButtons[i].setImageResource(R.drawable.foodlight);
 						break;
     			}
     		}
         } 
     	return active;
     }
     
     
     /**
      * Recursively sets a typeface to all TextViews in a ViewGroup.
      */
     public static final void setAppFont(ViewGroup mContainer, Typeface mFont)
     {
         if (mContainer == null || mFont == null) return;
 
         final int mCount = mContainer.getChildCount();
 
         // Loop through all of the children.
         for (int i = 0; i < mCount; ++i)
         {
             final View mChild = mContainer.getChildAt(i);
             if (mChild instanceof TextView)
             {
                 // Set the font if it is a TextView.
                 ((TextView) mChild).setTypeface(mFont);
             }
             else if (mChild instanceof ViewGroup)
             {
                 // Recursively attempt another ViewGroup.
                 setAppFont((ViewGroup) mChild, mFont);
             }
         }
     }
     
     
     
     public final void fixFonts() {
     	
         final Typeface font = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Regular.ttf");
 		final ViewGroup mContainer = (ViewGroup) findViewById(android.R.id.content).getRootView();
 		CardActivity.setAppFont(mContainer, font);
 
     }
     
     
     
     public TextView getTextViewByPageNum(int page) {
     	
     	RelativeLayout card = (RelativeLayout) ((ViewGroup) swiper.getChildAt(0)).getChildAt(page);
     	return getTextViewChild(card);
     }
     
     public ImageView getImageViewByPageNum(int page) {
     	
     	RelativeLayout card = (RelativeLayout) ((ViewGroup) swiper.getChildAt(0)).getChildAt(page);
     	return getImageViewChild(card);
     }
     
     public RelativeLayout getCardByPageNum(int page) {
     	return (RelativeLayout) ((ViewGroup) swiper.getChildAt(0)).getChildAt(page);
     }
     
     public TextView getTextViewChild(ViewGroup group) {
     	int childCount = group.getChildCount();
     	
     	for (int i = 0; i < childCount; i++) {
     		View child = group.getChildAt(i);
     		if (child instanceof TextView) {
     			return (TextView) child;
     		} else if (child instanceof ViewGroup) {
     			return getTextViewChild((ViewGroup) child);
     		}
     	}
     	
     	return null;
     }
     
     public ImageView getImageViewChild(ViewGroup group) {
     	int childCount = group.getChildCount();
     	
     	for (int i = 0; i < childCount; i++) {
     		View child = group.getChildAt(i);
     		if (child instanceof ImageView) {
     			return (ImageView) child;
     		} else if (child instanceof ViewGroup) {
     			return getImageViewChild((ViewGroup) child);
     		}
     	}
     	
     	return null;
     }
     
     public Button[] getCardButtonsByPageNum(int page) {
     	Button[] buttons = new Button[2];
     	
     	buttons[0] = (Button) getCardByPageNum(page).findViewWithTag("first");
     	buttons[1] = (Button) getCardByPageNum(page).findViewWithTag("second");
     	
     	return buttons;
     }
 
     public void deleteSuggestion(String title){
         SharedPreferences prefs = getPreferences(0);
         SharedPreferences.Editor prefEdit = prefs.edit();
         int length = prefs.getInt("length", 0);
         Log.d("Suggestible", "length of deleted list: " + length);
         prefEdit.putString(""+length, title);
         length++;
         prefEdit.putInt("length", length);
         Log.d("Suggestible", "new length of deleted list: " + prefs.getInt("length", 0));
     }
 
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
 }
