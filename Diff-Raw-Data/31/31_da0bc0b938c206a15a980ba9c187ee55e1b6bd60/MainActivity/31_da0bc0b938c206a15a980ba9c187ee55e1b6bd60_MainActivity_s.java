 package com.iptton.SpamBlocker;
 
 import com.iptton.SpamBlocker.R;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.ContentValues;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.SimpleCursorAdapter;
 
 public class MainActivity extends Activity {
 
 	final static String TAG = "MainActivity";
 	SQLiteDatabase db;
 	DBHelper helper;
 	ListView lv;
 	EditText editText;
 	Cursor spamCursor;
 
     Boolean isShowingSpam = false;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         helper = new DBHelper(this);
         db = helper.getWritableDatabase();
 
 		
         lv = (ListView)findViewById(R.id.blackList);
         editText = (EditText)findViewById(R.id.editText2);
         
         
         String t[] = new String[1];
         t[0] = "h";
 		int indexOfH = java.util.Arrays.asList(t).indexOf("h");
 		Log.i(TAG,"indexof h = "+indexOfH);
         
         editText.setText("");
         String[] numbers = helper.getNumbers(db);
         for(int i=0;i<numbers.length;++i){
         	editText.append(numbers[i]+"\n");
         }
         
         Button updateBtn = (Button)findViewById(R.id.update);
         updateBtn.setOnClickListener(new OnClickListener(){
 
 			@Override
 			public void onClick(View v) {
 				String str = editText.getText().toString();
 				str = str.trim();
 				String[] numbers = str.split("\n");
 				db.execSQL("DELETE FROM " + DBHelper.TB_NAME);
 				for(int i=0;i<numbers.length;++i){
 					ContentValues values = new ContentValues();
 			        values.put(DBHelper.NUMBER, numbers[i]);
 			        db.insertWithOnConflict(DBHelper.TB_NAME, DBHelper.ID, values,SQLiteDatabase.CONFLICT_REPLACE);
 				}
 			}
         	
         });
         
         
         
         Button toggleBtn = (Button)findViewById(R.id.toggleSpams);
         toggleBtn.setOnClickListener(new OnClickListener(){
 
 			@Override
 			public void onClick(View v) {
 				if(!isShowingSpam){
 					lv.setVisibility(View.VISIBLE);
 					editText.setVisibility(View.GONE);
 				}else{
 					lv.setVisibility(View.GONE);
 					editText.setVisibility(View.VISIBLE);
 				}
 				isShowingSpam = !isShowingSpam;
 			}
         	
         });
         
         spamCursor = db.query(DBHelper.TB_SPAM_NAME, null, null, null, null, null, null);//helper.getSpamMessage(db);
         startManagingCursor(spamCursor);
         SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
         		android.R.layout.simple_list_item_2,
         		spamCursor,
         		new String[]{DBHelper.SENDER,DBHelper.MESSAGE_BODY},
         		new int[]{android.R.id.text1,android.R.id.text2});
         lv.setAdapter(adapter);
         
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
     @Override
     public void onPause(){
     	super.onPause();
     	db.close();
     }
 }
