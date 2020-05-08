 package com.example.androidfileclient;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnCancelListener;
 import android.os.AsyncTask;
 import android.os.Bundle;
import android.util.Log;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.Toast;
 
 import com.example.androidfileclient.models.FileItem;
 import com.example.androidfileclient.services.FileLister;
 
 public class MainActivity extends ListActivity {
 
 	private ArrayList<FileItem> filesList = new ArrayList<FileItem>();
 	private ArrayAdapter<FileItem> filesAdapter;
 	private ProgressDialog progressDialog;
 	private FileItem goBack = new FileItem();
 	private HashMap<String, String> previousKeys;
 	private String currentKey;
 
 	@SuppressWarnings("unchecked")
     @Override
     public void onCreate(Bundle savedInstanceState) {
 		goBack.name = "Back";
 		goBack.type = "action";
 		currentKey  = "initial";
 		previousKeys = new HashMap<String, String>();
 		
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         
         filesList = (ArrayList<FileItem>) getIntent().getSerializableExtra("files");
         if (filesList == null) filesList = new ArrayList<FileItem>();
         if (filesList.isEmpty()) performSearch("initial");
         
     	filesAdapter = new ArrayAdapter<FileItem>(this, android.R.layout.simple_list_item_1, filesList);
     	setListAdapter(filesAdapter);
     }
     
     @Override
     protected void onListItemClick(ListView l, View v, int position, long id) {
         super.onListItemClick(l, v, position, id);
         FileItem file = filesAdapter.getItem(position);
         if (file.type.equals("folder") || file.type.equals("action")) {
         	performSearch(file.key);
         } else {
         	longToast("Action is not supported yet");
         }
     }

     
 	
 
 	public void longToast(CharSequence message) {
 		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
 	}
 	
     public void performSearch(String key) {
     	if (!previousKeys.containsKey(key)) previousKeys.put(key, currentKey);
     	currentKey = key;
 
     	goBack.key = previousKeys.get(currentKey);
    	Log.e("PETER", goBack.key);
     	
     	progressDialog = ProgressDialog.show(MainActivity.this, "Please wait...", "Retrieving data...", true, true);
 
     	PerformFileListSearchTask task = new PerformFileListSearchTask();
 		task.execute(key);
 		progressDialog.setOnCancelListener(new CancelTaskOnCancelListener(task));
     }
     
     private class PerformFileListSearchTask extends AsyncTask<String, Void, ArrayList<FileItem>> {
     	private FileLister fileLister;
     	@Override
     	protected ArrayList<FileItem> doInBackground(String... params) {
     		String query = params[0];
             fileLister = new FileLister();
             return fileLister.retrieveFilesList(query);
     	}
     	
     	@Override
     	protected void onPostExecute(final ArrayList<FileItem> result) {
     		runOnUiThread(new Runnable() {
     			@Override
     			public void run() {
     				if (progressDialog != null) {
     					progressDialog.dismiss();
     					progressDialog = null;
     				}
     				filesList.clear();
     				for (int i = 0; i < result.size(); i++) {
     					filesList.add(result.get(i));
     				}
     				if (!currentKey.equals(goBack.key)) filesList.add(0, goBack);
     		        filesAdapter.notifyDataSetChanged();
     			}
     		});
     	}
     }
     
     private class CancelTaskOnCancelListener implements OnCancelListener {
     	private AsyncTask<?, ?, ?> task;
     	public CancelTaskOnCancelListener(AsyncTask<?, ?, ?> task) {
     		this.task = task;
     	}
     	@Override
 		public void onCancel(DialogInterface dialog) {
     		if (task!=null) {
         		task.cancel(true);
         	}
 		}
     }
     
 }
