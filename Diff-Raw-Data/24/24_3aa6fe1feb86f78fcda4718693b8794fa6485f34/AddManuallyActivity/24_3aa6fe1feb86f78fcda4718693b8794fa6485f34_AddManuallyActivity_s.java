 package com.github.barcodescanner.product;
 
 import java.util.List;
 import java.util.Scanner;
 
 import com.github.barcodescanner.R;
 import com.github.barcodescanner.activities.MainActivity;
 import com.github.barcodescanner.database.DatabaseHelper;
 import com.github.barcodescanner.database.DatabaseHelperFactory;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
import android.view.Window;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class AddManuallyActivity extends Activity{
 	
 	private DatabaseHelper database;
 	private EditText editPrice;
 	private EditText editName;
 	private EditText editId;
 	private boolean adminMode;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_add_manually);
 		adminMode = getIntent().getExtras().getBoolean("adminMode");
 		setupDatabase();
 
 		editName = (EditText) findViewById(R.id.manual_product_name_field);
 		editPrice = (EditText) findViewById(R.id.manual_product_price_field);
 		editId = (EditText) findViewById(R.id.manual_product_id_field);
 	}
 	
 	
 	/**
 	 * Gives the class access to the database.
 	 */
 	private void setupDatabase() {
 		DatabaseHelperFactory.init(this);
 		database = DatabaseHelperFactory.getInstance();
 	}
 	
 	
 	public void addProductManually(View view) {
 		String productName = editName.getText().toString();
 		String productId = editId.getText().toString();
 		int productPrice;
 		Scanner scanner = new Scanner(editPrice.getText().toString());
 		boolean idExist = checkId(productId);
 		
 		if(idExist){
 			Context context = getApplicationContext();
 			String text = productId + " ID already exists in database";
 			Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
 			
 			toast.show();
 			
 			finish();
 		} else if (scanner.hasNextInt()) {
 			// If the price is an integer, we accept it.
 			productPrice = scanner.nextInt();
 
 			Product newProduct = new Product(productName, productPrice, productId);
 			database.addProduct(newProduct);
 
 			Context context = getApplicationContext();
 			CharSequence text = getString(R.string.new_product_toast_done, productName);
 			
 			Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
 			toast.show();
 			
 			Intent intent = new Intent(this, MainActivity.class);
 			intent.putExtra("adminMode", adminMode);
 			startActivity(intent);
 		} else {
 			// otherwise, we show a toast
 			Context context = getApplicationContext();
 			CharSequence text = getString(R.string.new_product_toast_failed);
 			int duration = Toast.LENGTH_SHORT;
 
 			Toast toast = Toast.makeText(context, text, duration);
 			toast.show();
 		}
 	}
 	
 	private boolean checkId(String id){
 		List<Product> productList = database.getProducts();
 		boolean exists = false;
 		
 		for(Product p : productList){
 			if(p.getBarcode().equals(id)){
 				exists = true;
 				break;
 			}
 		}
 		return exists;
 	}
 }
