 /**
  * Copyright (c) 2011, Charles Brandon Keifer Miller
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
  * DISCLAIMED. IN NO EVENT SHALL Charles Brandon Keifer Miller BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 **/
 
 package com.keifermiller.inkbar.fragments;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.Bundle;
 import android.os.Environment;
 import android.support.v4.app.Fragment;
 import android.support.v4.view.Menu;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.LayoutInflater;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.EditText;
 import android.widget.TextView;
 
 import com.keifermiller.inkbar.Codes;
 import com.keifermiller.inkbar.ExternalStorageBroadcastReceiver;
 import com.keifermiller.inkbar.IMountStateHandler;
 import com.keifermiller.inkbar.R;
 import com.keifermiller.inkbar.activities.CreateDocumentActivity;
 import com.keifermiller.inkbar.activities.PreferencesActivity;
 import com.keifermiller.inkbar.activities.TextEditorActivity;
 import com.keifermiller.inkbar.documentutils.DocumentFileManager;
 import com.keifermiller.inkbar.documentutils.DocumentReader;
 import com.keifermiller.inkbar.documentutils.DocumentWriter;
 import com.keifermiller.inkbar.documentutils.HistoryFileManager;
 import com.keifermiller.inkbar.exceptions.DirectoryFileExpectedException;
 import com.keifermiller.inkbar.exceptions.FileFoundException;
 
 
 public class TextEditorFragment extends Fragment implements IMountStateHandler{
 
 	// Indicates whether there are changes in the text editor that should be
 	// placed in a save file or history file when the activity stops.
 	private boolean mUnsavedHistory;
 	// The filename of the document being worked upon by the editor. Does not
 	// include path.
 	private String mDocumentName;
 	// The component that warns the user of unsaved changes.
 	private TextView mHistoryFileWarning;
 	// Self Explanatory
 	private EditText mDocumentTextEditor;
 	// The class's line to the underlying files that relate to the document
 	private File mDirectoryFile = new File(Environment.getExternalStorageDirectory(), "Documents");
 	private DocumentFileManager mDocumentManager = new DocumentFileManager();
 	
 	private File mHistoryDirectoryFile = new File(Environment.getExternalStorageDirectory(), "/Android/data/com.keifermiller.inkbar/files/history");
 	private HistoryFileManager mHistoryManager = new HistoryFileManager();
 	
 	// Keeps track of whether the editor is starting or already running. For
 	// the benefit of the text watcher.
 	private boolean mStarted;
 	
 	private ExternalStorageBroadcastReceiver mExternalStorageListener;
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		return inflater
 				.inflate(R.layout.text_editor_fragment, container, false);
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 
 		// Get the extra data from the intent, which we will use to set
 		// the document name
 		Intent intent = getActivity().getIntent();
 		String action = intent.getAction();
 		Bundle extras = intent.getExtras();
 
 		if (TextEditorActivity.OPEN_DOCUMENT_ACTION.equals(action)) {
 			mDocumentName = extras.getString("filename");
 		}
 		
 		getActivity().setResult(Activity.RESULT_OK);
 		
 		// If the document name is null or empty, something went wrong. Exit!!!
 		if (mDocumentName == null || mDocumentName.equals("")) {
 			getActivity().finish();
 		}
 
 		mExternalStorageListener = new ExternalStorageBroadcastReceiver(this);
 		
 		// Now that the document name is decided, the name of it should be
 		// displayed to the user.
 		getActivity().getSupportActionBar().setSubtitle(mDocumentName);
 
 		// We just loaded the document, so there is no way the text has changed.
 		mUnsavedHistory = false;
 
 		// the document helper now needs pointed in the right direction. Open
 		// that file! If we cannot access the file, gracefully act accordingly.
 
 		try {
 			mDocumentManager.setDirectory(mDirectoryFile);
 			mDocumentManager.selectFile(mDocumentName);
 			mHistoryManager.setDirectory(mHistoryDirectoryFile);
 			mHistoryManager.setActiveDocument(mDocumentManager.getDocument());
 		} catch (FileNotFoundException e) {
 			// TODO exit with file not found error code
 			e.printStackTrace();
 		} catch (DirectoryFileExpectedException e) {
 			// TODO exit with error opening file code
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		setHasOptionsMenu(true);
 
 		// The display to the user that the text displayed does not reflect the
 		// contents of their file.
 		// TODO: animate visibility changes
 		// TODO: display a quick access menu upon click, which lets the user:
 		// 		 revert to save file.
 		// 		 walk through history files.
 		// 		 save current contents into a new file.
 		mHistoryFileWarning = (TextView) getActivity().findViewById(
 				R.id.history_file_warning);
 
 		// The meat and potatoes of the fragment.
 		mDocumentTextEditor = (EditText) getActivity().findViewById(
 				R.id.body_editor);
 
 		// Inform the fragment that we need to save a history file if the
 		// user exits without first saving their changes.
 		mDocumentTextEditor.addTextChangedListener(new TextWatcher() {
 			public void afterTextChanged(Editable s) {
 				if (mStarted) {
 					mUnsavedHistory = true;
 					mHistoryFileWarning.setVisibility(View.VISIBLE);
 					getActivity().invalidateOptionsMenu();
 
 				}
 			}
 
 			@Override
 			public void beforeTextChanged(CharSequence s, int start, int count,
 					int after) {
 			}
 
 			@Override
 			public void onTextChanged(CharSequence s, int start, int before,
 					int count) {
 			}
 		});
 	}
 
 	@Override
 	public void onStart() {
 		super.onStart();
 		// Used by the text editor's text watcher to ensure it does not flag
 		// changes that occur because the editor is starting. We only want to
 		// catch changes that originate from the user.
 		mStarted = false;
 		
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
 		if(state.equals(Environment.MEDIA_MOUNTED)) {
 			handleMounted();
 		} else if(state.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
 			handleReadonly();
 		} else {
 			handleUnmounted();
 		}
 		
 		// Load the text from the document into the editor. Handle any situation
 		// that went awry.
 		if (mHistoryManager.historyExists()) {
 			readFileToTextEdit(mHistoryManager.getLastHistory().getPath());
 		} else {
 			readFileToTextEdit(mDocumentManager.getDocument().getPath());
 		}
 		
 		getActivity().invalidateOptionsMenu();
 		mUnsavedHistory = false;
 
 		updateHistoryFileWarningVisiblity();
 		mStarted = true;
 	}
 
 	@Override
 	public void onStop() {
 		super.onStop();
 		
 		if (mUnsavedHistory) {
 			try {
 				mHistoryManager.createHistoryFile();
 				saveTextEditToFile(mHistoryManager.getLastHistory().getPath());
 			} catch (FileFoundException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 			mUnsavedHistory = false;
 		}
 		mDocumentTextEditor.setText("");
 		
 		getActivity().unregisterReceiver(mExternalStorageListener);
 	}
 
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 		inflater.inflate(R.menu.editor_options_menu, menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			getActivity().finish();
 			return true;
 		case R.id.new_file_menu_item:
 			startActivityForResult(new Intent(this.getActivity(),
 					CreateDocumentActivity.class)
 					.setAction(CreateDocumentActivity.NEW_DOCUMENT_ACTION), Codes.GET_STATUS);
 			return true;
 		case R.id.save_file_menu_item:
 			saveTextEditToFile(mDocumentManager.getDocument().getPath());
 			try {
 				mHistoryManager.deleteAllHistory();
 				mUnsavedHistory = false;
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 			updateHistoryFileWarningVisiblity();
 			getActivity().invalidateOptionsMenu();
 			return true;
 		case R.id.share_file_menu_item:
 			Intent shareIntent = new Intent();
 			shareIntent.setAction(Intent.ACTION_SEND);
			shareIntent.setType("text/plain");
 			shareIntent.putExtra(Intent.EXTRA_TEXT, mDocumentTextEditor
 					.getText().toString());
 			startActivity(Intent.createChooser(shareIntent, getActivity()
 					.getText(R.string.share_chooser_title)));
 			return true;
 		case R.id.delete_file_menu_item:
 			try {
 				mDocumentManager.deleteFile();
 				mHistoryManager.deleteAllHistory();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 				mUnsavedHistory = false;
 				getActivity().finish();
 			return true;
 		case R.id.revert_file_menu_item:
 			if (mHistoryManager.historyExists()) {
 				try {
 					mHistoryManager.deleteAllHistory();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 			readFileToTextEdit(mDocumentManager.getDocument().getPath());
 			mUnsavedHistory = false;
 			updateHistoryFileWarningVisiblity();
 			getActivity().invalidateOptionsMenu();
 			return true;
 		case R.id.preferences_menu_item:
 			startActivity(new Intent(getActivity(), PreferencesActivity.class));
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 	
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		if (requestCode == Codes.GET_STATUS) {
 			if (resultCode == Codes.ERR_NOT_MOUNTED) {
 				handleUnmounted();
 			}
 			
 			if (resultCode == Codes.ERR_READ_ONLY) {
 				handleReadonly();
 			}
 			
 			if (resultCode == Activity.RESULT_OK) {
 				this.getActivity().setResult(Activity.RESULT_OK);
 			}
 		}
 	}
 	
 	@Override
 	public void handleUnmounted() {
 		this.getActivity().setResult(Codes.ERR_NOT_MOUNTED);
 		getActivity().finish();
 	}
 	
 	@Override
 	public void handleReadonly() {
 		this.getActivity().setResult(Codes.ERR_READ_ONLY);
 		getActivity().finish();
 	}
 
 	@Override
 	public void handleMounted() {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	private void readFileToTextEdit(String path) {
 		try {
 			mDocumentTextEditor.setText(DocumentReader.read(new FileReader(path)));
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	private void saveTextEditToFile(String path) {
 		try {
 			DocumentWriter.write(mDocumentTextEditor.getText().toString(),
 					new BufferedWriter(new FileWriter(new File(path))));
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	// If the file opened has existing history files, or if the editor contains
 	// changes to that file, then the fragment will display the the history
 	// warning to the user by changing it's visibility to VISIBLE.
 	//
 	// Otherwise, this warning will be removed from the screen.
 	private void updateHistoryFileWarningVisiblity() {
 		if (mHistoryManager.historyExists() || mUnsavedHistory) {
 			// Animation slideIn = AnimationUtils.loadAnimation(getActivity(),
 			// R.anim.history_warning_slide_in);
 			mHistoryFileWarning.setVisibility(View.VISIBLE);
 			// mHistoryFileWarning.startAnimation(slideIn);
 		} else {
 			mHistoryFileWarning.setVisibility(View.GONE);
 		}
 	}
 
 
 }
