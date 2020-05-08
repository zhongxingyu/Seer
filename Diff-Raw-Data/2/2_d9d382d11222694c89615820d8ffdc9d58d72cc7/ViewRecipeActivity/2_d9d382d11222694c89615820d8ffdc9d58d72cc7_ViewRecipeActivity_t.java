 /* ViewRecipeAcivity
  * 
  * Last Edited: March 7, 2013
  * 
  * 
  */
 
 package ca.ualberta.team2recipefinder.views;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 import java.util.concurrent.ExecutionException;
 
 import ca.ualberta.team2recipefinder.R;
 import ca.ualberta.team2recipefinder.R.id;
 import ca.ualberta.team2recipefinder.R.layout;
 import ca.ualberta.team2recipefinder.controller.Controller;
 import ca.ualberta.team2recipefinder.controller.RecipeFinderApplication;
 import ca.ualberta.team2recipefinder.controller.SearchResult;
 import ca.ualberta.team2recipefinder.model.Ingredient;
 import ca.ualberta.team2recipefinder.model.Recipe;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.drawable.Drawable;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Environment;
 import android.provider.MediaStore;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 
 /**
  * ViewRecipeActivity is an android activity for displaying information about
  * a single recipe.
  * 
  * @author cmput-301 team 2
  * @see ca.ualberta.team2recipefinder.model.Recipe
  */
 public class ViewRecipeActivity extends Activity implements ca.ualberta.team2recipefinder.views.View<Recipe> {
 
 	long recipeID = -1;
 	Recipe currentRecipe = new Recipe();
 	String serverID = "";
 	int imageIndex = 0;
 	
 	boolean isLocal;
 	int source = -1;
 	
 	private static final int EDIT_SERVER_RECIPE = 0;
 	
 	Controller c;
 	
 
 	/** 
 	 * Sets up all button listeners for this activity. Called when the activity is first created.
 	 * 
 	 * @param	savedInstanceState Bundle containing the activity's previously frozen state, if there was one. 
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_view_recipe);
 
 		c = RecipeFinderApplication.getController();
 		
 		Recipe localRecipe = null;
 
 		if (savedInstanceState == null) {
 			Bundle extras = getIntent().getExtras();
 			if (extras != null) {
 				source = extras.getInt("source");				
 				recipeID = extras.getLong("recipeID");
 				serverID = extras.getString("serverID");
 				localRecipe = c.getRecipe(recipeID);
 			}
 		}
 		
 
 		isLocal = localRecipe != null;
 		
 		if (source == SearchResult.SOURCE_LOCAL) {
 			currentRecipe = localRecipe;
 		}
 		
 		Button publishDownloadButton = (Button) findViewById(R.id.publish_download_button);
 		publishDownloadButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View view) {
 				// if local, then publish
 				if (source == SearchResult.SOURCE_LOCAL) {
 					AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
 						@Override
 						protected Void doInBackground(Void... arg0) {
 							try {
 								c.publishRecipe(currentRecipe);
 							} catch (IOException e) {
 								e.printStackTrace();								
 							}
 	
 							return null;
 						}
 					}.execute();					
 
 					try {
 						task.get();
 						// if successful, mark the recipe as on the server
 						currentRecipe.setOnServer(true);
 						c.replaceRecipe(currentRecipe, currentRecipe.getRecipeID());
 						update(currentRecipe);
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					} catch (ExecutionException e) {
 						e.printStackTrace();
 						Toast.makeText(ViewRecipeActivity.this, getString(R.string.no_connection), 
 							   Toast.LENGTH_LONG).show();
 					}
 				}
 				// otherwise, save it locally
 				else if (source == SearchResult.SOURCE_REMOTE) {
 					c.addRecipe(currentRecipe);
					isLocal = true;
					update(currentRecipe);
 				}
 			}		
 		});
 
 		Button shareButton = (Button) findViewById(R.id.share_button);
 		shareButton.setText("Share");
 		shareButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View view) {
 				Intent intent = new Intent(ViewRecipeActivity.this, ShareRecipeActivity.class);
 				intent.putExtra("recipeID", recipeID);
 				startActivity(intent);
 			}
 		});
 
 		Button editButton = (Button) findViewById(R.id.edit_button);
 		editButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View view) {
 				Intent intent = new Intent(ViewRecipeActivity.this, EditRecipeActivity.class);
 				intent.putExtra("source", source);
 				
 				if (source == SearchResult.SOURCE_LOCAL) {
 					intent.putExtra("recipeID", recipeID);
 					startActivity(intent);
 				}
 				else {
 					intent.putExtra("serverID", serverID);
 					startActivityForResult(intent, EDIT_SERVER_RECIPE);
 				}				
 			}
 		});
 		
 		Button deleteButton = (Button) findViewById(R.id.delete_button);
 		deleteButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View view) {
 				/* CALL DELETE FROM CONTROLLER, EXIT ACTIVITY */
 				c.deleteRecipe(currentRecipe);				
 				finish();
 			}
 		});
 
 		Button rightButton = (Button) findViewById(R.id.button_forward);
 		rightButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View view) {
 				if (imageIndex < (currentRecipe.getAllPhotos().size()-1)) {
 					imageIndex++;
 				}
 				update(currentRecipe);
 			}
 		});
 
 		Button leftButton = (Button) findViewById(R.id.button_back);
 		leftButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View view) {
 				if (imageIndex > 0) {
 					imageIndex--;
 				}
 				update(currentRecipe);
 			}
 		});
 
 
 		update(currentRecipe);
 		currentRecipe.addView(this);
 	}
 
 	/**
 	 * Updates the current info being displayed. Use if the recipe is changed.
 	 * 
 	 * @param model the model that called the method
 	 */
 	public void update(Recipe model) {
 		Button publishDownloadButton = (Button) findViewById(R.id.publish_download_button);
 		if (source == SearchResult.SOURCE_LOCAL) {
 			publishDownloadButton.setText("Publish");
 
 			// if already on server, user can not publish the recipe
 			if (!c.canPublish(currentRecipe) || currentRecipe.getOnServer()) {
 				publishDownloadButton.setEnabled(false);
 			}
 		}
 		else {
 			publishDownloadButton.setText("Download");
 			
 			// you cannot download a recipe that you downloaded already
 			if (isLocal) {
 				publishDownloadButton.setEnabled(false);
 			}
 
 			AsyncTask<Void, Void, Recipe> task = (new AsyncTask<Void, Void, Recipe>() {
 				@Override
 				protected Recipe doInBackground(Void... arg0) {
 					try {
 						return c.downloadRecipe(serverID);
 					} catch (IOException e) {
 						e.printStackTrace();
 					}
 
 					return null;
 				}
 			}).execute();
 
 			try {
 				currentRecipe = task.get();
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			} catch (ExecutionException e) {
 				e.printStackTrace();
 				Toast.makeText(ViewRecipeActivity.this, getString(R.string.no_connection), 
 					   Toast.LENGTH_LONG).show();
 				finish();
 			}
 		}
 		
 		Button editButton = (Button) findViewById(R.id.edit_button);
 		editButton.setText("Edit");
 
 		Button deleteButton = (Button) findViewById(R.id.delete_button);
 		deleteButton.setText("Delete");
 		if (source == SearchResult.SOURCE_LOCAL) {
 			deleteButton.setEnabled(true);
 		}
 		else {
 			deleteButton.setEnabled(false);
 		}
 		
 		TextView recipeName = (TextView) findViewById(R.id.recipe_name);
 		recipeName.setText(currentRecipe.getName());
 
 		TextView procedure = (TextView) findViewById(R.id.procedure_text);
 		String procedureText = currentRecipe.getProcedure();
 		procedure.setText(procedureText);
 
 		TextView ingredients = (TextView) findViewById(R.id.ingredients_text);
 		List<Ingredient> ingredientTextArray = currentRecipe.getIngredients();
 		
 		TextView comments = (TextView) findViewById(R.id.comment_text);
 		List<String> commentsTextArray = currentRecipe.getAllComments();
 
 		String ingredientText = new String();
 		String nl = System.getProperty("line.separator");
 		for (int i = 0; i < ingredientTextArray.size(); i++) {
 			ingredientText += ingredientTextArray.get(i).toString() + nl;
 		}
 		ingredients.setText(ingredientText);
 		
 		String commentsText = new String();
 		String cl = System.getProperty("line.separator");
 		for (int i = 0; i < commentsTextArray.size(); i++) {
 			commentsText += commentsTextArray.get(i) + cl;
 		}
 		comments.setText(commentsText);
 
 		ImageView pictureBox = (ImageView) findViewById(R.id.recipe_images);
 		Bitmap image = currentRecipe.getPhoto(imageIndex);
 		if (image != null) {
 			pictureBox.setImageBitmap(image);
 		}
 
 		TextView imageInfo = (TextView) findViewById(R.id.image_numbers);
 		String info;
 		if (currentRecipe.hasPhotos()) {
 			info = (imageIndex+1)+"/"+currentRecipe.getAllPhotos().size();
 		} else {
 			info = "No photos for this recipe";
 		}
 		imageInfo.setText(info);
 	}
 
 	/**
 	 * Called when the recipe is being edited remotely. Checks if the edit was okay
 	 * 
 	 * @param requestCode the ID of the request code
 	 * @param resultCode the ID of the result code
 	 */
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode,
             Intent data) {
         if (requestCode == EDIT_SERVER_RECIPE) {
             if (resultCode == RESULT_OK) {
     			update(currentRecipe);
             }
         }
 	}
 
 	/**
 	 * Removes this view from the model
 	 */
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		currentRecipe.removeView(this);
 	}
 }
