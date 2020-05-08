 package ca.ualberta.cmput301w13t11.FoodBook;
 
 import ca.ualberta.cmput301w13t11.FoodBook.controller.DbController;
 import ca.ualberta.cmput301w13t11.FoodBook.controller.ServerController;
 import ca.ualberta.cmput301w13t11.FoodBook.model.DbManager;
 import ca.ualberta.cmput301w13t11.FoodBook.model.FView;
 import ca.ualberta.cmput301w13t11.FoodBook.model.Recipe;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.TextView;
 
 public class ViewSearchResultActivity extends Activity implements FView<DbManager>
 {
 	public static final String EXTRA_URI = "extra_uri";
 	private long uri;
 	private Recipe viewedRecipe;
 	private TextView recipeName;
 	private TextView instructions;
 
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
 		DbController DbC = DbController.getInstance(this, this);
 		
 		
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_view_search_result);
 		
 		Intent intent = getIntent();
 		// Minor adj. passing uris as long -Pablo
 		//String URI = 
 		//long uri=Long.parseLong(URI);
 		uri = intent.getLongExtra(EXTRA_URI, 0);
 		viewedRecipe=null;
 
 		for(int index=0; index<DbC.getStoredRecipes().size(); index++)
 		{
 			if(DbC.getStoredRecipes().get(index).getUri()==uri)
 					{
 					viewedRecipe=DbC.getStoredRecipes().get(index);
 					index=DbC.getStoredRecipes().size();
 					
 					}
 		}
		if(viewedRecipe!=null){
			recipeName = (TextView) findViewById(R.id.textView2);
			instructions = (TextView) findViewById(R.id.Instructions);
			recipeName.setText(viewedRecipe.getTitle());
			instructions.setText(viewedRecipe.getInstructions());	
		}
 		//This method appeas to function fine if this method is commented out? For now I'll go with it -Thomas
 		//updateView();
 		
 	}
 protected void updateView(){
 		
 		DbController DbC = DbController.getInstance(this, this);
 		
 		//viewedRecipe = DbC.getUserRecipe(uri);
 		recipeName.setText(viewedRecipe.getTitle());
 		instructions.setText(viewedRecipe.getInstructions());
 	}
 
 	
 	public void OnGobacktoSearchResults(View View)
     {
 		// responds to button Go Back to Search Results
 		 Intent intent = new Intent(View.getContext(), SearchResultsActivity.class);
 		 intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 		 ViewSearchResultActivity.this.finish();
     }
 	
 	public void OnViewPhotos (View View)
     {
 		// responds to button View Photos
     	Intent intent = new Intent(this, ViewPhotosActivity.class);
     	intent.putExtra(EXTRA_URI, uri);
 		startActivity(intent);
     }
 	public void OnAddPhotos (View View)
     {
 		// responds to button Add Photos
     	Intent intent = new Intent(this, EditPhotos.class);
 		startActivity(intent);
     }
 	public void OnDownloadRecipe (View View)
     {
 		DbController DbC = DbController.getInstance(this, this);
 		
 		Intent intent = getIntent();
 		long URI = intent.getLongExtra(SearchResultsActivity.EXTRA_URI, 0);
 		//long uri = Long.parseLong(URI);
 		Recipe viewedRecipe=null;
 		for(int index=0; index<DbC.getStoredRecipes().size(); index++)
 		{
 			if(DbC.getStoredRecipes().get(index).getUri()==uri)
 					{
 					viewedRecipe=DbC.getStoredRecipes().get(index);
 					index=DbC.getStoredRecipes().size();
 					
 					}
 		}
 		
 		DbC.addRecipe(viewedRecipe);
 		finish();
 
     	
     }
 
 	@Override
 	public void update(DbManager db)
 	{
 		
 	}
 
 	public void onDestroy()
 	{	super.onDestroy();
 		DbController DbC = DbController.getInstance(this, this);
 		DbC.deleteView(this);
 	}
 }
