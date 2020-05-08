 package com.fooding.activities;
 
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Context;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.fooding.adapters.ProductsDbAdapter;
 import com.fooding.contracts.ProductDbContract;
 import com.fooding.models.Product;
 
 public class PurchaseListAcitivity extends Activity {
 	static final private String TAG = "PurchaseListAcitivity";
 	static final private String SELECTED_RECIPES_KEY = "selectedRIDs";
 	
 	private ListView purchaseListView;
 	private ProductDbContract db;
 	private List<Product> products;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.purchase_list_layout);
 		
 		Log.d(TAG, "content view set, next will handle extra from bundle");
 		
 		long[] selectedRIDs = getIntent().getExtras().getLongArray(SELECTED_RECIPES_KEY);
 		if (selectedRIDs.length <= 0) {
 			Toast
 				.makeText(getApplicationContext(), "No products for the recipes selected", Toast.LENGTH_LONG)
 				.show();
 		}
 		
 		db = new ProductsDbAdapter(getApplicationContext());
 		db.open();
 		
 		products = db.getProductsByRecipes(selectedRIDs);
 		
 		purchaseListView = (ListView) findViewById(R.id.purchase_list_view);
 		CustomArrayAdapter adapter = 
 				new CustomArrayAdapter(getApplicationContext(), R.layout.select_product_item, products);
 		purchaseListView.setAdapter(adapter);
 	}
 	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		db.close();
	}
	
 	private class CustomArrayAdapter extends ArrayAdapter<Product> {
 		private Context context;
 		private int resourceId;
 		private List<Product> products;
 		private View view;
 		
 		public CustomArrayAdapter(Context context, int resourceId, List<Product> products) {
 			super(context, resourceId, products);
 			
 			this.context = context;
 			this.resourceId = resourceId;
 			this.products = products;
 		}
 		
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			view = convertView;
 			
 			if (view == null) {
 				LayoutInflater inflater = 
 						(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 				view = inflater.inflate(resourceId, parent, false);
 			}
 			
 			Product product = products.get(position);
 			
 			if (product != null) {
 				TextView idTextView = (TextView) view.findViewById(R.id.checkable_selected_product_id);
 				if (idTextView != null)
 					idTextView.setText(String.valueOf(product.getId()));
 				
 				TextView nameTextView = (TextView) view.findViewById(R.id.checkable_selected_product_name);
 				if (nameTextView != null)
 					nameTextView.setText(product.getName());
 				
 				TextView qtyTextView = (TextView) view.findViewById(R.id.checkable_selected_product_quantity);
				if (qtyTextView != null) {
 					qtyTextView.setText(product.getQuantity());
				}
 			}
 			
 			return view;
 		}
 	}
 }
