 package com.autburst.picture;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 
 import org.apache.commons.codec.binary.Base64;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.text.format.DateFormat;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.RadioGroup;
 import android.widget.TextView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 
 public class MainActivity extends Activity implements OnItemClickListener,
 		OnCheckedChangeListener {
 
 	private static final String TAG = MainActivity.class.getSimpleName();
 
 	static final int DIALOG_CREATE_ALBUM_ID = 0;
 	static final int DIALOG_SHOW_ALBUM_ID = 1;
 	static final int DIALOG_CREATE_ALBUM_INPUT_ERROR = 2;
 
 	private ListView albumsList;
 	private ArrayAdapter<String> adapter;
 	private LayoutInflater inflater;
 
 	private ImageButton deleteButton;
 	private ImageButton openButton;
 
 	private List<Button> openAlbumButtons = new ArrayList<Button>();
 
 	private List<CheckBox> deleteAlbumCheckBoxes = new ArrayList<CheckBox>();
 	private LinearLayout deletionPanel;
 	private Button cancelDeletion;
 	private Button deleteCheckedAlbums;
 
 	private List<Integer> checkedAlbums = new ArrayList<Integer>();
 
 	//gui status for disabling or enabling the delete and open buttons
 	private boolean openButtonsVisible = false;
 	private boolean deleteCheckboxesVisible = false;
 	private boolean hasAnyPics = true;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		albumsList = (ListView) findViewById(R.id.albumList);
 
 		inflater = getLayoutInflater();
 
 		List<String> albumsAsList = Utilities.getAlbumsAsList();
 		adapter = new AlbumAdapter(inflater, this, R.layout.row, albumsAsList);
 		adapter.sort(Utilities.getAlbumNameComparator());
 
 		albumsList.setAdapter(adapter);
 		albumsList.setOnItemClickListener(this);
 
 		cancelDeletion = (Button) findViewById(R.id.cancelDeletion);
 		cancelDeletion.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 				hideDeletionPanelAndCB();
 			}
 		});
 
 		deleteCheckedAlbums = (Button) findViewById(R.id.deleteCheckedAlbums);
 		deleteCheckedAlbums.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 				List<String> albums = Utilities.getAlbumsAsList();
 				Collections.sort(albums, Utilities.getAlbumNameComparator());
 
 				for (int i = 0; i < checkedAlbums.size(); i++) {
 					// get checked rows
 					int rowIndex = checkedAlbums.get(i);
 					String albumName = albums.get(rowIndex);
 
 					Log.d(TAG, "delete album: "
 							+ albumName
 							+ " at rowIndex: "
 							+ rowIndex
 							+ " decodedName: "
 							+ new String(Base64.decodeBase64(albumName
 									.getBytes())));
 
 					// delete albums
 					// remove files on sdcard
 					File album = Utilities.getAlbumDirectory(albumName);
 					File[] file = album.listFiles();
 					for (File file2 : file) {
 						file2.delete();
 						Log.d(TAG, "deleted file: " + file2.getName());
 					}
 					album.delete();
 
 					// remove shared preferences entry
 					SharedPreferences preferences = getSharedPreferences(
 							Utilities.PIC_STORE, 0);
 					Editor edit = preferences.edit();
 					String pref0 = albumName + ".id";
 					String pref1 = albumName + ".portrait";
 					String pref2 = albumName + ".videoId";
 
 					if (preferences.contains(pref0)) {
 						edit.remove(pref0);
 						Log.d(TAG, "deleted pref .id");
 					}
 
 					if (preferences.contains(pref1)) {
 						edit.remove(pref1);
 						Log.d(TAG, "deleted pref .portrait");
 					}
 
 					if (preferences.contains(pref2)) {
 						edit.remove(pref2);
 						Log.d(TAG, "deleted pref .videoId");
 					}
 
 					edit.commit();
 
 					adapter.remove(albumName);
 				}
 
 				// hide deletion panel and checkboxes
 				hideDeletionPanelAndCB();
 
 				// reload list
 				adapter.notifyDataSetChanged();
 				
 				//check if there are still albums, otherwise disable delete button
 				if (Utilities.getAlbumsAsList().size() == 0) {
 					deleteButton.setClickable(false);
 					Log.d(TAG, "disabled delete button");
 				}
 				
 				//check if there are still pics in any album, otherwise disable open button
 				if(!Utilities.hasAnyAlbumPics()) {
 					hasAnyPics = false;
 					openButton.setClickable(false);
 				}
 			}
 		});
 
 		deletionPanel = (LinearLayout) findViewById(R.id.deletionPanel);
 		deleteButton = (ImageButton) findViewById(R.id.deleteButton);
 
 		deleteButton.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 				if (hasAnyPics) {
 					// en-/disable open button
 					if (openButton.isClickable()) {
 						openButton.setClickable(false);
 						Log.d(TAG, "delete button set openbutton clickable false");
 					} else {
 						openButton.setClickable(true);
 						Log.d(TAG, "delete button set openbutton clickable true");
 					}
 				}
 				
 				if(deleteCheckboxesVisible) {
 					//hide checkboxes
 					deleteCheckboxesVisible = false;
 					
 					// deletionPanel
 					if (deletionPanel.getVisibility() == View.VISIBLE) {
 						Animation myFadeInAnimation = AnimationUtils.loadAnimation(
 								MainActivity.this, R.anim.slight_bottom);
 						deletionPanel.startAnimation(myFadeInAnimation);
 						deletionPanel.setVisibility(View.GONE);
 						Log.d(TAG, "set deletionPanel gone");
 					} 
 					
 					// make checkboxes visible or invisible
 					for (CheckBox cbToDelete : deleteAlbumCheckBoxes) {
 						if (cbToDelete.getVisibility() == View.VISIBLE) {
 							Animation myFadeInAnimation = AnimationUtils
 									.loadAnimation(MainActivity.this,
 											R.anim.slight_left_cb);
 							cbToDelete.startAnimation(myFadeInAnimation); // Set
 							// animation
 							// to
 							// your
 							// ImageView
 							cbToDelete.setVisibility(View.GONE);
 							if (cbToDelete.isChecked())
 								cbToDelete.setChecked(false);
 
 							Log.d(TAG, "set delete checkbox GONE");
 						}
 					}
 					
 				} else {
 					//show checkboxes
 					
 					deleteCheckboxesVisible = true;
 					
 					// deletionPanel
 					if (deletionPanel.getVisibility() == View.GONE) {
 						Animation myFadeInAnimation = AnimationUtils.loadAnimation(
 								MainActivity.this, R.anim.slight_top);
 						deletionPanel.startAnimation(myFadeInAnimation);
 
 						deletionPanel.setVisibility(View.VISIBLE);
 						Log.d(TAG, "set deletionPanel visible");
 					}
 					
 					for (CheckBox cbToDelete : deleteAlbumCheckBoxes) {
 						if (cbToDelete.getVisibility() == View.GONE) {
 							// checkboxes
 
 							Animation myFadeInAnimation = AnimationUtils
 									.loadAnimation(MainActivity.this,
 											R.anim.slight_right_cb);
 							cbToDelete.startAnimation(myFadeInAnimation); // Set
 							// animation
 							// to
 							// your
 							// ImageView
 							cbToDelete.setVisibility(View.VISIBLE);
 
 							Log.d(TAG, "set delete checkbox visible");
 						}
 					}
 				}			
 			}
 		});
 		
 		// when no album exist disable button
 		Log.d(TAG, "No of albums on device: " + albumsAsList.size());
 		if (albumsAsList.size() == 0) {
 			deleteButton.setClickable(false);
 			Log.d(TAG, "disabled delete button");
 		}
 
 
 		openButton = (ImageButton) findViewById(R.id.openButton);
 
 		openButton.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 				if (openButtonsVisible) {
 					// hide open Buttons
 					deleteButton.setClickable(true);
 
 					// make buttons gone
 					for (Button openAlbum : openAlbumButtons) {
 						if (openAlbum.getVisibility() == View.VISIBLE) {
 							Animation myFadeInAnimation = AnimationUtils
 									.loadAnimation(MainActivity.this,
 											R.anim.slight_right);
 							openAlbum.startAnimation(myFadeInAnimation); // Set
 							// animation
 							// to
 							// your
 							// ImageView
 							openAlbum.setVisibility(View.GONE);
 						}
 					}
 					openButtonsVisible = false;
 				} else {
 					// show open buttons
 					deleteButton.setClickable(false);
 
 					// make buttons visible
 					for (Button openAlbum : openAlbumButtons) {
 						if (openAlbum.getVisibility() == View.GONE) {
 							Animation myFadeInAnimation = AnimationUtils
 									.loadAnimation(MainActivity.this,
 											R.anim.slight_left);
 							openAlbum.startAnimation(myFadeInAnimation); // Set
 							// animation
 							// to
 							// your
 							// ImageView
 							openAlbum.setVisibility(View.VISIBLE);
 						}
 					}
 
 					openButtonsVisible = true;
 				}
 
 			}
 		});
 		
 
 		// when no album exist disable button
 		hasAnyPics = Utilities.hasAnyAlbumPics();
 		Log.d(TAG, "pics already exist: " + hasAnyPics);
 		
 		if (!hasAnyPics) {
 			openButton.setClickable(false);
 			Log.d(TAG, "disabled open button");
 		}
 	}
 
 	private void hideDeletionPanelAndCB() {
 		deleteCheckboxesVisible = false;
 		
 		// enable open button
 		if (!openButton.isClickable() && hasAnyPics)
 			openButton.setClickable(true);
 
 		if (deletionPanel.getVisibility() == View.VISIBLE) {
 			Animation myFadeInAnimation = AnimationUtils.loadAnimation(
 					MainActivity.this, R.anim.slight_bottom);
 			deletionPanel.startAnimation(myFadeInAnimation);
 			deletionPanel.setVisibility(View.GONE);
 		}
 
 		// make checkboxes invisible
 		for (CheckBox cbToDelete : deleteAlbumCheckBoxes) {
 			if (cbToDelete.getVisibility() == View.VISIBLE) {
 				Animation myFadeInAnimation = AnimationUtils.loadAnimation(
 						MainActivity.this, R.anim.slight_left_cb);
 				cbToDelete.startAnimation(myFadeInAnimation); // Set animation
 				// to your
 				// ImageView
 				cbToDelete.setVisibility(View.GONE);
 				if (cbToDelete.isChecked())
 					cbToDelete.setChecked(false);
 			}
 		}
 	}
 
 	private class AlbumAdapter extends ArrayAdapter<String> {
 
 		private LayoutInflater inflater;
 		private List<String> albums;
 
 		public AlbumAdapter(LayoutInflater inflater, Context context,
 				int layout, List<String> albums) {
 			super(context, layout, albums);
 			this.inflater = inflater;
 			this.albums = albums;
 
 			openAlbumButtons.clear();
 			deleteAlbumCheckBoxes.clear();
 			super.setNotifyOnChange(false);
 
 			Log.d(TAG, "constructed AlbumAdapter");
 		}
 
 		@Override
 		public int getCount() {
 			Log.d(TAG, "getCount " + (albums.size() + 1));
 			return albums.size() + 1;
 		}
 
 		@Override
 		public void notifyDataSetChanged() {
 			Log.d(TAG, "notifyDataSetChanged");
 
 			openAlbumButtons.clear();
 			deleteAlbumCheckBoxes.clear();
 			super.notifyDataSetChanged();
 			super.setNotifyOnChange(false);
 		}
 
 		@Override
 		public View getView(final int position, View convertView,
 				ViewGroup parent) {
 			Log.d(TAG, "getView: " + position);
 
 			View row;
 
 			if (position == 0) {
 				if (null == convertView
 						|| convertView.getId() != R.id.createAlbumRow) {
 					row = inflater.inflate(R.layout.create_album_row, null);
 				} else
 					row = convertView;
 
 				Log.d(TAG, "LOAD create album row!");
 
 			} else {
 				final int albumListPosition = position - 1;
 
 				if (null == convertView
 						|| convertView.getId() == R.id.createAlbumRow) {
 					row = inflater.inflate(R.layout.row, null);
 				} else {
 					row = convertView;
 				}
 
 				ImageView firstImage = (ImageView) row
 						.findViewById(R.id.rowImage);
 
 				// get checkbox to delete an album
 				CheckBox cbToDelete = (CheckBox) row
 						.findViewById(R.id.cbToDelete);
 				cbToDelete.setTag(albumListPosition);
 				cbToDelete.setOnCheckedChangeListener(MainActivity.this);
 				addCheckBoxToDelete(cbToDelete);
 
 				BitmapFactory.Options options = new BitmapFactory.Options();
 				options.inSampleSize = 10;
 				options.inPurgeable = true;
 				File[] listFiles = Utilities.getAlbumDirectory(
 						this.getItem(albumListPosition)).listFiles();
 				if (listFiles != null && listFiles.length > 0) {
 					File selectedPic = listFiles[0];
 
 					Bitmap bm = null;
 					try {
 						bm = Bitmap.createBitmap(BitmapFactory.decodeFile(
 								selectedPic.getAbsolutePath(), options));
 					} catch (Exception e) {
 						Log
 								.d(
 										TAG,
 										"normal behavior; async image saver was still saving the image! So it cannot be displayed yet!");
 					}
 					firstImage.setImageBitmap(bm);
 
 					// button to open an album
 					Button openAlbum = (Button) row
 							.findViewById(R.id.openAlbum);
 					addOpenAlbumButton(openAlbum);
 					openAlbum.setOnClickListener(new View.OnClickListener() {
 
 						@Override
 						public void onClick(View arg0) {
 							// open gallery
 							Intent intent = new Intent(MainActivity.this,
 									GalleryActivity.class);
 							intent.putExtra("albumName",
 									getItem(albumListPosition));
 							startActivity(intent);
 
 							// set the openAlbum buttons to gone
 							for (Button openAlbum : openAlbumButtons)
 								openAlbum.setVisibility(View.GONE);
							
							deleteButton.setClickable(true);
							openButtonsVisible = false;
 
 						}
 					});
 
 				} else
 					firstImage.setImageBitmap(null);
 
 				// set album name
 				TextView albumName = (TextView) row
 						.findViewById(R.id.rowAlbumName);
 				String decodedName = new String(Base64.decodeBase64(this
 						.getItem(albumListPosition).getBytes()));
 				albumName.setText(decodedName);
 
 				// set date of last taken picture in row
 				Date newestPic = null;
 				for (File file : listFiles) {
 					Date creationDate = new Date(file.lastModified());
 					if (newestPic == null || creationDate.after(newestPic))
 						newestPic = creationDate;
 				}
 
 				TextView albumLastPicture = (TextView) row
 						.findViewById(R.id.rowAlbumLastPicture);
 				if (newestPic != null) {
 					albumLastPicture.setText("letztes Foto vom "
 							+ DateFormat.format("dd.MM.yyyy kk:mm", newestPic));
 				} else {
 					albumLastPicture.setText("letztes Foto vom");
 				}
 
 				Log.d(TAG, "LOAD ROW: " + decodedName + " encoded name: "
 						+ this.getItem(albumListPosition));
 			}
 			return row;
 		}
 	}
 
 	private void addCheckBoxToDelete(CheckBox cb) {
 		if (deleteAlbumCheckBoxes.contains(cb))
 			return;
 		else
 			deleteAlbumCheckBoxes.add(cb);
 	}
 
 	private void addOpenAlbumButton(Button button) {
 		if (openAlbumButtons.contains(button))
 			return;
 		else
 			openAlbumButtons.add(button);
 	}
 
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		final Dialog dialog;
 		final Context mContext = this;
 
 		switch (id) {
 
 		case DIALOG_CREATE_ALBUM_ID:
 
 			AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
 			builder2.setTitle("Neues Album");
 
 			View view = getLayoutInflater()
 					.inflate(R.layout.create_album, null);
 			final EditText edit = (EditText) view
 					.findViewById(R.id.albumNameEditText);
 			final RadioGroup rg = (RadioGroup) view
 					.findViewById(R.id.formatRadioGroup);
 
 			builder2.setView(view);
 
 			builder2.setPositiveButton("Speichern",
 					new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int id) {
 
 							// enable only delete button if they are
 							// disabled, 'cause no album existed
 							// open button is enabled when first pic is taken
 							if (!deleteButton.isClickable()) {
 								deleteButton.setClickable(true);
 								Log.d(TAG, "enabled delete button");
 							}
 
 							String name = edit.getText().toString();
 							Log.d(TAG, "Entered name: " + name);
 
 							// base64 encode string
 							String encodedName = new String(Base64
 									.encodeBase64(name.getBytes()));
 							Log.d(TAG, "base64 encoded name: " + encodedName);
 
 							// create folder
 							Utilities.getAlbumDirectory(encodedName);
 
 							// get format
 							int checkedRadioButtonId = rg
 									.getCheckedRadioButtonId();
 							final Boolean portrait;
 							if (checkedRadioButtonId == R.id.portraitRB)
 								portrait = true;
 							else
 								portrait = false;
 
 							SharedPreferences settings = getSharedPreferences(
 									Utilities.PIC_STORE, 0);
 							Editor edit2 = settings.edit();
 							edit2.putBoolean(encodedName + ".portrait",
 									portrait);
 							edit2.commit();
 							Log.d(TAG, "portrait: " + portrait
 									+ " settingsname: " + encodedName
 									+ ".portrait");
 
 							// update table
 							adapter.add(encodedName);
 							adapter.sort(Utilities.getAlbumNameComparator());
 							adapter.notifyDataSetChanged();
 
 							dialog.dismiss();
 						}
 					});
 			builder2.setNegativeButton("Abbrechen",
 					new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int id) {
 							dialog.dismiss();
 						}
 					});
 
 			AlertDialog create = builder2.create();
 			dialog = create;
 
 			break;
 		case DIALOG_SHOW_ALBUM_ID:
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setTitle("Wähle Album");
 			final String[] albums = Utilities.getAlbumsDecoded();
 			builder.setItems(albums, new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int item) {
 					// Toast.makeText(getApplicationContext(), albums[item],
 					// Toast.LENGTH_SHORT).show();
 					// open gallery
 					Intent intent = new Intent(mContext, GalleryActivity.class);
 					intent.putExtra("albumName", Utilities
 							.getEncodedString(albums[item]));
 					startActivity(intent);
 				}
 			});
 			dialog = builder.create();
 			break;
 		default:
 			dialog = null;
 		}
 
 		return dialog;
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		new MenuInflater(this).inflate(R.menu.mainmenu, menu);
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.createAlbumMenuItem:
 			showDialog(DIALOG_CREATE_ALBUM_ID);
 			return true;
 		case R.id.showAlbumMenuItem:
 			showDialog(DIALOG_SHOW_ALBUM_ID);
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
 		if (arg2 == 0) {
 			// open create album dialog
 			showDialog(DIALOG_CREATE_ALBUM_ID);
 		} else {
 			int albumListPosition = arg2 - 1;
 			List<String> albums = Utilities.getAlbumsAsList();
 			Collections.sort(albums, Utilities.getAlbumNameComparator());
 
 			Intent intent = new Intent(this, CameraActivity.class);
 			intent.putExtra("albumName", albums.get(albumListPosition));
 			startActivityForResult(intent, 0);
 		}
 	}
 
 	private Handler handler = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
 			switch (msg.what) {
 			case Utilities.MSG_FINISHED_PIC_SAVING:
 				Log.d(TAG, "finished saving; notify dataset has changed!");
 				adapter.notifyDataSetChanged();
 				
 				//if openbutton is disabled, because no album and pic existed before, -> enable it (now the first pic was taken)
 				if (!hasAnyPics) {
 					if(!deleteCheckboxesVisible)
 						openButton.setClickable(true);
 					
 					hasAnyPics = true;
 					Log.d(TAG, "enabled open button");
 				}
 				break;
 			default:
 				break;
 			}
 		}
 	};
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		Log.d(TAG, "finished activity camera!");
 
 		try {
 			String filename = data.getStringExtra("filename");
 			String albumName = data.getStringExtra("albumNameOfFile");
 			byte[] imageData = data.getByteArrayExtra("imageData");
 			Boolean isPortraitImage = data.getBooleanExtra("isPortraitImage",
 					true);
 
 			if (filename != null && !filename.equals("") && albumName != null
 					&& !albumName.equals("") && imageData != null)
 				new SavePhotoAsyncTask(albumName, filename, isPortraitImage,
 						handler).execute(imageData);
 		} catch (NullPointerException e) {
 			Log.d(TAG, "NO PICTURE WAS TAKEN!");
 		}
 
 	}
 
 	@Override
 	public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
 		Integer index = (Integer) arg0.getTag();
 
 		if (arg1) {
 			checkedAlbums.add(index);
 			Log.d(TAG, "added cbToDelete: " + index + " to list");
 		} else {
 			checkedAlbums.remove(index);
 			Log.d(TAG, "removed cbToDelete: " + index + " from list");
 		}
 	}
 
 }
