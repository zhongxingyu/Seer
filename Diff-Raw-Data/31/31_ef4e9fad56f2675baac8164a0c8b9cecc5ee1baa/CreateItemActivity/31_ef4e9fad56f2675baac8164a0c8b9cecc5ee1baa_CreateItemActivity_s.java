 package ch.unibe.ese.shoppinglist;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 import java.math.BigDecimal;
 
 import ch.unibe.ese.core.BaseActivity;
 import ch.unibe.ese.core.Item;
 import ch.unibe.ese.core.ListManager;
 import ch.unibe.ese.core.Shop;
 import ch.unibe.ese.core.ShoppingList;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class CreateItemActivity extends BaseActivity {
 	
 	private ListManager manager;
 	private ShoppingList list;
 	private Item item;
 	private int listIndex;
 	private TextView textViewTitle;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_create_item);
 		// Show the Up button in the action bar.
 		getActionBar().hide();
 		manager = getListManager();
 		
 		Bundle extras = getIntent().getExtras();
 		if (extras != null) {
 			// get item name
 			String name = extras.getString("Item");
 			EditText textName = (EditText) findViewById(R.id.editTextName);
 			textName.setText(name);
 			// get list
 			listIndex = extras.getInt("selectedList");
 			list = manager.getShoppingLists().get(listIndex);
 			// edit item
 			if (extras.getBoolean("editItem")) {
 				int itemIndex = extras.getInt("selectedItem");
 				List<Item> items = manager.getItemsFor(list);
 				item = items.get(itemIndex);
 				textViewTitle = (TextView) findViewById(R.id.textViewTitle);
 				textViewTitle.setText("Edit item:");
 				editItem();
 			}
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.create_item, menu);
 		return true;
 	}
 	
 	private void editItem() {
 		// set name
 		EditText textName = (EditText) findViewById(R.id.editTextName);
 		textName.setText(item.getName());
 
 		// set shop
 		if(item.getShop() != null) {
 		EditText textShop = (EditText) findViewById(R.id.editTextShop);
 		textShop.setText(item.getShop().toString());
 		}
 		
 		// TODO: set price
 		//EditText textDate = (EditText) findViewById(R.id.editTextDate);
 		//Date date = list.getDueDate();
 		
		// delete existing item and add as new one
		// TODO: fix position
		manager.removeItemFromList(item, list);
 	}
 	
 	/** Called when the user touches the abort button */
 	public void goBack(View view) {
 		finish();
 	}
 
 	/** Called when the user touches the save button */
 	public void saveItem(View view) {
 		// get name
 		EditText textName = (EditText) findViewById(R.id.editTextName);
 		String name = textName.getText().toString();
 		boolean check = true;
 		try {
 			if (item == null)
 				item = new Item(name);
 			else
 				item.setName(name);
 		} catch (IllegalArgumentException e) {
 			Toast.makeText(this, "Please enter a name", Toast.LENGTH_SHORT)
 					.show();
 			check = false;
 		}
 
 		if (check) {
 			// get shop
 			EditText textShop = (EditText) findViewById(R.id.editTextShop);
 			String shopName = textShop.getText().toString();
 			if (shopName.trim().length() != 0) {
 				Shop shop = new Shop(shopName);
 				item.setShop(shop);
 			}
 			
 			// get price
 			EditText textPrice = (EditText)findViewById(R.id.editTextPrice);
 			// TODO: save price
 			//BigDecimal price = textPrice.getText();
 
 			// save the item
 			try {
				manager.addItemToList(item, list);
 
 			} catch (IllegalStateException e) {
 				Toast.makeText(this, "Please enter a name", Toast.LENGTH_SHORT)
 						.show();
 			}	
 		
 		// go back to the list
 		Intent intent = new Intent(this, ViewListActivity.class);
 		intent.putExtra("selectedList", listIndex);
 		this.startActivity(intent);
 		}
 	}
 }
