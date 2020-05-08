 package com.jpqr.filepicker;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Stack;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.actionbarsherlock.app.SherlockFragment;
 import com.jpqr.adapters.FileListAdapter;
 import com.jpqr.listpad.R;
 import com.jpqr.listpad.activities.EditChecklist;
 import com.jpqr.listpad.models.Checklist;
 
 public class FilePicker extends SherlockFragment {
 
 	private File mFile;
 	private ArrayList<File> mFiles;
 	private TextView mLabel;
 	private ListView mListView;
 	private Context mContext;
 	private ArrayAdapter<File> mAdapter;
 	private Stack<File> mForwardStack;
 	private View mEmptyText;
 
 	public static void newInstance(Context context) {
 		Intent intent = new Intent(context, FilePicker.class);
 		context.startActivity(intent);
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		mContext = getActivity();
 		mFile = new File(Checklist.DEFAULT_DIRECTORY);
 		mFiles = new ArrayList<File>();
 		mAdapter = new FileListAdapter(mContext, mFiles);
 		mForwardStack = new Stack<File>();
 
 		View view = inflater.inflate(R.layout.file_picker, container, false);
 
 		mLabel = (TextView) view.findViewById(R.id.current_dir_label);
 		mEmptyText = view.findViewById(R.id.empty_text);
 		
 		mListView = (ListView) view.findViewById(R.id.list_files);
 		mListView.setOnItemClickListener(new OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 				File file = mFiles.get(position);
 				if (file.isDirectory()) {
 					mFile = file;
 					updateDir();
 					mForwardStack.clear();
 				} else {
 					EditChecklist.newInstance(mContext, file.getAbsolutePath());
 				}
 			}
 		});
 		mListView.setAdapter(mAdapter);
 
 		view.findViewById(R.id.folder_back).setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				back();
 			}
 		});
 
 		view.findViewById(R.id.folder_forward).setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				forward();
 			}
 		});
 
 		view.findViewById(R.id.file_add).setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				newFile();
 			}
 		});
 
 		updateDir();
 		
 
 		return view;
 	}
 
 	@Override
 	public void onResume() {
 		updateDir();
 		super.onResume();
 	}
 	
 	private void updateEmptyText() {
 		if (mFiles.size() == 0) {
 			mEmptyText.setVisibility(View.VISIBLE);
 		} else {
 			mEmptyText.setVisibility(View.GONE);
 		}
 	}
 
 	private void updateDir() {
 		mLabel.setText(mFile.getPath());
 		mFiles.clear();
		File[] files = mFile.listFiles();
		if (files != null) {
			mFiles.addAll(Arrays.asList(files));
			mAdapter.notifyDataSetChanged();
		}
 		updateEmptyText();
 	}
 
 	private void newFile() {
 		AlertDialog.Builder editPrompt = new AlertDialog.Builder(mContext);
 		final EditText editText = new EditText(mContext);
 		editText.setHint("File name");
 		editPrompt.setView(editText);
 
 		editPrompt.setPositiveButton("Create", new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int whichButton) {
 				String fileName = editText.getText().toString();
 				if (!fileName.contains(".")) {
 					fileName += ".txt";
 				}
 				File newFile = new File(mFile, fileName);
 				try {
 					newFile.createNewFile();
 					EditChecklist.newInstance(mContext, newFile.getAbsolutePath());
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		});
 
 		editPrompt.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int whichButton) {
 			}
 		});
 
 		editPrompt.show();
 	}
 
 	private void back() {
 		File parent = mFile.getParentFile();
 		if (parent != null) {
 			mForwardStack.push(mFile);
 			mFile = parent;
 			updateDir();
 		}
 	}
 
 	private void forward() {
 		if (!mForwardStack.isEmpty()) {
 			mFile = mForwardStack.pop();
 			updateDir();
 		}
 	}
 
 }
