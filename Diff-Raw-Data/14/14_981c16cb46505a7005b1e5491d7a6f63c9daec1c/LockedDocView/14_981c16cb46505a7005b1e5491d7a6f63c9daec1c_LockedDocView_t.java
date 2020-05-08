 package edu.caltech.cs141b.hw5.android;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.widget.EditText;
 import android.widget.Toast;
 import edu.caltech.cs141b.hw5.android.data.InvalidRequest;
 import edu.caltech.cs141b.hw5.android.data.LockExpired;
 import edu.caltech.cs141b.hw5.android.data.LockedDocument;
 import edu.caltech.cs141b.hw5.android.data.UnlockedDocument;
 import edu.caltech.cs141b.hw5.android.proto.CollabServiceWrapper;
 
 public class LockedDocView extends Activity {
 	// implements OnClickListener, OnFocusChangeListener {
 
 	// debugging
 	private static String TAG = "LockedDocView";
 
 	// the key that identifies the doc that is passed between activities
 	private static String intentDataKey = "doc";
 
 	// initial title + contents of new doc
 	private static String newDocTitle = "";
 	private static String newDocContents = "";
 
 	// makes server calls
 	private CollabServiceWrapper service;
 
 	// the current locked doc we are dealing with
 	private LockedDocument currDoc;
 
 	// textboxes
 	private EditText titleBox;
 	private EditText contentBox;
 
	private boolean srcBack = false;

 	private boolean saveFailed = false;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		Log.d(TAG, "created the locked doc view activity");
 
 		service = new CollabServiceWrapper();
 
 		setContentView(R.layout.lockeddocgui);
 		titleBox = (EditText) findViewById(R.id.title);
 		contentBox = (EditText) findViewById(R.id.content);
 
 		// sets the currDoc class variable
 		extractLockedDoc();
 
 		// displays currDoc
 		if (currDoc != null)
 			displayLockedDoc();
 		else {
 			Log.i(TAG, "Cannot display - doc is null.");
 
 			// inform the user that something went wrong
 			Toast errorMsg = Toast.makeText(this,
 					"Something went wrong - document is null.",
 					Toast.LENGTH_SHORT);
 			errorMsg.show();
 
 			// go back to list view
 			startActivity(new Intent(this, DocListView.class));
 		}
 	}
 
 	/**
 	 * Gets the locked doc that was passed to this activity.
 	 * 
 	 * @return
 	 */
 	private void extractLockedDoc() {
 		Log.i(TAG, "starting to extract locked doc");
 
 		Bundle extras = getIntent().getExtras();
 		LockedDocument doc = null;
 
 		// extract the doc
 		if (extras != null)
 			doc = (LockedDocument) extras.get(intentDataKey);
 
 		this.currDoc = doc;
 
 		Log.i(TAG, "extracted locked doc");
 	}
 
 	/**
 	 * Display the locked doc.
 	 */
 	private void displayLockedDoc() {
 		// assumes currDoc is non-null
 
 		Log.i(TAG, "starting to display locked doc");
 
 		// display the title and contents
 		titleBox.setText(currDoc.getTitle());
 		contentBox.setText(currDoc.getContents());
 
 		// set the cursor to the end of the title text box
 		titleBox.setSelection(titleBox.getText().length());
 
 		Log.i(TAG, currDoc.getTitle());
 	}
 
 	/**
 	 * Create a menu when user presses the physical 'menu' button.
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.lockedmenu, menu);
 		return true;
 	}
 
 	/**
 	 * Click handler for the menu buttons. Here the user has 3 options: create a
 	 * new doc, save the doc, and refresh the doc list. New doc and refresh list
 	 * should be enabled.
 	 */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		Log.i(TAG, "doc key: " + currDoc.getKey());
 
 		// which button did the user press?
 		switch (item.getItemId()) {
 		// create a new doc
 		case R.id.newDoc:
 			// if we had a new doc open before, dont release the lock on it
 			// since it hasnt been saved
 			if (currDoc.getKey() != null) {
 				// release lock since we are closing the current doc for which
 				// we likely hold the lock and are starting a new one
 				releaseLock();
 			}
 
 			LockedDocument newDoc = new LockedDocument(null, null, null,
 					newDocTitle, newDocContents);
 			this.currDoc = newDoc;
 
 			// do this activity again with a new doc
 			displayLockedDoc();
 			return true;
 
 			// refresh the doc list
 		case R.id.docList:
 			// if we had a new doc open before, dont release the lock on it
 			// since it hasnt been saved
 			if (currDoc.getKey() != null) {
 				// release lock since we are closing the current doc for which
 				// we likely hold the lock and are starting a new one
 				releaseLock();
 			}
 
 			// once we release the lock, go to the list view since this is what
 			// the user requested
 			startActivity(new Intent(this, DocListView.class));
 			return true;
 
 			// save this doc
 		case R.id.saveDoc:
 			saveDoc();
 			return true;
 
 		default:
 			return true;
 		}
 	}
 
 	/**
 	 * Called when the user exits this view.
 	 */
 	@Override
 	public void finish() {
 		Log.i(TAG, "quitting!");
 		// user is leaving this view, so release the lock
 		// of the current doc if it is not a new doc
 		if (!saveFailed && currDoc.getKey() != null) {
 			Log.i(TAG, "doc here should not be a new doc");
 			releaseLock();
 		}
 
 		super.finish();
 	}
 
 	/**
 	 * Saves the current doc.
 	 */
 	private void saveDoc() {
 		UnlockedDocument doc = null;
 
 		// set the title and contents of the doc we are trying to save
 		// to be what is in the text boxes at this moment (whatever the
 		// user input)
 		if (currDoc != null) {
 			try {
 				// if the user made no changes to the doc
 				if (currDoc.getTitle().equals(titleBox.getText().toString())
 						&& currDoc.getContents().equals(
 								contentBox.getText().toString())) {
 					Log.i(TAG, "no changes to the doc, so not saving");
 
 					// alert the user that we are not saving
 					Toast errorMsg = Toast.makeText(this,
 							"No document changes; not saving.",
 							Toast.LENGTH_SHORT);
 					errorMsg.show();
 				} else {
 					// set the to-be-saved doc to have the msot
 					// recent title and contents
 					currDoc.setTitle(titleBox.getText().toString());
 					currDoc.setContents(contentBox.getText().toString());
 
 					doc = service.saveDocument(currDoc);
 
 					// save if successful
 					if (doc != null)
 						saveComplete(doc);
 					else {
 						Log.i(TAG,
 								"Error - save could not complete (server returned null).");
 						throw new LockExpired();
 
 					}
 
 				}
 			} catch (LockExpired e) {
 				Log.i(TAG, "Caught LockExpired when trying to save doc.");
 
 				// alert the user that the save failed
 				Toast errorMsg = Toast.makeText(this,
 						"Save failed - lock was expired.", Toast.LENGTH_SHORT);
 				errorMsg.show();
 
 				// inform the user that the save failed
 				Toast msg = Toast.makeText(this, "Save failed. Lock expired",
 						Toast.LENGTH_LONG);
 				msg.show();
 
 				// start a new unlockedDocView activity that will
 				// display the doc that
 				// we had before since the save operation failed
 
 				String docKey = currDoc.getKey();
 
 				// set currDoc to null as to not release the lock when
 				// this activity terminates
 				saveFailed = true;
 				startActivity(new Intent(this, UnlockedDocView.class).putExtra(
 						intentDataKey, docKey));
 
 			} catch (InvalidRequest e) {
 				// alert the user that the save failed
 				Toast errorMsg = Toast.makeText(this,
 						"Save failed - invalid request.", Toast.LENGTH_SHORT);
 				errorMsg.show();
 
 				Log.i(TAG, "Caught InvalidRequest when trying to save doc.");
 				displayLockedDoc();
 			}
 		}
 	}
 
 	@Override
 	protected void onPause() {
		if (!srcBack) {
			finish();
		}
 		super.onPause();
 	}
 
 	/**
 	 * Called when the saveDoc operation completes successfully.
 	 * 
 	 * @param doc
 	 */
 	private void saveComplete(UnlockedDocument doc) {
 		Log.i(TAG, "saved the doc");
 
 		// inform the user that we saved the doc
 		Toast msg = Toast.makeText(this, "Document saved", Toast.LENGTH_SHORT);
 		msg.show();
 
 		// start a new unlockedDocView activity that will display the doc
 		// that was saved
 		startActivity(new Intent(this, UnlockedDocView.class).putExtra(
 				intentDataKey, doc.getKey()));
 
 	}
 
 	/**
 	 * Release the lock for the current doc.
 	 */
 	private void releaseLock() {
 		Log.i(TAG, "trying to release the lock");
 
 		if (currDoc != null) {
 			try {
 				service.releaseLock(currDoc);
 				Log.i(TAG, "released the lock");
 
 				// inform the user of the release
 				Toast errorMsg = Toast.makeText(this, "Lock released.",
 						Toast.LENGTH_SHORT);
 				errorMsg.show();
 			} catch (LockExpired e) {
 				// alert the user that the release failed
 				Toast errorMsg = Toast.makeText(this,
 						"Lock release failed - lock expired.",
 						Toast.LENGTH_SHORT);
 				errorMsg.show();
 
 				Log.i(TAG, "Caught LockExpired when trying to release lock.");
 			} catch (InvalidRequest e) {
 				// alert the user that the release failed
 				Toast errorMsg = Toast.makeText(this,
 						"Lock release failed - invalid request.",
 						Toast.LENGTH_SHORT);
 				errorMsg.show();
 
 				Log.i(TAG, "Caught InvalidRequest when trying to release lock.");
 			}
 		}
 	}
 
	// @Override
 	public void onBackPressed() {
		srcBack = true;
 		super.onBackPressed();
 	}
 
 }
