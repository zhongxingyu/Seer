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
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.Locale;
 import java.util.Map;
 
 import ac.robinson.mediatablet.activity.HomesteadBrowserActivity;
 import ac.robinson.mediatablet.activity.PeopleBrowserActivity;
 import ac.robinson.mediatablet.importing.ImportedFileParser;
 import ac.robinson.mediatablet.provider.MediaItem;
 import ac.robinson.mediatablet.provider.MediaManager;
 import ac.robinson.mediatablet.provider.MediaTabletProvider;
 import ac.robinson.mediatablet.provider.PersonItem;
 import ac.robinson.mediatablet.provider.PersonManager;
 import ac.robinson.mediautilities.FrameMediaContainer;
 import ac.robinson.mediautilities.MediaUtilities;
 import ac.robinson.mediautilities.SMILUtilities;
 import ac.robinson.util.IOUtilities;
 import ac.robinson.util.ImageCacheUtilities;
 import ac.robinson.util.StringUtilities;
 import ac.robinson.util.UIUtilities;
 import ac.robinson.util.UIUtilities.ReflectionTab;
 import ac.robinson.util.UIUtilities.ReflectionTabListener;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.ContentResolver;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.Resources;
 import android.net.Uri;
 import android.os.Build;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public abstract class MediaViewerActivity extends MediaTabletActivity {
 
 	private String mMediaInternalId;
 	private String mMediaParentId;
 	private File mMediaFile;
 	private int mMediaType;
 	private boolean mOwnerMode;
 
 	abstract protected void initialiseView(Bundle savedInstanceState);
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		// load ids on screen rotation
 		mMediaInternalId = null;
 		mMediaParentId = null;
 		if (savedInstanceState != null) {
 			mMediaInternalId = savedInstanceState.getString(getString(R.string.extra_internal_id));
 			mMediaParentId = savedInstanceState.getString(getString(R.string.extra_parent_id));
 		} else {
 			final Intent intent = getIntent();
 			if (intent != null) {
 				mMediaInternalId = intent.getStringExtra(getString(R.string.extra_internal_id));
 				mMediaParentId = intent.getStringExtra(getString(R.string.extra_parent_id));
 			}
 		}
 
 		if (mMediaInternalId == null) {
 			UIUtilities.showToast(MediaViewerActivity.this, R.string.error_loading_media);
 			finish();
 			return;
 		}
 
 		MediaItem currentMediaItem = MediaManager.findMediaByInternalId(getContentResolver(), mMediaInternalId);
 		mMediaFile = currentMediaItem.getFile();
 		mMediaType = currentMediaItem.getType();
 		if (mMediaFile == null || !mMediaFile.exists()) {
 			UIUtilities.showToast(MediaViewerActivity.this, R.string.error_loading_media);
 			finish();
 			return;
 		}
 
 		// mMediaParentId == null -> public media mode
 		// mMediaParentId != null && mOwnerMode == true -> private mode
 		// mMediaParentId != null && mOwnerMode == false -> private shared media mode
 		mOwnerMode = false;
 		ContentResolver contentResolver = getContentResolver();
 		UIUtilities.configureActionBar(this, false, false, R.string.title_playback, 0);
 		if (mMediaParentId != null) {
 			PersonItem currentPersonItem = PersonManager.findPersonByInternalId(contentResolver, mMediaParentId);
 			mOwnerMode = !currentPersonItem.isLocked();
 
 			String personName = currentPersonItem == null ? null : currentPersonItem.getName();
 			String mediaTitle = currentPersonItem == null || personName == null ? getString(R.string.title_media_browser)
 					: String.format(getString(mOwnerMode ? R.string.title_media_browser_private_personalised
 							: R.string.title_media_browser_public_personalised), personName);
 			String peopleTitle = currentPersonItem == null || personName == null ? getString(R.string.title_people_browser)
 					: String.format(getString(R.string.title_people_browser_personalised), personName);
 			UIUtilities.addActionBarTabs(this, new ReflectionTab[] {
 					new ReflectionTab(R.id.intent_homestead_browser, R.drawable.ic_menu_homesteads,
 							getString(R.string.title_homestead_browser)),
 					new ReflectionTab(R.id.intent_people_browser, R.drawable.ic_menu_people, peopleTitle),
 					new ReflectionTab(R.id.intent_media_browser, R.drawable.ic_menu_media, mediaTitle),
 					new ReflectionTab(R.id.intent_media_item_viewer, android.R.drawable.ic_menu_info_details,
 							getString(R.string.title_playback), true) }, mReflectionTabListener);
 		} else {
 			UIUtilities.addActionBarTabs(this, new ReflectionTab[] {
 					new ReflectionTab(R.id.intent_homestead_browser, R.drawable.ic_menu_homesteads,
 							getString(R.string.title_homestead_browser)),
 					new ReflectionTab(R.id.intent_media_browser, R.drawable.ic_menu_public_media,
 							getString(R.string.title_media_browser_public)),
 					new ReflectionTab(R.id.intent_media_item_viewer, android.R.drawable.ic_menu_info_details,
 							getString(R.string.title_playback), true) }, mReflectionTabListener);
 		}
 
 		initialiseView(savedInstanceState);
 
 		// for API 11 and above, buttons are in the action bar - could use XML-v11 but maintenance is a hassle
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 			findViewById(R.id.panel_media_viewer).setVisibility(View.GONE);
 		} else {
 			if (mMediaParentId == null || !mOwnerMode) {
 				findViewById(R.id.button_media_view_view_people).setVisibility(View.GONE);
 				findViewById(R.id.button_media_view_view_media).setVisibility(View.GONE);
 				findViewById(R.id.button_media_view_view_public_media).setVisibility(View.GONE);
 				findViewById(R.id.button_media_view_back).setVisibility(View.VISIBLE);
 			}
 		}
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle savedInstanceState) {
 		savedInstanceState.putString(getString(R.string.extra_internal_id), mMediaInternalId);
 		savedInstanceState.putString(getString(R.string.extra_parent_id), mMediaParentId);
 		super.onSaveInstanceState(savedInstanceState);
 	}
 
 	@Override
 	protected void onDestroy() {
 		ImageCacheUtilities.cleanupCache();
 		super.onDestroy();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		if (mOwnerMode) {
 			inflater.inflate(R.menu.share, menu);
 		}
 		inflater.inflate(R.menu.send, menu);
 		if (mMediaParentId != null) {
 			inflater.inflate(R.menu.public_media, menu);
 		}
 		inflater.inflate(R.menu.delete, menu);
 		menu.findItem(R.id.menu_delete).setTitle(getString(R.string.menu_delete_media));
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 			case R.id.menu_share:
 				shareMedia();
 				return true;
 
 			case R.id.menu_send:
 				sendMedia();
 				return true;
 
 			case R.id.menu_view_public_media:
 				viewPublicMedia();
 				return true;
 
 			case R.id.menu_delete:
 				showDeleteMediaDialog();
 				return true;
 
 			default:
 				return super.onOptionsItemSelected(item);
 		}
 	}
 
 	@Override
 	protected void loadPreferences(SharedPreferences mediaTabletSettings) {
 	}
 
 	@Override
 	protected String getCurrentPersonId() {
 		return mOwnerMode ? mMediaParentId : PersonItem.UNKNOWN_PERSON_ID;
 	}
 
 	protected String getCurrentMediaId() {
 		return mMediaInternalId;
 	}
 
 	protected File getCurrentMediaFile() {
 		return mMediaFile;
 	}
 
 	protected int getCurrentMediaType() {
 		return mMediaType;
 	}
 
 	private void sendFiles(ArrayList<Uri> filesToSend, String mimeType) {
 		if (filesToSend == null || filesToSend.size() <= 0) {
 			// TODO: show error (but remember it's from a background task, so we can't show a Toast)
 			return;
 		}
 		// also see: http://stackoverflow.com/questions/2344768/
 		final Intent sendIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
 		sendIntent.setType(mimeType); // application/smil+xml (or html), or video/quicktime, but then no bluetooth opt
 		sendIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, filesToSend);
 		startActivity(Intent.createChooser(sendIntent, getString(R.string.send_media_title))); // (single task mode)
 		// startActivity(sendIntent); //no title (but does allow saving default...)
 	}
 
 	private void sendMedia() {
 		if (MediaTablet.DIRECTORY_TEMP == null) {
 			UIUtilities.showToast(MediaViewerActivity.this, R.string.export_missing_directory, true);
 			return;
 		}
 		boolean canCopyToExternal = true;
 		if (IOUtilities.isInternalPath(MediaTablet.DIRECTORY_TEMP.getAbsolutePath())) {
 			canCopyToExternal = false;
 			UIUtilities.showToast(MediaViewerActivity.this, R.string.export_potential_problem, true);
 		}
 
 		// important to keep awake to export because we only have one chance to display the export options
 		// after creating mov or smil file (will be cancelled on screen unlock; Android is weird)
 		// TODO: move to a better (e.g. notification bar) method of exporting?
 		UIUtilities.acquireKeepScreenOn(getWindow());
 
 		ArrayList<Uri> filesToSend;
 		String mimeType = "video/*";
 		MediaItem currentMediaItem = MediaManager.findMediaByInternalId(getContentResolver(), mMediaInternalId);
 		if (currentMediaItem.getType() != MediaTabletProvider.TYPE_NARRATIVE) {
 
 			filesToSend = new ArrayList<Uri>();
 			File currentFile = currentMediaItem.getFile();
 			File publicFile = currentFile;
 			// can't send from private data directory
 			if (IOUtilities.isInternalPath(currentFile.getAbsolutePath()) && canCopyToExternal) {
 				try {
 					publicFile = new File(MediaTablet.DIRECTORY_TEMP, currentFile.getName());
 					IOUtilities.copyFile(currentFile, publicFile);
 					IOUtilities.setFullyPublic(publicFile);
 				} catch (IOException e) {
 					UIUtilities.showToast(MediaViewerActivity.this, R.string.error_sending_media);
 					return;
 				}
 			}
 			filesToSend.add(Uri.fromFile(publicFile));
 			switch (currentMediaItem.getType()) {
 				case MediaTabletProvider.TYPE_IMAGE_BACK:
 				case MediaTabletProvider.TYPE_IMAGE_FRONT:
 					mimeType = "image/*";
 					break;
 				case MediaTabletProvider.TYPE_AUDIO:
 					mimeType = "audio/*";
 					break;
 				case MediaTabletProvider.TYPE_VIDEO:
 					break;
 				case MediaTabletProvider.TYPE_TEXT:
 					mimeType = "text/*";
 					break;
 				case MediaTabletProvider.TYPE_UNKNOWN:
 				default:
 					mimeType = "unknown"; // TODO
 					break;
 			}
 		} else {
 			// TODO: extract and combine with MediaPhone version
 			Resources res = getResources();
 			final ArrayList<FrameMediaContainer> smilContents = SMILUtilities.getSMILFrameList(
 					currentMediaItem.getFile(), res.getInteger(R.integer.frame_narrative_sequence_increment), false, 0,
 					false);
 
 			// random name to counter repeat sending name issues
 			final String exportName = String.format("%s-%s",
 					getString(R.string.app_name).replaceAll("[^a-zA-Z0-9]+", "-").toLowerCase(Locale.ENGLISH),
 					MediaTabletProvider.getNewInternalId().substring(0, 8));
 
 			final Map<Integer, Object> settings = new Hashtable<Integer, Object>();
 			settings.put(MediaUtilities.KEY_AUDIO_RESOURCE_ID, R.raw.ic_audio_playback);
 
 			// some output settings
 			settings.put(MediaUtilities.KEY_BACKGROUND_COLOUR, res.getColor(R.color.icon_background));
 			settings.put(MediaUtilities.KEY_TEXT_COLOUR_NO_IMAGE, res.getColor(R.color.icon_text_no_image));
 			settings.put(MediaUtilities.KEY_TEXT_COLOUR_WITH_IMAGE, res.getColor(R.color.icon_text_with_image));
 			settings.put(MediaUtilities.KEY_TEXT_BACKGROUND_COLOUR, res.getColor(R.color.icon_text_background));
 			settings.put(MediaUtilities.KEY_TEXT_SPACING, res.getDimensionPixelSize(R.dimen.export_icon_text_padding));
 			settings.put(MediaUtilities.KEY_TEXT_CORNER_RADIUS,
 					res.getDimensionPixelSize(R.dimen.export_icon_text_corner_radius));
 			settings.put(MediaUtilities.KEY_TEXT_BACKGROUND_SPAN_WIDTH,
 					Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB);
 			// TODO: do we want to do getDimensionPixelSize for export?
 			settings.put(MediaUtilities.KEY_MAX_TEXT_FONT_SIZE,
 					res.getDimensionPixelSize(R.dimen.export_maximum_text_size));
 			settings.put(MediaUtilities.KEY_MAX_TEXT_CHARACTERS_PER_LINE,
 					res.getInteger(R.integer.export_maximum_text_characters_per_line));
 			settings.put(MediaUtilities.KEY_MAX_TEXT_HEIGHT_WITH_IMAGE,
 					res.getDimensionPixelSize(R.dimen.export_maximum_text_height_with_image));
 
 			settings.put(MediaUtilities.KEY_OUTPUT_WIDTH, res.getInteger(R.integer.export_smil_width));
 			settings.put(MediaUtilities.KEY_OUTPUT_HEIGHT, res.getInteger(R.integer.export_smil_height));
 			settings.put(MediaUtilities.KEY_PLAYER_BAR_ADJUSTMENT,
 					res.getInteger(R.integer.export_smil_player_bar_adjustment));
 
 			filesToSend = SMILUtilities.generateNarrativeSMIL(getResources(), new File(MediaTablet.DIRECTORY_TEMP,
					exportName + MediaUtilities.SMIL_FILE_EXTENSION), smilContents, settings);
 		}
 
 		sendFiles(filesToSend, mimeType);
 	}
 
 	private void shareMedia() {
 		final CharSequence[] publicMediaOptions = { getString(R.string.share_media_private),
 				getString(R.string.share_media_choose), getString(R.string.share_media_public) };
 		MediaItem currentMediaItem = MediaManager.findMediaByInternalId(getContentResolver(), mMediaInternalId);
 		AlertDialog.Builder builder = new AlertDialog.Builder(MediaViewerActivity.this);
 		builder.setTitle(R.string.share_media_title);
 		// builder.setMessage(R.string.share_media_hint); // breaks the dialog
 		builder.setIcon(android.R.drawable.ic_dialog_info);
 		builder.setNegativeButton(android.R.string.cancel, null);
 		builder.setSingleChoiceItems(publicMediaOptions, (currentMediaItem.isPubliclyShared() ? 2 : 0),
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int item) {
 						if (item == 0 || item == 2) {
 
 							// no need to update the icon (will be done when next viewed)
 							ContentResolver contentResolver = getContentResolver();
 							MediaItem sharedMediaItem = MediaManager.findMediaByInternalId(contentResolver,
 									mMediaInternalId);
 							int newVisibility = item == 0 ? MediaItem.MEDIA_PRIVATE : MediaItem.MEDIA_PUBLIC;
 
 							// TODO: should we share each individual narrative component?
 							// if (sharedMediaItem.getType() == MediaTabletProvider.TYPE_NARRATIVE) {
 							// ArrayList<FrameMediaContainer> smilContents = SMILUtilities.getSMILFrameList(
 							// sharedMediaItem.getFile(), 1, false, 0, false);
 							//
 							// for (FrameMediaContainer frame : smilContents) {
 							//
 							// if (frame.mImagePath != null) {
 							// final MediaItem imageMediaItem = MediaManager.findMediaByInternalId(
 							// contentResolver, MediaItem.getInternalId(frame.mImagePath));
 							// imageMediaItem.setPubliclySharedStatus(newVisibility);
 							// MediaManager.updateMedia(contentResolver, imageMediaItem);
 							// }
 							//
 							// for (String mediaPath : frame.mAudioPaths) {
 							// final MediaItem audioMediaItem = MediaManager.findMediaByInternalId(
 							// contentResolver, MediaItem.getInternalId(mediaPath));
 							// audioMediaItem.setPubliclySharedStatus(newVisibility);
 							// MediaManager.updateMedia(contentResolver, audioMediaItem);
 							// }
 							//
 							// // text is stored in the narrative file itself - no need to share
 							// }
 							// }
 
 							sharedMediaItem.setPubliclySharedStatus(newVisibility);
 							MediaManager.updateMedia(contentResolver, sharedMediaItem);
 
 						} else {
 							Intent showPeopleIntent = new Intent(MediaViewerActivity.this, PeopleBrowserActivity.class);
 							startActivityForResult(showPeopleIntent, R.id.intent_people_browser);
 						}
 						dialog.dismiss();
 					}
 				});
 		AlertDialog alert = builder.create();
 		alert.show();
 	}
 
 	private void sendShareMedia() {
 		if (mMediaParentId == null || !mOwnerMode) {
 			sendMedia();
 		} else {
 			final CharSequence[] sendShareOptions = { getString(R.string.menu_send), getString(R.string.menu_share),
 					getString(R.string.button_delete) };
 			AlertDialog.Builder builder = new AlertDialog.Builder(MediaViewerActivity.this);
 			builder.setTitle(R.string.edit_media_title);
 			// builder.setMessage(R.string.edit_media_hint); // breaks the dialog
 			builder.setIcon(android.R.drawable.ic_dialog_info);
 			builder.setNegativeButton(android.R.string.cancel, null);
 			builder.setItems(sendShareOptions, new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int item) {
 					switch (item) {
 						case 0:
 							sendMedia();
 							break;
 						case 1:
 							shareMedia();
 							break;
 						case 2:
 							showDeleteMediaDialog();
 							break;
 					}
 				}
 			});
 			AlertDialog alert = builder.create();
 			alert.show();
 		}
 	}
 
 	private void showDeleteMediaDialog() {
 		if (mOwnerMode) {
 			AlertDialog.Builder builder = new AlertDialog.Builder(MediaViewerActivity.this);
 			builder.setTitle(R.string.delete_media_confirmation);
 			builder.setMessage(R.string.delete_media_hint);
 			builder.setIcon(android.R.drawable.ic_dialog_alert);
 			builder.setNegativeButton(android.R.string.cancel, null);
 			builder.setPositiveButton(R.string.button_delete, new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int whichButton) {
 					deleteMedia();
 				}
 			});
 			AlertDialog alert = builder.create();
 			alert.show();
 		} else {
 			LayoutInflater inflater = LayoutInflater.from(MediaViewerActivity.this);
 			final View textEntryView = inflater.inflate(R.layout.password_input, null);
 			AlertDialog.Builder builder = new AlertDialog.Builder(MediaViewerActivity.this);
 			builder.setMessage(R.string.delete_media_password_prompt).setCancelable(false).setView(textEntryView)
 					.setPositiveButton(R.string.button_delete, new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int id) {
 							if (MediaTablet.ADMINISTRATOR_PASSWORD.equals(StringUtilities
 									.sha1Hash(((EditText) textEntryView.findViewById(R.id.text_password_entry))
 											.getText().toString()))) {
 								deleteMedia();
 							} else {
 								UIUtilities.showToast(MediaViewerActivity.this,
 										R.string.delete_media_password_incorrect);
 							}
 						}
 					}).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int id) {
 							dialog.cancel();
 						}
 					});
 			builder.create();
 			builder.show();
 		}
 	}
 
 	private void deleteMedia() {
 		ContentResolver contentResolver = getContentResolver();
 		MediaItem mediaToDelete = MediaManager.findMediaByInternalId(getContentResolver(), mMediaInternalId);
 		mediaToDelete.setDeleted(true);
 		MediaManager.updateMedia(contentResolver, mediaToDelete);
 		UIUtilities.showToast(MediaViewerActivity.this, R.string.delete_media_deleted);
 		finish();
 	}
 
 	private void viewHomesteads() {
 		Intent intent = new Intent(MediaViewerActivity.this, HomesteadBrowserActivity.class);
 		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 		startActivity(intent);
 		finish();
 	}
 
 	private void viewPeople() {
 		final Intent resultIntent = new Intent();
 		resultIntent.putExtra(getString(R.string.extra_finish_activity), true);
 		setResult(Activity.RESULT_OK, resultIntent); // exit media browser too
 		finish();
 	}
 
 	public void handleButtonClicks(View currentButton) {
 		switch (currentButton.getId()) {
 			case R.id.button_media_view_share_media:
 				sendShareMedia();
 				break;
 
 			case R.id.button_media_view_view_homesteads:
 				viewHomesteads();
 
 			case R.id.button_media_view_view_people:
 				viewPeople();
 				break;
 
 			case R.id.button_media_view_view_media:
 			case R.id.button_media_view_back:
 				finish();
 				break;
 
 			case R.id.button_media_view_view_public_media:
 				viewPublicMedia();
 				break;
 		}
 	}
 
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent resultIntent) {
 		switch (requestCode) {
 			case R.id.intent_media_browser:
 				if (resultCode == Activity.RESULT_OK && resultIntent != null) {
 					if (resultIntent.getBooleanExtra(getString(R.string.extra_finish_activity), false)) {
 						if (resultIntent.getBooleanExtra(getString(R.string.extra_finish_parent_activities), false)) {
 							viewHomesteads();
 						} else {
 							finish();
 						}
 					}
 				}
 				break;
 
 			case R.id.intent_people_browser:
 				if (resultCode == Activity.RESULT_OK && resultIntent != null
 						&& resultIntent.hasExtra(getString(R.string.extra_selected_items))) {
 
 					ContentResolver contentResolver = getContentResolver();
 					MediaItem sharedMediaItem = MediaManager.findMediaByInternalId(contentResolver, mMediaInternalId);
 					ArrayList<FrameMediaContainer> smilContents = null;
 					if (sharedMediaItem.getType() == MediaTabletProvider.TYPE_NARRATIVE) {
 						smilContents = SMILUtilities.getSMILFrameList(sharedMediaItem.getFile(), 1, false, 0, false);
 					}
 					String[] selectedPeople = resultIntent
 							.getStringArrayExtra(getString(R.string.extra_selected_items));
 					int numPeopleShared = selectedPeople.length;
 					for (String shareDestination : selectedPeople) {
 						if (!shareDestination.equals(mMediaParentId)) { // don't send to self
 							if (sharedMediaItem.getType() == MediaTabletProvider.TYPE_NARRATIVE) {
 								ImportedFileParser.duplicateSMILElements(contentResolver, smilContents,
 										sharedMediaItem.getFile(), shareDestination, MediaItem.MEDIA_PRIVATE, false);
 							} else {
 								final MediaItem newMediaItem = MediaItem.fromExisting(sharedMediaItem,
 										MediaTabletProvider.getNewInternalId(), shareDestination,
 										System.currentTimeMillis());
 								newMediaItem.setPubliclySharedStatus(MediaItem.MEDIA_PRIVATE);
 								try {
 									IOUtilities.copyFile(sharedMediaItem.getFile(), newMediaItem.getFile());
 									MediaManager.addMedia(contentResolver, newMediaItem);
 								} catch (IOException e) {
 								}
 							}
 						} else {
 							numPeopleShared -= 1;
 						}
 					}
 					Toast.makeText(
 							MediaViewerActivity.this,
 							String.format(
 									numPeopleShared == 1 ? getString(R.string.share_media_choose_completed_singular)
 											: getString(R.string.share_media_choose_completed_plural), numPeopleShared),
 							Toast.LENGTH_SHORT).show();
 				}
 				break;
 
 			default:
 				super.onActivityResult(requestCode, resultCode, resultIntent);
 		}
 	}
 
 	private ReflectionTabListener mReflectionTabListener = new ReflectionTabListener() {
 		@Override
 		public void onTabSelected(int tabId) {
 			switch (tabId) {
 				case R.id.intent_homestead_browser:
 					viewHomesteads();
 					break;
 
 				case R.id.intent_people_browser:
 					viewPeople();
 					break;
 
 				case R.id.intent_media_browser:
 					finish();
 					break;
 
 				default:
 					break;
 			}
 		}
 
 		@Override
 		public void onTabReselected(int tabId) {
 		}
 
 		@Override
 		public void onTabUnselected(int tabId) {
 		}
 	};
 }
