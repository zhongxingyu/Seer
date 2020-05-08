 package com.example.restfulclient;
 
 import java.util.List;
 
 import android.app.ListActivity;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.widget.BaseAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.example.restfulclient.helpers.Category;
 import com.example.restfulclient.helpers.DatabaseHelper;
 import com.example.restfulclient.helpers.ILibraryDAO;
 import com.example.restfulclient.helpers.MyApplication;
 import com.example.restfulclient.helpers.OnlineLibrary;
 import com.example.restfulclient.helpers.SQLiteLibrary;
 
 public class CategoryListActivity extends ListActivity
 {
 	MyApplication myApp;
 	boolean dumpToOffline = false;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.activity_category_list);
 
 		myApp = (MyApplication) getApplication();
 
		new GetCategoriesThread().execute(myApp.addr);
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu)
 	{
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_category_list, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item)
 	{
 		switch (item.getItemId())
 		{
 			case R.id.menuCacheOffline:
 				doOfflineCache();
 				return true;
 			default:
 				return super.onOptionsItemSelected(item);
 		}
 	}
 
 	private void doOfflineCache()
 	{
 		Log.v("cacheing", "offline");
 		dumpToOffline = true;
		new GetCategoriesThread().execute(myApp.addr);
 	}
 
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id)
 	{
 		// super.onListItemClick(l, v, position, id);
 		Category selection = (Category) l.getItemAtPosition(position);
 		Log.d("kliknieto: ", selection.getCategoryId());
 		myApp.c = selection;
 
 		Intent i = new Intent(CategoryListActivity.this,
 		        BooksListActivity.class);
 		CategoryListActivity.this.startActivity(i);
 	}
 
 	class GetCategoriesThread extends AsyncTask<String, Void, List<Category>>
 	{
 		ILibraryDAO library;
 
 		protected List<Category> doInBackground(String... url)
 		{
 			if (myApp.offline)
 				library = new SQLiteLibrary();
 			else
 				library = new OnlineLibrary();
 
 			library.setActivity(CategoryListActivity.this);
 
 			return library.getCategories(url[0]);
 		}
 
 		protected void onPostExecute(List<Category> result)
 		{
 			if (dumpToOffline && !myApp.offline)
 			{
 				Log.v("AsynTask",
 				        "dumping offline cache size: " + result.size());
 				dumpData(result);
 				dumpToOffline = false;
 			}
 			Log.v("AsynTask", "setting result");
 			setCategory(result);
 		}
 	}
 
 	private void dumpData(List<Category> result)
 	{
 		DatabaseHelper dh = new DatabaseHelper(this);
 
 		if (result == null)
 			return;
 
 		dh.dropDb();
 		for (Category c : result)
 			dh.addCategory(c);
 
 		Toast.makeText(this,
 		        "Zapisano " + dh.getCatCount() + " kategorii do SQLite",
 		        Toast.LENGTH_LONG).show();
 		if (myApp.offline)
 			goOffline();
 
 	}
 
 	private void goOffline()
 	{
 		DatabaseHelper dh = new DatabaseHelper(this);
 		myApp.categories.clear();
 		myApp.categories.addAll(dh.getAllCats());
 	}
 
 	public void setCategory(List<Category> result)
 	{
 
 		if (myApp.offline)
 		{
 			goOffline();
 		} else
 		{
 			myApp.categories.clear();
 			myApp.categories.addAll(result);
 		}
 
 		Log.v("zapisane kategorie: ", String.valueOf(myApp.categories.size()));
 		setListAdapter(new CatAdapter());
 	}
 
 	private class CatAdapter extends BaseAdapter
 	{
 		public View getView(int pos, View view, ViewGroup parent)
 		{
 			if (view == null)
 			{
 				view = View.inflate(CategoryListActivity.this,
 				        android.R.layout.two_line_list_item, null);
 			}
 
 			Category cat = (Category) getItem(pos);
 
 			TextView text = (TextView) view.findViewById(android.R.id.text1);
 			text.setText(cat.getCategoryId());
 
 			text = (TextView) view.findViewById(android.R.id.text2);
 			text.setText(cat.getCategoryName());
 			return view;
 		}
 
 		public long getItemId(int position)
 		{
 			return position;
 		}
 
 		public Object getItem(int position)
 		{
 			return myApp.categories.get(position);
 		}
 
 		public int getCount()
 		{
 			return myApp.categories.size();
 		}
 	}
 }
