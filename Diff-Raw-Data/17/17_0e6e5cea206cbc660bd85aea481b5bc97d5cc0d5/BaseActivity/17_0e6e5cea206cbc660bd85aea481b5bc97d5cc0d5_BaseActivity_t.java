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
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class BaseActivity extends FragmentActivity implements NewEntryDialogFragment.NewEntryDialogListener {
 
 	protected static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
 
 	protected Uri imageUri;
 	protected DBAdapter dataBase;
 	protected BitmapWorker bitmapWorker;
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
 
 		dataBase = new DBAdapter(this);
 		bitmapWorker = BitmapWorker.findOrCreateBitmapWorker(getSupportFragmentManager());
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 			case R.id.action_settings:
 				Intent intent = new Intent(this, PreferencesActivity.class);
 				startActivity(intent);
 			default:
 				return super.onOptionsItemSelected(item);
 		}
 	}
 
 	public void onHomeButtonClicked(View view) {
 		if (!(this instanceof MainActivity)) {
 			Intent intent = new Intent(this, MainActivity.class);
 			startActivity(intent);
 		}
 	}
 
 	public void onOpenPreferencesClicked(View view) {
 		Intent intent = new Intent(this, PreferencesActivity.class);
 		startActivity(intent);
 	}
 
 	//method used when "newEntryButton" button is clicked
 	public void onNewEntryButtonClicked(View view){
 		NewEntryDialogFragment newFragment = new NewEntryDialogFragment();
 		newFragment.show(getSupportFragmentManager(), "newEntry");
 	}
 
 	public void onOpenGalleryClicked(View view){
 		Intent intent = new Intent(this, GalleryActivity.class);
 		startActivity(intent);
 	}
 
 	//method used when "takePhoto" button is clicked
 	public void onTakePhotoClicked(View view){
 		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
 		imageUri = Uri.fromFile(Photo.getOutputMediaFile(1));
 		intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
 		startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
 			if (resultCode == RESULT_OK) {
 				dataBase.addPhoto(imageUri.getPath());
 				// Image captured and saved to fileUri specified in the Intent
 				Toast.makeText(this, "Image saved to:\n" + imageUri, Toast.LENGTH_LONG).show();
 				bitmapWorker.clearBitmapFromMemCache(imageUri.getPath());
 			} else if (resultCode == RESULT_CANCELED) {
 				// User cancelled the image capture
 			} else {
 				// Image capture failed, advise user
 			}
 		}
 	}
 
 	@Override
 	public void onDialogPositiveClick(DialogFragment dialog) {
 		Context context = getApplicationContext();
 		CharSequence text;
 		int duration = Toast.LENGTH_SHORT;
 
 		EditText edit=(EditText)dialog.getDialog().findViewById(R.id.newMessage);
 		String message = edit.getText().toString();
 
 		if(message != ""){
 			text = "Message saved";
 			dataBase.addEntry(message, 0);
 			if (this instanceof MainActivity) {
 				((MainActivity) this).showEntries();
 			}
 		} else {
 			text = "Message not saved";
 		}
 
 		Toast toast1 = Toast.makeText(context, text, duration);
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
 		//entryAdapter.getCursor().close();
 		super.onPause();
 	}
 
 	@Override
 	protected void onResume(){
 		super.onResume();
 	}
 }
