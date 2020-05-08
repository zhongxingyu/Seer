 package com.cs301w01.meatload.activities;
 
 import android.widget.EditText;
 import android.widget.TextView;
 import com.cs301w01.meatload.R;
 import com.cs301w01.meatload.controllers.AlbumManager;
 import com.cs301w01.meatload.controllers.GalleryManager;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import com.cs301w01.meatload.model.Album;
 import com.cs301w01.meatload.model.gallery.AlbumGallery;
 
 /**
  * View Activity which uses AlbumManager to modify Albums.
  * <p>
  * TODO: Carriage Return (Enter key) needs to be handled by the OnKeyListener of
  * the AlbumName EditText.
  * 
  * @author Joel Burford
  */
 public class EditAlbumActivity extends Skindactivity {
 
 	private AlbumManager albumManager;
 	private GalleryManager galleryManager;
 	private Album album;
 	private AlertDialog currentDialog;
 
 	@Override
 	public void update(Object model) {
 
 	}
 
 	@Override
 	public void finish() {
 		if (currentDialog != null) {
 			if (currentDialog.isShowing()) {
 				currentDialog.dismiss();
 			}
 		}
 
 		super.finish();
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.edit_album);
 
 		Bundle b = getIntent().getExtras();
 		AlbumGallery aGal = (AlbumGallery) b.getSerializable("gallery");
 
 		album = aGal.getAlbum(this);
 		galleryManager = new GalleryManager(this, new AlbumGallery(album));
 
 		createListeners();
 		populate();
 	}
 
 	private void populate() {
 		EditText name = (EditText) findViewById(R.id.edit_album_name);
 		name.setText(album.getName());
 
 		TextView numView = (TextView) findViewById(R.id.text_num_pics);
 		numView.setText("" + galleryManager.getPictureGallery().size());
 
 		TextView dateView = (TextView) findViewById(R.id.text_date);
 		dateView.setText(album.getDate().toString());
 	}
 
 	private void createListeners() {
 		final Button saveAlbumButton = (Button) findViewById(R.id.save_album_button);
 		saveAlbumButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View view) {
 				if (updateAlbumName() > 0) {
 					EditAlbumActivity.this.setResult(RESULT_OK, null);
 					finish();
 				}
 			}
 		});
 
 		final Button deleteAlbumButton = (Button) findViewById(R.id.delete_album_button);
 		deleteAlbumButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View view) {
 				deleteAlbumDialog();
 			}
 		});
 	}
 
 	/**
 	 * Checks to see if the user has made changes to the name of the album, if
 	 * so updates the album name in the DB.
 	 */
 	private int updateAlbumName() {
 		final EditText albumName = (EditText) findViewById(R.id.edit_album_name);
 		if (albumName.length() == 0) {
 			errorDialog("Invalid album name, no changes saved.");
 			return -1;
 		}
 		String newAlbumName = albumName.getText().toString().trim();
 		String oldAlbumName = album.getName();
 
 		if (newAlbumName.length() == 0) {
 			errorDialog("Album name cannot be empty, no changes saved.");
 			return -1;
 		} else if (newAlbumName.length() > albumManager.getMaxAlbumName()) {
 			newAlbumName = newAlbumName.substring(0, albumManager.getMaxAlbumName());
 		}
 
 		if (oldAlbumName.equals(newAlbumName)) {
 			return 1;
 		}
 
 		if (albumManager.albumExists(newAlbumName)) {
 			errorDialog("Album name already exists, no changes saved.");
 			return -1;
 		}
 
 		albumManager.changeAlbumName(newAlbumName, album);
 		return 1;
 	}
 
 	/**
 	 * Opens a dialog warning the user about deleting the pictures in the album.
 	 */
 	private void deleteAlbumDialog() {
 		AlertDialog.Builder alert = new AlertDialog.Builder(this);
 
 		alert.setTitle("Confirm");
 		alert.setMessage("This will delete all pictures associated with this album. "
 				+ "Are you sure you want to delete this album?");
 
 		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int whichButton) {
 				EditAlbumActivity.this.setResult(RESULT_CANCELED, null);
 				albumManager.deleteAlbum((int) album.getID());
 				finish();
 			}
 		});
 
 		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int whichButton) {
 				dialog.dismiss();
 			}
 		});
 
 		alert.show();
 
 	}
 
 	/**
 	 * Pops up error dialog with given string in message.
 	 * 
 	 * @param err
 	 *            Error message to put into the error dialog
 	 */
 	private void errorDialog(String err) {
 		AlertDialog.Builder alert = new AlertDialog.Builder(this);
 		alert.setTitle("Error");
 		alert.setMessage(err);
 		currentDialog = alert.show();
 	}
 }
