 package com.jpqr.listpad.activities;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.inputmethod.EditorInfo;
 import android.webkit.MimeTypeMap;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.TextView.OnEditorActionListener;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.jpqr.listpad.R;
 import com.jpqr.listpad.adapters.ChecklistAdapter;
 import com.jpqr.listpad.database.FilesDataSource;
 import com.jpqr.listpad.dragdrop.TouchInterceptor;
 import com.jpqr.listpad.helper.ReadWriteHelper;
 import com.jpqr.listpad.models.Checklist;
 
 public class EditActivity extends SherlockActivity {
 	public static final String EXTRA_PATH = "PATH";
 	public static final String EXTRA_SAVED_TEXT = "SAVED_TEXT";
 	private ChecklistAdapter mAdapter;
 	private Checklist mChecklist;
 	private ListView mListView;
 	private Context mContext;
 	private EditText mChecklistNameField;
 	private boolean mListModeActive = true;
 	private boolean mIsFavourite;
 	private EditText mChecklistTextField;
 	private String mPath;
 	private FilesDataSource mDataSource;
 	private EditText mAddItemField;
 	private String mSavedText = null;
 
 	public static void newInstance(Context context, String path) {
 		Intent intent = new Intent(context, EditActivity.class);
 		intent.putExtra(EXTRA_PATH, path);
 		context.startActivity(intent);
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		mContext = this;
 		mDataSource = new FilesDataSource(mContext);
 		
 		if (savedInstanceState != null) {
 			mSavedText = savedInstanceState.getString(EXTRA_SAVED_TEXT);
 		}
 
 		Uri pathUri = getIntent().getData();
 		String pathString = getIntent().getStringExtra(EXTRA_PATH);
 		File file = null;
 		try {
 			if (pathUri != null) {
 				file = new File(new URI(pathUri.toString()));
 			} else if (pathString != null) {
 				file = new File(pathString);
 			} else {
 				throw new URISyntaxException("null", "EditChecklist Activity must be opened with a path in the intent or the instance");
 			}
 		} catch (URISyntaxException e) {
 			Toast.makeText(mContext, "Problem with file path.", Toast.LENGTH_LONG).show();
 			e.printStackTrace();
 		}
 		checkFile(file);
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
		if (mChecklist != null) {
			outState.putString(EXTRA_SAVED_TEXT, mChecklist.toString());
		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getSupportMenuInflater();
 		inflater.inflate(R.menu.edit_file_menu, menu);
 		MenuItem favouriteItem = menu.findItem(R.id.favourite);
		favouriteItem.setIcon(mIsFavourite ? R.drawable.ics_rating_important : R.drawable.ics_rating_not_important);
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 			case R.id.save:
 				save();
 				return true;
 			case R.id.delete:
 				delete();
 				return true;
 			case R.id.switch_mode:
 				switchMode();
 				return true;
 			case R.id.favourite:
 				favourite(item);
 				return true;
 			case R.id.close:
 				close();
 				return true;
 			default:
 				return super.onOptionsItemSelected(item);
 		}
 	}
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		switch (keyCode) {
 			case KeyEvent.KEYCODE_BACK:
 				close();
 			break;
 		}
 		return super.onKeyDown(keyCode, event);
 	}
 
 	private void checkFile(File file) {
 		if (file.isDirectory() || !file.exists()) {
 			Toast.makeText(mContext, "This is not a file", Toast.LENGTH_LONG).show();
 			finish();
 		}
 
 		String fileExtension = MimeTypeMap.getFileExtensionFromUrl(file.toURI().toString());
 		String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);
 		if (mimeType == null || !mimeType.matches("text/.+")) {
 			confirmFileOpen("This file is not a standard plain-text file. Do you still want to open it?", file);
 		} else if (file.length() > 1024) {
 			confirmFileOpen("This file may be too large for ListPad. Do you still want to open it?", file);
 		} else {
 			setUpChecklist(file);
 			setUpViews();
 		}
 	}
 
 	private void confirmFileOpen(String msg, final File file) {
 		AlertDialog.Builder oddFileDialogBuilder = new AlertDialog.Builder(mContext);
 		DialogInterface.OnClickListener oddFileDialog = new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				switch (which) {
 					case DialogInterface.BUTTON_POSITIVE:
 						setUpChecklist(file);
 						setUpViews();
 					break;
 
 					case DialogInterface.BUTTON_NEGATIVE:
 						finish();
 					break;
 				}
 			}
 		};
 		oddFileDialogBuilder.setPositiveButton("Yes", oddFileDialog);
 		oddFileDialogBuilder.setNegativeButton("No", oddFileDialog);
 		oddFileDialogBuilder.setMessage(msg);
 		oddFileDialogBuilder.show();
 
 	}
 
 	private void setUpChecklist(File file) {
 		try {
 			mChecklist = new Checklist(file);
 		} catch (FileNotFoundException e) {
 			Toast.makeText(mContext, "File not found.", Toast.LENGTH_LONG).show();
 			e.printStackTrace();
 		} catch (IOException e) {
 			Toast.makeText(mContext, "Input/output problem.", Toast.LENGTH_LONG).show();
 			e.printStackTrace();
 		}
 		if (mSavedText != null) {
 			mChecklist.fromString(mSavedText);
 		}
 		mPath = file.getAbsolutePath();
 		mDataSource.open();
 		mDataSource.addFile(mPath, FilesDataSource.Type.RECENT);
 		mIsFavourite = mDataSource.isFavourite(mPath);
 		mDataSource.close();
 	}
 
 	private void setUpViews() {
 		setContentView(R.layout.edit_checklist_activity);
 		mListView = (ListView) findViewById(R.id.list_field);
 
 		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
 		((TouchInterceptor) mListView).setDropListener(mDropListener);
 
 		View addItemView = inflater.inflate(R.layout.checklist_new_item, null);
 		mListView.addFooterView(addItemView);
 
 		mChecklistNameField = (EditText) findViewById(R.id.checklist_title);
 		mChecklistNameField.setText(mChecklist.getTitle());
 
 		mChecklistTextField = (EditText) findViewById(R.id.text_field);
 
 		mAdapter = new ChecklistAdapter(mContext, mChecklist, R.layout.checklist_edit_item);
 		mListView.setAdapter(mAdapter);
 
 		mListView.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
 				AlertDialog.Builder editPrompt = new AlertDialog.Builder(mContext);
 				final TextView itemName = (TextView) view.findViewById(R.id.item_name);
 				final EditText editText = new EditText(mContext);
 				editText.setText(itemName.getText());
 				editPrompt.setView(editText);
 
 				editPrompt.setPositiveButton("Save", new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int whichButton) {
 						String value = editText.getText().toString();
 						itemName.setText(value);
 						mChecklist.set(position, value);
 					}
 				});
 
 				editPrompt.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int whichButton) {
 					}
 				});
 
 				editPrompt.show();
 			}
 		});
 
 		mAddItemField = (EditText) findViewById(R.id.add_item_field);
 		mAddItemField.setOnEditorActionListener(new OnEditorActionListener() {
 			@Override
 			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
 				if (actionId == EditorInfo.IME_ACTION_NEXT) {
 					addToChecklist();
 					return true;
 				}
 				return false;
 			}
 		});
 		ImageView addButton = (ImageView) findViewById(R.id.add_button);
 		addButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				addToChecklist();
 			}
 		});
 	}
 
 	private void addToChecklist() {
 		String itemToAdd = mAddItemField.getText().toString();
 		if (!itemToAdd.equals("")) {
 			mChecklist.add(itemToAdd);
 			mAddItemField.setText("");
 			mAdapter.notifyDataSetChanged();
 		}
 		mListView.setSelection(mAdapter.getCount() - 2);
 	}
 
 	private void switchMode() {
 		if (mListModeActive) {
 			mChecklistTextField.setText(mChecklist.toString());
 			mChecklistTextField.setVisibility(View.VISIBLE);
 			mListView.setVisibility(View.GONE);
 			mListModeActive = false;
 		} else {
 			mChecklist.fromString(mChecklistTextField.getText().toString());
 			mChecklistTextField.setVisibility(View.GONE);
 			mListView.setVisibility(View.VISIBLE);
 			mAdapter.notifyDataSetChanged();
 			mListModeActive = true;
 		}
 	}
 
 	private boolean save() {
 		String fileName = mChecklistNameField.getText().toString().trim();
 
 		if (!ReadWriteHelper.isFileNameValid(fileName)) {
 			Toast.makeText(mContext, "Invalid file name.", Toast.LENGTH_LONG).show();
 			return false;
 		}
 
 		mChecklist.setTitle(fileName);
 		try {
 			mChecklist.saveFile();
 			Toast.makeText(mContext, "List saved.", Toast.LENGTH_SHORT).show();
 			return true;
 		} catch (IOException e) {
 			Toast.makeText(mContext, "Problem saving file.", Toast.LENGTH_SHORT).show();
 			e.printStackTrace();
 			return false;
 		}
 	}
 
 	private void favourite(MenuItem item) {
 		mDataSource.open();
 		if (mIsFavourite) {
 			mIsFavourite = false;
			item.setIcon(R.drawable.ics_rating_not_important);
 			mDataSource.deleteFile(mPath, FilesDataSource.Type.FAVOURITE);
 			Toast.makeText(mContext, "Unfavorited.", Toast.LENGTH_SHORT).show();
 		} else {
 			mIsFavourite = true;
			item.setIcon(R.drawable.ics_rating_important);
 			mDataSource.addFile(mPath, FilesDataSource.Type.FAVOURITE);
 			Toast.makeText(mContext, "Favorited.", Toast.LENGTH_SHORT).show();
 		}
 		mDataSource.close();
 	}
 
 	private void delete() {
 		AlertDialog.Builder deleteDialogBuilder;
 		DialogInterface.OnClickListener deleteDialog;
 		deleteDialog = new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				switch (which) {
 					case DialogInterface.BUTTON_POSITIVE:
 						if (mChecklist.delete()) {
 							finish();
 							Toast.makeText(mContext, "File deleted.", Toast.LENGTH_LONG).show();
 						} else {
 							Toast.makeText(mContext, "Problem deleting file.", Toast.LENGTH_LONG).show();
 						}
 					break;
 
 					case DialogInterface.BUTTON_NEGATIVE:
 					break;
 				}
 			}
 		};
 		deleteDialogBuilder = new AlertDialog.Builder(mContext);
 		deleteDialogBuilder.setMessage("Are you sure you want to delete this file?");
 		deleteDialogBuilder.setPositiveButton("Yes", deleteDialog);
 		deleteDialogBuilder.setNegativeButton("No", deleteDialog);
 		deleteDialogBuilder.show();
 	}
 
 	private void close() {
 		if (mChecklist.isModified()) {
 			AlertDialog.Builder closeDialogBuilder;
 			DialogInterface.OnClickListener closeDialog;
 
 			closeDialog = new DialogInterface.OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					switch (which) {
 						case DialogInterface.BUTTON_POSITIVE:
 							if (save()) {
 								finish();
 							}
 						break;
 						case DialogInterface.BUTTON_NEGATIVE:
 							finish();
 						break;
 						case DialogInterface.BUTTON_NEUTRAL:
 						break;
 					}
 				}
 			};
 			closeDialogBuilder = new AlertDialog.Builder(mContext);
 			closeDialogBuilder.setMessage("Do you want to save this file before closing?");
 			closeDialogBuilder.setPositiveButton("Yes", closeDialog);
 			closeDialogBuilder.setNegativeButton("No", closeDialog);
 			closeDialogBuilder.setNeutralButton("Cancel", closeDialog);
 			closeDialogBuilder.show();
 		} else {
 			finish();
 		}
 	}
 
 	private TouchInterceptor.DropListener mDropListener = new TouchInterceptor.DropListener() {
 		@Override
 		public void drop(int from, int to) {
 			ArrayList<String> list = mChecklist;
 			int size = list.size();
 			if (from < size) {
 				if (to >= size) {
 					to = size - 1;
 				}
 				String item = list.remove(from);
 				list.add(to, item);
 				mListView.invalidateViews();
 			}
 		}
 	};
 
 }
