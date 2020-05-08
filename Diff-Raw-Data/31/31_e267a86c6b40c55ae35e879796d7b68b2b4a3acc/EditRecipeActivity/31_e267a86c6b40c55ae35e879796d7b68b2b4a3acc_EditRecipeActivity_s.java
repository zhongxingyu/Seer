 package ca.ualberta.cmput301w13t11.FoodBook;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.View;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.view.WindowManager;
 import android.widget.LinearLayout;
 import android.widget.PopupWindow;
 import ca.ualberta.cmput301w13t11.FoodBook.controller.DbController;
 import ca.ualberta.cmput301w13t11.FoodBook.model.DbManager;
 import ca.ualberta.cmput301w13t11.FoodBook.model.FView;
 import ca.ualberta.cmput301w13t11.FoodBook.model.Recipe;
 
 
 public class EditRecipeActivity extends Activity implements FView<DbManager>
 {
 	static final String EXTRA_URI = "extra_uri";
 	PopupWindow popUp;
 	private long uri;
 	private Recipe viewedRecipe;
 	private EditText recipeName;
 	private EditText instructions;
 
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
 		
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_edit_recipe);
 		
 		recipeName = (EditText) findViewById(R.id.editText1);
 		instructions = (EditText) findViewById(R.id.editText3);
 		
 		Intent intent = getIntent();
 		//String URI = intent.getStringExtra(ViewRecipeActivity.EXTRA_URI);
 		//long uri=Long.parseLong(URI);
 		uri = intent.getLongExtra(EXTRA_URI, 0);
 		
 		/*
 		ArrayList<Recipe> RecipeList= DbC.getUserRecipes();
 		Recipe viewedRecipe = Recipe.generateTestRecipe();
 		
 		for(int index=0; index<RecipeList.size(); index++)
 		{
 			if(RecipeList.get(index).getUri()==uri)
 					{
 					viewedRecipe=RecipeList.get(index);
 					index=RecipeList.size();
 					
 					}
 		}
 		
 		*/
 		DbController DbC = DbController.getInstance(this, this);
 		viewedRecipe = DbC.getUserRecipe(uri);
 		recipeName.setText(viewedRecipe.getTitle());
 		instructions.setText(viewedRecipe.getInstructions());
 
 	}
 		
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu)
 	{
 
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.edit_recipe, menu);
 		return true;
 	}
 	public void OnGotoMyRecipes(View View)
     {
 		// responds to button Go Back to My Recipes
 		 Intent intent = new Intent(View.getContext(), MyRecipes.class);
 		 intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 		 EditRecipeActivity.this.finish();
     }
 	
	public void OnPhotos (View View)
     {
 		// responds to button OnPhotos
     	Intent intent = new Intent(this, EditPhotos.class);
     	intent.putExtra(EXTRA_URI, uri);
 		startActivity(intent);
     }
 	public void OnIngredients (View View)
     {
 		// responds to button Ingredients
     	Intent intent = new Intent(this, EditIngredients.class);
 		startActivity(intent);
     }
 	public void OnSaveChanges (View View)
     {
 		// responds to button Save Changes
 		
     }
 	public void OnDeleteRecipe (View View)
     {
 		// responds to button Delete Recipe
 		
 		//first darken the screen
 		ImageView darkenScreen = (ImageView) findViewById(R.id.darkenScreen);
 		LayoutParams darkenParams = darkenScreen.getLayoutParams();
 		darkenParams.height = 1000;
 		darkenParams.width = 1000;
 		darkenScreen.setLayoutParams(darkenParams);
 		//make the popup
 		LinearLayout layout = new LinearLayout(this);
 		LayoutInflater inflater = LayoutInflater.from(this);
 		popUp = new PopupWindow(inflater.inflate(R.layout.popup_delete_recipe, null, false),300,150,true);
 		popUp.showAtLocation(layout, Gravity.CENTER, 0, 0);
 		
 		//Log.d("what", "what");
 		//popUp.update(50, 50, 300, 80);
 		
 
     }
 	@Override
 	public void update(DbManager db)
 	{
 
 		// TODO Auto-generated method stub
 		DbController DbC = DbController.getInstance(this, this);
 		viewedRecipe = DbC.getUserRecipe(uri);
 		recipeName.setText(viewedRecipe.getTitle());
 		instructions.setText(viewedRecipe.getInstructions());
 		
 	}
 	public void OnOK(View v){
 		
 		popUp.dismiss();
 		//delete the recipe
 		DbController DbC = DbController.getInstance(this, this);
 		DbC.deleteRecipe(viewedRecipe);
 		//Intent intent = getIntent();
 		//String URI = intent.getStringExtra(ViewRecipeActivity.EXTRA_URI);
 		//long uri=Long.parseLong(URI);
 		/*
 		ArrayList<Recipe> RecipeList= DbC.getUserRecipes();		
 		for(int index=0; index<RecipeList.size(); index++)
 		{
 			if(RecipeList.get(index).getUri()==uri)
 					{
 					DbC.deleteRecipe(RecipeList.get(index));
 					index=RecipeList.size();
 					
 					}
 		}
 		*/
 		Intent intentGo = new Intent(this, MyRecipes.class);
 		startActivity(intentGo);
 		finish();
 	}
 	public void OnCancel(View v){
 		
 		
 		popUp.dismiss();
 		//remove the darkScreen
 		ImageView darkenScreen = (ImageView) findViewById(R.id.darkenScreen);
 		LayoutParams darkenParams = darkenScreen.getLayoutParams();
 		darkenParams.height = 0;
 		darkenParams.width = 0;
 		darkenScreen.setLayoutParams(darkenParams);
 	}
 	public void onDestroy()
 	{	super.onDestroy();
 		DbController DbC = DbController.getInstance(this, this);
 		DbC.deleteView(this);
 	}
 
 }
