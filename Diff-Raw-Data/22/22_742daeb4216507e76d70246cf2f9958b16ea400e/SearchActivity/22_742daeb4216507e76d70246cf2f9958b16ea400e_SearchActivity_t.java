 package ca.ualberta.cmput301w13t11.FoodBook;
 
 import ca.ualberta.cmput301w13t11.FoodBook.controller.ServerController;
 import ca.ualberta.cmput301w13t11.FoodBook.model.DbManager;
 import ca.ualberta.cmput301w13t11.FoodBook.model.FView;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.View;
 import android.widget.EditText;
 
 public class SearchActivity extends Activity implements FView<DbManager>
 {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
 
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_search);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu)
 	{
 
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_search, menu);
 		return true;
 	}
 	public void OnGotoMainMenu(View View)
     {
 		// responds to button Main Menu
 		 Intent intent = new Intent(View.getContext(), MainScreen.class);
 		 intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 		 SearchActivity.this.finish();
     }
 	/*
 	 * Sends the user input keywords to the server controller
 	 */
 	
 	public void OnSearch (View View)
     {
 		EditText keywords = (EditText) findViewById(R.id.editText1);
 		String keyword=keywords.toString();
 		ServerController SC=ServerController.getInstance();
 		SC.searchByKeywords(keyword);
 		
     	Intent intent = new Intent(this, SearchResultsActivity.class);
 		startActivity(intent);
     }
 
 	@Override
 	public void update(DbManager db)
 	{
 		
 	}
 
 }
