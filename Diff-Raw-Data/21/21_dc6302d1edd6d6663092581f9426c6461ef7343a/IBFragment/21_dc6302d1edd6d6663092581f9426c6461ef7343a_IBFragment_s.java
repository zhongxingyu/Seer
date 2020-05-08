 /**
  * Copyright (c) 2011 Keifer Miller
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *     * Redistributions of source code must retain the above copyright
  *       notice, this list of conditions and the following disclaimer.
  *     * Redistributions in binary form must reproduce the above copyright
  *       notice, this list of conditions and the following disclaimer in the
  *       documentation and/or other materials provided with the distribution.
  *     * Neither the name of Ink Bar nor the
  *       names of its contributors may be used to endorse or promote products
  *       derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL KEIFER MILLER BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  **/
 package com.keifermiller.inkbar.fragments;
 
 import java.io.File;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.FileObserver;
 import android.support.v4.app.Fragment;
 
 import com.keifermiller.inkbar.Codes;
 import com.keifermiller.inkbar.ExternalStorageBroadcastReceiver;
 import com.keifermiller.inkbar.IMountStateHandler;
 import com.keifermiller.inkbar.activities.IBActivity;
 
 /**
  * @author keifer
  * 
  */
 public class IBFragment extends Fragment implements IMountStateHandler {
 
 	private ExternalStorageBroadcastReceiver mExternalStorageListener;
 	private FileObserver mFileObserver;
 	private File mFileToObserve;
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
 	 */
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 		mExternalStorageListener = new ExternalStorageBroadcastReceiver(this);
 		mFileObserver = null;
 		mFileToObserve = null;
 		setHasOptionsMenu(true);
 	}
 
	/*
	 * (non-Javadoc)
	 * 
 	 * @see android.support.v4.app.Fragment#onStart()
 	 */
 	@Override
	public void onResume() {
		super.onResume();
 
 		IntentFilter filter = new IntentFilter();
 		filter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
 		filter.addAction(Intent.ACTION_MEDIA_CHECKING);
 		filter.addAction(Intent.ACTION_MEDIA_EJECT);
 		filter.addAction(Intent.ACTION_MEDIA_REMOVED);
 		filter.addAction(Intent.ACTION_MEDIA_UNMOUNTABLE);
 		filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
 		filter.addAction(Intent.ACTION_MEDIA_NOFS);
 		filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
 		filter.addAction(Intent.ACTION_MEDIA_SHARED);
 		filter.addDataScheme("file");
 
 		getActivity().registerReceiver(mExternalStorageListener, filter);
 
 		String state = Environment.getExternalStorageState();
 		if (state.equals(Environment.MEDIA_MOUNTED)) {
 			handleMounted();
 		} else if (state.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
 			handleReadonly();
 		} else {
 			handleUnmounted();
 		}
 
 		if (getObservedFile() != null) {
 			mFileObserver = new FileObserver(getObservedFile().getPath()) {
 				@Override
 				public void onEvent(int event, String path) {
 					final int fevent = event;
 					final String fpath = path;
 					Runnable runnable = new Runnable() {
 						@Override
 						public void run() {
 							switch (fevent) {
 							case FileObserver.ATTRIB:
 								handleFileAttributeChange(fpath);
 								break;
 							case FileObserver.CREATE:
 								handleFileCreated(fpath);
 								break;
 							case FileObserver.DELETE:
 								handleFileDeleted(fpath);
 								break;
 							case FileObserver.DELETE_SELF:
 								handleBaseFileDeleted();
 								break;
 							case FileObserver.MODIFY:
 								handleFileModified(fpath);
 								break;
 							case FileObserver.MOVE_SELF:
 								handleBaseFileMovedTo(fpath);
 								break;
 							case FileObserver.MOVED_FROM:
 								handleFileMovedFrom(fpath);
 								break;
 							case FileObserver.MOVED_TO:
 								handleFileMovedTo(fpath);
 								break;
 							}
 						}
 					};
 					((IBActivity) getActivity()).getHandler().post(runnable);
 				}
 			};
 			startObservingFile();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.support.v4.app.Fragment#onStop()
 	 */
 	@Override
 	public void onStop() {
 		getActivity().unregisterReceiver(mExternalStorageListener);
 		stopObservingFile();
 		mFileObserver = null;
 		super.onStop();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.support.v4.app.Fragment#onActivityResult(int, int,
 	 * android.content.Intent)
 	 */
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 
 		if (resultCode == Codes.ERR_NOT_MOUNTED) {
 			handleUnmounted();
 		}
 
 		if (resultCode == Codes.ERR_READ_ONLY) {
 			handleReadonly();
 		}
 
 		if (resultCode == Activity.RESULT_OK) {
 			handleMounted();
 		}
 	}
 
 	public void setFileToObserve(File file) {
 		if (file == null) {
 			throw new IllegalArgumentException("file may not be null");
 		}
 		mFileToObserve = file;
 	}
 
 	public File getObservedFile() {
 		return mFileToObserve;
 	}
 
 	public void startObservingFile() {
 		if (mFileObserver != null) {
 			mFileObserver.startWatching();
 		}
 	}
 
 	public void stopObservingFile() {
 		if (mFileObserver != null) {
 			mFileObserver.stopWatching();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.keifermiller.inkbar.IMountStateHandler#handleMounted()
 	 */
 	@Override
 	public void handleMounted() {
 		this.getActivity().setResult(Activity.RESULT_OK);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.keifermiller.inkbar.IMountStateHandler#handleUnmounted()
 	 */
 	@Override
 	public void handleUnmounted() {
 		getActivity().setResult(Codes.ERR_NOT_MOUNTED);
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.keifermiller.inkbar.IMountStateHandler#handleReadonly()
 	 */
 	@Override
 	public void handleReadonly() {
 		getActivity().setResult(Codes.ERR_READ_ONLY);
 	}
 
 	protected void handleFileAttributeChange(String path) {
 
 	}
 
 	protected void handleFileCreated(String path) {
 
 	}
 
 	protected void handleFileDeleted(String path) {
 
 	}
 
 	protected void handleBaseFileDeleted() {
 
 	}
 
 	protected void handleFileModified(String path) {
 
 	}
 
 	protected void handleBaseFileMovedTo(String path) {
 
 	}
 
 	protected void handleFileMovedFrom(String path) {
 
 	}
 
 	protected void handleFileMovedTo(String path) {
 
 	}
 }
