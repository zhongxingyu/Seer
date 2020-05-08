 package com.caspian.android.removal;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.regex.Pattern;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.util.SparseBooleanArray;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public abstract class AppManagementScreen extends Activity
 {
 
     protected ListView fileList;
     
     protected AppRemovalManager mgr;
     protected LinearLayout panel;
     
     protected Button btnDeleteFiles;
     protected Button btnCopyFiles;
     
     protected String appManagementDir;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
 
         mgr = new AppRemovalManager();
         
         // layout the page
         layoutPage();
         
         // create the list of files
         createFileList();
     }
 
     private void layoutPage()
     {
         // Create a panel and set it as the content view
         panel = new LinearLayout(this);
         panel.setOrientation(LinearLayout.VERTICAL);
         setContentView(panel);
 
         // create a couple buttons
         LinearLayout buttonPanel = new LinearLayout(this);
         buttonPanel.setOrientation(LinearLayout.HORIZONTAL);
 
         btnDeleteFiles = new Button(this);
         btnDeleteFiles.setText("Delete Selected Files");
         buttonPanel.addView(btnDeleteFiles);
 
         btnCopyFiles = new Button(this);
         btnCopyFiles.setText("Copy Selected Files");
         buttonPanel.addView(btnCopyFiles);
 
         // add the button panel
         panel.addView(buttonPanel);
         
         TextView lbl = new TextView(this);
         lbl.setText("Current directory: " + appManagementDir);
         panel.addView(lbl);
         
         fileList = new ListView(this);
         fileList.setFastScrollEnabled(true);
         fileList.setItemsCanFocus(false);
         fileList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
 
         fileList.setAdapter(new ArrayAdapter<String>(this,
             android.R.layout.simple_list_item_multiple_choice));
         panel.addView(fileList);
         
 
         // create callback for the buttons
         btnCopyFiles.setOnClickListener(new View.OnClickListener() {
 
             @Override
             public void onClick(View v)
             {
                if (copyCheckedFiles())
                {
                    Toast toast = Toast.makeText(
                            getApplicationContext(), 
                            "Successfully restored files", 
                            Toast.LENGTH_SHORT);
                    toast.show();
                }
             }
         });
 
         btnDeleteFiles.setOnClickListener(new View.OnClickListener() {
 
             @Override
             public void onClick(View v)
             {
                 deleteCheckedFiles();
             }
         });
     }
     
     /**
      * Filter to only show certain patterns in the management screens.
      * 
      * @author stelle
      */
     class FileFilter implements FilenameFilter {
         private Pattern pattern;
 
         public FileFilter(String search) 
         {
             // escape the . character
             search = search.replaceAll("\\.", "\\\\.");
             
             // change * to .*
             search = search.replaceAll("\\*", ".*");
             
             pattern = Pattern.compile(search);
         }
 
         public boolean accept(File dir, String name) {
           // Strip path information, search for regex:
           return pattern.matcher(new File(name).getName()).matches();
         }
       }
     
     /**
      * (re)create the file list that displays all the files
      */
     protected void createFileList()
     {
         // get a sorted array of files
         File f = new File(appManagementDir);
         String[] files = f.list(new FileFilter(AppSettings.getFilter()));
         
         // get the adapter for this file list
         ArrayAdapter<String> a = (ArrayAdapter<String>)fileList.getAdapter();
         a.clear();
         
         // clear the check boxes
         fileList.clearChoices();
         
         if (files != null && files.length > 0)
         {
             Arrays.sort(files, 0, files.length, new Comparator<Object>() {
 
                 @Override
                 public int compare(Object object1, Object object2)
                 {
                     int comp = 0;
                     if (object1 instanceof String 
                         && object2 instanceof String)
                     {
                         String str1 = (String)object1;
                         String str2 = (String)object2;
                         
                         comp = str1.compareToIgnoreCase(str2);
                     }
                     
                     return comp;
                 }});
     
             
             // add all the files
             for (String fileName : files)
             {
                 a.add(fileName);
             }
             
             fileList.invalidate();
         }
     }
     
 
     /**
      * Get a list of strings that contain the file names of the selected items
      * 
      * @return
      */
     protected ArrayList<String> getCheckedFiles()
     {
         SparseBooleanArray items = fileList.getCheckedItemPositions();
         ArrayList<String> files = new ArrayList<String>();
 
         for (int i = 0; i < items.size(); i++)
         {
             if (items.valueAt(i))
             {
                 files
                     .add(fileList.getItemAtPosition(items.keyAt(i)).toString());
             }
         }
 
         return files;
     }
     
     /**
      * Callback to handle what we do when the user presses the "copy" button
      * 
      * @return whether the operation was successful
      */
     protected abstract boolean copyCheckedFiles();
 
     /**
      * Callback to handle what we do when the user presses the "delete" button
      * 
      * @return whether the operation was successful
      */
     protected abstract boolean deleteCheckedFiles();
 }
