 package com.example;
 
 import android.app.Activity;
 import android.app.ListActivity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.provider.ContactsContract;
 import android.util.Log;
 import android.util.SparseBooleanArray;
 import android.view.View;
 import android.widget.*;
 
 import java.util.ArrayList;
 
 public class MyActivity extends Activity implements View.OnClickListener
 {
     ArrayAdapter<String> adapter;
     ListView listView;
     Button button;
 
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         findViewsById();
 
         Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, null);
 
         ArrayList<String> contacts = new ArrayList<java.lang.String>();
         while (phones.moveToNext())
         {
             String name=phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
             String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
             Log.d("*************************MyMessage",phoneNumber);
             Log.d("*************************MyMessage",name);
 
             contacts.add(name + " " + phoneNumber);
         }
 
        adapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_multiple_choice,contacts);
        listView.findViewById(android.R.id.list);
         listView.setAdapter(adapter);
        //listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
 
         button.setOnClickListener(this);
         phones.close();
     }
 
     private void findViewsById() {
         listView = (ListView) findViewById(android.R.id.list);
         button = (Button) findViewById(R.id.testbutton);
     }
     public void onClick(View v) {
         SparseBooleanArray checked = listView.getCheckedItemPositions();
         ArrayList<String> selectedItems = new ArrayList<String>();
         for (int i = 0; i < checked.size(); i++) {
             int position = checked.keyAt(i);
             if (checked.valueAt(i)){
                 selectedItems.add(adapter.getItem(position));
                 Log.d("MyMessage"," result" + checked.valueAt(i));
             }
         }
         String[] outputStrArr = new String[selectedItems.size()];
 
         for (int i = 0; i < selectedItems.size(); i++) {
             outputStrArr[i] = selectedItems.get(i);
         }
 
         Intent intent = new Intent(getApplicationContext(),
                 ResultActivity.class);
 
         Bundle b = new Bundle();
         b.putStringArray("selectedItems", outputStrArr);
 
         intent.putExtras(b);
 
         startActivity(intent);
     }
 }
