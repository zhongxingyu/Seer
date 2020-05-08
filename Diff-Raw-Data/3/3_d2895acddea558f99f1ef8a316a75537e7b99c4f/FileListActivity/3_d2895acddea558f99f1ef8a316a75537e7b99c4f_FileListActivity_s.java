 package org.hlidskialf.android.filer;
 
 import android.app.ListActivity;
 import android.widget.ListView;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.Toast;
 import android.view.View;
 import android.view.Menu;
 import android.view.LayoutInflater;
 import android.view.ViewGroup;
 import android.view.ContextMenu;
 import android.view.MenuItem;
 import android.os.Bundle;
 import android.os.Environment;
 import android.net.Uri;
 
 import java.util.GregorianCalendar;
 import java.util.Calendar;
 import java.util.Date;
 import java.text.SimpleDateFormat;
 import java.text.DecimalFormat;
 
 import android.widget.AdapterView.AdapterContextMenuInfo;
 
 import java.io.File;
 import java.lang.String;
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Collections;
 
 import android.util.Log;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 
 public class FileListActivity extends ListActivity 
 {
     private final DecimalFormat pNumFmt  = new DecimalFormat();
     private final SimpleDateFormat pDateFmt_time = new SimpleDateFormat("MMM dd HH:mm");
     private final SimpleDateFormat pDateFmt_year  = new SimpleDateFormat("MMM dd yyyy");
   
     private BroadcastReceiver pReceiver = new BroadcastReceiver() {
       @Override
       public void onReceive(Context context, Intent intent) { fillData(pCurDir); }
     };
     private File pCurDir;
     private List<String> pCurFiles;
     private File pYanked = null;
     private final File pRootFile = new File(Environment.getExternalStorageDirectory().toString());
     private boolean pIgnoreNextClick = false;
     private boolean pCreatingShortcut = false;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
       super.onCreate(savedInstanceState);
       setContentView(R.layout.filer);
 
       Intent i = getIntent();
 
       pCreatingShortcut = Intent.ACTION_CREATE_SHORTCUT.equals(i.getAction());
 
       if (pCreatingShortcut) {
         Toast t = Toast.makeText(this, "Longclick to create a shortcut", Toast.LENGTH_LONG);
         t.show();
       }
 
       pCurDir = pRootFile;
 
       Uri uri = i.getData();
       if (uri != null) {
         File f = new File(uri.getPath());
         if (f.isDirectory())
           pCurDir = f;
         else { // file viewer
           Log.v("Filer", "view file");
           finish();
         }
       }
 
       registerForContextMenu(getListView());  
     }
 
     @Override
     public void onResume()
     {
       super.onResume();
     
       IntentFilter filt = new IntentFilter();
       filt.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
       filt.addAction(Intent.ACTION_MEDIA_MOUNTED);
       filt.addDataScheme("file");
       registerReceiver(pReceiver, filt);
 
       fillData(pCurDir);
     }
     @Override
     public void onPause()
     {
       super.onPause();
       unregisterReceiver(pReceiver);
     }
 
 
     private void fillData(File new_dir)
     {
       pCurDir = new_dir;
       try {
         setTitle(pCurDir.getCanonicalPath());
       } catch (Exception e) {}
 
       View empty = findViewById(R.id.empty);
 
       pCurFiles = new ArrayList<String>();
 
       String state = Environment.getExternalStorageState();
       if (Environment.MEDIA_MOUNTED.equals(state))  {
         String[] ls = pCurDir.list();
         int i;
         for (i=0; i < ls.length; i++) {
           if (!ls[i].startsWith(".")) pCurFiles.add(ls[i]);
         }
         empty.setVisibility(View.INVISIBLE);
       }
       else {
         empty.setVisibility(View.VISIBLE);
       }
 
       Collections.sort(pCurFiles, new Comparator() {
         public int compare(Object a, Object b) {
           File fa = new File(pCurDir, (String)a); 
           File fb = new File(pCurDir, (String)b); 
           if (fa.isDirectory()) {
             if (fb.isDirectory()) 
               return fa.getName().compareTo( fb.getName() );
             return -1;
           }
           if (fb.isDirectory()) 
             return 1;
           return 0;
         }
       });
 
       try {
         if (!pCurDir.getCanonicalPath().equals(pRootFile.getCanonicalPath()))
           pCurFiles.add(0,"..");
       } catch (Exception e) {}
 
       final ArrayAdapter<String> file_adapter = new ArrayAdapter(this, R.layout.file_list_item, pCurFiles) {
         public View getView(int position, View v, ViewGroup parent)
         {
           String name = (String)pCurFiles.get(position);
           LayoutInflater li = getLayoutInflater();
           View ret = li.inflate(R.layout.file_list_item, parent, false);
 
           TextView txt = (TextView)ret.findViewById(R.id.row_text);
           txt.setText(name);
 
           ImageView img = (ImageView)ret.findViewById(R.id.row_icon);
           File f = new File(pCurDir, name);
 
           if (name.equals("..")) {
             img.setImageResource(android.R.drawable.ic_menu_revert);
           }
           else
           if (f.isDirectory()) {
             img.setImageResource(android.R.drawable.ic_menu_more);
 
           }
           else {
             txt = (TextView)ret.findViewById(R.id.row_type);
             txt.setText("text/*");
 
             txt = (TextView)ret.findViewById(R.id.row_size);
             txt.setText( format_size(f.length()) );
 
             txt = (TextView)ret.findViewById(R.id.row_mtime);
             txt.setText( format_date(f.lastModified()) );
           }
 
           return ret;
 
         }
       };
 
   
       setListAdapter(file_adapter);
     }
 
 
     @Override
     protected void onListItemClick(ListView l, View v, int position, long id) 
     {
   
 
       if (pIgnoreNextClick) {
         pIgnoreNextClick = false;
         return;
       }
       super.onListItemClick(l,v,position,id);
 
       File f = new File(pCurDir, (String)pCurFiles.get(position));
 
       if (f.isDirectory()) {
         fillData(f);
       }
       else {
         if (pCreatingShortcut) {
           create_shortcut(f);
         }
         else {
 
           try {
             Intent i = new Intent(Intent.ACTION_VIEW);
             i.setDataAndType(Uri.fromFile(f), "text/*");
             startActivity(i);
           } catch (android.content.ActivityNotFoundException ex) { }
 
         }
       }
     }
 
     private void create_shortcut(File f)
     {
       int draw;
       String type;
       String action = Intent.ACTION_VIEW;
 
       if (f.isDirectory()) {
         type = "text/directory";
         action = Intent.ACTION_RUN;
         draw = R.drawable.mime_folder;
       }
       else {
         type = "text/*";
         draw = R.drawable.mime_generic;
       }
 
       Intent.ShortcutIconResource icon = Intent.ShortcutIconResource.fromContext(this, draw);
 
       Intent i = new Intent(action);
       i.setDataAndType(Uri.fromFile(f), type);
 
       Intent short_i = new Intent();
       short_i.putExtra(Intent.EXTRA_SHORTCUT_INTENT, i);
       short_i.putExtra(Intent.EXTRA_SHORTCUT_NAME, f.getName());
       short_i.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
       setResult(RESULT_OK, short_i);
       finish();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu)
     {
       super.onCreateOptionsMenu(menu);
       getMenuInflater().inflate(R.menu.options, menu);
 
       MenuItem mimove = menu.findItem(R.id.options_menu_move);
       MenuItem micopy = menu.findItem(R.id.options_menu_copy);
       MenuItem midelete = menu.findItem(R.id.options_menu_delete);
 
       if (pYanked == null) {
         mimove.setVisible(false);
         micopy.setVisible(false);
         midelete.setVisible(false);
       }
       else {
         mimove.setTitle("Move "+pYanked.getName()+" here");
         micopy.setTitle("Copy "+pYanked.getName()+" here");
         midelete.setTitle("Delete "+pYanked.getName()+" forever");
       }
 
       return true;
     }
     @Override
     public boolean onOptionsItemSelected(MenuItem item)
     {
       switch (item.getItemId())
       {
         case R.id.options_menu_move:
           return true;
         case R.id.options_menu_copy:
           return true;
         case R.id.options_menu_delete:
           return true;
 
         case R.id.options_menu_newdir:
           return true;
         case R.id.options_menu_prefs:
           return true;
         case R.id.options_menu_help:
           return true;
       }
       return false;
     }
 
     @Override
     public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
     {
       super.onCreateContextMenu(menu, v, menuInfo);
       AdapterContextMenuInfo info = (AdapterContextMenuInfo)menuInfo;
 
 
       if (pCreatingShortcut) {
 
         File f = new File(pCurDir, pCurFiles.get(info.position));
         create_shortcut(f);
         return;
 
       } else {
 
         if (pCurFiles.get(info.position).equals("..")) {
           pIgnoreNextClick = true;
           fillData(pRootFile);
           return;
         }
         getMenuInflater().inflate(R.menu.files_context, menu);
 
       }
     }
 
 
     @Override
     public boolean onContextItemSelected(MenuItem item)
     {
      String name = pCurFiles.get(item.getOrder());
 
       File f = new File(pCurDir, name);
 
       switch (item.getItemId()) {
         case R.id.file_context_menu_open:
           return true;
         case R.id.file_context_menu_yank:
           pYanked = f;
           return true;
         case R.id.file_context_menu_rename:
           return true;
       }
       return super.onContextItemSelected(item);
 
     }
 
 
     private String format_size(long size)
     {
       String ret;
       if (size > 1024*1024*1024) ret = pNumFmt.format(size / 1024*1024*1024) + "G";
       else if (size > 1024*1024) ret = pNumFmt.format(size / 1024*1024) + "M";
       else if (size > 1024) ret = pNumFmt.format(size / 1024) + "k";
       else ret = pNumFmt.format(size) + "b";
       return ret;
     }
     private String format_date(long when)
     {
       Date last = new Date(when);
       GregorianCalendar now = new GregorianCalendar();
       GregorianCalendar then = new GregorianCalendar();
       then.setTime(last);
 
       if (now.get(Calendar.YEAR) == then.get(Calendar.YEAR))
         return pDateFmt_time.format(last);
       else
         return pDateFmt_year.format(last);
     }
 }
