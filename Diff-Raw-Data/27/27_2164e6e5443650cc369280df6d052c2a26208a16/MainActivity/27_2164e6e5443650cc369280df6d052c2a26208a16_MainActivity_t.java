 package edu.gatech.oad.rocket.findmythings;
 
 import java.util.ArrayList;
 
 import android.app.ActionBar;
 import android.app.AlertDialog;
 import android.app.ActionBar.Tab;
 import android.app.ListActivity;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ListView;
 import android.widget.SearchView;
 
 import edu.gatech.oad.rocket.findmythings.control.*;
 import edu.gatech.oad.rocket.findmythings.model.Item;
 import edu.gatech.oad.rocket.findmythings.model.Type;
 import edu.gatech.oad.rocket.findmythings.util.*;
 /**
  * CS 2340 - FindMyStuff Android App
  *
  * An activity representing a list of Items. This activity has different
  * presentations for handset and tablet-size devices. On handsets, the activity
  * presents a list of items, which when touched, lead to a
  * {@link ItemDetailActivity} representing item details. On tablets, the
  * activity presents the list of items and item details side-by-side using two
  * vertical panes.
  *
  * @author TeamRocket
  */
 public class MainActivity extends ListActivity  {
 
 	/**
 	 * The class of {@link Item} displayed in this list.
 	 */
 	public static Type mType = null;
 	
 	/**
 	 * Reference to the actionbar (the thing at the top of every activity)
 	 */
 	private ActionBar actionBar;
 	
 	/**
 	 * Used for the AlertDialog when user tries to sign out
 	 */
 	private boolean logOut = false;
 	
 	/**
 	 * Reference to Singleton class
 	 */
 	private Controller control = Controller.shared();
 	
 	/**
 	 * The ArrayAdapter to be displayed
 	 */
 	public static Adapter adapter;
 	
 	/**
 	 * Current list the Adapter is displaying
 	 */
 	public static ArrayList<Item> currList;
 	
 	/**
 	 * Reference to the view holding the ArrayAdapter
 	 */
 	private ListView mView;
 	
 	/**
 	 * Indirect reference to the search bar in the ActionBar
 	 */
 	private MenuItem search;
 	
 	/**
 	 * Direct reference to the search bar in the ActionBar
 	 */
 	private SearchView mSearch;
 	
 	/**
 	 * creates window with correct layout
 	 * @param savedInstanceState
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_item_list);
 		
 
 		
 		mView = (ListView)findViewById(android.R.id.list);
 		mView.setTextFilterEnabled(true); //TODO: gestures
 		//mView.setOnScrollListener(new OnScrollListener { 
 			//public void onScroll
 		//});
 		
 		//Create tabs and hide title
 		actionBar = getActionBar();
 	    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 	    actionBar.setDisplayShowTitleEnabled(true);
 	    createTabs();
 	   
 	    //Array to be represented by the adapter
 	  	currList = control.getItem(mType);
 	  	//Takes the array and creates individual views for each item
 	  	adapter = new Adapter(this,
 	  			android.R.layout.simple_list_item_activated_1,
 				android.R.id.text1, currList);
 	  	
 	  	mView.setAdapter(adapter);
 	    	
 		setTitle("Find My Things");
 		getActionBar().setDisplayHomeAsUpEnabled(false);
 		
 	}
 	
 	/**
 	 * Updates the ArrayList the adapter is displaying as well as the Type of items being displayed
 	 * @param tempList
 	 * @param kind
 	 */
 	public static void update(ArrayList<Item> tempList, Type kind) {
		mType = kind;
 		currList = tempList;
 		adapter.setList(tempList);		
		adapter.notifyDataSetChanged();
 	}	
	
	/**
	 * 
	 * @param tempList
	 */
	public static void update(ArrayList<Item> tempList) {
		currList = tempList;
		adapter.setList(tempList);
		adapter.notifyDataSetChanged();
	}
 
 	/**
 	 * takes care of action when a key is pressed down
 	 * @param  keyCode
 	 * @param  event
 	 * @return boolean 
 	 */
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event)  {
 		//Tells Activity what to do when back key is pressed
 	    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
 			return false;
 	    }
 	    if (keyCode == KeyEvent.KEYCODE_SEARCH) {
 	    	//Shows the search bar
 	    	search.expandActionView();
 	    	return true;
 	    }
 	    return super.onKeyDown(keyCode, event);
 	}
 
 	/**
 	 * shows the options that are available for user depending on the item selected (submit/search/login/etc)
 	 * @param item - menu selected
 	 * @return boolean 
 	 */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 	    switch (item.getItemId()) {
 	        case R.id.item_list_submit:
 			return toSubmit();
 	        case R.id.menu_list_filter:
 	        	Intent i = new Intent(MainActivity.this, FilterActivity.class);
 				startActivityForResult(i, 1);
 			    overridePendingTransition(R.anim.slide_up_modal, android.R.anim.fade_out);
 				return true;
 	       	case R.id.menu_login: 
 				return Login.currUser==null? toLogIn():logOut(); 
 			case R.id.menu_account: 
 				return toAccount();
 			case R.id.menu_admin:
 				return toAdmin();
 	    }
 	    return super.onOptionsItemSelected(item);
 	}
 
 	/**
 	 * takes user to new window with the clicked item's details
 	 * @param l
 	 * @param v
 	 * @param position
 	 * @param id
 	 */
 	@Override
 	protected void onListItemClick (ListView l, View v, int position, long id) {
 		super.onListItemClick(l, v, position, id);
 		Intent next = new Intent(this, ItemDetailActivity.class);
	
		next.putExtra("id", position);
 	    startActivity(next);
 	    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
 	}
 
 	/**
 	 * Opens a new SubmitActivity activity with the current type of item.
 	 */
 	public boolean toSubmit() {
 		if (Login.currUser != null) {
 			Intent goToNextActivity = new Intent(this, SubmitActivity.class);
 			if (mType != null) goToNextActivity.putExtra(Type.ID, mType.ordinal());
 			startActivity(goToNextActivity);
 			overridePendingTransition(R.anim.slide_up_modal, R.anim.hold);
 		}
 		else {
 			ErrorDialog toLogin =  new ErrorDialog("Must Sign-in to submit an item.", "Sign-in", "Cancel");
 			AlertDialog.Builder temp = toLogin.getDialog(this,
 				new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int id) {
 						MainActivity.this.toLogIn();
 					}	
 				}, 
 				new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int id) {
 			            	//cancel
 					}
 				});
 			temp.show();
 		}
 	    return true;
 	}
 
 	/**
 	 * creates the options menu (login, account, admin button)
 	 * @param menu
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_item_list, menu);
 		getMenuInflater().inflate(R.menu.activity_main_menu, menu);
 		
 	    mSearch = (SearchView) menu.findItem(R.id.main_search_bar).getActionView();
 	    mSearch.bringToFront();
 	    //Used for filtering results
 		final SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() { 
 		    @Override 
 		    public boolean onQueryTextChange(String newText) { 
 		    	// TODO actually search
 		    	if(!newText.isEmpty()) {
 			    	adapter.getFilter().filter(newText);
 					adapter.notifyDataSetChanged();
 			    	}
 		    	else adapter.setList(currList);
 		        return true; 
 		    } 
 
 		    @Override 
 		    public boolean onQueryTextSubmit(String query) { 
 		    	
 		        return true; 
 		    } 
 		}; 
 		
 		mSearch.setOnQueryTextListener(queryTextListener);
 		
 	    search = menu.findItem(R.id.main_search_bar);
 		//Set Login Title
 		MenuItem loginMenu = menu.findItem(R.id.menu_login);
 		String title = Login.currUser==null? "Login":"Logout";
 		loginMenu.setTitle(title);
 					
 		//Set Account Title
 		MenuItem accountMenu = menu.findItem(R.id.menu_account);
 		if(Login.currUser!=null) {
 			String account = LoginActivity.Email;
 			accountMenu.setTitle(account);
 		} else { 
 			accountMenu.setVisible(false);
 		}
 				
 		//Show/Hide admin button
 		if(Login.currUser==null || !Login.currUser.isAdmin()) {
 			MenuItem adminMenu = menu.findItem(R.id.menu_admin);
 			adminMenu.setVisible(false);
 		}
 		return true;
 	}
 	
 	/**
 	 * Creates the tabs, duh.
 	 */
 	public void createTabs() {
 		Tab tab;
 	    String tabName = "";
 	    for(int i = 0; i <5;i++) {
 	    	switch(i) { //Create tabs
 	    	case 0:
 	    		tabName = "ALL";
 	    		break;
 	    	case 1:
 	    		tabName = "LOST";
 	    		break;
 	    	case 2:
 	    		tabName = "FOUND";
 	    		break;
 	    	case 3: 
 	    		tabName = "DONATIONS";
 	    		break;
 	    	case 4:
 	    		tabName = "REQUESTS";
 	    		break;
 	    	}
 	    	tab = actionBar.newTab()
 		            .setText(tabName)
 		    		.setTabListener(new MainTabListener(tabName));
 	    	 actionBar.addTab(tab);
 	    }
 	}
 
 	/**
 	 * Returns the kind of Item displayed in this list.
 	 * @return An enumerated Type value
 	 */
 	public Type getItemType() {
 		return mType;
 	}
 	
 	 /**
      * Go to LoginActivity
      * @return boolean true when done
      */
     public boolean toLogIn() {
     	Intent toLogin = new Intent(this, LoginActivity.class);
     	startActivity(toLogin);
 		overridePendingTransition(R.anim.slide_up_modal, R.anim.hold);
 		return true;
     }
     
     /**
      * Go to AccountActivity
      * @return true when done
      */
     public boolean toAccount() {
     	Intent toAccount = new Intent(this, AccountActivity.class);
 		startActivity(toAccount);
 		overridePendingTransition(R.anim.slide_up_modal, R.anim.hold);
 		return true;
     }
     
     /**
      * Go to AdminActivity
      * @return true when done
      */
     public boolean toAdmin() {
     	Intent toAccount = new Intent(this, AdminActivity.class);
 		startActivity(toAccount);
 		overridePendingTransition(R.anim.slide_up_modal, R.anim.hold);
 		return true;
     }
     
     /**
      * Logout
      * @return boolean: true if logout, false if not
      */
     public boolean logOut() {
     	ErrorDialog toLogin =  new ErrorDialog(getString(R.string.main_message_signout), "Sign out", "Cancel");
 		AlertDialog.Builder temp = toLogin.getDialog(this,
 				new DialogInterface.OnClickListener() {
 
 			/**
 			 * logs current user out
 			 * @param dialog
 			 * @param id
 			 */
 			@Override
 			public void onClick(DialogInterface dialog, int id) {
 				//Clear current user
 				Login.currUser=null;
 				//Making sense is for squares
 				Intent newMain = getIntent();
 				newMain.setFlags(Intent.FLAG_ACTIVITY_TASK_ON_HOME);
 				newMain.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
 				finish(); 
 				startActivity(newMain);
 				overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
 				logOut = true;
 			}	
 		}, 
 		
 		new DialogInterface.OnClickListener() {
 
 			/**
 			 * keeps user loged in
 			 * @param DialogInterface dialog
 			 * @param int id
 			 */
 			@Override
 			public void onClick(DialogInterface dialog, int id) {
 				logOut = false;
 				}    
 		
 			});
 		temp.show();
     	return logOut;    	
     }
 }
