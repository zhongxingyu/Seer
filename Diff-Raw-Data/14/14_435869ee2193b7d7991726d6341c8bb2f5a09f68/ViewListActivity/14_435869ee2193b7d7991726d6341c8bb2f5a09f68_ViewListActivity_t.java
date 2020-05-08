 package ch.unibe.ese.shopnote.activities;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.NavUtils;
 import android.support.v4.widget.DrawerLayout;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.inputmethod.EditorInfo;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.AutoCompleteTextView;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.TextView.OnEditorActionListener;
 import android.widget.Toast;
 import ch.unibe.ese.shopnote.R;
 import ch.unibe.ese.shopnote.adapters.ItemListAdapter;
 import ch.unibe.ese.shopnote.core.BaseActivity;
 import ch.unibe.ese.shopnote.core.Comparators;
 import ch.unibe.ese.shopnote.core.Item;
 import ch.unibe.ese.shopnote.core.ItemRecipeAdapter;
 import ch.unibe.ese.shopnote.core.ListManager;
 import ch.unibe.ese.shopnote.core.Recipe;
 import ch.unibe.ese.shopnote.core.ShoppingList;
 import ch.unibe.ese.shopnote.core.sqlite.SQLiteItemAdapter;
 import ch.unibe.ese.shopnote.drawer.NavigationDrawer;
 import ch.unibe.ese.shopnote.share.SyncManager;
 
 public class ViewListActivity extends BaseActivity {
 
 	private ListManager manager;
 	private SyncManager syncmanager;
 	private ArrayAdapter<Item> itemAdapter;
 	private ArrayAdapter<Item> itemBoughtAdapter;
 	private SQLiteItemAdapter sqliteAdapter;
 	private ArrayList<Item>	itemsList;
 	private ArrayList<Item>	itemsBoughtList;
 	private Activity viewListActivity = this;
 	private ShoppingList list;
 	private DrawerLayout drawMenu;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_view_list);
 		// Show the Up button in the action bar.
 		setupActionBar();
 
 		manager = getListManager();
 		syncmanager = getSyncManager();
 
 		// Create drawer menu
 		NavigationDrawer nDrawer = new NavigationDrawer();
 		drawMenu = nDrawer.constructNavigationDrawer(drawMenu, this);
 
 		Bundle extras = getIntent().getExtras();
 		if (extras != null) {
 			// Get list
 			long listIndex = extras.getLong(EXTRAS_LIST_ID);
 			list = manager.getShoppingList(listIndex);
 			setTitle(list.getName());
 		}
 
 		updateAdapters();
 
 		// Autocompletion
 		AutoCompleteTextView itemName = (AutoCompleteTextView) findViewById(R.id.editTextName);
 		sqliteAdapter = new SQLiteItemAdapter(this, android.R.layout.simple_list_item_1, manager);
 		itemName.setAdapter(sqliteAdapter);
 		
 		itemName.setOnItemClickListener(new OnItemClickListener() {
 
 	        @Override
 	        public void onItemClick(AdapterView<?> parent, View arg1, int position,
 	                long id) {
 	        	ItemRecipeAdapter entry = (ItemRecipeAdapter) sqliteAdapter.getItem(position);
 	
 	        	if(entry.isItem()) {
 	        		Item item = manager.getItem(entry.getId());
 	        		manager.addItemToList(item, list);
 	        	} else {
 	        		Recipe recipe = manager.getRecipeAt(entry.getId());
 	        		for (Item item : recipe.getItemList()) {
 						manager.addItemToList(item, list);
 						itemAdapter.add(item);
 					}
 	        	}
 	        	updateAdapters();
 	        	
 	        	EditText addItem = (EditText) findViewById(R.id.editTextName);
 	        	addItem.setText("");
 	        }
 	    });
 
 		// Done button
 		itemName.setOnEditorActionListener(new OnEditorActionListener() {
 			@Override
 			public boolean onEditorAction(TextView view, int actionId,
 					KeyEvent event) {
 				if (actionId == EditorInfo.IME_ACTION_DONE) {
 					addItem(view);
 					return true;
 				}
 				return false;
 			}
 		});
 	}
 
 	/**
 	 * Updates the adapters
 	 */
 	public void updateAdapters() {
 		// Get list items
 		List<Item> items = manager.getItemsFor(list);
 		separateBoughtItems(items);
 
 		ListView listView = updateItemAdapter();
 		ListView listViewBought = updateItemBoughtAdapter();
 		addListeners(listView, listViewBought);
 		toggleShoppingCart();
 	}
 
 	private void separateBoughtItems(List<Item> items) {
 		itemsBoughtList = new ArrayList<Item>();
 		itemsList = new ArrayList<Item>();
 
 		for (Item item : items) {
 			if (item.isBought())
 				itemsBoughtList.add(item);
 			else
 				itemsList.add(item);
 		}
 	}
 
 	private ListView updateItemAdapter() {
 		itemAdapter = new ItemListAdapter(this, R.layout.shopping_list_item,
 				itemsList);
 		ListView listView = (ListView) findViewById(R.id.ItemView);
 		listView.setAdapter(itemAdapter);
 		return listView;
 	}
 
 	private ListView updateItemBoughtAdapter() {
 		ListView listViewBought = (ListView) findViewById(R.id.ItemBoughtView);
 		itemBoughtAdapter = new ItemListAdapter(viewListActivity,
 				R.layout.shopping_list_item, itemsBoughtList);
 		listViewBought.setAdapter(itemBoughtAdapter);
 		return listViewBought;
 	}
 
 	// TODO: rewrite this method-code, its not that beautiful...
 	private void addListeners(ListView listView, ListView listViewBought) {
 		// Add long click Listener
 		listView.setOnItemLongClickListener(new OnItemLongClickListener() {
 
 			@Override
 			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
 					int arg2, long arg3) {
 				Item selectedItem = itemAdapter.getItem(arg2);
 				ViewListActivity.this
 						.startActionMode(new ShoppingListActionMode(
 								ViewListActivity.this.manager, selectedItem,
 								list, ViewListActivity.this.itemAdapter,
 								ViewListActivity.this));
 				return true;
 			}
 		});
 
 		// Add click Listener
 		listView.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view,
 					int position, long id) {
 				// bought items
 				Item item = itemAdapter.getItem(position);
 				item.setBought(true);
 				manager.addItemToList(item, list);
 
 				itemAdapter.remove(item);
 				itemBoughtAdapter.add(item);
 				toggleShoppingCart();
 			}
 		});
 
 		// Add long click Listener to bought items
 		listViewBought
 				.setOnItemLongClickListener(new OnItemLongClickListener() {
 
 					@Override
 					public boolean onItemLongClick(AdapterView<?> parent,
 							View view, int position, long id) {
 						Item selectedItem = itemBoughtAdapter.getItem(position);
 						ViewListActivity.this
 								.startActionMode(new ShoppingListActionMode(
 										ViewListActivity.this.manager,
 										selectedItem, list,
 										ViewListActivity.this.itemAdapter,
 										ViewListActivity.this));
 						return true;
 					}
 				});
 
 		// Add click Listener to bought items
 		listViewBought.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view,
 					int position, long id) {
 				// bought items
 				Item item = itemBoughtAdapter.getItem(position);
 				item.setBought(false);
 				manager.addItemToList(item, list);
 				itemBoughtAdapter.remove(item);
 				itemAdapter.add(item);
 				toggleShoppingCart();
 			}
 		});
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
 		intent.putExtra(EXTRAS_LIST_ID, list.getId());
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
 			Toast.makeText(this, this.getString(R.string.error_name),
 					Toast.LENGTH_SHORT).show();
 			return;
 		} else if (name.charAt(0) == '/') {
 			List<Recipe> recipeList = manager.getRecipes();
 			String recipeName = name.toLowerCase();
 
 			for (Recipe recipe : recipeList) {
 				if (recipe.getName().toLowerCase().equals(recipeName)) {
 					for (Item item : recipe.getItemList()) {
						item.setBought(false);
 						manager.addItemToList(item, list);
 						itemAdapter.add(item);
 					}
 				}
 			}
 
 		} else {
 			Item item = new Item(name);
 			boolean added = manager.addItemToList(item, list);
 
 			if (added) {	// refresh view
 				itemAdapter.add(item);
 				itemAdapter.sort(Comparators.ITEM_COMPARATOR);
 			}
 		}
 
 		// remove text from field
 		textName = (AutoCompleteTextView) findViewById(R.id.editTextName);
 		textName.setText("");
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.view_list, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		long listIndex = list.getId();
 
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
 
 			// Handle presses on the action bar items
 		case R.id.action_share:
 			Intent intentShare = new Intent(this, ShareListActivity.class);
 			intentShare.putExtra(EXTRAS_LIST_ID, listIndex);
 			this.startActivity(intentShare);
 			return true;
 
 			// Handle presses on overflow menu items
 		case R.id.action_edit_list:
 			Intent intentEdit = new Intent(this, CreateListActivity.class);
 			intentEdit.putExtra(EXTRAS_LIST_ID, listIndex);
 			this.startActivity(intentEdit);
 			return true;
 		case R.id.action_archive:
 			// toggle archive state
 			list.setArchived(!list.isArchived());
 			manager.saveShoppingList(list);
 			NavUtils.navigateUpFromSameTask(this);
 			return true;
 		case R.id.action_delete:
 			manager.removeShoppingList(list);
 			NavUtils.navigateUpFromSameTask(this);
 			return true;
 		case R.id.action_refresh:
 			syncmanager.synchronise(this);
 			return true;
 		}
 
 		return super.onOptionsItemSelected(item);
 	}
 
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (requestCode == 1 && resultCode == RESULT_OK)
 			updateAdapters();
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		drawMenu.closeDrawers();
 	}
 
 	private void toggleShoppingCart() {
 		// hide shopping cart image if no items bought
 		ImageView imageView = (ImageView) findViewById(R.id.imageItemsBought);
 		TextView textView = (TextView) findViewById(R.id.textItemsBought);
 		if (!itemBoughtAdapter.isEmpty()) {
 			imageView.setVisibility(View.VISIBLE);
 			textView.setVisibility(View.VISIBLE);
 		} else {
 			imageView.setVisibility(View.INVISIBLE);
 			textView.setVisibility(View.INVISIBLE);
 		}
 	}
 
 }
