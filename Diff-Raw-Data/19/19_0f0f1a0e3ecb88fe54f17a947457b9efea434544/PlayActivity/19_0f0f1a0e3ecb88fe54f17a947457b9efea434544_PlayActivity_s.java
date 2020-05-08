 package com.android.shakemusic;
 
 import android.app.ListActivity;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ListAdapter;
 import android.widget.ListView;
import android.support.v4.widget.SimpleCursorAdapter;
 
 public class PlayActivity extends ListActivity{
 	
 	private Cursor cursor;
 	private ListAdapter adapter;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.playlayout);
 		DBManager dbManager = new DBManager(this);
 		SQLiteDatabase db = dbManager.getWritableDatabase();
 		cursor = db.rawQuery("SELECT * FROM " + DBManager.TABLE_NAME, null);
 		adapter = new SimpleCursorAdapter(this, R.layout.list, cursor,
 				new String[] { "File name" },
 				new int[] { R.id.fileName }, 0);
 		setListAdapter(adapter);
 	}
 
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		// TODO Auto-generated method stub
		
 	}
 }
