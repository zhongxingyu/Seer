 package com.gui.taptobuy.activity;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 import org.json.JSONObject;
 
 
 
 import com.gui.taptobuy.customadapter.CategoriesCustomAdapter;
 import com.gui.taptobuy.phase1.R;
 import com.gui.taptobuy.views.Category;
 
 import android.app.Activity;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 
 
 public class CategoryActivity extends Activity implements OnItemClickListener {
 	
 	private LayoutInflater layoutInflator;
 	private ListView categoryList;
 	private ArrayList<Category> showingCategories;
 	private String currentParentCategoryName;
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {		
 		super.onCreate(savedInstanceState);
		setContentView(R.layout.emptylist);	
 		categoryList = (ListView)findViewById(R.id.ViewList);
 		this.layoutInflator = LayoutInflater.from(this);	
 		categoryList.setOnItemClickListener(this);
		new getSubCategoriesTask().execute("All"); 
 	}
 		
 	
 	private ArrayList<Category> getSubCategories(String clickedCategory){
 		HttpClient httpClient = new DefaultHttpClient();
 		String categoryDir = "http://10.0.2.2:9000/categories/";
		if(currentParentCategoryName == null && clickedCategory.equals("All")){
			categoryDir+="All";
 		}
 		else{
 			categoryDir+=currentParentCategoryName + "/" + clickedCategory;
 		}
		currentParentCategoryName = new String(clickedCategory);
 		HttpGet get = new HttpGet(categoryDir);
 		get.setHeader("content-type", "application/json");
 		try
 		{
 			HttpResponse resp = httpClient.execute(get);
 			if(resp.getStatusLine().getStatusCode() == 200){
 				String jsonString = EntityUtils.toString(resp.getEntity());
 				JSONObject json = new JSONObject(jsonString);
				showingCategories = new ArrayList<Category>();		
 				String tmpCatName = "";
 				Iterator<String> iter = (Iterator<String>) json.keys();
				while(iter.hasNext()){
 					tmpCatName = String.valueOf(iter.next());
 					showingCategories.add(new Category(tmpCatName,json.getBoolean(tmpCatName)));
 				}
 			}
 			else{
 				Log.e("JSON","Categories json could not be downloaded.");
 			}
 		}
 		catch(Exception ex)
 		{
 			Log.e("Categories","Error!", ex);
 		}
 		return showingCategories;
 	}
 	
 	public class getSubCategoriesTask extends AsyncTask<String,Void,ArrayList<Category>> {
 
 		protected ArrayList<Category> doInBackground(String... params) {
 			return getSubCategories(params[0]);//download subcategories of clickedCategory
 		}
 
 		protected void onPostExecute(ArrayList<Category> showingCategories ) {
 			categoryList.setAdapter(new CategoriesCustomAdapter(CategoryActivity.this,CategoryActivity.this.layoutInflator, showingCategories));
 		}
 	}
 	
 	public static class MyViewCategory {
 		public Category category;
 		public TextView categoryName;
 		public ImageView arrow;		
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> categories, View v, int arg2, long arg3) 
 	{
 		MyViewCategory categoriesHolder = (MyViewCategory) v.getTag();
		new getSubCategoriesTask().execute(categoriesHolder.category.getName());	
 	}
 }
