 package com.example.ContentProviderTutorial;
 
 import android.app.Activity;
 import android.content.ContentValues;
import android.database.Cursor;
 import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
 import android.widget.Toast;
 
 public class MyActivity extends Activity {
   private MyDatabaseHelper mDatabaseHelper;
 
   public void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
    setContentView(R.layout.my_activity);
 
     mDatabaseHelper = new MyDatabaseHelper(this);
 
    Cursor c = mDatabaseHelper.query(MyDatabaseHelper.TABLE_USERS, MyDatabaseHelper.COL_NAME);
    String[] from = new String[]{MyDatabaseHelper.COL_NAME, MyDatabaseHelper.COL_EMAIL};
    int[] to = { android.R.id.text1, android.R.id.text2 };

    SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_2, c, from, to, 0);

    ListView listView = (ListView) findViewById(R.id.listView);
    listView.setAdapter(adapter);

     addUser(null, null, 0);
     addUser("Joe User", "joe@example.com", 0);
     addUser("Mary Jones", "mary@example.com", 0);
     addUser("Sue Bloggs", "sue@example.com", 0);
   }
 
   private void addUser(String name, String email, long dateOfBirthMillis) {
     ContentValues values = new ContentValues();
     values.put(MyDatabaseHelper.COL_NAME, name);
 
     if (email != null) {
       values.put(MyDatabaseHelper.COL_EMAIL, email);
     }
 
     if (dateOfBirthMillis != 0) {
       values.put(MyDatabaseHelper.COL_DOB, dateOfBirthMillis);
     }
 
     try {
       mDatabaseHelper.insert(MyDatabaseHelper.TABLE_USERS, values);
     } catch (MyDatabaseHelper.NotValidException e) {
       Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
     }
   }
 }
