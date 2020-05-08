 package net.analogyc.wordiary;
 
 import net.analogyc.wordiary.models.BitmapWorker;
 import net.analogyc.wordiary.models.DBAdapter;
 import net.analogyc.wordiary.models.Photo;
 import android.content.Context;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.provider.MediaStore;
 import android.support.v4.app.DialogFragment;
 import android.support.v4.app.FragmentActivity;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.Toast;
 
 /**
  * Allows having the header in every page extending it
  * Gives basic CRUD functions for Entries and Days
  */
 public class BaseActivity extends FragmentActivity implements NewEntryDialogFragment.NewEntryDialogListener {
 
 	protected static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
 	
 	protected final int TOAST_DURATION_L = 2000;
 	protected final int TOAST_DURATION_S = 1000;
 
 	protected Uri imageUri;
 	protected boolean saveUri;
 	protected DBAdapter dataBase;
 	protected BitmapWorker bitmapWorker;
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
 		saveUri = false;
 		bitmapWorker = BitmapWorker.findOrCreateBitmapWorker(getSupportFragmentManager());
 		
 		//get an instance of database
 		dataBase = new DBAdapter(this);
 	}
 
 	/**
 	 * Brings back to the home page
 	 *
 	 * @param view
 	 */
 	public void onHomeButtonClicked(View view) {
 		if (!(this instanceof MainActivity)) {
 			Intent intent = new Intent(this, MainActivity.class);
 			startActivity(intent);
 		}
 	}
 
 	/**
	 * Displays the preferences panel, instead of using the preferences button
 	 *
 	 * @param view
 	 */
 	public void onOpenPreferencesClicked(View view) {
 		Intent intent = new Intent(this, PreferencesActivity.class);
 		startActivity(intent);
 	}
 
 	/**
 	 * Pops up a simple dialog to input a new entry
 	 *
 	 * @param view
 	 */
 	public void onNewEntryButtonClicked(View view){
 		NewEntryDialogFragment newFragment = new NewEntryDialogFragment();
 		newFragment.show(getSupportFragmentManager(), "newEntry");
 	}
 
 	/**
 	 * Opens the gallery
 	 *
 	 * @param view
 	 */
 	public void onOpenGalleryClicked(View view){
 		Intent intent = new Intent(this, GalleryActivity.class);
 		startActivity(intent);
 	}
 
 	/**
 	 * Runs the standard Android camera intent
 	 *
 	 * @param view
 	 */
 	public void onTakePhotoClicked(View view){
 		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
 		imageUri = Uri.fromFile(Photo.getOutputMediaFile(1));
 		intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
 		startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
 	}
 
 	/**
 	 * Takes results from: camera intent (100)
 	 *
 	 * @param requestCode
 	 * @param resultCode
 	 * @param data
 	 */
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
 			if (resultCode == RESULT_OK) {
 				dataBase.addPhoto(imageUri.getPath());
 				// Image captured and saved to fileUri specified in the Intent
 				Toast.makeText(this, getString(R.string.image_saved) + imageUri, TOAST_DURATION_L).show();
 				bitmapWorker.clearBitmapFromMemCache(imageUri.getPath());
 			}
 		}
 	}
 
 	/**
 	 * Positive input for the dialog for creating a new Entry
 	 *
 	 * @param dialog
 	 */
 	@Override
 	public void onDialogPositiveClick(DialogFragment dialog) {
 		Context context = getApplicationContext();
 		CharSequence text;
 
 		EditText edit=(EditText)dialog.getDialog().findViewById(R.id.newMessage);
 		String message = edit.getText().toString();
 
 		if(!message.equals("")){
 			text = "Message saved";
 			dataBase.addEntry(message, 0);
 			if (this instanceof MainActivity) {
 				((MainActivity) this).showEntries();
 			}
 		} else {
 			text = "Message not saved";
 		}
 
 		Toast toast1 = Toast.makeText(context, text, TOAST_DURATION_S);
 		toast1.show();
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState){
 		super.onSaveInstanceState(outState);
 		if(imageUri != null){
 			outState.putString("cameraImageUri", imageUri.toString());
 		}
 	}
 
 	@Override
 	protected void onRestoreInstanceState(Bundle savedInstanceState){
 		super.onRestoreInstanceState(savedInstanceState);
 		if(savedInstanceState.containsKey("cameraImageUri")){
 			imageUri = Uri.parse(savedInstanceState.getString("cameraImageUri"));
 		}
 	}
 
 	@Override
 	protected void onPause(){
 		dataBase.close();
 		super.onPause();
 	}
 
 	@Override
 	protected void onResume(){
 		super.onResume();
 	}
 }
