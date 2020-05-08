 package com.cs301w01.meatload.activities;
 
 import java.util.ArrayList;
 
 import com.cs301w01.meatload.R;
 import com.cs301w01.meatload.adapters.AlbumAdapter;
 import com.cs301w01.meatload.controllers.AlbumManager;
 import com.cs301w01.meatload.controllers.MainManager;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.AdapterView.OnItemClickListener;
 import com.cs301w01.meatload.model.Album;
 import com.cs301w01.meatload.model.gallery.AlbumGallery;
 import com.cs301w01.meatload.model.gallery.AllPicturesGallery;
 import com.cs301w01.meatload.model.gallery.GalleryData;
 import com.cs301w01.meatload.model.querygenerators.AlbumQueryGenerator;
 
 /**
  * Implements the logic in the Albums view of the Tab layout in Skindex.
  * 
  * @author Joel Burford
  */
 public class ViewAlbumsActivity extends Skindactivity {
 
 	private MainManager mainManager;
 	private AlbumManager albumManager;
 	private ListView albumListView;
 	private AlbumAdapter adapter;
 	private AlertDialog currentDialog;
 	private AlertDialog errorDialog;
 	private EditText currentEditText;
 	private boolean testing = false;
 
 	// private int[] adapterIDs = { R.id.itemName, R.id.itemValue };
 	// private String[] adapterCols = { "name", "numPictures" };
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.viewgroups);
 		mainManager = new MainManager(this);
 		albumManager = new AlbumManager(this);
 
 		refreshScreen();
 		createListeners();
 
 	}
 
 	public void update(Object model) {
 		refreshScreen();
 	}
 
 	@Override
 	public void finish() {
 		
 		if (errorDialog != null) {
 			if (errorDialog.isShowing()) {
 				errorDialog.dismiss();
 			}
 		}
 		
 		if (currentDialog != null) {
 			if (currentDialog.isShowing()) {
 				currentDialog.dismiss();
 			}
 		}
 
 		super.finish();
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		refreshScreen();
 		mainManager.setContext(this);
 	}
 
 	protected void createListeners() {
 		// on click listener
 		final Button takePicButton = (Button) findViewById(R.id.takePic);
 		takePicButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				// Perform action on click
 				takePicture();
 			}
 		});
 
 		final Button addAlbumButton = (Button) findViewById(R.id.newAlbum);
 		addAlbumButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				addAlbum(false); // false means not taking a picture, just
 									// adding album
 			}
 		});
 
 		// Below is the listener for a list button click.
 
 		albumListView.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 				openGallery(adapter.getItem(position));
 			}
 		});
 	}
 
 	public void refreshScreen() {
 		albumListView = (ListView) findViewById(R.id.albumListView);
 
 		ArrayList<Album> albumList = new ArrayList<Album>();
 		albumList.add(new Album("All Pictures", mainManager.getPictureCount(), -1));
 		albumList.addAll(albumManager.getAllAlbums());
 
 		adapter = new AlbumAdapter(this, R.layout.list_item, albumList);
 		albumListView.setAdapter(adapter);
 	}
 
 	protected void takePicture() {
 		// Display prompt
 
 		showCreateAlbumPrompt();
 
 	}
 
 	private void switchToTakePicture(Album album) {
 		if (testing) {
 			return;
 		}
 		
 		Intent goToGallery = new Intent();
 		goToGallery.setClassName("com.cs301w01.meatload",
 				"com.cs301w01.meatload.activities.GalleryActivity");
 		goToGallery.putExtra("gallery", new AlbumGallery(album));
 
 		startActivity(goToGallery);
 
 		Intent myIntent = new Intent();
 		myIntent.setClassName("com.cs301w01.meatload",
 				"com.cs301w01.meatload.activities.TakePictureActivity");
 		myIntent.putExtra("album", album);
 
 		startActivity(myIntent);
 	}
 
 	private void showCreateAlbumPrompt() {
 
 		AlertDialog.Builder alert = new AlertDialog.Builder(this);
 
 		alert.setTitle("New Album?");
 		alert.setMessage("Would you like to create a new album or choose an existing one?");
 
 		alert.setPositiveButton("Create", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int whichButton) {
 				addAlbum(true);
 			}
 		});
 
 		alert.setNegativeButton("Choose", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int whichButton) {
 				chooseAlbumPrompt();
 			}
 		});
 
 		currentDialog = alert.show();
 
 	}
 
 	private void chooseAlbumPrompt() {
 
 		final CharSequence[] albumNames = albumManager.albumsToStrings(albumManager.getAllAlbums());
 
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setTitle("Choose an Album");
 		builder.setItems(albumNames, new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int item) {
 				switchToTakePicture(albumManager.getAlbumByName((String) albumNames[item]));
 			}
 		});
 		currentDialog = builder.create();
 		currentDialog.show();
 	}
 
 	/**
 	 * Prompts the user to enter an album name and pick a set of tags. When the
 	 * user presses OK, gather the name and tags the user entered, and call
 	 * photoManager.addNewAlbum();
 	 * 
 	 * @return Name of the new album (for use in takePicture)
 	 * @see <a
 	 *      href="http://www.androidsnippets.com/prompt-user-input-with-an-alertdialog">
 	 *      http://www.androidsnippets.com/prompt-user-input-with-an-alertdialog</a>
 	 */
 	private void addAlbum(final Boolean takePicture) {
 
 		// Alert code snippet taken from:
 		// http://www.androidsnippets.com/prompt-user-input-with-an-alertdialog
 		AlertDialog.Builder alert = new AlertDialog.Builder(this);
 
 		alert.setTitle("New Album");
 		alert.setMessage("Enter the name of the new album");
 
 		// Set an EditText view to get user input
 		final EditText input = new EditText(this);
 		currentEditText = input;
 		currentEditText.setHint("Enter New Album Name");
 		input.setOnKeyListener(new View.OnKeyListener() {
 
 			public boolean onKey(View v, int keyCode, KeyEvent event) {
 				if (keyCode == KeyEvent.KEYCODE_ENTER) {
 					Button positiveButton = currentDialog.getButton(AlertDialog.BUTTON_POSITIVE);
 					positiveButton.requestFocus();
 					positiveButton.performClick();
 					return true;
 				}
 				return false;
 			}
 		});
 		alert.setView(input);
 
 		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int whichButton) {
 				String newAlbumName = input.getText().toString();
				newAlbumName = albumManager.addAlbum(newAlbumName);
 
 				if (newAlbumName.length() == 0) {
 					errorDialog("Adding album failed.  Album names must be "
 							+ albumManager.getMaxAlbumName() + " chars, not empty, and unique.");
 				} else if (takePicture) {
 					AlbumQueryGenerator albumQueryGenerator = new AlbumQueryGenerator(
 							ViewAlbumsActivity.this);
 					switchToTakePicture(albumQueryGenerator.getAlbumByName(newAlbumName));
 				}
 
 				refreshScreen();
 			}
 		});
 
 		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int whichButton) {
 				// Canceled.
 			}
 		});
 
 		currentDialog = alert.show();
 
 		Log.d("Alert Dialog", "Created.");
 
 	}
 
 	/**
 	 * Starts a new gallery activity using the GalleryManager object passed via
 	 * argument.
 	 * 
 	 * @param album
 	 *            The album to open the GalleryManager on
 	 */
 	private void openGallery(Album album) {
 		GalleryData gDat;
 		if (album.getID() < 0) {
 			gDat = new AllPicturesGallery();
 		} else {
 			gDat = new AlbumGallery(album);
 		}
 		Intent myIntent = new Intent();
 		myIntent.setClassName("com.cs301w01.meatload",
 				"com.cs301w01.meatload.activities.GalleryActivity");
 		myIntent.putExtra("gallery", gDat);
 
 		startActivity(myIntent);
 	}
 
 	/**
 	 * Pops up error dialog with given string in message.
 	 * 
 	 * @param err
 	 *            String containing error message
 	 */
 	private void errorDialog(String err) {
 		AlertDialog.Builder alert = new AlertDialog.Builder(this);
 		alert.setTitle("Error");
 		alert.setMessage(err);
 		errorDialog = alert.show();
 	}
 
 	/**
 	 * Used for JUnit testing.
 	 * 
 	 * @return The current AlertDialog
 	 */
 	public AlertDialog getCurrentDialog() {
 		return currentDialog;
 	}
 
 	public void setDialogEditText(String text) {
 		currentEditText.setText(text);
 	}
 
 	public void performDialogClick(boolean button) {
 		if (button == true) {
 			currentDialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
 		} else {
 			currentDialog.getButton(AlertDialog.BUTTON_NEGATIVE).performClick();
 		}
 	}
 
 	public void performListViewClick(int position) {
 		testing = true;
 		ListView list = currentDialog.getListView();
 		list.performItemClick(null, position, list.getAdapter().getItemId(position));
 	}
 
 }
