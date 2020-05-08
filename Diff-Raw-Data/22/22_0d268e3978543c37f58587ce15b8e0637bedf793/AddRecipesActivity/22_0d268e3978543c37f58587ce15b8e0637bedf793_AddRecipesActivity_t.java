 package ca.ualberta.cmput301w13t11.FoodBook;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import ca.ualberta.cmput301w13t11.FoodBook.controller.DbController;
 import ca.ualberta.cmput301w13t11.FoodBook.model.DbManager;
 import ca.ualberta.cmput301w13t11.FoodBook.model.FView;
 import ca.ualberta.cmput301w13t11.FoodBook.model.Recipe;
 import ca.ualberta.cmput301w13t11.FoodBook.model.User;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.PopupWindow;
 /**
  * Used to create new recipes in the local memory called "My Recipes"
  * @author Thomas Cline and Pablo Jaramillo
  *
  */


 public class AddRecipesActivity extends Activity implements FView<DbManager>
 {
 
 	PopupWindow popUp;
 	static private final Logger logger = Logger.getLogger(AddRecipesActivity.class.getName());
 	private long mRecipeUri; /* timestamp the recipe on entry into the  */
 	public static String EXTRA_URI = "extra_uri"; /*passed with intent to newly launched "edit" acitivities */
 	private Recipe mRecipe; /* the recipe being created.  Saved on entry, we use the setters to set the fields when "press save" is hit */
	private boolean saved=false;
 	
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
 
 		super.onCreate(savedInstanceState);
 
 		/* Initialize the recipe on entry. */
 		mRecipeUri = System.currentTimeMillis();
 		mRecipe = new Recipe(mRecipeUri);
 		mRecipe.setTitle(""); /* We set blank title */
 
 		/* Immediately write the Recipe to the Db -- will be deleted if need be at the appropriate time. */
 		DbController DbC = DbController.getInstance(this, this);
 		DbC.addRecipe(mRecipe);
 
 
 		setContentView(R.layout.activity_add_recipes);
 	}
 
 	/**
 	 * Responds to the button to go back to the "My Recipes" view
 	 * @param The View that is calling the method
 	 *
 	 */
 	
 	public void OnGobacktoMyRecipes(View View)
 	{
 		// responds to button Go Back to My Recipes
 		DbController DbC = DbController.getInstance(this, this);
 		DbC.deleteRecipe(mRecipe);
 		
 		Intent intent = new Intent(View.getContext(), MyRecipes.class);
 		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 		AddRecipesActivity.this.finish();
 	}
 
 
 	/**
 	 * Responds to the "Edit Ingredients" Button. Passes the URI of the 
 	 * current recipe to the edit ingredients activity.
 	 * @param The View that is calling the method
 	 *
 	 */
 	
 	public void OnEditIngredients (View View)
 	{
 		// responds to button Edit Ingredients
 		String uri = Long.toString(mRecipeUri);
 		Intent intent = new Intent(this, EditIngredients.class);
 		intent.putExtra(EXTRA_URI, uri);
 		logger.log(Level.INFO, "Uri passed FROM AddRecipesActivity: " + uri);
 		startActivity(intent);
 	}
 	
 
 	/**
 	 * Responds to the "Edit Photos" Button. Passes the URI of the 
 	 * current recipe to the edit photos activity.
 	 * @param The View that is calling the method
 	 *
 	 */
 	
 	public void OnEditPhotos (View View)
 	{
 		String uri = Long.toString(mRecipeUri);
 		Intent intent = new Intent(this, EditPhotos.class);
 		intent.putExtra("extra_uri", uri);
 		logger.log(Level.INFO, "Uri passed FROM AddRecipesActivity: " + uri);
 		startActivity(intent);
 	}
 	
 	/**
 	 * Responds to the "Save" Button. Saves the changes to the database
 	 * and returns to the My Recipes.
 	 * @param The View that is calling the method
 	 *
 	 */
 	
 	public void OnSaveChanges (View View)
 	{
 		//Gets the user's recipes
 		DbController DbC = DbController.getInstance(this, this);
 
 		EditText editText = (EditText) findViewById(R.id.editText1);
 		String Title= editText.getText().toString();
 
 		editText = (EditText) findViewById(R.id.editText3);
 		String Instructions = editText.getText().toString();
 
 		editText = (EditText) findViewById(R.id.editText2);
 		String Author = editText.getText().toString();
 
 		//validate input
 		if(Title.isEmpty()||Instructions.isEmpty()||Author.isEmpty()) {
 			//first darken the screen
 			ImageView darkenScreen = (ImageView) findViewById(R.id.darkenScreen);
 			LayoutParams darkenParams = darkenScreen.getLayoutParams();
 			darkenParams.height = 1000;
 			darkenParams.width = 1000;
 			darkenScreen.setLayoutParams(darkenParams);
 
 			//make the popup
 			LinearLayout layout = new LinearLayout(this);
 			LayoutInflater inflater = LayoutInflater.from(this);
 			popUp = new PopupWindow(inflater.inflate(R.layout.popup_add_recipe_incomplete, null, false),400,300,true);
 			popUp.showAtLocation(layout, Gravity.CENTER, 0, 0);
 			return;
 		}
 
 		//Still have to do ingredients and photos
 		User author=new User(Author);
 		mRecipe.setAuthor(author);
 		mRecipe.setTitle(Title);
 		mRecipe.setInstructions(Instructions);
		saved=true;
 		DbC.updateRecipe(mRecipe);
 		finish();
 	}
 
 	
 	/**
 	 * Responds to the "Ok" Button is pressed in the pop=up
 	 * Removes the pop-up after the recipe is saved
 	 * @param The View that is calling the method
 	 *
 	 */
 	
 	public void OnOK(View v){
 		//remove the darkScreen
 		ImageView darkenScreen = (ImageView) findViewById(R.id.darkenScreen);
 		LayoutParams darkenParams = darkenScreen.getLayoutParams();
 		darkenParams.height = 0;
 		darkenParams.width = 0;
 		darkenScreen.setLayoutParams(darkenParams);
 
 		popUp.dismiss();
 	}
 	
 	
 	/**
 	 * When the activity ends, ensures the view is deleted to not cause errors
 	 *
 	 */
 	
 	public void onDestroy()
 	{	super.onDestroy();
	if(!saved)
	{DbController DbC = DbController.getInstance(this, this);
	DbC.deleteRecipe(mRecipe);	
	}
 	DbController DbC = DbController.getInstance(this, this);
 	DbC.deleteView(this);
 	}
 
	
 	@Override
 	public void update(DbManager model)
	{	
 	}
 
 }
