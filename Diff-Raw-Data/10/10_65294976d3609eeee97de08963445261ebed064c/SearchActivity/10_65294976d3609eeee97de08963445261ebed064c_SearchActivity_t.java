 package com.derpicons.gshelf;
 
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.Toast;
 import android.app.Activity;
 import android.view.GestureDetector;
 import android.view.MotionEvent;
 
 
 public class SearchActivity extends Base_Activity {
 	
 	private ListView listViewGames;
 	private Context ctx;
 	private ArrayList<Game> AGames;
	private String Username;
	private int Userkey;
 	
 	// swipe constants
 	private static final int SWIPE_MIN_DISTANCE = 120;
 	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
 
 	private GestureDetector gestureDetector;
 
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) 
 	{
 		// Inflate the menu
 		getMenuInflater().inflate(R.menu.search, menu);
 		
 		return true;
 	}
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_search);
 				
 		//ActionBar actionBar = getActionBar();
 		//actionBar.setDisplayHomeAsUpEnabled(true);
 
 		// Listen for swipes
 		gestureDetector = new GestureDetector(this,
 				new OnSwipeGestureListener());
 		
 		final EditText SearchText = (EditText) findViewById(R.id.editTextSearch);
 		Button SearchButton = (Button) findViewById(R.id.buttonSearch);
 		ctx = this;
 		
 		Intent intent = getIntent();
 		Username = intent.getStringExtra("UserName");
 		Userkey = intent.getIntExtra("UKey", 0);
 		
 		//ArrayList<Game> AGames = new ArrayList<Game>();
 
 		// Display list of games
 		listViewGames = (ListView) findViewById(R.id.result_item);
 		listViewGames.setClickable(true);
 
 		// Display list of games
 		
 		SearchButton.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				String search = SearchText.getText().toString();
 				AGames = new ArrayList<Game>();
 				if (search.length() != 0) {
 					AGames = new Network(ctx).getGames(search);
 				}
 				listViewGames.setAdapter(new SearchListAdapter(ctx, R.layout.result_item,
 						AGames));
 				
 			}
 		});
 		
 		
 		listViewGames.setOnItemClickListener(new OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View view,
 					int position, long id) {
 
 				//Toast.makeText(getApplicationContext(),
 				//		"Click GameItemNumber " + position, Toast.LENGTH_LONG)
 				//		.show();
 				
 				Intent i = new Intent(getApplicationContext(), SearchInfo.class);
 				i.putExtra("GameKey", AGames.get(position).getKey());
 				i.putExtra("UserName", Username);
 				i.putExtra("UKey", Userkey);
 				startActivity(i);
 			}
 		});
 		
 		
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
 		Intent i = new Intent(getApplicationContext(), DealsView.class);
		i.putExtra("UserName", Username);
		i.putExtra("UKey", Userkey);
 		startActivity(i);
 	}
 
 	// Handle swipe from right to left
 	private void handleSwipeRightToLeft() {
 		Intent i = new Intent(getApplicationContext(), GamesLibrary.class);
		i.putExtra("UserName", Username);
		i.putExtra("UKey", Userkey);
 		startActivity(i);
 	}	
 }
