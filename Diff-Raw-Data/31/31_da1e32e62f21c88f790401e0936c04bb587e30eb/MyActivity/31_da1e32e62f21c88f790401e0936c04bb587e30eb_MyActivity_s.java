 package com.example.ContentProviderTutorial;
 
 import android.app.Activity;
 import android.content.ContentValues;
 import android.os.Bundle;
 import android.widget.Toast;
 
 public class MyActivity extends Activity {
   private MyDatabaseHelper mDatabaseHelper;
 
   public void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
 
     mDatabaseHelper = new MyDatabaseHelper(this);
 
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
