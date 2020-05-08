 package com.connectsy.categories;
 
 import java.util.ArrayList;
 
 import org.json.JSONException;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 
 import com.connectsy.R;
 import com.connectsy.categories.CategoryManager.Category;
 import com.connectsy.data.DataManager.DataUpdateListener;
import com.connectsy.ActionBarHandler;
 
 public class CategorySelector extends Activity implements DataUpdateListener, OnItemClickListener {
 	private CategoryAdapter adapter;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.category_selector);
         
        //set up logo clicks
        ActionBarHandler ab = new ActionBarHandler(this);
        
         Intent i = getIntent();
         ArrayList<Category> categories = null;
         if (i.hasExtra("com.connectsy.categories")){
 			try {
 				categories = Category.deserializeList(i.getExtras()
 						.getString("com.connectsy.categories"));
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}
         }else{
         	categories = new CategoryManager(this, this).getCategories();
         	if (categories.size() == 0)
         		findViewById(R.id.ab_refresh_spinner).setVisibility(View.VISIBLE);
         }
         adapter = new CategoryAdapter(this, R.layout.category_list_item, categories);
         ListView lv = (ListView)findViewById(R.id.category_list);
         lv.setOnItemClickListener(this);
         lv.setAdapter(adapter);
     }
 
     private void returnCategory(String cat){
 		Intent i = new Intent();
 		i.putExtra("com.connectsy.category", cat);
 		setResult(RESULT_OK, i);
 		finish();
     }
     
 	public void onItemClick(AdapterView<?> adapterView, View itemView, int position, long id) {
 		Category selected = adapter.getItem(position);
 		if (selected.categories != null){
 			Intent i = new Intent(Intent.ACTION_CHOOSER);
 			i.setType("vnd.android.cursor.item/vnd.connectsy.category");
 			i.putExtra("com.connectsy.categories", Category.serializeList(selected.categories));
 			startActivityForResult(i, 0);
 		}else{
 			returnCategory(selected.serialize());
 		}
 	}
 	
 	protected void onActivityResult(int requestCode, int resultCode, Intent data){
 		if (resultCode == RESULT_OK)
 			returnCategory(data.getExtras().getString("com.connectsy.category"));
 	}
 	
 	public void onDataUpdate(int code, String response) {
 		ArrayList<Category> categories = new CategoryManager(this, this).getCategories();
 	    adapter = new CategoryAdapter(this, R.layout.category_list_item, categories);
 	    ListView lv = (ListView)findViewById(R.id.category_list);
 	    lv.setAdapter(adapter);
 	    findViewById(R.id.ab_refresh_spinner).setVisibility(View.GONE);
 	}
 	public void onRemoteError(int httpStatus, int code) {}
 }
