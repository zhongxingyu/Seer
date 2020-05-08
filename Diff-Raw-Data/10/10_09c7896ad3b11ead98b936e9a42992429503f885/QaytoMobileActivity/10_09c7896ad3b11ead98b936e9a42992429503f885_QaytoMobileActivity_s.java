 package com.qayto.mobile;
 
import org.json.JSONArray;

 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.GridView;
 
 
 public class QaytoMobileActivity extends Activity {
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         GridView gridview = (GridView) findViewById(R.id.gridview);
         
         final ImageAdapter my_adapter = new ImageAdapter(this);
         gridview.setAdapter(my_adapter);
         
 
         gridview.setOnItemClickListener(new OnItemClickListener() {
             public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                 Intent intent = new Intent();
                 intent.putExtra("subcatIndex", (position + 1));
                 intent.setClass(QaytoMobileActivity.this, SubcatListActivity.class);
                 startActivity(intent);
             }
         });
     }
 }
 
