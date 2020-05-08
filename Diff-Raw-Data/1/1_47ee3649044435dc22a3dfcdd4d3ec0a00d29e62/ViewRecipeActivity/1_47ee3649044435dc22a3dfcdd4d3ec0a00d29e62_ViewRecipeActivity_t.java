 package ch.unibe.ese.shopnote.activities;
 
 import java.util.ArrayList;
 
 import ch.unibe.ese.shopnote.R;
 import ch.unibe.ese.shopnote.core.BaseActivity;
 import ch.unibe.ese.shopnote.core.Item;
 import ch.unibe.ese.shopnote.core.ListManager;
 import ch.unibe.ese.shopnote.core.Recipe;
 import ch.unibe.ese.shopnote.drawer.NavigationDrawer;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.inputmethod.EditorInfo;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.AutoCompleteTextView;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.TextView.OnEditorActionListener;
 import android.support.v4.app.NavUtils;
 import android.support.v4.widget.DrawerLayout;
 
 /**
  *	Displays a single recipe including the items
  */
 public class ViewRecipeActivity extends BaseActivity {
 	private ListManager manager;
 	private Recipe recipe;
 	private ArrayList<Item> itemsOfRecipe;
 	private ArrayAdapter<Item> itemAdapter;
 	ArrayAdapter<Item> autocompleteAdapter;
 	private DrawerLayout drawMenu;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_view_recipe);
 		// Show the Up button in the action bar.
 		setupActionBar();
 		
 		// Create drawer menu
 		NavigationDrawer nDrawer = new NavigationDrawer();
 		drawMenu = nDrawer.constructNavigationDrawer(drawMenu, this);
 		
 		manager = getListManager();
 		
 		Bundle extras = getIntent().getExtras();
 		if (extras != null) {
 			long recipeIndex = extras.getLong(EXTRAS_RECIPE_ID);
 			recipe = manager.getRecipe(recipeIndex);
 			itemsOfRecipe = recipe.getItemList();
 			setTitle(this.getString(R.string.view_recipe_title) + " " + recipe.toString());
 		} 
 		else
 			itemsOfRecipe = new ArrayList<Item>();
 		
 		//create or update recipe list
 		updateRecipeList();
 		
 		//add autocomplete items
 		createAutocomplete();
 		
 		// Done button
 		EditText addItem = (EditText) findViewById(R.id.editTextName);
 		addItem.setOnEditorActionListener(new OnEditorActionListener() {
 		    @Override
 		    public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
 		        if (actionId == EditorInfo.IME_ACTION_DONE) {
 		        	addItem(view);
 		            return true;
 		        }
 		        return false;
 		    }
 		});
 	}
 
 	private void updateRecipeList() {
 		// Get listOfRecipes and put it in the listview	
 		itemAdapter = new ArrayAdapter<Item>(this,
 				R.layout.shopping_list_item, itemsOfRecipe);
 		ListView listView = (ListView) findViewById(R.id.ItemView);
 		listView.setAdapter(itemAdapter);
		manager.saveRecipe(recipe);
 		
 		listView.setOnItemLongClickListener(new OnItemLongClickListener() {
 
 			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
 					int position, long arg3) {
 				Item selectedItem = itemAdapter.getItem(position);
 				ViewRecipeActivity.this.startActionMode(new RecipeListActionMode(
 						ViewRecipeActivity.this.manager, recipe, selectedItem,
 						ViewRecipeActivity.this.itemAdapter,
 						ViewRecipeActivity.this));
 				return true;
 			}
 		});
 	}
 	
 	private AutoCompleteTextView createAutocomplete() {
 		AutoCompleteTextView addItems = (AutoCompleteTextView) findViewById(R.id.editTextName);
 		autocompleteAdapter = new ArrayAdapter<Item>(this,
 				android.R.layout.simple_list_item_1, manager.getAllItems());
 		addItems.setAdapter(autocompleteAdapter);
 		
 		addItems.setOnItemClickListener(new OnItemClickListener() {
 
 	        @Override
 	        public void onItemClick(AdapterView<?> parent, View arg1, int position,
 	                long id) {
 	        	Item item = (Item) autocompleteAdapter.getItem(position);
 	        	if(!itemsOfRecipe.contains(item))
 	        		itemsOfRecipe.add(item);
 	        	updateRecipeList();
 	        	
 	        	EditText addItem = (EditText) findViewById(R.id.editTextName);
 	        	addItem.setText("");
 	        }
 	    });
 		
 		return addItems;
 	}
 
 	/**
 	 * Set up the {@link android.app.ActionBar}.
 	 */
 	private void setupActionBar() {
 
 		getActionBar().setDisplayHomeAsUpEnabled(true);
 
 	}
 	
 	/** Called when the user touches the add item button */
 	public void addItemEdit(View view) {
 		Intent intent = new Intent(this, CreateItemActivity.class);
 		EditText textName = (EditText) findViewById(R.id.editTextName);
 		String name = textName.getText().toString();
 		intent.putExtra(EXTRAS_ITEM_NAME, name);
 		intent.putExtra(EXTRAS_RECIPE_ID, recipe.getId());
 		this.startActivityForResult(intent, 1);
 		
 		// remove text from field
 		textName = (AutoCompleteTextView) findViewById(R.id.editTextName);
 		textName.setText("");
 	}
 
 	/** Called when the user touches the ok button */
 	public void addItem(View view) {
 		EditText textName = (EditText) findViewById(R.id.editTextName);
 		String name = textName.getText().toString();
 		if (name.trim().length() == 0) {
 			Toast.makeText(this, this.getString(R.string.error_name), Toast.LENGTH_SHORT).show();
 			return;				
 		} 
 		else {
 			Item item = new Item(name);
 			manager.save(item);
 			recipe.addItem(item);
 			updateRecipeList();
 		}
 
 		// remove text from field
 		textName = (AutoCompleteTextView) findViewById(R.id.editTextName);
 		textName.setText("");
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.view_recipe, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
     	long recipeIndex = recipe.getId();
     	
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			// This ID represents the Home or Up button. In the case of this
 			// activity, the Up button is shown. Use NavUtils to allow users
 			// to navigate up one level in the application structure. For
 			// more details, see the Navigation pattern on Android Design:
 			//
 			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
 			//
 			NavUtils.navigateUpFromSameTask(this);
 			return true;
 			
 		// Handle presses on overflow menu items
 		case R.id.action_edit_recipe:
         	Intent intentEdit = new Intent(this, CreateRecipeActivity.class);
         	intentEdit.putExtra(EXTRAS_RECIPE_ID, recipeIndex);
             this.startActivity(intentEdit);
 			return true;
 		case R.id.action_delete:
 			manager.removeRecipe(recipe);
 			NavUtils.navigateUpFromSameTask(this);
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 	
 	@Override
 	protected void onPause() {
 		super.onPause();
     	drawMenu.closeDrawers();
 	}
 
 }
