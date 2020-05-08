 package com.tracker;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.widget.Button;
 import android.widget.TextView;
 import android.view.View;
 import android.content.Intent;
 import java.util.Date;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import com.tracker.DbOpenHelper;
 
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.content.ContentValues;
 
 public class Main extends Activity 
 {
     /** Called when the activity is first created. */
     @Override
 	public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         DbOpenHelper dbOpenHelper = new DbOpenHelper(Main.this);
         SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
         Cursor cursor = db.rawQuery("SELECT date FROM history ORDER BY id DESC LIMIT 1", null);
         int cnt = cursor.getCount();
         db.close();
         
         if (cnt > 0){
         	cursor.moveToFirst();
             String lastDate = cursor.getString(0);        
             // start timer
             SetTimer(lastDate);
         }
         
         Button history_btn = (Button) findViewById(R.id.history_btn);
         history_btn.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View view) {
 			    Intent myIntent = new Intent(view.getContext(), History.class);
 			    startActivityForResult(myIntent, 0);
 			}
 	    });
         
         Button save_event_btn = (Button) findViewById(R.id.save_event_btn);
         
         save_event_btn.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View view) {
 			    DbOpenHelper dbOpenHelper = new DbOpenHelper(Main.this);
 			    SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
 			    ContentValues cv = new ContentValues();
 			    String lastDate = new Date().toString();
 			    cv.put(DbOpenHelper.DATE, lastDate);
 			    db.insert(DbOpenHelper.TABLE_NAME, null, cv);
 			    db.close();
 			    SetTimer(lastDate);
 			}
 	    });
     }
     
     /**
      * Retrieves the latest entry and starts timer
      */
     public void SetTimer(String lastDate)
     {
     	long dte = Date.parse(lastDate);
     	TextView txt = (TextView) findViewById(R.id.txtCounter);
        Timer timer = new Timer();
         TimerTask task = new MyTimerTask(txt, dte);
         timer.schedule(task, 1, 300);        
     }
 }
