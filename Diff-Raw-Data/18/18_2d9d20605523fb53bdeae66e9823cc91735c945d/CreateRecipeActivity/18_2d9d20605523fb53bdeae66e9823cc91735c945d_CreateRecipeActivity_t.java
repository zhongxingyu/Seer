 package ca.ualberta.cs.oneclick_cookbook;
 
 import java.io.File;
 import java.io.IOException;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.drawable.Drawable;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.provider.MediaStore;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.ImageView;
 
 /**
  * Class that acts as the controller for the create recipe screen If the current
  * recipe variable in GlobalApplication is set, does not create a new recipe,
  * rather edits the current one. Is responsible for unsetting the current recipe
  * for future instances to work correctly.
  * 
  * @author Kenneth Armstrong
  * 
  */
 public class CreateRecipeActivity extends Activity {
 
 	private int position = -1;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_create_recipe);
 		Bundle extras = getIntent().getExtras();
 		if (extras != null) {
 			position = extras.getInt("position");
 		}
 	}
 
 	// Called when the app resumes
 	public void onResume() {
 		super.onResume();
 		GlobalApplication app = (GlobalApplication) getApplication();
 		Recipe recipe = app.getCurrentRecipe();
 		setInfo(recipe); // Set the info they entered before leaving
 	}
 
 	/**
 	 * Overides the back key action. Kills the current activity, as normal, but
 	 * also sets the current recipe to null so that future recipes work
 	 * properly.
 	 */
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		super.onKeyDown(keyCode, event);
 		if (keyCode == KeyEvent.KEYCODE_BACK) {
 			GlobalApplication app = (GlobalApplication) getApplication();
 			app.setCurrentRecipe(null);
 			finish();
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Function that takes the info of the recipe being edited and puts in into
 	 * the text fields. This is used to restore any info the use may have had
 	 * prior to leaving the activity.
 	 * 
 	 * @param recipe
 	 *            The recipe to set the info for
 	 */
 	public void setInfo(Recipe recipe) {
 		EditText name = (EditText) findViewById(R.id.createEnterName);
 		EditText steps = (EditText) findViewById(R.id.createEnterSteps);
 
 		name.setText(recipe.getName());
 		steps.setText(recipe.getSteps());
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.create_recipe, menu);
 		return true;
 	}
 
 	/**
 	 * Function that handles the clicks from a user. Calls the appropriate
 	 * function depending on the buton the user clicked.
 	 * 
 	 * @param v
 	 *            The view of the button that was clicked
 	 */
 	public void clickHandler(View v) {
 		switch (v.getId()) {
 		case R.id.bCreateDone:
 			onDone();
 			break;
 		case R.id.bCreateDelete:
 			onDelete();
 			break;
 		case R.id.bCreateAddIngredient:
 			onAddIngredients();
 			break;
 		case R.id.bCreateAddPhoto:
 			onAddPhoto();
 			break;
 		case R.id.bCreateRemovePhoto:
 			onRemovePhoto();
 			break;
 		}
 	}
 
 	/**
 	 * Function that is called when the user clicks on the add ingredients
 	 * button.
 	 */
 	public void onAddIngredients() {
 		EditText name = (EditText) findViewById(R.id.createEnterName);
 		EditText steps = (EditText) findViewById(R.id.createEnterSteps);
 		GlobalApplication app = (GlobalApplication) getApplication();
		
 		// Do this to save any user text they may have
		app.getCurrentRecipe().changeName(name.getText().toString());
		app.getCurrentRecipe().changeSteps(steps.getText().toString());
		
 		Intent intent = new Intent(app, AddIngredientsActivity.class);
 		startActivity(intent);
 	}
 
 	/**
 	 * Function that is called when the user clicks on the done button of the
 	 * recipe. Exits the create recipe activity.
 	 */
 	public void onDone() {
 		EditText name;
 		EditText steps;
 
 		name = (EditText) findViewById(R.id.createEnterName);
 		steps = (EditText) findViewById(R.id.createEnterSteps);
 		String namestring = name.getText().toString();
 		String stepstring = steps.getText().toString();
 		GlobalApplication app = (GlobalApplication) getApplication();
 		
 		Recipe r = null;
 		
 		// Check whether or not we are editing
 		if (position == -1) {
 			r = new Recipe(namestring, app.getCurrentRecipe()
 					.getIngredients(), stepstring);
 		}
 		else {
 			// If we are editing, do this
 			r = app.getCurrentRecipe();
 			r.changeName(namestring);
 			r.changeSteps(stepstring);
 		}
 
 		// TODO Add appropriate feedback here
 		if (r.isValidInfo() != Constants.GOOD) {
 			return;
 		}
 		
 		 NetworkHandler nh = new NetworkHandler(); 
 		 
 		 // Attempt posting to ES
 		 try {
 			 nh.postToES(r); 
 		 } catch (IllegalStateException e) { 
 			 e.printStackTrace(); 
 		 } catch (IOException e) { 
 			 e.printStackTrace();
 		 }
 
		// If it's a new recipe, add it to the user recipe list
		if (position == -1) {
			app.getCurrentUser().addRecipe(r.getID());
 		}
		
 
 		// Set null so future recipes start fresh
 		app.setCurrentRecipe(null);
 		finish();
 		return;
 	}
 
 	/**
 	 * Function that is called when the user clicks on the delete button of the
 	 * recipe.
 	 */
 	public void onDelete() {
 		// If the user is not editing, just delete
 		if (position == -1) {
 			GlobalApplication app = (GlobalApplication) getApplication();
 			app.setCurrentRecipe(null);
 			finish();
 			return;
 		}
 		// Builds the alert dialog box
 		AlertDialog.Builder prompt = new AlertDialog.Builder(this);
 		prompt.setTitle("Delete All");
 		prompt.setMessage("Are you sure you want to delete this recipe? It "
 				+ "will be gone... forever.");
 
 		prompt.setNegativeButton("No", new DialogInterface.OnClickListener() {
 
 			// User has changed their mind
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				return;
 			}
 		});
 		prompt.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 
 			// User does want to delete this recipe
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				GlobalApplication app = (GlobalApplication) getApplication();
 				NetworkHandler nh = new NetworkHandler();
 				
 				try {
 					nh.deleteRecipe(app.getCurrentRecipe().getID());
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 				
 				app.getCurrentUser().getUserRecipes().remove(position);
 				
 				// Set null so future recipes start fresh
 				app.setCurrentRecipe(null);
 				finish();
 			}
 		});
 		prompt.show();
 		return;
 	}
 
 	/**
 	 * Function that is called when the user clicks on the add photo button.
 	 */
 	public void onAddPhoto() {
 		return;
 	}
 
 	/**
 	 * Function that is called when the user clicks on the remove photo button.
 	 */
 	public void onRemovePhoto() {
 		return;
 	}
 }
