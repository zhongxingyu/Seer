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
 import android.os.Build;
 import android.os.Bundle;
 import android.support.v4.view.Menu;
 import android.text.Editable;
 import android.text.InputFilter;
 import android.text.TextWatcher;
 import android.view.Gravity;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.inputmethod.EditorInfo;
 import android.widget.EditText;
 import android.widget.TextSwitcher;
 import android.widget.TextView;
 import android.widget.TextView.OnEditorActionListener;
 import android.widget.Toast;
 import android.widget.ViewSwitcher.ViewFactory;
 
 import com.keifermiller.inkbar.Codes;
 import com.keifermiller.inkbar.Constants;
 import com.keifermiller.inkbar.FilenameInputFilter;
 import com.keifermiller.inkbar.R;
 import com.keifermiller.inkbar.activities.PreferencesActivity;
 import com.keifermiller.inkbar.activities.TextEditorActivity;
 import com.keifermiller.inkbar.documentutils.DocumentFileManager;
 import com.keifermiller.inkbar.exceptions.DirectoryFileExpectedException;
 
 public class CreateDocumentFragment extends IBFragment implements ViewFactory {
 
 	/* (non-Javadoc)
 	 * @see com.keifermiller.inkbar.fragments.IBFragment#onActivityResult(int, int, android.content.Intent)
 	 */
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		getActivity().finish();
 	}
 
 
 	private DocumentFileManager mDocumentCreator;
 	private TextSwitcher mTextSwitcher;
 	
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 
 		mDocumentCreator = new DocumentFileManager();
 		mTextSwitcher = (TextSwitcher) getActivity().findViewById(R.id.filenameErrorSwitcher);
 		mTextSwitcher.setFactory(this);
 
 		try {
 			File dir = Constants.DOCUMENTS_DIRECTORY;
 			dir.mkdir();
 			mDocumentCreator.setDirectory(dir);
 		} catch (FileNotFoundException e) {
 			Toast.makeText(getActivity(), R.string.warn_unable_to_find_directory, Toast.LENGTH_SHORT).show();
 		} catch (DirectoryFileExpectedException e) {
 			Toast.makeText(getActivity(), R.string.warn_unable_to_find_directory, Toast.LENGTH_SHORT).show();
 		} 
 
 		final EditText documentNameEditor = (EditText) this.getActivity()
 				.findViewById(R.id.editDocumentName);
 		documentNameEditor
 				.setFilters(new InputFilter[] { (InputFilter) new FilenameInputFilter() });
 
 		documentNameEditor.addTextChangedListener(new TextWatcher() {
 
 			@Override
 			public void afterTextChanged(Editable arg0) {
 				mTextSwitcher.setText("");		
 				
 			}
 
 			@Override
 			public void beforeTextChanged(CharSequence arg0, int arg1,
 					int arg2, int arg3) {				
 			}
 
 			@Override
 			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
 					int arg3) {				
 			}
 			
 		});
 		
 		documentNameEditor
 				.setOnEditorActionListener(new OnEditorActionListener() {
 					public boolean onEditorAction(TextView v, int actionId,
 							KeyEvent event) { 
						if (actionId == EditorInfo.IME_ACTION_SEND) {
 							createAndPassFile(documentNameEditor.getText()
 									.toString());
 							return true;
 						} else if (event != null) {
 							createAndPassFile(documentNameEditor.getText()
 									.toString());
 							return true;
 						}
 						return false;
 					}
 				});
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		return inflater.inflate(R.layout.create_document_fragment, container,
 				false);
 	}
 
 	public void createAndPassFile(String name) {
 		if (name.equals("")) {
 			mTextSwitcher.setText("No name given");
 			return;
 		}
 		
 		if (name.matches("\\.+") || name.matches(" +")) {
 			mTextSwitcher.setText("Invalid name");
 			return;
 		}
 		
 		// eat ending spaces
 		String scrubbedname = name.replaceAll("\\s+$","").replaceAll("^\\s+", "");
 		
 		File[] files;
 		files = mDocumentCreator.getDirectory().listFiles();
 		if (files != null) {
 			for (File f : files) {
 				if (scrubbedname.equals(f.getName())) {
 					mTextSwitcher.setText("Name already in use");
 					return;
 				}
 			}
 		}
 
 		try {
 			mDocumentCreator.createFile(scrubbedname);
 			startActivityForResult(new Intent(getActivity(), TextEditorActivity.class)
 			.setAction(TextEditorActivity.OPEN_DOCUMENT_ACTION).putExtra(
 					"filename", scrubbedname), Codes.GET_STATUS);
 		} catch (IOException e) {
 			mTextSwitcher.setText("Unable to create file");
 		}
 
 
 	}
 
 	/* (non-Javadoc)
 	 * @see android.support.v4.app.Fragment#onCreateOptionsMenu(android.support.v4.view.Menu, android.view.MenuInflater)
 	 */
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 		inflater.inflate(R.menu.create_document_options_menu, menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			getActivity().finish();
 			return true;
 		case R.id.preferences_menu_item:
 			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
 				startActivity(new Intent(getActivity(), PreferencesActivity.class));
 			} else {
 				startActivity(new Intent(getActivity(), PreferencesActivity.class));
 			}
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 	
 	public void handleUnmounted() {
 		super.handleUnmounted();
 		getActivity().finish();
 	}
 	
 	public void handleReadonly() {
 		super.handleReadonly();
 		getActivity().finish();
 	}
 	
 
 	@Override
 	public View makeView() {
 		TextView t = new TextView(this.getActivity());
         t.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
         return t;
 	}
 }
