 package edu.sysuedaily.ui;
 
 import android.os.Bundle;
 import android.support.v4.app.FragmentTransaction;
 import android.widget.ArrayAdapter;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.MenuItem;
 
 import edu.sysuedaily.R;
 
 public class NewsActivity extends SherlockFragmentActivity implements OnNavigationListener {
 	
 	ActionBar actionBar;
 	//ListView listView;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		setTheme(com.actionbarsherlock.R.style.Theme_Sherlock_Light);
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_news);
 		
 		//listView = (ListView) findViewById(R.id.listview_head_activity_news);
 		
 		actionBar = getSupportActionBar();
 		actionBar.setDisplayHomeAsUpEnabled(true);
 		actionBar.setDisplayShowTitleEnabled(false);
 		
 		String[] arrayList = new String[] {"头条新闻", "讲座信息"};
 		ArrayAdapter<String> list = 
 				new ArrayAdapter<String>(this, com.actionbarsherlock.R.layout.sherlock_spinner_item, arrayList);
 		list.setDropDownViewResource(com.actionbarsherlock.R.layout.sherlock_spinner_dropdown_item);
 		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
 		actionBar.setListNavigationCallbacks(list, this);
 		
 		
 		
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch(item.getItemId()) {
 		case android.R.id.home:
 			finish();
 			break;
 		}
 		
 		
 		return super.onOptionsItemSelected(item);
 	}
 	
 	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
 		System.out.println(itemPosition);
 		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		NewsListFragment fragment = NewsListFragment.getInstant(itemPosition);
 		transaction.replace(R.id.framelayout_list_activity_news, fragment);
 		transaction.commit();
 		return false;
 	}
 	
 	
 }
