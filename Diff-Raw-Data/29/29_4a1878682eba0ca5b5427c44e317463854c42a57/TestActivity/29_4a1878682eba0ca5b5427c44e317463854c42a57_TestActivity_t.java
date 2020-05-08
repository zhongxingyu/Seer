 package com.example.ipcplayer.provider;
 
import com.example.ipcplayer.utils.LogUtil;

 import android.R.string;
 import android.app.Activity;
 import android.content.ContentValues;
 import android.net.Uri;
 import android.os.Bundle;
 
 public class TestActivity extends Activity{
 	private Uri mUir;
 	private string[] mPorjection = null;
 	
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		insertDB();
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 	}
 	
 	private void insertDB(){
		LogUtil.d("insertDB");
 		Uri uri = MusicDB.MusicInfoColumns.getContentUri();
		LogUtil.d("uri= " + uri);
 		ContentValues values = new ContentValues();
 		for (int i = 0; i < 5; i++) {
 			values.put(MusicDB.MusicInfoColumns.MUSICNAME, "name_" + i);
 		}
 		this.getContentResolver().insert(uri, values);
 	}
 	
 }
