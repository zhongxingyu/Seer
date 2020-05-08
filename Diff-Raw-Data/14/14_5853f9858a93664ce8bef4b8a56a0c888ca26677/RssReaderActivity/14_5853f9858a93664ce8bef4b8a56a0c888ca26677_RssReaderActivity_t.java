 package com.junj.imagerssreader;
 
 import android.os.Bundle;
import android.app.ListActivity;
 import android.view.Menu;
 
import java.util.ArrayList;

 public class RssReaderActivity extends ListActivity {
	private ArrayList<Item> mItems;
 	private RssListAdapter mAdapter;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		
		mItems = new ArrayList<Item>();
 		mAdapter = new RssListAdapter(this, mItems);
 		
 		setListAdapter(mAdapter);
 		
 		for(int i = 0; i < 10; i++) {
 			mAdapter.add(new Item());
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.rss_reader, menu);
 		return true;
 	}
 
 }
