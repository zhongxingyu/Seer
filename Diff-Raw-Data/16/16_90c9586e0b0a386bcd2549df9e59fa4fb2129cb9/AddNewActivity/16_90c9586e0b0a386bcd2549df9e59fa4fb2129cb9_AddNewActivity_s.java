 package com.github.barcodescanner.activities;
 
 import com.github.barcodescanner.R;
 import com.github.barcodescanner.core.DatabaseHelper;
 import com.github.barcodescanner.core.DatabaseHelperFactory;
 import com.github.barcodescanner.core.Product;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.view.Window;
 import android.widget.EditText;
 import android.widget.TextView;
 
 public class AddNewActivity extends Activity{
 	
 	@SuppressWarnings("unused")
 	private static final String TAG = "AddNewActivity";
 	
 	private DatabaseHelper database;
 	private EditText editPrice;
 	private EditText editName;
 	private String productID;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.activity_addnew);
 		
 		//Get instance of DatabaseHelper class
 		database = DatabaseHelperFactory.getInstance();
 		//Get product ID to be displayed for new product
 		productID = getIntent().getExtras().getString("productID");
 		//TextView instance to display product ID for new product
     	TextView view = (TextView) findViewById(R.id.new_product_id);
     	//Display product ID for the new product
     	view.setText(this.productID);
     	//EditText instance to get new Name for product
     	editName = (EditText)findViewById(R.id.new_product_name_field);
     	//EditText instance to get new Price for product
     	editPrice = (EditText)findViewById(R.id.new_product_price);
 	}
 	
 	public void addProduct(View view) {
 		// Get the product id and store it to a string
 		String barcode = this.productID;
 		// Get new Product name from EditText
 		String productName = editName.getText().toString();
 		// Get new Product price from EditText and cast to an int
		int productPrice = Integer.parseInt(editPrice.getText().toString());
 		// Create instance of Product class for the new Product
 		Product newProduct = new Product(productName,productPrice,barcode);
 		// Add the new Product to the database
 		database.addProduct(newProduct);
 		// We're done, so call finish()
 		finish();
 	}

 }
