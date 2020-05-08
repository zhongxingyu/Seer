 package ca.ualberta.cmput301w13t11.FoodBook;
 
 import ca.ualberta.cmput301w13t11.FoodBook.model.DbManager;
 import ca.ualberta.cmput301w13t11.FoodBook.model.FView;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.View;
 
 public class SearchResultsActivity extends Activity implements FView<DbManager>
 {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
 
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_search_results);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu)
 	{
 
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.search_results, menu);
 		return true;
 	}
 	public void OnGotoMainMenu(View View)
     {
 		// responds to button Go Back to Main Menu
 		 Intent intent = new Intent(View.getContext(), MainScreen.class);
 		 intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 		 SearchResultsActivity.this.finish();
     }
 	public void OnSearchAgain(View View)
     {
 		// responds to button Search Again
 		 Intent intent = new Intent(View.getContext(), SearchActivity.class);
 		 intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 		 SearchResultsActivity.this.finish();
     }

 	@Override
 	public void update(DbManager db)
 	{

		// TODO Auto-generated method stub
		
 	}
 
 }
