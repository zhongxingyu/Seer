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
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.keifermiller.inkbar.Codes;
 import com.keifermiller.inkbar.Constants;
 import com.keifermiller.inkbar.IMountStateHandler;
 import com.keifermiller.inkbar.R;
 import com.keifermiller.inkbar.activities.CreateDocumentActivity;
 import com.keifermiller.inkbar.activities.PreferencesActivity;
 import com.keifermiller.inkbar.activities.TextEditorActivity;
 import com.keifermiller.inkbar.documentutils.HistoryFileManager;
 import com.keifermiller.inkbar.exceptions.DirectoryFileExpectedException;
 import com.keifermiller.inkbar.exceptions.FileFileExpectedException;
 
 public class DashboardListFragment extends IBFragment implements
 		IMountStateHandler {
 
 	private File mDirectory;
 	private HistoryFileManager mHistoryManager;
 	private ArrayAdapter<String> mAdapter;
 	private ListView mDocumentListView;
 	private boolean mSDMounted;
 	private boolean mWarningShown;
 	private LinearLayout mCreateHeader;
 	private boolean mDirWritable;
 	private View mReadonlyWarning;
 
 	// /////////////////////////////////////////////////////////////////////////
 	// MountStateHandler //
 	// /////////////////////////////////////////////////////////////////////////
 
 	public void handleMounted() {
 		super.handleMounted();
 		mSDMounted = true;
 		mDirWritable = true;
 		updateReadonlyWarning();
 		updateSDCardStatus();
 	}
 
 	public void handleUnmounted() {
 		super.handleUnmounted();
 		mSDMounted = false;
 		mDirWritable = false;
 		updateReadonlyWarning();
 		updateSDCardStatus();
 	}
 
 	public void handleReadonly() {
 		super.handleReadonly();
 		mSDMounted = true;
 		mDirWritable = false;
 		updateReadonlyWarning();
 		updateSDCardStatus();
 	}
 
 	private void handleWriteable() {
 		mDirWritable = true;
 		updateReadonlyWarning();
 	}
 	
 	/* (non-Javadoc)
 	 * @see com.keifermiller.inkbar.fragments.IBFragment#handleFileCreated(java.lang.String)
 	 */
 	@Override
 	protected void handleFileCreated(String path) {
 		super.handleFileCreated(path);
 		File file = new File(path);
 		mAdapter.add(file.getName());
 		mAdapter.notifyDataSetChanged();
 	}
 
 	/* (non-Javadoc)
 	 * @see com.keifermiller.inkbar.fragments.IBFragment#handleFileDeleted(java.lang.String)
 	 */
 	@Override
 	protected void handleFileDeleted(String path) {
 		super.handleFileDeleted(path);
 		File file = new File(path);
 		mAdapter.remove(file.getName());
 		mAdapter.notifyDataSetChanged();
 	}
 	
 	/* (non-Javadoc)
 	 * @see com.keifermiller.inkbar.fragments.IBFragment#handleFileMovedFrom(java.lang.String)
 	 */
 	@Override
 	protected void handleFileMovedFrom(String path) {
 		super.handleFileMovedFrom(path);
 		File file = new File(path);
 		mAdapter.remove(file.getName());
 		mAdapter.notifyDataSetChanged();
 	}
 
 	/* (non-Javadoc)
 	 * @see com.keifermiller.inkbar.fragments.IBFragment#handleFileMovedTo(java.lang.String)
 	 */
 	@Override
 	protected void handleFileMovedTo(String path) {
 		super.handleFileMovedTo(path);
 		File file = new File(path);
 		mAdapter.add(file.getName());
 		mAdapter.notifyDataSetChanged();
 	}
 	
 	/* (non-Javadoc)
 	 * @see com.keifermiller.inkbar.fragments.IBFragment#handleBaseFileDeleted()
 	 */
 	@Override
 	protected void handleBaseFileDeleted() {
 		super.handleBaseFileDeleted();
 		this.displayErrorMessage(R.string.warn_base_file_deleted);
 	}
 
 	// /////////////////////////////////////////////////////////////////////////
 	// Life Cycle Methods //
 	// /////////////////////////////////////////////////////////////////////////
 
 
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater,
 	 * android.view.ViewGroup, android.os.Bundle)
 	 */
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		return inflater.inflate(R.layout.dashboard_list, container, false);
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 		mDirectory = Constants.DOCUMENTS_DIRECTORY;
 		mHistoryManager = new HistoryFileManager();
 		mDocumentListView = (ListView) getActivity().findViewById(
 				android.R.id.list);
 		mAdapter = new ArrayAdapter<String>(getActivity(),
 				R.layout.dashboard_list_item);
 		mAdapter.setNotifyOnChange(true);
 		mReadonlyWarning = getActivity().findViewById(R.id.readonly_warning);
 		mWarningShown = false;
 		mCreateHeader = (LinearLayout) getActivity().findViewById(
 				R.id.top_create_item);
 
 		mCreateHeader.setClickable(true);
 		mCreateHeader.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				if(mDirectory != null && mDirectory.exists() && mDirectory.isDirectory()) {
 				startActivityForResult(
 						new Intent(getActivity(), CreateDocumentActivity.class)
 								.setAction(
 										CreateDocumentActivity.NEW_DOCUMENT_ACTION)
 								.setData(
 										Uri.parse("file:/"
 												+ mDirectory
 														.getAbsolutePath())),
 						Codes.GET_STATUS);
 						}
 				return;
 			}
 		});
 
 		registerForContextMenu(mDocumentListView);
 
 		mDocumentListView.setOnItemClickListener(new OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> l, View v, int position,
 					long id) {
 				String documentName = (String) (mAdapter.getItem(position));
 				if (documentName == null) {
 					return;
 				}
 				if(mDirectory != null && mDirectory.exists() && mDirectory.isDirectory()) {
 				startActivityForResult(
 						new Intent(getActivity(), TextEditorActivity.class)
 								.setAction(Intent.ACTION_VIEW).setDataAndType(
 										Uri.parse("file:/"
 												+ mDirectory
 														.getAbsolutePath()
 												+ "/" + documentName),
 										"text/plain"), Codes.GET_STATUS);
 				}
 			}});
 		mDocumentListView.setAdapter(mAdapter);
 		this.setFileToObserve(Constants.DOCUMENTS_DIRECTORY);
 	}
 
 	@Override
 	public void onStart() {
 		super.onStart();
 
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 
 		if (mSDMounted) {
 			File directory = mDirectory;
 			if (directory.exists()) {
 				if (!directory.isDirectory()) {
 					this.displayErrorMessage(R.string.error_parent_not_a_directory);
 					return;
 				}
 			} else if (!directory.mkdirs()) {
 				this.displayErrorMessage(R.string.warn_unable_to_find_directory);
 				return;
 			}
 
 			try {
 				Constants.HISTORY_DIRECTORY.mkdirs();
 				mHistoryManager.setDirectory(Constants.HISTORY_DIRECTORY);
 			} catch (FileNotFoundException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (DirectoryFileExpectedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			
 			if (!directory.canWrite()) {
 				handleReadonly();
 			} else {
 				handleWriteable();
 			}
 
 			populateDocumentsInList();
 		}
 	}
 
 	@Override
 	public void onStop() {
 		super.onStop();
 		mAdapter.clear();
 	}
 
 	// /////////////////////////////////////////////////////////////////////////
 	// Menu Hook Methods //
 	// /////////////////////////////////////////////////////////////////////////
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * android.support.v4.app.Fragment#onCreateContextMenu(android.view.ContextMenu
 	 * , android.view.View, android.view.ContextMenu.ContextMenuInfo)
 	 */
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v,
 			ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 
 		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
 
 		menu.add(Menu.NONE, R.id.open_file_menu_item, Menu.NONE,
 				R.string.open_file_menu_item_title);
 
 		if (mDirWritable) {
 			menu.add(Menu.NONE, R.id.delete_file_menu_item, Menu.NONE,
 					R.string.delete_file_menu_item_title);
 		}
 		// Display to user which document they are acting on.
 		menu.setHeaderTitle(((TextView) info.targetView).getText().toString());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * android.support.v4.app.Fragment#onContextItemSelected(android.view.MenuItem
 	 * )
 	 */
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		// Retrieve the name of the document about to be operated on
 		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
 				.getMenuInfo();
 		String documentName = ((TextView) info.targetView).getText().toString();
 
 		switch (item.getItemId()) {
 		case R.id.open_file_menu_item:
 			startActivityForResult(
 					new Intent(this.getActivity(), TextEditorActivity.class)
 							.setAction(Intent.ACTION_VIEW).setDataAndType(
 									Uri.parse("file:/"
 											+ mDirectory
 													.getAbsolutePath() + "/"
 											+ documentName), "text/plain"),
 					Codes.GET_STATUS);
 			return true;
 		case R.id.delete_file_menu_item:
 			// Delete from file system
 			try {
 				File file = new File(mDirectory, documentName);
 				mHistoryManager.setActiveDocument(file);
 				mHistoryManager.deleteAllHistory();
 				if (!file.delete()) {
 					Toast.makeText(
 							getActivity(),
 							R.string.warn_unable_to_find_file + "\n"
 									+ R.string.warn_unable_to_delete,
 							Toast.LENGTH_SHORT).show();
 				} else { 
 				mAdapter.remove(documentName);
 				}
 			} catch (FileFileExpectedException e) {
 				Toast.makeText(
 						getActivity(),
 						R.string.warn_unable_to_find_file + "\n"
 								+ R.string.warn_unable_to_delete,
 						Toast.LENGTH_SHORT).show();
 			} catch (FileNotFoundException e) {
 				Toast.makeText(
 						getActivity(),
 						R.string.warn_unable_to_find_file + "\n"
 								+ R.string.warn_unable_to_delete,
 						Toast.LENGTH_SHORT).show();
 			} catch (IOException e) {
 				Toast.makeText(
 						getActivity(),
 						R.string.warn_io_exception + "\n"
 								+ R.string.warn_unable_to_delete,
 						Toast.LENGTH_SHORT).show();
 			}
 			return true;
 		default:
 			return super.onContextItemSelected(item);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * android.support.v4.app.Fragment#onCreateOptionsMenu(android.support.v4
 	 * .view.Menu, android.view.MenuInflater)
 	 */
 	@Override
 	public void onCreateOptionsMenu(android.support.v4.view.Menu menu,
 			MenuInflater inflater) {
 		inflater.inflate(R.menu.dashboard_options_menu, menu);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * android.support.v4.app.Fragment#onOptionsItemSelected(android.view.MenuItem
 	 * )
 	 */
 	@Override
	public boolean onOptionsItemSelected(android.support.v4.view.MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 		case R.id.preferences_menu_item:
 			startActivity(new Intent(getActivity(), PreferencesActivity.class));
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	// /////////////////////////////////////////////////////////////////////////
 	// User Interface Convenience Methods //
 	// /////////////////////////////////////////////////////////////////////////
 
 	/**
 	 * Populates mAdapter with all of the readable/writable FILES in
 	 * mDirectoryFile.
 	 */
 	private void populateDocumentsInList() {
 		mAdapter.clear();
 
 		File[] docs = mDirectory.listFiles();
 
 		if (docs == null) {
 			return;
 		}
 
 		for (File doc : docs) {
 			if (doc.canRead() && doc.canWrite() && doc.isFile()) { // TODO:
 																	// Store
 																	// files in
 																	// a map
 				// indicated r/w status, be
 				// able to view
 				mAdapter.add(doc.getName()); // nonwriteable files.
 			}
 		}
 	}
 
 	private void updateSDCardStatus() {
 		if (mSDMounted) {
 			if (mWarningShown) {
 				this.clearErrorMessage();
 				mWarningShown = false;
 			} else {
 				return;
 			}
 		} else {
 			if (mWarningShown) {
 				return;
 			} else {
 				this.displayErrorMessage(R.string.sd_card_warning);
 				mWarningShown = true;
 			}
 		}
 	}
 
 	/**
 	 * 
 	 */
 	private void updateReadonlyWarning() {
 		if (mDirWritable == false) {
 			mReadonlyWarning.setVisibility(View.VISIBLE);
 			mCreateHeader.setVisibility(View.GONE);
 		} else {
 			mReadonlyWarning.setVisibility(View.GONE);
 			mCreateHeader.setVisibility(View.VISIBLE);
 		}
 	}
 }
