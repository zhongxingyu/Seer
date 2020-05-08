 package edu.gatech.cs2340_sp13.teamrocket.findmythings;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.text.TextUtils;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.EditText;
 
 public class Submit extends Activity {
 	
 	//UI references
 	private EditText description;
 	private EditText location;
 	private EditText reward;
 	private EditText iName;
 	
 	private View focusView;
 	
 	//Hold strings from the UI
 	private String desc, loc, name;
 	private int rward;
 
 	/**
 	 * Data source we submit to.
 	 */
 	private Controller control = Controller.shared();
 
 	/**
 	 * The list to submit this item to.
 	 */
 	private Item.Type mType = Item.Type.LOST;
 
 	/**
 	 * Category for this item, helper for {@link SubmitFrag}.
 	 */
 	private Item.Category mCategory = Item.Category.MISC;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 				
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_submit);
 		
 		//References the layout in activity_submit
 		iName = (EditText) findViewById(R.id.name);
 		description = (EditText) findViewById(R.id.description);
 		location = (EditText) findViewById(R.id.locationtext);
 		reward = (EditText) findViewById(R.id.rewardtext);
 		
 		Bundle extraInfo = getIntent().getExtras();
 		if (extraInfo != null && extraInfo.containsKey(Item.Type.ID)) {
 			mType = Item.Type.forInt(extraInfo.getInt(Item.Type.ID));
 		}
 		
 		// Hide the Up button in the action bar.
 		setupActionBar();
 		
 		setTitle("Submit an Item");
 		
 		SubmitFrag frag = (SubmitFrag) getFragmentManager().findFragmentById(R.id.submit_fragment);
 		frag.syncTypePref(mType);
		frag.syncCatPref(mCategory);
 	}
 		
 	/**
 	 * Set up the {@link android.app.ActionBar}.
 	 */
 	private void setupActionBar() {
 
 		getActionBar().setDisplayHomeAsUpEnabled(false);
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.submit, menu);
 		return true;
 	}
 	
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event)  {
 		//Tells Activity what to do when back key is pressed
 	    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
 	    	return false;
 	    }
 
 	    return super.onKeyDown(keyCode, event);
 	}
 
 	/**
 	 * Returns to Item List activity. Animation and ID helper.
 	 */
 	public boolean toItemList() {
 		Intent goToNextActivity = new Intent(getApplicationContext(), ItemListActivity.class);
 		goToNextActivity.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
 		goToNextActivity.putExtra(getString(R.string.key_nooverride_animation), true);
 		goToNextActivity.putExtra(Item.Type.ID, mType.ordinal());
 		finish();
 		startActivity(goToNextActivity);
 		overridePendingTransition(R.anim.hold, R.anim.slide_down_modal);
 		return true;
 	}
 	
 	public boolean checkforErrors() {
 		boolean cancel = false;
 		focusView = null;
 		
 		desc = description.getText().toString();
 		name = iName.getText().toString();
 		
 		//Check to see if name is empty
 		if (TextUtils.isEmpty(name.trim())) {
 			iName.setError(getString(R.string.error_field_required));
 			focusView = iName;
 			cancel = true;
 		}
 		
 		//Check to see if description is empty
 		if (TextUtils.isEmpty(desc.trim())) {
 			description.setError(getString(R.string.error_field_required));
 			focusView = description;
 			cancel = true;
 		} 
 		return cancel;
 		
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 			
 			switch (item.getItemId()) {
 			case R.id.submit_ok:
 				if (checkforErrors()) { //There was an error
 					focusView.requestFocus();
 					return false;
 				}	
 					
 				else {
 				loc = location.getText().toString();
 				rward = reward.getText().length() == 0 ? 0:Integer.parseInt(reward.getText().toString());
 	 		
 				Item temp = new Item(name,rward);
 				temp.setCategory(mCategory);
 				temp.setType(mType);
 				temp.setDescription(desc);
 				temp.setLoc(loc);
 				
 				control.addItem(temp);
 				
 				return toItemList();
 				}
 			case R.id.submit_cancel:
 				return toItemList();
 			case android.R.id.home:
 			// This ID represents the Home or Up button. In the case of this
 			// activity, the Up button is shown. Use NavUtils to allow users
 			// to navigate up one level in the application structure. For
 			// more details, see the Navigation pattern on Android Design:
 			//
 			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
 			//
 			
 			return true;	
 			}	
 		
 			
 			return super.onOptionsItemSelected(item);
 
 	}
 
 	/**
 	 * Sets the item type for this submission, i.e., the list
 	 * the item will be put on.
 	 * @param type An Item Type enumerated value.
 	 */
 	public void setItemType(Item.Type type) {
 		mType = type;
 	}
 
 	/**
 	 * Returns the list the item will be put on.
 	 * @return An Item Type enumerated value.
 	 */
 	public Item.Type getItemType() {
 		return mType;
 	}
 
 	/**
 	 * Sets the item category for this submission, used for filtering
 	 * @param type An Item Category enumerated value.
 	 */
 	public void setItemCategory(Item.Category type) {
 		mCategory = type;
 	}
 
 	/**
 	 * Returns the category for the item.
 	 * @return An Item Category enumerated value.
 	 */
 	public Item.Category getItemCategory() {
 		return mCategory;
 	}
 
 }
