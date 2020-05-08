 package com.grupo3.productConsult.activities;
 
 import java.io.Serializable;
 import java.util.List;
 
 import android.app.ListActivity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.ResultReceiver;
import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.AdapterView.OnItemClickListener;
 
 import com.grupo3.productConsult.Product;
 import com.grupo3.productConsult.R;
 import com.grupo3.productConsult.services.CategoriesSearchService;
 
 public class ProductListActivity extends ListActivity {
 
 	private List<Product> currList;
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		Bundle recdData = getIntent().getExtras();
 		currList = (List<Product>) recdData.getSerializable("products");
 		String[] products = getProductNames();
 		setListAdapter(new ArrayAdapter<String>(this, R.layout.list_item,
 				products));
 
 		ListView lv = getListView();
 		lv.setTextFilterEnabled(true);
 		lv.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> parent, View view,
 					int position, long id) {
 				loadProduct(currList.get(position).getId());
 				/*
 				 * Intent newIntent = new Intent(ProductListActivity.this
 				 * .getApplicationContext(), ProductDisplayActivity.class);
 				 * 
 				 * Bundle bundle = new Bundle(); bundle.putString("productId",
 				 * currList.get(position).getId() + "");
 				 * newIntent.putExtras(bundle);
 				 * startActivityForResult(newIntent, 0);
 				 */
 			}
 		});
 	}
 
 	private String[] getProductNames() {
 		String[] names = new String[currList.size()];
 		int i = 0;
 		for (Product c : currList) {
 			names[i++] = c.getName() + "  " + Product.CURRENCY + " "
 					+ c.getPrice();
 		}
 		return names;
 	}
 
 	private void loadProduct(int prodId) {
 		Intent intent = new Intent(Intent.ACTION_SYNC, null, this,
 				CategoriesSearchService.class);
 		intent.putExtra("command", CategoriesSearchService.LOAD_PRODUCT);
		intent.putExtra("productId", prodId + "");
 		intent.putExtra("receiver", new ResultReceiver(new Handler()) {
 			@Override
 			protected void onReceiveResult(int resultCode, Bundle resultData) {
 				super.onReceiveResult(resultCode, resultData);
 				switch (resultCode) {
 				case CategoriesSearchService.STATUS_SUCCESS:
 					Serializable product = resultData
 							.getSerializable("product");
 					Intent intent = new Intent(ProductListActivity.this,
 							ProductDisplayActivity.class);
 					Bundle b = new Bundle();
 					b.putSerializable("product", product);
 					intent.putExtras(b);
 					startActivity(intent);
 					break;
 				case CategoriesSearchService.STATUS_ERROR:
					Log.d("FAIL", resultData.getString("error"));
 					break;
 				}
 			}
 		});
 		startService(intent);
 	}
 }
