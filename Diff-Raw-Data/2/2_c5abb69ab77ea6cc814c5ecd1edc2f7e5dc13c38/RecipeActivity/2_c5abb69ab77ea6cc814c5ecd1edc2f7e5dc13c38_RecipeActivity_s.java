 package cs169.project.thepantry;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.DialogFragment;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.WindowManager;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.FrameLayout;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.loopj.android.image.SmartImageView;
 
 import cs169.project.thepantry.ThePantryContract.ShoppingList;
 
 public class RecipeActivity extends BasicMenuActivity {
 	
 	Recipe recipe;
 	String type;
 	
 	SmartImageView picture;
 	TextView name;
 	ImageButton star;
 	ImageButton check;
 	LinearLayout ll;
 	LinearLayout ings;
 	static ArrayList<CheckBox> ingChecks;
 	
 	boolean faved;
 	boolean cooked;
 	
 	static DatabaseModel dm;
 	
 	private static final String DATABASE_NAME = "thepantry";
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_recipe);
 		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
 		
 		//Get bundle with recipe information.
 		//Intent i = this.getIntent();
 		recipe = (Recipe)getIntent().getExtras().getSerializable("result");
 		type = getIntent().getExtras().getString("type");
 		
 		//Display recipe picture if there is one.
 		picture = (SmartImageView)findViewById(R.id.recipePic);
 		if (recipe.images != null && recipe.images.hostedLargeUrl != null && isOnline()) {
 			picture.setImageUrl(recipe.images.hostedLargeUrl);
 			picture.setScaleType(ImageView.ScaleType.CENTER_CROP);
 		}
 		
 		//Display recipe name.
 		name = (TextView)findViewById(R.id.recipeName);
 		name.setText(recipe.name);
 		
 		//Render the ingredients list to view.
 		ingChecks = new ArrayList<CheckBox>();
 		ings = (LinearLayout)findViewById(R.id.ingList);
 		displayIngreds(recipe.ingredientLines);
 		
 		//Render directions to view.
 		//fetch and parse directions aynchronously
 		dm = new DatabaseModel(this, DATABASE_NAME);
		if (dm.findItem(ThePantryContract.Recipe.TABLE_NAME, recipe.id) || dm.findItem(ThePantryContract.CookBook.TABLE_NAME, recipe.id)) {
 			displayDirections(recipe.directionLines);
 		} else {
 			new ParseDirectionsTask().execute(recipe.source.sourceRecipeUrl);
 		}
 
 		Button source = (Button)findViewById(R.id.source);
 		
 		star = (ImageButton)findViewById(R.id.favorite);
 		check = (ImageButton)findViewById(R.id.cooked);
 		
 		//display the source and link to the web page source, open in a webview inside the app if clicked
 		if (type.equals("cookbook")) {
 			source.setVisibility(View.GONE);
 			star.setVisibility(View.GONE);
 			check.setVisibility(View.GONE);
 		} else {
 			if (recipe.source != null) {
 				source.setText("Source: " + recipe.source.sourceDisplayName);
 				source.setOnClickListener(new OnClickListener() {
 			        @Override
 			        public void onClick(View view) {
 			            displayWebpage(recipe.source.sourceRecipeUrl);
 			        }
 			    });
 			}
 			dm = new DatabaseModel(this, DATABASE_NAME);
 			// check if recipe is in database and get favorite and cooked values true or false
 			faved = dm.isItemChecked(ThePantryContract.Recipe.TABLE_NAME, recipe.id, ThePantryContract.Recipe.FAVORITE);
 			cooked = dm.isItemChecked(ThePantryContract.Recipe.TABLE_NAME, recipe.id, ThePantryContract.Recipe.COOKED);
 			
 			//set favorite button to grayscale or colored image based on state in db
 			//check if recipe in database or if not favorited
 			setStarButton(faved);
 			
 			//set cooked button to grayscale or colored image based on state in db
 			//check if recipe is in db or not cooked
 			setCheckButton(cooked);
 			dm.close();
 		}
 		
 	}
 	
 	// show all the ingredients in the ings layout
 	public void displayIngreds(List<String> ingreds) {
 		for (String ingred : ingreds) {
 			CheckBox tv = new CheckBox(this);
 			tv.setText(ingred);
 			ings.addView(tv);
 			ingChecks.add(tv);
 		}
 	}
 	
 	// return a string of all checked items
 	public static String getCheckedIngredientsString() {
 		String message = "";
 		for (CheckBox cb : ingChecks) {
 			String ingred = (String) cb.getText();
 			if (cb.isChecked()) {
 				// TODO: parse amount and item, check for ingredient and previous amount
 				String[] parsed = IngredientParser.parse(ingred);
 				String amt = parsed[0] + " " + parsed[1];
 				String ingred_name = parsed[3];
 				message += "Ingredient: " + ingred_name + ", Amount: " + amt + "\n";
 			}
 		}
 		return message;
 	}
 	
 	// open the ingredient adding dialog
 	public void openIngDialog(View v) {
 		AddIngredientsDialogFragment dialog = new AddIngredientsDialogFragment();
 		dialog.context = this;
 		dialog.show(getFragmentManager(), "dialog");
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getSupportMenuInflater().inflate(R.menu.recipe, menu);
 		return true;
 	}
 	
 	/**
 	 * Set favorite button to correct image either grayscale for not favorited
 	 * or color for favorited
 	 */
 	public void setStarButton(boolean faveStatus) {
 		if (faveStatus) {
 			star.setBackgroundResource(R.drawable.star_on);
 		} else {
 			star.setBackgroundResource(R.drawable.star_off);
 		}
 	}
 	
 	/**
 	 * Set cooked button to correct image either grayscale for not cooked
 	 * or color for cooked
 	 */
 	public void setCheckButton(boolean cookStatus) {
 		if (cookStatus) {
 			check.setBackgroundResource(R.drawable.check_on);
 		} else {
 			check.setBackgroundResource(R.drawable.check_off);
 		}
 	}
 	
 	/** 
 	 * saves this recipe to the favorites list.
 	 * TODO -=--need to add in refreshing the favs fragment adapter
 	*/
 	public void toggleFavorites(View v) {
 		// update recipe table of database so favorited column is yes/no
 		Toast toast;
 		dm = new DatabaseModel(this, DATABASE_NAME);
 		// need to add recipe to database if not already in it
 		dm.addStorage(ThePantryContract.Recipe.TABLE_NAME, recipe);
 		faved = !faved;
 		dm.check(ThePantryContract.Recipe.TABLE_NAME, recipe.id, ThePantryContract.Recipe.FAVORITE, faved);
 		if (faved) {
 			toast = Toast.makeText(this, "This recipe has been added to your favorites!", Toast.LENGTH_SHORT);
 		} else {
 			toast = Toast.makeText(this, "This recipe has been removed from your favorites!", Toast.LENGTH_SHORT);
 		}
 		toast.show();
 		setStarButton(faved);
 		dm.close();
 	}
 	
 	/**
 	 * marks this recipe as having been cooked before and updates inventory
 	 * according to ingredients list.
 	 * TODO -=--need to add in refreshing the cooked fragment adapter
 	 */
 	public void toggleCooked(View v) {
 		// update recipe table of database so cooked column is true
 		Toast toast;
 		dm = new DatabaseModel(this, DATABASE_NAME);
 		// need to add recipe if not already in database
 		dm.addStorage(ThePantryContract.Recipe.TABLE_NAME, recipe);
 		cooked = !cooked;
 		dm.check(ThePantryContract.Recipe.TABLE_NAME, recipe.id, ThePantryContract.Recipe.COOKED, cooked);
 		if (cooked) {
 			toast = Toast.makeText(this, "You have cooked this recipe!", Toast.LENGTH_SHORT);
 		} else {
 			toast = Toast.makeText(this, "You haven't cooked this recipe before!", Toast.LENGTH_SHORT);
 		}
 		toast.show();
 		setCheckButton(cooked);
 		dm.close();
 	}
 	
 	/**
 	 * checks inventory for ingredients and adds missing ingredients to
 	 * shopping list
 	 */
 	public static void addToShopping(Context context) {
 		// for each ingredient in list
 		for (CheckBox cb : ingChecks) {
 			String ingred = (String) cb.getText();
 			if (cb.isChecked()) {
 				// TODO: parse amount and item, check for ingredient and previous amount
 				String[] parsed = IngredientParser.parse(ingred);
 				String amt = parsed[0] + " " + parsed[1];
 				String ingred_name = parsed[3];
 				dm = new DatabaseModel(context, DATABASE_NAME);	
 				dm.addIngredient(ShoppingList.TABLE_NAME, ingred_name, "Other", amt);
 				dm.close();
 			}
 		}
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			finish();
 			return true;
 		}	
 		return super.onOptionsItemSelected(item);
 	}
 	
 	// display webpage activity with the url
 	public void displayWebpage(String url) {
 		Intent intent = new Intent(getApplicationContext(), DisplayWebpageActivity.class);
 		intent.putExtra("url", url);
 		startActivity(intent);
 	}
 	
 	public void displayDirections(ArrayList<String> directionsList) {	
 		if (directionsList != null && directionsList.size() > 0 && !directionsList.get(0).equals("")){
 			for (int i = 0; i < directionsList.size(); i++) {
 				LinearLayout directionsll = (LinearLayout)findViewById(R.id.dirList);
 				LayoutInflater inflater = (LayoutInflater)getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 				View thisDirection = inflater.inflate(R.layout.direction, null);
 				TextView number = (TextView)thisDirection.findViewById(R.id.number);
 				number.setText(i+1+"");				
 				
 				TextView directions = (TextView)thisDirection.findViewById(R.id.direction);
 				directions.setText(directionsList.get(i));
 				
 				directionsll.addView(thisDirection);
 			}
 		} else {
 			LinearLayout directionsll = (LinearLayout)findViewById(R.id.dirList);
 			LayoutInflater inflater = (LayoutInflater)getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			View thisDirection = inflater.inflate(R.layout.direction, null);
 			TextView directions = (TextView)thisDirection.findViewById(R.id.direction); // Change layout for this text
 			directions.setText("Directions currently unavailable");
 			directionsll.addView(thisDirection);
 		}
 		
 	}
 	
 	/* Class for asynchronously retrieving directions
 	 * calls DirectionParser on the recipe url
 	 */
 	public class ParseDirectionsTask extends AsyncTask<String, Void, ArrayList<String>> {
 		
 		FrameLayout mFrameOverlay;
 		
 		@Override
 	    protected void onPreExecute() {
 			mFrameOverlay = (FrameLayout)findViewById(R.id.overlay);
 			mFrameOverlay.setVisibility(View.VISIBLE);
 	    };
 		
 		@Override
 		protected ArrayList<String> doInBackground(String... url) {
 			return DirectionParser.getDirections(url[0]);
 		}
 		
 		@Override
 		protected void onPostExecute(ArrayList<String> directionsList) {
 			
 			//store these directions in the recipe database
 			//also store the recipe
 			dm = new DatabaseModel(RecipeActivity.this, DATABASE_NAME);
 			recipe.directionLines = directionsList;
 			boolean success;
 			if (dm.findItem(ThePantryContract.Recipe.TABLE_NAME, recipe.id)){
 				success = dm.setDirections(ThePantryContract.Recipe.TABLE_NAME, recipe.id, directionsList);
 			}else {
 				success = dm.addStorage(ThePantryContract.Recipe.TABLE_NAME, recipe);
 			}
 			// Do something with success?
 			dm.close();
 			
 			mFrameOverlay.setVisibility(View.GONE);
 			
 			displayDirections(directionsList);
 		}
 		
 	}
 	
 	/* Class for displaying popup dialog for adding ingredients
 	 * 
 	 */
 	public static class AddIngredientsDialogFragment extends DialogFragment {
 		
 		Context context;
 		
 	    @Override
 	    public Dialog onCreateDialog(Bundle savedInstanceState) {
 	        // Use the Builder class for convenient dialog construction
 	        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 	        builder.setTitle(R.string.dialog_add_ingredients_to_shopping_list)
 	        	   .setMessage(getCheckedIngredientsString()) // TODO: change to editable view
 	               .setPositiveButton(R.string.dialog_add_and_return, new DialogInterface.OnClickListener() {
 	                   public void onClick(DialogInterface dialog, int id) {
 	                       addToShopping(context);
 	                   }
 	               })
 	               .setNeutralButton(R.string.dialog_go_to_shopping, new DialogInterface.OnClickListener() {
 	                   public void onClick(DialogInterface dialog, int id) {
 	                	    addToShopping(context);
 	               			Intent intent = new Intent(getActivity(), ShoppingListActivity.class);
 	               			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 	               			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 	               			startActivity(intent);
 	                   }
 	               })
 	               .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
 	                   public void onClick(DialogInterface dialog, int id) {
 	                       // User cancelled the dialog
 	                   }
 	               });
 	        // Create the AlertDialog object and return it
 	        return builder.create();
 	    }
 	}
 }
