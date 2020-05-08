 package ku.mobilepos.activity;
 
 import java.util.List;
 
 import ku.mobilepos.controller.CartController;
 import ku.mobilepos.controller.InventoryController;
 import ku.mobilepos.domain.Cart;
 import ku.mobilepos.domain.Inventory;
 
 import com.example.mobilepos.R;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemClickListener;
 
 public class SaleMainActivity extends Activity {
 	private Button addToCartButton;
 	private Button saleButton;
 	private Button inventoryButton;
 	private Button customerButton;
 	private Button historyButton;
 	private Button confirmButton;
 	private Button clearButton;
 	private ListView itemInCartList;
 	private TextView totalPriceTextView;
 	private double totalPrice;
 	private Cart cart;
 	private Inventory inventory;
 	private String[] itemInCartListStringArr;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.sale_main);
 
 		addToCartButton = (Button) findViewById(R.id.sale_main_b_add_cart);
 		itemInCartList = (ListView) findViewById(R.id.sale_main_itemlist);
 		totalPriceTextView = (TextView) findViewById(R.id.sale_main_t_price_total_cal);
 		saleButton = (Button) findViewById(R.id.sale);
 		inventoryButton = (Button) findViewById(R.id.inventory);
 		historyButton = (Button) findViewById(R.id.history);
 		customerButton = (Button) findViewById(R.id.customer);
 		confirmButton = (Button) findViewById(R.id.sale_main_b_confirmsell);
 		clearButton = (Button) findViewById(R.id.sale_main_b_cancel);
 		inventory = InventoryController.getInstance();
 		cart = Cart.getCartInstance();
 		createItemListStringArr();
 		
 		totalPriceTextView.setText(cart.getTotalSale()+" .-");
 		saleButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				Toast.makeText(getApplicationContext(),
 						"Your current page is already Sale", Toast.LENGTH_LONG)
 						.show();
 			}
 		});
 
 		inventoryButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				Intent goInventoryPage = new Intent(getApplicationContext(),
 						MainActivity.class);
 				startActivity(goInventoryPage);
 			}
 		});
 
 		historyButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				Toast.makeText(getApplicationContext(), "UnderConstruction",
 						Toast.LENGTH_LONG).show();
 			}
 		});
 
 		customerButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				Intent goCusPage = new Intent(getApplicationContext(),CustomerMainActivity.class);
     			startActivity(goCusPage);
 			}
 		});
 
 		addToCartButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				if (inventory.isEmpty()) {
 					Toast.makeText(
 							getApplicationContext(),
 							"Your inventory is empty\nPlease go to inventory to an item first",
 							Toast.LENGTH_SHORT).show();
 				} else {
 					Intent goSaleSelectItem = new Intent(
 							getApplicationContext(),
 							SaleSelectItemActivity.class);
 					startActivity(goSaleSelectItem);
 				}
 			}
 		});
 		
 		clearButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				cart.resetCart();
 				Intent refreshCart = new Intent(getApplicationContext(),SaleMainActivity.class);
     			startActivity(refreshCart);
 				Toast.makeText(
 						getApplicationContext(),
 						"Cart is reset.",
 						Toast.LENGTH_SHORT).show();
 			}
 		});
 		
 	}
 
 	public void createItemListStringArr() {
 		if (cart.getItemListInCart().size() != 0) {
 			itemInCartListStringArr = new String[cart.getItemListInCart()
 				.size()];
 			for (int i = 0; i < itemInCartListStringArr.length; i++) {
 				itemInCartListStringArr[i] = "Product name: " + cart.getItemListInCart().get(i)
 						.getItemName()+"\nPrice: "+cart.getItemListInCart().get(i).getItemPrice()+"\nQuantity: "+cart.getItemQuantity(i);
 				
 			}
 		
			itemInCartList = (ListView)findViewById(R.id.sale_main_itemlist);
 			ArrayAdapter<String> itemListAdapter = new ArrayAdapter<String>(
 					this, android.R.layout.simple_list_item_1,
 					android.R.id.text1, itemInCartListStringArr);
 			itemInCartList.setAdapter(itemListAdapter);
 			
 			
 			itemInCartList.setOnItemClickListener(new OnItemClickListener() {
 
 				@Override
 				public void onItemClick(AdapterView<?> parent, View view,
 						int position, long id) {
 					// TODO Auto-generated method stub
 
 					int itemPosition = position;
 					String itemValue = (String) itemInCartList
 							.getItemAtPosition(position);
 					// Show Alert
 					Toast.makeText(getApplicationContext(), "click",
 							Toast.LENGTH_LONG).show();
 				}
 			});
 		}
 	}
 }
