 package com.grupo3.productConsult.activities;
 
 import java.io.Serializable;
 
 import android.app.ListActivity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.ResultReceiver;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.grupo3.productConsult.Category;
 import com.grupo3.productConsult.CategoryManager;
 import com.grupo3.productConsult.R;
 import com.grupo3.productConsult.services.CategoriesSearchService;
 import com.grupo3.productConsult.utilities.CustomAdapter;
 
 public class SubCategoryListActivity extends ListActivity {
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		Bundle recdData = getIntent().getExtras();
 		int catPos = Integer.parseInt(recdData.getString("categoryPos"));
 		String breadCrumb = recdData.getString("breadCrumb");
		setTitle(breadCrumb + " > ");
 
 		CategoryManager catManager = CategoryManager.getInstance();
 		setListAdapter(new CustomAdapter(this,
 				R.layout.list_item, catManager
 						.getSubCategoryNames(catPos)));
 
 		ListView lv = getListView();
 		lv.setTextFilterEnabled(true);
 
 		Animation a = AnimationUtils.makeInAnimation(getBaseContext(), false);
 		a.setDuration(500);
 		lv.setAnimation(a);
 	}
 
 	@Override
 	protected void onListItemClick(ListView l, View view, int position, long id) {
 		CharSequence text = ((TextView)view.findViewById(R.id.listText)).getText();
 		Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT)
 				.show();
 		loadProductList(position);
 	}
 
 	private void loadProductList(final int subCatIndex) {
 		Bundle data = getIntent().getExtras();
 		final int catIndex = Integer.parseInt(data.getString("categoryPos"));
 		Intent intent = new Intent(Intent.ACTION_SYNC, null, this,
 				CategoriesSearchService.class);
 		intent.putExtra("command",
 				CategoriesSearchService.LOAD_PRODUCTS_BY_SUBCATEGORY);
 		final Category category = CategoryManager.getInstance()
 				.getCategoryList().get(catIndex);
 		int catId = category.getId();
 		int subCatId = category.getSubCategories().get(subCatIndex).getId();
 		intent.putExtra("categoryId", catId + "");
 		intent.putExtra("subCategoryId", subCatId + "");
 		intent.putExtra("receiver", new ResultReceiver(new Handler()) {
 			@Override
 			protected void onReceiveResult(int resultCode, Bundle resultData) {
 				super.onReceiveResult(resultCode, resultData);
 				switch (resultCode) {
 				case CategoriesSearchService.STATUS_SUCCESS:
 					Serializable productList = resultData
 							.getSerializable("products");
 					Intent intent = new Intent(SubCategoryListActivity.this,
 							ProductListActivity.class);
 					Bundle b = new Bundle();
 					b.putSerializable("products", productList);
 					String subCatName = category.getSubCategories().get(
 							subCatIndex).getName();
 					b.putString("breadCrumb", getTitle().toString()
 							+ subCatName + " > ");
 					intent.putExtras(b);
 					startActivity(intent);
 					break;
 				case CategoriesSearchService.STATUS_ERROR:
 					CharSequence text = getText(R.string.connectionError);
 					Toast.makeText(getApplicationContext(), text,
 							Toast.LENGTH_SHORT).show();
 					break;
 				}
 			}
 		});
 		startService(intent);
 	}
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if (keyCode == KeyEvent.KEYCODE_MENU) {
 			Intent intent = new Intent(this, MenuActivity.class);
 			intent.putExtras(getIntent());
 			startActivity(intent);
 			return true;
 		}
 		return super.onKeyDown(keyCode, event);
 	}
 }
