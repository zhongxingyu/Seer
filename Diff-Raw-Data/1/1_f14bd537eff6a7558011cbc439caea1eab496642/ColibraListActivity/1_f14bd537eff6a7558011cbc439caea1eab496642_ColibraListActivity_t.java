 package com.cogniance.rodush;
 
 import com.cogniance.rodush.colibra.data.ColibraDbAdapter;
 import com.cogniance.rodush.colibra.data.ColibraDbHelper;
 import com.googlecode.androidannotations.annotations.EActivity;
 import com.googlecode.androidannotations.annotations.OptionsItem;
 import com.googlecode.androidannotations.annotations.OptionsMenu;
 import com.googlecode.androidannotations.annotations.sharedpreferences.Pref;
 
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 //import android.widget.ArrayAdapter;
 import android.view.View;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.SimpleCursorAdapter;
 import android.widget.Toast;
 
 //import android.widget.Toast;
 @EActivity
 @OptionsMenu(R.menu.book_list)
 public class ColibraListActivity extends ListActivity {
 	
 	@Pref
 	ColibraPreferences_ myPrefs;
 
 	protected ListView books_list;
 	long itmId = -1;
 
 	private Cursor myCursor;
 	private ListAdapter myListAdapter;
 
 	private static final String[] myBookContent = new String[] {
 			ColibraDbHelper._ID, ColibraDbHelper.BOOK_NAME,
 			ColibraDbHelper.BOOK_PUBLISH_YEAR,
 			ColibraDbHelper.BOOK_TECHNOLOGY_ID };
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		// check if user is authenticated
 		// if (!userIsAuthenticated) {
 		// @TODO: Forward to signing Activity
 		// }
 		
 		// Get view mode from the preferences
 		// whether it grid or list:
 		
 
 		myCursor = managedQuery(ColibraDbAdapter.BOOKS_URI, myBookContent,
 				null, null, ColibraDbHelper.BOOK_NAME); // order by book name
 
 		myListAdapter = new SimpleCursorAdapter(this, R.layout.list, myCursor,
 				new String[] { ColibraDbHelper.BOOK_NAME,
 						ColibraDbHelper.BOOK_PUBLISH_YEAR,
 						ColibraDbHelper.BOOK_TECHNOLOGY_ID }, new int[] {
 						R.id.book_name, R.id.book_year, R.id.book_technology });
 		this.setListAdapter(myListAdapter);
 		
 		
 	}
 	
 	public void onResume(){
 		super.onResume();
 		// Set view mode
 	}
 	
 	public void onListItemClick(ListView lv, View v, int position, long id) {
 
 		super.onListItemClick(lv, v, position, id);
 
 		// Get the item that was clicked
 		// Object o = this.getListAdapter().getItem(position);
 		myCursor.moveToPosition(position);
 
 		Toast.makeText(getApplicationContext(), "The selected item is: "
 				+ myCursor.getInt(0), Toast.LENGTH_SHORT).show();
 
 		// Start 'Show book details' activity and pass id of selected book to it
 		Intent intent = new Intent();
 		intent.setClass(this.getApplicationContext(),
 				ColibraBookDetailsActivity_.class);
 		intent.putExtra("book_id", myCursor.getInt(0));
 		intent.putExtra("book_name", myCursor.getString(1));
 		intent.putExtra("book_year", myCursor.getInt(2));
 		intent.putExtra("book_technology", myCursor.getInt(3));
 		startActivity(intent);
 	}
 	
 	@OptionsItem(R.id.menu_item_view_mode)
 	void viewModeClicked(){
 		myPrefs.viewMode().get();
 		// TODO: Trigger preferences dialog show-up
 //		myPrefs.viewMode().put();
 	}
 	
 	@OptionsItem(R.id.menu_item_cabinet)
 	void cabinetClicked(){
 		Intent intent = new Intent();
 		intent.setClass(this, ColibraCabinetActivity.class);
 		startActivity(intent);
 	}
 	
 	@OptionsItem(R.id.menu_item_exit)
 	void exitClicked(){
 		AlertDialog.Builder dialogBuider = new AlertDialog.Builder(this);
 		dialogBuider.setTitle("Exit");
 		dialogBuider.setMessage("Are you sure?");
 		dialogBuider.setCancelable(false); // do not allow to close with
 											// 'Back' button
 		dialogBuider.setPositiveButton("Yes", this.onExitConfirmListener);
 		dialogBuider.setNegativeButton("No", this.onExitCancelListener);
 		dialogBuider.show();
 	}
 
 	protected DialogInterface.OnClickListener onExitConfirmListener = new DialogInterface.OnClickListener() {
 		public void onClick(DialogInterface dialog, int id) {
 			// close current activity
 			ColibraListActivity.this.finish();
 		}
 	};
 
 	protected DialogInterface.OnClickListener onExitCancelListener = new DialogInterface.OnClickListener() {
 		public void onClick(DialogInterface dialog, int id) {
 			// close the dialog and return to current activity
 			dialog.cancel();
 		}
 	};
 
 }
