 package com.example.memopad;
 
 import android.app.ListActivity;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ListView;
 //added imports
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.widget.SimpleCursorAdapter;
 import android.content.Intent;
 
 public class MemoList extends ListActivity
 {
 	static final String [] cols = {"title","memo", android.provider.BaseColumns._ID,};
 	MemoDBHelper memos;
 	//cols is gotten when searching database
 	
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id)
 	{
 		// TODO Auto-generated method stub
 		super.onListItemClick(l, v, position, id);
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) 
 	{
 		setContentView(R.layout.memolist);
		showMemos(getMemos());//getMemos() is Undefined.
 		super.onCreate(savedInstanceState);
 	}
 
 	public MemoList()
 	{
 		// TODO Auto-generated constructor stub
 	}
 
 }//class
