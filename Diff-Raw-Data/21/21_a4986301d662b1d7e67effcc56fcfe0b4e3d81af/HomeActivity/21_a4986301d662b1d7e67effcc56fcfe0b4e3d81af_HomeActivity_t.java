 package ch.unibe.ese.shoppinglist;
 
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import ch.unibe.ese.core.BaseActivity;
 import ch.unibe.ese.core.ListManager;
 import ch.unibe.ese.core.ShoppingList;
 
 public class HomeActivity extends BaseActivity {
 
 	private ListManager manager;
 	private ArrayAdapter<ShoppingList> shoppingListAdapter;
 	private Activity homeActivity = this;
 	private boolean longClick = false;
	
	private DrawerLayout drawMenu;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		
		//create slide list
		drawMenu = NavigationDrawer.constructNavigationDrawer(drawMenu, this);
		
 		manager = getListManager();
 		
 		// Get List from manager
 		List<ShoppingList> shoppingLists = manager.getShoppingLists();
 		
 		shoppingListAdapter = new ArrayAdapter<ShoppingList>(this, 
 		        R.layout.shopping_list_item, shoppingLists);
 		
 		ListView listView = (ListView) findViewById(R.id.ShoppingListView);
 		listView.setAdapter(shoppingListAdapter);
 		
 		// Add long click Listener
 		listView.setOnItemLongClickListener(new OnItemLongClickListener() {
 
 			@Override
 			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
 					int arg2, long arg3) {
 				longClick = true; // make sure that the list doesn't open on long click
 				ShoppingList selectedList = shoppingListAdapter.getItem(arg2);
 				HomeActivity.this.startActionMode(
 						new ShoppingListActionMode(HomeActivity.this.manager,selectedList, HomeActivity.this.shoppingListAdapter, HomeActivity.this)
 						);
 				return false;
 			}
 		});
 		
 		// Add click Listener
 		listView.setOnItemClickListener(new OnItemClickListener() {
 			
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 				if (!longClick) {
 					Intent intent = new Intent(homeActivity, ViewListActivity.class);
 					intent.putExtra("selectedList", position);
 					homeActivity.startActivity(intent);
 				}
 				longClick = false;
 			}
 		});
 		
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.home, menu);
 		getMenuInflater().inflate(R.menu.home_menu, menu);
 		return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 	    // Handle presses on the action bar items
 	    switch (item.getItemId()) {
 	        case R.id.action_new:
 	        	Intent intent = new Intent(this, CreateListActivity.class);
 	            this.startActivity(intent);
 	            return true;
 	        default:
 	            return super.onOptionsItemSelected(item);
 	    }
 	}
 	
 	@Override
 	protected void onPause() {
 		super.onPause();
 	}
 }
