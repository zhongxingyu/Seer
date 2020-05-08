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
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 
 import android.app.Activity;
 import android.content.ActivityNotFoundException;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
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
 import android.widget.Toast;
 
 import com.keifermiller.inkbar.Codes;
 import com.keifermiller.inkbar.Constants;
 import com.keifermiller.inkbar.IMountStateHandler;
 import com.keifermiller.inkbar.R;
 import com.keifermiller.inkbar.activities.CreateDocumentActivity;
 import com.keifermiller.inkbar.activities.PreferencesActivity;
 import com.keifermiller.inkbar.documentutils.DocumentReader;
 import com.keifermiller.inkbar.documentutils.DocumentWriter;
 import com.keifermiller.inkbar.documentutils.HistoryFileManager;
 import com.keifermiller.inkbar.exceptions.DirectoryFileExpectedException;
 import com.keifermiller.inkbar.exceptions.FileFileExpectedException;
 
 public class TextEditorFragment extends IBFragment implements
 		IMountStateHandler {
 
 	private boolean mUnsavedHistory;
 	private String mDocumentName;
 	private String mDirectoryPath;
 	private TextView mHistoryFileWarning;
 	private EditText mDocumentEditor;
 	private boolean mStarted;
 	private boolean mReverting;
 
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.keifermiller.inkbar.fragments.IBFragment#handleBaseFileDeleted()
 	 */
 	@Override
 	protected void handleBaseFileDeleted() {
 		super.handleBaseFileDeleted();
 		this.displayErrorMessage(R.string.warn_unable_to_find_directory);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.keifermiller.inkbar.fragments.IBFragment#handleFileCreated(java.lang
 	 * .String)
 	 */
 	@Override
 	protected void handleFileCreated(String path) {
 		super.handleFileCreated(path);
 		File file = new File(path);
 		if (this.setManagersTo(mDirectoryPath, mDocumentName)) {
 			if (file.getName().equals(mDocumentName)) {
 				this.clearErrorMessage();
 				// TODO: display unsaved changes warning.
 			}
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.keifermiller.inkbar.fragments.IBFragment#handleFileDeleted(java.lang
 	 * .String)
 	 */
 	@Override
 	protected void handleFileDeleted(String path) {
 		super.handleFileDeleted(path);
 
 		this.setManagersTo(mDirectoryPath, mDocumentName);
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		return inflater
 				.inflate(R.layout.text_editor_fragment, container, false);
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 		Intent intent = getActivity().getIntent();
 		String action = intent.getAction();
 		Uri data = intent.getData();
 
 		if (data == null) {
 			getActivity().finish();
 			return;
 		}
 
 		if (!data.getScheme().equals("file")) {
 			getActivity().finish();
 			return;
 		}
 
 		mReverting = false;
 		
		// Using the Uri methods to produce a file truncates the path?
		File document = new File(data.toString().replace("file:/", ""));
 
 		mDocumentName = document.getName();
 		mDirectoryPath = document.getParentFile().getAbsolutePath();
 
 		getActivity().setResult(Activity.RESULT_OK);
 
 		if (!Intent.ACTION_VIEW.equals(action) || mDocumentName == null
 				|| mDocumentName.equals("") || mDirectoryPath == null
 				|| mDirectoryPath.equals("")) {
 			getActivity().setResult(Activity.RESULT_CANCELED);
 			getActivity().finish();
 			return;
 		}
 
 		getActivity().getSupportActionBar().setTitle(mDocumentName);
 		getActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(true);
 
 		mUnsavedHistory = false;
 
 		setHasOptionsMenu(true);
 
 		// TODO: animate visibility changes
 		// TODO: display a quick access menu upon click, which lets the user:
 		//  revert to save file.
 		//  walk through history files.
 		//  save current contents into a new file.
 		mHistoryFileWarning = (TextView) getActivity().findViewById(
 				R.id.history_file_warning);
 
 		mDocumentEditor = (EditText) getActivity().findViewById(
 				R.id.body_editor);
 
 		mDocumentEditor.addTextChangedListener(new TextWatcher() {
 
 			public void afterTextChanged(Editable s) {
 				if (mStarted && !mReverting) {
 					mUnsavedHistory = true;
 					updateHistoryFileWarningVisiblity();
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
 		this.setFileToObserve(new File(mDirectoryPath));
 	}
 
 	@Override
 	public void onStart() {
 		super.onStart();
 		mStarted = false;
 
 		if (!this.setManagersTo(mDirectoryPath, mDocumentName)) {
 			return;
 		}
 
 		HistoryFileManager manager = new HistoryFileManager();
 		
 		try {
 			manager.setDirectory(Constants.HISTORY_DIRECTORY);
 			manager.setActiveDocument(mDirectoryPath + "/" + mDocumentName);
 			if (manager.historyExists()) {
 				readFileToTextEdit(manager.getLastHistory().getPath());
 			} else {
 				readFileToTextEdit(mDirectoryPath + "/" + mDocumentName);
 			}
 			mUnsavedHistory = false;
 
 			updateHistoryFileWarningVisiblity();
 		} catch (FileNotFoundException e) {
 			this.displayErrorMessage(R.string.warn_unable_to_find_file);
 		} catch (DirectoryFileExpectedException e) {
 			this.displayErrorMessage(R.string.warn_unable_to_find_directory);
 		} catch (IOException e) {
 			this.displayErrorMessage(R.string.warn_io_exception);
 		}
 		mStarted = true;
 	}
 
 	@Override
 	public void onStop() {
 		super.onStop();
 		if (mUnsavedHistory) {
 			HistoryFileManager manager = new HistoryFileManager();
 			
 			try {
 				manager.setDirectory(Constants.HISTORY_DIRECTORY);
 				manager.setActiveDocument(mDirectoryPath + "/" + mDocumentName);
 				manager.createHistoryFile();
 				saveTextEditToFile(manager.getLastHistory().getPath());
 			} catch (IOException e) {
 				// Too late to rescue the data. Keep calm and carry on.
 			}
 			mUnsavedHistory = false;
 		}
 		mDocumentEditor.setText("");
 	}
 
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 		inflater.inflate(R.menu.editor_options_menu, menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(android.support.v4.view.MenuItem item) {
 		File document = new File(mDirectoryPath, mDocumentName);
 		HistoryFileManager manager = new HistoryFileManager();
 		
 		try {
 			manager.setDirectory(Constants.HISTORY_DIRECTORY);
 			manager.setActiveDocument(document);
 		} catch (FileNotFoundException e) {
 			this.displayErrorMessage(R.string.warn_unable_to_find_file);
 			return true;
 		} catch (DirectoryFileExpectedException e) {
 			this.displayErrorMessage(R.string.warn_unable_to_find_directory);
 			return true;
 		} catch (IOException e) {
 			this.displayErrorMessage(R.string.warn_io_exception);
 			return true;
 		}
 		
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			getActivity().finish();
 			return true;
 		case R.id.new_file_menu_item:
 			startActivityForResult(
 					new Intent(this.getActivity(), CreateDocumentActivity.class)
 							.setAction(
 									CreateDocumentActivity.NEW_DOCUMENT_ACTION)
 							.setDataAndType(
 									Uri.fromFile(document.getParentFile()), "text/plain"),
 					Codes.GET_STATUS);
 			return true;
 		case R.id.save_file_menu_item:
 			saveTextEditToFile(document.getPath());
 			
 			if(manager.historyExists() 
 					&& !manager.deleteAllHistory()) {
 				Toast.makeText(getActivity(),
 						R.string.warn_unable_to_delete_history,
 						Toast.LENGTH_SHORT).show();
 			} else {
 				mUnsavedHistory = false;
 			}
 			updateHistoryFileWarningVisiblity();
 			return true;
 		case R.id.share_file_menu_item:
 			Editable text = mDocumentEditor.getText();
 			if (text.length() >= Constants.MAX_EDITABLE_SHARE_SIZE) {
 				Toast.makeText(getActivity(),
 						R.string.share_file_too_large_warning,
 						Toast.LENGTH_LONG).show();
 			} else {
 				Intent shareIntent = new Intent();
 				shareIntent.setAction(Intent.ACTION_SEND);
 				shareIntent.setType("message/rfc822");
 				shareIntent.putExtra(Intent.EXTRA_TEXT, text);
 				try {
 					startActivity(Intent
 							.createChooser(
 									shareIntent,
 									getActivity().getText(
 											R.string.share_chooser_title)));
 				} catch (ActivityNotFoundException e) {
 					Toast.makeText(getActivity(),
 							R.string.message_email_client_not_found,
 							Toast.LENGTH_SHORT).show();
 				}
 			}
 			return true;
 		case R.id.delete_file_menu_item:
 			if (!document.delete()) {
 				Toast.makeText(getActivity(),
 						R.string.warn_unable_to_delete,
 						Toast.LENGTH_SHORT).show();
 			}
 			
 			if (!manager.deleteAllHistory() 
 					&& manager.historyExists()) {
 				Toast.makeText(getActivity(),
 						R.string.warn_unable_to_delete_history,
 						Toast.LENGTH_SHORT).show(); 
 			}
 
 			mUnsavedHistory = false;
 			getActivity().finish();
 			return true;
 		case R.id.revert_file_menu_item:
 			mReverting = true;
 			if (manager.historyExists()) {
 				if (!manager.deleteAllHistory()) {
 					Toast.makeText(getActivity(),
 							R.string.warn_unable_to_delete_history,
 							Toast.LENGTH_SHORT).show();
 					mUnsavedHistory = true;
 				} else {
 					mUnsavedHistory = false;
 				}
 			} else {
 				mUnsavedHistory = false;
 			}
 			readFileToTextEdit(document.getPath());
 			updateHistoryFileWarningVisiblity();
 			mReverting = false;
 			return true;
 		case R.id.preferences_menu_item:
 			startActivity(new Intent(getActivity(), PreferencesActivity.class));
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	@Override
 	public void handleUnmounted() {
 		super.handleReadonly();
 		getActivity().finish();
 	}
 
 	@Override
 	public void handleReadonly() {
 		super.handleReadonly();
 		getActivity().finish();
 	}
 
 	private void readFileToTextEdit(String path) {
 		try {
 			mDocumentEditor.setText(DocumentReader.read(new FileReader(path)));
 		} catch (FileNotFoundException e) {
 			Toast.makeText(getActivity(), R.string.warn_unable_to_find_file,
 					Toast.LENGTH_SHORT).show();
 			getActivity().setResult(Codes.ERR_FILE_NOT_FOUND);
 			getActivity().finish();
 		} catch (IOException e) {
 			//
 			e.printStackTrace();
 		}
 	}
 
 	private void saveTextEditToFile(String path) {
 		try {
 			DocumentWriter.write(mDocumentEditor.getText().toString(),
 					new BufferedWriter(new FileWriter(new File(path))));
 		} catch (IOException e) {
 			Toast.makeText(
 					getActivity(),
 					R.string.warn_io_exception + "\n"
 							+ R.string.warn_file_not_saved, Toast.LENGTH_SHORT)
 					.show();
 			getActivity().setResult(Codes.ERR_IO_EXCEPTION);
 		}
 	}
 
 	private void updateHistoryFileWarningVisiblity() {
 		HistoryFileManager manager = new HistoryFileManager();
 		
 		try {
 			manager.setDirectory(Constants.HISTORY_DIRECTORY);
 			manager.setActiveDocument(mDirectoryPath + "/" + mDocumentName);
 		} catch (FileNotFoundException e) {
 			this.displayErrorMessage(R.string.warn_unable_to_find_file);
 			return;
 		} catch (DirectoryFileExpectedException e) {
 			this.displayErrorMessage(R.string.warn_unable_to_find_directory);
 			return;
 		} catch (IOException e) {
 			this.displayErrorMessage(R.string.warn_io_exception);
 			return;
 		}
 		if (manager.historyExists() || mUnsavedHistory) {
 			// Animation slideIn = AnimationUtils.loadAnimation(getActivity(),
 			// R.anim.history_warning_slide_in);
 			mHistoryFileWarning.setVisibility(View.VISIBLE);
 			// mHistoryFileWarning.startAnimation(slideIn);
 		} else {
 			mHistoryFileWarning.setVisibility(View.GONE);
 		}
 	}
 
 	private boolean setManagersTo(String directory, String filename) {
 		HistoryFileManager manager = new HistoryFileManager();
 
 		try {
 			manager.setDirectory(Constants.HISTORY_DIRECTORY);
 			manager.setActiveDocument(new File(mDirectoryPath, mDocumentName));
 		} catch (FileNotFoundException e) {
 			this.displayErrorMessage(R.string.warn_unable_to_find_file);
 			return false;
 		} catch (DirectoryFileExpectedException e) {
 			this.displayErrorMessage(R.string.warn_unable_to_find_directory);
 			return false;
 		} catch (FileFileExpectedException e) {
 			this.displayErrorMessage(R.string.warn_unable_to_find_file);
 			return false;
 		} catch (IOException e) {
 			Toast.makeText(getActivity(), R.string.warn_io_exception,
 					Toast.LENGTH_SHORT).show();
 			getActivity().setResult(Codes.ERR_IO_EXCEPTION);
 			getActivity().finish();
 			return false;
 		}
 
 		return true;
 	}
 }
