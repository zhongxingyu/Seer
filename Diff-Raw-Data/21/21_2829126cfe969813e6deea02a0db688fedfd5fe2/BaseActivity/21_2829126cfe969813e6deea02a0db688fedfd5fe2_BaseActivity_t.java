 package ch.unibe.ese.shopnote.core;
 
 import java.util.List;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.Configuration;
 import android.graphics.Color;
 import android.graphics.drawable.ColorDrawable;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.preference.PreferenceManager;
 import android.support.v4.app.ActionBarDrawerToggle;
 import android.support.v4.widget.DrawerLayout;
 import android.telephony.TelephonyManager;
 import android.view.View;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import ch.unibe.ese.shopnote.R;
 import ch.unibe.ese.shopnote.activities.VerifyNumberActivity;
 import ch.unibe.ese.shopnote.adapters.ShoppingListAdapter;
 import ch.unibe.ese.shopnote.core.sqlite.SQLitePersistenceManager;
 import ch.unibe.ese.shopnote.drawer.NavigationDrawer;
 import ch.unibe.ese.shopnote.share.SyncManager;
 
 /**
  * Extension of {@link Activity} that allows easy access to the global managers
  * (like {@link ListManager}, {@link FriendsManager} or {@link SyncManager}.
  */
 public class BaseActivity extends Activity {
 	
 	public static final String EXTRAS_LIST_ID = "listId";
 	public static final String EXTRAS_ITEM_ID = "itemId";
 	public static final String EXTRAS_ITEM_NAME = "itemName";
 	public static final String EXTRAS_ITEM_EDIT= "itemEdit";
 	public static final String EXTRAS_IS_RECIPE = "isRecipe";
 	public static final String EXTRAS_RECIPE_ID = "recipeId";
 	public static final String EXTRAS_FRIEND_ID = "friendId";
 	public static final String EXTRAS_FRIEND_NAME= "friendName";
 	public static final String EXTRAS_FRIEND_PHONENR= "friendNr";
 	public static final int INTENT_FRIEND_REQUEST= 011;
 	protected DrawerLayout drawMenu;
 	protected ActionBarDrawerToggle drawerToggle;
 	private boolean drawerToggleCreated;
 	private String title;
 	private static boolean isSyncing = false;
 	
 	//color variables
 	private String color = "white";
	private int actionBarColor;
 	private int backgroundColor;
 	private int navigationDrawerColor;
 	private int createTextBoxColor;
 	private int textColor;
 	private int textColor2;
 	private int listViewColor;
 	private int listViewDividerColor;
	private int buttonDividerColor;
 	public boolean colorUpdated = false;
 
 	
 	/**
 	 * Returns the singleton ListManager which is responsible for all the shopping lists
 	 * @return ListManager
 	 */
 	public ListManager getListManager() {
 		BaseApplication app = (BaseApplication) this.getApplication();
 		ListManager manager = app.getListManager();
 		if (manager == null) {
 			manager = new ListManager(new SQLitePersistenceManager(
 					getApplicationContext()));
 			app.setListManager(manager);
 		}
 		return manager;
 	}
 
 	/**
 	 * Returns the singleton FriendsManager which is responsible for the friends
 	 * @return FriendsManager
 	 */
 	public FriendsManager getFriendsManager() {
 		BaseApplication app = (BaseApplication) this.getApplication();
 		FriendsManager manager = app.getFriendsManager();
 		if (manager == null) {
 			manager = new FriendsManager(new SQLitePersistenceManager(
 					getApplicationContext()), getSyncManager(), this);
 			app.setFriendsManager(manager);
 		}
 		return manager;
 	}
 
 	/**
 	 * Returns the singleton SyncManager which is responsible for the communication between app and server
 	 * @return
 	 */
 	public SyncManager getSyncManager() {
 		BaseApplication app = (BaseApplication) this.getApplication();
 		SyncManager manager = app.getSyncManager();
 		if (manager == null) {
 			manager = new SyncManager();
 			app.setSyncManager(manager);
 		}
 		return manager;
 	}
 
 	/**
 	 * Shortcut to display a toast on an activity
 	 * @param text
 	 */
 	public void showToast(String text) {
 		Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
 	}
 
 	/**
 	 * Sets a text to a TextView.
 	 * <p>
 	 * If the TextView with the given id does not exist, a
 	 * {@link NullPointerException} is thrown.
 	 * 
 	 * @param id
 	 *            id of the TextView.
 	 * @param value
 	 *            The value to set.
 	 */
 	public void setTextViewText(int id, String value) {
 		TextView textView = (TextView) findViewById(id);
 		textView.setText(value);
 	}
 
 	/**
 	 * Gets the text from the TextView with the given id.
 	 * <p>
 	 * If the TextView with the given id does not exist, a
 	 * {@link NullPointerException} is thrown.
 	 * 
 	 * @param id
 	 *            id of the TextView.
 	 * @return The text in the view.
 	 */
 	public String getTextViewText(int id) {
 		TextView textView = (TextView) findViewById(id);
 		return textView.getText().toString().trim();
 	}
 	
 	/**
 	 * Checks if the android device has a connection to the internet
 	 * 
 	 * @return true if it is online
 	 */
 	public boolean isOnline() {
 	    ConnectivityManager cm =
 	        (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
 	    if (netInfo != null && netInfo.isConnectedOrConnecting()) {
 	        return true;
 	    }
 	    return false;
 	}
 	
 	/**
 	 * Shortcut to get the phonenumber of the current device
 	 * @return Phonenumber as String (Or empty String, if it is not supported by your phone)
 	 */
 	public String getMyPhoneNumber() {
 		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
 		boolean phoneNumberApproved = settings.getBoolean("phonenumberapproved", false);
 		if ("sdk".equals( Build.PRODUCT )) {
 			TelephonyManager tMgr =(TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
 			return tMgr.getLine1Number();
 		}
 		else if(!phoneNumberApproved) {
 			Intent intent = new Intent(this, VerifyNumberActivity.class);
 			startActivity(intent);
 		}
 		String phoneNumber = settings.getString("phonenumber", "-1");
 		return phoneNumber;
 	}
 	
 	/**
 	 * Needs to be implemented by every activity
 	 * Refreshes all the content it has
 	 */
 	public void refresh() {};
 	    
 	 /**
 	 * Force opens the soft keyboard
 	 */
 	public void openKeyboard() {
 		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
 		imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
 	}
 	
 	/**
 	 * Force closes the soft keyboard
 	 */
 	public void closeKeyboard() {
         InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
         imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
 	}
 	
 	/**
 	 * Closes the keyboard and finishes the current activity
 	 */
 	@Override
 	public void finish() {
 		closeKeyboard();
 		super.finish();
 	}
 	
 	/**
 	 * Calculates the number of bought items and the total number of items in a shopping list
 	 */
 	public void calculateItemCount(List<ShoppingList> lists, ShoppingListAdapter adapter) {
 		for (ShoppingList list: lists) {
 			List<Item> items = getListManager().getItemsFor(list);
 			
 			int boughtItems = 0;
 			int totalItems = 0;
 			
 			if (items != null) {
 				for (Item item: items) {
 					if (item.isBought())
 						boughtItems++;			
 				}
 				totalItems = items.size();
 			}
 			
 			adapter.setCount(boughtItems, totalItems);
 		}
 	}
 	
 	protected void createDrawerMenu() {
 		NavigationDrawer nDrawer = new NavigationDrawer();
 		drawMenu = nDrawer.constructNavigationDrawer(drawMenu, this);
 	}
 	
 	protected void createDrawerToggle() {
 		drawerToggle = new ActionBarDrawerToggle(
                 this,                  	/* host Activity */
                 drawMenu,         		/* DrawerLayout object */
                 R.drawable.ic_drawer,  	/* nav drawer icon to replace 'Up' caret */
                 R.string.app_name,  	/* "open drawer" description */
                 R.string.app_name  		/* "close drawer" description */
                 ) {
 
             /** Called when a drawer has settled in a completely closed state. */
             public void onDrawerClosed(View view) {
                 getActionBar().setTitle(title);
             }
 
             /** Called when a drawer has settled in a completely open state. */
             public void onDrawerOpened(View drawerView) {
             	title = (String) getActionBar().getTitle();
                 getActionBar().setTitle(R.string.app_name);
             }
         };
         
         // Set the drawer toggle as the DrawerListener
         drawMenu.setDrawerListener(drawerToggle);
         drawerToggleCreated = true;
 	}
 	
     @Override
     protected void onPostCreate(Bundle savedInstanceState) {
         super.onPostCreate(savedInstanceState);
         // Sync the toggle state after onRestoreInstanceState has occurred.
         if (drawerToggleCreated)
         	drawerToggle.syncState();
     }
     
     @Override
     public void onConfigurationChanged(Configuration newConfig) {
         super.onConfigurationChanged(newConfig);
         if (drawerToggleCreated)
         	drawerToggle.onConfigurationChanged(newConfig);
     }
     
 	@Override
 	protected void onPause() {
 		super.onPause();
 		if (drawMenu != null)
 			drawMenu.closeDrawers();
 	}
 	
 	/**
 	 * Creates a dialog which asks to invite friend
 	 * @param activity
 	 */
 	protected void friendPopup(final BaseActivity activity) {
 		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
 		
 		//add texts
 		builder.setMessage(R.string.friend_has_not_app_text)
 	       .setTitle(R.string.friend_has_not_app);
 		
 		// Add the buttons
 		builder.setPositiveButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
 	           public void onClick(DialogInterface dialog, int id) {
 	        	   //close window
 	           }
 	       });
 		builder.setNegativeButton(R.string.send, new DialogInterface.OnClickListener() {
 	           public void onClick(DialogInterface dialog, int id) {
 	        	   invitePopup(activity);
 	           }
 	       });
 
 
 		// Create the AlertDialog
 		AlertDialog dialog = builder.create();
 		dialog.show();
 		
 	}
 	
 	/**
 	 * creates a share popup with text R.string.inviteFriendMessage
 	 * @param activity
 	 */
 	protected void invitePopup(BaseActivity activity) {
 		Intent sendIntent = new Intent();
 		sendIntent.setAction(Intent.ACTION_SEND);
 		sendIntent.putExtra(Intent.EXTRA_TEXT, R.string.inviteFriendMessage);
 		sendIntent.setType("text/plain");
 		startActivity(sendIntent);
 	}
 	
 	
 	//Color functions to paint the program
 	/**
 	 * Updates colors for a listview
 	 * @param lv
 	 */
 	protected void updateThemeListView(ListView lv) {
 		if(!colorUpdated)
 			getColorSettings();
 		lv.setBackgroundColor(listViewColor);
 		lv.setDivider(new ColorDrawable(listViewDividerColor));
 		lv.setDividerHeight(2);
 	}
 	
 	/**
 	 * Updates color for a textbox
 	 * @param view
 	 */
 	protected void updateThemeTextBox(View view) {
 		if(!colorUpdated)
 			getColorSettings();
 		view.setBackgroundColor(createTextBoxColor);
 	}
 	
 	/**
 	 * Updates the save/abord buttons in the create activities
 	 * @param buttons
 	 * @param divider
 	 */
 	protected void updateSaveAbordButtons(LinearLayout buttons, LinearLayout shadow, LinearLayout divider) {
		buttons.setBackgroundColor(actionBarColor);
		divider.setBackgroundColor(buttonDividerColor);
		if(!color.equals("white"))
 			shadow.setBackgroundColor(listViewDividerColor);
 	}
 	
 	/**
 	 * Updates Colors for a layout, the actionbar and a drawer/in create activities the abord/save buttons
 	 * @param layout
 	 * @param actionBar
 	 * @param layoutDrawer
 	 */
 	protected void updateTheme(View layout, ActionBar actionBar, View layoutDrawer) {
 		updateTheme(layout, actionBar);
 		layoutDrawer.setBackgroundColor(navigationDrawerColor);		
 	}
 	
 	/**
 	 * Updates Colors for a layout and the actionbar, the standard activity set
 	 * @param layout
 	 * @param actionBar
 	 */
 	protected void updateTheme(View layout, ActionBar actionBar) {
 		if(!colorUpdated)
 			getColorSettings();
		actionBar.setBackgroundDrawable(new ColorDrawable(actionBarColor));
 		layout.setBackgroundColor(backgroundColor);
 	}
 	
 	/**
 	 * Updates text color of main attributes
 	 * @param textView
 	 */
 	public void updateTextColor(TextView textView) {
 		if(!colorUpdated)
 			getColorSettings();
 		textView.setTextColor(textColor);
 	}
 	
 	public void updateTextColorSecond(TextView textView) {
 		if(!colorUpdated)
 			getColorSettings();
 		textView.setTextColor(textColor2);
 	}
 	
 	/**
 	 * loads the actual color settings
 	 */
 	private void getColorSettings() {
 		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
 		String colorString = sharedPref.getString("color_setting", "white");
 		String colorChoosable[] = getResources().getStringArray(R.array.default_color_choice_names);
 		String colors[] = getResources().getStringArray(R.array.default_color_choice_white);
 
 		if(colorString.equals(colorChoosable[1])) {
 			colors = getResources().getStringArray(R.array.default_color_choice_chocolate);
 			color = "chocolate";
 		}
 		else if(colorString.equals(colorChoosable[2])) {
 			colors = getResources().getStringArray(R.array.default_color_choice_barbie);
 			color = "barbie";
 		}
 		if(colors == null) throw new IllegalStateException();
 		
 		this.backgroundColor = Color.parseColor(colors[0]);
 		this.navigationDrawerColor = Color.parseColor(colors[1]);
		this.actionBarColor = Color.parseColor(colors[2]);
 		this.textColor = Color.parseColor(colors[3]);
 		this.textColor2 = Color.parseColor(colors[4]);
 		this.createTextBoxColor = Color.parseColor(colors[5]);
 		this.listViewColor = Color.parseColor(colors[6]);
 		this.listViewDividerColor = Color.parseColor(colors[7]);
		this.buttonDividerColor = Color.parseColor(colors[8]);
 		this.colorUpdated = true;
 	}
 	
 	/**
 	 * Handler class to allow threads to update UI thread
 	 */
 	protected class SynchHandler extends Handler {
 		private BaseActivity activity;
 		
 		public SynchHandler(BaseActivity activity) {
 			super();
 			this.activity = activity;
 		}
 		
 		public void handleMessage(Message msg) {
       	  super.handleMessage(msg);  
       	  switch(msg.what) {
       	  		case 0:
       	  		 	refresh();
       	  		 	break;
       	  		case 1:
       	  			friendPopup(activity);
       	  			break;
       	  }
       	 
 		}
 	}
 	
 	protected void synchronize() {
 		if(isOnline()) {
 			getSyncManager().synchronise(this);
 		} else {
 			Toast.makeText(this, this.getString(R.string.no_connection),
 					Toast.LENGTH_SHORT).show();
 		}
 	}
 	
 	public void isSyncing(boolean b) {
 		isSyncing = b;
 	}
 	
 	public boolean isSyncing() {
 		return isSyncing;
 	}
 	
 }
