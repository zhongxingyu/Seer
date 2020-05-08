 package edu.cis350.mosstalkwords;
 
 import java.util.ArrayList;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ListView;
 
 /*
 * CategoryList is to represent the category list module
  */
 public class CategoryListActivity extends UserActivity {
 
 	// The data to show
 	ArrayList<Category> categoryList;
 
 	private void initList() {
 		// new dynamic categories, but no image icon
 		new LoadCategories().execute(this);

		// old has hardocoded categoris(living/nonliving) + image

		// categoryList = new ArrayList<Category>();
		// We populate the category list
		// categoryList.add(new Category(R.drawable.living, "Living"));
		// categoryList.add(new Category(R.drawable.nonliving, "NonLiving"));
 	}
 
 	public void startMain(String categoryName) {
 		Intent activityMain = new Intent(this, MainActivity.class);
 		activityMain.putExtra("startCategory", categoryName);
 		startActivity(activityMain);
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.category_list);
 
 		initList();
 
 		// We get the ListView component from the layout
 	}
 
 	private class LoadCategories extends
 			AsyncTask<CategoryListActivity, Category, Void> {
 		@Override
 		protected Void doInBackground(CategoryListActivity... params) {
 			// TODO Auto-generated method stub
 			ImageManager im = new ImageManager(getUserName(),
 					getApplicationContext());
 			categoryList = new ArrayList<Category>();
 
 			for (String s : im.img_SDB.getAllCategories()) {
 				publishProgress(new Category(-1, s));
 			}
 			return null;
 		}
 
 		@Override
 		public void onProgressUpdate(Category... progress) {
 			categoryList.add(progress[0]);
 		}
 
 		@Override
 		public void onPostExecute(Void v) {
 			ListView lv = (ListView) findViewById(R.id.category_list);
 
 			// This is a simple adapter that accepts as parameter
 			// Context
 			// Data list
 			// The row layout that is used during the row creation
 			// The View id used to show the data. The key number and the view id
 			// must match
 
 			lv.setAdapter(new CategoryListAdapter(CategoryListActivity.this
 					.getApplicationContext(), R.layout.row_image, categoryList));
 			lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 
 				public void onItemClick(AdapterView<?> parent, final View view,
 						int position, long id) {
 					startMain(categoryList.get(position).getName());
 					finish();
 				}
 			});
 		}
 	}
 }
