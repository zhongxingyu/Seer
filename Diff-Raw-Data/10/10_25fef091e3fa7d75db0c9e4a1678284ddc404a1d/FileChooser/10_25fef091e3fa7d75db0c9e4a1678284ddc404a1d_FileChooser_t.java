 package cryptocast.client.filechooser;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 
 import cryptocast.client.HelpActivity;
 import cryptocast.client.MainActivity;
 import cryptocast.client.OptionsActivity;
 import cryptocast.client.R;
 import cryptocast.client.StreamViewerActivity;
 
 import android.os.Bundle;
 import android.os.Environment;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.AdapterView.OnItemClickListener;
 
 public class FileChooser extends Activity implements OnItemClickListener {
 
     private File currentDir;
     private ArrayList<ListElement> currentDirList;
 
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_file_chooser);
         currentDir = Environment.getExternalStorageDirectory();
         currentDirList = new ArrayList<ListElement>();
         
         updateItems(currentDir);
         
         ListView listView = (ListView) findViewById(R.id.listView1);
         listView.setOnItemClickListener(this);
     }
     
     private void updateItems(File curDir) {
         currentDirList = listDirectory(curDir);
         currentDirList.add(0, new NavigateUpListElement(curDir.getParentFile()));
         
     // set title
         TextView textView = (TextView) findViewById(R.id.textView1);
         textView.setText("Current Dir: " + currentDir.toString());
         
         ListView listView = (ListView) findViewById(R.id.listView1);
         ArrayAdapter<ListElement> adapter = new ArrayAdapter<ListElement>(this, android.R.layout.simple_list_item_1, 
                 android.R.id.text1, currentDirList);
         listView.setAdapter(adapter);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
     
     /** Handles a click on the main menu.
      * @param item The clicked item
      */
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         Intent intent;
         // Handle menu item click
         switch (item.getItemId()) {
             case R.id.itemMain:
                 intent = new Intent(this, MainActivity.class);
                 startActivity(intent);
                 return true;
             case R.id.itemOptions:
                 intent = new Intent(this, OptionsActivity.class);
                 startActivity(intent);
                 return true;
             case R.id.itemHelp:
                 intent = new Intent(this, HelpActivity.class);
                 startActivity(intent);
                 return true;
             case R.id.itemPlayer:
                 intent = new Intent(this, StreamViewerActivity.class);
                 startActivity(intent);
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
     @Override
     public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
         currentDir = enterDirectory(currentDirList.get(pos).getPath());
         updateItems(currentDir);
     }
     
     /**
      * Tries to enter the given directory.
      * @param target The directory to enter.
      * @return The new directory
      */
     public File enterDirectory(File target) {
         File actualTarget = target;
         if (target == null) {
             return currentDir;
         }
         if (target.isDirectory() && target.getParent() != null) {
             // check if newDir is an actual directory
             if (target.listFiles() == null) {
                 actualTarget = target.getParentFile();
             }
         } else if (!target.isDirectory()) {
             actualTarget = target.getParentFile();
         }
         return actualTarget;
     }
     
     /**
      * Returns a list of the elements in the gven directory
      * @param dir  Direcotory to list. Needs to be a valid directory.
      * @return The listed files
      */
     public ArrayList<ListElement> listDirectory(File dir) {
         ArrayList<ListElement> list = new ArrayList<ListElement>();
         File[] curDirFiles = dir.listFiles();
 
         for (File file : curDirFiles) {
             if (file.isDirectory()) {
                 list.add(new DirectoryListElement(file));
             } else {
                 list.add(new FileListElement(file));
             }
         }
         
         Collections.sort(list, new FileComparator());
         return list;
     }
 }
 
 class FileComparator implements Comparator<ListElement> {
 
     @Override
     public int compare(ListElement e1, ListElement e2) {
         File file1 = e1.getPath();
         File file2 = e2.getPath();
         
         if (file1.isDirectory() && file2.isFile()) {
             return -1;
         } else if (file1.isFile() && file2.isDirectory()) {
             return 1;
         } else {
             return file1.getName().compareTo(file2.getName());
         }
     }
 }
