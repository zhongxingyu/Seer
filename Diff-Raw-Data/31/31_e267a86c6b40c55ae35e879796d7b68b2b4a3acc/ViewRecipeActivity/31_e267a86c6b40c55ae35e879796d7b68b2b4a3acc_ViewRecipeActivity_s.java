 package ca.ualberta.cmput301w13t11.FoodBook;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.PopupWindow;
 import android.widget.TextView;
 import ca.ualberta.cmput301w13t11.FoodBook.controller.DbController;
 import ca.ualberta.cmput301w13t11.FoodBook.controller.ServerController;
 import ca.ualberta.cmput301w13t11.FoodBook.model.DbManager;
 import ca.ualberta.cmput301w13t11.FoodBook.model.FView;
 import ca.ualberta.cmput301w13t11.FoodBook.model.Recipe;
 
 public class ViewRecipeActivity extends Activity implements FView<DbManager>
 {
 
 	static final String EXTRA_URI = "extra_uri";
 	private long uri;
 	private Recipe viewedRecipe;
 	private PopupWindow popUp;
 	private TextView recipeName;
 	private TextView instructions;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{	
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_view_recipe);
 		
 		recipeName = (TextView) findViewById(R.id.textView2);
 		instructions = (TextView) findViewById(R.id.textView5);
 		
 		Intent intent = getIntent();
 		//String URI = intent.getStringExtra(ViewRecipeActivity.EXTRA_URI);
 		//long uri=Long.parseLong(URI);
 		uri = intent.getLongExtra(EXTRA_URI, 0);
 		//Log.d("recipeuri", Long.toString(uri));
 
 		this.updateView();
 	}
 	public void updateView(){
 		DbController DbC = DbController.getInstance(this, this);
 		viewedRecipe = DbC.getUserRecipe(uri);
 		recipeName.setText(viewedRecipe.getTitle());
 		instructions.setText(viewedRecipe.getInstructions());
 	}
 
 	public void OnGotoMyRecipes(View View)
 	{
 		// responds to button Go Back
 		 Intent intent = new Intent(View.getContext(), MyRecipes.class);
 		 intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 		 ViewRecipeActivity.this.finish();
     }
 	
 	public void OnEditRecipe (View View)
     {	
 		//Intent intent1 = getIntent();
 		//String URI = intent1.getStringExtra(MyRecipes.EXTRA_URI);
 		
 		// responds to button Edit Recipe
     	Intent intent = new Intent(this, EditRecipeActivity.class);
     	//intent.putExtra(EXTRA_URI, URI);
     	intent.putExtra(EXTRA_URI, uri);
 		startActivity(intent);
     }
	public void OnViewPhotos (View View)
     {
 		// responds to button Edit Photos
    	Intent intent = new Intent(this, ViewPhotosActivity.class);
     	intent.putExtra(EXTRA_URI, uri);
 		startActivity(intent);
     }
 	public void OnEmailRecipe (View View)
     {
 		// responds to button Email Recipe
     	
     }
 	public void OnPublishRecipe (View View)
     {
 		// responds to button Publish Recipe
 		DbController DbC = DbController.getInstance(this, this);
 		ServerController SC=ServerController.getInstance(this);
 		Intent intent = getIntent();
 		//String URI = intent.getStringExtra(MyRecipes.EXTRA_URI);
 		//long uri=Long.parseLong(URI);
 		intent.putExtra(EXTRA_URI, uri);
 		SC.uploadRecipe(viewedRecipe);
 		/*
 		ArrayList<Recipe> RecipeList= DbC.getUserRecipes();
 		
 		for(int index=0; index<RecipeList.size(); index++)
 		{
 			if(RecipeList.get(index).getUri()==uri)
 					{
 					SC.uploadRecipe(RecipeList.get(index));
 					index=RecipeList.size();
 					
 					}
 		}
 		*/
 		//first darken the screen
 		ImageView darkenScreen = (ImageView) findViewById(R.id.darkenScreen);
 		LayoutParams darkenParams = darkenScreen.getLayoutParams();
 		darkenParams.height = 1000;
 		darkenParams.width = 1000;
 		darkenScreen.setLayoutParams(darkenParams);
 		//make the popup
 		LinearLayout layout = new LinearLayout(this);
 		LayoutInflater inflater = LayoutInflater.from(this);
 		popUp = new PopupWindow(inflater.inflate(R.layout.popup_recipe_upload_success, null, false),300,130,true);
 		popUp.showAtLocation(layout, Gravity.CENTER, 0, 0);
     	
     }
 	public void OnOK(View v){
 		popUp.dismiss();
 		//remove the darkScreen
 		ImageView darkenScreen = (ImageView) findViewById(R.id.darkenScreen);
 		LayoutParams darkenParams = darkenScreen.getLayoutParams();
 		darkenParams.height = 0;
 		darkenParams.width = 0;
 		darkenScreen.setLayoutParams(darkenParams);
 	}
 	@Override
 	public void update(DbManager db)
 	{
 		this.updateView();
 	}
 
 	public void onDestroy()
 	{	super.onDestroy();
 		DbController DbC = DbController.getInstance(this, this);
 		DbC.deleteView(this);
 	}
 }
