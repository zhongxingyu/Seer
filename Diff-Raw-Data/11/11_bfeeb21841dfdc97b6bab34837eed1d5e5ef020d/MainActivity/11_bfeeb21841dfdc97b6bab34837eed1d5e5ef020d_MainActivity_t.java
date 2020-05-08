 package edu.gatech.oad.rocket.findmythings;
 
 import android.app.ActionBar;
 import android.app.AlertDialog;
 import android.app.ActionBar.Tab;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 
 import edu.gatech.oad.rocket.findmythings.NonActivity.*;
 import edu.gatech.oad.rocket.findmythings.Helpers.*;
 /**
  * CS 2340 - FindMyStuff Android App
  *
  * An activity representing a list of Items. This activity has different
  * presentations for handset and tablet-size devices. On handsets, the activity
  * presents a list of items, which when touched, lead to a
  * {@link ItemDetailActivity} representing item details. On tablets, the
  * activity presents the list of items and item details side-by-side using two
  * vertical panes.
  * <p>
  * The activity makes heavy use of fragments. The list of items is a
  * {@link ItemListFragment} and the item details (if present) is a
  * {@link ItemDetailFragment}.
  * <p>
  * This activity also implements the required {@link ItemListFragment.Callbacks}
  * interface to listen for item selections.
  *
  * @author TeamRocket
  */
 public class MainActivity extends FragmentActivity implements
 		ItemListFragment.Callbacks {
 
 	/**
 	 * Whether or not the activity is in two-pane mode, i.e. running on a tablet
 	 * device.
 	 */
 	private boolean mTwoPane;
 
 	/**
 	 * The class of {@link Item} displayed in this list.
 	 */
 	public static Type mType = null;
 
 	/**
 	 * Identifies the item list fragment across instantiations.
 	 */
 	private static final String kItemListFragmentKey = "ItemListFragment";
 	
 	/**
 	 * Reference to the actionbar (the thing at the top of every activity)
 	 */
 	private ActionBar actionBar;
 	
 	/**
 	 * Used for the AlertDialog when user tries to sign out
 	 */
 	private boolean logOut = false;
 	
 
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_item_list);
 		
 		
 		//Create tabs and hide title
 		actionBar = getActionBar();
 	    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 	    actionBar.setDisplayShowTitleEnabled(true);
 	    createTabs();
 	   
 		getActionBar().setDisplayHomeAsUpEnabled(true);
 
 		Bundle extraInfo = getIntent().getExtras();
 		if (extraInfo != null && extraInfo.containsKey(Type.ID)) {
 			mType = Type.values()[extraInfo.getInt(Type.ID)];
 		}
 		
 		
 		ItemListFragment fragment;
 		if (savedInstanceState == null) {
 			// Create the detail fragment and add it to the activity
 			// using a fragment transaction.
 			Bundle arguments = new Bundle();
 			if (getIntent().getExtras() != null) arguments.putAll(getIntent().getExtras());
 
 			fragment = new ItemListFragment();
 			fragment.setArguments(arguments);
 			getSupportFragmentManager().beginTransaction().add(R.id.item_list_container, fragment, kItemListFragmentKey).commit();
 		} else {
 			fragment = (ItemListFragment)getSupportFragmentManager().findFragmentByTag(kItemListFragmentKey);
 		}
 
 		if (findViewById(R.id.item_detail_container) != null) {
 			// The detail container view will be present only in the
 			// large-screen layouts (res/values-large and
 			// res/values-sw600dp). If this view is present, then the
 			// activity should be in two-pane mode.
 			mTwoPane = true;
 
 			// In two-pane mode, list items should be given the
 			// 'activated' state when touched.
 			fragment.setActivateOnItemClick(true);
 		}
 		setTitle("Find My Things");
 		// TODO: If exposing deep links into your app, handle intents here.
 	}
 	
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event)  {
 		//Tells Activity what to do when back key is pressed
 	    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
 			return false;
 	    }
 	    return super.onKeyDown(keyCode, event);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 	    switch (item.getItemId()) {
 	        case R.id.item_list_submit:
 			return toSubmit();
 	        case R.id.menu_list_search:
 	        	Intent i = new Intent(MainActivity.this, FilterActivity.class);
 				startActivityForResult(i, 1);
 			    overridePendingTransition(R.anim.slide_up_modal, android.R.anim.fade_out);
 				return true;
 	        case R.id.menu_search:
 				Intent im = new Intent(MainActivity.this, Search_Main.class);
 				finish();
 				startActivity(im);
 			    overridePendingTransition(R.anim.slide_up_modal, android.R.anim.fade_out);
 				return true;
 			case R.id.menu_login: 
 				return Login.currUser==null? logIn():logOut(); 
 			case R.id.menu_account: 
 				return toAccount();
 			case R.id.menu_admin:
 				return toAdmin();
 	    }
 	    return super.onOptionsItemSelected(item);
 	}
 
 	@Override
 	protected void onResume() {
 	    super.onResume();
 	    String noOverKey = getString(R.string.key_nooverride_animation);
 	    Bundle extraInfo = getIntent().getExtras();
 		if (extraInfo == null || (extraInfo != null && !extraInfo.getBoolean(noOverKey))) {
 			//overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
 		}
 	}
 
 	/**
 	 * Callback method from {@link ItemListFragment.Callbacks} indicating that
 	 * the item with the given ID was selected.
 	 */
 	@Override
 	public void onItemSelected(String id) {
 		if (mTwoPane) {
 			// In two-pane mode, show the detail view in this activity by
 			// adding or replacing the detail fragment using a
 			// fragment transaction.
 			Bundle arguments = new Bundle();
 			arguments.putString(Item.ID, id);
 			arguments.putInt(Type.ID, mType.ordinal());
 			ItemDetailFragment fragment = new ItemDetailFragment();
 			fragment.setArguments(arguments);
 			getSupportFragmentManager().beginTransaction()
 					.replace(R.id.item_detail_container, fragment).commit();
 
 		} else {
 			// In single-pane mode, simply start the detail activity
 			// for the selected item ID.
 			Intent detailIntent = new Intent(this, ItemDetailActivity.class);
 			detailIntent.putExtra(Item.ID, id);
 			if(mType!=null)
 			detailIntent.putExtra(Type.ID, mType.ordinal());
 			startActivity(detailIntent);
 		}
 	}
 
 	/**
 	 * Opens a new Submit activity with the current type of item.
 	 */
 	public boolean toSubmit() {
 		if(Login.currUser!=null) {
 			Intent goToNextActivity = new Intent(MainActivity.this, Submit.class);
 			if(mType!=null)
				goToNextActivity.putExtra(Type.ID, mType.ordinal());
 			finish();
 			startActivity(goToNextActivity);
 			overridePendingTransition(R.anim.slide_up_modal, R.anim.hold);
 		}
 		else {
 			ErrorDialog toLogin =  new ErrorDialog("Must Sign-in to submit an item.", "Sign-in", "Cancel");
 			AlertDialog.Builder temp = toLogin.getDialog(this,
 				new DialogInterface.OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int id) {
 		            	Intent goToNextActivity = new Intent(getApplicationContext(), LoginWindow.class);
		            		goToNextActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 		               	startActivityForResult(goToNextActivity, 1);
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
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_item_list, menu);
 		getMenuInflater().inflate(R.menu.activity_main_menu, menu);
 		
 		//Set Login Title
 		MenuItem loginMenu = menu.findItem(R.id.menu_login);
 		String title = Login.currUser==null? "Login":"Logout";
 		loginMenu.setTitle(title);
 						
		//Set Account Title
 		MenuItem accountMenu = menu.findItem(R.id.menu_account);
 		if(Login.currUser!=null) {
 			String account = LoginWindow.Email;
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
 		    		.setTabListener(new TabHelp(tabName));
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
      * Go to LoginWindow
      */
     public boolean logIn() {
     	Intent toLogin = new Intent(MainActivity.this, LoginWindow.class);
 		finish();
 		startActivity(toLogin);
 		overridePendingTransition(R.anim.slide_up_modal, android.R.anim.fade_out);
 		return true;
     }
     
     /**
      * Go to MyAccount
      */
     public boolean toAccount() {
     	Intent toAccount = new Intent(MainActivity.this, MyAccount.class);
 		finish();
 		startActivity(toAccount);
 		overridePendingTransition(R.anim.slide_up_modal, android.R.anim.fade_out);
 		return true;
     }
     
     /**
      * Go to AdminActivity
      * @return
      */
     public boolean toAdmin() {
     	Intent toAccount = new Intent(MainActivity.this, AdminActivity.class);
 		finish();
 		startActivity(toAccount);
 		overridePendingTransition(R.anim.slide_up_modal, android.R.anim.fade_out);
 		return true;
     }
     
     /**
      * Logout
      */
     public boolean logOut() {
     	ErrorDialog toLogin =  new ErrorDialog("Really log out?", "Sign out", "Cancel");
 		AlertDialog.Builder temp = toLogin.getDialog(this,
 			new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int id) {
 				//Clear current user
 				Login.currUser=null;
				//Making sense is for squares
 				finish(); 
 				startActivity(getIntent());
 				overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
 				logOut = true;
 	            }	
 			}, 
 			new DialogInterface.OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int id) {
 					logOut = false;
 				}    
 		
 			});
 		temp.show();
     	return logOut;    	
     }
 }
