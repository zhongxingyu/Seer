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
 
 
 public class SearchActivity extends Base_Activity {
 	
 	private ListView listViewGames;
 	private Context ctx;
 
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
 
 		final EditText SearchText = (EditText) findViewById(R.id.editTextSearch);
 		Button SearchButton = (Button) findViewById(R.id.buttonSearch);
 		ctx = this;
 		
 		Intent intent = getIntent();
 		String Username = intent.getStringExtra("UserName");
 		int Userkey = intent.getIntExtra("UKey", 0);
 		
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
 				ArrayList<Game> AGames = new ArrayList<Game>();
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
 
 				Toast.makeText(getApplicationContext(),
 						"Click GameItemNumber " + position, Toast.LENGTH_LONG)
 						.show();
 				// Takes user to GameView page with required data.
 				/*
 				 * Intent i = new Intent(getApplicationContext(),
 				 * GameView.class); i.putExtra("key",
 				 * LGames.getShowGames().get(position).getKey());
 				 * i.putExtra("title", LGames.getShowGames().get(position)
 				 * .getTitle()); i.putExtra("platform",
 				 * LGames.getShowGames().get(position) .getPlatform());
 				 * i.putExtra("overview", LGames.getShowGames().get(position)
 				 * .getOverview()); i.putExtra("genre",
 				 * LGames.getShowGames().get(position) .getGenre());
 				 * i.putExtra("developer", LGames.getShowGames().get(position)
 				 * .getDeveloper()); startActivity(i);
 				 */
 		
 			}
 		});
 		
 		
 	}
 
 }
