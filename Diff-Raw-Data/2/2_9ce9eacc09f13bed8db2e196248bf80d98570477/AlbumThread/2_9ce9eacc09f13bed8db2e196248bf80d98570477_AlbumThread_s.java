 /*
  * ComicsReader is an Android application to read comics
  * Copyright (C) 2011-2013 Cedric OCHS
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 
 package net.kervala.comicsreader;
 
 import java.io.File;
 import java.lang.ref.WeakReference;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.graphics.Bitmap;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Handler.Callback;
 import android.os.HandlerThread;
 import android.os.Looper;
 import android.os.Message;
 import android.os.Process;
 import android.preference.PreferenceManager;
 
 public class AlbumThread extends HandlerThread {
 	private Album mAlbum;
 	private int mCurrentPage = -1;
 	private int mNextPage = -1;
 	private int mPreviousPage = -1;
 	private int mWidth = 0;
 	private boolean mLoadingPage = false;
 	private boolean mHighQuality = false;
 	private boolean mFullScreen = false;
 	private int mZoom = 0;
 	private int mSample = 0;
 	private int mOverlayDuration = 5000;
 	private int mEdgesResistance = 1;
 	private int mPageTransitionSpeed = 2;
 	private boolean mRightToLeft = false;
 	private boolean mDoublePage = false;
 	private boolean mFitToScreen = true;
 	private boolean mPreferencesLoaded = false;
 	private Handler mMainHandler;
 	private Handler mLoaderHandler;
 	private WeakReference<AlbumPageCallback> mCallback;
 
 	static final int ZOOM_NONE = 0;
 	static final int ZOOM_FIT_WIDTH = 1;
 	static final int ZOOM_FIT_HEIGHT = 2;
 	static final int ZOOM_FIT_SCREEN = 3;
 	static final int ZOOM_100 = 4;
 	static final int ZOOM_50 = 5;
 	static final int ZOOM_25 = 6;
 	
 	static final int TIME_CHECK_INTERVAL = 16; // milliseconds
 
 	static final int LOADER_OPEN = 1;
 	static final int LOADER_UPDATE_CURRENT_PAGE = 2;
 	static final int LOADER_UPDATE_NEXT_PAGE = 3;
 	static final int LOADER_UPDATE_PREVIOUS_PAGE = 4;
 	static final int LOADER_LOAD_PREFERENCES = 5;
 	static final int LOADER_SAVE_CURRENT_ALBUM = 6;
 
 	static final int VIEWER_CHANGE_PAGE = 1;
 	static final int VIEWER_PREPARE_CURRENT_PAGE = 4;
 	static final int VIEWER_PREPARE_NEXT_PAGE = 5;
 	static final int VIEWER_PREPARE_PREVIOUS_PAGE = 6;
 	static final int VIEWER_UPDATE_CURRENT_PAGE = 7;
 	static final int VIEWER_UPDATE_NEXT_PAGE = 8;
 	static final int VIEWER_UPDATE_PREVIOUS_PAGE = 9;
 	static final int VIEWER_WINDOW_CHANGED = 10;
 	static final int VIEWER_SCROLL_PAGE = 11;
 	static final int VIEWER_OPEN_BEGIN = 12;
 	static final int VIEWER_OPEN_END = 13;
 	static final int VIEWER_ERROR = 14;
 
 	public interface AlbumPageCallback {
 		public int getPageWidth();
 		public int getPageHeight();
 
 		public boolean onSwapNextPage();
 		public boolean onSwapPreviousPage();
 
 		public boolean onPrepareNextPage(int newPage, int oldPage);
 		public boolean onPreparePreviousPage(int newPage, int oldPage);
 		public boolean onPrepareCurrentPage(int newPage, int oldPage);
 
 		public boolean onUpdateNextPage(Bitmap bitmap);
 		public boolean onUpdatePreviousPage(Bitmap bitmap);
 		public boolean onUpdateCurrentPage(Bitmap bitmap);
 
 		public void onDisplayPageNumber(int page, int pages, int duration);
 		public void onPageChanged();
 		public boolean onReset();
 		public void onError(int error);
 		public void onWindowChanged(boolean highQuality, boolean fullScreen);
 		public void onPageScrolled(int page);
 
 		public void onOpenBegin();
 		public void onOpenEnd();
 
 		public Context getContext();
 	}
 
 	public AlbumThread() {
 		super("ComicsReader", Process.THREAD_PRIORITY_MORE_FAVORABLE /* Process.THREAD_PRIORITY_BACKGROUND */);
 
 		mMainHandler = new Handler(Looper.getMainLooper(), mMainCallback);
 		
 		start();
 	}
 
 	public void setAlbumPageCallback(AlbumPageCallback callback) {
 		mCallback = new WeakReference<AlbumPageCallback>(callback);
 	}
 
 	@Override
 	protected void onLooperPrepared() {
 		super.onLooperPrepared();
 		synchronized (this) {
 			mLoaderHandler = new Handler(getLooper(), mLoaderCallback);
 			notifyAll();
 		}
 	}
 
 	public boolean exit() {
 		if (mLoaderHandler != null) {
 			mLoaderHandler.removeMessages(LOADER_OPEN);
 			mLoaderHandler.removeMessages(LOADER_UPDATE_NEXT_PAGE);
 			mLoaderHandler.removeMessages(LOADER_UPDATE_PREVIOUS_PAGE);
 			mLoaderHandler.removeMessages(LOADER_UPDATE_CURRENT_PAGE);
 			mLoaderHandler.removeMessages(LOADER_LOAD_PREFERENCES);
 			mLoaderHandler = null;
 		}
 
 		return true;
 	}
 
 	private boolean isLoaderReady() {
 		if (!isAlive()) return false;
 
 		synchronized (this) {
 			while (isAlive() && mLoaderHandler == null) {
 				try {
 					wait();
 				} catch (InterruptedException e) {
 				}
 			}
 		}
 		
 		return true;
 	}
 
 	private Bitmap getPage(int page) {
 		if (mAlbum == null || ComicsParameters.sScreenWidth < 1 || ComicsParameters.sScreenHeight < 1) return null;
 
 		mLoadingPage = true;
 
 		mAlbum.setHighQuality(mHighQuality);
 		mAlbum.setFitToScreen(mFitToScreen);
 		mAlbum.setScale(mSample);
 
 		boolean divideByTwo = false;
 		int width = -1;
 		int height = -1;
 
 		switch (mZoom) {
 		case ZOOM_FIT_WIDTH: {
 			width = ComicsParameters.sScreenWidth;
 			if (mDoublePage) {
 				divideByTwo = true;
 			}
 			break;
 		}
 		case ZOOM_FIT_HEIGHT: {
 			height = ComicsParameters.sScreenHeight;
 			break;
 		}
 		case ZOOM_FIT_SCREEN: {
 			width = ComicsParameters.sScreenWidth;
 			if (mDoublePage) {
 				divideByTwo = true;
 			}
 			height = ComicsParameters.sScreenHeight;
 			break;
 		}
 		case ZOOM_50: {
 			width = -2;
 			height = -2;
 			break;
 		}
 		case ZOOM_25: {
 			width = -4;
 			height = -4;
 			break;
 		}
 		}
 
 		Bitmap bitmap = null;
 
 		if (mDoublePage && page > 0) {
 			bitmap = mAlbum.getDoublePage(page, divideByTwo ? width/2:width, height);
 		} else {
 			bitmap = mAlbum.getPage(page, width, height, false);
 		}
 
 		if (!mLoadingPage) {
 			displayError(R.string.error_out_of_memory);
 			return null;
 		}
 
 		mLoadingPage = false;
 
 		return bitmap;
 	}
 
 	public Album getAlbum() {
 		return mAlbum;
 	}
 
 	public Uri getAlbumUri() {
 		if (mAlbum == null) return null;
 
 		String filename = mAlbum.getFilename();
 		if (filename == null) return null;
 
 		return Uri.parse(Uri.fromFile(new File(filename)).toString() + "#" + String.valueOf(mCurrentPage));
 	}
 
 	public boolean isValid() {
 		return mAlbum != null && mAlbum.getNumPages() > 0;
 	}
 
 	public int getPageWidth() {
 		return Math.max(mWidth, ComicsParameters.sScreenWidth);
 	}
 
 	public int getCurrentPage() {
 		return mCurrentPage;
 	}
 
 	public int getNextPage() {
 		if (mRightToLeft) {
 			if (mCurrentPage == 1) return 0;
 
 			int previousPage = mCurrentPage - (mDoublePage ? 2:1);
 
 			return previousPage < getLastPage() ? -1:previousPage;
 		}
 		
 		if (mCurrentPage == 0 && getLastPage() > 0) return 1;
 
 		int nextPage = mCurrentPage + (mDoublePage ? 2:1);
 
 		return nextPage > getLastPage() ? -1:nextPage;
 	}
 
 	public int getPreviousPage() {
 		if (mRightToLeft) {
 			if (mCurrentPage == 0 && getFirstPage() > 0) return 1;
 
 			int nextPage = mCurrentPage + (mDoublePage ? 2:1);
 
 			return nextPage > getFirstPage() ? -1:nextPage;
 		}
 		
 		if (mCurrentPage == 1) return 0;
 
 		int previousPage = mCurrentPage - (mDoublePage ? 2:1);
 
 		return previousPage < getFirstPage() ? -1:previousPage;
 	}
 
 	public int getLastPage() {
 		if (mRightToLeft || mAlbum == null) return 0;
 
 		int page = mAlbum.getNumPages() - 1;
 		
 		if (mDoublePage && (page % 2) == 0) --page;
 		
 		return page;
 
 	}
 
 	public int getFirstPage() {
 		if (!mRightToLeft || mAlbum == null) return 0;
 
 		int page = mAlbum.getNumPages() - 1;
 			
 		if (mDoublePage && (page % 2) == 0) --page;
 			
 		return page;
 	}
 
 	public boolean isFirstPage() {
 		return getCurrentPage() == getFirstPage();
 	}
 	
 	public boolean isLastPage() {
 		return getCurrentPage() == getLastPage();
 	}
 	
 	public int getEdgeResistance() {
 		return mEdgesResistance;
 	}
 	
 	public int getPageTransitionSpeed() {
 		return mPageTransitionSpeed;
 	}
 
 	public boolean isRightToLeft() {
 		return mRightToLeft;
 	}
 
 	/*
 	 * Loader actions
 	 */
 
 	/**
 	 * Open album
 	 * @param uri URI for album
 	 */
 	public void open(Uri uri) {
 		if (!isLoaderReady()) return;
 
 		Message msg = mLoaderHandler.obtainMessage(LOADER_OPEN);
 		msg.obj = uri;
 		mLoaderHandler.sendMessage(msg);
 	}
 
 	/**
 	 * go to specified page
 	 * @param page the page
 	 */
 	public void changePage(int page) {
 		if (!isLoaderReady()) return;
 
 		Message msg = mMainHandler.obtainMessage(VIEWER_CHANGE_PAGE);
 		msg.getData().putInt("page", page);
 		mMainHandler.sendMessage(msg);
 	}
 
 	/*
 	 * go to next page
 	 */
 	public void nextPage() {
 		changePage(getNextPage());
 	}
 
 	/*
 	 * go to previous page
 	 */
 	public void previousPage() {
 		changePage(getPreviousPage());
 	}
 
 	/*
 	 * go to last page
 	 */
 	public void lastPage() {
 		changePage(getLastPage());
 	}
 
 	/*
 	 * go to first page
 	 */
 	public void firstPage() {
 		changePage(getFirstPage());
 	}
 
 	public void updatePreviousPage() {
 		if (mAlbum.getMaxImagesInMemory() > 2) {
 			if (!isLoaderReady()) return;
 			mMainHandler.sendEmptyMessage(VIEWER_PREPARE_PREVIOUS_PAGE);
 		}
 	}
 
 	public void updateNextPage() {
 		if (mAlbum.getMaxImagesInMemory() > 1) {
 			if (!isLoaderReady()) return;
 			mMainHandler.sendEmptyMessage(VIEWER_PREPARE_NEXT_PAGE);
 		}
 	}
 
 	public void updateCurrentPage(boolean force) {
 		if (!isLoaderReady()) return;
 
 		Message msg = mMainHandler.obtainMessage(VIEWER_PREPARE_CURRENT_PAGE);
 		msg.getData().putBoolean("force", force);
 		msg.getData().putInt("page", mCurrentPage);
 		mMainHandler.sendMessage(msg);
 	}
 
 	public void loadPreferences() {
 		if (!isLoaderReady()) return;
 
 		// cancel previously page loading
 		mLoadingPage = false;
 
 		mLoaderHandler.sendEmptyMessage(LOADER_LOAD_PREFERENCES);
 	}
 
 	public void saveCurrentAlbum() {
 		if (!isLoaderReady()) return;
 
 		mLoaderHandler.sendEmptyMessage(LOADER_SAVE_CURRENT_ALBUM);
 	}
 	
 	/*
 	 * Main actions
 	 */
 
 	public void updatePageScrolling(int page) {
 		mMainHandler.removeMessages(VIEWER_SCROLL_PAGE);
 
 		final Message msg = mMainHandler.obtainMessage(VIEWER_SCROLL_PAGE);
 		msg.getData().putInt("page", page);
 		mMainHandler.sendMessageDelayed(msg, TIME_CHECK_INTERVAL);
 	}
 
 	private void displayError(int error) {
 		Message msg = mMainHandler.obtainMessage(VIEWER_ERROR);
 		msg.getData().putInt("error", error);
 		mMainHandler.sendMessage(msg);
 	}
 
 	private Callback mLoaderCallback = new Callback() {
 		public boolean handleMessage(Message msg) {
 			switch (msg.what) {
 			case LOADER_OPEN: {
 				int error = 0;
 				Uri uri = (Uri)msg.obj;
 
 				String filename = Album.getFilenameFromUri(uri);
 				String strPage = Album.getPageFromUri(uri);
 
 				int page = -1;
 
 				if (strPage != null) {
 					try {
 						page = Integer.valueOf(strPage);
 					} catch(NumberFormatException e) {
 					}
 				}
 
 				if (Album.isFilenameValid(filename)) {
 					mMainHandler.sendEmptyMessage(VIEWER_OPEN_BEGIN);
 
 					// cancel previously page loading
 					mLoadingPage = false;
 
 					if (mAlbum != null) {
 						synchronized (mAlbum) {
 							mAlbum.close();
 						}
 					}
 					
 					// create an album depending on file type
 					mAlbum = Album.createInstance(filename);
 
 					synchronized (mAlbum) {
 						mAlbum.setHighQuality(mHighQuality);
 						mAlbum.setFitToScreen(mFitToScreen);
 						mAlbum.setScale(mSample);
 
 						if (mAlbum.open(filename, true)) {
 							ComicsParameters.sCurrentOpenAlbum = mAlbum.getFilename();
 						} else {
 							error = R.string.error_no_album_loaded;
 						}
 					}
 
 //					mMainHandler.sendEmptyMessage(VIEWER_RESET);
 					mMainHandler.sendEmptyMessage(VIEWER_OPEN_END);
 
 					msg = mMainHandler.obtainMessage(VIEWER_CHANGE_PAGE);
 					msg.getData().putInt("page", page);
 					mMainHandler.sendMessage(msg);
 				} else {
 					error = R.string.error_no_album_loaded; // TODO: filename invalid
 				}
 
 				if (error != 0) {
 					displayError(error);
 				}
 
 				return true;
 			}
 			case LOADER_UPDATE_CURRENT_PAGE: {
 				Bundle b = msg.getData();
 				int page = b.getInt("page");
 
 //				Log.i("ComicsReader", "LOADER_UPDATE_CURRENT_PAGE " + String.valueOf(page));
 				
 				final Bitmap bitmap = getPage(page);
 
 				if (bitmap != null) {
 					msg = mMainHandler.obtainMessage(VIEWER_UPDATE_CURRENT_PAGE);
 					msg.getData().putInt("page", page);
 					msg.obj = bitmap;
 					mMainHandler.sendMessage(msg);
 				} else {
 					// TODO: create an error message
 				}
 				
 				return true;
 			}
 			case LOADER_UPDATE_NEXT_PAGE: {
 				Bundle b = msg.getData();
 				int page = b.getInt("page");
 
 //				Log.i("ComicsReader", "LOADER_UPDATE_NEXT_PAGE " + String.valueOf(page));
 				
 				final Bitmap bitmap = getPage(page);
 
 				if (bitmap != null) {
 					msg = mMainHandler.obtainMessage(VIEWER_UPDATE_NEXT_PAGE);
 					msg.getData().putInt("page", page);
 					msg.obj = bitmap;
 					mMainHandler.sendMessage(msg);
 				} else {
 					// TODO: error
 				}
 
 				return true;
 			}
 			case LOADER_UPDATE_PREVIOUS_PAGE: {
 				Bundle b = msg.getData();
 				int page = b.getInt("page");
 
 				final Bitmap bitmap = getPage(page);
 
 				if (bitmap != null) {
 					msg = mMainHandler.obtainMessage(VIEWER_UPDATE_PREVIOUS_PAGE);
 					msg.getData().putInt("page", page);
 					msg.obj = bitmap;
 					mMainHandler.sendMessage(msg);
 				} else {
 					// TODO: error
 				}
 
 				return true;
 			}
 			case LOADER_LOAD_PREFERENCES: {
 				final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mCallback.get().getContext());
 				mHighQuality = prefs.getBoolean("preference_high_quality", false);
 				mFullScreen = prefs.getBoolean("preference_full_screen", false);
 				mZoom = Integer.parseInt(prefs.getString("preference_zoom", "1"));
 				mDoublePage = prefs.getBoolean("preference_double_page", false);
 				mSample = Integer.parseInt(prefs.getString("preference_sample", "0"));
 				mFitToScreen = prefs.getBoolean("preference_fit_to_screen", true);
 				mOverlayDuration = Integer.parseInt(prefs.getString("preference_overlay_duration", "5000"));
 				mEdgesResistance = Integer.parseInt(prefs.getString("preference_edges_resistance", "1"));
 				mPageTransitionSpeed = Integer.parseInt(prefs.getString("preference_page_transition_speed", "2"));
 				mRightToLeft = prefs.getBoolean("preference_reading_direction", false);
 
 				switch(mPageTransitionSpeed) {
 					case 1:
 					mPageTransitionSpeed = 8;
 					break;
 
 					case 3:
 					mPageTransitionSpeed = 10;
 					break;
 
 					default:
 					mPageTransitionSpeed = 9;
 					break;
 				} 
 				
 				msg = mMainHandler.obtainMessage(VIEWER_WINDOW_CHANGED);
 				Bundle b = msg.getData();
 				b.putBoolean("highQuality", mHighQuality);
 				b.putBoolean("fullScreen", mFullScreen);
 				mMainHandler.sendMessage(msg);
 
 				mPreferencesLoaded = true;
 
 				return true;
 			}
 			case LOADER_SAVE_CURRENT_ALBUM: {
 				final SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(mCallback.get().getContext()).edit();
 				editor.putString("last_file", getAlbumUri().toString());
 //				Log.d("ComicsReader", "Save " + getAlbumUri().toString());
 				editor.commit();
 			}
 			}
 			return true;
 		}
 	};
 
 	private Callback mMainCallback = new Callback() {
 		public boolean handleMessage(Message msg) {
 			if (mLoaderHandler == null) {
 				// Looper stopped, we consider all terminating tasks invalid
 				return false;
 			}
 
 			switch (msg.what) {
 			case VIEWER_CHANGE_PAGE: {
 				Bundle b = msg.getData();
 				int page = b.getInt("page");
 
 //				Log.i("ComicsReader", "VIEWER_CHANGE_PAGE " + String.valueOf(page));
 				
 				if (page == -1) {
 					page = mAlbum.getCurrentPage();
 				}
 
 				// wait until album is loaded
 				if (page >= mAlbum.getNumPages()) {
 					page = mAlbum.getNumPages() - 1;
 				} else if (page < 0) {
 					page = 0;
 				}
 
 				if (mDoublePage) {
 					if (page > 0) {
 						page -= 1 - (page % 2);
 					} else {
 						page = 0;
 					}
 				}
 
 				if (page != mCurrentPage) {
 					if (mNextPage == page) {
 						if (mCallback.get().onSwapNextPage()) {
 							mWidth = mCallback.get().getPageWidth();
 							mNextPage = -1;
 							mPreviousPage = mCurrentPage;
 							mCurrentPage = page;
 
 							if (mOverlayDuration > -1) {
 								mCallback.get().onDisplayPageNumber(page, mAlbum.getNumPages(), mOverlayDuration);
 							}
 
 							mCallback.get().onPageChanged();
 						} else {
 							msg = mLoaderHandler.obtainMessage(LOADER_UPDATE_CURRENT_PAGE);
 							msg.getData().putInt("page", page);
 							mLoaderHandler.sendMessage(msg);
 						}
 					} else if (mPreviousPage == page) {
 						if (mCallback.get().onSwapPreviousPage()) {
 							mWidth = mCallback.get().getPageWidth();
 							mPreviousPage = -1;
 							mNextPage = mCurrentPage;
 							mCurrentPage = page;
 
 							if (mOverlayDuration > -1) {
 								mCallback.get().onDisplayPageNumber(page, mAlbum.getNumPages(), mOverlayDuration);
 							}
 
 							mCallback.get().onPageChanged();
 						} else {
 							msg = mLoaderHandler.obtainMessage(LOADER_UPDATE_CURRENT_PAGE);
 							msg.getData().putInt("page", page);
 							mLoaderHandler.sendMessage(msg);
 						}
 					} else {
 						msg = mMainHandler.obtainMessage(VIEWER_PREPARE_CURRENT_PAGE);
 						msg.getData().putInt("page", page);
 						mMainHandler.sendMessage(msg);
 					}
 				}
 
 				return true;
 			}
 			case VIEWER_PREPARE_CURRENT_PAGE: {
 				Bundle b = msg.getData();
 				boolean force = b.getBoolean("force");
 				int page = b.getInt("page");
 
 //				Log.i("ComicsReader", "VIEWER_PREPARE_CURRENT_PAGE " + String.valueOf(page));
 				
 				if (force) {
 					// TODO: remove all pending messages
 
 					// release all cached images in FullImageView
 					if (mCallback.get().onReset()) {
 						mCurrentPage = -1;
 						mNextPage = -1;
 						mPreviousPage = -1;
 					}
 				}
 				
 				if (page > -1 && mCallback.get().onPrepareCurrentPage(page, mCurrentPage)) {
 					msg = mLoaderHandler.obtainMessage(LOADER_UPDATE_CURRENT_PAGE);
 					msg.getData().putInt("page", page);
 					mLoaderHandler.sendMessage(msg);
 				}
 
 				return true;
 			}
 			case VIEWER_UPDATE_CURRENT_PAGE: {
 				Bundle b = msg.getData();
 				int page = b.getInt("page");
 				Bitmap bitmap = (Bitmap)msg.obj;
 
 //				Log.i("ComicsReader", "VIEWER_UPDATE_CURRENT_PAGE " + String.valueOf(page));
 				
 				if (mCallback.get().onUpdateCurrentPage(bitmap)) {
 					mWidth = mCallback.get().getPageWidth();
 					mCurrentPage = page;
 
 					if (mOverlayDuration > -1) {
 						mCallback.get().onDisplayPageNumber(page, mAlbum.getNumPages(), mOverlayDuration);
 					}
 
 					mCallback.get().onPageChanged();
 				}
 
 				return true;
 			}
 			case VIEWER_PREPARE_NEXT_PAGE: {
 				int page = getNextPage();
 
 //				Log.i("ComicsReader", "VIEWER_PREPARE_NEXT_PAGE " + String.valueOf(page));
 				
 				if (mCallback.get().onPrepareNextPage(page, mNextPage)) {
 					msg = mLoaderHandler.obtainMessage(LOADER_UPDATE_NEXT_PAGE);
 					msg.getData().putInt("page", page);
 					mLoaderHandler.sendMessage(msg);
 				}
 
 				return true;
 			}
 			case VIEWER_UPDATE_NEXT_PAGE: {
 				Bundle b = msg.getData();
 				int page = b.getInt("page");
 				Bitmap bitmap = (Bitmap)msg.obj;
 
 //				Log.i("ComicsReader", "VIEWER_UPDATE_NEXT_PAGE " + String.valueOf(page));
 				
 				if (mCallback.get().onUpdateNextPage(bitmap)) {
 					mNextPage = page;
 				}
 
 				return true;
 			}
 			case VIEWER_PREPARE_PREVIOUS_PAGE: {
 				int page = getPreviousPage();
 
 //				Log.i("ComicsReader", "VIEWER_PREPARE_PREVIOUS_PAGE " + String.valueOf(page));
 				
 				if (mCallback.get().onPreparePreviousPage(page, mPreviousPage)) {
 					msg = mLoaderHandler.obtainMessage(LOADER_UPDATE_PREVIOUS_PAGE);
 					msg.getData().putInt("page", page);
 					mLoaderHandler.sendMessage(msg);
 				}
 
 				return true;
 			}
 			case VIEWER_UPDATE_PREVIOUS_PAGE: {
 				Bundle b = msg.getData();
 				int page = b.getInt("page");
 				Bitmap bitmap = (Bitmap)msg.obj;
 
 //				Log.i("ComicsReader", "VIEWER_UPDATE_PREVIOUS_PAGE " + String.valueOf(page));
 				
 				if (mCallback.get().onUpdatePreviousPage(bitmap)) {
 					mPreviousPage = page;
 				}
 
 				return true;
 			}
 			case VIEWER_WINDOW_CHANGED: {
 				Bundle b = msg.getData();
 				boolean fullScreen = b.getBoolean("fullScreen");
 				boolean highQuality = b.getBoolean("highQuality");
 
 				mCallback.get().onWindowChanged(highQuality, fullScreen);
 
 				return true;
 			}
 			case VIEWER_SCROLL_PAGE: {
 				Bundle bundle = msg.getData();
 				int page = bundle.getInt("page");
 
 				mCallback.get().onPageScrolled(page);
 
 				return true;
 			}
 			case VIEWER_OPEN_BEGIN: {
 				mCallback.get().onOpenBegin();
 
 				return true;
 			}
 			case VIEWER_OPEN_END: {
 				mCallback.get().onOpenEnd();
 
 				return true;
 			}
 			case VIEWER_ERROR: {
 				Bundle bundle = msg.getData();
 				int error = bundle.getInt("error");
 
 				mCallback.get().onError(error);
 
 				return true;
 			}
 			default:
 				break;
 			}
 			
 			return false;
 		}
 	};
 }
