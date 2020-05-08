 package com.playlist.playlist_generator;
 
 import android.app.ListActivity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 public class Settings_activity extends MyFileManager {
     static final ArrayList<HashMap<String,String>> list =
             new ArrayList<HashMap<String,String>>();
     final int REQUEST_CODE_MUSIC_PATH = 1;
     final int REQUEST_CODE_PL_PATH = 2;
     //String DefaultPath = "/";
     String DefaultMusicPath;
     String DefaultPLPath;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_settings);
         MyDB mydb = new MyDB(this);
         DefaultMusicPath = GetDefaultPath("Music",mydb);
         DefaultPLPath = GetDefaultPath("PL",mydb);
         list.clear();
         SimpleAdapter adapter = new SimpleAdapter(
             this,
             list,
             R.layout.settings_row,
             new String[] {"header","description"},
             new int[] {R.id.settings_item,R.id.settings_sub_item}
         );
         fillList();
         setListAdapter(adapter);
     }
     private void fillList() {
         HashMap<String,String> Back_row = new HashMap<String,String>();
         Back_row.put("header",getResources().getString(R.string.Settings_Back));
         //temp.put("description", DefaultPath);
         list.add(Back_row);
 
         HashMap<String,String> Music_path_row = new HashMap<String,String>();
         Music_path_row.put("header",getResources().getString(R.string.Settings_Music_Path));
         Music_path_row.put("description", DefaultMusicPath);
         list.add(Music_path_row);
 
         HashMap<String,String> PL_path_row = new HashMap<String,String>();
         PL_path_row.put("header",getResources().getString(R.string.Settings_PL_Path));
         PL_path_row.put("description", DefaultPLPath);
         list.add(PL_path_row);
     }
 
     @Override
     protected void onListItemClick(ListView l, View v, int position, long id) {
         Intent IntentVar;
         switch (position){
             case 0: //Back
                 IntentVar = new Intent(this, MainActivity.class);
                 startActivity(IntentVar);
                 break;
             case 1: //Music path
                 IntentVar = new Intent(this, PL_Path_Activity.class);
                 startActivityForResult(IntentVar, REQUEST_CODE_MUSIC_PATH);
                 break;
             case 2: //PL path
                 IntentVar = new Intent(this, PL_Path_Activity.class);
                 startActivityForResult(IntentVar, REQUEST_CODE_PL_PATH);
                 IntentVar.putExtra("PL",false);
                 break;
             default:
                 break;
         }
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         // запишем в лог значения requestCode и resultCode
         Log.d("myLogs", "requestCode = " + requestCode + ", resultCode = " + resultCode);
         //If result is positive
         if (resultCode == RESULT_OK) {
             switch (requestCode) {
                 case REQUEST_CODE_MUSIC_PATH:
                     break;
                 case REQUEST_CODE_PL_PATH:
                     break;
             }
         }
     }
 
     public String GetDefaultPath(String choice, MyDB mydb){
         String defaultPath = "";
         SQLiteDatabase db = mydb.getWritableDatabase();
         Cursor c = db.query("plgTable",null,null,null,null,null,null);
         if (c.moveToFirst()) {
             if (choice.equals("Music")){
                 int PathToMusic_ColIndex = c.getColumnIndex("music_path");
                 defaultPath = c.getString(PathToMusic_ColIndex);
             }
             else if (choice.equals("PL")) {
                 int PathToPL_ColIndex = c.getColumnIndex("pl_path");
                 defaultPath = c.getString(PathToPL_ColIndex);
             }
         }
         if (defaultPath == null){
             defaultPath = "/";
         }
         c.close();
         return defaultPath;
     }
 }
