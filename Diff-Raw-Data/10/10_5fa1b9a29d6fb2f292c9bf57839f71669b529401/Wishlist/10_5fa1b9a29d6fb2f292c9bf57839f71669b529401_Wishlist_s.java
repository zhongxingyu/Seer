 package com.derpicons.gshelf;
 
 import java.util.ArrayList;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 import android.widget.Toast;
 import android.app.Activity;
 import android.view.GestureDetector;
 import android.view.MotionEvent;
 
 public class Wishlist extends Base_Activity {
 
 	private ListView listViewGames;
 	private Context ctx;
 	private ArrayList<Game> WishListGames;
	String Username;
	int Userkey;
 	private SearchListAdapter SelectedSearchListAdapter;
 
 	// swipe constants
 	private static final int SWIPE_MIN_DISTANCE = 120;
 	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
 
 	private GestureDetector gestureDetector;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_wishlist);
 		ctx = this;
 		
 		// Listen for swipes
 		gestureDetector = new GestureDetector(this,
 				new OnSwipeGestureListener());
 
 		Intent intent = getIntent();
 		Username = intent.getStringExtra("UserName");
 		Userkey = intent.getIntExtra("UKey", 0);
 
 		// Get list of wishlist games
 
 		WishListGames = new ArrayList<Game>();
 		LocalDatabase LD = new LocalDatabase(ctx);
 		WishListGames = LD.getGamesFromWishlist();
 		LD.close();
 
 		// Display list of wishlist games
 		listViewGames = (ListView) findViewById(R.id.wishlist_item);
 		SelectedSearchListAdapter = new SearchListAdapter(ctx,
 				R.layout.result_item, WishListGames);
 		listViewGames.setAdapter(SelectedSearchListAdapter);
 
 		listViewGames.setClickable(true);
 
 		listViewGames.setOnItemClickListener(new OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View view,
 					int position, long id) {
 
 				//Toast.makeText(getApplicationContext(),
 				//		"Click GameItemNumber " + position, Toast.LENGTH_LONG)
 				//		.show();
 				// Takes user to WishListInfo page with required data.
 
 				Intent i = new Intent(getApplicationContext(), WishlistInfo.class);
 				i.putExtra("GameKey", WishListGames.get(position)
 						.getKey());
 				i.putExtra("UserName", Username);
 				i.putExtra("UKey", Userkey);
 				startActivity(i);
 
 			}
 		});
 	}
 	
 	@Override
 	protected void onResume() {
 		super.onResume();
 	
 		WishListGames = new ArrayList<Game>();
 		LocalDatabase LD = new LocalDatabase(ctx);
 		WishListGames = LD.getGamesFromDb(Userkey);
 		LD.close();
 		if(WishListGames != null)
 		{
 			SelectedSearchListAdapter.notifyDataSetChanged();
 		}
 		
 
 	}
 
 	
 	// Swipe accessor function
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		return gestureDetector.onTouchEvent(event);
 	}
 
 	// Swipe Class
 	private class OnSwipeGestureListener extends
 			GestureDetector.SimpleOnGestureListener {
 		// Swipe movement evaluation
 		@Override
 		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
 				float velocityY) {
 
 			float deltaX = e2.getX() - e1.getX();
 
 			if ((Math.abs(deltaX) < SWIPE_MIN_DISTANCE)
 					|| (Math.abs(velocityX) < SWIPE_THRESHOLD_VELOCITY)) {
 				return false; // insignificant swipe
 			} else {
 				if (deltaX < 0) { // left to right
 					handleSwipeLeftToRight();
 				} else { // right to left
 					handleSwipeRightToLeft();
 				}
 			}
 			return true;
 		}
 	}
 
 	// Handle swipe from left to right
 	private void handleSwipeLeftToRight() {
 		Intent i = new Intent(getApplicationContext(), GamesLibrary.class);
 		startActivity(i);
 	}
 
 	// Handle swipe from right to left
 	private void handleSwipeRightToLeft() {
 		Intent i = new Intent(getApplicationContext(), Marketplace.class);
 		startActivity(i);
 	}
 }
