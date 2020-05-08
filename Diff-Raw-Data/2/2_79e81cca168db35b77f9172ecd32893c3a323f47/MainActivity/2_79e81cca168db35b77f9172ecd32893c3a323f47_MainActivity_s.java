 package com.wu.androidfileclient;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import android.app.ListActivity;
 import android.os.Bundle;
 import android.view.ContextMenu;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.ListView;
 
 import com.wu.androidfileclient.async.PerformDeleteFileAsyncTask;
 import com.wu.androidfileclient.async.PerformDownloadFileAsyncTask;
 import com.wu.androidfileclient.async.PerformUpdateListAsyncTask;
 import com.wu.androidfileclient.models.ActionItem;
 import com.wu.androidfileclient.models.Credential;
 import com.wu.androidfileclient.models.FileItem;
 import com.wu.androidfileclient.models.FolderItem;
 import com.wu.androidfileclient.models.BaseListItem;
 import com.wu.androidfileclient.ui.FileItemsListAdapter;
 import com.wu.androidfileclient.utils.Utilities;
 
 public class MainActivity extends ListActivity {
 	
	private ArrayList<BaseListItem> objectsList                 = new ArrayList<BaseListItem>();
 	private ActionItem goBack                               = new ActionItem();
 	private Credential credential                           = new Credential();
 	private HashMap<FolderItem, FolderItem> previousFolders = new HashMap<FolderItem, FolderItem>();
 	private FolderItem currentFolder                        = new FolderItem();
 
 	private FileItemsListAdapter filesAdapter;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 
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
 				downloadFile(fileItem);
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
         default:
             return super.onOptionsItemSelected(item);
 		}
 		return true;
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
         	downloadFile((FileItem) listItem);
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
 
 	public void downloadFile(FileItem file) {
 		PerformDownloadFileAsyncTask task = new PerformDownloadFileAsyncTask(this, credential);
 		task.execute(file);
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
