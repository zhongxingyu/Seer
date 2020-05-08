 package ch.unibe.ese.shopnote.activities;
 
 import java.math.BigDecimal;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.widget.AutoCompleteTextView;
 import android.widget.Toast;
 import ch.unibe.ese.shopnote.R;
 import ch.unibe.ese.shopnote.adapters.ItemAutocompleteAdapter;
 import ch.unibe.ese.shopnote.adapters.ShopAutocompleteAdapter;
 import ch.unibe.ese.shopnote.core.BaseActivity;
 import ch.unibe.ese.shopnote.core.Item;
 import ch.unibe.ese.shopnote.core.ListManager;
 import ch.unibe.ese.shopnote.core.Recipe;
 import ch.unibe.ese.shopnote.core.Shop;
 import ch.unibe.ese.shopnote.core.ShoppingList;
 import ch.unibe.ese.shopnote.share.requests.listchange.ItemRequest;
 
 /**
  * 	Creates a frame to create new items or edit them if the intent has an extra
  */
 public class CreateItemActivity extends BaseActivity {
 
 	private ListManager manager;
 	private ShoppingList list;
 	private Recipe recipe;
 	private Item item;
 	private boolean editItem;
 	private boolean itemListView;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_create_item);
 		// Show the Up button in the action bar.
 		getActionBar().hide();
 		manager = getListManager();
 
 		setTextViews();
 		openKeyboard();
 	}
 
 	/**
 	 * Set the values of the textview fields if editing an item
 	 */
 	private void setTextViews() {
 		// Set autocompletion adapter for shop
 		AutoCompleteTextView textShop = (AutoCompleteTextView) findViewById(R.id.editTextShop);
 		ShopAutocompleteAdapter sqliteShopAdapter = new ShopAutocompleteAdapter(this,
 				android.R.layout.simple_list_item_1);
 		textShop.setAdapter(sqliteShopAdapter);
 		
 		Bundle extras = getIntent().getExtras();
		itemListView = extras.getBoolean(BaseActivity.EXTRAS_ITEM_EDIT); // to edit itemlistview
 		String name = "";
 
 		if (extras != null) {
 			// Create new item from ViewListActivity
 			name = extras.getString(EXTRAS_ITEM_NAME);
 			if ((name != null) && !name.equals("")) {
 				setTextViewText(R.id.editTextName, name);
 				// move focus to next field if name is set
 				textShop.requestFocus();
 				openKeyboard();
 			}
 			long listIndex = extras.getLong(EXTRAS_LIST_ID);
 			if (listIndex != 0)
 				list = manager.getShoppingList(listIndex);
 			long recipeIndex = extras.getLong(EXTRAS_RECIPE_ID);
 			if (recipeIndex != 0)
 				recipe = manager.getRecipe(recipeIndex);
 
 			// prepare for edit item when necessary
 			if (extras.getBoolean(EXTRAS_ITEM_EDIT)) {
 				long itemId = extras.getLong(EXTRAS_ITEM_ID);
 				editItem = true;
 				for (Item it : (list == null ? manager.getAllItems() : manager
 						.getItemsFor(list))) {
 					if (it.getId() == itemId) {
 						item = it;
 						break;
 					}
 				}
 				
 				setTextViewText(R.id.textViewTitle, this.getString(R.string.edit_item_title));
 				editItem();
 			}
 		}
 
 		// Autocomplete only if adding a new item
 		if (!editItem) {
 			// create autocomplete adapter for name
 			AutoCompleteTextView textName = (AutoCompleteTextView) findViewById(R.id.editTextName);
 			ItemAutocompleteAdapter sqliteItemAdapter = new ItemAutocompleteAdapter(this,
 					android.R.layout.simple_list_item_1, manager);
 			textName.setAdapter(sqliteItemAdapter);
 			textName.setText(name);
 		}
 		
 		// Set autocompletion adapter for shop
 		textShop = (AutoCompleteTextView) findViewById(R.id.editTextShop);
 		sqliteShopAdapter = new ShopAutocompleteAdapter(this,
 				android.R.layout.simple_list_item_1);
 		textShop.setAdapter(sqliteShopAdapter);
 	}
 
 	/**
 	 * enter item data in edittext fields if editing an item
 	 */
 	private void editItem() {
 		// set name
 		setTextViewText(R.id.editTextName, item.getName());
 
 		if (item.getShop() != null)
 			setTextViewText(R.id.editTextShop, item.getShop().toString());
 		
 		if (item.getPrice() != null)
 			setTextViewText(R.id.editTextPrice, item.getPrice().toString());
 
 		setTextViewText(R.id.editTextQuantity, item.getQuantity());
		
		// if ListView mode, make last 3 input uneditable as a temporary fix
		if(itemListView){
			findViewById(R.id.editTextShop).setEnabled(false);
			setTextViewText(R.id.editTextShop, "Not editable in this view");
			findViewById(R.id.editTextPrice).setEnabled(false);
			setTextViewText(R.id.editTextPrice, "Not editable in this view");
			findViewById(R.id.editTextQuantity).setEnabled(false);
			setTextViewText(R.id.editTextQuantity, "Not editable in this view");
		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.create_item, menu);
 		return true;
 	}
 
 	/** Called when the user touches the abort button */
 	public void goBack(View view) {
 		super.finish();
 	}
 
 	/** Called when the user touches the save button */
 	public void saveItem(View view) {
 		// get name and change it if necessary
 		String name = getTextViewText(R.id.editTextName);
 		if (name == null || name.trim().isEmpty()) {
 			Toast.makeText(this, this.getString(R.string.error_name),
 					Toast.LENGTH_SHORT).show();
 			return;
 		}
 
 		if (item == null)
 			item = new Item(name);
 		else
 			item.setName(name);
 
 		// get shop and set it if necessary
 		String shopName = getTextViewText(R.id.editTextShop);
 		if (!shopName.isEmpty()) {
 			Shop shop = new Shop(shopName);
 			item.setShop(shop);
 		}
 
 		// get price and set it if necessary
 		String priceString = getTextViewText(R.id.editTextPrice);
 		if (!priceString.isEmpty()) {
 			BigDecimal price = new BigDecimal(priceString);
 			item.setPrice(price);
 		}
 		
 		String quantity = getTextViewText(R.id.editTextQuantity);
 		item.setQuantity(quantity);
 
 		// save the item
 		if (list != null) {
 			manager.addItemToList(item, list);
 		}
 		else if (recipe != null) {
 			recipe.addItem(item);
 			manager.save(item);
 		}
 		else {
 			// save item if called in itemlist
 			manager.save(item);
 		}
 
 		// go back to the list
 		finish();
 	}
 
 	public void finish() {
 		Intent data = new Intent();
 		if (item != null)
 			data.putExtra(EXTRAS_ITEM_ID, item.getId());
 		setResult(Activity.RESULT_OK, data);
 		super.finish();
 	}
 }
