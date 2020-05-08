 /**
  * Copyright (C) 2010 Rednus Limited http://www.rednus.co.uk
  * 
  * Project : GNCAndroid Package : rednus.GNCAndroid File : Preferences.java
  * Description :
  */
 package rednus.gncandroid;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collections;
 import android.app.ListActivity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Environment;
 import android.util.Log;
 import android.view.View;
 import android.view.Window;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 
 /**
  * @author shyam, davide
  * 
  */
 public class FileChooser extends ListActivity {
 	private final String TAG = "File Chooser";
 	protected ArrayList<String> mFileList;
 	protected File mRoot;
 	public static final String FILEPATH_KEY = "formpath";
 	private GNCAndroid app;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		app = (GNCAndroid) getApplication();
 		// app.res.getString(R.string.pref_data_file_key);
 		if (app.localLOGV)
 			Log.i(TAG, "Filechooser started");
 		// make this a dialogue
 		requestWindowFeature(Window.FEATURE_LEFT_ICON);
 		setContentView(R.layout.filechooser);
 		getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON,
 				android.R.drawable.stat_notify_sdcard);
 		// set data
 		// if // It would be nice to start from the directory of the previously
 		// selected data file (if not null)
 		this.initialize(app.res.getString(R.string.def_folder));
 		// else {
 		// this.initialize(new
 		// File(app.res.getString(R.string.pref_data_file_key)).getParentFile().getAbsolutePath());
 		// }
 	}
 
 	private void initialize(String path) {
 		mFileList = new ArrayList<String>();
 		// mFileList.add("..");
 		if (getDirectory(path)) {
 			getFiles(mRoot);
 			Collections.sort(mFileList, String.CASE_INSENSITIVE_ORDER);
 			displayFiles();
 		}
 	}
 
 	private void refreshRoot(File f) {
 		mRoot = f;
 		mFileList.clear();
 		if (!f.getName().equalsIgnoreCase("sdcard"))
 			mFileList.add("..");
 		getFiles(mRoot);
 		Collections.sort(mFileList, String.CASE_INSENSITIVE_ORDER);
 		((ArrayAdapter) this.getListAdapter()).notifyDataSetChanged();
 	}
 
 	private boolean getDirectory(String path) {
 		TextView tv = (TextView) findViewById(R.id.filelister_message);
 		// check to see if there's an sd card.
 		String cardstatus = Environment.getExternalStorageState();
 		if (cardstatus.equals(Environment.MEDIA_REMOVED)
 				|| cardstatus.equals(Environment.MEDIA_UNMOUNTABLE)
 				|| cardstatus.equals(Environment.MEDIA_UNMOUNTED)
 				|| cardstatus.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
 			tv.setText(getString(R.string.sdcard_error));
 			return false;
 		}
 		// now get file object for the path
 		mRoot = new File(path);
 		if (!mRoot.exists())
 			tv.setText(getString(R.string.directory_error, path));
 		else
 			return true;
 		return false;
 	}
 
 	private void getFiles(File f) {
 		if (f.isDirectory()) {
 			File[] childs = f.listFiles();
 			if (null == childs)
 				return;
 			for (File child : childs) {
 				getFile(child);
 			}
 		} else
 			getFile(f);
 	}
 
 	private void getFile(File f) {
 		String filename = f.getName();
 		mFileList.add(filename);
 	}
 
 	/**
 	 * Opens the directory, puts valid files in array adapter for display
 	 */
 	private void displayFiles() {
 		ArrayAdapter<String> fileAdapter;
 		fileAdapter = new ArrayAdapter<String>(this,
 				android.R.layout.simple_list_item_1, mFileList);
 		setListAdapter(fileAdapter);
 	}
 
 	/**
 	 * Stores the path of clicked file in the intent and exits.
 	 */
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		File f;
 		if (mFileList.get(position) == "..")
 			f = new File(mRoot.getParent());
 		else
 			f = new File(mRoot + "/" + mFileList.get(position));
 		if (f.isDirectory()) {
 			this.refreshRoot(f);
 			/*
 			 * try { this.refreshRoot(f.getCanonicalFile()); } catch
 			 * (IOException e) { // TODO Auto-generated catch block
 			 * e.printStackTrace(); }
 			 */
 			return;
 		}
 		if (app.localLOGV)
 			Log.i(TAG, "File selected, returning result");
 		// send result
 		Intent i = new Intent();
 		i.putExtra(app.res.getString(R.string.pref_data_file_key), f
 				.getAbsolutePath());
 		setResult(RESULT_OK, i);
 		// close activity
 		finish();
 	}
 }
