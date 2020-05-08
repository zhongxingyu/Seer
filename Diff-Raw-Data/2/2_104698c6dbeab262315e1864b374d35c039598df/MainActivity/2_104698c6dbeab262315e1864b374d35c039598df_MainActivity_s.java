 package com.mustang.newsreader;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 
 import java.util.ArrayList;
 import java.util.List;
 import org.xmlpull.v1.XmlPullParserException;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.content.Context;
 import android.content.res.Configuration;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 import android.util.Log;
 import android.widget.Toast;
 import android.content.ContentValues;
 import android.database.Cursor;
 import android.support.v4.content.CursorLoader;
 import android.support.v4.content.Loader;
 import android.net.Uri;
 
 public class MainActivity extends SherlockFragmentActivity 
                        implements OnArticleSelectedListener,
                                   android.support.v4.app.LoaderManager.LoaderCallbacks<Cursor> {
 
 	private static final String feedurl = "http://mustangdaily.net/feed/";
 	private static final String listTag = "TAG_LIST";
 	private static final String itemTag = "TAG_ITEM";
 	private static final String menuTag = "TAG_MENU";
 	private static final String refreshTag = "TAG_REFRESH";
 	private static final String SAVED_UPDSETTING = "AUTOUPDATE";
 	private ArrayList<Article> m_arrItems;
 	private ArrayList<Article> m_tempItems;
 	private ArticleListFragment m_listFragment;
 	private ArticleFragment m_articleFragment;
 	private MenuFragment m_menuFragment;
 	private RefreshFragment m_refreshFragment;
 	private DataHandler m_dataHandler;
 	private String m_activeFragTag; //tag of active fragment when the orientation changed
 	private int m_articlePosition;  //postition of article viewed when the orientation changed
 	private boolean m_autoUpdate;
 	private int m_interval;
 	private boolean m_isTablet;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		this.m_isTablet = isTablet(this);
 		this.m_autoUpdate = true;
 		this.m_interval = 1;
 		m_dataHandler = DataHandler.getInstance();
 		this.m_arrItems = m_dataHandler.getArticles();
 		openArticleListFragment();
		getSupportActionBar().setTitle("");
 		//download xml only in fresh-start
 		if (savedInstanceState == null || this.m_arrItems.size() == 0 || this.m_autoUpdate)
 		    xmlHandler();
 	}
 	
 	/*@Override
 	public void onStart(){
 		super.onStart();
 		if (this.m_autoUpdate || this.m_arrItems.size() == 0)
 			xmlHandler();
 		//openArticleListFragment();
 	}*/
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu)
 	{
 		MenuInflater inflater = this.getSupportMenuInflater();
 		inflater.inflate(R.menu.mainmenu, menu);
 		return true;
 	}
 	
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		if(this.m_activeFragTag == itemTag) {
 			menu.getItem(3).setVisible(true);
 			menu.getItem(4).setVisible(true);
 			if(this.m_articlePosition >= this.m_arrItems.size()- 1)
 				menu.getItem(4).setEnabled(false);
 			if(this.m_articlePosition == 0)
 				menu.getItem(3).setEnabled(false);
 		}
 		else {
 			menu.getItem(3).setVisible(false);
 			menu.getItem(4).setVisible(false);			
 		}
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		int id = item.getItemId();
 		switch (id) {
 		case R.id.menu_home :
 		    if (m_articleFragment != null) {
 	    	    if (m_articleFragment.getCustomViewContainer() != null) {
 	    	        m_articleFragment.hideCustomView();
 	    	    }
 	    	    else if (m_articleFragment.getWebView().canGoBack()) {
 	    	        m_articleFragment.getWebView().goBack();
 	    	    }
 		    }
 			openArticleListFragment();
 			break;
 		case R.id.menu_menu :
 			openMenuFragment();
 			break;
 		case R.id.menu_refresh :
 			xmlHandler();
 			break;
 		case R.id.menu_settings :
 			openRefreshFragment();
 			break;
 		case R.id.menu_previous :
 			if(this.m_articlePosition > 0)
 				this.m_articlePosition--;
 			openArticleFragment(this.m_articlePosition);
 			break;
 		case R.id.menu_next :
 			if(this.m_articlePosition < this.m_arrItems.size()- 1)
 				this.m_articlePosition++;
 			openArticleFragment(this.m_articlePosition);
 			break;
 		}
 		return true;
 	}
 	@Override
 	public void onSaveInstanceState(Bundle outState){
 		//m_dataHandler.setArticles(m_arrItems);
 		String tag = getActiveFragment();
 		//if (tag != null && !tag.contentEquals(listTag)) {
 		if (tag != null) {
 			outState.putString("FRAGMENT_TAG", tag);
 			outState.putInt("ARTICLE_POSITION", m_articlePosition);
 		}
 		else {
 			outState.putString("FRAGMENT_TAG", listTag);
 			outState.putInt("ARTICLE_POSITION", 0);			
 		}
 		
 		outState.putBoolean(SAVED_UPDSETTING, this.m_autoUpdate);
 		super.onSaveInstanceState(outState);
 		Log.d("onSaveInstanceState","onSaveInstanceState");
 	}
 
 	@Override
 	public void onRestoreInstanceState(Bundle inState){
 		super.onRestoreInstanceState(inState);
 		m_dataHandler = DataHandler.getInstance();
 		m_arrItems = m_dataHandler.getArticles();
 		String tag = inState.getString("FRAGMENT_TAG");
 		if (tag != null) {
 			this.m_articlePosition = inState.getInt("ARTICLE_POSITION");
 			this.m_activeFragTag = tag;
 			if (this.m_activeFragTag == itemTag)
 				openArticleFragment(this.m_articlePosition);
 			if (this.m_activeFragTag == menuTag)
 				openMenuFragment();
 		}
 		this.m_autoUpdate = inState.getBoolean(SAVED_UPDSETTING, true);
 		Log.d("onRestoreInstanceState","onRestoreInstanceState");
 	}
 	
     // When user strat the app, calls AsyncTask.
     // Before attempting to fetch the URL, makes sure that there is a network connection.
     public void xmlHandler() {
         // Gets the URL from the UI's text field.
         ConnectivityManager connMgr = (ConnectivityManager) 
             getSystemService(Context.CONNECTIVITY_SERVICE);
         NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
         if (networkInfo != null && networkInfo.isConnected()) {
             new DownloadXmlTask().execute(feedurl);
         } else {
         	Toast.makeText(getApplicationContext(), "No network connection available.", Toast.LENGTH_LONG).show();
             //textView.setText("No network connection available.");
         }
     }
 
     private Uri constructContentUri(String type, String id) {
         String AUTHORITY = "com.mustang.newsreader.contentprovider";
         String BASE_PATH = "article_table";
         
         return Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH + 
                 "/" + type + "/" + id);
     }
 
      // Uses AsyncTask to create a task away from the main UI thread. This task takes a 
      // URL string and uses it to create an HttpUrlConnection. Once the connection
      // has been established, the AsyncTask downloads the contents of the webpage as
      // an InputStream. Finally, the InputStream is converted into a string, which is
      // displayed in the UI by the AsyncTask's onPostExecute method.
      private class DownloadXmlTask extends AsyncTask<String, Void, String> {
     	//private ArrayList<Article> list;
         @Override
         protected String doInBackground(String... urls) {
               
             // params comes from the execute() call: params[0] is the url.
             try {
             	m_tempItems = (ArrayList<Article>) downloadXml(feedurl);
             	if (m_tempItems.size() > 0){
                     return "success";
             	}
             	return "empty";
             } catch (IOException e) {
             	String message = "message not available";
             	if (e != null)
             		message = e.getMessage();
             	Log.e("IOException", message);
                 return "failed : " + message;
             }
             
         }
         
         @Override
         protected void onPostExecute(String result) {
         	if (result.equalsIgnoreCase("success")){
         		Log.d("parser",result);
         		m_arrItems.clear();
         		int count = 0;
         		for(Article a : m_tempItems){
         		    Uri contentUri = constructContentUri("articles", Long.toString(a.getID()));
         		    
         		    ContentValues values = new ContentValues();
         		    values.put(ArticleTable.ARTICLE_KEY_TITLE, a.getTitle());
         	        values.put(ArticleTable.ARTICLE_KEY_DESCRIPTION, a.getDescription());
         	        values.put(ArticleTable.ARTICLE_KEY_CONTENT, a.getContent());
         	        values.put(ArticleTable.ARTICLE_KEY_PUBDATE, a.getPubdate());
                     values.put(ArticleTable.ARTICLE_KEY_LINK, a.getLink());
                     values.put(ArticleTable.ARTICLE_KEY_CATEGORIES, a.getCategoriesInString());
                     values.put(ArticleTable.ARTICLE_KEY_IMGSRCS, a.getImgsrcInString());
                     
         		    Uri returnUri = getContentResolver().insert(contentUri, values);
         		    a.setID(Integer.parseInt(returnUri.getLastPathSegment()));
         		    
 
         			m_arrItems.add(a);
             		count++;
             		m_listFragment.notifyDataChanged();
         		}
         		//m_headerImage.setVisibility(View.GONE);
         		//getWindow().setBackgroundDrawable(null);
         		//getWindow().setBackgroundDrawableResource();
         		getWindow().setBackgroundDrawableResource(R.color.backgroundWhite);
         		Log.d("addItems",count + " added.");
         	}
         	else {
         		Toast.makeText(getApplicationContext(), "The server not available.", Toast.LENGTH_LONG).show();
         		Log.e("parser",result);
         	}
         }
         
     }
     
     // Given a URL, establishes an HttpUrlConnection and retrieves
     // the web page content as a InputStream, which it returns as
     // a string.
     private List<Article> downloadXml(String myurl) throws IOException {
        InputStream is = null;
        ArrayList<Article> items = new ArrayList<Article>();
        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
 	       conn.setConnectTimeout(15000 /* milliseconds */);
 	       conn.setRequestMethod("GET");
 	       conn.setDoInput(true);
 	       // Starts the query
 	       conn.connect();
 	       int response = conn.getResponseCode();
 	       Log.d("DEBUG", "The response is: " + response);
 	       is = conn.getInputStream();
 	
 	       NewsFeedXmlParser parser = new NewsFeedXmlParser();
 	       items = parser.parse(is);
       
         // Makes sure that the InputStream is closed after the app is
         // finished using it.
         } catch (XmlPullParserException e) {
         	Log.e("XmlPullParserException",e.getMessage());
 		   e.printStackTrace();
 	    } finally {
            if (is != null) {
               is.close();
            } 
         }
         return items;
     }
 
 	@Override
 	public void onArticleSelected(int pos) {
 		openArticleFragment(pos);
 	}     
 
 	public void openArticleFragment(int pos) {
 		this.m_articlePosition = pos;
 		FragmentManager fragmentManager = getSupportFragmentManager();
 		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
 		m_articleFragment = new ArticleFragment();
 		m_articleFragment.setActivityContent(this);
 		
 		fragmentTransaction.replace(R.id.fragment_container, m_articleFragment, itemTag);
 		fragmentTransaction.addToBackStack(itemTag);
 		fragmentTransaction.commit();
 		m_articleFragment.setContent(this.m_arrItems.get(pos));
 		this.m_activeFragTag = itemTag;
 		supportInvalidateOptionsMenu();
 	}
 	
 	/*public void resetList() {
 		int count = 0;
 		for(Article a : m_arrItems){
 			count = m_listFragment.addArticle(a);
 		}
 		Log.d("resetList",count + " added.");
 	}*/
 	
 	public String getActiveFragment() {
 		int count = getSupportFragmentManager().getBackStackEntryCount();
 		if (count == 0) {
 			return null;
 		}
 		String tag = getSupportFragmentManager().getBackStackEntryAt(count - 1).getName();
 		return tag;
 	}
 
 	public void openArticleListFragment() {
 		FragmentManager fragmentManager = getSupportFragmentManager();
 		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
 		m_listFragment = new ArticleListFragment();
 		Log.d("onCreate", "after new ArticleListFragment");
 		//replace fragment instead of adding it for handling orientation change
 		fragmentTransaction.replace(R.id.fragment_container, m_listFragment, listTag);
 		fragmentTransaction.addToBackStack(listTag);
 		fragmentTransaction.commit();
 		this.m_activeFragTag = listTag;
 		this.m_articlePosition = 0;
 		supportInvalidateOptionsMenu();
 	}
 	
 	public void openMenuFragment() {
 		int containerId;
 		if (this.m_isTablet)
 			containerId = R.id.fragment_subcontainer;
 		else
 			containerId = R.id.fragment_container;
 		FragmentManager fragmentManager = getSupportFragmentManager();
 		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
 		m_menuFragment = new MenuFragment();
 		//m_menuFragment.setArticles(this.m_arrItems);
 		fragmentTransaction.replace(containerId, m_menuFragment, menuTag);
 		fragmentTransaction.addToBackStack(menuTag);
 		fragmentTransaction.commit();
 		int size = m_menuFragment.setArticles(this.m_arrItems);
 		this.m_activeFragTag = menuTag;
 		supportInvalidateOptionsMenu();
 		//Toast.makeText(this, Integer.toString(size), Toast.LENGTH_LONG).show();
 	}
 	
 	public void openRefreshFragment() {
 		FragmentManager fragmentManager = getSupportFragmentManager();
 		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
 		m_refreshFragment = new RefreshFragment();
 		m_refreshFragment.setContext(this);
 		//m_menuFragment.setArticles(this.m_arrItems);
 		fragmentTransaction.replace(R.id.fragment_container, m_refreshFragment, refreshTag);
 		fragmentTransaction.addToBackStack(refreshTag);
 		fragmentTransaction.commit();
 		//int size = m_menuFragment.setArticles(this.m_arrItems);
 		this.m_activeFragTag = refreshTag;
 		supportInvalidateOptionsMenu();
 		//Toast.makeText(this, Integer.toString(size), Toast.LENGTH_LONG).show();
 	}
 	
 	public boolean isTablet(Context context) {
 	    boolean xlarge = ((context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == 4);
 	    boolean large = ((context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE);
 	    return (xlarge || large);
 	}
 	
 	public void setAutoUpdate(boolean b) {
 		this.m_autoUpdate = b;
 	}
 
 	public boolean getAutoUpdate() {
 		return this.m_autoUpdate;
 	}
 
 	public void setInterval(int h) {
 		this.m_interval = h;
 	}
 
 	public int getInterval() {
 		return this.m_interval;
 	}
     @Override
     public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
         Uri contentUri = constructContentUri("articles", "#");
         
         String[] columns = { ArticleTable.ARTICLE_KEY_ID, ArticleTable.ARTICLE_KEY_TITLE, 
                 ArticleTable.ARTICLE_KEY_DESCRIPTION, ArticleTable.ARTICLE_KEY_CONTENT, 
                 ArticleTable.ARTICLE_KEY_PUBDATE, ArticleTable.ARTICLE_KEY_LINK, 
                 ArticleTable.ARTICLE_KEY_CATEGORIES, ArticleTable.ARTICLE_KEY_IMGSRCS };
         
         return new CursorLoader(this, contentUri, columns, "", null, "");
     }
 
     @Override
     public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
     }
 
     @Override
     public void onLoaderReset(Loader<Cursor> loader) {
     }
 
 	@Override
 	public void onBackPressed() {
 	    if (m_articleFragment != null) {
     	    if (m_articleFragment.getCustomViewContainer() != null) {
     	        m_articleFragment.hideCustomView();
     	    }
     	    else if (m_articleFragment.getWebView().canGoBack()) {
     	        m_articleFragment.getWebView().goBack();
     	    }
     	    else {
                 super.onBackPressed();
             }
 	    }
 	    else {
 	        super.onBackPressed();
 	    }
 	}
 	
 	@Override
 	public void onResume(){
 		super.onResume();
 		if (this.m_arrItems.size() > 0)
 			getWindow().setBackgroundDrawableResource(R.color.backgroundWhite);
 	}
 }
