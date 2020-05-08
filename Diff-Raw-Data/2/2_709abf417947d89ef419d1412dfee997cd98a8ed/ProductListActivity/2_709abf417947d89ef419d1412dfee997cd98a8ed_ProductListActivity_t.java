 package com.grupo3.productConsult.activities;
 
 import java.io.IOException;
 import java.util.List;
 
 import org.apache.http.client.ClientProtocolException;
 
 import android.app.ListActivity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.AdapterView.OnItemClickListener;
 
 import com.grupo3.productConsult.Category;
 import com.grupo3.productConsult.CategoryManager;
 import com.grupo3.productConsult.Product;
 import com.grupo3.productConsult.R;
 import com.grupo3.productConsult.services.CategoriesSearchService;
 
 public class ProductListActivity extends ListActivity {
 
 	private List<Product> currList;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		String[] products = getProductNames();
 		setListAdapter(new ArrayAdapter<String>(this, R.layout.list_item,
 				products));
 
 		ListView lv = getListView();
 		lv.setTextFilterEnabled(true);
 		lv.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> parent, View view,
 					int position, long id) {
 				Intent newIntent = new Intent(ProductListActivity.this
 						.getApplicationContext(), ProductDisplayActivity.class);
 
 				Bundle bundle = new Bundle();
 				bundle.putSerializable("product", currList.get(position));
 				newIntent.putExtras(bundle);
 				startActivityForResult(newIntent, 0);
 			}
 		});
 	}
 
 	private String[] getProductNames() {
 		Bundle recdData = getIntent().getExtras();
 		int catPos = Integer.parseInt(recdData.getString("categoryPos"));
		int subCatPos = Integer.parseInt(recdData.getString("subCategoryPos"));
 
 		CategoryManager catManager;
 		try {
 			catManager = CategoryManager.getInstance();
 			Category slectedCat = catManager.getCategoryList().get(catPos);
 			int catId = slectedCat.getId();
 			int subCatId = slectedCat.getSubCategories().get(subCatPos).getId();
 			currList = CategoriesSearchService.fetchProductsBySubcategory(
 					catId, subCatId);
 			String[] names = new String[currList.size()];
 			int i = 0;
 			for (Product c : currList) {
 				names[i++] = c.getName() + "  " + Product.CURRENCY + " "
 						+ c.getPrice();
 			}
 			return names;
 		} catch (ClientProtocolException e) {
 		} catch (IOException e) {
 		}
 		return null;
 	}
 }
