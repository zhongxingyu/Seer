 package edu.gatech.oad.rocket.findmythings;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 
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
 public class ItemListActivity extends FragmentActivity implements
 		ItemListFragment.Callbacks {
 
 	/**
 	 * Whether or not the activity is in two-pane mode, i.e. running on a tablet
 	 * device.
 	 */
 	private boolean mTwoPane;
 
 	/**
 	 * The class of {@link Item} displayed in this list.
 	 */
 	private Type mType = Type.LOST;
 
 	/**
 	 * Identifies the item list fragment across instantiations.
 	 */
 	private static final String kItemListFragmentKey = "ItemListFragment";
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_item_list);
 
 
 		getActionBar().setDisplayHomeAsUpEnabled(true);
 
 		Bundle extraInfo = getIntent().getExtras();
 		if (extraInfo != null && extraInfo.containsKey(Type.ID)) {
 			mType = Type.values()[extraInfo.getInt(Type.ID)];
 		}
 		
 		setTitle(EnumHelper.localizedFromArray(this, R.array.item_list_titles, mType));
 
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
 
 		// TODO: If exposing deep links into your app, handle intents here.
 	}
 
 	/**
 	 * Animation helper. Goes back from an Item List to the {@link MainActivity}.
 	 */
 	private void goToParentActivity() {
 	Intent goToNextActivity = new Intent(this, MainActivity.class);
 	goToNextActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
         startActivity(goToNextActivity);
 	    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
 	}
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event)  {
 		//Tells Activity what to do when back key is pressed
 	    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
 		goToParentActivity();
 	        return true;
 	    }
 
 	    return super.onKeyDown(keyCode, event);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 	    switch (item.getItemId()) {
 	        case android.R.id.home:
 			goToParentActivity();
 	            return true;
 	        case R.id.item_list_submit:
 			return toSubmit();
 	    }
 	    return super.onOptionsItemSelected(item);
 	}
 
 	@Override
 	protected void onResume() {
 	    super.onResume();
 
 	    String noOverKey = getString(R.string.key_nooverride_animation);
 	    Bundle extraInfo = getIntent().getExtras();
 		if (extraInfo == null || (extraInfo != null && !extraInfo.getBoolean(noOverKey))) {
 			overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
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
 			detailIntent.putExtra(Type.ID, mType.ordinal());
 			startActivity(detailIntent);
 		}
 	}
 
 	/**
 	 * Opens a new Submit activity with the current type of item.
 	 */
 	public boolean toSubmit() {
 		if(Login.currUser!=null) {
 			Intent goToNextActivity = new Intent(ItemListActivity.this, Submit.class);
 			goToNextActivity.putExtra(Type.ID, mType.ordinal());
 			startActivity(goToNextActivity);
 			overridePendingTransition(R.anim.slide_up_modal, R.anim.hold);
 		}
 		else {
			ErrorDialog toLogin =  new ErrorDialog("Must Sign-in to submit an item.");
 			AlertDialog.Builder temp = toLogin.getDialog(this,
 				new DialogInterface.OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int id) {
 		            	Intent goToNextActivity = new Intent(getApplicationContext(), LoginWindow.class);
 		            	finish();
 		            	startActivity(goToNextActivity);
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
 		return true;
 	}
 
 	/**
 	 * Returns the kind of Item displayed in this list.
 	 * @return An enumerated Type value
 	 */
 	public Type getItemType() {
 		return mType;
 	}
 }
