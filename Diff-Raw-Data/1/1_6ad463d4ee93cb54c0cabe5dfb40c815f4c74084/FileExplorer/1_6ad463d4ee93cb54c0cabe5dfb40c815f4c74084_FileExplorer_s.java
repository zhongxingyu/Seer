 package com.qhm123.fileselector;
 
 import java.io.File;
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.util.SparseBooleanArray;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.qhm123.fileselector.adapters.DirFileAdapter;
 import com.qhm123.fileselector.adapters.FileAdapter;
 import com.qhm123.fileselector.adapters.FileAdapter.ItemCheckListener;
 import com.qhm123.fileselector.adapters.MultiFileAdapter;
 import com.qhm123.fileselector.adapters.SingleFileAdapter;
 
 public class FileExplorer extends Activity implements OnItemClickListener,
 		OnClickListener, ItemCheckListener {
 
 	private static final String TAG = FileExplorer.class.getSimpleName();
 
 	private static final int MODE_SINGLE = 0;
 	private static final int MODE_MULTIPLE = 1;
 	private static final int MODE_DIR = 2;
 
 	private ListView mList;
 	private FileAdapter mFileAdapter;
 	private Button mSelectButton;
 	private TextView mCurrentDirPath;
 
 	private int mMode = MODE_MULTIPLE;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.activity_explorer);
 
 		mSelectButton = (Button) findViewById(R.id.select);
 		mSelectButton.setOnClickListener(this);
 		mCurrentDirPath = (TextView) findViewById(R.id.dir_path);
 
 		String action = getIntent().getAction();
 		if (Actions.SINGLE.equals(action)) {
 			mMode = MODE_SINGLE;
 			mFileAdapter = new SingleFileAdapter(this);
 		} else if (Actions.MULTIPLE.equals(action)) {
 			mMode = MODE_MULTIPLE;
 			mFileAdapter = new MultiFileAdapter(this);
 		} else if (Actions.DIR.equals(action)) {
 			mMode = MODE_DIR;
 			mFileAdapter = new DirFileAdapter(this);
 		}
 		Log.d(TAG, "action: " + action + ", mode: " + mMode);
 		mFileAdapter.setItemCheckListener(this);
 
 		mList = (ListView) findViewById(R.id.list);
 		mList.setAdapter(mFileAdapter);
 		mList.setFastScrollEnabled(true);
 		// mList.setItemsCanFocus(false);
 		// mList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
 		mList.setOnItemClickListener(this);
 		// mList.setOnItemSelectedListener(this);
 
 		String initPath = "/sdcard";
 		mFileAdapter.setPath(initPath, true);
 		mCurrentDirPath.setText(initPath);
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> parent, View view, int position,
 			long id) {
 		Log.d(TAG, "onItemClick");
 
 		FileInfo fileInfo = mFileAdapter.getItem(position);
 		File file = new File(fileInfo.path);
 		if (file.isDirectory()) {
 			mList.clearChoices();
 			mFileAdapter.setPath(fileInfo.path, true);
 			mList.scrollTo(0, 0);
 			mCurrentDirPath.setText(fileInfo.path);
 		} else {
 			if (mMode == MODE_SINGLE) {
 				Intent data = new Intent(getIntent());
 				Uri uri = Uri.parse(mFileAdapter.getItem(position).path);
 				data.putExtra("file", uri);
 				setResult(RESULT_OK, data);
 				finish();
 			}
 		}
 	}
 
 	@Override
 	public void onBackPressed() {
 		mList.clearChoices();
 		FileInfo fileInfo = mFileAdapter.back();
 		if (fileInfo != null) {
 			mCurrentDirPath.setText(fileInfo.path);
 		} else {
 			setResult(RESULT_CANCELED);
 			super.onBackPressed();
 		}
 	}
 
 	@Override
 	public void onClick(View v) {
 		switch (v.getId()) {
 		case R.id.select:
 			Intent data = new Intent(getIntent());
 			ArrayList<Uri> files = new ArrayList<Uri>();
 			SparseBooleanArray positions = mFileAdapter
 					.getCheckedItemPositions();
 			for (int i = 0; i < positions.size(); i++) {
 				int position = positions.keyAt(i);
 				Log.d(TAG, "position: " + position);
 				files.add(Uri.parse(mFileAdapter.getItem(position).path));
 			}
 			data.putParcelableArrayListExtra("files", files);
 			setResult(RESULT_OK, data);
 			finish();
 			break;
 		default:
 			break;
 		}
 	}
 
 	@Override
 	public void onItemCheckListener(int position, boolean isChecked) {
 
 	}
 }
