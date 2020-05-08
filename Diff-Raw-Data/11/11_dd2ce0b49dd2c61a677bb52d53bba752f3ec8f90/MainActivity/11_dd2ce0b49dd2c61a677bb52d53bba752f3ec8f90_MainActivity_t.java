 package com.wu.androidfileclient;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import android.app.ListActivity;
 import android.content.ActivityNotFoundException;
 import android.content.Intent;
import android.content.pm.ActivityInfo;
 import android.os.Bundle;
 import android.view.ContextMenu;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.ListView;
 
 import com.ipaulpro.afilechooser.utils.FileUtils;
 import com.wu.androidfileclient.async.PerformDeleteFileAsyncTask;
 import com.wu.androidfileclient.async.PerformUpdateListAsyncTask;
 import com.wu.androidfileclient.models.ActionItem;
 import com.wu.androidfileclient.models.BaseListItem;
 import com.wu.androidfileclient.models.Credential;
 import com.wu.androidfileclient.models.FileItem;
 import com.wu.androidfileclient.models.FolderItem;
 import com.wu.androidfileclient.services.FileDownloader;
 import com.wu.androidfileclient.services.FileUploader;
 import com.wu.androidfileclient.ui.FileItemsListAdapter;
 import com.wu.androidfileclient.utils.Utilities;
 
 public class MainActivity extends ListActivity {
 	
 	private ArrayList<BaseListItem> objectsList             = new ArrayList<BaseListItem>();
 	private ActionItem goBack                               = new ActionItem();
 	private Credential credential                           = new Credential();
 	private HashMap<FolderItem, FolderItem> previousFolders = new HashMap<FolderItem, FolderItem>();
 	private FolderItem currentFolder                        = new FolderItem();
 
 	private FileItemsListAdapter filesAdapter;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 
 		credential        = Utilities.getCredential(this);
 		goBack.name       = "Back";
 		currentFolder.key = "initial";
 
 		previousFolders.put(currentFolder, currentFolder);
 
         if (objectsList == null) objectsList = new ArrayList<BaseListItem>();
         if (objectsList.isEmpty()) loadList(currentFolder);
 
     	filesAdapter = new FileItemsListAdapter(this, R.layout.file_list_row, objectsList);
     	setListAdapter(filesAdapter);
 
     	registerForContextMenu(getListView());
     }
 
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
 		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
 	    MenuInflater inflater = getMenuInflater();
 	    if (!(objectsList.get(info.position) instanceof ActionItem))
 	    	inflater.inflate(R.menu.activity_main_context, menu);
 	}
 	
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
 		BaseListItem listItem = objectsList.get(info.position);
 		if (listItem instanceof FileItem) {
 			FileItem fileItem = (FileItem) listItem;
 	
 			switch (item.getItemId()) {
 			case R.id.open:
 				FileDownloader fileDownloader = new FileDownloader(credential);
 				fileDownloader.downloadWithProgressUpdate(this, fileItem);
 				break;
 			case R.id.delete:
 	    		deleteFile(fileItem);
 		    	break;
 	        default:
 	            return super.onOptionsItemSelected(item);
 			}
 		}
 		return true;
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 	    MenuInflater inflater = getMenuInflater();
 	    inflater.inflate(R.menu.activity_main, menu);
 	    return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
         case R.id.refresh:
         	refreshList();
         	break;
         case R.id.upload:
         	Intent target = FileUtils.createGetContentIntent();
             Intent intent = Intent.createChooser(target, "Select a file");
             try {
                 startActivityForResult(intent, 1234);
             } catch (ActivityNotFoundException e) {
                 // The reason for the existence of aFileChooser
             }
         	break;
         default:
             return super.onOptionsItemSelected(item);
 		}
 		return true;
 	}
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 	    switch (requestCode) {
 	    case 1234:  
 	        if (resultCode == RESULT_OK) {
 	        	File f = Utilities.getFileFromUri(data.getData(), this.getContentResolver());
 	        	FileItem file = new FileItem();
 	        	file.localPath = f.getParentFile().getPath() + "/";
 	        	file.name = f.getName();
 
 	        	FileUploader fileUploader = new FileUploader(credential);
 	        	fileUploader.uploadWithProgressUpdate(this, currentFolder.key, file);
 	        }
 	    }
 	}
 
     @Override
     protected void onListItemClick(ListView l, View v, int position, long id) {
         super.onListItemClick(l, v, position, id);
         BaseListItem listItem = filesAdapter.getItem(position);
         if (listItem instanceof FolderItem) {
         	loadList((FolderItem) listItem);
         } else if (listItem instanceof ActionItem) {
         	loadList(((ActionItem) listItem).folderItem);
         } else {
 			FileDownloader fileDownloader = new FileDownloader(credential);
 			fileDownloader.downloadWithProgressUpdate(this, (FileItem) listItem);
         }
     }
 
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {
         if ((keyCode == KeyEvent.KEYCODE_BACK)) {
         	if (currentFolder != goBack.folderItem) {
         		loadList(goBack.folderItem);
         		return true;
         	}
         }
         return super.onKeyDown(keyCode, event);
     }
 
 //	TODO: currently only allow to delete file
 	public void deleteFile(FileItem file) {
 		PerformDeleteFileAsyncTask task = new PerformDeleteFileAsyncTask(this, credential);
 		task.execute(file);
 	}
 
 	public void refreshList() {
 		loadList(currentFolder);
 	}
 
     public void loadList(FolderItem folderItem) {
     	if (!previousFolders.containsKey(folderItem)) previousFolders.put(folderItem, currentFolder);
     	currentFolder = folderItem;
 
     	goBack.folderItem = previousFolders.get(currentFolder);
 
     	PerformUpdateListAsyncTask task = new PerformUpdateListAsyncTask(this, credential);
 		task.execute(currentFolder.key);
     }
     
     public void updateList(ArrayList<BaseListItem> result) {
     	if (result != null) {
 			objectsList.clear();
 			for (int i = 0; i < result.size(); i++) {
 				objectsList.add(result.get(i));
 			}
 			if (currentFolder != goBack.folderItem) objectsList.add(0, goBack);
 	        filesAdapter.notifyDataSetChanged();
 		}
     }
 }
