 /*
  *  Copyright (C) 2012 Simon Robinson
  * 
  *  This file is part of Com-Me.
  * 
  *  Com-Me is free software; you can redistribute it and/or modify it 
  *  under the terms of the GNU Lesser General Public License as 
  *  published by the Free Software Foundation; either version 3 of the 
  *  License, or (at your option) any later version.
  *
  *  Com-Me is distributed in the hope that it will be useful, but WITHOUT 
  *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
  *  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General 
  *  Public License for more details.
  *
  *  You should have received a copy of the GNU Lesser General Public
  *  License along with Com-Me.
  *  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package ac.robinson.mediatablet;
 
 import java.io.File;
 
 import ac.robinson.mediatablet.activity.AddUserActivity;
 import ac.robinson.mediatablet.activity.HomesteadBrowserActivity;
 import ac.robinson.mediatablet.activity.MediaBrowserActivity;
 import ac.robinson.mediatablet.activity.PreferencesActivity;
 import ac.robinson.mediatablet.importing.ImportedFileParser;
 import ac.robinson.mediatablet.provider.MediaItem;
 import ac.robinson.mediatablet.provider.PersonItem;
 import ac.robinson.mediautilities.MediaUtilities;
 import ac.robinson.mediautilities.MediaUtilities.FrameMediaContainer;
import ac.robinson.mediatablet.R;
 import ac.robinson.util.IOUtilities;
 import ac.robinson.util.UIUtilities;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Message;
 import android.preference.PreferenceManager;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.Window;
 
 public abstract class MediaTabletActivity extends Activity {
 	private boolean mCanSendFiles;
 
 	abstract protected void loadPreferences(SharedPreferences mediaTabletSettings);
 
 	abstract protected String getCurrentPersonId();
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		// for API < 11, buttons are in the main screen, so hide the title/action bar
 		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
 			requestWindowFeature(Window.FEATURE_NO_TITLE);
 		}
 		UIUtilities.setPixelDithering(getWindow());
 		checkDirectoriesExist();
 
 		SharedPreferences panoramaSettings = getSharedPreferences(MediaTablet.APPLICATION_NAME, Context.MODE_PRIVATE);
 		MediaTablet.ADMINISTRATOR_PASSWORD = panoramaSettings.getString(getString(R.string.key_administrator_password),
 				MediaTablet.ADMINISTRATOR_PASSWORD);
 	}
 
 	@Override
 	protected void onStart() {
 		loadAllPreferences();
 		super.onStart();
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		((MediaTabletApplication) this.getApplication()).registerActivityHandle(this);
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		((MediaTabletApplication) this.getApplication()).removeActivityHandle(this);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.preferences, menu);
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 			case android.R.id.home:
 				Intent intent = new Intent(MediaTabletActivity.this, HomesteadBrowserActivity.class);
 				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 				startActivity(intent);
 				finish();
 				return true;
 
 			case R.id.menu_preferences:
 				final Intent preferencesIntent = new Intent(MediaTabletActivity.this, PreferencesActivity.class);
 				startActivityForResult(preferencesIntent, R.id.intent_preferences);
 				return true;
 
 			default:
 				return super.onOptionsItemSelected(item);
 		}
 	}
 
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent resultIntent) {
 		switch (requestCode) {
 			case R.id.intent_preferences:
 				loadAllPreferences();
 				break;
 			default:
 				super.onActivityResult(requestCode, resultCode, resultIntent);
 		}
 	}
 
 	private void loadAllPreferences() {
 		SharedPreferences mediaTabletSettings = PreferenceManager.getDefaultSharedPreferences(MediaTabletActivity.this);
 
 		// file changes are handled in startWatchingBluetooth() - call to reset if necessary
 		((MediaTabletApplication) this.getApplication()).startWatchingBluetooth();
 
 		// other activity-specific preferences
 		loadPreferences(mediaTabletSettings);
 	}
 
 	private void checkDirectoriesExist() {
 
 		// nothing will work, and previously saved files will not load
 		if (MediaTablet.DIRECTORY_STORAGE == null) {
 
 			SharedPreferences mediaTabletSettings = getSharedPreferences(MediaTablet.APPLICATION_NAME,
 					Context.MODE_PRIVATE);
 
 			// TODO: add a way of moving content to internal locations
 			if (mediaTabletSettings.contains(MediaTablet.KEY_USE_EXTERNAL_STORAGE)) {
 				if (mediaTabletSettings.getBoolean(MediaTablet.KEY_USE_EXTERNAL_STORAGE,
 						IOUtilities.isInstalledOnSdCard(this))) {
 
 					UIUtilities.showToast(MediaTabletActivity.this, R.string.error_opening_media_content_sd, true);
 
 					finish();
 					return;
 				}
 			}
 
 			UIUtilities.showToast(MediaTabletActivity.this, R.string.error_opening_media_content, true);
 
 			finish();
 			return;
 		} else {
 			// this directory is where all public media is transferred to - it must exist
 			if (!PersonItem.getStorageDirectory(PersonItem.UNKNOWN_PERSON_ID).exists()) {
 				if (!PersonItem.getStorageDirectory(PersonItem.UNKNOWN_PERSON_ID).mkdirs()) {
 					UIUtilities.showToast(MediaTabletActivity.this, R.string.error_opening_public_content, true);
 				}
 			}
 		}
 
 		// thumbnails and sending narratives won't work, but not really fatal
 		if (MediaTablet.DIRECTORY_THUMBS == null || MediaTablet.DIRECTORY_TEMP == null) {
 			UIUtilities.showToast(MediaTabletActivity.this, R.string.error_opening_cache_content);
 		}
 
 		mCanSendFiles = true;
 		if (MediaTablet.DIRECTORY_TEMP == null) {
 			mCanSendFiles = false;
 		}
 	}
 
 	protected boolean canSendFiles() {
 		return mCanSendFiles;
 	}
 
 	public void processIncomingFiles(Message msg) {
 
 		// deal with messages from the BluetoothObserver
 		Bundle fileData = msg.peekData();
 		if (fileData == null) {
 			return; // error - no parameters passed
 		}
 
 		String importedFileName = fileData.getString(MediaUtilities.KEY_FILE_NAME);
 		if (importedFileName == null) {
 			return; // error - no filename
 		}
 
 		// get the imported file object
 		final File importedFile = new File(importedFileName);
 		if (!importedFile.canRead() || !importedFile.canWrite()) {
 			importedFile.delete(); // error - probably won't work, but might
 									// as well try; doesn't throw, so is okay
 			return;
 		}
 
 		final String mediaParent = getCurrentPersonId();
 		final int mediaVisibility = PersonItem.UNKNOWN_PERSON_ID.equals(mediaParent) ? MediaItem.MEDIA_PUBLIC
 				: MediaItem.MEDIA_PRIVATE;
 		switch (msg.what) {
 			case MediaUtilities.MSG_RECEIVED_IMPORT_FILE:
 				ImportedFileParser.importMediaItem(getContentResolver(), mediaParent, importedFile, mediaVisibility,
 						true);
 				break;
 
 			case MediaUtilities.MSG_RECEIVED_SMIL_FILE:
 				FrameMediaContainer currentSmilFile = new FrameMediaContainer(importedFile.getAbsolutePath(),
 						mediaVisibility); // hacky, but it works
 				currentSmilFile.mParentId = mediaParent;
 				new ImportSmilTask().execute(new FrameMediaContainer[] { currentSmilFile });
 				break;
 
 			case MediaUtilities.MSG_RECEIVED_HTML_FILE:
 				UIUtilities.showToast(MediaTabletActivity.this, R.string.html_feature_coming_soon);
 				importedFile.delete();
 				// TODO: this
 				break;
 		}
 	}
 
 	private class ImportSmilTask extends AsyncTask<FrameMediaContainer, Void, Void> {
 		@Override
 		protected Void doInBackground(FrameMediaContainer... smilParents) {
 			for (int i = 0, n = smilParents.length; i < n; i++) {
 				final FrameMediaContainer currentSmilFile = smilParents[i];
 				ImportedFileParser.importSMILNarrative(getContentResolver(), new File(currentSmilFile.mFrameId),
 						currentSmilFile.mParentId, currentSmilFile.mFrameSequenceId);
 				publishProgress();
 			}
 			return null;
 		}
 
 		@Override
 		protected void onProgressUpdate(Void... unused) {
 		}
 
 		@Override
 		protected void onPostExecute(Void unused) {
 		}
 	}
 
 	protected void viewPublicMedia() {
 		Intent publicMediaIntent = new Intent(MediaTabletActivity.this, MediaBrowserActivity.class);
 		startActivityForResult(publicMediaIntent, R.id.intent_media_browser);
 	}
 
 	protected void editPerson(String homesteadId, String personId) {
 		Intent addUserIntent = new Intent(MediaTabletActivity.this, AddUserActivity.class);
 		addUserIntent.putExtra(getString(R.string.extra_parent_id), homesteadId);
 		if (personId != null) {
 			addUserIntent.putExtra(getString(R.string.extra_internal_id), personId);
 			addUserIntent.putExtra(getString(R.string.extra_edit_mode), true);
 		}
 		startActivityForResult(addUserIntent, R.id.intent_add_user);
 	}
 }
