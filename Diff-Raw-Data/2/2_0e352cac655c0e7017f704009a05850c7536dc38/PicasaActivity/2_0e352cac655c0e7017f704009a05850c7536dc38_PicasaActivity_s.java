 package com.photobox.app;
 
 import java.io.File;
 import java.util.*;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.util.*;
 import android.os.Bundle;
 import android.os.Environment;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.*;
 
 import com.photobox.R;
 import com.photobox.files.*;
 
 public class PicasaActivity extends Activity {
 
     private ListView albumList;
     private EditText usernameEditText;
     private PicasaActivity thisActivity;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                 WindowManager.LayoutParams.FLAG_FULLSCREEN);
 
         thisActivity = this;
 
         setContentView(R.layout.picasa);
 
         usernameEditText = (EditText)findViewById(R.id.usernameEditText);
         albumList = (ListView)findViewById(R.id.albumList);
 
         Button loadAlbumsButton = (Button)findViewById(R.id.loadAlbumsButton);
         loadAlbumsButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 List<String> albums = new PicasaApi("" + usernameEditText.getText()).getAlbums();
                 String[] items;
                 if (albums != null) {
                    items = (String[])albums.toArray();
                 } else {
                     items = new String[] { "no albums found" };
                 }
                 ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                         thisActivity,
                         android.R.layout.simple_list_item_1,
                         android.R.id.text1,
                         items);
                 albumList.setAdapter(adapter);
             }
         });
     }
 
 }
