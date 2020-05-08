 package net.m_kawato.tabletpos;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import android.os.Bundle;
 import android.os.Environment;
 import android.app.Activity;
 import android.content.Intent;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 
 public class LoadingFilePicker extends Activity implements AdapterView.OnItemClickListener {
     private static final String TAG = "LoadingFilePicker";
     private List<FileListItem> fileListItems;
     
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_loading_file_picker);
 
         String sdcardPath = Environment.getExternalStorageDirectory().getPath();
         String dirname = String.format("%s/%s", sdcardPath, Globals.SDCARD_DIRNAME);
         File dir = new File(dirname);
         File[] filelist = dir.listFiles();
 
         // Build ListView of files/directories
         this.fileListItems = new ArrayList<FileListItem>();
         for(File file: filelist) {
            this.fileListItems.add(new FileListItem(file));
         }
         Collections.sort(fileListItems);
         ArrayAdapter<FileListItem> adapter = new ArrayAdapter<FileListItem>(this, R.layout.filelist_item, this.fileListItems);
         ListView listView = (ListView) findViewById(R.id.list);
         listView.setAdapter(adapter);
         
         listView.setOnItemClickListener(this);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.loading_file_picker, menu);
         return true;
     }
 
     @Override
     public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
         FileListItem item = this.fileListItems.get(position);
         Log.d(TAG, String.format("onItemClick: file=%s", item.toString()));
         if (item.isFile()) {
             Intent data = new Intent();
             data.putExtra("filename", item.toString());
             setResult(RESULT_OK, data);
             finish();
         }
     }
 }
 
 class FileListItem implements Comparable<FileListItem> {
     private File file;
 
     public FileListItem(File file) {
         this.file = file;
     }
 
     public boolean isFile() {
         return this.file.isFile();
     }
     public boolean isDirectory() {
         return this.file.isDirectory();
     }
 
     @Override
     public String toString() {
         if (file.isDirectory()) {
             return file.getName() + "/";
         } else {
             return file.getName();
         }
     }
 
     @Override
     public int compareTo(FileListItem other) {
         if (this.isDirectory() && !other.isDirectory()) {
             return -1;
         } else if (!this.isDirectory() && other.isDirectory()) {
             return 1;
         } else {
             return this.toString().compareTo(other.toString());
         }
     }
 }
