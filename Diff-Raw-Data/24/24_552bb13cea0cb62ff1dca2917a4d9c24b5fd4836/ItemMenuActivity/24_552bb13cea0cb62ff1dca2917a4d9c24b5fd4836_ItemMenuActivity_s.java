 package com.shawnhanna.shop;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.TextView;
 
 public class ItemMenuActivity extends ShopActivity {
 	static final String TAG = "ItemMenuActivity";
 
 	private ImageButton removebutton;
 	private Button findButton;
 	private Button aisleButton;
 	private Button backButton;
 	private ImageButton incrementButton;
 	private ImageButton decrementButton;
 	private TextView quantity;
 	private TextView totalPrice;
 	private DataService dataService;
 	private Item item;
 
 	// -----------------------------------------------------------------------------------------------------------------------------
 	// -- ONCREATE
 	// -----------------------------------------------------------------------------------------------------------------------------
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_item);
 
 		dataService = DataService.getInstance();
 
 		initializeViewItems();
 		setupMenuBarButtons(this);
 
 		if (!receiveItem()) {
 			AlertDialog alertDialog = new AlertDialog.Builder(
 					ItemMenuActivity.this).create();
 			alertDialog.setTitle("Error");
 			alertDialog.setMessage("ERROR: no item selected");
 			alertDialog.show();
 		} else {
 			initializeButtonListeners();
 		}
 	}
 
 	private boolean receiveItem() {
 		item = dataService.getSelectedItem();
 		if (item != null) {
 			TextView price = (TextView) findViewById(R.id.item_price);
 			TextView quantity = (TextView) findViewById(R.id.item_quantity);
 			TextView itemName = (TextView) findViewById(R.id.item_name);
 			price.setText("" + item.getPrice());
 			quantity.setText("" + item.getQuantity());
 			itemName.setText("" + item.getName());
 			totalPrice.setText(String.valueOf(item.getPrice()
 					* item.getQuantity()));
 			return true;
 		}
 		return false;
 	}
 
 	// -----------------------------------------------------------------------------------------------------------------------------
 	// -- UTILITY FUNCTIONS
 	// -----------------------------------------------------------------------------------------------------------------------------
 	private void initializeViewItems() {
 		setContentView(R.layout.activity_item);
 
 		removebutton = (ImageButton) findViewById(R.id.remove_button);
 		findButton = (Button) findViewById(R.id.find_button);
 		aisleButton = (Button) findViewById(R.id.aisle_button);
 		backButton = (Button) findViewById(R.id.back_button);
 		incrementButton = (ImageButton) findViewById(R.id.increment_quantity);
 		decrementButton = (ImageButton) findViewById(R.id.decrement_quantity);
 		quantity = (TextView) findViewById(R.id.item_quantity);
 		totalPrice = (TextView) findViewById(R.id.total_item_price);
 	}
 
 	private void initializeButtonListeners() {
 		removebutton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				dataService.removeFromCart(item);
 				// TODO: fix this desperate workaround and force refresh
 				Intent intent = new Intent(ItemMenuActivity.this,
 						ShopListActivity.class);
 				startActivity(intent);
 			}
 		});
 		findButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				Intent intent = new Intent(ItemMenuActivity.this,
 						MapActivity.class);
 				startActivity(intent);
 			}
 		});
 		aisleButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				Intent intent = new Intent(ItemMenuActivity.this,
 						AisleViewActivity.class);
 				startActivity(intent);
 			}
 		});
 		backButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				Intent intent = new Intent(ItemMenuActivity.this,
 						ShopListActivity.class);
 				startActivity(intent);
 			}
 		});
 		incrementButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				item.incrementQuantity();
 				quantity.setText("" + item.getQuantity());
 				totalPrice.setText(String.valueOf(item.getPrice()
 						* item.getQuantity()));
 			}
 		});
 		decrementButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				item.decrementQuantity();
 				quantity.setText("" + item.getQuantity());
 				totalPrice.setText(String.valueOf(item.getPrice()
 						* item.getQuantity()));
 
 				if (item.getQuantity() == 0) {
 					createDialog(item);
 				}
 			}
 		});
 	}
 
 	private void createDialog(final Item item) {
 		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
 		dialog.setMessage("Are you sure?");
 		dialog.setCancelable(false);
 		dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int id) {
 				// if this button is clicked, close
 				// current activity
 				DataService.getInstance().removeFromCart(item);
				Intent intent = new Intent(ShopListActivity.this,
						ShopListActivity.class);
 				startActivity(intent);
 			}
 		});
 		dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int id) {
 				// DataService.getInstance().removeFromCart(item);
 				// TODO: set the quantity back to 1?
 				item.setQuantity(1);
 			}
 		});
 
 	}
 }
