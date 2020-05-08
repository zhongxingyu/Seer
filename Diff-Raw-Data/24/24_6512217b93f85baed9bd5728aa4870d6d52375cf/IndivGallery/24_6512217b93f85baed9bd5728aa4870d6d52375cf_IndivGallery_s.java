 package com.theluvexchange.android;
 
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.res.TypedArray;
 import android.os.Bundle;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.BaseAdapter;
 import android.widget.Gallery;
 import android.widget.ImageView;
 import android.widget.Toast;
 
 
 /**
  * @author Android Developers Gallery Tutorial, edit by Matt Glasert
  * 
  * This displays an individual album's contents (doesn't run)
  */
 public class IndivGallery extends Activity
 {
 
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) 
 	{
 		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
 
 		Gallery gallery = (Gallery) findViewById(R.id.gallery);
 		gallery.setAdapter(new ImageAdapter(this));
 
 		gallery.setOnItemClickListener(new OnItemClickListener() {
             public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                 Toast.makeText(IndivGallery.this, "" + position, Toast.LENGTH_SHORT).show();
             }
         });
     }
 }
