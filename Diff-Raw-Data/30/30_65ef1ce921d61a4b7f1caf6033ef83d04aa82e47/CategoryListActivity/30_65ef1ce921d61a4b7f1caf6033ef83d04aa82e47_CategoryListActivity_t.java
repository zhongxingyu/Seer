 package com.example.restfulclient;
 
 import java.util.Collection;
 import java.util.List;
 
 import android.app.ListActivity;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.widget.AdapterView;
 import android.widget.BaseAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.example.restfulclient.helpers.Book;
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
 	
 	ListView lv = (ListView) findViewById(android.R.id.list);
 	registerForContextMenu(lv);
 	
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
 	    case R.id.menuAddCategory:
 		myApp.c = null;
 		Intent intent = new Intent(CategoryListActivity.this,
 		        CategoryDetailsActivity.class);
 		CategoryListActivity.this.startActivity(intent);
 		return true;
 	    case R.id.menuSync:
 		doSync();
 		return true;
 	    case R.id.menuRefresh:
 		new GetCategoriesThread().execute(myApp.addr);
 	    default:
 		return super.onOptionsItemSelected(item);
 	}
     }
     
     private void doSync()
     {
 	new SyncThread().execute();
     }
     
     @Override
     public void onResume()
     {
 	super.onResume();
 	new GetCategoriesThread().execute(myApp.addr);
 	myApp.c = null;
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
 	Log.d("kliknieto: ", "" + selection.getCategoryId());
 	myApp.c = selection;
 	
 	Intent i = new Intent(CategoryListActivity.this,
 	        BooksListActivity.class);
 	CategoryListActivity.this.startActivity(i);
     }
     
     private void dumpData(List<Category> result)
     {
 	DatabaseHelper dh = new DatabaseHelper(this);
 	
 	if (result == null)
 	    return;
 	
 	dh.dropDb();
 	for (Category c : result)
 	{
 	    Log.v("add c: ", c.getCategoryId());
 	    dh.addCategory(c);
 	    for (Book b : c.getBooks())
 	    {
 		b.setCatId(c.getCategoryId());
 		Log.v("add b: ", b.getBookId());
 		dh.addBook(b);
 	    }
 	    
 	}
 	
 	Toast.makeText(
 	        this,
 	        "Zapisano " + dh.getCatCount() + " kategorii i "
 	                + dh.getBookCount() + " ksiazek do SQLite",
 	        Toast.LENGTH_LONG).show();
 	if (myApp.isOffline())
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
 	
 	if (myApp.isOffline())
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
 	@Override
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
 	
 	@Override
         public long getItemId(int position)
 	{
 	    return position;
 	}
 	
 	@Override
         public Object getItem(int position)
 	{
 	    return myApp.categories.get(position);
 	}
 	
 	@Override
         public int getCount()
 	{
 	    return myApp.categories.size();
 	}
     }
     
     @Override
     public void onCreateContextMenu(ContextMenu menu, View v,
 	    ContextMenuInfo menuInfo)
     {
 	super.onCreateContextMenu(menu, v, menuInfo);
 	menu.setHeaderTitle("Kategorie");
 	menu.add(0, v.getId(), 0, "Edytuj");
 	menu.add(0, v.getId(), 0, "Usu");
     }
     
     @Override
     public boolean onContextItemSelected(MenuItem item)
     {
 	AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
 	        .getMenuInfo();
 	myApp.c = myApp.categories.get(info.position);
 	if (item.getTitle() == "Edytuj")
 	{
 	    Intent intent = new Intent(CategoryListActivity.this,
 		    CategoryDetailsActivity.class);
 	    
 	    CategoryListActivity.this.startActivity(intent);
 	    
 	} else if (item.getTitle() == "Usu")
 	{
 	    
 	    new DeleteCategoriesThread().execute(myApp.addr);
 	} else
 	{
 	    myApp.c = null;
 	    return false;
 	}
 	return true;
     }
     
     class GetCategoriesThread extends AsyncTask<String, Void, List<Category>>
     {
 	ILibraryDAO library;
 	
 	@Override
         protected List<Category> doInBackground(String... url)
 	{
 	    if (myApp.isOffline())
 		library = new SQLiteLibrary(CategoryListActivity.this);
 	    else
 		library = new OnlineLibrary(url[0]);
 	    if (dumpToOffline && !myApp.isOffline())
 		return library.getCatsAndBooks();
 	    else
 		return library.getCategories();
 	}
 	
 	@Override
         protected void onPostExecute(List<Category> result)
 	{
 	    Log.v("dump/offline: ", dumpToOffline + "/" + myApp.isOffline());
 	    // jeli uytkownik wymusi pobranie danych do SQLite i mamy
 	    // poczenie z internetem to ...
 	    if (dumpToOffline && !myApp.isOffline())
 	    {
 		Log.v("AsyncTask",
 		        "dumping offline cache size: " + result.size());
 		dumpData(result);
 		dumpToOffline = false;
 	    }
 	    Log.v("AsyncTask", "setting result");
 	    setCategory(result);
 	}
     }
     
     class DeleteCategoriesThread extends AsyncTask<String, Void, Void>
     {
 	ILibraryDAO library;
 	
 	@Override
         protected Void doInBackground(String... url)
 	{
 	    if (myApp.isOffline())
 		library = new SQLiteLibrary(CategoryListActivity.this);
 	    else
 		library = new OnlineLibrary(url[0]);
 	    
 	    library.deleteCategory(myApp.c);
 	    return null;
 	}
 	
 	@Override
         protected void onPostExecute(Void result)
 	{
 	    new GetCategoriesThread().execute(myApp.addr);
 	    Log.v("AsyncTask", "setting result");
 	}
     }
     class SyncThread extends AsyncTask<String, Void, Void>
     {
 	@Override
         protected Void doInBackground(String... url)
 	{
 	    if (!myApp.isOffline())
 	    {
 		SQLiteLibrary sqlLib = new SQLiteLibrary(getApplicationContext());
 		OnlineLibrary olLib = new OnlineLibrary(myApp.addr);
 		
 		Collection<Category> localCats = sqlLib.getCategories();
 		Collection<Category> remoteCats = olLib.getCategories();
 		
		localCats.removeAll(remoteCats); // TODO porownywanie ksiazek -> pobieranie ksiazek z jax
 		
 		for(Category c : localCats)
 		{
 		    Log.v("localCats: ", c.getCategoryId());
 		    olLib.addCategory(c);
 		}
 	    } else
 	    {
 		
 	    }
 	    
 	    return null;
 	}
 
 	@Override
         protected void onPostExecute(Void result)
 	{
 	    new GetCategoriesThread().execute(myApp.addr);
 	    Log.v("AsyncTask", "setting result");
 	}
     }
 }
