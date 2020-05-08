 package com.melnykov.vkphotoviewer.ui.activity;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentTransaction;
 import android.util.Log;
 
 import com.melnykov.vkphotoviewer.R;
 import com.melnykov.vkphotoviewer.ui.fragment.AlbumListFragment;
 import com.melnykov.vkphotoviewer.ui.fragment.PhotoGridFragment;
 import com.melnykov.vkphotoviewer.ui.fragment.PhotoViewFragment;
 import com.melnykov.vkphotoviewer.util.Constants;
 import com.melnykov.vkphotoviewer.util.NetworkUtil;
 import com.melnykov.vkphotoviewer.util.Session;
 
 public class MainActivity extends FragmentActivity implements AlbumListFragment.OnAlbumSelectedListener, PhotoGridFragment.OnPhotoSelectedListener {
 	
 	private static final String TAG = MainActivity.class.getSimpleName();
 	private String mCurrentFragmentTag;
 	private boolean mIsConfigChanged;
 	private static final String FRAGMENT_ALBUM_LIST_TAG = "AlbumListFragment";
 	private static final String FRAGMENT_PHOTO_GRID_TAG = "PhotoGridFragment";
 	private static final String FRAGMENT_PHOTO_VIEW_TAG = "PhotoViewFragment";
 	private static final String BUNDLE_KEY_CURRENT_FRAGMENT_TAG = "current_fragment_tag";
 	private static final String BUNDLE_KEY_ALBUM_ID = "album_id";
 	private static final String BUNDLE_KEY_PHOTO_URL = "photo_url";
 	private static final String BUNDLE_KEY_PHOTO_ID = "photo_id";
 	
 	private boolean onAuthResultSuccess;
 	
 	@Override
 	protected void onCreate(Bundle bundle) {
 		super.onCreate(bundle);
 		setContentView(R.layout.activity_main);
 		initialize(bundle);
 	}
 	
 	private void initialize(Bundle bundle) {
 		// Check internet connection
 		if (NetworkUtil.hasInternetConnection(this)) {
 			final Session session = Session.getInstance(getApplicationContext());
 			if (session.getAccessToken() == null) {
 				startLoginActivity();
 			} else {
 				restoreFragmentState(bundle);
 			}
 		} else {
 			showNoInternetConnectionDialog(bundle);
 		}
 	}
 
 	private void startLoginActivity() {
 		Intent intent = new Intent();
         intent.setClass(this, LoginActivity.class);
         startActivityForResult(intent, Constants.REQUEST_CODE_LOGIN);
 	}
 
 	private void showNoInternetConnectionDialog(final Bundle bundle) {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setMessage("No Internet connection")
 			   .setTitle("Connection problems")
 			   .setCancelable(false)
 			   .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
 				
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					dialog.dismiss();
 					initialize(bundle);
 				}
 			});
 		builder.create().show();
 	}
 	
 	@Override
 	protected void onResume() {
 		super.onResume();
 		if (onAuthResultSuccess) {
 			loadAlbumList();
 		}
 	}
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (requestCode == Constants.REQUEST_CODE_LOGIN) {
 			if (resultCode == RESULT_OK) {
 				final String accessToken = data.getStringExtra(Constants.IEXTRA_ACCESS_TOKEN);
 				final String userId = data.getStringExtra(Constants.IEXTRA_USER_ID);
 				if (Constants.DEBUG) Log.d(TAG, "Authorization successfull. Access token = " + accessToken + " user id = " + userId);
 				Session newSession = Session.getInstance(getApplicationContext());
 				newSession.setAccessToken(accessToken);
 				newSession.setUserId(userId);
 				newSession.save(getApplicationContext());
 				onAuthResultSuccess = true;
 			}
 		}
 	}
 	
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		// Save current fragment
 		outState.putString(BUNDLE_KEY_CURRENT_FRAGMENT_TAG, mCurrentFragmentTag);
 		if (mCurrentFragmentTag == FRAGMENT_PHOTO_GRID_TAG) {
 			PhotoGridFragment photoGridFragment = (PhotoGridFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_PHOTO_GRID_TAG);
 			if (photoGridFragment != null) {
 				outState.putLong(BUNDLE_KEY_ALBUM_ID, photoGridFragment.getAlbumId());
 			}
 		} else if (mCurrentFragmentTag == FRAGMENT_PHOTO_VIEW_TAG) {
 			PhotoViewFragment photoViewFragment = (PhotoViewFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_PHOTO_VIEW_TAG);
 			if (photoViewFragment != null) {
 				outState.putString(BUNDLE_KEY_PHOTO_URL, photoViewFragment.getPhotoUrl());
 				outState.putString(BUNDLE_KEY_PHOTO_ID, photoViewFragment.getPhotoId());
 			}
 		}
 		 super.onSaveInstanceState(outState);
 	}
 	
 	private void restoreFragmentState(Bundle savedInstanceState) {
 		if (savedInstanceState == null) {
 			// If it's not a configuration change - show the album list fragment as first screen
 			loadAlbumList();
 		} else {
 			mIsConfigChanged = true;
 			final String tag = savedInstanceState.getString(BUNDLE_KEY_CURRENT_FRAGMENT_TAG);
 			if (tag == null || tag == FRAGMENT_ALBUM_LIST_TAG) {
 				loadAlbumList();
 			} else if (tag == FRAGMENT_PHOTO_GRID_TAG) {
 				final long albumId = savedInstanceState.getLong(BUNDLE_KEY_ALBUM_ID, -1);
 				if (albumId != -1) {
 					loadPhotosForAlbum(albumId);
 				} else {
 					loadAlbumList();
 				}
 			} else if (tag == FRAGMENT_PHOTO_VIEW_TAG) {
 				final String photoUrl = savedInstanceState.getString(BUNDLE_KEY_PHOTO_URL);
 				final String photoId = savedInstanceState.getString(BUNDLE_KEY_PHOTO_ID); 
 				if (photoUrl != null && photoId != null) {
 					loadPhoto(photoUrl, photoId);
 				} else {
 					loadAlbumList();
 				}
 			}
 		}
 	}
 	
 	private void loadAlbumList() {
		onAuthResultSuccess = false;
 		AlbumListFragment albumListFragment = new AlbumListFragment();
 		replaceMainFragment(albumListFragment, FRAGMENT_ALBUM_LIST_TAG);
 	}
 	
 	private void loadPhotosForAlbum(long albumId) {
 		PhotoGridFragment photoGridFragment = PhotoGridFragment.newInstance(albumId);
 		replaceMainFragment(photoGridFragment, FRAGMENT_PHOTO_GRID_TAG);
 	}
 	
 	private void loadPhoto(String photoUrl, String photoId) {
 		PhotoViewFragment photoViewFragment = PhotoViewFragment.newInstance(photoUrl, photoId);
 		replaceMainFragment(photoViewFragment, FRAGMENT_PHOTO_VIEW_TAG);
 	}
 	
 	private void replaceMainFragment(Fragment fragment, String tag) {
 		FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
 		fragmentTransaction.replace(R.id.fragment_container, fragment, tag);
 		if (!mIsConfigChanged && mCurrentFragmentTag != null) {
 			fragmentTransaction.addToBackStack(tag);
 		}
 		mCurrentFragmentTag = tag;
 		fragmentTransaction.commit();
 	}
 
 	@Override
 	public void onAlbumSelected(long albumId) {
 		loadPhotosForAlbum(albumId);
 	}
 
 	@Override
 	public void onPhotoSelected(String photoUrl, String photoId) {
 		loadPhoto(photoUrl, photoId);
 	}
 }
