 package com.iusenko.filechooser;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Stack;
 
 import android.app.ListActivity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.View;
 import android.widget.ListView;
 import android.widget.TextView;
 
 /**
  * @author iusenko
  */
 public class FileChooserActivity extends ListActivity {
 	private static final String TAG = FileChooserActivity.class.getSimpleName();
 
	public static final int PICK_UP_FILE_REQUEST = 1;
	public static final int NOTHING_SELECTD_RESULT = 2;
	public static final int FILE_SELECTED_RESULT = 3;
 
 	// public static final String ACCEPT_FILE_EXTENSIONS_KEY =
 	// "extensions-filter-key";
 	public static final String SELECTED_FILE_KEY = "selected-file-key";
 	public static final String WORKING_DIRECTORY_KEY = "working-directory-key";
 	private static final String DEFAULT_WORKING_DIRECTORY = "/";
 	private Comparator<File> filenameComparator = new FileNameComparator();
 
 	private File workingDirectory = new File(DEFAULT_WORKING_DIRECTORY);
 	private ArrayList<File> filelist = new ArrayList<File>();
 	private FileAdapter filelistAdapter;
 	private TextView workingDirectoryTextView;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.filechooser_activity);
 		workingDirectoryTextView = (TextView) findViewById(R.id.working_dir_text_view);
 
 		Bundle b = getIntent().getExtras();
 		if (b != null) {
 			workingDirectory = getWorkingDirectory(b);
 		}
 
 		filelistAdapter = new FileAdapter(this);
 		setListAdapter(filelistAdapter);
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		refreshFilelist(workingDirectory);
 	}
 
 	private File getWorkingDirectory(Bundle bundle) {
 		if (bundle.containsKey(WORKING_DIRECTORY_KEY)) {
 			String value = bundle.getString(WORKING_DIRECTORY_KEY);
 			return new File(value);
 		}
 		return workingDirectory;
 	}
 
 	private void refreshFilelist(File directory) {
 		Log.d(TAG, "Current path: " + directory.getAbsolutePath());
 
 		filelist.clear();
 		File[] files = directory.listFiles();
 		for (int i = 0; files != null && i < files.length; i++) {
 			filelist.add(files[i]);
 		}
 		Collections.sort(filelist, filenameComparator);
 
 		filelistAdapter.setListItems(filelist);
 		filelistAdapter.notifyDataSetChanged();
 		workingDirectoryTextView.setText(directory.getAbsolutePath());
 		getListView().setSelection(0);
 	}
 
 	@Override
 	public void onBackPressed() {
 		workingDirectory = workingDirectory.getParentFile();
 		if (workingDirectory == null) {
 			closeFileChooser(null);
 		} else {
 			refreshFilelist(workingDirectory);
 		}
 	}
 
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		super.onListItemClick(l, v, position, id);
 		File file = filelist.get(position);
 		Log.d(TAG, "Selected file: " + file.getAbsolutePath());
 
 		if (file.isDirectory()) {
 			workingDirectory = file;
 			refreshFilelist(file);
 		} else {
 			closeFileChooser(file);
 		}
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		outState.putString("working-dir", workingDirectory.getAbsolutePath());
 	}
 
 	@Override
 	protected void onRestoreInstanceState(Bundle state) {
 		String path = state.getString("working-dir");
 		workingDirectory = new File(path);
 		super.onRestoreInstanceState(state);
 	}
 
 	private void closeFileChooser(File selected) {
 		if (selected == null) {
 			setResult(NOTHING_SELECTD_RESULT);
 		} else {
 			Intent i = new Intent();
 			i.putExtra(SELECTED_FILE_KEY, selected.getAbsolutePath());
 			setResult(FILE_SELECTED_RESULT, i);
 		}
 		finish();
 	}
 
 	private class FileNameComparator implements Comparator<File> {
 		public int compare(File f1, File f2) {
 			if (f1.isFile() && f2.isDirectory()) {
 				return 1;
 			} else if (f1.isDirectory() && f2.isFile()) {
 				return -1;
 			}
 			return f1.getName().toLowerCase().compareTo(f2.getName().toLowerCase());
 		}
 	}
 }
