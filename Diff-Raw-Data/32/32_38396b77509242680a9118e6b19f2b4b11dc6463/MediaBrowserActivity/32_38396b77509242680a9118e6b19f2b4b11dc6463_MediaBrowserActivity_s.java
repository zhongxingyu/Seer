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
 
 package ac.robinson.mediatablet.activity;
 
 import group.pals.android.lib.ui.lockpattern.LockPatternActivity;
 
 import java.io.File;
 import java.util.ArrayList;
 
 import ac.robinson.mediatablet.MediaTablet;
 import ac.robinson.mediatablet.MediaTabletActivity;
 import ac.robinson.mediatablet.R;
 import ac.robinson.mediatablet.provider.HomesteadManager;
 import ac.robinson.mediatablet.provider.MediaAdapter;
 import ac.robinson.mediatablet.provider.MediaItem;
 import ac.robinson.mediatablet.provider.MediaManager;
 import ac.robinson.mediatablet.provider.MediaTabletProvider;
 import ac.robinson.mediatablet.provider.PersonItem;
 import ac.robinson.mediatablet.provider.PersonManager;
 import ac.robinson.mediatablet.view.MediaViewHolder;
 import ac.robinson.util.BitmapUtilities;
 import ac.robinson.util.ImageCacheUtilities;
 import ac.robinson.util.StringUtilities;
 import ac.robinson.util.UIUtilities;
 import ac.robinson.util.UIUtilities.ReflectionTab;
 import ac.robinson.util.UIUtilities.ReflectionTabListener;
 import ac.robinson.view.CrossFadeDrawable;
 import ac.robinson.view.FastBitmapDrawable;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.ContentResolver;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.Resources;
 import android.graphics.ColorFilter;
 import android.graphics.ColorMatrix;
 import android.graphics.ColorMatrixColorFilter;
 import android.graphics.drawable.BitmapDrawable;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.AbsListView;
 import android.widget.AdapterView;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.GridView;
 import android.widget.ImageView;
 import android.widget.ToggleButton;
 
 //TODO: add a search view: http://developer.android.com/guide/topics/ui/actionbar.html#ActionView
 //TODO: filter by text - original filename or text content
 public class MediaBrowserActivity extends MediaTabletActivity {
 
 	private MediaAdapter mMediaAdapter;
 	private FastBitmapDrawable mDefaultIcon;
 	private GridView mGrid;
 
 	private final Handler mScrollHandler = new ScrollHandler();
 	private int mScrollState = ScrollManager.SCROLL_STATE_IDLE;
 	private boolean mPendingIconsUpdate;
 	private boolean mFingerUp = true;
 	private ColorFilter mToggleButtonFilter;
 
 	// TODO: for showing the grid position hints
 	// private PopupWindow mPopup;
 	// private View mCurrentGridPositionView;
 	// private TextView mCurrentGridPositionText;
 
 	private String mParentId; // the owner of this media, or null if it's the public media view
 	private int mMediaVisibility;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		// load previous visibility and parent id on screen rotation
 		mParentId = null;
 		mMediaVisibility = MediaItem.MEDIA_PUBLIC;
 		if (savedInstanceState != null) {
 			mParentId = savedInstanceState.getString(getString(R.string.extra_parent_id));
 			mMediaVisibility = savedInstanceState.getInt(getString(R.string.extra_media_visibility));
 		} else {
 			final Intent intent = getIntent();
 			if (intent != null) {
 				mParentId = intent.getStringExtra(getString(R.string.extra_parent_id));
 				mMediaVisibility = intent.getIntExtra(getString(R.string.extra_media_visibility),
 						MediaItem.MEDIA_PUBLIC);
 			}
 		}
 
 		if (mParentId != null) {
 			addPersonalisedTabs();
 		} else {
 			UIUtilities.addActionBarTabs(this, new ReflectionTab[] {
 					new ReflectionTab(R.id.intent_homestead_browser, R.drawable.ic_menu_homesteads,
 							getString(R.string.title_homestead_browser)),
 					new ReflectionTab(R.id.intent_media_browser, R.drawable.ic_menu_public_media,
 							getString(R.string.title_media_browser_public), true) }, mReflectionTabListener);
 		}
 		UIUtilities.configureActionBar(this, true, false, R.string.title_media_browser_public, 0);
 		setContentView(R.layout.media_browser);
 
 		initialiseMediaGridView();
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle savedInstanceState) {
 		savedInstanceState.putString(getString(R.string.extra_parent_id), mParentId);
 		savedInstanceState.putInt(getString(R.string.extra_media_visibility), mMediaVisibility);
 		super.onSaveInstanceState(savedInstanceState);
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		mScrollHandler.removeMessages(MediaTablet.MESSAGE_UPDATE_GRID_ICONS);
 		// dismissPopup();
 	}
 
 	@Override
 	protected void onDestroy() {
 		ImageCacheUtilities.cleanupCache();
 		super.onDestroy();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		if (mParentId != null) {
 			MenuInflater inflater = getMenuInflater();
 			inflater.inflate(R.menu.public_media, menu);
 			inflater.inflate(R.menu.delete, menu);
 		} else {
 			getMenuInflater().inflate(R.menu.back, menu);
 		}
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 			case android.R.id.home:
 				return super.onOptionsItemSelected(item);
 
 			case R.id.menu_back:
 				finish();
 				return true;
 
 			case R.id.menu_view_public_media:
 				viewPublicMedia();
 				return true;
 
 			case R.id.menu_delete:
 				LayoutInflater inflater = LayoutInflater.from(MediaBrowserActivity.this);
 				final View textEntryView = inflater.inflate(R.layout.password_input, null);
 				AlertDialog.Builder builder = new AlertDialog.Builder(MediaBrowserActivity.this);
 				builder.setMessage(R.string.delete_person_password_prompt).setCancelable(false).setView(textEntryView)
 						.setPositiveButton(R.string.button_delete, new DialogInterface.OnClickListener() {
 							@Override
 							public void onClick(DialogInterface dialog, int id) {
 								if (MediaTablet.ADMINISTRATOR_PASSWORD.equals(StringUtilities
 										.sha1Hash(((EditText) textEntryView.findViewById(R.id.text_password_entry))
 												.getText().toString()))) {
 									ContentResolver contentResolver = getContentResolver();
 									PersonItem personToDelete = PersonManager.findPersonByInternalId(contentResolver,
 											mParentId);
 									personToDelete.setDeleted(true);
 									PersonManager.updatePerson(contentResolver, personToDelete);
 
 									String parentId = personToDelete.getParentId();
 									ArrayList<PersonItem> testPeople = PersonManager.findPeopleByParentId(
 											contentResolver, parentId);
 									if (testPeople.size() <= 0) {
 										HomesteadManager.deleteHomesteadByInternalId(getContentResolver(), parentId);
 									} else {
 										HomesteadManager.reloadHomesteadIcon(getResources(), contentResolver, parentId);
 									}
 									UIUtilities.showToast(MediaBrowserActivity.this, R.string.delete_person_deleted);
 									viewHomesteads();
 								} else {
 									UIUtilities.showToast(MediaBrowserActivity.this,
 											R.string.delete_person_password_incorrect);
 								}
 							}
						}).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
 							@Override
 							public void onClick(DialogInterface dialog, int id) {
 								dialog.cancel();
 							}
 						});
 				builder.show();
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
 		// only return the current person's id when they are unlocked
 		return mMediaVisibility == MediaItem.MEDIA_PRIVATE ? mParentId : PersonItem.UNKNOWN_PERSON_ID;
 	}
 
 	// setBackgroundDrawable is deprecated from API 16+ (Jelly Bean), but we still want to target earlier versions;
 	// since this is purely a name change, there's no real reason to do anything platform-independent
 	@SuppressWarnings("deprecation")
 	private void initialiseMediaGridView() {
 		// for API 11 and above, buttons are in the action bar - could use XML-v11 but maintenance is a hassle
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 			findViewById(R.id.panel_media).setVisibility(View.GONE);
 		}
 
 		if (mParentId == null) {
 			findViewById(R.id.panel_profile_picture).setVisibility(View.GONE);
 			findViewById(R.id.panel_filter_buttons).setBackgroundDrawable(null);
 			findViewById(R.id.button_media_view_public_media).setVisibility(View.GONE);
 			findViewById(R.id.button_media_view_homesteads).setVisibility(View.GONE);
 			((Button) findViewById(R.id.button_media_view_people)).setCompoundDrawablesWithIntrinsicBounds(0,
 					android.R.drawable.ic_menu_revert, 0, 0);
 		} else {
 			((ImageView) findViewById(R.id.profile_picture_overlay_filter))
 					.setOnLongClickListener(imageLongPressListener);
 		}
 		updateOwnerPicture(); // show the locked/unlocked owner picture or the public icon
 
 		mMediaAdapter = new MediaAdapter(this, mParentId, mMediaVisibility);
 		mDefaultIcon = mMediaAdapter.getDefaultIcon();
 		mGrid = (GridView) findViewById(R.id.grid_media);
 
 		ColorMatrix colourMatrix = new ColorMatrix();
 		colourMatrix.setSaturation(0);
 		mToggleButtonFilter = new ColorMatrixColorFilter(colourMatrix);
 		uncheckOtherFilterButtons(0);
 
 		final GridView grid = mGrid;
 		grid.setAdapter(mMediaAdapter);
 		grid.setOnScrollListener(new ScrollManager());
 		grid.setOnTouchListener(new FingerTracker());
 		grid.setOnItemSelectedListener(new SelectionTracker());
 		grid.setOnItemClickListener(new MediaViewer());
 
 		// TODO: for the scrolling hints
 		// mCurrentGridPositionView = getLayoutInflater().inflate(R.layout.grid_annotation, null);
 		// mCurrentGridPositionText = (TextView) mCurrentGridPositionView.findViewById(R.id.text_grid_annotation);
 	}
 
 	private void addPersonalisedTabs() {
 		UIUtilities.removeActionBarTabs(this);
 		PersonItem person = PersonManager.findPersonByInternalId(getContentResolver(), mParentId);
 		String personName = person == null ? null : person.getName();
 		String mediaTitle = person == null || personName == null ? getString(R.string.title_media_browser) : getString(
 				mMediaVisibility == MediaItem.MEDIA_PUBLIC ? R.string.title_media_browser_public_personalised
 						: R.string.title_media_browser_private_personalised, personName);
 		String peopleTitle = person == null || personName == null ? getString(R.string.title_people_browser) : String
 				.format(getString(R.string.title_people_browser_personalised), personName);
 		UIUtilities.addActionBarTabs(this, new ReflectionTab[] {
 				new ReflectionTab(R.id.intent_homestead_browser, R.drawable.ic_menu_homesteads,
 						getString(R.string.title_homestead_browser)),
 				new ReflectionTab(R.id.intent_people_browser, R.drawable.ic_menu_people, peopleTitle),
 				new ReflectionTab(R.id.intent_media_browser, R.drawable.ic_menu_media, mediaTitle, true) },
 				mReflectionTabListener);
 	}
 
 	private void updateOwnerPicture() {
 		if (mParentId != null && MediaTablet.DIRECTORY_THUMBS != null) {
 			Resources resources = getResources();
 			int iconWidth = resources.getDimensionPixelSize(R.dimen.media_owner_icon_width);
 			int iconHeight = resources.getDimensionPixelSize(R.dimen.media_owner_icon_width);
 			ImageView ownerIcon = ((ImageView) findViewById(R.id.profile_picture_image));
 			BitmapDrawable cachedIcon = new BitmapDrawable(getResources(), BitmapUtilities.loadAndCreateScaledBitmap(
 					new File(MediaTablet.DIRECTORY_THUMBS, PersonItem.getCacheId(mParentId)).getAbsolutePath(),
 					iconWidth, iconHeight, BitmapUtilities.ScalingLogic.CROP, true));
 			ownerIcon.setImageDrawable(cachedIcon);
 		}
 		updateLockVisibility();
 	}
 
 	private void updateLockVisibility() {
 		if (mParentId == null || mMediaVisibility == MediaItem.MEDIA_PRIVATE) {
 			findViewById(R.id.profile_picture_overlay).setVisibility(View.GONE);
 		} else {
 			findViewById(R.id.profile_picture_overlay).setVisibility(View.VISIBLE);
 		}
 	}
 
 	public void handleFilterClicks(View currentButton) {
 		final int buttonId = currentButton.getId();
 		final boolean buttonIsChecked = ((ToggleButton) currentButton).isChecked();
 		switch (buttonId) {
 			case R.id.button_media_filter_images:
 				mMediaAdapter.setMediaFilters(buttonIsChecked, MediaTabletProvider.TYPE_IMAGE_BACK,
 						MediaTabletProvider.TYPE_IMAGE_FRONT);
 				break;
 
 			case R.id.button_media_filter_videos:
 				mMediaAdapter.setMediaFilters(buttonIsChecked, MediaTabletProvider.TYPE_VIDEO);
 				break;
 
 			case R.id.button_media_filter_audio:
 				mMediaAdapter.setMediaFilters(buttonIsChecked, MediaTabletProvider.TYPE_AUDIO);
 				break;
 
 			case R.id.button_media_filter_text:
 				mMediaAdapter.setMediaFilters(buttonIsChecked, MediaTabletProvider.TYPE_TEXT);
 				break;
 
 			case R.id.button_media_filter_narratives:
 				mMediaAdapter.setMediaFilters(buttonIsChecked, MediaTabletProvider.TYPE_NARRATIVE);
 				break;
 		}
 		uncheckOtherFilterButtons(buttonId);
 	}
 
 	private void uncheckOtherFilterButtons(int currentButtonId) {
 		int[] otherButtons = new int[] { R.id.button_media_filter_images, R.id.button_media_filter_videos,
 				R.id.button_media_filter_audio, R.id.button_media_filter_text, R.id.button_media_filter_narratives };
 		for (int i = 0, n = otherButtons.length; i < n; i++) {
 			final ToggleButton currentButton = ((ToggleButton) findViewById(otherButtons[i]));
 			if ((currentButtonId == otherButtons[i] && currentButton.isChecked())
 					|| (currentButtonId == 0 && currentButton.isChecked())) {
 				currentButton.getBackground().setColorFilter(null);
 			} else {
 				currentButton.setChecked(false);
 				currentButton.getBackground().setColorFilter(mToggleButtonFilter);
 			}
 		}
 	}
 
 	public void handlePersonIconClick(View personIcon) {
 		if (mMediaVisibility == MediaItem.MEDIA_PUBLIC) {
 			PersonItem ownerPerson = PersonManager.findPersonByInternalId(getContentResolver(), mParentId);
 			Intent lockPatternIntent = new Intent(MediaBrowserActivity.this, LockPatternActivity.class);
 			lockPatternIntent.putExtra(LockPatternActivity._Theme, android.R.style.Theme_Dialog);
 			lockPatternIntent.putExtra(LockPatternActivity._Mode, LockPatternActivity.LPMode.ComparePattern);
 			lockPatternIntent.putExtra(LockPatternActivity._MaxRetry, 1); // default: 5
 			lockPatternIntent.putExtra(LockPatternActivity._PaternSha1, ownerPerson.getPasswordHash());
 			startActivityForResult(lockPatternIntent, R.id.intent_lock_pattern);
 		} else {
 			mMediaVisibility = MediaItem.MEDIA_PUBLIC;
 			mMediaAdapter.setVisibilityFilter(mMediaVisibility);
 			PersonItem person = PersonManager.findPersonByInternalId(getContentResolver(), mParentId);
 			person.setLockStatus(PersonItem.PERSON_LOCKED);
 			PersonManager.updatePerson(getContentResolver(), person);
 			addPersonalisedTabs(); // must remove and re-add - Android bug means setting text fails
 			updateLockVisibility();
 			UIUtilities.showToast(MediaBrowserActivity.this, R.string.message_locking_person_success);
 		}
 	}
 
 	private void viewHomesteads() {
 		final Intent resultIntent = new Intent();
 		resultIntent.putExtra(getString(R.string.extra_finish_activity), true);
 		resultIntent.putExtra(getString(R.string.extra_finish_parent_activities), true); // in case from public media
 		setResult(Activity.RESULT_OK, resultIntent); // exit people browser too
 		finish();
 	}
 
 	public void handleButtonClicks(View currentButton) {
 		switch (currentButton.getId()) {
 			case R.id.button_media_view_homesteads:
 				viewHomesteads();
 				break;
 
 			case R.id.button_media_view_people:
 				finish();
 				break;
 
 			case R.id.button_media_view_public_media:
 				viewPublicMedia();
 				break;
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
 
 	private View.OnLongClickListener imageLongPressListener = new View.OnLongClickListener() {
 		@Override
 		public boolean onLongClick(View view) {
 			if (mMediaVisibility == MediaItem.MEDIA_PRIVATE) {
 				PersonItem person = PersonManager.findPersonByInternalId(getContentResolver(), mParentId);
 				if (person != null) {
 					editPerson(person.getParentId(), mParentId);
 					return true;
 				}
 				return false;
 			}
 			return false;
 		}
 	};
 
 	public int getScrollState() {
 		return mScrollState; // for MediaAdapter purposes
 	}
 
 	public boolean isPendingIconsUpdate() {
 		return mPendingIconsUpdate; // for MediaAdapter purposes
 	}
 
 	private void onView(MediaViewHolder currentMediaHolder) {
 		Class<?> launchClass = null;
 		switch (currentMediaHolder.mediaType) {
 			case MediaTabletProvider.TYPE_IMAGE_BACK:
 			case MediaTabletProvider.TYPE_IMAGE_FRONT:
 			case MediaTabletProvider.TYPE_UNKNOWN:
 				launchClass = ImageViewerActivity.class;
 				break;
 			case MediaTabletProvider.TYPE_AUDIO:
 			case MediaTabletProvider.TYPE_VIDEO:
 				launchClass = AudioVideoViewerActivity.class;
 				break;
 
 			case MediaTabletProvider.TYPE_TEXT:
 				launchClass = TextViewerActivity.class;
 				break;
 
 			case MediaTabletProvider.TYPE_NARRATIVE:
 				launchClass = NarrativeViewerActivity.class;
 				break;
 
 			default:
 				break;
 		}
 		if (launchClass != null) {
 			Intent storyViewerIntent = new Intent(this, launchClass);
 			storyViewerIntent.putExtra(getString(R.string.extra_internal_id), currentMediaHolder.mediaInternalId);
 			storyViewerIntent.putExtra(getString(R.string.extra_parent_id), mParentId == null
 					|| PersonItem.UNKNOWN_PERSON_ID.equals(currentMediaHolder.mediaOwnerId) ? null
 					: currentMediaHolder.mediaOwnerId);
 			startActivityForResult(storyViewerIntent, R.id.intent_media_item_viewer);
 		}
 	}
 
 	// private void dismissPopup() {
 	// if (mPopup != null) {
 	// mPopup.dismiss();
 	// }
 	// }
 
 	// private void showPopup() {
 	// if (mPopup == null) {
 	// PopupWindow p = new PopupWindow(this);
 	// p.setFocusable(false);
 	// p.setContentView(mCurrentGridPositionView);
 	// p.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
 	// p.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
 	// p.setBackgroundDrawable(null);
 	//
 	// p.setAnimationStyle(R.style.PopupAnimation);
 	//
 	// mPopup = p;
 	// }
 	//
 	// if (mGrid.getWindowVisibility() == View.VISIBLE) {
 	// mPopup.showAtLocation(mGrid, Gravity.CENTER, 0, 0);
 	// }
 	// }
 
 	// setBackgroundDrawable is deprecated from API 16+ (Jelly Bean), but we still want to target earlier versions;
 	// since this is purely a name change, there's no real reason to do anything platform-independent
 	@SuppressWarnings("deprecation")
 	private void updateMediaIcons() {
 		mPendingIconsUpdate = false;
 
 		final GridView grid = mGrid;
 		final FastBitmapDrawable icon = mDefaultIcon;
 		final int count = grid.getChildCount();
 
 		// show a different icon depending on whether we're viewing from the public media or this person's media store
 		final int iconVisibility = mParentId == null ? MediaItem.MEDIA_PUBLIC : MediaItem.MEDIA_PRIVATE;
 		for (int i = 0; i < count; i++) {
 			final View view = grid.getChildAt(i);
 			final MediaViewHolder holder = (MediaViewHolder) view.getTag();
 			if (holder.queryIcon) {
 				// if the icon has gone missing (recently imported or cache deletion), regenerate it
 				String mediaCacheId = MediaItem.getCacheId(holder.mediaInternalId, iconVisibility);
 				FastBitmapDrawable cachedIcon = ImageCacheUtilities.getCachedIcon(MediaTablet.DIRECTORY_THUMBS,
 						mediaCacheId, ImageCacheUtilities.NULL_DRAWABLE);
 				if (ImageCacheUtilities.NULL_DRAWABLE.equals(cachedIcon)) {
 					MediaManager.reloadMediaIcon(getResources(), getContentResolver(), holder.mediaInternalId,
 							iconVisibility);
 					cachedIcon = ImageCacheUtilities.getCachedIcon(MediaTablet.DIRECTORY_THUMBS, mediaCacheId, icon);
 				}
 				CrossFadeDrawable d = holder.transition;
 				d.setEnd(cachedIcon.getBitmap());
 				holder.display.setImageDrawable(d);
 				d.startTransition(MediaTablet.ANIMATION_FADE_TRANSITION_DURATION);
 				holder.loader.setVisibility(View.GONE);
 
 				if (mParentId != null && holder.mediaVisibility == MediaItem.MEDIA_PUBLIC) {
 					holder.overlay.setBackgroundResource(R.drawable.item_public);
 					holder.overlay.setPadding(0, 0, 0, 0);
 				} else {
 					holder.overlay.setBackgroundDrawable(null);
 				}
 				holder.queryIcon = false;
 			}
 		}
 
 		grid.invalidate();
 	}
 
 	private void postUpdateMediaIcons() {
 		Handler handler = mScrollHandler;
 		Message message = handler.obtainMessage(MediaTablet.MESSAGE_UPDATE_GRID_ICONS, MediaBrowserActivity.this);
 		handler.removeMessages(MediaTablet.MESSAGE_UPDATE_GRID_ICONS);
 		mPendingIconsUpdate = true;
 		handler.sendMessage(message);
 	}
 
 	private class ScrollManager implements AbsListView.OnScrollListener {
 		// private String mPreviousPrefix;
 		// private boolean mPopupWillShow;
 
 		// private final Runnable mShowPopup = new Runnable() {
 		// public void run() {
 		// showPopup();
 		// }
 		// };
 
 		// private final Runnable mDismissPopup = new Runnable() {
 		// public void run() {
 		// mScrollHandler.removeCallbacks(mShowPopup);
 		// mPopupWillShow = false;
 		// dismissPopup();
 		// }
 		// };
 
 		public void onScrollStateChanged(AbsListView view, int scrollState) {
 			if (mScrollState == SCROLL_STATE_FLING && scrollState != SCROLL_STATE_FLING) {
 				final Handler handler = mScrollHandler;
 				final Message message = handler.obtainMessage(MediaTablet.MESSAGE_UPDATE_GRID_ICONS,
 						MediaBrowserActivity.this);
 				handler.removeMessages(MediaTablet.MESSAGE_UPDATE_GRID_ICONS);
 				handler.sendMessageDelayed(message, mFingerUp ? 0 : MediaTablet.ANIMATION_ICON_SHOW_DELAY);
 				mPendingIconsUpdate = true;
 			} else if (scrollState == SCROLL_STATE_FLING) {
 				mPendingIconsUpdate = false;
 				mScrollHandler.removeMessages(MediaTablet.MESSAGE_UPDATE_GRID_ICONS);
 			}
 			mScrollState = scrollState;
 		}
 
 		// TODO: fix this (see MediaPhone)
 		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
 			if (mScrollState != SCROLL_STATE_FLING)
 				return;
 
 			// final int count = view.getChildCount();
 			// if (count == 0)
 			// return;
 			//
 			// final StringBuilder buffer = new StringBuilder(7);
 			//
 			// String title = Long.toString(((MediaViewHolder) view.getChildAt(0).getTag()).mediaDateCreated);
 			// title = title.substring(0, Math.min(title.length(), 2));
 			// if (title.length() == 2) {
 			// buffer.append(Character.toUpperCase(title.charAt(0)));
 			// buffer.append(title.charAt(1));
 			// } else {
 			// buffer.append(title.toUpperCase());
 			// }
 			//
 			// if (count > 1) {
 			// buffer.append(" - ");
 			//
 			// final int lastChild = count - 1;
 			// title = Long.toString(((MediaViewHolder) view.getChildAt(lastChild).getTag()).mediaDateCreated);
 			// title = title.substring(0, Math.min(title.length(), 2));
 			//
 			// if (title.length() == 2) {
 			// buffer.append(Character.toUpperCase(title.charAt(0)));
 			// buffer.append(title.charAt(1));
 			// } else {
 			// buffer.append(title.toUpperCase());
 			// }
 			// }
 			//
 			// final String prefix = buffer.toString();
 			// final Handler scrollHandler = mScrollHandler;
 			//
 			// if (!mPopupWillShow && (mPopup == null || !mPopup.isShowing()) && !prefix.equals(mPreviousPrefix)) {
 			//
 			// mPopupWillShow = true;
 			// final Runnable showPopup = mShowPopup;
 			// scrollHandler.removeCallbacks(showPopup);
 			// scrollHandler.postDelayed(showPopup, MediaTablet.ANIMATION_GRIDHINT_SHOW_DELAY);
 			// }
 			//
 			// mCurrentGridPositionText.setText(prefix);
 			// mPreviousPrefix = prefix;
 			//
 			// final Runnable dismissPopup = mDismissPopup;
 			// scrollHandler.removeCallbacks(dismissPopup);
 			// scrollHandler.postDelayed(dismissPopup, MediaTablet.ANIMATION_GRIDHINT_HIDE_DELAY);
 		}
 	}
 
 	private static class ScrollHandler extends Handler {
 		@Override
 		public void handleMessage(Message msg) {
 			switch (msg.what) {
 				case MediaTablet.MESSAGE_UPDATE_GRID_ICONS:
 					((MediaBrowserActivity) msg.obj).updateMediaIcons();
 					break;
 			}
 		}
 	}
 
 	private class FingerTracker implements View.OnTouchListener {
 		public boolean onTouch(View view, MotionEvent event) {
 			final int action = event.getAction();
 			mFingerUp = action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL;
 			if (mFingerUp && mScrollState != ScrollManager.SCROLL_STATE_FLING) {
 				postUpdateMediaIcons();
 			}
 			return false;
 		}
 	}
 
 	private class SelectionTracker implements AdapterView.OnItemSelectedListener {
 		public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
 			if (mScrollState != ScrollManager.SCROLL_STATE_IDLE) {
 				mScrollState = ScrollManager.SCROLL_STATE_IDLE;
 				postUpdateMediaIcons();
 			}
 		}
 
 		public void onNothingSelected(AdapterView<?> adapterView) {
 		}
 	}
 
 	private class MediaViewer implements AdapterView.OnItemClickListener {
 		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 			MediaViewHolder currentMediaHolder = ((MediaViewHolder) view.getTag());
 			onView(currentMediaHolder);
 		}
 	}
 
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent resultIntent) {
 		switch (requestCode) {
 			case R.id.intent_media_browser:
 			case R.id.intent_media_item_viewer:
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
 
 			case R.id.intent_lock_pattern:
 				if (resultCode == Activity.RESULT_OK) {
 					mMediaVisibility = MediaItem.MEDIA_PRIVATE;
 					mMediaAdapter.setVisibilityFilter(mMediaVisibility);
 					PersonItem person = PersonManager.findPersonByInternalId(getContentResolver(), mParentId);
 					person.setLockStatus(PersonItem.PERSON_UNLOCKED);
 					PersonManager.updatePerson(getContentResolver(), person);
 					addPersonalisedTabs(); // must remove and re-add - Android bug means setting text fails
 					updateLockVisibility();
 					UIUtilities.showToast(MediaBrowserActivity.this, R.string.message_unlocking_person_success);
 				} else {
 					UIUtilities.showToast(MediaBrowserActivity.this, R.string.message_unlocking_person_failed);
 				}
 				break;
 
 			case R.id.intent_add_user:
 				updateOwnerPicture();
 				break;
 
 			default:
 				super.onActivityResult(requestCode, resultCode, resultIntent);
 		}
 	}
 }
