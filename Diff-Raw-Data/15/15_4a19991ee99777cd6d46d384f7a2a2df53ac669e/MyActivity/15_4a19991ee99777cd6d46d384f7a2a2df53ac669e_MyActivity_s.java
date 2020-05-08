 package com.example.MyNotebook;
 
 
 import android.app.Activity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.SimpleCursorAdapter;
 
 public class MyActivity extends Activity {
     CreateAdapter adapter;
     CreateOpenHelper helper;
     SQLiteDatabase db;
     ListView noteList;
     Button createBtn;
     Cursor cursor;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         noteList = (ListView)findViewById(R.id.lv_note);
         createBtn = (Button)findViewById(R.id.btn_register);
         adapter = new CreateAdapter(this);
 
         String[] from = {helper.TITLE, helper.NOTE};
         int[] to = {R.id.tv_title, R.id.tv_note};
         cursor = adapter.queryNote();
         SimpleCursorAdapter cursorAdapter = new SimpleCursorAdapter(this,R.layout.row, cursor, from, to);
         noteList.setAdapter(cursorAdapter);
        noteList.setOnItemClickListener(new AdapterView.OnItemClickListener(){

             @Override
            public void onItemClick(AdapterView arg0, View arg1, int arg2,long arg3) {
                 Bundle passdata = new Bundle();
                 Cursor listCursor = (Cursor) arg0.getItemAtPosition(arg2);
                 int nameId = listCursor.getInt(listCursor
                         .getColumnIndex(helper.KEY_ID));
                 // Toast.makeText(getApplicationContext(),
                 // Integer.toString(nameId), 500).show();
                 passdata.putInt("keyid", nameId);
                 Intent passIntent = new Intent(MyActivity.this,
                         EditActivity.class);
                 passIntent.putExtras(passdata);
                 startActivity(passIntent);
             }
         });
 
         createBtn.setOnClickListener(new View.OnClickListener(){
 
             @Override
             public void onClick(View v) {
                 Intent registerIntent = new Intent(MyActivity.this,
                         CreateActivity.class);
                 startActivity(registerIntent);
             }
         });
     }
 
 
 }
