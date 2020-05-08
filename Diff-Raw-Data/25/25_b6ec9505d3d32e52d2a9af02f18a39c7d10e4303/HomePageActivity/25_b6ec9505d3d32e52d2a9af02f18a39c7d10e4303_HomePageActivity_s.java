 package cs169.project.thepantry;
 
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.AdapterView.OnItemClickListener;
 
 public class HomePageActivity extends BasicMenuActivity {
 	
 	ArrayList<SearchMatch> recommendations;
 	SearchResultAdapter srAdapter;
 	SearchModel sm = new SearchModel();
 	ListView listView;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_home_page);
 		
 		recommendations = new ArrayList<SearchMatch>();
 		listView = (ListView) findViewById(R.id.recsList);
 		srAdapter = new SearchResultAdapter(getApplicationContext(), recommendations);          
 		listView.setAdapter(srAdapter);
 		srAdapter.notifyDataSetChanged();
 		
 		//when a search result item is clicked
 		listView.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> parent, View view,
 					int position, long id) {
 			    // When clicked
 				if (isOnline()){
 		    		SearchCriteria searchcriteria = new SearchCriteria("recipe", (String)view.getTag());
 		    		new HomeSearchTask(getApplicationContext()).execute(searchcriteria);
 				}
 			}
 		});
 		
 		if (isOnline()) {
 			getRecommendations();
 		}
 		
 	}
 	
 	public void search(View view) throws Exception {
 		EditText searchText = (EditText) findViewById(R.id.search_text);
     	String search = searchText.getText().toString();
     	if (isOnline()) {
     		SearchCriteria searchcriteria = new SearchCriteria("search", search);
     		new HomeSearchTask(getApplicationContext()).execute(searchcriteria);
     	}
 	}
 	
 	public void getRecommendations() {
 		SearchCriteria searchcriteria = new SearchCriteria("home", "bacon", 4);
 		new HomeSearchTask(getApplicationContext()).execute(searchcriteria);
 	}
 	
 	public class HomeSearchTask extends AsyncTask<SearchCriteria, String, Storage> {
 		
 		String type;
 		String q;
 		Context context;
 		
 		public HomeSearchTask(Context context) {
 		    	this.context = context;
 		}
 		
 		@Override
 		protected Storage doInBackground(SearchCriteria... sc) {
 			this.type = sc[0].type;
 			this.q = sc[0].q;
 			return sm.search(sc[0]);
 		}
 		
 		@Override
 		protected void onPostExecute(Storage result) {
 			if (this.type == "home") {
 				if (srAdapter.values.size() == 0) {
 					recommendations = ((SearchResult)result).matches;
 					srAdapter = new SearchResultAdapter(HomePageActivity.this, recommendations);   
 					listView.setAdapter(srAdapter);
					listView.bringToFront();
 				} else {
 					srAdapter.values = ((SearchResult)result).matches; 
					recommendations = ((SearchResult)result).matches;
 					srAdapter.notifyDataSetChanged();
 				}
 			}
 			else if (this.type == "search") {
 				Intent intent = new Intent(context, SearchResultsActivity.class);
 				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 				intent.putExtra("result", result);
 				intent.putExtra("currentSearch", this.q);
 				startActivity(intent);
 			}
 			else if (this.type == "recipe") {
 				Intent intent = new Intent(context, RecipeActivity.class);
 				intent.putExtra("result", result);
 				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 				startActivity(intent);
 			}
 		}
 	}
 }
